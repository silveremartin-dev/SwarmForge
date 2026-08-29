/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Functional role and ecological classification of a species within the simulation.
 * Distinguishes primary colony-building eusocial species from accessory environmental fauna.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public enum SpeciesCategory {
    /** Primary colony-forming eusocial insects (Monogyne / Standard Colony Builder). */
    EUSOCIAL_PRIMARY("🐜 Primary Eusocial (Colony Builder)"),

    /** Polygyne supercolony forming species (Formica lugubris, Linepithema humile). */
    EUSOCIAL_POLYGYNE("👑 Polygyne Eusocial (Supercolony)"),

    /** Social parasites or temporary host-nest invading queens (Lasius fuliginosus, Bothriomyrmex). */
    PARASITIC_QUEEN("🐝 Social Parasite / Inquiline Queen"),

    /** Subsocial or incipient eusocial insects with primitive worker castes (Halictidae, Polistes). */
    SUBSOCIAL_INCIPIENT("🐜 Subsocial / Primitive Eusocial"),

    /** Commensal species living inside or around host nests (myrmecophiles, beetles). */
    COMMENSAL("🪲 Commensal / Nest Guest"),

    /** Sap-sucking insects farmed for honeydew (Aphids, Scale Insects, Treehoppers). */
    HONEYDEW_PRODUCER("🌱 Honeydew Producer (Aphids)"),

    /** Prey organisms foraging target for predators (Caterpillars, Flies, Grubs). */
    PREY_ORGANISM("🐛 Prey Organism / Food Source"),

    /** Soil-agitating decomposers and burrowers (Earthworms, Woodlice/Isopods, Springtails). */
    SOIL_FAUNA("🪱 Soil Fauna & Decomposer"),

    /** Parasites or active predators targeting colonies (Mites, Ant Lions, Parasitic Wasps). */
    PARASITE_PREDATOR("🦂 Parasite / Active Predator");

    public final String label;

    SpeciesCategory(String label) {
        this.label = label;
    }
}
