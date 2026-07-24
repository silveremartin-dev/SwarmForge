/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecology;

import org.junit.jupiter.api.Test;
import org.swarmforge.core.behavior.Interaction;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.genetics.Genome;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BiochemistryTest {

    @Test
    void testPheromoneSignatureMatching() {
        UUID colony1 = UUID.randomUUID();
        UUID colony2 = UUID.randomUUID();

        PheromoneSignature sig1 = new PheromoneSignature(colony1, 1.0f, PheromoneSignature.CompoundType.TRAIL);
        PheromoneSignature sig2 = new PheromoneSignature(colony1, 0.8f, PheromoneSignature.CompoundType.ALARM);
        PheromoneSignature sig3 = new PheromoneSignature(colony2, 1.0f, PheromoneSignature.CompoundType.TRAIL);

        assertTrue(sig1.isSameColony(sig2.colonyId()));
        assertFalse(sig1.isSameColony(sig3.colonyId()));
    }

    @Test
    void testGenomeMutation() {
        Genome g = new Genome();
        float initialSpeed = g.getSpeedMultiplier();

        // Force mutation loop
        boolean mutated = false;
        for (int i = 0; i < 100; i++) {
            g.mutate(1.0f); // High rate
            if (g.getSpeedMultiplier() != initialSpeed) {
                mutated = true;
                break;
            }
        }
        assertTrue(mutated, "Genome should mutate given high mutation rate");
    }

    @Test
    void testTrophallaxis() {
        UUID colId = UUID.randomUUID();
        Individual giver = new Individual(colId, Individual.Caste.WORKER, 0, 0, 0);
        Individual receiver = new Individual(colId, Individual.Caste.WORKER, 0, 0, 0);

        giver.setCarriedItem(Individual.CarriedItem.FOOD);
        giver.setCarriedResourceType(org.swarmforge.core.domain.ResourceType.HONEYDEW);
        receiver.setCarriedItem(Individual.CarriedItem.NONE);

        boolean success = Interaction.trophallaxis(giver, receiver);

        assertTrue(success, "Trophallaxis should succeed");
        assertEquals(Individual.CarriedItem.NONE, giver.getCarriedItem());
        assertEquals(Individual.CarriedItem.FOOD, receiver.getCarriedItem());
        assertEquals(org.swarmforge.core.domain.ResourceType.HONEYDEW, receiver.getCarriedResourceType());
    }
}
