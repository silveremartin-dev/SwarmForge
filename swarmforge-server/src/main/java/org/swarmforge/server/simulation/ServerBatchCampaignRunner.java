/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.server.SwarmForgeServer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Executes headless batch simulation campaigns for scientific experiments,
 * recording snapshots to disk and database.
 */
public class ServerBatchCampaignRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ServerBatchCampaignRunner.class);

    private final SwarmForgeServer server;

    public ServerBatchCampaignRunner(SwarmForgeServer server) {
        this.server = server;
    }

    public void runBatchCampaign(Scenario scenario, long targetTicks, String exportDir, int snapshotInterval) {
        LOG.info("==========================================================");
        LOG.info("   🐜 SWARMFORGE — HEADLESS BATCH SIMULATION CAMPAIGN     ");
        LOG.info("==========================================================");
        LOG.info("Scenario: {} ({})", scenario != null ? scenario.getTitle() : "Demo", scenario != null ? scenario.getId() : "demo");
        LOG.info("Target Ticks: {} | Snapshot Interval: {} | Export Dir: {}", targetTicks, snapshotInterval, exportDir);

        if (scenario != null) {
            server.loadAcademicScenario(scenario);
        } else {
            server.createDemoWorld();
        }

        Simulation sim = server.getSimulation();
        if (sim == null) {
            LOG.error("No active simulation to run in batch mode!");
            return;
        }

        File dir = new File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        long startNanos = System.nanoTime();
        long lastReportNanos = startNanos;
        long lastReportTick = 0;

        List<Map<String, Object>> timeSeriesData = new ArrayList<>();

        try {
            server.getDatabaseManager().connect();
        } catch (Exception ignored) {}

        LOG.info("Executing batch simulation ticks...");
        for (long t = 0; t < targetTicks; t++) {
            sim.tick();

            long currentTick = sim.getTickCount();
            if (currentTick % snapshotInterval == 0 || currentTick == targetTicks) {
                int totalPop = sim.getColonies().stream().mapToInt(Colony::getPopulation).sum();
                float totalFood = (float) sim.getColonies().stream().mapToDouble(Colony::getFoodStored).sum();
                float totalBiomass = (float) sim.getColonies().stream().mapToDouble(Colony::getTotalBiomass).sum();

                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("tick", currentTick);
                snapshot.put("simTimeSeconds", sim.getAccumulatedSimulationSeconds());
                snapshot.put("population", totalPop);
                snapshot.put("foodStored", totalFood);
                snapshot.put("totalBiomass", totalBiomass);
                snapshot.put("coloniesCount", sim.getColonies().size());
                timeSeriesData.add(snapshot);

                if (server.getDatabaseManager().isConnected()) {
                    try {
                        server.saveWorld("Batch Save (Tick " + currentTick + ")");
                    } catch (Exception ignored) {}
                }

                long now = System.nanoTime();
                if (now - lastReportNanos >= 2_000_000_000L || currentTick == targetTicks) {
                    double tps = (currentTick - lastReportTick) / ((now - lastReportNanos) / 1_000_000_000.0);
                    double progressPct = (currentTick * 100.0) / targetTicks;
                    LOG.info(String.format(Locale.US, "Progress: %.1f%% (%d/%d ticks) | Speed: %.1f TPS | Population: %d | Biomass: %.2f g",
                            progressPct, currentTick, targetTicks, tps, totalPop, totalBiomass));
                    lastReportNanos = now;
                    lastReportTick = currentTick;
                }
            }
        }

        long totalNanos = System.nanoTime() - startNanos;
        double totalSeconds = totalNanos / 1_000_000_000.0;
        double avgTps = targetTicks / Math.max(0.001, totalSeconds);

        LOG.info("==========================================================");
        LOG.info("   ✅ BATCH SIMULATION CAMPAIGN COMPLETED                 ");
        LOG.info("==========================================================");
        LOG.info(String.format(Locale.US, "Total Ticks: %d in %.2f s (Average Speed: %.1f TPS)", targetTicks, totalSeconds, avgTps));

        try {
            Map<String, Object> runReport = new LinkedHashMap<>();
            runReport.put("scenarioId", scenario != null ? scenario.getId() : "demo");
            runReport.put("scenarioTitle", scenario != null ? scenario.getTitle() : "Demo World");
            runReport.put("totalTicks", targetTicks);
            runReport.put("elapsedSeconds", totalSeconds);
            runReport.put("averageTPS", avgTps);
            runReport.put("finalPopulation", sim.getColonies().stream().mapToInt(Colony::getPopulation).sum());
            runReport.put("timeSeries", timeSeriesData);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            File reportFile = new File(dir, "batch_report.json");
            mapper.writeValue(reportFile, runReport);
            LOG.info("Batch report exported to: {}", reportFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Failed to write batch report JSON: " + e.getMessage(), e);
        }
    }
}
