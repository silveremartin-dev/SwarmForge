package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles mixing wood dust with salivary secretions to seal damaged gallery tunnels.
 */
public class PassalidWoodWallPlasterSystem {

    public void processWoodWallPlastering(Individual passalid, boolean galleryBreached) {
        if (passalid == null) return;
        Species species = passalid.getSpecies();
        if (species == null || !species.canPlasterWoodWallGallery()) return;

        if (galleryBreached && passalid.isAlive()) {
            passalid.setEnergy(Math.max(0.0f, passalid.getEnergy() - 0.025f));
        }
    }
}
