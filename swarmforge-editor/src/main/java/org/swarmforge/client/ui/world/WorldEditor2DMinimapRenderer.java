/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.spatial.Morton3D;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * High-performance 2D Canvas Minimap Renderer for the World Editor Studio.
 * Handles stratigraphic vertical cross-sections (side profile) and top-down
 * spatial maps with layers for topography, soil moisture, pH, botanical flora,
 * tree canopies, root networks, colony nests, and chemical pheromones.
 */
public class WorldEditor2DMinimapRenderer {

    public void drawSide(WorldEditorPane pane) {
        if (pane == null || pane.getCanvasSide() == null || pane.getCanvasSide().getWidth() < 10 || pane.getCanvasSide().getHeight() < 10) return;
        GraphicsContext gcSide = pane.getGcSide();
        double w = pane.getCanvasSide().getWidth();
        double h = pane.getCanvasSide().getHeight();

        int gridSize = WorldEditorPane.GRID_SIZE;
        int soilDepth = WorldEditorPane.SOIL_DEPTH;

        double cutRatio = pane.getSliceCutRatio();
        int cutX = Math.max(0, Math.min(gridSize - 1, (int) (gridSize * cutRatio)));

        Label lblSideTitle = pane.getLblSideMinimapTitle();
        if (lblSideTitle != null) {
            lblSideTitle.textProperty().unbind();
            lblSideTitle.setText(String.format(Locale.US, I18nManager.getInstance().get("minimap.stratigraphic_profile"), cutX, cutRatio * 100.0));
        }

        gcSide.save();
        gcSide.setFill(Color.web("#0f172a"));
        gcSide.fillRect(0, 0, w, h);

        gcSide.beginPath();
        gcSide.rect(1, 1, w - 2, h - 2);
        gcSide.clip();

        double blockW = (w - 20.0) / gridSize;
        double blockH = (h - 90.0) / soilDepth;

        double refSurfaceH = 0.50 * 25.0;
        double[][] heightGrid = pane.getHeightGrid();
        byte[][][] soilLayers = pane.getSoilLayers();
        boolean[][][] voidGrid = pane.getVoidGrid();
        boolean[][] carvedVoxelGrid = pane.getCarvedVoxelGrid();
        float[][][] humidityGrid = pane.getHumidityGrid();
        float[][][] phGrid = pane.getPhGrid();
        float[][][] rootGrid = pane.getRootGrid();

        for (int y = 0; y < gridSize; y++) {
            int yIdx = gridSize - 1 - y;
            double surfaceH = heightGrid[cutX][yIdx] * 25.0;
            double pySurf = 65 - surfaceH;

            for (int d = 0; d < soilDepth; d++) {
                double px = 10 + y * blockW;
                double topY = 65 - refSurfaceH + d * blockH;
                double botY = 65 - refSurfaceH + (d + 1) * blockH;

                if (botY <= pySurf) continue;

                double pyDraw = Math.max(topY, pySurf);
                double hDraw = botY - pyDraw;

                byte mat = soilLayers[cutX][yIdx][d];
                boolean isVoid = voidGrid[cutX][yIdx][d] && pane.isGalleriesVisible() && pane.isShowGalleries();
                if (isVoid || !pane.isMaterialVisible(mat)) {
                    gcSide.setFill(Color.web("#0f172a"));
                } else {
                    gcSide.setFill(TerrainMaterialPalette.getMaterialColor(mat));
                }
                gcSide.fillRect(px, pyDraw, Math.max(1, blockW + 0.5), Math.max(1, hDraw));

                if (pane.isShowHumidity() && humidityGrid != null) {
                    float hum = humidityGrid[cutX][yIdx][d];
                    gcSide.setFill(Color.web("#0284c7", Math.min(0.60, hum * 0.50)));
                    gcSide.fillRect(px, pyDraw, Math.max(1, blockW + 0.5), Math.max(1, hDraw));
                }
                if (pane.isShowPh() && phGrid != null) {
                    float ph = phGrid[cutX][yIdx][d] > 0 ? phGrid[cutX][yIdx][d] : 6.5f;
                    Color phColor = ph < 6.5f ? Color.web("#eab308", 0.35) : (ph > 7.5f ? Color.web("#1e40af", 0.35) : Color.web("#22c55e", 0.25));
                    gcSide.setFill(phColor);
                    gcSide.fillRect(px, pyDraw, Math.max(1, blockW + 0.5), Math.max(1, hDraw));
                }
                if (pane.isShowRoots() && rootGrid != null && rootGrid[cutX][yIdx][d] > 0.15f) {
                    gcSide.setStroke(Color.web("#78350f", 0.85));
                    gcSide.setLineWidth(Math.max(1.0, rootGrid[cutX][yIdx][d] * 2.0));
                    gcSide.strokeLine(px + blockW / 2, pyDraw, px + blockW / 2, pyDraw + hDraw);
                }
            }

            double px = 10 + y * blockW;
            double pyBase = 65 - refSurfaceH + soilDepth * blockH;
            gcSide.setFill(Color.web("#020617"));
            gcSide.fillRect(px, pyBase, Math.max(1, blockW + 0.5), h - pyBase);
            gcSide.setStroke(Color.web("#334155"));
            gcSide.setLineWidth(1.0);
            gcSide.strokeLine(px, pyBase, px + Math.max(1, blockW), pyBase);
        }

        double wtDepthM = pane.getWaterTableDepthMeters();
        double totalDepthM = pane.getTotalDepthMeters();

        double totalSoilH = soilDepth * blockH;
        double wtRatio = wtDepthM / Math.max(0.1, totalDepthM);
        double wtY = 65 - refSurfaceH + wtRatio * totalSoilH;

        if (wtY >= 10 && wtY <= h - 10) {
            gcSide.setFill(Color.web("#0284c7"));
            gcSide.setGlobalAlpha(0.40);
            gcSide.fillRect(10, wtY, w - 20, Math.max(0, (65 - refSurfaceH + totalSoilH) - wtY));
            gcSide.setGlobalAlpha(1.0);

            gcSide.setStroke(Color.web("#38bdf8", 0.95));
            gcSide.setLineWidth(1.5);
            gcSide.setLineDashes(4.0, 4.0);
            gcSide.strokeLine(10, wtY, w - 20, wtY);
            gcSide.setLineDashes((double[]) null);

            gcSide.setFill(Color.web("#38bdf8"));
            gcSide.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 9));
            String wtLabel = I18nManager.getInstance().get("world.water_table");
            gcSide.fillText(String.format(Locale.US, "💧 %s: %.1fm", wtLabel, wtDepthM), w - 110, Math.max(22.0, wtY - 3));
        }

        gcSide.setFill(Color.web("#d97706"));
        for (int y = 0; y < gridSize; y += 2) {
            int yIdx = gridSize - 1 - y;
            if (carvedVoxelGrid[cutX][yIdx]) {
                double px = 10 + y * blockW;
                double py = 70;
                gcSide.fillOval(px, py, 6, 6);
            }
        }

        gcSide.restore();

        gcSide.setStroke(Color.web("#38bdf8", 0.6));
        gcSide.setLineWidth(1.0);
        gcSide.strokeRect(1, 1, w - 2, h - 2);
    }

    public void drawTop(WorldEditorPane pane) {
        if (pane == null || pane.getCanvasTop() == null || pane.getCanvasTop().getWidth() < 10 || pane.getCanvasTop().getHeight() < 10) return;
        GraphicsContext gcTop = pane.getGcTop();
        double w = pane.getCanvasTop().getWidth();
        double h = pane.getCanvasTop().getHeight();

        int gridSize = WorldEditorPane.GRID_SIZE;

        double sideM = pane.getSurfaceSizeMeters();
        double areaM2 = sideM * sideM;
        Label lblTopTitle = pane.getLblTopdownMinimapTitle();
        if (lblTopTitle != null) {
            lblTopTitle.textProperty().unbind();
            lblTopTitle.setText(String.format(Locale.US, I18nManager.getInstance().get("minimap.topdown_title"), sideM, areaM2));
        }

        gcTop.save();
        gcTop.setFill(Color.web("#0f172a"));
        gcTop.fillRect(0, 0, w, h);

        gcTop.beginPath();
        gcTop.rect(1, 1, w - 2, h - 2);
        gcTop.clip();

        double tZoom = pane.getTopZoom();
        double cx = pane.getTopPanX();
        double cy = pane.getTopPanY();

        double gridPx = Math.min(w - 12, h - 12) * tZoom;
        double cellW = gridPx / gridSize;
        double cellH = gridPx / gridSize;
        double offsetX = (w - gridPx) / 2.0 + cx;
        double offsetY = (h - gridPx) / 2.0 + cy;
        double cutRatio = pane.getSliceCutRatio();

        byte[][][] soilLayers = pane.getSoilLayers();
        boolean[][] carvedVoxelGrid = pane.getCarvedVoxelGrid();
        float[][][] humidityGrid = pane.getHumidityGrid();
        float[][][] rootGrid = pane.getRootGrid();
        double[][] heightGrid = pane.getHeightGrid();

        for (int x = 0; x < gridSize; x += 2) {
            for (int y = 0; y < gridSize; y += 2) {
                byte mat = soilLayers[x][y][0];
                boolean visibleMat = pane.isMaterialVisible(mat);

                Color col = visibleMat ? TerrainMaterialPalette.getMaterialColor(mat) : Color.web("#0f172a");
                if (carvedVoxelGrid[x][y]) col = Color.web("#d97706");
                gcTop.setFill(col);
                gcTop.fillRect(offsetX + x * cellW, offsetY + y * cellH, cellW * 2, cellH * 2);

                if (pane.isShowHumidity() && humidityGrid != null) {
                    gcTop.setFill(Color.web("#0284c7"));
                    gcTop.setGlobalAlpha(humidityGrid[x][y][0] * 0.6);
                    gcTop.fillRect(offsetX + x * cellW, offsetY + y * cellH, cellW * 2, cellH * 2);
                    gcTop.setGlobalAlpha(1.0);
                }
            }
        }

        if (pane.isTerrainVisible() && pane.hasRiver()) {
            List<int[]> riverPath = pane.getRiverPath();
            if (riverPath != null && riverPath.size() > 1) {
                gcTop.setStroke(Color.web("#0284c7"));
                gcTop.setLineWidth(Math.max(1.5, (pane.getRiverWidthSliderValue() / 35.0) * tZoom));
                gcTop.beginPath();
                boolean first = true;
                for (int[] pt : riverPath) {
                    double px = offsetX + pt[0] * cellW;
                    double py = offsetY + pt[1] * cellH;
                    if (first) { gcTop.moveTo(px, py); first = false; }
                    else gcTop.lineTo(px, py);
                }
                gcTop.stroke();
            }
        }

        int stumpCount = pane.getHollowLogsCount();
        if (stumpCount > 0) {
            Random sRand = new Random(99);
            for (int i = 0; i < stumpCount; i++) {
                int sx = (int) (sRand.nextDouble() * (gridSize - 4));
                int sy = (int) (sRand.nextDouble() * (gridSize - 4));
                double px = offsetX + sx * cellW;
                double py = offsetY + sy * cellH;

                gcTop.setFill(Color.web("#78350f"));
                gcTop.fillRect(px - 3 * tZoom, py - 3 * tZoom, 6 * tZoom, 6 * tZoom);
                gcTop.setFill(Color.web("#451a03"));
                gcTop.fillOval(px - 2 * tZoom, py - 2 * tZoom, 4 * tZoom, 4 * tZoom);
            }
        }

        List<WorldEditorPane.SurfaceFloraItem> floraItems = pane.getSurfaceFloraItems();
        if (floraItems != null) {
            for (WorldEditorPane.SurfaceFloraItem item : floraItems) {
                double px = offsetX + item.gx * cellW;
                double py = offsetY + item.gy * cellH;
                double sz = item.scale * tZoom * 3.5;

                switch (item.type) {
                    case 0 -> {
                        gcTop.setFill(Color.web("#4ade80"));
                        gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    }
                    case 1 -> {
                        gcTop.setFill(Color.web("#166534"));
                        gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                        gcTop.setFill(Color.web("#a3e635"));
                        gcTop.fillOval(px - sz * 0.5, py - sz * 0.5, sz, sz);
                    }
                    case 2 -> {
                        gcTop.setFill(Color.web("#f43f5e"));
                        gcTop.fillOval(px - sz, py - sz, sz * 2, sz * 2);
                    }
                    case 3 -> {
                        gcTop.setFill(Color.web("#15803d", 0.75));
                        gcTop.fillOval(px - sz * 1.5, py - sz, sz * 3, sz * 2);
                    }
                    case 4 -> {
                        gcTop.setFill(Color.web("#854d0e", 0.85));
                        gcTop.fillOval(px - sz * 1.5, py - sz * 0.8, sz * 3.0, sz * 1.6);
                        gcTop.setFill(Color.web("#a16207", 0.7));
                        gcTop.fillOval(px - sz * 0.8, py - sz * 1.0, sz * 2.0, sz * 1.5);
                    }
                    case 5 -> {
                        gcTop.setStroke(Color.web("#451a03"));
                        gcTop.setLineWidth(1.4 * tZoom);
                        gcTop.strokeLine(px - sz, py - sz, px + sz, py + sz);
                    }
                    case 6 -> {
                        gcTop.setFill(Color.web("#94a3b8"));
                        gcTop.fillOval(px - sz * 0.6, py - sz * 0.6, sz * 1.2, sz * 1.2);
                    }
                }
            }
        }

        if (pane.isShowVegetation()) {
            List<WorldEditorPane.BotanicalTreeData> treeDataList = pane.getBotanicalTreeInstances();
            if (treeDataList != null) {
                for (WorldEditorPane.BotanicalTreeData btd : treeDataList) {
                    int gx = btd.gx;
                    int gy = btd.gy;
                    double baseTreeHeightM = switch (btd.speciesIdx) {
                        case 0 -> 6.0;   // Bambou
                        case 1 -> 2.5;   // Souche
                        case 2 -> 12.0;  // Bouleau
                        case 3 -> 4.0;   // Cactus
                        case 4 -> 15.0;  // Chêne
                        case 5 -> 18.0;  // Pin
                        case 6 -> 8.0;   // Acacia
                        default -> 15.0;
                    };
                    double topTreeRadius = Math.max(4.0, (baseTreeHeightM * 0.30 * btd.ageScale) * ((cellW * gridSize) / Math.max(1.0, sideM)) * tZoom);

                    double px = offsetX + gx * cellW;
                    double py = offsetY + gy * cellH;

                    gcTop.setFill(Color.web("#14532d"));
                    gcTop.fillOval(px - topTreeRadius, py - topTreeRadius, topTreeRadius * 2, topTreeRadius * 2);
                    gcTop.setFill(Color.web("#166534"));
                    gcTop.fillOval(px - topTreeRadius * 0.7, py - topTreeRadius * 0.7, topTreeRadius * 1.4, topTreeRadius * 1.4);
                    gcTop.setFill(Color.web("#15803d"));
                    gcTop.fillOval(px - topTreeRadius * 0.4, py - topTreeRadius * 0.4, topTreeRadius * 0.8, topTreeRadius * 0.8);
                }
            }
        }

        if (pane.isShowRoots() && rootGrid != null) {
            gcTop.setStroke(Color.web("#78350f", 0.75));
            gcTop.setLineWidth(1.2 * tZoom);
            for (int rx = 0; rx < gridSize; rx += 4) {
                for (int ry = 0; ry < gridSize; ry += 4) {
                    if (rootGrid[rx][ry][0] > 0.02f) {
                        double rpx = offsetX + rx * cellW;
                        double rpy = offsetY + ry * cellH;
                        gcTop.strokeOval(rpx - 3 * tZoom, rpy - 3 * tZoom, 6 * tZoom, 6 * tZoom);
                    }
                }
            }
        }

        // Pheromone Heatmap Overlay
        if (pane.isSimulationMode() && pane.getActiveSimulation() != null && pane.isPheromonesVisible()) {
            Simulation activeSimulation = pane.getActiveSimulation();
            SparsePheromoneGrid pGrid = activeSimulation.getPheromoneGrid();
            if (pGrid != null) {
                Map<Long, float[]> pEntries = pGrid.getAllEntries();
                if (pEntries != null && !pEntries.isEmpty()) {
                    for (Map.Entry<Long, float[]> e : pEntries.entrySet()) {
                        long key = e.getKey();
                        float[] vals = e.getValue();
                        if (vals == null) continue;

                        int[] coords = Morton3D.decode(key);
                        double terrW = Math.max(1.0, activeSimulation.getTerrarium().getWidth());
                        double terrH = Math.max(1.0, activeSimulation.getTerrarium().getHeight());
                        double gx = (coords[0] / terrW) * gridSize;
                        double gy = (coords[1] / terrH) * gridSize;

                        if (gx < 0 || gx >= gridSize || gy < 0 || gy >= gridSize) continue;

                        float homeVal = (vals.length > 0 && pane.isShowHomePheromone()) ? vals[0] : 0f;
                        float foodVal = (vals.length > 1 && pane.isShowFoodPheromone()) ? vals[1] : 0f;
                        float alarmVal = (vals.length > 2 && pane.isShowAlarmPheromone()) ? vals[2] : 0f;
                        float recruitVal = (vals.length > 3 && pane.isShowRecruitmentPheromone()) ? vals[3] : 0f;
                        float queenVal = (vals.length > 4 && pane.isShowQueenPheromone()) ? vals[4] : 0f;
                        float deathVal = (vals.length > 5 && pane.isShowDeathPheromone()) ? vals[5] : 0f;

                        float maxVal = Math.max(homeVal, Math.max(foodVal, Math.max(alarmVal, Math.max(recruitVal, Math.max(queenVal, deathVal)))));
                        if (maxVal < 0.02f) continue;

                        String colorHex = (alarmVal == maxVal) ? "#ef4444" :
                                         (foodVal == maxVal) ? "#a855f7" :
                                         (homeVal == maxVal) ? "#0284c7" :
                                         (recruitVal == maxVal) ? "#f59e0b" :
                                         (queenVal == maxVal) ? "#ec4899" : "#64748b";

                        double topPx = offsetX + gx * cellW;
                        double topPy = offsetY + gy * cellH;
                        double dotR = cellW * (1.2 + maxVal * 1.5);

                        gcTop.setFill(Color.web(colorHex, Math.min(0.85, maxVal * 0.7)));
                        gcTop.fillOval(topPx - dotR * 0.5, topPy - dotR * 0.5, dotR, dotR);
                    }
                }
            }
        }

        // Draw Colony Nests and Ants in Simulation Mode
        if (pane.isSimulationMode() && pane.getActiveSimulation() != null) {
            Simulation activeSimulation = pane.getActiveSimulation();
            for (Colony colony : activeSimulation.getColonies()) {
                double nGx = Math.max(0.0, Math.min(gridSize - 1.0, (colony.getNestX() / (float) Math.max(1, activeSimulation.getTerrarium().getWidth())) * gridSize));
                double nGy = Math.max(0.0, Math.min(gridSize - 1.0, (colony.getNestY() / (float) Math.max(1, activeSimulation.getTerrarium().getHeight())) * gridSize));
                double nPx = offsetX + nGx * cellW;
                double nPy = offsetY + nGy * cellH;
                gcTop.setFill(Color.web("#f59e0b", 0.9));
                gcTop.fillOval(nPx - 4 * tZoom, nPy - 4 * tZoom, 8 * tZoom, 8 * tZoom);
                gcTop.setStroke(Color.WHITE);
                gcTop.setLineWidth(1.0);
                gcTop.strokeOval(nPx - 4 * tZoom, nPy - 4 * tZoom, 8 * tZoom, 8 * tZoom);

                for (Individual ind : colony.getLivingIndividuals()) {
                    double ax = ind.getX();
                    double ay = ind.getY();
                    double gx = Math.max(0.0, Math.min(gridSize - 1.0, (ax / (float) Math.max(1, activeSimulation.getTerrarium().getWidth())) * gridSize));
                    double gy = Math.max(0.0, Math.min(gridSize - 1.0, (ay / (float) Math.max(1, activeSimulation.getTerrarium().getHeight())) * gridSize));
                    double aPx = offsetX + gx * cellW;
                    double aPy = offsetY + gy * cellH;
                    gcTop.setFill(Color.web("#fbbf24"));
                    gcTop.fillOval(aPx - 1.5 * tZoom, aPy - 1.5 * tZoom, 3.0 * tZoom, 3.0 * tZoom);
                }
            }
        }

        // 2D Isolines
        if (pane.isShowElevationIsolines()) {
            gcTop.setStroke(Color.web("#38bdf8", 0.70));
            gcTop.setLineWidth(1.0 * tZoom);
            for (int ix = 0; ix < gridSize - 1; ix += 2) {
                for (int iy = 0; iy < gridSize - 1; iy += 2) {
                    if ((int) (heightGrid[ix][iy] / 0.05) != (int) (heightGrid[ix + 1][iy] / 0.05)) {
                        double ipx = offsetX + ix * cellW;
                        double ipy = offsetY + iy * cellH;
                        gcTop.strokeLine(ipx, ipy, ipx + cellW * 2, ipy + cellH * 2);
                    }
                }
            }
        }
        if (pane.isShowClimateIsolines() && humidityGrid != null) {
            gcTop.setStroke(Color.web("#06b6d4", 0.70));
            gcTop.setLineWidth(1.0 * tZoom);
            for (int ix = 0; ix < gridSize - 1; ix += 2) {
                for (int iy = 0; iy < gridSize - 1; iy += 2) {
                    if ((int) (humidityGrid[ix][iy][0] / 0.10) != (int) (humidityGrid[ix + 1][iy][0] / 0.10)) {
                        double ipx = offsetX + ix * cellW;
                        double ipy = offsetY + iy * cellH;
                        gcTop.strokeLine(ipx + cellW * 2, ipy, ipx, ipy + cellH * 2);
                    }
                }
            }
        }

        if (cutRatio < 0.99) {
            int cutX = Math.max(0, Math.min(gridSize - 1, (int) (gridSize * cutRatio)));
            double scanLineX = offsetX + cutX * cellW;
            gcTop.setStroke(Color.web("#0284c7", 0.45));
            gcTop.setLineWidth(3.0);
            gcTop.strokeLine(scanLineX, 0, scanLineX, h);

            gcTop.setStroke(Color.web("#38bdf8", 0.95));
            gcTop.setLineWidth(1.5);
            gcTop.strokeLine(scanLineX, 0, scanLineX, h);
        }

        gcTop.restore();

        gcTop.setStroke(Color.web("#38bdf8", 0.6));
        gcTop.setLineWidth(1.0);
        gcTop.strokeRect(1, 1, w - 2, h - 2);
    }
}
