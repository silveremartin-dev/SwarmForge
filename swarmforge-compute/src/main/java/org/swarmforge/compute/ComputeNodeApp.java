/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.compute;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.swarmforge.protocol.grpc.ComputeServiceGrpc;
import org.swarmforge.protocol.grpc.SimulationServiceGrpc;
import org.swarmforge.protocol.grpc.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SwarmForge Compute Node - Distributed Worker
 * Headless node that registers with a server and processes simulation tasks.
 * Supports GPU acceleration (via PheromoneKernel if enabled).
 */
public class ComputeNodeApp {

    private static final Logger LOG = Logger.getLogger(ComputeNodeApp.class.getName());

    private final String nodeId;
    private final String serverHost;
    private final int serverPort;
    private final int myPort;
    private final int workerThreads;
    private final boolean gpuEnabled;

    private Server server;
    private ManagedChannel channel;
    private SimulationServiceGrpc.SimulationServiceBlockingStub stub;

    private ScheduledExecutorService heartbeatExecutor;
    private boolean registered = false;

    public ComputeNodeApp(String serverHost, int serverPort, int myPort, int workerThreads, boolean gpuEnabled) {
        this.nodeId = "node-" + UUID.randomUUID().toString().substring(0, 8);
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.myPort = myPort;
        this.workerThreads = workerThreads;
        this.gpuEnabled = gpuEnabled;
    }

    public void start() throws IOException, InterruptedException {
        printBanner();

        LOG.info("Starting compute node: " + nodeId);
        LOG.info("Listening on port: " + myPort);
        LOG.info("Main Server: " + serverHost + ":" + serverPort);
        LOG.info("Worker threads: " + workerThreads);
        LOG.info("GPU acceleration: " + (gpuEnabled ? "ENABLED" : "DISABLED"));

        // 1. Start gRPC Server
        server = ServerBuilder.forPort(myPort)
                .addService(new ComputeServiceImpl())
                .build()
                .start();

        // 2. Connect to Main Server
        connectToServer();

        // 3. Register
        registerWithServer();

        // 4. Heartbeat
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 10, 30, TimeUnit.SECONDS);

        LOG.info("Compute node ready. Waiting for tasks...");

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        server.awaitTermination();
    }

    void connectToServer() {
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext()
                .build();
        stub = SimulationServiceGrpc.newBlockingStub(channel);
    }

    void registerWithServer() {
        LOG.info("Registering with server...");
        try {
            String hostIp = "127.0.0.1";
            try {
                hostIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ignored) {}
            String myAddress = hostIp + ":" + myPort;
            RegisterNodeResponse response = stub.registerNode(
                    RegisterNodeRequest.newBuilder()
                            .setNodeId(nodeId)
                            .setAddress(myAddress)
                            .setPort(myPort)
                            .setHasGpu(gpuEnabled)
                            .build());

            if (response.getSuccess()) {
                registered = true;
                LOG.info("✓ Registered successfully as " + nodeId);
                startHeartbeat();
            } else {
                LOG.warning("Registration failed!");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to register with server: " + e.getMessage(), e);
        }
    }

    private void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (registered && stub != null) {
                try {
                    double systemCpuLoad = com.sun.management.OperatingSystemMXBean.class.isInstance(
                            java.lang.management.ManagementFactory.getOperatingSystemMXBean())
                            ? ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean()).getCpuLoad()
                            : 0.2;
                    float cpuLoad = (float) Math.max(0.0, systemCpuLoad);
                    stub.sendHeartbeat(HeartbeatRequest.newBuilder()
                            .setNodeId(nodeId)
                            .setCpuLoad(cpuLoad)
                            .setGpuLoad(gpuEnabled ? 0.1f : 0.0f)
                            .setTasksCompleted(1)
                            .build());
                } catch (Exception e) {
                    LOG.fine("Heartbeat failed: " + e.getMessage());
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public boolean isRegistered() {
        return registered;
    }

    private void shutdown() {
        LOG.info("Shutting down compute node...");
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        if (channel != null) {
            channel.shutdown();
        }
        if (server != null) {
            server.shutdown();
        }
    }

    private void printBanner() {
        System.out.println("""
                +------------------------------------------------------+
                |       SWARMFORGE COMPUTE NODE                        |
                +------------------------------------------------------+
                |  Node ID: %-34s |
                |  Port:    %-34d |
                +------------------------------------------------------+
                """.formatted(nodeId, myPort));
    }

    private void sendHeartbeat() {
        if (!registered || stub == null)
            return;
        try {
            stub.sendHeartbeat(HeartbeatRequest.newBuilder()
                    .setNodeId(nodeId)
                    .setNodeId(nodeId)
                    .build());
        } catch (Exception e) {
            LOG.warning("Heartbeat failed: " + e.getMessage());
        }
    }

    // === Service Implementation ===

    class ComputeServiceImpl extends ComputeServiceGrpc.ComputeServiceImplBase {
        @Override
        public void processTick(ProcessTickRequest request,
                StreamObserver<ProcessTickResponse> responseObserver) {
            // LOG.info("Received tick task: " + request.getIndividualsCount() + "
            // entities");

            // Dummy processing for now
            ProcessTickResponse.Builder response = ProcessTickResponse.newBuilder();

            // Just echo back or do simple movement
            for (var indState : request.getIndividualsList()) {
                response.addUpdates(IndividualDelta.newBuilder()
                        .setId(indState.getId())
                        // .setPosition(...) calculate new pos?
                        .setAlive(true)
                        .build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        @Override
        public void processPheromones(PheromoneTaskRequest request,
                StreamObserver<PheromoneTaskResponse> responseObserver) {
            try {
                int total3dElements = (request.getWidth() > 0 && request.getHeight() > 0 && request.getDepth() > 0)
                        ? (request.getWidth() * request.getHeight() * request.getDepth() * 8)
                        : Math.max(1, request.getPheromonesCount());
                int size = Math.max(total3dElements, request.getPheromonesCount());
                LOG.info("Processing pheromones RPC task (" + size + " elements)...");
                float[] data = new float[size];
                int pCount = request.getPheromonesCount();
                for (int i = 0; i < pCount; i++) {
                    data[i] = request.getPheromones(i);
                }
                float[] result = new float[size];

                boolean processedOnGpu = false;
                if (gpuEnabled) {
                    try {
                        org.swarmforge.core.simulation.gpu.PheromoneKernel kernel = new org.swarmforge.core.simulation.gpu.PheromoneKernel(
                                request.getWidth(), request.getHeight(), request.getDepth(),
                                8, data, result, 0.1f, 0.01f);
                        kernel.execute(request.getWidth() * request.getHeight() * request.getDepth());
                        kernel.dispose();
                        processedOnGpu = true;
                    } catch (Throwable e) {
                        LOG.warning("GPU execution failed, falling back to CPU processing: " + e.getMessage());
                    }
                }

                if (!processedOnGpu) {
                    org.swarmforge.core.simulation.gpu.PheromoneVectorFallback.processVectorized(
                            request.getWidth(), request.getHeight(), request.getDepth(),
                            8, data, result, 0.1f, 0.01f);
                }

                PheromoneTaskResponse.Builder builder = PheromoneTaskResponse.newBuilder()
                        .setSuccess(true);
                for (float f : result) {
                    builder.addNewPheromones(f);
                }

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Error in processPheromones: " + t.getMessage(), t);
                responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(t.toString()).withCause(t).asRuntimeException());
            }
        }

        @Override
        public void processPathfinding(PathfindingRequest request,
                StreamObserver<PathfindingResponse> responseObserver) {
            // Simple A* implementation (or delegate to existing pathfinder)
            int sx = request.getStart().getX(), sy = request.getStart().getY(), sz = request.getStart().getZ();
            int gx = request.getGoal().getX(), gy = request.getGoal().getY(), gz = request.getGoal().getZ();

            // For now, return a straight-line path (naive)
            // Real implementation would use A* with walkableData
            PathfindingResponse.Builder resp = PathfindingResponse.newBuilder()
                    .setSuccess(true)
                    .addPath(Vec3i.newBuilder().setX(sx).setY(sy).setZ(sz))
                    .addPath(Vec3i.newBuilder().setX(gx).setY(gy).setZ(gz));

            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int serverPort = 50051;
        int myPort = 50052; // Default diff from server
        int threads = Runtime.getRuntime().availableProcessors();
        boolean gpu = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> serverPort = Integer.parseInt(args[++i]); // Server port
                case "--my-port" -> myPort = Integer.parseInt(args[++i]); // My port
                case "--threads" -> threads = Integer.parseInt(args[++i]);
                case "--gpu" -> gpu = true;
                case "--help", "-h" -> {
                    printHelp();
                    return;
                }
            }
        }

        try {
            ComputeNodeApp node = new ComputeNodeApp(host, serverPort, myPort, threads, gpu);
            node.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("""
                Usage: java -jar swarmforge-compute.jar [OPTIONS]
                --host <addr>      Server hostname (sim server)
                --port <num>       Server port
                --my-port <num>    Listening port for this node
                --gpu              Enable GPU
                """);
    }
}
