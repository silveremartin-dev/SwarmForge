package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles solitary fertilized autumn queen digging subterranean overwintering hibernacula in soil banks.
 */
public class QueenHibernationBurrowSystem {

    public void processQueenHibernationBurrow(Individual queen, boolean autumnCooling) {
        if (queen == null) return;
        Species species = queen.getSpecies();
        if (species == null || !species.canExcavateHibernationBurrow()) return;

        if (autumnCooling && queen.isAlive()) {
            queen.setEnergy(Math.max(0.0f, queen.getEnergy() - 0.04f));
        }
    }
}
