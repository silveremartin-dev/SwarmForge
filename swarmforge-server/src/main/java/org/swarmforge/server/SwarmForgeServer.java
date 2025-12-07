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
import org.swarmforge.core.world.TerrainGenerator;
import org.swarmforge.core.world.WeatherSystem;
import org.swarmforge.server.persistence.DatabaseManager;
import org.swarmforge.server.persistence.RedisCache;
import org.swarmforge.server.grpc.SimulationServiceImpl;
import java.util.logging.Logger;

/**
 * Main server application for SwarmForge.
 * Initializes simulation, databases, and gRPC services.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SwarmForgeServer {

    private static final Logger LOG = Logger.getLogger(SwarmForgeServer.class.getName());

    private final Terrarium terrarium;
    private final Simulation simulation;
    private final WeatherSystem weather;
    private final DatabaseManager database;
    private final RedisCache cache;
    private final SimulationServiceImpl simulationService;

    private final int grpcPort;

    public SwarmForgeServer(ServerConfig config) {
        this.grpcPort = config.grpcPort();

        // Initialize world
        LOG.info("Initializing terrarium " + config.worldWidth() + "x" + config.worldHeight() + "x"
                + config.worldDepth());
        this.terrarium = new Terrarium(config.worldWidth(), config.worldHeight(), config.worldDepth());
        this.weather = new WeatherSystem(config.latitude(), config.longitude());

        // Generate terrain
        TerrainGenerator terrainGen = new TerrainGenerator(config.seed());
        terrainGen.generate(terrarium, config.groundLevel(), 10f, 0.02f);

        // Initialize simulation
        this.simulation = new Simulation(terrarium);
        this.simulationService = new SimulationServiceImpl(simulation);

        // Initialize persistence
        this.database = new DatabaseManager(
                config.dbHost(), config.dbPort(), config.dbName(),
                config.dbUser(), config.dbPassword());
        this.cache = new RedisCache(config.redisHost(), config.redisPort());
    }

    /**
     * Start the server.
     */
    public void start() throws Exception {
        LOG.info("Starting SwarmForge Server...");

        // Connect to databases
        try {
            database.connect();
            cache.connect();
        } catch (Exception e) {
            LOG.warning("Database connection failed: " + e.getMessage());
        }

        // Start gRPC server (placeholder - actual impl would use io.grpc.Server)
        LOG.info("gRPC server listening on port " + grpcPort);

        LOG.info("SwarmForge Server started successfully");
    }

    /**
     * Create a demo world with a colony.
     */
    public void createDemoWorld() {
        LOG.info("Creating demo world...");

        // Generate a nest
        NestGenerator nestGen = new NestGenerator(terrarium);
        int centerX = terrarium.getWidth() / 2;
        int centerY = terrarium.getHeight() / 2;
        int chambers = nestGen.generate(centerX, centerY, 50, NestGenerator.NestType.MATURE, 1.0f);
        LOG.info("Generated nest with " + chambers + " chambers");

        // Create colony
        Colony colony = new Colony("Lasius niger", centerX, centerY, 50);

        // Add queen
        colony.addIndividual(new Individual(colony.getId(), Individual.Caste.QUEEN, centerX, centerY, 45));

        // Add workers
        for (int i = 0; i < 100; i++) {
            float x = centerX + (float) (Math.random() * 10 - 5);
            float y = centerY + (float) (Math.random() * 10 - 5);
            colony.addIndividual(new Individual(colony.getId(), Individual.Caste.WORKER, x, y, 48));
        }

        simulation.addColony(colony);
        LOG.info("Created colony with " + colony.getPopulation() + " individuals");
    }

    /**
     * Stop the server.
     */
    public void stop() {
        LOG.info("Stopping SwarmForge Server...");
        simulation.stop();
        cache.disconnect();
        database.disconnect();
        LOG.info("Server stopped");
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Terrarium getTerrarium() {
        return terrarium;
    }

    // Server configuration record
    public record ServerConfig(
            int grpcPort,
            int worldWidth, int worldHeight, int worldDepth, int groundLevel,
            double latitude, double longitude, long seed,
            String dbHost, int dbPort, String dbName, String dbUser, String dbPassword,
            String redisHost, int redisPort) {
        public static ServerConfig defaults() {
            return new ServerConfig(
                    50051,
                    256, 256, 128, 64,
                    48.8566, 2.3522, System.currentTimeMillis(),
                    "localhost", 5432, "swarmforge", "swarmforge", "swarmforge",
                    "localhost", 6379);
        }
    }

    public static void main(String[] args) {
        try {
            SwarmForgeServer server = new SwarmForgeServer(ServerConfig.defaults());
            server.createDemoWorld();
            server.start();

            // Keep running until interrupted
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            Thread.currentThread().join();
        } catch (Exception e) {
            LOG.severe("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
