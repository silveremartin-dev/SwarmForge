/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;

/**
 * Represents a food source in the world.
 * Ants can detect, collect, and deplete food sources.
 */
public class FoodSource {

    private final UUID id;
    private float x, y, z;
    private float quantity;
    private final ResourceType type;

    public FoodSource(float x, float y, float z, float quantity, ResourceType type) {
        this.id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.z = z;
        this.quantity = quantity;
        this.type = type;
    }

    /**
     * Take food from this source.
     * 
     * @param amount Amount to take
     * @return Actual amount taken (may be less if depleted)
     */
    public float take(float amount) {
        float taken = Math.min(amount, quantity);
        quantity -= taken;
        return taken;
    }

    public boolean isDepleted() {
        return quantity <= 0;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getQuantity() {
        return quantity;
    }

    protected void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public ResourceType getType() {
        return type;
    }

    /**
     * Update food source state (regeneration, decay, etc).
     */
    public void tick() {
        // Default: do nothing
    }
}
