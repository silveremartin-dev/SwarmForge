package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles squeezing predatory fly larvae against plant stems using enlarged spinous forelegs.
 */
public class AphidForelegIntruderSqueezeSystem {

    public void processForelegIntruderSqueeze(Individual aphidSoldier, boolean predatorPresent) {
        if (aphidSoldier == null) return;
        Species species = aphidSoldier.getSpecies();
        if (species == null || !species.canSqueezeIntrudersWithForelegs()) return;

        if (predatorPresent && aphidSoldier.isAlive()) {
            aphidSoldier.setEnergy(Math.max(0.0f, aphidSoldier.getEnergy() - 0.025f));
        }
    }
}
