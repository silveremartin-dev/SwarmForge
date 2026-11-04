/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;

import java.util.Random;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;

/**
 * 2.5D Isometric block and voxel drawing helper for {@link WorldEditorPane}.
 */
final class WorldEditorVoxelDrawingHelper {

    private WorldEditorVoxelDrawingHelper() {}

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
}
