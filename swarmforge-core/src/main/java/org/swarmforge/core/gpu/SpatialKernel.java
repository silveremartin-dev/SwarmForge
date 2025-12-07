/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.gpu;

/**
 * GPU compute kernel for spatial queries and collision detection.
 * Implements spatial hashing for efficient neighbor lookups.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpatialKernel {

    /**
     * Find all individuals within radius of a target position.
     * CPU fallback implementation.
     *
     * @param positions Flat array of positions [x0,y0,z0, x1,y1,z1, ...]
     * @param targetX   Target X position
     * @param targetY   Target Y position
     * @param targetZ   Target Z position
     * @param radius    Search radius
     * @param results   Output array of distances (MAX_VALUE if outside radius)
     * @param count     Number of individuals
     */
    public static void findNeighborsCPU(
            float[] positions,
            float targetX, float targetY, float targetZ,
            float radius,
            float[] results,
            int count) {

        float radiusSq = radius * radius;

        for (int i = 0; i < count; i++) {
            int idx = i * 3;
            float dx = positions[idx] - targetX;
            float dy = positions[idx + 1] - targetY;
            float dz = positions[idx + 2] - targetZ;

            float distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= radiusSq) {
                results[i] = (float) Math.sqrt(distSq);
            } else {
                results[i] = Float.MAX_VALUE;
            }
        }
    }

    /**
     * Update positions for all individuals based on heading and speed.
     * GPU-friendly parallel position update.
     *
     * @param positions Flat array of positions [x,y,z, x,y,z, ...]
     * @param headings  Array of headings (radians)
     * @param speeds    Array of movement speeds
     * @param count     Number of individuals
     */
    public static void updatePositionsCPU(
            float[] positions,
            float[] headings,
            float[] speeds,
            int count) {

        for (int i = 0; i < count; i++) {
            int idx = i * 3;
            float heading = headings[i];
            float speed = speeds[i];

            positions[idx] += Math.cos(heading) * speed; // x
            positions[idx + 1] += Math.sin(heading) * speed; // y
            // z stays same for ground-based movement
        }
    }

    /**
     * Calculate pheromone gradient direction for each individual.
     * Returns angles pointing toward strongest pheromone source.
     *
     * @param positions     Individual positions [x,y,z, ...]
     * @param pheromones    Flat pheromone grid
     * @param gradients     Output gradient directions (radians)
     * @param width         Grid width
     * @param height        Grid height
     * @param pheromoneType Which pheromone type to follow
     * @param count         Number of individuals
     */
    public static void calculateGradientsCPU(
            float[] positions,
            float[] pheromones,
            float[] gradients,
            int width, int height, int depth,
            int pheromoneType,
            int count) {

        int planeSize = width * height;
        int volumeSize = planeSize * depth;

        for (int i = 0; i < count; i++) {
            int posIdx = i * 3;
            int x = (int) positions[posIdx];
            int y = (int) positions[posIdx + 1];
            int z = (int) positions[posIdx + 2];

            // Clamp to bounds
            x = Math.max(1, Math.min(width - 2, x));
            y = Math.max(1, Math.min(height - 2, y));
            z = Math.max(1, Math.min(depth - 2, z));

            int baseIdx = x + y * width + z * planeSize + pheromoneType * volumeSize;

            // Sample in X and Y directions
            float px = pheromones[baseIdx + 1] - pheromones[baseIdx - 1];
            float py = pheromones[baseIdx + width] - pheromones[baseIdx - width];

            // Calculate gradient direction
            if (px != 0 || py != 0) {
                gradients[i] = (float) Math.atan2(py, px);
            } else {
                gradients[i] = Float.NaN; // No gradient
            }
        }
    }
}
