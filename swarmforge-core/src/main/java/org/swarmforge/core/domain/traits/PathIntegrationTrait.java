/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain.traits;

import java.io.Serializable;

/**
 * Celestial Polarized Light & Vectorial Path Integration Trait (e.g. Cataglyphis desert ants).
 * Maintains egocentric homing vector via step integration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PathIntegrationTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    private float pathIntegrationX = 0.0f;
    private float pathIntegrationY = 0.0f;

    public float getX() {
        return pathIntegrationX;
    }

    public float getY() {
        return pathIntegrationY;
    }

    public void setVector(float x, float y) {
        this.pathIntegrationX = x;
        this.pathIntegrationY = y;
    }

    public void reset() {
        this.pathIntegrationX = 0.0f;
        this.pathIntegrationY = 0.0f;
    }

    public void integrate(float dx, float dy) {
        this.pathIntegrationX += dx;
        this.pathIntegrationY += dy;
    }

    public float getDistance() {
        return (float) Math.hypot(pathIntegrationX, pathIntegrationY);
    }

    public float getReturnHeading() {
        return (float) Math.atan2(-pathIntegrationY, -pathIntegrationX);
    }
}
