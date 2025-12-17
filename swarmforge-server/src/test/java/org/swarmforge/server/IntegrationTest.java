package org.swarmforge.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import org.swarmforge.protocol.grpc.*;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private SwarmForgeServer server;
    private ManagedChannel channel;
    private SimulationServiceGrpc.SimulationServiceBlockingStub stub;
    private int port = 50055;

    @BeforeEach
    public void setUp() throws Exception {
        ServerConfig defaultConfig = ServerConfig.offline();
        ServerConfig testConfig = new ServerConfig(
                port,
                defaultConfig.worldWidth(), defaultConfig.worldHeight(), defaultConfig.worldDepth(),
                defaultConfig.groundLevel(),
                defaultConfig.latitude(), defaultConfig.longitude(), defaultConfig.seed(),
                "", 0, "", "", "", // DB
                "", 0 // Redis
        );

        server = new SwarmForgeServer(testConfig);

        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Allow server to startup with polling
        boolean connected = false;
        long start = System.currentTimeMillis();
        ManagedChannel testChannel = null;

        while (System.currentTimeMillis() - start < 10000) {
            try {
                testChannel = ManagedChannelBuilder.forAddress("localhost", port)
                        .usePlaintext()
                        .build();
                SimulationServiceGrpc.SimulationServiceBlockingStub testStub = SimulationServiceGrpc
                        .newBlockingStub(testChannel);
                // Call lightweight method
                testStub.listSimulations(ListSimulationsRequest.newBuilder().build());
                connected = true;
                break;
            } catch (Exception e) {
                // Ignore connection failure
                Thread.sleep(500);
            } finally {
                if (testChannel != null) {
                    testChannel.shutdownNow();
                }
            }
        }

        if (!connected) {
            // throw new RuntimeException("Server failed to start within timeout");
            // Don't throw here, let the main stub fail for clearer error?
            // No, fail fast is better.
            throw new RuntimeException("Server failed to start within 10s timeout");
        }

        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        stub = SimulationServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFullSimulationLoop() throws InterruptedException {
        ListSimulationsResponse listResp = stub.listSimulations(ListSimulationsRequest.newBuilder().build());
        assertTrue(listResp.getSimulationsCount() > 0, "Should have at least one simulation");

        String simId = listResp.getSimulations(0).getId();
        System.out.println("Testing against Simulation ID: " + simId);

        ControlResponse startResp = stub.control(ControlRequest.newBuilder()
                .setSimulationId(simId)
                .setAction(ControlAction.CTRL_START)
                .build());
        assertTrue(startResp.getSuccess(), "Control Start success");

        Thread.sleep(2000);

        SimulationState state = stub.getState(GetStateRequest.newBuilder()
                .setSimulationId(simId)
                .build());

        System.out.println("Current Tick: " + state.getTick());
        assertTrue(state.getTick() > 0, "Tick should be > 0");
    }
}
