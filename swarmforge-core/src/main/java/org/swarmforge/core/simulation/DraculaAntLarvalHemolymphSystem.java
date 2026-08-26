package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles non-fatally puncturing larval integument ("dracula ants") to drink hemolymph droplets during food scarcity.
 */
public class DraculaAntLarvalHemolymphSystem {

    public void processDraculaHemolymphFeeding(Individual adultAnt, Individual larva) {
        if (adultAnt == null || larva == null) return;
        Species species = adultAnt.getSpecies();
        if (species == null || !species.canFeedOnLarvalHemolymphDracula()) return;

        if (adultAnt.isAlive() && larva.isAlive() && adultAnt.getEnergy() < 30.0f) {
            adultAnt.setEnergy(Math.min(100.0f, adultAnt.getEnergy() + 0.05f));
        }
    }
}
