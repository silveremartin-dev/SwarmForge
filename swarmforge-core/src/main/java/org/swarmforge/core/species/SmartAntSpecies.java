/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.swarmforge.core.behavior.rl.RLArchitecture;
import org.swarmforge.core.domain.Individual;

/**
 * An ant species that uses Reinforcement Learning (Q-Learning) for decision making.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 */
public class SmartAntSpecies extends CustomSpecies {

    public SmartAntSpecies() {
        setPresetName("Smart Ant (RL)");
        setCommonName("Smart Ant");
        setScientificName("Formica intelligens");
        setInsectType("ANT");
        setDescription("RL-driven intelligent ant species.");
        setWorkerLifespan(5000);
        setQueenLifespan(50000);
        setWorkerSpeed(5.0f);
        setViewDistance(15.0f);
        setTypicalColonySize(1000);
    }

    @Override
    public void configureIndividual(Individual individual) {
        individual.setReasoningArchitecture(new RLArchitecture());
        individual.setMaxEnergy(100.0f);
    }
}
