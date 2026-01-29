package org.swarmforge.core.ecs;

import org.swarmforge.core.domain.Colony;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry to find Colony domain objects from their UUID.
 * Helps bridge ECS systems with the domain model.
 */
public class ColonyRegistry {
    private static final Map<UUID, Colony> colonies = new ConcurrentHashMap<>();

    public static void register(Colony colony) {
        colonies.put(colony.getId(), colony);
    }

    public static Colony getColony(UUID id) {
        return colonies.get(id);
    }

    public static void clear() {
        colonies.clear();
    }
}
