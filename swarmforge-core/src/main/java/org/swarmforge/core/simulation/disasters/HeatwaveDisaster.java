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
 * Heatwave disaster that raises temperatures dangerously high.
 * Causes increased metabolism and potential death from overheating.
 */
public class HeatwaveDisaster implements DisasterEvent {

    private final float intensity;
    private final int durationTicks;

    public HeatwaveDisaster(float intensity, int durationTicks) {
        this.intensity = Math.min(1.0f, Math.max(0.1f, intensity));
        this.durationTicks = durationTicks;
    }

    public HeatwaveDisaster() {
        this(0.6f, 100);
    }

    /**
     * Get the intended duration of this heatwave in ticks.
     */
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public String getName() {
        return "Extreme Heatwave";
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
        float tempIncrease = intensity * 25f; // Up to 25°C increase
        System.out.println("☀️ DISASTER ALERT: " + getName() + " (+" +
                String.format("%.1f", tempIncrease) + "°C)!");

        // Update weather system if available
        var weather = simulation.getWeather();
        float newTemp = weather.getTemperature() + tempIncrease;
        weather.setTemperature(newTemp);
        weather.setHumidity(Math.max(10f, weather.getHumidity() - intensity * 30f));

        int affectedAnts = 0;

        // Immediate heat stress to all surface ants
        for (Colony colony : simulation.getColonies()) {
            for (Individual ant : colony.getLivingIndividuals()) {
                // Surface ants (high Z values) are more affected
                float depthFactor = 1f - (ant.getZ() / terrarium.getDepth());
                float heatDamage = intensity * 5f * depthFactor;

                if (heatDamage > 0.5f) {
                    ant.takeDamage(heatDamage);

                    // Increase energy consumption (faster metabolism)
                    ant.setEnergy(ant.getEnergy() - intensity * 2f);

                    affectedAnts++;
                }
            }
        }

        System.out.println("  Temperature now " + String.format("%.1f", newTemp) +
                "°C, affected " + affectedAnts + " surface ants");
    }
}
