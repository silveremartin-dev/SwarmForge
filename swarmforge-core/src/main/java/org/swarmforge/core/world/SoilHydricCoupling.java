/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

/**
 * Hydric Coupling & Subterranean Soil Dynamics Model.
 * Simulates subterranean thermal wave attenuation (lagged temperature based on soil inertia)
 * and soil moisture absorption, evaporation, and vertical gallery diffusion.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SoilHydricCoupling {

    private final float[] soilMoistureByDepth; // 0 (surface) to 32 (deep)
    private final int maxDepth;
    private float surfaceMoisture = 50.0f; // %

    public SoilHydricCoupling(int maxDepth) {
        this.maxDepth = Math.max(1, maxDepth);
        this.soilMoistureByDepth = new float[this.maxDepth];
        for (int z = 0; z < this.maxDepth; z++) {
            soilMoistureByDepth[z] = 45.0f; // Default baseline moisture
        }
    }

    /**
     * Update soil moisture levels based on rainfall, temperature, wind, and vertical diffusion.
     *
     * @param rainfallMm Rainfall rate in mm/hour
     * @param surfaceTemp Current surface air temperature (°C)
     * @param windSpeed Current wind speed (km/h)
     * @param deltaHours Time step in hours
     */
    public void updateMoisture(float rainfallMm, float surfaceTemp, float windSpeed, float deltaHours) {
        // 1. Surface Infiltration & Runoff
        if (rainfallMm > 0) {
            float absorptionRate = 0.8f; // 80% absorbed, 20% runoff
            surfaceMoisture = Math.min(100.0f, surfaceMoisture + rainfallMm * absorptionRate * deltaHours * 2.0f);
        } else {
            // Evaporation based on temperature and wind speed
            float evapRate = (float) (0.05f * (1.0 + Math.max(0, surfaceTemp) / 10.0) * (1.0 + windSpeed / 20.0));
            surfaceMoisture = Math.max(5.0f, surfaceMoisture - evapRate * deltaHours);
        }

        soilMoistureByDepth[0] = surfaceMoisture;

        // 2. Downward Capillary Diffusion through Soil Depths
        float diffusionCoeff = 0.15f;
        for (int z = 1; z < maxDepth; z++) {
            float diff = (soilMoistureByDepth[z - 1] - soilMoistureByDepth[z]) * diffusionCoeff * deltaHours;
            soilMoistureByDepth[z] += diff;
            soilMoistureByDepth[z] = Math.max(10.0f, Math.min(95.0f, soilMoistureByDepth[z]));
        }
    }

    /**
     * Calculate underground temperature at depth z (cells) considering soil thermal inertia phase lag and dampening.
     *
     * @param depth Depth in cells (0 = surface)
     * @param surfaceTemp Current surface temperature
     * @param annualAvgTemp Annual average temperature
     * @param soilInertiaDays Soil thermal inertia phase lag in days
     * @param depthAttenuation Attenuation factor per cell (0.0 to 1.0)
     * @return Temperature at specified depth
     */
    public float calculateTemperatureAtDepth(int depth, float surfaceTemp, float annualAvgTemp,
                                            double soilInertiaDays, double depthAttenuation) {
        if (depth <= 0) return surfaceTemp;

        // Exponential thermal amplitude dampening with depth
        double attenuationFactor = Math.pow(depthAttenuation, depth);

        // Underground thermal lag
        float tempDelta = surfaceTemp - annualAvgTemp;
        float dampenedDelta = (float) (tempDelta * attenuationFactor);

        return annualAvgTemp + dampenedDelta;
    }

    /**
     * Get soil humidity percentage (0-100%) at a specific depth cell.
     */
    public float getMoistureAtDepth(int depth) {
        int z = Math.max(0, Math.min(maxDepth - 1, depth));
        return soilMoistureByDepth[z];
    }
}
