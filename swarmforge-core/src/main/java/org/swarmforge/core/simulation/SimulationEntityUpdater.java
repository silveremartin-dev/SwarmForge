/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.spatial.SpatialPartition;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles individual lifecycle, growth differentiation, action execution,
 * environmental hazards, and food depletion for the simulation tick.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SimulationEntityUpdater {

    private final Simulation simulation;

    public SimulationEntityUpdater(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Updates and prunes depleted food sources, refreshing spatial indexing.
     */
    public void updateFoodSources(List<FoodSource> foodSources,
                                  SpatialPartition<FoodSource> foodIndex,
                                  long currentTick,
                                  double accumulatedSimulationSeconds,
                                  Queue<SimulationEvent> eventQueue) {
        foodIndex.clear();
        foodSources.removeIf(food -> {
            boolean depleted = food.isDepleted();
            if (depleted) {
                String resName = (food.getType() != null) ? food.getType().name() : "Nourriture";
                int xPos = (int) food.getX();
                int yPos = (int) food.getY();
                int zPos = (int) food.getZ();

                Map<String, Object> data = new HashMap<>();
                data.put("resource", resName);
                data.put("x", xPos);
                data.put("y", yPos);
                data.put("z", zPos);

                String msg = String.format(Locale.ROOT, "🥀 Food Depleted: %s source fully consumed at (%d, %d, %d)", resName, xPos, yPos, zPos);
                SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.FOOD_DEPLETED, SimulationEvent.Severity.INFO, currentTick, msg, data);
                evt.setSimTimeSeconds(accumulatedSimulationSeconds);
                eventQueue.offer(evt);
                org.swarmforge.core.event.EventBus.getInstance().publish(evt);
            }
            return depleted;
        });
        for (FoodSource food : foodSources) {
            food.tick();
            foodIndex.insert(food, food.getX(), food.getY(), food.getZ());
        }
    }

    /**
     * Manages developmental stage transitions (egg -> larva -> pupa -> adult).
     */
    public void processGrowth(Individual ind, long currentTick, double accumulatedSimulationSeconds, Queue<SimulationEvent> eventQueue) {
        if (!ind.isAlive() || ind.getLifeStage() == Individual.LifeStage.ADULT) {
            return;
        }

        if (ind.getAgeInSeconds() >= ind.getMaturationThreshold()) {
            var sp = ind.getSpecies();
            switch (ind.getLifeStage()) {
                case EGG -> {
                    ind.setLifeStage(Individual.LifeStage.LARVA);
                    float larvaDur = sp != null ? sp.getLarvaStageDuration() : 600f;
                    if (larvaDur <= 30f) larvaDur *= 60.0f;
                    ind.setMaturationThreshold(ind.getAgeInSeconds() + larvaDur);
                    SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, currentTick, "Hatching: Egg hatched into Larva", null);
                    evt.setSimTimeSeconds(accumulatedSimulationSeconds);
                    eventQueue.offer(evt);
                    org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                }
                case LARVA -> {
                    if (ind.getEnergy() > 50) {
                        ind.setLifeStage(Individual.LifeStage.PUPA);
                        float pupaDur = sp != null ? sp.getPupaStageDuration() : 500f;
                        if (pupaDur <= 30f) pupaDur *= 60.0f;
                        ind.setMaturationThreshold(ind.getAgeInSeconds() + pupaDur);
                        SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, currentTick, "Pupation: Larva pupated into Pupa", null);
                        evt.setSimTimeSeconds(accumulatedSimulationSeconds);
                        eventQueue.offer(evt);
                        org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                    }
                }
                case PUPA -> {
                    ind.setLifeStage(Individual.LifeStage.ADULT);
                    if (ind.getCaste() == Individual.Caste.WORKER) {
                        float r = ind.getRandom() != null ? ind.getRandom().nextFloat() : (float) Math.random();
                        if (r < 0.2f) {
                            ind.setJob(Individual.Job.NURSE);
                        } else if (r < 0.5f) {
                            ind.setJob(Individual.Job.GUARD);
                        } else {
                            ind.setJob(Individual.Job.FORAGER);
                        }
                    } else if (ind.getCaste() == Individual.Caste.SOLDIER) {
                        ind.setJob(Individual.Job.GUARD);
                    }
                    SimulationEvent evt = SimulationEvent.obtain(
                            ind.getCaste() == Individual.Caste.QUEEN ? SimulationEvent.EventType.QUEEN_BORN :
                                    (ind.getCaste() == Individual.Caste.SOLDIER ? SimulationEvent.EventType.SOLDIER_BORN : SimulationEvent.EventType.WORKER_BORN),
                            SimulationEvent.Severity.INFO, currentTick, "Emergence: New Adult (" + ind.getCaste() + ") emerged", null);
                    evt.setSimTimeSeconds(accumulatedSimulationSeconds);
                    eventQueue.offer(evt);
                    org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                }
                case ADULT -> {
                    // Adult stage reached
                }
            }
        }
    }

    /**
     * Updates an individual ant's microclimate, life stage, decision loop, spatial constraints, and hazards.
     */
    public void updateIndividual(Individual individual, Colony colony, SimulationContext context,
                                 float simulationStepSeconds, long currentTick,
                                 Queue<SimulationEvent> eventQueue,
                                 ConcurrentLinkedQueue<Individual> livingIndividuals) {
        if (!individual.isAlive()) return;

        Terrarium terrarium = simulation.getTerrarium();

        // Update thermodynamic ambient temperature and humidity from weather context
        float ambTemp = context.getTemperature();
        float ambHum = context.getRelativeHumidity(individual.getX(), individual.getY(), individual.getZ());
        float surfaceZ = terrarium != null ? terrarium.getSurfaceElevation(individual.getX(), individual.getY()) : 16.0f;
        if (individual.getZ() < surfaceZ) {
            float optT = individual.getSpecies() != null ? individual.getSpecies().getOptimalTempCelsius() : 20.0f;
            float optH = individual.getSpecies() != null ? individual.getSpecies().getOptimalHumidityPercent() : 85.0f;
            float depthFactor = Math.min(1.0f, (surfaceZ - individual.getZ()) / 4.0f);
            ambTemp = ambTemp * (1.0f - depthFactor) + optT * depthFactor;
            ambHum = ambHum * (1.0f - depthFactor) + optH * depthFactor;
        }
        individual.setAmbientTemperatureC(ambTemp);
        individual.setAmbientHumidityPercent(ambHum);

        // Non-adult individuals (eggs, larvae, pupae) only undergo biological maturation
        if (individual.getLifeStage() != Individual.LifeStage.ADULT) {
            processGrowth(individual, currentTick, simulation.getAccumulatedSimulationSeconds(), eventQueue);
            individual.tick(simulationStepSeconds);

            // Ensure brood remains strictly subterranean inside the nest
            if (individual.getZ() >= surfaceZ) {
                float chamberDepth = Math.max(1.0f, colony.getDynamicQueenChamberDepth() * 0.5f);
                individual.setZ(Math.max(1.0f, surfaceZ - chamberDepth));
            }
            if (individual.isAlive()) {
                livingIndividuals.add(individual);
            }
            return;
        }

        if (individual.getBrain() == null) {
            individual.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
        }
        org.swarmforge.core.behavior.ReasoningArchitecture.Action action;
        boolean mustDecide = (currentTick < individual.getLastDecisionTick()
                || currentTick - individual.getLastDecisionTick() >= individual.getDynamicDecisionInterval())
                || individual.getCachedAction() == null;

        if (mustDecide && individual.getBrain() != null) {
            action = individual.getBrain().decide(individual, context);
            individual.setCachedAction(action);
            individual.setLastDecisionTick(currentTick);
        } else {
            action = individual.getCachedAction();
            if (action == null) {
                action = org.swarmforge.core.behavior.ReasoningArchitecture.Action.rest();
            }
        }

        // Action Execution
        org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult result = individual.executeAction(action, colony);

        // Learning / Update
        if (mustDecide && individual.getBrain() != null) {
            individual.getBrain().update(individual, action, result);
        }

        // Pheromone Deposit (Dual-Channel Algorithm)
        handlePheromoneDeposit(individual, action, terrarium);

        // Attack Target Resolution
        if (action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.ATTACK) {
            handleAttack(individual, action);
        }

        // Coordinate boundary clamping
        clampPosition(individual, terrarium);

        // Master deterministic PRNG
        individual.setRandom(simulation.getRandom());

        // Update construction context
        org.swarmforge.core.structure.ConstructionManager cm = simulation.getConstructionManagers().get(colony);
        if (cm != null) {
            individual.update(cm, simulationStepSeconds);
        } else {
            individual.tick(simulationStepSeconds);
        }

        // Collect living individuals and enforce spatial containment & environmental hazards
        if (individual.isAlive()) {
            clampPosition(individual, terrarium);

            // Ground-snapping for non-flying terrestrial ants
            if (!individual.canFly() && !individual.isClimbingTree() && terrarium != null) {
                if (individual.getZ() >= surfaceZ - 0.2f) {
                    individual.setZ(surfaceZ);
                }
            }

            livingIndividuals.add(individual);

            // Environmental hazards (floods, water drowning)
            checkWaterHazards(individual, context, terrarium, simulationStepSeconds, eventQueue);
        }
    }

    private void handlePheromoneDeposit(Individual individual, org.swarmforge.core.behavior.ReasoningArchitecture.Action action, Terrarium terrarium) {
        if (simulation.getPheromoneGrid() == null) return;
        if (action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.MOVE ||
                action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.RETURN_HOME ||
                action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.FOLLOW_TRAIL) {

            org.swarmforge.core.domain.PheromoneType typeToDeposit = individual.isCarryingFood()
                    ? org.swarmforge.core.domain.PheromoneType.FOOD_TRAIL
                    : org.swarmforge.core.domain.PheromoneType.HOME_TRAIL;

            int maxDepth = terrarium != null ? terrarium.getDepth() - 1 : 63;
            int depZ = Math.max(0, Math.min(maxDepth, (int) individual.getZ()));
            simulation.getPheromoneGrid().deposit(
                    (int) individual.getX(),
                    (int) individual.getY(),
                    depZ,
                    typeToDeposit.getIndex(),
                    1.0f
            );
        }
    }

    private void handleAttack(Individual individual, org.swarmforge.core.behavior.ReasoningArchitecture.Action action) {
        Object target = action.target();
        if (target instanceof Individual targetInd) {
            targetInd.takeDamage(individual.getAttackDamage(), individual);
        } else if (target instanceof org.swarmforge.core.domain.Predator targetPred) {
            targetPred.takeDamage(individual.getAttackDamage());
            if (!targetPred.isAlive() && simulation.getPredatorManager() != null) {
                simulation.getPredatorManager().removePredator(targetPred);
            }
        }
    }

    private void clampPosition(Individual individual, Terrarium terrarium) {
        if (terrarium == null) return;
        float maxX = Math.max(1.0f, terrarium.getWidth() - 1.0f);
        float maxY = Math.max(1.0f, terrarium.getHeight() - 1.0f);
        float maxDepth = Math.max(1.0f, (float) terrarium.getDepth() - 1.0f);
        float cx = Math.max(0.0f, Math.min(maxX, individual.getX()));
        float cy = Math.max(0.0f, Math.min(maxY, individual.getY()));
        float cz = Math.max(0.0f, Math.min(maxDepth, individual.getZ()));
        if (cx != individual.getX() || cy != individual.getY() || cz != individual.getZ()) {
            individual.setPosition(cx, cy, cz);
            individual.setHeading(individual.getHeading() + (float) Math.PI);
        }
    }

    private void checkWaterHazards(Individual individual, SimulationContext context, Terrarium terrarium,
                                   float simulationStepSeconds, Queue<SimulationEvent> eventQueue) {
        float waterLevel = context.getWaterLevel(individual.getX(), individual.getY(), individual.getZ());
        boolean isWaterVoxel = false;
        if (terrarium != null) {
            int ix = Math.max(0, Math.min(terrarium.getWidth() - 1, Math.round(individual.getX())));
            int iy = Math.max(0, Math.min(terrarium.getHeight() - 1, Math.round(individual.getY())));
            int iz = Math.max(0, Math.min(terrarium.getDepth() - 1, Math.round(individual.getZ())));
            TerrariumCell cell = terrarium.getCell(ix, iy, iz);
            if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                isWaterVoxel = true;
            }
        }

        if ((waterLevel > 0.3f || isWaterVoxel) && !individual.canFly()) {
            individual.setHeading(individual.getHeading() + (float) Math.PI * 0.75f);
            float drownDamage = 50.0f * simulationStepSeconds;
            if (individual.getHealth() <= drownDamage) {
                individual.die("Noyade / Cours d'eau (Drowning)");
            }
            individual.setHealth(Math.max(0.0f, individual.getHealth() - drownDamage));
            if (!individual.isAlive()) {
                eventQueue.offer(new SimulationEvent(SimulationEvent.EventType.DEATH,
                        simulation.getTickCount(), "Noyade dans le cours d'eau"));
            }
        }
    }
}
