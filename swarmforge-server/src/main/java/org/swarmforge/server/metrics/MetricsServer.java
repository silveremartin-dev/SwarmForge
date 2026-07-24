/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.metrics;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * HTTP server for Prometheus metrics scraping.
 * Exposes /metrics endpoint on configurable port (default 9090).
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class MetricsServer {

    private HttpServer server;
    private final MetricsExporter exporter;
    private final int port;
    private volatile boolean running = false;

    public MetricsServer(MetricsExporter exporter) {
        this(exporter, 9090);
    }

    public MetricsServer(MetricsExporter exporter, int port) {
        this.exporter = exporter;
        this.port = port;
    }

    /**
     * Start the metrics HTTP server.
     */
    public void start() throws IOException {
        if (running)
            return;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/metrics", new MetricsHandler());
        server.createContext("/health", new HealthHandler());
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        running = true;

        System.out.println("Metrics server started on http://localhost:" + port + "/metrics");
    }

    /**
     * Stop the metrics server.
     */
    public void stop() {
        if (server != null && running) {
            server.stop(1);
            running = false;
            System.out.println("Metrics server stopped");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    /**
     * Handler for /metrics endpoint.
     */
    private class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = exporter.export();
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /**
     * Handler for /health endpoint.
     */
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\"}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
