/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.SimulationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages reinforcement learning episodes for 'Smart' agents.
 * This class overlays the standard simulation to enforce training scenarios
 * (e.g., reset position if taking too long, reward on success).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrainingManager {

    private static final Logger LOG = Logger.getLogger(TrainingManager.class.getName());

    private final List<Individual> trainees = new ArrayList<>();

    private int episodeCount = 0;
    private int maxStepsPerEpisode = 1000;
    private int currentEpisodeSteps = 0;

    // Metrics
    // Metrics
    private int successCount = 0;

    public TrainingManager(SimulationContext context) {
        // Context might be used later for more complex environmental queries
    }

    public void registerTrainee(Individual individual) {
        if (!trainees.contains(individual)) {
            trainees.add(individual);
        }
    }

    public void update() {
        currentEpisodeSteps++;

        boolean allFinished = true;

        // Check status of each trainee
        for (Individual agent : new ArrayList<>(trainees)) { // Copy to avoid concurrent mod
            if (!checkEpisodeStatus(agent)) {
                allFinished = false;
            }
        }

        // Force reset if max steps reached OR all agents finished
        if (currentEpisodeSteps >= maxStepsPerEpisode || (allFinished && !trainees.isEmpty())) {
            if (currentEpisodeSteps >= maxStepsPerEpisode) {
                LOG.info("Episode " + episodeCount + " timeout.");
            } else {
                LOG.info("Episode " + episodeCount + " finished early.");
            }
            resetAll();
        }
    }

    private boolean checkEpisodeStatus(Individual agent) {
        // Define Success Condition
        // Simple scenario: Agent finds food and returns home (or just finds food for
        // now)

        if (agent.isCarryingFood()) {
            // Success!
            // Reward is likely handled inside RLArchitecture upon action result,
            // but we can add an extra "Completion Bonus" here if we had access.

            successCount++;
            resetAgent(agent);
            return true;
        }

        // Check Failure/Death
        if (agent.getEnergy() <= 0) {
            resetAgent(agent); // Failed
            return true;
        }

        return false;
    }

    private void resetAll() {
        for (Individual agent : trainees) {
            resetAgent(agent);
        }
        episodeCount++;
        currentEpisodeSteps = 0;

        if (episodeCount % 10 == 0) {
            LOG.info(String.format("Training Stats: Episode %d | Success Rate: %.2f%%",
                    episodeCount, (successCount / (double) episodeCount) * 100.0));
        }
    }

    private void resetAgent(Individual agent) {
        // Reset position to Home (Nest)
        agent.setPosition(agent.getHomeX(), agent.getHomeY(), agent.getZ());

        // Reset State
        agent.setEnergy(100.0f); // Full energy
        agent.dropFood(); // Clear inventory

        // We should also notify the RL Architecture to reset its transient state
        // (lastState)
        if (agent.getReasoningArchitecture() instanceof RLArchitecture) {
            ((RLArchitecture) agent.getReasoningArchitecture()).initialize(agent);
        }
    }
}
