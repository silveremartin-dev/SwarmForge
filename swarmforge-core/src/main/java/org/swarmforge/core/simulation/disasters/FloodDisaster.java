/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.disasters;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;

import java.util.Random;

/**
 * Flood disaster that fills low-lying areas with water.
 * Water flows downhill and can drown ants.
 */
public class FloodDisaster implements DisasterEvent {

    private final float intensity;
    private final int waterLevel;

    public FloodDisaster(float intensity, int waterLevel) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.waterLevel = waterLevel;
    }

    public FloodDisaster() {
        this(0.5f, 10); // Default moderate flood at level 10
    }

    @Override
    public String getName() {
        return "Flash Flood";
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
        System.out.println("🌊 DISASTER ALERT: " + getName() + " (" + getSeverity() + ")!");

        Random rand = new Random();
        int floodedCells = 0;
        int drownedAnts = 0;

        // Water coverage based on intensity
        float coverage = 0.1f + intensity * 0.4f;

        // Fill low areas with water
        for (int x = 0; x < terrarium.getWidth(); x++) {
            for (int y = 0; y < terrarium.getHeight(); y++) {
                // Random water distribution based on coverage
                if (rand.nextFloat() > coverage)
                    continue;

                // Find ground level and flood above it
                for (int z = 0; z < Math.min(waterLevel, terrarium.getDepth()); z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);

                    // Only flood air cells (don't replace solid matter)
                    if (cell.material() == TerrariumCell.Material.AIR ||
                            cell.material() == TerrariumCell.Material.CHAMBER) {
                        terrarium.setCell(new TerrariumCell(
                                x, y, z, TerrariumCell.Material.WATER,
                                new float[TerrariumCell.PHEROMONE_TYPES],
                                15f, // Cold water
                                100f // 100% humidity
                        ));
                        floodedCells++;
                    }
                }
            }
        }

        // Drown ants below water level
        for (Colony colony : simulation.getColonies()) {
            for (Individual ant : colony.getLivingIndividuals()) {
                if (ant.getZ() < waterLevel && rand.nextFloat() < intensity * 0.5f) {
                    // Check if in flooded cell
                    TerrariumCell cell = terrarium.getCell(
                            (int) ant.getX(), (int) ant.getY(), (int) ant.getZ());
                    if (cell.material() == TerrariumCell.Material.WATER) {
                        ant.takeDamage(50f * intensity); // Drowning damage
                        drownedAnts++;
                    }
                }
            }
        }

        System.out.println("  Flooded " + floodedCells + " cells, affected " + drownedAnts + " ants");
    }
}
