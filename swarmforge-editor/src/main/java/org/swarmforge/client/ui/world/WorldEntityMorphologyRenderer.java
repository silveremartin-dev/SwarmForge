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
        PredatorMorphologyRenderer.drawRealisticPredator(gc, px, py, heading, type, healthPct, baseR);
    }
}