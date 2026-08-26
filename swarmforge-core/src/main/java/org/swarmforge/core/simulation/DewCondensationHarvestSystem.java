package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles early morning tarsal dew condensation harvesting on vegetation.
 */
public class DewCondensationHarvestSystem {

    public void processDewHarvest(Individual agent, double ambientHumidity, double temperatureC) {
        if (agent == null) return;
        Species species = agent.getSpecies();
        if (species == null || !species.canHarvestDewCondensation()) return;

        if (ambientHumidity > 85.0 && temperatureC < 18.0 && agent.getThirst() > 0.2f) {
            agent.setThirst(Math.max(0.0f, agent.getThirst() - 0.15f));
        }
    }
}
