/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.behavior.AgentView;
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
    boolean hasEnemyNearby(AgentView agent);

    /**
     * Get the nearest enemy individual.
     */
    Individual getNearestEnemy(AgentView agent);

    /**
     * Check if there's food nearby.
     */
    boolean hasFoodNearby(AgentView agent);

    /**
     * Get the nearest food source position.
     */
    float[] getNearestFoodPosition(AgentView agent);

    /**
     * Check if there's food of specific types nearby.
     */
    boolean hasFoodNearby(AgentView agent, java.util.Set<org.swarmforge.core.domain.ResourceType> types);

    /**
     * Get the nearest food source position of specific types.
     */
    float[] getNearestFoodPosition(AgentView agent, java.util.Set<org.swarmforge.core.domain.ResourceType> types);

    /**
     * Get the nearest FoodSource object (internal use).
     */
    org.swarmforge.core.domain.FoodSource getNearestFood(AgentView agent,
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

    /**
     * Get relative humidity (% RH, 0.0 to 100.0).
     */
    float getRelativeHumidity(float x, float y, float z);

    /**
     * Get CO2 concentration in ppm (ambient baseline ~400 ppm).
     */
    float getCo2Ppm(float x, float y, float z);

    /**
     * Get geomagnetic inclination angle (degrees) for magnetoreceptive species orientation.
     */
    float getGeomagneticHeading(float x, float y, float z);

    /**
     * Get local thermal gradient X component for thermoreception navigation.
     */
    float getThermalGradientX(float x, float y, float z);

    /**
     * Get local thermal gradient Y component for thermoreception navigation.
     */
    float getThermalGradientY(float x, float y, float z);

    /**
     * Get deterministic navigation flow vector components [dx, dy, dz] towards a target cell.
     */
    float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ);
}
