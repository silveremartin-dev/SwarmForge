/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.audio.SimulationAudioManager;
import org.swarmforge.client.network.ClientServerConnectionHandler;
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
import org.swarmforge.client.ui.SettingsPane;
import org.swarmforge.client.ui.SimulationControlPanel;
import org.swarmforge.client.ui.SpeciesEditorPane;
import org.swarmforge.client.ui.SplashScreen;
import org.swarmforge.client.ui.StatisticsDashboard;
import org.swarmforge.client.ui.WeatherEditorPane;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.IconUtils;
import org.swarmforge.client.util.SoundEffectManager;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.engine.EnginePreferences;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.DefaultSpecies;
import org.swarmforge.core.world.TerrainGenerator;

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
    final I18nManager i18n = I18nManager.getInstance();

    final ExecutorService simLoopExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "local-simulation-tick-loop");
        t.setDaemon(true);
        return t;
    });
    volatile boolean simLoopActive = false;
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    final ClientScenarioExecutionHelper scenarioHelper = new ClientScenarioExecutionHelper(this);
    final ClientTelemetryHudUpdater hudUpdater = new ClientTelemetryHudUpdater(this);
    final ClientViewportSidebarBuilder viewportBuilder = new ClientViewportSidebarBuilder(this);

    private static boolean clientOnlyMode = false;
    public static boolean isClientOnlyMode() { return clientOnlyMode; }

    public static void setClientOnlyMode(boolean clientOnly) {
        clientOnlyMode = clientOnly;
    }

    public static void silenceJme3Loggers() {
        IconUtils.silenceJme3Warnings();
    }

    static {
        silenceJme3Loggers();
    }

    @Override
    public void start(Stage primaryStage) {
        LOG.info(clientOnlyMode ? "Starting SwarmForge Dedicated Client..." : "Starting SwarmForge Editor...");

        IconUtils.applyWindowIcons(primaryStage);

        boolean isTestMode = Boolean.getBoolean("testfx.headless") || Boolean.getBoolean("headless")
                || "true".equals(System.getProperty("swarmforge.test")) || "true".equals(System.getProperty("testfx.robot"));

        SplashScreen splashScreen = null;
        if (!isTestMode) {
            if (clientOnlyMode) {
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

        if (clientOnlyMode) {
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

        if (clientOnlyMode) {
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
            if (clientOnlyMode) {
                ClientServerConnectionHandler.autoConnectStandalone(networkClient, simControlPanel);
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
        ClientServerConnectionHandler.promptAndStartLocalServer(
                targetHost, targetPort, networkClient, simControlPanel, gameView, onServerReady
        );
    }

    private Node createSimulationManager() {
        return ClientSimulationTabBuilder.buildSimulationManager(this);
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
        tt.getStyleClass().add("warning-tooltip");
        tt.setShowDelay(Duration.millis(100));
        return tt;
    }

    boolean isSim3DFocused() {
        if (clientOnlyMode) {
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
        return ClientWorldEditorTabBuilder.createWorldEditor(this);
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
