/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Apis mellifera - Western Honey Bee
 * Eusocial aerial nest builder, dances to communicate nectar locations, forms winter thermal clusters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ApisMellifera implements Species {

    @Override
    public String getScientificName() {
        return "Apis mellifera";
    }

    @Override
    public String getCommonName() {
        return "Western Honey Bee";
    }

    @Override
    public int getWorkerLifespan() {
        return 60 * 24 * 45; // ~45 days in summer
    }

    @Override
    public int getQueenLifespan() {
        return 365 * 4; // ~4 years
    }

    @Override
    public float getWorkerSpeed() {
        return 1.2f; // Fast aerial flight
    }

    @Override
    public float getViewDistance() {
        return 8.0f; // High visual acuity
    }

    @Override
    public int getTypicalColonySize() {
        return 50000;
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.BEE;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.NECTAR,
                org.swarmforge.core.domain.ResourceType.SUGAR);
    }
}
