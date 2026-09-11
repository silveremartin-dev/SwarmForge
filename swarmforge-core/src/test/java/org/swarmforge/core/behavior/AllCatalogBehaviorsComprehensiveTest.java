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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive verification for the 16 Mass-Implemented Eusocial Behaviors across Lots A, B, C, D.
 */
public class AllCatalogBehaviorsComprehensiveTest {

    // =========================================================================
    // LOT A: Advanced Biological & Mechanical Defense
    // =========================================================================

    @Test
    @DisplayName("A1. Autothysis: Suicidal glandular explosion & toxic glue splash")
    void testAutothysisSuicideExplosion() {
        UUID colony1 = UUID.randomUUID();
        UUID colony2 = UUID.randomUUID();

        Individual explodingAnt = new Individual(colony1, Individual.Caste.WORKER, 10f, 10f, 0f);
        Individual enemy1 = new Individual(colony2, Individual.Caste.SOLDIER, 10.2f, 10f, 0f);
        Individual enemy2 = new Individual(colony2, Individual.Caste.WORKER, 10f, 10.3f, 0f);

        float initialEnemy1Health = enemy1.getHealth();
        boolean exploded = explodingAnt.performAutothysis(List.of(enemy1, enemy2));

        assertTrue(exploded, "Autothysis should trigger");
        assertFalse(explodingAnt.isAlive(), "Exploding ant sacrifices itself");
        assertTrue(explodingAnt.hasAutothysed(), "State flag set to autothysed");

        assertTrue(enemy1.getHealth() < initialEnemy1Health, "Enemy takes splash damage from toxic glue");
        assertTrue(enemy1.isChemotacticallyBlind(), "Enemy receives chemotactic blindness from adhesive vapor");
    }

    @Test
    @DisplayName("A2. Phragmosis: Saucer head door blocking & 80% damage reduction")
    void testPhragmosisDoorSealing() {
        UUID colony = UUID.randomUUID();
        Individual soldier = new Individual(colony, Individual.Caste.SOLDIER, 5f, 5f, 0f);

        soldier.setPhragmoticShieldActive(true);
        assertTrue(soldier.isPhragmoticShieldActive(), "Phragmotic shield must be active");

        float initialHealth = soldier.getHealth();
        soldier.takeDamage(50.0f, "Enemy Mandible Strike");

        // With 80% reduction, damage is 10 instead of 50
        assertEquals(initialHealth - 10.0f, soldier.getHealth(), 0.01f, "Phragmotic shield reduces incoming physical damage by 80%");
    }

    @Test
    @DisplayName("A3. Thermal Balling Defense: 47°C oven against predatory invaders")
    void testThermalBallingDefense() {
        UUID colony = UUID.randomUUID();
        UUID enemyColony = UUID.randomUUID();

        Individual defender = new Individual(colony, Individual.Caste.WORKER, 15f, 15f, 0f);
        Individual hornet = new Individual(enemyColony, Individual.Caste.SOLDIER, 15f, 15f, 0f);

        float hornetHealthBefore = hornet.getHealth();
        boolean balled = defender.performThermalBalling(hornet, 1.5f);

        assertTrue(balled, "Thermal balling should succeed");
        assertTrue(defender.isInThermalBallCluster(), "Defender enters cluster state");
        assertTrue(defender.getThoraxTemperatureC() > 25.0f, "Thorax temperature rises towards 47°C");
        assertTrue(hornet.getHealth() < hornetHealthBefore, "Hornet takes thermal asphyxiation damage");
    }

    @Test
    @DisplayName("A4. Nasute Terpene Jet: 0.8m chemical squirt & immobilization")
    void testNasuteResinSquirt() {
        UUID colony = UUID.randomUUID();
        UUID enemyColony = UUID.randomUUID();

        Individual nasute = new Individual(colony, Individual.Caste.SOLDIER, 20f, 20f, 0f);
        Individual enemy = new Individual(enemyColony, Individual.Caste.WORKER, 20.5f, 20f, 0f); // 0.5m distance

        float enemyHealthBefore = enemy.getHealth();
        boolean squirted = nasute.squirtNasuteResin(enemy, 0.5f);

        assertTrue(squirted, "Nasute squirt succeeds within 0.8m range");
        assertTrue(enemy.getHealth() < enemyHealthBefore, "Enemy takes chemical monoterpene damage");
        assertTrue(enemy.isChemotacticallyBlind(), "Enemy gets chemical blindness");
        assertTrue(nasute.getNasuteResinReservoir() < 100.0f, "Resin reservoir is consumed");
    }

    // =========================================================================
    // LOT B: Living Civil Engineering & Subterranean Architecture
    // =========================================================================

    @Test
    @DisplayName("B1. Living Bridges & Hydrophobic Floating Rafts")
    void testLivingBridgesAndRafts() {
        UUID colony = UUID.randomUUID();
        Individual ant = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 0f);

        ant.setFormingLivingBridge(true);
        assertTrue(ant.isFormingLivingBridge(), "Ant links tarsi into living bridge structure");

        ant.setFormingLivingBridge(false);
        ant.setFormingLivingRaft(true);
        assertTrue(ant.isFormingLivingRaft(), "Ant forms floating raft against floodwaters");
    }

    @Test
    @DisplayName("B2. Leaf-Sewing with Larval Silk Shuttles")
    void testLeafSewingLarvalSilk() {
        UUID colony = UUID.randomUUID();
        Individual weaver = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 5f);
        Individual larva = new Individual(colony, Individual.Caste.WORKER, 10f, 10f, 5f);
        larva.setLifeStage(Individual.LifeStage.LARVA);

        boolean grabbed = weaver.sewLeavesWithLarvalSilk(larva);
        assertTrue(grabbed, "Weaver ant holds silk-producing larva to weave leaves");
        assertEquals(Individual.CarriedItem.BROOD, weaver.getCarriedItem(), "Larva is held as living shuttle");
    }

    @Test
    @DisplayName("B3. Stercoral Cement Substrate Reinforcement")
    void testStercoralCement() {
        UUID colony = UUID.randomUUID();
        Individual builder = new Individual(colony, Individual.Caste.WORKER, 8f, 8f, 1f);

        boolean applied = builder.applyStercoralCement();
        assertTrue(applied, "Stercoral cement applied to tunnel walls");
        assertTrue(builder.getStercoralMortarReservoir() < 100.0f, "Mortar reservoir consumed");
    }

    @Test
    @DisplayName("B4. Domatia Mutualism & Acacia Inhabitation")
    void testDomatiaMutualism() {
        UUID colony = UUID.randomUUID();
        Individual ant = new Individual(colony, Individual.Caste.WORKER, 30f, 30f, 2f);

        ant.setInhabitingDomatia(true);
        assertTrue(ant.isInhabitingDomatia(), "Ant nests inside swollen thorn domatia");
    }

    // =========================================================================
    // LOT C: Agronomy, Storage & Plant/Insect Symbiosis
    // =========================================================================

    @Test
    @DisplayName("C1. Granary Seed Radicle De-Germination")
    void testGranarySeedDeGermination() {
        UUID colony = UUID.randomUUID();
        Individual harvester = new Individual(colony, Individual.Caste.WORKER, 12f, 12f, 0f);

        harvester.setCarriedItem(Individual.CarriedItem.FOOD);
        boolean degermed = harvester.deGermStoredSeed();

        assertTrue(degermed, "Harvester bites off seed radicle to stop subterranean germination");
        assertEquals(1, harvester.getDeGerminatedSeedsCount(), "De-germinated seeds counter incremented");
    }

    @Test
    @DisplayName("C2. Honeypot Replete Gaster Volume Storage")
    void testHoneypotRepleteStorage() {
        UUID colony = UUID.randomUUID();
        Individual replete = new Individual(colony, Individual.Caste.WORKER, 14f, 14f, 1f);

        replete.setRepleteStorageCaste(true);
        assertTrue(replete.isRepleteStorageCaste(), "Ant is specialized repletes caste");

        replete.setRepleteStorageVolume(75.0f);
        assertEquals(75.0f, replete.getRepleteStorageVolume(), 0.01f, "Replete holds 75% gaster nectar capacity");
    }

    @Test
    @DisplayName("C3. Aphid Herding, Milking & Honeydew Yield")
    void testAphidMilking() {
        UUID colony = UUID.randomUUID();
        Individual herder = new Individual(colony, Individual.Caste.WORKER, 16f, 16f, 0f);
        herder.setEnergy(50.0f);

        float honeydew = herder.milkAphid(2.5f);
        assertEquals(2.5f, honeydew, 0.01f, "Harvested honeydew yield");
        assertTrue(herder.getEnergy() > 50.0f, "Herder ant absorbs nutritional sugars from honeydew");
        assertEquals(1, herder.getManagedAphidsCount(), "Managed aphids counter updated");
    }

    // =========================================================================
    // LOT D: Bio-Acoustic Communication, Dance & Royal Signaling
    // =========================================================================

    @Test
    @DisplayName("D1. Waggle Dance: Vector heading and distance transmission")
    void testWaggleDanceVectorCommunication() {
        UUID colony = UUID.randomUUID();
        Individual dancer = new Individual(colony, Individual.Caste.FORAGER, 25f, 25f, 0f);

        dancer.performWaggleDance(1.57f, 45.0f);
        assertTrue(dancer.isWaggleDancing(), "Dancer performs waggle dance");
        assertEquals(1.57f, dancer.getWaggleTargetHeading(), 0.01f, "Waggle heading relative to sun vector");
        assertEquals(45.0f, dancer.getWaggleDistanceMeters(), 0.01f, "Distance encoded in waggle run duration");

        dancer.stopWaggleDance();
        assertFalse(dancer.isWaggleDancing(), "Dance stopped after recruitment");
    }

    @Test
    @DisplayName("D2. Tremble Dance: Unloading bottleneck signal")
    void testTrembleDance() {
        UUID colony = UUID.randomUUID();
        Individual forager = new Individual(colony, Individual.Caste.FORAGER, 25f, 25f, 0f);

        forager.setTrembleDancing(true);
        assertTrue(forager.isTrembleDancing(), "Tremble dance signals receiver shortage");
    }

    @Test
    @DisplayName("D3. Virgin Queen Piping: 450 Hz challenge acoustic signal")
    void testQueenPipingSignal() {
        UUID colony = UUID.randomUUID();
        Individual queen = new Individual(colony, Individual.Caste.QUEEN, 5f, 5f, 0f);

        queen.triggerQueenPiping();
        assertTrue(queen.isQueenPipingActive(), "Virgin queen emits piping acoustic challenge");
        assertEquals(450.0f, queen.getQueenPipingFrequencyHz(), 1.0f, "Piping frequency matches 450 Hz");

        queen.stopQueenPiping();
        assertFalse(queen.isQueenPipingActive(), "Queen piping deactivated");
    }
}
