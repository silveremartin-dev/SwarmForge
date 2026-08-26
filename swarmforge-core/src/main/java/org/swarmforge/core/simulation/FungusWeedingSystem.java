/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Fungus Garden Microclimate & Escovopsis Mold Weeding System.
 * Models manual weeding of parasitic Escovopsis molds from Atta fungus gardens
 * and application of Pseudonocardia bacterial antibiotics secreted on ant cuticles.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FungusWeedingSystem {

    public static void weedGarden(FungusGarden garden, Individual worker) {
        if (garden == null || worker == null || worker.getSpecies() == null) return;
        if (!worker.getSpecies().canWeedFungusGarden()) return;

        // Apply Pseudonocardia antibiotic treatment to reduce contamination level
        float currentContamination = garden.getContaminationLevel();
        if (currentContamination > 0) {
            float cleaned = Math.min(currentContamination, 0.05f);
            garden.setContaminationLevel(currentContamination - cleaned);
        }
    }
}
