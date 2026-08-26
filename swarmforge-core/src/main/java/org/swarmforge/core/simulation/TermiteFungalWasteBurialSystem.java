package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles sealing senescent fungal comb waste in dead-end gallery vaults.
 */
public class TermiteFungalWasteBurialSystem {

    public void processFungalWasteBurial(Individual termite, boolean senescentCombPresent) {
        if (termite == null) return;
        Species species = termite.getSpecies();
        if (species == null || !species.canBuryFungalWasteInGallery()) return;

        if (senescentCombPresent && termite.isAlive()) {
            termite.setEnergy(Math.max(0.0f, termite.getEnergy() - 0.02f));
        }
    }
}
