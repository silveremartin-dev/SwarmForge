package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles multi-spider wrapping of large captured grasshoppers in dense silk sheets.
 */
public class SpiderCommunalSilkPreyWrapSystem {

    public void processCommunalPreyWrap(Individual spider, boolean largePreyEntangled) {
        if (spider == null) return;
        Species species = spider.getSpecies();
        if (species == null || !species.canWrapPreyInCommunalSilk()) return;

        if (largePreyEntangled && spider.isAlive()) {
            spider.setEnergy(Math.max(0.0f, spider.getEnergy() - 0.03f));
        }
    }
}
