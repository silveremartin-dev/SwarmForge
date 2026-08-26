package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles sowing fresh fungal spores on newly prepared chewed-wood combs.
 */
public class FungalSporeCombSystem {

    public void processFungalSporeSowing(Individual termite, boolean freshCombReady) {
        if (termite == null) return;
        Species species = termite.getSpecies();
        if (species == null || !species.canSowFungalSporeCombs()) return;

        if (freshCombReady && termite.isAlive()) {
            termite.setEnergy(Math.max(0.0f, termite.getEnergy() - 0.02f));
        }
    }
}
