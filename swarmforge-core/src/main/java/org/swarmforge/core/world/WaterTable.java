/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

/**
 * Underground water table simulation.
 * Water level rises during rain and can flood tunnels.
 *
 * @author Gemini AI Assistant
 */
public class WaterTable {

    private final int depth;
    private float baseLevel; // Default water level (as percentage of depth)
    private float currentLevel; // Current water level
    private float saturation; // Soil saturation 0-1
    private float drainageRate = 0.001f;
    private float absorptionRate = 0.01f;

    public WaterTable(int width, int depth) {
        this.depth = depth;
        this.baseLevel = depth * 0.3f; // Default 30% depth
        this.currentLevel = baseLevel;
        this.saturation = 0.3f;
    }

    /**
     * Process one simulation tick.
     */
    public void tick(float rainfall, float evaporation) {
        // Add rainfall to saturation
        saturation += rainfall * absorptionRate;
        saturation -= evaporation * 0.001f;
        saturation = Math.max(0, Math.min(1, saturation));

        // Water level rises with saturation
        float targetLevel = baseLevel + (saturation * depth * 0.5f);

        // Gradual change
        if (currentLevel < targetLevel) {
            currentLevel += (targetLevel - currentLevel) * 0.01f;
        } else {
            currentLevel -= (currentLevel - targetLevel) * drainageRate;
        }

        currentLevel = Math.max(0, Math.min(depth * 0.9f, currentLevel));
    }

    /**
     * Check if a position is flooded.
     */
    public boolean isFlooded(int x, int y, int z) {
        // z is depth, lower z = deeper
        return z < currentLevel;
    }

    /**
     * Get water depth at a position.
     */
    public float getWaterDepth(int z) {
        if (z < currentLevel) {
            return currentLevel - z;
        }
        return 0;
    }

    /**
     * Get current water level as position.
     */
    public float getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Get saturation level (0-1).
     */
    public float getSaturation() {
        return saturation;
    }

    /**
     * Set base water level.
     */
    public void setBaseLevel(float level) {
        this.baseLevel = level;
        this.currentLevel = level;
    }

    /**
     * Trigger flood event (e.g., from heavy rain).
     */
    public void triggerFlood(float intensity) {
        saturation = Math.min(1.0f, saturation + intensity);
    }

    /**
     * Drain water (e.g., during drought).
     */
    public void drain(float amount) {
        currentLevel = Math.max(0, currentLevel - amount);
        saturation = Math.max(0, saturation - amount * 0.1f);
    }
}
