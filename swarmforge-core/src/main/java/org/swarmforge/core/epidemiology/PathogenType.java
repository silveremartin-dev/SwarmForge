/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.epidemiology;

import java.io.Serializable;

/**
 * Defines fungal and microbial pathogen strains affecting eusocial colonies:
 * - Beauveria Bassiana (White Muscardine)
 * - Ophiocordyceps (Zombie Ant Fungus)
 * - Metarhizium (Green Muscardine)
 * - Microsporidia (Intracellular Spore Parasite)
 */
public enum PathogenType implements Serializable {
    BEAUVERIA_BASSIANA("Beauveria Bassiana", 0.08f, 0.03f, 150),
    OPHIOCORDYCEPS("Ophiocordyceps Unilateralis", 0.05f, 0.05f, 300),
    METARHIZIUM("Metarhizium Anisopliae", 0.10f, 0.02f, 100),
    MICROSPORIDIA("Microsporidia Nosema", 0.03f, 0.01f, 500);

    private final String name;
    private final float baseInfectivity;   // Germination probability per tick
    private final float baseLethality;     // Damage per tick once infected
    private final int incubationTicks;     // Ticks from exposure to active infection

    PathogenType(String name, float baseInfectivity, float baseLethality, int incubationTicks) {
        this.name = name;
        this.baseInfectivity = baseInfectivity;
        this.baseLethality = baseLethality;
        this.incubationTicks = incubationTicks;
    }

    public String getName() {
        return name;
    }

    public float getBaseInfectivity() {
        return baseInfectivity;
    }

    public float getBaseLethality() {
        return baseLethality;
    }

    public int getIncubationTicks() {
        return incubationTicks;
    }
}
