package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles building multi-layered silk tent hammocks for group thermal buffering.
 */
public class CaterpillarSilkHammockTentSystem {

    public void processSilkHammockWeaving(Individual caterpillar, boolean branchForkReached) {
        if (caterpillar == null) return;
        Species species = caterpillar.getSpecies();
        if (species == null || !species.canWeaveSocialSilkHammock()) return;

        if (branchForkReached && caterpillar.isAlive()) {
            caterpillar.setEnergy(Math.max(0.0f, caterpillar.getEnergy() - 0.025f));
        }
    }
}
