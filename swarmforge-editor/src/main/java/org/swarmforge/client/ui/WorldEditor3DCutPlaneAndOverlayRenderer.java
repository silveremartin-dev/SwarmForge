/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.client.ui.world.TerrainMaterialPalette;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;
import static org.swarmforge.client.ui.WorldEditor3DTechnicalRenderer.drawVoxelBlockTexture;
import static org.swarmforge.client.ui.WorldEditor3DTechnicalRenderer.project3DPoint;

/**
 * Subterranean cut-plane profile, 3D hover focus cell overlay,
 * and SI metric scale bar renderer for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditor3DCutPlaneAndOverlayRenderer {

    private WorldEditor3DCutPlaneAndOverlayRenderer() {}

    static void drawSubterraneanCutPlane(WorldEditorPane pane, int cutXLimit, double cx, double cy, double scale, double radAz, double radEl, double maxDepthPx, boolean isScanning) {
        if (cutXLimit <= 0 || cutXLimit >= GRID_SIZE) return;

        double stepY = 2;
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;
        boolean showInclusions = pane.showGravelInclusionsCheck != null && pane.showGravelInclusionsCheck.isSelected();

        for (int y = 0; y < GRID_SIZE - (int) stepY; y += (int) stepY) {
            double surfZ0 = pane.heightGrid[cutXLimit][y] * 40.0;
            double surfZ1 = pane.heightGrid[cutXLimit][y + (int) stepY] * 40.0;
            if (pane.carvedVoxelGrid[cutXLimit][y]) surfZ0 -= 15.0;
            if (pane.carvedVoxelGrid[cutXLimit][y + (int) stepY]) surfZ1 -= 15.0;

            double refZ0 = 32.0;
            double refZ1 = 32.0;
            for (int d = 0; d < SOIL_DEPTH; d++) {
                double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
                double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
                double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
                double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);

                if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                double[] pTop0 = project3DPoint(cutXLimit, y, topZ0, cx, cy, scale, radAz, radEl);
                double[] pTop1 = project3DPoint(cutXLimit, y + (int) stepY, topZ1, cx, cy, scale, radAz, radEl);
                double[] pBot1 = project3DPoint(cutXLimit, y + (int) stepY, botZ1, cx, cy, scale, radAz, radEl);
                double[] pBot0 = project3DPoint(cutXLimit, y, botZ0, cx, cy, scale, radAz, radEl);

                double[] pxs = {pTop0[0], pTop1[0], pBot1[0], pBot0[0]};
                double[] pys = {pTop0[1], pTop1[1], pBot1[1], pBot0[1]};

                byte mat = pane.soilLayers[cutXLimit][y][d];
                boolean isVoid = pane.voidGrid[cutXLimit][y][d];
                Color matCol;
                if (pane.currentRenderMode == RenderMode.GAMIFIED) {
                    if (isVoid) {
                        pane.gc3D.setFill(Color.web("#090d16"));
                        pane.gc3D.fillPolygon(pxs, pys, 4);
                        pane.gc3D.setStroke(Color.web("#0f172a", 0.95));
                        pane.gc3D.setLineWidth(1.4);
                        pane.gc3D.strokePolygon(pxs, pys, 4);
                    } else {
                        drawVoxelBlockTexture(pane, pxs, pys, mat, false, true, false, cutXLimit, y, d);
                    }
                    continue;
                } else if (isVoid) {
                    matCol = Color.web("#090d16");
                } else if (!pane.isMaterialVisible(mat)) {
                    matCol = Color.web("#1e293b", 0.35);
                } else {
                    matCol = TerrainMaterialPalette.getMaterialColor(mat).deriveColor(0, 1.15, 1.1, 1.0);
                }

                if (isVoid) {
                    if (pane.isGalleriesVisible && (pane.showGalleriesCheck == null || pane.showGalleriesCheck.isSelected())) {
                        pane.gc3D.setFill(matCol);
                        pane.gc3D.fillPolygon(pxs, pys, 4);
                    }
                } else {
                    pane.gc3D.setFill(matCol);
                    pane.gc3D.fillPolygon(pxs, pys, 4);
                }

                double cutInclNoise = pane.valueNoise3D(cutXLimit * 0.35, y * 0.35, d * 0.35);
                if (showInclusions && !isVoid && pane.isMaterialVisible(mat) && cutInclNoise > 0.68) {
                    double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                    double sz = Math.max(1.5, Math.abs(pTop1[0] - pTop0[0]) * 0.35);

                    if (mat == 3) {
                        pane.gc3D.setFill(Color.web("#e2e8f0"));
                        pane.gc3D.fillOval(midX - sz * 0.4, midY - sz * 0.3, sz * 0.8, sz * 0.6);
                    } else if (mat == 4) {
                        pane.gc3D.setFill(Color.web("#cbd5e1"));
                        pane.gc3D.fillOval(midX - sz * 0.3, midY - sz * 0.3, sz * 0.6, sz * 0.5);
                    } else if (mat == 2) {
                        pane.gc3D.setFill(Color.web("#7c2d12"));
                        pane.gc3D.fillRect(midX - sz * 0.4, midY - sz * 0.3, sz * 0.8, sz * 0.5);
                    } else if (mat == 1) {
                        pane.gc3D.setFill(Color.web("#fef08a"));
                        pane.gc3D.fillOval(midX - sz * 0.3, midY - sz * 0.3, sz * 0.6, sz * 0.6);
                    }
                }

                if (pane.showHumidityCheck != null && pane.showHumidityCheck.isSelected() && pane.humidityGrid != null) {
                    float hum = pane.humidityGrid[cutXLimit][y][d];
                    pane.gc3D.setFill(Color.web("#0284c7", Math.min(0.70, hum * 0.50 + 0.05)));
                    pane.gc3D.fillPolygon(pxs, pys, 4);
                }

                if (pane.showPhCheck != null && pane.showPhCheck.isSelected() && pane.phGrid != null) {
                    float ph = pane.phGrid[cutXLimit][y][d] > 0 ? pane.phGrid[cutXLimit][y][d] : 6.5f;
                    Color phColor;
                    if (ph < 6.5f) {
                        phColor = Color.web("#eab308", Math.min(0.65, (6.5 - ph) * 0.22 + 0.15));
                    } else if (ph > 7.5f) {
                        phColor = Color.web("#1e40af", Math.min(0.65, (ph - 7.5) * 0.22 + 0.15));
                    } else {
                        phColor = Color.web("#22c55e", 0.35);
                    }
                    pane.gc3D.setFill(phColor);
                    pane.gc3D.fillPolygon(pxs, pys, 4);
                }

                if (pane.showRootsCheck != null && pane.showRootsCheck.isSelected() && pane.rootGrid != null && pane.rootGrid[cutXLimit][y][d] > 0.02f) {
                    double midX = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midY = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;
                    double rSc = Math.max(1.0, pane.rootGrid[cutXLimit][y][d] * 2.2 * (pane.zoom / 7.5));
                    pane.gc3D.setStroke(Color.web("#78350f", Math.min(0.95, pane.rootGrid[cutXLimit][y][d] * 1.3)));
                    pane.gc3D.setLineWidth(rSc);
                    pane.gc3D.strokeLine(pTop0[0], pTop0[1], pBot1[0], pBot1[1]);
                    pane.gc3D.strokeLine(pTop1[0], pTop1[1], pBot0[0], pBot0[1]);
                    pane.gc3D.setFill(Color.web("#451a03"));
                    pane.gc3D.fillOval(midX - rSc, midY - rSc, rSc * 2, rSc * 2);
                }

                pane.gc3D.setStroke(matCol.darker());
                pane.gc3D.setLineWidth(0.3);
                pane.gc3D.strokePolygon(pxs, pys, 4);
            }
        }
    }

    static void draw3DHoverOverlay(WorldEditorPane pane, double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        if (!pane.isVoxelInfoVisible || pane.hover3DCell == null) return;
        int gx = pane.hover3DCell[0];
        int gy = pane.hover3DCell[1];
        int gd = pane.hover3DCell.length > 2 ? pane.hover3DCell[2] : 0;
        if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE) return;

        double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        double targetDepthVal = pane.depthSlider != null ? pane.depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;

        if (gx == cutXLimit && cutXLimit > 0 && cutXLimit < GRID_SIZE) {
            double stepY = 2;
            double layerDepthPx = maxDepthPx / SOIL_DEPTH;
            double surfZ0 = pane.heightGrid[gx][gy] * 40.0;
            double surfZ1 = pane.heightGrid[gx][Math.min(GRID_SIZE - 1, gy + (int) stepY)] * 40.0;
            if (pane.carvedVoxelGrid[gx][gy]) surfZ0 -= 15.0;

            double refZ0 = 32.0;
            double refZ1 = 32.0;
            double topZ0 = Math.min(surfZ0, refZ0 - gd * layerDepthPx);
            double topZ1 = Math.min(surfZ1, refZ1 - gd * layerDepthPx);
            double botZ0 = Math.min(surfZ0, refZ0 - (gd + 1) * layerDepthPx);
            double botZ1 = Math.min(surfZ1, refZ1 - (gd + 1) * layerDepthPx);

            double[] pTop0 = project3DPoint(gx, gy, topZ0, cx, cy, scale, radAz, radEl);
            double[] pTop1 = project3DPoint(gx, Math.min(GRID_SIZE - 1, gy + (int) stepY), topZ1, cx, cy, scale, radAz, radEl);
            double[] pBot1 = project3DPoint(gx, Math.min(GRID_SIZE - 1, gy + (int) stepY), botZ1, cx, cy, scale, radAz, radEl);
            double[] pBot0 = project3DPoint(gx, gy, botZ0, cx, cy, scale, radAz, radEl);

            double[] pxs = {pTop0[0], pTop1[0], pBot1[0], pBot0[0]};
            double[] pys = {pTop0[1], pTop1[1], pBot1[1], pBot0[1]};

            pane.gc3D.setFill(Color.web("#38bdf8", 0.65));
            pane.gc3D.fillPolygon(pxs, pys, 4);
            pane.gc3D.setStroke(Color.web("#7dd3fc"));
            pane.gc3D.setLineWidth(2.2);
            pane.gc3D.strokePolygon(pxs, pys, 4);
        } else {
            double z0 = pane.heightGrid[gx][gy] * 40.0;
            int step = 2;
            int gx2 = Math.min(GRID_SIZE - 1, gx + step);
            int gy2 = Math.min(GRID_SIZE - 1, gy + step);
            double z1 = pane.heightGrid[gx2][gy] * 40.0;
            double z2 = pane.heightGrid[gx2][gy2] * 40.0;
            double z3 = pane.heightGrid[gx][gy2] * 40.0;

            double[] p0 = project3DPoint(gx, gy, z0, cx, cy, scale, radAz, radEl);
            double[] p1 = project3DPoint(gx2, gy, z1, cx, cy, scale, radAz, radEl);
            double[] p2 = project3DPoint(gx2, gy2, z2, cx, cy, scale, radAz, radEl);
            double[] p3 = project3DPoint(gx, gy2, z3, cx, cy, scale, radAz, radEl);

            pane.gc3D.setFill(Color.web("#f59e0b", 0.45));
            pane.gc3D.fillPolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
            pane.gc3D.setStroke(Color.web("#fbbf24"));
            pane.gc3D.setLineWidth(2.0);
            pane.gc3D.strokePolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
        }
    }

    static void drawMetricScaleBar3D(WorldEditorPane pane, double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        double sideM = pane.surfaceSizeSlider != null ? pane.surfaceSizeSlider.getValue() : 2.5;
        double refM;
        String scaleLabel;
        if (sideM >= 50.0) { refM = 10.0; scaleLabel = "10 m"; }
        else if (sideM >= 10.0) { refM = 2.0; scaleLabel = "2 m"; }
        else if (sideM >= 2.0) { refM = 0.5; scaleLabel = "50 cm"; }
        else { refM = 0.1; scaleLabel = "10 cm"; }

        double[] p0 = project3DPoint(0, 0, 0, cx, cy, scale, radAz, radEl);
        double[] p1 = project3DPoint(GRID_SIZE, 0, 0, cx, cy, scale, radAz, radEl);
        double projLen = Math.hypot(p1[0] - p0[0], p1[1] - p0[1]);

        double barWidthPx = Math.max(35.0, (refM / sideM) * projLen);

        double bx = w - barWidthPx - 20.0;
        double by = h - 25.0;

        pane.gc3D.setStroke(Color.web("#38bdf8"));
        pane.gc3D.setLineWidth(2.5);
        pane.gc3D.strokeLine(bx, by, bx + barWidthPx, by);
        pane.gc3D.strokeLine(bx, by - 4, bx, by + 4);
        pane.gc3D.strokeLine(bx + barWidthPx, by - 4, bx + barWidthPx, by + 4);

        pane.gc3D.setFill(Color.web("#38bdf8"));
        pane.gc3D.setFont(Font.font("System", FontWeight.BOLD, 10));
        pane.gc3D.fillText(scaleLabel, bx + barWidthPx / 2.0 - 14.0, by - 6.0);
    }

    static Color getGaussianSplatSoilColor(WorldEditorPane pane, int x, int y) {
        double rAcc = 0.0, gAcc = 0.0, bAcc = 0.0;
        double totalWeight = 0.0;
        int radius = 2;
        double sigma = 1.25;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = Math.min(GRID_SIZE - 1, Math.max(0, x + dx));
                int ny = Math.min(GRID_SIZE - 1, Math.max(0, y + dy));
                byte nMat = pane.soilLayers[nx][ny][0];
                if (!pane.isMaterialVisible(nMat)) continue;

                double distSq = dx * dx + dy * dy;
                double w = Math.exp(-distSq / (2.0 * sigma * sigma));
                Color c = TerrainMaterialPalette.getMaterialColor(nMat);

                rAcc += c.getRed() * w;
                gAcc += c.getGreen() * w;
                bAcc += c.getBlue() * w;
                totalWeight += w;
            }
        }

        Color blendedCol;
        if (totalWeight <= 0.001) {
            blendedCol = TerrainMaterialPalette.getMaterialColor(pane.soilLayers[x][y][0]);
        } else {
            blendedCol = Color.color(
                Math.min(1.0, Math.max(0.0, rAcc / totalWeight)),
                Math.min(1.0, Math.max(0.0, gAcc / totalWeight)),
                Math.min(1.0, Math.max(0.0, bAcc / totalWeight))
            );
        }

        double macroNoise = pane.valueNoise3D(x * 0.25, y * 0.25, 0.5);
        double microNoise = pane.valueNoise3D(x * 1.5, y * 1.5, 2.5);
        double combinedNoise = (macroNoise * 0.14 - 0.07) + (microNoise * 0.08 - 0.04);

        double r = Math.min(1.0, Math.max(0.0, blendedCol.getRed() + combinedNoise));
        double g = Math.min(1.0, Math.max(0.0, blendedCol.getGreen() + combinedNoise * 0.9));
        double b = Math.min(1.0, Math.max(0.0, blendedCol.getBlue() + combinedNoise * 0.8));

        return Color.color(r, g, b, blendedCol.getOpacity());
    }
}
