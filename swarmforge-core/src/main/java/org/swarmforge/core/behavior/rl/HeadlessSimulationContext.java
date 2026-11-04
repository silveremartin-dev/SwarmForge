/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.simulation.SimulationContext;

import java.util.Set;

/**
 * Minimal synthetic SimulationContext for headless neural network training and validation.
 */
public class HeadlessSimulationContext implements SimulationContext {
    public float foodGradX = 0.2f;
    public float foodGradY = 0.1f;
    public float alarmIntensity = 0.0f;
    public boolean enemyNearby = false;
    public boolean foodNearby = false;

    @Override public float getFoodPheromone(float x, float y, float z) { return 0.5f; }
    @Override public float getHomePheromone(float x, float y, float z) { return 0.5f; }
    @Override public float getAlarmPheromone(float x, float y, float z) { return alarmIntensity; }
    @Override public float getFoodPheromoneGradientX(float x, float y, float z) { return foodGradX; }
    @Override public float getFoodPheromoneGradientY(float x, float y, float z) { return foodGradY; }

    @Override public boolean hasEnemyNearby(AgentView agent) {
        if (agent instanceof HeadlessAgentView mv) return mv.hasEnemyNearby || enemyNearby;
        return enemyNearby;
    }

    @Override public Individual getNearestEnemy(AgentView agent) { return null; }

    @Override public boolean hasFoodNearby(AgentView agent) {
        if (agent instanceof HeadlessAgentView mv) return mv.hasFoodNearby || foodNearby;
        return foodNearby || Math.hypot(agent.getX() - 10.0, agent.getY() - 10.0) < 3.0;
    }

    @Override public float[] getNearestFoodPosition(AgentView agent) { return new float[]{10f, 10f, 0f}; }
    @Override public boolean hasFoodNearby(AgentView agent, Set<ResourceType> types) { return hasFoodNearby(agent); }
    @Override public float[] getNearestFoodPosition(AgentView agent, Set<ResourceType> types) { return getNearestFoodPosition(agent); }
    @Override public FoodSource getNearestFood(AgentView agent, Set<ResourceType> types) { return null; }
    @Override public long getCurrentTick() { return 100L; }
    @Override public float getTemperature() { return 24.0f; }
    @Override public boolean isRaining() { return false; }
    @Override public float getLightLevel() { return 1.0f; }
    @Override public float getWaterLevel(float x, float y, float z) { return 0.0f; }
    @Override public float getRelativeHumidity(float x, float y, float z) { return 50.0f; }
    @Override public float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ) { return new float[]{1f, 0f, 0f}; }
}
