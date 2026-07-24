/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Lasius niger - Black Garden Ant
 * Common European ant species, excellent for simulation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LasiusNiger implements Species {

    @Override
    public String getScientificName() {
        return "Lasius niger";
    }

    @Override
    public String getCommonName() {
        return "Black Garden Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 365 * 3;
    } // ~3 years

    @Override
    public int getQueenLifespan() {
        return 365 * 15;
    } // ~15 years

    @Override
    public float getWorkerSpeed() {
        return 0.5f;
    }

    @Override
    public float getViewDistance() {
        return 3.0f;
    }

    @Override
    public int getTypicalColonySize() {
        return 15000;
    }

    @Override
    public boolean formsMegaColonies() {
        return false;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.HONEYDEW,
                org.swarmforge.core.domain.ResourceType.INSECT,
                org.swarmforge.core.domain.ResourceType.SUGAR);
    }
}
