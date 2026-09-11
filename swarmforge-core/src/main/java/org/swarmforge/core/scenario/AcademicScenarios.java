/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository of predefined Academic & Research Scenarios for SwarmForge.
 * Enables repeatable scientific observations and AI behavioral benchmarking.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AcademicScenarios {

    /**
     * Scenario 1: Optimal Foraging Theory (Lévy Flights vs Brownian Walk).
     */
    public static Scenario createLevyVsBrownianScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_01_LEVY_BROWNIAN",
                "Exploration Strategy Evaluation: Lévy Flights vs Brownian Walk",
                "Comparative study of foraging harvesting efficiency between ants guided by Neural Networks/RL (Lévy Flight) and ants guided by FSM (Brownian Walk) in a dispersed resource environment."
        );
        scenario.setAcademicCategory("Ethology / Optimal Foraging Theory");
        scenario.setMasterSeed(seed);
        scenario.setWidth(300);
        scenario.setHeight(300);
        scenario.setDepth(32);
        scenario.setBiomeName("TEMPERATE_FOREST");
        scenario.setFoodPatchesCount(25); // Dispersed sparse patches

        Map<String, ArchitectureType> neuralEngine = new HashMap<>();
        neuralEngine.put("WORKER", ArchitectureType.NEURAL_NETWORK);
        neuralEngine.put("QUEEN", ArchitectureType.BDI);

        Map<String, ArchitectureType> fsmEngine = new HashMap<>();
        fsmEngine.put("WORKER", ArchitectureType.FINITE_STATE_MACHINE);
        fsmEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Lévy RL)", "COLONY_LEVY", 1, 100, 0, 500, neuralEngine));
        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Brownian FSM)", "COLONY_BROWNIAN", 1, 100, 0, 500, fsmEngine));

        scenario.addTargetMetric("FORAGING_EFFICIENCY_INDEX");
        scenario.addTargetMetric("MEAN_SEARCH_TIME_PER_ITEM");
        scenario.addTargetMetric("ENERGY_ROI_PER_TRIP");
        scenario.addTargetMetric("TRAIL_BIFURCATION_COUNT");

        // Scheduled Event: Sudden Resource Shift at Tick 20,000
        scenario.addEvent(new Scenario.ScenarioEvent(
                20_000L,
                "RESOURCE_SHIFT",
                "Shift food sources to test adaptability",
                Map.of("newPatchCount", 15, "radius", 100)
        ));

        return scenario;
    }

    /**
     * Scenario 2: Polyethism & Division of Labor (Symbolic BDI).
     */
    public static Scenario createPolyethismScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_02_POLYETHISM_BDI",
                "Polyethism and Symbolic Behavioral Specialization (BDI)",
                "Analysis of the emergence of division of labor (brood care, excavation, foraging, defense) driven by an adaptive Belief-Desire-Intention (BDI) engine."
        );
        scenario.setAcademicCategory("Sociobiology / Division of Labor");
        scenario.setMasterSeed(seed);
        scenario.setWidth(200);
        scenario.setHeight(200);
        scenario.setDepth(48);
        scenario.setBiomeName("MEDITERRANEAN");

        Map<String, ArchitectureType> bdiEngine = new HashMap<>();
        bdiEngine.put("WORKER", ArchitectureType.BDI);
        bdiEngine.put("SOLDIER", ArchitectureType.BDI);
        bdiEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Messor barbarus (BDI Colony)", "COLONY_BDI", 1, 150, 20, 500, bdiEngine));
 
         scenario.addTargetMetric("TASK_ALLOCATION_ENTROPY");
         scenario.addTargetMetric("SPECIALIZATION_INDEX");
         scenario.addTargetMetric("BROOD_SURVIVAL_RATE");
         scenario.addTargetMetric("QUEEN_HEALTH_INDEX");
 
         return scenario;
     }

    /**
     * Scenario 3: Nest Morphogenesis & Subterranean Microclimate.
     */
    public static Scenario createNestMorphogenesisScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_03_NEST_MORPHOGENESIS",
                "Nest Morphogenesis and Bioclimatic Thermoregulation",
                "Study of emergent underground tunnel and gallery architecture and its impact on the thermal gradient and nest hygrometry."
        );
        scenario.setAcademicCategory("Biophysics & Animal Architecture");
        scenario.setMasterSeed(seed);
        scenario.setDepth(96); // Deep soil
        scenario.setSoilDensity(0.75f); // Clay/loam soil
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> btEngine = new HashMap<>();
        btEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        btEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Excavators)", "COLONY_DIGGERS", 1, 200, 0, 500, btEngine));

        scenario.addTargetMetric("TUNNEL_FRACTAL_DIMENSION");
        scenario.addTargetMetric("CHAMBER_DEPTH_DISTRIBUTION");
        scenario.addTargetMetric("THERMAL_STABILITY_DELTA");
        scenario.addTargetMetric("EXCAVATION_RATE_PER_TICK");

        return scenario;
    }

    /**
     * Scenario 4: Interspecific Territorial Competition.
     */
    public static Scenario createInterspecificCompetitionScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_04_INTERSPECIFIC_COMPETITION",
                "Interspecific Competition and Territorial Dynamics",
                "Modeling territorial conflict and resource monopolization between Lasius niger (native species) and Linepithema humile (invasive Argentine ant)."
        );
        scenario.setAcademicCategory("Population Ecology / Biological Invasions");
        scenario.setMasterSeed(seed);
        scenario.setWidth(400);
        scenario.setHeight(400);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> lasiusEngine = new HashMap<>();
        lasiusEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        lasiusEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);

        Map<String, ArchitectureType> argentineEngine = new HashMap<>();
        argentineEngine.put("WORKER", ArchitectureType.NEURAL_NETWORK);
        argentineEngine.put("SOLDIER", ArchitectureType.NEURAL_NETWORK);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Native Monogyne)", "COLONY_LASIUS", 1, 120, 15, 500, lasiusEngine));
        scenario.addColony(new Scenario.ColonySetup("Linepithema humile (Invasive Polygyne)", "COLONY_ARGENTINE", 3, 250, 30, 500, argentineEngine));

        scenario.addTargetMetric("TERRITORIAL_DOMINANCE_RATIO");
        scenario.addTargetMetric("MORTALITY_CONTEST_RATE");
        scenario.addTargetMetric("RESOURCE_MONOPOLIZATION_SPEED");
        scenario.addTargetMetric("SWARM_EXPANSION_VECTOR");

        return scenario;
    }

    /**
     * Scenario 5: Trophallaxis & Nutrient Dynamics.
     */
    public static Scenario createTrophallaxisScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_05_TROPHALLAXIS",
                "Food Flow & Colonial Trophallaxis",
                "Analysis of trophallactic nutrient distribution and the impact of undernutrition on brood and queen."
        );
        scenario.setAcademicCategory("Sociobiology / Colonial Metabolism");
        scenario.setMasterSeed(seed);
        scenario.setFoodPatchesCount(10);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        engine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Camponotus ligniperda (Trophallaxis)", "COLONY_TROPH", 1, 100, 0, 500, engine));
        scenario.addTargetMetric("NUTRIENT_SPREAD_VELOCITY");
        scenario.addTargetMetric("REPRODUCTIVE_CASTING_FEEDING_INDEX");
        return scenario;
    }

    /**
     * Scenario 6: Epidemiology & Self-Quarantine.
     */
    public static Scenario createEpidemiologyScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_06_EPIDEMIOLOGY_QUARANTINE",
                "Epidemiology & Bio-Behavioral Self-Quarantine",
                "Spread of an entomopathogenic fungal spore (Cordyceps) and observation of quarantine and necrophoric behaviors."
        );
        scenario.setAcademicCategory("Ethology / Social Immunity");
        scenario.setMasterSeed(seed);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.FUZZY_LOGIC);
        engine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        engine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Quarantine Group)", "COLONY_EPIDEM", 1, 150, 20, 500, engine));
        scenario.addTargetMetric("CONTAGION_RO");
        scenario.addTargetMetric("NECROPHORIC_REMOVAL_EFFICIENCY");
        return scenario;
    }

    /**
     * Scenario 7: Attine Fungi Agriculture.
     */
    public static Scenario createAttineFungiScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_07_ATTINE_FUNGI",
                "Fungal-Colonial Symbiosis of Attines",
                "Symbiotic culture of Leucoagaricus by Atta sexdens via foliage input and selective weeding."
        );
        scenario.setAcademicCategory("Symbiosis & Animal Agriculture");
        scenario.setMasterSeed(seed);
        scenario.setBiomeName("TROPICAL_RAINFOREST");

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        engine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Atta sexdens (Fungi Cultivators)", "COLONY_ATTA", 1, 200, 30, 500, engine));
        scenario.addTargetMetric("FUNGI_BIOMASS_YIELD");
        scenario.addTargetMetric("LEAF_HARVEST_RATE");
        return scenario;
    }

    /**
     * Scenario 8: Stigmergic Pheromones.
     */
    public static Scenario createStigmergyScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_08_STIGMERGIC_PHEROMONES",
                "Pheromonal Stigmergy & Maze Resolution",
                "Route optimization through intense stigmergic trail pheromone deposition in a labyrinthine environment."
        );
        scenario.setAcademicCategory("Collective Intelligence & Stigmergy");
        scenario.setMasterSeed(seed);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.NEURAL_NETWORK);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Stigmergy Team)", "COLONY_STIGMERGY", 1, 180, 0, 500, engine));
        scenario.addTargetMetric("PATH_SHORTEST_RATIO");
        scenario.addTargetMetric("PHEROMONE_CONCENTRIC_PEAK");
        return scenario;
    }

    /**
     * Scenario 9: Dulosis & Slave-Making Raids (Polyergus rufescens vs Formica fusca).
     */
    public static Scenario createDulosisRaidScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_09_DULOSIS_RAID",
                "Dulosis & Slave-Making Raids (Polyergus rufescens vs Formica fusca)",
                "Simulation of obligate dulotic raids: Polyergus rufescens raiding column launching a targeted assault on a Formica fusca nest to capture pupae/cocoons and repatriate them as slave labor."
        );
        scenario.setAcademicCategory("Sociobiology / Dulosis & Parasitism");
        scenario.setMasterSeed(seed);
        scenario.setWidth(350);
        scenario.setHeight(350);
        scenario.setDepth(48);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> raidingEngine = new HashMap<>();
        raidingEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        raidingEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        raidingEngine.put("QUEEN", ArchitectureType.BDI);

        Map<String, ArchitectureType> hostEngine = new HashMap<>();
        hostEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        hostEngine.put("QUEEN", ArchitectureType.BDI);

        // Polyergus rufescens (Amazon/Slave-maker ants - Raiders)
        scenario.addColony(new Scenario.ColonySetup("Polyergus rufescens (Amazon Raiding Party)", "COLONY_POLYERGUS", 1, 80, 40, 500, raidingEngine));
        // Formica fusca (Host/Target Species - Slave Target)
        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Target Host Colony)", "COLONY_FORMICA", 1, 150, 0, 500, hostEngine));

        scenario.addTargetMetric("PUPAE_CAPTURED_COUNT");
        scenario.addTargetMetric("RAID_COLUMN_COHESION");
        scenario.addTargetMetric("REPATRIATION_SUCCESS_RATE");
        scenario.addTargetMetric("HOST_DEFENSE_CASUALTIES");

        // Scheduled Event: Launch Slave-making Raid at Tick 5,000
        scenario.addEvent(new Scenario.ScenarioEvent(
                5_000L,
                "DULOTIC_RAID_TRIGGER",
                "Polyergus scout detects host nest and recruits raiding column",
                Map.of("targetColonyId", "COLONY_FORMICA", "raidForceSize", 60)
        ));

        return scenario;
    }

    /**
     * Scenario 10: Savanna Bioclimatic Adaptation & Acacia-Ant Mutualism (Serengeti Savanna).
     */
    public static Scenario createSavannaCoevolutionScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_10_SAVANNA_COEVOLUTION",
                "Savanna Bioclimatic Adaptation & Acacia-Ant Mutualism (Serengeti)",
                "Ecological dynamics under alternating wet/dry tropical seasons: acacia tree nesting, herbivore deterrence, and subterranean termite competition in the Serengeti savanna."
        );
        scenario.setAcademicCategory("Tropical Ecology / Mutualism & Drought Cycles");
        scenario.setMasterSeed(seed);
        scenario.setWidth(400);
        scenario.setHeight(400);
        scenario.setDepth(48);
        scenario.setBiomeName("SAVANNA");

        Map<String, ArchitectureType> antEngine = new HashMap<>();
        antEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        antEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        antEngine.put("QUEEN", ArchitectureType.BDI);

        Map<String, ArchitectureType> termiteEngine = new HashMap<>();
        termiteEngine.put("WORKER", ArchitectureType.FINITE_STATE_MACHINE);
        termiteEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        termiteEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Camponotus ligniperda (Savanna Defenders)", "COLONY_SAVANNA_ANTS", 1, 150, 30, 500, antEngine));
        scenario.addColony(new Scenario.ColonySetup("Reticulitermes flavipes (Savanna Termites)", "COLONY_SAVANNA_TERMITES", 1, 250, 40, 500, termiteEngine));

        scenario.addTargetMetric("HERBIVORE_DEFENSE_INDEX");
        scenario.addTargetMetric("DROUGHT_RESILIENCE_RATIO");
        scenario.addTargetMetric("INTERSPECIFIC_COMPETITION_SCORE");
        return scenario;
    }

    /**
     * Scenario 11: High-Altitude Cryo-Tolerance & Alpine Thermoregulation.
     */
    public static Scenario createAlpineThermoregulationScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_11_ALPINE_THERMOREGULATION",
                "High-Altitude Cryo-Tolerance & Alpine Thermoregulation (Mont Blanc / Valais)",
                "Study of colonial thermal budgeting, sub-zero freeze avoidance, and metabolic suppression during extreme alpine freeze-thaw diurnal cycles."
        );
        scenario.setAcademicCategory("Eco-Physiology / Cryo-Biology");
        scenario.setMasterSeed(seed);
        scenario.setWidth(250);
        scenario.setHeight(250);
        scenario.setDepth(64);
        scenario.setBiomeName("ALPINE_TUNDRA");

        Map<String, ArchitectureType> alpineEngine = new HashMap<>();
        alpineEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        alpineEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Alpine Cryo-Colony)", "COLONY_ALPINE", 1, 180, 0, 500, alpineEngine));
        scenario.addTargetMetric("THERMAL_GRADIENT_EFFICIENCY");
        scenario.addTargetMetric("METABOLIC_CONSERVATION_INDEX");
        scenario.addTargetMetric("FREEZE_SURVIVAL_PERCENT");
        return scenario;
    }

    /**
     * Scenario 12: Boreal Pine Needle Domes & Solar Heat Storage (Rovaniemi Taiga).
     */
    public static Scenario createBorealSolarDomesScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_12_BOREAL_SOLAR_DOMES",
                "Boreal Pine Needle Domes & Solar Heat Storage (Rovaniemi Taiga)",
                "Investigation of mound solar orientation, infrared thatch heat capacitance, and active collective sun-basking heat carriage by wood ants (Formica rufa)."
        );
        scenario.setAcademicCategory("Thermoregulation & Biophysics");
        scenario.setMasterSeed(seed);
        scenario.setWidth(300);
        scenario.setHeight(300);
        scenario.setDepth(48);
        scenario.setBiomeName("BOREAL_TAIGA");

        Map<String, ArchitectureType> taigaEngine = new HashMap<>();
        taigaEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        taigaEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        taigaEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Formica rufa (Taiga Mound Builders)", "COLONY_TAIGA_RUFA", 1, 200, 25, 500, taigaEngine));
        scenario.addTargetMetric("MOUND_CORE_TEMPERATURE_DELTA");
        scenario.addTargetMetric("SOLAR_CALORIE_HARVEST_RATE");
        scenario.addTargetMetric("NEEDLE_STRUCTURE_STABILITY");
        return scenario;
    }

    /**
     * Scenario 13: Semi-Arid Steppe Granivory & Desiccation Resistance (Astana Steppe).
     */
    public static Scenario createSteppeHarvestingScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_13_STEPPE_HARVESTING",
                "Semi-Arid Steppe Granivory & Desiccation Resistance (Astana)",
                "Analysis of long-distance seed harvesting corridors, subterranean granary dehydration prevention, and high wind foraging thresholds."
        );
        scenario.setAcademicCategory("Desert Ecology / Hydric Stress");
        scenario.setMasterSeed(seed);
        scenario.setWidth(350);
        scenario.setHeight(350);
        scenario.setDepth(60);
        scenario.setBiomeName("STEPPE");

        Map<String, ArchitectureType> steppeEngine = new HashMap<>();
        steppeEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        steppeEngine.put("SOLDIER", ArchitectureType.BDI);
        steppeEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Pogonomyrmex barbatus (Steppe Harvesters)", "COLONY_STEPPE", 1, 160, 20, 500, steppeEngine));
        scenario.addTargetMetric("SEED_STORAGE_HYGROMETRY_STABILITY");
        scenario.addTargetMetric("WIND_FORAGING_CUTOFF_EFFICIENCY");
        scenario.addTargetMetric("WATER_LOSS_PER_KM_FORAGED");
        return scenario;
    }

    /**
     * Scenario 14: Hydrodynamic Self-Assembled Rafting in Subtropical Wetlands (Everglades).
     */
    public static Scenario createWetlandFloodRaftingScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_14_WETLAND_FLOOD_RAFTING",
                "Hydrodynamic Self-Assembled Rafting in Subtropical Wetlands (Everglades)",
                "Emergent multi-agent structural raft construction by Solenopsis invicta under heavy rainfall and rising floodwaters to safeguard the queen and brood."
        );
        scenario.setAcademicCategory("Biomechanics & Self-Assembly");
        scenario.setMasterSeed(seed);
        scenario.setWidth(300);
        scenario.setHeight(300);
        scenario.setDepth(32);
        scenario.setBiomeName("WETLAND");

        Map<String, ArchitectureType> fireAntEngine = new HashMap<>();
        fireAntEngine.put("WORKER", ArchitectureType.NEURAL_NETWORK);
        fireAntEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        fireAntEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Solenopsis invicta (Wetland Raft Colony)", "COLONY_WETLAND_RAFT", 1, 300, 40, 500, fireAntEngine));
        scenario.addTargetMetric("RAFT_STRUCTURAL_COHESION");
        scenario.addTargetMetric("BROOD_IMMERSION_AVOIDANCE_RATE");
        scenario.addTargetMetric("FLOOD_DISPERSAL_SUCCESS");
        return scenario;
    }

    /**
     * Scenario 15: Arboreal Wasp Nest vs Wild Tree Hollow Beehive Predation & Defense.
     */
    public static Scenario createWaspVsWildBeehiveScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_15_WASP_WILD_BEEHIVE",
                "Arboreal Wasp Nest vs Wild Beehive (Tree Hollow)",
                "Ecological predation dynamics between an arboreal hanging paper wasp nest (Vespula germanica) and a wild honeybee colony (Apis mellifera) nested inside a hollow tree trunk with thermal balling defense."
        );
        scenario.setAcademicCategory("Aerial Ecology / Predation & Thermal Defense");
        scenario.setMasterSeed(seed);
        scenario.setWidth(350);
        scenario.setHeight(350);
        scenario.setDepth(32);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> waspEngine = new HashMap<>();
        waspEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        waspEngine.put("QUEEN", ArchitectureType.BDI);

        Map<String, ArchitectureType> beeEngine = new HashMap<>();
        beeEngine.put("WORKER", ArchitectureType.BDI);
        beeEngine.put("QUEEN", ArchitectureType.BDI);

        // Vespula germanica (Hanging Paper Nest in Canopy at Z ≈ +8.5m)
        scenario.addColony(new Scenario.ColonySetup("Vespula germanica (Canopy Paper Nest)", "COLONY_WASP_NEST", 1, 80, 20, 500, waspEngine));
        // Apis mellifera (Wild Hollow Trunk at Z ≈ +1.2m)
        scenario.addColony(new Scenario.ColonySetup("Apis mellifera (Wild Hollow Trunk)", "COLONY_WILD_BEEHIVE", 1, 250, 0, 500, beeEngine));

        scenario.addTargetMetric("AERIAL_INTERCEPTION_RATE");
        scenario.addTargetMetric("THERMAL_BALL_DEFENSE_SUCCESS");
        scenario.addTargetMetric("HONEY_DEPLETION_PREDATION_LOSS");
        scenario.addTargetMetric("WORKER_DEFENSIVE_AUTOTOMY_COUNT");

        // Scheduled Event: Wasp Foraging Raid at Tick 6,000
        scenario.addEvent(new Scenario.ScenarioEvent(
                6_000L,
                "WASP_RAID_TRIGGER",
                "Wasp scouting party detects honey stores and triggers aerial raid",
                Map.of("targetColonyId", "COLONY_WILD_BEEHIVE", "raidForceSize", 30)
        ));

        return scenario;
    }

    /**
     * Scenario 16: Managed Apicultural Apiary & Regional Landscape Foraging.
     */
    public static Scenario createApiculturalApiaryScenario(long seed) {
        Scenario scenario = new Scenario(
                "ACAD_16_APICULTURAL_APIARY",
                "Managed Apicultural Apiary & Regional Landscape Foraging (Dadant 10-Frame)",
                "Complete simulation of a managed modern apiary: Dadant 10-frame honey supers, comb honey maturation, active brood thermoregulation (34.5°C), seasonal swarming (SWARM_DIVISION), and micro-macro long-distance floral foraging across regional orchards and meadows."
        );
        scenario.setAcademicCategory("Apiculture & Ecosystem Services / Macro-Foraging");
        scenario.setMasterSeed(seed);
        scenario.setWidth(400);
        scenario.setHeight(400);
        scenario.setDepth(24);
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> beeEngine = new HashMap<>();
        beeEngine.put("WORKER", ArchitectureType.BDI);
        beeEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        beeEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Apis mellifera (Dadant Apiary Hive)", "COLONY_DADANT_APIARY", 1, 400, 0, 800, beeEngine));

        scenario.addTargetMetric("NECTAR_TO_HONEY_MATURATION_KG");
        scenario.addTargetMetric("BROOD_CORE_TEMPERATURE_DELTA");
        scenario.addTargetMetric("MACRO_FORAGING_FLIGHT_HOURS");
        scenario.addTargetMetric("SWARMING_PREPARATION_INDEX");
        scenario.addTargetMetric("UV_POLARIZATION_NAVIGATION_PRECISION");

        // Scheduled Event: Spring Floral Bloom Surge at Tick 10,000
        scenario.addEvent(new Scenario.ScenarioEvent(
                10_000L,
                "HONEY_FLOW_SURGE",
                "Massive floral bloom trigger across regional macro-patches",
                Map.of("regionalNectarMultiplier", 2.5f, "durationTicks", 50000)
        ));

        return scenario;
    }

    /**
     * List all available academic scenarios.
     */
    public static List<Scenario> getAllAcademicScenarios(long seed) {
        return List.of(
                createLevyVsBrownianScenario(seed),
                createPolyethismScenario(seed),
                createNestMorphogenesisScenario(seed),
                createInterspecificCompetitionScenario(seed),
                createTrophallaxisScenario(seed),
                createEpidemiologyScenario(seed),
                createAttineFungiScenario(seed),
                createStigmergyScenario(seed),
                createDulosisRaidScenario(seed),
                createSavannaCoevolutionScenario(seed),
                createAlpineThermoregulationScenario(seed),
                createBorealSolarDomesScenario(seed),
                createSteppeHarvestingScenario(seed),
                createWetlandFloodRaftingScenario(seed),
                createWaspVsWildBeehiveScenario(seed),
                createApiculturalApiaryScenario(seed)
        );
    }
}
