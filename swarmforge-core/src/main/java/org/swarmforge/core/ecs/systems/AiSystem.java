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
    ComponentMapper<ColonyComponent> mColony;
    ComponentMapper<LifeCycleComponent> mLife;

    // Sub-systems or Logic helpers could be injected here

    public AiSystem() {
        super(Aspect.all(AiComponent.class, PositionComponent.class, VelocityComponent.class, ColonyComponent.class));
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
            float hx = 50f, hy = 50f, hz = 0f;
            if (mColony != null && mColony.has(entityId)) {
                var col = org.swarmforge.core.ecs.ColonyRegistry.getColony(mColony.get(entityId).colonyId);
                if (col != null) {
                    hx = col.getNestX();
                    hy = col.getNestY();
                    hz = col.getNestZ();
                }
            }
            float dx = hx - pos.x;
            float dy = hy - pos.y;
            float dz = hz - pos.z;
            float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 1.0f) {
                inv.carriedItem = InventoryComponent.ItemType.NONE; // Drop
                vel.dx = 0;
                vel.dy = 0;
                vel.dz = 0;
            } else {
                vel.dx = (dx / dist) * vel.speed;
                vel.dy = (dy / dist) * vel.speed;
                vel.dz = (dz / dist) * vel.speed;
            }
        } else {
            // Random Walk on X/Y plane
            double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
            vel.dx = (float) Math.cos(angle) * vel.speed;
            vel.dy = (float) Math.sin(angle) * vel.speed;
            vel.dz = 0;
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
        agentAdapter.mColony = mColony;
        agentAdapter.mLife = mLife;
    }

    private void runRlAgent(int entityId) {
        AiComponent ai = mAi.get(entityId);
        
        // 1. Get or Create Brain
        if (ai.brainInstance == null || !(ai.brainInstance instanceof org.swarmforge.core.behavior.rl.RLArchitecture)) {
            ai.brainInstance = new org.swarmforge.core.behavior.rl.RLArchitecture();
            ((org.swarmforge.core.behavior.rl.RLArchitecture) ai.brainInstance).initialize(null); // Init
        }
        
        org.swarmforge.core.behavior.rl.RLArchitecture brain = (org.swarmforge.core.behavior.rl.RLArchitecture) ai.brainInstance;
        
        // 2. Prepare Adapter
        agentAdapter.setEntityId(entityId);
        
        // 3. Decide
        org.swarmforge.core.behavior.ReasoningArchitecture.Action action = brain.decide(agentAdapter, null);
        
        // 4. Apply Action
        applyAction(entityId, action);
    }
    
    private void applyAction(int entityId, org.swarmforge.core.behavior.ReasoningArchitecture.Action action) {
        VelocityComponent vel = mVel.get(entityId);
        
        switch (action.type()) {
            case MOVE -> {
                vel.dx = action.directionX() * action.intensity();
                vel.dy = action.directionY() * action.intensity();
                vel.dz = action.directionZ() * action.intensity();
            }
            case FORAGE -> {
                 InventoryComponent inv = mInv.get(entityId);
                 inv.carriedItem = InventoryComponent.ItemType.FOOD;
            }
            case REST -> {
                vel.dx = 0;
                vel.dy = 0;
            }
            case RETURN_HOME -> {
                PositionComponent pos = mPos.get(entityId);
                float hx = 50f, hy = 50f, hz = 0f;
                if (mColony != null && mColony.has(entityId)) {
                    var col = org.swarmforge.core.ecs.ColonyRegistry.getColony(mColony.get(entityId).colonyId);
                    if (col != null) {
                        hx = col.getNestX();
                        hy = col.getNestY();
                        hz = col.getNestZ();
                    }
                }
                float dx = hx - pos.x;
                float dy = hy - pos.y;
                float dz = hz - pos.z;
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 0.01f) {
                    vel.dx = (dx / dist) * vel.speed;
                    vel.dy = (dy / dist) * vel.speed;
                    vel.dz = (dz / dist) * vel.speed;
                }
            }
            case DEPOSIT_FOOD -> {
                InventoryComponent inv = mInv.get(entityId);
                inv.carriedItem = InventoryComponent.ItemType.NONE;
                java.util.UUID colonyId = mColony.get(entityId).colonyId;
                org.swarmforge.core.domain.Colony colony = org.swarmforge.core.ecs.ColonyRegistry.getColony(colonyId);
                if (colony != null) {
                    colony.addResource(org.swarmforge.core.domain.ResourceType.SEED, 1.0f);
                }
            }
            case EXPLORE -> {
                // Random walk on X/Y ground plane
                double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                vel.dx = (float) Math.cos(angle) * vel.speed;
                vel.dy = (float) Math.sin(angle) * vel.speed;
                vel.dz = 0;
            }
            case NURSE, DEPOSIT_PHEROMONE, FLEE, COMMUNICATE, FOLLOW_TRAIL, GROOM, ATTACK -> {
                // Placeholder for more complex actions
            }
        }
    }

    private void runFsmworker(int entityId) {
        AiComponent ai = mAi.get(entityId);
        if (ai.brainInstance == null || !(ai.brainInstance instanceof org.swarmforge.core.behavior.FSMArchitecture)) {
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
        if (ai.brainInstance == null || !(ai.brainInstance instanceof org.swarmforge.core.behavior.FuzzyLogicArchitecture)) {
            ai.brainInstance = new org.swarmforge.core.behavior.FuzzyLogicArchitecture();
            ((org.swarmforge.core.behavior.FuzzyLogicArchitecture) ai.brainInstance).initialize(null);
        }
        org.swarmforge.core.behavior.FuzzyLogicArchitecture brain = (org.swarmforge.core.behavior.FuzzyLogicArchitecture) ai.brainInstance;
        agentAdapter.setEntityId(entityId);
        org.swarmforge.core.behavior.ReasoningArchitecture.Action action = brain.decide(agentAdapter, null);
        applyAction(entityId, action);
    }
}
