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
        sender.setResourceAmount(ResourceType.SEED, 100f);
        receiver.setResourceAmount(ResourceType.SEED, 0f);

        // Test Failure (Insufficient funds)
        assertFalse(sender.sendResource(receiver, ResourceType.SEED, 200f));
        assertEquals(100f, sender.getResourceAmount(ResourceType.SEED));
        assertEquals(0f, receiver.getResourceAmount(ResourceType.SEED));

        // Test Success
        assertTrue(sender.sendResource(receiver, ResourceType.SEED, 50f));
        assertEquals(50f, sender.getResourceAmount(ResourceType.SEED));
        assertEquals(50f, receiver.getResourceAmount(ResourceType.SEED));

        // Test Tribute loop (Reciprocal)
        receiver.sendResource(sender, ResourceType.SEED, 10f);
        assertEquals(60f, sender.getResourceAmount(ResourceType.SEED));
        assertEquals(40f, receiver.getResourceAmount(ResourceType.SEED));
    }
}
