package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles thermo-balling around hornet intruders raising temperature to 47°C to cook predators alive.
 */
public class HotBallThermalDefenseSystem {

    public void processHotBallThermalDefense(Individual bee, boolean hornetAttacking) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canFormHotBallThermalDefense()) return;

        if (hornetAttacking && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.04f));
        }
    }
}
