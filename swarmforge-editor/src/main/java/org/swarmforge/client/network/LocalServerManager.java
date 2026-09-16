/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.network;

import org.swarmforge.server.ServerConfig;
import org.swarmforge.server.SwarmForgeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle of a local in-process SwarmForge Server instance.
 * Allows starting, stopping, and querying server reachability from the GUI client.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LocalServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalServerManager.class);
    private static final LocalServerManager INSTANCE = new LocalServerManager();

    public static LocalServerManager getInstance() {
        return INSTANCE;
    }

    private SwarmForgeServer serverInstance;
    private Thread serverThread;
    private ServerConfig activeConfig;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private LocalServerManager() {
    }

    /**
     * Checks if a TCP port is reachable at given host.
     */
    public static boolean isPortOpen(String host, int port) {
        String targetHost = (host == null || host.trim().isEmpty() || "0.0.0.0".equals(host)) ? "localhost" : host.trim();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHost, port), 600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Starts the SwarmForge server asynchronously in a dedicated daemon thread.
     */
    public synchronized void startServer(ServerConfig config, Runnable onStarted, java.util.function.Consumer<Throwable> onError) {
        if (isRunning.get()) {
            LOG.info("Server is already running on port {}", activeConfig != null ? activeConfig.grpcPort() : 50051);
            if (onStarted != null) onStarted.run();
            return;
        }

        this.activeConfig = config;
        this.serverThread = new Thread(() -> {
            try {
                LOG.info("Starting in-process SwarmForgeServer on gRPC port {}...", config.grpcPort());
                serverInstance = new SwarmForgeServer(config);
                serverInstance.start();
                isRunning.set(true);
                LOG.info("SwarmForgeServer successfully initialized and listening on port {}", config.grpcPort());

                if (onStarted != null) {
                    javafx.application.Platform.runLater(onStarted);
                }

                // Keep thread alive while server is running
                while (isRunning.get()) {
                    Thread.sleep(1000);
                }
            } catch (Throwable t) {
                LOG.error("Failed to start SwarmForgeServer: {}", t.getMessage(), t);
                isRunning.set(false);
                serverInstance = null;
                if (onError != null) {
                    javafx.application.Platform.runLater(() -> onError.accept(t));
                }
            }
        }, "swarmforge-local-server-thread");

        serverThread.setDaemon(true);
        serverThread.start();
    }

    /**
     * Stops the local server instance.
     */
    public synchronized void stopServer() {
        if (!isRunning.get()) return;
        LOG.info("Stopping in-process SwarmForgeServer...");
        isRunning.set(false);
        if (serverInstance != null) {
            try {
                serverInstance.stop();
            } catch (Exception e) {
                LOG.warn("Error during SwarmForgeServer stop: {}", e.getMessage());
            }
            serverInstance = null;
        }
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
        LOG.info("SwarmForgeServer stopped.");
    }

    public boolean isServerRunning() {
        return isRunning.get();
    }

    public ServerConfig getActiveConfig() {
        return activeConfig;
    }

    public SwarmForgeServer getServerInstance() {
        return serverInstance;
    }
}
