package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.AiState;
import org.swarmforge.core.domain.Individual.CarriedItem;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.domain.ResourceType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive verification test suite for Mega-Batch Wave 1 (53 behaviors).
 */
public class MegaBatch53Wave1Test {

    @Test
    @DisplayName("Wave 1 (Behaviors 1-10): Nest Architecture, Water, Osmoregulation & Defense")
    void testBehaviors1to10() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);

        // 1. Mud-Resin Funnel
        assertFalse(ant.isMudResinFunnelConstructed());
        ant.constructMudResinFunnel();
        assertTrue(ant.isMudResinFunnelConstructed());

        // 2. Fungal Comb Aeration
        assertEquals(0, ant.getFungalCombAerationPerforations());
        ant.addFungalCombPerforations(12);
        assertEquals(12, ant.getFungalCombAerationPerforations());

        // 3. Phonic Isolation
        assertFalse(ant.isPhonicIsolationChamberBuilt());
        ant.constructPhonicIsolationChamber();
        assertTrue(ant.isPhonicIsolationChamberBuilt());

        // 4. Clay Breach Repair
        assertEquals(0, ant.getClayBreachRepairCount());
        assertTrue(ant.repairClayBreach());
        assertEquals(1, ant.getClayBreachRepairCount());

        // 5. Dew Harvesting
        assertEquals(0.0f, ant.getHarvestedDewMl(), 0.001f);
        ant.harvestDewDrops(0.05f);
        assertEquals(0.05f, ant.getHarvestedDewMl(), 0.001f);

        // 6. Mandibular Water Droplet
        assertEquals(0.0f, ant.getMandibleWaterDropletMl(), 0.001f);
        ant.loadMandibleWaterDroplet(0.18f);
        assertEquals(0.18f, ant.getMandibleWaterDropletMl(), 0.001f);
        assertEquals(0.18f, ant.unloadMandibleWaterDroplet(), 0.001f);
        assertEquals(0.0f, ant.getMandibleWaterDropletMl(), 0.001f);

        // 7. Salt Crystal Osmoregulation
        Individual larva = new Individual(UUID.randomUUID(), Caste.WORKER);
        larva.takeDamage(20.0f, "Osmotic Dehydration");
        float preHealth = larva.getHealth();
        ant.forageSaltCrystals(10.0f);
        assertTrue(ant.feedSaltCrystalsToLarva(larva));
        assertEquals(preHealth + 5.0f, larva.getHealth(), 0.001f);

        // 8. Relay Seed Transport
        ant.setCarriedItem(CarriedItem.FOOD);
        ant.setCarriedResourceType(ResourceType.SEED);
        Individual receiver = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.relaySeedHandoff(receiver));
        assertEquals(CarriedItem.NONE, ant.getCarriedItem());
        assertEquals(CarriedItem.FOOD, receiver.getCarriedItem());
        assertEquals(ResourceType.SEED, receiver.getCarriedResourceType());

        // 9. Cocked-Gaster Venom Projection
        Individual enemy = new Individual(UUID.randomUUID(), Caste.SOLDIER);
        assertFalse(ant.dischargeCockedGasterVenom(enemy));
        ant.setCockedGasterDefense(true);
        assertTrue(ant.dischargeCockedGasterVenom(enemy));
        assertEquals(88.0f, enemy.getHealth(), 0.001f);

        // 10. Shimmering Wave
        assertFalse(ant.isShimmeringWaveActive());
        ant.propagateShimmeringWave();
        assertTrue(ant.isShimmeringWaveActive());
        ant.resetShimmeringWave();
        assertFalse(ant.isShimmeringWaveActive());
    }

    @Test
    @DisplayName("Wave 1 (Behaviors 11-20): Chemical Foraging, Minim Grooming, Dances & Cooling")
    void testBehaviors11to20() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);

        // 11. Prey-Size Selective Chemical Trails
        assertEquals(0.0f, ant.getLastPreyPheromoneMassDeposited(), 0.001f);
        ant.depositPreySizePheromone(2.45f);
        assertEquals(2.45f, ant.getLastPreyPheromoneMassDeposited(), 0.001f);

        // 12. Minim Leaf Cleansing
        assertFalse(ant.isRidingOnLeafForager());
        ant.setRidingOnLeafForager(true);
        assertTrue(ant.groomLeafPulpParasites(new Individual(UUID.randomUUID(), Caste.WORKER)));

        // 13. Callow Acid Coating
        Individual callow = new Individual(UUID.randomUUID(), Caste.WORKER);
        callow.setHealth(80.0f);
        assertTrue(ant.coatCallowExoskeletonAcid(callow));
        assertEquals(90.0f, callow.getHealth(), 0.001f);

        // 14. Solar Brood Basking
        assertFalse(ant.isSolarBroodBaskingActive());
        ant.baskBroodInSun(30.0f);
        assertTrue(ant.isSolarBroodBaskingActive());

        // 15. Larval Exuvia Recycling
        Individual hungryLarva = new Individual(UUID.randomUUID(), Caste.WORKER);
        hungryLarva.consumeEnergy(50.0f);
        float preE = hungryLarva.getEnergy();
        assertTrue(ant.recycleLarvalExuviaChitin(hungryLarva));
        assertEquals(preE + 12.0f, hungryLarva.getEnergy(), 0.001f);

        // 16. Tremble Dance
        assertFalse(ant.isTrembleDancing());
        ant.triggerTrembleDance();
        assertTrue(ant.isTrembleDancing());
        ant.stopTrembleDance();
        assertFalse(ant.isTrembleDancing());

        // 17. Soil-Moisture Drought Vibrato
        assertFalse(ant.isDroughtVibratoDancing());
        ant.triggerDroughtVibrato();
        assertTrue(ant.isDroughtVibratoDancing());
        ant.stopDroughtVibrato();
        assertFalse(ant.isDroughtVibratoDancing());

        // 18. Geomagnetic Navigation
        float heading = ant.getGeomagneticOrientationHeading((float) Math.PI / 4.0f);
        assertEquals((float) Math.PI / 4.0f, heading, 0.001f);

        // 19. Stercoral Cement Mortar
        assertEquals(0.0f, ant.getStercoralCementMortarMg(), 0.001f);
        ant.mixStercoralCement(10.0f, 5.0f); // 10 + 5 * 1.5 = 17.5
        assertEquals(17.5f, ant.getStercoralCementMortarMg(), 0.001f);

        // 20. Evaporative Hive Cooling
        assertFalse(ant.isEvaporativeCoolingActive());
        ant.performEvaporativeCoolingFanning(0.2f);
        assertTrue(ant.isEvaporativeCoolingActive());
        assertTrue(ant.isWingFanning());
    }

    @Test
    @DisplayName("Wave 1 (Behaviors 21-30): Mound Orientation, Allogrooming, Incubation, Swarming")
    void testBehaviors21to30() {
        UUID colonyId = UUID.randomUUID();
        Individual ant = new Individual(colonyId, Caste.WORKER);
        Individual nestmate = new Individual(colonyId, Caste.WORKER);

        // 21. South-Sloping Solar Mound
        ant.orientMoundSolarNorthSouth();
        assertEquals(180.0f, ant.getMoundSolarOrientationDegrees(), 0.001f);

        // 22. Allogrooming
        nestmate.setHealth(90.0f);
        assertTrue(ant.allogroomPartner(nestmate));
        assertEquals(94.0f, nestmate.getHealth(), 0.001f);

        // 23. Thoracic Shivering Incubation
        Individual brood = new Individual(colonyId, Caste.WORKER);
        brodAgeTest(ant, brood);

        // 24. Ritual Jousting
        Individual rival = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.engageRitualJoustingDisplay(rival));
        assertTrue(ant.isRitualJoustingActive());
        assertTrue(rival.isRitualJoustingActive());

        // 25. Robber Bee Raid
        assertFalse(ant.isRobberRaidActive());
        ant.lootEnemyHiveReserves(20.0f, 5.0f);
        assertTrue(ant.isRobberRaidActive());
        assertEquals(25.0f, ant.getPlunderedHoneyReserves(), 0.001f);

        // 26. Emergency Swarming
        assertFalse(ant.isEmergencySwarmingTriggered());
        ant.triggerEmergencySwarmingFission();
        assertTrue(ant.isEmergencySwarmingTriggered());
        assertEquals(AiState.FLEEING, ant.getState());

        // 27. Subterranean Spiral Clay Pillars
        assertEquals(0.0f, ant.getSpiralClayPillarHeightM(), 0.001f);
        ant.buildSpiralClayPillars(1.25f);
        assertEquals(1.25f, ant.getSpiralClayPillarHeightM(), 0.001f);

        // 28. Granary Seed De-Germination
        assertEquals(0, ant.getSeedsDeGermedCount());
        ant.setCarriedItem(CarriedItem.FOOD);
        assertTrue(ant.deGermStoredSeed());
        assertEquals(1, ant.getSeedsDeGermedCount());

        // 29. High-Frequency Virgin Queen Piping
        Individual queen = new Individual(colonyId, Caste.QUEEN);
        queen.triggerQueenPiping();
        assertTrue(queen.isQueenPiping());
        assertEquals(450.0f, queen.getPipingFrequencyHz(), 0.001f);

        // 30. Water Trophallaxis
        Individual thirstyAnt = new Individual(colonyId, Caste.WORKER);
        thirstyAnt.takeDamage(0, "Thirst Setup");
        assertTrue(ant.waterTrophallaxisTransfer(thirstyAnt, 0.10f));
    }

    private void brodAgeTest(Individual ant, Individual brood) {
        float ageBefore = brood.getAge();
        ant.incubateBroodThoracicHeat(brood);
        assertTrue(ant.isThoracicIncubationActive());
        assertTrue(ant.isShiveringThermogenesis());
        assertEquals(39.5f, ant.getThoraxTemperatureC(), 0.001f);
        assertEquals(ageBefore + 15.0f, brood.getAge(), 0.001f);
    }

    @Test
    @DisplayName("Wave 1 (Behaviors 31-40): Culling, Bridges, Prey Surge, Wax, Suction, Ventilation")
    void testBehaviors31to40() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);

        // 31. Aphid Sanitary Culling
        assertEquals(0, ant.getCulledInfectedAphidsCount());
        assertTrue(ant.cullInfectedAphidHerd());
        assertEquals(1, ant.getCulledInfectedAphidsCount());

        // 32. Living Bridges
        assertFalse(ant.isLivingBridgeActive());
        ant.formLivingBridgeSpan(0.45f);
        assertTrue(ant.isLivingBridgeActive());
        assertEquals(0.45f, ant.getLivingBridgeSpanMeters(), 0.001f);

        // 33. Acoustic Prey Surge
        assertEquals(0.0f, ant.getPreySurgeAcousticDb(), 0.001f);
        ant.emitPreySurgeAcousticSignal();
        assertEquals(75.0f, ant.getPreySurgeAcousticDb(), 0.001f);
        assertEquals(1.0f, ant.getOctopamine(), 0.001f);

        // 34. Subterranean Wood-Fungus Cultivation
        assertEquals(0.0f, ant.getWoodFungusCombSubstrateMg(), 0.001f);
        ant.inoculateWoodFungusComb(50.0f);
        assertEquals(50.0f, ant.getWoodFungusCombSubstrateMg(), 0.001f);

        // 35. Emergency Escape Alarm
        assertFalse(ant.isEmergencyEscapeAlarmActive());
        ant.depositEmergencyEscapeAlarm();
        assertTrue(ant.isEmergencyEscapeAlarmActive());
        assertEquals(AiState.FLEEING, ant.getState());

        // 36. Wax Lipid Queen Cell Sealing
        assertFalse(ant.isQueenChamberWaxSealed());
        ant.sealQueenChamberLipidWax();
        assertTrue(ant.isQueenChamberWaxSealed());

        // 37. Suction Escape Posture
        assertFalse(ant.isSuctionEscapeClamped());
        ant.clampSubstrateSuctionPosture();
        assertTrue(ant.isSuctionEscapeClamped());
        ant.releaseSubstrateSuctionPosture();
        assertFalse(ant.isSuctionEscapeClamped());

        // 38. Low-Frequency Queen Recognition Stridulation
        Individual queen = new Individual(UUID.randomUUID(), Caste.QUEEN);
        Individual aggressiveWorker = new Individual(UUID.randomUUID(), Caste.WORKER);
        aggressiveWorker.setOctopamine(0.9f);
        assertTrue(queen.stridulateQueenRecognitionPacification(aggressiveWorker));
        assertEquals(0.3f, aggressiveWorker.getOctopamine(), 0.001f);

        // 39. Pulsatile Abdominal Ventilation
        assertEquals(0.0f, ant.getVentilationFlowRateLpm(), 0.001f);
        ant.performPulsatileAbdominalVentilation();
        assertEquals(1.85f, ant.getVentilationFlowRateLpm(), 0.001f);

        // 40. Dynamic Depleting Trail
        assertEquals(0.40f, ant.adjustDepletingTrailConcentration(0.40f), 0.001f);
    }

    @Test
    @DisplayName("Wave 1 (Behaviors 41-53): Brood Transport, Traps, CHC, Mummies, Queen Nutrition & Bricks")
    void testBehaviors41to53() {
        UUID colonyId = UUID.randomUUID();
        Individual ant = new Individual(colonyId, Caste.WORKER);
        Individual brood = new Individual(colonyId, Caste.WORKER);

        // 41. Solar Brood Basking Carrier
        assertTrue(ant.carryBroodToSunlitMound(brood));
        assertEquals(CarriedItem.BROOD, ant.getCarriedItem());
        assertTrue(ant.isSolarBroodBaskingActive());

        // 42. Epicuticular CHC Gestalt
        Individual nestmate = new Individual(colonyId, Caste.WORKER);
        assertTrue(ant.exchangeChcGestalt(nestmate));

        // 43. Subterranean Collapsible Pitfall Traps
        assertEquals(0.0f, ant.getCollapsiblePitTrapRadiusM(), 0.001f);
        ant.excavateCollapsiblePitTrap(0.30f);
        assertEquals(0.30f, ant.getCollapsiblePitTrapRadiusM(), 0.001f);

        // 44. Guard Shift Vibrational Whisper
        ant.whisperGuardShiftSignal();
        assertTrue(ant.isGuardShiftWhispering());

        // 45. Thermoregulated Conduits
        assertEquals(0, ant.getThermoregulatedConduitsCount());
        ant.excavateThermoregulatedConduits(4);
        assertEquals(4, ant.getThermoregulatedConduitsCount());
        assertTrue(ant.isDualConduitsActive());

        // 46. Toxic Plant Resin
        assertEquals(0.0f, ant.getToxicPlantResinCarriedMg(), 0.001f);
        ant.gatherToxicPlantResin(25.0f);
        assertEquals(25.0f, ant.getToxicPlantResinCarriedMg(), 0.001f);

        // 47. Fine Dust Substrate Camouflage
        assertFalse(ant.isSoilDustCamouflageApplied());
        ant.applySoilDustCamouflage();
        assertTrue(ant.isSoilDustCamouflageApplied());

        // 48. Mandible Chain Brood Transport
        Individual larva = new Individual(colonyId, Caste.WORKER);
        assertTrue(ant.linkMandibleBroodChain(larva));
        assertEquals(1, ant.getInterlockedMandibleBroodChainCount());

        // 49. Oral Trophallactic Ovary Suppression
        Individual queen = new Individual(colonyId, Caste.QUEEN);
        Individual worker = new Individual(colonyId, Caste.WORKER);
        worker.setRoyalPheromoneInhibitionTiter(0.0f);
        assertTrue(worker.isOvariesActivated());
        assertTrue(queen.transferInhibitoryOvaryPeptides(worker));
        assertFalse(worker.isOvariesActivated());

        // 50. Clay-Saliva Propolis Mummification
        assertEquals(0, ant.getEncapsulatedLargeMummiesCount());
        assertTrue(ant.encapsulateLargeCarcassMummy(15.0f, 8.0f));
        assertEquals(1, ant.getEncapsulatedLargeMummiesCount());

        // 51. Hydrophobic Lipid Trail Coating
        assertFalse(ant.isHydrophobicGalleryFilmApplied());
        ant.applyHydrophobicGalleryCoating();
        assertTrue(ant.isHydrophobicGalleryFilmApplied());

        // 52. Pre-Flight Virgin Queen Hyper-Nourishment
        Individual virginQueen = new Individual(colonyId, Caste.QUEEN);
        assertTrue(ant.preFlightVirginQueenLipidEnrichment(virginQueen));
        assertEquals(150.0f, virginQueen.getMaxEnergy(), 0.001f);
        assertEquals(150.0f, virginQueen.getEnergy(), 0.001f);

        // 53. Honey Store Brick Plugging
        assertFalse(ant.isHoneyStoreBrickPluggingActive());
        ant.plugHoneyStoresWithBricks();
        assertTrue(ant.isHoneyStoreBrickPluggingActive());
    }
}
