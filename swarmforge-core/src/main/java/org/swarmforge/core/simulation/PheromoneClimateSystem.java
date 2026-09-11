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

        if (grid == null || weather == null) return;

        float temp = weather.getTemperature(); // °C
        float humidity = weather.getHumidity(); // %
        float windMs = weather.getWindSpeedMs(); // m/s (SI Standard: 1 m/s = 3.6 km/h)

        // Evaporation rate multiplier calculation (SI Compliant):
        // Hot dry air + high wind = high volatility (rapid decay)
        // Cool moist air = slow evaporation (persistent trails)
        float tempFactor = (float) Math.max(0.5, 1.0 + (temp - 20.0) / 20.0);
        float humFactor = (float) Math.max(0.4, 1.2 - humidity / 100.0);
        float windFactor = (float) Math.max(1.0, 1.0 + windMs / 4.167f); // 4.167 m/s ≈ 15.0 km/h
        float rainFactor = weather.isRaining() ? (1.0f + weather.getRainfall() / 15.0f) : 1.0f; // Rain runoff wash-off

        float decayMultiplier = tempFactor * humFactor * windFactor * rainFactor;

        // Apply decay multiplier to pheromone grid tick logic
        grid.setEvaporationMultiplier(decayMultiplier);

        // Calculate and apply wind vector components (SI m/s) for surface advection
        float windAngleRad = (float) Math.toRadians(weather.getWindDirectionAngle());
        float windVx = (float) Math.sin(windAngleRad) * windMs;
        float windVy = (float) Math.cos(windAngleRad) * windMs;
        grid.setWindVector(windVx, windVy);

        // Apply surface rain intensity (mm/h) for differentiated wash-off
        grid.setSurfaceRainIntensity(weather.isRaining() ? weather.getRainfall() : 0.0f);
    }
}
