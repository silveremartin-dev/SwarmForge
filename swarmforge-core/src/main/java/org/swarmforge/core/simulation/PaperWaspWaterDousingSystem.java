package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles carrying water droplets in crop and spitting them on paper comb cells combined with fanning to evaporatively cool nest.
 */
public class PaperWaspWaterDousingSystem {

    public void processWaterDousingCooling(Individual wasp, boolean ambientOverheating) {
        if (wasp == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canDouseNestWaterCooling()) return;

        if (ambientOverheating && wasp.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.02f));
        }
    }
}
