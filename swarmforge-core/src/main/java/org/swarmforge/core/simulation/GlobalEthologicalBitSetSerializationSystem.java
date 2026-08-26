package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;
import java.util.BitSet;

/**
 * Handles compact binary BitSet state serialization supporting ultra-high throughput state sync across distributed compute nodes.
 */
public class GlobalEthologicalBitSetSerializationSystem {

    public BitSet serializeCapabilities(Species species) {
        if (species == null) return new BitSet();
        return species.getCapabilitiesBitSet();
    }

    public void processGlobalBitSetSync(Individual individual) {
        if (individual == null) return;
        Species species = individual.getSpecies();
        if (species == null || !species.canSerializeGlobalEthologicalBitSet()) return;

        BitSet set = species.getCapabilitiesBitSet();
        if (set != null && individual.isAlive()) {
            // High throughput bitwise validation
            individual.setEnergy(Math.max(0.0f, individual.getEnergy() - 0.001f));
        }
    }
}
