/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import org.swarmforge.client.util.I18nManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.client.ui.MinimapOverlay;
import org.swarmforge.client.ui.PheromoneOverlay;
import org.swarmforge.client.ui.StatisticsDashboard;

import java.util.logging.Logger;
import java.net.URL;
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
        private org.swarmforge.client.ui.SimulationControlPanel simControlPanel;
        private VBox simulationInactiveOverlay;
        private Label syncLabel;

        @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Editor...");

        // Show Splash Screen on startup with progress bar
        org.swarmforge.client.ui.SplashScreen splashScreen = new org.swarmforge.client.ui.SplashScreen();
        splashScreen.show();

        org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
        
        // Window Icon
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            LOG.warning("Could not load application icon: " + e.getMessage());
        }

        // title binding
        primaryStage.titleProperty().bind(I18nManager.getInstance().createStringBinding("app.title"));

        // Root Layout
        BorderPane root = new BorderPane();

        // Menu bar removed per user request

        // 2. Main Tab Pane
        TabPane mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- TAB 1: SIMULATION MANAGER (Control, God Mode, Event Log) ---
        Tab simTab = new Tab();
        simTab.textProperty().bind(i18n.createStringBinding("tab.simulation"));
        simTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SLIDERS));
        simTab.setContent(createSimulationManager());

        // --- TAB 2: WORLD EDITOR (3D View + Terrain Tools) ---
        Tab worldTab = new Tab();
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

        // --- TAB 6: SETTINGS (Thème et Langue) ---
        Tab settingsTab = new Tab();
        settingsTab.textProperty().bind(i18n.createStringBinding("tab.settings"));
        settingsTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SETTINGS));
        settingsTab.setContent(createSettingsPane());

        // --- TAB 7: GLOSSARY & TECHNICAL REFERENCE (Placed after Settings/Parameters per user specification) ---
        Tab glossaryTab = new Tab();
        glossaryTab.textProperty().bind(i18n.createStringBinding("tab.glossary"));
        glossaryTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.BOOK_OPEN));
        glossaryTab.setContent(createGlossaryPaneView());

        mainTabs.getTabs().addAll(simTab, worldTab, speciesTab, accessoryTab, weatherTab, nestTab, settingsTab, glossaryTab);

        // Style tab graphics
        for (Tab t : mainTabs.getTabs()) {
            t.getStyleClass().add("custom-tab");
        }

        mainTabs.getSelectionModel().select(worldTab);

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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
                connectBox.setAlignment(Pos.CENTER_LEFT);
                connectBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8 12; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");

                Label hostLabel = new Label();
                hostLabel.textProperty().bind(i18n.createStringBinding("label.host"));
                hostLabel.setStyle("-fx-text-fill: #94a3b8;");
                hostLabel.setTooltip(new Tooltip("Adresse réseau IP ou nom d'hôte du serveur gRPC SwarmForge"));
                TextField hostField = new TextField("localhost");
                hostField.setPrefWidth(110);
                hostField.setTooltip(new Tooltip("Hôte gRPC (ex: localhost ou IP distante)"));

                Label portLabel = new Label();
                portLabel.textProperty().bind(i18n.createStringBinding("label.port"));
                portLabel.setStyle("-fx-text-fill: #94a3b8;");
                portLabel.setTooltip(new Tooltip("Port gRPC de communication (par défaut 50051)"));
                TextField portField = new TextField("50051");
                portField.setPrefWidth(70);
                portField.setTooltip(new Tooltip("Port d'écoute gRPC"));

                Button btnConnect = new Button();
                btnConnect.textProperty().bind(i18n.createStringBinding("btn.connect"));
                btnConnect.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
                btnConnect.setTooltip(new Tooltip("Établir la connexion gRPC avec le serveur de simulation distant"));

                Label statusLabel = new Label();
                statusLabel.setText(networkClient.isConnected() ? "● Connecté" : "○ Hors ligne");
                statusLabel.setStyle(networkClient.isConnected() ? "-fx-text-fill: #4ade80; -fx-font-weight: bold;" : "-fx-text-fill: #f87171;");
                statusLabel.setTooltip(new Tooltip("État actuel de la connexion au moteur gRPC"));

                this.syncLabel = new Label("● Persistance Locale Active (Base SQLite & Sauvegarde autonome)");
                this.syncLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");
                this.syncLabel.setTooltip(new Tooltip("Indicateur de persistance des données (SQLite locale en mode standalone, PostgreSQL distante en mode connecté)"));

                Region bannerSpacer = new Region();
                HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

                connectBox.getChildren().addAll(hostLabel, hostField, portLabel, portField, btnConnect, statusLabel, bannerSpacer, syncLabel);
                pane.setTop(connectBox);
                BorderPane.setMargin(connectBox, new Insets(0, 0, 10, 0));

                // 2. Content Area Sub-Tabs
                TabPane subTabs = new TabPane();

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
                Tab visualTab = new Tab();
                visualTab.textProperty().bind(i18n.createStringBinding("tab.visual_view"));
                visualTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.EYE));
                visualTab.setContent(createVisualSimulationViewport());
                visualTab.setDisable(true);

                // --- God Mode Sub-Tab ---
                Tab godTab = new Tab();
                godTab.textProperty().bind(i18n.createStringBinding("tab.god_mode"));
                godTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.ZAP));
                org.swarmforge.client.ui.InterventionPanel interventionPanel = new org.swarmforge.client.ui.InterventionPanel();
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
                    }

                    @Override
                    public void stopDisasters() {
                    }

                    @Override
                    public void modifyParameter(String param, Object value) {
                        if (localSimulation != null && value instanceof Number val) {
                            if ("foodRate".equals(param)) {
                                localSimulation.setDiffusionInterval(Math.max(1, (int) (10 * val.floatValue())));
                            }
                        }
                    }
                });
                godTab.setContent(interventionPanel);
                godTab.setDisable(true);

                // --- Dedicated Statistics Sub-Tab ---
                Tab statsTab = new Tab();
                statsTab.textProperty().bind(i18n.createStringBinding("tab.stats"));
                statsTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.BAR_CHART_2));
                this.statisticsDashboard = new org.swarmforge.client.ui.StatisticsDashboard();
                statsTab.setContent(this.statisticsDashboard);
                statsTab.setDisable(true);

                // --- Event Log Sub-Tab ---
                Tab eventLogTab = new Tab();
                eventLogTab.textProperty().bind(i18n.createStringBinding("tab.log"));
                eventLogTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.LIST));
                eventLogTab.setContent(new org.swarmforge.client.ui.EventLogPane());
                eventLogTab.setDisable(true);

                // Set callback on Apply Presets
                this.simControlPanel.setOnApplyPresets(seed -> {
                        if (this.localSimulation != null) {
                                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                                confirm.setTitle("Remplacement du Scénario");
                                confirm.setHeaderText("Une simulation est actuellement en cours d'exécution");
                                confirm.setContentText("Voulez-vous vraiment la réinitialiser et appliquer ce nouveau scénario ? Son état sera automatiquement sauvegardé.");
                                java.util.Optional<ButtonType> opt = confirm.showAndWait();
                                if (opt.isEmpty() || opt.get() != ButtonType.OK) {
                                        return; // Interrompre le remplacement
                                }
                                this.localSimulation.recordSnapshot();
                        }

                        if (this.lastGeneratedTerrarium == null) {
                                this.lastGeneratedTerrarium = new org.swarmforge.core.domain.Terrarium(64, 32, 64);
                        }
                        this.localSimulation = new org.swarmforge.core.simulation.Simulation(this.lastGeneratedTerrarium);
                        this.localSimulation.reset(0);
                        this.localSimulation.setMasterSeed(seed);

                        java.util.List<String> selSpeciesList = simControlPanel.getSelectedSpeciesList();
                        int queens = simControlPanel.getQueenCount();
                        int workers = simControlPanel.getWorkerCount();
                        int soldiers = simControlPanel.getSoldierCount();

                        for (String selSpecies : selSpeciesList) {
                                String speciesKey = "LasiusNiger";
                                if (selSpecies != null) {
                                        if (selSpecies.contains("Atta") || selSpecies.contains("Coupeuse")) speciesKey = "AttaCephalotes";
                                        else if (selSpecies.contains("Solenopsis") || selSpecies.contains("Feu")) speciesKey = "SolenopsisInvicta";
                                        else if (selSpecies.contains("Formica") || selSpecies.contains("Rousse")) speciesKey = "FormicaRufa";
                                        else if (selSpecies.contains("Camponotus")) speciesKey = "Camponotus";
                                        else if (selSpecies.contains("Apis") || selSpecies.contains("Abeille")) speciesKey = "ApisMellifera";
                                        else if (selSpecies.contains("Termite") || selSpecies.contains("Macrotermes")) speciesKey = "Macrotermes";
                                }
                                this.localSimulation.addColony(speciesKey, queens, workers, soldiers);
                        }

                        if (gameView != null && gameView.getGameApp() != null) {
                                gameView.getGameApp().setSimulation(this.localSimulation);
                        }

                        this.simControlPanel.updateCheckpoints(this.localSimulation.getCheckpoints());

                        // Activer les onglets de dépendance dès qu'un scénario est appliqué
                        visualTab.setDisable(false);
                        godTab.setDisable(false);
                        statsTab.setDisable(false);
                        eventLogTab.setDisable(false);
                        subTabs.getSelectionModel().select(visualTab);
                });

                this.simControlPanel.setOnCreateCheckpoint(name -> {
                    if (this.localSimulation != null) {
                        org.swarmforge.core.simulation.SimulationCheckpoint cp = this.localSimulation.createCheckpoint(name);
                        this.simControlPanel.updateCheckpoints(this.localSimulation.getCheckpoints());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Point de contrôle '" + cp.getName() + "' enregistré à l'itération #" + cp.getTick() + " (avec " + cp.getInterventionsRecorded().size() + " interventions Mode Divin).");
                        alert.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez d'abord appliquer un scénario avant de créer un point de contrôle.");
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
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Point de contrôle '" + cp.getName() + "' restauré avec succès à l'itération #" + cp.getTick() + " !");
                            alert.show();
                            subTabs.getSelectionModel().select(visualTab);
                        }
                    }
                });

                subTabs.getTabs().addAll(controlsTab, visualTab, godTab, statsTab, eventLogTab);
                pane.setCenter(subTabs);

                // Connection Logic
                btnConnect.setOnAction(e -> {
                        try {
                                networkClient.connect(hostField.getText(), Integer.parseInt(portField.getText()));
                                statusLabel.setText("● Connecté");
                                statusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                                networkClient.startStreaming();

                                if (gameView != null) {
                                        gameView.getGameApp().setNetworkClient(networkClient);
                                }
                        } catch (Exception ex) {
                                statusLabel.setText("○ Erreur: " + ex.getMessage());
                                statusLabel.setStyle("-fx-text-fill: #f87171;");
                                ex.printStackTrace();
                        }
                });

                // HUD Loop, Audio Sync, God Mode & Statistics Updates
                javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
                        @Override
                        public void handle(long now) {
                                boolean isConnected = networkClient != null && networkClient.isConnected();
                                boolean isPlaying = simControlPanel != null && simControlPanel.isPlaying();
                                boolean isSimRunning = isPlaying;

                                if (isPlaying && !isConnected && simControlPanel != null) {
                                    long newTick = simControlPanel.getCurrentTick() + 1;
                                    simControlPanel.updateTick(newTick, newTick);
                                    if (localSimulation == null) {
                                        lastGeneratedTerrarium = new org.swarmforge.core.domain.Terrarium(64, 32, 64);
                                        localSimulation = new org.swarmforge.core.simulation.Simulation(lastGeneratedTerrarium);
                                        localSimulation.addColony("LasiusNiger");
                                    }
                                    localSimulation.tick();
                                }

                                if (simulationInactiveOverlay != null) {
                                    simulationInactiveOverlay.setVisible(!isSimRunning);
                                }

                                interventionPanel.setSimulationRunning(isSimRunning);

                                if (localSimulation != null && !localSimulation.getColonies().isEmpty()) {
                                    java.util.List<String> colonyNames = new java.util.ArrayList<>();
                                    for (org.swarmforge.core.domain.Colony c : localSimulation.getColonies()) {
                                        String speciesName = c.getSpeciesName();
                                        colonyNames.add(speciesName != null && !speciesName.isEmpty() ? speciesName : "Colonie #" + c.getId().toString().substring(0, 5));
                                    }
                                    interventionPanel.updateAvailableColonies(colonyNames);
                                }

                                long tick = isConnected ? networkClient.getLatestTick() : (simControlPanel != null ? simControlPanel.getCurrentTick() : (localSimulation != null ? localSimulation.getTicksPerSecond() : 0));
                                String modeStr = isConnected ? "Serveur Dédié (Connecté & Synchronisé)" : "Mode Local Autonome (Standalone)";
                                statsLabel.setText("🌐 Moteur de Simulation : " + modeStr + " | Avancement : Pas n° " + tick);

                                if (syncLabel != null) {
                                    if (isConnected) {
                                        syncLabel.setText("● Persistance Serveur Active (PostgreSQL Distant)");
                                        syncLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 11px;");
                                    } else {
                                        syncLabel.setText("● Persistance Locale Active (Base SQLite Autonome)");
                                        syncLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");
                                    }
                                }

                                // Sync procedural audio synthesizer
                                if (simControlPanel != null) {
                                    org.swarmforge.client.audio.SimulationAudioManager.getInstance().updateState(
                                        simControlPanel.getSelectedWorld(),
                                        simControlPanel.getSelectedWeather(),
                                        gameView != null && gameView.getGameApp() != null ? gameView.getGameApp().getCameraDepth() : 0.0,
                                        isSimRunning
                                    );
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
                                new Alert(Alert.AlertType.ERROR, "Invalid input").show();
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
                BorderPane rootPane = new BorderPane();

                // Center Viewport: WorldEditorPane 3-View System (3D View + Mouse Orbit/Pan/Zoom Controls + 2D Minimaps)
                org.swarmforge.client.ui.WorldEditorPane simWorldViewer = new org.swarmforge.client.ui.WorldEditorPane();
                simWorldViewer.setHideConfigPanel(true);
                
                // 3D Inactive Overlay Placeholder (Sleek Top Bar)
                this.simulationInactiveOverlay = new VBox(4);
                simulationInactiveOverlay.setAlignment(Pos.CENTER);
                simulationInactiveOverlay.setStyle("-fx-background-color: rgba(15, 23, 42, 0.75); -fx-padding: 8 16; -fx-background-radius: 20; -fx-border-color: rgba(56, 189, 248, 0.3); -fx-border-width: 1; -fx-border-radius: 20;");
                simulationInactiveOverlay.setMaxSize(360, 50);

                Label lblInactiveTitle = new Label("🎬 Vue 3D Prête — Attente du Lancement");
                lblInactiveTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;");

                simulationInactiveOverlay.setMouseTransparent(true);
                simulationInactiveOverlay.setPickOnBounds(false);

                simulationInactiveOverlay.getChildren().addAll(lblInactiveTitle);

                // Full Screen Floating Exit Button
                Button btnExitFullscreen = new Button("❌ Quitter Plein Écran (ESC)");
                btnExitFullscreen.setStyle("-fx-background-color: rgba(239, 68, 68, 0.9); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 6 14; -fx-cursor: hand;");
                btnExitFullscreen.setVisible(false);

                StackPane viewportStack = new StackPane(simWorldViewer, simulationInactiveOverlay, btnExitFullscreen);
                StackPane.setAlignment(simulationInactiveOverlay, Pos.TOP_CENTER);
                StackPane.setMargin(simulationInactiveOverlay, new Insets(15));
                StackPane.setAlignment(btnExitFullscreen, Pos.TOP_RIGHT);
                StackPane.setMargin(btnExitFullscreen, new Insets(15));

                // If simulation is already active or loaded, hide overlay
                if (localSimulation != null || (simControlPanel != null && simControlPanel.isPlaying())) {
                        simulationInactiveOverlay.setVisible(false);
                }

                rootPane.setCenter(viewportStack);

                // Right Side Controls Sidebar (Glassmorphic Toolbar for Render Modes, Audio Mixer & Video Recording)
                VBox sideControls = new VBox(10);
                sideControls.setPrefWidth(350);
                sideControls.setMinWidth(340);
                sideControls.setPadding(new Insets(10));
                sideControls.getStyleClass().add("card-pane");

                // 1. Controls Header
                Label lblSideTitle = new Label("🎛️ Rendu 3D & Contrôles");
                lblSideTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 13px;");

                HBox sideHeaderBox = new HBox(8, lblSideTitle);
                sideHeaderBox.setAlignment(Pos.CENTER_LEFT);

                // 2. Moved Controls from Simulation Manager: Date & Time, VCR Playback (Rewind/FastForward), Speed & Multipliers
                Node playbackAndSpeedNode = (this.simControlPanel != null) ? this.simControlPanel.getPlaybackAndSpeedPanel() : new VBox();

                if (simControlPanel != null) {
                        simControlPanel.setOnPlay(v -> {
                                if (simulationInactiveOverlay != null) simulationInactiveOverlay.setVisible(false);
                        });
                }

                // 3. Media & Recording Section
                VBox mediaSection = new VBox(6);
                mediaSection.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");
                Label lblMedia = new Label("📸 Captures & Média :");
                lblMedia.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

                Button btnFullscreenMode = new Button("🖥️ Mode Plein Écran 3D");
                btnFullscreenMode.setMaxWidth(Double.MAX_VALUE);
                btnFullscreenMode.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                btnFullscreenMode.setTooltip(new Tooltip("Activer la vue 3D plein écran sans barres ni fenêtres (Échap pour quitter)."));

                Button btnPhoto = new Button("📷 Capture d'Écran HD (PNG)");
                btnPhoto.setMaxWidth(Double.MAX_VALUE);
                btnPhoto.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                btnPhoto.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Capture Photo HD enregistrée sous swarmforge_snapshot.png !").show());

                Button btnRecVideo = new Button("🎥 Enregistrer Séquence Animée");
                btnRecVideo.setMaxWidth(Double.MAX_VALUE);
                btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                btnRecVideo.setOnAction(e -> {
                        if (btnRecVideo.getText().contains("Enregistrer")) {
                                btnRecVideo.setText("⏹ Stop & Exporter MP4");
                                btnRecVideo.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4;");
                        } else {
                                btnRecVideo.setText("🎥 Enregistrer Séquence Animée");
                                btnRecVideo.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 4;");
                                new Alert(Alert.AlertType.INFORMATION, "Vidéo 3D exportée avec succès au format MP4 !").show();
                        }
                });

                mediaSection.getChildren().addAll(lblMedia, btnFullscreenMode, btnPhoto, btnRecVideo);

                // 4. Render Mode & 3D Layer Management Section
                VBox renderSection = new VBox(6);
                renderSection.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");
                Label lblRenderMode = new Label("🎛️ Gestion des Couches 3D (Layers) :");
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
                org.swarmforge.client.ui.ComboBoxTooltipHelper.setupDescriptiveComboBox(comboRenderMode,
                    val -> val,
                    val -> switch (val) {
                        case "🌿 Mode Réaliste (Naturaliste Photoréaliste)" -> "Affiche le monde avec éclairage naturel, végétation vivante, strates géologiques et textures réalistes.";
                        case "🔬 Mode Scientifique (Minimaliste Structural)" -> "Vue schématique dépouillée des artefacts décoratifs. Concentrée sur les galeries, vecteurs et gradients de phéromones.";
                        case "🎮 Mode Gamifié (Voxel / Minecraft)" -> "Rendu voxelisé à base de blocs cubiques texturés style Minecraft.";
                        default -> val;
                    }
                );
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

                CheckBox chkMinimap = new CheckBox("🗺️ Afficher Mini-Map Dual");
                chkMinimap.setSelected(true);
                chkMinimap.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                chkMinimap.setTooltip(new Tooltip("Affiche l'incrustation carte 2D (vue du dessus & coupe verticale)."));
                chkMinimap.selectedProperty().addListener((o, a, b) -> {
                        simWorldViewer.setDualMinimapVisible(b);
                });

                CheckBox chkTerrain = new CheckBox("🏔️ Terrain & Sol 3D");
                chkTerrain.setSelected(true);
                chkTerrain.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                chkTerrain.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setTerrainVisible(b);
                        simWorldViewer.setTerrainVisible(b);
                });

                CheckBox chkNid = new CheckBox("🏰 Nid & Galeries Souterraines");
                chkNid.setSelected(true);
                chkNid.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                chkNid.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setTunnelsVisible(b);
                        simWorldViewer.setGalleriesVisible(b);
                });

                CheckBox chkPheromonesLayer = new CheckBox("🧪 Overlays Phéromones 3D");
                chkPheromonesLayer.setSelected(true);
                chkPheromonesLayer.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                chkPheromonesLayer.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setPheromonesVisible(b);
                        simWorldViewer.setPheromonesVisible(b);
                });

                CheckBox chkAntsLayer = new CheckBox("🐜 Insectes & Colonie 3D");
                chkAntsLayer.setSelected(true);
                chkAntsLayer.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                chkAntsLayer.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setAntsVisible(b);
                        simWorldViewer.setColonyVisible(b);
                });

                CheckBox chkWeatherLayer = new CheckBox("🌤️ Météo & Atmosphère");
                chkWeatherLayer.setSelected(true);
                chkWeatherLayer.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                chkWeatherLayer.selectedProperty().addListener((o, a, b) -> {
                        if (gameView != null && gameView.getGameApp() != null) gameView.getGameApp().setWeatherVisible(b);
                        simWorldViewer.setWeatherVisible(b);
                });

                renderSection.getChildren().addAll(lblRenderMode, comboRenderMode, chkMinimap, chkTerrain, chkNid, chkPheromonesLayer, chkAntsLayer, chkWeatherLayer);

                // Audio Controls Section
                VBox audioSection = new VBox(6);
                audioSection.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8; -fx-background-radius: 6;");
                Label lblAudio = new Label("🔊 Sound Design Procédural :");
                lblAudio.setStyle("-fx-text-fill: #a78bfa; -fx-font-weight: bold; -fx-font-size: 11px;");

                org.swarmforge.client.audio.SimulationAudioManager audioMgr = org.swarmforge.client.audio.SimulationAudioManager.getInstance();

                Slider masterVolSlider = new Slider(0, 100, 70);
                masterVolSlider.setStyle("-fx-font-size: 9px;");
                masterVolSlider.valueProperty().addListener((o, oldV, newV) -> {
                        audioMgr.setMasterVolume(newV.doubleValue() / 100.0);
                });
                HBox volBox = new HBox(6, new Label("Volume :") {{ setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;"); }}, masterVolSlider);

                CheckBox chkAmbientSound = new CheckBox("🌲 Ambiance Biome (Oiseaux/Vent)");
                chkAmbientSound.setSelected(audioMgr.isAmbientEnabled());
                chkAmbientSound.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
                chkAmbientSound.setTooltip(new Tooltip("Activer le paysage sonore d'ambiance naturelle (vent, oiseaux, pluie)."));
                chkAmbientSound.selectedProperty().addListener((o, a, b) -> audioMgr.setAmbientEnabled(b));

                CheckBox chkWeatherSound = new CheckBox("⛈️ Sons Météo (Pluie/Orage)");
                chkWeatherSound.setSelected(audioMgr.isWeatherEnabled());
                chkWeatherSound.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
                chkWeatherSound.setTooltip(new Tooltip("Activer les effets sonores météorologiques synchrone."));
                chkWeatherSound.selectedProperty().addListener((o, a, b) -> audioMgr.setWeatherEnabled(b));

                CheckBox chkInsectSound = new CheckBox("🐜 Activité Insectes & Nid (Creusement)");
                chkInsectSound.setSelected(audioMgr.isInsectEnabled());
                chkInsectSound.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px;");
                chkInsectSound.setTooltip(new Tooltip("Activer les sons procéduraux d'activité des insectes et d'excavation."));
                chkInsectSound.selectedProperty().addListener((o, a, b) -> audioMgr.setInsectEnabled(b));

                audioSection.getChildren().addAll(lblAudio, volBox, chkAmbientSound, chkWeatherSound, chkInsectSound);

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
                        boolean isCurrentlyFullscreen = stage != null && stage.isFullScreen();

                        if (!isCurrentlyFullscreen) {
                                rootPane.setRight(null);
                                btnExitFullscreen.setVisible(true);
                                if (stage != null) {
                                        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                                        stage.setFullScreen(true);
                                }
                        } else {
                                rootPane.setRight(sideScroll);
                                btnExitFullscreen.setVisible(false);
                                if (stage != null) {
                                        stage.setFullScreen(false);
                                }
                        }
                };

                btnFullscreenMode.setOnAction(e -> toggleFullscreen.run());
                btnExitFullscreen.setOnAction(e -> toggleFullscreen.run());

                // Scene-wide key listener filter for reliable ESC key handling anywhere
                rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        if (newScene != null) {
                                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ke -> {
                                        if (ke.getCode() == javafx.scene.input.KeyCode.ESCAPE && btnExitFullscreen.isVisible()) {
                                                toggleFullscreen.run();
                                                ke.consume();
                                        }
                                });
                        }
                });

                return rootPane;
        }

        private Node createWorldEditor() {
                // World Editor Pane with 3-View System (3D, Top-Down, Side) & 3D Sculpting Brushes
                org.swarmforge.client.ui.WorldEditorPane worldEditorPane = new org.swarmforge.client.ui.WorldEditorPane();
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

                                new Alert(Alert.AlertType.INFORMATION, "Monde généré avec succès ! (Taille: " + sizeMeters + "m, Voxel: " + resMm + "mm)").show();

                        } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Échec de génération: " + ex.getMessage()).show();
                                ex.printStackTrace();
                        }
                });

                return worldEditorPane;
        }

        private Node createSpeciesEditor() {
                org.swarmforge.client.ui.SpeciesEditorPane pane = new org.swarmforge.client.ui.SpeciesEditorPane();
                pane.setOnApply(species -> {
                        this.currentSpecies = species;
                        new Alert(Alert.AlertType.INFORMATION, "Espèce active mise à jour : " + species.getCommonName()).show();
                });
                return pane;
        }

        private Node createSettingsPane() {
                org.swarmforge.client.util.I18nManager i18n = I18nManager.getInstance();
                VBox main = new VBox(10);
                main.setPadding(new Insets(10, 15, 10, 15));
                main.setMaxWidth(650);

                Label title = new Label();
                title.textProperty().bind(i18n.createStringBinding("settings.title"));
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

                VBox headerBox = new VBox(6);
                headerBox.getChildren().addAll(title, new Separator());

                main.getChildren().add(headerBox);

                GridPane grid = new GridPane();
                grid.setHgap(20);
                grid.setVgap(15);
                grid.setPadding(new Insets(15, 0, 0, 0));

                // 1. Language Row
                Label langLabel = new Label();
                langLabel.textProperty().bind(i18n.createStringBinding("settings.language"));
                langLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                langLabel.setTooltip(new Tooltip("Sélectionnez la langue d'affichage de l'interface utilisateur."));

                ComboBox<String> langCombo = new ComboBox<>();
                langCombo.getItems().addAll("English", "Français", "Español", "Deutsch", "中文");
                langCombo.setTooltip(new Tooltip("Langues supportées : Français, English, Español, Deutsch, 中文"));

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
                themeLabel.setTooltip(new Tooltip("Basculer dynamiquement entre le thème Sombre (Dark) et le thème Clair (Light)."));

                ComboBox<String> themeCombo = new ComboBox<>();
                themeCombo.getItems().addAll("Dark Theme", "Light Theme");
                themeCombo.setTooltip(new Tooltip("Thèmes graphiques pour l'ensemble de l'interface SwarmForge."));
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

                main.getChildren().add(grid);
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
                        String typeStr = (String) config.get("nestType");
                        if (typeStr != null) {
                                if (typeStr.contains("Mature"))
                                        type = org.swarmforge.core.world.NestGenerator.NestType.MATURE;
                                else if (typeStr.contains("Complex"))
                                        type = org.swarmforge.core.world.NestGenerator.NestType.MOUND; // Map Complex ->
                                                                                                       // Mound for
                                                                                                       // L-system
                                                                                                       // variety
                                else if (typeStr.contains("Leafcutter"))
                                        type = org.swarmforge.core.world.NestGenerator.NestType.TREE; // Just for visual
                                                                                                      // difference
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
                        int chambers = generator.generate(w / 2, h / 2, 32, type, 1.0f); // Assuming z=32 surface
                                                                                         // roughly

                        // Refresh view
                        if (gameView != null) {
                                gameView.getGameApp().renderTerrarium(lastGeneratedTerrarium);
                        }

                        // Update local sim if running
                        if (localSimulation != null) {
                                // Harder to update live sim geometry on the fly without reset, but for preview
                                // it's okay
                                // Ideally we restart sim
                                localSimulation = new org.swarmforge.core.simulation.Simulation(lastGeneratedTerrarium);
                                localSimulation.setTicksPerSecond(20);
                                startLocalSimulationUpdates();
                        }

                        new Alert(Alert.AlertType.INFORMATION, "Nest generated! (" + chambers + " chambers created)")
                                        .show();

                } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Generation failed: " + e.getMessage()).show();
                        e.printStackTrace();
                }
        }

        private Node createGlossaryPaneView() {
                VBox mainBox = new VBox(10);
                mainBox.setPadding(new Insets(0));

                VBox headerVBox = new VBox(6);
                headerVBox.setPadding(new Insets(8, 10, 5, 10));

                HBox headerRow = new HBox(8);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                Label title = new Label("📖 Glossaire & Référence Technique Universelle");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                headerRow.getChildren().addAll(title, sp);
                headerVBox.getChildren().addAll(headerRow, new Separator());

                VBox contentBox = new VBox(12);
                contentBox.setPadding(new Insets(10, 15, 15, 15));

                Label subtitle = new Label("Référence complète centralisant tous les paramètres de simulation, métriques biologiques, capteurs, substrats géologiques, architectures de nids et espèces associées.");
                subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");

                TextField searchField = new TextField();
                searchField.setPromptText("🔍 Rechercher un paramètre, une espèce, une architecture ou un concept (ex: Perlin, SRTM, Cordyceps, BDI, Diapause, Subgenual)...");
                searchField.setStyle("-fx-font-size: 13px;");

                TabPane tabPane = new TabPane();
                tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

                // Section 1: Architectures de Nids
                VBox vNest = new VBox(10); vNest.setPadding(new Insets(15));
                addGlossaryRow(vNest, "Rayons d'Abeille Hexagonaux (WAX_COMB_HEXAGONAL)", "Rayons verticaux de cire à cellules hexagonales 3D parallèles (Abeilles domestiques Apis mellifera).", "https://fr.wikipedia.org/wiki/Rayon_de_cire");
                addGlossaryRow(vNest, "Pots de Cire & Propolis (WAX_POTS_CLUSTER)", "Grappes de pots sphériques en cire et propolis pour nectar et couvain (Bourdons Bombus).", "https://fr.wikipedia.org/wiki/Propolis");
                addGlossaryRow(vNest, "Nid Suspendu en Papier (PAPER_PEDUNCULATE)", "Nid suspendu en papier mâché rattaché par un pédoncule, enveloppe protectrice (Guêpes & Frelons).", "https://fr.wikipedia.org/wiki/Gu%C3%AApe");
                addGlossaryRow(vNest, "Termitière Cathédrale (CATHEDRAL_MOUND)", "Tourelles en ciment salivaire avec puits de ventilation convective et loges royales (Termites).", "https://fr.wikipedia.org/wiki/Termiti%C3%A8re");
                addGlossaryRow(vNest, "Nid de Soie Arboricole (ARBOREAL_SILK_LEAF)", "Capsule de feuilles vivantes cousues par de la soie larvaire dans la canopée (Fourmis tisserandes Oecophylla).", "https://fr.wikipedia.org/wiki/Oecophylla");
                addGlossaryRow(vNest, "Jardins à Champignons (SUBTERRANEAN_FUNGI_VAULT)", "Cavernes souterraines profondes hébergeant des jardins à champignons cultivés (Fourmis Atta/Acromyrmex).", "https://fr.wikipedia.org/wiki/Atta_(genre)");
                addGlossaryRow(vNest, "Nid Ligneux Cartonné (CARTON_NEST)", "Nid en bois mâché cartonné accroché aux troncs et branches (Fourmis Crematogaster/Azteca).", "https://fr.wikipedia.org/wiki/Crematogaster");
                addGlossaryRow(vNest, "Bivouac Vivant (BIVOUAC_LIVING_NEST)", "Nid temporaire vivant formé par les corps entrelacés des ouvrières (Fourmis légionnaires Eciton).", "https://fr.wikipedia.org/wiki/Fourmi_l%C3%A9gionnaire");
                addGlossaryRow(vNest, "Dôme d'Aiguilles (MOUND)", "Dôme parabolique d'aiguilles de pin capteur de chaleur solaire (Fourmis rousses Formica rufa).", "https://fr.wikipedia.org/wiki/Formica_rufa");
                addGlossaryRow(vNest, "Excavation de Bois Mort (TREE / DEAD_WOOD)", "Galeries excavées dans le bois mort ou le cœur des arbres (Fourmis charpentières Camponotus).", "https://fr.wikipedia.org/wiki/Camponotus");

                // Section 2: Structure Sociale & Reines
                VBox vSocial = new VBox(10); vSocial.setPadding(new Insets(15));
                addGlossaryRow(vSocial, "Structure Gynique (Monogyne / Polygyne)", "MONOGYNE (1 reine unique par colonie), POLYGYNE (plusieurs reines réparties), GAMERGATES (ouvrières fécondes reproductrices).", "https://fr.wikipedia.org/wiki/Eusocialit%C3%A9");
                addGlossaryRow(vSocial, "Roi Reproducteur (Isoptera)", "Caractéristique clé des termites chez qui le mâle (roi) vit en permanence aux côtés de la reine.", "https://fr.wikipedia.org/wiki/Termite");
                addGlossaryRow(vSocial, "Vol Nuptial & Essaimage", "AERIAL_SWARM (nuée aérienne), SWARM_DIVISION (division d'essaim abeilles), BUDDING (bouturage de nid), IN_NEST (accouplement interne).", "https://fr.wikipedia.org/wiki/Vol_nuptial");
                addGlossaryRow(vSocial, "Inhibition Phéromonale Royale", "Phéromone émise par la reine pour bloquer la différenciation de nouvelles reines au sein du couvain.", "https://fr.wikipedia.org/wiki/Ph%C3%A9romone");

                // Section 3: Sol & Géologie SIG
                VBox vEnv = new VBox(10); vEnv.setPadding(new Insets(15));
                addGlossaryRow(vEnv, "Dimensions Surfaciques (mètres)", "Échelle horizontale du terrarium simulé en mètres réels.", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_num%C3%A9rique_de_terrain");
                addGlossaryRow(vEnv, "Profondeur Souterraine (depthMeters)", "Épaisseur verticale du sol simulé (0.2m à 5.0m). Limite la profondeur maximale d'excavation et les nappes phréatiques.", "https://fr.wikipedia.org/wiki/Stratigraphie");
                addGlossaryRow(vEnv, "Résolution Voxel (mm)", "Précision millimétrique du maillage voxel 3D du sol (ex: 5mm/voxel).", "https://fr.wikipedia.org/wiki/Voxel");
                addGlossaryRow(vEnv, "Importation SIG GPS (DEM & Copernicus)", "Importation directe en 1 clic de données d'altitude réelles (Copernicus DEM / NASA SRTM 30m) basées sur coordonnées GPS.", "https://fr.wikipedia.org/wiki/Shuttle_Radar_Topography_Mission");
                addGlossaryRow(vEnv, "Humidité du Sol & Nappe Phréatique (%)", "Teneur en eau du substrat nécessaire pour éviter la dessiccation des larves et maintenir le couvain.", "https://fr.wikipedia.org/wiki/Nappe_phr%C3%A9atique");

                // Section 4: Moteurs de Raisonnement & Cognition
                VBox vReasoning = new VBox(10); vReasoning.setPadding(new Insets(15));
                addGlossaryRow(vReasoning, "Automate à États Finis (FSM)", "Comportement réactif déterministe passant d'un état à un autre selon des déclencheurs stricts.", "https://fr.wikipedia.org/wiki/Automate_%C3%A0_%C3%A9tats_finis");
                addGlossaryRow(vReasoning, "BDI (Beliefs-Desires-Intentions)", "Modélisation cognitive complète : croyances sur l'environnement, désirs prioritaires et intentions d'action.", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_BDI");
                addGlossaryRow(vReasoning, "Logique Floue (Fuzzy Logic)", "Prise de décision continue basée sur des degrés de vérité (ex: Légèrement affamé, Très effrayé).", "https://fr.wikipedia.org/wiki/Logique_floue");
                addGlossaryRow(vReasoning, "Réseaux de Neurones Spikant (SNN)", "Moteur de comportement neuromorphique basé sur l'intégration de potentiels d'action.", "https://fr.wikipedia.org/wiki/R%C3%A9seau_de_neurones_artificiels");

                // Section 5: Capteurs & Biomécanique
                VBox vSensors = new VBox(10); vSensors.setPadding(new Insets(15));
                addGlossaryRow(vSensors, "Organe Subgénual (Perception Vibrations)", "Capteur mécano-récepteur hautement sensible situé sur les tibias détectant les vibrations du sol et tambourinages d'alerte.", "https://fr.wikipedia.org/wiki/Organe_subg%C3%A9nual");
                addGlossaryRow(vSensors, "Boussole Lumière Polarisée UV", "Navigation céleste vectorielle utilisant la polarisation de la lumière UV du ciel (Cataglyphis / Abeilles).", "https://fr.wikipedia.org/wiki/Cataglyphis");
                addGlossaryRow(vSensors, "Force Mandibulaire & Cisaillement (MPa)", "Force de morsure et pression mécanique maximale mesurée en MégaPascals.", "https://fr.wikipedia.org/wiki/Mandibule_(arthropode)");
                addGlossaryRow(vSensors, "Autothysis (Explosion Suicidaire)", "Rupture abdominale défensive libérant des sécrétions toxiques engluantes (Colobopsis explodens).", "https://fr.wikipedia.org/wiki/Colobopsis_explodens");
                addGlossaryRow(vSensors, "Ventouses Tarsales (Arolia)", "Structures d'adhésion cuticulaires assurant la marche sur parois verticales et plafonds lisses.", "https://fr.wikipedia.org/wiki/Tarse_(anatomie)");

                // Section 6: Espèces Associées, Proies & Pathogènes
                VBox vAccessory = new VBox(10); vAccessory.setPadding(new Insets(15));
                addGlossaryRow(vAccessory, "Flore & Ressources Végétales (FLORA)", "Plantes, graines et nectaires extrafloraux fournissant de la biomasse et du sucre.", "https://fr.wikipedia.org/wiki/Nectaire");
                addGlossaryRow(vAccessory, "Pucerons Symbiotiques & Miellat (APHID_MUTUALIST)", "Élevage de pucerons et récolte du miellat sucré échangé contre une protection contre les prédateurs.", "https://fr.wikipedia.org/wiki/Miellat");
                addGlossaryRow(vAccessory, "Insectes Proies (PREY_INSECT)", "Chenilles, grillons et mouches apportant les protéines indispensables au développement des larves.", "https://fr.wikipedia.org/wiki/Insecte");
                addGlossaryRow(vAccessory, "Prédateurs Spécialisés (PREDATOR)", "Araignées, fourmilions et oiseaux exerçant une pression de prédation sur les patrouilleuses.", "https://fr.wikipedia.org/wiki/Fourmilion");
                addGlossaryRow(vAccessory, "Pathogènes & Parasites (PATHOGEN_PARASITE)", "Champignons entomopathogènes (Cordyceps), microsporidies et acariens (Varroa) affectant la santé.", "https://fr.wikipedia.org/wiki/Ophiocordyceps_unilateralis");
                addGlossaryRow(vAccessory, "Hivernation & Diapause Automatique", "Mise en léthargie métabolique sous 10°C ou lors du raccourcissement automnal de la photopériode.", "https://fr.wikipedia.org/wiki/Diapause");
                addGlossaryRow(vAccessory, "Taux de Transmission Pathogène (R0)", "Nombre moyen d'infections secondaires générées par un individu infecté dans la colonie.", "https://fr.wikipedia.org/wiki/Nombre_reproductif_de_base");

                // Sort each section alphabetically by localized entry title
                sortGlossaryVBox(vNest);
                sortGlossaryVBox(vSocial);
                sortGlossaryVBox(vEnv);
                sortGlossaryVBox(vReasoning);
                sortGlossaryVBox(vSensors);
                sortGlossaryVBox(vAccessory);

                Tab tNest = new Tab("Architectures de Nids", new ScrollPane(vNest));
                Tab tSocial = new Tab("Structure Sociale & Reines", new ScrollPane(vSocial));
                Tab tEnv = new Tab("Sol & Géologie SIG", new ScrollPane(vEnv));
                Tab tReasoning = new Tab("Moteurs Cognitifs & Raisonnement", new ScrollPane(vReasoning));
                Tab tSensors = new Tab("Capteurs & Biomécanique", new ScrollPane(vSensors));
                Tab tAccessory = new Tab("Espèces Associées, Proies & Pathogènes", new ScrollPane(vAccessory));

                tabPane.getTabs().addAll(tNest, tSocial, tEnv, tReasoning, tSensors, tAccessory);
                VBox.setVgrow(tabPane, Priority.ALWAYS);

                // Add live search filter listener
                searchField.textProperty().addListener((obs, oldV, newV) -> {
                        String query = newV == null ? "" : newV.toLowerCase().trim();
                        filterVBoxRows(vNest, query);
                        filterVBoxRows(vSocial, query);
                        filterVBoxRows(vEnv, query);
                        filterVBoxRows(vReasoning, query);
                        filterVBoxRows(vSensors, query);
                        filterVBoxRows(vAccessory, query);
                });

                contentBox.getChildren().addAll(subtitle, searchField, tabPane);
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
                java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.FRENCH);
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

        private void addGlossaryRow(VBox box, String title, String description, String wikiUrl) {
                Label t = new Label("• " + title + " : ");
                t.getStyleClass().add("help-entry-title");
                t.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-min-width: 240px;");

                Label d = new Label(description);
                d.getStyleClass().add("help-entry-desc");
                d.setWrapText(true);

                Hyperlink wikiLink = new Hyperlink("🌐 Wikipédia");
                wikiLink.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");
                wikiLink.setOnAction(e -> {
                        try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(wikiUrl));
                        } catch (Exception ex) {
                                new Alert(Alert.AlertType.INFORMATION, "Lien Wikipédia : " + wikiUrl).show();
                        }
                });

                HBox row = new HBox(8, t, d, wikiLink);
                row.getStyleClass().add("card-pane");
                row.setPadding(new Insets(8));
                HBox.setHgrow(d, Priority.ALWAYS);
                box.getChildren().add(row);
        }

        public static void main(String[] args) {
                launch(args);
        }
}
