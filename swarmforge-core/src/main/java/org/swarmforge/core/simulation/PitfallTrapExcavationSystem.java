package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles pitfall trap excavation in loose substrate around the nest perimeter.
 */
public class PitfallTrapExcavationSystem {

    public void processPitfallExcavation(Individual excavator, boolean looseSandDetected) {
        if (excavator == null) return;
        Species species = excavator.getSpecies();
        if (species == null || !species.canExcavatePitfallTraps()) return;

        if (looseSandDetected && excavator.isAlive()) {
            excavator.setEnergy(Math.max(0.0f, excavator.getEnergy() - 0.03f));
        }
    }
}
