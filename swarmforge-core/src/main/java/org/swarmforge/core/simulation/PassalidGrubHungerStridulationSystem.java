package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles larval stridulation chirps soliciting wood frass regurgitation from adult guardians.
 */
public class PassalidGrubHungerStridulationSystem {

    public void processGrubHungerStridulation(Individual grub, boolean hungry) {
        if (grub == null) return;
        Species species = grub.getSpecies();
        if (species == null || !species.canStridulateLarvalHungerChirp()) return;

        if (hungry && grub.isAlive()) {
            grub.setEnergy(Math.max(0.0f, grub.getEnergy() - 0.01f));
        }
    }
}
