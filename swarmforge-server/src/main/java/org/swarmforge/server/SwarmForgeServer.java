/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.plugin.PluginContext;
import org.swarmforge.core.plugin.PluginManager;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.server.ai.FallbackRLService;
import org.swarmforge.server.cli.SwarmForgeServerCli;
import org.swarmforge.server.compute.ComputeClusterManager;
import org.swarmforge.server.grpc.AuthServiceImpl;
import org.swarmforge.server.grpc.LeaderboardServiceImpl;
import org.swarmforge.server.grpc.MatchmakingServiceImpl;
import org.swarmforge.server.grpc.SimulationServiceImpl;
import org.swarmforge.server.net.SwarmForgeWebSocketServer;
import org.swarmforge.server.persistence.DatabaseManager;
import org.swarmforge.server.persistence.RedisCache;
import org.swarmforge.server.rest.RestApiServer;
import org.swarmforge.server.security.JwtServerInterceptor;
import org.swarmforge.server.simulation.ServerBatchCampaignRunner;
import org.swarmforge.server.simulation.SimulationManager;

/**
 * Main server application for SwarmForge.
 * Initializes simulation, databases, gRPC services, REST endpoints, and WebSocket streaming.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SwarmForgeServer {

    private static final Logger LOG = LoggerFactory.getLogger(SwarmForgeServer.class);

    private final Terrarium terrarium;
    private final SimulationManager simulationManager;
    private final DatabaseManager database;
    private final RedisCache cache;
    private final SimulationServiceImpl simulationService;
    private final MatchmakingServiceImpl matchmakingService;
    private final LeaderboardServiceImpl leaderboardService;
    private final ComputeClusterManager clusterManager;
    private final PluginManager pluginManager;
    private final RestApiServer restApiServer;
    private SwarmForgeWebSocketServer webSocketServer;
    private final int grpcPort;
    private final ServerConfig config;
    private final ServerWorldManager worldManager;
    private Server grpcServer;

    private final ExecutorService dbAsyncExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("db-async-writer-", 0).factory());
    private final long startTime = System.currentTimeMillis();

    public SwarmForgeServer(ServerConfig config) {
        this.config = config;
        this.grpcPort = config.grpcPort();
        this.worldManager = new ServerWorldManager(this);

        // Initialize Simulation Manager
        this.simulationManager = new SimulationManager();

        // Create MAIN simulation
        LOG.info("Initializing main simulation...");
        this.simulationManager.createSimulation("main", "Main World",
                config.worldWidth(), config.worldHeight(), config.worldDepth());

        Simulation mainSim = simulationManager.getSimulation("main").orElseThrow();
        this.terrarium = mainSim.getTerrarium();
        try {
            loadAcademicScenario(AcademicScenarios.createLevyVsBrownianScenario(42L));
        } catch (Exception e) {
            LOG.warn("Could not load initial academic scenario: {}", e.getMessage());
        }

        // Initialize persistence
        this.database = new DatabaseManager(
                config.dbHost(), config.dbPort(), config.dbName(),
                config.dbUser(), config.dbPassword());
        this.cache = new RedisCache(config.redisHost(), config.redisPort());

        // Initialize Compute Cluster
        this.clusterManager = new ComputeClusterManager();
        mainSim.setClusterManager(clusterManager);

        // Initialize Plugins
        this.pluginManager = new PluginManager();
        this.pluginManager.setContext(new PluginContext(mainSim, pluginManager));
        this.pluginManager.loadPluginsFromDirectory(new File("plugins"));

        // Initialize gRPC Services
        this.simulationService = new SimulationServiceImpl(this);
        this.matchmakingService = new MatchmakingServiceImpl(this.simulationManager);
        this.leaderboardService = new LeaderboardServiceImpl();

        // Initialize REST API
        this.restApiServer = new RestApiServer(config.grpcPort() + 1000);
        this.restApiServer.setSimulation(mainSim);
    }

    public void start() throws Exception {
        try {
            LOG.info("Starting SwarmForge Server...");

            // Auto-start Docker services if down
            checkAndAutoStartInfrastructure();

            // Connect Database
            try {
                database.connect();
                if (database.isConnected()) {
                    LOG.info("Database connected successfully");
                    if (getSimulation() != null) {
                        saveWorld("Initial World State");
                    }
                } else {
                    LOG.info("Database running in fallback mode");
                }
            } catch (Exception e) {
                LOG.warn("Database connection failed (running in offline mode): {}", e.getMessage());
            }

            // Connect Redis
            try {
                cache.connect();
                if (cache.isConnected()) {
                    LOG.info("Redis connected successfully");
                } else {
                    LOG.info("Redis caching disabled (running without Redis)");
                }
            } catch (Exception e) {
                LOG.warn("Redis connection failed (caching disabled): {}", e.getMessage());
            }

            // TLS Setup
            SslContext sslContext = createSslContextIfEnabled();

            NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(grpcPort)
                    .maxInboundMessageSize(16 * 1024 * 1024)
                    .maxInboundMetadataSize(64 * 1024);
            if (sslContext != null) {
                serverBuilder.sslContext(sslContext);
            }

            this.grpcServer = serverBuilder
                    .intercept(new JwtServerInterceptor())
                    .addService(new AuthServiceImpl())
                    .addService(simulationService)
                    .addService(matchmakingService)
                    .addService(leaderboardService)
                    .addService(new FallbackRLService())
                    .build()
                    .start();
            LOG.info("gRPC Server (Secure) started on port {}", grpcPort);

            // Start REST API
            try {
                this.restApiServer.start();
            } catch (Exception e) {
                LOG.warn("Failed to start REST API: {}", e.getMessage());
            }

            // Start WebSocket Server
            startWebSocketServer();

            // Start Redis updater & DB checkpointer
            startBackgroundWorkers();

            printStatusBanner();
        } catch (Throwable t) {
            LOG.error("SERVER STARTUP FAILED CRITICALLY: ", t);
            throw t;
        }
    }

    private void checkAndAutoStartInfrastructure() {
        if (config.dbHost() == null || config.dbHost().trim().isEmpty() || config.dbPort() <= 0) {
            return;
        }
        try {
            boolean postgresRunning = isPortOpen(config.dbHost(), config.dbPort());
            boolean redisRunning = (config.redisHost() != null && !config.redisHost().trim().isEmpty() && config.redisPort() > 0)
                    ? isPortOpen(config.redisHost(), config.redisPort())
                    : true;

            if (!postgresRunning || !redisRunning) {
                LOG.warn("Infrastructure (Postgres/Redis) appears down. Attempting auto-start...");
                Process p = null;
                try {
                    p = new ProcessBuilder("docker-compose", "up", "-d").inheritIO().start();
                } catch (Exception e) {
                    try {
                        p = new ProcessBuilder("docker", "compose", "up", "-d").inheritIO().start();
                    } catch (Exception ex) {
                        LOG.warn("Could not invoke docker-compose: {}", ex.getMessage());
                    }
                }
                if (p != null) {
                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        LOG.info("Auto-start command executed. Waiting for services to initialize...");
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(500);
                            if (isPortOpen(config.dbHost(), config.dbPort())) {
                                LOG.info("Services are now reachable.");
                                break;
                            }
                        }
                    } else {
                        LOG.warn("Auto-start Docker infrastructure exited with code {}", exitCode);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Auto-start check skipped or failed: {}", e.getMessage());
        }
    }

    private SslContext createSslContextIfEnabled() {
        if (!"true".equalsIgnoreCase(System.getenv("ENABLE_TLS"))) {
            LOG.info("gRPC Server running in plaintext mode (set ENABLE_TLS=true to activate TLS)");
            return null;
        }
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext ctx = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(io.netty.handler.ssl.SslProvider.OPENSSL)
                    .build();
            LOG.info("TLS Enabled using self-signed certificate (OpenSSL)");
            return ctx;
        } catch (Throwable e) {
            LOG.warn("Failed to initialize OpenSSL TLS, trying JDK SSL: {}", e.getMessage());
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                SslContext ctx = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey()).build();
                LOG.info("TLS Enabled using JDK SSL");
                return ctx;
            } catch (Throwable e2) {
                LOG.warn("Could not initialize TLS, running plaintext: {}", e2.getMessage());
                return null;
            }
        }
    }

    private void startWebSocketServer() {
        try {
            int wsPort = 8081;
            boolean bound = false;
            for (int p = wsPort; p < wsPort + 10; p++) {
                if (!isPortOpen("localhost", p)) {
                    try {
                        this.webSocketServer = new SwarmForgeWebSocketServer(p, simulationManager);
                        this.webSocketServer.start();
                        bound = true;
                        LOG.info("WebSocket Server started on port {}", p);
                        break;
                    } catch (Exception ex) {
                        LOG.warn("Could not bind WebSocket on port {}, trying next: {}", p, ex.getMessage());
                    }
                }
            }
            if (!bound) {
                LOG.warn("Could not find an available port for WebSocket Server (8081-8090 are busy).");
            }
        } catch (Exception e) {
            LOG.warn("Failed to start WebSocket Server: {}", e.getMessage());
        }
    }

    private void startBackgroundWorkers() {
        // Redis live position streamer
        Thread.ofVirtual().name("redis-updater").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (cache.isConnected()) {
                    for (var entry : simulationManager.getAllSimulations().entrySet()) {
                        String simId = entry.getKey();
                        Simulation sim = entry.getValue();
                        if (sim.getState() == Simulation.State.RUNNING) {
                            try {
                                cache.setTick(simId, sim.getTickCount());
                                for (Colony colony : sim.getColonies()) {
                                    for (Individual ind : colony.getLivingIndividuals()) {
                                        cache.setIndividualPosition(simId, ind.getId().toString(),
                                                ind.getX(), ind.getY(), ind.getZ());
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Error updating Redis for {}: {}", simId, e.getMessage());
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Periodic Database Checkpointer
        Thread.ofVirtual().name("db-checkpointer").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000);
                    if (database.isConnected()) {
                        Simulation sim = getSimulation();
                        if (sim != null && sim.getState() == Simulation.State.RUNNING) {
                            saveWorld("Autosave (Tick " + sim.getTickCount() + ")");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    LOG.warn("Auto-checkpoint failed: {}", ex.getMessage());
                }
            }
        });
    }

    // === Simulation Control ===

    public void pauseSimulation() {
        simulationManager.getAllSimulations().values().forEach(Simulation::pause);
        LOG.info("All simulations paused by server request");
    }

    public void resumeSimulation() {
        simulationManager.getAllSimulations().values().forEach(Simulation::start);
        LOG.info("All simulations resumed by server request");
    }

    public void stopSimulation() {
        simulationManager.stopAll();
        LOG.info("All simulations stopped by server request");
    }

    public boolean isSimulationRunning() {
        Simulation sim = getSimulation();
        return sim != null && sim.getState() == Simulation.State.RUNNING;
    }

    public boolean isSimulationPaused() {
        Simulation sim = getSimulation();
        return sim != null && sim.getState() == Simulation.State.PAUSED;
    }

    // === Client Management ===

    public List<String> getConnectedClients() {
        return simulationService.getConnectedClientIds();
    }

    public void kickClient(String clientId) {
        simulationService.kickClient(clientId);
        LOG.info("Kicked client: {}", clientId);
    }

    public void printStatusBanner() {
        String dbStatus = database.isConnected() 
                ? (database.isH2Fallback() ? "[OK] H2 (MEM)" : "[OK] POSTGRES") 
                : "[X] OFFLINE";
        String dbColor = database.isConnected() ? "\u001B[32m" : "\u001B[31m";
        String redisStatus = cache.isConnected() ? "[OK] ONLINE " : "[X] OFFLINE";
        String redisColor = cache.isConnected() ? "\u001B[32m" : "\u001B[31m";
        String reset = "\u001B[0m";

        System.out.println();
        System.out.println("+------------------------------------------------------+");
        System.out.println("|          SWARMFORGE SERVER - STATUS                  |");
        System.out.println("+------------------------------------------------------+");
        System.out.println("|  gRPC Server    : \u001B[32m[OK] RUNNING\u001B[0m  (port " + grpcPort + ")           |");
        System.out.println("|  Database       : " + dbColor + dbStatus + reset + "                       |");
        System.out.println("|  Redis Cache    : " + redisColor + redisStatus + reset + "                       |");
        System.out.println("+------------------------------------------------------+");
        if (database.isH2Fallback()) {
            System.out.println("|  \u001B[33m[!] Running in Standalone / H2 In-Memory Fallback\u001B[0m |");
        }
        if (!cache.isConnected()) {
            System.out.println("|  \u001B[33m[!] Caching disabled - start Redis for caching\u001B[0m     |");
        }
        if (database.isConnected() && cache.isConnected() && !database.isH2Fallback()) {
            System.out.println("|  \u001B[32m[OK] All production services operational\u001B[0m            |");
        }
        System.out.println("+------------------------------------------------------+");
        System.out.println();
    }

    // === World & Scenario Delegations ===

    public void createNewWorld(String name, String terrainType, String speciesType) {
        worldManager.createNewWorld(name, terrainType, speciesType);
    }

    public List<String> getAvailableWorlds() {
        return worldManager.getAvailableWorlds();
    }

    public void loadAcademicScenario(Scenario scenario) {
        worldManager.loadAcademicScenario(scenario);
    }

    public void runBatchCampaign(Scenario scenario, long targetTicks, String exportDir, int snapshotInterval) {
        new ServerBatchCampaignRunner(this).runBatchCampaign(scenario, targetTicks, exportDir, snapshotInterval);
    }

    public void createDemoWorld() {
        worldManager.createDemoWorld();
    }

    public void saveWorld(String name) {
        worldManager.saveWorld(name);
    }

    public void loadWorld(String worldIdStr) {
        worldManager.loadWorld(worldIdStr);
    }

    public void stop() {
        LOG.info("Stopping SwarmForge Server...");
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
        if (simulationManager != null) {
            simulationManager.stopAll();
        }
        if (cache != null) {
            cache.disconnect();
        }
        if (database != null) {
            database.disconnect();
        }
        if (restApiServer != null) {
            try {
                restApiServer.stop();
            } catch (Exception e) {
                LOG.warn("Error stopping REST API server: {}", e.getMessage());
            }
        }
        if (webSocketServer != null) {
            try {
                webSocketServer.stop(500);
            } catch (Exception e) {
                LOG.warn("Error stopping WebSocket server: {}", e.getMessage());
            }
            webSocketServer = null;
        }
        LOG.info("Server stopped");
    }

    public Terrarium getTerrarium() {
        return terrarium;
    }

    public Simulation getSimulation() {
        return simulationManager.getSimulation("main").orElse(null);
    }

    public SimulationManager getSimulationManager() {
        return simulationManager;
    }

    public boolean isDatabaseConnected() {
        return database != null && database.isConnected();
    }

    public boolean isRedisConnected() {
        return cache != null && cache.isConnected();
    }

    public void connectDatabase() throws Exception {
        if (!database.isConnected()) {
            database.connect();
            LOG.info("Database connected via UI request");
        }
    }

    public void disconnectDatabase() {
        if (database.isConnected()) {
            database.disconnect();
            LOG.info("Database disconnected via UI request");
        }
    }

    public void connectCache() throws Exception {
        if (!cache.isConnected()) {
            cache.connect();
            LOG.info("Redis cache connected via UI request");
        }
    }

    public void disconnectCache() {
        if (cache.isConnected()) {
            cache.disconnect();
            LOG.info("Redis cache disconnected via UI request");
        }
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public ComputeClusterManager getClusterManager() {
        return clusterManager;
    }

    public void listAcademicScenarios() {
        SwarmForgeServerCli.listAcademicScenarios();
    }

    public static void main(String[] args) {
        SwarmForgeServerCli.main(args);
    }

    public void printSimulationInfo() {
        SwarmForgeServerCli.printSimulationInfo(this);
    }

    public void listSimulations() {
        SwarmForgeServerCli.listSimulations(this);
    }

    public Server getGrpcServer() {
        return grpcServer;
    }

    public DatabaseManager getDatabaseManager() {
        return database;
    }

    public ServerWorldManager getWorldManager() {
        return worldManager;
    }

    public ExecutorService getDbAsyncExecutor() {
        return dbAsyncExecutor;
    }

    private boolean isPortOpen(String host, int port) {
        if (host == null || host.trim().isEmpty() || port <= 0) return false;
        String targetHost = ("0.0.0.0".equals(host.trim()) || host.trim().isEmpty()) ? "localhost" : host.trim();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHost, port), 600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
