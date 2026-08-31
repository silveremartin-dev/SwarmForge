package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * ECS Component tracking mandibular wear, biting force (MPa),
 * and polyethism retirement triggers for excavators/foragers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class MandibularBiomechanicsComponent extends Component {
    public float mandibleWear = 0.0f; // 0.0 (fresh) to 1.0 (severely worn)
    public float bitingForceMPa = 15.0f;
    public boolean retiredToNurse = false;

    public void applyWear(float deltaWear) {
        this.mandibleWear = Math.min(1.0f, this.mandibleWear + deltaWear);
        if (this.mandibleWear >= 0.85f) {
            this.retiredToNurse = true;
        }
    }
}
