package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Compact 16-Bit Pathogen & Contagion Bitmask Component.
 * Encodes active viral/fungal pathogen infection states into a packed 16-bit bitmask,
 * enabling O(1) epidemic infection checks and zero-allocation viral load tracking.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class CompactPathogenBitmaskComponent extends Component {

    public static final short PATHOGEN_NONE        = 0;
    public static final short PATHOGEN_METARHIZIUM  = 1 << 0; // Fungal spore
    public static final short PATHOGEN_CBPV         = 1 << 1; // Chronic Bee Paralysis Virus
    public static final short PATHOGEN_VARROA_MITES = 1 << 2; // Parasitic mite infestation
    public static final short PATHOGEN_NOSEMA_SPORE = 1 << 3; // Microsporidian gut parasite

    public short activePathogensBitmask = PATHOGEN_NONE;
    public short viralLoadPacked = 0; // 0..65535 scale

    public boolean isInfectedWith(short pathogenFlag) {
        return (activePathogensBitmask & pathogenFlag) != 0;
    }

    public void addInfection(short pathogenFlag) {
        activePathogensBitmask |= pathogenFlag;
    }

    public void clearInfections() {
        activePathogensBitmask = PATHOGEN_NONE;
        viralLoadPacked = 0;
    }
}
