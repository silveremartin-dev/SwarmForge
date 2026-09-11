/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

/**
 * Bio-Acoustic Substrate Wave Propagation & Attenuation Model.
 * Simulates elastic wave transmission (P-waves/Rayleigh waves) through porous soil substrates
 * from ant stridulation distress calls and head-drumming alarm signals.
 *
 * Wave velocity: v = sqrt(E / rho)
 * Attenuation: alpha(rho, porosity, moisture, frequency)
 * Received SPL: SPL(r) = SPL_0 - 20*log10(max(1, r)) - alpha * r
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class SubstrateAcoustics {

    /** Subgenual organ detection threshold in insect tibia (dB) */
    public static final float SUBGENUAL_DETECTION_THRESHOLD_DB = 28.0f;

    private SubstrateAcoustics() {}

    /**
     * Calculates the phase velocity of compression waves in the given substrate (m/s).
     */
    public static float computeWaveVelocity(VoxelMaterial material) {
        if (material == null || material == VoxelMaterial.AIR) {
            return 343.0f; // Speed of sound in air (m/s at 20°C)
        }
        float ePa = material.getYoungsModulusMPa() * 1_000_000.0f;
        float rho = Math.max(1.0f, material.getDensityKgM3());
        return (float) Math.sqrt(ePa / rho);
    }

    /**
     * Calculates the spatial attenuation coefficient alpha (dB/cell) for a given material and frequency.
     */
    public static float computeAttenuationCoefficient(VoxelMaterial material, float moisturePercent, float frequencyHz) {
        if (material == null || material == VoxelMaterial.AIR) {
            return 1.2f * (frequencyHz / 1000.0f); // High porous/air acoustic attenuation
        }

        float normFreq = Math.max(0.1f, frequencyHz / 1000.0f);
        float porosityFactor = (float) Math.pow(Math.max(0.01f, material.getPorosity()), 1.2);
        float densityFactor = (float) Math.sqrt(Math.max(0.1f, material.getDensityKgM3() / 1000.0f));
        float moistureFactor = 1.0f + 0.4f * Math.min(100.0f, Math.max(0.0f, moisturePercent)) / 100.0f;

        // Porous granular dissipative soil attenuation
        return (1.25f * normFreq * porosityFactor / densityFactor) * moistureFactor;
    }

    /**
     * Computes the received Sound Pressure Level (SPL in dB) at a receiver position.
     */
    public static float computeReceivedSplDb(float sourceDb, float frequencyHz,
                                             float startX, float startY, float startZ,
                                             float targetX, float targetY, float targetZ,
                                             Terrarium terrarium) {
        float dx = targetX - startX;
        float dy = targetY - startY;
        float dz = targetZ - startZ;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.1f) {
            return sourceDb;
        }

        // Geometric spherical spreading loss
        float geometricLoss = 20.0f * (float) Math.log10(Math.max(1.0f, distance));

        // Substrate material sampling along path (sample midpoint)
        VoxelMaterial pathMaterial = VoxelMaterial.SOIL;
        float moisture = 45.0f;

        if (terrarium != null) {
            int midX = Math.round((startX + targetX) * 0.5f);
            int midY = Math.round((startY + targetY) * 0.5f);
            int midZ = Math.round((startZ + targetZ) * 0.5f);
            TerrariumCell cell = terrarium.getCell(midX, midY, midZ);
            if (cell != null) {
                pathMaterial = VoxelMaterial.fromDomainMaterial(cell.material());
                moisture = cell.humidity();
            }
        }

        float alpha = computeAttenuationCoefficient(pathMaterial, moisture, frequencyHz);
        float absorptionLoss = alpha * distance;

        return Math.max(0.0f, sourceDb - geometricLoss - absorptionLoss);
    }

    /**
     * Checks whether an acoustic distress signal is audible to an insect's subgenual organs.
     */
    public static boolean isSignalAudible(float sourceDb, float frequencyHz,
                                          float startX, float startY, float startZ,
                                          float targetX, float targetY, float targetZ,
                                          Terrarium terrarium) {
        float receivedSpl = computeReceivedSplDb(sourceDb, frequencyHz, startX, startY, startZ, targetX, targetY, targetZ, terrarium);
        return receivedSpl >= SUBGENUAL_DETECTION_THRESHOLD_DB;
    }

    /**
     * Calculates the unit vector direction pointing from the receiver towards the acoustic source.
     */
    public static float[] computeAcousticGradientVector(float receiverX, float receiverY, float receiverZ,
                                                        float sourceX, float sourceY, float sourceZ) {
        float dx = sourceX - receiverX;
        float dy = sourceY - receiverY;
        float dz = sourceZ - receiverZ;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 0.001f) {
            return new float[] { 0.0f, 0.0f, 0.0f };
        }
        return new float[] { dx / dist, dy / dist, dz / dist };
    }
}
