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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.client.ui.world.TerrainMaterialPalette;
import org.swarmforge.client.util.ThemeManager;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;

/**
 * 2.5D scientific and voxel terrain renderer, subterranean cut-plane profile,
 * planar water bodies, 3D hover highlight, and metric scale bar renderer
 * for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditor3DTechnicalRenderer {

    private WorldEditor3DTechnicalRenderer() {
    }

    static double[] project3DPoint(double x, double y, double z, double cx, double cy, double scale, double radAz, double radEl) {
        double isoX = (x - GRID_SIZE / 2.0) * Math.cos(radAz) - (y - GRID_SIZE / 2.0) * Math.sin(radAz);
        double isoY = (x - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (y - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - z * Math.cos(radEl);
        double px = cx + isoX * (scale / 10.0);
        double py = cy + isoY * (scale / 10.0);
        return new double[]{px, py};
    }

    static boolean isPointOccluded(WorldEditorPane pane, double gx, double gy, double gz, double radAz, double radEl) {
        double camStepX = Math.sin(radAz);
        double camStepY = Math.cos(radAz);
        double tanEl = Math.tan(radEl);

        for (int rayStep = 1; rayStep <= 30; rayStep++) {
            int checkX = (int) Math.round(gx + camStepX * rayStep);
            int checkY = (int) Math.round(gy + camStepY * rayStep);
            if (checkX < 0 || checkX >= GRID_SIZE || checkY < 0 || checkY >= GRID_SIZE) break;
            double rayZ = gz + rayStep * tanEl * 0.8;
            if (pane.heightGrid[checkX][checkY] * 40.0 > rayZ + 0.8) {
                return true;
            }
        }
        return false;
    }

    static void drawVoxelBlockTexture(WorldEditorPane pane, double[] pxs, double[] pys, byte mat, boolean isTop, boolean isSide, boolean isRightSide, int gx, int gy, int gz) {
        Color baseCol = switch (mat) {
            case 0 -> isTop ? Color.web("#15803d") : Color.web("#78350f");
            case 1 -> isSide ? Color.web("#ca8a04") : Color.web("#eab308");
            case 2 -> isSide ? Color.web("#92400e") : Color.web("#b45309");
            case 3 -> isSide ? Color.web("#334155") : Color.web("#64748b");
            case 4 -> isSide ? Color.web("#475569") : Color.web("#94a3b8");
            case 5 -> isSide ? Color.web("#854d0e") : Color.web("#ca8a04");
            case 6 -> isSide ? Color.web("#291605") : Color.web("#451a03");
            case 7 -> Color.web("#090d16", 0.92);
            case 8 -> isTop ? Color.web("#0284c7") : Color.web("#1d4ed8");
            default -> isSide ? Color.web("#451a03") : Color.web("#78350f");
        };
        if (isRightSide) baseCol = baseCol.darker();

        pane.gc3D.setFill(baseCol);
        pane.gc3D.fillPolygon(pxs, pys, 4);

        double midX = (pxs[0] + pxs[1] + pxs[2] + pxs[3]) / 4.0;
        double midY = (pys[0] + pys[1] + pys[2] + pys[3]) / 4.0;
        double faceW = Math.abs(pxs[1] - pxs[0]) + Math.abs(pys[2] - pys[1]);

        if (faceW > 3.0) {
            int hash = (gx * 73856093) ^ (gy * 19349663) ^ (gz * 83492791);
            Random pRand = new Random(hash);

            for (int p = 0; p < 5; p++) {
                double rx = (pRand.nextDouble() - 0.5) * 0.7;
                double ry = (pRand.nextDouble() - 0.5) * 0.7;
                double dotX = midX + rx * (pxs[1] - pxs[0]) + ry * (pxs[3] - pxs[0]);
                double dotY = midY + rx * (pys[1] - pys[0]) + ry * (pys[3] - pys[0]);
                double dotSz = Math.max(1.2, faceW * 0.15);

                Color speckCol = switch (mat) {
                    case 0 -> isTop ? (pRand.nextBoolean() ? Color.web("#86efac") : Color.web("#14532d"))
                                    : (pRand.nextBoolean() ? Color.web("#451a03") : Color.web("#9a3412"));
                    case 1 -> pRand.nextBoolean() ? Color.web("#fde047") : Color.web("#a16207");
                    case 2 -> pRand.nextBoolean() ? Color.web("#d97706") : Color.web("#7c2d12");
                    case 3 -> pRand.nextBoolean() ? Color.web("#94a3b8") : Color.web("#1e293b");
                    case 4 -> pRand.nextBoolean() ? Color.web("#cbd5e1") : Color.web("#1e293b");
                    case 5 -> pRand.nextBoolean() ? Color.web("#fef08a") : Color.web("#713f12");
                    case 7 -> pRand.nextBoolean() ? Color.web("#0f172a") : Color.web("#1e293b");
                    default -> pRand.nextBoolean() ? Color.web("#451a03") : Color.web("#9a3412");
                };
                pane.gc3D.setFill(speckCol);
                pane.gc3D.fillRect(dotX - dotSz / 2, dotY - dotSz / 2, dotSz, dotSz);
            }

            if (mat == 0 && isSide) {
                pane.gc3D.setStroke(Color.web("#22c55e", 0.95));
                pane.gc3D.setLineWidth(Math.max(1.5, faceW * 0.12));
                pane.gc3D.strokeLine(pxs[0], pys[0], pxs[1], pys[1]);
            }
        }

        pane.gc3D.setStroke(Color.web("#0f172a", 0.85));
        pane.gc3D.setLineWidth(1.4);
        pane.gc3D.strokePolygon(pxs, pys, 4);
    }

    static void drawGamifiedVoxelCube3D(
        WorldEditorPane pane,
        double wx, double wy, double wz,
        double sizeX, double sizeY, double sizeZ,
        Color topCol, Color sideCol,
        double cx, double cy, double scale, double radAz, double radEl
    ) {
        double[] p0 = project3DPoint(wx, wy, wz + sizeZ, cx, cy, scale, radAz, radEl);
        double[] p1 = project3DPoint(wx + sizeX, wy, wz + sizeZ, cx, cy, scale, radAz, radEl);
        double[] p2 = project3DPoint(wx + sizeX, wy + sizeY, wz + sizeZ, cx, cy, scale, radAz, radEl);
        double[] p3 = project3DPoint(wx, wy + sizeY, wz + sizeZ, cx, cy, scale, radAz, radEl);

        double[] b0 = project3DPoint(wx, wy, wz, cx, cy, scale, radAz, radEl);
        double[] b1 = project3DPoint(wx + sizeX, wy, wz, cx, cy, scale, radAz, radEl);
        double[] b2 = project3DPoint(wx + sizeX, wy + sizeY, wz, cx, cy, scale, radAz, radEl);
        double[] b3 = project3DPoint(wx, wy + sizeY, wz, cx, cy, scale, radAz, radEl);

        pane.gc3D.setStroke(Color.web("#0f172a", 0.75));
        pane.gc3D.setLineWidth(1.0);

        boolean showRight = Math.sin(radAz) >= 0;
        boolean showFront = Math.cos(radAz) >= 0;

        if (showFront) {
            pane.gc3D.setFill(sideCol.darker());
            pane.gc3D.fillPolygon(new double[]{p3[0], p2[0], b2[0], b3[0]}, new double[]{p3[1], p2[1], b2[1], b3[1]}, 4);
            pane.gc3D.strokePolygon(new double[]{p3[0], p2[0], b2[0], b3[0]}, new double[]{p3[1], p2[1], b2[1], b3[1]}, 4);
        } else {
            pane.gc3D.setFill(sideCol.darker());
            pane.gc3D.fillPolygon(new double[]{p0[0], p1[0], b1[0], b0[0]}, new double[]{p0[1], p1[1], b1[1], b0[1]}, 4);
            pane.gc3D.strokePolygon(new double[]{p0[0], p1[0], b1[0], b0[0]}, new double[]{p0[1], p1[1], b1[1], b0[1]}, 4);
        }

        if (showRight) {
            pane.gc3D.setFill(sideCol);
            pane.gc3D.fillPolygon(new double[]{p1[0], p2[0], b2[0], b1[0]}, new double[]{p1[1], p2[1], b2[1], b1[1]}, 4);
            pane.gc3D.strokePolygon(new double[]{p1[0], p2[0], b2[0], b1[0]}, new double[]{p1[1], p2[1], b2[1], b1[1]}, 4);
        } else {
            pane.gc3D.setFill(sideCol);
            pane.gc3D.fillPolygon(new double[]{p0[0], p3[0], b3[0], b0[0]}, new double[]{p0[1], p3[1], b3[1], b0[1]}, 4);
            pane.gc3D.strokePolygon(new double[]{p0[0], p3[0], b3[0], b0[0]}, new double[]{p0[1], p3[1], b3[1], b0[1]}, 4);
        }

        pane.gc3D.setFill(topCol);
        pane.gc3D.fillPolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
        pane.gc3D.strokePolygon(new double[]{p0[0], p1[0], p2[0], p3[0]}, new double[]{p0[1], p1[1], p2[1], p3[1]}, 4);
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
            drawSubterraneanCutPlane(pane, cutXLimit, cx, cy, scale, radAz, radEl, maxDepthPx, isScanning);
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
                                baseMatCol = getGaussianSplatSoilColor(pane, x, y);
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

                            pane.gc3D.setStroke(Color.web("#4ade80")); pane.gc3D.setLineWidth(1.2 * gSc);
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
            drawSubterraneanCutPlane(pane, cutXLimit, cx, cy, scale, radAz, radEl, maxDepthPx, isScanning);
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
            double botZ1 = Math.min(surfZ1, refZ1 - (dLayerClamp(gd + 1)) * layerDepthPx);

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

    private static int dLayerClamp(int d) {
        return d;
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
