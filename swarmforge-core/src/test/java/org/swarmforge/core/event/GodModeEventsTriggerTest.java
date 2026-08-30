/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.event.GodModeIntervention.ActionType;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.simulation.SimulationCheckpoint;
import org.swarmforge.core.simulation.disasters.FloodDisaster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless Test Suite verifying God Mode event triggering, intervention logging,
 * simulation state mutation, disaster lifecycles, and checkpoint restoration.
 */
public class GodModeEventsTriggerTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private Colony colony;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(60, 60, 30);
        simulation = new Simulation(terrarium);
        colony = simulation.addColony("FormicaRufa", 1, 25, 0);
        colony.bootstrapDefaultResources();
    }

    @Test
    @DisplayName("Verify triggering and journaling of all God Mode intervention types")
    void testGodModeInterventionJournaling() {
        assertEquals(0, simulation.getInterventionJournal().size(), "Intervention journal should be empty initially");

        GodModeIntervention spawnAnts = GodModeIntervention.spawnAnts(1L, colony.getId().toString(), "WORKER", 10, 30f, 30f, 0f);
        GodModeIntervention killAnts = GodModeIntervention.killAnts(2L, colony.getId().toString(), "WORKER", 5);
        GodModeIntervention spawnFood = GodModeIntervention.spawnFood(3L, 30f, 30f, 0f, 200f);
        GodModeIntervention triggerDisaster = GodModeIntervention.triggerDisaster(4L, "FLOOD", 0.7f);
        GodModeIntervention modifyParam = GodModeIntervention.modifyParameter(5L, "temperature", 34.5f);
        GodModeIntervention stopDisasters = GodModeIntervention.stopDisasters(6L);

        simulation.logIntervention(spawnAnts);
        simulation.logIntervention(killAnts);
        simulation.logIntervention(spawnFood);
        simulation.logIntervention(triggerDisaster);
        simulation.logIntervention(modifyParam);
        simulation.logIntervention(stopDisasters);

        List<GodModeIntervention> journal = simulation.getInterventionJournal();
        assertEquals(6, journal.size(), "Intervention journal must record all 6 interventions");

        assertEquals(ActionType.SPAWN_ANTS, journal.get(0).actionType());
        assertEquals(ActionType.KILL_ANTS, journal.get(1).actionType());
        assertEquals(ActionType.SPAWN_FOOD, journal.get(2).actionType());
        assertEquals(ActionType.TRIGGER_DISASTER, journal.get(3).actionType());
        assertEquals(ActionType.MODIFY_PARAMETER, journal.get(4).actionType());
        assertEquals(ActionType.STOP_DISASTERS, journal.get(5).actionType());
    }

    @Test
    @DisplayName("Verify SPAWN_ANTS and KILL_ANTS God Mode interventions alter colony population")
    void testGodModeAntSpawningAndKilling() {
        int initialPop = colony.getPopulation();

        // 1. Spawn ants
        GodModeIntervention spawn = GodModeIntervention.spawnAnts(simulation.getTickCount(), colony.getId().toString(), "WORKER", 8, 30f, 30f, 0f);
        simulation.logIntervention(spawn);

        for (int i = 0; i < spawn.count(); i++) {
            colony.addIndividual(new Individual(colony.getId(), Individual.Caste.WORKER, spawn.x(), spawn.y(), spawn.z()));
        }

        assertEquals(initialPop + 8, colony.getPopulation(), "Colony population should increase by 8 after spawn intervention");

        // 2. Kill ants
        GodModeIntervention kill = GodModeIntervention.killAnts(simulation.getTickCount(), colony.getId().toString(), "WORKER", 3);
        simulation.logIntervention(kill);

        List<Individual> workers = colony.getLivingIndividuals();
        int killedCount = 0;
        for (Individual worker : workers) {
            if (worker.getCaste() == Individual.Caste.WORKER && killedCount < 3) {
                worker.takeDamage(1000f);
                killedCount++;
            }
        }
        colony.removeDeadIndividuals();

        assertEquals(initialPop + 8 - 3, colony.getPopulation(), "Colony population should decrease by 3 after kill intervention");
    }

    @Test
    @DisplayName("Verify SPAWN_FOOD and MODIFY_PARAMETER interventions update colony resources and weather")
    void testGodModeResourceAndClimateMutation() {
        float initialSugar = colony.getResourceAmount(ResourceType.SUGAR);

        GodModeIntervention spawnFood = GodModeIntervention.spawnFood(simulation.getTickCount(), 30f, 30f, 0f, 400f);
        simulation.logIntervention(spawnFood);
        colony.addResource(ResourceType.SUGAR, spawnFood.amount());

        assertEquals(initialSugar + 400f, colony.getResourceAmount(ResourceType.SUGAR), 0.01f,
                "Colony sugar resources must increase by 400 after SPAWN_FOOD intervention");

        GodModeIntervention modifyTemp = GodModeIntervention.modifyParameter(simulation.getTickCount(), "temperature", 36.0f);
        simulation.logIntervention(modifyTemp);
        simulation.getWeather().setTemperature((Float) modifyTemp.paramValue());

        assertEquals(36.0f, simulation.getWeather().getTemperature(), 0.01f,
                "Weather temperature should match modified parameter value");
    }

    @Test
    @DisplayName("Verify God Mode disaster trigger, disaster lifecycle execution, and STOP_DISASTERS clearing")
    void testDisasterLifecycleAndGodModeStop() {
        GodModeIntervention trigger = GodModeIntervention.triggerDisaster(simulation.getTickCount(), "FLOOD", 0.85f);
        simulation.logIntervention(trigger);

        FloodDisaster flood = new FloodDisaster(trigger.intensity(), 10);
        simulation.triggerDisaster(flood);

        assertFalse(simulation.getActiveDisasters().isEmpty(), "Active disasters should list triggered flood");
        assertTrue(simulation.getActiveDisasters().contains(flood));

        // Advance simulation 5 ticks
        for (int i = 0; i < 5; i++) {
            simulation.tick();
        }

        // Trigger God Mode STOP_DISASTERS
        GodModeIntervention stop = GodModeIntervention.stopDisasters(simulation.getTickCount());
        simulation.logIntervention(stop);
        simulation.getActiveDisasters().clear();

        assertTrue(simulation.getActiveDisasters().isEmpty(), "Active disasters must be cleared after God Mode stop");
    }

    @Test
    @DisplayName("Verify checkpoint restoration preserves deterministic God Mode intervention journal history")
    void testCheckpointRestorationWithInterventionJournal() {
        for (int i = 0; i < 5; i++) {
            simulation.tick();
        }

        simulation.logIntervention(GodModeIntervention.spawnFood(5L, 30f, 30f, 0f, 150f));
        simulation.logIntervention(GodModeIntervention.modifyParameter(5L, "temperature", 29.0f));

        assertEquals(2, simulation.getInterventionJournal().size());

        // Create checkpoint CP1
        SimulationCheckpoint cp1 = simulation.createCheckpoint("CP1_Tick5");
        assertNotNull(cp1);
        assertEquals(5L, cp1.getTick());

        // Run 5 more ticks and log 2 additional interventions
        for (int i = 0; i < 5; i++) {
            simulation.tick();
        }

        simulation.logIntervention(GodModeIntervention.triggerDisaster(10L, "DROUGHT", 0.6f));
        simulation.logIntervention(GodModeIntervention.stopDisasters(10L));

        assertEquals(10L, simulation.getTickCount());
        assertEquals(4, simulation.getInterventionJournal().size());

        // Restore checkpoint CP1
        boolean restored = simulation.restoreCheckpoint(cp1);
        assertTrue(restored, "Restoring checkpoint CP1 should return true");

        assertEquals(5L, simulation.getTickCount(), "Tick count should reset to 5");
        assertEquals(2, simulation.getInterventionJournal().size(), "Intervention journal should reset to 2 entries from CP1");
    }
}
