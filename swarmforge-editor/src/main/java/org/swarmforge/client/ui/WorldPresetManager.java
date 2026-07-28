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
import java.util.*;

/**
 * Manages preset configurations for the World Editor Pane.
 * Handles built-in world configurations and persistence to/from world_presets.json.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WorldPresetManager {

    private static final File PRESETS_FILE = new File("world_presets.json");

    private final Map<String, Map<String, Object>> presets = new LinkedHashMap<>();

    public WorldPresetManager() {
        presets.putAll(builtins());
        loadFromFileSystem();
    }

    private Map<String, Map<String, Object>> builtins() {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();

        map.put("Tempéré Standard (Temperate Forest)", makeConfig(
                256, 256, 64, 1.0, 48.8566, 2.3522, "FOREST", 0.65, 0.55, 0.45
        ));
        map.put("Désert Aride (Arid Desert)", makeConfig(
                384, 384, 48, 1.0, 24.7136, 46.5163, "DESERT", 0.85, 0.15, 0.70
        ));
        map.put("Forêt Tropicale (Tropical Rainforest)", makeConfig(
                256, 256, 80, 0.8, -3.1190, -60.0217, "TROPICAL", 0.40, 0.90, 0.35
        ));
        map.put("Montagne Rocheuse (Alpine Mountain)", makeConfig(
                512, 512, 128, 1.2, 45.8326, 6.8652, "ALPINE", 0.90, 0.40, 0.85
        ));
        map.put("Permafrost Arctique (Arctic Tundra)", makeConfig(
                256, 256, 32, 1.0, 78.2232, 15.6469, "ARCTIC", 0.30, 0.30, 0.90
        ));

        return map;
    }

    private Map<String, Object> makeConfig(int width, int height, int depth, double resolution,
                                           double lat, double lon, String biome,
                                           double elevationScale, double moistureScale, double compaction) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("width", width);
        cfg.put("height", height);
        cfg.put("depth", depth);
        cfg.put("resolution", resolution);
        cfg.put("latitude", lat);
        cfg.put("longitude", lon);
        cfg.put("biome", biome);
        cfg.put("elevationScale", elevationScale);
        cfg.put("moistureScale", moistureScale);
        cfg.put("soilCompaction", compaction);
        return cfg;
    }

    private void loadFromFileSystem() {
        if (!PRESETS_FILE.exists()) return;
        try {
            ObjectMapper m = new ObjectMapper();
            Map<String, Map<String, Object>> userPresets = m.readValue(PRESETS_FILE, new TypeReference<>() {});
            if (userPresets != null) {
                presets.putAll(userPresets);
            }
        } catch (Exception ex) {
            System.err.println("[WorldPresets] Could not read " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }

    public Map<String, Map<String, Object>> getAll() {
        return Collections.unmodifiableMap(presets);
    }

    public Set<String> names() {
        return presets.keySet();
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
            System.err.println("[WorldPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
