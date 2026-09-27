package org.swarmforge.benchmarks;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.engine.*;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.Species;

import java.util.UUID;

/**
 * Comparative Dual-Engine Benchmark: Pure Java 21 Artemis-odb ECS vs Native Rust SIMD Engine.
 *
 * Measures:
 *  - Tick Latency (ms/tick)
 *  - Entity Throughput (updates/s)
 *  - Memory Footprint & Off-heap Allocation
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class DualEngineBenchmark {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" SwarmForge Dual-Engine Comparative Benchmark: Java 21 ECS vs Native Rust");
        System.out.println("==========================================================================");

        int[] populationTiers = { 1_000, 10_000, 50_000, 100_000, 250_000 };
        int warmupTicks = 5;
        int benchmarkTicks = 20;
        float dt = 0.1f; // 10 Hz physical tick step

        for (int population : populationTiers) {
            System.out.printf("%n====================================================================%n");
            System.out.printf("  BENCHMARK TIER: %,d ENTITIES (Ticks: %d, Step: %.2fs)%n", population, benchmarkTicks, dt);
            System.out.printf("====================================================================%n");

            // 1. Benchmark Java ECS Engine
            BenchmarkResult javaResult = runEngineBenchmark(
                    SimulationEngineType.JAVA_ECS, population, warmupTicks, benchmarkTicks, dt
            );

            // 2. Benchmark Rust Native Engine (or Fallback)
            BenchmarkResult rustResult = runEngineBenchmark(
                    SimulationEngineType.RUST_NATIVE, population, warmupTicks, benchmarkTicks, dt
            );

            // Print Comparative Table
            printComparisonTable(population, javaResult, rustResult);
        }

        System.out.println("\n[DONE] Dual-engine comparative benchmark completed successfully.");
    }

    private static BenchmarkResult runEngineBenchmark(SimulationEngineType type, int population,
                                                      int warmupTicks, int benchmarkTicks, float dt) {
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        long memBefore = getUsedMemoryMB();

        SimulationEngine engine = SimulationEngineFactory.createEngine(type);
        SparsePheromoneGrid pGrid = new SparsePheromoneGrid(200, 200, 50);
        engine.initialize(200, 200, 50, pGrid);
        engine.start();

        UUID colonyId = UUID.randomUUID();
        Species species = new FormicaRufa();

        // Spawn population
        for (int i = 0; i < population; i++) {
            float x = (float) (Math.random() * 180.0 + 10.0);
            float y = (float) (Math.random() * 180.0 + 10.0);
            float z = (float) (Math.random() * 5.0);
            Individual.Caste caste = (i % 10 == 0) ? Individual.Caste.SOLDIER : Individual.Caste.WORKER;
            Individual.Job job = (i % 2 == 0) ? Individual.Job.FORAGER : Individual.Job.NURSE;
            engine.spawnAnt(colonyId, caste, job, x, y, z, species);
        }

        // Warmup
        for (int i = 0; i < warmupTicks; i++) {
            engine.step(dt);
        }

        // Timed benchmark loop
        long startNanos = System.nanoTime();
        for (int i = 0; i < benchmarkTicks; i++) {
            engine.step(dt);
        }
        long elapsedNanos = System.nanoTime() - startNanos;

        long memAfter = getUsedMemoryMB();
        engine.close();

        double totalSec = elapsedNanos / 1e9;
        double tps = benchmarkTicks / totalSec;
        double msPerTick = (elapsedNanos / (double) benchmarkTicks) / 1e6;
        double updatesPerSec = (population * (double) benchmarkTicks) / totalSec;
        long memDeltaMB = Math.max(0, memAfter - memBefore);

        return new BenchmarkResult(type.name(), totalSec, tps, msPerTick, updatesPerSec, memDeltaMB);
    }

    private static void printComparisonTable(int pop, BenchmarkResult javaRes, BenchmarkResult rustRes) {
        System.out.printf("%-20s | %-12s | %-14s | %-18s | %-12s%n",
                "Engine Backend", "Time (s)", "Throughput (TPS)", "Updates/sec", "Heap Delta");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-20s | %10.3f s | %10.1f TPS | %,15d u/s | %9d MB%n",
                javaRes.engineName, javaRes.totalTimeSec, javaRes.tps, (long) javaRes.updatesPerSec, javaRes.memDeltaMB);
        System.out.printf("%-20s | %10.3f s | %10.1f TPS | %,15d u/s | %9d MB%n",
                rustRes.engineName, rustRes.totalTimeSec, rustRes.tps, (long) rustRes.updatesPerSec, rustRes.memDeltaMB);

        double speedup = rustRes.updatesPerSec / javaRes.updatesPerSec;
        System.out.printf("--> Relative Performance: %.2fx%n", speedup);
    }

    private static long getUsedMemoryMB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    private record BenchmarkResult(
            String engineName,
            double totalTimeSec,
            double tps,
            double msPerTick,
            double updatesPerSec,
            long memDeltaMB
    ) {}
}
