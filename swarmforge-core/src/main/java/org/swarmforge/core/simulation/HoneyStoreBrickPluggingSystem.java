package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles emergency soil-brick plugging of honey vaults during wasp raids.
 */
public class HoneyStoreBrickPluggingSystem {

    public void processVaultPlugging(Individual worker, boolean waspRaidActive) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canPlugHoneyStoresBricks()) return;

        if (waspRaidActive && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.04f));
        }
    }
}
