/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.disasters;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

import java.util.Random;

/**
 * Severe thunderstorm disaster.
 * Wind speed and heavy rain wash away surface pheromones and infiltrate top soil over duration.
 */
public class StormDisaster implements DisasterEvent {

    private final float intensity;
    private final int durationTicks;
    private int remainingTicks;

    public StormDisaster(float intensity, int durationTicks) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.durationTicks = Math.max(10, durationTicks);
        this.remainingTicks = this.durationTicks;
    }

    public StormDisaster(float intensity) {
        this(intensity, (int) (60 + intensity * 180));
    }

    public StormDisaster() {
        this(0.5f);
    }

    @Override
    public float getIntensity() {
        return intensity;
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public int getRemainingTicks() {
        return remainingTicks;
    }

    @Override
    public boolean isFinished() {
        return remainingTicks <= 0;
    }

    @Override
    public String getName() {
        return "Severe Thunderstorm (Tempête Violente)";
    }

    @Override
    public String getSeverity() {
        if (intensity > 0.8f) return "CATASTROPHIC";
        if (intensity > 0.5f) return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🌩️ DISASTER TRIGGER: " + getName() + " (" + getSeverity() + " | Durée: " + durationTicks + " pas)!");
        this.remainingTicks = durationTicks;
        tick(simulation, terrarium);
    }

    @Override
    public void tick(Simulation simulation, Terrarium terrarium) {
        if (remainingTicks <= 0) return;
        remainingTicks--;

        if (simulation != null) {
            var weather = simulation.getWeather();
            if (weather != null) {
                weather.setRainfall(15.0f * intensity);
                weather.setWindSpeed(intensity * 90.0f);
            }

            // Washes away surface pheromones (accelerated pheromone decay over storm duration)
            if (simulation.getPheromoneGrid() != null) {
                simulation.getPheromoneGrid().setEvaporationMultiplier(1.0f + intensity * 3.0f);
            }
        }

        // Localized surface flash flooding
        if (terrarium != null) {
            Random rand = new Random();
            int count = (int) (3 + intensity * 10);
            for (int i = 0; i < count; i++) {
                int x = rand.nextInt(terrarium.getWidth());
                int y = rand.nextInt(terrarium.getHeight());
                for (int z = terrarium.getDepth() - 1; z >= 0; z--) {
                    var cell = terrarium.getCell(x, y, z);
                    if (cell.material() != org.swarmforge.core.domain.TerrariumCell.Material.AIR) {
                        if (z < 12 && z + 1 < terrarium.getDepth()) {
                            terrarium.setCell(new org.swarmforge.core.domain.TerrariumCell(
                                    x, y, z + 1,
                                    org.swarmforge.core.domain.TerrariumCell.Material.WATER,
                                    new float[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TYPES],
                                    14f, 100f));
                        }
                        break;
                    }
                }
            }
        }
    }
}
