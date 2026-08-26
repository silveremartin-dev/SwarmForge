package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles feeding molted chitinous exuviae back to larvae to recycle essential nitrogenous nutrients.
 */
public class LarvalExuviaChitinRecyclingSystem {

    public void processExuviaRecycling(Individual adultBeetle, boolean exuviaAvailable) {
        if (adultBeetle == null) return;
        Species species = adultBeetle.getSpecies();
        if (species == null || !species.canFeedLarvaeExuviaRecycling()) return;

        if (exuviaAvailable && adultBeetle.isAlive()) {
            adultBeetle.setEnergy(Math.max(0.0f, adultBeetle.getEnergy() - 0.01f));
        }
    }
}
