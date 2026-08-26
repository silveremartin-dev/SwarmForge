package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles tiny minim workers riding on harvested leaf discs to clean parasitic phorid fly eggs off leaf-cutter foragers.
 */
public class MinimLeafParasiteGroomingSystem {

    public void processMinimGrooming(Individual minimAnt, boolean leafDiscTransported) {
        if (minimAnt == null) return;
        Species species = minimAnt.getSpecies();
        if (species == null || !species.canGroomLeafPulpParasitesMinim()) return;

        if (leafDiscTransported && minimAnt.isAlive()) {
            minimAnt.setEnergy(Math.max(0.0f, minimAnt.getEnergy() - 0.01f));
        }
    }
}
