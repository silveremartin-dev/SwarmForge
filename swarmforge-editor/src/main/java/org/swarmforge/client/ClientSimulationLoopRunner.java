/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.world.TerrainGenerator;

/**
 * Runner handling the background simulation loop and stop/end threshold checks.
 */
class ClientSimulationLoopRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ClientSimulationLoopRunner.class);

    static void startLocalSimulationLoop(SwarmForgeClient client) {
        if (client.simLoopActive) return;
        client.simLoopActive = true;
        client.simLoopExecutor.submit(() -> {
            long lastTime = System.nanoTime();
            while (client.simLoopActive) {
                boolean isConnected = client.networkClient != null && client.networkClient.isConnected();
                boolean isPlaying = client.simControlPanel != null && client.simControlPanel.isPlaying();

                if (isPlaying && !isConnected) {
                    if (client.localSimulation == null) {
                        if (client.lastGeneratedTerrarium == null) {
                            client.lastGeneratedTerrarium = new Terrarium(64, 64, 32);
                            TerrainGenerator gen = new TerrainGenerator(12345L);
                            gen.generate(client.lastGeneratedTerrarium, 16, 6.0f, 0.08f);
                        }
                        client.localSimulation = new Simulation(client.lastGeneratedTerrarium);
                        client.localSimulation.addColony("LasiusNiger");
                        final Simulation finalSim = client.localSimulation;
                        Platform.runLater(() -> {
                            if (client.simWorldViewer != null) {
                                Terrarium uiTerrarium = client.simWorldViewer.exportToTerrarium();
                                if (uiTerrarium != null) {
                                    client.lastGeneratedTerrarium = uiTerrarium;
                                    client.localSimulation = new Simulation(uiTerrarium);
                                    client.localSimulation.addColony("LasiusNiger");
                                }
                                client.simWorldViewer.setSimulation(client.localSimulation != null ? client.localSimulation : finalSim);
                                client.simWorldViewer.setSlicePlane(100.0);
                                if (client.sliceSlider != null) client.sliceSlider.setValue(100.0);
                            }
                        });
                    }
                    float speed = client.simControlPanel != null ? client.simControlPanel.getSpeedMultiplier() : 1.0f;
                    client.localSimulation.tick();

                    if (speed >= 100.0f) {
                        Thread.onSpinWait();
                        lastTime = System.nanoTime();
                    } else {
                        double targetFps = 60.0 * Math.max(0.1, Math.min(20.0, speed));
                        long targetNanos = (long) (1_000_000_000L / targetFps);

                        long elapsed = System.nanoTime() - lastTime;
                        lastTime = System.nanoTime();
                        long sleepNanos = targetNanos - elapsed;
                        if (sleepNanos > 0) {
                            try {
                                Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    if (client.localSimulation != null && client.simControlPanel != null) {
                        long curTick = client.localSimulation.getTickCount();
                        int totalPop = 0;
                        if (client.localSimulation.getColonies() != null) {
                            for (Colony col : client.localSimulation.getColonies()) {
                                totalPop += col.getPopulation();
                            }
                        }
                        int minPopStop = client.simControlPanel.getMinPopulationStopThreshold();
                        long maxTicks = client.simControlPanel.getMaxSimulationTicks();

                        if (curTick > 0 && totalPop <= minPopStop) {
                            int finalPop = totalPop;
                            Platform.runLater(() -> {
                                if (client.simControlPanel.isPlaying()) {
                                    client.simControlPanel.pauseSimulation();
                                    NotificationOverlay.show(
                                            client.simSubTabs != null ? client.simSubTabs : client.simControlPanel,
                                            "🛑 SIMULATION INTERROMPUE : La population totale est tombée à " + finalPop + " (seuil minimal d'arrêt : " + minPopStop + ").",
                                            NotificationOverlay.NotificationType.WARNING,
                                            true
                                    );
                                }
                            });
                        } else if (maxTicks > 0 && curTick >= maxTicks) {
                            long finalMaxTicks = maxTicks;
                            Platform.runLater(() -> {
                                if (client.simControlPanel.isPlaying()) {
                                    client.simControlPanel.pauseSimulation();
                                    NotificationOverlay.show(
                                            client.simSubTabs != null ? client.simSubTabs : client.simControlPanel,
                                            "⏱️ SIMULATION TERMINÉE : La durée maximale de " + finalMaxTicks + " pas de temps (ticks) a été atteinte.",
                                            NotificationOverlay.NotificationType.INFO,
                                            true
                                    );
                                }
                            });
                        }
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    lastTime = System.nanoTime();
                }
            }
        });
    }
}
