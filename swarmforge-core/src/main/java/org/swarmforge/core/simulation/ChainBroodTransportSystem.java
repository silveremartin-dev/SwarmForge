package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles interlocked mandible chain brood transport for emergency nest evacuations.
 */
public class ChainBroodTransportSystem {

    public void processChainTransport(Individual leader, Individual follower) {
        if (leader == null || follower == null) return;
        Species species = leader.getSpecies();
        if (species == null || !species.canTransportChainBrood()) return;

        if (leader.isAlive() && follower.isAlive()) {
            follower.setEnergy(Math.min(100.0f, follower.getEnergy() + 0.05f));
        }
    }
}
