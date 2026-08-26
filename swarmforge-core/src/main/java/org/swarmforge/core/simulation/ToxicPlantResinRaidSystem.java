package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Manages targeted foraging of toxic plant resins for burrowing rodent repellents.
 */
public class ToxicPlantResinRaidSystem {

    public void processResinRaid(Individual forager, boolean rodentNearby) {
        if (forager == null) return;
        Species species = forager.getSpecies();
        if (species == null || !species.canRaidToxicPlantResin()) return;

        if (rodentNearby && forager.isAlive()) {
            forager.setEnergy(Math.max(0.0f, forager.getEnergy() - 0.02f));
        }
    }
}
