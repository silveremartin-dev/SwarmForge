package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles laying unfertilized trophic eggs to feed queen and young larvae during colony founding.
 */
public class TrophicEggNourishmentSystem {

    public void processTrophicEggLaying(Individual worker, boolean foundingPhase) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canLayTrophicNourishmentEggs()) return;

        if (foundingPhase && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.03f));
        }
    }
}
