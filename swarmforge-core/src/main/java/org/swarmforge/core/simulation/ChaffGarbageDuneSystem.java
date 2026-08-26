package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles dumping seed chaff refuse in specialized exterior crescent dunes in harvester ants.
 */
public class ChaffGarbageDuneSystem {

    public void processChaffGarbageDumping(Individual worker, boolean carryingChaff) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canConstructChaffGarbageDunes()) return;

        if (carryingChaff && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.015f));
        }
    }
}
