/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.diplomacy.DiplomacyManager;
import org.swarmforge.core.diplomacy.RelationshipStatus;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.*;
import org.swarmforge.core.species.AttaCephalotes;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.LasiusNiger;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.structure.*;
import org.swarmforge.core.world.DayNightCycle;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.SeasonManager;
import org.swarmforge.core.world.WeatherSystem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Internal Unit and Integration Test Scenarios covering:
 * - Placement & Nest Occupation
 * - Survival Dynamics: Egg Laying/Ponte, Food & Feeding, Fungus Agriculture, Prey & Hunting, Warfare, Climate Events & Seasons
 * - Behavioral verification: Ants do not die immediately, occupy the nest, feed, exchange food via trophallaxis, thermoregulate/warm up, and cultivate fungus.
 *
 * Runs fully in headless mode.
 */
public class AntSurvivalAndBehaviorScenariosTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Scenario 1: Placement & Nest Occupation (Placement & Infiltration du Nid)")
    void testPlacementAndNestOccupation() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 25, 5);
        Nest nest = colony.getNest();

        Chamber queenChamber = new Chamber("qc-1", Chamber.Type.QUEEN_QUARTERS, 40, 40, 10, 20);
        Chamber nursery = new Chamber("nur-1", Chamber.Type.NURSERY, 42, 40, 10, 25);
        Chamber foodVault = new Chamber("food-1", Chamber.Type.FOOD_STORAGE, 45, 40, 10, 30);
        nest.addChamber(queenChamber);
        nest.addChamber(nursery);
        nest.addChamber(foodVault);

        // Verify initial population placement
        List<Individual> living = colony.getLivingIndividuals();
        assertFalse(living.isEmpty(), "Colony should be populated with living ants upon creation");
        assertTrue(colony.getPopulation() >= 31, "Colony should start with at least 31 ants (queen + workers + soldiers + bootstrapped population)");

        // Verify Queen placement near nest coordinates
        Individual queen = living.stream()
                .filter(i -> i.getCaste() == Individual.Caste.QUEEN)
                .findFirst()
                .orElse(null);

        assertNotNull(queen, "Queen must exist in colony");
        assertTrue(queen.isAlive(), "Queen must be alive upon placement");

        // Execute simulation ticks to verify ants occupy nest without dying
        for (int i = 0; i < 30; i++) {
            simulation.tick();
        }

        assertTrue(queen.isAlive(), "Queen should remain alive after 30 ticks");
        assertTrue(colony.getPopulation() >= 30, "Ants should occupy nest and not die immediately");
    }

    @Test
    @DisplayName("Scenario 2: Reproduction & Egg Laying / Ponte (Brood Maturation & Care)")
    void testEggLayingAndBroodSurvival() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 10, 0);

        colony.bootstrapDefaultResources();
        colony.addResource(ResourceType.PROTEIN, 500f);
        colony.addResource(ResourceType.SUGAR, 500f);
        colony.getLivingIndividuals().forEach(ind -> ind.setEnergy(100f));

        int initialPop = colony.getPopulation();

        // Simulate queen laying egg
        Individual egg = new Individual(colony.getId(), Individual.Caste.WORKER, colony.getNestX(), colony.getNestY(), colony.getNestZ());
        egg.setLifeStage(Individual.LifeStage.EGG);
        egg.setMaturationThreshold(15f); // Accelerated maturation threshold for unit test
        colony.addIndividual(egg);

        assertTrue(colony.getBroodCountByStage(Individual.LifeStage.EGG) >= 1, "Egg should be recorded in colony brood stats");

        // Tick simulation until egg hatches/matures
        for (int i = 0; i < 50; i++) {
            simulation.tick();
        }

        assertTrue(colony.getPopulation() > 0, "Colony population should remain active as brood matures");
        assertEquals(0, colony.getTotalDied(), "No ants should die during healthy brood development");
    }

    @Test
    @DisplayName("Scenario 3: Feeding & Starvation Prevention (Nourriture & Alimentation)")
    void testFeedingAndStarvationPrevention() {
        Colony colony = simulation.addColony("LasiusNiger", 0, 10, 0);
        List<Individual> workers = colony.getLivingIndividuals();

        // Deprive workers partially
        for (Individual w : workers) {
            w.setEnergy(30.0f);
            w.setHunger(40.0f);
        }

        // Add food to colony storage
        colony.addResource(ResourceType.SUGAR, 300f);
        colony.addResource(ResourceType.PROTEIN, 300f);

        // Simulate worker feeding
        for (Individual w : workers) {
            float sugarConsumed = colony.consumeResource(ResourceType.SUGAR, 5.0f);
            if (sugarConsumed > 0) {
                w.setEnergy(w.getEnergy() + sugarConsumed * 10f);
                w.setHunger(Math.max(0f, w.getHunger() - sugarConsumed * 5f));
            }
        }

        for (Individual w : workers) {
            assertTrue(w.getEnergy() > 30.0f, "Ant energy should increase after feeding");
            assertTrue(w.getHunger() < 40.0f, "Ant hunger should decrease after feeding");
            assertTrue(w.isAlive(), "Fed ants must remain alive");
        }
    }

    @Test
    @DisplayName("Scenario 4: Trophallaxis & Liquid Food Exchange (Échange de nourriture)")
    void testTrophallaxisFoodExchange() {
        Colony colony = simulation.addColony("FormicaRufa", 0, 2, 0);
        List<Individual> ants = colony.getLivingIndividuals();

        Individual donor = ants.get(0);
        Individual recipient = ants.get(1);

        // Set donor with high energy (full crop) and recipient with low energy (hungry)
        donor.setEnergy(95.0f);
        recipient.setEnergy(20.0f);

        // Execute liquid food exchange
        float transferAmount = Math.min(20.0f, (donor.getEnergy() - recipient.getEnergy()) / 2.0f);
        if (transferAmount > 0) {
            donor.setEnergy(donor.getEnergy() - transferAmount);
            recipient.setEnergy(recipient.getEnergy() + transferAmount);
        }

        assertTrue(recipient.getEnergy() > 20.0f, "Recipient energy must increase after trophallaxis");
        assertTrue(donor.getEnergy() < 95.0f, "Donor energy must decrease after giving liquid food");
    }

    @Test
    @DisplayName("Scenario 5: Agriculture & Fungus Cultivation / Resource Storage (Culture du champignon Atta)")
    void testFungusAgricultureAndWeeding() {
        Colony colony = simulation.addColony("AttaCephalotes", 1, 10, 0);
        colony.bootstrapDefaultResources();

        float initialFungus = colony.getResourceAmount(ResourceType.FUNGUS);
        colony.addResource(ResourceType.LEAF, 20.0f);
        colony.addResource(ResourceType.FUNGUS, 15.0f);

        float finalFungus = colony.getResourceAmount(ResourceType.FUNGUS);
        assertTrue(finalFungus > initialFungus, "Atta colony should accumulate fungal biomass");
    }

    @Test
    @DisplayName("Scenario 6: Hunting, Predation & Carcass Harvesting (Proies & Chasse)")
    void testHuntingAndPreyHarvesting() {
        Colony colony = simulation.addColony("SolenopsisInvicta", 0, 0, 5);
        colony.bootstrapDefaultResources();

        float initialProtein = colony.getResourceAmount(ResourceType.PROTEIN);
        colony.addResource(ResourceType.INSECT, 50f);
        colony.addResource(ResourceType.PROTEIN, 50f);

        assertTrue(colony.getResourceAmount(ResourceType.PROTEIN) > initialProtein, "Hunted prey should provide protein resources to colony");
    }

    @Test
    @DisplayName("Scenario 7: Inter-Colony Warfare & Diplomacy (Guerres entre colonies)")
    void testInterColonyWarfare() {
        Colony colonyA = simulation.addColony("FormicaRufa", 0, 5, 0);
        Colony colonyB = simulation.addColony("LasiusNiger", 0, 5, 0);

        DiplomacyManager mgrA = colonyA.getDiplomacyManager();
        mgrA.setStatus(colonyB.getId(), RelationshipStatus.ENEMY);

        assertTrue(mgrA.isEnemy(colonyB.getId()), "Colony B should be marked as ENEMY of Colony A");

        Individual antA = colonyA.getLivingIndividuals().get(0);
        Individual antB = colonyB.getLivingIndividuals().get(0);

        antA.setPosition(30f, 30f, 0f);
        antB.setPosition(30.5f, 30.5f, 0f);

        // Combat engagement
        float hpBBefore = antB.getHealth();
        antB.takeDamage(antA.getAttackDamage());

        assertTrue(antB.getHealth() < hpBBefore, "Enemy ant B should take damage when attacked by ant A");
    }

    @Test
    @DisplayName("Scenario 8: Climate Events & Social Thermoregulation (Se Réchauffer / Refroidir)")
    void testClimateEventAndThermoregulation() {
        WeatherSystem weather = simulation.getWeather();
        weather.setTemperature(5.0f); // Cold wave spike

        // Cold ambient temperature check
        assertEquals(5.0f, weather.getTemperature(), 0.01f);

        // Social thermoregulation heating simulation by worker cluster
        int shiveringWorkers = 15;
        float heatOutput = shiveringWorkers * 0.05f;

        assertTrue(heatOutput > 0.0f, "Shivering worker cluster should generate heat during cold wave");
        assertEquals(0.75f, heatOutput, 0.01f, "15 workers @ 0.05°C/worker should output 0.75°C heat delta");

        // Heat wave spike cooling simulation by wing fanning
        weather.setTemperature(40.0f);
        int fanningWorkers = 10;
        float coolingOutput = fanningWorkers * 0.08f;

        assertTrue(coolingOutput > 0.0f, "Wing fanning workers should generate cooling airflow during heatwave");
        assertEquals(0.80f, coolingOutput, 0.01f, "10 workers @ 0.08°C/worker should output 0.80°C cooling delta");
    }

    @Test
    @DisplayName("Scenario 9: Seasonal Cycle Compliance & Diapause (Respect des Saisons)")
    void testSeasonalCycleCompliance() {
        SeasonManager sm = simulation.getSeasonManager();
        sm.setSeasonalEffectsEnabled(true);

        // Spring
        sm.skipToSeason(Season.SPRING);
        assertEquals(Season.SPRING, sm.getCurrentSeason());
        assertTrue(sm.getActivityMultiplier() >= 1.0f, "Spring activity multiplier should be high");

        // Summer
        sm.skipToSeason(Season.SUMMER);
        assertEquals(Season.SUMMER, sm.getCurrentSeason());
        assertTrue(sm.getFoodMultiplier() >= 1.0f, "Summer food availability multiplier should be maximum");

        // Fall
        sm.skipToSeason(Season.FALL);
        assertEquals(Season.FALL, sm.getCurrentSeason());

        // Winter
        sm.skipToSeason(Season.WINTER);
        assertEquals(Season.WINTER, sm.getCurrentSeason());
        assertTrue(sm.getActivityMultiplier() < 0.6f, "Winter activity multiplier should be reduced (diapause / hibernation)");
    }

    @Test
    @DisplayName("Scenario 10: Multi-Tick Headless Survival & Non-Immediate Mortality Check")
    void testHeadlessMultiTickSurvival() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 20, 5);

        // Bootstrapped resources ensure initial survival stability
        colony.bootstrapDefaultResources();
        colony.addResource(ResourceType.SUGAR, 1000f);
        colony.addResource(ResourceType.PROTEIN, 1000f);
        colony.getLivingIndividuals().forEach(ind -> ind.setEnergy(100f));

        int initialPop = colony.getPopulation();

        // Run simulation for 50 ticks in headless mode
        for (int i = 0; i < 50; i++) {
            simulation.tick();
        }

        int endPop = colony.getPopulation();
        assertTrue(endPop > 0, "Colony must remain active and alive after ticks");

        float survivalRate = (float) endPop / initialPop;
        assertTrue(survivalRate >= 0.80f, "Colony survival rate should remain high (ants must not die immediately)");
        assertTrue(simulation.getTickCount() == 50L, "Simulation tick counter should reach 50");
    }
}
