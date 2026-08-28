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
        scenario.setBiomeName("ARID_SAVANNA");
        scenario.setFoodPatchesCount(25); // Dispersed sparse patches

        Map<String, ArchitectureType> neuralEngine = new HashMap<>();
        neuralEngine.put("WORKER", ArchitectureType.NEURAL_NETWORK);
        neuralEngine.put("QUEEN", ArchitectureType.BDI);

        Map<String, ArchitectureType> fsmEngine = new HashMap<>();
        fsmEngine.put("WORKER", ArchitectureType.FINITE_STATE_MACHINE);
        fsmEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Lévy RL)", "COLONY_LEVY", 1, 100, 0, 50, neuralEngine));
        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Brownian FSM)", "COLONY_BROWNIAN", 1, 100, 0, 50, fsmEngine));

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
        scenario.setBiomeName("TEMPERATE_FOREST");

        Map<String, ArchitectureType> bdiEngine = new HashMap<>();
        bdiEngine.put("WORKER", ArchitectureType.BDI);
        bdiEngine.put("SOLDIER", ArchitectureType.BDI);
        bdiEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Messor barbarus (BDI Colony)", "COLONY_BDI", 1, 150, 20, 200, bdiEngine));

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

        Map<String, ArchitectureType> btEngine = new HashMap<>();
        btEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        btEngine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Excavators)", "COLONY_DIGGERS", 1, 200, 0, 100, btEngine));

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

        Map<String, ArchitectureType> lasiusEngine = new HashMap<>();
        lasiusEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        lasiusEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);

        Map<String, ArchitectureType> argentineEngine = new HashMap<>();
        argentineEngine.put("WORKER", ArchitectureType.NEURAL_NETWORK);
        argentineEngine.put("SOLDIER", ArchitectureType.NEURAL_NETWORK);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Native Monogyne)", "COLONY_LASIUS", 1, 120, 15, 100, lasiusEngine));
        scenario.addColony(new Scenario.ColonySetup("Linepithema humile (Invasive Polygyne)", "COLONY_ARGENTINE", 3, 250, 30, 150, argentineEngine));

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

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        engine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Camponotus ligniperda (Trophallaxis)", "COLONY_TROPH", 1, 100, 0, 100, engine));
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

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.FUZZY_LOGIC);
        engine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);
        engine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Formica fusca (Quarantine Group)", "COLONY_EPIDEM", 1, 150, 20, 100, engine));
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

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        engine.put("QUEEN", ArchitectureType.BDI);

        scenario.addColony(new Scenario.ColonySetup("Atta sexdens (Fungi Cultivators)", "COLONY_ATTA", 1, 200, 30, 250, engine));
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

        Map<String, ArchitectureType> engine = new HashMap<>();
        engine.put("WORKER", ArchitectureType.NEURAL_NETWORK);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Stigmergy Team)", "COLONY_STIGMERGY", 1, 180, 0, 50, engine));
        scenario.addTargetMetric("PATH_SHORTEST_RATIO");
        scenario.addTargetMetric("PHEROMONE_CONCENTRIC_PEAK");
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
                createStigmergyScenario(seed)
        );
    }
}
