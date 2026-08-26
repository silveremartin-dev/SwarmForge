package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles distending gaster with liquid nectar as hanging living honeypot repletes.
 */
public class HoneypotRepleteStorageSystem {

    public void processHoneypotRepleteStorage(Individual replete, boolean nectarSurplusAvailable) {
        if (replete == null) return;
        Species species = replete.getSpecies();
        if (species == null || !species.canStoreNectarAsHoneypotReplete()) return;

        if (nectarSurplusAvailable && replete.isAlive()) {
            replete.setEnergy(Math.min(100.0f, replete.getEnergy() + 0.05f));
        }
    }
}
