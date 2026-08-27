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

        // Load Application Icon
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            LOG.warning("Could not load application icon: " + e.getMessage());
        }

        // Connection Dialog
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Header with Icon
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER);
        
        try {
            java.io.InputStream headerIconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (headerIconStream != null) {
                javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(headerIconStream));
                logoView.setFitWidth(40);
                logoView.setFitHeight(40);
                logoView.setPreserveRatio(true);
                titleBox.getChildren().add(logoView);
            }
        } catch (Exception ignored) {}

        // Title
        Label title = new Label(I18nManager.getInstance().get("client.title"));
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        titleBox.getChildren().add(title);

        Label subtitle = new Label(I18nManager.getInstance().get("client.subtitle"));
        subtitle.setStyle("-fx-font-size: 14px;");

        // Server Connection Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        Label lblHost = new Label(I18nManager.getInstance().get("client.host"));
        TextField txtHost = new TextField(serverHost);
        txtHost.setPromptText("localhost");

        Label lblPort = new Label(I18nManager.getInstance().get("client.port"));
        lblPort.setId("lblPort");
        TextField txtPort = new TextField(String.valueOf(serverPort));
        txtPort.setPromptText("50051");

        form.add(lblHost, 0, 0);
        form.add(txtHost, 1, 0);
        form.add(lblPort, 0, 1);
        form.add(txtPort, 1, 1);

        // God Mode Toggle
        CheckBox chkGodMode = new CheckBox(I18nManager.getInstance().get("client.godmode"));
        chkGodMode.setId("godModeToggle");
        chkGodMode.setSelected(false);

        // Simulation List (placeholder)
        ListView<String> simList = new ListView<>();
        simList.getItems().addAll(
                "⏳ Connect to see available simulations...");
        simList.setPrefHeight(150);
        simList.setId("simulationList");

        // Buttons
        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button btnRefresh = new Button(I18nManager.getInstance().get("client.refresh"));
        btnRefresh.setId("btnRefresh");
        btnRefresh.setOnAction(e -> {
            serverHost = txtHost.getText();
            serverPort = Integer.parseInt(txtPort.getText());
            refreshSimulationList(simList);
        });

        Button btnConnect = new Button(I18nManager.getInstance().get("client.connect"));
        btnConnect.setStyle("-fx-font-weight: bold;");
        btnConnect.setOnAction(e -> {
            serverHost = txtHost.getText();
            serverPort = Integer.parseInt(txtPort.getText());
            godModeEnabled = chkGodMode.isSelected();
            String selected = simList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.startsWith("⏳")) {
                launchViewer(primaryStage, selected);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a simulation first");
                alert.show();
            }
        });

        buttons.getChildren().addAll(btnRefresh, btnConnect);

        // Status
        Label status = new Label(I18nManager.getInstance().get("status.ready"));
        status.setId("statusLabel");

        Label availableSimsLabel = new Label(I18nManager.getInstance().get("client.available_sims"));
        availableSimsLabel.setId("availableSimsLabel");

        root.getChildren().addAll(titleBox, subtitle, new Separator(), form, chkGodMode,
                availableSimsLabel,
                simList, buttons, status);

        Scene scene = new Scene(root, 500, 570);
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
        viewerRoot.setId("viewerRoot");

        // Info Panel instead of 3D placeholder
        VBox infoPanel = new VBox(10);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setPadding(new Insets(20));
        Label infoTitle = new Label(I18nManager.getInstance().get("client.controller_active"));
        infoTitle.setStyle("-fx-font-size: 24px;");
        Label infoDesc = new Label(
                "3D Simulation is running in a separate window.\nUse this window for God Mode controls.");
        infoDesc.setStyle("-fx-text-alignment: center;");
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
        panel.setId("godModePanel");
        panel.setPadding(new Insets(10));
        panel.setMinWidth(250);

        Label title = new Label(I18nManager.getInstance().get("godmode.title"));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Intervention buttons
        Button btnAddFood = new Button(I18nManager.getInstance().get("godmode.add_food"));
        Button btnSpawnAnts = new Button(I18nManager.getInstance().get("godmode.spawn_ants"));
        Button btnTriggerRain = new Button(I18nManager.getInstance().get("godmode.trigger_rain"));
        Button btnTriggerHeat = new Button(I18nManager.getInstance().get("godmode.heat_wave"));
        Button btnKillSelected = new Button(I18nManager.getInstance().get("godmode.kill_selected"));

        btnKillSelected.setId("btnKillSelected");

        Label heightLabel = new Label("Spawn");
        heightLabel.setId("spawnLabel");

        Label eventsLabel = new Label("Events");
        eventsLabel.setId("eventsLabel");

        Label destroyLabel = new Label("Destroy");
        destroyLabel.setId("destroyLabel");

        panel.getChildren().addAll(
                title, new Separator(),
                heightLabel,
                btnAddFood, btnSpawnAnts,
                new Separator(),
                eventsLabel,
                btnTriggerRain, btnTriggerHeat,
                new Separator(),
                destroyLabel,
                btnKillSelected);

        return panel;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
