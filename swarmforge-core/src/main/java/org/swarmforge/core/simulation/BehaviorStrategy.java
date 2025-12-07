/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.Colony;

/**
 * Behavior strategy interface for individual decision-making.
 * Implementations can use different AI approaches (state machines,
 * neural networks, fuzzy logic, etc.)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@FunctionalInterface
public interface BehaviorStrategy {

    /**
     * Decide and execute behavior for an individual.
     *
     * @param individual The individual to control
     * @param terrarium  The world
     * @param colony     The individual's colony
     * @param context    Additional context for decision-making
     */
    void execute(Individual individual, Terrarium terrarium, Colony colony, BehaviorContext context);

    /**
     * Context passed to behaviors with sensory information.
     */
    record BehaviorContext(
            float nearestFoodDistance,
            float nearestFoodDirection,
            float homePheromoneStrength,
            float foodPheromoneStrength,
            float alarmPheromoneStrength,
            boolean enemyNearby,
            boolean atNest) {
        public static BehaviorContext empty() {
            return new BehaviorContext(Float.MAX_VALUE, 0, 0, 0, 0, false, false);
        }
    }
}
