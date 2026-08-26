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
        return switch (insectType.toUpperCase()) {
            case "BEE" -> InsectOrder.BEE;
            case "WASP" -> InsectOrder.WASP;
            case "TERMITE" -> InsectOrder.TERMITE;
            default -> InsectOrder.ANT;
        };
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

