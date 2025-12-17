/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.swarmforge.core.behavior.rl.RLArchitecture;
import org.swarmforge.core.domain.Individual;

/**
 * An ant species that uses Reinforcement Learning (Q-Learning) for decision
 * making.
 */
public class SmartAntSpecies implements Species {

    @Override
    public void configureIndividual(Individual individual) {
        individual.setReasoningArchitecture(new RLArchitecture());

        // Stats are derived from Species, so we might not need to set them on
        // Individual if Individual uses Species reference.
        // However, if we want per-individual Override:
        individual.setMaxEnergy(100.0f);
        // speed is typically derived from species * genome
        // viewDistance likewise
    }

    @Override
    public String getScientificName() {
        return "Formica intelligens";
    }

    @Override
    public String getCommonName() {
        return "Smart Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 5000;
    }

    @Override
    public int getQueenLifespan() {
        return 50000;
    }

    @Override
    public float getWorkerSpeed() {
        return 5.0f;
    }

    @Override
    public float getViewDistance() {
        return 15.0f;
    }

    @Override
    public int getTypicalColonySize() {
        return 1000;
    }
}
