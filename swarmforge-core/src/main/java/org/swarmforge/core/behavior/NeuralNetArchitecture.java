/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;


import org.swarmforge.core.simulation.SimulationContext;

import java.util.Random;

/**
 * Simple Neural Network implementation for ant behavior.
 * Uses a small feedforward network with backpropagation-like learning.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NeuralNetArchitecture implements ReasoningArchitecture {

    private static final int INPUT_SIZE = 12; // Sensor inputs
    private static final int HIDDEN_SIZE = 16; // Hidden layer
    private static final int OUTPUT_SIZE = 6; // Action outputs

    private final float[][] weightsInputHidden;
    private final float[][] weightsHiddenOutput;
    private final float[] hiddenBias;
    private final float[] outputBias;
    private final float learningRate;
    private final Random random = new Random();

    // Last hidden activations (for learning)
    private float[] lastHidden;
    private float[] lastOutputs;

    public NeuralNetArchitecture() {
        this(0.01f);
    }

    public NeuralNetArchitecture(float learningRate) {
        this.learningRate = learningRate;

        // Initialize weights with small random values
        weightsInputHidden = new float[INPUT_SIZE][HIDDEN_SIZE];
        weightsHiddenOutput = new float[HIDDEN_SIZE][OUTPUT_SIZE];
        hiddenBias = new float[HIDDEN_SIZE];
        outputBias = new float[OUTPUT_SIZE];

        initializeWeights();
    }

    private void initializeWeights() {
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                weightsInputHidden[i][j] = (random.nextFloat() - 0.5f) * 0.5f;
            }
        }
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsHiddenOutput[i][j] = (random.nextFloat() - 0.5f) * 0.5f;
            }
            hiddenBias[i] = (random.nextFloat() - 0.5f) * 0.1f;
        }
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            outputBias[i] = (random.nextFloat() - 0.5f) * 0.1f;
        }
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.NEURAL_NETWORK;
    }

    @Override
    public String getName() {
        return "Neural Network (Feedforward)";
    }

    @Override
    public void initialize(AgentView agent) {
        lastHidden = null;
        lastOutputs = null;
    }

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        // Build input vector from sensors
        float[] inputs = buildInputVector(agent, context);

        // Forward pass
        float[] hidden = new float[HIDDEN_SIZE];
        for (int j = 0; j < HIDDEN_SIZE; j++) {
            float sum = hiddenBias[j];
            for (int i = 0; i < INPUT_SIZE; i++) {
                sum += inputs[i] * weightsInputHidden[i][j];
            }
            hidden[j] = relu(sum);
        }
        lastHidden = hidden;

        float[] outputs = new float[OUTPUT_SIZE];
        for (int j = 0; j < OUTPUT_SIZE; j++) {
            float sum = outputBias[j];
            for (int i = 0; i < HIDDEN_SIZE; i++) {
                sum += hidden[i] * weightsHiddenOutput[i][j];
            }
            outputs[j] = sigmoid(sum);
        }
        lastOutputs = outputs;

        // Convert outputs to action
        return outputsToAction(outputs);
    }

    private float[] buildInputVector(AgentView agent, SimulationContext context) {
        float[] inputs = new float[INPUT_SIZE];

        // Normalized inputs
        inputs[0] = agent.getEnergyLevel();
        inputs[1] = agent.isCarryingFood() ? 1.0f : 0.0f;
        // inputs[2] = agent.getAge() / 10000f; // Age not in AgentView yet, use 0
        inputs[2] = 0;

        if (context != null) {
            inputs[3] = context.getFoodPheromone(agent.getX(), agent.getY(), agent.getZ());
            inputs[4] = context.getHomePheromone(agent.getX(), agent.getY(), agent.getZ());
            inputs[5] = context.hasEnemyNearby(agent) ? 1.0f : 0.0f;
            inputs[6] = context.getFoodPheromoneGradientX(agent.getX(), agent.getY(), agent.getZ())
                    + 0.5f;
            inputs[7] = context.getFoodPheromoneGradientY(agent.getX(), agent.getY(), agent.getZ())
                    + 0.5f;
        }

        // Distance from home (normalized)
        float dx = agent.getHomeX() - agent.getX();
        float dy = agent.getHomeY() - agent.getY();
        float distHome = (float) Math.sqrt(dx * dx + dy * dy);
        inputs[8] = Math.min(distHome / 100f, 1.0f);

        // Direction to home
        if (distHome > 0) {
            inputs[9] = (dx / distHome + 1) / 2; // Normalized 0-1
            inputs[10] = (dy / distHome + 1) / 2;
        }

        inputs[11] = agent.isSoldier() ? 1.0f : 0.0f;

        return inputs;
    }

    private Action outputsToAction(float[] outputs) {
        // outputs[0]: move forward strength
        // outputs[1]: turn left/right (-1 to 1)
        // outputs[2]: forage/return toggle
        // outputs[3]: attack
        // outputs[4]: rest
        // outputs[5]: explore

        // Find dominant action
        int maxIdx = 0;
        float maxVal = 0;
        for (int i = 2; i < OUTPUT_SIZE; i++) {
            if (outputs[i] > maxVal) {
                maxVal = outputs[i];
                maxIdx = i;
            }
        }

        return switch (maxIdx) {
            case 2 -> outputs[2] > 0.5f ? Action.returnHome() : Action.forage();
            case 3 -> Action.attack(null);
            case 4 -> Action.rest();
            default -> {
                float moveStrength = outputs[0];
                float turnAngle = (outputs[1] - 0.5f) * (float) Math.PI;
                yield Action.move((float) Math.cos(turnAngle) * moveStrength,
                        (float) Math.sin(turnAngle) * moveStrength, 0);
            }
        };
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        // Simple reinforcement: adjust weights based on reward
        if (lastHidden == null || lastOutputs == null)
            return;

        float reward = result.reward();

        // Simplified weight update (not full backprop)
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsHiddenOutput[i][j] += learningRate * reward * lastHidden[i] * lastOutputs[j];
            }
        }
    }

    @Override
    public void reset() {
        lastHidden = null;
        lastOutputs = null;
    }

    @Override
    public ReasoningArchitecture clone() {
        NeuralNetArchitecture copy = new NeuralNetArchitecture(learningRate);
        // Copy weights
        for (int i = 0; i < INPUT_SIZE; i++) {
            System.arraycopy(weightsInputHidden[i], 0, copy.weightsInputHidden[i], 0, HIDDEN_SIZE);
        }
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            System.arraycopy(weightsHiddenOutput[i], 0, copy.weightsHiddenOutput[i], 0, OUTPUT_SIZE);
        }
        System.arraycopy(hiddenBias, 0, copy.hiddenBias, 0, HIDDEN_SIZE);
        System.arraycopy(outputBias, 0, copy.outputBias, 0, OUTPUT_SIZE);
        return copy;
    }

    private float relu(float x) {
        return Math.max(0, x);
    }

    private float sigmoid(float x) {
        return 1.0f / (1.0f + (float) Math.exp(-x));
    }

    /**
     * Mutate weights for genetic evolution.
     */
    public void mutate(float mutationRate, float mutationStrength) {
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                if (random.nextFloat() < mutationRate) {
                    weightsInputHidden[i][j] += (random.nextFloat() - 0.5f) * mutationStrength;
                }
            }
        }
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (random.nextFloat() < mutationRate) {
                    weightsHiddenOutput[i][j] += (random.nextFloat() - 0.5f) * mutationStrength;
                }
            }
        }
    }
}
