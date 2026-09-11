/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the 12 Eusocial Behavioral Systems across Lots E, F, and G:
 * - Lot E: Genetics, Reproduction & Social Regulation (9-ODA Ovary Suppression, Dracula Hemolymph Feeding, Gamergates, Oophagy)
 * - Lot F: Extreme Eco-Physiology & Climatic Adaptation (Glycerol Cryoprotection, Desert Stilt Walking, Hydrostatic Flood Evacuation)
 * - Lot G: Advanced Sanitation & Social Immunity (Voluntary Self-Isolation, Formic Acid Bath Grooming, Propolis Varnish, Aphid Wing Clipping)
 */
public class LotsEFGBehaviorsTest {

    // =========================================================================
    // LOT E: Genetics, Reproduction & Social Regulation
    // =========================================================================

    @Test
    @DisplayName("E1. Royal Pheromone (9-ODA) Ovary Suppression & Orphan Trophic Egg Laying")
    void testRoyalPheromoneOvarySuppression() {
        UUID colony = UUID.randomUUID();
        Individual worker = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 0f);

        // Under queen presence (titer = 1.0): ovaries suppressed
        worker.setRoyalPheromoneInhibitionTiter(1.0f);
        assertFalse(worker.isOvariesActivated(), "Ovaries must be suppressed in queen-right colony");
        assertFalse(worker.layTrophicEgg(), "Suppressed worker cannot lay eggs");

        // Queen lost (orphan colony, titer falls below 0.15)
        worker.setRoyalPheromoneInhibitionTiter(0.05f);
        assertTrue(worker.isOvariesActivated(), "Ovaries activate upon royal pheromone dissipation");
        boolean laid = worker.layTrophicEgg();
        assertTrue(laid, "Orphaned worker lays trophic egg for colony sustenance");
        assertEquals(1, worker.getTrophicEggsLaid(), "Trophic egg count incremented");
    }

    @Test
    @DisplayName("E2. Dracula Ant Larval Hemolymph Tapping (Non-destructive Vampirism)")
    void testDraculaAntHemolymphFeeding() {
        UUID colony = UUID.randomUUID();
        Individual queen = new Individual(colony, Individual.Caste.QUEEN, 5f, 5f, 0f);
        queen.setEnergy(40f);

        Individual larva = new Individual(colony, Individual.Caste.WORKER, 5f, 5f, 0f);
        larva.setLifeStage(Individual.LifeStage.LARVA);

        float larvaHealthBefore = larva.getHealth();
        boolean fed = queen.feedOnLarvalHemolymph(larva);

        assertTrue(fed, "Queen successfully taps larval hemolymph");
        assertTrue(larva.isAlive(), "Larva survives non-destructive hemolymph puncture");
        assertTrue(larva.getHealth() < larvaHealthBefore, "Larva takes slight puncture damage");
        assertTrue(queen.getEnergy() > 40f, "Queen regains energy from nutrient-rich hemolymph");
    }

    @Test
    @DisplayName("E3. Gamergate Dominance Tournaments & Queen Differentiation")
    void testGamergateDominanceTournament() {
        UUID colony = UUID.randomUUID();
        Individual workerA = new Individual(colony, Individual.Caste.WORKER, 12f, 12f, 0f);
        Individual workerB = new Individual(colony, Individual.Caste.WORKER, 12f, 12f, 0f);

        assertFalse(workerA.isGamergate(), "Worker A is not a gamergate initially");

        // Simulate 5 winning tournament rounds
        for (int i = 0; i < 5; i++) {
            workerA.engageDominanceTournament(workerB);
        }

        assertTrue(workerA.isGamergate(), "Winner transitions to reproductive gamergate status");
        assertEquals(Individual.Caste.QUEEN, workerA.getCaste(), "Gamergate adopts functional reproductive caste");
    }

    @Test
    @DisplayName("E4. Selective Oophagy: Nutrition recycling of defective eggs")
    void testSelectiveOophagy() {
        UUID colony = UUID.randomUUID();
        Individual nurse = new Individual(colony, Individual.Caste.NURSE, 8f, 8f, 0f);
        nurse.setEnergy(50f);

        boolean consumed = nurse.consumeDefectiveEgg();
        assertTrue(consumed, "Nurse consumes defective egg");
        assertTrue(nurse.getEnergy() > 50f, "Energy restored via egg oophagy");
    }

    // =========================================================================
    // LOT F: Extreme Eco-Physiology & Climatic Adaptation
    // =========================================================================

    @Test
    @DisplayName("F1. Glycerol Cryoprotection: Subzero survival down to -15°C")
    void testGlycerolCryoprotection() {
        UUID colony = UUID.randomUUID();
        Individual ant = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 0f);

        // Without cryoprotectant: cannot survive -10°C
        assertFalse(ant.canSurviveSubzero(-10.0f), "Ant without glycerol cannot survive -10°C");

        // Synthesize 40 mg/mL glycerol polyols during autumn diapause
        ant.synthesizeGlycerol(40.0f);
        assertEquals(40.0f, ant.getGlycerolConcentrationMgMl(), 0.01f);
        assertTrue(ant.canSurviveSubzero(-10.0f), "Glycerol-loaded ant survives -10°C supercooling");
        assertTrue(ant.canSurviveSubzero(-15.0f), "Glycerol-loaded ant survives -15°C critical threshold");
        assertFalse(ant.canSurviveSubzero(-20.0f), "Subzero extreme below -15°C remains lethal");
    }

    @Test
    @DisplayName("F2. Desert Ant Thermal Stilt Walking (+50% sprint velocity)")
    void testDesertAntStiltWalking() {
        UUID colony = UUID.randomUUID();
        Individual desertAnt = new Individual(colony, Individual.Caste.FORAGER, 15f, 15f, 0f);

        float baseSpeed = desertAnt.getEffectiveLocomotionSpeed();
        desertAnt.setStiltWalking(true);
        float stiltSpeed = desertAnt.getEffectiveLocomotionSpeed();

        assertTrue(stiltSpeed > baseSpeed, "Stilt-walking posture increases locomotion speed");
        assertEquals(baseSpeed * 1.50f, stiltSpeed, 0.01f, "Stilt walking provides exactly +50% sprint multiplier");
    }

    @Test
    @DisplayName("F3. Hydrostatic Pressure Barometric Evacuation Trigger")
    void testHydrostaticFloodEvacuation() {
        UUID colony = UUID.randomUUID();
        Individual nurse = new Individual(colony, Individual.Caste.NURSE, 20f, 20f, -2f);

        assertFalse(nurse.isFloodEvacuating(), "Nurse not evacuating under normal conditions");

        // Sudden barometric drop (-20 hPa) & high soil moisture (90%)
        nurse.detectHydrostaticFloodPressure(20.0f, 90.0f);
        assertTrue(nurse.isFloodEvacuating(), "Nurse triggers emergency flood evacuation of subterranean brood");
    }

    // =========================================================================
    // LOT G: Advanced Sanitation, Social Immunity & Pharmacy
    // =========================================================================

    @Test
    @DisplayName("G1. Altruistic Necrotropic Self-Isolation: Terminally infected ant leaves nest")
    void testAltruisticSelfIsolation() {
        UUID colony = UUID.randomUUID();
        Individual infectedAnt = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 0f);

        assertFalse(infectedAnt.isVoluntarySelfIsolating(), "Healthy ant does not self-isolate");

        // Terminal fungal pathogen spore load (80%)
        infectedAnt.checkPathogenSelfIsolation(80.0f);
        assertTrue(infectedAnt.isVoluntarySelfIsolating(), "Terminally sporulating ant voluntarily leaves nest to protect colony");
    }

    @Test
    @DisplayName("G2. Formic Acid Cuticular Auto-Sanitation Bath Grooming")
    void testFormicAcidBathGrooming() {
        UUID colony = UUID.randomUUID();
        Individual ant = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 0f);

        float glandBefore = ant.getFormicAcidGland();
        boolean groomed = ant.performFormicAcidBathGrooming();

        assertTrue(groomed, "Formic acid bath grooming succeeds");
        assertTrue(ant.getFormicAcidGland() < glandBefore, "Formic acid reservoir consumed for cuticular disinfection");
    }

    @Test
    @DisplayName("G3. Propolis Varnish Hive Shielding & Aphid Wing Clipping")
    void testPropolisVarnishAndAphidWingClipping() {
        UUID colony = UUID.randomUUID();
        Individual builder = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 0f);

        builder.setPropolisVarnishCarried(10.0f);
        boolean varnished = builder.applyPropolisVarnish();
        assertTrue(varnished, "Builder applies propolis varnish coating to hive walls");
        assertEquals(5.0f, builder.getPropolisVarnishCarried(), 0.01f, "Propolis reserve reduced by 5.0");

        Individual herder = new Individual(colony, Individual.Caste.FORAGER, 10f, 10f, 0f);
        boolean clipped = herder.clipAphidWingBuds();
        assertTrue(clipped, "Herder clips aphid wings to keep herd captive");
        assertEquals(1, herder.getManagedAphidsCount(), "Managed aphids counter updated");
    }
}
