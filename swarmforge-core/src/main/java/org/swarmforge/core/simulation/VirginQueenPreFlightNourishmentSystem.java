package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles targeted hyper-nourishment of virgin queens prior to nuptial flight.
 */
public class VirginQueenPreFlightNourishmentSystem {

    public void processQueenNourishment(Individual nurse, Individual virginQueen) {
        if (nurse == null || virginQueen == null) return;
        Species species = nurse.getSpecies();
        if (species == null || !species.canNourishVirginQueensPreFlight()) return;

        if (nurse.isAlive() && virginQueen.isAlive()) {
            virginQueen.setEnergy(Math.min(100.0f, virginQueen.getEnergy() + 5.0f));
        }
    }
}
