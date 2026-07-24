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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.protocol.grpc.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Cluster Integration Test for SwarmForge Compute Worker Node.
 * Validates worker registration with orchestrator and task processing via gRPC.
 */
public class ClusterIntegrationTest {

    private static final int MOCK_SERVER_PORT = 50081;
    private static final int WORKER_PORT = 50082;

    private Server mockOrchestratorServer;
    private ComputeNodeApp workerNode;
    private Thread workerThread;
    private final AtomicBoolean registrationReceived = new AtomicBoolean(false);

    @BeforeEach
    void setUp() throws Exception {
        // 1. Start Mock Server Orchestrator
        mockOrchestratorServer = ServerBuilder.forPort(MOCK_SERVER_PORT)
                .addService(new SimulationServiceGrpc.SimulationServiceImplBase() {
                    @Override
                    public void registerNode(RegisterNodeRequest request, StreamObserver<RegisterNodeResponse> responseObserver) {
                        registrationReceived.set(true);
                        RegisterNodeResponse response = RegisterNodeResponse.newBuilder()
                                .setSuccess(true)
                                .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void sendHeartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
                        responseObserver.onNext(HeartbeatResponse.newBuilder().setAcknowledged(true).build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        // 2. Start Compute Node Worker App
        workerNode = new ComputeNodeApp("localhost", MOCK_SERVER_PORT, WORKER_PORT, 2, false);
        workerThread = new Thread(() -> {
            try {
                workerNode.start();
            } catch (Exception e) {
                // Expected on teardown
            }
        });
        workerThread.start();

        Thread.sleep(1000);
        workerNode.connectToServer();
        workerNode.registerWithServer();
    }

    @Test
    void testWorkerNodeRegistrationAndTaskExecution() throws Exception {
        assertTrue(registrationReceived.get(), "Worker node should register with server");
        assertTrue(workerNode.isRegistered());

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", WORKER_PORT)
                .usePlaintext()
                .build();

        try {
            ComputeServiceGrpc.ComputeServiceBlockingStub computeStub = ComputeServiceGrpc.newBlockingStub(channel);

            // Test Pheromone task execution on compute worker node
            PheromoneTaskRequest req = PheromoneTaskRequest.newBuilder()
                    .setWidth(1)
                    .setHeight(1)
                    .setDepth(1)
                    .addPheromones(15.0f)
                    .build();

            PheromoneTaskResponse resp = computeStub.processPheromones(req);
            assertNotNull(resp);
            assertTrue(resp.getSuccess());

            // Test Pathfinding task execution on compute worker node
            PathfindingRequest pathReq = PathfindingRequest.newBuilder()
                    .setStart(Vec3i.newBuilder().setX(0).setY(0).setZ(0).build())
                    .setGoal(Vec3i.newBuilder().setX(10).setY(10).setZ(10).build())
                    .build();

            PathfindingResponse pathResp = computeStub.processPathfinding(pathReq);
            assertNotNull(pathResp);
            assertTrue(pathResp.getSuccess());
            assertTrue(pathResp.getPathCount() >= 2);
        } finally {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @AfterEach
    void tearDown() {
        if (mockOrchestratorServer != null) {
            mockOrchestratorServer.shutdownNow();
        }
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }
}
