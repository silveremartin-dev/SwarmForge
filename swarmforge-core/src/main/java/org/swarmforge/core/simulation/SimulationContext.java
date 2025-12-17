/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Context interface providing sensory information to behavior architectures.
 * Abstracts the simulation state for decision-making.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface SimulationContext {

    /**
     * Get food pheromone intensity at position.
     */
    float getFoodPheromone(float x, float y, float z);

    /**
     * Get home pheromone intensity at position.
     */
    float getHomePheromone(float x, float y, float z);

    /**
     * Get alarm pheromone intensity at position.
     */
    float getAlarmPheromone(float x, float y, float z);

    /**
     * Get food pheromone gradient X component.
     */
    float getFoodPheromoneGradientX(float x, float y, float z);

    /**
     * Get food pheromone gradient Y component.
     */
    float getFoodPheromoneGradientY(float x, float y, float z);

    /**
     * Check if there's an enemy nearby.
     */
    boolean hasEnemyNearby(Individual individual);

    /**
     * Get the nearest enemy individual.
     */
    Individual getNearestEnemy(Individual individual);

    /**
     * Check if there's food nearby.
     */
    boolean hasFoodNearby(Individual individual);

    /**
     * Get the nearest food source position.
     */
    float[] getNearestFoodPosition(Individual individual);

    /**
     * Check if there's food of specific types nearby.
     */
    boolean hasFoodNearby(Individual individual, java.util.Set<org.swarmforge.core.domain.ResourceType> types);

    /**
     * Get the nearest food source position of specific types.
     */
    float[] getNearestFoodPosition(Individual individual, java.util.Set<org.swarmforge.core.domain.ResourceType> types);

    /**
     * Get the nearest FoodSource object (internal use).
     */
    org.swarmforge.core.domain.FoodSource getNearestFood(Individual individual,
            java.util.Set<org.swarmforge.core.domain.ResourceType> types);

    /**
     * Get current simulation tick.
     */
    long getCurrentTick();

    /**
     * Get current temperature.
     */
    float getTemperature();

    /**
     * Check if it's currently raining.
     */
    boolean isRaining();

    /**
     * Get light level (0=dark, 1=bright).
     */
    float getLightLevel();

    /**
     * Get water level at position (0.0=dry, 1.0=flooded).
     */
    float getWaterLevel(float x, float y, float z);
}
