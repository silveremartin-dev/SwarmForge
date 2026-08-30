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

import java.util.List;
import java.util.Random;

/**
 * Wildfire disaster that spreads progressively through surface vegetation.
 * Damage scales directly with duration and intensity.
 */
public class FireDisaster implements DisasterEvent {

    private final float intensity;
    private final int spreadRadius;
    private int centerX, centerY, centerZ;
    private final int durationTicks;
    private int remainingTicks;

    public FireDisaster(int centerX, int centerY, int centerZ, float intensity, int durationTicks) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.durationTicks = Math.max(10, durationTicks);
        this.remainingTicks = this.durationTicks;
        this.spreadRadius = (int) (8 + intensity * 22);
    }

    public FireDisaster(int centerX, int centerY, int centerZ, float intensity) {
        this(centerX, centerY, centerZ, intensity, (int) (60 + intensity * 240));
    }

    public FireDisaster() {
        this(-1, -1, -1, 0.5f);
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
        return "Wildfire";
    }

    @Override
    public String getSeverity() {
        if (intensity > 0.8f) return "CATASTROPHIC";
        if (intensity > 0.5f) return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🔥 DISASTER TRIGGER: " + getName() + " (" + getSeverity() + " | Durée: " + durationTicks + " pas)!");

        Random rand = new Random();
        if (centerX < 0 && terrarium != null) centerX = rand.nextInt(terrarium.getWidth());
        if (centerY < 0 && terrarium != null) centerY = rand.nextInt(terrarium.getHeight());
        if (centerZ < 0 && terrarium != null) centerZ = terrarium.getDepth() - 5;

        this.remainingTicks = durationTicks;
        tick(simulation, terrarium);
    }

    @Override
    public void tick(Simulation simulation, Terrarium terrarium) {
        if (remainingTicks <= 0) return;
        remainingTicks--;

        Random rand = new Random();
        float progress = 1.0f - ((float) remainingTicks / (float) durationTicks);
        int currentRadius = Math.max(2, (int) (spreadRadius * Math.min(1.0f, progress * 1.3f)));

        int burnedCells = 0;
        int damagedAnts = 0;

        if (terrarium != null) {
            for (int dx = -currentRadius; dx <= currentRadius; dx++) {
                for (int dy = -currentRadius; dy <= currentRadius; dy++) {
                    int x = centerX + dx;
                    int y = centerY + dy;

                    if (!terrarium.inBounds(x, y, centerZ)) continue;

                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > currentRadius) continue;

                    float burnChance = intensity * (1f - dist / currentRadius) * 0.3f;
                    if (rand.nextFloat() > burnChance) continue;

                    for (int z = centerZ; z < Math.min(terrarium.getDepth(), centerZ + 5); z++) {
                        TerrariumCell cell = terrarium.getCell(x, y, z);
                        if (cell.material() == TerrariumCell.Material.ORGANIC) {
                            terrarium.setCell(new TerrariumCell(
                                    x, y, z, TerrariumCell.Material.AIR,
                                    new float[TerrariumCell.PHEROMONE_TYPES],
                                    cell.temperature() + 100f * intensity,
                                    0f
                            ));
                            burnedCells++;
                        }
                    }
                }
            }
        }

        // Burn/destroy surface food sources progressively in fire radius
        if (simulation != null && simulation.getFoodSources() != null) {
            simulation.getFoodSources().forEach(food -> {
                float dx = food.getX() - centerX;
                float dy = food.getY() - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist <= currentRadius) {
                    food.take(food.getQuantity() * (0.1f + intensity * 0.2f));
                }
            });
        }

        // Damage ants progressively based on duration exposure and intensity
        if (simulation != null) {
            for (Colony colony : simulation.getColonies()) {
                List<Individual> ants = colony.getLivingIndividuals();
                for (Individual ant : ants) {
                    float dx = ant.getX() - centerX;
                    float dy = ant.getY() - centerY;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist <= currentRadius) {
                        float damagePerTick = (intensity * 12.0f / Math.max(1, durationTicks / 10)) * (1f - dist / currentRadius);
                        ant.takeDamage(damagePerTick);
                        damagedAnts++;
                    }
                }
            }
        }
    }
}
