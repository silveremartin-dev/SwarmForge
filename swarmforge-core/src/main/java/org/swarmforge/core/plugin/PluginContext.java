/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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

    private final java.util.Map<String, org.swarmforge.core.species.Species> speciesRegistry = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Class<? extends org.swarmforge.core.behavior.ReasoningArchitecture>> behaviorRegistry = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, NestArchitecture> nestRegistry = new java.util.concurrent.ConcurrentHashMap<>();

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
        speciesRegistry.put(species.getScientificName(), species);
        log("PluginRegistry", "Registered species: " + species.getScientificName());
    }

    /**
     * Register a custom behavior architecture.
     */
    public void registerBehavior(String name,
            Class<? extends org.swarmforge.core.behavior.ReasoningArchitecture> behaviorClass) {
        behaviorRegistry.put(name, behaviorClass);
        log("PluginRegistry", "Registered behavior: " + name);
    }

    /**
     * Register a custom nest architecture for a plugin species.
     */
    public void registerNestArchitecture(NestArchitecture nestArchitecture) {
        nestRegistry.put(nestArchitecture.getId(), nestArchitecture);
        log("PluginRegistry", "Registered nest architecture: " + nestArchitecture.getName() + " [" + nestArchitecture.getId() + "]");
    }

    public java.util.Map<String, org.swarmforge.core.species.Species> getSpeciesRegistry() {
        return java.util.Collections.unmodifiableMap(speciesRegistry);
    }

    public java.util.Map<String, NestArchitecture> getNestRegistry() {
        return java.util.Collections.unmodifiableMap(nestRegistry);
    }
}
