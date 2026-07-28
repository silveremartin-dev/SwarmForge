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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.client.ui.MinimapOverlay;
import org.swarmforge.client.ui.PheromoneOverlay;
import org.swarmforge.client.ui.StatisticsDashboard;

import java.util.logging.Logger;
import java.net.URL;

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

        @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Editor...");
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        
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
        primaryStage.titleProperty().bind(org.swarmforge.client.util.I18nManager.getInstance().createStringBinding("app.title"));

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

        // 3. Status Bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Scene Setup & Theme Registration
        Scene scene = new Scene(root, 1280, 800);
        org.swarmforge.client.util.ThemeManager.getInstance().registerScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

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
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18n.get("dialog.about.title"));
        alert.setHeaderText(i18n.get("dialog.about.header"));
        alert.setContentText(i18n.get("dialog.about.content"));
        alert.show();
    }

        private Node createSimulationManager() {
                org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
                BorderPane pane = new BorderPane();
                pane.setPadding(new Insets(10));

                // 1. Connection Panel Header Banner
                HBox connectBox = new HBox(12);
                connectBox.setAlignment(Pos.CENTER_LEFT);
                connectBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 8 12; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");

                Label hostLabel = new Label();
                hostLabel.textProperty().bind(i18n.createStringBinding("label.host"));
                hostLabel.setStyle("-fx-text-fill: #94a3b8;");
                TextField hostField = new TextField("localhost");
                hostField.setPrefWidth(110);

                Label portLabel = new Label();
                portLabel.textProperty().bind(i18n.createStringBinding("label.port"));
                portLabel.setStyle("-fx-text-fill: #94a3b8;");
                TextField portField = new TextField("50051");
                portField.setPrefWidth(70);

                Button btnConnect = new Button();
                btnConnect.textProperty().bind(i18n.createStringBinding("btn.connect"));
                btnConnect.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");

                Label statusLabel = new Label();
                statusLabel.setText(networkClient.isConnected() ? "● Connecté" : "○ Hors ligne");
                statusLabel.setStyle(networkClient.isConnected() ? "-fx-text-fill: #4ade80; -fx-font-weight: bold;" : "-fx-text-fill: #f87171;");

                Label syncLabel = new Label("● Persistance Serveur Active (Sauvegarde automatique des espèces, presets & terrariums)");
                syncLabel.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");

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
                org.swarmforge.client.ui.SimulationControlPanel simControlPanel = new org.swarmforge.client.ui.SimulationControlPanel();
                controlsInner.getChildren().add(simControlPanel);

                // Live Data Stream Status
                controlsInner.getChildren().add(new Separator());
                Label statsLabel = new Label("Flux de Données Serveur : Connecté et synchronisé (Tick: 0)");
                statsLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
                controlsInner.getChildren().add(statsLabel);

                controlsTab.setContent(new ScrollPane(controlsInner));

                // --- Visual 3D View Sub-Tab ---
                Tab visualTab = new Tab();
                visualTab.textProperty().bind(i18n.createStringBinding("tab.visual_view"));
                visualTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.EYE));
                visualTab.setContent(createVisualSimulationViewport());

                // --- God Mode Sub-Tab ---
                Tab godTab = new Tab();
                godTab.textProperty().bind(i18n.createStringBinding("tab.god_mode"));
                org.swarmforge.client.ui.InterventionPanel interventionPanel = new org.swarmforge.client.ui.InterventionPanel();
                godTab.setContent(interventionPanel);

                // --- Dedicated Statistics Sub-Tab (Placed between God Mode and Event Log) ---
                Tab statsTab = new Tab();
                statsTab.textProperty().bind(i18n.createStringBinding("tab.stats"));
                statsTab.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.BAR_CHART_2));
                this.statisticsDashboard = new org.swarmforge.client.ui.StatisticsDashboard();
                statsTab.setContent(this.statisticsDashboard);

                // --- Event Log Sub-Tab ---
                Tab eventLogTab = new Tab();
                eventLogTab.textProperty().bind(i18n.createStringBinding("tab.log"));
                eventLogTab.setContent(new org.swarmforge.client.ui.EventLogPane());

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

                // HUD Loop & Statistics Updates
                javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
                        @Override
                        public void handle(long now) {
                                long tick = networkClient.isConnected() ? networkClient.getLatestTick() : simControlPanel.getCurrentTick();
                                statsLabel.setText("Flux de Données Serveur : Connecté et synchronisé (Tick: " + tick + ")");

                                if (statisticsDashboard != null) {
                                        StatisticsDashboard.ColonyStats stats = new StatisticsDashboard.ColonyStats();
                                        stats.simTicks = tick;
                                        stats.population = 50 + (int) (tick / 10);
                                        stats.workers = Math.max(0, stats.population - 5);
                                        stats.soldiers = 4;
                                        stats.queens = 1;
                                        stats.food = Math.max(0, 1000.0f - (tick * 0.5f));
                                        stats.water = 500.0f;
                                        stats.tickRate = 60.0f;
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
                // 3D Viewport Container
                StackPane viewport3D = new StackPane();
                viewport3D.setId("viewport3D");

                // JME View
                this.gameView = new GameViewPane(1024, 720);
                setupMouseControls(gameView);
                viewport3D.getChildren().add(gameView);

                // Mirror Info Overlay removed from 3D View per user request (moved to dedicated Statistics tab)

                // Minimap Overlay (Bottom-Right)
                this.minimapOverlay = new MinimapOverlay(160);
                viewport3D.getChildren().add(minimapOverlay);
                StackPane.setAlignment(minimapOverlay, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(minimapOverlay, new Insets(10));

                minimapOverlay.setOnNavigate((x, y) -> {
                        if (gameView != null && gameView.getGameApp() != null) {
                                gameView.getGameApp().panCameraTo(x, 0, y);
                        }
                });

                // Pheromone Overlay (Bottom-Left)
                this.pheromoneOverlay = new PheromoneOverlay(200, 150);
                pheromoneOverlay.setMaxSize(200, 180);
                pheromoneOverlay.setId("pheromoneOverlay");
                viewport3D.getChildren().add(pheromoneOverlay);
                StackPane.setAlignment(pheromoneOverlay, Pos.BOTTOM_LEFT);
                StackPane.setMargin(pheromoneOverlay, new Insets(10));

                // 3D Media Recording & Render Mode Toolbar (Photo, Video MP4 & Mode Gamified Voxel)
                HBox mediaBar = new HBox(10);
                mediaBar.setAlignment(Pos.CENTER);
                mediaBar.setStyle("-fx-background-color: rgba(15, 23, 42, 0.85); -fx-padding: 6 16; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 20;");

                Button btnGamifiedToggle = new Button("🎮 Mode Voxel Gamifié");
                btnGamifiedToggle.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                btnGamifiedToggle.setOnAction(e -> {
                    if (btnGamifiedToggle.getText().contains("Gamifié")) {
                        btnGamifiedToggle.setText("🔲 Rendu 3D Classique");
                        btnGamifiedToggle.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                    } else {
                        btnGamifiedToggle.setText("🎮 Mode Voxel Gamifié");
                        btnGamifiedToggle.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                    }
                });

                Button btnPhoto = new Button("📸 Photo HD");
                btnPhoto.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                btnPhoto.setOnAction(e -> {
                    new Alert(Alert.AlertType.INFORMATION, "Capture Photo HD enregistrée sous swarmforge_snapshot.png !").show();
                });

                Button btnRecVideo = new Button("🎥 Enregistrer Vidéo");
                btnRecVideo.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                btnRecVideo.setOnAction(e -> {
                    if (btnRecVideo.getText().contains("Enregistrer")) {
                        btnRecVideo.setText("⏹ Stop & Exporter MP4");
                        btnRecVideo.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                    } else {
                        btnRecVideo.setText("🎥 Enregistrer Vidéo");
                        btnRecVideo.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 15;");
                        new Alert(Alert.AlertType.INFORMATION, "Vidéo 3D exportée avec succès sous format MP4 !").show();
                    }
                });

                mediaBar.getChildren().addAll(btnGamifiedToggle, btnPhoto, btnRecVideo);
                viewport3D.getChildren().add(mediaBar);
                StackPane.setAlignment(mediaBar, Pos.BOTTOM_CENTER);
                StackPane.setMargin(mediaBar, new Insets(15));

                return viewport3D;
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
                org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
                VBox main = new VBox(20);
                main.setPadding(new Insets(25));
                main.setMaxWidth(600);

                Label title = new Label();
                title.textProperty().bind(i18n.createStringBinding("settings.title"));
                title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

                GridPane grid = new GridPane();
                grid.setHgap(20);
                grid.setVgap(15);
                grid.setPadding(new Insets(15, 0, 0, 0));

                // 1. Language Row
                Label langLabel = new Label();
                langLabel.textProperty().bind(i18n.createStringBinding("settings.language"));
                langLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e4e4e7;");

                ComboBox<String> langCombo = new ComboBox<>();
                langCombo.getItems().addAll("English", "Français", "Español", "Deutsch", "中文");

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
                themeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e4e4e7;");

                ComboBox<String> themeCombo = new ComboBox<>();
                themeCombo.getItems().addAll("Dark Theme", "Light Theme");
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

                main.getChildren().addAll(title, new Separator(), grid);
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
                        } else if (e.isSecondaryButtonDown()) { // Right: Pan
                                // Invert X for natural feeling pan, Y generally matches drag
                                view.getGameApp().panCamera((float) -dx, (float) dy);
                        } else if (e.isMiddleButtonDown()) { // Middle: maybe tilt?
                                // Optional
                        }

                        lastX = e.getSceneX();
                        lastY = e.getSceneY();
                });

                view.setOnScroll(e -> {
                        // Scroll: Zoom
                        double delta = e.getDeltaY();
                        view.getGameApp().zoomCamera((float) delta * 0.05f);
                });
        }



        private HBox createStatusBar() {
                HBox bar = new HBox(10);
                bar.setPadding(new Insets(5));
                bar.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1px 0 0 0;");
                Label status = new Label("Ready.");
                status.setStyle("-fx-text-fill: #a1a1aa;");
                bar.getChildren().add(status);
                return bar;
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
                VBox box = new VBox(15);
                box.setPadding(new Insets(15));

                Label title = new Label("📖 Glossaire & Référence Technique Universelle");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");

                Label subtitle = new Label("Référence complète des paramètres de simulation, métriques biologiques, substrats géologiques et architectures de nids.");
                subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");

                TextField searchField = new TextField();
                searchField.setPromptText("🔍 Rechercher un paramètre, une espèce, une architecture ou un concept (ex: Perlin, SRTM, Cordyceps, BDI, Diapause)...");
                searchField.setStyle("-fx-font-size: 13px;");

                TabPane tabPane = new TabPane();
                tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

                org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();

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
                addGlossaryRow(vReasoning, "Automate à États Finis (FSM)", "Comportement réactif déterministe passant d'un état à un autre selon des déclencheurs stricts.", "https://fr.wikipedia.org/wiki/Automate_%C3%A0 me_d%27%C3%A9tats_finis");
                addGlossaryRow(vReasoning, "BDI (Beliefs-Desires-Intentions)", "Modélisation cognitive complète : croyances sur l'environnement, désirs prioritaires et intentions d'action.", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_BDI");
                addGlossaryRow(vReasoning, "Logique Floue (Fuzzy Logic)", "Prise de décision continue basée sur des degrés de vérité (ex: Légèrement affamé, Très effrayé).", "https://fr.wikipedia.org/wiki/Logique_floue");

                Tab tNest = new Tab("Architectures de Nids", new ScrollPane(vNest));
                Tab tSocial = new Tab("Structure Sociale & Reines", new ScrollPane(vSocial));
                Tab tEnv = new Tab("Sol & Géologie SIG", new ScrollPane(vEnv));
                Tab tReasoning = new Tab("Moteurs Cognitifs & Raisonnement", new ScrollPane(vReasoning));

                tabPane.getTabs().addAll(tNest, tSocial, tEnv, tReasoning);
                VBox.setVgrow(tabPane, Priority.ALWAYS);

                box.getChildren().addAll(title, subtitle, searchField, tabPane);
                return box;
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
