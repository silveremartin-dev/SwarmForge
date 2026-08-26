package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles adult wasp solicitation of nutrient-rich amino acid saliva droplets from larvae in exchange for meat.
 */
public class LarvalSalivaHarvestingSystem {

    public void processLarvalSalivaHarvest(Individual adult, boolean larvaPresent) {
        if (adult == null) return;
        Species species = adult.getSpecies();
        if (species == null || !species.canHarvestLarvalSalivaDroplets()) return;

        if (larvaPresent && adult.isAlive()) {
            adult.setEnergy(Math.min(100.0f, adult.getEnergy() + 0.03f));
        }
    }
}
