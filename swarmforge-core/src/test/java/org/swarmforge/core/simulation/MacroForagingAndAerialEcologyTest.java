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
import org.swarmforge.core.species.ApisMellifera;
import org.swarmforge.core.species.VespulaGermanica;
import org.swarmforge.core.spatial.OptimalColonyPlacementEngine;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite validating Macro-Foraging Landscape scaling, Honey maturation,
 * and Aerial Tree/Canopy Placement calculations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MacroForagingAndAerialEcologyTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private Colony beeColony;
    private Individual foragerBee;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(120, 120, 60);
        simulation = new Simulation(terrarium);

        ApisMellifera beeSpecies = new ApisMellifera();
        beeColony = new Colony(beeSpecies, 40, 40, 1.2f);
        simulation.addColony(beeColony);

        foragerBee = new Individual(beeColony.getId(), Individual.Caste.WORKER, 40, 40, 1.2f);
        foragerBee.setSpecies(beeSpecies);
        beeColony.addIndividual(foragerBee);
    }

    @Test
    @DisplayName("Macro Foraging: Dispatch to 1.2km Rapeseed Field, Energy Cost & Honey Maturation")
    void testMacroForagingAndHoneyMaturation() {
        MacroForagingSystem macroSystem = simulation.getMacroForagingSystem();
        assertNotNull(macroSystem);
        assertEquals(5, macroSystem.getLandscapePatches().size());

        MacroForagingSystem.LandscapePatch rapeseedPatch = macroSystem.getLandscapePatches().get(0);
        assertEquals("patch_rapeseed", rapeseedPatch.id());
        assertEquals(1.2f, rapeseedPatch.distanceKm());

        float initialEnergy = foragerBee.getEnergy();
        boolean dispatched = macroSystem.dispatchForager(foragerBee, beeColony, rapeseedPatch);
        assertTrue(dispatched);
        assertEquals(1, macroSystem.getActiveForagersCount());
        assertTrue(foragerBee.getEnergy() < initialEnergy, "Energy consumed for 2.4km round-trip flight");

        // Simulate flight and flower foraging time until trip completion (~500-550s)
        Map<UUID, Individual> indMap = Map.of(foragerBee.getId(), foragerBee);
        Map<UUID, Colony> colMap = Map.of(beeColony.getId(), beeColony);

        for (int i = 0; i < 60; i++) {
            macroSystem.update(10.0f, indMap, colMap);
        }

        // Forager should have completed trip, returned to hive with harvest, and initiated honey curing
        assertEquals(0, macroSystem.getActiveForagersCount());
        assertTrue(foragerBee.isCarryingFood());
        assertEquals(ResourceType.NECTAR, foragerBee.getCarriedResourceType());

        // Verify nectar enzymatic dehydration into capped honey
        float cappedHoney = macroSystem.getCappedHoneyGrams(beeColony.getId());
        assertTrue(cappedHoney > 0.0f, "Ripe capped honey produced after worker ventilation");
    }

    @Test
    @DisplayName("Optimal Spatial Placement: Aerial Tree Canopy Snap for Wasps & Hive Stand Elevation for Bees")
    void testOptimalAerialAndArborealPlacement() {
        // 1. Wasp colony placement should snap to canopy branch elevation (Z > 5m)
        OptimalColonyPlacementEngine.PlacementResult waspResult =
                OptimalColonyPlacementEngine.calculateOptimalPosition(terrarium, "Vespula germanica", 0, 2, "Optimal");
        assertTrue(waspResult.z() >= 6.0f, "Wasp nest must be elevated into arboreal tree canopy");

        // 2. Honeybee colony placement should be on a hive stand (Z ~ 1.2m)
        OptimalColonyPlacementEngine.PlacementResult beeResult =
                OptimalColonyPlacementEngine.calculateOptimalPosition(terrarium, "Apis mellifera", 1, 2, "Optimal");
        assertEquals(1.2f, beeResult.z(), 0.1f, "Beehive must be placed at hive stand height");
    }

    @Test
    @DisplayName("Custom Biome Fallback & Wind Drift Heading Compensation")
    void testCustomBiomeFallbackAndWindDrift() {
        MacroForagingSystem macroSystem = new MacroForagingSystem();
        // Fallback for an unknown/custom modded biome
        macroSystem.configureForCustomBiome("Volcanic Caldera Flora", 24.0f, 1400.0f, 0.9f);
        assertFalse(macroSystem.getLandscapePatches().isEmpty(), "Custom biome must generate fallback flora patches");
        assertTrue(macroSystem.getLandscapePatches().get(0).name().contains("Volcanic Caldera"));

        // Wind drift test: heading 90° (East), crosswind from North (0°) at 3.0 m/s with 6.5 m/s airspeed
        float driftCompensatedHeading = MacroForagingSystem.calculateFlightHeadingWithWindDrift(90.0f, 6.5f, 3.0f, 0.0f);
        assertTrue(driftCompensatedHeading < 90.0f, "Heading must crab northwards into the wind to counteract drift");

        // Ground speed calculation
        float groundSpeed = MacroForagingSystem.calculateGroundSpeed(6.5f, 3.0f, 90.0f, 0.0f);
        assertTrue(groundSpeed > 0.0f);
    }

    @Test
    @DisplayName("Existing Tree Snapping for Arboreal Nests")
    void testExistingTreeSnapping() {
        org.swarmforge.core.world.VegetationSystem veg = new org.swarmforge.core.world.VegetationSystem(120, 120);
        org.swarmforge.core.world.VegetationSystem.Plant matureOak = new org.swarmforge.core.world.VegetationSystem.Plant(
                org.swarmforge.core.world.VegetationSystem.PlantType.TREE, 55, 65, 0);
        matureOak.growth = 1.0f;
        veg.getPlants().add(matureOak);

        OptimalColonyPlacementEngine.PlacementResult waspSnap =
                OptimalColonyPlacementEngine.calculateOptimalPosition(terrarium, veg, "Vespula germanica", 0, 1, "Optimal");
        
        assertEquals(55f, waspSnap.x(), "Wasp nest must snap to the X coordinate of the existing mature tree");
        assertEquals(65f, waspSnap.y(), "Wasp nest must snap to the Y coordinate of the existing mature tree");
        assertTrue(waspSnap.z() >= 6.0f, "Wasp nest must be elevated in canopy");
    }

    @Test
    @DisplayName("Combat Realism & Worker Bee Sting Non-Systematic Autotomy")
    void testCombatAndBeeStingAutotomyRealism() {
        ApisMellifera beeSpecies = new ApisMellifera();
        Colony colony = new Colony(beeSpecies, 10, 10, 0);
        Individual beeWorker = new Individual(colony.getId(), Individual.Caste.WORKER, 10, 10, 0);
        beeWorker.setSpecies(beeSpecies);

        // Verify biological stats scaling
        assertEquals(40.0f, beeWorker.getMaxHealth(), "Worker bee health dynamically scaled to 40 PV");
        assertEquals(5.0f, beeWorker.getAttackDamage(), "Base mandible damage scaled");

        // Stinging an enemy insect: Worker should survive in the majority of attempts
        Individual waspEnemy = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 11, 10, 0);
        waspEnemy.setSpecies(new VespulaGermanica());
        float initialWaspHealth = waspEnemy.getHealth();

        boolean stung = beeWorker.performBarbedBeeSting(waspEnemy);
        assertTrue(stung);
        assertTrue(waspEnemy.getHealth() < initialWaspHealth, "Apitoxin damage inflicted on insect target");

        // Mammalian sting test: 100% autotomy
        Individual beeDefender = new Individual(colony.getId(), Individual.Caste.WORKER, 10, 10, 0);
        beeDefender.setSpecies(beeSpecies);
        beeDefender.performMammalianDefensiveSting(50.0f);
        assertFalse(beeDefender.isAlive(), "Mammalian skin elasticity causes 100% fatal autotomy");
        assertTrue(beeDefender.getCauseOfDeath().contains("mammifère"));
    }
}
