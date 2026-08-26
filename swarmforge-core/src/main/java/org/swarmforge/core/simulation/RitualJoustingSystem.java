/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Ritualized Tournament Jousting System.
 * Models non-lethal intimidation tournaments (Myrmecocystus honeypot ants)
 * where workers tiptoe and raise abdomens to establish territorial dominance without casualties.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RitualJoustingSystem {

    public static boolean engageRitualJousting(Individual ant1, Individual ant2) {
        if (ant1 == null || ant2 == null || ant1.getSpecies() == null) return false;
        if (!ant1.getSpecies().canPerformRitualJousting()) return false;

        // Non-lethal posture intimidation: larger ant wins territorial boundary
        return ant1.getMandibleWear() <= ant2.getMandibleWear();
    }
}
