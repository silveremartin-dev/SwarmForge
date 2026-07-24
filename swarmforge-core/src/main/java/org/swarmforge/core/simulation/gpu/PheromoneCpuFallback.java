/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.gpu;

import java.util.stream.IntStream;

/**
 * Multi-threaded CPU fallback for 3D Pheromone grid diffusion and evaporation.
 * Guarantees high-performance tick calculation when GPU / TornadoVM / OpenCL hardware is unavailable.
 */
public class PheromoneCpuFallback {

    /**
     * Process 3D pheromone grid diffusion and evaporation using parallel CPU streams.
     *
     * @param width           Grid width (X)
     * @param height          Grid height (Y)
     * @param depth           Grid depth (Z)
     * @param pheromoneTypes  Number of interleaved pheromone channels per cell
     * @param pheromones      Input matrix
     * @param newPheromones   Output matrix (result)
     * @param diffusionRate   Diffusion coefficient
     * @param evaporationRate Evaporation decay per tick
     */
    public static void process(int width, int height, int depth, int pheromoneTypes,
                               float[] pheromones, float[] newPheromones,
                               float diffusionRate, float evaporationRate) {
        int totalCells = width * height * depth;

        IntStream.range(0, totalCells).parallel().forEach(i -> {
            int tmp = i;
            int x = tmp % width;
            tmp /= width;
            int y = tmp % height;
            int z = tmp / height;

            for (int t = 0; t < pheromoneTypes; t++) {
                int index = (i * pheromoneTypes) + t;
                float currentVal = pheromones[index];

                float sum = 0;
                int count = 0;

                if (x > 0) {
                    sum += pheromones[((i - 1) * pheromoneTypes) + t];
                    count++;
                }
                if (x < width - 1) {
                    sum += pheromones[((i + 1) * pheromoneTypes) + t];
                    count++;
                }
                if (y > 0) {
                    sum += pheromones[((i - width) * pheromoneTypes) + t];
                    count++;
                }
                if (y < height - 1) {
                    sum += pheromones[((i + width) * pheromoneTypes) + t];
                    count++;
                }
                if (z > 0) {
                    sum += pheromones[((i - width * height) * pheromoneTypes) + t];
                    count++;
                }
                if (z < depth - 1) {
                    sum += pheromones[((i + width * height) * pheromoneTypes) + t];
                    count++;
                }

                float avg = (count > 0) ? sum / count : 0;
                float diffused = (1.0f - diffusionRate) * currentVal + diffusionRate * avg;
                newPheromones[index] = Math.max(0.0f, diffused - evaporationRate);
            }
        });
    }
}
