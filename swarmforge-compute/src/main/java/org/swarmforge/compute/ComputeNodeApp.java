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

        // 4. Heartbeat & Auto-Registration Loop
        startHeartbeat();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        LOG.info("Compute node ready. Waiting for tasks...");
        server.awaitTermination();
    }

    void connectToServer() {
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext()
                .maxInboundMessageSize(16 * 1024 * 1024)
                .build();
        stub = SimulationServiceGrpc.newBlockingStub(channel).withCompression("gzip");
    }

    void registerWithServer() {
        LOG.info("Registering with server...");
        try {
            String hostIp = "127.0.0.1";
            try {
                hostIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ignored) {}
            String myHost = System.getenv().getOrDefault("COMPUTE_HOST", System.getenv().getOrDefault("MY_HOST", hostIp));
            String myAddress = myHost + ":" + myPort;
            RegisterNodeResponse response = stub.registerNode(
                    RegisterNodeRequest.newBuilder()
                            .setNodeId(nodeId)
                            .setAddress(myAddress)
                            .setPort(myPort)
                            .setHasGpu(gpuEnabled)
                            .build());

            if (response.getSuccess()) {
                registered = true;
                LOG.info("✓ Registered successfully with server as " + nodeId + " (" + myAddress + ")");
                startHeartbeat();
            } else {
                LOG.warning("Registration rejected by server, will retry...");
            }
        } catch (Exception e) {
            LOG.warning("Waiting for server at " + serverHost + ":" + serverPort + " (" + e.getMessage() + ")");
        }
    }

    private synchronized void startHeartbeat() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            return;
        }
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 5, 5, TimeUnit.SECONDS);
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
        if (!registered) {
            registerWithServer();
            return;
        }
        if (stub == null) return;
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
            LOG.warning("Heartbeat failed: " + e.getMessage() + ", re-registering...");
            registered = false;
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
        String host = System.getenv().getOrDefault("SERVER_HOST", "localhost");
        int serverPort = 50051;
        try {
            String sp = System.getenv("SERVER_PORT");
            if (sp != null && !sp.isBlank()) serverPort = Integer.parseInt(sp);
        } catch (Exception ignored) {}

        int myPort = 50052;
        try {
            String mp = System.getenv("COMPUTE_PORT");
            if (mp == null || mp.isBlank()) mp = System.getenv("MY_PORT");
            if (mp != null && !mp.isBlank()) myPort = Integer.parseInt(mp);
        } catch (Exception ignored) {}

        // 1. Initialize EnginePreferences from args / system env
        org.swarmforge.core.engine.EnginePreferences.applyCommandLineArgs(args);

        int threads = org.swarmforge.core.engine.EnginePreferences.getThreadCount();
        boolean gpu = org.swarmforge.core.engine.EnginePreferences.getSelectedAccelerationMode() == org.swarmforge.core.engine.ComputeAccelerationMode.GPU_ACCELERATED
                || "true".equalsIgnoreCase(System.getenv("GPU_ENABLED"));

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("--host=")) {
                    host = arg.substring("--host=".length());
                } else if (arg.startsWith("--port=")) {
                    try { serverPort = Integer.parseInt(arg.substring("--port=".length())); } catch (Exception ignored) {}
                } else if (arg.startsWith("--my-port=")) {
                    try { myPort = Integer.parseInt(arg.substring("--my-port=".length())); } catch (Exception ignored) {}
                } else {
                    switch (arg) {
                        case "--host" -> { if (i + 1 < args.length) host = args[++i]; }
                        case "--port" -> { if (i + 1 < args.length) serverPort = Integer.parseInt(args[++i]); }
                        case "--my-port" -> { if (i + 1 < args.length) myPort = Integer.parseInt(args[++i]); }
                        case "--threads", "--engine", "--accel" -> { if (i + 1 < args.length) i++; }
                        case "--gpu", "--cpu", "--rust", "--java", "--single-core", "--multi-core", "--monocoeur", "--multicoeur" -> {}
                        case "--help", "-h" -> {
                            printHelp();
                            return;
                        }
                    }
                }
            }
        }

        // Re-read updated thread count and GPU flag after arguments iteration
        threads = org.swarmforge.core.engine.EnginePreferences.getThreadCount();
        if (org.swarmforge.core.engine.EnginePreferences.getSelectedAccelerationMode() == org.swarmforge.core.engine.ComputeAccelerationMode.GPU_ACCELERATED) {
            gpu = true;
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
                +------------------------------------------------------+
                |        SWARMFORGE COMPUTE NODE - HELP                |
                +------------------------------------------------------+
                |  Usage: java -jar swarmforge-compute.jar [OPTIONS]   |
                +------------------------------------------------------+
                |  Cluster Network Options:                            |
                |    --host <addr>      Server hostname (default: localhost)
                |    --port <num>       Server gRPC port (default: 50051)
                |    --my-port <num>    Listening port for worker (default: 50052)
                |                                                      |
                |  Compute Engine & Acceleration Options:              |
                |    --engine <auto|java|rust> Engine backend selector |
                |    --rust / --java           Shortcut engine flags   |
                |    --accel <auto|gpu|cpu>    Hardware compute accel  |
                |    --gpu / --cpu             Shortcut accel flags    |
                |    --threads <N>             Worker thread count     |
                |    --single-core / --monocoeur Force 1 worker thread |
                |    --multi-core / --multicoeur Max available cores   |
                |    --help, -h                Show this help message  |
                +------------------------------------------------------+
                """);
    }
}
