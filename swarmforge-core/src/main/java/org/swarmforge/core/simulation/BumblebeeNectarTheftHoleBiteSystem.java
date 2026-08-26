package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles piercing flower corolla bases to rob nectar directly without flower pollination.
 */
public class BumblebeeNectarTheftHoleBiteSystem {

    public void processNectarTheftBite(Individual bumblebee, boolean deepCorollaFlower) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canBiteNectarTheftHoles()) return;

        if (deepCorollaFlower && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.min(100.0f, bumblebee.getEnergy() + 0.03f));
        }
    }
}
