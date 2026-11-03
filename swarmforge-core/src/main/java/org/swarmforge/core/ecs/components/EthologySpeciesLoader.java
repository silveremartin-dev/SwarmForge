/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecs.components;

import org.swarmforge.core.species.Species;
import static org.swarmforge.core.ecs.components.EthologyComponent.*;

/**
 * Helper responsible for loading ethological capability bitmasks into EthologyComponent from Species.
 */
public final class EthologySpeciesLoader {

    private EthologySpeciesLoader() {
    }

    public static void load(EthologyComponent eth, Species s) {
        if (eth == null || s == null) return;
        long c0 = 0L, c1 = 0L, c2 = 0L, c3 = 0L;



        // Word 0: Navigation, Trophallaxis, Sanitation, Defense, Reproduction, Signals
        if (s.hasMagnetoreception()) c0 |= W0_MAGNETORECEPTION;
        if (s.hasUVPolarizedLightNavigation()) c0 |= W0_UV_POLARIZED_NAV;
        if (s.canPerformWaggleDance()) c0 |= W0_WAGGLE_DANCE;
        if (s.canEncodeWaggleDanceSunCompass()) c0 |= W0_WAGGLE_DANCE_SUN_COMPASS;
        if (s.canPerformTandemRunning()) c0 |= W0_TANDEM_RUNNING;
        if (s.canStiltWalkThermalRegim()) c0 |= W0_DESERT_ANT_STILT_WALKING;
        if (s.canPerformArborealGlidingEscape()) c0 |= W0_ARBOREAL_GLIDING_ESCAPE;
        if (s.canLearnTrapliningFlightRoutes()) c0 |= W0_TRAPLINE_FLIGHT_ROUTE;
        if (s.canNavigatePolarizedTwilightUV()) c0 |= W0_POLARIZED_TWILIGHT_UV_NAV;
        if (s.canNavigatePolarizedLightCompass()) c0 |= W0_POLAR_LIGHT_COMPASS;
        c0 |= W0_TROPHALLAXIS; // Universal trophallaxis baseline
        if (s.hasProctodealTrophallaxis()) c0 |= W0_PROCTODEAL_TROPHALLAXIS;
        if (s.canPerformLarvalSalivaryTrophallaxis()) c0 |= W0_LARVAL_SALIVARY_TROPHALLAXIS;
        if (s.canPerformWaterTrophallaxis()) c0 |= W0_WATER_TROPHALLAXIS;
        if (s.canLayTrophicNourishmentEggs()) c0 |= W0_TROPHIC_EGG_NOURISHMENT;
        if (s.canFeedOnLarvalHemolymphDracula()) c0 |= W0_DRACULA_ANT_LARVAL_HEMOLYMPH;
        if (s.canHarvestLarvalSalivaDroplets()) c0 |= W0_LARVAL_SALIVA_HARVESTING;
        if (s.canDeliverStenogastrinePapFood()) c0 |= W0_STENOGASTRINE_PAP_FOOD;
        if (s.canPerformAllogrooming()) c0 |= W0_ALLOGROOMING;
        if (s.canPerformFormicAcidBathGrooming()) c0 |= W0_FORMIC_ACID_BATH_GROOMING;
        if (s.canPerformAntennalDustGrooming()) c0 |= W0_ANTENNAL_DUST_GROOMING;
        if (s.canGroomLeafPulpParasitesMinim()) c0 |= W0_MINIM_LEAF_PARASITE_GROOMING;
        if (s.canSowFungalSporeCombs()) c0 |= W0_FUNGAL_SPORE_COMB;
        if (s.canPerformExoskeletonAntiFungalPatrol()) c0 |= W0_EXOSKELETON_ANTIFUNGAL_PATROL;
        if (s.canDepositSulfurDustAntiMitePatrol()) c0 |= W0_SULFUR_DUST_ANTIMITE_PATROL;
        if (s.hasAutothysis()) c0 |= W0_AUTOTHYSIS;
        if (s.canPerformFontanelleAutothysis()) c0 |= W0_FONTANELLE_AUTOTHYSIS;
        if (s.hasTrapJawMechanism()) c0 |= W0_TRAP_JAW;
        if (s.canSnapTrapMandiblesCatapult()) c0 |= W0_TRAP_MANDIBLE_CATAPULT;
        if (s.canFireFormicAcidArtilleryJet()) c0 |= W0_FORMIC_ACID_ARTILLERY_JET;
        if (s.canPerformFanoutEscapeFormicAcid()) c0 |= W0_FANOUT_ESCAPE_FORMIC_ACID;
        if (s.canSquirtNasuteChemical()) c0 |= W0_NASITE_CHEMICAL_SQUIRT;
        if (s.canSquirtNasuteViscousResin()) c0 |= W0_NASITE_VISCOUS_RESIN_SQUIRT;
        if (s.canSprayFormicResinDisinfectant()) c0 |= W0_RESIN_SPRAY;
        if (s.canCockGasterFormicAcidRepellent()) c0 |= W0_ACROBAT_ANT_GASTER_VENOM;
        if (s.canFormHotBallThermalDefense() || s.canPerformThermalBalling()) c0 |= W0_HOT_BALL_THERMAL_DEFENSE;
        if (s.canPerformSuctionEscapePosture()) c0 |= W0_SUCTION_ESCAPE;
        if (s.hasRoyalPheromoneInhibition()) c0 |= W0_ROYAL_INHIBITION;
        if (s.hasCasteRatioPheromoneInhibition()) c0 |= W0_CASTE_RATIO_INHIBITION;
        if (s.hasTrophallacticOvaryInhibition()) c0 |= W0_TROPHALLACTIC_OVARY_INHIB;
        if (s.canHarmonizeChcGestalt()) c0 |= W0_CHC_GESTALT_HARMONIZATION;
        if (s.isUnicolonial()) c0 |= W0_UNICOLONIALITY;
        if (s.canPerformGamergateDominanceTournament()) c0 |= W0_GAMERGATER_DOMINANCE;
        if (s.canPerformQueenPiping()) c0 |= W0_QUEEN_PIPING;
        if (s.canStridulateQueenRecognition()) c0 |= W0_QUEEN_RECOGNITION_STRIDULATION;
        if (s.canStridulateEggLayingSynchronization()) c0 |= W0_EGG_LAYING_SYNC_STRIDULATION;
        if (s.canDrumNuptialFlightSynchronization()) c0 |= W0_NUPTIAL_FLIGHT_DRUMMING;
        if (s.canNourishVirginQueensPreFlight()) c0 |= W0_VIRGIN_QUEEN_PRE_FLIGHT;
        if (s.canSealQueenChamberWax()) c0 |= W0_QUEEN_WAX_SEALING;
        if (s.canExcavateHibernationBurrow()) c0 |= W0_QUEEN_HIBERNATION_BURROW;
        if (s.canRecycleInviableEggs()) c0 |= W0_EGG_CANNIBALISM;
        if (s.canPerformPedestrianSwarmBudding()) c0 |= W0_PEDESTRIAN_SWARM_BUDDING;
        if (s.canTriggerEmergencySwarming()) c0 |= W0_EMERGENCY_SWARMING;
        if (s.canDrumSubstrate()) c0 |= W0_SUBSTRATE_ACOUSTIC;
        if (s.canStridulateRescueCall()) c0 |= W0_STRIDULATION_RESCUE;
        if (s.canPerformTrembleDance()) c0 |= W0_TREMBLE_DANCE;
        if (s.hasEmergencyEscapePheromone()) c0 |= W0_ESCAPE_PHEROMONE;
        if (s.isPolycalic()) c0 |= W0_POLYCALIC_NETWORK;
        if (s.hasDepletingTrailPheromone()) c0 |= W0_DEPLETION_TRAIL;
        if (s.canTransportInjuredPheromonalStretcher()) c0 |= W0_INJURED_PHEROMONE_STRETCHER;
        if (s.canDanceVibratoHatchingEnthusiasm()) c0 |= W0_HATCHING_ENTHUSIASM_VIBRATO;
        if (s.canPerformGuardShiftVibrationalWhisper()) c0 |= W0_GUARD_SHIFT_VIBRATIONAL_WHISPER;

        // Word 1: Nest Construction, Thermoregulation, Foraging
        if (s.canPlugContaminatedGalleries()) c1 |= W1_GRAVEL_PLUGGING;
        if (s.canMakeStercoralCement()) c1 |= W1_STERCORAL_CEMENT;
        if (s.canConstructClayPillars()) c1 |= W1_CLAY_PILLAR;
        if (s.canConstructClayVaultArches()) c1 |= W1_CLAY_VAULT_ARCH;
        if (s.canAerateFungalCombChambers()) c1 |= W1_CLAY_WALL_FUNGAL_AERATION;
        if (s.canRepairBreachesClay()) c1 |= W1_CLAY_BREACH_REPAIR;
        if (s.canConstructMudResinEntranceFunnel()) c1 |= W1_MUD_RESIN_ENTRANCE_FUNNEL;
        if (s.canMasticatePaperPulpCarton()) c1 |= W1_PAPER_PULP_CARTON_MASTICATION;
        if (s.canScrapeWoodPulpCarton()) c1 |= W1_WOOD_PULP_CARTON_SCRAPE;
        if (s.canPlugHoneyStoresBricks()) c1 |= W1_HONEY_STORE_BRICK_PLUGGING;
        if (s.canExcavateVerticalDrainageShafts()) c1 |= W1_VERTICAL_DRAINAGE_SHAFT;
        if (s.canDigSubterraneanClayAqueducts()) c1 |= W1_SUBTERRANEAN_CLAY_AQUEDUCT;
        if (s.canConstructSphagnumMoistureDomes()) c1 |= W1_SPHAGNUM_MOISTURE_DOME;
        if (s.canConstructPhonicIsolationChambers()) c1 |= W1_PHONIC_ISOLATION_CHAMBER;
        if (s.canPlasterWoodWallGallery()) c1 |= W1_PASSALID_WOOD_WALL_PLASTER;
        if (s.canPlasterFrassGalleryWalls()) c1 |= W1_BEETLE_FRASS_GALLERY_PLASTER;
        if (s.canCoatWaspPedicelAntRepellent() || s.canApplyPedicelAntRepellent()) c1 |= W1_WASP_PEDICAL_ANT_REPELLENT;
        if (s.canApplyHydrophobicTrailCoating()) c1 |= W1_HYDROPHOBIC_TRAIL_COATING;
        if (s.canConstructChaffGarbageDunes()) c1 |= W1_CHAFF_GARBAGE_DUNE;
        if (s.canExcavatePitfallTraps()) c1 |= W1_PITFALL_TRAP_EXCAVATION;
        if (s.canConstructCollapsiblePitTraps()) c1 |= W1_COLLAPSIBLE_PIT_TRAP;
        if (s.hasSolarOrientedMound()) c1 |= W1_SOLAR_MOUND;
        if (s.canPerformEvaporativeCooling()) c1 |= W1_EVAPORATIVE_COOLING;
        if (s.canPerformThoracicIncubation()) c1 |= W1_THORACIC_INCUBATION;
        if (s.canPerformSocialThermoregulation()) c1 |= W1_SOCIAL_THERMOREGULATION;
        if (s.canPerformSolarBroodBasking()) c1 |= W1_SOLAR_BROOD_BASKING;
        if (s.canFanWingsForBroodThermoregulation()) c1 |= W1_BROOD_WING_FANNING;
        if (s.canIncubateBroodAbdominalHeat()) c1 |= W1_BUMBLEBEE_ABDOMINAL_INCUBATION;
        if (s.canEmitMoundOverheatVibrato()) c1 |= W1_MOUND_OVERHEAT_VIBRATO;
        if (s.canShieldSwarmCoreHeat()) c1 |= W1_HONEYBEE_SWARM_CORE_HEAT_SHIELD;
        if (s.canCoolNestWaterRegurgitation()) c1 |= W1_WASP_NEST_WATER_COOLING;
        if (s.canSynthesizeGlycerolCryoprotection()) c1 |= W1_GLYCEROL_CRYOPROTECTION;
        if (s.canForageSubZeroBumblebee()) c1 |= W1_SUB_ZERO_BUMBLEBEE_FORAGING;
        if (s.hasThermalTrailDecay()) c1 |= W1_THERMAL_TRAIL_DECAY;
        if (s.canConstructThermoregulatedConduits()) c1 |= W1_THERMOREGULATED_AIR_WATER_CONDUIT;
        if (s.canConstructThermalChimneyFlues()) c1 |= W1_TERMITE_THERMAL_CHIMNEY_FLUE;
        if (s.canPerformPulsatileVentilation()) c1 |= W1_PULSATILE_VENTILATION;
        if (s.canPerformPulsedAirConvectiveVentilation()) c1 |= W1_PULSED_AIR_CONVECTIVE_VENTILATION;
        if (s.canFarmAphids()) c1 |= W1_APHID_FARMING;
        if (s.canMilkAphidHoneydewStroking()) c1 |= W1_APHID_HONEYDEW_MILKING;
        if (s.canEjectHoneydewSignalingDroplets()) c1 |= W1_APHID_HONEYDEW_SIGNALING;
        if (s.canEnforceAphidSanitaryCordon()) c1 |= W1_APHID_SANITARY_CORDON;
        if (s.isHoneypotStorageCaste()) c1 |= W1_HONEYPOT_STORAGE;
        if (s.canStoreNectarAsHoneypotReplete()) c1 |= W1_HONEYPOT_REPLETE_STORAGE;
        if (s.canDeGermStoredSeeds()) c1 |= W1_SEED_STORAGE;
        if (s.canTurnGranarySeedsAeration()) c1 |= W1_GRANARY_SEED_AERATION;
        if (s.canChewSeedHuskBreadPulp()) c1 |= W1_HARVESTER_BREAD_PULP_CHEW;
        if (s.canMutilateSeedRadicles()) c1 |= W1_HARVESTER_SEED_RADICLE_MUTILATION;
        if (s.canPerformRelaySeedTransport()) c1 |= W1_RELAY_SEED_TRANSPORT;
        if (s.canHarvestDewCondensation()) c1 |= W1_DEW_CONDENSATION_HARVEST;
        if (s.canTransportWaterInMandibleDroplet()) c1 |= W1_MANDIBLE_DROPLET_WATER_TRANSPORT;
        if (s.canPackCorbiculaPollenBaskets()) c1 |= W1_CORBICULA_POLLEN_PACKING;
        if (s.canPerformBuzzPollination()) c1 |= W1_BUZZ_POLLINATION;
        if (s.canPerformBuzzPollinationSonication()) c1 |= W1_BUZZ_POLLINATION_SONICATION;
        if (s.canConstructNectarWaxPots()) c1 |= W1_BUMBLEBEE_NECTAR_WAX_POT;
        if (s.canLapNectarTongueExtension()) c1 |= W1_BUMBLEBEE_NECTAR_TONGUE_LAPPING;
        if (s.canBiteNectarTheftHoles()) c1 |= W1_BUMBLEBEE_NECTAR_THEFT_HOLE_BITE;
        if (s.canConsumeFermentedSapAnesthetic()) c1 |= W1_FERMENTED_SAP_ANESTHETIC;
        if (s.canForageSaltCrystalsOsmoregulation()) c1 |= W1_SALT_CRYSTAL_OSMOREGULATION;
        if (s.canRaidToxicPlantResin()) c1 |= W1_TOXIC_PLANT_RESIN_RAID;
        if (s.canIngestPhenolicResinMedication()) c1 |= W1_PHENOLIC_RESIN_MEDICATION;
        if (s.canRaidAbandonedWaxVaults()) c1 |= W1_ABANDONED_WAX_VAULT_RAID;

        // Word 2: Fungiculture, Silk, Slavery, Territorial, Biostructures, Disasters
        if (s.canWeedFungusGarden() || s.canFarmFungus()) c2 |= W2_FUNGUS_WEEDING;
        if (s.canShearLeafCrescentMandible()) c2 |= W2_ATTA_LEAF_CRESCENT_SHEAR;
        if (s.canExcavateGardenWasteChambers()) c2 |= W2_ATTA_GARDEN_WASTE_CHAMBER_DIG;
        if (s.canInoculateLeafPulpEnzymes()) c2 |= W2_LEAF_PULP_ENZYME_INOCULATION;
        if (s.canInoculateFungalCombTermite()) c2 |= W2_TERMITE_FUNGAL_COMB;
        if (s.canBuryFungalWasteInGallery()) c2 |= W2_TERMITE_FUNGAL_WASTE_BURIAL;
        if (s.canCultivateWoodFungus()) c2 |= W2_SUBTERRANEAN_FUNGUS_WOOD;
        if (s.canCultivateStreptomycesAntibiotics()) c2 |= W2_STREPTOMYCES_ANTIBIOTICS;
        if (s.hasTermiteGutSymbiosis()) c2 |= W2_TERMITE_GUT_SYMBIOSIS;
        if (s.canTrophallaxisProtozoa()) c2 |= W2_TERMITE_PROTOZOA_TROPHALLAXIS;
        if (s.canInhabitDomatia()) c2 |= W2_DOMALIA_MUTUALISM;
        if (s.canAbsorbHostPlantChemicalCamouflage()) c2 |= W2_HOST_PLANT_CHEMICAL_CAMOUFLAGE;
        if (s.canSewLeavesWithLarvalSilk()) c2 |= W2_WEAVER_SILK;
        if (s.canFormLeafPullingChains()) c2 |= W2_WEAVER_LEAF_PULLING_CHAIN;
        if (s.canWeaveSilkPavilionAphidShelter()) c2 |= W2_WEAVER_SILK_PAVILION_APHID_SHELTER;
        if (s.canHarnessLarvalSilkCocoon()) c2 |= W2_LARVAL_SILK_HARNESS;
        if (s.canWeaveLarvalSilkCanopyBridges()) c2 |= W2_LARVAL_SILK_CANOPY_BRIDGE;
        if (s.canCollectPropolis()) c2 |= W2_PROPOLIS_SHIELD;
        if (s.canSealNestGapsWithPropolis()) c2 |= W2_HONEYBEE_PROPOLIS_NEST_SEAL;
        if (s.canWeaveStenogastrinePaperJelly()) c2 |= W2_STENOGASTRINE_PAPER_JELLY_WEAVING;
        if (s.canApplyBeeBreadHydrophobicCoating()) c2 |= W2_BEE_BREAD_HYDROPHOBIC_COATING;
        if (s.canResinMummifyNymphalChambers()) c2 |= W2_RESIN_NYMPHAL_MUMMIFICATION;
        if (s.canWeaveSocialSilkHammock()) c2 |= W2_CATERPILLAR_SILK_HAMMOCK_TENT;
        if (s.canFormProcessionarySilkTrail()) c2 |= W2_PROCESSIONARY_SILK_TRAIL;
        if (s.isSlaveMakingSpecies()) c2 |= W2_DULOSIS_RAID;
        if (s.isRobberBeeSpecies()) c2 |= W2_ROBBER_BEE;
        if (s.canBindParasitesWithSilk()) c2 |= W2_PARASITE_SILK_BINDING;
        if (s.canQuarantineInvasiveParasites()) c2 |= W2_PARASITE_QUARANTINE;
        if (s.canMarkParasitizedCadaverRepellent()) c2 |= W2_PARASITIZED_CADAVER_REPELLENT;
        if (s.canSelfIsolateWhenInfected()) c2 |= W2_SELF_ISOLATION;
        if (s.hasTerritorialRepellentPheromone()) c2 |= W2_TERRITORIAL_REPELLENT;
        if (s.canPerformRitualJousting()) c2 |= W2_RITUAL_JOUSTING;
        if (s.canPerformRitualMandibularWrestling()) c2 |= W2_RITUAL_MANDIBULAR_WRESTLING;
        if (s.canPerformDominanceMounting()) c2 |= W2_WASP_DOMINANCE_MOUNTING;
        if (s.canRecognizeFacialVisualPatterns() || s.canRecognizeWaspFacialPatterns()) c2 |= W2_WASP_FACIAL_RECOGNITION;
        if (s.canFormLivingBivouac()) c2 |= W2_LIVING_BIVOUAC;
        if (s.canFormBiomechanicalBivouac()) c2 |= W2_BIOMECHANICAL_BIVOUAC;
        if (s.canFormLivingBridges()) c2 |= W2_LIVING_BRIDGE;
        if (s.canFormTarsalFrictionBridge()) c2 |= W2_TARSAL_FRICTION_BRIDGE;
        if (s.canTransportChainBrood()) c2 |= W2_CHAIN_BROOD_TRANSPORT;
        if (s.canPerformBiostructures() || s.canFormLivingRaft()) c2 |= W2_SELF_ASSEMBLED_RAFT;
        if (s.canFormFloatingAntRaft()) c2 |= W2_FLOATING_ANT_RAFT;
        if (s.canConstructRainEvacuationSiphons()) c2 |= W2_RAIN_EVACUATION_SIPHON;
        if (s.canPerformPhragmosis()) c2 |= W2_PHRAGMOSIS;
        if (s.hasOleicAcidThresholdNecrophoresis()) c2 |= W2_OLEIC_ACID_NECROPHORESIS;
        if (s.canPerformNecrophoresis()) c2 |= W2_NECROPHORESIS;
        if (s.canSortExternalRefusePits()) c2 |= W2_REFUSE_SORTING;
        if (s.canFeedLarvaeExuviaRecycling()) c2 |= W2_LARVAL_EXUVIA_CHITIN_RECYCLING;
        if (s.canDryLarvaeWoodDust()) c2 |= W2_LARVAL_WOOD_DUST_DRYING;
        if (s.canApplyEggMassMucilageEnvelope()) c2 |= W2_EGG_MASS_MUCILAGE_ENVELOPE;
        if (s.canPerformEggLickingGrooming()) c2 |= W2_EARWIG_EGG_LICKING_GROOMING;
        if (s.canRegurgitateEarwigMaternalFood()) c2 |= W2_EARWIG_MATERNAL_REGURGITATION;
        if (s.canGroomNymphCuticularSurface()) c2 |= W2_EARWIG_NYMPH_CUTICULAR_GROOMING;
        if (s.canPerformMaternalShieldGuarding()) c2 |= W2_MATERNAL_SHIELD_GUARDING;
        if (s.canDetectHydrostaticPressure()) c2 |= W2_FLOOD_EVACUATION;
        if (s.canTriggerUniversalEmergencyEvacuation()) c2 |= W2_UNIVERSAL_EMERGENCY_EVACUATION;
        if (s.canDrumAntennaeLarvalStimulation()) c2 |= W2_WASP_ANTENNAL_DRUMMING;
        if (s.canDrumAbdomenWaspCellRim()) c2 |= W2_WASP_CELL_RIM_DRUMMING;
        if (s.canDepositLarvalFoodSalivaDrop()) c2 |= W2_WASP_EMERGENCY_SALIVA_FOOD_DROP;
        if (s.canDouseNestWaterCooling()) c2 |= W2_WASP_WATER_DOUSING;
        if (s.canEmitHornetGroupAlarmPheromone()) c2 |= W2_HORNET_GROUP_ALARM_PHEROMONE;
        if (s.canPerformAntiPredatorShimmeringWave()) c2 |= W2_GIANT_HONEYBEE_SHIMMERING_WAVE;
        if (s.canPerformQueenPiping()) c2 |= W2_QUEEN_PIPING_SIGNAL;

        // Word 3: Clade Specializations (Termites, Passalids, Thrips, Spiders, Aphids, Shrimp, Sensors)
        if (s.canSynchronizeSoldierAlarmDrumming()) c3 |= W3_TERMITE_SOLDIER_ALARM_DRUM_SYNCHRONY;
        if (s.canSnapMandibleAcousticAlarm()) c3 |= W3_TERMITE_MANDIBLE_SNAP_ALARM;
        if (s.canApplySalivaryCementMoistureSeal()) c3 |= W3_TERMITE_SALIVA_CEMENT_MOISTURE_SEAL;
        if (s.canBlockRoyalChamberSentry()) c3 |= W3_TERMITE_ROYAL_CHAMBER_BLOCKADE;
        if (s.canExchangeRoyalPairGrooming()) c3 |= W3_TERMITE_ROYAL_PAIR_GROOMING;
        if (s.canPerformQueenPhysogastricPeristalsis() || s.canPerformPhysogastricPeristalsis()) c3 |= W3_TERMITE_QUEEN_PHYSOGASTRIC_EGG_PERISTALSIS;
        if (s.canStridulatePassalidParentalCare()) c3 |= W3_PASSALID_PARENTAL_STRIDULATION;
        if (s.canStridulateLarvalHungerChirp()) c3 |= W3_PASSALID_GRUB_HUNGER_STRIDULATION;
        if (s.canDuetPassalidSubstrateVibration()) c3 |= W3_PASSALID_SUBSTRATE_DUET;
        if (s.canTrophallaxisPassalidWoodFrass()) c3 |= W3_PASSALID_WOOD_FRASS_TROPHALLAXIS;
        if (s.canSqueezeGallIntrudersThrips()) c3 |= W3_THRIPS_GALL_FORELEG_SQUEEZING;
        if (s.canRepairGallSubstratalSecretion()) c3 |= W3_THRIPS_GALL_REPAIR_SECRETION;
        if (s.canPlugGallWithChitinousTube()) c3 |= W3_THRIPS_CHITINOUS_TUBE_PLUG;
        if (s.canWrapPreyInCommunalSilk()) c3 |= W3_SPIDER_COMMUNAL_SILK_PREY_WRAP;
        if (s.canPerformCrecheRegurgitationSpider()) c3 |= W3_SPIDER_CRECHE_REGURGITATION;
        if (s.canSensePreySignalWireTripping()) c3 |= W3_SPIDER_DRAGLINE_SIGNAL_WIRE;
        if (s.canEjectGarbageChuteRefuse()) c3 |= W3_SPIDER_GARBAGE_CHUTE;
        if (s.canCamouflageWebWithPlantDebris()) c3 |= W3_SPIDER_WEB_DEBRIS_CAMOUFLAGE;
        if (s.canWeaveCommunalSpiderSilk()) c3 |= W3_COMMUNAL_SPIDER_SILK;
        if (s.canSqueezeIntrudersWithForelegs()) c3 |= W3_APHID_FORELEG_INTRUDER_SQUEEZE;
        if (s.canSecreteGallClosingFluid()) c3 |= W3_APHID_GALL_CLOSING_FLUID;
        if (s.canStabFrontalHornsAphid()) c3 |= W3_APHID_SOLDIER_HORN_STABBING;
        if (s.canFireShrimpAcousticCannon()) c3 |= W3_SHRIMP_ACOUSTIC_CANNON;
        if (s.canSnapClawAcousticShockwave()) c3 |= W3_EUSOCIAL_SHRIMP_CLAW_SHOCKWAVE;
        if (s.canShieldEggsFromParasitoidWasps()) c3 |= W3_SHIELD_BUG_PARASITOID_SHIELD;
        if (s.canEmitParentBugAlarmGathering()) c3 |= W3_PARENT_BUG_ALARM_GATHERING;
        if (s.canClusterSolarHeatCollector()) c3 |= W3_MOUND_SOLAR_HEAT_COLLECTOR;
        if (s.canOrientMagneticMound()) c3 |= W3_MAGNETIC_MOUND_ORIENTATION;
        if (s.canPerformBiostructures()) c3 |= W3_EUSOCIAL_BIOSTRUCTURE;
        if (s.canEmitAcousticPreySurge()) c3 |= W3_ACOUSTIC_SURGE;
        if (s.canApplyDustSubstrateCamouflage()) c3 |= W3_DUST_SUBSTRATE_CAMOUFLAGE;
        if (s.canHarvestCuticularWaterCondensation()) c3 |= W3_CUTICLE_WATER_CONDENSATION;
        if (s.canPerformDroughtVibratoDance()) c3 |= W3_DROUGHT_SOIL_MOISTURE_VIBRATO;
        if (s.canEmitSubstrateObstacleVibrato()) c3 |= W3_SUBSTRATE_OBSTACLE_VIBRATO;
        if (s.canHuntNocturnalInfrared()) c3 |= W3_NOCTURNAL_INFRARED_HUNTING;
        if (s.hasPreySizeSelectivePheromones()) c3 |= W3_PREY_SIZE_SELECTIVE_PHEROMONE;
        if (s.canEncapsulateLargeIntrudersClay()) c3 |= W3_LARGE_INTRUDER_CLAY_ENCAPSULATION;
        if (s.canEmitMoundOverheatVibrato()) c3 |= W3_MOUND_OVERHEAT_VIBRATO;

        

        eth.setCapabilities(c0, c1, c2, c3);
    }
}