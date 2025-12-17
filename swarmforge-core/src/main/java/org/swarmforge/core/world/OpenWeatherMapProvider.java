/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenWeatherMap API weather provider implementation.
 * Requires an API key from openweathermap.org
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * WeatherProvider provider = new OpenWeatherMapProvider("YOUR_API_KEY");
 * WeatherData weather = provider.getCurrentWeather(48.8566, 2.3522);
 * </pre>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class OpenWeatherMapProvider implements WeatherProvider {

    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final String apiKey;
    private boolean available = true;

    /**
     * Create provider with API key.
     * 
     * @param apiKey OpenWeatherMap API key
     */
    public OpenWeatherMapProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public WeatherData getCurrentWeather(double latitude, double longitude) throws WeatherException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new WeatherException("API key not configured");
        }

        try {
            String urlStr = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                    API_URL, latitude, longitude, apiKey);

            @SuppressWarnings("deprecation") // URL(String) works and URI.toURL() is more complex
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                available = false;
                throw new WeatherException("API returned status " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parseResponse(response.toString());

        } catch (WeatherException e) {
            throw e;
        } catch (Exception e) {
            available = false;
            throw new WeatherException("Failed to fetch weather: " + e.getMessage(), e);
        }
    }

    /**
     * Parse JSON response (simple regex-based to avoid external JSON dependency).
     */
    private WeatherData parseResponse(String json) throws WeatherException {
        try {
            float temp = extractFloat(json, "\"temp\":([-\\d.]+)");
            float humidity = extractFloat(json, "\"humidity\":([-\\d.]+)");
            float windSpeed = extractFloat(json, "\"speed\":([-\\d.]+)");
            float windDir = extractFloat(json, "\"deg\":([-\\d.]+)");
            float pressure = extractFloat(json, "\"pressure\":([-\\d.]+)");
            float clouds = extractFloat(json, "\"all\":([-\\d.]+)");

            // Rainfall is optional
            float rainfall = 0f;
            try {
                rainfall = extractFloat(json, "\"1h\":([-\\d.]+)");
            } catch (Exception ignored) {
            }

            String condition = extractString(json, "\"description\":\"([^\"]+)\"");
            long timestamp = System.currentTimeMillis() / 1000;

            available = true;
            return new WeatherData(temp, humidity, windSpeed, windDir,
                    rainfall, clouds, pressure, condition, timestamp);

        } catch (Exception e) {
            throw new WeatherException("Failed to parse weather response", e);
        }
    }

    private float extractFloat(String json, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Float.parseFloat(m.group(1));
        }
        return 0f;
    }

    private String extractString(String json, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "Unknown";
    }

    @Override
    public boolean isAvailable() {
        return available && apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getProviderName() {
        return "OpenWeatherMap";
    }
}
