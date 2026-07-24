/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Reticulitermes flavipes - Eastern Subterranean Termite
 * Subterranean eusocial insect, builds shelter tubes from soil and fecal cement, feeds on cellulose.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ReticulitermesFlavipes implements Species {

    @Override
    public String getScientificName() {
        return "Reticulitermes flavipes";
    }

    @Override
    public String getCommonName() {
        return "Eastern Subterranean Termite";
    }

    @Override
    public int getWorkerLifespan() {
        return 365 * 2; // ~2 years
    }

    @Override
    public int getQueenLifespan() {
        return 365 * 25; // ~25 years
    }

    @Override
    public float getWorkerSpeed() {
        return 0.35f; // Slow underground movement
    }

    @Override
    public float getViewDistance() {
        return 1.5f; // Eyeless / low vision, relies on tactile and chemical cues
    }

    @Override
    public int getTypicalColonySize() {
        return 250000;
    }

    @Override
    public InsectOrder getInsectOrder() {
        return InsectOrder.TERMITE;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.MULCH,
                org.swarmforge.core.domain.ResourceType.FUNGUS);
    }
}
