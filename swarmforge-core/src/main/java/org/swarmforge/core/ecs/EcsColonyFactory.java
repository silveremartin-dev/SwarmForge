package org.swarmforge.core.ecs;

import com.artemis.World;
import com.artemis.EntityEdit;
import org.swarmforge.core.ecs.components.*;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.Species;
import java.util.UUID;

/**
 * High-performance Factory for spawning ECS entities with complete micro-fidelity components.
 * Eliminates Java Heap object allocations during simulation ticks while preserving individual genetics,
 * CHC profiles, metabolism, and mandibular mechanics.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EcsColonyFactory {

    private final World ecsWorld;
    private java.util.Random random = new java.util.Random(1337L);

    public EcsColonyFactory(World ecsWorld) {
        this.ecsWorld = ecsWorld;
    }

    public void setRandom(java.util.Random random) {
        this.random = random != null ? random : new java.util.Random(1337L);
    }

    /**
     * Spawns an ant entity in the ECS world with full biological components.
     */
    public int createAnt(UUID colonyId, Individual.Caste caste, Individual.Job job,
                         float x, float y, float z, Species species) {
        int entityId = ecsWorld.create();
        EntityEdit edit = ecsWorld.edit(entityId);

        // Position & Velocity
        PositionComponent pos = edit.create(PositionComponent.class);
        pos.x = x;
        pos.y = y;
        pos.z = z;
        pos.heading = (float) (random.nextDouble() * Math.PI * 2);

        VelocityComponent vel = edit.create(VelocityComponent.class);
        vel.dx = 0.0f;
        vel.dy = 0.0f;
        vel.dz = 0.0f;

        // Colony & Caste Ownership
        ColonyComponent col = edit.create(ColonyComponent.class);
        col.colonyId = colonyId;

        // Biological Metabolism
        MetabolismComponent meta = edit.create(MetabolismComponent.class);
        meta.energy = 100.0f;
        meta.maxEnergy = 100.0f;
        meta.maxHealth = 100.0f;
        double healthGaussian = (caste == Individual.Caste.QUEEN) ? 100.0 : (94.0 + 5.0 * random.nextGaussian());
        meta.health = (float) Math.max(65.0, Math.min(100.0, healthGaussian));
        meta.hunger = 0.0f;
        meta.thirst = 0.0f;
        meta.alive = true;
        meta.metabolicRate = (caste == Individual.Caste.QUEEN) ? 2.2f :
                             (caste == Individual.Caste.SOLDIER ? 1.3f : 1.0f);

        // Genetics & Odor Profile
        GeneticsComponent gen = edit.create(GeneticsComponent.class);
        if (species != null) {
            gen.speedMultiplier = species.getWorkerSpeed();
            gen.metabolismRate = species.getMetabolism();
            gen.payloadRatio = species.getMaxCarryingPayloadRatio();
            gen.visionDistance = species.getViewDistance();
        }

        // Mandibular Biomechanics
        MandibularBiomechanicsComponent mand = edit.create(MandibularBiomechanicsComponent.class);
        mand.mandibleWear = 0.0f;
        mand.bitingForceMPa = (species != null) ? species.getMandibularBitingForceMPa() : 15.0f;

        // Inventory
        InventoryComponent inv = edit.create(InventoryComponent.class);
        inv.carriedItem = InventoryComponent.ItemType.NONE;

        // Ethology & Behavioral Capabilities
        EthologyComponent eth = edit.create(EthologyComponent.class);
        if (species != null) {
            eth.loadFromSpecies(species);
        }

        // Pathogen & Epidemiological State
        PathogenComponent path = edit.create(PathogenComponent.class);

        // Life Cycle & Caste Template
        LifeCycleComponent life = edit.create(LifeCycleComponent.class);
        life.casteName = (caste != null) ? caste.name() : "WORKER";
        float meanLifespanSeconds = 300.0f; // Default 300 seconds

        CasteTemplate casteTemplate = null;
        if (species != null && species.getCastes() != null) {
            for (CasteTemplate ct : species.getCastes()) {
                if (matchesCasteTemplate(ct, caste, life.casteName)) {
                    casteTemplate = ct;
                    break;
                }
            }
        }

        // Apply Caste Overrides to components if present
        if (casteTemplate != null) {
            if (casteTemplate.getBaseHealth() > 0) {
                meta.maxHealth = casteTemplate.getBaseHealth();
                meta.health = meta.maxHealth;
            }
            if (casteTemplate.getWalkSpeedMps() > 0) {
                vel.speed = casteTemplate.getWalkSpeedMps();
            }
            if (casteTemplate.getMandibularBitingForceMPa() > 0) {
                mand.bitingForceMPa = casteTemplate.getMandibularBitingForceMPa();
            }
            if (casteTemplate.getMaxCarryingPayloadRatio() > 0) {
                gen.payloadRatio = casteTemplate.getMaxCarryingPayloadRatio();
            }
        }

        if (species != null) {
            int rawVal = 300;
            if (casteTemplate != null && casteTemplate.getLifespan() > 0) {
                rawVal = casteTemplate.getLifespan();
            } else if (caste == Individual.Caste.QUEEN) {
                rawVal = species.getQueenLifespan();
            } else if (caste == Individual.Caste.SOLDIER) {
                rawVal = (int) Math.round(species.getWorkerLifespan() * 1.4);
            } else if (caste == Individual.Caste.MALE) {
                rawVal = (int) Math.round(species.getWorkerLifespan() * 0.35);
            } else {
                rawVal = species.getWorkerLifespan();
            }
            // Convert lifespan in days to simulation seconds (scale: 10s per sim-day, minimum 300s)
            meanLifespanSeconds = Math.max(300.0f, rawVal * 10.0f);
        }

        // Compute 100% deterministic seed based on colony UUID and entityId
        long entitySeed = (colonyId != null ? colonyId.getLeastSignificantBits() : 1337L) ^ ((long) entityId * 0x9E3779B97F4A7C15L);
        org.swarmforge.core.util.FastDeterministicRandom entityRng = new org.swarmforge.core.util.FastDeterministicRandom(entitySeed);
        life.setGaussianLifespan(meanLifespanSeconds, 0.10, entityRng);

        // AI / FSM
        AiComponent ai = edit.create(AiComponent.class);
        ai.type = AiComponent.AiType.FSM_WORKER;

        return entityId;
    }

    private static boolean matchesCasteTemplate(CasteTemplate ct, Individual.Caste caste, String casteName) {
        if (ct == null || ct.getName() == null) return false;
        String name = ct.getName().toLowerCase().trim();
        if (casteName != null && name.equalsIgnoreCase(casteName.trim())) return true;
        if (caste == null) return false;
        return switch (caste) {
            case WORKER -> name.contains("worker") || name.contains("ouvri") || name.contains("minor") || name.contains("media");
            case QUEEN -> name.contains("queen") || name.contains("rein") || name.contains("gyne");
            case SOLDIER -> name.contains("soldier") || name.contains("soldat") || name.contains("major") || name.contains("guard");
            case MALE -> name.contains("male") || name.contains("mâle") || name.contains("drone");
            default -> false;
        };
    }

    /**
     * Batch spawn N workers for a colony.
     */
    public int[] createWorkersBatch(UUID colonyId, int count, float nestX, float nestY, float nestZ, Species species) {
        int[] entityIds = new int[count];
        for (int i = 0; i < count; i++) {
            float rAngle = (float) (random.nextDouble() * Math.PI * 2);
            float rDist = random.nextFloat() * 2.5f;
            float x = nestX + (float) Math.cos(rAngle) * rDist;
            float y = nestY + (float) Math.sin(rAngle) * rDist;
            Individual.Job job = (i % 2 == 0) ? Individual.Job.FORAGER : Individual.Job.NURSE;
            entityIds[i] = createAnt(colonyId, Individual.Caste.WORKER, job, x, y, nestZ, species);
        }
        return entityIds;
    }
}
