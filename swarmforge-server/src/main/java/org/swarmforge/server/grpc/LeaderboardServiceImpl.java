/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.grpc;

import io.grpc.stub.StreamObserver;
import org.swarmforge.protocol.grpc.LeaderboardServiceGrpc;
import org.swarmforge.protocol.grpc.GetLeaderboardRequest;
import org.swarmforge.protocol.grpc.GetLeaderboardResponse;
import org.swarmforge.protocol.grpc.LeaderboardEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

/**
 * gRPC Service for handling leaderboards.
 * Currently uses an in-memory store, to be replaced by Redis/DB.
 */
public class LeaderboardServiceImpl extends LeaderboardServiceGrpc.LeaderboardServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardServiceImpl.class);

    // Mock data store
    private final List<LeaderboardEntry> mockData = new ArrayList<>();

    public LeaderboardServiceImpl() {
        // Seed some data
        mockData.add(createEntry(1, "The Red Horde", "PlayerOne", 5000f, "uuid-1"));
        mockData.add(createEntry(2, "Anty McAntFace", "PlayerTwo", 4500f, "uuid-2"));
        mockData.add(createEntry(3, "Leaf Cutters Local 101", "PlayerThree", 3000f, "uuid-3"));
    }

    private LeaderboardEntry createEntry(int rank, String colony, String owner, float score, String id) {
        return LeaderboardEntry.newBuilder()
                .setRank(rank)
                .setColonyName(colony)
                .setOwnerName(owner)
                .setScore(score)
                .setColonyId(id)
                .build();
    }

    @Override
    public void getTopColonies(GetLeaderboardRequest request, StreamObserver<GetLeaderboardResponse> responseObserver) {
        LOG.info("Leaderboard requested for category: {}", request.getCategory());

        // In a real implementation, we would query DB/Redis based on Category
        // For now, return the mock data sorted by score (descending)

        List<LeaderboardEntry> sorted = mockData.stream()
                .sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
                .limit(request.getLimit() > 0 ? request.getLimit() : 10)
                .collect(Collectors.toList());

        // Re-assign ranks based on current sort
        List<LeaderboardEntry> ranked = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ranked.add(sorted.get(i).toBuilder().setRank(i + 1).build());
        }

        responseObserver.onNext(GetLeaderboardResponse.newBuilder()
                .addAllEntries(ranked)
                .build());
        responseObserver.onCompleted();
    }
}
