/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import java.util.Random;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.client.ui.world.TerrainMaterialPalette;
import org.swarmforge.client.util.ThemeManager;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;

/**
 * 2.5D scientific and voxel terrain renderer for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditor3DTechnicalRenderer {

    private WorldEditor3DTechnicalRenderer() {
    }

    static double[] project3DPoint(double x, double y, double z, double cx, double cy, double scale, double radAz, double radEl) {
        return WorldEditorVoxelDrawingHelper.project3DPoint(x, y, z, cx, cy, scale, radAz, radEl);
    }

    static boolean isPointOccluded(WorldEditorPane pane, double gx, double gy, double gz, double radAz, double radEl) {
        return WorldEditorVoxelDrawingHelper.isPointOccluded(pane, gx, gy, gz, radAz, radEl);
    }

    static void drawVoxelBlockTexture(WorldEditorPane pane, double[] pxs, double[] pys, byte mat, boolean isTop, boolean isSide, boolean isRightSide, int gx, int gy, int gz) {
        WorldEditorVoxelDrawingHelper.drawVoxelBlockTexture(pane, pxs, pys, mat, isTop, isSide, isRightSide, gx, gy, gz);
    }

    static void drawGamifiedVoxelCube3D(
        WorldEditorPane pane,
        double wx, double wy, double wz,
        double sizeX, double sizeY, double sizeZ,
        Color topCol, Color sideCol,
        double cx, double cy, double scale, double radAz, double radEl
    ) {
        WorldEditorVoxelDrawingHelper.drawGamifiedVoxelCube3D(pane, wx, wy, wz, sizeX, sizeY, sizeZ, topCol, sideCol, cx, cy, scale, radAz, radEl);
    }

    static void draw3DTechnical(WorldEditorPane pane) {
        if (pane.canvas3D == null || pane.canvas3D.getWidth() < 10 || pane.canvas3D.getHeight() < 10) return;
        double w = pane.canvas3D.getWidth();
        double h = pane.canvas3D.getHeight();

        if (pane.currentRenderMode == RenderMode.SCIENTIFIC) {
            LinearGradient skyGrad = new LinearGradient(
                0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#38bdf8")),
                new Stop(0.5, Color.web("#7dd3fc")),
                new Stop(1.0, Color.web("#bae6fd"))
            );
            pane.gc3D.setFill(skyGrad);
        } else if (pane.currentRenderMode == RenderMode.GAMIFIED) {
            pane.gc3D.setFill(Color.web("#1e1b4b"));
        } else {
            boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
            pane.gc3D.setFill(isDark ? Color.web("#0b0f19") : Color.web("#e2e8f0"));
        }
        pane.gc3D.fillRect(0, 0, w, h);

        double radAz = Math.toRadians(pane.azimuth);
        double radEl = Math.toRadians(pane.elevation);

        double cx = w / 2 + pane.pan3DX;
        double cy = h / 2 + pane.pan3DY + 40;
        double scale = pane.zoom * 12.0;

        int step = 2;
        double sideM = pane.surfaceSizeSlider != null ? pane.surfaceSizeSlider.getValue() : 25.0;
        double targetDepthVal = pane.depthSlider != null ? pane.depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;

        if (pane.showSubstrateStratigraphyCheck == null || pane.showSubstrateStratigraphyCheck.isSelected()) {
            pane.stratigraphyRenderer.drawStratigraphySideWalls3D(pane, pane.gc3D, cx, cy, scale, radAz, radEl, maxDepthPx, step, true);
        }

        double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        cutXLimit = (cutXLimit / step) * step;
        cutXLimit = Math.max(step, Math.min(GRID_SIZE, cutXLimit));
        boolean isScanning = false;
        boolean isCutPlaneFacingCamera = Math.sin(radAz) >= 0;

        if (!isCutPlaneFacingCamera && (cutRatio < 0.99 || isScanning)) {
            WorldEditor3DCutPlaneAndOverlayRenderer.drawSubterraneanCutPlane(pane, cutXLimit, cx, cy, scale, radAz, radEl, maxDepthPx, isScanning);
        }

        if (pane.isTerrainVisible) {
            boolean sinPos = Math.sin(radAz) >= 0;
            boolean cosPos = Math.cos(radAz) >= 0;

            if (pane.currentRenderMode == RenderMode.GAMIFIED) {
                double[][] blockZ = new double[GRID_SIZE][GRID_SIZE];
                for (int x = 0; x < GRID_SIZE; x += step) {
                    for (int y = 0; y < GRID_SIZE; y += step) {
                        double rawZ = pane.heightGrid[x][y] * 40.0;
                        if (pane.carvedVoxelGrid[x][y]) rawZ -= 15.0;
                        blockZ[x][y] = Math.floor(rawZ / 4.0) * 4.0;
                    }
                }

                int limitX = Math.min(GRID_SIZE, cutXLimit);
                int xStart = sinPos ? 0 : limitX - step;
                int xEnd = sinPos ? limitX : -step;
                int xDir = sinPos ? step : -step;

                int yStart = cosPos ? 0 : GRID_SIZE - step;
                int yEnd = cosPos ? GRID_SIZE : -step;
                int yDir = cosPos ? step : -step;

                for (int x = xStart; (xDir > 0 ? x < xEnd : x >= 0); x += xDir) {
                    for (int y = yStart; (yDir > 0 ? y < yEnd : y >= 0); y += yDir) {
                        double zTop = blockZ[x][y];

                        double[] p0 = project3DPoint(x, y, zTop, cx, cy, scale, radAz, radEl);
                        double[] p1 = project3DPoint(x + step, y, zTop, cx, cy, scale, radAz, radEl);
                        double[] p2 = project3DPoint(x + step, y + step, zTop, cx, cy, scale, radAz, radEl);
                        double[] p3 = project3DPoint(x, y + step, zTop, cx, cy, scale, radAz, radEl);

                        byte mat = pane.soilLayers[x][y][0];
                        if (pane.riverCheck != null && pane.riverCheck.isSelected() && WorldEditorGeneratorHelper.isNearRiver(pane, x, y, 2)) {
                            mat = 8;
                        }
                        drawVoxelBlockTexture(pane, new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, mat, true, false, false, x, y, (int) zTop);

                        double zFront = (y + step < GRID_SIZE) ? blockZ[x][y + step] : 0.0;
                        if (zFront < zTop && cosPos) {
                            double[] f2 = project3DPoint(x + step, y + step, zFront, cx, cy, scale, radAz, radEl);
                            double[] f3 = project3DPoint(x, y + step, zFront, cx, cy, scale, radAz, radEl);
                            drawVoxelBlockTexture(pane, new double[]{p3[0], p2[0], f2[0], f3[0]}, new double[]{p3[1], p2[1], f2[1], f3[1]}, mat, false, true, false, x, y, (int) zTop);
                        }

                        double zBack = (y - step >= 0) ? blockZ[x][y - step] : 0.0;
                        if (zBack < zTop && !cosPos) {
                            double[] b0 = project3DPoint(x, y, zBack, cx, cy, scale, radAz, radEl);
                            double[] b1 = project3DPoint(x + step, y, zBack, cx, cy, scale, radAz, radEl);
                            drawVoxelBlockTexture(pane, new double[]{p0[0], p1[0], b1[0], b0[0]}, new double[]{p0[1], p1[1], b1[1], b0[1]}, mat, false, true, false, x, y, (int) zTop);
                        }

                        double zRight = (x + step < cutXLimit) ? blockZ[x + step][y] : 0.0;
                        if (zRight < zTop && sinPos) {
                            double[] r2 = project3DPoint(x + step, y + step, zRight, cx, cy, scale, radAz, radEl);
                            double[] r3 = project3DPoint(x + step, y, zRight, cx, cy, scale, radAz, radEl);
                            drawVoxelBlockTexture(pane, new double[]{p1[0], p2[0], r2[0], r3[0]}, new double[]{p1[1], p2[1], r2[1], r3[1]}, mat, false, true, true, x, y, (int) zTop);
                        }

                        double zLeft = (x - step >= 0) ? blockZ[x - step][y] : 0.0;
                        if (zLeft < zTop && !sinPos) {
                            double[] l0 = project3DPoint(x, y, zLeft, cx, cy, scale, radAz, radEl);
                            double[] l3 = project3DPoint(x, y + step, zLeft, cx, cy, scale, radAz, radEl);
                            drawVoxelBlockTexture(pane, new double[]{p0[0], p3[0], l3[0], l0[0]}, new double[]{p0[1], p3[1], l3[1], l0[1]}, mat, false, true, true, x, y, (int) zTop);
                        }
                    }
                }
            } else {
                int limitX = Math.min(GRID_SIZE, cutXLimit);
                int xStart = sinPos ? 0 : limitX - step;
                int xEnd = sinPos ? limitX : -step;
                int xDir = sinPos ? step : -step;

                int yStart = cosPos ? 0 : GRID_SIZE - step;
                int yEnd = cosPos ? GRID_SIZE : -step;
                int yDir = cosPos ? step : -step;

                for (int x = xStart; (xDir > 0 ? x < xEnd : x >= 0); x += xDir) {
                    for (int y = yStart; (yDir > 0 ? y < yEnd : y >= 0); y += yDir) {
                        double z0 = pane.heightGrid[x][y] * 40.0;
                        double z1 = pane.heightGrid[Math.min(GRID_SIZE - 1, x + step)][y] * 40.0;
                        double z2 = pane.heightGrid[Math.min(GRID_SIZE - 1, x + step)][Math.min(GRID_SIZE - 1, y + step)] * 40.0;
                        double z3 = pane.heightGrid[x][Math.min(GRID_SIZE - 1, y + step)] * 40.0;

                        if (pane.carvedVoxelGrid[x][y]) z0 -= 15.0;
                        if (pane.carvedVoxelGrid[Math.min(GRID_SIZE - 1, x + step)][y]) z1 -= 15.0;
                        if (pane.carvedVoxelGrid[Math.min(GRID_SIZE - 1, x + step)][Math.min(GRID_SIZE - 1, y + step)]) z2 -= 15.0;
                        if (pane.carvedVoxelGrid[x][Math.min(GRID_SIZE - 1, y + step)]) z3 -= 15.0;

                        double[] p0 = project3DPoint(x, y, z0, cx, cy, scale, radAz, radEl);
                        double[] p1 = project3DPoint(x + step, y, z1, cx, cy, scale, radAz, radEl);
                        double[] p2 = project3DPoint(x + step, y + step, z2, cx, cy, scale, radAz, radEl);
                        double[] p3 = project3DPoint(x, y + step, z3, cx, cy, scale, radAz, radEl);

                        double[] pxs = new double[]{p0[0], p1[0], p2[0], p3[0]};
                        double[] pys = new double[]{p0[1], p1[1], p2[1], p3[1]};

                        byte mat = pane.soilLayers[x][y][0];
                        boolean visibleMat = pane.isMaterialVisible(mat);

                        double humFactor = pane.humidityGrid[x][y][0];
                        Color baseMatCol;
                        if (visibleMat) {
                            if (pane.currentRenderMode == RenderMode.REALISTIC) {
                                baseMatCol = WorldEditor3DCutPlaneAndOverlayRenderer.getGaussianSplatSoilColor(pane, x, y);
                            } else {
                                baseMatCol = TerrainMaterialPalette.getMaterialColor(mat);
                            }
                        } else {
                            baseMatCol = Color.web("#1e293b", 0.35);
                        }

                        if (x + step >= cutXLimit && isScanning) {
                            baseMatCol = Color.web("#38bdf8");
                        }
                        double dzdx = ((z1 + z2) - (z0 + z3)) * 0.025;
                        double dzdy = ((z2 + z3) - (z0 + z1)) * 0.025;
                        double sunSlopeFactor = (dzdx * -0.5 + dzdy * 0.5);
                        float weatherLight = (pane.activeSimulation != null && pane.activeSimulation.getWeather() != null) ? pane.activeSimulation.getWeather().getLightLevel() : 1.0f;
                        double sunLightMultiplier = Math.max(0.85, Math.min(1.45, 1.15 + sunSlopeFactor * 0.65)) * Math.max(0.75f, weatherLight * 1.15f);

                        Color col = baseMatCol.deriveColor(0, 1.05, Math.max(0.70, sunLightMultiplier * (1.0 - humFactor * 0.12)), 1.0);
                        if (pane.carvedVoxelGrid[x][y]) col = Color.web("#d97706");

                        pane.gc3D.setFill(col);
                        pane.gc3D.fillPolygon(pxs, pys, 4);

                        if (pane.showElevationIsolinesCheck != null && pane.showElevationIsolinesCheck.isSelected()) {
                            double normZ = pane.heightGrid[x][y];
                            double isoStep = 0.05;
                            int nx = Math.min(GRID_SIZE - 1, x + step);
                            int ny = Math.min(GRID_SIZE - 1, y + step);
                            if ((int) (normZ / isoStep) != (int) (pane.heightGrid[nx][y] / isoStep) ||
                                (int) (normZ / isoStep) != (int) (pane.heightGrid[x][ny] / isoStep)) {
                                pane.gc3D.setStroke(Color.web("#38bdf8", 0.90));
                                pane.gc3D.setLineWidth(1.2);
                                pane.gc3D.strokeLine(p0[0], p0[1], p2[0], p2[1]);
                            }
                        }

                        if (pane.showClimateIsolinesCheck != null && pane.showClimateIsolinesCheck.isSelected() && pane.humidityGrid != null) {
                            float hum = pane.humidityGrid[x][y][0];
                            double humStep = 0.10;
                            int nx = Math.min(GRID_SIZE - 1, x + step);
                            int ny = Math.min(GRID_SIZE - 1, y + step);
                            if ((int) (hum / humStep) != (int) (pane.humidityGrid[nx][y][0] / humStep) ||
                                (int) (hum / humStep) != (int) (pane.humidityGrid[x][ny][0] / humStep)) {
                                pane.gc3D.setStroke(Color.web("#06b6d4", 0.90));
                                pane.gc3D.setLineWidth(1.2);
                                pane.gc3D.strokeLine(p1[0], p1[1], p3[0], p3[1]);
                            }
                        }

                        if (pane.showPheromoneIsolinesCheck != null && pane.showPheromoneIsolinesCheck.isSelected() && pane.activeSimulation != null) {
                            org.swarmforge.core.gpu.SparsePheromoneGrid pGrid = pane.activeSimulation.getPheromoneGrid();
                            if (pGrid != null) {
                                float val0 = pGrid.read(x, y, 0, 1);
                                if (val0 > 0.05f) {
                                    pane.gc3D.setStroke(Color.web("#ec4899", 0.85));
                                    pane.gc3D.setLineWidth(1.5);
                                    pane.gc3D.strokeLine(p0[0], p0[1], p2[0], p2[1]);
                                }
                            }
                        }

                        if (pane.currentRenderMode != RenderMode.REALISTIC) {
                            pane.gc3D.setStroke(col.darker());
                            pane.gc3D.setLineWidth(visibleMat ? 0.3 : 0.1);
                            pane.gc3D.strokePolygon(pxs, pys, 4);
                        }

                        if (mat == 5 && visibleMat && (x + y) % 3 == 0) {
                            double zG = z0 + 0.8;
                            double[] pG = project3DPoint(x, y, zG, cx, cy, scale, radAz, radEl);
                            double gSc = (pane.zoom / 7.5);
                            pane.gc3D.setFill(Color.web("#78350f"));
                            pane.gc3D.fillOval(pG[0] - 2 * gSc, pG[1] - 1 * gSc, 4 * gSc, 2.5 * gSc);
                            pane.gc3D.setFill(Color.web("#b45309"));
                            pane.gc3D.fillOval(pG[0] - 1 * gSc, pG[1] - 1.5 * gSc, 3 * gSc, 1.8 * gSc);
                        }

                        if (pane.currentRenderMode == RenderMode.REALISTIC && mat == 0 && (x + y) % 5 == 0 && (pane.showVegetationCheck == null || pane.showVegetationCheck.isSelected())) {
                            double zG = z0 + 1.0;
                            double[] pG = project3DPoint(x, y, zG, cx, cy, scale, radAz, radEl);
                            double gSc = (pane.zoom / 7.5);

                            pane.gc3D.setStroke(Color.web("#4ade80"));
                            pane.gc3D.setLineWidth(1.2 * gSc);
                            pane.gc3D.strokeLine(pG[0], pG[1], pG[0] - 2 * gSc, pG[1] - 6 * gSc);
                            pane.gc3D.strokeLine(pG[0], pG[1], pG[0] + 3 * gSc, pG[1] - 7 * gSc);
                            pane.gc3D.strokeLine(pG[0], pG[1], pG[0] + 1 * gSc, pG[1] - 9 * gSc);

                            if ((x * 7 + y * 13) % 11 == 0) {
                                pane.gc3D.setFill(Color.web("#facc15"));
                                pane.gc3D.fillOval(pG[0] + 1 * gSc - 1.5, pG[1] - 9 * gSc - 1.5, 3.0 * gSc, 3.0 * gSc);
                            }
                        }
                    }
                }
            }
        }

        if (isCutPlaneFacingCamera && (cutRatio < 0.99 || isScanning)) {
            WorldEditor3DCutPlaneAndOverlayRenderer.drawSubterraneanCutPlane(pane, cutXLimit, cx, cy, scale, radAz, radEl, maxDepthPx, isScanning);
        }

        if (pane.isTerrainVisible) {
            if (pane.riverCheck != null && pane.riverCheck.isSelected() && pane.riverPath != null && pane.riverPath.size() > 1) {
                double rWidthPx = Math.max(4.0, (pane.riverWidthSlider != null ? pane.riverWidthSlider.getValue() : 120.0) / 20.0 * (pane.zoom / 7.5));
                boolean isWinterRiver = pane.simSeason != null && (pane.simSeason.toLowerCase().contains("hiver") || pane.simSeason.toLowerCase().contains("winter"));
                boolean isDryRiver = pane.simSeason != null && (pane.simSeason.toLowerCase().contains("été") || pane.simSeason.toLowerCase().contains("summer")) && pane.baseHumiditySlider != null && pane.baseHumiditySlider.getValue() < 0.25;

                Color rCol = isWinterRiver ? Color.web("#e0f2fe", 0.95) : (isDryRiver ? Color.web("#78350f", 0.85) : Color.web("#0284c7", 0.90));
                Color rHighlightCol = isWinterRiver ? Color.web("#ffffff", 0.90) : (isDryRiver ? Color.web("#a16207", 0.70) : Color.web("#7dd3fc", 0.75));

                double camStepX = Math.sin(radAz);
                double camStepY = Math.cos(radAz);
                double tanEl = Math.tan(radEl);

                pane.gc3D.setStroke(rCol);
                pane.gc3D.setLineWidth(rWidthPx);
                pane.gc3D.setLineCap(StrokeLineCap.ROUND);
                pane.gc3D.setLineJoin(StrokeLineJoin.ROUND);

                boolean inSegment = false;
                for (int[] rPt : pane.riverPath) {
                    int rx = rPt[0], ry = rPt[1];
                    if (rx > cutXLimit) {
                        if (inSegment) { pane.gc3D.stroke(); inSegment = false; }
                        continue;
                    }
                    double rz = pane.heightGrid[rx][ry] * 40.0 + 0.6;

                    boolean isOccluded = false;
                    for (int rayStep = 1; rayStep <= 30; rayStep++) {
                        int checkX = (int) Math.round(rx + camStepX * rayStep);
                        int checkY = (int) Math.round(ry + camStepY * rayStep);
                        if (checkX < 0 || checkX >= GRID_SIZE || checkY < 0 || checkY >= GRID_SIZE) break;
                        double rayZ = rz + rayStep * tanEl * 0.8;
                        if (pane.heightGrid[checkX][checkY] * 40.0 > rayZ + 0.8) {
                            isOccluded = true;
                            break;
                        }
                    }

                    if (isOccluded) {
                        if (inSegment) { pane.gc3D.stroke(); inSegment = false; }
                        continue;
                    }

                    double[] rP = project3DPoint(rx, ry, rz, cx, cy, scale, radAz, radEl);
                    if (!inSegment) {
                        pane.gc3D.beginPath();
                        pane.gc3D.moveTo(rP[0], rP[1]);
                        inSegment = true;
                    } else {
                        pane.gc3D.lineTo(rP[0], rP[1]);
                    }
                }
                if (inSegment) {
                    pane.gc3D.stroke();
                }

                pane.gc3D.setStroke(rHighlightCol);
                pane.gc3D.setLineWidth(Math.max(1.5, rWidthPx * 0.45));
                inSegment = false;
                for (int[] rPt : pane.riverPath) {
                    int rx = rPt[0], ry = rPt[1];
                    if (rx > cutXLimit) {
                        if (inSegment) { pane.gc3D.stroke(); inSegment = false; }
                        continue;
                    }
                    double rz = pane.heightGrid[rx][ry] * 40.0 + 0.8;

                    boolean isOccluded = false;
                    for (int rayStep = 1; rayStep <= 30; rayStep++) {
                        int checkX = (int) Math.round(rx + camStepX * rayStep);
                        int checkY = (int) Math.round(ry + camStepY * rayStep);
                        if (checkX < 0 || checkX >= GRID_SIZE || checkY < 0 || checkY >= GRID_SIZE) break;
                        double rayZ = rz + rayStep * tanEl * 0.8;
                        if (pane.heightGrid[checkX][checkY] * 40.0 > rayZ + 0.8) {
                            isOccluded = true;
                            break;
                        }
                    }

                    if (isOccluded) {
                        if (inSegment) { pane.gc3D.stroke(); inSegment = false; }
                        continue;
                    }

                    double[] rP = project3DPoint(rx, ry, rz, cx, cy, scale, radAz, radEl);
                    if (!inSegment) {
                        pane.gc3D.beginPath();
                        pane.gc3D.moveTo(rP[0] - 0.5, rP[1] - 0.5);
                        inSegment = true;
                    } else {
                        pane.gc3D.lineTo(rP[0] - 0.5, rP[1] - 0.5);
                    }
                }
                if (inSegment) {
                    pane.gc3D.stroke();
                }
            }

            if (pane.staticPoolsSlider != null && pane.staticPoolsSlider.getValue() > 0) {
                int poolCount = (int) pane.staticPoolsSlider.getValue();
                Random pRand = new Random(1234);
                for (int i = 0; i < poolCount; i++) {
                    int poolX = 20 + (i * 27 + pRand.nextInt(15)) % (GRID_SIZE - 40);
                    int poolY = 20 + (i * 31 + pRand.nextInt(15)) % (GRID_SIZE - 40);
                    if (poolX > cutXLimit) continue;
                    double baseRadius = 3.5 + (i % 3) * 1.5;

                    double minTerrainH = pane.heightGrid[poolX][poolY];
                    for (int dx = -3; dx <= 3; dx++) {
                        for (int dy = -3; dy <= 3; dy++) {
                            int gx = Math.min(GRID_SIZE - 1, Math.max(0, poolX + dx));
                            int gy = Math.min(GRID_SIZE - 1, Math.max(0, poolY + dy));
                            minTerrainH = Math.min(minTerrainH, pane.heightGrid[gx][gy]);
                        }
                    }
                    double flatWaterZ = (minTerrainH + 0.02) * 40.0;
                    if (isPointOccluded(pane, poolX, poolY, flatWaterZ, radAz, radEl)) continue;

                    int numPts = 16;
                    double[] xPts = new double[numPts];
                    double[] yPts = new double[numPts];
                    for (int p = 0; p < numPts; p++) {
                        double angle = (2.0 * Math.PI * p) / numPts;
                        double rPerturb = baseRadius * (0.85 + 0.30 * Math.sin(angle * 3.0 + i));
                        double vx = poolX + Math.cos(angle) * rPerturb;
                        double vy = poolY + Math.sin(angle) * rPerturb;
                        double[] proj = project3DPoint(vx, vy, flatWaterZ, cx, cy, scale, radAz, radEl);
                        xPts[p] = proj[0];
                        yPts[p] = proj[1];
                    }

                    pane.gc3D.setFill(Color.web("#0284c7", 0.85));
                    pane.gc3D.fillPolygon(xPts, yPts, numPts);
                    pane.gc3D.setStroke(Color.web("#38bdf8", 0.7));
                    pane.gc3D.setLineWidth(1.2 * (pane.zoom / 7.5));
                    pane.gc3D.strokePolygon(xPts, yPts, numPts);
                }
            }
        }

        if (pane.showSubstrateStratigraphyCheck == null || pane.showSubstrateStratigraphyCheck.isSelected()) {
            pane.stratigraphyRenderer.drawStratigraphySideWalls3D(pane, pane.gc3D, cx, cy, scale, radAz, radEl, maxDepthPx, step, false);
        }

        WorldEditor3DFloraAndOverlayRenderer.renderFloraTreesAndSimulationOverlays(
            pane, w, h, cx, cy, scale, radAz, radEl, cutXLimit, sideM, step
        );
    }

    static void draw3DHoverOverlay(WorldEditorPane pane, double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DCutPlaneAndOverlayRenderer.draw3DHoverOverlay(pane, w, h, cx, cy, scale, radAz, radEl);
    }

    static void drawMetricScaleBar3D(WorldEditorPane pane, double w, double h, double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DCutPlaneAndOverlayRenderer.drawMetricScaleBar3D(pane, w, h, cx, cy, scale, radAz, radEl);
    }
}
