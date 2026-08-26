package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles interlocked worker body chains pulling tree leaves together for weaver nest assembly.
 */
public class WeaverLeafPullingChainSystem {

    public void processLeafPullingChain(Individual weaver, boolean leafGapToBridge) {
        if (weaver == null) return;
        Species species = weaver.getSpecies();
        if (species == null || !species.canFormLeafPullingChains()) return;

        if (leafGapToBridge && weaver.isAlive()) {
            weaver.setEnergy(Math.max(0.0f, weaver.getEnergy() - 0.03f));
        }
    }
}
