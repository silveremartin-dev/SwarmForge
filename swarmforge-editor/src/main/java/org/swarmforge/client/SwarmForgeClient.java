/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
        LOG.info("Starting SwarmForge Studio...");
        
        // title binding
        primaryStage.titleProperty().bind(org.swarmforge.client.util.I18nManager.getInstance().createStringBinding("app.title"));

        // Root Layout
        BorderPane root = new BorderPane();

        // 1. Menu Bar (Global)
        MenuBar menuBar = createMenuBar(primaryStage); // Pass stage for owner
        root.setTop(menuBar);

        // 2. Main Tab Pane
        TabPane mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- TAB 1: SIMULATION MANAGER ---
        Tab simTab = new Tab();
        simTab.textProperty().bind(org.swarmforge.client.util.I18nManager.getInstance().createStringBinding("menu.tools")); // Reuse or new key
        simTab.setContent(createSimulationManager());

        // --- TAB 2: WORLD EDITOR (3D View + Terrain Tools) ---
        Tab worldTab = new Tab("World Editor"); // TODO: Add key
        worldTab.setContent(createWorldEditor());

        // --- TAB 3: SPECIES EDITOR ---
        Tab speciesTab = new Tab();
        speciesTab.textProperty().bind(org.swarmforge.client.util.I18nManager.getInstance().createStringBinding("species.queen")); // Placeholder key, should be "Species Editor"
        speciesTab.setContent(createSpeciesEditor());

        // --- Other Tabs (Weather, Nest, etc.) ---
        // Keeping them simple for now to focus on Menu functionality
        Tab weatherTab = new Tab("Weather"); 
        weatherTab.setContent(new org.swarmforge.client.ui.WeatherEditorPane());

        Tab nestTab = new Tab("Nest");
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

        Tab eventTab = new Tab("Log");
        eventTab.setContent(new org.swarmforge.client.ui.EventLogPane());

        // God Mode
        Tab godTab = new Tab("God Mode");
        godTab.setContent(new org.swarmforge.client.ui.InterventionPanel());

        mainTabs.getTabs().addAll(simTab, worldTab, speciesTab, weatherTab, nestTab, eventTab, godTab);
        mainTabs.getSelectionModel().select(worldTab);

        root.setCenter(mainTabs);

        // 3. Status Bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Scene Setup - Remove hardcoded styles
        Scene scene = new Scene(root, 1280, 800);
        
        // Load CSS if available (user wants valid theme usage)
        URL cssUrl = getClass().getResource("/styles/dark-theme.css");
        if(cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage owner) {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        MenuBar bar = new MenuBar();

        // --- FILE ---
        Menu file = new Menu();
        file.textProperty().bind(i18n.createStringBinding("menu.file"));
        
        MenuItem mNew = new MenuItem();
        mNew.textProperty().bind(i18n.createStringBinding("menu.file.new"));
        mNew.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"));
        mNew.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.FILE_PLUS));
        
        MenuItem mOpen = new MenuItem();
        mOpen.textProperty().bind(i18n.createStringBinding("menu.file.open"));
        mOpen.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        mOpen.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.FOLDER));

        MenuItem mSave = new MenuItem();
        mSave.textProperty().bind(i18n.createStringBinding("menu.file.save"));
        mSave.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        mSave.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SAVE));

        MenuItem mSaveAs = new MenuItem();
        mSaveAs.textProperty().bind(i18n.createStringBinding("menu.file.saveAs"));
        mSaveAs.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Shift+S"));

        Menu mExport = new Menu(); // Submenu
        mExport.textProperty().bind(i18n.createStringBinding("menu.file.export"));
        mExport.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.UPLOAD));

        MenuItem mExit = new MenuItem();
        mExit.textProperty().bind(i18n.createStringBinding("menu.file.exit"));
        mExit.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Q"));
        mExit.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.LOG_OUT));
        mExit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        file.getItems().addAll(mNew, mOpen, new SeparatorMenuItem(), mSave, mSaveAs, new SeparatorMenuItem(), mExport, new SeparatorMenuItem(), mExit);

        // --- VIEW ---
        Menu view = new Menu();
        view.textProperty().bind(i18n.createStringBinding("menu.view"));
        
        MenuItem mZoomIn = new MenuItem();
        mZoomIn.textProperty().bind(i18n.createStringBinding("menu.view.zoomIn"));
        mZoomIn.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl++"));
        mZoomIn.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.ZOOM_IN));
        
        MenuItem mZoomOut = new MenuItem();
        mZoomOut.textProperty().bind(i18n.createStringBinding("menu.view.zoomOut"));
        mZoomOut.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+-"));
        mZoomOut.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.ZOOM_OUT));

        MenuItem mFit = new MenuItem();
        mFit.textProperty().bind(i18n.createStringBinding("menu.view.fit"));
        mFit.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.MAXIMIZE));
        
        MenuItem mFull = new MenuItem();
        mFull.textProperty().bind(i18n.createStringBinding("menu.view.fullscreen"));
        mFull.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F11"));
        mFull.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.MONITOR));
        mFull.setOnAction(e -> owner.setFullScreen(!owner.isFullScreen()));

        view.getItems().addAll(mZoomIn, mZoomOut, mFit, new SeparatorMenuItem(), mFull);

        // --- TOOLS ---
        Menu tools = new Menu();
        tools.textProperty().bind(i18n.createStringBinding("menu.tools"));

        MenuItem mRun = new MenuItem();
        mRun.textProperty().bind(i18n.createStringBinding("menu.tools.start"));
        mRun.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F5"));
        mRun.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.PLAY));
        mRun.setOnAction(e -> { if(networkClient != null) networkClient.control(org.swarmforge.protocol.grpc.ControlAction.CTRL_START); });

        MenuItem mPause = new MenuItem();
        mPause.textProperty().bind(i18n.createStringBinding("menu.tools.pause"));
        mPause.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.PAUSE));
        mPause.setOnAction(e -> { if(networkClient != null) networkClient.control(org.swarmforge.protocol.grpc.ControlAction.CTRL_PAUSE); });

        MenuItem mStop = new MenuItem();
        mStop.textProperty().bind(i18n.createStringBinding("menu.tools.stop"));
        mStop.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.SQUARE));
         mStop.setOnAction(e -> { if(networkClient != null) networkClient.control(org.swarmforge.protocol.grpc.ControlAction.CTRL_STOP); });
        
        tools.getItems().addAll(mRun, mPause, mStop);

        // --- PREFERENCES ---
        Menu prefs = new Menu();
        prefs.textProperty().bind(i18n.createStringBinding("menu.prefs"));

        Menu mLang = new Menu();
        mLang.textProperty().bind(i18n.createStringBinding("menu.prefs.language"));
        mLang.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.GLOBE));
        
        // Language Options
        ToggleGroup langGroup = new ToggleGroup();
        addLangItem(mLang, langGroup, "English", java.util.Locale.ENGLISH);
        addLangItem(mLang, langGroup, "Français", java.util.Locale.FRENCH);
        addLangItem(mLang, langGroup, "Español", new java.util.Locale("es"));
        addLangItem(mLang, langGroup, "Deutsch", new java.util.Locale("de"));
        addLangItem(mLang, langGroup, "中文", new java.util.Locale("zh"));

        MenuItem mDefaults = new MenuItem();
        mDefaults.textProperty().bind(i18n.createStringBinding("menu.prefs.defaults"));
        mDefaults.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.ROTATE_CCW));

        prefs.getItems().addAll(mLang, new SeparatorMenuItem(), mDefaults);

        // --- HELP ---
        Menu help = new Menu();
        help.textProperty().bind(i18n.createStringBinding("menu.help"));
        
        MenuItem mAbout = new MenuItem();
        mAbout.textProperty().bind(i18n.createStringBinding("menu.help.about"));
        mAbout.setGraphic(new org.kordamp.ikonli.javafx.FontIcon(org.kordamp.ikonli.feather.Feather.INFO));
        mAbout.setOnAction(e -> showAboutDialog());

        help.getItems().add(mAbout);

        bar.getMenus().addAll(file, view, tools, prefs, help);
        return bar;
    }

    private void addLangItem(Menu menu, ToggleGroup group, String label, java.util.Locale locale) {
        RadioMenuItem item = new RadioMenuItem(label);
        item.setToggleGroup(group);
        if (org.swarmforge.client.util.I18nManager.getInstance().getLocale().getLanguage().equals(locale.getLanguage())) {
            item.setSelected(true);
        }
        item.setOnAction(e -> org.swarmforge.client.util.I18nManager.getInstance().setLocale(locale));
        menu.getItems().add(item);
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SwarmForge");
        alert.setHeaderText("SwarmForge v2.0.0");
        alert.setContentText("Eusocial Insect Simulation Studio\n\nCopyright (c) 2025 Silvère Martin-Michiellot\nAssisted by Gemini AI");
        alert.show();
    }

        private Node createSimulationManager() {
                BorderPane pane = new BorderPane();
                pane.setPadding(new Insets(10));

                // 1. Connection Panel
                HBox connectBox = new HBox(10);
                connectBox.setAlignment(Pos.CENTER_LEFT);
                TextField hostField = new TextField("localhost");
                TextField portField = new TextField("50051");
                Button btnConnect = new Button("Connect");
                Label statusLabel = new Label("Disconnected");
                // statusLabel.setStyle("-fx-text-fill: red;"); // Removed hardcoded style

                connectBox.getChildren().addAll(new Label("Host:"), hostField, new Label("Port:"), portField,
                                btnConnect,
                                statusLabel);
                pane.setTop(connectBox);

                // 2. Content Area (Tabs or Split)
                TabPane subTabs = new TabPane();

                // --- Controls Tab ---
                Tab controlsTab = new Tab("Controls");
                VBox controlsInner = new VBox(10);
                controlsInner.setPadding(new Insets(10));

                // Sim Control
                HBox simCtrl = new HBox(10);
                Button btnStart = new Button("Start");
                Button btnPause = new Button("Pause");
                Button btnStop = new Button("Stop");
                simCtrl.getChildren().addAll(btnStart, btnPause, btnStop);
                controlsInner.getChildren().add(new Label("Simulation Control"));
                controlsInner.getChildren().add(simCtrl);

                // Persistence
                controlsInner.getChildren().add(new Separator());
                controlsInner.getChildren().add(new Label("Persistence (Server)"));
                HBox persistCtrl = new HBox(10);
                Button btnServerSave = new Button("Server Save");
                Button btnServerLoad = new Button("Server Load");
                persistCtrl.getChildren().addAll(btnServerSave, btnServerLoad);
                controlsInner.getChildren().add(persistCtrl);

                // Live Stats Label
                controlsInner.getChildren().add(new Separator());
                Label statsLabel = new Label("Waiting for data...");
                controlsInner.getChildren().add(statsLabel);

                // Population Graph
                controlsInner.getChildren().add(new Separator());
                controlsInner.getChildren().add(new Label("Real-time Statistics"));
                org.swarmforge.client.ui.PopulationGraphPane graphPane = new org.swarmforge.client.ui.PopulationGraphPane();
                controlsInner.getChildren().add(graphPane);

                controlsTab.setContent(controlsInner);

                // --- God Mode Tab ---
                Tab godTab = new Tab("God Mode");
                godTab.setContent(createGodModePanel());

                subTabs.getTabs().addAll(controlsTab, godTab);
                pane.setCenter(subTabs);

                // Logic
                btnConnect.setOnAction(e -> {
                        try {
                                networkClient.connect(hostField.getText(), Integer.parseInt(portField.getText()));
                                statusLabel.setText("Connected");
                                statusLabel.setStyle("-fx-text-fill: green;");
                                networkClient.startStreaming(); // Start updates

                                // Pass client to game view if needed (for rendering)
                                if (gameView != null) {
                                        gameView.getGameApp().setNetworkClient(networkClient);
                                }
                        } catch (Exception ex) {
                                statusLabel.setText("Error: " + ex.getMessage());
                                ex.printStackTrace();
                        }
                });

                btnStart.setOnAction(e -> networkClient.control(org.swarmforge.protocol.grpc.ControlAction.CTRL_START));
                btnPause.setOnAction(e -> networkClient.control(org.swarmforge.protocol.grpc.ControlAction.CTRL_PAUSE));
                btnStop.setOnAction(e -> networkClient.control(org.swarmforge.protocol.grpc.ControlAction.CTRL_STOP));

                btnServerSave.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog("save1");
                        dialog.setTitle("Save World");
                        dialog.setHeaderText("Enter save name:");
                        dialog.showAndWait().ifPresent(name -> {
                                try {
                                        String msg = networkClient.saveWorld(name);
                                        new Alert(Alert.AlertType.INFORMATION, "Saved: " + msg).show();
                                } catch (Exception ex) {
                                        new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).show();
                                }
                        });
                });

                btnServerLoad.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog("");
                        dialog.setTitle("Load World");
                        dialog.setHeaderText("Enter World ID (UUID):");
                        dialog.showAndWait().ifPresent(id -> {
                                try {
                                        String msg = networkClient.loadWorld(id);
                                        new Alert(Alert.AlertType.INFORMATION, "Loaded: " + msg).show();
                                } catch (Exception ex) {
                                        new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).show();
                                }
                        });
                });

                // HUD Loop
                javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
                        @Override
                        public void handle(long now) {
                                if (networkClient.isConnected()) {
                                        long tick = networkClient.getLatestTick();
                                        statsLabel.setText("Tick: " + tick);

                                        // Mock data for graphs (would normally come from getColonyStats)
                                        // NOTE: Using simulated data for graph demonstration until
                                        // the dedicated Statistics gRPC service is implemented.
                                        // For now, we show simulated data update if connected
                                        if (tick % 60 == 0) { // Update every second
                                                int mockPop = 50 + (int) (tick / 100);
                                                graphPane.addDataPoint(mockPop, mockPop - 5, 5,
                                                                1000 - (int) (tick / 10), 1, 0);
                                        }
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

        private Node createWorldEditor() {
                // SplitPane: 3D View (Left/Center) | Tools (Right)
                SplitPane split = new SplitPane();

                // 3D Viewport Container
                StackPane viewport3D = new StackPane();
                viewport3D.setStyle("-fx-background-color: black;");

                // JME View
                this.gameView = new GameViewPane(1024, 720);
                setupMouseControls(gameView);
                viewport3D.getChildren().add(gameView);

                // Mirror Info Overlay (Top-Left)
                VBox mirrorOverlay = createMirrorOverlay();
                viewport3D.getChildren().add(mirrorOverlay);
                StackPane.setAlignment(mirrorOverlay, Pos.TOP_LEFT);
                StackPane.setMargin(mirrorOverlay, new Insets(10));

                // Minimap Overlay (Bottom-Right)
                this.minimapOverlay = new MinimapOverlay(160);
                viewport3D.getChildren().add(minimapOverlay);
                StackPane.setAlignment(minimapOverlay, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(minimapOverlay, new Insets(10));
                minimapOverlay.setOnNavigate((x, y) -> {
                        if (gameView != null && gameView.getGameApp() != null) {
                                gameView.getGameApp().panCameraTo(x, 0, y); // Navigate to clicked position
                        }
                });

                // Pheromone Overlay (Bottom-Left, collapsible)
                this.pheromoneOverlay = new PheromoneOverlay(200, 150);
                pheromoneOverlay.setMaxSize(200, 180);
                pheromoneOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 5;");
                viewport3D.getChildren().add(pheromoneOverlay);
                StackPane.setAlignment(pheromoneOverlay, Pos.BOTTOM_LEFT);
                StackPane.setMargin(pheromoneOverlay, new Insets(10));

                // Editor Tools (Right Panel)
                VBox tools = new VBox(10);
                tools.setPadding(new Insets(10));
                tools.setMinWidth(250);
                tools.setMaxWidth(300);
                tools.setStyle("-fx-background-color: #f0f0f0;"); // Light bg for editor tools

                tools.getChildren().add(new Label("Terrain Generator"));
                tools.getChildren().add(new Separator());

                TextField seedField = new TextField("123456789");
                tools.getChildren().add(new Label("Seed:"));
                tools.getChildren().add(seedField);

                TextField scaleField = new TextField("0.05");
                tools.getChildren().add(new Label("Scale (0.01-0.1):"));
                tools.getChildren().add(scaleField);

                CheckBox cavesCheck = new CheckBox("Caves");
                tools.getChildren().add(cavesCheck);
                CheckBox waterCheck = new CheckBox("Water");
                tools.getChildren().add(waterCheck);

                Button genButton = new Button("Generate Preview");
                genButton.setOnAction(e -> {
                        try {
                                long seed = Long.parseLong(seedField.getText());
                                float scale = Float.parseFloat(scaleField.getText());

                                org.swarmforge.core.domain.Terrarium terrarium = new org.swarmforge.core.domain.Terrarium(
                                                64, 64, 32);
                                org.swarmforge.core.world.TerrainGenerator gen = new org.swarmforge.core.world.TerrainGenerator(
                                                seed);

                                gen.generate(terrarium, 16, 10, scale);
                                if (waterCheck.isSelected()) {
                                        gen.addWater(terrarium, 14, 0.4f);
                                }

                                // Store for simulation use
                                this.lastGeneratedTerrarium = terrarium;

                                // Pass to JME
                                gameView.getGameApp().renderTerrarium(terrarium);

                                // Create local simulation for preview with this terrarium
                                localSimulation = new org.swarmforge.core.simulation.Simulation(terrarium);
                                localSimulation.setTicksPerSecond(20);
                                startLocalSimulationUpdates();

                        } catch (NumberFormatException ex) {
                                new Alert(Alert.AlertType.ERROR, "Invalid number format").show();
                        }
                });
                tools.getChildren().add(genButton);

                tools.getChildren().add(new Separator());
                tools.getChildren().add(new Label("Persistence"));

                HBox persistence = new HBox(10);
                Button btnSaveWorld = new Button("Save World");
                Button btnLoadWorld = new Button("Load World");
                persistence.getChildren().addAll(btnSaveWorld, btnLoadWorld);
                tools.getChildren().add(persistence);

                tools.getChildren().add(new Separator());
                tools.getChildren().add(new Label("Interaction"));
                ComboBox<String> toolSelect = new ComboBox<>();
                toolSelect.getItems().addAll("View Mode", "Add Block", "Remove Block");
                toolSelect.getSelectionModel().selectFirst();
                toolSelect.setOnAction(e -> {
                        if (gameView != null && gameView.getGameApp() != null) {
                                gameView.getGameApp().setTool(toolSelect.getValue());
                        }
                });
                tools.getChildren().add(toolSelect);

                // Wire Listener
                gameView.setTerrainListener((x, y, z, added) -> {
                        // 1. Update Server if connected
                        if (networkClient != null && networkClient.isConnected()) {
                                networkClient.modifyTerrain(x, y, z, added ? 1 : 0); // 1 = Soil, 0 = Air
                        }

                        // 2. Update Local Model
                        if (this.lastGeneratedTerrarium != null) {
                                try {
                                        if (!added) {
                                                // Remove (set to AIR)
                                                org.swarmforge.core.domain.TerrariumCell air = org.swarmforge.core.domain.TerrariumCell
                                                                .air(x, y, z);
                                                this.lastGeneratedTerrarium.setCell(air);
                                        } else {
                                                // Add Block (Dirt for now)
                                                org.swarmforge.core.domain.TerrariumCell cell = new org.swarmforge.core.domain.TerrariumCell(
                                                                x, y, z,
                                                                org.swarmforge.core.domain.TerrariumCell.Material.EARTH,
                                                                new float[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TYPES],
                                                                20f, 50f);
                                                this.lastGeneratedTerrarium.setCell(cell);
                                                // Trigger re-render
                                                Platform.runLater(() -> gameView.getGameApp()
                                                                .renderTerrarium(this.lastGeneratedTerrarium));
                                        }
                                } catch (Exception ex) {
                                        LOG.warning("Failed to modify terrain: " + ex.getMessage());
                                }
                        }
                });

                // === Simulation Controls ===
                tools.getChildren().add(new Separator());
                tools.getChildren().add(new Label("Simulation Controls"));

                HBox simControlBox = new HBox(8);
                simControlBox.setAlignment(Pos.CENTER_LEFT);
                Button btnPlayPause = new Button("▶ Start");
                btnPlayPause.setStyle("-fx-font-size: 14px;");
                simControlBox.getChildren().add(btnPlayPause);
                tools.getChildren().add(simControlBox);

                // Speed presets
                HBox speedBox = new HBox(5);
                speedBox.setAlignment(Pos.CENTER_LEFT);
                Label lblSpeed = new Label("Speed:");
                ToggleGroup speedGroup = new ToggleGroup();
                ToggleButton btn05x = new ToggleButton("0.5x");
                ToggleButton btn1x = new ToggleButton("1x");
                ToggleButton btn2x = new ToggleButton("2x");
                ToggleButton btn4x = new ToggleButton("4x");
                btn05x.setToggleGroup(speedGroup);
                btn1x.setToggleGroup(speedGroup);
                btn2x.setToggleGroup(speedGroup);
                btn4x.setToggleGroup(speedGroup);
                btn1x.setSelected(true);
                speedBox.getChildren().addAll(lblSpeed, btn05x, btn1x, btn2x, btn4x);
                tools.getChildren().add(speedBox);

                // Speed control actions
                btn05x.setOnAction(e -> {
                        if (localSimulation != null)
                                localSimulation.setTicksPerSecond(10);
                });
                btn1x.setOnAction(e -> {
                        if (localSimulation != null)
                                localSimulation.setTicksPerSecond(20);
                });
                btn2x.setOnAction(e -> {
                        if (localSimulation != null)
                                localSimulation.setTicksPerSecond(40);
                });
                btn4x.setOnAction(e -> {
                        if (localSimulation != null)
                                localSimulation.setTicksPerSecond(80);
                });

                // Play/Pause action
                btnPlayPause.setOnAction(e -> {
                        if (localSimulation == null)
                                return;
                        if (localSimulation.isRunning()) {
                                localSimulation.pause();
                                btnPlayPause.setText("▶ Resume");
                        } else {
                                localSimulation.start();
                                btnPlayPause.setText("⏸ Pause");
                        }
                });

                // Logic
                btnSaveWorld.setOnAction(e -> {
                        if (networkClient.isConnected()) {
                                TextInputDialog dialog = new TextInputDialog("MyWorld");
                                dialog.setTitle("Save World to DB");
                                dialog.setHeaderText("Enter Name:");
                                dialog.showAndWait().ifPresent(name -> {
                                        try {
                                                String msg = networkClient.saveWorld(name);
                                                new Alert(Alert.AlertType.INFORMATION, "Saved to DB: " + msg).show();
                                        } catch (Exception ex) {
                                                new Alert(Alert.AlertType.ERROR, "DB Save Failed: " + ex.getMessage())
                                                                .show();
                                        }
                                });
                                return;
                        }

                        // Fallback to local
                        if (lastGeneratedTerrarium == null) {
                                new Alert(Alert.AlertType.WARNING, "No world generated to save.").show();
                                return;
                        }
                        try {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.io.File file = new java.io.File("world_save.json");
                                mapper.writerWithDefaultPrettyPrinter().writeValue(file, lastGeneratedTerrarium);
                                new Alert(Alert.AlertType.INFORMATION,
                                                "World saved locally to " + file.getAbsolutePath())
                                                .show();
                        } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Local Save failed: " + ex.getMessage()).show();
                                ex.printStackTrace();
                        }
                });

                btnLoadWorld.setOnAction(e -> {
                        if (networkClient.isConnected()) {
                                TextInputDialog dialog = new TextInputDialog("");
                                dialog.setTitle("Load World from DB");
                                dialog.setHeaderText("Enter World ID:");
                                dialog.showAndWait().ifPresent(id -> {
                                        try {
                                                String msg = networkClient.loadWorld(id);
                                                new Alert(Alert.AlertType.INFORMATION, "Loaded from DB: " + msg).show();
                                                // Note: Does not download terrain to client yet!
                                                new Alert(Alert.AlertType.WARNING,
                                                                "Visuals will not update until implemented.").show();
                                        } catch (Exception ex) {
                                                new Alert(Alert.AlertType.ERROR, "DB Load Failed: " + ex.getMessage())
                                                                .show();
                                        }
                                });
                                return;
                        }

                        try {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.io.File file = new java.io.File("world_save.json");
                                if (!file.exists()) {
                                        new Alert(Alert.AlertType.ERROR, "world_save.json not found").show();
                                        return;
                                }
                                org.swarmforge.core.domain.Terrarium loaded = mapper.readValue(file,
                                                org.swarmforge.core.domain.Terrarium.class);
                                this.lastGeneratedTerrarium = loaded;
                                gameView.getGameApp().renderTerrarium(loaded);
                                new Alert(Alert.AlertType.INFORMATION, "World loaded locally!").show();
                        } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Local Load failed: " + ex.getMessage()).show();
                                ex.printStackTrace();
                        }
                });

                // === Right Panel TabPane ===
                TabPane rightTabs = new TabPane();
                rightTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                rightTabs.setMinWidth(280);
                rightTabs.setMaxWidth(350);

                // Tools Tab
                Tab toolsTab = new Tab("Tools");
                ScrollPane toolsScroll = new ScrollPane(tools);
                toolsScroll.setFitToWidth(true);
                toolsTab.setContent(toolsScroll);

                // Statistics Tab
                Tab statsTab = new Tab("Statistics");
                this.statisticsDashboard = new StatisticsDashboard();
                ScrollPane statsScroll = new ScrollPane(statisticsDashboard);
                statsScroll.setFitToWidth(true);
                statsTab.setContent(statsScroll);

                rightTabs.getTabs().addAll(toolsTab, statsTab);

                split.getItems().addAll(viewport3D, rightTabs);
                split.setDividerPositions(0.75);
                return split;
        }

        private Node createSpeciesEditor() {
                VBox pane = new VBox(20);
                pane.setPadding(new Insets(20));

                Label header = new Label("Species Designer");
                header.setStyle("-fx-font-size: 24px;");

                // Data Model
                // Used to be local, now using class field 'currentSpecies'
                if (currentSpecies == null) {
                        currentSpecies = new org.swarmforge.core.species.CustomSpecies();
                }

                // UI Form (Grid)
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20));

                TextField nameField = new TextField(currentSpecies.getCommonName());
                TextField scienceField = new TextField(currentSpecies.getScientificName());
                TextField lifespanField = new TextField(String.valueOf(currentSpecies.getWorkerLifespan()));
                TextField speedField = new TextField(String.valueOf(currentSpecies.getWorkerSpeed()));

                grid.addRow(0, new Label("Common Name:"), nameField);
                grid.addRow(1, new Label("Scientific Name:"), scienceField);
                grid.addRow(2, new Label("Worker Lifespan (ticks):"), lifespanField);
                grid.addRow(3, new Label("Worker Speed:"), speedField);

                // Actions
                HBox actions = new HBox(10);
                Button btnSave = new Button("Save Species...");
                Button btnLoad = new Button("Load Species...");

                actions.getChildren().addAll(btnSave, btnLoad);

                // Logic
                btnSave.setOnAction(e -> {
                        // Update model from UI
                        currentSpecies.setCommonName(nameField.getText());
                        currentSpecies.setScientificName(scienceField.getText());
                        try {
                                currentSpecies.setWorkerLifespan(Integer.parseInt(lifespanField.getText()));
                                currentSpecies.setWorkerSpeed(Float.parseFloat(speedField.getText()));

                                // Serialize
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.io.File file = new java.io.File("species_"
                                                + currentSpecies.getCommonName().replace(" ", "_") + ".json");
                                mapper.writerWithDefaultPrettyPrinter().writeValue(file, currentSpecies);

                                new Alert(Alert.AlertType.INFORMATION, "Saved to " + file.getAbsolutePath()).show();
                        } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Error saving: " + ex.getMessage()).show();
                                ex.printStackTrace();
                        }
                });

                btnLoad.setOnAction(e -> {
                        try {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                java.io.File file = new java.io.File(
                                                "species_" + nameField.getText().replace(" ", "_") + ".json"); // Simple
                                                                                                               // load
                                                                                                               // by
                                                                                                               // name
                                                                                                               // for
                                                                                                               // demo

                                if (!file.exists()) {
                                        // Fallback to file chooser in real app, here just mock or error
                                        new Alert(Alert.AlertType.WARNING,
                                                        "File not found: " + file.getName() + " (Try saving first)")
                                                        .show();
                                        return;
                                }

                                org.swarmforge.core.species.CustomSpecies loaded = mapper.readValue(file,
                                                org.swarmforge.core.species.CustomSpecies.class);

                                // Update UI
                                nameField.setText(loaded.getCommonName());
                                scienceField.setText(loaded.getScientificName());
                                lifespanField.setText(String.valueOf(loaded.getWorkerLifespan()));
                                speedField.setText(String.valueOf(loaded.getWorkerSpeed()));

                                new Alert(Alert.AlertType.INFORMATION, "Loaded species!").show();

                        } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Error loading: " + ex.getMessage()).show();
                        }
                });

                pane.getChildren().addAll(header, grid, new Separator(), actions);
                return pane;
        }

        private VBox createMirrorOverlay() {
                VBox box = new VBox(5);
                box.setStyle("-fx-background-color: rgba(20, 20, 30, 0.6); -fx-padding: 10; -fx-background-radius: 5;");
                box.setMaxSize(200, 150);

                // "Mirror" effect items
                Label title = new Label("LIVE STATUS");
                title.setStyle("-fx-text-fill: cyan; -fx-font-weight: bold;");

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

        private MenuBar createMenuBar() {
                MenuBar bar = new MenuBar();
                Menu file = new Menu("File");
                file.getItems().add(new MenuItem("Exit"));
                Menu help = new Menu("Help");
                help.getItems().add(new MenuItem("About"));
                bar.getMenus().addAll(file, help);
                return bar;
        }

        private HBox createStatusBar() {
                HBox bar = new HBox(10);
                bar.setPadding(new Insets(5));
                bar.setStyle("-fx-background-color: #ddd;");
                bar.getChildren().add(new Label("Ready."));
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

        public static void main(String[] args) {
                launch(args);
        }
}
