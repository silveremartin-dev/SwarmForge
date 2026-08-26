package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles tilting body armor to block parasitoid wasp ovipositors from reaching egg clutches.
 */
public class ShieldBugParasitoidShieldSystem {

    public void processParasitoidShielding(Individual motherBug, boolean waspParasitoidPresent) {
        if (motherBug == null) return;
        Species species = motherBug.getSpecies();
        if (species == null || !species.canShieldEggsFromParasitoidWasps()) return;

        if (waspParasitoidPresent && motherBug.isAlive()) {
            motherBug.setEnergy(Math.max(0.0f, motherBug.getEnergy() - 0.02f));
        }
    }
}
