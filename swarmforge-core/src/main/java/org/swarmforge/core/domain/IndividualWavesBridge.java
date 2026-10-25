/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

/**
 * Interface providing default delegates for Ethology Waves 1, 2, and 3.
 */
public interface IndividualWavesBridge {

    IndividualEthologyWave1Trait getWave1Trait();
    IndividualEthologyWave2Trait getWave2Trait();
    IndividualEthologyWave3Trait getWave3Trait();

    default Individual asIndividual() {
        return (Individual) this;
    }

    // Wave 1
    default boolean isMudResinFunnelConstructed() { return getWave1Trait().mudResinFunnelConstructed; }
    default void constructMudResinFunnel() { getWave1Trait().mudResinFunnelConstructed = true; }
    default int getFungalCombAerationPerforations() { return getWave1Trait().fungalCombAerationPerforations; }
    default void addFungalCombPerforations(int count) { getWave1Trait().fungalCombAerationPerforations += Math.max(0, count); }
    default boolean isPhonicIsolationChamberBuilt() { return getWave1Trait().phonicIsolationChamberBuilt; }
    default void constructPhonicIsolationChamber() { getWave1Trait().phonicIsolationChamberBuilt = true; }
    default int getClayBreachRepairCount() { return getWave1Trait().clayBreachRepairCount; }
    default boolean repairClayBreach() { getWave1Trait().clayBreachRepairCount++; return true; }
    default float getHarvestedDewMl() { return getWave1Trait().harvestedDewMl; }
    default void harvestDewDrops(float ml) { getWave1Trait().harvestDewDrops(asIndividual(), ml); }
    default float getMandibleWaterDropletMl() { return getWave1Trait().mandibleWaterDropletMl; }
    default void loadMandibleWaterDroplet(float ml) { getWave1Trait().mandibleWaterDropletMl = Math.min(0.25f, ml); }
    default float unloadMandibleWaterDroplet() { return getWave1Trait().unloadMandibleWaterDroplet(); }
    default float getSaltCrystalsMg() { return getWave1Trait().saltCrystalsMg; }
    default void forageSaltCrystals(float mg) { getWave1Trait().saltCrystalsMg = Math.min(50.0f, getWave1Trait().saltCrystalsMg + mg); }
    default boolean feedSaltCrystalsToLarva(Individual larva) { return getWave1Trait().feedSaltCrystalsToLarva(larva); }
    default boolean relaySeedHandoff(Individual receiver) { return getWave1Trait().relaySeedHandoff(asIndividual(), receiver); }
    default boolean isCockedGasterDefense() { return getWave1Trait().cockedGasterDefense; }
    default void setCockedGasterDefense(boolean active) { getWave1Trait().cockedGasterDefense = active; }
    default boolean dischargeCockedGasterVenom(Individual target) { return getWave1Trait().dischargeCockedGasterVenom(target); }
    default boolean isShimmeringWaveActive() { return getWave1Trait().shimmeringWaveActive; }
    default void propagateShimmeringWave() { getWave1Trait().shimmeringWaveActive = true; }
    default void resetShimmeringWave() { getWave1Trait().shimmeringWaveActive = false; }
    default float getLastPreyPheromoneMassDeposited() { return getWave1Trait().lastPreyPheromoneMassDeposited; }
    default void depositPreySizePheromone(float preyMassGrams) { getWave1Trait().lastPreyPheromoneMassDeposited = preyMassGrams; }
    default boolean isRidingOnLeafForager() { return getWave1Trait().ridingOnLeafForager; }
    default void setRidingOnLeafForager(boolean riding) { getWave1Trait().ridingOnLeafForager = riding; }
    default boolean groomLeafPulpParasites(Individual forager) { return getWave1Trait().ridingOnLeafForager && forager != null; }
    default boolean coatCallowExoskeletonAcid(Individual callow) { return getWave1Trait().coatCallowExoskeletonAcid(asIndividual(), callow); }
    default boolean isSolarBroodBaskingActive() { return getWave1Trait().solarBroodBaskingActive; }
    default void baskBroodInSun(float durationSec) { getWave1Trait().baskBroodInSun(asIndividual(), durationSec); }
    default boolean recycleLarvalExuviaChitin(Individual larva) { return getWave1Trait().recycleLarvalExuviaChitin(larva); }
    default boolean isDroughtVibratoDancing() { return getWave1Trait().droughtVibratoDancing; }
    default void triggerDroughtVibrato() { getWave1Trait().droughtVibratoDancing = true; }
    default void stopDroughtVibrato() { getWave1Trait().droughtVibratoDancing = false; }
    default float getGeomagneticOrientationHeading(float ambientDeclinationRad) { return (float) ((ambientDeclinationRad + 2.0 * Math.PI) % (2.0 * Math.PI)); }
    default float getStercoralCementMortarMg() { return getWave1Trait().stercoralCementMortarMg; }
    default void mixStercoralCement(float clayMg, float salivaMg) { getWave1Trait().stercoralCementMortarMg += (clayMg + salivaMg * 1.5f); }
    default boolean isEvaporativeCoolingActive() { return getWave1Trait().evaporativeCoolingActive; }
    default void performEvaporativeCoolingFanning(float waterDropMl) { getWave1Trait().performEvaporativeCoolingFanning(asIndividual(), waterDropMl); }
    default float getMoundSolarOrientationDegrees() { return getWave1Trait().moundSolarOrientationDegrees; }
    default void orientMoundSolarNorthSouth() { getWave1Trait().moundSolarOrientationDegrees = 180.0f; }
    default boolean allogroomPartner(Individual partner) { return getWave1Trait().allogroomPartner(asIndividual(), partner); }
    default boolean isThoracicIncubationActive() { return getWave1Trait().thoracicIncubationActive; }
    default void incubateBroodThoracicHeat(Individual brood) { getWave1Trait().incubateBroodThoracicHeat(asIndividual(), brood); }
    default boolean isRitualJoustingActive() { return getWave1Trait().ritualJoustingActive; }
    default boolean engageRitualJoustingDisplay(Individual rival) { return getWave1Trait().engageRitualJoustingDisplay(asIndividual(), rival); }
    default boolean isRobberRaidActive() { return getWave1Trait().robberRaidActive; }
    default float getPlunderedHoneyReserves() { return getWave1Trait().plunderedHoneyReserves; }
    default void lootEnemyHiveReserves(float sugarAmt, float propolisAmt) { getWave1Trait().lootEnemyHiveReserves(sugarAmt, propolisAmt); }
    default boolean isEmergencySwarmingTriggered() { return getWave1Trait().emergencySwarmingTriggered; }
    default void triggerEmergencySwarmingFission() { getWave1Trait().triggerEmergencySwarmingFission(asIndividual()); }
    default float getSpiralClayPillarHeightM() { return getWave1Trait().spiralClayPillarHeightM; }
    default void buildSpiralClayPillars(float heightM) { getWave1Trait().spiralClayPillarHeightM = Math.max(getWave1Trait().spiralClayPillarHeightM, heightM); }
    default boolean waterTrophallaxisTransfer(Individual recipient, float amountMl) { return getWave1Trait().waterTrophallaxisTransfer(recipient, amountMl); }
    default int getCulledInfectedAphidsCount() { return getWave1Trait().culledInfectedAphidsCount; }
    default boolean cullInfectedAphidHerd() { getWave1Trait().culledInfectedAphidsCount++; return true; }
    default boolean isLivingBridgeActive() { return getWave1Trait().livingBridgeActive; }
    default float getLivingBridgeSpanMeters() { return getWave1Trait().livingBridgeSpanMeters; }
    default void formLivingBridgeSpan(float gapMeters) { getWave1Trait().formLivingBridgeSpan(gapMeters); }
    default float getPreySurgeAcousticDb() { return getWave1Trait().preySurgeAcousticDb; }
    default void emitPreySurgeAcousticSignal() { getWave1Trait().emitPreySurgeAcousticSignal(asIndividual()); }
    default float getWoodFungusCombSubstrateMg() { return getWave1Trait().woodFungusCombSubstrateMg; }
    default void inoculateWoodFungusComb(float woodMg) { getWave1Trait().woodFungusCombSubstrateMg += Math.max(0.0f, woodMg); }
    default boolean isEmergencyEscapeAlarmActive() { return getWave1Trait().emergencyEscapeAlarmActive; }
    default void depositEmergencyEscapeAlarm() { getWave1Trait().depositEmergencyEscapeAlarm(asIndividual()); }
    default boolean isQueenChamberWaxSealed() { return getWave1Trait().queenChamberWaxSealed; }
    default void sealQueenChamberLipidWax() { getWave1Trait().queenChamberWaxSealed = true; }
    default boolean isSuctionEscapeClamped() { return getWave1Trait().suctionEscapeClamped; }
    default void clampSubstrateSuctionPosture() { getWave1Trait().suctionEscapeClamped = true; }
    default void releaseSubstrateSuctionPosture() { getWave1Trait().suctionEscapeClamped = false; }
    default boolean stridulateQueenRecognitionPacification(Individual worker) { return getWave1Trait().stridulateQueenRecognitionPacification(asIndividual(), worker); }
    default float getVentilationFlowRateLpm() { return getWave1Trait().ventilationFlowRateLpm; }
    default void performPulsatileAbdominalVentilation() { getWave1Trait().ventilationFlowRateLpm = 1.85f; }
    default float adjustDepletingTrailConcentration(float remainingFoodRatio) { return Math.max(0.05f, Math.min(1.0f, remainingFoodRatio)); }
    default boolean carryBroodToSunlitMound(Individual brood) { return getWave1Trait().carryBroodToSunlitMound(asIndividual(), brood); }
    default boolean exchangeChcGestalt(Individual nestmate) { return getWave1Trait().exchangeChcGestalt(asIndividual(), nestmate); }
    default float getCollapsiblePitTrapRadiusM() { return getWave1Trait().collapsiblePitTrapRadiusM; }
    default void excavateCollapsiblePitTrap(float radiusM) { getWave1Trait().collapsiblePitTrapRadiusM = Math.max(0.0f, radiusM); }
    default int getThermoregulatedConduitsCount() { return getWave1Trait().thermoregulatedConduitsCount; }
    default void excavateThermoregulatedConduits(int count) { getWave1Trait().excavateThermoregulatedConduits(asIndividual(), count); }
    default float getToxicPlantResinCarriedMg() { return getWave1Trait().toxicPlantResinCarriedMg; }
    default void gatherToxicPlantResin(float mg) { getWave1Trait().toxicPlantResinCarriedMg = Math.min(80.0f, getWave1Trait().toxicPlantResinCarriedMg + mg); }
    default boolean isSoilDustCamouflageApplied() { return getWave1Trait().soilDustCamouflageApplied; }
    default void applySoilDustCamouflage() { getWave1Trait().applySoilDustCamouflage(asIndividual()); }
    default int getInterlockedMandibleBroodChainCount() { return getWave1Trait().interlockedMandibleBroodChainCount; }
    default boolean linkMandibleBroodChain(Individual larva) { return getWave1Trait().linkMandibleBroodChain(asIndividual(), larva); }
    default boolean transferInhibitoryOvaryPeptides(Individual worker) { return getWave1Trait().transferInhibitoryOvaryPeptides(asIndividual(), worker); }
    default int getEncapsulatedLargeMummiesCount() { return getWave1Trait().encapsulatedLargeMummiesCount; }
    default boolean encapsulateLargeCarcassMummy(float clayMg, float propolisMg) { return getWave1Trait().encapsulateLargeCarcassMummy(clayMg, propolisMg); }
    default boolean isHydrophobicGalleryFilmApplied() { return getWave1Trait().hydrophobicGalleryFilmApplied; }
    default void applyHydrophobicGalleryCoating() { getWave1Trait().hydrophobicGalleryFilmApplied = true; }
    default boolean preFlightVirginQueenLipidEnrichment(Individual virginQueen) { return getWave1Trait().preFlightVirginQueenLipidEnrichment(virginQueen); }
    default boolean isHoneyStoreBrickPluggingActive() { return getWave1Trait().honeyStoreBrickPluggingActive; }
    default void plugHoneyStoresWithBricks() { getWave1Trait().honeyStoreBrickPluggingActive = true; }

    // Wave 2
    default boolean isHatchingAnnouncementVibratoActive() { return getWave2Trait().hatchingAnnouncementVibratoActive; }
    default void announcePreyHatchingVibrato() { getWave2Trait().hatchingAnnouncementVibratoActive = true; }
    default int getResinNymphalMummificationCount() { return getWave2Trait().resinNymphalMummificationCount; }
    default boolean mummifyNymphalCocoonResin(float resinMg) { return getWave2Trait().mummifyNymphalCocoonResin(resinMg); }
    default int getSandPitfallTrapsCount() { return getWave2Trait().sandPitfallTrapsCount; }
    default void excavateSandPitfallTrap() { getWave2Trait().sandPitfallTrapsCount++; }
    default boolean isInjuredPheromonalStretcherTransportActive() { return getWave2Trait().injuredPheromonalStretcherTransportActive; }
    default boolean transportInjuredOnStretcher(Individual injured) { return getWave2Trait().transportInjuredOnStretcher(asIndividual(), injured); }
    default int getAbandonedWaxVaultsRaidedCount() { return getWave2Trait().abandonedWaxVaultsRaidedCount; }
    default void raidAbandonedWaxVault() { getWave2Trait().abandonedWaxVaultsRaidedCount++; }
    default boolean isRitualMandibularWrestlingActive() { return getWave2Trait().ritualMandibularWrestlingActive; }
    default boolean engageRitualMandibularWrestling(Individual rival) { return getWave2Trait().engageRitualMandibularWrestling(rival); }
    default float getPulsedAirConvectiveVentilationRateLpm() { return getWave2Trait().pulsedAirConvectiveVentilationRateLpm; }
    default void performPulsedAirConvectiveVentilation() { getWave2Trait().pulsedAirConvectiveVentilationRateLpm = 2.4f; }
    default boolean isStreptomycesAntibioticCryptsActive() { return getWave2Trait().streptomycesAntibioticCryptsActive; }
    default boolean applyStreptomycesAntibiotics(Individual gardenWorker) { return getWave2Trait().applyStreptomycesAntibiotics(gardenWorker); }
    default float getTwilightUvPolarizationHeading(float sunElevationDeg) { return (float) Math.toRadians((sunElevationDeg + 90.0f) % 360.0f); }
    default boolean isPedestrianSwarmBuddingActive() { return getWave2Trait().pedestrianSwarmBuddingActive; }
    default void triggerPedestrianSwarmBudding() { getWave2Trait().triggerPedestrianSwarmBudding(asIndividual()); }
    default float getPaperPulpCartonMasticationMg() { return getWave2Trait().paperPulpCartonMasticationMg; }
    default void masticatePaperPulpCarton(float woodFrassMg, float salivaMg) { getWave2Trait().paperPulpCartonMasticationMg += (woodFrassMg + salivaMg * 2.0f); }
    default float getLarvalAminoSalivaHarvestedMl() { return getWave2Trait().larvalAminoSalivaHarvestedMl; }
    default boolean harvestLarvalAminoSaliva(Individual larva) { return getWave2Trait().harvestLarvalAminoSaliva(asIndividual(), larva); }
    default boolean isPedicelAntRepellentCoatingApplied() { return getWave2Trait().pedicelAntRepellentCoatingApplied; }
    default void applyPedicelAntRepellentCoating() { getWave2Trait().pedicelAntRepellentCoatingApplied = true; }
    default boolean isFacialPatternVisualRecognitionTrained() { return getWave2Trait().facialPatternVisualRecognitionTrained; }
    default boolean recognizeFacialVisualPattern(Individual nestmate) { return getWave2Trait().recognizeFacialVisualPattern(asIndividual(), nestmate); }
    default float getBuzzPollinationSonicationHz() { return getWave2Trait().buzzPollinationSonicationHz; }
    default void performBuzzPollinationSonication() { getWave2Trait().buzzPollinationSonicationHz = 300.0f; }
    default boolean isBumblebeeAbdominalIncubationActive() { return getWave2Trait().bumblebeeAbdominalIncubationActive; }
    default void performBumblebeeAbdominalIncubation(Individual brood) { getWave2Trait().performBumblebeeAbdominalIncubation(brood); }
    default boolean performAphidSoldierHornStabbing(Individual predator) { return getWave2Trait().performAphidSoldierHornStabbing(predator); }
    default boolean crushGallIntruderThrips(Individual intruder) { return getWave2Trait().crushGallIntruderThrips(intruder); }
    default float getEusocialShrimpCavitationSnapShockwaveDb() { return getWave2Trait().eusocialShrimpCavitationSnapShockwaveDb; }
    default boolean snapClawAcousticShockwave(Individual invader) { return getWave2Trait().snapClawAcousticShockwave(invader); }
    default boolean isPassalidFrassParentalStridulationActive() { return getWave2Trait().passalidFrassParentalStridulationActive; }
    default void stridulatePassalidParentalFrass(Individual grub) { getWave2Trait().stridulatePassalidParentalFrass(grub); }
    default boolean isPhysogastricQueenPeristalsisActive() { return getWave2Trait().physogastricQueenPeristalsisActive; }
    default void performPhysogastricQueenPeristalsis() { getWave2Trait().performPhysogastricQueenPeristalsis(asIndividual()); }
    default float getMagneticMoundOrientationNSRad() { return getWave2Trait().magneticMoundOrientationNSRad; }
    default void orientMagneticMoundNS() { getWave2Trait().magneticMoundOrientationNSRad = 0.0f; }
    default boolean isHornetGroupAlarmVenomSprayActive() { return getWave2Trait().hornetGroupAlarmVenomSprayActive; }
    default void sprayHornetGroupAlarmVenom(Individual beeTarget) { getWave2Trait().sprayHornetGroupAlarmVenom(beeTarget); }
    default float getStenogastrinePaperJellyMg() { return getWave2Trait().stenogastrinePaperJellyMg; }
    default void weaveStenogastrinePaperJelly(float plantFiberMg) { getWave2Trait().stenogastrinePaperJellyMg += plantFiberMg * 1.8f; }
    default int getTermiteFungalCombInoculationCount() { return getWave2Trait().termiteFungalCombInoculationCount; }
    default void inoculateFungalCombTermiteFecalPellet() { getWave2Trait().termiteFungalCombInoculationCount++; }
    default float getWaspCellRimDrummingDb() { return getWave2Trait().waspCellRimDrummingDb; }
    default void drumWaspCellRimWarning() { getWave2Trait().waspCellRimDrummingDb = 82.0f; }
    default int getStinglessBeeSpiralingBroodCellsCount() { return getWave2Trait().stinglessBeeSpiralingBroodCellsCount; }
    default void constructSpiralingBroodCells() { getWave2Trait().stinglessBeeSpiralingBroodCellsCount++; }
    default float getLivingBridgeTensionForceNewtons() { return getWave2Trait().livingBridgeTensionForceNewtons; }
    default float measureLivingBridgeTension() { getWave2Trait().livingBridgeTensionForceNewtons = 0.045f; return getWave2Trait().livingBridgeTensionForceNewtons; }
    default boolean isSubterraneanWaterSiphonPrimed() { return getWave2Trait().subterraneanWaterSiphonPrimed; }
    default void primeSubterraneanWaterSiphon() { getWave2Trait().subterraneanWaterSiphonPrimed = true; }
    default int getSoilHydraulicDrainageChannelCount() { return getWave2Trait().soilHydraulicDrainageChannelCount; }
    default void excavateSoilHydraulicDrainageChannel() { getWave2Trait().soilHydraulicDrainageChannelCount++; }
    default boolean isToxicResinBurrowRepellentApplied() { return getWave2Trait().toxicResinBurrowRepellentApplied; }
    default void applyToxicResinBurrowRepellent() { getWave2Trait().toxicResinBurrowRepellentApplied = true; }
    default float getSiltCamouflageReflectanceReduction() { return getWave2Trait().siltCamouflageReflectanceReduction; }
    default void applySiltCamouflage() { getWave2Trait().siltCamouflageReflectanceReduction = 0.65f; }
    default int getMandibleLinkedChainEscortSize() { return getWave2Trait().mandibleLinkedChainEscortSize; }
    default void escortLarvaeInMandibleChain(int chainSize) { getWave2Trait().mandibleLinkedChainEscortSize = Math.max(0, chainSize); }
    default float getTrophallacticOvaryInhibitoryPeptideTiter() { return getWave2Trait().trophallacticOvaryInhibitoryPeptideTiter; }
    default boolean deliverTrophallacticOvaryInhibition(Individual worker) { return getWave2Trait().deliverTrophallacticOvaryInhibition(worker); }
    default boolean isClayPropolisCarcassHermeticSeal() { return getWave2Trait().clayPropolisCarcassHermeticSeal; }
    default void sealCarcassHermeticClayPropolis() { getWave2Trait().clayPropolisCarcassHermeticSeal = true; }
    default float getHydrophobicEpicuticularLipidWallCoverage() { return getWave2Trait().hydrophobicEpicuticularLipidWallCoverage; }
    default void applyHydrophobicEpicuticularLipidWall() { getWave2Trait().hydrophobicEpicuticularLipidWallCoverage = 0.85f; }
    default float getFermentedSapEnduranceMultiplier() { return getWave2Trait().fermentedSapEnduranceMultiplier; }
    default void activateFermentedSapCombatEndurance() { getWave2Trait().fermentedSapEnduranceMultiplier = 2.2f; }
    default float getPreFlightLipidCropFullness() { return getWave2Trait().preFlightLipidCropFullness; }
    default void gorgePreFlightVirginQueenLipids(Individual gyne) { getWave2Trait().gorgePreFlightVirginQueenLipids(gyne); }
    default boolean isHoneyStoreBrickDefensivePlugging() { return getWave2Trait().honeyStoreBrickDefensivePlugging; }
    default void defensivePlugHoneyStores() { getWave2Trait().honeyStoreBrickDefensivePlugging = true; }
    default boolean isThermalInfraredSensillaActive() { return getWave2Trait().thermalInfraredSensillaActive; }
    default boolean detectThermalInfraredPrey(Individual prey) { return getWave2Trait().detectThermalInfraredPrey(prey); }
    default float getArborealCanopySilkBridgeSpanM() { return getWave2Trait().arborealCanopySilkBridgeSpanM; }
    default void weaveCanopySilkBridgeSpan(float spanM) { getWave2Trait().arborealCanopySilkBridgeSpanM = Math.max(0.0f, spanM); }
    default float getSynchronizedGyneEggStridulationHz() { return getWave2Trait().synchronizedGyneEggStridulationHz; }
    default void stridulateGyneEggBurst() { getWave2Trait().synchronizedGyneEggStridulationHz = 160.0f; }
    default float getTarsalNotchBrushCleansingPercent() { return getWave2Trait().tarsalNotchBrushCleansingPercent; }
    default void cleanAntennalSensillaTarsalNotch() { getWave2Trait().tarsalNotchBrushCleansingPercent = 100.0f; }
    default float getSaltCrystalOsmoticRetentionBar() { return getWave2Trait().saltCrystalOsmoticRetentionBar; }
    default void osmoregulateSaltCrystalRetention() { getWave2Trait().saltCrystalOsmoticRetentionBar = 1.25f; }
    default float getGravityDrainageConduitLitres() { return getWave2Trait().gravityDrainageConduitLitres; }
    default void drainGravityConduitStormwater(float liters) { getWave2Trait().gravityDrainageConduitLitres += Math.max(0.0f, liters); }
    default float getHostTreeHydrocarbonMimicryScore() { return getWave2Trait().hostTreeHydrocarbonMimicryScore; }
    default void absorbHostTreeHydrocarbonMimicry(float score) { getWave2Trait().hostTreeHydrocarbonMimicryScore = Math.min(1.0f, score); }
    default float getMineralSulfurAcaricideDustMg() { return getWave2Trait().mineralSulfurAcaricideDustMg; }
    default void dustBroodMineralSulfurAcaricide(float mg) { getWave2Trait().mineralSulfurAcaricideDustMg += Math.max(0.0f, mg); }
    default float getHatchingVibratoScoutAnnouncementDb() { return getWave2Trait().hatchingVibratoScoutAnnouncementDb; }
    default void announceHatchingPreyVibrato() { getWave2Trait().hatchingVibratoScoutAnnouncementDb = 78.0f; }
    default boolean isAntisepticResinEnvelopeBuilt() { return getWave2Trait().antisepticResinEnvelopeBuilt; }
    default void cocoonInAntisepticResinEnvelope() { getWave2Trait().antisepticResinEnvelopeBuilt = true; }
    default float getFunnelSandyPitfallSlopeDeg() { return getWave2Trait().funnelSandyPitfallSlopeDeg; }
    default void shapeConicalSandyPitfallSlope() { getWave2Trait().funnelSandyPitfallSlopeDeg = 33.0f; }
    default float getCryoprotectantPolyolSynthesisRate() { return getWave2Trait().cryoprotectantPolyolSynthesisRate; }
    default void accumulatePolyolCryoprotectants(float rate) { getWave2Trait().cryoprotectantPolyolSynthesisRate = rate; }
    default boolean isBattlefieldStretcherSquadActive() { return getWave2Trait().battlefieldStretcherSquadActive; }
    default void formBattlefieldStretcherSquad(Individual casualty) { if (casualty != null) getWave2Trait().battlefieldStretcherSquadActive = true; }
    default float getFeralWaxVaultScavengingYieldMg() { return getWave2Trait().feralWaxVaultScavengingYieldMg; }
    default void scavengeFeralWaxVault(float waxMg) { getWave2Trait().feralWaxVaultScavengingYieldMg += Math.max(0.0f, waxMg); }

    // Wave 3
    default float getAttaGardenWasteChamberDigVolumeM3() { return getWave3Trait().attaGardenWasteChamberDigVolumeM3; }
    default void excavateGardenWasteChamber(float volM3) { getWave3Trait().attaGardenWasteChamberDigVolumeM3 += Math.max(0.0f, volM3); }
    default float getTermiteRoyalPairMutualGroomingSec() { return getWave3Trait().termiteRoyalPairMutualGroomingSec; }
    default void exchangeRoyalPairMutualGrooming(Individual royalPartner) { if (royalPartner != null) getWave3Trait().termiteRoyalPairMutualGroomingSec += 30.0f; }
    default boolean isUniversalEmergencyEvacuationActive() { return getWave3Trait().universalEmergencyEvacuationActive; }
    default void triggerUniversalEmergencyEvacuationAll() { getWave3Trait().triggerUniversalEmergencyEvacuationAll(asIndividual()); }
    default float getMyrmecocystusRepleteGasterVolumeUl() { return getWave3Trait().myrmecocystusRepleteGasterVolumeUl; }
    default void distendRepleteGasterVolume(float ul) { getWave3Trait().myrmecocystusRepleteGasterVolumeUl = Math.min(350.0f, getWave3Trait().myrmecocystusRepleteGasterVolumeUl + ul); }
    default int getFloatingAntRaftClawInterlockCount() { return getWave3Trait().floatingAntRaftClawInterlockCount; }
    default void interlockClawsForFloatingRaft(int workerCount) { getWave3Trait().floatingAntRaftClawInterlockCount = Math.max(0, workerCount); }
    default float getMudResinTrumpetFunnelHeightCm() { return getWave3Trait().mudResinTrumpetFunnelHeightCm; }
    default void buildMudResinTrumpetFunnel(float cm) { getWave3Trait().mudResinTrumpetFunnelHeightCm = Math.max(getWave3Trait().mudResinTrumpetFunnelHeightCm, cm); }
    default float getBombusOverwinteringHibernaculumDepthCm() { return getWave3Trait().bombusOverwinteringHibernaculumDepthCm; }
    default void excavateBombusHibernaculum(float depthCm) { getWave3Trait().bombusOverwinteringHibernaculumDepthCm = Math.max(getWave3Trait().bombusOverwinteringHibernaculumDepthCm, depthCm); }
    default float getAcromyrmexLeafMicroMasticationEnzymeMg() { return getWave3Trait().acromyrmexLeafMicroMasticationEnzymeMg; }
    default void inoculateLeafPulpWithDigestiveEnzymes(float mg) { getWave3Trait().acromyrmexLeafMicroMasticationEnzymeMg += Math.max(0.0f, mg); }
    default boolean isDinoponeraGamergateDominanceStingSmear() { return getWave3Trait().dinoponeraGamergateDominanceStingSmear; }
    default boolean applyDominanceStingSmearTournament(Individual rival) { return getWave3Trait().applyDominanceStingSmearTournament(asIndividual(), rival); }
    default float getDraculaLarvalHemolymphSustenanceDose() { return getWave3Trait().draculaLarvalHemolymphSustenanceDose; }
    default void consumeDraculaLarvalHemolymphDose(Individual larva) { getWave3Trait().consumeDraculaLarvalHemolymphDose(asIndividual(), larva); }
    default float getOecophyllaTarsalFrictionBridgeTensileKg() { return getWave3Trait().oecophyllaTarsalFrictionBridgeTensileKg; }
    default void exertTarsalFrictionBridgePull(float pullKg) { getWave3Trait().oecophyllaTarsalFrictionBridgeTensileKg = Math.max(getWave3Trait().oecophyllaTarsalFrictionBridgeTensileKg, pullKg); }
    default float getPachycondylaMandibleSurfaceTensionDropUl() { return getWave3Trait().pachycondylaMandibleSurfaceTensionDropUl; }
    default void trapMandibleSurfaceTensionWaterDrop(float ul) { getWave3Trait().pachycondylaMandibleSurfaceTensionDropUl = Math.min(25.0f, ul); }
    default float getGiantHoneybeeShimmeringWavePhaseRad() { return getWave3Trait().giantHoneybeeShimmeringWavePhaseRad; }
    default void syncGiantHoneybeeShimmeringWave() { getWave3Trait().giantHoneybeeShimmeringWavePhaseRad = (float) Math.PI; }
    default float getPaperWaspEvaporativeWaterDouseMl() { return getWave3Trait().paperWaspEvaporativeWaterDouseMl; }
    default void dousePaperWaspCombWithWater(float ml) { getWave3Trait().paperWaspEvaporativeWaterDouseMl += Math.max(0.0f, ml); }
    default int getTermiteFungalCombClayMicroPoresCount() { return getWave3Trait().termiteFungalCombClayMicroPoresCount; }
    default void perforateClayWallFungalPores(int count) { getWave3Trait().termiteFungalCombClayMicroPoresCount += Math.max(0, count); }
    default float getPassalidLarvalChitinNitrogenRecyclingMg() { return getWave3Trait().passalidLarvalChitinNitrogenRecyclingMg; }
    default void feedLarvaeNitrogenousExuvia(Individual larva, float mg) { getWave3Trait().feedLarvaeNitrogenousExuvia(larva, mg); }
    default int getAttaMinimPhoridFlyEggGroomingCount() { return getWave3Trait().attaMinimPhoridFlyEggGroomingCount; }
    default void groomMinimPhoridFlyEggs(Individual forager) { getWave3Trait().attaMinimPhoridFlyEggGroomingCount++; }
    default float getSocialSpiderWebDebrisDisguisePercent() { return getWave3Trait().socialSpiderWebDebrisDisguisePercent; }
    default void attachPlantDebrisWebDisguise(float coveragePercent) { getWave3Trait().socialSpiderWebDebrisDisguisePercent = Math.min(100.0f, coveragePercent); }
    default boolean isAcrobatAntGasterVenomAerosolActive() { return getWave3Trait().acrobatAntGasterVenomAerosolActive; }
    default void dischargeAcrobatVenomAerosol(Individual attacker) { getWave3Trait().dischargeAcrobatVenomAerosol(attacker); }
    default float getLasiusAphidAntennalStrokingRateHz() { return getWave3Trait().lasiusAphidAntennalStrokingRateHz; }
    default void strokeAphidAntennalHoneydew(float durationSec) { getWave3Trait().strokeAphidAntennalHoneydew(asIndividual(), durationSec); }
    default int getFormicaSolarHeatCollectorClusterCount() { return getWave3Trait().formicaSolarHeatCollectorClusterCount; }
    default void baskInSolarMoundCollectorCluster(int clusterSize) { getWave3Trait().baskInSolarMoundCollectorCluster(asIndividual(), clusterSize); }
    default boolean isNectarReceiverTrembleDanceResponseActive() { return getWave3Trait().nectarReceiverTrembleDanceResponseActive; }
    default void respondToTrembleDanceForNectar() { getWave3Trait().nectarReceiverTrembleDanceResponseActive = true; }
    default float getSubterraneanAirCurrentFlappingHz() { return getWave3Trait().subterraneanAirCurrentFlappingHz; }
    default void flapAbdomenSubterraneanAirCurrent() { getWave3Trait().subterraneanAirCurrentFlappingHz = 18.0f; }
    default float getSocialCropNectarSucroseBrixPercent() { return getWave3Trait().socialCropNectarSucroseBrixPercent; }
    default void setSocialCropSucroseBrix(float brix) { getWave3Trait().socialCropNectarSucroseBrixPercent = Math.max(0.0f, Math.min(100.0f, brix)); }
    default int getForagerVisualLandmarkMemoryEntries() { return getWave3Trait().foragerVisualLandmarkMemoryEntries; }
    default void recordVisualLandmarkMemory(float x, float y, float heading) { getWave3Trait().foragerVisualLandmarkMemoryEntries++; }
    default float getSubterraneanCarbonDioxideTolerancePpm() { return getWave3Trait().subterraneanCarbonDioxideTolerancePpm; }
    default void adaptSubterraneanGasTolerance(float co2Ppm) { getWave3Trait().subterraneanCarbonDioxideTolerancePpm = Math.max(25000.0f, co2Ppm); }
    default float getFungalGardenSterilizingMetapleuralSecretionMg() { return getWave3Trait().fungalGardenSterilizingMetapleuralSecretionMg; }
    default void applyMetapleuralGardenSterilization(float mg) { getWave3Trait().fungalGardenSterilizingMetapleuralSecretionMg += Math.max(0.0f, mg); }
    default float getNurseAntFatBodyVitellogeninTiter() { return getWave3Trait().nurseAntFatBodyVitellogeninTiter; }
    default void elevateFatBodyVitellogenin(float vitellogenin) { getWave3Trait().nurseAntFatBodyVitellogeninTiter = Math.max(0.0f, Math.min(1.0f, vitellogenin)); }
    default float getQueenNuptialFlightSpermathecaCapacity() { return getWave3Trait().queenNuptialFlightSpermathecaCapacity; }
    default void storeSpermathecaSpermReserves(float spermCount) { getWave3Trait().queenNuptialFlightSpermathecaCapacity = Math.max(0.0f, spermCount); }
    default float getCallowCuticularTanningSclerotizationScore() { return getWave3Trait().callowCuticularTanningSclerotizationScore; }
    default void progressCallowSclerotization(float dtSec) { getWave3Trait().callowCuticularTanningSclerotizationScore = Math.min(1.0f, getWave3Trait().callowCuticularTanningSclerotizationScore + dtSec * 0.001f); }
    default int getInterColonyWarfareCasualtyAttritionIndex() { return getWave3Trait().interColonyWarfareCasualtyAttritionIndex; }
    default void recordInterColonyWarfareCasualty() { getWave3Trait().interColonyWarfareCasualtyAttritionIndex++; }
    default float getLeafCutterMandibleChitinZincHardeningGpa() { return getWave3Trait().leafCutterMandibleChitinZincHardeningGpa; }
    default void hardenMandibleChitinZinc(float hardnessGpa) { getWave3Trait().leafCutterMandibleChitinZincHardeningGpa = Math.max(getWave3Trait().leafCutterMandibleChitinZincHardeningGpa, hardnessGpa); }
    default float getSubterraneanFungalMoistureSpongeTranslocation() { return getWave3Trait().subterraneanFungalMoistureSpongeTranslocation; }
    default void translocateFungalMoistureSponge(float moistureAmt) { getWave3Trait().subterraneanFungalMoistureSpongeTranslocation += Math.max(0.0f, moistureAmt); }
    default boolean evaluateUnicolonialChcAcceptance(Individual alienAnt) { return getWave3Trait().evaluateUnicolonialChcAcceptance(asIndividual(), alienAnt); }
}
