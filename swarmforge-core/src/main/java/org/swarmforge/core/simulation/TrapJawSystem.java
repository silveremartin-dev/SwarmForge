/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Trap-Jaw Mandible Catapult System.
 * Models elastic energy storage and ultra-fast release in Odontomachus / Anochetus mandibles (60 m/s)
 * enabling lethal strikes or catapulting backward escape jumps.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrapJawSystem {

    public static class TrapJawStrike {
        public final float damage;
        public final boolean isEscapeJump;

        public TrapJawStrike(float damage, boolean isEscapeJump) {
            this.damage = damage;
            this.isEscapeJump = isEscapeJump;
        }
    }

    public static TrapJawStrike executeMandibleSnap(Individual ant, boolean targetGroundForJump) {
        if (ant == null || ant.getSpecies() == null) return null;
        if (!ant.getSpecies().hasTrapJawMechanism()) return null;

        if (targetGroundForJump) {
            // Recoil catapult jump backward
            float backAngle = ant.getHeading() + (float) Math.PI;
            ant.setPosition(
                ant.getX() + (float) Math.cos(backAngle) * 5.0f,
                ant.getY() + (float) Math.sin(backAngle) * 5.0f,
                ant.getZ() + 1.5f
            );
            return new TrapJawStrike(0.0f, true);
        } else {
            // Ultra-fast strike (60 m/s) inflicting 120.0 damage
            return new TrapJawStrike(120.0f, false);
        }
    }
}
