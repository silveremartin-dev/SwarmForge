package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles queen abdominal stridulation triggering synchronized egg-laying across co-queens.
 */
public class EggLayingSynchronizationStridulationSystem {

    public void processEggSynchronization(Individual queen, boolean polygyneColony) {
        if (queen == null) return;
        Species species = queen.getSpecies();
        if (species == null || !species.canStridulateEggLayingSynchronization()) return;

        if (polygyneColony && queen.isAlive()) {
            queen.setEnergy(Math.max(0.0f, queen.getEnergy() - 0.01f));
        }
    }
}
