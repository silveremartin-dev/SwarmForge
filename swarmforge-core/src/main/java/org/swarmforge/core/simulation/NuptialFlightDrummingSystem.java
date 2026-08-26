package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles collective substrate drumming triggering simultaneous alate takeoff for nuptial flights.
 */
public class NuptialFlightDrummingSystem {

    public void processNuptialFlightDrumming(Individual alate, boolean optimalFlightWeather) {
        if (alate == null) return;
        Species species = alate.getSpecies();
        if (species == null || !species.canDrumNuptialFlightSynchronization()) return;

        if (optimalFlightWeather && alate.isAlive()) {
            alate.setEnergy(Math.max(0.0f, alate.getEnergy() - 0.025f));
        }
    }
}
