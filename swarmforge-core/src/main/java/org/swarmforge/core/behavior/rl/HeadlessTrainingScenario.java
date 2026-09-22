/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.behavior.FSMArchitecture;
import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.simulation.SimulationContext;
import org.swarmforge.core.species.CustomSpecies;
import org.swarmforge.core.species.Species;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Advanced Headless micro-simulation harness for pure-Java multi-caste, multi-species training.
 * Trains a universal 24-input, 14-output species neural brain:
 * 1. Comprehensive Behavioral Cloning across all 5 castes (Queen, Nurse, Worker, Soldier, Drone)
 *    and 4 insect families (Formicidae, Apidae, Vespidae, Isoptera).
 * 2. Closed-Loop Double-DQN Multi-Task Fine-Tuning.
 * 3. Direct ONNX export to standard .onnx format.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HeadlessTrainingScenario {

    private final UUID colonyId = UUID.randomUUID();
    private final List<Individual> colonyMembers = new ArrayList<>();
    private final SimpleNeuralNetwork network;
    private final DqnTrainer dqnTrainer;
    private final MockSimulationContext context;
    private final Random rng = new Random(42);

    private float lastImitationLoss = 1.0f;
    private float averageReward = 0.0f;

    public HeadlessTrainingScenario() {
        // [24 inputs -> 64 -> 64 -> 14 outputs]
        this.network = new SimpleNeuralNetwork(
                OnnxBrainArchitecture.OBSERVATION_DIM,
                64,
                64,
                OnnxBrainArchitecture.NUM_ACTIONS
        );
        this.network.learningRate = 0.002f;
        this.dqnTrainer = new DqnTrainer(network, 20_000);
        this.dqnTrainer.epsilon = 0.4f;
        this.dqnTrainer.epsilonMin = 0.02f;
        this.dqnTrainer.batchSize = 32;
        this.context = new MockSimulationContext();
    }

    /**
     * Initializes the micro-colony with mixed architectures.
     */
    public void setupColony(int numQueens, int numFsmWorkers, int numLearners) {
        colonyMembers.clear();

        // 1. Stable Queens with FSM
        for (int i = 0; i < numQueens; i++) {
            Individual queen = new Individual(colonyId, Individual.Caste.QUEEN, 0, 0, 0);
            queen.setHomePosition(0, 0, 0);
            queen.setBrain(new FSMArchitecture());
            colonyMembers.add(queen);
        }

        // 2. Stable FSM Workers
        for (int i = 0; i < numFsmWorkers; i++) {
            Individual worker = new Individual(colonyId, Individual.Caste.WORKER, 0, 0, 0);
            worker.setHomePosition(0, 0, 0);
            worker.setBrain(new FSMArchitecture());
            colonyMembers.add(worker);
        }

        // 3. Learning Ants
        for (int i = 0; i < numLearners; i++) {
            Individual learner = new Individual(colonyId, Individual.Caste.WORKER, 0, 0, 0);
            learner.setHomePosition(0, 0, 0);
            colonyMembers.add(learner);
        }
    }

    /**
     * Phase 1: Comprehensive Multi-Caste & Multi-Species Behavioral Cloning.
     *
     * @param steps Number of imitation training steps
     */
    public void runImitationPhase(int steps) {
        float totalLoss = 0.0f;
        int count = 0;

        for (int s = 0; s < steps; s++) {
            MockAgentView mockAgent = generateSyntheticAgentState();
            int expertAction = computeMultiCasteExpertAction(mockAgent, context);

            float[] obs = OnnxBrainArchitecture.buildObservationVector(mockAgent, context);
            float loss = network.trainImitationStep(obs, expertAction);

            totalLoss += loss;
            count++;
        }

        this.lastImitationLoss = (count > 0) ? (totalLoss / count) : 0.0f;
    }

    /**
     * Phase 2: Closed-Loop Multi-Caste Double-DQN Fine-Tuning.
     * Simulates agent exploration across all castes while maintaining multi-task stability.
     *
     * @param steps Number of simulation ticks
     */
    public void runDqnPhase(int steps) {
        Individual.Caste[] castes = {Individual.Caste.QUEEN, Individual.Caste.SOLDIER, Individual.Caste.NURSE, Individual.Caste.WORKER};
        List<Individual> learners = new ArrayList<>();
        for (Individual.Caste c : castes) {
            Individual ind = new Individual(colonyId, c, 0, 0, 0);
            ind.setHomePosition(0, 0, 0);
            learners.add(ind);
        }

        float totalReward = 0.0f;
        int rewardCount = 0;

        for (int t = 0; t < steps; t++) {
            Individual learner = learners.get(t % learners.size());

            float[] state = OnnxBrainArchitecture.buildObservationVector(learner, context);
            int actionIdx = dqnTrainer.selectAction(state);

            float reward = executeStep(learner, actionIdx);
            totalReward += reward;
            rewardCount++;

            float[] nextState = OnnxBrainArchitecture.buildObservationVector(learner, context);
            boolean done = !learner.isAlive() || (learner.getEnergy() <= 1.0f);

            dqnTrainer.recordTransition(state, actionIdx, reward, nextState, done);
            dqnTrainer.trainStep();

            // Multi-task imitation regularization to prevent catastrophic forgetting
            if (t % 2 == 0) {
                MockAgentView regAgent = generateSyntheticAgentState();
                int expAct = computeMultiCasteExpertAction(regAgent, context);
                float[] regObs = OnnxBrainArchitecture.buildObservationVector(regAgent, context);
                network.trainImitationStep(regObs, expAct);
            }

            if (done) {
                learner.setEnergy(100.0f);
                learner.setPosition(0, 0, 0);
            }
        }

        this.averageReward = (rewardCount > 0) ? (totalReward / rewardCount) : 0.0f;
    }

    private float executeStep(Individual agent, int actionIdx) {
        float reward = -0.05f; // Baseline metabolic cost
        float speed = 1.0f;
        float h = agent.getHeading();

        switch (actionIdx) {
            case 0 -> { // MOVE_FORWARD
                float nx = agent.getX() + (float) Math.cos(h) * speed;
                float ny = agent.getY() + (float) Math.sin(h) * speed;
                agent.setPosition(nx, ny, agent.getZ());
                if (agent.isCarryingFood()) {
                    float distToNest = (float) Math.hypot(nx, ny);
                    float prevDist = (float) Math.hypot(agent.getX(), agent.getY());
                    if (distToNest < prevDist) reward += 0.5f;
                }
            }
            case 1 -> agent.setHeading(h + 0.4f); // TURN_LEFT
            case 2 -> agent.setHeading(h - 0.4f); // TURN_RIGHT
            case 3 -> { // FORAGE / PICK_UP
                if (!agent.isCarryingFood() && context.hasFoodNearby(agent)) {
                    agent.setCarriedItem(Individual.CarriedItem.FOOD);
                    reward += 50.0f;
                } else {
                    reward -= 0.5f;
                }
            }
            case 4 -> { // DEPOSIT_FOOD
                if (agent.isCarryingFood() && agent.isAtNest()) {
                    agent.setCarriedItem(Individual.CarriedItem.NONE);
                    reward += 100.0f;
                } else {
                    reward -= 0.5f;
                }
            }
            case 5 -> { // RETURN_HOME
                if (agent.isCarryingFood() || agent.getEnergyLevel() < 0.2f) {
                    reward += 5.0f;
                }
            }
            case 6 -> { // ATTACK
                if (context.hasEnemyNearby(agent) || context.getAlarmPheromone(agent.getX(), agent.getY(), agent.getZ()) > 0.3f) {
                    reward += agent.isSoldier() ? 50.0f : 10.0f;
                } else {
                    reward -= 2.0f; // Penalize false attacks
                }
            }
            case 7 -> { // DEFEND_PATROL
                if (agent.isSoldier() && agent.isAtNest()) {
                    reward += 15.0f;
                }
            }
            case 8, 10 -> { // TEND_BROOD / NURSE
                if (agent.isNurse() && agent.isAtNest()) {
                    reward += 30.0f;
                }
            }
            case 9 -> { // LAY_EGG
                if (agent.isQueen() && agent.isAtNest() && agent.getEnergyLevel() > 0.4f) {
                    reward += 100.0f;
                } else {
                    reward -= 5.0f;
                }
            }
            case 11 -> { // GROOM / TROPHALLAXIS
                if (agent.isAtNest()) {
                    reward += 5.0f;
                }
            }
            case 12 -> { // DEPOSIT_PHEROMONE
                if (agent.isCarryingFood()) {
                    reward += 10.0f;
                }
            }
            case 13 -> { // REST
                if (agent.getEnergyLevel() < 0.3f && agent.isAtNest()) {
                    agent.setEnergy(Math.min(100.0f, agent.getEnergy() + 10.0f));
                    reward += 5.0f;
                }
            }
        }

        agent.consumeEnergy(0.1f);
        return reward;
    }

    private MockAgentView generateSyntheticAgentState() {
        MockAgentView view = new MockAgentView();
        view.energy = 0.3f + rng.nextFloat() * 0.7f;

        // Balanced caste selection
        Individual.Caste[] castes = {Individual.Caste.QUEEN, Individual.Caste.SOLDIER, Individual.Caste.NURSE, Individual.Caste.WORKER};
        view.caste = castes[rng.nextInt(castes.length)];

        // Insect order distribution
        String[] insectTypes = {"ANT", "BEE", "WASP", "TERMITE"};
        view.insectType = insectTypes[rng.nextInt(insectTypes.length)];

        view.atNest = rng.nextBoolean();
        view.carryingFood = (view.caste == Individual.Caste.WORKER || view.caste == Individual.Caste.NURSE) && rng.nextBoolean();
        view.hasFoodNearby = (view.caste == Individual.Caste.WORKER) && !view.carryingFood && rng.nextBoolean();
        view.hasEnemyNearby = (view.caste == Individual.Caste.SOLDIER) && rng.nextBoolean();
        view.alarmPheromone = view.hasEnemyNearby ? (0.6f + rng.nextFloat() * 0.4f) : 0.0f;

        view.heading = (float) (rng.nextFloat() * 2 * Math.PI);
        view.x = view.atNest ? 0f : (rng.nextFloat() - 0.5f) * 30.0f;
        view.y = view.atNest ? 0f : (rng.nextFloat() - 0.5f) * 30.0f;

        return view;
    }

    /**
     * Multi-Caste & Multi-Species Biological Expert Oracle.
     */
    public static int computeMultiCasteExpertAction(AgentView agent, SimulationContext ctx) {
        boolean atNest = agent.isAtNest();
        boolean hasFood = ctx != null && ctx.hasFoodNearby(agent);
        boolean hasEnemy = ctx != null && ctx.hasEnemyNearby(agent);
        float alarm = (ctx != null) ? ctx.getAlarmPheromone(agent.getX(), agent.getY(), agent.getZ()) : 0.0f;

        // 1. QUEEN BEHAVIOR
        if (agent.isQueen()) {
            if (!atNest) {
                return 5; // RETURN_HOME
            }
            if (agent.getEnergyLevel() > 0.30f) {
                return 9; // LAY_EGG (Royal Chamber Oviposition)
            }
            return 13; // REST
        }

        // 2. SOLDIER BEHAVIOR
        if (agent.isSoldier() || agent.isMajor()) {
            if (hasEnemy || alarm > 0.20f) {
                return 6; // ATTACK
            }
            if (atNest) {
                return 7; // DEFEND_PATROL
            }
            return 0; // MOVE_FORWARD (Patrol perimeter)
        }

        // 3. NURSE BEHAVIOR
        if (agent.isNurse()) {
            if (!atNest) {
                return 5; // RETURN_HOME to nursery
            }
            return 8; // TEND_BROOD (Care for larvae/eggs)
        }

        // 4. WORKER / FORAGER BEHAVIOR
        if (agent.isCarryingFood()) {
            if (atNest) {
                return 4; // DEPOSIT_FOOD (Store in granary/comb)
            }
            return 5; // RETURN_HOME (Navigate back to nest)
        } else {
            if (hasFood) {
                return 3; // FORAGE (Pick up food source)
            }
            if (atNest && agent.getEnergyLevel() < 0.25f) {
                return 13; // REST
            }
            // Navigate along chemical gradient
            if (ctx != null) {
                float gx = ctx.getFoodPheromoneGradientX(agent.getX(), agent.getY(), agent.getZ());
                float gy = ctx.getFoodPheromoneGradientY(agent.getX(), agent.getY(), agent.getZ());
                float heading = agent.getHeading();
                float lateral = (float) (-gx * Math.sin(heading) + gy * Math.cos(heading));
                if (lateral > 0.2f) return 1; // TURN_LEFT
                if (lateral < -0.2f) return 2; // TURN_RIGHT
            }
            return 0; // MOVE_FORWARD (Explore for resources)
        }
    }

    /**
     * Exports the trained neural network directly to a standard binary .onnx file.
     */
    public void exportToOnnx(File outputFile) throws IOException {
        OnnxModelSerializer.serializeToFile(network, "species_brain", outputFile);
    }

    public Individual getSampleLearner() {
        return colonyMembers.isEmpty() ? null : colonyMembers.get(colonyMembers.size() - 1);
    }

    public SimpleNeuralNetwork getNeuralNetwork() {
        return network;
    }

    public SimulationContext getContext() {
        return context;
    }

    public float getImitationLoss() {
        return lastImitationLoss;
    }

    public float getAverageReward() {
        return averageReward;
    }

    /**
     * Minimal synthetic SimulationContext for headless training.
     */
    public static class MockSimulationContext implements SimulationContext {
        public float foodGradX = 0.2f;
        public float foodGradY = 0.1f;
        public float alarmIntensity = 0.0f;
        public boolean enemyNearby = false;
        public boolean foodNearby = false;

        @Override public float getFoodPheromone(float x, float y, float z) { return 0.5f; }
        @Override public float getHomePheromone(float x, float y, float z) { return 0.5f; }
        @Override public float getAlarmPheromone(float x, float y, float z) { return alarmIntensity; }
        @Override public float getFoodPheromoneGradientX(float x, float y, float z) { return foodGradX; }
        @Override public float getFoodPheromoneGradientY(float x, float y, float z) { return foodGradY; }
        @Override public boolean hasEnemyNearby(AgentView agent) { return enemyNearby; }
        @Override public Individual getNearestEnemy(AgentView agent) { return null; }
        @Override public boolean hasFoodNearby(AgentView agent) { return foodNearby || Math.hypot(agent.getX() - 10.0, agent.getY() - 10.0) < 3.0; }
        @Override public float[] getNearestFoodPosition(AgentView agent) { return new float[]{10f, 10f, 0f}; }
        @Override public boolean hasFoodNearby(AgentView agent, Set<ResourceType> types) { return hasFoodNearby(agent); }
        @Override public float[] getNearestFoodPosition(AgentView agent, Set<ResourceType> types) { return getNearestFoodPosition(agent); }
        @Override public FoodSource getNearestFood(AgentView agent, Set<ResourceType> types) { return null; }
        @Override public long getCurrentTick() { return 100L; }
        @Override public float getTemperature() { return 24.0f; }
        @Override public boolean isRaining() { return false; }
        @Override public float getLightLevel() { return 1.0f; }
        @Override public float getWaterLevel(float x, float y, float z) { return 0.0f; }
        @Override public float getRelativeHumidity(float x, float y, float z) { return 50.0f; }
        @Override public float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ) { return new float[]{1f, 0f, 0f}; }
    }

    /**
     * Synthetic AgentView adapter for rapid multi-caste training data generation.
     */
    public static class MockAgentView implements AgentView {
        public float x = 0f, y = 0f, z = 0f;
        public float heading = 0f;
        public float energy = 1.0f;
        public boolean carryingFood = false;
        public boolean atNest = false;
        public Individual.Caste caste = Individual.Caste.WORKER;
        public String insectType = "ANT";
        public boolean hasFoodNearby = false;
        public boolean hasEnemyNearby = false;
        public float alarmPheromone = 0.0f;
        public float foodPheromoneGradientX = 0.0f;
        public float foodPheromoneGradientY = 0.0f;

        @Override public float getX() { return x; }
        @Override public float getY() { return y; }
        @Override public float getZ() { return z; }
        @Override public float getHeading() { return heading; }
        @Override public float getHomeX() { return 0f; }
        @Override public float getHomeY() { return 0f; }
        @Override public boolean isCarryingFood() { return carryingFood; }
        @Override public boolean isAtNest() { return atNest; }
        @Override public float getEnergyLevel() { return energy; }
        @Override public float getHunger() { return 1.0f - energy; }
        @Override public UUID getColonyId() { return UUID.randomUUID(); }
        @Override public boolean isSoldier() { return caste == Individual.Caste.SOLDIER; }
        @Override public boolean isQueen() { return caste == Individual.Caste.QUEEN; }
        @Override public boolean isNurse() { return caste == Individual.Caste.NURSE; }
        @Override public boolean isDrone() { return caste == Individual.Caste.MALE; }
        @Override public Species getSpecies() {
            CustomSpecies sp = new CustomSpecies();
            sp.setInsectType(insectType);
            return sp;
        }
    }
}
