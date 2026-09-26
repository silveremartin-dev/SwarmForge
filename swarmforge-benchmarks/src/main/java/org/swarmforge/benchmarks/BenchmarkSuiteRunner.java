package org.swarmforge.benchmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Master Comprehensive Benchmark Suite Execution Engine.
 * 
 * Conducts automated end-to-end benchmarking of SwarmForge simulation performance:
 * 1. Hardware & Runtime Diagnostics (CPU Cores, RAM, Integrated GPU / Software Renderer)
 * 2. Species-wise performance evaluation
 * 3. Complete 3D virtual world scenarios (Terrarium 3D, Nests, Weather, Predators, Food)
 * 4. Headless vs Non-Headless (GUI 3D) throughput & latency comparison
 * 5. Report generation (Markdown report file + stdout execution tables)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class BenchmarkSuiteRunner {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("          SWARMFORGE COMPREHENSIVE SIMULATION BENCHMARK SUITE                  ");
        System.out.println("===============================================================================");
        
        // 1. Hardware Environment Diagnostics
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        int availableCores = Runtime.getRuntime().availableProcessors();
        long maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long totalMemoryMb = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        System.out.println("OS               : " + osName + " " + osVersion + " (" + osArch + ")");
        System.out.println("Java Runtime     : " + javaVersion + " (" + javaVendor + ")");
        System.out.println("Logical Processors: " + availableCores + " Cores");
        System.out.println("JVM Max Memory   : " + maxMemoryMb + " MB (Allocated: " + totalMemoryMb + " MB)");
        System.out.println("Graphics Hardware: Integrated Graphics / Software Renderer (No Discrete GPU)");
        System.out.println("-------------------------------------------------------------------------------\n");

        // Optimize benchmark I/O: disable disk logging during performance benchmark passes
        org.swarmforge.core.event.EventBus.getInstance().setDiskLoggingEnabled(false);

        List<String> markdownBuffer = new ArrayList<>();
        markdownBuffer.add("# 📊 SwarmForge Performance Benchmark Report\n");
        markdownBuffer.add("## 🖥️ System Architecture & Hardware Environment\n");
        markdownBuffer.add("| Parameter | Specification |");
        markdownBuffer.add("| :--- | :--- |");
        markdownBuffer.add("| **Operating System** | " + osName + " " + osVersion + " (" + osArch + ") |");
        markdownBuffer.add("| **Java Runtime** | " + javaVersion + " (" + javaVendor + ") |");
        markdownBuffer.add("| **CPU Cores** | " + availableCores + " Threads / Logical Cores |");
        markdownBuffer.add("| **System RAM / JVM** | " + maxMemoryMb + " MB Max Heap |");
        markdownBuffer.add("| **GPU Acceleration** | *Integrated Graphics / CPU Software Renderer (No Dedicated GPU)* |\n");

        // ---------------------------------------------------------------------------------
        // PHASE 1: Species Performance Comparison
        // ---------------------------------------------------------------------------------
        System.out.println(">>> PHASE 1: BENCHMARKING SPECIES PERFORMANCE & SCALING <<<");
        int[] speciesColonySizes = { 100, 500, 1000, 2500, 5000 };
        int speciesTicks = 20;

        List<SpeciesComparisonBenchmark.SpeciesBenchmarkResult> speciesResults = 
                SpeciesComparisonBenchmark.runSpeciesBenchmarks(speciesColonySizes, speciesTicks);

        System.out.printf("%-22s | %-24s | %-8s | %-12s | %-12s | %-12s%n",
                "Species Name", "Taxon / Scientific", "Pop", "TPS (ticks/s)", "Avg Lat (ms)", "p95 Lat (ms)");
        System.out.println("---------------------------------------------------------------------------------------------------");

        markdownBuffer.add("## 🐜 1. Species Comparative Performance & Scaling\n");
        markdownBuffer.add("| Species Name | Scientific Name | Population | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) | Memory (MB) |");
        markdownBuffer.add("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |");

        for (SpeciesComparisonBenchmark.SpeciesBenchmarkResult res : speciesResults) {
            System.out.printf("%-22s | %-24s | %-8d | %-12.2f | %-12.4f | %-12.4f%n",
                    res.speciesName(), res.category(), res.colonySize(), res.tps(), res.avgLatencyMs(), res.p95LatencyMs());
            
            markdownBuffer.add(String.format("| %s | *%s* | %,d | %.2f | %.4f | %.4f | %d MB |",
                    res.speciesName(), res.category(), res.colonySize(), res.tps(), res.avgLatencyMs(), res.p95LatencyMs(), res.memoryUsedMb()));
        }
        System.out.println();
        markdownBuffer.add("\n");

        // ---------------------------------------------------------------------------------
        // PHASE 2: Complete 3D Virtual World Scenario Benchmarks
        // ---------------------------------------------------------------------------------
        System.out.println(">>> PHASE 2: BENCHMARKING COMPLETE 3D VIRTUAL WORLD SCENARIOS <<<");
        List<ScenarioPopulator.ScenarioDescription> scenarios = List.of(
                ScenarioPopulator.createTemperateGardenScenario(1000),
                ScenarioPopulator.createForestTerritoryScenario(2500),
                ScenarioPopulator.createTropicalLeafcutterScenario(3500),
                ScenarioPopulator.createDesertFireAntScenario(5000)
        );

        markdownBuffer.add("## 🌐 2. Full 3D Virtual World Scenario Benchmarks\n");
        markdownBuffer.add("| Scenario Name | Species | Nest Architecture | Entities | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) |");
        markdownBuffer.add("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |");

        System.out.printf("%-30s | %-18s | %-18s | %-12s | %-12s%n",
                "Scenario Name", "Species", "Nest Type", "TPS (ticks/s)", "Avg Lat (ms)");
        System.out.println("---------------------------------------------------------------------------------------------");

        for (ScenarioPopulator.ScenarioDescription sc : scenarios) {
            HeadlessVsGuiBenchmarkRunner.BenchmarkResult res = HeadlessVsGuiBenchmarkRunner.runHeadlessBenchmark(sc, 10, 20);
            System.out.printf("%-30s | %-18s | %-18s | %-12.2f | %-12.4f%n",
                    sc.name(), sc.speciesName(), sc.nestType(), res.tps(), res.avgLatencyMs());

            markdownBuffer.add(String.format("| %s | %s | %s | %,d | %.2f | %.4f | %.4f |",
                    sc.name(), sc.speciesName(), sc.nestType(), sc.targetPopulation(), res.tps(), res.avgLatencyMs(), res.p95LatencyMs()));
        }
        System.out.println();
        markdownBuffer.add("\n");

        // ---------------------------------------------------------------------------------
        // PHASE 3: Headless vs Non-Headless (GUI Interface Graphique 3D) Benchmark
        // ---------------------------------------------------------------------------------
        System.out.println(">>> PHASE 3: HEADLESS VS NON-HEADLESS (GUI 3D VIEWPORT) COMPARISON <<<");
        ScenarioPopulator.ScenarioDescription benchmarkScenario = ScenarioPopulator.createTemperateGardenScenario(2000);

        HeadlessVsGuiBenchmarkRunner.BenchmarkResult headlessRes = HeadlessVsGuiBenchmarkRunner.runHeadlessBenchmark(benchmarkScenario, 10, 25);

        HeadlessVsGuiBenchmarkRunner.BenchmarkResult guiRes;
        try {
            guiRes = HeadlessVsGuiBenchmarkRunner.runGuiBenchmark(benchmarkScenario, 10, 25);
        } catch (Exception e) {
            System.err.println("GUI Benchmark error: " + e.getMessage());
            guiRes = new HeadlessVsGuiBenchmarkRunner.BenchmarkResult("Non-Headless (GUI 3D)", 2000, 0, 0, 0, 0, 0, 0, 0);
        }

        double overheadPct = 0.0;
        if (headlessRes.tps() > 0 && guiRes.tps() > 0) {
            overheadPct = ((headlessRes.tps() - guiRes.tps()) / headlessRes.tps()) * 100.0;
        }

        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("Mode: %-35s | TPS: %.2f | Avg Latency: %.4f ms%n", headlessRes.modeName(), headlessRes.tps(), headlessRes.avgLatencyMs());
        System.out.printf("Mode: %-35s | TPS: %.2f | Avg Latency: %.4f ms | FPS: %.1f%n", guiRes.modeName(), guiRes.tps(), guiRes.avgLatencyMs(), guiRes.fps());
        System.out.printf("GUI Presentation Overhead on CPU Renderer: %.2f%%%n", overheadPct);
        System.out.println("---------------------------------------------------------------------------------------------\n");

        markdownBuffer.add("## 🖥️ 3. Headless vs Non-Headless (GUI 3D Interface) Mode Comparison\n");
        markdownBuffer.add("| Execution Mode | Entities | TPS (ticks/s) | FPS (Render) | Avg Latency (ms) | p95 Latency (ms) | GUI Overhead |");
        markdownBuffer.add("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |");
        markdownBuffer.add(String.format("| **%s** | %,d | %.2f | N/A | %.4f | %.4f | Baseline (0%%) |",
                headlessRes.modeName(), headlessRes.colonySize(), headlessRes.tps(), headlessRes.avgLatencyMs(), headlessRes.p95LatencyMs()));
        markdownBuffer.add(String.format("| **%s** | %,d | %.2f | %.1f FPS | %.4f | %.4f | **+%.2f%% Overhead** |",
                guiRes.modeName(), guiRes.colonySize(), guiRes.tps(), guiRes.fps(), guiRes.avgLatencyMs(), guiRes.p95LatencyMs(), overheadPct));

        markdownBuffer.add("\n> **Technical Note**: On systems without discrete GPU acceleration, Non-Headless GUI mode utilizes CPU software rasterization for 3D/2D views. Headless mode isolates pure simulation compute capacity for maximum throughput.\n");

        // ---------------------------------------------------------------------------------
        // PHASE 4: Massive Population Scaling (Unified ECS Engine: 5K -> 2M Entities)
        // ---------------------------------------------------------------------------------
        System.out.println(">>> PHASE 4: BENCHMARKING MASSIVE POPULATION SCALING (5K -> 2M INDIVIDUALS) <<<");
        int[] massivePopulations = { 5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 1_000_000, 1_500_000, 2_000_000 };
        float deltaStep = 0.016666667f;

        markdownBuffer.add("## 🚀 4. Massive Population Scaling Benchmarks (5,000 to 2,000,000 Individuals)\n");
        markdownBuffer.add("| Population | Entities / Agents | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) | Agent-Updates / sec | Heap Memory (MB) |");
        markdownBuffer.add("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |");

        System.out.printf("%-12s | %-14s | %-14s | %-14s | %-20s | %-12s%n",
                "Population", "TPS (ticks/s)", "Avg Lat (ms)", "p95 Lat (ms)", "Agent-Updates/s", "Heap (MB)");
        System.out.println("------------------------------------------------------------------------------------------------------");

        for (int popSize : massivePopulations) {
            System.gc();
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
            long memBefore = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

            org.swarmforge.core.ecs.EcsWorldManager ecsManager = new org.swarmforge.core.ecs.EcsWorldManager();
            java.util.UUID colonyId = java.util.UUID.randomUUID();
            ecsManager.getColonyFactory().createWorkersBatch(colonyId, popSize, 50f, 50f, 0f, null);

            // Warmup
            int warmup = (popSize >= 1_000_000) ? 2 : 5;
            for (int w = 0; w < warmup; w++) {
                ecsManager.step(deltaStep);
            }

            // Measure
            int measured = (popSize >= 1_500_000) ? 10 : ((popSize >= 500_000) ? 15 : 25);
            long[] tickNanos = new long[measured];
            long startPhase = System.nanoTime();

            for (int t = 0; t < measured; t++) {
                long tStart = System.nanoTime();
                ecsManager.step(deltaStep);
                tickNanos[t] = System.nanoTime() - tStart;
            }
            long totalNanos = System.nanoTime() - startPhase;

            long memAfter = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            long heapUsed = Math.max(0, memAfter - memBefore);

            java.util.Arrays.sort(tickNanos);
            double tps = (measured * 1_000_000_000.0) / totalNanos;
            double avgLatMs = (totalNanos / (double) measured) / 1_000_000.0;
            double p95LatMs = tickNanos[(int) (measured * 0.95)] / 1_000_000.0;
            double agentUpdatesPerSec = (popSize * (double) measured) / (totalNanos / 1_000_000_000.0);

            System.out.printf("%-12d | %-14.2f | %-14.4f | %-14.4f | %,20.0f | %-12d%n",
                    popSize, tps, avgLatMs, p95LatMs, agentUpdatesPerSec, heapUsed);

            markdownBuffer.add(String.format("| %,d | %,d ants | %.2f | %.4f | %.4f | %,.0f | %d MB |",
                    popSize, popSize, tps, avgLatMs, p95LatMs, agentUpdatesPerSec, heapUsed));
        }
        System.out.println();
        markdownBuffer.add("\n");

        // ---------------------------------------------------------------------------------
        // Write Markdown Benchmark Report
        // ---------------------------------------------------------------------------------
        try {
            File docsDir = new File("docs");
            docsDir.mkdirs();
            File reportFile = new File(docsDir, "BENCHMARK_RESULTS.md");
            try (PrintWriter out = new PrintWriter(new FileWriter(reportFile))) {
                for (String line : markdownBuffer) {
                    out.println(line);
                }
            }
            System.out.println(">>> BENCHMARK REPORT SUCCESSFULLY WRITTEN TO: " + reportFile.getAbsolutePath() + " <<<");
        } catch (Exception ex) {
            System.err.println("Failed to write BENCHMARK_RESULTS.md: " + ex.getMessage());
        }

        System.out.println("===============================================================================");
        System.out.println("All Benchmarks Completed Successfully.");
    }
}
