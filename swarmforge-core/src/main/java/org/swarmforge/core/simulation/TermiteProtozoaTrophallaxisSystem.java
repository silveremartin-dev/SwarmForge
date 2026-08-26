package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles anal trophallactic transfer of flagellate protozoa essential for termite wood cellulose digestion.
 */
public class TermiteProtozoaTrophallaxisSystem {

    public void processProtozoaTrophallaxis(Individual donor, Individual recipient) {
        if (donor == null || recipient == null) return;
        Species species = donor.getSpecies();
        if (species == null || !species.canTrophallaxisProtozoa()) return;

        if (donor.isAlive() && recipient.isAlive()) {
            donor.setEnergy(Math.max(0.0f, donor.getEnergy() - 0.01f));
        }
    }
}
