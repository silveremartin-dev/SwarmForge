/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Trophic Inviable Egg Cannibalism System.
 * Models nurse workers consuming unfertilized, trophic, or damaged eggs to recycle protein during starvation periods.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EggCannibalismSystem {

    public static boolean recycleEgg(Individual nurse, boolean isEggInviable) {
        if (nurse == null || nurse.getSpecies() == null) return false;
        if (!nurse.getSpecies().canRecycleInviableEggs()) return false;

        if (isEggInviable && nurse.getHunger() > 20.0f) {
            nurse.setHunger(Math.max(0.0f, nurse.getHunger() - 25.0f));
            return true;
        }
        return false;
    }
}
