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
        m.put("Temperate", makePreset("Temperate", 48.8, 2.35, 100.0, 1013.25, "SW", 3.0, 0.85,
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
        m.put("Tropical", makePreset("Tropical", -3.1, -60.0, 50.0, 1011.0, "E", 2.0, 0.90,
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

        // 3. Arid (Sahara)
        m.put("Arid", makePreset("Arid", 22.8, 5.5, 1300.0, 1015.0, "NE", 5.0, 0.70,
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
        m.put("Mediterranean", makePreset("Mediterranean", 43.3, 5.4, 50.0, 1016.0, "NW", 3.0, 0.80,
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

        // 5. Arctic (Svalbard)
        m.put("Arctic", makePreset("Arctic", 78.2, 15.6, 20.0, 1008.0, "NE", 7.0, 0.95,
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

        return m;
    }

    private Map<String, Object> makePreset(String name,
            double lat, double lon, double alt, double press, String windDir, double soilInertia, double depthAtten,
            double[] tMin, double[] tAvg, double[] tMax,
            double[] wMin, double[] wAvg, double[] wMax,
            double[] rMin, double[] rAvg, double[] rMax,
            double[] hMin, double[] hAvg, double[] hMax) {

        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("presetName", name);

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
