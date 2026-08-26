package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles arching heart-shaped gaster overhead ("acrobat ants") to deposit defensive venom droplets on intruders.
 */
public class AcrobatAntGasterVenomSystem {

    public void processAcrobatGasterVenom(Individual acrobatAnt, boolean intruderThreat) {
        if (acrobatAnt == null) return;
        Species species = acrobatAnt.getSpecies();
        if (species == null || !species.canCockGasterFormicAcidRepellent()) return;

        if (intruderThreat && acrobatAnt.isAlive()) {
            acrobatAnt.setEnergy(Math.max(0.0f, acrobatAnt.getEnergy() - 0.025f));
        }
    }
}
