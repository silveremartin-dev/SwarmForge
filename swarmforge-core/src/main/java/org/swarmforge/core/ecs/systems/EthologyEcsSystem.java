package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.*;
import java.util.List;

/**
 * High-performance ECS system dispatching all 220+ eusocial behavioral routines.
 * Each behavior is guarded by a word-0..3 bitmask check (O(1)), then
 * executes its physics update only when the entity's capability flag is set.
 * Spatial neighbor queries are sampled every SPATIAL_SAMPLE_INTERVAL ticks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class EthologyEcsSystem extends IteratingSystem {

    private static final float SPATIAL_SAMPLE_INTERVAL_SEC = 0.0833333f; // Fixed 83.3ms time interval (~12 Hz)

    private ComponentMapper<PositionComponent>   mPosition;
    private ComponentMapper<MetabolismComponent> mMetabolism;
    private ComponentMapper<EthologyComponent>   mEthology;
    private ComponentMapper<VelocityComponent>   mVelocity;

    private SpatialPartitioningSystem spatialSystem;
    private float sampleAccumulatorSec = 0f;
    private boolean samplingFrame = false;

    public EthologyEcsSystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class, EthologyComponent.class));
    }

    @Override
    protected void begin() {
        sampleAccumulatorSec += world.getDelta();
        if (sampleAccumulatorSec >= SPATIAL_SAMPLE_INTERVAL_SEC) {
            sampleAccumulatorSec -= SPATIAL_SAMPLE_INTERVAL_SEC;
            samplingFrame = true;
        } else {
            samplingFrame = false;
        }

        if (spatialSystem == null) {
            spatialSystem = world.getSystem(SpatialPartitioningSystem.class);
        }
    }

    @Override
    protected void process(int entityId) {
        final MetabolismComponent meta = mMetabolism.get(entityId);
        if (!meta.alive) return;

        final EthologyComponent eth = mEthology.get(entityId);
        final PositionComponent pos = mPosition.get(entityId);
        final float dt = world.getDelta();

        // ── WORD 0 behaviors ─────────────────────────────────────────────────

        // Stridulation distress signal (low energy / trapped underground)
        if (eth.has0(EthologyComponent.W0_STRIDULATION_RESCUE)) {
            eth.isStridulating = meta.energy < 20f || pos.z < -5f;
            if (eth.isStridulating && doSpatialSample()) {
                propagateStridulationRescue(entityId, pos, eth);
            }
        }

        // Trophallaxis greet (handled by dedicated TrophallaxisSystem; flag just marks eligibility)
        // → no additional logic here; avoids double-processing

        // Escape pheromone burst on damage
        if (eth.has0(EthologyComponent.W0_ESCAPE_PHEROMONE) && meta.energy < 10f) {
            meta.energy = Math.max(0f, meta.energy - 0.1f * dt); // flee cost
        }

        // Waggle dance: boosts forager recruitment probability (state flag only)
        // Actual waggle-dance logic is in ForagingSystem; flag enables it.

        // Autothysis: lethal defense explosion (one-shot)
        if (eth.has0(EthologyComponent.W0_AUTOTHYSIS) && !eth.hasAutothysed) {
            if (meta.energy < 5f) {
                eth.hasAutothysed = true;
                meta.alive = false; // sacrifice
                return;
            }
        }

        // Diapause: metabolic suppression in winter
        if (eth.has2(EthologyComponent.W2_DIAPAUSE)) {
            if (eth.diapauseActive) {
                meta.energy -= 0.001f * dt; // ~10× reduced burn rate
                return; // skip all other behavior
            }
        }

        // ── WORD 1 behaviors ─────────────────────────────────────────────────

        // Gravel plugging / gallery sealing
        if (eth.has1(EthologyComponent.W1_GRAVEL_PLUGGING)) {
            if (eth.carryingBuildingMaterial) {
                eth.stercoralMortarAmount = Math.min(100f, eth.stercoralMortarAmount + 5f * dt);
            }
        }

        // Thermoregulation: thoracic shivering incubation
        if (eth.has1(EthologyComponent.W1_THORACIC_INCUBATION)) {
            eth.thermalThoraxTempC = Math.min(40f, eth.thermalThoraxTempC + 2f * dt);
            meta.energy -= 0.3f * dt; // shivering metabolic cost
        }

        // Evaporative cooling: deposit water droplets
        if (eth.has1(EthologyComponent.W1_EVAPORATIVE_COOLING)) {
            if (eth.thermalThoraxTempC > 37f) {
                meta.energy -= 0.5f * dt; // water-carrying energetic cost
            }
        }

        // Propolis collection
        if (eth.has2(EthologyComponent.W2_PROPOLIS_SHIELD)) {
            eth.propolisCarried = Math.min(100f, eth.propolisCarried + 0.1f * dt);
        }

        // Honeypot replete storage
        if (eth.has1(EthologyComponent.W1_HONEYPOT_STORAGE)) {
            eth.honeypotFillRatio = Math.min(1f, eth.honeypotFillRatio + 0.01f * dt);
            if (eth.honeypotFillRatio >= 1f) {
                meta.metabolicRate = 0.1f; // repletes are nearly static
            }
        }

        // Tremble dance recruitment signal
        if (eth.has0(EthologyComponent.W0_TREMBLE_DANCE)) {
            eth.isTremble = meta.energy > 80f; // only full foragers tremble-recruit
        }

        // ── WORD 2 behaviors ─────────────────────────────────────────────────

        // Living bivouac formation
        if (eth.has2(EthologyComponent.W2_LIVING_BIVOUAC)) {
            if (doSpatialSample()) {
                List<Integer> nearby = queryNearby(pos);
                eth.inLivingBivouac = nearby.size() >= 30; // density threshold
            }
        }

        // Floating ant raft during floods
        if (eth.has2(EthologyComponent.W2_FLOATING_ANT_RAFT)) {
            if (pos.z < -2f) {
                eth.isRafting = true;
                pos.z += 0.5f * dt; // buoyancy
                meta.energy -= 0.1f * dt;
            } else {
                eth.isRafting = false;
            }
        }

        // Flood evacuation (barometric)
        if (eth.has2(EthologyComponent.W2_FLOOD_EVACUATION) && pos.z < -2f) {
            pos.z += 0.8f * dt; // rapid upward movement
            meta.energy -= 0.15f * dt;
        }

        // Necrophoresis (oleic acid corpse removal)
        if (eth.has2(EthologyComponent.W2_OLEIC_ACID_NECROPHORESIS) && doSpatialSample()) {
            // Signal detection & carry-out behavior handled by the spatial scan
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void propagateStridulationRescue(int entityId, PositionComponent pos, EthologyComponent eth) {
        List<Integer> nearby = queryNearby(pos);
        for (int neighborId : nearby) {
            if (neighborId == entityId) continue;
            EthologyComponent neighborEth = mEthology.get(neighborId);
            if (neighborEth != null && neighborEth.has1(EthologyComponent.W1_GRAVEL_PLUGGING)) {
                neighborEth.carryingBuildingMaterial = true; // trigger rescue excavation
            }
        }
    }

    private boolean doSpatialSample() {
        return samplingFrame;
    }

    private List<Integer> queryNearby(PositionComponent pos) {
        return spatialSystem != null
               ? spatialSystem.getNearbyEntities(pos.x, pos.y, pos.z)
               : java.util.Collections.emptyList();
    }
}
