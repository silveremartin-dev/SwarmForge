package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles oral trophallactic transfer of worker ovary suppression factors.
 */
public class TrophallacticOvaryInhibitionSystem {

    public void processOvarySuppression(Individual donor, Individual recipient) {
        if (donor == null || recipient == null) return;
        Species species = donor.getSpecies();
        if (species == null || !species.hasTrophallacticOvaryInhibition()) return;

        if (donor.isAlive() && recipient.isAlive()) {
            recipient.setHunger(Math.max(0.0f, recipient.getHunger() - 0.1f));
        }
    }
}
