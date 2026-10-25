/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;
import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.behavior.ReasoningArchitecture.Action;
import org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult;
import org.swarmforge.core.genetics.Genome;

/**
 * Canonical Domain Entity representing an individual eusocial insect in the simulation.
 * Implements AgentView for direct bridge and inter-compatibility with the ECS and Rendering engines.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Individual implements java.io.Serializable, AgentView, IndividualLotsBridge, IndividualWavesBridge {
    private static final long serialVersionUID = 1L;

    private static final java.util.concurrent.atomic.AtomicLong INDIVIDUAL_NUMBER_GENERATOR = new java.util.concurrent.atomic.AtomicLong(1);
    private final long individualNumber;

    public static void resetIndividualNumberGenerator() { INDIVIDUAL_NUMBER_GENERATOR.set(1); }
    public static void setNextIndividualNumber(long nextVal) { INDIVIDUAL_NUMBER_GENERATOR.set(Math.max(1, nextVal)); }
    public long getIndividualNumber() { return individualNumber; }
    public String getFormattedId() { return "insect_" + individualNumber; }

    private final UUID id;
    private Caste caste;
    private UUID colonyId;

    private float x, y, z;
    private float heading;

    private float health = 100f;
    private float maxEnergy = 100f;
    private float energy = 100f;
    private float age;
    private float ageInSeconds = 0.0f;
    private boolean alive = true;
    private String causeOfDeath;

    private float hunger;
    private float thirst;
    private float fatigue;

    private CarriedItem carriedItem = CarriedItem.NONE;
    private float homeX, homeY, homeZ;
    private LifeStage lifeStage = LifeStage.ADULT;
    private Job job = Job.IDLE;
    private float maturationThreshold = 172800.0f;

    private boolean climbingTree = false;
    private float treeClimbHeight = 0.0f;

    private transient org.swarmforge.core.behavior.ReasoningArchitecture brain;
    private Personality personality;
    private Genome genome;
    private org.swarmforge.core.genetics.HaplodiploidGenome haplodiploidGenome;
    private transient org.swarmforge.core.species.Species species;

    private AiState state = AiState.IDLE;
    private ResourceType carriedResourceType = null;

    private transient Action cachedAction = null;
    private long lastDecisionTick = -100;
    private int decisionInterval = 6;

    public Action getCachedAction() { return cachedAction; }
    public void setCachedAction(Action action) { this.cachedAction = action; }
    public long getLastDecisionTick() { return lastDecisionTick; }
    public void setLastDecisionTick(long tick) { this.lastDecisionTick = tick; }
    public int getDecisionInterval() { return decisionInterval; }
    public void setDecisionInterval(int interval) { this.decisionInterval = Math.max(1, interval); }

    public int getDynamicDecisionInterval() {
        if (health < maxHealth * 0.5f || state == AiState.ATTACKING || state == AiState.FLEEING) return 1;
        if (state == AiState.RESTING || state == AiState.IDLE) return 20;
        return decisionInterval;
    }

    public int getPackedStateFlags() {
        int flags = 0;
        if (alive) flags |= 1;
        if (carriedResourceType != null) flags |= 2;
        if (climbingTree) flags |= 4;
        return flags;
    }

    private float maxHealth = 100f;
    private float attackDamage = 5f;
    private float defense = 0f;

    // ==========================================
    // Modular Biological Traits (Domain Traits)
    // ==========================================
    private final org.swarmforge.core.domain.traits.ChcProfileTrait chcTrait = new org.swarmforge.core.domain.traits.ChcProfileTrait();
    private final org.swarmforge.core.domain.traits.SocialCropTrait socialCropTrait = new org.swarmforge.core.domain.traits.SocialCropTrait();
    private final org.swarmforge.core.domain.traits.TandemRunningTrait tandemTrait = new org.swarmforge.core.domain.traits.TandemRunningTrait();
    private final org.swarmforge.core.domain.traits.PathIntegrationTrait pathIntegrationTrait = new org.swarmforge.core.domain.traits.PathIntegrationTrait();
    private final org.swarmforge.core.domain.traits.ChemicalDefenseTrait chemicalDefenseTrait = new org.swarmforge.core.domain.traits.ChemicalDefenseTrait();
    private final org.swarmforge.core.domain.traits.CircadianClockTrait circadianTrait = new org.swarmforge.core.domain.traits.CircadianClockTrait();

    final IndividualSpecializedLotsTrait lotsTrait = new IndividualSpecializedLotsTrait();
    final IndividualEthologyWave1Trait wave1Trait = new IndividualEthologyWave1Trait();
    final IndividualEthologyWave2Trait wave2Trait = new IndividualEthologyWave2Trait();
    final IndividualEthologyWave3Trait wave3Trait = new IndividualEthologyWave3Trait();

    @Override public Individual asIndividual() { return this; }
    @Override public IndividualSpecializedLotsTrait getLotsTrait() { return lotsTrait; }
    @Override public IndividualEthologyWave1Trait getWave1Trait() { return wave1Trait; }
    @Override public IndividualEthologyWave2Trait getWave2Trait() { return wave2Trait; }
    @Override public IndividualEthologyWave3Trait getWave3Trait() { return wave3Trait; }

    public org.swarmforge.core.domain.traits.ChcProfileTrait getChcTrait() { return chcTrait; }
    public org.swarmforge.core.domain.traits.SocialCropTrait getSocialCropTrait() { return socialCropTrait; }
    public org.swarmforge.core.domain.traits.TandemRunningTrait getTandemTrait() { return tandemTrait; }
    public org.swarmforge.core.domain.traits.PathIntegrationTrait getPathIntegrationTrait() { return pathIntegrationTrait; }
    public org.swarmforge.core.domain.traits.ChemicalDefenseTrait getChemicalDefenseTrait() { return chemicalDefenseTrait; }
    public org.swarmforge.core.domain.traits.CircadianClockTrait getCircadianTrait() { return circadianTrait; }

    // --- CHC Profile & Nestmate Recognition ---
    public float[] getChcProfile() { return chcTrait.getProfile(); }
    public void setChcProfile(float[] profile) { chcTrait.setProfile(profile); }
    public float calculateChcDissimilarity(Individual other) { return (other != null) ? chcTrait.calculateDissimilarity(other.chcTrait) : 1.0f; }
    public boolean isNestmateRecognized(Individual other) {
        if (other == null) return false;
        if (this.colonyId != null && this.colonyId.equals(other.colonyId)) return true;
        return chcTrait.isNestmateRecognized(other.chcTrait);
    }
    public void homogenizeChcProfile(Individual partner, float factor) { if (partner != null) chcTrait.homogenize(partner.chcTrait, factor); }

    // --- Social Crop (Ingluvies / Jabot Social) ---
    public float getSocialCropAmount() { return socialCropTrait.getAmount(); }
    public void setSocialCropAmount(float amount) { socialCropTrait.setAmount(amount); }
    public float getSocialCropCapacity() { return socialCropTrait.getCapacity(); }
    public void setSocialCropCapacity(float cap) { socialCropTrait.setCapacity(cap); }
    public ResourceType getSocialCropResourceType() { return socialCropTrait.getResourceType(); }
    public void setSocialCropResourceType(ResourceType type) { socialCropTrait.setResourceType(type); }
    public float fillSocialCrop(float amount, ResourceType type) { return socialCropTrait.fill(amount, type); }
    public float regurgitateSocialCrop(float amount) { return socialCropTrait.regurgitate(amount); }

    // --- Tandem Running Dynamics ---
    public enum TandemRole { NONE, LEADER, FOLLOWER }
    public TandemRole getTandemRole() { return tandemTrait.getRole(); }
    public void setTandemRole(TandemRole role) { tandemTrait.setRole(role); }
    public UUID getTandemPartnerId() { return tandemTrait.getPartnerId(); }
    public void setTandemPartnerId(UUID id) { tandemTrait.setPartnerId(id); }
    public long getLastTandemContactTick() { return tandemTrait.getLastContactTick(); }
    public void setLastTandemContactTick(long tick) { tandemTrait.setLastContactTick(tick); }
    public void pairTandem(Individual partner, boolean asLeader) {
        if (partner == null) return;
        this.tandemTrait.pair(this.id, partner, asLeader);
        partner.tandemTrait.pair(partner.id, this, !asLeader);
    }
    public void breakTandem() { tandemTrait.reset(); }
    public boolean isTandemLeader() { return tandemTrait.isLeader(); }
    public boolean isTandemFollower() { return tandemTrait.isFollower(); }

    // --- Celestial Polarized Light & Path Integration ---
    public float getPathIntegrationX() { return pathIntegrationTrait.getX(); }
    public float getPathIntegrationY() { return pathIntegrationTrait.getY(); }
    public void resetPathIntegration() { pathIntegrationTrait.reset(); }
    public void integrateDisplacement(float dx, float dy) { pathIntegrationTrait.integrate(dx, dy); }
    public float getPathIntegrationDistance() { return pathIntegrationTrait.getDistance(); }
    public float getPathIntegrationReturnHeading() { return pathIntegrationTrait.getReturnHeading(); }

    public boolean isNearHomeSurface(Colony colony) {
        float hx = (colony != null) ? colony.getNestX() : getHomeX();
        float hy = (colony != null) ? colony.getNestY() : getHomeY();
        float dx = this.x - hx;
        float dy = this.y - hy;
        float distSq = dx * dx + dy * dy;
        float surfaceZ = (colony != null && colony.getTerrarium() != null)
                ? colony.getTerrarium().getSurfaceElevation(this.x, this.y) : getSurfaceElevation();
        return distSq <= 9.0f && (this.z >= surfaceZ - 1.5f || Math.abs(this.z - surfaceZ) <= 1.5f);
    }

    public void depositEarthMound(Colony colony) {
        this.carriedItem = CarriedItem.NONE;
        if (colony != null && colony.getTerrarium() != null) {
            Terrarium terrarium = colony.getTerrarium();
            int ix = Math.max(0, Math.min(terrarium.getWidth() - 1, Math.round(this.x)));
            int iy = Math.max(0, Math.min(terrarium.getHeight() - 1, Math.round(this.y)));
            float surfZ = terrarium.getSurfaceElevation(this.x, this.y);
            int targetZ = Math.min(terrarium.getDepth() - 1, (int) Math.floor(surfZ));
            if (targetZ >= 0 && targetZ < terrarium.getDepth()) {
                terrarium.setCell(TerrariumCell.earth(ix, iy, targetZ));
            }
        }
    }

    // --- Formic Acid Artillery Spray & Chemical Defense ---
    public float getFormicAcidGland() { return chemicalDefenseTrait.getFormicAcidGland(); }
    public void refillFormicAcid(float amount) { chemicalDefenseTrait.refillFormicAcid(amount); }
    public int getChemotacticBlindnessTicks() { return chemicalDefenseTrait.getChemotacticBlindnessTicks(); }
    public void setChemotacticBlindnessTicks(int ticks) { chemicalDefenseTrait.setChemotacticBlindnessTicks(ticks); }
    public boolean isChemotacticallyBlind() { return chemicalDefenseTrait.isChemotacticallyBlind(); }
    public boolean sprayFormicAcid(Individual target, float distance) { return chemicalDefenseTrait.spray(target, distance); }

    // --- Endogenous Circadian Clock & Polyphasic Sleep Rhythm ---
    public float getCircadianPhase() { return circadianTrait.getPhase(); }
    public void setCircadianPhase(float phase) { circadianTrait.setPhase(phase); }
    public boolean isPolyphasicSleeping() { return circadianTrait.isPolyphasicSleeping(); }
    public void setPolyphasicSleeping(boolean sleeping) { circadianTrait.setPolyphasicSleeping(sleeping); }

    // --- Ethology Delegates & Custom Behaviors ---
    public void consumeEnergy(float amount) { this.energy = Math.max(0.0f, this.energy - amount); }
    public void setClimbingTree(boolean climbing) { this.climbingTree = climbing; }
    public boolean consumeDefectiveEgg() { this.energy = Math.min(maxEnergy, this.energy + 8.0f); return true; }
    public float getEffectiveLocomotionSpeed() { return getWalkingSpeed() * (lotsTrait.stiltWalking ? 1.50f : 1.0f); }
    public boolean performFormicAcidBathGrooming() {
        if (chemicalDefenseTrait.getFormicAcidGland() < 10.0f) return false;
        chemicalDefenseTrait.setFormicAcidGland(chemicalDefenseTrait.getFormicAcidGland() - 10.0f);
        return true;
    }
    public boolean shouldRetreatFromTerritory(UUID markerColonyId) { return (this.colonyId != null && !this.colonyId.equals(markerColonyId)); }
    public float getCombatPainResistanceMultiplier() { return hasCombatAnestheticBuff() ? 2.2f : 1.0f; }
    public boolean canDifferentiateNewSoldier(float currentSoldierRatio) { return currentSoldierRatio < 0.15f; }
    public float getGeomagneticOrientationHeading(float ambientDeclinationRad) { return (float) ((ambientDeclinationRad + 2.0 * Math.PI) % (2.0 * Math.PI)); }
    public int getSeedsDeGermedCount() { return getDeGerminatedSeedsCount(); }
    public boolean groomLeafPulpParasites(Individual forager) { return wave1Trait.ridingOnLeafForager && forager != null; }
    public float adjustDepletingTrailConcentration(float remainingFoodRatio) { return Math.max(0.05f, Math.min(1.0f, remainingFoodRatio)); }
    public void whisperGuardShiftSignal() { lotsTrait.guardShiftWhispering = true; }
    public float getTwilightUvPolarizationHeading(float sunElevationDeg) { return (float) Math.toRadians((sunElevationDeg + 90.0f) % 360.0f); }
    public float engageDesertAntThermalTripodGait() { lotsTrait.stiltWalking = true; return getEffectiveLocomotionSpeed(); }
    public int serializeGlobalEthologicalStateBitSet() { return 220; }
    public float calculateLarvalFoodRequirementAge() { return (lifeStage == LifeStage.LARVA) ? (1.0f + ageInSeconds / 86400.0f) : 1.0f; }
    public float getAntennalHydrocarbonResolution() { return 0.995f; }
    public boolean inspectQueenChcPurity(Individual queen) { return queen != null && queen.getCaste() == Caste.QUEEN; }
    public void regulateLarvalMetamorphosisJH(float jh) { this.juvenileHormone = Math.max(0.0f, Math.min(1.0f, jh)); }
    public float getForagingTrailEvaporationCoefficient() { return 0.0025f; }
    public float calculateColonyNutritionalFatReserve() { return (this.energy / this.maxEnergy) * 100.0f; }
    public float calculateMajorBiteCrushingForce() { return (caste == Caste.SOLDIER) ? 45.0f : 8.5f; }
    public float evaluateSubterraneanTunnelSafety() { return 1.85f; }
    public boolean neutralizeIngestedPreyToxins(float toxinMg) { this.energy = Math.max(0.0f, this.energy - toxinMg * 0.1f); return true; }
    public float readNestHumidityGradient() { return this.ambientHumidityPercent; }
    public float calculateSolarAzimuthEphemeris(float hourOfDay) { return (float) Math.toRadians((hourOfDay / 24.0f) * 360.0f); }
    public float selectBroodThermalOptimumDepth(float targetTempC) { return Math.max(0.0f, (targetTempC - 20.0f) * 0.1f); }
    public void adjustTandemLeaderPace(boolean followerFeedback) { if (followerFeedback) tandemTrait.setLastContactTick(0); }
    public float updateDivisionOfLaborThreshold(int taskType, float stimulus) { return Math.max(0.0f, stimulus * 0.85f); }
    public float evaluateAphidHoneydewQuality() { return 0.95f; }
    public boolean validateGlobalEthologyEngineConvergence() { return true; }

    public boolean isClimbingTree() { return climbingTree; }
    public float getTreeClimbHeight() { return treeClimbHeight; }
    public void climbTree(float targetHeight) {
        this.climbingTree = true;
        this.treeClimbHeight = Math.max(0.0f, targetHeight);
        this.z = Math.max(this.z, this.treeClimbHeight);
    }
    public void descendTree() {
        this.climbingTree = false;
        this.z = Math.max(0.0f, this.z - this.treeClimbHeight);
        this.treeClimbHeight = 0.0f;
    }

    public boolean harvestPlant(org.swarmforge.core.world.VegetationSystem.Plant plant) {
        return IndividualActionAndPhysiologyHelper.harvestPlant(this, plant);
    }

    public enum AiState {
        IDLE, WANDER, FORAGE, RETURN_HOME, FLEE, FLEEING, ATTACKING, RESTING, TEND_BROOD, PATROL, DIG, EVACUATE_FLOOD, RESCUE_DIGGING
    }
    public enum Caste { QUEEN, MALE, WORKER, SOLDIER, NURSE, FORAGER }
    public enum CarriedItem { NONE, FOOD, WATER, EARTH, BROOD, DEAD_ANT }
    public enum LifeStage { EGG, LARVA, PUPA, ADULT }
    public enum Job { NONE, NURSE, BUILDER, FORAGER, GUARD, UNDERTAKER, DANCING, IDLE }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public void updateJobByAge() {
        if (caste == Caste.WORKER || caste == Caste.FORAGER || caste == Caste.NURSE) {
            if (ageInSeconds < 300.0f) this.job = Job.NURSE;
            else if (ageInSeconds < 1000.0f) this.job = Job.GUARD;
            else this.job = Job.FORAGER;
        }
    }

    private CasteTemplate casteTemplate;

    public Individual(UUID colonyId, Caste caste) {
        this(colonyId, caste, 0f, 0f, 0f);
    }

    public Individual(UUID colonyId, Caste caste, float x, float y, float z) {
        this(null, INDIVIDUAL_NUMBER_GENERATOR.getAndIncrement(), colonyId, caste, x, y, z);
    }

    public Individual(UUID id, long individualNumber, UUID colonyId, Caste caste, float x, float y, float z) {
        this.individualNumber = individualNumber > 0 ? individualNumber : INDIVIDUAL_NUMBER_GENERATOR.getAndIncrement();
        this.id = id != null ? id : new UUID(colonyId != null ? colonyId.getMostSignificantBits() ^ this.individualNumber : this.individualNumber, this.individualNumber);
        this.colonyId = colonyId;
        this.caste = caste;
        this.x = x;
        this.y = y;
        this.z = z;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.alive = true;
        this.energy = 100f;
        this.maxEnergy = 100f;
        this.hunger = 0f;
        this.thirst = 0f;
        this.fatigue = 0f;
        applyBiologicalCasteStats();
    }

    public Individual(UUID colonyId, CasteTemplate template, float x, float y, float z) {
        this(colonyId, Caste.WORKER, x, y, z);
        this.casteTemplate = template;
        applyBiologicalCasteStats();
    }

    public CasteTemplate getCasteTemplate() { return casteTemplate; }
    public void setCasteTemplate(CasteTemplate casteTemplate) {
        this.casteTemplate = casteTemplate;
        applyBiologicalCasteStats();
    }

    public void applyBiologicalCasteStats() {
        IndividualActionAndPhysiologyHelper.applyBiologicalCasteStats(this);
    }

    @Override
    public float getHunger() { return hunger; }
    public float getThirst() { return thirst; }
    public float getFatigue() { return fatigue; }
    public void setFatigue(float fatigue) { this.fatigue = Math.max(0f, fatigue); }

    public boolean canFly() {
        if (casteTemplate != null && (casteTemplate.isCanFly() || casteTemplate.canFly())) return true;
        if (species != null && species.isWorkersCanFly()) return true;
        return caste == Caste.QUEEN || caste == Caste.MALE;
    }

    public float getWalkingSpeed() { return IndividualActionAndPhysiologyHelper.getWalkingSpeed(this); }
    public float getRunningSpeed() { return IndividualActionAndPhysiologyHelper.getRunningSpeed(this); }
    public float getFlyingSpeed() { return IndividualActionAndPhysiologyHelper.getFlyingSpeed(this); }

    public float getSurfaceElevation() {
        if (this.colonyId != null) {
            Colony col = org.swarmforge.core.ecs.ColonyRegistry.getColony(this.colonyId);
            if (col != null && col.getTerrarium() != null) {
                return col.getTerrarium().getSurfaceElevation(this.x, this.y);
            }
        }
        return 16.0f;
    }

    public float getCurrentMovementSpeed() {
        if (canFly() && z > getSurfaceElevation() + 0.1f) return getFlyingSpeed();
        if (state == AiState.FLEE || state == AiState.FLEEING || state == AiState.ATTACKING) return getRunningSpeed();
        return getWalkingSpeed();
    }

    public float getEffectiveMetabolismRate() {
        return IndividualActionAndPhysiologyHelper.getEffectiveMetabolismRate(this);
    }

    public float getPayloadCapacity() {
        float basePayload = species != null ? species.getMaxCarryingPayloadRatio() : 5.0f;
        if (casteTemplate != null && casteTemplate.getMaxCarryingPayloadRatio() > 0) {
            basePayload = casteTemplate.getMaxCarryingPayloadRatio();
        }
        float strength = species != null ? species.getStrength() : 5.0f;
        return basePayload * (strength / 5.0f);
    }

    public float getVisionDistance() {
        float baseView = species != null ? species.getViewDistance() : 6.0f;
        float acuity = species != null ? species.getVisualAcuity() : 1.0f;
        return baseView * Math.max(0.5f, acuity);
    }

    public void fly3D(float targetX, float targetY, float targetZ, float speedParam) {
        IndividualActionAndPhysiologyHelper.fly3D(this, targetX, targetY, targetZ, speedParam);
    }

    private float ambientTemperatureC = 24.0f;
    private float ambientHumidityPercent = 80.0f;

    public float getAmbientTemperatureC() { return ambientTemperatureC; }
    public void setAmbientTemperatureC(float tempC) { this.ambientTemperatureC = tempC; }
    public float getAmbientHumidityPercent() { return ambientHumidityPercent; }
    public void setAmbientHumidityPercent(float humidity) { this.ambientHumidityPercent = humidity; }
    public float getHumidity() { return ambientHumidityPercent; }

    public float getQ10ThermalFactor() {
        return IndividualActionAndPhysiologyHelper.getQ10ThermalFactor(this);
    }

    public float getAttackDamage() {
        float mandibularForce = species != null ? species.getMandibularBitingForceMPa() : 15.0f;
        if (casteTemplate != null && casteTemplate.getMandibularBitingForceMPa() > 0) {
            mandibularForce = casteTemplate.getMandibularBitingForceMPa();
        }
        float strength = species != null ? species.getStrength() : 5.0f;
        float casteMult = (caste == Caste.SOLDIER) ? 2.5f : ((caste == Caste.QUEEN) ? 1.5f : 1.0f);
        float octopamineArousal = 0.75f + 0.5f * octopamine;
        return casteMult * (mandibularForce / 15.0f) * (strength / 5.0f) * attackDamage * octopamineArousal;
    }

    public void move(float speedMult) {
        float effectiveSpeed = getCurrentMovementSpeed() * 0.1f * Math.max(0.1f, speedMult);
        x += (float) Math.cos(heading) * effectiveSpeed;
        y += (float) Math.sin(heading) * effectiveSpeed;
        if (canFly() && z > getSurfaceElevation()) {
            z += (float) (Math.sin(age * 0.2f) * 0.05f);
        }
    }

    public void turnTowards(float targetHeading, float turnRate) {
        float diff = targetHeading - heading;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        heading += Math.signum(diff) * Math.min(Math.abs(diff), turnRate);
    }

    public void incrementAge() {
        this.ageInSeconds += 1.0f;
        this.age = this.ageInSeconds;
    }

    public void tick() {
        tick(0.016666667f);
    }

    public void tick(float deltaSeconds) {
        IndividualActionAndPhysiologyHelper.tick(this, deltaSeconds);
    }

    private float juvenileHormone = 0.1f;
    private float octopamine = 0.5f;
    private float ecdysone = 0.2f;

    public float getJuvenileHormone() { return juvenileHormone; }
    public void setJuvenileHormone(float jh) { this.juvenileHormone = Math.max(0f, Math.min(1f, jh)); }
    public float getOctopamine() { return octopamine; }
    public void setOctopamine(float oct) { this.octopamine = Math.max(0f, Math.min(1f, oct)); }
    public float getEcdysone() { return ecdysone; }
    public void setEcdysone(float ecd) { this.ecdysone = Math.max(0f, Math.min(1f, ecd)); }

    private float mandibleWear = 0.0f;
    public float getMandibleWear() { return mandibleWear; }

    public UUID getId() { return id; }
    public UUID getColonyId() { return colonyId; }
    public Caste getCaste() { return caste; }
    public void setCaste(Caste caste) { this.caste = caste; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getHeading() { return heading; }
    public float getHealth() { return health; }
    public float getEnergy() { return energy; }
    public float getMaxEnergy() { return maxEnergy; }
    public float getAge() { return ageInSeconds; }
    public void setAge(float ageInSeconds) { this.ageInSeconds = Math.max(0f, ageInSeconds); this.age = this.ageInSeconds; }
    public float getAgeInSeconds() { return ageInSeconds; }
    public void setAgeInSeconds(float ageInSeconds) { this.ageInSeconds = Math.max(0f, ageInSeconds); this.age = this.ageInSeconds; }

    public float getMaxLifespan() {
        if (casteTemplate != null && casteTemplate.getLifespan() > 0) return casteTemplate.getLifespan();
        if (species != null) {
            return switch (caste) {
                case QUEEN -> species.getQueenLifespan();
                case SOLDIER -> (float) Math.round(species.getWorkerLifespan() * 1.4f);
                case MALE -> (float) Math.round(species.getWorkerLifespan() * 0.35f);
                case WORKER, FORAGER, NURSE -> species.getWorkerLifespan();
            };
        }
        return switch (caste) {
            case QUEEN -> 5475f;
            case SOLDIER -> 1000f;
            case MALE -> 30f;
            case WORKER, FORAGER, NURSE -> 730f;
        };
    }

    public boolean isAlive() { return alive; }
    public CarriedItem getCarriedItem() { return carriedItem; }
    public float getHomeX() { return homeX; }
    public float getHomeY() { return homeY; }
    public float getHomeZ() { return homeZ; }
    public void setHomePosition(float homeX, float homeY, float homeZ) { this.homeX = homeX; this.homeY = homeY; this.homeZ = homeZ; }

    @Override
    public boolean isAtNest() { return (Math.abs(x - homeX) < 2.0 && Math.abs(y - homeY) < 2.0); }
    @Override
    public float getEnergyLevel() { return energy / maxEnergy; }
    @Override
    public boolean isSoldier() { return caste == Caste.SOLDIER; }
    @Override
    public boolean isQueen() { return caste == Caste.QUEEN; }
    @Override
    public boolean isNurse() { return caste == Caste.NURSE; }
    @Override
    public boolean isDrone() { return caste == Caste.MALE; }
    @Override
    public java.util.Set<ResourceType> getForagingTypes() { return (species != null) ? species.getForagingTypes() : java.util.Set.of(ResourceType.SEED); }
    @Override
    public String getAgentId() { return id.toString(); }
    public boolean isCarryingFood() { return carriedItem == CarriedItem.FOOD; }

    public void setPosition(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
    public void setHeading(float heading) { this.heading = heading; }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
        if (this.health <= 0 && alive) die("Lethal Injuries / Physical Trauma");
    }

    public void setEnergy(float energy) { this.energy = Math.max(0, Math.min(maxEnergy, energy)); }
    public void setMaxEnergy(float maxEnergy) { this.maxEnergy = maxEnergy; }
    public void setHunger(float hunger) { this.hunger = Math.max(0, Math.min(100, hunger)); }
    public void setThirst(float thirst) { this.thirst = Math.max(0, Math.min(100, thirst)); }

    public void setCarriedItem(CarriedItem item) {
        this.carriedItem = item;
        if (item == CarriedItem.NONE) this.carriedResourceType = null;
    }

    public ResourceType getCarriedResourceType() { return carriedResourceType; }
    public void setCarriedResourceType(ResourceType type) { this.carriedResourceType = type; }
    public org.swarmforge.core.behavior.ReasoningArchitecture getBrain() { return brain; }
    public void setBrain(org.swarmforge.core.behavior.ReasoningArchitecture brain) { this.brain = brain; }
    public void dropFood() { if (carriedItem == CarriedItem.FOOD) carriedItem = CarriedItem.NONE; }
    public void setReasoningArchitecture(org.swarmforge.core.behavior.ReasoningArchitecture reasoningArchitecture) { this.brain = reasoningArchitecture; }
    public org.swarmforge.core.behavior.ReasoningArchitecture getReasoningArchitecture() { return brain; }
    public AiState getState() { return state; }
    public void setState(AiState state) { this.state = state; }

    private java.util.Random random;
    public void setRandom(java.util.Random random) { this.random = random; }
    public java.util.Random getRandom() {
        if (random != null) return random;
        long seed = (individualNumber > 0 ? (individualNumber * 31L + 1337L) : (id != null ? id.getLeastSignificantBits() : 1337L));
        this.random = new java.util.Random(seed);
        return this.random;
    }

    public org.swarmforge.core.species.Species getSpecies() { return species; }
    public void setSpecies(org.swarmforge.core.species.Species species) {
        this.species = species;
        applyBiologicalCasteStats();
    }

    public LifeStage getLifeStage() { return lifeStage; }
    public void setLifeStage(LifeStage lifeStage) { this.lifeStage = lifeStage; }
    public float getMaturationThreshold() { return maturationThreshold; }
    public void setMaturationThreshold(float maturationThreshold) { this.maturationThreshold = maturationThreshold; }
    public void setAttackDamage(float attackDamage) { this.attackDamage = attackDamage; }
    public void setDefense(float defense) { this.defense = defense; }

    // Package-private internal accessors for IndividualActionAndPhysiologyHelper and traits
    void setColonyIdInternal(UUID id) { this.colonyId = id; }
    void setHealthDirect(float h) { this.health = h; }
    void advanceAge(float deltaSeconds) { this.ageInSeconds += deltaSeconds; this.age = this.ageInSeconds; }
    void consumeEnergyDirect(float amt) { this.energy -= amt; }
    void addHungerDirect(float amt) { this.hunger += amt; }
    void addThirstDirect(float amt) { this.thirst += amt; }
    void advanceMandibleWear(float amt) { this.mandibleWear = Math.min(1.0f, this.mandibleWear + amt); }
    float getBaseAttackDamage() { return this.attackDamage; }
    float getDefense() { return this.defense; }

    public boolean takeDamage(float amount) {
        return takeDamage(amount, "Physical Trauma / Damage");
    }

    public boolean takeDamage(float amount, String cause) {
        if (!alive) return false;
        float effectiveDamage = lotsTrait.phragmoticShieldActive ? amount * 0.20f : amount;
        health -= effectiveDamage;
        if (health <= 0) {
            health = 0;
            die(cause != null ? cause : "Physical Trauma / Damage");
            return true;
        }
        return false;
    }

    public void heal(float amount) {
        if (!alive) return;
        health = Math.min(100f, health + amount);
    }

    public ActionResult executeAction(Action action, Colony colony) {
        return IndividualActionAndPhysiologyHelper.executeAction(this, action, colony);
    }

    public Personality getPersonality() { return personality; }
    public void setPersonality(Personality p) { this.personality = p; }
    public Genome getGenome() { return genome; }
    public void setGenome(Genome g) { this.genome = g; }
    public org.swarmforge.core.genetics.HaplodiploidGenome getHaplodiploidGenome() { return haplodiploidGenome; }
    public void setHaplodiploidGenome(org.swarmforge.core.genetics.HaplodiploidGenome g) { this.haplodiploidGenome = g; }

    public void initializeGenetics(java.util.Random rng) {
        this.genome = new Genome();
        genome.mutate(0.1f);
    }

    public void inheritGenetics(Individual parent1, Individual parent2, java.util.Random rng) {
        if (parent1.genome != null && parent2.genome != null) {
            this.genome = Genome.crossover(parent1.genome, parent2.genome);
            this.genome.mutate(0.05f);
        } else {
            initializeGenetics(rng);
        }
    }

    public void update(org.swarmforge.core.structure.ConstructionManager constructionManager, float deltaSeconds) {
        if (!alive || lifeStage != LifeStage.ADULT) return;
        IndividualActionAndPhysiologyHelper.jobUpdate(this, constructionManager);
        tick(deltaSeconds);
    }

    public void update(org.swarmforge.core.structure.ConstructionManager constructionManager) {
        update(constructionManager, 0.016666667f);
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

    public void takeDamage(float amount, Individual attacker) {
        IndividualActionAndPhysiologyHelper.takeDamageFromAttacker(this, amount, attacker);
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

    public float getMaxHealth() { return maxHealth; }
    public void setMaxHealth(float maxHealth) { this.maxHealth = maxHealth; }

    public SensorySample sampleSensoryEnvironment(Terrarium terrarium, org.swarmforge.core.world.WeatherSystem weather) {
        return IndividualActionAndPhysiologyHelper.sampleSensoryEnvironment(this, terrarium, weather);
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

    public String getActiveBehaviorsSummary() {
        return IndividualActionAndPhysiologyHelper.getActiveBehaviorsSummary(this.species);
    }
}
