/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Worker Parasite Encirclement Quarantine System.
 * Models workers forming dense physical rings around intruding nest mites, small hive beetles, or myrmecophilous parasites to pin them to gallery walls.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ParasiteQuarantineSystem {

    public static boolean quarantineParasite(Individual guard, int surroundingGuardsCount) {
        if (guard == null || guard.getSpecies() == null) return false;
        if (!guard.getSpecies().canQuarantineInvasiveParasites()) return false;

        return surroundingGuardsCount >= 6; // Pin parasite in gallery corner with 6+ guards
    }
}
