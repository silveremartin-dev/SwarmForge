/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Ecological data model for non-eusocial accessory organisms, prey species,
 * apex predators, mutualists (aphids/mealybugs), flora, fungi, and pathogens.
 *
 * Distinct from colonial eusocial insect species ({@link org.swarmforge.core.species.DefaultSpecies}),
 * accessory presets model population densities, biomass yields, predatory styles, and epidemiological parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessoryPreset implements Serializable {

    public enum Category {
        FLORA,
        APHID_MUTUALIST,
        PREY_INSECT,
        PREDATOR,
        PATHOGEN_PARASITE,
        FUNGI,
        DETRITIVORE
    }

    public enum HuntMode {
        AMBUSH,
        DIRECT_ATTACK,
        PARASITOID,
        TRAP_FUNNEL
    }

    public enum TargetCaste {
        ALL_CASTES,
        WORKERS,
        BROOD_PUPAE,
        QUEENS_ALATES
    }

    public enum PathogenVector {
        AIRBORNE_SPORES,
        CONTAMINATED_FOOD,
        GROOMING,
        SOIL_CONTACT
    }

    private String id;
    private String name;
    private String scientificName;
    private String description;
    private Category category = Category.PREY_INSECT;
    private String biome = "TEMPERATE_DECIDUOUS";
    private double latitude = 45.0;

    // Thermal envelope & Growth
    private float minTempCelsius = 5.0f;
    private float optimalTempCelsius = 22.0f;
    private float maxTempCelsius = 35.0f;
    private float growthRate = 1.0f;
    private float initialBiomassDensity = 50.0f;
    private float initialPopulationDensity = 20.0f;
    private boolean diapause = false;

    // Seasonal Multipliers
    private String hemisphere = "NORTHERN";
    private float seasonMultiplierSpring = 0.8f;
    private float seasonMultiplierSummer = 1.0f;
    private float seasonMultiplierAutumn = 0.6f;
    private float seasonMultiplierWinter = 0.1f;

    // Predation & Parasitism
    private TargetCaste targetCaste = TargetCaste.ALL_CASTES;
    private HuntMode huntMode = HuntMode.DIRECT_ATTACK;
    private float killRate = 0.0f;
    private float visionRange = 10.0f;
    private float attackDamage = 10.0f;

    // Pathogen & Epidemiology
    private PathogenVector pathogenVector = PathogenVector.AIRBORNE_SPORES;
    private float transmissionR0 = 0.0f;
    private float incubationDays = 0.0f;
    private float mortalityRate = 0.0f;

    // Nutritional & Resource Yields
    private float proteinYield = 10.0f;
    private float sugarHoneydewYield = 0.0f;
    private float leafBiomassYield = 0.0f;

    private boolean builtIn = true;
    private String author = "Academic Reference";

    public AccessoryPreset() {}

    public AccessoryPreset(String id, String name, Category category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getBiome() { return biome; }
    public void setBiome(String biome) { this.biome = biome; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public float getMinTempCelsius() { return minTempCelsius; }
    public void setMinTempCelsius(float minTempCelsius) { this.minTempCelsius = minTempCelsius; }

    public float getOptimalTempCelsius() { return optimalTempCelsius; }
    public void setOptimalTempCelsius(float optimalTempCelsius) { this.optimalTempCelsius = optimalTempCelsius; }

    public float getMaxTempCelsius() { return maxTempCelsius; }
    public void setMaxTempCelsius(float maxTempCelsius) { this.maxTempCelsius = maxTempCelsius; }

    public float getGrowthRate() { return growthRate; }
    public void setGrowthRate(float growthRate) { this.growthRate = growthRate; }

    public float getInitialBiomassDensity() { return initialBiomassDensity; }
    public void setInitialBiomassDensity(float initialBiomassDensity) { this.initialBiomassDensity = initialBiomassDensity; }

    public float getInitialPopulationDensity() { return initialPopulationDensity; }
    public void setInitialPopulationDensity(float initialPopulationDensity) { this.initialPopulationDensity = initialPopulationDensity; }

    public boolean isDiapause() { return diapause; }
    public void setDiapause(boolean diapause) { this.diapause = diapause; }

    public String getHemisphere() { return hemisphere; }
    public void setHemisphere(String hemisphere) { this.hemisphere = hemisphere; }

    public float getSeasonMultiplierSpring() { return seasonMultiplierSpring; }
    public void setSeasonMultiplierSpring(float seasonMultiplierSpring) { this.seasonMultiplierSpring = seasonMultiplierSpring; }

    public float getSeasonMultiplierSummer() { return seasonMultiplierSummer; }
    public void setSeasonMultiplierSummer(float seasonMultiplierSummer) { this.seasonMultiplierSummer = seasonMultiplierSummer; }

    public float getSeasonMultiplierAutumn() { return seasonMultiplierAutumn; }
    public void setSeasonMultiplierAutumn(float seasonMultiplierAutumn) { this.seasonMultiplierAutumn = seasonMultiplierAutumn; }

    public float getSeasonMultiplierWinter() { return seasonMultiplierWinter; }
    public void setSeasonMultiplierWinter(float seasonMultiplierWinter) { this.seasonMultiplierWinter = seasonMultiplierWinter; }

    public TargetCaste getTargetCaste() { return targetCaste; }
    public void setTargetCaste(TargetCaste targetCaste) { this.targetCaste = targetCaste; }

    public HuntMode getHuntMode() { return huntMode; }
    public void setHuntMode(HuntMode huntMode) { this.huntMode = huntMode; }

    public float getKillRate() { return killRate; }
    public void setKillRate(float killRate) { this.killRate = killRate; }

    public float getVisionRange() { return visionRange; }
    public void setVisionRange(float visionRange) { this.visionRange = visionRange; }

    public float getAttackDamage() { return attackDamage; }
    public void setAttackDamage(float attackDamage) { this.attackDamage = attackDamage; }

    public PathogenVector getPathogenVector() { return pathogenVector; }
    public void setPathogenVector(PathogenVector pathogenVector) { this.pathogenVector = pathogenVector; }

    public float getTransmissionR0() { return transmissionR0; }
    public void setTransmissionR0(float transmissionR0) { this.transmissionR0 = transmissionR0; }

    public float getIncubationDays() { return incubationDays; }
    public void setIncubationDays(float incubationDays) { this.incubationDays = incubationDays; }

    public float getMortalityRate() { return mortalityRate; }
    public void setMortalityRate(float mortalityRate) { this.mortalityRate = mortalityRate; }

    public float getProteinYield() { return proteinYield; }
    public void setProteinYield(float proteinYield) { this.proteinYield = proteinYield; }

    public float getSugarHoneydewYield() { return sugarHoneydewYield; }
    public void setSugarHoneydewYield(float sugarHoneydewYield) { this.sugarHoneydewYield = sugarHoneydewYield; }

    public float getLeafBiomassYield() { return leafBiomassYield; }
    public void setLeafBiomassYield(float leafBiomassYield) { this.leafBiomassYield = leafBiomassYield; }

    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
