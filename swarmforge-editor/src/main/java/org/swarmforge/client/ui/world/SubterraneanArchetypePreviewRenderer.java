/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.swarmforge.client.ui.WorldEditorPane;

/**
 * Procedural 3D archetype renderer for previewing subterranean nest architectures
 * (Thatch mounds, Termite cathedrals, Paper wasp multi-tiers, and standard ant burrows)
 * when no live simulation tunnel network is active.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class SubterraneanArchetypePreviewRenderer {

    private SubterraneanArchetypePreviewRenderer() {
    }

    static void renderArchetypeChambers(WorldEditorPane pane, GraphicsContext gc3D,
                                        int nestX, int nestY, double baseZ,
                                        double[] pE1, double[] pE2, double[] pE3,
                                        double cx, double cy, double scale, double radAz, double radEl,
                                        double zSc, double rQueen, double rBrood, double rFood,
                                        double rFungus, double rAphid, double rHibern, double rTrash,
                                        boolean isWinter, boolean isLeafcutterOrTermite, String adapt) {
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
            renderThatchMound(pane, gc3D, nestX, nestY, baseZ, pE1, pE2, cx, cy, scale, radAz, radEl,
                    tunnelCol, rQueen, rBrood, rAphid, rHibern, isWinter);
        } else if (isCathedral) {
            renderCathedralSpire(pane, gc3D, nestX, nestY, baseZ, cx, cy, scale, radAz, radEl,
                    rQueen, rBrood, rFungus);
        } else if (isPaper || isBees || isCarton || isBivouac) {
            renderArborealMultiTier(pane, gc3D, nestX, nestY, baseZ, cx, cy, scale, radAz, radEl, rBrood);
        } else {
            renderSubterraneanBurrow(pane, gc3D, nestX, nestY, baseZ, pE1, pE2, pE3, cx, cy, scale, radAz, radEl,
                    rQueen, rBrood, rFood, rFungus, rAphid, rHibern, rTrash, isWinter, isLeafcutterOrTermite);
        }
    }

    private static void renderThatchMound(WorldEditorPane pane, GraphicsContext gc3D,
                                          int nestX, int nestY, double baseZ,
                                          double[] pE1, double[] pE2,
                                          double cx, double cy, double scale, double radAz, double radEl,
                                          Color tunnelCol, double rQueen, double rBrood,
                                          double rAphid, double rHibern, boolean isWinter) {
        double hubZ = baseZ - 4.0;
        double[] pHub = pane.project3DPoint(nestX, nestY, hubZ, cx, cy, scale, radAz, radEl);
        double[] pApex = pane.project3DPoint(nestX, nestY, baseZ + 14.0, cx, cy, scale, radAz, radEl);
        gc3D.strokeLine(pApex[0], pApex[1], pHub[0], pHub[1]);
        gc3D.strokeLine(pE1[0], pE1[1], pHub[0], pHub[1]);
        gc3D.strokeLine(pE2[0], pE2[1], pHub[0], pHub[1]);

        // Solarium Incubation Chamber 1
        double[] pSolar1 = pane.project3DPoint(nestX + 3, nestY + 1, baseZ + 8.0, cx, cy, scale, radAz, radEl);
        gc3D.strokeLine(pHub[0], pHub[1], pSolar1[0], pSolar1[1]);
        gc3D.setFill(Color.web("#f8fafc", 0.85));
        gc3D.fillOval(pSolar1[0] - rBrood * 1.1, pSolar1[1] - rBrood * 0.8, rBrood * 2.2, rBrood * 1.6);
        gc3D.setStroke(Color.web("#ffffff", 0.95)); gc3D.setLineWidth(1.5);
        gc3D.strokeOval(pSolar1[0] - rBrood * 1.1, pSolar1[1] - rBrood * 0.8, rBrood * 2.2, rBrood * 1.6);

        // Solarium Incubation Chamber 2
        double[] pSolar2 = pane.project3DPoint(nestX - 3, nestY - 2, baseZ + 5.0, cx, cy, scale, radAz, radEl);
        gc3D.strokeLine(pHub[0], pHub[1], pSolar2[0], pSolar2[1]);
        gc3D.setFill(Color.web("#f8fafc", 0.80));
        gc3D.fillOval(pSolar2[0] - rBrood, pSolar2[1] - rBrood * 0.7, rBrood * 2.0, rBrood * 1.4);
        gc3D.strokeOval(pSolar2[0] - rBrood, pSolar2[1] - rBrood * 0.7, rBrood * 2.0, rBrood * 1.4);

        // Deep Queen Crypt
        double[] pQueen = pane.project3DPoint(nestX + 4, nestY + 2, baseZ - 24.0, cx, cy, scale, radAz, radEl);
        gc3D.setStroke(tunnelCol); gc3D.strokeLine(pHub[0], pHub[1], pQueen[0], pQueen[1]);
        gc3D.setFill(Color.web("#d946ef", 0.70));
        gc3D.fillOval(pQueen[0] - rQueen, pQueen[1] - rQueen * 0.7, rQueen * 2, rQueen * 1.4);
        gc3D.setStroke(Color.web("#f0abfc", 0.95)); gc3D.setLineWidth(1.8);
        gc3D.strokeOval(pQueen[0] - rQueen, pQueen[1] - rQueen * 0.7, rQueen * 2, rQueen * 1.4);

        // Winter Hibernation Vault
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
    }

    private static void renderCathedralSpire(WorldEditorPane pane, GraphicsContext gc3D,
                                             int nestX, int nestY, double baseZ,
                                             double cx, double cy, double scale, double radAz, double radEl,
                                             double rQueen, double rBrood, double rFungus) {
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
    }

    private static void renderArborealMultiTier(WorldEditorPane pane, GraphicsContext gc3D,
                                               int nestX, int nestY, double baseZ,
                                               double cx, double cy, double scale, double radAz, double radEl,
                                               double rBrood) {
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
    }

    private static void renderSubterraneanBurrow(WorldEditorPane pane, GraphicsContext gc3D,
                                                int nestX, int nestY, double baseZ,
                                                double[] pE1, double[] pE2, double[] pE3,
                                                double cx, double cy, double scale, double radAz, double radEl,
                                                double rQueen, double rBrood, double rFood, double rFungus,
                                                double rAphid, double rHibern, double rTrash,
                                                boolean isWinter, boolean isLeafcutterOrTermite) {
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
