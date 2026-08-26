package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles biting off seed radicles to prevent stored seeds from germinating inside moist underground granaries.
 */
public class HarvesterSeedRadicleMutilationSystem {

    public void processSeedRadicleMutilation(Individual worker, boolean seedStoredInGranary) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canMutilateSeedRadicles()) return;

        if (seedStoredInGranary && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.015f));
        }
    }
}
