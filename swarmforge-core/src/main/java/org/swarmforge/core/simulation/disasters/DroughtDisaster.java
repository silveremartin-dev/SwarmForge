/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.disasters;

import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;

import java.util.Random;

/**
 * Severe drought disaster depleting water and food reserves.
 * Desiccation damage scales progressively with duration and intensity.
 */
public class DroughtDisaster implements DisasterEvent {

    private final float intensity;
    private final int durationTicks;
    private int remainingTicks;

    public DroughtDisaster(float intensity, int durationTicks) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.durationTicks = Math.max(10, durationTicks);
        this.remainingTicks = this.durationTicks;
    }

    public DroughtDisaster(float intensity) {
        this(intensity, (int) (150 + intensity * 450));
    }

    public DroughtDisaster() {
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
        return "Severe Drought (Sécheresse Prolongée)";
    }

    @Override
    public String getSeverity() {
        if (intensity > 0.8f) return "CATASTROPHIC";
        if (intensity > 0.5f) return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🏜️ DISASTER TRIGGER: " + getName() + " (" + getSeverity() + " | Durée: " + durationTicks + " pas)!");
        this.remainingTicks = durationTicks;
        tick(simulation, terrarium);
    }

    @Override
    public void tick(Simulation simulation, Terrarium terrarium) {
        if (remainingTicks <= 0) return;
        remainingTicks--;

        Random rand = new Random();

        if (terrarium != null) {
            float dryChance = intensity * 0.05f;
            for (var cell : terrarium.getAllCells()) {
                if (cell.material() == TerrariumCell.Material.WATER && rand.nextFloat() < dryChance) {
                    terrarium.setCell(new TerrariumCell(
                            cell.x(), cell.y(), cell.z(),
                            TerrariumCell.Material.SAND,
                            new float[TerrariumCell.PHEROMONE_TYPES],
                            cell.temperature() + 2f,
                            15f
                    ));
                }
            }
        }

        if (simulation != null) {
            // Progressive food source degradation under drought desiccation
            for (FoodSource food : simulation.getFoodSources()) {
                food.take(food.getQuantity() * intensity * 0.003f);
            }

            // Lower global atmospheric humidity
            var weather = simulation.getWeather();
            if (weather != null) {
                weather.setHumidity(Math.max(5f, weather.getHumidity() - intensity * 0.2f));
            }
        }
    }
}
