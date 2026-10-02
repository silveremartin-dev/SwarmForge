/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Loader for Academic & Research Scenarios in SwarmForge.
 * All scenarios are strictly persisted as JSON files in {@code scenarios/} and bundled in {@code /presets/scenarios/}.
 * No hardcoded scenario data is stored in Java code.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AcademicScenarios {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String[] ACADEMIC_SCENARIO_FILES = {
            "acad_01_levy_brownian.json",
            "acad_02_polyethism_bdi.json",
            "acad_03_nest_morphogenesis.json",
            "acad_04_interspecific_competition.json",
            "acad_05_trophallaxis.json",
            "acad_06_epidemiology_quarantine.json",
            "acad_07_attine_fungi.json",
            "acad_08_stigmergic_pheromones.json",
            "acad_09_dulosis_raid.json",
            "acad_10_savanna_coevolution.json",
            "acad_11_alpine_thermoregulation.json",
            "acad_12_boreal_solar_domes.json",
            "acad_13_steppe_harvesting.json",
            "acad_14_wetland_flood_rafting.json",
            "acad_15_wasp_wild_beehive.json",
            "acad_16_apicultural_apiary.json"
    };

    private static final String[] MULTIPLAYER_SCENARIO_FILES = {
            "mp_01_battle_arena_1v1.json",
            "mp_02_coop_tribute_trade.json",
            "mp_03_megaterrarium_4node_alliance.json"
    };

    public static Scenario loadScenario(String filename, long seed) {
        Scenario scenario = null;

        // 1. Try relative filesystem path (data/scenarios/academic, data/scenarios/multiplayer, etc.)
        File[] candidatePaths = new File[]{
                new File("data/scenarios/academic", filename),
                new File("data/scenarios/multiplayer", filename),
                new File("data/scenarios/server", filename),
                new File("data/scenarios", filename),
                new File("../data/scenarios/academic", filename),
                new File("../data/scenarios/multiplayer", filename),
                new File("../data/scenarios/server", filename),
                new File("../data/scenarios", filename),
                new File("scenarios/academic", filename),
                new File("scenarios/multiplayer", filename),
                new File("scenarios/server", filename),
                new File("scenarios", filename),
                new File("../scenarios/academic", filename),
                new File("../scenarios/multiplayer", filename),
                new File("../scenarios/server", filename),
                new File("../scenarios", filename)
        };

        for (File path : candidatePaths) {
            if (path.exists()) {
                try {
                    scenario = MAPPER.readValue(path, Scenario.class);
                    break;
                } catch (Exception ignored) {}
            }
        }

        // 2. Fallback to classpath resource
        if (scenario == null) {
            try (InputStream in = AcademicScenarios.class.getResourceAsStream("/presets/scenarios/" + filename)) {
                if (in != null) {
                    scenario = MAPPER.readValue(in, Scenario.class);
                }
            } catch (Exception ignored) {}
        }

        if (scenario != null) {
            scenario.setMasterSeed(seed);
            scenario.setBuiltIn(true);
            scenario.calculateChecksum();
        }
        return scenario;
    }

    public static Scenario createLevyVsBrownianScenario(long seed) {
        return loadScenario("acad_01_levy_brownian.json", seed);
    }

    public static Scenario createPolyethismScenario(long seed) {
        return loadScenario("acad_02_polyethism_bdi.json", seed);
    }

    public static Scenario createNestMorphogenesisScenario(long seed) {
        return loadScenario("acad_03_nest_morphogenesis.json", seed);
    }

    public static Scenario createInterspecificCompetitionScenario(long seed) {
        return loadScenario("acad_04_interspecific_competition.json", seed);
    }

    public static Scenario createTrophallaxisScenario(long seed) {
        return loadScenario("acad_05_trophallaxis.json", seed);
    }

    public static Scenario createEpidemiologyScenario(long seed) {
        return loadScenario("acad_06_epidemiology_quarantine.json", seed);
    }

    public static Scenario createAttineFungiScenario(long seed) {
        return loadScenario("acad_07_attine_fungi.json", seed);
    }

    public static Scenario createStigmergyScenario(long seed) {
        return loadScenario("acad_08_stigmergic_pheromones.json", seed);
    }

    public static Scenario createDulosisRaidScenario(long seed) {
        return loadScenario("acad_09_dulosis_raid.json", seed);
    }

    public static Scenario createSavannaCoevolutionScenario(long seed) {
        return loadScenario("acad_10_savanna_coevolution.json", seed);
    }

    public static Scenario createAlpineThermoregulationScenario(long seed) {
        return loadScenario("acad_11_alpine_thermoregulation.json", seed);
    }

    public static Scenario createBorealSolarDomesScenario(long seed) {
        return loadScenario("acad_12_boreal_solar_domes.json", seed);
    }

    public static Scenario createSteppeHarvestingScenario(long seed) {
        return loadScenario("acad_13_steppe_harvesting.json", seed);
    }

    public static Scenario createWetlandFloodRaftingScenario(long seed) {
        return loadScenario("acad_14_wetland_flood_rafting.json", seed);
    }

    public static Scenario createWaspVsWildBeehiveScenario(long seed) {
        return loadScenario("acad_15_wasp_wild_beehive.json", seed);
    }

    public static Scenario createApiculturalApiaryScenario(long seed) {
        return loadScenario("acad_16_apicultural_apiary.json", seed);
    }

    public static Scenario createMultiplayerBattleArena1v1(long seed) {
        return loadScenario("mp_01_battle_arena_1v1.json", seed);
    }

    public static Scenario createMultiplayerCoopTributeTrade(long seed) {
        return loadScenario("mp_02_coop_tribute_trade.json", seed);
    }

    public static Scenario createMultiplayerMegaterrariumSharded(long seed) {
        return loadScenario("mp_03_megaterrarium_4node_alliance.json", seed);
    }

    /**
     * List all available academic single-player and standard scenarios.
     */
    public static List<Scenario> getAllAcademicScenarios(long seed) {
        List<Scenario> list = new ArrayList<>();
        for (String file : ACADEMIC_SCENARIO_FILES) {
            Scenario s = loadScenario(file, seed);
            if (s != null) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * List all dedicated multiplayer & megaterrarium sharded scenarios.
     */
    public static List<Scenario> getAllMultiplayerScenarios(long seed) {
        List<Scenario> list = new ArrayList<>();
        for (String file : MULTIPLAYER_SCENARIO_FILES) {
            Scenario s = loadScenario(file, seed);
            if (s != null) {
                list.add(s);
            }
        }
        return list;
    }
}
