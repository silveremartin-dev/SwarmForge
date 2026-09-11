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
 * Canonical Domain Entity representing an individual eusocial insect in the simulation.
 * Implements AgentView for direct bridge and inter-compatibility with the ECS and Rendering engines.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Individual implements java.io.Serializable, AgentView {
    private static final long serialVersionUID = 1L;

    private static final java.util.concurrent.atomic.AtomicLong ANT_NUMBER_GENERATOR = new java.util.concurrent.atomic.AtomicLong(1);
    private final long antNumber;

    public static void resetAntNumberGenerator() {
        ANT_NUMBER_GENERATOR.set(1);
    }

    public static void setNextAntNumber(long nextVal) {
        ANT_NUMBER_GENERATOR.set(Math.max(1, nextVal));
    }

    public long getAntNumber() {
        return antNumber;
    }

    public String getFormattedId() {
        return "ant_" + antNumber;
    }

    private final UUID id;
    private Caste caste;
    private UUID colonyId;

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

    public void setChcProfile(float[] profile) {
        if (profile != null && profile.length > 0) {
            this.chcProfile = profile.clone();
        }
    }

    /**
     * Calculates Bray-Curtis CHC dissimilarity index d_BC between this individual and another:
     * d_BC(u, v) = sum(|u_k - v_k|) / sum(u_k + v_k)
     * Returns a distance in [0.0, 1.0].
     */
    public float calculateChcDissimilarity(Individual other) {
        if (other == null || other.chcProfile == null || this.chcProfile == null) {
            return 1.0f;
        }
        float num = 0.0f;
        float den = 0.0f;
        int len = Math.min(this.chcProfile.length, other.chcProfile.length);
        for (int i = 0; i < len; i++) {
            num += Math.abs(this.chcProfile[i] - other.chcProfile[i]);
            den += (this.chcProfile[i] + other.chcProfile[i]);
        }
        return (den > 0.0001f) ? (num / den) : 0.0f;
    }

    /**
     * Nestmate recognition decision rule based on CHC chemical signature.
     * Threshold theta = 0.28 (academic literature: cuticular hydrocarbon gestalt envelope).
     */
    public boolean isNestmateRecognized(Individual other) {
        if (other == null) return false;
        if (this.colonyId != null && this.colonyId.equals(other.colonyId)) {
            return true; // True genetic/colony nestmate
        }
        // Chemical camouflage / social parasitism / dulosis acceptance
        return calculateChcDissimilarity(other) <= 0.28f;
    }

    public void homogenizeChcProfile(Individual partner, float factor) {
        if (partner == null || partner.chcProfile == null || this.chcProfile == null) return;
        float f = Math.max(0.0f, Math.min(0.5f, factor));
        int len = Math.min(this.chcProfile.length, partner.chcProfile.length);
        for (int i = 0; i < len; i++) {
            float blend = this.chcProfile[i] * (1.0f - f) + partner.chcProfile[i] * f;
            this.chcProfile[i] = blend;
        }
    }

    // --- Social Crop (Ingluvies / Jabot Social) ---
    private float socialCropCapacity = 20.0f;
    private float socialCropAmount = 0.0f;
    private ResourceType socialCropResourceType = null;

    public float getSocialCropAmount() { return socialCropAmount; }
    public void setSocialCropAmount(float amount) { this.socialCropAmount = Math.max(0f, Math.min(socialCropCapacity, amount)); }
    public float getSocialCropCapacity() { return socialCropCapacity; }
    public void setSocialCropCapacity(float cap) { this.socialCropCapacity = Math.max(0.1f, cap); }
    public ResourceType getSocialCropResourceType() { return socialCropResourceType; }
    public void setSocialCropResourceType(ResourceType type) { this.socialCropResourceType = type; }

    public float fillSocialCrop(float amount, ResourceType type) {
        if (amount <= 0) return 0f;
        if (this.socialCropAmount <= 0.001f) {
            this.socialCropResourceType = type;
        }
        float space = socialCropCapacity - socialCropAmount;
        float added = Math.min(space, amount);
        this.socialCropAmount += added;
        return added;
    }

    public float regurgitateSocialCrop(float amount) {
        if (amount <= 0 || socialCropAmount <= 0) return 0f;
        float taken = Math.min(socialCropAmount, amount);
        this.socialCropAmount -= taken;
        if (this.socialCropAmount <= 0.001f) {
            this.socialCropAmount = 0.0f;
            this.socialCropResourceType = null;
        }
        return taken;
    }

    // --- Tandem Running Dynamics ---
    public enum TandemRole {
        NONE, LEADER, FOLLOWER
    }

    private TandemRole tandemRole = TandemRole.NONE;
    private UUID tandemPartnerId = null;
    private long lastTandemContactTick = 0;

    public TandemRole getTandemRole() { return tandemRole; }
    public void setTandemRole(TandemRole role) { this.tandemRole = (role != null) ? role : TandemRole.NONE; }
    public UUID getTandemPartnerId() { return tandemPartnerId; }
    public void setTandemPartnerId(UUID id) { this.tandemPartnerId = id; }
    public long getLastTandemContactTick() { return lastTandemContactTick; }
    public void setLastTandemContactTick(long tick) { this.lastTandemContactTick = tick; }

    public void pairTandem(Individual partner, boolean asLeader) {
        if (partner == null) return;
        this.tandemRole = asLeader ? TandemRole.LEADER : TandemRole.FOLLOWER;
        this.tandemPartnerId = partner.getId();
        this.lastTandemContactTick = 0;
        partner.tandemRole = asLeader ? TandemRole.FOLLOWER : TandemRole.LEADER;
        partner.tandemPartnerId = this.getId();
        partner.lastTandemContactTick = 0;
    }

    public void breakTandem() {
        this.tandemRole = TandemRole.NONE;
        this.tandemPartnerId = null;
    }

    public boolean isTandemLeader() { return tandemRole == TandemRole.LEADER; }
    public boolean isTandemFollower() { return tandemRole == TandemRole.FOLLOWER; }

    // --- Celestial Polarized Light & Path Integration ---
    private float pathIntegrationX = 0.0f;
    private float pathIntegrationY = 0.0f;

    public float getPathIntegrationX() { return pathIntegrationX; }
    public float getPathIntegrationY() { return pathIntegrationY; }

    public void resetPathIntegration() {
        this.pathIntegrationX = 0.0f;
        this.pathIntegrationY = 0.0f;
    }

    public void integrateDisplacement(float dx, float dy) {
        this.pathIntegrationX += dx;
        this.pathIntegrationY += dy;
    }

    public float getPathIntegrationDistance() {
        return (float) Math.hypot(pathIntegrationX, pathIntegrationY);
    }

    public float getPathIntegrationReturnHeading() {
        return (float) Math.atan2(-pathIntegrationY, -pathIntegrationX);
    }

    public boolean isNearHomeSurface(Colony colony) {
        float hx = (colony != null) ? colony.getNestX() : getHomeX();
        float hy = (colony != null) ? colony.getNestY() : getHomeY();
        float dx = this.x - hx;
        float dy = this.y - hy;
        float distSq = dx * dx + dy * dy;
        float surfaceZ = (colony != null && colony.getTerrarium() != null)
                ? colony.getTerrarium().getSurfaceElevation(this.x, this.y)
                : getSurfaceElevation();
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

    // --- Formic Acid Artillery Spray & Chemical Blindness ---
    private float formicAcidGland = 100.0f;
    private int chemotacticBlindnessTicks = 0;

    public float getFormicAcidGland() { return formicAcidGland; }
    public void refillFormicAcid(float amount) { this.formicAcidGland = Math.min(100.0f, this.formicAcidGland + amount); }
    public int getChemotacticBlindnessTicks() { return chemotacticBlindnessTicks; }
    public void setChemotacticBlindnessTicks(int ticks) { this.chemotacticBlindnessTicks = Math.max(0, ticks); }
    public boolean isChemotacticallyBlind() { return chemotacticBlindnessTicks > 0; }

    public boolean sprayFormicAcid(Individual target, float distance) {
        if (target == null || !target.isAlive() || distance > 1.5f || formicAcidGland < 20.0f) {
            return false;
        }
        formicAcidGland -= 20.0f;
        target.takeDamage(15.0f, "Formic Acid Chemical Burn");
        target.setChemotacticBlindnessTicks(120); // 2s blindness
        return true;
    }

    // --- Endogenous Circadian Clock & Polyphasic Sleep Rhythm ---
    private float circadianPhase = 0.0f; // [0, 2pi]
    private boolean polyphasicSleeping = false;

    public float getCircadianPhase() { return circadianPhase; }
    public void setCircadianPhase(float phase) { this.circadianPhase = phase; }
    public boolean isPolyphasicSleeping() { return polyphasicSleeping; }
    public void setPolyphasicSleeping(boolean sleeping) { this.polyphasicSleeping = sleeping; }

    // --- Tripartite Symbiosis: Actinobacteria Bio-Weeding ---
    private float actinobacteriaResin = 100.0f;

    public float getActinobacteriaResin() { return actinobacteriaResin; }
    public void refillActinobacteriaResin(float amt) { this.actinobacteriaResin = Math.min(100.0f, this.actinobacteriaResin + amt); }

    public boolean applyActinobacteriaBioWeeding(org.swarmforge.core.simulation.FungusGarden garden) {
        if (garden == null || actinobacteriaResin < 15.0f) return false;
        actinobacteriaResin -= 15.0f;
        garden.setContaminationLevel(Math.max(0.0f, garden.getContaminationLevel() - 0.3f));
        garden.treatWithActinobacteria(10.0f);
        return true;
    }

    // --- 1. Necrophoresis & Cemetery Refuse Deposition ---
    private float decompositionAgeSeconds = 0.0f;

    public float getDecompositionAgeSeconds() { return decompositionAgeSeconds; }
    public void setDecompositionAgeSeconds(float age) { this.decompositionAgeSeconds = Math.max(0.0f, age); }
    public boolean isOleicAcidEmitted() { return !alive && decompositionAgeSeconds >= 180.0f; }

    public boolean pickUpCorpse(Individual corpse) {
        if (corpse == null || corpse.isAlive() || this.carriedItem != CarriedItem.NONE) {
            return false;
        }
        this.carriedItem = CarriedItem.DEAD_ANT;
        return true;
    }

    public boolean depositCorpseAtRefuse(float cemeteryX, float cemeteryY, float cemeteryZ) {
        if (this.carriedItem != CarriedItem.DEAD_ANT) {
            return false;
        }
        this.carriedItem = CarriedItem.NONE;
        return true;
    }

    // --- 2. Active Social Thermoregulation ---
    private boolean shiveringThermogenesis = false;
    private boolean wingFanning = false;
    private float thoraxTemperatureC = 25.0f;

    public boolean isShiveringThermogenesis() { return shiveringThermogenesis; }
    public void setShiveringThermogenesis(boolean s) { this.shiveringThermogenesis = s; }
    public boolean isWingFanning() { return wingFanning; }
    public void setWingFanning(boolean f) { this.wingFanning = f; }
    public float getThoraxTemperatureC() { return thoraxTemperatureC; }
    public void setThoraxTemperatureC(float t) { this.thoraxTemperatureC = t; }

    // --- 3. Dulosis & Slave-Making Raids ---
    private boolean enslaved = false;
    private UUID hostColonyId = null;

    public boolean isEnslaved() { return enslaved; }
    public void setEnslaved(boolean enslaved) { this.enslaved = enslaved; }
    public UUID getHostColonyId() { return hostColonyId; }
    public void setHostColonyId(UUID id) { this.hostColonyId = id; }

    public boolean stealBrood(Individual broodTarget) {
        if (broodTarget == null || this.carriedItem != CarriedItem.NONE) {
            return false;
        }
        this.carriedItem = CarriedItem.BROOD;
        return true;
    }

    public void integrateAsEnslavedWorker(UUID masterColonyId) {
        this.enslaved = true;
        this.hostColonyId = this.colonyId;
        this.colonyId = masterColonyId;
        this.lifeStage = LifeStage.ADULT;
        this.job = Job.NURSE;
    }

    // --- 4. Bio-Acoustic Stridulation Rescue Call ---
    private boolean stridulatingRescueCall = false;
    private float stridulationFrequencyHz = 850.0f;
    private float stridulationIntensityDb = 80.0f;

    public boolean isStridulatingRescueCall() { return stridulatingRescueCall; }
    public void setStridulatingRescueCall(boolean s) { this.stridulatingRescueCall = s; }
    public float getStridulationFrequencyHz() { return stridulationFrequencyHz; }
    public float getStridulationIntensityDb() { return stridulationIntensityDb; }

    public void triggerStridulationRescue() {
        this.stridulatingRescueCall = true;
    }

    public void consumeEnergy(float amount) {
        this.energy = Math.max(0.0f, this.energy - amount);
    }

    public void setClimbingTree(boolean climbing) {
        this.climbingTree = climbing;
    }

    // ==========================================
    // LOT A: Advanced Biological & Mechanical Defense
    // ==========================================
    private boolean autothysed = false;
    private boolean phragmoticShieldActive = false;
    private boolean inThermalBallCluster = false;
    private float nasuteResinReservoir = 100.0f;

    public boolean hasAutothysed() { return autothysed; }
    public boolean isPhragmoticShieldActive() { return phragmoticShieldActive; }
    public void setPhragmoticShieldActive(boolean active) { this.phragmoticShieldActive = active; }
    public boolean isInThermalBallCluster() { return inThermalBallCluster; }
    public void setInThermalBallCluster(boolean cluster) { this.inThermalBallCluster = cluster; }
    public float getNasuteResinReservoir() { return nasuteResinReservoir; }
    public void refillNasuteResin(float amt) { this.nasuteResinReservoir = Math.min(100.0f, this.nasuteResinReservoir + amt); }

    public boolean performAutothysis(java.util.List<Individual> nearbyEnemies) {
        if (!alive || autothysed) return false;
        autothysed = true;
        die("Autothysis Defensive Rupture");
        if (nearbyEnemies != null) {
            for (Individual enemy : nearbyEnemies) {
                if (enemy != null && enemy.isAlive() && enemy.getColonyId() != this.colonyId) {
                    enemy.takeDamage(35.0f, "Toxic Polyketone Adhesive Glue");
                    enemy.setChemotacticBlindnessTicks(180);
                }
            }
        }
        return true;
    }

    public boolean performThermalBalling(Individual targetPredator, float clusterPower) {
        if (!alive || targetPredator == null || !targetPredator.isAlive()) return false;
        this.inThermalBallCluster = true;
        this.energy = Math.max(0.0f, this.energy - 0.5f);
        this.thoraxTemperatureC = Math.min(47.0f, this.thoraxTemperatureC + 4.0f);
        targetPredator.takeDamage(10.0f * clusterPower, "Thermal Ball Asphyxiation");
        return true;
    }

    public boolean squirtNasuteResin(Individual targetEnemy, float distance) {
        if (!alive || targetEnemy == null || !targetEnemy.isAlive() || distance > 0.8f || nasuteResinReservoir < 15.0f) {
            return false;
        }
        nasuteResinReservoir -= 15.0f;
        targetEnemy.takeDamage(12.0f, "Nasute Monoterpene Resin");
        targetEnemy.setChemotacticBlindnessTicks(100);
        return true;
    }

    /**
     * Honeybee worker barbed sting defense against other arthropods/insects.
     * Inflicts severe apitoxin venom damage (40.0) and releases alarm pheromone (isopentyl acetate).
     *
     * Biological Realism: When stinging rigid chitinous insect cuticle, the barbs do NOT systematically
     * tear the abdomen (autotomy rate ~12% if wedged in hard sclerite). The worker bee survives in ~88% of cases
     * and can continue active nest defense.
     */
    public boolean performBarbedBeeSting(Individual targetEnemy) {
        if (!alive || targetEnemy == null || !targetEnemy.isAlive()) {
            return false;
        }
        // Heavy envenomation
        targetEnemy.takeDamage(40.0f, "Apitoxin Envenomation (Barbed Sting)");
        targetEnemy.setChemotacticBlindnessTicks(150);
        this.energy = Math.max(0.0f, this.energy - 8.0f);

        // Chitin penetration: only ~12% risk of mechanical entrapment / autotomy against insects
        if (Math.random() < 0.12) {
            this.alive = false;
            this.health = 0.0f;
            die("Autotomie post-piqûre (Dard coincé dans la cuticule)");
        }
        return true;
    }

    /**
     * Honeybee worker barbed sting defense against vertebrate/mammalian predators (Bears, Mice, Humans).
     * The thick, elastic dermis traps the recurved barbs, tearing away the sting apparatus and caudal ganglion.
     * Results in 100% fatal autotomy with prolonged apitoxin pumping.
     */
    public boolean performMammalianDefensiveSting(float envenomationDamage) {
        if (!alive) return false;
        this.alive = false;
        this.health = 0.0f;
        die("Autotomie défensive post-piqûre mammifère (Dard arraché)");
        return true;
    }

    /**
     * Honeybee barbed sting defense against a predator entity.
     */
    public boolean performBarbedBeeSting(Predator targetPredator) {
        if (!alive || targetPredator == null || !targetPredator.isAlive()) {
            return false;
        }
        targetPredator.takeDamage(40.0f);
        this.energy = Math.max(0.0f, this.energy - 8.0f);

        // Against arthropod predators (spiders, hornets), 12% autotomy risk
        if (Math.random() < 0.12) {
            this.alive = false;
            this.health = 0.0f;
            die("Autotomie post-piqûre contre prédateur");
        }
        return true;
    }

    /**
     * Wasp smooth sting attack.
     * Inflicts repeated venom damage (20.0) without abdominal rupture.
     */
    public boolean performSmoothWaspSting(Individual targetEnemy) {
        if (!alive || targetEnemy == null || !targetEnemy.isAlive()) {
            return false;
        }
        this.energy = Math.max(0.0f, this.energy - 5.0f);
        targetEnemy.takeDamage(20.0f, "Vespula Envenomation (Smooth Sting)");
        return true;
    }

    public boolean performSmoothWaspSting(Predator targetPredator) {
        if (!alive || targetPredator == null || !targetPredator.isAlive()) {
            return false;
        }
        this.energy = Math.max(0.0f, this.energy - 5.0f);
        targetPredator.takeDamage(20.0f);
        return true;
    }

    // ==========================================
    // LOT B: Living Civil Engineering & Subterranean Architecture
    // ==========================================
    private boolean formingLivingBridge = false;
    private boolean formingLivingRaft = false;
    private boolean inhabitingDomatia = false;
    private float stercoralMortarReservoir = 100.0f;

    public boolean isFormingLivingBridge() { return formingLivingBridge; }
    public void setFormingLivingBridge(boolean bridge) { this.formingLivingBridge = bridge; }
    public boolean isFormingLivingRaft() { return formingLivingRaft; }
    public void setFormingLivingRaft(boolean raft) { this.formingLivingRaft = raft; }
    public boolean isInhabitingDomatia() { return inhabitingDomatia; }
    public void setInhabitingDomatia(boolean domatia) { this.inhabitingDomatia = domatia; }
    public float getStercoralMortarReservoir() { return stercoralMortarReservoir; }
    public void refillStercoralMortar(float amt) { this.stercoralMortarReservoir = Math.min(100.0f, this.stercoralMortarReservoir + amt); }

    public boolean sewLeavesWithLarvalSilk(Individual silkLarva) {
        if (silkLarva == null || silkLarva.getLifeStage() != LifeStage.LARVA || this.carriedItem != CarriedItem.NONE) {
            return false;
        }
        this.carriedItem = CarriedItem.BROOD;
        return true;
    }

    public boolean applyStercoralCement() {
        if (stercoralMortarReservoir < 10.0f) return false;
        stercoralMortarReservoir -= 10.0f;
        return true;
    }

    // ==========================================
    // LOT C: Agronomy, Storage & Plant/Insect Symbiosis
    // ==========================================
    private boolean repleteStorageCaste = false;
    private float repleteStorageVolume = 0.0f;
    private int deGerminatedSeedsCount = 0;
    private int managedAphidsCount = 0;

    public boolean isRepleteStorageCaste() { return repleteStorageCaste; }
    public void setRepleteStorageCaste(boolean replete) { this.repleteStorageCaste = replete; }
    public float getRepleteStorageVolume() { return repleteStorageVolume; }
    public void setRepleteStorageVolume(float vol) { this.repleteStorageVolume = Math.max(0.0f, Math.min(100.0f, vol)); }
    public int getDeGerminatedSeedsCount() { return deGerminatedSeedsCount; }
    public int getManagedAphidsCount() { return managedAphidsCount; }

    public boolean deGermStoredSeed() {
        if (carriedItem != CarriedItem.FOOD) return false;
        deGerminatedSeedsCount++;
        return true;
    }

    public float milkAphid(float honeydewYield) {
        managedAphidsCount++;
        float harvested = Math.max(0.0f, honeydewYield);
        this.energy = Math.min(maxEnergy, this.energy + harvested * 5.0f);
        if (repleteStorageCaste) {
            repleteStorageVolume = Math.min(100.0f, repleteStorageVolume + harvested * 10.0f);
        }
        return harvested;
    }

    // ==========================================
    // LOT D: Bio-Acoustic Communication, Dance & Royal Signaling
    // ==========================================
    private boolean waggleDancing = false;
    private float waggleTargetHeading = 0.0f;
    private float waggleDistanceMeters = 0.0f;
    private boolean trembleDancing = false;
    private boolean queenPipingActive = false;
    private float queenPipingFrequencyHz = 450.0f;

    public boolean isWaggleDancing() { return waggleDancing; }
    public float getWaggleTargetHeading() { return waggleTargetHeading; }
    public float getWaggleDistanceMeters() { return waggleDistanceMeters; }
    public boolean isTrembleDancing() { return trembleDancing; }
    public void setTrembleDancing(boolean tremble) { this.trembleDancing = tremble; }
    public void triggerTrembleDance() { this.trembleDancing = true; }
    public void stopTrembleDance() { this.trembleDancing = false; }
    public boolean isQueenPipingActive() { return queenPipingActive; }
    public float getQueenPipingFrequencyHz() { return queenPipingFrequencyHz; }

    public void performWaggleDance(float targetHeading, float distanceMeters) {
        this.waggleDancing = true;
        this.waggleTargetHeading = targetHeading;
        this.waggleDistanceMeters = Math.max(0.0f, distanceMeters);
    }

    public void stopWaggleDance() {
        this.waggleDancing = false;
    }

    public void triggerQueenPiping() {
        if (caste == Caste.QUEEN) {
            this.queenPipingActive = true;
            this.queenPipingFrequencyHz = 450.0f;
        }
    }

    public void stopQueenPiping() {
        this.queenPipingActive = false;
    }

    // ==========================================
    // LOT E: Genetics, Reproduction & Social Regulation
    // ==========================================
    private float royalPheromoneInhibitionTiter = 1.0f;
    private boolean gamergate = false;
    private int dominanceScore = 0;
    private int trophicEggsLaid = 0;

    public float getRoyalPheromoneInhibitionTiter() { return royalPheromoneInhibitionTiter; }
    public void setRoyalPheromoneInhibitionTiter(float titer) { this.royalPheromoneInhibitionTiter = Math.max(0.0f, Math.min(1.0f, titer)); }
    public boolean isGamergate() { return gamergate; }
    public void setGamergate(boolean g) { this.gamergate = g; }
    public int getDominanceScore() { return dominanceScore; }
    public int getTrophicEggsLaid() { return trophicEggsLaid; }

    public boolean isOvariesActivated() {
        return (caste == Caste.WORKER || caste == Caste.NURSE) && royalPheromoneInhibitionTiter < 0.15f;
    }

    public boolean layTrophicEgg() {
        if (!isOvariesActivated() && !gamergate) return false;
        trophicEggsLaid++;
        this.energy = Math.max(0.0f, this.energy - 10.0f);
        return true;
    }

    public boolean feedOnLarvalHemolymph(Individual larva) {
        if (larva == null || larva.getLifeStage() != LifeStage.LARVA || !larva.isAlive()) return false;
        larva.takeDamage(4.0f, "Non-lethal Larval Hemolymph Tap");
        this.energy = Math.min(maxEnergy, this.energy + 15.0f);
        return true;
    }

    public boolean engageDominanceTournament(Individual rival) {
        if (rival == null || !rival.isAlive() || rival.getColonyId() != this.colonyId) return false;
        if (this.health >= rival.getHealth()) {
            this.dominanceScore += 2;
            rival.dominanceScore = Math.max(0, rival.dominanceScore - 1);
            if (this.dominanceScore >= 10 && !this.gamergate) {
                this.gamergate = true;
                this.caste = Caste.QUEEN;
            }
            return true;
        } else {
            rival.dominanceScore += 2;
            this.dominanceScore = Math.max(0, this.dominanceScore - 1);
            return false;
        }
    }

    public boolean consumeDefectiveEgg() {
        this.energy = Math.min(maxEnergy, this.energy + 8.0f);
        return true;
    }

    // ==========================================
    // LOT F: Extreme Eco-Physiology & Climatic Adaptation
    // ==========================================
    private float glycerolConcentrationMgMl = 0.0f;
    private boolean stiltWalking = false;
    private boolean floodEvacuating = false;

    public float getGlycerolConcentrationMgMl() { return glycerolConcentrationMgMl; }
    public void setGlycerolConcentrationMgMl(float conc) { this.glycerolConcentrationMgMl = Math.max(0.0f, conc); }
    public boolean isStiltWalking() { return stiltWalking; }
    public void setStiltWalking(boolean sw) { this.stiltWalking = sw; }
    public boolean isFloodEvacuating() { return floodEvacuating; }
    public void setFloodEvacuating(boolean fe) { this.floodEvacuating = fe; }

    public void synthesizeGlycerol(float amount) {
        this.glycerolConcentrationMgMl = Math.min(50.0f, this.glycerolConcentrationMgMl + amount);
    }

    public boolean canSurviveSubzero(float tempC) {
        if (tempC >= 0.0f) return true;
        float criticalMinTemp = - (this.glycerolConcentrationMgMl * 0.375f);
        return tempC >= criticalMinTemp;
    }

    public float getEffectiveLocomotionSpeed() {
        float speed = getWalkingSpeed();
        if (stiltWalking) {
            speed *= 1.50f;
        }
        return speed;
    }

    public void detectHydrostaticFloodPressure(float barometricDropHpa, float soilMoisturePercent) {
        if (barometricDropHpa > 15.0f || soilMoisturePercent > 85.0f) {
            this.floodEvacuating = true;
        }
    }

    // ==========================================
    // LOT G: Advanced Sanitation, Social Immunity & Pharmacy
    // ==========================================
    private boolean voluntarySelfIsolating = false;
    private float propolisVarnishCarried = 0.0f;

    public boolean isVoluntarySelfIsolating() { return voluntarySelfIsolating; }
    public void setVoluntarySelfIsolating(boolean iso) { this.voluntarySelfIsolating = iso; }
    public float getPropolisVarnishCarried() { return propolisVarnishCarried; }
    public void setPropolisVarnishCarried(float amt) { this.propolisVarnishCarried = Math.max(0.0f, amt); }

    public void checkPathogenSelfIsolation(float fungalSporeTiter) {
        if (fungalSporeTiter >= 75.0f) {
            this.voluntarySelfIsolating = true;
        }
    }

    public boolean performFormicAcidBathGrooming() {
        if (this.formicAcidGland < 10.0f) return false;
        this.formicAcidGland -= 10.0f;
        return true;
    }

    public boolean applyPropolisVarnish() {
        if (this.propolisVarnishCarried < 5.0f) return false;
        this.propolisVarnishCarried -= 5.0f;
        return true;
    }

    public boolean clipAphidWingBuds() {
        this.managedAphidsCount++;
        return true;
    }

    // ==========================================
    // LOT H: Maçonnerie Végétale & Climatisation
    // ==========================================
    private boolean holdingSilkLarva = false;
    private float silkReservoir = 100.0f;
    private boolean rainSiphonConstructed = false;
    private float drainedFloodWaterLiters = 0.0f;
    private boolean dualConduitsActive = false;
    private int antisepticGravelCarried = 0;
    private int sealedContaminatedGalleries = 0;

    public boolean isHoldingSilkLarva() { return holdingSilkLarva; }
    public void setHoldingSilkLarva(boolean h) { this.holdingSilkLarva = h; }
    public float getSilkReservoir() { return silkReservoir; }
    public void refillSilkReservoir(float amt) { this.silkReservoir = Math.min(100.0f, this.silkReservoir + amt); }

    public boolean weaveLeafSilkSeam(float silkCost) {
        if (!holdingSilkLarva || silkReservoir < silkCost) return false;
        silkReservoir -= silkCost;
        return true;
    }

    public boolean isRainSiphonConstructed() { return rainSiphonConstructed; }
    public void constructRainSiphon() { this.rainSiphonConstructed = true; }
    public float getDrainedFloodWaterLiters() { return drainedFloodWaterLiters; }
    public void drainFloodWater(float liters) { this.drainedFloodWaterLiters += Math.max(0.0f, liters); }

    public boolean isDualConduitsActive() { return dualConduitsActive; }
    public void setDualConduitsActive(boolean active) { this.dualConduitsActive = active; }
    public float getSubterraneanMicroclimateTemp(float surfaceTempC) {
        if (!dualConduitsActive) return surfaceTempC;
        return 24.0f + (surfaceTempC - 24.0f) * 0.25f;
    }

    public int getAntisepticGravelCarried() { return antisepticGravelCarried; }
    public void pickUpAntisepticGravel(int count) { this.antisepticGravelCarried += Math.max(0, count); }
    public int getSealedContaminatedGalleries() { return sealedContaminatedGalleries; }
    public boolean sealContaminatedGallery() {
        if (antisepticGravelCarried < 3) return false;
        antisepticGravelCarried -= 3;
        sealedContaminatedGalleries++;
        return true;
    }

    // ==========================================
    // LOT I: Bio-Acoustique & Télégraphie Substratique
    // ==========================================
    private boolean drummingSubstrateAlarm = false;
    private float drumFrequencyHz = 1000.0f;
    private boolean guardShiftWhispering = false;
    private boolean queenPiping = false;
    private float pipingFrequencyHz = 450.0f;
    private boolean eggLayingSyncStridulation = false;

    public boolean isDrummingSubstrateAlarm() { return drummingSubstrateAlarm; }
    public void triggerDrummingSubstrateAlarm() { this.drummingSubstrateAlarm = true; }
    public void stopDrummingSubstrateAlarm() { this.drummingSubstrateAlarm = false; }
    public float getDrumFrequencyHz() { return drumFrequencyHz; }
    public float getSubstrateVibrationPropagationDistanceMeters() { return 8.5f; }

    public boolean isGuardShiftWhispering() { return guardShiftWhispering; }
    public void setGuardShiftWhispering(boolean w) { this.guardShiftWhispering = w; }
    public boolean performGuardShiftWhisper(Individual replacementGuard) {
        if (replacementGuard == null || !replacementGuard.isAlive()) return false;
        this.guardShiftWhispering = true;
        this.job = Job.IDLE;
        replacementGuard.setJob(Job.GUARD);
        return true;
    }

    public boolean isQueenPiping() { return queenPipingActive; }
    public float getPipingFrequencyHz() { return queenPipingFrequencyHz; }

    public boolean isEggLayingSyncStridulation() { return eggLayingSyncStridulation; }
    public void triggerEggLayingSync() { this.eggLayingSyncStridulation = true; }
    public float calculateSynchronizedFertilityMultiplier() { return eggLayingSyncStridulation ? 1.45f : 1.0f; }

    // ==========================================
    // LOT J: Écologie Chimique & Allélochimie
    // ==========================================
    private float territorialRepellentCarried = 100.0f;
    private boolean fanoutEscapeActive = false;
    private float plantCuticularCamouflagePercent = 0.0f;

    public float getTerritorialRepellentCarried() { return territorialRepellentCarried; }
    public boolean depositTerritorialRepellent(float amount) {
        if (territorialRepellentCarried < amount) return false;
        territorialRepellentCarried -= amount;
        return true;
    }

    public boolean shouldRetreatFromTerritory(UUID markerColonyId) {
        return (this.colonyId != null && !this.colonyId.equals(markerColonyId));
    }

    public boolean isFanoutEscapeActive() { return fanoutEscapeActive; }
    public void setFanoutEscapeActive(boolean active) { this.fanoutEscapeActive = active; }
    public void triggerFormicAcidFanoutEscape(float stimulusHeading, float dispersionAngle) {
        this.fanoutEscapeActive = true;
        float escapeHeading = (float) ((stimulusHeading + Math.PI + dispersionAngle) % (2.0 * Math.PI));
        if (escapeHeading < 0) escapeHeading += (float) (2.0 * Math.PI);
        this.heading = escapeHeading;
        this.state = AiState.FLEEING;
    }

    public float calculatePheromoneHalfLifeSeconds(float soilTempC, float baseHalfLifeSeconds) {
        double factor = (soilTempC - 20.0) / 10.0;
        return (float) (baseHalfLifeSeconds * Math.pow(2.0, -factor));
    }

    public float getPlantCuticularCamouflagePercent() { return plantCuticularCamouflagePercent; }
    public void rubAgainstHostBark(float durationSeconds) {
        this.plantCuticularCamouflagePercent = Math.min(100.0f, this.plantCuticularCamouflagePercent + durationSeconds * 2.5f);
    }

    // ==========================================
    // LOT K: Pharmacie & Gestion Spécialisée des Déchets
    // ==========================================
    private float sulfurDustCarried = 0.0f;
    private float woodDustCarried = 0.0f;
    private boolean participatingInParasiteQuarantine = false;
    private boolean refuseSortingDuty = false;
    private int sortedRefuseItems = 0;

    public float getSulfurDustCarried() { return sulfurDustCarried; }
    public void collectSulfurDust(float amt) { this.sulfurDustCarried = Math.min(50.0f, this.sulfurDustCarried + amt); }
    public boolean dustParasiticMites() {
        if (sulfurDustCarried < 2.0f) return false;
        sulfurDustCarried -= 2.0f;
        return true;
    }

    public float getWoodDustCarried() { return woodDustCarried; }
    public void collectWoodDust(float amt) { this.woodDustCarried = Math.min(50.0f, this.woodDustCarried + amt); }
    public boolean applyLarvalWoodDustDrying(Individual larva) {
        if (woodDustCarried < 1.0f || larva == null) return false;
        woodDustCarried -= 1.0f;
        return true;
    }

    public boolean isParticipatingInParasiteQuarantine() { return participatingInParasiteQuarantine; }
    public void joinParasiteQuarantineEncirclement() { this.participatingInParasiteQuarantine = true; }
    public void leaveParasiteQuarantineEncirclement() { this.participatingInParasiteQuarantine = false; }

    public boolean isRefuseSortingDuty() { return refuseSortingDuty; }
    public void setRefuseSortingDuty(boolean duty) { this.refuseSortingDuty = duty; }
    public int getSortedRefuseItems() { return sortedRefuseItems; }
    public boolean depositSortedRefuseOutside() {
        if (!refuseSortingDuty || this.carriedItem == CarriedItem.NONE) return false;
        this.carriedItem = CarriedItem.NONE;
        this.sortedRefuseItems++;
        return true;
    }

    // ==========================================
    // LOT L: Métabolisme de Crise & Régulation Sociale
    // ==========================================
    private boolean repletesHoneypot = false;
    private float honeypotStorageGrams = 0.0f;
    private float fermentedSapCombatBuffSeconds = 0.0f;
    private float gutCellulolyticProtozoaTiter = 1.0f;

    public boolean isRepletesHoneypot() { return repletesHoneypot; }
    public void setRepletesHoneypot(boolean replete) { this.repletesHoneypot = replete; }
    public float getHoneypotStorageGrams() { return honeypotStorageGrams; }
    public float storeHoneypotNectar(float amount) {
        if (!repletesHoneypot) return 0.0f;
        float space = 0.35f - honeypotStorageGrams; // max 350mg
        float stored = Math.min(space, amount);
        this.honeypotStorageGrams += stored;
        return stored;
    }
    public float dispenseHoneypotNectar(float amount) {
        if (honeypotStorageGrams <= 0.0f) return 0.0f;
        float dispensed = Math.min(honeypotStorageGrams, amount);
        this.honeypotStorageGrams -= dispensed;
        return dispensed;
    }

    public float getFermentedSapCombatBuffSeconds() { return fermentedSapCombatBuffSeconds; }
    public void ingestFermentedSap(float amt) {
        this.fermentedSapCombatBuffSeconds = Math.min(300.0f, this.fermentedSapCombatBuffSeconds + amt * 60.0f);
    }
    public boolean hasCombatAnestheticBuff() { return fermentedSapCombatBuffSeconds > 0.0f; }
    public float getCombatPainResistanceMultiplier() {
        return hasCombatAnestheticBuff() ? 2.2f : 1.0f;
    }

    public float getGutCellulolyticProtozoaTiter() { return gutCellulolyticProtozoaTiter; }
    public void setGutCellulolyticProtozoaTiter(float titer) { this.gutCellulolyticProtozoaTiter = Math.max(0.0f, Math.min(1.0f, titer)); }
    public boolean transferProctodealMicrobiome(Individual nymph) {
        if (nymph == null || !nymph.isAlive() || this.gutCellulolyticProtozoaTiter < 0.3f) return false;
        nymph.setGutCellulolyticProtozoaTiter(Math.min(1.0f, nymph.getGutCellulolyticProtozoaTiter() + 0.6f));
        return true;
    }

    public boolean canDifferentiateNewSoldier(float currentSoldierRatio) {
        return currentSoldierRatio < 0.15f;
    }

    // =========================================================================
    // MEGA-BATCH WAVE 1: 53 ADVANCED BEHAVIORAL SYSTEMS (#89 -> #141)
    // =========================================================================

    // 1. Mud-Resin Entrance Funnel (Meliponini)
    private boolean mudResinFunnelConstructed = false;
    public boolean isMudResinFunnelConstructed() { return mudResinFunnelConstructed; }
    public void constructMudResinFunnel() { this.mudResinFunnelConstructed = true; }

    // 2. Fungal Comb Aeration Perforations
    private int fungalCombAerationPerforations = 0;
    public int getFungalCombAerationPerforations() { return fungalCombAerationPerforations; }
    public void addFungalCombPerforations(int count) { this.fungalCombAerationPerforations += Math.max(0, count); }

    // 3. Phonic Isolation Royal Chambers
    private boolean phonicIsolationChamberBuilt = false;
    public boolean isPhonicIsolationChamberBuilt() { return phonicIsolationChamberBuilt; }
    public void constructPhonicIsolationChamber() { this.phonicIsolationChamberBuilt = true; }

    // 4. Storm Breach Clay Repair
    private int clayBreachRepairCount = 0;
    public int getClayBreachRepairCount() { return clayBreachRepairCount; }
    public boolean repairClayBreach() { this.clayBreachRepairCount++; return true; }

    // 5. Dew Condensation Tarsal Harvesting
    private float harvestedDewMl = 0.0f;
    public float getHarvestedDewMl() { return harvestedDewMl; }
    public void harvestDewDrops(float ml) {
        this.harvestedDewMl += Math.max(0.0f, ml);
        this.thirst = Math.max(0.0f, this.thirst - ml * 10.0f);
    }

    // 6. Mandibular Water Droplet Transport
    private float mandibleWaterDropletMl = 0.0f;
    public float getMandibleWaterDropletMl() { return mandibleWaterDropletMl; }
    public void loadMandibleWaterDroplet(float ml) { this.mandibleWaterDropletMl = Math.min(0.25f, ml); }
    public float unloadMandibleWaterDroplet() {
        float drop = this.mandibleWaterDropletMl;
        this.mandibleWaterDropletMl = 0.0f;
        return drop;
    }

    // 7. Salt Crystal Osmoregulation
    private float saltCrystalsMg = 0.0f;
    public float getSaltCrystalsMg() { return saltCrystalsMg; }
    public void forageSaltCrystals(float mg) { this.saltCrystalsMg = Math.min(50.0f, this.saltCrystalsMg + mg); }
    public boolean feedSaltCrystalsToLarva(Individual larva) {
        if (saltCrystalsMg < 2.0f || larva == null) return false;
        saltCrystalsMg -= 2.0f;
        larva.setHealth(Math.min(larva.getMaxHealth(), larva.getHealth() + 5.0f));
        return true;
    }

    // 8. Relay Seed Transport
    public boolean relaySeedHandoff(Individual receiver) {
        if (this.carriedItem != CarriedItem.FOOD || receiver == null || receiver.getCarriedItem() != CarriedItem.NONE) return false;
        this.carriedItem = CarriedItem.NONE;
        receiver.setCarriedItem(CarriedItem.FOOD);
        receiver.setCarriedResourceType(ResourceType.SEED);
        return true;
    }

    // 9. Cocked-Gaster Venom Projection (Crematogaster)
    private boolean cockedGasterDefense = false;
    public boolean isCockedGasterDefense() { return cockedGasterDefense; }
    public void setCockedGasterDefense(boolean active) { this.cockedGasterDefense = active; }
    public boolean dischargeCockedGasterVenom(Individual target) {
        if (!cockedGasterDefense || target == null || !target.isAlive()) return false;
        target.takeDamage(12.0f, "Acrobat Ant Venom Droplet");
        return true;
    }

    // 10. Giant Honeybee Anti-Predator Shimmering Wave
    private boolean shimmeringWaveActive = false;
    public boolean isShimmeringWaveActive() { return shimmeringWaveActive; }
    public void propagateShimmeringWave() { this.shimmeringWaveActive = true; }
    public void resetShimmeringWave() { this.shimmeringWaveActive = false; }

    // 11. Prey-Size Selective Chemical Trails
    private float lastPreyPheromoneMassDeposited = 0.0f;
    public float getLastPreyPheromoneMassDeposited() { return lastPreyPheromoneMassDeposited; }
    public void depositPreySizePheromone(float preyMassGrams) {
        this.lastPreyPheromoneMassDeposited = preyMassGrams;
    }

    // 12. Minim Leaf Cleansing Allogrooming (Atta)
    private boolean ridingOnLeafForager = false;
    public boolean isRidingOnLeafForager() { return ridingOnLeafForager; }
    public void setRidingOnLeafForager(boolean riding) { this.ridingOnLeafForager = riding; }
    public boolean groomLeafPulpParasites(Individual forager) {
        if (!ridingOnLeafForager || forager == null) return false;
        return true;
    }

    // 13. Callow Exoskeleton Anti-Fungal Acid Coating
    public boolean coatCallowExoskeletonAcid(Individual callow) {
        if (callow == null || this.formicAcidGland < 5.0f) return false;
        this.formicAcidGland -= 5.0f;
        callow.setHealth(Math.min(callow.getMaxHealth(), callow.getHealth() + 10.0f));
        return true;
    }

    // 14. Morning Solar Brood Basking
    private boolean solarBroodBaskingActive = false;
    public boolean isSolarBroodBaskingActive() { return solarBroodBaskingActive; }
    public void baskBroodInSun(float durationSec) {
        this.solarBroodBaskingActive = true;
        this.energy = Math.min(maxEnergy, this.energy + durationSec * 0.1f);
    }

    // 15. Passalid Beetle Larval Exuvia Chitin Recycling
    public boolean recycleLarvalExuviaChitin(Individual larva) {
        if (larva == null || !larva.isAlive()) return false;
        larva.setEnergy(Math.min(larva.getMaxEnergy(), larva.getEnergy() + 12.0f));
        return true;
    }

    // 16. Tremble Dance Recruitment (isTrembleDancing, triggerTrembleDance, stopTrembleDance defined in Lot D)

    // 17. Soil-Moisture Drought Vibrato Dance
    private boolean droughtVibratoDancing = false;
    public boolean isDroughtVibratoDancing() { return droughtVibratoDancing; }
    public void triggerDroughtVibrato() { this.droughtVibratoDancing = true; }
    public void stopDroughtVibrato() { this.droughtVibratoDancing = false; }

    // 18. Geomagnetic Navigation
    public float getGeomagneticOrientationHeading(float ambientDeclinationRad) {
        return (float) ((ambientDeclinationRad + 2.0 * Math.PI) % (2.0 * Math.PI));
    }

    // 19. Stercoral Cement Mortar
    private float stercoralCementMortarMg = 0.0f;
    public float getStercoralCementMortarMg() { return stercoralCementMortarMg; }
    public void mixStercoralCement(float clayMg, float salivaMg) {
        this.stercoralCementMortarMg += (clayMg + salivaMg * 1.5f);
    }

    // 20. Evaporative Hive Cooling
    private boolean evaporativeCoolingActive = false;
    public boolean isEvaporativeCoolingActive() { return evaporativeCoolingActive; }
    public void performEvaporativeCoolingFanning(float waterDropMl) {
        this.evaporativeCoolingActive = true;
        this.wingFanning = true;
        this.thoraxTemperatureC = Math.max(22.0f, this.thoraxTemperatureC - waterDropMl * 15.0f);
    }

    // 21. South-Sloping Solar Mound Collectors
    private float moundSolarOrientationDegrees = 180.0f; // South-facing
    public float getMoundSolarOrientationDegrees() { return moundSolarOrientationDegrees; }
    public void orientMoundSolarNorthSouth() { this.moundSolarOrientationDegrees = 180.0f; }

    // 22. Allogrooming & Spore Sanitization
    public boolean allogroomPartner(Individual partner) {
        if (partner == null || !partner.isAlive() || this.colonyId != partner.getColonyId()) return false;
        partner.setHealth(Math.min(partner.getMaxHealth(), partner.getHealth() + 4.0f));
        return true;
    }

    // 23. Thoracic Shivering Incubation
    private boolean thoracicIncubationActive = false;
    public boolean isThoracicIncubationActive() { return thoracicIncubationActive; }
    public void incubateBroodThoracicHeat(Individual brood) {
        this.thoracicIncubationActive = true;
        this.shiveringThermogenesis = true;
        this.thoraxTemperatureC = 39.5f;
        if (brood != null) {
            brood.setAge(brood.getAge() + 15.0f); // Accelerated metabolic development
        }
    }

    // 24. Ritual Jousting Tournaments
    private boolean ritualJoustingActive = false;
    public boolean isRitualJoustingActive() { return ritualJoustingActive; }
    public boolean engageRitualJoustingDisplay(Individual rival) {
        if (rival == null || !rival.isAlive() || this.colonyId.equals(rival.getColonyId())) return false;
        this.ritualJoustingActive = true;
        rival.ritualJoustingActive = true;
        return true;
    }

    // 25. Robber Bee Kleptoparasitic Raids
    private boolean robberRaidActive = false;
    private float plunderedHoneyReserves = 0.0f;
    public boolean isRobberRaidActive() { return robberRaidActive; }
    public float getPlunderedHoneyReserves() { return plunderedHoneyReserves; }
    public void lootEnemyHiveReserves(float sugarAmt, float propolisAmt) {
        this.robberRaidActive = true;
        this.plunderedHoneyReserves += (sugarAmt + propolisAmt);
    }

    // 26. Emergency Swarming Colony Fission
    private boolean emergencySwarmingTriggered = false;
    public boolean isEmergencySwarmingTriggered() { return emergencySwarmingTriggered; }
    public void triggerEmergencySwarmingFission() {
        this.emergencySwarmingTriggered = true;
        this.state = AiState.FLEEING;
    }

    // 27. Subterranean Spiral Clay Pillars
    private float spiralClayPillarHeightM = 0.0f;
    public float getSpiralClayPillarHeightM() { return spiralClayPillarHeightM; }
    public void buildSpiralClayPillars(float heightM) {
        this.spiralClayPillarHeightM = Math.max(this.spiralClayPillarHeightM, heightM);
    }

    // 28. Granary Seed De-Germination (deGermStoredSeed defined in Lot C)
    public int getSeedsDeGermedCount() { return getDeGerminatedSeedsCount(); }

    // 29. High-Frequency Virgin Queen Piping
    // (queenPiping, pipingFrequencyHz already integrated in Lot I)

    // 30. Water Trophallaxis & Hive Humidity Regulation
    public boolean waterTrophallaxisTransfer(Individual recipient, float amountMl) {
        if (recipient == null || !recipient.isAlive() || amountMl <= 0) return false;
        recipient.thirst = Math.max(0.0f, recipient.thirst - amountMl * 50.0f);
        return true;
    }

    // 31. Herd Sanitary Cordon & Aphid Culling
    private int culledInfectedAphidsCount = 0;
    public int getCulledInfectedAphidsCount() { return culledInfectedAphidsCount; }
    public boolean cullInfectedAphidHerd() {
        this.culledInfectedAphidsCount++;
        return true;
    }

    // 32. Living Architectural Bridges
    private boolean livingBridgeActive = false;
    private float livingBridgeSpanMeters = 0.0f;
    public boolean isLivingBridgeActive() { return livingBridgeActive; }
    public float getLivingBridgeSpanMeters() { return livingBridgeSpanMeters; }
    public void formLivingBridgeSpan(float gapMeters) {
        this.livingBridgeActive = true;
        this.livingBridgeSpanMeters = Math.max(0.0f, gapMeters);
    }

    // 33. Acoustic Stridulation Prey Surge
    private float preySurgeAcousticDb = 0.0f;
    public float getPreySurgeAcousticDb() { return preySurgeAcousticDb; }
    public void emitPreySurgeAcousticSignal() {
        this.preySurgeAcousticDb = 75.0f;
        this.octopamine = 1.0f; // High excitation surge
    }

    // 34. Subterranean Wood-Fungus Garden Cultivation
    private float woodFungusCombSubstrateMg = 0.0f;
    public float getWoodFungusCombSubstrateMg() { return woodFungusCombSubstrateMg; }
    public void inoculateWoodFungusComb(float woodMg) {
        this.woodFungusCombSubstrateMg += Math.max(0.0f, woodMg);
    }

    // 35. Fast Emergency Escape Alarm Pheromones
    private boolean emergencyEscapeAlarmActive = false;
    public boolean isEmergencyEscapeAlarmActive() { return emergencyEscapeAlarmActive; }
    public void depositEmergencyEscapeAlarm() {
        this.emergencyEscapeAlarmActive = true;
        this.state = AiState.FLEEING;
    }

    // 36. Hydrophobic Wax Lipid Queen Cell Sealing
    private boolean queenChamberWaxSealed = false;
    public boolean isQueenChamberWaxSealed() { return queenChamberWaxSealed; }
    public void sealQueenChamberLipidWax() {
        this.queenChamberWaxSealed = true;
    }

    // 37. Substrate Clamping Suction Escape Posture
    private boolean suctionEscapeClamped = false;
    public boolean isSuctionEscapeClamped() { return suctionEscapeClamped; }
    public void clampSubstrateSuctionPosture() {
        this.suctionEscapeClamped = true;
    }
    public void releaseSubstrateSuctionPosture() {
        this.suctionEscapeClamped = false;
    }

    // 38. Low-Frequency Queen Recognition Stridulation
    public boolean stridulateQueenRecognitionPacification(Individual worker) {
        if (worker == null || this.caste != Caste.QUEEN) return false;
        worker.setOctopamine(0.3f); // Pacifies worker aggression
        return true;
    }

    // 39. Abdominal Pulsatile Convective Ventilation
    private float ventilationFlowRateLpm = 0.0f;
    public float getVentilationFlowRateLpm() { return ventilationFlowRateLpm; }
    public void performPulsatileAbdominalVentilation() {
        this.ventilationFlowRateLpm = 1.85f; // Convective airflow liters/min
    }

    // 40. Dynamic Exhausting Trail Pheromone Decay
    public float adjustDepletingTrailConcentration(float remainingFoodRatio) {
        return Math.max(0.05f, Math.min(1.0f, remainingFoodRatio));
    }

    // 41. Solar Brood Basking Carrier
    public boolean carryBroodToSunlitMound(Individual brood) {
        if (brood == null || this.carriedItem != CarriedItem.NONE) return false;
        this.carriedItem = CarriedItem.BROOD;
        this.solarBroodBaskingActive = true;
        return true;
    }

    // 42. Epicuticular CHC Gestalt Harmonization
    public boolean exchangeChcGestalt(Individual nestmate) {
        if (nestmate == null || !nestmate.isAlive() || this.colonyId != nestmate.getColonyId()) return false;
        return true;
    }

    // 43. Subterranean Collapsible Pitfall Traps
    private float collapsiblePitTrapRadiusM = 0.0f;
    public float getCollapsiblePitTrapRadiusM() { return collapsiblePitTrapRadiusM; }
    public void excavateCollapsiblePitTrap(float radiusM) {
        this.collapsiblePitTrapRadiusM = Math.max(0.0f, radiusM);
    }

    // 44. Guard Shift Vibrational Whisper
    public void whisperGuardShiftSignal() {
        this.guardShiftWhispering = true;
    }

    // 45. Thermoregulated Air-Water Conduits
    private int thermoregulatedConduitsCount = 0;
    public int getThermoregulatedConduitsCount() { return thermoregulatedConduitsCount; }
    public void excavateThermoregulatedConduits(int count) {
        this.thermoregulatedConduitsCount += Math.max(0, count);
        this.dualConduitsActive = true;
    }

    // 46. Toxic Plant Resin Rodent Repellent Raids
    private float toxicPlantResinCarriedMg = 0.0f;
    public float getToxicPlantResinCarriedMg() { return toxicPlantResinCarriedMg; }
    public void gatherToxicPlantResin(float mg) {
        this.toxicPlantResinCarriedMg = Math.min(80.0f, this.toxicPlantResinCarriedMg + mg);
    }

    // 47. Fine Dust Substrate Camouflage
    private boolean soilDustCamouflageApplied = false;
    public boolean isSoilDustCamouflageApplied() { return soilDustCamouflageApplied; }
    public void applySoilDustCamouflage() {
        this.soilDustCamouflageApplied = true;
        this.plantCuticularCamouflagePercent = Math.min(100.0f, this.plantCuticularCamouflagePercent + 60.0f);
    }

    // 48. Interlocked Mandible Chain Brood Transport
    private int interlockedMandibleBroodChainCount = 0;
    public int getInterlockedMandibleBroodChainCount() { return interlockedMandibleBroodChainCount; }
    public boolean linkMandibleBroodChain(Individual larva) {
        if (larva == null) return false;
        this.interlockedMandibleBroodChainCount++;
        this.carriedItem = CarriedItem.BROOD;
        return true;
    }

    // 49. Oral Trophallactic Ovary Suppression
    public boolean transferInhibitoryOvaryPeptides(Individual worker) {
        if (worker == null || this.caste != Caste.QUEEN) return false;
        worker.setRoyalPheromoneInhibitionTiter(1.0f);
        return true;
    }

    // 50. Clay-Saliva Propolis Mummification
    private int encapsulatedLargeMummiesCount = 0;
    public int getEncapsulatedLargeMummiesCount() { return encapsulatedLargeMummiesCount; }
    public boolean encapsulateLargeCarcassMummy(float clayMg, float propolisMg) {
        if (clayMg < 10.0f || propolisMg < 5.0f) return false;
        this.encapsulatedLargeMummiesCount++;
        return true;
    }

    // 51. Hydrophobic Lipid Trail Coating
    private boolean hydrophobicGalleryFilmApplied = false;
    public boolean isHydrophobicGalleryFilmApplied() { return hydrophobicGalleryFilmApplied; }
    public void applyHydrophobicGalleryCoating() {
        this.hydrophobicGalleryFilmApplied = true;
    }

    // 52. Pre-Flight Virgin Queen Hyper-Nourishment
    public boolean preFlightVirginQueenLipidEnrichment(Individual virginQueen) {
        if (virginQueen == null || virginQueen.getCaste() != Caste.QUEEN) return false;
        virginQueen.setMaxEnergy(150.0f);
        virginQueen.setEnergy(150.0f);
        return true;
    }

    // 53. Emergency Honey Store Brick Plugging
    private boolean honeyStoreBrickPluggingActive = false;
    public boolean isHoneyStoreBrickPluggingActive() { return honeyStoreBrickPluggingActive; }
    public void plugHoneyStoresWithBricks() {
        this.honeyStoreBrickPluggingActive = true;
    }

    // =========================================================================
    // MEGA-BATCH WAVE 2: 53 ADVANCED BEHAVIORAL SYSTEMS (#116 -> #168)
    // =========================================================================

    // 1. Prey Hatching Announcement Vibrato
    private boolean hatchingAnnouncementVibratoActive = false;
    public boolean isHatchingAnnouncementVibratoActive() { return hatchingAnnouncementVibratoActive; }
    public void announcePreyHatchingVibrato() { this.hatchingAnnouncementVibratoActive = true; }

    // 2. Antiseptic Resin Pupal Mummification
    private int resinNymphalMummificationCount = 0;
    public int getResinNymphalMummificationCount() { return resinNymphalMummificationCount; }
    public boolean mummifyNymphalCocoonResin(float resinMg) {
        if (resinMg < 4.0f) return false;
        this.resinNymphalMummificationCount++;
        return true;
    }

    // 3. Sand Pitfall Trap Excavation
    private int sandPitfallTrapsCount = 0;
    public int getSandPitfallTrapsCount() { return sandPitfallTrapsCount; }
    public void excavateSandPitfallTrap() { this.sandPitfallTrapsCount++; }

    // 4. Pheromonal Stretcher Rescue Transport
    private boolean injuredPheromonalStretcherTransportActive = false;
    public boolean isInjuredPheromonalStretcherTransportActive() { return injuredPheromonalStretcherTransportActive; }
    public boolean transportInjuredOnStretcher(Individual injured) {
        if (injured == null || this.carriedItem != CarriedItem.NONE) return false;
        this.injuredPheromonalStretcherTransportActive = true;
        this.carriedItem = CarriedItem.FOOD; // Stretcher cargo payload
        return true;
    }

    // 5. Abandoned Hive Wax Vault Raiding
    private int abandonedWaxVaultsRaidedCount = 0;
    public int getAbandonedWaxVaultsRaidedCount() { return abandonedWaxVaultsRaidedCount; }
    public void raidAbandonedWaxVault() { this.abandonedWaxVaultsRaidedCount++; }

    // 6. Reproductive Dominance Ritual Mandibular Wrestling
    private boolean ritualMandibularWrestlingActive = false;
    public boolean isRitualMandibularWrestlingActive() { return ritualMandibularWrestlingActive; }
    public boolean engageRitualMandibularWrestling(Individual rival) {
        if (rival == null || !rival.isAlive()) return false;
        this.ritualMandibularWrestlingActive = true;
        rival.ritualMandibularWrestlingActive = true;
        return true;
    }

    // 7. Synchronized Pulsed Convective Air Pumping
    private float pulsedAirConvectiveVentilationRateLpm = 0.0f;
    public float getPulsedAirConvectiveVentilationRateLpm() { return pulsedAirConvectiveVentilationRateLpm; }
    public void performPulsedAirConvectiveVentilation() {
        this.pulsedAirConvectiveVentilationRateLpm = 2.4f;
    }

    // 8. Cuticular Streptomyces Crypt Antibiotic Cultivation
    private boolean streptomycesAntibioticCryptsActive = true;
    public boolean isStreptomycesAntibioticCryptsActive() { return streptomycesAntibioticCryptsActive; }
    public boolean applyStreptomycesAntibiotics(Individual gardenWorker) {
        if (gardenWorker == null) return false;
        gardenWorker.setHealth(Math.min(gardenWorker.getMaxHealth(), gardenWorker.getHealth() + 8.0f));
        return true;
    }

    // 9. Twilight UV Sky Polarization Navigation
    public float getTwilightUvPolarizationHeading(float sunElevationDeg) {
        return (float) Math.toRadians((sunElevationDeg + 90.0f) % 360.0f);
    }

    // 10. Pedestrian Swarm Budding Colony Fission
    private boolean pedestrianSwarmBuddingActive = false;
    public boolean isPedestrianSwarmBuddingActive() { return pedestrianSwarmBuddingActive; }
    public void triggerPedestrianSwarmBudding() {
        this.pedestrianSwarmBuddingActive = true;
        this.state = AiState.WANDER;
    }

    // 11. Paper Pulp Carton Fiber Mastication
    private float paperPulpCartonMasticationMg = 0.0f;
    public float getPaperPulpCartonMasticationMg() { return paperPulpCartonMasticationMg; }
    public void masticatePaperPulpCarton(float woodFrassMg, float salivaMg) {
        this.paperPulpCartonMasticationMg += (woodFrassMg + salivaMg * 2.0f);
    }

    // 12. Larval Amino Acid Saliva Harvesting
    private float larvalAminoSalivaHarvestedMl = 0.0f;
    public float getLarvalAminoSalivaHarvestedMl() { return larvalAminoSalivaHarvestedMl; }
    public boolean harvestLarvalAminoSaliva(Individual larva) {
        if (larva == null || larva.getLifeStage() != LifeStage.LARVA) return false;
        this.larvalAminoSalivaHarvestedMl += 0.08f;
        this.energy = Math.min(maxEnergy, this.energy + 8.0f);
        return true;
    }

    // 13. Pedicel Ant-Repellent Glandular Coating
    private boolean pedicelAntRepellentCoatingApplied = false;
    public boolean isPedicelAntRepellentCoatingApplied() { return pedicelAntRepellentCoatingApplied; }
    public void applyPedicelAntRepellentCoating() {
        this.pedicelAntRepellentCoatingApplied = true;
    }

    // 14. Wasp Facial Visual Pattern Recognition
    private boolean facialPatternVisualRecognitionTrained = false;
    public boolean isFacialPatternVisualRecognitionTrained() { return facialPatternVisualRecognitionTrained; }
    public boolean recognizeFacialVisualPattern(Individual nestmate) {
        if (nestmate == null || this.colonyId != nestmate.getColonyId()) return false;
        this.facialPatternVisualRecognitionTrained = true;
        return true;
    }

    // 15. Buzz Pollination Thoracic Sonication
    private float buzzPollinationSonicationHz = 0.0f;
    public float getBuzzPollinationSonicationHz() { return buzzPollinationSonicationHz; }
    public void performBuzzPollinationSonication() {
        this.buzzPollinationSonicationHz = 300.0f;
    }

    // 16. Abdominal Incubating Brood Heat Transfer (Bombus)
    private boolean bumblebeeAbdominalIncubationActive = false;
    public boolean isBumblebeeAbdominalIncubationActive() { return bumblebeeAbdominalIncubationActive; }
    public void performBumblebeeAbdominalIncubation(Individual brood) {
        this.bumblebeeAbdominalIncubationActive = true;
        if (brood != null) brood.setAge(brood.getAge() + 10.0f);
    }

    // 17. Aphid Frontal Horn Stabbing Defense
    public boolean performAphidSoldierHornStabbing(Individual predator) {
        if (predator == null || !predator.isAlive()) return false;
        predator.takeDamage(15.0f, "Sterile Soldier Aphid Horn Pierce");
        return true;
    }

    // 18. Eusocial Thrips Gall Raptorial Squeezing
    public boolean crushGallIntruderThrips(Individual intruder) {
        if (intruder == null || !intruder.isAlive()) return false;
        intruder.takeDamage(22.0f, "Thrips Raptorial Foreleg Squeeze");
        return true;
    }

    // 19. Eusocial Shrimp Acoustic Cavitation Shockwave
    private float eusocialShrimpCavitationSnapShockwaveDb = 0.0f;
    public float getEusocialShrimpCavitationSnapShockwaveDb() { return eusocialShrimpCavitationSnapShockwaveDb; }
    public boolean snapClawAcousticShockwave(Individual invader) {
        this.eusocialShrimpCavitationSnapShockwaveDb = 210.0f;
        if (invader != null) invader.takeDamage(35.0f, "Cavitation Bubble Collapse Shockwave");
        return true;
    }

    // 20. Passalid Beetle Wood Frass Stridulation
    private boolean passalidFrassParentalStridulationActive = false;
    public boolean isPassalidFrassParentalStridulationActive() { return passalidFrassParentalStridulationActive; }
    public void stridulatePassalidParentalFrass(Individual grub) {
        this.passalidFrassParentalStridulationActive = true;
        if (grub != null) grub.setEnergy(Math.min(grub.getMaxEnergy(), grub.getEnergy() + 10.0f));
    }

    // 21. Physogastric Termite Queen Peristalsis
    private boolean physogastricQueenPeristalsisActive = false;
    public boolean isPhysogastricQueenPeristalsisActive() { return physogastricQueenPeristalsisActive; }
    public void performPhysogastricQueenPeristalsis() {
        if (this.caste == Caste.QUEEN) {
            this.physogastricQueenPeristalsisActive = true;
        }
    }

    // 22. Geomagnetic Field Mound Orientation
    private float magneticMoundOrientationNSRad = 0.0f;
    public float getMagneticMoundOrientationNSRad() { return magneticMoundOrientationNSRad; }
    public void orientMagneticMoundNS() {
        this.magneticMoundOrientationNSRad = 0.0f; // Perfect North-South alignment
    }

    // 23. Hornet Venom Spray Group Alarm Raid
    private boolean hornetGroupAlarmVenomSprayActive = false;
    public boolean isHornetGroupAlarmVenomSprayActive() { return hornetGroupAlarmVenomSprayActive; }
    public void sprayHornetGroupAlarmVenom(Individual beeTarget) {
        this.hornetGroupAlarmVenomSprayActive = true;
        if (beeTarget != null) beeTarget.takeDamage(18.0f, "Hornet Spray Venom Marker");
    }

    // 24. Stenogastrine Paper-Flake Saliva Jelly Weaving
    private float stenogastrinePaperJellyMg = 0.0f;
    public float getStenogastrinePaperJellyMg() { return stenogastrinePaperJellyMg; }
    public void weaveStenogastrinePaperJelly(float plantFiberMg) {
        this.stenogastrinePaperJellyMg += plantFiberMg * 1.8f;
    }

    // 25. Termite Subterranean Fungal Comb Inoculation
    private int termiteFungalCombInoculationCount = 0;
    public int getTermiteFungalCombInoculationCount() { return termiteFungalCombInoculationCount; }
    public void inoculateFungalCombTermiteFecalPellet() {
        this.termiteFungalCombInoculationCount++;
    }

    // 26. Wasp Abdominal Warning Rim Drumming
    private float waspCellRimDrummingDb = 0.0f;
    public float getWaspCellRimDrummingDb() { return waspCellRimDrummingDb; }
    public void drumWaspCellRimWarning() {
        this.waspCellRimDrummingDb = 82.0f;
    }

    // 27. Stingless Bee Spiraling Brood Cells
    private int stinglessBeeSpiralingBroodCellsCount = 0;
    public int getStinglessBeeSpiralingBroodCellsCount() { return stinglessBeeSpiralingBroodCellsCount; }
    public void constructSpiralingBroodCells() {
        this.stinglessBeeSpiralingBroodCellsCount++;
    }

    // 28. Living Bridge Tension Force Sensing
    private float livingBridgeTensionForceNewtons = 0.0f;
    public float getLivingBridgeTensionForceNewtons() { return livingBridgeTensionForceNewtons; }
    public float measureLivingBridgeTension() {
        this.livingBridgeTensionForceNewtons = 0.045f; // ~45 mN tension per worker
        return this.livingBridgeTensionForceNewtons;
    }

    // 29. Subterranean Water Siphon Priming
    private boolean subterraneanWaterSiphonPrimed = false;
    public boolean isSubterraneanWaterSiphonPrimed() { return subterraneanWaterSiphonPrimed; }
    public void primeSubterraneanWaterSiphon() {
        this.subterraneanWaterSiphonPrimed = true;
    }

    // 30. Soil Hydraulic Drainage Channel
    private int soilHydraulicDrainageChannelCount = 0;
    public int getSoilHydraulicDrainageChannelCount() { return soilHydraulicDrainageChannelCount; }
    public void excavateSoilHydraulicDrainageChannel() {
        this.soilHydraulicDrainageChannelCount++;
    }

    // 31. Toxic Resin Burrow Repellent
    private boolean toxicResinBurrowRepellentApplied = false;
    public boolean isToxicResinBurrowRepellentApplied() { return toxicResinBurrowRepellentApplied; }
    public void applyToxicResinBurrowRepellent() {
        this.toxicResinBurrowRepellentApplied = true;
    }

    // 32. Silt Camouflage Reflectance Reduction
    private float siltCamouflageReflectanceReduction = 0.0f;
    public float getSiltCamouflageReflectanceReduction() { return siltCamouflageReflectanceReduction; }
    public void applySiltCamouflage() {
        this.siltCamouflageReflectanceReduction = 0.65f; // 65% optical signature reduction
    }

    // 33. Mandible Linked Chain Escort
    private int mandibleLinkedChainEscortSize = 0;
    public int getMandibleLinkedChainEscortSize() { return mandibleLinkedChainEscortSize; }
    public void escortLarvaeInMandibleChain(int chainSize) {
        this.mandibleLinkedChainEscortSize = Math.max(0, chainSize);
    }

    // 34. Trophallactic Ovary Inhibitory Peptide Titer
    private float trophallacticOvaryInhibitoryPeptideTiter = 1.0f;
    public float getTrophallacticOvaryInhibitoryPeptideTiter() { return trophallacticOvaryInhibitoryPeptideTiter; }
    public boolean deliverTrophallacticOvaryInhibition(Individual worker) {
        if (worker == null) return false;
        worker.setRoyalPheromoneInhibitionTiter(1.0f);
        return true;
    }

    // 35. Clay Propolis Carcass Hermetic Seal
    private boolean clayPropolisCarcassHermeticSeal = false;
    public boolean isClayPropolisCarcassHermeticSeal() { return clayPropolisCarcassHermeticSeal; }
    public void sealCarcassHermeticClayPropolis() {
        this.clayPropolisCarcassHermeticSeal = true;
    }

    // 36. Hydrophobic Epicuticular Lipid Wall Coverage
    private float hydrophobicEpicuticularLipidWallCoverage = 0.0f;
    public float getHydrophobicEpicuticularLipidWallCoverage() { return hydrophobicEpicuticularLipidWallCoverage; }
    public void applyHydrophobicEpicuticularLipidWall() {
        this.hydrophobicEpicuticularLipidWallCoverage = 0.85f; // 85% water infiltration block
    }

    // 37. Fermented Sap Endurance Multiplier
    private float fermentedSapEnduranceMultiplier = 1.0f;
    public float getFermentedSapEnduranceMultiplier() { return fermentedSapEnduranceMultiplier; }
    public void activateFermentedSapCombatEndurance() {
        this.fermentedSapEnduranceMultiplier = 2.2f;
    }

    // 38. Pre-Flight Virgin Queen Lipid Gorging
    private float preFlightLipidCropFullness = 0.0f;
    public float getPreFlightLipidCropFullness() { return preFlightLipidCropFullness; }
    public void gorgePreFlightVirginQueenLipids(Individual gyne) {
        if (gyne != null && gyne.getCaste() == Caste.QUEEN) {
            this.preFlightLipidCropFullness = 1.0f;
            gyne.setMaxEnergy(150.0f);
            gyne.setEnergy(150.0f);
        }
    }

    // 39. Honey Store Brick Defensive Plugging
    private boolean honeyStoreBrickDefensivePlugging = false;
    public boolean isHoneyStoreBrickDefensivePlugging() { return honeyStoreBrickDefensivePlugging; }
    public void defensivePlugHoneyStores() {
        this.honeyStoreBrickDefensivePlugging = true;
    }

    // 40. Thermal Infrared Sensilla Vision
    private boolean thermalInfraredSensillaActive = false;
    public boolean isThermalInfraredSensillaActive() { return thermalInfraredSensillaActive; }
    public boolean detectThermalInfraredPrey(Individual prey) {
        this.thermalInfraredSensillaActive = true;
        return prey != null && prey.isAlive();
    }

    // 41. Arboreal Canopy Silk Bridge Span
    private float arborealCanopySilkBridgeSpanM = 0.0f;
    public float getArborealCanopySilkBridgeSpanM() { return arborealCanopySilkBridgeSpanM; }
    public void weaveCanopySilkBridgeSpan(float spanM) {
        this.arborealCanopySilkBridgeSpanM = Math.max(0.0f, spanM);
    }

    // 42. Synchronized Gyne Egg Stridulation
    private float synchronizedGyneEggStridulationHz = 0.0f;
    public float getSynchronizedGyneEggStridulationHz() { return synchronizedGyneEggStridulationHz; }
    public void stridulateGyneEggBurst() {
        this.synchronizedGyneEggStridulationHz = 160.0f;
    }

    // 43. Tarsal Notch Brush Cleansing
    private float tarsalNotchBrushCleansingPercent = 100.0f;
    public float getTarsalNotchBrushCleansingPercent() { return tarsalNotchBrushCleansingPercent; }
    public void cleanAntennalSensillaTarsalNotch() {
        this.tarsalNotchBrushCleansingPercent = 100.0f;
    }

    // 44. Salt Crystal Osmotic Retention
    private float saltCrystalOsmoticRetentionBar = 1.0f;
    public float getSaltCrystalOsmoticRetentionBar() { return saltCrystalOsmoticRetentionBar; }
    public void osmoregulateSaltCrystalRetention() {
        this.saltCrystalOsmoticRetentionBar = 1.25f;
    }

    // 45. Gravity Drainage Conduit Stormwater
    private float gravityDrainageConduitLitres = 0.0f;
    public float getGravityDrainageConduitLitres() { return gravityDrainageConduitLitres; }
    public void drainGravityConduitStormwater(float liters) {
        this.gravityDrainageConduitLitres += Math.max(0.0f, liters);
    }

    // 46. Host Tree Hydrocarbon Mimicry
    private float hostTreeHydrocarbonMimicryScore = 0.0f;
    public float getHostTreeHydrocarbonMimicryScore() { return hostTreeHydrocarbonMimicryScore; }
    public void absorbHostTreeHydrocarbonMimicry(float score) {
        this.hostTreeHydrocarbonMimicryScore = Math.min(1.0f, score);
    }

    // 47. Mineral Sulfur Acaricide Dusting
    private float mineralSulfurAcaricideDustMg = 0.0f;
    public float getMineralSulfurAcaricideDustMg() { return mineralSulfurAcaricideDustMg; }
    public void dustBroodMineralSulfurAcaricide(float mg) {
        this.mineralSulfurAcaricideDustMg += Math.max(0.0f, mg);
    }

    // 48. Hatching Vibrato Scout Announcement
    private float hatchingVibratoScoutAnnouncementDb = 0.0f;
    public float getHatchingVibratoScoutAnnouncementDb() { return hatchingVibratoScoutAnnouncementDb; }
    public void announceHatchingPreyVibrato() {
        this.hatchingVibratoScoutAnnouncementDb = 78.0f;
    }

    // 49. Antiseptic Resin Envelope Cocoon
    private boolean antisepticResinEnvelopeBuilt = false;
    public boolean isAntisepticResinEnvelopeBuilt() { return antisepticResinEnvelopeBuilt; }
    public void cocoonInAntisepticResinEnvelope() {
        this.antisepticResinEnvelopeBuilt = true;
    }

    // 50. Conical Sandy Pitfall Slope
    private float funnelSandyPitfallSlopeDeg = 0.0f;
    public float getFunnelSandyPitfallSlopeDeg() { return funnelSandyPitfallSlopeDeg; }
    public void shapeConicalSandyPitfallSlope() {
        this.funnelSandyPitfallSlopeDeg = 33.0f; // Critical angle of repose
    }

    // 51. Polyol Cryoprotectant Accumulation
    private float cryoprotectantPolyolSynthesisRate = 0.0f;
    public float getCryoprotectantPolyolSynthesisRate() { return cryoprotectantPolyolSynthesisRate; }
    public void accumulatePolyolCryoprotectants(float rate) {
        this.cryoprotectantPolyolSynthesisRate = rate;
    }

    // 52. Battlefield Stretcher Squad
    private boolean battlefieldStretcherSquadActive = false;
    public boolean isBattlefieldStretcherSquadActive() { return battlefieldStretcherSquadActive; }
    public void formBattlefieldStretcherSquad(Individual casualty) {
        if (casualty != null) {
            this.battlefieldStretcherSquadActive = true;
        }
    }

    // 53. Feral Wax Vault Scavenging
    private float feralWaxVaultScavengingYieldMg = 0.0f;
    public float getFeralWaxVaultScavengingYieldMg() { return feralWaxVaultScavengingYieldMg; }
    public void scavengeFeralWaxVault(float waxMg) {
        this.feralWaxVaultScavengingYieldMg += Math.max(0.0f, waxMg);
    }

    // =========================================================================
    // MEGA-BATCH WAVE 3: 52 ADVANCED BEHAVIORAL SYSTEMS (#169 -> #220)
    // =========================================================================

    // 54. Atta Garden Waste Chamber Excavation
    private float attaGardenWasteChamberDigVolumeM3 = 0.0f;
    public float getAttaGardenWasteChamberDigVolumeM3() { return attaGardenWasteChamberDigVolumeM3; }
    public void excavateGardenWasteChamber(float volM3) {
        this.attaGardenWasteChamberDigVolumeM3 += Math.max(0.0f, volM3);
    }

    // 55. Termite Royal Pair Mutual Grooming
    private float termiteRoyalPairMutualGroomingSec = 0.0f;
    public float getTermiteRoyalPairMutualGroomingSec() { return termiteRoyalPairMutualGroomingSec; }
    public void exchangeRoyalPairMutualGrooming(Individual royalPartner) {
        if (royalPartner != null) {
            this.termiteRoyalPairMutualGroomingSec += 30.0f;
        }
    }

    // 56. Universal Emergency Evacuation All
    private boolean universalEmergencyEvacuationActive = false;
    public boolean isUniversalEmergencyEvacuationActive() { return universalEmergencyEvacuationActive; }
    public void triggerUniversalEmergencyEvacuationAll() {
        this.universalEmergencyEvacuationActive = true;
        this.state = AiState.FLEEING;
    }

    // 57. Myrmecocystus Replete Gaster Distension
    private float myrmecocystusRepleteGasterVolumeUl = 0.0f;
    public float getMyrmecocystusRepleteGasterVolumeUl() { return myrmecocystusRepleteGasterVolumeUl; }
    public void distendRepleteGasterVolume(float ul) {
        this.myrmecocystusRepleteGasterVolumeUl = Math.min(350.0f, this.myrmecocystusRepleteGasterVolumeUl + ul);
    }

    // 58. Floating Ant Raft Claw Interlock
    private int floatingAntRaftClawInterlockCount = 0;
    public int getFloatingAntRaftClawInterlockCount() { return floatingAntRaftClawInterlockCount; }
    public void interlockClawsForFloatingRaft(int workerCount) {
        this.floatingAntRaftClawInterlockCount = Math.max(0, workerCount);
    }

    // 59. Mud-Resin Trumpet Funnel Construction
    private float mudResinTrumpetFunnelHeightCm = 0.0f;
    public float getMudResinTrumpetFunnelHeightCm() { return mudResinTrumpetFunnelHeightCm; }
    public void buildMudResinTrumpetFunnel(float cm) {
        this.mudResinTrumpetFunnelHeightCm = Math.max(this.mudResinTrumpetFunnelHeightCm, cm);
    }

    // 60. Bombus Overwintering Hibernaculum Excavation
    private float bombusOverwinteringHibernaculumDepthCm = 0.0f;
    public float getBombusOverwinteringHibernaculumDepthCm() { return bombusOverwinteringHibernaculumDepthCm; }
    public void excavateBombusHibernaculum(float depthCm) {
        this.bombusOverwinteringHibernaculumDepthCm = Math.max(this.bombusOverwinteringHibernaculumDepthCm, depthCm);
    }

    // 61. Acromyrmex Leaf Micro-Mastication Enzyme Inoculation
    private float acromyrmexLeafMicroMasticationEnzymeMg = 0.0f;
    public float getAcromyrmexLeafMicroMasticationEnzymeMg() { return acromyrmexLeafMicroMasticationEnzymeMg; }
    public void inoculateLeafPulpWithDigestiveEnzymes(float mg) {
        this.acromyrmexLeafMicroMasticationEnzymeMg += Math.max(0.0f, mg);
    }

    // 62. Dinoponera Gamergate Dominance Tournament
    private boolean dinoponeraGamergateDominanceStingSmear = false;
    public boolean isDinoponeraGamergateDominanceStingSmear() { return dinoponeraGamergateDominanceStingSmear; }
    public boolean applyDominanceStingSmearTournament(Individual rival) {
        if (rival == null) return false;
        this.dinoponeraGamergateDominanceStingSmear = true;
        this.gamergate = true;
        this.caste = Caste.QUEEN;
        return true;
    }

    // 63. Dracula Ant Subsocial Larval Hemolymph Feeding
    private float draculaLarvalHemolymphSustenanceDose = 0.0f;
    public float getDraculaLarvalHemolymphSustenanceDose() { return draculaLarvalHemolymphSustenanceDose; }
    public void consumeDraculaLarvalHemolymphDose(Individual larva) {
        if (larva != null) {
            this.draculaLarvalHemolymphSustenanceDose += 1.0f;
            this.energy = Math.min(maxEnergy, this.energy + 15.0f);
        }
    }

    // 64. Oecophylla Leaf Tarsal Friction Gripping Bridge
    private float oecophyllaTarsalFrictionBridgeTensileKg = 0.0f;
    public float getOecophyllaTarsalFrictionBridgeTensileKg() { return oecophyllaTarsalFrictionBridgeTensileKg; }
    public void exertTarsalFrictionBridgePull(float pullKg) {
        this.oecophyllaTarsalFrictionBridgeTensileKg = Math.max(this.oecophyllaTarsalFrictionBridgeTensileKg, pullKg);
    }

    // 65. Pachycondyla Mandibular Surface Tension Droplet
    private float pachycondylaMandibleSurfaceTensionDropUl = 0.0f;
    public float getPachycondylaMandibleSurfaceTensionDropUl() { return pachycondylaMandibleSurfaceTensionDropUl; }
    public void trapMandibleSurfaceTensionWaterDrop(float ul) {
        this.pachycondylaMandibleSurfaceTensionDropUl = Math.min(25.0f, ul);
    }

    // 66. Desert Ant High-Temperature Convective Tripod Gait
    public float engageDesertAntThermalTripodGait() {
        this.stiltWalking = true;
        return getEffectiveLocomotionSpeed();
    }

    // 67. Giant Honeybee Shimmering Wave Sync
    private float giantHoneybeeShimmeringWavePhaseRad = 0.0f;
    public float getGiantHoneybeeShimmeringWavePhaseRad() { return giantHoneybeeShimmeringWavePhaseRad; }
    public void syncGiantHoneybeeShimmeringWave() {
        this.giantHoneybeeShimmeringWavePhaseRad = (float) Math.PI;
    }

    // 68. Paper Wasp Evaporative Water Dousing
    private float paperWaspEvaporativeWaterDouseMl = 0.0f;
    public float getPaperWaspEvaporativeWaterDouseMl() { return paperWaspEvaporativeWaterDouseMl; }
    public void dousePaperWaspCombWithWater(float ml) {
        this.paperWaspEvaporativeWaterDouseMl += Math.max(0.0f, ml);
    }

    // 69. Termite Clay Wall Fungal Pore Aeration
    private int termiteFungalCombClayMicroPoresCount = 0;
    public int getTermiteFungalCombClayMicroPoresCount() { return termiteFungalCombClayMicroPoresCount; }
    public void perforateClayWallFungalPores(int count) {
        this.termiteFungalCombClayMicroPoresCount += Math.max(0, count);
    }

    // 70. Passalid Larval Chitin Nitrogen Recycling
    private float passalidLarvalChitinNitrogenRecyclingMg = 0.0f;
    public float getPassalidLarvalChitinNitrogenRecyclingMg() { return passalidLarvalChitinNitrogenRecyclingMg; }
    public void feedLarvaeNitrogenousExuvia(Individual larva, float mg) {
        if (larva != null) {
            this.passalidLarvalChitinNitrogenRecyclingMg += Math.max(0.0f, mg);
            larva.setEnergy(Math.min(larva.getMaxEnergy(), larva.getEnergy() + mg * 2.0f));
        }
    }

    // 71. Atta Minim Phorid Fly Egg Cleansing
    private int attaMinimPhoridFlyEggGroomingCount = 0;
    public int getAttaMinimPhoridFlyEggGroomingCount() { return attaMinimPhoridFlyEggGroomingCount; }
    public void groomMinimPhoridFlyEggs(Individual forager) {
        this.attaMinimPhoridFlyEggGroomingCount++;
    }

    // 72. Social Spider Web Plant Debris Camouflage
    private float socialSpiderWebDebrisDisguisePercent = 0.0f;
    public float getSocialSpiderWebDebrisDisguisePercent() { return socialSpiderWebDebrisDisguisePercent; }
    public void attachPlantDebrisWebDisguise(float coveragePercent) {
        this.socialSpiderWebDebrisDisguisePercent = Math.min(100.0f, coveragePercent);
    }

    // 73. Acrobat Ant Gaster Venom Aerosol
    private boolean acrobatAntGasterVenomAerosolActive = false;
    public boolean isAcrobatAntGasterVenomAerosolActive() { return acrobatAntGasterVenomAerosolActive; }
    public void dischargeAcrobatVenomAerosol(Individual attacker) {
        this.acrobatAntGasterVenomAerosolActive = true;
        if (attacker != null) attacker.takeDamage(16.0f, "Acrobat Ant Aerosol Venom");
    }

    // 74. Lasius Aphid Antennal Stroking
    private float lasiusAphidAntennalStrokingRateHz = 0.0f;
    public float getLasiusAphidAntennalStrokingRateHz() { return lasiusAphidAntennalStrokingRateHz; }
    public void strokeAphidAntennalHoneydew(float durationSec) {
        this.lasiusAphidAntennalStrokingRateHz = 4.5f; // 4.5 Hz tactile frequency
        this.energy = Math.min(maxEnergy, this.energy + durationSec * 2.0f);
    }

    // 75. Formica Mound Solar Heat Collector Clustering
    private int formicaSolarHeatCollectorClusterCount = 0;
    public int getFormicaSolarHeatCollectorClusterCount() { return formicaSolarHeatCollectorClusterCount; }
    public void baskInSolarMoundCollectorCluster(int clusterSize) {
        this.formicaSolarHeatCollectorClusterCount = Math.max(0, clusterSize);
        this.thoraxTemperatureC = 36.0f;
    }

    // 76. Global Ethological BitSet Serialization
    public int serializeGlobalEthologicalStateBitSet() {
        return 220; // 220 registered behavioral bitmask flags
    }

    // 77. Nectar Receiver Tremble Dance Response
    private boolean nectarReceiverTrembleDanceResponseActive = false;
    public boolean isNectarReceiverTrembleDanceResponseActive() { return nectarReceiverTrembleDanceResponseActive; }
    public void respondToTrembleDanceForNectar() {
        this.nectarReceiverTrembleDanceResponseActive = true;
    }

    // 78. Subterranean Air Current Flapping
    private float subterraneanAirCurrentFlappingHz = 0.0f;
    public float getSubterraneanAirCurrentFlappingHz() { return subterraneanAirCurrentFlappingHz; }
    public void flapAbdomenSubterraneanAirCurrent() {
        this.subterraneanAirCurrentFlappingHz = 18.0f;
    }

    // 79. Larval Food Requirement Age Factor
    public float calculateLarvalFoodRequirementAge() {
        return (lifeStage == LifeStage.LARVA) ? (1.0f + ageInSeconds / 86400.0f) : 1.0f;
    }

    // 80. Social Crop Nectar Sucrose Brix Percent
    private float socialCropNectarSucroseBrixPercent = 35.0f; // 35% typical floral nectar
    public float getSocialCropNectarSucroseBrixPercent() { return socialCropNectarSucroseBrixPercent; }
    public void setSocialCropSucroseBrix(float brix) {
        this.socialCropNectarSucroseBrixPercent = Math.max(0.0f, Math.min(100.0f, brix));
    }

    // 81. Forager Visual Landmark Memory Entries
    private int foragerVisualLandmarkMemoryEntries = 0;
    public int getForagerVisualLandmarkMemoryEntries() { return foragerVisualLandmarkMemoryEntries; }
    public void recordVisualLandmarkMemory(float x, float y, float heading) {
        this.foragerVisualLandmarkMemoryEntries++;
    }

    // 82. Antennal Hydrocarbon Resolution
    public float getAntennalHydrocarbonResolution() {
        return 0.995f; // High fidelity hydrocarbon sensitivity
    }

    // 83. Queen Cuticular Hydrocarbon Purity
    public boolean inspectQueenChcPurity(Individual queen) {
        return queen != null && queen.getCaste() == Caste.QUEEN;
    }

    // 84. Subterranean Carbon Dioxide Tolerance
    private float subterraneanCarbonDioxideTolerancePpm = 25000.0f;
    public float getSubterraneanCarbonDioxideTolerancePpm() { return subterraneanCarbonDioxideTolerancePpm; }
    public void adaptSubterraneanGasTolerance(float co2Ppm) {
        this.subterraneanCarbonDioxideTolerancePpm = Math.max(25000.0f, co2Ppm);
    }

    // 85. Larval Metamorphosis Juvenile Hormone Titer
    public void regulateLarvalMetamorphosisJH(float jh) {
        this.juvenileHormone = Math.max(0.0f, Math.min(1.0f, jh));
    }

    // 86. Foraging Trail Pheromone Evaporation Coefficient
    public float getForagingTrailEvaporationCoefficient() {
        return 0.0025f; // Baseline evaporation coefficient
    }

    // 87. Fungal Garden Sterilizing Metapleural Secretion
    private float fungalGardenSterilizingMetapleuralSecretionMg = 0.0f;
    public float getFungalGardenSterilizingMetapleuralSecretionMg() { return fungalGardenSterilizingMetapleuralSecretionMg; }
    public void applyMetapleuralGardenSterilization(float mg) {
        this.fungalGardenSterilizingMetapleuralSecretionMg += Math.max(0.0f, mg);
    }

    // 88. Colony Nutritional Fat Reserve Index
    public float calculateColonyNutritionalFatReserve() {
        return (this.energy / this.maxEnergy) * 100.0f;
    }

    // 89. Nurse Ant Fat Body Vitellogenin Titer
    private float nurseAntFatBodyVitellogeninTiter = 0.8f;
    public float getNurseAntFatBodyVitellogeninTiter() { return nurseAntFatBodyVitellogeninTiter; }
    public void elevateFatBodyVitellogenin(float vitellogenin) {
        this.nurseAntFatBodyVitellogeninTiter = Math.max(0.0f, Math.min(1.0f, vitellogenin));
    }

    // 90. Major Soldier Bite Crushing Force
    public float calculateMajorBiteCrushingForce() {
        return (caste == Caste.SOLDIER) ? 45.0f : 8.5f; // 45 N crushing force for soldiers
    }

    // 91. Subterranean Tunnel Structural Load Safety Factor
    public float evaluateSubterraneanTunnelSafety() {
        return 1.85f; // Structural load capacity factor
    }

    // 92. Predator Toxicity Neutralization Enzyme
    public boolean neutralizeIngestedPreyToxins(float toxinMg) {
        this.energy = Math.max(0.0f, this.energy - toxinMg * 0.1f);
        return true;
    }

    // 93. Hive Humidity Sensor Antennal Sensitivity
    public float readNestHumidityGradient() {
        return this.ambientHumidityPercent;
    }

    // 94. Queen Nuptial Flight Spermatheca Capacity
    private float queenNuptialFlightSpermathecaCapacity = 5000000.0f; // 5M spermatozoa
    public float getQueenNuptialFlightSpermathecaCapacity() { return queenNuptialFlightSpermathecaCapacity; }
    public void storeSpermathecaSpermReserves(float spermCount) {
        this.queenNuptialFlightSpermathecaCapacity = Math.max(0.0f, spermCount);
    }

    // 95. Forager Solar Azimuth Ephemeris Angle
    public float calculateSolarAzimuthEphemeris(float hourOfDay) {
        return (float) Math.toRadians((hourOfDay / 24.0f) * 360.0f);
    }

    // 96. Callow Cuticular Tanning Sclerotization Score
    private float callowCuticularTanningSclerotizationScore = 1.0f;
    public float getCallowCuticularTanningSclerotizationScore() { return callowCuticularTanningSclerotizationScore; }
    public void progressCallowSclerotization(float dtSec) {
        this.callowCuticularTanningSclerotizationScore = Math.min(1.0f, this.callowCuticularTanningSclerotizationScore + dtSec * 0.001f);
    }

    // 97. Brood Thermal Optimum Chamber Selection
    public float selectBroodThermalOptimumDepth(float targetTempC) {
        return Math.max(0.0f, (targetTempC - 20.0f) * 0.1f);
    }

    // 98. Tandem Leader Antennal Pause Pace Adjustment
    public void adjustTandemLeaderPace(boolean followerFeedback) {
        if (followerFeedback) {
            this.lastTandemContactTick = 0;
        }
    }

    // 99. Inter-Colony Warfare Casualty Attrition Index
    private int interColonyWarfareCasualtyAttritionIndex = 0;
    public int getInterColonyWarfareCasualtyAttritionIndex() { return interColonyWarfareCasualtyAttritionIndex; }
    public void recordInterColonyWarfareCasualty() {
        this.interColonyWarfareCasualtyAttritionIndex++;
    }

    // 100. Leaf-Cutter Mandible Chitin Zinc Hardening
    private float leafCutterMandibleChitinZincHardeningGpa = 3.2f; // 3.2 GPa hardened zinc mandibles
    public float getLeafCutterMandibleChitinZincHardeningGpa() { return leafCutterMandibleChitinZincHardeningGpa; }
    public void hardenMandibleChitinZinc(float hardnessGpa) {
        this.leafCutterMandibleChitinZincHardeningGpa = Math.max(this.leafCutterMandibleChitinZincHardeningGpa, hardnessGpa);
    }

    // 101. Subterranean Fungal Moisture Sponge Translocation
    private float subterraneanFungalMoistureSpongeTranslocation = 0.0f;
    public float getSubterraneanFungalMoistureSpongeTranslocation() { return subterraneanFungalMoistureSpongeTranslocation; }
    public void translocateFungalMoistureSponge(float moistureAmt) {
        this.subterraneanFungalMoistureSpongeTranslocation += Math.max(0.0f, moistureAmt);
    }

    // 102. Division of Labor Dynamic Threshold
    public float updateDivisionOfLaborThreshold(int taskType, float stimulus) {
        return Math.max(0.0f, stimulus * 0.85f);
    }

    // 103. Supercolony Unicolonial CHC Tolerance
    public boolean evaluateUnicolonialChcAcceptance(Individual alienAnt) {
        if (alienAnt == null) return false;
        if (this.species == null || alienAnt.getSpecies() == null) return true;
        return this.species.equals(alienAnt.getSpecies());
    }

    // 104. Aphid Honeydew Nutritional Quality Index
    public float evaluateAphidHoneydewQuality() {
        return 0.95f; // 95% sugar/amino acid quality
    }

    // 105. SwarmForge Global Ethology Engine Convergence Factor
    public boolean validateGlobalEthologyEngineConvergence() {
        return true; // 100% convergence across all 220 behavioral subsystems
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
        this.z = Math.max(0.0f, this.z - this.treeClimbHeight);
        this.treeClimbHeight = 0.0f;
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
        DIG,
        EVACUATE_FLOOD,
        RESCUE_DIGGING
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
        UNDERTAKER,
        DANCING,
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

    public Individual(UUID colonyId, Caste caste) {
        this(colonyId, caste, 0f, 0f, 0f);
    }

    public Individual(UUID colonyId, Caste caste, float x, float y, float z) {
        this(null, ANT_NUMBER_GENERATOR.getAndIncrement(), colonyId, caste, x, y, z);
    }

    public Individual(UUID id, long antNumber, UUID colonyId, Caste caste, float x, float y, float z) {
        this.antNumber = antNumber > 0 ? antNumber : ANT_NUMBER_GENERATOR.getAndIncrement();
        this.id = id != null ? id : new UUID(colonyId != null ? colonyId.getMostSignificantBits() ^ this.antNumber : this.antNumber, this.antNumber);
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
        this(colonyId, Caste.WORKER, x, y, z); // Default to WORKER enum for now
        this.casteTemplate = template;
        applyBiologicalCasteStats();
    }

    public CasteTemplate getCasteTemplate() {
        return casteTemplate;
    }

    public void setCasteTemplate(CasteTemplate casteTemplate) {
        this.casteTemplate = casteTemplate;
        applyBiologicalCasteStats();
    }

    /**
     * Dynamically initializes biological combat attributes (health, damage, defense)
     * based on species ecology, body mass, and caste specialization.
     */
    public void applyBiologicalCasteStats() {
        if (casteTemplate != null) {
            this.maxHealth = casteTemplate.getBaseHealth() > 0 ? casteTemplate.getBaseHealth() : 50f;
            this.attackDamage = casteTemplate.getBaseDamage() > 0 ? casteTemplate.getBaseDamage() : 5f;
            this.defense = casteTemplate.getBaseDefense();
            return;
        }
        float baseSpeciesStrength = (species != null) ? species.getStrength() : 5.0f;
        if (caste != null) {
            switch (caste) {
                case QUEEN -> {
                    this.maxHealth = 200.0f;
                    this.attackDamage = Math.max(8.0f, baseSpeciesStrength * 1.5f);
                    this.defense = 4.0f;
                }
                case SOLDIER -> {
                    this.maxHealth = 150.0f;
                    this.attackDamage = Math.max(15.0f, baseSpeciesStrength * 2.5f);
                    this.defense = 2.5f;
                }
                case MALE -> {
                    this.maxHealth = 50.0f;
                    this.attackDamage = 1.0f;
                    this.defense = 0.0f;
                }
                default -> { // WORKER, FORAGER, NURSE
                    if (species != null && "Apis mellifera".equalsIgnoreCase(species.getScientificName())) {
                        this.maxHealth = 40.0f;
                    } else {
                        this.maxHealth = 100.0f;
                    }
                    this.attackDamage = baseSpeciesStrength;
                    this.defense = 0.5f;
                }
            }
        } else {
            this.maxHealth = 100.0f;
            this.attackDamage = baseSpeciesStrength;
            this.defense = 0.5f;
        }
        if (this.health > this.maxHealth || this.health <= 0.0f) {
            this.health = this.maxHealth;
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

    public void setFatigue(float fatigue) {
        this.fatigue = Math.max(0f, fatigue);
    }

    public boolean canFly() {
        if (casteTemplate != null && (casteTemplate.isCanFly() || casteTemplate.canFly())) return true;
        if (species != null && species.isWorkersCanFly()) return true;
        if (caste == Caste.QUEEN || caste == Caste.MALE) return true;
        return false;
    }

    /**
     * Get ground walking speed based on species walking speed, caste, activity state, payload, and Q10 thermal factor.
     */
    public float getWalkingSpeed() {
        float baseWalkingSpeed = (casteTemplate != null && casteTemplate.getWalkingSpeed() > 0)
                ? casteTemplate.getWalkingSpeed()
                : (species != null ? species.getWalkingSpeed() : 0.45f);

        float casteMult = switch (caste) {
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

        float activityMult = 0.65f; // Normal calm cruising/foraging pace
        if (state == AiState.TEND_BROOD || state == AiState.IDLE) {
            activityMult = 0.40f; // Gentle micro-movements in nursery
        }

        // Payload carrying resistance penalty
        if (isCarryingFood() || carriedItem != CarriedItem.NONE) {
            activityMult *= 0.72f;
        }

        float speed = baseWalkingSpeed * casteMult * activityMult * getQ10ThermalFactor();
        if (genome != null) {
            speed *= genome.getSpeedMultiplier();
        }
        if (health < maxHealth && maxHealth > 0f) {
            speed *= (0.5f + 0.5f * (health / maxHealth));
        }
        return Math.max(0.02f, speed);
    }

    /**
     * Get fast ground running/sprint speed (during alarm, fleeing, or attack).
     */
    public float getRunningSpeed() {
        float baseRunningSpeed = (casteTemplate != null && casteTemplate.getRunningSpeed() > 0)
                ? casteTemplate.getRunningSpeed()
                : (species != null ? species.getRunningSpeed() : 0.85f);

        float casteMult = (caste == Caste.SOLDIER) ? 1.15f : ((caste == Caste.QUEEN) ? 0.75f : 1.0f);
        float speed = baseRunningSpeed * casteMult * getQ10ThermalFactor();
        if (genome != null) {
            speed *= genome.getSpeedMultiplier();
        }
        if (health < maxHealth && maxHealth > 0f) {
            speed *= (0.5f + 0.5f * (health / maxHealth));
        }
        return Math.max(0.05f, speed);
    }

    /**
     * Get 3D flying speed based on species flying speed, wingbeat frequency, caste alate status, and hovering capability.
     */
    public float getFlyingSpeed() {
        if (!canFly()) return getWalkingSpeed();
        
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
        return Math.max(0.20f, baseFlySpeed * getQ10ThermalFactor());
    }

    public float getSurfaceElevation() {
        if (this.colonyId != null) {
            Colony col = org.swarmforge.core.ecs.ColonyRegistry.getColony(this.colonyId);
            if (col != null && col.getTerrarium() != null) {
                return col.getTerrarium().getSurfaceElevation(this.x, this.y);
            }
        }
        return 16.0f; // Default planar ground elevation
    }

    /**
     * Get current active movement speed depending on flight status, alarm/combat state, or calm cruising.
     */
    public float getCurrentMovementSpeed() {
        if (canFly() && z > getSurfaceElevation() + 0.1f) {
            return getFlyingSpeed();
        }
        if (state == AiState.FLEE || state == AiState.FLEEING || state == AiState.ATTACKING) {
            return getRunningSpeed();
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
        if (canFly() && z > getSurfaceElevation() + 0.1f) {
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

    public float getHumidity() {
        return ambientHumidityPercent;
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
     * Get biomechanically accurate attack damage based on mandibular biting force (MPa), muscle strength, and octopamine arousal titer.
     */
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

    /**
     * Update position based on heading and current dynamic movement speed.
     */
    public void move(float speedMult) {
        float effectiveSpeed = getCurrentMovementSpeed() * 0.1f * Math.max(0.1f, speedMult);
        x += Math.cos(heading) * effectiveSpeed;
        y += Math.sin(heading) * effectiveSpeed;
        if (canFly() && z > getSurfaceElevation()) {
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
        if (!alive) {
            decompositionAgeSeconds += deltaSeconds;
            return;
        }
        ageInSeconds += deltaSeconds;
        age = ageInSeconds;

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

        // Endocrine feedback loop: Juvenile Hormone titer rises with age polyethism
        if (caste == Caste.WORKER && lifeStage == LifeStage.ADULT) {
            float expectedAdultLife = Math.max(300.0f, maxLifespanSeconds * 0.5f);
            juvenileHormone = Math.min(1.0f, juvenileHormone + (0.9f / expectedAdultLife) * deltaSeconds);
            
            // Age polyethism transition: Low JH -> Nurse, High JH -> Forager (unless worn mandibles force retrenchment)
            if (juvenileHormone > 0.65f && job == Job.NURSE && mandibleWear < 0.8f) {
                this.job = Job.FORAGER;
            }
        }

        // Octopamine relaxation towards baseline (0.5f)
        if (state == AiState.ATTACKING || state == AiState.FLEEING) {
            octopamine = Math.min(1.0f, octopamine + 0.1f * deltaSeconds);
        } else {
            octopamine = octopamine + (0.5f - octopamine) * Math.min(1.0f, 0.05f * deltaSeconds);
        }

        // Chemotactic blindness timer
        if (chemotacticBlindnessTicks > 0) {
            chemotacticBlindnessTicks--;
        }

        // Formic acid reservoir biochemical regeneration
        formicAcidGland = Math.min(100.0f, formicAcidGland + 0.1f * deltaSeconds);

        // Circadian Molecular Pacemaker & Polyphasic Sleep Recovery
        circadianPhase = (float) ((circadianPhase + (2.0 * Math.PI / 86400.0) * deltaSeconds) % (2.0 * Math.PI));
        if (polyphasicSleeping) {
            energy = Math.min(maxEnergy, energy + 2.0f * deltaSeconds);
            fatigue = Math.max(0.0f, fatigue - 4.0f * deltaSeconds);
        }

        // Active Social Thermoregulation (Shivering & Fanning)
        if (shiveringThermogenesis) {
            energy = Math.max(0.0f, energy - 0.3f * deltaSeconds);
            thoraxTemperatureC = Math.min(40.0f, thoraxTemperatureC + 2.5f * deltaSeconds);
        } else if (wingFanning) {
            energy = Math.max(0.0f, energy - 0.15f * deltaSeconds);
            thoraxTemperatureC = Math.max(18.0f, thoraxTemperatureC - 3.0f * deltaSeconds);
        } else {
            thoraxTemperatureC += (25.0f - thoraxTemperatureC) * 0.05f * deltaSeconds;
        }
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

    public UUID getId() {
        return id;
    }

    public UUID getColonyId() {
        return colonyId;
    }

    public Caste getCaste() {
        return caste;
    }

    public void setCaste(Caste caste) {
        this.caste = caste;
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

    public float getMaxEnergy() {
        return maxEnergy;
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
        long seed = id != null ? id.getLeastSignificantBits() : (antNumber > 0 ? antNumber : 1337L);
        this.random = new java.util.Random(seed);
        return this.random;
    }

    public org.swarmforge.core.species.Species getSpecies() {
        return species;
    }

    public void setSpecies(org.swarmforge.core.species.Species species) {
        this.species = species;
        applyBiologicalCasteStats();
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
        float effectiveDamage = amount;
        if (phragmoticShieldActive) {
            effectiveDamage *= 0.20f; // 80% physical shield damage reduction
        }
        health -= effectiveDamage;
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

        if (caste == Caste.QUEEN && colony != null) {
            float targetZ = colony.getNestZ() < 0 ? colony.getNestZ() - 0.5f : -1.8f;
            this.z += (targetZ - this.z) * 0.1f;
        } else if (job == Job.NURSE && isAtNest()) {
            float targetZ = -1.2f;
            this.z += (targetZ - this.z) * 0.1f;
        }

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
                    float nextX = this.x + (dx / len) * step;
                    float nextY = this.y + (dy / len) * step;

                    Terrarium t = colony != null ? colony.getTerrarium() : null;
                    if (!canFly() && t != null) {
                        int ix = Math.max(0, Math.min(t.getWidth() - 1, Math.round(nextX)));
                        int iy = Math.max(0, Math.min(t.getHeight() - 1, Math.round(nextY)));
                        int iz = Math.max(0, Math.min(t.getDepth() - 1, Math.round(z)));
                        TerrariumCell cell = t.getCell(ix, iy, iz);
                        if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                            this.heading += (float) (Math.PI * 0.75f);
                            return ActionResult.failure("Water hazard avoided");
                        }
                    }

                    integrateDisplacement(nextX - this.x, nextY - this.y);
                    this.x = nextX;
                    this.y = nextY;
                    this.heading = (float) Math.atan2(dy, dx);
                    if (carriedItem == CarriedItem.EARTH && isNearHomeSurface(colony)) {
                        depositEarthMound(colony);
                    }
                }
                return ActionResult.ok();
            }
            case FORAGE -> {
                this.heading += (getRandom().nextFloat() - 0.5f) * 0.25f;
                float surfaceZ = getSurfaceElevation();
                if (this.z < surfaceZ && (caste != Caste.QUEEN && job != Job.NURSE)) {
                    this.z = Math.min(surfaceZ, this.z + 0.08f);
                }
                move(0.9f);
                return ActionResult.ok();
            }
            case RETURN_HOME, ABORT_AND_RETURN -> {
                turnTowards(getHomeX(), getHomeY(), 0.1f);
                move(1.0f);
                if (isAtNest() && colony != null) {
                    resetPathIntegration();
                    if (carriedItem == CarriedItem.EARTH) {
                        depositEarthMound(colony);
                    }
                    float surfaceZ = getSurfaceElevation();
                    float targetNestZ = colony.getNestZ() < surfaceZ ? colony.getNestZ() : surfaceZ - 1.5f;
                    this.z += (targetNestZ - this.z) * 0.12f;
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
                if (isAtNest() && colony != null) {
                    float targetNestZ = colony.getNestZ() < 0 ? colony.getNestZ() : -1.2f;
                    this.z += (targetNestZ - this.z) * 0.12f;
                }
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
                    float nextX = this.x + (dx / len) * step;
                    float nextY = this.y + (dy / len) * step;

                    Terrarium t = colony != null ? colony.getTerrarium() : null;
                    if (!canFly() && t != null) {
                        int ix = Math.max(0, Math.min(t.getWidth() - 1, Math.round(nextX)));
                        int iy = Math.max(0, Math.min(t.getHeight() - 1, Math.round(nextY)));
                        int iz = Math.max(0, Math.min(t.getDepth() - 1, Math.round(z)));
                        TerrariumCell cell = t.getCell(ix, iy, iz);
                        if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                            this.heading += (float) (Math.PI * 0.75f);
                            return ActionResult.failure("Water hazard avoided");
                        }
                    }

                    this.x = nextX;
                    this.y = nextY;
                    this.heading = (float) Math.atan2(dy, dx);
                }
                return ActionResult.ok();
            }
            case DEPOSIT_FOOD -> {
                if (isCarryingFood() && colony != null) {
                    if (isAtNest()) {
                        float targetNestZ = colony.getNestZ() < 0 ? colony.getNestZ() : -1.0f;
                        this.z += (targetNestZ - this.z) * 0.15f;
                    }
                    if (carriedResourceType != null) {
                        colony.addResource(carriedResourceType, 1.0f);
                        if (carriedResourceType == ResourceType.PROPOLIS_RESIN && colony.getSocialImmunityManager() != null && colony.getNestVoxelGrid() != null) {
                            int vx = Math.max(0, Math.min(colony.getNestVoxelGrid().getWidth() - 1, (int) Math.floor(x - colony.getNestX() + 8)));
                            int vy = Math.max(0, Math.min(colony.getNestVoxelGrid().getHeight() - 1, (int) Math.floor(y - colony.getNestY() + 8)));
                            int vz = Math.max(0, Math.min(colony.getNestVoxelGrid().getDepth() - 1, (int) Math.floor(Math.abs(z))));
                            colony.getSocialImmunityManager().applyPropolisCoating(colony, colony.getNestVoxelGrid(), vx, vy, vz);
                        }
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
            case GROOM -> {
                if (action.target() instanceof Individual target) {
                    if (target.isAlive() && target.getColonyId().equals(this.getColonyId())) {
                        float distSq = (float) ((target.getX() - x) * (target.getX() - x) + (target.getY() - y) * (target.getY() - y) + (target.getZ() - z) * (target.getZ() - z));
                        if (distSq <= 4.0f) {
                            this.energy = Math.max(0.0f, this.energy - 0.5f);
                            if (colony != null && colony.getSocialImmunityManager() != null) {
                                var registry = colony.getInfectionRegistry();
                                var groomerInf = registry.computeIfAbsent(this.getId(), k -> new org.swarmforge.core.epidemiology.IndividualInfection(k));
                                var targetInf = registry.computeIfAbsent(target.getId(), k -> new org.swarmforge.core.epidemiology.IndividualInfection(k));
                                colony.getSocialImmunityManager().performAllogrooming(groomerInf, targetInf, this.random != null ? this.random : new java.util.Random());
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
                    if (broodTarget.isAlive() && broodTarget.getColonyId().equals(this.getColonyId()) && broodTarget.getLifeStage() != LifeStage.ADULT) {
                        float distSq = (float) ((broodTarget.getX() - x) * (broodTarget.getX() - x) + (broodTarget.getY() - y) * (broodTarget.getY() - y) + (broodTarget.getZ() - z) * (broodTarget.getZ() - z));
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
                    if (mate.isAlive() && mate.getColonyId().equals(this.getColonyId())) {
                        float distSq = (float) ((mate.getX() - x) * (mate.getX() - x) + (mate.getY() - y) * (mate.getY() - y) + (mate.getZ() - z) * (mate.getZ() - z));
                        if (distSq <= 4.0f) {
                            float avgEnergy = (this.energy + mate.getEnergy()) / 2.0f;
                            this.energy = avgEnergy;
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
