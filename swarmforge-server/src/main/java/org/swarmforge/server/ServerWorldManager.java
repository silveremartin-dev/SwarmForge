/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.species.SpeciesRegistry;
import org.swarmforge.core.world.NestGenerator;
import org.swarmforge.core.world.TerrainGenerator;
import org.swarmforge.server.persistence.DatabaseManager;
import org.swarmforge.server.persistence.SimulationSerializer;

/**
 * Handles world lifecycle operations (creation, academic scenario loading,
 * serialization, checkpointing, and restoration) for SwarmForgeServer.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ServerWorldManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServerWorldManager.class);

    private final SwarmForgeServer server;

    public ServerWorldManager(SwarmForgeServer server) {
        this.server = server;
    }

    public void createNewWorld(String name, String terrainType, String speciesType) {
        LOG.info("Creating new world: {} ({}, {})", name, terrainType, speciesType);

        Simulation simulation = server.getSimulation();
        Terrarium terrarium = server.getTerrarium();
        if (simulation == null || terrarium == null) {
            return;
        }

        simulation.stop();
        terrarium.clear();
        simulation.reset(0);

        TerrainGenerator terrainGen = new TerrainGenerator();
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

        NestGenerator nestGen = new NestGenerator(terrarium);
        int centerX = terrarium.getWidth() / 2;
        int centerY = terrarium.getHeight() / 2;
        nestGen.generate(centerX, centerY, groundLevel - 5, NestGenerator.NestType.MATURE, 1.0f);

        Species species;
        if (speciesType != null && speciesType.contains("Atta")) {
            species = SpeciesRegistry.getInstance().getSpecies("atta-cephalotes");
        } else {
            species = SpeciesRegistry.getInstance().getSpecies("lasius-niger");
        }

        Colony colony = new Colony(species, centerX, centerY, 50);
        colony.addIndividual(colony.createQueen());
        for (int i = 0; i < 50; i++) {
            Individual worker = colony.createWorker();
            worker.setPosition(centerX, centerY, groundLevel - 2);
            colony.addIndividual(worker);
        }
        simulation.addColony(colony);

        if (server.isDatabaseConnected()) {
            saveWorld(name);
        }
        simulation.start();
        LOG.info("New world created, running and ready.");
    }

    public List<String> getAvailableWorlds() {
        DatabaseManager database = server.getDatabaseManager();
        if (database == null || !database.isConnected()) {
            return Collections.emptyList();
        }
        try {
            return database.worldRepository().findAll().stream()
                    .map(w -> w.name() + " [" + w.id() + "]")
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Failed to list worlds", e);
            return Collections.emptyList();
        }
    }

    public void loadAcademicScenario(Scenario scenario) {
        if (scenario == null) return;
        LOG.info("Loading Academic Scenario: {} ({})", scenario.getTitle(), scenario.getId());

        Simulation simulation = server.getSimulation();
        Terrarium terrarium = server.getTerrarium();
        if (simulation == null || terrarium == null) return;

        simulation.stop();
        terrarium.clear();
        simulation.reset(0);
        simulation.setMasterSeed(scenario.getMasterSeed());

        TerrainGenerator terrainGen = new TerrainGenerator();
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

        if (simulation.getWeather() != null) {
            simulation.getWeather().setTemperature(scenario.getInitialTemperature());
            simulation.getWeather().setHumidity(scenario.getInitialHumidity() * 100f);
        }

        int totalColonies = scenario.getColonies().size();
        for (int i = 0; i < totalColonies; i++) {
            var colSetup = scenario.getColonies().get(i);
            String spName = colSetup.speciesName();
            Species species = SpeciesRegistry.getInstance().getSpecies(spName);

            int posX = (totalColonies == 1) ? (terrarium.getWidth() / 2) : (int) ((i + 1) * (terrarium.getWidth() / (totalColonies + 1.0f)));
            int posY = (totalColonies == 1) ? (terrarium.getHeight() / 2) : (int) ((i + 1) * (terrarium.getHeight() / (totalColonies + 1.0f)));

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

        int foodCount = Math.max(10, scenario.getFoodPatchesCount());
        Random rand = new Random(scenario.getMasterSeed());
        for (int f = 0; f < foodCount; f++) {
            float fx = 10 + rand.nextFloat() * (terrarium.getWidth() - 20);
            float fy = 10 + rand.nextFloat() * (terrarium.getHeight() - 20);
            simulation.spawnFood(fx, fy, groundLevel, 15 + rand.nextFloat() * 30, ResourceType.SUGAR);
        }

        simulation.start();
        if (server.isDatabaseConnected()) {
            saveWorld(scenario.getTitle());
        }
        LOG.info("Scenario '{}' initialized and running (Colonies: {}, Population: {})",
                scenario.getTitle(), simulation.getColonies().size(),
                simulation.getColonies().stream().mapToInt(Colony::getPopulation).sum());
    }

    public void createDemoWorld() {
        createNewWorld("Demo World", "Perlin Hills", "Lasius niger");

        Terrarium terrarium = server.getTerrarium();
        int rivalX = terrarium.getWidth() - 30;
        int rivalY = terrarium.getHeight() - 30;
        int groundLevel = terrarium.getDepth() - 10;
        Colony rivalColony = new Colony(SpeciesRegistry.getInstance().getSpecies("atta-cephalotes"), rivalX, rivalY, groundLevel - 5);
        rivalColony.addIndividual(rivalColony.createQueen());
        for (int i = 0; i < 30; i++) {
            Individual worker = rivalColony.createWorker();
            worker.setPosition(rivalX, rivalY, groundLevel - 2);
            rivalColony.addIndividual(worker);
        }
        Simulation simulation = server.getSimulation();
        if (simulation != null) {
            simulation.addColony(rivalColony);
        }

        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            float fx = rand.nextFloat() * terrarium.getWidth();
            float fy = rand.nextFloat() * terrarium.getHeight();
            if (simulation != null) {
                simulation.spawnFood(fx, fy, groundLevel, 10 + rand.nextFloat() * 20, ResourceType.SEED);
            }
        }
    }

    public void saveWorld(String name) {
        DatabaseManager database = server.getDatabaseManager();
        if (!database.isConnected()) {
            return;
        }
        try {
            Simulation simulation = server.getSimulation();
            if (simulation == null) {
                LOG.warn("Cannot save world: simulation is null");
                return;
            }
            Terrarium targetTerrarium = simulation.getTerrarium() != null ? simulation.getTerrarium() : server.getTerrarium();
            if (targetTerrarium == null) {
                LOG.warn("Cannot save world: terrarium is null");
                return;
            }

            SimulationSerializer serializer = new SimulationSerializer();
            byte[] cellsData = serializer.serializeCells(targetTerrarium);
            byte[] coloniesData = serializer.serializeColonies(simulation.getColonies());
            byte[] individualsData = serializer.serializeIndividuals(new ArrayList<>());
            long tickCount = simulation.getTickCount();
            int width = targetTerrarium.getWidth();
            int height = targetTerrarium.getHeight();
            int depth = targetTerrarium.getDepth();
            List<Colony> coloniesCopy = new ArrayList<>(simulation.getColonies());

            server.getDbAsyncExecutor().submit(() -> {
                try {
                    UUID worldId = database.worldRepository().save(name, width, height, depth, 0, 0, 0);

                    for (Colony col : coloniesCopy) {
                        try {
                            database.colonyRepository().save(col, "System");
                        } catch (Exception ignored) {}
                    }

                    database.checkpointRepository().save(
                            worldId, tickCount, name,
                            cellsData, coloniesData, individualsData);

                    LOG.info("Async world save completed: {} (Tick {})", worldId, tickCount);
                } catch (Exception ex) {
                    LOG.error("Failed to async save world to database: {}", ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            LOG.error("Failed to serialize world for saving: {}", e.getMessage(), e);
        }
    }

    public void loadWorld(String worldIdStr) {
        LOG.info("Loading world: {}", worldIdStr);
        DatabaseManager database = server.getDatabaseManager();
        try {
            UUID worldId = UUID.fromString(worldIdStr);
            var checkpoints = database.checkpointRepository().findByWorld(worldId);

            if (checkpoints.isEmpty()) {
                throw new Exception("No checkpoints found for world " + worldIdStr);
            }

            UUID checkpointId = checkpoints.get(0).id();
            var checkpointOpt = database.checkpointRepository().findById(checkpointId);

            if (checkpointOpt.isEmpty()) {
                throw new Exception("Checkpoint data missing for " + checkpointId);
            }

            var checkpoint = checkpointOpt.get();
            Simulation simulation = server.getSimulation();
            if (simulation == null) return;

            boolean wasRunning = simulation.getState() == Simulation.State.RUNNING;
            simulation.stop();
            Thread.sleep(100);

            SimulationSerializer serializer = new SimulationSerializer();
            var cells = serializer.deserializeCells(checkpoint.cellsData());
            var colonies = serializer.deserializeColonies(checkpoint.coloniesData());

            Terrarium terrarium = server.getTerrarium();
            terrarium.clear();
            for (TerrariumCell cell : cells) {
                terrarium.setCell(cell);
            }

            simulation.reset(checkpoint.tick());
            for (Colony colony : colonies) {
                simulation.addColony(colony);
            }

            LOG.info("World loaded successfully. Tick: {}", checkpoint.tick());

            if (wasRunning) {
                simulation.start();
            }
        } catch (Exception e) {
            LOG.error("Failed to load world: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
