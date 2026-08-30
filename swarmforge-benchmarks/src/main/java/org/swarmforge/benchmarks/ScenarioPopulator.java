package org.swarmforge.benchmarks;

import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.*;
import org.swarmforge.core.world.NestGenerator;
import org.swarmforge.core.world.VegetationSystem;

import java.util.Random;

/**
 * Factory class for constructing complete, high-fidelity 3D simulation scenarios
 * for SwarmForge benchmarking.
 * 
 * Each scenario configures:
 * 1. 3D Terrarium grid (soil, air, depth strata)
 * 2. Species & colony demographics (Queens, Nurses, Workers, Soldiers, Foragers, Brood)
 * 3. Procedural 3D Nests (Burrows, Mounds, Tree structures, Fungi vaults)
 * 4. Resource distribution (Sugar, Nectar, Insects, Seeds)
 * 5. Predator dynamics (Beetles, Spiders)
 * 6. Environmental physics (Weather, Seasons, Hydric balance, Multi-channel pheromones)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ScenarioPopulator {

    public record ScenarioDescription(
            String id,
            String name,
            String description,
            String speciesName,
            String nestType,
            Simulation simulation,
            int targetPopulation
    ) {}

    /**
     * Builds a 3D Terrarium with realistic soil strata and air layer.
     */
    public static Terrarium createTerrarium(int width, int height, int depth) {
        Terrarium terrarium = new Terrarium(width, height, depth);
        int surfaceLevel = 5;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell.Material mat = (z < surfaceLevel) ? TerrariumCell.Material.AIR : TerrariumCell.Material.EARTH;
                    terrarium.setCell(new TerrariumCell(
                            x, y, z, mat,
                            new float[TerrariumCell.PHEROMONE_TYPES], 22.0f, 60.0f));
                }
            }
        }
        return terrarium;
    }

    /**
     * Scenario 1: Temperate Garden (Lasius niger)
     */
    public static ScenarioDescription createTemperateGardenScenario(int population) {
        Terrarium terrarium = createTerrarium(100, 100, 20);
        Simulation sim = new Simulation(terrarium);
        LasiusNiger species = new LasiusNiger();

        Colony colony = new Colony(species, 50.0f, 50.0f, 5.0f);
        colony.addProtein(5000.0f);
        colony.addCarbohydrate(5000.0f);
        colony.setWaterStored(5000.0f);
        colony.createQueens(1);

        populateColonyDemographics(colony, species, population, 50.0f, 50.0f, 5.0f);
        sim.addColony(colony);

        // Generate Subterranean Burrow Nest
        NestGenerator nestGen = new NestGenerator(terrarium, 42L);
        nestGen.generate(50, 50, 5, NestGenerator.NestType.SIMPLE, 1.2f);

        // Resources & Predators
        spawnFoodCluster(sim, 30, 30, ResourceType.SUGAR, 1500.0f);
        spawnFoodCluster(sim, 70, 70, ResourceType.NECTAR, 1200.0f);
        spawnPredators(sim, PredatorType.BEETLE, 4);

        return new ScenarioDescription(
                "lasius_garden",
                "Jardin Tempéré (Lasius niger)",
                "Écosystème souterrain tempéré avec forage de ressources sucrées et prédateurs coléoptères.",
                "Lasius niger",
                "Terrier Souterrain",
                sim,
                population
        );
    }

    /**
     * Scenario 2: Forest Territory (Formica rufa)
     */
    public static ScenarioDescription createForestTerritoryScenario(int population) {
        Terrarium terrarium = createTerrarium(120, 120, 24);
        Simulation sim = new Simulation(terrarium);
        FormicaRufa species = new FormicaRufa();

        Colony colony = new Colony(species, 60.0f, 60.0f, 5.0f);
        colony.addProtein(10000.0f);
        colony.addCarbohydrate(10000.0f);
        colony.createQueens(2);

        populateColonyDemographics(colony, species, population, 60.0f, 60.0f, 5.0f);
        sim.addColony(colony);

        // Generate Above-Ground Dome Mound Nest
        NestGenerator nestGen = new NestGenerator(terrarium, 101L);
        nestGen.generate(60, 60, 5, NestGenerator.NestType.MOUND, 1.8f);

        // Vegetation & Resources
        sim.getVegetationSystem().populate(25, VegetationSystem.PlantType.TREE);
        spawnFoodCluster(sim, 25, 25, ResourceType.INSECT, 2500.0f);
        spawnFoodCluster(sim, 90, 85, ResourceType.SUGAR, 3000.0f);
        spawnPredators(sim, PredatorType.SPIDER, 6);

        return new ScenarioDescription(
                "formica_forest",
                "Forêt Épicéa (Formica rufa)",
                "Dôme d'aiguilles en forêt avec patrouilles territoriales agressives et récolte d'insectes.",
                "Formica rufa",
                "Dôme de Pin",
                sim,
                population
        );
    }

    /**
     * Scenario 3: Tropical Agriculture (Atta cephalotes)
     */
    public static ScenarioDescription createTropicalLeafcutterScenario(int population) {
        Terrarium terrarium = createTerrarium(128, 128, 25);
        Simulation sim = new Simulation(terrarium);
        AttaCephalotes species = new AttaCephalotes();

        Colony colony = new Colony(species, 64.0f, 64.0f, 5.0f);
        colony.addProtein(15000.0f);
        colony.addCarbohydrate(15000.0f);
        colony.createQueens(1);

        populateColonyDemographics(colony, species, population, 64.0f, 64.0f, 5.0f);
        sim.addColony(colony);

        // Generate Subterranean Fungi Vault Nest
        NestGenerator nestGen = new NestGenerator(terrarium, 777L);
        nestGen.generate(64, 64, 5, NestGenerator.NestType.SUBTERRANEAN_FUNGI_VAULT, 2.0f);

        // Dense Foliage & High Humidity
        sim.getWeather().setHumidity(85.0f);
        sim.getWeather().setTemperature(28.0f);
        sim.getVegetationSystem().populate(40, VegetationSystem.PlantType.TREE);
        sim.getVegetationSystem().populate(30, VegetationSystem.PlantType.SHRUB);
        spawnFoodCluster(sim, 20, 30, ResourceType.SEED, 4000.0f);
        spawnFoodCluster(sim, 100, 90, ResourceType.NECTAR, 3500.0f);

        return new ScenarioDescription(
                "atta_fungus",
                "Jungle Tropicale (Atta cephalotes)",
                "Nid agricole souterrain avec culture fongique, récolte de feuilles et forte humidité.",
                "Atta cephalotes",
                "Chambres Fongiques",
                sim,
                population
        );
    }

    /**
     * Scenario 4: Desert Swarming (Solenopsis invicta)
     */
    public static ScenarioDescription createDesertFireAntScenario(int population) {
        Terrarium terrarium = createTerrarium(100, 100, 20);
        Simulation sim = new Simulation(terrarium);
        SolenopsisInvicta species = new SolenopsisInvicta();

        Colony colony = new Colony(species, 50.0f, 50.0f, 5.0f);
        colony.addProtein(8000.0f);
        colony.addCarbohydrate(8000.0f);
        colony.createQueens(5); // Polygyne colony

        populateColonyDemographics(colony, species, population, 50.0f, 50.0f, 5.0f);
        sim.addColony(colony);

        NestGenerator nestGen = new NestGenerator(terrarium, 999L);
        nestGen.generate(50, 50, 5, NestGenerator.NestType.MATURE, 1.5f);

        sim.getWeather().setTemperature(35.0f);
        sim.getWeather().setHumidity(30.0f);
        spawnFoodCluster(sim, 15, 15, ResourceType.INSECT, 3000.0f);
        spawnFoodCluster(sim, 85, 80, ResourceType.SUGAR, 3000.0f);
        spawnPredators(sim, PredatorType.SPIDER, 5);

        return new ScenarioDescription(
                "solenopsis_desert",
                "Supercolonie Aride (Solenopsis invicta)",
                "Réseau polygynique dense en milieu aride à forte agressivité et essaimage rapide.",
                "Solenopsis invicta",
                "Supercolonie Mature",
                sim,
                population
        );
    }

    /**
     * Helper to populate colony with realistic caste distribution.
     */
    private static void populateColonyDemographics(Colony colony, Species species, int count, float cx, float cy, float cz) {
        int nurseCount = (int) (count * 0.20);
        int soldierCount = (int) (count * 0.15);
        int foragerCount = (int) (count * 0.35);
        int workerCount = count - (nurseCount + soldierCount + foragerCount);

        Random rng = new Random(42);

        for (int i = 0; i < nurseCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.NURSE, cx + (float)(rng.nextGaussian()*3), cy + (float)(rng.nextGaussian()*3), cz);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.NURSE);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            colony.addIndividual(ind);
        }

        for (int i = 0; i < soldierCount; i++) {
            Individual ind = colony.createSoldier();
            ind.setPosition(cx + (float)(rng.nextGaussian()*5), cy + (float)(rng.nextGaussian()*5), cz);
            ind.setJob(Individual.Job.GUARD);
        }

        for (int i = 0; i < foragerCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.FORAGER, cx + (float)(rng.nextGaussian()*8), cy + (float)(rng.nextGaussian()*8), cz);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.FORAGER);
            ind.setBrain(new org.swarmforge.core.behavior.BDIArchitecture());
            colony.addIndividual(ind);
        }

        for (int i = 0; i < workerCount; i++) {
            Individual ind = colony.createWorker();
            ind.setPosition(cx + (float)(rng.nextGaussian()*5), cy + (float)(rng.nextGaussian()*5), cz);
            ind.setJob(Individual.Job.BUILDER);
        }
    }

    private static void spawnFoodCluster(Simulation sim, float x, float y, ResourceType type, float amount) {
        sim.spawnFood(x, y, 5.0f, amount, type);
        sim.spawnFood(x + 2, y + 2, 5.0f, amount * 0.5f, type);
    }

    private static void spawnPredators(Simulation sim, PredatorType type, int count) {
        Random rng = new Random(123);
        for (int p = 0; p < count; p++) {
            sim.getPredatorManager().spawnPredator(
                    type,
                    (float) (rng.nextDouble() * 80 + 10),
                    (float) (rng.nextDouble() * 80 + 10),
                    5.0f
            );
        }
    }
}
