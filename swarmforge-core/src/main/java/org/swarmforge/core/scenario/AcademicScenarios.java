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
                "Évaluation des Stratégies d'Exploration : Vol de Lévy vs Marche Brownienne",
                "Étude comparative de l'efficacité de récolte de nourriture entre des fourmis guidées par Réseau de Neurones/RL (Vol de Lévy) et des fourmis guidées par FSM (Marche Brownienne) dans un milieu à ressources dispersées."
        );
        scenario.setAcademicCategory("Etologie / Optimal Foraging Theory");
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
                "Déplacement des sources de nourriture pour tester l'adaptativité",
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
                "Polyéthisme et Spécialisation Comportementale Symbolique (BDI)",
                "Analyse de l'émergence de la division du travail (soin du couvain, creusement, fourrage, défense) pilotée par un moteur BDI (Croyances-Désirs-Intentions) adaptatif."
        );
        scenario.setAcademicCategory("Sociobiologie / Division du Travail");
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
                "Morphogenèse du Nid et Thermorégulation Bioclimatique",
                "Étude de l'architecture émergente des tunnels et galeries sous-terraines et son impact sur le gradient thermique et l'hygrométrie du nid."
        );
        scenario.setAcademicCategory("Biophysique & Architexture Animale");
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
                "Compétition Interspécifique et Dynamique Territoriale",
                "Modélisation du conflit territorial et du monopole des ressources entre Lasius niger (espèce indigène) et Linepithema humile (fourmi d'Argentine invasive)."
        );
        scenario.setAcademicCategory("Écologie des Populations / Invasions Biologiques");
        scenario.setMasterSeed(seed);
        scenario.setWidth(400);
        scenario.setHeight(400);

        Map<String, ArchitectureType> lasiusEngine = new HashMap<>();
        lasiusEngine.put("WORKER", ArchitectureType.BEHAVIOR_TREE);
        lasiusEngine.put("SOLDIER", ArchitectureType.FUZZY_LOGIC);

        Map<String, ArchitectureType> argentineEngine = new HashMap<>();
        argentineEngine.put("WORKER", ArchitectureType.NEURAL_NETWORK);
        argentineEngine.put("SOLDIER", ArchitectureType.NEURAL_NETWORK);

        scenario.addColony(new Scenario.ColonySetup("Lasius niger (Monogyne Native)", "COLONY_LASIUS", 1, 120, 15, 100, lasiusEngine));
        scenario.addColony(new Scenario.ColonySetup("Linepithema humile (Polygyne Invasive)", "COLONY_ARGENTINE", 3, 250, 30, 150, argentineEngine));

        scenario.addTargetMetric("TERRITORIAL_DOMINANCE_RATIO");
        scenario.addTargetMetric("MORTALITY_CONTEST_RATE");
        scenario.addTargetMetric("RESOURCE_MONOPOLIZATION_SPEED");
        scenario.addTargetMetric("SWARM_EXPANSION_VECTOR");

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
                createInterspecificCompetitionScenario(seed)
        );
    }
}
