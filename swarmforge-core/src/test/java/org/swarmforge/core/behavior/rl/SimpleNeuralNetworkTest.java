/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleNeuralNetworkTest {

    @Test
    void testForwardDimensionConsistency() {
        SimpleNeuralNetwork net = new SimpleNeuralNetwork(16, 32, 6);
        float[] input = new float[16];
        input[0] = 0.8f;
        input[3] = -0.5f;

        float[] output = net.forward(input);
        assertEquals(6, output.length, "Output layer dimension should match network definition");
        for (float v : output) {
            assertFalse(Float.isNaN(v), "Output should not contain NaN");
        }
    }

    @Test
    void testXorConvergence() {
        // Simple non-linear 2-layer network learning XOR
        SimpleNeuralNetwork net = new SimpleNeuralNetwork(2, 16, 2);
        net.learningRate = 0.05f;

        float[][] inputs = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };
        int[] targets = {0, 1, 1, 0};

        // Train for 2000 steps
        for (int step = 0; step < 2000; step++) {
            int idx = step % 4;
            net.trainImitationStep(inputs[idx], targets[idx]);
        }

        // Test accuracy
        for (int i = 0; i < 4; i++) {
            float[] probs = net.predictProbabilities(inputs[i]);
            int predicted = SimpleNeuralNetwork.argmax(probs);
            assertEquals(targets[i], predicted, "XOR failed for input [" + inputs[i][0] + ", " + inputs[i][1] + "]");
        }
    }

    @Test
    void testTargetWeightCopying() {
        SimpleNeuralNetwork source = new SimpleNeuralNetwork(4, 8, 2);
        SimpleNeuralNetwork target = new SimpleNeuralNetwork(4, 8, 2);

        target.copyWeightsFrom(source, 1.0f); // 100% copy

        float[] input = new float[]{0.1f, 0.2f, 0.3f, 0.4f};
        float[] outSource = source.forward(input);
        float[] outTarget = target.forward(input);

        for (int i = 0; i < outSource.length; i++) {
            assertEquals(outSource[i], outTarget[i], 1e-5f, "Target weights should be identical after hard copy");
        }
    }
}
