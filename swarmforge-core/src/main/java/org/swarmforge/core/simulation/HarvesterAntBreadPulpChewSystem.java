package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles chewing seed starch mixed with amylase saliva to produce ant bread.
 */
public class HarvesterAntBreadPulpChewSystem {

    public void processAntBreadChewing(Individual worker, boolean seedAvailable) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canChewSeedHuskBreadPulp()) return;

        if (seedAvailable && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
