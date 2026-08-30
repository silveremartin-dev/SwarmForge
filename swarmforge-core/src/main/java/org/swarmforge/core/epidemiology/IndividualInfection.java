/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.epidemiology;

import java.io.Serializable;
import java.util.UUID;

/**
 * Tracks pathogen load, infection stage, incubation timer, and social immunity level for an individual ant.
 */
public class IndividualInfection implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID individualId;
    private InfectionState state;
    private PathogenType activePathogen;
    private float cuticularSporeLoad; // 0.0 to 1.0
    private float internalInfectionLoad; // 0.0 to 1.0
    private int incubationProgressTicks;
    private float socialImmunityLevel; // 0.0 to 1.0 (acquired protection)

    public IndividualInfection(UUID individualId) {
        this.individualId = individualId;
        this.state = InfectionState.SUSCEPTIBLE;
        this.activePathogen = null;
        this.cuticularSporeLoad = 0.0f;
        this.internalInfectionLoad = 0.0f;
        this.incubationProgressTicks = 0;
        this.socialImmunityLevel = 0.0f;
    }

    public void exposeToSpores(PathogenType pathogen, float sporeAmount) {
        if (state == InfectionState.IMMUNE || state == InfectionState.SPORULATING_DEAD) {
            return;
        }

        // Apply social immunity resistance
        float effectiveAmount = sporeAmount * (1.0f - socialImmunityLevel * 0.7f);
        this.cuticularSporeLoad = Math.min(1.0f, this.cuticularSporeLoad + effectiveAmount);

        if (state == InfectionState.SUSCEPTIBLE && cuticularSporeLoad > 0.05f) {
            this.state = InfectionState.EXPOSED;
            this.activePathogen = pathogen;
            this.incubationProgressTicks = 0;
        }
    }

    public void groomSporeLoad(float removalFraction) {
        if (state == InfectionState.EXPOSED) {
            this.cuticularSporeLoad *= (1.0f - Math.max(0.0f, Math.min(1.0f, removalFraction)));
            if (this.cuticularSporeLoad <= 0.05f) {
                this.state = InfectionState.SUSCEPTIBLE;
                // Low exposure builds social immunity!
                this.socialImmunityLevel = Math.min(1.0f, this.socialImmunityLevel + 0.15f);
            }
        }
    }

    private float incubationProgressSeconds = 0.0f;

    public void tick(float geneticPathogenResistance, float deltaSeconds) {
        if (state == InfectionState.EXPOSED && activePathogen != null) {
            incubationProgressSeconds += deltaSeconds;
            // Check germination after incubation period in seconds (adjusted by genetic resistance)
            float effectiveIncubationSeconds = activePathogen.getIncubationTicks() * Math.max(0.1f, geneticPathogenResistance);
            if (incubationProgressSeconds >= effectiveIncubationSeconds) {
                this.state = InfectionState.INFECTED;
                this.internalInfectionLoad = 0.1f;
            }
        } else if (state == InfectionState.INFECTED && activePathogen != null) {
            this.internalInfectionLoad = Math.min(1.0f, this.internalInfectionLoad + activePathogen.getBaseLethality() * deltaSeconds);
        }
    }

    public void tick(float geneticPathogenResistance) {
        tick(geneticPathogenResistance, 1.0f);
    }

    public UUID getIndividualId() {
        return individualId;
    }

    public InfectionState getState() {
        return state;
    }

    public void setState(InfectionState state) {
        this.state = state;
    }

    public PathogenType getActivePathogen() {
        return activePathogen;
    }

    public float getCuticularSporeLoad() {
        return cuticularSporeLoad;
    }

    public float getInternalInfectionLoad() {
        return internalInfectionLoad;
    }

    public float getSocialImmunityLevel() {
        return socialImmunityLevel;
    }
}
