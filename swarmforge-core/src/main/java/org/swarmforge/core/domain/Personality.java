/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.Random;

/**
 * Represents individual personality traits that influence behavior.
 * Based on "Five Factor Model" adapted for insects.
 *
 * @author Gemini AI Assistant
 */
public record Personality(
        float boldness, // 0-1: risk tolerance (0=cautious, 1=bold)
        float aggression, // 0-1: combat tendency
        float curiosity, // 0-1: exploration drive
        float sociability, // 0-1: preference for group activities
        float diligence // 0-1: work ethic, task persistence
) {

    /**
     * Generate random personality with normal distribution around 0.5.
     */
    public static Personality random(Random rng) {
        return new Personality(
                clamp((float) (rng.nextGaussian() * 0.2 + 0.5)),
                clamp((float) (rng.nextGaussian() * 0.2 + 0.5)),
                clamp((float) (rng.nextGaussian() * 0.2 + 0.5)),
                clamp((float) (rng.nextGaussian() * 0.2 + 0.5)),
                clamp((float) (rng.nextGaussian() * 0.2 + 0.5)));
    }

    /**
     * Default balanced personality.
     */
    public static Personality balanced() {
        return new Personality(0.5f, 0.5f, 0.5f, 0.5f, 0.5f);
    }

    /**
     * Create from parent personalities with crossover and mutation.
     */
    public static Personality inherit(Personality p1, Personality p2, Random rng) {
        float mutationRate = 0.1f;
        return new Personality(
                mutate(crossover(p1.boldness, p2.boldness, rng), mutationRate, rng),
                mutate(crossover(p1.aggression, p2.aggression, rng), mutationRate, rng),
                mutate(crossover(p1.curiosity, p2.curiosity, rng), mutationRate, rng),
                mutate(crossover(p1.sociability, p2.sociability, rng), mutationRate, rng),
                mutate(crossover(p1.diligence, p2.diligence, rng), mutationRate, rng));
    }

    private static float crossover(float a, float b, Random rng) {
        return rng.nextBoolean() ? a : b;
    }

    private static float mutate(float value, float rate, Random rng) {
        if (rng.nextFloat() < rate) {
            return clamp(value + (float) (rng.nextGaussian() * 0.1));
        }
        return value;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    /**
     * Is this individual a "bold explorer" type?
     */
    public boolean isBoldExplorer() {
        return boldness > 0.7f && curiosity > 0.6f;
    }

    /**
     * Is this individual a "cautious worker" type?
     */
    public boolean isCautiousWorker() {
        return boldness < 0.3f && diligence > 0.7f;
    }

    /**
     * Is this individual an "aggressive defender" type?
     */
    public boolean isAggressiveDefender() {
        return aggression > 0.7f && boldness > 0.5f;
    }
}
