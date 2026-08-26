package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles metabolic glycerol synthesis prior to winter freezing temperatures.
 */
public class GlycerolCryoprotectionSystem {

    public void processGlycerolSynthesis(Individual worker, boolean subZeroTemperature) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canSynthesizeGlycerolCryoprotection()) return;

        if (subZeroTemperature && worker.isAlive()) {
            worker.setHealth(Math.min(100.0f, worker.getHealth() + 0.02f));
        }
    }
}
