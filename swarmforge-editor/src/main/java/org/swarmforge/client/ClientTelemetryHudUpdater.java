/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.animation.AnimationTimer;
import org.swarmforge.client.audio.SimulationAudioManager;
import org.swarmforge.client.ui.MultiplayerScoreboardOverlay;
import org.swarmforge.client.ui.SimulationControlPanel;
import org.swarmforge.client.ui.StatisticsDashboard;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.world.WeatherSystem;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class managing the real-time HUD animation timer, audio synthesizer
 * synchronization, multiplayer scoreboard updates, statistics dashboard telemetry,
 * and local minimap/pheromone overlay updates for {@link SwarmForgeClient}.
 */
class ClientTelemetryHudUpdater {

    private final SwarmForgeClient client;
    private final I18nManager i18n = I18nManager.getInstance();

    ClientTelemetryHudUpdater(SwarmForgeClient client) {
        this.client = client;
    }

    void startMainHudTimer() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastSimRenderNano = 0L;
            private long lastCpuSampleNano = 0L;
            private long targetIntervalNano = 50_000_000L; // 50ms baseline = 20 FPS

            @Override
            public void handle(long now) {
                boolean isConnected = client.networkClient != null && client.networkClient.isConnected();
                boolean isPlaying = client.simControlPanel != null && client.simControlPanel.isPlaying();
                boolean isSimRunning = isPlaying;

                if (now - lastCpuSampleNano >= 500_000_000L) {
                    lastCpuSampleNano = now;
                    double cpuLoad = getSystemCpuLoad();
                    if (cpuLoad >= 0.0 && cpuLoad < 0.50) {
                        double freeFactor = Math.max(0.0, Math.min(1.0, (0.50 - cpuLoad) / 0.35));
                        double targetFps = 20.0 + freeFactor * 40.0;
                        targetIntervalNano = (long) (1_000_000_000L / targetFps);
                    } else {
                        targetIntervalNano = 50_000_000L;
                    }
                }

                if (isSimRunning && client.isSim3DFocused() && client.simWorldViewer != null) {
                    if (now - lastSimRenderNano >= targetIntervalNano) {
                        lastSimRenderNano = now;
                        client.simWorldViewer.repaintAllViews();
                    }
                }

                if (client.localSimulation != null) {
                    long curTick = client.localSimulation.getTickCount();
                    if (isPlaying && client.simControlPanel != null) {
                        long maxTicks = client.simControlPanel.getMaxSimulationTicks();
                        if (curTick >= maxTicks) {
                            client.simControlPanel.pauseSimulation();
                            if (client.localSimulation.isRunning()) {
                                client.localSimulation.pause();
                            }
                        }
                    }
                    if (isPlaying && !isConnected && client.simControlPanel != null) {
                        client.simControlPanel.updateTick(curTick, client.localSimulation.getHighestRecordedTick(), client.localSimulation.getElapsedSimulationSeconds());
                    }
                    if (client.simControlPanel != null && client.localSimulation.getCheckpoints().size() != client.simControlPanel.getCheckpointsCount()) {
                        client.simControlPanel.updateCheckpoints(client.localSimulation.getCheckpoints());
                    }
                    if (client.interventionPanel != null) {
                        client.interventionPanel.processScheduledEvents(curTick);
                    }
                }

                if (client.simulationInactiveOverlay != null) {
                    client.simulationInactiveOverlay.setVisible(!isSimRunning);
                }

                if (client.interventionPanel != null) {
                    client.interventionPanel.setSimulationRunning(isSimRunning);
                }

                if (client.localSimulation != null && !client.localSimulation.getColonies().isEmpty() && client.interventionPanel != null) {
                    List<String> colonyNames = new ArrayList<>();
                    for (Colony c : client.localSimulation.getColonies()) {
                        String speciesName = c.getSpeciesName();
                        colonyNames.add(speciesName != null && !speciesName.isEmpty() ? speciesName : "Colony #" + c.getId().toString().substring(0, 5));
                    }
                    client.interventionPanel.updateAvailableColonies(colonyNames);
                }

                long tick = isConnected ? client.networkClient.getLatestTick() : (client.localSimulation != null ? client.localSimulation.getTickCount() : 0);
                double stepDt = client.simControlPanel != null ? client.simControlPanel.getSimulationStepSeconds() : 0.05;
                String formattedTime = SimulationControlPanel.formatSimulationTime(tick, stepDt);

                boolean isServerMode = client.simControlPanel != null && client.simControlPanel.isServerExecutionMode();
                boolean isMatchmaking = isServerMode && client.simControlPanel.isServerJoinMode();

                if (isServerMode) {
                    String scName = client.simControlPanel.getSelectedScenarioName();
                    String roleLabel = isMatchmaking ? i18n.get("sim.server.role.join_short", "Rejoindre (Matchmaking)") : i18n.get("sim.server.role.host_short", "Hôte Déporté");
                    if (isConnected) {
                        client.statsLabel.setText(String.format("🌐 %s (%s:%d) | %s | %s | %s (Step #%d)",
                                i18n.get("sim.server.title_short", "Serveur SwarmForge"),
                                client.simControlPanel.getServerHost(), client.simControlPanel.getServerPort(), roleLabel, scName, formattedTime, tick));
                        if (client.syncLabel != null) {
                            client.syncLabel.setText("● " + i18n.get("sim.server.sync.connected", "Connecté au Serveur (gRPC Stream)"));
                            client.syncLabel.getStyleClass().removeAll("telemetry-sync-success", "telemetry-sync-warning", "telemetry-sync-info");
                            client.syncLabel.getStyleClass().add("telemetry-sync-success");
                        }
                        client.simControlPanel.setServerSessionStats(i18n.get("sim.server.online", "En Ligne (~15ms)"));
                    } else {
                        client.statsLabel.setText(String.format("🌐 %s (%s:%d - %s) | %s",
                                i18n.get("sim.server.mode_short", "Mode Serveur SwarmForge"),
                                client.simControlPanel.getServerHost(), client.simControlPanel.getServerPort(),
                                i18n.get("sim.server.offline", "Hors-Ligne"), formattedTime));
                        if (client.syncLabel != null) {
                            client.syncLabel.setText("○ " + i18n.get("sim.server.sync.disconnected", "Serveur Déconnecté"));
                            client.syncLabel.getStyleClass().removeAll("telemetry-sync-success", "telemetry-sync-warning", "telemetry-sync-info");
                            client.syncLabel.getStyleClass().add("telemetry-sync-warning");
                        }
                        client.simControlPanel.setServerSessionStats(i18n.get("sim.server.offline", "Hors-Ligne"));
                    }
                } else {
                    client.statsLabel.setText(String.format("💻 %s | %s (Step #%d)", i18n.get("engine.mode.local.short", "Moteur Local In-Process"), formattedTime, tick));
                    if (client.syncLabel != null) {
                        client.syncLabel.setText("● " + i18n.get("engine.mode.local.sync", "Moteur Local Autonome"));
                        client.syncLabel.getStyleClass().removeAll("telemetry-sync-success", "telemetry-sync-warning", "telemetry-sync-info");
                        client.syncLabel.getStyleClass().add("telemetry-sync-info");
                    }
                }

                updateScoreboardHud(isMatchmaking);
                updateAudioSynthesizer(isSimRunning);
                if (client.simWorldViewer != null && client.localSimulation != null && client.simWorldViewer.getActiveSimulation() != client.localSimulation) {
                    client.simWorldViewer.setSimulation(client.localSimulation);
                }
                updateStatisticsDashboard(tick, isSimRunning, isPlaying);
            }
        };
        timer.start();
    }

    private void updateScoreboardHud(boolean isMatchmaking) {
        List<MultiplayerScoreboardOverlay.ColonyEntry> entries = new ArrayList<>();
        String[] palette = new String[] { "#38bdf8", "#f87171", "#4ade80", "#fbbf24", "#c084fc", "#f472b6" };

        if (client.localSimulation != null && !client.localSimulation.getColonies().isEmpty()) {
            int idx = 0;
            for (Colony col : client.localSimulation.getColonies()) {
                int cPop = col.getPopulation();
                int cW = col.countByCaste(Individual.Caste.WORKER);
                int cS = col.countByCaste(Individual.Caste.SOLDIER);
                int cQ = col.countByCaste(Individual.Caste.QUEEN);
                float cFood = col.getFoodStored();
                boolean queenAlive = cQ > 0;
                String colName = col.getSpeciesName() != null && !col.getSpeciesName().isEmpty() ? col.getSpeciesName() : "Colonie #" + (idx + 1);
                String pName;
                if (idx == 0) {
                    pName = (client.simControlPanel != null) ? client.simControlPanel.getPlayerAlias() : i18n.get("sim.server.alias.default", "Participant_1");
                } else {
                    String assignedName = col.getParticipantName();
                    if (assignedName != null && !assignedName.trim().isEmpty()) {
                        pName = assignedName.trim();
                    } else {
                        pName = i18n.get("multiplayer.scoreboard.participant", "Participant") + " #" + (idx + 1);
                    }
                }
                String color = palette[idx % palette.length];

                entries.add(new MultiplayerScoreboardOverlay.ColonyEntry(
                        String.valueOf(idx + 1),
                        pName,
                        colName,
                        color,
                        cPop, cW, cS, cQ,
                        cFood,
                        queenAlive,
                        col.getNestX(), col.getNestY(), col.getNestZ(),
                        idx == 0
                ));
                idx++;
            }
        }

        boolean hasOtherParticipants = entries.size() > 1;
        boolean showScoreboardOption = isMatchmaking && hasOtherParticipants;

        if (client.chkShowScoreboard != null) {
            if (client.chkShowScoreboard.isVisible() != showScoreboardOption) {
                client.chkShowScoreboard.setVisible(showScoreboardOption);
                client.chkShowScoreboard.setManaged(showScoreboardOption);
            }
        }

        if (client.scoreboardOverlay != null) {
            if (!showScoreboardOption) {
                if (client.scoreboardOverlay.isVisible()) {
                    client.scoreboardOverlay.setVisible(false);
                    client.scoreboardOverlay.setManaged(false);
                }
            } else {
                boolean shouldShow = (client.chkShowScoreboard == null || client.chkShowScoreboard.isSelected());
                if (client.scoreboardOverlay.isVisible() != shouldShow) {
                    client.scoreboardOverlay.setVisible(shouldShow);
                    client.scoreboardOverlay.setManaged(shouldShow);
                }
                if (client.scoreboardOverlay.isVisible()) {
                    client.scoreboardOverlay.updateColonies(entries);
                }
            }
        }
    }

    private void updateAudioSynthesizer(boolean isSimRunning) {
        if (client.simControlPanel == null) return;
        String weatherStr = client.simControlPanel.getSelectedWeather();
        double windSpeed = 5.0;
        double rainRate = 0.0;
        int popCount = 100;
        boolean hasRiverInWorld = (client.lastGeneratedTerrarium != null) && client.lastGeneratedTerrarium.hasRiver();

        if (client.localSimulation != null) {
            WeatherSystem wSys = client.localSimulation.getWeather();
            if (wSys != null) {
                if (wSys.getWeatherState() != null) {
                    weatherStr = wSys.getWeatherState().name();
                }
                windSpeed = wSys.getWindSpeed();
                rainRate = wSys.getRainfall();
            }
            popCount = client.localSimulation.getColonies().stream().mapToInt(Colony::getPopulation).sum();
        }

        SimulationAudioManager.getInstance().setWindAndPrecipitation(windSpeed, rainRate);
        if (client.localSimulation != null && client.localSimulation.getWeather() != null) {
            WeatherSystem wSys = client.localSimulation.getWeather();
            SimulationAudioManager.getInstance().setEnvironmentPhysics(
                    wSys.getLatitude(),
                    wSys.getDayOfYear(),
                    wSys.getTimeOfDay(),
                    wSys.getLightLevel()
            );
        }
        double currentZoom = (client.simWorldViewer != null) ? client.simWorldViewer.getZoom() : 7.5;
        double currentDepth = (client.simWorldViewer != null && client.simWorldViewer.isSubterraneanExposed()) ? 1.0
                : (client.gameView != null && client.gameView.getGameApp() != null ? client.gameView.getGameApp().getCameraDepth() : 0.0);
        SimulationAudioManager.getInstance().setCameraDepth(currentDepth);
        SimulationAudioManager.getInstance().updateState(
                client.simControlPanel.getSelectedWorld(),
                weatherStr,
                "SUMMER",
                hasRiverInWorld,
                popCount,
                currentZoom,
                isSimRunning && client.isSim3DFocused()
        );
    }

    private void updateStatisticsDashboard(long tick, boolean isSimRunning, boolean isPlaying) {
        if (client.statisticsDashboard == null) return;
        StatisticsDashboard.ColonyStats stats = new StatisticsDashboard.ColonyStats();
        stats.simTicks = tick;
        stats.stepTimeSeconds = client.simControlPanel != null ? client.simControlPanel.getSimulationStepSeconds() : 0.05;
        int pop = 0;
        int workers = 0;
        int soldiers = 0;
        int queens = 0;
        int males = 0;
        float foodAmt = 0;
        float waterAmt = 0;
        int foraging = 0;
        int digging = 0;
        int nursing = 0;
        int guarding = 0;
        int royalCare = 0;
        int resting = 0;

        if (client.localSimulation != null && !client.localSimulation.getColonies().isEmpty()) {
            for (Colony col : client.localSimulation.getColonies()) {
                int cPop = col.getPopulation();
                int cW = col.countByCaste(Individual.Caste.WORKER);
                int cS = col.countByCaste(Individual.Caste.SOLDIER);
                int cQ = col.countByCaste(Individual.Caste.QUEEN);
                int cM = col.countByCaste(Individual.Caste.MALE);
                float cFood = col.getFoodStored();
                float cWater = col.getWaterStored();

                pop += cPop;
                workers += cW;
                soldiers += cS;
                queens += cQ;
                males += cM;
                foodAmt += cFood;
                waterAmt += cWater;

                for (Individual ind : col.getLivingIndividuals()) {
                    if (!ind.isAlive()) continue;
                    if (ind.getCaste() == Individual.Caste.QUEEN) {
                        royalCare++;
                    } else if (ind.getState() == Individual.AiState.FORAGE
                            || ind.getJob() == Individual.Job.FORAGER
                            || ind.isCarryingFood()) {
                        foraging++;
                    } else if (ind.getState() == Individual.AiState.DIG
                            || ind.getJob() == Individual.Job.BUILDER
                            || ind.getCarriedItem() == Individual.CarriedItem.EARTH) {
                        digging++;
                    } else if (ind.getState() == Individual.AiState.TEND_BROOD
                            || ind.getJob() == Individual.Job.NURSE
                            || ind.getCarriedItem() == Individual.CarriedItem.BROOD) {
                        nursing++;
                    } else if (ind.getState() == Individual.AiState.ATTACKING
                            || ind.getState() == Individual.AiState.PATROL
                            || ind.getJob() == Individual.Job.GUARD
                            || ind.getCaste() == Individual.Caste.SOLDIER) {
                        guarding++;
                    } else {
                        resting++;
                    }
                }

                Map<String, Integer> casteMap = new HashMap<>();
                casteMap.put("Reines", cQ);
                casteMap.put("Ouvrières", cW);
                casteMap.put("Soldats", cS);
                casteMap.put("Mâles", cM);
                casteMap.put("Total", cPop);

                String speciesName = col.getSpeciesName();
                String colName = (speciesName != null && !speciesName.isEmpty())
                        ? speciesName
                        : "Colonie #" + col.getId().toString().substring(0, 5);
                stats.colonyCasteCounts.put(colName, casteMap);
            }
        } else {
            pop = isSimRunning ? 171 : 0;
            workers = isSimRunning ? 150 : 0;
            soldiers = isSimRunning ? 20 : 0;
            queens = isSimRunning ? 1 : 0;
            males = 0;
            foodAmt = isSimRunning ? Math.max(0, 1000.0f - (tick * 0.5f)) : 0;
            waterAmt = isSimRunning ? 500.0f : 0;
        }

        stats.population = pop;
        stats.workers = workers;
        stats.soldiers = soldiers;
        stats.queens = queens;
        stats.males = males;
        stats.stateForaging = foraging;
        stats.stateDigging = digging;
        stats.stateNursing = nursing;
        stats.stateGuarding = guarding;
        stats.stateRoyalCare = royalCare;
        stats.stateResting = resting;
        stats.food = foodAmt;
        stats.water = waterAmt;
        if (client.localSimulation != null) {
            stats.tickRate = client.localSimulation.getActualTicksPerSecond();
            stats.targetTickRate = client.localSimulation.getTargetTicksPerSecond();
        } else {
            stats.tickRate = isPlaying ? (float) (1.0 / stats.stepTimeSeconds) : 0.0f;
            stats.targetTickRate = 60.0;
        }
        client.statisticsDashboard.update(stats);
        if (client.simWorldViewer != null && client.simWorldViewer.getFollowedIndividual() != null) {
            client.statisticsDashboard.updateIndividualTelemetryFromEntity(client.simWorldViewer.getFollowedIndividual());
        }
    }

    void startLocalSimulationUpdates() {
        if (client.localSimulation == null) return;

        AnimationTimer updateTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 100_000_000) return;
                lastUpdate = now;

                if (client.localSimulation == null) return;

                if (client.minimapOverlay != null) {
                    client.minimapOverlay.update(client.localSimulation);
                }

                if (client.pheromoneOverlay != null && client.localSimulation.getPheromoneGrid() != null) {
                    var grid = client.localSimulation.getPheromoneGrid();
                    var terrarium = client.localSimulation.getTerrarium();
                    int w = terrarium.getWidth();
                    int h = terrarium.getHeight();
                    float[][] pheromoneData = new float[w][h];

                    int z = terrarium.getDepth() / 2;
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            pheromoneData[x][y] = grid.read(x, y, z, 0);
                        }
                    }
                    client.pheromoneOverlay.updateData(pheromoneData, w, h);
                }

                boolean isPlaying = client.simControlPanel != null && client.simControlPanel.isPlaying();
                String weather = client.localSimulation.getWeather() != null ? client.localSimulation.getWeather().getCurrentWeatherType() : "Clear";
                String season = client.localSimulation.getSeasonManager() != null ? client.localSimulation.getSeasonManager().getCurrentSeason().name() : "SUMMER";
                boolean hasRiver = client.localSimulation.getTerrarium() != null && client.localSimulation.getTerrarium().hasWater();
                int popCount = client.localSimulation.getColonies().stream().mapToInt(c -> c.getLivingIndividuals().size()).sum();
                double zoom = client.simWorldViewer != null ? client.simWorldViewer.getZoom() : 7.5;
                double windSpd = client.localSimulation.getWeather() != null ? client.localSimulation.getWeather().getWindSpeed() : 5.0;
                double rainRate = client.localSimulation.getWeather() != null ? client.localSimulation.getWeather().getRainfallIntensity() : 0.0;

                SimulationAudioManager.getInstance().setWindAndPrecipitation(windSpd, rainRate);
                SimulationAudioManager.getInstance().updateState(
                        "Forest", weather, season, hasRiver, popCount, zoom, isPlaying && client.isSim3DFocused()
                );

                if (client.statisticsDashboard != null && !client.localSimulation.getColonies().isEmpty()) {
                    var colony = client.localSimulation.getColonies().get(0);
                    var stats = new StatisticsDashboard.ColonyStats();
                    stats.population = colony.getLivingIndividuals().size();
                    stats.queens = colony.countByCaste(Individual.Caste.QUEEN);
                    stats.workers = colony.countByCaste(Individual.Caste.WORKER);
                    stats.soldiers = colony.countByCaste(Individual.Caste.SOLDIER);
                    stats.food = colony.getFoodStored();
                    stats.water = colony.getWaterStored();
                    stats.tickRate = client.localSimulation.getActualTicksPerSecond();
                    stats.targetTickRate = client.localSimulation.getTargetTicksPerSecond();
                    stats.simTicks = client.localSimulation.getTickCount();
                    client.statisticsDashboard.update(stats);
                }
            }
        };
        updateTimer.start();
    }

    static double getSystemCpuLoad() {
        try {
            OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            if (bean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                double load = sunBean.getCpuLoad();
                if (load >= 0.0) return load;
                double procLoad = sunBean.getProcessCpuLoad();
                if (procLoad >= 0.0) return procLoad;
            }
        } catch (Throwable ignored) {
        }
        return -1.0;
    }
}
