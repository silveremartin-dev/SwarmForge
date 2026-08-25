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
            HuntingStyle.CHASE),

    SYRPHID_LARVA(
            "Larve de Syrphe (Prédateur de Pucerons)",
            0.4f,
            8f,
            6f,
            0f,
            HuntingStyle.CHASE),

    LADYBUG_LARVA(
            "Larve de Coccinelle (Vorace)",
            0.6f,
            10f,
            7f,
            0f,
            HuntingStyle.CHASE),

    KLEPTOPARASITE_THRIPS(
            "Thrips Kleptoparasite (Envahisseur de Galle)",
            0.8f,
            6f,
            5f,
            0f,
            HuntingStyle.CHASE),

    CATERPILLAR(
            "Chenille (Proie pour Guêpes)",
            0.2f,
            2f,
            4f,
            0f,
            HuntingStyle.AMBUSH),

    MYRMECOPHILE_BEETLE(
            "Staphylin Myrmécophile (Commensal/Parasite du Nid)",
            0.5f,
            4f,
            5f,
            0f,
            HuntingStyle.AMBUSH),

    ASIAN_HORNET(
            "Frelon Asiatique (Prédateur spécialisé d'Abeilles)",
            4.2f,
            18f,
            20f,
            0f,
            HuntingStyle.SWOOP),

    BEE_WOLF(
            "Philanthe Apivore (Guêpe chasseuse d'Abeilles)",
            3.5f,
            14f,
            16f,
            0f,
            HuntingStyle.CHASE),

    VARROA_MITE(
            "Mite Varroa (Ectoparasite des Nymphes d'Abeilles)",
            0.2f,
            5f,
            3f,
            0.9f,
            HuntingStyle.AMBUSH),

    HONEY_BUZZARD(
            "Bondrée Apivore (Oiseau rapace de Nids de Guêpes/Abeilles)",
            5.5f,
            45f,
            35f,
            0f,
            HuntingStyle.SWOOP),

    MEGAPONERA_RAIDER(
            "Fourmi Raideuse Megaponera (Raid spécialisé sur Termites)",
            2.2f,
            16f,
            14f,
            0f,
            HuntingStyle.CHASE),

    AARDVARK_MOUND_BREAKER(
            "Oryctérope / Tamanoir (Destructeur de Termitières)",
            1.0f,
            50f,
            25f,
            0f,
            HuntingStyle.CHASE),

    WOODPECKER(
            "Pic Noir (Prédateur de Scolytes & Fourmis Bois)",
            4.8f,
            30f,
            28f,
            0f,
            HuntingStyle.SWOOP);

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
