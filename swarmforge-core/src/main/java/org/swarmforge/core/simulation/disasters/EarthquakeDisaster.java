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
 * Earthquake disaster that collapses tunnels and chambers.
 * Cave-ins can trap or kill ants underground.
 */
public class EarthquakeDisaster implements DisasterEvent {

    private final float magnitude; // Richter-like scale 0.0-1.0

    public EarthquakeDisaster(float magnitude) {
        this.magnitude = Math.min(1.0f, Math.max(0.1f, magnitude));
    }

    public EarthquakeDisaster() {
        this(0.5f);
    }

    @Override
    public String getName() {
        return "Earthquake";
    }

    @Override
    public String getSeverity() {
        if (magnitude > 0.8f)
            return "CATASTROPHIC";
        if (magnitude > 0.5f)
            return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🌍 DISASTER ALERT: " + getName() + " (Magnitude " +
                String.format("%.1f", magnitude * 10) + ")!");

        Random rand = new Random();
        int collapsedCells = 0;
        int trappedAnts = 0;

        // Collapse probability based on magnitude
        float collapseChance = magnitude * 0.3f;

        // Check all chamber and air cells underground
        for (int x = 0; x < terrarium.getWidth(); x++) {
            for (int y = 0; y < terrarium.getHeight(); y++) {
                // Only affect underground areas
                for (int z = 0; z < terrarium.getDepth() - 10; z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);

                    // Collapse air pockets and chambers
                    if (cell.material() == TerrariumCell.Material.AIR ||
                            cell.material() == TerrariumCell.Material.CHAMBER) {

                        // Higher collapse chance for chambers (known weak points)
                        float actualChance = cell.material() == TerrariumCell.Material.CHAMBER
                                ? collapseChance * 1.5f
                                : collapseChance;

                        // Check structural integrity (adjacent earth count)
                        int earthNeighbors = countEarthNeighbors(terrarium, x, y, z);

                        // More likely to collapse if surrounded by earth
                        if (earthNeighbors >= 4 && rand.nextFloat() < actualChance) {
                            terrarium.setCell(new TerrariumCell(
                                    x, y, z, TerrariumCell.Material.EARTH,
                                    new float[TerrariumCell.PHEROMONE_TYPES],
                                    cell.temperature(),
                                    cell.humidity()));
                            collapsedCells++;
                        }
                    }
                }
            }
        }

        // Damage ants caught in collapsed areas
        for (Colony colony : simulation.getColonies()) {
            for (Individual ant : colony.getLivingIndividuals()) {
                TerrariumCell atAnt = terrarium.getCell(
                        (int) ant.getX(), (int) ant.getY(), (int) ant.getZ());

                // Ant is now inside solid earth (collapsed on them)
                if (atAnt.material() == TerrariumCell.Material.EARTH) {
                    ant.takeDamage(100f); // Crushing damage - usually fatal
                    trappedAnts++;
                } else if (rand.nextFloat() < magnitude * 0.2f) {
                    // Shaking damage
                    ant.takeDamage(magnitude * 15f);
                }
            }
        }

        System.out.println("  Collapsed " + collapsedCells + " cells, trapped " + trappedAnts + " ants");
    }

    private int countEarthNeighbors(Terrarium terrarium, int x, int y, int z) {
        int count = 0;
        int[][] dirs = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };

        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            int nz = z + dir[2];

            if (terrarium.inBounds(nx, ny, nz)) {
                TerrariumCell neighbor = terrarium.getCell(nx, ny, nz);
                if (neighbor.material() == TerrariumCell.Material.EARTH ||
                        neighbor.material() == TerrariumCell.Material.ROCK) {
                    count++;
                }
            }
        }
        return count;
    }
}
