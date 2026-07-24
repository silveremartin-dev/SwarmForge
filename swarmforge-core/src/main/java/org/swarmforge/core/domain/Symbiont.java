/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

/**
 * Represents a symbiotic organism living with the colony.
 * Could be mutualistic (aphids) or parasitic (beetles).
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class Symbiont extends Individual {

    public enum SymbiontType {
        APHID, // Produces honeydew
        FUNGUS, // Food source
        BEETLE, // Commensal/Parasitic
        CATERPILLAR // Mutualistic
    }

    private final SymbiontType type;
    private float resourceProductionRate;
    private float resourceStored;

    public Symbiont(java.util.UUID colonyId, SymbiontType type, float x, float y, float z) {
        super(colonyId, Caste.WORKER, x, y, z); // Use WORKER as base caste for now
        this.type = type;
        this.resourceProductionRate = switch (type) {
            case APHID -> 0.1f;
            case FUNGUS -> 0.2f;
            default -> 0.0f;
        };
    }

    public void tick() {
        if (type == SymbiontType.APHID || type == SymbiontType.FUNGUS) {
            resourceStored += resourceProductionRate;
            if (resourceStored > 10f)
                resourceStored = 10f;
        }
    }

    public float harvest() {
        float amount = resourceStored;
        resourceStored = 0;
        return amount;
    }

    public SymbiontType getType() {
        return type;
    }
}
