/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.PredatorType;

/**
 * High-fidelity Canvas 2D/3D Morphology Renderer for Insects and Macro-fauna.
 * Renders realistic morphological anatomies for:
 * <ul>
 *   <li>Brood items: eggs, segmented larvae, silken cocoons / pupae</li>
 *   <li>Ants: Queens, soldiers, males, workers with articulated jointed legs, petiole, gaster, antennae, and mandibles</li>
 *   <li>Macro-faunal predators: arachnids, birds/woodpeckers, lizards, hymenopterans, caterpillars, and antlions</li>
 * </ul>
 */
public class WorldEntityMorphologyRenderer {

    public void drawRealisticBroodItem(GraphicsContext gc, double px, double py,
                                       Individual.LifeStage stage, double baseR) {
        if (stage == null) stage = Individual.LifeStage.EGG;
        double r = Math.max(1.2, baseR * 0.7);
        switch (stage) {
            case EGG -> {
                // Shiny pearly white egg ovoid
                gc.setFill(Color.web("#f8fafc", 0.92));
                gc.fillOval(px - r * 0.6, py - r * 0.9, r * 1.2, r * 1.8);
                gc.setStroke(Color.web("#cbd5e1", 0.7));
                gc.setLineWidth(Math.max(0.5, r * 0.15));
                gc.strokeOval(px - r * 0.6, py - r * 0.9, r * 1.2, r * 1.8);
                // Specular highlight
                gc.setFill(Color.web("#ffffff", 0.95));
                gc.fillOval(px - r * 0.25, py - r * 0.6, r * 0.35, r * 0.5);
            }
            case LARVA -> {
                // Curved segmented cream-colored grub
                gc.setFill(Color.web("#fef08a", 0.95));
                gc.fillOval(px - r * 0.8, py - r * 1.1, r * 1.6, r * 2.2);
                // Cuticular segments
                gc.setStroke(Color.web("#ca8a04", 0.45));
                gc.setLineWidth(Math.max(0.5, r * 0.12));
                gc.strokeLine(px - r * 0.6, py - r * 0.4, px + r * 0.6, py - r * 0.4);
                gc.strokeLine(px - r * 0.7, py, px + r * 0.7, py);
                gc.strokeLine(px - r * 0.6, py + r * 0.4, px + r * 0.6, py + r * 0.4);
                // Head capsule dot
                gc.setFill(Color.web("#854d0e", 0.9));
                gc.fillOval(px - r * 0.25, py - r * 1.0, r * 0.5, r * 0.5);
            }
            case PUPA -> {
                // Silken cocoon / exarate nymph
                gc.setFill(Color.web("#fed7aa", 0.92));
                gc.fillOval(px - r * 0.7, py - r * 1.3, r * 1.4, r * 2.6);
                gc.setStroke(Color.web("#d97706", 0.6));
                gc.setLineWidth(Math.max(0.5, r * 0.15));
                gc.strokeOval(px - r * 0.7, py - r * 1.3, r * 1.4, r * 2.6);
                // Cocoon longitudinal fiber highlight
                gc.setStroke(Color.web("#ffedd5", 0.7));
                gc.strokeLine(px, py - r * 1.0, px, py + r * 1.0);
            }
            default -> {}
        }
    }

    public void drawRealisticAnt(GraphicsContext gc, double px, double py, double heading,
                                 Individual.Caste caste,
                                 Color baseColor, double baseR, boolean isCarryingFood) {
        if (caste == null) caste = Individual.Caste.WORKER;

        double scale = switch (caste) {
            case QUEEN -> 1.8;
            case SOLDIER -> 1.4;
            case MALE -> 1.25;
            default -> 1.0;
        };
        double r = baseR * scale;

        double cosH = Math.cos(heading);
        double sinH = Math.sin(heading);
        double pxH = -sinH;
        double pyH = cosH;

        double headDist = r * 1.6;
        double petioleDist = -r * 0.8;
        double gasterDist = -r * 2.3;

        double headX = px + cosH * headDist;
        double headY = py + sinH * headDist;
        double petioleX = px + cosH * petioleDist;
        double petioleY = py + sinH * petioleDist;
        double gasterX = px + cosH * gasterDist;
        double gasterY = py + sinH * gasterDist;

        double headR = (caste == Individual.Caste.SOLDIER) ? r * 1.3 : r * 0.85;
        double thoraxR = r * 0.75;
        double gasterR = (caste == Individual.Caste.QUEEN) ? r * 1.7 : r * 1.25;

        // 1. LEGS (6 Articulated Legs)
        gc.setLineWidth(Math.max(1.0, r * 0.35));
        gc.setStroke(baseColor.darker().darker());

        double[][] legOffsets = {
            {r * 0.5, 0.95, 0.4},
            {0.0, 1.1, 0.0},
            {-r * 0.5, 1.0, -0.4}
        };

        double animTime = (System.currentTimeMillis() % 600) / 600.0;
        int legIdx = 0;

        for (double[] legDef : legOffsets) {
            double offFwd = legDef[0];
            double legLen = r * 1.8 * legDef[1];
            double fwdAng = legDef[2];
            double swing = Math.sin(animTime * Math.PI * 2 + legIdx * Math.PI / 3) * 0.25;
            legIdx++;

            for (int side : new int[]{1, -1}) {
                double rootX = px + cosH * offFwd + pxH * (thoraxR * 0.6 * side);
                double rootY = py + sinH * offFwd + pyH * (thoraxR * 0.6 * side);

                double kneeAngle = heading + (fwdAng + swing * side) + (Math.PI / 2.0 * side);
                double kneeX = rootX + Math.cos(kneeAngle) * (legLen * 0.55);
                double kneeY = rootY + Math.sin(kneeAngle) * (legLen * 0.55);

                double tipAngle = kneeAngle + (0.4 * side);
                double tipX = kneeX + Math.cos(tipAngle) * (legLen * 0.5);
                double tipY = kneeY + Math.sin(tipAngle) * (legLen * 0.5);

                gc.strokeLine(rootX, rootY, kneeX, kneeY);
                gc.strokeLine(kneeX, kneeY, tipX, tipY);
            }
        }

        // 2. GASTER (Abdomen)
        gc.setFill(baseColor.darker());
        gc.fillOval(gasterX - gasterR, gasterY - gasterR, gasterR * 2, gasterR * 2);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.0);
        gc.strokeOval(gasterX - gasterR, gasterY - gasterR, gasterR * 2, gasterR * 2);

        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(1.2);
        gc.strokeArc(gasterX - gasterR * 0.7, gasterY - gasterR * 0.7, gasterR * 1.4, gasterR * 1.4, Math.toDegrees(heading) + 120, 120, javafx.scene.shape.ArcType.OPEN);

        // 3. PETIOLE
        gc.setFill(baseColor.darker().darker());
        double petR = Math.max(1.2, r * 0.4);
        gc.fillOval(petioleX - petR, petioleY - petR, petR * 2, petR * 2);

        // 4. THORAX
        gc.setFill(baseColor);
        gc.fillOval(px - thoraxR, py - thoraxR, thoraxR * 2, thoraxR * 2);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.0);
        gc.strokeOval(px - thoraxR, py - thoraxR, thoraxR * 2, thoraxR * 2);

        if (caste == Individual.Caste.MALE || caste == Individual.Caste.QUEEN) {
            gc.setFill(Color.web("rgba(224, 242, 254, 0.55)"));
            gc.setStroke(Color.web("rgba(255, 255, 255, 0.85)"));
            gc.setLineWidth(0.8);

            double wingLen = r * 2.6;
            for (int side : new int[]{1, -1}) {
                double wAngle = heading - Math.PI * 0.75 * side;
                double wx = px + Math.cos(wAngle) * wingLen;
                double wy = py + Math.sin(wAngle) * wingLen;
                gc.fillOval(px + (wx - px) * 0.5 - r * 0.4, py + (wy - py) * 0.5 - r * 0.4, r * 1.5, r * 0.7);
                gc.strokeOval(px + (wx - px) * 0.5 - r * 0.4, py + (wy - py) * 0.5 - r * 0.4, r * 1.5, r * 0.7);
            }
        }

        // 5. HEAD & EYES
        gc.setFill(baseColor);
        gc.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.0);
        gc.strokeOval(headX - headR, headY - headR, headR * 2, headR * 2);

        gc.setFill(Color.web("#090d16"));
        double eyeR = Math.max(1.0, headR * 0.35);
        double eyeOffX = pxH * (headR * 0.65);
        double eyeOffY = pyH * (headR * 0.65);
        gc.fillOval(headX + eyeOffX - eyeR, headY + eyeOffY - eyeR, eyeR * 2, eyeR * 2);
        gc.fillOval(headX - eyeOffX - eyeR, headY - eyeOffY - eyeR, eyeR * 2, eyeR * 2);

        // 6. ANTENNAE
        gc.setStroke(baseColor.brighter().brighter());
        gc.setLineWidth(Math.max(1.0, r * 0.3));

        double antLen = r * 2.2;
        for (int side : new int[]{1, -1}) {
            double scapeAng = heading + (0.35 * side);
            double jointX = headX + Math.cos(scapeAng) * (antLen * 0.5);
            double jointY = headY + Math.sin(scapeAng) * (antLen * 0.5);

            double funicleAng = scapeAng + (0.45 * side);
            double tipX = jointX + Math.cos(funicleAng) * (antLen * 0.5);
            double tipY = jointY + Math.sin(funicleAng) * (antLen * 0.5);

            gc.strokeLine(headX + Math.cos(scapeAng) * (headR * 0.5), headY + Math.sin(scapeAng) * (headR * 0.5), jointX, jointY);
            gc.strokeLine(jointX, jointY, tipX, tipY);
        }

        // 7. MANDIBLES
        gc.setStroke(caste == Individual.Caste.SOLDIER ? Color.web("#7f1d1d") : Color.web("#451a03"));
        gc.setLineWidth(caste == Individual.Caste.SOLDIER ? Math.max(1.8, r * 0.5) : Math.max(1.0, r * 0.3));

        double mandLen = (caste == Individual.Caste.SOLDIER) ? r * 1.3 : r * 0.7;
        for (int side : new int[]{1, -1}) {
            double mRootX = headX + cosH * (headR * 0.7) + pxH * (headR * 0.4 * side);
            double mRootY = headY + sinH * (headR * 0.7) + pyH * (headR * 0.4 * side);

            double mTipX = mRootX + cosH * mandLen - pxH * (mandLen * 0.35 * side);
            double mTipY = mRootY + sinH * mandLen - pyH * (mandLen * 0.35 * side);

            gc.strokeLine(mRootX, mRootY, mTipX, mTipY);
        }

        // 8. CARRIED ITEM
        if (isCarryingFood) {
            double foodX = headX + cosH * (headR * 1.5);
            double foodY = headY + sinH * (headR * 1.5);
            double foodR = r * 0.9;
            gc.setFill(Color.web("#22c55e"));
            gc.fillOval(foodX - foodR, foodY - foodR * 0.6, foodR * 2, foodR * 1.2);
            gc.setStroke(Color.web("#15803d"));
            gc.setLineWidth(1.0);
            gc.strokeOval(foodX - foodR, foodY - foodR * 0.6, foodR * 2, foodR * 1.2);
        }
    }

    public void drawRealisticPredator(GraphicsContext gc, double px, double py, double heading,
                                       PredatorType type, float healthPct, double baseR) {
        if (type == null) type = PredatorType.BEETLE;

        double cosH = Math.cos(heading);
        double sinH = Math.sin(heading);
        double pxH = -sinH;
        double pyH = cosH;

        // Species-specific scale and color palette
        double scaleMult = switch (type) {
            case VARROA_MITE -> 0.75;
            case SPIDER -> 1.5;
            case ANTLION -> 1.9;
            case BEETLE, MYRMECOPHILE_BEETLE -> 1.8;
            case WASP, BEE_WOLF -> 2.0;
            case ASIAN_HORNET -> 2.4;
            case MEGAPONERA_RAIDER -> 2.0;
            case LADYBUG_LARVA, CATERPILLAR -> 1.8;
            case LIZARD -> 2.8;
            case BIRD, WOODPECKER -> 3.6;
            case HONEY_BUZZARD -> 4.2;
            case AARDVARK_MOUND_BREAKER -> 4.5;
            default -> 1.5;
        };
        double r = baseR * scaleMult;

        Color bodyColor = switch (type) {
            case SPIDER -> Color.web("#1e1b4b");
            case ANTLION -> Color.web("#a16207");
            case BEETLE -> Color.web("#064e3b");
            case MYRMECOPHILE_BEETLE -> Color.web("#78350f");
            case BIRD -> Color.web("#0284c7");
            case WOODPECKER -> Color.web("#dc2626");
            case HONEY_BUZZARD -> Color.web("#78350f");
            case LIZARD -> Color.web("#16a34a");
            case WASP, BEE_WOLF -> Color.web("#eab308");
            case ASIAN_HORNET -> Color.web("#f97316");
            case MEGAPONERA_RAIDER -> Color.web("#09090b");
            case CATERPILLAR -> Color.web("#84cc16");
            case LADYBUG_LARVA -> Color.web("#ea580c");
            case VARROA_MITE -> Color.web("#991b1b");
            case AARDVARK_MOUND_BREAKER -> Color.web("#713f12");
            default -> Color.web("#7c2d12");
        };

        if (type == PredatorType.SPIDER) {
            // ── TRUE ARACHNID MORPHOLOGY (Prosoma + Opisthosoma + 8 Jointed Legs + Chelicerae + Pedipalps) ──
            gc.setStroke(Color.web("#0f172a"));
            gc.setLineWidth(Math.max(1.2, r * 0.22));

            double prosomaR = r * 0.75;
            double opisthosomaR = r * 1.15;
            double opisthoX = px - cosH * (r * 1.05);
            double opisthoY = py - sinH * (r * 1.05);

            for (int i = 0; i < 4; i++) {
                for (int side : new int[]{1, -1}) {
                    double rootSpread = (i - 1.5) * 0.28;
                    double legRootX = px + cosH * (rootSpread * prosomaR) + pxH * (prosomaR * 0.75 * side);
                    double legRootY = py + sinH * (rootSpread * prosomaR) + pyH * (prosomaR * 0.75 * side);

                    double legAng = heading + (0.35 + i * 0.38) * side + (Math.PI * 0.45 * side);
                    if (i == 0) legAng = heading + 0.55 * side;
                    else if (i == 3) legAng = heading + Math.PI * side - 0.45 * side;

                    double kneeLen = r * 1.45;
                    double tibiaLen = r * 1.35;

                    double kneeX = legRootX + Math.cos(legAng) * kneeLen;
                    double kneeY = legRootY + Math.sin(legAng) * kneeLen;

                    double tipAng = legAng + (i <= 1 ? 0.45 * side : -0.45 * side);
                    double tipX = kneeX + Math.cos(tipAng) * tibiaLen;
                    double tipY = kneeY + Math.sin(tipAng) * tibiaLen;

                    gc.strokeLine(legRootX, legRootY, kneeX, kneeY);
                    gc.strokeLine(kneeX, kneeY, tipX, tipY);
                }
            }

            // 2. Opisthosoma
            gc.setFill(bodyColor);
            gc.fillOval(opisthoX - opisthosomaR, opisthoY - opisthosomaR * 0.85, opisthosomaR * 2, opisthosomaR * 1.7);
            gc.setStroke(Color.web("#020617"));
            gc.setLineWidth(1.2);
            gc.strokeOval(opisthoX - opisthosomaR, opisthoY - opisthosomaR * 0.85, opisthosomaR * 2, opisthosomaR * 1.7);

            // Dorsal Foliate / Chevron Pattern
            gc.setStroke(Color.web("#6366f1"));
            gc.setLineWidth(1.0);
            for (int ch = 0; ch < 3; ch++) {
                double chDist = (ch - 1) * (opisthosomaR * 0.4);
                double chX = opisthoX + cosH * chDist;
                double chY = opisthoY + sinH * chDist;
                double chWingX = chX - cosH * (opisthosomaR * 0.25);
                double chWingY = chY - sinH * (opisthosomaR * 0.25);
                gc.strokeLine(chX, chY, chWingX + pxH * (opisthosomaR * 0.45), chWingY + pyH * (opisthosomaR * 0.45));
                gc.strokeLine(chX, chY, chWingX - pxH * (opisthosomaR * 0.45), chWingY - pyH * (opisthosomaR * 0.45));
            }

            // 3. Pedicel
            gc.setFill(Color.web("#09090b"));
            double pedX = px - cosH * (prosomaR * 0.7);
            double pedY = py - sinH * (prosomaR * 0.7);
            gc.fillOval(pedX - r * 0.2, pedY - r * 0.2, r * 0.4, r * 0.4);

            // 4. Prosoma
            gc.setFill(bodyColor.brighter());
            gc.fillOval(px - prosomaR, py - prosomaR * 0.8, prosomaR * 2, prosomaR * 1.6);
            gc.setStroke(Color.web("#020617"));
            gc.setLineWidth(1.2);
            gc.strokeOval(px - prosomaR, py - prosomaR * 0.8, prosomaR * 2, prosomaR * 1.6);

            // 5. 8 Frontal Ocelli
            gc.setFill(Color.web("#38bdf8"));
            double eyeBaseX = px + cosH * (prosomaR * 0.55);
            double eyeBaseY = py + sinH * (prosomaR * 0.55);
            for (int row = 0; row < 2; row++) {
                double rOff = row * (prosomaR * 0.22);
                for (int col = -2; col <= 2; col++) {
                    if (col == 0) continue;
                    double eX = eyeBaseX + cosH * rOff + pxH * (col * 0.18 * prosomaR);
                    double eY = eyeBaseY + sinH * rOff + pyH * (col * 0.18 * prosomaR);
                    gc.fillOval(eX - 0.9, eY - 0.9, 1.8, 1.8);
                }
            }

            // 6. Chelicerae & Fangs
            gc.setStroke(Color.web("#dc2626"));
            gc.setLineWidth(Math.max(1.2, r * 0.25));
            double fangBaseX = px + cosH * (prosomaR * 0.9);
            double fangBaseY = py + sinH * (prosomaR * 0.9);
            for (int side : new int[]{1, -1}) {
                double fRootX = fangBaseX + pxH * (prosomaR * 0.3 * side);
                double fRootY = fangBaseY + pyH * (prosomaR * 0.3 * side);
                double fTipX = fRootX + cosH * (r * 0.55) - pxH * (r * 0.15 * side);
                double fTipY = fRootY + sinH * (r * 0.55) - pyH * (r * 0.15 * side);
                gc.strokeLine(fRootX, fRootY, fTipX, fTipY);
            }

            // 7. Pedipalps
            gc.setStroke(bodyColor.darker());
            gc.setLineWidth(Math.max(1.0, r * 0.18));
            for (int side : new int[]{1, -1}) {
                double pRootX = fangBaseX + pxH * (prosomaR * 0.5 * side);
                double pRootY = fangBaseY + pyH * (prosomaR * 0.5 * side);
                double pTipX = pRootX + cosH * (r * 0.8) + pxH * (r * 0.45 * side);
                double pTipY = pRootY + sinH * (r * 0.8) + pyH * (r * 0.45 * side);
                gc.strokeLine(pRootX, pRootY, pTipX, pTipY);
            }

        } else if (type == PredatorType.BIRD || type == PredatorType.HONEY_BUZZARD || type == PredatorType.WOODPECKER) {
            // ── AVIAN SILHOUETTE (Body + Wings + Beak + Fanned Tail Plumage) ──
            gc.setFill(bodyColor.darker());
            gc.setStroke(Color.web("#0f172a"));
            gc.setLineWidth(1.2);
            for (int side : new int[]{1, -1}) {
                double wX = px + pxH * (r * 1.6 * side);
                double wY = py + pyH * (r * 1.6 * side);
                gc.fillOval(wX - r * 0.8, wY - r * 0.4, r * 1.6, r * 0.8);
                gc.strokeOval(wX - r * 0.8, wY - r * 0.4, r * 1.6, r * 0.8);
            }

            // 2. Fanned Tail
            double tailX = px - cosH * (r * 1.4);
            double tailY = py - sinH * (r * 1.4);
            gc.setFill(bodyColor.darker().darker());
            gc.fillOval(tailX - r * 0.6, tailY - r * 0.4, r * 1.2, r * 0.8);

            // 3. Torso & Head
            gc.setFill(bodyColor);
            gc.fillOval(px - r * 0.8, py - r * 0.6, r * 1.6, r * 1.2);
            gc.strokeOval(px - r * 0.8, py - r * 0.6, r * 1.6, r * 1.2);

            double headX = px + cosH * (r * 0.9);
            double headY = py + sinH * (r * 0.9);
            double headR = r * 0.45;
            gc.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);

            // 4. Sharp Beak
            gc.setFill(Color.web("#f59e0b"));
            double beakTipX = headX + cosH * (headR * 2.0);
            double beakTipY = headY + sinH * (headR * 2.0);
            gc.strokeLine(headX + pxH * (headR * 0.4), headY + pyH * (headR * 0.4), beakTipX, beakTipY);
            gc.strokeLine(headX - pxH * (headR * 0.4), headY - pyH * (headR * 0.4), beakTipX, beakTipY);

            // Eyes
            gc.setFill(Color.web("#0f172a"));
            gc.fillOval(headX + pxH * (headR * 0.5) - 1.2, headY + pyH * (headR * 0.5) - 1.2, 2.4, 2.4);
            gc.fillOval(headX - pxH * (headR * 0.5) - 1.2, headY - pyH * (headR * 0.5) - 1.2, 2.4, 2.4);

        } else if (type == PredatorType.LIZARD) {
            // ── REPTILIAN MORPHOLOGY (Elongate S-curving body + 4 splayed clawed limbs + long tail) ──
            gc.setStroke(bodyColor.darker());
            gc.setLineWidth(Math.max(1.5, r * 0.35));
            double tailBaseX = px - cosH * (r * 0.9);
            double tailBaseY = py - sinH * (r * 0.9);
            double tailMidX = tailBaseX - cosH * (r * 1.2) + pxH * (r * 0.6);
            double tailMidY = tailBaseY - sinH * (r * 1.2) + pyH * (r * 0.6);
            double tailEndX = tailMidX - cosH * (r * 1.2) - pxH * (r * 0.4);
            double tailEndY = tailMidY - sinH * (r * 1.2) - pyH * (r * 0.4);
            gc.strokeLine(tailBaseX, tailBaseY, tailMidX, tailMidY);
            gc.strokeLine(tailMidX, tailMidY, tailEndX, tailEndY);

            // 2. 4 Splayed Limbs
            gc.setStroke(Color.web("#14532d"));
            gc.setLineWidth(Math.max(1.2, r * 0.2));
            for (int f = -1; f <= 1; f += 2) {
                double rootX = px + cosH * (f * r * 0.5);
                double rootY = py + sinH * (f * r * 0.5);
                for (int side : new int[]{1, -1}) {
                    double elbowX = rootX + pxH * (r * 0.8 * side) + cosH * (f * r * 0.3);
                    double elbowY = rootY + pyH * (r * 0.8 * side) + sinH * (f * r * 0.3);
                    double footX = elbowX + pxH * (r * 0.5 * side) + cosH * (f * r * 0.5);
                    double footY = elbowY + pyH * (r * 0.5 * side) + sinH * (f * r * 0.5);
                    gc.strokeLine(rootX, rootY, elbowX, elbowY);
                    gc.strokeLine(elbowX, elbowY, footX, footY);
                }
            }

            // 3. Elongated Reptilian Body
            gc.setFill(bodyColor);
            gc.fillOval(px - r * 1.0, py - r * 0.55, r * 2.0, r * 1.1);
            gc.setStroke(Color.web("#052e16"));
            gc.setLineWidth(1.2);
            gc.strokeOval(px - r * 1.0, py - r * 0.55, r * 2.0, r * 1.1);

            // 4. Triangular Head
            double headX = px + cosH * (r * 1.0);
            double headY = py + sinH * (r * 1.0);
            double headR = r * 0.5;
            gc.fillOval(headX - headR, headY - headR * 0.8, headR * 2, headR * 1.6);
            gc.strokeOval(headX - headR, headY - headR * 0.8, headR * 2, headR * 1.6);

            // Eyes
            gc.setFill(Color.web("#eab308"));
            gc.fillOval(headX + pxH * (headR * 0.5) - 1.2, headY + pyH * (headR * 0.5) - 1.2, 2.4, 2.4);
            gc.fillOval(headX - pxH * (headR * 0.5) - 1.2, headY - pyH * (headR * 0.5) - 1.2, 2.4, 2.4);

        } else if (type == PredatorType.WASP || type == PredatorType.ASIAN_HORNET || type == PredatorType.BEE_WOLF) {
            // ── HYMENOPTERAN PREDATOR (Banded Gaster + Petiole + Thorax + Translucent Wings + Stinger) ──
            gc.setFill(Color.web("rgba(224, 242, 254, 0.60)"));
            gc.setStroke(Color.web("rgba(255, 255, 255, 0.85)"));
            gc.setLineWidth(0.9);
            for (int side : new int[]{1, -1}) {
                double wAngle = heading - Math.PI * 0.7 * side;
                double wx = px + Math.cos(wAngle) * (r * 1.9);
                double wy = py + Math.sin(wAngle) * (r * 1.9);
                gc.fillOval(px + (wx - px) * 0.5 - r * 0.5, py + (wy - py) * 0.5 - r * 0.3, r * 1.8, r * 0.8);
                gc.strokeOval(px + (wx - px) * 0.5 - r * 0.5, py + (wy - py) * 0.5 - r * 0.3, r * 1.8, r * 0.8);
            }

            // 2. Banded Gaster
            double gasterX = px - cosH * (r * 1.0);
            double gasterY = py - sinH * (r * 1.0);
            double gastR = r * 0.85;
            gc.setFill(Color.web("#eab308"));
            gc.fillOval(gasterX - gastR, gasterY - gastR * 0.7, gastR * 2, gastR * 1.4);
            gc.setStroke(Color.web("#18181b"));
            gc.setLineWidth(1.2);
            gc.strokeOval(gasterX - gastR, gasterY - gastR * 0.7, gastR * 2, gastR * 1.4);

            // Black stripes across gaster
            gc.setStroke(Color.web("#18181b"));
            gc.setLineWidth(Math.max(1.8, r * 0.25));
            gc.strokeLine(gasterX - gastR * 0.4, gasterY - gastR * 0.5, gasterX - gastR * 0.4, gasterY + gastR * 0.5);
            gc.strokeLine(gasterX + gastR * 0.2, gasterY - gastR * 0.5, gasterX + gastR * 0.2, gasterY + gastR * 0.5);

            // Stinger
            gc.setStroke(Color.web("#09090b"));
            gc.setLineWidth(1.5);
            double stingX = gasterX - cosH * (gastR * 1.3);
            double stingY = gasterY - sinH * (gastR * 1.3);
            gc.strokeLine(gasterX - cosH * gastR, gasterY - sinH * gastR, stingX, stingY);

            // 3. Thorax & Head
            gc.setFill(Color.web("#18181b"));
            gc.fillOval(px - r * 0.5, py - r * 0.45, r * 1.0, r * 0.9);
            double headX = px + cosH * (r * 0.75);
            double headY = py + sinH * (r * 0.75);
            double headR = r * 0.4;
            gc.setFill(Color.web("#f97316"));
            gc.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);

            // Compound eyes
            gc.setFill(Color.web("#09090b"));
            gc.fillOval(headX + pxH * (headR * 0.6) - 1.2, headY + pyH * (headR * 0.6) - 1.2, 2.4, 2.4);
            gc.fillOval(headX - pxH * (headR * 0.6) - 1.2, headY - pyH * (headR * 0.6) - 1.2, 2.4, 2.4);

        } else if (type == PredatorType.CATERPILLAR || type == PredatorType.LADYBUG_LARVA) {
            // ── METAMERIC SEGMENTED LARVA (6-8 overlapping circular segments) ──
            int numSegs = 7;
            for (int s = numSegs - 1; s >= 0; s--) {
                double segOffset = (s - numSegs / 2.0) * (r * 0.4);
                double sX = px - cosH * segOffset;
                double sY = py - sinH * segOffset;
                double sR = (s == 0 || s == numSegs - 1) ? r * 0.45 : r * 0.6;

                gc.setFill(s % 2 == 0 ? bodyColor : bodyColor.brighter());
                gc.fillOval(sX - sR, sY - sR, sR * 2, sR * 2);
                gc.setStroke(Color.web("#1e293b"));
                gc.setLineWidth(1.0);
                gc.strokeOval(sX - sR, sY - sR, sR * 2, sR * 2);

                // Bristles/Dots on segments
                gc.setFill(Color.web("#0f172a"));
                gc.fillOval(sX + pxH * (sR * 0.6) - 1.0, sY + pyH * (sR * 0.6) - 1.0, 2.0, 2.0);
                gc.fillOval(sX - pxH * (sR * 0.6) - 1.0, sY - pyH * (sR * 0.6) - 1.0, 2.0, 2.0);
            }
            // Head capsule
            double headX = px + cosH * (r * 1.3);
            double headY = py + sinH * (r * 1.3);
            gc.setFill(Color.web("#18181b"));
            gc.fillOval(headX - r * 0.4, headY - r * 0.4, r * 0.8, r * 0.8);

        } else if (type == PredatorType.ANTLION) {
            // ── ANTLION (Flattened Sand-Pit Ambush Body with Massive Falcate Jaws) ──
            gc.setFill(bodyColor);
            gc.fillOval(px - r * 1.1, py - r * 0.9, r * 2.2, r * 1.8);
            gc.setStroke(Color.web("#451a03"));
            gc.setLineWidth(1.2);
            gc.strokeOval(px - r * 1.1, py - r * 0.9, r * 2.2, r * 1.8);

            // 2. Broad Flat Head
            double headX = px + cosH * (r * 0.85);
            double headY = py + sinH * (r * 0.85);
            double headR = r * 0.6;
            gc.setFill(bodyColor.darker());
            gc.fillOval(headX - headR, headY - headR * 0.8, headR * 2, headR * 1.6);

            // 3. Huge Curved Falciform Mandibles
            gc.setStroke(Color.web("#78350f"));
            gc.setLineWidth(Math.max(1.8, r * 0.3));
            for (int side : new int[]{1, -1}) {
                double mRootX = headX + cosH * (headR * 0.8) + pxH * (headR * 0.5 * side);
                double mRootY = headY + sinH * (headR * 0.8) + pyH * (headR * 0.5 * side);
                double mMidX = mRootX + cosH * (r * 0.9) + pxH * (r * 0.6 * side);
                double mMidY = mRootY + sinH * (r * 0.9) + pyH * (r * 0.6 * side);
                double mTipX = mMidX + cosH * (r * 0.5) - pxH * (r * 0.4 * side);
                double mTipY = mMidY + sinH * (r * 0.5) - pyH * (r * 0.4 * side);
                gc.strokeLine(mRootX, mRootY, mMidX, mMidY);
                gc.strokeLine(mMidX, mMidY, mTipX, mTipY);
            }

        } else {
            // ── SCLEROTIZED BEETLE / DEFAULT HARD SHELL ──
            gc.setStroke(Color.web("#0f172a"));
            gc.setLineWidth(Math.max(1.0, r * 0.2));
            for (int i = 0; i < 3; i++) {
                double off = (i - 1) * (r * 0.6);
                double lRootX = px + cosH * off;
                double lRootY = py + sinH * off;
                for (int side : new int[]{1, -1}) {
                    double kneeX = lRootX + pxH * (r * 1.2 * side);
                    double kneeY = lRootY + pyH * (r * 1.2 * side);
                    double tipX = kneeX + pxH * (r * 0.6 * side) + cosH * (i == 0 ? r * 0.4 : -r * 0.4);
                    double tipY = kneeY + pyH * (r * 0.6 * side) + sinH * (i == 0 ? r * 0.4 : -r * 0.4);
                    gc.strokeLine(lRootX, lRootY, kneeX, kneeY);
                    gc.strokeLine(kneeX, kneeY, tipX, tipY);
                }
            }

            // 2. Elytra (Wing Cases)
            gc.setFill(bodyColor);
            gc.fillOval(px - r * 0.9, py - r * 0.75, r * 1.8, r * 1.5);
            gc.setStroke(Color.web("#022c22"));
            gc.setLineWidth(1.2);
            gc.strokeOval(px - r * 0.9, py - r * 0.75, r * 1.8, r * 1.5);

            // Elytra median suture
            gc.setStroke(Color.web("#022c22"));
            gc.setLineWidth(1.0);
            gc.strokeLine(px - cosH * (r * 0.9), py - sinH * (r * 0.9), px + cosH * (r * 0.4), py + sinH * (r * 0.4));

            // Pronotum & Head
            double headX = px + cosH * (r * 0.85);
            double headY = py + sinH * (r * 0.85);
            double headR = r * 0.45;
            gc.setFill(bodyColor.darker());
            gc.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);

            // Eyes
            gc.setFill(Color.web("#09090b"));
            gc.fillOval(headX + pxH * (headR * 0.5) - 1.2, headY + pyH * (headR * 0.5) - 1.2, 2.4, 2.4);
            gc.fillOval(headX - pxH * (headR * 0.5) - 1.2, headY - pyH * (headR * 0.5) - 1.2, 2.4, 2.4);
        }

        // ── Health Bar Indicator above entity ──
        double barW = Math.max(16.0, r * 2.2);
        double barH = 3.5;
        double barX = px - barW / 2.0;
        double barY = py - r - 7.0;
        gc.setFill(Color.web("rgba(15, 23, 42, 0.85)"));
        gc.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
        gc.setFill(healthPct > 0.5 ? Color.web("#22c55e") : (healthPct > 0.25 ? Color.web("#f59e0b") : Color.web("#ef4444")));
        gc.fillRect(barX, barY, barW * Math.max(0.0, Math.min(1.0, healthPct)), barH);
    }
}
