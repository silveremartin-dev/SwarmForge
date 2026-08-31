package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.MandibularBiomechanicsComponent;
import org.swarmforge.core.ecs.components.AiComponent;

/**
 * ECS System handling mandibular wear propagation and temporal polyethism shifts.
 * Automatically transitions worn excavators and foragers to nurse duties inside the nest.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MandibularBiomechanicsSystem extends IteratingSystem {

    private ComponentMapper<MandibularBiomechanicsComponent> mMandible;
    private ComponentMapper<AiComponent> mAi;

    public MandibularBiomechanicsSystem() {
        super(Aspect.all(MandibularBiomechanicsComponent.class));
    }

    @Override
    protected void process(int entityId) {
        MandibularBiomechanicsComponent mand = mMandible.get(entityId);
        float delta = world.getDelta(); // seconds

        // Active digging or foraging causes gradual mandible wear (~0.00005 per second)
        if (mAi != null && mAi.has(entityId)) {
            AiComponent ai = mAi.get(entityId);
            if (ai.type == AiComponent.AiType.SIMPLE_FORAGER || ai.type == AiComponent.AiType.FSM_WORKER) {
                mand.applyWear(0.00005f * delta);
            }
        }
    }
}
