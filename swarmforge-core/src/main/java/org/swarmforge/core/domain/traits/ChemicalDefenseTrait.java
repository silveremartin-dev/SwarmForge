/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain.traits;

import java.io.Serializable;
import org.swarmforge.core.domain.Individual;

/**
 * Chemical Defense Trait (Formicine acid artillery spraying and chemotactic blindness).
 * Grounded in Formica rufa chemical ecology and venom gland dynamics.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ChemicalDefenseTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final float SPRAY_COST = 20.0f;
    public static final float SPRAY_DAMAGE = 15.0f;
    public static final float MAX_SPRAY_RANGE = 1.5f;
    public static final int BLINDNESS_TICKS = 120; // 2s at 60 TPS

    private float formicAcidGland = 100.0f;
    private int chemotacticBlindnessTicks = 0;

    public float getFormicAcidGland() {
        return formicAcidGland;
    }

    public void refillFormicAcid(float amount) {
        this.formicAcidGland = Math.min(100.0f, this.formicAcidGland + amount);
    }

    public void setFormicAcidGland(float value) {
        this.formicAcidGland = Math.max(0.0f, Math.min(100.0f, value));
    }

    public int getChemotacticBlindnessTicks() {
        return chemotacticBlindnessTicks;
    }

    public void setChemotacticBlindnessTicks(int ticks) {
        this.chemotacticBlindnessTicks = Math.max(0, ticks);
    }

    public boolean isChemotacticallyBlind() {
        return chemotacticBlindnessTicks > 0;
    }

    public void decrementBlindnessTicks() {
        if (chemotacticBlindnessTicks > 0) {
            chemotacticBlindnessTicks--;
        }
    }

    public boolean spray(Individual target, float distance) {
        if (target == null || !target.isAlive() || distance > MAX_SPRAY_RANGE || formicAcidGland < SPRAY_COST) {
            return false;
        }
        formicAcidGland -= SPRAY_COST;
        target.takeDamage(SPRAY_DAMAGE, "Formic Acid Chemical Burn");
        target.setChemotacticBlindnessTicks(BLINDNESS_TICKS);
        return true;
    }
}
