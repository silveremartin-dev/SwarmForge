package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles mixing salivary gelatinous secretions with fine plant fibers to construct delicate hair-thin suspended nests.
 */
public class StenogastrinePaperJellyWeavingSystem {

    public void processPaperJellyWeaving(Individual hoverWasp, boolean plantFibersAvailable) {
        if (hoverWasp == null) return;
        Species species = hoverWasp.getSpecies();
        if (species == null || !species.canWeaveStenogastrinePaperJelly()) return;

        if (plantFibersAvailable && hoverWasp.isAlive()) {
            hoverWasp.setEnergy(Math.max(0.0f, hoverWasp.getEnergy() - 0.02f));
        }
    }
}
