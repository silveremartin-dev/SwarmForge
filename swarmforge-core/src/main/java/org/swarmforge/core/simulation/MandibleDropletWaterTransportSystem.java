package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles transporting surface tension water droplets trapped between mandibles for brood hydration.
 */
public class MandibleDropletWaterTransportSystem {

    public void processWaterDropletTransport(Individual ant, boolean waterSourceFound) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canTransportWaterInMandibleDroplet()) return;

        if (waterSourceFound && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.015f));
        }
    }
}
