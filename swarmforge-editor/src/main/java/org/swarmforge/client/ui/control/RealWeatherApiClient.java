/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Real-world Meteorological & Geocoding API Client for SwarmForge Studio.
 * Interfaces asynchronously with Open-Meteo REST endpoints:
 * <ul>
 *   <li>Geocoding API: resolves city names into WGS84 latitude, longitude, and elevation.</li>
 *   <li>Elevation API: retrieves high-precision terrain elevation.</li>
 *   <li>Weather Forecast API: retrieves real-time temperature, humidity, wind, and pressure.</li>
 *   <li>Climate Archive API: aggregates historical annual climate curves (monthly splines).</li>
 * </ul>
 */
public class RealWeatherApiClient {

    private final ObjectMapper mapper = new ObjectMapper();

    public record LocationResult(String name, double latitude, double longitude, double altitude) {}

    public record WeatherData(
            String name,
            double latitude,
            double longitude,
            double altitude,
            double temperature,
            double humidity,
            double windSpeed,
            double precipitation,
            double pressure,
            Map<String, Object> configMap
    ) {}

    public record AnnualClimateData(
            double[] tempMax,
            double[] tempMin,
            double[] tempAvg,
            double[] rainAvg,
            double[] windAvg,
            double[] humidityAvg
    ) {}

    /**
     * Resolves a city name or query string into geographical coordinates via Open-Meteo Geocoding.
     */
    public void resolveLocation(String query, Consumer<LocationResult> onSuccess, Consumer<String> onError) {
        Thread t = new Thread(() -> {
            try {
                String geoUrlStr = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8)
                        + "&count=1&language=fr";
                String geoJson = fetchHttp(geoUrlStr);
                JsonNode geoRoot = mapper.readTree(geoJson);
                if (!geoRoot.has("results") || geoRoot.get("results").isEmpty()) {
                    onError.accept("Ville introuvable : \"" + query + "\"");
                    return;
                }
                JsonNode loc = geoRoot.get("results").get(0);
                String cityName = loc.get("name").asText();
                double lat = loc.get("latitude").asDouble();
                double lon = loc.get("longitude").asDouble();
                double alt = loc.has("elevation") && !loc.get("elevation").isNull() ? loc.get("elevation").asDouble() : 50.0;
                onSuccess.accept(new LocationResult(cityName, lat, lon, alt));
            } catch (Exception ex) {
                onError.accept("Erreur recherche : " + query + " (" + ex.getMessage() + ")");
            }
        }, "OpenMeteoGeoThread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Fetches elevation for given coordinates.
     */
    public double fetchElevation(double lat, double lon) {
        try {
            String elevUrlStr = String.format(Locale.US, "https://api.open-meteo.com/v1/elevation?latitude=%.4f&longitude=%.4f", lat, lon);
            String elevJson = fetchHttp(elevUrlStr);
            JsonNode elevRoot = mapper.readTree(elevJson);
            if (elevRoot.has("elevation") && elevRoot.get("elevation").isArray() && !elevRoot.get("elevation").isEmpty()) {
                return elevRoot.get("elevation").get(0).asDouble();
            }
        } catch (Exception ignored) {}
        return 100.0;
    }

    /**
     * Fetches current meteorological observations for specified coordinates.
     */
    public void fetchWeather(double lat, double lon, double alt, String name,
                             Consumer<WeatherData> onSuccess, Consumer<String> onError) {
        final String locationName = (name != null && !name.isBlank()) ? name : String.format(Locale.US, "%.2f°N, %.2f°E", lat, lon);
        Thread t = new Thread(() -> {
            try {
                String liveUrlStr = String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&elevation=%.1f&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,surface_pressure",
                        lat, lon, alt);
                String liveJson = fetchHttp(liveUrlStr);
                JsonNode liveRoot = mapper.readTree(liveJson);
                if (liveRoot.has("current")) {
                    JsonNode cur = liveRoot.get("current");
                    double temp = cur.has("temperature_2m") ? cur.get("temperature_2m").asDouble() : 18.0;
                    double hum = cur.has("relative_humidity_2m") ? cur.get("relative_humidity_2m").asDouble() : 60.0;
                    double wind = cur.has("wind_speed_10m") ? cur.get("wind_speed_10m").asDouble() : 15.0;
                    double rain = cur.has("precipitation") ? cur.get("precipitation").asDouble() : 0.0;
                    double press = cur.has("surface_pressure") ? cur.get("surface_pressure").asDouble() : 1013.25;

                    Map<String, Object> map = new HashMap<>();
                    map.put("name", locationName);
                    map.put("latitude", lat);
                    map.put("longitude", lon);
                    map.put("altitude", alt);
                    map.put("basePressure", press);

                    double[] tAvg = new double[12];
                    double[] tMin = new double[12];
                    double[] tMax = new double[12];
                    double[] wAvg = new double[12];
                    double[] rAvg = new double[12];
                    double[] hAvg = new double[12];
                    for (int m = 0; m < 12; m++) {
                        tAvg[m] = temp;
                        tMin[m] = temp - 4.0;
                        tMax[m] = temp + 4.0;
                        wAvg[m] = wind;
                        rAvg[m] = rain * 24.0;
                        hAvg[m] = hum;
                    }
                    map.put("tempAvg", tAvg);
                    map.put("tempMin", tMin);
                    map.put("tempMax", tMax);
                    map.put("windAvg", wAvg);
                    map.put("rainAvg", rAvg);
                    map.put("humidityAvg", hAvg);

                    WeatherData data = new WeatherData(locationName, lat, lon, alt, temp, hum, wind, rain, press, map);
                    onSuccess.accept(data);
                } else {
                    onError.accept("Réponse Open-Meteo invalide pour " + locationName);
                }
            } catch (Exception ex) {
                onError.accept("Hors-ligne / Erreur Open-Meteo (" + locationName + ")");
            }
        }, "OpenMeteoFetchCoordsThread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Fetches and aggregates historical annual climate data for monthly splines.
     */
    public AnnualClimateData fetchHistoricalClimate(double lat, double lon) throws Exception {
        String climateUrlStr = String.format(Locale.US,
                "https://archive-api.open-meteo.com/v1/archive?latitude=%.4f&longitude=%.4f&start_date=2023-01-01&end_date=2023-12-31&daily=temperature_2m_max,temperature_2m_min,temperature_2m_mean,precipitation_sum,wind_speed_10m_max",
                lat, lon);
        String climateJson = fetchHttp(climateUrlStr);
        JsonNode climateRoot = mapper.readTree(climateJson);

        double[] tempMax = new double[12];
        double[] tempMin = new double[12];
        double[] tempAvg = new double[12];
        double[] rainAvg = new double[12];
        double[] windAvg = new double[12];
        double[] humidityAvg = new double[12];

        if (climateRoot.has("daily")) {
            JsonNode dNode = climateRoot.get("daily");
            JsonNode timeN = dNode.get("time");
            JsonNode tMaxN = dNode.get("temperature_2m_max");
            JsonNode tMinN = dNode.get("temperature_2m_min");
            JsonNode tAvgN = dNode.get("temperature_2m_mean");
            JsonNode rainN = dNode.get("precipitation_sum");
            JsonNode windN = dNode.get("wind_speed_10m_max");

            double[] monthlyTMaxSum = new double[12];
            double[] monthlyTMinSum = new double[12];
            double[] monthlyTAvgSum = new double[12];
            double[] monthlyRainSum = new double[12];
            double[] monthlyWindSum = new double[12];
            int[] monthlyDaysCount = new int[12];

            if (timeN != null && timeN.isArray()) {
                for (int i = 0; i < timeN.size(); i++) {
                    String dateStr = timeN.get(i).asText();
                    if (dateStr.length() >= 7) {
                        int monthIndex = Integer.parseInt(dateStr.substring(5, 7)) - 1;
                        if (monthIndex >= 0 && monthIndex < 12) {
                            monthlyDaysCount[monthIndex]++;
                            if (tMaxN != null && i < tMaxN.size() && !tMaxN.get(i).isNull()) monthlyTMaxSum[monthIndex] += tMaxN.get(i).asDouble();
                            if (tMinN != null && i < tMinN.size() && !tMinN.get(i).isNull()) monthlyTMinSum[monthIndex] += tMinN.get(i).asDouble();
                            if (tAvgN != null && i < tAvgN.size() && !tAvgN.get(i).isNull()) monthlyTAvgSum[monthIndex] += tAvgN.get(i).asDouble();
                            if (rainN != null && i < rainN.size() && !rainN.get(i).isNull()) monthlyRainSum[monthIndex] += rainN.get(i).asDouble();
                            if (windN != null && i < windN.size() && !windN.get(i).isNull()) monthlyWindSum[monthIndex] += windN.get(i).asDouble();
                        }
                    }
                }
            }

            for (int m = 0; m < 12; m++) {
                int count = Math.max(1, monthlyDaysCount[m]);
                tempMax[m] = monthlyTMaxSum[m] / count;
                tempMin[m] = monthlyTMinSum[m] / count;
                tempAvg[m] = monthlyTAvgSum[m] / count;

                rainAvg[m] = monthlyRainSum[m];

                windAvg[m] = monthlyWindSum[m] / count;

                double approxHum = Math.min(95, Math.max(25, 60.0 + (rainAvg[m] > 50 ? 15 : -10) - (tempAvg[m] > 25 ? 15 : 0)));
                humidityAvg[m] = approxHum;
            }
        }
        return new AnnualClimateData(tempMax, tempMin, tempAvg, rainAvg, windAvg, humidityAvg);
    }

    /**
     * Low-level synchronous HTTP GET retrieval helper.
     */
    public String fetchHttp(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);
        conn.setRequestProperty("User-Agent", "SwarmForge-Simulation/1.0");
        int status = conn.getResponseCode();
        InputStream stream = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) throw new java.io.IOException("HTTP stream empty (" + status + ")");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
