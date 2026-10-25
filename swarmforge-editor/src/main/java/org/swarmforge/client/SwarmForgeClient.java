/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.audio.SimulationAudioManager;
import org.swarmforge.client.network.LocalServerManager;
import org.swarmforge.client.network.SimulationClient;
import org.swarmforge.client.ui.AccessorySpeciesEditorPane;
import org.swarmforge.client.ui.EventLogPane;
import org.swarmforge.client.ui.GlossaryDialog;
import org.swarmforge.client.ui.GlossaryPaneView;
import org.swarmforge.client.ui.InterventionPanel;
import org.swarmforge.client.ui.MinimapOverlay;
import org.swarmforge.client.ui.MultiplayerScoreboardOverlay;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.ui.PheromoneOverlay;
import org.swarmforge.client.ui.ServerLauncherDialog;
import org.swarmforge.client.ui.SettingsPane;
import org.swarmforge.client.ui.SimulationControlPanel;
import org.swarmforge.client.ui.SpeciesEditorPane;
import org.swarmforge.client.ui.SplashScreen;
import org.swarmforge.client.ui.StatisticsDashboard;
import org.swarmforge.client.ui.WeatherEditorPane;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.IconUtils;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.SoundEffectManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.engine.EnginePreferences;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.DefaultSpecies;
import org.swarmforge.core.world.TerrainGenerator;
import org.swarmforge.server.ServerConfig;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * SwarmForge "Simulation Studio" Client.
 * Organized into main tabs: Simulation, World Editor, Species Editor, Accessory Species,
 * Weather Editor, Nest Generator, Settings, and Glossary.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SwarmForgeClient extends Application {

    private static final Logger LOG = Logger.getLogger(SwarmForgeClient.class.getName());

    GameViewPane gameView;
    DefaultSpecies currentSpecies = new DefaultSpecies();
    SimulationClient networkClient = new SimulationClient();
    Terrarium lastGeneratedTerrarium;
    Simulation localSimulation;
    MinimapOverlay minimapOverlay;
    PheromoneOverlay pheromoneOverlay;
    StatisticsDashboard statisticsDashboard;
    MultiplayerScoreboardOverlay scoreboardOverlay;
    EventLogPane eventLogPane;
    InterventionPanel interventionPanel;
    SimulationControlPanel simControlPanel;
    WorldEditorPane simWorldViewer;
    WorldEditorPane worldEditorPane;
    NestGeneratorPane nestPane;
    Slider sliceSlider;
    TabPane mainTabs;
    Tab simTab;
    Tab worldTab;
    Tab visualTab;
    Tab godTab;
    Tab statsTab;
    Tab eventLogTab;
    Tab glossaryTab;
    GlossaryPaneView glossaryPaneView;
    TabPane simSubTabs;
    HBox connectBox;
    VBox simulationInactiveOverlay;
    Label statsLabel;
    Label syncLabel;
    CheckBox chkAntTracking;
    CheckBox chkShowScoreboard;
    Runnable cancelAndResetVideoRecording;
    private final I18nManager i18n = I18nManager.getInstance();

    final ExecutorService simLoopExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "local-simulation-tick-loop");
        t.setDaemon(true);
        return t;
    });
    volatile boolean simLoopActive = false;
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private final ClientScenarioExecutionHelper scenarioHelper = new ClientScenarioExecutionHelper(this);
    private final ClientTelemetryHudUpdater hudUpdater = new ClientTelemetryHudUpdater(this);
    private final ClientViewportSidebarBuilder viewportBuilder = new ClientViewportSidebarBuilder(this);

    public static boolean isClientOnlyMode = false;

    public static void setClientOnlyMode(boolean clientOnly) {
        isClientOnlyMode = clientOnly;
    }

    public static void silenceJme3Loggers() {
        IconUtils.silenceJme3Warnings();
    }

    static {
        silenceJme3Loggers();
    }

    @Override
    public void start(Stage primaryStage) {
        LOG.info(isClientOnlyMode ? "Starting SwarmForge Dedicated Client..." : "Starting SwarmForge Editor...");

        IconUtils.applyWindowIcons(primaryStage);

        boolean isTestMode = Boolean.getBoolean("testfx.headless") || Boolean.getBoolean("headless")
                || "true".equals(System.getProperty("swarmforge.test")) || "true".equals(System.getProperty("testfx.robot"));

        SplashScreen splashScreen = null;
        if (!isTestMode) {
            if (isClientOnlyMode) {
                splashScreen = new SplashScreen(
                        null,
                        i18n.get("splash.title.client", "SwarmForge Client"),
                        i18n.get("splash.badge.client", "SIMULATION VIEWER"),
                        i18n.get("splash.client.subtitle", "3D Eusocial Simulation Viewer & God Mode Controller")
                );
            } else {
                splashScreen = new SplashScreen(
                        null,
                        i18n.get("splash.title.studio", "SwarmForge Studio"),
                        i18n.get("splash.badge.editor", "STUDIO & WORLD EDITOR"),
                        i18n.get("splash.subtitle", "Eusocial Insect Society Simulator (Ants, Termites, Wasps & Bees)")
                );
            }
            splashScreen.show();
        }

        if (isClientOnlyMode) {
            primaryStage.titleProperty().bind(i18n.createStringBinding("client.title"));
        } else {
            primaryStage.titleProperty().bind(i18n.createStringBinding("app.title"));
        }

        BorderPane root = new BorderPane();

        this.mainTabs = new TabPane();
        this.mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.simTab = new Tab();
        simTab.textProperty().bind(i18n.createStringBinding("tab.simulation"));
        simTab.setGraphic(new FontIcon(Feather.SLIDERS));
        simTab.setContent(createSimulationManager());

        Tab settingsTab = new Tab();
        settingsTab.textProperty().bind(i18n.createStringBinding("tab.settings"));
        settingsTab.setGraphic(new FontIcon(Feather.SETTINGS));
        settingsTab.setContent(new SettingsPane());

        this.glossaryPaneView = new GlossaryPaneView();
        this.glossaryTab = new Tab();
        this.glossaryTab.textProperty().bind(i18n.createStringBinding("tab.glossary"));
        this.glossaryTab.setGraphic(new FontIcon(Feather.BOOK_OPEN));
        this.glossaryTab.setContent(this.glossaryPaneView);

        if (isClientOnlyMode) {
            root.setCenter(createSimulationManager());
            primaryStage.setMaximized(true);
        } else {
            this.worldTab = new Tab();
            worldTab.textProperty().bind(i18n.createStringBinding("tab.world_editor"));
            worldTab.setGraphic(new FontIcon(Feather.GLOBE));
            worldTab.setContent(createWorldEditor());

            Tab speciesTab = new Tab();
            speciesTab.textProperty().bind(i18n.createStringBinding("tab.species_editor"));
            speciesTab.setGraphic(new FontIcon(Feather.CPU));
            SpeciesEditorPane speciesPane = new SpeciesEditorPane();
            speciesPane.setOnApply(species -> {
                this.currentSpecies = species;
                new Alert(Alert.AlertType.INFORMATION, "Espèce active mise à jour : " + species.getCommonName()).show();
            });
            speciesTab.setContent(speciesPane);

            Tab accessoryTab = new Tab();
            accessoryTab.textProperty().bind(i18n.createStringBinding("tab.accessory"));
            accessoryTab.setGraphic(new FontIcon(Feather.FEATHER));
            accessoryTab.setContent(new AccessorySpeciesEditorPane());

            Tab weatherTab = new Tab();
            weatherTab.textProperty().bind(i18n.createStringBinding("tab.weather"));
            weatherTab.setGraphic(new FontIcon(Feather.SUN));
            weatherTab.setContent(new WeatherEditorPane());

            Tab nestTab = new Tab();
            nestTab.textProperty().bind(i18n.createStringBinding("tab.nest"));
            nestTab.setGraphic(new FontIcon(Feather.HOME));
            this.nestPane = new NestGeneratorPane();
            nestPane.setOnApply(config -> {
                if (this.lastGeneratedTerrarium == null) {
                    new Alert(Alert.AlertType.WARNING, "No world generated.").show();
                    mainTabs.getSelectionModel().select(worldTab);
                    return;
                }
                generateNest(config);
                mainTabs.getSelectionModel().select(worldTab);
            });
            nestTab.setContent(nestPane);

            speciesPane.setOnGenerateNestForSpecies(species -> {
                this.currentSpecies = species;
                nestPane.configureFromSpecies(species);
                mainTabs.getSelectionModel().select(nestTab);
            });

            mainTabs.getTabs().addAll(simTab, worldTab, speciesTab, accessoryTab, weatherTab, nestTab, settingsTab, this.glossaryTab);

            final boolean[] isProgrammaticTabSwitch = { false };
            mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (isProgrammaticTabSwitch[0]) return;
                if (oldTab != null && oldTab.getContent() != null) {
                    Node content = oldTab.getContent();
                    boolean canLeave = true;
                    if (content instanceof SpeciesEditorPane speciesEditor) {
                        canLeave = speciesEditor.promptUnsavedChanges();
                    } else if (content instanceof WorldEditorPane worldEditor) {
                        canLeave = worldEditor.promptUnsavedChanges();
                    } else if (content instanceof WeatherEditorPane weatherEditor) {
                        canLeave = weatherEditor.promptUnsavedChanges();
                    } else if (content instanceof NestGeneratorPane nestEditor) {
                        canLeave = nestEditor.promptUnsavedChanges();
                    } else if (content instanceof AccessorySpeciesEditorPane accessoryEditor) {
                        canLeave = accessoryEditor.promptUnsavedChanges();
                    }

                    if (!canLeave) {
                        isProgrammaticTabSwitch[0] = true;
                        try {
                            mainTabs.getSelectionModel().select(oldTab);
                        } finally {
                            isProgrammaticTabSwitch[0] = false;
                        }
                    }
                }
                update3DRenderingState();
            });

            for (Tab t : mainTabs.getTabs()) {
                t.getStyleClass().add("custom-tab");
            }

            mainTabs.getSelectionModel().select(simTab);
            root.setCenter(mainTabs);
        }

        GlossaryDialog.setNavigationHandler(this::navigateToGlossaryTab);

        Scene scene = new Scene(root, 1280, 800);
        ThemeManager.getInstance().registerScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            try {
                this.stop();
            } catch (Throwable ignored) {
            }
            Platform.exit();
            if (!isTestMode) {
                try {
                    System.exit(0);
                } catch (Throwable ignored) {
                }
            }
        });

        if (isTestMode || splashScreen == null) {
            IconUtils.applyWindowIcons(primaryStage);
            primaryStage.show();
            Platform.runLater(() -> IconUtils.applyWindowIcons(primaryStage));
        } else {
            splashScreen.startProgressAndLaunch(() -> {
                IconUtils.applyWindowIcons(primaryStage);
                primaryStage.show();
                Platform.runLater(() -> IconUtils.applyWindowIcons(primaryStage));
            });
        }

        if (!isTestMode) {
            if (isClientOnlyMode) {
                Thread.ofVirtual().start(() -> {
                    boolean reachable = false;
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress("localhost", 50051), 350);
                        reachable = true;
                    } catch (Throwable ignored) {
                    }
                    if (reachable) {
                        try {
                            networkClient.connect("localhost", 50051);
                            networkClient.startStreaming();
                            LOG.info("Auto-connected to SwarmForge server at localhost:50051");
                            Platform.runLater(() -> {
                                if (simControlPanel != null) {
                                    simControlPanel.setServerConnectionStatus(true, "localhost:50051");
                                }
                            });
                        } catch (Throwable ex) {
                            Platform.runLater(() -> {
                                if (simControlPanel != null) {
                                    simControlPanel.setServerConnectionStatus(false, "Hors-ligne");
                                }
                            });
                        }
                    } else {
                        LOG.info("Standalone mode active (no SwarmForge server detected on localhost:50051)");
                        Platform.runLater(() -> {
                            if (simControlPanel != null) {
                                simControlPanel.setServerConnectionStatus(false, "Hors-ligne");
                            }
                        });
                    }
                });
            } else {
                Platform.runLater(() -> {
                    if (simControlPanel != null) {
                        simControlPanel.setServerConnectionStatus(false, "Hors-ligne");
                    }
                });
            }
        }
    }

    void promptAndStartLocalServer(String targetHost, int targetPort, Runnable onServerReady) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> promptAndStartLocalServer(targetHost, targetPort, onServerReady));
            return;
        }
        ServerLauncherDialog dialog = new ServerLauncherDialog(
                targetHost != null ? targetHost : "localhost",
                targetPort > 0 ? targetPort : 50051
        );
        Optional<ServerConfig> optConfig = dialog.showAndWait();
        optConfig.ifPresent(config -> {
            if (simControlPanel != null) {
                simControlPanel.setServerConnectionStatus(false, "Démarrage du serveur local...");
            }
            LocalServerManager.getInstance().startServer(
                    config,
                    () -> {
                        if (simControlPanel != null) {
                            simControlPanel.setServerHost("localhost");
                            simControlPanel.setServerPort(config.grpcPort());
                        }
                        try {
                            networkClient.connect("localhost", config.grpcPort());
                            networkClient.startStreaming();
                            if (simControlPanel != null) {
                                simControlPanel.setServerConnectionStatus(true, "localhost:" + config.grpcPort() + " (Serveur Local)");
                            }
                            if (gameView != null) {
                                gameView.getGameApp().setNetworkClient(networkClient);
                            }
                            if (onServerReady != null) {
                                onServerReady.run();
                            }
                        } catch (Exception ex) {
                            if (simControlPanel != null) {
                                simControlPanel.setServerConnectionStatus(false, "Erreur connexion: " + ex.getMessage());
                            }
                        }
                    },
                    err -> {
                        if (simControlPanel != null) {
                            simControlPanel.setServerConnectionStatus(false, "Erreur démarrage: " + err.getMessage());
                        }
                        ThemeManager.createAlert(
                                Alert.AlertType.ERROR,
                                "Échec du démarrage du serveur SwarmForge : " + err.getMessage()
                        ).showAndWait();
                    }
            );
        });
    }

    private Node createSimulationManager() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        HBox connectBox = new HBox(12);
        this.connectBox = connectBox;
        connectBox.setAlignment(Pos.CENTER_LEFT);
        connectBox.getStyleClass().add("header-banner");

        this.statsLabel = new Label();
        this.statsLabel.getStyleClass().add("stats-status-label");
        this.statsLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.stats.tt"));

        this.syncLabel = new Label();
        this.syncLabel.getStyleClass().add("sync-status-label");
        this.syncLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.sync.tt"));

        Region bannerSpacer = new Region();
        HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

        connectBox.getChildren().addAll(this.statsLabel, bannerSpacer, this.syncLabel);
        pane.setTop(connectBox);
        BorderPane.setMargin(connectBox, new Insets(0, 0, 10, 0));

        this.simSubTabs = new TabPane();
        this.simSubTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> update3DRenderingState());
        TabPane subTabs = this.simSubTabs;

        Tab controlsTab = new Tab();
        controlsTab.textProperty().bind(i18n.createStringBinding("sim.title"));
        controlsTab.setGraphic(new FontIcon(Feather.SLIDERS));
        VBox controlsInner = new VBox(12);
        controlsInner.setPadding(new Insets(10));

        this.simControlPanel = new SimulationControlPanel();
        this.simControlPanel.setWorldConfigSupplier(() -> (this.worldEditorPane != null) ? this.worldEditorPane.getConfiguration() : null);
        controlsInner.getChildren().add(this.simControlPanel);
        controlsTab.setContent(new ScrollPane(controlsInner));

        this.visualTab = new Tab();
        visualTab.textProperty().bind(i18n.createStringBinding("tab.visual_view"));
        visualTab.setGraphic(new FontIcon(Feather.EYE));
        visualTab.setContent(viewportBuilder.createVisualSimulationViewport());
        visualTab.setDisable(true);

        this.godTab = new Tab();
        godTab.textProperty().bind(i18n.createStringBinding("tab.god_mode"));
        godTab.setGraphic(new FontIcon(Feather.ZAP));
        this.interventionPanel = new InterventionPanel();
        this.interventionPanel.setCallback(new ClientInterventionBridge(this));
        godTab.setContent(this.interventionPanel);
        godTab.setDisable(true);

        this.statsTab = new Tab();
        statsTab.textProperty().bind(i18n.createStringBinding("tab.stats"));
        statsTab.setGraphic(new FontIcon(Feather.BAR_CHART_2));
        this.statisticsDashboard = new StatisticsDashboard();
        this.statisticsDashboard.setOnSelectIndividual(antId -> {
            if (this.simWorldViewer != null) {
                this.simWorldViewer.setFollowedIndividualById(antId);
            }
        });
        this.statisticsDashboard.setOnTrackIndividual(antId -> {
            if (this.simWorldViewer != null) {
                this.simWorldViewer.setFollowedIndividualById(antId);
                this.simWorldViewer.setFollowAntCameraEnabled(true);
                this.simWorldViewer.setAntTrackingEnabled(true);
                if (this.chkAntTracking != null) {
                    this.chkAntTracking.setSelected(true);
                }
                if (this.simWorldViewer.getTrackedIndividualPane() != null) {
                    this.simWorldViewer.getTrackedIndividualPane().setVisible(true);
                }
                if (this.simSubTabs != null && this.visualTab != null) {
                    this.simSubTabs.getSelectionModel().select(this.visualTab);
                }
            }
        });
        statsTab.setContent(this.statisticsDashboard);

        this.eventLogTab = new Tab();
        eventLogTab.textProperty().bind(i18n.createStringBinding("tab.log"));
        eventLogTab.setGraphic(new FontIcon(Feather.LIST));
        this.eventLogPane = new EventLogPane();
        if (this.simControlPanel != null) {
            this.eventLogPane.setStartDateTime(this.simControlPanel.getStartDateTime());
            this.eventLogPane.setSimulationStepSeconds(this.simControlPanel.getSimulationStepSeconds());
            this.simControlPanel.setOnStartDateTimeChange(newDt -> {
                if (this.eventLogPane != null) {
                    this.eventLogPane.setStartDateTime(newDt);
                }
            });
            this.simControlPanel.setOnStepChange(newStep -> {
                if (this.eventLogPane != null) {
                    this.eventLogPane.setSimulationStepSeconds(newStep);
                }
                if (this.localSimulation != null) {
                    this.localSimulation.setSimulationStepSeconds(newStep);
                }
            });
        }
        eventLogTab.setContent(this.eventLogPane);

        setSimTabsEnabled(false);

        scenarioHelper.wireSimulationControlCallbacks();

        subTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            update3DRenderingState();
            if (newTab == godTab) {
                if (simControlPanel != null && simControlPanel.isPlaying()) {
                    simControlPanel.pauseSimulation();
                    NotificationOverlay.show(
                            subTabs,
                            "⏸️ MODE DIVIN : SIMULATION MISE EN PAUSE AUTOMATIQUEMENT\nLa simulation a été mise en pause pour vous permettre d'agencer et programmer vos événements en toute sécurité.",
                            NotificationOverlay.NotificationType.WARNING
                    );
                }
                if (interventionPanel != null) {
                    interventionPanel.setSimulationRunning(false);
                    long currentTick = localSimulation != null ? localSimulation.getTickCount() : 0;
                    double stepSec = simControlPanel != null ? simControlPanel.getSimulationStepSeconds() : 0.0166f;
                    interventionPanel.syncCurrentSimulationTime(currentTick, stepSec);
                }
            }
        });

        Tab simSettingsTab = new Tab();
        simSettingsTab.textProperty().bind(i18n.createStringBinding("tab.settings"));
        simSettingsTab.setGraphic(new FontIcon(Feather.SETTINGS));
        simSettingsTab.setContent(new SettingsPane());

        this.glossaryPaneView = new GlossaryPaneView();
        this.glossaryTab = new Tab();
        this.glossaryTab.textProperty().bind(i18n.createStringBinding("tab.glossary"));
        this.glossaryTab.setGraphic(new FontIcon(Feather.BOOK_OPEN));
        this.glossaryTab.setContent(this.glossaryPaneView);

        subTabs.getTabs().addAll(controlsTab, visualTab, godTab, statsTab, eventLogTab, simSettingsTab, this.glossaryTab);
        pane.setCenter(subTabs);

        scenarioHelper.startLocalSimulationLoop();
        hudUpdater.startMainHudTimer();
        return pane;
    }

    void setSimTabsEnabled(boolean enabled) {
        if (visualTab != null) visualTab.setDisable(!enabled);
        if (godTab != null) godTab.setDisable(!enabled);
        if (statsTab != null) statsTab.setDisable(!enabled);
        if (eventLogTab != null) eventLogTab.setDisable(!enabled);

        String disabledMsg = i18n.get("tab.disabled.requires_simulation");
        if (disabledMsg == null || disabledMsg.startsWith("!")) {
            disabledMsg = "⚠ Veuillez d'abord appliquer et créer un scénario pour accéder à cet onglet.";
        }

        Tooltip disabledTooltip = !enabled ? createWarningTooltip(disabledMsg) : null;

        if (visualTab != null) visualTab.setTooltip(disabledTooltip);
        if (godTab != null) godTab.setTooltip(disabledTooltip);
        if (statsTab != null) statsTab.setTooltip(disabledTooltip);
        if (eventLogTab != null) eventLogTab.setTooltip(disabledTooltip);
    }

    private Tooltip createWarningTooltip(String text) {
        Tooltip tt = new Tooltip(text);
        tt.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-background-color: #1e293b; -fx-border-color: #f59e0b; -fx-border-radius: 4px;");
        tt.setShowDelay(Duration.millis(100));
        return tt;
    }

    boolean isSim3DFocused() {
        if (isClientOnlyMode) {
            return simSubTabs == null || simSubTabs.getSelectionModel().getSelectedItem() == visualTab;
        }
        boolean isSimTabSelected = (mainTabs == null || mainTabs.getSelectionModel().getSelectedItem() == simTab);
        boolean isVisualSubTabSelected = (simSubTabs == null || simSubTabs.getSelectionModel().getSelectedItem() == visualTab);
        return isSimTabSelected && isVisualSubTabSelected;
    }

    void update3DRenderingState() {
        boolean sim3DFocused = isSim3DFocused();

        if (simWorldViewer != null) {
            simWorldViewer.setActive(sim3DFocused);
        }

        boolean isWorldTabSelected = (mainTabs != null && mainTabs.getSelectionModel().getSelectedItem() == worldTab);
        if (worldEditorPane != null) {
            worldEditorPane.setActive(isWorldTabSelected);
        }

        boolean isSimRunning = (localSimulation != null && localSimulation.isRunning());
        boolean hasRiverInWorld = (lastGeneratedTerrarium != null && lastGeneratedTerrarium.hasRiver())
                || (localSimulation != null && localSimulation.getTerrarium() != null && (localSimulation.getTerrarium().hasRiver() || localSimulation.getTerrarium().hasWater()));

        SimulationAudioManager.getInstance().updateState(
                simControlPanel != null ? simControlPanel.getSelectedWorld() : "Forest",
                "Clear",
                "SUMMER",
                hasRiverInWorld,
                0,
                simWorldViewer != null ? simWorldViewer.getZoom() : 7.5,
                isSimRunning && sim3DFocused
        );
    }

    private Node createWorldEditor() {
        this.worldEditorPane = new WorldEditorPane();
        worldEditorPane.setSimulationMode(false);
        worldEditorPane.setOnGenerate(config -> {
            try {
                double sizeMeters = (double) config.getOrDefault("surfaceSizeMeters", 2.0);
                double resMm = (double) config.getOrDefault("resolutionMm", 0.5);
                double roughness = (double) config.getOrDefault("roughness", 0.45);

                int gridW = (int) (sizeMeters * 32);
                int gridH = (int) (sizeMeters * 32);
                int gridDepth = 32;

                Terrarium terrarium = new Terrarium(gridW, gridH, gridDepth);
                TerrainGenerator gen = new TerrainGenerator(System.currentTimeMillis());

                gen.generate(terrarium, gridDepth / 2, 10, (float) roughness * 0.1f);
                if (Boolean.TRUE.equals(config.get("hasRiver"))) {
                    gen.addWater(terrarium, gridDepth / 2 - 2, 0.4f);
                }

                this.lastGeneratedTerrarium = terrarium;
                if (gameView != null && gameView.getGameApp() != null) {
                    gameView.getGameApp().renderTerrarium(terrarium);
                }

                localSimulation = new Simulation(terrarium);
                localSimulation.setTicksPerSecond(20);
                hudUpdater.startLocalSimulationUpdates();

                ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Monde généré avec succès ! (Taille: " + sizeMeters + "m, Voxel: " + resMm + "mm)").show();
            } catch (Exception ex) {
                ThemeManager.createAlert(Alert.AlertType.ERROR, "Échec de génération: " + ex.getMessage()).show();
                ex.printStackTrace();
            }
        });

        return worldEditorPane;
    }

    private void generateNest(Map<String, Object> config) {
        scenarioHelper.generateNest(config);
    }

    public void navigateToGlossaryTab(String searchTerm) {
        if (this.glossaryTab != null && this.mainTabs != null) {
            this.mainTabs.getSelectionModel().select(this.glossaryTab);
            if (this.glossaryPaneView != null) {
                this.glossaryPaneView.searchAndSelect(searchTerm);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (!isShuttingDown.compareAndSet(false, true)) {
            return;
        }
        simLoopActive = false;
        try {
            simLoopExecutor.shutdownNow();
            simLoopExecutor.awaitTermination(300, TimeUnit.MILLISECONDS);
        } catch (Throwable ignored) {}
        try {
            if (localSimulation != null) {
                localSimulation.stop();
            }
        } catch (Throwable ignored) {}
        try {
            if (gameView != null) {
                gameView.stop();
            }
        } catch (Throwable ignored) {}
        try {
            if (networkClient != null) {
                networkClient.disconnect();
            }
        } catch (Throwable ignored) {}
        try {
            SimulationAudioManager.getInstance().stop();
        } catch (Throwable ignored) {}
        try {
            SoundEffectManager.getInstance().stopAmbience();
        } catch (Throwable ignored) {}
        try {
            super.stop();
        } catch (Throwable ignored) {}
    }

    public static void main(String[] args) {
        IconUtils.initEarlyTaskbarAppId();
        if (args != null) {
            EnginePreferences.applyCommandLineArgs(args);
            for (String arg : args) {
                if ("--client".equalsIgnoreCase(arg) || "--client-only".equalsIgnoreCase(arg) || "--viewer".equalsIgnoreCase(arg)) {
                    setClientOnlyMode(true);
                    break;
                }
            }
        }
        launch(args);
    }
}
