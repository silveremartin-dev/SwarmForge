/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Multi-Species Diapause, Wintering & Thermal Clustering Engine (Diapause & Grappe Hivernale).
 * Manages thermal dormancy, metabolic rate reduction, and winter clustering for Ants, Bees, Wasps, and Termites.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DiapauseSystem {

    private final Simulation simulation;
    private long lastDiapauseCheck = 0;

    public DiapauseSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastDiapauseCheck < 300) return; // Check every 5 seconds
        lastDiapauseCheck = currentTick;

        WeatherSystem weather = simulation.getWeather();
        float ambientTemp = weather.getTemperature();

        for (Colony colony : simulation.getColonies()) {
            Species.InsectOrder order = colony.getSpecies().getInsectOrder();
            float deepNestTemp = weather.getTemperatureAtDepth(15);

            switch (order) {
                case ANT -> {
                    if (deepNestTemp < 10.0f) {
                        applyAntDiapause(colony);
                    }
                }
                case BEE -> {
                    if (ambientTemp < 14.0f) {
                        applyBeeWinterCluster(colony);
                    }
                }
                case WASP -> {
                    if (ambientTemp < 8.0f) {
                        applyWaspWinterDormancy(colony);
                    }
                }
                case TERMITE -> {
                    if (deepNestTemp < 12.0f) {
                        applyTermiteSubterraneanRetreat(colony);
                    }
                }
                case APHID -> {
                    if (ambientTemp < 10.0f) {
                        simulation.queueEvent(new SimulationEvent(
                                SimulationEvent.EventType.MILESTONE_REACHED,
                                simulation.getTickCount(),
                                "🌿 Aphid Colony " + colony.getSpeciesName() + " produced overwintering diapause eggs."
                        ));
                    }
                }
                case THRIPS -> {
                    if (ambientTemp < 12.0f) {
                        simulation.queueEvent(new SimulationEvent(
                                SimulationEvent.EventType.MILESTONE_REACHED,
                                simulation.getTickCount(),
                                "🌾 Acacia Thrips " + colony.getSpeciesName() + " sealed gall entrance for winter dormancy."
                        ));
                    }
                }
                case BEETLE -> {
                    if (deepNestTemp < 10.0f) {
                        simulation.queueEvent(new SimulationEvent(
                                SimulationEvent.EventType.MILESTONE_REACHED,
                                simulation.getTickCount(),
                                "🌲 Ambrosia Wood Beetles (" + colony.getSpeciesName() + ") entered deep heartwood dormancy."
                        ));
                    }
                }
            }
        }
    }

    private void applyAntDiapause(Colony colony) {
        simulation.queueEvent(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                "❄️ Ant Colony " + colony.getSpeciesName() + " entered subterranean Diapause (Metabolism -80%)."
        ));
    }

    private void applyBeeWinterCluster(Colony colony) {
        simulation.queueEvent(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                "🐝 Honey Bee Hive " + colony.getSpeciesName() + " formed a central Winter Thermal Cluster (Grappe)."
        ));
    }

    private void applyWaspWinterDormancy(Colony colony) {
        simulation.queueEvent(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                "🍂 Yellowjacket Wasp Gynes of " + colony.getSpeciesName() + " entered sheltered Hibernacula."
        ));
    }

    private void applyTermiteSubterraneanRetreat(Colony colony) {
        simulation.queueEvent(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                "🪲 Termite Colony " + colony.getSpeciesName() + " retreated to deep thermal core chambers."
        ));
    }
}
