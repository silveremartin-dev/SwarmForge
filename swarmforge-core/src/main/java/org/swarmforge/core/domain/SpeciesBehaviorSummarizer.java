/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.species.Species;

import java.util.ArrayList;
import java.util.List;

/**
 * Ethological analyzer summarizing active biological and social behaviors for a given {@link Species}.
 * Evaluates physiological adaptations, communication modes, defensive mechanisms, and foraging strategies.
 */
public final class SpeciesBehaviorSummarizer {

    private SpeciesBehaviorSummarizer() {
        // Utility class
    }

    /**
     * Constructs a human-readable summarized list of active behavioral capabilities for the given species.
     *
     * @param species the species to analyze
     * @return a comma-separated summary string of active behaviors
     */
    public static String getActiveBehaviorsSummary(Species species) {
        if (species == null) return "STANDARD_PATROL";
        List<String> list = new ArrayList<>();
        if (species.canFarmAphids()) list.add("APHID_FARMING");
        if (species.hasRoyalPheromoneInhibition()) list.add("ROYAL_INHIBITION");
        if (species.canDrumSubstrate()) list.add("ACOUSTIC_DRUMMING");
        if (species.isPolycalic()) list.add("POLYCALIC_ROUTING");
        if (species.canCollectPropolis()) list.add("PROPOLIS_SHIELD");
        if (species.canSewLeavesWithLarvalSilk()) list.add("WEAVER_SILK");
        if (species.canWeedFungusGarden()) list.add("FUNGUS_WEEDING");
        if (species.canMakeStercoralCement()) list.add("STERCORAL_CEMENT");
        if (species.hasProctodealTrophallaxis()) list.add("PROCTODEAL_TROPHALLAXIS");
        if (species.canPerformPhragmosis()) list.add("PHRAGMOSIS_GATE");
        if (species.canPerformEvaporativeCooling()) list.add("EVAPORATIVE_COOLING");
        if (species.hasTrapJawMechanism()) list.add("TRAP_JAW");
        if (species.isSlaveMakingSpecies()) list.add("DULOSIS_RAID");
        if (species.canFormLivingBivouac()) list.add("LIVING_BIVOUAC");
        if (species.hasSolarOrientedMound()) list.add("SOLAR_MOUND");
        if (species.canPerformAllogrooming()) list.add("ALLOGROOMING");
        if (species.canPerformTrembleDance()) list.add("TREMBLE_DANCE");
        if (species.hasThermalTrailDecay()) list.add("THERMAL_TRAIL_DECAY");
        if (species.canPerformThoracicIncubation()) list.add("THORACIC_INCUBATION");
        if (species.canPerformRitualJousting()) list.add("RITUAL_JOUSTING");
        if (species.hasTerritorialRepellentPheromone()) list.add("TERRITORIAL_REPELLENT");
        if (species.canDetectHydrostaticPressure()) list.add("FLOOD_DETECTION");
        if (species.isRobberBeeSpecies()) list.add("ROBBER_BEE_RAID");
        if (species.canStridulateRescueCall()) list.add("RESCUE_STRIDULATION");
        if (species.isHoneypotStorageCaste()) list.add("HONEYPOT_STORAGE");
        if (species.canPlugContaminatedGalleries()) list.add("GRAVEL_PLUGGING");
        if (species.hasOleicAcidThresholdNecrophoresis()) list.add("NECROPHORESIS");
        if (species.hasUVPolarizedLightNavigation()) list.add("UV_COMPASS");
        if (species.canPerformTandemRunning()) list.add("TANDEM_RUNNING");
        if (species.canPerformThermalBalling()) list.add("THERMAL_BALLING");
        if (species.canFormLivingRaft()) list.add("LIVING_RAFT");
        if (species.canInhabitDomatia()) list.add("DOMATIA_PRUNING");
        if (species.canSelfIsolateWhenInfected()) list.add("SELF_ISOLATION");
        if (species.canSprayFormicResinDisinfectant()) list.add("RESIN_SPRAY");
        if (species.canTriggerEmergencySwarming()) list.add("EMERGENCY_SWARM");
        if (species.canConstructClayPillars()) list.add("CLAY_PILLARS");
        if (species.canDeGermStoredSeeds()) list.add("SEED_DEGERMINATION");
        if (species.canPerformQueenPiping()) list.add("QUEEN_PIPING");
        if (species.canPerformWaterTrophallaxis()) list.add("WATER_TROPHALLAXIS");
        if (species.canEnforceAphidSanitaryCordon()) list.add("APHID_SANITARY_CORDON");
        if (species.canFormLivingBridges()) list.add("LIVING_BRIDGES");
        if (species.canEmitAcousticPreySurge()) list.add("ACOUSTIC_PREY_SURGE");
        if (species.canSortExternalRefusePits()) list.add("REFUSE_SORTING");
        if (species.canCultivateWoodFungus()) list.add("WOOD_FUNGUS_CULTIVATION");
        if (species.hasEmergencyEscapePheromone()) list.add("EMERGENCY_ESCAPE_ALARM");
        if (species.canSealQueenChamberWax()) list.add("QUEEN_CELL_WAX_SEAL");
        if (species.hasCasteRatioPheromoneInhibition()) list.add("CASTE_RATIO_REGULATION");
        if (species.canPerformSuctionEscapePosture()) list.add("SUCTION_ESCAPE_POSTURE");
        if (species.canStridulateQueenRecognition()) list.add("QUEEN_RECOGNITION_CALL");
        if (species.canPerformPulsatileVentilation()) list.add("PULSATILE_VENTILATION");
        if (species.canRepairBreachesClay()) list.add("CLAY_BREACH_REPAIR");
        if (species.hasDepletingTrailPheromone()) list.add("DEPLETING_TRAIL_DECAY");
        if (species.canRecycleInviableEggs()) list.add("EGG_CANNIBALISM");
        if (species.canQuarantineInvasiveParasites()) list.add("PARASITE_QUARANTINE");
        if (species.canPerformArborealGlidingEscape()) list.add("ARBOREAL_GLIDING_ESCAPE");
        if (species.canPerformSolarBroodBasking()) list.add("SOLAR_BROOD_BASKING");
        if (species.canHarmonizeChcGestalt()) list.add("CHC_GESTALT_HARMONIZATION");
        if (species.canConstructCollapsiblePitTraps()) list.add("COLLAPSIBLE_PIT_TRAP");
        if (species.canHarvestDewCondensation()) list.add("DEW_CONDENSATION_HARVEST");
        if (species.canPerformExoskeletonAntiFungalPatrol()) list.add("EXOSKELETON_ANTIFUNGAL_PATROL");
        if (species.canPerformGuardShiftVibrationalWhisper()) list.add("GUARD_SHIFT_WHISPER");
        if (species.canConstructThermoregulatedConduits()) list.add("THERMOREGULATED_CONDUITS");
        if (species.canRaidToxicPlantResin()) list.add("TOXIC_PLANT_RESIN_RAID");
        if (species.canApplyDustSubstrateCamouflage()) list.add("DUST_SUBSTRATE_CAMOUFLAGE");
        if (species.canTransportChainBrood()) list.add("CHAIN_BROOD_TRANSPORT");
        if (species.hasTrophallacticOvaryInhibition()) list.add("TROPHALLACTIC_OVARY_INHIBITION");
        if (species.canPerformDroughtVibratoDance()) list.add("DROUGHT_VIBRATO_DANCE");
        if (species.canEncapsulateLargeIntrudersClay()) list.add("LARGE_INTRUDER_CLAY_ENCAPSULATION");
        if (species.canConstructPhonicIsolationChambers()) list.add("PHONIC_ISOLATION_CHAMBER");
        if (species.canApplyHydrophobicTrailCoating()) list.add("HYDROPHOBIC_TRAIL_COATING");
        if (species.canConsumeFermentedSapAnesthetic()) list.add("FERMENTED_SAP_ANESTHETIC");
        if (species.canPerformRelaySeedTransport()) list.add("RELAY_SEED_TRANSPORT");
        if (species.hasPreySizeSelectivePheromones()) list.add("PREY_SIZE_PHEROMONE");
        if (species.canDryLarvaeWoodDust()) list.add("LARVAL_WOOD_DUST_DRYING");
        if (species.canPerformFanoutEscapeFormicAcid()) list.add("FANOUT_ESCAPE_FORMIC_ACID");
        if (species.canEmitMoundOverheatVibrato()) list.add("MOUND_OVERHEAT_VIBRATO");
        if (species.canNourishVirginQueensPreFlight()) list.add("PREFLIGHT_QUEEN_NOURISHMENT");
        if (species.canPlugHoneyStoresBricks()) list.add("HONEY_STORE_BRICK_PLUG");
        if (species.canHuntNocturnalInfrared()) list.add("NOCTURNAL_INFRARED_HUNTING");
        if (species.canWeaveLarvalSilkCanopyBridges()) list.add("LARVAL_SILK_CANOPY_BRIDGE");
        if (species.canStridulateEggLayingSynchronization()) list.add("EGG_LAYING_SYNCHRONIZATION_STRIDULATION");
        if (species.canPerformAntennalDustGrooming()) list.add("ANTENNAL_DUST_GROOMING");
        if (species.canForageSaltCrystalsOsmoregulation()) list.add("SALT_CRYSTAL_OSMOREGULATION");
        if (species.canConstructRainEvacuationSiphons()) list.add("RAIN_EVACUATION_SIPHON");
        if (species.canAbsorbHostPlantChemicalCamouflage()) list.add("HOST_PLANT_CHEMICAL_CAMOUFLAGE");
        if (species.canDepositSulfurDustAntiMitePatrol()) list.add("SULFUR_DUST_ANTI_MITE_PATROL");
        if (species.canDanceVibratoHatchingEnthusiasm()) list.add("HATCHING_ENTHUSIASM_VIBRATO_DANCE");
        if (species.canResinMummifyNymphalChambers()) list.add("RESIN_NYMPHAL_MUMMIFICATION");
        if (species.canExcavatePitfallTraps()) list.add("PITFALL_TRAP_EXCAVATION");
        if (species.canSynthesizeGlycerolCryoprotection()) list.add("GLYCEROL_CRYOPROTECTION");
        if (species.canTransportInjuredPheromonalStretcher()) list.add("INJURED_PHEROMONAL_STRETCHER");
        if (species.canRaidAbandonedWaxVaults()) list.add("ABANDONED_WAX_VAULT_RAID");
        if (species.canPerformRitualMandibularWrestling()) list.add("RITUAL_MANDIBULAR_WRESTLING");
        if (species.canPerformPulsedAirConvectiveVentilation()) list.add("PULSED_AIR_VENTILATION");
        if (species.canCultivateStreptomycesAntibiotics()) list.add("STREPTOMYCES_ANTIBIOTICS");
        if (species.canNavigatePolarizedTwilightUV()) list.add("POLARIZED_TWILIGHT_UV_NAV");
        if (species.canSnapTrapMandiblesCatapult()) list.add("TRAP_MANDIBLE_CATAPULT");
        if (species.canPerformPedestrianSwarmBudding()) list.add("PEDESTRIAN_SWARM_BUDDING");
        if (species.canTrophallaxisProtozoa()) list.add("TERMITE_PROTOZOA_TROPHALLAXIS");
        if (species.canSquirtNasuteChemical()) list.add("NASUTE_SQUIRT_NOZZLE");
        if (species.canMasticatePaperPulpCarton()) list.add("PAPER_PULP_CARTON_MASTICATE");
        if (species.canHarvestLarvalSalivaDroplets()) list.add("LARVAL_SALIVA_HARVEST");
        if (species.canApplyPedicelAntRepellent()) list.add("PEDICEL_ANT_REPELLENT");
        if (species.canRecognizeFacialVisualPatterns()) list.add("WASPFACE_VISUAL_RECOGNITION");
        if (species.canPerformBuzzPollination()) list.add("BUZZ_POLLINATION_SONICATION");
        if (species.canIncubateBroodAbdominalHeat()) list.add("BUMBLEBEE_ABDOMINAL_BROOD_INCUBATION");
        if (species.canStabFrontalHornsAphid()) list.add("APHID_SOLDIER_HORN_STAB");
        if (species.canSqueezeGallIntrudersThrips()) list.add("THRIPS_GALL_FORELEG_SQUEEZE");
        if (species.canSnapClawAcousticShockwave()) list.add("EUSOCIAL_SHRIMP_CLAW_SHOCKWAVE");
        if (species.canStridulatePassalidParentalCare()) list.add("PASSALID_WOOD_FRASS_STRIDULATION");
        if (species.canPerformPhysogastricPeristalsis()) list.add("PHYSOGASTRIC_QUEEN_PERISTALSIS");
        if (species.canOrientMagneticMound()) list.add("GEOMAGNETIC_MOUND_ALIGNMENT");
        if (species.canEmitHornetGroupAlarmPheromone()) list.add("HORNET_VENOM_ALARM_RAID");
        if (species.canWeaveStenogastrinePaperJelly()) list.add("STENOGASTRINE_PAPER_JELLY_WEAVE");
        if (species.canInoculateFungalCombTermite()) list.add("TERMITE_TERMITOMYCES_FUNGAL_COMB");
        if (species.canDrumAbdomenWaspCellRim()) list.add("WASP_CELL_RIM_DRUMMING");
        if (species.canConstructNectarWaxPots()) list.add("BUMBLEBEE_NECTAR_WAX_POT");
        if (species.canPerformMaternalShieldGuarding()) list.add("PARENT_BUG_MATERNAL_SHIELD");
        if (species.canWeaveCommunalSpiderSilk()) list.add("COMMUNAL_SPIDER_SILK_WEAVE");
        if (species.canFormProcessionarySilkTrail()) list.add("PROCESSIONARY_SILK_TRAIL");
        if (species.canConstructClayVaultArches()) list.add("CLAY_VAULT_ARCH_ENGINEERING");
        if (species.canDeliverStenogastrinePapFood()) list.add("STENOGASTRINE_PAP_FOOD_DELIVERY");
        if (species.canPlasterFrassGalleryWalls()) list.add("BEETLE_FRASS_GALLERY_PLASTER");
        if (species.canLearnTrapliningFlightRoutes()) list.add("TRAPLINING_FLIGHT_ROUTE_LEARNING");
        if (species.canCoolNestWaterRegurgitation()) list.add("WASP_NEST_WATER_COOLING");
        if (species.canEjectHoneydewSignalingDroplets()) list.add("APHID_HONEYDEW_SIGNAL_FLICK");
        if (species.canSnapMandibleAcousticAlarm()) list.add("TERMITE_MANDIBLE_SNAP_ALARM");
        if (species.canPerformEggLickingGrooming()) list.add("EARWIG_EGG_LICKING_GROOMING");
        if (species.canConstructChaffGarbageDunes()) list.add("SEED_CHAFF_GARBAGE_DUNE");
        if (species.canDrumAntennaeLarvalStimulation()) list.add("WASP_ANTENNAL_DRUMMING_LARVA");
        if (species.canFormLeafPullingChains()) list.add("WEAVER_LEAF_PULLING_CHAIN");
        if (species.canApplySalivaryCementMoistureSeal()) list.add("TERMITE_SALIVARY_CEMENT_SEAL");
        if (species.canForageSubZeroBumblebee()) list.add("SUBZERO_BUMBLEBEE_FORAGING");
        if (species.canRepairGallSubstratalSecretion()) list.add("THRIPS_GALL_REPAIR_SECRETION");
        if (species.canTrophallaxisPassalidWoodFrass()) list.add("PASSALID_WOOD_FRASS_TROPHALLAXIS");
        if (species.canPerformCrecheRegurgitationSpider()) list.add("SPIDER_CRECHE_REGURGITATION");
        if (species.canBlockRoyalChamberSentry()) list.add("TERMITE_ROYAL_CHAMBER_BLOCKADE");
        if (species.canEmitParentBugAlarmGathering()) list.add("PARENT_BUG_ALARM_CLUSTER");
        if (species.canApplyBeeBreadHydrophobicCoating()) list.add("BEE_BREAD_LIPID_COATING");
        if (species.canBindParasitesWithSilk()) list.add("MYRMECOPHILE_PARASITE_SILK_BINDING");
        if (species.canEmitSubstrateObstacleVibrato()) list.add("SUBSTRATE_OBSTACLE_VIBRATO_WARNING");
        if (species.canPerformFormicAcidBathGrooming()) list.add("POST_COMBAT_FORMIC_ACID_BATH");
        if (species.canExcavateVerticalDrainageShafts()) list.add("VERTICAL_DRAINAGE_SHAFT_EXCAVATION");
        if (species.canIngestPhenolicResinMedication()) list.add("PHENOLIC_RESIN_IMMUNE_STIMULATION");
        if (species.canConstructSphagnumMoistureDomes()) list.add("SPHAGNUM_MOSS_MOISTURE_DOME");
        if (species.canMarkParasitizedCadaverRepellent()) list.add("PARASITIZED_CADAVER_REPELLENT_MARK");
        if (species.canDrumNuptialFlightSynchronization()) list.add("NUPTIAL_FLIGHT_SUBSTRATE_DRUMMING");
        if (species.canHarvestCuticularWaterCondensation()) list.add("CUTICULAR_HAIR_FOG_CONDENSATION");
        if (species.canStridulateLarvalHungerChirp()) list.add("PASSALID_GRUB_HUNGER_STRIDULATION");
        if (species.canConstructThermalChimneyFlues()) list.add("TERMITE_THERMAL_CHIMNEY_FLUE");
        if (species.canDepositLarvalFoodSalivaDrop()) list.add("WASP_EMERGENCY_SALIVA_FOOD_DROP");
        if (species.canApplyEggMassMucilageEnvelope()) list.add("EGG_MASS_MUCILAGE_ENVELOPE");
        if (species.canWeaveSilkPavilionAphidShelter()) list.add("WEAVER_SILK_PAVILION_APHID_SHELTER");
        if (species.canFormHotBallThermalDefense()) list.add("HONEYBEE_HOT_BALL_THERMAL_DEFENSE");
        if (species.canPerformFontanelleAutothysis()) list.add("TERMITE_AUTOTHYSIS_EXPLOSIVE_SACRIFICE");
        if (species.canSensePreySignalWireTripping()) list.add("SPIDER_DRAGLINE_SIGNAL_WIRE_TRIP");
        if (species.canMutilateSeedRadicles()) list.add("HARVESTER_SEED_RADICLE_MUTILATION");
        if (species.canBiteNectarTheftHoles()) list.add("BUMBLEBEE_NECTAR_THEFT_HOLE_BITE");
        if (species.canSowFungalSporeCombs()) list.add("TERMITE_FUNGAL_SPORE_SOWING");
        if (species.canHarnessLarvalSilkCocoon()) list.add("WEAVER_LARVAL_SILK_HARNESS");
        if (species.canFormBiomechanicalBivouac()) list.add("ARMY_ANT_BIOMECHANICAL_BIVOUAC");
        if (species.canPerformBuzzPollinationSonication()) list.add("BUMBLEBEE_BUZZ_POLLINATION");
        if (species.canRegurgitateEarwigMaternalFood()) list.add("EARWIG_MATERNAL_FOOD_REGURGITATION");
        if (species.canRecognizeWaspFacialPatterns()) list.add("WASP_FACIAL_PATTERN_RECOGNITION");
        if (species.canFireShrimpAcousticCannon()) list.add("SHRIMP_ACOUSTIC_CANNON_DEFENSE");
        if (species.canDuetPassalidSubstrateVibration()) list.add("PASSALID_SUBSTRATE_DUET_COMMUNICATION");
        if (species.canTurnGranarySeedsAeration()) list.add("HARVESTER_GRANARY_SEED_TURNING");
        if (species.canEncodeWaggleDanceSunCompass()) list.add("HONEYBEE_WAGGLE_DANCE_SUN_COMPASS");
        if (species.canDigSubterraneanClayAqueducts()) list.add("TERMITE_CLAY_AQUEDUCT_WELL");
        if (species.canFireFormicAcidArtilleryJet()) list.add("FORMICA_ACID_ARTILLERY_JET");
        if (species.canEjectGarbageChuteRefuse()) list.add("SPIDER_GARBAGE_CHUTE_EJECTIONS");
        if (species.canFanWingsForBroodThermoregulation()) list.add("BROOD_THERMOREGULATORY_WING_FANNING");
        if (species.canPlugGallWithChitinousTube()) list.add("THRIPS_GALL_CHITINOUS_TUBE_PLUG");
        if (species.canCoatWaspPedicelAntRepellent()) list.add("WASP_PEDICEL_ANT_REPELLENT_COAT");
        if (species.canSquirtNasuteViscousResin()) list.add("NASUTE_VISCOUS_RESIN_SQUIRT");
        if (species.canSealNestGapsWithPropolis()) list.add("HONEYBEE_PROPOLIS_NEST_SEAL");
        if (species.canSynchronizeSoldierAlarmDrumming()) list.add("TERMITE_SOLDIER_ALARM_DRUM_SYNCHRONY");
        if (species.canPerformDominanceMounting()) list.add("WASP_DOMINANCE_MOUNTING_DRUM");
        if (species.canLapNectarTongueExtension()) list.add("BUMBLEBEE_NECTAR_TONGUE_LAPPING");
        if (species.canSecreteGallClosingFluid()) list.add("APHID_GALL_CLOSING_FLUID_SECRETION");
        if (species.canGroomNymphCuticularSurface()) list.add("EARWIG_NYMPH_CUTICULAR_GROOMING");
        if (species.canExcavateGardenWasteChambers()) list.add("ATTA_GARDEN_WASTE_CHAMBER_DIG");
        if (species.canExchangeRoyalPairGrooming()) list.add("TERMITE_ROYAL_PAIR_GROOMING");
        if (species.canTriggerUniversalEmergencyEvacuation()) list.add("SWARMFORGE_UNIVERSAL_EMERGENCY_EVACUATION");
        if (species.canStoreNectarAsHoneypotReplete()) list.add("HONEYPOT_REPLETE_NECTAR_STORAGE");
        if (species.canFormFloatingAntRaft()) list.add("FLOATING_ANT_FLOOD_RAFT");
        if (species.canConstructMudResinEntranceFunnel()) list.add("MUD_RESIN_ENTRANCE_FUNNEL_GUARD");
        if (species.canExcavateHibernationBurrow()) list.add("QUEEN_SOIL_HIBERNATION_BURROW");
        if (species.canInoculateLeafPulpEnzymes()) list.add("LEAF_PULP_ENZYME_INOCULATION");
        if (species.canPerformGamergateDominanceTournament()) list.add("GAMERGATE_DOMINANCE_TOURNAMENT");
        if (species.canFeedOnLarvalHemolymphDracula()) list.add("DRACULA_ANT_LARVAL_HEMOLYMPH_FEED");
        if (species.canFormTarsalFrictionBridge()) list.add("TARSAL_FRICTION_TENSILE_BRIDGE");
        if (species.canTransportWaterInMandibleDroplet()) list.add("MANDIBLE_DROPLET_WATER_TRANSPORT");
        if (species.canStiltWalkThermalRegim()) list.add("DESERT_ANT_STILT_WALKING_COOLING");
        if (species.canPerformAntiPredatorShimmeringWave()) list.add("GIANT_HONEYBEE_SHIMMERING_WAVE");
        if (species.canDouseNestWaterCooling()) list.add("PAPER_WASP_WATER_DOUSING_COOLING");
        if (species.canAerateFungalCombChambers()) list.add("CLAY_WALL_FUNGAL_AERATION");
        if (species.canFeedLarvaeExuviaRecycling()) list.add("LARVAL_EXUVIA_CHITIN_RECYCLING");
        if (species.canGroomLeafPulpParasitesMinim()) list.add("MINIM_LEAF_PARASITE_GROOMING");
        if (species.canCamouflageWebWithPlantDebris()) list.add("SPIDER_WEB_DEBRIS_CAMOUFLAGE");
        if (species.canCockGasterFormicAcidRepellent()) list.add("ACROBAT_ANT_GASTER_VENOM_DEFENSE");
        if (species.canMilkAphidHoneydewStroking()) list.add("APHID_HONEYDEW_ANTENNAL_MILKING");
        if (species.canClusterSolarHeatCollector()) list.add("MOUND_SOLAR_HEAT_COLLECTOR_CLUSTER");
        if (species.canSerializeGlobalEthologicalBitSet()) list.add("GLOBAL_ETHOLOGICAL_BITSET_SERIALIZATION");

        if (list.isEmpty()) return "BASE_PATROL";
        if (list.size() > 3) {
            return String.join(", ", list.subList(0, 3)) + String.format(" (+%d)", list.size() - 3);
        }
        return String.join(", ", list);
    }
}
