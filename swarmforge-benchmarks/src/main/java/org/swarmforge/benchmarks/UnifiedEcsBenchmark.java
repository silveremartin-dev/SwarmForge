package org.swarmforge.benchmarks;

import org.swarmforge.core.ecs.EcsWorldManager;
import java.util.UUID;

/**
 * SwarmForge v2.0 Unified ECS Performance Benchmark.
 * Measures entity throughput, frame latency, ant-updates/sec, and heap footprint
 * across populations from 1,000 to 1,000,000 entities.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class UnifiedEcsBenchmark {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" SwarmForge v2.0 Unified ECS Performance Benchmark");
        System.out.println("=================================================");

        // Populations: 1k → 10k → 50k → 100k → 500k → 1M
        int[] testPopulations = {1_000, 10_000, 50_000, 100_000, 500_000, 1_000_000};
        int warmupTicks   = 10;
        int benchmarkTicks = 50;
        float dt = 0.016666667f; // 60 Hz

        for (int popSize : testPopulations) {
            runBenchmark(popSize, warmupTicks, benchmarkTicks, dt);
        }

        System.out.println("\n[DONE] Full ECS benchmark suite completed.");
    }

    private static void runBenchmark(int popSize, int warmupTicks, int benchmarkTicks, float dt) {
        System.out.printf("%n--> Benchmarking Population: %,d ECS Ants...%n", popSize);
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        long memBefore = usedHeapMB();

        EcsWorldManager ecsManager = new EcsWorldManager();
        UUID colonyId = UUID.randomUUID();
        ecsManager.getColonyFactory().createWorkersBatch(colonyId, popSize, 50f, 50f, 0f, null);

        // Warmup
        for (int i = 0; i < warmupTicks; i++) ecsManager.step(dt);

        // Benchmark
        long t0 = System.nanoTime();
        for (int i = 0; i < benchmarkTicks; i++) ecsManager.step(dt);
        long elapsed = System.nanoTime() - t0;

        long memAfter = usedHeapMB();

        double totalSec  = elapsed / 1e9;
        double tps       = benchmarkTicks / totalSec;
        double msPerTick = (elapsed / (double) benchmarkTicks) / 1e6;
        double antRate   = (popSize * (double) benchmarkTicks) / totalSec;
        long   memMB     = Math.max(0, memAfter - memBefore);

        System.out.printf("   Ticks         : %d%n",            benchmarkTicks);
        System.out.printf("   Time          : %.3f s%n",        totalSec);
        System.out.printf("   Throughput    : %.1f TPS  (%.2f ms/tick)%n", tps, msPerTick);
        System.out.printf("   Ant-Update/s  : %,.0f%n",         antRate);
        System.out.printf("   Heap delta    : %d MB%n",          memMB);
    }

    private static long usedHeapMB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }
}
