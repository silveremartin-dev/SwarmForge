package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles rescue and mandible transport of injured nestmates to hospital chambers.
 */
public class InjuredPheromonalStretcherSystem {

    public void processRescueTransport(Individual rescuer, Individual victim) {
        if (rescuer == null || victim == null) return;
        Species species = rescuer.getSpecies();
        if (species == null || !species.canTransportInjuredPheromonalStretcher()) return;

        if (victim.getHealth() < 40.0f && rescuer.isAlive() && victim.isAlive()) {
            rescuer.setEnergy(Math.max(0.0f, rescuer.getEnergy() - 0.02f));
        }
    }
}
