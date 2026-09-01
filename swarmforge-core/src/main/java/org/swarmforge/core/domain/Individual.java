/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;
import org.swarmforge.core.behavior.ReasoningArchitecture.Action;
import org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult;
import org.swarmforge.core.genetics.Genome;

import org.swarmforge.core.behavior.AgentView;

/**
 * Represents an individual eusocial insect in the simulation.
 * Uses a sealed interface for type-safe caste representation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @deprecated Use org.swarmforge.core.ecs.components.* and org.swarmforge.core.ecs.systems.* instead.
 */
@Deprecated
public class Individual implements java.io.Serializable, AgentView {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Caste caste;
    private final UUID colonyId;

    // Position
    private float x, y, z;
    private float heading; // Direction in radians

    // State
    private float health = 100f;
    private float maxEnergy = 100f;
    private float energy = 100f;
    private float age; // Synced in seconds
    private float ageInSeconds = 0.0f;
    private boolean alive = true;
    private String causeOfDeath;

    // Needs
    private float hunger;
    private float thirst;
    private float fatigue;

    // Carrying
    private CarriedItem carriedItem = CarriedItem.NONE;

    // Home position (nest)
    private float homeX, homeY, homeZ;
    // Life Cycle
    private LifeStage lifeStage = LifeStage.ADULT; // Default for now
    private Job job = Job.IDLE; // Default
    private float maturationThreshold = 172800.0f; // Seconds between stages (2 days)

    // Tree Climbing & Vegetation Interactivity
    private boolean climbingTree = false;
    private float treeClimbHeight = 0.0f;

    // Brain
    private org.swarmforge.core.behavior.ReasoningArchitecture brain;

    // Personality & Genetics
    private Personality personality;
    private Genome genome;
    private org.swarmforge.core.genetics.HaplodiploidGenome haplodiploidGenome;
    private org.swarmforge.core.species.Species species;

    // Memory optimization: Removed per-instance Random
    private AiState state = AiState.IDLE;
    private ResourceType carriedResourceType = null;

    // Cognitive LOD & Decision Caching Optimization
    private Action cachedAction = null;
    private long lastDecisionTick = -100;
    private int decisionInterval = 6; // Evaluate brain cognitive decision every 6 ticks (10 Hz decision rate at 60 TPS)

    public Action getCachedAction() { return cachedAction; }
    public void setCachedAction(Action action) { this.cachedAction = action; }
    public long getLastDecisionTick() { return lastDecisionTick; }
    public void setLastDecisionTick(long tick) { this.lastDecisionTick = tick; }
    public int getDecisionInterval() { return decisionInterval; }
    public void setDecisionInterval(int interval) { this.decisionInterval = Math.max(1, interval); }

    public int getDynamicDecisionInterval() {
        if (health < maxHealth * 0.5f || state == AiState.ATTACKING || state == AiState.FLEEING) {
            return 1; // Real-time high-priority decision frequency for combat / emergency (60 Hz)
        }
        if (state == AiState.RESTING || state == AiState.IDLE) {
            return 20; // Reduced decision frequency for idle / resting ants inside nest (3 Hz)
        }
        return decisionInterval; // Default standard frequency (10 Hz)
    }

    /**
     * Compact bitfield state packing for cache-friendly fast state checks.
     * bit 0: alive, bit 1: carryingFood, bit 2: climbingTree, bit 3: hasDisease
     */
    public int getPackedStateFlags() {
        int flags = 0;
        if (alive) flags |= 1;
        if (carriedResourceType != null) flags |= 2;
        if (climbingTree) flags |= 4;
        return flags;
    }

    // Combat Stats
    private float maxHealth = 100f;
    private float attackDamage = 5f;
    private float defense = 0f;

    // Cuticular Hydrocarbon (CHC) Gestalt Odor Profile
    private float[] chcProfile = new float[]{0.5f, 0.5f, 0.5f, 0.5f};

    public float[] getChcProfile() {
        return chcProfile;
    }

    public boolean isClimbingTree() {
        return climbingTree;
    }

    public float getTreeClimbHeight() {
        return treeClimbHeight;
    }

    public void climbTree(float targetHeight) {
        this.climbingTree = true;
        this.treeClimbHeight = Math.max(0.0f, targetHeight);
        this.z = Math.max(this.z, this.treeClimbHeight);
    }

    public void descendTree() {
        this.climbingTree = false;
        this.treeClimbHeight = 0.0f;
        this.z = 0.0f;
    }

    public boolean harvestPlant(org.swarmforge.core.world.VegetationSystem.Plant plant) {
        if (plant == null || !alive) return false;
        if (plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.GRASS ||
            plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.FLOWER) {
            float seeds = plant.harvestSeeds(0.5f);
            if (seeds > 0) {
                setCarriedItem(CarriedItem.FOOD);
                setCarriedResourceType(ResourceType.SEED);
                return true;
            }
        } else if (plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.TREE ||
                   plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB) {
            float foliage = plant.harvestFoliage(0.5f);
            if (foliage > 0) {
                climbTree(plant.getCurrentHeight() * 0.5f);
                setCarriedItem(CarriedItem.FOOD);
                setCarriedResourceType(ResourceType.LEAF);
                return true;
            }
        }
        return false;
    }

    public void setChcProfile(float[] profile) {
        this.chcProfile = profile;
    }

    // Disease Status - Handled by DiseaseManager externally

    /**
     * AI States for the FSM.
     */
    public enum AiState {
        IDLE,
        WANDER,
        FORAGE,
        RETURN_HOME,
        FLEE,
        FLEEING,
        ATTACKING,
        RESTING,
        TEND_BROOD,
        PATROL,
        DIG
    }

    /**
     * Caste types for eusocial insects.
     */
    public enum Caste {
        QUEEN,
        MALE,
        WORKER,
        SOLDIER,
        NURSE,
        FORAGER
    }

    /**
     * Items that can be carried.
     */
    public enum CarriedItem {
        NONE,
        FOOD,
        WATER,
        EARTH,
        BROOD,
        DEAD_ANT
    }

    /**
     * Life stages of an ant.
     */
    public enum LifeStage {
        EGG,
        LARVA,
        PUPA,
        ADULT
    }

    /**
     * Jobs for adult ants.
     */
    public enum Job {
        NONE, // For immature stages
        NURSE,
        BUILDER,
        FORAGER,
        GUARD,
        IDLE
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void updateJobByAge() {
        if (caste == Caste.WORKER || caste == Caste.FORAGER || caste == Caste.NURSE) {
            if (ageInSeconds < 300.0f) {
                this.job = Job.NURSE;
            } else if (ageInSeconds < 1000.0f) {
                this.job = Job.GUARD;
            } else {
                this.job = Job.FORAGER;
            }
        }
    }

    private CasteTemplate casteTemplate;

    public Individual(UUID colonyId, Caste caste, float x, float y, float z) {
        this.id = new UUID(java.util.concurrent.ThreadLocalRandom.current().nextLong(), java.util.concurrent.ThreadLocalRandom.current().nextLong());
        this.colonyId = colonyId;
        this.caste = caste;
        this.x = x;
        this.y = y;
        this.z = z;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.alive = true;
        this.health = 100f;
        this.maxHealth = 100f;
        this.energy = 100f;
        this.maxEnergy = 100f;
        this.hunger = 0f;
        this.thirst = 0f;
        this.fatigue = 0f;
    }

    public Individual(UUID colonyId, CasteTemplate template, float x, float y, float z) {
        this(colonyId, Caste.WORKER, x, y, z); // Default to WORKER enum for now
        this.casteTemplate = template;
        this.maxHealth = template.getBaseHealth();
        this.attackDamage = template.getBaseDamage();
        this.defense = template.getBaseDefense();
        this.health = this.maxHealth;
    }

    public CasteTemplate getCasteTemplate() {
        return casteTemplate;
    }

    public void setCasteTemplate(CasteTemplate casteTemplate) {
        this.casteTemplate = casteTemplate;
        if (casteTemplate != null) {
            this.maxHealth = casteTemplate.getBaseHealth();
            this.attackDamage = casteTemplate.getBaseDamage();
            this.defense = casteTemplate.getBaseDefense();
        }
    }

    // Getter for hunger needed by brain
    @Override
    public float getHunger() {
        return hunger;
    }

    public float getThirst() {
        return thirst;
    }

    public float getFatigue() {
        return fatigue;
    }

    public boolean canFly() {
        if (casteTemplate != null && (casteTemplate.isCanFly() || casteTemplate.canFly())) return true;
        if (species != null && species.isWorkersCanFly()) return true;
        if (caste == Caste.QUEEN || caste == Caste.MALE) return true;
        return false;
    }

    /**
     * Get ground walking/running speed based on species, caste, genome, Q10 thermal factor, and health state.
     */
    public float getWalkingSpeed() {
        float baseWorkerSpeed = species != null ? species.getWorkerSpeed() : 0.5f; // m/s scale
        float casteMult = 1.0f;
        if (casteTemplate != null && casteTemplate.getBodyLengthMm() > 0) {
            casteMult = (float) Math.pow(casteTemplate.getBodyLengthMm() / 4.0f, 0.4);
        } else {
            casteMult = switch (caste) {
                case QUEEN -> 0.75f;
                case MALE -> 0.90f;
                case SOLDIER -> 1.10f;
                case FORAGER -> 1.05f;
                case NURSE -> 0.85f;
                case WORKER -> 1.00f;
            };
        }
        float speed = baseWorkerSpeed * casteMult * getQ10ThermalFactor();
        if (genome != null) {
            speed *= genome.getSpeedMultiplier();
        }
        if (health < maxHealth && maxHealth > 0f) {
            speed *= (0.5f + 0.5f * (health / maxHealth));
        }
        return Math.max(0.02f, speed);
    }

    /**
     * Get 3D flying speed based on species wingbeat frequency, caste alate status, hovering capability, and individual state.
     */
    public float getFlyingSpeed() {
        if (!canFly()) return getWalkingSpeed();
        
        float wingbeatHz = species != null ? species.getWingbeatFrequencyHz() : 180.0f;
        if (casteTemplate != null && casteTemplate.getWingbeatFrequencyHz() > 0) {
            wingbeatHz = casteTemplate.getWingbeatFrequencyHz();
        }
        
        float baseFlySpeed = getWalkingSpeed() * 2.5f * (Math.max(50.0f, wingbeatHz) / 180.0f);
        if (species != null && species.hasHoveringCapability()) {
            baseFlySpeed *= 1.2f;
        }
        return Math.max(0.08f, baseFlySpeed);
    }

    /**
     * Get current active movement speed depending on flight status (walking vs 3D flight).
     */
    public float getCurrentMovementSpeed() {
        if (canFly() && z > 0.1f) {
            return getFlyingSpeed();
        }
        return getWalkingSpeed();
    }

    /**
     * Get dynamic effective metabolic consumption multiplier based on species, daily food requirement, caste, activity, and temperature.
     */
    public float getEffectiveMetabolismRate() {
        float speciesMetabolism = species != null ? species.getMetabolism() : 1.0f;
        float dailyConsumption = species != null ? species.getDailyFoodConsumption() : 0.3f;

        float casteMetabolicFactor = switch (caste) {
            case QUEEN -> 2.2f;
            case SOLDIER -> 1.3f;
            case MALE -> 0.8f;
            case FORAGER -> 1.1f;
            case NURSE, WORKER -> 1.0f;
        };

        float activityFactor = 1.0f;
        if (canFly() && z > 0.1f) {
            activityFactor = 3.5f;
        } else if (job == Job.BUILDER || job == Job.GUARD) {
            activityFactor = 1.3f;
        } else if (state == AiState.IDLE || state == AiState.TEND_BROOD) {
            activityFactor = 0.6f;
        }

        float effectiveMetabolism = speciesMetabolism * dailyConsumption * casteMetabolicFactor * activityFactor * getQ10ThermalFactor();
        if (genome != null) {
            effectiveMetabolism *= genome.getMetabolismRate();
        }
        return Math.max(0.1f, effectiveMetabolism);
    }

    /**
     * Get payload carrying capacity ratio based on species payload ratio, strength, and caste.
     */
    public float getPayloadCapacity() {
        float basePayload = species != null ? species.getMaxCarryingPayloadRatio() : 5.0f;
        if (casteTemplate != null && casteTemplate.getMaxCarryingPayloadRatio() > 0) {
            basePayload = casteTemplate.getMaxCarryingPayloadRatio();
        }
        float strength = species != null ? species.getStrength() : 5.0f;
        return basePayload * (strength / 5.0f);
    }

    /**
     * Get visual perception distance based on species view distance, visual acuity, and light levels.
     */
    public float getVisionDistance() {
        float baseView = species != null ? species.getViewDistance() : 6.0f;
        float acuity = species != null ? species.getVisualAcuity() : 1.0f;
        return baseView * Math.max(0.5f, acuity);
    }

    public void fly3D(float targetX, float targetY, float targetZ, float speedParam) {
        if (!alive) return;
        float dx = targetX - x;
        float dy = targetY - y;
        float dz = targetZ - z;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 0.001f) return;

        float effectiveSpeed = getFlyingSpeed();
        if (speedParam > 0) {
            effectiveSpeed = Math.min(effectiveSpeed, speedParam);
        }
        float step = Math.min(dist, effectiveSpeed * 0.1f);
        x += (dx / dist) * step;
        y += (dy / dist) * step;
        z += (dz / dist) * step;
        heading = (float) Math.atan2(dy, dx);

        float wingbeatHz = species != null ? species.getWingbeatFrequencyHz() : 180.0f;
        if (casteTemplate != null && casteTemplate.getWingbeatFrequencyHz() > 0) {
            wingbeatHz = casteTemplate.getWingbeatFrequencyHz();
        }
        energy -= 0.0003f * (wingbeatHz / 180.0f) * getEffectiveMetabolismRate() * step;
    }

    private float ambientTemperatureC = 24.0f;
    private float ambientHumidityPercent = 80.0f;

    public float getAmbientTemperatureC() {
        return ambientTemperatureC;
    }

    public void setAmbientTemperatureC(float tempC) {
        this.ambientTemperatureC = tempC;
    }

    public float getAmbientHumidityPercent() {
        return ambientHumidityPercent;
    }

    public void setAmbientHumidityPercent(float humidity) {
        this.ambientHumidityPercent = humidity;
    }

    /**
     * Compute thermodynamic response factor using asymmetric Schoolfield thermal reaction norm kinetics.
     * Incorporates rapid enzymatic inactivation near Critical Thermal Maximum (CTmax).
     */
    public float getQ10ThermalFactor() {
        float optTemp = species != null ? species.getOptimalTempCelsius() : 24.0f;
        float minTemp = species != null ? species.getMinTempCelsius() : 10.0f;
        float maxTemp = species != null ? species.getMaxTempCelsius() : 40.0f;
        
        float diff = ambientTemperatureC - optTemp;
        if (diff <= 0) {
            float sigmaLow = Math.max(1.0f, 0.4f * (optTemp - minTemp));
            return Math.max(0.05f, (float) Math.exp(-(diff * diff) / (2.0f * sigmaLow * sigmaLow)));
        } else {
            float sigmaHigh = Math.max(0.5f, 0.2f * (maxTemp - optTemp));
            float q10 = (float) Math.exp(-(diff * diff) / (2.0f * sigmaHigh * sigmaHigh));
            if (ambientTemperatureC >= maxTemp) return 0.05f;
            return Math.max(0.05f, Math.min(1.2f, q10));
        }
    }

    /**
     * Get biomechanically accurate attack damage based on mandibular biting force (MPa) and muscle strength.
     */
    public float getAttackDamage() {
        float mandibularForce = species != null ? species.getMandibularBitingForceMPa() : 15.0f;
        if (casteTemplate != null && casteTemplate.getMandibularBitingForceMPa() > 0) {
            mandibularForce = casteTemplate.getMandibularBitingForceMPa();
        }
        float strength = species != null ? species.getStrength() : 5.0f;
        float casteMult = (caste == Caste.SOLDIER) ? 2.5f : ((caste == Caste.QUEEN) ? 1.5f : 1.0f);
        return casteMult * (mandibularForce / 15.0f) * (strength / 5.0f) * attackDamage;
    }

    /**
     * Update position based on heading and current dynamic movement speed.
     */
    public void move(float speedMult) {
        float effectiveSpeed = getCurrentMovementSpeed() * 0.1f * Math.max(0.1f, speedMult);
        x += Math.cos(heading) * effectiveSpeed;
        y += Math.sin(heading) * effectiveSpeed;
        if (canFly() && z > 0.0f) {
            z += (float) (Math.sin(age * 0.2f) * 0.05f);
        }
    }

    /**
     * Turn towards a target heading.
     */
    public void turnTowards(float targetHeading, float turnRate) {
        float diff = targetHeading - heading;
        while (diff > Math.PI)
            diff -= 2 * Math.PI;
        while (diff < -Math.PI)
            diff += 2 * Math.PI;
        heading += Math.signum(diff) * Math.min(Math.abs(diff), turnRate);
    }

    /**
     * Increment age by 1 tick.
     */
    public void incrementAge() {
        this.ageInSeconds += 1.0f;
        this.age = this.ageInSeconds;
    }

    /**
     * Consume energy and update needs based dynamically on species, caste, and activity metabolism.
     */
    public void tick() {
        tick(0.016666667f);
    }

    public void tick(float deltaSeconds) {
        if (!alive)
            return;
        age++;
        ageInSeconds += deltaSeconds;

        float effectiveMetabolism = getEffectiveMetabolismRate();
        float waterReq = species != null ? species.getWaterRequirement() : 0.15f;

        // Humidity factor on thirst accumulation (high ambient moisture reduces transpiration/thirst)
        float humidityThirstMult = Math.max(0.2f, 1.8f - (ambientHumidityPercent / 100.0f) * 1.6f);

        energy -= 0.006f * effectiveMetabolism * deltaSeconds;
        hunger += 0.0048f * effectiveMetabolism * deltaSeconds;
        thirst += 0.0024f * effectiveMetabolism * (waterReq / 0.15f) * humidityThirstMult * deltaSeconds;

        float minTemp = species != null ? species.getMinTempCelsius() : 10.0f;
        float maxTemp = species != null ? species.getMaxTempCelsius() : 40.0f;
        float thermalExcess = 0.0f;
        if (ambientTemperatureC < minTemp - 5.0f) {
            thermalExcess = (minTemp - 5.0f) - ambientTemperatureC;
        } else if (ambientTemperatureC > maxTemp + 5.0f) {
            thermalExcess = ambientTemperatureC - (maxTemp + 5.0f);
        }
        if (thermalExcess > 0.0f) {
            float damageRate = Math.min(8.0f, thermalExcess * 0.4f);
            takeDamage(damageRate * deltaSeconds, "Thermal Extremes (Extreme Heat/Cold)");
        }

        float minHum = species != null ? species.getMinHumidityPercent() : 25.0f;
        float maxHum = species != null ? species.getMaxHumidityPercent() : 95.0f;
        float humidityExcess = 0.0f;
        if (ambientHumidityPercent < minHum - 10.0f) {
            humidityExcess = (minHum - 10.0f) - ambientHumidityPercent;
        } else if (ambientHumidityPercent > maxHum + 5.0f) {
            humidityExcess = ambientHumidityPercent - (maxHum + 5.0f);
        }
        if (humidityExcess > 0.0f) {
            float desiccationDamageRate = Math.min(4.0f, humidityExcess * 0.15f);
            takeDamage(desiccationDamageRate * deltaSeconds, "Desiccation (Extreme Dryness/Humidity)");
        }

        // Gas hypercapnia (CO2 > 25,000 ppm) or Hypoxia (O2 < 12%) in unventilated subterranean galleries
        float co2Ppm = 400.0f;
        float o2Percent = 21.0f;
        if (co2Ppm > 25000.0f) {
            float gasDamage = Math.min(3.0f, (co2Ppm - 25000.0f) / 10000.0f);
            takeDamage(gasDamage * deltaSeconds, "Hypercapnia / Gas Asphyxiation");
        }
        if (o2Percent < 12.0f) {
            float hypoxiaDamage = Math.min(3.0f, (12.0f - o2Percent) * 0.5f);
            takeDamage(hypoxiaDamage * deltaSeconds, "Hypoxia / Oxygen Depletion");
        }

        if (energy <= 0 || hunger >= 100) {
            die("Starvation / Energy Exhaustion");
            return;
        }
        if (thirst >= 100) {
            die("Severe Dehydration");
            return;
        }

        float maxLifespanDays = getMaxLifespan();
        float maxLifespanSeconds = maxLifespanDays * 86400.0f;

        if (ageInSeconds >= maxLifespanSeconds) {
            die("Old Age (Natural end of life)");
            return;
        }

        if (species != null && species.hasMandibularWearPolyethism() && (job == Job.BUILDER || caste == Caste.FORAGER)) {
            mandibleWear = Math.min(1.0f, mandibleWear + 0.0001f * deltaSeconds);
            if (mandibleWear >= 0.8f) {
                this.job = Job.NURSE;
            }
        }
    }

    private float mandibleWear = 0.0f;
    public float getMandibleWear() { return mandibleWear; }

    public UUID getId() {
        return id;
    }

    public UUID getColonyId() {
        return colonyId;
    }

    public Caste getCaste() {
        return caste;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getHeading() {
        return heading;
    }

    public float getHealth() {
        return health;
    }

    public float getEnergy() {
        return energy;
    }

    public float getAge() {
        return ageInSeconds;
    }

    public void setAge(float ageInSeconds) {
        this.ageInSeconds = Math.max(0f, ageInSeconds);
        this.age = this.ageInSeconds;
    }

    public float getAgeInSeconds() {
        return ageInSeconds;
    }

    public void setAgeInSeconds(float ageInSeconds) {
        this.ageInSeconds = Math.max(0f, ageInSeconds);
        this.age = this.ageInSeconds;
    }

    public float getMaxLifespan() {
        if (casteTemplate != null && casteTemplate.getLifespan() > 0) {
            return casteTemplate.getLifespan();
        }
        if (species != null) {
            return switch (caste) {
                case QUEEN -> species.getQueenLifespan();
                case SOLDIER -> (float) Math.round(species.getWorkerLifespan() * 1.4f);
                case MALE -> (float) Math.round(species.getWorkerLifespan() * 0.35f);
                case WORKER, FORAGER, NURSE -> species.getWorkerLifespan();
            };
        }
        return switch (caste) {
            case QUEEN -> 5475f; // ~15 years default
            case SOLDIER -> 1000f;
            case MALE -> 30f;
            case WORKER, FORAGER, NURSE -> 730f; // ~2 years default
        };
    }

    public boolean isAlive() {
        return alive;
    }

    public CarriedItem getCarriedItem() {
        return carriedItem;
    }

    public float getHomeX() {
        return homeX;
    }

    public float getHomeY() {
        return homeY;
    }

    public float getHomeZ() {
        return homeZ;
    }

    public void setHomePosition(float homeX, float homeY, float homeZ) {
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
    }
    
    @Override
    public boolean isAtNest() {
        return (Math.abs(x - homeX) < 2.0 && Math.abs(y - homeY) < 2.0);
    }

    @Override
    public float getEnergyLevel() {
        return (float) (energy / maxEnergy);
    }

    @Override
    public boolean isSoldier() {
        return caste == Caste.SOLDIER;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        if (species != null) {
            return species.getForagingTypes();
        }
        return java.util.Set.of(ResourceType.SEED);
    }

    @Override
    public String getAgentId() {
        return id.toString();
    }

    public boolean isCarryingFood() {
        return carriedItem == CarriedItem.FOOD;
    }

    // Setters
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
        if (this.health <= 0 && alive) {
            die("Lethal Injuries / Physical Trauma");
        }
    }

    public void setEnergy(float energy) {
        this.energy = Math.max(0, Math.min(maxEnergy, energy));
    }

    public void setMaxEnergy(float maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public void setHunger(float hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public void setThirst(float thirst) {
        this.thirst = Math.max(0, Math.min(100, thirst));
    }

    public void setCarriedItem(CarriedItem item) {
        this.carriedItem = item;
        if (item == CarriedItem.NONE) {
            this.carriedResourceType = null;
        }
    }

    public ResourceType getCarriedResourceType() {
        return carriedResourceType;
    }

    public void setCarriedResourceType(ResourceType type) {
        this.carriedResourceType = type;
    }

    public org.swarmforge.core.behavior.ReasoningArchitecture getBrain() {
        return brain;
    }

    public void setBrain(org.swarmforge.core.behavior.ReasoningArchitecture brain) {
        this.brain = brain;
    }

    public void dropFood() {
        if (carriedItem == CarriedItem.FOOD) {
            carriedItem = CarriedItem.NONE;
            // Logic for actually placing food in world should be handled by Simulation
            // event or caller
        }
    }

    public void setReasoningArchitecture(org.swarmforge.core.behavior.ReasoningArchitecture reasoningArchitecture) {
        this.brain = reasoningArchitecture;
    }

    public org.swarmforge.core.behavior.ReasoningArchitecture getReasoningArchitecture() {
        return brain;
    }

    public AiState getState() {
        return state;
    }

    public void setState(AiState state) {
        this.state = state;
    }

    private java.util.Random random;

    public void setRandom(java.util.Random random) {
        this.random = random;
    }

    public java.util.Random getRandom() {
        if (random != null) {
            return random;
        }
        long seed = id != null ? id.getLeastSignificantBits() : 1337L;
        return new java.util.Random(seed);
    }

    public org.swarmforge.core.species.Species getSpecies() {
        return species;
    }

    public void setSpecies(org.swarmforge.core.species.Species species) {
        this.species = species;
    }

    public LifeStage getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(LifeStage lifeStage) {
        this.lifeStage = lifeStage;
    }

    public float getMaturationThreshold() {
        return maturationThreshold;
    }

    public void setMaturationThreshold(float maturationThreshold) {
        this.maturationThreshold = maturationThreshold;
    }

    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public void setDefense(float defense) {
        this.defense = defense;
    }

    /**
     * Apply damage to this individual.
     * 
     * @param amount Damage amount to apply
     * @return true if the individual died from this damage
     */
    public boolean takeDamage(float amount) {
        return takeDamage(amount, "Physical Trauma / Damage");
    }

    public boolean takeDamage(float amount, String cause) {
        if (!alive)
            return false;
        health -= amount;
        if (health <= 0) {
            health = 0;
            die(cause != null ? cause : "Physical Trauma / Damage");
            return true;
        }
        return false;
    }

    /**
     * Heal this individual.
     * 
     * @param amount Amount to heal
     */
    public void heal(float amount) {
        if (!alive)
            return;
        health = Math.min(100f, health + amount);
    }

    /**
     * Execute an action decided by the brain.
     * 
     * @param action The action to execute
     * @param colony The colony (for depositing resources)
     * @return Result of the action
     */
    public ActionResult executeAction(Action action, org.swarmforge.core.domain.Colony colony) {
        if (!alive)
            return ActionResult.failure("Dead");

        switch (action.type()) {
            case MOVE -> {
                // Normalized & speed-scaled directional move
                float dx = action.directionX();
                float dy = action.directionY();
                float len = (float) Math.hypot(dx, dy);
                if (len > 0.0001f) {
                    float moveSpeed = getCurrentMovementSpeed();
                    float baseStep = Math.max(0.02f, moveSpeed * 0.1f);
                    float step = Math.min(baseStep * Math.max(0.1f, action.intensity()), 0.25f);
                    this.x += (dx / len) * step;
                    this.y += (dy / len) * step;
                    this.heading = (float) Math.atan2(dy, dx);
                }
                return ActionResult.ok();
            }
            case FORAGE -> {
                return ActionResult.ok(); // Intent registered
            }
            case RETURN_HOME, ABORT_AND_RETURN -> {
                turnTowards(getHomeX(), getHomeY(), 0.1f);
                move(1.0f);
                if (isAtNest() && colony != null) {
                    if (colony.getFoodStored() > 0.1f) {
                        if (colony.getCarbohydrateStored() > 0.05f) {
                            colony.setCarbohydrateStored(colony.getCarbohydrateStored() - 0.05f);
                        } else if (colony.getProteinStored() > 0.05f) {
                            colony.setProteinStored(colony.getProteinStored() - 0.05f);
                        } else {
                            colony.consumeResource(ResourceType.SEED, 0.05f);
                        }
                        this.hunger = Math.max(0f, this.hunger - 15.0f);
                        this.energy = Math.min(this.maxEnergy, this.energy + 15.0f);
                    }
                    if (colony.getResourceAmount(ResourceType.WATER) > 0.1f) {
                        colony.consumeResource(ResourceType.WATER, 0.05f);
                        this.thirst = Math.max(0f, this.thirst - 15.0f);
                    }
                }
                return ActionResult.ok();
            }
            case REST -> {
                energy = Math.min(maxEnergy, energy + 5.0f);
                hunger = Math.max(0f, hunger - 5.0f);
                thirst = Math.max(0f, thirst - 5.0f);
                return ActionResult.ok();
            }
            case ATTACK -> {
                if (action.target() instanceof Individual target) {
                    // Diplomacy Check
                    if (colony.getDiplomacy().isAlly(target.getColonyId())) {
                        return ActionResult.failure("Target is an Ally!");
                    }

                    // Range Check
                    float dx = target.getX() - x;
                    float dy = target.getY() - y;
                    float dz = target.getZ() - z;
                    float distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq < 4.0f) { // Range 2.0
                        target.takeDamage(this.attackDamage, this);
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
                    float workerSpeed = species != null ? species.getWorkerSpeed() : 0.5f;
                    float baseStep = Math.max(0.02f, workerSpeed * 0.1f) * getQ10ThermalFactor();
                    float step = Math.min(baseStep * Math.max(0.1f, action.intensity()), 0.15f);
                    this.x += (dx / len) * step;
                    this.y += (dy / len) * step;
                    this.heading = (float) Math.atan2(dy, dx);
                }
                return ActionResult.ok();
            }
            case DEPOSIT_FOOD -> {
                if (isCarryingFood() && colony != null) {
                    if (carriedResourceType != null) {
                        colony.addResource(carriedResourceType, 1.0f);
                    } else {
                        // Default to SEED if unspecified
                        colony.addResource(ResourceType.SEED, 1.0f);
                    }
                    setCarriedItem(CarriedItem.NONE);
                    // Ants feed while depositing food at the nest
                    this.hunger = 0f;
                    this.thirst = 0f;
                    this.energy = this.maxEnergy;
                    return ActionResult.ok();
                }
                return ActionResult.failure("Not carrying food");
            }
            default -> {
                return ActionResult.ok();
            }
        }
    }

    private void turnTowards(float targetX, float targetY, float rate) {
        float dx = targetX - x;
        float dy = targetY - y;
        float targetHeading = (float) Math.atan2(dy, dx);
        turnTowards(targetHeading, rate);
    }

    // === Personality & Genetics ===

    public Personality getPersonality() {
        return personality;
    }

    public void setPersonality(Personality p) {
        this.personality = p;
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome g) {
        this.genome = g;
    }

    public org.swarmforge.core.genetics.HaplodiploidGenome getHaplodiploidGenome() {
        return haplodiploidGenome;
    }

    public void setHaplodiploidGenome(org.swarmforge.core.genetics.HaplodiploidGenome g) {
        this.haplodiploidGenome = g;
    }

    /**
     * Initialize genetics for this individual (call after construction).
     */
    /**
     * Initialize genetics for this individual (call after construction).
     */
    public void initializeGenetics(java.util.Random rng) {
        this.genome = new Genome();
        genome.mutate(0.1f); // Initial variation
        // Personality derived from genome? For now keep separate or link them later.
        // this.personality = new Personality(genome);
    }

    /**
     * Create offspring genetics from two parents.
     */
    public void inheritGenetics(Individual parent1, Individual parent2, java.util.Random rng) {
        if (parent1.genome != null && parent2.genome != null) {
            this.genome = Genome.crossover(parent1.genome, parent2.genome);
            this.genome.mutate(0.05f); // Small mutation chance
        } else {
            initializeGenetics(rng);
        }
    }

    public void update(org.swarmforge.core.structure.ConstructionManager constructionManager, float deltaSeconds) {
        if (!alive || lifeStage != LifeStage.ADULT)
            return;

        jobUpdate(constructionManager);

        // Energy consumption
        tick(deltaSeconds);
    }

    public void update(org.swarmforge.core.structure.ConstructionManager constructionManager) {
        update(constructionManager, 0.016666667f);
    }

    private void jobUpdate(org.swarmforge.core.structure.ConstructionManager constructionManager) {
        if (job == Job.BUILDER) {
            java.util.Optional<org.swarmforge.core.structure.ConstructionTask> taskOpt = constructionManager
                    .getAvailableTask(x, y, z);
            if (taskOpt.isPresent()) {
                org.swarmforge.core.structure.ConstructionTask task = taskOpt.get();
                if (!task.isAssigned()) {
                    task.setAssigned(true);
                    // Move to task
                    float dx = task.getX() - x;
                    float dy = task.getY() - y;
                    float dz = task.getZ() - z;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (dist < 1.0f) {
                        task.work(1.0f); // Work on task
                    } else {
                        // Move towards
                        this.heading = (float) Math.atan2(dy, dx);
                        // Z movement? For now 2D movement logic only
                    }
                }
            }
        }
    }

    public static String formatCasteFr(Caste c) {
        if (c == null) return "Individu";
        return switch (c) {
            case QUEEN -> "Reine";
            case SOLDIER -> "Soldat";
            case MALE -> "Mâle";
            default -> "Ouvrière";
        };
    }

    // Combat Logic
    public void takeDamage(float amount, Individual attacker) {
        float effectiveDamage = Math.max(0, amount - this.defense);
        this.health -= effectiveDamage;

        if (attacker != null) {
            String attShort = "#" + attacker.getId().toString().substring(0, 8).toUpperCase();
            String defShort = "#" + this.getId().toString().substring(0, 8).toUpperCase();
            String attCaste = formatCasteFr(attacker.getCaste());
            String defCaste = formatCasteFr(this.caste);
            int xPos = (int) this.x;
            int yPos = (int) this.y;
            int zPos = (int) this.z;

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("attackerId", attShort);
            data.put("attackerCaste", attacker.getCaste().name());
            data.put("defenderId", defShort);
            data.put("defenderCaste", this.caste.name());
            data.put("damage", Math.round(effectiveDamage * 10.0f) / 10.0f);
            data.put("healthRemaining", Math.max(0, Math.round(this.health * 10.0f) / 10.0f));
            data.put("x", xPos);
            data.put("y", yPos);
            data.put("z", zPos);

            String msg = String.format("Combat : %s %s a infligé %.1f dégâts à %s %s à (%d, %d, %d) [PV restant: %.1f]",
                    attCaste, attShort, effectiveDamage, defCaste, defShort, xPos, yPos, zPos, Math.max(0, this.health));

            org.swarmforge.core.event.SimulationEvent evt = org.swarmforge.core.event.SimulationEvent.obtain(
                    org.swarmforge.core.event.SimulationEvent.EventType.COMBAT_OCCURRED,
                    org.swarmforge.core.event.SimulationEvent.Severity.WARNING,
                    0, msg, data);
            org.swarmforge.core.event.EventBus.getInstance().publish(evt);
        }

        if (this.health <= 0) {
            String attackerName = (attacker != null) ? (attacker.getCaste() + " #" + attacker.getId().toString().substring(0, 5)) : "Ennemi";
            die("Combat contre " + attackerName);
        }
    }

    public void die() {
        die(this.causeOfDeath != null && !"Inconnue".equals(this.causeOfDeath) ? this.causeOfDeath : "Old Age / Natural Exhaustion");
    }

    public void die(String cause) {
        this.alive = false;
        if (this.causeOfDeath == null || "Inconnue".equals(this.causeOfDeath) || "UNKNOWN".equalsIgnoreCase(this.causeOfDeath)) {
            this.causeOfDeath = (cause != null && !cause.isBlank()) ? cause : "Old Age / Natural Exhaustion";
        }
    }

    public String getCauseOfDeath() {
        if (this.causeOfDeath == null || "Inconnue".equals(this.causeOfDeath) || "UNKNOWN".equalsIgnoreCase(this.causeOfDeath)) {
            return "Old Age / Natural Exhaustion";
        }
        return causeOfDeath;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    // === Environmental Sensory Perception (World & Weather Sampling) ===

    /**
     * Samples the local voxel terrarium and atmospheric weather using the species' sensory parameters.
     */
    public SensorySample sampleSensoryEnvironment(Terrarium terrarium, org.swarmforge.core.world.WeatherSystem weather) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);

        TerrariumCell currentCell = terrarium != null ? terrarium.getCell(ix, iy, iz) : null;

        float feltTemp = currentCell != null ? currentCell.temperature() : 20.0f;
        float feltHumidity = currentCell != null ? currentCell.humidity() : 0.5f;
        float feltCo2Ppm = currentCell != null ? currentCell.co2() : 400.0f;
        float feltLightLux = currentCell != null ? currentCell.light() * 1000.0f : 500.0f;

        // Magnetoreception
        float geomagIntensity = (weather != null && species != null && species.hasMagnetoreception())
                ? weather.getMagneticField() : 0.0f;

        // Substrate Vibration
        float vibrationDb = (species != null && species.hasSubstrateVibrationSensing()) ? 12.0f : 0.0f;

        // Electroception
        float electricFieldVolts = (weather != null && species != null && species.hasElectrosensing())
                ? (weather.getWeatherState() == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM ? 300.0f : 20.0f) : 0.0f;

        // Polarized UV Compass Angle
        float solarAzimuthRad = (weather != null && species != null && species.hasPolarizedLightNavigation())
                ? (float) Math.toRadians(weather.getSunAngle() * 15.0f) : heading;

        return new SensorySample(feltTemp, feltHumidity, feltCo2Ppm, feltLightLux, geomagIntensity, vibrationDb, electricFieldVolts, solarAzimuthRad);
    }

    public record SensorySample(
        float temperatureCelsius,
        float humidityRelative,
        float co2Ppm,
        float lightLux,
        float geomagIntensityMicrotesla,
        float vibrationDb,
        float electricFieldVoltsPerMeter,
        float solarAzimuthRad
    ) {}

    /**
     * Returns a active biological behavior flags summary for mouseover / inspector HUD rendering.
     */
    public String getActiveBehaviorsSummary() {
        if (species == null) return "STANDARD_PATROL";
        java.util.List<String> list = new java.util.ArrayList<>();
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
        if (species.canPerformCrècheRegurgitationSpider()) list.add("SPIDER_CRÈCHE_REGURGITATION");
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

        // Batch XV (161-180)
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

        // Batch XVI (181-200)
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

        // Batch XVII (201-220)
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
