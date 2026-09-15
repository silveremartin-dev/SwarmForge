/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * SwarmForge Client - High-Fidelity 3D Simulation Viewer & God Mode Controller.
 * Directly launches the full-screen Simulation Manager with complete real-time controls,
 * 3D viewport, God Mode interventions, metrics dashboard, event log, and gRPC auto-connection.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ClientApp extends Application {

    private static final Logger LOG = Logger.getLogger(ClientApp.class.getName());

    @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting SwarmForge Dedicated Client (Simulation Manager)...");
        org.swarmforge.client.util.IconUtils.initEarlyTaskbarAppId();
        SwarmForgeClient.silenceJme3Loggers();
        SwarmForgeClient.setClientOnlyMode(true);
        new SwarmForgeClient().start(primaryStage);
    }

    public static void main(String[] args) {
        org.swarmforge.client.util.IconUtils.initEarlyTaskbarAppId();
        SwarmForgeClient.silenceJme3Loggers();
        SwarmForgeClient.setClientOnlyMode(true);
        SwarmForgeClient.main(args);
    }
}
