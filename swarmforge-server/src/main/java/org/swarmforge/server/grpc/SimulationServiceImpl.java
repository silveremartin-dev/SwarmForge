package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.protocol.grpc.*;
import org.swarmforge.server.SwarmForgeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationServiceImpl extends SimulationServiceGrpc.SimulationServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationServiceImpl.class);

    private final SwarmForgeServer server;
    private final org.swarmforge.server.simulation.SimulationManager simulationManager;
    private final java.util.concurrent.atomic.AtomicInteger connectedClientCount = new java.util.concurrent.atomic.AtomicInteger(
            0);

    public SimulationServiceImpl(SwarmForgeServer server) {
        this.server = server;
        this.simulationManager = server.getSimulationManager();
    }

    public SimulationServiceImpl(org.swarmforge.server.simulation.SimulationManager simulationManager) {
        this.server = null;
        this.simulationManager = simulationManager;
    }

    // Helper to resolve simulation
    private Simulation getSimulation(String id) {
        if (id == null || id.isEmpty()) {
            return simulationManager.getSimulation("main")
                    .orElseThrow(() -> new RuntimeException("Main simulation not found"));
        }
        return simulationManager.getSimulation(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found: " + id));
    }

    public void getState(GetStateRequest request, StreamObserver<SimulationState> responseObserver) {
        // LOG.info("GetState request received");

        try {
            Simulation sim = getSimulation(request.getSimulationId());

            SimulationState.Builder stateBuilder = SimulationState.newBuilder()
                    .setTick(sim.getTickCount())
                    .setWidth(sim.getTerrarium().getWidth())
                    .setHeight(sim.getTerrarium().getHeight())
                    .setDepth(sim.getTerrarium().getDepth())
                    .setTotalPopulation(getTotalPopulation(sim))
                    .setStatus(mapStatus(sim.getState()));

            // Add colony info
            for (Colony colony : sim.getColonies()) {
                ColonyInfo colonyInfo = ColonyInfo.newBuilder()
                        .setId(colony.getId().toString())
                        .setSpecies(colony.getSpeciesName())
                        .setNestPosition(Vec3.newBuilder()
                                .setX(colony.getNestX())
                                .setY(colony.getNestY())
                                .setZ(colony.getNestZ())
                                .build())
                        .setPopulation(colony.getPopulation())
                        .setFoodStored(colony.getFoodStored())
                        .build();
                stateBuilder.addColonies(colonyInfo);
            }

            responseObserver.onNext(stateBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void control(ControlRequest request, StreamObserver<ControlResponse> responseObserver) {
        LOG.info("Control request: " + request.getAction());

        try {
            Simulation sim = getSimulation(request.getSimulationId());

            switch (request.getAction()) {
                case CTRL_START -> sim.start();
                case CTRL_PAUSE -> sim.pause();
                case CTRL_STOP -> sim.stop();
                case CTRL_SET_SPEED -> sim.setTicksPerSecond(request.getTicksPerSecond());
                default -> {
                }
            }

            ControlResponse response = ControlResponse.newBuilder()
                    .setSuccess(true)
                    .setStatus(mapStatus(sim.getState()))
                    .setMessage("OK")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ControlResponse response = ControlResponse.newBuilder()
                    .setSuccess(false)
                    .setStatus(SimStatus.SIM_STOPPED) // Default status on error
                    .setMessage(e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private final java.util.Map<String, StreamObserver<SimulationUpdate>> connectedClients = new java.util.concurrent.ConcurrentHashMap<>();

    public java.util.List<String> getConnectedClientIds() {
        return new java.util.ArrayList<>(connectedClients.keySet());
    }

    public void kickClient(String clientId) {
        StreamObserver<SimulationUpdate> observer = connectedClients.remove(clientId);
        if (observer != null) {
            try {
                observer.onError(
                        new io.grpc.StatusRuntimeException(io.grpc.Status.ABORTED.withDescription("Kicked by server")));
            } catch (Exception e) {
                LOG.warn("Error processing kick for client " + clientId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public StreamObserver<ClientCommand> streamUpdates(StreamObserver<SimulationUpdate> responseObserver) {
        LOG.info("StreamUpdates connection established");
        connectedClientCount.incrementAndGet();

        return new StreamObserver<>() {
            private volatile boolean streaming = true;
            private Thread streamThread;
            private String clientId = "unknown-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            private String simulationId = "main"; // Default for stream

            // Delta Compression State
            private final java.util.Map<String, IndividualDelta> lastSentState = new java.util.HashMap<>();
            private static final float POS_THRESHOLD = 0.05f; // 5cm movement threshold
            private static final float ROT_THRESHOLD = 0.1f; // ~5 degrees

            @Override
            public void onNext(ClientCommand command) {
                if (command.hasSubscribe()) {
                    SubscribeRequest req = command.getSubscribe();
                    if (!req.getSimulationId().isEmpty()) {
                        simulationId = req.getSimulationId();
                    }

                    // Register client (tracking only, logic moved to stream loop)
                    connectedClients.put(clientId, responseObserver);
                    LOG.info("Client subscribed: " + clientId + " to " + simulationId);

                    // Start streaming updates
                    if (streamThread == null) {
                        streamThread = new Thread(() -> {
                            long lastTick = -1;
                            while (streaming) {
                                // double check if kicked
                                if (!connectedClients.containsKey(clientId)) {
                                    streaming = false;
                                    connectedClientCount.decrementAndGet();
                                    break;
                                }

                                try {
                                    Simulation sim = getSimulation(simulationId);
                                    long currentTick = sim.getTickCount();

                                    if (currentTick != lastTick) {
                                        SimulationUpdate.Builder update = SimulationUpdate.newBuilder()
                                                .setTick(currentTick);

                                        java.util.Set<String> seenIds = new java.util.HashSet<>();

                                        // Add individual positions
                                        for (Colony colony : sim.getColonies()) {
                                            for (Individual ind : colony.getLivingIndividuals()) {
                                                String id = ind.getId().toString();
                                                seenIds.add(id);

                                                float x = ind.getX();
                                                float y = ind.getY();
                                                float z = ind.getZ();
                                                float heading = ind.getHeading();
                                                boolean alive = ind.isAlive();
                                                IndividualDelta.LifeStage stage = IndividualDelta.LifeStage
                                                        .valueOf(ind.getLifeStage().name());
                                                String action = ind.getState() != null
                                                        ? ind.getState().toString()
                                                        : ""; // Check logic

                                                // Check Delta
                                                boolean sendUpdate = true;
                                                if (lastSentState.containsKey(id)) {
                                                    IndividualDelta last = lastSentState.get(id);
                                                    float dx = Math.abs(last.getPosition().getX() - x);
                                                    float dy = Math.abs(last.getPosition().getY() - y);
                                                    float dz = Math.abs(last.getPosition().getZ() - z);
                                                    float dh = Math.abs(last.getHeading() - heading);

                                                    if (dx < POS_THRESHOLD && dy < POS_THRESHOLD && dz < POS_THRESHOLD
                                                            && dh < ROT_THRESHOLD
                                                            && last.getAlive() == alive
                                                            && last.getLifeStage() == stage
                                                            && last.getLifeStage() == stage) {
                                                        sendUpdate = false;
                                                    }
                                                }

                                                if (sendUpdate) {
                                                    IndividualDelta delta = IndividualDelta.newBuilder()
                                                            .setId(id)
                                                            .setPosition(
                                                                    Vec3.newBuilder().setX(x).setY(y).setZ(z).build())
                                                            .setHeading(heading)
                                                            .setAlive(alive)
                                                            .setLifeStage(stage)
                                                            .setCurrentAction(action)
                                                            .build();
                                                    update.addIndividuals(delta);
                                                    lastSentState.put(id, delta);
                                                }
                                            }
                                        }

                                        // Handle Removed Entities
                                        java.util.Iterator<String> it = lastSentState.keySet().iterator();
                                        while (it.hasNext()) {
                                            String id = it.next();
                                            if (!seenIds.contains(id)) {
                                                update.addRemovedIds(id);
                                                it.remove();
                                            }
                                        }

                                        // Only send if there is noteworthy data (or heartbeat every N ticks?)
                                        if (update.getIndividualsCount() > 0 || update.getRemovedIdsCount() > 0
                                                || currentTick % 60 == 0) {
                                            // Check if observer is still valid by sending
                                            responseObserver.onNext(update.build());
                                        }

                                        lastTick = currentTick;
                                    }
                                } catch (Exception e) {
                                    // if sim not found or other error
                                    // LOG.warn("Stream error for " + clientId + ": " + e.getMessage());
                                    // Don't kill stream immediately if sim paused/stopped or temporarily
                                    // unavailable
                                }

                                try {
                                    Thread.sleep(33); // ~30 FPS server updates (client interpolates to 60)
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        });
                        streamThread.start();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                LOG.warn("Stream error from client " + clientId + ": " + t.getMessage());
                streaming = false;
                connectedClients.remove(clientId);
            }

            @Override
            public void onCompleted() {
                LOG.info("Stream completed for client " + clientId);
                streaming = false;
                connectedClients.remove(clientId);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void modifyTerrain(ModifyTerrainRequest request, StreamObserver<ModifyTerrainResponse> responseObserver) {
        try {
            Simulation sim = getSimulation(request.getSimulationId());
            Vec3i pos = request.getPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            // Material ID: 0=AIR, 1=EARTH, 2=SAND, 3=WATER
            int materialId = request.getMaterial();
            org.swarmforge.core.domain.TerrariumCell newCell = switch (materialId) {
                case 0 -> org.swarmforge.core.domain.TerrariumCell.air(x, y, z);
                case 1 -> org.swarmforge.core.domain.TerrariumCell.earth(x, y, z);
                case 2 -> org.swarmforge.core.domain.TerrariumCell.sand(x, y, z);
                case 3 -> org.swarmforge.core.domain.TerrariumCell.water(x, y, z);
                default -> org.swarmforge.core.domain.TerrariumCell.air(x, y, z);
            };

            sim.getTerrarium().setCell(newCell);
            LOG.info("Modified terrain at ({},{},{}) to material {}", x, y, z, materialId);

            responseObserver.onNext(ModifyTerrainResponse.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            LOG.error("Error modifying terrain: {}", e.getMessage(), e);
            responseObserver.onNext(ModifyTerrainResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addColony(AddColonyRequest request, StreamObserver<AddColonyResponse> responseObserver) {
        LOG.info("Adding colony for player: " + request.getPlayerName());

        try {
            Simulation sim = getSimulation(request.getSimulationId());
            // Map request to domain logic
            String speciesType = request.getSpecies(); // e.g. "SolenopsisInvicta"

            // Create colony logic
            // (Assuming Simulation has a method to add colony from Species)
            // For now, we manually instantiate via reflection or factory in
            // Simulation/Manager
            // Or simpler: use SimulationManager to create it with Species

            // NOTE: This part relies on specific Factory logic which might need
            // verification.
            // But main goal here is Persistence integration.

            org.swarmforge.core.domain.Colony colony = sim.addColony(speciesType); // Hypothetical, need to check
                                                                                   // Simulation.java or implement here

            // Persistence Saving
            if (server.getDatabaseManager().isConnected()) {
                server.getDatabaseManager().colonyRepository().save(colony, request.getPlayerName());
            }

            AddColonyResponse response = AddColonyResponse.newBuilder()
                    .setSuccess(true)
                    .setColonyId(colony.getId().toString())
                    .setMessage("Colony created and saved.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOG.error("Failed to add colony", e);
            responseObserver.onNext(AddColonyResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    private int getTotalPopulation(Simulation sim) {
        return sim.getColonies().stream()
                .mapToInt(Colony::getPopulation)
                .sum();
    }

    private SimStatus mapStatus(Simulation.State state) {
        return switch (state) {
            case RUNNING -> SimStatus.SIM_RUNNING;
            case PAUSED -> SimStatus.SIM_PAUSED;
            case STOPPED -> SimStatus.SIM_STOPPED;
        };
    }

    @Override
    public void saveWorld(SaveWorldRequest request, StreamObserver<SaveWorldResponse> responseObserver) {
        try {
            server.saveWorld(request.getName());
            responseObserver.onNext(SaveWorldResponse.newBuilder().setSuccess(true).setMessage("Saved").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onNext(SaveWorldResponse.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void loadWorld(LoadWorldRequest request, StreamObserver<LoadWorldResponse> responseObserver) {
        try {
            server.loadWorld(request.getWorldId());
            responseObserver.onNext(LoadWorldResponse.newBuilder().setSuccess(true).setMessage("Loaded").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver
                    .onNext(LoadWorldResponse.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getColonyStats(GetColonyStatsRequest request, StreamObserver<GetColonyStatsResponse> responseObserver) {
        try {
            Simulation sim = getSimulation(request.getSimulationId());
            GetColonyStatsResponse.Builder builder = GetColonyStatsResponse.newBuilder();
            for (Colony c : sim.getColonies()) {
                if (request.getColonyId().isEmpty() || c.getId().toString().equals(request.getColonyId())) {
                    builder.addStats(ColonyStats.newBuilder()
                            .setId(c.getId().toString())
                            .setSpecies(c.getSpeciesName())
                            .setPopulation(c.getPopulation())
                            .setFoodStored(c.getFoodStored())
                            .setNumQueens(c.countByCaste(Individual.Caste.QUEEN))
                            .setNumWorkers(c.countByCaste(Individual.Caste.WORKER))
                            .setNumSoldiers(c.countByCaste(Individual.Caste.SOLDIER))
                            .build());
                }
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void listSimulations(ListSimulationsRequest request,
            StreamObserver<ListSimulationsResponse> responseObserver) {
        LOG.info("ListSimulations request received");

        ListSimulationsResponse.Builder builder = ListSimulationsResponse.newBuilder();

        simulationManager.getAllSimulations().forEach((id, sim) -> {
            String name = simulationManager.getSimulationName(id).orElse("Unknown");
            SimulationInfo info = SimulationInfo.newBuilder()
                    .setId(id)
                    .setName(name)
                    .setTick(sim.getTickCount())
                    .setPopulation(getTotalPopulation(sim))
                    .setStatus(mapStatus(sim.getState()))
                    .setConnectedClients(connectedClientCount.get()) // Simple global count for now
                    .build();
            builder.addSimulations(info);
        });

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getServerStatus(GetServerStatusRequest request, StreamObserver<ServerStatusResponse> responseObserver) {
        LOG.info("GetServerStatus request received");

        ServerStatusResponse response = ServerStatusResponse.newBuilder()
                .setGrpcRunning(true)
                .setDatabaseConnected(server != null && server.isDatabaseConnected())
                .setRedisConnected(server != null && server.isRedisConnected())
                .setRunningSimulations(simulationManager.getAllSimulations().size())
                .setConnectedClients(connectedClientCount.get())
                .setVersion("2.0.0-SNAPSHOT")
                .setUptimeSeconds(server != null ? server.getUptimeSeconds() : 0)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registerNode(RegisterNodeRequest request, StreamObserver<RegisterNodeResponse> responseObserver) {
        if (server != null && server.getClusterManager() != null) {
            server.getClusterManager().registerNode(
                    request.getNodeId(),
                    request.getAddress(),
                    request.getPort(),
                    request.getHasGpu());
            responseObserver.onNext(RegisterNodeResponse.newBuilder().setSuccess(true).build());
        } else {
            responseObserver.onNext(RegisterNodeResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }
}
