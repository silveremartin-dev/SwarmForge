package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles weaving dry twigs, leaves, and prey husks into outer silk web walls to disguise the nest from bird predators.
 */
public class SpiderWebDebrisCamouflageSystem {

    public void processWebCamouflage(Individual spider, boolean debrisAvailable) {
        if (spider == null) return;
        Species species = spider.getSpecies();
        if (species == null || !species.canCamouflageWebWithPlantDebris()) return;

        if (debrisAvailable && spider.isAlive()) {
            spider.setEnergy(Math.max(0.0f, spider.getEnergy() - 0.02f));
        }
    }
}
