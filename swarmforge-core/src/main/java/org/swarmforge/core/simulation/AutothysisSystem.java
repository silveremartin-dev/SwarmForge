/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Suicidal Chemical Explosion (Autothysis) System.
 * Models suicidal abdominal wall rupture in Colobopsis explodens ants and Globitermes termites
 * releasing sticky toxic adhesive slime to entrap and kill hostile invaders.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AutothysisSystem {

    public static class AutothysisExplosion {
        public final float x, y, z;
        public final float blastRadius = 2.0f;
        public final float stickySlimeToxicity = 50.0f;

        public AutothysisExplosion(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static AutothysisExplosion triggerSuicidalExplosion(Individual worker) {
        if (worker == null || worker.getSpecies() == null) return null;
        if (!worker.getSpecies().hasAutothysis()) return null;

        AutothysisExplosion explosion = new AutothysisExplosion(worker.getX(), worker.getY(), worker.getZ());
        worker.die(); // Immediate suicidal mortality
        return explosion;
    }
}
