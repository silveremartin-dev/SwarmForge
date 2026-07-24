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
            "Common European garden ant, moderate aggression, generalist forager",
            LasiusNiger.class),

    ATTA_CEPHALOTES("Atta cephalotes", "Leafcutter Ant",
            "Large colonies, fungus farmers, polymorphic castes",
            AttaCephalotes.class),

    FORMICA_RUFA("Formica rufa", "Wood Ant",
            "Builds large mounds, aggressive defense, sprays formic acid",
            FormicaRufa.class),

    SOLENOPSIS_INVICTA("Solenopsis invicta", "Fire Ant",
            "Highly aggressive, venomous sting, invasive species",
            SolenopsisInvicta.class),

    CAMPONOTUS("Camponotus", "Carpenter Ant",
            "Large ants, nests in wood, nocturnal foragers",
            Camponotus.class);

    private final String scientificName;
    private final String commonName;
    private final String description;
    private final Class<? extends Species> speciesClass;

    SpeciesTemplates(String scientificName, String commonName, String description,
            Class<? extends Species> speciesClass) {
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.description = description;
        this.speciesClass = speciesClass;
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
        try {
            return speciesClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Fallback to Lasius niger
            return new LasiusNiger();
        }
    }

    @Override
    public String toString() {
        return scientificName + " (" + commonName + ")";
    }
}
