/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Oleic Acid Threshold Necrophoresis System.
 * Models corpse detection and transport based on cuticular lipid oxidation releasing oleic acid.
 * Workers carry corpses to refuse pits only when oleic acid concentration exceeds the perception threshold.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class OleicAcidNecrophoresisSystem {

    public static boolean triggerNecrophoresisTransport(Individual undertaker, Individual corpse, float hoursDead) {
        if (undertaker == null || corpse == null || undertaker.getSpecies() == null) return false;
        if (corpse.isAlive()) return false;

        // Oleic acid builds up as cuticular lipids decompose (seuil ~4 hours)
        float oleicAcidConcentration = hoursDead * 0.25f;
        if (oleicAcidConcentration >= 1.0f && undertaker.getSpecies().hasOleicAcidThresholdNecrophoresis()) {
            return true; // Undertaker carries corpse to cemetery/refuse trench
        }
        return false;
    }
}
