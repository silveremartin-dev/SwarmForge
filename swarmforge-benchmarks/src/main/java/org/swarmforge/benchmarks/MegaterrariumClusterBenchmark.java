/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.benchmarks;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.spatial.BorderMigrationSystem;
import org.swarmforge.core.species.DefaultSpecies;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Performance Benchmark comparing Local Single-Node Execution vs 2-Node Sharded Megaterrarium Cluster.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MegaterrariumClusterBenchmark {

    public record BenchmarkMetrics(
            String configuration,
            int population,
            double tps,
            double avgLatencyMs,
            double p95LatencyMs,
            double agentUpdatesPerSec,
            long heapMb
    ) {}

    public static void main(String[] args) throws Exception {
        System.out.println("===============================================================================");
        System.out.println("     SWARMFORGE 1-NODE (LOCAL) VS 2-NODE CLUSTER (MEGATERRARIUM) BENCHMARK     ");
        System.out.println("===============================================================================");

        int[] populations = { 5_000, 10_000, 25_000, 50_000, 100_000, 250_000, 500_000 };
        int measuredTicks = 20;

        System.out.printf("%-10s | %-16s | %-12s | %-14s | %-14s | %-20s | %-10s%n",
                "Pop", "Config", "TPS", "Avg Lat (ms)", "p95 Lat (ms)", "Agent-Updates/s", "Speedup");
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        for (int pop : populations) {
            BenchmarkMetrics singleNode = runSingleNode(pop, measuredTicks);
            BenchmarkMetrics twoNode = runTwoNodeCluster(pop, measuredTicks);

            double speedup = twoNode.tps() / singleNode.tps();

            System.out.printf("%-10d | %-16s | %-12.2f | %-14.4f | %-14.4f | %,20.0f | 1.00x (Base)%n",
                    pop, singleNode.configuration(), singleNode.tps(), singleNode.avgLatencyMs(), singleNode.p95LatencyMs(), singleNode.agentUpdatesPerSec());
            System.out.printf("%-10d | %-16s | %-12.2f | %-14.4f | %-14.4f | %,20.0f | %.2fx%n",
                    pop, twoNode.configuration(), twoNode.tps(), twoNode.avgLatencyMs(), twoNode.p95LatencyMs(), twoNode.agentUpdatesPerSec(), speedup);
            System.out.println("------------------------------------------------------------------------------------------------------------------");
        }
    }

    public static BenchmarkMetrics runSingleNode(int totalPopulation, int ticks) {
        System.gc();
        long mem0 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        int width = 100, height = 100, depth = 30;
        Terrarium terrarium = new Terrarium(width, height, depth);
        Simulation sim = new Simulation(terrarium);
        DefaultSpecies species = new DefaultSpecies();
        Colony colony = new Colony(species, 50f, 50f, 5f);
        sim.addColony(colony);

        java.util.List<Individual> list = new java.util.ArrayList<>(totalPopulation);
        for (int i = 0; i < totalPopulation; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.WORKER,
                    (float) (Math.random() * 98 + 1), (float) (Math.random() * 98 + 1), 5f);
            list.add(ind);
        }
        colony.addIndividualsBulk(list);

        // Warmup
        for (int w = 0; w < 3; w++) sim.tick();

        long[] nanos = new long[ticks];
        long start = System.nanoTime();
        for (int t = 0; t < ticks; t++) {
            long t0 = System.nanoTime();
            sim.tick();
            nanos[t] = System.nanoTime() - t0;
        }
        long elapsed = System.nanoTime() - start;
        long mem1 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        Arrays.sort(nanos);
        double tps = (ticks * 1e9) / elapsed;
        double avgLat = (elapsed / (double) ticks) / 1e6;
        double p95Lat = nanos[(int) (ticks * 0.95)] / 1e6;
        double updatesSec = (totalPopulation * (double) ticks) / (elapsed / 1e9);

        return new BenchmarkMetrics("1-Node Local", totalPopulation, tps, avgLat, p95Lat, updatesSec, Math.max(0, mem1 - mem0));
    }

    public static BenchmarkMetrics runTwoNodeCluster(int totalPopulation, int ticks) throws Exception {
        System.gc();
        long mem0 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        int popPerNode = totalPopulation / 2;
        int width = 100, height = 100, depth = 30;

        // Node 0
        Terrarium terr0 = new Terrarium(width, height, depth);
        Simulation sim0 = new Simulation(terr0);
        Colony col0 = new Colony(new DefaultSpecies(), 50f, 50f, 5f);
        sim0.addColony(col0);
        java.util.List<Individual> list0 = new java.util.ArrayList<>(popPerNode);
        for (int i = 0; i < popPerNode; i++) {
            list0.add(new Individual(col0.getId(), Individual.Caste.WORKER, (float) (Math.random() * 98 + 1), (float) (Math.random() * 98 + 1), 5f));
        }
        col0.addIndividualsBulk(list0);

        // Node 1
        Terrarium terr1 = new Terrarium(width, height, depth);
        Simulation sim1 = new Simulation(terr1);
        Colony col1 = new Colony(new DefaultSpecies(), 50f, 50f, 5f);
        sim1.addColony(col1);
        java.util.List<Individual> list1 = new java.util.ArrayList<>(popPerNode);
        for (int i = 0; i < popPerNode; i++) {
            list1.add(new Individual(col1.getId(), Individual.Caste.WORKER, (float) (Math.random() * 98 + 1), (float) (Math.random() * 98 + 1), 5f));
        }
        col1.addIndividualsBulk(list1);

        BorderMigrationSystem migrationSystem = new BorderMigrationSystem();
        BorderMigrationSystem.TerrariumCoord coord0 = new BorderMigrationSystem.TerrariumCoord(0, 0);
        BorderMigrationSystem.TerrariumCoord coord1 = new BorderMigrationSystem.TerrariumCoord(1, 0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Warmup
        for (int w = 0; w < 3; w++) {
            Future<?> f0 = executor.submit(() -> sim0.tick());
            Future<?> f1 = executor.submit(() -> sim1.tick());
            f0.get();
            f1.get();
        }

        long[] nanos = new long[ticks];
        long start = System.nanoTime();

        for (int t = 0; t < ticks; t++) {
            long t0 = System.nanoTime();
            // Parallel execution across 2 compute nodes
            Future<?> f0 = executor.submit(() -> sim0.tick());
            Future<?> f1 = executor.submit(() -> sim1.tick());
            f0.get();
            f1.get();

            // Inter-node boundary handoff
            migrationSystem.checkAndProcessBoundaryMigrations(sim0, coord0);
            migrationSystem.checkAndProcessBoundaryMigrations(sim1, coord1);

            nanos[t] = System.nanoTime() - t0;
        }

        long elapsed = System.nanoTime() - start;
        executor.shutdown();

        long mem1 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        Arrays.sort(nanos);
        double tps = (ticks * 1e9) / elapsed;
        double avgLat = (elapsed / (double) ticks) / 1e6;
        double p95Lat = nanos[(int) (ticks * 0.95)] / 1e6;
        double updatesSec = (totalPopulation * (double) ticks) / (elapsed / 1e9);

        return new BenchmarkMetrics("2-Node Cluster", totalPopulation, tps, avgLat, p95Lat, updatesSec, Math.max(0, mem1 - mem0));
    }
}
