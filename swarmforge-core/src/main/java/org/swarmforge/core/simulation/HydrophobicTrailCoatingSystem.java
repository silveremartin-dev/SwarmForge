package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Manages lipid film coating on flood-prone lower gallery walls to reduce water infiltration.
 */
public class HydrophobicTrailCoatingSystem {

    public void processHydrophobicCoating(Individual worker, boolean galleryFloodRisk) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canApplyHydrophobicTrailCoating()) return;

        if (galleryFloodRisk && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.04f));
        }
    }
}
