package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles extending glossal tongue into deep floral nectaries to lap high-viscosity nectar.
 */
public class BumblebeeNectarTongueLappingSystem {

    public void processNectarTongueLapping(Individual bumblebee, boolean deepNectaryVisited) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canLapNectarTongueExtension()) return;

        if (deepNectaryVisited && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.min(100.0f, bumblebee.getEnergy() + 0.04f));
        }
    }
}
