/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.domain.ResourceType;

/**
 * Represents an Aphid, a "living" food source that produces Honeydew.
 * Behaving as a renewable resource that regenerates over time.
 */
public class Aphid extends FoodSource {

    private final float maxQuantity;
    private final float regenerationRate; // Amount per tick

    public Aphid(float x, float y, float z, float initialQuantity) {
        super(x, y, z, initialQuantity, ResourceType.HONEYDEW);
        this.maxQuantity = initialQuantity;
        this.regenerationRate = 0.05f; // Slow regeneration
    }

    @Override
    public void tick() {
        // Regenerate honeydew if below max
        if (getQuantity() < maxQuantity) {
            setQuantity(Math.min(maxQuantity, getQuantity() + regenerationRate));
        }
    }
}
