package org.swarmforge.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.Test;
import org.swarmforge.protocol.grpc.MatchRequest;
import org.swarmforge.protocol.grpc.MatchUpdate;
import org.swarmforge.protocol.grpc.MatchmakingServiceGrpc;
import org.swarmforge.server.simulation.SimulationManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for MatchmakingServiceImpl.
 */
public class MatchmakingTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Test
    public void testMatchmakingPairsTwoPlayers() throws Exception {
        // 1. Setup Server
        String serverName = InProcessServerBuilder.generateName();
        SimulationManager simManager = new SimulationManager();

        grpcCleanup.register(java.util.Objects.requireNonNull(InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new MatchmakingServiceImpl(simManager))
                .build()
                .start()));

        // 2. Setup Clients
        ManagedChannel channel1 = grpcCleanup
                .register(java.util.Objects
                        .requireNonNull(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
        ManagedChannel channel2 = grpcCleanup
                .register(java.util.Objects
                        .requireNonNull(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

        MatchmakingServiceGrpc.MatchmakingServiceStub stub1 = MatchmakingServiceGrpc.newStub(channel1);
        MatchmakingServiceGrpc.MatchmakingServiceStub stub2 = MatchmakingServiceGrpc.newStub(channel2);

        // 3. Helpers
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> simId1 = new AtomicReference<>();
        AtomicReference<String> simId2 = new AtomicReference<>();

        StreamObserver<MatchUpdate> obs1 = new StreamObserver<>() {
            @Override
            public void onNext(MatchUpdate value) {
                if (value.getStatus() == MatchUpdate.Status.FOUND) {
                    simId1.set(value.getSimulationId());
                    latch.countDown();
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        };

        StreamObserver<MatchUpdate> obs2 = new StreamObserver<>() {
            @Override
            public void onNext(MatchUpdate value) {
                if (value.getStatus() == MatchUpdate.Status.FOUND) {
                    simId2.set(value.getSimulationId());
                    latch.countDown();
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        };

        // 4. Run Logic (Connect P1 then P2)
        stub1.findMatch(MatchRequest.newBuilder().setPlayerId("p1").setPlayerName("PlayerOne").build(), obs1);

        // Slight delay to ensure P1 is in queue
        Thread.sleep(100);

        stub2.findMatch(MatchRequest.newBuilder().setPlayerId("p2").setPlayerName("PlayerTwo").build(), obs2);

        // 5. Verify
        boolean matched = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Should have matched within timeout", matched);
        assertNotNull("Should have matched both players", simId1.get());
        assertEquals("Players should be in same simulation", simId1.get(), simId2.get());
    }
}
