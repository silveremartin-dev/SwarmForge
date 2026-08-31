package org.swarmforge.core.ecs.components;

import com.artemis.Component;
import org.swarmforge.core.util.FastDeterministicRandom;

/**
 * Component for tracking age, caste, and life stage in SECONDS (independent of tick rate).
 * Lifespan follows a 100% deterministic normal (Gaussian) distribution centered around
 * the expected mean lifespan defined in the species/caste configuration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LifeCycleComponent extends Component {

    public float ageSeconds = 0.0f;
    public float maxLifespanSeconds = 300.0f; // Default 300s (5 minutes)
    
    // Kept for backward-compatibility with tick queries
    public int getAgeTicks() { return (int) (ageSeconds * 60.0f); }
    public int getMaxLifespanTicks() { return (int) (maxLifespanSeconds * 60.0f); }

    public enum LifeStage { EGG, LARVA, PUPA, ADULT }
    public LifeStage stage = LifeStage.ADULT;
    
    public String casteName = "WORKER";

    /**
     * Initializes maxLifespanSeconds deterministically following a Gaussian (normal) distribution
     * around the specified mean lifespan in SECONDS.
     *
     * @param meanLifespanSeconds Expected mean lifespan in simulation seconds
     * @param stdDevRatio Standard deviation ratio relative to mean (default 0.15 = 15%)
     * @param random Deterministic PRNG instance (or null for deterministic hash seed)
     */
    public void setGaussianLifespan(float meanLifespanSeconds, double stdDevRatio, FastDeterministicRandom random) {
        if (meanLifespanSeconds <= 0.0f) {
            meanLifespanSeconds = 300.0f;
        }
        double stdDev = Math.max(1.0, meanLifespanSeconds * stdDevRatio);
        double gaussianValue = (random != null) ? random.nextGaussian() : 0.0;
        double sampled = meanLifespanSeconds + gaussianValue * stdDev;
        // Clamp to at least 20% of mean lifespan (min 5.0 seconds) to avoid premature death outliers
        float minLimit = Math.max(5.0f, meanLifespanSeconds * 0.20f);
        this.maxLifespanSeconds = Math.max(minLimit, (float) sampled);
    }

    /**
     * Deterministic Gaussian lifespan with standard 15% dispersion.
     */
    public void setGaussianLifespan(float meanLifespanSeconds, FastDeterministicRandom random) {
        setGaussianLifespan(meanLifespanSeconds, 0.15, random);
    }

    /**
     * Deterministic Gaussian lifespan using an entity-specific seed.
     */
    public void setGaussianLifespan(float meanLifespanSeconds, long seed) {
        FastDeterministicRandom rng = new FastDeterministicRandom(seed);
        setGaussianLifespan(meanLifespanSeconds, 0.15, rng);
    }
}

