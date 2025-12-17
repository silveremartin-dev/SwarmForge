package org.swarmforge.core.diplomacy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.species.CustomSpecies;

public class TradeTest {
    @Test
    void testSendResource() {
        CustomSpecies s = new CustomSpecies();
        s.setScientificName("Test Ant");
        Colony sender = new Colony(s, 0, 0, 0);
        Colony receiver = new Colony(s, 10, 10, 0);

        // Setup initial resources
        sender.setFoodStored(100f); // Default to SEED=100
        receiver.setFoodStored(0f);

        // Test Failure (Insufficient funds)
        assertFalse(sender.sendResource(receiver, ResourceType.SEED, 200f));
        assertEquals(100f, sender.getFoodStored());
        assertEquals(0f, receiver.getFoodStored());

        // Test Success
        assertTrue(sender.sendResource(receiver, ResourceType.SEED, 50f));
        assertEquals(50f, sender.getFoodStored());
        assertEquals(50f, receiver.getFoodStored());

        // Test Tribute loop (Reciprocal)
        receiver.sendResource(sender, ResourceType.SEED, 10f);
        assertEquals(60f, sender.getFoodStored());
        assertEquals(40f, receiver.getFoodStored());
    }
}
