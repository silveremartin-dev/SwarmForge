/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.event.SimulationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Coordinates single-tick execution across spatial indexing, biological entity lifecycles,
 * physical engine stepping, and macro-foraging.
 */
final class SimulationTickExecutor {

    private SimulationTickExecutor() {}

    static void executeTick(
            Simulation sim,
            long currentTick,
            float simulationStepSeconds,
            double accumulatedSimulationSeconds,
            Queue<SimulationEvent> eventQueue,
            SimulationEntityUpdater entityUpdater,
            SimulationTimeMachine timeMachine
    ) {
        sim.getEcsWorldManager().step(simulationStepSeconds);

        // Pre-populate spatial index for individuals before brain decision processing
        sim.getSpatialIndex().clear();
        for (Colony colony : sim.getColonies()) {
            for (Individual ind : colony.getIndividuals()) {
                if (ind.isAlive()) {
                    sim.getSpatialIndex().insert(ind, ind.getX(), ind.getY(), ind.getZ());
                }
            }
        }

        // Rebuild food index and process depleted sources
        entityUpdater.updateFoodSources(
                sim.getMutableFoodSources(),
                sim.getFoodIndex(),
                currentTick,
                accumulatedSimulationSeconds,
                eventQueue
        );

        if (currentTick % 60 == 0) {
            for (Colony colony : sim.getColonies()) {
                colony.getStatistics().record(
                        currentTick,
                        colony.getPopulation(),
                        colony.getFoodStored(),
                        colony.getWaterStored(),
                        colony.getTotalDied(),
                        colony.getTotalBorn()
                );
            }
        }

        // Use parallel streams with virtual thread executor for better scalability
        SimulationContext context = new SimulationContextImpl(sim);
        ConcurrentLinkedQueue<Individual> livingIndividuals = new ConcurrentLinkedQueue<>();

        for (Colony colony : sim.getColonies()) {
            colony.getIndividuals().forEach(individual ->
                    entityUpdater.updateIndividual(
                            individual, colony, context, simulationStepSeconds, currentTick, eventQueue, livingIndividuals
                    ));
            colony.removeDeadIndividuals();
            colony.tick(); // Update internal state (Fungus Gardens, Consumption)
            colony.tickPhysicalEngines(
                    sim.getWeather() != null ? sim.getWeather().getTemperature() : 20.0f,
                    org.swarmforge.core.domain.TerrariumCell.DEFAULT_CO2,
                    sim.getWeather() != null ? sim.getWeather().getWindSpeedMs() : 1.0f,
                    sim.getSoilHydricCoupling() != null ? (sim.getSoilHydricCoupling().getMoistureAtDepth(0) / 100.0f) : 0.2f,
                    simulationStepSeconds,
                    sim.getRandom()
            );
        }

        // Re-insert moved living individuals into spatial index
        sim.getSpatialIndex().clear();
        Map<UUID, Individual> indMap = new HashMap<>();
        for (Individual ind : livingIndividuals) {
            sim.getSpatialIndex().insert(ind, ind.getX(), ind.getY(), ind.getZ());
            indMap.put(ind.getId(), ind);
        }

        Map<UUID, Colony> colMap = new HashMap<>();
        for (Colony col : sim.getColonies()) {
            colMap.put(col.getId(), col);
        }
        sim.getMacroForagingSystem().update(simulationStepSeconds, indMap, colMap);
        sim.getSymbiosisSystem().tick();

        while (eventQueue.size() > 1000) {
            eventQueue.poll();
        }

        // Auto periodic checkpoint creation every autoCheckpointInterval ticks (default 50,000) for live checkpoint manager
        if (currentTick > 0 && currentTick % timeMachine.getAutoCheckpointInterval() == 0) {
            timeMachine.createCheckpoint("Auto-Snapshot (Tick #" + currentTick + ")");
        }

        // Record history snapshot periodically
        timeMachine.getHistory().recordIfNeeded(sim);
    }
}
