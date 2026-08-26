package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles colony-wide emergency evacuation trigger evacuating brood and queen during catastrophic nest collapse.
 */
public class UniversalEmergencyEvacuationSystem {

    public void processEmergencyEvacuation(Individual individual, boolean catastrophicNestCollapse) {
        if (individual == null) return;
        Species species = individual.getSpecies();
        if (species == null || !species.canTriggerUniversalEmergencyEvacuation()) return;

        if (catastrophicNestCollapse && individual.isAlive()) {
            individual.setEnergy(Math.max(0.0f, individual.getEnergy() - 0.05f));
        }
    }
}
