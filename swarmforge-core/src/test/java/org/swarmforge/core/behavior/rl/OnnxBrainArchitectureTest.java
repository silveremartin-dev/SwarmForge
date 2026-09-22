/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.domain.Individual;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OnnxBrainArchitectureTest {

    private static SimpleNeuralNetwork baseNetwork;
    private static byte[] onnxBytes;

    @BeforeAll
    static void setUp() {
        baseNetwork = new SimpleNeuralNetwork(
                OnnxBrainArchitecture.OBSERVATION_DIM,
                32,
                OnnxBrainArchitecture.NUM_ACTIONS
        );
        onnxBytes = OnnxModelSerializer.serializeToBytes(baseNetwork, "test_species_brain");
        assertNotNull(onnxBytes);
        assertTrue(onnxBytes.length > 0, "Serialized ONNX model must have non-zero length");
    }

    @Test
    void testDirectByteInference() {
        try (OnnxBrainArchitecture brain = new OnnxBrainArchitecture(onnxBytes, "test_byte_model")) {
            Individual ant = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 5.0f, 5.0f, 0.0f);
            ant.setEnergy(80.0f);
            ant.setHomePosition(0f, 0f, 0f);

            ReasoningArchitecture.Action action = brain.decide(ant, null);

            assertNotNull(action, "Brain decision must not be null");
            assertNotNull(action.type(), "Action type must not be null");
            System.out.println("✅ ONNX direct byte inference succeeded. Decided Action: " + action.type());
        }
    }

    @Test
    void testFileBasedInference(@TempDir Path tempDir) throws Exception {
        File modelFile = tempDir.resolve("species_brain_test.onnx").toFile();
        OnnxModelSerializer.serializeToFile(baseNetwork, "test_file_model", modelFile);
        assertTrue(modelFile.exists());

        try (OnnxBrainArchitecture brain = new OnnxBrainArchitecture(modelFile.getAbsolutePath())) {
            Individual ant = new Individual(UUID.randomUUID(), Individual.Caste.SOLDIER, 10.0f, 10.0f, 0.0f);
            ant.setEnergy(95.0f);

            ReasoningArchitecture.Action action = brain.decide(ant, null);

            assertNotNull(action);
            System.out.println("✅ ONNX file-based inference succeeded. Decided Action: " + action.type());
        }
    }

    @Test
    void testGracefulFallbackOnMissingFile() {
        try (OnnxBrainArchitecture brain = new OnnxBrainArchitecture("non_existent_file.onnx")) {
            Individual ant = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 0, 0, 0);

            // Should not crash, but fallback gracefully to heuristic/Q-Table
            ReasoningArchitecture.Action action = brain.decide(ant, null);
            assertNotNull(action, "Should return a valid action even when model file is absent");
        }
    }

    @Test
    void testHighThroughputInferenceBenchmark() {
        try (OnnxBrainArchitecture brain = new OnnxBrainArchitecture(onnxBytes, "benchmark_model")) {
            Individual ant = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 0, 0, 0);

            long start = System.nanoTime();
            int iterations = 10_000;
            for (int i = 0; i < iterations; i++) {
                brain.decide(ant, null);
            }
            long durationNs = System.nanoTime() - start;
            double msPerDecision = (durationNs / 1_000_000.0) / iterations;
            double opsPerSec = iterations / (durationNs / 1_000_000_000.0);

            System.out.printf("⚡ ONNX Inference Performance: %.4f ms/decision (%.0f decisions/sec)%n", msPerDecision, opsPerSec);
            assertTrue(opsPerSec > 1000, "Inference rate should exceed 1000 decisions/sec");
        }
    }
}
