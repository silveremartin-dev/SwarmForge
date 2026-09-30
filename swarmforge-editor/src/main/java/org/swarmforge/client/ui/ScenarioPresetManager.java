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
 * Protects built-in scenarios from overwrite and prevents silent title/ID collisions.
 * Persists custom scenarios to {@code scenario_presets.json} on disk.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ScenarioPresetManager {

    public static final File PRESETS_FILE = new File("scenario_presets.json");
    private final Map<String, Scenario> presetsById = new LinkedHashMap<>();
    private final Map<String, String> displayTitleToId = new LinkedHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScenarioPresetManager() {
        loadAll(42L);
    }

    public void loadAll(long masterSeed) {
        presetsById.clear();
        displayTitleToId.clear();

        // 1. Built-in Academic Presets
        for (Scenario s : AcademicScenarios.getAllAcademicScenarios(masterSeed)) {
            s.setBuiltIn(true);
            s.setAuthor("Academic Reference");
            s.calculateChecksum();
            registerPreset(s);
        }

        // 2. Built-in Multiplayer & Sharded Megaterrarium Presets
        for (Scenario s : AcademicScenarios.getAllMultiplayerScenarios(masterSeed)) {
            s.setBuiltIn(true);
            s.setAuthor("Academic Multiplayer");
            s.calculateChecksum();
            registerPreset(s);
        }

        // 3. User-saved Custom Scenarios from file
        if (PRESETS_FILE.exists()) {
            try {
                Scenario[] customScenarios = objectMapper.readValue(PRESETS_FILE, Scenario[].class);
                for (Scenario s : customScenarios) {
                    if (s == null) continue;
                    // Protect against shadowing built-in presets
                    if (s.getId() == null || s.getId().startsWith("ACAD_") || s.getId().startsWith("MP_") || presetsById.containsKey(s.getId())) {
                        if (presetsById.containsKey(s.getId()) && presetsById.get(s.getId()).isBuiltIn()) {
                            s.setId("CUSTOM_FORK_" + s.getId() + "_" + s.calculateChecksum().substring(0, 6));
                            s.setTitle("[Fork] " + s.getTitle());
                        }
                    }
                    s.setBuiltIn(false);
                    s.calculateChecksum();
                    registerPreset(s);
                }
            } catch (Exception ex) {
                System.err.println("[ScenarioPresetManager] Failed to read " + PRESETS_FILE + ": " + ex.getMessage());
            }
        }

        rebuildDisplayIndex();
    }

    private void registerPreset(Scenario s) {
        if (s == null) return;
        String id = s.getId();
        if (id == null || id.isEmpty()) {
            id = "SCENARIO_" + s.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "_") + "_" + s.calculateChecksum().substring(0, 6);
            s.setId(id);
        }
        presetsById.put(id, s);
    }

    /**
     * Rebuilds display title mappings, resolving naming collisions by appending version/author tags.
     */
    private void rebuildDisplayIndex() {
        displayTitleToId.clear();
        Map<String, List<Scenario>> byRawTitle = new LinkedHashMap<>();

        for (Scenario s : presetsById.values()) {
            String rawTitle = s.getTitle() != null ? s.getTitle() : s.getId();
            byRawTitle.computeIfAbsent(rawTitle, k -> new ArrayList<>()).add(s);
        }

        for (Map.Entry<String, List<Scenario>> entry : byRawTitle.entrySet()) {
            String rawTitle = entry.getKey();
            List<Scenario> list = entry.getValue();

            if (list.size() == 1) {
                Scenario s = list.get(0);
                displayTitleToId.put(rawTitle, s.getId());
            } else {
                // Disambiguate identical titles with author and version badges
                for (Scenario s : list) {
                    String disambiguatedTitle;
                    if (s.isBuiltIn()) {
                        disambiguatedTitle = rawTitle + " [Référence Académique]";
                    } else {
                        String author = (s.getAuthor() != null && !s.getAuthor().isEmpty()) ? s.getAuthor() : "Custom";
                        disambiguatedTitle = String.format("%s (v%d - %s #%s)", rawTitle, s.getVersion(), author, s.getContentChecksum().substring(0, 4));
                    }
                    displayTitleToId.put(disambiguatedTitle, s.getId());
                }
            }
        }
    }

    public Map<String, Scenario> getAll() {
        return Collections.unmodifiableMap(presetsById);
    }

    public Set<String> getPresetNames() {
        return new TreeSet<>(displayTitleToId.keySet());
    }

    public Scenario getById(String id) {
        return presetsById.get(id);
    }

    public Scenario get(String titleOrId) {
        if (titleOrId == null) return null;
        if (presetsById.containsKey(titleOrId)) {
            return presetsById.get(titleOrId);
        }
        String id = displayTitleToId.get(titleOrId);
        if (id != null) {
            return presetsById.get(id);
        }
        // Fallback: match by raw title
        for (Scenario s : presetsById.values()) {
            if (titleOrId.equalsIgnoreCase(s.getTitle()) || titleOrId.equalsIgnoreCase(s.getId())) {
                return s;
            }
        }
        return null;
    }

    public void save(Scenario scenario) {
        if (scenario == null) return;
        
        // Prevent overwriting built-in academic presets
        if (scenario.getId() != null && (scenario.getId().startsWith("ACAD_") || scenario.getId().startsWith("MP_") || (presetsById.containsKey(scenario.getId()) && presetsById.get(scenario.getId()).isBuiltIn()))) {
            String forkId = "CUSTOM_FORK_" + scenario.getId() + "_" + System.currentTimeMillis();
            scenario.setId(forkId);
            scenario.setTitle("[Fork] " + scenario.getTitle());
            scenario.setBuiltIn(false);
        }

        if (scenario.getId() == null || scenario.getId().isEmpty()) {
            String cleanTitle = scenario.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "_");
            scenario.setId("CUSTOM_" + cleanTitle + "_" + System.currentTimeMillis());
        }

        scenario.setBuiltIn(false);
        scenario.setRevisionTimestamp(System.currentTimeMillis());
        scenario.calculateChecksum();

        // Check for collision with existing custom scenario
        if (presetsById.containsKey(scenario.getId())) {
            Scenario existing = presetsById.get(scenario.getId());
            if (!existing.isContentEqualTo(scenario)) {
                scenario.setVersion(existing.getVersion() + 1);
            }
        }

        presetsById.put(scenario.getId(), scenario);
        rebuildDisplayIndex();
        persistCustomScenarios();
    }

    public boolean delete(String titleOrId) {
        Scenario target = get(titleOrId);
        if (target == null || target.isBuiltIn()) {
            return false; // Built-in scenarios cannot be deleted
        }
        presetsById.remove(target.getId());
        rebuildDisplayIndex();
        persistCustomScenarios();
        return true;
    }

    private void persistCustomScenarios() {
        try {
            List<Scenario> customList = new ArrayList<>();
            for (Scenario s : presetsById.values()) {
                if (!s.isBuiltIn() && s.getId() != null && !s.getId().startsWith("ACAD_") && !s.getId().startsWith("MP_")) {
                    customList.add(s);
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, customList);
        } catch (Exception ex) {
            System.err.println("[ScenarioPresetManager] Failed to write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
