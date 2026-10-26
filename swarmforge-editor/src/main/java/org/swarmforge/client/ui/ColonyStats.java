package org.swarmforge.client.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Data structure holding telemetry and demographic statistics for colonies and environment.
 */
public class ColonyStats {
    public long simTicks;
    public double stepTimeSeconds = 0.05; // Default 20 Hz (0.05s per tick)
    public int population;
    public int queens;
    public int workers;
    public int soldiers;
    public int males;
    public double food;
    public double water;
    public double protein;
    public double carbohydrate;
    public double temperature = 22.5; // °C
    public double rainfall = 0.0;     // mm/h
    public double pheromones = 100.0;
    public double tickRate;
    public double targetTickRate = 60.0;
    public int births;
    public int deaths;
    public String activeEvent = "AUCUN";

    // Ethological / Behavioral state distribution
    public int stateForaging;
    public int stateDigging;
    public int stateNursing;
    public int stateGuarding;
    public int stateRoyalCare;
    public int stateResting;

    // Multi-colony & dynamic caste breakdown: ColonyName -> (CasteName -> Count)
    public Map<String, Map<String, Integer>> colonyCasteCounts = new HashMap<>();

    public ColonyStats() {
    }

    public ColonyStats(long simTicks, int population, int queens, int workers, int soldiers,
                       double food, double water, double tickRate, int births, int deaths) {
        this.simTicks = simTicks;
        this.population = population;
        this.queens = queens;
        this.workers = workers;
        this.soldiers = soldiers;
        this.food = food;
        this.water = water;
        this.tickRate = tickRate;
        this.births = births;
        this.deaths = deaths;
    }

    public double getSimTimeSeconds() {
        return simTicks * stepTimeSeconds;
    }
}
