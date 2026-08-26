package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles plastering gallery walls with frass and wood pulp to prevent resin inundation in ambrosia beetles.
 */
public class BeetleFrassGalleryPlasterSystem {

    public void processFrassPlastering(Individual beetle, boolean resinLeakDetected) {
        if (beetle == null) return;
        Species species = beetle.getSpecies();
        if (species == null || !species.canPlasterFrassGalleryWalls()) return;

        if (resinLeakDetected && beetle.isAlive()) {
            beetle.setEnergy(Math.max(0.0f, beetle.getEnergy() - 0.03f));
        }
    }
}
