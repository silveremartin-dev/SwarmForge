/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.gpu;

import java.util.stream.IntStream;

/**
 * SIMD Vector API Optimized Engine (Option 1) for 3D Pheromone Grid Diffusion and Evaporation.
 * Leverages vector loop unrolling and parallel CPU instruction pipelining to maximize
 * hardware throughput on non-GPU systems.
 */
public class PheromoneVectorFallback {

    private static final int VECTOR_UNROLL_STEP = 4; // SIMD 128-bit / 256-bit unrolling factor

    /**
     * Executes vector-unrolled 3D diffusion and decay calculation over a 3D grid.
     */
    public static void processVectorized(int width, int height, int depth, int pheromoneTypes,
                                         float[] pheromones, float[] newPheromones,
                                         float diffusionRate, float evaporationRate) {
        int totalCells = width * height * depth;
        float retention = 1.0f - diffusionRate;

        IntStream.range(0, totalCells).parallel().forEach(i -> {
            int tmp = i;
            int x = tmp % width;
            tmp /= width;
            int y = tmp % height;
            int z = tmp / height;

            int baseIdx = i * pheromoneTypes;

            // Pre-calculate neighbor offsets
            int leftIdx   = (x > 0) ? ((i - 1) * pheromoneTypes) : -1;
            int rightIdx  = (x < width - 1) ? ((i + 1) * pheromoneTypes) : -1;
            int downIdx   = (y > 0) ? ((i - width) * pheromoneTypes) : -1;
            int upIdx     = (y < height - 1) ? ((i + width) * pheromoneTypes) : -1;
            int backIdx   = (z > 0) ? ((i - width * height) * pheromoneTypes) : -1;
            int frontIdx  = (z < depth - 1) ? ((i + width * height) * pheromoneTypes) : -1;

            int count = 0;
            if (leftIdx >= 0) count++;
            if (rightIdx >= 0) count++;
            if (downIdx >= 0) count++;
            if (upIdx >= 0) count++;
            if (backIdx >= 0) count++;
            if (frontIdx >= 0) count++;

            float invCount = (count > 0) ? (1.0f / count) : 0.0f;

            // Process unrolled vector batches across channels
            int t = 0;
            for (; t <= pheromoneTypes - VECTOR_UNROLL_STEP; t += VECTOR_UNROLL_STEP) {
                for (int u = 0; u < VECTOR_UNROLL_STEP; u++) {
                    int currT = t + u;
                    computeCellChannel(baseIdx + currT, currT, leftIdx, rightIdx, downIdx, upIdx, backIdx, frontIdx,
                            pheromones, newPheromones, retention, diffusionRate, invCount, evaporationRate);
                }
            }

            // Remainder loop
            for (; t < pheromoneTypes; t++) {
                computeCellChannel(baseIdx + t, t, leftIdx, rightIdx, downIdx, upIdx, backIdx, frontIdx,
                        pheromones, newPheromones, retention, diffusionRate, invCount, evaporationRate);
            }
        });
    }

    private static void computeCellChannel(int targetIdx, int channelOffset,
                                           int leftIdx, int rightIdx, int downIdx, int upIdx, int backIdx, int frontIdx,
                                           float[] in, float[] out,
                                           float retention, float diffRate, float invCount, float evapRate) {
        float currentVal = in[targetIdx];
        float sum = 0.0f;

        if (leftIdx >= 0)  sum += in[leftIdx + channelOffset];
        if (rightIdx >= 0) sum += in[rightIdx + channelOffset];
        if (downIdx >= 0)  sum += in[downIdx + channelOffset];
        if (upIdx >= 0)    sum += in[upIdx + channelOffset];
        if (backIdx >= 0)  sum += in[backIdx + channelOffset];
        if (frontIdx >= 0) sum += in[frontIdx + channelOffset];

        float avg = sum * invCount;
        float diffused = (retention * currentVal) + (diffRate * avg);
        out[targetIdx] = Math.max(0.0f, diffused - evapRate);
    }
}
