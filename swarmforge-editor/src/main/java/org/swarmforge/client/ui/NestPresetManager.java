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
 * Manages NestGenerator presets: loaded purely from JSON files in {@code presets/nests/} and classpath.
 * Persists user presets to {@code nest_presets.json} in the working directory.
 * No hardcoded nest data is stored in Java code.
 */
public class NestPresetManager {

    public static final File PRESETS_FILE = new File("nest_presets.json");

    private static final String[] BUILTIN_NEST_FILES = {
            "young-subterranean-burrow.json",
            "mature-subterranean-nest.json",
            "solar-pine-needle-mound.json",
            "complex-supercolony-network.json",
            "subterranean-fungus-vaults.json",
            "arboreal-carton-nest.json",
            "hollow-stem-acorn-nest.json",
            "hollow-trunk-nest.json",
            "hanging-paper-nest.json",
            "cathedral-termite-mound.json",
            "hexagonal-wax-comb-nest.json",
            "arboreal-silk-weave-nest.json",
            "propolis-wax-pots-cluster.json",
            "living-army-ant-bivouac.json"
    };

    /** All presets (built-in first, then user-saved). */
    private final Map<String, Map<String, Object>> presets = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public NestPresetManager() {
        loadAll();
    }

    private void loadAll() {
        presets.clear();

        // 1. Load built-ins from JSON files / resources
        for (String file : BUILTIN_NEST_FILES) {
            Map<String, Object> cfg = loadBuiltinNest(file);
            if (cfg != null && cfg.containsKey("presetName")) {
                presets.put(String.valueOf(cfg.get("presetName")), cfg);
            }
        }

        // 2. Load user presets from filesystem
        if (PRESETS_FILE.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> saved = mapper.readValue(PRESETS_FILE, Map.class);
                presets.putAll(saved);
            } catch (Exception ex) {
                System.err.println("[NestPresets] Could not read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadBuiltinNest(String filename) {
        Map<String, Object> cfg = null;
        File[] candidatePaths = new File[]{
                new File("data/presets/nests", filename),
                new File("../data/presets/nests", filename),
                new File("presets/nests", filename),
                new File("../presets/nests", filename),
                new File("swarmforge-core/src/main/resources/presets/nests", filename),
                new File("../swarmforge-core/src/main/resources/presets/nests", filename)
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
            try (InputStream in = getClass().getResourceAsStream("/presets/nests/" + filename)) {
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
            mapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, presets);
        } catch (Exception ex) {
            System.err.println("[NestPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
