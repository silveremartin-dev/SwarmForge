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
import org.swarmforge.core.simulation.Simulation;

/**
 * Extreme heatwave disaster raising ambient and soil temperatures.
 * Progressive thermal stress and metabolic drain scale with duration and intensity.
 */
public class HeatwaveDisaster implements DisasterEvent {

    private final float intensity;
    private final int durationTicks;
    private int remainingTicks;

    public HeatwaveDisaster(float intensity, int durationTicks) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.durationTicks = Math.max(10, durationTicks);
        this.remainingTicks = this.durationTicks;
    }

    public HeatwaveDisaster() {
        this(0.6f, 100);
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
        return "Extreme Heatwave";
    }

    @Override
    public String getSeverity() {
        if (intensity > 0.8f) return "CATASTROPHIC";
        if (intensity > 0.5f) return "MAJOR";
        return "MINOR";
    }

    @Override
    public void trigger(Simulation simulation, Terrarium terrarium) {
        System.out.println("☀️ DISASTER TRIGGER: " + getName() + " (" + getSeverity() + " | Durée: " + durationTicks + " pas)!");
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
                float targetTempBoost = intensity * 25f;
                weather.setTemperature(Math.min(55f, weather.getTemperature() + targetTempBoost * 0.05f));
                weather.setHumidity(Math.max(10f, weather.getHumidity() - intensity * 0.3f));
            }

            // Progressive heat stress to surface ants
            float heatDamagePerTick = intensity * 0.4f;
            float metabolicDrain = intensity * 0.15f;

            for (Colony colony : simulation.getColonies()) {
                for (Individual ant : colony.getLivingIndividuals()) {
                    if (terrarium != null) {
                        float depthFactor = 1f - (ant.getZ() / (float) terrarium.getDepth());
                        if (depthFactor > 0.3f) {
                            ant.takeDamage(heatDamagePerTick * depthFactor);
                            ant.setEnergy(Math.max(0f, ant.getEnergy() - metabolicDrain));
                        }
                    }
                }
            }
        }
    }
}
