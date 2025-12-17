/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

/**
 * Launcher that bypasses JavaFX module system requirements.
 */
public class ClientLauncher {
    public static void main(String[] args) {
        SwarmForgeClient.main(args);
    }
}
