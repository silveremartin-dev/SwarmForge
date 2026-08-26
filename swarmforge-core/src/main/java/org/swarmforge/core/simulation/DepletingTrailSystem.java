/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Dynamic Exhausting Trail Pheromone Decay System.
 * Models foragers modulating trail pheromone deposition rates as food sources near exhaustion to rapidly redirect worker traffic.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DepletingTrailSystem {

    public static float calculateDepletingTrailIntensity(Individual forager, float resourceRemainingRatio) {
        if (forager == null || forager.getSpecies() == null) return 0.0f;
        if (!forager.getSpecies().hasDepletingTrailPheromone()) return 1.0f;

        // Intensity scales linearly with remaining resource ratio
        return Math.max(0.0f, resourceRemainingRatio);
    }
}
