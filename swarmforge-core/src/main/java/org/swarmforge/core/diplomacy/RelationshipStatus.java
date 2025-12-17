/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.diplomacy;

/**
 * Diplomacy status between two colonies.
 */
public enum RelationshipStatus {
    NEUTRAL, // Default: Ignore unless provoked
    ALLY, // Friends: Do not attack, share pheromones (future)
    ENEMY, // Hostile: Attack on sight
    TRADING // Neutral + Trade rights
}
