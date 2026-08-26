package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles regurgitating crop contents to feed first-instar nymphs inside maternal chambers.
 */
public class EarwigMaternalRegurgitationSystem {

    public void processEarwigMaternalFeeding(Individual motherEarwig, boolean nymphPresent) {
        if (motherEarwig == null) return;
        Species species = motherEarwig.getSpecies();
        if (species == null || !species.canRegurgitateEarwigMaternalFood()) return;

        if (nymphPresent && motherEarwig.isAlive()) {
            motherEarwig.setEnergy(Math.max(0.0f, motherEarwig.getEnergy() - 0.02f));
        }
    }
}
