/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.disasters;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

/**
 * Interface for environmental disasters and events.
 * Can be weather-based (Storm, Snow) or physical (Fire, Earthquake).
 */
public interface DisasterEvent {
    String getName();

    String getSeverity(); // "MINOR", "MAJOR", "CATASTROPHIC"

    /**
     * Apply the disaster's effect to the simulation world.
     */
    void trigger(Simulation simulation, Terrarium terrarium);
}
