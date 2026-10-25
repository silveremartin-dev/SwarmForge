package org.swarmforge.client.ui;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.swarmforge.client.util.ObjModelLoader;
import org.swarmforge.client.util.ObjModelLoader.ObjMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders 3D flora, botanical trees, Wavefront OBJ models, and coordinates simulation overlays.
 */
public final class WorldEditor3DFloraAndOverlayRenderer {

    private static List<ObjMesh> forestObjMeshes;
    private static List<ObjMesh> bambooObjMeshes;
    private static List<ObjMesh> cactusObjMeshes;
    private static List<ObjMesh> tropicalObjMeshes;

    private static ObjMesh oakMesh;
    private static ObjMesh birchMesh;
    private static ObjMesh pineMesh;
    private static ObjMesh deadTreeMesh;
    private static ObjMesh stumpMesh;
    private static ObjMesh bushMesh;
    private static ObjMesh grassHighMesh;
    private static ObjMesh grassLowMesh;
    private static ObjMesh flowerWhiteMesh;
    private static ObjMesh flowerRedMesh;
    private static ObjMesh mushroomMesh;
    private static ObjMesh fernMesh;

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
                draw3DVolumetricStump(pane, sp[0], sp[1], stW, stH);
            }
        }

        // Draw Surface Flora Items in 3D
        if (pane.showVegetationCheck == null || pane.showVegetationCheck.isSelected()) {
            loadObjMeshesIfNeeded();
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
                            if (bambooObjMeshes != null && !bambooObjMeshes.isEmpty()) {
                                ObjMesh bMesh = bambooObjMeshes.get(0);
                                double bScale = (2.5 * item.scale * gridPerM) / Math.max(0.1, bMesh.height);
                                drawObjSingleMesh3D(pane, bMesh, item.gx, item.gy, z, bScale, Color.web("#4ade80"), Color.web("#166534"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 1 -> { // Small Cactus
                            if (cactusObjMeshes != null && !cactusObjMeshes.isEmpty()) {
                                ObjMesh cMesh = cactusObjMeshes.get(0);
                                double cScale = (0.7 * item.scale * gridPerM) / Math.max(0.1, cMesh.height);
                                drawObjSingleMesh3D(pane, cMesh, item.gx, item.gy, z, cScale, Color.web("#eab308"), Color.web("#854d0e"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 2 -> { // Flowering Cactus
                            if (cactusObjMeshes != null && !cactusObjMeshes.isEmpty()) {
                                ObjMesh cMesh = cactusObjMeshes.get(0);
                                double cScale = (0.9 * item.scale * gridPerM) / Math.max(0.1, cMesh.height);
                                drawObjSingleMesh3D(pane, cMesh, item.gx, item.gy, z, cScale, Color.web("#f43f5e"), Color.web("#854d0e"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 3, 4 -> { // Fern
                            ObjMesh fMesh = fernMesh != null ? fernMesh : (tropicalObjMeshes != null && !tropicalObjMeshes.isEmpty() ? tropicalObjMeshes.get(0) : null);
                            if (fMesh != null) {
                                double fScale = (0.65 * item.scale * gridPerM) / Math.max(0.1, fMesh.height);
                                drawObjSingleMesh3D(pane, fMesh, item.gx, item.gy, z, fScale, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 5 -> { // Dead wood
                            ObjMesh sMesh = stumpMesh != null ? stumpMesh : deadTreeMesh;
                            if (sMesh != null) {
                                double sScale = (0.8 * item.scale * gridPerM) / Math.max(0.1, sMesh.height);
                                drawObjSingleMesh3D(pane, sMesh, item.gx, item.gy, z, sScale, Color.web("#78350f"), Color.web("#451a03"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 7 -> { // Tall grass
                            ObjMesh gMesh = grassHighMesh != null ? grassHighMesh : bushMesh;
                            if (gMesh != null) {
                                double gScale = (0.85 * item.scale * gridPerM) / Math.max(0.1, gMesh.height);
                                drawObjSingleMesh3D(pane, gMesh, item.gx, item.gy, z, gScale, Color.web("#65a30d"), Color.web("#365314"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 8 -> { // Low grass
                            ObjMesh gMesh = grassLowMesh != null ? grassLowMesh : bushMesh;
                            if (gMesh != null) {
                                double gScale = (0.45 * item.scale * gridPerM) / Math.max(0.1, gMesh.height);
                                drawObjSingleMesh3D(pane, gMesh, item.gx, item.gy, z, gScale, Color.web("#84cc16"), Color.web("#3f6212"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 9 -> { // Flowers / Mushrooms
                            ObjMesh flMesh = flowerWhiteMesh != null ? flowerWhiteMesh : mushroomMesh;
                            if (flMesh != null) {
                                double flScale = (0.35 * item.scale * gridPerM) / Math.max(0.1, flMesh.height);
                                drawObjSingleMesh3D(pane, flMesh, item.gx, item.gy, z, flScale, Color.web("#f8fafc"), Color.web("#e11d48"), cx, cy, scale, radAz, radEl);
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
        if (pane.treeCountSlider != null && (pane.showVegetationCheck == null || pane.showVegetationCheck.isSelected())) {
            List<WorldEditorPane.BotanicalTreeData> treeDataList = pane.getBotanicalTreeInstances();

            sideM = pane.surfaceSizeSlider != null ? pane.surfaceSizeSlider.getValue() : 25.0;
            double pixelsPerMeter = Math.max(1.5, (WorldEditorPane.GRID_SIZE * scale * (pane.zoom / 7.5)) / Math.max(1.0, sideM));

            class TreeInstance {
                double[] p;
                double depth;
                int gx, gy;
                int speciesIdx;
                double ageScale;
            }
            List<TreeInstance> trees = new ArrayList<>();

            for (WorldEditorPane.BotanicalTreeData btd : treeDataList) {
                int gx = btd.gx;
                int gy = btd.gy;
                if (gx > cutXLimit) continue;

                double z = pane.heightGrid[gx][gy] * 40.0 - (pane.carvedVoxelGrid[gx][gy] ? 15.0 : 0.0);
                double[] p = pane.project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);

                double depth = (gx - WorldEditorPane.GRID_SIZE / 2.0) * Math.sin(radAz) + (gy - WorldEditorPane.GRID_SIZE / 2.0) * Math.cos(radAz) + (z / 40.0) * Math.sin(radEl);
                TreeInstance ti = new TreeInstance();
                ti.p = p;
                ti.depth = depth;
                ti.gx = gx; ti.gy = gy;
                ti.ageScale = btd.ageScale;
                ti.speciesIdx = btd.speciesIdx;
                trees.add(ti);
            }

            trees.sort((t1, t2) -> Double.compare(t1.p[1], t2.p[1]));

            for (TreeInstance ti : trees) {
                double[] p = ti.p;
                int speciesIdx = ti.speciesIdx;
                double z = pane.heightGrid[ti.gx][ti.gy] * 40.0 - (pane.carvedVoxelGrid[ti.gx][ti.gy] ? 15.0 : 0.0);

                if (pane.showRootsCheck != null && pane.showRootsCheck.isSelected()) {
                    pane.gc3D.setStroke(Color.web("#78350f", 0.85));
                    double rootRadiusPx = Math.max(6.0, 12.0 * ti.ageScale * (pane.zoom / 7.5));
                    pane.gc3D.setLineWidth(Math.max(1.2, 2.5 * (pane.zoom / 7.5)));
                    for (int rAng = 0; rAng < 5; rAng++) {
                        double ang = rAng * (Math.PI * 2.0 / 5.0) + (ti.gx * 0.3);
                        double rx = p[0] + Math.cos(ang) * rootRadiusPx;
                        double ry = p[1] + Math.sin(ang) * (rootRadiusPx * 0.45) + 4.0;
                        pane.gc3D.strokeLine(p[0], p[1], rx, ry);
                        pane.gc3D.strokeLine(rx, ry, rx + Math.cos(ang + 0.4) * (rootRadiusPx * 0.5), ry + 3.0);
                    }
                }

                double baseTreeHeightM = switch (speciesIdx) {
                    case 0 -> 5.5;   // Quercus
                    case 1 -> 4.5;   // Pinus
                    case 2 -> 4.0;   // Acacia
                    case 3 -> 2.5;   // Cactus
                    case 4 -> 4.5;   // Bouleau
                    case 5 -> 2.5;   // Bambou
                    case 6 -> 0.8;   // Souche
                    default -> 4.5;
                };

                double canopyRadiusM = switch (speciesIdx) {
                    case 0 -> 2.2;
                    case 1 -> 1.5;
                    case 2 -> 2.5;
                    case 3 -> 0.6;
                    case 4 -> 1.4;
                    case 5 -> 0.8;
                    case 6 -> 0.5;
                    default -> 1.8;
                } * ti.ageScale;

                double treeHeightM = (speciesIdx == 6) ? 0.65 * ti.ageScale : Math.min(baseTreeHeightM * ti.ageScale, Math.max(1.8, sideM * 0.35));

                double trunkRatio = switch (speciesIdx) {
                    case 0 -> 0.35;
                    case 1 -> 0.30;
                    case 2 -> 0.45;
                    case 3 -> 0.85;
                    case 4 -> 0.35;
                    case 5 -> 0.60;
                    case 6 -> 0.75;
                    default -> 0.35;
                };
                double trunkH = (speciesIdx == 6)
                        ? Math.max(6.0, 0.55 * ti.ageScale * pixelsPerMeter)
                        : (treeHeightM * pixelsPerMeter * trunkRatio);

                double trunkW = (speciesIdx == 6)
                        ? Math.max(8.0, (1.00 * ti.ageScale) * pixelsPerMeter)
                        : Math.max(2.5, (treeHeightM * 0.038) * pixelsPerMeter);

                double canopyR = Math.max(5.0, canopyRadiusM * pixelsPerMeter);

                double windSwayX = 0.0;
                double windSwayY = 0.0;
                if (pane.isSimulationMode) {
                    double windFactor = Math.min(1.5, Math.max(0.1, pane.simWindSpeed / 30.0));
                    double windAngleRad = switch (pane.simWindDirection.toUpperCase()) {
                        case "N" -> -Math.PI / 2;
                        case "NE" -> -Math.PI / 4;
                        case "E" -> 0.0;
                        case "SE" -> Math.PI / 4;
                        case "S" -> Math.PI / 2;
                        case "SW" -> 3.0 * Math.PI / 4.0;
                        case "W" -> Math.PI;
                        case "NW" -> -3.0 * Math.PI / 4.0;
                        default -> 3.0 * Math.PI / 4.0;
                    };
                    double swayAmp = Math.sin(System.currentTimeMillis() * 0.0022 + p[0] * 0.02) * (canopyR * 0.12 * windFactor);
                    windSwayX = Math.cos(windAngleRad) * swayAmp;
                    windSwayY = Math.sin(windAngleRad) * swayAmp * 0.4;
                }
                double swayX = p[0] + windSwayX;

                boolean isStump = (speciesIdx == 6);
                double shadowOffsetDist = trunkH * 0.45;
                double shadowDx = Math.cos(radAz + Math.PI / 4.0) * shadowOffsetDist + windSwayX * 0.5;
                double shadowDy = Math.sin(radAz + Math.PI / 4.0) * shadowOffsetDist * Math.sin(radEl) + windSwayY * 0.5;
                double shadowW = canopyR * 1.5;
                double shadowH = canopyR * 0.7 * Math.sin(radEl);

                if (!isStump && trunkH > 3.0) {
                    double perpAngle = Math.atan2(shadowDy, shadowDx) + Math.PI / 2.0;
                    double halfTw = Math.max(1.2, trunkW * 0.45);
                    double pxPerp = Math.cos(perpAngle) * halfTw;
                    double pyPerp = Math.sin(perpAngle) * halfTw;
                    double[] trunkShadowX = new double[]{
                        p[0] - pxPerp, p[0] + pxPerp,
                        p[0] + shadowDx + pxPerp * 0.7, p[0] + shadowDx - pxPerp * 0.7
                    };
                    double[] trunkShadowY = new double[]{
                        p[1] - pyPerp, p[1] + pyPerp,
                        p[1] + shadowDy + pyPerp * 0.7, p[1] + shadowDy - pyPerp * 0.7
                    };
                    pane.gc3D.setFill(Color.color(0.02, 0.04, 0.08, pane.isSimulationMode ? 0.18 : 0.12));
                    pane.gc3D.fillPolygon(trunkShadowX, trunkShadowY, 4);
                } else if (isStump) {
                    double sDist = trunkH * 0.35;
                    double sDx = Math.cos(radAz + Math.PI / 4.0) * sDist;
                    double sDy = Math.sin(radAz + Math.PI / 4.0) * sDist * Math.sin(radEl);
                    double halfTw = Math.max(1.8, trunkW * 0.6);
                    double perpAngle = Math.atan2(sDy, sDx) + Math.PI / 2.0;
                    double pxPerp = Math.cos(perpAngle) * halfTw;
                    double pyPerp = Math.sin(perpAngle) * halfTw;
                    double[] stumpShadowX = new double[]{
                        p[0] - pxPerp, p[0] + pxPerp,
                        p[0] + sDx + pxPerp * 0.8, p[0] + sDx - pxPerp * 0.8
                    };
                    double[] stumpShadowY = new double[]{
                        p[1] - pyPerp, p[1] + pyPerp,
                        p[1] + sDy + pyPerp * 0.8, p[1] + sDy - pyPerp * 0.8
                    };
                    pane.gc3D.setFill(Color.color(0.01, 0.02, 0.05, 0.22));
                    pane.gc3D.fillPolygon(stumpShadowX, stumpShadowY, 4);
                }

                if (!isStump) {
                    pane.gc3D.setFill(Color.color(0.02, 0.04, 0.09, pane.isSimulationMode ? 0.18 : 0.12));
                    pane.gc3D.fillOval(p[0] + shadowDx - shadowW * 0.5, p[1] + shadowDy - shadowH * 0.5, shadowW, shadowH);
                }

                pane.gc3D.setFill(Color.color(0.01, 0.02, 0.05, 0.28));
                pane.gc3D.fillOval(p[0] - trunkW * 0.75, p[1] - trunkW * 0.25, trunkW * 1.5, trunkW * 0.5);

                int seasonIdx = 1;
                if (pane.isSimulationMode && pane.simSeason != null) {
                    String sName = pane.simSeason.toLowerCase();
                    if (sName.contains("printemps") || sName.contains("spring")) seasonIdx = 0;
                    else if (sName.contains("été") || sName.contains("summer")) seasonIdx = 1;
                    else if (sName.contains("automne") || sName.contains("autumn") || sName.contains("fall")) seasonIdx = 2;
                    else if (sName.contains("hiver") || sName.contains("winter")) seasonIdx = 3;
                }

                double latVal = 48.4047;
                if (pane.latField != null && pane.latField.getText() != null) {
                    String latTxt = pane.latField.getText().trim();
                    try {
                        latVal = Double.parseDouble(latTxt.replaceAll("[^0-9.-]", ""));
                        if (latTxt.toLowerCase().contains("s") || latTxt.toLowerCase().contains("sud")) {
                            latVal = -Math.abs(latVal);
                        }
                    } catch (Exception ignored) {
                        if (latTxt.startsWith("-") || latTxt.toLowerCase().contains("s") || latTxt.toLowerCase().contains("sud")) {
                            latVal = -34.0;
                        }
                    }
                }

                String bName = pane.presetsCombo != null && pane.presetsCombo.getValue() != null ? pane.presetsCombo.getValue().toUpperCase() : "";
                boolean isEquatorialOrWarmBiome = bName.contains("SAVANNA") || bName.contains("DESERT") || bName.contains("TROPICAL") || bName.contains("EQUATORIAL") || Math.abs(latVal) < 12.0;

                if (latVal < 0.0 && !isEquatorialOrWarmBiome) {
                    seasonIdx = (seasonIdx + 2) % 4;
                }

                if (pane.currentRenderMode == WorldEditorPane.RenderMode.GAMIFIED) {
                    double worldX = ti.gx;
                    double worldY = ti.gy;
                    double worldZ = pane.heightGrid[ti.gx][ti.gy] * 40.0;
                    double vSize = step * 1.5;

                    switch (speciesIdx) {
                        case 5 -> {
                            for (double[] offset : new double[][]{{-0.4, -0.4}, {0.3, -0.2}, {-0.1, 0.4}}) {
                                double bx = worldX + offset[0] * vSize;
                                double by = worldY + offset[1] * vSize;
                                double bH = Math.max(vSize * 3.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.50);
                                int bC = Math.max(3, (int)(bH / vSize));
                                double dz = bH / bC;
                                for (int i = 0; i < bC; i++) {
                                    pane.drawGamifiedVoxelCube3D(bx - vSize * 0.25, by - vSize * 0.25, worldZ + i * dz, vSize * 0.5, vSize * 0.5, dz, Color.web("#84cc16"), Color.web("#4d7c0f"), cx, cy, scale, radAz, radEl);
                                }
                                pane.drawGamifiedVoxelCube3D(bx - vSize * 0.5, by - vSize * 0.5, worldZ + bH, vSize, vSize, vSize * 0.8, Color.web("#4ade80"), Color.web("#15803d"), cx, cy, scale, radAz, radEl);
                            }
                        }
                        case 6 -> {
                            pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.6, worldY - vSize * 0.6, worldZ, vSize * 1.2, vSize * 1.2, vSize * 0.8, Color.web("#92400e"), Color.web("#451a03"), cx, cy, scale, radAz, radEl);
                            pane.drawGamifiedVoxelCube3D(worldX + vSize * 0.2, worldY - vSize * 0.4, worldZ + vSize * 0.8, vSize * 0.5, vSize * 0.5, vSize * 0.3, Color.web("#f59e0b"), Color.web("#b45309"), cx, cy, scale, radAz, radEl);
                        }
                        case 3 -> {
                            double cH = Math.max(vSize * 4.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.60);
                            int cCount = Math.max(4, (int)(cH / vSize));
                            double dz = cH / cCount;
                            for (int i = 0; i < cCount; i++) {
                                pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.4, worldY - vSize * 0.4, worldZ + i * dz, vSize * 0.8, vSize * 0.8, dz, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                            }
                            pane.drawGamifiedVoxelCube3D(worldX - vSize * 1.2, worldY - vSize * 0.3, worldZ + cH * 0.5, vSize * 0.8, vSize * 0.6, vSize * 0.6, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                            pane.drawGamifiedVoxelCube3D(worldX - vSize * 1.2, worldY - vSize * 0.3, worldZ + cH * 0.5 + vSize * 0.6, vSize * 0.6, vSize * 0.6, vSize * 1.0, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                            pane.drawGamifiedVoxelCube3D(worldX + vSize * 0.4, worldY - vSize * 0.3, worldZ + cH * 0.65, vSize * 0.8, vSize * 0.6, vSize * 0.6, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                            pane.drawGamifiedVoxelCube3D(worldX + vSize * 0.6, worldY - vSize * 0.3, worldZ + cH * 0.65 + vSize * 0.6, vSize * 0.6, vSize * 0.6, vSize * 0.8, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                        }
                        case 1 -> {
                            double vTrunkH = Math.max(step * 4.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.65);
                            double trunkTopZ = worldZ + vTrunkH;
                            int trunkCount = Math.max(3, (int)(vTrunkH / vSize));
                            double dz = vTrunkH / trunkCount;
                            for (int i = 0; i < trunkCount; i++) {
                                pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.4, worldY - vSize * 0.4, worldZ + i * dz, vSize * 0.8, vSize * 0.8, dz, Color.web("#78350f"), Color.web("#451a03"), cx, cy, scale, radAz, radEl);
                            }
                            Color pineCol = seasonIdx == 3 ? Color.web("#1e293b") : Color.web("#14532d");
                            for (int bx = -2; bx <= 2; bx++) {
                                for (int by = -2; by <= 2; by++) {
                                    if (Math.abs(bx) == 2 && Math.abs(by) == 2) continue;
                                    pane.drawGamifiedVoxelCube3D(worldX + bx * vSize * 0.7 - vSize * 0.4, worldY + by * vSize * 0.7 - vSize * 0.4, trunkTopZ - vSize * 0.5, vSize * 0.9, vSize * 0.9, vSize * 0.9, pineCol, pineCol.darker(), cx, cy, scale, radAz, radEl);
                                }
                            }
                            for (int bx = -1; bx <= 1; bx++) {
                                for (int by = -1; by <= 1; by++) {
                                    pane.drawGamifiedVoxelCube3D(worldX + bx * vSize * 0.7 - vSize * 0.4, worldY + by * vSize * 0.7 - vSize * 0.4, trunkTopZ + vSize * 0.4, vSize * 0.9, vSize * 0.9, vSize * 0.9, pineCol, pineCol.darker(), cx, cy, scale, radAz, radEl);
                                }
                            }
                            pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.4, worldY - vSize * 0.4, trunkTopZ + vSize * 1.3, vSize * 0.9, vSize * 0.9, vSize * 1.1, pineCol, pineCol.darker(), cx, cy, scale, radAz, radEl);
                        }
                        case 2 -> {
                            double vTrunkH = Math.max(step * 4.5, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.70);
                            double trunkTopZ = worldZ + vTrunkH;
                            int trunkCount = Math.max(3, (int)(vTrunkH / vSize));
                            double dz = vTrunkH / trunkCount;
                            for (int i = 0; i < trunkCount; i++) {
                                pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.45, worldY - vSize * 0.45, worldZ + i * dz, vSize * 0.9, vSize * 0.9, dz, Color.web("#78350f"), Color.web("#451a03"), cx, cy, scale, radAz, radEl);
                            }
                            Color acaciaCol = seasonIdx == 2 ? Color.web("#eab308") : Color.web("#a16207");
                            for (int bx = -3; bx <= 3; bx++) {
                                for (int by = -3; by <= 3; by++) {
                                    if (Math.abs(bx) == 3 && Math.abs(by) == 3) continue;
                                    pane.drawGamifiedVoxelCube3D(worldX + bx * vSize * 0.7 - vSize * 0.4, worldY + by * vSize * 0.7 - vSize * 0.4, trunkTopZ, vSize * 0.9, vSize * 0.9, vSize * 0.6, acaciaCol, acaciaCol.darker(), cx, cy, scale, radAz, radEl);
                                }
                            }
                        }
                        default -> {
                            double vTrunkH = Math.max(step * 4.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.65);
                            double trunkTopZ = worldZ + vTrunkH;
                            int trunkCount = Math.max(3, (int)(vTrunkH / vSize));
                            double dz = vTrunkH / trunkCount;

                            Color barkCol = (speciesIdx == 4) ? Color.web("#f8fafc") : Color.web("#78350f");
                            Color barkSideCol = (speciesIdx == 4) ? Color.web("#cbd5e1") : Color.web("#451a03");

                            for (int i = 0; i < trunkCount; i++) {
                                pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.5, worldY - vSize * 0.5, worldZ + i * dz, vSize, vSize, dz, barkCol, barkSideCol, cx, cy, scale, radAz, radEl);
                            }

                            Color leafBaseCol = switch (speciesIdx) {
                                case 4 -> (seasonIdx == 2 ? Color.web("#fde047") : (seasonIdx == 0 ? Color.web("#86efac") : (seasonIdx == 3 ? Color.web("#e2e8f0") : Color.web("#4ade80"))));
                                default -> (seasonIdx == 2 ? Color.web("#d97706") : (seasonIdx == 0 ? Color.web("#22c55e") : (seasonIdx == 3 ? Color.web("#cbd5e1") : Color.web("#15803d"))));
                            };
                            Color leafSideCol = leafBaseCol.darker();

                            double cSize = Math.max(vSize * 1.2, (canopyRadiusM / sideM) * WorldEditorPane.GRID_SIZE * 0.40);
                            int rVox = 2;
                            int hVox = 3;

                            int startX = Math.cos(radAz) >= 0 ? -rVox : rVox;
                            int endX = Math.cos(radAz) >= 0 ? rVox : -rVox;
                            int stepX = Math.cos(radAz) >= 0 ? 1 : -1;

                            int startY = Math.sin(radAz) >= 0 ? -rVox : rVox;
                            int endY = Math.sin(radAz) >= 0 ? rVox : -rVox;
                            int stepY = Math.sin(radAz) >= 0 ? 1 : -1;

                            for (int bz = 0; bz < hVox; bz++) {
                                for (int bx = startX; bx != endX + stepX; bx += stepX) {
                                    for (int by = startY; by != endY + stepY; by += stepY) {
                                        if (Math.abs(bx) == rVox && Math.abs(by) == rVox && (bz == 0 || bz == hVox - 1)) continue;
                                        double cxWorld = worldX + bx * cSize * 0.70 - cSize * 0.5;
                                        double cyWorld = worldY + by * cSize * 0.70 - cSize * 0.5;
                                        double czWorld = trunkTopZ + bz * cSize * 0.70;

                                        pane.drawGamifiedVoxelCube3D(cxWorld, cyWorld, czWorld, cSize, cSize, cSize, leafBaseCol, leafSideCol, cx, cy, scale, radAz, radEl);
                                    }
                                }
                            }
                        }
                    }
                    continue;
                }

                double gridPerM = (double) WorldEditorPane.GRID_SIZE / Math.max(1.0, sideM);

                switch (speciesIdx) {
                    case 1 -> { // Pin Sylvestre
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && (pineMesh != null || oakMesh != null)) {
                            ObjMesh pMesh = pineMesh != null ? pineMesh : oakMesh;
                            double pScale = (treeHeightM * gridPerM) / Math.max(0.1, pMesh.height);
                            Color pineFoliage = (seasonIdx == 3) ? Color.web("#14532d") : Color.web("#166534");
                            drawObjSingleMesh3D(pane, pMesh, ti.gx, ti.gy, z, pScale, pineFoliage, Color.web("#78350f"), cx, cy, scale, radAz, radEl);
                        } else {
                            draw3DCylinderTrunk(pane, p[0], p[1], trunkW, trunkH, Color.web("#78350f"), Color.web("#3d1a04"), Color.web("#a16207"));
                            double[] pxs1 = {swayX, swayX - canopyR * 0.95, swayX + canopyR * 0.95};
                            double[] pys1 = {p[1] - trunkH - canopyR * 0.7, p[1] - trunkH * 0.35, p[1] - trunkH * 0.35};
                            pane.gc3D.setFill(Color.web("#166534")); pane.gc3D.fillPolygon(pxs1, pys1, 3);
                            double[] pxs2 = {swayX, swayX - canopyR * 0.75, swayX + canopyR * 0.75};
                            double[] pys2 = {p[1] - trunkH - canopyR * 1.1, p[1] - trunkH * 0.62, p[1] - trunkH * 0.62};
                            pane.gc3D.setFill(Color.web("#15803d")); pane.gc3D.fillPolygon(pxs2, pys2, 3);
                        }
                    }
                    case 2 -> { // Acacia
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && (oakMesh != null || tropicalObjMeshes != null)) {
                            ObjMesh aMesh = oakMesh != null ? oakMesh : (tropicalObjMeshes != null && !tropicalObjMeshes.isEmpty() ? tropicalObjMeshes.get(0) : null);
                            if (aMesh != null) {
                                double aScale = (treeHeightM * gridPerM) / Math.max(0.1, aMesh.height);
                                Color aCol = (seasonIdx == 2 || seasonIdx == 3) ? Color.web("#eab308") : Color.web("#4d7c0f");
                                drawObjSingleMesh3D(pane, aMesh, ti.gx, ti.gy, z, aScale, aCol, Color.web("#78350f"), cx, cy, scale, radAz, radEl);
                            }
                        } else {
                            draw3DCylinderTrunk(pane, p[0], p[1], trunkW, trunkH, Color.web("#78350f"), Color.web("#3d1a04"), Color.web("#a16207"));
                            pane.gc3D.setFill(Color.web("#4d7c0f"));
                            pane.gc3D.fillOval(swayX - canopyR * 1.1, p[1] - trunkH * 0.95 - canopyR * 0.45, canopyR * 2.2, canopyR * 0.75);
                        }
                    }
                    case 3 -> { // Cactus Saguaro
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && cactusObjMeshes != null && !cactusObjMeshes.isEmpty()) {
                            ObjMesh cMesh = cactusObjMeshes.get(0);
                            double cScale = (3.2 * ti.ageScale * gridPerM) / Math.max(0.1, cMesh.height);
                            drawObjSingleMesh3D(pane, cMesh, ti.gx, ti.gy, z, cScale, Color.web("#15803d"), Color.web("#14532d"), cx, cy, scale, radAz, radEl);
                        } else {
                            draw3DCylinderTrunk(pane, p[0], p[1], trunkW, trunkH, Color.web("#15803d"), Color.web("#14532d"), Color.web("#4ade80"));
                            pane.gc3D.setFill(Color.web("#166534"));
                            pane.gc3D.fillRect(p[0] - trunkH * 0.3, p[1] - trunkH * 0.6, trunkH * 0.6, trunkW * 0.7);
                            pane.gc3D.fillRect(p[0] - trunkH * 0.3, p[1] - trunkH * 0.85, trunkW * 0.7, trunkH * 0.3);
                            pane.gc3D.fillRect(p[0] + trunkH * 0.2, p[1] - trunkH * 0.9, trunkW * 0.7, trunkH * 0.35);
                        }
                    }
                    case 4 -> { // Bouleau
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && (birchMesh != null || oakMesh != null)) {
                            ObjMesh bMesh = birchMesh != null ? birchMesh : oakMesh;
                            double bScale = (treeHeightM * gridPerM) / Math.max(0.1, bMesh.height);
                            Color foliageB = (seasonIdx == 2) ? Color.web("#fde047") : ((seasonIdx == 3) ? Color.web("#f8fafc") : Color.web("#86efac"));
                            drawObjSingleMesh3D(pane, bMesh, ti.gx, ti.gy, z, bScale, foliageB, Color.web("#f8fafc"), cx, cy, scale, radAz, radEl);
                        } else {
                            draw3DCylinderTrunk(pane, p[0], p[1], trunkW, trunkH, Color.web("#f8fafc"), Color.web("#475569"), Color.web("#ffffff"));
                            pane.gc3D.setFill(Color.web("#0f172a"));
                            pane.gc3D.fillRect(p[0] - trunkW * 0.45, p[1] - trunkH * 0.7, trunkW * 0.9, Math.max(2.0, trunkW * 0.15));
                            pane.gc3D.fillRect(p[0] - trunkW * 0.45, p[1] - trunkH * 0.4, trunkW * 0.9, Math.max(2.0, trunkW * 0.15));
                            pane.gc3D.setStroke(Color.web("#334155"));
                            pane.gc3D.setLineWidth(Math.max(1.5, trunkW * 0.3));
                            pane.gc3D.strokeLine(p[0], p[1] - trunkH * 0.4, swayX - canopyR * 0.6, p[1] - trunkH * 0.75);
                            pane.gc3D.strokeLine(p[0], p[1] - trunkH * 0.4, swayX + canopyR * 0.6, p[1] - trunkH * 0.75);
                            if (seasonIdx != 3) {
                                Color bCol1 = (seasonIdx == 2) ? Color.web("#fde047") : Color.web("#86efac");
                                pane.gc3D.setFill(bCol1);
                                pane.gc3D.fillOval(swayX - canopyR * 0.75, p[1] - trunkH - canopyR * 0.7, canopyR * 1.5, canopyR * 1.2);
                            }
                        }
                    }
                    case 5 -> { // Bambouseraie
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && bambooObjMeshes != null && !bambooObjMeshes.isEmpty()) {
                            ObjMesh bMesh = bambooObjMeshes.get(0);
                            double bScale = (3.5 * ti.ageScale * gridPerM) / Math.max(0.1, bMesh.height);
                            drawObjSingleMesh3D(pane, bMesh, ti.gx, ti.gy, z, bScale, Color.web("#4ade80"), Color.web("#15803d"), cx, cy, scale, radAz, radEl);
                        } else {
                            pane.gc3D.setStroke(Color.web("#84cc16")); pane.gc3D.setLineWidth(Math.max(2.0, trunkW * 0.35));
                            pane.gc3D.strokeLine(p[0] - trunkW * 0.6, p[1], swayX - trunkW * 0.4, p[1] - trunkH * 1.1);
                            pane.gc3D.strokeLine(p[0] + trunkW * 0.6, p[1], swayX + trunkW * 0.7, p[1] - trunkH * 1.25);
                            pane.gc3D.strokeLine(p[0], p[1], swayX, p[1] - trunkH * 1.2);
                            Color fCol = switch (seasonIdx) {
                                case 0 -> Color.web("#84cc16");
                                case 2 -> Color.web("#eab308");
                                case 3 -> Color.web("#a1a1aa");
                                default -> Color.web("#4ade80");
                            };
                            pane.gc3D.setFill(fCol);
                            pane.gc3D.fillOval(swayX - canopyR * 0.6, p[1] - trunkH * 1.25, canopyR * 1.2, canopyR * 0.7);
                        }
                    }
                    case 6 -> { // Souche / Bois mort
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && (stumpMesh != null || deadTreeMesh != null)) {
                            ObjMesh sMesh = stumpMesh != null ? stumpMesh : deadTreeMesh;
                            double sScale = (0.55 * ti.ageScale * gridPerM) / Math.max(0.1, sMesh.height);
                            drawObjSingleMesh3D(pane, sMesh, ti.gx, ti.gy, z, sScale, Color.web("#78350f"), Color.web("#451a03"), cx, cy, scale, radAz, radEl);
                        } else {
                            double stumpW = Math.max(3.0, (0.45 * ti.ageScale * gridPerM) * (pane.zoom / 7.5) * 8.0);
                            double stumpH = Math.max(2.5, (0.35 * ti.ageScale * gridPerM) * (pane.zoom / 7.5) * 8.0);
                            draw3DVolumetricStump(pane, p[0], p[1], stumpW, stumpH);
                        }
                    }
                    default -> { // Chêne Quercus
                        if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC && oakMesh != null) {
                            double oScale = (treeHeightM * gridPerM) / Math.max(0.1, oakMesh.height);
                            Color oakFoliage = switch (seasonIdx) {
                                case 0 -> Color.web("#4ade80");
                                case 2 -> Color.web("#d97706");
                                case 3 -> Color.web("#78716c");
                                default -> Color.web("#15803d");
                            };
                            drawObjSingleMesh3D(pane, oakMesh, ti.gx, ti.gy, z, oScale, oakFoliage, Color.web("#78350f"), cx, cy, scale, radAz, radEl);
                        } else {
                            draw3DCylinderTrunk(pane, p[0], p[1], trunkW, trunkH, Color.web("#78350f"), Color.web("#3d1a04"), Color.web("#a16207"));
                            Color cCol = switch (seasonIdx) {
                                case 0 -> Color.web("#4ade80");
                                case 2 -> Color.web("#d97706");
                                case 3 -> Color.web("#f8fafc");
                                default -> Color.web("#15803d");
                            };
                            pane.gc3D.setFill(cCol);
                            pane.gc3D.fillOval(swayX - canopyR, p[1] - trunkH - canopyR * 0.9, canopyR * 2.0, canopyR * 1.3);
                        }
                    }
                }
            }
        }

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
        if (forestObjMeshes == null) {
            forestObjMeshes = ObjModelLoader.loadObjModel("/models/forest_nature_set_all_in.obj");
            bambooObjMeshes = ObjModelLoader.loadObjModel("/models/bamboo_set.obj");
            cactusObjMeshes = ObjModelLoader.loadObjModel("/models/cactus.obj");
            tropicalObjMeshes = ObjModelLoader.loadObjModel("/models/tropical_plants.obj");

            oakMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Tree_average_regular");
            if (oakMesh == null) oakMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Tree_average_lush");
            birchMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Tree_small_regular");
            pineMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Tree_Spruce_small");
            deadTreeMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Tree_average_bare");
            stumpMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Stump_average_high");
            bushMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Bush_average");
            grassHighMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Grass_bush_high");
            grassLowMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Grass_bush_low");
            flowerWhiteMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Flower_bush_white");
            flowerRedMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Flower_bush_red");
            mushroomMesh = ObjModelLoader.findSubmesh(forestObjMeshes, "Mushroom_big_brown");
            fernMesh = ObjModelLoader.findSubmesh(tropicalObjMeshes, "Plant_alpha");
        }
    }

    public static void drawObjSingleMesh3D(
            WorldEditorPane pane, ObjMesh mesh,
            double wx, double wy, double wz, double scaleObj,
            Color foliageColor, Color barkColor,
            double cx, double cy, double scale, double radAz, double radEl) {
        loadObjMeshesIfNeeded();
        if (mesh == null || mesh.vertices == null || mesh.vertices.isEmpty() || mesh.faces == null || mesh.faces.isEmpty()) return;

        int faceCount = mesh.faces.size();
        for (int i = 0; i < faceCount; i++) {
            int[] face = mesh.faces.get(i);
            if (face.length < 3) continue;

            double[] xPts = new double[face.length];
            double[] yPts = new double[face.length];
            boolean valid = true;
            double avgY = 0;

            for (int k = 0; k < face.length; k++) {
                int vIdx = face[k];
                if (vIdx >= mesh.vertices.size()) { valid = false; break; }
                float[] v = mesh.vertices.get(vIdx);
                avgY += v[1];

                double vx = wx + v[0] * scaleObj;
                double vy = wy + v[2] * scaleObj;
                double vz = wz + v[1] * scaleObj;

                double[] p = pane.project3DPoint(vx, vy, vz, cx, cy, scale, radAz, radEl);
                xPts[k] = p[0];
                yPts[k] = p[1];
            }

            if (valid) {
                avgY /= face.length;
                boolean isTrunk = (barkColor != null) && (avgY < mesh.height * 0.32) && (mesh.name != null && (mesh.name.toLowerCase().contains("tree") || mesh.name.toLowerCase().contains("stump") || mesh.name.toLowerCase().contains("log") || mesh.name.toLowerCase().contains("wood") || mesh.name.toLowerCase().contains("bark")));
                Color col = isTrunk ? barkColor : foliageColor;
                double shade = 0.90 + ((i % 11) * 0.02);
                Color shadedCol = col.deriveColor(0, 1.0, shade, 1.0);

                pane.gc3D.setFill(shadedCol);
                pane.gc3D.fillPolygon(xPts, yPts, face.length);
                pane.gc3D.setStroke(shadedCol.darker());
                pane.gc3D.setLineWidth(0.30);
                pane.gc3D.strokePolygon(xPts, yPts, face.length);
            }
        }
    }

    public static void drawObjMesh3D(
            WorldEditorPane pane, List<ObjMesh> objMeshes,
            double wx, double wy, double wz, double scaleObj,
            Color baseColor, double cx, double cy, double scale, double radAz, double radEl) {
        loadObjMeshesIfNeeded();
        if (objMeshes == null || objMeshes.isEmpty()) return;
        for (ObjMesh mesh : objMeshes) {
            drawObjSingleMesh3D(pane, mesh, wx, wy, wz, scaleObj, baseColor, Color.web("#78350f"), cx, cy, scale, radAz, radEl);
        }
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

        LinearGradient trunkGrad = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, shadowCol),
            new Stop(0.35, mainCol),
            new Stop(0.75, highlightCol),
            new Stop(1.0, shadowCol.darker())
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

        LinearGradient trunkGrad = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, barkShadow),
            new Stop(0.35, barkBase),
            new Stop(0.75, barkHighlight),
            new Stop(1.0, barkShadow.darker())
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
