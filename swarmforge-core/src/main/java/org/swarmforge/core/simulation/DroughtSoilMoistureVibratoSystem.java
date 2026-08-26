package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles soil-moisture drought vibrato dance recruiting deep soil digging.
 */
public class DroughtSoilMoistureVibratoSystem {

    public void processDroughtVibrato(Individual dancer, double soilMoisturePercent) {
        if (dancer == null) return;
        Species species = dancer.getSpecies();
        if (species == null || !species.canPerformDroughtVibratoDance()) return;

        if (soilMoisturePercent < 15.0) {
            dancer.setEnergy(Math.max(0.0f, dancer.getEnergy() - 0.01f));
        }
    }
}
