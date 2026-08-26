package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles periodically moving and turning stored seed stockpiles to prevent humidity accumulation.
 */
public class GranarySeedAerationSystem {

    public void processSeedAeration(Individual worker, boolean granaryHumidityHigh) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canTurnGranarySeedsAeration()) return;

        if (granaryHumidityHigh && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
