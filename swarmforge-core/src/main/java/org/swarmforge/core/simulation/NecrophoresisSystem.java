/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Necrophoresis & Oleic Acid Sanitation System.
 * Simulates post-mortem lipid breakdown emitting oleic acid and linoleic acid after ~24-48 hours.
 * Specialized undertaker workers detect oleic acid signals, pick up corpses, and carry them
 * to external or designated subterranean refuse dumps (kitchen middens) to prevent pathogen outbreaks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NecrophoresisSystem {

    public static final float OLEIC_ACID_THRESHOLD = 0.40f;

    /**
     * Calculates current oleic acid emission level of a deceased individual based on ticks since death.
     */
    public static float calculateOleicAcidEmission(long currentTick, long deathTick) {
        long ticksDead = currentTick - deathTick;
        if (ticksDead < 500) return 0.0f; // Fresh corpse (no oleic acid yet)
        // Oleic acid builds up exponentially after 500 ticks (~8 minutes of sim time)
        return Math.min(1.0f, (ticksDead - 500) / 1500.0f);
    }

    /**
     * Evaluates if a worker should pick up a corpse for necrophoric transport.
     */
    public static boolean shouldTransportCorpse(Individual undertaker, float oleicAcidLevel) {
        if (undertaker == null || !undertaker.isAlive() || undertaker.getCarriedItem() != Individual.CarriedItem.NONE) {
            return false;
        }
        return oleicAcidLevel >= OLEIC_ACID_THRESHOLD;
    }
}
