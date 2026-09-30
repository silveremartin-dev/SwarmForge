package org.swarmforge.server.net;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.util.JsonFormat;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.protocol.grpc.IndividualDelta;
import org.swarmforge.protocol.grpc.SimulationUpdate;
import org.swarmforge.protocol.grpc.Vec3;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server for streaming simulation state to web clients.
 * Supports full bidirectional protocol:
 * - Streaming SimulationUpdate frames
 * - Scenario Deployment (Client -> Server: Host Mode)
 * - Session Joining & Colony Assignment (Server -> Client: Join Mode)
 * - Remote Transport Controls (Play, Pause, Step, Speed)
 * - God Mode remote interventions & Event Scheduling
 */
public class SwarmForgeWebSocketServer extends WebSocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(SwarmForgeWebSocketServer.class);
    private static final Gson GSON = new Gson();
    private final org.swarmforge.server.simulation.SimulationManager simulationManager;
    private final Map<WebSocket, String> clientSubscriptions = new ConcurrentHashMap<>();
    private final Map<WebSocket, java.util.concurrent.atomic.AtomicInteger> clientMessageCounters = new ConcurrentHashMap<>();
    private final Map<WebSocket, Long> clientWindowTimestamps = new ConcurrentHashMap<>();
    private final Map<String, JsonObject> activeScenarios = new ConcurrentHashMap<>();
    private final Map<String, JsonObject> customScenarios = new ConcurrentHashMap<>();
    private final java.nio.file.Path scenarioStorageDir = java.nio.file.Paths.get("scenarios");

    public SwarmForgeWebSocketServer(int port, org.swarmforge.server.simulation.SimulationManager simulationManager) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        this.simulationManager = simulationManager;
        initScenarioStorage();
    }

    private void initScenarioStorage() {
        try {
            if (!java.nio.file.Files.exists(scenarioStorageDir)) {
                java.nio.file.Files.createDirectories(scenarioStorageDir);
            }
            try (var stream = java.nio.file.Files.list(scenarioStorageDir)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    try {
                        String content = java.nio.file.Files.readString(p);
                        JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                        String id = obj.has("id") ? obj.get("id").getAsString() : p.getFileName().toString().replace(".json", "");
                        obj.addProperty("id", id);
                        customScenarios.put(id, obj);
                        LOG.info("Loaded custom server scenario: {}", id);
                    } catch (Exception e) {
                        LOG.warn("Failed to load scenario file {}: {}", p, e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOG.warn("Could not initialize scenario storage directory: {}", e.getMessage());
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOG.info("New WebSocket connection: " + conn.getRemoteSocketAddress());
        clientSubscriptions.put(conn, "main");
        clientMessageCounters.put(conn, new java.util.concurrent.atomic.AtomicInteger(0));
        clientWindowTimestamps.put(conn, System.currentTimeMillis());
        conn.send("{\"type\": \"WELCOME\", \"message\": \"Connected to SwarmForge Server\"}");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOG.info("Closed WebSocket connection: " + conn.getRemoteSocketAddress());
        clientSubscriptions.remove(conn);
        clientMessageCounters.remove(conn);
        clientWindowTimestamps.remove(conn);
        JsonObject removedPlayer = connectedPlayers.remove(conn);
        if (removedPlayer != null) {
            broadcastLobbyState();
            String pTag = removedPlayer.has("tag") ? removedPlayer.get("tag").getAsString() : "Joueur";
            broadcastEventLog("INFO", "PLAYER_LEFT", "Lobby Serveur",
                    "Participant déconnecté : " + pTag);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message == null || message.length() > 1048576) { // 1MB max for full scenario payload
            conn.close(1009, "Payload too large (>1MB)");
            return;
        }

        // Rate Limiter: Max 150 messages per 1-second sliding window per connection
        long now = System.currentTimeMillis();
        long windowStart = clientWindowTimestamps.computeIfAbsent(conn, k -> now);
        if (now - windowStart > 1000) {
            clientWindowTimestamps.put(conn, now);
            clientMessageCounters.computeIfAbsent(conn, k -> new java.util.concurrent.atomic.AtomicInteger(0)).set(0);
        }
        var counter = clientMessageCounters.computeIfAbsent(conn, k -> new java.util.concurrent.atomic.AtomicInteger(0));
        if (counter.incrementAndGet() > 150) {
            conn.close(1008, "Rate limit exceeded (Max 150 msg/sec)");
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type.toUpperCase()) {
                case "PING" -> {
                    JsonObject pong = new JsonObject();
                    pong.addProperty("type", "PONG");
                    pong.addProperty("timestamp", System.currentTimeMillis());
                    conn.send(pong.toString());
                }
                case "SUBSCRIBE" -> {
                    String simId = json.has("simulationId") ? json.get("simulationId").getAsString() : "main";
                    if (simId == null || simId.trim().isEmpty()) simId = "main";
                    clientSubscriptions.put(conn, simId);

                    String tag = json.has("participantTag") ? json.get("participantTag").getAsString() : "Joueur_" + (connectedPlayers.size() + 1);
                    String species = json.has("species") ? json.get("species").getAsString() : "Formica fusca";
                    String role = json.has("role") ? json.get("role").getAsString() : "JOIN";

                    JsonObject player = new JsonObject();
                    player.addProperty("tag", tag);
                    player.addProperty("species", species);
                    player.addProperty("role", role);
                    player.addProperty("isReady", "HOST".equalsIgnoreCase(role));
                    player.addProperty("joinedAt", System.currentTimeMillis());
                    connectedPlayers.put(conn, player);

                    sendScenarioState(conn, simId);
                    sendServerScenarios(conn);
                    broadcastLobbyState();
                }
                case "LIST_SERVER_SCENARIOS", "GET_SCENARIOS" -> {
                    sendServerScenarios(conn);
                }
                case "GET_SCENARIO", "EXPORT_SCENARIO" -> {
                    String scenarioId = json.has("scenarioId") ? json.get("scenarioId").getAsString() : selectedScenarioId;
                    handleExportScenario(conn, scenarioId);
                }
                case "SAVE_SCENARIO", "CREATE_SCENARIO" -> {
                    JsonObject scObj = json.has("scenario") ? json.getAsJsonObject("scenario") : json;
                    saveCustomScenario(scObj);
                    sendServerScenarios(conn);
                }
                case "DEPLOY_SCENARIO" -> {
                    handleDeployScenario(conn, json);
                }
                case "JOIN_SESSION" -> {
                    handleJoinSession(conn, json);
                }
                case "PLAYER_READY" -> {
                    handlePlayerReady(conn, json);
                }
                case "START_MATCH" -> {
                    handleStartMatch(conn, json);
                }
                case "SELECT_SCENARIO" -> {
                    handleSelectScenario(conn, json);
                }
                case "CONTROL_COMMAND", "CONTROL" -> {
                    handleControlCommand(conn, json);
                }
                case "GOD_MODE_INTERVENTION" -> {
                    handleGodModeIntervention(conn, json);
                }
                case "SCHEDULE_EVENT" -> {
                    handleScheduleEvent(conn, json);
                }
                default -> {
                    LOG.debug("Unhandled WebSocket message type: {}", type);
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse WebSocket message from {}: {}", conn.getRemoteSocketAddress(), e.getMessage());
        }
    }

    private String currentLobbyStatus = "LOBBY_WAITING"; // LOBBY_WAITING, ACTIVE, INACTIVE
    private String selectedScenarioId = "ACAD_01_LEVY_BROWNIAN";
    private final Map<WebSocket, JsonObject> connectedPlayers = new ConcurrentHashMap<>();

    private void handleJoinSession(WebSocket conn, JsonObject json) {
        String simId = json.has("simulationId") ? json.get("simulationId").getAsString() : "main";
        if (simId == null || simId.trim().isEmpty()) simId = "main";
        clientSubscriptions.put(conn, simId);

        String tag = json.has("participantTag") ? json.get("participantTag").getAsString() : "Joueur_" + (connectedPlayers.size() + 1);
        String species = json.has("species") ? json.get("species").getAsString() : "Formica fusca";
        String role = json.has("role") ? json.get("role").getAsString() : "JOIN";

        JsonObject player = new JsonObject();
        player.addProperty("tag", tag);
        player.addProperty("species", species);
        player.addProperty("role", role);
        player.addProperty("isReady", false);
        player.addProperty("joinedAt", System.currentTimeMillis());
        connectedPlayers.put(conn, player);

        sendScenarioState(conn, simId);
        sendServerScenarios(conn);
        broadcastLobbyState();
        broadcastEventLog("INFO", "PLAYER_JOINED", "Lobby Serveur",
                "Nouveau participant connecté : " + tag + " (" + species + ").");
    }

    private void handlePlayerReady(WebSocket conn, JsonObject json) {
        JsonObject player = connectedPlayers.get(conn);
        if (player != null) {
            boolean ready = json.has("ready") ? json.get("ready").getAsBoolean() : true;
            player.addProperty("isReady", ready);
            broadcastLobbyState();
        }
    }

    private void handleStartMatch(WebSocket conn, JsonObject json) {
        if ("ACTIVE".equals(currentLobbyStatus)) {
            LOG.warn("Match is already ACTIVE. Ignoring START_MATCH.");
            return;
        }

        currentLobbyStatus = "ACTIVE";
        Simulation sim = simulationManager.getSimulation("main").orElse(null);
        if (sim != null) {
            if (sim.getColonies().isEmpty()) {
                loadScenarioById(sim, selectedScenarioId);
            }
            sim.start();
        }
        broadcastLobbyState();
        broadcastServerScenarios();
        broadcastEventLog("WARNING", "MATCH_STARTED", "SwarmForge Server",
                "La partie multijoueur vient de démarrer ! Simulation active.");
    }

    private void handleSelectScenario(WebSocket conn, JsonObject json) {
        if ("ACTIVE".equals(currentLobbyStatus)) {
            LOG.warn("Cannot change scenario while a match is ACTIVE.");
            return;
        }
        if (json.has("scenarioId")) {
            selectedScenarioId = json.get("scenarioId").getAsString();
            Simulation sim = simulationManager.getSimulation("main").orElse(null);
            if (sim != null) {
                loadScenarioById(sim, selectedScenarioId);
                sendScenarioState(conn, "main");
            }
            broadcastLobbyState();
            broadcastServerScenarios();
        }
    }

    public void loadScenarioById(Simulation sim, String scenarioId) {
        if (sim == null || scenarioId == null) return;
        List<org.swarmforge.core.scenario.Scenario> allScenarios = new java.util.ArrayList<>();
        allScenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(42L));
        allScenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllMultiplayerScenarios(42L));

        org.swarmforge.core.scenario.Scenario target = allScenarios.stream()
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

            // 0. Register embedded self-contained species
            if (target.getEmbeddedSpecies() != null && !target.getEmbeddedSpecies().isEmpty()) {
                for (org.swarmforge.core.species.CustomSpecies customSp : target.getEmbeddedSpecies().values()) {
                    org.swarmforge.core.species.SpeciesRegistry.getInstance().register(customSp);
                }
            }

            // 1. Biome Terrain
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

            // 2. Weather & Climate
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

            // 3. Colonies & Individuals
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

            // 4. Food Patches
            int foodCount = Math.max(10, target.getFoodPatchesCount());
            java.util.Random rand = new java.util.Random(target.getMasterSeed());
            for (int f = 0; f < foodCount; f++) {
                float fx = 10 + rand.nextFloat() * (sim.getTerrarium().getWidth() - 20);
                float fy = 10 + rand.nextFloat() * (sim.getTerrarium().getHeight() - 20);
                sim.spawnFood(fx, fy, groundLevel, 15 + rand.nextFloat() * 30, org.swarmforge.core.domain.ResourceType.SUGAR);
            }
        }
    }

    private String computeJsonChecksum(JsonObject json) {
        if (json == null) return "00000000";
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
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
            byte[] hash = md.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString().substring(0, 12);
        } catch (Exception e) {
            return "00000000";
        }
    }

    public void sendServerScenarios(WebSocket conn) {
        if (conn == null || !conn.isOpen()) return;
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "SERVER_SCENARIOS_LIST");
        JsonArray arr = new JsonArray();

        try {
            List<org.swarmforge.core.scenario.Scenario> builtInScenarios = new java.util.ArrayList<>();
            builtInScenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(42L));
            builtInScenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllMultiplayerScenarios(42L));

            // Map for tracking duplicate titles across all scenarios
            Map<String, Integer> titleCounts = new java.util.HashMap<>();
            for (org.swarmforge.core.scenario.Scenario sc : builtInScenarios) {
                titleCounts.put(sc.getTitle(), titleCounts.getOrDefault(sc.getTitle(), 0) + 1);
            }
            for (JsonObject customSc : customScenarios.values()) {
                String t = customSc.has("title") ? customSc.get("title").getAsString() : (customSc.has("name") ? customSc.get("name").getAsString() : "Custom");
                titleCounts.put(t, titleCounts.getOrDefault(t, 0) + 1);
            }

            for (org.swarmforge.core.scenario.Scenario sc : builtInScenarios) {
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

                // Status calculation
                if (sc.getId().equals(selectedScenarioId)) {
                    item.addProperty("status", currentLobbyStatus);
                } else {
                    item.addProperty("status", "INACTIVE");
                }
                arr.add(item);
            }

            // Include persisted custom server scenarios
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

    public void saveCustomScenario(JsonObject scenarioObj) {
        if (scenarioObj == null) return;

        String rawId = scenarioObj.has("id") && !scenarioObj.get("id").getAsString().isEmpty()
                ? scenarioObj.get("id").getAsString()
                : null;
        String title = scenarioObj.has("title") ? scenarioObj.get("title").getAsString() : (scenarioObj.has("name") ? scenarioObj.get("name").getAsString() : "Custom Scenario");
        String author = scenarioObj.has("author") ? scenarioObj.get("author").getAsString() : "Custom";
        String checksum = computeJsonChecksum(scenarioObj);
        scenarioObj.addProperty("contentChecksum", checksum);
        scenarioObj.addProperty("author", author);

        // Protect built-in academic and multiplayer IDs
        boolean isBuiltInConflict = (rawId != null && (rawId.startsWith("ACAD_") || rawId.startsWith("MP_")));
        if (!isBuiltInConflict && rawId != null) {
            for (org.swarmforge.core.scenario.Scenario sc : org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(42L)) {
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

        // Check for collision with existing custom scenario
        if (customScenarios.containsKey(targetId)) {
            JsonObject existing = customScenarios.get(targetId);
            String existingChecksum = existing.has("contentChecksum") ? existing.get("contentChecksum").getAsString() : computeJsonChecksum(existing);
            if (!existingChecksum.equals(checksum)) {
                // Different content! Check if overwrite is requested
                boolean allowOverwrite = scenarioObj.has("overwrite") && scenarioObj.get("overwrite").getAsBoolean();
                if (allowOverwrite) {
                    int nextVer = existing.has("version") ? existing.get("version").getAsInt() + 1 : 2;
                    scenarioObj.addProperty("version", nextVer);
                } else {
                    // Disambiguate without destructive overwrite
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
            if (!java.nio.file.Files.exists(scenarioStorageDir)) {
                java.nio.file.Files.createDirectories(scenarioStorageDir);
            }
            java.nio.file.Files.writeString(scenarioStorageDir.resolve(targetId + ".json"), scenarioObj.toString());
            LOG.info("Persisted custom scenario to disk: {}.json (Checksum: {})", targetId, checksum);
        } catch (Exception e) {
            LOG.warn("Failed to persist custom scenario: {}", e.getMessage());
        }
        broadcastServerScenarios();
    }

    private void handleExportScenario(WebSocket conn, String scenarioId) {
        if (conn == null || !conn.isOpen() || scenarioId == null) return;
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "SCENARIO_DATA");
        resp.addProperty("scenarioId", scenarioId);

        if (customScenarios.containsKey(scenarioId)) {
            resp.add("scenario", customScenarios.get(scenarioId));
        } else {
            // Find in Academic / Multiplayer scenarios
            List<org.swarmforge.core.scenario.Scenario> allScenarios = new java.util.ArrayList<>();
            allScenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(42L));
            allScenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllMultiplayerScenarios(42L));

            org.swarmforge.core.scenario.Scenario target = allScenarios.stream()
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

    public void broadcastServerScenarios() {
        for (WebSocket conn : getConnections()) {
            if (conn.isOpen()) {
                sendServerScenarios(conn);
            }
        }
    }

    public void broadcastLobbyState() {
        JsonObject lobby = new JsonObject();
        lobby.addProperty("type", "LOBBY_STATE");
        lobby.addProperty("status", currentLobbyStatus);
        lobby.addProperty("selectedScenarioId", selectedScenarioId);
        
        JsonArray playersArr = new JsonArray();
        for (Map.Entry<WebSocket, JsonObject> entry : connectedPlayers.entrySet()) {
            if (entry.getKey().isOpen()) {
                playersArr.add(entry.getValue());
            }
        }
        lobby.add("players", playersArr);
        lobby.addProperty("playerCount", playersArr.size());

        String json = lobby.toString();
        for (WebSocket conn : getConnections()) {
            if (conn.isOpen()) {
                try {
                    conn.send(json);
                } catch (Exception ignored) {}
            }
        }
    }

    private void handleDeployScenario(WebSocket conn, JsonObject json) {
        String simId = json.has("simulationId") ? json.get("simulationId").getAsString() : "main";
        if (simId == null || simId.trim().isEmpty()) simId = "main";
        clientSubscriptions.put(conn, simId);

        JsonObject scenarioObj = json.has("scenario") ? json.getAsJsonObject("scenario") : json;
        activeScenarios.put(simId, scenarioObj);
        saveCustomScenario(scenarioObj);

        Simulation sim = simulationManager.getSimulation(simId).orElse(null);
        if (sim != null) {
            sim.stop();
            if (scenarioObj.has("masterSeed")) {
                sim.setMasterSeed(scenarioObj.get("masterSeed").getAsLong());
            }
            if (scenarioObj.has("stepSeconds")) {
                double stepSec = scenarioObj.get("stepSeconds").getAsDouble();
                if (stepSec > 0) {
                    sim.setTicksPerSecond(Math.max(1, (int) Math.round(1.0 / stepSec)));
                }
            }
            sim.reset(0);
            sim.start();
        }

        LOG.info("Master Scenario deployed on simulation '{}' by client {}", simId, conn.getRemoteSocketAddress());

        // Broadcast updated scenario state and event log to all clients
        broadcastScenarioState(simId);
        broadcastEventLog("INFO", "SCENARIO_DEPLOYED", "SwarmForge Server",
                "Scénario maître déployé avec succès par l'hôte distant. Simulation initialisée.");
    }

    private void handleControlCommand(WebSocket conn, JsonObject json) {
        String simId = clientSubscriptions.getOrDefault(conn, "main");
        Simulation sim = simulationManager.getSimulation(simId).orElse(null);
        if (sim == null) return;

        String action = json.has("action") ? json.get("action").getAsString().toUpperCase() : "";
        switch (action) {
            case "PLAY", "START" -> {
                sim.start();
                LOG.info("Simulation '{}' started via remote control", simId);
            }
            case "PAUSE" -> {
                sim.pause();
                LOG.info("Simulation '{}' paused via remote control", simId);
            }
            case "STEP" -> {
                sim.tick();
                LOG.info("Simulation '{}' stepped 1 tick via remote control", simId);
            }
            case "SET_SPEED", "SPEED" -> {
                double spd = json.has("value") ? json.get("value").getAsDouble() : (json.has("speed") ? json.get("speed").getAsDouble() : 1.0);
                int tps = (int) Math.max(1, Math.round(20.0 * spd));
                sim.setTicksPerSecond(tps);
                LOG.info("Simulation '{}' speed set to {}x ({} TPS)", simId, spd, tps);
            }
            case "RESET" -> {
                sim.stop();
                sim.reset(0);
                LOG.info("Simulation '{}' reset via remote control", simId);
            }
        }
    }

    private void handleGodModeIntervention(WebSocket conn, JsonObject json) {
        String simId = clientSubscriptions.getOrDefault(conn, "main");
        JsonObject intervention = json.has("intervention") ? json.getAsJsonObject("intervention") : json;
        String category = intervention.has("category") ? intervention.get("category").getAsString() : "";
        String type = intervention.has("type") ? intervention.get("type").getAsString() : "";

        LOG.info("God Mode Intervention received on '{}': [{}] {}", simId, category, type);

        broadcastEventLog("WARNING", "GOD_MODE_INTERVENTION", "Mode Divin (Serveur)",
                "Intervention divine exécutée : [" + category + "] " + type);
    }

    private void handleScheduleEvent(WebSocket conn, JsonObject json) {
        String simId = clientSubscriptions.getOrDefault(conn, "main");
        JsonObject evt = json.has("scheduledEvent") ? json.getAsJsonObject("scheduledEvent") : json;
        long targetTick = evt.has("targetTick") ? evt.get("targetTick").getAsLong() : 0;
        String desc = evt.has("description") ? evt.get("description").getAsString() : "Événement planifié";

        LOG.info("Event scheduled on '{}' at tick {}: {}", simId, targetTick, desc);
    }

    public void sendScenarioState(WebSocket conn, String simId) {
        if (conn == null || !conn.isOpen()) return;
        JsonObject stateMsg = new JsonObject();
        stateMsg.addProperty("type", "SCENARIO_STATE");
        stateMsg.addProperty("simulationId", simId);

        JsonObject scenarioObj = activeScenarios.get(simId);
        if (scenarioObj != null) {
            stateMsg.add("scenario", scenarioObj);
        } else {
            // Default server scenario descriptor
            JsonObject defaultScenario = new JsonObject();
            defaultScenario.addProperty("title", "Scénario Serveur Maître");
            defaultScenario.addProperty("worldPresetId", "world_terrarium_01");
            defaultScenario.addProperty("weatherPresetId", "weather_printemps_doux");
            defaultScenario.addProperty("masterSeed", 12345);
            defaultScenario.addProperty("stepSeconds", 0.05);
            stateMsg.add("scenario", defaultScenario);
        }

        try {
            conn.send(stateMsg.toString());
        } catch (Exception e) {
            LOG.warn("Failed to send scenario state: {}", e.getMessage());
        }
    }

    public void broadcastScenarioState(String simId) {
        for (WebSocket conn : getConnections()) {
            if (simId.equals(clientSubscriptions.get(conn))) {
                sendScenarioState(conn, simId);
            }
        }
    }

    public void broadcastEventLog(String severity, String type, String source, String message) {
        JsonObject logMsg = new JsonObject();
        logMsg.addProperty("type", "EVENT_LOG");
        JsonObject evt = new JsonObject();
        evt.addProperty("id", "evt_srv_" + System.currentTimeMillis());
        evt.addProperty("severity", severity);
        evt.addProperty("type", type);
        evt.addProperty("source", source);
        evt.addProperty("message", message);
        evt.addProperty("timestamp", System.currentTimeMillis());
        logMsg.add("eventLog", evt);

        String json = logMsg.toString();
        for (WebSocket conn : getConnections()) {
            if (conn.isOpen()) {
                try {
                    conn.send(json);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOG.error("WebSocket error", ex);
    }

    @Override
    public void onStart() {
        LOG.info("WebSocket Server started on port: " + getPort());
        startBroadcaster();
    }

    private void startBroadcaster() {
        Thread.ofVirtual().name("ws-broadcaster").start(() -> {
            JsonFormat.Printer printer = JsonFormat.printer().includingDefaultValueFields();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    broadcastUpdates(printer);
                    Thread.sleep(50); // 20 FPS
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOG.error("Broadcast error", e);
                }
            }
        });
    }

    private void broadcastUpdates(JsonFormat.Printer printer) {
        var connections = getConnections();
        if (connections.isEmpty())
            return;

        // Cache serialized JSON per simulation ID to avoid redundant formatting
        Map<String, String> cachedJsonPerSim = new HashMap<>();

        for (WebSocket conn : connections) {
            String simId = clientSubscriptions.getOrDefault(conn, "main");

            String json = cachedJsonPerSim.computeIfAbsent(simId, id -> {
                Simulation sim = simulationManager.getSimulation(id).orElse(null);
                if (sim != null && sim.getState() == Simulation.State.RUNNING) {
                    try {
                        SimulationUpdate.Builder update = SimulationUpdate.newBuilder()
                                .setTick(sim.getTickCount());

                        int count = 0;
                        for (Colony colony : sim.getColonies()) {
                            if (count < 1000) {
                                for (Individual ind : colony.getLivingIndividuals()) {
                                    if (count++ >= 1000)
                                        break;

                                    update.addIndividuals(IndividualDelta.newBuilder()
                                            .setId(ind.getId().toString())
                                            .setPosition(Vec3.newBuilder()
                                                    .setX(ind.getX())
                                                    .setY(ind.getY())
                                                    .setZ(ind.getZ())
                                                    .build())
                                            .setHeading(ind.getHeading())
                                            .setAlive(ind.isAlive())
                                            .setHealth((float) ind.getHealth())
                                            .setEnergy((float) ind.getEnergy())
                                            .setJob(ind.getJob() != null ? ind.getJob().name() : (ind.getCaste() != null ? ind.getCaste().name() : "WORKER"))
                                            .setCurrentAction(ind.getCaste() != null ? ind.getCaste().name() : "WORKER")
                                            .build());
                                }
                            }

                            // Add Nest Structure
                            org.swarmforge.core.structure.Nest nest = colony.getNest();
                            org.swarmforge.protocol.grpc.NestStructure.Builder nestBuilder = org.swarmforge.protocol.grpc.NestStructure
                                    .newBuilder()
                                    .setId(colony.getId().toString());

                            for (org.swarmforge.core.structure.Chamber chamber : nest.getChambers()) {
                                nestBuilder.addChambers(org.swarmforge.protocol.grpc.ChamberInfo.newBuilder()
                                        .setId(chamber.getId())
                                        .setType(chamber.getType().name())
                                        .setPosition(Vec3.newBuilder().setX(chamber.getX()).setY(chamber.getY())
                                                .setZ(chamber.getZ()).build())
                                        .setCapacity(chamber.getCapacity())
                                        .setCurrentLoading(chamber.getCurrentLoad())
                                        .build());
                            }

                            for (org.swarmforge.core.structure.Tunnel tunnel : nest.getTunnels()) {
                                nestBuilder.addTunnels(org.swarmforge.protocol.grpc.TunnelInfo.newBuilder()
                                        .setStartChamberId(tunnel.getStart().getId())
                                        .setEndChamberId(tunnel.getEnd().getId())
                                        .setLength(tunnel.getLength())
                                        .build());
                            }

                            update.addNests(nestBuilder);
                        }

                        // Add Food Sources
                        for (org.swarmforge.core.domain.FoodSource food : sim.getFoodSources()) {
                            if (!food.isDepleted()) {
                                update.addFood(org.swarmforge.protocol.grpc.FoodSourceInfo.newBuilder()
                                        .setPosition(Vec3.newBuilder().setX(food.getX()).setY(food.getY()).setZ(food.getZ()).build())
                                        .setQuantity(food.getQuantity())
                                        .setType(food.getType() != null ? food.getType().name() : "SUGAR")
                                        .build());
                            }
                        }

                        // Environment
                        org.swarmforge.core.world.DayNightCycle cycle = sim.getDayNightCycle();
                        org.swarmforge.core.world.WeatherSystem weather = sim.getWeather();
                        org.swarmforge.core.world.SeasonManager seasons = sim.getSeasonManager();

                        update.setEnvironment(org.swarmforge.protocol.grpc.Environment.newBuilder()
                                .setLightLevel(cycle.getLightLevel())
                                .setTimeOfDay(cycle.getTimeOfDay().name())
                                .setSunAngle(cycle.getSunAngle())
                                .setTemperature(weather.getTemperature())
                                .setHumidity(weather.getHumidity())
                                .setRainIntensity(weather.getRainfall())
                                .setWindSpeed(weather.getWindSpeed())
                                .setSeason(seasons.getCurrentSeason().name())
                                .build());

                        String protoJson = printer.print(update.build());
                        JsonObject fullUpdate = JsonParser.parseString(protoJson).getAsJsonObject();

                        // Add live predators
                        JsonArray predArray = new JsonArray();
                        if (sim.getPredatorManager() != null) {
                            for (org.swarmforge.core.domain.Predator pred : sim.getPredatorManager().getPredators()) {
                                if (pred.isAlive()) {
                                    JsonObject pObj = new JsonObject();
                                    pObj.addProperty("id", pred.getId().toString());
                                    pObj.addProperty("type", pred.getType() != null ? pred.getType().name() : "SPIDER");
                                    pObj.addProperty("x", pred.getX());
                                    pObj.addProperty("y", pred.getY());
                                    pObj.addProperty("z", pred.getZ());
                                    pObj.addProperty("state", pred.getState() != null ? pred.getState().name() : "HUNTING");
                                    pObj.addProperty("health", pred.getHealth());
                                    predArray.add(pObj);
                                }
                            }
                        }
                        fullUpdate.add("predators", predArray);

                        // Add terrain dimensions
                        JsonObject terrainObj = new JsonObject();
                        terrainObj.addProperty("width", sim.getTerrarium().getWidth());
                        terrainObj.addProperty("height", sim.getTerrarium().getHeight());
                        terrainObj.addProperty("depth", sim.getTerrarium().getDepth());
                        fullUpdate.add("terrain", terrainObj);

                        return fullUpdate.toString();
                    } catch (Exception e) {
                        LOG.warn("Error serializing simulation update for " + id + ": " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            if (json != null && conn.isOpen()) {
                try {
                    conn.send(json);
                } catch (Exception e) {
                    // ignore connection drop
                }
            }
        }
    }
}
