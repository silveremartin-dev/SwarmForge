/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.behavior.ReasoningArchitecture.Action;
import org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult;
import org.swarmforge.core.epidemiology.IndividualInfection;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.structure.ConstructionManager;
import org.swarmforge.core.structure.ConstructionTask;
import org.swarmforge.core.world.VegetationSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Executes high-level cognitive actions, plant harvesting, construction job updates,
 * and combat interactions for {@link Individual}.
 *
 * <p>Separated from {@link IndividualActionAndPhysiologyHelper} to adhere to the
 * Single Responsibility Principle and maintain clean file bounds.</p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class IndividualActionExecutor {

    private IndividualActionExecutor() {
    }

    /**
     * Harvests seeds or foliage from a target plant depending on plant species.
     */
    public static boolean harvestPlant(Individual self, VegetationSystem.Plant plant) {
        if (plant == null || !self.isAlive()) return false;
        if (plant.type == VegetationSystem.PlantType.GRASS || plant.type == VegetationSystem.PlantType.FLOWER) {
            float seeds = plant.harvestSeeds(0.5f);
            if (seeds > 0) {
                self.setCarriedItem(Individual.CarriedItem.FOOD);
                self.setCarriedResourceType(ResourceType.SEED);
                return true;
            }
        } else if (plant.type == VegetationSystem.PlantType.TREE || plant.type == VegetationSystem.PlantType.SHRUB) {
            float foliage = plant.harvestFoliage(0.5f);
            if (foliage > 0) {
                self.climbTree(plant.getCurrentHeight() * 0.5f);
                self.setCarriedItem(Individual.CarriedItem.FOOD);
                self.setCarriedResourceType(ResourceType.LEAF);
                return true;
            }
        }
        return false;
    }

    /**
     * Executes the supplied cognitive {@link Action} in the context of the individual's colony.
     */
    public static ActionResult executeAction(Individual self, Action action, Colony colony) {
        if (!self.isAlive()) return ActionResult.failure("Dead");

        if (self.getCaste() == Individual.Caste.QUEEN && colony != null) {
            float targetZ = colony.getNestZ() < 0 ? colony.getNestZ() - 0.5f : -1.8f;
            self.setZ(self.getZ() + (targetZ - self.getZ()) * 0.1f);
        } else if (self.getJob() == Individual.Job.NURSE && self.isAtNest()) {
            float targetZ = -1.2f;
            self.setZ(self.getZ() + (targetZ - self.getZ()) * 0.1f);
        }

        switch (action.type()) {
            case MOVE -> {
                float dx = action.directionX();
                float dy = action.directionY();
                float len = (float) Math.hypot(dx, dy);
                if (len > 0.0001f) {
                    float moveSpeed = self.getCurrentMovementSpeed();
                    float baseStep = Math.max(0.02f, moveSpeed * 0.1f);
                    float step = Math.min(baseStep * Math.max(0.1f, action.intensity()), 0.25f);
                    float nextX = self.getX() + (dx / len) * step;
                    float nextY = self.getY() + (dy / len) * step;

                    Terrarium t = colony != null ? colony.getTerrarium() : null;
                    if (!self.canFly() && t != null) {
                        int ix = Math.max(0, Math.min(t.getWidth() - 1, Math.round(nextX)));
                        int iy = Math.max(0, Math.min(t.getHeight() - 1, Math.round(nextY)));
                        int iz = Math.max(0, Math.min(t.getDepth() - 1, Math.round(self.getZ())));
                        TerrariumCell cell = t.getCell(ix, iy, iz);
                        if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                            self.setHeading(self.getHeading() + (float) (Math.PI * 0.75f));
                            return ActionResult.failure("Water hazard avoided");
                        }
                    }

                    self.integrateDisplacement(nextX - self.getX(), nextY - self.getY());
                    self.setX(nextX);
                    self.setY(nextY);
                    self.setHeading((float) Math.atan2(dy, dx));
                    if (self.getCarriedItem() == Individual.CarriedItem.EARTH && self.isNearHomeSurface(colony)) {
                        self.depositEarthMound(colony);
                    }
                }
                return ActionResult.ok();
            }
            case FORAGE -> {
                self.setHeading(self.getHeading() + (self.getRandom().nextFloat() - 0.5f) * 0.25f);
                float surfaceZ = self.getSurfaceElevation();
                if (self.getZ() < surfaceZ && (self.getCaste() != Individual.Caste.QUEEN && self.getJob() != Individual.Job.NURSE)) {
                    self.setZ(Math.min(surfaceZ, self.getZ() + 0.08f));
                }
                self.move(0.9f);
                return ActionResult.ok();
            }
            case RETURN_HOME, ABORT_AND_RETURN -> {
                float dx = self.getHomeX() - self.getX();
                float dy = self.getHomeY() - self.getY();
                self.turnTowards((float) Math.atan2(dy, dx), 0.1f);
                self.move(1.0f);
                if (self.isAtNest() && colony != null) {
                    self.resetPathIntegration();
                    if (self.getCarriedItem() == Individual.CarriedItem.EARTH) {
                        self.depositEarthMound(colony);
                    }
                    float surfaceZ = self.getSurfaceElevation();
                    float targetNestZ = colony.getNestZ() < surfaceZ ? colony.getNestZ() : surfaceZ - 1.5f;
                    self.setZ(self.getZ() + (targetNestZ - self.getZ()) * 0.12f);
                    if (colony.getFoodStored() > 0.1f) {
                        if (colony.getCarbohydrateStored() > 0.05f) {
                            colony.setCarbohydrateStored(colony.getCarbohydrateStored() - 0.05f);
                        } else if (colony.getProteinStored() > 0.05f) {
                            colony.setProteinStored(colony.getProteinStored() - 0.05f);
                        } else {
                            colony.consumeResource(ResourceType.SEED, 0.05f);
                        }
                        self.setHunger(Math.max(0f, self.getHunger() - 15.0f));
                        self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 15.0f));
                    }
                    if (colony.getResourceAmount(ResourceType.WATER) > 0.1f) {
                        colony.consumeResource(ResourceType.WATER, 0.05f);
                        self.setThirst(Math.max(0f, self.getThirst() - 15.0f));
                    }
                }
                return ActionResult.ok();
            }
            case REST -> {
                if (self.isAtNest() && colony != null) {
                    float targetNestZ = colony.getNestZ() < 0 ? colony.getNestZ() : -1.2f;
                    self.setZ(self.getZ() + (targetNestZ - self.getZ()) * 0.12f);
                }
                self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 5.0f));
                self.setHunger(Math.max(0f, self.getHunger() - 5.0f));
                self.setThirst(Math.max(0f, self.getThirst() - 5.0f));
                return ActionResult.ok();
            }
            case ATTACK -> {
                if (action.target() instanceof Individual target) {
                    if (colony != null && colony.getDiplomacy().isAlly(target.getColonyId())) {
                        return ActionResult.failure("Target is an Ally!");
                    }
                    float dx = target.getX() - self.getX();
                    float dy = target.getY() - self.getY();
                    float dz = target.getZ() - self.getZ();
                    float distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < 4.0f) {
                        target.takeDamage(self.getBaseAttackDamage(), self);
                        return ActionResult.ok();
                    }
                    return ActionResult.failure("Target out of range");
                }
                return ActionResult.failure("Invalid Target");
            }
            case FOLLOW_TRAIL -> {
                float dx = action.directionX();
                float dy = action.directionY();
                float len = (float) Math.hypot(dx, dy);
                if (len > 0.0001f) {
                    Species species = self.getSpecies();
                    float workerSpeed = species != null ? species.getWorkerSpeed() : 0.5f;
                    float baseStep = Math.max(0.02f, workerSpeed * 0.1f) * self.getQ10ThermalFactor();
                    float step = Math.min(baseStep * Math.max(0.1f, action.intensity()), 0.15f);
                    float nextX = self.getX() + (dx / len) * step;
                    float nextY = self.getY() + (dy / len) * step;

                    Terrarium t = colony != null ? colony.getTerrarium() : null;
                    if (!self.canFly() && t != null) {
                        int ix = Math.max(0, Math.min(t.getWidth() - 1, Math.round(nextX)));
                        int iy = Math.max(0, Math.min(t.getHeight() - 1, Math.round(nextY)));
                        int iz = Math.max(0, Math.min(t.getDepth() - 1, Math.round(self.getZ())));
                        TerrariumCell cell = t.getCell(ix, iy, iz);
                        if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                            self.setHeading(self.getHeading() + (float) (Math.PI * 0.75f));
                            return ActionResult.failure("Water hazard avoided");
                        }
                    }

                    self.setX(nextX);
                    self.setY(nextY);
                    self.setHeading((float) Math.atan2(dy, dx));
                }
                return ActionResult.ok();
            }
            case DEPOSIT_FOOD -> {
                if (self.isCarryingFood() && colony != null) {
                    if (self.isAtNest()) {
                        float targetNestZ = colony.getNestZ() < 0 ? colony.getNestZ() : -1.0f;
                        self.setZ(self.getZ() + (targetNestZ - self.getZ()) * 0.15f);
                    }
                    ResourceType carriedResourceType = self.getCarriedResourceType();
                    if (carriedResourceType != null) {
                        colony.addResource(carriedResourceType, 1.0f);
                        if (carriedResourceType == ResourceType.PROPOLIS_RESIN && colony.getSocialImmunityManager() != null && colony.getNestVoxelGrid() != null) {
                            int vx = Math.max(0, Math.min(colony.getNestVoxelGrid().getWidth() - 1, (int) Math.floor(self.getX() - colony.getNestX() + 8)));
                            int vy = Math.max(0, Math.min(colony.getNestVoxelGrid().getHeight() - 1, (int) Math.floor(self.getY() - colony.getNestY() + 8)));
                            int vz = Math.max(0, Math.min(colony.getNestVoxelGrid().getDepth() - 1, (int) Math.floor(Math.abs(self.getZ()))));
                            colony.getSocialImmunityManager().applyPropolisCoating(colony, colony.getNestVoxelGrid(), vx, vy, vz);
                        }
                    } else {
                        colony.addResource(ResourceType.SEED, 1.0f);
                    }
                    self.setCarriedItem(Individual.CarriedItem.NONE);
                    self.setHunger(0f);
                    self.setThirst(0f);
                    self.setEnergy(self.getMaxEnergy());
                    return ActionResult.ok();
                }
                return ActionResult.failure("Not carrying food");
            }
            case GROOM -> {
                if (action.target() instanceof Individual target) {
                    if (target.isAlive() && target.getColonyId().equals(self.getColonyId())) {
                        float distSq = (target.getX() - self.getX()) * (target.getX() - self.getX())
                                + (target.getY() - self.getY()) * (target.getY() - self.getY())
                                + (target.getZ() - self.getZ()) * (target.getZ() - self.getZ());
                        if (distSq <= 4.0f) {
                            self.consumeEnergy(0.5f);
                            if (colony != null && colony.getSocialImmunityManager() != null) {
                                var registry = colony.getInfectionRegistry();
                                var groomerInf = registry.computeIfAbsent(self.getId(), IndividualInfection::new);
                                var targetInf = registry.computeIfAbsent(target.getId(), IndividualInfection::new);
                                colony.getSocialImmunityManager().performAllogrooming(groomerInf, targetInf, self.getRandom());
                            }
                            return ActionResult.ok();
                        }
                        return ActionResult.failure("Target out of grooming range");
                    }
                }
                return ActionResult.failure("Invalid grooming target");
            }
            case NURSE -> {
                if (action.target() instanceof Individual broodTarget) {
                    if (broodTarget.isAlive() && broodTarget.getColonyId().equals(self.getColonyId()) && broodTarget.getLifeStage() != Individual.LifeStage.ADULT) {
                        float distSq = (broodTarget.getX() - self.getX()) * (broodTarget.getX() - self.getX())
                                + (broodTarget.getY() - self.getY()) * (broodTarget.getY() - self.getY())
                                + (broodTarget.getZ() - self.getZ()) * (broodTarget.getZ() - self.getZ());
                        if (distSq <= 4.0f) {
                            if (colony != null && (colony.getProteinStored() > 0.1f || colony.getFoodStored() > 0.1f)) {
                                if (colony.getProteinStored() > 0.1f) colony.setProteinStored(colony.getProteinStored() - 0.1f);
                                else colony.setFoodStored(colony.getFoodStored() - 0.1f);
                                broodTarget.setEnergy(Math.min(100.0f, broodTarget.getEnergy() + 10.0f));
                            }
                            return ActionResult.ok();
                        }
                        return ActionResult.failure("Brood out of range");
                    }
                }
                return ActionResult.failure("Invalid brood target");
            }
            case COMMUNICATE -> {
                if (action.target() instanceof Individual mate) {
                    if (mate.isAlive() && mate.getColonyId().equals(self.getColonyId())) {
                        float distSq = (mate.getX() - self.getX()) * (mate.getX() - self.getX())
                                + (mate.getY() - self.getY()) * (mate.getY() - self.getY())
                                + (mate.getZ() - self.getZ()) * (mate.getZ() - self.getZ());
                        if (distSq <= 4.0f) {
                            float avgEnergy = (self.getEnergy() + mate.getEnergy()) / 2.0f;
                            self.setEnergy(avgEnergy);
                            mate.setEnergy(avgEnergy);
                            return ActionResult.ok();
                        }
                    }
                }
                return ActionResult.failure("Nestmate not in contact range");
            }
            default -> {
                return ActionResult.ok();
            }
        }
    }

    /**
     * Updates construction building tasks for builder individuals.
     */
    public static void jobUpdate(Individual self, ConstructionManager constructionManager) {
        if (self.getJob() == Individual.Job.BUILDER && constructionManager != null) {
            Optional<ConstructionTask> taskOpt = constructionManager.getAvailableTask(self.getX(), self.getY(), self.getZ());
            if (taskOpt.isPresent()) {
                ConstructionTask task = taskOpt.get();
                if (!task.isAssigned()) {
                    task.setAssigned(true);
                    float dx = task.getX() - self.getX();
                    float dy = task.getY() - self.getY();
                    float dz = task.getZ() - self.getZ();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist < 1.0f) {
                        task.work(1.0f);
                    } else {
                        self.setHeading((float) Math.atan2(dy, dx));
                    }
                }
            }
        }
    }

    /**
     * Resolves incoming combat damage and emits combat telemetry events.
     */
    public static void takeDamageFromAttacker(Individual self, float amount, Individual attacker) {
        float effectiveDamage = Math.max(0, amount - self.getDefense());
        self.setHealthDirect(self.getHealth() - effectiveDamage);

        if (attacker != null) {
            String attShort = "#" + attacker.getId().toString().substring(0, 8).toUpperCase();
            String defShort = "#" + self.getId().toString().substring(0, 8).toUpperCase();
            String attCaste = Individual.formatCasteFr(attacker.getCaste());
            String defCaste = Individual.formatCasteFr(self.getCaste());
            int xPos = (int) self.getX();
            int yPos = (int) self.getY();
            int zPos = (int) self.getZ();

            Map<String, Object> data = new HashMap<>();
            data.put("attackerId", attShort);
            data.put("attackerCaste", attacker.getCaste().name());
            data.put("defenderId", defShort);
            data.put("defenderCaste", self.getCaste().name());
            data.put("damage", Math.round(effectiveDamage * 10.0f) / 10.0f);
            data.put("healthRemaining", Math.max(0, Math.round(self.getHealth() * 10.0f) / 10.0f));
            data.put("x", xPos);
            data.put("y", yPos);
            data.put("z", zPos);

            String msg = String.format("Combat : %s %s a infligé %.1f dégâts à %s %s à (%d, %d, %d) [PV restant: %.1f]",
                    attCaste, attShort, effectiveDamage, defCaste, defShort, xPos, yPos, zPos, Math.max(0, self.getHealth()));

            SimulationEvent evt = SimulationEvent.obtain(
                    SimulationEvent.EventType.COMBAT_OCCURRED,
                    SimulationEvent.Severity.WARNING,
                    0, msg, data);
            EventBus.getInstance().publish(evt);
        }

        if (self.getHealth() <= 0) {
            String attackerName = (attacker != null) ? (attacker.getCaste() + " #" + attacker.getId().toString().substring(0, 5)) : "Ennemi";
            self.die("Combat contre " + attackerName);
        }
    }
}
