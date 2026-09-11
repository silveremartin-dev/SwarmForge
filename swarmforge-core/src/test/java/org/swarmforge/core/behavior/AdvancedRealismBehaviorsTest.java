/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.FungusGarden;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.AttaCephalotes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates advanced biological realism behaviors:
 * 1. Formic Acid Artillery Spray & Chemotactic Blindness
 * 2. Endogenous Circadian Clock & Polyphasic Sleep Rhythm
 * 3. Tripartite Symbiosis & Actinobacteria Bio-Weeding
 */
public class AdvancedRealismBehaviorsTest {

    @Test
    @DisplayName("1. Formic Acid Spray: 1.5m range, chemical damage and 120-tick chemotactic blindness")
    void testFormicAcidArtillerySpray() {
        UUID colony1 = UUID.randomUUID();
        UUID colony2 = UUID.randomUUID();

        Individual defender = new Individual(colony1, Individual.Caste.SOLDIER, 10f, 10f, 0f);
        defender.setSpecies(new FormicaRufa());

        Individual enemy = new Individual(colony2, Individual.Caste.WORKER, 11.2f, 10f, 0f); // 1.2m distance (<= 1.5m range)

        float initialEnemyHealth = enemy.getHealth();
        assertFalse(enemy.isChemotacticallyBlind(), "Enemy should not be blind initially");

        // Execute spray
        boolean sprayed = defender.sprayFormicAcid(enemy, 1.2f);
        assertTrue(sprayed, "Formic acid spray should succeed within 1.5m range");

        // Verify effects
        assertTrue(enemy.getHealth() < initialEnemyHealth, "Enemy should take chemical burn damage");
        assertTrue(enemy.isChemotacticallyBlind(), "Enemy should suffer chemotactic blindness");
        assertEquals(120, enemy.getChemotacticBlindnessTicks(), "Blindness duration should be 120 ticks");

        // Verify gland depletion
        assertTrue(defender.getFormicAcidGland() < 100.0f, "Formic acid gland should be depleted");

        // Verify distance cut-off (> 1.5m)
        Individual farEnemy = new Individual(colony2, Individual.Caste.WORKER, 12f, 10f, 0f); // 2.0m distance
        boolean farSprayed = defender.sprayFormicAcid(farEnemy, 2.0f);
        assertFalse(farSprayed, "Spray should fail beyond 1.5m biological range");
    }

    @Test
    @DisplayName("2. Endogenous Circadian Clock: Phase advancement and polyphasic sleep restoration")
    void testEndogenousCircadianClockAndSleep() {
        UUID colonyId = UUID.randomUUID();
        Individual ant = new Individual(colonyId, Individual.Caste.WORKER, 5f, 5f, 0f);

        ant.setCircadianPhase(0.0f);
        ant.setEnergy(50.0f);
        ant.setFatigue(60.0f);
        ant.setPolyphasicSleeping(true);

        // Advance 10 seconds of simulation
        ant.tick(10.0f);

        // Circadian phase should advance smoothly
        assertTrue(ant.getCircadianPhase() > 0.0f, "Circadian phase should advance with physical time");

        // Polyphasic sleep should regenerate energy and reduce fatigue
        assertTrue(ant.getEnergy() > 50.0f, "Polyphasic sleep should restore energy");
        assertTrue(ant.getFatigue() < 60.0f, "Polyphasic sleep should reduce fatigue");
    }

    @Test
    @DisplayName("3. Tripartite Symbiosis: Actinobacteria bio-weeding against Escovopsis contamination")
    void testTripartiteSymbiosisBioWeeding() {
        AttaCephalotes species = new AttaCephalotes();
        Colony colony = new Colony(species, 20f, 20f, 0f);
        FungusGarden garden = new FungusGarden(colony);

        // Contaminate garden with Escovopsis
        garden.setContaminationLevel(0.8f);
        assertEquals(0.8f, garden.getContaminationLevel(), 0.001f);

        Individual minimWorker = new Individual(colony.getId(), Individual.Caste.WORKER, 20f, 20f, 0f);
        minimWorker.setSpecies(species);

        // Apply antibiotic bio-weeding
        boolean applied = minimWorker.applyActinobacteriaBioWeeding(garden);
        assertTrue(applied, "Actinobacteria bio-weeding should succeed");

        // Contamination level should be significantly reduced
        assertTrue(garden.getContaminationLevel() < 0.8f, "Escovopsis contamination should decrease after bio-weeding");
        assertTrue(minimWorker.getActinobacteriaResin() < 100.0f, "Ant actinobacteria resin reservoir should decrease");
    }
}
