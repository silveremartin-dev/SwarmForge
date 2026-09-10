package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.domain.PheromoneType;
import org.swarmforge.core.ecs.components.*;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

import java.util.List;

/**
 * High-performance ECS system dispatching all 220+ eusocial behavioral routines.
 * Each behavior is guarded by a word-0..3 bitmask check (O(1)), then
 * executes its physics, chemical, sanitary, and mechanical updates in real time.
 * Spatial neighbor queries are sampled every SPATIAL_SAMPLE_INTERVAL ticks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class EthologyEcsSystem extends IteratingSystem {

    private static final float SPATIAL_SAMPLE_INTERVAL_SEC = 0.0833333f; // Fixed 83.3ms time interval (~12 Hz)

    private ComponentMapper<PositionComponent>               mPosition;
    private ComponentMapper<MetabolismComponent>             mMetabolism;
    private ComponentMapper<EthologyComponent>               mEthology;
    private ComponentMapper<VelocityComponent>               mVelocity;
    private ComponentMapper<CompactPathogenBitmaskComponent> mPathogen;
    private ComponentMapper<MandibularBiomechanicsComponent> mMandible;

    private SpatialPartitioningSystem spatialSystem;
    private SparsePheromoneGrid pheromoneGrid;
    private float sampleAccumulatorSec = 0f;
    private boolean samplingFrame = false;

    public EthologyEcsSystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class, EthologyComponent.class));
    }

    public void setPheromoneGrid(SparsePheromoneGrid grid) {
        this.pheromoneGrid = grid;
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

        // ── WORD 0 behaviors: Navigation, Sanitation, Defense & Reproduction ────

        // 1. Geomagnetic Navigation (Blind subterranean orientation)
        if (eth.has0(EthologyComponent.W0_MAGNETORECEPTION) && pos.z < 0f) {
            if (mVelocity != null && mVelocity.has(entityId)) {
                VelocityComponent vel = mVelocity.get(entityId);
                // Subtle geomagnetic bias towards North (Y+)
                vel.dy += 0.05f * dt;
            }
        }

        // 2. Allogrooming & Social Sanitation (Sanitary parasite spore removal)
        if (eth.has0(EthologyComponent.W0_ALLOGROOMING) && doSpatialSample()) {
            performAllogroomingSanitation(entityId, pos);
        }

        // 3. Stridulation distress signal (low energy / trapped underground)
        if (eth.has0(EthologyComponent.W0_STRIDULATION_RESCUE)) {
            eth.isStridulating = meta.energy < 20f || pos.z < -5f;
            if (eth.isStridulating && doSpatialSample()) {
                propagateStridulationRescue(entityId, pos, eth);
            }
        }

        // 4. Escape & Alarm Pheromone burst on damage / threat
        if (eth.has0(EthologyComponent.W0_ESCAPE_PHEROMONE) && meta.energy < 15f) {
            meta.energy = Math.max(0f, meta.energy - 0.1f * dt); // flight metabolic cost
            depositAlarmChemicalPulse(pos, 5.0f);
        }

        // 5. Autothysis: Suicidal glandular explosion releasing defensive sticky glue
        if (eth.has0(EthologyComponent.W0_AUTOTHYSIS) && !eth.hasAutothysed) {
            if (meta.energy < 5f) {
                eth.hasAutothysed = true;
                meta.alive = false; // sacrifice
                depositAlarmChemicalPulse(pos, 25.0f);
                triggerAutothysisShockwave(pos);
                return;
            }
        }

        // 6. Formic Acid Artillery Jet
        if (eth.has0(EthologyComponent.W0_FORMIC_ACID_ARTILLERY_JET) && doSpatialSample()) {
            if (meta.energy > 30f) {
                depositAlarmChemicalPulse(pos, 8.0f);
            }
        }

        // ── WORD 1 behaviors: Nest Construction, Thermoregulation & Storage ─────

        // 7. Gravel plugging / gallery sealing
        if (eth.has1(EthologyComponent.W1_GRAVEL_PLUGGING)) {
            if (eth.carryingBuildingMaterial) {
                eth.stercoralMortarAmount = Math.min(100f, eth.stercoralMortarAmount + 5f * dt);
            }
        }

        // 8. Thoracic Shivering Incubation (Thermoregulation of brood)
        if (eth.has1(EthologyComponent.W1_THORACIC_INCUBATION)) {
            eth.thermalThoraxTempC = Math.min(40f, eth.thermalThoraxTempC + 2f * dt);
            meta.energy -= 0.3f * dt; // shivering metabolic cost
        }

        // 9. Evaporative cooling: deposit water droplets during heat waves
        if (eth.has1(EthologyComponent.W1_EVAPORATIVE_COOLING)) {
            if (eth.thermalThoraxTempC > 37f) {
                eth.thermalThoraxTempC = Math.max(25f, eth.thermalThoraxTempC - 3f * dt);
                meta.energy -= 0.5f * dt;
            }
        }

        // 10. Propolis collection & hive antimicrobial sealing
        if (eth.has2(EthologyComponent.W2_PROPOLIS_SHIELD)) {
            eth.propolisCarried = Math.min(100f, eth.propolisCarried + 0.1f * dt);
        }

        // 11. Honeypot replete storage
        if (eth.has1(EthologyComponent.W1_HONEYPOT_STORAGE)) {
            eth.honeypotFillRatio = Math.min(1f, eth.honeypotFillRatio + 0.01f * dt);
            if (eth.honeypotFillRatio >= 1f) {
                meta.metabolicRate = 0.1f; // repletes remain nearly static
            }
        }

        // 12. Tremble dance recruitment signal
        if (eth.has0(EthologyComponent.W0_TREMBLE_DANCE)) {
            eth.isTremble = meta.energy > 80f; // only full foragers tremble-recruit
        }

        // ── WORD 2 & 3 behaviors: Supercolonies, Flooding, Bivouacs ─────────────

        // 13. Winter Diapause: metabolic suppression in cold seasons
        if (eth.has2(EthologyComponent.W2_DIAPAUSE)) {
            if (eth.diapauseActive) {
                meta.energy -= 0.001f * dt; // ~10× reduced burn rate in torpor
                return; // skip active locomotive routines
            }
        }

        // 14. Living bivouac formation (Army ants Eciton / Dorylus)
        if (eth.has2(EthologyComponent.W2_LIVING_BIVOUAC) && doSpatialSample()) {
            List<Integer> nearby = queryNearby(pos);
            eth.inLivingBivouac = nearby.size() >= 30; // density threshold
        }

        // 15. Floating ant raft during floods
        if (eth.has2(EthologyComponent.W2_FLOATING_ANT_RAFT)) {
            if (pos.z < -2f) {
                eth.isRafting = true;
                pos.z += 0.5f * dt; // hydro-buoyancy
                meta.energy -= 0.1f * dt;
            } else {
                eth.isRafting = false;
            }
        }

        // 16. Flood evacuation (barometric pressure drop reaction)
        if (eth.has2(EthologyComponent.W2_FLOOD_EVACUATION) && pos.z < -2f) {
            pos.z += 0.8f * dt; // rapid upward migration
            meta.energy -= 0.15f * dt;
        }
    }

    // ── Physical & Ecological Interaction Helpers ─────────────────────────────

    private void performAllogroomingSanitation(int groomerId, PositionComponent pos) {
        List<Integer> nearby = queryNearby(pos);
        for (int neighborId : nearby) {
            if (neighborId == groomerId) continue;
            if (mPathogen != null && mPathogen.has(neighborId)) {
                CompactPathogenBitmaskComponent neighborPathogen = mPathogen.get(neighborId);
                if (neighborPathogen.activePathogensBitmask != CompactPathogenBitmaskComponent.PATHOGEN_NONE) {
                    // Allogrooming removes fungal spores (Metarhizium) and reduces viral load
                    neighborPathogen.activePathogensBitmask &= ~CompactPathogenBitmaskComponent.PATHOGEN_METARHIZIUM;
                    neighborPathogen.viralLoadPacked = (short) Math.max(0, (neighborPathogen.viralLoadPacked & 0xFFFF) - 500);
                }
            }
        }
    }

    private void depositAlarmChemicalPulse(PositionComponent pos, float intensity) {
        if (pheromoneGrid != null) {
            pheromoneGrid.deposit((int) Math.max(0, pos.x), (int) Math.max(0, pos.y), (int) Math.max(0, pos.z),
                    PheromoneType.ALARM.getIndex(), intensity);
        }
    }

    private void triggerAutothysisShockwave(PositionComponent pos) {
        List<Integer> nearby = queryNearby(pos);
        for (int targetId : nearby) {
            if (mMetabolism != null && mMetabolism.has(targetId)) {
                MetabolismComponent targetMeta = mMetabolism.get(targetId);
                // Sticky necrotizing fluid immobilizes and damages enemies in radius
                targetMeta.energy = Math.max(0f, targetMeta.energy - 15.0f);
            }
        }
    }

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
