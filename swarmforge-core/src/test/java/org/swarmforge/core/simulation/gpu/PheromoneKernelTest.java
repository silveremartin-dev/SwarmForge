/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.gpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Pheromone diffusion (Aparapi GPU Kernel and CPU multi-thread fallback).
 */
public class PheromoneKernelTest {

    @Test
    void testCpuFallbackDiffusionAndEvaporation() {
        int w = 5, h = 5, d = 5;
        int types = 2;
        int totalSize = w * h * d * types;

        float[] input = new float[totalSize];
        float[] output = new float[totalSize];

        // Center cell (2, 2, 2) has pheromone intensity 10.0
        int centerIndex = ((2 + 2 * w + 2 * w * h) * types) + 0;
        input[centerIndex] = 10.0f;

        float diffRate = 0.2f;
        float evapRate = 0.05f;

        PheromoneCpuFallback.process(w, h, d, types, input, output, diffRate, evapRate);

        // Center cell should diffuse away part of its value and evaporate
        assertTrue(output[centerIndex] < input[centerIndex], "Center intensity should decay and diffuse");

        // Neighbor (3, 2, 2) should receive diffused pheromone
        int neighborIndex = ((3 + 2 * w + 2 * w * h) * types) + 0;
        assertTrue(output[neighborIndex] > 0.0f, "Neighbor should receive diffused pheromone");
    }

    @Test
    void testAparapiKernelFallbackExecution() {
        int w = 4, h = 4, d = 4;
        int types = 1;
        int totalSize = w * h * d * types;

        float[] input = new float[totalSize];
        float[] output = new float[totalSize];

        input[0] = 5.0f; // cell (0,0,0)

        PheromoneKernel kernel = new PheromoneKernel(w, h, d, types, input, output, 0.1f, 0.01f);
        assertDoesNotThrow(() -> {
            kernel.execute(w * h * d);
            kernel.dispose();
        }, "Aparapi kernel execution should complete or fallback to CPU cleanly");
    }
}
