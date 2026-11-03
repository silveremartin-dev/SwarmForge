/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.ecs.EcsWorldManager;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.spatial.AStarPathfinder;
import org.swarmforge.core.structure.ConstructionManager;
import org.swarmforge.core.world.SoilHydricCoupling;
import org.swarmforge.core.world.WaterTable;
import org.swarmforge.core.world.WeatherSystem;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes environmental physical simulation loops (atmospheric weather coupling, soil hydrology,
 * groundwater table, fluid mechanics, predator ecosystem dynamics, and pheromone diffusion).
 */
public class SimulationEnvironmentUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationEnvironmentUpdater.class);

    private final Simulation simulation;

    public SimulationEnvironmentUpdater(Simulation simulation) {
        this.simulation = simulation;
    }

    public void updateEnvironment(long currentTick) {
        SparsePheromoneGrid pheromoneGrid = simulation.getPheromoneGrid();
        AStarPathfinder pathfinder = simulation.getPathfinder();
        Terrarium terrarium = simulation.getTerrarium();
        float simulationStepSeconds = simulation.getSimulationStepSeconds();

        if (pheromoneGrid != null) {
            pheromoneGrid.tick();
        }
        if (pathfinder != null) {
            pathfinder.tick();
        }

        Map<Colony, ConstructionManager> constructionManagers = simulation.getConstructionManagers();
        if (constructionManagers != null) {
            for (ConstructionManager cm : constructionManagers.values()) {
                cm.tick();
            }
        }

        if (simulation.getPredatorManager() != null) {
            simulation.getPredatorManager().tick(simulationStepSeconds);
        }

        if (simulation.getDiseaseManager() != null) {
            simulation.getDiseaseManager().tick(simulationStepSeconds);
        }

        if (simulation.getActiveDisasters() != null && !simulation.getActiveDisasters().isEmpty()) {
            simulation.getActiveDisasters().removeIf(disaster -> {
                disaster.tick(simulation, terrarium);
                return disaster.isFinished();
            });
        }

        if (currentTick % 10 == 0 && simulation.getWaterGrid() != null) {
            List<TunnelNetwork> tunnels = simulation.getColonies().stream()
                    .map(Colony::getTunnelNetwork)
                    .toList();

            WeatherSystem weather = simulation.getWeather();
            if (weather != null && weather.isRaining()) {
                float rainAmount = Math.max(0.1f, weather.getRainfall()) * 0.001f * simulationStepSeconds * 10.0f;
                simulation.getWaterGrid().addRain(rainAmount);
            }
            simulation.getWaterGrid().tick(tunnels, simulationStepSeconds * 10.0f);
        }

        SoilHydricCoupling soilHydricCoupling = simulation.getSoilHydricCoupling();
        WeatherSystem weather = simulation.getWeather();
        if (soilHydricCoupling != null && weather != null) {
            float sTemp = weather.getTemperature();
            float rain = weather.getRainfall();
            float snow = weather.getSnowfall();
            float wind = weather.getWindSpeed();
            soilHydricCoupling.tick(rain, snow, sTemp, wind, simulationStepSeconds / 3600.0f);
        }

        WaterTable waterTable = simulation.getWaterTable();
        if (waterTable != null && weather != null) {
            float evap = (weather.isRaining() ? 0.0f : 0.2f);
            if (simulation.getVegetationSystem() != null) {
                float solarIrr = (simulation.getDayNightCycle() != null) ? simulation.getDayNightCycle().getLightLevel() : 0.8f;
                evap += simulation.getVegetationSystem().calculateRootEvapotranspiration(solarIrr);
            }
            waterTable.tick(weather.getRainfall(), evap);
        }

        EcsWorldManager ecsWorldManager = simulation.getEcsWorldManager();
        if (weather != null && ecsWorldManager != null) {
            float sTemp = weather.getTemperature();
            float sMoist = (soilHydricCoupling != null) ? (soilHydricCoupling.getMoistureAtDepth(0) / 100.0f) : (weather.getSoilHumidityAtDepth(0) / 100.0f);
            ecsWorldManager.updateEnvironmentalBoundaryConditions(sTemp, sMoist, 0.0f);
        }

        boolean pheromonesProcessed = false;
        var computeCluster = simulation.getComputeCluster();
        if (computeCluster != null && terrarium != null && terrarium.getWidth() * terrarium.getHeight() * terrarium.getDepth() <= 64 * 64 * 64) {
            try {
                float[] pheromoneData = terrarium.exportPheromones();
                pheromonesProcessed = computeCluster.dispatchPheromoneTask(
                        terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth(),
                        pheromoneData);
                if (pheromonesProcessed) {
                    terrarium.importPheromones(pheromoneData);
                }
            } catch (Exception ex) {
                LOG.warn("Remote compute cluster pheromone dispatch failed: {}", ex.getMessage());
            }
        }

        if (!pheromonesProcessed && currentTick % simulation.getDiffusionInterval() == 0 && pheromoneGrid != null) {
            pheromoneGrid.diffuse();
        }

        if (simulation.getTerritoryManager() != null) {
            simulation.getTerritoryManager().tick();
        }
    }

    public Colony createColonyWithCastes(String speciesType, int queens, int workers, int soldiers, int brood, float x, float y) {
        org.swarmforge.core.species.Species species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(speciesType);
        Terrarium terrarium = simulation.getTerrarium();
        float surfaceZ = terrarium != null ? terrarium.getSurfaceElevation(x, y) : 0f;
        Colony colony = new Colony(species, x, y, surfaceZ);
        if (terrarium != null) {
            colony.setTerrarium(terrarium);
        }
        if (simulation.getRandom() != null) {
            colony.setRandom(new java.util.Random(simulation.getRandom().nextLong()));
        }
        if (queens > 0) colony.createQueens(queens);
        if (workers > 0) colony.createWorkers(workers);
        if (soldiers > 0) colony.createSoldiers(soldiers);
        if (brood > 0) colony.createBrood(brood);
        colony.bootstrapDefaultResources();
        if (colony.getTunnelNetwork() != null) {
            colony.getTunnelNetwork().rebuildForArchitecture(x, y, surfaceZ, species != null ? species.getNestType() : "BURROW_UNDERGROUND", colony);
        }
        simulation.addColony(colony);
        return colony;
    }

    public void seedInitialEnvironmentResources() {
        Terrarium terrarium = simulation.getTerrarium();
        if (terrarium == null) return;
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int totalPopulation = simulation.getColonies().stream().mapToInt(Colony::getPopulation).sum();
        int foodClusters = Math.max(8, totalPopulation / 20);

        var vegetationSystem = simulation.getVegetationSystem();
        if (vegetationSystem != null && vegetationSystem.getPlantCount() == 0) {
            vegetationSystem.populate(Math.max(30, width / 2), org.swarmforge.core.world.VegetationSystem.PlantType.GRASS);
            vegetationSystem.populate(Math.max(15, width / 4), org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB);
            vegetationSystem.populate(Math.max(20, width / 3), org.swarmforge.core.world.VegetationSystem.PlantType.FLOWER);
            vegetationSystem.populate(Math.max(8, width / 8), org.swarmforge.core.world.VegetationSystem.PlantType.TREE);
        }

        if (simulation.getFoodSources().isEmpty()) {
            java.util.Random rng = (simulation.getRandom() != null) ? simulation.getRandom() : new java.util.Random(12345);
            org.swarmforge.core.domain.ResourceType[] types = {
                org.swarmforge.core.domain.ResourceType.SUGAR,
                org.swarmforge.core.domain.ResourceType.SEED,
                org.swarmforge.core.domain.ResourceType.NECTAR,
                org.swarmforge.core.domain.ResourceType.INSECT
            };
            for (int i = 0; i < foodClusters; i++) {
                float fx = 5.0f + rng.nextFloat() * Math.max(1.0f, width - 10.0f);
                float fy = 5.0f + rng.nextFloat() * Math.max(1.0f, height - 10.0f);
                org.swarmforge.core.domain.ResourceType rType = types[i % types.length];
                float qty = 300.0f + rng.nextFloat() * 700.0f;
                simulation.addFoodSource(new org.swarmforge.core.domain.FoodSource(fx, fy, 0.0f, qty, rType));
            }
        }

        var predatorManager = simulation.getPredatorManager();
        if (predatorManager != null && predatorManager.getPredatorCount() == 0) {
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.CATERPILLAR, width * 0.35f, height * 0.35f, 0.0f);
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.BEETLE, width * 0.65f, height * 0.40f, 0.0f);
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.SPIDER, width * 0.50f, height * 0.70f, 0.0f);
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.LADYBUG_LARVA, width * 0.25f, height * 0.60f, 0.0f);
        }
    }
}
