package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles dropping prey carcasses and molted cuticles through dedicated vertical silk chute funnels.
 */
public class SpiderGarbageChuteSystem {

    public void processSpiderGarbageEjection(Individual spider, boolean carcassPresent) {
        if (spider == null) return;
        Species species = spider.getSpecies();
        if (species == null || !species.canEjectGarbageChuteRefuse()) return;

        if (carcassPresent && spider.isAlive()) {
            spider.setEnergy(Math.max(0.0f, spider.getEnergy() - 0.01f));
        }
    }
}
