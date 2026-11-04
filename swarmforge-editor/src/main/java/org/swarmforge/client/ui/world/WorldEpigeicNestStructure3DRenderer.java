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

        WorldEpigeicArchetypeDrawingHelper.drawArchetype(nestType, gc3D, surfaceP, zScale, maturityScale, isFoundingStage);
    }
}
