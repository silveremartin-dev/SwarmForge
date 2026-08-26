package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles cooperative spinning of massive capture webs in social spiders.
 */
public class CommunalSpiderSilkSystem {

    public void processCommunalSilkWeaving(Individual spider, boolean captureWebDamaged) {
        if (spider == null) return;
        Species species = spider.getSpecies();
        if (species == null || !species.canWeaveCommunalSpiderSilk()) return;

        if (captureWebDamaged && spider.isAlive()) {
            spider.setEnergy(Math.max(0.0f, spider.getEnergy() - 0.02f));
        }
    }
}
