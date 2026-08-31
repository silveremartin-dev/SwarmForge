package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * ECS Component storing individual genetic traits, cuticular hydrocarbon (CHC) profile,
 * and species-level multipliers for unified micro/macro simulation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GeneticsComponent extends Component {
    public float speedMultiplier = 1.0f;
    public float metabolismRate = 1.0f;
    public float payloadRatio = 5.0f;
    public float visionDistance = 6.0f;
    
    // CHC Gestalt Odor Profile (Colony recognition signature)
    public float[] chcProfile = new float[8];
    
    public void setChcProfile(float[] profile) {
        if (profile != null && profile.length == 8) {
            System.arraycopy(profile, 0, this.chcProfile, 0, 8);
        }
    }
}
