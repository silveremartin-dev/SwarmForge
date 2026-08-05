/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import java.io.Serializable;

/**
 * Defines the 13 Nest Typologies in SwarmForge.
 * Each typology specifies distinct structural baseline parameters, stack effect chimney draft multipliers,
 * thermal insulation values, and collapse resilience.
 */
public enum NestType implements Serializable {
    /** 1. Simple subterranean gallery system (e.g., Lasius niger) */
    SUBTERRANEAN_SIMPLE("Subterranean Simple", 1.0f, 0.8f, 1.0f, 1.5f),

    /** 2. Twig/thatch mound nest (e.g., Formica rufa) */
    SUBTERRANEAN_MOUND("Subterranean Mound", 1.8f, 1.5f, 0.6f, 0.8f),

    /** 3. Arboreal carton nest (e.g., Crematogaster) */
    ARBOREAL_CARTON("Arboreal Carton", 1.3f, 1.2f, 1.2f, 4.0f),

    /** 4. Leaf-sewn canopy nest (e.g., Oecophylla smaragdina) */
    ARBOREAL_WEAVER("Arboreal Leaf-Weaver", 1.1f, 0.5f, 0.8f, 2.5f),

    /** 5. Volcanic-crater cone nest for desert thermal venting (e.g., Pogonomyrmex) */
    CRATER_NEST("Crater Cone Nest", 2.2f, 0.7f, 0.5f, 1.0f),

    /** 6. High turret chimney nest maximizing stack effect (e.g., Odontomachus) */
    TOWER_CHIMNEY("Tower Chimney Nest", 3.0f, 0.9f, 0.9f, 2.0f),

    /** 7. Deep vertical shaft desert nest (e.g., Cataglyphis bombycina) */
    CATAGLYPHIS_DEEP("Cataglyphis Deep Shaft", 1.5f, 1.8f, 1.1f, 3.5f),

    /** 8. Termite-style cathedral ventilation duct nest (e.g., Macrotermes) */
    TERMITE_CATHEDRAL("Termite Cathedral", 3.5f, 2.0f, 1.5f, 5.0f),

    /** 9. Dead tree trunk cavity nest (e.g., Camponotus herculeanus) */
    CAVITY_TREE("Tree Cavity Nest", 1.4f, 1.6f, 1.4f, 6.0f),

    /** 10. Isolated soil pocket nest (e.g., Solenopsis fugax) */
    HYPOGAEIC_POCKET("Hypogaeic Soil Pocket", 0.6f, 1.1f, 0.7f, 1.2f),

    /** 11. Sub-lithic rock nest (e.g., Aphaenogaster senilis) */
    LITHOPHILIC("Lithophilic Rock Nest", 0.9f, 2.2f, 1.8f, 4.5f),

    /** 12. Hollow bamboo internode nest (e.g., Tetraponera) */
    SUSPENDED_BAMBOO("Suspended Bamboo Nest", 1.6f, 1.4f, 1.3f, 7.0f),

    /** 13. Polydomous multi-satellite nest network (e.g., Linepithema humile) */
    POLYDOMOUS_NETWORK("Polydomous Network", 1.7f, 1.0f, 0.9f, 1.8f);

    private final String displayName;
    private final float chimneyDraftMultiplier;
    private final float thermalInsulation;
    private final float baselineCo2PurgeRate;
    private final float structuralIntegrityBaseline;

    NestType(String displayName, float chimneyDraftMultiplier, float thermalInsulation,
             float baselineCo2PurgeRate, float structuralIntegrityBaseline) {
        this.displayName = displayName;
        this.chimneyDraftMultiplier = chimneyDraftMultiplier;
        this.thermalInsulation = thermalInsulation;
        this.baselineCo2PurgeRate = baselineCo2PurgeRate;
        this.structuralIntegrityBaseline = structuralIntegrityBaseline;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getChimneyDraftMultiplier() {
        return chimneyDraftMultiplier;
    }

    public float getThermalInsulation() {
        return thermalInsulation;
    }

    public float getBaselineCo2PurgeRate() {
        return baselineCo2PurgeRate;
    }

    public float getStructuralIntegrityBaseline() {
        return structuralIntegrityBaseline;
    }
}
