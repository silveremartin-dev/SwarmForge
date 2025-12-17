/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Camponotus - Carpenter Ant
 * Large ants that nest in wood, primarily nocturnal foragers.
 */
public class Camponotus implements Species {

    @Override
    public String getScientificName() {
        return "Camponotus";
    }

    @Override
    public String getCommonName() {
        return "Carpenter Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 365 * 7;
    } // ~7 years

    @Override
    public int getQueenLifespan() {
        return 365 * 25;
    } // ~25 years

    @Override
    public float getWorkerSpeed() {
        return 0.45f;
    } // Slower, larger

    @Override
    public float getViewDistance() {
        return 3.5f;
    }

    @Override
    public int getTypicalColonySize() {
        return 10000;
    }

    @Override
    public boolean formsMegaColonies() {
        return false;
    }
}
