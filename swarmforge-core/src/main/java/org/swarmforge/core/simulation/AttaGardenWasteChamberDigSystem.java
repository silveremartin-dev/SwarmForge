package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles digging deep subterranean waste chambers for toxic spent fungal substrate.
 */
public class AttaGardenWasteChamberDigSystem {

    public void processGardenWasteChamberDigging(Individual worker, boolean spentSubstrateAccumulated) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canExcavateGardenWasteChambers()) return;

        if (spentSubstrateAccumulated && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.035f));
        }
    }
}
