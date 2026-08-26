package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles scraping and saliva mastication of weathered wood fibers for carton paper nest cell building.
 */
public class PaperPulpCartonMasticationSystem {

    public void processPaperMastication(Individual builder, boolean woodFiberAvailable) {
        if (builder == null) return;
        Species species = builder.getSpecies();
        if (species == null || !species.canMasticatePaperPulpCarton()) return;

        if (woodFiberAvailable && builder.isAlive()) {
            builder.setEnergy(Math.max(0.0f, builder.getEnergy() - 0.02f));
        }
    }
}
