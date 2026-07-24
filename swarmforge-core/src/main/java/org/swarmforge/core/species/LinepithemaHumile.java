/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Linepithema humile - Argentine Ant
 * Invasive species known for forming supercolonies.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LinepithemaHumile implements Species {

    @Override
    public String getScientificName() {
        return "Linepithema humile";
    }

    @Override
    public String getCommonName() {
        return "Argentine Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 365;
    } // ~1 year

    @Override
    public int getQueenLifespan() {
        return 365 * 10;
    }

    @Override
    public float getWorkerSpeed() {
        return 0.7f;
    } // Fast

    @Override
    public float getViewDistance() {
        return 2.5f;
    }

    @Override
    public int getTypicalColonySize() {
        return 100000;
    }

    @Override
    public boolean formsMegaColonies() {
        return true;
    } // Famous for supercolonies
}
