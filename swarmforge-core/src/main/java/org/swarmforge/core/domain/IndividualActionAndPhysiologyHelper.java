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
        return IndividualActionExecutor.harvestPlant(self, plant);
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
        return IndividualActionExecutor.executeAction(self, action, colony);
    }

    static void jobUpdate(Individual self, ConstructionManager constructionManager) {
        IndividualActionExecutor.jobUpdate(self, constructionManager);
    }

    static void takeDamageFromAttacker(Individual self, float amount, Individual attacker) {
        IndividualActionExecutor.takeDamageFromAttacker(self, amount, attacker);
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
        return SpeciesBehaviorSummarizer.getActiveBehaviorsSummary(species);
    }
}
