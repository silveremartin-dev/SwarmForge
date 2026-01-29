package org.swarmforge.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.Test;
import org.swarmforge.protocol.grpc.GetLeaderboardRequest;
import org.swarmforge.protocol.grpc.GetLeaderboardResponse;
import org.swarmforge.protocol.grpc.LeaderboardServiceGrpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LeaderboardTest {

        @Rule
        public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

        @Test
        public void testGetTopColoniesReturnsMockData() throws Exception {
                // 1. Setup Server
                String serverName = InProcessServerBuilder.generateName();

                io.grpc.Server server = InProcessServerBuilder.forName(serverName)
                                .directExecutor()
                                .addService(new LeaderboardServiceImpl())
                                .build()
                                .start();
                grpcCleanup.register(java.util.Objects.requireNonNull(server));

                // 2. Setup Client
                ManagedChannel channel = grpcCleanup
                                .register(java.util.Objects.requireNonNull(
                                                InProcessChannelBuilder.forName(serverName).directExecutor().build()));
                LeaderboardServiceGrpc.LeaderboardServiceBlockingStub stub = LeaderboardServiceGrpc
                                .newBlockingStub(channel);

                // 3. Test
                GetLeaderboardResponse response = stub.getTopColonies(GetLeaderboardRequest.newBuilder()
                                .setCategory(GetLeaderboardRequest.Category.BIOMASS)
                                .setLimit(5)
                                .build());

                // 4. Verify
                assertTrue("Should return entries", response.getEntriesCount() > 0);
                assertEquals("Rank 1 should be The Red Horde", "The Red Horde", response.getEntries(0).getColonyName());
                assertTrue("Score should be descending",
                                response.getEntries(0).getScore() >= response.getEntries(1).getScore());
        }
}
