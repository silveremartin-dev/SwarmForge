/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
    private final List<DetailedDataPoint> detailedHistory = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY_SIZE = 1000; // Keep last 1000 points

    public record DataPoint(long tick, int population, float food, float water, int deaths, int births)
            implements java.io.Serializable {
    }

    public record DetailedDataPoint(
            long tick,
            int colonyAgeTicks,
            int totalPopulation,
            java.util.Map<Individual.Caste, Integer> casteCounts,
            java.util.Map<Individual.Caste, Float> avgAgeByCaste,
            java.util.Map<Individual.LifeStage, Integer> broodCounts,
            float foodStored,
            float proteinStored,
            float carbohydrateStored,
            float waterStored,
            float nestTemperature,
            float nestCo2Level,
            float nestO2Level,
            int chamberCount,
            int totalBorn,
            int totalDied
    ) implements java.io.Serializable {
    }

    public void record(long tick, int population, float food, float water, int deaths, int births) {
        history.add(new DataPoint(tick, population, food, water, deaths, births));
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    public void recordDetailed(DetailedDataPoint point) {
        detailedHistory.add(point);
        record(point.tick(), point.totalPopulation(), point.foodStored(), point.waterStored(), point.totalDied(), point.totalBorn());
        if (detailedHistory.size() > MAX_HISTORY_SIZE) {
            detailedHistory.remove(0);
        }
    }

    public List<DataPoint> getHistory() {
        return new ArrayList<>(history); // Return copy to avoid concurrent mod exceptions during iteration
    }

    public List<DetailedDataPoint> getDetailedHistory() {
        return new ArrayList<>(detailedHistory);
    }
}
