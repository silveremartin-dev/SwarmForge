/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain.traits;

import java.io.Serializable;

/**
 * Cuticular Hydrocarbon (CHC) Gestalt Odor Profile & Nestmate Recognition Trait.
 * Grounded in empirical myrmecological chemical ecology (Bray-Curtis dissimilarity metric).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ChcProfileTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final float RECOGNITION_THRESHOLD = 0.28f;

    private float[] profile = new float[]{0.5f, 0.5f, 0.5f, 0.5f};

    public float[] getProfile() {
        return profile;
    }

    public void setProfile(float[] newProfile) {
        if (newProfile != null && newProfile.length > 0) {
            this.profile = newProfile.clone();
        }
    }

    /**
     * Calculates Bray-Curtis CHC dissimilarity index d_BC between this profile and another:
     * d_BC(u, v) = sum(|u_k - v_k|) / sum(u_k + v_k)
     * Returns a distance in [0.0, 1.0].
     */
    public float calculateDissimilarity(ChcProfileTrait other) {
        if (other == null || other.profile == null || this.profile == null) {
            return 1.0f;
        }
        float num = 0.0f;
        float den = 0.0f;
        int len = Math.min(this.profile.length, other.profile.length);
        for (int i = 0; i < len; i++) {
            num += Math.abs(this.profile[i] - other.profile[i]);
            den += (this.profile[i] + other.profile[i]);
        }
        return (den > 0.0001f) ? (num / den) : 0.0f;
    }

    /**
     * Determines whether the other CHC profile falls within the gestalt envelope.
     */
    public boolean isNestmateRecognized(ChcProfileTrait other) {
        return calculateDissimilarity(other) <= RECOGNITION_THRESHOLD;
    }

    /**
     * Homogenizes (blends) this profile with another individual's profile through social contact.
     */
    public void homogenize(ChcProfileTrait partner, float factor) {
        if (partner == null || partner.profile == null || this.profile == null) return;
        float f = Math.max(0.0f, Math.min(0.5f, factor));
        int len = Math.min(this.profile.length, partner.profile.length);
        for (int i = 0; i < len; i++) {
            float blend = this.profile[i] * (1.0f - f) + partner.profile[i] * f;
            this.profile[i] = blend;
        }
    }
}
