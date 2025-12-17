package org.swarmforge.core.diplomacy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

public class DiplomacyManagerTest {
    @Test
    void testRelationships() {
        UUID colony1 = UUID.randomUUID();
        UUID colony2 = UUID.randomUUID();
        DiplomacyManager mgr = new DiplomacyManager(colony1);

        // Default Neutral
        assertEquals(RelationshipStatus.NEUTRAL, mgr.getStatus(colony2));

        // Ally
        mgr.setStatus(colony2, RelationshipStatus.ALLY);
        assertTrue(mgr.isAlly(colony2));
        assertFalse(mgr.isEnemy(colony2));

        // Enemy
        mgr.setStatus(colony2, RelationshipStatus.ENEMY);
        assertTrue(mgr.isEnemy(colony2));
        assertFalse(mgr.isAlly(colony2));

        // Self check
        assertEquals(RelationshipStatus.ALLY, mgr.getStatus(colony1));
    }
}
