package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles rhythmic shearing of foliage into transportable crescent leaf discs.
 */
public class AttaLeafCrescentShearSystem {

    public void processLeafCrescentShearing(Individual worker, boolean leafDiscovered) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canShearLeafCrescentMandible()) return;

        if (leafDiscovered && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.03f));
        }
    }
}
