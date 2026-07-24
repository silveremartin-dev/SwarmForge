/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

/**
 * Types of predators that can threaten ant colonies.
 * Each type has unique hunting behaviors and attributes.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public enum PredatorType {

    /**
     * Ambush predator that builds webs to trap ants.
     * Low mobility, high trap effectiveness.
     */
    SPIDER(
            "Spider",
            0.5f, // speed
            15f, // damage
            8f, // vision range
            0.8f, // trap chance
            HuntingStyle.AMBUSH),

    /**
     * Pit trap predator that digs cone-shaped traps in sand.
     * Stationary, extremely effective in sandy terrain.
     */
    ANTLION(
            "Antlion",
            0.1f, // speed (mostly stationary)
            20f, // damage
            5f, // vision range
            0.9f, // trap chance
            HuntingStyle.TRAP),

    /**
     * Fast ground hunter that chases prey.
     * High mobility, moderate damage.
     */
    BEETLE(
            "Ground Beetle",
            2.0f, // speed
            10f, // damage
            12f, // vision range
            0f, // no trap
            HuntingStyle.CHASE),

    /**
     * Aerial predator that swoops down on surface ants.
     * Very fast, attacks from above.
     */
    BIRD(
            "Bird",
            5.0f, // speed
            25f, // damage (instant kill)
            30f, // vision range
            0f, // no trap
            HuntingStyle.SWOOP),

    /**
     * Large predator that can consume multiple ants.
     * Slow but devastating when it attacks.
     */
    LIZARD(
            "Lizard",
            1.5f, // speed
            40f, // damage
            20f, // vision range
            0f, // no trap
            HuntingStyle.CHASE),

    /**
     * Aerial insect.
     */
    WASP(
            "Wasp",
            4.0f,
            12f,
            15f,
            0f,
            HuntingStyle.CHASE);

    /**
     * Hunting style determines AI behavior.
     */
    public enum HuntingStyle {
        AMBUSH, // Wait and attack when prey is close
        TRAP, // Create traps and wait
        CHASE, // Actively pursue prey
        SWOOP // Attack from above
    }

    private final String displayName;
    private final float baseSpeed;
    private final float baseDamage;
    private final float visionRange;
    private final float trapChance;
    private final HuntingStyle huntingStyle;

    PredatorType(String displayName, float baseSpeed, float baseDamage,
            float visionRange, float trapChance, HuntingStyle huntingStyle) {
        this.displayName = displayName;
        this.baseSpeed = baseSpeed;
        this.baseDamage = baseDamage;
        this.visionRange = visionRange;
        this.trapChance = trapChance;
        this.huntingStyle = huntingStyle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getVisionRange() {
        return visionRange;
    }

    public float getTrapChance() {
        return trapChance;
    }

    public HuntingStyle getHuntingStyle() {
        return huntingStyle;
    }

    /**
     * Check if this predator can attack underground ants.
     */
    public boolean canAttackUnderground() {
        return this == ANTLION; // Only antlions can attack underground
    }

    /**
     * Check if this predator requires specific terrain.
     */
    public boolean requiresSandTerrain() {
        return this == ANTLION;
    }
}
