package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles solid mineral salt crystal foraging for larval osmotic balance.
 */
public class SaltCrystalOsmoregulationSystem {

    public void processSaltOsmoregulation(Individual forager, boolean saltDepositDetected) {
        if (forager == null) return;
        Species species = forager.getSpecies();
        if (species == null || !species.canForageSaltCrystalsOsmoregulation()) return;

        if (saltDepositDetected && forager.isAlive()) {
            forager.setEnergy(Math.max(0.0f, forager.getEnergy() - 0.02f));
        }
    }
}
