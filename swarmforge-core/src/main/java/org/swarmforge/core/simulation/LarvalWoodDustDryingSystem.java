package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles fine wood dust dusting of damp larvae to prevent mildew growth.
 */
public class LarvalWoodDustDryingSystem {

    public void processLarvalDrying(Individual nurse, Individual larva) {
        if (nurse == null || larva == null) return;
        Species species = nurse.getSpecies();
        if (species == null || !species.canDryLarvaeWoodDust()) return;

        if (nurse.isAlive() && larva.isAlive()) {
            larva.setHealth(Math.min(100.0f, larva.getHealth() + 2.0f));
        }
    }
}
