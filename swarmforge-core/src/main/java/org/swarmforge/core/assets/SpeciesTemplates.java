/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.assets;

import org.swarmforge.core.species.*;

/**
 * Pre-configured species templates for quick colony setup.
 */
public enum SpeciesTemplates {

    LASIUS_NIGER("Lasius niger", "Black Garden Ant",
            "Common European garden ant, moderate aggression, generalist forager"),

    ATTA_CEPHALOTES("Atta cephalotes", "Leafcutter Ant",
            "Large colonies, fungus farmers, polymorphic castes"),

    FORMICA_RUFA("Formica rufa", "Wood Ant",
            "Builds large mounds, aggressive defense, sprays formic acid"),

    SOLENOPSIS_INVICTA("Solenopsis invicta", "Fire Ant",
            "Highly aggressive, venomous sting, invasive species"),

    CAMPONOTUS("Camponotus ligniperda", "Carpenter Ant",
            "Large ants, nests in wood, nocturnal foragers"),

    APIS_MELLIFERA("Apis mellifera", "Western Honey Bee",
            "Eusocial aerial nectar forager, forms winter thermal clusters"),

    VESPULA_GERMANICA("Vespula germanica", "European Yellowjacket Wasp",
            "Predatory paper wasp, annual colony cycle, high aggression"),

    RETICULITERMES_FLAVIPES("Reticulitermes flavipes", "Eastern Subterranean Termite",
            "Subterranean cellulose consumer, builds shelter tubes from soil cement"),

    PSEUDOREGMA_BAMBUCICOLA("Pseudoregma bambucicola", "Social Bamboo Aphid",
            "Gall-dwelling aphid with sterile 1st-instar horned soldiers"),

    KLADOTHRIPS_HARTERI("Kladothrips harteri", "Acacia Gall Thrips",
            "Australian gall-inducing thrips with wingless soldier caste"),

    AUSTROPLATYPUS_INCOMPERTUS("Austroplatypus incompertus", "Ambrosia Wood Beetle",
            "Eusocial wood-tunneling beetle with sterile female workers");

    private final String scientificName;
    private final String commonName;
    private final String description;

    SpeciesTemplates(String scientificName, String commonName, String description) {
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.description = description;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getDescription() {
        return description;
    }

    public Species createInstance() {
        return SpeciesRegistry.getInstance().get(scientificName)
                .orElseGet(() -> {
                    DefaultSpecies custom = new DefaultSpecies();
                    custom.setScientificName(scientificName);
                    custom.setCommonName(commonName);
                    custom.setDescription(description);
                    return custom;
                });
    }

    @Override
    public String toString() {
        return scientificName + " (" + commonName + ")";
    }
}
