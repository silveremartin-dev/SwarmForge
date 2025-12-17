/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * Level-of-Detail Manager for optimizing 3D rendering performance.
 * Selects appropriate mesh complexity based on distance from camera.
 *
 * <p>
 * LOD Levels:
 * </p>
 * <ul>
 * <li>HIGH - Full detailed mesh with all body parts and legs</li>
 * <li>MEDIUM - Simplified mesh with basic body shapes</li>
 * <li>LOW - Billboard sprite or single box</li>
 * <li>CULLED - Not rendered (too far)</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class LODManager {

    /**
     * LOD levels from highest to lowest detail.
     */
    public enum LODLevel {
        HIGH(0),
        MEDIUM(1),
        LOW(2),
        CULLED(3);

        private final int index;

        LODLevel(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    // Distance thresholds (configurable)
    private float highToMediumDistance = 20f;
    private float mediumToLowDistance = 50f;
    private float lowToCulledDistance = 100f;

    // Camera reference for distance calculations
    private Camera camera;

    // Statistics
    private int[] lodCounts = new int[4];
    private int totalEntities = 0;

    public LODManager() {
    }

    public LODManager(Camera camera) {
        this.camera = camera;
    }

    /**
     * Calculate the appropriate LOD level for an entity at the given position.
     */
    public LODLevel calculateLOD(float x, float y, float z) {
        if (camera == null) {
            return LODLevel.HIGH; // Default to high if no camera
        }

        Vector3f entityPos = new Vector3f(x, y, z);
        float distance = camera.getLocation().distance(entityPos);

        return calculateLODByDistance(distance);
    }

    /**
     * Calculate LOD based on distance value.
     */
    public LODLevel calculateLODByDistance(float distance) {
        if (distance < highToMediumDistance) {
            return LODLevel.HIGH;
        } else if (distance < mediumToLowDistance) {
            return LODLevel.MEDIUM;
        } else if (distance < lowToCulledDistance) {
            return LODLevel.LOW;
        } else {
            return LODLevel.CULLED;
        }
    }

    /**
     * Calculate LOD for a position vector.
     */
    public LODLevel calculateLOD(Vector3f position) {
        return calculateLOD(position.x, position.y, position.z);
    }

    /**
     * Update LOD statistics for reporting.
     */
    public void updateStats(LODLevel level) {
        lodCounts[level.getIndex()]++;
        totalEntities++;
    }

    /**
     * Reset statistics for a new frame.
     */
    public void resetStats() {
        lodCounts = new int[4];
        totalEntities = 0;
    }

    /**
     * Get percentage of entities at each LOD level.
     */
    public float[] getLODDistribution() {
        if (totalEntities == 0) {
            return new float[] { 0, 0, 0, 0 };
        }
        return new float[] {
                (float) lodCounts[0] / totalEntities,
                (float) lodCounts[1] / totalEntities,
                (float) lodCounts[2] / totalEntities,
                (float) lodCounts[3] / totalEntities
        };
    }

    /**
     * Get memory savings estimate based on LOD distribution.
     * Assumes HIGH=1.0, MEDIUM=0.5, LOW=0.1, CULLED=0 relative memory.
     */
    public float getMemorySavingsPercent() {
        if (totalEntities == 0)
            return 0;

        float fullMemory = totalEntities; // All at HIGH
        float actualMemory = lodCounts[0] * 1.0f
                + lodCounts[1] * 0.5f
                + lodCounts[2] * 0.1f
                + lodCounts[3] * 0.0f;

        return (1.0f - actualMemory / fullMemory) * 100f;
    }

    // === Configuration ===

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setHighToMediumDistance(float distance) {
        this.highToMediumDistance = distance;
    }

    public void setMediumToLowDistance(float distance) {
        this.mediumToLowDistance = distance;
    }

    public void setLowToCulledDistance(float distance) {
        this.lowToCulledDistance = distance;
    }

    public float getHighToMediumDistance() {
        return highToMediumDistance;
    }

    public float getMediumToLowDistance() {
        return mediumToLowDistance;
    }

    public float getLowToCulledDistance() {
        return lowToCulledDistance;
    }

    /**
     * Configure all thresholds at once.
     */
    public void setThresholds(float highMed, float medLow, float lowCull) {
        this.highToMediumDistance = highMed;
        this.mediumToLowDistance = medLow;
        this.lowToCulledDistance = lowCull;
    }

    // === Statistics Getters ===

    public int getHighCount() {
        return lodCounts[0];
    }

    public int getMediumCount() {
        return lodCounts[1];
    }

    public int getLowCount() {
        return lodCounts[2];
    }

    public int getCulledCount() {
        return lodCounts[3];
    }

    public int getTotalEntities() {
        return totalEntities;
    }

    /**
     * Get a formatted string of LOD statistics.
     */
    public String getStatsString() {
        return String.format("LOD: H=%d M=%d L=%d C=%d (%.1f%% savings)",
                lodCounts[0], lodCounts[1], lodCounts[2], lodCounts[3],
                getMemorySavingsPercent());
    }
}
