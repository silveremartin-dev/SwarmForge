/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.swarmforge.protocol.grpc.MatchmakingServiceGrpc;
import org.swarmforge.protocol.grpc.MatchRequest;
import org.swarmforge.protocol.grpc.MatchUpdate;
import org.swarmforge.protocol.grpc.MatchResponse;
import org.swarmforge.protocol.grpc.OpponentInfo;
import org.swarmforge.server.simulation.SimulationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * gRPC Service for handling multiplayer matchmaking.
 */
public class MatchmakingServiceImpl extends MatchmakingServiceGrpc.MatchmakingServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(MatchmakingServiceImpl.class);

    private final SimulationManager simulationManager;

    // Simple FIFO queue for now.
    // In a real system, would use buckets based on ELO/Biomass.
    private final BlockingQueue<PendingPlayer> matchQueue = new LinkedBlockingQueue<>();

    // Map to keep track of active observers to send updates
    private final ConcurrentHashMap<String, StreamObserver<MatchUpdate>> activeObservers = new ConcurrentHashMap<>();

    public MatchmakingServiceImpl(SimulationManager simulationManager) {
        this.simulationManager = simulationManager;
        startMatchmakerThread();
    }

    private record PendingPlayer(MatchRequest request, StreamObserver<MatchUpdate> observer) {
    }

    @Override
    public void findMatch(MatchRequest request, StreamObserver<MatchUpdate> responseObserver) {
        LOG.info("Player {} ({}) joined matchmaking.", request.getPlayerName(), request.getPlayerId());

        activeObservers.put(request.getPlayerId(), responseObserver);
        matchQueue.offer(new PendingPlayer(request, responseObserver));

        // Send initial update
        responseObserver.onNext(MatchUpdate.newBuilder()
                .setStatus(MatchUpdate.Status.SEARCHING)
                .setMessage("Searching for opponent...")
                .build());
    }

    @Override
    public void cancelMatch(MatchRequest request, StreamObserver<MatchResponse> responseObserver) {
        LOG.info("Player {} cancelled matchmaking.", request.getPlayerId());

        // Remove from queue (inefficient O(n) but fine for prototype)
        matchQueue.removeIf(p -> p.request.getPlayerId().equals(request.getPlayerId()));

        StreamObserver<MatchUpdate> obs = activeObservers.remove(request.getPlayerId());
        if (obs != null) {
            obs.onCompleted();
        }

        responseObserver.onNext(MatchResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Cancelled")
                .build());
        responseObserver.onCompleted();
    }

    private void startMatchmakerThread() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Take first player
                    PendingPlayer p1 = matchQueue.take();

                    // Wait for second player (with timeout ideally, but blocking for now)
                    PendingPlayer p2 = matchQueue.take();

                    // Match found!
                    matchPlayers(p1, p2);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOG.error("Error in matchmaker loop", e);
                }
            }
        });
        thread.setName("Matchmaker-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void matchPlayers(PendingPlayer p1, PendingPlayer p2) {
        LOG.info("Matching {} vs {}", p1.request.getPlayerName(), p2.request.getPlayerName());

        String simId = UUID.randomUUID().toString();
        String simName = "Match-" + simId.substring(0, 8);

        // Create shared simulation
        simulationManager.createSimulation(simId, simName, 200, 200, 50);

        // Notify P1
        p1.observer.onNext(MatchUpdate.newBuilder()
                .setStatus(MatchUpdate.Status.FOUND)
                .setMessage("Match Found!")
                .setOpponent(OpponentInfo.newBuilder()
                        .setName(p2.request.getPlayerName())
                        .setColonyAge(p2.request.getColonyAge())
                        .build())
                .setSimulationId(simId)
                .build());
        p1.observer.onCompleted();
        activeObservers.remove(p1.request.getPlayerId());

        // Notify P2
        p2.observer.onNext(MatchUpdate.newBuilder()
                .setStatus(MatchUpdate.Status.FOUND)
                .setMessage("Match Found!")
                .setOpponent(OpponentInfo.newBuilder()
                        .setName(p1.request.getPlayerName())
                        .setColonyAge(p1.request.getColonyAge())
                        .build())
                .setSimulationId(simId)
                .build());
        p2.observer.onCompleted();
        activeObservers.remove(p2.request.getPlayerId());

        // Note: Actual colony spawning handling would happen when clients connect
        // to the simulation ID or via a separate "SetupMatch" call.
        // For now, we assume clients will connect to 'simId' and send
        // 'AddColonyRequest'.
    }
}
