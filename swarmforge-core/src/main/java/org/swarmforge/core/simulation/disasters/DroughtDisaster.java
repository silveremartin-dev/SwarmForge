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
 * Drought disaster that depletes water sources and food.
 * Reduces humidity and causes gradual starvation conditions.
 */
public class DroughtDisaster implements DisasterEvent {

    private final float intensity;

    public DroughtDisaster(float intensity) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
    }

    public DroughtDisaster() {
        this(0.5f);
    }

    @Override
    public String getName() {
        return "Severe Drought";
    }

    @Override
    public String getSeverity() {
        if (intensity > 0.8f)
            return "CATASTROPHIC";
        if (intensity > 0.5f)
            return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🏜️ DISASTER ALERT: " + getName() + " (" + getSeverity() + ")!");

        Random rand = new Random();
        int driedCells = 0;
        int depletedFood = 0;

        // Dry up water cells
        float dryChance = intensity * 0.5f;
        for (var cell : terrarium.getAllCells()) {
            if (cell.material() == TerrariumCell.Material.WATER) {
                if (rand.nextFloat() < dryChance) {
                    terrarium.setCell(new TerrariumCell(
                            cell.x(), cell.y(), cell.z(),
                            TerrariumCell.Material.SAND, // Water becomes sand
                            new float[TerrariumCell.PHEROMONE_TYPES],
                            cell.temperature() + 5f, // Warmer
                            20f // Low humidity
                    ));
                    driedCells++;
                }
            }
        }

        // Reduce food source quantities
        for (FoodSource food : simulation.getFoodSources()) {
            float reduction = food.getQuantity() * intensity * 0.3f;
            food.take(reduction);
            if (reduction > 0)
                depletedFood++;
        }

        // Lower global humidity
        var weather = simulation.getWeather();
        weather.setHumidity(Math.max(5f, weather.getHumidity() * (1f - intensity * 0.4f)));

        System.out.println("  Dried " + driedCells + " water cells, depleted " +
                depletedFood + " food sources");
        System.out.println("  Humidity now " + String.format("%.0f", weather.getHumidity()) + "%");
    }
}
