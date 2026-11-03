/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.nest;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.ui.NestRenderer;

import java.util.function.Function;

/**
 * Renders 3D Ghost Mesh contours and structural cues for all 13 biological nest architectures
 * (beehive frames, cathedral spires, paper envelopes, bivouac chains, bamboo cylinders, fungi vaults).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class NestGhostMeshRenderer {

    private NestGhostMeshRenderer() {}

    /**
     * Draws 3D Ghost Mesh contours for all 13 biological architectures.
     */
    public static void drawGhostMesh(
            String arch,
            NestGeneratorPane.GeneratedNest nest,
            Function<double[], double[]> proj,
            GraphicsContext gc,
            double zoom
    ) {
        String archKey = NestRenderer.normalizeArchKey(arch);

        // 1. Surface Ground Grid Mesh (z = 0)
        gc.setStroke(Color.rgb(56, 189, 248, 0.30));
        gc.setLineWidth(1.0);
        for (double g = -25; g <= 25; g += 5) {
            double[] a = proj.apply(new double[]{g, -25, 0});
            double[] b = proj.apply(new double[]{g, 25, 0});
            double[] c2 = proj.apply(new double[]{-25, g, 0});
            double[] d = proj.apply(new double[]{25, g, 0});
            gc.strokeLine(a[0], a[1], b[0], b[1]);
            gc.strokeLine(c2[0], c2[1], d[0], d[1]);
        }

        double maxD = nest != null ? nest.maxDepth : 30.0;
        double scale = nest != null ? nest.workerSizeMm / 5.0 : 1.0;
        double rx0 = nest != null ? nest.getRootX() : 0.0;
        double ry0 = nest != null ? nest.getRootY() : 0.0;

        switch (archKey) {
            case "WOODEN_BEEHIVE" -> drawWoodenBeehiveGhost(gc, proj, zoom, rx0, ry0, scale);
            case "WAX_COMB_HEXAGONAL" -> drawWaxCombGhost(gc, proj, zoom, rx0, ry0, scale);
            case "WAX_POTS_CLUSTER" -> drawWaxPotsGhost(gc, proj, zoom, rx0, ry0, scale);
            case "PAPER_PEDUNCULATE" -> drawPaperPedunculateGhost(gc, proj, zoom, rx0, ry0, scale, maxD);
            case "CATHEDRAL_MOUND" -> drawCathedralMoundGhost(gc, proj, rx0, ry0, zoom, maxD);
            case "ARBOREAL_SILK_LEAF" -> drawArborealSilkLeafGhost(gc, proj, zoom, rx0, ry0, scale);
            case "SUBTERRANEAN_FUNGI_VAULT" -> drawFungiVaultGhost(gc, proj, zoom, rx0, ry0, scale, maxD);
            case "CARTON_NEST" -> drawCartonNestGhost(gc, proj, zoom, rx0, ry0, scale);
            case "BAMBOO_STEM_NEST" -> drawBambooStemGhost(gc, proj, zoom, rx0, ry0, scale);
            case "BIVOUAC_LIVING_NEST" -> drawBivouacLivingGhost(gc, proj, zoom, rx0, ry0, scale);
            case "SURFACE_MOUND" -> drawSurfaceMoundGhost(gc, proj, zoom, rx0, ry0, scale, maxD);
            case "HOLLOW_TRUNK_NEST" -> drawHollowTrunkGhost(gc, proj, zoom, rx0, ry0, scale);
            default -> drawClassicSubterraneanGhost(gc, proj, rx0, ry0, maxD);
        }
    }

    private static void drawWoodenBeehiveGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        gc.setStroke(Color.web("#d97706", 0.75));
        gc.setLineWidth(2.0 * zoom);
        double boxW = 9.0 * scale, boxD = 9.0 * scale, boxH = 15.0 * scale;
        double zMin = -2.0 * scale, zMax = zMin + boxH;
        double[][] corners = {{rx0 - boxW, ry0 - boxD}, {rx0 + boxW, ry0 - boxD}, {rx0 + boxW, ry0 + boxD}, {rx0 - boxW, ry0 + boxD}};
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            double[] b1 = proj.apply(new double[]{corners[i][0], corners[i][1], zMin});
            double[] b2 = proj.apply(new double[]{corners[next][0], corners[next][1], zMin});
            double[] t1 = proj.apply(new double[]{corners[i][0], corners[i][1], zMax});
            double[] t2 = proj.apply(new double[]{corners[next][0], corners[next][1], zMax});

            gc.strokeLine(b1[0], b1[1], b2[0], b2[1]);
            gc.strokeLine(t1[0], t1[1], t2[0], t2[1]);
            gc.strokeLine(b1[0], b1[1], t1[0], t1[1]);
        }
        for (double offsetY = -6.0 * scale; offsetY <= 6.0 * scale; offsetY += 3.0 * scale) {
            double cW = 7.5 * scale, cH = 11.0 * scale;
            double[] topA = proj.apply(new double[]{rx0 - cW, ry0 + offsetY, zMin + 1.5 * scale});
            double[] topB = proj.apply(new double[]{rx0 + cW, ry0 + offsetY, zMin + 1.5 * scale});
            double[] botA = proj.apply(new double[]{rx0 - cW, ry0 + offsetY, zMin + 1.5 * scale + cH});
            double[] botB = proj.apply(new double[]{rx0 + cW, ry0 + offsetY, zMin + 1.5 * scale + cH});

            gc.setStroke(Color.web("#fbbf24", 0.65));
            gc.setLineWidth(1.4 * zoom);
            gc.strokeLine(topA[0], topA[1], topB[0], topB[1]);
            gc.strokeLine(botA[0], botA[1], botB[0], botB[1]);
            gc.strokeLine(topA[0], topA[1], botA[0], botA[1]);
            gc.strokeLine(topB[0], topB[1], botB[0], botB[1]);
        }
        double[] ent1 = proj.apply(new double[]{rx0 - 4 * scale, ry0 + boxD, zMax - 1.5 * scale});
        double[] ent2 = proj.apply(new double[]{rx0 + 4 * scale, ry0 + boxD, zMax - 1.5 * scale});
        gc.setStroke(Color.web("#ef4444", 0.95));
        gc.setLineWidth(3.5 * zoom);
        gc.strokeLine(ent1[0], ent1[1], ent2[0], ent2[1]);
    }

    private static void drawWaxCombGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        double cW = 11.0 * scale, cH = 14.0 * scale;
        int combCount = 7;
        double spacing = 2.4 * scale;
        double startY = ry0 - ((combCount - 1) * spacing) / 2.0;

        for (int i = 0; i < combCount; i++) {
            double yPos = startY + i * spacing;
            double[] topA = proj.apply(new double[]{rx0 - cW, yPos, 0});
            double[] topB = proj.apply(new double[]{rx0 + cW, yPos, 0});
            double[] botA = proj.apply(new double[]{rx0 - cW, yPos, cH});
            double[] botB = proj.apply(new double[]{rx0 + cW, yPos, cH});

            gc.setStroke(Color.web("#f59e0b", 0.75));
            gc.setLineWidth(1.8 * zoom);
            gc.strokeLine(topA[0], topA[1], topB[0], topB[1]);
            gc.strokeLine(botA[0], botA[1], botB[0], botB[1]);
            gc.strokeLine(topA[0], topA[1], botA[0], botA[1]);
            gc.strokeLine(topB[0], topB[1], botB[0], botB[1]);

            double[] hTopA = proj.apply(new double[]{rx0 - cW, yPos, cH * 0.25});
            double[] hTopB = proj.apply(new double[]{rx0 + cW, yPos, cH * 0.25});
            gc.setStroke(Color.web("#fef08a", 0.60));
            gc.setLineWidth(1.2 * zoom);
            gc.strokeLine(hTopA[0], hTopA[1], hTopB[0], hTopB[1]);
        }
    }

    private static void drawWaxPotsGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        double[] cp = proj.apply(new double[]{rx0, ry0, 4.0 * scale});
        double rx = 12.0 * scale * zoom * 0.85, ry = 12.0 * scale * zoom * 0.42;

        gc.setFill(Color.web("#65a30d", 0.22));
        gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
        gc.setStroke(Color.web("#84cc16", 0.65));
        gc.setLineWidth(1.5 * zoom);
        gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
    }

    private static void drawPaperPedunculateGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale, double maxD) {
        gc.setStroke(Color.web("#92400e", 0.75));
        gc.setLineWidth(3.5 * zoom);
        double[] brA = proj.apply(new double[]{rx0 - 16, ry0, -5 * scale}), brB = proj.apply(new double[]{rx0 + 16, ry0, -5 * scale});
        gc.strokeLine(brA[0], brA[1], brB[0], brB[1]);

        gc.setStroke(Color.web("#78350f", 0.90));
        gc.setLineWidth(2.0 * zoom);
        double[] stemTop = proj.apply(new double[]{rx0, ry0, -5 * scale}), stemBot = proj.apply(new double[]{rx0, ry0, 0});
        gc.strokeLine(stemTop[0], stemTop[1], stemBot[0], stemBot[1]);

        double nestH = Math.max(10.0, maxD);
        for (double zLevel = 0; zLevel <= nestH; zLevel += 3.0 * scale) {
            double norm = zLevel / nestH;
            double rad = (Math.sin(norm * Math.PI) * 10.0 + 2.5) * scale;
            double[] cp = proj.apply(new double[]{rx0, ry0, zLevel});
            double rx = rad * zoom * 0.85, ry = rad * zoom * 0.42;

            gc.setFill(Color.web("#e2e8f0", 0.22 + (1 - norm) * 0.20));
            gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            gc.setStroke(Color.web("#cbd5e1", 0.65));
            gc.setLineWidth(1.2 * zoom);
            gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
        }
    }

    private static void drawCathedralMoundGhost(GraphicsContext gc, Function<double[], double[]> proj, double rx0, double ry0, double zoom, double maxD) {
        double moundH = 18.0;
        int baseRad = 14;
        for (double hZ = 0; hZ <= moundH; hZ += 1.5) {
            double radius = baseRad * (1.0 - Math.pow(hZ / moundH, 0.75));
            double[] centerP = proj.apply(new double[]{rx0, ry0, -hZ});
            double rx = radius * zoom * 0.85, ry = radius * zoom * 0.42;

            gc.setFill(Color.web("#854d0e", 0.30 + (hZ / moundH) * 0.35));
            gc.fillOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
            gc.setStroke(Color.web("#a16207", 0.75));
            gc.setLineWidth(1.2);
            gc.strokeOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
        }
        double[] angles = {0, Math.PI / 3, 2 * Math.PI / 3, Math.PI, 4 * Math.PI / 3, 5 * Math.PI / 3};
        for (double ang : angles) {
            double[] botP = proj.apply(new double[]{rx0 + baseRad * Math.cos(ang), ry0 + baseRad * Math.sin(ang), 0});
            double[] topP = proj.apply(new double[]{rx0 + 3 * Math.cos(ang), ry0 + 3 * Math.sin(ang), -moundH});
            gc.setStroke(Color.web("#ca8a04", 0.85));
            gc.setLineWidth(2.0);
            gc.strokeLine(botP[0], botP[1], topP[0], topP[1]);
        }
        gc.setStroke(Color.web("#713f12", 0.35));
        gc.setLineWidth(1.0);
        for (double g = -14; g <= 14; g += 7) {
            double[] a = proj.apply(new double[]{rx0 + g, ry0 - 14, maxD}), b = proj.apply(new double[]{rx0 + g, ry0 + 14, maxD});
            gc.strokeLine(a[0], a[1], b[0], b[1]);
        }
    }

    private static void drawArborealSilkLeafGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        gc.setStroke(Color.web("#92400e", 0.75));
        gc.setLineWidth(3.0 * zoom);
        double[] b1 = proj.apply(new double[]{rx0 - 16, ry0, -8 * scale}), b2 = proj.apply(new double[]{rx0 + 16, ry0, -8 * scale});
        gc.strokeLine(b1[0], b1[1], b2[0], b2[1]);

        double[] centerP = proj.apply(new double[]{rx0, ry0, 3 * scale});
        double rx = 13.0 * scale * zoom * 0.85, ry = 13.0 * scale * zoom * 0.42;

        gc.setFill(Color.web("#15803d", 0.22));
        gc.fillOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
        gc.setStroke(Color.web("#22c55e", 0.65));
        gc.setLineWidth(1.5 * zoom);
        gc.strokeOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);

        gc.setStroke(Color.web("#f8fafc", 0.50));
        gc.setLineWidth(1.0 * zoom);
        for (int i = 0; i < 6; i++) {
            double a1 = i * Math.PI / 3;
            double[] s1 = proj.apply(new double[]{rx0 + 12 * scale * Math.cos(a1), ry0 + 12 * scale * Math.sin(a1), (i % 2 == 0 ? -2 : 8) * scale});
            double[] s2 = proj.apply(new double[]{rx0 - 12 * scale * Math.cos(a1), ry0 - 12 * scale * Math.sin(a1), (i % 2 == 0 ? 8 : -2) * scale});
            gc.strokeLine(s1[0], s1[1], s2[0], s2[1]);
        }
    }

    private static void drawFungiVaultGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale, double maxD) {
        for (int i = 0; i < 3; i++) {
            double ang = i * (Math.PI * 2 / 3.0);
            double[] crP = proj.apply(new double[]{rx0 + (5.0 + i * 2.0) * Math.cos(ang) * scale, ry0 + (5.0 + i * 2.0) * Math.sin(ang) * scale, 0});
            gc.setFill(Color.web("#854d0e", 0.40));
            gc.fillOval(crP[0] - 6 * zoom, crP[1] - 3 * zoom, 12 * zoom, 6 * zoom);
        }

        double zVault = maxD * 0.45;
        double[] vaultP = proj.apply(new double[]{rx0, ry0, zVault});
        double rx = 14.0 * scale * zoom * 0.85, ry = 14.0 * scale * zoom * 0.42;
        gc.setFill(Color.web("#a855f7", 0.25));
        gc.fillOval(vaultP[0] - rx, vaultP[1] - ry, rx * 2, ry * 2);
        gc.setStroke(Color.web("#c084fc", 0.65));
        gc.setLineWidth(1.4 * zoom);
        gc.strokeOval(vaultP[0] - rx, vaultP[1] - ry, rx * 2, ry * 2);

        double zWaste = maxD * 0.85;
        double[] wasteP = proj.apply(new double[]{rx0, ry0, zWaste});
        double rxW = 10.0 * scale * zoom * 0.85, ryW = 10.0 * scale * zoom * 0.42;
        gc.setFill(Color.web("#ef4444", 0.25));
        gc.fillOval(wasteP[0] - rxW, wasteP[1] - ryW, rxW * 2, ryW * 2);
        gc.setStroke(Color.web("#f87171", 0.65));
        gc.setLineWidth(1.4 * zoom);
        gc.strokeOval(wasteP[0] - rxW, wasteP[1] - ryW, rxW * 2, ryW * 2);
    }

    private static void drawCartonNestGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        gc.setStroke(Color.web("#78350f", 0.70));
        gc.setLineWidth(4.0 * zoom);
        double[] trunkB = proj.apply(new double[]{rx0, ry0 - 10 * scale, 16 * scale}), trunkT = proj.apply(new double[]{rx0, ry0 - 10 * scale, -14 * scale});
        gc.strokeLine(trunkB[0], trunkB[1], trunkT[0], trunkT[1]);

        double[] cp = proj.apply(new double[]{rx0, ry0, 2 * scale});
        double rx = 12.0 * scale * zoom * 0.85, ry = 12.0 * scale * zoom * 0.42;

        gc.setFill(Color.web("#a16207", 0.28));
        gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
        gc.setStroke(Color.web("#78350f", 0.75));
        gc.setLineWidth(1.6 * zoom);
        gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
    }

    private static void drawBambooStemGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        double stemMinX = rx0 - 15.0 * scale, stemMaxX = rx0 + 18.0 * scale;
        double rStem = 3.6 * scale;

        gc.setStroke(Color.web("#84cc16", 0.60));
        gc.setLineWidth(1.6 * zoom);
        for (double x = stemMinX; x <= stemMaxX; x += 6 * scale) {
            double[] topP = proj.apply(new double[]{x, ry0, -rStem});
            double[] botP = proj.apply(new double[]{x, ry0, rStem});
            gc.strokeLine(topP[0], topP[1], botP[0], botP[1]);
        }

        double[] leftTop = proj.apply(new double[]{stemMinX, ry0, -rStem});
        double[] rightTop = proj.apply(new double[]{stemMaxX, ry0, -rStem});
        double[] leftBot = proj.apply(new double[]{stemMinX, ry0, rStem});
        double[] rightBot = proj.apply(new double[]{stemMaxX, ry0, rStem});

        gc.strokeLine(leftTop[0], leftTop[1], rightTop[0], rightTop[1]);
        gc.strokeLine(leftBot[0], leftBot[1], rightBot[0], rightBot[1]);

        double[] plug = proj.apply(new double[]{rx0 - 12 * scale, ry0, 0});
        gc.setFill(Color.web("#ef4444", 0.85));
        gc.fillOval(plug[0] - 3 * zoom, plug[1] - 3 * zoom, 6 * zoom, 6 * zoom);
    }

    private static void drawBivouacLivingGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        gc.setStroke(Color.web("#451a03", 0.90));
        gc.setLineWidth(5.0 * zoom);
        double[] log1 = proj.apply(new double[]{rx0 - 18, ry0, -8 * scale}), log2 = proj.apply(new double[]{rx0 + 18, ry0, -8 * scale});
        gc.strokeLine(log1[0], log1[1], log2[0], log2[1]);
        gc.setStroke(Color.web("#78350f", 0.70));
        gc.setLineWidth(2.5 * zoom);
        gc.strokeLine(log1[0], log1[1] - 2 * zoom, log2[0], log2[1] - 2 * zoom);

        double bivouacH = 14.0 * scale;
        for (double zLevel = -8 * scale; zLevel <= bivouacH; zLevel += 2.0 * scale) {
            double norm = (zLevel - (-8 * scale)) / (bivouacH - (-8 * scale));
            double rad = (Math.sin(norm * Math.PI * 0.85 + 0.15) * 11.0 + 3.0) * scale;
            double[] cp = proj.apply(new double[]{rx0, ry0, zLevel});
            double rx = rad * zoom * 0.85, ry = rad * zoom * 0.42;

            gc.setFill(Color.web("#421c0e", 0.28 + (1 - norm) * 0.20));
            gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            gc.setStroke(Color.web("#7f1d1d", 0.75));
            gc.setLineWidth(1.3 * zoom);
            gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
        }

        gc.setStroke(Color.web("#991b1b", 0.85));
        gc.setLineWidth(1.8 * zoom);
        for (double offsetStrand = -10.0 * scale; offsetStrand <= 10.0 * scale; offsetStrand += 3.5 * scale) {
            double strandLen = (12.0 - Math.abs(offsetStrand) * 0.45) * scale;
            double[] topP = proj.apply(new double[]{rx0 + offsetStrand, ry0, -7 * scale});
            double[] botP = proj.apply(new double[]{rx0 + offsetStrand * 0.7, ry0, strandLen});
            gc.strokeLine(topP[0], topP[1], botP[0], botP[1]);
            gc.setFill(Color.web("#421c0e"));
            gc.fillOval(botP[0] - 2 * zoom, botP[1] - 1.5 * zoom, 4 * zoom, 3 * zoom);
        }
    }

    private static void drawSurfaceMoundGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale, double maxD) {
        double moundH = 8.0 * scale;
        for (double hZ = 0; hZ <= moundH; hZ += 1.5 * scale) {
            double radius = 14.0 * scale * (1.0 - Math.pow(hZ / moundH, 0.65));
            double[] cp = proj.apply(new double[]{rx0, ry0, -hZ});
            double rx = radius * zoom * 0.85, ry = radius * zoom * 0.42;

            gc.setFill(Color.web("#ca8a04", 0.25 + (hZ / moundH) * 0.25));
            gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            gc.setStroke(Color.web("#eab308", 0.65));
            gc.setLineWidth(1.2 * zoom);
            gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
        }
        gc.setStroke(Color.web("#78350f", 0.35));
        gc.setLineWidth(1.0);
        for (double g = -14; g <= 14; g += 7) {
            double[] a = proj.apply(new double[]{rx0 + g, ry0 - 14, maxD}), b = proj.apply(new double[]{rx0 + g, ry0 + 14, maxD});
            gc.strokeLine(a[0], a[1], b[0], b[1]);
        }
    }

    private static void drawHollowTrunkGhost(GraphicsContext gc, Function<double[], double[]> proj, double zoom, double rx0, double ry0, double scale) {
        gc.setStroke(Color.web("#78350f", 0.65));
        gc.setLineWidth(1.8 * zoom);
        for (double z = -14 * scale; z <= 14 * scale; z += 4 * scale) {
            double[] cp = proj.apply(new double[]{rx0, ry0, z});
            double rx = 8.5 * scale * zoom * 0.85, ry = 8.5 * scale * zoom * 0.42;
            gc.setFill(Color.web("#78350f", 0.22));
            gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
        }
        double[] knot = proj.apply(new double[]{rx0, ry0 - 5.5 * scale, -4.0 * scale});
        gc.setFill(Color.web("#22c55e", 0.85));
        gc.fillOval(knot[0] - 4 * zoom, knot[1] - 3 * zoom, 8 * zoom, 6 * zoom);
    }

    private static void drawClassicSubterraneanGhost(GraphicsContext gc, Function<double[], double[]> proj, double rx0, double ry0, double maxD) {
        gc.setStroke(Color.rgb(148, 163, 184, 0.25));
        gc.setLineWidth(0.8);
        double bw = 22, bd = 22;
        double[][] botCorners = {
            {rx0 - bw, ry0 - bd, maxD}, {rx0 + bw, ry0 - bd, maxD}, {rx0 + bw, ry0 + bd, maxD}, {rx0 - bw, ry0 + bd, maxD}
        };
        double[][] topCorners = {
            {rx0 - bw, ry0 - bd, 0}, {rx0 + bw, ry0 - bd, 0}, {rx0 + bw, ry0 + bd, 0}, {rx0 - bw, ry0 + bd, 0}
        };
        for (int i = 0; i < 4; i++) {
            double[] p1 = proj.apply(botCorners[i]), p2 = proj.apply(botCorners[(i + 1) % 4]);
            gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);

            double[] pT = proj.apply(topCorners[i]), pB = proj.apply(botCorners[i]);
            gc.strokeLine(pT[0], pT[1], pB[0], pB[1]);
        }
    }
}
