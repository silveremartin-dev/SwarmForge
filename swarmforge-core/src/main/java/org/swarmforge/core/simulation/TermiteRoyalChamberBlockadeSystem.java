package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles interlocked soldier head capsules blocking access to termite royal physogastric chamber.
 */
public class TermiteRoyalChamberBlockadeSystem {

    public void processRoyalChamberBlockade(Individual soldier, boolean breachThreat) {
        if (soldier == null) return;
        Species species = soldier.getSpecies();
        if (species == null || !species.canBlockRoyalChamberSentry()) return;

        if (breachThreat && soldier.isAlive()) {
            soldier.setEnergy(Math.max(0.0f, soldier.getEnergy() - 0.02f));
        }
    }
}
