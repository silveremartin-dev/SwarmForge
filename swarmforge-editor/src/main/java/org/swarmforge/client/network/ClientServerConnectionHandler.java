/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.network;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.swarmforge.client.SwarmForgeClient;
import org.swarmforge.client.ui.ServerLauncherDialog;
import org.swarmforge.client.ui.SimulationControlPanel;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.server.ServerConfig;

/**
 * Handles remote and local gRPC server connection dialogs, auto-discovery,
 * and lifecycle hooks for the SwarmForge desktop client.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ClientServerConnectionHandler {

    private static final Logger LOG = Logger.getLogger(ClientServerConnectionHandler.class.getName());

    public static void autoConnectStandalone(SimulationClient networkClient, SimulationControlPanel simControlPanel) {
        Thread.ofVirtual().start(() -> {
            boolean reachable = false;
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("localhost", 50051), 350);
                reachable = true;
            } catch (Throwable ignored) {
            }
            if (reachable) {
                try {
                    networkClient.connect("localhost", 50051);
                    networkClient.startStreaming();
                    LOG.info("Auto-connected to SwarmForge server at localhost:50051");
                    Platform.runLater(() -> {
                        if (simControlPanel != null) {
                            simControlPanel.setServerConnectionStatus(true, "localhost:50051");
                        }
                    });
                } catch (Throwable ex) {
                    Platform.runLater(() -> {
                        if (simControlPanel != null) {
                            simControlPanel.setServerConnectionStatus(false, "Hors-ligne");
                        }
                    });
                }
            } else {
                LOG.info("Standalone mode active (no SwarmForge server detected on localhost:50051)");
                Platform.runLater(() -> {
                    if (simControlPanel != null) {
                        simControlPanel.setServerConnectionStatus(false, "Hors-ligne");
                    }
                });
            }
        });
    }

    public static void promptAndStartLocalServer(
            String targetHost,
            int targetPort,
            SimulationClient networkClient,
            SimulationControlPanel simControlPanel,
            GameViewPane gameView,
            Runnable onServerReady) {

        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> promptAndStartLocalServer(targetHost, targetPort, networkClient, simControlPanel, gameView, onServerReady));
            return;
        }

        ServerLauncherDialog dialog = new ServerLauncherDialog(
                targetHost != null ? targetHost : "localhost",
                targetPort > 0 ? targetPort : 50051
        );
        Optional<ServerConfig> optConfig = dialog.showAndWait();
        optConfig.ifPresent(config -> {
            if (simControlPanel != null) {
                simControlPanel.setServerConnectionStatus(false, "Démarrage du serveur local...");
            }
            LocalServerManager.getInstance().startServer(
                    config,
                    () -> {
                        if (simControlPanel != null) {
                            simControlPanel.setServerHost("localhost");
                            simControlPanel.setServerPort(config.grpcPort());
                        }
                        try {
                            networkClient.connect("localhost", config.grpcPort());
                            networkClient.startStreaming();
                            if (simControlPanel != null) {
                                simControlPanel.setServerConnectionStatus(true, "localhost:" + config.grpcPort() + " (Serveur Local)");
                            }
                            if (gameView != null && gameView.getGameApp() != null) {
                                gameView.getGameApp().setNetworkClient(networkClient);
                            }
                            if (onServerReady != null) {
                                onServerReady.run();
                            }
                        } catch (Exception ex) {
                            if (simControlPanel != null) {
                                simControlPanel.setServerConnectionStatus(false, "Erreur connexion: " + ex.getMessage());
                            }
                        }
                    },
                    err -> {
                        if (simControlPanel != null) {
                            simControlPanel.setServerConnectionStatus(false, "Erreur démarrage: " + err.getMessage());
                        }
                        ThemeManager.createAlert(
                                Alert.AlertType.ERROR,
                                "Échec du démarrage du serveur SwarmForge : " + err.getMessage()
                        ).showAndWait();
                    }
            );
        });
    }
}
