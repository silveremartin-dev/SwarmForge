package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles high-frequency thorax muscular vibration (sonication at 300 Hz) to dislodge pollen from pore-bearing flowers.
 */
public class BuzzPollinationSonicationSystem {

    public void processBuzzPollination(Individual bumblebee, boolean solanaceousFlowerPresent) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canPerformBuzzPollination()) return;

        if (solanaceousFlowerPresent && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.max(0.0f, bumblebee.getEnergy() - 0.03f));
        }
    }
}
