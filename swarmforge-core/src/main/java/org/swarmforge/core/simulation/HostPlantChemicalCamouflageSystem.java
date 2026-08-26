package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles cuticular absorption of host tree bark odor for chemical camouflage.
 */
public class HostPlantChemicalCamouflageSystem {

    public void processChemicalCamouflage(Individual ant, boolean hostBarkContact) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canAbsorbHostPlantChemicalCamouflage()) return;

        if (hostBarkContact && ant.isAlive()) {
            ant.setHealth(Math.min(100.0f, ant.getHealth() + 0.01f));
        }
    }
}
