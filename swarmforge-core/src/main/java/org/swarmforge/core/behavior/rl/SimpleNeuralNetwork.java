/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * Lightweight, high-performance pure-Java multi-layer perceptron (MLP) neural network.
 * Features:
 * - Forward inference with ReLU/LeakyReLU hidden activations & Softmax/Linear output.
 * - Backpropagation with analytical gradients.
 * - Adam optimizer with first and second moment tracking.
 * - Cross-entropy loss (for behavioral cloning / imitation learning) and MSE/Huber loss (for DQN).
 * - Polyak target network weight copying.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimpleNeuralNetwork implements Serializable {

    private static final long serialVersionUID = 1L;

    public final int[] layerSizes; // e.g. [16, 64, 64, 6]
    public final int numLayers;

    // Weights: layer l has shape [layerSizes[l+1]][layerSizes[l]]
    public final float[][][] weights;
    // Biases: layer l has shape [layerSizes[l+1]]
    public final float[][] biases;

    // Adam Optimizer moments
    private final float[][][] mWeights;
    private final float[][][] vWeights;
    private final float[][] mBiases;
    private final float[][] vBiases;
    private long adamStep = 0;

    // Hyperparameters
    public float learningRate = 0.001f;
    public float beta1 = 0.9f;
    public float beta2 = 0.999f;
    public float epsilon = 1e-8f;
    public float weightDecay = 1e-4f;

    // Transient activations for backpropagation
    private transient float[][] activations;
    private transient float[][] zValues;

    /**
     * Creates a new neural network with Xavier/Glorot initialization.
     *
     * @param layerSizes Layer dimensions (must have length >= 2)
     */
    public SimpleNeuralNetwork(int... layerSizes) {
        if (layerSizes.length < 2) {
            throw new IllegalArgumentException("Neural network must have at least input and output layers");
        }
        this.layerSizes = Arrays.copyOf(layerSizes, layerSizes.length);
        this.numLayers = layerSizes.length - 1;

        this.weights = new float[numLayers][][];
        this.biases = new float[numLayers][];
        this.mWeights = new float[numLayers][][];
        this.vWeights = new float[numLayers][][];
        this.mBiases = new float[numLayers][];
        this.vBiases = new float[numLayers][];

        Random rng = new Random(42);
        initLayers(rng);
        initTransientBuffers();
    }

    private void initLayers(Random rng) {
        for (int l = 0; l < numLayers; l++) {
            int inDim = layerSizes[l];
            int outDim = layerSizes[l + 1];

            weights[l] = new float[outDim][inDim];
            biases[l] = new float[outDim];
            mWeights[l] = new float[outDim][inDim];
            vWeights[l] = new float[outDim][inDim];
            mBiases[l] = new float[outDim];
            vBiases[l] = new float[outDim];

            // Xavier / He initialization
            float stdDev = (float) Math.sqrt(2.0 / inDim);
            for (int j = 0; j < outDim; j++) {
                for (int i = 0; i < inDim; i++) {
                    weights[l][j][i] = (float) (rng.nextGaussian() * stdDev);
                }
                biases[l][j] = 0.01f; // small positive bias
            }
        }
    }

    private void initTransientBuffers() {
        activations = new float[layerSizes.length][];
        zValues = new float[numLayers][];
        for (int l = 0; l < layerSizes.length; l++) {
            activations[l] = new float[layerSizes[l]];
            if (l < numLayers) {
                zValues[l] = new float[layerSizes[l + 1]];
            }
        }
    }

    /**
     * Performs a forward pass through the network.
     *
     * @param input Input vector of length layerSizes[0]
     * @return Output vector of length layerSizes[numLayers]
     */
    public synchronized float[] forward(float[] input) {
        if (activations == null) {
            initTransientBuffers();
        }
        if (input.length != layerSizes[0]) {
            throw new IllegalArgumentException("Input dimension mismatch. Expected: " + layerSizes[0] + ", got: " + input.length);
        }

        System.arraycopy(input, 0, activations[0], 0, input.length);

        for (int l = 0; l < numLayers; l++) {
            int inDim = layerSizes[l];
            int outDim = layerSizes[l + 1];
            float[] inAct = activations[l];
            float[] outZ = zValues[l];
            float[] outAct = activations[l + 1];
            float[][] w = weights[l];
            float[] b = biases[l];

            boolean isLastLayer = (l == numLayers - 1);

            for (int j = 0; j < outDim; j++) {
                float sum = b[j];
                float[] wj = w[j];
                for (int i = 0; i < inDim; i++) {
                    sum += wj[i] * inAct[i];
                }
                outZ[j] = sum;

                if (isLastLayer) {
                    outAct[j] = sum; // Linear output for Q-values / logits
                } else {
                    outAct[j] = (sum > 0) ? sum : 0.01f * sum; // LeakyReLU
                }
            }
        }

        return Arrays.copyOf(activations[numLayers], layerSizes[numLayers]);
    }

    /**
     * Compute Softmax probabilities on the output layer.
     */
    public float[] predictProbabilities(float[] input) {
        float[] logits = forward(input);
        return softmax(logits);
    }

    /**
     * Supervised learning step: Cross-Entropy loss on target class index (Imitation learning / Behavioral cloning).
     *
     * @param input       Input feature vector
     * @param targetClass Expected class index (0 <= targetClass < outputDim)
     * @return Cross-entropy loss value
     */
    public synchronized float trainImitationStep(float[] input, int targetClass) {
        float[] logits = forward(input);
        float[] probs = softmax(logits);

        float loss = (float) -Math.log(Math.max(probs[targetClass], 1e-7));

        // Gradient of Cross-Entropy with Softmax: dL/dz = probs - 1_target
        float[] dZ = Arrays.copyOf(probs, probs.length);
        dZ[targetClass] -= 1.0f;

        backward(dZ);
        return loss;
    }

    /**
     * Reinforcement Learning Q-learning step: MSE / Smooth L1 loss on Q(s, a).
     *
     * @param state       State observation vector s
     * @param action      Action index a
     * @param targetQ     Target Q-value y = r + gamma * max Q(s', a')
     * @return Absolute TD-error
     */
    public synchronized float trainQStep(float[] state, int action, float targetQ) {
        float[] qValues = forward(state);
        float currentQ = qValues[action];
        float tdError = currentQ - targetQ;

        // Clip TD error for gradient stability (Huber-like)
        float clippedError = Math.max(-1.0f, Math.min(1.0f, tdError));

        float[] dZ = new float[layerSizes[numLayers]];
        dZ[action] = clippedError;

        backward(dZ);
        return Math.abs(tdError);
    }

    /**
     * Backpropagation and Adam parameter updates.
     */
    private void backward(float[] outputGrad) {
        adamStep++;
        float biasCorrection1 = 1.0f - (float) Math.pow(beta1, adamStep);
        float biasCorrection2 = 1.0f - (float) Math.pow(beta2, adamStep);

        float[] currentDelta = outputGrad;

        for (int l = numLayers - 1; l >= 0; l--) {
            int inDim = layerSizes[l];
            int outDim = layerSizes[l + 1];
            float[] inAct = activations[l];
            float[][] w = weights[l];
            float[] b = biases[l];
            float[][] mw = mWeights[l];
            float[][] vw = vWeights[l];
            float[] mb = mBiases[l];
            float[] vb = vBiases[l];

            float[] nextDelta = (l > 0) ? new float[inDim] : null;

            for (int j = 0; j < outDim; j++) {
                float dj = currentDelta[j];

                // Update bias with Adam
                mb[j] = beta1 * mb[j] + (1 - beta1) * dj;
                vb[j] = beta2 * vb[j] + (1 - beta2) * dj * dj;
                float mHatB = mb[j] / biasCorrection1;
                float vHatB = vb[j] / biasCorrection2;
                b[j] -= learningRate * (mHatB / ((float) Math.sqrt(vHatB) + epsilon));

                float[] wj = w[j];
                float[] mwj = mw[j];
                float[] vwj = vw[j];

                for (int i = 0; i < inDim; i++) {
                    float gradW = dj * inAct[i] + weightDecay * wj[i];

                    // Update weight with Adam
                    mwj[i] = beta1 * mwj[i] + (1 - beta1) * gradW;
                    vwj[i] = beta2 * vwj[i] + (1 - beta2) * gradW * gradW;
                    float mHatW = mwj[i] / biasCorrection1;
                    float vHatW = vwj[i] / biasCorrection2;
                    wj[i] -= learningRate * (mHatW / ((float) Math.sqrt(vHatW) + epsilon));

                    if (nextDelta != null) {
                        nextDelta[i] += dj * wj[i];
                    }
                }
            }

            if (l > 0 && nextDelta != null) {
                // Apply derivative of LeakyReLU for hidden layer: f'(z) = 1 if z > 0 else 0.01
                float[] prevZ = zValues[l - 1];
                for (int i = 0; i < inDim; i++) {
                    nextDelta[i] *= (prevZ[i] > 0) ? 1.0f : 0.01f;
                }
                currentDelta = nextDelta;
            }
        }
    }

    /**
     * Polyak averaging weight copy: target = tau * source + (1 - tau) * target
     */
    public void copyWeightsFrom(SimpleNeuralNetwork source, float tau) {
        for (int l = 0; l < numLayers; l++) {
            int outDim = layerSizes[l + 1];
            int inDim = layerSizes[l];
            for (int j = 0; j < outDim; j++) {
                biases[l][j] = tau * source.biases[l][j] + (1.0f - tau) * biases[l][j];
                for (int i = 0; i < inDim; i++) {
                    weights[l][j][i] = tau * source.weights[l][j][i] + (1.0f - tau) * weights[l][j][i];
                }
            }
        }
    }

    public SimpleNeuralNetwork clone() {
        SimpleNeuralNetwork copy = new SimpleNeuralNetwork(this.layerSizes);
        copy.copyWeightsFrom(this, 1.0f);
        copy.learningRate = this.learningRate;
        return copy;
    }

    public static float[] softmax(float[] logits) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : logits) {
            if (v > max) max = v;
        }
        float sum = 0;
        float[] out = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            out[i] = (float) Math.exp(logits[i] - max);
            sum += out[i];
        }
        if (sum > 0) {
            for (int i = 0; i < logits.length; i++) {
                out[i] /= sum;
            }
        }
        return out;
    }

    public static int argmax(float[] array) {
        int bestIdx = 0;
        float maxVal = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                bestIdx = i;
            }
        }
        return bestIdx;
    }
}
