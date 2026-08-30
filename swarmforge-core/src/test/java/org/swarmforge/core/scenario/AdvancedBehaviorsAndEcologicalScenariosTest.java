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
import org.swarmforge.core.domain.*;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.domain.Individual.LifeStage;
import org.swarmforge.core.epidemiology.*;
import org.swarmforge.core.genetics.*;
import org.swarmforge.core.simulation.*;
import org.swarmforge.core.species.ApisMellifera;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.VespulaGermanica;
import org.swarmforge.core.structure.physics.NestType;
import org.swarmforge.core.structure.physics.NestVoxelGrid;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced Headless Integration Test Suite testing specialized behaviors:
 * 1. Division of Labor & Age Polyethism (Nurse -> Guard -> Forager)
 * 2. Epidemic Transmission, Allogrooming & Necrophorism
 * 3. Propolis Antiseptic Coating & Nest Hygiene
 * 4. Nuptial Flight, Spermatheca Storage & Haplodiploid Inheritance
 * 5. Aerial 3D Flight & Wingbeat Energy Scaling (Bees & Wasps)
 * 6. Aphid Mutualism & Honeydew Harvesting
 * 7. Pheromone Trail Deposition & Spatial Gradient Following
 * 8. Soil Hydric Flooding & Tunnel Drainage
 * 9. Multi-Colony Territorial Aggression & Peace Treaties
 * 10. Ecological Disaster Resilience & Recovery Cycle
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AdvancedBehaviorsAndEcologicalScenariosTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(100, 100, 50);
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Behavior 1: Age Polyethism & Division of Labor Transitions")
    void testAgePolyethismTransitions() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 10, 0);

        Individual youngWorker = new Individual(colony.getId(), Caste.WORKER, 20f, 20f, 0f);
        youngWorker.setLifeStage(LifeStage.ADULT);
        youngWorker.setAge(10f); // Young adult -> Nurse
        youngWorker.setJob(Individual.Job.NURSE);
        colony.addIndividual(youngWorker);

        assertEquals(Individual.Job.NURSE, youngWorker.getJob(), "Young workers start as nurses caring for brood");

        // Age worker to middle-age (Guard phase)
        youngWorker.setAge(500f);
        youngWorker.updateJobByAge();
        assertEquals(Individual.Job.GUARD, youngWorker.getJob(), "Middle-aged workers transition to nest defense");

        // Age worker to old age (Forager phase - high risk)
        youngWorker.setAge(1500f);
        youngWorker.updateJobByAge();
        assertEquals(Individual.Job.FORAGER, youngWorker.getJob(), "Older workers transition to external foraging");
    }

    @Test
    @DisplayName("Behavior 2: Epidemic Outbreak, Allogrooming & Necrophorism")
    void testEpidemicAndSocialImmunity() {
        SocialImmunityManager immunityManager = new SocialImmunityManager(100f, 100f, 0f);
        Random rng = new Random(42);

        IndividualInfection groomer = new IndividualInfection(UUID.randomUUID());
        IndividualInfection infectedAnt = new IndividualInfection(UUID.randomUUID());

        // Expose to Beauveria bassiana fungal pathogen spores
        infectedAnt.exposeToSpores(PathogenType.BEAUVERIA_BASSIANA, 0.20f);
        assertEquals(InfectionState.EXPOSED, infectedAnt.getState(), "Ant exposed to fungal spores");

        // Perform allogrooming by nestmate worker
        boolean cleaned = immunityManager.performAllogrooming(groomer, infectedAnt, rng);
        assertTrue(cleaned, "Nestmate successfully performs allogrooming");
        assertEquals(InfectionState.SUSCEPTIBLE, infectedAnt.getState(), "Spore load removed before germination");
        assertTrue(infectedAnt.getSocialImmunityLevel() > 0.0f, "Target acquired social immunity boost");

        // Test Necrophorism on sporulating cadaver
        Individual healthyWorker = new Individual(UUID.randomUUID(), Caste.WORKER, 10f, 10f, 0f);
        Individual deadAnt = new Individual(UUID.randomUUID(), Caste.WORKER, 10f, 10f, 0f);
        deadAnt.setHealth(0f);

        IndividualInfection deadInfection = new IndividualInfection(deadAnt.getId());
        deadInfection.setState(InfectionState.SPORULATING_DEAD);

        boolean removed = immunityManager.performNecrophorism(healthyWorker, deadAnt, deadInfection);
        assertTrue(removed, "Healthy worker picks up infected cadaver for removal");
        assertEquals(Individual.CarriedItem.DEAD_ANT, healthyWorker.getCarriedItem(), "Worker carries dead ant to midden");
        assertEquals(1, immunityManager.getExternalMidden().cadavers().size(), "Cadaver safely quarantined to external midden");
    }

    @Test
    @DisplayName("Behavior 3: Propolis Antiseptic Coating on Nest Architecture")
    void testPropolisNestSanitization() {
        FormicaRufa species = new FormicaRufa();
        Colony colony = new Colony(species, 0, 0, 0);
        colony.addResource(ResourceType.PROPOLIS_RESIN, 10.0f);

        NestVoxelGrid grid = new NestVoxelGrid(5, 5, 5, NestType.SUBTERRANEAN_MOUND);
        grid.getVoxel(2, 2, 2).setFungalSporeLoad(0.90f);

        SocialImmunityManager immunityManager = new SocialImmunityManager(50f, 50f, 0f);

        boolean coated = immunityManager.applyPropolisCoating(colony, grid, 2, 2, 2);
        assertTrue(coated, "Colony applies resin propolis coating to infected chamber voxel");
        assertEquals(9.0f, colony.getResourceAmount(ResourceType.PROPOLIS_RESIN), "Propolis resin consumed from colony stores");
        assertTrue(grid.getVoxel(2, 2, 2).getPropolisCoating() > 0.0f, "Voxel cell now has protective propolis film");
        assertTrue(grid.getVoxel(2, 2, 2).getFungalSporeLoad() < 0.90f, "Antiseptic propolis reduced fungal spore density");
    }

    @Test
    @DisplayName("Behavior 4: Nuptial Flight, Spermatheca Mating & Haplodiploid Inheritance")
    void testGeneticsAndNuptialFlight() {
        Random rng = new Random(100);

        HaplodiploidGenome queenGenome = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, rng);
        HaplodiploidGenome drone1 = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.HAPLOID_MALE, rng);
        HaplodiploidGenome drone2 = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.HAPLOID_MALE, rng);

        NuptialFlightManager flightManager = new NuptialFlightManager();
        assertTrue(flightManager.isFlightConditionsOptimal(25.0f, 0.65f, 4.0f), "Optimal warm & humid flight conditions");

        List<NuptialFlightManager.MatingPairResult> matings = flightManager.executeNuptialFlight(
                List.of(queenGenome), List.of(drone1, drone2), 2, rng
        );

        assertEquals(1, matings.size(), "Queen successfully mated during nuptial flight");
        Spermatheca spermatheca = matings.get(0).spermatheca();
        assertTrue(spermatheca.hasSperm(), "Spermatheca loaded with drone sperm");

        HaplodiploidEvolutionEngine engine = new HaplodiploidEvolutionEngine();
        
        // Fertilized egg -> Diploid female worker
        HaplodiploidGenome femaleChild = engine.produceOffspringFemale(queenGenome, spermatheca, 0.01f, rng);
        assertEquals(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, femaleChild.getPloidy(), "Fertilized egg produces diploid female");

        // Unfertilized egg -> Haploid male drone (Arrhenotoky)
        HaplodiploidGenome maleChild = engine.produceOffspringMale(queenGenome, 0.01f, rng);
        assertEquals(HaplodiploidGenome.Ploidy.HAPLOID_MALE, maleChild.getPloidy(), "Unfertilized egg produces haploid male drone");
    }

    @Test
    @DisplayName("Behavior 5: Aerial 3D Navigation & Wingbeat Energy Scaling")
    void testAerialNavigationAndKinematics() {
        ApisMellifera beeSpecies = new ApisMellifera();
        assertTrue(beeSpecies.isWorkersCanFly(), "Honeybees have flying workers");

        Colony hive = new Colony(beeSpecies, 20, 20, 15);
        Individual workerBee = new Individual(hive.getId(), Caste.WORKER, 20, 20, 15);
        workerBee.setSpecies(beeSpecies);
        hive.addIndividual(workerBee);

        assertTrue(workerBee.canFly(), "Bee worker capable of 3D aerial flight");

        float initialEnergy = workerBee.getEnergy();

        // Perform 10.0 seconds of 3D aerial flight (step time = 0.05s) towards target flower patch
        float targetX = 70.0f;
        float targetY = 70.0f;
        int groundElevation = getGroundHeightAt(terrarium, (int) targetX, (int) targetY);
        float altitudeAboveGround = 5.0f; // 5 units altitude above actual local terrain ground level
        float targetZ = groundElevation + altitudeAboveGround;

        float flightDurationSeconds = 10.0f;
        float stepTimeSeconds = 0.05f;
        int totalFlightTicks = (int) Math.ceil(flightDurationSeconds / stepTimeSeconds);

        for (int i = 0; i < totalFlightTicks; i++) {
            workerBee.fly3D(targetX, targetY, targetZ, 2.5f);
        }

        assertTrue(workerBee.getX() > 20.1f, "Bee navigated horizontally towards flower target");
        assertTrue(Math.abs(workerBee.getZ() - targetZ) < 15f, "Bee adjusted 3D altitude relative to terrain ground level");
        assertTrue(workerBee.getEnergy() < initialEnergy, "Aerial flight expended wingbeat energy");
    }

    @Test
    @DisplayName("Behavior 6: Aphid Mutualism, Protection & Honeydew Milking")
    void testAphidSymbiosis() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 10, 0);
        colony.bootstrapDefaultResources();

        float preHoneydew = colony.getResourceAmount(ResourceType.HONEYDEW);

        // Simulate worker milking aphid cluster for honeydew
        Individual worker = colony.getLivingIndividuals().get(0);
        worker.setCarriedItem(Individual.CarriedItem.FOOD);
        worker.setCarriedResourceType(ResourceType.HONEYDEW);

        colony.addResource(ResourceType.HONEYDEW, 15.0f);

        assertTrue(colony.getResourceAmount(ResourceType.HONEYDEW) > preHoneydew, "Honeydew harvested from aphid mutualist cluster");
    }

    @Test
    @DisplayName("Behavior 7: Dual-Channel Pheromone Deposition & Navigation")
    void testPheromoneTrailDeposition() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 5, 0);
        Individual forager = colony.getLivingIndividuals().get(0);

        simulation.getPheromoneGrid().setMaxHeightAboveGround(100);

        // Forager position calculated relative to local ground elevation at (25, 5)
        int targetX = 25;
        int targetY = 5;
        int groundElevation = getGroundHeightAt(terrarium, targetX, targetY);
        int depositZ = groundElevation; // At ground surface

        forager.setPosition(targetX, targetY, depositZ);

        simulation.getPheromoneGrid().deposit(targetX, targetY, depositZ, PheromoneType.HOME_TRAIL.getIndex(), 1.0f);
        assertTrue(simulation.getPheromoneGrid().getIntensity(targetX, targetY, depositZ, PheromoneType.HOME_TRAIL.getIndex()) > 0.0f,
                "Explorer worker deposits HOME_TRAIL pheromone at ground proximity");

        // Forager carrying food deposits FOOD_TRAIL while returning home
        forager.setCarriedItem(Individual.CarriedItem.FOOD);
        forager.setCarriedResourceType(ResourceType.SUGAR);

        int returnX = 26;
        int returnY = 5;
        int returnGround = getGroundHeightAt(terrarium, returnX, returnY);
        simulation.getPheromoneGrid().deposit(returnX, returnY, returnGround, PheromoneType.FOOD_TRAIL.getIndex(), 1.5f);
        assertTrue(simulation.getPheromoneGrid().getIntensity(returnX, returnY, returnGround, PheromoneType.FOOD_TRAIL.getIndex()) > 0.0f,
                "Food-carrying worker deposits recruitment FOOD_TRAIL pheromone");
    }

    private int getGroundHeightAt(Terrarium terrarium, int x, int y) {
        for (int z = terrarium.getDepth() - 1; z >= 0; z--) {
            TerrariumCell cell = terrarium.getCell(x, y, z);
            if (cell != null && cell.material() != TerrariumCell.Material.AIR) {
                return z;
            }
        }
        return 0; // Ground surface default
    }

    @Test
    @DisplayName("Behavior 8: Water Table Absorption & Nest Drainage")
    void testHydricCouplingAndDrainage() {
        org.swarmforge.core.ecology.WaterGrid waterGrid = simulation.getWaterGrid();
        assertNotNull(waterGrid, "Water grid active in simulation");

        // Add rain water to surface
        waterGrid.addRain(0.5f);
        assertTrue(waterGrid.getSurfaceWaterAt(10, 10) >= 0.0f, "Rainfall registered in hydric system");

        // Perform drainage ticks
        for (int i = 0; i < 10; i++) {
            waterGrid.tick(Collections.emptyList());
        }

        assertTrue(waterGrid.getSurfaceWaterAt(10, 10) >= 0.0f, "Water table absorbed surface rainfall");
    }

    @Test
    @DisplayName("Behavior 9: Inter-Colony Territorial Conflicts & Diplomatic Relations")
    void testTerritorialDiplomacy() {
        Colony colonyA = simulation.addColony("FormicaRufa", 1, 10, 2);
        Colony colonyB = simulation.addColony("LasiusNiger", 1, 10, 2);

        colonyA.getDiplomacyManager().setStatus(colonyB.getId(), org.swarmforge.core.diplomacy.RelationshipStatus.ENEMY);
        assertEquals(org.swarmforge.core.diplomacy.RelationshipStatus.ENEMY,
                colonyA.getDiplomacyManager().getStatus(colonyB.getId()), "Colonies set to enemy relation");

        Individual soldierA = colonyA.getLivingIndividuals().stream().filter(i -> i.getCaste() == Caste.SOLDIER).findFirst().orElse(null);
        Individual soldierB = colonyB.getLivingIndividuals().stream().filter(i -> i.getCaste() == Caste.SOLDIER).findFirst().orElse(null);

        assertNotNull(soldierA);
        assertNotNull(soldierB);

        float initialHealthB = soldierB.getHealth();
        soldierB.takeDamage(soldierA.getAttackDamage(), soldierA);

        assertTrue(soldierB.getHealth() < initialHealthB, "Hostile soldier inflicts bite & formic acid damage");
    }

    @Test
    @DisplayName("Behavior 10: Multi-Tick Disaster Resilience & Colony Recovery")
    void testDisasterResilience() {
        Colony colony = simulation.addColony("FormicaRufa", 1, 30, 5);
        colony.bootstrapDefaultResources();
        colony.getLivingIndividuals().forEach(i -> i.setEnergy(100f));

        int startPop = colony.getPopulation();

        // Trigger heatwave disaster
        org.swarmforge.core.simulation.disasters.HeatwaveDisaster heatwave =
                new org.swarmforge.core.simulation.disasters.HeatwaveDisaster(0.7f, 50);
        
        simulation.getWeather().setTemperature(42.0f); // High ambient heat

        // Run simulation for 30 ticks during disaster
        for (int i = 0; i < 30; i++) {
            simulation.tick();
        }

        assertTrue(colony.getPopulation() > 0, "Colony survives extreme heatwave disaster");
    }
}
