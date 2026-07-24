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
 * Unit tests for PheromoneVectorFallback (Option 1) and PheromoneOffHeapGrid (Option 3).
 */
public class PheromoneAdvancedFallbackTest {

    @Test
    void testVectorizedFallbackDiffusion() {
        int w = 6, h = 6, d = 6;
        int types = 4; // Use 4 types to test SIMD vector unroll loop
        int totalSize = w * h * d * types;

        float[] input = new float[totalSize];
        float[] output = new float[totalSize];

        int centerIndex = ((3 + 3 * w + 3 * w * h) * types) + 0;
        input[centerIndex] = 20.0f;

        PheromoneVectorFallback.processVectorized(w, h, d, types, input, output, 0.25f, 0.05f);

        assertTrue(output[centerIndex] < input[centerIndex], "Center cell value should diffuse and decrease");
        int neighborIndex = ((4 + 3 * w + 3 * w * h) * types) + 0;
        assertTrue(output[neighborIndex] > 0.0f, "Neighbor cell should receive diffused intensity");
    }

    @Test
    void testOffHeapMemoryGridLifecycle() {
        int w = 4, h = 4, d = 4, types = 2;
        try (PheromoneOffHeapGrid offHeap = new PheromoneOffHeapGrid(w, h, d, types)) {
            assertEquals(w * h * d * types, offHeap.getTotalElements());

            offHeap.set(1, 1, 1, 0, 15.5f);
            assertEquals(15.5f, offHeap.get(1, 1, 1, 0), 0.001f);

            float[] testBuffer = new float[offHeap.getTotalElements()];
            testBuffer[0] = 99.9f;
            offHeap.copyFrom(testBuffer);

            float[] readBack = new float[offHeap.getTotalElements()];
            offHeap.copyTo(readBack);

            assertEquals(99.9f, readBack[0], 0.001f);
        }
    }
}
