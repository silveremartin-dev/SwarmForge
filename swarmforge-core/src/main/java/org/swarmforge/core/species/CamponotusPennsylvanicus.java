/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Camponotus pennsylvanicus - Black Carpenter Ant
 * Large wood-nesting species from North America.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CamponotusPennsylvanicus implements Species {

    @Override
    public String getScientificName() {
        return "Camponotus pennsylvanicus";
    }

    @Override
    public String getCommonName() {
        return "Black Carpenter Ant";
    }

    @Override
    public int getWorkerLifespan() {
        return 365 * 7;
    } // Long-lived

    @Override
    public int getQueenLifespan() {
        return 365 * 25;
    } // Very long

    @Override
    public float getWorkerSpeed() {
        return 0.4f;
    } // Slower, larger

    @Override
    public float getViewDistance() {
        return 5.0f;
    } // Good vision

    @Override
    public int getTypicalColonySize() {
        return 3000;
    }

    @Override
    public boolean formsMegaColonies() {
        return false;
    }
}
