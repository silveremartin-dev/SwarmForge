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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * SwarmForge Client - Simulation Viewer
 * Connects to a SwarmForge Server and displays running simulations.
 * Supports God Mode for interventions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ClientApp extends Application {

    private static final Logger LOG = Logger.getLogger(ClientApp.class.getName());

    private String serverHost = "localhost";
    private int serverPort = 50051;
    private boolean godModeEnabled = false;

    @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Client...");

        // Connection Dialog
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        // Title
        Label title = new Label("SwarmForge Viewer");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        Label subtitle = new Label("Connect to a running simulation");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        // Server Connection Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        Label lblHost = new Label("Server Host:");
        lblHost.setStyle("-fx-text-fill: white;");
        TextField txtHost = new TextField(serverHost);
        txtHost.setPromptText("localhost");

        Label lblPort = new Label("Server Port:");
        lblPort.setStyle("-fx-text-fill: white;");
        TextField txtPort = new TextField(String.valueOf(serverPort));
        txtPort.setPromptText("50051");

        form.add(lblHost, 0, 0);
        form.add(txtHost, 1, 0);
        form.add(lblPort, 0, 1);
        form.add(txtPort, 1, 1);

        // God Mode Toggle
        CheckBox chkGodMode = new CheckBox("Enable God Mode (Interventions)");
        chkGodMode.setStyle("-fx-text-fill: #ff6b6b;");
        chkGodMode.setSelected(false);

        // Simulation List (placeholder)
        ListView<String> simList = new ListView<>();
        simList.getItems().addAll(
                "⏳ Connect to see available simulations...");
        simList.setPrefHeight(150);
        simList.setStyle("-fx-background-color: #0f0f23; -fx-text-fill: white;");

        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button btnRefresh = new Button("Refresh List");
        btnRefresh.setStyle("-fx-background-color: #4a4a6a; -fx-text-fill: white;");
        btnRefresh.setOnAction(e -> {
            serverHost = txtHost.getText();
            serverPort = Integer.parseInt(txtPort.getText());
            refreshSimulationList(simList);
        });

        Button btnConnect = new Button("Connect & View");
        btnConnect.setStyle("-fx-background-color: #00d4ff; -fx-text-fill: black; -fx-font-weight: bold;");
        btnConnect.setOnAction(e -> {
            serverHost = txtHost.getText();
            serverPort = Integer.parseInt(txtPort.getText());
            godModeEnabled = chkGodMode.isSelected();
            String selected = simList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.startsWith("⏳")) {
                launchViewer(primaryStage, selected);
            } else {
                new Alert(Alert.AlertType.WARNING, "Please select a simulation first").show();
            }
        });

        buttons.getChildren().addAll(btnRefresh, btnConnect);

        // Status
        Label status = new Label("Ready");
        status.setStyle("-fx-text-fill: #666;");

        root.getChildren().addAll(title, subtitle, new Separator(), form, chkGodMode,
                new Label("Available Simulations:") {
                    {
                        setStyle("-fx-text-fill: white;");
                    }
                },
                simList, buttons, status);

        Scene scene = new Scene(root, 500, 550);
        primaryStage.setTitle("SwarmForge Viewer");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void refreshSimulationList(ListView<String> simList) {
        LOG.info("Connecting to " + serverHost + ":" + serverPort);
        simList.getItems().clear();
        simList.getItems().add("🔄 Connecting to server...");

        // Placeholder: In a real implementation, this would call ListSimulations gRPC
        if (serverHost.equals("localhost")) {
            Platform.runLater(() -> {
                simList.getItems().clear();
                simList.getItems().addAll(
                        "🌍 Demo World - Tick: 12,450 - Pop: 342",
                        "🏜️ Desert Simulation - Tick: 5,200 - Pop: 89",
                        "🌲 Forest Colony - Tick: 45,000 - Pop: 1,234");
            });
        } else {
            simList.getItems().add("❌ Connection failed (Mock)");
        }
    }

    private org.swarmforge.client.view.SwarmViewerApp viewerApp;
    private boolean isBridgeRunning = false;

    private void launchViewer(Stage primaryStage, String simulationName) {
        LOG.info("Launching viewer for: " + simulationName + " (God Mode: " + godModeEnabled + ")");

        // 1. Launch JMonkeyEngine Window (if not already running)
        if (viewerApp == null) {
            viewerApp = new org.swarmforge.client.view.SwarmViewerApp();
            viewerApp.start(); // Spawns JME thread and window
        }

        // 2. Start Mock Data Bridge
        startMockDataBridge();

        // 3. Setup JavaFX Controller Window (God Mode)
        BorderPane viewerRoot = new BorderPane();
        viewerRoot.setStyle("-fx-background-color: #1a1a2e;");

        // Info Panel instead of 3D placeholder
        VBox infoPanel = new VBox(10);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setPadding(new Insets(20));
        Label infoTitle = new Label("Controller Active");
        infoTitle.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 24px;");
        Label infoDesc = new Label(
                "3D Simulation is running in a separate window.\nUse this window for God Mode controls.");
        infoDesc.setStyle("-fx-text-fill: #888; -fx-text-alignment: center;");
        infoPanel.getChildren().addAll(infoTitle, infoDesc);

        viewerRoot.setCenter(infoPanel);

        // God Mode Panel (right side)
        if (godModeEnabled) {
            VBox godPanel = createGodModePanel();
            viewerRoot.setRight(godPanel);
        }

        Scene viewerScene = new Scene(viewerRoot, 400, 600); // Smaller controller window
        primaryStage.setScene(viewerScene);
        primaryStage.setTitle("SwarmForge Controller - " + simulationName);
        primaryStage.setX(100);
        primaryStage.setY(100);
    }

    private void startMockDataBridge() {
        if (isBridgeRunning)
            return;
        isBridgeRunning = true;

        Thread bridgeThread = new Thread(() -> {
            java.util.Random rand = new java.util.Random();
            // Mock ants
            String[] antIds = new String[50];
            float[] antX = new float[50];
            float[] antZ = new float[50];
            float[] antDirX = new float[50];
            float[] antDirZ = new float[50];

            for (int i = 0; i < 50; i++) {
                antIds[i] = "ant_" + i;
                antX[i] = 50 + (rand.nextFloat() - 0.5f) * 20;
                antZ[i] = 50 + (rand.nextFloat() - 0.5f) * 20;
                antDirX[i] = (rand.nextFloat() - 0.5f) * 0.2f;
                antDirZ[i] = (rand.nextFloat() - 0.5f) * 0.2f;
            }

            while (isBridgeRunning) {
                if (viewerApp != null) {
                    for (int i = 0; i < 50; i++) {
                        // Move
                        antX[i] += antDirX[i];
                        antZ[i] += antDirZ[i];

                        // Bounce
                        if (antX[i] < 0 || antX[i] > 100)
                            antDirX[i] *= -1;
                        if (antZ[i] < 0 || antZ[i] > 100)
                            antDirZ[i] *= -1;

                        viewerApp.updateEntity(antIds[i], antX[i], 0.5f, antZ[i]);
                    }
                }
                try {
                    Thread.sleep(16); // ~60 updates/sec
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        bridgeThread.setDaemon(true);
        bridgeThread.start();
    }

    private VBox createGodModePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2a2a4a; -fx-min-width: 250px;");

        Label title = new Label("⚡ GOD MODE");
        title.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Intervention buttons
        Button btnAddFood = new Button("Add Food Source");
        Button btnSpawnAnts = new Button("Spawn Ants");
        Button btnTriggerRain = new Button("Trigger Rain");
        Button btnTriggerHeat = new Button("Heat Wave");
        Button btnKillSelected = new Button("Kill Selected");

        btnKillSelected.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

        panel.getChildren().addAll(
                title, new Separator(),
                new Label("Spawn") {
                    {
                        setStyle("-fx-text-fill: #888;");
                    }
                },
                btnAddFood, btnSpawnAnts,
                new Separator(),
                new Label("Events") {
                    {
                        setStyle("-fx-text-fill: #888;");
                    }
                },
                btnTriggerRain, btnTriggerHeat,
                new Separator(),
                new Label("Destroy") {
                    {
                        setStyle("-fx-text-fill: #888;");
                    }
                },
                btnKillSelected);

        return panel;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
