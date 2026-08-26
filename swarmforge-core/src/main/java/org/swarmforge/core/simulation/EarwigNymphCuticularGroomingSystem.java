package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles licking cuticular surfaces of newly molted nymphs to remove exuviae and pathogens.
 */
public class EarwigNymphCuticularGroomingSystem {

    public void processNymphCuticularGrooming(Individual motherEarwig, boolean newlyMoltedNymph) {
        if (motherEarwig == null) return;
        Species species = motherEarwig.getSpecies();
        if (species == null || !species.canGroomNymphCuticularSurface()) return;

        if (newlyMoltedNymph && motherEarwig.isAlive()) {
            motherEarwig.setEnergy(Math.max(0.0f, motherEarwig.getEnergy() - 0.015f));
        }
    }
}
