/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.evolution;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.species.Species;
import java.util.Random;

/**
 * Evolution Engine for genetic algorithms.
 * Modifies species traits over generations based on colony success.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EvolutionEngine {

    private final Random random = new Random();
    private static final float MUTATION_RATE = 0.05f;
    private static final float MUTATION_STRENGTH = 0.1f; // +/- 10% change

    /**
     * Evolve a new colony configuration from a parent colony.
     * Use when a colony swarms/founds a new nest.
     */
    public EvolvedTraits evolve(Colony parent, Species species) {
        // Base traits from species
        float speed = species.getWorkerSpeed();
        float view = species.getViewDistance();
        int lifespan = species.getWorkerLifespan();

        // Apply mutations
        if (random.nextFloat() < MUTATION_RATE) {
            speed *= (1.0f + (random.nextFloat() - 0.5f) * MUTATION_STRENGTH);
        }
        if (random.nextFloat() < MUTATION_RATE) {
            view *= (1.0f + (random.nextFloat() - 0.5f) * MUTATION_STRENGTH);
        }
        if (random.nextFloat() < MUTATION_RATE) {
            lifespan *= (1.0f + (random.nextFloat() - 0.5f) * MUTATION_STRENGTH);
        }

        return new EvolvedTraits(speed, view, lifespan);
    }

    public record EvolvedTraits(float speed, float viewDistance, int workerLifespan) {
    }
}
