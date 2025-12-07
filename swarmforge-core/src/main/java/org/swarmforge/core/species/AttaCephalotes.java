/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Atta cephalotes - Leafcutter Ant
 * Complex fungus-farming species from South America.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AttaCephalotes implements Species {

    @Override
    public String getScientificName() {
        return "Atta cephalotes";
    }

    @Override
    public String getCommonName() {
        return "Leafcutter Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 365 * 2;
    }

    @Override
    public int getQueenLifespan() {
        return 365 * 20;
    } // Up to 20 years

    @Override
    public float getWorkerSpeed() {
        return 0.6f;
    }

    @Override
    public float getViewDistance() {
        return 4.0f;
    }

    @Override
    public int getTypicalColonySize() {
        return 5000000;
    } // Massive colonies

    @Override
    public boolean formsMegaColonies() {
        return false;
    }
}
