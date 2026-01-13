/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.swarmforge.core.domain.Individual;

/**
 * A soldier ant species with enhanced defense and aggressive traits.
 */
public class SoldierAntSpecies implements Species {

    @Override
    public void configureIndividual(Individual individual) {
        individual.setMaxEnergy(150.0f);
        individual.setEnergy(150.0f);
        // Soldiers are typically slower but stronger
        // Behavioral logic could be set here if not using ECS defaults
    }

    @Override
    public String getScientificName() {
        return "Formica militaris";
    }

    @Override
    public String getCommonName() {
        return "Soldier Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 3000; // Soldiers might live less due to combat risk
    }

    @Override
    public int getQueenLifespan() {
        return 40000;
    }

    @Override
    public float getWorkerSpeed() {
        return 3.5f; // Slower than smart ants
    }

    @Override
    public float getViewDistance() {
        return 10.0f;
    }

    @Override
    public int getTypicalColonySize() {
        return 500;
    }
}
