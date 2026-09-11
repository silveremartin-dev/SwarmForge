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
 * Validates advanced biological behavioral mechanisms:
 * 1. Necrophoresis, Oleic Acid Oxidation & Cemetery Refuse Deposition
 * 2. Active Social Thermoregulation (Shivering Thermogenesis & Active Fanning)
 * 3. Slave-Raiding Dynamics (Dulosis - Polyergus / Formica Host Integration)
 * 4. Bio-Acoustic Stridulation & Substrate Distress Rescue
 */
public class MassRealismBehaviorsTest {

    @Test
    @DisplayName("1. Necrophoresis: Oleic acid oxidation after 3 min and cemetery transport")
    void testNecrophoresisAndCemetery() {
        UUID colonyId = UUID.randomUUID();
        Individual corpse = new Individual(colonyId, Individual.Caste.WORKER, 10f, 10f, 0f);
        corpse.die("Natural Causes / Old Age");
        assertFalse(corpse.isAlive(), "Individual should be dead");
        assertFalse(corpse.isOleicAcidEmitted(), "Fresh corpse should not emit oxidized oleic acid immediately");

        // Simulate 200 seconds of post-mortem decomposition
        corpse.tick(200.0f);
        assertTrue(corpse.getDecompositionAgeSeconds() >= 180.0f, "Decomposition time should advance");
        assertTrue(corpse.isOleicAcidEmitted(), "Corpse should emit oxidized oleic acid after >180s");

        // Undertaker worker picks up corpse
        Individual undertaker = new Individual(colonyId, Individual.Caste.WORKER, 10f, 10f, 0f);
        undertaker.setJob(Individual.Job.UNDERTAKER);
        boolean pickedUp = undertaker.pickUpCorpse(corpse);
        assertTrue(pickedUp, "Undertaker should pick up dead nestmate");
        assertEquals(Individual.CarriedItem.DEAD_ANT, undertaker.getCarriedItem(), "Undertaker must carry DEAD_ANT");

        // Undertaker transports and deposits corpse at external refuse cemetery pit
        boolean deposited = undertaker.depositCorpseAtRefuse(25.0f, 25.0f, 0.0f);
        assertTrue(deposited, "Undertaker should deposit corpse at cemetery refuse zone");
        assertEquals(Individual.CarriedItem.NONE, undertaker.getCarriedItem(), "Undertaker should no longer carry corpse");
    }

    @Test
    @DisplayName("2. Active Social Thermoregulation: Shivering warming & Fanning cooling")
    void testActiveSocialThermoregulation() {
        UUID colonyId = UUID.randomUUID();
        Individual ant = new Individual(colonyId, Individual.Caste.WORKER, 5f, 5f, 0f);
        ant.setEnergy(100.0f);
        ant.setThoraxTemperatureC(24.0f);

        // Shivering thermogenesis (when nursery is cold)
        ant.setShiveringThermogenesis(true);
        ant.tick(5.0f);

        assertTrue(ant.getThoraxTemperatureC() > 24.0f, "Thorax temperature should rise during shivering thermogenesis");
        assertTrue(ant.getEnergy() < 100.0f, "Metabolic energy should be consumed by thoracic muscle shivering");

        // Fanning heat dissipation (when hive/nursery overheats)
        ant.setShiveringThermogenesis(false);
        ant.setThoraxTemperatureC(36.0f);
        ant.setWingFanning(true);
        ant.tick(5.0f);

        assertTrue(ant.getThoraxTemperatureC() < 36.0f, "Thorax temperature should decrease during active wing fanning");
    }

    @Test
    @DisplayName("3. Dulosis / Slave-Making: Brood capture & Host colony integration")
    void testDulosisSlaveMakingIntegration() {
        UUID slavemakerColonyId = UUID.randomUUID();
        UUID hostColonyId = UUID.randomUUID();

        // Host colony pupa/larva
        Individual hostBrood = new Individual(hostColonyId, Individual.Caste.WORKER, 12f, 12f, 0f);
        hostBrood.setLifeStage(Individual.LifeStage.PUPA);

        // Slavemaker warrior
        Individual slavemaker = new Individual(slavemakerColonyId, Individual.Caste.SOLDIER, 12f, 12f, 0f);
        boolean stolen = slavemaker.stealBrood(hostBrood);
        assertTrue(stolen, "Slavemaker warrior should capture host brood");
        assertEquals(Individual.CarriedItem.BROOD, slavemaker.getCarriedItem(), "Slavemaker should carry stolen brood");

        // Stolen brood arrives at master nest and is reared into enslaved worker
        hostBrood.integrateAsEnslavedWorker(slavemakerColonyId);
        assertTrue(hostBrood.isEnslaved(), "Brood should be flagged as enslaved");
        assertEquals(hostColonyId, hostBrood.getHostColonyId(), "Host lineage should be recorded");
        assertEquals(slavemakerColonyId, hostBrood.getColonyId(), "Colony ID should now be master slavemaker colony");
        assertEquals(Individual.LifeStage.ADULT, hostBrood.getLifeStage(), "Brood matures into adult");
        assertEquals(Individual.Job.NURSE, hostBrood.getJob(), "Enslaved worker performs domestic nest duties");
    }

    @Test
    @DisplayName("4. Bio-Acoustic Stridulation: Seismic distress signal generation")
    void testBioAcousticStridulationRescue() {
        UUID colonyId = UUID.randomUUID();
        Individual trappedAnt = new Individual(colonyId, Individual.Caste.WORKER, 8f, 8f, 2f);

        assertFalse(trappedAnt.isStridulatingRescueCall(), "Ant should not stridulate by default");
        trappedAnt.triggerStridulationRescue();

        assertTrue(trappedAnt.isStridulatingRescueCall(), "Trapped ant should emit stridulation rescue call");
        assertEquals(850.0f, trappedAnt.getStridulationFrequencyHz(), 1.0f, "Stridulation frequency should match biological pitch ~850 Hz");
        assertEquals(80.0f, trappedAnt.getStridulationIntensityDb(), 1.0f, "Substrate vibration sound intensity should be ~80 dB");
    }
}
