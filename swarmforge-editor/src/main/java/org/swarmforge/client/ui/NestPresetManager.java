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
 * Manages NestGenerator presets: built-in defaults (ants, bees, wasps, termites) + user-saved configurations.
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
        Map<String, Map<String, Object>> m = new TreeMap<>();

        // Ant presets - mapped to exact biological species models
        m.put("Young Subterranean Burrow",                  make("Young Subterranean Burrow",                  "🐜 Black Garden Ant (Lasius niger)", 0, "🐜 Ants (Formicidae)",             "BURROW_UNDERGROUND", "EARTH",             4.0, 10, 15, 2, 2, 1, 2,  1, 1, 0,  0));
        m.put("Mature Subterranean Nest",         make("Mature Subterranean Nest",         "🐜 Dusky Wood Ant (Formica fusca)",   2, "🐜 Ants (Formicidae)",             "BURROW_UNDERGROUND", "EARTH",             5.0, 25, 20, 3, 3, 1, 5,  6, 3, 2,  0));
        m.put("Solar Pine-Needle Mound",              make("Solar Pine-Needle Mound",              "🐜 Red Wood Ant (Formica rufa)",   2, "🐜 Ants (Formicidae)",             "SURFACE_MOUND",      "EARTH",             5.5, 20, 25, 3, 3, 1, 6,  8, 2, 2,  0));
        m.put("Complex Supercolony Network",                 make("Complex Supercolony Network",                 "🐜 Argentine Ant (Linepithema humile)", 3, "🐜 Ants (Formicidae)",             "BURROW_UNDERGROUND", "EARTH",             4.5, 45, 50, 4, 5, 5,12, 15, 8, 5,  0));
        m.put("Subterranean Fungus Vaults",     make("Subterranean Fungus Vaults",     "🐜 Leafcutter Ant (Atta sexdens)", 2, "🐜 Ants (Formicidae)",             "SUBTERRANEAN_FUNGI_VAULT", "EARTH",      7.0, 45, 35, 3, 4, 1, 4,  3, 3, 4, 12));
        m.put("Arboreal Carton Nest",                 make("Arboreal Carton Nest",                 "🐜 Acrobat Ant (Crematogaster scutellaris)", 2, "🐜 Ants (Formicidae)",          "CARTON_NEST",        "CARTON_PULP",       3.5, 15, 20, 2, 3, 1, 5,  6, 2, 2,  0));
        m.put("Hollow Stem & Acorn Nest",             make("Hollow Stem & Acorn Nest",             "🐜 Acorn Ant (Temnothorax unifasciatus)", 1, "🐜 Ants (Formicidae)",        "BAMBOO_STEM_NEST",   "WOOD_PULP_PAPER",   2.5,  8,  8, 1, 1, 1, 3,  2, 1, 1,  0));
        m.put("Hollow Trunk Nest",                 make("Hollow Trunk Nest",                 "🐜 Carpenter Ant (Camponotus herculeanus)", 2, "🐜 Ants (Formicidae)",        "HOLLOW_TRUNK_NEST",  "TREE_TRUNK",       12.0, 10, 20, 3, 3, 1, 6,  8, 2, 2,  0));
        m.put("Living Army Ant Bivouac",                        make("Living Army Ant Bivouac",                        "🐜 Army Ant (Eciton burchellii)",  2, "🐜 Ants (Formicidae)",             "BIVOUAC_LIVING_NEST","LIVING_INSECT_BODIES", 8.0, 15, 25, 3, 3, 1, 8,  8, 2, 2,  0));

        // Bee presets
        m.put("Hexagonal Wax Comb Nest",           make("Hexagonal Wax Comb Nest",           "🐝 Honeybee (Apis mellifera)",         2, "🐝 Honeybees (Apis)",              "WAX_COMB_HEXAGONAL", "BEESWAX",          14.0, 15, 30, 2, 3, 1, 8, 12, 1, 0,  0));
        m.put("Propolis & Wax Pots Cluster",               make("Propolis & Wax Pots Cluster",               "🐝 Buff-Tailed Bumblebee (Bombus terrestris)",   1, "🐝 Bumblebees (Bombus)",           "WAX_POTS_CLUSTER",   "PROPOLIS",         16.0, 10, 18, 2, 2, 1, 4,  6, 1, 1,  0));

        // Wasp presets
        m.put("Hanging Paper Nest",                make("Hanging Paper Nest",                "🐝 Common Wasp (Vespula vulgaris)",        2, "🐝 Wasps & Hornets (Vespidae)",    "PAPER_PEDUNCULATE",  "WOOD_PULP_PAPER",  13.0, 12, 24, 2, 3, 1, 6,  4, 2, 1,  0));

        // Termite presets
        m.put("Cathedral Termite Mound",                 make("Cathedral Termite Mound",                 "🐜 Subterranean Termite (Reticulitermes flavipes)", 3, "🐜 Termites (Isoptera)",           "CATHEDRAL_MOUND", "STERCORAL_CEMENT", 6.0, 30, 40, 3, 4, 1,10,  8, 4, 5,  0));

        // Weaver ant presets
        m.put("Arboreal Silk Weave Nest",                make("Arboreal Silk Weave Nest",                "🐜 Weaver Ant (Oecophylla smaragdina)", 2, "🐜 Ants (Formicidae)",             "ARBOREAL_SILK_LEAF", "SILK_WEAVE",        6.0,  8, 16, 2, 2, 1, 5,  4, 2, 1,  0));

        return m;
    }

    private Map<String, Object> make(String name, String speciesModel, int nestStageIndex, String taxon, String arch, String mat, double workerSizeMm,
            int depth, int chambers, int tunnel, int branch,
            int queen, int brood, int food, int entrance, int waste, int fungus) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("presetName",       name);
        cfg.put("speciesModel",     speciesModel);
        cfg.put("nestStageIndex",   nestStageIndex);
        cfg.put("taxonCategory",    taxon);
        cfg.put("architecture",     arch);
        cfg.put("material",         mat);
        cfg.put("workerSizeMm",     workerSizeMm);
        cfg.put("depth",            depth);
        cfg.put("chamberCount",     chambers);
        cfg.put("tunnelWidth",      tunnel);
        cfg.put("branching",        branch);
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("👑 Queen Chamber",   queen);
        dist.put("🥚 Brood Chambers",  brood);
        dist.put("🍖 Food Storage",    food);
        dist.put("🚪 Entrances",       entrance);
        dist.put("🗑 Waste Dumps",     waste);
        dist.put("🍄 Fungus Gardens",  fungus);
        cfg.put("chamberDistribution", dist);
        return cfg;
    }

    // ── Public API ────────────────────────────────────────────────────────────

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

    /**
     * Save a preset (adds to in-memory map and persists everything to disk).
     */
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
            System.err.println("[NestPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}

