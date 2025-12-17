/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.plugin;

import org.swarmforge.core.simulation.Simulation;

/**
 * Context provided to plugins for accessing simulation.
 *
 * @author Gemini AI Assistant
 */
public class PluginContext {

    private final Simulation simulation;
    private final PluginManager manager;

    public PluginContext(Simulation simulation, PluginManager manager) {
        this.simulation = simulation;
        this.manager = manager;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public PluginManager getPluginManager() {
        return manager;
    }

    /**
     * Log a message from the plugin.
     */
    public void log(String pluginId, String message) {
        System.out.println("[Plugin:" + pluginId + "] " + message);
    }

    /**
     * Register a custom species.
     */
    public void registerSpecies(org.swarmforge.core.species.Species species) {
        // Species registry would go here
    }

    /**
     * Register a custom behavior architecture.
     */
    public void registerBehavior(String name,
            Class<? extends org.swarmforge.core.behavior.ReasoningArchitecture> behaviorClass) {
        // Behavior registry would go here
    }
}
