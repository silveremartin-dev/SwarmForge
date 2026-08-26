/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.structure.Chamber;

/**
 * Stercoral Soil-Saliva-Feces Plastering System.
 * Models soil particle bonding with fecal-salivary cement by termites (Macrotermes / Reticulitermes)
 * to reinforce royal chamber ceilings against Mohr-Coulomb shear collapse under rain saturation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class StercoralCementSystem {

    public static void reinforceChamberCeiling(Chamber chamber, Individual worker) {
        if (chamber == null || worker == null || worker.getSpecies() == null) return;
        if (!worker.getSpecies().canMakeStercoralCement()) return;

        // Increase structural cohesion against soil moisture shear failure
        float currentStability = chamber.getStabilityFactor();
        chamber.setStabilityFactor(Math.min(2.5f, currentStability + 0.05f));
    }
}
