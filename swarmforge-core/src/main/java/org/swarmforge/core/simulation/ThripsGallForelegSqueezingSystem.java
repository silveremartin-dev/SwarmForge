package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles eusocial thrips soldier raptorial foreleg crushing of gall invaders.
 */
public class ThripsGallForelegSqueezingSystem {

    public void processForelegSqueeze(Individual thripsSoldier, boolean gallInvaderDetected) {
        if (thripsSoldier == null) return;
        Species species = thripsSoldier.getSpecies();
        if (species == null || !species.canSqueezeGallIntrudersThrips()) return;

        if (gallInvaderDetected && thripsSoldier.isAlive()) {
            thripsSoldier.setEnergy(Math.max(0.0f, thripsSoldier.getEnergy() - 0.04f));
        }
    }
}
