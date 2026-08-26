package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles arching gaster forward to spray formic acid jets up to 30 cm against vertebrate predators.
 */
public class FormicAcidArtilleryJetSystem {

    public void processAcidArtilleryJet(Individual ant, boolean vertebratePredatorThreat) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canFireFormicAcidArtilleryJet()) return;

        if (vertebratePredatorThreat && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.03f));
        }
    }
}
