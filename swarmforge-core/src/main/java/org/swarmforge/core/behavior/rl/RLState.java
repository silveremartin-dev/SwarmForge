/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

/**
 * Represents a discrete state for Q-Learning.
 * This state condenses continuous sensory data into a manageable set of
 * scenarios.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public record RLState(
        boolean hasFood,
        PheromoneDirection foodPheromoneDirection,
        PheromoneDirection homePheromoneDirection,
        boolean isAtNest,
        boolean isLoaded) {
    public enum PheromoneDirection {
        NONE,
        FORWARD,
        LEFT,
        RIGHT
    }
}
