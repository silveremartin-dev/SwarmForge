/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.ai;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.protocol.grpc.*;

import java.util.Random;

/**
 * Mock implementation of RLService for development and testing.
 * Returns random valid actions.
 */
public class MockRLService extends RLServiceGrpc.RLServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(MockRLService.class);
    private final Random random = new Random();

    @Override
    public void predict(PredictRequest request, StreamObserver<PredictResponse> responseObserver) {
        // Log occasionally to avoid spam
        if (random.nextFloat() < 0.01) {
            LOG.info("Received predict request for model: " + request.getModelId() + " Agent: " + request.getAgentId());
        }

        // Return random action index (0-4 based on RemoteRLArchitecture mapping)
        // 0=MOVE_FWD, 1=TURN_L, 2=TURN_R, 3=PICKUP, 4=DROP
        int action = random.nextInt(5);

        PredictResponse response = PredictResponse.newBuilder()
                .setActionIndex(action)
                .setConfidence(random.nextFloat())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void train(TrainRequest request, StreamObserver<TrainResponse> responseObserver) {
        // Mock training - just acknowledge
        // LOG.info("Received training sample. Reward: " + request.getReward());

        TrainResponse response = TrainResponse.newBuilder()
                .setSuccess(true)
                .setCurrentLoss(random.nextFloat() * 0.5f)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
