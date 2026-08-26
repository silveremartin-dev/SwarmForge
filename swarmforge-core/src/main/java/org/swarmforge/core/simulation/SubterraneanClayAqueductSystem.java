package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles digging deep vertical wells down to water tables to bring moisture up to dry mounds.
 */
public class SubterraneanClayAqueductSystem {

    public void processClayAqueductDigging(Individual termite, boolean severeDrought) {
        if (termite == null) return;
        Species species = termite.getSpecies();
        if (species == null || !species.canDigSubterraneanClayAqueducts()) return;

        if (severeDrought && termite.isAlive()) {
            termite.setEnergy(Math.max(0.0f, termite.getEnergy() - 0.04f));
        }
    }
}
