/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

import org.swarmforge.core.spatial.Morton3D;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks changes to simulation state for delta generation.
 * Collects individual and cell updates between ticks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DeltaTracker {

    private final Map<String, SimulationDelta.IndividualUpdate> individualUpdates;
    private final Map<Long, SimulationDelta.CellUpdate> cellUpdates;
    private final List<String> newIndividuals;
    private final List<String> removedIndividuals;

    public DeltaTracker() {
        this.individualUpdates = new ConcurrentHashMap<>();
        this.cellUpdates = new ConcurrentHashMap<>();
        this.newIndividuals = new ArrayList<>();
        this.removedIndividuals = new ArrayList<>();
    }

    /**
     * Track an individual update.
     */
    public void trackIndividual(Individual ind) {
        individualUpdates.put(
                ind.getId().toString(),
                SimulationDelta.IndividualUpdate.from(ind));
    }

    /**
     * Track a new individual spawn.
     */
    public void trackNewIndividual(Individual ind) {
        newIndividuals.add(ind.getId().toString());
        trackIndividual(ind);
    }

    /**
     * Track an individual removal.
     */
    public void trackRemovedIndividual(String id) {
        removedIndividuals.add(id);
        individualUpdates.remove(id);
    }

    /**
     * Track a cell update.
     */
    public void trackCell(int x, int y, int z, org.swarmforge.core.domain.TerrariumCell cell) {
        long key = Morton3D.encode(x, y, z);
        cellUpdates.put(key, SimulationDelta.CellUpdate.from(key, cell));
    }

    /**
     * Build and return the delta, then reset tracker.
     */
    public SimulationDelta buildDelta(long tick) {
        SimulationDelta delta = new SimulationDelta(
                tick,
                new ArrayList<>(individualUpdates.values()),
                new ArrayList<>(cellUpdates.values()),
                new ArrayList<>(newIndividuals),
                new ArrayList<>(removedIndividuals));
        reset();
        return delta;
    }

    /**
     * Reset tracker for next tick.
     */
    public void reset() {
        individualUpdates.clear();
        cellUpdates.clear();
        newIndividuals.clear();
        removedIndividuals.clear();
    }

    /**
     * Check if there are any updates.
     */
    public boolean hasUpdates() {
        return !individualUpdates.isEmpty() ||
                !cellUpdates.isEmpty() ||
                !newIndividuals.isEmpty() ||
                !removedIndividuals.isEmpty();
    }
}
