package org.swarmforge.benchmarks;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.core.simulation.Simulation;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Headless vs Non-Headless (GUI) Performance Benchmark Harness for SwarmForge.
 * 
 * Compares simulation throughput (TPS), average tick latency, and memory footprint
 * between pure backend engine execution (Headless Mode) and active JavaFX 3D/2D 
 * GUI editor rendering passes (Non-Headless Mode).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HeadlessVsGuiBenchmarkRunner {

    private static final AtomicBoolean javaFxInitialized = new AtomicBoolean(false);

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
     * Initializes JavaFX runtime if not already active.
     */
    public static void ensureJavaFxInitialized() throws Exception {
        if (javaFxInitialized.compareAndSet(false, true)) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException e) {
                // JavaFX platform already started
                latch.countDown();
            }
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout initializing JavaFX Toolkit for Non-Headless benchmark");
            }
        }
    }

    /**
     * Runs Headless mode benchmark for a scenario.
     */
    public static BenchmarkResult runHeadlessBenchmark(ScenarioPopulator.ScenarioDescription scenario, int warmupTicks, int measuredTicks) {
        Simulation sim = scenario.simulation();

        // Warmup
        for (int w = 0; w < warmupTicks; w++) {
            sim.tick();
        }

        long[] elapsedNanos = new long[measuredTicks];
        long startTotal = System.nanoTime();

        for (int t = 0; t < measuredTicks; t++) {
            long tickStart = System.nanoTime();
            sim.tick();
            elapsedNanos[t] = System.nanoTime() - tickStart;
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
    public static BenchmarkResult runGuiBenchmark(ScenarioPopulator.ScenarioDescription scenario, int warmupTicks, int measuredTicks) throws Exception {
        ensureJavaFxInitialized();

        Simulation sim = scenario.simulation();
        long[] elapsedNanos = new long[measuredTicks];
        double[] frameFpsList = new double[measuredTicks];

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                WorldEditorPane worldEditor = new WorldEditorPane();
                worldEditor.setSimulationMode(true);
                worldEditor.setActive(true);

                Stage stage = new Stage();
                Scene scene = new Scene(worldEditor, 1280, 800);
                stage.setScene(scene);
                stage.show();
                stage.toBack(); // keep background during benchmark

                // Warmup
                for (int w = 0; w < warmupTicks; w++) {
                    sim.tick();
                    worldEditor.repaintAllViews();
                }

                // Benchmark execution on FX thread (Tick + 3D View Render)
                long startTotal = System.nanoTime();

                for (int t = 0; t < measuredTicks; t++) {
                    long stepStart = System.nanoTime();

                    sim.tick();
                    worldEditor.repaintAllViews();

                    long duration = System.nanoTime() - stepStart;
                    elapsedNanos[t] = duration;
                    frameFpsList[t] = 1_000_000_000.0 / Math.max(1, duration);
                }

                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(30, TimeUnit.SECONDS)) {
            System.err.println("WARNING: GUI Benchmark timed out waiting for FX Thread completion!");
        }

        double totalNanos = Arrays.stream(elapsedNanos).sum();
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
