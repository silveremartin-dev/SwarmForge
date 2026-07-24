/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Pheromone-Climate Coupling System.
 * Dynamically adjusts chemical evaporation rates and spatial dissipation of trail and alarm pheromones
 * based on ambient temperature, wind turbulence, and relative humidity.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PheromoneClimateSystem {

    private final Simulation simulation;

    public PheromoneClimateSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        WeatherSystem weather = simulation.getWeather();
        SparsePheromoneGrid grid = simulation.getPheromoneGrid();

        if (grid == null) return;

        float temp = weather.getTemperature();
        float humidity = weather.getHumidity();
        float wind = weather.getWindSpeed();

        // Evaporation rate multiplier calculation:
        // Hot dry air + high wind = high volatility (rapid decay)
        // Cool moist air = slow evaporation (persistent trails)
        float tempFactor = (float) Math.max(0.5, 1.0 + (temp - 20.0) / 20.0);
        float humFactor = (float) Math.max(0.4, 1.2 - humidity / 100.0);
        float windFactor = (float) Math.max(1.0, 1.0 + wind / 15.0);

        float decayMultiplier = tempFactor * humFactor * windFactor;

        // Apply decay multiplier to pheromone grid tick logic
        grid.setEvaporationMultiplier(decayMultiplier);
    }
}
