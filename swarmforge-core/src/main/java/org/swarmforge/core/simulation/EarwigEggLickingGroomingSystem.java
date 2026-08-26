package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles maternal mouthpart licking and salivary coating of earwig eggs to prevent fungal germination.
 */
public class EarwigEggLickingGroomingSystem {

    public void processEggLickingGrooming(Individual motherEarwig, boolean eggClutchPresent) {
        if (motherEarwig == null) return;
        Species species = motherEarwig.getSpecies();
        if (species == null || !species.canPerformEggLickingGrooming()) return;

        if (eggClutchPresent && motherEarwig.isAlive()) {
            motherEarwig.setEnergy(Math.max(0.0f, motherEarwig.getEnergy() - 0.01f));
        }
    }
}
