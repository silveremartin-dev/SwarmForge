package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles visual recognition of individual facial markings to enforce dominance hierarchies.
 */
public class WaspFacialRecognitionSystem {

    public void processFacialRecognition(Individual wasp, Individual encounterTarget) {
        if (wasp == null || encounterTarget == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canRecognizeWaspFacialPatterns()) return;

        if (wasp.isAlive() && encounterTarget.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.005f));
        }
    }
}
