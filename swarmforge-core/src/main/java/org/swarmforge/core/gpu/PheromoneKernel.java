/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.gpu;

/**
 * GPU compute kernel for pheromone diffusion and evaporation.
 * Uses a simple API that can be executed on GPU via TornadoVM or on CPU as
 * fallback.
 * 
 * Pheromone diffusion follows the heat equation:
 * P(t+1) = P(t) + D * (sum of neighbors - 6*P(t)) - E * P(t)
 * where D = diffusion rate, E = evaporation rate
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PheromoneKernel {

    // Diffusion rate (how fast pheromones spread)
    private static final float DIFFUSION_RATE = 0.1f;

    // Evaporation rate (how fast pheromones fade)
    private static final float EVAPORATION_RATE = 0.01f;

    /**
     * CPU fallback implementation of pheromone diffusion.
     * Processes a 3D grid of pheromone values.
     *
     * @param input  Input pheromone grid [x][y][z][type]
     * @param output Output pheromone grid (must be same size)
     * @param width  Grid width
     * @param height Grid height
     * @param depth  Grid depth
     * @param types  Number of pheromone types
     */
    public static void diffuseCPU(
            float[][][][] input,
            float[][][][] output,
            int width, int height, int depth, int types) {

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                for (int z = 1; z < depth - 1; z++) {
                    for (int t = 0; t < types; t++) {
                        // Get current value
                        float current = input[x][y][z][t];

                        // Sum of 6 neighbors (3D)
                        float neighbors = input[x - 1][y][z][t] + input[x + 1][y][z][t] +
                                input[x][y - 1][z][t] + input[x][y + 1][z][t] +
                                input[x][y][z - 1][t] + input[x][y][z + 1][t];

                        // Diffusion + evaporation
                        float diffused = current + DIFFUSION_RATE * (neighbors - 6 * current);
                        float evaporated = diffused * (1.0f - EVAPORATION_RATE);

                        // Clamp to [0, 1]
                        output[x][y][z][t] = Math.max(0, Math.min(1, evaporated));
                    }
                }
            }
        }
    }

    /**
     * Flat array version for GPU compatibility.
     * Arrays are linearized as: index = x + y*width + z*width*height +
     * t*width*height*depth
     *
     * @param input  Flat input array
     * @param output Flat output array
     * @param width  Grid width
     * @param height Grid height
     * @param depth  Grid depth
     * @param types  Number of pheromone types
     */
    public static void diffuseFlat(
            float[] input,
            float[] output,
            int width, int height, int depth, int types) {

        int planeSize = width * height;
        int volumeSize = planeSize * depth;

        for (int idx = 0; idx < volumeSize * types; idx++) {
            int t = idx / volumeSize;
            int remainder = idx % volumeSize;
            int z = remainder / planeSize;
            int rem2 = remainder % planeSize;
            int y = rem2 / width;
            int x = rem2 % width;

            // Skip boundaries
            if (x == 0 || x == width - 1 ||
                    y == 0 || y == height - 1 ||
                    z == 0 || z == depth - 1) {
                output[idx] = 0;
                continue;
            }

            float current = input[idx];

            // Neighbor offsets in flat array
            float neighbors = input[idx - 1] + // x-1
                    input[idx + 1] + // x+1
                    input[idx - width] + // y-1
                    input[idx + width] + // y+1
                    input[idx - planeSize] + // z-1
                    input[idx + planeSize]; // z+1

            float diffused = current + DIFFUSION_RATE * (neighbors - 6 * current);
            float evaporated = diffused * (1.0f - EVAPORATION_RATE);

            output[idx] = Math.max(0, Math.min(1, evaporated));
        }
    }
}
