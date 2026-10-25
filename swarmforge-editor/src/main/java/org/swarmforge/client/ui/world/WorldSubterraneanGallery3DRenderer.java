/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.TunnelNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D Subterranean Gallery Networks & Excavated Chambers Renderer for the World Editor Studio.
 * Handles subterranean galleries, chambers, shafts, and glassmorphism HUD legend.
 */
public class WorldSubterraneanGallery3DRenderer {

    private final WorldEpigeicNestStructure3DRenderer epigeicRenderer;

    public WorldSubterraneanGallery3DRenderer() {
        this(new WorldEpigeicNestStructure3DRenderer());
    }

    public WorldSubterraneanGallery3DRenderer(WorldEpigeicNestStructure3DRenderer epigeicRenderer) {
        this.epigeicRenderer = epigeicRenderer;
    }
    public void drawGalleriesOverlay3D(WorldEditorPane pane, GraphicsContext gc3D,
                                       double cx, double cy, double scale, double radAz, double radEl) {
        // 1. Render 3D Surface Nest Architecture Structure only in 2D technical mode (3D OpenGL engine handles realistic & gamified modes)
        if (pane.getRenderMode() == RenderMode.SCIENTIFIC) {
            if (epigeicRenderer != null) epigeicRenderer.drawRealistic3DNestStructure(pane, gc3D, cx, cy, scale, radAz, radEl);
        }

        int gridSize = WorldEditorPane.GRID_SIZE;
        int soilDepth = WorldEditorPane.SOIL_DEPTH;
        double[][] heightGrid = pane.getHeightGrid();
        boolean[][][] voidGrid = pane.getVoidGrid();
        Simulation activeSimulation = pane.getActiveSimulation();
        double zoom = pane.getZoom();

        int nestX = gridSize / 2;
        int nestY = gridSize / 2;
        if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
            int tWidth = Math.max(1, activeSimulation.getTerrarium().getWidth());
            int tHeight = Math.max(1, activeSimulation.getTerrarium().getHeight());
            Colony col0 = activeSimulation.getColonies().get(0);
            nestX = (int) Math.max(0, Math.min(gridSize - 1, ((col0.getNestX() + 0.5) / (double) tWidth) * gridSize));
            nestY = (int) Math.max(0, Math.min(gridSize - 1, ((col0.getNestY() + 0.5) / (double) tHeight) * gridSize));
        }
        double baseZ = heightGrid[nestX][nestY] * 40.0;
        double zSc = zoom / 7.5;

        // Determine if current species architecture is arboreal/suspended or leafcutter/termite
        boolean isArboreal = false;
        boolean isLeafcutterOrTermite = false;
        String adapt = pane.getSpeciesAdaptationValue().toLowerCase();
        if (adapt.contains("vespula") || adapt.contains("polistes") || adapt.contains("guêpe") ||
            adapt.contains("cremato") || adapt.contains("oeco") || adapt.contains("apis") || adapt.contains("abeille")) {
            isArboreal = true;
        }
        if (adapt.contains("atta") || adapt.contains("coupeuse") || adapt.contains("termit") || adapt.contains("macrotermes")) {
            isLeafcutterOrTermite = true;
        }

        // Scan actual carved voidGrid voxels (Real Voxel-by-Voxel Excavation Matrix)
        int carvedVoxCount = 0;
        for (int x = nestX - 10; x <= nestX + 10; x++) {
            for (int y = nestY - 10; y <= nestY + 10; y++) {
                if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) continue;
                for (int d = 0; d < soilDepth; d++) {
                    if (voidGrid[x][y][d]) carvedVoxCount++;
                }
            }
        }

        // Physical real chamber scale derived from actual carved voxel volume
        double volScale = Math.max(0.75, Math.min(2.5, Math.cbrt(Math.max(40, carvedVoxCount) / 80.0)));

        // Real physical chamber radii (cm converted to projected 3D screen px)
        double rQueen   = Math.max(3.5, 6.0 * zSc * volScale);   // Loge Royale (~60-80mm real diameter)
        double rBrood   = Math.max(3.0, 5.0 * zSc * volScale);   // Loge Couvain (~40-50mm real diameter)
        double rFood    = Math.max(2.8, 4.5 * zSc * volScale);   // Grenier / Stock (~40mm real diameter)
        double rFungus  = Math.max(3.2, 5.5 * zSc * volScale);   // Champignonnière Atta / Termites (~55mm diameter)
        double rAphid   = Math.max(2.8, 4.5 * zSc * volScale);   // Laiterie à Pucerons / Élevage
        double rHibern  = Math.max(3.0, 5.0 * zSc * volScale);   // Chambre Hibernation / Diapause
        double rTrash   = Math.max(2.5, 4.0 * zSc * volScale);   // Dépotoir / Déchets

        String simSeason = pane.getSimSeason();
        boolean isWinter = simSeason != null && (simSeason.toLowerCase().contains("hiver") || simSeason.toLowerCase().contains("winter"));
        double wtDepth = pane.getWaterTableDepthSliderValue();
        boolean isFlooded = wtDepth < 18.0 || (pane.getBaseHumiditySliderValue() > 0.70);

        if (isArboreal) {
            // ARBOREAL / BRANCH-SUSPENDED NEST TOPOLOGY (Chambers inside/around tree canopy branch)
            double branchZ = baseZ + 35.0;
            double[] pBranch = pane.project3DPoint(nestX, nestY, branchZ, cx, cy, scale, radAz, radEl);

            double[] pQ = pane.project3DPoint(nestX + 2, nestY, branchZ + 6.0, cx, cy, scale, radAz, radEl);
            double[] pB = pane.project3DPoint(nestX - 3, nestY + 2, branchZ - 2.0, cx, cy, scale, radAz, radEl);
            double[] pF = pane.project3DPoint(nestX + 3, nestY - 2, branchZ - 4.0, cx, cy, scale, radAz, radEl);

            gc3D.setStroke(Color.web("#f59e0b", 0.70));
            gc3D.setLineWidth(Math.max(2.0, 3.5 * zSc));
            gc3D.strokeLine(pBranch[0], pBranch[1], pQ[0], pQ[1]);
            gc3D.strokeLine(pBranch[0], pBranch[1], pB[0], pB[1]);
            gc3D.strokeLine(pBranch[0], pBranch[1], pF[0], pF[1]);

            // 1. Loge Royale (Magenta)
            gc3D.setFill(Color.web("#d946ef", 0.65));
            gc3D.fillOval(pQ[0] - rQueen, pQ[1] - rQueen, rQueen * 2, rQueen * 2);
            gc3D.setStroke(Color.web("#f0abfc", 0.90)); gc3D.setLineWidth(1.5);
            gc3D.strokeOval(pQ[0] - rQueen, pQ[1] - rQueen, rQueen * 2, rQueen * 2);

            // 2. Chambre du Couvain (Blanc)
            gc3D.setFill(Color.web("#f8fafc", 0.65));
            gc3D.fillOval(pB[0] - rBrood, pB[1] - rBrood, rBrood * 2, rBrood * 2);
            gc3D.setStroke(Color.web("#ffffff", 0.90)); gc3D.setLineWidth(1.2);
            gc3D.strokeOval(pB[0] - rBrood, pB[1] - rBrood, rBrood * 2, rBrood * 2);

            // 3. Grenier / Stockage (Vert)
            gc3D.setFill(Color.web("#22c55e", 0.65));
            gc3D.fillOval(pF[0] - rFood, pF[1] - rFood, rFood * 2, rFood * 2);
            gc3D.setStroke(Color.web("#4ade80", 0.90)); gc3D.setLineWidth(1.2);
            gc3D.strokeOval(pF[0] - rFood, pF[1] - rFood, rFood * 2, rFood * 2);
        } else {
            // SUBTERRANEAN / MOUND NEST TOPOLOGY (Multiple Exit Craters + Excavated Soil Mounds + Deep Stratified Chambers)
            int exit1X = nestX, exit1Y = nestY;
            int exit2X = Math.min(gridSize - 1, nestX + 6), exit2Y = Math.max(0, nestY - 5);
            int exit3X = Math.max(0, nestX - 7), exit3Y = Math.min(gridSize - 1, nestY + 4);

            double zE1 = heightGrid[exit1X][exit1Y] * 40.0;
            double zE2 = heightGrid[exit2X][exit2Y] * 40.0;
            double zE3 = heightGrid[exit3X][exit3Y] * 40.0;

            double[] pE1 = pane.project3DPoint(exit1X, exit1Y, zE1, cx, cy, scale, radAz, radEl);
            double[] pE2 = pane.project3DPoint(exit2X, exit2Y, zE2, cx, cy, scale, radAz, radEl);
            double[] pE3 = pane.project3DPoint(exit3X, exit3Y, zE3, cx, cy, scale, radAz, radEl);

            if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
                int tWidth = Math.max(1, activeSimulation.getTerrarium().getWidth());
                int tHeight = Math.max(1, activeSimulation.getTerrarium().getHeight());

                for (Colony colony : activeSimulation.getColonies()) {
                    List<double[]> entrancePositions = new ArrayList<>();
                    TunnelNetwork tn = colony.getTunnelNetwork();
                    if (tn != null && !tn.getNodes().isEmpty()) {
                        for (TunnelNetwork.TunnelNode n : tn.getNodes()) {
                            float sY = activeSimulation.getTerrarium().getSurfaceElevation(n.x(), n.y());
                            if (n.type() == TunnelNetwork.ChamberType.ENTRANCE || Math.abs(n.z() - sY) < 0.6f) {
                                double egx = (n.x() / (double) tWidth) * gridSize;
                                double egy = (n.y() / (double) tHeight) * gridSize;
                                entrancePositions.add(new double[]{egx, egy});
                            }
                        }
                    }
                    if (entrancePositions.isEmpty()) {
                        int cNestX = (int) Math.max(0, Math.min(gridSize - 1, colony.getNestX()));
                        int cNestY = (int) Math.max(0, Math.min(gridSize - 1, colony.getNestY()));
                        entrancePositions.add(new double[]{(double) cNestX, (double) cNestY});
                    }

                    if (!entrancePositions.isEmpty()) {
                        double[] ep1 = entrancePositions.get(0);
                        int ix1 = (int) Math.max(0, Math.min(gridSize - 1, ep1[0]));
                        int iy1 = (int) Math.max(0, Math.min(gridSize - 1, ep1[1]));
                        pE1 = pane.project3DPoint(ep1[0], ep1[1], heightGrid[ix1][iy1] * 40.0, cx, cy, scale, radAz, radEl);

                        if (entrancePositions.size() >= 2) {
                            double[] ep2 = entrancePositions.get(1);
                            int ix2 = (int) Math.max(0, Math.min(gridSize - 1, ep2[0]));
                            int iy2 = (int) Math.max(0, Math.min(gridSize - 1, ep2[1]));
                            pE2 = pane.project3DPoint(ep2[0], ep2[1], heightGrid[ix2][iy2] * 40.0, cx, cy, scale, radAz, radEl);
                        } else {
                            pE2 = pE1;
                        }

                        if (entrancePositions.size() >= 3) {
                            double[] ep3 = entrancePositions.get(2);
                            int ix3 = (int) Math.max(0, Math.min(gridSize - 1, ep3[0]));
                            int iy3 = (int) Math.max(0, Math.min(gridSize - 1, ep3[1]));
                            pE3 = pane.project3DPoint(ep3[0], ep3[1], heightGrid[ix3][iy3] * 40.0, cx, cy, scale, radAz, radEl);
                        } else {
                            pE3 = pE1;
                        }
                    }

                    for (double[] ePos : entrancePositions) {
                        int iegx = (int) Math.max(0, Math.min(gridSize - 1, ePos[0]));
                        int iegy = (int) Math.max(0, Math.min(gridSize - 1, ePos[1]));
                        double zE = heightGrid[iegx][iegy] * 40.0;
                        double[] pExit = pane.project3DPoint(ePos[0], ePos[1], zE, cx, cy, scale, radAz, radEl);
                        double dirtR = 7.0 * zSc;

                        gc3D.setFill(Color.web("#9a3412", 0.80)); // Terracotta soil granule ring
                        gc3D.fillOval(pExit[0] - dirtR, pExit[1] - dirtR * 0.5, dirtR * 2, dirtR * 1.0);
                        gc3D.setFill(Color.web("#18181b")); // Entrance aperture
                        gc3D.fillOval(pExit[0] - dirtR * 0.4, pExit[1] - dirtR * 0.25, dirtR * 0.8, dirtR * 0.5);

                        if (isWinter) {
                            gc3D.setFill(Color.web("#f1f5f9", 0.40));
                            gc3D.fillOval(pExit[0] - dirtR * 1.1, pExit[1] - dirtR * 0.55, dirtR * 2.2, dirtR * 1.1);
                            gc3D.setStroke(Color.web("#e0f2fe", 0.90));
                            gc3D.setLineWidth(Math.max(1.2, 2.0 * zSc));
                            gc3D.strokeOval(pExit[0] - dirtR * 1.1, pExit[1] - dirtR * 0.55, dirtR * 2.2, dirtR * 1.1);
                        }
                    }
                }
            } else {
                double dirtR = 7.0 * zSc;
                for (double[] pExit : new double[][]{pE1, pE2, pE3}) {
                    gc3D.setFill(Color.web("#9a3412", 0.80));
                    gc3D.fillOval(pExit[0] - dirtR, pExit[1] - dirtR * 0.5, dirtR * 2, dirtR * 1.0);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(pExit[0] - dirtR * 0.4, pExit[1] - dirtR * 0.25, dirtR * 0.8, dirtR * 0.5);

                    if (isWinter) {
                        gc3D.setFill(Color.web("#f1f5f9", 0.40));
                        gc3D.fillOval(pExit[0] - dirtR * 1.1, pExit[1] - dirtR * 0.55, dirtR * 2.2, dirtR * 1.1);
                        gc3D.setStroke(Color.web("#e0f2fe", 0.90));
                        gc3D.setLineWidth(Math.max(1.2, 2.0 * zSc));
                        gc3D.strokeOval(pExit[0] - dirtR * 1.1, pExit[1] - dirtR * 0.55, dirtR * 2.2, dirtR * 1.1);
                    }
                }
            }

            // Occlusion & Cut-Plane check: subterranean chambers & tunnels are only rendered if terrain is hidden, sliced open at/past nestX, or in translucent volumetric mode
            double cutRatio = pane.getSliceCutRatio();
            int cutXLimit = (int) (gridSize * cutRatio);
            boolean isTranslucent = pane.isTranslucentVolumetricMode();
            boolean isSubterraneanExposed = !pane.isTerrainVisible() || (cutXLimit <= nestX) || isTranslucent;

            if (isSubterraneanExposed) {
                boolean renderedActualNetwork = false;
                if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
                    int tWidth = Math.max(1, activeSimulation.getTerrarium().getWidth());
                    int tHeight = Math.max(1, activeSimulation.getTerrarium().getHeight());

                    for (Colony colony : activeSimulation.getColonies()) {
                        TunnelNetwork tn = colony.getTunnelNetwork();
                        if (tn == null || tn.getNodes().isEmpty()) continue;
                        renderedActualNetwork = true;

                        // 1. Draw tunnel connection lines (with curved organic meander points)
                        for (TunnelNetwork.TunnelEdge edge : tn.getEdges()) {
                            TunnelNetwork.TunnelNode n1 = null;
                            TunnelNetwork.TunnelNode n2 = null;
                            for (TunnelNetwork.TunnelNode node : tn.getNodes()) {
                                if (node.id().equals(edge.fromNode())) n1 = node;
                                if (node.id().equals(edge.toNode())) n2 = node;
                            }
                            if (n1 == null || n2 == null) continue;

                            float dynamicFrostDepth = 1.5f;
                            if (activeSimulation.getSoilHydricCoupling() != null) {
                                dynamicFrostDepth = Math.max(0.4f, activeSimulation.getSoilHydricCoupling().getFrostDepthCells() * 0.5f);
                            } else if (activeSimulation.getWeather() != null) {
                                float sTemp = activeSimulation.getWeather().getTemperature();
                                dynamicFrostDepth = sTemp < 0.0f ? Math.min(2.5f, 0.4f + Math.abs(sTemp) * 0.10f) : 0.5f;
                            }

                            boolean isNodeFrozen = isWinter && (n1.z() >= -dynamicFrostDepth || n1.type() == TunnelNetwork.ChamberType.ENTRANCE);
                            Color tunnelCol = isNodeFrozen ? Color.web("#e0f2fe", 0.85) : Color.web("#f59e0b", 0.70);

                            gc3D.setStroke(tunnelCol);
                            gc3D.setLineWidth(Math.max(2.2, 4.0 * zSc * (tn.getTunnelWidthSetting() / 2.0)));

                            int tDepth = Math.max(1, activeSimulation.getTerrarium().getDepth());
                            List<float[]> pts = edge.pathPoints();
                            if (pts != null && pts.size() >= 2) {
                                for (int pi = 0; pi < pts.size() - 1; pi++) {
                                    float[] ptA = pts.get(pi);
                                    float[] ptB = pts.get(pi + 1);
                                    double gaX = (ptA[0] / (double) tWidth) * gridSize;
                                    double gaY = (ptA[1] / (double) tHeight) * gridSize;
                                    int igaX = Math.max(0, Math.min(gridSize - 1, (int) gaX));
                                    int igaY = Math.max(0, Math.min(gridSize - 1, (int) gaY));
                                    float sElevA = activeSimulation.getTerrarium().getSurfaceElevation(ptA[0], ptA[1]);
                                    double gaZ = heightGrid[igaX][igaY] * 40.0 + ((ptA[2] - sElevA) / (double) tDepth) * 40.0;

                                    double gbX = (ptB[0] / (double) tWidth) * gridSize;
                                    double gbY = (ptB[1] / (double) tHeight) * gridSize;
                                    int igbX = Math.max(0, Math.min(gridSize - 1, (int) gbX));
                                    int igbY = Math.max(0, Math.min(gridSize - 1, (int) gbY));
                                    float sElevB = activeSimulation.getTerrarium().getSurfaceElevation(ptB[0], ptB[1]);
                                    double gbZ = heightGrid[igbX][igbY] * 40.0 + ((ptB[2] - sElevB) / (double) tDepth) * 40.0;

                                    double[] pa = pane.project3DPoint(gaX, gaY, gaZ, cx, cy, scale, radAz, radEl);
                                    double[] pb = pane.project3DPoint(gbX, gbY, gbZ, cx, cy, scale, radAz, radEl);
                                    gc3D.strokeLine(pa[0], pa[1], pb[0], pb[1]);
                                }
                            } else {
                                double g1x = (n1.x() / (double) tWidth) * gridSize;
                                double g1y = (n1.y() / (double) tHeight) * gridSize;
                                int ig1x = Math.max(0, Math.min(gridSize - 1, (int) g1x));
                                int ig1y = Math.max(0, Math.min(gridSize - 1, (int) g1y));
                                float sElev1 = activeSimulation.getTerrarium().getSurfaceElevation(n1.x(), n1.y());
                                double g1z = heightGrid[ig1x][ig1y] * 40.0 + ((n1.z() - sElev1) / (double) tDepth) * 40.0;

                                double g2x = (n2.x() / (double) tWidth) * gridSize;
                                double g2y = (n2.y() / (double) tHeight) * gridSize;
                                int ig2x = Math.max(0, Math.min(gridSize - 1, (int) g2x));
                                int ig2y = Math.max(0, Math.min(gridSize - 1, (int) g2y));
                                float sElev2 = activeSimulation.getTerrarium().getSurfaceElevation(n2.x(), n2.y());
                                double g2z = heightGrid[ig2x][ig2y] * 40.0 + ((n2.z() - sElev2) / (double) tDepth) * 40.0;

                                double[] p1 = pane.project3DPoint(g1x, g1y, g1z, cx, cy, scale, radAz, radEl);
                                double[] p2 = pane.project3DPoint(g2x, g2y, g2z, cx, cy, scale, radAz, radEl);
                                gc3D.strokeLine(p1[0], p1[1], p2[0], p2[1]);
                            }
                        }

                        // 2. Draw chamber nodes with biological lenticular geometry and color palette
                        int tDepth = Math.max(1, activeSimulation.getTerrarium().getDepth());
                        for (TunnelNetwork.TunnelNode node : tn.getNodes()) {
                            double gx = (node.x() / (double) tWidth) * gridSize;
                            double gy = (node.y() / (double) tHeight) * gridSize;
                            int igx = Math.max(0, Math.min(gridSize - 1, (int) gx));
                            int igy = Math.max(0, Math.min(gridSize - 1, (int) gy));
                            float sElev = activeSimulation.getTerrarium().getSurfaceElevation(node.x(), node.y());
                            double gz = heightGrid[igx][igy] * 40.0 + ((node.z() - sElev) / (double) tDepth) * 40.0;

                            double[] pNode = pane.project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);

                            double rxChamber = Math.max(3.0, Math.min(22.0, (Math.max(0.04, (double) node.radiusX()) / (double) tWidth) * gridSize * (scale * 0.45)));
                            double rzChamber = Math.max(2.0, Math.min(15.0, (Math.max(0.025, (double) node.radiusZ()) / (double) tDepth) * 40.0 * (scale * 0.35)));

                            Color fillCol = switch (node.type()) {
                                case QUEEN_CHAMBER, BIVOUAC_CORE -> Color.web("#d946ef", 0.80);
                                case BROOD_CHAMBER, SOLARIUM, GALL_NURSERY -> Color.web("#f8fafc", 0.80);
                                case FOOD_STORAGE, HONEYCOMB, POLLEN_POT, LEAF_CACHE -> Color.web("#22c55e", 0.75);
                                case FUNGUS_GARDEN -> Color.web("#a855f7", 0.80);
                                case HIBERNATION -> Color.web("#38bdf8", 0.75);
                                case WASTE_DUMP -> Color.web("#eab308", 0.70);
                                case ENTRANCE, VENTILATION_CHIMNEY -> Color.web("#18181b", 0.90);
                                case TUNNEL -> Color.web("#f59e0b", 0.60);
                                default -> Color.web("#f59e0b", 0.60);
                            };
                            Color strokeCol = fillCol.brighter();

                            gc3D.setFill(fillCol);
                            gc3D.fillOval(pNode[0] - rxChamber, pNode[1] - rzChamber, rxChamber * 2, rzChamber * 2);
                            gc3D.setStroke(strokeCol);
                            gc3D.setLineWidth(1.5);
                            gc3D.strokeOval(pNode[0] - rxChamber, pNode[1] - rzChamber, rxChamber * 2, rzChamber * 2);
                        }
                    }
                }

                if (!renderedActualNetwork) {
                    String nestType = "Subterranean";
                    if (!adapt.isEmpty()) {
                        if (adapt.contains("termit") || adapt.contains("macrotermes")) nestType = "TermiteCathedral";
                        else if (adapt.contains("vespula") || adapt.contains("polistes") || adapt.contains("guêpe")) nestType = "PaperWaspSuspended";
                        else if (adapt.contains("apis") || adapt.contains("abeille")) nestType = "HoneybeeTreeComb";
                        else if (adapt.contains("formica") || adapt.contains("rufous")) nestType = "ThatchMound";
                        else if (adapt.contains("atta") || adapt.contains("coupeuse")) nestType = "LeafcutterVault";
                        else if (adapt.contains("cremato")) nestType = "ArborealCarton";
                        else if (adapt.contains("eciton")) nestType = "LivingBivouac";
                        else if (adapt.contains("oeco")) nestType = "WeaverLeaf";
                    }

                    boolean isThatch = "ThatchMound".equals(nestType);
                    boolean isCathedral = "TermiteCathedral".equals(nestType);
                    boolean isPaper = "PaperWaspSuspended".equals(nestType);
                    boolean isBees = "HoneybeeTreeComb".equals(nestType);
                    boolean isCarton = "ArborealCarton".equals(nestType);
                    boolean isBivouac = "LivingBivouac".equals(nestType);

                    Color tunnelCol = isWinter ? Color.web("#e0f2fe", 0.85) : Color.web("#f59e0b", 0.70);
                    gc3D.setStroke(tunnelCol);
                    gc3D.setLineWidth(Math.max(2.2, 4.0 * zSc));

                    if (isThatch) {
                        // Formica rufa Solar Thatch Mound dual-zone architecture
                        double hubZ = baseZ - 4.0;
                        double[] pHub = pane.project3DPoint(nestX, nestY, hubZ, cx, cy, scale, radAz, radEl);
                        double[] pApex = pane.project3DPoint(nestX, nestY, baseZ + 14.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pApex[0], pApex[1], pHub[0], pHub[1]);
                        gc3D.strokeLine(pE1[0], pE1[1], pHub[0], pHub[1]);
                        gc3D.strokeLine(pE2[0], pE2[1], pHub[0], pHub[1]);

                        // Above-Ground Solarium Incubation Chamber 1
                        double[] pSolar1 = pane.project3DPoint(nestX + 3, nestY + 1, baseZ + 8.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pSolar1[0], pSolar1[1]);
                        gc3D.setFill(Color.web("#f8fafc", 0.85));
                        gc3D.fillOval(pSolar1[0] - rBrood * 1.1, pSolar1[1] - rBrood * 0.8, rBrood * 2.2, rBrood * 1.6);
                        gc3D.setStroke(Color.web("#ffffff", 0.95)); gc3D.setLineWidth(1.5);
                        gc3D.strokeOval(pSolar1[0] - rBrood * 1.1, pSolar1[1] - rBrood * 0.8, rBrood * 2.2, rBrood * 1.6);

                        // Above-Ground Solarium Incubation Chamber 2
                        double[] pSolar2 = pane.project3DPoint(nestX - 3, nestY - 2, baseZ + 5.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pSolar2[0], pSolar2[1]);
                        gc3D.setFill(Color.web("#f8fafc", 0.80));
                        gc3D.fillOval(pSolar2[0] - rBrood, pSolar2[1] - rBrood * 0.7, rBrood * 2.0, rBrood * 1.4);
                        gc3D.strokeOval(pSolar2[0] - rBrood, pSolar2[1] - rBrood * 0.7, rBrood * 2.0, rBrood * 1.4);

                        // Deep Subterranean Queen Crypt
                        double[] pQueen = pane.project3DPoint(nestX + 4, nestY + 2, baseZ - 24.0, cx, cy, scale, radAz, radEl);
                        gc3D.setStroke(tunnelCol); gc3D.strokeLine(pHub[0], pHub[1], pQueen[0], pQueen[1]);
                        gc3D.setFill(Color.web("#d946ef", 0.70));
                        gc3D.fillOval(pQueen[0] - rQueen, pQueen[1] - rQueen * 0.7, rQueen * 2, rQueen * 1.4);
                        gc3D.setStroke(Color.web("#f0abfc", 0.95)); gc3D.setLineWidth(1.8);
                        gc3D.strokeOval(pQueen[0] - rQueen, pQueen[1] - rQueen * 0.7, rQueen * 2, rQueen * 1.4);

                        // Deep Winter Hibernation Vault
                        double[] pHibern = pane.project3DPoint(nestX - 4, nestY - 4, baseZ - 20.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pHibern[0], pHibern[1]);
                        Color hCol = isWinter ? Color.web("#06b6d4", 0.85) : Color.web("#0284c7", 0.60);
                        gc3D.setFill(hCol);
                        gc3D.fillOval(pHibern[0] - rHibern, pHibern[1] - rHibern * 0.7, rHibern * 2, rHibern * 1.4);
                        gc3D.setStroke(Color.web("#38bdf8", 0.90)); gc3D.setLineWidth(1.4);
                        gc3D.strokeOval(pHibern[0] - rHibern, pHibern[1] - rHibern * 0.7, rHibern * 2, rHibern * 1.4);

                        // Aphid Honey Storage
                        double[] pAphid = pane.project3DPoint(nestX + 2, nestY - 5, baseZ - 10.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pAphid[0], pAphid[1]);
                        gc3D.setFill(Color.web("#ec4899", 0.65));
                        gc3D.fillOval(pAphid[0] - rAphid, pAphid[1] - rAphid * 0.7, rAphid * 2, rAphid * 1.4);
                        gc3D.setStroke(Color.web("#f472b6", 0.90)); gc3D.setLineWidth(1.3);
                        gc3D.strokeOval(pAphid[0] - rAphid, pAphid[1] - rAphid * 0.7, rAphid * 2, rAphid * 1.4);

                    } else if (isCathedral) {
                        // Macrotermes Cathedral Spire architecture
                        double hubZ = baseZ - 2.0;
                        double[] pHub = pane.project3DPoint(nestX, nestY, hubZ, cx, cy, scale, radAz, radEl);
                        double[] pSpireTop = pane.project3DPoint(nestX, nestY, baseZ + 25.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pSpireTop[0], pSpireTop[1], pHub[0], pHub[1]);

                        // Spire brood nurseries
                        double[] pSpireNursery = pane.project3DPoint(nestX + 2, nestY, baseZ + 12.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pSpireNursery[0], pSpireNursery[1]);
                        gc3D.setFill(Color.web("#f8fafc", 0.75));
                        gc3D.fillOval(pSpireNursery[0] - rBrood, pSpireNursery[1] - rBrood * 0.7, rBrood * 2, rBrood * 1.4);

                        // Subterranean Fungus Garden
                        double[] pFungus = pane.project3DPoint(nestX - 5, nestY + 3, baseZ - 12.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pFungus[0], pFungus[1]);
                        gc3D.setFill(Color.web("#a855f7", 0.70));
                        gc3D.fillOval(pFungus[0] - rFungus, pFungus[1] - rFungus * 0.7, rFungus * 2, rFungus * 1.4);
                        gc3D.setStroke(Color.web("#c084fc", 0.95)); gc3D.setLineWidth(1.6);
                        gc3D.strokeOval(pFungus[0] - rFungus, pFungus[1] - rFungus * 0.7, rFungus * 2, rFungus * 1.4);

                        // Deep Royal Cell
                        double[] pQueen = pane.project3DPoint(nestX, nestY, baseZ - 22.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pQueen[0], pQueen[1]);
                        gc3D.setFill(Color.web("#d946ef", 0.75));
                        gc3D.fillOval(pQueen[0] - rQueen * 1.2, pQueen[1] - rQueen * 0.8, rQueen * 2.4, rQueen * 1.6);
                        gc3D.setStroke(Color.web("#f0abfc", 0.95)); gc3D.setLineWidth(1.8);
                        gc3D.strokeOval(pQueen[0] - rQueen * 1.2, pQueen[1] - rQueen * 0.8, rQueen * 2.4, rQueen * 1.6);

                    } else if (isPaper || isBees || isCarton || isBivouac) {
                        // Arboreal / Aerial Multi-tier architecture
                        double anchorZ = baseZ + 20.0;
                        double[] pAnchor = pane.project3DPoint(nestX, nestY, anchorZ, cx, cy, scale, radAz, radEl);

                        for (int t = 0; t < 3; t++) {
                            double zTier = baseZ + 16.0 - t * 5.0;
                            double[] pTier = pane.project3DPoint(nestX + (t % 2 == 0 ? 3 : -3), nestY, zTier, cx, cy, scale, radAz, radEl);
                            gc3D.strokeLine(pAnchor[0], pAnchor[1], pTier[0], pTier[1]);
                            Color col = t == 0 ? Color.web("#d946ef", 0.75) : (t == 1 ? Color.web("#f8fafc", 0.75) : Color.web("#22c55e", 0.70));
                            gc3D.setFill(col);
                            gc3D.fillOval(pTier[0] - rBrood, pTier[1] - rBrood * 0.7, rBrood * 2, rBrood * 1.4);
                            gc3D.setStroke(col.brighter()); gc3D.setLineWidth(1.4);
                            gc3D.strokeOval(pTier[0] - rBrood, pTier[1] - rBrood * 0.7, rBrood * 2, rBrood * 1.4);
                        }

                    } else {
                        // Standard Subterranean Ant Burrow (preview mode)
                        double hubZ = baseZ - 12.0;
                        double[] pHub = pane.project3DPoint(nestX, nestY, hubZ, cx, cy, scale, radAz, radEl);

                        gc3D.strokeLine(pE1[0], pE1[1], pHub[0], pHub[1]);
                        gc3D.strokeLine(pE2[0], pE2[1], pHub[0], pHub[1]);
                        gc3D.strokeLine(pE3[0], pE3[1], pHub[0], pHub[1]);

                        // 1. Loge Royale
                        double[] pQueen = pane.project3DPoint(nestX + 5, nestY + 2, baseZ - 24.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pQueen[0], pQueen[1]);
                        gc3D.setFill(Color.web("#d946ef", 0.60));
                        gc3D.fillOval(pQueen[0] - rQueen, pQueen[1] - rQueen, rQueen * 2, rQueen * 2);
                        gc3D.setStroke(Color.web("#f0abfc", 0.95)); gc3D.setLineWidth(1.8);
                        gc3D.strokeOval(pQueen[0] - rQueen, pQueen[1] - rQueen, rQueen * 2, rQueen * 2);

                        // 2. Chambre du Couvain
                        double[] pBrood = pane.project3DPoint(nestX - 5, nestY + 3, baseZ - 16.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pBrood[0], pBrood[1]);
                        gc3D.setFill(Color.web("#f8fafc", 0.60));
                        gc3D.fillOval(pBrood[0] - rBrood, pBrood[1] - rBrood, rBrood * 2, rBrood * 2);
                        gc3D.setStroke(Color.web("#ffffff", 0.95)); gc3D.setLineWidth(1.4);
                        gc3D.strokeOval(pBrood[0] - rBrood, pBrood[1] - rBrood, rBrood * 2, rBrood * 2);

                        // 3. Champignonnière / Grenier
                        double[] pFungus = pane.project3DPoint(nestX + 6, nestY - 3, baseZ - 18.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pFungus[0], pFungus[1]);
                        if (isLeafcutterOrTermite) {
                            gc3D.setFill(Color.web("#a855f7", 0.65));
                            gc3D.fillOval(pFungus[0] - rFungus, pFungus[1] - rFungus, rFungus * 2, rFungus * 2);
                            gc3D.setStroke(Color.web("#c084fc", 0.95)); gc3D.setLineWidth(1.6);
                            gc3D.strokeOval(pFungus[0] - rFungus, pFungus[1] - rFungus, rFungus * 2, rFungus * 2);
                        } else {
                            gc3D.setFill(Color.web("#22c55e", 0.60));
                            gc3D.fillOval(pFungus[0] - rFood, pFungus[1] - rFood, rFood * 2, rFood * 2);
                            gc3D.setStroke(Color.web("#4ade80", 0.95)); gc3D.setLineWidth(1.4);
                            gc3D.strokeOval(pFungus[0] - rFood, pFungus[1] - rFood, rFood * 2, rFood * 2);
                        }

                        // 4. Laiterie à Pucerons
                        double[] pAphid = pane.project3DPoint(nestX + 2, nestY - 7, baseZ - 11.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pAphid[0], pAphid[1]);
                        gc3D.setFill(Color.web("#ec4899", 0.60));
                        gc3D.fillOval(pAphid[0] - rAphid, pAphid[1] - rAphid, rAphid * 2, rAphid * 2);
                        gc3D.setStroke(Color.web("#f472b6", 0.90)); gc3D.setLineWidth(1.3);
                        gc3D.strokeOval(pAphid[0] - rAphid, pAphid[1] - rAphid, rAphid * 2, rAphid * 2);

                        // 5. Loge d'Hibernation
                        double[] pHibern = pane.project3DPoint(nestX - 4, nestY - 6, baseZ - 20.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pHibern[0], pHibern[1]);
                        Color hCol = isWinter ? Color.web("#06b6d4", 0.75) : Color.web("#0284c7", 0.50);
                        gc3D.setFill(hCol);
                        gc3D.fillOval(pHibern[0] - rHibern, pHibern[1] - rHibern, rHibern * 2, rHibern * 2);
                        gc3D.setStroke(Color.web("#38bdf8", 0.90)); gc3D.setLineWidth(1.3);
                        gc3D.strokeOval(pHibern[0] - rHibern, pHibern[1] - rHibern, rHibern * 2, rHibern * 2);

                        // 6. Dépotoir
                        double[] pTrash = pane.project3DPoint(nestX - 8, nestY - 1, baseZ - 13.0, cx, cy, scale, radAz, radEl);
                        gc3D.strokeLine(pHub[0], pHub[1], pTrash[0], pTrash[1]);
                        gc3D.setFill(Color.web("#eab308", 0.55));
                        gc3D.fillOval(pTrash[0] - rTrash, pTrash[1] - rTrash, rTrash * 2, rTrash * 2);
                        gc3D.setStroke(Color.web("#fef08a", 0.85)); gc3D.setLineWidth(1.2);
                        gc3D.strokeOval(pTrash[0] - rTrash, pTrash[1] - rTrash, rTrash * 2, rTrash * 2);
                    }
                }

                // Hydrological Flooding Effect in lower chambers (Projected in 3D)
                if (isFlooded) {
                    double[] pFlood = pane.project3DPoint(nestX, nestY, baseZ - 20.0, cx, cy, scale, radAz, radEl);
                    gc3D.setFill(Color.web("#0284c7", 0.45));
                    gc3D.fillOval(pFlood[0] - rQueen * 1.1, pFlood[1] - rQueen * 0.4, rQueen * 2.2, rQueen * 0.8);
                }
            }
        }
    }

    public void drawGalleriesLegendHUD(WorldEditorPane pane, GraphicsContext gc3D, double w, double h) {
        double boxW = 290.0;
        double boxH = 240.0;
        double hx = w - boxW - 15.0;
        double hy = 15.0;

        // HUD Glassmorphism Card
        gc3D.setFill(Color.web("rgba(15, 23, 42, 0.94)"));
        gc3D.fillRoundRect(hx, hy, boxW, boxH, 10, 10);
        gc3D.setStroke(Color.web("#38bdf8", 0.85));
        gc3D.setLineWidth(1.2);
        gc3D.strokeRoundRect(hx, hy, boxW, boxH, 10, 10);

        // Header Title
        gc3D.setFill(Color.web("#38bdf8"));
        gc3D.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
        gc3D.fillText("🏰 NEST & MULTI-SPECIES CHAMBERS LEGEND", hx + 12, hy + 20);

        gc3D.setStroke(Color.web("rgba(56, 189, 248, 0.3)"));
        gc3D.setLineWidth(1.0);
        gc3D.strokeLine(hx + 10, hy + 26, hx + boxW - 10, hy + 26);

        gc3D.setFont(Font.font("System", 10));

        // 1. Loge Royale
        gc3D.setFill(Color.web("#d946ef")); gc3D.fillOval(hx + 14, hy + 35, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.legend.royal"), hx + 32, hy + 44);

        // 2. Chambre du Couvain
        gc3D.setFill(Color.web("#f8fafc")); gc3D.fillOval(hx + 14, hy + 53, 10, 10);
        gc3D.setStroke(Color.web("#94a3b8")); gc3D.strokeOval(hx + 14, hy + 53, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.chambers.brood"), hx + 32, hy + 62);

        // 3. Champignonnière (Atta / Macrotermes)
        gc3D.setFill(Color.web("#a855f7")); gc3D.fillOval(hx + 14, hy + 71, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.chambers.fungus"), hx + 32, hy + 80);

        // 4. Laiterie à Pucerons / Élevage
        gc3D.setFill(Color.web("#ec4899")); gc3D.fillOval(hx + 14, hy + 89, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.chambers.food"), hx + 32, hy + 98);

        // 5. Grenier & Stockage
        gc3D.setFill(Color.web("#22c55e")); gc3D.fillOval(hx + 14, hy + 107, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.legend.storage"), hx + 32, hy + 116);

        // 6. Loge d'Hibernation / Diapause
        gc3D.setFill(Color.web("#0284c7")); gc3D.fillOval(hx + 14, hy + 125, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        String simSeason = pane.getSimSeason();
        String hibState = (simSeason != null && simSeason.toLowerCase().contains("hiver")) ? " (" + I18nManager.getInstance().get("nest.legend.active") + ")" : "";
        gc3D.fillText(I18nManager.getInstance().get("nest.legend.hibernation") + hibState, hx + 32, hy + 134);

        // 7. Dépotoir / Déchets
        gc3D.setFill(Color.web("#eab308")); gc3D.fillOval(hx + 14, hy + 143, 10, 10);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.chambers.waste"), hx + 32, hy + 152);

        // 8. Galeries & Puits Voxel Excavés
        gc3D.setStroke(Color.web("#f59e0b")); gc3D.setLineWidth(2.5);
        gc3D.strokeLine(hx + 14, hy + 166, hx + 24, hy + 166);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.legend.tunnel"), hx + 32, hy + 170);

        // 9. Tunnels sous la Neige / Glace
        gc3D.setStroke(Color.web("#e0f2fe")); gc3D.setLineWidth(2.5);
        gc3D.strokeLine(hx + 14, hy + 184, hx + 24, hy + 184);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.legend.snow_tunnels"), hx + 32, hy + 188);

        // 10. Déblais de Terre & Exits
        gc3D.setFill(Color.web("#9a3412")); gc3D.fillOval(hx + 14, hy + 199, 10, 6);
        gc3D.setFill(Color.web("#18181b")); gc3D.fillOval(hx + 17, hy + 200, 4, 3);
        gc3D.setFill(Color.web("#f8fafc"));
        gc3D.fillText(I18nManager.getInstance().get("nest.chambers.entrance"), hx + 32, hy + 207);

        // Live Dynamic Status Note
        gc3D.setFill(Color.web("#4ade80"));
        gc3D.setFont(Font.font("System", 9));
        gc3D.fillText(I18nManager.getInstance().get("nest.legend.matrix"), hx + 12, hy + 230);
    }

}