package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles vibrating flight muscles at high frequency to shake pollen free from anthers.
 */
public class BuzzPollinationSystem {

    public void processBuzzPollination(Individual bumblebee, boolean solanaceousFlowerVisited) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canPerformBuzzPollinationSonication()) return;

        if (solanaceousFlowerVisited && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.max(0.0f, bumblebee.getEnergy() - 0.03f));
        }
    }
}
