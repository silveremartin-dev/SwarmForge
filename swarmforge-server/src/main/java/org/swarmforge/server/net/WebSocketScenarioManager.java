package org.swarmforge.server.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.simulation.Simulation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Handles server-side scenario catalogs, persistence, checksum calculation, and scenario instantiation.
 */
public final class WebSocketScenarioManager {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketScenarioManager.class);

    private WebSocketScenarioManager() {
        // Utility class
    }

    public static void initScenarioStorage(Path storageDir, Map<String, JsonObject> customScenarios) {
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            try (var stream = Files.walk(storageDir)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    try {
                        String content = Files.readString(p);
                        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                        String id = obj.has("id") ? obj.get("id").getAsString() : p.getFileName().toString().replace(".json", "");
                        obj.addProperty("id", id);
                        customScenarios.put(id, obj);
                        LOG.info("Loaded server scenario: {} from {}", id, p);
                    } catch (Exception e) {
                        LOG.warn("Failed to load scenario file {}: {}", p, e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOG.warn("Could not initialize scenario storage directory: {}", e.getMessage());
        }
    }

    public static String computeJsonChecksum(JsonObject json) {
        if (json == null) return "00000000";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            if (json.has("title")) sb.append(json.get("title").getAsString());
            if (json.has("masterSeed")) sb.append(json.get("masterSeed").getAsString());
            if (json.has("width")) sb.append(json.get("width").getAsString());
            if (json.has("height")) sb.append(json.get("height").getAsString());
            if (json.has("depth")) sb.append(json.get("depth").getAsString());
            if (json.has("biomeName")) sb.append(json.get("biomeName").getAsString());
            if (json.has("worldPresetId")) sb.append(json.get("worldPresetId").getAsString());
            if (json.has("colonies")) sb.append(json.get("colonies").toString());
            if (json.has("events")) sb.append(json.get("events").toString());
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().substring(0, 12);
        } catch (Exception e) {
            return "00000000";
        }
    }

    public static void sendServerScenarios(WebSocket conn, Map<String, JsonObject> customScenarios,
                                          String selectedScenarioId, String currentLobbyStatus) {
        if (conn == null || !conn.isOpen()) return;
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "SERVER_SCENARIOS_LIST");
        JsonArray arr = new JsonArray();

        try {
            List<Scenario> builtInScenarios = new ArrayList<>();
            builtInScenarios.addAll(AcademicScenarios.getAllAcademicScenarios(42L));
            builtInScenarios.addAll(AcademicScenarios.getAllMultiplayerScenarios(42L));

            Map<String, Integer> titleCounts = new HashMap<>();
            for (Scenario sc : builtInScenarios) {
                titleCounts.put(sc.getTitle(), titleCounts.getOrDefault(sc.getTitle(), 0) + 1);
            }
            for (JsonObject customSc : customScenarios.values()) {
                String t = customSc.has("title") ? customSc.get("title").getAsString() : (customSc.has("name") ? customSc.get("name").getAsString() : "Custom");
                titleCounts.put(t, titleCounts.getOrDefault(t, 0) + 1);
            }

            for (Scenario sc : builtInScenarios) {
                JsonObject item = new JsonObject();
                item.addProperty("id", sc.getId());
                String title = sc.getTitle();
                if (titleCounts.getOrDefault(title, 0) > 1) {
                    title = title + " [Référence Académique]";
                }
                item.addProperty("title", title);
                item.addProperty("description", sc.getDescription());
                item.addProperty("academicCategory", sc.getAcademicCategory() != null ? sc.getAcademicCategory() : "Academic");
                item.addProperty("biomeName", sc.getBiomeName());
                item.addProperty("requiredPlayerCount", sc.getRequiredPlayerCount());
                item.addProperty("isMultiplayerOnly", sc.isMultiplayerOnly());
                item.addProperty("maxDurationValue", sc.getMaxDurationValue());
                item.addProperty("maxDurationUnit", sc.getMaxDurationUnit());
                item.addProperty("version", sc.getVersion());
                item.addProperty("author", sc.getAuthor());
                item.addProperty("contentChecksum", sc.getContentChecksum());
                item.addProperty("isBuiltIn", true);

                if (sc.getId().equals(selectedScenarioId)) {
                    item.addProperty("status", currentLobbyStatus);
                } else {
                    item.addProperty("status", "INACTIVE");
                }
                arr.add(item);
            }

            for (JsonObject customSc : customScenarios.values()) {
                JsonObject item = new JsonObject();
                String cId = customSc.has("id") ? customSc.get("id").getAsString() : "custom";
                String rawTitle = customSc.has("title") ? customSc.get("title").getAsString() : (customSc.has("name") ? customSc.get("name").getAsString() : cId);
                String author = customSc.has("author") ? customSc.get("author").getAsString() : "Custom";
                int version = customSc.has("version") ? customSc.get("version").getAsInt() : 1;
                String checksum = customSc.has("contentChecksum") ? customSc.get("contentChecksum").getAsString() : computeJsonChecksum(customSc);

                String displayTitle = rawTitle;
                if (titleCounts.getOrDefault(rawTitle, 0) > 1) {
                    displayTitle = String.format("%s (v%d - %s #%s)", rawTitle, version, author, checksum.substring(0, Math.min(4, checksum.length())));
                }

                item.addProperty("id", cId);
                item.addProperty("title", displayTitle);
                item.addProperty("description", customSc.has("description") ? customSc.get("description").getAsString() : "Scénario personnalisé stocké sur le serveur");
                item.addProperty("academicCategory", "Custom Server Scenarios");
                item.addProperty("biomeName", customSc.has("worldPresetId") ? customSc.get("worldPresetId").getAsString() : "TEMPERATE");
                item.addProperty("requiredPlayerCount", customSc.has("requiredPlayerCount") ? customSc.get("requiredPlayerCount").getAsInt() : 1);
                item.addProperty("isMultiplayerOnly", customSc.has("isMultiplayerOnly") && customSc.get("isMultiplayerOnly").getAsBoolean());
                item.addProperty("maxDurationValue", customSc.has("maxDuration") ? customSc.get("maxDuration").getAsDouble() : 100);
                item.addProperty("maxDurationUnit", customSc.has("durationUnit") ? customSc.get("durationUnit").getAsString() : "Days");
                item.addProperty("version", version);
                item.addProperty("author", author);
                item.addProperty("contentChecksum", checksum);
                item.addProperty("isBuiltIn", false);

                if (cId.equals(selectedScenarioId)) {
                    item.addProperty("status", currentLobbyStatus);
                } else {
                    item.addProperty("status", "INACTIVE");
                }
                arr.add(item);
            }
        } catch (Exception e) {
            LOG.warn("Error building scenario catalog: {}", e.getMessage());
        }

        resp.add("scenarios", arr);
        try {
            conn.send(resp.toString());
        } catch (Exception ignored) {}
    }

    public static void saveCustomScenario(JsonObject scenarioObj, Map<String, JsonObject> customScenarios,
                                          Path storageDir, Runnable onBroadcast) {
        if (scenarioObj == null) return;

        String rawId = scenarioObj.has("id") && !scenarioObj.get("id").getAsString().isEmpty()
                ? scenarioObj.get("id").getAsString()
                : null;
        String title = scenarioObj.has("title") ? scenarioObj.get("title").getAsString() : (scenarioObj.has("name") ? scenarioObj.get("name").getAsString() : "Custom Scenario");
        String author = scenarioObj.has("author") ? scenarioObj.get("author").getAsString() : "Custom";
        String checksum = computeJsonChecksum(scenarioObj);
        scenarioObj.addProperty("contentChecksum", checksum);
        scenarioObj.addProperty("author", author);

        boolean isBuiltInConflict = (rawId != null && (rawId.startsWith("ACAD_") || rawId.startsWith("MP_")));
        if (!isBuiltInConflict && rawId != null) {
            for (Scenario sc : AcademicScenarios.getAllAcademicScenarios(42L)) {
                if (sc.getId().equalsIgnoreCase(rawId)) {
                    isBuiltInConflict = true;
                    break;
                }
            }
        }

        String targetId = rawId;
        if (isBuiltInConflict) {
            targetId = "CUSTOM_FORK_" + (rawId != null ? rawId : "BUILTIN") + "_" + checksum.substring(0, Math.min(6, checksum.length()));
            scenarioObj.addProperty("title", "[Fork] " + title);
        } else if (targetId == null) {
            String slug = title.toLowerCase().replaceAll("[^a-z0-9]+", "_");
            targetId = "CUSTOM_" + slug + "_" + checksum.substring(0, Math.min(6, checksum.length()));
        }

        if (customScenarios.containsKey(targetId)) {
            JsonObject existing = customScenarios.get(targetId);
            String existingChecksum = existing.has("contentChecksum") ? existing.get("contentChecksum").getAsString() : computeJsonChecksum(existing);
            if (!existingChecksum.equals(checksum)) {
                boolean allowOverwrite = scenarioObj.has("overwrite") && scenarioObj.get("overwrite").getAsBoolean();
                if (allowOverwrite) {
                    int nextVer = existing.has("version") ? existing.get("version").getAsInt() + 1 : 2;
                    scenarioObj.addProperty("version", nextVer);
                } else {
                    int nextVer = existing.has("version") ? existing.get("version").getAsInt() + 1 : 2;
                    targetId = targetId + "_v" + nextVer + "_" + checksum.substring(0, Math.min(4, checksum.length()));
                    scenarioObj.addProperty("version", nextVer);
                    scenarioObj.addProperty("title", title + " (v" + nextVer + ")");
                }
            }
        } else {
            if (!scenarioObj.has("version")) {
                scenarioObj.addProperty("version", 1);
            }
        }

        scenarioObj.addProperty("id", targetId);
        scenarioObj.addProperty("revisionTimestamp", System.currentTimeMillis());
        scenarioObj.addProperty("isBuiltIn", false);

        customScenarios.put(targetId, scenarioObj);
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            Files.writeString(storageDir.resolve(targetId + ".json"), scenarioObj.toString());
            LOG.info("Persisted custom scenario to disk: {}.json (Checksum: {})", targetId, checksum);
        } catch (Exception e) {
            LOG.warn("Failed to persist custom scenario: {}", e.getMessage());
        }
        if (onBroadcast != null) {
            onBroadcast.run();
        }
    }

    public static void handleExportScenario(WebSocket conn, String scenarioId, Map<String, JsonObject> customScenarios) {
        if (conn == null || !conn.isOpen() || scenarioId == null) return;
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "SCENARIO_DATA");
        resp.addProperty("scenarioId", scenarioId);

        if (customScenarios.containsKey(scenarioId)) {
            resp.add("scenario", customScenarios.get(scenarioId));
        } else {
            List<Scenario> allScenarios = new ArrayList<>();
            allScenarios.addAll(AcademicScenarios.getAllAcademicScenarios(42L));
            allScenarios.addAll(AcademicScenarios.getAllMultiplayerScenarios(42L));

            Scenario target = allScenarios.stream()
                    .filter(s -> s.getId().equalsIgnoreCase(scenarioId))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                JsonObject scObj = new JsonObject();
                scObj.addProperty("id", target.getId());
                scObj.addProperty("title", target.getTitle());
                scObj.addProperty("description", target.getDescription());
                scObj.addProperty("academicCategory", target.getAcademicCategory());
                scObj.addProperty("masterSeed", target.getMasterSeed());
                scObj.addProperty("biomeName", target.getBiomeName());
                scObj.addProperty("isMultiplayerOnly", target.isMultiplayerOnly());
                scObj.addProperty("requiredPlayerCount", target.getRequiredPlayerCount());
                scObj.addProperty("maxDuration", target.getMaxDurationValue());
                scObj.addProperty("durationUnit", target.getMaxDurationUnit());

                JsonArray colArr = new JsonArray();
                for (var c : target.getColonies()) {
                    JsonObject colObj = new JsonObject();
                    colObj.addProperty("speciesName", c.speciesName());
                    colObj.addProperty("queens", c.queenCount());
                    colObj.addProperty("workers", c.workerCount());
                    colObj.addProperty("soldiers", c.soldierCount());
                    colArr.add(colObj);
                }
                scObj.add("colonies", colArr);
                resp.add("scenario", scObj);
            }
        }

        try {
            conn.send(resp.toString());
        } catch (Exception ignored) {}
    }

    public static void loadScenarioById(Simulation sim, String scenarioId) {
        if (sim == null || scenarioId == null) return;
        List<Scenario> allScenarios = new ArrayList<>();
        allScenarios.addAll(AcademicScenarios.getAllAcademicScenarios(42L));
        allScenarios.addAll(AcademicScenarios.getAllMultiplayerScenarios(42L));

        Scenario target = allScenarios.stream()
                .filter(s -> s.getId().equalsIgnoreCase(scenarioId))
                .findFirst()
                .orElse(null);

        if (target == null && !allScenarios.isEmpty()) {
            target = allScenarios.get(0);
        }

        if (target != null) {
            LOG.info("Loading Server Scenario: {} ({})", target.getTitle(), target.getId());
            sim.stop();
            sim.getTerrarium().clear();
            sim.reset(0);
            sim.setMasterSeed(target.getMasterSeed());

            if (target.getEmbeddedSpecies() != null && !target.getEmbeddedSpecies().isEmpty()) {
                for (org.swarmforge.core.species.DefaultSpecies customSp : target.getEmbeddedSpecies().values()) {
                    org.swarmforge.core.species.SpeciesRegistry.getInstance().register(customSp);
                }
            }

            org.swarmforge.core.world.TerrainGenerator terrainGen = new org.swarmforge.core.world.TerrainGenerator();
            int groundLevel = sim.getTerrarium().getDepth() - 10;
            float roughness = 8f;
            float scale = 0.03f;
            String biome = target.getBiomeName() != null ? target.getBiomeName().toUpperCase() : "TEMPERATE";
            if (biome.contains("DESERT") || biome.contains("STEPPE") || biome.contains("ARID")) {
                roughness = 4f;
                scale = 0.02f;
            } else if (biome.contains("ALPINE") || biome.contains("MOUNTAIN") || biome.contains("HILLS") || biome.contains("TAIGA")) {
                roughness = 14f;
                scale = 0.05f;
            } else if (biome.contains("WETLAND") || biome.contains("SAVANNA")) {
                roughness = 3f;
                scale = 0.01f;
            }
            terrainGen.generate(sim.getTerrarium(), groundLevel, roughness, scale);

            if (sim.getWeather() != null) {
                if (target.getEmbeddedClimateConfig() != null && !target.getEmbeddedClimateConfig().isEmpty()) {
                    Map<String, Object> climate = target.getEmbeddedClimateConfig();
                    if (climate.containsKey("temperatureMin") && climate.containsKey("temperatureMax")) {
                        double tMin = ((Number) climate.get("temperatureMin")).doubleValue();
                        double tMax = ((Number) climate.get("temperatureMax")).doubleValue();
                        sim.getWeather().setTemperature((float) ((tMin + tMax) / 2.0));
                    } else {
                        sim.getWeather().setTemperature(target.getInitialTemperature());
                    }
                    if (climate.containsKey("humidity")) {
                        sim.getWeather().setHumidity(((Number) climate.get("humidity")).floatValue());
                    } else {
                        sim.getWeather().setHumidity(target.getInitialHumidity() * 100f);
                    }
                } else {
                    sim.getWeather().setTemperature(target.getInitialTemperature());
                    sim.getWeather().setHumidity(target.getInitialHumidity() * 100f);
                }
            }

            int totalColonies = target.getColonies().size();
            for (int i = 0; i < totalColonies; i++) {
                var colSetup = target.getColonies().get(i);
                String spName = colSetup.speciesName();
                org.swarmforge.core.species.Species species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(spName);

                int posX = (totalColonies == 1) ? (sim.getTerrarium().getWidth() / 2) : (int) ((i + 1) * (sim.getTerrarium().getWidth() / (totalColonies + 1.0f)));
                int posY = (totalColonies == 1) ? (sim.getTerrarium().getHeight() / 2) : (int) ((i + 1) * (sim.getTerrarium().getHeight() / (totalColonies + 1.0f)));

                org.swarmforge.core.world.NestGenerator nestGen = new org.swarmforge.core.world.NestGenerator(sim.getTerrarium());
                nestGen.generate(posX, posY, groundLevel - 5, org.swarmforge.core.world.NestGenerator.NestType.MATURE, 1.0f);

                Colony colony = new Colony(species, posX, posY, groundLevel - 5);
                for (int q = 0; q < colSetup.queenCount(); q++) {
                    colony.addIndividual(colony.createQueen());
                }
                for (int w = 0; w < colSetup.workerCount(); w++) {
                    Individual worker = colony.createWorker();
                    worker.setPosition(posX, posY, groundLevel - 2);
                    colony.addIndividual(worker);
                }
                for (int s = 0; s < colSetup.soldierCount(); s++) {
                    Individual soldier = colony.createSoldier();
                    soldier.setPosition(posX, posY, groundLevel - 2);
                    colony.addIndividual(soldier);
                }

                sim.addColony(colony);
            }

            int foodCount = Math.max(10, target.getFoodPatchesCount());
            Random rand = new Random(target.getMasterSeed());
            for (int f = 0; f < foodCount; f++) {
                float fx = 10 + rand.nextFloat() * (sim.getTerrarium().getWidth() - 20);
                float fy = 10 + rand.nextFloat() * (sim.getTerrarium().getHeight() - 20);
                sim.spawnFood(fx, fy, groundLevel, 15 + rand.nextFloat() * 30, org.swarmforge.core.domain.ResourceType.SUGAR);
            }
        }
    }
}
