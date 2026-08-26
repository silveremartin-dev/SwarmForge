/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Phragmosis & Head-Door Blockade System.
 * Models specialized major soldiers (Cephalotes, Colobopsis) using their truncated disc-shaped head
 * as a living plug to seal gallery entrances against hostile raiding ants.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PhragmosisSystem {

    public static class EntranceGate {
        private boolean pluggedByPhragmotics = false;

        public void sealEntrance(Individual soldier) {
            if (soldier == null || soldier.getSpecies() == null) return;
            if (!soldier.getSpecies().canPerformPhragmosis()) return;

            this.pluggedByPhragmotics = true;
        }

        public void unsealEntrance() {
            this.pluggedByPhragmotics = false;
        }

        public boolean isPlugged() { return pluggedByPhragmotics; }
    }
}
