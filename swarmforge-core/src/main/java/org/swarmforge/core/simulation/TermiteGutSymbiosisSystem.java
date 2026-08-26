/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Termite (Reticulitermes / Macrotermes) Gut Symbiosis & Stercoral Cementing System.
 * Simulates:
 * 1. Lignocellulose digestion efficiency dependent on gut flagellate protists & bacterial endosymbionts.
 * 2. Proctodeal trophallaxis (anus-to-mouth transfer) to inoculate newly molted nymphs with endosymbionts lost during ecdysis.
 * 3. Stercoral Salivary Soil Cementing for constructing cathedral termite mounds.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TermiteGutSymbiosisSystem {

    /**
     * Calculates cellulose digestion energy yield based on gut flagellate density (0.0 to 1.0).
     */
    public static float calculateCelluloseDigestionYield(float ingestedWoodMass, float gutSymbiontDensity) {
        if (ingestedWoodMass <= 0.0f || gutSymbiontDensity <= 0.0f) return 0.0f;
        // High symbiont density allows 85% conversion of lignocellulose into short-chain fatty acids
        return ingestedWoodMass * gutSymbiontDensity * 0.85f;
    }

    /**
     * Executes proctodeal trophallaxis to re-inoculate a freshly molted nymph with flagellate gut symbionts.
     */
    public static float executeProctodealInoculation(Individual donorWorker, float nymphGutSymbiontDensity) {
        if (donorWorker == null || !donorWorker.isAlive()) return nymphGutSymbiontDensity;
        // Transfers gut flora restoring 100% symbiont density
        return 1.0f;
    }

    /**
     * Computes structural durability score of saliva-fecal soil cement (stercoral cement) for termite mound walls.
     */
    public static float calculateStercoralCementHardness(float clayPercentage, float salivaSalivaryBinding) {
        return (clayPercentage * 0.6f) + (salivaSalivaryBinding * 0.4f) * 1.5f;
    }
}
