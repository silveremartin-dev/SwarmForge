/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ColonyListener;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Observes colony demographic lifecycle events (oviposition, birth, death) and publishes
 * structured telemetry events to the simulation EventBus and local queue.
 */
public class SimulationColonyObserver implements ColonyListener {

    private final Simulation simulation;

    public SimulationColonyObserver(Simulation simulation) {
        this.simulation = simulation;
    }

    private static String formatCaste(Individual.Caste caste) {
        if (caste == null) return "Individual";
        return switch (caste) {
            case QUEEN -> "Queen";
            case SOLDIER -> "Soldier";
            case MALE -> "Male";
            case NURSE -> "Nurse";
            case FORAGER -> "Forager";
            case WORKER -> "Worker";
        };
    }

    @Override
    public void onBirth(Colony colony, Individual individual) {
        SimulationEvent.EventType type;
        String shortId = "#" + individual.getId().toString().substring(0, 8).toUpperCase();
        String colName = (colony.getSpeciesName() != null && !colony.getSpeciesName().isEmpty())
                ? colony.getSpeciesName() : "Colony #" + colony.getId().toString().substring(0, 5);
        String casteStr = formatCaste(individual.getCaste());
        int x = (int) individual.getX();
        int y = (int) individual.getY();
        int z = (int) individual.getZ();

        String message;
        if (individual.getLifeStage() == Individual.LifeStage.EGG) {
            type = SimulationEvent.EventType.WORKER_BORN;
            message = String.format("Egg Laying: Queen of %s laid an egg (%s %s) at (%d, %d, %d)",
                    colName, casteStr, shortId, x, y, z);
        } else {
            type = switch (individual.getCaste()) {
                case QUEEN -> SimulationEvent.EventType.QUEEN_BORN;
                case SOLDIER -> SimulationEvent.EventType.SOLDIER_BORN;
                default -> SimulationEvent.EventType.WORKER_BORN;
            };
            message = String.format("Birth: %s %s in colony %s at (%d, %d, %d)",
                    casteStr, shortId, colName, x, y, z);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("colony", colName);
        data.put("colonyId", colony.getId().toString());
        data.put("individualId", shortId);
        data.put("caste", individual.getCaste().name());
        data.put("stage", individual.getLifeStage().name());
        data.put("job", individual.getJob().name());
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);

        SimulationEvent event = SimulationEvent.obtain(type, SimulationEvent.Severity.INFO, simulation.getTickCount(), message, data);
        event.setSimTimeSeconds(simulation.getAccumulatedSimulationSeconds());
        simulation.getEventQueue().offer(event);
        EventBus.getInstance().publish(event);
    }

    @Override
    public void onDeath(Colony colony, Individual individual) {
        SimulationEvent.EventType type = switch (individual.getCaste()) {
            case QUEEN -> SimulationEvent.EventType.QUEEN_DIED;
            case SOLDIER -> SimulationEvent.EventType.SOLDIER_DIED;
            default -> SimulationEvent.EventType.WORKER_DIED;
        };
        SimulationEvent.Severity severity = (individual.getCaste() == Individual.Caste.QUEEN)
                ? SimulationEvent.Severity.CRITICAL
                : SimulationEvent.Severity.WARNING;

        String shortId = "#" + individual.getId().toString().substring(0, 8).toUpperCase();
        String colName = (colony.getSpeciesName() != null && !colony.getSpeciesName().isEmpty())
                ? colony.getSpeciesName() : "Colony #" + colony.getId().toString().substring(0, 5);
        String casteStr = formatCaste(individual.getCaste());
        String cause = (individual.getCauseOfDeath() != null && !individual.getCauseOfDeath().isEmpty())
                ? individual.getCauseOfDeath() : "Unknown";
        double ageDays = individual.getAge() / 86400.0;
        int x = (int) individual.getX();
        int y = (int) individual.getY();
        int z = (int) individual.getZ();

        Map<String, Object> data = new HashMap<>();
        data.put("colony", colName);
        data.put("colonyId", colony.getId().toString());
        data.put("individualId", shortId);
        data.put("caste", individual.getCaste().name());
        data.put("ageDays", String.format(Locale.US, "%.1f j", ageDays));
        data.put("age", String.format(Locale.US, "%.1f jours", ageDays));
        data.put("cause", cause);
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);

        String message = String.format(Locale.US, "Death: %s %s (Cause: %s, Age: %.1f jours) in colony %s at (%d, %d, %d)",
                casteStr, shortId, cause, ageDays, colName, x, y, z);
        SimulationEvent event = SimulationEvent.obtain(type, severity, simulation.getTickCount(), message, data);
        event.setSimTimeSeconds(simulation.getAccumulatedSimulationSeconds());
        simulation.getEventQueue().offer(event);
        EventBus.getInstance().publish(event);
    }
}
