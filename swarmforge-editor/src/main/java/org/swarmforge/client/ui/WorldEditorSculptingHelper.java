/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.ui.world.TerrainProceduralGenerator;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;

/**
 * 3D cell/voxel picking, sculpting brush execution, and slope stabilization for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorSculptingHelper {

    private WorldEditorSculptingHelper() {}

    static int[] pick3DGridCell(WorldEditorPane pane, double mx, double my, double cx, double cy, double scale, double radAz, double radEl) {
        int bestX = -1, bestY = -1, bestD = 0;
        double minDstSq = Double.MAX_VALUE;
        int step = 2;

        double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        double targetDepthVal = pane.depthSlider != null ? pane.depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;

        boolean isCutPlaneFacingCamera = Math.sin(radAz) >= 0;
        if (cutXLimit > 0 && cutXLimit < GRID_SIZE && (!pane.isTerrainVisible || isCutPlaneFacingCamera)) {
            for (int y = 0; y < GRID_SIZE - step; y += step) {
                double surfZ0 = pane.heightGrid[cutXLimit][y] * 40.0;
                double surfZ1 = pane.heightGrid[cutXLimit][y + step] * 40.0;
                if (pane.carvedVoxelGrid[cutXLimit][y]) surfZ0 -= 15.0;
                if (pane.carvedVoxelGrid[cutXLimit][y + step]) surfZ1 -= 15.0;

                double refZ0 = 32.0;
                double refZ1 = 32.0;

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
                    double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
                    double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
                    double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);

                    if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                    double[] pTop0 = pane.project3DPoint(cutXLimit, y, topZ0, cx, cy, scale, radAz, radEl);
                    double[] pTop1 = pane.project3DPoint(cutXLimit, y + step, topZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot1 = pane.project3DPoint(cutXLimit, y + step, botZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot0 = pane.project3DPoint(cutXLimit, y, botZ0, cx, cy, scale, radAz, radEl);

                    double midPx = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midPy = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;

                    double dSq = (midPx - mx) * (midPx - mx) + (midPy - my) * (midPy - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = cutXLimit;
                        bestY = y;
                        bestD = d;
                    }
                }
            }
        }

        int[][] skirtEdges = new int[][]{
            {0, 0, GRID_SIZE - 1, 0},
            {GRID_SIZE - 1, 0, GRID_SIZE - 1, GRID_SIZE - 1},
            {0, GRID_SIZE - 1, GRID_SIZE - 1, GRID_SIZE - 1},
            {0, 0, 0, GRID_SIZE - 1}
        };
        for (int[] edge : skirtEdges) {
            int ex0 = edge[0], ey0 = edge[1], ex1 = edge[2], ey1 = edge[3];
            int edx = Integer.compare(ex1, ex0);
            int edy = Integer.compare(ey1, ey0);
            int elen = Math.max(Math.abs(ex1 - ex0), Math.abs(ey1 - ey0));
            for (int i = 0; i < elen; i += step) {
                int wx0 = ex0 + i * edx;
                int wy0 = ey0 + i * edy;
                int wx1 = ex0 + Math.min(elen, i + step) * edx;
                int wy1 = ey0 + Math.min(elen, i + step) * edy;
                double surfZ0 = pane.heightGrid[wx0][wy0] * 40.0;
                double surfZ1 = pane.heightGrid[wx1][wy1] * 40.0;

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double topZ0 = Math.min(surfZ0, 32.0 - d * layerDepthPx);
                    double topZ1 = Math.min(surfZ1, 32.0 - d * layerDepthPx);
                    double botZ0 = Math.min(surfZ0, 32.0 - (d + 1) * layerDepthPx);
                    double botZ1 = Math.min(surfZ1, 32.0 - (d + 1) * layerDepthPx);
                    if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                    double[] pTop0 = pane.project3DPoint(wx0, wy0, topZ0, cx, cy, scale, radAz, radEl);
                    double[] pTop1 = pane.project3DPoint(wx1, wy1, topZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot1 = pane.project3DPoint(wx1, wy1, botZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot0 = pane.project3DPoint(wx0, wy0, botZ0, cx, cy, scale, radAz, radEl);

                    double midPx = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midPy = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;

                    double dSq = (midPx - mx) * (midPx - mx) + (midPy - my) * (midPy - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = wx0;
                        bestY = wy0;
                        bestD = d;
                    }
                }
            }
        }

        int maxX = Math.min(GRID_SIZE - 1, cutXLimit - 1);
        for (int x = 0; x <= maxX; x += step) {
            for (int y = 0; y < GRID_SIZE; y += step) {
                int x2 = Math.min(maxX, x + step);
                int y2 = Math.min(GRID_SIZE - 1, y + step);
                int[][] quadPts = {{x, y}, {x2, y}, {x, y2}, {x2, y2}, {x, GRID_SIZE - 1}, {x2, GRID_SIZE - 1}, {maxX, y}, {maxX, y2}};
                for (int[] qp : quadPts) {
                    int qx = qp[0];
                    int qy = qp[1];
                    double z = pane.heightGrid[qx][qy] * 40.0;
                    double[] p = pane.project3DPoint(qx, qy, z, cx, cy, scale, radAz, radEl);
                    double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = qx;
                        bestY = qy;
                        bestD = 0;
                    }
                }
            }
        }

        if (minDstSq < 4900.0) {
            return new int[]{bestX, bestY, bestD};
        }
        return null;
    }

    static void handleSculptClick(WorldEditorPane pane, double mx, double my, String viewType) {
        if (pane.enableSculptingCheck == null || !pane.enableSculptingCheck.isSelected()) return;
        double cw = "3D".equals(viewType) ? pane.canvas3D.getWidth() : ("SIDE".equals(viewType) ? pane.canvasSide.getWidth() : pane.canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? pane.canvas3D.getHeight() : ("SIDE".equals(viewType) ? pane.canvasSide.getHeight() : pane.canvasTop.getHeight());
        if (cw <= 0 || ch <= 0) return;

        int gx, gy;
        if ("3D".equals(viewType)) {
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double cx = cw / 2 + pane.pan3DX;
            double cy = ch / 2 + pane.pan3DY + 40;
            double scale = pane.zoom * 12.0;
            int[] picked = pick3DGridCell(pane, mx, my, cx, cy, scale, radAz, radEl);
            if (picked != null) {
                gx = picked[0];
                gy = picked[1];
            } else {
                gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx / cw) * GRID_SIZE)));
                gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((my / ch) * GRID_SIZE)));
            }
        } else if ("SIDE".equals(viewType)) {
            double blockW = (cw - 20.0) / GRID_SIZE;
            double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
            gx = Math.max(0, Math.min(GRID_SIZE - 1, (int) (GRID_SIZE * cutRatio)));
            int yScreen = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx - 10.0) / blockW)));
            gy = GRID_SIZE - 1 - yScreen;
        } else {
            double gridPx = Math.min(cw - 12, ch - 12) * pane.topZoom;
            double cellW = gridPx / GRID_SIZE;
            double cellH = gridPx / GRID_SIZE;
            double offsetX = (cw - gridPx) / 2.0 + pane.topPanX;
            double offsetY = (ch - gridPx) / 2.0 + pane.topPanY;
            gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx - offsetX) / cellW)));
            gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((my - offsetY) / cellH)));
        }
        int radius = (int) pane.brushRadiusSlider.getValue();
        double strength = pane.brushStrengthSlider.getValue() / 100.0 * 0.08;
        String mode = pane.brushModeSelect.getValue();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    int cx = Math.max(0, Math.min(GRID_SIZE - 1, gx + dx));
                    int cy2 = Math.max(0, Math.min(GRID_SIZE - 1, gy + dy));
                    double falloff = 1.0 - Math.sqrt(dx * dx + dy * dy) / (double) (radius + 1);
                    if (mode != null && (mode.contains("RAISE") || mode.contains("Raise"))) {
                        pane.heightGrid[cx][cy2] = Math.min(1.0, pane.heightGrid[cx][cy2] + strength * falloff);
                    } else if (mode != null && (mode.contains("LOWER") || mode.contains("Lower"))) {
                        pane.heightGrid[cx][cy2] = Math.max(0.01, pane.heightGrid[cx][cy2] - strength * falloff);
                    } else if (mode != null && (mode.contains("SMOOTH") || mode.contains("Smooth"))) {
                        pane.heightGrid[cx][cy2] = TerrainProceduralGenerator.lrp(pane.heightGrid[cx][cy2], 0.5, strength * 2 * falloff);
                    }
                }
            }
        }
        smoothSlopeStabilization(pane);
        pane.repaintAllViews();
    }

    static void smoothSlopeStabilization(WorldEditorPane pane) {
        double maxDelta = 0.18;
        for (int x = 1; x < GRID_SIZE - 1; x++) {
            for (int y = 1; y < GRID_SIZE - 1; y++) {
                double minN = Math.min(Math.min(pane.heightGrid[x - 1][y], pane.heightGrid[x + 1][y]),
                                       Math.min(pane.heightGrid[x][y - 1], pane.heightGrid[x][y + 1]));
                if (pane.heightGrid[x][y] - minN > maxDelta) pane.heightGrid[x][y] = minN + maxDelta;
            }
        }
    }
}
