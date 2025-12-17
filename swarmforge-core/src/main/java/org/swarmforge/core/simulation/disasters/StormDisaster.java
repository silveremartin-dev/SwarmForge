/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.disasters;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

/**
 * Concrete Disaster: Storm.
 * Increases humidity, reduces temperature, adds wind, may flood cells.
 */
public class StormDisaster implements DisasterEvent {
    @Override
    public String getName() {
        return "Severe Thunderstorm";
    }

    @Override
    public String getSeverity() {
        return "MAJOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("⚠️ DISASTER ALERT: Severe Thunderstorm approaching!");
        // Update weather system
        // world.getWeather().setPrecipitation(1.0f);
        // world.getWeather().setWindSpeed(80.0f);

        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            int x = rand.nextInt(terrarium.getWidth());
            int y = rand.nextInt(terrarium.getHeight());
            for (int z = terrarium.getDepth() - 1; z >= 0; z--) {
                org.swarmforge.core.domain.TerrariumCell cell = terrarium.getCell(x, y, z);
                if (cell.material() != org.swarmforge.core.domain.TerrariumCell.Material.AIR) {
                    if (z < 12 && z + 1 < terrarium.getDepth()) { // Flash flood low areas
                        terrarium.setCell(new org.swarmforge.core.domain.TerrariumCell(
                                x, y, z + 1,
                                org.swarmforge.core.domain.TerrariumCell.Material.WATER,
                                new float[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TYPES],
                                15f, 100f));
                    }
                    break;
                }
            }
        }
    }
}
