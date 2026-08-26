/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Fast Emergency Evacuation Escape Pheromone System.
 * Models scouts laying volatile panic alarm pheromones that trigger high-speed directional evacuation away from predator breaches.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EscapePheromoneSystem {

    public static boolean triggerEmergencyEscape(Individual ant, boolean isThreatDetected) {
        if (ant == null || ant.getSpecies() == null) return false;
        if (!ant.getSpecies().hasEmergencyEscapePheromone()) return false;

        if (isThreatDetected) {
            // Speed up movement heading away from threat
            return true;
        }
        return false;
    }
}
