/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.epidemiology;

import java.io.Serializable;

/**
 * Stages of infection progression for an individual insect.
 */
public enum InfectionState implements Serializable {
    SUSCEPTIBLE,       // Healthy, vulnerable to exposure
    EXPOSED,           // Cuticular spores present, not yet germinated
    INFECTED,          // Germinated spores, internal hyphal growth
    SPORULATING_DEAD,  // Cadaver producing contagious infectious spores
    IMMUNE             // Developed social immunity via low-dose exposure
}
