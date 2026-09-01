/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.client.ui.MinimapOverlay;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.ui.PheromoneOverlay;
import org.swarmforge.client.ui.StatisticsDashboard;

import java.util.logging.Logger;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * SwarmForge "Simulation Studio" Client.
 * Organized into 3 main tabs: Simulation, World Editor, Species Editor.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SwarmForgeClient extends Application {

        private static final Logger LOG = Logger.getLogger(SwarmForgeClient.class.getName());

        private GameViewPane gameView; // 3D View
        private double lastX, lastY;
        private org.swarmforge.core.species.CustomSpecies currentSpecies = new org.swarmforge.core.species.CustomSpecies();
        private org.swarmforge.client.network.SimulationClient networkClient = new org.swarmforge.client.network.SimulationClient();
        private org.swarmforge.core.domain.Terrarium lastGeneratedTerrarium;
        private org.swarmforge.core.simulation.Simulation localSimulation; // For local preview
        private MinimapOverlay minimapOverlay;
        private PheromoneOverlay pheromoneOverlay;
        private StatisticsDashboard statisticsDashboard;
        private org.swarmforge.client.ui.EventLogPane eventLogPane;
        private org.swarmforge.client.ui.InterventionPanel interventionPanel;
        private org.swarmforge.client.ui.SimulationControlPanel simControlPanel;
        private org.swarmforge.client.ui.WorldEditorPane simWorldViewer;
        private org.swarmforge.client.ui.WorldEditorPane worldEditorPane;
        private TabPane mainTabs;
        private Tab simTab;
        private Tab worldTab;
        private Tab visualTab;
        private Tab godTab;
        private Tab statsTab;
        private Tab eventLogTab;
        private Tab glossaryTab;
        private TextField glossarySearchField;
        private TabPane glossaryCategoryTabPane;
        private TabPane simSubTabs;
        private HBox connectBox;
        private VBox simulationInactiveOverlay;
        private Label syncLabel;
        private Label statsLabel;
        private final I18nManager i18n = I18nManager.getInstance();

        private boolean isVideoRecording = false;
        private boolean isVideoArmed = false;
        private final java.util.List<java.awt.image.BufferedImage> recordedVideoFrames = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        private javafx.animation.Timeline videoCaptureTimeline;
        private long videoRecordingStartMs = 0;
        private Runnable stopVideoRecordingAndExport;
        private Runnable cancelAndResetVideoRecording;
        private Button btnRecVideo;

        private final java.util.concurrent.ExecutorService simLoopExecutor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "local-simulation-tick-loop");
                t.setDaemon(true);
                return t;
        });
        private volatile boolean simLoopActive = false;

        @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Editor...");

        // 1. Immediately bind window icons to primary stage for OS taskbar registration
        org.swarmforge.client.util.IconUtils.applyWindowIcons(primaryStage);

        // 2. Show Splash Screen on startup with progress bar, bound to primary stage owner
        org.swarmforge.client.ui.SplashScreen splashScreen = new org.swarmforge.client.ui.SplashScreen(primaryStage);
        splashScreen.show();

        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();

        // title binding
        primaryStage.titleProperty().bind(I18nManager.getInstance().createStringBinding("app.title"));

        // Root Layout
        BorderPane root = new BorderPane();

        // Menu bar removed per user request

        // 2. Main Tab Pane
        this.mainTabs = new TabPane();
        this.mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- TAB 1: SIMULATION MANAGER (Control, God Mode, Event Log) ---
        this.simTab = new Tab();
        simTab.textProperty().bind(i18n.createStringBinding("tab.simulation"));
        simTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SLIDERS));
        simTab.setContent(createSimulationManager());

        // --- TAB 2: WORLD EDITOR (3D View + Terrain Tools) ---
        this.worldTab = new Tab();
        worldTab.textProperty().bind(i18n.createStringBinding("tab.world_editor"));
        worldTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.GLOBE));
        worldTab.setContent(createWorldEditor());

        // --- TAB 3: SPECIES EDITOR ---
        Tab speciesTab = new Tab();
        speciesTab.textProperty().bind(i18n.createStringBinding("tab.species_editor"));
        speciesTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.CPU));
        org.swarmforge.client.ui.SpeciesEditorPane speciesPane = new org.swarmforge.client.ui.SpeciesEditorPane();
        speciesPane.setOnApply(species -> {
            this.currentSpecies = species;
            new Alert(Alert.AlertType.INFORMATION, "Espèce active mise à jour : " + species.getCommonName()).show();
        });
        speciesTab.setContent(speciesPane);

        // --- TAB 3b: ACCESSORY SPECIES EDITOR ---
        Tab accessoryTab = new Tab();
        accessoryTab.textProperty().bind(i18n.createStringBinding("tab.accessory"));
        accessoryTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.FEATHER));
        accessoryTab.setContent(new org.swarmforge.client.ui.AccessorySpeciesEditorPane());

        // --- TAB 4: WEATHER ---
        Tab weatherTab = new Tab();
        weatherTab.textProperty().bind(i18n.createStringBinding("tab.weather"));
        weatherTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SUN));
        weatherTab.setContent(new org.swarmforge.client.ui.WeatherEditorPane());

        // --- TAB 5: NEST GENERATOR ---
        Tab nestTab = new Tab();
        nestTab.textProperty().bind(i18n.createStringBinding("tab.nest"));
        nestTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.HOME));
        org.swarmforge.client.ui.NestGeneratorPane nestPane = new org.swarmforge.client.ui.NestGeneratorPane();
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

        // Wire direct species-to-nest generation pipeline
        speciesPane.setOnGenerateNestForSpecies(species -> {
            this.currentSpecies = species;
            nestPane.configureFromSpecies(species);
            mainTabs.getSelectionModel().select(nestTab);
        });

        // --- TAB 7: SETTINGS ---
        Tab settingsTab = new Tab();
        settingsTab.textProperty().bind(i18n.createStringBinding("tab.settings"));
        settingsTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SETTINGS));
        settingsTab.setContent(createSettingsPane());

        // --- TAB 8: GLOSSARY ---
        this.glossaryTab = new Tab();
        this.glossaryTab.textProperty().bind(i18n.createStringBinding("tab.glossary"));
        this.glossaryTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.BOOK_OPEN));
        this.glossaryTab.setContent(createGlossaryPaneView());

        mainTabs.getTabs().addAll(simTab, worldTab, speciesTab, accessoryTab, weatherTab, nestTab, settingsTab, this.glossaryTab);

        org.swarmforge.client.ui.GlossaryDialog.setNavigationHandler(this::navigateToGlossaryTab);

        final boolean[] isProgrammaticTabSwitch = { false };
        mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (isProgrammaticTabSwitch[0]) return;
            if (oldTab != null && oldTab.getContent() != null) {
                javafx.scene.Node content = oldTab.getContent();
                boolean canLeave = true;
                if (content instanceof org.swarmforge.client.ui.SpeciesEditorPane speciesEditor) {
                    canLeave = speciesEditor.promptUnsavedChanges();
                } else if (content instanceof org.swarmforge.client.ui.WorldEditorPane worldEditor) {
                    canLeave = worldEditor.promptUnsavedChanges();
                } else if (content instanceof org.swarmforge.client.ui.WeatherEditorPane weatherEditor) {
                    canLeave = weatherEditor.promptUnsavedChanges();
                } else if (content instanceof org.swarmforge.client.ui.NestGeneratorPane nestEditor) {
                    canLeave = nestEditor.promptUnsavedChanges();
                } else if (content instanceof org.swarmforge.client.ui.AccessorySpeciesEditorPane accessoryEditor) {
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

        // Style tab graphics
        for (Tab t : mainTabs.getTabs()) {
            t.getStyleClass().add("custom-tab");
        }

        // Select Simulation Tab by default on launch
        mainTabs.getSelectionModel().select(simTab);

        root.setCenter(mainTabs);

        // Scene Setup & Theme Registration
        Scene scene = new Scene(root, 1280, 800);
        org.swarmforge.client.util.ThemeManager.getInstance().registerScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Start loading progress on splash screen and reveal main window upon completion
        splashScreen.startProgressAndLaunch(() -> {
            primaryStage.show();
            primaryStage.toFront();
        });

        // Auto-connect to server at launch (localhost:50051)
        Platform.runLater(() -> {
            try {
                networkClient.connect("localhost", 50051);
                networkClient.startStreaming();
                LOG.info("Auto-connected to SwarmForge server at localhost:50051");
            } catch (Exception ex) {
                LOG.info("Standalone mode active (server auto-connect offline)");
            }
        });
    }


    private void showAboutDialog() {
        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "");
        alert.setTitle(i18n.get("dialog.about.title"));
        alert.setHeaderText(i18n.get("dialog.about.header"));
        alert.setContentText(i18n.get("dialog.about.content"));
        alert.show();
    }

        private Node createSimulationManager() {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                BorderPane pane = new BorderPane();
                pane.setPadding(new Insets(10));

                // 1. Connection Panel Header Banner
                HBox connectBox = new HBox(12);
                this.connectBox = connectBox;
                connectBox.setAlignment(Pos.CENTER_LEFT);
                connectBox.getStyleClass().add("header-banner");

                Label hostLabel = new Label();
                hostLabel.textProperty().bind(i18n.createStringBinding("label.host"));
                hostLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.host.tt"));
                TextField hostField = new TextField("localhost");
                hostField.setPrefWidth(110);
                hostField.tooltipProperty().bind(i18n.createTooltipBinding("label.host.input.tt"));

                Label portLabel = new Label();
                portLabel.textProperty().bind(i18n.createStringBinding("label.port"));
                portLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.port.tt"));
                TextField portField = new TextField("50051");
                portField.setPrefWidth(70);
                portField.tooltipProperty().bind(i18n.createTooltipBinding("label.port.input.tt"));

                Button btnConnect = new Button();
                btnConnect.textProperty().bind(i18n.createStringBinding("btn.connect"));
                btnConnect.getStyleClass().add("btn-primary");
                btnConnect.tooltipProperty().bind(i18n.createTooltipBinding("btn.connect.tt"));

                Label statusLabel = new Label();
                statusLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> networkClient.isConnected() ? i18n.get("label.status.connected") : i18n.get("label.status.offline"),
                    i18n.localeProperty()
                ));
                statusLabel.setStyle(networkClient.isConnected() ? "-fx-text-fill: #4ade80; -fx-font-weight: bold;" : "-fx-text-fill: #f87171;");
                statusLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.status.tt"));

                this.statsLabel = new Label();
                this.statsLabel.getStyleClass().add("stats-status-label");
                this.statsLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.stats.tt"));

                this.syncLabel = new Label();
                this.syncLabel.getStyleClass().add("sync-status-label");
                this.syncLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.sync.tt"));

                Region bannerSpacer = new Region();
                HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

                connectBox.getChildren().addAll(hostLabel, hostField, portLabel, portField, btnConnect, statusLabel, bannerSpacer, this.statsLabel, this.syncLabel);
                pane.setTop(connectBox);
                BorderPane.setMargin(connectBox, new Insets(0, 0, 10, 0));

                // 2. Content Area Sub-Tabs
                this.simSubTabs = new TabPane();
                this.simSubTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> update3DRenderingState());
                TabPane subTabs = this.simSubTabs;

                // --- Controls Tab ---
                Tab controlsTab = new Tab();
                controlsTab.textProperty().bind(i18n.createStringBinding("sim.title"));
                VBox controlsInner = new VBox(12);
                controlsInner.setPadding(new Insets(10));

                // Embedded SimulationControlPanel (Contains Preset Selectors ABOVE Start/Pause/Stop controls)
                this.simControlPanel = new org.swarmforge.client.ui.SimulationControlPanel();
                controlsInner.getChildren().add(this.simControlPanel);

                controlsTab.setContent(new ScrollPane(controlsInner));

                // --- Visual 3D View Sub-Tab ---
                this.visualTab = new Tab();
                Tab visualTab = this.visualTab;
                visualTab.textProperty().bind(i18n.createStringBinding("tab.visual_view"));
                visualTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.EYE));
                visualTab.setContent(createVisualSimulationViewport());
                visualTab.setDisable(true);

                // --- God Mode Sub-Tab ---
                this.godTab = new Tab();
                Tab godTab = this.godTab;
                godTab.textProperty().bind(i18n.createStringBinding("tab.god_mode"));
                godTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.ZAP));
                this.interventionPanel = new org.swarmforge.client.ui.InterventionPanel();
                org.swarmforge.client.ui.InterventionPanel interventionPanel = this.interventionPanel;
                interventionPanel.setCallback(new org.swarmforge.client.ui.InterventionPanel.InterventionCallback() {
                    @Override
                    public void spawnAnts(String colonyId, String caste, int count, float x, float y, float z) {
                        if (localSimulation != null) {
                            org.swarmforge.core.domain.Colony targetColony = null;
                            for (org.swarmforge.core.domain.Colony c : localSimulation.getColonies()) {
                                String name = c.getSpeciesName();
                                if (colonyId != null && (name.contains(colonyId) || colonyId.contains(name))) {
                                    targetColony = c;
                                    break;
                                }
                            }
                            if (targetColony == null && !localSimulation.getColonies().isEmpty()) {
                                targetColony = localSimulation.getColonies().get(0);
                            }
                            if (targetColony != null) {
                                for (int i = 0; i < count; i++) {
                                    if (caste != null && (caste.contains("Reine") || caste.contains("Queen"))) {
                                        targetColony.createQueen();
                                    } else if (caste != null && (caste.contains("Soldat") || caste.contains("Soldier"))) {
                                        targetColony.createSoldier();
                                    } else if (caste != null && (caste.contains("Mâle") || caste.contains("Male"))) {
                                        targetColony.createMale();
                                    } else {
                                        targetColony.createWorker();
                                    }
                                }
                            }
                        }
                        if (networkClient != null && networkClient.isConnected()) {
                            networkClient.spawnFood(x, y, z, count);
                        }
                    }

                    @Override
                    public void killAnts(String colonyId, String caste, int count) {
                        if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                            org.swarmforge.core.domain.Colony colony = localSimulation.getColonies().get(0);
                            int killed = 0;
                            for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                                if (killed >= count) break;
                                ind.die();
                                killed++;
                            }
                            colony.removeDeadIndividuals();
                        }
                    }

                    @Override
                    public void spawnFood(float x, float y, float z, float amount) {
                        if (localSimulation != null) {
                            localSimulation.spawnFood(x, y, z, amount, org.swarmforge.core.domain.ResourceType.SUGAR);
                        }
                        if (networkClient != null && networkClient.isConnected()) {
                            networkClient.spawnFood(x, y, z, amount);
                        }
                    }

                    @Override
                    public void triggerDisaster(String type, float intensity) {
                        if (networkClient != null && networkClient.isConnected()) {
                            networkClient.triggerDisaster(type, intensity);
                        }
                        if (localSimulation != null && lastGeneratedTerrarium != null) {
                            org.swarmforge.core.simulation.disasters.DisasterEvent event;
                            String upper = type != null ? type.toUpperCase() : "FIRE";
                            if (upper.contains("FIRE") || upper.contains("INCENDIE")) {
                                event = new org.swarmforge.core.simulation.disasters.FireDisaster(-1, -1, -1, intensity);
                                if (worldEditorPane != null) {
                                    worldEditorPane.triggerWildfire(32, 16, (int)(10 + intensity * 15));
                                }
                            } else if (upper.contains("FLOOD") || upper.contains("INONDATION")) {
                                event = new org.swarmforge.core.simulation.disasters.FloodDisaster(intensity, (int)(intensity * 50));
                                if (worldEditorPane != null) {
                                    worldEditorPane.triggerFlood(32, 16, (int)(12 + intensity * 15));
                                }
                            } else if (upper.contains("DROUGHT") || upper.contains("SECHERESSE")) {
                                event = new org.swarmforge.core.simulation.disasters.DroughtDisaster(intensity);
                                if (worldEditorPane != null) {
                                    worldEditorPane.triggerDrought();
                                }
                            } else if (upper.contains("HEAT") || upper.contains("CHALEUR")) {
                                event = new org.swarmforge.core.simulation.disasters.HeatwaveDisaster(intensity, (int)(intensity * 100));
                            } else {
                                event = new org.swarmforge.core.simulation.disasters.StormDisaster();
                            }
                            localSimulation.triggerDisaster(event);
                            if (simWorldViewer != null) {
                                simWorldViewer.repaintAllViews();
                            }
                        }
                    }

                    @Override
                    public void stopDisasters() {
                    }

                    @Override
                    public void modifyParameter(String param, Object value) {
                        if (localSimulation != null && value instanceof Number val) {
                            float fVal = val.floatValue();
                            if ("foodRate".equals(param)) {
                                localSimulation.setDiffusionInterval(Math.max(1, (int) (10 * fVal)));
                            } else if ("temperatureCelsius".equals(param) || "temperature".equals(param)) {
                                if (localSimulation.getWeather() != null) localSimulation.getWeather().setTemperature(fVal);
                            } else if ("humidityPercent".equals(param) || "humidity".equals(param)) {
                                if (localSimulation.getWeather() != null) localSimulation.getWeather().setHumidity(fVal);
                            } else if ("windSpeed".equals(param) || "wind".equals(param)) {
                                if (localSimulation.getWeather() != null) localSimulation.getWeather().setWindSpeed(fVal);
                            } else if ("solarRadiation".equals(param) || "solar".equals(param)) {
                                if (localSimulation.getWeather() != null) localSimulation.getWeather().setTemperatureOffset(fVal / 100.0f - 4.5f);
                            }
                        }
                    }

                    @Override
                    public void triggerInvasionEvent(String type, float x, float y, float z, int count, float durationMinutes) {
                        if (localSimulation != null && localSimulation.getPredatorManager() != null) {
                            boolean isSuppression = count == 0 || (type != null && (type.contains("Protection") || type.contains("Absence") || type.contains("Sanctuary")));
                            if (isSuppression) {
                                int durationTicks = (int) (durationMinutes * 60.0 * 60.0);
                                localSimulation.getPredatorManager().suppressPredators(durationTicks);
                            } else {
                                org.swarmforge.core.domain.PredatorType pType = org.swarmforge.core.domain.PredatorType.AARDVARK_MOUND_BREAKER;
                                if (type != null) {
                                    String u = type.toUpperCase();
                                    if (u.contains("ARMY") || u.contains("LEGION")) pType = org.swarmforge.core.domain.PredatorType.MEGAPONERA_RAIDER;
                                    else if (u.contains("MITE") || u.contains("ACARIEN")) pType = org.swarmforge.core.domain.PredatorType.VARROA_MITE;
                                    else if (u.contains("LOCUST") || u.contains("SAUTERELLE")) pType = org.swarmforge.core.domain.PredatorType.BEETLE;
                                    else if (u.contains("WASP") || u.contains("GUEPE")) pType = org.swarmforge.core.domain.PredatorType.WASP;
                                    else if (u.contains("SPIDER") || u.contains("ARAIGNEE")) pType = org.swarmforge.core.domain.PredatorType.SPIDER;
                                    else if (u.contains("MANTIS") || u.contains("MANTE")) pType = org.swarmforge.core.domain.PredatorType.BEETLE;
                                }
                                int spawnCount = Math.max(1, count);
                                for (int i = 0; i < spawnCount; i++) {
                                    localSimulation.getPredatorManager().spawnPredator(pType, x, y, z);
                                }
                            }
                        }
                    }

                    @Override
                    public void triggerPheromoneEvent(String type, float x, float y, float z, float intensity, float radius, float durationMinutes) {
                        if (localSimulation != null && localSimulation.getPheromoneGrid() != null) {
                            int index = org.swarmforge.core.domain.PheromoneType.HOME_TRAIL.getIndex();
                            if (type != null) {
                                String u = type.toUpperCase();
                                if (u.contains("ALARM") || u.contains("ALERTE")) index = org.swarmforge.core.domain.PheromoneType.ALARM.getIndex();
                                else if (u.contains("FOOD") || u.contains("ATTRACTANT") || u.contains("BIOMASS")) index = org.swarmforge.core.domain.PheromoneType.FOOD_TRAIL.getIndex();
                                else if (u.contains("QUEEN") || u.contains("REINE")) index = org.swarmforge.core.domain.PheromoneType.QUEEN_SCENT.getIndex();
                                else if (u.contains("RECRUIT") || u.contains("RECRUTEMENT")) index = org.swarmforge.core.domain.PheromoneType.RECRUITMENT.getIndex();
                                else if (u.contains("ERASER") || u.contains("DISPERSION")) {
                                    int r = Math.max(1, (int) radius);
                                    for (int dx = (int)(x - r); dx <= (int)(x + r); dx++) {
                                        for (int dy = (int)(y - r); dy <= (int)(y + r); dy++) {
                                            for (int dz = (int)(z - r); dz <= (int)(z + r); dz++) {
                                                localSimulation.getPheromoneGrid().clearAt(dx, dy, dz);
                                            }
                                        }
                                    }
                                    return;
                                }
                            }
                            int r = Math.max(1, (int) radius);
                            for (int dx = (int)(x - r); dx <= (int)(x + r); dx++) {
                                for (int dy = (int)(y - r); dy <= (int)(y + r); dy++) {
                                    double dist = Math.hypot(dx - x, dy - y);
                                    if (dist <= radius) {
                                        float conc = (float) (intensity * (1.0 - dist / (radius + 0.1)));
                                        localSimulation.getPheromoneGrid().deposit(dx, dy, (int)z, index, conc);
                                    }
                                }
                            }
                        }
                    }
                });
                godTab.setContent(interventionPanel);
                godTab.setDisable(true);

                // --- Dedicated Statistics Sub-Tab ---
                this.statsTab = new Tab();
                Tab statsTab = this.statsTab;
                statsTab.textProperty().bind(i18n.createStringBinding("tab.stats"));
                statsTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.BAR_CHART_2));
                this.statisticsDashboard = new org.swarmforge.client.ui.StatisticsDashboard();
                statsTab.setContent(this.statisticsDashboard);

                // --- Event Log Sub-Tab ---
                this.eventLogTab = new Tab();
                Tab eventLogTab = this.eventLogTab;
                eventLogTab.textProperty().bind(i18n.createStringBinding("tab.log"));
                eventLogTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.LIST));
                this.eventLogPane = new org.swarmforge.client.ui.EventLogPane();
                eventLogTab.setContent(this.eventLogPane);

                setSimTabsEnabled(false);

                // Set callback on Apply Presets
                this.simControlPanel.setOnApplyPresets(seed -> {
                    try {
                        if (this.cancelAndResetVideoRecording != null) {
                                this.cancelAndResetVideoRecording.run();
                        }
                        if (this.localSimulation != null) {
                                this.localSimulation.stop();
                                this.localSimulation.recordSnapshot();
                        }
                        String scName = (simControlPanel != null) ? simControlPanel.getSelectedScenarioName() : "Scenario";
                        if (this.eventLogPane != null) {
                                this.eventLogPane.setScenarioName(scName);
                                this.eventLogPane.clearLog();
                        }
                        if (this.statisticsDashboard != null) {
                                this.statisticsDashboard.setScenarioName(scName);
                                this.statisticsDashboard.clear();
                        }
                        if (this.interventionPanel != null) {
                                this.interventionPanel.clearScheduledEvents();
                        }

                        System.out.println("[INFO] [SwarmForge Engine] Création du terrarium et de la simulation locale...");
                        if (this.lastGeneratedTerrarium == null) {
                                this.lastGeneratedTerrarium = new org.swarmforge.core.domain.Terrarium(64, 32, 64);
                        }
                        if (this.interventionPanel != null && this.lastGeneratedTerrarium != null) {
                                this.interventionPanel.setTerrainDimensions(
                                        this.lastGeneratedTerrarium.getWidth(),
                                        this.lastGeneratedTerrarium.getDepth(),
                                        5.0, 25.0
                                );
                        }
                        this.localSimulation = new org.swarmforge.core.simulation.Simulation(this.lastGeneratedTerrarium);
                        this.localSimulation.reset(0);
                        this.localSimulation.setMasterSeed(seed);

                        String selWeatherName = (simControlPanel != null) ? simControlPanel.getSelectedWeather() : "Temperate";
                        org.swarmforge.client.ui.WeatherPresetManager wPresetMgr = new org.swarmforge.client.ui.WeatherPresetManager();
                        Map<String, Object> wProfile = wPresetMgr.get(selWeatherName);
                        if (wProfile == null) wProfile = wPresetMgr.get("Temperate");
                        if (wProfile != null && this.localSimulation.getWeather() != null) {
                            this.localSimulation.getWeather().applyClimateProfile(wProfile);
                        }

                        org.swarmforge.client.ui.ScenarioSetupSnapshot setupSnap = simControlPanel.getLastSetupSnapshot();
                        List<org.swarmforge.client.ui.SpeciesConfigSnapshot> speciesSnapshots = 
                            (setupSnap != null) ? setupSnap.speciesSnapshots() : List.of();

                        int totalCols = speciesSnapshots.isEmpty() ? 1 : speciesSnapshots.size();

                        System.out.println("[INFO] [SwarmForge Engine] Instanciation de " + totalCols + " colonie(s)...");

                        for (int colIdx = 0; colIdx < totalCols; colIdx++) {
                                org.swarmforge.client.ui.SpeciesConfigSnapshot card = 
                                    (!speciesSnapshots.isEmpty()) ? speciesSnapshots.get(colIdx) : null;

                                String selSpecies = (card != null) ? card.speciesName() : (setupSnap != null ? setupSnap.selectedSpecies() : "Lasius niger");
                                int queens = (card != null) ? card.queenCount() : (setupSnap != null ? setupSnap.queenCount() : 1);
                                int workers = (card != null) ? card.workerCount() : (setupSnap != null ? setupSnap.workerCount() : 500);
                                int soldiers = (card != null) ? card.soldierCount() : (setupSnap != null ? setupSnap.soldierCount() : 50);
                                int brood = (card != null) ? card.broodCount() : (setupSnap != null ? setupSnap.broodCount() : 20000);
                                int initialFood = (card != null) ? card.initialFood() : 500;
                                String rawNestType = (card != null) ? card.nestType() : (setupSnap != null ? setupSnap.selectedNestType() : "BURROW_UNDERGROUND");

                                String speciesKey = "LasiusNiger";
                                if (selSpecies != null) {
                                        if (selSpecies.contains("Atta") || selSpecies.contains("Coupeuse")) speciesKey = "AttaCephalotes";
                                        else if (selSpecies.contains("Solenopsis") || selSpecies.contains("Feu")) speciesKey = "SolenopsisInvicta";
                                        else if (selSpecies.contains("Formica") || selSpecies.contains("Rousse")) speciesKey = "FormicaRufa";
                                        else if (selSpecies.contains("Camponotus")) speciesKey = "Camponotus";
                                        else if (selSpecies.contains("Apis") || selSpecies.contains("Abeille")) speciesKey = "ApisMellifera";
                                        else if (selSpecies.contains("Termite") || selSpecies.contains("Macrotermes")) speciesKey = "Macrotermes";
                                }

                                String placementStrategy = (card != null && card.placementStrategy() != null) ? card.placementStrategy() : "Optimal Multi-Territory Cluster";
                                double customX = (card != null) ? card.customX() : 0.0;
                                double customZ = (card != null) ? card.customZ() : 0.0;

                                org.swarmforge.core.spatial.OptimalColonyPlacementEngine.PlacementResult pos;
                                if (placementStrategy.contains("Manual") || placementStrategy.contains("Manuel")) {
                                    int w = this.lastGeneratedTerrarium != null ? this.lastGeneratedTerrarium.getWidth() : 64;
                                    int h = this.lastGeneratedTerrarium != null ? this.lastGeneratedTerrarium.getHeight() : 64;
                                    float targetX = (float) Math.max(2, Math.min(w - 2, (w / 2.0) + customX));
                                    float targetY = (float) Math.max(2, Math.min(h - 2, (h / 2.0) + customZ));
                                    float surfZ = this.lastGeneratedTerrarium != null ? this.lastGeneratedTerrarium.getSurfaceElevation(targetX, targetY) : 0f;
                                    pos = new org.swarmforge.core.spatial.OptimalColonyPlacementEngine.PlacementResult(targetX, targetY, surfZ, "Manual Placement");
                                } else {
                                    pos = org.swarmforge.core.spatial.OptimalColonyPlacementEngine.calculateOptimalPosition(
                                            this.lastGeneratedTerrarium, speciesKey, colIdx, totalCols, placementStrategy
                                    );
                                }

                                System.out.println("[INFO] [SwarmForge Engine]   -> Colonie #" + (colIdx + 1) + ": " + speciesKey +
                                        " [X=" + String.format("%.1f", pos.x()) + ", Y=" + String.format("%.1f", pos.y()) + "] | Reines: " + queens +
                                        ", Ouvrières: " + workers + ", Soldats: " + soldiers + ", Couvain: " + brood + ", Réserve Nourriture: " + initialFood);

                                org.swarmforge.core.domain.Colony colony = this.localSimulation.addColony(speciesKey, queens, 0, soldiers, pos.x(), pos.y());
                                if (colony != null) {
                                        colony.setMapBounds(this.lastGeneratedTerrarium.getWidth(), this.lastGeneratedTerrarium.getHeight());

                                        org.swarmforge.core.world.NestGenerator.NestType genType = org.swarmforge.core.world.NestGenerator.NestType.MATURE;
                                        float scaleFactor = 1.2f;

                                        if (rawNestType != null) {
                                                String ntl = rawNestType.toLowerCase();
                                                if (ntl.contains("mound") || ntl.contains("dôme") || ntl.contains("solar")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.MOUND;
                                                        scaleFactor = 1.6f;
                                                } else if (ntl.contains("tree") || ntl.contains("arbre") || ntl.contains("trunk") || ntl.contains("hollow")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.TREE;
                                                        scaleFactor = 1.4f;
                                                } else if (ntl.contains("fungi") || ntl.contains("champignon") || ntl.contains("fungal") || ntl.contains("vault")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.SUBTERRANEAN_FUNGI_VAULT;
                                                        scaleFactor = 1.8f;
                                                } else if (ntl.contains("cathedral") || ntl.contains("cathédrale") || ntl.contains("termite")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.CATHEDRAL_MOUND;
                                                        scaleFactor = 2.0f;
                                                } else if (ntl.contains("wax comb") || ntl.contains("beehive") || ntl.contains("hexagonal")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.WAX_COMB_HEXAGONAL;
                                                        scaleFactor = 1.5f;
                                                } else if (ntl.contains("pots") || ntl.contains("bourdon") || ntl.contains("propolis")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.WAX_POTS_CLUSTER;
                                                        scaleFactor = 1.3f;
                                                } else if (ntl.contains("paper") || ntl.contains("guêpe") || ntl.contains("suspended")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.PAPER_PEDUNCULATE;
                                                        scaleFactor = 1.4f;
                                                } else if (ntl.contains("silk") || ntl.contains("soie") || ntl.contains("leaf")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.ARBOREAL_SILK_LEAF;
                                                        scaleFactor = 1.3f;
                                                } else if (ntl.contains("carton")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.CARTON_NEST;
                                                        scaleFactor = 1.4f;
                                                } else if (ntl.contains("bamboo") || ntl.contains("stem") || ntl.contains("gall")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.BAMBOO_STEM_NEST;
                                                        scaleFactor = 1.2f;
                                                } else if (ntl.contains("bivouac") || ntl.contains("army")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.BIVOUAC_LIVING_NEST;
                                                        scaleFactor = 1.6f;
                                                } else if (ntl.contains("jeune") || ntl.contains("young")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.SIMPLE;
                                                        scaleFactor = 0.8f;
                                                } else if (ntl.contains("supercolony") || ntl.contains("supercolonie")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.MATURE;
                                                        scaleFactor = 3.5f;
                                                } else if (ntl.contains("old") || ntl.contains("âgé")) {
                                                        genType = org.swarmforge.core.world.NestGenerator.NestType.MATURE;
                                                        scaleFactor = 2.2f;
                                                }

                                                // Stage scale modifications
                                                if (ntl.contains("jeune") || ntl.contains("young")) {
                                                        scaleFactor *= 0.7f;
                                                } else if (ntl.contains("supercolony") || ntl.contains("supercolonie")) {
                                                        scaleFactor *= 2.0f;
                                                }
                                        }

                                        org.swarmforge.core.world.NestGenerator nestGen = new org.swarmforge.core.world.NestGenerator(this.lastGeneratedTerrarium, seed + colIdx);
                                        nestGen.generate((int) pos.x(), (int) pos.y(), (int) pos.z(), genType, scaleFactor);

                                        colony.getTunnelNetwork().rebuildForArchitecture(pos.x(), pos.y(), pos.z(), rawNestType != null ? rawNestType.toString() : "BURROW_UNDERGROUND", colony);

                                        if (initialFood > 0) {
                                                colony.setFoodStored(initialFood);
                                        }
                                        if (workers > 0) {
                                                int createdWorkers = 0;
                                                int batchSize = 50000;
                                                while (createdWorkers < workers) {
                                                        if (simControlPanel != null && !simControlPanel.isCreatingScenario()) {
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
                                                        simControlPanel.updateScenarioCreationProgress(progress, status);

                                                        // Publish event selectively at start, completion, or 100k intervals to conserve memory
                                                        if (createdWorkers == chunk || createdWorkers >= workers || createdWorkers % 100000 == 0) {
                                                            org.swarmforge.core.event.EventBus.getInstance().publish(
                                                                org.swarmforge.core.event.SimulationEvent.obtain(
                                                                    org.swarmforge.core.event.SimulationEvent.EventType.WORKER_BORN,
                                                                    org.swarmforge.core.event.SimulationEvent.Severity.INFO,
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
                                                        if (simControlPanel != null && !simControlPanel.isCreatingScenario()) {
                                                            break;
                                                        }
                                                        int chunk = Math.min(batchSize, brood - createdBrood);
                                                        colony.createBrood(chunk);
                                                        createdBrood += chunk;
                                                }
                                        }
                                }
                        }

                        System.gc(); // Trigger garbage collection after population phase
                        if (this.localSimulation != null) {
                            this.localSimulation.recordInitialSnapshot();
                        }
                        // resetTimelineTicks() mutates JavaFX labels — must run on FX thread
                        javafx.application.Platform.runLater(() -> this.simControlPanel.resetTimelineTicks());

                        javafx.application.Platform.runLater(() -> {
                                try {
                                        if (gameView != null && gameView.getGameApp() != null) {
                                                gameView.getGameApp().setSimulation(this.localSimulation);
                                        }

                                        this.simControlPanel.updateCheckpoints(this.localSimulation.getCheckpoints());
                                        if (this.interventionPanel != null && this.localSimulation != null) {
                                                List<String> colonyNames = this.localSimulation.getColonies().stream()
                                                        .map(c -> (c.getSpeciesName() != null && !c.getSpeciesName().isEmpty()) ? c.getSpeciesName() : "Colonie #" + c.getId().toString().substring(0, 5))
                                                        .collect(java.util.stream.Collectors.toList());
                                                this.interventionPanel.updateAvailableColonies(colonyNames);
                                        }

                                        // Activer les onglets de dépendance et basculer sur la vue 3D
                                        setSimTabsEnabled(true);
                                        if (simSubTabs != null && visualTab != null) {
                                                simSubTabs.getSelectionModel().select(visualTab);
                                        }
                                        if (mainTabs != null && simTab != null) {
                                                mainTabs.getSelectionModel().select(simTab);
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
                });

                this.simControlPanel.setOnCreateCheckpoint(name -> {
                    if (this.localSimulation != null) {
                        org.swarmforge.core.simulation.SimulationCheckpoint cp = this.localSimulation.createCheckpoint(name);
                        this.simControlPanel.updateCheckpoints(this.localSimulation.getCheckpoints());
                        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Point de contrôle '" + cp.getName() + "' enregistré à l'itération #" + cp.getTick() + " (avec " + cp.getInterventionsRecorded().size() + " interventions Mode Divin).");
                        alert.show();
                    } else {
                        Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.WARNING, "Veuillez d'abord appliquer un scénario avant de créer un point de contrôle.");
                        alert.show();
                    }
                });

                this.simControlPanel.setOnRestoreCheckpoint(cp -> {
                    if (this.localSimulation != null && cp != null) {
                        boolean ok = this.localSimulation.restoreCheckpoint(cp);
                        if (ok) {
                            if (this.minimapOverlay != null) {
                                this.minimapOverlay.update(this.localSimulation);
                            }
                            Alert alert = org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Point de contrôle '" + cp.getName() + "' restauré avec succès à l'itération #" + cp.getTick() + " !");
                            alert.show();
                            subTabs.getSelectionModel().select(visualTab);
                        }
                    }
                });

                this.simControlPanel.setOnPlay(v -> {
                    if (this.localSimulation != null) {
                        this.localSimulation.start();
                    }
                });

                this.simControlPanel.setOnPause(v -> {
                    if (this.localSimulation != null) {
                        this.localSimulation.pause();
                    }
                });

                this.simControlPanel.setOnSeek(targetTick -> {
                    if (this.localSimulation != null) {
                        boolean ok = this.localSimulation.seekToTick(targetTick);
                        if (ok) {
                            this.simControlPanel.updateTick(this.localSimulation.getTickCount(), this.localSimulation.getHighestRecordedTick());
                        }
                    }
                });

                this.simControlPanel.setOnRewind(steps -> {
                    if (this.localSimulation != null) {
                        this.localSimulation.pause();
                        boolean ok = this.localSimulation.rewind(steps);
                        if (ok) {
                            this.simControlPanel.updateTick(this.localSimulation.getTickCount(), this.localSimulation.getHighestRecordedTick());
                        }
                    }
                });

                this.simControlPanel.setOnStepForward(v -> {
                    if (this.localSimulation != null && !this.localSimulation.isRunning()) {
                        this.localSimulation.tick();
                        this.simControlPanel.updateTick(this.localSimulation.getTickCount(), this.localSimulation.getHighestRecordedTick());
                    }
                });

                subTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                    update3DRenderingState();
                    if (newTab == godTab) {
                        if (simControlPanel != null && simControlPanel.isPlaying()) {
                            simControlPanel.pauseSimulation();
                            org.swarmforge.client.util.NotificationOverlay.show(
                                subTabs,
                                "⏸️ MODE DIVIN : SIMULATION MISE EN PAUSE AUTOMATIQUEMENT\nLa simulation a été mise en pause pour vous permettre d'agencer et programmer vos événements en toute sécurité.",
                                org.swarmforge.client.util.NotificationOverlay.NotificationType.WARNING
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

                subTabs.getTabs().addAll(controlsTab, visualTab, godTab, statsTab, eventLogTab);
                pane.setCenter(subTabs);

                // Connection Logic
                btnConnect.setOnAction(e -> {
                        try {
                                networkClient.connect(hostField.getText(), Integer.parseInt(portField.getText()));
                                statusLabel.setText(i18n.get("label.status.connected"));
                                statusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                                networkClient.startStreaming();

                                if (gameView != null) {
                                        gameView.getGameApp().setNetworkClient(networkClient);
                                }
                        } catch (Exception ex) {
                                statusLabel.setText(i18n.get("label.status.offline") + ": " + ex.getMessage());
                                statusLabel.setStyle("-fx-text-fill: #f87171;");
                                ex.printStackTrace();
                        }
                });

                startLocalSimulationLoop();

                // HUD Loop, Audio Sync, God Mode & Statistics Updates
                javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
                        @Override
                        public void handle(long now) {
                                boolean isConnected = networkClient != null && networkClient.isConnected();
                                boolean isPlaying = simControlPanel != null && simControlPanel.isPlaying();
                                boolean isSimRunning = isPlaying;

                                if (localSimulation != null) {
                                    long curTick = localSimulation.getTickCount();
                                    if (isPlaying && !isConnected && simControlPanel != null) {
                                        simControlPanel.updateTick(curTick, curTick);
                                    }
                                    if (interventionPanel != null) {
                                        interventionPanel.processScheduledEvents(curTick);
                                    }
                                }

                                if (simulationInactiveOverlay != null) {
                                    simulationInactiveOverlay.setVisible(!isSimRunning);
                                }

                                interventionPanel.setSimulationRunning(isSimRunning);

                                if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                                    java.util.List<String> colonyNames = new java.util.ArrayList<>();
                                    for (org.swarmforge.core.domain.Colony c : localSimulation.getColonies()) {
                                        String speciesName = c.getSpeciesName();
                                        colonyNames.add(speciesName != null && !speciesName.isEmpty() ? speciesName : "Colony #" + c.getId().toString().substring(0, 5));
                                    }
                                    interventionPanel.updateAvailableColonies(colonyNames);
                                }

                                long tick = isConnected ? networkClient.getLatestTick() : (localSimulation != null ? localSimulation.getTickCount() : 0);
                                String modeStr = isConnected ? "Dedicated Server (Connected & Synced)" : "Standalone Local Mode";
                                double stepDt = simControlPanel != null ? simControlPanel.getSimulationStepSeconds() : 0.05;
                                String formattedTime = org.swarmforge.client.ui.SimulationControlPanel.formatSimulationTime(tick, stepDt);
                                statsLabel.setText(String.format("🌐 Simulation Engine: %s | Elapsed Time: %s (Step #%d)", modeStr, formattedTime, tick));

                                if (syncLabel != null) {
                                    if (isConnected) {
                                        syncLabel.setText("● Active Remote Persistence (PostgreSQL Server)");
                                        syncLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 11px;");
                                    } else {
                                        syncLabel.setText("● Active Local Persistence (Autonomous SQLite)");
                                        syncLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");
                                    }
                                }

                                // Sync audio synthesizer with live simulation state
                                if (simControlPanel != null) {
                                    String weatherStr = simControlPanel.getSelectedWeather();
                                    double windSpeed = 5.0;
                                    double rainRate = 0.0;
                                    int popCount = 100;
                                    boolean hasRiverInWorld = (lastGeneratedTerrarium != null) && lastGeneratedTerrarium.hasRiver();

                                    if (localSimulation != null) {
                                        org.swarmforge.core.world.WeatherSystem wSys = localSimulation.getWeather();
                                        if (wSys != null) {
                                            if (wSys.getWeatherState() != null) {
                                                weatherStr = wSys.getWeatherState().name();
                                            }
                                            windSpeed = wSys.getWindSpeed();
                                            rainRate = wSys.getRainfall();
                                        }
                                        popCount = localSimulation.getColonies().stream().mapToInt(org.swarmforge.core.domain.Colony::getPopulation).sum();
                                    }

                                    org.swarmforge.client.audio.SimulationAudioManager.getInstance().setWindAndPrecipitation(windSpeed, rainRate);
                                    if (localSimulation != null && localSimulation.getWeather() != null) {
                                        org.swarmforge.core.world.WeatherSystem wSys = localSimulation.getWeather();
                                        org.swarmforge.client.audio.SimulationAudioManager.getInstance().setEnvironmentPhysics(
                                            wSys.getLatitude(),
                                            wSys.getDayOfYear(),
                                            wSys.getTimeOfDay(),
                                            wSys.getLightLevel()
                                        );
                                    }
                                    org.swarmforge.client.audio.SimulationAudioManager.getInstance().updateState(
                                        simControlPanel.getSelectedWorld(),
                                        weatherStr,
                                        "SUMMER",
                                        hasRiverInWorld,
                                        popCount,
                                        gameView != null && gameView.getGameApp() != null ? gameView.getGameApp().getCameraDepth() : 0.0,
                                        isSimRunning && isSim3DFocused()
                                    );
                                }

                                 if (simWorldViewer != null && localSimulation != null) {
                                     simWorldViewer.setSimulation(localSimulation);
                                 }

                                 // Update statistics continuously so initial colony counts are visible
                                if (statisticsDashboard != null) {
                                        StatisticsDashboard.ColonyStats stats = new StatisticsDashboard.ColonyStats();
                                        stats.simTicks = tick;
                                        stats.stepTimeSeconds = simControlPanel != null ? simControlPanel.getSimulationStepSeconds() : 0.05;
                                        int pop = 0;
                                        int workers = 0;
                                        int soldiers = 0;
                                        int queens = 0;
                                        int males = 0;
                                        float foodAmt = 0;
                                        float waterAmt = 0;

                                        if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                                            for (org.swarmforge.core.domain.Colony col : localSimulation.getColonies()) {
                                                int cPop = col.getPopulation();
                                                int cW = col.countByCaste(org.swarmforge.core.domain.Individual.Caste.WORKER);
                                                int cS = col.countByCaste(org.swarmforge.core.domain.Individual.Caste.SOLDIER);
                                                int cQ = col.countByCaste(org.swarmforge.core.domain.Individual.Caste.QUEEN);
                                                int cM = col.countByCaste(org.swarmforge.core.domain.Individual.Caste.MALE);
                                                float cFood = col.getFoodStored();
                                                float cWater = col.getWaterStored();

                                                pop += cPop;
                                                workers += cW;
                                                soldiers += cS;
                                                queens += cQ;
                                                males += cM;
                                                foodAmt += cFood;
                                                waterAmt += cWater;

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
                                        stats.food = foodAmt;
                                        stats.water = waterAmt;
                                        stats.tickRate = isPlaying ? (float) (1.0 / stats.stepTimeSeconds) : 0.0f;
                                        statisticsDashboard.update(stats);
                                }
                        }
                };
                timer.start();
                return pane;
        }

        private void setSimTabsEnabled(boolean enabled) {
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
                tt.setShowDelay(javafx.util.Duration.millis(100));
                return tt;
        }

        private Node createGodModePanel() {
                VBox box = new VBox(10);
                box.setPadding(new Insets(10));

                // Spawn Food
                HBox spawnBox = new HBox(10);
                TextField xF = new TextField("0");
                w(50, xF);
                TextField yF = new TextField("0");
                w(50, yF);
                TextField zF = new TextField("48");
                w(50, zF);
                TextField amtF = new TextField("100");
                w(50, amtF);
                Button spawnBtn = new Button("Spawn Food");

                spawnBox.getChildren().addAll(new Label("X:"), xF, new Label("Y:"), yF, new Label("Z:"), zF,
                                new Label("Amt:"), amtF, spawnBtn);

                spawnBtn.setOnAction(e -> {
                        try {
                                float x = Float.parseFloat(xF.getText());
                                float y = Float.parseFloat(yF.getText());
                                float z = Float.parseFloat(zF.getText());
                                float amt = Float.parseFloat(amtF.getText());
                                networkClient.spawnFood(x, y, z, amt);
                        } catch (Exception ex) {
                                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Invalid input").show();
                        }
                });

                box.getChildren().add(new Label("Spawn Entity"));
                box.getChildren().add(spawnBox);

                box.getChildren().add(new Separator());

                // Disasters
                HBox eventBox = new HBox(10);
                Button rainBtn = new Button("Trigger Rain");
                Button heatBtn = new Button("Trigger Heatwave");

                rainBtn.setOnAction(e -> networkClient.triggerDisaster("RAIN", 1.0f));
                heatBtn.setOnAction(e -> networkClient.triggerDisaster("HEATWAVE", 1.0f));

                eventBox.getChildren().addAll(rainBtn, heatBtn);

                box.getChildren().add(new Label("Environmental Events"));
                box.getChildren().add(eventBox);
                return box;
        }

        private void w(double width, Control c) {
                c.setPrefWidth(width);
        }

        private Node createVisualSimulationViewport() {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                BorderPane rootPane = new BorderPane();

                // Center Viewport: WorldEditorPane 3-View System (3D View + Mouse Orbit/Pan/Zoom Controls + 2D Minimaps)
                // Center Viewport: WorldEditorPane 3-View System (3D View + Mouse Orbit/Pan/Zoom Controls + 2D Minimaps)
                this.simWorldViewer = new org.swarmforge.client.ui.WorldEditorPane();
                this.simWorldViewer.setSimulationMode(true);
                
                // 3D Inactive Overlay Placeholder (Sleek Top Bar - Hidden)
                this.simulationInactiveOverlay = new VBox(4);
                simulationInactiveOverlay.setAlignment(Pos.CENTER);
                simulationInactiveOverlay.setStyle("-fx-background-color: rgba(15, 23, 42, 0.75); -fx-padding: 8 16; -fx-background-radius: 20; -fx-border-color: rgba(56, 189, 248, 0.3); -fx-border-width: 1; -fx-border-radius: 20;");
                simulationInactiveOverlay.setMaxSize(360, 50);

                Label lblInactiveTitle = new Label("🎬 3D View Ready — Waiting for Launch");
                lblInactiveTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;");

                simulationInactiveOverlay.setMouseTransparent(true);
                simulationInactiveOverlay.setPickOnBounds(false);
                simulationInactiveOverlay.setVisible(false);
                simulationInactiveOverlay.setManaged(false);

                simulationInactiveOverlay.getChildren().addAll(lblInactiveTitle);

                // Full Screen Floating Exit Button
                Button btnExitFullscreen = new Button("❌ Exit Fullscreen (ESC)");
                btnExitFullscreen.setStyle("-fx-background-color: rgba(239, 68, 68, 0.9); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 6 14; -fx-cursor: hand;");
                btnExitFullscreen.setVisible(false);
                btnExitFullscreen.setManaged(false);

                StackPane viewportStack = new StackPane(simWorldViewer, simulationInactiveOverlay, btnExitFullscreen);
                StackPane.setAlignment(simulationInactiveOverlay, Pos.TOP_CENTER);
                StackPane.setMargin(simulationInactiveOverlay, new Insets(15));
                StackPane.setAlignment(btnExitFullscreen, Pos.TOP_RIGHT);
                StackPane.setMargin(btnExitFullscreen, new Insets(15));

                simulationInactiveOverlay.setVisible(false);
                simulationInactiveOverlay.setManaged(false);

                rootPane.setCenter(viewportStack);

                // Right Side Controls Sidebar (Glassmorphic Toolbar for Render Modes, Audio Mixer & Video Recording)
                VBox sideControls = new VBox(10);
                sideControls.setPrefWidth(350);
                sideControls.setMinWidth(340);
                sideControls.setPadding(new Insets(10));
                sideControls.getStyleClass().add("card-pane");

                // 1. Controls Header
                Label lblSideTitle = new Label();
                lblSideTitle.textProperty().bind(i18n.createStringBinding("sidebar.title"));
                lblSideTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 13px;");

                HBox sideHeaderBox = new HBox(8, lblSideTitle);
                sideHeaderBox.setAlignment(Pos.CENTER_LEFT);

                // 2. Moved Controls from Simulation Manager: Date & Time, VCR Playback (Rewind/FastForward), Speed & Multipliers
                Node playbackAndSpeedNode = (this.simControlPanel != null) ? this.simControlPanel.getPlaybackAndSpeedPanel() : new VBox();

                this.btnRecVideo = new Button(i18n.get("sidebar.btn.video"));
                this.btnRecVideo.setMaxWidth(Double.MAX_VALUE);
                this.btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                java.io.File videoDir = new java.io.File("captures/videos");
                Tooltip ttRecVideo = new Tooltip("🎥 Enregistrement Vidéo 3D (MP4 / GIF)\n📍 Emplacement: " + videoDir.getAbsolutePath());
                this.btnRecVideo.setTooltip(ttRecVideo);

                Runnable startVideoRecordingInternal = () -> {
                    if (isVideoRecording) return;
                    isVideoRecording = true;
                    isVideoArmed = false;
                    recordedVideoFrames.clear();
                    videoRecordingStartMs = System.currentTimeMillis();
                    org.swarmforge.client.audio.SimulationAudioManager.getInstance().startAudioRecording();

                    btnRecVideo.setText(i18n.get("sidebar.btn.video_recording", "🔴 Stop (REC 00:00 / 10:00 - 0 frames)"));
                    btnRecVideo.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");

                    videoCaptureTimeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), ev -> {
                            if (!isVideoRecording) return;
                            try {
                                javafx.scene.image.WritableImage snap = simWorldViewer.snapshot(new javafx.scene.SnapshotParameters(), null);
                                java.awt.image.BufferedImage frame = org.swarmforge.client.util.MediaCaptureUtil.convertToBufferedImage(snap);
                                recordedVideoFrames.add(frame);

                                long elapsedMs = System.currentTimeMillis() - videoRecordingStartMs;
                                long elapsedSec = elapsedMs / 1000;
                                long minutes = elapsedSec / 60;
                                long seconds = elapsedSec % 60;

                                btnRecVideo.setText(String.format("🔴 Stop (REC %02d:%02d / 10:00 - %d frames)", minutes, seconds, recordedVideoFrames.size()));

                                if (elapsedSec >= 600) {
                                    if (stopVideoRecordingAndExport != null) stopVideoRecordingAndExport.run();
                                }
                            } catch (Exception err) {
                                LOG.warning("Failed frame capture: " + err.getMessage());
                            }
                        })
                    );
                    videoCaptureTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
                    videoCaptureTimeline.play();
                };

                Runnable disarmVideoRecording = () -> {
                    if (isVideoArmed) {
                        isVideoArmed = false;
                        btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                        btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                    }
                };

                this.stopVideoRecordingAndExport = () -> {
                    if (!isVideoRecording) return;
                    isVideoRecording = false;
                    isVideoArmed = false;
                    if (videoCaptureTimeline != null) {
                        videoCaptureTimeline.stop();
                        videoCaptureTimeline = null;
                    }

                    final byte[] pcmAudioData = org.swarmforge.client.audio.SimulationAudioManager.getInstance().stopAudioRecording();

                    btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                    btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");

                    final java.util.List<java.awt.image.BufferedImage> framesToExport = new java.util.ArrayList<>(recordedVideoFrames);
                    final String scName = (simControlPanel != null) ? simControlPanel.getSelectedScenarioName() : "Scenario";

                    if (framesToExport.isEmpty()) {
                        return;
                    }

                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            java.io.File videoFile = org.swarmforge.client.util.MediaCaptureUtil.exportMp4VideoClip(framesToExport, pcmAudioData, scName, 10);
                            Platform.runLater(() -> {
                                LOG.info("[SwarmForge] Vidéo 3D MP4 exportée: " + videoFile.getAbsolutePath());
                                btnRecVideo.setText("✓ Video Recorded!");
                                btnRecVideo.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");

                                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                                pt.setOnFinished(ev -> {
                                    btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                                    btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                                });
                                pt.play();
                            });
                        } catch (Exception ex) {
                            LOG.warning("MP4 encoding failed, falling back to Animated GIF: " + ex.getMessage());
                            try {
                                java.io.File videoFile = org.swarmforge.client.util.MediaCaptureUtil.exportGifVideoClip(framesToExport, scName, 100);
                                Platform.runLater(() -> {
                                    LOG.info("[SwarmForge] Vidéo 3D GIF exportée: " + videoFile.getAbsolutePath());
                                    btnRecVideo.setText("✓ Video Recorded!");
                                    btnRecVideo.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");

                                    javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                                    pt.setOnFinished(ev -> {
                                        btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                                        btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                                    });
                                    pt.play();
                                });
                            } catch (Exception gifEx) {
                                LOG.severe("Error exporting video clip: " + gifEx.getMessage());
                            }
                        }
                    });
                };

                this.cancelAndResetVideoRecording = () -> {
                    isVideoRecording = false;
                    isVideoArmed = false;
                    if (videoCaptureTimeline != null) {
                        videoCaptureTimeline.stop();
                        videoCaptureTimeline = null;
                    }
                    recordedVideoFrames.clear();
                    try {
                        org.swarmforge.client.audio.SimulationAudioManager.getInstance().stopAudioRecording();
                    } catch (Exception ignored) {}
                    if (btnRecVideo != null) {
                        Platform.runLater(() -> {
                            btnRecVideo.setText(i18n.get("sidebar.btn.video"));
                            btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                        });
                    }
                };
                Runnable cancelAndResetVideoRecording = this.cancelAndResetVideoRecording;

                if (simControlPanel != null) {
                        simControlPanel.setOnPlay(v -> {
                                if (simulationInactiveOverlay != null) simulationInactiveOverlay.setVisible(false);
                                if (localSimulation != null) localSimulation.start();
                                if (isVideoArmed && !isVideoRecording) {
                                    startVideoRecordingInternal.run();
                                }
                        });
                        simControlPanel.setOnPause(v -> {
                                if (localSimulation != null) localSimulation.pause();
                                if (isVideoRecording && stopVideoRecordingAndExport != null) {
                                    stopVideoRecordingAndExport.run();
                                } else if (isVideoArmed) {
                                    disarmVideoRecording.run();
                                }
                        });
                        simControlPanel.setOnStop(v -> {
                                if (localSimulation != null) {
                                        localSimulation.stop();
                                        localSimulation.reset(0);
                                        simControlPanel.updateTick(0, 0);
                                }
                                if (simulationInactiveOverlay != null) simulationInactiveOverlay.setVisible(true);
                                if (isVideoRecording && stopVideoRecordingAndExport != null) {
                                    stopVideoRecordingAndExport.run();
                                } else if (isVideoArmed) {
                                    disarmVideoRecording.run();
                                }
                        });
                        simControlPanel.setOnRewind(steps -> {
                                if (localSimulation != null) {
                                        localSimulation.pause();
                                        localSimulation.rewind(steps);
                                        long curTick = localSimulation.getTickCount();
                                        simControlPanel.updateTick(curTick, localSimulation.getHighestRecordedTick());
                                        simWorldViewer.repaintAllViews();
                                }
                                if (isVideoRecording && stopVideoRecordingAndExport != null) {
                                    stopVideoRecordingAndExport.run();
                                } else if (isVideoArmed) {
                                    disarmVideoRecording.run();
                                }
                        });
                        simControlPanel.setOnStepForward(v -> {
                                if (localSimulation != null) {
                                        localSimulation.pause();
                                        localSimulation.tick();
                                        long curTick = localSimulation.getTickCount();
                                        simControlPanel.updateTick(curTick, localSimulation.getHighestRecordedTick());
                                        simWorldViewer.repaintAllViews();
                                }
                                if (isVideoRecording && stopVideoRecordingAndExport != null) {
                                    stopVideoRecordingAndExport.run();
                                } else if (isVideoArmed) {
                                    disarmVideoRecording.run();
                                }
                        });
                        simControlPanel.setOnSeek(tick -> {
                                if (tick == 0) {
                                        cancelAndResetVideoRecording.run();
                                        if (interventionPanel != null) {
                                                interventionPanel.resetEventsState();
                                        }
                                }
                                if (localSimulation != null) {
                                        localSimulation.pause();
                                        localSimulation.seekToTick(tick);
                                        long curTick = localSimulation.getTickCount();
                                        simControlPanel.updateTick(curTick, localSimulation.getHighestRecordedTick());
                                        simWorldViewer.repaintAllViews();
                                }
                                if (isVideoRecording && stopVideoRecordingAndExport != null) {
                                    stopVideoRecordingAndExport.run();
                                } else if (isVideoArmed) {
                                    disarmVideoRecording.run();
                                }
                        });
                        simControlPanel.setOnSpeedChange(speed -> {
                                if (localSimulation != null) localSimulation.setSpeedMultiplier(speed);
                                if (isVideoRecording && stopVideoRecordingAndExport != null) {
                                    stopVideoRecordingAndExport.run();
                                } else if (isVideoArmed) {
                                    disarmVideoRecording.run();
                                }
                        });
                }

                // 3. Media & Recording Section (Toast Notifications)
                VBox mediaSection = new VBox(6);
                mediaSection.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");
                Label lblMedia = new Label();
                lblMedia.textProperty().bind(i18n.createStringBinding("sidebar.media.title"));
                lblMedia.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

                Button btnFullscreenMode = new Button();
                btnFullscreenMode.textProperty().bind(i18n.createStringBinding("sidebar.btn.fullscreen"));
                btnFullscreenMode.setMaxWidth(Double.MAX_VALUE);
                btnFullscreenMode.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                Tooltip ttFS = new Tooltip();
                ttFS.textProperty().bind(i18n.createStringBinding("sidebar.btn.fullscreen.tt"));
                btnFullscreenMode.setTooltip(ttFS);

                Button btnPhoto = new Button(i18n.get("sidebar.btn.photo"));
                btnPhoto.setMaxWidth(Double.MAX_VALUE);
                btnPhoto.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                java.io.File screenshotDir = new java.io.File("captures/screenshots");
                Tooltip ttPhoto = new Tooltip("📸 Capture d'écran HD (PNG)\n📍 Emplacement: " + screenshotDir.getAbsolutePath());
                btnPhoto.setTooltip(ttPhoto);

                btnPhoto.setOnAction(e -> {
                    try {
                        // 1. Camera shutter visual flash effect overlay on viewport
                        javafx.scene.shape.Rectangle flash = new javafx.scene.shape.Rectangle();
                        flash.widthProperty().bind(viewportStack.widthProperty());
                        flash.heightProperty().bind(viewportStack.heightProperty());
                        flash.setFill(javafx.scene.paint.Color.WHITE);
                        flash.setOpacity(0.75);
                        flash.setMouseTransparent(true);
                        viewportStack.getChildren().add(flash);

                        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), flash);
                        ft.setFromValue(0.75);
                        ft.setToValue(0.0);
                        ft.setOnFinished(ev -> viewportStack.getChildren().remove(flash));
                        ft.play();

                        // 2. Take HD screenshot
                        String scName = (simControlPanel != null) ? simControlPanel.getSelectedScenarioName() : "Scenario";
                        java.io.File screenshotFile = org.swarmforge.client.util.MediaCaptureUtil.takeScreenshot(simWorldViewer, scName);
                        LOG.info("[SwarmForge] Screenshot HD enregistré: " + screenshotFile.getAbsolutePath());

                        // 3. Green checkmark indicator directly on button for 3 seconds
                        btnPhoto.setText("✓ Screenshot Saved!");
                        btnPhoto.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");

                        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                        pt.setOnFinished(ev -> {
                            btnPhoto.setText(i18n.get("sidebar.btn.photo"));
                            btnPhoto.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                        });
                        pt.play();
                    } catch (Exception ex) {
                        LOG.severe("Error capturing HD screenshot: " + ex.getMessage());
                    }
                });

                btnRecVideo.setOnAction(e -> {
                    boolean isSimRunning = (simControlPanel != null && simControlPanel.isPlaying());
                    if (isVideoRecording) {
                        // Stop Video Recording & Export Clip
                        if (stopVideoRecordingAndExport != null) stopVideoRecordingAndExport.run();
                    } else if (isVideoArmed) {
                        // Cancel armed status
                        disarmVideoRecording.run();
                    } else if (!isSimRunning) {
                        // Arm video recording for when simulation starts
                        isVideoArmed = true;
                        btnRecVideo.textProperty().unbind();
                        btnRecVideo.setText("⏳ REC Armé (En attente)");
                        btnRecVideo.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                    } else {
                        // Simulation is running -> Start recording immediately
                        startVideoRecordingInternal.run();
                    }
                });

                mediaSection.getChildren().addAll(lblMedia, btnFullscreenMode, btnPhoto, btnRecVideo);

                // 4. Render Mode & 3D Layer Management Section
                VBox renderSection = new VBox(6);
                renderSection.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");
                Label lblRenderMode = new Label();
                lblRenderMode.textProperty().bind(i18n.createStringBinding("sidebar.render_layers.title"));
                lblRenderMode.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

                ComboBox<String> comboRenderMode = new ComboBox<>();
                comboRenderMode.getItems().addAll(
                    "🌿 Mode Réaliste (Naturaliste Photoréaliste)",
                    "🔬 Mode Scientifique (Minimaliste Structural)",
                    "🎮 Mode Gamifié (Voxel / Minecraft)"
                );
                comboRenderMode.getSelectionModel().selectFirst();
                comboRenderMode.setMaxWidth(Double.MAX_VALUE);
                comboRenderMode.setStyle("-fx-font-size: 11px;");
                comboRenderMode.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
                    if (newV == null) return;
                    if (newV.contains("Scientifique")) {
                        simWorldViewer.setRenderMode(org.swarmforge.client.ui.WorldEditorPane.RenderMode.SCIENTIFIC);
                        if (gameView != null) gameView.setScientificMode(true);
                    } else if (newV.contains("Gamifié")) {
                        simWorldViewer.setRenderMode(org.swarmforge.client.ui.WorldEditorPane.RenderMode.GAMIFIED);
                        if (gameView != null) gameView.setGamifiedVoxelMode(true);
                    } else {
                        simWorldViewer.setRenderMode(org.swarmforge.client.ui.WorldEditorPane.RenderMode.REALISTIC);
                        if (gameView != null) {
                            gameView.setScientificMode(false);
                            gameView.setGamifiedVoxelMode(false);
                        }
                    }
                });

                CheckBox chkMinimap = new CheckBox();
                chkMinimap.textProperty().bind(i18n.createStringBinding("sidebar.chk.minimap"));
                chkMinimap.setSelected(true);
                chkMinimap.setStyle("-fx-font-size: 11px;");
                Tooltip ttMinimap = new Tooltip();
                ttMinimap.textProperty().bind(i18n.createStringBinding("sidebar.chk.minimap.tt"));
                chkMinimap.setTooltip(ttMinimap);
                chkMinimap.selectedProperty().addListener((o, a, b) -> simWorldViewer.setDualMinimapVisible(b));

                CheckBox chkTerrain = new CheckBox();
                chkTerrain.textProperty().bind(i18n.createStringBinding("sidebar.chk.terrain"));
                chkTerrain.setSelected(true);
                chkTerrain.setStyle("-fx-font-size: 11px;");
                chkTerrain.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setTerrainVisible(b);
                        simWorldViewer.setTerrainVisible(b);
                });

                CheckBox chkTrees = new CheckBox();
                chkTrees.textProperty().bind(i18n.createStringBinding("sidebar.chk.trees"));
                chkTrees.setSelected(true);
                chkTrees.setStyle("-fx-font-size: 11px;");
                Tooltip ttTrees = new Tooltip();
                ttTrees.textProperty().bind(i18n.createStringBinding("sidebar.chk.trees.tt"));
                chkTrees.setTooltip(ttTrees);
                chkTrees.selectedProperty().addListener((o, a, b) -> simWorldViewer.setShowTrees(b));

                CheckBox chkSkirt = new CheckBox();
                chkSkirt.textProperty().bind(i18n.createStringBinding("sidebar.chk.skirt"));
                chkSkirt.setSelected(true);
                chkSkirt.setStyle("-fx-font-size: 11px;");
                Tooltip ttSkirt = new Tooltip();
                ttSkirt.textProperty().bind(i18n.createStringBinding("sidebar.chk.skirt.tt"));
                chkSkirt.setTooltip(ttSkirt);
                chkSkirt.selectedProperty().addListener((o, a, b) -> simWorldViewer.setShow3DSkirt(b));

                Slider sliceSlider = new Slider(0, 100, 50);
                sliceSlider.setPrefWidth(120);
                sliceSlider.valueProperty().addListener((o, a, b) -> simWorldViewer.setSlicePlane(b.doubleValue()));
                Label sliceLbl = new Label();
                sliceLbl.textProperty().bind(i18n.createStringBinding("sidebar.lbl.slice"));
                sliceLbl.setStyle("-fx-font-size: 10px;");
                HBox sliceBox = new HBox(6, sliceLbl, sliceSlider);

                CheckBox chkNid = new CheckBox();
                chkNid.textProperty().bind(i18n.createStringBinding("sidebar.chk.nest"));
                chkNid.setSelected(true);
                chkNid.setStyle("-fx-font-size: 11px;");
                chkNid.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setTunnelsVisible(b);
                        simWorldViewer.setGalleriesVisible(b);
                });

                CheckBox chkPheromonesLayer = new CheckBox();
                chkPheromonesLayer.textProperty().bind(i18n.createStringBinding("sidebar.chk.pheromones"));
                chkPheromonesLayer.setSelected(true);
                chkPheromonesLayer.setStyle("-fx-font-size: 11px;");
                chkPheromonesLayer.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setPheromonesVisible(b);
                        simWorldViewer.setPheromonesVisible(b);
                });

                ComboBox<String> comboPheromoneType = new ComboBox<>();
                comboPheromoneType.getItems().addAll(
                    "🌐 Toutes les Phéromones",
                    "🚨 Phéromone d'Alarme",
                    "🍃 Phéromone de Nourriture",
                    "🏠 Phéromone de Nid / Homing",
                    "📢 Phéromone de Recrutement",
                    "👑 Phéromone de Reine",
                    "💀 Phéromone de Cadavre (Nécrophorèse)"
                );
                comboPheromoneType.getSelectionModel().selectFirst();
                comboPheromoneType.setMaxWidth(Double.MAX_VALUE);
                comboPheromoneType.setStyle("-fx-font-size: 10px;");
                comboPheromoneType.getSelectionModel().selectedIndexProperty().addListener((o, oldV, newV) -> {
                    if (newV == null) return;
                    int idx = newV.intValue();
                    if (simWorldViewer != null) {
                        simWorldViewer.setPheromoneChannelFilter(idx);
                        simWorldViewer.setPheromoneRenderMode(org.swarmforge.client.ui.WorldEditorPane.PheromoneRenderMode.HEATMAP_GRADIENT);
                    }
                });

                CheckBox chkAntsLayer = new CheckBox();
                chkAntsLayer.textProperty().bind(i18n.createStringBinding("sidebar.chk.ants"));
                chkAntsLayer.setSelected(true);
                chkAntsLayer.setStyle("-fx-font-size: 11px;");
                chkAntsLayer.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setAntsVisible(b);
                        simWorldViewer.setColonyVisible(b);
                });

                CheckBox chkWeatherLayer = new CheckBox();
                chkWeatherLayer.textProperty().bind(i18n.createStringBinding("sidebar.chk.weather"));
                chkWeatherLayer.setSelected(true);
                chkWeatherLayer.setStyle("-fx-font-size: 11px;");
                chkWeatherLayer.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setWeatherVisible(b);
                        simWorldViewer.setWeatherVisible(b);
                });

                renderSection.getChildren().addAll(lblRenderMode, comboRenderMode, chkMinimap, chkTerrain, chkTrees, chkSkirt, sliceBox, chkNid, chkPheromonesLayer, comboPheromoneType, chkAntsLayer, chkWeatherLayer);

                // Audio Controls Section
                VBox audioSection = new VBox(6);
                audioSection.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");
                Label lblAudio = new Label();
                lblAudio.textProperty().bind(i18n.createStringBinding("sidebar.audio.title"));
                lblAudio.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

                org.swarmforge.client.audio.SimulationAudioManager audioMgr = org.swarmforge.client.audio.SimulationAudioManager.getInstance();

                Slider masterVolSlider = new Slider(0, 100, 70);
                masterVolSlider.setStyle("-fx-font-size: 9px;");
                masterVolSlider.valueProperty().addListener((o, oldV, newV) -> {
                        audioMgr.setMasterVolume(newV.doubleValue() / 100.0);
                });
                Label volLbl = new Label();
                volLbl.textProperty().bind(i18n.createStringBinding("sidebar.audio.volume"));
                volLbl.setStyle("-fx-font-size: 10px;");
                HBox volBox = new HBox(6, volLbl, masterVolSlider);

                CheckBox chkAmbientSound = new CheckBox();
                chkAmbientSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.ambient"));
                chkAmbientSound.setSelected(audioMgr.isAmbientEnabled());
                chkAmbientSound.setStyle("-fx-font-size: 10px;");
                Tooltip ttAmb = new Tooltip();
                ttAmb.textProperty().bind(i18n.createStringBinding("sidebar.audio.ambient.tt"));
                chkAmbientSound.setTooltip(ttAmb);
                chkAmbientSound.selectedProperty().addListener((o, a, b) -> audioMgr.setAmbientEnabled(b));

                CheckBox chkRiverSound = new CheckBox();
                chkRiverSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.river"));
                chkRiverSound.setSelected(audioMgr.isRiverEnabled());
                chkRiverSound.setStyle("-fx-font-size: 10px;");
                Tooltip ttRiv = new Tooltip();
                ttRiv.textProperty().bind(i18n.createStringBinding("sidebar.audio.river.tt"));
                chkRiverSound.setTooltip(ttRiv);
                chkRiverSound.selectedProperty().addListener((o, a, b) -> audioMgr.setRiverEnabled(b));

                CheckBox chkWeatherSound = new CheckBox();
                chkWeatherSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.weather"));
                chkWeatherSound.setSelected(audioMgr.isWeatherEnabled());
                chkWeatherSound.setStyle("-fx-font-size: 10px;");
                Tooltip ttWea = new Tooltip();
                ttWea.textProperty().bind(i18n.createStringBinding("sidebar.audio.weather.tt"));
                chkWeatherSound.setTooltip(ttWea);
                chkWeatherSound.selectedProperty().addListener((o, a, b) -> audioMgr.setWeatherEnabled(b));

                CheckBox chkInsectSound = new CheckBox();
                chkInsectSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.insect"));
                chkInsectSound.setSelected(audioMgr.isInsectEnabled());
                chkInsectSound.setStyle("-fx-font-size: 10px;");
                Tooltip ttIns = new Tooltip();
                ttIns.textProperty().bind(i18n.createStringBinding("sidebar.audio.insect.tt"));
                chkInsectSound.setTooltip(ttIns);
                chkInsectSound.selectedProperty().addListener((o, a, b) -> audioMgr.setInsectEnabled(b));

                audioSection.getChildren().addAll(lblAudio, volBox, chkAmbientSound, chkRiverSound, chkWeatherSound, chkInsectSound);

                sideControls.getChildren().addAll(sideHeaderBox, playbackAndSpeedNode, new Separator(), mediaSection, renderSection, audioSection);

                ScrollPane sideScroll = new ScrollPane(sideControls);
                sideScroll.setMinWidth(350);
                sideScroll.setPrefWidth(350);
                sideScroll.setFitToWidth(true);
                sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                sideScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

                rootPane.setRight(sideScroll);

                // Native Stage Full Screen Toggle Action
                Runnable toggleFullscreen = () -> {
                        javafx.stage.Stage stage = rootPane.getScene() != null ? (javafx.stage.Stage) rootPane.getScene().getWindow() : null;
                        if (stage != null) {
                                boolean willBeFS = !stage.isFullScreen();
                                if (willBeFS) {
                                        if (mainTabs != null && simTab != null) {
                                                mainTabs.getSelectionModel().select(simTab);
                                        }
                                        if (simSubTabs != null && visualTab != null) {
                                                simSubTabs.getSelectionModel().select(visualTab);
                                        }
                                }
                                stage.setFullScreen(willBeFS);
                        }
                };

                btnFullscreenMode.setOnAction(e -> toggleFullscreen.run());
                btnExitFullscreen.setOnAction(e -> toggleFullscreen.run());

                // -----------------------------------------------------------------------
                // Full-screen state handler — extracted so it can be reused safely
                // regardless of whether scene/window are already attached.
                // -----------------------------------------------------------------------
                java.util.function.Consumer<javafx.stage.Stage> registerFsListener = new java.util.function.Consumer<javafx.stage.Stage>() {
                        // Guard: only register once per stage instance
                        private final java.util.Set<javafx.stage.Stage> registered = new java.util.HashSet<>();

                        @Override
                        public void accept(javafx.stage.Stage stage) {
                                if (stage == null || !registered.add(stage)) return;
                                stage.fullScreenProperty().addListener((fsObs, oldFS, isFS) -> {
                                        if (isFS) {
                                                if (mainTabs != null && simTab != null) {
                                                        mainTabs.getSelectionModel().select(simTab);
                                                }
                                                if (simSubTabs != null && visualTab != null) {
                                                        simSubTabs.getSelectionModel().select(visualTab);
                                                }
                                                if (mainTabs != null) {
                                                        if (!mainTabs.getStyleClass().contains("tab-pane-fullscreen")) {
                                                                mainTabs.getStyleClass().add("tab-pane-fullscreen");
                                                        }
                                                        mainTabs.setStyle("-fx-tab-max-height: 0; -fx-tab-min-height: 0; -fx-padding: 0; -fx-border-width: 0;");
                                                        Node hdr = mainTabs.lookup(".tab-header-area");
                                                        if (hdr != null) { hdr.setVisible(false); hdr.setManaged(false); }
                                                }
                                                if (simSubTabs != null) {
                                                        if (!simSubTabs.getStyleClass().contains("tab-pane-fullscreen")) {
                                                                simSubTabs.getStyleClass().add("tab-pane-fullscreen");
                                                        }
                                                        simSubTabs.setStyle("-fx-tab-max-height: 0; -fx-tab-min-height: 0; -fx-padding: 0; -fx-border-width: 0;");
                                                        Node hdr = simSubTabs.lookup(".tab-header-area");
                                                        if (hdr != null) { hdr.setVisible(false); hdr.setManaged(false); }
                                                }
                                                if (connectBox != null) {
                                                        connectBox.setVisible(false);
                                                        connectBox.setManaged(false);
                                                }
                                                rootPane.setRight(null);
                                                if (simWorldViewer != null) {
                                                        simWorldViewer.setFullscreenMode(true);
                                                }
                                                if (worldEditorPane != null) {
                                                        worldEditorPane.setFullscreenMode(true);
                                                }
                                                btnExitFullscreen.setVisible(false);
                                        } else {
                                                if (mainTabs != null) {
                                                        mainTabs.getStyleClass().remove("tab-pane-fullscreen");
                                                        mainTabs.setStyle("");
                                                        Node hdr = mainTabs.lookup(".tab-header-area");
                                                        if (hdr != null) { hdr.setVisible(true); hdr.setManaged(true); }
                                                }
                                                if (simSubTabs != null) {
                                                        simSubTabs.getStyleClass().remove("tab-pane-fullscreen");
                                                        simSubTabs.setStyle("");
                                                        Node hdr = simSubTabs.lookup(".tab-header-area");
                                                        if (hdr != null) { hdr.setVisible(true); hdr.setManaged(true); }
                                                }
                                                if (connectBox != null) {
                                                        connectBox.setVisible(true);
                                                        connectBox.setManaged(true);
                                                }
                                                rootPane.setRight(sideScroll);
                                                if (simWorldViewer != null) {
                                                        simWorldViewer.setFullscreenMode(false);
                                                        simWorldViewer.setDualMinimapVisible(chkMinimap.isSelected());
                                                }
                                                if (worldEditorPane != null) {
                                                        worldEditorPane.setFullscreenMode(false);
                                                }
                                                btnExitFullscreen.setVisible(false);
                                        }
                                        update3DRenderingState();
                                        if (simWorldViewer != null) {
                                                simWorldViewer.repaintAllViews();
                                        }
                                        if (worldEditorPane != null) {
                                                worldEditorPane.repaintAllViews();
                                        }
                                });
                        }
                };

                // Wire up: scene → window → stage, handling already-attached values at each level
                java.util.function.Consumer<javafx.scene.Scene> onSceneAttached = scene -> {
                        if (scene == null) return;
                        // F11 shortcut
                        scene.setOnKeyPressed(ke -> {
                                if (ke.getCode() == javafx.scene.input.KeyCode.F11) {
                                        toggleFullscreen.run();
                                        ke.consume();
                                }
                        });
                        // Register fullscreen listener now if window already known, else wait
                        java.util.function.Consumer<javafx.stage.Stage> finalRegister = registerFsListener;
                        scene.windowProperty().addListener((wObs, oldW, newW) -> {
                                if (newW instanceof javafx.stage.Stage s) finalRegister.accept(s);
                        });
                        // Window may already be set at this point
                        if (scene.getWindow() instanceof javafx.stage.Stage s) {
                                finalRegister.accept(s);
                        }
                };

                rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> onSceneAttached.accept(newScene));
                // Scene may already be set at this point (e.g. if tab was pre-loaded)
                onSceneAttached.accept(rootPane.getScene());

                return rootPane;
        }

        private boolean isSim3DFocused() {
                boolean isSimTabSelected = (mainTabs != null && mainTabs.getSelectionModel().getSelectedItem() == simTab);
                boolean isVisualSubTabSelected = (simSubTabs == null || simSubTabs.getSelectionModel().getSelectedItem() == visualTab);
                return isSimTabSelected && isVisualSubTabSelected;
        }

        private void update3DRenderingState() {
                boolean sim3DFocused = isSim3DFocused();

                if (simWorldViewer != null) {
                        simWorldViewer.setActive(sim3DFocused);
                }

                boolean isWorldTabSelected = (mainTabs != null && mainTabs.getSelectionModel().getSelectedItem() == worldTab);
                if (worldEditorPane != null) {
                        worldEditorPane.setActive(isWorldTabSelected);
                }

                boolean isSimRunning = (localSimulation != null && localSimulation.isRunning());
                org.swarmforge.client.audio.SimulationAudioManager.getInstance().updateState(
                        simControlPanel != null ? simControlPanel.getSelectedWorld() : "Forest",
                        "Clear",
                        "SUMMER",
                        false,
                        0,
                        simWorldViewer != null ? simWorldViewer.getZoom() : 7.5,
                        isSimRunning && sim3DFocused
                );
        }

        private Node createWorldEditor() {
                // World Editor Pane with 3-View System (3D, Top-Down, Side) & 3D Sculpting Brushes
                this.worldEditorPane = new org.swarmforge.client.ui.WorldEditorPane();
                worldEditorPane.setSimulationMode(false);
                worldEditorPane.setOnGenerate(config -> {
                        try {
                                double sizeMeters = (double) config.getOrDefault("surfaceSizeMeters", 2.0);
                                double resMm = (double) config.getOrDefault("resolutionMm", 0.5);
                                double roughness = (double) config.getOrDefault("roughness", 0.45);

                                int gridW = (int) (sizeMeters * 32);
                                int gridH = (int) (sizeMeters * 32);
                                int gridDepth = 32;

                                org.swarmforge.core.domain.Terrarium terrarium = new org.swarmforge.core.domain.Terrarium(gridW, gridH, gridDepth);
                                org.swarmforge.core.world.TerrainGenerator gen = new org.swarmforge.core.world.TerrainGenerator(System.currentTimeMillis());

                                gen.generate(terrarium, gridDepth / 2, 10, (float) roughness * 0.1f);
                                if (Boolean.TRUE.equals(config.get("hasRiver"))) {
                                        gen.addWater(terrarium, gridDepth / 2 - 2, 0.4f);
                                }

                                this.lastGeneratedTerrarium = terrarium;
                                if (gameView != null && gameView.getGameApp() != null) {
                                        gameView.getGameApp().renderTerrarium(terrarium);
                                }

                                localSimulation = new org.swarmforge.core.simulation.Simulation(terrarium);
                                localSimulation.setTicksPerSecond(20);
                                startLocalSimulationUpdates();

                                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Monde généré avec succès ! (Taille: " + sizeMeters + "m, Voxel: " + resMm + "mm)").show();

                        } catch (Exception ex) {
                                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Échec de génération: " + ex.getMessage()).show();
                                ex.printStackTrace();
                        }
                });

                return worldEditorPane;
        }

        private Node createSpeciesEditor() {
                org.swarmforge.client.ui.SpeciesEditorPane pane = new org.swarmforge.client.ui.SpeciesEditorPane();
                pane.setOnApply(species -> {
                        this.currentSpecies = species;
                        org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Espèce active mise à jour : " + species.getCommonName()).show();
                });
                return pane;
        }

        private Node createSettingsPane() {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                VBox main = new VBox(15);
                main.setPadding(new Insets(10, 15, 10, 15));

                Label title = new Label();
                title.textProperty().bind(i18n.createStringBinding("settings.title"));
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

                VBox headerBox = new VBox(6);
                headerBox.getChildren().addAll(title, new Separator());

                GridPane grid = new GridPane();
                grid.setHgap(20);
                grid.setVgap(15);
                grid.setPadding(new Insets(10, 0, 10, 0));

                // 1. Language Row
                Label langLabel = new Label();
                langLabel.textProperty().bind(i18n.createStringBinding("settings.language"));
                langLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                langLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.language.tt"));

                ComboBox<String> langCombo = new ComboBox<>();
                langCombo.getItems().addAll("English", "Français", "Español", "Deutsch", "中文");
                langCombo.tooltipProperty().bind(i18n.createTooltipBinding("settings.language.combo.tt"));

                java.util.Locale currentLoc = i18n.getLocale();
                String langStr = currentLoc.getLanguage();
                if (langStr.equals("fr")) langCombo.setValue("Français");
                else if (langStr.equals("es")) langCombo.setValue("Español");
                else if (langStr.equals("de")) langCombo.setValue("Deutsch");
                else if (langStr.equals("zh")) langCombo.setValue("中文");
                else langCombo.setValue("English");

                langCombo.setOnAction(e -> {
                        String val = langCombo.getValue();
                        if ("Français".equals(val)) i18n.setLocale(java.util.Locale.FRENCH);
                        else if ("Español".equals(val)) i18n.setLocale(java.util.Locale.forLanguageTag("es"));
                        else if ("Deutsch".equals(val)) i18n.setLocale(java.util.Locale.forLanguageTag("de"));
                        else if ("中文".equals(val)) i18n.setLocale(java.util.Locale.forLanguageTag("zh"));
                        else i18n.setLocale(java.util.Locale.ENGLISH);
                });

                // 2. Theme Row
                Label themeLabel = new Label();
                themeLabel.textProperty().bind(i18n.createStringBinding("settings.theme"));
                themeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                themeLabel.tooltipProperty().bind(i18n.createTooltipBinding("settings.theme.tt"));

                ComboBox<String> themeCombo = new ComboBox<>();
                themeCombo.getItems().addAll("Dark Theme", "Light Theme");
                themeCombo.tooltipProperty().bind(i18n.createTooltipBinding("settings.theme.combo.tt"));
                if (org.swarmforge.client.util.ThemeManager.getInstance().getCurrentTheme() == org.swarmforge.client.util.ThemeManager.Theme.DARK) {
                        themeCombo.setValue("Dark Theme");
                } else {
                        themeCombo.setValue("Light Theme");
                }

                themeCombo.setOnAction(e -> {
                        if ("Dark Theme".equals(themeCombo.getValue())) {
                                org.swarmforge.client.util.ThemeManager.getInstance().setTheme(org.swarmforge.client.util.ThemeManager.Theme.DARK);
                        } else {
                                org.swarmforge.client.util.ThemeManager.getInstance().setTheme(org.swarmforge.client.util.ThemeManager.Theme.LIGHT);
                        }
                });

                grid.add(langLabel, 0, 0);
                grid.add(langCombo, 1, 0);
                grid.add(themeLabel, 0, 1);
                grid.add(themeCombo, 1, 1);

                VBox settingsCard = new VBox(8, headerBox, grid);
                settingsCard.getStyleClass().add("card-pane");
                settingsCard.setPadding(new Insets(12));
                settingsCard.setMaxWidth(650);

                main.getChildren().add(settingsCard);
                VBox.setVgrow(main, Priority.ALWAYS);
                return main;
        }

        private VBox createMirrorOverlay() {
                VBox box = new VBox(5);
                box.setStyle("-fx-background-color: rgba(20, 20, 30, 0.6); -fx-padding: 10; -fx-background-radius: 5;");
                box.setMaxSize(200, 150);

                // "Mirror" effect items
                Label title = new Label("LIVE STATUS");
                title.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold;");

                box.getChildren().addAll(
                                title,
                                new Separator(),
                                formatStat("Tick", "12,450"),
                                formatStat("FPS", "60"),
                                formatStat("Pop", "342"),
                                formatStat("Food", "1.2k"));

                // Pass clicks through
                box.setPickOnBounds(false);
                return box;
        }

        private HBox formatStat(String label, String value) {
                HBox row = new HBox(10);
                Label l = new Label(label + ":");
                l.setStyle("-fx-text-fill: lightgray;");
                Label v = new Label(value);
                v.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                row.getChildren().addAll(l, v);
                return row;
        }

        private void setupMouseControls(GameViewPane view) {
                view.setOnMousePressed(e -> {
                        lastX = e.getSceneX();
                        lastY = e.getSceneY();
                });

                view.setOnMouseDragged(e -> {
                        double dx = e.getSceneX() - lastX;
                        double dy = e.getSceneY() - lastY;

                        if (e.isPrimaryButtonDown()) { // Left: Rotate
                                view.getGameApp().rotateCamera((float) dx, (float) dy);
                        } else if (e.isSecondaryButtonDown() || e.isShiftDown()) { // Right / Shift: Pan
                                view.getGameApp().panCamera((float) -dx, (float) dy);
                        } else if (e.isMiddleButtonDown()) { // Middle: Pan
                                view.getGameApp().panCamera((float) -dx, (float) dy);
                        }

                        lastX = e.getSceneX();
                        lastY = e.getSceneY();
                });

                view.setOnScroll(e -> {
                        // Scroll: Zoom
                        double delta = e.getDeltaY();
                        view.getGameApp().zoomCamera((float) delta * 0.05f);
                });

                view.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                                view.getGameApp().resetCamera();
                        }
                });
        }

        /**
         * Start update loop for local simulation visualization (minimap, pheromones).
         */
        private void startLocalSimulationUpdates() {
                if (localSimulation == null)
                        return;

                javafx.animation.AnimationTimer updateTimer = new javafx.animation.AnimationTimer() {
                        private long lastUpdate = 0;

                        @Override
                        public void handle(long now) {
                                // Update ~10 times per second
                                if (now - lastUpdate < 100_000_000)
                                        return;
                                lastUpdate = now;

                                if (localSimulation == null)
                                        return;

                                // Update minimap
                                if (minimapOverlay != null) {
                                        minimapOverlay.update(localSimulation);
                                }

                                // Update pheromone overlay (extract 2D slice at ground level)
                                if (pheromoneOverlay != null && localSimulation.getPheromoneGrid() != null) {
                                        var grid = localSimulation.getPheromoneGrid();
                                        var terrarium = localSimulation.getTerrarium();
                                        int w = terrarium.getWidth();
                                        int h = terrarium.getHeight();
                                        float[][] pheromoneData = new float[w][h];

                                        // Sample at z=surface (middle of height)
                                        int z = terrarium.getDepth() / 2;
                                        for (int x = 0; x < w; x++) {
                                                for (int y = 0; y < h; y++) {
                                                        pheromoneData[x][y] = grid.read(x, y, z, 0); // Type 0 = FOOD
                                                }
                                        }
                                        pheromoneOverlay.updateData(pheromoneData, w, h);
                                }

                                // Synchronize audio engine with active simulation environmental state
                                if (localSimulation != null) {
                                        boolean isPlaying = simControlPanel != null && simControlPanel.isPlaying();
                                        String weather = localSimulation.getWeather() != null ? localSimulation.getWeather().getCurrentWeatherType() : "Clear";
                                        String season = localSimulation.getSeasonManager() != null ? localSimulation.getSeasonManager().getCurrentSeason().name() : "SUMMER";
                                        boolean hasRiver = localSimulation.getTerrarium() != null && localSimulation.getTerrarium().hasWater();
                                        int popCount = localSimulation.getColonies().stream().mapToInt(c -> c.getLivingIndividuals().size()).sum();
                                        double zoom = simWorldViewer != null ? simWorldViewer.getZoom() : 7.5;
                                        double windSpd = localSimulation.getWeather() != null ? localSimulation.getWeather().getWindSpeed() : 5.0;
                                        double rainRate = localSimulation.getWeather() != null ? localSimulation.getWeather().getRainfallIntensity() : 0.0;

                                        org.swarmforge.client.audio.SimulationAudioManager.getInstance().setWindAndPrecipitation(windSpd, rainRate);
                                        org.swarmforge.client.audio.SimulationAudioManager.getInstance().updateState(
                                                "Forest", weather, season, hasRiver, popCount, zoom, isPlaying && isSim3DFocused()
                                        );
                                }

                                // Update statistics dashboard
                                if (statisticsDashboard != null && !localSimulation.getColonies().isEmpty()) {
                                        var colony = localSimulation.getColonies().get(0);
                                        var stats = new StatisticsDashboard.ColonyStats();
                                        stats.population = colony.getLivingIndividuals().size();
                                        stats.queens = (int) colony.countByCaste(
                                                        org.swarmforge.core.domain.Individual.Caste.QUEEN);
                                        stats.workers = (int) colony.countByCaste(
                                                        org.swarmforge.core.domain.Individual.Caste.WORKER);
                                        stats.soldiers = (int) colony.countByCaste(
                                                        org.swarmforge.core.domain.Individual.Caste.SOLDIER);
                                        stats.food = colony.getFoodStored();
                                        stats.water = colony.getWaterStored();
                                        stats.tickRate = localSimulation.getTicksPerSecond();
                                        stats.simTicks = localSimulation.getTickCount();
                                        statisticsDashboard.update(stats);
                                }
                        }
                };
                updateTimer.start();
        }

        private void generateNest(java.util.Map<String, Object> config) {
                if (lastGeneratedTerrarium == null)
                        return;

                // Assuming LOG is defined elsewhere, e.g., private static final Logger LOG =
                // LoggerFactory.getLogger(MainApp.class);
                // If not, this line would cause a compilation error. For this task, I'll assume
                // it's handled.
                // LOG.info("Generating nest with config: " + config);

                try {
                        int w = lastGeneratedTerrarium.getWidth();
                        int h = lastGeneratedTerrarium.getHeight();

                        org.swarmforge.core.world.NestGenerator.NestType type = org.swarmforge.core.world.NestGenerator.NestType.SIMPLE;
                        String typeStr = config.get("architecture") != null ? (String) config.get("architecture") : (String) config.get("nestType");
                        if (typeStr != null) {
                                String u = typeStr.toUpperCase();
                                if (u.contains("CATHEDRAL") || u.contains("TERMITE")) type = org.swarmforge.core.world.NestGenerator.NestType.CATHEDRAL_MOUND;
                                else if (u.contains("WAX_COMB") || u.contains("HEXAGONAL") || u.contains("BEEHIVE")) type = org.swarmforge.core.world.NestGenerator.NestType.WAX_COMB_HEXAGONAL;
                                else if (u.contains("WAX_POTS") || u.contains("CLUSTER") || u.contains("BOMBUS")) type = org.swarmforge.core.world.NestGenerator.NestType.WAX_POTS_CLUSTER;
                                else if (u.contains("PAPER") || u.contains("PEDUNCULATE") || u.contains("WASPS")) type = org.swarmforge.core.world.NestGenerator.NestType.PAPER_PEDUNCULATE;
                                else if (u.contains("SILK") || u.contains("LEAF") || u.contains("WEAVER")) type = org.swarmforge.core.world.NestGenerator.NestType.ARBOREAL_SILK_LEAF;
                                else if (u.contains("FUNGI") || u.contains("VAULT") || u.contains("LEAFCUTTER") || u.contains("ATTA")) type = org.swarmforge.core.world.NestGenerator.NestType.SUBTERRANEAN_FUNGI_VAULT;
                                else if (u.contains("CARTON")) type = org.swarmforge.core.world.NestGenerator.NestType.CARTON_NEST;
                                else if (u.contains("BAMBOO") || u.contains("STEM")) type = org.swarmforge.core.world.NestGenerator.NestType.BAMBOO_STEM_NEST;
                                else if (u.contains("BIVOUAC")) type = org.swarmforge.core.world.NestGenerator.NestType.BIVOUAC_LIVING_NEST;
                                else if (u.contains("HOLLOW") || u.contains("TRUNK") || u.contains("TREE")) type = org.swarmforge.core.world.NestGenerator.NestType.TREE;
                                else if (u.contains("MOUND") || u.contains("SURFACE_MOUND")) type = org.swarmforge.core.world.NestGenerator.NestType.MOUND;
                                else if (u.contains("MATURE")) type = org.swarmforge.core.world.NestGenerator.NestType.MATURE;
                                else type = org.swarmforge.core.world.NestGenerator.NestType.SIMPLE;
                        }

                        org.swarmforge.core.world.NestGenerator generator = new org.swarmforge.core.world.NestGenerator(
                                        lastGeneratedTerrarium, System.currentTimeMillis());

                        // Apply parameters
                        if (config.containsKey("depth"))
                                generator.maxDepth((int) config.get("depth"));
                        if (config.containsKey("tunnelWidth"))
                                generator.tunnelRadius(((Number) config.get("tunnelWidth")).floatValue());
                        if (config.containsKey("branching"))
                                generator.branchingFactor(((Number) config.get("branching")).intValue());
                        // Chamber distribution
                        if (config.containsKey("chamberDistribution")) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Integer> dist = (java.util.Map<String, Integer>) config
                                                .get("chamberDistribution");
                                generator.setChamberCounts(dist);
                        }

                        // Generate in center
                        int chambers = generator.generate(w / 2, h / 2, 32, type, 1.0f);

                        // Update live simulation colonies' TunnelNetworks & Terrarium carving
                        if (localSimulation != null) {
                                float maxDepthVal = config.containsKey("depth") ? ((Number) config.get("depth")).floatValue() : 50.0f;
                                float tunnelWidthVal = config.containsKey("tunnelWidth") ? ((Number) config.get("tunnelWidth")).floatValue() : 2.0f;
                                String archName = typeStr != null ? typeStr : "BURROW_UNDERGROUND";

                                if (!localSimulation.getColonies().isEmpty()) {
                                        for (org.swarmforge.core.domain.Colony colony : localSimulation.getColonies()) {
                                                if (colony.getTunnelNetwork() != null) {
                                                        colony.getTunnelNetwork().rebuildForArchitecture(
                                                                colony.getNestX(), colony.getNestY(), colony.getNestZ(),
                                                                archName, colony, maxDepthVal, tunnelWidthVal, 1.0f
                                                        );
                                                }
                                        }
                                }
                        }

                        // Refresh view
                        if (gameView != null) {
                                gameView.getGameApp().renderTerrarium(lastGeneratedTerrarium);
                        }

                        if (worldEditorPane != null && localSimulation != null) {
                                worldEditorPane.setSimulation(localSimulation);
                                worldEditorPane.repaintAllViews();
                        }

                        org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Nest generated! (" + chambers + " chambers created)")
                                        .show();

                } catch (Exception e) {
                        org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Generation failed: " + e.getMessage()).show();
                        e.printStackTrace();
                }
        }

        public void navigateToGlossaryTab(String searchTerm) {
                if (this.glossaryTab != null && this.mainTabs != null) {
                        this.mainTabs.getSelectionModel().select(this.glossaryTab);
                        if (searchTerm != null && !searchTerm.isBlank()) {
                                if (this.glossarySearchField != null) {
                                        this.glossarySearchField.setText(searchTerm);
                                }
                                if (this.glossaryCategoryTabPane != null) {
                                        String lowerTerm = searchTerm.toLowerCase();
                                        if (lowerTerm.contains("nest") || lowerTerm.contains("nid") || lowerTerm.contains("wax") || lowerTerm.contains("cire") || lowerTerm.contains("mound") || lowerTerm.contains("arch")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(0);
                                        } else if (lowerTerm.contains("queen") || lowerTerm.contains("reine") || lowerTerm.contains("social") || lowerTerm.contains("king") || lowerTerm.contains("roi") || lowerTerm.contains("nuptial") || lowerTerm.contains("troph") || lowerTerm.contains("stig")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(1);
                                        } else if (lowerTerm.contains("env") || lowerTerm.contains("temp") || lowerTerm.contains("press") || lowerTerm.contains("sol") || lowerTerm.contains("micro") || lowerTerm.contains("phero") || lowerTerm.contains("moist") || lowerTerm.contains("humid")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(2);
                                        } else if (lowerTerm.contains("fsm") || lowerTerm.contains("bdi") || lowerTerm.contains("décision") || lowerTerm.contains("reason") || lowerTerm.contains("quorum") || lowerTerm.contains("bt") || lowerTerm.contains("fuzzy")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(3);
                                        } else if (lowerTerm.contains("subgenual") || lowerTerm.contains("vibration") || lowerTerm.contains("uv") || lowerTerm.contains("autothys") || lowerTerm.contains("arolia") || lowerTerm.contains("mandib") || lowerTerm.contains("olfac") || lowerTerm.contains("hydrocarbon") || lowerTerm.contains("formic") || lowerTerm.contains("metabol") || lowerTerm.contains("polymorph") || lowerTerm.contains("desiccat")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(4);
                                        } else if (lowerTerm.contains("flora") || lowerTerm.contains("aphid") || lowerTerm.contains("prey") || lowerTerm.contains("predat") || lowerTerm.contains("pathogen") || lowerTerm.contains("diapause")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(5);
                                        } else if (lowerTerm.contains("seed") || lowerTerm.contains("moteur") || lowerTerm.contains("divin") || lowerTerm.contains("audio") || lowerTerm.contains("coloni") || lowerTerm.contains("god") || lowerTerm.contains("dt")) {
                                                this.glossaryCategoryTabPane.getSelectionModel().select(6);
                                        }
                                }
                        }
                }
        }

        private Node createGlossaryPaneView() {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                VBox mainBox = new VBox(10);
                mainBox.setPadding(new Insets(0));

                VBox headerVBox = new VBox(6);
                headerVBox.setPadding(new Insets(8, 10, 5, 10));

                HBox headerRow = new HBox(8);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                Label title = new Label();
                title.textProperty().bind(i18n.createStringBinding("glossary.title"));
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                headerRow.getChildren().addAll(title, sp);
                headerVBox.getChildren().addAll(headerRow, new Separator());

                VBox contentBox = new VBox(12);
                contentBox.setPadding(new Insets(10, 15, 15, 15));

                Label subtitle = new Label();
                subtitle.textProperty().bind(i18n.createStringBinding("glossary.subtitle"));
                subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");

                this.glossarySearchField = new TextField();
                this.glossarySearchField.promptTextProperty().bind(i18n.createStringBinding("glossary.search_prompt"));
                this.glossarySearchField.setStyle("-fx-font-size: 13px;");

                this.glossaryCategoryTabPane = new TabPane();
                this.glossaryCategoryTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

                // Section 1: Architectures de Nids
                VBox vNest = new VBox(10); vNest.setPadding(new Insets(15));
                addGlossaryRowKey(vNest, "glossary.entry.nest.wax_comb.title", "glossary.entry.nest.wax_comb.desc", "https://fr.wikipedia.org/wiki/Rayon_de_cire");
                addGlossaryRowKey(vNest, "glossary.entry.nest.wax_pots.title", "glossary.entry.nest.wax_pots.desc", "https://fr.wikipedia.org/wiki/Propolis");
                addGlossaryRowKey(vNest, "glossary.entry.nest.paper.title", "glossary.entry.nest.paper.desc", "https://fr.wikipedia.org/wiki/Gu%C3%AApe");
                addGlossaryRowKey(vNest, "glossary.entry.nest.cathedral.title", "glossary.entry.nest.cathedral.desc", "https://fr.wikipedia.org/wiki/Termiti%C3%A8re");
                addGlossaryRowKey(vNest, "glossary.entry.nest.silk.title", "glossary.entry.nest.silk.desc", "https://fr.wikipedia.org/wiki/Oecophylla");
                addGlossaryRowKey(vNest, "glossary.entry.nest.fungi.title", "glossary.entry.nest.fungi.desc", "https://fr.wikipedia.org/wiki/Atta_(genre)");
                addGlossaryRowKey(vNest, "glossary.entry.nest.carton.title", "glossary.entry.nest.carton.desc", "https://fr.wikipedia.org/wiki/Crematogaster");
                addGlossaryRowKey(vNest, "glossary.entry.nest.bivouac.title", "glossary.entry.nest.bivouac.desc", "https://fr.wikipedia.org/wiki/Fourmi_l%C3%A9gionnaire");
                addGlossaryRowKey(vNest, "glossary.entry.nest.mound.title", "glossary.entry.nest.mound.desc", "https://fr.wikipedia.org/wiki/Formica_rufa");
                addGlossaryRowKey(vNest, "glossary.entry.nest.wood.title", "glossary.entry.nest.wood.desc", "https://fr.wikipedia.org/wiki/Camponotus");
                addGlossaryRowKey(vNest, "glossary.nest.bamboo.title", "glossary.nest.bamboo.desc");
                addGlossaryRowKey(vNest, "glossary.nest.subterranean.title", "glossary.nest.subterranean.desc");
                addGlossaryRowKey(vNest, "glossary.nest.subterranean_lime.title", "glossary.nest.subterranean_lime.desc");
                addGlossaryRowKey(vNest, "glossary.nest.arboreal_carton.title", "glossary.nest.arboreal_carton.desc");
                addGlossaryRowKey(vNest, "glossary.nest.materials.title", "glossary.nest.materials.desc");
                addGlossaryRowKey(vNest, "glossary.nest.stages.title", "glossary.nest.stages.desc");

                // Section 2: Structure Sociale & Reines
                VBox vSocial = new VBox(10); vSocial.setPadding(new Insets(15));
                addGlossaryRowKey(vSocial, "glossary.entry.social.gynic.title", "glossary.entry.social.gynic.desc", "https://fr.wikipedia.org/wiki/Eusocialit%C3%A9");
                addGlossaryRowKey(vSocial, "glossary.entry.social.king.title", "glossary.entry.social.king.desc", "https://fr.wikipedia.org/wiki/Termite");
                addGlossaryRowKey(vSocial, "glossary.entry.social.flight.title", "glossary.entry.social.flight.desc", "https://fr.wikipedia.org/wiki/Vol_nuptial");
                addGlossaryRowKey(vSocial, "glossary.entry.social.inhibition.title", "glossary.entry.social.inhibition.desc", "https://fr.wikipedia.org/wiki/Ph%C3%A9romone");
                addGlossaryRowKey(vSocial, "glossary.entry.social.polycalism.title", "glossary.entry.social.polycalism.desc", "https://fr.wikipedia.org/wiki/Supercolonie");
                addGlossaryRowKey(vSocial, "glossary.social.queen_mode.title", "glossary.social.queen_mode.desc");
                addGlossaryRowKey(vSocial, "glossary.social.trophallaxis.title", "glossary.social.trophallaxis.desc");
                addGlossaryRowKey(vSocial, "glossary.social.polyethism.title", "glossary.social.polyethism.desc");
                addGlossaryRowKey(vSocial, "glossary.social.stigmergy.title", "glossary.social.stigmergy.desc");

                // Section 3: Sol & Géologie SIG
                VBox vEnv = new VBox(10); vEnv.setPadding(new Insets(15));
                addGlossaryRowKey(vEnv, "glossary.entry.env.dim.title", "glossary.entry.env.dim.desc", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_num%C3%A9rique_de_terrain");
                addGlossaryRowKey(vEnv, "glossary.entry.env.depth.title", "glossary.entry.env.depth.desc", "https://fr.wikipedia.org/wiki/Stratigraphie");
                addGlossaryRowKey(vEnv, "glossary.entry.env.res.title", "glossary.entry.env.res.desc", "https://fr.wikipedia.org/wiki/Voxel");
                addGlossaryRowKey(vEnv, "glossary.entry.env.sig.title", "glossary.entry.env.sig.desc", "https://fr.wikipedia.org/wiki/Shuttle_Radar_Topography_Mission");
                addGlossaryRowKey(vEnv, "glossary.entry.env.water.title", "glossary.entry.env.water.desc", "https://fr.wikipedia.org/wiki/Nappe_phr%C3%A9atique");
                addGlossaryRowKey(vEnv, "glossary.entry.env.microclimate.title", "glossary.entry.env.microclimate.desc", "https://fr.wikipedia.org/wiki/Microclimat");
                addGlossaryRowKey(vEnv, "glossary.env.moisture.title", "glossary.env.moisture.desc");
                addGlossaryRowKey(vEnv, "glossary.env.temperature.title", "glossary.env.temperature.desc");
                addGlossaryRowKey(vEnv, "glossary.env.co2.title", "glossary.env.co2.desc");
                addGlossaryRowKey(vEnv, "glossary.env.solar.title", "glossary.env.solar.desc");
                addGlossaryRowKey(vEnv, "glossary.env.magnetic.title", "glossary.env.magnetic.desc");
                addGlossaryRowKey(vEnv, "glossary.env.soil_layers.title", "glossary.env.soil_layers.desc");
                addGlossaryRowKey(vEnv, "glossary.env.pressure.title", "glossary.env.pressure.desc");
                addGlossaryRowKey(vEnv, "glossary.env.trail_pheromones.title", "glossary.env.trail_pheromones.desc");

                // Section 4: Moteurs de Raisonnement & Cognition
                VBox vReasoning = new VBox(10); vReasoning.setPadding(new Insets(15));
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.bt.title", "glossary.entry.reasoning.bt.desc", "https://fr.wikipedia.org/wiki/Arbre_de_comportement");
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.blackboard.title", "glossary.entry.reasoning.blackboard.desc", "https://en.wikipedia.org/wiki/Blackboard_system");
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.fsm.title", "glossary.entry.reasoning.fsm.desc", "https://fr.wikipedia.org/wiki/Automate_%C3%A0_%C3%A9tats_finis");
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.bdi.title", "glossary.entry.reasoning.bdi.desc", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_BDI");
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.fuzzy.title", "glossary.entry.reasoning.fuzzy.desc", "https://fr.wikipedia.org/wiki/Logique_floue");
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.hybrid.title", "glossary.entry.reasoning.hybrid.desc", "https://fr.wikipedia.org/wiki/Syst%C3%A8me_hybride");
                addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.snn.title", "glossary.entry.reasoning.snn.desc", "https://fr.wikipedia.org/wiki/R%C3%A9seau_de_neurones_artificiels");
                addGlossaryRowKey(vReasoning, "glossary.reasoning.nn.title", "glossary.reasoning.nn.desc");
                addGlossaryRowKey(vReasoning, "glossary.reasoning.bulk.title", "glossary.reasoning.bulk.desc");
                addGlossaryRowKey(vReasoning, "glossary.reasoning.quorum.title", "glossary.reasoning.quorum.desc");

                // Section 5: Capteurs & Biomécanique
                VBox vSensors = new VBox(10); vSensors.setPadding(new Insets(15));
                addGlossaryRowKey(vSensors, "glossary.entry.sensors.subgenual.title", "glossary.entry.sensors.subgenual.desc", "https://fr.wikipedia.org/wiki/Organe_subg%C3%A9nual");
                addGlossaryRowKey(vSensors, "glossary.entry.sensors.uv.title", "glossary.entry.sensors.uv.desc", "https://fr.wikipedia.org/wiki/Cataglyphis");
                addGlossaryRowKey(vSensors, "glossary.entry.sensors.mandible.title", "glossary.entry.sensors.mandible.desc", "https://fr.wikipedia.org/wiki/Mandibule_(arthropode)");
                addGlossaryRowKey(vSensors, "glossary.entry.sensors.autothysis.title", "glossary.entry.sensors.autothysis.desc", "https://fr.wikipedia.org/wiki/Colobopsis_explodens");
                addGlossaryRowKey(vSensors, "glossary.entry.sensors.arolia.title", "glossary.entry.sensors.arolia.desc", "https://fr.wikipedia.org/wiki/Tarse_(anatomie)");
                addGlossaryRowKey(vSensors, "glossary.biomech.antennal_olfaction.title", "glossary.biomech.antennal_olfaction.desc");
                addGlossaryRowKey(vSensors, "glossary.species.hydrocarbons.title", "glossary.species.hydrocarbons.desc");
                addGlossaryRowKey(vSensors, "glossary.species.formic_acid.title", "glossary.species.formic_acid.desc");
                addGlossaryRowKey(vSensors, "glossary.species.metabolism.title", "glossary.species.metabolism.desc");
                addGlossaryRowKey(vSensors, "glossary.species.polymorphism.title", "glossary.species.polymorphism.desc");
                addGlossaryRowKey(vSensors, "glossary.species.desiccation.title", "glossary.species.desiccation.desc");

                // Section 6: Espèces Associées, Proies & Pathogènes
                VBox vAccessory = new VBox(10); vAccessory.setPadding(new Insets(15));
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.flora.title", "glossary.entry.accessory.flora.desc", "https://fr.wikipedia.org/wiki/Nectaire");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.aphids.title", "glossary.entry.accessory.aphids.desc", "https://fr.wikipedia.org/wiki/Miellat");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.prey.title", "glossary.entry.accessory.prey.desc", "https://fr.wikipedia.org/wiki/Insecte");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.predator.title", "glossary.entry.accessory.predator.desc", "https://fr.wikipedia.org/wiki/Fourmilion");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.pathogen.title", "glossary.entry.accessory.pathogen.desc", "https://fr.wikipedia.org/wiki/Ophiocordyceps_unilateralis");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.trophic_web.title", "glossary.entry.accessory.trophic_web.desc", "https://fr.wikipedia.org/wiki/R%C3%A9seau_trophique");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.commensalism.title", "glossary.entry.accessory.commensalism.desc", "https://fr.wikipedia.org/wiki/Myrm%C3%A9cophilie");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.myrmecochory.title", "glossary.entry.accessory.myrmecochory.desc", "https://fr.wikipedia.org/wiki/Myrm%C3%A9cochorie");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.parasitoidism.title", "glossary.entry.accessory.parasitoidism.desc", "https://fr.wikipedia.org/wiki/Parasito%C3%AFde");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.diapause.title", "glossary.entry.accessory.diapause.desc", "https://fr.wikipedia.org/wiki/Diapause");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.r0.title", "glossary.entry.accessory.r0.desc", "https://fr.wikipedia.org/wiki/Nombre_reproductif_de_base");
                addGlossaryRowKey(vAccessory, "glossary.entry.accessory.lat.title", "glossary.entry.accessory.lat.desc", "https://fr.wikipedia.org/wiki/Dur%C3%A9e_du_jour");

                // Section 7: Moteur & Contrôles Temps Réel
                VBox vEngine = new VBox(10); vEngine.setPadding(new Insets(15));
                addGlossaryRowKey(vEngine, "glossary.engine.god_mode.title", "glossary.engine.god_mode.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.dt.title", "glossary.engine.dt.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.multi_colony.title", "glossary.engine.multi_colony.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.seed.title", "glossary.engine.seed.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.audio_synth.title", "glossary.engine.audio_synth.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.time_scale.title", "glossary.engine.time_scale.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.view_focus.title", "glossary.engine.view_focus.desc");
                addGlossaryRowKey(vEngine, "glossary.engine.checkpoint.title", "glossary.engine.checkpoint.desc");

                // Sort each section alphabetically by localized entry title
                sortGlossaryVBox(vNest);
                sortGlossaryVBox(vSocial);
                sortGlossaryVBox(vEnv);
                sortGlossaryVBox(vReasoning);
                sortGlossaryVBox(vSensors);
                sortGlossaryVBox(vAccessory);
                sortGlossaryVBox(vEngine);

                Tab tNest = new Tab();
                tNest.textProperty().bind(i18n.createStringBinding("glossary.cat.nest"));
                tNest.setContent(new ScrollPane(vNest));

                Tab tSocial = new Tab();
                tSocial.textProperty().bind(i18n.createStringBinding("glossary.cat.social"));
                tSocial.setContent(new ScrollPane(vSocial));

                Tab tEnv = new Tab();
                tEnv.textProperty().bind(i18n.createStringBinding("glossary.cat.env"));
                tEnv.setContent(new ScrollPane(vEnv));

                Tab tReasoning = new Tab();
                tReasoning.textProperty().bind(i18n.createStringBinding("glossary.cat.reasoning"));
                tReasoning.setContent(new ScrollPane(vReasoning));

                Tab tSensors = new Tab();
                tSensors.textProperty().bind(i18n.createStringBinding("glossary.cat.sensors"));
                tSensors.setContent(new ScrollPane(vSensors));

                Tab tAccessory = new Tab();
                tAccessory.textProperty().bind(i18n.createStringBinding("glossary.cat.accessory"));
                tAccessory.setContent(new ScrollPane(vAccessory));

                Tab tEngine = new Tab();
                tEngine.textProperty().bind(i18n.createStringBinding("glossary.tab.engine", "Engine & Controls"));
                tEngine.setContent(new ScrollPane(vEngine));

                this.glossaryCategoryTabPane.getTabs().addAll(tNest, tSocial, tEnv, tReasoning, tSensors, tAccessory, tEngine);
                VBox.setVgrow(this.glossaryCategoryTabPane, Priority.ALWAYS);

                // Add live search filter listener
                this.glossarySearchField.textProperty().addListener((obs, oldV, newV) -> {
                        String query = newV == null ? "" : newV.toLowerCase().trim();
                        filterVBoxRows(vNest, query);
                        filterVBoxRows(vSocial, query);
                        filterVBoxRows(vEnv, query);
                        filterVBoxRows(vReasoning, query);
                        filterVBoxRows(vSensors, query);
                        filterVBoxRows(vAccessory, query);
                        filterVBoxRows(vEngine, query);
                });

                contentBox.getChildren().addAll(subtitle, this.glossarySearchField, this.glossaryCategoryTabPane);
                VBox.setVgrow(contentBox, Priority.ALWAYS);
                mainBox.getChildren().addAll(headerVBox, contentBox);
                return mainBox;
        }

        private void filterVBoxRows(VBox box, String query) {
                for (Node child : box.getChildren()) {
                        if (child instanceof HBox row) {
                                boolean visible = query.isEmpty();
                                if (!visible) {
                                        for (Node n : row.getChildren()) {
                                                if (n instanceof Label l && l.getText().toLowerCase().contains(query)) {
                                                        visible = true;
                                                        break;
                                                }
                                        }
                                }
                                row.setVisible(visible);
                                row.setManaged(visible);
                        }
                }
        }

        private void sortGlossaryVBox(VBox box) {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                java.text.Collator collator = java.text.Collator.getInstance(i18n.getLocale());
                collator.setStrength(java.text.Collator.PRIMARY);
                java.util.List<Node> list = new java.util.ArrayList<>(box.getChildren());
                list.sort((n1, n2) -> {
                        if (n1 instanceof HBox h1 && n2 instanceof HBox h2) {
                                String t1 = extractRowTitle(h1);
                                String t2 = extractRowTitle(h2);
                                return collator.compare(t1, t2);
                        }
                        return 0;
                });
                box.getChildren().setAll(list);
        }

        private String extractRowTitle(HBox row) {
                for (Node n : row.getChildren()) {
                        if (n instanceof Label l && l.getStyleClass().contains("help-entry-title")) {
                                return l.getText().replaceAll("^•\\s*", "").replaceAll("\\s*:\\s*$", "").trim();
                        }
                }
                return "";
        }

        private void addGlossaryRowKey(VBox box, String titleKey, String descKey) {
                addGlossaryRowKey(box, titleKey, descKey, null);
        }

        private void addGlossaryRowKey(VBox box, String titleKey, String descKey, String wikiUrl) {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                Label t = new Label();
                t.textProperty().bind(javafx.beans.binding.Bindings.concat("• ", i18n.createStringBinding(titleKey), " : "));
                t.getStyleClass().add("help-entry-title");
                t.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-min-width: 240px;");

                Label d = new Label();
                d.textProperty().bind(i18n.createStringBinding(descKey));
                d.getStyleClass().add("help-entry-desc");
                d.setWrapText(true);

                HBox row = new HBox(8, t, d);
                if (wikiUrl != null && !wikiUrl.isBlank()) {
                        Hyperlink wikiLink = new Hyperlink();
                        wikiLink.textProperty().bind(i18n.createStringBinding("glossary.link.wikipedia"));
                        wikiLink.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");
                        wikiLink.setOnAction(e -> {
                                try {
                                        java.awt.Desktop.getDesktop().browse(new java.net.URI(wikiUrl));
                                } catch (Exception ex) {
                                        org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Wikipédia : " + wikiUrl).show();
                                }
                        });
                        row.getChildren().add(wikiLink);
                }
                row.getStyleClass().add("card-pane");
                row.setPadding(new Insets(8));
                HBox.setHgrow(d, Priority.ALWAYS);
                box.getChildren().add(row);
        }

        private void startLocalSimulationLoop() {
                if (simLoopActive) return;
                simLoopActive = true;
                simLoopExecutor.submit(() -> {
                        long lastTime = System.nanoTime();
                        while (simLoopActive) {
                                boolean isConnected = networkClient != null && networkClient.isConnected();
                                boolean isPlaying = simControlPanel != null && simControlPanel.isPlaying();

                                if (isPlaying && !isConnected) {
                                        if (localSimulation == null) {
                                                lastGeneratedTerrarium = new org.swarmforge.core.domain.Terrarium(64, 32, 64);
                                                localSimulation = new org.swarmforge.core.simulation.Simulation(lastGeneratedTerrarium);
                                                localSimulation.addColony("LasiusNiger");
                                                javafx.application.Platform.runLater(() -> {
                                                        if (gameView != null && gameView.getGameApp() != null) {
                                                                gameView.getGameApp().setSimulation(localSimulation);
                                                        }
                                                        if (simWorldViewer != null) {
                                                                simWorldViewer.setSimulation(localSimulation);
                                                        }
                                                });
                                        }
                                        float speed = simControlPanel != null ? simControlPanel.getSpeedMultiplier() : 1.0f;
                                        if (speed >= 100.0f) {
                                                // Ultra-Fast MAX Speed Mode: batch multiple ticks with micro-sleeps to keep UI 100% smooth
                                                int ticksPerBatch = 20;
                                                for (int b = 0; b < ticksPerBatch && isPlaying && !isConnected; b++) {
                                                        localSimulation.tick();
                                                }
                                                try {
                                                        Thread.sleep(1);
                                                } catch (InterruptedException e) {
                                                        Thread.currentThread().interrupt();
                                                        break;
                                                }
                                                lastTime = System.nanoTime();
                                        } else {
                                                localSimulation.tick();

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

                                        // Evaluate Simulation Auto-Stop Conditions (Minimum Population & Max Ticks)
                                        if (localSimulation != null && simControlPanel != null) {
                                                long curTick = localSimulation.getTickCount();
                                                int totalPop = 0;
                                                if (localSimulation.getColonies() != null) {
                                                        for (org.swarmforge.core.domain.Colony col : localSimulation.getColonies()) {
                                                                totalPop += col.getPopulation();
                                                        }
                                                }
                                                int minPopStop = simControlPanel.getMinPopulationStopThreshold();
                                                long maxTicks = simControlPanel.getMaxSimulationTicks();

                                                if (curTick > 0 && totalPop <= minPopStop) {
                                                        int finalPop = totalPop;
                                                        javafx.application.Platform.runLater(() -> {
                                                                if (simControlPanel.isPlaying()) {
                                                                        simControlPanel.pauseSimulation();
                                                                        org.swarmforge.client.util.NotificationOverlay.show(
                                                                                simSubTabs != null ? simSubTabs : simControlPanel,
                                                                                "🛑 SIMULATION INTERROMPUE : La population totale est tombée à " + finalPop + " (seuil minimal d'arrêt : " + minPopStop + ").",
                                                                                org.swarmforge.client.util.NotificationOverlay.NotificationType.WARNING,
                                                                                true
                                                                        );
                                                                }
                                                        });
                                                } else if (maxTicks > 0 && curTick >= maxTicks) {
                                                        long finalMaxTicks = maxTicks;
                                                        javafx.application.Platform.runLater(() -> {
                                                                if (simControlPanel.isPlaying()) {
                                                                        simControlPanel.pauseSimulation();
                                                                        org.swarmforge.client.util.NotificationOverlay.show(
                                                                                simSubTabs != null ? simSubTabs : simControlPanel,
                                                                                "⏱️ SIMULATION TERMINÉE : La durée maximale de " + finalMaxTicks + " pas de temps (ticks) a été atteinte.",
                                                                                org.swarmforge.client.util.NotificationOverlay.NotificationType.INFO,
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

        public static void main(String[] args) {
                launch(args);
        }
}
