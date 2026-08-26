package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles pressing hairless ventral abdomen onto brood cells to transfer metabolic heat (35°C) in bumblebees.
 */
public class BumblebeeAbdominalIncubationSystem {

    public void processAbdominalIncubation(Individual queenOrWorker, boolean broodCold) {
        if (queenOrWorker == null) return;
        Species species = queenOrWorker.getSpecies();
        if (species == null || !species.canIncubateBroodAbdominalHeat()) return;

        if (broodCold && queenOrWorker.isAlive()) {
            queenOrWorker.setEnergy(Math.max(0.0f, queenOrWorker.getEnergy() - 0.02f));
        }
    }
}
