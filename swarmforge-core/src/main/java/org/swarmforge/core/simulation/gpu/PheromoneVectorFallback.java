/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.gpu;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.stream.IntStream;

/**
 * SIMD Vector API Optimized Engine for 3D Pheromone Grid Diffusion and Evaporation.
 * Leverages Java 21 Project Panama Vector API (jdk.incubator.vector) to maximize
 * hardware throughput using 256-bit AVX2 / 512-bit AVX-512 vector registers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PheromoneVectorFallback {

    private static final VectorSpecies<Float> SPECIES_256 = FloatVector.SPECIES_256;
    private static final VectorSpecies<Float> SPECIES_PREFERRED = FloatVector.SPECIES_PREFERRED;
    private static final int VECTOR_UNROLL_STEP = 4; // SIMD 128-bit / 256-bit unrolling factor

    /**
     * Executes hardware vector-accelerated 3D diffusion and decay calculation over a 3D grid.
     * When pheromoneTypes == 8 (SwarmForge standard), uses SPECIES_256 for single-instruction 8-channel processing.
     */
    public static void processVectorized(int width, int height, int depth, int pheromoneTypes,
                                         float[] pheromones, float[] newPheromones,
                                         float diffusionRate, float evaporationRate) {
        if (pheromoneTypes == 8) {
            processVectorApi8Channels(width, height, depth, pheromones, newPheromones, diffusionRate, evaporationRate);
            return;
        }

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

    /**
     * SIMD 256-bit Vector API specialized kernel for 8-channel pheromone grids.
     * Evaluates all 8 chemical channels simultaneously in a single vector register instruction.
     */
    public static void processVectorApi8Channels(int width, int height, int depth,
                                                 float[] in, float[] out,
                                                 float diffusionRate, float evaporationRate) {
        int totalCells = width * height * depth;
        float retention = 1.0f - diffusionRate;
        FloatVector vRetention = FloatVector.broadcast(SPECIES_256, retention);
        FloatVector vDiffRate = FloatVector.broadcast(SPECIES_256, diffusionRate);
        FloatVector vEvapRate = FloatVector.broadcast(SPECIES_256, evaporationRate);
        FloatVector vZero = FloatVector.zero(SPECIES_256);

        IntStream.range(0, totalCells).parallel().forEach(i -> {
            int tmp = i;
            int x = tmp % width;
            tmp /= width;
            int y = tmp % height;
            int z = tmp / height;

            int baseIdx = i * 8;

            int leftIdx   = (x > 0) ? ((i - 1) * 8) : -1;
            int rightIdx  = (x < width - 1) ? ((i + 1) * 8) : -1;
            int downIdx   = (y > 0) ? ((i - width) * 8) : -1;
            int upIdx     = (y < height - 1) ? ((i + width) * 8) : -1;
            int backIdx   = (z > 0) ? ((i - width * height) * 8) : -1;
            int frontIdx  = (z < depth - 1) ? ((i + width * height) * 8) : -1;

            int count = 0;
            FloatVector vSum = vZero;
            if (leftIdx >= 0)  { vSum = vSum.add(FloatVector.fromArray(SPECIES_256, in, leftIdx)); count++; }
            if (rightIdx >= 0) { vSum = vSum.add(FloatVector.fromArray(SPECIES_256, in, rightIdx)); count++; }
            if (downIdx >= 0)  { vSum = vSum.add(FloatVector.fromArray(SPECIES_256, in, downIdx)); count++; }
            if (upIdx >= 0)    { vSum = vSum.add(FloatVector.fromArray(SPECIES_256, in, upIdx)); count++; }
            if (backIdx >= 0)  { vSum = vSum.add(FloatVector.fromArray(SPECIES_256, in, backIdx)); count++; }
            if (frontIdx >= 0) { vSum = vSum.add(FloatVector.fromArray(SPECIES_256, in, frontIdx)); count++; }

            FloatVector vCurrent = FloatVector.fromArray(SPECIES_256, in, baseIdx);
            if (count > 0) {
                FloatVector vInvCount = FloatVector.broadcast(SPECIES_256, 1.0f / count);
                FloatVector vAvg = vSum.mul(vInvCount);
                FloatVector vDiffused = vCurrent.mul(vRetention).add(vAvg.mul(vDiffRate));
                FloatVector vOut = vDiffused.sub(vEvapRate).max(vZero);
                vOut.intoArray(out, baseIdx);
            } else {
                FloatVector vOut = vCurrent.mul(vRetention).sub(vEvapRate).max(vZero);
                vOut.intoArray(out, baseIdx);
            }
        });
    }

    /**
     * Vectorized continuous array decay over contiguous blocks of memory using preferred hardware registers.
     */
    public static void applyEvaporationAndDecaySimd(float[] pheromones, float decayFactor, float evaporationCutoff) {
        if (pheromones == null || pheromones.length == 0) return;
        int i = 0;
        int length = pheromones.length;
        VectorSpecies<Float> species = SPECIES_PREFERRED;
        FloatVector vDecay = FloatVector.broadcast(species, decayFactor);
        FloatVector vCutoff = FloatVector.broadcast(species, evaporationCutoff);
        FloatVector vZero = FloatVector.zero(species);

        for (; i <= species.loopBound(length); i += species.length()) {
            FloatVector vVal = FloatVector.fromArray(species, pheromones, i);
            FloatVector vDecayed = vVal.mul(vDecay);
            FloatVector vRes = vDecayed.sub(vCutoff).max(vZero);
            vRes.intoArray(pheromones, i);
        }

        for (; i < length; i++) {
            float val = pheromones[i] * decayFactor;
            float res = val - evaporationCutoff;
            pheromones[i] = (res > 0.0f) ? res : 0.0f;
        }
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
