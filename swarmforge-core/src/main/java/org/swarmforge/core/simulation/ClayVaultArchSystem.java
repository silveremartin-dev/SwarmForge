package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles subterranean gothic arch construction built from clay-saliva pellets in termites.
 */
public class ClayVaultArchSystem {

    public void processClayArchConstruction(Individual mason, boolean highCeilingRoom) {
        if (mason == null) return;
        Species species = mason.getSpecies();
        if (species == null || !species.canConstructClayVaultArches()) return;

        if (highCeilingRoom && mason.isAlive()) {
            mason.setEnergy(Math.max(0.0f, mason.getEnergy() - 0.03f));
        }
    }
}
