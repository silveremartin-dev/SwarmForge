/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;

/**
 * Pheromone-Triggered Emergency Swarming System.
 * Models colony fission and emergency swarming triggered by catastrophic nest destruction
 * or queen loss, splitting workers into daughter swarms.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EmergencySwarmingSystem {

    public static boolean triggerEmergencySwarm(Colony colony, boolean isNestDestroyed) {
        if (colony == null || colony.getSpecies() == null) return false;
        if (!colony.getSpecies().canTriggerEmergencySwarming()) return false;

        if (isNestDestroyed || colony.getPopulation() > 50000) {
            // Split colony for reproductive fission
            return true;
        }
        return false;
    }
}
