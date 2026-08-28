/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
 * Earthquake disaster causing structural collapse in tunnels/chambers.
 * Cave-in probability and crushing damage scale with magnitude and quake duration.
 */
public class EarthquakeDisaster implements DisasterEvent {

    private final float magnitude; // Richter-like scale 0.1 - 1.0
    private final int durationTicks;
    private int remainingTicks;

    public EarthquakeDisaster(float magnitude, int durationTicks) {
        this.magnitude = Math.min(1.0f, Math.max(0.1f, magnitude));
        this.durationTicks = Math.max(10, durationTicks);
        this.remainingTicks = this.durationTicks;
    }

    public EarthquakeDisaster(float magnitude) {
        this(magnitude, (int) (20 + magnitude * 80));
    }

    public EarthquakeDisaster() {
        this(0.5f);
    }

    @Override
    public float getIntensity() {
        return magnitude;
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
        return "Earthquake (Séisme Souterrain)";
    }

    @Override
    public String getSeverity() {
        if (magnitude > 0.8f) return "CATASTROPHIC";
        if (magnitude > 0.5f) return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🌍 DISASTER TRIGGER: " + getName() + " (Magnitude " + String.format("%.1f", magnitude * 10) + " | Durée: " + durationTicks + " pas)!");
        this.remainingTicks = durationTicks;
        tick(simulation, terrarium);
    }

    @Override
    public void tick(Simulation simulation, Terrarium terrarium) {
        if (remainingTicks <= 0) return;
        remainingTicks--;

        Random rand = new Random();
        float collapseChance = (magnitude * 0.15f) / Math.max(1, durationTicks / 10);

        if (terrarium != null) {
            for (int x = 0; x < terrarium.getWidth(); x++) {
                for (int y = 0; y < terrarium.getHeight(); y++) {
                    for (int z = 0; z < terrarium.getDepth() - 10; z++) {
                        TerrariumCell cell = terrarium.getCell(x, y, z);
                        if (cell.material() == TerrariumCell.Material.AIR || cell.material() == TerrariumCell.Material.CHAMBER) {
                            float actualChance = cell.material() == TerrariumCell.Material.CHAMBER ? collapseChance * 1.5f : collapseChance;
                            int earthNeighbors = countEarthNeighbors(terrarium, x, y, z);

                            if (earthNeighbors >= 4 && rand.nextFloat() < actualChance) {
                                terrarium.setCell(new TerrariumCell(
                                        x, y, z, TerrariumCell.Material.EARTH,
                                        new float[TerrariumCell.PHEROMONE_TYPES],
                                        cell.temperature(), cell.humidity()));
                            }
                        }
                    }
                }
            }
        }

        // Damage ants caught in collapsed areas or shaken
        if (simulation != null) {
            for (Colony colony : simulation.getColonies()) {
                for (Individual ant : colony.getLivingIndividuals()) {
                    if (terrarium != null) {
                        TerrariumCell atAnt = terrarium.getCell((int) ant.getX(), (int) ant.getY(), (int) ant.getZ());
                        if (atAnt.material() == TerrariumCell.Material.EARTH) {
                            ant.takeDamage(100f); // Crushing collapse damage
                        } else if (rand.nextFloat() < magnitude * 0.1f) {
                            ant.takeDamage(magnitude * 4.0f); // Shaking stress
                        }
                    }
                }
            }
        }
    }

    private int countEarthNeighbors(Terrarium terrarium, int x, int y, int z) {
        int count = 0;
        int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            int nz = z + dir[2];
            if (terrarium.inBounds(nx, ny, nz)) {
                TerrariumCell neighbor = terrarium.getCell(nx, ny, nz);
                if (neighbor.material() == TerrariumCell.Material.EARTH || neighbor.material() == TerrariumCell.Material.ROCK) {
                    count++;
                }
            }
        }
        return count;
    }
}
