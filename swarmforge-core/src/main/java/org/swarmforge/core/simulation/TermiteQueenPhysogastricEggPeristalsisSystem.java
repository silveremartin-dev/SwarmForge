package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles rhythmic abdominal peristalsis producing 30,000 eggs per day.
 */
public class TermiteQueenPhysogastricEggPeristalsisSystem {

    public void processQueenEggPeristalsis(Individual queen, boolean royalChamberActive) {
        if (queen == null) return;
        Species species = queen.getSpecies();
        if (species == null || !species.canPerformQueenPhysogastricPeristalsis()) return;

        if (royalChamberActive && queen.isAlive()) {
            queen.setEnergy(Math.max(0.0f, queen.getEnergy() - 0.05f));
        }
    }
}
