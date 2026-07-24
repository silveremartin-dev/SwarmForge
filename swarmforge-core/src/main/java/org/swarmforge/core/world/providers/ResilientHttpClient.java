/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world.providers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resilient HTTP Client with exponential backoff retries, in-memory caching, and circuit breaker.
 */
public class ResilientHttpClient {

    private static final Logger LOG = Logger.getLogger(ResilientHttpClient.class.getName());
    private static final int MAX_RETRIES = 3;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();
    private volatile boolean circuitOpen = false;
    private volatile long circuitOpenedTime = 0;
    private static final long CIRCUIT_RESET_MS = 30_000; // 30 seconds

    public ResilientHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
    }

    public CompletableFuture<String> executeWithRetry(String url) {
        if (isCircuitOpen()) {
            LOG.warning("Circuit breaker is OPEN for URL: " + url + ". Returning cached or empty response.");
            String cached = responseCache.get(url);
            return CompletableFuture.completedFuture(cached != null ? cached : "");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(DEFAULT_TIMEOUT)
                .build();

        return sendWithRetry(request, url, 1);
    }

    private CompletableFuture<String> sendWithRetry(HttpRequest request, String url, int attempt) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        String body = response.body();
                        responseCache.put(url, body);
                        resetCircuit();
                        return CompletableFuture.completedFuture(body);
                    } else if (attempt <= MAX_RETRIES) {
                        LOG.warning("HTTP " + response.statusCode() + " on attempt " + attempt + " for " + url + ". Retrying...");
                        sleepBackoff(attempt);
                        return sendWithRetry(request, url, attempt + 1);
                    } else {
                        tripCircuit();
                        String cached = responseCache.get(url);
                        return CompletableFuture.completedFuture(cached != null ? cached : "");
                    }
                })
                .exceptionally(throwable -> {
                    if (attempt <= MAX_RETRIES) {
                        LOG.log(Level.WARNING, "Request failed (attempt " + attempt + "): " + throwable.getMessage() + ". Retrying...");
                        sleepBackoff(attempt);
                        return sendWithRetry(request, url, attempt + 1).join();
                    } else {
                        tripCircuit();
                        String cached = responseCache.get(url);
                        return cached != null ? cached : "";
                    }
                });
    }

    private boolean isCircuitOpen() {
        if (circuitOpen) {
            if (System.currentTimeMillis() - circuitOpenedTime > CIRCUIT_RESET_MS) {
                circuitOpen = false; // Half-open / reset
                return false;
            }
            return true;
        }
        return false;
    }

    private void tripCircuit() {
        this.circuitOpen = true;
        this.circuitOpenedTime = System.currentTimeMillis();
        LOG.severe("Circuit breaker TRIPPED! Falling back to cache for the next 30 seconds.");
    }

    private void resetCircuit() {
        this.circuitOpen = false;
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(200L * (1L << attempt));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
