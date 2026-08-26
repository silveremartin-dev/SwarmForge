package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles scraping weathered wood fibres with mandibles to produce paper pulp carton.
 */
public class WoodPulpCartonScrapeSystem {

    public void processWoodPulpScraping(Individual wasp, boolean dryWoodFound) {
        if (wasp == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canScrapeWoodPulpCarton()) return;

        if (dryWoodFound && wasp.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.02f));
        }
    }
}
