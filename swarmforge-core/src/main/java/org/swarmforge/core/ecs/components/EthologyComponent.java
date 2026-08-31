package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * ECS Component encoding all 220+ eusocial insect behavioral capabilities as packed long bitmasks.
 * Uses 4 × 64-bit longs to cover the full behavioral catalog without boxing or allocation.
 * Each bit maps 1:1 to a legacy simulation system in org.swarmforge.core.simulation.*
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class EthologyComponent extends Component {

    // ─────────── WORD 0 (bits 0-63) ──────────────────────────────────────────
    // Navigation & Orientation
    public static final long W0_MAGNETORECEPTION          = 1L << 0;
    public static final long W0_UV_POLARIZED_NAV          = 1L << 1;
    public static final long W0_WAGGLE_DANCE              = 1L << 2;
    public static final long W0_WAGGLE_DANCE_SUN_COMPASS  = 1L << 3;
    public static final long W0_TANDEM_RUNNING            = 1L << 4;
    public static final long W0_DESERT_ANT_STILT_WALKING  = 1L << 5;
    public static final long W0_ARBOREAL_GLIDING_ESCAPE   = 1L << 6;
    public static final long W0_TRAPLINE_FLIGHT_ROUTE     = 1L << 7;
    public static final long W0_POLARIZED_TWILIGHT_UV_NAV = 1L << 8;
    public static final long W0_POLAR_LIGHT_COMPASS       = 1L << 9;

    // Trophallaxis & Food Exchange
    public static final long W0_TROPHALLAXIS              = 1L << 10;
    public static final long W0_PROCTODEAL_TROPHALLAXIS   = 1L << 11;
    public static final long W0_LARVAL_SALIVARY_TROPHALLAXIS = 1L << 12;
    public static final long W0_WATER_TROPHALLAXIS        = 1L << 13;
    public static final long W0_TROPHIC_EGG_NOURISHMENT   = 1L << 14;
    public static final long W0_DRACULA_ANT_LARVAL_HEMOLYMPH = 1L << 15;
    public static final long W0_LARVAL_SALIVA_HARVESTING  = 1L << 16;
    public static final long W0_STENOGASTRINE_PAP_FOOD    = 1L << 17;

    // Grooming & Sanitation
    public static final long W0_ALLOGROOMING             = 1L << 18;
    public static final long W0_FORMIC_ACID_BATH_GROOMING = 1L << 19;
    public static final long W0_ANTENNAL_DUST_GROOMING   = 1L << 20;
    public static final long W0_MINIM_LEAF_PARASITE_GROOMING = 1L << 21;
    public static final long W0_FUNGAL_SPORE_COMB        = 1L << 22;
    public static final long W0_EXOSKELETON_ANTIFUNGAL_PATROL = 1L << 23;
    public static final long W0_SULFUR_DUST_ANTIMITE_PATROL = 1L << 24;

    // Defense & Combat
    public static final long W0_AUTOTHYSIS               = 1L << 25;
    public static final long W0_FONTANELLE_AUTOTHYSIS     = 1L << 26;
    public static final long W0_TRAP_JAW                 = 1L << 27;
    public static final long W0_TRAP_MANDIBLE_CATAPULT   = 1L << 28;
    public static final long W0_FORMIC_ACID_ARTILLERY_JET = 1L << 29;
    public static final long W0_FANOUT_ESCAPE_FORMIC_ACID = 1L << 30;
    public static final long W0_NASITE_CHEMICAL_SQUIRT   = 1L << 31;
    public static final long W0_NASITE_VISCOUS_RESIN_SQUIRT = 1L << 32;
    public static final long W0_RESIN_SPRAY              = 1L << 33;
    public static final long W0_ACROBAT_ANT_GASTER_VENOM = 1L << 34;
    public static final long W0_HOT_BALL_THERMAL_DEFENSE = 1L << 35;
    public static final long W0_SUCTION_ESCAPE           = 1L << 36;

    // Social Organization
    public static final long W0_ROYAL_INHIBITION         = 1L << 37;
    public static final long W0_CASTE_RATIO_INHIBITION   = 1L << 38;
    public static final long W0_TROPHALLACTIC_OVARY_INHIB = 1L << 39;
    public static final long W0_CHC_GESTALT_HARMONIZATION = 1L << 40;
    public static final long W0_CUTICULAR_HYDROCARBON    = 1L << 41;
    public static final long W0_UNICOLONIALITY           = 1L << 42;
    public static final long W0_GAMERGATER_DOMINANCE     = 1L << 43;
    public static final long W0_QUEEN_PIPING             = 1L << 44;
    public static final long W0_QUEEN_RECOGNITION_STRIDULATION = 1L << 45;
    public static final long W0_EGG_LAYING_SYNC_STRIDULATION = 1L << 46;

    // Reproduction
    public static final long W0_NUPTIAL_FLIGHT           = 1L << 47;
    public static final long W0_NUPTIAL_FLIGHT_DRUMMING  = 1L << 48;
    public static final long W0_VIRGIN_QUEEN_PRE_FLIGHT  = 1L << 49;
    public static final long W0_QUEEN_WAX_SEALING        = 1L << 50;
    public static final long W0_QUEEN_HIBERNATION_BURROW = 1L << 51;
    public static final long W0_EGG_CANNIBALISM          = 1L << 52;
    public static final long W0_PEDESTRIAN_SWARM_BUDDING = 1L << 53;
    public static final long W0_EMERGENCY_SWARMING       = 1L << 54;

    // Recruitment & Communication
    public static final long W0_SUBSTRATE_ACOUSTIC       = 1L << 55;
    public static final long W0_STRIDULATION_RESCUE      = 1L << 56;
    public static final long W0_TREMBLE_DANCE            = 1L << 57;
    public static final long W0_ESCAPE_PHEROMONE         = 1L << 58;
    public static final long W0_POLYCALIC_NETWORK        = 1L << 59;
    public static final long W0_DEPLETION_TRAIL          = 1L << 60;
    public static final long W0_INJURED_PHEROMONE_STRETCHER = 1L << 61;
    public static final long W0_HATCHING_ENTHUSIASM_VIBRATO = 1L << 62;
    public static final long W0_GUARD_SHIFT_VIBRATIONAL_WHISPER = 1L << 63;

    // ─────────── WORD 1 (bits 64-127) ────────────────────────────────────────
    // Nest Construction
    public static final long W1_GRAVEL_PLUGGING          = 1L << 0;
    public static final long W1_STERCORAL_CEMENT         = 1L << 1;
    public static final long W1_CLAY_PILLAR              = 1L << 2;
    public static final long W1_CLAY_VAULT_ARCH          = 1L << 3;
    public static final long W1_CLAY_WALL_FUNGAL_AERATION = 1L << 4;
    public static final long W1_CLAY_BREACH_REPAIR       = 1L << 5;
    public static final long W1_MUD_RESIN_ENTRANCE_FUNNEL = 1L << 6;
    public static final long W1_PAPER_PULP_CARTON_MASTICATION = 1L << 7;
    public static final long W1_WOOD_PULP_CARTON_SCRAPE  = 1L << 8;
    public static final long W1_HONEY_STORE_BRICK_PLUGGING = 1L << 9;
    public static final long W1_VERTICAL_DRAINAGE_SHAFT  = 1L << 10;
    public static final long W1_SUBTERRANEAN_CLAY_AQUEDUCT = 1L << 11;
    public static final long W1_SPHAGNUM_MOISTURE_DOME   = 1L << 12;
    public static final long W1_PHONIC_ISOLATION_CHAMBER = 1L << 13;
    public static final long W1_PASSALID_WOOD_WALL_PLASTER = 1L << 14;
    public static final long W1_BEETLE_FRASS_GALLERY_PLASTER = 1L << 15;
    public static final long W1_WASP_PEDICAL_ANT_REPELLENT = 1L << 16;
    public static final long W1_HYDROPHOBIC_TRAIL_COATING = 1L << 17;
    public static final long W1_CHAFF_GARBAGE_DUNE       = 1L << 18;
    public static final long W1_PHEROMONE_CLIMATE        = 1L << 19;
    public static final long W1_PITFALL_TRAP_EXCAVATION  = 1L << 20;
    public static final long W1_COLLAPSIBLE_PIT_TRAP     = 1L << 21;

    // Thermoregulation
    public static final long W1_SOLAR_MOUND              = 1L << 22;
    public static final long W1_EVAPORATIVE_COOLING      = 1L << 23;
    public static final long W1_THORACIC_INCUBATION      = 1L << 24;
    public static final long W1_SOCIAL_THERMOREGULATION  = 1L << 25;
    public static final long W1_SOLAR_BROOD_BASKING      = 1L << 26;
    public static final long W1_BROOD_WING_FANNING       = 1L << 27;
    public static final long W1_BUMBLEBEE_ABDOMINAL_INCUBATION = 1L << 28;
    public static final long W1_MOUND_OVERHEAT_VIBRATO   = 1L << 29;
    public static final long W1_HONEYBEE_SWARM_CORE_HEAT_SHIELD = 1L << 30;
    public static final long W1_WASP_NEST_WATER_COOLING  = 1L << 31;
    public static final long W1_GLYCEROL_CRYOPROTECTION  = 1L << 32;
    public static final long W1_SUB_ZERO_BUMBLEBEE_FORAGING = 1L << 33;
    public static final long W1_THERMAL_TRAIL_DECAY      = 1L << 34;
    public static final long W1_THERMOREGULATED_AIR_WATER_CONDUIT = 1L << 35;
    public static final long W1_TERMITE_THERMAL_CHIMNEY_FLUE = 1L << 36;
    public static final long W1_PULSATILE_VENTILATION    = 1L << 37;
    public static final long W1_PULSED_AIR_CONVECTIVE_VENTILATION = 1L << 38;

    // Foraging & Resource Acquisition
    public static final long W1_APHID_FARMING            = 1L << 39;
    public static final long W1_APHID_HONEYDEW_MILKING   = 1L << 40;
    public static final long W1_APHID_HONEYDEW_SIGNALING = 1L << 41;
    public static final long W1_APHID_SANITARY_CORDON    = 1L << 42;
    public static final long W1_HONEYPOT_STORAGE         = 1L << 43;
    public static final long W1_HONEYPOT_REPLETE_STORAGE = 1L << 44;
    public static final long W1_SEED_STORAGE             = 1L << 45;
    public static final long W1_GRANARY_SEED_AERATION    = 1L << 46;
    public static final long W1_HARVESTER_BREAD_PULP_CHEW = 1L << 47;
    public static final long W1_HARVESTER_SEED_RADICLE_MUTILATION = 1L << 48;
    public static final long W1_RELAY_SEED_TRANSPORT     = 1L << 49;
    public static final long W1_DEW_CONDENSATION_HARVEST = 1L << 50;
    public static final long W1_MANDIBLE_DROPLET_WATER_TRANSPORT = 1L << 51;
    public static final long W1_CORBICULA_POLLEN_PACKING = 1L << 52;
    public static final long W1_BUZZ_POLLINATION         = 1L << 53;
    public static final long W1_BUZZ_POLLINATION_SONICATION = 1L << 54;
    public static final long W1_BUMBLEBEE_NECTAR_WAX_POT = 1L << 55;
    public static final long W1_BUMBLEBEE_NECTAR_TONGUE_LAPPING = 1L << 56;
    public static final long W1_BUMBLEBEE_NECTAR_THEFT_HOLE_BITE = 1L << 57;
    public static final long W1_FERMENTED_SAP_ANESTHETIC = 1L << 58;
    public static final long W1_SALT_CRYSTAL_OSMOREGULATION = 1L << 59;
    public static final long W1_TOXIC_PLANT_RESIN_RAID   = 1L << 60;
    public static final long W1_PHENOLIC_RESIN_MEDICATION = 1L << 61;
    public static final long W1_ABANDONED_WAX_VAULT_RAID = 1L << 62;

    // ─────────── WORD 2 (bits 128-191) ───────────────────────────────────────
    // Fungiculture & Symbiosis
    public static final long W2_FUNGUS_WEEDING           = 1L << 0;
    public static final long W2_ATTA_LEAF_CRESCENT_SHEAR = 1L << 1;
    public static final long W2_ATTA_GARDEN_WASTE_CHAMBER_DIG = 1L << 2;
    public static final long W2_LEAF_PULP_ENZYME_INOCULATION = 1L << 3;
    public static final long W2_TERMITE_FUNGAL_COMB      = 1L << 4;
    public static final long W2_TERMITE_FUNGAL_WASTE_BURIAL = 1L << 5;
    public static final long W2_SUBTERRANEAN_FUNGUS_WOOD  = 1L << 6;
    public static final long W2_STREPTOMYCES_ANTIBIOTICS = 1L << 7;
    public static final long W2_TERMITE_GUT_SYMBIOSIS    = 1L << 8;
    public static final long W2_TERMITE_PROTOZOA_TROPHALLAXIS = 1L << 9;
    public static final long W2_DOMALIA_MUTUALISM        = 1L << 10;
    public static final long W2_HOST_PLANT_CHEMICAL_CAMOUFLAGE = 1L << 11;

    // Nest Architecture Advanced
    public static final long W2_WEAVER_SILK              = 1L << 12;
    public static final long W2_WEAVER_LEAF_PULLING_CHAIN = 1L << 13;
    public static final long W2_WEAVER_SILK_PAVILION_APHID_SHELTER = 1L << 14;
    public static final long W2_LARVAL_SILK_HARNESS      = 1L << 15;
    public static final long W2_LARVAL_SILK_CANOPY_BRIDGE = 1L << 16;
    public static final long W2_PROPOLIS_SHIELD          = 1L << 17;
    public static final long W2_HONEYBEE_PROPOLIS_NEST_SEAL = 1L << 18;
    public static final long W2_STENOGASTRINE_PAPER_JELLY_WEAVING = 1L << 19;
    public static final long W2_BEE_BREAD_HYDROPHOBIC_COATING = 1L << 20;
    public static final long W2_RESIN_NYMPHAL_MUMMIFICATION = 1L << 21;
    public static final long W2_CATERPILLAR_SILK_HAMMOCK_TENT = 1L << 22;
    public static final long W2_PROCESSIONARY_SILK_TRAIL = 1L << 23;

    // Slavery & Parasitism
    public static final long W2_DULOSIS_RAID             = 1L << 24;
    public static final long W2_ROBBER_BEE               = 1L << 25;
    public static final long W2_PARASITE_SILK_BINDING    = 1L << 26;
    public static final long W2_PARASITE_QUARANTINE      = 1L << 27;
    public static final long W2_PARASITIZED_CADAVER_REPELLENT = 1L << 28;
    public static final long W2_SELF_ISOLATION           = 1L << 29;

    // Territory & Conflict
    public static final long W2_TERRITORIAL_REPELLENT    = 1L << 30;
    public static final long W2_RITUAL_JOUSTING          = 1L << 31;
    public static final long W2_RITUAL_MANDIBULAR_WRESTLING = 1L << 32;
    public static final long W2_WASP_DOMINANCE_MOUNTING  = 1L << 33;
    public static final long W2_WASP_FACIAL_RECOGNITION  = 1L << 34;

    // Army Ant & Nomadic
    public static final long W2_LIVING_BIVOUAC           = 1L << 35;
    public static final long W2_BIOMECHANICAL_BIVOUAC    = 1L << 36;
    public static final long W2_LIVING_BRIDGE            = 1L << 37;
    public static final long W2_TARSAL_FRICTION_BRIDGE   = 1L << 38;
    public static final long W2_CHAIN_BROOD_TRANSPORT    = 1L << 39;
    public static final long W2_SELF_ASSEMBLED_RAFT      = 1L << 40;
    public static final long W2_FLOATING_ANT_RAFT        = 1L << 41;
    public static final long W2_RAIN_EVACUATION_SIPHON   = 1L << 42;

    // Lifecycle & Development
    public static final long W2_PHRAGMOSIS               = 1L << 43;
    public static final long W2_OLEIC_ACID_NECROPHORESIS = 1L << 44;
    public static final long W2_NECROPHORESIS            = 1L << 45;
    public static final long W2_REFUSE_SORTING           = 1L << 46;
    public static final long W2_LARVAL_EXUVIA_CHITIN_RECYCLING = 1L << 47;
    public static final long W2_LARVAL_WOOD_DUST_DRYING  = 1L << 48;
    public static final long W2_EGG_MASS_MUCILAGE_ENVELOPE = 1L << 49;
    public static final long W2_EARWIG_EGG_LICKING_GROOMING = 1L << 50;
    public static final long W2_EARWIG_MATERNAL_REGURGITATION = 1L << 51;
    public static final long W2_EARWIG_NYMPH_CUTICULAR_GROOMING = 1L << 52;
    public static final long W2_MATERNAL_SHIELD_GUARDING = 1L << 53;
    public static final long W2_DIAPAUSE                 = 1L << 54;

    // Flood & Disaster Response
    public static final long W2_FLOOD_EVACUATION         = 1L << 55;
    public static final long W2_UNIVERSAL_EMERGENCY_EVACUATION = 1L << 56;

    // Wasp-specific
    public static final long W2_WASP_ANTENNAL_DRUMMING  = 1L << 57;
    public static final long W2_WASP_CELL_RIM_DRUMMING  = 1L << 58;
    public static final long W2_WASP_EMERGENCY_SALIVA_FOOD_DROP = 1L << 59;
    public static final long W2_WASP_WATER_DOUSING       = 1L << 60;
    public static final long W2_HORNET_GROUP_ALARM_PHEROMONE = 1L << 61;
    public static final long W2_GIANT_HONEYBEE_SHIMMERING_WAVE = 1L << 62;
    public static final long W2_QUEEN_PIPING_SIGNAL      = 1L << 63;

    // ─────────── WORD 3 (bits 192-255) ───────────────────────────────────────
    // Termite-specific
    public static final long W3_TERMITE_SOLDIER_ALARM_DRUM_SYNCHRONY = 1L << 0;
    public static final long W3_TERMITE_MANDIBLE_SNAP_ALARM = 1L << 1;
    public static final long W3_TERMITE_SALIVA_CEMENT_MOISTURE_SEAL = 1L << 2;
    public static final long W3_TERMITE_ROYAL_CHAMBER_BLOCKADE = 1L << 3;
    public static final long W3_TERMITE_ROYAL_PAIR_GROOMING = 1L << 4;
    public static final long W3_TERMITE_QUEEN_PHYSOGASTRIC_EGG_PERISTALSIS = 1L << 5;

    // Passalid Beetle
    public static final long W3_PASSALID_PARENTAL_STRIDULATION = 1L << 6;
    public static final long W3_PASSALID_GRUB_HUNGER_STRIDULATION = 1L << 7;
    public static final long W3_PASSALID_SUBSTRATE_DUET  = 1L << 8;
    public static final long W3_PASSALID_WOOD_FRASS_TROPHALLAXIS = 1L << 9;

    // Thrips
    public static final long W3_THRIPS_GALL_FORELEG_SQUEEZING = 1L << 10;
    public static final long W3_THRIPS_GALL_REPAIR_SECRETION = 1L << 11;
    public static final long W3_THRIPS_CHITINOUS_TUBE_PLUG = 1L << 12;

    // Spider
    public static final long W3_SPIDER_COMMUNAL_SILK_PREY_WRAP = 1L << 13;
    public static final long W3_SPIDER_CRECHE_REGURGITATION = 1L << 14;
    public static final long W3_SPIDER_DRAGLINE_SIGNAL_WIRE = 1L << 15;
    public static final long W3_SPIDER_GARBAGE_CHUTE     = 1L << 16;
    public static final long W3_SPIDER_WEB_DEBRIS_CAMOUFLAGE = 1L << 17;
    public static final long W3_COMMUNAL_SPIDER_SILK     = 1L << 18;

    // Aphid-specific
    public static final long W3_APHID_FORELEG_INTRUDER_SQUEEZE = 1L << 19;
    public static final long W3_APHID_GALL_CLOSING_FLUID = 1L << 20;
    public static final long W3_APHID_SOLDIER_HORN_STABBING = 1L << 21;

    // Shrimp & Aquatic
    public static final long W3_SHRIMP_ACOUSTIC_CANNON   = 1L << 22;
    public static final long W3_EUSOCIAL_SHRIMP_CLAW_SHOCKWAVE = 1L << 23;

    // Shield Bug & Parent Bug
    public static final long W3_SHIELD_BUG_PARASITOID_SHIELD = 1L << 24;
    public static final long W3_PARENT_BUG_ALARM_GATHERING = 1L << 25;

    // Additional misc behaviors
    public static final long W3_MOUND_SOLAR_HEAT_COLLECTOR = 1L << 26;
    public static final long W3_MAGNETIC_MOUND_ORIENTATION = 1L << 27;
    public static final long W3_EUSOCIAL_BIOSTRUCTURE    = 1L << 28;
    public static final long W3_ACOUSTIC_SURGE           = 1L << 29;
    public static final long W3_DUST_SUBSTRATE_CAMOUFLAGE = 1L << 30;
    public static final long W3_CUTICLE_WATER_CONDENSATION = 1L << 31;
    public static final long W3_DROUGHT_SOIL_MOISTURE_VIBRATO = 1L << 32;
    public static final long W3_SUBSTRATE_OBSTACLE_VIBRATO = 1L << 33;
    public static final long W3_NOCTURNAL_INFRARED_HUNTING = 1L << 34;
    public static final long W3_PREY_SIZE_SELECTIVE_PHEROMONE = 1L << 35;
    public static final long W3_LARGE_INTRUDER_CLAY_ENCAPSULATION = 1L << 36;
    public static final long W3_MOUND_OVERHEAT_VIBRATO   = 1L << 37;

    // ─────────── INSTANCE FIELDS ──────────────────────────────────────────────

    /** 64 behaviors packed in word 0 */
    public long caps0 = W0_ALLOGROOMING | W0_STRIDULATION_RESCUE | W0_ROYAL_INHIBITION;
    /** 64 behaviors packed in word 1 */
    public long caps1 = W1_GRAVEL_PLUGGING;
    /** 64 behaviors packed in word 2 */
    public long caps2 = 0L;
    /** 64 behaviors packed in word 3 */
    public long caps3 = 0L;

    // ── Live behavioral state floats ──────────────────────────────────────────
    public boolean isStridulating = false;
    public float stridulationFrequencyHz = 850.0f;
    public boolean carryingBuildingMaterial = false;
    public float stercoralMortarAmount = 0.0f;
    public boolean inLivingBivouac = false;
    public boolean isRafting = false;
    public float rafDensity = 0.0f;
    public float thermalThoraxTempC = 25.0f;
    public float propolisCarried = 0.0f;
    public float honeypotFillRatio = 0.0f;
    public boolean isTremble = false;
    public boolean hasAutothysed = false;
    public boolean diapauseActive = false;

    // ── Convenience helpers ───────────────────────────────────────────────────
    /** Returns true if the entity has the given word-0 capability flag set. */
    public boolean has0(long flag) { return (caps0 & flag) != 0L; }
    /** Returns true if the entity has the given word-1 capability flag set. */
    public boolean has1(long flag) { return (caps1 & flag) != 0L; }
    /** Returns true if the entity has the given word-2 capability flag set. */
    public boolean has2(long flag) { return (caps2 & flag) != 0L; }
    /** Returns true if the entity has the given word-3 capability flag set. */
    public boolean has3(long flag) { return (caps3 & flag) != 0L; }

    /** Total count of active capabilities across all 4 words. */
    public int countActiveBehaviors() {
        return Long.bitCount(caps0) + Long.bitCount(caps1)
             + Long.bitCount(caps2) + Long.bitCount(caps3);
    }
}
