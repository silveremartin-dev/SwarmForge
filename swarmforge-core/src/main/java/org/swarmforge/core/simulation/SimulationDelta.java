/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.TerrariumCell;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a delta (incremental update) in simulation state.
 * Used for efficient network transmission - only changed data is sent.
 *
 * @param tick                 Current simulation tick
 * @param individualUpdates    Changed individuals
 * @param cellUpdates          Changed cells
 * @param newIndividualIds     Newly spawned individuals
 * @param removedIndividualIds Dead/removed individuals
 */
public record SimulationDelta(
        long tick,
        List<IndividualUpdate> individualUpdates,
        List<CellUpdate> cellUpdates,
        List<String> newIndividualIds,
        List<String> removedIndividualIds) {
    /**
     * Create an empty delta for a tick.
     */
    public static SimulationDelta empty(long tick) {
        return new SimulationDelta(tick, List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Minimal update for an individual (position + state).
     */
    public record IndividualUpdate(
            String id,
            float x,
            float y,
            float z,
            float heading,
            float health,
            float energy,
            boolean alive,
            Individual.CarriedItem carried) {
        public static IndividualUpdate from(Individual ind) {
            return new IndividualUpdate(
                    ind.getId().toString(),
                    ind.getX(), ind.getY(), ind.getZ(),
                    ind.getHeading(),
                    ind.getHealth(),
                    ind.getEnergy(),
                    ind.isAlive(),
                    ind.getCarriedItem());
        }
    }

    /**
     * Minimal update for a cell (morton key + data).
     */
    public record CellUpdate(
            long mortonKey,
            TerrariumCell.Material material,
            float[] pheromones) {
        public static CellUpdate from(long key, TerrariumCell cell) {
            return new CellUpdate(key, cell.material(), cell.pheromones().clone());
        }
    }
}
