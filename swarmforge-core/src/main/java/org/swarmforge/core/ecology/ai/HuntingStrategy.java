/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecology.ai;

import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.simulation.Simulation;

/**
 * Strategy interface for predator hunting behaviors.
 */
public interface HuntingStrategy {

    /**
     * Update the predator's behavior for this tick.
     * 
     * @param predator   The predator using this strategy
     * @param simulation Access to the world and prey
     */
    void update(Predator predator, Simulation simulation);
}
