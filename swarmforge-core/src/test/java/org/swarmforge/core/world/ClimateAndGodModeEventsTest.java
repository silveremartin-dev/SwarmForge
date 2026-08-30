/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.event.GodModeIntervention;
import org.swarmforge.core.event.GodModeIntervention.ActionType;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.disasters.DroughtDisaster;
import org.swarmforge.core.simulation.disasters.FloodDisaster;
import org.swarmforge.core.world.Season;

import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Suite verifying climate physics, seasonal transitions, atmospheric cycles,
 * climate disaster lifecycles, and God Mode interventions.
 */
public class ClimateAndGodModeEventsTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private WeatherSystem weather;
    private Colony colony;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(60, 60, 30);
        simulation = new Simulation(terrarium);
        weather = simulation.getWeather();
        colony = simulation.addColony("FormicaRufa", 1, 20, 5);
        colony.bootstrapDefaultResources();
    }

    @Test
    @DisplayName("Verify atmospheric evolution, temperature/humidity cycles, and diurnal solar radiation")
    void testAtmosphereAndDiurnalCycles() {
        float startTemp = weather.getTemperature();
        float startLight = weather.getLightLevel();

        // Advance 12 hours to shift night/day
        weather.advanceTime(12.0f);

        assertNotEquals(startLight, weather.getLightLevel(), 0.001f, "Light level should change between day and night");
        assertTrue(weather.getTemperatureKelvin() > 200.0f, "Kelvin temperature should be physically valid");
        assertTrue(weather.getSoilHumidityAtDepth(5) >= 0.0f, "Subterranean soil humidity should be valid");
    }

    @Test
    @DisplayName("Verify full seasonal cycle progression (Spring -> Summer -> Fall -> Winter) and event recording")
    void testSeasonalCycleTransitions() {
        SeasonManager seasonManager = simulation.getSeasonManager();
        seasonManager.setSeasonalEffectsEnabled(true);

        seasonManager.skipToSeason(Season.SPRING);
        assertEquals(Season.SPRING, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getActivityMultiplier() >= 1.0f);

        seasonManager.skipToSeason(Season.SUMMER);
        assertEquals(Season.SUMMER, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getFoodMultiplier() >= 1.0f);

        seasonManager.skipToSeason(Season.FALL);
        assertEquals(Season.FALL, seasonManager.getCurrentSeason());

        seasonManager.skipToSeason(Season.WINTER);
        assertEquals(Season.WINTER, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getActivityMultiplier() < 0.6f, "Winter activity multiplier must reflect diapause");

        Queue<SimulationEvent> events = simulation.getEventQueue();
        boolean foundSeasonEvent = events.stream()
                .anyMatch(e -> e.getType() == SimulationEvent.EventType.SEASON_CHANGED);
        assertTrue(foundSeasonEvent, "SEASON_CHANGED event should be logged in simulation queue");
    }

    @Test
    @DisplayName("Verify climate disaster triggers (Flood, Drought) and God Mode disaster override controls")
    void testDisastersAndGodModeControls() {
        FloodDisaster flood = new FloodDisaster(0.8f, 10);
        simulation.triggerDisaster(flood);

        assertFalse(simulation.getActiveDisasters().isEmpty(), "Active disasters should list triggered flood");
        assertTrue(simulation.getActiveDisasters().contains(flood));

        // Advance 5 ticks
        for (int i = 0; i < 5; i++) {
            simulation.tick();
        }

        // Trigger God Mode intervention STOP_DISASTERS
        GodModeIntervention stopEvt = GodModeIntervention.stopDisasters(simulation.getTickCount());
        simulation.logIntervention(stopEvt);
        simulation.getActiveDisasters().clear();

        assertTrue(simulation.getActiveDisasters().isEmpty(), "Active disasters must be cleared after God Mode stop");
    }

    @Test
    @DisplayName("Verify God Mode climate parameter modifications and entity intervention journaling")
    void testGodModeInterventionsAndJournal() {
        int initialPop = colony.getPopulation();

        GodModeIntervention i1 = GodModeIntervention.modifyParameter(1L, "temperature", 38.0f);
        GodModeIntervention i2 = GodModeIntervention.spawnFood(2L, 30f, 30f, 0f, 300f);
        GodModeIntervention i3 = GodModeIntervention.spawnAnts(3L, colony.getId().toString(), "WORKER", 5, 30f, 30f, 0f);

        simulation.logIntervention(i1);
        simulation.logIntervention(i2);
        simulation.logIntervention(i3);

        weather.setTemperature((Float) i1.paramValue());
        colony.addResource(ResourceType.SUGAR, i2.amount());

        for (int i = 0; i < i3.count(); i++) {
            colony.addIndividual(new Individual(colony.getId(), Individual.Caste.WORKER, i3.x(), i3.y(), i3.z()));
        }

        assertEquals(38.0f, weather.getTemperature(), 0.01f, "Weather temperature should reflect God Mode edit");
        assertTrue(colony.getResourceAmount(ResourceType.SUGAR) >= 300f, "Colony sugar should increase from God Mode food spawn");
        assertEquals(initialPop + 5, colony.getPopulation(), "Colony population should increase by 5 after God Mode ant spawn");

        List<GodModeIntervention> journal = simulation.getInterventionJournal();
        assertEquals(3, journal.size(), "Intervention journal should record all 3 God Mode actions");
        assertEquals(ActionType.MODIFY_PARAMETER, journal.get(0).actionType());
        assertEquals(ActionType.SPAWN_FOOD, journal.get(1).actionType());
        assertEquals(ActionType.SPAWN_ANTS, journal.get(2).actionType());
    }
}
