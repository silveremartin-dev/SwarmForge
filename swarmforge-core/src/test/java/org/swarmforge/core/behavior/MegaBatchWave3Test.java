package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.AiState;
import org.swarmforge.core.domain.Individual.Caste;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test suite for Mega-Batch Wave 3 (Behaviors 54-105).
 */
public class MegaBatchWave3Test {

    @Test
    @DisplayName("Wave 3 (Behaviors 54-80): Atta Waste, Repletes, Rafts, Gaster Aerosol & BitSets")
    void testWave3Part1() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);

        // 54. Atta Garden Waste Excavation
        assertEquals(0.0f, ant.getAttaGardenWasteChamberDigVolumeM3(), 0.001f);
        ant.excavateGardenWasteChamber(0.15f);
        assertEquals(0.15f, ant.getAttaGardenWasteChamberDigVolumeM3(), 0.001f);

        // 55. Termite Royal Pair Mutual Grooming
        Individual royalPartner = new Individual(UUID.randomUUID(), Caste.QUEEN);
        ant.exchangeRoyalPairMutualGrooming(royalPartner);
        assertEquals(30.0f, ant.getTermiteRoyalPairMutualGroomingSec(), 0.001f);

        // 56. Universal Emergency Evacuation
        assertFalse(ant.isUniversalEmergencyEvacuationActive());
        ant.triggerUniversalEmergencyEvacuationAll();
        assertTrue(ant.isUniversalEmergencyEvacuationActive());
        assertEquals(AiState.FLEEING, ant.getState());

        // 57. Myrmecocystus Replete Gaster Distension
        assertEquals(0.0f, ant.getMyrmecocystusRepleteGasterVolumeUl(), 0.001f);
        ant.distendRepleteGasterVolume(250.0f);
        assertEquals(250.0f, ant.getMyrmecocystusRepleteGasterVolumeUl(), 0.001f);

        // 58. Floating Ant Raft Claw Interlock
        ant.interlockClawsForFloatingRaft(40);
        assertEquals(40, ant.getFloatingAntRaftClawInterlockCount());

        // 59. Mud-Resin Trumpet Funnel
        ant.buildMudResinTrumpetFunnel(6.5f);
        assertEquals(6.5f, ant.getMudResinTrumpetFunnelHeightCm(), 0.001f);

        // 60. Bombus Hibernaculum Excavation
        ant.excavateBombusHibernaculum(15.0f);
        assertEquals(15.0f, ant.getBombusOverwinteringHibernaculumDepthCm(), 0.001f);

        // 61. Acromyrmex Leaf Micro-Mastication Enzymes
        ant.inoculateLeafPulpWithDigestiveEnzymes(8.0f);
        assertEquals(8.0f, ant.getAcromyrmexLeafMicroMasticationEnzymeMg(), 0.001f);

        // 62. Dinoponera Gamergate Tournament
        Individual rival = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.applyDominanceStingSmearTournament(rival));
        assertTrue(ant.isDinoponeraGamergateDominanceStingSmear());
        assertTrue(ant.isGamergate());
        assertEquals(Caste.QUEEN, ant.getCaste());

        // 63. Dracula Hemolymph Feeding Dose
        Individual larva = new Individual(UUID.randomUUID(), Caste.WORKER);
        ant.consumeDraculaLarvalHemolymphDose(larva);
        assertEquals(1.0f, ant.getDraculaLarvalHemolymphSustenanceDose(), 0.001f);

        // 64. Oecophylla Tarsal Friction Bridge Pull
        ant.exertTarsalFrictionBridgePull(0.35f);
        assertEquals(0.35f, ant.getOecophyllaTarsalFrictionBridgeTensileKg(), 0.001f);

        // 65. Pachycondyla Surface Tension Water Drop
        ant.trapMandibleSurfaceTensionWaterDrop(18.0f);
        assertEquals(18.0f, ant.getPachycondylaMandibleSurfaceTensionDropUl(), 0.001f);

        // 66. Desert Ant Thermal Tripod Gait Speed
        float speed = ant.engageDesertAntThermalTripodGait();
        assertTrue(speed > 0.0f);
        assertTrue(ant.isStiltWalking());

        // 67. Giant Honeybee Shimmering Wave Sync
        ant.syncGiantHoneybeeShimmeringWave();
        assertEquals((float) Math.PI, ant.getGiantHoneybeeShimmeringWavePhaseRad(), 0.001f);

        // 68. Paper Wasp Water Douse
        ant.dousePaperWaspCombWithWater(0.5f);
        assertEquals(0.5f, ant.getPaperWaspEvaporativeWaterDouseMl(), 0.001f);

        // 69. Termite Clay Wall Fungal Pores
        ant.perforateClayWallFungalPores(24);
        assertEquals(24, ant.getTermiteFungalCombClayMicroPoresCount());

        // 70. Passalid Chitin Nitrogen Recycling
        ant.feedLarvaeNitrogenousExuvia(larva, 6.0f);
        assertEquals(6.0f, ant.getPassalidLarvalChitinNitrogenRecyclingMg(), 0.001f);

        // 71. Atta Minim Phorid Fly Cleansing
        ant.groomMinimPhoridFlyEggs(ant);
        assertEquals(1, ant.getAttaMinimPhoridFlyEggGroomingCount());

        // 72. Social Spider Web Debris
        ant.attachPlantDebrisWebDisguise(85.0f);
        assertEquals(85.0f, ant.getSocialSpiderWebDebrisDisguisePercent(), 0.001f);

        // 73. Acrobat Ant Gaster Aerosol
        Individual attacker = new Individual(UUID.randomUUID(), Caste.SOLDIER);
        ant.dischargeAcrobatVenomAerosol(attacker);
        assertTrue(ant.isAcrobatAntGasterVenomAerosolActive());
        assertEquals(84.0f, attacker.getHealth(), 0.001f);

        // 74. Lasius Aphid Stroking
        ant.strokeAphidAntennalHoneydew(5.0f);
        assertEquals(4.5f, ant.getLasiusAphidAntennalStrokingRateHz(), 0.001f);

        // 75. Formica Solar Heat Collector Cluster
        ant.baskInSolarMoundCollectorCluster(15);
        assertEquals(15, ant.getFormicaSolarHeatCollectorClusterCount());
        assertEquals(36.0f, ant.getThoraxTemperatureC(), 0.001f);

        // 76. Global Ethological BitSet
        assertEquals(220, ant.serializeGlobalEthologicalStateBitSet());

        // 77. Nectar Receiver Tremble Dance Response
        assertFalse(ant.isNectarReceiverTrembleDanceResponseActive());
        ant.respondToTrembleDanceForNectar();
        assertTrue(ant.isNectarReceiverTrembleDanceResponseActive());

        // 78. Subterranean Air Current Flapping
        ant.flapAbdomenSubterraneanAirCurrent();
        assertEquals(18.0f, ant.getSubterraneanAirCurrentFlappingHz(), 0.001f);

        // 79. Larval Food Requirement Age
        assertTrue(ant.calculateLarvalFoodRequirementAge() >= 1.0f);

        // 80. Social Crop Sucrose Brix
        ant.setSocialCropSucroseBrix(42.5f);
        assertEquals(42.5f, ant.getSocialCropNectarSucroseBrixPercent(), 0.001f);
    }

    @Test
    @DisplayName("Wave 3 (Behaviors 81-105): Sensory Ephemeris, CHC Purity, Safety & 100% Convergence")
    void testWave3Part2() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.SOLDIER);

        // 81. Visual Landmark Memory
        assertEquals(0, ant.getForagerVisualLandmarkMemoryEntries());
        ant.recordVisualLandmarkMemory(10.0f, 20.0f, 1.57f);
        assertEquals(1, ant.getForagerVisualLandmarkMemoryEntries());

        // 82. Antennal Hydrocarbon Resolution
        assertEquals(0.995f, ant.getAntennalHydrocarbonResolution(), 0.001f);

        // 83. Queen Cuticular Hydrocarbon Purity
        Individual queen = new Individual(UUID.randomUUID(), Caste.QUEEN);
        assertTrue(ant.inspectQueenChcPurity(queen));
        assertFalse(ant.inspectQueenChcPurity(ant));

        // 84. Subterranean Carbon Dioxide Tolerance
        assertEquals(25000.0f, ant.getSubterraneanCarbonDioxideTolerancePpm(), 0.001f);
        ant.adaptSubterraneanGasTolerance(30000.0f);
        assertEquals(30000.0f, ant.getSubterraneanCarbonDioxideTolerancePpm(), 0.001f);

        // 85. Larval Metamorphosis JH
        ant.regulateLarvalMetamorphosisJH(0.45f);
        assertEquals(0.45f, ant.getJuvenileHormone(), 0.001f);

        // 86. Foraging Trail Evaporation Coefficient
        assertEquals(0.0025f, ant.getForagingTrailEvaporationCoefficient(), 0.0001f);

        // 87. Metapleural Garden Sterilization
        ant.applyMetapleuralGardenSterilization(3.5f);
        assertEquals(3.5f, ant.getFungalGardenSterilizingMetapleuralSecretionMg(), 0.001f);

        // 88. Colony Fat Reserve Index
        assertTrue(ant.calculateColonyNutritionalFatReserve() > 0.0f);

        // 89. Nurse Ant Fat Body Vitellogenin
        ant.elevateFatBodyVitellogenin(0.95f);
        assertEquals(0.95f, ant.getNurseAntFatBodyVitellogeninTiter(), 0.001f);

        // 90. Major Soldier Bite Crushing Force
        assertEquals(45.0f, ant.calculateMajorBiteCrushingForce(), 0.001f);

        // 91. Tunnel Structural Load Safety
        assertEquals(1.85f, ant.evaluateSubterraneanTunnelSafety(), 0.001f);

        // 92. Predator Toxicity Neutralization
        assertTrue(ant.neutralizeIngestedPreyToxins(5.0f));

        // 93. Humidity Sensor Antennal Sensitivity
        assertTrue(ant.readNestHumidityGradient() >= 0.0f);

        // 94. Nuptial Flight Spermatheca Capacity
        ant.storeSpermathecaSpermReserves(6500000.0f);
        assertEquals(6500000.0f, ant.getQueenNuptialFlightSpermathecaCapacity(), 0.001f);

        // 95. Solar Azimuth Ephemeris
        float angle = ant.calculateSolarAzimuthEphemeris(12.0f);
        assertEquals((float) Math.PI, angle, 0.001f);

        // 96. Callow Sclerotization Score
        ant.progressCallowSclerotization(10.0f);
        assertEquals(1.0f, ant.getCallowCuticularTanningSclerotizationScore(), 0.001f);

        // 97. Brood Thermal Optimum Depth
        assertTrue(ant.selectBroodThermalOptimumDepth(30.0f) > 0.0f);

        // 98. Tandem Leader Pace Adjustment
        ant.adjustTandemLeaderPace(true);
        assertEquals(0, ant.getLastTandemContactTick());

        // 99. Warfare Casualty Attrition
        assertEquals(0, ant.getInterColonyWarfareCasualtyAttritionIndex());
        ant.recordInterColonyWarfareCasualty();
        assertEquals(1, ant.getInterColonyWarfareCasualtyAttritionIndex());

        // 100. Mandible Zinc Hardening
        ant.hardenMandibleChitinZinc(4.1f);
        assertEquals(4.1f, ant.getLeafCutterMandibleChitinZincHardeningGpa(), 0.001f);

        // 101. Fungal Moisture Sponge Translocation
        ant.translocateFungalMoistureSponge(14.0f);
        assertEquals(14.0f, ant.getSubterraneanFungalMoistureSpongeTranslocation(), 0.001f);

        // 102. Division of Labor Threshold
        assertTrue(ant.updateDivisionOfLaborThreshold(1, 10.0f) > 0.0f);

        // 103. Supercolony Unicolonial CHC Tolerance
        assertTrue(ant.evaluateUnicolonialChcAcceptance(ant));

        // 104. Aphid Honeydew Quality
        assertEquals(0.95f, ant.evaluateAphidHoneydewQuality(), 0.001f);

        // 105. SwarmForge Global Ethology Engine 100% Convergence
        assertTrue(ant.validateGlobalEthologyEngineConvergence());
    }
}
