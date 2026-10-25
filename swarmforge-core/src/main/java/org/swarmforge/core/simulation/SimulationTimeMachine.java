/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.GodModeIntervention;
import org.swarmforge.core.event.SimulationEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages simulation state historical recording, deterministic rollback/rewind,
 * manual/automatic checkpoint management, and God-mode intervention auditing.
 */
public class SimulationTimeMachine {

    private final Simulation simulation;
    private final SimulationHistory history;
    private final List<GodModeIntervention> interventionJournal = Collections.synchronizedList(new ArrayList<>());
    private final List<SimulationCheckpoint> checkpoints = Collections.synchronizedList(new ArrayList<>());
    private int autoCheckpointInterval = 50000; // Default 50,000 ticks

    public SimulationTimeMachine(Simulation simulation, int historyCapacity, int sampleRate) {
        this.simulation = simulation;
        this.history = new SimulationHistory(historyCapacity, sampleRate);
    }

    public SimulationHistory getHistory() {
        return history;
    }

    public void restoreState(long tick) {
        simulation.setTickCount(tick);
        simulation.clearColonies();
        simulation.clearFoodSources();
        simulation.getSpatialIndex().clear();
        simulation.getFoodIndex().clear();
        if (simulation.getPheromoneGrid() != null) {
            simulation.getPheromoneGrid().clear();
        }
        if (simulation.getWaterGrid() != null) {
            simulation.getWaterGrid().clear();
        }
        if (simulation.getPredatorManager() != null) {
            simulation.getPredatorManager().clearPredators();
        }
        if (simulation.getActiveDisasters() != null) {
            simulation.getActiveDisasters().clear();
        }
        simulation.getEventQueue().clear();
    }

    public void reset(long tick) {
        restoreState(tick);
        simulation.setRandom(new java.util.Random(simulation.getMasterSeed()));
        if (this.history != null) {
            this.history.clear();
        }
        this.interventionJournal.clear();
        this.checkpoints.clear();
    }

    public void recordInitialSnapshot() {
        if (this.history != null) {
            this.history.record(SimulationSnapshot.capture(simulation));
        }
        if (this.checkpoints.isEmpty()) {
            createCheckpoint("Départ / Initial Setup (#0)");
        }
    }

    public long getHighestRecordedTick() {
        if (this.history != null) {
            long hTick = this.history.getHighestRecordedTick();
            return Math.max(simulation.getTickCount(), hTick);
        }
        return simulation.getTickCount();
    }

    public boolean rewind(int steps) {
        Simulation.State oldState = simulation.getState();
        if (oldState == Simulation.State.RUNNING) {
            simulation.pause();
        }

        boolean success = history.rewind(simulation, steps);

        if (success) {
            long curTick = simulation.getTickCount();
            for (Colony colony : simulation.getColonies()) {
                for (Individual ind : colony.getIndividuals()) {
                    ind.setLastDecisionTick(curTick);
                    ind.setCachedAction(null);
                    if (ind.getBrain() == null) {
                        ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
                    }
                }
            }
            simulation.getEventQueue().offer(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                curTick, "Rewound " + steps + " steps to tick " + curTick
            ));
        }

        return success;
    }

    public boolean seekToTick(long tick) {
        Simulation.State oldState = simulation.getState();
        if (oldState == Simulation.State.RUNNING) {
            simulation.pause();
        }

        boolean success = history.seekToTick(simulation, tick);

        if (success) {
            simulation.setTickCount(tick);
            for (Colony colony : simulation.getColonies()) {
                for (Individual ind : colony.getIndividuals()) {
                    ind.setLastDecisionTick(tick);
                    ind.setCachedAction(null);
                    if (ind.getBrain() == null) {
                        ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
                    }
                }
            }
            simulation.getEventQueue().offer(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                tick, "Seeked to tick " + tick
            ));
        }

        return success;
    }

    public int getAutoCheckpointInterval() {
        return autoCheckpointInterval;
    }

    public void setAutoCheckpointInterval(int interval) {
        this.autoCheckpointInterval = Math.max(1000, interval);
    }

    public void logIntervention(GodModeIntervention intervention) {
        if (intervention != null) {
            interventionJournal.add(intervention);
            EventBus.getInstance().publish(
                SimulationEvent.obtain(
                    SimulationEvent.EventType.GOD_MODE_INTERVENTION,
                    SimulationEvent.Severity.INFO,
                    simulation.getTickCount(),
                    "⚡ Intervention Mode Divin (" + intervention.actionType() + ") enregistrée au tick #" + simulation.getTickCount(),
                    null
                )
            );
        }
    }

    public List<GodModeIntervention> getInterventionJournal() {
        return new ArrayList<>(interventionJournal);
    }

    public synchronized SimulationCheckpoint createCheckpoint(String name) {
        SimulationSnapshot snap = SimulationSnapshot.capture(simulation);
        SimulationCheckpoint cp = new SimulationCheckpoint(name, simulation.getTickCount(), snap, interventionJournal);
        checkpoints.add(cp);
        while (checkpoints.size() > 60) {
            checkpoints.remove(1); // Keep index 0 (initial), drop oldest intermediate
        }
        return cp;
    }

    public boolean restoreCheckpoint(SimulationCheckpoint cp) {
        if (cp == null || cp.getSnapshot() == null) return false;
        cp.getSnapshot().restore(simulation);
        this.interventionJournal.clear();
        this.interventionJournal.addAll(cp.getInterventionsRecorded());
        long curTick = simulation.getTickCount();
        for (Colony colony : simulation.getColonies()) {
            for (Individual ind : colony.getIndividuals()) {
                ind.setLastDecisionTick(curTick);
                ind.setCachedAction(null);
                if (ind.getBrain() == null) {
                    ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
                }
            }
        }
        return true;
    }

    public List<SimulationCheckpoint> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public void recordSnapshot() {
        history.record(SimulationSnapshot.capture(simulation));
    }
}
