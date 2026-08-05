/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import java.io.Serializable;

/**
 * Physics-Based Passive Ventilation & Thermoregulation Engine.
 * Calculates airflow velocity via Stack / Chimney Effect:
 * v_draft = C_nest * sqrt(g * h_chimney * |T_nest - T_ambient| / T_ambient)
 * Regulates internal CO2 levels and nest internal temperature across the 13 Nest Typologies.
 */
public class PassiveVentilationEngine implements Serializable {
    private static final long serialVersionUID = 1L;

    public record NestAtmosphereState(
            float meanNestTemperatureC,
            float meanNestCo2Ppm,
            float totalAirflowDraftMPerSec,
            float co2PurgeRatePpmPerTick
    ) implements Serializable {}

    /**
     * Compute chimney stack effect airflow velocity (m/s).
     */
    public float calculateStackEffectAirflow(NestVoxelGrid grid, float externalTempC, float externalWindSpeed) {
        NestType nestType = grid.getNestType();
        float draftMult = nestType.getChimneyDraftMultiplier();
        float heightChimney = grid.getHeight() * 0.1f; // height in meters (10cm per voxel)

        // Find average nest temperature
        float nestTempSum = 0.0f;
        int count = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int z = 0; z < grid.getDepth(); z++) {
                    NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, z);
                    if (cell != null && cell.getMaterial() == VoxelMaterial.AIR) {
                        nestTempSum += cell.getTemperatureC();
                        count++;
                    }
                }
            }
        }
        float meanNestTemp = (count > 0) ? (nestTempSum / count) : externalTempC;

        float deltaT = Math.abs(meanNestTemp - externalTempC);
        float absExtK = Math.max(250.0f, externalTempC + 273.15f);

        // Thermal buoyancy stack velocity formula
        float buoyancyVelocity = (float) Math.sqrt(Math.max(0.0f, (9.81f * heightChimney * deltaT) / absExtK));

        // Total passive draft combines thermal buoyancy and wind suction draft
        float windDraft = externalWindSpeed * 0.15f;
        return draftMult * (buoyancyVelocity + windDraft);
    }

    /**
     * Ticks ventilation cycle across the voxel grid: purges CO2, adjusts internal temperature towards ambient equilibrium.
     */
    public NestAtmosphereState simulateVentilationStep(NestVoxelGrid grid, float externalTempC,
                                                      float externalCo2Ppm, float externalWindSpeed,
                                                      int antMetabolismCount) {

        float draftVelocity = calculateStackEffectAirflow(grid, externalTempC, externalWindSpeed);
        NestType nestType = grid.getNestType();

        float co2PurgeFraction = Math.min(0.40f, 0.02f * draftVelocity * nestType.getBaselineCo2PurgeRate());
        float thermalEquilRate = Math.min(0.30f, 0.01f * draftVelocity / Math.max(0.1f, nestType.getThermalInsulation()));

        // Ant metabolic CO2 emission: ~0.5 ppm per ant per tick inside air voxels
        float metabolicCo2Addition = antMetabolismCount * 0.5f;

        float totalTemp = 0.0f;
        float totalCo2 = 0.0f;
        int airVoxelCount = 0;

        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int z = 0; z < grid.getDepth(); z++) {
                    NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, z);
                    if (cell != null && cell.getMaterial() == VoxelMaterial.AIR) {
                        // CO2 Update: Purge + Ant Respiration
                        float currentCo2 = cell.getCo2Ppm();
                        float purgedCo2 = currentCo2 - co2PurgeFraction * (currentCo2 - externalCo2Ppm);
                        float nextCo2 = Math.max(350.0f, purgedCo2 + metabolicCo2Addition / Math.max(1, airVoxelCount));
                        cell.setCo2Ppm(nextCo2);

                        // Temperature Update towards ambient equilibrium
                        float currentTemp = cell.getTemperatureC();
                        float nextTemp = currentTemp + thermalEquilRate * (externalTempC - currentTemp);
                        cell.setTemperatureC(nextTemp);

                        totalTemp += nextTemp;
                        totalCo2 += nextCo2;
                        airVoxelCount++;
                    }
                }
            }
        }

        float meanTemp = (airVoxelCount > 0) ? (totalTemp / airVoxelCount) : externalTempC;
        float meanCo2 = (airVoxelCount > 0) ? (totalCo2 / airVoxelCount) : externalCo2Ppm;

        return new NestAtmosphereState(meanTemp, meanCo2, draftVelocity, co2PurgeFraction * 100.0f);
    }
}
