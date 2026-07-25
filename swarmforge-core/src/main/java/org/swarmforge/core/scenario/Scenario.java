/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete Scenario configuration model for SwarmForge simulations.
 * Encapsulates master random seed, terrain, climate, demographic setups,
 * behavior engine assignments per caste, event schedules, and academic evaluation metrics.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Scenario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private String academicCategory;
    private long masterSeed = 42L;

    // Environment & World
    private int width = 256;
    private int height = 128;
    private int depth = 64;
    private String biomeName = "TEMPERATE_FOREST";
    private float soilDensity = 0.6f;

    // Climate & Season
    private float initialTemperature = 22.0f; // Celsius
    private float initialHumidity = 0.65f;
    private boolean dayNightCycleEnabled = true;

    // Demographics & Species Configuration
    private final List<ColonySetup> colonies = new ArrayList<>();

    // Resource Spawning Setup
    private int foodPatchesCount = 10;
    private int aphidColoniesCount = 2;
    private int seedPlantsCount = 15;
    private int preySpawnersCount = 3;

    // Events & Scheduling
    private final List<ScenarioEvent> scheduledEvents = new ArrayList<>();

    // Academic & Scientific Metrics
    private final List<String> targetMetrics = new ArrayList<>();

    // Termination Criteria
    private long maxSimulationTicks = 100_000L;
    private int minPopulationStopThreshold = 0;

    public Scenario() {}

    public Scenario(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    /**
     * Colony initialization record for scenario presets.
     */
    public record ColonySetup(
            String speciesName,
            String colonyId,
            int queenCount,
            int workerCount,
            int soldierCount,
            int initialFoodStore,
            Map<String, ArchitectureType> casteEngineMap
    ) implements Serializable {
        public ColonySetup(String speciesName, String colonyId, int queenCount, int workerCount) {
            this(speciesName, colonyId, queenCount, workerCount, 0, 100, createDefaultEngineMap());
        }

        private static Map<String, ArchitectureType> createDefaultEngineMap() {
            Map<String, ArchitectureType> map = new HashMap<>();
            map.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
            map.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
            map.put("QUEEN", ArchitectureType.BDI);
            return map;
        }
    }

    /**
     * Timed Event in Scenario.
     */
    public record ScenarioEvent(
            long triggerTick,
            String eventType,
            String description,
            Map<String, Object> parameters
    ) implements Serializable {}

    // Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAcademicCategory() { return academicCategory; }
    public void setAcademicCategory(String academicCategory) { this.academicCategory = academicCategory; }

    public long getMasterSeed() { return masterSeed; }
    public void setMasterSeed(long masterSeed) { this.masterSeed = masterSeed; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public String getBiomeName() { return biomeName; }
    public void setBiomeName(String biomeName) { this.biomeName = biomeName; }

    public float getSoilDensity() { return soilDensity; }
    public void setSoilDensity(float soilDensity) { this.soilDensity = soilDensity; }

    public float getInitialTemperature() { return initialTemperature; }
    public void setInitialTemperature(float initialTemperature) { this.initialTemperature = initialTemperature; }

    public float getInitialHumidity() { return initialHumidity; }
    public void setInitialHumidity(float initialHumidity) { this.initialHumidity = initialHumidity; }

    public boolean isDayNightCycleEnabled() { return dayNightCycleEnabled; }
    public void setDayNightCycleEnabled(boolean dayNightCycleEnabled) { this.dayNightCycleEnabled = dayNightCycleEnabled; }

    public List<ColonySetup> getColonies() { return colonies; }
    public void addColony(ColonySetup setup) { this.colonies.add(setup); }

    public int getFoodPatchesCount() { return foodPatchesCount; }
    public void setFoodPatchesCount(int foodPatchesCount) { this.foodPatchesCount = foodPatchesCount; }

    public int getAphidColoniesCount() { return aphidColoniesCount; }
    public void setAphidColoniesCount(int aphidColoniesCount) { this.aphidColoniesCount = aphidColoniesCount; }

    public int getSeedPlantsCount() { return seedPlantsCount; }
    public void setSeedPlantsCount(int seedPlantsCount) { this.seedPlantsCount = seedPlantsCount; }

    public int getPreySpawnersCount() { return preySpawnersCount; }
    public void setPreySpawnersCount(int preySpawnersCount) { this.preySpawnersCount = preySpawnersCount; }

    public List<ScenarioEvent> getScheduledEvents() { return scheduledEvents; }
    public void addEvent(ScenarioEvent event) { this.scheduledEvents.add(event); }

    public List<String> getTargetMetrics() { return targetMetrics; }
    public void addTargetMetric(String metric) { this.targetMetrics.add(metric); }

    public long getMaxSimulationTicks() { return maxSimulationTicks; }
    public void setMaxSimulationTicks(long maxSimulationTicks) { this.maxSimulationTicks = maxSimulationTicks; }

    public int getMinPopulationStopThreshold() { return minPopulationStopThreshold; }
    public void setMinPopulationStopThreshold(int threshold) { this.minPopulationStopThreshold = threshold; }
}
