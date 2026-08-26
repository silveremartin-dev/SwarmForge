package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles depositing concentrated nectar-saliva droplets in empty cells as emergency larval food.
 */
public class WaspEmergencySalivaFoodDropSystem {

    public void processEmergencyFoodDeposit(Individual wasp, boolean foodShortage) {
        if (wasp == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canDepositLarvalFoodSalivaDrop()) return;

        if (foodShortage && wasp.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.02f));
        }
    }
}
