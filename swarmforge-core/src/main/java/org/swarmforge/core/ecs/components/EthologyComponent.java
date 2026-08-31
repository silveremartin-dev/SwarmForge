package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * ECS Component representing behavioral capabilities and ethological states
 * for the 30 eusocial behaviors documented in BEHAVIORAL_ETHOLOGY_SPECIFICATION.md.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EthologyComponent extends Component {

    // Capability Flags Bitmask
    public static final long FLAG_MAGNETORECEPTION          = 1L << 0;
    public static final long FLAG_APHID_FARMING             = 1L << 1;
    public static final long FLAG_ROYAL_INHIBITION          = 1L << 2;
    public static final long FLAG_SUBSTRATE_ACOUSTIC        = 1L << 3;
    public static final long FLAG_POLYCALIC_NETWORK         = 1L << 4;
    public static final long FLAG_PROPOLIS_SHIELD           = 1L << 5;
    public static final long FLAG_WEAVER_SILK               = 1L << 6;
    public static final long FLAG_FUNGUS_WEEDING            = 1L << 7;
    public static final long FLAG_AUTOTHYSIS                = 1L << 8;
    public static final long FLAG_STERCORAL_CEMENT          = 1L << 9;
    public static final long FLAG_PROCTODEAL_TROPHALLAXIS   = 1L << 10;
    public static final long FLAG_PHRAGMOSIS                = 1L << 11;
    public static final long FLAG_EVAPORATIVE_COOLING       = 1L << 12;
    public static final long FLAG_TRAP_JAW                  = 1L << 13;
    public static final long FLAG_DULOSIS_RAID              = 1L << 14;
    public static final long FLAG_LIVING_BIVOUAC            = 1L << 15;
    public static final long FLAG_SOLAR_MOUND               = 1L << 16;
    public static final long FLAG_ALLOGROOMING              = 1L << 17;
    public static final long FLAG_TREMBLE_DANCE             = 1L << 18;
    public static final long FLAG_THERMAL_TRAIL_DECAY       = 1L << 19;
    public static final long FLAG_THORACIC_INCUBATION       = 1L << 20;
    public static final long FLAG_RITUAL_JOUSTING           = 1L << 21;
    public static final long FLAG_TERRITORIAL_REPELLENT     = 1L << 22;
    public static final long FLAG_FLOOD_EVACUATION          = 1L << 23;
    public static final long FLAG_ROBBER_BEE                = 1L << 24;
    public static final long FLAG_STRIDULATION_RESCUE       = 1L << 25; // Prop 6
    public static final long FLAG_HONEYPOT_STORAGE          = 1L << 26;
    public static final long FLAG_GRAVEL_PLUGGING           = 1L << 27; // Prop 7
    public static final long FLAG_OLEIC_ACID_NECROPHORESIS  = 1L << 28;
    public static final long FLAG_UV_POLARIZED_NAV          = 1L << 29;

    public long activeCapabilities = FLAG_ALLOGROOMING | FLAG_STRIDULATION_RESCUE | FLAG_GRAVEL_PLUGGING | FLAG_ROYAL_INHIBITION;

    // Stridulation signal state (Prop 6)
    public boolean isStridulating = false;
    public float stridulationFrequencyHz = 850.0f; // ~85 dB acoustic signal through soil

    // Nest building & stercoral gravel plugging state (Prop 7)
    public boolean carryingBuildingMaterial = false;
    public float stercoralMortarAmount = 0.0f;
}
