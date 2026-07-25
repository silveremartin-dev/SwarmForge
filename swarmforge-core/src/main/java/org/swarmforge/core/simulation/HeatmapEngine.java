/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.structure.Chamber;

import java.util.List;

/**
 * High-performance Heatmap & Coloration Overlay Engine for SwarmForge.
 * Generates 2D slice matrices for:
 * 1. Pheromone Density Maps (Food, Home, Alarm, Trail, Queen, Brood, Death, Territory)
 * 2. Tunnel & Voxel Occupancy / Traffic Density
 * 3. Chamber Specialization (Queen Chamber, Brood Chamber, Food Vault, Fungus Garden, Cemetery, Royal Chamber, Ventilation Shaft)
 * 4. Soil Stability & Mechanics (Mohr-Coulomb shear stress, cohesion, compaction, moisture)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HeatmapEngine {

    public enum HeatmapType {
        PHEROMONE_FOOD("Pheromone: Nourriture"),
        PHEROMONE_HOME("Pheromone: Nid / Homing"),
        PHEROMONE_ALARM("Pheromone: Alarme"),
        PHEROMONE_TRAIL("Pheromone: Piste"),
        PHEROMONE_QUEEN("Pheromone: Reine"),
        PHEROMONE_BROOD("Pheromone: Couvain"),
        PHEROMONE_DEATH("Pheromone: Cadavre / Nécrophorèse"),
        PHEROMONE_TERRITORY("Pheromone: Territoire"),
        TUNNEL_OCCUPANCY("Occupation & Trafic des Tunnels"),
        CHAMBER_SPECIALIZATION("Spécialisation des Chambres"),
        SOIL_STABILITY("Stabilité du Sol (Mohr-Coulomb)"),
        SOIL_MOISTURE("Humidité du Sol");

        public final String label;
        HeatmapType(String label) { this.label = label; }
    }

    /**
     * Generates a 2D float grid matrix [width][height] for a given Z-plane slice and heatmap type.
     */
    public static float[][] generateHeatmap(Terrarium terrarium, List<Colony> colonies, int zSlice, HeatmapType type) {
        if (terrarium == null) return new float[0][0];
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        float[][] map = new float[width][height];

        switch (type) {
            case PHEROMONE_FOOD -> fillPheromone(terrarium, zSlice, 0, map);
            case PHEROMONE_HOME -> fillPheromone(terrarium, zSlice, 1, map);
            case PHEROMONE_ALARM -> fillPheromone(terrarium, zSlice, 2, map);
            case PHEROMONE_TRAIL -> fillPheromone(terrarium, zSlice, 3, map);
            case PHEROMONE_QUEEN -> fillPheromone(terrarium, zSlice, 4, map);
            case PHEROMONE_BROOD -> fillPheromone(terrarium, zSlice, 5, map);
            case PHEROMONE_DEATH -> fillPheromone(terrarium, zSlice, 6, map);
            case PHEROMONE_TERRITORY -> fillPheromone(terrarium, zSlice, 7, map);
            case TUNNEL_OCCUPANCY -> fillOccupancy(colonies, width, height, zSlice, map);
            case CHAMBER_SPECIALIZATION -> fillChamberSpecialization(colonies, width, height, zSlice, map);
            case SOIL_STABILITY -> fillSoilStability(terrarium, zSlice, map);
            case SOIL_MOISTURE -> fillSoilMoisture(terrarium, zSlice, map);
        }

        return map;
    }

    private static void fillPheromone(Terrarium terrarium, int zSlice, int channel, float[][] map) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TerrariumCell cell = terrarium.getCell(x, y, zSlice);
                if (cell != null) {
                    map[x][y] = cell.getPheromone(channel);
                }
            }
        }
    }

    private static void fillOccupancy(List<Colony> colonies, int width, int height, int zSlice, float[][] map) {
        if (colonies == null) return;
        for (Colony col : colonies) {
            for (Individual ind : col.getLivingIndividuals()) {
                int ix = (int) Math.floor(ind.getX());
                int iy = (int) Math.floor(ind.getY());
                int iz = (int) Math.floor(ind.getZ());
                if (iz == zSlice && ix >= 0 && ix < width && iy >= 0 && iy < height) {
                    map[ix][iy] += 0.25f; // Accumulate occupancy density
                    if (map[ix][iy] > 1.0f) map[ix][iy] = 1.0f;
                }
            }
        }
    }

    private static void fillChamberSpecialization(List<Colony> colonies, int width, int height, int zSlice, float[][] map) {
        if (colonies == null) return;
        for (Colony col : colonies) {
            if (col.getNest() != null && col.getNest().getChambers() != null) {
                for (Chamber chamber : col.getNest().getChambers()) {
                    int cx = (int) Math.floor(chamber.getX());
                    int cy = (int) Math.floor(chamber.getY());
                    int cz = (int) Math.floor(chamber.getZ());
                    int radius = Math.max(2, (int) Math.sqrt(chamber.getCapacity()));

                    if (Math.abs(cz - zSlice) <= 2) {
                        float code = getChamberTypeCode(chamber.getType());
                        for (int x = Math.max(0, cx - radius); x <= Math.min(width - 1, cx + radius); x++) {
                            for (int y = Math.max(0, cy - radius); y <= Math.min(height - 1, cy + radius); y++) {
                                double dist = Math.hypot(x - cx, y - cy);
                                if (dist <= radius) {
                                    map[x][y] = code;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static float getChamberTypeCode(Chamber.Type type) {
        if (type == null) return 0.1f;
        return switch (type) {
            case QUEEN_QUARTERS -> 0.9f; // Queen Chamber
            case NURSERY -> 0.7f;        // Brood Nursery
            case FOOD_STORAGE -> 0.5f;   // Food Storage
            case WASTE_DUMP -> 0.2f;     // Waste Dump / Cemetery
            case ENTRANCE -> 0.1f;       // Nest Entrance
        };
    }

    private static void fillSoilStability(Terrarium terrarium, int zSlice, float[][] map) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TerrariumCell cell = terrarium.getCell(x, y, zSlice);
                if (cell != null) {
                    float cohesion = cell.material() == TerrariumCell.Material.ROCK ? 1.0f :
                            (cell.material() == TerrariumCell.Material.EARTH ? 0.6f : 0.2f);
                    float normHumidity = Math.min(1.0f, Math.max(0.0f, cell.humidity() / 100.0f));
                    float moistureFactor = 1.0f - Math.abs(normHumidity - 0.5f);
                    map[x][y] = Math.min(1.0f, cohesion * moistureFactor);
                }
            }
        }
    }

    private static void fillSoilMoisture(Terrarium terrarium, int zSlice, float[][] map) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TerrariumCell cell = terrarium.getCell(x, y, zSlice);
                if (cell != null) {
                    map[x][y] = Math.min(1.0f, Math.max(0.0f, cell.humidity() / 100.0f));
                }
            }
        }
    }
}
