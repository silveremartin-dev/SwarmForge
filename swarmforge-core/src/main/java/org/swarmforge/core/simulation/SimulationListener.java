/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

/**
 * Listener interface for simulation events.
 * Implementations receive callbacks for simulation lifecycle and updates.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface SimulationListener {

    /**
     * Called when simulation starts.
     */
    default void onStart(Simulation simulation) {
    }

    /**
     * Called when simulation pauses.
     */
    default void onPause(Simulation simulation) {
    }

    /**
     * Called when simulation stops.
     */
    default void onStop(Simulation simulation) {
    }

    /**
     * Called after each tick with the delta.
     */
    default void onTick(Simulation simulation, SimulationDelta delta) {
    }

    /**
     * Called when a new colony is added.
     */
    default void onColonyAdded(Simulation simulation, org.swarmforge.core.domain.Colony colony) {
    }

    /**
     * Called when an individual is born.
     */
    default void onIndividualBorn(org.swarmforge.core.domain.Individual individual) {
    }

    /**
     * Called when an individual dies.
     */
    default void onIndividualDied(org.swarmforge.core.domain.Individual individual) {
    }
}
