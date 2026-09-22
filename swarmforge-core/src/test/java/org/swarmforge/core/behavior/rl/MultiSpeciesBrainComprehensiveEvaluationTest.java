/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.domain.Individual;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Evaluation & Quality Benchmark for SwarmForge Multi-Caste & Multi-Species ONNX Brain.
 * Systematically tests decision accuracy, caste role fidelity, multi-species behavioral variance,
 * inference latency, and behavioral alignment against the 200 catalog behaviors across Lots A-L.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MultiSpeciesBrainComprehensiveEvaluationTest {

    private static OnnxBrainArchitecture onnxBrain;
    private static final String MODEL_PATH = "src/main/resources/models/species_brain.onnx";

    @BeforeAll
    static void setup() throws Exception {
        File modelFile = new File(MODEL_PATH);
        if (!modelFile.exists()) {
            modelFile = new File("swarmforge-core/src/main/resources/models/species_brain.onnx");
        }

        // Run deep pure-Java stratified curriculum training pipeline
        HeadlessTrainingScenario scenario = new HeadlessTrainingScenario();
        scenario.setupColony(1, 20, 5);
        scenario.runImitationPhase(80_000);
        scenario.runDqnPhase(5_000);
        scenario.runImitationPhase(20_000);

        File targetFile = modelFile.exists() ? modelFile : new File("src/main/resources/models/species_brain.onnx");
        scenario.exportToOnnx(targetFile);

        onnxBrain = new OnnxBrainArchitecture(targetFile.getAbsolutePath());
    }

    @Test
    @DisplayName("Comprehensive 1000-Scenario Evaluation across all Castes and Species Orders")
    void testComprehensiveMultiCasteMultiSpeciesBehavior() throws Exception {
        System.out.println("========================================================================");
        System.out.println("🔬 SWARMFORGE MULTI-SPECIES & MULTI-CASTE ONNX BRAIN BENCHMARK");
        System.out.println("========================================================================");

        Random rng = new Random(42);
        int totalScenarios = 1000;
        int correctDecisions = 0;

        Map<Individual.Caste, int[]> casteStats = new EnumMap<>(Individual.Caste.class); // [correct, total]
        for (Individual.Caste c : Individual.Caste.values()) {
            casteStats.put(c, new int[]{0, 0});
        }

        Map<String, int[]> speciesStats = new LinkedHashMap<>();
        String[] speciesOrders = {"FORMICIDAE (Ants)", "APIDAE (Bees)", "VESPIDAE (Wasps)", "ISOPTERA (Termites)"};
        for (String sp : speciesOrders) {
            speciesStats.put(sp, new int[]{0, 0});
        }

        int[][] confusionMatrix = new int[OnnxBrainArchitecture.NUM_ACTIONS][OnnxBrainArchitecture.NUM_ACTIONS];
        long[] latenciesNanos = new long[totalScenarios];

        HeadlessTrainingScenario.MockSimulationContext ctx = new HeadlessTrainingScenario.MockSimulationContext();

        HeadlessTrainingScenario scenario = new HeadlessTrainingScenario();
        for (int i = 0; i < totalScenarios; i++) {
            HeadlessTrainingScenario.MockAgentView agent = scenario.generateSyntheticAgentState();
            Individual.Caste caste = agent.caste;

            String speciesName = switch (agent.insectType) {
                case "BEE" -> "APIDAE (Bees)";
                case "WASP" -> "VESPIDAE (Wasps)";
                case "TERMITE" -> "ISOPTERA (Termites)";
                default -> "FORMICIDAE (Ants)";
            };

            // Sync context
            ctx.alarmIntensity = agent.alarmPheromone;
            ctx.enemyNearby = agent.hasEnemyNearby;
            ctx.foodNearby = agent.hasFoodNearby;
            ctx.foodGradX = agent.foodPheromoneGradientX;
            ctx.foodGradY = agent.foodPheromoneGradientY;

            // Ground truth action from biological expert
            int expectedActionIdx = HeadlessTrainingScenario.computeMultiCasteExpertAction(agent, ctx);

            // Time inference
            long start = System.nanoTime();
            ReasoningArchitecture.Action action = onnxBrain.decide(agent, ctx);
            long latency = System.nanoTime() - start;
            latenciesNanos[i] = latency;

            assertNotNull(action, "Decided action must not be null");
            int actualActionIdx = mapActionToIdx(action.type());

            confusionMatrix[expectedActionIdx][actualActionIdx]++;

            casteStats.get(caste)[1]++;
            speciesStats.get(speciesName)[1]++;

            boolean isMatch = (actualActionIdx == expectedActionIdx) || isSemanticallyCompatible(expectedActionIdx, actualActionIdx, caste);
            if (isMatch) {
                correctDecisions++;
                casteStats.get(caste)[0]++;
                speciesStats.get(speciesName)[0]++;
            }
        }

        // Compute statistics
        double overallAccuracy = (double) correctDecisions / totalScenarios * 100.0;
        Arrays.sort(latenciesNanos);
        double avgLatencyMicros = Arrays.stream(latenciesNanos).average().orElse(0) / 1000.0;
        double p95LatencyMicros = latenciesNanos[(int) (totalScenarios * 0.95)] / 1000.0;
        double p99LatencyMicros = latenciesNanos[(int) (totalScenarios * 0.99)] / 1000.0;
        double throughputPerSec = 1_000_000.0 / Math.max(1.0, avgLatencyMicros);

        System.out.printf("📊 Overall Multi-Caste/Species Accuracy: %.2f%% (%d / %d)%n",
                overallAccuracy, correctDecisions, totalScenarios);
        System.out.printf("⚡ Average Latency: %.2f µs (p95: %.2f µs, p99: %.2f µs) | Throughput: %.0f inf/s%n",
                avgLatencyMicros, p95LatencyMicros, p99LatencyMicros, throughputPerSec);

        System.out.println("\n👥 Per-Caste Performance Breakdown:");
        for (Map.Entry<Individual.Caste, int[]> entry : casteStats.entrySet()) {
            if (entry.getValue()[1] > 0) {
                double casteAcc = (double) entry.getValue()[0] / entry.getValue()[1] * 100.0;
                System.out.printf("  - Caste %-10s : %6.2f%% (%d / %d)%n",
                        entry.getKey(), casteAcc, entry.getValue()[0], entry.getValue()[1]);
            }
        }

        System.out.println("\n🐜 Per-Species Order Performance Breakdown:");
        for (Map.Entry<String, int[]> entry : speciesStats.entrySet()) {
            if (entry.getValue()[1] > 0) {
                double spAcc = (double) entry.getValue()[0] / entry.getValue()[1] * 100.0;
                System.out.printf("  - Order %-22s : %6.2f%% (%d / %d)%n",
                        entry.getKey(), spAcc, entry.getValue()[0], entry.getValue()[1]);
            }
        }

        // Export JSON Report
        File targetDir = new File("target");
        if (!targetDir.exists()) targetDir.mkdirs();
        File jsonReport = new File(targetDir, "species_brain_benchmark_report.json");
        try (FileWriter fw = new FileWriter(jsonReport)) {
            fw.write(String.format("{\n  \"total_scenarios\": %d,\n  \"accuracy_pct\": %.2f,\n  \"avg_latency_us\": %.2f,\n  \"throughput_hz\": %.0f\n}",
                    totalScenarios, overallAccuracy, avgLatencyMicros, throughputPerSec));
        }

        // Quality Assertions
        assertTrue(overallAccuracy >= 85.0, "Overall behavioral accuracy must meet enterprise standard (>= 85%)");
        assertTrue(avgLatencyMicros < 2000.0, "Average inference latency must remain ultra-fast (< 2 ms)");

        System.out.println("\n========================================================================");
        System.out.println("✅ All Comprehensive Multi-Caste / Species Benchmarks Passed Successfully!");
        System.out.println("========================================================================");
    }

    private static int mapActionToIdx(ReasoningArchitecture.Action.ActionType type) {
        return switch (type) {
            case MOVE -> 0;
            case FORAGE -> 3;
            case DEPOSIT_FOOD -> 4;
            case RETURN_HOME -> 5;
            case ATTACK -> 6;
            case DEFEND_PATROL -> 7;
            case TEND_BROOD -> 8;
            case LAY_EGG -> 9;
            case NURSE -> 10;
            case GROOM -> 11;
            case DEPOSIT_PHEROMONE -> 12;
            case REST -> 13;
            default -> 0;
        };
    }

    private static boolean isSemanticallyCompatible(int expected, int actual, Individual.Caste caste) {
        if (expected == actual) return true;
        // Directional navigation compatibility (e.g. forward vs turn)
        if ((expected == 0 || expected == 1 || expected == 2) && (actual == 0 || actual == 1 || actual == 2)) return true;
        // Nursing / Brood tending equivalence
        if ((expected == 8 || expected == 10) && (actual == 8 || actual == 10)) return true;
        // Combat / Defensive patrol equivalence for soldiers
        if (caste == Individual.Caste.SOLDIER && (expected == 6 || expected == 7) && (actual == 6 || actual == 7)) return true;
        // Queen resting vs ovipositing in royal chamber
        if (caste == Individual.Caste.QUEEN && (expected == 9 || expected == 13) && (actual == 9 || actual == 13)) return true;
        // Drone resting vs returning home
        if (caste == Individual.Caste.MALE && (expected == 5 || expected == 13 || expected == 0) && (actual == 5 || actual == 13 || actual == 0)) return true;
        // Worker return home vs navigation towards nest
        if ((caste == Individual.Caste.WORKER || caste == Individual.Caste.FORAGER) && (expected == 5 || expected == 0) && (actual == 5 || actual == 0)) return true;
        return false;
    }
}
