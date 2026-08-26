/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Proctodeal Trophallaxis & Gut Microbiome Transfer System.
 * Models anal fluid exchange in termites (Reticulitermes, Cryptotermes) transferring
 * essential cellulolytist flagellate protozoa to newly molted workers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ProctodealTrophallaxisSystem {

    public static boolean transferGutSymbionts(Individual donor, Individual recipient) {
        if (donor == null || recipient == null || donor.getSpecies() == null) return false;
        if (!donor.getSpecies().hasProctodealTrophallaxis()) return false;

        // Transfer cellulolytic flagellates
        recipient.setEnergy(Math.min(1.0f, recipient.getEnergyLevel() + 0.20f));
        return true;
    }
}
