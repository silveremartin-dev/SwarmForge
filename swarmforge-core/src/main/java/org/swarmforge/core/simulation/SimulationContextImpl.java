/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.FoodSource;
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
    public boolean hasEnemyNearby(Individual individual) {
        // Query spatial index for enemies within radius 5
        var neighbors = simulation.getSpatialIndex().queryRadius(individual.getX(), individual.getY(),
                individual.getZ(), 5.0f);
        for (Individual neighbor : neighbors) {
            if (!neighbor.getColonyId().equals(individual.getColonyId()) && neighbor.isAlive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Individual getNearestEnemy(Individual individual) {
        var neighbors = simulation.getSpatialIndex().queryRadius(individual.getX(), individual.getY(),
                individual.getZ(), 10.0f);
        Individual nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (Individual neighbor : neighbors) {
            if (!neighbor.getColonyId().equals(individual.getColonyId()) && neighbor.isAlive()) {
                float dx = neighbor.getX() - individual.getX();
                float dy = neighbor.getY() - individual.getY();
                float distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = neighbor;
                }
            }
        }
        return nearest;
    }

    @Override
    public boolean hasFoodNearby(Individual individual) {
        var food = simulation.getFoodIndex().queryRadius(individual.getX(), individual.getY(), individual.getZ(), 5.0f);
        return !food.isEmpty();
    }

    @Override
    public float[] getNearestFoodPosition(Individual individual) {
        var foods = simulation.getFoodIndex().queryRadius(individual.getX(), individual.getY(), individual.getZ(),
                10.0f);
        FoodSource nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (FoodSource f : foods) {
            float dx = f.getX() - individual.getX();
            float dy = f.getY() - individual.getY();
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
    public boolean hasFoodNearby(Individual individual, java.util.Set<org.swarmforge.core.domain.ResourceType> types) {
        var foods = simulation.getFoodIndex().queryRadius(individual.getX(), individual.getY(), individual.getZ(),
                5.0f);
        return foods.stream().anyMatch(f -> types.contains(f.getType()));
    }

    @Override
    public float[] getNearestFoodPosition(Individual individual,
            java.util.Set<org.swarmforge.core.domain.ResourceType> types) {
        FoodSource nearest = getNearestFood(individual, types);
        if (nearest != null) {
            return new float[] { nearest.getX(), nearest.getY(), nearest.getZ() };
        }
        return null;
    }

    @Override
    public FoodSource getNearestFood(Individual individual,
            java.util.Set<org.swarmforge.core.domain.ResourceType> types) {
        var foods = simulation.getFoodIndex().queryRadius(individual.getX(), individual.getY(), individual.getZ(),
                10.0f);
        FoodSource nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (FoodSource f : foods) {
            if (types.contains(f.getType())) {
                float dx = f.getX() - individual.getX();
                float dy = f.getY() - individual.getY();
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
        // Simple day/cycle approximation based on tick count assuming 14400 ticks per
        // day
        long timeOfDay = simulation.getTickCount() % 14400;
        if (timeOfDay > 3600 && timeOfDay < 10800)
            return 1.0f; // Day
        return 0.1f; // Night
    }

    @Override
    public float getWaterLevel(float x, float y, float z) {
        return simulation.getWaterGrid().getWaterAt(x, y, z);
    }
}
