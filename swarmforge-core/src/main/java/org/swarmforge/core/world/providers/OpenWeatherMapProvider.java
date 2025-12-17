/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world.providers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Integration with OpenWeatherMap API for real-time weather data.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class OpenWeatherMapProvider {

    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final HttpClient httpClient;
    private final String apiKey;

    public OpenWeatherMapProvider(String apiKey) {
        this.httpClient = HttpClient.newBuilder().build();
        this.apiKey = apiKey;
    }

    /**
     * Fetch current weather for coordinates.
     */
    public CompletableFuture<String> fetchWeather(double lat, double lon) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric",
                API_URL, lat, lon, apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
