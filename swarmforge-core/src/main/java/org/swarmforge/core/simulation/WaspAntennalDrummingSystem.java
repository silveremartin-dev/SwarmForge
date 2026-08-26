package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles antennal drumming on larval heads soliciting saliva release in paper wasps.
 */
public class WaspAntennalDrummingSystem {

    public void processAntennalDrumming(Individual wasp, boolean larvaCellVisited) {
        if (wasp == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canDrumAntennaeLarvalStimulation()) return;

        if (larvaCellVisited && wasp.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.01f));
        }
    }
}
