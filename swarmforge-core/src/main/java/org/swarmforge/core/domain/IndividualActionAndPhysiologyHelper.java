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
import org.swarmforge.core.world.WeatherMarkovChain;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Helper class encapsulating physiological metabolism, locomotion dynamics,
 * cognitive action execution, combat damage resolution, environmental sensory
 * sampling, and active behavior summaries for {@link Individual}.
 */
class IndividualActionAndPhysiologyHelper {

    private IndividualActionAndPhysiologyHelper() {
    }

    static boolean harvestPlant(Individual self, VegetationSystem.Plant plant) {
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

    static void applyBiologicalCasteStats(Individual self) {
        CasteTemplate casteTemplate = self.getCasteTemplate();
        if (casteTemplate != null) {
            self.setMaxHealth(casteTemplate.getBaseHealth() > 0 ? casteTemplate.getBaseHealth() : 50f);
            self.setAttackDamage(casteTemplate.getBaseDamage() > 0 ? casteTemplate.getBaseDamage() : 5f);
            self.setDefense(casteTemplate.getBaseDefense());
            return;
        }
        Species species = self.getSpecies();
        float baseSpeciesStrength = (species != null) ? species.getStrength() : 5.0f;
        Individual.Caste caste = self.getCaste();
        if (caste != null) {
            switch (caste) {
                case QUEEN -> {
                    self.setMaxHealth(200.0f);
                    self.setAttackDamage(Math.max(8.0f, baseSpeciesStrength * 1.5f));
                    self.setDefense(4.0f);
                }
                case SOLDIER -> {
                    self.setMaxHealth(150.0f);
                    self.setAttackDamage(Math.max(15.0f, baseSpeciesStrength * 2.5f));
                    self.setDefense(2.5f);
                }
                case MALE -> {
                    self.setMaxHealth(50.0f);
                    self.setAttackDamage(1.0f);
                    self.setDefense(0.0f);
                }
                default -> {
                    if (species != null && "Apis mellifera".equalsIgnoreCase(species.getScientificName())) {
                        self.setMaxHealth(40.0f);
                    } else {
                        self.setMaxHealth(100.0f);
                    }
                    self.setAttackDamage(baseSpeciesStrength);
                    self.setDefense(0.5f);
                }
            }
        } else {
            self.setMaxHealth(100.0f);
            self.setAttackDamage(baseSpeciesStrength);
            self.setDefense(0.5f);
        }
        if (self.getHealth() > self.getMaxHealth() || self.getHealth() <= 0.0f) {
            self.setHealthDirect(self.getMaxHealth());
        }
    }

    static float getWalkingSpeed(Individual self) {
        CasteTemplate casteTemplate = self.getCasteTemplate();
        Species species = self.getSpecies();
        float baseWalkingSpeed = (casteTemplate != null && casteTemplate.getWalkingSpeed() > 0)
                ? casteTemplate.getWalkingSpeed()
                : (species != null ? species.getWalkingSpeed() : 0.45f);

        float casteMult = switch (self.getCaste()) {
            case QUEEN -> 0.65f;
            case MALE -> 0.90f;
            case SOLDIER -> 1.05f;
            case FORAGER -> 1.00f;
            case NURSE -> 0.75f;
            case WORKER -> 0.95f;
        };

        if (casteTemplate != null && casteTemplate.getBodyLengthMm() > 0) {
            casteMult *= (float) Math.pow(casteTemplate.getBodyLengthMm() / 5.0f, 0.35);
        }

        float activityMult = 0.65f;
        if (self.getState() == Individual.AiState.TEND_BROOD || self.getState() == Individual.AiState.IDLE) {
            activityMult = 0.40f;
        }
        if (self.isCarryingFood() || self.getCarriedItem() != Individual.CarriedItem.NONE) {
            activityMult *= 0.72f;
        }

        float speed = baseWalkingSpeed * casteMult * activityMult * self.getQ10ThermalFactor();
        if (self.getGenome() != null) {
            speed *= self.getGenome().getSpeedMultiplier();
        }
        if (self.getHealth() < self.getMaxHealth() && self.getMaxHealth() > 0f) {
            speed *= (0.5f + 0.5f * (self.getHealth() / self.getMaxHealth()));
        }
        return Math.max(0.02f, speed);
    }

    static float getRunningSpeed(Individual self) {
        CasteTemplate casteTemplate = self.getCasteTemplate();
        Species species = self.getSpecies();
        float baseRunningSpeed = (casteTemplate != null && casteTemplate.getRunningSpeed() > 0)
                ? casteTemplate.getRunningSpeed()
                : (species != null ? species.getRunningSpeed() : 0.85f);

        float casteMult = (self.getCaste() == Individual.Caste.SOLDIER) ? 1.15f : ((self.getCaste() == Individual.Caste.QUEEN) ? 0.75f : 1.0f);
        float speed = baseRunningSpeed * casteMult * self.getQ10ThermalFactor();
        if (self.getGenome() != null) {
            speed *= self.getGenome().getSpeedMultiplier();
        }
        if (self.getHealth() < self.getMaxHealth() && self.getMaxHealth() > 0f) {
            speed *= (0.5f + 0.5f * (self.getHealth() / self.getMaxHealth()));
        }
        return Math.max(0.05f, speed);
    }

    static float getFlyingSpeed(Individual self) {
        if (!self.canFly()) return self.getWalkingSpeed();
        CasteTemplate casteTemplate = self.getCasteTemplate();
        Species species = self.getSpecies();

        float baseFlySpeed = (casteTemplate != null && casteTemplate.getFlyingSpeed() > 0)
                ? casteTemplate.getFlyingSpeed()
                : (species != null ? species.getFlyingSpeed() : 4.5f);

        float wingbeatHz = species != null ? species.getWingbeatFrequencyHz() : 180.0f;
        if (casteTemplate != null && casteTemplate.getWingbeatFrequencyHz() > 0) {
            wingbeatHz = casteTemplate.getWingbeatFrequencyHz();
        }

        baseFlySpeed *= (Math.max(50.0f, wingbeatHz) / 180.0f);
        if (species != null && species.hasHoveringCapability()) {
            baseFlySpeed *= 1.15f;
        }
        return Math.max(0.20f, baseFlySpeed * self.getQ10ThermalFactor());
    }

    static float getEffectiveMetabolismRate(Individual self) {
        Species species = self.getSpecies();
        float speciesMetabolism = species != null ? species.getMetabolism() : 1.0f;
        float dailyConsumption = species != null ? species.getDailyFoodConsumption() : 0.3f;

        float casteMetabolicFactor = switch (self.getCaste()) {
            case QUEEN -> 2.2f;
            case SOLDIER -> 1.3f;
            case MALE -> 0.8f;
            case FORAGER -> 1.1f;
            case NURSE, WORKER -> 1.0f;
        };

        float activityFactor = 1.0f;
        if (self.canFly() && self.getZ() > self.getSurfaceElevation() + 0.1f) {
            activityFactor = 3.5f;
        } else if (self.getJob() == Individual.Job.BUILDER || self.getJob() == Individual.Job.GUARD) {
            activityFactor = 1.3f;
        } else if (self.getState() == Individual.AiState.IDLE || self.getState() == Individual.AiState.TEND_BROOD) {
            activityFactor = 0.6f;
        }

        float effectiveMetabolism = speciesMetabolism * dailyConsumption * casteMetabolicFactor * activityFactor * self.getQ10ThermalFactor();
        if (self.getGenome() != null) {
            effectiveMetabolism *= self.getGenome().getMetabolismRate();
        }
        return Math.max(0.1f, effectiveMetabolism);
    }

    static void fly3D(Individual self, float targetX, float targetY, float targetZ, float speedParam) {
        if (!self.isAlive()) return;
        float dx = targetX - self.getX();
        float dy = targetY - self.getY();
        float dz = targetZ - self.getZ();
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 0.001f) return;

        float effectiveSpeed = self.getFlyingSpeed();
        if (speedParam > 0) {
            effectiveSpeed = Math.min(effectiveSpeed, speedParam);
        }
        float step = Math.min(dist, effectiveSpeed * 0.1f);
        self.setPosition(
                self.getX() + (dx / dist) * step,
                self.getY() + (dy / dist) * step,
                self.getZ() + (dz / dist) * step
        );
        self.setHeading((float) Math.atan2(dy, dx));

        Species species = self.getSpecies();
        CasteTemplate casteTemplate = self.getCasteTemplate();
        float wingbeatHz = species != null ? species.getWingbeatFrequencyHz() : 180.0f;
        if (casteTemplate != null && casteTemplate.getWingbeatFrequencyHz() > 0) {
            wingbeatHz = casteTemplate.getWingbeatFrequencyHz();
        }
        self.consumeEnergy(0.0003f * (wingbeatHz / 180.0f) * self.getEffectiveMetabolismRate() * step);
    }

    static float getQ10ThermalFactor(Individual self) {
        Species species = self.getSpecies();
        float optTemp = species != null ? species.getOptimalTempCelsius() : 24.0f;
        float minTemp = species != null ? species.getMinTempCelsius() : 10.0f;
        float maxTemp = species != null ? species.getMaxTempCelsius() : 40.0f;

        float diff = self.getAmbientTemperatureC() - optTemp;
        if (diff <= 0) {
            float sigmaLow = Math.max(1.0f, 0.4f * (optTemp - minTemp));
            return Math.max(0.05f, (float) Math.exp(-(diff * diff) / (2.0f * sigmaLow * sigmaLow)));
        } else {
            float sigmaHigh = Math.max(0.5f, 0.2f * (maxTemp - optTemp));
            float q10 = (float) Math.exp(-(diff * diff) / (2.0f * sigmaHigh * sigmaHigh));
            if (self.getAmbientTemperatureC() >= maxTemp) return 0.05f;
            return Math.max(0.05f, Math.min(1.2f, q10));
        }
    }

    static void tick(Individual self, float deltaSeconds) {
        if (!self.isAlive()) {
            self.lotsTrait.decompositionAgeSeconds += deltaSeconds;
            return;
        }
        self.advanceAge(deltaSeconds);

        Species species = self.getSpecies();
        float effectiveMetabolism = self.getEffectiveMetabolismRate();
        float waterReq = species != null ? species.getWaterRequirement() : 0.15f;
        float ambientHum = self.getAmbientHumidityPercent();
        float ambientTemp = self.getAmbientTemperatureC();

        float humidityThirstMult = Math.max(0.2f, 1.8f - (ambientHum / 100.0f) * 1.6f);

        self.consumeEnergyDirect(0.006f * effectiveMetabolism * deltaSeconds);
        self.addHungerDirect(0.0048f * effectiveMetabolism * deltaSeconds);
        self.addThirstDirect(0.0024f * effectiveMetabolism * (waterReq / 0.15f) * humidityThirstMult * deltaSeconds);

        float minTemp = species != null ? species.getMinTempCelsius() : 10.0f;
        float maxTemp = species != null ? species.getMaxTempCelsius() : 40.0f;
        float thermalExcess = 0.0f;
        if (ambientTemp < minTemp - 5.0f) {
            thermalExcess = (minTemp - 5.0f) - ambientTemp;
        } else if (ambientTemp > maxTemp + 5.0f) {
            thermalExcess = ambientTemp - (maxTemp + 5.0f);
        }
        if (thermalExcess > 0.0f) {
            float damageRate = Math.min(8.0f, thermalExcess * 0.4f);
            self.takeDamage(damageRate * deltaSeconds, "Thermal Extremes (Extreme Heat/Cold)");
        }

        float minHum = species != null ? species.getMinHumidityPercent() : 25.0f;
        float maxHum = species != null ? species.getMaxHumidityPercent() : 95.0f;
        float humidityExcess = 0.0f;
        if (ambientHum < minHum - 10.0f) {
            humidityExcess = (minHum - 10.0f) - ambientHum;
        } else if (ambientHum > maxHum + 5.0f) {
            humidityExcess = ambientHum - (maxHum + 5.0f);
        }
        if (humidityExcess > 0.0f) {
            float desiccationDamageRate = Math.min(4.0f, humidityExcess * 0.15f);
            self.takeDamage(desiccationDamageRate * deltaSeconds, "Desiccation (Extreme Dryness/Humidity)");
        }

        if (self.getEnergy() <= 0 || self.getHunger() >= 100) {
            self.die("Starvation / Energy Exhaustion");
            return;
        }
        if (self.getThirst() >= 100) {
            self.die("Severe Dehydration");
            return;
        }

        float maxLifespanSeconds = self.getMaxLifespan() * 86400.0f;
        if (self.getAgeInSeconds() >= maxLifespanSeconds) {
            self.die("Old Age (Natural end of life)");
            return;
        }

        if (species != null && species.hasMandibularWearPolyethism() && (self.getJob() == Individual.Job.BUILDER || self.getCaste() == Individual.Caste.FORAGER)) {
            self.advanceMandibleWear(0.0001f * deltaSeconds);
            if (self.getMandibleWear() >= 0.8f) {
                self.setJob(Individual.Job.NURSE);
            }
        }

        if (self.getCaste() == Individual.Caste.WORKER && self.getLifeStage() == Individual.LifeStage.ADULT) {
            float expectedAdultLife = Math.max(300.0f, maxLifespanSeconds * 0.5f);
            self.setJuvenileHormone(Math.min(1.0f, self.getJuvenileHormone() + (0.9f / expectedAdultLife) * deltaSeconds));
            if (self.getJuvenileHormone() > 0.65f && self.getJob() == Individual.Job.NURSE && self.getMandibleWear() < 0.8f) {
                self.setJob(Individual.Job.FORAGER);
            }
        }

        if (self.getState() == Individual.AiState.ATTACKING || self.getState() == Individual.AiState.FLEEING) {
            self.setOctopamine(Math.min(1.0f, self.getOctopamine() + 0.1f * deltaSeconds));
        } else {
            self.setOctopamine(self.getOctopamine() + (0.5f - self.getOctopamine()) * Math.min(1.0f, 0.05f * deltaSeconds));
        }

        if (self.getChemicalDefenseTrait().isChemotacticallyBlind()) {
            self.getChemicalDefenseTrait().decrementBlindnessTicks();
        }
        self.getChemicalDefenseTrait().refillFormicAcid(0.1f * deltaSeconds);

        self.getCircadianTrait().advancePhase((float) ((2.0 * Math.PI / 86400.0) * deltaSeconds));
        if (self.getCircadianTrait().isPolyphasicSleeping()) {
            self.setEnergy(Math.min(self.getMaxEnergy(), self.getEnergy() + 2.0f * deltaSeconds));
            self.setFatigue(Math.max(0.0f, self.getFatigue() - 4.0f * deltaSeconds));
        }

        if (self.lotsTrait.shiveringThermogenesis) {
            self.consumeEnergy(0.3f * deltaSeconds);
            self.lotsTrait.thoraxTemperatureC = Math.min(40.0f, self.lotsTrait.thoraxTemperatureC + 2.5f * deltaSeconds);
        } else if (self.lotsTrait.wingFanning) {
            self.consumeEnergy(0.15f * deltaSeconds);
            self.lotsTrait.thoraxTemperatureC = Math.max(18.0f, self.lotsTrait.thoraxTemperatureC - 3.0f * deltaSeconds);
        } else {
            self.lotsTrait.thoraxTemperatureC += (25.0f - self.lotsTrait.thoraxTemperatureC) * 0.05f * deltaSeconds;
        }
    }

    static ActionResult executeAction(Individual self, Action action, Colony colony) {
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

    static void jobUpdate(Individual self, ConstructionManager constructionManager) {
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

    static void takeDamageFromAttacker(Individual self, float amount, Individual attacker) {
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

    static Individual.SensorySample sampleSensoryEnvironment(Individual self, Terrarium terrarium, WeatherSystem weather) {
        int ix = (int) Math.floor(self.getX());
        int iy = (int) Math.floor(self.getY());
        int iz = (int) Math.floor(self.getZ());

        TerrariumCell currentCell = terrarium != null ? terrarium.getCell(ix, iy, iz) : null;
        Species species = self.getSpecies();

        float feltTemp = currentCell != null ? currentCell.temperature() : 20.0f;
        float feltHumidity = currentCell != null ? currentCell.humidity() : 0.5f;
        float feltCo2Ppm = currentCell != null ? currentCell.co2() : 400.0f;
        float feltLightLux = currentCell != null ? currentCell.light() * 1000.0f : 500.0f;

        float geomagIntensity = (weather != null && species != null && species.hasMagnetoreception())
                ? weather.getMagneticField() : 0.0f;
        float vibrationDb = (species != null && species.hasSubstrateVibrationSensing()) ? 12.0f : 0.0f;
        float electricFieldVolts = (weather != null && species != null && species.hasElectrosensing())
                ? (weather.getWeatherState() == WeatherMarkovChain.WeatherState.THUNDERSTORM ? 300.0f : 20.0f) : 0.0f;
        float solarAzimuthRad = (weather != null && species != null && species.hasPolarizedLightNavigation())
                ? (float) Math.toRadians(weather.getSunAngle() * 15.0f) : self.getHeading();

        return new Individual.SensorySample(feltTemp, feltHumidity, feltCo2Ppm, feltLightLux, geomagIntensity, vibrationDb, electricFieldVolts, solarAzimuthRad);
    }

    static String getActiveBehaviorsSummary(Species species) {
        if (species == null) return "STANDARD_PATROL";
        List<String> list = new ArrayList<>();
        if (species.canFarmAphids()) list.add("APHID_FARMING");
        if (species.hasRoyalPheromoneInhibition()) list.add("ROYAL_INHIBITION");
        if (species.canDrumSubstrate()) list.add("ACOUSTIC_DRUMMING");
        if (species.isPolycalic()) list.add("POLYCALIC_ROUTING");
        if (species.canCollectPropolis()) list.add("PROPOLIS_SHIELD");
        if (species.canSewLeavesWithLarvalSilk()) list.add("WEAVER_SILK");
        if (species.canWeedFungusGarden()) list.add("FUNGUS_WEEDING");
        if (species.canMakeStercoralCement()) list.add("STERCORAL_CEMENT");
        if (species.hasProctodealTrophallaxis()) list.add("PROCTODEAL_TROPHALLAXIS");
        if (species.canPerformPhragmosis()) list.add("PHRAGMOSIS_GATE");
        if (species.canPerformEvaporativeCooling()) list.add("EVAPORATIVE_COOLING");
        if (species.hasTrapJawMechanism()) list.add("TRAP_JAW");
        if (species.isSlaveMakingSpecies()) list.add("DULOSIS_RAID");
        if (species.canFormLivingBivouac()) list.add("LIVING_BIVOUAC");
        if (species.hasSolarOrientedMound()) list.add("SOLAR_MOUND");
        if (species.canPerformAllogrooming()) list.add("ALLOGROOMING");
        if (species.canPerformTrembleDance()) list.add("TREMBLE_DANCE");
        if (species.hasThermalTrailDecay()) list.add("THERMAL_TRAIL_DECAY");
        if (species.canPerformThoracicIncubation()) list.add("THORACIC_INCUBATION");
        if (species.canPerformRitualJousting()) list.add("RITUAL_JOUSTING");
        if (species.hasTerritorialRepellentPheromone()) list.add("TERRITORIAL_REPELLENT");
        if (species.canDetectHydrostaticPressure()) list.add("FLOOD_DETECTION");
        if (species.isRobberBeeSpecies()) list.add("ROBBER_BEE_RAID");
        if (species.canStridulateRescueCall()) list.add("RESCUE_STRIDULATION");
        if (species.isHoneypotStorageCaste()) list.add("HONEYPOT_STORAGE");
        if (species.canPlugContaminatedGalleries()) list.add("GRAVEL_PLUGGING");
        if (species.hasOleicAcidThresholdNecrophoresis()) list.add("NECROPHORESIS");
        if (species.hasUVPolarizedLightNavigation()) list.add("UV_COMPASS");
        if (species.canPerformTandemRunning()) list.add("TANDEM_RUNNING");
        if (species.canPerformThermalBalling()) list.add("THERMAL_BALLING");
        if (species.canFormLivingRaft()) list.add("LIVING_RAFT");
        if (species.canInhabitDomatia()) list.add("DOMATIA_PRUNING");
        if (species.canSelfIsolateWhenInfected()) list.add("SELF_ISOLATION");
        if (species.canSprayFormicResinDisinfectant()) list.add("RESIN_SPRAY");
        if (species.canTriggerEmergencySwarming()) list.add("EMERGENCY_SWARM");
        if (species.canConstructClayPillars()) list.add("CLAY_PILLARS");
        if (species.canDeGermStoredSeeds()) list.add("SEED_DEGERMINATION");
        if (species.canPerformQueenPiping()) list.add("QUEEN_PIPING");
        if (species.canPerformWaterTrophallaxis()) list.add("WATER_TROPHALLAXIS");
        if (species.canEnforceAphidSanitaryCordon()) list.add("APHID_SANITARY_CORDON");
        if (species.canFormLivingBridges()) list.add("LIVING_BRIDGES");
        if (species.canEmitAcousticPreySurge()) list.add("ACOUSTIC_PREY_SURGE");
        if (species.canSortExternalRefusePits()) list.add("REFUSE_SORTING");
        if (species.canCultivateWoodFungus()) list.add("WOOD_FUNGUS_CULTIVATION");
        if (species.hasEmergencyEscapePheromone()) list.add("EMERGENCY_ESCAPE_ALARM");
        if (species.canSealQueenChamberWax()) list.add("QUEEN_CELL_WAX_SEAL");
        if (species.hasCasteRatioPheromoneInhibition()) list.add("CASTE_RATIO_REGULATION");
        if (species.canPerformSuctionEscapePosture()) list.add("SUCTION_ESCAPE_POSTURE");
        if (species.canStridulateQueenRecognition()) list.add("QUEEN_RECOGNITION_CALL");
        if (species.canPerformPulsatileVentilation()) list.add("PULSATILE_VENTILATION");
        if (species.canRepairBreachesClay()) list.add("CLAY_BREACH_REPAIR");
        if (species.hasDepletingTrailPheromone()) list.add("DEPLETING_TRAIL_DECAY");
        if (species.canRecycleInviableEggs()) list.add("EGG_CANNIBALISM");
        if (species.canQuarantineInvasiveParasites()) list.add("PARASITE_QUARANTINE");
        if (species.canPerformArborealGlidingEscape()) list.add("ARBOREAL_GLIDING_ESCAPE");
        if (species.canPerformSolarBroodBasking()) list.add("SOLAR_BROOD_BASKING");
        if (species.canHarmonizeChcGestalt()) list.add("CHC_GESTALT_HARMONIZATION");
        if (species.canConstructCollapsiblePitTraps()) list.add("COLLAPSIBLE_PIT_TRAP");
        if (species.canHarvestDewCondensation()) list.add("DEW_CONDENSATION_HARVEST");
        if (species.canPerformExoskeletonAntiFungalPatrol()) list.add("EXOSKELETON_ANTIFUNGAL_PATROL");
        if (species.canPerformGuardShiftVibrationalWhisper()) list.add("GUARD_SHIFT_WHISPER");
        if (species.canConstructThermoregulatedConduits()) list.add("THERMOREGULATED_CONDUITS");
        if (species.canRaidToxicPlantResin()) list.add("TOXIC_PLANT_RESIN_RAID");
        if (species.canApplyDustSubstrateCamouflage()) list.add("DUST_SUBSTRATE_CAMOUFLAGE");
        if (species.canTransportChainBrood()) list.add("CHAIN_BROOD_TRANSPORT");
        if (species.hasTrophallacticOvaryInhibition()) list.add("TROPHALLACTIC_OVARY_INHIBITION");
        if (species.canPerformDroughtVibratoDance()) list.add("DROUGHT_VIBRATO_DANCE");
        if (species.canEncapsulateLargeIntrudersClay()) list.add("LARGE_INTRUDER_CLAY_ENCAPSULATION");
        if (species.canConstructPhonicIsolationChambers()) list.add("PHONIC_ISOLATION_CHAMBER");
        if (species.canApplyHydrophobicTrailCoating()) list.add("HYDROPHOBIC_TRAIL_COATING");
        if (species.canConsumeFermentedSapAnesthetic()) list.add("FERMENTED_SAP_ANESTHETIC");
        if (species.canPerformRelaySeedTransport()) list.add("RELAY_SEED_TRANSPORT");
        if (species.hasPreySizeSelectivePheromones()) list.add("PREY_SIZE_PHEROMONE");
        if (species.canDryLarvaeWoodDust()) list.add("LARVAL_WOOD_DUST_DRYING");
        if (species.canPerformFanoutEscapeFormicAcid()) list.add("FANOUT_ESCAPE_FORMIC_ACID");
        if (species.canEmitMoundOverheatVibrato()) list.add("MOUND_OVERHEAT_VIBRATO");
        if (species.canNourishVirginQueensPreFlight()) list.add("PREFLIGHT_QUEEN_NOURISHMENT");
        if (species.canPlugHoneyStoresBricks()) list.add("HONEY_STORE_BRICK_PLUG");
        if (species.canHuntNocturnalInfrared()) list.add("NOCTURNAL_INFRARED_HUNTING");
        if (species.canWeaveLarvalSilkCanopyBridges()) list.add("LARVAL_SILK_CANOPY_BRIDGE");
        if (species.canStridulateEggLayingSynchronization()) list.add("EGG_LAYING_SYNCHRONIZATION_STRIDULATION");
        if (species.canPerformAntennalDustGrooming()) list.add("ANTENNAL_DUST_GROOMING");
        if (species.canForageSaltCrystalsOsmoregulation()) list.add("SALT_CRYSTAL_OSMOREGULATION");
        if (species.canConstructRainEvacuationSiphons()) list.add("RAIN_EVACUATION_SIPHON");
        if (species.canAbsorbHostPlantChemicalCamouflage()) list.add("HOST_PLANT_CHEMICAL_CAMOUFLAGE");
        if (species.canDepositSulfurDustAntiMitePatrol()) list.add("SULFUR_DUST_ANTI_MITE_PATROL");
        if (species.canDanceVibratoHatchingEnthusiasm()) list.add("HATCHING_ENTHUSIASM_VIBRATO_DANCE");
        if (species.canResinMummifyNymphalChambers()) list.add("RESIN_NYMPHAL_MUMMIFICATION");
        if (species.canExcavatePitfallTraps()) list.add("PITFALL_TRAP_EXCAVATION");
        if (species.canSynthesizeGlycerolCryoprotection()) list.add("GLYCEROL_CRYOPROTECTION");
        if (species.canTransportInjuredPheromonalStretcher()) list.add("INJURED_PHEROMONAL_STRETCHER");
        if (species.canRaidAbandonedWaxVaults()) list.add("ABANDONED_WAX_VAULT_RAID");
        if (species.canPerformRitualMandibularWrestling()) list.add("RITUAL_MANDIBULAR_WRESTLING");
        if (species.canPerformPulsedAirConvectiveVentilation()) list.add("PULSED_AIR_VENTILATION");
        if (species.canCultivateStreptomycesAntibiotics()) list.add("STREPTOMYCES_ANTIBIOTICS");
        if (species.canNavigatePolarizedTwilightUV()) list.add("POLARIZED_TWILIGHT_UV_NAV");
        if (species.canSnapTrapMandiblesCatapult()) list.add("TRAP_MANDIBLE_CATAPULT");
        if (species.canPerformPedestrianSwarmBudding()) list.add("PEDESTRIAN_SWARM_BUDDING");
        if (species.canTrophallaxisProtozoa()) list.add("TERMITE_PROTOZOA_TROPHALLAXIS");
        if (species.canSquirtNasuteChemical()) list.add("NASUTE_SQUIRT_NOZZLE");
        if (species.canMasticatePaperPulpCarton()) list.add("PAPER_PULP_CARTON_MASTICATE");
        if (species.canHarvestLarvalSalivaDroplets()) list.add("LARVAL_SALIVA_HARVEST");
        if (species.canApplyPedicelAntRepellent()) list.add("PEDICEL_ANT_REPELLENT");
        if (species.canRecognizeFacialVisualPatterns()) list.add("WASPFACE_VISUAL_RECOGNITION");
        if (species.canPerformBuzzPollination()) list.add("BUZZ_POLLINATION_SONICATION");
        if (species.canIncubateBroodAbdominalHeat()) list.add("BUMBLEBEE_ABDOMINAL_BROOD_INCUBATION");
        if (species.canStabFrontalHornsAphid()) list.add("APHID_SOLDIER_HORN_STAB");
        if (species.canSqueezeGallIntrudersThrips()) list.add("THRIPS_GALL_FORELEG_SQUEEZE");
        if (species.canSnapClawAcousticShockwave()) list.add("EUSOCIAL_SHRIMP_CLAW_SHOCKWAVE");
        if (species.canStridulatePassalidParentalCare()) list.add("PASSALID_WOOD_FRASS_STRIDULATION");
        if (species.canPerformPhysogastricPeristalsis()) list.add("PHYSOGASTRIC_QUEEN_PERISTALSIS");
        if (species.canOrientMagneticMound()) list.add("GEOMAGNETIC_MOUND_ALIGNMENT");
        if (species.canEmitHornetGroupAlarmPheromone()) list.add("HORNET_VENOM_ALARM_RAID");
        if (species.canWeaveStenogastrinePaperJelly()) list.add("STENOGASTRINE_PAPER_JELLY_WEAVE");
        if (species.canInoculateFungalCombTermite()) list.add("TERMITE_TERMITOMYCES_FUNGAL_COMB");
        if (species.canDrumAbdomenWaspCellRim()) list.add("WASP_CELL_RIM_DRUMMING");
        if (species.canConstructNectarWaxPots()) list.add("BUMBLEBEE_NECTAR_WAX_POT");
        if (species.canPerformMaternalShieldGuarding()) list.add("PARENT_BUG_MATERNAL_SHIELD");
        if (species.canWeaveCommunalSpiderSilk()) list.add("COMMUNAL_SPIDER_SILK_WEAVE");
        if (species.canFormProcessionarySilkTrail()) list.add("PROCESSIONARY_SILK_TRAIL");
        if (species.canConstructClayVaultArches()) list.add("CLAY_VAULT_ARCH_ENGINEERING");
        if (species.canDeliverStenogastrinePapFood()) list.add("STENOGASTRINE_PAP_FOOD_DELIVERY");
        if (species.canPlasterFrassGalleryWalls()) list.add("BEETLE_FRASS_GALLERY_PLASTER");
        if (species.canLearnTrapliningFlightRoutes()) list.add("TRAPLINING_FLIGHT_ROUTE_LEARNING");
        if (species.canCoolNestWaterRegurgitation()) list.add("WASP_NEST_WATER_COOLING");
        if (species.canEjectHoneydewSignalingDroplets()) list.add("APHID_HONEYDEW_SIGNAL_FLICK");
        if (species.canSnapMandibleAcousticAlarm()) list.add("TERMITE_MANDIBLE_SNAP_ALARM");
        if (species.canPerformEggLickingGrooming()) list.add("EARWIG_EGG_LICKING_GROOMING");
        if (species.canConstructChaffGarbageDunes()) list.add("SEED_CHAFF_GARBAGE_DUNE");
        if (species.canDrumAntennaeLarvalStimulation()) list.add("WASP_ANTENNAL_DRUMMING_LARVA");
        if (species.canFormLeafPullingChains()) list.add("WEAVER_LEAF_PULLING_CHAIN");
        if (species.canApplySalivaryCementMoistureSeal()) list.add("TERMITE_SALIVARY_CEMENT_SEAL");
        if (species.canForageSubZeroBumblebee()) list.add("SUBZERO_BUMBLEBEE_FORAGING");
        if (species.canRepairGallSubstratalSecretion()) list.add("THRIPS_GALL_REPAIR_SECRETION");
        if (species.canTrophallaxisPassalidWoodFrass()) list.add("PASSALID_WOOD_FRASS_TROPHALLAXIS");
        if (species.canPerformCrecheRegurgitationSpider()) list.add("SPIDER_CRECHE_REGURGITATION");
        if (species.canBlockRoyalChamberSentry()) list.add("TERMITE_ROYAL_CHAMBER_BLOCKADE");
        if (species.canEmitParentBugAlarmGathering()) list.add("PARENT_BUG_ALARM_CLUSTER");
        if (species.canApplyBeeBreadHydrophobicCoating()) list.add("BEE_BREAD_LIPID_COATING");
        if (species.canBindParasitesWithSilk()) list.add("MYRMECOPHILE_PARASITE_SILK_BINDING");
        if (species.canEmitSubstrateObstacleVibrato()) list.add("SUBSTRATE_OBSTACLE_VIBRATO_WARNING");
        if (species.canPerformFormicAcidBathGrooming()) list.add("POST_COMBAT_FORMIC_ACID_BATH");
        if (species.canExcavateVerticalDrainageShafts()) list.add("VERTICAL_DRAINAGE_SHAFT_EXCAVATION");
        if (species.canIngestPhenolicResinMedication()) list.add("PHENOLIC_RESIN_IMMUNE_STIMULATION");
        if (species.canConstructSphagnumMoistureDomes()) list.add("SPHAGNUM_MOSS_MOISTURE_DOME");
        if (species.canMarkParasitizedCadaverRepellent()) list.add("PARASITIZED_CADAVER_REPELLENT_MARK");
        if (species.canDrumNuptialFlightSynchronization()) list.add("NUPTIAL_FLIGHT_SUBSTRATE_DRUMMING");
        if (species.canHarvestCuticularWaterCondensation()) list.add("CUTICULAR_HAIR_FOG_CONDENSATION");
        if (species.canStridulateLarvalHungerChirp()) list.add("PASSALID_GRUB_HUNGER_STRIDULATION");
        if (species.canConstructThermalChimneyFlues()) list.add("TERMITE_THERMAL_CHIMNEY_FLUE");
        if (species.canDepositLarvalFoodSalivaDrop()) list.add("WASP_EMERGENCY_SALIVA_FOOD_DROP");
        if (species.canApplyEggMassMucilageEnvelope()) list.add("EGG_MASS_MUCILAGE_ENVELOPE");
        if (species.canWeaveSilkPavilionAphidShelter()) list.add("WEAVER_SILK_PAVILION_APHID_SHELTER");
        if (species.canFormHotBallThermalDefense()) list.add("HONEYBEE_HOT_BALL_THERMAL_DEFENSE");
        if (species.canPerformFontanelleAutothysis()) list.add("TERMITE_AUTOTHYSIS_EXPLOSIVE_SACRIFICE");
        if (species.canSensePreySignalWireTripping()) list.add("SPIDER_DRAGLINE_SIGNAL_WIRE_TRIP");
        if (species.canMutilateSeedRadicles()) list.add("HARVESTER_SEED_RADICLE_MUTILATION");
        if (species.canBiteNectarTheftHoles()) list.add("BUMBLEBEE_NECTAR_THEFT_HOLE_BITE");
        if (species.canSowFungalSporeCombs()) list.add("TERMITE_FUNGAL_SPORE_SOWING");
        if (species.canHarnessLarvalSilkCocoon()) list.add("WEAVER_LARVAL_SILK_HARNESS");
        if (species.canFormBiomechanicalBivouac()) list.add("ARMY_ANT_BIOMECHANICAL_BIVOUAC");
        if (species.canPerformBuzzPollinationSonication()) list.add("BUMBLEBEE_BUZZ_POLLINATION");
        if (species.canRegurgitateEarwigMaternalFood()) list.add("EARWIG_MATERNAL_FOOD_REGURGITATION");
        if (species.canRecognizeWaspFacialPatterns()) list.add("WASP_FACIAL_PATTERN_RECOGNITION");
        if (species.canFireShrimpAcousticCannon()) list.add("SHRIMP_ACOUSTIC_CANNON_DEFENSE");
        if (species.canDuetPassalidSubstrateVibration()) list.add("PASSALID_SUBSTRATE_DUET_COMMUNICATION");
        if (species.canTurnGranarySeedsAeration()) list.add("HARVESTER_GRANARY_SEED_TURNING");
        if (species.canEncodeWaggleDanceSunCompass()) list.add("HONEYBEE_WAGGLE_DANCE_SUN_COMPASS");
        if (species.canDigSubterraneanClayAqueducts()) list.add("TERMITE_CLAY_AQUEDUCT_WELL");
        if (species.canFireFormicAcidArtilleryJet()) list.add("FORMICA_ACID_ARTILLERY_JET");
        if (species.canEjectGarbageChuteRefuse()) list.add("SPIDER_GARBAGE_CHUTE_EJECTIONS");
        if (species.canFanWingsForBroodThermoregulation()) list.add("BROOD_THERMOREGULATORY_WING_FANNING");
        if (species.canPlugGallWithChitinousTube()) list.add("THRIPS_GALL_CHITINOUS_TUBE_PLUG");
        if (species.canCoatWaspPedicelAntRepellent()) list.add("WASP_PEDICEL_ANT_REPELLENT_COAT");
        if (species.canSquirtNasuteViscousResin()) list.add("NASUTE_VISCOUS_RESIN_SQUIRT");
        if (species.canSqueezeIntrudersWithForelegs()) list.add("APHID_FORELEG_INTRUDER_SQUEEZE");
        if (species.canShieldEggsFromParasitoidWasps()) list.add("SHIELD_BUG_EGG_PARASITOID_SHIELD");
        if (species.canPlasterWoodWallGallery()) list.add("PASSALID_WOOD_WALL_GALLERY_PLASTER");
        if (species.canShearLeafCrescentMandible()) list.add("ATTA_LEAF_CRESCENT_MANDIBLE_SHEAR");
        if (species.canShieldSwarmCoreHeat()) list.add("HONEYBEE_SWARM_CORE_HEAT_SHIELD");
        if (species.canPerformQueenPhysogastricPeristalsis()) list.add("TERMITE_QUEEN_EGG_PERISTALSIS");
        if (species.canWeaveSocialSilkHammock()) list.add("CATERPILLAR_SILK_HAMMOCK_TENT");
        if (species.canPackCorbiculaPollenBaskets()) list.add("BUMBLEBEE_CORBICULA_POLLEN_PACKING");
        if (species.canScrapeWoodPulpCarton()) list.add("WASP_WOOD_PULP_CARTON_SCRAPE");
        if (species.canLayTrophicNourishmentEggs()) list.add("WEAVER_TROPHIC_EGG_NOURISHMENT");
        if (species.canNavigatePolarizedLightCompass()) list.add("DESERT_ANT_POLARIZED_LIGHT_COMPASS");
        if (species.canBuryFungalWasteInGallery()) list.add("TERMITE_FUNGAL_WASTE_BURIAL");
        if (species.canChewSeedHuskBreadPulp()) list.add("HARVESTER_ANT_BREAD_PULP_CHEW");
        if (species.canWrapPreyInCommunalSilk()) list.add("SPIDER_COMMUNAL_SILK_PREY_WRAP");
        if (species.canSealNestGapsWithPropolis()) list.add("HONEYBEE_PROPOLIS_NEST_SEAL");
        if (species.canSynchronizeSoldierAlarmDrumming()) list.add("TERMITE_SOLDIER_ALARM_DRUM_SYNCHRONY");
        if (species.canPerformDominanceMounting()) list.add("WASP_DOMINANCE_MOUNTING_DRUM");
        if (species.canLapNectarTongueExtension()) list.add("BUMBLEBEE_NECTAR_TONGUE_LAPPING");
        if (species.canSecreteGallClosingFluid()) list.add("APHID_GALL_CLOSING_FLUID_SECRETION");
        if (species.canGroomNymphCuticularSurface()) list.add("EARWIG_NYMPH_CUTICULAR_GROOMING");
        if (species.canExcavateGardenWasteChambers()) list.add("ATTA_GARDEN_WASTE_CHAMBER_DIG");
        if (species.canExchangeRoyalPairGrooming()) list.add("TERMITE_ROYAL_PAIR_GROOMING");
        if (species.canTriggerUniversalEmergencyEvacuation()) list.add("SWARMFORGE_UNIVERSAL_EMERGENCY_EVACUATION");
        if (species.canStoreNectarAsHoneypotReplete()) list.add("HONEYPOT_REPLETE_NECTAR_STORAGE");
        if (species.canFormFloatingAntRaft()) list.add("FLOATING_ANT_FLOOD_RAFT");
        if (species.canConstructMudResinEntranceFunnel()) list.add("MUD_RESIN_ENTRANCE_FUNNEL_GUARD");
        if (species.canExcavateHibernationBurrow()) list.add("QUEEN_SOIL_HIBERNATION_BURROW");
        if (species.canInoculateLeafPulpEnzymes()) list.add("LEAF_PULP_ENZYME_INOCULATION");
        if (species.canPerformGamergateDominanceTournament()) list.add("GAMERGATE_DOMINANCE_TOURNAMENT");
        if (species.canFeedOnLarvalHemolymphDracula()) list.add("DRACULA_ANT_LARVAL_HEMOLYMPH_FEED");
        if (species.canFormTarsalFrictionBridge()) list.add("TARSAL_FRICTION_TENSILE_BRIDGE");
        if (species.canTransportWaterInMandibleDroplet()) list.add("MANDIBLE_DROPLET_WATER_TRANSPORT");
        if (species.canStiltWalkThermalRegim()) list.add("DESERT_ANT_STILT_WALKING_COOLING");
        if (species.canPerformAntiPredatorShimmeringWave()) list.add("GIANT_HONEYBEE_SHIMMERING_WAVE");
        if (species.canDouseNestWaterCooling()) list.add("PAPER_WASP_WATER_DOUSING_COOLING");
        if (species.canAerateFungalCombChambers()) list.add("CLAY_WALL_FUNGAL_AERATION");
        if (species.canFeedLarvaeExuviaRecycling()) list.add("LARVAL_EXUVIA_CHITIN_RECYCLING");
        if (species.canGroomLeafPulpParasitesMinim()) list.add("MINIM_LEAF_PARASITE_GROOMING");
        if (species.canCamouflageWebWithPlantDebris()) list.add("SPIDER_WEB_DEBRIS_CAMOUFLAGE");
        if (species.canCockGasterFormicAcidRepellent()) list.add("ACROBAT_ANT_GASTER_VENOM_DEFENSE");
        if (species.canMilkAphidHoneydewStroking()) list.add("APHID_HONEYDEW_ANTENNAL_MILKING");
        if (species.canClusterSolarHeatCollector()) list.add("MOUND_SOLAR_HEAT_COLLECTOR_CLUSTER");
        if (species.canSerializeGlobalEthologicalBitSet()) list.add("GLOBAL_ETHOLOGICAL_BITSET_SERIALIZATION");

        if (list.isEmpty()) return "BASE_PATROL";
        if (list.size() > 3) {
            return String.join(", ", list.subList(0, 3)) + String.format(" (+%d)", list.size() - 3);
        }
        return String.join(", ", list);
    }
}
