/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * SwarmForge Server GUI - Management Console
 * Provides graphical interface for server administration:
 * - Health & Logging Panel
 * - Simulation Manager
 * - Running Simulations Dashboard
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ServerGuiApp extends Application {

    private static final Logger LOG = Logger.getLogger(ServerGuiApp.class.getName());

    private SwarmForgeServer server;
    private TextArea logArea;
    private Circle dbStatusIndicator;
    private Circle redisStatusIndicator;
    private Circle grpcStatusIndicator;
    private ListView<String> runningSimsList;
    private ListView<String> connectedClientsList;
    private ListView<String> savedSimsList;

    @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Server GUI...");

        // Load Window Icon (multi-resolution for Windows taskbar and titlebar)
        try {
            java.net.URL iconUrl = getClass().getResource("/icons/icon.png");
            if (iconUrl != null) {
                String urlStr = iconUrl.toExternalForm();
                primaryStage.getIcons().clear();
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 16, 16, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 32, 32, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 48, 48, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 64, 64, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 128, 128, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr, 256, 256, true, true));
                primaryStage.getIcons().add(new javafx.scene.image.Image(urlStr));
            }
        } catch (Exception e) {
            LOG.warning("Could not load server icon: " + e.getMessage());
        }

        // Root Layout with Tabs
        TabPane mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // === TAB 1: HEALTH & LOGGING ===
        Tab healthTab = new Tab("Health & Logging");
        healthTab.setContent(createHealthPanel());

        // === TAB 2: SIMULATION MANAGER ===
        Tab simTab = new Tab("Simulation Manager");
        simTab.setContent(createSimulationManager());

        // === TAB 3: RUNNING SIMULATIONS ===
        Tab dashTab = new Tab("Dashboard");
        dashTab.setContent(createDashboard());

        mainTabs.getTabs().addAll(healthTab, simTab, dashTab);

        Scene scene = new Scene(mainTabs, 900, 600);

        // Dark theme styling
        scene.getRoot().setStyle("-fx-background-color: #1a1a2e;");

        primaryStage.setTitle("SwarmForge Server Console");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            if (server != null) {
                server.stop();
            }
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

        // Auto-start server
        startServer();

        // Redirect logs to GUI
        setupLogRedirection();
    }

    private void setupLogRedirection() {
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        rootLogger.addHandler(new java.util.logging.Handler() {
            @Override
            public void publish(java.util.logging.LogRecord record) {
                if (logArea != null) {
                    // Format message
                    String msg = String.format("[%s] [%s] %s",
                            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                            record.getLevel(),
                            record.getMessage());

                    Platform.runLater(() -> {
                        if (logArea != null) {
                            logArea.appendText(msg + "\n");
                        }
                    });
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

    private VBox createHealthPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #16213e;");

        // Title
        Label title = new Label("Server Health");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        // Status Indicators
        HBox statusRow = new HBox(30);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        // gRPC Status
        VBox grpcBox = new VBox(5);
        grpcBox.setAlignment(Pos.CENTER);
        grpcStatusIndicator = new Circle(12, Color.GRAY);
        grpcBox.getChildren().addAll(grpcStatusIndicator, new Label("gRPC Server") {
            {
                setStyle("-fx-text-fill: white;");
            }
        });

        // Database Status
        VBox dbBox = new VBox(5);
        dbBox.setAlignment(Pos.CENTER);
        dbStatusIndicator = new Circle(12, Color.GRAY);
        dbBox.getChildren().addAll(dbStatusIndicator, new Label("PostgreSQL") {
            {
                setStyle("-fx-text-fill: white;");
            }
        });

        // Redis Status
        VBox redisBox = new VBox(5);
        redisBox.setAlignment(Pos.CENTER);
        redisStatusIndicator = new Circle(12, Color.GRAY);
        redisBox.getChildren().addAll(redisStatusIndicator, new Label("Redis") {
            {
                setStyle("-fx-text-fill: white;");
            }
        });

        statusRow.getChildren().addAll(grpcBox, dbBox, redisBox);

        // Control Buttons
        HBox controls = new HBox(10);
        Button btnStartDb = new Button("Start Database");
        btnStartDb.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnStartDb.setOnAction(e -> startDatabase());

        Button btnStopDb = new Button("Stop Database");
        btnStopDb.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnStopDb.setOnAction(e -> stopDatabase());

        Button btnClearLog = new Button("Clear Log");
        btnClearLog.setOnAction(e -> logArea.clear());

        controls.getChildren().addAll(btnStartDb, btnStopDb, btnClearLog);

        // Log Area
        Label logLabel = new Label("Server Log");
        logLabel.setStyle("-fx-text-fill: #888;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #0a0a1a; -fx-text-fill: #00ff00; -fx-font-family: monospace;");
        logArea.setPrefHeight(350);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        panel.getChildren().addAll(title, new Separator(), statusRow, controls, new Separator(), logLabel, logArea);
        return panel;
    }

    private VBox createSimulationManager() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("Create New Simulation");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        TextField nameField = new TextField("New World");
        TextField latField = new TextField("48.8566");
        TextField lonField = new TextField("2.3522");
        ComboBox<String> terrainSelect = new ComboBox<>();
        terrainSelect.getItems().addAll("Perlin Hills", "Flat Plains", "Desert Dunes", "Forest");
        terrainSelect.getSelectionModel().selectFirst();

        ComboBox<String> speciesSelect = new ComboBox<>();
        speciesSelect.getItems().addAll("Formica rufa", "Lasius niger", "Atta cephalotes", "Custom...");
        speciesSelect.getSelectionModel().selectFirst();

        form.add(label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(label("Latitude:"), 0, 1);
        form.add(latField, 1, 1);
        form.add(label("Longitude:"), 0, 2);
        form.add(lonField, 1, 2);
        form.add(label("Terrain:"), 0, 3);
        form.add(terrainSelect, 1, 3);
        form.add(label("Species:"), 0, 4);
        form.add(speciesSelect, 1, 4);

        // Buttons
        HBox buttons = new HBox(10);
        Button btnCreate = new Button("Create & Launch");
        btnCreate.setStyle("-fx-background-color: #00d4ff; -fx-text-fill: black; -fx-font-weight: bold;");
        btnCreate.setOnAction(
                e -> createSimulation(nameField.getText(), terrainSelect.getValue(), speciesSelect.getValue()));

        buttons.getChildren().add(btnCreate);

        // Saved Simulations List
        Label savedLabel = new Label("Saved Simulations (Database)");
        savedLabel.setStyle("-fx-text-fill: #888;");

        savedSimsList = new ListView<>();
        savedSimsList.setPrefHeight(200);

        HBox listControls = new HBox(10);
        Button btnRefresh = new Button("Refresh List");
        btnRefresh.setOnAction(e -> refreshSavedSims());

        Button btnLoad = new Button("Load Selected");
        btnLoad.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnLoad.setOnAction(e -> loadSelectedSimulation());

        listControls.getChildren().addAll(btnRefresh, btnLoad);

        panel.getChildren().addAll(title, new Separator(), form, buttons, new Separator(), savedLabel, listControls,
                savedSimsList);
        return panel;
    }

    private Button btnPause;
    private Button btnStop;

    private VBox createDashboard() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("Running Simulations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        // Split: Simulations | Clients
        HBox split = new HBox(20);
        HBox.setHgrow(split, Priority.ALWAYS);

        // Running Simulations
        VBox simsPane = new VBox(10);
        simsPane.setPrefWidth(400);
        Label simsLabel = new Label("Active Simulations");
        simsLabel.setStyle("-fx-text-fill: white;");

        runningSimsList = new ListView<>();
        // runningSimsList.getItems().addAll(
        // "▶ Demo World - Tick: 12,450 - Pop: 342",
        // "⏸ Test Colony - Tick: 5,200 - Pop: 89");
        runningSimsList.setPrefHeight(200);

        HBox simControls = new HBox(10);
        btnPause = new Button("⏸ Pause");
        btnPause.setOnAction(e -> togglePauseSimulation());

        btnStop = new Button("⏹ Stop");
        btnStop.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnStop.setOnAction(e -> stopSimulation());

        simControls.getChildren().addAll(btnPause, btnStop);

        simsPane.getChildren().addAll(simsLabel, runningSimsList, simControls);

        // Connected Clients
        VBox clientsPane = new VBox(10);
        clientsPane.setPrefWidth(400);
        Label clientsLabel = new Label("Connected Clients");
        clientsLabel.setStyle("-fx-text-fill: white;");

        connectedClientsList = new ListView<>();
        connectedClientsList.setPrefHeight(200);

        Button btnKick = new Button("Disconnect Selected");
        btnKick.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        btnKick.setOnAction(e -> kickSelectedClient());

        clientsPane.getChildren().addAll(clientsLabel, connectedClientsList, btnKick);

        split.getChildren().addAll(simsPane, clientsPane);

        // Server Stats
        Label statsLabel = new Label("Server Statistics");
        statsLabel.setStyle("-fx-text-fill: #888;");

        HBox stats = new HBox(30);
        stats.getChildren().addAll(
                stat("CPU", "45%"), // Placeholder
                stat("Memory", "2.1 GB"), // Placeholder
                stat("Uptime", "0s"),
                stat("Total Ticks", "0"));

        panel.getChildren().addAll(title, new Separator(), split, new Separator(), statsLabel, stats);

        // Start dashboard polling
        startDashboardPolling();

        return panel;
    }

    private void startDashboardPolling() {
        new Thread(() -> {
            while (true) {
                if (server != null) {
                    // Update connected clients
                    java.util.List<String> clients = server.getConnectedClients();

                    // Update UI
                    Platform.runLater(() -> {
                        // Update Client List if changed
                        if (!connectedClientsList.getItems().equals(clients)) {
                            connectedClientsList.getItems().setAll(clients);
                        }

                        // Update Sim Controls
                        if (server.isSimulationPaused()) {
                            btnPause.setText("▶ Resume");
                            btnPause.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                        } else {
                            btnPause.setText("⏸ Pause");
                            btnPause.setStyle("");
                        }

                        // Update Running Sim List (Mock for now, just main sim)
                        if (server.isSimulationRunning() || server.isSimulationPaused()) {
                            // Tick access removed as unused
                            // server.getSimulation().getTickCount() - unsafe if null.
                            // But for GUI simplicity we assume main sim.
                            String status = server.isSimulationPaused() ? "⏸" : "▶";
                            String item = status + " Main Simulation (Active)";
                            if (runningSimsList.getItems().isEmpty()) {
                                runningSimsList.getItems().add(item);
                            } else {
                                runningSimsList.getItems().set(0, item);
                            }
                        } else {
                            runningSimsList.getItems().clear();
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void togglePauseSimulation() {
        if (server == null)
            return;
        if (server.isSimulationPaused()) {
            server.resumeSimulation();
        } else {
            server.pauseSimulation();
        }
    }

    private void stopSimulation() {
        if (server != null) {
            server.stopSimulation();
            appendLog("Simulation stopped.");
        }
    }

    private void kickSelectedClient() {
        if (server == null)
            return;
        String client = connectedClientsList.getSelectionModel().getSelectedItem();
        if (client != null) {
            server.kickClient(client);
            appendLog("Kicked client: " + client);
        }
    }

    // ... label helpers ...

    private void startServer() {
        appendLog("Starting SwarmForge Server...");
        grpcStatusIndicator.setFill(Color.YELLOW);

        new Thread(() -> {
            try {
                server = new SwarmForgeServer(ServerConfig.fromEnvironment());
                server.createDemoWorld();
                server.start();

                Platform.runLater(() -> {
                    grpcStatusIndicator.setFill(Color.LIMEGREEN);
                    appendLog("✓ gRPC Server started on port 50051");
                    updateDbStatus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    grpcStatusIndicator.setFill(Color.RED);
                    appendLog("✗ Server failed: " + e.getMessage());
                });
            }
        }).start();
    }

    private void startDatabase() {
        if (server != null) {
            new Thread(() -> {
                try {
                    server.connectDatabase();
                    server.connectCache();
                    Platform.runLater(() -> appendLog("✓ Database connection request sent"));
                } catch (Exception e) {
                    Platform.runLater(() -> appendLog("✗ Database connection failed: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void stopDatabase() {
        if (server != null) {
            server.disconnectDatabase();
            server.disconnectCache();
            appendLog("Database disconnection request sent");
        }
    }

    private void updateDbStatus() {
        if (server == null)
            return;

        // Poll connections periodically
        new Thread(() -> {
            try {
                while (server != null) {
                    // Check DB
                    boolean dbOk = server.isDatabaseConnected();
                    Platform.runLater(() -> {
                        dbStatusIndicator.setFill(dbOk ? Color.LIMEGREEN : Color.RED);
                    });

                    // Check Redis
                    boolean redisOk = server.isRedisConnected();
                    Platform.runLater(() -> {
                        redisStatusIndicator.setFill(redisOk ? Color.LIMEGREEN : Color.RED);
                    });

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // Stopped
            }
        }).start();
    }

    private void createSimulation(String name, String terrain, String species) {
        if (server == null)
            return;
        appendLog("Creating simulation: " + name + "...");

        new Thread(() -> {
            try {
                server.createNewWorld(name, terrain, species);
                Platform.runLater(() -> {
                    appendLog("✓ World created: " + name);
                    if (runningSimsList != null) {
                        runningSimsList.getItems().add("▶ " + name + " (Running)");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> appendLog("✗ Creation failed: " + e.getMessage()));
            }
        }).start();
    }

    private void refreshSavedSims() {
        if (server == null)
            return;
        appendLog("Fetching saved worlds...");
        new Thread(() -> {
            var worlds = server.getAvailableWorlds();
            Platform.runLater(() -> {
                if (savedSimsList != null) {
                    savedSimsList.getItems().setAll(worlds);
                    appendLog("Found " + worlds.size() + " saved worlds.");
                }
            });
        }).start();
    }

    private void loadSelectedSimulation() {
        if (savedSimsList == null)
            return;
        String selected = savedSimsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendLog("⚠ Select a world to load first.");
            return;
        }

        // Format is "Name [UUID]" or similar.
        String uuidStr = "";
        try {
            uuidStr = selected.substring(selected.lastIndexOf('[') + 1, selected.lastIndexOf(']'));
        } catch (IndexOutOfBoundsException e) {
            appendLog("⚠ Invalid format for selected world: " + selected);
            return;
        }

        appendLog("Loading world " + uuidStr + "...");
        String finalUuid = uuidStr;
        new Thread(() -> {
            try {
                server.loadWorld(finalUuid);
                Platform.runLater(() -> {
                    appendLog("✓ World loaded: " + selected);
                    if (runningSimsList != null) {
                        runningSimsList.getItems().add("▶ " + selected + " (Loaded)");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> appendLog("✗ Load failed: " + e.getMessage()));
            }
        }).start();
    }

    private void appendLog(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private javafx.scene.Node stat(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private Label label(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #ccc;");
        return lbl;
    }
}
