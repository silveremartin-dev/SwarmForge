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
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(as = DefaultSpecies.class)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public interface Species extends java.io.Serializable, ExtendedSpeciesCapabilitiesContract {

    /**
     * @return Scientific name of the species
     */
    String getScientificName();

    /**
     * @return Common name of the species
     */
    String getCommonName();

    /**
     * @return Taxonomic Genus of the species (e.g. Formica, Apis, Solenopsis)
     */
    default String getGenus() {
        String sciName = getScientificName();
        if (sciName != null && sciName.contains(" ")) {
            return sciName.split(" ")[0];
        }
        return "Formica";
    }

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
     * @return Nominal cruising/foraging walking speed (m/s)
     */
    default float getWalkingSpeed() {
        return getWorkerSpeed();
    }

    /**
     * @return Fast sprint/alarm/attack running speed (m/s)
     */
    default float getRunningSpeed() {
        return Math.max(0.1f, getWorkerSpeed() * 1.65f);
    }

    /**
     * @return 3D flight speed (m/s) for flying hymenoptera or alates
     */
    default float getFlyingSpeed() {
        return isWorkersCanFly() ? Math.max(3.5f, getWorkerSpeed() * 4.0f) : Math.max(1.5f, getWorkerSpeed() * 2.5f);
    }

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
     * @return Innate physiological resistance to pathogen spore germination (0.0 to 1.0)
     */
    default float getPathogenResistance() {
        return 0.5f;
    }

    /**
     * @return Social allogrooming defense efficacy against cuticular ectoparasites and spores (0.0 to 1.0)
     */
    default float getGroomingDefenseEfficacy() {
        return 0.6f;
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

    /**
     * @return Average body length in millimeters across defined castes
     */
    default float getAverageCasteBodyLengthMm() {
        java.util.List<org.swarmforge.core.domain.CasteTemplate> castes = getCastes();
        if (castes == null || castes.isEmpty()) return 5.0f;
        float sum = 0;
        int count = 0;
        for (org.swarmforge.core.domain.CasteTemplate ct : castes) {
            if (ct != null && ct.getBodyLengthMm() > 0) {
                sum += ct.getBodyLengthMm();
                count++;
            }
        }
        return count > 0 ? sum / count : 5.0f;
    }

    /**
     * @return Minimum gallery diameter in millimeters required for the largest caste of this species
     */
    default float getRequiredTunnelDiameterMm() {
        java.util.List<org.swarmforge.core.domain.CasteTemplate> castes = getCastes();
        if (castes == null || castes.isEmpty()) return 6.0f;
        float maxMinDiameter = 4.0f;
        for (org.swarmforge.core.domain.CasteTemplate ct : castes) {
            if (ct != null) {
                maxMinDiameter = Math.max(maxMinDiameter, ct.getMinTunnelDiameterMm());
            }
        }
        return maxMinDiameter;
    }

    enum InsectOrder {
        ANT("🐜 Hymenoptera: Formicidae (Fourmis / Ants)"),
        BEE("🐝 Hymenoptera: Apidae (Abeilles / Bees)"),
        WASP("🐝 Hymenoptera: Vespidae (Guêpes & Frelons / Wasps)"),
        TERMITE("🪵 Blattodea: Termitoidea (Termites)"),
        APHID("🌿 Hemiptera: Aphididae (Pucerons / Aphids)"),
        THRIPS("🌾 Thysanoptera (Thrips gallicoles)"),
        BEETLE("🪲 Coleoptera (Coléoptères sociaux / Passalidae)");

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
    default float getMinTempCelsius() { return 0.0f; }
    default float getMaxTempCelsius() { return 45.0f; }
    default float getOptimalHumidityPercent() { return 80.0f; }
    default float getMinHumidityPercent() { return 25.0f; }
    default float getMaxHumidityPercent() { return 95.0f; }
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

    default boolean canPerformBiostructures() { return false; } // Bridges, Rafts, Bivouacs (Eciton, Solenopsis)
    default boolean canPerformNecrophoresis() { return true; }  // Oleic acid corpse cleanup (Most ants & social bees)
    default boolean canPerformSocialThermoregulation() { return false; } // Endothermic shivering & fanning (Formica, Apis)
    default boolean isUnicolonial() { return false; } // Supercolony CHC tolerance (Linepithema humile)
    default boolean hasMandibularWearPolyethism() { return true; } // Tooth wear age shifts
    default boolean canPerformTandemRunning() { return false; } // Leader-follower recruitment (Temnothorax)
    default boolean canPerformWaggleDance() { return false; } // Polar vector dance (Apis mellifera)
    default boolean canPerformLarvalSalivaryTrophallaxis() { return false; } // Wasp protein-saliva exchange (Vespula/Vespa)
    default boolean hasTermiteGutSymbiosis() { return false; } // Lignocellulose protist digestion (Isoptera)
    default boolean canFarmAphids() { return false; } // Aphid milking and herd protection (Lasius, Formica)
    default boolean canFarmFungus() { return false; } // Symbiotic fungus garden cultivation (Atta, Acromyrmex, Macrotermitinae)
    default boolean hasRoyalPheromoneInhibition() { return true; } // 9-ODA ovary suppression & queen succession
    default boolean canDrumSubstrate() { return false; } // Head-banging substrate acoustic vibrations (Camponotus, Reticulitermes)
    default boolean isPolycalic() { return false; } // Polycalic inter-mound network routing (Formica lugubris)
    default boolean canCollectPropolis() { return false; } // Resin foraging & antimicrobial propolis coating (Apis mellifera)
    default boolean canSewLeavesWithLarvalSilk() { return false; } // Arboreal weaver ant silk sewing (Oecophylla)
    default boolean canWeedFungusGarden() { return false; } // Escovopsis weeding & Pseudonocardia antibiotic application (Atta)
    default boolean canMakeStercoralCement() { return false; } // Soil-saliva-feces plastering (Macrotermes)
    default boolean hasProctodealTrophallaxis() { return false; } // Anal protist transfer (Reticulitermes)
    default boolean canPerformPhragmosis() { return false; } // Flat head-door gallery plugging (Cephalotes, Colobopsis)
    default boolean canPerformEvaporativeCooling() { return false; } // Water droplet hive cooling (Apis mellifera)
    default boolean hasTrapJawMechanism() { return false; } // Elastic catapult mandible snap (Odontomachus)
    default boolean isSlaveMakingSpecies() { return false; } // Dulosis raids on host pupae (Polyergus)
    default boolean canFormLivingBivouac() { return false; } // Interlocking worker suspended nest (Eciton)
    default boolean hasSolarOrientedMound() { return false; } // Asymmetric south-sloping pine needle mound (Formica)
    default boolean canPerformAllogrooming() { return true; } // Mutual grooming & fungal spore removal (Lasius, Apis)
    default boolean canPerformTrembleDance() { return false; } // Receiver recruitment dance when overwhelmed (Apis)
    default boolean hasThermalTrailDecay() { return true; } // Temperature-dependent Q10 trail evaporation (Linepithema)
    default boolean canPerformThoracicIncubation() { return false; } // Thoracic muscle shivering brood incubation (Formica, Apis)
    default boolean canPerformRitualJousting() { return false; } // Non-lethal tournament intimidation (Myrmecocystus)
    default boolean hasTerritorialRepellentPheromone() { return false; } // Inter-species avoidance marking (Solenopsis, Linepithema)
    default boolean canDetectHydrostaticPressure() { return true; } // Barometric flood evacuation (Subterranean ants)
    default boolean isRobberBeeSpecies() { return false; } // Honey theft raids on weak hives (Lestrimelitta)
    default boolean canStridulateRescueCall() { return false; } // Acoustic emergency rescue stridulation (Atta, Pogonomyrmex)
    default boolean isHoneypotStorageCaste() { return false; } // Replete swollen abdominal liquid storage (Myrmecocystus)
    default boolean canPlugContaminatedGalleries() { return false; } // Antiseptic gravel chamber sealing (Lasius)
    default boolean hasOleicAcidThresholdNecrophoresis() { return true; } // Oleic acid oxidation corpse cleanup
    default boolean hasUVPolarizedLightNavigation() { return false; } // Polarized UV sky compass (Cataglyphis)
    default boolean canPerformThermalBalling() { return false; } // 47°C heat oven defense against hornets (Apis cerana)
    default boolean canFormLivingRaft() { return false; } // Hydrophobic worker interlocking flood raft (Solenopsis)
    default boolean canInhabitDomatia() { return false; } // Plant thorn domatia mutualism & foliage pruning (Pseudomyrmex)
    default boolean canSelfIsolateWhenInfected() { return true; } // Voluntary exit from nest upon fungal infection (Formica)
    default boolean canSprayFormicResinDisinfectant() { return false; } // Formic acid + tree resin social disinfectant spray (Formica rufa)
    default boolean canTriggerEmergencySwarming() { return true; } // Colony fission & emergency swarming upon nest loss
    default boolean canConstructClayPillars() { return false; } // Subterranean load-bearing spiral clay pillar construction
    default boolean canDeGermStoredSeeds() { return false; } // Seed embryo destruction to prevent granary sprouting (Messor)
    default boolean canPerformQueenPiping() { return false; } // High-frequency acoustic queen challenging calls (Apis mellifera)
    default boolean canPerformWaterTrophallaxis() { return true; } // High-volume liquid water transfer for 90% RH nest humidity control
    default boolean canEnforceAphidSanitaryCordon() { return false; } // Preventive culling of pathogen-infected aphids in managed herds
    default boolean canFormLivingBridges() { return false; } // Interlocking worker chains over spatial gaps (Eciton army ants)
    default boolean canEmitAcousticPreySurge() { return false; } // Cuticular acoustic stridulation for synchronized prey capture
    default boolean canSortExternalRefusePits() { return true; } // External cemetery sorting and refuse pit quarantine management
    default boolean canCultivateWoodFungus() { return false; } // Termite cellulolytic wood-fungus garden cultivation on dead wood
    default boolean hasEmergencyEscapePheromone() { return true; } // Fast-evacuation alarm trail pheromone during predator invasions
    default boolean canSealQueenChamberWax() { return false; } // Waterproof wax lipid sealing of royal queen cells
    default boolean hasCasteRatioPheromoneInhibition() { return true; } // Pheromonal feedback regulation keeping soldier caste ratio <= 15%
    default boolean canPerformSuctionEscapePosture() { return false; } // Body-clamping suction posture on substrate against bird/lizard strikes
    default boolean canStridulateQueenRecognition() { return false; } // Low-frequency queen stridulation suppressing worker aggression
    default boolean canPerformPulsatileVentilation() { return false; } // Abdominal pumping for active convective nest airflow ventilation
    default boolean canRepairBreachesClay() { return true; } // Rapid clay mortar patching of storm-damaged nest wall breaches
    default boolean hasDepletingTrailPheromone() { return true; } // Dynamic decay rate modulation on exhausting food resource trails
    default boolean canRecycleInviableEggs() { return true; } // Trophic cannibalism of unfertilized or damaged eggs by nurse workers
    default boolean canQuarantineInvasiveParasites() { return false; } // Worker encirclement and physical quarantine of nest mites and beetles
    default boolean canPerformArborealGlidingEscape() { return false; } // Controlled free-fall gliding jump from canopy branches upon predator strike
    default boolean canPerformSolarBroodBasking() { return false; } // Morning solar surface basking with brood for thermal acceleration
    default boolean canHarmonizeChcGestalt() { return true; } // Cuticular hydrocarbon epicuticular lipid exchange for nestmate recognition
    default boolean canConstructCollapsiblePitTraps() { return false; } // Subterranean fragile soil pitfall trap construction around nest perimeter
    default boolean canHarvestDewCondensation() { return true; } // Tarsal dew condensation harvesting on morning vegetation
    default boolean canPerformExoskeletonAntiFungalPatrol() { return true; } // Cuticular acid washing patrol protecting callow exoskeleton
    default boolean canPerformGuardShiftVibrationalWhisper() { return false; } // Sternal acoustic pulse signaling guard shift transitions
    default boolean canConstructThermoregulatedConduits() { return false; } // Dual air-water subterranean ventilation conduit network
    default boolean canRaidToxicPlantResin() { return false; } // Targeted foraging of toxic plant resins for burrowing rodent repellent
    default boolean canApplyDustSubstrateCamouflage() { return false; } // Fine soil dust coating for visual predator camouflage
    default boolean canTransportChainBrood() { return true; } // Interlocked mandible chain brood transport for emergency evacuations
    default boolean hasTrophallacticOvaryInhibition() { return true; } // Oral trophallactic transfer of worker ovary suppression factors
    default boolean canPerformDroughtVibratoDance() { return false; } // Soil-moisture drought vibrato dance recruiting deep soil digging
    default boolean canEncapsulateLargeIntrudersClay() { return true; } // Clay-saliva propolis mummification of large intruder carcasses
    default boolean canConstructPhonicIsolationChambers() { return false; } // Phonic isolation clay chambers dampening acoustic noise around queen
    default boolean canApplyHydrophobicTrailCoating() { return false; } // Lipid film coating on flood-prone lower gallery walls
    default boolean canConsumeFermentedSapAnesthetic() { return false; } // Fermented sap ingestion prior to major inter-colony battles
    default boolean canPerformRelaySeedTransport() { return true; } // Major-to-minor size-graded relay transport of heavy seeds
    default boolean hasPreySizeSelectivePheromones() { return true; } // Distinct chemical trails denoting prey mass for recruitment tuning
    default boolean canDryLarvaeWoodDust() { return true; } // Fine wood dust dusting of damp larvae to prevent mildew growth
    default boolean canPerformFanoutEscapeFormicAcid() { return true; } // 360-degree fan-out escape response upon detecting enemy formic acid
    default boolean canEmitMoundOverheatVibrato() { return false; } // Synchronous vibrato call triggering opening of upper vent chimneys
    default boolean canNourishVirginQueensPreFlight() { return true; } // Targeted hyper-nourishment of virgin queens prior to nuptial flight
    default boolean canPlugHoneyStoresBricks() { return true; } // Emergency soil-brick plugging of honey vaults during wasp raids

    // --- Batch IX Ethological Capabilities (81-90) ---
    default boolean canHuntNocturnalInfrared() { return false; } // Thermal infrared detection of sleeping vertebrate prey during nocturnal foraging
    default boolean canWeaveLarvalSilkCanopyBridges() { return false; } // Inter-canopy silk bridge weaving using larval silk threads
    default boolean canStridulateEggLayingSynchronization() { return false; } // Queen abdominal stridulation triggering synchronized egg-laying across co-queens
    default boolean canPerformAntennalDustGrooming() { return true; } // Tarsal brush grooming of antennal sensilla to maintain chemical acuity
    default boolean canForageSaltCrystalsOsmoregulation() { return false; } // Solid mineral salt crystal foraging for larval osmotic balance
    default boolean canConstructRainEvacuationSiphons() { return false; } // Hydraulic curved siphon conduits for automatic storm water evacuation
    default boolean canAbsorbHostPlantChemicalCamouflage() { return false; } // Cuticular absorption of host tree bark odor for chemical camouflage
    default boolean canDepositSulfurDustAntiMitePatrol() { return false; } // Natural mineral sulfur dusting of brood chambers for parasite control
    default boolean canDanceVibratoHatchingEnthusiasm() { return true; } // Synchronized drumming dance announcing major prey hatching events
    default boolean canResinMummifyNymphalChambers() { return false; } // Antiseptic resin sealing of queen pupal cocoons during metamorphosis

    // --- Batch X Ethological Capabilities (91-100) ---
    default boolean canExcavatePitfallTraps() { return false; } // Funnel pitfall trap excavation in loose sand around nest perimeter
    default boolean canSynthesizeGlycerolCryoprotection() { return true; } // Metabolic glycerol synthesis prior to winter freezing temperatures
    default boolean canTransportInjuredPheromonalStretcher() { return true; } // Rescue and mandible transport of injured nestmates to hospital chambers
    default boolean canRaidAbandonedWaxVaults() { return false; } // Scavenging of wax and honey stores from abandoned hives
    default boolean canPerformRitualMandibularWrestling() { return true; } // Non-lethal ritual wrestling matches establishing reproductive dominance
    default boolean canPerformPulsedAirConvectiveVentilation() { return false; } // Synchronized abdominal pumping creating forced air circulation in deep galleries
    default boolean canCultivateStreptomycesAntibiotics() { return true; } // Cultivation of Streptomyces cuticular bacteria producing antifungals
    default boolean canNavigatePolarizedTwilightUV() { return true; } // Navigation using twilight UV sky polarization patterns
    default boolean canSnapTrapMandiblesCatapult() { return false; } // Elastic energy release in mandibles for catapult defense jumps
    default boolean canPerformPedestrianSwarmBudding() { return true; } // Pedestrian column migration with complete brood and resource transfer

    /**
     * Queen oviposition rate (eggs / day).
     */
    default float getOvipositionRate() {
        return 15.0f;
    }

    /**
     * Returns a BitSet representing all enabled capability flags for high-performance bitwise operations.
     */
    default java.util.BitSet getCapabilitiesBitSet() {
        return new java.util.BitSet();
    }

    // ── Extensible Custom & Plugin Attributes ─────────────────────────────────
    /**
     * Generic dynamic attributes map for plugin extensibility and novel sensory/motor parameters.
     */
    default java.util.Map<String, Object> getCustomAttributes() { return java.util.Collections.emptyMap(); }
    default Object getCustomAttribute(String key, Object defaultValue) {
        return getCustomAttributes().getOrDefault(key, defaultValue);
    }
}
