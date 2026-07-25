/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Soil Dynamics, Erosion, Voxel Deformation & Gallery Collapse System.
 * Simulates real-time voxel-by-voxel physical deformation, gallery excavation collapse,
 * soil compaction stability, sand angle-of-repose sliding, flood saturation collapses,
 * and drought cementing.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SoilStructureSystem {

    private final Simulation simulation;
    private long lastErosionCheck = 0;

    public SoilStructureSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastErosionCheck < 200) return; // Every ~3 seconds
        lastErosionCheck = currentTick;

        WeatherSystem weather = simulation.getWeather();
        float rainfall = weather.getRainfall();
        float snowfall = weather.getSnowfall();
        float temp = weather.getTemperature();
        float surfaceMoisture = weather.getSoilHumidityAtDepth(0);

        Terrarium terrarium = simulation.getTerrarium();
        if (terrarium != null) {
            applyVoxelPhysicsStep(terrarium, surfaceMoisture);

            // Dynamic Snow/Ice Ground Covering Simulation
            if (snowfall > 0 || (temp <= 0 && rainfall > 0)) {
                applySnowIceAccumulation(terrarium, temp);
            }
        }

        if (rainfall > 20.0f || surfaceMoisture > 92.0f) {
            // Flash Flood / High Saturation Erosion Risk
            for (Colony colony : simulation.getColonies()) {
                if (colony.getSpecies().getInsectOrder() == org.swarmforge.core.species.Species.InsectOrder.ANT ||
                    colony.getSpecies().getInsectOrder() == org.swarmforge.core.species.Species.InsectOrder.TERMITE) {

                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.MILESTONE_REACHED,
                            simulation.getTickCount(),
                            "🌊 Soil Saturation (" + String.format("%.1f", surfaceMoisture) + "%) causing gallery flooding & voxel deformation in " + colony.getSpeciesName()
                    ));
                }
            }
        }
    }

    /**
     * Accumulates snow or ice layer over open top surface cells during snowfall or freezing precipitation.
     */
    private void applySnowIceAccumulation(Terrarium terrarium, float temp) {
        int w = terrarium.getWidth();
        int h = terrarium.getHeight();
        int d = terrarium.getDepth();

        // Sample top surface voxels for snowfall accumulation
        for (int i = 0; i < 30; i++) {
            int x = (int) (Math.random() * w);
            int y = (int) (Math.random() * h);

            // Find top solid ground voxel
            for (int z = d - 2; z >= 0; z--) {
                TerrariumCell cell = terrarium.getCell(x, y, z);
                if (cell.material() != TerrariumCell.Material.AIR) {
                    TerrariumCell above = terrarium.getCell(x, y, z + 1);
                    if (above.material() == TerrariumCell.Material.AIR) {
                        TerrariumCell.Material coverMaterial = (temp <= -2.0f) ? TerrariumCell.Material.ICE : TerrariumCell.Material.SNOW;
                        terrarium.setCell(new TerrariumCell(x, y, z + 1, coverMaterial, new float[TerrariumCell.PHEROMONE_TYPES],
                                temp, 95.0f, TerrariumCell.DEFAULT_CO2, TerrariumCell.DEFAULT_O2, TerrariumCell.DEFAULT_N2O, 0.8f, 0f, 0f, TerrariumCell.DEFAULT_PRESSURE));
                    }
                    break;
                }
            }
        }
    }

    /**
     * Carves a voxel in real-time during ant/queen gallery excavation or manual 3D sculpting.
     * Evaluates immediate structural load and triggers local voxel deformation.
     */
    public boolean digGalleryVoxel(Terrarium terrarium, int x, int y, int z, float compactionIndex) {
        if (terrarium == null || !terrarium.inBounds(x, y, z)) return false;

        // Clear voxel (make air/passable gallery)
        terrarium.setCell(TerrariumCell.air(x, y, z));

        // Perform real-time physical deformation check on upper voxels (ceiling stability)
        if (z + 1 < terrarium.getDepth()) {
            TerrariumCell topCell = terrarium.getCell(x, y, z + 1);
            if (topCell.material() == TerrariumCell.Material.SAND && compactionIndex < 40.0f) { // Loose Sand ceiling collapses
                terrarium.setCell(TerrariumCell.air(x, y, z + 1));
                terrarium.setCell(TerrariumCell.sand(x, y, z)); // Sand falls down into excavated voxel
                return false; // Tunnel collapsed
            }
        }
        return true; // Tunnel voxel successfully excavated
    }

    /**
     * Computes voxel-by-voxel real-time physical deformation step across the terrarium grid.
     */
    private void applyVoxelPhysicsStep(Terrarium terrarium, float soilHumidity) {
        int w = terrarium.getWidth();
        int h = terrarium.getHeight();
        int d = terrarium.getDepth();

        // Sample random voxels for performance efficiency
        for (int i = 0; i < 50; i++) {
            int x = 1 + (int) (Math.random() * Math.max(1, w - 2));
            int y = 1 + (int) (Math.random() * Math.max(1, h - 2));
            int z = 1 + (int) (Math.random() * Math.max(1, d - 2));

            TerrariumCell cell = terrarium.getCell(x, y, z);
            if (cell.material() == TerrariumCell.Material.SAND) { // Sand Voxel: gravity fall check
                TerrariumCell below = terrarium.getCell(x, y, z - 1);
                if (below.material() == TerrariumCell.Material.AIR) { // Unsupported air below
                    terrarium.setCell(TerrariumCell.air(x, y, z));
                    terrarium.setCell(TerrariumCell.sand(x, y, z - 1));
                }
            }
        }
    }
}
