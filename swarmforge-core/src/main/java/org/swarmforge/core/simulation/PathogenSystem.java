/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Pathogen, Parasite & Fungal Infection System.
 * Models entomopathogenic fungi (Cordyceps, Metarhizium, Beauveria),
 * parasite transmission, zombie-ant behavioral alteration, and necrophoresis grooming.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PathogenSystem {

    public enum PathogenType {
        CORDYCEPS_UNILATERALIS, // Zombie fungus (summit disease)
        METARHIZIUM_ANISOPLIAE, // Green muscardine soil fungus
        BEAUVERIA_BASSIANA,     // White muscardine
        MICROSPORIDIA           // Intracellular parasite
    }

    public static class InfectionState {
        private final PathogenType pathogen;
        private int incubationTicks;
        private boolean isBehaviorModulating = false; // Zombie phase

        public InfectionState(PathogenType pathogen) {
            this.pathogen = pathogen;
            this.incubationTicks = 0;
        }

        public void tick() {
            incubationTicks++;
            if (pathogen == PathogenType.CORDYCEPS_UNILATERALIS && incubationTicks > 1200) {
                isBehaviorModulating = true; // Forces climbing to high foliage elevation
            }
        }

        public PathogenType getPathogen() { return pathogen; }
        public boolean isBehaviorModulating() { return isBehaviorModulating; }
        public boolean isFatal() { return incubationTicks > 2400; }
    }

    /**
     * Evaluates fungal spore transmission risk during grooming or physical contact.
     */
    public static boolean checkSporeTransmission(float sporeDensity, float relativeHumidity) {
        if (relativeHumidity < 65.0f) return false; // Fungi require high humidity to germinate
        float transmissionProb = sporeDensity * (relativeHumidity / 100.0f) * 0.05f;
        return Math.random() < transmissionProb;
    }

    /**
     * Social Immunity: Active worker-to-worker Allogrooming removes 85% of un-germinated surface spores.
     */
    public static float performSocialAllogrooming(float initialSporeLoad) {
        return Math.max(0.0f, initialSporeLoad * 0.15f); // 85% spore load cleared
    }

    /**
     * Social Immunity: Formic acid (HCOOH) spraying on nest chambers inhibits pathogen proliferation.
     */
    public static float applyFormicAcidDisinfection(float chamberPathogenLoad, boolean hasFormicAcidAccess) {
        if (!hasFormicAcidAccess) return chamberPathogenLoad;
        return Math.max(0.0f, chamberPathogenLoad * 0.40f); // 60% pathogen reduction
    }
}
