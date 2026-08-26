package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles rhythmic antennal stroking of aphid abdomens to solicit honeydew excretion.
 */
public class AphidHoneydewMilkingSystem {

    public void processAphidMilking(Individual ant, boolean aphidPresent) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canMilkAphidHoneydewStroking()) return;

        if (aphidPresent && ant.isAlive()) {
            ant.setEnergy(Math.min(100.0f, ant.getEnergy() + 0.03f));
        }
    }
}
