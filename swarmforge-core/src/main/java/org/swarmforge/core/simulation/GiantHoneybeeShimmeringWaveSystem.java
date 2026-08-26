package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles synchronized abdomen flipping creating visible shimmering waves across open-nest giant honeybee comb curtain.
 */
public class GiantHoneybeeShimmeringWaveSystem {

    public void processShimmeringWave(Individual bee, boolean hornetScoutDetected) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canPerformAntiPredatorShimmeringWave()) return;

        if (hornetScoutDetected && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.015f));
        }
    }
}
