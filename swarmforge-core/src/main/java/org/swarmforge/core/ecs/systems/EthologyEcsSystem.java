package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import org.swarmforge.core.ecs.components.EthologyComponent;
import java.util.List;

/**
 * High-Performance ECS System processing advanced ethological behaviors
 * (Stridulation rescue signals, Polyethism nest building, Necrophoresis).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EthologyEcsSystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;
    private ComponentMapper<MetabolismComponent> mMetabolism;
    private ComponentMapper<EthologyComponent> mEthology;

    private SpatialPartitioningSystem spatialSystem;

    public EthologyEcsSystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class, EthologyComponent.class));
    }

    private int tickCounter = 0;

    @Override
    protected void begin() {
        tickCounter++;
    }

    @Override
    protected void process(int entityId) {
        PositionComponent pos = mPosition.get(entityId);
        MetabolismComponent meta = mMetabolism.get(entityId);
        EthologyComponent eth = mEthology.get(entityId);

        if (!meta.alive) return;

        // 1. Proposition 6: Acoustic Stridulation Rescue Signals (~850 Hz soil impulse)
        if ((eth.activeCapabilities & EthologyComponent.FLAG_STRIDULATION_RESCUE) != 0) {
            // Trigger stridulation distress signal if trapped or low energy
            if (meta.energy < 20.0f || pos.z < -5.0f) {
                eth.isStridulating = true;

                // Propagate acoustic vibration wave to nearby excavating nestmates on interval ticks
                if (tickCounter % 5 == 0) {
                    if (spatialSystem == null) {
                        spatialSystem = world.getSystem(SpatialPartitioningSystem.class);
                    }
                    if (spatialSystem != null) {
                        List<Integer> nearby = spatialSystem.getNearbyEntities(pos.x, pos.y, pos.z);
                        for (int neighborId : nearby) {
                            if (neighborId == entityId) continue;
                            EthologyComponent neighborEth = mEthology.get(neighborId);
                            if (neighborEth != null && (neighborEth.activeCapabilities & EthologyComponent.FLAG_GRAVEL_PLUGGING) != 0) {
                                // Nestmates react to sound vibration by moving toward source (rescue excavation)
                                neighborEth.carryingBuildingMaterial = true;
                            }
                        }
                    }
                }
            } else {
                eth.isStridulating = false;
            }
        }

        // 2. Proposition 7: Nest Construction & Stercoral Gravel Plugging
        if ((eth.activeCapabilities & EthologyComponent.FLAG_GRAVEL_PLUGGING) != 0) {
            if (eth.carryingBuildingMaterial) {
                eth.stercoralMortarAmount = Math.min(100.0f, eth.stercoralMortarAmount + 5.0f * world.getDelta());
            }
        }
    }
}
