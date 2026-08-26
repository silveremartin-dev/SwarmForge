package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles synchronized drumming dance announcing major prey hatching events.
 */
public class HatchingEnthusiasmVibratoDanceSystem {

    public void processHatchingDance(Individual scout, boolean majorHatchingEvent) {
        if (scout == null) return;
        Species species = scout.getSpecies();
        if (species == null || !species.canDanceVibratoHatchingEnthusiasm()) return;

        if (majorHatchingEvent && scout.isAlive()) {
            scout.setEnergy(Math.max(0.0f, scout.getEnergy() - 0.01f));
        }
    }
}
