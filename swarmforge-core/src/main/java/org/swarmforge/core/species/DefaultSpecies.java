/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
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
 * @author SilvÃ¨re Martin-Michiellot
 * @author Gemini AI Assistant
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultSpecies extends SpeciesCapabilities {

    private String id;
    private String author = "Custom";
    private int version = 1;
    private long revisionTimestamp = System.currentTimeMillis();
    private String contentChecksum = "";
    private boolean builtIn = false;

    private String presetName = "Default Species";
    private String scientificName = "Formica genericus";
    private String genus = "Formica";
    private String commonName = "Generic Ant";
    private String description = "Generic eusocial species for simulation.";
    private String insectType = "ANT"; // ANT, BEE, WASP, TERMITE, OTHER
    private SpeciesCategory category = SpeciesCategory.EUSOCIAL_PRIMARY; // EUSOCIAL_PRIMARY, COMMENSAL, HONEYDEW_PRODUCER, PREY_ORGANISM, SOIL_FAUNA, PARASITE_PREDATOR

    // --- Colony & Queens (Aspect CRITIQUE) ---
    private String queenCountMode = null; // MONOGYNE, POLYGYNE, GAMERGATES
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
    private float walkingSpeed = 0.45f;
    private float runningSpeed = 0.85f;
    private float flyingSpeed = 4.5f;
    private float viewDistance = 5.0f;
    private int typicalColonySize = 1000;
    private boolean formsMegaColonies = false;
    private float aggression = 0.3f;
    private float metabolism = 1.0f;
    private float strength = 5.0f;
    private boolean workersCanFly = false; // True for bees, wasps

    // --- Diet & Metabolism ---
    private String primaryDiet = null; // SUGARS_NECTAR, INSECTS_MEAT, SEEDS, FUNGUS, WOOD_CELLULOSE, HONEYDEW, OMNIVORE
    private String secondaryDiet = null;
    private float dailyFoodConsumption = 0.5f;
    private float waterRequirement = 0.2f;

    // --- Nest & Environment ---
    private String nestType = null; // UNDERGROUND_BURROW, MOUND, WOOD_TUNNELS, PAPER_NEST, WAX_COMB, ARBOREAL_LEAF
    private float optimalTempCelsius = 24.0f;
    private float minTempCelsius = 0.0f;
    private float maxTempCelsius = 45.0f;
    private float optimalHumidityPercent = 80.0f;
    private float minHumidityPercent = 20.0f;
    private float maxHumidityPercent = 100.0f;
    private float territoriality = 0.5f;
    private String venomType = null; // NONE, FORMIC_ACID, VENOMOUS_STING, CHEMICAL_SPRAY, POWERFUL_MANDIBLES

    // --- Sensory Systems (Thermic, Gas, Visual, Magnetoreception, Vibration, Hygro, Electro, Polarized Light) ---
    private boolean hasMagnetoreception = false;
    private float magnetoreceptionSensitivity = 5.0f; // ÂµT
    private float thermoreceptionSensitivity = 0.5f; // Â°C gradient
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

    // --- Castes ---
    private List<CasteTemplate> casteTemplates = new ArrayList<>();

    // Default constructor for Jackson
    public DefaultSpecies() {
    }

    public DefaultSpecies(String genus, InsectOrder order) {
        applyTaxonomicArchetype(genus, order);
    }

    /**
     * Applies baseline taxonomic archetype parameters based on current genus, scientificName and insectType.
     */
    public void applyTaxonomicArchetypes() {
        InsectOrder order = null;
        if (insectType != null) {
            try {
                order = InsectOrder.valueOf(insectType.toUpperCase());
            } catch (Exception ignored) {}
        }
        String g = this.genus;
        if ((g == null || g.isBlank()) && scientificName != null && !scientificName.isBlank()) {
            g = scientificName.trim().split("\\s+")[0];
        }
        applyTaxonomicArchetype(g, order);
    }

    /**
     * Applies baseline taxonomic archetype parameters and ethological capabilities based on
     * the taxonomic order and genus, establishing a biologically sound foundation that specific species
     * can specialize or override.
     */
    public void applyTaxonomicArchetype(String genus, InsectOrder order) {
        TaxonomicArchetypeApplier.apply(this, genus, order);
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) { this.presetName = presetName; }

    @Override public String getScientificName() { return scientificName; }

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

    public void setGenus(String genus) { this.genus = genus; }

    @Override public String getCommonName() { return commonName; }

    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) { this.description = description; }

    public String getInsectType() {
        return insectType;
    }

    public void setInsectType(String insectType) { this.insectType = insectType; }

    public String getQueenCountMode() {
        return queenCountMode != null ? queenCountMode : "MONOGYNE";
    }

    public void setQueenCountMode(String queenCountMode) { this.queenCountMode = queenCountMode; }

    public int getQueenCount() {
        return queenCount;
    }

    public void setQueenCount(int queenCount) { this.queenCount = queenCount; }

    @Override public int getQueenLifespan() { return queenLifespan; }

    public void setQueenLifespan(int queenLifespan) { this.queenLifespan = queenLifespan; }

    public boolean isHasKing() {
        return hasKing;
    }

    public void setHasKing(boolean hasKing) { this.hasKing = hasKing; }

    public int getKingLifespan() {
        return kingLifespan;
    }

    public void setKingLifespan(int kingLifespan) { this.kingLifespan = kingLifespan; }

    public float getQueenEggLayingRate() {
        return queenEggLayingRate;
    }

    public void setQueenEggLayingRate(float queenEggLayingRate) { this.queenEggLayingRate = queenEggLayingRate; }

    public String getNuptialFlightType() {
        return nuptialFlightType;
    }

    public void setNuptialFlightType(String nuptialFlightType) { this.nuptialFlightType = nuptialFlightType; }

    public int getEggStageDuration() {
        return eggStageDuration;
    }

    public void setEggStageDuration(int eggStageDuration) { this.eggStageDuration = eggStageDuration; }

    public int getLarvaStageDuration() {
        return larvaStageDuration;
    }

    public void setLarvaStageDuration(int larvaStageDuration) { this.larvaStageDuration = larvaStageDuration; }

    public int getPupaStageDuration() {
        return pupaStageDuration;
    }

    public void setPupaStageDuration(int pupaStageDuration) { this.pupaStageDuration = pupaStageDuration; }

    public String getLarvaDietRequirement() {
        return larvaDietRequirement;
    }

    public void setLarvaDietRequirement(String larvaDietRequirement) { this.larvaDietRequirement = larvaDietRequirement; }

    @Override public int getWorkerLifespan() { return workerLifespan; }

    public void setWorkerLifespan(int workerLifespan) { this.workerLifespan = workerLifespan; }

    @Override public float getWorkerSpeed() { return workerSpeed; }

    public void setWorkerSpeed(float workerSpeed) {
        this.workerSpeed = workerSpeed;
        if (this.walkingSpeed <= 0) this.walkingSpeed = workerSpeed;
        if (this.runningSpeed <= 0) this.runningSpeed = workerSpeed * 1.65f;
    }

    @Override public float getWalkingSpeed() { return walkingSpeed > 0 ? walkingSpeed : workerSpeed; }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
        this.workerSpeed = walkingSpeed;
    }

    @Override public float getRunningSpeed() { return runningSpeed > 0 ? runningSpeed : (getWalkingSpeed() * 1.65f); }

    public void setRunningSpeed(float runningSpeed) { this.runningSpeed = runningSpeed; }

    @Override public float getFlyingSpeed() { return flyingSpeed > 0 ? flyingSpeed : (isWorkersCanFly() ? 4.5f : getWalkingSpeed() * 2.5f); }

    public void setFlyingSpeed(float flyingSpeed) { this.flyingSpeed = flyingSpeed; }

    @Override public float getViewDistance() { return viewDistance; }

    public void setViewDistance(float viewDistance) { this.viewDistance = viewDistance; }

    @Override public int getTypicalColonySize() { return typicalColonySize; }

    public void setTypicalColonySize(int typicalColonySize) { this.typicalColonySize = typicalColonySize; }

    @Override public boolean formsMegaColonies() { return formsMegaColonies; }

    public void setFormsMegaColonies(boolean formsMegaColonies) { this.formsMegaColonies = formsMegaColonies; }

    @Override public float getAggression() { return aggression; }

    public void setAggression(float aggression) { this.aggression = aggression; }

    @Override public float getMetabolism() { return metabolism; }

    public void setMetabolism(float metabolism) { this.metabolism = metabolism; }

    @Override public float getStrength() { return strength; }

    public void setStrength(float strength) { this.strength = strength; }

    public boolean isWorkersCanFly() {
        return workersCanFly;
    }

    public void setWorkersCanFly(boolean workersCanFly) { this.workersCanFly = workersCanFly; }

    public String getPrimaryDiet() {
        return primaryDiet != null ? primaryDiet : "SUGARS_NECTAR";
    }

    public void setPrimaryDiet(String primaryDiet) { this.primaryDiet = primaryDiet; }

    public String getSecondaryDiet() {
        return secondaryDiet != null ? secondaryDiet : "INSECTS_MEAT";
    }

    public void setSecondaryDiet(String secondaryDiet) { this.secondaryDiet = secondaryDiet; }

    public float getDailyFoodConsumption() {
        return dailyFoodConsumption;
    }

    public void setDailyFoodConsumption(float dailyFoodConsumption) { this.dailyFoodConsumption = dailyFoodConsumption; }

    public float getWaterRequirement() {
        return waterRequirement;
    }

    public void setWaterRequirement(float waterRequirement) { this.waterRequirement = waterRequirement; }

    public String getNestType() {
        return nestType != null ? nestType : "UNDERGROUND_BURROW";
    }

    public void setNestType(String nestType) { this.nestType = nestType; }

    public float getOptimalTempCelsius() {
        return optimalTempCelsius;
    }

    public void setOptimalTempCelsius(float optimalTempCelsius) { this.optimalTempCelsius = optimalTempCelsius; }

    public float getMinTempCelsius() {
        return minTempCelsius;
    }

    public void setMinTempCelsius(float minTempCelsius) { this.minTempCelsius = minTempCelsius; }

    public float getMaxTempCelsius() {
        return maxTempCelsius;
    }

    public void setMaxTempCelsius(float maxTempCelsius) { this.maxTempCelsius = maxTempCelsius; }

    @Override public float getOptimalHumidityPercent() { return optimalHumidityPercent; }

    public void setOptimalHumidityPercent(float optimalHumidityPercent) { this.optimalHumidityPercent = optimalHumidityPercent; }

    @Override public float getMinHumidityPercent() { return minHumidityPercent; }

    public void setMinHumidityPercent(float minHumidityPercent) { this.minHumidityPercent = minHumidityPercent; }

    @Override public float getMaxHumidityPercent() { return maxHumidityPercent; }

    public void setMaxHumidityPercent(float maxHumidityPercent) { this.maxHumidityPercent = maxHumidityPercent; }

    public float getTerritoriality() {
        return territoriality;
    }

    public void setTerritoriality(float territoriality) { this.territoriality = territoriality; }

    public String getVenomType() {
        return venomType != null ? venomType : "NONE";
    }

    public void setVenomType(String venomType) { this.venomType = venomType; }

    public List<CasteTemplate> getCasteTemplates() {
        return casteTemplates;
    }

    public void setCasteTemplates(List<CasteTemplate> casteTemplates) { this.casteTemplates = casteTemplates; }

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

    @Override public SpeciesCategory getCategory() { return category != null ? category : SpeciesCategory.EUSOCIAL_PRIMARY; }

    public void setCategory(SpeciesCategory category) { this.category = category; }

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

    public void setCanDeliverSalivaryLarvalPap(boolean val) { setCanDeliverStenogastrinePapFood(val); }

    @Override public float getOvipositionRate() { return queenEggLayingRate; }
    public void setOvipositionRate(float rate) { this.queenEggLayingRate = rate; }

    @Override public BitSet getCapabilitiesBitSet() { return capabilitiesBitSet; }

    // --- Dynamic Plugin Extensibility Attributes ---
    private java.util.Map<String, Object> customAttributes = new java.util.HashMap<>();

    @Override
    public java.util.Map<String, Object> getCustomAttributes() { return customAttributes; }
    public void setCustomAttributes(java.util.Map<String, Object> customAttributes) { this.customAttributes = customAttributes != null ? customAttributes : new java.util.HashMap<>(); }

    public void setCustomAttribute(String key, Object value) {
        if (this.customAttributes == null) this.customAttributes = new java.util.HashMap<>();
        this.customAttributes.put(key, value);
    }

    @Override
    public Object getCustomAttribute(String key, Object defaultValue) {
        if (customAttributes == null) return defaultValue;
        return customAttributes.getOrDefault(key, defaultValue);
    }

    public String getId() {
        if (id == null || id.isEmpty()) {
            String base = (scientificName != null && !scientificName.isEmpty()) ? scientificName : presetName;
            return (base != null ? base : "custom_species").toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author != null ? author : "Custom"; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = Math.max(1, version); }

    public long getRevisionTimestamp() { return revisionTimestamp; }
    public void setRevisionTimestamp(long revisionTimestamp) { this.revisionTimestamp = revisionTimestamp; }

    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }

    public String getContentChecksum() {
        if (contentChecksum == null || contentChecksum.isEmpty()) {
            return calculateChecksum();
        }
        return contentChecksum;
    }
    public void setContentChecksum(String contentChecksum) { this.contentChecksum = contentChecksum; }

    public String calculateChecksum() {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            sb.append(scientificName != null ? scientificName : "").append("|");
            sb.append(presetName != null ? presetName : "").append("|");
            sb.append(insectType != null ? insectType : "").append("|");
            sb.append(queenCountMode != null ? queenCountMode : "").append("|");
            sb.append(queenCount).append("|").append(queenLifespan).append("|").append(queenEggLayingRate).append("|");
            sb.append(workerLifespan).append("|").append(workerSpeed).append("|").append(typicalColonySize).append("|");
            sb.append(aggression).append("|").append(metabolism).append("|").append(strength).append("|");
            sb.append(primaryDiet != null ? primaryDiet : "").append("|").append(secondaryDiet != null ? secondaryDiet : "").append("|");
            sb.append(dailyFoodConsumption).append("|").append(waterRequirement).append("|");
            sb.append(nestType != null ? nestType : "").append("|").append(venomType != null ? venomType : "").append("|");
            sb.append(optimalTempCelsius).append("|").append(minTempCelsius).append("|").append(maxTempCelsius).append("|");
            sb.append(optimalHumidityPercent).append("|").append(minHumidityPercent).append("|").append(maxHumidityPercent);
            byte[] hash = md.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            this.contentChecksum = hex.toString().substring(0, 16);
            return this.contentChecksum;
        } catch (Exception e) {
            this.contentChecksum = "0000000000000000";
            return this.contentChecksum;
        }
    }

    public boolean isContentEqualTo(DefaultSpecies other) {
        if (other == null) return false;
        return getContentChecksum().equals(other.getContentChecksum());
    }
}

