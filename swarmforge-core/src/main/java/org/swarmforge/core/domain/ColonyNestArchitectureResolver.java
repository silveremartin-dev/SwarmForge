/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.species.Species;
import org.swarmforge.core.structure.physics.NestType;

/**
 * Resolves nest architectures and dynamic queen chamber depths based on species characteristics.
 */
public final class ColonyNestArchitectureResolver {

    private ColonyNestArchitectureResolver() {}

    public static NestType resolveNestType(Species species, String speciesName) {
        String arch = species != null ? species.getNestType() : (speciesName != null ? speciesName : "SUBTERRANEAN_SIMPLE");
        if (arch != null) {
            String u = arch.toUpperCase();
            if (u.contains("MOUND") || u.contains("FORMICA")) return NestType.SUBTERRANEAN_MOUND;
            if (u.contains("CATHEDRAL") || u.contains("TERMITE") || u.contains("MACROTERMES")) return NestType.TERMITE_CATHEDRAL;
            if (u.contains("TREE") || u.contains("WOOD") || u.contains("CAVITY") || u.contains("CAMPONOTUS")) return NestType.CAVITY_TREE;
            if (u.contains("WEAVER") || u.contains("OECOPHYLLA")) return NestType.ARBOREAL_WEAVER;
            if (u.contains("CARTON") || u.contains("CREMATOGASTER")) return NestType.ARBOREAL_CARTON;
            if (u.contains("CRATER") || u.contains("POGONOMYRMEX")) return NestType.CRATER_NEST;
            if (u.contains("TOWER") || u.contains("CHIMNEY") || u.contains("ODONTOMACHUS")) return NestType.TOWER_CHIMNEY;
            if (u.contains("CATAGLYPHIS") || u.contains("DESERT")) return NestType.CATAGLYPHIS_DEEP;
            if (u.contains("ROCK") || u.contains("LITHOPHILIC")) return NestType.LITHOPHILIC;
            if (u.contains("BAMBOO")) return NestType.SUSPENDED_BAMBOO;
            if (u.contains("POLYDOMOUS") || u.contains("LINEPITHEMA")) return NestType.POLYDOMOUS_NETWORK;
            if (u.contains("POCKET") || u.contains("SOLENOPSIS")) return NestType.HYPOGAEIC_POCKET;
        }
        return NestType.SUBTERRANEAN_SIMPLE;
    }

    public static float calculateDynamicQueenChamberDepth(Species species, String speciesName, float nestZ) {
        String arch = species != null ? species.getNestType() : (speciesName != null ? speciesName : "MATURE");
        if (arch != null) {
            String u = arch.toUpperCase();
            if (u.contains("FUNGI") || u.contains("ATTA")) return nestZ - 14.0f;
            if (u.contains("MOUND") || u.contains("FORMICA")) return nestZ - 10.0f;
            if (u.contains("CATHEDRAL") || u.contains("TERMITE")) return nestZ - 8.0f;
            if (u.contains("TREE") || u.contains("WOOD") || u.contains("ARBOREAL")) return nestZ + 6.0f;
            if (u.contains("PAPER") || u.contains("VESPA")) return nestZ + 4.0f;
            if (u.contains("WAX")) return nestZ + 2.0f;
        }
        return nestZ - 6.0f;
    }
}
