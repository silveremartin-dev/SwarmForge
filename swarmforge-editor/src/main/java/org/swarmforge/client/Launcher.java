/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

/**
 * Launcher class to bypass JavaFX module path requirements when running from
 * classpath.
 * This class does NOT extend Application, allowing the JVM to load JavaFX
 * classes from the classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        SwarmForgeClient.main(args);
    }
}
