package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles secreting liquid waxy droplets to plug natural openings in plant galls against predators.
 */
public class AphidGallClosingFluidSystem {

    public void processGallClosingSecretion(Individual aphid, boolean gallOpeningExposed) {
        if (aphid == null) return;
        Species species = aphid.getSpecies();
        if (species == null || !species.canSecreteGallClosingFluid()) return;

        if (gallOpeningExposed && aphid.isAlive()) {
            aphid.setEnergy(Math.max(0.0f, aphid.getEnergy() - 0.02f));
        }
    }
}
