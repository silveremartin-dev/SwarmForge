/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * Unit tests for Predator class.
 */
class PredatorTest {

    private Predator spider;
    private Predator beetle;
    private Individual ant;

    @BeforeEach
    void setUp() {
        spider = new Predator(PredatorType.SPIDER, 10f, 10f, 5f);
        beetle = new Predator(PredatorType.BEETLE, 20f, 20f, 5f);
        ant = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 12f, 12f, 5f);
    }

    @Test
    void testPredatorCreation() {
        assertNotNull(spider.getId());
        assertEquals(PredatorType.SPIDER, spider.getType());
        assertEquals(10f, spider.getX());
        assertEquals(10f, spider.getY());
        assertTrue(spider.isAlive());
        assertEquals(Predator.HuntingState.IDLE, spider.getState());
    }

    @Test
    void testPredatorTypeAttributes() {
        assertEquals("Spider", PredatorType.SPIDER.getDisplayName());
        assertEquals(0.5f, PredatorType.SPIDER.getBaseSpeed());
        assertEquals(15f, PredatorType.SPIDER.getBaseDamage());
        assertEquals(PredatorType.HuntingStyle.AMBUSH, PredatorType.SPIDER.getHuntingStyle());

        assertEquals("Ground Beetle", PredatorType.BEETLE.getDisplayName());
        assertEquals(PredatorType.HuntingStyle.CHASE, PredatorType.BEETLE.getHuntingStyle());
    }

    @Test
    void testPredatorMovement() {
        float initialX = beetle.getX();
        float initialY = beetle.getY();

        beetle.moveToward(25f, 25f, 1f);

        assertTrue(beetle.getX() > initialX);
        assertTrue(beetle.getY() > initialY);
    }

    @Test
    void testPredatorTick() {
        float initialHunger = spider.getHunger();

        spider.tick();

        assertEquals(1, spider.getAge());
        assertTrue(spider.getHunger() > initialHunger);
    }

    @Test
    void testTrapBuilding() {
        assertFalse(spider.isTrapBuilt());

        spider.buildTrap();

        assertTrue(spider.isTrapBuilt());
    }

    @Test
    void testPredatorDamage() {
        float initialHealth = spider.getHealth();

        spider.takeDamage(20f);

        assertEquals(initialHealth - 20f, spider.getHealth());
        assertTrue(spider.isAlive());
    }

    @Test
    void testPredatorDeath() {
        spider.takeDamage(1000f);

        assertFalse(spider.isAlive());
        assertEquals(Predator.HuntingState.DEAD, spider.getState());
    }

    @Test
    void testPredatorFleesWhenLowHealth() {
        spider.takeDamage(spider.getMaxHealth() * 0.8f);

        assertEquals(Predator.HuntingState.FLEEING, spider.getState());
        assertTrue(spider.isAlive());
    }

    @Test
    void testDistanceCalculation() {
        float dist = spider.distanceTo(ant);

        // Distance from (10,10,5) to (12,12,5) = sqrt(4+4) = ~2.83
        assertTrue(dist > 2.8f && dist < 2.9f);
    }

    @Test
    void testVisionRange() {
        // Ant at (12,12) is ~2.8 units from spider at (10,10)
        // Spider vision range is 8
        assertTrue(spider.canSee(ant));

        // Move ant far away
        ant.setPosition(100f, 100f, 5f);
        assertFalse(spider.canSee(ant));
    }

    @Test
    void testHuntingStyles() {
        assertEquals(PredatorType.HuntingStyle.AMBUSH, PredatorType.SPIDER.getHuntingStyle());
        assertEquals(PredatorType.HuntingStyle.TRAP, PredatorType.ANTLION.getHuntingStyle());
        assertEquals(PredatorType.HuntingStyle.CHASE, PredatorType.BEETLE.getHuntingStyle());
        assertEquals(PredatorType.HuntingStyle.SWOOP, PredatorType.BIRD.getHuntingStyle());
        assertEquals(PredatorType.HuntingStyle.CHASE, PredatorType.LIZARD.getHuntingStyle());
    }

    @Test
    void testAttackCooldown() {
        spider.setCurrentTarget(ant);

        // First attack should work
        spider.attack(ant);

        // Second immediate attack should fail due to cooldown
        float healthAfterFirst = ant.getHealth();
        spider.attack(ant);
        assertEquals(healthAfterFirst, ant.getHealth());
    }
}
