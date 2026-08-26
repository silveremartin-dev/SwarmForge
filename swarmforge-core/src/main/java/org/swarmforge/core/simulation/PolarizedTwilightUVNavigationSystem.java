package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles navigation using twilight UV sky polarization patterns.
 */
public class PolarizedTwilightUVNavigationSystem {

    public void processTwilightNavigation(Individual navigator, boolean isTwilight) {
        if (navigator == null) return;
        Species species = navigator.getSpecies();
        if (species == null || !species.canNavigatePolarizedTwilightUV()) return;

        if (isTwilight && navigator.isAlive()) {
            navigator.setEnergy(Math.max(0.0f, navigator.getEnergy() - 0.01f));
        }
    }
}
