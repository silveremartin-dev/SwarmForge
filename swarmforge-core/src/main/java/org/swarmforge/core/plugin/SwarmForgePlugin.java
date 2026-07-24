/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.plugin;

/**
 * Plugin interface for extending SwarmForge.
 *
 * @author Gemini AI Assistant
 */
public interface SwarmForgePlugin {

    /**
     * Unique identifier for this plugin.
     */
    String getId();

    /**
     * Human-readable name.
     */
    String getName();

    /**
     * Plugin version.
     */
    String getVersion();

    /**
     * Called when plugin is loaded.
     */
    void onLoad(PluginContext context);

    /**
     * Called when plugin is unloaded.
     */
    void onUnload();

    /**
     * Called every simulation tick.
     */
    default void onTick(long tickNumber) {
    }
}
