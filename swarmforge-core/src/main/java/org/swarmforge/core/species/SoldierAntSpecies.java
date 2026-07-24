/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.swarmforge.core.domain.Individual;

/**
 * A soldier ant species with enhanced defense and aggressive traits.
 * Refactored to extend CustomSpecies for full JSON & Species Editor parameter compatibility.
 */
public class SoldierAntSpecies extends CustomSpecies {

    public SoldierAntSpecies() {
        setPresetName("Soldier Ant");
        setCommonName("Soldier Ant");
        setScientificName("Formica militaris");
        setInsectType("ANT");
        setDescription("Soldier ant species with enhanced defense and aggressive traits.");
        setWorkerLifespan(3000);
        setQueenLifespan(40000);
        setWorkerSpeed(3.5f);
        setViewDistance(10.0f);
        setTypicalColonySize(500);
        setAggression(0.9f);
        setStrength(20.0f);
    }

    @Override
    public void configureIndividual(Individual individual) {
        individual.setMaxEnergy(150.0f);
        individual.setEnergy(150.0f);
        individual.setMaxHealth(200.0f);
        individual.setHealth(200.0f);
        individual.setAttackDamage(20.0f);
        individual.setDefense(5.0f);
    }
}
