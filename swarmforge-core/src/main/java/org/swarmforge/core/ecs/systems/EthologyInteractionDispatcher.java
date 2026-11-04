/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecs.systems;

import com.artemis.ComponentMapper;
import org.swarmforge.core.domain.PheromoneType;
import org.swarmforge.core.ecs.components.CompactPathogenBitmaskComponent;
import org.swarmforge.core.ecs.components.EthologyComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.VelocityComponent;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

import java.util.List;

/**
 * Dispatches physical, sanitary, acoustic, and trophallactic ecological interactions between neighboring ECS entities.
 */
final class EthologyInteractionDispatcher {

    private EthologyInteractionDispatcher() {}

    static void performAllogroomingSanitation(
            int groomerId,
            PositionComponent pos,
            SpatialPartitioningSystem spatialSystem,
            ComponentMapper<CompactPathogenBitmaskComponent> mPathogen
    ) {
        List<Integer> nearby = queryNearby(spatialSystem, pos);
        for (int neighborId : nearby) {
            if (neighborId == groomerId) continue;
            if (mPathogen != null && mPathogen.has(neighborId)) {
                CompactPathogenBitmaskComponent neighborPathogen = mPathogen.get(neighborId);
                if (neighborPathogen.activePathogensBitmask != CompactPathogenBitmaskComponent.PATHOGEN_NONE) {
                    neighborPathogen.activePathogensBitmask &= ~CompactPathogenBitmaskComponent.PATHOGEN_METARHIZIUM;
                    neighborPathogen.viralLoadPacked = (short) Math.max(0, (neighborPathogen.viralLoadPacked & 0xFFFF) - 500);
                }
            }
        }
    }

    static void depositAlarmChemicalPulse(SparsePheromoneGrid pheromoneGrid, PositionComponent pos, float intensity) {
        if (pheromoneGrid != null) {
            int px = Math.max(0, (int) pos.x);
            int py = Math.max(0, (int) pos.y);
            int pz = Math.max(0, (int) pos.z);
            if (pheromoneGrid.getTerrarium() != null) {
                org.swarmforge.core.domain.Terrarium terr = pheromoneGrid.getTerrarium();
                px = Math.min(terr.getWidth() - 1, px);
                py = Math.min(terr.getHeight() - 1, py);
                pz = Math.min(terr.getDepth() - 1, pz);
            }
            pheromoneGrid.deposit(px, py, pz, PheromoneType.ALARM.getIndex(), intensity);
        }
    }

    static void triggerAutothysisShockwave(
            PositionComponent pos,
            SpatialPartitioningSystem spatialSystem,
            ComponentMapper<MetabolismComponent> mMetabolism
    ) {
        List<Integer> nearby = queryNearby(spatialSystem, pos);
        for (int targetId : nearby) {
            if (mMetabolism != null && mMetabolism.has(targetId)) {
                MetabolismComponent targetMeta = mMetabolism.get(targetId);
                targetMeta.energy = Math.max(0f, targetMeta.energy - 15.0f);
            }
        }
    }

    static void propagateStridulationRescue(
            int entityId,
            PositionComponent pos,
            EthologyComponent eth,
            SpatialPartitioningSystem spatialSystem,
            ComponentMapper<PositionComponent> mPosition,
            ComponentMapper<EthologyComponent> mEthology,
            ComponentMapper<VelocityComponent> mVelocity
    ) {
        List<Integer> nearby = queryNearby(spatialSystem, pos);
        float attenuationCoeff = 0.04f;

        for (int neighborId : nearby) {
            if (neighborId == entityId) continue;
            if (mPosition != null && mPosition.has(neighborId)) {
                PositionComponent neighborPos = mPosition.get(neighborId);
                float dx = pos.x - neighborPos.x;
                float dy = pos.y - neighborPos.y;
                float dz = pos.z - neighborPos.z;
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                float amplitude = (float) (Math.exp(-attenuationCoeff * dist) / Math.max(0.2f, dist));
                if (amplitude > 0.15f) {
                    EthologyComponent neighborEth = mEthology != null ? mEthology.get(neighborId) : null;
                    if (neighborEth != null) {
                        neighborEth.carryingBuildingMaterial = true;
                    }
                    if (mVelocity != null && mVelocity.has(neighborId) && dist > 0.1f) {
                        VelocityComponent vel = mVelocity.get(neighborId);
                        vel.dx = (dx / dist) * 0.8f;
                        vel.dy = (dy / dist) * 0.8f;
                    }
                }
            }
        }
    }

    static void performStomatodealTrophallaxis(
            int donorId,
            PositionComponent pos,
            MetabolismComponent donorMeta,
            SpatialPartitioningSystem spatialSystem,
            ComponentMapper<MetabolismComponent> mMetabolism
    ) {
        List<Integer> nearby = queryNearby(spatialSystem, pos);
        for (int recipientId : nearby) {
            if (recipientId == donorId) continue;
            if (mMetabolism != null && mMetabolism.has(recipientId)) {
                MetabolismComponent recipientMeta = mMetabolism.get(recipientId);
                if (recipientMeta.alive && recipientMeta.energy < 40f) {
                    float transfer = Math.min(10.0f, (donorMeta.energy - 50.0f) * 0.5f);
                    if (transfer > 0.5f) {
                        donorMeta.energy -= transfer;
                        recipientMeta.energy += transfer;
                        break;
                    }
                }
            }
        }
    }

    private static List<Integer> queryNearby(SpatialPartitioningSystem spatialSystem, PositionComponent pos) {
        return spatialSystem != null
                ? spatialSystem.getNearbyEntities(pos.x, pos.y, pos.z)
                : java.util.Collections.emptyList();
    }
}
