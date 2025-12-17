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

import java.util.List;
import java.util.Random;

/**
 * Fire disaster that spreads through organic material.
 * Destroys organic cells and damages nearby ants.
 */
public class FireDisaster implements DisasterEvent {

    private final float intensity;
    private final int spreadRadius;
    private final int centerX, centerY, centerZ;

    public FireDisaster(int centerX, int centerY, int centerZ, float intensity) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.spreadRadius = (int) (5 + intensity * 20);
    }

    public FireDisaster() {
        this(-1, -1, -1, 0.5f); // Random location
    }

    @Override
    public String getName() {
        return "Wildfire";
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
        System.out.println("🔥 DISASTER ALERT: " + getName() + " (" + getSeverity() + ")!");

        Random rand = new Random();
        int fireX = centerX >= 0 ? centerX : rand.nextInt(terrarium.getWidth());
        int fireY = centerY >= 0 ? centerY : rand.nextInt(terrarium.getHeight());
        int fireZ = centerZ >= 0 ? centerZ : terrarium.getDepth() - 5; // Near surface

        int burnedCells = 0;
        int damagedAnts = 0;

        // Spread fire in radius
        for (int dx = -spreadRadius; dx <= spreadRadius; dx++) {
            for (int dy = -spreadRadius; dy <= spreadRadius; dy++) {
                int x = fireX + dx;
                int y = fireY + dy;

                if (!terrarium.inBounds(x, y, fireZ))
                    continue;

                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > spreadRadius)
                    continue;

                // Fire probability decreases with distance
                float burnChance = intensity * (1f - dist / spreadRadius);
                if (rand.nextFloat() > burnChance)
                    continue;

                // Burn surface cells
                for (int z = fireZ; z < Math.min(terrarium.getDepth(), fireZ + 5); z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);

                    // Fire consumes organic material
                    if (cell.material() == TerrariumCell.Material.ORGANIC) {
                        terrarium.setCell(new TerrariumCell(
                                x, y, z, TerrariumCell.Material.AIR,
                                new float[TerrariumCell.PHEROMONE_TYPES],
                                cell.temperature() + 100f, // High temp
                                0f // Humidity drops
                        ));
                        burnedCells++;
                    }
                }
            }
        }

        // Damage ants in fire zone
        for (Colony colony : simulation.getColonies()) {
            List<Individual> ants = colony.getLivingIndividuals();
            for (Individual ant : ants) {
                float dx = ant.getX() - fireX;
                float dy = ant.getY() - fireY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < spreadRadius) {
                    float damage = intensity * 30f * (1f - dist / spreadRadius);
                    ant.takeDamage(damage);
                    damagedAnts++;
                }
            }
        }

        System.out.println("  Burned " + burnedCells + " cells, damaged " + damagedAnts + " ants");
    }
}
