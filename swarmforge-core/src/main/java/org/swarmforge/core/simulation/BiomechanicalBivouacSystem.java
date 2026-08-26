package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles interlocking legs and claws to build living hanging walls and thermal nest bivouacs.
 */
public class BiomechanicalBivouacSystem {

    public void processBiomechanicalBivouac(Individual armyAnt, boolean bivouacPhase) {
        if (armyAnt == null) return;
        Species species = armyAnt.getSpecies();
        if (species == null || !species.canFormBiomechanicalBivouac()) return;

        if (bivouacPhase && armyAnt.isAlive()) {
            armyAnt.setEnergy(Math.max(0.0f, armyAnt.getEnergy() - 0.015f));
        }
    }
}
