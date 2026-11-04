/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.server.net.SwarmForgeWebSocketServer;
import org.swarmforge.server.persistence.DatabaseManager;
import org.swarmforge.server.persistence.RedisCache;
import org.swarmforge.server.simulation.SimulationManager;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Handles TLS certificate provisioning, WebSocket daemon initialization,
 * and background persistence workers for {@link SwarmForgeServer}.
 */
class ServerLifecycleHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ServerLifecycleHelper.class);

    private ServerLifecycleHelper() {}

    static SslContext createSslContextIfEnabled() {
        if (!"true".equalsIgnoreCase(System.getenv("ENABLE_TLS"))) {
            LOG.info("gRPC Server running in plaintext mode (set ENABLE_TLS=true to activate TLS)");
            return null;
        }
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext ctx = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(io.netty.handler.ssl.SslProvider.OPENSSL)
                    .build();
            LOG.info("TLS Enabled using self-signed certificate (OpenSSL)");
            return ctx;
        } catch (Throwable e) {
            LOG.warn("Failed to initialize OpenSSL TLS, trying JDK SSL: {}", e.getMessage());
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                SslContext ctx = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey()).build();
                LOG.info("TLS Enabled using JDK SSL");
                return ctx;
            } catch (Throwable e2) {
                LOG.warn("Could not initialize TLS, running plaintext: {}", e2.getMessage());
                return null;
            }
        }
    }

    static SwarmForgeWebSocketServer startWebSocketServer(SimulationManager simulationManager) {
        try {
            int wsPort = 8081;
            for (int p = wsPort; p < wsPort + 10; p++) {
                if (!isPortOpen("localhost", p)) {
                    try {
                        SwarmForgeWebSocketServer ws = new SwarmForgeWebSocketServer(p, simulationManager);
                        ws.start();
                        LOG.info("WebSocket Server started on port {}", p);
                        return ws;
                    } catch (Exception ex) {
                        LOG.warn("Could not bind WebSocket on port {}, trying next: {}", p, ex.getMessage());
                    }
                }
            }
            LOG.warn("Could not find an available port for WebSocket Server (8081-8090 are busy).");
        } catch (Exception e) {
            LOG.warn("Failed to start WebSocket Server: {}", e.getMessage());
        }
        return null;
    }

    static void startBackgroundWorkers(
            RedisCache cache,
            DatabaseManager database,
            SimulationManager simulationManager,
            ServerWorldManager worldManager) {
        // Redis live position streamer
        Thread.ofVirtual().name("redis-updater").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (cache.isConnected()) {
                    for (var entry : simulationManager.getAllSimulations().entrySet()) {
                        String simId = entry.getKey();
                        Simulation sim = entry.getValue();
                        if (sim.getState() == Simulation.State.RUNNING) {
                            try {
                                cache.setTick(simId, sim.getTickCount());
                                for (Colony colony : sim.getColonies()) {
                                    for (Individual ind : colony.getLivingIndividuals()) {
                                        cache.setIndividualPosition(simId, ind.getId().toString(),
                                                ind.getX(), ind.getY(), ind.getZ());
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Error updating Redis for {}: {}", simId, e.getMessage());
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Periodic Database Checkpointer
        Thread.ofVirtual().name("db-checkpointer").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000);
                    if (database.isConnected()) {
                        Simulation sim = simulationManager.getSimulation("main").orElse(null);
                        if (sim != null && sim.getState() == Simulation.State.RUNNING) {
                            worldManager.saveWorld("Autosave (Tick " + sim.getTickCount() + ")");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    LOG.warn("Auto-checkpoint failed: {}", ex.getMessage());
                }
            }
        });
    }

    static boolean isPortOpen(String host, int port) {
        if (host == null || host.trim().isEmpty() || port <= 0) return false;
        String targetHost = ("0.0.0.0".equals(host.trim()) || host.trim().isEmpty()) ? "localhost" : host.trim();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHost, port), 600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
