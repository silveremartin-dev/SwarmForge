/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles real-time Open-Meteo weather synchronization, GPS geocoding, and biotope weather queries.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class SimulationScenarioWeatherSync {

    private SimulationScenarioWeatherSync() {}

    static void fetchRealWeatherFromWorld(SimulationControlPanel panel, TextField targetField) {
        double lat = 48.4047;
        double lon = 2.7016;
        double alt = 35.0;
        String name = "Monde Actuel";

        if (panel.worldConfigSupplier != null) {
            Map<String, Object> cfg = panel.worldConfigSupplier.get();
            if (cfg != null) {
                if (cfg.containsKey("cityName") && cfg.get("cityName") != null) {
                    name = String.valueOf(cfg.get("cityName"));
                }
                if (cfg.containsKey("latitude") && cfg.get("latitude") != null) {
                    try { lat = Double.parseDouble(String.valueOf(cfg.get("latitude"))); } catch (NumberFormatException ignored) {}
                }
                if (cfg.containsKey("longitude") && cfg.get("longitude") != null) {
                    try { lon = Double.parseDouble(String.valueOf(cfg.get("longitude"))); } catch (NumberFormatException ignored) {}
                }
                if (cfg.containsKey("elevation") && cfg.get("elevation") != null) {
                    try { alt = Double.parseDouble(String.valueOf(cfg.get("elevation"))); } catch (NumberFormatException ignored) {}
                }
            }
        } else {
            String worldPreset = panel.comboWorld != null ? panel.comboWorld.getValue() : null;
            if (worldPreset != null) {
                name = worldPreset;
                WorldPresetManager wpm = new WorldPresetManager();
                Map<String, Object> pCfg = wpm.getPreset(worldPreset);
                if (pCfg != null) {
                    if (pCfg.containsKey("cityName") && pCfg.get("cityName") != null) name = String.valueOf(pCfg.get("cityName"));
                    if (pCfg.containsKey("latitude") && pCfg.get("latitude") != null) {
                        try { lat = Double.parseDouble(String.valueOf(pCfg.get("latitude"))); } catch (NumberFormatException ignored) {}
                    }
                    if (pCfg.containsKey("longitude") && pCfg.get("longitude") != null) {
                        try { lon = Double.parseDouble(String.valueOf(pCfg.get("longitude"))); } catch (NumberFormatException ignored) {}
                    }
                    if (pCfg.containsKey("elevation") && pCfg.get("elevation") != null) {
                        try { alt = Double.parseDouble(String.valueOf(pCfg.get("elevation"))); } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        if (targetField != null) {
            targetField.setText(name != null && !name.isBlank() ? name : String.format(Locale.US, "%.4f, %.4f", lat, lon));
        }

        fetchRealWeatherByCoordinates(panel, lat, lon, alt, name);
    }

    static void fetchRealWeather(SimulationControlPanel panel, String cityQuery) {
        if (cityQuery == null || cityQuery.trim().isEmpty()) {
            fetchRealWeatherFromWorld(panel, null);
            return;
        }
        final String query = cityQuery.trim();

        Pattern coordPattern = Pattern.compile("^([+-]?\\d+(?:\\.\\d+)?)[,\\s/]+([+-]?\\d+(?:\\.\\d+)?)(?:[,\\s/]+([+-]?\\d+(?:\\.\\d+)?)(?:\\s*m)?)?$");
        Matcher m = coordPattern.matcher(query);
        if (m.matches()) {
            try {
                double lat = Double.parseDouble(m.group(1));
                double lon = Double.parseDouble(m.group(2));
                double alt = (m.group(3) != null && !m.group(3).isEmpty()) ? Double.parseDouble(m.group(3)) : 50.0;
                fetchRealWeatherByCoordinates(panel, lat, lon, alt, "GPS " + String.format(Locale.US, "%.2f, %.2f", lat, lon));
                return;
            } catch (Exception ignored) {}
        }

        panel.lblRealWeatherStatus.setText("⏳ Recherche Open-Meteo pour \"" + query + "\"...");
        panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        panel.weatherApiClient.resolveLocation(
            query,
            loc -> fetchRealWeatherByCoordinates(panel, loc.latitude(), loc.longitude(), loc.altitude(), loc.name()),
            errMsg -> Platform.runLater(() -> {
                panel.lblRealWeatherStatus.setText("⚠️ " + errMsg);
                panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 10px;");
            })
        );
    }

    static void fetchRealWeatherByCoordinates(SimulationControlPanel panel, double lat, double lon, double alt, String name) {
        final String locationName = (name != null && !name.isBlank()) ? name : String.format(Locale.US, "%.2f°N, %.2f°E", lat, lon);
        panel.lblRealWeatherStatus.setText("⏳ Chargement Open-Meteo pour " + locationName + "...");
        panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        panel.weatherApiClient.fetchWeather(
            lat, lon, alt, name,
            data -> {
                panel.cachedRealWeatherData = data.configMap();
                Platform.runLater(() -> {
                    panel.lblRealWeatherStatus.setText(String.format(Locale.US, "🟢 %s (%.2f°N, %.2f°E, %.0fm) : %.1f°C, Vent %.0f km/h, Hum. %.0f%%",
                        locationName, lat, lon, alt, data.temperature(), data.windSpeed(), data.humidity()));
                    panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 10px;");
                });
            },
            errMsg -> Platform.runLater(() -> {
                panel.lblRealWeatherStatus.setText("⚠️ " + errMsg);
                panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 10px;");
            })
        );
    }
}
