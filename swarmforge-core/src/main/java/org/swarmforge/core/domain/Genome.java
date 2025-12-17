/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.Random;
import java.util.UUID;

/**
 * Represents an individual's genetic traits that are inherited.
 * Enables evolution over generations.
 *
 * @author Gemini AI Assistant
 */
public record Genome(
        UUID id,
        UUID parent1Id, // null for initial generation
        UUID parent2Id, // null if asexual
        int generation,

        // Physical traits
        float size, // 0.8-1.2 multiplier
        float speed, // 0.8-1.2 multiplier
        float strength, // 0.8-1.2 multiplier
        float lifespan, // 0.8-1.2 multiplier

        // Sensory traits
        float pheromoneReception, // 0.8-1.2
        float visualAcuity, // 0.8-1.2

        // Personality (inherited)
        Personality personality) {

    /**
     * Create initial generation genome.
     */
    public static Genome initial(Random rng) {
        return new Genome(
                UUID.randomUUID(),
                null, null, 0,
                randomTrait(rng), randomTrait(rng), randomTrait(rng), randomTrait(rng),
                randomTrait(rng), randomTrait(rng),
                Personality.random(rng));
    }

    /**
     * Create offspring genome from two parents.
     */
    public static Genome reproduce(Genome p1, Genome p2, Random rng) {
        float mutationChance = 0.1f;
        return new Genome(
                UUID.randomUUID(),
                p1.id, p2.id,
                Math.max(p1.generation, p2.generation) + 1,
                mutate(crossover(p1.size, p2.size, rng), mutationChance, rng),
                mutate(crossover(p1.speed, p2.speed, rng), mutationChance, rng),
                mutate(crossover(p1.strength, p2.strength, rng), mutationChance, rng),
                mutate(crossover(p1.lifespan, p2.lifespan, rng), mutationChance, rng),
                mutate(crossover(p1.pheromoneReception, p2.pheromoneReception, rng), mutationChance, rng),
                mutate(crossover(p1.visualAcuity, p2.visualAcuity, rng), mutationChance, rng),
                Personality.inherit(p1.personality, p2.personality, rng));
    }

    /**
     * Asexual reproduction (clone with mutation).
     */
    public Genome clone(Random rng) {
        float mutationChance = 0.05f;
        return new Genome(
                UUID.randomUUID(),
                this.id, null,
                this.generation + 1,
                mutate(size, mutationChance, rng),
                mutate(speed, mutationChance, rng),
                mutate(strength, mutationChance, rng),
                mutate(lifespan, mutationChance, rng),
                mutate(pheromoneReception, mutationChance, rng),
                mutate(visualAcuity, mutationChance, rng),
                Personality.inherit(personality, personality, rng));
    }

    private static float randomTrait(Random rng) {
        return (float) (0.8 + rng.nextGaussian() * 0.1 + 0.2);
    }

    private static float crossover(float a, float b, Random rng) {
        // Blend crossover
        float alpha = rng.nextFloat();
        return a * alpha + b * (1 - alpha);
    }

    private static float mutate(float value, float chance, Random rng) {
        if (rng.nextFloat() < chance) {
            return Math.max(0.5f, Math.min(1.5f, value + (float) (rng.nextGaussian() * 0.05)));
        }
        return value;
    }

    /**
     * Calculate genetic fitness score (higher = better adapted).
     */
    public float getFitness() {
        return (size + speed + strength + lifespan + pheromoneReception + visualAcuity) / 6f;
    }
}
