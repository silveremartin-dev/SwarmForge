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
 * Verification test suite for Mega-Batch Wave 2 (Behaviors 1-53).
 */
public class MegaBatchWave2Test {

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
}
