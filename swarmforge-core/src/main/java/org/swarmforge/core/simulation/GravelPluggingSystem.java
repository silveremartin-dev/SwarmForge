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
 * Antiseptic Chamber Gravel Plugging System.
 * Models sanitary sealing of parasite/pathogen infected nest chambers using gravel particles
 * coated with metapleural gland antibiotic secretions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GravelPluggingSystem {

    public static boolean sealContaminatedChamber(Individual worker, Chamber chamber) {
        if (worker == null || chamber == null || worker.getSpecies() == null) return false;
        if (!worker.getSpecies().canPlugContaminatedGalleries()) return false;

        // If chamber contamination exceeds safety threshold
        if (chamber.getContaminationLevel() > 0.6f) {
            chamber.setStabilityFactor(chamber.getStabilityFactor() + 0.25f);
            chamber.setContaminationLevel(0.0f); // Sealed off from nest rest
            return true;
        }
        return false;
    }
}
