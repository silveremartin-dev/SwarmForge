/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.world.NestGenerator;
import org.swarmforge.server.persistence.DatabaseManager;
import org.swarmforge.server.persistence.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Main server application for SwarmForge.
 * Initializes simulation, databases, and gRPC services.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SwarmForgeServer {

    private static final Logger LOG = LoggerFactory.getLogger(SwarmForgeServer.class);

    private final Terrarium terrarium; // Default/Main terrarium? Or one per sim? One per sim.
    // private final Simulation simulation; // REPLACED
    private final org.swarmforge.server.simulation.SimulationManager simulationManager;

    // private final WeatherSystem weather; // Unused
    private final DatabaseManager database;
    private final RedisCache cache;
    private final org.swarmforge.server.grpc.SimulationServiceImpl simulationService; // Fully qualified
    private final org.swarmforge.server.grpc.MatchmakingServiceImpl matchmakingService;
    private final org.swarmforge.server.grpc.LeaderboardServiceImpl leaderboardService;
    private final org.swarmforge.server.compute.ComputeClusterManager clusterManager;
    private final org.swarmforge.core.plugin.PluginManager pluginManager;
    private final org.swarmforge.server.rest.RestApiServer restApiServer;
    private org.swarmforge.server.net.SwarmForgeWebSocketServer webSocketServer;
    private final int grpcPort;
    private io.grpc.Server grpcServer;

    public SwarmForgeServer(ServerConfig config) {
        this.grpcPort = config.grpcPort();

        // Initialize Simulation Manager
        this.simulationManager = new org.swarmforge.server.simulation.SimulationManager();

        // Create MAIN simulation
        LOG.info("Initializing main simulation...");
        this.simulationManager.createSimulation("main", "Main World",
                config.worldWidth(), config.worldHeight(), config.worldDepth());

        // Get main components for legacy support/accessors
        Simulation mainSim = simulationManager.getSimulation("main").orElseThrow();
        this.terrarium = mainSim.getTerrarium();

        // Initialize persistence
        this.database = new DatabaseManager(
                config.dbHost(), config.dbPort(), config.dbName(),
                config.dbUser(), config.dbPassword());
        this.cache = new RedisCache(config.redisHost(), config.redisPort());

        // Initialize Compute Cluster
        this.clusterManager = new org.swarmforge.server.compute.ComputeClusterManager();
        mainSim.setClusterManager(clusterManager); // Set for main

        // Initialize Plugins
        this.pluginManager = new org.swarmforge.core.plugin.PluginManager();
        this.pluginManager.setContext(new org.swarmforge.core.plugin.PluginContext(mainSim, pluginManager)); // Plugins
                                                                                                             // attached
                                                                                                             // to main?
        this.pluginManager.loadPluginsFromDirectory(new java.io.File("plugins"));

        // Initialize gRPC Service
        // We pass 'this' (server) so service can access manager
        this.simulationService = new org.swarmforge.server.grpc.SimulationServiceImpl(this);
        this.matchmakingService = new org.swarmforge.server.grpc.MatchmakingServiceImpl(this.simulationManager);
        this.leaderboardService = new org.swarmforge.server.grpc.LeaderboardServiceImpl();

        // Initialize REST API
        this.restApiServer = new org.swarmforge.server.rest.RestApiServer(config.grpcPort() + 1000);
        this.restApiServer.setSimulation(mainSim); // REST API currently tied to main
    }

    // ... constructors ...

    public void start() throws Exception {
        LOG.info("Starting SwarmForge Server...");

        // Check Infrastructure & Auto-Start
        try {
            boolean postgresRunning = isPortOpen("localhost", 5432);
            boolean redisRunning = isPortOpen("localhost", 6379);

            if (!postgresRunning || !redisRunning) {
                LOG.warn("Infrastructure (Postgres/Redis) appears down. Attempting auto-start...");
                ProcessBuilder pb = new ProcessBuilder("docker-compose", "up", "-d");
                pb.inheritIO();
                Process p = pb.start();
                int exitCode = p.waitFor();
                
                if (exitCode == 0) {
                    LOG.info("Auto-start command executed. Waiting for services to initialize...");
                    for (int i = 0; i < 10; i++) {
                         Thread.sleep(1000);
                         if (isPortOpen("localhost", 5432) && isPortOpen("localhost", 6379)) {
                             LOG.info("Services are now reachable.");
                             break;
                         }
                         LOG.info("Waiting for ports...");
                    }
                } else {
                    LOG.error("Failed to auto-start infrastructure (exit code " + exitCode + ")");
                }
            }
        } catch (Exception e) {
            LOG.warn("Auto-start check failed: " + e.getMessage());
        }

        // Connect to databases
        // Auto-Start Check
        try {
            boolean postgresRunning = isPortOpen("localhost", 5432);
            boolean redisRunning = isPortOpen("localhost", 6379);

            if (!postgresRunning || !redisRunning) {
                LOG.warn("Infrastructure (Postgres/Redis) appears down. Attempting auto-start...");
                ProcessBuilder pb = new ProcessBuilder("docker-compose", "up", "-d");
                pb.inheritIO();
                Process p = pb.start();
                int exitCode = p.waitFor();
                
                if (exitCode == 0) {
                    LOG.info("Auto-start command executed. Waiting for services to initialize...");
                    for (int i = 0; i < 10; i++) {
                         Thread.sleep(1000);
                         if (isPortOpen("localhost", 5432) && isPortOpen("localhost", 6379)) {
                             LOG.info("Services are now reachable.");
                             break;
                         }
                         LOG.info("Waiting for ports...");
                    }
                } else {
                    LOG.error("Failed to auto-start infrastructure (exit code " + exitCode + ")");
                }
            }
        } catch (Exception e) {
            LOG.warn("Auto-start check failed: " + e.getMessage());
        }

        try {
            database.connect();
            LOG.info("Database connected successfully");
        } catch (Exception e) {
            LOG.warn("Database connection failed (running in offline mode): {}", e.getMessage());
        }

        try {
            cache.connect();
            LOG.info("Redis connected successfully");
        } catch (Exception e) {
            LOG.warn("Redis connection failed (caching disabled): {}", e.getMessage());
        }

        // TLS Setup
        SslContext sslContext = null;
        try {
            // Generate self-signed certificate for development
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslContext = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(io.netty.handler.ssl.SslProvider.OPENSSL) // Use tcnative if
                                                                                                // available
                    .build();
            LOG.info("TLS Enabled using self-signed certificate (SHA256: {})", ssc.certificate().getName());
        } catch (Exception e) {
            LOG.warn("Failed to initialize Native TLS (OpenSSL), falling back to JDK SSL or insecure: "
                    + e.getMessage());
            try {
                // Fallback to JDK provider if OpenSSL fails
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslContext = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey()).build();
                LOG.info("TLS Enabled using JDK SSL (Slower, but working)");
            } catch (Exception e2) {
                LOG.error("Could not initialize TLS at all: " + e2.getMessage());
                throw new RuntimeException("TLS init failed", e2);
            }
        }

        this.grpcServer = NettyServerBuilder.forPort(grpcPort)
                .sslContext(sslContext)
                .intercept(new org.swarmforge.server.security.JwtServerInterceptor()) // Register Interceptor
                .addService(new org.swarmforge.server.grpc.AuthServiceImpl()) // Register Auth Service
                .addService(simulationService)
                .addService(matchmakingService)
                .addService(leaderboardService)
                .addService(new org.swarmforge.server.ai.MockRLService())
                // 1-160).
                // Line 145 in original code had `getSimulation()`.
                // I need to be careful.
                .build()
                .start();
        LOG.info("gRPC Server (Secure) started on port " + grpcPort);

        // Start REST API
        try

        {
            this.restApiServer.start();
        } catch (

        Exception e) {
            LOG.warn("Failed to start REST API: " + e.getMessage());
        }

        // Start WebSocket Server
        try {
            this.webSocketServer = new org.swarmforge.server.net.SwarmForgeWebSocketServer(8081, simulationManager);
            this.webSocketServer.start(); // Starts internally on own thread
        } catch (Exception e) {
            LOG.warn("Failed to start WebSocket Server: " + e.getMessage());
        }

        // Start Redis updater
        Thread.ofVirtual().name("redis-updater").start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (cache.isConnected()) {
                    for (var entry : simulationManager.getAllSimulations().entrySet()) {
                        String simId = entry.getKey();
                        Simulation sim = entry.getValue();

                        if (sim.getState() == Simulation.State.RUNNING) {
                            try {
                                long tick = sim.getTickCount();
                                cache.setTick(simId, tick); // Use simId as world key? cache key is usually world name.
                                // We'll use simId for now.

                                for (Colony colony : sim.getColonies()) {
                                    for (Individual ind : colony.getLivingIndividuals()) {
                                        cache.setIndividualPosition(simId, ind.getId().toString(),
                                                ind.getX(), ind.getY(), ind.getZ());
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Error updating Redis for " + simId + ": " + e.getMessage());
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(50); // 20 updates per second
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        printStatusBanner();

    }

    // ... existing logic ...

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
    public java.util.List<String> getConnectedClients() {
        // Delegate to service
        return simulationService.getConnectedClientIds();
    }

    public void kickClient(String clientId) {
        simulationService.kickClient(clientId);
        LOG.info("Kicked client: " + clientId);
    }

    // ... rest of class ...

    /**
     * Print a clear status banner showing service states.
     */
    private void printStatusBanner() {
        String dbStatus = database.isConnected() ? "✓ ONLINE" : "✗ OFFLINE";
        String dbColor = database.isConnected() ? "\u001B[32m" : "\u001B[31m"; // Green/Red
        String redisStatus = cache.isConnected() ? "✓ ONLINE" : "✗ OFFLINE";
        String redisColor = cache.isConnected() ? "\u001B[32m" : "\u001B[31m";
        String reset = "\u001B[0m";

        System.out.println();
        System.out.println("+------------------------------------------------------+");
        System.out.println("|          SWARMFORGE SERVER - STATUS                  |");
        System.out.println("+------------------------------------------------------+");
        System.out.println("|  gRPC Server    : \u001B[32m✓ RUNNING\u001B[0m  (port " + grpcPort + ")            |");
        System.out.println("|  PostgreSQL     : " + dbColor + dbStatus + reset + "                         |");
        System.out.println("|  Redis Cache    : " + redisColor + redisStatus + reset + "                         |");
        System.out.println("+------------------------------------------------------+");
        if (!database.isConnected()) {
            System.out.println("|  \u001B[33m⚠ Persistence disabled - run 'start-docker' first\u001B[0m   |");
        }
        if (!cache.isConnected()) {
            System.out.println("|  \u001B[33m⚠ Caching disabled - run 'start-docker' first\u001B[0m       |");
        }
        if (database.isConnected() && cache.isConnected()) {
            System.out.println("|  \u001B[32m✓ All services operational\u001B[0m                           |");
        }
        System.out.println("+------------------------------------------------------+");
        System.out.println();
    }

    public void createNewWorld(String name, String terrainType, String speciesType) {
        LOG.info("Creating new world: " + name + " (" + terrainType + ", " + speciesType + ")");

        Simulation simulation = getSimulation(); // Access main
        if (simulation == null)
            return;

        simulation.stop();
        terrarium.clear();
        simulation.reset(0);

        // 1. Terrain
        org.swarmforge.core.world.TerrainGenerator terrainGen = new org.swarmforge.core.world.TerrainGenerator();
        int groundLevel = terrarium.getDepth() - 10;

        float roughness = 8f;
        float scale = 0.03f;

        if (terrainType.contains("Flat")) {
            roughness = 1f;
            scale = 0.005f;
        } else if (terrainType.contains("Desert")) {
            roughness = 4f;
            scale = 0.02f;
        } else if (terrainType.contains("Hills")) {
            roughness = 12f;
            scale = 0.05f;
        }

        terrainGen.generate(terrarium, groundLevel, roughness, scale);

        // 2. Nest
        NestGenerator nestGen = new NestGenerator(terrarium);
        int centerX = terrarium.getWidth() / 2;
        int centerY = terrarium.getHeight() / 2;
        nestGen.generate(centerX, centerY, groundLevel - 5, NestGenerator.NestType.MATURE, 1.0f);

        // 3. Colony & Species
        org.swarmforge.core.species.Species species;
        if (speciesType != null && speciesType.contains("Atta")) {
            species = new org.swarmforge.core.species.AttaCephalotes();
        } else {
            species = new org.swarmforge.core.species.LasiusNiger(); // Default
        }

        Colony colony = new Colony(species, centerX, centerY, 50);
        colony.addIndividual(colony.createQueen());
        for (int i = 0; i < 50; i++) {
            Individual worker = colony.createWorker();
            worker.setPosition(centerX, centerY, groundLevel - 2);
            colony.addIndividual(worker);
        }
        simulation.addColony(colony);

        // 4. Persistence
        if (database.isConnected()) {
            saveWorld(name);
        }

        LOG.info("New world created and ready.");
    }

    public java.util.List<String> getAvailableWorlds() {
        if (database == null || !database.isConnected()) {
            return java.util.Collections.emptyList();
        }
        try {
            return database.worldRepository().findAll().stream()
                    .map(w -> w.name() + " [" + w.id() + "]")
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            LOG.error("Failed to list worlds", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Create a demo world with a colony.
     */
    public void createDemoWorld() {
        createNewWorld("Demo World", "Perlin Hills", "Lasius niger");

        // Add extra stuff for demo
        int rivalX = terrarium.getWidth() - 30;
        int rivalY = terrarium.getHeight() - 30;
        int groundLevel = terrarium.getDepth() - 10;
        Colony rivalColony = new Colony(new org.swarmforge.core.species.AttaCephalotes(), rivalX, rivalY,
                groundLevel - 5);
        rivalColony.addIndividual(rivalColony.createQueen());
        for (int i = 0; i < 30; i++) {
            Individual worker = rivalColony.createWorker();
            worker.setPosition(rivalX, rivalY, groundLevel - 2);
            rivalColony.addIndividual(worker);
        }
        // spawn rival (assuming main simulation)
        Simulation simulation = getSimulation();
        if (simulation != null) {
            simulation.addColony(rivalColony);
        }

        // Spawn food
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            float fx = rand.nextFloat() * terrarium.getWidth();
            float fy = rand.nextFloat() * terrarium.getHeight();
            // Assuming current simulation is the one createNewWorld set up (which operates
            // on main)
            Simulation sim = getSimulation();
            if (sim != null)
                sim.spawnFood(fx, fy, groundLevel, 10 + rand.nextFloat() * 20,
                        org.swarmforge.core.domain.ResourceType.SEED);
        }
    }

    /**
     * Stop the server.
     */
    public void stop() {
        LOG.info("Stopping SwarmForge Server...");
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
        if (simulationManager != null)
            simulationManager.stopAll();
        if (cache != null)
            cache.disconnect();
        if (database != null)
            database.disconnect();
        if (webSocketServer != null) {
            try {
                webSocketServer.stop();
            } catch (Exception e) {
                LOG.warn("Error stopping WebSocket server: " + e.getMessage());
            }
        }
        LOG.info("Server stopped");
    }

    /**
     * Save the current world state.
     */
    public void saveWorld(String name) {
        LOG.info("Saving world: " + name);
        try {
            Simulation simulation = getSimulation();
            if (simulation == null)
                return;

            org.swarmforge.server.persistence.SimulationSerializer serializer = new org.swarmforge.server.persistence.SimulationSerializer();

            // Serialize data
            byte[] cellsData = serializer.serializeCells(terrarium);
            byte[] coloniesData = serializer.serializeColonies(simulation.getColonies());
            // Individuals are inside colonies for JSON, but let's check repo expectation
            // Repo takes separate individuals_data, but our serializer might bundle them.
            // For now, pass null for separate individuals_data if colonies cover it,
            // or implement separate serialization if needed.
            byte[] individualsData = serializer.serializeIndividuals(new java.util.ArrayList<>()); // Placeholder

            // Save world metadata
            java.util.UUID worldId = database.worldRepository().save(
                    name, terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth(),
                    0, 0, 0); // Lat/Long/Alt placeholders

            // Save checkpoint
            database.checkpointRepository().save(
                    worldId, simulation.getTickCount(), "Manual Save",
                    cellsData, coloniesData, individualsData);

            LOG.info("World saved successfully: " + worldId);
        } catch (Exception e) {
            LOG.error("Failed to save world: " + e.getMessage(), e);
        }
    }

    /**
     * Load a world state.
     */
    public void loadWorld(String worldIdStr) {
        LOG.info("Loading world: " + worldIdStr);
        try {
            java.util.UUID worldId = java.util.UUID.fromString(worldIdStr);

            // 1. Find latest checkpoint
            java.util.List<org.swarmforge.server.persistence.CheckpointRepository.CheckpointSummary> checkpoints = database
                    .checkpointRepository().findByWorld(worldId);

            if (checkpoints.isEmpty()) {
                throw new Exception("No checkpoints found for world " + worldIdStr);
            }

            java.util.UUID checkpointId = checkpoints.get(0).id();
            var checkpointOpt = database.checkpointRepository().findById(checkpointId);

            if (checkpointOpt.isEmpty()) {
                throw new Exception("Checkpoint data missing for " + checkpointId);
            }

            var checkpoint = checkpointOpt.get();

            Simulation simulation = getSimulation();
            if (simulation == null)
                return;

            // 2. Stop simulation
            boolean wasRunning = simulation.getState() == Simulation.State.RUNNING;
            simulation.stop();
            // Wait a bit for threads to stop? separate thread logic handles flag check.
            Thread.sleep(100);

            // 3. Deserialize
            org.swarmforge.server.persistence.SimulationSerializer serializer = new org.swarmforge.server.persistence.SimulationSerializer();
            java.util.Collection<org.swarmforge.core.domain.TerrariumCell> cells = serializer
                    .deserializeCells(checkpoint.cellsData());
            java.util.Collection<Colony> colonies = serializer.deserializeColonies(checkpoint.coloniesData());

            // 4. Reset World
            terrarium.clear();
            for (org.swarmforge.core.domain.TerrariumCell cell : cells) {
                terrarium.setCell(cell);
            }

            // 5. Reset Simulation
            simulation.reset(checkpoint.tick());
            for (Colony colony : colonies) {
                simulation.addColony(colony);
            }

            LOG.info("World loaded successfully. Tick: " + checkpoint.tick());

            // 6. Resume if was running
            if (wasRunning) {
                simulation.start();
            }

        } catch (Exception e) {
            LOG.error("Failed to load world: " + e.getMessage(), e);
            throw new RuntimeException(e); // Propagate to gRPC
        }
    }

    public Terrarium getTerrarium() {
        return terrarium; // Main
    }

    public Simulation getSimulation() {
        return simulationManager.getSimulation("main").orElse(null);
    }

    public org.swarmforge.server.simulation.SimulationManager getSimulationManager() {
        return simulationManager;
    }

    /**
     * Check if database is connected.
     */
    public boolean isDatabaseConnected() {
        return database != null && database.isConnected();
    }

    /**
     * Check if Redis cache is connected.
     */
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

    /**
     * Get server uptime in seconds.
     */
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    private long startTime = System.currentTimeMillis();

    public org.swarmforge.server.compute.ComputeClusterManager getClusterManager() {
        return clusterManager;
    }

    public static void main(String[] args) {
        // Parse command-line arguments
        boolean createDemo = false;
        boolean noDb = false;
        boolean listSims = false;
        String runSimulation = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help", "-h" -> {
                    printHelp();
                    return;
                }
                case "--create-demo" -> createDemo = true;
                case "--no-db" -> noDb = true;
                case "--list" -> listSims = true;
                case "--run" -> {
                    if (i + 1 < args.length) {
                        runSimulation = args[++i];
                    } else {
                        System.err.println("Error: --run requires a simulation name");
                        System.exit(1);
                    }
                }
                default -> {
                    if (args[i].startsWith("-")) {
                        System.err.println("Unknown option: " + args[i]);
                        printHelp();
                        System.exit(1);
                    }
                }
            }
        }

        try {
            ServerConfig config = noDb ? ServerConfig.offline() : ServerConfig.fromEnvironment();
            SwarmForgeServer server = new SwarmForgeServer(config);

            if (listSims) {
                server.listSimulations();
                return;
            }

            if (createDemo || runSimulation == null) {
                server.createDemoWorld();
            } else {
                server.loadWorld(runSimulation);
            }

            server.start();

            // Interactive Console Loop
            System.out.println("SwarmForge Server ready. Type 'help' for commands.");

            try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
                boolean running = true;
                while (running) {
                    System.out.print("> ");
                    String line = "";
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine().trim();
                    } else {
                        break;
                    }

                    if (line.isEmpty())
                        continue;

                    String[] parts = line.split("\\s+");
                    String cmd = parts[0].toLowerCase();

                    switch (cmd) {
                        case "help", "?" -> printHelpCommands();
                        case "shutdown", "exit", "stop" -> {
                            server.stop();
                            running = false;
                        }
                        case "status" -> server.printStatusBanner();
                        case "list" -> server.listSimulations();
                        case "save" -> server.saveWorld("console_save");
                        case "info" -> server.printSimulationInfo();
                        default -> System.out.println("Unknown command: " + cmd + " (type 'help' for usage)");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Server error: " + e.getMessage(), e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelpCommands() {
        System.out.println("""
                Available Commands:
                  status      - Show server health and connection status
                  info        - Show current simulation statistics
                  list        - List available simulations in database
                  stop / exit - Stop the server and exit
                  help        - Show this message
                """);
    }

    public void printSimulationInfo() {
        System.out.println("\n=== Multi-Simulation Info ===");
        simulationManager.getAllSimulations().forEach((id, sim) -> {
            System.out.println("ID: " + id);
            System.out.println("  Tick: " + sim.getTickCount());
            System.out.println("  State: " + sim.getState());
            System.out.println("  Colonies: " + sim.getColonies().size());
            int totalPop = sim.getColonies().stream().mapToInt(Colony::getPopulation).sum();
            System.out.println("  Population: " + totalPop);
        });
        System.out.println("===========================\n");
    }

    private static void printHelp() {
        System.out.println("""
                +------------------------------------------------------+
                |           SWARMFORGE SERVER - HELP                   |
                +------------------------------------------------------+
                |  Usage: java -jar swarmforge-server.jar [OPTIONS]    |
                +------------------------------------------------------+
                |  Options:                                            |
                |    --help, -h       Show this help message           |
                |    --create-demo    Create and run demo world        |
                |    --run <name>     Run named simulation from DB     |
                |    --list           List available simulations       |
                |    --no-db          Run without database (offline)   |
                +------------------------------------------------------+
                """);
    }

    private void listSimulations() {
        System.out.println("\n=== Available Simulations ===");
        if (!database.isConnected()) {
            try {
                database.connect();
            } catch (Exception e) {
                System.err.println("Cannot list simulations: Database not connected");
                System.err.println("Hint: Start Docker with 'scripts/start-docker'");
                return;
            }
        }
        try {
            var worlds = database.worldRepository().findAll();
            if (worlds.isEmpty()) {
                System.out.println("No simulations found. Use --create-demo to create one.");
            } else {
                System.out.println("ID                                   | Name                | Size");
                System.out.println("-".repeat(70));
                for (var world : worlds) {
                    System.out.printf("%-36s | %-19s | %dx%dx%d%n",
                            world.id(), world.name(),
                            world.width(), world.height(), world.depth());
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing simulations: " + e.getMessage());
        }
        System.out.println();
    }

    public io.grpc.Server getGrpcServer() {
        return grpcServer;
    }

    public org.swarmforge.server.persistence.DatabaseManager getDatabaseManager() {
        return database;
    }

    private boolean isPortOpen(String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
