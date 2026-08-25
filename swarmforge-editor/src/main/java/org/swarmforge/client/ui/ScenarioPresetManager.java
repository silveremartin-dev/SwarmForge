/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;

import java.io.File;
import java.util.*;

/**
 * Manages Scenario presets: built-in academic research scenarios + user-created scenarios.
 * Persists custom scenarios to {@code scenario_presets.json} on disk without hardcoding.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ScenarioPresetManager {

    public static final File PRESETS_FILE = new File("scenario_presets.json");
    private final Map<String, Scenario> presets = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScenarioPresetManager() {
        loadAll(42L);
    }

    public void loadAll(long masterSeed) {
        presets.clear();
        
        // 1. Built-in Academic Presets
        for (Scenario s : AcademicScenarios.getAllAcademicScenarios(masterSeed)) {
            presets.put(s.getTitle(), s);
        }

        // 2. User-saved Custom Scenarios from file
        if (PRESETS_FILE.exists()) {
            try {
                Scenario[] customScenarios = objectMapper.readValue(PRESETS_FILE, Scenario[].class);
                for (Scenario s : customScenarios) {
                    presets.put(s.getTitle(), s);
                }
            } catch (Exception ex) {
                System.err.println("[ScenarioPresetManager] Failed to read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }
    }

    public Map<String, Scenario> getAll() {
        return Collections.unmodifiableMap(presets);
    }

    public Set<String> getPresetNames() {
        return new TreeSet<>(presets.keySet());
    }

    public Scenario get(String title) {
        return presets.get(title);
    }

    public void save(Scenario scenario) {
        presets.put(scenario.getTitle(), scenario);
        persistCustomScenarios();
    }

    private void persistCustomScenarios() {
        try {
            List<Scenario> customList = new ArrayList<>();
            for (Scenario s : presets.values()) {
                if (!s.getId().startsWith("ACAD_")) {
                    customList.add(s);
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, customList);
        } catch (Exception ex) {
            System.err.println("[ScenarioPresetManager] Failed to write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
