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
 * Flash flood disaster filling low areas progressively.
 * Submersion drowning damage scales with duration and intensity.
 */
public class FloodDisaster implements DisasterEvent {

    private final float intensity;
    private final int waterLevel;
    private final int durationTicks;
    private int remainingTicks;

    public FloodDisaster(float intensity, int waterLevel, int durationTicks) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.waterLevel = waterLevel;
        this.durationTicks = Math.max(10, durationTicks);
        this.remainingTicks = this.durationTicks;
    }

    public FloodDisaster(float intensity, int waterLevel) {
        this(intensity, waterLevel, (int) (80 + intensity * 200));
    }

    public FloodDisaster() {
        this(0.5f, 10);
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
        return "Flash Flood (Inondation Subite)";
    }

    @Override
    public String getSeverity() {
        if (intensity > 0.8f) return "CATASTROPHIC";
        if (intensity > 0.5f) return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("🌊 DISASTER TRIGGER: " + getName() + " (" + getSeverity() + " | Durée: " + durationTicks + " pas)!");
        this.remainingTicks = durationTicks;
        tick(simulation, terrarium);
    }

    @Override
    public void tick(Simulation simulation, Terrarium terrarium) {
        if (remainingTicks <= 0) return;
        remainingTicks--;

        Random rand = new Random();
        float progress = 1.0f - ((float) remainingTicks / (float) durationTicks);
        int currentWaterLevel = Math.max(1, (int) (waterLevel * progress));

        if (terrarium != null) {
            float coverage = 0.05f + intensity * 0.45f * progress;
            for (int x = 0; x < terrarium.getWidth(); x++) {
                for (int y = 0; y < terrarium.getHeight(); y++) {
                    if (rand.nextFloat() > coverage) continue;

                    for (int z = 0; z < Math.min(currentWaterLevel, terrarium.getDepth()); z++) {
                        TerrariumCell cell = terrarium.getCell(x, y, z);
                        if (cell.material() == TerrariumCell.Material.AIR || cell.material() == TerrariumCell.Material.CHAMBER) {
                            terrarium.setCell(new TerrariumCell(
                                    x, y, z, TerrariumCell.Material.WATER,
                                    new float[TerrariumCell.PHEROMONE_TYPES],
                                    15f, 100f
                            ));
                        }
                    }
                }
            }
        }

        // Drown submerged ants progressively scaled by duration exposure & intensity
        if (simulation != null) {
            float drowningDamagePerTick = (intensity * 25.0f / Math.max(1, durationTicks / 10));
            for (Colony colony : simulation.getColonies()) {
                for (Individual ant : colony.getLivingIndividuals()) {
                    if (ant.getZ() < currentWaterLevel) {
                        if (terrarium != null) {
                            TerrariumCell cell = terrarium.getCell((int) ant.getX(), (int) ant.getY(), (int) ant.getZ());
                            if (cell.material() == TerrariumCell.Material.WATER) {
                                ant.takeDamage(drowningDamagePerTick);
                            }
                        }
                    }
                }
            }
        }
    }
}
