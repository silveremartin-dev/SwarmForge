/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.domain.Individual;

/**
 * Concrete implementation of SimulationContext.
 * Provides safe, read-only access to simulation state for ant brains.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationContextImpl implements SimulationContext {

    private final Simulation simulation;

    public SimulationContextImpl(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public float getFoodPheromone(float x, float y, float z) {
        return simulation.getPheromoneGrid().read((int) x, (int) y, (int) z,
                org.swarmforge.core.domain.PheromoneType.FOOD_TRAIL.getIndex());
    }

    @Override
    public float getHomePheromone(float x, float y, float z) {
        return simulation.getPheromoneGrid().read((int) x, (int) y, (int) z,
                org.swarmforge.core.domain.PheromoneType.HOME_TRAIL.getIndex());
    }

    @Override
    public float getAlarmPheromone(float x, float y, float z) {
        return simulation.getPheromoneGrid().read((int) x, (int) y, (int) z,
                org.swarmforge.core.domain.PheromoneType.ALARM.getIndex());
    }

    @Override
    public float getFoodPheromoneGradientX(float x, float y, float z) {
        // Simple gradient approximation by sampling neighbors
        float left = getFoodPheromone(x - 1, y, z);
        float right = getFoodPheromone(x + 1, y, z);
        return right - left;
    }

    @Override
    public float getFoodPheromoneGradientY(float x, float y, float z) {
        float down = getFoodPheromone(x, y - 1, z);
        float up = getFoodPheromone(x, y + 1, z);
        return up - down;
    }

    @Override
    public boolean hasEnemyNearby(AgentView agent) {
        // Query spatial index for enemies within radius 5
        var neighbors = simulation.getSpatialIndex().queryRadius(agent.getX(), agent.getY(),
                agent.getZ(), 5.0f);
        org.swarmforge.core.domain.Colony agentColony = simulation.getColony(agent.getColonyId());
        for (Individual neighbor : neighbors) {
            if (isEnemy(agentColony, agent, neighbor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Individual getNearestEnemy(AgentView agent) {
        var neighbors = simulation.getSpatialIndex().queryRadius(agent.getX(), agent.getY(),
                agent.getZ(), 10.0f);
        Individual nearest = null;
        float minDistSq = Float.MAX_VALUE;
        org.swarmforge.core.domain.Colony agentColony = simulation.getColony(agent.getColonyId());

        for (Individual neighbor : neighbors) {
            if (isEnemy(agentColony, agent, neighbor)) {
                float dx = neighbor.getX() - agent.getX();
                float dy = neighbor.getY() - agent.getY();
                float distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = neighbor;
                }
            }
        }
        return nearest;
    }

    private boolean isEnemy(org.swarmforge.core.domain.Colony agentColony, AgentView agent, Individual neighbor) {
        if (neighbor == null || !neighbor.isAlive()) return false;
        if (neighbor.getColonyId().equals(agent.getColonyId())) return false;
        if (agentColony != null) {
            return agentColony.getDiplomacyManager().isEnemy(neighbor.getColonyId());
        }
        return false;
    }

    @Override
    public boolean hasFoodNearby(AgentView agent) {
        var food = simulation.getFoodIndex().queryRadius(agent.getX(), agent.getY(), agent.getZ(), 5.0f);
        return !food.isEmpty();
    }

    @Override
    public float[] getNearestFoodPosition(AgentView agent) {
        var foods = simulation.getFoodIndex().queryRadius(agent.getX(), agent.getY(), agent.getZ(),
                10.0f);
        FoodSource nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (FoodSource f : foods) {
            float dx = f.getX() - agent.getX();
            float dy = f.getY() - agent.getY();
            float distSq = dx * dx + dy * dy;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = f;
            }
        }

        if (nearest != null) {
            return new float[] { nearest.getX(), nearest.getY(), nearest.getZ() };
        }
        return null;
    }

    @Override
    public boolean hasFoodNearby(AgentView agent, java.util.Set<org.swarmforge.core.domain.ResourceType> types) {
        var foods = simulation.getFoodIndex().queryRadius(agent.getX(), agent.getY(), agent.getZ(),
                5.0f);
        return foods.stream().anyMatch(f -> types.contains(f.getType()));
    }

    @Override
    public float[] getNearestFoodPosition(AgentView agent,
            java.util.Set<org.swarmforge.core.domain.ResourceType> types) {
        FoodSource nearest = getNearestFood(agent, types);
        if (nearest != null) {
            return new float[] { nearest.getX(), nearest.getY(), nearest.getZ() };
        }
        return null;
    }

    @Override
    public FoodSource getNearestFood(AgentView agent,
            java.util.Set<org.swarmforge.core.domain.ResourceType> types) {
        var foods = simulation.getFoodIndex().queryRadius(agent.getX(), agent.getY(), agent.getZ(),
                10.0f);
        FoodSource nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (FoodSource f : foods) {
            if (types.contains(f.getType())) {
                float dx = f.getX() - agent.getX();
                float dy = f.getY() - agent.getY();
                float distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = f;
                }
            }
        }
        return nearest;
    }

    @Override
    public long getCurrentTick() {
        return simulation.getTickCount();
    }

    @Override
    public float getTemperature() {
        return simulation.getWeather().getTemperature();
    }

    @Override
    public boolean isRaining() {
        return simulation.getWeather().getRainfall() > 0;
    }

    @Override
    public float getLightLevel() {
        // Day/night cycle calculated in real elapsed seconds (240s full cycle)
        double totalSeconds = simulation.getTickCount() * (double) simulation.getSimulationStepSeconds();
        double timeOfDaySec = totalSeconds % 240.0;
        if (timeOfDaySec > 60.0 && timeOfDaySec < 180.0)
            return 1.0f; // Day
        return 0.1f; // Night
    }

    @Override
    public float getWaterLevel(float x, float y, float z) {
        return simulation.getWaterGrid().getWaterAt(x, y, z);
    }

    @Override
    public float getRelativeHumidity(float x, float y, float z) {
        // High subterranean humidity (~85%), lower ambient surface humidity (~55%)
        if (z < 0) return 85.0f;
        return isRaining() ? 95.0f : 55.0f;
    }

    @Override
    public float getCo2Ppm(float x, float y, float z) {
        // Underground respiration accumulation (baseline 400 ppm, elevated underground)
        if (z < 0) {
            return 400.0f + Math.abs(z) * 150.0f;
        }
        return 400.0f;
    }

    @Override
    public float getGeomagneticHeading(float x, float y, float z) {
        // Earth magnetic field inclination gradient (approx 45 degrees north)
        return 45.0f;
    }

    @Override
    public float getThermalGradientX(float x, float y, float z) {
        float left = getTemperature();
        float right = getTemperature();
        return right - left;
    }

    @Override
    public float getThermalGradientY(float x, float y, float z) {
        // Surface is exposed to solar warming, subterranean is insulated
        return (z < 0) ? -0.1f : 0.1f;
    }

    @Override
    public float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ) {
        return simulation.getFlowVector(x, y, z, targetX, targetY, targetZ);
    }
}
