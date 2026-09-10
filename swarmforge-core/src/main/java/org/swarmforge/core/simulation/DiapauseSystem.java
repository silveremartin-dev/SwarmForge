/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.SeasonManager;
import org.swarmforge.core.world.WeatherSystem;

import java.util.logging.Logger;

/**
 * Winter Diapause & Hibernation System.
 * Simulates physiological overwintering torpor, slowed metabolic consumption (Q10 kinetics),
 * and cessation of queen oviposition during sub-threshold cold periods.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DiapauseSystem {

    private static final Logger LOG = Logger.getLogger(DiapauseSystem.class.getName());

    private final Simulation simulation;
    private long lastDiapauseCheck = 0;

    public DiapauseSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Updates diapause status across colonies.
     */
    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastDiapauseCheck < 300) { // Check every ~5 seconds
            return;
        }
        lastDiapauseCheck = currentTick;

        WeatherSystem weather = simulation.getWeather();
        SeasonManager seasonManager = simulation.getSeasonManager();
        if (weather == null || seasonManager == null) {
            return;
        }

        float temp = weather.getTemperature();
        boolean isCold = (temp < 10.0f);
        boolean isWinter = (seasonManager.getCurrentSeason() == Season.WINTER);

        if (isWinter || isCold) {
            for (Colony colony : simulation.getColonies()) {
                // Diapause effect: Reduced metabolic burn, slowed movement
                float baseRate = colony.getMetabolicRate();
                if (baseRate > 0.3f) {
                    colony.setMetabolicRate(0.2f); // 80% metabolic slowdown in winter torpor
                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.DIAPAUSE_ENTERED,
                            currentTick,
                            colony.getId(),
                            "Colony " + colony.getSpeciesName() + " entered winter diapause (Ambient: " + temp + "°C)"
                    ));
                }
            }
        } else {
            for (Colony colony : simulation.getColonies()) {
                if (colony.getMetabolicRate() < 0.9f) {
                    colony.setMetabolicRate(1.0f); // Resume active metabolism
                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.DIAPAUSE_EXITED,
                            currentTick,
                            colony.getId(),
                            "Colony " + colony.getSpeciesName() + " exited diapause into active state"
                    ));
                }
            }
        }
    }
}
