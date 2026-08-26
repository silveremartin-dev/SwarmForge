package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles oral regurgitation of pre-digested wood pulp to passalid beetle grubs lacking gut microbiota.
 */
public class PassalidWoodFrassTrophallaxisSystem {

    public void processWoodFrassTrophallaxis(Individual parent, Individual grub) {
        if (parent == null || grub == null) return;
        Species species = parent.getSpecies();
        if (species == null || !species.canTrophallaxisPassalidWoodFrass()) return;

        if (parent.isAlive() && grub.isAlive()) {
            parent.setEnergy(Math.max(0.0f, parent.getEnergy() - 0.015f));
            grub.setEnergy(Math.min(100.0f, grub.getEnergy() + 0.02f));
        }
    }
}
