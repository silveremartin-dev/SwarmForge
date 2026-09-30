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

        // 1. Temperate Deciduous Forest (Fontainebleau, France)
        map.put("Temperate Deciduous (Fontainebleau, FR)", makeConfig(
                "Fontainebleau, FR",
                25.0, 3.0, 0.5, 48.4047, 2.7016, "FOREST",
                0.45, 0.45, 65.0, 0.7, 0.3, 0.08,
                40, 20, 15, 15, 5, 0, 5, 0,
                0, 70, 10, 0, 0, 10, 0, 10,
                "774829", 40.0, 60.0, 50.0, 40.0,
                true, true, true, false, true, true, true,
                true, 120.0, 0.3, 2.0, 1500.0,
                8, 3, 3
        ));

        // 2. Arid Desert Erg Chebbi (Sahara, Morocco)
        map.put("Arid Desert (Erg Chebbi, MA)", makeConfig(
                "Erg Chebbi, MA",
                25.0, 3.0, 0.8, 31.1444, -3.9708, "DESERT",
                0.65, 0.12, 70.0, 0.3, 0.5, 0.02,
                0, 75, 5, 0, 5, 0, 15, 0,
                3, 0, 0, 15, 75, 0, 0, 10,
                "284910", 10.0, 20.0, 5.0, 20.0,
                false, false, true, false, false, false, false,
                false, 60.0, 0.0, 0.0, 3500.0,
                3, 1, 6
        ));

        // 3. Equatorial Tropical Rainforest (Amazon, Manaus, Brazil)
        map.put("Tropical Rainforest (Manaus, BR)", makeConfig(
                "Manaus, BR",
                25.0, 3.0, 0.4, -3.1190, -60.0217, "TROPICAL",
                0.40, 0.85, 50.0, 0.6, 0.4, 0.12,
                35, 10, 20, 25, 0, 0, 0, 10,
                2, 0, 5, 80, 0, 0, 5, 10,
                "918273", 80.0, 70.0, 80.0, 60.0,
                true, true, true, true, true, false, true,
                true, 250.0, 0.8, 4.0, 600.0,
                12, 5, 2
        ));

        // 4. Rocky Mountain & Scree (Mont Blanc, Chamonix, France)
        map.put("Rocky Mountain (Mont Blanc, FR)", makeConfig(
                "Mont Blanc, FR",
                25.0, 3.0, 0.6, 45.8326, 6.8652, "ALPINE",
                0.90, 0.40, 85.0, 0.8, 0.2, 0.15,
                10, 10, 10, 10, 15, 0, 45, 0,
                1, 10, 75, 0, 0, 10, 0, 5,
                "551928", 25.0, 50.0, 40.0, 50.0,
                false, true, true, false, true, true, true,
                true, 90.0, 1.2, 1.0, 2500.0,
                5, 2, 8
        ));

        // 5. Arctic Permafrost (Longyearbyen, Svalbard, Norway)
        map.put("Permafrost Tundra (Svalbard, NO)", makeConfig(
                "Svalbard, NO",
                25.0, 3.0, 0.5, 78.2232, 15.6469, "ARCTIC",
                0.25, 0.30, 90.0, 0.9, 0.1, 0.04,
                15, 10, 15, 10, 10, 25, 15, 0,
                4, 15, 15, 0, 0, 60, 0, 10,
                "109283", 15.0, 30.0, 15.0, 25.0,
                false, false, true, false, true, false, false,
                false, 50.0, 0.1, 3.0, 1000.0,
                2, 1, 5
        ));

        // 6. Acacia Savanna (Serengeti, Tanzania)
        map.put("Acacia Savanna (Serengeti, TZ)", makeConfig(
                "Serengeti, TZ",
                25.0, 3.0, 0.5, -2.3333, 34.8333, "SAVANNA",
                0.35, 0.28, 60.0, 0.5, 0.4, 0.05,
                15, 45, 15, 15, 5, 0, 5, 0,
                6, 0, 0, 90, 0, 0, 0, 10,
                "381920", 50.0, 40.0, 30.0, 30.0,
                false, true, true, false, false, false, false,
                true, 150.0, 0.4, 1.0, 2500.0,
                6, 4, 3
        ));

        // 7. Mediterranean Shrubland (Corsica, France)
        map.put("Mediterranean Shrubland (Corsica, FR)", makeConfig(
                "Corsica, FR",
                25.0, 3.0, 0.5, 42.1500, 9.1500, "MEDITERRANEAN",
                0.55, 0.35, 70.0, 0.6, 0.3, 0.06,
                25, 25, 15, 15, 10, 0, 10, 0,
                1, 15, 70, 0, 0, 0, 0, 15,
                "492810", 35.0, 50.0, 45.0, 45.0,
                true, true, true, false, true, true, true,
                true, 80.0, 0.3, 1.0, 2000.0,
                7, 3, 5
        ));

        // 8. Boreal Taiga (Rovaniemi, Lapland, Finland)
        map.put("Boreal Taiga (Rovaniemi, FI)", makeConfig(
                "Rovaniemi, FI",
                25.0, 3.0, 0.5, 66.5039, 25.7294, "TAIGA",
                0.40, 0.55, 65.0, 0.75, 0.25, 0.07,
                30, 15, 15, 15, 5, 10, 10, 0,
                1, 10, 80, 0, 0, 10, 0, 10,
                "618293", 30.0, 70.0, 70.0, 60.0,
                true, false, true, true, true, true, true,
                true, 140.0, 0.4, 3.0, 1200.0,
                9, 4, 3
        ));

        // 9. Swamp & Mangrove (Everglades, Florida, USA)
        map.put("Swamp & Mangrove (Everglades, US)", makeConfig(
                "Everglades, US",
                25.0, 3.0, 0.4, 25.2866, -80.8987, "WETLAND",
                0.20, 0.90, 40.0, 0.4, 0.5, 0.10,
                30, 5, 25, 20, 0, 15, 0, 5,
                2, 0, 10, 70, 0, 0, 10, 10,
                "829104", 75.0, 65.0, 60.0, 50.0,
                true, true, true, true, true, false, true,
                true, 300.0, 0.2, 5.0, 150.0,
                8, 6, 2
        ));

        // 10. Semi-Arid Steppe (Astana, Kazakhstan)
        map.put("Semi-Arid Steppe (Astana, KZ)", makeConfig(
                "Astana, KZ",
                25.0, 3.0, 0.6, 51.1694, 71.4491, "STEPPE",
                0.30, 0.30, 75.0, 0.65, 0.35, 0.04,
                35, 25, 15, 15, 5, 0, 5, 0,
                4, 20, 10, 0, 0, 50, 0, 20,
                "501928", 65.0, 30.0, 20.0, 25.0,
                false, true, true, false, false, false, false,
                true, 70.0, 0.3, 1.0, 2500.0,
                4, 2, 4
        ));

        return map;
    }

    private Map<String, Object> makeConfig(String cityName, double surfaceSize, double depth, double res,
                                           double lat, double lon, String biome,
                                           double roughness, double baseHumidity, double compaction,
                                           double stratification, double mixingRate, double voidDensity,
                                           int earth, int sand, int silt, int clay, int gravel, int peat, int stone, int organic,
                                           int treeSpeciesIdx, int oak, int pine, int acacia, int cactus, int birch, int bamboo, int deadWood,
                                           String floraSeed, double edibleDensity, double nonEdibleDensity, double leafLitter, double twigDebris,
                                           boolean aphidPlant, boolean nectarFlowers, boolean seedGrass, boolean fungusFoliage, boolean moss, boolean pineLitter, boolean fernObstacle,
                                           boolean hasRiver, double riverWidth, double riverVelocity, double staticPools, double waterTableDepth,
                                           int treeCount, int hollowLogs, int rockCrevices) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("cityName", cityName);
        cfg.put("surfaceSizeMeters", surfaceSize);
        cfg.put("depthMeters", depth);
        cfg.put("resolutionMm", res);
        cfg.put("latitude", lat);
        cfg.put("longitude", lon);
        cfg.put("biome", biome);
        cfg.put("roughness", roughness);
        cfg.put("baseHumidity", baseHumidity);
        cfg.put("compaction", compaction);
        cfg.put("stratification", stratification);
        cfg.put("mixingRate", mixingRate);
        cfg.put("voidDensity", voidDensity);
        cfg.put("soilComposition", Map.of(
                "earth", earth,
                "sand", sand,
                "silt", silt,
                "clay", clay,
                "gravel", gravel,
                "peat", peat,
                "stone", stone,
                "organic", organic
        ));
        cfg.put("treeSpeciesIndex", treeSpeciesIdx);
        cfg.put("treeComposition", Map.of(
                "oak", oak, "pine", pine, "acacia", acacia, "cactus", cactus,
                "birch", birch, "bamboo", bamboo, "deadWood", deadWood
        ));
        cfg.put("floraSeed", floraSeed);
        cfg.put("edibleFloraDensity", edibleDensity);
        cfg.put("nonEdibleFloraDensity", nonEdibleDensity);
        cfg.put("leafLitter", leafLitter);
        cfg.put("twigDebris", twigDebris);
        cfg.put("aphidPlant", aphidPlant);
        cfg.put("nectarFlowers", nectarFlowers);
        cfg.put("seedGrass", seedGrass);
        cfg.put("fungusFoliage", fungusFoliage);
        cfg.put("moss", moss);
        cfg.put("pineLitter", pineLitter);
        cfg.put("fernObstacle", fernObstacle);
        cfg.put("hasRiver", hasRiver);
        cfg.put("riverWidthMm", riverWidth);
        cfg.put("riverVelocity", riverVelocity);
        cfg.put("staticPools", staticPools);
        cfg.put("waterTableDepth", waterTableDepth);
        cfg.put("treeCount", treeCount);
        cfg.put("hollowLogs", hollowLogs);
        cfg.put("rockCrevices", rockCrevices);
        cfg.put("builtIn", true);
        cfg.put("author", "Academic Reference");
        cfg.put("version", 1);
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
        return new TreeSet<>(presets.keySet());
    }

    public Map<String, Object> get(String name) {
        return presets.get(name);
    }

    public Map<String, Object> getPreset(String name) {
        return get(name);
    }

    public boolean contains(String name) {
        return presets.containsKey(name);
    }

    public boolean isBuiltIn(String name) {
        if (name == null || !presets.containsKey(name)) return false;
        Map<String, Object> cfg = presets.get(name);
        return cfg != null && Boolean.TRUE.equals(cfg.get("builtIn"));
    }

    public void save(String name, Map<String, Object> config) {
        if (name == null || name.isBlank() || config == null) return;
        Map<String, Object> copy = new LinkedHashMap<>(config);
        
        // Protect built-in presets
        if (isBuiltIn(name)) {
            name = "[Fork] " + name;
            copy.put("builtIn", false);
        }

        copy.put("builtIn", false);
        copy.put("revisionTimestamp", System.currentTimeMillis());
        
        // Calculate deterministic checksum
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
                return false; // Cannot delete reference built-in world
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
            ObjectMapper m = new ObjectMapper();
            m.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, customOnly);
        } catch (Exception ex) {
            System.err.println("[WorldPresets] Could not write " + PRESETS_FILE + ": " + ex.getMessage());
        }
    }
}
