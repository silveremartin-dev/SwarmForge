package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles dominance mounting and antennal drumming to suppress worker ovarian development.
 */
public class WaspDominanceMountingSystem {

    public void processDominanceMounting(Individual queen, Individual subordinateWorker) {
        if (queen == null || subordinateWorker == null) return;
        Species species = queen.getSpecies();
        if (species == null || !species.canPerformDominanceMounting()) return;

        if (queen.isAlive() && subordinateWorker.isAlive()) {
            queen.setEnergy(Math.max(0.0f, queen.getEnergy() - 0.01f));
        }
    }
}
