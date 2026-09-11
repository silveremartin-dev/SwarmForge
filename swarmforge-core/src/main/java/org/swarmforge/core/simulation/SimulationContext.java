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
     * Get home pheromone gradient X component.
     */
    default float getHomePheromoneGradientX(float x, float y, float z) {
        float hLeft = getHomePheromone(x - 1, y, z);
        float hRight = getHomePheromone(x + 1, y, z);
        return hRight - hLeft;
    }

    /**
     * Get home pheromone gradient Y component.
     */
    default float getHomePheromoneGradientY(float x, float y, float z) {
        float hDown = getHomePheromone(x, y - 1, z);
        float hUp = getHomePheromone(x, y + 1, z);
        return hUp - hDown;
    }

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
     * Check if weather conditions (rain, storm, high winds, freezing cold) force flying foragers to shelter.
     */
    default boolean isAdverseWeatherForFlight() {
        return isRaining() || getTemperature() < 10.0f;
    }

    /**
     * Species-specific biometeorological adverse flight evaluation.
     * Takes into account species thermal envelope, cold flight traits (e.g. Bombus at 4°C), and flight speed vs wind.
     */
    default boolean isAdverseWeatherForFlight(org.swarmforge.core.species.Species species) {
        if (isRaining()) return true;
        float temp = getTemperature();
        if (species != null) {
            float minFlightTemp = species.canForageSubZeroBumblebee() ? 4.0f :
                    ("WASP".equalsIgnoreCase(species.getInsectType()) ? 12.0f :
                    ("BEE".equalsIgnoreCase(species.getInsectType()) ? 10.0f : Math.max(8.0f, species.getMinTempCelsius() + 4.0f)));
            if (temp < minFlightTemp) return true;
            if (temp > species.getMaxTempCelsius()) return true;
        } else {
            if (temp < 10.0f) return true;
        }
        return false;
    }

    default boolean isAdverseWeatherForFlight(org.swarmforge.core.behavior.AgentView agent) {
        if (agent != null && agent.getSpecies() != null) {
            return isAdverseWeatherForFlight(agent.getSpecies());
        }
        return isAdverseWeatherForFlight();
    }

    /**
     * Calculates air dynamic viscosity in Pa.s as a function of air temperature in Celsius (Sutherland approximation).
     */
    default float getAirDynamicViscosity(float tempCelsius) {
        double tKelvin = 273.15 + tempCelsius;
        return (float) (1.716e-5 * Math.pow(tKelvin / 273.15, 0.76));
    }

    /**
     * Calculates flight Reynolds number Re = (rho * v * L) / mu.
     */
    default float getReynoldsNumber(float speedMps, float lengthMm, float tempCelsius) {
        float mu = getAirDynamicViscosity(tempCelsius);
        float rho = (float) (1.293 * (273.15 / (273.15 + tempCelsius))); // Air density kg/m^3
        float lengthM = lengthMm / 1000.0f;
        return (rho * Math.max(0.01f, speedMps) * lengthM) / (mu + 1e-9f);
    }

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
    default float getCo2Ppm(float x, float y, float z) { return 400.0f; }

    /**
     * Get geomagnetic inclination angle (degrees) for magnetoreceptive species orientation.
     */
    default float getGeomagneticHeading(float x, float y, float z) { return 0.0f; }

    /**
     * Get local thermal gradient X component for thermoreception navigation.
     */
    default float getThermalGradientX(float x, float y, float z) { return 0.0f; }

    /**
     * Get local thermal gradient Y component for thermoreception navigation.
     */
    default float getThermalGradientY(float x, float y, float z) { return 0.0f; }

    /**
     * Get deterministic navigation flow vector components [dx, dy, dz] towards a target cell.
     */
    float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ);
}
