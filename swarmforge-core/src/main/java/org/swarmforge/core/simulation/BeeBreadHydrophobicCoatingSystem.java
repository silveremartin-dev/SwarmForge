package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles lipid secretion coating on stored pollen/bee-bread preventing mold growth.
 */
public class BeeBreadHydrophobicCoatingSystem {

    public void processBeeBreadCoating(Individual bee, boolean pollenStored) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canApplyBeeBreadHydrophobicCoating()) return;

        if (pollenStored && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.02f));
        }
    }
}
