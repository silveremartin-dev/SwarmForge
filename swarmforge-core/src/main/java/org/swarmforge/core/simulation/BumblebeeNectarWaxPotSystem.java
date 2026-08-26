package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles molding urn-shaped wax pots near nest entrance for emergency honey storage in bumblebees.
 */
public class BumblebeeNectarWaxPotSystem {

    public void processNectarWaxPotConstruction(Individual bumblebee, boolean waxAvailable) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canConstructNectarWaxPots()) return;

        if (waxAvailable && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.max(0.0f, bumblebee.getEnergy() - 0.03f));
        }
    }
}
