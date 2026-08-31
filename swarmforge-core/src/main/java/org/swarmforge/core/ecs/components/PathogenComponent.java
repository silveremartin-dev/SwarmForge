package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * ECS Component tracking active infections, viral load, fungal spores, and parasites.
 * Uses bitmasks for zero-allocation performance on high-density populations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PathogenComponent extends Component {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_FUNGAL_OPHIOCORDYCEPS = 1 << 0; // 1
    public static final int TYPE_BACTERIAL_GUT = 1 << 1;        // 2
    public static final int TYPE_MITE_VARROA = 1 << 2;          // 4

    public int activePathogens = TYPE_NONE;
    public float viralLoad = 0.0f;        // 0.0 to 100.0
    public float incubationTimer = 0.0f;  // incubation age in seconds
    public float immunityLevel = 100.0f;  // 0.0 to 100.0
    public boolean quarantined = false;
}
