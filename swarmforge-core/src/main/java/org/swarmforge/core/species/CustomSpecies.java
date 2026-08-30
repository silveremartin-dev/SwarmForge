/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.swarmforge.core.domain.CasteTemplate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * A comprehensive, customizable species definition supporting realistic eusocial insects
 * (ants, bees, wasps, termites, etc.).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSpecies implements Species {

    private String presetName = "Default Species";
    private String scientificName = "Formica genericus";
    private String genus = "Formica";
    private String commonName = "Generic Ant";
    private String description = "Generic eusocial species for simulation.";
    private String insectType = "ANT"; // ANT, BEE, WASP, TERMITE, OTHER
    private SpeciesCategory category = SpeciesCategory.EUSOCIAL_PRIMARY; // EUSOCIAL_PRIMARY, COMMENSAL, HONEYDEW_PRODUCER, PREY_ORGANISM, SOIL_FAUNA, PARASITE_PREDATOR

    // --- Colony & Queens (Aspect CRITIQUE) ---
    private String queenCountMode = "MONOGYNE"; // MONOGYNE, POLYGYNE, GAMERGATES
    private int queenCount = 1;
    private int queenLifespan = 25000;
    private boolean hasKing = false; // For termites
    private int kingLifespan = 15000;
    private float queenEggLayingRate = 15.0f; // eggs/day or tick unit
    private String nuptialFlightType = "AERIAL_SWARM"; // AERIAL_SWARM, SWARM_DIVISION, BUDDING, IN_NEST

    // --- Life Stages & Caste Transition Matrix Parameters ---
    private int eggStageDuration = 300;
    private int larvaStageDuration = 600;
    private int pupaStageDuration = 500;
    private String larvaDietRequirement = "HIGH_PROTEIN_MEAT"; // HIGH_PROTEIN_MEAT, SUGAR_HONEY, FUNGUS, CELLULOSE, OMNIVORE
    private float proteinThresholdMinor = 0.35f;
    private float proteinThresholdMajor = 0.70f;
    private float proteinThresholdSoldier = 0.85f;
    private float proteinThresholdQueen = 0.95f;
    private float queenPheromoneInhibitionFactor = 0.80f;
    private boolean haplodiploidyEnabled = true;
    private float pathogenResistance = 0.50f;
    private float groomingDefenseEfficacy = 0.70f;

    // --- Worker Traits & Physical Characteristics ---
    private int workerLifespan = 5000;
    private float workerSpeed = 0.5f;
    private float viewDistance = 5.0f;
    private int typicalColonySize = 1000;
    private boolean formsMegaColonies = false;
    private float aggression = 0.3f;
    private float metabolism = 1.0f;
    private float strength = 5.0f;
    private boolean workersCanFly = false; // True for bees, wasps

    // --- Diet & Metabolism ---
    private String primaryDiet = "SUGARS_NECTAR"; // SUGARS_NECTAR, INSECTS_MEAT, SEEDS, FUNGUS, WOOD_CELLULOSE, HONEYDEW, OMNIVORE
    private String secondaryDiet = "INSECTS_MEAT";
    private float dailyFoodConsumption = 0.5f;
    private float waterRequirement = 0.2f;

    // --- Nest & Environment ---
    private String nestType = "UNDERGROUND_BURROW"; // UNDERGROUND_BURROW, MOUND, WOOD_TUNNELS, PAPER_NEST, WAX_COMB, ARBOREAL_LEAF
    private float optimalTempCelsius = 24.0f;
    private float minTempCelsius = 10.0f;
    private float maxTempCelsius = 38.0f;
    private float territoriality = 0.5f;
    private String venomType = "NONE"; // NONE, FORMIC_ACID, VENOMOUS_STING, CHEMICAL_SPRAY, POWERFUL_MANDIBLES

    // --- Sensory Systems (Thermic, Gas, Visual, Magnetoreception, Vibration, Hygro, Electro, Polarized Light) ---
    private boolean hasMagnetoreception = false;
    private float magnetoreceptionSensitivity = 5.0f; // µT
    private float thermoreceptionSensitivity = 0.5f; // °C gradient
    private float gasSensitivityCo2Ppm = 400.0f; // ppm
    private float visualAcuity = 1.0f;
    private float minLightLevelThreshold = 0.05f;
    private boolean hasSubstrateVibrationSensing = true;
    private float vibrationSensitivityDb = 10.0f;
    private boolean hasHygroreception = true;
    private float hygroreceptionSensitivityPercent = 2.0f;
    private boolean hasElectrosensing = false;
    private float electroceptionSensitivityVolts = 50.0f;
    private boolean hasPolarizedLightNavigation = false;

    // --- Biomechanical & Motor Systems ---
    private float wingbeatFrequencyHz = 200.0f;
    private boolean hasHoveringCapability = false;
    private float maxCarryingPayloadRatio = 5.0f;
    private float mandibularBitingForceMPa = 15.0f;
    private boolean hasAutothysis = false;
    private boolean hasSubstrateAdhesionArolia = true;

    // --- Species-Specific Behavioral Capability Flags ---
    private boolean canPerformBiostructures = false;
    private boolean canPerformNecrophoresis = true;
    private boolean canPerformSocialThermoregulation = false;
    private boolean isUnicolonial = false;
    private boolean hasMandibularWearPolyethism = true;
    private boolean canPerformTandemRunning = false;
    private boolean canPerformWaggleDance = false;
    private boolean canPerformLarvalSalivaryTrophallaxis = false;
    private boolean hasTermiteGutSymbiosis = false;
    private boolean canFarmAphids = false;
    private boolean hasRoyalPheromoneInhibition = true;
    private boolean canDrumSubstrate = false;
    private boolean isPolycalic = false;
    private boolean canCollectPropolis = false;
    private boolean canSewLeavesWithLarvalSilk = false;
    private boolean canWeedFungusGarden = false;
    private boolean canMakeStercoralCement = false;
    private boolean hasProctodealTrophallaxis = false;
    private boolean canPerformPhragmosis = false;
    private boolean canPerformEvaporativeCooling = false;
    private boolean hasTrapJawMechanism = false;
    private boolean isSlaveMakingSpecies = false;
    private boolean canFormLivingBivouac = false;
    private boolean hasSolarOrientedMound = false;
    private boolean canPerformAllogrooming = true;
    private boolean canPerformTrembleDance = false;
    private boolean hasThermalTrailDecay = true;
    private boolean canPerformThoracicIncubation = false;
    private boolean canPerformRitualJousting = false;
    private boolean hasTerritorialRepellentPheromone = false;
    private boolean canDetectHydrostaticPressure = true;
    private boolean isRobberBeeSpecies = false;
    private boolean canStridulateRescueCall = false;
    private boolean isHoneypotStorageCaste = false;
    private boolean canPlugContaminatedGalleries = false;
    private boolean hasOleicAcidThresholdNecrophoresis = true;
    private boolean hasUVPolarizedLightNavigation = false;
    private boolean canPerformThermalBalling = false;
    private boolean canFormLivingRaft = false;
    private boolean canInhabitDomatia = false;
    private boolean canSelfIsolateWhenInfected = true;
    private boolean canSprayFormicResinDisinfectant = false;
    private boolean canTriggerEmergencySwarming = true;
    private boolean canConstructClayPillars = false;
    private boolean canDeGermStoredSeeds = false;
    private boolean canPerformQueenPiping = false;
    private boolean canPerformWaterTrophallaxis = true;
    private boolean canEnforceAphidSanitaryCordon = false;
    private boolean canFormLivingBridges = false;
    private boolean canEmitAcousticPreySurge = false;
    private boolean canSortExternalRefusePits = true;
    private boolean canCultivateWoodFungus = false;
    private boolean hasEmergencyEscapePheromone = true;
    private boolean canSealQueenChamberWax = false;
    private boolean hasCasteRatioPheromoneInhibition = true;
    private boolean canPerformSuctionEscapePosture = false;
    private boolean canStridulateQueenRecognition = false;
    private boolean canPerformPulsatileVentilation = false;
    private boolean canRepairBreachesClay = true;
    private boolean hasDepletingTrailPheromone = true;
    private boolean canRecycleInviableEggs = true;
    private boolean canQuarantineInvasiveParasites = false;
    private boolean canPerformArborealGlidingEscape = false;
    private boolean canPerformSolarBroodBasking = false;
    private boolean canHarmonizeChcGestalt = true;
    private boolean canConstructCollapsiblePitTraps = false;
    private boolean canHarvestDewCondensation = true;
    private boolean canPerformExoskeletonAntiFungalPatrol = true;
    private boolean canPerformGuardShiftVibrationalWhisper = false;
    private boolean canConstructThermoregulatedConduits = false;
    private boolean canRaidToxicPlantResin = false;
    private boolean canApplyDustSubstrateCamouflage = false;
    private boolean canTransportChainBrood = true;
    private boolean hasTrophallacticOvaryInhibition = true;
    private boolean canPerformDroughtVibratoDance = false;
    private boolean canEncapsulateLargeIntrudersClay = true;
    private boolean canConstructPhonicIsolationChambers = false;
    private boolean canApplyHydrophobicTrailCoating = false;
    private boolean canConsumeFermentedSapAnesthetic = false;
    private boolean canPerformRelaySeedTransport = true;
    private boolean hasPreySizeSelectivePheromones = true;
    private boolean canDryLarvaeWoodDust = true;
    private boolean canPerformFanoutEscapeFormicAcid = true;
    private boolean canEmitMoundOverheatVibrato = false;
    private boolean canNourishVirginQueensPreFlight = true;
    private boolean canPlugHoneyStoresBricks = true;

    // Batch IX fields (81-90)
    private boolean canHuntNocturnalInfrared = false;
    private boolean canWeaveLarvalSilkCanopyBridges = false;
    private boolean canStridulateEggLayingSynchronization = false;
    private boolean canPerformAntennalDustGrooming = true;
    private boolean canForageSaltCrystalsOsmoregulation = false;
    private boolean canConstructRainEvacuationSiphons = false;
    private boolean canAbsorbHostPlantChemicalCamouflage = false;
    private boolean canDepositSulfurDustAntiMitePatrol = false;
    private boolean canDanceVibratoHatchingEnthusiasm = true;
    private boolean canResinMummifyNymphalChambers = false;

    // Batch X fields (91-100)
    private boolean canExcavatePitfallTraps = false;
    private boolean canSynthesizeGlycerolCryoprotection = true;
    private boolean canTransportInjuredPheromonalStretcher = true;
    private boolean canRaidAbandonedWaxVaults = false;
    private boolean canPerformRitualMandibularWrestling = true;
    private boolean canPerformPulsedAirConvectiveVentilation = false;
    private boolean canCultivateStreptomycesAntibiotics = true;
    private boolean canNavigatePolarizedTwilightUV = true;
    private boolean canSnapTrapMandiblesCatapult = false;
    private boolean canPerformPedestrianSwarmBudding = true;

    // Batch XI & XII fields (101-120)
    private boolean canTrophallaxisProtozoa = false;
    private boolean canSquirtNasuteChemical = false;
    private boolean canMasticatePaperPulpCarton = false;
    private boolean canHarvestLarvalSalivaDroplets = false;
    private boolean canApplyPedicelAntRepellent = false;
    private boolean canRecognizeFacialVisualPatterns = false;
    private boolean canPerformBuzzPollination = false;
    private boolean canIncubateBroodAbdominalHeat = false;
    private boolean canStabFrontalHornsAphid = false;
    private boolean canSqueezeGallIntrudersThrips = false;
    private boolean canSnapClawAcousticShockwave = false;
    private boolean canStridulatePassalidParentalCare = false;
    private boolean canPerformPhysogastricPeristalsis = false;
    private boolean canOrientMagneticMound = false;
    private boolean canEmitHornetGroupAlarmPheromone = false;
    private boolean canWeaveStenogastrinePaperJelly = false;
    private boolean canInoculateFungalCombTermite = false;
    private boolean canDrumAbdomenWaspCellRim = false;
    private boolean canConstructNectarWaxPots = false;
    private boolean canPerformMaternalShieldGuarding = false;

    // Batch XIII fields (121-140)
    private boolean canWeaveCommunalSpiderSilk = false;
    private boolean canFormProcessionarySilkTrail = false;
    private boolean canConstructClayVaultArches = false;
    private boolean canDeliverStenogastrinePapFood = false;
    private boolean canPlasterFrassGalleryWalls = false;
    private boolean canLearnTrapliningFlightRoutes = false;
    private boolean canCoolNestWaterRegurgitation = false;
    private boolean canEjectHoneydewSignalingDroplets = false;
    private boolean canSnapMandibleAcousticAlarm = false;
    private boolean canPerformEggLickingGrooming = false;
    private boolean canConstructChaffGarbageDunes = false;
    private boolean canDrumAntennaeLarvalStimulation = false;
    private boolean canFormLeafPullingChains = false;
    private boolean canApplySalivaryCementMoistureSeal = false;
    private boolean canForageSubZeroBumblebee = false;
    private boolean canRepairGallSubstratalSecretion = false;
    private boolean canTrophallaxisPassalidWoodFrass = false;
    private boolean canPerformCrècheRegurgitationSpider = false;
    private boolean canBlockRoyalChamberSentry = false;
    private boolean canEmitParentBugAlarmGathering = false;

    // Batch XIV fields (141-160)
    private boolean canApplyBeeBreadHydrophobicCoating = false;
    private boolean canBindParasitesWithSilk = false;
    private boolean canEmitSubstrateObstacleVibrato = false;
    private boolean canPerformFormicAcidBathGrooming = false;
    private boolean canExcavateVerticalDrainageShafts = false;
    private boolean canIngestPhenolicResinMedication = false;
    private boolean canConstructSphagnumMoistureDomes = false;
    private boolean canMarkParasitizedCadaverRepellent = false;
    private boolean canDrumNuptialFlightSynchronization = false;
    private boolean canHarvestCuticularWaterCondensation = false;
    private boolean canStridulateLarvalHungerChirp = false;
    private boolean canConstructThermalChimneyFlues = false;
    private boolean canDepositLarvalFoodSalivaDrop = false;
    private boolean canApplyEggMassMucilageEnvelope = false;
    private boolean canWeaveSilkPavilionAphidShelter = false;
    private boolean canFormHotBallThermalDefense = false;
    private boolean canPerformFontanelleAutothysis = false;
    private boolean canSensePreySignalWireTripping = false;
    private boolean canMutilateSeedRadicles = false;
    private boolean canBiteNectarTheftHoles = false;

    // Batch XV fields (161-180)
    private boolean canSowFungalSporeCombs = false;
    private boolean canHarnessLarvalSilkCocoon = false;
    private boolean canFormBiomechanicalBivouac = false;
    private boolean canPerformBuzzPollinationSonication = false;
    private boolean canRegurgitateEarwigMaternalFood = false;
    private boolean canRecognizeWaspFacialPatterns = false;
    private boolean canFireShrimpAcousticCannon = false;
    private boolean canDuetPassalidSubstrateVibration = false;
    private boolean canTurnGranarySeedsAeration = false;
    private boolean canEncodeWaggleDanceSunCompass = false;
    private boolean canDigSubterraneanClayAqueducts = false;
    private boolean canFireFormicAcidArtilleryJet = false;
    private boolean canEjectGarbageChuteRefuse = false;
    private boolean canFanWingsForBroodThermoregulation = false;
    private boolean canPlugGallWithChitinousTube = false;
    private boolean canCoatWaspPedicelAntRepellent = false;
    private boolean canSquirtNasuteViscousResin = false;
    private boolean canSqueezeIntrudersWithForelegs = false;
    private boolean canShieldEggsFromParasitoidWasps = false;
    private boolean canPlasterWoodWallGallery = false;

    // Batch XVI fields (181-200)
    private boolean canShearLeafCrescentMandible = false;
    private boolean canShieldSwarmCoreHeat = false;
    private boolean canPerformQueenPhysogastricPeristalsis = false;
    private boolean canWeaveSocialSilkHammock = false;
    private boolean canPackCorbiculaPollenBaskets = false;
    private boolean canScrapeWoodPulpCarton = false;
    private boolean canLayTrophicNourishmentEggs = false;
    private boolean canNavigatePolarizedLightCompass = false;
    private boolean canBuryFungalWasteInGallery = false;
    private boolean canChewSeedHuskBreadPulp = false;
    private boolean canWrapPreyInCommunalSilk = false;
    private boolean canSealNestGapsWithPropolis = false;
    private boolean canSynchronizeSoldierAlarmDrumming = false;
    private boolean canPerformDominanceMounting = false;
    private boolean canLapNectarTongueExtension = false;
    private boolean canSecreteGallClosingFluid = false;
    private boolean canGroomNymphCuticularSurface = false;
    private boolean canExcavateGardenWasteChambers = false;
    private boolean canExchangeRoyalPairGrooming = false;
    private boolean canTriggerUniversalEmergencyEvacuation = false;

    // Batch XVII fields (201-220)
    private boolean canStoreNectarAsHoneypotReplete = false;
    private boolean canFormFloatingAntRaft = false;
    private boolean canConstructMudResinEntranceFunnel = false;
    private boolean canExcavateHibernationBurrow = false;
    private boolean canInoculateLeafPulpEnzymes = false;
    private boolean canPerformGamergateDominanceTournament = false;
    private boolean canFeedOnLarvalHemolymphDracula = false;
    private boolean canFormTarsalFrictionBridge = false;
    private boolean canTransportWaterInMandibleDroplet = false;
    private boolean canStiltWalkThermalRegim = false;
    private boolean canPerformAntiPredatorShimmeringWave = false;
    private boolean canDouseNestWaterCooling = false;
    private boolean canAerateFungalCombChambers = false;
    private boolean canFeedLarvaeExuviaRecycling = false;
    private boolean canGroomLeafPulpParasitesMinim = false;
    private boolean canCamouflageWebWithPlantDebris = false;
    private boolean canCockGasterFormicAcidRepellent = false;
    private boolean canMilkAphidHoneydewStroking = false;
    private boolean canClusterSolarHeatCollector = false;
    private boolean canSerializeGlobalEthologicalBitSet = false;

    // --- BitSet Storage for High-Performance Engine Queries ---
    @JsonIgnore
    private final BitSet capabilitiesBitSet = new BitSet(256);

    // --- Castes ---
    private List<CasteTemplate> casteTemplates = new ArrayList<>();

    // Default constructor for Jackson
    public CustomSpecies() {
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    @Override
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
        if (scientificName != null && scientificName.contains(" ")) {
            this.genus = scientificName.split(" ")[0];
        }
    }

    @Override
    public String getGenus() {
        if (genus == null || genus.trim().isEmpty()) {
            if (scientificName != null && scientificName.contains(" ")) {
                return scientificName.split(" ")[0];
            }
            return "Formica";
        }
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    @Override
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInsectType() {
        return insectType;
    }

    public void setInsectType(String insectType) {
        this.insectType = insectType;
    }

    public String getQueenCountMode() {
        return queenCountMode;
    }

    public void setQueenCountMode(String queenCountMode) {
        this.queenCountMode = queenCountMode;
    }

    public int getQueenCount() {
        return queenCount;
    }

    public void setQueenCount(int queenCount) {
        this.queenCount = queenCount;
    }

    @Override
    public int getQueenLifespan() {
        return queenLifespan;
    }

    public void setQueenLifespan(int queenLifespan) {
        this.queenLifespan = queenLifespan;
    }

    public boolean isHasKing() {
        return hasKing;
    }

    public void setHasKing(boolean hasKing) {
        this.hasKing = hasKing;
    }

    public int getKingLifespan() {
        return kingLifespan;
    }

    public void setKingLifespan(int kingLifespan) {
        this.kingLifespan = kingLifespan;
    }

    public float getQueenEggLayingRate() {
        return queenEggLayingRate;
    }

    public void setQueenEggLayingRate(float queenEggLayingRate) {
        this.queenEggLayingRate = queenEggLayingRate;
    }

    public String getNuptialFlightType() {
        return nuptialFlightType;
    }

    public void setNuptialFlightType(String nuptialFlightType) {
        this.nuptialFlightType = nuptialFlightType;
    }

    public int getEggStageDuration() {
        return eggStageDuration;
    }

    public void setEggStageDuration(int eggStageDuration) {
        this.eggStageDuration = eggStageDuration;
    }

    public int getLarvaStageDuration() {
        return larvaStageDuration;
    }

    public void setLarvaStageDuration(int larvaStageDuration) {
        this.larvaStageDuration = larvaStageDuration;
    }

    public int getPupaStageDuration() {
        return pupaStageDuration;
    }

    public void setPupaStageDuration(int pupaStageDuration) {
        this.pupaStageDuration = pupaStageDuration;
    }

    public String getLarvaDietRequirement() {
        return larvaDietRequirement;
    }

    public void setLarvaDietRequirement(String larvaDietRequirement) {
        this.larvaDietRequirement = larvaDietRequirement;
    }

    @Override
    public int getWorkerLifespan() {
        return workerLifespan;
    }

    public void setWorkerLifespan(int workerLifespan) {
        this.workerLifespan = workerLifespan;
    }

    @Override
    public float getWorkerSpeed() {
        return workerSpeed;
    }

    public void setWorkerSpeed(float workerSpeed) {
        this.workerSpeed = workerSpeed;
    }

    @Override
    public float getViewDistance() {
        return viewDistance;
    }

    public void setViewDistance(float viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public int getTypicalColonySize() {
        return typicalColonySize;
    }

    public void setTypicalColonySize(int typicalColonySize) {
        this.typicalColonySize = typicalColonySize;
    }

    @Override
    public boolean formsMegaColonies() {
        return formsMegaColonies;
    }

    public void setFormsMegaColonies(boolean formsMegaColonies) {
        this.formsMegaColonies = formsMegaColonies;
    }

    @Override
    public float getAggression() {
        return aggression;
    }

    public void setAggression(float aggression) {
        this.aggression = aggression;
    }

    @Override
    public float getMetabolism() {
        return metabolism;
    }

    public void setMetabolism(float metabolism) {
        this.metabolism = metabolism;
    }

    @Override
    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public boolean isWorkersCanFly() {
        return workersCanFly;
    }

    public void setWorkersCanFly(boolean workersCanFly) {
        this.workersCanFly = workersCanFly;
    }

    public String getPrimaryDiet() {
        return primaryDiet;
    }

    public void setPrimaryDiet(String primaryDiet) {
        this.primaryDiet = primaryDiet;
    }

    public String getSecondaryDiet() {
        return secondaryDiet;
    }

    public void setSecondaryDiet(String secondaryDiet) {
        this.secondaryDiet = secondaryDiet;
    }

    public float getDailyFoodConsumption() {
        return dailyFoodConsumption;
    }

    public void setDailyFoodConsumption(float dailyFoodConsumption) {
        this.dailyFoodConsumption = dailyFoodConsumption;
    }

    public float getWaterRequirement() {
        return waterRequirement;
    }

    public void setWaterRequirement(float waterRequirement) {
        this.waterRequirement = waterRequirement;
    }

    public String getNestType() {
        return nestType;
    }

    public void setNestType(String nestType) {
        this.nestType = nestType;
    }

    public float getOptimalTempCelsius() {
        return optimalTempCelsius;
    }

    public void setOptimalTempCelsius(float optimalTempCelsius) {
        this.optimalTempCelsius = optimalTempCelsius;
    }

    public float getMinTempCelsius() {
        return minTempCelsius;
    }

    public void setMinTempCelsius(float minTempCelsius) {
        this.minTempCelsius = minTempCelsius;
    }

    public float getMaxTempCelsius() {
        return maxTempCelsius;
    }

    public void setMaxTempCelsius(float maxTempCelsius) {
        this.maxTempCelsius = maxTempCelsius;
    }

    public float getTerritoriality() {
        return territoriality;
    }

    public void setTerritoriality(float territoriality) {
        this.territoriality = territoriality;
    }

    public String getVenomType() {
        return venomType;
    }

    public void setVenomType(String venomType) {
        this.venomType = venomType;
    }

    public List<CasteTemplate> getCasteTemplates() {
        return casteTemplates;
    }

    public void setCasteTemplates(List<CasteTemplate> casteTemplates) {
        this.casteTemplates = casteTemplates;
    }

    @Override
    @JsonIgnore
    public List<CasteTemplate> getCastes() {
        return casteTemplates;
    }

    /**
     * Calculates the average body length (mm) across defined castes.
     */
    public float getAverageCasteBodyLengthMm() {
        if (casteTemplates == null || casteTemplates.isEmpty()) {
            return 5.0f;
        }
        float sum = 0.0f;
        int count = 0;
        for (CasteTemplate ct : casteTemplates) {
            sum += ct.getBodyLengthMm();
            count++;
        }
        return count > 0 ? sum / count : 5.0f;
    }

    /**
     * Calculates the minimum tunnel diameter (mm) required for the largest caste of this species to pass.
     */
    public float getRequiredTunnelDiameterMm() {
        if (casteTemplates == null || casteTemplates.isEmpty()) {
            return 3.0f;
        }
        float maxMinDiameter = 1.0f;
        for (CasteTemplate ct : casteTemplates) {
            maxMinDiameter = Math.max(maxMinDiameter, ct.getMinTunnelDiameterMm());
        }
        return maxMinDiameter;
    }

    @Override
    public InsectOrder getInsectOrder() {
        if (insectType == null) return InsectOrder.ANT;
        String type = insectType.toUpperCase().trim();
        if (type.contains("BEE") || type.contains("APIDAE")) return InsectOrder.BEE;
        if (type.contains("WASP") || type.contains("VESPIDAE")) return InsectOrder.WASP;
        if (type.contains("TERMITE") || type.contains("ISOPTERA") || type.contains("TERMITOIDAE")) return InsectOrder.TERMITE;
        if (type.contains("APHID") || type.contains("APHIDIDAE")) return InsectOrder.APHID;
        if (type.contains("THRIPS") || type.contains("THYSANOPTERA")) return InsectOrder.THRIPS;
        if (type.contains("BEETLE") || type.contains("COLEOPTERA")) return InsectOrder.BEETLE;
        return InsectOrder.ANT;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        java.util.Set<org.swarmforge.core.domain.ResourceType> set = new java.util.HashSet<>();
        addDietResourceType(set, primaryDiet);
        addDietResourceType(set, secondaryDiet);
        if (set.isEmpty()) {
            set.add(org.swarmforge.core.domain.ResourceType.SEED);
        }
        return set;
    }

    private void addDietResourceType(java.util.Set<org.swarmforge.core.domain.ResourceType> set, String diet) {
        if (diet == null || diet.equalsIgnoreCase("NONE")) return;
        switch (diet.toUpperCase()) {
            case "WOOD_CELLULOSE", "CELLULOSE", "WOOD" -> set.add(org.swarmforge.core.domain.ResourceType.WOOD);
            case "FUNGUS" -> set.add(org.swarmforge.core.domain.ResourceType.FUNGUS);
            case "SEEDS" -> set.add(org.swarmforge.core.domain.ResourceType.SEED);
            case "SUGARS_NECTAR", "SUGAR_HONEY" -> {
                set.add(org.swarmforge.core.domain.ResourceType.NECTAR);
                set.add(org.swarmforge.core.domain.ResourceType.SUGAR);
            }
            case "HONEYDEW" -> set.add(org.swarmforge.core.domain.ResourceType.HONEYDEW);
            case "INSECTS_MEAT", "HIGH_PROTEIN_MEAT" -> {
                set.add(org.swarmforge.core.domain.ResourceType.PROTEIN);
                set.add(org.swarmforge.core.domain.ResourceType.INSECT);
            }
            case "OMNIVORE" -> {
                set.add(org.swarmforge.core.domain.ResourceType.SEED);
                set.add(org.swarmforge.core.domain.ResourceType.SUGAR);
                set.add(org.swarmforge.core.domain.ResourceType.PROTEIN);
            }
        }
    }

    @Override
    public SpeciesCategory getCategory() {
        return category != null ? category : SpeciesCategory.EUSOCIAL_PRIMARY;
    }

    public void setCategory(SpeciesCategory category) {
        this.category = category;
    }

    public float getProteinThresholdMinor() { return proteinThresholdMinor; }
    public void setProteinThresholdMinor(float proteinThresholdMinor) { this.proteinThresholdMinor = proteinThresholdMinor; }

    public float getProteinThresholdMajor() { return proteinThresholdMajor; }
    public void setProteinThresholdMajor(float proteinThresholdMajor) { this.proteinThresholdMajor = proteinThresholdMajor; }

    public float getProteinThresholdSoldier() { return proteinThresholdSoldier; }
    public void setProteinThresholdSoldier(float proteinThresholdSoldier) { this.proteinThresholdSoldier = proteinThresholdSoldier; }

    public float getProteinThresholdQueen() { return proteinThresholdQueen; }
    public void setProteinThresholdQueen(float proteinThresholdQueen) { this.proteinThresholdQueen = proteinThresholdQueen; }

    public float getQueenPheromoneInhibitionFactor() { return queenPheromoneInhibitionFactor; }
    public void setQueenPheromoneInhibitionFactor(float queenPheromoneInhibitionFactor) { this.queenPheromoneInhibitionFactor = queenPheromoneInhibitionFactor; }

    public boolean isHaplodiploidyEnabled() { return haplodiploidyEnabled; }
    public void setHaplodiploidyEnabled(boolean haplodiploidyEnabled) { this.haplodiploidyEnabled = haplodiploidyEnabled; }

    public float getPathogenResistance() { return pathogenResistance; }
    public void setPathogenResistance(float pathogenResistance) { this.pathogenResistance = pathogenResistance; }

    public float getGroomingDefenseEfficacy() { return groomingDefenseEfficacy; }
    public void setGroomingDefenseEfficacy(float groomingDefenseEfficacy) { this.groomingDefenseEfficacy = groomingDefenseEfficacy; }

    @Override
    public boolean hasMagnetoreception() { return hasMagnetoreception; }
    public void setHasMagnetoreception(boolean hasMagnetoreception) { this.hasMagnetoreception = hasMagnetoreception; }

    @Override
    public float getMagnetoreceptionSensitivity() { return magnetoreceptionSensitivity; }
    public void setMagnetoreceptionSensitivity(float magnetoreceptionSensitivity) { this.magnetoreceptionSensitivity = magnetoreceptionSensitivity; }

    @Override
    public float getThermoreceptionSensitivity() { return thermoreceptionSensitivity; }
    public void setThermoreceptionSensitivity(float thermoreceptionSensitivity) { this.thermoreceptionSensitivity = thermoreceptionSensitivity; }

    @Override
    public float getGasSensitivityCo2Ppm() { return gasSensitivityCo2Ppm; }
    public void setGasSensitivityCo2Ppm(float gasSensitivityCo2Ppm) { this.gasSensitivityCo2Ppm = gasSensitivityCo2Ppm; }

    @Override
    public float getVisualAcuity() { return visualAcuity; }
    public void setVisualAcuity(float visualAcuity) { this.visualAcuity = visualAcuity; }

    @Override
    public float getMinLightLevelThreshold() { return minLightLevelThreshold; }
    public void setMinLightLevelThreshold(float minLightLevelThreshold) { this.minLightLevelThreshold = minLightLevelThreshold; }

    @Override
    public boolean hasSubstrateVibrationSensing() { return hasSubstrateVibrationSensing; }
    public void setHasSubstrateVibrationSensing(boolean hasSubstrateVibrationSensing) { this.hasSubstrateVibrationSensing = hasSubstrateVibrationSensing; }

    @Override
    public float getVibrationSensitivityDb() { return vibrationSensitivityDb; }
    public void setVibrationSensitivityDb(float vibrationSensitivityDb) { this.vibrationSensitivityDb = vibrationSensitivityDb; }

    @Override
    public boolean hasHygroreception() { return hasHygroreception; }
    public void setHasHygroreception(boolean hasHygroreception) { this.hasHygroreception = hasHygroreception; }

    @Override
    public float getHygroreceptionSensitivityPercent() { return hygroreceptionSensitivityPercent; }
    public void setHygroreceptionSensitivityPercent(float hygroreceptionSensitivityPercent) { this.hygroreceptionSensitivityPercent = hygroreceptionSensitivityPercent; }

    @Override
    public boolean hasElectrosensing() { return hasElectrosensing; }
    public void setHasElectrosensing(boolean hasElectrosensing) { this.hasElectrosensing = hasElectrosensing; }

    @Override
    public float getElectroceptionSensitivityVolts() { return electroceptionSensitivityVolts; }
    public void setElectroceptionSensitivityVolts(float electroceptionSensitivityVolts) { this.electroceptionSensitivityVolts = electroceptionSensitivityVolts; }

    @Override
    public boolean hasPolarizedLightNavigation() { return hasPolarizedLightNavigation; }
    public void setHasPolarizedLightNavigation(boolean hasPolarizedLightNavigation) { this.hasPolarizedLightNavigation = hasPolarizedLightNavigation; }

    @Override
    public float getWingbeatFrequencyHz() { return wingbeatFrequencyHz; }
    public void setWingbeatFrequencyHz(float wingbeatFrequencyHz) { this.wingbeatFrequencyHz = wingbeatFrequencyHz; }

    @Override
    public boolean hasHoveringCapability() { return hasHoveringCapability; }
    public void setHasHoveringCapability(boolean hasHoveringCapability) { this.hasHoveringCapability = hasHoveringCapability; }

    @Override
    public float getMaxCarryingPayloadRatio() { return maxCarryingPayloadRatio; }
    public void setMaxCarryingPayloadRatio(float maxCarryingPayloadRatio) { this.maxCarryingPayloadRatio = maxCarryingPayloadRatio; }

    @Override
    public float getMandibularBitingForceMPa() { return mandibularBitingForceMPa; }
    public void setMandibularBitingForceMPa(float mandibularBitingForceMPa) { this.mandibularBitingForceMPa = mandibularBitingForceMPa; }

    @Override
    public boolean hasAutothysis() { return hasAutothysis; }
    public void setHasAutothysis(boolean hasAutothysis) { this.hasAutothysis = hasAutothysis; }

    @Override
    public boolean hasSubstrateAdhesionArolia() { return hasSubstrateAdhesionArolia; }
    public void setHasSubstrateAdhesionArolia(boolean hasSubstrateAdhesionArolia) { this.hasSubstrateAdhesionArolia = hasSubstrateAdhesionArolia; }

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

    @Override public boolean canPerformCrècheRegurgitationSpider() { return canPerformCrècheRegurgitationSpider; }
    public void setCanPerformCrècheRegurgitationSpider(boolean val) { this.canPerformCrècheRegurgitationSpider = val; }

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

    @Override
    public BitSet getCapabilitiesBitSet() {
        return capabilitiesBitSet;
    }

    // --- Dynamic Plugin Extensibility Attributes ---
    private java.util.Map<String, Object> customAttributes = new java.util.HashMap<>();

    @Override
    public java.util.Map<String, Object> getCustomAttributes() { return customAttributes; }
    public void setCustomAttributes(java.util.Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes != null ? customAttributes : new java.util.HashMap<>();
    }

    public void setCustomAttribute(String key, Object value) {
        if (this.customAttributes == null) this.customAttributes = new java.util.HashMap<>();
        this.customAttributes.put(key, value);
    }

    @Override
    public Object getCustomAttribute(String key, Object defaultValue) {
        if (customAttributes == null) return defaultValue;
        return customAttributes.getOrDefault(key, defaultValue);
    }
}

