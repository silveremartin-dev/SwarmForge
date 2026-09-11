package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.AiState;
import org.swarmforge.core.domain.Individual.CarriedItem;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.domain.Individual.LifeStage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive verification test suite for Mega-Batch Waves 2 & 3 (105 behaviors, completing the full 220-behavior catalog).
 */
public class MegaBatchWaves2And3Test {

    @Test
    @DisplayName("Wave 2 (Behaviors 1-20): Stretcher, Paper Pulp, Sonication, Shockwaves & Defense")
    void testWave2Part1() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);

        // 1. Hatching Announcement Vibrato
        assertFalse(ant.isHatchingAnnouncementVibratoActive());
        ant.announcePreyHatchingVibrato();
        assertTrue(ant.isHatchingAnnouncementVibratoActive());

        // 2. Antiseptic Resin Pupal Mummification
        assertEquals(0, ant.getResinNymphalMummificationCount());
        assertTrue(ant.mummifyNymphalCocoonResin(5.0f));
        assertEquals(1, ant.getResinNymphalMummificationCount());

        // 3. Sand Pitfall Trap
        assertEquals(0, ant.getSandPitfallTrapsCount());
        ant.excavateSandPitfallTrap();
        assertEquals(1, ant.getSandPitfallTrapsCount());

        // 4. Injured Stretcher Transport
        Individual casualty = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.transportInjuredOnStretcher(casualty));
        assertTrue(ant.isInjuredPheromonalStretcherTransportActive());
        assertEquals(CarriedItem.FOOD, ant.getCarriedItem());

        // 5. Abandoned Wax Vaults
        assertEquals(0, ant.getAbandonedWaxVaultsRaidedCount());
        ant.raidAbandonedWaxVault();
        assertEquals(1, ant.getAbandonedWaxVaultsRaidedCount());

        // 6. Ritual Mandibular Wrestling
        Individual rival = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.engageRitualMandibularWrestling(rival));
        assertTrue(ant.isRitualMandibularWrestlingActive());
        assertTrue(rival.isRitualMandibularWrestlingActive());

        // 7. Pulsed Air Convective Ventilation
        assertEquals(0.0f, ant.getPulsedAirConvectiveVentilationRateLpm(), 0.001f);
        ant.performPulsedAirConvectiveVentilation();
        assertEquals(2.4f, ant.getPulsedAirConvectiveVentilationRateLpm(), 0.001f);

        // 8. Cuticular Streptomyces Crypts
        Individual gardenWorker = new Individual(UUID.randomUUID(), Caste.WORKER);
        gardenWorker.takeDamage(15.0f, "Escovopsis Mold");
        float preH = gardenWorker.getHealth();
        assertTrue(ant.applyStreptomycesAntibiotics(gardenWorker));
        assertEquals(preH + 8.0f, gardenWorker.getHealth(), 0.001f);

        // 9. Twilight UV Sky Polarization
        float heading = ant.getTwilightUvPolarizationHeading(45.0f);
        assertTrue(heading > 0.0f);

        // 10. Pedestrian Swarm Budding
        assertFalse(ant.isPedestrianSwarmBuddingActive());
        ant.triggerPedestrianSwarmBudding();
        assertTrue(ant.isPedestrianSwarmBuddingActive());
        assertEquals(AiState.WANDER, ant.getState());

        // 11. Paper Pulp Carton Mastication
        assertEquals(0.0f, ant.getPaperPulpCartonMasticationMg(), 0.001f);
        ant.masticatePaperPulpCarton(10.0f, 5.0f);
        assertEquals(20.0f, ant.getPaperPulpCartonMasticationMg(), 0.001f);

        // 12. Larval Amino Acid Saliva
        Individual larva = new Individual(UUID.randomUUID(), Caste.WORKER);
        larva.setLifeStage(LifeStage.LARVA);
        assertTrue(ant.harvestLarvalAminoSaliva(larva));
        assertEquals(0.08f, ant.getLarvalAminoSalivaHarvestedMl(), 0.001f);

        // 13. Pedicel Ant-Repellent
        assertFalse(ant.isPedicelAntRepellentCoatingApplied());
        ant.applyPedicelAntRepellentCoating();
        assertTrue(ant.isPedicelAntRepellentCoatingApplied());

        // 14. Facial Visual Pattern Recognition
        UUID colonyId = ant.getColonyId();
        Individual nestmate = new Individual(colonyId, Caste.WORKER);
        assertTrue(ant.recognizeFacialVisualPattern(nestmate));
        assertTrue(ant.isFacialPatternVisualRecognitionTrained());

        // 15. Buzz Pollination Sonication
        assertEquals(0.0f, ant.getBuzzPollinationSonicationHz(), 0.001f);
        ant.performBuzzPollinationSonication();
        assertEquals(300.0f, ant.getBuzzPollinationSonicationHz(), 0.001f);

        // 16. Bumblebee Abdominal Incubation
        Individual brood = new Individual(colonyId, Caste.WORKER);
        float preAge = brood.getAge();
        ant.performBumblebeeAbdominalIncubation(brood);
        assertTrue(ant.isBumblebeeAbdominalIncubationActive());
        assertEquals(preAge + 10.0f, brood.getAge(), 0.001f);

        // 17. Aphid Horn Stabbing
        Individual predator = new Individual(UUID.randomUUID(), Caste.SOLDIER);
        assertTrue(ant.performAphidSoldierHornStabbing(predator));
        assertEquals(85.0f, predator.getHealth(), 0.001f);

        // 18. Thrips Gall Foreleg Squeezing
        Individual intruder = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.crushGallIntruderThrips(intruder));
        assertEquals(78.0f, intruder.getHealth(), 0.001f);

        // 19. Eusocial Shrimp Cavitation Shockwave
        Individual invader = new Individual(UUID.randomUUID(), Caste.SOLDIER);
        assertTrue(ant.snapClawAcousticShockwave(invader));
        assertEquals(210.0f, ant.getEusocialShrimpCavitationSnapShockwaveDb(), 0.001f);
        assertEquals(65.0f, invader.getHealth(), 0.001f);

        // 20. Passalid Frass Stridulation
        Individual grub = new Individual(UUID.randomUUID(), Caste.WORKER);
        ant.stridulatePassalidParentalFrass(grub);
        assertTrue(ant.isPassalidFrassParentalStridulationActive());
    }

    @Test
    @DisplayName("Wave 2 (Behaviors 21-53): Geomagnetic, Inoculation, Raft, Silk, Venom & Scavenging")
    void testWave2Part2() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);

        // 21. Physogastric Termite Queen Peristalsis
        Individual queen = new Individual(UUID.randomUUID(), Caste.QUEEN);
        queen.performPhysogastricQueenPeristalsis();
        assertTrue(queen.isPhysogastricQueenPeristalsisActive());

        // 22. Geomagnetic Field Mound Orientation
        ant.orientMagneticMoundNS();
        assertEquals(0.0f, ant.getMagneticMoundOrientationNSRad(), 0.001f);

        // 23. Hornet Venom Spray Group Alarm
        Individual beeTarget = new Individual(UUID.randomUUID(), Caste.WORKER);
        ant.sprayHornetGroupAlarmVenom(beeTarget);
        assertTrue(ant.isHornetGroupAlarmVenomSprayActive());
        assertEquals(82.0f, beeTarget.getHealth(), 0.001f);

        // 24. Stenogastrine Paper-Flake Jelly
        assertEquals(0.0f, ant.getStenogastrinePaperJellyMg(), 0.001f);
        ant.weaveStenogastrinePaperJelly(10.0f);
        assertEquals(18.0f, ant.getStenogastrinePaperJellyMg(), 0.001f);

        // 25. Termite Fungal Comb Inoculation
        assertEquals(0, ant.getTermiteFungalCombInoculationCount());
        ant.inoculateFungalCombTermiteFecalPellet();
        assertEquals(1, ant.getTermiteFungalCombInoculationCount());

        // 26. Wasp Cell Rim Drumming
        ant.drumWaspCellRimWarning();
        assertEquals(82.0f, ant.getWaspCellRimDrummingDb(), 0.001f);

        // 27. Stingless Bee Spiraling Brood Cells
        assertEquals(0, ant.getStinglessBeeSpiralingBroodCellsCount());
        ant.constructSpiralingBroodCells();
        assertEquals(1, ant.getStinglessBeeSpiralingBroodCellsCount());

        // 28. Living Bridge Tension Sensing
        assertEquals(0.045f, ant.measureLivingBridgeTension(), 0.001f);

        // 29. Subterranean Water Siphon Priming
        assertFalse(ant.isSubterraneanWaterSiphonPrimed());
        ant.primeSubterraneanWaterSiphon();
        assertTrue(ant.isSubterraneanWaterSiphonPrimed());

        // 30. Soil Hydraulic Drainage Channel
        assertEquals(0, ant.getSoilHydraulicDrainageChannelCount());
        ant.excavateSoilHydraulicDrainageChannel();
        assertEquals(1, ant.getSoilHydraulicDrainageChannelCount());

        // 31. Toxic Resin Burrow Repellent
        assertFalse(ant.isToxicResinBurrowRepellentApplied());
        ant.applyToxicResinBurrowRepellent();
        assertTrue(ant.isToxicResinBurrowRepellentApplied());

        // 32. Silt Camouflage Reflectance
        ant.applySiltCamouflage();
        assertEquals(0.65f, ant.getSiltCamouflageReflectanceReduction(), 0.001f);

        // 33. Mandible Linked Chain Escort
        ant.escortLarvaeInMandibleChain(5);
        assertEquals(5, ant.getMandibleLinkedChainEscortSize());

        // 34. Trophallactic Ovary Peptide Delivery
        Individual worker = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.deliverTrophallacticOvaryInhibition(worker));

        // 35. Clay Propolis Carcass Seal
        assertFalse(ant.isClayPropolisCarcassHermeticSeal());
        ant.sealCarcassHermeticClayPropolis();
        assertTrue(ant.isClayPropolisCarcassHermeticSeal());

        // 36. Hydrophobic Lipid Wall Coverage
        ant.applyHydrophobicEpicuticularLipidWall();
        assertEquals(0.85f, ant.getHydrophobicEpicuticularLipidWallCoverage(), 0.001f);

        // 37. Fermented Sap Endurance
        ant.activateFermentedSapCombatEndurance();
        assertEquals(2.2f, ant.getFermentedSapEnduranceMultiplier(), 0.001f);

        // 38. Pre-Flight Virgin Queen Gorging
        Individual gyne = new Individual(UUID.randomUUID(), Caste.QUEEN);
        ant.gorgePreFlightVirginQueenLipids(gyne);
        assertEquals(1.0f, ant.getPreFlightLipidCropFullness(), 0.001f);
        assertEquals(150.0f, gyne.getEnergy(), 0.001f);

        // 39. Defensive Honey Store Plugging
        assertFalse(ant.isHoneyStoreBrickDefensivePlugging());
        ant.defensivePlugHoneyStores();
        assertTrue(ant.isHoneyStoreBrickDefensivePlugging());

        // 40. Thermal Infrared Vision
        Individual prey = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertTrue(ant.detectThermalInfraredPrey(prey));
        assertTrue(ant.isThermalInfraredSensillaActive());

        // 41. Canopy Silk Bridge Span
        ant.weaveCanopySilkBridgeSpan(1.25f);
        assertEquals(1.25f, ant.getArborealCanopySilkBridgeSpanM(), 0.001f);

        // 42. Gyne Egg Stridulation
        ant.stridulateGyneEggBurst();
        assertEquals(160.0f, ant.getSynchronizedGyneEggStridulationHz(), 0.001f);

        // 43. Tarsal Notch Brush
        ant.cleanAntennalSensillaTarsalNotch();
        assertEquals(100.0f, ant.getTarsalNotchBrushCleansingPercent(), 0.001f);

        // 44. Salt Crystal Osmotic Retention
        ant.osmoregulateSaltCrystalRetention();
        assertEquals(1.25f, ant.getSaltCrystalOsmoticRetentionBar(), 0.001f);

        // 45. Gravity Drainage Conduit
        ant.drainGravityConduitStormwater(25.0f);
        assertEquals(25.0f, ant.getGravityDrainageConduitLitres(), 0.001f);

        // 46. Host Tree Mimicry
        ant.absorbHostTreeHydrocarbonMimicry(0.92f);
        assertEquals(0.92f, ant.getHostTreeHydrocarbonMimicryScore(), 0.001f);

        // 47. Mineral Sulfur Acaricide
        ant.dustBroodMineralSulfurAcaricide(4.5f);
        assertEquals(4.5f, ant.getMineralSulfurAcaricideDustMg(), 0.001f);

        // 48. Hatching Vibrato Announcement
        ant.announceHatchingPreyVibrato();
        assertEquals(78.0f, ant.getHatchingVibratoScoutAnnouncementDb(), 0.001f);

        // 49. Antiseptic Resin Envelope
        assertFalse(ant.isAntisepticResinEnvelopeBuilt());
        ant.cocoonInAntisepticResinEnvelope();
        assertTrue(ant.isAntisepticResinEnvelopeBuilt());

        // 50. Conical Sandy Pitfall Slope
        ant.shapeConicalSandyPitfallSlope();
        assertEquals(33.0f, ant.getFunnelSandyPitfallSlopeDeg(), 0.001f);

        // 51. Polyol Cryoprotectant Accumulation
        ant.accumulatePolyolCryoprotectants(0.75f);
        assertEquals(0.75f, ant.getCryoprotectantPolyolSynthesisRate(), 0.001f);

        // 52. Battlefield Stretcher Squad
        ant.formBattlefieldStretcherSquad(new Individual(UUID.randomUUID(), Caste.WORKER));
        assertTrue(ant.isBattlefieldStretcherSquadActive());

        // 53. Feral Wax Scavenging
        ant.scavengeFeralWaxVault(12.0f);
        assertEquals(12.0f, ant.getFeralWaxVaultScavengingYieldMg(), 0.001f);
    }

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
