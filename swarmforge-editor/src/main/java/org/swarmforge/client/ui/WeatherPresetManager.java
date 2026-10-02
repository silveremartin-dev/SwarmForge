/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Manages Weather and Climate presets loaded purely from JSON files in {@code presets/weather/} and classpath.
 * Persists user presets to {@code weather_presets.json} in the working directory.
 * No hardcoded weather data is stored in Java code.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherPresetManager {

    public static final File PRESETS_FILE = new File("weather_presets.json");

    private static final String[] BUILTIN_WEATHER_FILES = {
            "temperate.json",
            "tropical.json",
            "arid.json",
            "mediterranean.json",
            "arctic.json",
            "oceanic.json",
            "alpine.json",
            "taiga.json",
            "savanna.json",
            "steppe.json",
            "wetland.json"
    };

    private final Map<String, Map<String, Object>> presets = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherPresetManager() {
        loadAll();
    }

    private void loadAll() {
        presets.clear();

        // 1. Load built-ins from JSON files / resources
        for (String file : BUILTIN_WEATHER_FILES) {
            Map<String, Object> cfg = loadBuiltinWeather(file);
            if (cfg != null) {
                String name = cfg.containsKey("name") ? String.valueOf(cfg.get("name")) :
                        (cfg.containsKey("presetName") ? String.valueOf(cfg.get("presetName")) :
                        (cfg.containsKey("cityName") ? String.valueOf(cfg.get("cityName")) : file.replace(".json", "")));
                cfg.put("name", name);
                cfg.put("presetName", name);
                cfg.put("builtIn", true);
                presets.put(name, cfg);
            }
        }

        // 2. Load user presets from filesystem
        if (PRESETS_FILE.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> saved = mapper.readValue(PRESETS_FILE, Map.class);
                for (Map.Entry<String, Map<String, Object>> entry : saved.entrySet()) {
                    Map<String, Object> cfg = entry.getValue();
                    if (cfg != null) {
                        cfg.put("builtIn", false);
                        presets.put(entry.getKey(), cfg);
                    }
                }
            } catch (Exception ex) {
                System.err.println("[WeatherPresets] Could not read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadBuiltinWeather(String filename) {
        Map<String, Object> cfg = null;
        File[] candidatePaths = new File[]{
                new File("data/presets/weather", filename),
                new File("../data/presets/weather", filename),
                new File("presets/weather", filename),
                new File("../presets/weather", filename),
                new File("swarmforge-core/src/main/resources/presets/weather", filename),
                new File("../swarmforge-core/src/main/resources/presets/weather", filename)
        };

        for (File path : candidatePaths) {
            if (path.exists()) {
                try {
                    cfg = mapper.readValue(path, Map.class);
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (cfg == null) {
            try (InputStream in = getClass().getResourceAsStream("/presets/weather/" + filename)) {
                if (in != null) {
                    cfg = mapper.readValue(in, Map.class);
                }
            } catch (Exception ignored) {}
        }
        return cfg;
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

    public boolean isBuiltIn(String name) {
        Map<String, Object> cfg = presets.get(name);
        return cfg != null && Boolean.TRUE.equals(cfg.get("builtIn"));
    }

    public void save(String name, Map<String, Object> config) {
        if (name == null || name.isBlank() || config == null) return;
        Map<String, Object> copy = new LinkedHashMap<>(config);
        copy.put("name", name);

        // Protect built-in presets
        if (isBuiltIn(name)) {
            name = "[Fork] " + name;
            copy.put("builtIn", false);
        }

        copy.put("builtIn", false);
        copy.put("revisionTimestamp", System.currentTimeMillis());

        // Deterministic checksum
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(copy.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            copy.put("checksum", hex.toString().substring(0, 12));
        } catch (Exception ignored) {}

        if (presets.containsKey(name)) {
            Map<String, Object> existing = presets.get(name);
            int ver = existing.containsKey("version") && existing.get("version") instanceof Number ? ((Number) existing.get("version")).intValue() + 1 : 2;
            copy.put("version", ver);
        } else {
            copy.putIfAbsent("version", 1);
        }

        presets.put(name, copy);
        persist();
    }

    public boolean delete(String name) {
        if (presets.containsKey(name)) {
            if (isBuiltIn(name)) {
                return false; // Cannot delete reference built-in climate
            }
            presets.remove(name);
            persist();
            return true;
        }
        return false;
    }

    private void persist() {
        try {
            Map<String, Map<String, Object>> customOnly = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : presets.entrySet()) {
                if (!Boolean.TRUE.equals(entry.getValue().get("builtIn"))) {
                    customOnly.put(entry.getKey(), entry.getValue());
                }
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, customOnly);
        } catch (Exception ex) {
            System.err.println("[WeatherPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
