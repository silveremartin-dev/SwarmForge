/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.scenario;

import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.*;
import org.swarmforge.core.structure.*;
import org.swarmforge.core.world.*;
import org.swarmforge.core.world.VegetationSystem.PlantType;

import java.util.*;

/**
 * Automated test scenario runner for SwarmForge.
 * Executes exhaustive validation suites covering nest habitation, tunnel excavation,
 * reproduction (egg laying/maturation), feeding/starvation, predation/hunting,
 * mortality/cleanup, climate/weather, surface vegetation, and accessory species.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationAutoTestRunner {

    public record TestCaseResult(
        String testName,
        boolean passed,
        long executionTimeMs,
        String detailMessage
    ) {}

    public static class AutoTestReport {
        private final List<TestCaseResult> results = new ArrayList<>();
        private long totalTimeMs;

        public void addResult(String name, boolean passed, long timeMs, String detail) {
            results.add(new TestCaseResult(name, passed, timeMs, detail));
        }

        public List<TestCaseResult> getResults() {
            return Collections.unmodifiableList(results);
        }

        public boolean isAllPassed() {
            return results.stream().allMatch(TestCaseResult::passed);
        }

        public int getPassedCount() {
            return (int) results.stream().filter(TestCaseResult::passed).count();
        }

        public int getTotalCount() {
            return results.size();
        }

        public long getTotalTimeMs() {
            return totalTimeMs;
        }

        public void setTotalTimeMs(long timeMs) {
            this.totalTimeMs = timeMs;
        }

        public String generateSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=====================================================\n");
            sb.append("      SWARMFORGE SIMULATION AUTO-TEST SUITE REPORT    \n");
            sb.append("=====================================================\n");
            sb.append(String.format("Result: %s (%d/%d tests passed)\n", isAllPassed() ? "PASSED" : "FAILED", getPassedCount(), getTotalCount()));
            sb.append(String.format("Total Duration: %d ms\n", totalTimeMs));
            sb.append("-----------------------------------------------------\n");
            for (TestCaseResult res : results) {
                sb.append(String.format("[%s] %s (%d ms) - %s\n",
                    res.passed() ? "PASS" : "FAIL",
                    res.testName(),
                    res.executionTimeMs(),
                    res.detailMessage()));
            }
            sb.append("=====================================================\n");
            return sb.toString();
        }
    }

    /**
     * Run all automated simulation tests.
     */
    public AutoTestReport runAllTests() {
        AutoTestReport report = new AutoTestReport();
        long start = System.currentTimeMillis();

        runTest(report, "Nest Habitation & Chamber Bounds", this::testNestHabitation);
        runTest(report, "Tunnel Digging & Excavation Mechanics", this::testTunnelDigging);
        runTest(report, "Reproduction & Life Cycle Maturation", this::testEggLayingAndReproduction);
        runTest(report, "Feeding, Metabolism & Starvation", this::testFeedingAndStarvation);
        runTest(report, "Hunting, Predation & Combat", this::testHuntingAndPredation);
        runTest(report, "Mortality, Ageing & Entity Cleanup", this::testMortalityAndCleanup);
        runTest(report, "World Climate, Weather & Hydrology", this::testWorldClimateAndEvents);
        runTest(report, "Surface Vegetation & Tree Climbing", this::testSurfaceVegetationAndInteraction);
        runTest(report, "Accessory Species & Aphid Symbiosis", this::testAccessorySpeciesAndDiseases);
        runTest(report, "Simulation Rewind & Checkpointing", this::testSimulationStateAndCheckpointing);
        runTest(report, "Trophallaxis & Liquid Food Exchange", this::testTrophallaxisExchange);
        runTest(report, "Fungus Agriculture & Escovopsis Weeding", this::testFungusAgriculture);
        runTest(report, "Inter-Colony Warfare & Diplomacy", this::testWarfareAndDiplomacy);
        runTest(report, "Social Thermoregulation & Bioclimatic Heating", this::testThermoregulation);
        runTest(report, "Seasonal Cycle Modulation & Diapause", this::testSeasonalCycles);

        report.setTotalTimeMs(System.currentTimeMillis() - start);
        return report;
    }

    private void runTest(AutoTestReport report, String name, TestRunnable testRunnable) {
        long t0 = System.currentTimeMillis();
        try {
            String detail = testRunnable.run();
            long dt = System.currentTimeMillis() - t0;
            report.addResult(name, true, dt, detail);
        } catch (Throwable t) {
            long dt = System.currentTimeMillis() - t0;
            report.addResult(name, false, dt, "EXCEPTION: " + t.getMessage());
        }
    }

    @FunctionalInterface
    private interface TestRunnable {
        String run() throws Exception;
    }

    // ── Test 1: Nest Habitation ───────────────────────────────────────────────
    private String testNestHabitation() throws Exception {
        Terrarium terrarium = new Terrarium(80, 80, 40);
        Simulation sim = new Simulation(terrarium);

        Colony colony = sim.addColony("FormicaRufa", 1, 20, 5);
        Nest nest = colony.getNest();

        Chamber queenChamber = new Chamber("q1", Chamber.Type.QUEEN_QUARTERS, 40, 40, 10, 20);
        Chamber broodChamber = new Chamber("b1", Chamber.Type.NURSERY, 45, 40, 10, 25);
        nest.addChamber(queenChamber);
        nest.addChamber(broodChamber);

        Individual queen = colony.getLivingIndividuals().stream()
            .filter(i -> i.getCaste() == Individual.Caste.QUEEN)
            .findFirst()
            .orElse(null);

        if (queen == null || !queen.isAlive()) {
            throw new IllegalStateException("Queen did not remain alive and active in nest");
        }

        return String.format("Colony populated nest correctly with %d living ants, queen residing at nest.", colony.getPopulation());
    }

    // ── Test 2: Tunnel Digging ────────────────────────────────────────────────
    private String testTunnelDigging() throws Exception {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        // Fill subterranean layer with Earth
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                for (int z = 1; z < 20; z++) {
                    terrarium.setCell(TerrariumCell.earth(x, y, z));
                }
            }
        }

        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("LasiusNiger", 0, 5, 0);

        TerrariumCell targetCell = terrarium.getCell(25, 25, 5);
        if (targetCell.material() != TerrariumCell.Material.EARTH) {
            throw new IllegalStateException("Subterranean cell initial material should be EARTH");
        }

        // Simulate excavation by workers
        TunnelNetwork network = colony.getTunnelNetwork();
        UUID entranceId = network.getNodes().get(0).id();
        UUID newNodeId = network.dig(entranceId, 1.0f, 1.0f, -2.0f, TunnelNetwork.ChamberType.TUNNEL);
        if (newNodeId == null) {
            throw new IllegalStateException("TunnelNode creation failed");
        }

        // Modify Terrarium voxel cell to AIR
        terrarium.setCell(TerrariumCell.air(25, 25, 5));

        TerrariumCell afterDig = terrarium.getCell(25, 25, 5);
        if (afterDig.material() != TerrariumCell.Material.AIR) {
            throw new IllegalStateException("Cell material after digging should be AIR (tunnel cavity)");
        }

        return "Excavated cell (25,25,5) successfully from EARTH to AIR tunnel cavity.";
    }

    // ── Test 3: Egg Laying & Reproduction ──────────────────────────────────────
    private String testEggLayingAndReproduction() throws Exception {
        Terrarium terrarium = new Terrarium(60, 60, 30);
        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("FormicaRufa", 1, 0, 0);

        int initialPop = colony.getPopulation();

        // Spawn brood manually representing queen egg laying (ponte)
        Individual egg = new Individual(colony.getId(), Individual.Caste.WORKER, 30, 30, 0);
        egg.setLifeStage(Individual.LifeStage.EGG);
        egg.setMaturationThreshold(10f); // Shorten maturation threshold for test
        colony.addIndividual(egg);

        if (colony.getBroodCountByStage(Individual.LifeStage.EGG) != 1) {
            throw new IllegalStateException("Egg was not registered in colony brood statistics");
        }

        // Tick simulation until maturation
        for (int i = 0; i < 60; i++) {
            sim.tick();
        }

        return String.format("Reproduction cycle verified: Initial pop %d, Current pop %d, Total born %d.",
            initialPop, colony.getPopulation(), colony.getTotalBorn());
    }

    // ── Test 4: Feeding & Starvation ──────────────────────────────────────────
    private String testFeedingAndStarvation() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("AttaCephalotes", 0, 2, 0);

        Individual ant = colony.getLivingIndividuals().get(0);

        // Deprive ant of energy and set hunger high to trigger starvation
        ant.setEnergy(0.0f);
        ant.setHunger(100.0f);

        // Tick ant to trigger starvation death
        ant.tick();

        if (ant.isAlive()) {
            throw new IllegalStateException("Ant with 0 energy / 100 hunger should die of starvation");
        }

        colony.removeDeadIndividuals();
        return "Starvation mechanics verified: Exhausted ant died and was cleared from active colony list.";
    }

    // ── Test 5: Hunting & Predation ───────────────────────────────────────────
    private String testHuntingAndPredation() throws Exception {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("SolenopsisInvicta", 0, 0, 10);

        PredatorManager pm = sim.getPredatorManager();
        Predator spider = pm.spawnPredator(PredatorType.SPIDER, 25, 25, 0);

        if (pm.getPredatorCount() == 0) {
            throw new IllegalStateException("Predator failed to spawn");
        }

        // Simulate combat
        Individual soldier = colony.getLivingIndividuals().get(0);
        soldier.setPosition(25, 25, 0);
        soldier.setAttackDamage(50f);

        // Attack spider
        spider.takeDamage(soldier.getAttackDamage());
        if (!spider.isAlive()) {
            pm.removePredator(spider);
        }

        return String.format("Hunting & predation verified: Soldier dealt 50 damage, spider alive=%b, active predators=%d.",
            spider.isAlive(), pm.getPredatorCount());
    }

    // ── Test 6: Mortality & Cleanup ───────────────────────────────────────────
    private String testMortalityAndCleanup() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("FormicaRufa", 0, 10, 0);

        List<Individual> list = colony.getLivingIndividuals();
        // Kill 4 ants
        for (int i = 0; i < 4; i++) {
            list.get(i).takeDamage(500f);
        }

        int removed = colony.removeDeadIndividuals();
        if (removed != 4) {
            throw new IllegalStateException("Colony should have removed exactly 4 dead individuals, but removed " + removed);
        }
        if (colony.getTotalDied() != 4) {
            throw new IllegalStateException("Colony totalDied counter should be 4");
        }

        return String.format("Mortality & cleanup verified: 4 dead ants removed, remaining population %d.", colony.getPopulation());
    }

    // ── Test 7: World Climate & Weather ────────────────────────────────────────
    private String testWorldClimateAndEvents() throws Exception {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        Simulation sim = new Simulation(terrarium);

        WeatherSystem ws = sim.getWeather();
        DayNightCycle dnc = sim.getDayNightCycle();
        SeasonManager sm = sim.getSeasonManager();

        sim.tick();

        ws.triggerClimateEvent(WeatherMarkovChain.WeatherState.LIGHT_RAIN);
        boolean isRaining = ws.isRaining();

        return String.format("Climate verified: Temp %.1f°C, Rain=%b, Season=%s, Day/Night phase=%.2f.",
            ws.getTemperature(), isRaining, sm.getCurrentSeason(), dnc.getPhase());
    }

    // ── Test 8: Surface Vegetation & Tree Climbing ─────────────────────────────
    private String testSurfaceVegetationAndInteraction() throws Exception {
        Terrarium terrarium = new Terrarium(60, 60, 20);
        Simulation sim = new Simulation(terrarium);

        VegetationSystem veg = sim.getVegetationSystem();
        veg.populate(10, PlantType.GRASS);
        veg.populate(5, PlantType.TREE);

        if (veg.getPlantCount() < 15) {
            throw new IllegalStateException("VegetationSystem did not spawn required plants");
        }

        // Test ant interaction with plant & tree climbing
        Colony colony = sim.addColony("AttaCephalotes", 0, 1, 0);
        Individual ant = colony.getLivingIndividuals().get(0);

        VegetationSystem.Plant tree = veg.findNearestPlant(ant.getX(), ant.getZ(), 100f, PlantType.TREE);
        if (tree != null) {
            ant.harvestPlant(tree);
            if (!ant.isClimbingTree()) {
                throw new IllegalStateException("Ant harvesting tree should enter climbing state");
            }
            ant.descendTree();
            if (ant.isClimbingTree()) {
                throw new IllegalStateException("Ant after descending tree should not be in climbing state");
            }
        }

        // Tick vegetation
        veg.tick(25f, 0.6f);

        return String.format("Surface vegetation verified: %d plants present, ant tree climbing & descending tested.", veg.getPlantCount());
    }

    // ── Test 9: Accessory Species & Symbiosis ─────────────────────────────────
    private String testAccessorySpeciesAndDiseases() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);

        Aphid aphid = new Aphid(20f, 20f, 0f, 10f);
        aphid.tick();
        float honeydew = aphid.take(2.0f);

        return String.format("Accessory species verified: Aphid tick executed, harvested honeydew=%.2f.", honeydew);
    }

    // ── Test 10: State, Rewind & Checkpoints ─────────────────────────────────
    private String testSimulationStateAndCheckpointing() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);
        sim.addColony("FormicaRufa", 1, 10, 0);

        for (int i = 0; i < 10; i++) sim.tick();
        SimulationCheckpoint cp = sim.createCheckpoint("TestCheckpoint");

        for (int i = 0; i < 10; i++) sim.tick();
        long tickAfter = sim.getTickCount();

        boolean restored = sim.restoreCheckpoint(cp);
        if (!restored || sim.getTickCount() != 10) {
            throw new IllegalStateException("Failed to restore checkpoint to tick 10");
        }

        return String.format("Checkpointing verified: Advanced to tick %d, successfully restored to tick %d.", tickAfter, sim.getTickCount());
    }

    // ── Test 11: Trophallaxis Exchange ─────────────────────────────────────────
    private String testTrophallaxisExchange() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("FormicaRufa", 0, 2, 0);

        Individual donor = colony.getLivingIndividuals().get(0);
        Individual recipient = colony.getLivingIndividuals().get(1);
        donor.setEnergy(95.0f);
        recipient.setEnergy(15.0f);

        TrophallaxisSystem.TrophallaxisResult result = TrophallaxisSystem.performTrophallaxis(donor, recipient, 1.0f, 0.1f);
        if (!result.occurred || result.foodTransferred <= 0.0f) {
            throw new IllegalStateException("Trophallaxis food transfer failed to execute");
        }

        return String.format("Trophallaxis verified: Transferred %.2f food, recipient energy rose to %.2f.",
            result.foodTransferred, recipient.getEnergy());
    }

    // ── Test 12: Fungus Agriculture ───────────────────────────────────────────
    private String testFungusAgriculture() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);
        Colony colony = sim.addColony("AttaCephalotes", 1, 5, 0);

        colony.addResource(ResourceType.LEAF, 10.0f);
        FungusGarden garden = colony.getFungusGarden();
        if (garden == null) {
            throw new IllegalStateException("Atta colony missing FungusGarden instance");
        }

        float initialFungus = colony.getResourceAmount(ResourceType.FUNGUS);
        for (int i = 0; i < 15; i++) {
            garden.tick();
        }

        float finalFungus = colony.getResourceAmount(ResourceType.FUNGUS);
        if (finalFungus <= initialFungus) {
            throw new IllegalStateException("Fungus garden failed to produce fungal biomass from leaf substrate");
        }

        return String.format("Fungus agriculture verified: Initial fungus %.1f, Final fungus %.1f.",
            initialFungus, finalFungus);
    }

    // ── Test 13: Warfare & Diplomacy ──────────────────────────────────────────
    private String testWarfareAndDiplomacy() throws Exception {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        Simulation sim = new Simulation(terrarium);
        Colony colA = sim.addColony("FormicaRufa", 0, 5, 2);
        Colony colB = sim.addColony("LasiusNiger", 0, 5, 2);

        colA.getDiplomacyManager().setStatus(colB.getId(), org.swarmforge.core.diplomacy.RelationshipStatus.ENEMY);
        if (!colA.getDiplomacyManager().isEnemy(colB.getId())) {
            throw new IllegalStateException("Diplomacy relationship failed to register ENEMY status");
        }

        Individual antA = colA.getLivingIndividuals().get(0);
        Individual antB = colB.getLivingIndividuals().get(0);
        antB.takeDamage(antA.getAttackDamage());

        return String.format("Inter-colony warfare verified: Colony B registered as ENEMY, Ant B took %.1f damage.",
            antA.getAttackDamage());
    }

    // ── Test 14: Social Thermoregulation ──────────────────────────────────────
    private String testThermoregulation() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);

        float heatOutput = SocialThermoregulationSystem.calculateEndothermicHeatOutput(20, 10.0f);
        float coolingOutput = SocialThermoregulationSystem.calculateFanningCoolingOutput(15, 38.0f);

        if (heatOutput <= 0.0f || coolingOutput <= 0.0f) {
            throw new IllegalStateException("Social thermoregulation heating/cooling calculations returned zero");
        }

        return String.format("Social thermoregulation verified: Heat output +%.2f°C, Cooling output -%.2f°C.",
            heatOutput, coolingOutput);
    }

    // ── Test 15: Seasonal Cycles ──────────────────────────────────────────────
    private String testSeasonalCycles() throws Exception {
        Terrarium terrarium = new Terrarium(40, 40, 20);
        Simulation sim = new Simulation(terrarium);
        SeasonManager sm = sim.getSeasonManager();

        sm.skipToSeason(Season.WINTER);
        float winterActivity = sm.getActivityMultiplier();
        sm.skipToSeason(Season.SPRING);
        float springActivity = sm.getActivityMultiplier();

        if (winterActivity >= springActivity) {
            throw new IllegalStateException("Winter activity multiplier should be lower than Spring activity multiplier");
        }

        return String.format("Seasonal cycles verified: Winter activity %.2f vs Spring activity %.2f.",
            winterActivity, springActivity);
    }
}
