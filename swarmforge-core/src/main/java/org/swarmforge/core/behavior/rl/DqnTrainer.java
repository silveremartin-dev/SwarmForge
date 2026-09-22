/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Double Deep Q-Network (Double-DQN) Trainer in pure Java.
 * Handles Experience Replay, TD Target calculation with target network, and epsilon-greedy exploration.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DqnTrainer {

    public record Transition(
            float[] state,
            int action,
            float reward,
            float[] nextState,
            boolean done
    ) {}

    private final SimpleNeuralNetwork policyNetwork;
    private final SimpleNeuralNetwork targetNetwork;
    private final List<Transition> replayBuffer;
    private final int bufferCapacity;
    private final Random rng = new Random(42);

    // Hyperparameters
    public float gamma = 0.95f;        // Discount factor
    public float epsilon = 1.0f;       // Initial exploration rate
    public float epsilonMin = 0.05f;   // Minimum exploration rate
    public float epsilonDecay = 0.999f;// Decay per step
    public float targetUpdateTau = 0.005f; // Soft target update rate
    public int batchSize = 32;

    private int bufferIndex = 0;
    private int bufferSize = 0;

    public DqnTrainer(SimpleNeuralNetwork policyNetwork, int bufferCapacity) {
        this.policyNetwork = policyNetwork;
        this.targetNetwork = policyNetwork.clone();
        this.bufferCapacity = bufferCapacity;
        this.replayBuffer = new ArrayList<>(bufferCapacity);
        for (int i = 0; i < bufferCapacity; i++) {
            replayBuffer.add(null);
        }
    }

    /**
     * Stores a new experience transition in the circular replay buffer.
     */
    public synchronized void recordTransition(float[] state, int action, float reward, float[] nextState, boolean done) {
        replayBuffer.set(bufferIndex, new Transition(state.clone(), action, reward, nextState.clone(), done));
        bufferIndex = (bufferIndex + 1) % bufferCapacity;
        if (bufferSize < bufferCapacity) {
            bufferSize++;
        }
    }

    /**
     * Selects an action using epsilon-greedy policy.
     *
     * @param state Observation vector
     * @return Chosen action index
     */
    public int selectAction(float[] state) {
        if (rng.nextFloat() < epsilon) {
            // Exploration
            return rng.nextInt(policyNetwork.layerSizes[policyNetwork.numLayers]);
        } else {
            // Exploitation
            float[] qValues = policyNetwork.forward(state);
            return SimpleNeuralNetwork.argmax(qValues);
        }
    }

    /**
     * Executes a minibatch training step using Double-DQN target formulation.
     *
     * @return Average loss / TD-error across the batch
     */
    public synchronized float trainStep() {
        if (bufferSize < batchSize) {
            return 0.0f;
        }

        float totalError = 0.0f;

        for (int b = 0; b < batchSize; b++) {
            int idx = rng.nextInt(bufferSize);
            Transition t = replayBuffer.get(idx);

            float targetQ;
            if (t.done()) {
                targetQ = t.reward();
            } else {
                // Double-DQN: Action selection via policyNetwork, Evaluation via targetNetwork
                float[] nextPolicyQ = policyNetwork.forward(t.nextState());
                int bestNextAction = SimpleNeuralNetwork.argmax(nextPolicyQ);

                float[] nextTargetQ = targetNetwork.forward(t.nextState());
                targetQ = t.reward() + gamma * nextTargetQ[bestNextAction];
            }

            float err = policyNetwork.trainQStep(t.state(), t.action(), targetQ);
            totalError += err;
        }

        // Soft target network update
        targetNetwork.copyWeightsFrom(policyNetwork, targetUpdateTau);

        // Decay epsilon
        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay;
            if (epsilon < epsilonMin) epsilon = epsilonMin;
        }

        return totalError / batchSize;
    }

    public SimpleNeuralNetwork getPolicyNetwork() {
        return policyNetwork;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
