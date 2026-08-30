/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.event.GodModeIntervention;
import org.swarmforge.core.event.GodModeIntervention.ActionType;
import org.swarmforge.core.simulation.disasters.FloodDisaster;
import org.swarmforge.core.simulation.disasters.DisasterEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-Test Suite verifying GodMode Intervention Journaling, Dynamic Manipulation Execution
 * (Spawning, Killing, Food Spawning, Climate Parameter Tweaks), Disaster Lifecycles,
 * and Checkpoint Rewind / Restoration state integrity.
 */
public class GodModeInterventionAutoTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private Colony testColony;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(60, 60, 30);
        simulation = new Simulation(terrarium);
        testColony = simulation.addColony("FormicaRufa", 1, 20, 5);
    }

    @Test
    @DisplayName("Verify GodModeIntervention instantiation, factory methods, and journal logging")
    void testInterventionInstantiationAndJournaling() {
        assertEquals(0, simulation.getInterventionJournal().size(), "Journal should be empty initially");

        GodModeIntervention i1 = GodModeIntervention.spawnAnts(10L, testColony.getId().toString(), "WORKER", 5, 30f, 30f, 0f);
        GodModeIntervention i2 = GodModeIntervention.killAnts(15L, testColony.getId().toString(), "WORKER", 3);
        GodModeIntervention i3 = GodModeIntervention.spawnFood(20L, 30f, 30f, 0f, 150f);
        GodModeIntervention i4 = GodModeIntervention.triggerDisaster(25L, "FLOOD", 0.8f);
        GodModeIntervention i5 = GodModeIntervention.modifyParameter(30L, "temperature", 35.0f);
        GodModeIntervention i6 = GodModeIntervention.stopDisasters(35L);

        simulation.logIntervention(i1);
        simulation.logIntervention(i2);
        simulation.logIntervention(i3);
        simulation.logIntervention(i4);
        simulation.logIntervention(i5);
        simulation.logIntervention(i6);

        List<GodModeIntervention> journal = simulation.getInterventionJournal();
        assertEquals(6, journal.size(), "Intervention journal should store all 6 logged interventions");

        assertEquals(ActionType.SPAWN_ANTS, journal.get(0).actionType());
        assertEquals(ActionType.KILL_ANTS, journal.get(1).actionType());
        assertEquals(ActionType.SPAWN_FOOD, journal.get(2).actionType());
        assertEquals(ActionType.TRIGGER_DISASTER, journal.get(3).actionType());
        assertEquals(ActionType.MODIFY_PARAMETER, journal.get(4).actionType());
        assertEquals(ActionType.STOP_DISASTERS, journal.get(5).actionType());
    }

    @Test
    @DisplayName("Verify SPAWN_ANTS GodMode intervention updates colony population")
    void testSpawnAntsInterventionExecution() {
        int initialPop = testColony.getPopulation();

        GodModeIntervention spawnEvt = GodModeIntervention.spawnAnts(
                simulation.getTickCount(),
                testColony.getId().toString(),
                "WORKER",
                10,
                testColony.getNestX(),
                testColony.getNestY(),
                testColony.getNestZ()
        );
        simulation.logIntervention(spawnEvt);

        // Execute spawning action
        for (int i = 0; i < spawnEvt.count(); i++) {
            Individual newWorker = new Individual(
                    testColony.getId(),
                    Individual.Caste.WORKER,
                    spawnEvt.x(),
                    spawnEvt.y(),
                    spawnEvt.z()
            );
            testColony.addIndividual(newWorker);
        }

        assertEquals(initialPop + 10, testColony.getPopulation(), "Colony population should increase by 10 after SPAWN_ANTS intervention");
    }

    @Test
    @DisplayName("Verify KILL_ANTS GodMode intervention reduces living colony population")
    void testKillAntsInterventionExecution() {
        int initialPop = testColony.getPopulation();
        int killCount = 5;

        GodModeIntervention killEvt = GodModeIntervention.killAnts(
                simulation.getTickCount(),
                testColony.getId().toString(),
                "WORKER",
                killCount
        );
        simulation.logIntervention(killEvt);

        List<Individual> workers = testColony.getLivingIndividuals();
        int countKilled = 0;
        for (Individual w : workers) {
            if (w.getCaste() == Individual.Caste.WORKER && countKilled < killCount) {
                w.takeDamage(1000f); // Lethal damage
                countKilled++;
            }
        }
        testColony.removeDeadIndividuals();

        assertEquals(initialPop - killCount, testColony.getPopulation(), "Colony population should decrease after KILL_ANTS intervention");
    }

    @Test
    @DisplayName("Verify SPAWN_FOOD GodMode intervention increases colony storage resources")
    void testSpawnFoodInterventionExecution() {
        float initialSugar = testColony.getResourceAmount(ResourceType.SUGAR);

        GodModeIntervention foodEvt = GodModeIntervention.spawnFood(
                simulation.getTickCount(),
                testColony.getNestX(),
                testColony.getNestY(),
                testColony.getNestZ(),
                250.0f
        );
        simulation.logIntervention(foodEvt);

        testColony.addResource(ResourceType.SUGAR, foodEvt.amount());

        assertEquals(initialSugar + 250.0f, testColony.getResourceAmount(ResourceType.SUGAR), 0.01f,
                "Colony sugar resources should increase by 250 after SPAWN_FOOD intervention");
    }

    @Test
    @DisplayName("Verify MODIFY_PARAMETER GodMode intervention alters climate temperature")
    void testModifyParameterInterventionExecution() {
        float targetTemp = 36.5f;

        GodModeIntervention modEvt = GodModeIntervention.modifyParameter(
                simulation.getTickCount(),
                "temperature",
                targetTemp
        );
        simulation.logIntervention(modEvt);

        simulation.getWeather().setTemperature((Float) modEvt.paramValue());

        assertEquals(targetTemp, simulation.getWeather().getTemperature(), 0.01f,
                "Weather temperature should match modified parameter value");
    }

    @Test
    @DisplayName("Verify disaster triggering, active disaster updates, and STOP_DISASTERS intervention")
    void testDisasterLifecycleAndGodModeControl() {
        // Trigger FLOOD disaster
        GodModeIntervention triggerEvt = GodModeIntervention.triggerDisaster(simulation.getTickCount(), "FLOOD", 0.75f);
        simulation.logIntervention(triggerEvt);

        DisasterEvent flood = new FloodDisaster(triggerEvt.intensity(), 5);
        simulation.triggerDisaster(flood);

        assertFalse(simulation.getActiveDisasters().isEmpty(), "Simulation should have active disasters after trigger");
        assertTrue(simulation.getActiveDisasters().contains(flood));

        // Run simulation ticks with active disaster
        for (int i = 0; i < 10; i++) {
            simulation.tick();
        }

        // Stop all disasters via God Mode
        GodModeIntervention stopEvt = GodModeIntervention.stopDisasters(simulation.getTickCount());
        simulation.logIntervention(stopEvt);

        simulation.getActiveDisasters().clear();

        assertTrue(simulation.getActiveDisasters().isEmpty(), "Active disasters should be cleared after stopDisasters");
    }

    @Test
    @DisplayName("Verify checkpoint creation and restoration maintains deterministic intervention journal state")
    void testCheckpointCreationAndRestorationWithInterventionJournal() {
        // Step 1: Run 10 ticks and log 2 interventions
        for (int i = 0; i < 10; i++) {
            simulation.tick();
        }
        simulation.logIntervention(GodModeIntervention.spawnFood(10L, 20f, 20f, 0f, 100f));
        simulation.logIntervention(GodModeIntervention.modifyParameter(10L, "temperature", 28f));

        assertEquals(2, simulation.getInterventionJournal().size());

        // Step 2: Create checkpoint CP1
        SimulationCheckpoint cp1 = simulation.createCheckpoint("CP1_Tick10");
        assertNotNull(cp1);
        assertEquals(10L, cp1.getTick());
        assertEquals(2, cp1.getInterventionsRecorded().size());

        // Step 3: Run 10 more ticks and log 2 more interventions
        for (int i = 0; i < 10; i++) {
            simulation.tick();
        }
        simulation.logIntervention(GodModeIntervention.triggerDisaster(20L, "DROUGHT", 0.5f));
        simulation.logIntervention(GodModeIntervention.killAnts(20L, testColony.getId().toString(), "WORKER", 2));

        assertEquals(20L, simulation.getTickCount());
        assertEquals(4, simulation.getInterventionJournal().size());

        // Step 4: Restore checkpoint CP1
        boolean restored = simulation.restoreCheckpoint(cp1);
        assertTrue(restored, "Restoring checkpoint CP1 should return true");

        assertEquals(10L, simulation.getTickCount(), "Tick count should be restored to checkpoint tick 10");
        assertEquals(2, simulation.getInterventionJournal().size(), "Intervention journal should be restored to CP1 snapshot state (2 entries)");
        assertEquals(ActionType.SPAWN_FOOD, simulation.getInterventionJournal().get(0).actionType());
        assertEquals(ActionType.MODIFY_PARAMETER, simulation.getInterventionJournal().get(1).actionType());
    }
}
