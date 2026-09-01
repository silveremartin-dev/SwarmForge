/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.behaviors;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.event.SimulationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advanced queen behavior system.
 * Manages queen lifecycle: mating flights, colony foundation, egg laying, and
 * succession.
 *
 * <p>
 * Queen States:
 * </p>
 * <ul>
 * <li>VIRGIN - Not yet mated, may participate in mating flight</li>
 * <li>MATED - Successfully mated, can lay fertilized eggs</li>
 * <li>LAYING - Actively producing eggs</li>
 * <li>DECLINING - Old queen, reduced fertility</li>
 * <li>DEAD - No longer functional</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class QueenBehavior {

    public enum QueenState {
        VIRGIN,
        MATED,
        LAYING,
        DECLINING,
        DEAD
    }

    private final Colony colony;
    private final Simulation simulation;
    private final Random random;

    // Queen lifecycle parameters
    private QueenState state = QueenState.VIRGIN;
    private int storedSperm = 0;
    private int maxSperm = 10_000_000; // Typical ant queen storage
    private float layingCooldownSeconds = 0.0f;
    private int eggsLaidTotal = 0;
    private float matingFlightAgeSeconds = 300.0f; // Seconds until mating flight (5 minutes)
    private float fertilityRate = 1.0f;

    // Mating flight parameters
    private boolean matingFlightComplete = false;
    private List<String> matedMaleIds = new ArrayList<>();

    public QueenBehavior(Colony colony, Simulation simulation) {
        this.colony = colony;
        this.simulation = simulation;
        this.random = new Random();
    }

    /**
     * Process queen behavior each tick.
     * 
     * @param queen The queen individual
     */
    public void tick(Individual queen) {
        tick(queen, 0.016666667f);
    }

    public void tick(Individual queen, float deltaSeconds) {
        if (queen == null || queen.getCaste() != Caste.QUEEN || !queen.isAlive()) {
            return;
        }

        // Update state based on age
        updateState(queen);

        // Process based on current state
        switch (state) {
            case VIRGIN -> processVirginState(queen);
            case MATED -> processMatedState(queen);
            case LAYING -> processLayingState(queen);
            case DECLINING -> processDecliningState(queen);
            case DEAD -> {
                /* No action */ }
        }

        // Decrement cooldowns in seconds
        if (layingCooldownSeconds > 0.0f) {
            layingCooldownSeconds -= deltaSeconds;
        }
    }

    private void updateState(Individual queen) {
        float ageSec = queen.getAgeInSeconds();

        if (!queen.isAlive()) {
            state = QueenState.DEAD;
            return;
        }

        if (state == QueenState.VIRGIN && matingFlightComplete && storedSperm > 0) {
            state = QueenState.MATED;
            simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.MILESTONE_REACHED,
                    simulation.getTickCount(),
                    "Queen successfully mated with " + matedMaleIds.size() + " males"));
        }

        if (state == QueenState.MATED) {
            state = QueenState.LAYING;
        }

        // Check for decline based on age (in seconds) or sperm depletion
        float maxAgeDays = queen.getSpecies() != null ? queen.getSpecies().getQueenLifespan() : 365.0f;
        float maxAgeSec = maxAgeDays * 86400.0f;
        if (ageSec > maxAgeSec * 0.8f || storedSperm < maxSperm * 0.1f) {
            if (state == QueenState.LAYING) {
                state = QueenState.DECLINING;
                fertilityRate = 0.3f;
                simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.MILESTONE_REACHED,
                        simulation.getTickCount(),
                        "Queen entering declining phase"));
            }
        }
    }

    private void processVirginState(Individual queen) {
        // Check if it's time for mating flight
        if (queen.getAgeInSeconds() >= matingFlightAgeSeconds && !matingFlightComplete) {
            attemptMatingFlight(queen);
        }
    }

    /**
     * Attempt a mating flight.
     * Virgin queen flies to find males from other colonies.
     */
    private void attemptMatingFlight(Individual queen) {
        // Skip weather check for now - simplified version
        // In the future, could integrate with WeatherSystem

        simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                "Queen initiating mating flight"));

        // Find males in the simulation
        List<Individual> males = new ArrayList<>();
        for (Colony c : simulation.getColonies()) {
            for (Individual ind : c.getLivingIndividuals()) {
                if (ind.getCaste() == Caste.MALE && ind.isAlive() &&
                        ind.getLifeStage() == Individual.LifeStage.ADULT) {
                    // Only mate with males from different colonies (avoid inbreeding)
                    if (!c.getId().equals(colony.getId())) {
                        males.add(ind);
                    }
                }
            }
        }

        // Mate with multiple males (polyandry typical in ants)
        int matingsCount = Math.min(males.size(), 5 + random.nextInt(10));

        for (int i = 0; i < matingsCount; i++) {
            Individual male = males.get(random.nextInt(males.size()));
            matedMaleIds.add(male.getId().toString());
            storedSperm += 500_000 + random.nextInt(500_000); // Each male contributes

            // Males typically die after mating
            male.takeDamage(100);
        }

        storedSperm = Math.min(storedSperm, maxSperm);
        matingFlightComplete = true;

        simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                "Mating flight complete. Queen mated with " + matingsCount + " males"));
    }

    private void processMatedState(Individual queen) {
        // Transition to laying - find a suitable nest site
        if (queen.getState() == Individual.AiState.IDLE) {
            // Queen should find a location to establish colony
            queen.setState(Individual.AiState.RETURN_HOME);
        }
    }

    private void processLayingState(Individual queen) {
        if (layingCooldownSeconds > 0.0f)
            return;
        if (storedSperm <= 0)
            return;
        if (queen.getEnergy() < 30)
            return; // Need energy to lay

        // Check colony needs
        int currentPop = colony != null ? colony.getPopulation() : 0;
        int targetPop = 1000; // Base target capacity

        if (currentPop >= targetPop * 1.5f) {
            // Colony is full, slow down laying for 120 seconds
            layingCooldownSeconds = 120.0f;
            return;
        }

        // Lay an egg
        layEgg(queen);
    }

    /**
     * Lay a single egg.
     */
    private void layEgg(Individual queen) {
        if (storedSperm <= 0 || random.nextFloat() > fertilityRate) {
            // Unfertilized egg → Male
            Individual male = new Individual(colony.getId(), Caste.MALE, queen.getX(), queen.getY(), queen.getZ());
            male.setLifeStage(Individual.LifeStage.EGG);
            colony.addIndividual(male);
            eggsLaidTotal++;
        } else {
            // Fertilized egg → Worker (usually)
            storedSperm--;
            Caste caste = decideCaste();
            Individual offspring = new Individual(colony.getId(), caste, queen.getX(), queen.getY(), queen.getZ());
            offspring.setLifeStage(Individual.LifeStage.EGG);
            colony.addIndividual(offspring);
            eggsLaidTotal++;
        }

        queen.setEnergy(queen.getEnergy() - 2); // Laying costs energy
        float rate = queen.getSpecies() != null ? queen.getSpecies().getQueenEggLayingRate() : 25.0f;
        float baseIntervalSec = Math.max(2.0f, 86400.0f / Math.max(0.1f, rate) / 100.0f);
        layingCooldownSeconds = baseIntervalSec + random.nextFloat() * (baseIntervalSec * 0.5f);
    }

    /**
     * Decide what caste the new offspring should be.
     * Based on colony needs and pheromone signals.
     */
    private Caste decideCaste() {
        int workers = 0, soldiers = 0, nurses = 0;

        for (Individual ind : colony.getLivingIndividuals()) {
            switch (ind.getCaste()) {
                case WORKER, FORAGER -> workers++;
                case SOLDIER -> soldiers++;
                case NURSE -> nurses++;
                case QUEEN, MALE -> {
                    /* don't count */ }
            }
        }

        int total = workers + soldiers + nurses + 1;

        // Target ratios: 70% workers, 20% soldiers, 10% nurses
        float workerRatio = workers / (float) total;
        float soldierRatio = soldiers / (float) total;

        if (workerRatio < 0.6f)
            return random.nextBoolean() ? Caste.WORKER : Caste.FORAGER;
        if (soldierRatio < 0.15f)
            return Caste.SOLDIER;

        // Occasionally produce new queens (rare)
        if (random.nextFloat() < 0.001f)
            return Caste.QUEEN;

        return random.nextFloat() < 0.7f ? Caste.WORKER : random.nextFloat() < 0.5f ? Caste.SOLDIER : Caste.NURSE;
    }

    private void processDecliningState(Individual queen) {
        // Reduced egg production
        if (layingCooldownSeconds <= 0.0f && random.nextFloat() < 0.3f) {
            layEgg(queen);
        }
        layingCooldownSeconds = 60.0f; // Slower laying (60 seconds)

        // Check if succession needed
        checkSuccession(queen);
    }

    /**
     * Check if a new queen should take over.
     */
    private void checkSuccession(Individual currentQueen) {
        // Look for virgin queens in the colony
        for (Individual ind : colony.getLivingIndividuals()) {
            if (ind != currentQueen &&
                    ind.getCaste() == Caste.QUEEN &&
                    ind.isAlive() &&
                    ind.getLifeStage() == Individual.LifeStage.ADULT) {

                // Potential successor found
                simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.MILESTONE_REACHED,
                        simulation.getTickCount(),
                        "Potential queen successor detected"));
                break;
            }
        }
    }

    // === Getters ===

    public QueenState getState() {
        return state;
    }

    public int getStoredSperm() {
        return storedSperm;
    }

    public int getEggsLaidTotal() {
        return eggsLaidTotal;
    }

    public float getFertilityRate() {
        return fertilityRate;
    }

    public boolean isMatingFlightComplete() {
        return matingFlightComplete;
    }

    public List<String> getMatedMaleIds() {
        return new ArrayList<>(matedMaleIds);
    }

    public void setState(QueenState state) {
        this.state = state;
    }

    public void setStoredSperm(int sperm) {
        this.storedSperm = Math.min(sperm, maxSperm);
    }
}
