/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Interface for species definitions.
 * Each species defines physical characteristics, lifecycle, and behavior
 * parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS, include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Species {

    /**
     * @return Scientific name of the species
     */
    String getScientificName();

    /**
     * @return Common name of the species
     */
    String getCommonName();

    /**
     * @return Average lifespan in simulation ticks for workers
     */
    int getWorkerLifespan();

    /**
     * @return Average lifespan in simulation ticks for queens
     */
    int getQueenLifespan();

    /**
     * @return Movement speed for workers
     */
    float getWorkerSpeed();

    /**
     * @return View distance for detecting food/threats
     */
    float getViewDistance();

    /**
     * @return Typical colony size at maturity
     */
    int getTypicalColonySize();

    /**
     * @return Whether this species forms mega-colonies
     */
    default boolean formsMegaColonies() {
        return false;
    }

    /**
     * @return Aggression level (0.0 to 1.0). High aggression leads to more combat.
     */
    default float getAggression() {
        return 0.3f; // Default low aggression
    }

    /**
     * @return Metabolism rate (multiplier). Higher means faster hunger/fatigue.
     */
    default float getMetabolism() {
        return 1.0f;
    }

    /**
     * @return Combat strength (damage per hit).
     */
    default float getStrength() {
        return 5.0f;
    }

    /**
     * @return Set of resource types this species forages for to bring to the
     * 
     *         colony.
     */
    default java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        // Default to common ants
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.SEED,
                org.swarmforge.core.domain.ResourceType.NECTAR);
    }

    /**
     * @return List of available caste templates for this species.
     */
    default java.util.List<org.swarmforge.core.domain.CasteTemplate> getCastes() {
        return java.util.Collections.emptyList();
    }

    enum InsectOrder {
        ANT("🐜 Formicidae (Fourmi)"),
        BEE("🐝 Apidae (Abeille)"),
        WASP("🐝 Vespidae (Guêpe/Frelon)"),
        TERMITE("🪲 Isoptera (Termite)"),
        APHID("🌿 Aphididae (Puceron à soldats)"),
        THRIPS("🌾 Thysanoptera (Thrips gallicole)"),
        BEETLE("🌲 Coleoptera (Scolyte du bois)");

        public final String label;
        InsectOrder(String label) { this.label = label; }
    }

    /**
     * @return The taxonomic order / eusocial clade of this species.
     */
    default InsectOrder getInsectOrder() {
        return InsectOrder.ANT;
    }

    /**
     * @return Ecological role category of this species (Eusocial Primary vs. Accessory Fauna).
     */
    default SpeciesCategory getCategory() {
        return SpeciesCategory.EUSOCIAL_PRIMARY;
    }

    /**
     * Configure an individual of this species (set stats, brain, etc).
     */
    default void configureIndividual(org.swarmforge.core.domain.Individual individual) {
        // Default: do nothing or basic setup
    }

    default String getDescription() { return ""; }
    default String getInsectType() { return "ANT"; }
    default String getQueenCountMode() { return "MONOGYNE"; }
    default int getQueenCount() { return 1; }
    default boolean isHasKing() { return false; }
    default int getKingLifespan() { return 15000; }
    default float getQueenEggLayingRate() { return 15.0f; }
    default String getNuptialFlightType() { return "AERIAL_SWARM"; }
    default int getEggStageDuration() { return 300; }
    default int getLarvaStageDuration() { return 600; }
    default int getPupaStageDuration() { return 500; }
    default String getLarvaDietRequirement() { return "HIGH_PROTEIN_MEAT"; }
    default boolean isWorkersCanFly() { return false; }
    default String getPrimaryDiet() { return "SUGARS_NECTAR"; }
    default String getSecondaryDiet() { return "INSECTS_MEAT"; }
    default float getDailyFoodConsumption() { return 0.5f; }
    default float getWaterRequirement() { return 0.2f; }
    default String getNestType() { return "UNDERGROUND_BURROW"; }
    default float getOptimalTempCelsius() { return 24.0f; }
    default float getMinTempCelsius() { return 10.0f; }
    default float getMaxTempCelsius() { return 38.0f; }
    default float getTerritoriality() { return 0.5f; }
    default String getVenomType() { return "NONE"; }

    // ── Advanced Sensory Systems (SI Compliant) ──────────────────────────────
    /**
     * Magnetoreception sensitivity (µT resolution).
     * Used by termites (*Amitermes meridionalis*, *Macrotermes*, *Reticulitermes*)
     * to perceive Earth's geomagnetic field inclination and intensity for mound/gallery orientation.
     */
    default boolean hasMagnetoreception() { return false; }
    default float getMagnetoreceptionSensitivity() { return 5.0f; } // µT sensitivity threshold

    /**
     * Thermoreception (Thermal gradient detection sensitivity in °C / K).
     * Used by subterranean colonies to navigate thermal gradients towards optimal incubation chambers.
     */
    default float getThermoreceptionSensitivity() { return 0.5f; } // °C gradient threshold

    /**
     * Chemoreception / Gas sensing sensitivity (CO2, O2, N2O ppm thresholds).
     * Used to detect hypercapnia (>2.5% CO2) and trigger ventilation shaft excavation.
     */
    default float getGasSensitivityCo2Ppm() { return 400.0f; } // CO2 ppm detection threshold

    /**
     * Vision / Photoreception properties (Light level detection, compound eye visual acuity).
     */
    default float getVisualAcuity() { return 1.0f; } // Visual acuity multiplier
    default float getMinLightLevelThreshold() { return 0.05f; } // Minimum light level needed for visual navigation

    /**
     * Substrate Vibration Perception (Johnston's organ & subgenual organ).
     * Used for drumming alarm signals (termites/carpenter ants) & waggle dance acoustics (honeybees).
     */
    default boolean hasSubstrateVibrationSensing() { return true; }
    default float getVibrationSensitivityDb() { return 10.0f; } // dB vibration threshold

    /**
     * Hygroreception (Humidity gradient sensing).
     * Used for brood chamber positioning and avoiding desiccation.
     */
    default boolean hasHygroreception() { return true; }
    default float getHygroreceptionSensitivityPercent() { return 2.0f; } // % RH gradient threshold

    /**
     * Electro-reception / Atmospheric Electrostatic Charge Sensing.
     * Used by bees/wasps to sense flower charges & electrostatic storm building.
     */
    default boolean hasElectrosensing() { return false; }
    default float getElectroceptionSensitivityVolts() { return 50.0f; } // V/m electric field sensitivity

    /**
     * Sky UV Polarized Light Navigation (Dorsal Rim Area & Ocelli).
     * Used by bees, wasps, and desert ants (Cataglyphis) for dead reckoning homing.
     */
    default boolean hasPolarizedLightNavigation() { return false; }

    // ── Biomechanical & Motor Systems (SI Compliant) ─────────────────────────
    /**
     * Asynchronous Wingbeat Frequency (Hz) for flying castes (bees, wasps, alates).
     */
    default float getWingbeatFrequencyHz() { return 200.0f; } // Hz

    /**
     * Hovering flight capability (Stationary aerial maneuvering for bees & wasps).
     */
    default boolean hasHoveringCapability() { return false; }

    /**
     * Maximum payload carrying ratio relative to body mass.
     * Ants: ~10x to 50x body mass; Bees: ~0.8x to 1.5x (nectar/pollen crop).
     */
    default float getMaxCarryingPayloadRatio() { return 5.0f; }

    /**
     * Mandibular biting / shearing force (MPa).
     * Wood-cutting termites (~20 MPa), Paper wasps (~15 MPa), Leafcutter ants (~30 MPa).
     */
    default float getMandibularBitingForceMPa() { return 15.0f; }

    /**
     * Suicidal chemical explosion (Autothysis) for colony defense.
     */
    default boolean hasAutothysis() { return false; }

    /**
     * Tarsal Arolia Adhesion for inverted ceiling walking and smooth surface climbing.
     */
    default boolean hasSubstrateAdhesionArolia() { return true; }

    // ── Extensible Custom & Plugin Attributes ─────────────────────────────────
    /**
     * Generic dynamic attributes map for plugin extensibility and novel sensory/motor parameters.
     */
    default java.util.Map<String, Object> getCustomAttributes() { return java.util.Collections.emptyMap(); }
    default Object getCustomAttribute(String key, Object defaultValue) {
        return getCustomAttributes().getOrDefault(key, defaultValue);
    }
}
