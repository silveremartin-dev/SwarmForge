package org.swarmforge.client.ui;

import org.swarmforge.client.ui.world.TerrainProceduralGenerator;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

/**
 * Handles disaster simulations (wildfire, flood, drought) and terrarium model exports for {@link WorldEditorPane}.
 */
public final class WorldEditorDisasterAndExportHelper {

    private WorldEditorDisasterAndExportHelper() {}

    public static void triggerWildfire(WorldEditorPane pane, int cx, int cy, int radius) {
        if (pane.surfaceFloraItems != null) {
            for (WorldEditorPane.SurfaceFloraItem item : pane.surfaceFloraItems) {
                double distSq = (item.gx - cx) * (item.gx - cx) + (item.gy - cy) * (item.gy - cy);
                if (distSq <= radius * radius) {
                    item.isCharred = true;
                    if (item.type < 4) item.type = 5;
                }
            }
        }
        if (pane.tempGrid != null && pane.humidityGrid != null && pane.phGrid != null) {
            for (int x = Math.max(0, cx - radius); x <= Math.min(WorldEditorPane.GRID_SIZE - 1, cx + radius); x++) {
                for (int y = Math.max(0, cy - radius); y <= Math.min(WorldEditorPane.GRID_SIZE - 1, cy + radius); y++) {
                    double distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy);
                    if (distSq <= radius * radius) {
                        for (int d = 0; d < WorldEditorPane.SOIL_DEPTH; d++) {
                            pane.humidityGrid[x][y][d] = 0.0f;
                            pane.phGrid[x][y][d] = Math.min(10.0f, pane.phGrid[x][y][d] + 1.2f);
                            pane.tempGrid[x][y][d] = Math.min(120.0f, pane.tempGrid[x][y][d] + 85.0f);
                        }
                    }
                }
            }
        }
        pane.repaintAllViews();
    }

    public static void triggerFlood(WorldEditorPane pane, int cx, int cy, int radius) {
        if (pane.tempGrid != null && pane.humidityGrid != null) {
            for (int x = Math.max(0, cx - radius); x <= Math.min(WorldEditorPane.GRID_SIZE - 1, cx + radius); x++) {
                for (int y = Math.max(0, cy - radius); y <= Math.min(WorldEditorPane.GRID_SIZE - 1, cy + radius); y++) {
                    double distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy);
                    if (distSq <= radius * radius) {
                        for (int d = 0; d < WorldEditorPane.SOIL_DEPTH; d++) {
                            pane.humidityGrid[x][y][d] = 1.0f;
                            pane.tempGrid[x][y][d] = Math.max(5.0f, pane.tempGrid[x][y][d] - 12.0f);
                        }
                    }
                }
            }
        }
        pane.repaintAllViews();
    }

    public static void triggerDrought(WorldEditorPane pane) {
        if (pane.tempGrid != null && pane.humidityGrid != null) {
            for (int x = 0; x < WorldEditorPane.GRID_SIZE; x++) {
                for (int y = 0; y < WorldEditorPane.GRID_SIZE; y++) {
                    for (int d = 0; d < WorldEditorPane.SOIL_DEPTH; d++) {
                        pane.humidityGrid[x][y][d] = Math.max(0.02f, pane.humidityGrid[x][y][d] * 0.25f);
                        pane.tempGrid[x][y][d] = Math.min(45.0f, pane.tempGrid[x][y][d] + 8.0f);
                    }
                }
            }
        }
        pane.repaintAllViews();
    }

    public static Terrarium exportToTerrarium(WorldEditorPane pane) {
        int w = 64, h = 64, d = 32;
        Terrarium terrarium = new Terrarium(w, h, d);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int gx = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) ((x / (double) w) * WorldEditorPane.GRID_SIZE)));
                int gy = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) ((y / (double) h) * WorldEditorPane.GRID_SIZE)));

                double hFrac = pane.heightGrid[gx][gy];
                int surfaceZ = Math.max(2, Math.min(d - 2, (int) Math.round(hFrac * (d - 1))));

                for (int z = 0; z < d; z++) {
                    if (z > surfaceZ) continue;
                    int soilIdx = (int) Math.max(0, Math.min(WorldEditorPane.SOIL_DEPTH - 1, ((double) (surfaceZ - z) / Math.max(1, surfaceZ)) * (WorldEditorPane.SOIL_DEPTH - 1)));
                    byte soilMatId = pane.soilLayers[gx][gy][soilIdx];
                    boolean isVoid = pane.voidGrid[gx][gy][soilIdx] || pane.carvedVoxelGrid[gx][gy];
                    boolean isRiv = (pane.riverCheck != null && pane.riverCheck.isSelected()) && TerrainProceduralGenerator.isNearRiver(pane.riverPath, gx, gy, 2);

                    TerrariumCell.Material mat;
                    if (isVoid) mat = TerrariumCell.Material.CAVITY;
                    else if (isRiv && z == surfaceZ) mat = TerrariumCell.Material.WATER;
                    else {
                        mat = switch (soilMatId) {
                            case 1 -> TerrariumCell.Material.SAND;
                            case 2 -> TerrariumCell.Material.CLAY;
                            case 3 -> TerrariumCell.Material.ROCK;
                            case 4 -> TerrariumCell.Material.GRAVEL;
                            case 5 -> TerrariumCell.Material.PEAT;
                            default -> TerrariumCell.Material.EARTH;
                        };
                    }

                    float hum = pane.humidityGrid[gx][gy][soilIdx] * 100.0f;
                    float temp = pane.tempGrid[gx][gy][soilIdx] > 0 ? pane.tempGrid[gx][gy][soilIdx] : 18.0f;
                    terrarium.setCell(new TerrariumCell(x, y, z, mat, new float[TerrariumCell.PHEROMONE_TYPES], temp, hum));
                }
            }
        }
        return terrarium;
    }
}
