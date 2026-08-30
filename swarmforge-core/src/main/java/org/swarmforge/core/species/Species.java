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
public interface Species extends java.io.Serializable {

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
        ANT("🐜 Formicidae (Ant)"),
        BEE("🐝 Apidae (Bee)"),
        WASP("🐝 Vespidae (Wasp/Hornet)"),
        TERMITE("🪲 Isoptera (Termite)"),
        APHID("🌿 Aphididae (Soldier Aphid)"),
        THRIPS("🌾 Thysanoptera (Gall Thrips)"),
        BEETLE("🌲 Coleoptera (Bark Beetle)");

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

    // --- Batch XI & XII Eusocial/Subsocial Arthropod Capabilities (101-120) ---
    default boolean canTrophallaxisProtozoa() { return false; } // Anal trophallactic transfer of flagellate protozoa essential for cellulose digestion in termites
    default boolean canSquirtNasuteChemical() { return false; } // Specialized nasus nozzle squirt of sticky terpenoid defense glue
    default boolean canMasticatePaperPulpCarton() { return false; } // Scraping and saliva mastication of weathered wood fibers for carton paper nest cell building
    default boolean canHarvestLarvalSalivaDroplets() { return false; } // Solicit nutrient-rich amino acid saliva droplets from larvae in exchange for meat
    default boolean canApplyPedicelAntRepellent() { return false; } // Glandular organ secretion coating on nest pedicel stalk to block ant invasion
    default boolean canRecognizeFacialVisualPatterns() { return false; } // Visual recognition of unique facial markings maintaining wasp dominance hierarchy
    default boolean canPerformBuzzPollination() { return false; } // High-frequency thorax muscular vibration (sonication) to dislodge pollen
    default boolean canIncubateBroodAbdominalHeat() { return false; } // Pressing hairless ventral abdomen onto brood cells to transfer metabolic heat
    default boolean canStabFrontalHornsAphid() { return false; } // Sterile soldier aphid horn stabbing and hemolymph gluing of ladybug predators
    default boolean canSqueezeGallIntrudersThrips() { return false; } // Eusocial thrips soldier raptorial foreleg crushing of gall invaders
    default boolean canSnapClawAcousticShockwave() { return false; } // Eusocial shrimp giant chela cavitation snap producing acoustic shockwaves in sponge canals
    default boolean canStridulatePassalidParentalCare() { return false; } // Passalid beetle adult/larval stridulatory communication during wood frass feeding
    default boolean canPerformPhysogastricPeristalsis() { return false; } // Termite queen massive abdominal peristalsis supporting high daily egg laying
    default boolean canOrientMagneticMound() { return false; } // Building wedge-shaped mounds aligned along geomagnetic lines for thermoregulation
    default boolean canEmitHornetGroupAlarmPheromone() { return false; } // Venom spray alarm recruitment triggering mass coordinated hornet raids
    default boolean canWeaveStenogastrinePaperJelly() { return false; } // Mixing salivary gelatinous secretions with fine plant fibers for hair-thin suspended nests
    default boolean canInoculateFungalCombTermite() { return false; } // Termite cultivation of Termitomyces fungal gardens on chewed fecal pellets
    default boolean canDrumAbdomenWaspCellRim() { return false; } // Rhythmic abdominal drumming against paper cell walls to alert nestmates
    default boolean canConstructNectarWaxPots() { return false; } // Molding urn-shaped wax pots near nest entrance for honey storage
    default boolean canPerformMaternalShieldGuarding() { return false; } // Subsocial parent bug maternal body shield guarding over egg clutch

    // --- Batch XIII Arthropod & Eusocial Capabilities (121-140) ---
    default boolean canWeaveCommunalSpiderSilk() { return false; } // Cooperative spinning of massive capture webs in social spiders
    default boolean canFormProcessionarySilkTrail() { return false; } // Single file procession guided by silk thread and abdominal trail pheromones
    default boolean canConstructClayVaultArches() { return false; } // Subterranean gothic arches built from clay-saliva pellets
    default boolean canDeliverStenogastrinePapFood() { return false; } // Secretion of proteinaceous pap droplets into egg cells before hatching
    default boolean canPlasterFrassGalleryWalls() { return false; } // Plastering gallery walls with frass to prevent resin inundation
    default boolean canLearnTrapliningFlightRoutes() { return false; } // Traplining spatial memory trajectories for bumblebee foraging
    default boolean canCoolNestWaterRegurgitation() { return false; } // Water droplet regurgitation on cell caps for evaporative cooling
    default boolean canEjectHoneydewSignalingDroplets() { return false; } // Abdominal flicks ejecting honeydew to recruit mutualist ants
    default boolean canSnapMandibleAcousticAlarm() { return false; } // Mandible snapping against wooden gallery walls creating acoustic alarms
    default boolean canPerformEggLickingGrooming() { return false; } // Maternal mouthpart licking and salivary coating of earwig eggs
    default boolean canConstructChaffGarbageDunes() { return false; } // Dumping seed chaff refuse in specialized exterior crescent dunes
    default boolean canDrumAntennaeLarvalStimulation() { return false; } // Antennal drumming on larval heads soliciting saliva release
    default boolean canFormLeafPullingChains() { return false; } // Interlocked worker body chains pulling tree leaves together for weaver nest assembly
    default boolean canApplySalivaryCementMoistureSeal() { return false; } // Water-resistant salivary-clay mortar sealing gallery breaches against desiccation
    default boolean canForageSubZeroBumblebee() { return false; } // Flight muscle shivering allowing Arctic bumblebee nectar foraging at 0°C
    default boolean canRepairGallSubstratalSecretion() { return false; } // Abdominal secretion of growth stimulants to repair thrips gall cracks
    default boolean canTrophallaxisPassalidWoodFrass() { return false; } // Oral regurgitation of pre-digested wood pulp to passalid beetle grubs
    default boolean canPerformCrècheRegurgitationSpider() { return false; } // Maternal liquefied gut regurgitation feeding of social spider crèches
    default boolean canBlockRoyalChamberSentry() { return false; } // Interlocked soldier head capsules blocking access to termite royal chamber
    default boolean canEmitParentBugAlarmGathering() { return false; } // Abdominal alarm pheromone burst signaling nymphs to cluster under maternal shield

    // --- Batch XIV Capabilities (141-160) ---
    default boolean canApplyBeeBreadHydrophobicCoating() { return false; } // Lipid coating on stored pollen/bee-bread preventing mold growth
    default boolean canBindParasitesWithSilk() { return false; } // Collective silk tethering and immobilization of parasitic beetles inside the nest
    default boolean canEmitSubstrateObstacleVibrato() { return false; } // Substrate vibration warnings to foraging columns about fallen obstacles or cave-ins
    default boolean canPerformFormicAcidBathGrooming() { return false; } // Mutual cuticular spraying and bath grooming with formic acid for disinfection after rival battles
    default boolean canExcavateVerticalDrainageShafts() { return false; } // Emergency digging of vertical drainage shafts during water table breaches or nest flooding
    default boolean canIngestPhenolicResinMedication() { return false; } // Selective ingestion of phenolic plant resins to boost immune defenses during fungal outbreaks
    default boolean canConstructSphagnumMoistureDomes() { return false; } // Assembling living sphagnum moss onto nest mounds for atmospheric humidity capture
    default boolean canMarkParasitizedCadaverRepellent() { return false; } // Depositing rejection pheromones on sporulating infected pupal corpses
    default boolean canDrumNuptialFlightSynchronization() { return false; } // Collective substrate drumming triggering simultaneous alate takeoff for nuptial flights
    default boolean canHarvestCuticularWaterCondensation() { return false; } // Harvesting micro-droplets condensed on abdominal cuticular setae during morning fogs
    default boolean canStridulateLarvalHungerChirp() { return false; } // Larval stridulation chirps soliciting wood frass regurgitation from adult guardians
    default boolean canConstructThermalChimneyFlues() { return false; } // Vertical clay chimney flues drawing cool ambient air into subterranean fungus chambers
    default boolean canDepositLarvalFoodSalivaDrop() { return false; } // Depositing concentrated nectar-saliva droplets in empty cells as emergency larval food
    default boolean canApplyEggMassMucilageEnvelope() { return false; } // Coating egg clutches in protective antimicrobial mucilage to block parasitoid wasps
    default boolean canWeaveSilkPavilionAphidShelter() { return false; } // Weaving silk pavilions over honeydew aphid herds to shield them from rain and predators
    default boolean canFormHotBallThermalDefense() { return false; } // Thermo-balling around hornet intruders raising temperature to 47°C to cook predators alive
    default boolean canPerformFontanelleAutothysis() { return false; } // Age-dependent abdominal autothysis rupture releasing toxic blue copper protein crystals
    default boolean canSensePreySignalWireTripping() { return false; } // Sensing tension changes along communal draglines to coordinate simultaneous multi-spider strikes
    default boolean canMutilateSeedRadicles() { return false; } // Biting off seed radicles to prevent stored seeds from germinating inside moist underground granaries
    default boolean canBiteNectarTheftHoles() { return false; } // Piercing flower corolla bases to rob nectar directly without flower pollination

    // --- Batch XV Capabilities (161-180) ---
    default boolean canSowFungalSporeCombs() { return false; } // Sowing fresh fungal spores on newly prepared chewed-wood combs
    default boolean canHarnessLarvalSilkCocoon() { return false; } // Holding living larvae like silk glue-guns to stitch nest leaves together
    default boolean canFormBiomechanicalBivouac() { return false; } // Interlocking legs and claws to build living hanging walls and thermal nest bivouacs
    default boolean canPerformBuzzPollinationSonication() { return false; } // Vibrating flight muscles at high frequency to shake pollen free from anthers
    default boolean canRegurgitateEarwigMaternalFood() { return false; } // Regurgitating crop contents to feed first-instar nymphs inside maternal chambers
    default boolean canRecognizeWaspFacialPatterns() { return false; } // Visual recognition of individual facial markings to enforce dominance hierarchies
    default boolean canFireShrimpAcousticCannon() { return false; } // Firing high-speed water jets creating cavitation bubbles against sponge intruders
    default boolean canDuetPassalidSubstrateVibration() { return false; } // Adult-pupal vibrational acoustic duetting coordinating gallery movements
    default boolean canTurnGranarySeedsAeration() { return false; } // Periodically moving and turning stored seed stockpiles to prevent humidity accumulation
    default boolean canEncodeWaggleDanceSunCompass() { return false; } // Transducing waggle dance angles relative to gravity into sun-compass flight vectors
    default boolean canDigSubterraneanClayAqueducts() { return false; } // Digging deep vertical wells down to water tables to bring moisture up to dry mounds
    default boolean canFireFormicAcidArtilleryJet() { return false; } // Arching gaster forward to spray formic acid jets up to 30 cm against vertebrate predators
    default boolean canEjectGarbageChuteRefuse() { return false; } // Dropping prey carcasses and molted cuticles through dedicated vertical silk chute funnels
    default boolean canFanWingsForBroodThermoregulation() { return false; } // Fanning wings over brood comb cells during high ambient heat to cool larvae
    default boolean canPlugGallWithChitinousTube() { return false; } // Using flattened sclerotized abdominal tips to plug narrow gall entrance tubes
    default boolean canCoatWaspPedicelAntRepellent() { return false; } // Applying abdominal gland secretions onto nest attachment pedicels to deter climbing ants
    default boolean canSquirtNasuteViscousResin() { return false; } // Squirting sticky terpenoid sticky threads from frontal nozzle to entangle enemy ants
    default boolean canSqueezeIntrudersWithForelegs() { return false; } // Squeezing predatory fly larvae against plant stems using enlarged spinous forelegs
    default boolean canShieldEggsFromParasitoidWasps() { return false; } // Tilting body armor to block parasitoid wasp ovipositors from reaching egg clutches
    default boolean canPlasterWoodWallGallery() { return false; } // Mixing wood dust with salivary secretions to seal damaged gallery tunnels

    // --- Batch XVI Capabilities (181-200) ---
    default boolean canShearLeafCrescentMandible() { return false; } // Rhythmic shearing of foliage into transportable crescent leaf discs
    default boolean canShieldSwarmCoreHeat() { return false; } // Forming outer mantle layers of cool bees around the warm core during swarm cluster flight
    default boolean canPerformQueenPhysogastricPeristalsis() { return false; } // Rhythmic abdominal peristalsis producing 30,000 eggs per day
    default boolean canWeaveSocialSilkHammock() { return false; } // Building multi-layered silk tent hammocks for group thermal buffering
    default boolean canPackCorbiculaPollenBaskets() { return false; } // Moistening pollen with nectar and combing it into hind tibia corbiculae
    default boolean canScrapeWoodPulpCarton() { return false; } // Scraping weathered wood fibres with mandibles to produce paper pulp carton
    default boolean canLayTrophicNourishmentEggs() { return false; } // Laying unfertilized trophic eggs to feed queen and young larvae during colony founding
    default boolean canNavigatePolarizedLightCompass() { return false; } // Using dorsal rim area ommatidia to decode UV atmospheric polarization patterns for path integration
    default boolean canBuryFungalWasteInGallery() { return false; } // Sealing senescent fungal comb waste in dead-end gallery vaults
    default boolean canChewSeedHuskBreadPulp() { return false; } // Chewing seed starch mixed with amylase saliva to produce ant bread
    default boolean canWrapPreyInCommunalSilk() { return false; } // Multi-spider wrapping of large captured grasshoppers in dense silk sheets
    default boolean canSealNestGapsWithPropolis() { return false; } // Collecting tree propolis resins to caulk micro-cracks and sterilize hive walls
    default boolean canSynchronizeSoldierAlarmDrumming() { return false; } // Phase-locking head drumming across hundreds of soldiers to amplify substrate vibration
    default boolean canPerformDominanceMounting() { return false; } // Dominance mounting and antennal drumming to suppress worker ovarian development
    default boolean canLapNectarTongueExtension() { return false; } // Extending glossal tongue into deep floral nectaries to lap high-viscosity nectar
    default boolean canSecreteGallClosingFluid() { return false; } // Secreting liquid waxy droplets to plug natural openings in plant galls against predators
    default boolean canGroomNymphCuticularSurface() { return false; } // Licking cuticular surfaces of newly molted nymphs to remove exuviae and pathogens
    default boolean canExcavateGardenWasteChambers() { return false; } // Digging deep subterranean waste chambers for toxic spent fungal substrate
    default boolean canExchangeRoyalPairGrooming() { return false; } // Mutual grooming and pheromonal exchange between king and queen
    default boolean canTriggerUniversalEmergencyEvacuation() { return false; } // Colony-wide emergency evacuation trigger evacuating brood and queen during catastrophic collapse

    // --- Batch XVII Capabilities (201-220) ---
    default boolean canStoreNectarAsHoneypotReplete() { return false; } // Distending gaster with liquid nectar as hanging living honeypot repletes
    default boolean canFormFloatingAntRaft() { return false; } // Interlocking bodies with hydrophobic cuticles to form floating rafts carrying queen and brood
    default boolean canConstructMudResinEntranceFunnel() { return false; } // Building trumpet-shaped mud and resin entrance tubes guarded by specialized soldiers
    default boolean canExcavateHibernationBurrow() { return false; } // Solitary fertilized autumn queen digging subterranean overwintering hibernacula
    default boolean canInoculateLeafPulpEnzymes() { return false; } // Micro-masticating leaf margins into fine pulp and applying digestive enzymes before comb insertion
    default boolean canPerformGamergateDominanceTournament() { return false; } // Gamergate physical dominance tournaments to establish reproductive status in queenless colonies
    default boolean canFeedOnLarvalHemolymphDracula() { return false; } // Non-fatally puncturing larval integument to drink hemolymph droplets during food scarcity
    default boolean canFormTarsalFrictionBridge() { return false; } // Forming multi-individual tensile bridges pulling heavy tree branches together using tarsal adhesive pads
    default boolean canTransportWaterInMandibleDroplet() { return false; } // Transporting surface tension water droplets trapped between mandibles for brood hydration
    default boolean canStiltWalkThermalRegim() { return false; } // Raising body high on long legs ("stilt walking") to cool off above 50°C desert sand
    default boolean canPerformAntiPredatorShimmeringWave() { return false; } // Synchronized abdomen flipping creating visible shimmering waves across open-nest comb curtain
    default boolean canDouseNestWaterCooling() { return false; } // Carrying water droplets in crop and spitting them on comb cells combined with fanning to evaporatively cool nest
    default boolean canAerateFungalCombChambers() { return false; } // Chewing micro-perforations through clay chamber walls to maintain optimal CO2 exchange for fungus gardens
    default boolean canFeedLarvaeExuviaRecycling() { return false; } // Feeding molted chitinous exuviae back to larvae to recycle essential nitrogenous nutrients
    default boolean canGroomLeafPulpParasitesMinim() { return false; } // Tiny minim workers riding on harvested leaf discs to clean parasitic phorid fly eggs off leaf-cutters
    default boolean canCamouflageWebWithPlantDebris() { return false; } // Weaving dry twigs, leaves, and prey husks into outer silk web walls to disguise the nest from predators
    default boolean canCockGasterFormicAcidRepellent() { return false; } // Arching heart-shaped gaster overhead to deposit defensive venom droplets on intruders
    default boolean canMilkAphidHoneydewStroking() { return false; } // Rhythmic antennal stroking of aphid abdomens to solicit honeydew excretion
    default boolean canClusterSolarHeatCollector() { return false; } // Workers basking in morning sunlight on mound surfaces and carrying absorbed body heat back down into brood chambers
    default boolean canSerializeGlobalEthologicalBitSet() { return false; } // Compact binary BitSet state serialization supporting ultra-high throughput state sync

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
