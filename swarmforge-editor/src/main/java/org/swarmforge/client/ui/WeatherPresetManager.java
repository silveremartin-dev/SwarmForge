/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

/**
 * Manages Weather and Climate presets: built-in climate profiles (Temperate, Tropical, Arid,
 * Mediterranean, Arctic, Oceanic) + user-saved configurations.
 * Persists user presets to {@code weather_presets.json} in the working directory.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherPresetManager {

    public static final File PRESETS_FILE = new File("weather_presets.json");

    private final Map<String, Map<String, Object>> presets = new LinkedHashMap<>();

    public WeatherPresetManager() {
        loadAll();
    }

    private void loadAll() {
        presets.clear();
        presets.putAll(builtins());
        if (PRESETS_FILE.exists()) {
            try {
                ObjectMapper m = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> saved = m.readValue(PRESETS_FILE, Map.class);
                presets.putAll(saved);
            } catch (Exception ex) {
                System.err.println("[WeatherPresets] Could not read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }
    }

    private Map<String, Map<String, Object>> builtins() {
        Map<String, Map<String, Object>> m = new LinkedHashMap<>();

        // 1. Temperate (Paris)
        m.put("Temperate", makePreset("Temperate", "Paris", 48.8, 2.35, 100.0, 1013.25, "SW", 3.0, 0.85,
            new double[]{ -1,  0,  4,  9, 14, 17, 19, 18, 14,  9,  4,  0}, // Temp Min
            new double[]{  3,  5,  9, 14, 19, 22, 25, 24, 20, 14,  8,  4}, // Temp Avg
            new double[]{  7,  9, 14, 19, 24, 27, 30, 29, 25, 19, 12,  8}, // Temp Max
            new double[]{ 10, 12, 14, 12, 10,  8,  7,  7,  8, 10, 12, 12}, // Wind Min
            new double[]{ 20, 22, 25, 22, 18, 16, 15, 15, 17, 20, 22, 22}, // Wind Avg
            new double[]{ 45, 50, 55, 45, 40, 35, 35, 35, 40, 45, 50, 50}, // Wind Max
            new double[]{ 30, 25, 30, 35, 40, 35, 30, 35, 40, 45, 40, 35}, // Rain Min
            new double[]{ 55, 45, 50, 55, 65, 55, 50, 55, 65, 75, 65, 60}, // Rain Avg
            new double[]{ 90, 80, 85, 90, 110, 95, 85, 95, 110, 125, 110, 100}, // Rain Max
            new double[]{ 60, 55, 50, 45, 45, 45, 45, 50, 55, 60, 65, 65}, // Hum Min
            new double[]{ 78, 74, 68, 62, 63, 64, 65, 68, 73, 79, 82, 81}, // Hum Avg
            new double[]{ 90, 88, 85, 80, 80, 82, 83, 85, 88, 92, 94, 92}  // Hum Max
        ));

        // 2. Tropical (Manaus)
        m.put("Tropical", makePreset("Tropical", "Manaus", -3.1, -60.0, 50.0, 1011.0, "E", 2.0, 0.90,
            new double[]{ 22, 22, 23, 24, 24, 23, 23, 23, 23, 23, 23, 22}, // Temp Min
            new double[]{ 26, 27, 28, 29, 28, 27, 27, 27, 28, 28, 27, 26}, // Temp Avg
            new double[]{ 31, 32, 33, 34, 33, 31, 31, 31, 32, 32, 31, 31}, // Temp Max
            new double[]{  5,  5,  6,  8, 10, 12, 14, 12, 10,  8,  6,  5}, // Wind Min
            new double[]{ 12, 14, 15, 18, 22, 25, 28, 26, 22, 18, 15, 12}, // Wind Avg
            new double[]{ 30, 32, 35, 45, 55, 65, 70, 65, 55, 45, 35, 30}, // Wind Max
            new double[]{ 80, 70, 90, 140, 200, 250, 220, 180, 160, 140, 110, 90}, // Rain Min
            new double[]{140, 120, 160, 230, 310, 380, 340, 290, 250, 220, 180, 150}, // Rain Avg
            new double[]{220, 190, 250, 350, 450, 500, 480, 420, 370, 330, 270, 230}, // Rain Max
            new double[]{ 65, 62, 65, 70, 75, 80, 78, 75, 72, 70, 68, 66}, // Hum Min
            new double[]{ 78, 76, 78, 82, 86, 90, 88, 86, 84, 82, 80, 79}, // Hum Avg
            new double[]{ 92, 90, 92, 95, 98, 99, 98, 97, 95, 94, 93, 92}  // Hum Max
        ));

        // 3. Arid (Sahara / Tamanrasset)
        m.put("Arid", makePreset("Arid", "Tamanrasset", 22.8, 5.5, 1300.0, 1015.0, "NE", 5.0, 0.70,
            new double[]{  5,  7, 11, 15, 20, 24, 27, 26, 22, 16, 10,  6}, // Temp Min
            new double[]{ 12, 15, 20, 25, 31, 35, 38, 37, 33, 26, 18, 13}, // Temp Avg
            new double[]{ 20, 23, 28, 34, 40, 45, 47, 46, 42, 35, 26, 20}, // Temp Max
            new double[]{  8,  8, 10, 12, 15, 18, 16, 14, 12, 10,  8,  8}, // Wind Min
            new double[]{ 18, 20, 24, 28, 32, 36, 34, 30, 26, 22, 18, 16}, // Wind Avg
            new double[]{ 40, 45, 50, 60, 70, 75, 70, 65, 55, 48, 42, 40}, // Wind Max
            new double[]{  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // Rain Min
            new double[]{  8,  6,  5,  3,  1,  0,  2,  3,  1,  4,  6,  8}, // Rain Avg
            new double[]{ 20, 18, 15, 10,  5,  2,  8, 10,  5, 12, 16, 20}, // Rain Max
            new double[]{ 15, 12, 10,  8,  6,  5,  5,  6,  8, 10, 12, 14}, // Hum Min
            new double[]{ 32, 28, 22, 18, 15, 14, 16, 18, 20, 24, 28, 31}, // Hum Avg
            new double[]{ 55, 50, 42, 35, 28, 25, 28, 30, 35, 42, 48, 52}  // Hum Max
        ));

        // 4. Mediterranean (Marseille)
        m.put("Mediterranean", makePreset("Mediterranean", "Marseille", 43.3, 5.4, 50.0, 1016.0, "NW", 3.0, 0.80,
            new double[]{  4,  5,  7, 10, 14, 18, 21, 21, 18, 14,  9,  5}, // Temp Min
            new double[]{  9, 10, 13, 16, 20, 24, 27, 27, 24, 19, 14, 10}, // Temp Avg
            new double[]{ 14, 15, 18, 22, 26, 30, 33, 33, 29, 24, 18, 14}, // Temp Max
            new double[]{  8,  9, 11, 10,  8,  7,  7,  7,  8,  9, 10,  9}, // Wind Min
            new double[]{ 18, 20, 23, 20, 17, 15, 14, 14, 16, 19, 21, 19}, // Wind Avg
            new double[]{ 42, 45, 50, 45, 38, 32, 30, 30, 35, 42, 46, 44}, // Wind Max
            new double[]{ 40, 35, 30, 20, 10,  2,  1,  3, 15, 35, 45, 45}, // Rain Min
            new double[]{ 80, 70, 60, 45, 25,  8,  3,  8, 40, 75, 95, 90}, // Rain Avg
            new double[]{140, 120, 110, 80, 50, 20, 10, 20, 80, 130, 160, 150}, // Rain Max
            new double[]{ 52, 50, 46, 42, 38, 34, 32, 34, 38, 45, 50, 52}, // Hum Min
            new double[]{ 72, 70, 66, 62, 58, 52, 48, 50, 56, 66, 72, 73}, // Hum Avg
            new double[]{ 88, 86, 82, 78, 74, 68, 64, 66, 72, 82, 88, 89}  // Hum Max
        ));

        // 5. Arctic (Longyearbyen / Svalbard)
        m.put("Arctic", makePreset("Arctic", "Longyearbyen", 78.2, 15.6, 20.0, 1008.0, "NE", 7.0, 0.95,
            new double[]{-32,-33,-28,-19, -8, -1,  2,  1, -4,-13,-23,-29}, // Temp Min
            new double[]{-26,-27,-22,-13, -3,  3,  7,  5,  0, -8,-17,-23}, // Temp Avg
            new double[]{-20,-21,-16, -7,  2,  8, 12, 10,  4, -3,-11,-17}, // Temp Max
            new double[]{ 12, 14, 15, 12, 10,  8,  7,  8, 10, 13, 15, 14}, // Wind Min
            new double[]{ 24, 27, 28, 24, 20, 16, 15, 16, 20, 25, 28, 26}, // Wind Avg
            new double[]{ 55, 60, 65, 55, 45, 38, 35, 38, 46, 58, 64, 60}, // Wind Max
            new double[]{  5,  5,  5,  8, 10, 15, 20, 25, 20, 15, 10,  6}, // Rain Min
            new double[]{ 15, 14, 15, 18, 22, 30, 42, 48, 40, 30, 22, 16}, // Rain Avg
            new double[]{ 30, 28, 30, 35, 42, 55, 70, 80, 70, 55, 40, 32}, // Rain Max
            new double[]{ 68, 66, 65, 68, 72, 70, 68, 70, 74, 76, 74, 70}, // Hum Min
            new double[]{ 79, 78, 77, 80, 83, 81, 79, 81, 85, 86, 84, 80}, // Hum Avg
            new double[]{ 90, 89, 88, 91, 94, 92, 90, 92, 95, 96, 94, 90}  // Hum Max
        ));

        // 6. Savanna (Serengeti, Tanzania) - Tropical Savanna / Alternating Wet & Dry
        m.put("Savanna", makePreset("Savanna", "Serengeti", -2.33, 34.83, 1500.0, 1012.0, "E", 4.0, 0.85,
            new double[]{ 15, 16, 16, 16, 15, 13, 13, 14, 15, 16, 16, 15}, // Temp Min
            new double[]{ 22, 23, 23, 22, 21, 20, 20, 21, 22, 23, 22, 22}, // Temp Avg
            new double[]{ 28, 29, 28, 27, 26, 26, 26, 27, 29, 30, 28, 28}, // Temp Max
            new double[]{  8,  8,  9,  8,  7,  8,  9, 10, 10,  9,  8,  8}, // Wind Min
            new double[]{ 16, 16, 17, 15, 14, 16, 18, 20, 19, 17, 16, 16}, // Wind Avg
            new double[]{ 35, 35, 38, 35, 30, 35, 40, 42, 40, 38, 35, 35}, // Wind Max
            new double[]{ 50, 45, 90, 120, 70, 10,  5,  5, 10, 30, 60, 60}, // Rain Min
            new double[]{ 85, 80, 140, 190, 110, 25, 12, 15, 25, 65, 110, 100}, // Rain Avg
            new double[]{140, 130, 220, 280, 180, 50, 25, 30, 50, 110, 180, 160}, // Rain Max
            new double[]{ 45, 42, 50, 58, 52, 38, 32, 32, 35, 40, 48, 48}, // Hum Min
            new double[]{ 62, 60, 68, 76, 70, 55, 48, 48, 52, 58, 66, 65}, // Hum Avg
            new double[]{ 80, 78, 85, 90, 85, 72, 65, 65, 70, 76, 82, 82}  // Hum Max
        ));

        // 7. Alpine / Mountain (Mont Blanc / Valais)
        m.put("Alpine", makePreset("Alpine", "Valais", 45.83, 6.86, 2500.0, 750.0, "W", 5.0, 0.90,
            new double[]{-14,-13,-10, -6, -1,  3,  6,  6,  2, -2, -8,-12}, // Temp Min
            new double[]{ -9, -8, -5, -1,  4,  8, 11, 11,  7,  2, -4, -8}, // Temp Avg
            new double[]{ -4, -3,  0,  4,  9, 13, 16, 16, 12,  7,  0, -4}, // Temp Max
            new double[]{ 12, 14, 15, 14, 12, 10,  9,  9, 11, 13, 14, 13}, // Wind Min
            new double[]{ 25, 28, 30, 26, 22, 18, 16, 17, 22, 26, 28, 27}, // Wind Avg
            new double[]{ 65, 70, 75, 65, 55, 45, 40, 45, 55, 65, 70, 68}, // Wind Max
            new double[]{ 60, 55, 60, 70, 80, 75, 70, 75, 65, 60, 70, 65}, // Rain Min
            new double[]{100, 95, 110, 125, 140, 130, 120, 130, 110, 100, 115, 110}, // Rain Avg
            new double[]{160, 150, 175, 190, 210, 200, 185, 200, 170, 160, 180, 170}, // Rain Max
            new double[]{ 55, 52, 50, 48, 50, 52, 48, 50, 52, 54, 56, 56}, // Hum Min
            new double[]{ 72, 70, 68, 66, 68, 70, 66, 68, 70, 72, 74, 74}, // Hum Avg
            new double[]{ 88, 86, 85, 84, 86, 88, 84, 86, 88, 89, 90, 90}  // Hum Max
        ));

        // 8. Boreal Taiga (Rovaniemi, Finland)
        m.put("Taiga", makePreset("Taiga", "Rovaniemi", 66.50, 25.73, 140.0, 1005.0, "SW", 6.0, 0.88,
            new double[]{-17,-17,-12, -5,  2,  8, 11,  9,  4, -2, -9,-14}, // Temp Min
            new double[]{-12,-12, -7,  0,  7, 13, 16, 13,  8,  1, -5, -9}, // Temp Avg
            new double[]{ -7, -6, -1,  5, 12, 18, 21, 18, 12,  4, -2, -5}, // Temp Max
            new double[]{  8,  9, 10,  9,  8,  8,  7,  7,  8,  9, 10,  9}, // Wind Min
            new double[]{ 16, 17, 18, 16, 15, 14, 13, 13, 15, 17, 18, 17}, // Wind Avg
            new double[]{ 38, 40, 42, 38, 34, 32, 30, 30, 35, 40, 42, 40}, // Wind Max
            new double[]{ 20, 18, 20, 22, 28, 38, 45, 42, 35, 30, 26, 22}, // Rain Min
            new double[]{ 38, 32, 35, 38, 48, 65, 78, 72, 58, 50, 45, 40}, // Rain Avg
            new double[]{ 60, 52, 55, 60, 75, 100, 120, 110, 90, 80, 70, 65}, // Rain Max
            new double[]{ 72, 70, 62, 52, 46, 48, 52, 56, 64, 74, 80, 78}, // Hum Min
            new double[]{ 84, 82, 75, 66, 60, 62, 66, 72, 78, 86, 90, 88}, // Hum Avg
            new double[]{ 94, 92, 88, 80, 75, 78, 82, 86, 90, 95, 97, 96}  // Hum Max
        ));

        // 9. Semi-Arid Steppe (Astana, Kazakhstan)
        m.put("Steppe", makePreset("Steppe", "Astana", 51.17, 71.45, 350.0, 1018.0, "N", 4.5, 0.80,
            new double[]{-20,-20,-13, -1,  7, 12, 15, 13,  6, -1,-10,-17}, // Temp Min
            new double[]{-15,-14, -7,  6, 15, 20, 22, 20, 13,  5, -5,-12}, // Temp Avg
            new double[]{-10, -8, -1, 13, 22, 27, 29, 27, 20, 11,  0, -7}, // Temp Max
            new double[]{ 12, 13, 14, 14, 13, 12, 11, 11, 12, 13, 14, 13}, // Wind Min
            new double[]{ 22, 24, 25, 24, 22, 20, 18, 18, 20, 23, 25, 24}, // Wind Avg
            new double[]{ 50, 55, 58, 52, 48, 45, 40, 40, 45, 50, 55, 52}, // Wind Max
            new double[]{  8,  7,  8, 10, 15, 18, 22, 16, 10, 12, 10,  9}, // Rain Min
            new double[]{ 16, 15, 18, 22, 32, 38, 48, 34, 24, 26, 22, 19}, // Rain Avg
            new double[]{ 30, 28, 32, 40, 55, 65, 80, 60, 45, 48, 40, 35}, // Rain Max
            new double[]{ 68, 66, 64, 45, 36, 38, 40, 38, 42, 54, 68, 70}, // Hum Min
            new double[]{ 80, 78, 76, 58, 48, 50, 54, 50, 55, 68, 80, 82}, // Hum Avg
            new double[]{ 90, 88, 86, 72, 62, 64, 68, 64, 70, 82, 90, 92}  // Hum Max
        ));

        // 10. Oceanic (Brittany, Brest, France)
        m.put("Oceanic", makePreset("Oceanic", "Brest", 48.39, -4.48, 50.0, 1014.0, "W", 2.5, 0.82,
            new double[]{  4,  4,  5,  6,  9, 11, 13, 13, 11,  9,  6,  4}, // Temp Min
            new double[]{  7,  7,  9, 10, 13, 16, 18, 18, 16, 13, 10,  7}, // Temp Avg
            new double[]{ 10, 10, 12, 14, 17, 20, 22, 22, 20, 16, 13, 10}, // Temp Max
            new double[]{ 15, 16, 16, 14, 12, 10,  9,  9, 11, 14, 16, 16}, // Wind Min
            new double[]{ 26, 28, 27, 24, 20, 18, 16, 16, 19, 24, 27, 28}, // Wind Avg
            new double[]{ 60, 65, 62, 55, 45, 40, 35, 35, 45, 58, 65, 68}, // Wind Max
            new double[]{ 70, 60, 50, 40, 40, 30, 25, 30, 45, 75, 85, 80}, // Rain Min
            new double[]{120, 100, 85, 70, 65, 52, 45, 55, 78, 125, 140, 135}, // Rain Avg
            new double[]{180, 160, 135, 115, 105, 85, 75, 90, 125, 190, 210, 200}, // Rain Max
            new double[]{ 74, 70, 66, 62, 62, 62, 64, 64, 66, 72, 76, 76}, // Hum Min
            new double[]{ 85, 83, 80, 76, 76, 76, 78, 78, 80, 84, 87, 87}, // Hum Avg
            new double[]{ 94, 93, 91, 88, 88, 88, 90, 90, 92, 95, 96, 96}  // Hum Max
        ));

        // 11. Subtropical Wetland (Everglades, Florida, USA)
        m.put("Wetland", makePreset("Wetland", "Everglades", 25.28, -80.90, 5.0, 1015.0, "SE", 1.5, 0.92,
            new double[]{ 15, 16, 18, 20, 22, 24, 25, 25, 24, 22, 19, 16}, // Temp Min
            new double[]{ 20, 21, 23, 25, 27, 29, 30, 30, 29, 27, 24, 21}, // Temp Avg
            new double[]{ 26, 27, 29, 31, 32, 33, 34, 34, 33, 31, 29, 27}, // Temp Max
            new double[]{  6,  7,  8,  8,  7,  6,  5,  5,  6,  7,  7,  6}, // Wind Min
            new double[]{ 14, 15, 16, 17, 15, 13, 12, 12, 14, 15, 15, 14}, // Wind Avg
            new double[]{ 32, 35, 38, 40, 36, 32, 30, 30, 38, 40, 36, 34}, // Wind Max
            new double[]{ 20, 25, 30, 40, 90, 140, 130, 140, 150, 80, 35, 25}, // Rain Min
            new double[]{ 45, 52, 65, 80, 165, 240, 210, 230, 250, 145, 70, 50}, // Rain Avg
            new double[]{ 80, 95, 110, 140, 260, 360, 320, 350, 380, 230, 120, 90}, // Rain Max
            new double[]{ 58, 55, 54, 52, 56, 64, 65, 66, 68, 64, 60, 59}, // Hum Min
            new double[]{ 72, 70, 68, 66, 72, 80, 81, 82, 83, 78, 74, 73}, // Hum Avg
            new double[]{ 88, 86, 84, 82, 88, 94, 95, 95, 96, 92, 89, 88}  // Hum Max
        ));

        return m;
    }

    private Map<String, Object> makePreset(String name, String cityName,
            double lat, double lon, double alt, double press, String windDir, double soilInertia, double depthAtten,
            double[] tMin, double[] tAvg, double[] tMax,
            double[] wMin, double[] wAvg, double[] wMax,
            double[] rMin, double[] rAvg, double[] rMax,
            double[] hMin, double[] hAvg, double[] hMax) {

        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("presetName", name);
        cfg.put("cityName", cityName);

        cfg.put("latitude", lat);
        cfg.put("longitude", lon);
        cfg.put("altitude", alt);
        cfg.put("basePressure", press);
        cfg.put("windDirection", windDir);
        cfg.put("soilInertiaDays", soilInertia);
        cfg.put("depthAttenuation", depthAtten);

        cfg.put("tempMin", toList(tMin));
        cfg.put("tempAvg", toList(tAvg));
        cfg.put("tempMax", toList(tMax));

        cfg.put("windMin", toList(wMin));
        cfg.put("windAvg", toList(wAvg));
        cfg.put("windMax", toList(wMax));

        cfg.put("rainMin", toList(rMin));
        cfg.put("rainAvg", toList(rAvg));
        cfg.put("rainMax", toList(rMax));

        cfg.put("humidityMin", toList(hMin));
        cfg.put("humidityAvg", toList(hAvg));
        cfg.put("humidityMax", toList(hMax));

        // Default seasonal & disaster baseline values
        cfg.put("temperatureMin", minVal(tMin));
        cfg.put("temperatureMax", maxVal(tMax));
        cfg.put("humidity", avgVal(hAvg));
        cfg.put("rainFrequency", Math.round(sumVal(rAvg) / 3.0));

        Map<String, Double> events = new LinkedHashMap<>();
        events.put("☀️ Sunny", 60.0);
        events.put("🌧 Rain", 25.0);
        events.put("⛈ Storm", 5.0);
        events.put("🌫 Fog", 8.0);
        events.put("❄️ Snow", 2.0);
        events.put("💨 Wind", 15.0);
        cfg.put("events", events);

        Map<String, Double> disasters = new LinkedHashMap<>();
        disasters.put("🔥 Fire & Lightning", 1.0);
        disasters.put("🌊 Flash Flood", 2.0);
        disasters.put("🏜️ Sandstorm / Dust Storm", 1.5);
        disasters.put("⚡ Lightning Strikes", 3.0);
        disasters.put("🌪 Tornado", 0.5);
        disasters.put("🏜 Drought", 3.0);
        disasters.put("❄️ Hard Freeze", 2.0);
        cfg.put("disasters", disasters);

        return cfg;
    }

    private List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>(arr.length);
        for (double d : arr) list.add(d);
        return list;
    }

    private double minVal(double[] arr) {
        double min = Double.MAX_VALUE;
        for (double d : arr) if (d < min) min = d;
        return min;
    }

    private double maxVal(double[] arr) {
        double max = -Double.MAX_VALUE;
        for (double d : arr) if (d > max) max = d;
        return max;
    }

    private double avgVal(double[] arr) {
        double sum = 0;
        for (double d : arr) sum += d;
        return sum / arr.length;
    }

    private double sumVal(double[] arr) {
        double sum = 0;
        for (double d : arr) sum += d;
        return sum;
    }

    public Map<String, Map<String, Object>> getAll() {
        return Collections.unmodifiableMap(presets);
    }

    public Set<String> names() {
        return new TreeSet<>(presets.keySet());
    }

    public Map<String, Object> get(String name) {
        return presets.get(name);
    }

    public boolean contains(String name) {
        return presets.containsKey(name);
    }

    public void save(String name, Map<String, Object> config) {
        presets.put(name, new LinkedHashMap<>(config));
        persist();
    }

    public boolean delete(String name) {
        if (presets.containsKey(name)) {
            presets.remove(name);
            persist();
            return true;
        }
        return false;
    }

    private void persist() {
        try {
            ObjectMapper m = new ObjectMapper();
            m.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, presets);
        } catch (Exception ex) {
            System.err.println("[WeatherPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
