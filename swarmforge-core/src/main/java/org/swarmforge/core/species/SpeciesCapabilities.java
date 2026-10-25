/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.BitSet;

/**
 * Ethological capability flags and high-performance BitSet mappings
 * for social insect behavioral traits (Batches I to XVII).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SpeciesCapabilities implements Species {

    @JsonIgnore
    protected final BitSet capabilitiesBitSet = new BitSet(256);

    protected boolean canPerformBiostructures = false;
    protected boolean canPerformNecrophoresis = true;
    protected boolean canPerformSocialThermoregulation = false;
    protected boolean isUnicolonial = false;
    protected boolean hasMandibularWearPolyethism = true;
    protected boolean canPerformTandemRunning = false;
    protected boolean canPerformWaggleDance = false;
    protected boolean canPerformLarvalSalivaryTrophallaxis = false;
    protected boolean hasTermiteGutSymbiosis = false;
    protected boolean canFarmAphids = false;
    protected boolean canFarmFungus = false;
    protected boolean hasRoyalPheromoneInhibition = true;
    protected boolean canDrumSubstrate = false;
    protected boolean isPolycalic = false;
    protected boolean canCollectPropolis = false;
    protected boolean canSewLeavesWithLarvalSilk = false;
    protected boolean canWeedFungusGarden = false;
    protected boolean canMakeStercoralCement = false;
    protected boolean hasProctodealTrophallaxis = false;
    protected boolean canPerformPhragmosis = false;
    protected boolean canPerformEvaporativeCooling = false;
    protected boolean hasTrapJawMechanism = false;
    protected boolean isSlaveMakingSpecies = false;
    protected boolean canFormLivingBivouac = false;
    protected boolean hasSolarOrientedMound = false;
    protected boolean canPerformAllogrooming = true;
    protected boolean canPerformTrembleDance = false;
    protected boolean hasThermalTrailDecay = true;
    protected boolean canPerformThoracicIncubation = false;
    protected boolean canPerformRitualJousting = false;
    protected boolean hasTerritorialRepellentPheromone = false;
    protected boolean canDetectHydrostaticPressure = true;
    protected boolean isRobberBeeSpecies = false;
    protected boolean canStridulateRescueCall = false;
    protected boolean isHoneypotStorageCaste = false;
    protected boolean canPlugContaminatedGalleries = false;
    protected boolean hasOleicAcidThresholdNecrophoresis = true;
    protected boolean hasUVPolarizedLightNavigation = false;
    protected boolean canPerformThermalBalling = false;
    protected boolean canFormLivingRaft = false;
    protected boolean canInhabitDomatia = false;
    protected boolean canSelfIsolateWhenInfected = true;
    protected boolean canSprayFormicResinDisinfectant = false;
    protected boolean canTriggerEmergencySwarming = true;
    protected boolean canConstructClayPillars = false;
    protected boolean canDeGermStoredSeeds = false;
    protected boolean canPerformQueenPiping = false;
    protected boolean canPerformWaterTrophallaxis = true;
    protected boolean canEnforceAphidSanitaryCordon = false;
    protected boolean canFormLivingBridges = false;
    protected boolean canEmitAcousticPreySurge = false;
    protected boolean canSortExternalRefusePits = true;
    protected boolean canCultivateWoodFungus = false;
    protected boolean hasEmergencyEscapePheromone = true;
    protected boolean canSealQueenChamberWax = false;
    protected boolean hasCasteRatioPheromoneInhibition = true;
    protected boolean canPerformSuctionEscapePosture = false;
    protected boolean canStridulateQueenRecognition = false;
    protected boolean canPerformPulsatileVentilation = false;
    protected boolean canRepairBreachesClay = true;
    protected boolean hasDepletingTrailPheromone = true;
    protected boolean canRecycleInviableEggs = true;
    protected boolean canQuarantineInvasiveParasites = false;
    protected boolean canPerformArborealGlidingEscape = false;
    protected boolean canPerformSolarBroodBasking = false;
    protected boolean canHarmonizeChcGestalt = true;
    protected boolean canConstructCollapsiblePitTraps = false;
    protected boolean canHarvestDewCondensation = true;
    protected boolean canPerformExoskeletonAntiFungalPatrol = true;
    protected boolean canPerformGuardShiftVibrationalWhisper = false;
    protected boolean canConstructThermoregulatedConduits = false;
    protected boolean canRaidToxicPlantResin = false;
    protected boolean canApplyDustSubstrateCamouflage = false;
    protected boolean canTransportChainBrood = true;
    protected boolean hasTrophallacticOvaryInhibition = true;
    protected boolean canPerformDroughtVibratoDance = false;
    protected boolean canEncapsulateLargeIntrudersClay = true;
    protected boolean canConstructPhonicIsolationChambers = false;
    protected boolean canApplyHydrophobicTrailCoating = false;
    protected boolean canConsumeFermentedSapAnesthetic = false;
    protected boolean canPerformRelaySeedTransport = true;
    protected boolean hasPreySizeSelectivePheromones = true;
    protected boolean canDryLarvaeWoodDust = true;
    protected boolean canPerformFanoutEscapeFormicAcid = true;
    protected boolean canEmitMoundOverheatVibrato = false;
    protected boolean canNourishVirginQueensPreFlight = true;
    protected boolean canPlugHoneyStoresBricks = true;

    // Batch IX fields (81-90)
    protected boolean canHuntNocturnalInfrared = false;
    protected boolean canWeaveLarvalSilkCanopyBridges = false;
    protected boolean canStridulateEggLayingSynchronization = false;
    protected boolean canPerformAntennalDustGrooming = true;
    protected boolean canForageSaltCrystalsOsmoregulation = false;
    protected boolean canConstructRainEvacuationSiphons = false;
    protected boolean canAbsorbHostPlantChemicalCamouflage = false;
    protected boolean canDepositSulfurDustAntiMitePatrol = false;
    protected boolean canDanceVibratoHatchingEnthusiasm = true;
    protected boolean canResinMummifyNymphalChambers = false;

    // Batch X fields (91-100)
    protected boolean canExcavatePitfallTraps = false;
    protected boolean canSynthesizeGlycerolCryoprotection = true;
    protected boolean canTransportInjuredPheromonalStretcher = true;
    protected boolean canRaidAbandonedWaxVaults = false;
    protected boolean canPerformRitualMandibularWrestling = true;
    protected boolean canPerformPulsedAirConvectiveVentilation = false;
    protected boolean canCultivateStreptomycesAntibiotics = true;
    protected boolean canNavigatePolarizedTwilightUV = true;
    protected boolean canSnapTrapMandiblesCatapult = false;
    protected boolean canPerformPedestrianSwarmBudding = true;

    // Batch XI & XII fields (101-120)
    protected boolean canTrophallaxisProtozoa = false;
    protected boolean canSquirtNasuteChemical = false;
    protected boolean canMasticatePaperPulpCarton = false;
    protected boolean canHarvestLarvalSalivaDroplets = false;
    protected boolean canApplyPedicelAntRepellent = false;
    protected boolean canRecognizeFacialVisualPatterns = false;
    protected boolean canPerformBuzzPollination = false;
    protected boolean canIncubateBroodAbdominalHeat = false;
    protected boolean canStabFrontalHornsAphid = false;
    protected boolean canSqueezeGallIntrudersThrips = false;
    protected boolean canSnapClawAcousticShockwave = false;
    protected boolean canStridulatePassalidParentalCare = false;
    protected boolean canPerformPhysogastricPeristalsis = false;
    protected boolean canOrientMagneticMound = false;
    protected boolean canEmitHornetGroupAlarmPheromone = false;
    protected boolean canWeaveStenogastrinePaperJelly = false;
    protected boolean canInoculateFungalCombTermite = false;
    protected boolean canDrumAbdomenWaspCellRim = false;
    protected boolean canConstructNectarWaxPots = false;
    protected boolean canPerformMaternalShieldGuarding = false;

    // Batch XIII fields (121-140)
    protected boolean canWeaveCommunalSpiderSilk = false;
    protected boolean canFormProcessionarySilkTrail = false;
    protected boolean canConstructClayVaultArches = false;
    protected boolean canDeliverStenogastrinePapFood = false;
    protected boolean canPlasterFrassGalleryWalls = false;
    protected boolean canLearnTrapliningFlightRoutes = false;
    protected boolean canCoolNestWaterRegurgitation = false;
    protected boolean canEjectHoneydewSignalingDroplets = false;
    protected boolean canSnapMandibleAcousticAlarm = false;
    protected boolean canPerformEggLickingGrooming = false;
    protected boolean canConstructChaffGarbageDunes = false;
    protected boolean canDrumAntennaeLarvalStimulation = false;
    protected boolean canFormLeafPullingChains = false;
    protected boolean canApplySalivaryCementMoistureSeal = false;
    protected boolean canForageSubZeroBumblebee = false;
    protected boolean canRepairGallSubstratalSecretion = false;
    protected boolean canTrophallaxisPassalidWoodFrass = false;
    protected boolean canPerformCrecheRegurgitationSpider = false;
    protected boolean canBlockRoyalChamberSentry = false;
    protected boolean canEmitParentBugAlarmGathering = false;

    // Batch XIV fields (141-160)
    protected boolean canApplyBeeBreadHydrophobicCoating = false;
    protected boolean canBindParasitesWithSilk = false;
    protected boolean canEmitSubstrateObstacleVibrato = false;
    protected boolean canPerformFormicAcidBathGrooming = false;
    protected boolean canExcavateVerticalDrainageShafts = false;
    protected boolean canIngestPhenolicResinMedication = false;
    protected boolean canConstructSphagnumMoistureDomes = false;
    protected boolean canMarkParasitizedCadaverRepellent = false;
    protected boolean canDrumNuptialFlightSynchronization = false;
    protected boolean canHarvestCuticularWaterCondensation = false;
    protected boolean canStridulateLarvalHungerChirp = false;
    protected boolean canConstructThermalChimneyFlues = false;
    protected boolean canDepositLarvalFoodSalivaDrop = false;
    protected boolean canApplyEggMassMucilageEnvelope = false;
    protected boolean canWeaveSilkPavilionAphidShelter = false;
    protected boolean canFormHotBallThermalDefense = false;
    protected boolean canPerformFontanelleAutothysis = false;
    protected boolean canSensePreySignalWireTripping = false;
    protected boolean canMutilateSeedRadicles = false;
    protected boolean canBiteNectarTheftHoles = false;

    // Batch XV fields (161-180)
    protected boolean canSowFungalSporeCombs = false;
    protected boolean canHarnessLarvalSilkCocoon = false;
    protected boolean canFormBiomechanicalBivouac = false;
    protected boolean canPerformBuzzPollinationSonication = false;
    protected boolean canRegurgitateEarwigMaternalFood = false;
    protected boolean canRecognizeWaspFacialPatterns = false;
    protected boolean canFireShrimpAcousticCannon = false;
    protected boolean canDuetPassalidSubstrateVibration = false;
    protected boolean canTurnGranarySeedsAeration = false;
    protected boolean canEncodeWaggleDanceSunCompass = false;
    protected boolean canDigSubterraneanClayAqueducts = false;
    protected boolean canFireFormicAcidArtilleryJet = false;
    protected boolean canEjectGarbageChuteRefuse = false;
    protected boolean canFanWingsForBroodThermoregulation = false;
    protected boolean canPlugGallWithChitinousTube = false;
    protected boolean canCoatWaspPedicelAntRepellent = false;
    protected boolean canSquirtNasuteViscousResin = false;
    protected boolean canSqueezeIntrudersWithForelegs = false;
    protected boolean canShieldEggsFromParasitoidWasps = false;
    protected boolean canPlasterWoodWallGallery = false;

    // Batch XVI fields (181-200)
    protected boolean canShearLeafCrescentMandible = false;
    protected boolean canShieldSwarmCoreHeat = false;
    protected boolean canPerformQueenPhysogastricPeristalsis = false;
    protected boolean canWeaveSocialSilkHammock = false;
    protected boolean canPackCorbiculaPollenBaskets = false;
    protected boolean canScrapeWoodPulpCarton = false;
    protected boolean canLayTrophicNourishmentEggs = false;
    protected boolean canNavigatePolarizedLightCompass = false;
    protected boolean canBuryFungalWasteInGallery = false;
    protected boolean canChewSeedHuskBreadPulp = false;
    protected boolean canWrapPreyInCommunalSilk = false;
    protected boolean canSealNestGapsWithPropolis = false;
    protected boolean canSynchronizeSoldierAlarmDrumming = false;
    protected boolean canPerformDominanceMounting = false;
    protected boolean canLapNectarTongueExtension = false;
    protected boolean canSecreteGallClosingFluid = false;
    protected boolean canGroomNymphCuticularSurface = false;
    protected boolean canExcavateGardenWasteChambers = false;
    protected boolean canExchangeRoyalPairGrooming = false;
    protected boolean canTriggerUniversalEmergencyEvacuation = false;

    // Batch XVII fields (201-220)
    protected boolean canStoreNectarAsHoneypotReplete = false;
    protected boolean canFormFloatingAntRaft = false;
    protected boolean canConstructMudResinEntranceFunnel = false;
    protected boolean canExcavateHibernationBurrow = false;
    protected boolean canInoculateLeafPulpEnzymes = false;
    protected boolean canPerformGamergateDominanceTournament = false;
    protected boolean canFeedOnLarvalHemolymphDracula = false;
    protected boolean canFormTarsalFrictionBridge = false;
    protected boolean canTransportWaterInMandibleDroplet = false;
    protected boolean canStiltWalkThermalRegim = false;
    protected boolean canPerformAntiPredatorShimmeringWave = false;
    protected boolean canDouseNestWaterCooling = false;
    protected boolean canAerateFungalCombChambers = false;
    protected boolean canFeedLarvaeExuviaRecycling = false;
    protected boolean canGroomLeafPulpParasitesMinim = false;
    protected boolean canCamouflageWebWithPlantDebris = false;
    protected boolean canCockGasterFormicAcidRepellent = false;
    protected boolean canMilkAphidHoneydewStroking = false;
    protected boolean canClusterSolarHeatCollector = false;
    protected boolean canSerializeGlobalEthologicalBitSet = false;

    @Override public boolean canPerformBiostructures() { return canPerformBiostructures; }
    public void setCanPerformBiostructures(boolean val) { this.canPerformBiostructures = val; }

    @Override public boolean canPerformNecrophoresis() { return canPerformNecrophoresis; }
    public void setCanPerformNecrophoresis(boolean val) { this.canPerformNecrophoresis = val; }

    @Override public boolean canPerformSocialThermoregulation() { return canPerformSocialThermoregulation; }
    public void setCanPerformSocialThermoregulation(boolean val) { this.canPerformSocialThermoregulation = val; }

    @Override public boolean isUnicolonial() { return isUnicolonial; }
    public void setIsUnicolonial(boolean val) { this.isUnicolonial = val; }

    @Override public boolean hasMandibularWearPolyethism() { return hasMandibularWearPolyethism; }
    public void setHasMandibularWearPolyethism(boolean val) { this.hasMandibularWearPolyethism = val; }

    @Override public boolean canPerformTandemRunning() { return canPerformTandemRunning; }
    public void setCanPerformTandemRunning(boolean val) { this.canPerformTandemRunning = val; }

    @Override public boolean canPerformWaggleDance() { return canPerformWaggleDance; }
    public void setCanPerformWaggleDance(boolean val) { this.canPerformWaggleDance = val; }

    @Override public boolean canPerformLarvalSalivaryTrophallaxis() { return canPerformLarvalSalivaryTrophallaxis; }
    public void setCanPerformLarvalSalivaryTrophallaxis(boolean val) { this.canPerformLarvalSalivaryTrophallaxis = val; }

    @Override public boolean hasTermiteGutSymbiosis() { return hasTermiteGutSymbiosis; }
    public void setHasTermiteGutSymbiosis(boolean val) { this.hasTermiteGutSymbiosis = val; }

    @Override public boolean canFarmAphids() { return canFarmAphids; }
    public void setCanFarmAphids(boolean val) { this.canFarmAphids = val; }

    @Override public boolean canFarmFungus() { return canFarmFungus || canWeedFungusGarden || "FUNGUS".equalsIgnoreCase(getPrimaryDiet()); }
    public void setCanFarmFungus(boolean val) { this.canFarmFungus = val; }

    @Override public boolean hasRoyalPheromoneInhibition() { return hasRoyalPheromoneInhibition; }
    public void setHasRoyalPheromoneInhibition(boolean val) { this.hasRoyalPheromoneInhibition = val; }

    @Override public boolean canDrumSubstrate() { return canDrumSubstrate; }
    public void setCanDrumSubstrate(boolean val) { this.canDrumSubstrate = val; }

    @Override public boolean isPolycalic() { return isPolycalic; }
    public void setIsPolycalic(boolean val) { this.isPolycalic = val; }

    @Override public boolean canCollectPropolis() { return canCollectPropolis; }
    public void setCanCollectPropolis(boolean val) { this.canCollectPropolis = val; }

    @Override public boolean canSewLeavesWithLarvalSilk() { return canSewLeavesWithLarvalSilk; }
    public void setCanSewLeavesWithLarvalSilk(boolean val) { this.canSewLeavesWithLarvalSilk = val; }

    @Override public boolean canWeedFungusGarden() { return canWeedFungusGarden; }
    public void setCanWeedFungusGarden(boolean val) { this.canWeedFungusGarden = val; }

    @Override public boolean canMakeStercoralCement() { return canMakeStercoralCement; }
    public void setCanMakeStercoralCement(boolean val) { this.canMakeStercoralCement = val; }

    @Override public boolean hasProctodealTrophallaxis() { return hasProctodealTrophallaxis; }
    public void setHasProctodealTrophallaxis(boolean val) { this.hasProctodealTrophallaxis = val; }

    @Override public boolean canPerformPhragmosis() { return canPerformPhragmosis; }
    public void setCanPerformPhragmosis(boolean val) { this.canPerformPhragmosis = val; }

    @Override public boolean canPerformEvaporativeCooling() { return canPerformEvaporativeCooling; }
    public void setCanPerformEvaporativeCooling(boolean val) { this.canPerformEvaporativeCooling = val; }

    @Override public boolean hasTrapJawMechanism() { return hasTrapJawMechanism; }
    public void setHasTrapJawMechanism(boolean val) { this.hasTrapJawMechanism = val; }

    @Override public boolean isSlaveMakingSpecies() { return isSlaveMakingSpecies; }
    public void setIsSlaveMakingSpecies(boolean val) { this.isSlaveMakingSpecies = val; }

    @Override public boolean canFormLivingBivouac() { return canFormLivingBivouac; }
    public void setCanFormLivingBivouac(boolean val) { this.canFormLivingBivouac = val; }

    @Override public boolean hasSolarOrientedMound() { return hasSolarOrientedMound; }
    public void setHasSolarOrientedMound(boolean val) { this.hasSolarOrientedMound = val; }

    @Override public boolean canPerformAllogrooming() { return canPerformAllogrooming; }
    public void setCanPerformAllogrooming(boolean val) { this.canPerformAllogrooming = val; }

    @Override public boolean canPerformTrembleDance() { return canPerformTrembleDance; }
    public void setCanPerformTrembleDance(boolean val) { this.canPerformTrembleDance = val; }

    @Override public boolean hasThermalTrailDecay() { return hasThermalTrailDecay; }
    public void setHasThermalTrailDecay(boolean val) { this.hasThermalTrailDecay = val; }

    @Override public boolean canPerformThoracicIncubation() { return canPerformThoracicIncubation; }
    public void setCanPerformThoracicIncubation(boolean val) { this.canPerformThoracicIncubation = val; }

    @Override public boolean canPerformRitualJousting() { return canPerformRitualJousting; }
    public void setCanPerformRitualJousting(boolean val) { this.canPerformRitualJousting = val; }

    @Override public boolean hasTerritorialRepellentPheromone() { return hasTerritorialRepellentPheromone; }
    public void setHasTerritorialRepellentPheromone(boolean val) { this.hasTerritorialRepellentPheromone = val; }

    @Override public boolean canDetectHydrostaticPressure() { return canDetectHydrostaticPressure; }
    public void setCanDetectHydrostaticPressure(boolean val) { this.canDetectHydrostaticPressure = val; }

    @Override public boolean isRobberBeeSpecies() { return isRobberBeeSpecies; }
    public void setIsRobberBeeSpecies(boolean val) { this.isRobberBeeSpecies = val; }

    @Override public boolean canStridulateRescueCall() { return canStridulateRescueCall; }
    public void setCanStridulateRescueCall(boolean val) { this.canStridulateRescueCall = val; }

    @Override public boolean isHoneypotStorageCaste() { return isHoneypotStorageCaste; }
    public void setIsHoneypotStorageCaste(boolean val) { this.isHoneypotStorageCaste = val; }

    @Override public boolean canPlugContaminatedGalleries() { return canPlugContaminatedGalleries; }
    public void setCanPlugContaminatedGalleries(boolean val) { this.canPlugContaminatedGalleries = val; }

    @Override public boolean hasOleicAcidThresholdNecrophoresis() { return hasOleicAcidThresholdNecrophoresis; }
    public void setHasOleicAcidThresholdNecrophoresis(boolean val) { this.hasOleicAcidThresholdNecrophoresis = val; }

    @Override public boolean hasUVPolarizedLightNavigation() { return hasUVPolarizedLightNavigation; }
    public void setHasUVPolarizedLightNavigation(boolean val) { this.hasUVPolarizedLightNavigation = val; }

    @Override public boolean canPerformThermalBalling() { return canPerformThermalBalling; }
    public void setCanPerformThermalBalling(boolean val) { this.canPerformThermalBalling = val; }

    @Override public boolean canFormLivingRaft() { return canFormLivingRaft; }
    public void setCanFormLivingRaft(boolean val) { this.canFormLivingRaft = val; }

    @Override public boolean canInhabitDomatia() { return canInhabitDomatia; }
    public void setCanInhabitDomatia(boolean val) { this.canInhabitDomatia = val; }

    @Override public boolean canSelfIsolateWhenInfected() { return canSelfIsolateWhenInfected; }
    public void setCanSelfIsolateWhenInfected(boolean val) { this.canSelfIsolateWhenInfected = val; }

    @Override public boolean canSprayFormicResinDisinfectant() { return canSprayFormicResinDisinfectant; }
    public void setCanSprayFormicResinDisinfectant(boolean val) { this.canSprayFormicResinDisinfectant = val; }

    @Override public boolean canTriggerEmergencySwarming() { return canTriggerEmergencySwarming; }
    public void setCanTriggerEmergencySwarming(boolean val) { this.canTriggerEmergencySwarming = val; }

    @Override public boolean canConstructClayPillars() { return canConstructClayPillars; }
    public void setCanConstructClayPillars(boolean val) { this.canConstructClayPillars = val; }

    @Override public boolean canDeGermStoredSeeds() { return canDeGermStoredSeeds; }
    public void setCanDeGermStoredSeeds(boolean val) { this.canDeGermStoredSeeds = val; }

    @Override public boolean canPerformQueenPiping() { return canPerformQueenPiping; }
    public void setCanPerformQueenPiping(boolean val) { this.canPerformQueenPiping = val; }

    @Override public boolean canPerformWaterTrophallaxis() { return canPerformWaterTrophallaxis; }
    public void setCanPerformWaterTrophallaxis(boolean val) { this.canPerformWaterTrophallaxis = val; }

    @Override public boolean canEnforceAphidSanitaryCordon() { return canEnforceAphidSanitaryCordon; }
    public void setCanEnforceAphidSanitaryCordon(boolean val) { this.canEnforceAphidSanitaryCordon = val; }

    @Override public boolean canFormLivingBridges() { return canFormLivingBridges; }
    public void setCanFormLivingBridges(boolean val) { this.canFormLivingBridges = val; }

    @Override public boolean canEmitAcousticPreySurge() { return canEmitAcousticPreySurge; }
    public void setCanEmitAcousticPreySurge(boolean val) { this.canEmitAcousticPreySurge = val; }

    @Override public boolean canSortExternalRefusePits() { return canSortExternalRefusePits; }
    public void setCanSortExternalRefusePits(boolean val) { this.canSortExternalRefusePits = val; }

    @Override public boolean canCultivateWoodFungus() { return canCultivateWoodFungus; }
    public void setCanCultivateWoodFungus(boolean val) { this.canCultivateWoodFungus = val; }

    @Override public boolean hasEmergencyEscapePheromone() { return hasEmergencyEscapePheromone; }
    public void setHasEmergencyEscapePheromone(boolean val) { this.hasEmergencyEscapePheromone = val; }

    @Override public boolean canSealQueenChamberWax() { return canSealQueenChamberWax; }
    public void setCanSealQueenChamberWax(boolean val) { this.canSealQueenChamberWax = val; }

    @Override public boolean hasCasteRatioPheromoneInhibition() { return hasCasteRatioPheromoneInhibition; }
    public void setHasCasteRatioPheromoneInhibition(boolean val) { this.hasCasteRatioPheromoneInhibition = val; }

    @Override public boolean canPerformSuctionEscapePosture() { return canPerformSuctionEscapePosture; }
    public void setCanPerformSuctionEscapePosture(boolean val) { this.canPerformSuctionEscapePosture = val; }

    @Override public boolean canStridulateQueenRecognition() { return canStridulateQueenRecognition; }
    public void setCanStridulateQueenRecognition(boolean val) { this.canStridulateQueenRecognition = val; }

    @Override public boolean canPerformPulsatileVentilation() { return canPerformPulsatileVentilation; }
    public void setCanPerformPulsatileVentilation(boolean val) { this.canPerformPulsatileVentilation = val; }

    @Override public boolean canRepairBreachesClay() { return canRepairBreachesClay; }
    public void setCanRepairBreachesClay(boolean val) { this.canRepairBreachesClay = val; }

    @Override public boolean hasDepletingTrailPheromone() { return hasDepletingTrailPheromone; }
    public void setHasDepletingTrailPheromone(boolean val) { this.hasDepletingTrailPheromone = val; }

    @Override public boolean canRecycleInviableEggs() { return canRecycleInviableEggs; }
    public void setCanRecycleInviableEggs(boolean val) { this.canRecycleInviableEggs = val; }

    @Override public boolean canQuarantineInvasiveParasites() { return canQuarantineInvasiveParasites; }
    public void setCanQuarantineInvasiveParasites(boolean val) { this.canQuarantineInvasiveParasites = val; }

    @Override public boolean canPerformArborealGlidingEscape() { return canPerformArborealGlidingEscape; }
    public void setCanPerformArborealGlidingEscape(boolean val) { this.canPerformArborealGlidingEscape = val; }

    @Override public boolean canPerformSolarBroodBasking() { return canPerformSolarBroodBasking; }
    public void setCanPerformSolarBroodBasking(boolean val) { this.canPerformSolarBroodBasking = val; }

    @Override public boolean canHarmonizeChcGestalt() { return canHarmonizeChcGestalt; }
    public void setCanHarmonizeChcGestalt(boolean val) { this.canHarmonizeChcGestalt = val; }

    @Override public boolean canConstructCollapsiblePitTraps() { return canConstructCollapsiblePitTraps; }
    public void setCanConstructCollapsiblePitTraps(boolean val) { this.canConstructCollapsiblePitTraps = val; }

    @Override public boolean canHarvestDewCondensation() { return canHarvestDewCondensation; }
    public void setCanHarvestDewCondensation(boolean val) { this.canHarvestDewCondensation = val; }

    @Override public boolean canPerformExoskeletonAntiFungalPatrol() { return canPerformExoskeletonAntiFungalPatrol; }
    public void setCanPerformExoskeletonAntiFungalPatrol(boolean val) { this.canPerformExoskeletonAntiFungalPatrol = val; }

    @Override public boolean canPerformGuardShiftVibrationalWhisper() { return canPerformGuardShiftVibrationalWhisper; }
    public void setCanPerformGuardShiftVibrationalWhisper(boolean val) { this.canPerformGuardShiftVibrationalWhisper = val; }

    @Override public boolean canConstructThermoregulatedConduits() { return canConstructThermoregulatedConduits; }
    public void setCanConstructThermoregulatedConduits(boolean val) { this.canConstructThermoregulatedConduits = val; }

    @Override public boolean canRaidToxicPlantResin() { return canRaidToxicPlantResin; }
    public void setCanRaidToxicPlantResin(boolean val) { this.canRaidToxicPlantResin = val; }

    @Override public boolean canApplyDustSubstrateCamouflage() { return canApplyDustSubstrateCamouflage; }
    public void setCanApplyDustSubstrateCamouflage(boolean val) { this.canApplyDustSubstrateCamouflage = val; }

    @Override public boolean canTransportChainBrood() { return canTransportChainBrood; }
    public void setCanTransportChainBrood(boolean val) { this.canTransportChainBrood = val; }

    @Override public boolean hasTrophallacticOvaryInhibition() { return hasTrophallacticOvaryInhibition; }
    public void setHasTrophallacticOvaryInhibition(boolean val) { this.hasTrophallacticOvaryInhibition = val; }

    @Override public boolean canPerformDroughtVibratoDance() { return canPerformDroughtVibratoDance; }
    public void setCanPerformDroughtVibratoDance(boolean val) { this.canPerformDroughtVibratoDance = val; }

    @Override public boolean canEncapsulateLargeIntrudersClay() { return canEncapsulateLargeIntrudersClay; }
    public void setCanEncapsulateLargeIntrudersClay(boolean val) { this.canEncapsulateLargeIntrudersClay = val; }

    @Override public boolean canConstructPhonicIsolationChambers() { return canConstructPhonicIsolationChambers; }
    public void setCanConstructPhonicIsolationChambers(boolean val) { this.canConstructPhonicIsolationChambers = val; }

    @Override public boolean canApplyHydrophobicTrailCoating() { return canApplyHydrophobicTrailCoating; }
    public void setCanApplyHydrophobicTrailCoating(boolean val) { this.canApplyHydrophobicTrailCoating = val; }

    @Override public boolean canConsumeFermentedSapAnesthetic() { return canConsumeFermentedSapAnesthetic; }
    public void setCanConsumeFermentedSapAnesthetic(boolean val) { this.canConsumeFermentedSapAnesthetic = val; }

    @Override public boolean canPerformRelaySeedTransport() { return canPerformRelaySeedTransport; }
    public void setCanPerformRelaySeedTransport(boolean val) { this.canPerformRelaySeedTransport = val; }

    @Override public boolean hasPreySizeSelectivePheromones() { return hasPreySizeSelectivePheromones; }
    public void setHasPreySizeSelectivePheromones(boolean val) { this.hasPreySizeSelectivePheromones = val; }

    @Override public boolean canDryLarvaeWoodDust() { return canDryLarvaeWoodDust; }
    public void setCanDryLarvaeWoodDust(boolean val) { this.canDryLarvaeWoodDust = val; }

    @Override public boolean canPerformFanoutEscapeFormicAcid() { return canPerformFanoutEscapeFormicAcid; }
    public void setCanPerformFanoutEscapeFormicAcid(boolean val) { this.canPerformFanoutEscapeFormicAcid = val; }

    @Override public boolean canEmitMoundOverheatVibrato() { return canEmitMoundOverheatVibrato; }
    public void setCanEmitMoundOverheatVibrato(boolean val) { this.canEmitMoundOverheatVibrato = val; }

    @Override public boolean canNourishVirginQueensPreFlight() { return canNourishVirginQueensPreFlight; }
    public void setCanNourishVirginQueensPreFlight(boolean val) { this.canNourishVirginQueensPreFlight = val; }

    @Override public boolean canPlugHoneyStoresBricks() { return canPlugHoneyStoresBricks; }
    public void setCanPlugHoneyStoresBricks(boolean val) { this.canPlugHoneyStoresBricks = val; }

    @Override public boolean canHuntNocturnalInfrared() { return canHuntNocturnalInfrared; }
    public void setCanHuntNocturnalInfrared(boolean val) { this.canHuntNocturnalInfrared = val; }

    @Override public boolean canWeaveLarvalSilkCanopyBridges() { return canWeaveLarvalSilkCanopyBridges; }
    public void setCanWeaveLarvalSilkCanopyBridges(boolean val) { this.canWeaveLarvalSilkCanopyBridges = val; }

    @Override public boolean canStridulateEggLayingSynchronization() { return canStridulateEggLayingSynchronization; }
    public void setCanStridulateEggLayingSynchronization(boolean val) { this.canStridulateEggLayingSynchronization = val; }

    @Override public boolean canPerformAntennalDustGrooming() { return canPerformAntennalDustGrooming; }
    public void setCanPerformAntennalDustGrooming(boolean val) { this.canPerformAntennalDustGrooming = val; }

    @Override public boolean canForageSaltCrystalsOsmoregulation() { return canForageSaltCrystalsOsmoregulation; }
    public void setCanForageSaltCrystalsOsmoregulation(boolean val) { this.canForageSaltCrystalsOsmoregulation = val; }

    @Override public boolean canConstructRainEvacuationSiphons() { return canConstructRainEvacuationSiphons; }
    public void setCanConstructRainEvacuationSiphons(boolean val) { this.canConstructRainEvacuationSiphons = val; }

    @Override public boolean canAbsorbHostPlantChemicalCamouflage() { return canAbsorbHostPlantChemicalCamouflage; }
    public void setCanAbsorbHostPlantChemicalCamouflage(boolean val) { this.canAbsorbHostPlantChemicalCamouflage = val; }

    @Override public boolean canDepositSulfurDustAntiMitePatrol() { return canDepositSulfurDustAntiMitePatrol; }
    public void setCanDepositSulfurDustAntiMitePatrol(boolean val) { this.canDepositSulfurDustAntiMitePatrol = val; }

    @Override public boolean canDanceVibratoHatchingEnthusiasm() { return canDanceVibratoHatchingEnthusiasm; }
    public void setCanDanceVibratoHatchingEnthusiasm(boolean val) { this.canDanceVibratoHatchingEnthusiasm = val; }

    @Override public boolean canResinMummifyNymphalChambers() { return canResinMummifyNymphalChambers; }
    public void setCanResinMummifyNymphalChambers(boolean val) { this.canResinMummifyNymphalChambers = val; }

    @Override public boolean canExcavatePitfallTraps() { return canExcavatePitfallTraps; }
    public void setCanExcavatePitfallTraps(boolean val) { this.canExcavatePitfallTraps = val; }

    @Override public boolean canSynthesizeGlycerolCryoprotection() { return canSynthesizeGlycerolCryoprotection; }
    public void setCanSynthesizeGlycerolCryoprotection(boolean val) { this.canSynthesizeGlycerolCryoprotection = val; }

    @Override public boolean canTransportInjuredPheromonalStretcher() { return canTransportInjuredPheromonalStretcher; }
    public void setCanTransportInjuredPheromonalStretcher(boolean val) { this.canTransportInjuredPheromonalStretcher = val; }

    @Override public boolean canRaidAbandonedWaxVaults() { return canRaidAbandonedWaxVaults; }
    public void setCanRaidAbandonedWaxVaults(boolean val) { this.canRaidAbandonedWaxVaults = val; }

    @Override public boolean canPerformRitualMandibularWrestling() { return canPerformRitualMandibularWrestling; }
    public void setCanPerformRitualMandibularWrestling(boolean val) { this.canPerformRitualMandibularWrestling = val; }

    @Override public boolean canPerformPulsedAirConvectiveVentilation() { return canPerformPulsedAirConvectiveVentilation; }
    public void setCanPerformPulsedAirConvectiveVentilation(boolean val) { this.canPerformPulsedAirConvectiveVentilation = val; }

    @Override public boolean canCultivateStreptomycesAntibiotics() { return canCultivateStreptomycesAntibiotics; }
    public void setCanCultivateStreptomycesAntibiotics(boolean val) { this.canCultivateStreptomycesAntibiotics = val; }

    @Override public boolean canNavigatePolarizedTwilightUV() { return canNavigatePolarizedTwilightUV; }
    public void setCanNavigatePolarizedTwilightUV(boolean val) { this.canNavigatePolarizedTwilightUV = val; }

    @Override public boolean canSnapTrapMandiblesCatapult() { return canSnapTrapMandiblesCatapult; }
    public void setCanSnapTrapMandiblesCatapult(boolean val) { this.canSnapTrapMandiblesCatapult = val; }

    @Override public boolean canPerformPedestrianSwarmBudding() { return canPerformPedestrianSwarmBudding; }
    public void setCanPerformPedestrianSwarmBudding(boolean val) { this.canPerformPedestrianSwarmBudding = val; }

    @Override public boolean canTrophallaxisProtozoa() { return canTrophallaxisProtozoa; }
    public void setCanTrophallaxisProtozoa(boolean val) { this.canTrophallaxisProtozoa = val; }

    @Override public boolean canSquirtNasuteChemical() { return canSquirtNasuteChemical; }
    public void setCanSquirtNasuteChemical(boolean val) { this.canSquirtNasuteChemical = val; }

    @Override public boolean canMasticatePaperPulpCarton() { return canMasticatePaperPulpCarton; }
    public void setCanMasticatePaperPulpCarton(boolean val) { this.canMasticatePaperPulpCarton = val; }

    @Override public boolean canHarvestLarvalSalivaDroplets() { return canHarvestLarvalSalivaDroplets; }
    public void setCanHarvestLarvalSalivaDroplets(boolean val) { this.canHarvestLarvalSalivaDroplets = val; }

    @Override public boolean canApplyPedicelAntRepellent() { return canApplyPedicelAntRepellent; }
    public void setCanApplyPedicelAntRepellent(boolean val) { this.canApplyPedicelAntRepellent = val; }

    @Override public boolean canRecognizeFacialVisualPatterns() { return canRecognizeFacialVisualPatterns; }
    public void setCanRecognizeFacialVisualPatterns(boolean val) { this.canRecognizeFacialVisualPatterns = val; }

    @Override public boolean canPerformBuzzPollination() { return canPerformBuzzPollination; }
    public void setCanPerformBuzzPollination(boolean val) { this.canPerformBuzzPollination = val; }

    @Override public boolean canIncubateBroodAbdominalHeat() { return canIncubateBroodAbdominalHeat; }
    public void setCanIncubateBroodAbdominalHeat(boolean val) { this.canIncubateBroodAbdominalHeat = val; }

    @Override public boolean canStabFrontalHornsAphid() { return canStabFrontalHornsAphid; }
    public void setCanStabFrontalHornsAphid(boolean val) { this.canStabFrontalHornsAphid = val; }

    @Override public boolean canSqueezeGallIntrudersThrips() { return canSqueezeGallIntrudersThrips; }
    public void setCanSqueezeGallIntrudersThrips(boolean val) { this.canSqueezeGallIntrudersThrips = val; }

    @Override public boolean canSnapClawAcousticShockwave() { return canSnapClawAcousticShockwave; }
    public void setCanSnapClawAcousticShockwave(boolean val) { this.canSnapClawAcousticShockwave = val; }

    @Override public boolean canStridulatePassalidParentalCare() { return canStridulatePassalidParentalCare; }
    public void setCanStridulatePassalidParentalCare(boolean val) { this.canStridulatePassalidParentalCare = val; }

    @Override public boolean canPerformPhysogastricPeristalsis() { return canPerformPhysogastricPeristalsis; }
    public void setCanPerformPhysogastricPeristalsis(boolean val) { this.canPerformPhysogastricPeristalsis = val; }

    @Override public boolean canOrientMagneticMound() { return canOrientMagneticMound; }
    public void setCanOrientMagneticMound(boolean val) { this.canOrientMagneticMound = val; }

    @Override public boolean canEmitHornetGroupAlarmPheromone() { return canEmitHornetGroupAlarmPheromone; }
    public void setCanEmitHornetGroupAlarmPheromone(boolean val) { this.canEmitHornetGroupAlarmPheromone = val; }

    @Override public boolean canWeaveStenogastrinePaperJelly() { return canWeaveStenogastrinePaperJelly; }
    public void setCanWeaveStenogastrinePaperJelly(boolean val) { this.canWeaveStenogastrinePaperJelly = val; }

    @Override public boolean canInoculateFungalCombTermite() { return canInoculateFungalCombTermite; }
    public void setCanInoculateFungalCombTermite(boolean val) { this.canInoculateFungalCombTermite = val; }

    @Override public boolean canDrumAbdomenWaspCellRim() { return canDrumAbdomenWaspCellRim; }
    public void setCanDrumAbdomenWaspCellRim(boolean val) { this.canDrumAbdomenWaspCellRim = val; }

    @Override public boolean canConstructNectarWaxPots() { return canConstructNectarWaxPots; }
    public void setCanConstructNectarWaxPots(boolean val) { this.canConstructNectarWaxPots = val; }

    @Override public boolean canPerformMaternalShieldGuarding() { return canPerformMaternalShieldGuarding; }
    public void setCanPerformMaternalShieldGuarding(boolean val) { this.canPerformMaternalShieldGuarding = val; }

    @Override public boolean canWeaveCommunalSpiderSilk() { return canWeaveCommunalSpiderSilk; }
    public void setCanWeaveCommunalSpiderSilk(boolean val) { this.canWeaveCommunalSpiderSilk = val; }

    @Override public boolean canFormProcessionarySilkTrail() { return canFormProcessionarySilkTrail; }
    public void setCanFormProcessionarySilkTrail(boolean val) { this.canFormProcessionarySilkTrail = val; }

    @Override public boolean canConstructClayVaultArches() { return canConstructClayVaultArches; }
    public void setCanConstructClayVaultArches(boolean val) { this.canConstructClayVaultArches = val; }

    @Override public boolean canDeliverStenogastrinePapFood() { return canDeliverStenogastrinePapFood; }
    public void setCanDeliverStenogastrinePapFood(boolean val) { this.canDeliverStenogastrinePapFood = val; }

    @Override public boolean canPlasterFrassGalleryWalls() { return canPlasterFrassGalleryWalls; }
    public void setCanPlasterFrassGalleryWalls(boolean val) { this.canPlasterFrassGalleryWalls = val; }

    @Override public boolean canLearnTrapliningFlightRoutes() { return canLearnTrapliningFlightRoutes; }
    public void setCanLearnTrapliningFlightRoutes(boolean val) { this.canLearnTrapliningFlightRoutes = val; }

    @Override public boolean canCoolNestWaterRegurgitation() { return canCoolNestWaterRegurgitation; }
    public void setCanCoolNestWaterRegurgitation(boolean val) { this.canCoolNestWaterRegurgitation = val; }

    @Override public boolean canEjectHoneydewSignalingDroplets() { return canEjectHoneydewSignalingDroplets; }
    public void setCanEjectHoneydewSignalingDroplets(boolean val) { this.canEjectHoneydewSignalingDroplets = val; }

    @Override public boolean canSnapMandibleAcousticAlarm() { return canSnapMandibleAcousticAlarm; }
    public void setCanSnapMandibleAcousticAlarm(boolean val) { this.canSnapMandibleAcousticAlarm = val; }

    @Override public boolean canPerformEggLickingGrooming() { return canPerformEggLickingGrooming; }
    public void setCanPerformEggLickingGrooming(boolean val) { this.canPerformEggLickingGrooming = val; }

    @Override public boolean canConstructChaffGarbageDunes() { return canConstructChaffGarbageDunes; }
    public void setCanConstructChaffGarbageDunes(boolean val) { this.canConstructChaffGarbageDunes = val; }

    @Override public boolean canDrumAntennaeLarvalStimulation() { return canDrumAntennaeLarvalStimulation; }
    public void setCanDrumAntennaeLarvalStimulation(boolean val) { this.canDrumAntennaeLarvalStimulation = val; }

    @Override public boolean canFormLeafPullingChains() { return canFormLeafPullingChains; }
    public void setCanFormLeafPullingChains(boolean val) { this.canFormLeafPullingChains = val; }

    @Override public boolean canApplySalivaryCementMoistureSeal() { return canApplySalivaryCementMoistureSeal; }
    public void setCanApplySalivaryCementMoistureSeal(boolean val) { this.canApplySalivaryCementMoistureSeal = val; }

    @Override public boolean canForageSubZeroBumblebee() { return canForageSubZeroBumblebee; }
    public void setCanForageSubZeroBumblebee(boolean val) { this.canForageSubZeroBumblebee = val; }

    @Override public boolean canRepairGallSubstratalSecretion() { return canRepairGallSubstratalSecretion; }
    public void setCanRepairGallSubstratalSecretion(boolean val) { this.canRepairGallSubstratalSecretion = val; }

    @Override public boolean canTrophallaxisPassalidWoodFrass() { return canTrophallaxisPassalidWoodFrass; }
    public void setCanTrophallaxisPassalidWoodFrass(boolean val) { this.canTrophallaxisPassalidWoodFrass = val; }

    @Override public boolean canPerformCrecheRegurgitationSpider() { return canPerformCrecheRegurgitationSpider; }
    public void setCanPerformCrecheRegurgitationSpider(boolean val) { this.canPerformCrecheRegurgitationSpider = val; }

    @Override public boolean canBlockRoyalChamberSentry() { return canBlockRoyalChamberSentry; }
    public void setCanBlockRoyalChamberSentry(boolean val) { this.canBlockRoyalChamberSentry = val; }

    @Override public boolean canEmitParentBugAlarmGathering() { return canEmitParentBugAlarmGathering; }
    public void setCanEmitParentBugAlarmGathering(boolean val) { this.canEmitParentBugAlarmGathering = val; }

    @Override public boolean canApplyBeeBreadHydrophobicCoating() { return canApplyBeeBreadHydrophobicCoating; }
    public void setCanApplyBeeBreadHydrophobicCoating(boolean val) { this.canApplyBeeBreadHydrophobicCoating = val; }

    @Override public boolean canBindParasitesWithSilk() { return canBindParasitesWithSilk; }
    public void setCanBindParasitesWithSilk(boolean val) { this.canBindParasitesWithSilk = val; }

    @Override public boolean canEmitSubstrateObstacleVibrato() { return canEmitSubstrateObstacleVibrato; }
    public void setCanEmitSubstrateObstacleVibrato(boolean val) { this.canEmitSubstrateObstacleVibrato = val; }

    @Override public boolean canPerformFormicAcidBathGrooming() { return canPerformFormicAcidBathGrooming; }
    public void setCanPerformFormicAcidBathGrooming(boolean val) { this.canPerformFormicAcidBathGrooming = val; }

    @Override public boolean canExcavateVerticalDrainageShafts() { return canExcavateVerticalDrainageShafts; }
    public void setCanExcavateVerticalDrainageShafts(boolean val) { this.canExcavateVerticalDrainageShafts = val; }

    @Override public boolean canIngestPhenolicResinMedication() { return canIngestPhenolicResinMedication; }
    public void setCanIngestPhenolicResinMedication(boolean val) { this.canIngestPhenolicResinMedication = val; }

    @Override public boolean canConstructSphagnumMoistureDomes() { return canConstructSphagnumMoistureDomes; }
    public void setCanConstructSphagnumMoistureDomes(boolean val) { this.canConstructSphagnumMoistureDomes = val; }

    @Override public boolean canMarkParasitizedCadaverRepellent() { return canMarkParasitizedCadaverRepellent; }
    public void setCanMarkParasitizedCadaverRepellent(boolean val) { this.canMarkParasitizedCadaverRepellent = val; }

    @Override public boolean canDrumNuptialFlightSynchronization() { return canDrumNuptialFlightSynchronization; }
    public void setCanDrumNuptialFlightSynchronization(boolean val) { this.canDrumNuptialFlightSynchronization = val; }

    @Override public boolean canHarvestCuticularWaterCondensation() { return canHarvestCuticularWaterCondensation; }
    public void setCanHarvestCuticularWaterCondensation(boolean val) { this.canHarvestCuticularWaterCondensation = val; }

    @Override public boolean canStridulateLarvalHungerChirp() { return canStridulateLarvalHungerChirp; }
    public void setCanStridulateLarvalHungerChirp(boolean val) { this.canStridulateLarvalHungerChirp = val; }

    @Override public boolean canConstructThermalChimneyFlues() { return canConstructThermalChimneyFlues; }
    public void setCanConstructThermalChimneyFlues(boolean val) { this.canConstructThermalChimneyFlues = val; }

    @Override public boolean canDepositLarvalFoodSalivaDrop() { return canDepositLarvalFoodSalivaDrop; }
    public void setCanDepositLarvalFoodSalivaDrop(boolean val) { this.canDepositLarvalFoodSalivaDrop = val; }

    @Override public boolean canApplyEggMassMucilageEnvelope() { return canApplyEggMassMucilageEnvelope; }
    public void setCanApplyEggMassMucilageEnvelope(boolean val) { this.canApplyEggMassMucilageEnvelope = val; }

    @Override public boolean canWeaveSilkPavilionAphidShelter() { return canWeaveSilkPavilionAphidShelter; }
    public void setCanWeaveSilkPavilionAphidShelter(boolean val) { this.canWeaveSilkPavilionAphidShelter = val; }

    @Override public boolean canFormHotBallThermalDefense() { return canFormHotBallThermalDefense; }
    public void setCanFormHotBallThermalDefense(boolean val) { this.canFormHotBallThermalDefense = val; }

    @Override public boolean canPerformFontanelleAutothysis() { return canPerformFontanelleAutothysis; }
    public void setCanPerformFontanelleAutothysis(boolean val) { this.canPerformFontanelleAutothysis = val; }

    @Override public boolean canSensePreySignalWireTripping() { return canSensePreySignalWireTripping; }
    public void setCanSensePreySignalWireTripping(boolean val) { this.canSensePreySignalWireTripping = val; }

    @Override public boolean canMutilateSeedRadicles() { return canMutilateSeedRadicles; }
    public void setCanMutilateSeedRadicles(boolean val) { this.canMutilateSeedRadicles = val; }

    @Override public boolean canBiteNectarTheftHoles() { return canBiteNectarTheftHoles; }
    public void setCanBiteNectarTheftHoles(boolean val) { this.canBiteNectarTheftHoles = val; }

    @Override public boolean canSowFungalSporeCombs() { return canSowFungalSporeCombs; }
    public void setCanSowFungalSporeCombs(boolean val) { this.canSowFungalSporeCombs = val; }

    @Override public boolean canHarnessLarvalSilkCocoon() { return canHarnessLarvalSilkCocoon; }
    public void setCanHarnessLarvalSilkCocoon(boolean val) { this.canHarnessLarvalSilkCocoon = val; }

    @Override public boolean canFormBiomechanicalBivouac() { return canFormBiomechanicalBivouac; }
    public void setCanFormBiomechanicalBivouac(boolean val) { this.canFormBiomechanicalBivouac = val; }

    @Override public boolean canPerformBuzzPollinationSonication() { return canPerformBuzzPollinationSonication; }
    public void setCanPerformBuzzPollinationSonication(boolean val) { this.canPerformBuzzPollinationSonication = val; }

    @Override public boolean canRegurgitateEarwigMaternalFood() { return canRegurgitateEarwigMaternalFood; }
    public void setCanRegurgitateEarwigMaternalFood(boolean val) { this.canRegurgitateEarwigMaternalFood = val; }

    @Override public boolean canRecognizeWaspFacialPatterns() { return canRecognizeWaspFacialPatterns; }
    public void setCanRecognizeWaspFacialPatterns(boolean val) { this.canRecognizeWaspFacialPatterns = val; }

    @Override public boolean canFireShrimpAcousticCannon() { return canFireShrimpAcousticCannon; }
    public void setCanFireShrimpAcousticCannon(boolean val) { this.canFireShrimpAcousticCannon = val; }

    @Override public boolean canDuetPassalidSubstrateVibration() { return canDuetPassalidSubstrateVibration; }
    public void setCanDuetPassalidSubstrateVibration(boolean val) { this.canDuetPassalidSubstrateVibration = val; }

    @Override public boolean canTurnGranarySeedsAeration() { return canTurnGranarySeedsAeration; }
    public void setCanTurnGranarySeedsAeration(boolean val) { this.canTurnGranarySeedsAeration = val; }

    @Override public boolean canEncodeWaggleDanceSunCompass() { return canEncodeWaggleDanceSunCompass; }
    public void setCanEncodeWaggleDanceSunCompass(boolean val) { this.canEncodeWaggleDanceSunCompass = val; }

    @Override public boolean canDigSubterraneanClayAqueducts() { return canDigSubterraneanClayAqueducts; }
    public void setCanDigSubterraneanClayAqueducts(boolean val) { this.canDigSubterraneanClayAqueducts = val; }

    @Override public boolean canFireFormicAcidArtilleryJet() { return canFireFormicAcidArtilleryJet; }
    public void setCanFireFormicAcidArtilleryJet(boolean val) { this.canFireFormicAcidArtilleryJet = val; }

    @Override public boolean canEjectGarbageChuteRefuse() { return canEjectGarbageChuteRefuse; }
    public void setCanEjectGarbageChuteRefuse(boolean val) { this.canEjectGarbageChuteRefuse = val; }

    @Override public boolean canFanWingsForBroodThermoregulation() { return canFanWingsForBroodThermoregulation; }
    public void setCanFanWingsForBroodThermoregulation(boolean val) { this.canFanWingsForBroodThermoregulation = val; }

    @Override public boolean canPlugGallWithChitinousTube() { return canPlugGallWithChitinousTube; }
    public void setCanPlugGallWithChitinousTube(boolean val) { this.canPlugGallWithChitinousTube = val; }

    @Override public boolean canCoatWaspPedicelAntRepellent() { return canCoatWaspPedicelAntRepellent; }
    public void setCanCoatWaspPedicelAntRepellent(boolean val) { this.canCoatWaspPedicelAntRepellent = val; }

    @Override public boolean canSquirtNasuteViscousResin() { return canSquirtNasuteViscousResin; }
    public void setCanSquirtNasuteViscousResin(boolean val) { this.canSquirtNasuteViscousResin = val; }

    @Override public boolean canSqueezeIntrudersWithForelegs() { return canSqueezeIntrudersWithForelegs; }
    public void setCanSqueezeIntrudersWithForelegs(boolean val) { this.canSqueezeIntrudersWithForelegs = val; }

    @Override public boolean canShieldEggsFromParasitoidWasps() { return canShieldEggsFromParasitoidWasps; }
    public void setCanShieldEggsFromParasitoidWasps(boolean val) { this.canShieldEggsFromParasitoidWasps = val; }

    @Override public boolean canPlasterWoodWallGallery() { return canPlasterWoodWallGallery; }
    public void setCanPlasterWoodWallGallery(boolean val) { this.canPlasterWoodWallGallery = val; }

    @Override public boolean canShearLeafCrescentMandible() { return canShearLeafCrescentMandible; }
    public void setCanShearLeafCrescentMandible(boolean val) { this.canShearLeafCrescentMandible = val; }

    @Override public boolean canShieldSwarmCoreHeat() { return canShieldSwarmCoreHeat; }
    public void setCanShieldSwarmCoreHeat(boolean val) { this.canShieldSwarmCoreHeat = val; }

    @Override public boolean canPerformQueenPhysogastricPeristalsis() { return canPerformQueenPhysogastricPeristalsis; }
    public void setCanPerformQueenPhysogastricPeristalsis(boolean val) { this.canPerformQueenPhysogastricPeristalsis = val; }

    @Override public boolean canWeaveSocialSilkHammock() { return canWeaveSocialSilkHammock; }
    public void setCanWeaveSocialSilkHammock(boolean val) { this.canWeaveSocialSilkHammock = val; }

    @Override public boolean canPackCorbiculaPollenBaskets() { return canPackCorbiculaPollenBaskets; }
    public void setCanPackCorbiculaPollenBaskets(boolean val) { this.canPackCorbiculaPollenBaskets = val; }

    @Override public boolean canScrapeWoodPulpCarton() { return canScrapeWoodPulpCarton; }
    public void setCanScrapeWoodPulpCarton(boolean val) { this.canScrapeWoodPulpCarton = val; }

    @Override public boolean canLayTrophicNourishmentEggs() { return canLayTrophicNourishmentEggs; }
    public void setCanLayTrophicNourishmentEggs(boolean val) { this.canLayTrophicNourishmentEggs = val; }

    @Override public boolean canNavigatePolarizedLightCompass() { return canNavigatePolarizedLightCompass; }
    public void setCanNavigatePolarizedLightCompass(boolean val) { this.canNavigatePolarizedLightCompass = val; }

    @Override public boolean canBuryFungalWasteInGallery() { return canBuryFungalWasteInGallery; }
    public void setCanBuryFungalWasteInGallery(boolean val) { this.canBuryFungalWasteInGallery = val; }

    @Override public boolean canChewSeedHuskBreadPulp() { return canChewSeedHuskBreadPulp; }
    public void setCanChewSeedHuskBreadPulp(boolean val) { this.canChewSeedHuskBreadPulp = val; }

    @Override public boolean canWrapPreyInCommunalSilk() { return canWrapPreyInCommunalSilk; }
    public void setCanWrapPreyInCommunalSilk(boolean val) { this.canWrapPreyInCommunalSilk = val; }

    @Override public boolean canSealNestGapsWithPropolis() { return canSealNestGapsWithPropolis; }
    public void setCanSealNestGapsWithPropolis(boolean val) { this.canSealNestGapsWithPropolis = val; }

    @Override public boolean canSynchronizeSoldierAlarmDrumming() { return canSynchronizeSoldierAlarmDrumming; }
    public void setCanSynchronizeSoldierAlarmDrumming(boolean val) { this.canSynchronizeSoldierAlarmDrumming = val; }

    @Override public boolean canPerformDominanceMounting() { return canPerformDominanceMounting; }
    public void setCanPerformDominanceMounting(boolean val) { this.canPerformDominanceMounting = val; }

    @Override public boolean canLapNectarTongueExtension() { return canLapNectarTongueExtension; }
    public void setCanLapNectarTongueExtension(boolean val) { this.canLapNectarTongueExtension = val; }

    @Override public boolean canSecreteGallClosingFluid() { return canSecreteGallClosingFluid; }
    public void setCanSecreteGallClosingFluid(boolean val) { this.canSecreteGallClosingFluid = val; }

    @Override public boolean canGroomNymphCuticularSurface() { return canGroomNymphCuticularSurface; }
    public void setCanGroomNymphCuticularSurface(boolean val) { this.canGroomNymphCuticularSurface = val; }

    @Override public boolean canExcavateGardenWasteChambers() { return canExcavateGardenWasteChambers; }
    public void setCanExcavateGardenWasteChambers(boolean val) { this.canExcavateGardenWasteChambers = val; }

    @Override public boolean canExchangeRoyalPairGrooming() { return canExchangeRoyalPairGrooming; }
    public void setCanExchangeRoyalPairGrooming(boolean val) { this.canExchangeRoyalPairGrooming = val; }

    @Override public boolean canTriggerUniversalEmergencyEvacuation() { return canTriggerUniversalEmergencyEvacuation; }
    public void setCanTriggerUniversalEmergencyEvacuation(boolean val) { this.canTriggerUniversalEmergencyEvacuation = val; capabilitiesBitSet.set(200, val); }

    // Batch XVII accessors (201-220)
    @Override public boolean canStoreNectarAsHoneypotReplete() { return canStoreNectarAsHoneypotReplete; }
    public void setCanStoreNectarAsHoneypotReplete(boolean val) { this.canStoreNectarAsHoneypotReplete = val; capabilitiesBitSet.set(201, val); }

    @Override public boolean canFormFloatingAntRaft() { return canFormFloatingAntRaft; }
    public void setCanFormFloatingAntRaft(boolean val) { this.canFormFloatingAntRaft = val; capabilitiesBitSet.set(202, val); }

    @Override public boolean canConstructMudResinEntranceFunnel() { return canConstructMudResinEntranceFunnel; }
    public void setCanConstructMudResinEntranceFunnel(boolean val) { this.canConstructMudResinEntranceFunnel = val; capabilitiesBitSet.set(203, val); }

    @Override public boolean canExcavateHibernationBurrow() { return canExcavateHibernationBurrow; }
    public void setCanExcavateHibernationBurrow(boolean val) { this.canExcavateHibernationBurrow = val; capabilitiesBitSet.set(204, val); }

    @Override public boolean canInoculateLeafPulpEnzymes() { return canInoculateLeafPulpEnzymes; }
    public void setCanInoculateLeafPulpEnzymes(boolean val) { this.canInoculateLeafPulpEnzymes = val; capabilitiesBitSet.set(205, val); }

    @Override public boolean canPerformGamergateDominanceTournament() { return canPerformGamergateDominanceTournament; }
    public void setCanPerformGamergateDominanceTournament(boolean val) { this.canPerformGamergateDominanceTournament = val; capabilitiesBitSet.set(206, val); }

    @Override public boolean canFeedOnLarvalHemolymphDracula() { return canFeedOnLarvalHemolymphDracula; }
    public void setCanFeedOnLarvalHemolymphDracula(boolean val) { this.canFeedOnLarvalHemolymphDracula = val; capabilitiesBitSet.set(207, val); }

    @Override public boolean canFormTarsalFrictionBridge() { return canFormTarsalFrictionBridge; }
    public void setCanFormTarsalFrictionBridge(boolean val) { this.canFormTarsalFrictionBridge = val; capabilitiesBitSet.set(208, val); }

    @Override public boolean canTransportWaterInMandibleDroplet() { return canTransportWaterInMandibleDroplet; }
    public void setCanTransportWaterInMandibleDroplet(boolean val) { this.canTransportWaterInMandibleDroplet = val; capabilitiesBitSet.set(209, val); }

    @Override public boolean canStiltWalkThermalRegim() { return canStiltWalkThermalRegim; }
    public void setCanStiltWalkThermalRegim(boolean val) { this.canStiltWalkThermalRegim = val; capabilitiesBitSet.set(210, val); }

    @Override public boolean canPerformAntiPredatorShimmeringWave() { return canPerformAntiPredatorShimmeringWave; }
    public void setCanPerformAntiPredatorShimmeringWave(boolean val) { this.canPerformAntiPredatorShimmeringWave = val; capabilitiesBitSet.set(211, val); }

    @Override public boolean canDouseNestWaterCooling() { return canDouseNestWaterCooling; }
    public void setCanDouseNestWaterCooling(boolean val) { this.canDouseNestWaterCooling = val; capabilitiesBitSet.set(212, val); }

    @Override public boolean canAerateFungalCombChambers() { return canAerateFungalCombChambers; }
    public void setCanAerateFungalCombChambers(boolean val) { this.canAerateFungalCombChambers = val; capabilitiesBitSet.set(213, val); }

    @Override public boolean canFeedLarvaeExuviaRecycling() { return canFeedLarvaeExuviaRecycling; }
    public void setCanFeedLarvaeExuviaRecycling(boolean val) { this.canFeedLarvaeExuviaRecycling = val; capabilitiesBitSet.set(214, val); }

    @Override public boolean canGroomLeafPulpParasitesMinim() { return canGroomLeafPulpParasitesMinim; }
    public void setCanGroomLeafPulpParasitesMinim(boolean val) { this.canGroomLeafPulpParasitesMinim = val; capabilitiesBitSet.set(215, val); }

    @Override public boolean canCamouflageWebWithPlantDebris() { return canCamouflageWebWithPlantDebris; }
    public void setCanCamouflageWebWithPlantDebris(boolean val) { this.canCamouflageWebWithPlantDebris = val; capabilitiesBitSet.set(216, val); }

    @Override public boolean canCockGasterFormicAcidRepellent() { return canCockGasterFormicAcidRepellent; }
    public void setCanCockGasterFormicAcidRepellent(boolean val) { this.canCockGasterFormicAcidRepellent = val; capabilitiesBitSet.set(217, val); }

    @Override public boolean canMilkAphidHoneydewStroking() { return canMilkAphidHoneydewStroking; }
    public void setCanMilkAphidHoneydewStroking(boolean val) { this.canMilkAphidHoneydewStroking = val; capabilitiesBitSet.set(218, val); }

    @Override public boolean canClusterSolarHeatCollector() { return canClusterSolarHeatCollector; }
    public void setCanClusterSolarHeatCollector(boolean val) { this.canClusterSolarHeatCollector = val; capabilitiesBitSet.set(219, val); }

    @Override public boolean canSerializeGlobalEthologicalBitSet() { return canSerializeGlobalEthologicalBitSet; }
    public void setCanSerializeGlobalEthologicalBitSet(boolean val) { this.canSerializeGlobalEthologicalBitSet = val; capabilitiesBitSet.set(220, val); }

    // ── Academic Ethological & Biological Aliases (Academic Rigor & Scientific Nomenclature) ──
    @Override public boolean canPerformLarvalHemolymphophily() { return canFeedOnLarvalHemolymphDracula; }
    public void setCanPerformLarvalHemolymphophily(boolean val) { setCanFeedOnLarvalHemolymphDracula(val); }

    @Override public boolean canPerformEclosionVibratoryStimulation() { return canDanceVibratoHatchingEnthusiasm; }
    public void setCanPerformEclosionVibratoryStimulation(boolean val) { setCanDanceVibratoHatchingEnthusiasm(val); }

    @Override public boolean canPerformSubharmonicSentinelStridulation() { return canPerformGuardShiftVibrationalWhisper; }
    public void setCanPerformSubharmonicSentinelStridulation(boolean val) { setCanPerformGuardShiftVibrationalWhisper(val); }

    @Override public boolean canExtendProboscisForNectar() { return canLapNectarTongueExtension; }
    public void setCanExtendProboscisForNectar(boolean val) { setCanLapNectarTongueExtension(val); }

    @Override public boolean canSealCellsWithOpercula() { return canPlugHoneyStoresBricks; }
    public void setCanSealCellsWithOpercula(boolean val) { setCanPlugHoneyStoresBricks(val); }

    @Override public boolean canConstructMiddenRefusePiles() { return canConstructChaffGarbageDunes; }
    public void setCanConstructMiddenRefusePiles(boolean val) { setCanConstructChaffGarbageDunes(val); }

    @Override public boolean canSecreteAbdominalGelSupport() { return canWeaveStenogastrinePaperJelly; }
    public void setCanSecreteAbdominalGelSupport(boolean val) { setCanWeaveStenogastrinePaperJelly(val); }

    @Override public boolean canDeliverSalivaryLarvalPap() { return canDeliverStenogastrinePapFood; }

    @Override
    public BitSet getCapabilitiesBitSet() {
        return capabilitiesBitSet;
    }
}
