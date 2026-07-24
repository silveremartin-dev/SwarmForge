/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.studio;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * View for server logs and controls.
 */
public class ServerConsoleView extends VBox {

    private final TextArea logArea;
    private final ServerStudio studioApp;
    private boolean isRunning = false;

    public ServerConsoleView(ServerStudio app) {
        this.studioApp = app;
        setSpacing(10);
        setPadding(new Insets(10));

        // Header
        HBox header = new HBox(10);
        Label statusLabel = new Label("Status: STOPPED");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");

        Button toggleBtn = new Button("Start Server");
        toggleBtn.setOnAction(e -> {
            if (!isRunning) {
                studioApp.startServer();
                statusLabel.setText("Status: RUNNING");
                statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
                toggleBtn.setText("Stop Server"); // Stop logic mostly fake for now as implemented
                isRunning = true;
            } else {
                // Toggle stop mostly symbolic here without refactoring Server to be stoppable
                statusLabel.setText("Status: STOPPING...");
                toggleBtn.setDisable(true);
            }
        });

        header.getChildren().addAll(toggleBtn, statusLabel);
        getChildren().add(header);

        // Log Area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle(
                "-fx-font-family: 'Consolas', monospace; -fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4;");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        getChildren().add(logArea);

        // Redirect SysOut
        redirectOutput();
    }

    private void redirectOutput() {
        PrintStream ps = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                javafx.application.Platform.runLater(() -> {
                    logArea.appendText(String.valueOf((char) b));
                });
            }
        });
        System.setOut(ps);
        System.setErr(ps);
    }
}
