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
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.simulation.Simulation;

import java.util.Random;

/**
 * 3D Epigeic Surface Superstructures & Nest Ontogenetic Growth Renderer for the World Editor Studio.
 * Handles ontogenetic surface superstructures across species archetypes (Termite Cathedral spires,
 * Paper wasp suspended envelopes, Honeybee tree combs, Bumblebee wax pots, Formica thatch mounds,
 * Leafcutter vaults, Arboreal carton, Eciton living bivouacs, Weaver leaf pavilions, and Carpenter wood galleries).
 */
public class WorldEpigeicNestStructure3DRenderer {
    public void drawRealistic3DNestStructure(WorldEditorPane pane, GraphicsContext gc3D,
                                            double cx, double cy, double scale, double radAz, double radEl) {
        Simulation activeSimulation = pane.getActiveSimulation();
        int gridSize = WorldEditorPane.GRID_SIZE;

        if (activeSimulation != null && !activeSimulation.getColonies().isEmpty()) {
            int tWidth = Math.max(1, activeSimulation.getTerrarium().getWidth());
            int tHeight = Math.max(1, activeSimulation.getTerrarium().getHeight());
            for (Colony colony : activeSimulation.getColonies()) {
                double egx = ((colony.getNestX() + 0.5) / (double) tWidth) * gridSize;
                double egy = ((colony.getNestY() + 0.5) / (double) tHeight) * gridSize;
                int nx = (int) Math.max(0, Math.min(gridSize - 1, egx));
                int ny = (int) Math.max(0, Math.min(gridSize - 1, egy));

                int livingPop = colony.getLivingIndividuals().size();
                float buildingMaterials = colony.getResourceAmount(ResourceType.MULCH)
                        + colony.getResourceAmount(ResourceType.WOOD)
                        + colony.getResourceAmount(ResourceType.WOOD_CELLULOSE)
                        + colony.getResourceAmount(ResourceType.DIRT)
                        + colony.getResourceAmount(ResourceType.LEAF);

                // Exact biological ontogenetic growth:
                // Stage 0 (Founding Queen alone, 0 workers, 0 materials): matScale = 0.0 (starts from zero!)
                // Stage 1 (Incipient colony: 2-20 workers): matScale = 0.04 - 0.25
                // Stage 2 (Developing colony: 21-100 workers): matScale = 0.25 - 0.70
                // Stage 3 (Mature & Climax supercolony: >100 workers): matScale = 0.70 - 3.2
                double matScale;
                if (livingPop <= 1 && buildingMaterials < 5.0f) {
                    matScale = 0.0;
                } else if (livingPop <= 20) {
                    matScale = 0.04 + (livingPop / 20.0) * 0.20 + Math.min(0.15, buildingMaterials / 100.0);
                } else if (livingPop <= 100) {
                    matScale = 0.25 + ((livingPop - 20) / 80.0) * 0.45 + Math.min(0.30, buildingMaterials / 120.0);
                } else {
                    matScale = 0.70 + Math.min(2.5, Math.sqrt((livingPop - 100) / 300.0) + Math.min(1.5, buildingMaterials / 80.0));
                }

                drawSingleRealistic3DNestStructure(pane, gc3D, cx, cy, scale, radAz, radEl, nx, ny, matScale, colony);
            }
        } else {
            drawSingleRealistic3DNestStructure(pane, gc3D, cx, cy, scale, radAz, radEl, gridSize / 2, gridSize / 2, 1.0, null);
        }
    }

    public void drawSingleRealistic3DNestStructure(WorldEditorPane pane, GraphicsContext gc3D,
                                                  double cx, double cy, double scale, double radAz, double radEl,
                                                  int nestX, int nestY, double maturityScale, Colony colony) {
        double surfaceZ = pane.getHeightGrid()[nestX][nestY] * 40.0;
        double[] surfaceP = pane.project3DPoint(nestX, nestY, surfaceZ, cx, cy, scale, radAz, radEl);

        String nestType = "Subterranean";
        if (colony != null && colony.getSpecies() != null && colony.getSpecies().getNestType() != null) {
            String arch = colony.getSpecies().getNestType().toUpperCase();
            if (arch.contains("CATHEDRAL") || arch.contains("TERMITE")) nestType = "TermiteCathedral";
            else if (arch.contains("PAPER") || arch.contains("PEDUNCULATE") || arch.contains("VESPA")) nestType = "PaperWaspSuspended";
            else if (arch.contains("WAX_COMB") || arch.contains("BEEHIVE") || arch.contains("HEXAGONAL")) nestType = "HoneybeeTreeComb";
            else if (arch.contains("WAX_POTS") || arch.contains("POTS_CLUSTER") || arch.contains("BOMBUS")) nestType = "WaxPotsCluster";
            else if (arch.contains("MOUND") || arch.contains("SURFACE_MOUND") || arch.contains("SOLAR") || arch.contains("RUFA")) nestType = "ThatchMound";
            else if (arch.contains("FUNGI") || arch.contains("VAULT") || arch.contains("ATTA")) nestType = "LeafcutterVault";
            else if (arch.contains("CARTON")) nestType = "ArborealCarton";
            else if (arch.contains("BIVOUAC") || arch.contains("ARMY") || arch.contains("ECITON")) nestType = "LivingBivouac";
            else if (arch.contains("SILK") || arch.contains("LEAF") || arch.contains("OECOPHYLLA")) nestType = "WeaverLeaf";
            else if (arch.contains("WOOD") || arch.contains("TREE") || arch.contains("TRUNK") || arch.contains("HOLLOW_TRUNK")) nestType = "WoodGallery";
            else if (arch.contains("BAMBOO") || arch.contains("STEM") || arch.contains("GALL")) nestType = "BambooStem";
            else if (arch.contains("CRATER") || arch.contains("COMPOSITE") || arch.contains("POGONOMYRMEX")) nestType = "CompositeCraterMound";
            else if (arch.contains("SUPERCOLONY") || arch.contains("SUPERCOLONIE")) nestType = "SupercolonyNetwork";
        } else {
            String s = pane.getSpeciesAdaptationValue().toLowerCase();
            if (s.contains("termit") || s.contains("macrotermes")) nestType = "TermiteCathedral";
            else if (s.contains("vespula") || s.contains("polistes") || s.contains("guêpe") || s.contains("vespa")) nestType = "PaperWaspSuspended";
            else if (s.contains("apis") || s.contains("abeille")) nestType = "HoneybeeTreeComb";
            else if (s.contains("bombus") || s.contains("bourdon")) nestType = "WaxPotsCluster";
            else if (s.contains("formica") || s.contains("rufous") || s.contains("mound")) nestType = "ThatchMound";
            else if (s.contains("atta") || s.contains("coupeuse")) nestType = "LeafcutterVault";
            else if (s.contains("cremato")) nestType = "ArborealCarton";
            else if (s.contains("eciton") || s.contains("legion")) nestType = "LivingBivouac";
            else if (s.contains("oeco") || s.contains("tisserande")) nestType = "WeaverLeaf";
            else if (s.contains("camponotus") || s.contains("charpentière") || s.contains("wood")) nestType = "WoodGallery";
            else if (s.contains("bamboo") || s.contains("colobopsis") || s.contains("temnothorax")) nestType = "BambooStem";
            else if (s.contains("pogono") || s.contains("messor") || s.contains("crater")) nestType = "CompositeCraterMound";
            else if (s.contains("supercolony") || s.contains("linepithema")) nestType = "SupercolonyNetwork";
        }

        double zScale = pane.getZoom() / 7.5;
        boolean isFoundingStage = (maturityScale < 0.06);

        switch (nestType) {
            case "TermiteCathedral" -> {
                if (isFoundingStage) {
                    // Founding copula mud pellet (Stage 0: founding royal pair)
                    gc3D.setFill(Color.web("#9a3412"));
                    gc3D.fillOval(surfaceP[0] - 2.5 * zScale, surfaceP[1] - 2.0 * zScale, 5.0 * zScale, 4.0 * zScale);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 1.0 * zScale, surfaceP[1] - 1.0 * zScale, 2.0 * zScale, 2.0 * zScale);
                } else {
                    // Render Sculpted Terracotta Termite Cathedral Spire Mound (Macrotermes)
                    double moundH = Math.max(6.0 * zScale, 38.0 * maturityScale * zScale);
                    double moundW = Math.max(4.0 * zScale, 20.0 * maturityScale * zScale);

                    // Central cathedral spire with shadow & highlight facets
                    gc3D.setFill(Color.web("#9a3412"));
                    gc3D.fillPolygon(
                        new double[]{surfaceP[0] - moundW * 0.5, surfaceP[0], surfaceP[0] + moundW * 0.5},
                        new double[]{surfaceP[1], surfaceP[1] - moundH, surfaceP[1]}, 3
                    );
                    gc3D.setFill(Color.web("#7c2d12"));
                    gc3D.fillPolygon(
                        new double[]{surfaceP[0], surfaceP[0] + moundW * 0.5, surfaceP[0] + moundW * 0.2},
                        new double[]{surfaceP[1] - moundH, surfaceP[1], surfaceP[1]}, 3
                    );

                    // Secondary buttress side spires (only when developing / mature)
                    if (maturityScale > 0.30) {
                        gc3D.setFill(Color.web("#b45309"));
                        gc3D.fillPolygon(
                            new double[]{surfaceP[0] - moundW * 0.8, surfaceP[0] - moundW * 0.2, surfaceP[0]},
                            new double[]{surfaceP[1], surfaceP[1] - moundH * 0.65, surfaceP[1]}, 3
                        );
                        gc3D.fillPolygon(
                            new double[]{surfaceP[0] + moundW * 0.1, surfaceP[0] + moundW * 0.7, surfaceP[0] + moundW * 0.9},
                            new double[]{surfaceP[1], surfaceP[1] - moundH * 0.55, surfaceP[1]}, 3
                        );
                    }

                    // Fluted ridges
                    gc3D.setStroke(Color.web("#c2410c", 0.75));
                    gc3D.setLineWidth(1.2 * zScale);
                    gc3D.strokeLine(surfaceP[0] - moundW * 0.2, surfaceP[1], surfaceP[0], surfaceP[1] - moundH * 0.85);
                    gc3D.strokeLine(surfaceP[0] + moundW * 0.2, surfaceP[1], surfaceP[0], surfaceP[1] - moundH * 0.85);

                    // Chimney ventilation vents
                    gc3D.setFill(Color.web("#292524"));
                    gc3D.fillOval(surfaceP[0] - 2.5 * zScale, surfaceP[1] - moundH + 4 * zScale, 5.0 * zScale, 3.5 * zScale);
                    if (maturityScale > 0.4) {
                        gc3D.fillOval(surfaceP[0] - moundW * 0.35, surfaceP[1] - moundH * 0.6, 3 * zScale, 2 * zScale);
                        gc3D.fillOval(surfaceP[0] + moundW * 0.35, surfaceP[1] - moundH * 0.5, 3 * zScale, 2 * zScale);
                    }
                }
            }
            case "PaperWaspSuspended" -> {
                double branchY = surfaceP[1] - (isFoundingStage ? 25.0 : 45.0) * zScale;
                double branchLen = (isFoundingStage ? 25.0 : 50.0) * zScale;

                // Tree branch
                gc3D.setStroke(Color.web("#78350f"));
                gc3D.setLineWidth((isFoundingStage ? 3.0 : 4.5) * zScale);
                gc3D.strokeLine(surfaceP[0] - branchLen * 0.6, branchY - 3 * zScale, surfaceP[0] + branchLen * 0.6, branchY + 3 * zScale);

                if (isFoundingStage) {
                    // Solitary pedicel with 1 embryonic founding hexagon cell (Stage 0: Foundress queen)
                    gc3D.setStroke(Color.web("#451a03"));
                    gc3D.setLineWidth(1.5 * zScale);
                    gc3D.strokeLine(surfaceP[0], branchY, surfaceP[0], branchY + 6 * zScale);
                    gc3D.setFill(Color.web("#a1a1aa"));
                    gc3D.fillOval(surfaceP[0] - 2.5 * zScale, branchY + 6 * zScale, 5.0 * zScale, 4.0 * zScale);
                } else {
                    // Green leaf foliage canopy above branch
                    gc3D.setFill(Color.web("#15803d", 0.85));
                    gc3D.fillOval(surfaceP[0] - branchLen * 0.7, branchY - 20 * zScale, branchLen * 1.4, 25 * zScale);
                    gc3D.setFill(Color.web("#22c55e", 0.70));
                    gc3D.fillOval(surfaceP[0] - branchLen * 0.4, branchY - 24 * zScale, branchLen * 0.8, 18 * zScale);

                    // Pedicel attachment string
                    gc3D.setStroke(Color.web("#451a03"));
                    gc3D.setLineWidth(1.8 * zScale);
                    gc3D.strokeLine(surfaceP[0], branchY, surfaceP[0], branchY + 8 * zScale);

                    // Suspended Hexagonal Comb Paper Nest Envelope
                    double nestR = Math.max(4.0 * zScale, 16.0 * maturityScale * zScale);
                    double nestH = Math.max(5.0 * zScale, 20.0 * maturityScale * zScale);
                    double nestTopY = branchY + 8 * zScale;

                    gc3D.setFill(Color.web("#a1a1aa"));
                    gc3D.fillOval(surfaceP[0] - nestR, nestTopY, nestR * 2, nestH);
                    gc3D.setFill(Color.web("#71717a", 0.40));
                    gc3D.fillOval(surfaceP[0] - nestR * 0.8, nestTopY + nestH * 0.1, nestR * 1.6, nestH * 0.8);
                    gc3D.setStroke(Color.web("#52525b"));
                    gc3D.setLineWidth(1.2 * zScale);
                    gc3D.strokeOval(surfaceP[0] - nestR, nestTopY, nestR * 2, nestH);

                    // Multi-tier paper pulp striations
                    gc3D.setStroke(Color.web("#d4d4d8", 0.60));
                    gc3D.strokeArc(surfaceP[0] - nestR * 0.9, nestTopY + nestH * 0.25, nestR * 1.8, nestH * 0.3, 200, 140, javafx.scene.shape.ArcType.OPEN);
                    gc3D.strokeArc(surfaceP[0] - nestR * 0.85, nestTopY + nestH * 0.55, nestR * 1.7, nestH * 0.3, 200, 140, javafx.scene.shape.ArcType.OPEN);

                    // Bottom entrance aperture hole showing inner comb
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - nestR * 0.35, nestTopY + nestH - 5 * zScale, nestR * 0.7, 5 * zScale);
                    gc3D.setFill(Color.web("#eab308", 0.70));
                    gc3D.fillOval(surfaceP[0] - nestR * 0.2, nestTopY + nestH - 4 * zScale, nestR * 0.4, 2.5 * zScale);
                }
            }
            case "HoneybeeTreeComb" -> {
                double trunkW = Math.max(12.0 * zScale, 22.0 * zScale);
                double trunkH = (isFoundingStage ? 25.0 : 52.0) * zScale;
                double topY = surfaceP[1] - trunkH;

                gc3D.setFill(Color.web("#451a03"));
                gc3D.fillRect(surfaceP[0] - trunkW * 0.6, topY, trunkW * 1.2, trunkH);
                gc3D.setStroke(Color.web("#291205"));
                gc3D.setLineWidth(1.2 * zScale);
                gc3D.strokeLine(surfaceP[0] - trunkW * 0.2, topY, surfaceP[0] - trunkW * 0.2, surfaceP[1]);

                if (isFoundingStage) {
                    // Small founding wax patch inside bark cavity
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 4 * zScale, topY + trunkH * 0.3, 8 * zScale, 10 * zScale);
                    gc3D.setFill(Color.web("#eab308"));
                    gc3D.fillOval(surfaceP[0] - 2 * zScale, topY + trunkH * 0.4, 4 * zScale, 4 * zScale);
                } else {
                    // Main hollow tree entrance cavity
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - trunkW * 0.45, topY + trunkH * 0.25, trunkW * 0.9, trunkH * 0.55);

                    // Multiple cascading golden wax honeycomb discs
                    int combCount = (int) Math.max(1, Math.min(6, 1 + maturityScale * 4));
                    gc3D.setFill(Color.web("#eab308"));
                    for (int c = -combCount / 2; c <= combCount / 2; c++) {
                        gc3D.fillRect(surfaceP[0] + c * 2.5 * zScale - 1 * zScale, topY + trunkH * 0.3, 2.2 * zScale, trunkH * 0.42);
                        gc3D.setFill(Color.web("#fef08a", 0.80));
                        gc3D.fillOval(surfaceP[0] + c * 2.5 * zScale - 0.8 * zScale, topY + trunkH * 0.32, 1.6 * zScale, 3.0 * zScale);
                        gc3D.setFill(Color.web("#eab308"));
                    }
                }
            }
            case "WaxPotsCluster" -> {
                if (isFoundingStage) {
                    // Stage 0: 1 founding wax pot in moss hollow
                    gc3D.setFill(Color.web("#166534"));
                    gc3D.fillOval(surfaceP[0] - 4 * zScale, surfaceP[1] - 3 * zScale, 8 * zScale, 6 * zScale);
                    gc3D.setFill(Color.web("#ca8a04"));
                    gc3D.fillOval(surfaceP[0] - 2 * zScale, surfaceP[1] - 2 * zScale, 4 * zScale, 4 * zScale);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 1 * zScale, surfaceP[1] - 2 * zScale, 2 * zScale, 1.5 * zScale);
                } else {
                    // Moss and dried grass tussock covering
                    double nestR = Math.max(5.0 * zScale, 18.0 * maturityScale * zScale);
                    gc3D.setFill(Color.web("#14532d", 0.90));
                    gc3D.fillOval(surfaceP[0] - nestR, surfaceP[1] - nestR * 0.6, nestR * 2, nestR * 1.2);
                    gc3D.setFill(Color.web("#166534", 0.80));
                    gc3D.fillOval(surfaceP[0] - nestR * 0.8, surfaceP[1] - nestR * 0.5, nestR * 1.6, nestR * 1.0);

                    // Cluster of wax honeypots & pollen urns
                    int potCount = (int) Math.max(3, Math.min(16, 4 + maturityScale * 8));
                    Random r = new Random(88);
                    for (int p = 0; p < potCount; p++) {
                        double px = surfaceP[0] + (r.nextDouble() - 0.5) * nestR * 1.2;
                        double py = surfaceP[1] - nestR * 0.2 + (r.nextDouble() - 0.5) * nestR * 0.5;
                        double pr = (2.5 + r.nextDouble() * 2.5) * zScale;
                        gc3D.setFill(p % 3 == 0 ? Color.web("#eab308") : Color.web("#ca8a04"));
                        gc3D.fillOval(px - pr, py - pr, pr * 2, pr * 2);
                        gc3D.setFill(Color.web("#18181b"));
                        gc3D.fillOval(px - pr * 0.4, py - pr * 0.6, pr * 0.8, pr * 0.6);
                    }
                }
            }
            case "ThatchMound" -> {
                if (isFoundingStage) {
                    // Stage 0: Founding Queen claustral entry (starts from ZERO)
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillOval(surfaceP[0] - 3.5 * zScale, surfaceP[1] - 2.5 * zScale, 7.0 * zScale, 5.0 * zScale);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 1.5 * zScale, surfaceP[1] - 1.0 * zScale, 3.0 * zScale, 2.0 * zScale);
                    gc3D.setStroke(Color.web("#a16207"));
                    gc3D.setLineWidth(1.3 * zScale);
                    gc3D.strokeLine(surfaceP[0] - 2.5 * zScale, surfaceP[1] - 2 * zScale, surfaceP[0] + 3.0 * zScale, surfaceP[1] - 3.5 * zScale);
                } else {
                    // Stages 1 to 3: Growing Volumetric Pine-Needle Thatch Dome (Formica rufa)
                    double domeW = Math.max(5.0 * zScale, 30.0 * maturityScale * zScale);
                    double domeH = Math.max(4.0 * zScale, 24.0 * maturityScale * zScale);

                    // Base mound volume with layered organic stratigraphy
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillOval(surfaceP[0] - domeW, surfaceP[1] - domeH, domeW * 2, domeH * 1.6);
                    gc3D.setFill(Color.web("#582607"));
                    gc3D.fillOval(surfaceP[0] - domeW * 0.9, surfaceP[1] - domeH * 0.95, domeW * 1.8, domeH * 1.4);
                    gc3D.setFill(Color.web("#166534", 0.55));
                    gc3D.fillOval(surfaceP[0] - domeW * 0.8, surfaceP[1] - domeH * 0.85, domeW * 1.6, domeH * 1.25);

                    // Multi-tier pine needles texture scaled with mound size & collected twigs
                    int needleCount = (int) Math.max(6, Math.min(110, 24 * maturityScale));
                    gc3D.setStroke(Color.web("#a16207"));
                    gc3D.setLineWidth(Math.max(1.0, 1.4 * zScale));
                    Random r = new Random(55);
                    for (int k = 0; k < needleCount; k++) {
                        double angle = r.nextDouble() * Math.PI * 2;
                        double radDist = (0.2 + r.nextDouble() * 0.8) * (domeW * 0.9);
                        double dx = radDist * Math.cos(angle);
                        double dy = surfaceP[1] - (1.0 - (radDist / (domeW * 0.9))) * domeH + (r.nextDouble() - 0.5) * 3 * zScale;
                        double stickLen = (3.0 + r.nextDouble() * 5.0) * zScale;
                        double stickAng = r.nextDouble() * Math.PI * 2;
                        gc3D.strokeLine(surfaceP[0] + dx, dy, surfaceP[0] + dx + Math.cos(stickAng) * stickLen, dy + Math.sin(stickAng) * stickLen * 0.5);
                    }

                    // Top central ventilation chimney aperture
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 3.0 * zScale, surfaceP[1] - domeH * 0.95, 6.0 * zScale, 4.0 * zScale);
                }
            }
            case "LeafcutterVault" -> {
                if (isFoundingStage) {
                    gc3D.setFill(Color.web("#9a3412"));
                    gc3D.fillOval(surfaceP[0] - 3 * zScale, surfaceP[1] - 2 * zScale, 6 * zScale, 4 * zScale);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 1.5 * zScale, surfaceP[1] - 1 * zScale, 3 * zScale, 2 * zScale);
                } else {
                    double moundR = Math.max(5.0 * zScale, 22.0 * maturityScale * zScale);
                    gc3D.setFill(Color.web("#9a3412"));
                    gc3D.fillOval(surfaceP[0] - moundR, surfaceP[1] - moundR * 0.5, moundR * 2, moundR);
                    gc3D.setFill(Color.web("#7c2d12"));
                    gc3D.fillOval(surfaceP[0] - moundR * 0.8, surfaceP[1] - moundR * 0.4, moundR * 1.6, moundR * 0.8);

                    // Multiple conical crater turrets
                    gc3D.setFill(Color.web("#b45309"));
                    gc3D.fillOval(surfaceP[0] - moundR * 0.35, surfaceP[1] - moundR * 0.35, moundR * 0.7, moundR * 0.4);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - moundR * 0.15, surfaceP[1] - moundR * 0.25, moundR * 0.3, moundR * 0.2);

                    // Scattered leaf fragments on ground around nest
                    gc3D.setFill(Color.web("#22c55e"));
                    Random r = new Random(77);
                    int lfCount = (int) Math.min(25, 4 + maturityScale * 8);
                    for (int lf = 0; lf < lfCount; lf++) {
                        double lx = surfaceP[0] + (r.nextDouble() - 0.5) * moundR * 2.4;
                        double ly = surfaceP[1] + (r.nextDouble() - 0.5) * moundR * 1.2;
                        gc3D.fillOval(lx, ly, 3.5 * zScale, 2.2 * zScale);
                    }
                }
            }
            case "ArborealCarton" -> {
                double branchY = surfaceP[1] - (isFoundingStage ? 20.0 : 38.0) * zScale;
                // Support branch
                gc3D.setStroke(Color.web("#78350f"));
                gc3D.setLineWidth((isFoundingStage ? 3.0 : 5.0) * zScale);
                gc3D.strokeLine(surfaceP[0] - 25 * zScale, branchY, surfaceP[0] + 25 * zScale, branchY);

                if (isFoundingStage) {
                    // Small founding carton paste spot
                    gc3D.setFill(Color.web("#a16207"));
                    gc3D.fillOval(surfaceP[0] - 3 * zScale, branchY - 2 * zScale, 6 * zScale, 4 * zScale);
                } else {
                    // Spherical wood-pulp carton globe wrapped around branch (Crematogaster)
                    double bulbR = Math.max(4.0 * zScale, 18.0 * maturityScale * zScale);
                    gc3D.setFill(Color.web("#a16207", 0.95));
                    gc3D.fillOval(surfaceP[0] - bulbR, branchY - bulbR * 0.8, bulbR * 2, bulbR * 1.6);
                    gc3D.setFill(Color.web("#78350f", 0.60));
                    gc3D.fillOval(surfaceP[0] - bulbR * 0.85, branchY - bulbR * 0.65, bulbR * 1.7, bulbR * 1.3);
                    gc3D.setStroke(Color.web("#451a03"));
                    gc3D.setLineWidth(1.5 * zScale);
                    gc3D.strokeOval(surfaceP[0] - bulbR, branchY - bulbR * 0.8, bulbR * 2, bulbR * 1.6);

                    // Multiple carton entry ports
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - bulbR * 0.4, branchY - bulbR * 0.2, 3 * zScale, 3 * zScale);
                    gc3D.fillOval(surfaceP[0] + bulbR * 0.3, branchY + bulbR * 0.1, 3 * zScale, 3 * zScale);
                }
            }
            case "LivingBivouac" -> {
                if (isFoundingStage) {
                    // Solitary queen / small cluster under fallen root
                    gc3D.setFill(Color.web("#7f1d1d"));
                    gc3D.fillOval(surfaceP[0] - 3 * zScale, surfaceP[1] - 3 * zScale, 6 * zScale, 5 * zScale);
                } else {
                    // Hanging teardrop cluster of interlocking army ant bodies (Eciton)
                    double bivouacW = Math.max(4.0 * zScale, 18.0 * maturityScale * zScale);
                    double bivouacH = Math.max(6.0 * zScale, 32.0 * maturityScale * zScale);
                    double topY = surfaceP[1] - 42.0 * zScale;

                    // Fallen root anchor
                    gc3D.setStroke(Color.web("#78350f"));
                    gc3D.setLineWidth(4.5 * zScale);
                    gc3D.strokeLine(surfaceP[0] - 28 * zScale, topY, surfaceP[0] + 28 * zScale, topY);

                    // Living curtain cluster
                    gc3D.setFill(Color.web("#7f1d1d", 0.90));
                    gc3D.fillPolygon(
                        new double[]{surfaceP[0] - bivouacW, surfaceP[0], surfaceP[0] + bivouacW},
                        new double[]{topY, topY + bivouacH, topY}, 3
                    );
                    gc3D.setFill(Color.web("#991b1b", 0.85));
                    gc3D.fillOval(surfaceP[0] - bivouacW * 0.7, topY + bivouacH * 0.25, bivouacW * 1.4, bivouacH * 0.65);
                    gc3D.setFill(Color.web("#b91c1c", 0.75));
                    gc3D.fillOval(surfaceP[0] - bivouacW * 0.4, topY + bivouacH * 0.45, bivouacW * 0.8, bivouacH * 0.4);
                }
            }
            case "WeaverLeaf" -> {
                double branchY = surfaceP[1] - (isFoundingStage ? 22.0 : 40.0) * zScale;
                gc3D.setStroke(Color.web("#78350f"));
                gc3D.setLineWidth(3.5 * zScale);
                gc3D.strokeLine(surfaceP[0] - 25 * zScale, branchY, surfaceP[0] + 25 * zScale, branchY);

                if (isFoundingStage) {
                    // Single leaf edge curled with 2 silk threads (Founding Queen)
                    gc3D.setFill(Color.web("#22c55e"));
                    gc3D.fillOval(surfaceP[0] - 5 * zScale, branchY - 4 * zScale, 10 * zScale, 7 * zScale);
                    gc3D.setStroke(Color.web("#f8fafc", 0.85));
                    gc3D.strokeLine(surfaceP[0] - 2 * zScale, branchY - 3 * zScale, surfaceP[0] + 2 * zScale, branchY + 2 * zScale);
                } else {
                    double leafR = Math.max(5.0 * zScale, 20.0 * maturityScale * zScale);
                    gc3D.setFill(Color.web("#15803d"));
                    gc3D.fillOval(surfaceP[0] - leafR, branchY - leafR * 0.6, leafR * 1.7, leafR * 1.3);
                    gc3D.setFill(Color.web("#22c55e", 0.85));
                    gc3D.fillOval(surfaceP[0] - leafR * 0.5, branchY - leafR * 0.8, leafR * 1.5, leafR);

                    // Multi-leaf pavilion lobes
                    if (maturityScale > 0.4) {
                        gc3D.setFill(Color.web("#16a34a", 0.85));
                        gc3D.fillOval(surfaceP[0] + leafR * 0.2, branchY - leafR * 0.4, leafR * 0.9, leafR * 0.7);
                    }

                    // White silk stitching fibers
                    gc3D.setStroke(Color.web("#f8fafc", 0.90));
                    gc3D.setLineWidth(1.3 * zScale);
                    for (int s = -3; s <= 3; s++) {
                        gc3D.strokeLine(surfaceP[0] + s * 4 * zScale, branchY - 7 * zScale, surfaceP[0] + (s + 1) * 4 * zScale, branchY + 6 * zScale);
                    }
                }
            }
            case "WoodGallery" -> {
                double trunkW = Math.max(12.0 * zScale, 20.0 * zScale);
                double trunkH = (isFoundingStage ? 20.0 : 45.0) * zScale;
                double topY = surfaceP[1] - trunkH;

                gc3D.setFill(Color.web("#582607"));
                gc3D.fillRect(surfaceP[0] - trunkW * 0.5, topY, trunkW, trunkH);

                if (isFoundingStage) {
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 2 * zScale, topY + trunkH * 0.5, 4 * zScale, 6 * zScale);
                } else {
                    // Sawdust frass ejecta piles at trunk base
                    double frassR = Math.max(4.0 * zScale, 14.0 * maturityScale * zScale);
                    gc3D.setFill(Color.web("#ca8a04", 0.85));
                    gc3D.fillOval(surfaceP[0] - frassR, surfaceP[1] - frassR * 0.3, frassR * 2, frassR * 0.6);

                    // Multiple heartwood gallery slits
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillRect(surfaceP[0] - trunkW * 0.25, topY + trunkH * 0.3, 3 * zScale, 8 * zScale);
                    gc3D.fillRect(surfaceP[0] + trunkW * 0.1, topY + trunkH * 0.55, 3 * zScale, 7 * zScale);
                }
            }
            case "BambooStem" -> {
                double stemW = Math.max(4.0 * zScale, 8.0 * zScale);
                double stemH = (isFoundingStage ? 22.0 : 42.0) * zScale;
                double topY = surfaceP[1] - stemH;

                gc3D.setFill(Color.web("#4ade80"));
                gc3D.fillRect(surfaceP[0] - stemW * 0.5, topY, stemW, stemH);
                gc3D.setStroke(Color.web("#166534"));
                gc3D.setLineWidth(1.5 * zScale);
                gc3D.strokeLine(surfaceP[0] - stemW * 0.5, topY + stemH * 0.45, surfaceP[0] + stemW * 0.5, topY + stemH * 0.45);

                // Circular entrance hole with phragmotic soldier plug
                gc3D.setFill(Color.web("#18181b"));
                gc3D.fillOval(surfaceP[0] - stemW * 0.3, topY + stemH * 0.55, stemW * 0.6, stemW * 0.6);
                gc3D.setFill(Color.web("#b45309"));
                gc3D.fillOval(surfaceP[0] - stemW * 0.2, topY + stemH * 0.58, stemW * 0.4, stemW * 0.4);
            }
            case "CompositeCraterMound" -> {
                if (isFoundingStage) {
                    gc3D.setFill(Color.web("#78716c"));
                    gc3D.fillOval(surfaceP[0] - 3 * zScale, surfaceP[1] - 2 * zScale, 6 * zScale, 4 * zScale);
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - 1.5 * zScale, surfaceP[1] - 1 * zScale, 3 * zScale, 2 * zScale);
                } else {
                    double coneR = Math.max(5.0 * zScale, 18.0 * maturityScale * zScale);
                    // Chaff ring
                    gc3D.setFill(Color.web("#d97706", 0.45));
                    gc3D.fillOval(surfaceP[0] - coneR * 1.4, surfaceP[1] - coneR * 0.7, coneR * 2.8, coneR * 1.4);

                    // Gravel pebble cone
                    gc3D.setFill(Color.web("#78716c"));
                    gc3D.fillOval(surfaceP[0] - coneR, surfaceP[1] - coneR * 0.55, coneR * 2, coneR * 1.1);
                    gc3D.setFill(Color.web("#57534e"));
                    gc3D.fillOval(surfaceP[0] - coneR * 0.7, surfaceP[1] - coneR * 0.4, coneR * 1.4, coneR * 0.8);

                    // Funnel crater entrance
                    gc3D.setFill(Color.web("#18181b"));
                    gc3D.fillOval(surfaceP[0] - coneR * 0.3, surfaceP[1] - coneR * 0.2, coneR * 0.6, coneR * 0.4);
                }
            }
            case "SupercolonyNetwork" -> {
                if (isFoundingStage) {
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillOval(surfaceP[0] - 3 * zScale, surfaceP[1] - 2 * zScale, 6 * zScale, 4 * zScale);
                } else {
                    double mainR = Math.max(5.0 * zScale, 20.0 * maturityScale * zScale);
                    gc3D.setFill(Color.web("#78350f"));
                    gc3D.fillOval(surfaceP[0] - mainR, surfaceP[1] - mainR * 0.55, mainR * 2, mainR * 1.1);

                    // Satellite daughter nests linked by trails
                    double satR = mainR * 0.55;
                    double[] satOffset = new double[]{mainR * 1.5, -mainR * 0.4};
                    gc3D.setStroke(Color.web("#f59e0b", 0.65));
                    gc3D.setLineWidth(1.8 * zScale);
                    gc3D.strokeLine(surfaceP[0], surfaceP[1], surfaceP[0] + satOffset[0], surfaceP[1] + satOffset[1]);

                    gc3D.setFill(Color.web("#92400e"));
                    gc3D.fillOval(surfaceP[0] + satOffset[0] - satR, surfaceP[1] + satOffset[1] - satR * 0.5, satR * 2, satR);
                }
            }
            default -> {
                // Subterranean Ant Burrow entrance craters are rendered by the entrance overlay loop in drawGalleriesOverlay3D
            }
        }
    }
}