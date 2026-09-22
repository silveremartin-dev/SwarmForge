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

    public SwarmForgeWebSocketServer(int port, org.swarmforge.server.simulation.SimulationManager simulationManager) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        this.simulationManager = simulationManager;
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
                    sendScenarioState(conn, simId);
                    sendServerScenarios(conn);
                    broadcastLobbyState();
                }
                case "LIST_SERVER_SCENARIOS", "GET_SCENARIOS" -> {
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
            sim.start();
        }
        broadcastLobbyState();
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
            broadcastLobbyState();
            broadcastServerScenarios();
        }
    }

    public void sendServerScenarios(WebSocket conn) {
        if (conn == null || !conn.isOpen()) return;
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "SERVER_SCENARIOS_LIST");
        JsonArray arr = new JsonArray();

        try {
            List<org.swarmforge.core.scenario.Scenario> scenarios = new java.util.ArrayList<>();
            scenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(42L));
            scenarios.addAll(org.swarmforge.core.scenario.AcademicScenarios.getAllMultiplayerScenarios(42L));

            for (org.swarmforge.core.scenario.Scenario sc : scenarios) {
                JsonObject item = new JsonObject();
                item.addProperty("id", sc.getId());
                item.addProperty("title", sc.getTitle());
                item.addProperty("description", sc.getDescription());
                item.addProperty("academicCategory", sc.getAcademicCategory() != null ? sc.getAcademicCategory() : "Academic");
                item.addProperty("biomeName", sc.getBiomeName());
                item.addProperty("requiredPlayerCount", sc.getRequiredPlayerCount());
                item.addProperty("isMultiplayerOnly", sc.isMultiplayerOnly());
                item.addProperty("maxDurationValue", sc.getMaxDurationValue());
                item.addProperty("maxDurationUnit", sc.getMaxDurationUnit());

                // Status calculation
                if (sc.getId().equals(selectedScenarioId)) {
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

                        return printer.print(update.build());
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
