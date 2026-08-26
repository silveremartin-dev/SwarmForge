package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles major-to-minor size-graded relay transport of heavy seeds.
 */
public class RelaySeedTransportSystem {

    public void processSeedRelay(Individual majorWorker, Individual minorWorker) {
        if (majorWorker == null || minorWorker == null) return;
        Species species = majorWorker.getSpecies();
        if (species == null || !species.canPerformRelaySeedTransport()) return;

        if (majorWorker.isAlive() && minorWorker.isAlive()) {
            minorWorker.setEnergy(Math.min(100.0f, minorWorker.getEnergy() + 0.05f));
        }
    }
}
