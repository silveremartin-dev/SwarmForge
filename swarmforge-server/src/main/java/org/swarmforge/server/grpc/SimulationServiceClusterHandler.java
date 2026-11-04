/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.swarmforge.protocol.grpc.HeartbeatRequest;
import org.swarmforge.protocol.grpc.HeartbeatResponse;
import org.swarmforge.protocol.grpc.RegisterNodeRequest;
import org.swarmforge.protocol.grpc.RegisterNodeResponse;
import org.swarmforge.server.SwarmForgeServer;

/**
 * Handles compute node clustering registration and heartbeat telemetry for {@link SimulationServiceImpl}.
 */
final class SimulationServiceClusterHandler {

    private SimulationServiceClusterHandler() {}

    static void handleRegisterNode(SwarmForgeServer server, RegisterNodeRequest request, StreamObserver<RegisterNodeResponse> responseObserver) {
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

    static void handleHeartbeat(SwarmForgeServer server, HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        if (server != null && server.getClusterManager() != null) {
            server.getClusterManager().handleHeartbeat(
                    request.getNodeId(),
                    request.getCpuLoad(),
                    request.getGpuLoad(),
                    request.getTasksCompleted());
            responseObserver.onNext(HeartbeatResponse.newBuilder().setAcknowledged(true).build());
        } else {
            responseObserver.onNext(HeartbeatResponse.newBuilder().setAcknowledged(false).build());
        }
        responseObserver.onCompleted();
    }
}
