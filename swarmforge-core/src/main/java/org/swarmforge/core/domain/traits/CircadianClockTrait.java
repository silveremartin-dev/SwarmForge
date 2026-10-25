/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain.traits;

import java.io.Serializable;

/**
 * Endogenous Circadian Clock and Polyphasic Sleep Rhythm Trait.
 * Models phase oscillations [0, 2pi] and sleep-wake state cycles in social insects.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CircadianClockTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    private float circadianPhase = 0.0f; // [0, 2pi]
    private boolean polyphasicSleeping = false;

    public float getPhase() {
        return circadianPhase;
    }

    public void setPhase(float phase) {
        this.circadianPhase = (float) (phase % (2.0 * Math.PI));
        if (this.circadianPhase < 0) {
            this.circadianPhase += (float) (2.0 * Math.PI);
        }
    }

    public boolean isPolyphasicSleeping() {
        return polyphasicSleeping;
    }

    public void setPolyphasicSleeping(boolean sleeping) {
        this.polyphasicSleeping = sleeping;
    }

    public void advancePhase(float deltaRadians) {
        setPhase(this.circadianPhase + deltaRadians);
    }
}
