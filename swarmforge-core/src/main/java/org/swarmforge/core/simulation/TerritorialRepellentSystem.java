/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Inter-Species Territorial Repellent Pheromone System.
 * Models boundary avoidance marking deposited by dominant invasive ants (Solenopsis, Linepithema)
 * causing rival species to turn back upon encountering marked terrain.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TerritorialRepellentSystem {

    public static boolean shouldRetreatFromBoundary(Individual ant, float repellentPheromoneConcentration) {
        if (ant == null || ant.getSpecies() == null) return false;

        // If the ant belongs to a species sensitive to repellent marking
        if (repellentPheromoneConcentration > 0.4f && !ant.getSpecies().hasTerritorialRepellentPheromone()) {
            return true; // Turn back to avoid conflict
        }
        return false;
    }
}
