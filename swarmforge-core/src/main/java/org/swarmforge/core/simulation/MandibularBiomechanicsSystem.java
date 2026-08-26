/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Mandibular Biomechanics & Mandible Tooth Wear System.
 * Simulates mandibular cutting force (F_mandible = muscle_cross_section * mechanical_advantage),
 * progressive tooth wear against hard mineral substrates (quartz, clay),
 * and the resulting transition of worn-out foragers back into internal nurse roles (age polyethism).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MandibularBiomechanicsSystem {

    /**
     * Calculates mandibular bite force in Millinewtons (mN).
     */
    public static float calculateBiteForcemN(float headWidthMm, float muscleFactor) {
        // Bite force scales cubically with head width in soldier castes (e.g. Atta, Pheidole)
        return (float) (Math.pow(headWidthMm, 2.5) * 12.0f * muscleFactor);
    }

    /**
     * Updates mandibular wear level after digging in abrasive substrate.
     */
    public static float applyMandibleWear(float currentWear, float substrateHardness) {
        // Wear increases faster in hard rock/quartz than in soft earth
        float wearIncrement = 0.001f * substrateHardness;
        return Math.min(1.0f, currentWear + wearIncrement);
    }

    /**
     * Evaluates if worker mandible wear (>0.75) requires age polyethism job transition to brood nursing.
     */
    public static boolean requiresRetirementToNurse(float mandibleWear) {
        return mandibleWear >= 0.75f;
    }
}
