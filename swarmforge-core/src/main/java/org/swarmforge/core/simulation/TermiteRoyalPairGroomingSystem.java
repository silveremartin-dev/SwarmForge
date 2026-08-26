package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles mutual grooming and pheromonal exchange between king and queen.
 */
public class TermiteRoyalPairGroomingSystem {

    public void processRoyalPairGrooming(Individual king, Individual queen) {
        if (king == null || queen == null) return;
        Species species = king.getSpecies();
        if (species == null || !species.canExchangeRoyalPairGrooming()) return;

        if (king.isAlive() && queen.isAlive()) {
            king.setEnergy(Math.max(0.0f, king.getEnergy() - 0.005f));
        }
    }
}
