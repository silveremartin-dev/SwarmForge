package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles secretion of proteinaceous pap droplets into egg cells before hatching in hover wasps.
 */
public class StenogastrinePapFoodSystem {

    public void processPapFoodDelivery(Individual female, boolean eggLaid) {
        if (female == null) return;
        Species species = female.getSpecies();
        if (species == null || !species.canDeliverStenogastrinePapFood()) return;

        if (eggLaid && female.isAlive()) {
            female.setEnergy(Math.max(0.0f, female.getEnergy() - 0.02f));
        }
    }
}
