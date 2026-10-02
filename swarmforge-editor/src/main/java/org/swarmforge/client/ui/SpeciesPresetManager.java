/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swarmforge.core.species.DefaultSpecies;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Manages Species presets loaded purely from JSON files (in {@code data/presets/species/} and classpath).
 * Persists user-created presets to {@code species_presets.json} in the working directory.
 * No hardcoded species data is stored in Java code.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesPresetManager {

    public static final File PRESETS_FILE = new File("species_presets.json");

    private static final String[] BUILTIN_SPECIES_FILES = {
            "aphis-fabae.json",
            "apis-mellifera.json",
            "atta-cephalotes.json",
            "atta-sexdens.json",
            "austroplatypus-incompertus.json",
            "bombus-terrestris.json",
            "camponotus-ligniperda.json",
            "camponotus-pennsylvanicus.json",
            "cataglyphis-bombycina.json",
            "formica-fusca.json",
            "formica-rufa.json",
            "kladothrips-harteri.json",
            "lasius-niger.json",
            "linepithema-humile.json",
            "messor-barbarus.json",
            "myrmeleon-formicarius.json",
            "odontomachus-bauri.json",
            "pieris-brassicae.json",
            "pogonomyrmex-barbatus.json",
            "polyergus-rufescens.json",
            "porcellio-scaber.json",
            "pseudoregma-bambucicola.json",
            "reticulitermes-flavipes.json",
            "solenopsis-invicta.json",
            "vespa-crabro.json",
            "vespula-germanica.json",
            "vespula-vulgaris.json"
    };

    private final Map<String, DefaultSpecies> presets = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SpeciesPresetManager() {
        loadAll();
    }

    private void loadAll() {
        presets.clear();

        // 1. Load built-in species from JSON files / resources
        for (String file : BUILTIN_SPECIES_FILES) {
            DefaultSpecies sp = loadBuiltinSpecies(file);
            if (sp != null) {
                sp.setBuiltIn(true);
                sp.setAuthor("Academic Reference");
                sp.calculateChecksum();
                presets.put(sp.getPresetName(), sp);
            }
        }

        // 2. Load user presets from filesystem
        if (PRESETS_FILE.exists()) {
            try {
                Map<String, DefaultSpecies> saved = mapper.readValue(PRESETS_FILE, new TypeReference<LinkedHashMap<String, DefaultSpecies>>() {});
                for (Map.Entry<String, DefaultSpecies> entry : saved.entrySet()) {
                    DefaultSpecies s = entry.getValue();
                    if (s == null) continue;
                    // Protect built-in names
                    if (presets.containsKey(entry.getKey()) && presets.get(entry.getKey()).isBuiltIn()) {
                        s.setPresetName("[Fork] " + s.getPresetName());
                        s.setId("custom-fork-" + s.getId() + "-" + s.calculateChecksum().substring(0, 4));
                    }
                    s.setBuiltIn(false);
                    s.calculateChecksum();
                    presets.put(s.getPresetName(), s);
                }
            } catch (Exception e) {
                System.err.println("[SpeciesPresetManager] Could not load " + PRESETS_FILE + ": " + e.getMessage());
            }
        }

        // Register all presets in core SpeciesRegistry
        for (DefaultSpecies species : presets.values()) {
            org.swarmforge.core.species.SpeciesRegistry.getInstance().register(species);
        }
    }

    private DefaultSpecies loadBuiltinSpecies(String filename) {
        DefaultSpecies sp = null;
        File[] candidatePaths = new File[]{
                new File("data/presets/species", filename),
                new File("../data/presets/species", filename),
                new File("presets/species", filename),
                new File("../presets/species", filename),
                new File("swarmforge-core/src/main/resources/presets/species", filename),
                new File("../swarmforge-core/src/main/resources/presets/species", filename)
        };

        for (File path : candidatePaths) {
            if (path.exists()) {
                try {
                    sp = mapper.readValue(path, DefaultSpecies.class);
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (sp == null) {
            try (InputStream in = getClass().getResourceAsStream("/presets/species/" + filename)) {
                if (in != null) {
                    sp = mapper.readValue(in, DefaultSpecies.class);
                }
            } catch (Exception ignored) {}
        }
        return sp;
    }

    public Set<String> getPresetNames() {
        return presets.keySet();
    }

    public DefaultSpecies getPreset(String name) {
        return presets.get(name);
    }

    public boolean isBuiltIn(String name) {
        DefaultSpecies s = presets.get(name);
        return s != null && s.isBuiltIn();
    }

    public boolean contains(String name) {
        return presets.containsKey(name);
    }

    public void addPreset(String name, DefaultSpecies species) {
        savePreset(name, species);
    }

    public void savePreset(String name, DefaultSpecies species) {
        if (name == null || name.isBlank() || species == null) return;
        species.setPresetName(name);
        if (isBuiltIn(name)) {
            species.setPresetName("[Fork] " + name);
            species.setBuiltIn(false);
        }
        species.calculateChecksum();
        presets.put(species.getPresetName(), species);
        org.swarmforge.core.species.SpeciesRegistry.getInstance().register(species);
        persist();
    }

    public boolean delete(String name) {
        if (presets.containsKey(name)) {
            DefaultSpecies target = presets.get(name);
            if (target.isBuiltIn()) {
                return false; // Cannot delete reference built-in species
            }
            presets.remove(name);
            persist();
            return true;
        }
        return false;
    }

    public void persist() {
        try {
            Map<String, DefaultSpecies> customOnly = new LinkedHashMap<>();
            for (Map.Entry<String, DefaultSpecies> entry : presets.entrySet()) {
                if (!entry.getValue().isBuiltIn()) {
                    customOnly.put(entry.getKey(), entry.getValue());
                }
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, customOnly);
        } catch (Exception e) {
            System.err.println("[SpeciesPresetManager] Could not save " + PRESETS_FILE + ": " + e.getMessage());
        }
    }

    public void saveToFile(File file, DefaultSpecies species) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, species);
    }

    public DefaultSpecies loadFromFile(File file) throws Exception {
        return mapper.readValue(file, DefaultSpecies.class);
    }

    public DefaultSpecies getPresetOrFallback(String name) {
        if (name == null || name.isBlank()) {
            return presets.isEmpty() ? null : presets.values().iterator().next();
        }
        DefaultSpecies sp = presets.get(name);
        if (sp != null) return sp;
        String cleanName = name.replaceAll("\\s*\\([^)]*\\)", "").trim();
        for (DefaultSpecies s : presets.values()) {
            if (s.getPresetName().equalsIgnoreCase(name) ||
                s.getPresetName().equalsIgnoreCase(cleanName) ||
                (s.getCommonName() != null && (s.getCommonName().equalsIgnoreCase(name) || s.getCommonName().equalsIgnoreCase(cleanName))) ||
                (s.getScientificName() != null && (s.getScientificName().equalsIgnoreCase(name) || s.getScientificName().equalsIgnoreCase(cleanName))) ||
                (s.getScientificName() != null && !cleanName.isBlank() && (cleanName.toLowerCase().contains(s.getScientificName().toLowerCase()) || s.getScientificName().toLowerCase().contains(cleanName.toLowerCase()))) ||
                (s.getPresetName() != null && !cleanName.isBlank() && (cleanName.toLowerCase().contains(s.getPresetName().toLowerCase()) || s.getPresetName().toLowerCase().contains(cleanName.toLowerCase())))) {
                return s;
            }
        }
        DefaultSpecies fallback = new DefaultSpecies();
        fallback.setPresetName(name);
        fallback.setCommonName(cleanName.isEmpty() ? name : cleanName);
        fallback.setScientificName(cleanName.isEmpty() ? name : cleanName);
        fallback.setInsectType("ANT");
        fallback.setNestType("UNDERGROUND_BURROW");
        fallback.setOptimalTempCelsius(24.0f);
        fallback.setMinTempCelsius(0.0f);
        fallback.setMaxTempCelsius(45.0f);
        fallback.setOptimalHumidityPercent(75.0f);
        fallback.setMinHumidityPercent(15.0f);
        fallback.setMaxHumidityPercent(100.0f);
        fallback.setDescription("Dynamically synthesized fallback species.");
        presets.put(name, fallback);
        org.swarmforge.core.species.SpeciesRegistry.getInstance().register(fallback);
        return fallback;
    }
}
