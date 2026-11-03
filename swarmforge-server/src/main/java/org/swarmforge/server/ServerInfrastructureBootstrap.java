/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health-checking and infrastructure service bootstrapper (PostgreSQL / Redis Docker containers).
 */
public final class ServerInfrastructureBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(ServerInfrastructureBootstrap.class);

    private ServerInfrastructureBootstrap() {
    }

    /**
     * Checks if Postgres and Redis ports are open, attempting to start via docker-compose if configured.
     *
     * @param config Server configuration
     */
    public static void checkAndAutoStartInfrastructure(ServerConfig config) {
        if (config.dbHost() == null || config.dbHost().trim().isEmpty() || config.dbPort() <= 0) {
            return;
        }
        try {
            boolean postgresRunning = isPortOpen(config.dbHost(), config.dbPort());
            boolean redisRunning = (config.redisHost() != null && !config.redisHost().trim().isEmpty() && config.redisPort() > 0)
                    ? isPortOpen(config.redisHost(), config.redisPort())
                    : true;

            if (!postgresRunning || !redisRunning) {
                LOG.warn("Infrastructure (Postgres/Redis) appears down. Attempting auto-start...");
                Process p = null;
                try {
                    p = new ProcessBuilder("docker-compose", "up", "-d").inheritIO().start();
                } catch (Exception e) {
                    try {
                        p = new ProcessBuilder("docker", "compose", "up", "-d").inheritIO().start();
                    } catch (Exception ex) {
                        LOG.warn("Could not invoke docker-compose: {}", ex.getMessage());
                    }
                }
                if (p != null) {
                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        LOG.info("Auto-start command executed. Waiting for services to initialize...");
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(500);
                            if (isPortOpen(config.dbHost(), config.dbPort())) {
                                LOG.info("Services are now reachable.");
                                break;
                            }
                        }
                    } else {
                        LOG.warn("Docker auto-start returned exit code: {}", exitCode);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Error verifying infrastructure: {}", e.getMessage());
        }
    }

    /**
     * Probes whether a TCP socket port is responding within 600ms.
     *
     * @param host Hostname or IP
     * @param port Target port
     * @return true if open and accepted connection
     */
    public static boolean isPortOpen(String host, int port) {
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
