package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles abdominal secretion of plant growth stimulants to repair gall wall cracks in eusocial thrips.
 */
public class ThripsGallRepairSecretionSystem {

    public void processGallRepairSecretion(Individual thrips, boolean gallCrackPresent) {
        if (thrips == null) return;
        Species species = thrips.getSpecies();
        if (species == null || !species.canRepairGallSubstratalSecretion()) return;

        if (gallCrackPresent && thrips.isAlive()) {
            thrips.setEnergy(Math.max(0.0f, thrips.getEnergy() - 0.02f));
        }
    }
}
