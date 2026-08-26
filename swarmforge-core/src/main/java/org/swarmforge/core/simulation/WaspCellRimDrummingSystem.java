package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles rhythmic abdominal drumming against paper cell walls to alert workers of nearby threats.
 */
public class WaspCellRimDrummingSystem {

    public void processCellRimDrumming(Individual waspGuard, boolean threatDetected) {
        if (waspGuard == null) return;
        Species species = waspGuard.getSpecies();
        if (species == null || !species.canDrumAbdomenWaspCellRim()) return;

        if (threatDetected && waspGuard.isAlive()) {
            waspGuard.setEnergy(Math.max(0.0f, waspGuard.getEnergy() - 0.01f));
        }
    }
}
