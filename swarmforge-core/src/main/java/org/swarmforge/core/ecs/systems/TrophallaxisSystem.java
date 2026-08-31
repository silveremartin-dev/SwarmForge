package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import org.swarmforge.core.ecs.components.InventoryComponent;
import java.util.List;

/**
 * ECS System handling liquid food exchange (Trophallaxis) between colony nestmates.
 * Allows well-fed foragers returning to the nest to share crop energy with hungry nurses/queens
 * in zero-allocation O(1) spatial queries.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrophallaxisSystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;
    private ComponentMapper<MetabolismComponent> mMetabolism;
    private ComponentMapper<InventoryComponent> mInventory;

    private SpatialPartitioningSystem spatialSystem;

    public TrophallaxisSystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class));
    }

    private int tickCounter = 0;

    @Override
    protected void begin() {
        tickCounter++;
    }

    @Override
    protected void process(int donorId) {
        if (tickCounter % 5 != 0) return; // Process spatial exchange every 5 ticks

        MetabolismComponent donorMeta = mMetabolism.get(donorId);
        if (!donorMeta.alive || donorMeta.energy < 60.0f) return; // Donor must have spare energy

        PositionComponent donorPos = mPosition.get(donorId);
        if (spatialSystem == null) {
            spatialSystem = world.getSystem(SpatialPartitioningSystem.class);
        }
        if (spatialSystem == null) return;

        List<Integer> nearby = spatialSystem.getNearbyEntities(donorPos.x, donorPos.y, donorPos.z);
        float interactionRadiusSq = 0.25f; // 0.5m radius

        for (int recipientId : nearby) {
            if (recipientId == donorId) continue;

            MetabolismComponent recipMeta = mMetabolism.get(recipientId);
            if (recipMeta != null && recipMeta.alive && recipMeta.energy < 40.0f) {
                PositionComponent recipPos = mPosition.get(recipientId);
                float dx = recipPos.x - donorPos.x;
                float dy = recipPos.y - donorPos.y;
                float dz = recipPos.z - donorPos.z;

                if ((dx * dx + dy * dy + dz * dz) <= interactionRadiusSq) {
                    // Perform Trophallaxis exchange: transfer up to 15 energy units
                    float transferAmount = Math.min(15.0f, (donorMeta.energy - 40.0f) * 0.5f);
                    donorMeta.energy -= transferAmount;
                    recipMeta.energy = Math.min(recipMeta.maxEnergy, recipMeta.energy + transferAmount);
                    recipMeta.hunger = Math.max(0.0f, recipMeta.hunger - transferAmount * 0.5f);
                    break; // One exchange per process pass
                }
            }
        }
    }
}
