package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles chewing micro-perforations through clay chamber walls to maintain optimal CO2 exchange for fungus gardens.
 */
public class ClayWallFungalAerationSystem {

    public void processClayWallAeration(Individual termite, boolean highCO2Level) {
        if (termite == null) return;
        Species species = termite.getSpecies();
        if (species == null || !species.canAerateFungalCombChambers()) return;

        if (highCO2Level && termite.isAlive()) {
            termite.setEnergy(Math.max(0.0f, termite.getEnergy() - 0.02f));
        }
    }
}
