package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles collecting tree propolis resins to caulk micro-cracks and sterilize hive walls.
 */
public class HoneybeePropolisNestSealSystem {

    public void processPropolisNestSealing(Individual bee, boolean crackDetected) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canSealNestGapsWithPropolis()) return;

        if (crackDetected && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.02f));
        }
    }
}
