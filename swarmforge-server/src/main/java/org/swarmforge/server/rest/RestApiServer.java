/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.rest;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Simple REST API server using built-in HttpServer.
 * Alternative to gRPC for web clients.
 *
 * @author Gemini AI Assistant
 */
public class RestApiServer {

    private static final Logger LOG = Logger.getLogger(RestApiServer.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpServer server;
    private final int port;
    private org.swarmforge.core.simulation.Simulation simulation;

    public RestApiServer(int port) {
        this.port = port;
    }

    public void setSimulation(org.swarmforge.core.simulation.Simulation simulation) {
        this.simulation = simulation;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API endpoints
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/colonies", new ColoniesHandler());
        server.createContext("/api/simulation/control", new ControlHandler());
        server.createContext("/api/world", new WorldHandler());

        server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        LOG.info("REST API started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendJson(HttpExchange exchange, Object obj) throws IOException {
        String json = MAPPER.writeValueAsString(obj);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        handleCors(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        Map<String, Object> error = Map.of("error", message, "code", code);
        String json = MAPPER.writeValueAsString(error);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        handleCors(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // === Handlers ===

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            Map<String, Object> status = new HashMap<>();
            status.put("running", simulation != null && simulation.isRunning());
            status.put("tick", simulation != null ? simulation.getTickCount() : 0);
            status.put("colonies", simulation != null ? simulation.getColonies().size() : 0);
            status.put("version", "2.0.0");

            sendJson(exchange, status);
        }
    }

    class ColoniesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            if (simulation == null) {
                sendError(exchange, 503, "Simulation not running");
                return;
            }

            List<Map<String, Object>> colonies = new ArrayList<>();
            for (var colony : simulation.getColonies()) {
                Map<String, Object> c = new HashMap<>();
                c.put("id", colony.getId().toString());
                c.put("species", colony.getSpeciesName());
                c.put("population", colony.getPopulation());
                c.put("food", colony.getFoodStored());
                c.put("nestX", colony.getNestX());
                c.put("nestY", colony.getNestY());
                c.put("nestZ", colony.getNestZ());
                colonies.add(c);
            }

            sendJson(exchange, colonies);
        }
    }

    class ControlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            // Read body
            String body;
            try (var reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                body = reader.lines().reduce("", String::concat);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> request = MAPPER.readValue(body, Map.class);
            String action = (String) request.get("action");

            Map<String, Object> response = new HashMap<>();

            if (simulation != null) {
                switch (action) {
                    case "start" -> {
                        simulation.start();
                        response.put("status", "started");
                    }
                    case "pause" -> {
                        simulation.pause();
                        response.put("status", "paused");
                    }
                    case "stop" -> {
                        simulation.stop();
                        response.put("status", "stopped");
                    }
                    default -> {
                        response.put("error", "Unknown action");
                    }
                }
            } else {
                response.put("error", "No simulation");
            }

            sendJson(exchange, response);
        }
    }

    class WorldHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            if (simulation == null) {
                sendError(exchange, 503, "Simulation not running");
                return;
            }

            var terrarium = simulation.getTerrarium();
            Map<String, Object> world = new HashMap<>();
            world.put("width", terrarium.getWidth());
            world.put("height", terrarium.getHeight());
            world.put("depth", terrarium.getDepth());

            sendJson(exchange, world);
        }
    }
}
