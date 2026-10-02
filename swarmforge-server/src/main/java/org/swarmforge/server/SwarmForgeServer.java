/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
    private final ServerConfig config;
    private io.grpc.Server grpcServer;

    public SwarmForgeServer(ServerConfig config) {
        this.config = config;
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
        try {
            loadAcademicScenario(org.swarmforge.core.scenario.AcademicScenarios.createLevyVsBrownianScenario(42L));
        } catch (Exception e) {
            LOG.warn("Could not load initial academic scenario: {}", e.getMessage());
        }

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

    private final java.util.concurrent.ExecutorService dbAsyncExecutor = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "db-async-writer");
        t.setDaemon(true);
        return t;
    });

    // ... constructors ...

    public void start() throws Exception {
        try {
            LOG.info("Starting SwarmForge Server...");

        // Check Infrastructure & Auto-Start (only if database or redis host is configured)
        if (config.dbHost() != null && !config.dbHost().trim().isEmpty() && config.dbPort() > 0) {
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
                            LOG.warn("Could not invoke docker-compose or docker compose: " + ex.getMessage());
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
                            LOG.warn("Auto-start Docker infrastructure exited with code " + exitCode);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn("Auto-start check skipped or failed: " + e.getMessage());
            }
        }

        try {
            database.connect();
            if (database.isConnected()) {
                LOG.info("Database connected successfully");
                Simulation sim = getSimulation();
                if (sim != null) {
                    saveWorld("Initial World State");
                }
            } else {
                LOG.info("Database running in fallback mode");
            }
        } catch (Exception e) {
            LOG.warn("Database connection failed (running in offline mode): {}", e.getMessage());
        }

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

        // TLS Setup (enabled when ENABLE_TLS=true)
        SslContext sslContext = null;
        if ("true".equalsIgnoreCase(System.getenv("ENABLE_TLS"))) {
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslContext = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey())
                        .sslProvider(io.netty.handler.ssl.SslProvider.OPENSSL)
                        .build();
                LOG.info("TLS Enabled using self-signed certificate");
            } catch (Throwable e) {
                LOG.warn("Failed to initialize Native TLS (OpenSSL), trying JDK SSL: " + e.getMessage());
                try {
                    SelfSignedCertificate ssc = new SelfSignedCertificate();
                    sslContext = GrpcSslContexts.forServer(ssc.certificate(), ssc.privateKey()).build();
                    LOG.info("TLS Enabled using JDK SSL");
                } catch (Throwable e2) {
                    LOG.warn("Could not initialize TLS, running plaintext: " + e2.getMessage());
                    sslContext = null;
                }
            }
        } else {
            LOG.info("gRPC Server running in plaintext mode (set ENABLE_TLS=true to activate TLS)");
        }

        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(grpcPort)
                .maxInboundMessageSize(16 * 1024 * 1024) // 16 MB max payload size
                .maxInboundMetadataSize(64 * 1024);     // 64 KB metadata limit
        if (sslContext != null) {
            serverBuilder.sslContext(sslContext);
        }

        this.grpcServer = serverBuilder
                .intercept(new org.swarmforge.server.security.JwtServerInterceptor())
                .addService(new org.swarmforge.server.grpc.AuthServiceImpl())
                .addService(simulationService)
                .addService(matchmakingService)
                .addService(leaderboardService)
                .addService(new org.swarmforge.server.ai.FallbackRLService())
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

        // Start WebSocket Server (Port 8081 or next available port)
        try {
            int wsPort = 8081;
            boolean bound = false;
            for (int p = wsPort; p < wsPort + 10; p++) {
                if (!isPortOpen("localhost", p)) {
                    try {
                        this.webSocketServer = new org.swarmforge.server.net.SwarmForgeWebSocketServer(p, simulationManager);
                        this.webSocketServer.start();
                        bound = true;
                        LOG.info("WebSocket Server started on port " + p);
                        break;
                    } catch (Exception ex) {
                        LOG.warn("Could not bind WebSocket on port " + p + ", trying next: " + ex.getMessage());
                    }
                }
            }
            if (!bound) {
                LOG.warn("Could not find an available port for WebSocket Server (8081-8090 are busy). WebSocket streaming disabled.");
            }
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

        // Start Periodic Database Auto-Checkpointer (Every 10 seconds for replays)
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
                    break;
                } catch (Exception ex) {
                    LOG.warn("Auto-checkpoint failed: " + ex.getMessage());
                }
            }
        });

        printStatusBanner();
        } catch (Throwable t) {
            LOG.error("SERVER STARTUP FAILED CRITICALLY: ", t);
            throw t;
        }
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
        String dbStatus = database.isConnected() 
                ? (database.isH2Fallback() ? "[OK] H2 (MEM)" : "[OK] POSTGRES") 
                : "[X] OFFLINE";
        String dbColor = database.isConnected() ? "\u001B[32m" : "\u001B[31m"; // Green/Red
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
            species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies("atta-cephalotes");
        } else {
            species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies("lasius-niger"); // Default
        }

        Colony colony = new Colony(species, centerX, centerY, 50);
        colony.addIndividual(colony.createQueen());
        for (int i = 0; i < 50; i++) {
            Individual worker = colony.createWorker();
            worker.setPosition(centerX, centerY, groundLevel - 2);
            colony.addIndividual(worker);
        }
        simulation.addColony(colony);

        // 4. Persistence & Start
        if (database.isConnected()) {
            saveWorld(name);
        }
        simulation.start();
        LOG.info("New world created, running and ready.");
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
     * Load and configure an academic research scenario into the main simulation.
     */
    public void loadAcademicScenario(org.swarmforge.core.scenario.Scenario scenario) {
        if (scenario == null) return;
        LOG.info("Loading Academic Scenario: {} ({})", scenario.getTitle(), scenario.getId());

        Simulation simulation = getSimulation();
        if (simulation == null) return;

        simulation.stop();
        terrarium.clear();
        simulation.reset(0);
        simulation.setMasterSeed(scenario.getMasterSeed());

        // 1. Terrain generation according to Biome
        org.swarmforge.core.world.TerrainGenerator terrainGen = new org.swarmforge.core.world.TerrainGenerator();
        int groundLevel = terrarium.getDepth() - 10;
        float roughness = 8f;
        float scale = 0.03f;

        String biome = scenario.getBiomeName() != null ? scenario.getBiomeName().toUpperCase() : "TEMPERATE";
        if (biome.contains("DESERT") || biome.contains("STEPPE") || biome.contains("ARID")) {
            roughness = 4f;
            scale = 0.02f;
        } else if (biome.contains("ALPINE") || biome.contains("MOUNTAIN") || biome.contains("HILLS") || biome.contains("TAIGA")) {
            roughness = 14f;
            scale = 0.05f;
        } else if (biome.contains("WETLAND") || biome.contains("SAVANNA")) {
            roughness = 3f;
            scale = 0.01f;
        }

        terrainGen.generate(terrarium, groundLevel, roughness, scale);

        // 2. Weather & Day/Night
        if (simulation.getWeather() != null) {
            simulation.getWeather().setTemperature(scenario.getInitialTemperature());
            simulation.getWeather().setHumidity(scenario.getInitialHumidity() * 100f);
        }

        // 3. Colonies & Species
        int totalColonies = scenario.getColonies().size();
        for (int i = 0; i < totalColonies; i++) {
            var colSetup = scenario.getColonies().get(i);
            String spName = colSetup.speciesName();
            org.swarmforge.core.species.Species species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(spName);

            // Position calculation
            int posX = (totalColonies == 1) ? (terrarium.getWidth() / 2) : (int) ((i + 1) * (terrarium.getWidth() / (totalColonies + 1.0f)));
            int posY = (totalColonies == 1) ? (terrarium.getHeight() / 2) : (int) ((i + 1) * (terrarium.getHeight() / (totalColonies + 1.0f)));

            // Nest Generation
            NestGenerator nestGen = new NestGenerator(terrarium);
            nestGen.generate(posX, posY, groundLevel - 5, NestGenerator.NestType.MATURE, 1.0f);

            Colony colony = new Colony(species, posX, posY, groundLevel - 5);
            for (int q = 0; q < colSetup.queenCount(); q++) {
                colony.addIndividual(colony.createQueen());
            }
            for (int w = 0; w < colSetup.workerCount(); w++) {
                Individual worker = colony.createWorker();
                worker.setPosition(posX, posY, groundLevel - 2);
                colony.addIndividual(worker);
            }
            for (int s = 0; s < colSetup.soldierCount(); s++) {
                Individual soldier = colony.createSoldier();
                soldier.setPosition(posX, posY, groundLevel - 2);
                colony.addIndividual(soldier);
            }

            simulation.addColony(colony);
        }

        // 4. Food Patches
        int foodCount = Math.max(10, scenario.getFoodPatchesCount());
        java.util.Random rand = new java.util.Random(scenario.getMasterSeed());
        for (int f = 0; f < foodCount; f++) {
            float fx = 10 + rand.nextFloat() * (terrarium.getWidth() - 20);
            float fy = 10 + rand.nextFloat() * (terrarium.getHeight() - 20);
            simulation.spawnFood(fx, fy, groundLevel, 15 + rand.nextFloat() * 30, org.swarmforge.core.domain.ResourceType.SUGAR);
        }

        // 5. Start simulation
        simulation.start();
        if (database.isConnected()) {
            saveWorld(scenario.getTitle());
        }
        LOG.info("Scenario '{}' initialized and running (Colonies: {}, Population: {})",
                scenario.getTitle(), simulation.getColonies().size(),
                simulation.getColonies().stream().mapToInt(Colony::getPopulation).sum());
    }

    /**
     * Run an autonomous headless batch campaign for scientific experiments,
     * recording snapshots to disk and database, then exiting.
     */
    public void runBatchCampaign(org.swarmforge.core.scenario.Scenario scenario, long targetTicks, String exportDir, int snapshotInterval) {
        LOG.info("==========================================================");
        LOG.info("   🐜 SWARMFORGE — HEADLESS BATCH SIMULATION CAMPAIGN     ");
        LOG.info("==========================================================");
        LOG.info("Scenario: {} ({})", scenario != null ? scenario.getTitle() : "Demo", scenario != null ? scenario.getId() : "demo");
        LOG.info("Target Ticks: {} | Snapshot Interval: {} | Export Dir: {}", targetTicks, snapshotInterval, exportDir);

        if (scenario != null) {
            loadAcademicScenario(scenario);
        } else {
            createDemoWorld();
        }

        Simulation sim = getSimulation();
        if (sim == null) {
            LOG.error("No active simulation to run in batch mode!");
            return;
        }

        java.io.File dir = new java.io.File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        long startNanos = System.nanoTime();
        long lastReportNanos = startNanos;
        long lastReportTick = 0;

        java.util.List<java.util.Map<String, Object>> timeSeriesData = new java.util.ArrayList<>();

        // Start persistence if available
        try {
            database.connect();
        } catch (Exception ignored) {}

        LOG.info("Executing batch simulation ticks...");
        for (long t = 0; t < targetTicks; t++) {
            sim.tick();

            long currentTick = sim.getTickCount();
            if (currentTick % snapshotInterval == 0 || currentTick == targetTicks) {
                // Record telemetry metrics
                int totalPop = sim.getColonies().stream().mapToInt(Colony::getPopulation).sum();
                float totalFood = (float) sim.getColonies().stream().mapToDouble(Colony::getFoodStored).sum();
                float totalBiomass = (float) sim.getColonies().stream().mapToDouble(Colony::getTotalBiomass).sum();

                java.util.Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
                snapshot.put("tick", currentTick);
                snapshot.put("simTimeSeconds", sim.getAccumulatedSimulationSeconds());
                snapshot.put("population", totalPop);
                snapshot.put("foodStored", totalFood);
                snapshot.put("totalBiomass", totalBiomass);
                snapshot.put("coloniesCount", sim.getColonies().size());
                timeSeriesData.add(snapshot);

                // Save checkpoint to database
                if (database.isConnected()) {
                    try {
                        saveWorld("Batch Save (Tick " + currentTick + ")");
                    } catch (Exception ignored) {}
                }

                // Periodic console progress
                long now = System.nanoTime();
                if (now - lastReportNanos >= 2_000_000_000L || currentTick == targetTicks) {
                    double tps = (currentTick - lastReportTick) / ((now - lastReportNanos) / 1_000_000_000.0);
                    double progressPct = (currentTick * 100.0) / targetTicks;
                    LOG.info(String.format(java.util.Locale.US, "Progress: %.1f%% (%d/%d ticks) | Speed: %.1f TPS | Population: %d | Biomass: %.2f g",
                            progressPct, currentTick, targetTicks, tps, totalPop, totalBiomass));
                    lastReportNanos = now;
                    lastReportTick = currentTick;
                }
            }
        }

        long totalNanos = System.nanoTime() - startNanos;
        double totalSeconds = totalNanos / 1_000_000_000.0;
        double avgTps = targetTicks / Math.max(0.001, totalSeconds);

        LOG.info("==========================================================");
        LOG.info("   ✅ BATCH SIMULATION CAMPAIGN COMPLETED                 ");
        LOG.info("==========================================================");
        LOG.info(String.format(java.util.Locale.US, "Total Ticks: %d in %.2f s (Average Speed: %.1f TPS)", targetTicks, totalSeconds, avgTps));

        // Export summary JSON
        try {
            java.util.Map<String, Object> runReport = new java.util.LinkedHashMap<>();
            runReport.put("scenarioId", scenario != null ? scenario.getId() : "demo");
            runReport.put("scenarioTitle", scenario != null ? scenario.getTitle() : "Demo World");
            runReport.put("totalTicks", targetTicks);
            runReport.put("elapsedSeconds", totalSeconds);
            runReport.put("averageTPS", avgTps);
            runReport.put("finalPopulation", sim.getColonies().stream().mapToInt(Colony::getPopulation).sum());
            runReport.put("timeSeries", timeSeriesData);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            java.io.File reportFile = new java.io.File(dir, "batch_report.json");
            mapper.writeValue(reportFile, runReport);
            LOG.info("Batch report exported to: {}", reportFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Failed to write batch report JSON: " + e.getMessage(), e);
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
        Colony rivalColony = new Colony(org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies("atta-cephalotes"), rivalX, rivalY,
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
        if (restApiServer != null) {
            try {
                restApiServer.stop();
            } catch (Exception e) {
                LOG.warn("Error stopping REST API server: " + e.getMessage());
            }
        }
        if (webSocketServer != null) {
            try {
                webSocketServer.stop(500);
            } catch (Exception e) {
                LOG.warn("Error stopping WebSocket server: " + e.getMessage());
            }
            webSocketServer = null;
        }
        LOG.info("Server stopped");
    }

    /**
     * Save the current world state.
     */
    public void saveWorld(String name) {
        if (!database.isConnected()) {
            return;
        }
        try {
            Simulation simulation = getSimulation();
            if (simulation == null) {
                LOG.warn("Cannot save world: simulation is null");
                return;
            }
            Terrarium targetTerrarium = simulation.getTerrarium() != null ? simulation.getTerrarium() : this.terrarium;
            if (targetTerrarium == null) {
                LOG.warn("Cannot save world: terrarium is null");
                return;
            }

            org.swarmforge.server.persistence.SimulationSerializer serializer = new org.swarmforge.server.persistence.SimulationSerializer();

            // 1. Serialize memory buffers rapidly (<1ms)
            byte[] cellsData = serializer.serializeCells(targetTerrarium);
            byte[] coloniesData = serializer.serializeColonies(simulation.getColonies());
            byte[] individualsData = serializer.serializeIndividuals(new java.util.ArrayList<>());
            long tickCount = simulation.getTickCount();
            int width = targetTerrarium.getWidth();
            int height = targetTerrarium.getHeight();
            int depth = targetTerrarium.getDepth();
            java.util.List<Colony> coloniesCopy = new java.util.ArrayList<>(simulation.getColonies());

            // 2. Offload PostgreSQL I/O to background async writer thread (Zero tick stall)
            dbAsyncExecutor.submit(() -> {
                try {
                    java.util.UUID worldId = database.worldRepository().save(
                            name, width, height, depth, 0, 0, 0);

                    for (Colony col : coloniesCopy) {
                        try {
                            database.colonyRepository().save(col, "System");
                        } catch (Exception ignored) {}
                    }

                    database.checkpointRepository().save(
                            worldId, tickCount, name,
                            cellsData, coloniesData, individualsData);

                    LOG.info("Async world save completed: " + worldId + " (Tick " + tickCount + ")");
                } catch (Exception ex) {
                    LOG.error("Failed to async save world to database: " + ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            LOG.error("Failed to serialize world for saving: " + e.getMessage(), e);
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

    public void listAcademicScenarios() {
        System.out.println("\n=== Scénarios Académiques & Scientifiques Disponibles (SwarmForge) ===");
        var list = org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(42L);
        for (int i = 0; i < list.size(); i++) {
            var s = list.get(i);
            System.out.printf(" [%2d] %-34s | %s\n      Catégorie : %s (Colonies: %d, Biome: %s)\n",
                    (i + 1), s.getId(), s.getTitle(), s.getAcademicCategory(), s.getColonies().size(), s.getBiomeName());
        }
        System.out.println("=======================================================================\n");
    }

    public static void main(String[] args) {
        // Parse command-line arguments
        boolean createDemo = false;
        boolean listSims = false;
        boolean listScenarios = false;
        boolean batchMode = "true".equalsIgnoreCase(System.getenv("BATCH_MODE")) || "batch".equalsIgnoreCase(System.getenv("MODE"));
        long batchTicks = 1000;
        int snapshotInterval = 50;
        String exportDir = System.getenv().getOrDefault("EXPORT_DIR", "data/results");
        try {
            String envTicks = System.getenv("TICKS");
            if (envTicks != null && !envTicks.isBlank()) batchTicks = Long.parseLong(envTicks);
        } catch (Exception ignored) {}

        boolean noGui = "true".equalsIgnoreCase(System.getenv("NOGUI")) || Boolean.getBoolean("java.awt.headless") || batchMode;
        String dbMode = (System.getenv("DB_HOST") != null && !System.getenv("DB_HOST").trim().isEmpty()) ? "postgres" : "local";
        String runSimulation = null;
        String scenarioArg = System.getenv("SCENARIO");
        long masterSeed = 42L;

        // Parse global engine preferences and acceleration options first
        org.swarmforge.core.engine.EnginePreferences.applyCommandLineArgs(args);

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--ticks=")) {
                batchTicks = Long.parseLong(arg.substring("--ticks=".length()));
                batchMode = true;
                continue;
            }
            if (arg.startsWith("--scenario=")) {
                scenarioArg = arg.substring("--scenario=".length());
                continue;
            }
            if (arg.startsWith("--export-dir=")) {
                exportDir = arg.substring("--export-dir=".length());
                continue;
            }
            if (arg.startsWith("--engine=") || arg.startsWith("--accel=") || arg.startsWith("--threads=")) {
                continue;
            }

            switch (arg) {
                case "--help", "-h" -> {
                    printHelp();
                    return;
                }
                case "--batch", "--headless" -> batchMode = true;
                case "--create-demo"         -> createDemo = true;
                case "--list"               -> listSims = true;
                case "--list-scenarios"     -> listScenarios = true;
                case "--local", "--no-db"   -> dbMode = "local";
                case "--postgres"           -> dbMode = "postgres";
                case "--offline"            -> dbMode = "offline";
                case "--nogui"              -> noGui = true;
                case "--engine", "--accel", "--threads" -> {
                    if (i + 1 < args.length) i++; // skipped because processed in applyCommandLineArgs
                }
                case "--rust", "--java", "--gpu", "--cpu", "--single-core", "--multi-core", "--monocoeur", "--multicoeur" -> {
                    // processed in applyCommandLineArgs
                }
                case "--ticks" -> {
                    if (i + 1 < args.length) {
                        batchTicks = Long.parseLong(args[++i]);
                        batchMode = true;
                    }
                }
                case "--snapshot-interval" -> {
                    if (i + 1 < args.length) {
                        snapshotInterval = Integer.parseInt(args[++i]);
                    }
                }
                case "--export-dir" -> {
                    if (i + 1 < args.length) {
                        exportDir = args[++i];
                    }
                }
                case "--seed" -> {
                    if (i + 1 < args.length) {
                        try {
                            masterSeed = Long.parseLong(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seed: " + args[i]);
                        }
                    }
                }
                case "--scenario", "-s" -> {
                    if (i + 1 < args.length) {
                        scenarioArg = args[++i];
                    } else {
                        System.err.println("Error: --scenario requires a scenario ID or index (1-16)");
                        System.exit(1);
                    }
                }
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

        if (listScenarios) {
            new SwarmForgeServer(ServerConfig.offline()).listAcademicScenarios();
            return;
        }

        try {
            ServerConfig config = switch (dbMode) {
                case "postgres" -> ServerConfig.fromEnvironment();
                case "offline"  -> ServerConfig.offline();
                default         -> ServerConfig.local(); // local = localhost PG → H2 fallback
            };

            LOG.info("Server mode: {} | GUI: {} | Batch: {}", dbMode, noGui ? "disabled" : "enabled", batchMode);

            SwarmForgeServer server = new SwarmForgeServer(config);

            if (listSims) {
                server.listSimulations();
                return;
            }

            org.swarmforge.core.scenario.Scenario matchedScenario = null;
            if (scenarioArg != null) {
                var scenarios = org.swarmforge.core.scenario.AcademicScenarios.getAllAcademicScenarios(masterSeed);
                // Try index first (e.g. 1 to 16)
                try {
                    int idx = Integer.parseInt(scenarioArg);
                    if (idx >= 1 && idx <= scenarios.size()) {
                        matchedScenario = scenarios.get(idx - 1);
                    }
                } catch (NumberFormatException ignored) {}

                // Try ID or name matching
                if (matchedScenario == null) {
                    for (var sc : scenarios) {
                        if (sc.getId().equalsIgnoreCase(scenarioArg) ||
                            sc.getTitle().toLowerCase().contains(scenarioArg.toLowerCase()) ||
                            scenarioArg.toLowerCase().contains(sc.getId().toLowerCase())) {
                            matchedScenario = sc;
                            break;
                        }
                    }
                }
            }

            if (batchMode) {
                server.runBatchCampaign(matchedScenario, batchTicks, exportDir, snapshotInterval);
                System.exit(0);
                return;
            }

            if (matchedScenario != null) {
                server.loadAcademicScenario(matchedScenario);
            } else if (createDemo || runSimulation == null) {
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
                        case "scenarios" -> server.listAcademicScenarios();
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
                  scenarios   - List all 16 academic research scenario presets
                  save        - Save current simulation state
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
                |  Compute Engine & Acceleration options:              |
                |    --engine <auto|java|rust> Engine backend selector |
                |    --rust / --java           Shortcut engine flags   |
                |    --accel <auto|gpu|cpu>    Hardware compute accel  |
                |    --gpu / --cpu             Shortcut accel flags    |
                |    --threads <N>             Worker thread count     |
                |    --single-core / --monocoeur Force 1 core          |
                |    --multi-core / --multicoeur Max available cores   |
                |                                                      |
                |  Database mode (choose one):                         |
                |    --local          Local mode: H2 fallback if no PG |
                |                     (DEFAULT — dev/monoposte)        |
                |    --postgres       PostgreSQL + Redis (production)  |
                |    --offline        No database at all               |
                |                                                      |
                |  Scenario & Simulation options:                      |
                |    --scenario, -s <id|1-16> Run academic scenario    |
                |    --list-scenarios         List all 16 presets      |
                |    --seed <num>             Master simulation seed   |
                |    --create-demo            Create & run demo world  |
                |    --run <name>             Run simulation from DB   |
                |    --list                   List DB simulations      |
                |    --nogui                  Disable server GUI       |
                |    --help, -h               Show this help message   |
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
        if (host == null || host.trim().isEmpty() || port <= 0) return false;
        String targetHost = ("0.0.0.0".equals(host.trim()) || host.trim().isEmpty()) ? "localhost" : host.trim();
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(targetHost, port), 600);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
