package org.swarmforge.core.ecs.components;

import com.artemis.Component;
import java.util.Random;

/**
 * Component for tracking age, caste, and life stage.
 * Lifespan follows a normal (Gaussian) distribution centered around
 * the expected mean lifespan defined in the species/caste configuration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LifeCycleComponent extends Component {
    private static final Random RANDOM = new Random();

    public int ageTicks = 0;
    public int maxLifespan = 50000;
    
    public enum LifeStage { EGG, LARVA, PUPA, ADULT }
    public LifeStage stage = LifeStage.ADULT;
    
    public String casteName = "WORKER";

    /**
     * Initializes maxLifespan following a Gaussian (normal) distribution
     * around the specified mean lifespan (defined in species/caste settings).
     *
     * @param meanLifespanTicks Expected mean lifespan in simulation ticks
     * @param stdDevRatio Standard deviation ratio relative to mean (default 0.15 = 15%)
     */
    public void setGaussianLifespan(int meanLifespanTicks, double stdDevRatio) {
        if (meanLifespanTicks <= 0) {
            meanLifespanTicks = 50000;
        }
        double stdDev = Math.max(10.0, meanLifespanTicks * stdDevRatio);
        double sampled = RANDOM.nextGaussian() * stdDev + meanLifespanTicks;
        // Clamp to at least 20% of mean (min 100 ticks) so individuals don't die prematurely due to Gaussian tail outliers
        int minLimit = Math.max(100, (int) Math.round(meanLifespanTicks * 0.20));
        this.maxLifespan = Math.max(minLimit, (int) Math.round(sampled));
    }

    /**
     * Convenience method using standard 15% Gaussian dispersion around the mean lifespan.
     */
    public void setGaussianLifespan(int meanLifespanTicks) {
        setGaussianLifespan(meanLifespanTicks, 0.15);
    }
}

