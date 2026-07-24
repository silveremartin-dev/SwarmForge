/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Vespula germanica - German Wasp / Yellowjacket
 * Predatory paper wasp, annual colony cycle, high aggression.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class VespulaGermanica implements Species {

    @Override
    public String getScientificName() {
        return "Vespula germanica";
    }

    @Override
    public String getCommonName() {
        return "European Yellowjacket Wasp";
    }

    @Override
    public int getWorkerLifespan() {
        return 30 * 24; // ~30 days
    }

    @Override
    public int getQueenLifespan() {
        return 365; // ~1 year (annual colony cycle)
    }

    @Override
    public float getWorkerSpeed() {
        return 1.4f;
    }

    @Override
    public float getViewDistance() {
        return 6.0f;
    }

    @Override
    public float getAggression() {
        return 0.85f; // High aggression
    }

    @Override
    public int getTypicalColonySize() {
        return 4000;
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.WASP;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.INSECT,
                org.swarmforge.core.domain.ResourceType.SUGAR);
    }
}
