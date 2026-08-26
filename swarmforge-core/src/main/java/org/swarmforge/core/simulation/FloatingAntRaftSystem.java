package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles interlocking bodies with hydrophobic cuticles to form floating rafts carrying queen and brood during floods.
 */
public class FloatingAntRaftSystem {

    public void processFloatingAntRaft(Individual ant, boolean floodRising) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canFormFloatingAntRaft()) return;

        if (floodRising && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.02f));
        }
    }
}
