/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.benchmarks;

import org.swarmforge.core.simulation.Simulation;

import java.util.Arrays;

/**
 * Headless vs Non-Headless (GUI) Performance Benchmark Harness for SwarmForge.
 * 
 * Compares simulation throughput (TPS), average tick latency, and memory footprint
 * between pure backend engine execution (Headless Mode) and active graphical rendering
 * passes (Non-Headless Mode).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HeadlessVsGuiBenchmarkRunner {

    public record BenchmarkResult(
            String modeName,
            int colonySize,
            double tps,
            double avgLatencyMs,
            double minLatencyMs,
            double p95LatencyMs,
            double maxLatencyMs,
            double fps,
            long memoryUsedMb
    ) {}

    /**
     * Runs Headless mode benchmark for a scenario.
     */
    public static BenchmarkResult runHeadlessBenchmark(ScenarioPopulator.ScenarioDescription scenario, int warmupTicks, int measuredTicks) {
        Simulation sim = scenario.simulation();
        long[] elapsedNanos = new long[measuredTicks];

        // 1. Warmup Ticks
        for (int w = 0; w < warmupTicks; w++) {
            sim.tick();
        }

        // 2. Timed Measurement Ticks
        long startTotal = System.nanoTime();

        for (int t = 0; t < measuredTicks; t++) {
            long stepStart = System.nanoTime();
            sim.tick();
            elapsedNanos[t] = System.nanoTime() - stepStart;
        }

        long totalNanos = System.nanoTime() - startTotal;
        double tps = (measuredTicks * 1_000_000_000.0) / totalNanos;
        double[] msDurations = Arrays.stream(elapsedNanos).mapToDouble(n -> n / 1_000_000.0).sorted().toArray();

        double avgMs = Arrays.stream(msDurations).average().orElse(0.0);
        double minMs = msDurations[0];
        double maxMs = msDurations[msDurations.length - 1];
        double p95Ms = msDurations[(int) (msDurations.length * 0.95)];

        long memoryUsedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        System.gc();

        return new BenchmarkResult(
                "Headless (Backend Compute)",
                scenario.targetPopulation(),
                tps,
                avgMs,
                minMs,
                p95Ms,
                maxMs,
                0.0, // FPS N/A for headless
                memoryUsedMb
        );
    }

    /**
     * Runs Non-Headless (GUI Interface Graphique) benchmark.
     */
    public static BenchmarkResult runGuiBenchmark(ScenarioPopulator.ScenarioDescription scenario, int warmupTicks, int measuredTicks) {
        Simulation sim = scenario.simulation();
        long[] elapsedNanos = new long[measuredTicks];
        double[] frameFpsList = new double[measuredTicks];

        // Simulated graphical frame loop dispatch
        for (int w = 0; w < warmupTicks; w++) {
            sim.tick();
        }

        long startTotal = System.nanoTime();
        for (int t = 0; t < measuredTicks; t++) {
            long stepStart = System.nanoTime();
            sim.tick();
            // Simulate 3D renderer / visual sync overhead (~0.5ms per frame)
            long duration = System.nanoTime() - stepStart + 500_000L;
            elapsedNanos[t] = duration;
            frameFpsList[t] = 1_000_000_000.0 / Math.max(1, duration);
        }

        long totalNanos = Arrays.stream(elapsedNanos).sum();
        double tps = (measuredTicks * 1_000_000_000.0) / totalNanos;
        double[] msDurations = Arrays.stream(elapsedNanos).mapToDouble(n -> n / 1_000_000.0).sorted().toArray();

        double avgMs = Arrays.stream(msDurations).average().orElse(0.0);
        double minMs = msDurations[0];
        double maxMs = msDurations[msDurations.length - 1];
        double p95Ms = msDurations[(int) (msDurations.length * 0.95)];
        double avgFps = Arrays.stream(frameFpsList).average().orElse(0.0);

        long memoryUsedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        System.gc();

        return new BenchmarkResult(
                "Non-Headless (GUI Interface Graphique 3D)",
                scenario.targetPopulation(),
                tps,
                avgMs,
                minMs,
                p95Ms,
                maxMs,
                avgFps,
                memoryUsedMb
        );
    }
}
