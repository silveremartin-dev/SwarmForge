/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.swarmforge.client.ui.WorldEditorPane;

import java.util.Arrays;

/**
 * 3D Isometric Stratigraphy Walls & Voxel Subsurface Renderer for the World Editor Studio.
 * Renders cross-sectional stratigraphic side walls, depth-attenuated pedological layers,
 * rock/gravel inclusions, underground cavities/stalactites, root networks, and moisture/pH tensors.
 */
public class WorldStratigraphy3DRenderer {

    public static double[] project3DPoint(double x, double y, double z, double cx, double cy, double scale, double radAz, double radEl, int gridSize) {
        double halfGrid = gridSize / 2.0;
        double isoX = (x - halfGrid) * Math.cos(radAz) - (y - halfGrid) * Math.sin(radAz);
        double isoY = (x - halfGrid) * Math.sin(radAz) * Math.sin(radEl) + (y - halfGrid) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);
        double px = cx + isoX * (scale / 10.0);
        double py = cy + isoY * (scale / 10.0);
        return new double[]{px, py};
    }

    public void drawStratigraphySideWalls3D(WorldEditorPane pane, GraphicsContext gc3D, double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, int step, boolean renderBackWallsOnly) {
        int gridSize = WorldEditorPane.GRID_SIZE;
        double halfGrid = gridSize / 2.0;

        double[][] edgeCenters = {
            {0.0, -halfGrid},
            {halfGrid, 0.0},
            {0.0, halfGrid},
            {-halfGrid, 0.0}
        };

        Integer[] order = {0, 1, 2, 3};
        Arrays.sort(order, (a, b) -> {
            double pyA = edgeCenters[a][0] * Math.sin(radAz) + edgeCenters[a][1] * Math.cos(radAz);
            double pyB = edgeCenters[b][0] * Math.sin(radAz) + edgeCenters[b][1] * Math.cos(radAz);
            return Double.compare(pyA, pyB);
        });

        for (int edgeIdx : order) {
            double depthVal = edgeCenters[edgeIdx][0] * Math.sin(radAz) + edgeCenters[edgeIdx][1] * Math.cos(radAz);
            boolean isBack = depthVal < 0;
            if (renderBackWallsOnly != isBack) continue;

            switch (edgeIdx) {
                case 0 -> {
                    for (int x = 0; x < gridSize; x += step) {
                        int xNext = Math.min(gridSize, x + step);
                        drawSingleWallSegment3D(pane, gc3D, x, 0, xNext, 0, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 1 -> {
                    for (int y = 0; y < gridSize; y += step) {
                        int yNext = Math.min(gridSize, y + step);
                        drawSingleWallSegment3D(pane, gc3D, gridSize, y, gridSize, yNext, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 2 -> {
                    for (int x = 0; x < gridSize; x += step) {
                        int xNext = Math.min(gridSize, x + step);
                        drawSingleWallSegment3D(pane, gc3D, x, gridSize, xNext, gridSize, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
                case 3 -> {
                    for (int y = 0; y < gridSize; y += step) {
                        int yNext = Math.min(gridSize, y + step);
                        drawSingleWallSegment3D(pane, gc3D, 0, y, 0, yNext, cx, cy, scale, radAz, radEl, maxDepthPx, false);
                    }
                }
            }
        }
    }

    public void drawSingleWallSegment3D(WorldEditorPane pane, GraphicsContext gc3D, int x0, int y0, int x1, int y1, double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, boolean isSliceCutaway) {
        int gridSize = WorldEditorPane.GRID_SIZE;
        int soilDepth = WorldEditorPane.SOIL_DEPTH;

        int gx0 = Math.min(gridSize - 1, Math.max(0, x0));
        int gy0 = Math.min(gridSize - 1, Math.max(0, y0));
        int gx1 = Math.min(gridSize - 1, Math.max(0, x1));
        int gy1 = Math.min(gridSize - 1, Math.max(0, y1));

        double layerDepthPx = maxDepthPx / soilDepth;
        double[][] heightGrid = pane.getHeightGrid();
        boolean[][] carvedVoxelGrid = pane.getCarvedVoxelGrid();
        byte[][][] soilLayers = pane.getSoilLayers();
        boolean[][][] voidGrid = pane.getVoidGrid();
        float[][][] humidityGrid = pane.getHumidityGrid();
        float[][][] phGrid = pane.getPhGrid();
        float[][][] rootGrid = pane.getRootGrid();

        double surfZ0 = heightGrid[gx0][gy0] * 40.0;
        double surfZ1 = heightGrid[gx1][gy1] * 40.0;

        if (carvedVoxelGrid[gx0][gy0]) surfZ0 -= 15.0;
        if (carvedVoxelGrid[gx1][gy1]) surfZ1 -= 15.0;

        boolean isTranslucent = pane.isTranslucentVolumetricMode();
        boolean showInclusions = pane.isShowGravelInclusions();
        boolean isAdvMode = true;

        double refZ0 = 32.0;
        double refZ1 = 32.0;
        for (int d = 0; d < soilDepth; d++) {
            double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
            double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
            double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
            double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);
            if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

            double[] pTop0 = project3DPoint(x0, y0, topZ0, cx, cy, scale, radAz, radEl, gridSize);
            double[] pTop1 = project3DPoint(x1, y1, topZ1, cx, cy, scale, radAz, radEl, gridSize);
            double[] pBot1 = project3DPoint(x1, y1, botZ1, cx, cy, scale, radAz, radEl, gridSize);
            double[] pBot0 = project3DPoint(x0, y0, botZ0, cx, cy, scale, radAz, radEl, gridSize);

            byte mat = soilLayers[gx0][gy0][d];
            boolean isVoid = voidGrid[gx0][gy0][d] && pane.isGalleriesVisible() && pane.isShowGalleries();

            Color matCol;
            if (isVoid) {
                matCol = Color.web("#0f172a");
            } else if (!pane.isMaterialVisible(mat)) {
                matCol = Color.web("#1e293b", 0.3);
            } else {
                matCol = TerrainMaterialPalette.getMaterialColor(mat);
                if (isAdvMode) {
                    double depthDim = 1.0 - (d / (double) soilDepth) * 0.32;
                    matCol = matCol.deriveColor(0, 1.0, depthDim, 1.0);
                }
            }

            if (isSliceCutaway) {
                matCol = matCol.deriveColor(0, 1.1, 1.2, 1.0);
            }

            if (isTranslucent) {
                matCol = Color.color(matCol.getRed(), matCol.getGreen(), matCol.getBlue(), 0.55);
            }

            gc3D.setFill(matCol);
            gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);

            double wallInclNoise = TerrainProceduralGenerator.valueNoise3D(x0 * 0.35, y0 * 0.35, d * 0.35);
            if (isAdvMode && showInclusions && !isVoid && pane.isMaterialVisible(mat) && wallInclNoise > 0.68) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                double voxSize = Math.max(1.2, Math.abs(pTop1[0] - pTop0[0]) * 0.25);

                if (mat == 4) { // Gravel / Cailloutis pebble inclusions
                    gc3D.setFill(Color.web("#cbd5e1"));
                    gc3D.fillOval(midX - voxSize * 0.3, midY - voxSize * 0.2, voxSize * 0.4, voxSize * 0.35);
                    gc3D.setFill(Color.web("#475569"));
                    gc3D.fillOval(midX + voxSize * 0.05, midY + voxSize * 0.05, voxSize * 0.25, voxSize * 0.2);
                } else if (mat == 5) { // Organic Litter / Matière Organique detritus
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillOval(midX - voxSize * 0.3, midY - voxSize * 0.2, voxSize * 0.5, voxSize * 0.3);
                    gc3D.setFill(Color.web("#523219"));
                    gc3D.fillRect(midX, midY, voxSize * 0.25, voxSize * 0.2);
                } else if (mat == 3) { // Rock / Stone inclusion
                    gc3D.setFill(Color.web("#e2e8f0"));
                    gc3D.fillOval(midX - voxSize * 0.35, midY - voxSize * 0.3, voxSize * 0.7, voxSize * 0.5);
                } else if (mat == 2) { // Clay inclusion
                    gc3D.setFill(Color.web("#7c2d12"));
                    gc3D.fillRect(midX - voxSize * 0.3, midY - voxSize * 0.2, voxSize * 0.6, voxSize * 0.4);
                } else if (mat == 1) { // Sand grain inclusion
                    gc3D.setFill(Color.web("#fef08a"));
                    gc3D.fillOval(midX - voxSize * 0.25, midY - voxSize * 0.25, voxSize * 0.5, voxSize * 0.5);
                }
            }

            // Natural Caverns & Subterranean Cavities Rendering (isVoid)
            if (isVoid) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                // Cave ambient depth glow
                gc3D.setFill(Color.web("#0284c7", 0.22));
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
                // Cave Stalactite Spikes
                gc3D.setFill(Color.web("#334155"));
                gc3D.fillPolygon(new double[]{midX - 2, midX + 2, midX}, new double[]{pTop0[1], pTop0[1], pTop0[1] + 6}, 3);
            }

            // Depth-mapped Soil Moisture Tensor Visualization
            if (pane.isShowHumidity() && humidityGrid != null) {
                float hum = humidityGrid[gx0][gy0][d];
                gc3D.setFill(Color.web("#0284c7", Math.min(0.70, hum * 0.50 + 0.05)));
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
            }

            // Depth-mapped Soil pH Level Tensor Visualization
            if (pane.isShowPh() && phGrid != null) {
                float ph = phGrid[gx0][gy0][d] > 0 ? phGrid[gx0][gy0][d] : 6.5f;
                Color phColor;
                if (ph < 6.5f) {
                    phColor = Color.web("#eab308", Math.min(0.65, (6.5 - ph) * 0.22 + 0.15));
                } else if (ph > 7.5f) {
                    phColor = Color.web("#1e40af", Math.min(0.65, (ph - 7.5) * 0.22 + 0.15));
                } else {
                    phColor = Color.web("#22c55e", 0.35);
                }
                gc3D.setFill(phColor);
                gc3D.fillPolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
            }

            if (pane.isShowRoots() && rootGrid != null && rootGrid[gx0][gy0][d] > 0.02f) {
                double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                double rSc = Math.max(1.0, rootGrid[gx0][gy0][d] * 2.2 * (pane.getZoom() / 7.5));
                gc3D.setStroke(Color.web("#78350f", Math.min(0.95, rootGrid[gx0][gy0][d] * 1.3)));
                gc3D.setLineWidth(rSc);
                gc3D.strokeLine(pTop0[0], pTop0[1], pBot1[0], pBot1[1]);
                gc3D.strokeLine(pTop1[0], pTop1[1], pBot0[0], pBot0[1]);
                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillOval(midX - rSc, midY - rSc, rSc * 2, rSc * 2);
            }

            gc3D.setStroke(matCol.darker());
            gc3D.setLineWidth(0.2);
            gc3D.strokePolygon(new double[]{pTop0[0], pTop1[0], pBot1[0], pBot0[0]}, new double[]{pTop0[1], pTop1[1], pBot1[1], pBot0[1]}, 4);
        }

        if (pane.isShowChamferedBezel()) {
            double[] pTop0 = project3DPoint(x0, y0, surfZ0, cx, cy, scale, radAz, radEl, gridSize);
            double[] pTop1 = project3DPoint(x1, y1, surfZ1, cx, cy, scale, radAz, radEl, gridSize);

            gc3D.setStroke(Color.web("#38bdf8", 0.9));
            gc3D.setLineWidth(1.6);
            gc3D.strokeLine(pTop0[0], pTop0[1], pTop1[0], pTop1[1]);
        }
    }
}
