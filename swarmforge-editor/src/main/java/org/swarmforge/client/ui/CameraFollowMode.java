/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

/**
 * Camera follow mode when tracking an individual insect or predator.
 * - FREE: Smooth position follow with free manual orbit/pan
 * - TPS: Third-Person Follow (chase camera behind ant heading with tilted perspective)
 * - FPS: First-Person Subjective View (camera at ant head level looking forward)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public enum CameraFollowMode {
    FREE,
    TPS,
    FPS
}
