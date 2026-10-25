/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.swarmforge.client.network.LocalServerManager;
import org.swarmforge.client.ui.NestAlgorithm;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.ui.NestPresetManager;
import org.swarmforge.client.ui.ScenarioSetupSnapshot;
import org.swarmforge.client.ui.SpeciesConfigSnapshot;
import org.swarmforge.client.ui.WeatherPresetManager;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.SimulationCheckpoint;
import org.swarmforge.core.spatial.OptimalColonyPlacementEngine;
import org.swarmforge.core.world.NestGenerator;
import org.swarmforge.core.world.TerrainGenerator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Helper class managing scenario instantiation, colony spawning, nest generation,
 * checkpoint callbacks, server network discovery/connection, and the background
 * simulation tick loop for {@link SwarmForgeClient}.
 */
class ClientScenarioExecutionHelper {

    private static final Logger LOG = Logger.getLogger(ClientScenarioExecutionHelper.class.getName());
    private final SwarmForgeClient client;
    private final I18nManager i18n = I18nManager.getInstance();

    ClientScenarioExecutionHelper(SwarmForgeClient client) {
        this.client = client;
    }

    void wireSimulationControlCallbacks() {
        client.simControlPanel.setOnApplyPresets(this::handleApplyPresets);

        client.simControlPanel.setOnCreateCheckpoint(name -> {
            if (client.localSimulation != null) {
                SimulationCheckpoint cp = client.localSimulation.createCheckpoint(name);
                client.simControlPanel.updateCheckpoints(client.localSimulation.getCheckpoints());
                Alert alert = ThemeManager.createAlert(Alert.AlertType.INFORMATION,
                        "Point de contrôle '" + cp.getName() + "' enregistré à l'itération #" + cp.getTick() +
                        " (avec " + cp.getInterventionsRecorded().size() + " interventions Mode Divin).");
                alert.show();
            } else {
                Alert alert = ThemeManager.createAlert(Alert.AlertType.WARNING,
                        "Veuillez d'abord appliquer un scénario avant de créer un point de contrôle.");
                alert.show();
            }
        });

        client.simControlPanel.setOnRestoreCheckpoint(cp -> {
            if (client.localSimulation != null && cp != null) {
                boolean ok = client.localSimulation.restoreCheckpoint(cp);
                if (ok) {
                    if (client.minimapOverlay != null) {
                        client.minimapOverlay.update(client.localSimulation);
                    }
                    Alert alert = ThemeManager.createAlert(Alert.AlertType.INFORMATION,
                            "Point de contrôle '" + cp.getName() + "' restauré avec succès à l'itération #" + cp.getTick() + " !");
                    alert.show();
                    if (client.simSubTabs != null && client.visualTab != null) {
                        client.simSubTabs.getSelectionModel().select(client.visualTab);
                    }
                }
            }
        });

        client.simControlPanel.setOnServerConnect(() -> {
            String host = client.simControlPanel.getServerHost();
            int port = client.simControlPanel.getServerPort();
            boolean isOpen = LocalServerManager.isPortOpen(host, port);
            if (!isOpen) {
                String hLower = host != null ? host.toLowerCase().trim() : "";
                if (hLower.equals("localhost") || hLower.equals("127.0.0.1") || hLower.equals("0.0.0.0") || hLower.isEmpty()) {
                    client.promptAndStartLocalServer(host, port, null);
                    return;
                } else {
                    client.simControlPanel.setServerConnectionStatus(false, "Injoignable (" + host + ":" + port + ")");
                    ThemeManager.createAlert(
                        Alert.AlertType.WARNING,
                        "Impossible de joindre le serveur SwarmForge distant à " + host + ":" + port + ".\nVérifiez l'adresse IP et le port."
                    ).showAndWait();
                    return;
                }
            }

            try {
                client.networkClient.connect(host, port);
                client.networkClient.startStreaming();
                client.simControlPanel.setServerConnectionStatus(true, host + ":" + port);
                if (client.gameView != null) {
                    client.gameView.getGameApp().setNetworkClient(client.networkClient);
                }
            } catch (Exception ex) {
                client.simControlPanel.setServerConnectionStatus(false, "Erreur: " + ex.getMessage());
            }
        });

        client.simControlPanel.setOnServerStart(() ->
            client.promptAndStartLocalServer(client.simControlPanel.getServerHost(), client.simControlPanel.getServerPort(), null)
        );

        client.simControlPanel.setOnServerDisconnect(() -> {
            try {
                client.networkClient.disconnect();
                client.simControlPanel.setServerConnectionStatus(false, i18n.get("sim.server.sync.disconnected", "Déconnecté"));
            } catch (Exception ex) {
                client.simControlPanel.setServerConnectionStatus(false, i18n.get("sim.server.sync.disconnected", "Déconnecté"));
            }
        });

        client.simControlPanel.setOnServerDiscover(() -> {
            new Thread(() -> {
                Platform.runLater(() -> client.simControlPanel.setServerConnectionStatus(false, i18n.get("sim.server.status.detecting", "Détection en cours...")));
                try {
                    String host = client.simControlPanel.getServerHost();
                    int port = client.simControlPanel.getServerPort();
                    boolean open = LocalServerManager.isPortOpen(host, port);
                    if (!open && (!"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equals(host))) {
                        if (LocalServerManager.isPortOpen("localhost", 50051)) {
                            host = "localhost";
                            port = 50051;
                            open = true;
                        }
                    }
                    if (open) {
                        final String finalHost = host;
                        final int finalPort = port;
                        client.networkClient.connect(finalHost, finalPort);
                        client.networkClient.startStreaming();
                        Platform.runLater(() -> {
                            client.simControlPanel.setServerHost(finalHost);
                            client.simControlPanel.setServerPort(finalPort);
                            client.simControlPanel.setServerConnectionStatus(true, finalHost + ":" + finalPort);
                            if (client.gameView != null) {
                                client.gameView.getGameApp().setNetworkClient(client.networkClient);
                            }
                        });
                    } else {
                        Platform.runLater(() ->
                            client.simControlPanel.setServerConnectionStatus(false, i18n.get("sim.server.status.none_detected", "Aucun serveur actif détecté"))
                        );
                    }
                } catch (Exception e) {
                    Platform.runLater(() ->
                        client.simControlPanel.setServerConnectionStatus(false, i18n.get("sim.server.status.none_detected", "Aucun serveur actif détecté"))
                    );
                }
            }).start();
        });
    }

    void handleApplyPresets(Long seedObj) {
        long seed = seedObj != null ? seedObj : 12345L;
        try {
            if (client.cancelAndResetVideoRecording != null) {
                client.cancelAndResetVideoRecording.run();
            }
            if (client.localSimulation != null) {
                client.localSimulation.stop();
                client.localSimulation.recordSnapshot();
            }
            String scName = (client.simControlPanel != null) ? client.simControlPanel.getSelectedScenarioName() : "Scenario";
            if (client.eventLogPane != null) {
                client.eventLogPane.setScenarioName(scName);
                client.eventLogPane.clearLog();
            }
            if (client.statisticsDashboard != null) {
                client.statisticsDashboard.setScenarioName(scName);
                client.statisticsDashboard.clear();
            }
            if (client.interventionPanel != null) {
                client.interventionPanel.clearScheduledEvents();
            }
            if (client.simWorldViewer != null) {
                client.simWorldViewer.resetIndividualTracking();
            }
            Individual.resetIndividualNumberGenerator();

            if (client.simControlPanel != null && client.simControlPanel.isServerExecutionMode()) {
                String host = client.simControlPanel.getServerHost();
                int port = client.simControlPanel.getServerPort();
                System.out.println("[INFO] [SwarmForge Engine] Mode d'exécution Serveur SwarmForge (" + host + ":" + port + ") activé.");
                if (client.networkClient != null && !client.networkClient.isConnected()) {
                    boolean isOpen = LocalServerManager.isPortOpen(host, port);
                    if (!isOpen) {
                        String hLower = host != null ? host.toLowerCase().trim() : "";
                        if (hLower.equals("localhost") || hLower.equals("127.0.0.1") || hLower.equals("0.0.0.0") || hLower.isEmpty()) {
                            System.out.println("[INFO] Serveur local inactif, affichage du dialogue de configuration...");
                            client.promptAndStartLocalServer(host, port, null);
                        } else {
                            System.out.println("[WARN] Serveur distant non disponible à " + host + ":" + port + ", exécution en mode local fallback.");
                        }
                    } else {
                        try {
                            client.networkClient.connect(host, port);
                            client.networkClient.startStreaming();
                            if (client.simControlPanel != null) {
                                client.simControlPanel.setServerConnectionStatus(true, host + ":" + port);
                            }
                            if (client.gameView != null) {
                                client.gameView.getGameApp().setNetworkClient(client.networkClient);
                            }
                        } catch (Exception ex) {
                            System.out.println("[WARN] Échec de connexion au serveur à " + host + ":" + port + " (" + ex.getMessage() + ")");
                        }
                    }
                }
            } else {
                System.out.println("[INFO] [SwarmForge Engine] Création du terrarium et de la simulation locale in-process...");
            }

            if (client.lastGeneratedTerrarium == null) {
                if (client.simWorldViewer != null) {
                    client.lastGeneratedTerrarium = client.simWorldViewer.exportToTerrarium();
                } else {
                    client.lastGeneratedTerrarium = new Terrarium(64, 64, 32);
                    TerrainGenerator gen = new TerrainGenerator(seed);
                    gen.generate(client.lastGeneratedTerrarium, 16, 6.0f, 0.08f);
                }
            }
            if (client.interventionPanel != null && client.lastGeneratedTerrarium != null) {
                client.interventionPanel.setTerrainDimensions(
                        client.lastGeneratedTerrarium.getWidth(),
                        client.lastGeneratedTerrarium.getDepth(),
                        5.0, 25.0
                );
            }
            client.localSimulation = new Simulation(client.lastGeneratedTerrarium);
            client.localSimulation.reset(0);
            client.localSimulation.setMasterSeed(seed);
            if (client.simWorldViewer != null) {
                client.simWorldViewer.setSimulation(client.localSimulation);
            }

            if (client.simControlPanel != null && client.simControlPanel.isRealWeatherMode() && client.simControlPanel.getRealWeatherData() != null) {
                if (client.localSimulation.getWeather() != null) {
                    client.localSimulation.getWeather().applyClimateProfile(client.simControlPanel.getRealWeatherData());
                }
            } else {
                String selWeatherName = (client.simControlPanel != null) ? client.simControlPanel.getSelectedWeather() : "Temperate";
                WeatherPresetManager wPresetMgr = new WeatherPresetManager();
                Map<String, Object> wProfile = wPresetMgr.get(selWeatherName);
                if (wProfile == null) wProfile = wPresetMgr.get("Temperate");
                if (wProfile != null && client.localSimulation.getWeather() != null) {
                    client.localSimulation.getWeather().applyClimateProfile(wProfile);
                }
            }

            if (client.eventLogPane != null && client.simControlPanel != null) {
                client.eventLogPane.setStartDateTime(client.simControlPanel.getStartDateTime());
                client.eventLogPane.setSimulationStepSeconds(client.simControlPanel.getSimulationStepSeconds());
            }

            ScenarioSetupSnapshot setupSnap = client.simControlPanel.getLastSetupSnapshot();
            List<SpeciesConfigSnapshot> speciesSnapshots = (setupSnap != null) ? setupSnap.speciesSnapshots() : List.of();
            int totalCols = speciesSnapshots.isEmpty() ? 1 : speciesSnapshots.size();

            System.out.println("[INFO] [SwarmForge Engine] Instanciation de " + totalCols + " colonie(s)...");

            for (int colIdx = 0; colIdx < totalCols; colIdx++) {
                SpeciesConfigSnapshot card = (!speciesSnapshots.isEmpty()) ? speciesSnapshots.get(colIdx) : null;

                String selSpecies = (card != null) ? card.speciesName() : (setupSnap != null ? setupSnap.selectedSpecies() : "Lasius niger");
                int queens = (card != null) ? card.queenCount() : (setupSnap != null ? setupSnap.queenCount() : 1);
                int workers = (card != null) ? card.workerCount() : (setupSnap != null ? setupSnap.workerCount() : 500);
                int soldiers = (card != null) ? card.soldierCount() : (setupSnap != null ? setupSnap.soldierCount() : 50);
                int brood = (card != null) ? card.broodCount() : (setupSnap != null ? setupSnap.broodCount() : 20000);
                int initialFood = (card != null) ? card.initialFood() : 500;
                String rawNestType = (card != null) ? card.nestType() : (setupSnap != null ? setupSnap.selectedNestType() : "BURROW_UNDERGROUND");

                String speciesKey = resolveSpeciesKey(selSpecies);
                String placementStrategy = (card != null && card.placementStrategy() != null) ? card.placementStrategy() : "Optimal Multi-Territory Cluster";
                double customX = (card != null) ? card.customX() : 0.0;
                double customZ = (card != null) ? card.customZ() : 0.0;

                OptimalColonyPlacementEngine.PlacementResult pos;
                if (placementStrategy.contains("Manual") || placementStrategy.contains("Manuel")) {
                    int w = client.lastGeneratedTerrarium != null ? client.lastGeneratedTerrarium.getWidth() : 64;
                    int h = client.lastGeneratedTerrarium != null ? client.lastGeneratedTerrarium.getHeight() : 64;
                    float targetX = (float) Math.max(2, Math.min(w - 2, (w / 2.0) + customX));
                    float targetY = (float) Math.max(2, Math.min(h - 2, (h / 2.0) + customZ));
                    float surfZ = client.lastGeneratedTerrarium != null ? client.lastGeneratedTerrarium.getSurfaceElevation(targetX, targetY) : 0f;
                    pos = new OptimalColonyPlacementEngine.PlacementResult(targetX, targetY, surfZ, "Manual Placement");
                } else {
                    pos = OptimalColonyPlacementEngine.calculateOptimalPosition(
                            client.lastGeneratedTerrarium, speciesKey, colIdx, totalCols, placementStrategy
                    );
                }

                System.out.println("[INFO] [SwarmForge Engine]   -> Colonie #" + (colIdx + 1) + ": " + speciesKey +
                        " [X=" + String.format("%.1f", pos.x()) + ", Y=" + String.format("%.1f", pos.y()) + "] | Reines: " + queens +
                        ", Ouvrières: " + workers + ", Soldats: " + soldiers + ", Couvain: " + brood + ", Réserve Nourriture: " + initialFood);

                Colony colony = client.localSimulation.addColony(speciesKey, queens, 0, soldiers, pos.x(), pos.y());
                if (colony != null) {
                    colony.setMapBounds(client.lastGeneratedTerrarium.getWidth(), client.lastGeneratedTerrarium.getHeight());
                    float scaleFactor = resolveScaleFactor(rawNestType);

                    Map<String, Object> nestConfig = resolveNestConfig(rawNestType, scaleFactor);
                    NestGeneratorPane.GeneratedNest genNest = NestAlgorithm.generate(nestConfig, seed + colIdx);
                    NestAlgorithm.applyToTunnelNetwork(genNest, pos.x(), pos.y(), pos.z(), colony.getTunnelNetwork(), colony);

                    if (initialFood > 0) {
                        colony.setFoodStored(initialFood);
                    }
                    if (workers > 0) {
                        int createdWorkers = 0;
                        int batchSize = 50000;
                        while (createdWorkers < workers) {
                            if (client.simControlPanel != null && !client.simControlPanel.isCreatingScenario()) {
                                System.out.println("[INFO] [SwarmForge Engine] Instanciation interrompue par l'utilisateur.");
                                break;
                            }
                            int chunk = Math.min(batchSize, workers - createdWorkers);
                            colony.createWorkers(chunk);
                            createdWorkers += chunk;

                            double colProgress = (double) (colIdx * workers + createdWorkers) / (totalCols * Math.max(1, workers));
                            double progress = 0.85 + 0.14 * colProgress;
                            String status = String.format("Step 5/5 [%d%%]: Spawning %s (Colony #%d): %,d / %,d workers created...",
                                    (int) (progress * 100), speciesKey, colIdx + 1, createdWorkers, workers);
                            client.simControlPanel.updateScenarioCreationProgress(progress, status);

                            if (createdWorkers == chunk || createdWorkers >= workers || createdWorkers % 100000 == 0) {
                                EventBus.getInstance().publish(
                                    SimulationEvent.obtain(
                                        SimulationEvent.EventType.WORKER_BORN,
                                        SimulationEvent.Severity.INFO,
                                        0,
                                        status,
                                        null
                                    )
                                );
                            }
                        }
                    }
                    if (brood > 0) {
                        int createdBrood = 0;
                        int batchSize = 50000;
                        while (createdBrood < brood) {
                            if (client.simControlPanel != null && !client.simControlPanel.isCreatingScenario()) {
                                break;
                            }
                            int chunk = Math.min(batchSize, brood - createdBrood);
                            colony.createBrood(chunk);
                            createdBrood += chunk;
                        }
                    }
                }
            }

            System.gc();
            if (client.localSimulation != null) {
                client.localSimulation.recordInitialSnapshot();
            }
            Platform.runLater(() -> client.simControlPanel.resetTimelineTicks());

            Platform.runLater(() -> {
                try {
                    if (client.gameView != null && client.gameView.getGameApp() != null) {
                        if (client.localSimulation != null && client.localSimulation.getTerrarium() != null) {
                            client.gameView.getGameApp().renderTerrarium(client.localSimulation.getTerrarium());
                        }
                        client.gameView.getGameApp().setSimulation(client.localSimulation);
                    }

                    client.simControlPanel.updateCheckpoints(client.localSimulation.getCheckpoints());
                    if (client.interventionPanel != null && client.localSimulation != null) {
                        List<String> colonyNames = client.localSimulation.getColonies().stream()
                                .map(c -> (c.getSpeciesName() != null && !c.getSpeciesName().isEmpty()) ? c.getSpeciesName() : "Colonie #" + c.getId().toString().substring(0, 5))
                                .collect(Collectors.toList());
                        client.interventionPanel.updateAvailableColonies(colonyNames);
                    }

                    client.setSimTabsEnabled(true);
                    if (client.simSubTabs != null && client.visualTab != null) {
                        client.simSubTabs.getSelectionModel().select(client.visualTab);
                    }
                    if (client.mainTabs != null && client.simTab != null) {
                        client.mainTabs.getSelectionModel().select(client.simTab);
                    }
                } catch (Exception ex) {
                    LOG.severe("Error activating simulation sub-tabs: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            LOG.severe("Error creating simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String resolveSpeciesKey(String selSpecies) {
        if (selSpecies == null) return "LasiusNiger";
        if (selSpecies.contains("Atta") || selSpecies.contains("Coupeuse")) return "AttaCephalotes";
        if (selSpecies.contains("Solenopsis") || selSpecies.contains("Feu")) return "SolenopsisInvicta";
        if (selSpecies.contains("Formica") || selSpecies.contains("Rousse")) return "FormicaRufa";
        if (selSpecies.contains("Camponotus")) return "Camponotus";
        if (selSpecies.contains("Apis") || selSpecies.contains("Abeille")) return "ApisMellifera";
        if (selSpecies.contains("Termite") || selSpecies.contains("Macrotermes")) return "Macrotermes";
        return "LasiusNiger";
    }

    private float resolveScaleFactor(String rawNestType) {
        float scaleFactor = 1.2f;
        if (rawNestType != null) {
            String ntl = rawNestType.toLowerCase();
            if (ntl.contains("mound") || ntl.contains("dôme") || ntl.contains("solar") || ntl.contains("bivouac") || ntl.contains("army")) {
                scaleFactor = 1.6f;
            } else if (ntl.contains("tree") || ntl.contains("arbre") || ntl.contains("trunk") || ntl.contains("hollow") || ntl.contains("paper") || ntl.contains("guêpe") || ntl.contains("suspended") || ntl.contains("carton")) {
                scaleFactor = 1.4f;
            } else if (ntl.contains("fungi") || ntl.contains("champignon") || ntl.contains("fungal") || ntl.contains("vault")) {
                scaleFactor = 1.8f;
            } else if (ntl.contains("cathedral") || ntl.contains("cathédrale") || ntl.contains("termite")) {
                scaleFactor = 2.0f;
            } else if (ntl.contains("wax comb") || ntl.contains("beehive") || ntl.contains("hexagonal")) {
                scaleFactor = 1.5f;
            } else if (ntl.contains("pots") || ntl.contains("bourdon") || ntl.contains("propolis") || ntl.contains("silk") || ntl.contains("soie") || ntl.contains("leaf")) {
                scaleFactor = 1.3f;
            } else if (ntl.contains("jeune") || ntl.contains("young")) {
                scaleFactor = 0.8f;
            } else if (ntl.contains("supercolony") || ntl.contains("supercolonie")) {
                scaleFactor = 3.5f;
            } else if (ntl.contains("old") || ntl.contains("âgé")) {
                scaleFactor = 2.2f;
            }
            if (ntl.contains("jeune") || ntl.contains("young")) {
                scaleFactor *= 0.7f;
            } else if (ntl.contains("supercolony") || ntl.contains("supercolonie")) {
                scaleFactor *= 2.0f;
            }
        }
        return scaleFactor;
    }

    private Map<String, Object> resolveNestConfig(String rawNestType, float scaleFactor) {
        Map<String, Object> nestConfig = null;
        if (rawNestType != null) {
            String typeKey = rawNestType.trim();
            NestPresetManager presetMgr = new NestPresetManager();
            if (presetMgr.contains(typeKey)) {
                nestConfig = presetMgr.get(typeKey);
            } else {
                for (String pName : presetMgr.names()) {
                    if (pName.equalsIgnoreCase(typeKey) || pName.toLowerCase().contains(typeKey.toLowerCase())) {
                        nestConfig = presetMgr.get(pName);
                        break;
                    }
                }
            }
        }
        if (nestConfig == null) {
            nestConfig = new LinkedHashMap<>();
            nestConfig.put("architecture", rawNestType != null ? rawNestType : "BURROW_UNDERGROUND");
            nestConfig.put("depth", 25.0);
            nestConfig.put("workerSizeMm", 5.0 * scaleFactor);
        }
        return nestConfig;
    }

    void generateNest(Map<String, Object> config) {
        if (client.lastGeneratedTerrarium == null) return;
        try {
            NestGenerator generator = new NestGenerator(client.lastGeneratedTerrarium, System.currentTimeMillis());

            if (config.containsKey("depth"))
                generator.maxDepth((int) config.get("depth"));
            if (config.containsKey("tunnelWidth"))
                generator.tunnelRadius(((Number) config.get("tunnelWidth")).floatValue());
            if (config.containsKey("branching"))
                generator.branchingFactor(((Number) config.get("branching")).intValue());
            if (config.containsKey("chamberDistribution")) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> dist = (Map<String, Integer>) config.get("chamberDistribution");
                generator.setChamberCounts(dist);
            }

            long nestSeed = config.containsKey("seed") && config.get("seed") instanceof Number n ? n.longValue() : 123456L;
            NestGeneratorPane.GeneratedNest genNest = NestAlgorithm.generate(config, nestSeed);
            int chambers = genNest.nodes.size();

            if (client.localSimulation != null && !client.localSimulation.getColonies().isEmpty()) {
                for (Colony colony : client.localSimulation.getColonies()) {
                    if (colony.getTunnelNetwork() != null) {
                        NestAlgorithm.applyToTunnelNetwork(
                                genNest, colony.getNestX(), colony.getNestY(), colony.getNestZ(),
                                colony.getTunnelNetwork(), colony
                        );
                    }
                }
            }

            if (client.gameView != null) {
                client.gameView.getGameApp().renderTerrarium(client.lastGeneratedTerrarium);
            }
            if (client.worldEditorPane != null && client.localSimulation != null) {
                client.worldEditorPane.setSimulation(client.localSimulation);
                client.worldEditorPane.repaintAllViews();
            }

            ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Nest generated! (" + chambers + " chambers created)").show();
        } catch (Exception e) {
            ThemeManager.createAlert(Alert.AlertType.ERROR, "Generation failed: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }

    void startLocalSimulationLoop() {
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
