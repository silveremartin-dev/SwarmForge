/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;
import org.swarmforge.client.util.ObjModelLoader.ObjMesh;

import java.util.List;
import java.util.Random;

/**
 * Renders 3D flora, hollow log stumps, and coordinates simulation overlays
 * (galleries, colony, pheromones, weather HUD, scale bar).
 * Botanical tree rendering is delegated to {@link WorldEditor3DTreeRenderer}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class WorldEditor3DFloraAndOverlayRenderer {

    private WorldEditor3DFloraAndOverlayRenderer() {}

    public static void renderFloraTreesAndSimulationOverlays(
            WorldEditorPane pane,
            double w, double h,
            double cx, double cy,
            double scale, double radAz, double radEl,
            int cutXLimit, double sideM, double step) {

        // Draw Hollow Log Stumps in 3D (editor mode only)
        if (!pane.isSimulationMode && pane.hollowLogsSlider != null && (pane.showVegetationCheck == null || pane.showVegetationCheck.isSelected())) {
            int stumpCount = (int) pane.hollowLogsSlider.getValue();
            Random sRand = new Random(99);
            for (int i = 0; i < stumpCount; i++) {
                int sx = 15 + (int)(sRand.nextDouble() * (WorldEditorPane.GRID_SIZE - 30));
                int sy = 15 + (int)(sRand.nextDouble() * (WorldEditorPane.GRID_SIZE - 30));
                if (sx > cutXLimit) continue;
                double sz = pane.heightGrid[sx][sy] * 40.0;
                if (pane.isPointOccluded(sx, sy, sz, radAz, radEl)) continue;
                double[] sp = pane.project3DPoint(sx, sy, sz, cx, cy, scale, radAz, radEl);
                double stW = Math.max(3.0, 4.5 * (pane.zoom / 7.5));
                double stH = Math.max(2.5, 4.0 * (pane.zoom / 7.5));
                WorldEditor3DTreeRenderer.draw3DVolumetricStump(pane, sp[0], sp[1], stW, stH);
            }
        }

        // Draw Surface Flora Items in 3D
        if (pane.showVegetationCheck == null || pane.showVegetationCheck.isSelected()) {
            WorldEditor3DTreeRenderer.loadObjMeshesIfNeeded();
            for (WorldEditorPane.SurfaceFloraItem item : pane.surfaceFloraItems) {
                if (item.gx > cutXLimit) continue;
                double z = pane.heightGrid[item.gx][item.gy] * 40.0 + 0.6 - (pane.carvedVoxelGrid[item.gx][item.gy] ? 15.0 : 0.0);
                if (pane.isPointOccluded(item.gx, item.gy, z, radAz, radEl)) continue;
                double[] p = pane.project3DPoint(item.gx, item.gy, z, cx, cy, scale, radAz, radEl);
                double sc = item.scale * (pane.zoom / 7.5);

                if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && !item.isCharred) {
                    double gridPerM = (double) WorldEditorPane.GRID_SIZE / Math.max(1.0, sideM);
                    switch (item.type) {
                        case 0 -> { // Bamboo
                            if (WorldEditor3DTreeRenderer.bambooObjMeshes != null && !WorldEditor3DTreeRenderer.bambooObjMeshes.isEmpty()) {
                                ObjMesh bMesh = WorldEditor3DTreeRenderer.bambooObjMeshes.get(0);
                                double bScale = (2.5 * item.scale * gridPerM) / Math.max(0.1, bMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, bMesh, item.gx, item.gy, z, bScale, Color.web("#4ade80"), Color.web("#166534"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 1 -> { // Small Cactus
                            if (WorldEditor3DTreeRenderer.cactusObjMeshes != null && !WorldEditor3DTreeRenderer.cactusObjMeshes.isEmpty()) {
                                ObjMesh cMesh = WorldEditor3DTreeRenderer.cactusObjMeshes.get(0);
                                double cScale = (0.7 * item.scale * gridPerM) / Math.max(0.1, cMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, cMesh, item.gx, item.gy, z, cScale, Color.web("#eab308"), Color.web("#854d0e"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 2 -> { // Flowering Cactus
                            if (WorldEditor3DTreeRenderer.cactusObjMeshes != null && !WorldEditor3DTreeRenderer.cactusObjMeshes.isEmpty()) {
                                ObjMesh cMesh = WorldEditor3DTreeRenderer.cactusObjMeshes.get(0);
                                double cScale = (0.9 * item.scale * gridPerM) / Math.max(0.1, cMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, cMesh, item.gx, item.gy, z, cScale, Color.web("#f43f5e"), Color.web("#854d0e"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 3, 4 -> { // Fern
                            ObjMesh fMesh = WorldEditor3DTreeRenderer.fernMesh != null ? WorldEditor3DTreeRenderer.fernMesh : (WorldEditor3DTreeRenderer.tropicalObjMeshes != null && !WorldEditor3DTreeRenderer.tropicalObjMeshes.isEmpty() ? WorldEditor3DTreeRenderer.tropicalObjMeshes.get(0) : null);
                            if (fMesh != null) {
                                double fScale = (0.65 * item.scale * gridPerM) / Math.max(0.1, fMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, fMesh, item.gx, item.gy, z, fScale, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 5 -> { // Dead wood
                            ObjMesh sMesh = WorldEditor3DTreeRenderer.stumpMesh != null ? WorldEditor3DTreeRenderer.stumpMesh : WorldEditor3DTreeRenderer.deadTreeMesh;
                            if (sMesh != null) {
                                double sScale = (0.8 * item.scale * gridPerM) / Math.max(0.1, sMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, sMesh, item.gx, item.gy, z, sScale, Color.web("#78350f"), Color.web("#451a03"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 7 -> { // Tall grass
                            ObjMesh gMesh = WorldEditor3DTreeRenderer.grassHighMesh != null ? WorldEditor3DTreeRenderer.grassHighMesh : WorldEditor3DTreeRenderer.bushMesh;
                            if (gMesh != null) {
                                double gScale = (0.85 * item.scale * gridPerM) / Math.max(0.1, gMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, gMesh, item.gx, item.gy, z, gScale, Color.web("#65a30d"), Color.web("#365314"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 8 -> { // Low grass
                            ObjMesh gMesh = WorldEditor3DTreeRenderer.grassLowMesh != null ? WorldEditor3DTreeRenderer.grassLowMesh : WorldEditor3DTreeRenderer.bushMesh;
                            if (gMesh != null) {
                                double gScale = (0.45 * item.scale * gridPerM) / Math.max(0.1, gMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, gMesh, item.gx, item.gy, z, gScale, Color.web("#84cc16"), Color.web("#3f6212"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 9 -> { // Flowers / Mushrooms
                            ObjMesh flMesh = WorldEditor3DTreeRenderer.flowerWhiteMesh != null ? WorldEditor3DTreeRenderer.flowerWhiteMesh : WorldEditor3DTreeRenderer.mushroomMesh;
                            if (flMesh != null) {
                                double flScale = (0.35 * item.scale * gridPerM) / Math.max(0.1, flMesh.height);
                                WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, flMesh, item.gx, item.gy, z, flScale, Color.web("#f8fafc"), Color.web("#e11d48"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        default -> {
                            pane.gc3D.setFill(Color.web("#64748b"));
                            pane.gc3D.fillOval(p[0] - 3.5 * sc, p[1] - 3 * sc, 7 * sc, 5.5 * sc);
                            pane.gc3D.setFill(Color.web("#94a3b8"));
                            pane.gc3D.fillOval(p[0] - 2 * sc, p[1] - 4 * sc, 4 * sc, 3.5 * sc);
                        }
                    }
                } else if (pane.currentRenderMode == WorldEditorPane.RenderMode.GAMIFIED) {
                    switch (item.type) {
                        case 0 -> {
                            pane.gc3D.setFill(Color.web("#22c55e"));
                            pane.gc3D.fillRect(p[0] - 2 * sc, p[1] - 8 * sc, 4 * sc, 8 * sc);
                            pane.gc3D.setFill(Color.web("#86efac"));
                            pane.gc3D.fillRect(p[0] - 1 * sc, p[1] - 9 * sc, 2 * sc, 2 * sc);
                        }
                        case 1, 2 -> {
                            pane.gc3D.setFill(Color.web("#15803d"));
                            pane.gc3D.fillRect(p[0] - 1 * sc, p[1] - 10 * sc, 2 * sc, 10 * sc);
                            pane.gc3D.setFill(item.type == 1 ? Color.web("#facc15") : Color.web("#f43f5e"));
                            pane.gc3D.fillRect(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 5 * sc);
                            pane.gc3D.setStroke(Color.web("#0f172a", 0.9)); pane.gc3D.setLineWidth(1.0);
                            pane.gc3D.strokeRect(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 5 * sc);
                        }
                        case 3, 4 -> {
                            pane.gc3D.setFill(Color.web("#166534"));
                            pane.gc3D.fillRect(p[0] - 4 * sc, p[1] - 6 * sc, 8 * sc, 6 * sc);
                            pane.gc3D.setFill(Color.web("#22c55e"));
                            pane.gc3D.fillRect(p[0] - 3 * sc, p[1] - 8 * sc, 6 * sc, 4 * sc);
                            pane.gc3D.setStroke(Color.web("#0f172a", 0.9)); pane.gc3D.setLineWidth(1.2);
                            pane.gc3D.strokeRect(p[0] - 4 * sc, p[1] - 6 * sc, 8 * sc, 6 * sc);
                        }
                        default -> {
                            pane.gc3D.setFill(Color.web("#64748b"));
                            pane.gc3D.fillRect(p[0] - 3 * sc, p[1] - 3 * sc, 6 * sc, 3 * sc);
                            pane.gc3D.setStroke(Color.web("#0f172a", 0.9)); pane.gc3D.setLineWidth(1.0);
                            pane.gc3D.strokeRect(p[0] - 3 * sc, p[1] - 3 * sc, 6 * sc, 3 * sc);
                        }
                    }
                } else if (item.isCharred) {
                    pane.gc3D.setStroke(Color.web("#1e293b"));
                    pane.gc3D.setLineWidth(1.4);
                    pane.gc3D.strokeLine(p[0] - 4 * sc, p[1], p[0] + 4 * sc, p[1] - 2 * sc);
                    pane.gc3D.setFill(Color.web("#334155", 0.8));
                    pane.gc3D.fillOval(p[0] - 3 * sc, p[1] - 2 * sc, 6 * sc, 4 * sc);
                } else {
                    switch (item.type) {
                        case 0 -> {
                            pane.gc3D.setStroke(Color.web("#4ade80"));
                            pane.gc3D.setLineWidth(1.2);
                            pane.gc3D.strokeLine(p[0], p[1], p[0] - 3 * sc, p[1] - 8 * sc);
                            pane.gc3D.strokeLine(p[0], p[1], p[0] + 3 * sc, p[1] - 9 * sc);
                        }
                        case 1 -> {
                            pane.gc3D.setStroke(Color.web("#166534"));
                            pane.gc3D.setLineWidth(1.5);
                            pane.gc3D.strokeLine(p[0], p[1], p[0], p[1] - 12 * sc);
                            pane.gc3D.setFill(Color.web("#a3e635"));
                            pane.gc3D.fillOval(p[0] - 2 * sc, p[1] - 10 * sc, 4 * sc, 4 * sc);
                        }
                        case 2 -> {
                            pane.gc3D.setStroke(Color.web("#15803d"));
                            pane.gc3D.setLineWidth(1.2);
                            pane.gc3D.strokeLine(p[0], p[1], p[0], p[1] - 10 * sc);
                            pane.gc3D.setFill(Color.web("#f43f5e"));
                            pane.gc3D.fillOval(p[0] - 3 * sc, p[1] - 13 * sc, 6 * sc, 6 * sc);
                        }
                        case 3 -> { // Polytrichum Moss Cover
                            pane.gc3D.setFill(Color.web("#15803d", 0.85));
                            pane.gc3D.fillOval(p[0] - 5 * sc, p[1] - 2 * sc, 10 * sc, 4 * sc);
                            pane.gc3D.setFill(Color.web("#22c55e", 0.70));
                            pane.gc3D.fillOval(p[0] - 3 * sc, p[1] - 2.5 * sc, 6 * sc, 3 * sc);
                        }
                        case 4 -> { // Pine Needle Litter
                            pane.gc3D.setFill(Color.web("#854d0e", 0.85));
                            pane.gc3D.fillOval(p[0] - 6 * sc, p[1] - 3 * sc, 12 * sc, 6 * sc);
                            pane.gc3D.setFill(Color.web("#a16207", 0.7));
                            pane.gc3D.fillOval(p[0] - 3 * sc, p[1] - 4 * sc, 8 * sc, 5 * sc);
                            pane.gc3D.setStroke(Color.web("#451a03"));
                            pane.gc3D.setLineWidth(0.8 * sc);
                            pane.gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 2 * sc);
                        }
                        case 5 -> { // Twig & Micro Debris
                            pane.gc3D.setStroke(Color.web("#451a03"));
                            pane.gc3D.setLineWidth(1.4 * sc);
                            pane.gc3D.strokeLine(p[0] - 5 * sc, p[1], p[0] + 5 * sc, p[1] - 3 * sc);
                        }
                        case 6 -> { // Pebble
                            pane.gc3D.setFill(Color.web("#94a3b8"));
                            pane.gc3D.fillOval(p[0] - 2 * sc, p[1] - 2 * sc, 4 * sc, 4 * sc);
                        }
                        case 7 -> { // Leafcutter Foliage / Mushroom
                            pane.gc3D.setStroke(Color.web("#e2e8f0"));
                            pane.gc3D.setLineWidth(1.2 * sc);
                            pane.gc3D.strokeLine(p[0], p[1], p[0], p[1] - 8 * sc);
                            pane.gc3D.setFill(Color.web("#fef08a"));
                            pane.gc3D.fillOval(p[0] - 3 * sc, p[1] - 10 * sc, 6 * sc, 4 * sc);
                            pane.gc3D.setFill(Color.web("#ca8a04"));
                            pane.gc3D.fillOval(p[0] - 2 * sc, p[1] - 11 * sc, 4 * sc, 2 * sc);
                        }
                        case 8 -> { // Fern Understory
                            double fernWindSway = 0.0;
                            if (pane.isSimulationMode) {
                                fernWindSway = Math.sin(System.currentTimeMillis() * 0.003 + item.gx * 0.5) * (4.0 * sc * Math.min(1.5, Math.max(0.1, pane.simWindSpeed / 30.0)));
                            }
                            pane.gc3D.setStroke(Color.web("#15803d"));
                            pane.gc3D.setLineWidth(1.4 * sc);
                            double[] fAngles = {-0.60, -0.30, 0.0, 0.30, 0.60};
                            for (double fAng : fAngles) {
                                double frondLen = 8.5 * sc;
                                double tipX = p[0] + Math.sin(fAng) * frondLen + fernWindSway;
                                double tipY = p[1] - Math.cos(fAng) * frondLen * 0.7;
                                pane.gc3D.strokeLine(p[0], p[1], tipX, tipY);
                                pane.gc3D.setFill(Color.web("#22c55e", 0.90));
                                pane.gc3D.fillOval(tipX - 2.5 * sc, tipY - 2.0 * sc, 5.0 * sc, 4.0 * sc);
                            }
                            pane.gc3D.setFill(Color.web("#14532d"));
                            pane.gc3D.fillOval(p[0] - 2.5 * sc, p[1] - 1.5 * sc, 5.0 * sc, 3.0 * sc);
                        }
                        case 9 -> { // Rock Crevice
                            pane.gc3D.setStroke(Color.web("#0f172a", 0.95));
                            pane.gc3D.setLineWidth(2.0 * sc);
                            pane.gc3D.strokeLine(p[0] - 7 * sc, p[1] + 2 * sc, p[0] - 1 * sc, p[1] - 1 * sc);
                            pane.gc3D.strokeLine(p[0] - 1 * sc, p[1] - 1 * sc, p[0] + 7 * sc, p[1] + 3 * sc);
                            pane.gc3D.setFill(Color.web("#334155"));
                            pane.gc3D.fillOval(p[0] - 3 * sc, p[1] - 1 * sc, 6 * sc, 3 * sc);
                        }
                    }
                }
            }
        }

        // Draw 3D Overlays for Underground Galleries, Nests, and Colony Insects
        if (pane.isSimulationMode && (pane.isGalleriesVisible || !pane.isTerrainVisible)) {
            pane.nestArchitectureRenderer.drawGalleriesOverlay3D(pane, pane.gc3D, cx, cy, scale, radAz, radEl);
        }
        if (pane.isSimulationMode && pane.isColonyVisible) {
            WorldEditor3DOverlayRenderer.drawColonyOverlay3D(pane, cx, cy, scale, radAz, radEl);
        }
        if (pane.isSimulationMode && pane.isPheromonesVisible) {
            WorldEditor3DOverlayRenderer.drawPheromoneOverlay3D(pane, cx, cy, scale, radAz, radEl);
        }

        // Draw Trees & Sub-surface 3D Roots in 3D Viewport
        WorldEditor3DTreeRenderer.renderBotanicalTrees(pane, cx, cy, scale, radAz, radEl, cutXLimit, sideM, step);

        // Additional Overlays (Weather & HUD)
        if (pane.isSimulationMode && pane.isWeatherVisible) {
            WorldEditor3DOverlayRenderer.drawWeatherOverlay3D(pane, w, h);
        }

        if (pane.isGalleriesLegendHUDVisible) {
            pane.nestArchitectureRenderer.drawGalleriesLegendHUD(pane, pane.gc3D, w, h);
        }

        pane.draw3DHoverOverlay(w, h, cx, cy, scale, radAz, radEl);
        pane.drawMetricScaleBar3D(w, h, cx, cy, scale, radAz, radEl);
    }

    public static void loadObjMeshesIfNeeded() {
        WorldEditor3DTreeRenderer.loadObjMeshesIfNeeded();
    }

    public static void drawObjSingleMesh3D(
            WorldEditorPane pane, ObjMesh mesh,
            double wx, double wy, double wz, double scaleObj,
            Color foliageColor, Color barkColor,
            double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DTreeRenderer.drawObjSingleMesh3D(pane, mesh, wx, wy, wz, scaleObj, foliageColor, barkColor, cx, cy, scale, radAz, radEl);
    }

    public static void drawObjMesh3D(
            WorldEditorPane pane, List<ObjMesh> objMeshes,
            double wx, double wy, double wz, double scaleObj,
            Color baseColor, double cx, double cy, double scale, double radAz, double radEl) {
        WorldEditor3DTreeRenderer.drawObjMesh3D(pane, objMeshes, wx, wy, wz, scaleObj, baseColor, cx, cy, scale, radAz, radEl);
    }

    public static void draw3DCylinderTrunk(
            WorldEditorPane pane,
            double px, double py, double trunkW, double trunkH,
            Color mainCol, Color shadowCol, Color highlightCol) {
        double halfW = Math.max(1.2, trunkW / 2.0);
        double topY = py - trunkH;
        double capH = Math.max(1.8, trunkW * 0.35);

        pane.gc3D.setFill(shadowCol.darker());
        pane.gc3D.fillOval(px - halfW * 1.15, py - capH * 0.4, trunkW * 1.15, capH * 0.9);

        javafx.scene.paint.LinearGradient trunkGrad = new javafx.scene.paint.LinearGradient(
            0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0.0, shadowCol),
            new javafx.scene.paint.Stop(0.35, mainCol),
            new javafx.scene.paint.Stop(0.75, highlightCol),
            new javafx.scene.paint.Stop(1.0, shadowCol.darker())
        );
        pane.gc3D.setFill(trunkGrad);
        pane.gc3D.fillRect(px - halfW, topY, trunkW, trunkH);
        pane.gc3D.fillOval(px - halfW, py - capH * 0.5, trunkW, capH);

        pane.gc3D.setFill(mainCol.brighter());
        pane.gc3D.fillOval(px - halfW, topY - capH * 0.5, trunkW, capH);

        pane.gc3D.setStroke(shadowCol.darker());
        pane.gc3D.setLineWidth(Math.max(0.8, trunkW * 0.12));
        pane.gc3D.strokeLine(px - halfW, py, px - halfW, topY);
        pane.gc3D.strokeLine(px + halfW, py, px + halfW, topY);
        pane.gc3D.strokeArc(px - halfW, py - capH * 0.5, trunkW, capH, 180, 180, javafx.scene.shape.ArcType.OPEN);
        pane.gc3D.strokeOval(px - halfW, topY - capH * 0.5, trunkW, capH);
    }

    public static void draw3DVolumetricStump(WorldEditorPane pane, double px, double py, double stumpW, double stumpH) {
        double halfW = Math.max(1.8, stumpW / 2.0);
        double topY = py - stumpH;
        double capH = Math.max(2.2, stumpW * 0.40);

        Color barkBase = Color.web("#78350f");
        Color barkShadow = Color.web("#3b1a03");
        Color barkHighlight = Color.web("#a16207");
        Color woodHeart = Color.web("#d97706");
        Color woodSapwood = Color.web("#fef08a");
        Color woodHollow = Color.web("#1c1917");

        pane.gc3D.setFill(barkShadow);
        pane.gc3D.fillOval(px - halfW * 1.35, py - capH * 0.5, stumpW * 1.35, capH * 1.1);

        javafx.scene.paint.LinearGradient trunkGrad = new javafx.scene.paint.LinearGradient(
            0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0.0, barkShadow),
            new javafx.scene.paint.Stop(0.35, barkBase),
            new javafx.scene.paint.Stop(0.75, barkHighlight),
            new javafx.scene.paint.Stop(1.0, barkShadow.darker())
        );
        pane.gc3D.setFill(trunkGrad);
        pane.gc3D.fillRect(px - halfW, topY, stumpW, stumpH);
        pane.gc3D.fillOval(px - halfW, py - capH * 0.5, stumpW, capH);

        pane.gc3D.setFill(woodSapwood);
        pane.gc3D.fillOval(px - halfW, topY - capH * 0.5, stumpW, capH);

        pane.gc3D.setFill(woodHeart);
        pane.gc3D.fillOval(px - halfW * 0.8, topY - capH * 0.4, stumpW * 0.8, capH * 0.8);
        pane.gc3D.setStroke(Color.web("#92400e"));
        pane.gc3D.setLineWidth(Math.max(0.6, stumpW * 0.05));
        pane.gc3D.strokeOval(px - halfW * 0.6, topY - capH * 0.3, stumpW * 0.6, capH * 0.6);
        pane.gc3D.strokeOval(px - halfW * 0.4, topY - capH * 0.2, stumpW * 0.4, capH * 0.4);

        pane.gc3D.setFill(woodHollow);
        pane.gc3D.fillOval(px - halfW * 0.3, topY - capH * 0.15, stumpW * 0.3, capH * 0.3);

        pane.gc3D.setStroke(barkShadow.darker());
        pane.gc3D.setLineWidth(Math.max(0.8, stumpW * 0.10));
        pane.gc3D.strokeLine(px - halfW, py, px - halfW, topY);
        pane.gc3D.strokeLine(px + halfW, py, px + halfW, topY);
        pane.gc3D.strokeArc(px - halfW, py - capH * 0.5, stumpW, capH, 180, 180, javafx.scene.shape.ArcType.OPEN);
        pane.gc3D.strokeOval(px - halfW, topY - capH * 0.5, stumpW, capH);

        pane.gc3D.setFill(Color.web("#f59e0b"));
        pane.gc3D.fillOval(px + halfW * 0.6, py - stumpH * 0.65, halfW * 0.7, capH * 0.4);
        pane.gc3D.setFill(Color.web("#d97706"));
        pane.gc3D.fillOval(px + halfW * 0.5, py - stumpH * 0.35, halfW * 0.6, capH * 0.35);
    }
}
