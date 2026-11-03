/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.swarmforge.core.domain.PredatorType;

/**
 * Renders high-fidelity anatomical morphologies for macro-faunal predators
 * (Arachnids, Birds, Woodpeckers, Lizards, Hymenopteran wasps, Caterpillars, Beetles, and Antlions).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class PredatorMorphologyRenderer {

    private PredatorMorphologyRenderer() {
    }

    public static void drawRealisticPredator(GraphicsContext gc, double px, double py, double heading,
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
                    double clawX = elbowX + pxH * (r * 0.5 * side) + cosH * (f * r * 0.4);
                    double clawY = elbowY + pyH * (r * 0.5 * side) + sinH * (f * r * 0.4);
                    gc.strokeLine(rootX, rootY, elbowX, elbowY);
                    gc.strokeLine(elbowX, elbowY, clawX, clawY);
                }
            }

            // 3. Sinuous Body
            gc.setFill(bodyColor);
            gc.fillOval(px - r * 1.1, py - r * 0.5, r * 2.2, r * 1.0);
            gc.setStroke(Color.web("#052e16"));
            gc.setLineWidth(1.2);
            gc.strokeOval(px - r * 1.1, py - r * 0.5, r * 2.2, r * 1.0);

            // 4. Triangular Head
            double headX = px + cosH * (r * 1.1);
            double headY = py + sinH * (r * 1.1);
            gc.setFill(bodyColor.darker());
            gc.fillPolygon(
                new double[]{headX + cosH * (r * 0.6), headX + pxH * (r * 0.45) - cosH * (r * 0.2), headX - pxH * (r * 0.45) - cosH * (r * 0.2)},
                new double[]{headY + sinH * (r * 0.6), headY + pyH * (r * 0.45) - sinH * (r * 0.2), headY - pyH * (r * 0.45) - sinH * (r * 0.2)},
                3
            );
            gc.strokePolygon(
                new double[]{headX + cosH * (r * 0.6), headX + pxH * (r * 0.45) - cosH * (r * 0.2), headX - pxH * (r * 0.45) - cosH * (r * 0.2)},
                new double[]{headY + sinH * (r * 0.6), headY + pyH * (r * 0.45) - sinH * (r * 0.2), headY - pyH * (r * 0.45) - sinH * (r * 0.2)},
                3
            );

        } else if (type == PredatorType.WASP || type == PredatorType.ASIAN_HORNET || type == PredatorType.BEE_WOLF) {
            // ── HYMENOPTERAN WASP / HORNET (Striped Gaster + Translucent Wings + Thread Waist) ──
            gc.setFill(Color.web("rgba(224, 242, 254, 0.55)"));
            gc.setStroke(Color.web("rgba(186, 230, 253, 0.8)"));
            gc.setLineWidth(1.0);
            for (int side : new int[]{1, -1}) {
                double wRootX = px - cosH * (r * 0.1) + pxH * (r * 0.35 * side);
                double wRootY = py - sinH * (r * 0.1) + pyH * (r * 0.35 * side);
                double wAng = heading + 1.25 * side;
                double wTipX = wRootX + Math.cos(wAng) * (r * 1.6);
                double wTipY = wRootY + Math.sin(wAng) * (r * 1.6);
                gc.fillOval(wTipX - r * 0.6, wTipY - r * 0.35, r * 1.2, r * 0.7);
                gc.strokeOval(wTipX - r * 0.6, wTipY - r * 0.35, r * 1.2, r * 0.7);
            }

            // Gaster
            double gasterX = px - cosH * (r * 0.95);
            double gasterY = py - sinH * (r * 0.95);
            gc.setFill(bodyColor);
            gc.fillOval(gasterX - r * 0.85, gasterY - r * 0.65, r * 1.7, r * 1.3);

            // Aposematic Warning Stripes
            gc.setStroke(Color.web("#09090b"));
            gc.setLineWidth(Math.max(1.5, r * 0.2));
            for (int s = -1; s <= 1; s++) {
                double sDist = s * (r * 0.32);
                gc.strokeLine(
                    gasterX + cosH * sDist + pxH * (r * 0.55), gasterY + sinH * sDist + pyH * (r * 0.55),
                    gasterX + cosH * sDist - pxH * (r * 0.55), gasterY + sinH * sDist - pyH * (r * 0.55)
                );
            }

            // Stinger
            gc.setStroke(Color.web("#09090b"));
            gc.setLineWidth(1.2);
            gc.strokeLine(gasterX - cosH * (r * 0.85), gasterY - sinH * (r * 0.85), gasterX - cosH * (r * 1.25), gasterY - sinH * (r * 1.25));

            // Thorax
            gc.setFill(Color.web("#0f172a"));
            gc.fillOval(px - r * 0.6, py - r * 0.5, r * 1.2, r * 1.0);

            // Head & Large Ocelli
            double headX = px + cosH * (r * 0.75);
            double headY = py + sinH * (r * 0.75);
            gc.setFill(bodyColor);
            gc.fillOval(headX - r * 0.45, headY - r * 0.45, r * 0.9, r * 0.9);
            gc.setFill(Color.web("#09090b"));
            gc.fillOval(headX + pxH * (r * 0.32) - 1.2, headY + pyH * (r * 0.32) - 1.2, 2.4, 2.4);
            gc.fillOval(headX - pxH * (r * 0.32) - 1.2, headY - pyH * (r * 0.32) - 1.2, 2.4, 2.4);

        } else if (type == PredatorType.ANTLION) {
            // ── ANTLION LARVA (Plump teardrop abdomen + Gigantic Serrated Mandibles) ──
            gc.setStroke(Color.web("#78350f"));
            gc.setLineWidth(Math.max(1.8, r * 0.3));
            double mandRootX = px + cosH * (r * 0.75);
            double mandRootY = py + sinH * (r * 0.75);
            for (int side : new int[]{1, -1}) {
                double tipX = mandRootX + cosH * (r * 1.2) + pxH * (r * 0.75 * side);
                double tipY = mandRootY + sinH * (r * 1.2) + pyH * (r * 0.75 * side);
                gc.strokeLine(mandRootX, mandRootY, tipX, tipY);
                // Serration spikes
                for (double f = 0.3; f <= 0.8; f += 0.25) {
                    double spX = mandRootX + (tipX - mandRootX) * f;
                    double spY = mandRootY + (tipY - mandRootY) * f;
                    gc.strokeLine(spX, spY, spX - pxH * (r * 0.25 * side), spY - pyH * (r * 0.25 * side));
                }
            }

            // Abdomen (Subterranean plump oval)
            gc.setFill(bodyColor);
            gc.fillOval(px - r * 1.1, py - r * 0.9, r * 2.2, r * 1.8);
            gc.setStroke(Color.web("#451a03"));
            gc.setLineWidth(1.2);
            gc.strokeOval(px - r * 1.1, py - r * 0.9, r * 2.2, r * 1.8);

            // Small flattened head
            gc.setFill(Color.web("#451a03"));
            gc.fillOval(mandRootX - r * 0.35, mandRootY - r * 0.35, r * 0.7, r * 0.7);

        } else if (type == PredatorType.CATERPILLAR) {
            // ── CATERPILLAR / MYRMECOPHILE (7 overlapping rounded abdominal segments + prolegs) ──
            gc.setStroke(Color.web("#365314"));
            gc.setLineWidth(1.0);
            for (int seg = -3; seg <= 3; seg++) {
                double segX = px + cosH * (seg * r * 0.38);
                double segY = py + sinH * (seg * r * 0.38);
                double segR = r * (0.65 - Math.abs(seg) * 0.05);

                gc.setFill(seg % 2 == 0 ? bodyColor : bodyColor.brighter());
                gc.fillOval(segX - segR, segY - segR, segR * 2, segR * 2);
                gc.strokeOval(segX - segR, segY - segR, segR * 2, segR * 2);

                // Spiracles / Spots
                gc.setFill(Color.web("#ea580c"));
                gc.fillOval(segX + pxH * (segR * 0.7) - 0.9, segY + pyH * (segR * 0.7) - 0.9, 1.8, 1.8);
                gc.fillOval(segX - pxH * (segR * 0.7) - 0.9, segY - pyH * (segR * 0.7) - 0.9, 1.8, 1.8);
            }

        } else {
            // ── DEFAULT ROBUST BEETLE / GENERAL MACRO-PREDATOR ──
            gc.setStroke(Color.web("#0f172a"));
            gc.setLineWidth(Math.max(1.2, r * 0.2));
            for (int i = 0; i < 3; i++) {
                for (int side : new int[]{1, -1}) {
                    double lRootX = px + cosH * ((i - 1) * r * 0.4) + pxH * (r * 0.55 * side);
                    double lRootY = py + sinH * ((i - 1) * r * 0.4) + pyH * (r * 0.55 * side);
                    double kneeX = lRootX + pxH * (r * 0.65 * side);
                    double kneeY = lRootY + pyH * (r * 0.65 * side);
                    double tipX = kneeX + pxH * (r * 0.6 * side) + cosH * (i == 0 ? r * 0.4 : -r * 0.4);
                    double tipY = kneeY + pyH * (r * 0.6 * side) + sinH * (i == 0 ? r * 0.4 : -r * 0.4);
                    gc.strokeLine(lRootX, lRootY, kneeX, kneeY); gc.strokeLine(kneeX, kneeY, tipX, tipY);
                }
            }
            gc.setFill(bodyColor); gc.fillOval(px - r * 0.9, py - r * 0.75, r * 1.8, r * 1.5);
            gc.setStroke(Color.web("#022c22")); gc.setLineWidth(1.2); gc.strokeOval(px - r * 0.9, py - r * 0.75, r * 1.8, r * 1.5);
            gc.setLineWidth(1.0); gc.strokeLine(px - cosH * (r * 0.9), py - sinH * (r * 0.9), px + cosH * (r * 0.4), py + sinH * (r * 0.4));
            double headX = px + cosH * (r * 0.85), headY = py + sinH * (r * 0.85), headR = r * 0.45;
            gc.setFill(bodyColor.darker()); gc.fillOval(headX - headR, headY - headR, headR * 2, headR * 2);
            gc.setFill(Color.web("#09090b"));
            gc.fillOval(headX + pxH * (headR * 0.5) - 1.2, headY + pyH * (headR * 0.5) - 1.2, 2.4, 2.4);
            gc.fillOval(headX - pxH * (headR * 0.5) - 1.2, headY - pyH * (headR * 0.5) - 1.2, 2.4, 2.4);
        }
        double barW = Math.max(16.0, r * 2.2), barH = 3.5, barX = px - barW / 2.0, barY = py - r - 7.0;
        gc.setFill(Color.web("rgba(15, 23, 42, 0.85)")); gc.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
        gc.setFill(healthPct > 0.5 ? Color.web("#22c55e") : (healthPct > 0.25 ? Color.web("#f59e0b") : Color.web("#ef4444")));
        gc.fillRect(barX, barY, barW * Math.max(0.0, Math.min(1.0, healthPct)), barH);
    }
}
