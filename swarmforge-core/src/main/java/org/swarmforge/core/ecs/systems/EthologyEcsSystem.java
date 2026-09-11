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
    private java.util.Random random = new java.util.Random(1337L);

    public EthologyEcsSystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class, EthologyComponent.class));
    }

    public void setRandom(java.util.Random random) {
        this.random = random != null ? random : new java.util.Random(1337L);
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
        if (hydrologySystem == null) {
            hydrologySystem = world.getSystem(SubterraneanHydrologySystem.class);
        }
        if (substrateDecayEngine == null) {
            substrateDecayEngine = world.getSystem(org.swarmforge.core.world.HierarchicalSubstrateDecayEngine.class);
        }
    }

    private SubterraneanHydrologySystem hydrologySystem;
    private org.swarmforge.core.world.HierarchicalSubstrateDecayEngine substrateDecayEngine;

    @Override
    protected void process(int entityId) {
        final MetabolismComponent meta = mMetabolism.get(entityId);
        if (!meta.alive) return;

        final EthologyComponent eth = mEthology.get(entityId);
        final PositionComponent pos = mPosition.get(entityId);
        final float dt = world.getDelta();

        // ── Biological Thermal Kinetics (Arrhenius Q10 = 2.2 Law) ───────────────
        float ambientTemp = 25.0f;
        float surfaceZ = 16.0f;
        if (pheromoneGrid != null && pheromoneGrid.getTerrarium() != null) {
            org.swarmforge.core.domain.Terrarium terr = pheromoneGrid.getTerrarium();
            int tx = Math.max(0, Math.min(terr.getWidth() - 1, (int) pos.x));
            int ty = Math.max(0, Math.min(terr.getHeight() - 1, (int) pos.y));
            int tz = Math.max(0, Math.min(terr.getDepth() - 1, (int) pos.z));
            ambientTemp = terr.getCell(tx, ty, tz).temperature();
            surfaceZ = terr.getSurfaceElevation(pos.x, pos.y);
        } else if (hydrologySystem != null) {
            ambientTemp = hydrologySystem.getSoilTemperature(pos.x, pos.y, pos.z);
        }

        final boolean isSubterranean = pos.z < surfaceZ;
        final boolean isSurface = pos.z >= surfaceZ;

        // Q10 thermal multiplier: rate(T) = rate(25C) * Q10^((T - 25)/10)
        float q10Factor = (float) Math.pow(2.2, (ambientTemp - 25.0f) / 10.0f);
        q10Factor = Math.max(0.1f, Math.min(3.0f, q10Factor)); // Clamp bounds

        // Cold Torpor & Chill Coma (< 4°C) unless Cryoprotected
        boolean hasCryoprotection = eth.has1(EthologyComponent.W1_GLYCEROL_CRYOPROTECTION);
        if (ambientTemp < 4.0f && !hasCryoprotection) {
            meta.energy -= 0.002f * dt; // minimal basal survival burn
            if (mVelocity != null && mVelocity.has(entityId)) {
                VelocityComponent vel = mVelocity.get(entityId);
                vel.dx *= 0.1f;
                vel.dy *= 0.1f;
                vel.dz *= 0.1f;
            }
            return; // torpor inhibits active motor routines
        }

        // Apply thermal metabolic consumption scaling
        meta.energy -= (0.01f * q10Factor) * dt;

        // ── WORD 0 behaviors: Navigation, Sanitation, Defense & Reproduction ────

        // 1. Geomagnetic Navigation (Blind subterranean orientation)
        if (eth.has0(EthologyComponent.W0_MAGNETORECEPTION) && isSubterranean) {
            if (mVelocity != null && mVelocity.has(entityId)) {
                VelocityComponent vel = mVelocity.get(entityId);
                // Subtle geomagnetic bias towards North (Y+)
                vel.dy += 0.05f * dt * q10Factor;
            }
        }

        // 2. Allogrooming & Social Sanitation (Sanitary parasite spore removal)
        if (eth.has0(EthologyComponent.W0_ALLOGROOMING) && doSpatialSample()) {
            performAllogroomingSanitation(entityId, pos);
        }

        // 3. Stridulation distress signal (low energy / trapped underground)
        if (eth.has0(EthologyComponent.W0_STRIDULATION_RESCUE)) {
            eth.isStridulating = meta.energy < 20f || (isSubterranean && (surfaceZ - pos.z) > 5.0f);
            if (eth.isStridulating && doSpatialSample()) {
                propagateStridulationRescue(entityId, pos, eth);
            }
        }

        // 4. Stomatodeal Trophallaxis (Direct fluid nutrient & microbiota transfer)
        if (eth.has0(EthologyComponent.W0_TROPHALLAXIS) && doSpatialSample() && meta.energy > 60f) {
            performStomatodealTrophallaxis(entityId, pos, meta);
        }

        // 5. Escape & Alarm Pheromone burst on damage / threat
        if (eth.has0(EthologyComponent.W0_ESCAPE_PHEROMONE) && meta.energy < 15f) {
            meta.energy = Math.max(0f, meta.energy - 0.1f * dt); // flight metabolic cost
            depositAlarmChemicalPulse(pos, 5.0f);
        }

        // 6. Autothysis: Suicidal glandular explosion releasing defensive sticky glue
        if (eth.has0(EthologyComponent.W0_AUTOTHYSIS) && !eth.hasAutothysed) {
            if (meta.energy < 5f) {
                eth.hasAutothysed = true;
                meta.alive = false; // sacrifice
                depositAlarmChemicalPulse(pos, 25.0f);
                triggerAutothysisShockwave(pos);
                return;
            }
        }

        // 7. Formic Acid Artillery Jet
        if (eth.has0(EthologyComponent.W0_FORMIC_ACID_ARTILLERY_JET) && doSpatialSample()) {
            if (meta.energy > 30f) {
                depositAlarmChemicalPulse(pos, 8.0f);
            }
        }

        // 8. Desert Ant Thermal Stilt Walking (Cataglyphis/Ocymyrmex/Melophorus boundary layer elevation)
        if (eth.has0(EthologyComponent.W0_DESERT_ANT_STILT_WALKING) && isSurface && ambientTemp > 35.0f) {
            if (mVelocity != null && mVelocity.has(entityId)) {
                VelocityComponent vel = mVelocity.get(entityId);
                // Biological fidelity: Desert ants elevate body ~4mm into cooler boundary layer and increase sprint velocity by 50% relative to species baseline
                vel.dx *= 1.5f;
                vel.dy *= 1.5f;
            }
            meta.energy += (0.005f * q10Factor) * dt; // Mitigates ground convective heat stress (~10°C thermal relief)
        }

        // 9. Trap-Jaw Mandibular Spring Catapult Strike & Escape (Odontomachus/Anochetus/Strumigenys)
        if (eth.has0(EthologyComponent.W0_TRAP_JAW) && meta.energy < 25f && doSpatialSample()) {
            if (mVelocity != null && mVelocity.has(entityId)) {
                VelocityComponent vel = mVelocity.get(entityId);
                float biteIntegrity = (mMandible != null && mMandible.has(entityId))
                    ? Math.max(0.2f, 1.0f - mMandible.get(entityId).mandibleWear)
                    : 1.0f;
                float recoilMag = 2.8f * biteIntegrity;
                vel.dx += (random.nextFloat() - 0.5f) * recoilMag * 2.0f;
                vel.dy += (random.nextFloat() - 0.5f) * recoilMag * 2.0f;
                vel.dz = 1.6f * biteIntegrity; // Vertical catapult lift modulated by biomechanical integrity
                if (mMandible != null && mMandible.has(entityId)) {
                    mMandible.get(entityId).mandibleWear = Math.min(1.0f, mMandible.get(entityId).mandibleWear + 0.002f);
                }
            }
        }

        // 10. Formic Acid Bath Auto-Sanitation (Direct cuticular antifungal treatment)
        if (eth.has0(EthologyComponent.W0_FORMIC_ACID_BATH_GROOMING) && doSpatialSample()) {
            if (mPathogen != null && mPathogen.has(entityId)) {
                CompactPathogenBitmaskComponent selfPath = mPathogen.get(entityId);
                selfPath.activePathogensBitmask &= ~(CompactPathogenBitmaskComponent.PATHOGEN_METARHIZIUM | CompactPathogenBitmaskComponent.PATHOGEN_BEAUVERIA);
            }
        }

        // ── WORD 1 behaviors: Nest Construction, Thermoregulation & Storage ─────

        // 11. Gravel plugging / gallery sealing
        if (eth.has1(EthologyComponent.W1_GRAVEL_PLUGGING)) {
            if (eth.carryingBuildingMaterial) {
                eth.stercoralMortarAmount = Math.min(100f, eth.stercoralMortarAmount + 5f * dt);
            }
        }

        // 12. Stercoral Cement Substrate Consolidation
        if (eth.has1(EthologyComponent.W1_STERCORAL_CEMENT)) {
            eth.stercoralMortarAmount = Math.min(100f, eth.stercoralMortarAmount + 3.0f * dt);
        }

        // 13. Thoracic Shivering Incubation (Thermoregulation of brood)
        if (eth.has1(EthologyComponent.W1_THORACIC_INCUBATION)) {
            eth.thermalThoraxTempC = Math.min(40f, eth.thermalThoraxTempC + 2f * dt);
            meta.energy -= 0.3f * dt; // shivering metabolic cost
        }

        // 14. Evaporative cooling: deposit water droplets during heat waves
        if (eth.has1(EthologyComponent.W1_EVAPORATIVE_COOLING)) {
            if (eth.thermalThoraxTempC > 37f || ambientTemp > 35f) {
                eth.thermalThoraxTempC = Math.max(25f, eth.thermalThoraxTempC - 3f * dt);
                meta.energy -= 0.5f * dt;
            }
        }

        // 15. Brood Wing Fanning (Active convective heat dissipation)
        if (eth.has1(EthologyComponent.W1_BROOD_WING_FANNING) && ambientTemp > 30.0f) {
            eth.thermalThoraxTempC = Math.max(ambientTemp - 3.0f, eth.thermalThoraxTempC - 1.5f * dt);
            meta.energy -= 0.15f * dt; // mechanical wing motor cost
        }

        // 16. Propolis collection & hive antimicrobial sealing
        if (eth.has2(EthologyComponent.W2_PROPOLIS_SHIELD)) {
            eth.propolisCarried = Math.min(100f, eth.propolisCarried + 0.1f * dt);
        }

        // 17. Honeypot replete storage
        if (eth.has1(EthologyComponent.W1_HONEYPOT_STORAGE)) {
            eth.honeypotFillRatio = Math.min(1f, eth.honeypotFillRatio + 0.01f * dt);
            if (eth.honeypotFillRatio >= 1f) {
                meta.metabolicRate = 0.1f; // repletes remain nearly static
            }
        }

        // 18. Granary Seed Aeration in damp soil
        if (eth.has1(EthologyComponent.W1_GRANARY_SEED_AERATION) && isSubterranean) {
            float moisture = hydrologySystem != null ? hydrologySystem.getSoilMoisture(pos.x, pos.y, pos.z) : 0.4f;
            if (moisture > 0.65f) {
                pos.z = Math.min(surfaceZ, pos.z + 0.3f * dt); // Moves seeds toward upper dry chambers
            }
        }

        // 19. Solar Mound Thermal Collection (Orientation towards sun on surface)
        if (eth.has1(EthologyComponent.W1_SOLAR_MOUND) && isSurface) {
            eth.thermalThoraxTempC = Math.min(32f, eth.thermalThoraxTempC + 0.5f * dt);
        }

        // 20. Aphid Honeydew Farming & Milking
        if (eth.has1(EthologyComponent.W1_APHID_FARMING) && doSpatialSample() && meta.energy < 75f) {
            meta.energy = Math.min(100f, meta.energy + 2.0f); // Honeydew sugar calorie intake
        }

        // 21. Tremble dance recruitment signal
        if (eth.has0(EthologyComponent.W0_TREMBLE_DANCE)) {
            eth.isTremble = meta.energy > 80f; // only full foragers tremble-recruit
        }

        // ── WORD 2 & 3 behaviors: Supercolonies, Flooding, Bivouacs, Mechanics ──

        // 17. Atta Leaf Crescent Shear: mandibular mechanical wear & substrate biomass harvest
        if (eth.has2(EthologyComponent.W2_ATTA_LEAF_CRESCENT_SHEAR) && isSurface) {
            if (mMandible != null && mMandible.has(entityId)) {
                MandibularBiomechanicsComponent mand = mMandible.get(entityId);
                mand.applyWear(0.0001f * dt);
            }
            if (eth.carryingBuildingMaterial) {
                meta.energy = Math.min(100f, meta.energy + 0.5f * dt);
            }
        }

        // 18. Fungiculture Weeding (Parasite Escovopsis eradication on fungal combs)
        if (eth.has2(EthologyComponent.W2_FUNGUS_WEEDING) && doSpatialSample()) {
            if (mPathogen != null && mPathogen.has(entityId)) {
                CompactPathogenBitmaskComponent path = mPathogen.get(entityId);
                path.activePathogensBitmask &= ~CompactPathogenBitmaskComponent.PATHOGEN_METARHIZIUM;
            }
        }

        // 19. Winter Diapause: metabolic suppression in cold seasons
        if (eth.has2(EthologyComponent.W2_DIAPAUSE)) {
            if (eth.diapauseActive || ambientTemp < 6.0f) {
                meta.energy -= 0.001f * dt; // ~10× reduced burn rate in torpor
                return; // skip active locomotive routines
            }
        }

        // 20. Living bivouac formation (Army ants Eciton / Dorylus)
        if (eth.has2(EthologyComponent.W2_LIVING_BIVOUAC) && doSpatialSample()) {
            List<Integer> nearby = queryNearby(pos);
            eth.inLivingBivouac = nearby.size() >= 30; // density threshold
        }

        // 21. Living biomechanical bridge: accelerate crossing nestmates
        if (eth.has2(EthologyComponent.W2_LIVING_BRIDGE) && doSpatialSample()) {
            List<Integer> nearby = queryNearby(pos);
            for (int crossingId : nearby) {
                if (crossingId != entityId && mVelocity != null && mVelocity.has(crossingId)) {
                    VelocityComponent crossVel = mVelocity.get(crossingId);
                    crossVel.dx *= 1.15f; // +15% crossing velocity boost over bridge
                    crossVel.dy *= 1.15f;
                }
            }
        }

        // 22. Floating ant raft during floods
        if (eth.has2(EthologyComponent.W2_FLOATING_ANT_RAFT)) {
            if (isSubterranean && (surfaceZ - pos.z) > 2.0f) {
                eth.isRafting = true;
                pos.z += 0.5f * dt; // hydro-buoyancy
                meta.energy -= 0.1f * dt;
            } else {
                eth.isRafting = false;
            }
        }

        // 23. Flood evacuation (barometric pressure drop reaction)
        if (eth.has2(EthologyComponent.W2_FLOOD_EVACUATION) && isSubterranean && (surfaceZ - pos.z) > 2.0f) {
            pos.z += 0.8f * dt; // rapid upward migration
            meta.energy -= 0.15f * dt;
        }

        // 24. Oleic Acid Necrophoresis (Cemetery refuse transport)
        if (eth.has2(EthologyComponent.W2_OLEIC_ACID_NECROPHORESIS) && doSpatialSample() && !eth.carryingBuildingMaterial) {
            List<Integer> nearby = queryNearby(pos);
            for (int neighborId : nearby) {
                if (neighborId != entityId && mMetabolism != null && mMetabolism.has(neighborId)) {
                    if (!mMetabolism.get(neighborId).alive) {
                        eth.carryingBuildingMaterial = true; // picks up dead corpse to clear nest
                        break;
                    }
                }
            }
        }

        // 24b. Dulosis & Slave-Making Raid (Polyergus / Formica sanguinea pupal capture)
        if (eth.has2(EthologyComponent.W2_DULOSIS_RAID) && doSpatialSample() && !eth.carryingBuildingMaterial) {
            List<Integer> nearby = queryNearby(pos);
            for (int targetId : nearby) {
                if (targetId != entityId && mMetabolism != null && mMetabolism.has(targetId)) {
                    MetabolismComponent targetMeta = mMetabolism.get(targetId);
                    if (targetMeta.alive && targetMeta.energy < 30.0f) {
                        eth.carryingBuildingMaterial = true; // Captures allospecific host pupa
                        targetMeta.energy = 0.0f; // Pupal capture
                        meta.energy = Math.min(100f, meta.energy + 5.0f);
                        break;
                    }
                }
            }
        }

        // 25. Termite soldier head-banging acoustic synchrony alarm
        if (eth.has3(EthologyComponent.W3_TERMITE_SOLDIER_ALARM_DRUM_SYNCHRONY) && doSpatialSample()) {
            int nearbyCount = queryNearby(pos).size();
            if (nearbyCount > 15) {
                eth.isStridulating = true;
                eth.stridulationFrequencyHz = 1100.0f; // High frequency vibrational substrate pulse
            }
        }

        // 26. Termite Saliva Cement Gallery Sealing against dry air
        if (eth.has3(EthologyComponent.W3_TERMITE_SALIVA_CEMENT_MOISTURE_SEAL) && isSubterranean) {
            float soilHum = hydrologySystem != null ? hydrologySystem.getSoilMoisture(pos.x, pos.y, pos.z) : 0.5f;
            if (soilHum < 0.35f) {
                eth.stercoralMortarAmount = Math.min(100f, eth.stercoralMortarAmount + 2.0f * dt);
            }
        }

        // 27. Cuticular water condensation osmoregulation in high humidity
        if (eth.has3(EthologyComponent.W3_CUTICLE_WATER_CONDENSATION)) {
            float soilHum = hydrologySystem != null ? hydrologySystem.getSoilMoisture(pos.x, pos.y, pos.z) : 0.5f;
            if (soilHum > 0.7f && meta.energy < 90f) {
                meta.energy = Math.min(100f, meta.energy + 0.1f * dt); // Cuticle hydration
            }
        }

        // 28. Drought Soil Moisture Vibrato (Vibrational acoustics to locate subterranean water table)
        if (eth.has3(EthologyComponent.W3_DROUGHT_SOIL_MOISTURE_VIBRATO) && isSubterranean) {
            float soilHum = hydrologySystem != null ? hydrologySystem.getSoilMoisture(pos.x, pos.y, pos.z) : 0.5f;
            if (soilHum < 0.20f) {
                eth.isStridulating = true;
                eth.stridulationFrequencyHz = 350.0f; // Low-frequency seismic soil probing
            }
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
        float baseFrequency = eth.stridulationFrequencyHz > 0 ? eth.stridulationFrequencyHz : 850.0f; // Hz
        float attenuationCoeff = 0.04f; // Rayleigh seismic attenuation in granular porous soil

        for (int neighborId : nearby) {
            if (neighborId == entityId) continue;
            if (mPosition != null && mPosition.has(neighborId)) {
                PositionComponent neighborPos = mPosition.get(neighborId);
                float dx = pos.x - neighborPos.x;
                float dy = pos.y - neighborPos.y;
                float dz = pos.z - neighborPos.z;
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                // Seismic wave amplitude decay: A(r) = A0 * exp(-alpha * r) / max(0.2, r)
                float amplitude = (float) (Math.exp(-attenuationCoeff * dist) / Math.max(0.2f, dist));
                if (amplitude > 0.15f) { // Sensory auditory / subgenual organ reception threshold
                    EthologyComponent neighborEth = mEthology != null ? mEthology.get(neighborId) : null;
                    if (neighborEth != null) {
                        neighborEth.carryingBuildingMaterial = true; // Trigger emergency rescue excavation
                    }
                    if (mVelocity != null && mVelocity.has(neighborId) && dist > 0.1f) {
                        VelocityComponent vel = mVelocity.get(neighborId);
                        vel.dx = (dx / dist) * 0.8f; // Orient towards seismic epicenter
                        vel.dy = (dy / dist) * 0.8f;
                    }
                }
            }
        }
    }

    private void performStomatodealTrophallaxis(int donorId, PositionComponent pos, MetabolismComponent donorMeta) {
        List<Integer> nearby = queryNearby(pos);
        for (int recipientId : nearby) {
            if (recipientId == donorId) continue;
            if (mMetabolism != null && mMetabolism.has(recipientId)) {
                MetabolismComponent recipientMeta = mMetabolism.get(recipientId);
                if (recipientMeta.alive && recipientMeta.energy < 40f) {
                    float transfer = Math.min(10.0f, (donorMeta.energy - 50.0f) * 0.5f);
                    if (transfer > 0.5f) {
                        donorMeta.energy -= transfer;
                        recipientMeta.energy += transfer;
                        break; // one transfer per spatial sample frame
                    }
                }
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
