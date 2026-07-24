/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.genetics;

import java.util.Random;

/**
 * Defines the genetic traits of an individual.
 * Supports mutation and crossover for evolutionary algorithms.
 */
public class Genome {
    private static final Random RANDOM = new Random();

    // Traits (Multipliers centered around 1.0)
    private float speedMultiplier;
    private float strengthMultiplier;
    private float metabolismRate;
    private float aggressionLevel;

    public Genome() {
        // Standard genome
        this.speedMultiplier = 1.0f;
        this.strengthMultiplier = 1.0f;
        this.metabolismRate = 1.0f;
        this.aggressionLevel = 0.5f;
    }

    private Genome(float speed, float strength, float metabolism, float aggression) {
        this.speedMultiplier = speed;
        this.strengthMultiplier = strength;
        this.metabolismRate = metabolism;
        this.aggressionLevel = aggression;
    }

    public void mutate(float mutationRate) {
        if (RANDOM.nextFloat() < mutationRate) {
            speedMultiplier += (RANDOM.nextFloat() - 0.5f) * 0.1f;
        }
        if (RANDOM.nextFloat() < mutationRate) {
            strengthMultiplier += (RANDOM.nextFloat() - 0.5f) * 0.1f;
        }
        if (RANDOM.nextFloat() < mutationRate) {
            metabolismRate += (RANDOM.nextFloat() - 0.5f) * 0.1f;
        }
        // Clamp values
        speedMultiplier = Math.max(0.5f, speedMultiplier);
        strengthMultiplier = Math.max(0.5f, strengthMultiplier);
        metabolismRate = Math.max(0.5f, metabolismRate);
    }

    public static Genome crossover(Genome p1, Genome p2) {
        return new Genome(
                RANDOM.nextBoolean() ? p1.speedMultiplier : p2.speedMultiplier,
                RANDOM.nextBoolean() ? p1.strengthMultiplier : p2.strengthMultiplier,
                RANDOM.nextBoolean() ? p1.metabolismRate : p2.metabolismRate,
                RANDOM.nextBoolean() ? p1.aggressionLevel : p2.aggressionLevel);
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getStrengthMultiplier() {
        return strengthMultiplier;
    }

    public float getMetabolismRate() {
        return metabolismRate;
    }

    public float getAggressionLevel() {
        return aggressionLevel;
    }
}
