package org.swarmforge.server;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.server.security.JwtUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.swarmforge.protocol.grpc.*;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private SwarmForgeServer server;
    private ManagedChannel channel;
    private SimulationServiceGrpc.SimulationServiceBlockingStub stub;
    private int port = 50055;

    private ManagedChannel buildClientChannel() {
        return io.grpc.ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
    }

    private SimulationServiceGrpc.SimulationServiceBlockingStub createAuthStub(ManagedChannel channel) {
        String token = JwtUtil.generateToken("admin", List.of("ADMIN"));
        Metadata headers = new Metadata();
        Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authKey, "Bearer " + token);
        return SimulationServiceGrpc.newBlockingStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

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

        while (System.currentTimeMillis() - start < 30000) {
            try {
                testChannel = buildClientChannel();
                SimulationServiceGrpc.SimulationServiceBlockingStub testStub = createAuthStub(testChannel);
                // Call lightweight method
                testStub.listSimulations(ListSimulationsRequest.newBuilder().build());
                connected = true;
                break;
            } catch (Exception e) {
                // Ignore transient connection errors during startup polling
                Thread.sleep(300);
            } finally {
                if (testChannel != null) {
                    testChannel.shutdownNow();
                }
            }
        }

        if (!connected) {
            throw new RuntimeException("Server failed to start within timeout");
        }

        channel = buildClientChannel();
        stub = createAuthStub(channel);
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
