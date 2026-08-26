/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Allogrooming & Fungal Spore Sanitization System.
 * Models mutual licking and spore removal (Metarhizium / Beauveria) between workers
 * prior to spore germination on the cuticular surface.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AllogroomingSystem {

    public static boolean groomNestmate(Individual groomer, Individual target) {
        if (groomer == null || target == null || groomer.getSpecies() == null) return false;
        if (!groomer.getSpecies().canPerformAllogrooming()) return false;

        // Mechanical removal of fungal spores & parasitic mites
        target.setHealth(Math.min(100.0f, target.getHealth() + 2.0f));
        return true;
    }
}
