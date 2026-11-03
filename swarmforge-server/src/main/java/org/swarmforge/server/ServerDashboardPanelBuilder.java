/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Builds and manages the active simulations and connected clients dashboard panel
 * for {@link ServerGuiApp}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class ServerDashboardPanelBuilder {

    private ServerDashboardPanelBuilder() {
    }

    static VBox createDashboard(ServerGuiApp app) {
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

        app.runningSimsList = new ListView<>();
        app.runningSimsList.setPrefHeight(200);

        HBox simControls = new HBox(10);
        app.btnPause = new Button("⏸ Pause");
        app.btnPause.setOnAction(e -> togglePauseSimulation(app));

        app.btnStop = new Button("⏹ Stop");
        app.btnStop.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        app.btnStop.setOnAction(e -> stopSimulation(app));

        simControls.getChildren().addAll(app.btnPause, app.btnStop);
        simsPane.getChildren().addAll(simsLabel, app.runningSimsList, simControls);

        // Connected Clients
        VBox clientsPane = new VBox(10);
        clientsPane.setPrefWidth(400);
        Label clientsLabel = new Label("Connected Clients");
        clientsLabel.setStyle("-fx-text-fill: white;");

        app.connectedClientsList = new ListView<>();
        app.connectedClientsList.setPrefHeight(200);

        Button btnKick = new Button("Disconnect Selected");
        btnKick.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        btnKick.setOnAction(e -> kickSelectedClient(app));

        clientsPane.getChildren().addAll(clientsLabel, app.connectedClientsList, btnKick);
        split.getChildren().addAll(simsPane, clientsPane);

        // Server Stats
        Label statsLabel = new Label("Server Statistics");
        statsLabel.setStyle("-fx-text-fill: #888;");

        HBox stats = new HBox(30);
        stats.getChildren().addAll(
                stat("CPU", "45%"),
                stat("Memory", "2.1 GB"),
                stat("Uptime", "0s"),
                stat("Total Ticks", "0"));

        panel.getChildren().addAll(title, new Separator(), split, new Separator(), statsLabel, stats);

        startDashboardPolling(app);

        return panel;
    }

    static VBox stat(String title, String value) {
        VBox box = new VBox(5);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        box.getChildren().addAll(titleLbl, valLbl);
        return box;
    }

    private static void startDashboardPolling(ServerGuiApp app) {
        new Thread(() -> {
            while (true) {
                if (app.server != null) {
                    List<String> clients = app.server.getConnectedClients();

                    Platform.runLater(() -> {
                        if (!app.connectedClientsList.getItems().equals(clients)) {
                            app.connectedClientsList.getItems().setAll(clients);
                        }

                        if (app.server.isSimulationPaused()) {
                            app.btnPause.setText("▶ Resume");
                            app.btnPause.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                        } else {
                            app.btnPause.setText("⏸ Pause");
                            app.btnPause.setStyle("");
                        }

                        if (app.server.isSimulationRunning() || app.server.isSimulationPaused()) {
                            String status = app.server.isSimulationPaused() ? "⏸" : "▶";
                            String item = status + " Main Simulation (Active)";
                            if (app.runningSimsList.getItems().isEmpty()) {
                                app.runningSimsList.getItems().add(item);
                            } else {
                                app.runningSimsList.getItems().set(0, item);
                            }
                        } else {
                            app.runningSimsList.getItems().clear();
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private static void togglePauseSimulation(ServerGuiApp app) {
        if (app.server == null) return;
        if (app.server.isSimulationPaused()) {
            app.server.resumeSimulation();
        } else {
            app.server.pauseSimulation();
        }
    }

    private static void stopSimulation(ServerGuiApp app) {
        if (app.server != null) {
            app.server.stopSimulation();
            app.appendLog("Simulation stopped.");
        }
    }

    private static void kickSelectedClient(ServerGuiApp app) {
        if (app.server == null) return;
        String client = app.connectedClientsList.getSelectionModel().getSelectedItem();
        if (client != null) {
            app.server.kickClient(client);
            app.appendLog("Kicked client: " + client);
        }
    }
}
