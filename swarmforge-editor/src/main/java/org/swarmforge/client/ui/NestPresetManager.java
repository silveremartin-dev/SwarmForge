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
 * Manages NestGenerator presets: built-in defaults + user-saved configurations.
 * Persists user presets to {@code nest_presets.json} in the working directory.
 */
public class NestPresetManager {

    public static final File PRESETS_FILE = new File("nest_presets.json");

    /** All presets (built-in first, then user-saved). */
    private final Map<String, Map<String, Object>> presets = new LinkedHashMap<>();

    public NestPresetManager() {
        loadAll();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadAll() {
        presets.clear();
        presets.putAll(builtins());
        if (PRESETS_FILE.exists()) {
            try {
                ObjectMapper m = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> saved =
                        m.readValue(PRESETS_FILE, Map.class);
                presets.putAll(saved);
            } catch (Exception ex) {
                System.err.println("[NestPresets] Could not read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }
    }

    // ── Built-ins ─────────────────────────────────────────────────────────────

    private Map<String, Map<String, Object>> builtins() {
        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        m.put("Young Colony",            make("Young Colony",          10, 15, 2, 2, 1, 2,  1, 1, 0,  0));
        m.put("Mature Colony",           make("Mature Colony",         25, 20, 3, 3, 1, 5,  6, 3, 2,  0));
        m.put("Complex Supercolony",     make("Complex Supercolony",   45, 50, 4, 5, 5,12, 15, 8, 5,  0));
        m.put("Leafcutter Fungus Farm",  make("Leafcutter Fungus Farm",35, 30, 3, 4, 1, 4,  3, 4, 3, 10));
        return m;
    }

    private Map<String, Object> make(String type, int depth, int chambers, int tunnel, int branch,
            int queen, int brood, int food, int entrance, int waste, int fungus) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("nestType",     type);
        cfg.put("depth",        depth);
        cfg.put("chamberCount", chambers);
        cfg.put("tunnelWidth",  tunnel);
        cfg.put("branching",    branch);
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("\uD83D\uDC51 Queen Chamber",   queen);
        dist.put("\uD83E\uDD5A Brood Chambers",  brood);
        dist.put("\uD83C\uDF56 Food Storage",    food);
        dist.put("\uD83D\uDEAA Entrances",       entrance);
        dist.put("\uD83D\uDDD1 Waste Dumps",     waste);
        dist.put("\uD83C\uDF44 Fungus Gardens",  fungus);
        cfg.put("chamberDistribution", dist);
        return cfg;
    }

    // ── Public API ────────────────────────────────────────────────────────────

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

    /**
     * Save a preset (adds to in-memory map and persists everything to disk).
     */
    public void save(String name, Map<String, Object> config) {
        presets.put(name, new LinkedHashMap<>(config));
        persist();
    }

    private void persist() {
        try {
            ObjectMapper m = new ObjectMapper();
            m.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, presets);
        } catch (Exception ex) {
            System.err.println("[NestPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
