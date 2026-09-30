package org.swarmforge.server.net;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.server.simulation.SimulationManager;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketScenarioStreamingTest {

    private SwarmForgeWebSocketServer wsServer;
    private SimulationManager simManager;
    private Simulation mainSim;
    private WebSocketClient client;
    private final BlockingQueue<JsonObject> receivedMessages = new LinkedBlockingQueue<>();
    private int port;

    @BeforeEach
    public void setUp() throws Exception {
        receivedMessages.clear();
        try (java.net.ServerSocket ss = new java.net.ServerSocket(0)) {
            port = ss.getLocalPort();
        }

        simManager = new SimulationManager();
        simManager.createSimulation("main", "Main Simulation", 100, 100, 30);
        mainSim = simManager.getSimulation("main").orElseThrow();

        wsServer = new SwarmForgeWebSocketServer(port, simManager);
        wsServer.loadScenarioById(mainSim, "ACAD_01_LEVY_BROWNIAN");
        wsServer.start();

        // Connect WebSocket client
        URI serverUri = new URI("ws://localhost:" + port);
        client = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
            }

            @Override
            public void onMessage(String message) {
                try {
                    JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                    receivedMessages.offer(json);
                } catch (Exception ignored) {}
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onError(Exception ex) {
            }
        };

        boolean connected = client.connectBlocking(5, TimeUnit.SECONDS);
        assertTrue(connected, "WebSocket client should connect to server");
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (client != null && client.isOpen()) {
            client.closeBlocking();
        }
        if (wsServer != null) {
            wsServer.stop(1000);
        }
        if (simManager != null) {
            simManager.removeSimulation("main");
        }
        Thread.sleep(100);
    }

    @Test
    @DisplayName("Verify WELCOME handshake and SUBSCRIBE responses (ScenarioState, ScenariosList, LobbyState)")
    public void testWelcomeAndSubscribe() throws Exception {
        // 1. First message upon open is WELCOME
        JsonObject welcome = receivedMessages.poll(3, TimeUnit.SECONDS);
        assertNotNull(welcome);
        assertEquals("WELCOME", welcome.get("type").getAsString());

        // 2. Send SUBSCRIBE
        JsonObject sub = new JsonObject();
        sub.addProperty("type", "SUBSCRIBE");
        sub.addProperty("simulationId", "main");
        sub.addProperty("participantTag", "Alice_Test");
        sub.addProperty("species", "Formica fusca");
        sub.addProperty("role", "HOST");
        client.send(sub.toString());

        // We should receive SCENARIO_STATE, SERVER_SCENARIOS_LIST, and LOBBY_STATE
        boolean gotScenarioState = false;
        boolean gotScenariosList = false;
        boolean gotLobbyState = false;

        for (int i = 0; i < 5; i++) {
            JsonObject msg = receivedMessages.poll(3, TimeUnit.SECONDS);
            if (msg == null) break;
            String type = msg.has("type") ? msg.get("type").getAsString() : "";
            if ("SCENARIO_STATE".equals(type)) gotScenarioState = true;
            if ("SERVER_SCENARIOS_LIST".equals(type)) {
                gotScenariosList = true;
                assertTrue(msg.getAsJsonArray("scenarios").size() >= 19, "Catalog should have at least 19 scenarios");
            }
            if ("LOBBY_STATE".equals(type)) {
                gotLobbyState = true;
                assertEquals(1, msg.get("playerCount").getAsInt());
            }
        }

        assertTrue(gotScenarioState, "Client should receive SCENARIO_STATE");
        assertTrue(gotScenariosList, "Client should receive SERVER_SCENARIOS_LIST");
        assertTrue(gotLobbyState, "Client should receive LOBBY_STATE");
    }

    @Test
    @DisplayName("Verify saving custom scenario on server and exporting it")
    public void testSaveAndExportScenario() throws Exception {
        // Consume welcome
        receivedMessages.poll(3, TimeUnit.SECONDS);

        // 1. Send SAVE_SCENARIO
        String customId = "CUSTOM_EXP_TEST_" + System.currentTimeMillis();
        JsonObject customSc = new JsonObject();
        customSc.addProperty("id", customId);
        customSc.addProperty("title", "Expérimentation Toxicité & Acide");
        customSc.addProperty("description", "Scénario sur mesure créé par utilisateur");
        customSc.addProperty("worldPresetId", "world_tropical_rainforest");
        customSc.addProperty("masterSeed", 99999);

        JsonObject saveReq = new JsonObject();
        saveReq.addProperty("type", "SAVE_SCENARIO");
        saveReq.add("scenario", customSc);
        client.send(saveReq.toString());

        // Should receive updated SERVER_SCENARIOS_LIST containing our custom scenario
        boolean foundCustom = false;
        for (int i = 0; i < 5; i++) {
            JsonObject msg = receivedMessages.poll(3, TimeUnit.SECONDS);
            if (msg == null) break;
            if ("SERVER_SCENARIOS_LIST".equals(msg.get("type").getAsString())) {
                var arr = msg.getAsJsonArray("scenarios");
                for (var el : arr) {
                    if (customId.equals(el.getAsJsonObject().get("id").getAsString())) {
                        foundCustom = true;
                        break;
                    }
                }
            }
            if (foundCustom) break;
        }
        assertTrue(foundCustom, "Custom scenario must be registered in server catalog");

        // 2. Request GET_SCENARIO / EXPORT_SCENARIO
        JsonObject exportReq = new JsonObject();
        exportReq.addProperty("type", "GET_SCENARIO");
        exportReq.addProperty("scenarioId", customId);
        client.send(exportReq.toString());

        JsonObject exportResp = null;
        for (int i = 0; i < 5; i++) {
            JsonObject msg = receivedMessages.poll(3, TimeUnit.SECONDS);
            if (msg == null) break;
            if ("SCENARIO_DATA".equals(msg.get("type").getAsString()) && customId.equals(msg.get("scenarioId").getAsString())) {
                exportResp = msg;
                break;
            }
        }
        assertNotNull(exportResp, "Server should return SCENARIO_DATA for exported scenario");
        assertEquals("Expérimentation Toxicité & Acide", exportResp.getAsJsonObject("scenario").get("title").getAsString());
    }

    @Test
    @DisplayName("Verify streaming of real individuals, food, predators and environment")
    public void testStreamingSimulationUpdates() throws Exception {
        // Start simulation to activate streaming broadcaster
        mainSim.start();

        // Subscribe client
        JsonObject sub = new JsonObject();
        sub.addProperty("type", "SUBSCRIBE");
        sub.addProperty("simulationId", "main");
        client.send(sub.toString());

        // Wait for a broadcast frame (tick > 0)
        JsonObject simUpdate = null;
        for (int i = 0; i < 15; i++) {
            JsonObject msg = receivedMessages.poll(2, TimeUnit.SECONDS);
            if (msg != null && msg.has("individuals")) {
                simUpdate = msg;
                break;
            }
        }

        assertNotNull(simUpdate, "Client should receive SimulationUpdate broadcast frame");
        assertTrue(simUpdate.has("individuals"), "Update frame must have individuals");
        assertTrue(simUpdate.has("food"), "Update frame must have food sources");
        assertTrue(simUpdate.has("environment"), "Update frame must have environment data");
        assertTrue(simUpdate.has("predators"), "Update frame must stream predators array");
        assertTrue(simUpdate.has("terrain"), "Update frame must stream terrain dimensions");
    }

    @Test
    @DisplayName("Verify collision prevention, built-in protection and version disambiguation")
    public void testScenarioCollisionAndDisambiguation() throws Exception {
        // Consume welcome
        receivedMessages.poll(3, TimeUnit.SECONDS);

        // 1. Attempt to overwrite a protected academic scenario ID
        JsonObject acadAttempt = new JsonObject();
        acadAttempt.addProperty("id", "ACAD_01_LEVY_BROWNIAN");
        acadAttempt.addProperty("title", "Exploration Strategy Evaluation: Lévy Flights vs Brownian Walk");
        acadAttempt.addProperty("author", "Hacker_Test");
        acadAttempt.addProperty("masterSeed", 12345);

        JsonObject saveReq = new JsonObject();
        saveReq.addProperty("type", "SAVE_SCENARIO");
        saveReq.add("scenario", acadAttempt);
        client.send(saveReq.toString());

        // 2. The server must protect the built-in scenario and fork the custom one
        JsonObject listMsg = null;
        for (int i = 0; i < 5; i++) {
            JsonObject msg = receivedMessages.poll(3, TimeUnit.SECONDS);
            if (msg != null && "SERVER_SCENARIOS_LIST".equals(msg.get("type").getAsString())) {
                listMsg = msg;
                break;
            }
        }
        assertNotNull(listMsg, "Should receive updated scenario list");
        var scenariosArr = listMsg.getAsJsonArray("scenarios");
        
        // Find academic reference - must still be intact with isBuiltIn = true
        boolean acadIntact = false;
        boolean forkCreated = false;
        for (var el : scenariosArr) {
            JsonObject sc = el.getAsJsonObject();
            if ("ACAD_01_LEVY_BROWNIAN".equals(sc.get("id").getAsString()) && sc.get("isBuiltIn").getAsBoolean()) {
                acadIntact = true;
            }
            if (sc.get("id").getAsString().startsWith("CUSTOM_FORK_ACAD_01_LEVY_BROWNIAN")) {
                forkCreated = true;
                assertTrue(sc.get("title").getAsString().contains("[Fork]"), "Fork title should be marked");
            }
        }
        assertTrue(acadIntact, "Built-in academic scenario must never be overwritten");
        assertTrue(forkCreated, "Custom attempt on built-in ID must be safely forked");
    }
}
