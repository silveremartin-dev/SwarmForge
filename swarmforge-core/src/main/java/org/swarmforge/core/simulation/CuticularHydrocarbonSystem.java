/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Cuticular Hydrocarbon (CHC) Colony Recognition & Gestalt Odor System.
 * Simulates epicuticular hydrocarbon blend profiles (n-alkanes, monomethyl-alkanes, dimethyl-alkanes, alkenes).
 * Antennal contact calculates CHC Euclidean distance for nestmate recognition vs. enemy aggression.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CuticularHydrocarbonSystem {

    public static final int CHC_FRACTIONS = 4; // 0=n-Alkanes, 1=Mono-Methyl, 2=Di-Methyl, 3=Alkenes

    /**
     * Generates a colony-specific baseline Cuticular Hydrocarbon (CHC) profile.
     */
    public static float[] generateColonyProfile(java.util.Random random) {
        float[] profile = new float[CHC_FRACTIONS];
        float sum = 0f;
        java.util.Random rng = random != null ? random : java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < CHC_FRACTIONS; i++) {
            profile[i] = (float) (0.2 + rng.nextFloat() * 0.8);
            sum += profile[i];
        }
        for (int i = 0; i < CHC_FRACTIONS; i++) {
            profile[i] /= sum; // Normalize fractions to sum to 1.0
        }
        return profile;
    }

    public static float[] generateColonyProfile() {
        return generateColonyProfile(null);
    }

    /**
     * Computes the CHC Gestalt Odor Similarity Index between two individuals (0.0 to 1.0).
     * 1.0 = identical nestmate odor, < 0.7 = foreign intruder.
     */
    public static float computeOdorSimilarity(float[] profileA, float[] profileB) {
        if (profileA == null || profileB == null || profileA.length != CHC_FRACTIONS || profileB.length != CHC_FRACTIONS) {
            return 1.0f;
        }

        float distance = 0f;
        for (int i = 0; i < CHC_FRACTIONS; i++) {
            float diff = profileA[i] - profileB[i];
            distance += diff * diff;
        }
        float euclideanDist = (float) Math.sqrt(distance);
        return Math.max(0.0f, 1.0f - (euclideanDist * 1.5f));
    }

    /**
     * Evaluates antennal palpation between two individuals and returns interaction decision.
     */
    public static boolean isAcceptedNestmate(Individual a, Individual b) {
        if (a.getColonyId().equals(b.getColonyId())) return true;
        // Foreign intruder check via CHC similarity
        float similarity = computeOdorSimilarity(a.getChcProfile(), b.getChcProfile());
        return similarity >= 0.75f;
    }
}
