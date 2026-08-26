package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles scavenging of wax and honey stores from abandoned hives.
 */
public class AbandonedWaxVaultRaidSystem {

    public void processWaxVaultRaid(Individual raider, boolean abandonedHiveDetected) {
        if (raider == null) return;
        Species species = raider.getSpecies();
        if (species == null || !species.canRaidAbandonedWaxVaults()) return;

        if (abandonedHiveDetected && raider.isAlive()) {
            raider.setEnergy(Math.min(100.0f, raider.getEnergy() + 0.04f));
        }
    }
}
