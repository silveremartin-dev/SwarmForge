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
 * Can be weather-based (Storm, Heatwave, Drought) or physical (Fire, Earthquake, Flood).
 * Supports multi-tick duration and intensity-scaled progressive damage.
 */
public interface DisasterEvent {
    String getName();

    String getSeverity(); // "MINOR", "MAJOR", "CATASTROPHIC"

    default float getIntensity() {
        return 0.5f;
    }

    default int getDurationTicks() {
        return 1;
    }

    default int getRemainingTicks() {
        return 0;
    }

    default boolean isFinished() {
        return getRemainingTicks() <= 0;
    }

    /**
     * Initial trigger call when disaster starts.
     */
    void trigger(Simulation simulation, Terrarium terrarium);

    /**
     * Progressive per-tick execution over disaster duration.
     */
    default void tick(Simulation simulation, Terrarium terrarium) {
        // Default: single-tick instantaneous execution
    }
}
