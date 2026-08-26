package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles subsocial parent bug maternal body shield guarding over egg clutch and young nymphs.
 */
public class MaternalShieldGuardingSystem {

    public void processMaternalShield(Individual motherBug, boolean parasiticWaspThreat) {
        if (motherBug == null) return;
        Species species = motherBug.getSpecies();
        if (species == null || !species.canPerformMaternalShieldGuarding()) return;

        if (parasiticWaspThreat && motherBug.isAlive()) {
            motherBug.setEnergy(Math.max(0.0f, motherBug.getEnergy() - 0.01f));
        }
    }
}
