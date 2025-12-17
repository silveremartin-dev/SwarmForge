/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.network;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.swarmforge.protocol.grpc.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * gRPC client for connecting to SwarmForge server.
 */
public class SimulationClient {

    private static final Logger LOG = Logger.getLogger(SimulationClient.class.getName());

    private ManagedChannel channel;
    private SimulationServiceGrpc.SimulationServiceStub asyncStub;
    private SimulationServiceGrpc.SimulationServiceBlockingStub blockingStub;
    private WorldServiceGrpc.WorldServiceBlockingStub worldStub;

    private volatile boolean connected = false;
    private StreamObserver<ClientCommand> requestObserver;
    private long latestTick;
    private final CopyOnWriteArrayList<IndividualDelta> latestIndividuals = new CopyOnWriteArrayList<>();

    /**
     * Connect to the server.
     */
    public void connect(String host, int port) {
        LOG.info("Connecting to " + host + ":" + port);
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        asyncStub = SimulationServiceGrpc.newStub(channel);
        blockingStub = SimulationServiceGrpc.newBlockingStub(channel);
        worldStub = WorldServiceGrpc.newBlockingStub(channel);
        connected = true;
        LOG.info("Connected to server");
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        LOG.info("Disconnecting...");
        connected = false;
        if (requestObserver != null) {
            requestObserver.onCompleted();
        }
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get current simulation state.
     */
    public SimulationState getState() {
        if (!connected)
            return null;
        try {
            return blockingStub.getState(GetStateRequest.newBuilder()
                    .setIncludeIndividuals(true)
                    .build());
        } catch (Exception e) {
            LOG.warning("GetState failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Control the simulation.
     */
    public boolean control(ControlAction action) {
        if (!connected)
            return false;
        try {
            ControlResponse response = blockingStub.control(ControlRequest.newBuilder()
                    .setAction(action)
                    .build());
            return response.getSuccess();
        } catch (Exception e) {
            LOG.warning("Control failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Start streaming updates from the server.
     */
    public void startStreaming() {
        if (!connected)
            return;

        StreamObserver<SimulationUpdate> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SimulationUpdate update) {
                latestTick = update.getTick();
                latestIndividuals.clear();
                latestIndividuals.addAll(update.getIndividualsList());
            }

            @Override
            public void onError(Throwable t) {
                LOG.warning("Stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                LOG.info("Stream completed");
            }
        };

        requestObserver = asyncStub.streamUpdates(responseObserver);

        // Subscribe to updates
        requestObserver.onNext(ClientCommand.newBuilder()
                .setSubscribe(SubscribeRequest.newBuilder()
                        .setViewRadius(100f)
                        .build())
                .build());
    }

    /**
     * Get latest individual positions (thread-safe).
     */
    public List<IndividualDelta> getLatestIndividuals() {
        return List.copyOf(latestIndividuals);
    }

    public long getLatestTick() {
        return latestTick;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Save world state.
     */
    public String saveWorld(String name) throws Exception {
        if (!connected)
            throw new IllegalStateException("Not connected");
        SaveWorldResponse response = blockingStub.saveWorld(SaveWorldRequest.newBuilder().setName(name).build());
        if (!response.getSuccess()) {
            throw new Exception(response.getMessage());
        }
        return response.getMessage();
    }

    /**
     * Load world state.
     */
    public String loadWorld(String worldId) throws Exception {
        if (!connected)
            throw new IllegalStateException("Not connected");
        LoadWorldResponse response = blockingStub.loadWorld(LoadWorldRequest.newBuilder().setWorldId(worldId).build());
        if (!response.getSuccess()) {
            throw new Exception(response.getMessage());
        }
        return response.getMessage();
    }

    /**
     * Get colony stats.
     */
    public List<ColonyStats> getColonyStats(String colonyId) {
        if (!connected)
            return List.of();
        try {
            GetColonyStatsResponse response = blockingStub.getColonyStats(GetColonyStatsRequest.newBuilder()
                    .setColonyId(colonyId == null ? "" : colonyId)
                    .build());
            return response.getStatsList();
        } catch (Exception e) {
            LOG.warning("GetColonyStats failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Spawn food (God Mode).
     */
    public void spawnFood(float x, float y, float z, float amount) {
        if (!connected)
            return;
        try {
            SpawnEntityResponse response = worldStub.spawnEntity(SpawnEntityRequest.newBuilder()
                    .setType(EntityType.ENTITY_FOOD)
                    .setPosition(Vec3.newBuilder().setX(x).setY(y).setZ(z).build())
                    .setAmount(amount)
                    .build());
            if (!response.getSuccess()) {
                LOG.warning("Spawn failed: " + response.getMessage());
            }
        } catch (Exception e) {
            LOG.warning("Spawn error: " + e.getMessage());
        }
    }

    /**
     * Trigger disaster (God Mode).
     */
    public void triggerDisaster(String type, float intensity) {
        if (!connected)
            return;
        try {
            EventType eventType = EventType.EVENT_RAIN;
            if ("HEATWAVE".equalsIgnoreCase(type)) {
                eventType = EventType.EVENT_HEATWAVE;
            }
            TriggerEventResponse response = worldStub.triggerEvent(TriggerEventRequest.newBuilder()
                    .setType(eventType)
                    .setIntensity(intensity)
                    .build());
            if (!response.getSuccess()) {
                LOG.warning("Event failed: " + response.getMessage());
            }
        } catch (Exception e) {
            LOG.warning("Event error: " + e.getMessage());
        }
    }

    /**
     * Modify terrain block.
     * 
     * @param materialId 0=AIR, 1=SOIL, 2=SAND, 3=WATER
     */
    public void modifyTerrain(int x, int y, int z, int materialId) {
        if (!connected)
            return;
        try {
            worldStub.modifyTerrain(ModifyTerrainRequest.newBuilder()
                    .setPosition(Vec3i.newBuilder().setX(x).setY(y).setZ(z).build())
                    .setMaterial(materialId)
                    .build());
        } catch (Exception e) {
            LOG.warning("Terrain modification failed: " + e.getMessage());
        }
    }
}
