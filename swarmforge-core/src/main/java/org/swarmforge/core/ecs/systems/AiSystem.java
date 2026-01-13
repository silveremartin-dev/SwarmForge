package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.*;

/**
 * Central AI Brain System.
 * Dispatches logic based on AiComponent.type.
 */
public class AiSystem extends IteratingSystem {
    ComponentMapper<AiComponent> mAi;
    ComponentMapper<PositionComponent> mPos;
    ComponentMapper<VelocityComponent> mVel;
    ComponentMapper<InventoryComponent> mInv;
    ComponentMapper<MetabolismComponent> mMeta;

    // Sub-systems or Logic helpers could be injected here

    public AiSystem() {
        super(Aspect.all(AiComponent.class, PositionComponent.class, VelocityComponent.class));
    }

    @Override
    protected void process(int entityId) {
        AiComponent ai = mAi.get(entityId);
        
        // Optimize: Don't run full AI every tick
        ai.decisionTimer += world.getDelta();
        if (ai.decisionTimer < ai.decisionInterval) {
            return;
        }
        ai.decisionTimer = 0;

        if (mMeta.has(entityId) && !mMeta.get(entityId).alive) {
            mVel.get(entityId).speed = 0;
            return;
        }

        switch (ai.type) {
            case SIMPLE_FORAGER -> runSimpleForager(entityId);
            case RL_AGENT -> runRlAgent(entityId);
            case FSM_WORKER -> runFsmworker(entityId);
            case FUZZY_LOGIC -> runFuzzyLogic(entityId);
            case MANUAL -> { /* Do nothing, wait for external input */ }
        }
    }

    private void runSimpleForager(int entityId) {
        // Reduced version of ForagingSystem logic
        InventoryComponent inv = mInv.has(entityId) ? mInv.get(entityId) : null;
        VelocityComponent vel = mVel.get(entityId);
        PositionComponent pos = mPos.get(entityId);
        
        if (inv != null && inv.carriedItem == InventoryComponent.ItemType.FOOD) {
             // Go Home
            float dx = 50f - pos.x;
            float dz = 50f - pos.z;
            float dist = (float) Math.sqrt(dx*dx + dz*dz);
            if (dist < 1.0f) {
                inv.carriedItem = InventoryComponent.ItemType.NONE; // Drop
            } else {
                vel.dx = (dx / dist) * vel.speed;
                vel.dz = (dz / dist) * vel.speed;
            }
        } else {
            // Random Walk
            double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
            vel.dx = (float) Math.cos(angle) * vel.speed;
            vel.dz = (float) Math.sin(angle) * vel.speed;
        }
    }

    // AI Logic Instances (Ideally cached or pooled if stateful)
    // Since RLArchitecture has per-entity state (lastAction), we need one per entity.
    // Ideally stored in AiComponent. For now, we use a map or simplified approach.
    // Hack for prototype: We instantiate a new one if not present, but we lose state between ticks if not careful.
    // Solution: AiComponent should hold the instance.
    
    private final EcsAgentAdapter agentAdapter = new EcsAgentAdapter();

    @Override
    protected void initialize() {
        // Inject Mappers into Adapter once
        agentAdapter.mPos = mPos;
        agentAdapter.mVel = mVel;
        agentAdapter.mInv = mInv;
        agentAdapter.mMeta = mMeta;
    }

    private void runRlAgent(int entityId) {
        AiComponent ai = mAi.get(entityId);
        
        // 1. Get or Create Brain
        // We assume AiComponent has a generic 'Object statePayload' or we cast/add field
        if (ai.brainInstance == null) {
            ai.brainInstance = new org.swarmforge.core.behavior.rl.RLArchitecture();
            ((org.swarmforge.core.behavior.rl.RLArchitecture)ai.brainInstance).initialize(null); // Init
        }
        
        org.swarmforge.core.behavior.rl.RLArchitecture brain = (org.swarmforge.core.behavior.rl.RLArchitecture) ai.brainInstance;
        
        // 2. Prepare Adapter
        agentAdapter.setEntityId(entityId);
        
        // 3. Decide
        // Context is null for now, need to implement ECS SimulationContext later
        org.swarmforge.core.behavior.ReasoningArchitecture.Action action = brain.decide(agentAdapter, null);
        
        // 4. Apply Action (Convert back to Components)
        applyAction(entityId, action);
    }
    
    private void applyAction(int entityId, org.swarmforge.core.behavior.ReasoningArchitecture.Action action) {
        VelocityComponent vel = mVel.get(entityId);
        
        switch (action.type()) {
            case MOVE -> {
                vel.dx = action.directionX() * action.intensity();
                vel.dy = action.directionY() * action.intensity();
                vel.dz = action.directionZ() * action.intensity();
                // Update heading? PositionComponent has heading.
                // pos.heading = ... 
            }
            case FORAGE -> {
                // Simplified instant pickup for now
                 InventoryComponent inv = mInv.get(entityId);
                 inv.carriedItem = InventoryComponent.ItemType.FOOD;
            }
            case REST -> {
                vel.dx = 0;
                vel.dy = 0;
            }
            case RETURN_HOME -> {
                // Simplified: head to origin
                PositionComponent pos = mPos.get(entityId);
                float dx = -pos.x;
                float dz = -pos.z;
                float dist = (float) Math.sqrt(dx*dx + dz*dz);
                if (dist > 0) {
                    vel.dx = (dx / dist) * vel.speed;
                    vel.dz = (dz / dist) * vel.speed;
                }
            }
            case DEPOSIT_FOOD -> {
                InventoryComponent inv = mInv.get(entityId);
                inv.carriedItem = InventoryComponent.ItemType.NONE;
            }
            case EXPLORE -> {
                // Random walk
                double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                vel.dx = (float) Math.cos(angle) * vel.speed;
                vel.dz = (float) Math.sin(angle) * vel.speed;
            }
            case NURSE, DEPOSIT_PHEROMONE, FLEE, COMMUNICATE, FOLLOW_TRAIL, GROOM, ATTACK -> {
                // Placeholder for more complex actions
            }
        }
    }

    private void runFsmworker(int entityId) {
        AiComponent ai = mAi.get(entityId);
        if (ai.brainInstance == null) {
            ai.brainInstance = new org.swarmforge.core.behavior.FSMArchitecture();
            ((org.swarmforge.core.behavior.FSMArchitecture) ai.brainInstance).initialize(null);
        }

        org.swarmforge.core.behavior.FSMArchitecture brain = (org.swarmforge.core.behavior.FSMArchitecture) ai.brainInstance;
        agentAdapter.setEntityId(entityId);
        org.swarmforge.core.behavior.ReasoningArchitecture.Action action = brain.decide(agentAdapter, null);
        applyAction(entityId, action);
    }

    private void runFuzzyLogic(int entityId) {
        AiComponent ai = mAi.get(entityId);
        if (ai.brainInstance == null) {
            ai.brainInstance = new org.swarmforge.core.behavior.FuzzyLogicArchitecture();
            ((org.swarmforge.core.behavior.FuzzyLogicArchitecture)ai.brainInstance).initialize(null);
        }
        
        org.swarmforge.core.behavior.FuzzyLogicArchitecture brain = (org.swarmforge.core.behavior.FuzzyLogicArchitecture) ai.brainInstance;
        agentAdapter.setEntityId(entityId);
        org.swarmforge.core.behavior.ReasoningArchitecture.Action action = brain.decide(agentAdapter, null);
        applyAction(entityId, action);
    }
}
