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

    private float snowDepthMm = 0.0f;        // Snowpack depth in mm
    private float snowDensity = 100.0f;       // kg/m^3 (100 = fresh powder, 450 = compacted firn)
    private float iceThicknessMm = 0.0f;      // Surface ice sheet thickness in mm
    private float frostDepthCells = 0.0f;     // Subterranean freezing front depth in cells
    private final int maxDepth;
    private final float[] soilMoistureByDepth;
    private float surfaceMoisture = 45.0f;

    public SoilHydricCoupling(int maxDepth) {
        this.maxDepth = Math.max(1, maxDepth);
        this.soilMoistureByDepth = new float[this.maxDepth];
        for (int z = 0; z < this.maxDepth; z++) {
            soilMoistureByDepth[z] = 45.0f; // Default baseline moisture
        }
    }

    /**
     * Update soil moisture, snow compaction, thaw/melt, and thermal frost front.
     *
     * @param rainfallMm Rainfall rate in mm/hour
     * @param snowfallMm Snowfall rate in mm/hour
     * @param surfaceTemp Current surface air temperature (°C)
     * @param windSpeed Current wind speed (km/h)
     * @param deltaHours Time step in hours
     */
    public void updateHydricAndThermalState(float rainfallMm, float snowfallMm, float surfaceTemp, float windSpeed, float deltaHours) {
        // 1. Snow Accumulation & Compaction under Gravity
        if (surfaceTemp <= 0.0f) {
            if (snowfallMm > 0) {
                snowDepthMm += snowfallMm * deltaHours;
            }

            // Natural snowpack compaction over time (powder -> compacted firn)
            if (snowDepthMm > 0) {
                snowDensity = Math.min(450.0f, snowDensity + 0.5f * deltaHours);
                // Volume reduction proportional to density compaction
                snowDepthMm = Math.max(0.0f, snowDepthMm * (1.0f - 0.002f * deltaHours));
            }

            // Surface water freezing into ice
            if (surfaceTemp < -2.0f && surfaceMoisture > 70.0f) {
                iceThicknessMm = Math.min(200.0f, iceThicknessMm + Math.abs(surfaceTemp) * 0.1f * deltaHours);
            }
        } else {
            // 2. Thermal Thaw / Melt (Snow & Ice Melting when Temp > 0°C)
            float meltRateMmPerDegreeHour = 0.4f; // Degree-day melt factor
            float meltMm = surfaceTemp * meltRateMmPerDegreeHour * deltaHours;

            if (snowDepthMm > 0) {
                float actualSnowMelt = Math.min(snowDepthMm, meltMm);
                snowDepthMm -= actualSnowMelt;
                // Meltwater infiltrates soil surface
                rainfallMm += actualSnowMelt * 0.8f;
            }

            if (snowDepthMm <= 0 && iceThicknessMm > 0) {
                float actualIceMelt = Math.min(iceThicknessMm, meltMm * 0.5f);
                iceThicknessMm -= actualIceMelt;
                rainfallMm += actualIceMelt * 0.6f;
            }
        }

        // 3. Surface Infiltration & Runoff
        if (rainfallMm > 0) {
            float absorptionRate = 0.8f; // 80% absorbed, 20% runoff
            surfaceMoisture = Math.min(100.0f, surfaceMoisture + rainfallMm * absorptionRate * deltaHours * 2.0f);
        } else {
            // Evaporation based on temperature, wind speed, and snow cover insulation
            float snowCoverInsulation = Math.min(0.95f, snowDepthMm / 100.0f);
            float evapRate = (float) (0.05f * (1.0 + Math.max(0, surfaceTemp) / 10.0) * (1.0 + windSpeed / 20.0) * (1.0f - snowCoverInsulation));
            surfaceMoisture = Math.max(5.0f, surfaceMoisture - evapRate * deltaHours);
        }

        soilMoistureByDepth[0] = surfaceMoisture;

        // 4. Downward Capillary Diffusion through Soil Depths
        float diffusionCoeff = 0.15f;
        for (int z = 1; z < maxDepth; z++) {
            float diff = (soilMoistureByDepth[z - 1] - soilMoistureByDepth[z]) * diffusionCoeff * deltaHours;
            soilMoistureByDepth[z] += diff;
            soilMoistureByDepth[z] = Math.max(10.0f, Math.min(95.0f, soilMoistureByDepth[z]));
        }

        // 5. Frost Front & Permafrost Calculation
        if (surfaceTemp < 0.0f && snowDepthMm < 30.0f) {
            frostDepthCells = Math.min(maxDepth * 0.6f, frostDepthCells + Math.abs(surfaceTemp) * 0.02f * deltaHours);
        } else if (surfaceTemp > 5.0f) {
            frostDepthCells = Math.max(0.0f, frostDepthCells - surfaceTemp * 0.03f * deltaHours);
        }
    }

    /**
     * Calculate underground temperature at depth z (cells) considering soil thermal inertia phase lag and snow cover insulation.
     *
     * @param depth Depth in cells (0 = surface)
     * @param surfaceTemp Current surface temperature
     * @param annualAvgTemp Annual average temperature
     * @param soilInertiaDays Soil thermal inertia phase lag in days
     * @param depthAttenuation Attenuation factor per cell (0.0 to 1.0)
     * @return Temperature at specified depth in °C
     */
    public float calculateTemperatureAtDepth(int depth, float surfaceTemp, float annualAvgTemp,
                                            double soilInertiaDays, double depthAttenuation) {
        if (depth <= 0) return surfaceTemp;

        // Snow layer acts as a thermal insulator (blanket protecting sub-surface from extreme cold)
        float snowInsulationFactor = Math.min(0.90f, snowDepthMm / 80.0f);
        float insulatedSurfaceTemp = surfaceTemp;
        if (surfaceTemp < 0.0f && snowInsulationFactor > 0.0f) {
            // Dampen sub-zero surface temperature under snowpack
            insulatedSurfaceTemp = surfaceTemp * (1.0f - snowInsulationFactor) + 0.5f * snowInsulationFactor;
        }

        // Exponential thermal amplitude dampening with depth
        double attenuationFactor = Math.pow(depthAttenuation, depth);

        // Underground thermal lag
        float tempDelta = insulatedSurfaceTemp - annualAvgTemp;
        float dampenedDelta = (float) (tempDelta * attenuationFactor);

        return annualAvgTemp + dampenedDelta;
    }

    public float getSnowDepthMm() { return snowDepthMm; }
    public float getIceThicknessMm() { return iceThicknessMm; }
    public float getFrostDepthCells() { return frostDepthCells; }
    public float getSnowDensity() { return snowDensity; }

    /**
     * Get soil humidity percentage (0-100%) at a specific depth cell.
     */
    public float getMoistureAtDepth(int depth) {
        int z = Math.max(0, Math.min(maxDepth - 1, depth));
        return soilMoistureByDepth[z];
    }
}
