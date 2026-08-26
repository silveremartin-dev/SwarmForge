package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles nocturnal thermal infrared hunting for sleeping vertebrate prey.
 */
public class NocturnalInfraredHuntingSystem {

    public void processInfraredHunting(Individual hunter, boolean isNightTime, boolean thermalTargetDetected) {
        if (hunter == null) return;
        Species species = hunter.getSpecies();
        if (species == null || !species.canHuntNocturnalInfrared()) return;

        if (isNightTime && thermalTargetDetected && hunter.isAlive()) {
            hunter.setEnergy(Math.min(100.0f, hunter.getEnergy() + 0.05f));
        }
    }
}
