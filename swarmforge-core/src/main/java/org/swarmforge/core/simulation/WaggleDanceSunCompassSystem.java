package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles transducing waggle dance angles relative to gravity into sun-compass flight vectors.
 */
public class WaggleDanceSunCompassSystem {

    public void processWaggleDanceEncoding(Individual bee, boolean danceAudiencePresent) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canEncodeWaggleDanceSunCompass()) return;

        if (danceAudiencePresent && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.015f));
        }
    }
}
