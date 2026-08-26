/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Dulosis & Slave-Making Raid System.
 * Models raids by Amazon ants (Polyergus rufescens) targeting host nests (Serviformica)
 * to steal pupae and maintain enslaved worker labor for nest maintenance.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DulosisRaidSystem {

    public static boolean executePupaeRaid(Colony raiderColony, Colony hostColony, Individual raider) {
        if (raiderColony == null || hostColony == null || raider == null || raider.getSpecies() == null) return false;
        if (!raider.getSpecies().isSlaveMakingSpecies()) return false;

        // Steal pupa from host colony brood chamber
        if (hostColony.getBroodCount() > 0) {
            hostColony.decrementBroodCount();
            raiderColony.incrementEnslavedPupaeCount();
            return true;
        }
        return false;
    }
}
