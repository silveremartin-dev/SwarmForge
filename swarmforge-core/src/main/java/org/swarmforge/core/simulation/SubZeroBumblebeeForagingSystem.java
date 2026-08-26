package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles flight muscle thermogenic shivering allowing Arctic bumblebees to forage at 0°C.
 */
public class SubZeroBumblebeeForagingSystem {

    public void processSubZeroForaging(Individual bumblebee, boolean freezingWeather) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canForageSubZeroBumblebee()) return;

        if (freezingWeather && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.max(0.0f, bumblebee.getEnergy() - 0.04f));
        }
    }
}
