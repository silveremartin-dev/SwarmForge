/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks historical statistics for a colony.
 * Used for generating graphs and analyzing ecosystem stability.
 *
 * @author Gemini AI Assistant
 */
public class ColonyStatistics implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final List<DataPoint> history = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY_SIZE = 1000; // Keep last 1000 points

    public record DataPoint(long tick, int population, float food, float water, int deaths, int births)
            implements java.io.Serializable {
    }

    public void record(long tick, int population, float food, float water, int deaths, int births) {
        history.add(new DataPoint(tick, population, food, water, deaths, births));
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    public List<DataPoint> getHistory() {
        return new ArrayList<>(history); // Return copy to avoid concurrent mod exceptions during iteration
    }
}
