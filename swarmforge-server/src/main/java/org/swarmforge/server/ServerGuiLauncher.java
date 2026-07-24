/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

/**
 * Entry point launcher for ServerGuiApp.
 * Bypasses JavaFX 11+ runtime component check when running from non-modular classpath.
 */
public class ServerGuiLauncher {

    public static void main(String[] args) {
        ServerGuiApp.main(args);
    }
}
