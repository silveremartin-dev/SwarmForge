package org.swarmforge.benchmarks;

import org.swarmforge.core.ecs.EcsWorldManager;
import org.swarmforge.core.domain.Individual;
import java.util.UUID;

/**
 * Performance Benchmark for SwarmForge v2.0 Unified Entity Component System (ECS).
 * Measures throughput (ticks per second), frame time latency, and memory footprint
 * for scaling populations (1k, 10k, 50k, 100k, and 500k entities).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class UnifiedEcsBenchmark {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" SwarmForge v2.0 Unified ECS Performance Benchmark ");
        System.out.println("=================================================");

        int[] testPopulations = {1_000, 10_000, 50_000, 100_000, 500_000};
        int warmupTicks = 20;
        int benchmarkTicks = 100;
        float dt = 0.016666667f; // 60 Hz step

        for (int popSize : testPopulations) {
            runBenchmarkForPopulation(popSize, warmupTicks, benchmarkTicks, dt);
        }
    }

    private static void runBenchmarkForPopulation(int popSize, int warmupTicks, int benchmarkTicks, float dt) {
        System.out.printf("\n--> Benchmarking Population: %,d ECS Ants...\n", popSize);
        System.gc();

        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        EcsWorldManager ecsManager = new EcsWorldManager();
        UUID colonyId = UUID.randomUUID();
        ecsManager.getColonyFactory().createWorkersBatch(colonyId, popSize, 50.0f, 50.0f, 0.0f, null);

        // Warmup Pass
        for (int i = 0; i < warmupTicks; i++) {
            ecsManager.step(dt);
        }

        // Benchmark Pass
        long startTime = System.nanoTime();
        for (int i = 0; i < benchmarkTicks; i++) {
            ecsManager.step(dt);
        }
        long elapsedTimeNanos = System.nanoTime() - startTime;

        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        double totalSeconds = elapsedTimeNanos / 1_000_000_000.0;
        double ticksPerSecond = benchmarkTicks / totalSeconds;
        double avgFrameMs = (elapsedTimeNanos / (double) benchmarkTicks) / 1_000_000.0;
        double antUpdatesPerSecond = (popSize * (double) benchmarkTicks) / totalSeconds;
        double heapAllocatedMB = Math.max(0, endMemory - startMemory) / (1024.0 * 1024.0);

        System.out.printf("   - Benchmark Ticks    : %d ticks\n", benchmarkTicks);
        System.out.printf("   - Total Execution Time: %.3f s\n", totalSeconds);
        System.out.printf("   - Throughput         : %.1f Ticks/sec (%.2f ms/frame)\n", ticksPerSecond, avgFrameMs);
        System.out.printf("   - Ant Updates Rate   : %,.0f Ant-Updates/sec\n", antUpdatesPerSecond);
        System.out.printf("   - Memory Footprint   : %.2f MB\n", heapAllocatedMB);
    }
}
