/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

/**
 * A Boss-level predator with special abilities and high stats.
 * Example: Anteater, Large Spider, etc.
 *
 * @author Gemini AI Assistant
 */
public class BossPredator extends Predator {

    public enum BossType {
        ANTEATER,
        GIANT_SPIDER,
        WASP_QUEEN
    }

    private final BossType bossType;
    private final float maxHealth;

    // Boss Abilities
    private int cooldown = 0;

    public BossPredator(float x, float y, BossType type) {
        super(mapType(type), x, y, 0); // Z=0 surface
        this.bossType = type;

        switch (type) {
            case ANTEATER -> this.maxHealth = 5000f;
            case GIANT_SPIDER -> this.maxHealth = 2000f;
            case WASP_QUEEN -> this.maxHealth = 1500f;
            default -> this.maxHealth = 1000f;
        }

        this.setHealth(this.maxHealth);
    }

    private static PredatorType mapType(BossType type) {
        switch (type) {
            case ANTEATER:
                return PredatorType.BEETLE; // Placeholder
            case GIANT_SPIDER:
                return PredatorType.SPIDER;
            case WASP_QUEEN:
                return PredatorType.WASP;
            default:
                return PredatorType.BEETLE;
        }
    }

    public BossType getBossType() {
        return bossType;
    }

    @Override
    public void tick() {
        super.tick();
        if (cooldown > 0)
            cooldown--;

        // Regenerate slightly
        if (getHealth() < maxHealth) {
            setHealth(Math.min(maxHealth, getHealth() + 0.5f));
        }
    }

    // Special attack logic would go here
    public boolean canUseSpecialAbility() {
        return cooldown == 0;
    }

    public void useSpecialAbility() {
        cooldown = 300; // 5 seconds cooldown
        // Logic handled by PredatorManager or Simulation
    }
}
