/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain.traits;

import java.io.Serializable;
import org.swarmforge.core.domain.ResourceType;

/**
 * Social Crop (Ingluvies / Jabot Social) Trait for stomodeal trophallaxis in eusocial insects.
 * Models liquid food retention, capacity, resource categorization, and regurgitation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SocialCropTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    private float capacity = 20.0f;
    private float amount = 0.0f;
    private ResourceType resourceType = null;

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = Math.max(0f, Math.min(capacity, amount));
    }

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity(float capacity) {
        this.capacity = Math.max(0.1f, capacity);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public float fill(float liquidAmount, ResourceType type) {
        if (liquidAmount <= 0) return 0f;
        if (this.amount <= 0.001f) {
            this.resourceType = type;
        }
        float space = capacity - amount;
        float added = Math.min(space, liquidAmount);
        this.amount += added;
        return added;
    }

    public float regurgitate(float requestAmount) {
        if (requestAmount <= 0 || amount <= 0) return 0f;
        float taken = Math.min(amount, requestAmount);
        this.amount -= taken;
        if (this.amount <= 0.001f) {
            this.amount = 0.0f;
            this.resourceType = null;
        }
        return taken;
    }
}
