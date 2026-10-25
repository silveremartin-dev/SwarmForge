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

/**
 * Executes environmental physical simulation loops (atmospheric weather coupling, soil hydrology,
 * groundwater table, fluid mechanics, predator ecosystem dynamics, and pheromone diffusion).
 */
public class SimulationEnvironmentUpdater {

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
            } catch (Exception ignored) {
            }
        }

        if (!pheromonesProcessed && currentTick % simulation.getDiffusionInterval() == 0 && pheromoneGrid != null) {
            pheromoneGrid.diffuse();
        }

        if (simulation.getTerritoryManager() != null) {
            simulation.getTerritoryManager().tick();
        }
    }
}
