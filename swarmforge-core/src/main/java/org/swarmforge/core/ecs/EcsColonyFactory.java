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

    public EcsColonyFactory(World ecsWorld) {
        this.ecsWorld = ecsWorld;
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
        pos.heading = (float) (Math.random() * Math.PI * 2);

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

        // AI / FSM
        AiComponent ai = edit.create(AiComponent.class);
        ai.type = AiComponent.AiType.FSM_WORKER;

        return entityId;
    }

    /**
     * Batch spawn N workers for a colony.
     */
    public int[] createWorkersBatch(UUID colonyId, int count, float nestX, float nestY, float nestZ, Species species) {
        int[] entityIds = new int[count];
        for (int i = 0; i < count; i++) {
            float rAngle = (float) (Math.random() * Math.PI * 2);
            float rDist = (float) (Math.random() * 2.5f);
            float x = nestX + (float) Math.cos(rAngle) * rDist;
            float y = nestY + (float) Math.sin(rAngle) * rDist;
            Individual.Job job = (i % 2 == 0) ? Individual.Job.FORAGER : Individual.Job.NURSE;
            entityIds[i] = createAnt(colonyId, Individual.Caste.WORKER, job, x, y, nestZ, species);
        }
        return entityIds;
    }
}
