/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.grpc;

import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * gRPC service implementation for simulation control.
 * Handles client connections, streaming, and simulation commands.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationServiceImpl {

    private static final Logger LOG = Logger.getLogger(SimulationServiceImpl.class.getName());

    private final Simulation simulation;
    private final Map<String, ClientSession> sessions;

    public SimulationServiceImpl(Simulation simulation) {
        this.simulation = simulation;
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * Get current simulation state.
     */
    public SimulationStateResponse getState(boolean includeIndividuals, boolean includeCells) {
        Terrarium terrarium = simulation.getTerrarium();

        return new SimulationStateResponse(
                simulation.getTickCount(),
                terrarium.getWidth(),
                terrarium.getHeight(),
                terrarium.getDepth(),
                simulation.getColonies().size(),
                getTotalPopulation(),
                simulation.getState().name());
    }

    /**
     * Control simulation (start/pause/stop).
     */
    public ControlResponse control(String action, int ticksPerSecond) {
        try {
            switch (action.toUpperCase()) {
                case "START" -> {
                    simulation.start();
                    LOG.info("Simulation started");
                }
                case "PAUSE" -> {
                    simulation.pause();
                    LOG.info("Simulation paused");
                }
                case "STOP" -> {
                    simulation.stop();
                    LOG.info("Simulation stopped");
                }
                case "SET_SPEED" -> {
                    simulation.setTicksPerSecond(ticksPerSecond);
                    LOG.info("Simulation speed set to " + ticksPerSecond + " TPS");
                }
            }
            return new ControlResponse(true, simulation.getState().name(), "OK");
        } catch (Exception e) {
            return new ControlResponse(false, simulation.getState().name(), e.getMessage());
        }
    }

    /**
     * Register a client session for streaming.
     */
    public String registerSession(float viewRadius, float cameraX, float cameraY, float cameraZ) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new ClientSession(sessionId, viewRadius, cameraX, cameraY, cameraZ));
        LOG.info("Client session registered: " + sessionId);
        return sessionId;
    }

    /**
     * Update client camera position.
     */
    public void updateCamera(String sessionId, float x, float y, float z, float viewRadius) {
        ClientSession session = sessions.get(sessionId);
        if (session != null) {
            session.updateCamera(x, y, z, viewRadius);
        }
    }

    /**
     * Unregister client session.
     */
    public void unregisterSession(String sessionId) {
        sessions.remove(sessionId);
        LOG.info("Client session unregistered: " + sessionId);
    }

    private int getTotalPopulation() {
        return simulation.getColonies().stream()
                .mapToInt(Colony::getPopulation)
                .sum();
    }

    // Response records
    public record SimulationStateResponse(
            long tick, int width, int height, int depth,
            int colonyCount, int population, String status) {
    }

    public record ControlResponse(boolean success, String status, String message) {
    }

    // Client session tracking
    private static class ClientSession {
        final String id;
        float viewRadius;
        float cameraX, cameraY, cameraZ;

        ClientSession(String id, float viewRadius, float x, float y, float z) {
            this.id = id;
            this.viewRadius = viewRadius;
            this.cameraX = x;
            this.cameraY = y;
            this.cameraZ = z;
        }

        void updateCamera(float x, float y, float z, float radius) {
            this.cameraX = x;
            this.cameraY = y;
            this.cameraZ = z;
            this.viewRadius = radius;
        }
    }
}
