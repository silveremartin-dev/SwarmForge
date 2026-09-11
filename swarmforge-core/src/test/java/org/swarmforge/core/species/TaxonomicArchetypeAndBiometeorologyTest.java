/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.simulation.SimulationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite verifying taxonomic archetype propagation, species-specific ethological capabilities,
 * biometeorological flight thresholds, and specialized caste categories.
 */
public class TaxonomicArchetypeAndBiometeorologyTest {

    @Test
    @DisplayName("Verify Taxonomic Archetypes (Order -> Genus -> Species Overrides)")
    public void testTaxonomicArchetypes() {
        // Apis mellifera (Apidae / Bee)
        ApisMellifera apis = new ApisMellifera();
        assertTrue(apis.isWorkersCanFly());
        assertEquals("BEE", apis.getInsectType());
        assertTrue(apis.canPerformWaggleDance());
        assertTrue(apis.canEncodeWaggleDanceSunCompass());
        assertTrue(apis.canCollectPropolis());
        assertTrue(apis.canSealNestGapsWithPropolis());
        assertTrue(apis.canPerformQueenPiping());
        assertTrue(apis.canPerformSocialThermoregulation());
        assertTrue(apis.canPackCorbiculaPollenBaskets());

        // Bombus terrestris (Apidae / Bumblebee)
        BombusTerrestris bombus = new BombusTerrestris();
        assertTrue(bombus.isWorkersCanFly());
        assertTrue(bombus.canPerformBuzzPollination());
        assertTrue(bombus.canForageSubZeroBumblebee());
        assertTrue(bombus.canIncubateBroodAbdominalHeat());
        assertTrue(bombus.canConstructNectarWaxPots());
        assertEquals(4.0f, bombus.getMinTempCelsius(), 0.1f);

        // Vespula germanica (Vespidae / Wasp)
        VespulaGermanica vespula = new VespulaGermanica();
        assertTrue(vespula.isWorkersCanFly());
        assertEquals("WASP", vespula.getInsectType());
        assertTrue(vespula.canMasticatePaperPulpCarton());
        assertTrue(vespula.canHarvestLarvalSalivaDroplets());
        assertTrue(vespula.canApplyPedicelAntRepellent());

        // Vespa crabro (Vespidae / Hornet)
        VespaCrabro vespa = new VespaCrabro();
        assertTrue(vespa.canEmitHornetGroupAlarmPheromone());
        assertTrue(vespa.canMasticatePaperPulpCarton());

        // Formica rufa (Formicinae / Wood Ant)
        FormicaRufa rufa = new FormicaRufa();
        assertEquals("FORMIC_ACID", rufa.getVenomType());
        assertTrue(rufa.canSprayFormicResinDisinfectant());
        assertTrue(rufa.canFireFormicAcidArtilleryJet());
        assertTrue(rufa.canFarmAphids());
        assertTrue(rufa.canClusterSolarHeatCollector());
        assertTrue(rufa.hasSolarOrientedMound());
        assertTrue(rufa.isPolycalic());

        // Atta cephalotes (Myrmicinae / Leafcutter Ant)
        AttaCephalotes atta = new AttaCephalotes();
        assertTrue(atta.canFarmFungus());
        assertTrue(atta.canWeedFungusGarden());
        assertTrue(atta.canShearLeafCrescentMandible());
        assertTrue(atta.canInoculateLeafPulpEnzymes());
        assertTrue(atta.canGroomLeafPulpParasitesMinim());
        assertTrue(atta.canExcavateGardenWasteChambers());
        assertTrue(atta.canStridulateRescueCall());

        // Solenopsis invicta (Myrmicinae / Fire Ant)
        SolenopsisInvicta solenopsis = new SolenopsisInvicta();
        assertTrue(solenopsis.canFormFloatingAntRaft());
        assertTrue(solenopsis.canFormLivingRaft());
        assertTrue(solenopsis.hasTerritorialRepellentPheromone());

        // Linepithema humile (Dolichoderinae / Argentine Ant)
        LinepithemaHumile linepithema = new LinepithemaHumile();
        assertTrue(linepithema.isUnicolonial());
        assertTrue(linepithema.formsMegaColonies());

        // Odontomachus bauri (Ponerinae / Trap-Jaw Ant)
        OdontomachusBauri odonto = new OdontomachusBauri();
        assertTrue(odonto.hasTrapJawMechanism());
        assertTrue(odonto.canSnapTrapMandiblesCatapult());
        assertTrue(odonto.canPerformGamergateDominanceTournament());

        // Reticulitermes flavipes (Isoptera / Termite)
        ReticulitermesFlavipes termite = new ReticulitermesFlavipes();
        assertTrue(termite.isHasKing());
        assertTrue(termite.hasTermiteGutSymbiosis());
        assertTrue(termite.hasProctodealTrophallaxis());
        assertTrue(termite.canTrophallaxisProtozoa());
        assertTrue(termite.canDrumSubstrate());
        assertTrue(termite.canBlockRoyalChamberSentry());
    }

    @Test
    @DisplayName("Verify Biometeorological Flight Thresholds & Aerodynamics (Re, Viscosity)")
    public void testBiometeorology() {
        BombusTerrestris bombus = new BombusTerrestris();
        ApisMellifera apis = new ApisMellifera();
        VespulaGermanica vespula = new VespulaGermanica();

        // Stub context simulating 6°C dry weather
        SimulationContext coldContext = new SimulationContext() {
            @Override public float getFoodPheromone(float x, float y, float z) { return 0; }
            @Override public float getHomePheromone(float x, float y, float z) { return 0; }
            @Override public float getAlarmPheromone(float x, float y, float z) { return 0; }
            @Override public float getFoodPheromoneGradientX(float x, float y, float z) { return 0; }
            @Override public float getFoodPheromoneGradientY(float x, float y, float z) { return 0; }
            @Override public boolean hasEnemyNearby(org.swarmforge.core.behavior.AgentView agent) { return false; }
            @Override public org.swarmforge.core.domain.Individual getNearestEnemy(org.swarmforge.core.behavior.AgentView agent) { return null; }
            @Override public long getCurrentTick() { return 100; }
            @Override public float getTemperature() { return 6.0f; } // 6°C
            @Override public boolean isRaining() { return false; }
            @Override public float getLightLevel() { return 1.0f; }
            @Override public float getWaterLevel(float x, float y, float z) { return 0; }
            @Override public float getRelativeHumidity(float x, float y, float z) { return 50.0f; }
            @Override public boolean hasFoodNearby(org.swarmforge.core.behavior.AgentView agent) { return false; }
            @Override public boolean hasFoodNearby(org.swarmforge.core.behavior.AgentView agent, java.util.Set<org.swarmforge.core.domain.ResourceType> types) { return false; }
            @Override public float[] getNearestFoodPosition(org.swarmforge.core.behavior.AgentView agent) { return null; }
            @Override public float[] getNearestFoodPosition(org.swarmforge.core.behavior.AgentView agent, java.util.Set<org.swarmforge.core.domain.ResourceType> types) { return null; }
            @Override public org.swarmforge.core.domain.FoodSource getNearestFood(org.swarmforge.core.behavior.AgentView agent, java.util.Set<org.swarmforge.core.domain.ResourceType> types) { return null; }
            @Override public float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ) { return new float[]{0, 0, 0}; }
        };

        // At 6°C: Bombus can fly (sub-zero tolerance down to 4°C), but Apis (<10°C) and Vespula (<12°C) are grounded
        assertFalse(coldContext.isAdverseWeatherForFlight(bombus), "Bombus should be capable of flight at 6°C due to thoracic endothermy!");
        assertTrue(coldContext.isAdverseWeatherForFlight(apis), "Apis mellifera must shelter when temperature is below 10°C!");
        assertTrue(coldContext.isAdverseWeatherForFlight(vespula), "Vespula must shelter when temperature is below 12°C!");

        // Dynamic air viscosity test at 20°C
        float mu = coldContext.getAirDynamicViscosity(20.0f);
        assertTrue(mu > 1.7e-5f && mu < 1.9e-5f, "Air dynamic viscosity at 20°C should be ~1.81e-5 Pa.s");

        // Reynolds number test (e.g. Bombus flight at 5 m/s, 15 mm length)
        float reynolds = coldContext.getReynoldsNumber(5.0f, 15.0f, 20.0f);
        assertTrue(reynolds > 2000.0f, "Reynolds number for bumblebee flight should be in inertial regime (>2000)");
    }

    @Test
    @DisplayName("Verify Caste Categories, Task Weights, and Queen Egg Laying Action")
    public void testCasteAndEggLaying() {
        CasteTemplate honeypot = new CasteTemplate("Plérogate Pot de Miel", 100f, 0f);
        honeypot.setCategory(CasteTemplate.CasteCategory.METABOLIC_REPLETE);
        honeypot.setTaskStorageWeight(0.95f);
        assertEquals(CasteTemplate.CasteCategory.METABOLIC_REPLETE, honeypot.getCategory());
        assertEquals(0.95f, honeypot.getTaskStorageWeight(), 0.01f);

        CasteTemplate doorkeeper = new CasteTemplate("Portière Phragmotique", 250f, 10f);
        doorkeeper.setCategory(CasteTemplate.CasteCategory.SOLDIER_PHRAGMOTIC);
        doorkeeper.setTaskGateKeepingWeight(0.90f);
        assertEquals(CasteTemplate.CasteCategory.SOLDIER_PHRAGMOTIC, doorkeeper.getCategory());
        assertEquals(0.90f, doorkeeper.getTaskGateKeepingWeight(), 0.01f);

        // Verify Queen Egg Laying Action
        ReasoningArchitecture.Action layEggAction = ReasoningArchitecture.Action.layEgg();
        assertNotNull(layEggAction);
        assertEquals(ReasoningArchitecture.Action.ActionType.LAY_EGG, layEggAction.type());
    }
}
