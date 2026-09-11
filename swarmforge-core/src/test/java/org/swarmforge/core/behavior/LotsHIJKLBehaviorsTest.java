package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.CarriedItem;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.domain.Individual.Job;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive verification test suite for Lots H, I, J, K, L behavioral systems (20 behaviors).
 */
public class LotsHIJKLBehaviorsTest {

    // ==========================================
    // LOT H: Maçonnerie Végétale & Climatisation
    // ==========================================
    @Test
    @DisplayName("Lot H - Weaver Ant Silk Seam Assembly")
    void testWeaverSilkSeam() {
        Individual worker = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertFalse(worker.weaveLeafSilkSeam(15.0f), "Cannot weave without holding a silk larva");

        worker.setHoldingSilkLarva(true);
        assertTrue(worker.weaveLeafSilkSeam(15.0f), "Should successfully weave with silk larva");
        assertEquals(85.0f, worker.getSilkReservoir(), 0.001f);

        worker.refillSilkReservoir(10.0f);
        assertEquals(95.0f, worker.getSilkReservoir(), 0.001f);
    }

    @Test
    @DisplayName("Lot H - Subterranean Rain Siphon & Dual Conduits Climatisation")
    void testRainSiphonAndDualConduits() {
        Individual worker = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertFalse(worker.isRainSiphonConstructed());
        worker.constructRainSiphon();
        assertTrue(worker.isRainSiphonConstructed());
        worker.drainFloodWater(45.5f);
        assertEquals(45.5f, worker.getDrainedFloodWaterLiters(), 0.001f);

        assertFalse(worker.isDualConduitsActive());
        assertEquals(40.0f, worker.getSubterraneanMicroclimateTemp(40.0f), 0.001f);
        worker.setDualConduitsActive(true);
        // T = 24.0 + (40 - 24) * 0.25 = 28.0°C
        assertEquals(28.0f, worker.getSubterraneanMicroclimateTemp(40.0f), 0.001f);
    }

    @Test
    @DisplayName("Lot H - Antiseptic Gravel Gallery Sealing")
    void testAntisepticGravelSealing() {
        Individual worker = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertFalse(worker.sealContaminatedGallery(), "Cannot seal without enough gravel");
        worker.pickUpAntisepticGravel(5);
        assertEquals(5, worker.getAntisepticGravelCarried());
        assertTrue(worker.sealContaminatedGallery());
        assertEquals(2, worker.getAntisepticGravelCarried());
        assertEquals(1, worker.getSealedContaminatedGalleries());
    }

    // ==========================================
    // LOT I: Bio-Acoustique & Télégraphie Substratique
    // ==========================================
    @Test
    @DisplayName("Lot I - Wood Substrate Drumming Alarm")
    void testWoodSubstrateDrummingAlarm() {
        Individual worker = new Individual(UUID.randomUUID(), Caste.SOLDIER);
        assertFalse(worker.isDrummingSubstrateAlarm());
        worker.triggerDrummingSubstrateAlarm();
        assertTrue(worker.isDrummingSubstrateAlarm());
        assertEquals(1000.0f, worker.getDrumFrequencyHz(), 0.001f);
        assertEquals(8.5f, worker.getSubstrateVibrationPropagationDistanceMeters(), 0.001f);
        worker.stopDrummingSubstrateAlarm();
        assertFalse(worker.isDrummingSubstrateAlarm());
    }

    @Test
    @DisplayName("Lot I - Guard Shift Vibrational Whisper & Queen Piping & Egg Laying Sync")
    void testGuardShiftAndQueenPipingAndEggSync() {
        Individual guard = new Individual(UUID.randomUUID(), Caste.WORKER);
        guard.setJob(Job.GUARD);
        Individual replacement = new Individual(UUID.randomUUID(), Caste.WORKER);
        replacement.setJob(Job.IDLE);

        assertTrue(guard.performGuardShiftWhisper(replacement));
        assertTrue(guard.isGuardShiftWhispering());
        assertEquals(Job.GUARD, replacement.getJob());

        Individual queen = new Individual(UUID.randomUUID(), Caste.QUEEN);
        assertFalse(queen.isQueenPiping());
        queen.triggerQueenPiping();
        assertTrue(queen.isQueenPiping());
        assertEquals(450.0f, queen.getPipingFrequencyHz(), 0.001f);

        assertFalse(queen.isEggLayingSyncStridulation());
        assertEquals(1.0f, queen.calculateSynchronizedFertilityMultiplier(), 0.001f);
        queen.triggerEggLayingSync();
        assertTrue(queen.isEggLayingSyncStridulation());
        assertEquals(1.45f, queen.calculateSynchronizedFertilityMultiplier(), 0.001f);
    }

    // ==========================================
    // LOT J: Écologie Chimique & Allélochimie
    // ==========================================
    @Test
    @DisplayName("Lot J - Territorial Repellent & Host Bark Camouflage")
    void testTerritorialRepellentAndBarkCamouflage() {
        UUID colonyA = UUID.randomUUID();
        UUID colonyB = UUID.randomUUID();
        Individual antA = new Individual(colonyA, Caste.WORKER);
        Individual antB = new Individual(colonyB, Caste.WORKER);

        assertTrue(antA.depositTerritorialRepellent(30.0f));
        assertEquals(70.0f, antA.getTerritorialRepellentCarried(), 0.001f);
        assertTrue(antB.shouldRetreatFromTerritory(colonyA));
        assertFalse(antA.shouldRetreatFromTerritory(colonyA));

        assertEquals(0.0f, antA.getPlantCuticularCamouflagePercent(), 0.001f);
        antA.rubAgainstHostBark(20.0f); // 20 * 2.5 = 50%
        assertEquals(50.0f, antA.getPlantCuticularCamouflagePercent(), 0.001f);
    }

    @Test
    @DisplayName("Lot J - Formic Acid Fanout Escape & Q10 Thermal Decay")
    void testFormicAcidFanoutAndQ10ThermalDecay() {
        Individual ant = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertFalse(ant.isFanoutEscapeActive());
        ant.triggerFormicAcidFanoutEscape(0.0f, 0.2f); // stimulus from heading 0, flee opposite PI + 0.2
        assertTrue(ant.isFanoutEscapeActive());
        assertEquals(Individual.AiState.FLEEING, ant.getState());

        // At 20°C: factor = 0 -> half-life = 3600s
        assertEquals(3600.0f, ant.calculatePheromoneHalfLifeSeconds(20.0f, 3600.0f), 0.001f);
        // At 30°C: factor = 1 -> half-life = 1800s (doubled decay rate)
        assertEquals(1800.0f, ant.calculatePheromoneHalfLifeSeconds(30.0f, 3600.0f), 0.001f);
    }

    // ==========================================
    // LOT K: Pharmacie & Gestion Spécialisée des Déchets
    // ==========================================
    @Test
    @DisplayName("Lot K - Sulfur Dusting, Larval Wood Dust, Quarantine, Refuse Sorting")
    void testPharmacyAndRefuseSorting() {
        Individual worker = new Individual(UUID.randomUUID(), Caste.WORKER);
        worker.collectSulfurDust(10.0f);
        assertTrue(worker.dustParasiticMites());
        assertEquals(8.0f, worker.getSulfurDustCarried(), 0.001f);

        Individual larva = new Individual(UUID.randomUUID(), Caste.WORKER);
        worker.collectWoodDust(5.0f);
        assertTrue(worker.applyLarvalWoodDustDrying(larva));
        assertEquals(4.0f, worker.getWoodDustCarried(), 0.001f);

        assertFalse(worker.isParticipatingInParasiteQuarantine());
        worker.joinParasiteQuarantineEncirclement();
        assertTrue(worker.isParticipatingInParasiteQuarantine());
        worker.leaveParasiteQuarantineEncirclement();
        assertFalse(worker.isParticipatingInParasiteQuarantine());

        worker.setRefuseSortingDuty(true);
        worker.setCarriedItem(CarriedItem.FOOD);
        assertTrue(worker.depositSortedRefuseOutside());
        assertEquals(CarriedItem.NONE, worker.getCarriedItem());
        assertEquals(1, worker.getSortedRefuseItems());
    }

    // ==========================================
    // LOT L: Métabolisme de Crise & Régulation Sociale
    // ==========================================
    @Test
    @DisplayName("Lot L - Honeypot Replete Storage Dynamics")
    void testHoneypotStorage() {
        Individual replete = new Individual(UUID.randomUUID(), Caste.WORKER);
        assertFalse(replete.isRepletesHoneypot());
        assertEquals(0.0f, replete.storeHoneypotNectar(0.20f), 0.001f);

        replete.setRepletesHoneypot(true);
        float stored = replete.storeHoneypotNectar(0.20f);
        assertEquals(0.20f, stored, 0.001f);
        assertEquals(0.20f, replete.getHoneypotStorageGrams(), 0.001f);

        float dispensed = replete.dispenseHoneypotNectar(0.05f);
        assertEquals(0.05f, dispensed, 0.001f);
        assertEquals(0.15f, replete.getHoneypotStorageGrams(), 0.001f);
    }

    @Test
    @DisplayName("Lot L - Fermented Sap Anesthetic, Proctodeal Microbiome & Soldier Caste Ratio")
    void testFermentedSapAndMicrobiomeAndCasteRatio() {
        Individual combatant = new Individual(UUID.randomUUID(), Caste.SOLDIER);
        assertFalse(combatant.hasCombatAnestheticBuff());
        assertEquals(1.0f, combatant.getCombatPainResistanceMultiplier(), 0.001f);

        combatant.ingestFermentedSap(2.0f); // 2 * 60 = 120s
        assertTrue(combatant.hasCombatAnestheticBuff());
        assertEquals(2.2f, combatant.getCombatPainResistanceMultiplier(), 0.001f);

        Individual matureTermite = new Individual(UUID.randomUUID(), Caste.WORKER);
        Individual nymph = new Individual(UUID.randomUUID(), Caste.WORKER);
        nymph.setGutCellulolyticProtozoaTiter(0.1f);
        assertTrue(matureTermite.transferProctodealMicrobiome(nymph));
        assertEquals(0.7f, nymph.getGutCellulolyticProtozoaTiter(), 0.001f);

        assertTrue(combatant.canDifferentiateNewSoldier(0.10f), "Should allow soldier differentiation below 15%");
        assertFalse(combatant.canDifferentiateNewSoldier(0.16f), "Should inhibit soldier differentiation above 15%");
    }
}
