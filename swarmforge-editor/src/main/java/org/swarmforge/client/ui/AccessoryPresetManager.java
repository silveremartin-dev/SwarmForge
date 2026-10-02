/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.swarmforge.core.domain.AccessoryPreset;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages Accessory, Prey, Predator, Mutualist, Flora, Fungi, and Pathogen presets.
 * Loaded purely from JSON files in {@code data/presets/accessories/} and classpath.
 * Persists user presets to {@code accessory_presets.json} or user home.
 * No hardcoded accessory data is stored in Java code.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AccessoryPresetManager {

    public static final File PRESETS_FILE = new File("accessory_presets.json");

    private static final String[] BUILTIN_ACCESSORY_FILES = {
            "flora-grasses-messor.json",
            "flora-humid-moss.json",
            "flora-nectar-flowers.json",
            "mutualist-aphid-cinara.json",
            "mutualist-aphid-fabae.json",
            "mutualist-mealybug-eurhizococcus.json",
            "prey-mealworm.json",
            "prey-caterpillar-pieris.json",
            "prey-termite-microtermes.json",
            "prey-woodlouse-porcellio.json",
            "predator-antlion-myrmeleon.json",
            "predator-spider-salticidae.json",
            "predator-asian-hornet.json",
            "predator-bee-eater-wasp.json",
            "predator-honey-buzzard.json",
            "predator-megaponera.json",
            "predator-woodpecker.json",
            "predator-tamandua.json",
            "parasite-cordyceps.json",
            "parasite-varroa.json",
            "parasite-eucharitid.json",
            "parasite-nosema.json",
            "fungi-leucoagaricus.json",
            "fungi-termitomyces.json",
            "detritivore-springtails.json",
            "detritivore-lomechusa.json"
    };

    private final Map<String, AccessoryPreset> presets = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public AccessoryPresetManager() {
        loadAll();
    }

    public void loadAll() {
        presets.clear();

        // 1. Load built-ins from JSON files / resources
        for (String file : BUILTIN_ACCESSORY_FILES) {
            AccessoryPreset p = loadBuiltinAccessory(file);
            if (p != null) {
                String key = p.getName() != null ? p.getName() : p.getId();
                presets.put(key, p);
            }
        }

        // 2. Scan data/presets/accessories directory for custom/added presets
        File[] dataDirs = {
                new File("data/presets/accessories"),
                new File("../data/presets/accessories")
        };
        for (File dir : dataDirs) {
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File f : files) {
                        try {
                            AccessoryPreset p = mapper.readValue(f, AccessoryPreset.class);
                            String key = p.getName() != null ? p.getName() : p.getId();
                            presets.put(key, p);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        // 3. Load user presets from filesystem
        if (PRESETS_FILE.exists()) {
            try {
                AccessoryPreset[] saved = mapper.readValue(PRESETS_FILE, AccessoryPreset[].class);
                for (AccessoryPreset p : saved) {
                    String key = p.getName() != null ? p.getName() : p.getId();
                    presets.put(key, p);
                }
            } catch (Exception ex) {
                System.err.println("[AccessoryPresets] Could not read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }
    }

    public AccessoryPreset loadBuiltinAccessory(String filename) {
        // Classpath first
        try (InputStream in = getClass().getResourceAsStream("/presets/accessories/" + filename)) {
            if (in != null) {
                return mapper.readValue(in, AccessoryPreset.class);
            }
        } catch (Exception ignored) {}

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("presets/accessories/" + filename)) {
            if (in != null) {
                return mapper.readValue(in, AccessoryPreset.class);
            }
        } catch (Exception ignored) {}

        // Relative filesystem fallback
        File[] candidates = {
                new File("data/presets/accessories", filename),
                new File("../data/presets/accessories", filename),
                new File("swarmforge-core/src/main/resources/presets/accessories", filename),
                new File("src/main/resources/presets/accessories", filename),
                new File("../swarmforge-core/src/main/resources/presets/accessories", filename)
        };
        for (File f : candidates) {
            if (f.exists()) {
                try {
                    return mapper.readValue(f, AccessoryPreset.class);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public List<String> getPresetNames() {
        return new ArrayList<>(presets.keySet());
    }

    public Map<String, AccessoryPreset> getAll() {
        return Collections.unmodifiableMap(presets);
    }

    public Optional<AccessoryPreset> get(String name) {
        if (name == null) return Optional.empty();
        AccessoryPreset direct = presets.get(name);
        if (direct != null) return Optional.of(direct);

        // Fallback case-insensitive / id search
        for (Map.Entry<String, AccessoryPreset> entry : presets.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) return Optional.of(entry.getValue());
            if (entry.getValue().getId() != null && entry.getValue().getId().equalsIgnoreCase(name)) return Optional.of(entry.getValue());
        }
        return Optional.empty();
    }

    public void savePreset(String name, AccessoryPreset preset) {
        presets.put(name, preset);
        saveToFile();
    }

    public void deletePreset(String name) {
        presets.remove(name);
        saveToFile();
    }

    private void saveToFile() {
        try {
            // Save only non-built-in presets to avoid duplicating static files
            List<AccessoryPreset> userList = presets.values().stream()
                    .filter(p -> !p.isBuiltIn())
                    .toList();
            mapper.writeValue(PRESETS_FILE, userList);
        } catch (Exception ex) {
            System.err.println("[AccessoryPresets] Could not save presets: " + ex.getMessage());
        }
    }
}
