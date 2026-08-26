package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles mutual cuticular spraying and bath grooming with formic acid for disinfection after rival battles.
 */
public class FormicAcidBathGroomingSystem {

    public void processFormicAcidBath(Individual ant, boolean postBattle) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canPerformFormicAcidBathGrooming()) return;

        if (postBattle && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.02f));
        }
    }
}
