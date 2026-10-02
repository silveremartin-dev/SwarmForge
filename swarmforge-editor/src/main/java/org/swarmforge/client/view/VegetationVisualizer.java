/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.VegetationSystem;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D Vegetation, Nest Architectures and Naturalistic Flora/Fauna visualizer for JMonkeyEngine.
 * Handles rendering of trees, plants, biological nests, and ambient natural phenomena across 3 distinct modes:
 * - REALISTIC: 150+ native standalone OBJ Nature Pack models (Oaks, Birches, Spruces, Palms, Bushes, Rocks, Cacti)
 * - SCIENTIFIC: Metric parametric trees with DBH markers, LAI foliage envelopes, and sensor nodes
 * - GAMIFIED: Authentic full-scale Minecraft cubic voxel trees and structured block nests with active shadows
 *
 * Features physical wind sway coupling, hemisphere-aware seasonal foliage tinting, and micro-faunal agents.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class VegetationVisualizer {

    private final AssetManager assetManager;
    private final Node rootNode;
    private RenderMode currentRenderMode = RenderMode.REALISTIC;
    private boolean visible = true;
    private float swayTime = 0.0f;
    private double currentLatitude = 45.0; // Default temperate Northern hemisphere
    private Season currentSeason = Season.SPRING;
    private Terrarium activeTerrarium;
    private VegetationSystem activeVegetationSystem;
    private int currentGridWidth = 64;
    private int currentGridHeight = 64;

    // Loaded 3D Assets (Categorized Lists of OBJ Models)
    private final List<Spatial> deciduousTrees = new ArrayList<>();
    private final List<Spatial> coniferTrees = new ArrayList<>();
    private final List<Spatial> palmTrees = new ArrayList<>();
    private final List<Spatial> deadTrees = new ArrayList<>();
    private final List<Spatial> deadwoodLogs = new ArrayList<>();
    private final List<Spatial> bushes = new ArrayList<>();
    private final List<Spatial> flowers = new ArrayList<>();
    private final List<Spatial> mushrooms = new ArrayList<>();
    private final List<Spatial> rocks = new ArrayList<>();
    private final List<Spatial> mossyRocks = new ArrayList<>();
    private final List<Spatial> grassTufts = new ArrayList<>();
    private final List<Spatial> twigsAndBranches = new ArrayList<>();
    private Spatial cactusModel;
    private Spatial bambooModel;
    private Spatial beehiveModel;
    private boolean uvVisionMode = false;
    private boolean ommatidialVisionMode = false;
    private final Node ommatidialOverlayNode = new Node("OmmatidialOverlayNode");

    // Multi-Mode Nest Architecture & Colony Infrastructure System
    private final Node nestArchitectureNode = new Node("NestArchitectureNode");
    private final Node colonyInfrastructureNode = new Node("ColonyInfrastructureNode");
    private final List<Colony> trackedColonies = new ArrayList<>();

    // Ambient Atmosphere & Biological Fauna (Butterflies, Fireflies, Leaves, Birds, Pollen, Dust, Water Striders, Webs, Steam, Stridulations, Pappus)
    private final Node ambientAtmosphereNode = new Node("AmbientAtmosphereNode");
    private final List<Vector3f> activeFlowerLocations = new ArrayList<>();
    private final List<Vector3f> activeCanopyLocations = new ArrayList<>();
    private final List<ButterflyAgent> activeButterflies = new ArrayList<>();
    private final List<FireflyParticle> activeFireflies = new ArrayList<>();
    private final List<FallingLeafParticle> activeFallingLeaves = new ArrayList<>();
    private final List<BirdAgent> activeBirds = new ArrayList<>();
    private final List<PollenParticle> activePollen = new ArrayList<>();
    private final List<DesertDustParticle> activeDesertDust = new ArrayList<>();
    private final List<WaterStriderAgent> activeWaterStriders = new ArrayList<>();
    private final List<OrbWebAgent> activeOrbWebs = new ArrayList<>();
    private final List<SoilSteamParticle> activeSoilSteam = new ArrayList<>();
    private final List<StridulationAura> activeStridulations = new ArrayList<>();
    private final List<DandelionPappusParticle> activePappus = new ArrayList<>();
    private final List<MushroomClusterAgent> activeMushroomClusters = new ArrayList<>();

    // Dynamic Micro-Hydrology Agents (Flaques éphémères, égouttement de canopée, essaims de moucherons)
    private final List<DynamicPuddleAgent> activePuddles = new ArrayList<>();
    private final List<CanopyDripParticle> activeCanopyDrips = new ArrayList<>();
    private final List<GnatSwarmAgent> activeGnatSwarms = new ArrayList<>();

    // Trophobiosis, Extrafloral Nectaries & Fungal Pathogens (Pucerons, nectaires, Ophiocordyceps)
    private final List<AphidClusterAgent> activeAphidClusters = new ArrayList<>();
    private final List<ExtrafloralNectaryAgent> activeNectaries = new ArrayList<>();
    private final List<CordycepsCadaverAgent> activeCordycepsCadavers = new ArrayList<>();
    private final List<Geometry> activeMudcracks = new ArrayList<>();

    private float rainWetness = 0.0f;

    // Tree Species Distribution Matrix from World Editor (0=Oak, 1=Pine, 2=Acacia, 3=Cactus, 4=Birch, 5=Bamboo, 6=Deadwood)
    private int oakPct = 60;
    private int pinePct = 20;
    private int acaciaPct = 0;
    private int cactusPct = 0;
    private int birchPct = 10;
    private int bambooPct = 0;
    private int deadWoodPct = 10;

    public VegetationVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("VegetationNode");
        this.rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        this.rootNode.attachChild(ambientAtmosphereNode);
        this.rootNode.attachChild(nestArchitectureNode);
        this.rootNode.attachChild(colonyInfrastructureNode);
        loadAssets();
    }

    private void loadAssets() {
        // 1. Deciduous / Oak / Lush Forest Trees
        loadIntoList(deciduousTrees, "models/nature_pack_obj/forest_Tree_average_lush_Cube_004.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/forest_Tree_average_regular_Cube_002.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/forest_Tree_small_regular_Cube_005.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/lp_tree_1_Cube_029.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/lp_tree_2_Cube_028.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/lp_tree_3_Cube_001.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/lp_tree_4_Cube_033.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/lp_tree_5_Cube_034.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/lp_tree_6_Cube_032.obj");

        // 2. Conifers / Spruces / Pines
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_small_01_Cylinder_016.obj");
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_small_02_Cylinder_003.obj");
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_tiny_01_Cylinder_012.obj");
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_tiny_02_Cylinder_014.obj");

        // 3. Dead Trees, Logs & Stumps (Potée forestière / Litière)
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Tree_average_bare_Cube.obj");
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Tree_small_bare_Cube_007.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Log_big_regular_Cylinder_015.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Log_big_knotty_Cylinder_017.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Branch_average_bare_Cylinder_024.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Branch_average_leaves_Cylinder_025.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Stump_average_flat_Cube_013.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Stump_average_high_Cube_015.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Stump_average_hollow_Cube_016.obj");
        loadIntoList(deadwoodLogs, "models/nature_pack_obj/forest_Stump_average_low_Cube_014.obj");

        // 4. Bushes & Shrubs
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_average_Plane_001.obj");
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_group_average_Plane_137.obj");
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_group_big_Plane_138.obj");
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_group_small_Plane_140.obj");

        // 5. Flowers, Grass Tufts & Micro-Twigs
        loadIntoList(flowers, "models/nature_pack_obj/forest_Flower_bush_blue_Plane_030.obj");
        loadIntoList(flowers, "models/nature_pack_obj/forest_Flower_bush_red_Plane_031.obj");
        loadIntoList(flowers, "models/nature_pack_obj/forest_Flower_bush_white_Plane_023.obj");
        loadIntoList(grassTufts, "models/nature_pack_obj/forest_Grass_bush_high_01_Plane_002.obj");
        loadIntoList(grassTufts, "models/nature_pack_obj/forest_Grass_bush_low_01_Plane_005.obj");
        loadIntoList(twigsAndBranches, "models/nature_pack_obj/forest_Branch_average_bare_Cylinder_024.obj");
        loadIntoList(twigsAndBranches, "models/nature_pack_obj/forest_Branch_average_leaves_Cylinder_025.obj");

        // 6. Mushrooms (Champignons & Ronds de sorcière)
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_big_brown_Icosphere_019.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_big_group_brown_Icosphere_027.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_flat_group_white_Cylinder_046.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_flat_white_Cylinder_029.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_high_group_yellow_Cylinder_068.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_high_yellow_Cylinder_062.obj");

        // 7. Rocks & Boulders
        loadIntoList(rocks, "models/nature_pack_obj/forest_Stone_average_01_Icosphere.obj");
        loadIntoList(rocks, "models/nature_pack_obj/forest_Stone_group_average_Icosphere_022.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_stone_2_001_Cube_027.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_stone_3_001_Cube_024.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_stone_4_001_Cube_023.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_Rock_Type1_01_mesh_001_Icosphere_003.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_Rock_Type2_01_mesh_001_Icosphere_007.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_Rock_Type3_01_mesh_001_Icosphere_011.obj");
        loadIntoList(rocks, "models/nature_pack_obj/lp_Rock_Type5_01_mesh_001_Cube_021.obj");

        // Mossy Rocks & Boulders (Orientation bryophytique)
        loadIntoList(mossyRocks, "models/nature_pack_obj/forest_Stone_average_01_mossy_Icosphere_009.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/forest_Stone_group_average_mossy_Icosphere_030.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_1_001_Cube_017.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_2_001_Cube_011.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_3_001_Cube_009.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_4_001_Cube_008.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_5_001_Cube_006.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_6_001_Cube_005.obj");
        loadIntoList(mossyRocks, "models/nature_pack_obj/lp_stone_with_moss_7_001_Cube_002.obj");

        // 8. Cacti & Bamboo Sets
        cactusModel = safeLoadModel("models/cactus.obj");
        bambooModel = safeLoadModel("models/bamboo_set.obj");

        // 9. Beehive
        beehiveModel = safeLoadModel("models/beehive/beehive_low.glb");
        if (beehiveModel == null) beehiveModel = safeLoadModel("models/beehive/beehive_box.glb");

        // 10. Tropical Palms
        loadIntoList(palmTrees, "models/nature_pack_obj/lp_banan_2_Plane_002.obj");
        loadIntoList(palmTrees, "models/nature_pack_obj/lp_banan_3_Plane_001.obj");

        // 11. Load Low-Poly Ground Foliage into Bushes
        for (int i = 10; i <= 17; i++) {
            Spatial lp = safeLoadModel(String.format("models/nature_pack_obj/lp_Plane_%03d_Plane_%03d.obj", i, i + 9));
            if (lp != null) bushes.add(lp);
        }
    }

    private void loadIntoList(List<Spatial> list, String path) {
        Spatial s = safeLoadModel(path);
        if (s != null) {
            list.add(s);
        }
    }

    private Spatial safeLoadModel(String path) {
        try {
            Spatial model = assetManager.loadModel(path);
            if (model != null) {
                applyAlphaLightingAndShadows(model);
            }
            return model;
        } catch (Exception e) {
            return null;
        }
    }

    private void applyAlphaLightingAndShadows(Spatial spatial) {
        spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                // Ensure double-sided shadow depth rendering for foliage and leaves
                String name = geom.getName() != null ? geom.getName().toLowerCase() : "";
                if (name.contains("leaf") || name.contains("leaves") || name.contains("plane") || name.contains("branch") || name.contains("bush")) {
                    mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                } else {
                    mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
                }
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applyAlphaLightingAndShadows(child);
            }
        }
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setTreeSpeciesComposition(int oak, int pine, int acacia, int cactus, int birch, int bamboo, int deadWood) {
        this.oakPct = Math.max(0, oak);
        this.pinePct = Math.max(0, pine);
        this.acaciaPct = Math.max(0, acacia);
        this.cactusPct = Math.max(0, cactus);
        this.birchPct = Math.max(0, birch);
        this.bambooPct = Math.max(0, bamboo);
        this.deadWoodPct = Math.max(0, deadWood);
    }

    public int sampleTreeSpeciesIndex(Random rand) {
        int total = oakPct + pinePct + acaciaPct + cactusPct + birchPct + bambooPct + deadWoodPct;
        if (total <= 0) return 0; // Default to Oak
        int roll = rand.nextInt(total);
        if (roll < oakPct) return 0; // Oak
        roll -= oakPct;
        if (roll < pinePct) return 1; // Pine
        roll -= pinePct;
        if (roll < acaciaPct) return 2; // Acacia
        roll -= acaciaPct;
        if (roll < cactusPct) return 3; // Cactus
        roll -= cactusPct;
        if (roll < birchPct) return 4; // Birch
        roll -= birchPct;
        if (roll < bambooPct) return 5; // Bamboo
        return 6; // Dead Wood
    }

    public void setLatitude(double latitude) {
        this.currentLatitude = latitude;
    }

    public void setSeason(Season season) {
        if (this.currentSeason != season) {
            this.currentSeason = season;
            rebuildVegetation(currentGridWidth, currentGridHeight, activeTerrarium, activeVegetationSystem);
        }
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rebuildVegetation(currentGridWidth, currentGridHeight, activeTerrarium, activeVegetationSystem);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rootNode.setCullHint(visible ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
    }

    public Season getEffectiveSeason() {
        if (currentLatitude < 0) {
            return switch (currentSeason) {
                case SPRING -> Season.FALL;
                case SUMMER -> Season.WINTER;
                case FALL -> Season.SPRING;
                case WINTER -> Season.SUMMER;
            };
        }
        return currentSeason;
    }

    private float slicePlaneRatio = 1.0f;

    public void setSlicePlaneRatio(float ratio) {
        this.slicePlaneRatio = Math.max(0.05f, Math.min(1.0f, ratio));
        rebuildVegetation(currentGridWidth, currentGridHeight, activeTerrarium, activeVegetationSystem);
    }

    public void rebuildVegetation(int gridWidth, int gridHeight) {
        rebuildVegetation(gridWidth, gridHeight, activeTerrarium, activeVegetationSystem);
    }

    public void rebuildVegetation(int gridWidth, int gridHeight, Terrarium terrarium, VegetationSystem vegSystem) {
        this.currentGridWidth = gridWidth;
        this.currentGridHeight = gridHeight;
        this.activeTerrarium = terrarium;
        this.activeVegetationSystem = vegSystem;

        rootNode.detachAllChildren();
        rootNode.attachChild(ambientAtmosphereNode);
        rootNode.attachChild(nestArchitectureNode);
        rootNode.attachChild(colonyInfrastructureNode);
        if (ommatidialVisionMode) {
            rootNode.attachChild(ommatidialOverlayNode);
        }
        ambientAtmosphereNode.detachAllChildren();
        colonyInfrastructureNode.detachAllChildren();
        activeFlowerLocations.clear();
        activeCanopyLocations.clear();
        activeButterflies.clear();
        activeFireflies.clear();
        activeFallingLeaves.clear();
        activeBirds.clear();
        activePollen.clear();
        activeDesertDust.clear();
        activeWaterStriders.clear();
        activeOrbWebs.clear();
        activeSoilSteam.clear();
        activeStridulations.clear();
        activePappus.clear();
        activeMushroomClusters.clear();
        activePuddles.clear();
        activeCanopyDrips.clear();
        activeGnatSwarms.clear();
        activeAphidClusters.clear();
        activeNectaries.clear();
        activeCordycepsCadavers.clear();
        activeMudcracks.clear();
        rebuildNestArchitectures();

        if (!visible) return;

        Biome biome = Biome.forLatitude(currentLatitude);
        Season effectiveSeason = getEffectiveSeason();
        Random rand = new Random(42);
        int cutX = Math.max(2, Math.min(gridWidth, (int) Math.ceil(gridWidth * Math.max(0.05f, Math.min(1.0f, slicePlaneRatio)))));

        float marginX = Math.min(3.5f, cutX * 0.12f);
        float marginZ = Math.min(3.5f, gridHeight * 0.12f);
        float minX = marginX;
        float maxX = Math.max(minX, cutX - marginX);
        float minZ = marginZ;
        float maxZ = Math.max(minZ, gridHeight - marginZ);

        if (vegSystem != null && !vegSystem.getPlants().isEmpty()) {
            // Position flora according to real simulation plants
            for (VegetationSystem.Plant plant : vegSystem.getPlants()) {
                if (plant.x >= cutX - 0.5f) continue; // Respect 3D geological cutaway slice
                float x = Math.max(minX, Math.min(maxX, plant.x));
                float z = Math.max(minZ, Math.min(maxZ, plant.y)); // Horizontal Y in domain -> JME Z
                float y = (terrarium != null) ? terrarium.getSurfaceElevation(x, z) : 0.0f;

                if (currentRenderMode == RenderMode.REALISTIC) {
                    createRealisticFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand);
                } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    createScientificFloraForPlant(x, y, z, plant, biome, rand);
                } else if (currentRenderMode == RenderMode.GAMIFIED) {
                    createGamifiedFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand);
                }
            }
        } else if (terrarium != null) {
            // Procedural Natural Vegetation Pass for World Editor / Initial World state:
            // Multi-tiered natural ecosystem: Canopy Trees, Sub-canopy Saplings & Understory Shrubs
            float treeStep = 8.0f; // Unified spatial grid step across Realistic, Scientific, and Gamified modes
            for (float tx = minX + 0.5f; tx <= maxX - 0.5f; tx += treeStep) {
                for (float tz = minZ + 0.5f; tz <= maxZ - 0.5f; tz += treeStep) {
                    float jx = Math.max(minX, Math.min(maxX, tx + (rand.nextFloat() - 0.5f) * (treeStep * 0.75f)));
                    float jz = Math.max(minZ, Math.min(maxZ, tz + (rand.nextFloat() - 0.5f) * (treeStep * 0.75f)));

                    int ix = Math.max(0, Math.min(gridWidth - 1, Math.round(jx)));
                    int iz = Math.max(0, Math.min(gridHeight - 1, Math.round(jz)));
                    float elev = terrarium.getSurfaceElevation(jx, jz);
                    int iy = Math.max(0, Math.min(terrarium.getDepth() - 1, Math.round(elev)));

                    TerrariumCell cell = terrarium.getCell(ix, iz, iy);
                    if (cell == null || cell.material() == TerrariumCell.Material.WATER || cell.material() == TerrariumCell.Material.CAVITY) {
                        continue;
                    }

                    int speciesIdx = sampleTreeSpeciesIndex(rand);
                    if (currentRenderMode == RenderMode.GAMIFIED) {
                        createProceduralTreeGamified(jx, elev, jz, biome, effectiveSeason, rand, speciesIdx);
                    } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                        createProceduralTreeScientific(jx, elev, jz, biome, rand, speciesIdx);
                    } else {
                        // Canopy Tree
                        createRealisticFlora(jx, elev, jz, biome, effectiveSeason, rand, speciesIdx);

                        // 1. Ronds de sorcière (Fairy Rings): 38% probability around mature canopy trees in vegetative biomes
                        if ((biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.MEDITERRANEAN || biome == Biome.WETLAND)
                                && rand.nextFloat() < 0.38f && !mushrooms.isEmpty()) {
                            spawnFairyRing(jx, elev, jz, terrarium, minX, maxX, minZ, maxZ, rand);
                        }

                        // 2. Potée forestière & Bois mort (Deadwood, decaying logs, mossy stumps in understory): 35% probability
                        if ((biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.WETLAND)
                                && rand.nextFloat() < 0.35f && !deadwoodLogs.isEmpty()) {
                            float dx = (rand.nextFloat() - 0.5f) * 3.4f;
                            float dz = (rand.nextFloat() - 0.5f) * 3.4f;
                            float lx = Math.max(minX, Math.min(maxX, jx + dx));
                            float lz = Math.max(minZ, Math.min(maxZ, jz + dz));
                            float lelev = terrarium.getSurfaceElevation(lx, lz);
                            Spatial logModel = pickRandomFromList(deadwoodLogs, rand);
                            if (logModel != null) {
                                Spatial logInst = logModel.clone();
                                normalizeAndPositionModel(logInst, lx, lelev, lz, 0.8f + rand.nextFloat() * 0.7f, rand);
                            }
                        }

                        // 3. Secondary Understory Shrub or Sapling near tree cluster (45% probability)
                        if (rand.nextFloat() < 0.45f && !bushes.isEmpty()) {
                            float sx = Math.max(minX, Math.min(maxX, jx + (rand.nextFloat() - 0.5f) * 3.2f));
                            float sz = Math.max(minZ, Math.min(maxZ, jz + (rand.nextFloat() - 0.5f) * 3.2f));
                            float selev = terrarium.getSurfaceElevation(sx, sz);
                            Spatial shrubInstance = pickRandomFromList(bushes, rand).clone();
                            applySeasonalTint(shrubInstance, effectiveSeason, biome);
                            normalizeAndPositionModel(shrubInstance, sx, selev, sz, 1.4f + rand.nextFloat() * 1.2f, rand);
                        }
                    }
                }
            }
        }

        // Realistic Mode Natural Ground Scatter Pass:
        // Distributes pebbles, gravel, mossy rocks, mushrooms, deadwood, and grass tufts
        // based on simulation substrate and soil coverage.
        if (currentRenderMode == RenderMode.REALISTIC && terrarium != null) {
            spawnRealisticGroundScatter(terrarium, cutX, biome, effectiveSeason, rand);
            rebuildAtmosphericFauna(terrarium, biome, effectiveSeason, rand);
        }
    }

    private void spawnFairyRing(float cx, float cy, float cz, Terrarium terrarium, float minX, float maxX, float minZ, float maxZ, Random rand) {
        int count = 5 + rand.nextInt(4); // 5 to 8 mushrooms in a natural fairy circle
        float ringRadius = 1.8f + rand.nextFloat() * 1.2f;
        float baseAngle = rand.nextFloat() * FastMath.TWO_PI;
        List<Spatial> ringInstances = new ArrayList<>();
        for (int m = 0; m < count; m++) {
            float angle = baseAngle + (m / (float) count) * FastMath.TWO_PI + (rand.nextFloat() - 0.5f) * 0.25f;
            float mx = cx + FastMath.cos(angle) * ringRadius;
            float mz = cz + FastMath.sin(angle) * ringRadius;
            if (mx >= minX && mx <= maxX && mz >= minZ && mz <= maxZ) {
                float melev = terrarium.getSurfaceElevation(mx, mz);
                Spatial mModel = pickRandomFromList(mushrooms, rand);
                if (mModel != null) {
                    Spatial mInst = mModel.clone();
                    float targetH = 0.32f + rand.nextFloat() * 0.22f;
                    mInst.setUserData("OriginalScaleY", targetH);
                    normalizeAndPositionModel(mInst, mx, melev, mz, targetH, rand);
                    ringInstances.add(mInst);
                }
            }
        }
        if (!ringInstances.isEmpty()) {
            activeMushroomClusters.add(new MushroomClusterAgent(ringInstances, cx, cz));
        }
    }

    private void spawnRealisticGroundScatter(Terrarium terrarium, int cutX, Biome biome, Season season, Random rand) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        // Sample surface with tighter biological step clamped inside terrain margins
        int step = Math.max(2, Math.min(width, height) / 24);
        for (int x = 1; x < cutX - 1; x += step) {
            for (int y = 1; y < height - 1; y += step) {
                float jx = Math.max(1.0f, Math.min(cutX - 1.0f, x + (rand.nextFloat() - 0.5f) * (step * 0.9f)));
                float jz = Math.max(1.0f, Math.min(height - 1.0f, y + (rand.nextFloat() - 0.5f) * (step * 0.9f)));
                if (jx >= cutX - 0.5f || jx < 0.5f || jz < 0.5f || jz >= height - 0.5f) continue;

                float elev = terrarium.getSurfaceElevation(jx, jz);
                int ix = Math.max(0, Math.min(width - 1, Math.round(jx)));
                int iy = Math.max(0, Math.min(height - 1, Math.round(jz)));
                int iz = Math.max(0, Math.min(depth - 1, Math.round(elev)));

                TerrariumCell topCell = terrarium.getCell(ix, iy, iz);
                if (topCell == null || topCell.material() == TerrariumCell.Material.AIR || topCell.material() == TerrariumCell.Material.WATER) {
                    continue;
                }

                TerrariumCell.Material mat = topCell.material();
                float roll = rand.nextFloat();

                Spatial scatterModel = null;
                float targetScale = 0.40f;

                if (mat == TerrariumCell.Material.ROCK || mat == TerrariumCell.Material.GRAVEL) {
                    // Pebble & Rock Scatter on stony ground
                    if (roll < 0.35f && !mossyRocks.isEmpty()) {
                        scatterModel = pickRandomFromList(mossyRocks, rand);
                        targetScale = 0.40f + rand.nextFloat() * 0.50f;
                    } else if (roll < 0.65f && !rocks.isEmpty()) {
                        scatterModel = pickRandomFromList(rocks, rand);
                        targetScale = (mat == TerrariumCell.Material.GRAVEL) ? (0.25f + rand.nextFloat() * 0.35f) : (0.50f + rand.nextFloat() * 0.85f);
                    } else if (roll < 0.85f && !twigsAndBranches.isEmpty()) {
                        scatterModel = pickRandomFromList(twigsAndBranches, rand);
                        targetScale = 0.30f + rand.nextFloat() * 0.25f;
                    }
                } else if (mat == TerrariumCell.Material.PEAT || mat == TerrariumCell.Material.LEAF_LITTER) {
                    // Mushrooms, Micro-Twigs & Fallen Forest Detritus (Potée forestière / Litière) on rich organic soil
                    if (roll < 0.35f && !mushrooms.isEmpty()) {
                        scatterModel = pickRandomFromList(mushrooms, rand);
                        targetScale = 0.32f + rand.nextFloat() * 0.25f;
                    } else if (roll < 0.60f && !twigsAndBranches.isEmpty()) {
                        scatterModel = pickRandomFromList(twigsAndBranches, rand);
                        targetScale = 0.35f + rand.nextFloat() * 0.30f;
                    } else if (roll < 0.80f && !deadwoodLogs.isEmpty()) {
                        scatterModel = pickRandomFromList(deadwoodLogs, rand);
                        targetScale = 0.65f + rand.nextFloat() * 0.55f;
                    } else if (!mossyRocks.isEmpty()) {
                        scatterModel = pickRandomFromList(mossyRocks, rand);
                        targetScale = 0.38f + rand.nextFloat() * 0.45f;
                    }
                } else if (mat == TerrariumCell.Material.SAND) {
                    // Desert Pebbles & Small Scrub
                    if (roll < 0.35f && !rocks.isEmpty()) {
                        scatterModel = pickRandomFromList(rocks, rand);
                        targetScale = 0.25f + rand.nextFloat() * 0.35f;
                    } else if (roll < 0.60f && !twigsAndBranches.isEmpty()) {
                        scatterModel = pickRandomFromList(twigsAndBranches, rand);
                        targetScale = 0.28f + rand.nextFloat() * 0.20f;
                    }
                } else if (mat == TerrariumCell.Material.EARTH || mat == TerrariumCell.Material.SILT) {
                    // Abundant Ground Flora: Grass Tufts, Wild Flowers, Micro-Twigs, Shrubs, Mushrooms & Mossy Stones
                    if (roll < 0.30f && !grassTufts.isEmpty()) {
                        scatterModel = pickRandomFromList(grassTufts, rand);
                        targetScale = 0.35f + rand.nextFloat() * 0.40f;
                    } else if (roll < 0.55f && !flowers.isEmpty()) {
                        scatterModel = pickRandomFromList(flowers, rand);
                        targetScale = 0.40f + rand.nextFloat() * 0.45f;
                        activeFlowerLocations.add(new Vector3f(jx, elev, jz));
                    } else if (roll < 0.72f && !twigsAndBranches.isEmpty()) {
                        scatterModel = pickRandomFromList(twigsAndBranches, rand);
                        targetScale = 0.32f + rand.nextFloat() * 0.28f;
                    } else if (roll < 0.85f && !bushes.isEmpty()) {
                        scatterModel = pickRandomFromList(bushes, rand);
                        targetScale = 0.60f + rand.nextFloat() * 0.60f;
                    } else if (roll < 0.94f && !mushrooms.isEmpty()) {
                        scatterModel = pickRandomFromList(mushrooms, rand);
                        targetScale = 0.32f + rand.nextFloat() * 0.25f;
                    } else if (!deadwoodLogs.isEmpty()) {
                        scatterModel = pickRandomFromList(deadwoodLogs, rand);
                        targetScale = 0.70f + rand.nextFloat() * 0.50f;
                    }
                }

                if (scatterModel != null) {
                    Spatial instance = scatterModel.clone();
                    applySeasonalTint(instance, season, biome);
                    instance.setUserData("OriginalScaleY", targetScale);
                    normalizeAndPositionModel(instance, jx, elev, jz, targetScale, rand);
                    if (mushrooms.contains(scatterModel)) {
                        List<Spatial> single = new ArrayList<>(1);
                        single.add(instance);
                        activeMushroomClusters.add(new MushroomClusterAgent(single, jx, jz));
                    }
                }
            }
        }
    }

    private void createRealisticFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Season season, Random rand) {
        Spatial chosenModel = null;
        float targetHeight = 8.0f * Math.max(0.4f, plant.growth);

        switch (plant.type) {
            case TREE -> {
                chosenModel = pickModelForBiome(biome, rand, true);
                String name = (chosenModel != null && chosenModel.getName() != null) ? chosenModel.getName().toLowerCase() : "";
                if (name.contains("stump") || name.contains("log")) {
                    targetHeight = (0.9f + rand.nextFloat() * 0.6f) * Math.max(0.4f, plant.growth);
                } else if (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
                    targetHeight = (7.5f + rand.nextFloat() * 2.5f) * Math.max(0.4f, plant.growth);
                } else {
                    targetHeight = (7.0f + rand.nextFloat() * 2.5f) * Math.max(0.4f, plant.growth);
                }
            }
            case SHRUB -> {
                targetHeight = (1.6f + rand.nextFloat() * 1.1f) * Math.max(0.4f, plant.growth);
                chosenModel = pickRandomFromList(bushes, rand);
            }
            case FLOWER -> {
                targetHeight = (0.22f + rand.nextFloat() * 0.20f) * Math.max(0.4f, plant.growth);
                chosenModel = pickRandomFromList(flowers, rand);
                activeFlowerLocations.add(new Vector3f(x, y, z));
            }
            case MOSS, GRASS -> {
                targetHeight = (0.15f + rand.nextFloat() * 0.20f) * Math.max(0.4f, plant.growth);
                chosenModel = !grassTufts.isEmpty() ? pickRandomFromList(grassTufts, rand) : pickRandomFromList(flowers, rand);
            }
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            applySeasonalTint(instance, season, biome);
            normalizeAndPositionModel(instance, x, y, z, Math.max(0.20f, targetHeight), rand);
        } else {
            createProceduralTree3D(x, y, z, (biome == Biome.ALPINE_SNOW) ? 4 : 0, rand, season);
        }
    }

    private void createGamifiedFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Season season, Random rand) {
        if (plant.type == VegetationSystem.PlantType.TREE) {
            createProceduralTreeGamified(x, y, z, biome, season, rand);
            return;
        }

        Node floraNode = new Node("GamifiedFlora");
        floraNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        floraNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        floraNode.setUserData("BaseRotY", rotY);

        float micro = 0.15f;

        switch (plant.type) {
            case SHRUB -> {
                Material shrubMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                shrubMat.setBoolean("UseMaterialColors", true);
                ColorRGBA shrubCol = getSeasonFoliageColor(season, biome);
                shrubMat.setColor("Diffuse", shrubCol);
                shrubMat.setColor("Ambient", shrubCol.mult(0.6f));
                shrubMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

                // Multi-block stepped micro-voxel shrub
                float scale = Math.max(0.5f, plant.growth);
                floraNode.attachChild(createMicroVoxel("ShrubCore", 0.60f * scale, 0.50f * scale, 0.60f * scale, shrubMat, 0, 0.25f * scale, 0));
                floraNode.attachChild(createMicroVoxel("ShrubL", 0.30f * scale, 0.35f * scale, 0.30f * scale, shrubMat, -0.30f * scale, 0.18f * scale, 0.10f * scale));
                floraNode.attachChild(createMicroVoxel("ShrubR", 0.30f * scale, 0.35f * scale, 0.30f * scale, shrubMat, 0.28f * scale, 0.18f * scale, -0.10f * scale));
            }
            case FLOWER -> {
                // Stem
                Material stemMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                stemMat.setBoolean("UseMaterialColors", true);
                stemMat.setColor("Diffuse", new ColorRGBA(0.18f, 0.68f, 0.22f, 1f));
                stemMat.setColor("Ambient", new ColorRGBA(0.10f, 0.40f, 0.12f, 1f));
                floraNode.attachChild(createMicroVoxel("FlowerStem", 0.06f, 0.32f, 0.06f, stemMat, 0, 0.16f, 0));

                // Stepped 4-petal blossom
                Material flowerMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                flowerMat.setBoolean("UseMaterialColors", true);
                ColorRGBA blossomCol = switch (rand.nextInt(3)) {
                    case 0 -> new ColorRGBA(0.95f, 0.25f, 0.25f, 1f); // Red Poppy
                    case 1 -> new ColorRGBA(0.98f, 0.85f, 0.15f, 1f); // Dandelion
                    default -> new ColorRGBA(0.25f, 0.62f, 0.98f, 1f); // Blue Orchid
                };
                flowerMat.setColor("Diffuse", blossomCol);
                flowerMat.setColor("Ambient", blossomCol.mult(0.6f));

                floraNode.attachChild(createMicroVoxel("BloomCenter", 0.14f, 0.10f, 0.14f, flowerMat, 0, 0.34f, 0));
                floraNode.attachChild(createMicroVoxel("BloomPetalN", 0.10f, 0.08f, 0.10f, flowerMat, 0, 0.33f, 0.10f));
                floraNode.attachChild(createMicroVoxel("BloomPetalS", 0.10f, 0.08f, 0.10f, flowerMat, 0, 0.33f, -0.10f));
                floraNode.attachChild(createMicroVoxel("BloomPetalE", 0.10f, 0.08f, 0.10f, flowerMat, 0.10f, 0.33f, 0));
                floraNode.attachChild(createMicroVoxel("BloomPetalW", 0.10f, 0.08f, 0.10f, flowerMat, -0.10f, 0.33f, 0));
            }
            case MOSS, GRASS -> {
                Material grassMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                grassMat.setBoolean("UseMaterialColors", true);
                ColorRGBA grassCol = (season == Season.WINTER) ? new ColorRGBA(0.75f, 0.82f, 0.80f, 1f) : new ColorRGBA(0.28f, 0.72f, 0.22f, 1f);
                grassMat.setColor("Diffuse", grassCol);
                grassMat.setColor("Ambient", grassCol.mult(0.6f));

                // Stepped multi-blade voxel grass tuft
                floraNode.attachChild(createMicroVoxel("TuftCenter", 0.12f, 0.22f, 0.12f, grassMat, 0, 0.11f, 0));
                floraNode.attachChild(createMicroVoxel("TuftBlade1", 0.08f, 0.16f, 0.08f, grassMat, 0.10f, 0.08f, 0.05f));
                floraNode.attachChild(createMicroVoxel("TuftBlade2", 0.08f, 0.14f, 0.08f, grassMat, -0.08f, 0.07f, -0.06f));
                floraNode.attachChild(createMicroVoxel("TuftBlade3", 0.08f, 0.18f, 0.08f, grassMat, -0.04f, 0.09f, 0.09f));
            }
        }
        floraNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(floraNode);
    }

    private void createScientificFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Random rand) {
        if (plant.type == VegetationSystem.PlantType.TREE) {
            createProceduralTreeScientific(x, y, z, biome, rand);
            return;
        }

        Node floraNode = new Node("ScientificFlora");
        floraNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        floraNode.setLocalTranslation(x, y, z);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);

        switch (plant.type) {
            case SHRUB -> {
                mat.setColor("Diffuse", new ColorRGBA(0.20f, 0.70f, 0.30f, 1.0f));
                mat.setColor("Ambient", new ColorRGBA(0.12f, 0.45f, 0.20f, 1.0f));
                com.jme3.scene.shape.Sphere bushSphere = new com.jme3.scene.shape.Sphere(8, 8, 0.45f * Math.max(0.4f, plant.growth));
                Geometry g = new Geometry("SciShrub", bushSphere);
                g.setMaterial(mat);
                g.setLocalTranslation(0, 0.45f * Math.max(0.4f, plant.growth), 0);
                floraNode.attachChild(g);
            }
            case FLOWER -> {
                mat.setColor("Diffuse", new ColorRGBA(0.95f, 0.80f, 0.20f, 1.0f));
                mat.setColor("Ambient", new ColorRGBA(0.60f, 0.50f, 0.10f, 1.0f));
                com.jme3.scene.shape.Sphere flowerSphere = new com.jme3.scene.shape.Sphere(6, 6, 0.20f);
                Geometry g = new Geometry("SciFlower", flowerSphere);
                g.setMaterial(mat);
                g.setLocalTranslation(0, 0.20f, 0);
                floraNode.attachChild(g);
            }
            case MOSS, GRASS -> {
                mat.setColor("Diffuse", new ColorRGBA(0.35f, 0.85f, 0.35f, 1.0f));
                mat.setColor("Ambient", new ColorRGBA(0.20f, 0.55f, 0.20f, 1.0f));
                Cylinder tuftCyl = new Cylinder(6, 6, 0.15f, 0.10f, true);
                Geometry g = new Geometry("SciGrass", tuftCyl);
                g.setMaterial(mat);
                g.setLocalTranslation(0, 0.05f, 0);
                g.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
                floraNode.attachChild(g);
            }
        }
        rootNode.attachChild(floraNode);
    }

    private void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int speciesIdx) {
        Spatial chosenModel = pickModelForSpecies(speciesIdx, biome, rand);
        if (chosenModel == null) {
            chosenModel = pickModelForBiome(biome, rand, true);
        }
        float targetHeight;

        if (speciesIdx == 3 || chosenModel == cactusModel) {
            targetHeight = 3.5f + rand.nextFloat() * 2.0f;
        } else if (speciesIdx == 5 || chosenModel == bambooModel) {
            targetHeight = 3.5f + rand.nextFloat() * 2.0f;
        } else if (speciesIdx == 6 || (chosenModel != null && deadwoodLogs.contains(chosenModel))) {
            targetHeight = 0.9f + rand.nextFloat() * 0.6f;
        } else if (speciesIdx == 2 || (chosenModel != null && palmTrees.contains(chosenModel))) {
            targetHeight = 6.5f + rand.nextFloat() * 2.5f;
        } else if (speciesIdx == 1 || biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            targetHeight = 7.5f + rand.nextFloat() * 2.5f;
        } else {
            targetHeight = 7.0f + rand.nextFloat() * 2.5f;
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            applySeasonalTint(instance, season, biome);
            normalizeAndPositionModel(instance, x, y, z, targetHeight, rand);
        } else {
            createProceduralTree3D(x, y, z, speciesIdx, rand, season);
        }
    }

    private Spatial pickModelForSpecies(int speciesIdx, Biome biome, Random rand) {
        return switch (speciesIdx) {
            case 1 -> !coniferTrees.isEmpty() ? pickRandomFromList(coniferTrees, rand) : pickRandomFromList(deciduousTrees, rand);
            case 2 -> !palmTrees.isEmpty() ? pickRandomFromList(palmTrees, rand) : (!deciduousTrees.isEmpty() ? pickRandomFromList(deciduousTrees, rand) : null);
            case 3 -> cactusModel != null ? cactusModel : (!deadTrees.isEmpty() ? pickRandomFromList(deadTrees, rand) : null);
            case 4 -> !deciduousTrees.isEmpty() ? pickRandomFromList(deciduousTrees, rand) : null;
            case 5 -> (bambooPct > 0) ? bambooModel : null;
            case 6 -> !deadwoodLogs.isEmpty() ? pickRandomFromList(deadwoodLogs, rand) : (!deadTrees.isEmpty() ? pickRandomFromList(deadTrees, rand) : null);
            default -> !deciduousTrees.isEmpty() ? pickRandomFromList(deciduousTrees, rand) : null;
        };
    }

    private Spatial pickModelForBiome(Biome biome, Random rand, boolean preferTrees) {
        if (preferTrees) {
            return switch (biome) {
                case DESERT -> (cactusModel != null && rand.nextFloat() < 0.65f) ? cactusModel : (!deadTrees.isEmpty() ? pickRandomFromList(deadTrees, rand) : cactusModel);
                case TROPICAL -> {
                    float r = rand.nextFloat();
                    if (r < 0.55f && !palmTrees.isEmpty()) yield pickRandomFromList(palmTrees, rand);
                    if (r < 0.75f && bambooPct > 0 && bambooModel != null) yield bambooModel;
                    if (!deciduousTrees.isEmpty()) yield pickRandomFromList(deciduousTrees, rand);
                    yield pickRandomFromList(palmTrees, rand);
                }
                case ALPINE_SNOW, TUNDRA -> {
                    float r = rand.nextFloat();
                    if (r < 0.88f && !coniferTrees.isEmpty()) yield pickRandomFromList(coniferTrees, rand);
                    if (!deadTrees.isEmpty()) yield pickRandomFromList(deadTrees, rand);
                    yield pickRandomFromList(coniferTrees, rand);
                }
                case MEDITERRANEAN -> {
                    float r = rand.nextFloat();
                    if (r < 0.60f && !coniferTrees.isEmpty()) yield pickRandomFromList(coniferTrees, rand);
                    if (!deciduousTrees.isEmpty()) yield pickRandomFromList(deciduousTrees, rand);
                    yield pickRandomFromList(coniferTrees, rand);
                }
                case FOREST, GRASSLAND, WETLAND -> {
                    float r = rand.nextFloat();
                    if (r < 0.78f && !deciduousTrees.isEmpty()) yield pickRandomFromList(deciduousTrees, rand);
                    if (r < 0.94f && !coniferTrees.isEmpty()) yield pickRandomFromList(coniferTrees, rand);
                    if (!deadTrees.isEmpty()) yield pickRandomFromList(deadTrees, rand);
                    yield pickRandomFromList(deciduousTrees, rand);
                }
            };
        }

        return switch (biome) {
            case DESERT -> {
                float r = rand.nextFloat();
                if (r < 0.40f && cactusModel != null) yield cactusModel;
                if (r < 0.65f && !deadTrees.isEmpty()) yield pickRandomFromList(deadTrees, rand);
                if (!rocks.isEmpty()) yield pickRandomFromList(rocks, rand);
                yield cactusModel;
            }
            case TROPICAL -> {
                float r = rand.nextFloat();
                if (r < 0.35f && !palmTrees.isEmpty()) yield pickRandomFromList(palmTrees, rand);
                if (r < 0.55f && bambooPct > 0 && bambooModel != null) yield bambooModel;
                if (r < 0.75f && !deciduousTrees.isEmpty()) yield pickRandomFromList(deciduousTrees, rand);
                if (r < 0.90f && !bushes.isEmpty()) yield pickRandomFromList(bushes, rand);
                if (!flowers.isEmpty()) yield pickRandomFromList(flowers, rand);
                yield pickRandomFromList(deciduousTrees, rand);
            }
            case ALPINE_SNOW, TUNDRA -> {
                float r = rand.nextFloat();
                if (r < 0.60f && !coniferTrees.isEmpty()) yield pickRandomFromList(coniferTrees, rand);
                if (r < 0.85f && !rocks.isEmpty()) yield pickRandomFromList(rocks, rand);
                if (!deadTrees.isEmpty()) yield pickRandomFromList(deadTrees, rand);
                yield pickRandomFromList(coniferTrees, rand);
            }
            case MEDITERRANEAN -> {
                float r = rand.nextFloat();
                if (r < 0.45f && !coniferTrees.isEmpty()) yield pickRandomFromList(coniferTrees, rand);
                if (r < 0.75f && !bushes.isEmpty()) yield pickRandomFromList(bushes, rand);
                if (!rocks.isEmpty()) yield pickRandomFromList(rocks, rand);
                yield pickRandomFromList(coniferTrees, rand);
            }
            case FOREST, GRASSLAND, WETLAND -> {
                float r = rand.nextFloat();
                if (r < 0.50f && !deciduousTrees.isEmpty()) yield pickRandomFromList(deciduousTrees, rand);
                if (r < 0.70f && !coniferTrees.isEmpty()) yield pickRandomFromList(coniferTrees, rand);
                if (r < 0.88f && !bushes.isEmpty()) yield pickRandomFromList(bushes, rand);
                if (!flowers.isEmpty()) yield pickRandomFromList(flowers, rand);
                yield pickRandomFromList(deciduousTrees, rand);
            }
        };
    }

    private Spatial pickRandomFromList(List<Spatial> list, Random rand) {
        if (list == null || list.isEmpty()) return null;
        return list.get(rand.nextInt(list.size()));
    }

    private void normalizeAndPositionModel(Spatial spatial, float x, float y, float z, float targetHeight, Random rand) {
        spatial.updateGeometricState();
        com.jme3.bounding.BoundingVolume bv = spatial.getWorldBound();

        float modelHeight = 2.0f;
        float baseYOffset = 0.0f;
        float centerX = 0.0f;
        float centerZ = 0.0f;

        if (bv instanceof BoundingBox bbox) {
            modelHeight = Math.max(0.1f, bbox.getYExtent() * 2.0f);
            float scale = targetHeight / modelHeight;
            float minY = bbox.getCenter().y - bbox.getYExtent();
            baseYOffset = -minY * scale;
            centerX = bbox.getCenter().x;
            centerZ = bbox.getCenter().z;
            spatial.setLocalScale(scale);
        } else {
            spatial.setLocalScale(targetHeight / 2.0f);
        }

        // Asymmetric knobby scaling for trees (Arbres noueux & asymétrie naturelle)
        if (targetHeight >= 3.5f) {
            float asymX = 0.86f + rand.nextFloat() * 0.28f;
            float asymZ = 0.86f + rand.nextFloat() * 0.28f;
            float asymY = 0.92f + rand.nextFloat() * 0.16f;
            spatial.setLocalScale(spatial.getLocalScale().x * asymX, spatial.getLocalScale().y * asymY, spatial.getLocalScale().z * asymZ);
        }

        // Wrap in a pivot Node that centers the model's bounding box at (0,0,0)
        // so yaw rotation keeps the tree trunk centered in place instead of orbiting
        Node pivotNode = new Node("VegPivot");
        pivotNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Offset model so its horizontal center sits at (0, 0, 0) within the pivot
        float scale = spatial.getLocalScale().x;
        spatial.setLocalTranslation(-centerX * scale, baseYOffset - 0.08f, -centerZ * scale);
        pivotNode.attachChild(spatial);

        float rotY;
        String sName = (spatial.getName() != null) ? spatial.getName().toLowerCase() : "";
        if (sName.contains("moss") || mossyRocks.contains(spatial)) {
            // Orientation bryophytique: Moss faces the cold pole (North for lat >= 0, South for lat < 0) with slight natural variance
            float coldPoleAngle = (currentLatitude >= 0.0) ? FastMath.PI : 0.0f;
            rotY = coldPoleAngle + (rand.nextFloat() - 0.5f) * 0.75f;
        } else {
            rotY = rand.nextFloat() * FastMath.TWO_PI;
        }

        float tiltX = (targetHeight >= 3.5f) ? (rand.nextFloat() - 0.5f) * 0.08f : 0.0f;
        float tiltZ = (targetHeight >= 3.5f) ? (rand.nextFloat() - 0.5f) * 0.08f : 0.0f;

        pivotNode.setLocalTranslation(x, y, z);
        pivotNode.setLocalRotation(new Quaternion().fromAngles(tiltX, rotY, tiltZ));
        pivotNode.setUserData("BaseRotY", rotY);
        pivotNode.setUserData("TiltX", tiltX);
        pivotNode.setUserData("TiltZ", tiltZ);

        rootNode.attachChild(pivotNode);
    }

    private void applySeasonalTint(Spatial spatial, Season season, Biome biome) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Diffuse") != null) {
                String name = geom.getName() != null ? geom.getName().toLowerCase() : "";
                boolean isFoliage = name.contains("leaf") || name.contains("leaves") || name.contains("foliage")
                        || name.contains("bush") || name.contains("grass") || name.contains("crown");

                // Only apply foliage season color shifting in Fall or Winter when not rock/wood/flower
                if (isFoliage && (season == Season.FALL || season == Season.WINTER)) {
                    ColorRGBA seasonalColor = getSeasonFoliageColor(season, biome);
                    if (seasonalColor != null) {
                        if (mat.getMaterialDef().getMaterialParam("DiffuseMap") != null && mat.getParam("DiffuseMap") != null) {
                            mat.setColor("Diffuse", ColorRGBA.White.mult(0.6f).add(seasonalColor.mult(0.4f)));
                        } else {
                            mat.setColor("Diffuse", seasonalColor);
                        }
                        mat.setColor("Ambient", seasonalColor.mult(0.4f));
                    }
                }
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applySeasonalTint(child, season, biome);
            }
        }
    }

    private ColorRGBA getSeasonFoliageColor(Season season, Biome biome) {
        if (biome == Biome.DESERT) {
            return new ColorRGBA(0.38f, 0.52f, 0.20f, 1.0f); // Rich olive desert tone
        }
        return switch (season) {
            case SPRING -> new ColorRGBA(0.16f, 0.52f, 0.14f, 1.0f); // Deep Minecraft foliage green
            case SUMMER -> new ColorRGBA(0.10f, 0.44f, 0.12f, 1.0f); // Lush rich chlorophyll green
            case FALL -> new ColorRGBA(0.80f, 0.40f, 0.08f, 1.0f);   // Golden autumn amber
            case WINTER -> (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA)
                    ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1.0f)       // Snowy frosty white/green
                    : new ColorRGBA(0.22f, 0.36f, 0.20f, 1.0f);       // Muted winter evergreen
        };
    }

    private void createProceduralTree3D(float x, float y, float z, int speciesType, Random rand, Season season) {
        Node treeNode = new Node("Tree3D");
        treeNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        trunkMat.setColor("Diffuse", new ColorRGBA(0.42f, 0.25f, 0.12f, 1f));
        trunkMat.setColor("Ambient", new ColorRGBA(0.25f, 0.15f, 0.08f, 1f));
        trunkMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        leafMat.setBoolean("UseMaterialColors", true);
        ColorRGBA leafCol = getSeasonFoliageColor(season, Biome.FOREST);
        leafMat.setColor("Diffuse", leafCol);
        leafMat.setColor("Ambient", leafCol.mult(0.6f));
        leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        if (speciesType == 4) { // Pine
            Cylinder trunkMesh = new Cylinder(8, 12, 0.25f, 0.35f, 5.5f, true, false);
            Geometry trunkGeom = new Geometry("PineTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            trunkGeom.setLocalTranslation(0, 2.75f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            float[] tierRadii = {2.4f, 1.8f, 1.2f};
            float[] tierHeights = {3.2f, 4.6f, 6.0f};
            for (int i = 0; i < 3; i++) {
                Cylinder coneMesh = new Cylinder(10, 12, 0.05f, tierRadii[i], 1.8f, true, false);
                Geometry coneGeom = new Geometry("PineTier_" + i, coneMesh);
                coneGeom.setMaterial(leafMat);
                coneGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                coneGeom.setLocalTranslation(0, tierHeights[i], 0);
                coneGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
                treeNode.attachChild(coneGeom);
            }
        } else { // Deciduous / Oak
            Cylinder trunkMesh = new Cylinder(8, 12, 0.32f, 0.45f, 4.8f, true, false);
            Geometry trunkGeom = new Geometry("OakTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            trunkGeom.setLocalTranslation(0, 2.4f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            float[][] clusters = {
                {0.0f, 5.6f, 0.0f, 2.2f},
                {-1.1f, 4.8f, 0.8f, 1.7f},
                {1.1f, 5.0f, -0.8f, 1.7f}
            };
            for (int i = 0; i < clusters.length; i++) {
                com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(12, 12, clusters[i][3]);
                Geometry crownGeom = new Geometry("OakCluster_" + i, crownMesh);
                crownGeom.setMaterial(leafMat);
                crownGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                crownGeom.setLocalTranslation(clusters[i][0], clusters[i][1], clusters[i][2]);
                treeNode.attachChild(crownGeom);
            }
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        treeNode.setLocalScale(1.8f, 1.8f, 1.8f);
        rootNode.attachChild(treeNode);
    }

    private void createProceduralTreeScientific(float x, float y, float z, Biome biome, Random rand) {
        createProceduralTreeScientific(x, y, z, biome, rand, sampleTreeSpeciesIndex(rand));
    }

    private void createProceduralTreeScientific(float x, float y, float z, Biome biome, Random rand, int speciesIdx) {
        Node treeNode = new Node("TreeScientific");
        treeNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        ColorRGBA trunkCol = (speciesIdx == 4) ? new ColorRGBA(0.9f, 0.9f, 0.92f, 1.0f) : new ColorRGBA(0.40f, 0.25f, 0.12f, 1.0f);
        trunkMat.setColor("Diffuse", trunkCol);
        trunkMat.setColor("Ambient", trunkCol.mult(0.6f));
        trunkMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        float trunkH = (speciesIdx == 6) ? 1.2f : ((speciesIdx == 3) ? 4.5f : 6.0f);
        Cylinder trunkMesh = new Cylinder(8, 12, 0.15f, 0.15f, trunkH, true, false);
        Geometry trunkGeom = new Geometry("SciTrunk", trunkMesh);
        trunkGeom.setMaterial(trunkMat);
        trunkGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        trunkGeom.setLocalTranslation(0, trunkH * 0.5f, 0);
        trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        treeNode.attachChild(trunkGeom);

        Material dbhMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        dbhMat.setBoolean("UseMaterialColors", true);
        dbhMat.setColor("Diffuse", new ColorRGBA(0.1f, 0.8f, 1.0f, 1.0f));
        dbhMat.setColor("Ambient", new ColorRGBA(0.05f, 0.4f, 0.5f, 1.0f));
        Cylinder dbhRing = new Cylinder(8, 12, 0.22f, 0.22f, 0.10f, true, false);
        Geometry dbhGeom = new Geometry("DBHMarker", dbhRing);
        dbhGeom.setMaterial(dbhMat);
        dbhGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        dbhGeom.setLocalTranslation(0, 1.3f, 0);
        dbhGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        treeNode.attachChild(dbhGeom);

        if (speciesIdx != 6) {
            Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafMat.setBoolean("UseMaterialColors", true);
            ColorRGBA leafCol = (speciesIdx == 1) ? new ColorRGBA(0.08f, 0.45f, 0.18f, 1.0f) : new ColorRGBA(0.12f, 0.65f, 0.28f, 1.0f);
            leafMat.setColor("Diffuse", leafCol);
            leafMat.setColor("Ambient", leafCol.mult(0.6f));
            leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

            if (speciesIdx == 1) {
                // Conical LAI envelope for Conifers
                Cylinder coneMesh = new Cylinder(10, 12, 0.05f, 1.8f, 4.0f, true, false);
                Geometry coneGeom = new Geometry("SciCrownLAI", coneMesh);
                coneGeom.setMaterial(leafMat);
                coneGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                coneGeom.setLocalTranslation(0, 5.0f, 0);
                coneGeom.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
                treeNode.attachChild(coneGeom);
            } else {
                com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(12, 12, 1.8f);
                Geometry crownGeom = new Geometry("SciCrownLAI", crownMesh);
                crownGeom.setMaterial(leafMat);
                crownGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                crownGeom.setLocalTranslation(0, 5.5f, 0);
                treeNode.attachChild(crownGeom);
            }
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        treeNode.setLocalScale(2.0f, 2.0f, 2.0f);
        rootNode.attachChild(treeNode);
    }

    private Geometry createMicroVoxel(String name, float sizeX, float sizeY, float sizeZ, Material mat, float posX, float posY, float posZ) {
        Box box = new Box(sizeX * 0.5f, sizeY * 0.5f, sizeZ * 0.5f);
        Geometry g = new Geometry(name, box);
        g.setMaterial(mat);
        g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        g.setLocalTranslation(posX, posY, posZ);
        return g;
    }

    private void createProceduralTreeGamified(float x, float y, float z, Biome biome, Season season, Random rand) {
        createProceduralTreeGamified(x, y, z, biome, season, rand, sampleTreeSpeciesIndex(rand));
    }

    private void createProceduralTreeGamified(float x, float y, float z, Biome biome, Season season, Random rand, int speciesIdx) {
        Node treeNode = new Node("TreeGamified");
        treeNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material woodMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        woodMat.setBoolean("UseMaterialColors", true);
        woodMat.setColor("Diffuse", new ColorRGBA(0.42f, 0.26f, 0.12f, 1f));
        woodMat.setColor("Ambient", new ColorRGBA(0.24f, 0.15f, 0.07f, 1f));
        woodMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        if (speciesIdx == 3 || (biome == Biome.DESERT && cactusPct > 0)) {
            // Authentic Ribbed Micro-Voxel Saguaro Cactus (Scaled to 7.5m)
            Material cactusMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            cactusMat.setBoolean("UseMaterialColors", true);
            cactusMat.setColor("Diffuse", new ColorRGBA(0.24f, 0.58f, 0.22f, 1f));
            cactusMat.setColor("Ambient", new ColorRGBA(0.10f, 0.28f, 0.10f, 1f));
            cactusMat.setColor("Specular", ColorRGBA.Black);
            cactusMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            // Central Trunk (7.5m) with ribbed cross geometry
            treeNode.attachChild(createMicroVoxel("CactusCore", 1.0f, 7.5f, 1.0f, cactusMat, 0, 3.75f, 0));
            treeNode.attachChild(createMicroVoxel("CactusRibN", 0.50f, 7.0f, 0.35f, cactusMat, 0, 3.50f, 0.60f));
            treeNode.attachChild(createMicroVoxel("CactusRibS", 0.50f, 7.0f, 0.35f, cactusMat, 0, 3.50f, -0.60f));
            treeNode.attachChild(createMicroVoxel("CactusRibE", 0.35f, 7.0f, 0.50f, cactusMat, 0.60f, 3.50f, 0));
            treeNode.attachChild(createMicroVoxel("CactusRibW", 0.35f, 7.0f, 0.50f, cactusMat, -0.60f, 3.50f, 0));

            // Left Arm (Joint + Upward column)
            treeNode.attachChild(createMicroVoxel("CactusArmJointL", 1.0f, 0.70f, 0.70f, cactusMat, -0.95f, 3.8f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmUpL", 0.70f, 3.0f, 0.70f, cactusMat, -1.45f, 5.2f, 0));

            // Right Arm (Joint + Upward column at offset height)
            treeNode.attachChild(createMicroVoxel("CactusArmJointR", 1.0f, 0.70f, 0.70f, cactusMat, 0.95f, 4.6f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmUpR", 0.70f, 2.6f, 0.70f, cactusMat, 1.45f, 5.8f, 0));

        } else if (speciesIdx == 1 || biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            // Hierarchical Micro-Voxel Spruce/Pine Tree (13.5m height, 4 stepped needle tiers)
            treeNode.attachChild(createMicroVoxel("SpruceTrunk", 0.90f, 12.0f, 0.90f, woodMat, 0, 6.0f, 0));
            // Base Root Flares
            treeNode.attachChild(createMicroVoxel("RootN", 0.60f, 0.80f, 0.60f, woodMat, 0, 0.40f, 0.65f));
            treeNode.attachChild(createMicroVoxel("RootS", 0.60f, 0.80f, 0.60f, woodMat, 0, 0.40f, -0.65f));
            treeNode.attachChild(createMicroVoxel("RootE", 0.60f, 0.80f, 0.60f, woodMat, 0.65f, 0.40f, 0));
            treeNode.attachChild(createMicroVoxel("RootW", 0.60f, 0.80f, 0.60f, woodMat, -0.65f, 0.40f, 0));

            Material pineLeafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            pineLeafMat.setBoolean("UseMaterialColors", true);
            ColorRGBA pineCol = (season == Season.WINTER) ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1f) : new ColorRGBA(0.10f, 0.38f, 0.14f, 1f);
            pineLeafMat.setColor("Diffuse", pineCol);
            pineLeafMat.setColor("Ambient", pineCol.mult(0.35f));
            pineLeafMat.setColor("Specular", ColorRGBA.Black);
            pineLeafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            // Tier 1: Wide Lower Skirt (Stepped cross + overhangs, 7.0m span)
            treeNode.attachChild(createMicroVoxel("PineT1_Core", 5.0f, 1.20f, 5.0f, pineLeafMat, 0, 5.5f, 0));
            treeNode.attachChild(createMicroVoxel("PineT1_OverN", 3.0f, 0.90f, 1.20f, pineLeafMat, 0, 5.2f, 2.9f));
            treeNode.attachChild(createMicroVoxel("PineT1_OverS", 3.0f, 0.90f, 1.20f, pineLeafMat, 0, 5.2f, -2.9f));
            treeNode.attachChild(createMicroVoxel("PineT1_OverE", 1.20f, 0.90f, 3.0f, pineLeafMat, 2.9f, 5.2f, 0));
            treeNode.attachChild(createMicroVoxel("PineT1_OverW", 1.20f, 0.90f, 3.0f, pineLeafMat, -2.9f, 5.2f, 0));

            // Tier 2: Mid Skirt (5.5m span)
            treeNode.attachChild(createMicroVoxel("PineT2_Core", 3.8f, 1.20f, 3.8f, pineLeafMat, 0, 7.5f, 0));
            treeNode.attachChild(createMicroVoxel("PineT2_OverN", 2.2f, 0.80f, 0.90f, pineLeafMat, 0, 7.2f, 2.2f));
            treeNode.attachChild(createMicroVoxel("PineT2_OverS", 2.2f, 0.80f, 0.90f, pineLeafMat, 0, 7.2f, -2.2f));
            treeNode.attachChild(createMicroVoxel("PineT2_OverE", 0.90f, 0.80f, 2.2f, pineLeafMat, 2.2f, 7.2f, 0));
            treeNode.attachChild(createMicroVoxel("PineT2_OverW", 0.90f, 0.80f, 2.2f, pineLeafMat, -2.2f, 7.2f, 0));

            // Tier 3: Upper Tier (3.8m span)
            treeNode.attachChild(createMicroVoxel("PineT3_Core", 2.6f, 1.20f, 2.6f, pineLeafMat, 0, 9.5f, 0));

            // Tier 4: Spire Peak (1.8m peak)
            treeNode.attachChild(createMicroVoxel("PinePeak", 1.2f, 1.60f, 1.2f, pineLeafMat, 0, 11.5f, 0));

        } else if (speciesIdx == 5 && bambooPct > 0) {
            // Multi-culm Micro-Voxel Bamboo Stalks
            Material bambooMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            bambooMat.setBoolean("UseMaterialColors", true);
            bambooMat.setColor("Diffuse", new ColorRGBA(0.52f, 0.80f, 0.18f, 1f));
            bambooMat.setColor("Ambient", new ColorRGBA(0.30f, 0.48f, 0.08f, 1f));
            treeNode.attachChild(createMicroVoxel("Bamboo1", 0.35f, 6.0f, 0.35f, bambooMat, -0.4f, 3.0f, -0.4f));
            treeNode.attachChild(createMicroVoxel("Bamboo2", 0.35f, 7.2f, 0.35f, bambooMat, 0.3f, 3.6f, -0.2f));
            treeNode.attachChild(createMicroVoxel("Bamboo3", 0.35f, 5.5f, 0.35f, bambooMat, -0.1f, 2.75f, 0.4f));

        } else if (speciesIdx == 6) {
            // Micro-Voxel Deadwood Stump
            treeNode.attachChild(createMicroVoxel("StumpCore", 1.2f, 1.4f, 1.2f, woodMat, 0, 0.7f, 0));

        } else {
            // Authentic Majestic Micro-Voxel Oak / Deciduous Tree (Height: 12.5m, Crown: 8.5m)
            treeNode.attachChild(createMicroVoxel("OakTrunk", 1.10f, 8.5f, 1.10f, woodMat, 0, 4.25f, 0));
            // Base Root Spurs
            treeNode.attachChild(createMicroVoxel("OakRootN", 0.70f, 1.0f, 0.70f, woodMat, 0, 0.50f, 0.70f));
            treeNode.attachChild(createMicroVoxel("OakRootS", 0.70f, 1.0f, 0.70f, woodMat, 0, 0.50f, -0.70f));
            treeNode.attachChild(createMicroVoxel("OakRootE", 0.70f, 1.0f, 0.70f, woodMat, 0.70f, 0.50f, 0));
            treeNode.attachChild(createMicroVoxel("OakRootW", 0.70f, 1.0f, 0.70f, woodMat, -0.70f, 0.50f, 0));

            // Heavy Boughs / Branches
            treeNode.attachChild(createMicroVoxel("OakBranchL", 1.0f, 0.60f, 0.60f, woodMat, -0.90f, 6.0f, 0.30f));
            treeNode.attachChild(createMicroVoxel("OakBranchR", 1.0f, 0.60f, 0.60f, woodMat, 0.90f, 6.6f, -0.30f));

            ColorRGBA baseCol = getSeasonFoliageColor(season, biome);
            Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafMat.setBoolean("UseMaterialColors", true);
            leafMat.setColor("Diffuse", baseCol);
            leafMat.setColor("Ambient", baseCol.mult(0.35f));
            leafMat.setColor("Specular", ColorRGBA.Black);
            leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            Material leafHighlightMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafHighlightMat.setBoolean("UseMaterialColors", true);
            ColorRGBA highlightCol = new ColorRGBA(baseCol.r * 1.06f, baseCol.g * 1.06f, baseCol.b * 1.06f, 1.0f);
            leafHighlightMat.setColor("Diffuse", highlightCol);
            leafHighlightMat.setColor("Ambient", highlightCol.mult(0.35f));
            leafHighlightMat.setColor("Specular", ColorRGBA.Black);
            leafHighlightMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            // Layer 1: Lower Foliage Skirt (y = 5.8 - 7.2, Span: 6.5m)
            treeNode.attachChild(createMicroVoxel("OakL1_Center", 5.2f, 1.20f, 5.2f, leafMat, 0, 6.2f, 0));
            treeNode.attachChild(createMicroVoxel("OakL1_North", 3.0f, 1.0f, 1.20f, leafHighlightMat, 0, 6.0f, 2.9f));
            treeNode.attachChild(createMicroVoxel("OakL1_South", 3.0f, 1.0f, 1.20f, leafMat, 0, 6.0f, -2.9f));
            treeNode.attachChild(createMicroVoxel("OakL1_East", 1.20f, 1.0f, 3.0f, leafHighlightMat, 2.9f, 6.0f, 0));
            treeNode.attachChild(createMicroVoxel("OakL1_West", 1.20f, 1.0f, 3.0f, leafMat, -2.9f, 6.0f, 0));

            // Layer 2: Main Dense Stepped Canopy (y = 7.4 - 9.4, Span: 8.5m)
            treeNode.attachChild(createMicroVoxel("OakL2_Main", 6.8f, 1.60f, 6.8f, leafMat, 0, 8.2f, 0));
            treeNode.attachChild(createMicroVoxel("OakL2_CornerNE", 1.60f, 1.40f, 1.60f, leafHighlightMat, 2.7f, 8.2f, 2.7f));
            treeNode.attachChild(createMicroVoxel("OakL2_CornerSW", 1.60f, 1.40f, 1.60f, leafMat, -2.7f, 8.2f, -2.7f));

            // Layer 3: Upper Stepped Canopy (y = 9.4 - 11.0, Span: 5.5m)
            treeNode.attachChild(createMicroVoxel("OakL3_Core", 4.5f, 1.40f, 4.5f, leafHighlightMat, 0, 10.0f, 0));
            treeNode.attachChild(createMicroVoxel("OakL3_SideN", 2.2f, 1.0f, 1.0f, leafMat, 0, 9.8f, 2.3f));
            treeNode.attachChild(createMicroVoxel("OakL3_SideS", 2.2f, 1.0f, 1.0f, leafHighlightMat, 0, 9.8f, -2.3f));

            // Layer 4: Crown Cap (y = 11.0 - 12.5, Span: 2.8m)
            treeNode.attachChild(createMicroVoxel("OakL4_Cap", 2.8f, 1.20f, 2.8f, leafMat, 0, 11.6f, 0));
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        treeNode.setLocalScale(1.8f, 1.8f, 1.8f);
        rootNode.attachChild(treeNode);
    }

    public void update(WeatherSystem weather, float tpf) {
        if (!visible || rootNode.getChildren().isEmpty()) return;

        // Keep all trees stationary at their fixed world orientation (no sway)
        for (Spatial child : rootNode.getChildren()) {
            if (child == ambientAtmosphereNode) continue;
            if (child instanceof Node treeNode) {
                Float baseRotY = (Float) treeNode.getUserData("BaseRotY");
                if (baseRotY != null) {
                    Float tiltX = (Float) treeNode.getUserData("TiltX");
                    Float tiltZ = (Float) treeNode.getUserData("TiltZ");
                    float tx = tiltX != null ? tiltX : 0.0f;
                    float tz = tiltZ != null ? tiltZ : 0.0f;
                    treeNode.setLocalRotation(new Quaternion().fromAngles(tx, baseRotY, tz));
                }
            }
        }

        // Biological Atmospheric Micro-Fauna Simulation
        Season effectiveSeason = getEffectiveSeason();
        Biome currentBiome = Biome.forLatitude(currentLatitude);
        boolean isDay = (weather == null || weather.isDaytime());
        float temp = (weather != null) ? weather.getTemperature() : 20.0f;
        float rain = (weather != null) ? weather.getRainfall() : 0.0f;
        float windSpeedMs = (weather != null) ? weather.getWindSpeedMs() : 3.0f;
        float windAngle = (weather != null) ? (float) Math.toRadians(weather.getWindDirectionAngle()) : 0.785f;
        float windSin = FastMath.sin(windAngle);
        float windCos = FastMath.cos(windAngle);
        float windDx = windSin * (windSpeedMs * 0.45f);
        float windDz = -windCos * (windSpeedMs * 0.45f);

        // 1. Butterflies: Strictly present when flowers are blooming during Spring / Summer in the local hemisphere
        // and only during warm daytime (>= 14°C) without heavy rain
        boolean flowersInBloom = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && !activeFlowerLocations.isEmpty();
        boolean butterflyWeatherActive = isDay && temp >= 14.0f && rain < 0.20f;
        boolean butterfliesVisible = flowersInBloom && butterflyWeatherActive && currentRenderMode == RenderMode.REALISTIC;

        for (ButterflyAgent b : activeButterflies) {
            if (!butterfliesVisible) {
                b.node.setCullHint(Spatial.CullHint.Always);
            } else {
                b.node.setCullHint(Spatial.CullHint.Dynamic);
                float t = swayTime * b.flightSpeed + b.phaseOffset;
                float ox = FastMath.cos(t) * b.flightRadius + FastMath.sin(t * 2.3f) * (b.flightRadius * 0.35f);
                float oz = FastMath.sin(t * 1.15f) * b.flightRadius + FastMath.cos(t * 1.7f) * (b.flightRadius * 0.35f);
                float oy = FastMath.sin(t * 2.0f) * 0.30f + 0.45f;

                b.node.setLocalTranslation(b.anchor.x + ox, b.anchor.y + oy, b.anchor.z + oz);

                // Tangential flight orientation
                float vx = -FastMath.sin(t) * b.flightRadius;
                float vz = FastMath.cos(t * 1.15f) * b.flightRadius * 1.15f;
                float heading = FastMath.atan2(vx, vz);
                b.node.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));

                // Rapid wing flutter
                float flap = FastMath.sin(swayTime * b.flapSpeed + b.phaseOffset) * 0.85f;
                b.leftWing.setLocalRotation(new Quaternion().fromAngles(0, 0, flap));
                b.rightWing.setLocalRotation(new Quaternion().fromAngles(0, 0, -flap));
            }
        }

        // 2. Nocturnal Fireflies (Lucioles): Warm Nighttime (>= 12°C) in Spring / Summer
        boolean fireflySeasonActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER);
        boolean fireflyWeatherActive = !isDay && temp >= 12.0f && rain < 0.30f;
        boolean firefliesVisible = fireflySeasonActive && fireflyWeatherActive && currentRenderMode == RenderMode.REALISTIC;

        for (FireflyParticle fp : activeFireflies) {
            if (!firefliesVisible) {
                fp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                fp.geom.setCullHint(Spatial.CullHint.Dynamic);
                float ft = swayTime * fp.speed + fp.phase;
                float dx = FastMath.sin(ft * 0.7f) * 0.45f;
                float dy = FastMath.cos(ft * 1.1f) * 0.25f;
                float dz = FastMath.sin(ft * 0.9f) * 0.45f;
                fp.geom.setLocalTranslation(fp.basePos.x + dx, fp.basePos.y + dy, fp.basePos.z + dz);

                // Bioluminescent pulsing glow
                float pulse = 0.35f + 0.65f * FastMath.sqr(FastMath.sin(ft * 2.5f));
                Material fMat = fp.geom.getMaterial();
                if (fMat != null && fMat.getMaterialDef().getMaterialParam("Color") != null) {
                    fMat.setColor("Color", new ColorRGBA(0.68f * pulse, 0.92f * pulse, 0.22f * pulse, pulse));
                }
            }
        }

        // 3. Autumn Falling Leaves (Phénologie automnale - chute douce des feuilles mortes)
        boolean leavesActive = (effectiveSeason == Season.FALL) && currentRenderMode == RenderMode.REALISTIC;
        for (FallingLeafParticle leaf : activeFallingLeaves) {
            if (!leavesActive) {
                leaf.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                leaf.geom.setCullHint(Spatial.CullHint.Dynamic);
                leaf.currentY -= leaf.fallSpeed * tpf;
                if (leaf.currentY <= leaf.groundY + 0.05f) {
                    leaf.currentY = leaf.spawnPos.y;
                }

                float lt = swayTime * leaf.swayFrequency + leaf.phase;
                float lx = leaf.spawnPos.x + FastMath.sin(lt) * leaf.swayRadius;
                float lz = leaf.spawnPos.z + FastMath.cos(lt * 1.3f) * leaf.swayRadius;

                leaf.geom.setLocalTranslation(lx, leaf.currentY, lz);
                leaf.geom.setLocalRotation(new Quaternion().fromAngles(FastMath.sin(lt * 2.0f) * 0.6f, lt * 0.5f, FastMath.cos(lt * 1.5f) * 0.6f));
            }
        }

        // 4. Distant Soaring Birds (Oiseaux d'ambiance - rapaces planant en altitude)
        boolean birdsVisible = (effectiveSeason != Season.WINTER && isDay && rain < 0.25f && currentRenderMode == RenderMode.REALISTIC);
        for (BirdAgent bird : activeBirds) {
            if (!birdsVisible) {
                bird.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                bird.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    bird.currentAngle += bird.speed * tpf;
                }
                float bx = bird.center.x + FastMath.cos(bird.currentAngle) * bird.radius;
                float bz = bird.center.z + FastMath.sin(bird.currentAngle) * bird.radius;
                bird.geom.setLocalTranslation(bx, bird.altitude, bz);
                bird.geom.setLocalRotation(new Quaternion().fromAngles(0, -bird.currentAngle + FastMath.HALF_PI, 0.12f));
            }
        }

        // 5. Spring Aerobiological Pollen Drift (Dérive de pollen printanier)
        boolean pollenActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && isDay && rain < 0.10f && currentRenderMode == RenderMode.REALISTIC;
        for (PollenParticle p : activePollen) {
            if (!pollenActive) {
                p.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                p.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    Vector3f pos = p.geom.getLocalTranslation();
                    pos.x += (windDx * 0.25f + FastMath.sin(swayTime * p.driftFreq + p.phase) * 0.15f) * tpf;
                    pos.z += (windDz * 0.25f + FastMath.cos(swayTime * p.driftFreq + p.phase) * 0.15f) * tpf;
                    pos.y += FastMath.sin(swayTime * 0.8f + p.phase) * 0.08f * tpf;
                    if (pos.x < 1.0f) pos.x = currentGridWidth - 2.0f;
                    if (pos.x > currentGridWidth - 1.0f) pos.x = 2.0f;
                    if (pos.z < 1.0f) pos.z = currentGridHeight - 2.0f;
                    if (pos.z > currentGridHeight - 1.0f) pos.z = 2.0f;
                    p.geom.setLocalTranslation(pos);
                }
            }
        }

        // 6. Desert Dust Drift Simulation (Poussière éolienne du désert)
        boolean dustActive = (currentBiome == Biome.DESERT) && (windSpeedMs > 2.5f || temp > 30.0f) && currentRenderMode == RenderMode.REALISTIC;
        for (DesertDustParticle d : activeDesertDust) {
            if (!dustActive) {
                d.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                d.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    Vector3f pos = d.geom.getLocalTranslation();
                    pos.x += windDx * 0.65f * tpf;
                    pos.z += windDz * 0.65f * tpf;
                    pos.y += (FastMath.sin(swayTime * 2.0f + d.phase) * 0.20f) * tpf;
                    if (pos.x < 1.0f) pos.x = currentGridWidth - 2.0f;
                    if (pos.x > currentGridWidth - 1.0f) pos.x = 2.0f;
                    if (pos.z < 1.0f) pos.z = currentGridHeight - 2.0f;
                    if (pos.z > currentGridHeight - 1.0f) pos.z = 2.0f;
                    d.geom.setLocalTranslation(pos);
                }
            }
        }

        // Rain Wetness Calculation (Dynamique de mouillage et séchage réaliste)
        if (rain > 0.1f) {
            rainWetness = Math.min(1.0f, rainWetness + 0.35f * tpf);
        } else {
            float dryingRate = 0.02f + 0.05f * (Math.max(0.0f, temp) / 30.0f);
            rainWetness = Math.max(0.0f, rainWetness - dryingRate * tpf);
        }

        // 7. Water Striders (Gerridae / Patineurs d'eau): Active on water bodies in Spring/Summer/Fall
        boolean stridersActive = (effectiveSeason != Season.WINTER && rain < 0.40f && currentRenderMode == RenderMode.REALISTIC);
        for (WaterStriderAgent ws : activeWaterStriders) {
            if (!stridersActive) {
                ws.node.setCullHint(Spatial.CullHint.Always);
            } else {
                ws.node.setCullHint(Spatial.CullHint.Dynamic);
                ws.update(tpf, swayTime);
            }
        }

        // 8. Dew-covered Orb Webs (Toiles d'araignées matinales perlées de rosée avec cycle de dégradation et renouvellement)
        boolean websActive = currentRenderMode == RenderMode.REALISTIC;
        for (OrbWebAgent web : activeOrbWebs) {
            if (!websActive) {
                web.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                web.update(tpf, swayTime, rainWetness, isDay, windSpeedMs);
            }
        }

        // 9. Post-Rain Soil Steaming (Vapeur d'évaporation post-pluvieuse)
        boolean steamActive = (rainWetness > 0.15f && isDay && rain < 0.10f && temp > 10.0f && currentRenderMode == RenderMode.REALISTIC);
        for (SoilSteamParticle sp : activeSoilSteam) {
            if (!steamActive) {
                sp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                sp.geom.setCullHint(Spatial.CullHint.Dynamic);
                sp.update(tpf, windDx, windDz);
            }
        }

        // 10. Bioacoustic Stridulation Auras (Activité biophonique thermique)
        boolean stridActive = (temp > 22.0f && isDay && rain < 0.20f && (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && currentRenderMode == RenderMode.REALISTIC);
        for (StridulationAura sa : activeStridulations) {
            if (!stridActive) {
                sa.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                sa.geom.setCullHint(Spatial.CullHint.Dynamic);
                sa.update(tpf);
            }
        }

        // 11. Dynamic Mushroom Flush and Desiccation Lifecycle (Phénologie et sénescence fongique)
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (MushroomClusterAgent mc : activeMushroomClusters) {
                mc.update(tpf, rainWetness, effectiveSeason, temp);
            }
        }

        // 12. Dandelion Pappus Drift (Aigrettes de pissenlit et graines anémochores en sustentation)
        boolean pappusActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && isDay && rain < 0.10f && currentRenderMode == RenderMode.REALISTIC;
        for (DandelionPappusParticle dp : activePappus) {
            if (!pappusActive) {
                dp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                dp.geom.setCullHint(Spatial.CullHint.Dynamic);
                dp.update(tpf, windDx, windDz, swayTime);
            }
        }

        // 13. Dynamic Ephemeral Puddles (Flaques d'eau éphémères réactives à la pluie et l'évaporation)
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (DynamicPuddleAgent puddle : activePuddles) {
                puddle.update(tpf, rain, temp, swayTime);
            }
        }

        // 14. Canopy Drip Particles (Égouttement résiduel d'interception foliaire)
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (CanopyDripParticle drip : activeCanopyDrips) {
                drip.update(tpf, rainWetness);
            }
        }

        // 15. Crepuscular Gnat Swarms (Nuées de moucherons/diptères crépusculaires)
        float hour = (weather != null) ? weather.getTimeOfDay() : 19.0f;
        boolean isTwilight = (hour >= 5.5f && hour <= 7.5f) || (hour >= 18.5f && hour <= 21.0f);
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (GnatSwarmAgent swarm : activeGnatSwarms) {
                swarm.update(tpf, swayTime, isTwilight, rain, temp);
            }
        }

        // 16. Aphid Clusters & Honeydew Shimmer (Grappes de pucerons et sécrétion de miellat)
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (AphidClusterAgent aphid : activeAphidClusters) {
                aphid.update(tpf, swayTime);
            }
        }

        // 17. Extrafloral Nectary Secretions (Nectaires extra-floraux perlés)
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (ExtrafloralNectaryAgent nectary : activeNectaries) {
                nectary.update(tpf, swayTime);
            }
        }

        // 18. Parasitic Ophiocordyceps Zombie Fungi (Cadavres fongiques à stroma et halo de spores)
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (CordycepsCadaverAgent cordyceps : activeCordycepsCadavers) {
                cordyceps.update(tpf, swayTime);
            }
        }

        // 19. Desiccation Mudcracks in Severe Drought (Fentes de dessiccation par forte chaleur et sécheresse)
        boolean drought = (temp > 28.0f && rain < 0.05f && rainWetness < 0.05f && currentRenderMode == RenderMode.REALISTIC);
        for (Geometry mc : activeMudcracks) {
            mc.setCullHint(drought ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
        }
    }

    private void rebuildAtmosphericFauna(Terrarium terrarium, Biome biome, Season effectiveSeason, Random rand) {
        if (currentRenderMode != RenderMode.REALISTIC || terrarium == null) return;

        // 1. Butterflies: Spawned strictly over active blooming flower clusters in Spring / Summer (hemisphere)
        boolean flowersInBloom = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && !activeFlowerLocations.isEmpty();
        if (flowersInBloom) {
            int butterflyCount = Math.min(16, Math.max(2, activeFlowerLocations.size() * 2));
            for (int i = 0; i < butterflyCount; i++) {
                Vector3f flowerPos = activeFlowerLocations.get(i % activeFlowerLocations.size());
                ButterflyAgent b = createButterfly(flowerPos, rand, i);
                activeButterflies.add(b);
                ambientAtmosphereNode.attachChild(b.node);
            }
        }

        // 2. Fireflies (Lucioles): Spawned for warm Spring/Summer nights in vegetative biomes
        if ((effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && biome != Biome.DESERT && biome != Biome.ALPINE_SNOW) {
            int fireflyCount = 20;
            for (int i = 0; i < fireflyCount; i++) {
                float fx = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
                float fz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
                float felev = terrarium.getSurfaceElevation(fx, fz);
                FireflyParticle fp = createFirefly(new Vector3f(fx, felev + 0.35f + rand.nextFloat() * 1.2f, fz), rand);
                activeFireflies.add(fp);
                ambientAtmosphereNode.attachChild(fp.geom);
            }
        }

        // 3. Autumn Falling Leaves: Spawned in Fall for temperate forest biomes
        if (effectiveSeason == Season.FALL && biome != Biome.DESERT && biome != Biome.ALPINE_SNOW) {
            int leafCount = 28;
            for (int i = 0; i < leafCount; i++) {
                float lx = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
                float lz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
                float lelev = terrarium.getSurfaceElevation(lx, lz);
                FallingLeafParticle leaf = createFallingLeaf(new Vector3f(lx, lelev + 4.0f + rand.nextFloat() * 9.0f, lz), lelev, rand);
                activeFallingLeaves.add(leaf);
                ambientAtmosphereNode.attachChild(leaf.geom);
            }
        }

        // 4. Distant Soaring Birds (Oiseaux d'ambiance - rapaces thermiques)
        if (biome != Biome.ALPINE_SNOW && effectiveSeason != Season.WINTER) {
            int birdCount = 3 + rand.nextInt(2);
            Vector3f center = new Vector3f(currentGridWidth * 0.5f, 0, currentGridHeight * 0.5f);
            for (int i = 0; i < birdCount; i++) {
                BirdAgent bird = createBird(center, rand, i);
                activeBirds.add(bird);
                ambientAtmosphereNode.attachChild(bird.geom);
            }
        }

        // 5. Spring Aerobiological Pollen Drift (Dérive de pollen printanier)
        if ((effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && (biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.WETLAND)) {
            int pollenCount = 30;
            for (int i = 0; i < pollenCount; i++) {
                float px = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
                float pz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
                float pelev = terrarium.getSurfaceElevation(px, pz);
                PollenParticle p = createPollen(new Vector3f(px, pelev + 0.8f + rand.nextFloat() * 6.0f, pz), pelev, rand);
                activePollen.add(p);
                ambientAtmosphereNode.attachChild(p.geom);
            }
        }

        // 6. Desert Dust Drift (Poussière éolienne du désert)
        if (biome == Biome.DESERT) {
            int dustCount = 25;
            for (int i = 0; i < dustCount; i++) {
                float dx = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
                float dz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
                float delev = terrarium.getSurfaceElevation(dx, dz);
                DesertDustParticle d = createDesertDust(new Vector3f(dx, delev + 0.2f + rand.nextFloat() * 2.5f, dz), delev, rand);
                activeDesertDust.add(d);
                ambientAtmosphereNode.attachChild(d.geom);
            }
        }

        // 7. Water Striders (Gerridae / Patineurs d'eau sur les surfaces aquatiques)
        List<Vector3f> waterSpots = new ArrayList<>();
        int step = Math.max(2, currentGridWidth / 20);
        for (int x = 1; x < currentGridWidth - 1; x += step) {
            for (int z = 1; z < currentGridHeight - 1; z += step) {
                float elev = terrarium.getSurfaceElevation(x, z);
                int ix = Math.max(0, Math.min(terrarium.getWidth() - 1, x));
                int iz = Math.max(0, Math.min(terrarium.getHeight() - 1, z));
                int iy = Math.max(0, Math.min(terrarium.getDepth() - 1, Math.round(elev)));
                TerrariumCell cell = terrarium.getCell(ix, iz, iy);
                if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                    waterSpots.add(new Vector3f(x, elev + 0.02f, z));
                }
            }
        }
        if (!waterSpots.isEmpty()) {
            int striderCount = Math.min(6, Math.max(2, waterSpots.size()));
            for (int i = 0; i < striderCount; i++) {
                Vector3f spawnPos = waterSpots.get(rand.nextInt(waterSpots.size()));
                WaterStriderAgent ws = createWaterStrider(spawnPos, rand, i);
                activeWaterStriders.add(ws);
                ambientAtmosphereNode.attachChild(ws.node);
            }
        }

        // 8. Dew-covered Orb Webs (Toiles d'araignées perlées de rosée dans les branchages)
        if (biome == Biome.FOREST || biome == Biome.GRASSLAND || biome == Biome.WETLAND) {
            int webCount = 4 + rand.nextInt(3);
            for (int i = 0; i < webCount; i++) {
                float wx = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
                float wz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
                float welev = terrarium.getSurfaceElevation(wx, wz);
                OrbWebAgent web = createOrbWeb(new Vector3f(wx, welev + 0.6f + rand.nextFloat() * 1.8f, wz), rand, i);
                activeOrbWebs.add(web);
                ambientAtmosphereNode.attachChild(web.geom);
            }
        }

        // 9. Post-Rain Soil Steaming (Particules de vapeur d'eau tellurique)
        int steamCount = 20;
        for (int i = 0; i < steamCount; i++) {
            float sx = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
            float sz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
            float selev = terrarium.getSurfaceElevation(sx, sz);
            SoilSteamParticle sp = createSoilSteam(new Vector3f(sx, selev + 0.05f, sz), rand);
            activeSoilSteam.add(sp);
            ambientAtmosphereNode.attachChild(sp.geom);
        }


        // 11. Dandelion Pappus Drift (Aigrettes de pissenlit et graines anémochores en sustentation)
        if ((effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && (biome == Biome.GRASSLAND || biome == Biome.FOREST || biome == Biome.WETLAND)) {
            int pappusCount = 18;
            for (int i = 0; i < pappusCount; i++) {
                float px = 2.0f + rand.nextFloat() * (currentGridWidth - 4.0f);
                float pz = 2.0f + rand.nextFloat() * (currentGridHeight - 4.0f);
                float pelev = terrarium.getSurfaceElevation(px, pz);
                DandelionPappusParticle dp = createDandelionPappus(new Vector3f(px, pelev + 0.3f + rand.nextFloat() * 2.5f, pz), pelev, rand);
                activePappus.add(dp);
                ambientAtmosphereNode.attachChild(dp.geom);
            }
        }

        // 13. Canopy Drip Particles (Interception foliaire et égouttement post-pluie sous la canopée)
        if (!activeCanopyLocations.isEmpty()) {
            int dripCount = Math.min(24, activeCanopyLocations.size() * 3);
            for (int i = 0; i < dripCount; i++) {
                Vector3f canopyPos = activeCanopyLocations.get(i % activeCanopyLocations.size());
                float groundY = terrarium.getSurfaceElevation(canopyPos.x, canopyPos.z);
                CanopyDripParticle drip = createCanopyDrip(canopyPos, groundY, rand.nextFloat() * 2.0f);
                activeCanopyDrips.add(drip);
                ambientAtmosphereNode.attachChild(drip.geom);
            }
        }

        // 14. Crepuscular Gnat Swarms (Nuées de moucherons dansant au-dessus des buissons et points d'eau)
        int gnatCount = 4;
        for (int i = 0; i < gnatCount; i++) {
            float gx = 4.0f + rand.nextFloat() * (currentGridWidth - 8.0f);
            float gz = 4.0f + rand.nextFloat() * (currentGridHeight - 8.0f);
            float gelev = terrarium.getSurfaceElevation(gx, gz);
            GnatSwarmAgent gnatSwarm = createGnatSwarm(new Vector3f(gx, gelev + 1.2f + rand.nextFloat() * 0.8f, gz), 0.65f, rand, i);
            activeGnatSwarms.add(gnatSwarm);
            ambientAtmosphereNode.attachChild(gnatSwarm.swarmNode);
        }

        // 15. Trophobiotic Aphid Herds on Plant Stems (Grappes de pucerons avec perles de miellat)
        int aphidClusterCount = 5;
        for (int i = 0; i < aphidClusterCount; i++) {
            float ax = 3.0f + rand.nextFloat() * (currentGridWidth - 6.0f);
            float az = 3.0f + rand.nextFloat() * (currentGridHeight - 6.0f);
            float aelev = terrarium.getSurfaceElevation(ax, az);
            AphidClusterAgent aphidCluster = createAphidCluster(new Vector3f(ax, aelev + 0.45f + rand.nextFloat() * 0.5f, az), rand, i);
            activeAphidClusters.add(aphidCluster);
            ambientAtmosphereNode.attachChild(aphidCluster.node);
        }

        // 16. Extrafloral Nectary Beads (Nectaires extra-floraux perlés sur les tiges de buissons)
        int nectaryCount = 8;
        for (int i = 0; i < nectaryCount; i++) {
            float nx = 3.0f + rand.nextFloat() * (currentGridWidth - 6.0f);
            float nz = 3.0f + rand.nextFloat() * (currentGridHeight - 6.0f);
            float nelev = terrarium.getSurfaceElevation(nx, nz);
            ExtrafloralNectaryAgent nectary = createExtrafloralNectary(new Vector3f(nx, nelev + 0.35f + rand.nextFloat() * 0.4f, nz), rand);
            activeNectaries.add(nectary);
            ambientAtmosphereNode.attachChild(nectary.geom);
        }

        // 17. Parasitic Ophiocordyceps Zombie Cadavers (Cadavres de fourmis zombies fixés sous les feuilles)
        int cordycepsCount = 3;
        for (int i = 0; i < cordycepsCount; i++) {
            float cx = 4.0f + rand.nextFloat() * (currentGridWidth - 8.0f);
            float cz = 4.0f + rand.nextFloat() * (currentGridHeight - 8.0f);
            float celev = terrarium.getSurfaceElevation(cx, cz);
            CordycepsCadaverAgent cordyceps = createCordycepsCadaver(new Vector3f(cx, celev + 1.2f + rand.nextFloat() * 0.8f, cz), rand, i);
            activeCordycepsCadavers.add(cordyceps);
            ambientAtmosphereNode.attachChild(cordyceps.node);
        }

        // 18. Desiccation Mudcracks Grid (Craquelures de sol argileux en période de sécheresse)
        int crackPatches = 6;
        for (int i = 0; i < crackPatches; i++) {
            float mx = 3.0f + rand.nextFloat() * (currentGridWidth - 6.0f);
            float mz = 3.0f + rand.nextFloat() * (currentGridHeight - 6.0f);
            float melev = terrarium.getSurfaceElevation(mx, mz);
            Geometry crack = createMudcrackPatch(new Vector3f(mx, melev + 0.02f, mz), 0.75f + rand.nextFloat() * 0.5f, rand, i);
            activeMudcracks.add(crack);
            ambientAtmosphereNode.attachChild(crack);
        }
    }

    private FallingLeafParticle createFallingLeaf(Vector3f spawnPos, float groundY, Random rand) {
        ColorRGBA leafCol = switch (rand.nextInt(4)) {
            case 0 -> new ColorRGBA(0.92f, 0.58f, 0.12f, 0.95f); // Amber
            case 1 -> new ColorRGBA(0.88f, 0.28f, 0.12f, 0.95f); // Crimson
            case 2 -> new ColorRGBA(0.92f, 0.72f, 0.10f, 0.95f); // Golden
            default -> new ColorRGBA(0.65f, 0.22f, 0.10f, 0.95f); // Burnt Rust
        };

        Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        leafMat.setColor("Color", leafCol);
        leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Geometry geom = new Geometry("FallingLeaf", new Box(0.038f, 0.002f, 0.032f));
        geom.setMaterial(leafMat);
        geom.setLocalTranslation(spawnPos);

        float fallSpeed = 0.50f + rand.nextFloat() * 0.45f;
        float swayFrequency = 1.5f + rand.nextFloat() * 0.8f;
        float swayRadius = 0.40f + rand.nextFloat() * 0.45f;
        float phase = rand.nextFloat() * FastMath.TWO_PI;

        return new FallingLeafParticle(geom, spawnPos, groundY, fallSpeed, swayFrequency, swayRadius, phase);
    }

    private ButterflyAgent createButterfly(Vector3f flowerPos, Random rand, int index) {
        Node bNode = new Node("Butterfly_" + index);
        bNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // Species Wing Colors
        ColorRGBA wingCol = switch (rand.nextInt(4)) {
            case 0 -> new ColorRGBA(0.98f, 0.86f, 0.16f, 1.0f); // Papilio machaon (Yellow)
            case 1 -> new ColorRGBA(0.95f, 0.46f, 0.10f, 1.0f); // Danaus plexippus (Monarch Orange)
            case 2 -> new ColorRGBA(0.22f, 0.68f, 0.98f, 1.0f); // Polyommatus icarus (Blue Morpho)
            default -> new ColorRGBA(0.95f, 0.95f, 0.98f, 1.0f); // Pieris rapae (Meadow White)
        };

        Material wingMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wingMat.setColor("Color", wingCol);
        wingMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Material bodyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bodyMat.setColor("Color", new ColorRGBA(0.12f, 0.12f, 0.14f, 1.0f));

        // Thorax
        Geometry body = new Geometry("Body", new Box(0.008f, 0.008f, 0.035f));
        body.setMaterial(bodyMat);
        bNode.attachChild(body);

        // Left Wing (Pivoted at joint)
        Node leftWingPivot = new Node("LeftWingPivot");
        Geometry leftWingGeom = new Geometry("LeftWing", new Box(0.040f, 0.002f, 0.028f));
        leftWingGeom.setMaterial(wingMat);
        leftWingGeom.setLocalTranslation(-0.040f, 0, 0);
        leftWingPivot.attachChild(leftWingGeom);
        bNode.attachChild(leftWingPivot);

        // Right Wing (Pivoted at joint)
        Node rightWingPivot = new Node("RightWingPivot");
        Geometry rightWingGeom = new Geometry("RightWing", new Box(0.040f, 0.002f, 0.028f));
        rightWingGeom.setMaterial(wingMat);
        rightWingGeom.setLocalTranslation(0.040f, 0, 0);
        rightWingPivot.attachChild(rightWingGeom);
        bNode.attachChild(rightWingPivot);

        float flightSpeed = 1.1f + rand.nextFloat() * 0.6f;
        float flightRadius = 0.60f + rand.nextFloat() * 0.75f;
        float phaseOffset = rand.nextFloat() * FastMath.TWO_PI;
        float flapSpeed = 20.0f + rand.nextFloat() * 8.0f;

        return new ButterflyAgent(bNode, leftWingPivot, rightWingPivot, flowerPos, flightSpeed, flightRadius, phaseOffset, flapSpeed);
    }

    private FireflyParticle createFirefly(Vector3f basePos, Random rand) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.68f, 0.92f, 0.22f, 0.9f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("Firefly", new Box(0.025f, 0.025f, 0.025f));
        geom.setMaterial(mat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(basePos);

        float speed = 0.8f + rand.nextFloat() * 0.5f;
        float phase = rand.nextFloat() * FastMath.TWO_PI;

        return new FireflyParticle(geom, basePos, speed, phase);
    }

    private static class FallingLeafParticle {
        final Geometry geom;
        final Vector3f spawnPos;
        final float groundY;
        float currentY;
        final float fallSpeed;
        final float swayFrequency;
        final float swayRadius;
        final float phase;

        FallingLeafParticle(Geometry geom, Vector3f spawnPos, float groundY, float fallSpeed, float swayFrequency, float swayRadius, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.currentY = spawnPos.y;
            this.fallSpeed = fallSpeed;
            this.swayFrequency = swayFrequency;
            this.swayRadius = swayRadius;
            this.phase = phase;
        }
    }

    private static class ButterflyAgent {
        final Node node;
        final Spatial leftWing;
        final Spatial rightWing;
        final Vector3f anchor;
        final float flightSpeed;
        final float flightRadius;
        final float phaseOffset;
        final float flapSpeed;

        ButterflyAgent(Node node, Spatial leftWing, Spatial rightWing, Vector3f anchor, float flightSpeed, float flightRadius, float phaseOffset, float flapSpeed) {
            this.node = node;
            this.leftWing = leftWing;
            this.rightWing = rightWing;
            this.anchor = anchor;
            this.flightSpeed = flightSpeed;
            this.flightRadius = flightRadius;
            this.phaseOffset = phaseOffset;
            this.flapSpeed = flapSpeed;
        }
    }

    private static class FireflyParticle {
        final Geometry geom;
        final Vector3f basePos;
        final float speed;
        final float phase;

        FireflyParticle(Geometry geom, Vector3f basePos, float speed, float phase) {
            this.geom = geom;
            this.basePos = basePos;
            this.speed = speed;
            this.phase = phase;
        }
    }

    private BirdAgent createBird(Vector3f center, Random rand, int index) {
        Material birdMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        birdMat.setColor("Color", new ColorRGBA(0.12f, 0.12f, 0.15f, 0.85f));
        birdMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        birdMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        // V-shaped wing silhouette for high altitude soaring
        Box wingBox = new Box(0.45f, 0.02f, 0.12f);
        Geometry geom = new Geometry("SoaringBird_" + index, wingBox);
        geom.setMaterial(birdMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);

        float maxDim = Math.min(currentGridWidth, currentGridHeight);
        float orbitRadius = maxDim * 0.22f + rand.nextFloat() * (maxDim * 0.15f);
        float speed = 0.25f + rand.nextFloat() * 0.15f;
        float altitude = 22.0f + rand.nextFloat() * 8.0f;
        float startAngle = rand.nextFloat() * FastMath.TWO_PI;

        return new BirdAgent(geom, center, orbitRadius, speed, altitude, startAngle);
    }

    private static class BirdAgent {
        final Geometry geom;
        final Vector3f center;
        final float radius;
        final float speed;
        final float altitude;
        float currentAngle;

        BirdAgent(Geometry geom, Vector3f center, float radius, float speed, float altitude, float startAngle) {
            this.geom = geom;
            this.center = center;
            this.radius = radius;
            this.speed = speed;
            this.altitude = altitude;
            this.currentAngle = startAngle;
        }
    }

    private PollenParticle createPollen(Vector3f spawnPos, float groundY, Random rand) {
        Material pollenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA col = (rand.nextBoolean()) ? new ColorRGBA(0.96f, 0.92f, 0.28f, 0.75f) : new ColorRGBA(0.85f, 0.95f, 0.35f, 0.70f);
        pollenMat.setColor("Color", col);
        pollenMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("PollenGrain", new Box(0.018f, 0.018f, 0.018f));
        geom.setMaterial(pollenMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(spawnPos);

        float driftFreq = 0.8f + rand.nextFloat() * 0.6f;
        float phase = rand.nextFloat() * FastMath.TWO_PI;

        return new PollenParticle(geom, spawnPos, groundY, driftFreq, phase);
    }

    private DesertDustParticle createDesertDust(Vector3f spawnPos, float groundY, Random rand) {
        Material dustMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dustMat.setColor("Color", new ColorRGBA(0.92f, 0.78f, 0.45f, 0.55f));
        dustMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("DesertDust", new Box(0.022f, 0.022f, 0.022f));
        geom.setMaterial(dustMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(spawnPos);

        float phase = rand.nextFloat() * FastMath.TWO_PI;

        return new DesertDustParticle(geom, spawnPos, groundY, phase);
    }

    private static class PollenParticle {
        final Geometry geom;
        final Vector3f spawnPos;
        final float groundY;
        final float driftFreq;
        final float phase;

        PollenParticle(Geometry geom, Vector3f spawnPos, float groundY, float driftFreq, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.driftFreq = driftFreq;
            this.phase = phase;
        }
    }

    private static class DesertDustParticle {
        final Geometry geom;
        final Vector3f spawnPos;
        final float groundY;
        final float phase;

        DesertDustParticle(Geometry geom, Vector3f spawnPos, float groundY, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.phase = phase;
        }
    }

    private WaterStriderAgent createWaterStrider(Vector3f spawnPos, Random rand, int index) {
        Node striderNode = new Node("WaterStrider_" + index);
        striderNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        Material bodyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bodyMat.setColor("Color", new ColorRGBA(0.12f, 0.10f, 0.08f, 1.0f));

        Material legMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        legMat.setColor("Color", new ColorRGBA(0.18f, 0.15f, 0.12f, 0.95f));

        // Elongated body
        Geometry body = new Geometry("StriderBody", new Box(0.012f, 0.005f, 0.035f));
        body.setMaterial(bodyMat);
        striderNode.attachChild(body);

        // 6 Slender hydrophobic legs
        float[] legAngles = {-2.3f, -1.57f, -0.8f, 2.3f, 1.57f, 0.8f};
        float[] legLengths = {0.07f, 0.09f, 0.06f, 0.07f, 0.09f, 0.06f};
        for (int i = 0; i < 6; i++) {
            Geometry leg = new Geometry("StriderLeg_" + i, new Cylinder(4, 8, 0.002f, legLengths[i], true));
            leg.setMaterial(legMat);
            leg.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, legAngles[i], 0));
            leg.setLocalTranslation(FastMath.cos(legAngles[i]) * 0.03f, 0, FastMath.sin(legAngles[i]) * 0.03f);
            striderNode.attachChild(leg);
        }

        striderNode.setLocalTranslation(spawnPos);
        return new WaterStriderAgent(striderNode, spawnPos, currentGridWidth, currentGridHeight, rand);
    }

    private OrbWebAgent createOrbWeb(Vector3f pos, Random rand, int index) {
        Material webMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        webMat.setColor("Color", new ColorRGBA(0.92f, 0.95f, 0.98f, 0.55f));
        webMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        webMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        float size = 0.45f + rand.nextFloat() * 0.35f;
        Geometry geom = new Geometry("OrbWeb_" + index, new Quad(size, size));
        geom.setMaterial(webMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(pos.x - size * 0.5f, pos.y - size * 0.5f, pos.z);
        geom.setLocalRotation(new Quaternion().fromAngles(rand.nextFloat() * 0.3f, rand.nextFloat() * FastMath.TWO_PI, 0));

        float lifespan = 35.0f + rand.nextFloat() * 45.0f;
        return new OrbWebAgent(geom, pos, size, rand.nextFloat() * FastMath.TWO_PI, lifespan, currentGridWidth, currentGridHeight, activeTerrarium);
    }

    private DandelionPappusParticle createDandelionPappus(Vector3f spawnPos, float groundY, Random rand) {
        Material papMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        papMat.setColor("Color", new ColorRGBA(0.96f, 0.98f, 1.0f, 0.75f));
        papMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        papMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Geometry geom = new Geometry("DandelionPappus", new Box(0.015f, 0.015f, 0.015f));
        geom.setMaterial(papMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(spawnPos);

        float driftFreq = 0.7f + rand.nextFloat() * 0.5f;
        float phase = rand.nextFloat() * FastMath.TWO_PI;
        float floatSpeed = 0.15f + rand.nextFloat() * 0.20f;

        return new DandelionPappusParticle(geom, spawnPos, groundY, driftFreq, phase, floatSpeed);
    }

    private SoilSteamParticle createSoilSteam(Vector3f spawnPos, Random rand) {
        Material steamMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        steamMat.setColor("Color", new ColorRGBA(0.95f, 0.97f, 1.0f, 0.18f));
        steamMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("SoilSteam", new Box(0.045f, 0.045f, 0.045f));
        geom.setMaterial(steamMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(spawnPos);

        float riseSpeed = 0.20f + rand.nextFloat() * 0.25f;
        float maxAlt = spawnPos.y + 1.2f + rand.nextFloat() * 1.5f;

        return new SoilSteamParticle(geom, spawnPos, maxAlt, riseSpeed, rand.nextFloat() * FastMath.TWO_PI);
    }

    private StridulationAura createStridulationAura(Vector3f centerPos, Random rand) {
        Material auraMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        auraMat.setColor("Color", new ColorRGBA(0.40f, 0.85f, 0.95f, 0.25f));
        auraMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        auraMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Geometry geom = new Geometry("StridulationRing", new Cylinder(16, 16, 0.05f, 0.02f, false));
        geom.setMaterial(auraMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(centerPos);
        geom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));

        return new StridulationAura(geom, centerPos, 0.8f + rand.nextFloat() * 0.4f, 1.8f);
    }

    private static class WaterStriderAgent {
        final Node node;
        final Vector3f currentPos;
        final float waterY;
        final int gridW, gridH;
        float burstTimer;
        float burstInterval;
        float vx, vz;
        float heading;

        WaterStriderAgent(Node node, Vector3f spawnPos, int gridW, int gridH, Random rand) {
            this.node = node;
            this.currentPos = new Vector3f(spawnPos);
            this.waterY = spawnPos.y;
            this.gridW = gridW;
            this.gridH = gridH;
            this.burstTimer = rand.nextFloat() * 2.0f;
            this.burstInterval = 1.2f + rand.nextFloat() * 2.0f;
            this.heading = rand.nextFloat() * FastMath.TWO_PI;
        }

        void update(float tpf, float swayTime) {
            burstTimer += tpf;
            if (burstTimer >= burstInterval) {
                burstTimer = 0.0f;
                heading += (FastMath.nextRandomFloat() - 0.5f) * 1.8f;
                float speed = 0.8f + FastMath.nextRandomFloat() * 0.7f;
                vx = FastMath.sin(heading) * speed;
                vz = FastMath.cos(heading) * speed;
            }

            // Friction deceleration on surface film
            vx *= (1.0f - 2.8f * tpf);
            vz *= (1.0f - 2.8f * tpf);

            currentPos.x += vx * tpf;
            currentPos.z += vz * tpf;

            // Soft boundary bounce
            if (currentPos.x < 1.5f || currentPos.x > gridW - 1.5f) {
                vx = -vx;
                heading = FastMath.atan2(vx, vz);
            }
            if (currentPos.z < 1.5f || currentPos.z > gridH - 1.5f) {
                vz = -vz;
                heading = FastMath.atan2(vx, vz);
            }

            node.setLocalTranslation(currentPos.x, waterY + FastMath.sin(swayTime * 2.5f) * 0.005f, currentPos.z);
            node.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));
        }
    }

    private static class OrbWebAgent {
        final Geometry geom;
        final Vector3f basePos;
        final float size;
        final float phase;
        final float maxLifespan;
        float currentAge;
        float turnoverTimer;
        boolean isDestroyed;
        final int gridW, gridH;
        final Terrarium terrarium;

        OrbWebAgent(Geometry geom, Vector3f pos, float size, float phase, float maxLifespan, int gridW, int gridH, Terrarium terrarium) {
            this.geom = geom;
            this.basePos = new Vector3f(pos);
            this.size = size;
            this.phase = phase;
            this.maxLifespan = maxLifespan;
            this.currentAge = 0.0f;
            this.turnoverTimer = 0.0f;
            this.isDestroyed = false;
            this.gridW = gridW;
            this.gridH = gridH;
            this.terrarium = terrarium;
        }

        void update(float tpf, float swayTime, float rainWetness, boolean isDay, float windSpeed) {
            if (!isDestroyed) {
                currentAge += tpf;
                if (currentAge >= maxLifespan || windSpeed > 8.0f) {
                    isDestroyed = true;
                    turnoverTimer = 8.0f + FastMath.nextRandomFloat() * 12.0f;
                }
            } else {
                turnoverTimer -= tpf;
                if (turnoverTimer <= 0.0f) {
                    isDestroyed = false;
                    currentAge = 0.0f;
                    float newX = 2.0f + FastMath.nextRandomFloat() * (gridW - 4.0f);
                    float newZ = 2.0f + FastMath.nextRandomFloat() * (gridH - 4.0f);
                    float newY = (terrarium != null) ? (terrarium.getSurfaceElevation(newX, newZ) + 0.6f + FastMath.nextRandomFloat() * 1.8f) : basePos.y;
                    basePos.set(newX, newY, newZ);
                    geom.setLocalTranslation(newX - size * 0.5f, newY - size * 0.5f, newZ);
                }
            }

            if (isDestroyed) {
                geom.setCullHint(Spatial.CullHint.Always);
                return;
            }

            geom.setCullHint(Spatial.CullHint.Dynamic);
            float degradation = 1.0f - (currentAge / maxLifespan) * 0.6f;
            float dewShimmer = (0.25f + 0.50f * rainWetness + 0.25f * FastMath.sqr(FastMath.sin(swayTime * 1.5f + phase))) * degradation;
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                mat.setColor("Color", new ColorRGBA(0.92f, 0.96f, 1.0f, Math.min(0.85f, dewShimmer)));
            }
        }
    }

    private static class DandelionPappusParticle {
        final Geometry geom;
        final Vector3f spawnPos;
        final float groundY;
        final float driftFreq;
        final float phase;
        final float floatSpeed;

        DandelionPappusParticle(Geometry geom, Vector3f spawnPos, float groundY, float driftFreq, float phase, float floatSpeed) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.driftFreq = driftFreq;
            this.phase = phase;
            this.floatSpeed = floatSpeed;
        }

        void update(float tpf, float windDx, float windDz, float swayTime) {
            if (tpf <= 0.0001f) return;
            Vector3f pos = geom.getLocalTranslation();
            pos.x += (windDx * 0.35f + FastMath.sin(swayTime * driftFreq + phase) * 0.12f) * tpf;
            pos.z += (windDz * 0.35f + FastMath.cos(swayTime * driftFreq + phase) * 0.12f) * tpf;
            pos.y += (floatSpeed + FastMath.sin(swayTime * 1.2f + phase) * 0.08f) * tpf;
            if (pos.y > groundY + 6.0f) {
                pos.y = groundY + 0.3f;
            }
            geom.setLocalTranslation(pos);
        }
    }

    private static class MushroomClusterAgent {
        final List<Spatial> mushroomInstances;
        final float baseX, baseZ;
        float flushMoisture;
        float currentScale;
        boolean visible;

        MushroomClusterAgent(List<Spatial> instances, float x, float z) {
            this.mushroomInstances = instances;
            this.baseX = x;
            this.baseZ = z;
            this.flushMoisture = 0.8f;
            this.currentScale = 1.0f;
            this.visible = true;
        }

        void update(float tpf, float rainWetness, Season season, float temp) {
            if (rainWetness > 0.25f) {
                flushMoisture = Math.min(1.0f, flushMoisture + 0.20f * tpf);
            } else {
                float desiccationRate = 0.010f + 0.025f * (Math.max(0.0f, temp) / 30.0f);
                if (season == Season.WINTER || temp < 2.0f) desiccationRate = 0.06f;
                flushMoisture = Math.max(0.0f, flushMoisture - desiccationRate * tpf);
            }

            float targetScale = (season == Season.WINTER || flushMoisture < 0.10f) ? 0.0f : (0.25f + 0.75f * flushMoisture);
            currentScale += (targetScale - currentScale) * Math.min(1.0f, 2.5f * tpf);

            if (currentScale < 0.05f) {
                if (visible) {
                    for (Spatial s : mushroomInstances) s.setCullHint(Spatial.CullHint.Always);
                    visible = false;
                }
            } else {
                if (!visible) {
                    for (Spatial s : mushroomInstances) s.setCullHint(Spatial.CullHint.Dynamic);
                    visible = true;
                }
                for (Spatial s : mushroomInstances) {
                    Float origScale = (Float) s.getUserData("OriginalScaleY");
                    if (origScale != null) {
                        s.setLocalScale(s.getLocalScale().x, origScale * currentScale, s.getLocalScale().z);
                    }
                }
            }
        }
    }

    private static class SoilSteamParticle {
        final Geometry geom;
        final Vector3f spawnPos;
        final float maxAltitude;
        final float riseSpeed;
        final float phase;
        float currentY;

        SoilSteamParticle(Geometry geom, Vector3f spawnPos, float maxAltitude, float riseSpeed, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.maxAltitude = maxAltitude;
            this.riseSpeed = riseSpeed;
            this.phase = phase;
            this.currentY = spawnPos.y;
        }

        void update(float tpf, float windDx, float windDz) {
            currentY += riseSpeed * tpf;
            if (currentY >= maxAltitude) {
                currentY = spawnPos.y;
            }
            float progress = (currentY - spawnPos.y) / (maxAltitude - spawnPos.y);
            float alpha = (1.0f - progress) * 0.22f;

            Vector3f pos = geom.getLocalTranslation();
            pos.y = currentY;
            pos.x = spawnPos.x + windDx * progress * 0.8f;
            pos.z = spawnPos.z + windDz * progress * 0.8f;
            geom.setLocalTranslation(pos);

            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                mat.setColor("Color", new ColorRGBA(0.95f, 0.98f, 1.0f, alpha));
            }
        }
    }

    private static class StridulationAura {
        final Geometry geom;
        final Vector3f centerPos;
        final float expansionSpeed;
        final float maxRadius;
        float currentRadius = 0.1f;

        StridulationAura(Geometry geom, Vector3f centerPos, float expansionSpeed, float maxRadius) {
            this.geom = geom;
            this.centerPos = centerPos;
            this.expansionSpeed = expansionSpeed;
            this.maxRadius = maxRadius;
        }

        void update(float tpf) {
            currentRadius += expansionSpeed * tpf;
            if (currentRadius >= maxRadius) {
                currentRadius = 0.1f;
            }
            float progress = currentRadius / maxRadius;
            float alpha = (1.0f - progress) * 0.35f;

            geom.setLocalScale(currentRadius, currentRadius, 1.0f);
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                mat.setColor("Color", new ColorRGBA(0.40f, 0.85f, 0.95f, alpha));
            }
        }
    }

    private DynamicPuddleAgent createDynamicPuddle(Vector3f center, float maxRadius) {
        Material waterMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        waterMat.setColor("Color", new ColorRGBA(0.22f, 0.48f, 0.72f, 0.70f));
        waterMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("DynamicPuddle", new Cylinder(16, 16, maxRadius, 0.015f, true));
        geom.setMaterial(waterMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
        geom.setLocalTranslation(center);

        return new DynamicPuddleAgent(geom, center, maxRadius);
    }

    private static class DynamicPuddleAgent {
        final Geometry geom;
        final Vector3f center;
        final float maxRadius;
        float currentRadius = 0.05f;

        DynamicPuddleAgent(Geometry geom, Vector3f center, float maxRadius) {
            this.geom = geom;
            this.center = center;
            this.maxRadius = maxRadius;
        }

        void update(float tpf, float rain, float temp, float swayTime) {
            if (rain > 0.05f) {
                currentRadius = Math.min(maxRadius, currentRadius + 0.35f * rain * tpf);
            } else {
                float evapRate = 0.012f + 0.030f * (Math.max(0.0f, temp) / 30.0f);
                currentRadius = Math.max(0.0f, currentRadius - evapRate * tpf);
            }

            if (currentRadius <= 0.02f) {
                geom.setCullHint(Spatial.CullHint.Always);
            } else {
                geom.setCullHint(Spatial.CullHint.Dynamic);
                float ripple = (rain > 0.05f) ? (1.0f + 0.04f * FastMath.sin(swayTime * 12.0f + center.x)) : 1.0f;
                geom.setLocalScale(currentRadius * ripple, currentRadius * ripple, 1.0f);
                Material mat = geom.getMaterial();
                if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                    float alpha = Math.min(0.85f, 0.30f + (currentRadius / maxRadius) * 0.55f);
                    mat.setColor("Color", new ColorRGBA(0.20f, 0.45f, 0.70f, alpha));
                }
            }
        }
    }

    private CanopyDripParticle createCanopyDrip(Vector3f canopyPos, float groundY, float initialDelay) {
        Material dropMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dropMat.setColor("Color", new ColorRGBA(0.85f, 0.92f, 1.0f, 0.80f));
        dropMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("CanopyDrip", new Box(0.008f, 0.022f, 0.008f));
        geom.setMaterial(dropMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(canopyPos);

        return new CanopyDripParticle(geom, canopyPos, groundY, initialDelay);
    }

    private static class CanopyDripParticle {
        final Geometry geom;
        final Vector3f canopyPos;
        final float groundY;
        final Vector3f currentPos;
        float velY = 0.0f;
        float resetTimer = 0.0f;

        CanopyDripParticle(Geometry geom, Vector3f canopyPos, float groundY, float initialDelay) {
            this.geom = geom;
            this.canopyPos = canopyPos.clone();
            this.groundY = groundY;
            this.currentPos = canopyPos.clone();
            this.resetTimer = initialDelay;
        }

        void update(float tpf, float rainWetness) {
            if (rainWetness < 0.05f) {
                geom.setCullHint(Spatial.CullHint.Always);
                return;
            }

            if (resetTimer > 0) {
                resetTimer -= tpf;
                geom.setCullHint(Spatial.CullHint.Always);
                return;
            }

            geom.setCullHint(Spatial.CullHint.Dynamic);
            velY -= 9.81f * tpf;
            currentPos.y += velY * tpf;
            geom.setLocalTranslation(currentPos);

            if (currentPos.y <= groundY) {
                currentPos.set(canopyPos);
                velY = 0.0f;
                resetTimer = 0.4f + (1.0f - rainWetness) * 2.0f;
            }
        }
    }

    private GnatSwarmAgent createGnatSwarm(Vector3f center, float radius, Random rand, int index) {
        Node swarmNode = new Node("GnatSwarm_" + index);
        swarmNode.setShadowMode(RenderQueue.ShadowMode.Off);

        Material gnatMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gnatMat.setColor("Color", new ColorRGBA(0.12f, 0.12f, 0.15f, 0.75f));
        gnatMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        List<Geometry> gnats = new ArrayList<>();
        List<Float> phases = new ArrayList<>();
        int count = 16;
        for (int i = 0; i < count; i++) {
            Geometry g = new Geometry("Gnat_" + index + "_" + i, new Box(0.006f, 0.006f, 0.006f));
            g.setMaterial(gnatMat);
            g.setQueueBucket(RenderQueue.Bucket.Transparent);
            swarmNode.attachChild(g);
            gnats.add(g);
            phases.add(rand.nextFloat() * FastMath.TWO_PI);
        }

        return new GnatSwarmAgent(swarmNode, center, radius, gnats, phases);
    }

    private static class GnatSwarmAgent {
        final Node swarmNode;
        final Vector3f center;
        final List<Geometry> gnats = new ArrayList<>();
        final List<Float> gnatPhases = new ArrayList<>();
        final float radius;

        GnatSwarmAgent(Node swarmNode, Vector3f center, float radius, List<Geometry> gnats, List<Float> phases) {
            this.swarmNode = swarmNode;
            this.center = center;
            this.radius = radius;
            this.gnats.addAll(gnats);
            this.gnatPhases.addAll(phases);
        }

        void update(float tpf, float time, boolean isTwilight, float rain, float temp) {
            boolean active = (isTwilight || rain > 0.05f) && rain < 0.35f && temp > 10.0f;
            if (!active) {
                swarmNode.setCullHint(Spatial.CullHint.Always);
                return;
            }
            swarmNode.setCullHint(Spatial.CullHint.Dynamic);

            for (int i = 0; i < gnats.size(); i++) {
                Geometry g = gnats.get(i);
                float phase = gnatPhases.get(i);
                float theta = time * 3.2f + phase;
                float r = radius * (0.35f + 0.65f * FastMath.sin(time * 1.8f + phase * 2.0f));
                float gx = center.x + r * FastMath.cos(theta);
                float gy = center.y + 0.25f * FastMath.sin(theta * 2.2f + phase) + 0.15f * FastMath.cos(time * 4.0f + phase);
                float gz = center.z + r * FastMath.sin(theta);
                g.setLocalTranslation(gx, gy, gz);
            }
        }
    }

    private AphidClusterAgent createAphidCluster(Vector3f stemPos, Random rand, int index) {
        Node clusterNode = new Node("AphidCluster_" + index);
        clusterNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        Material aphidMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        aphidMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = (rand.nextBoolean()) ? new ColorRGBA(0.28f, 0.58f, 0.18f, 1.0f) : new ColorRGBA(0.15f, 0.18f, 0.12f, 1.0f);
        aphidMat.setColor("Diffuse", col);
        aphidMat.setColor("Ambient", col.mult(0.6f));

        Material honeyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        honeyMat.setColor("Color", new ColorRGBA(0.96f, 0.90f, 0.40f, 0.90f));
        honeyMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        List<Geometry> droplets = new ArrayList<>();
        int count = 4 + rand.nextInt(4);
        for (int i = 0; i < count; i++) {
            float ox = (rand.nextFloat() - 0.5f) * 0.18f;
            float oy = (rand.nextFloat() - 0.5f) * 0.12f;
            float oz = (rand.nextFloat() - 0.5f) * 0.18f;

            // Aphid body
            Geometry aphid = new Geometry("Aphid_" + i, new Sphere(8, 8, 0.025f));
            aphid.setMaterial(aphidMat);
            aphid.setLocalScale(1.0f, 0.8f, 1.4f);
            aphid.setLocalTranslation(stemPos.x + ox, stemPos.y + oy, stemPos.z + oz);
            clusterNode.attachChild(aphid);

            // Glistening Honeydew droplet on aphid posterior
            if (rand.nextFloat() < 0.65f) {
                Geometry droplet = new Geometry("Honeydew_" + i, new Sphere(6, 6, 0.014f));
                droplet.setMaterial(honeyMat);
                droplet.setQueueBucket(RenderQueue.Bucket.Transparent);
                droplet.setLocalTranslation(stemPos.x + ox, stemPos.y + oy + 0.025f, stemPos.z + oz - 0.02f);
                clusterNode.attachChild(droplet);
                droplets.add(droplet);
            }
        }

        return new AphidClusterAgent(clusterNode, stemPos, droplets);
    }

    private static class AphidClusterAgent {
        final Node node;
        final Vector3f stemPos;
        final List<Geometry> honeydewDroplets = new ArrayList<>();

        AphidClusterAgent(Node node, Vector3f stemPos, List<Geometry> honeydewDroplets) {
            this.node = node;
            this.stemPos = stemPos;
            this.honeydewDroplets.addAll(honeydewDroplets);
        }

        void update(float tpf, float swayTime) {
            float pulse = 0.85f + 0.15f * FastMath.sin(swayTime * 2.5f + stemPos.x);
            for (Geometry drop : honeydewDroplets) {
                drop.setLocalScale(pulse);
            }
        }
    }

    private ExtrafloralNectaryAgent createExtrafloralNectary(Vector3f axilPos, Random rand) {
        Material nectaryMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        nectaryMat.setColor("Color", new ColorRGBA(0.98f, 0.88f, 0.25f, 0.90f));
        nectaryMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("ExtrafloralNectary", new Sphere(6, 6, 0.022f));
        geom.setMaterial(nectaryMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(axilPos);

        return new ExtrafloralNectaryAgent(geom, axilPos);
    }

    private static class ExtrafloralNectaryAgent {
        final Geometry geom;
        final Vector3f position;

        ExtrafloralNectaryAgent(Geometry geom, Vector3f position) {
            this.geom = geom;
            this.position = position;
        }

        void update(float tpf, float swayTime) {
            float glisten = 0.90f + 0.10f * FastMath.sin(swayTime * 3.0f + position.z);
            geom.setLocalScale(glisten);
        }
    }

    private CordycepsCadaverAgent createCordycepsCadaver(Vector3f anchorPos, Random rand, int index) {
        Node cadaverNode = new Node("CordycepsCadaver_" + index);
        cadaverNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        Material deadAntMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        deadAntMat.setBoolean("UseMaterialColors", true);
        deadAntMat.setColor("Diffuse", new ColorRGBA(0.12f, 0.08f, 0.06f, 1.0f));
        deadAntMat.setColor("Ambient", new ColorRGBA(0.08f, 0.05f, 0.04f, 1.0f));

        // Fixed ant body (summit disease grip)
        Geometry antBody = new Geometry("DeadAntGrip", new Box(0.025f, 0.02f, 0.05f));
        antBody.setMaterial(deadAntMat);
        antBody.setLocalTranslation(anchorPos);
        cadaverNode.attachChild(antBody);

        // Ascending fungal stroma stalk
        Material stromaMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        stromaMat.setBoolean("UseMaterialColors", true);
        ColorRGBA stromaCol = new ColorRGBA(0.85f, 0.72f, 0.45f, 1.0f);
        stromaMat.setColor("Diffuse", stromaCol);
        stromaMat.setColor("Ambient", stromaCol.mult(0.6f));

        Geometry stroma = new Geometry("FungalStroma", new Cylinder(6, 8, 0.008f, 0.18f, true));
        stroma.setMaterial(stromaMat);
        stroma.setLocalTranslation(anchorPos.x, anchorPos.y + 0.10f, anchorPos.z);
        stroma.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        cadaverNode.attachChild(stroma);

        // Clavate perithecial bulb
        Geometry bulb = new Geometry("PerithecialBulb", new Sphere(8, 8, 0.022f));
        bulb.setMaterial(stromaMat);
        bulb.setLocalTranslation(anchorPos.x, anchorPos.y + 0.20f, anchorPos.z);
        cadaverNode.attachChild(bulb);

        // Faint spore cloud aura
        Material sporeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sporeMat.setColor("Color", new ColorRGBA(0.92f, 0.85f, 0.60f, 0.35f));
        sporeMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry sporeAura = new Geometry("SporeCloud", new Sphere(8, 8, 0.08f));
        sporeAura.setMaterial(sporeMat);
        sporeAura.setQueueBucket(RenderQueue.Bucket.Transparent);
        sporeAura.setLocalTranslation(anchorPos.x, anchorPos.y + 0.20f, anchorPos.z);
        cadaverNode.attachChild(sporeAura);

        return new CordycepsCadaverAgent(cadaverNode, anchorPos, sporeAura);
    }

    private static class CordycepsCadaverAgent {
        final Node node;
        final Vector3f anchorPos;
        final Geometry sporeAura;
        float auraPulse = 0.0f;

        CordycepsCadaverAgent(Node node, Vector3f anchorPos, Geometry sporeAura) {
            this.node = node;
            this.anchorPos = anchorPos;
            this.sporeAura = sporeAura;
        }

        void update(float tpf, float swayTime) {
            auraPulse += tpf * 1.5f;
            float scale = 0.9f + 0.2f * FastMath.sin(auraPulse);
            if (sporeAura != null) {
                sporeAura.setLocalScale(scale);
            }
        }
    }

    private Geometry createMudcrackPatch(Vector3f pos, float size, Random rand, int index) {
        Material crackMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        crackMat.setColor("Color", new ColorRGBA(0.28f, 0.18f, 0.12f, 0.70f));
        crackMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry crack = new Geometry("Mudcrack_" + index, new Quad(size, size));
        crack.setMaterial(crackMat);
        crack.setQueueBucket(RenderQueue.Bucket.Transparent);
        crack.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, rand.nextFloat() * FastMath.TWO_PI));
        crack.setLocalTranslation(pos.x - size * 0.5f, pos.y, pos.z - size * 0.5f);
        crack.setCullHint(Spatial.CullHint.Always); // Initially dormant until drought
        return crack;
    }

    // =========================================================================
    // Multi-Mode Nest Architecture Visualizer System (13 Biologically Rigorous Typologies)
    // =========================================================================

    public void updateNestArchitectures(List<Colony> colonies, RenderMode mode) {
        this.trackedColonies.clear();
        if (colonies != null) {
            this.trackedColonies.addAll(colonies);
        }
        this.currentRenderMode = mode;
        rebuildNestArchitectures();
    }

    public void rebuildNestArchitectures() {
        nestArchitectureNode.detachAllChildren();
        colonyInfrastructureNode.detachAllChildren();
        if (trackedColonies.isEmpty()) return;

        for (Colony colony : trackedColonies) {
            createNestForColony(colony, currentRenderMode);
            createColonyInfrastructure(colony, currentRenderMode);
        }
    }

    private void createNestForColony(Colony colony, RenderMode mode) {
        if (colony == null) return;

        float x = colony.getNestX();
        float y = colony.getNestY();
        float z = colony.getNestZ();
        float surfY = (activeTerrarium != null) ? activeTerrarium.getSurfaceElevation(x, y) : z;

        String arch = (colony.getSpecies() != null && colony.getSpecies().getNestType() != null)
                ? colony.getSpecies().getNestType().toUpperCase()
                : "BURROW_UNDERGROUND";

        String spName = (colony.getSpecies() != null && colony.getSpecies().getCommonName() != null)
                ? colony.getSpecies().getCommonName().toLowerCase()
                : "";

        Node nestNode = new Node("Nest_" + colony.getId() + "_" + arch);
        nestNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        if (arch.contains("WOODEN_BEEHIVE") || spName.contains("bee") || spName.contains("abeille")) {
            buildWoodenBeehive(nestNode, x, surfY, y, mode);
        } else if (arch.contains("PAPER_PEDUNCULATE") || spName.contains("wasp") || spName.contains("guêpe")) {
            buildPaperPedunculateNest(nestNode, x, Math.max(surfY + 2.5f, z), y, mode);
        } else if (arch.contains("CATHEDRAL_MOUND") || spName.contains("termite")) {
            buildCathedralTermiteMound(nestNode, x, surfY, y, mode);
        } else if (arch.contains("MOUND") || arch.contains("FORMICA") || spName.contains("formica") || spName.contains("mound") || spName.contains("dome")) {
            buildFormicaThatchedMound(nestNode, x, surfY, y, mode, 180.0f);
        } else if (arch.contains("ARBOREAL_SILK_LEAF") || spName.contains("weaver") || spName.contains("tisserande")) {
            buildArborealSilkLeafNest(nestNode, x, Math.max(surfY + 3.0f, z), y, mode);
        } else if (arch.contains("WAX_POTS_CLUSTER") || spName.contains("bourdon") || spName.contains("bombus")) {
            buildWaxPotsCluster(nestNode, x, surfY + 0.1f, y, mode);
        } else if (arch.contains("BIVOUAC_LIVING_NEST") || spName.contains("army") || spName.contains("légionnaire")) {
            buildLivingBivouac(nestNode, x, Math.max(surfY + 1.8f, z), y, mode);
        } else if (arch.contains("CARTON_NEST") || spName.contains("carton") || spName.contains("crematogaster")) {
            buildCartonNest(nestNode, x, Math.max(surfY + 2.2f, z), y, mode);
        } else if (arch.contains("BAMBOO_STEM_NEST") || spName.contains("gall") || spName.contains("galle")) {
            buildBambooStemNest(nestNode, x, Math.max(surfY + 1.2f, z), y, mode);
        } else if (arch.contains("HOLLOW_TRUNK_NEST") || spName.contains("camponotus") || spName.contains("charpentière")) {
            buildHollowTrunkNest(nestNode, x, surfY, y, mode);
        } else if (arch.contains("WAX_COMB_HEXAGONAL")) {
            buildHexagonalWaxComb(nestNode, x, Math.max(surfY + 2.0f, z), y, mode);
        } else {
            // Subterranean Excavation Collar Ring on Surface (No fake twig mound, true excavated substrate collar)
            buildSubterraneanEntranceCollar(nestNode, x, surfY, y, mode);
        }

        nestArchitectureNode.attachChild(nestNode);
    }

    private void createColonyInfrastructure(Colony colony, RenderMode mode) {
        if (colony == null) return;
        float x = colony.getNestX();
        float y = colony.getNestY();
        float z = colony.getNestZ();
        float surfY = (activeTerrarium != null) ? activeTerrarium.getSurfaceElevation(x, y) : z;

        Node infraNode = new Node("ColonyInfra_" + colony.getId());
        infraNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        buildRefuseMidden(infraNode, x, surfY, y, mode);
        buildTrunkTrails(infraNode, x, surfY, y, mode);

        colonyInfrastructureNode.attachChild(infraNode);
    }

    private void buildWoodenBeehive(Node parent, float x, float y, float z, RenderMode mode) {
        if (mode == RenderMode.REALISTIC && beehiveModel != null) {
            Spatial hive = beehiveModel.clone();
            hive.setLocalScale(0.015f);
            hive.setLocalTranslation(x, y, z);
            hive.setLocalRotation(new Quaternion().fromAngles(0, FastMath.DEG_TO_RAD * 45.0f, 0));
            parent.attachChild(hive);
            return;
        }

        if (mode == RenderMode.SCIENTIFIC) {
            // Metric Sensor Station Beehive (White calibrated data station box)
            Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            boxMat.setBoolean("UseMaterialColors", true);
            boxMat.setColor("Diffuse", new ColorRGBA(0.92f, 0.94f, 0.96f, 1.0f));
            boxMat.setColor("Ambient", new ColorRGBA(0.65f, 0.70f, 0.75f, 1.0f));

            Geometry body = new Geometry("SciHiveBody", new Box(0.42f, 0.50f, 0.42f));
            body.setMaterial(boxMat);
            body.setLocalTranslation(x, y + 0.50f, z);
            parent.attachChild(body);

            // Flight Entrance Sensor Bar
            Material sensorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            sensorMat.setColor("Color", new ColorRGBA(0.20f, 0.80f, 0.95f, 1.0f));
            Geometry sensorBar = new Geometry("SensorBar", new Box(0.35f, 0.03f, 0.05f));
            sensorBar.setMaterial(sensorMat);
            sensorBar.setLocalTranslation(x, y + 0.10f, z + 0.43f);
            parent.attachChild(sensorBar);
        } else {
            // GAMIFIED: Full Minecraft-style Bee Hive Block with amber stripes & entrance slit
            Material woodMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            woodMat.setBoolean("UseMaterialColors", true);
            woodMat.setColor("Diffuse", new ColorRGBA(0.85f, 0.58f, 0.22f, 1.0f));
            woodMat.setColor("Ambient", new ColorRGBA(0.55f, 0.35f, 0.12f, 1.0f));

            Geometry hiveBlock = new Geometry("GamifiedBeehive", new Box(0.50f, 0.50f, 0.50f));
            hiveBlock.setMaterial(woodMat);
            hiveBlock.setLocalTranslation(x, y + 0.50f, z);
            parent.attachChild(hiveBlock);

            // Honey slit
            Material honeyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            honeyMat.setColor("Color", new ColorRGBA(0.95f, 0.72f, 0.10f, 1.0f));
            Geometry slit = new Geometry("HoneySlit", new Box(0.30f, 0.06f, 0.02f));
            slit.setMaterial(honeyMat);
            slit.setLocalTranslation(x, y + 0.35f, z + 0.51f);
            parent.attachChild(slit);
        }
    }

    private void buildPaperPedunculateNest(Node parent, float x, float y, float z, RenderMode mode) {
        Material paperMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        paperMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = (mode == RenderMode.SCIENTIFIC) ? new ColorRGBA(0.70f, 0.82f, 0.90f, 0.75f) : new ColorRGBA(0.62f, 0.58f, 0.52f, 1.0f);
        paperMat.setColor("Diffuse", col);
        paperMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            // Stepped grey voxel umbrella
            Geometry stem = new Geometry("WaspStem", new Box(0.06f, 0.25f, 0.06f));
            stem.setMaterial(paperMat);
            stem.setLocalTranslation(x, y + 0.25f, z);
            parent.attachChild(stem);

            Geometry cone1 = new Geometry("WaspVoxelTop", new Box(0.35f, 0.15f, 0.35f));
            cone1.setMaterial(paperMat);
            cone1.setLocalTranslation(x, y, z);
            parent.attachChild(cone1);

            Geometry cone2 = new Geometry("WaspVoxelMid", new Box(0.50f, 0.20f, 0.50f));
            cone2.setMaterial(paperMat);
            cone2.setLocalTranslation(x, y - 0.20f, z);
            parent.attachChild(cone2);
        } else {
            // Suspended Papyraceous Dome
            Geometry pedicel = new Geometry("WaspPedicel", new Cylinder(6, 8, 0.025f, 0.40f, true));
            pedicel.setMaterial(paperMat);
            pedicel.setLocalTranslation(x, y + 0.20f, z);
            parent.attachChild(pedicel);

            Geometry dome = new Geometry("WaspEnvelope", new Sphere(16, 16, 0.65f));
            dome.setMaterial(paperMat);
            dome.setLocalScale(1.0f, 1.35f, 1.0f);
            dome.setLocalTranslation(x, y - 0.35f, z);
            parent.attachChild(dome);
        }
    }

    private void buildCathedralTermiteMound(Node parent, float x, float y, float z, RenderMode mode) {
        Material clayMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        clayMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = (mode == RenderMode.SCIENTIFIC) ? new ColorRGBA(0.92f, 0.45f, 0.30f, 1.0f) : new ColorRGBA(0.72f, 0.38f, 0.22f, 1.0f);
        clayMat.setColor("Diffuse", col);
        clayMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            // Stepped sandstone spire tower
            for (int i = 0; i < 6; i++) {
                float size = 0.90f - i * 0.12f;
                Geometry block = new Geometry("CathedralSpire_" + i, new Box(size, 0.30f, size));
                block.setMaterial(clayMat);
                block.setLocalTranslation(x, y + i * 0.60f + 0.30f, z);
                parent.attachChild(block);
            }
        } else {
            // Majestic fluted terracotta chimney spire (3.2m tall)
            Geometry mainSpire = new Geometry("CathedralSpire", new Cylinder(12, 16, 0.75f, 3.4f, true));
            mainSpire.setMaterial(clayMat);
            mainSpire.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            mainSpire.setLocalTranslation(x, y + 1.7f, z);
            parent.attachChild(mainSpire);

            // Fluted side buttresses
            for (int i = 0; i < 4; i++) {
                float angle = i * FastMath.HALF_PI;
                Geometry buttress = new Geometry("Buttress_" + i, new Cylinder(8, 12, 0.35f, 2.0f, true));
                buttress.setMaterial(clayMat);
                buttress.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
                buttress.setLocalTranslation(x + FastMath.cos(angle) * 0.65f, y + 1.0f, z + FastMath.sin(angle) * 0.65f);
                parent.attachChild(buttress);
            }
        }
    }

    private void buildArborealSilkLeafNest(Node parent, float x, float y, float z, RenderMode mode) {
        Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        leafMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = (mode == RenderMode.SCIENTIFIC) ? new ColorRGBA(0.35f, 0.85f, 0.45f, 0.8f) : new ColorRGBA(0.28f, 0.65f, 0.22f, 1.0f);
        leafMat.setColor("Diffuse", col);
        leafMat.setColor("Ambient", col.mult(0.6f));

        Material silkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        silkMat.setColor("Color", new ColorRGBA(0.96f, 0.98f, 1.0f, 0.90f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry leafCube = new Geometry("GamifiedSilkLeaf", new Box(0.60f, 0.50f, 0.60f));
            leafCube.setMaterial(leafMat);
            leafCube.setLocalTranslation(x, y, z);
            parent.attachChild(leafCube);

            Geometry silkStripe = new Geometry("SilkBand", new Box(0.62f, 0.08f, 0.62f));
            silkStripe.setMaterial(silkMat);
            silkStripe.setLocalTranslation(x, y, z);
            parent.attachChild(silkStripe);
        } else {
            // Curved leaf pavilion with white larval silk seams
            Geometry pavilion = new Geometry("SilkPavilion", new Sphere(14, 14, 0.75f));
            pavilion.setMaterial(leafMat);
            pavilion.setLocalScale(1.2f, 0.9f, 1.0f);
            pavilion.setLocalTranslation(x, y, z);
            parent.attachChild(pavilion);

            // Silk suture lines
            for (int i = 0; i < 3; i++) {
                Geometry seam = new Geometry("SilkSeam_" + i, new Cylinder(4, 12, 0.015f, 1.2f, true));
                seam.setMaterial(silkMat);
                seam.setLocalRotation(new Quaternion().fromAngles(i * 0.6f, i * 1.1f, 0));
                seam.setLocalTranslation(x, y, z);
                parent.attachChild(seam);
            }
        }
    }

    private void buildWaxPotsCluster(Node parent, float x, float y, float z, RenderMode mode) {
        Material waxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        waxMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.78f, 0.55f, 0.18f, 1.0f);
        waxMat.setColor("Diffuse", col);
        waxMat.setColor("Ambient", col.mult(0.6f));

        Random rand = new Random(42);
        int potCount = 7;
        for (int i = 0; i < potCount; i++) {
            float ox = (rand.nextFloat() - 0.5f) * 0.65f;
            float oz = (rand.nextFloat() - 0.5f) * 0.65f;
            float r = 0.18f + rand.nextFloat() * 0.10f;

            if (mode == RenderMode.GAMIFIED) {
                Geometry pot = new Geometry("GamifiedWaxPot_" + i, new Box(r, r * 1.2f, r));
                pot.setMaterial(waxMat);
                pot.setLocalTranslation(x + ox, y + r * 1.2f, z + oz);
                parent.attachChild(pot);
            } else {
                Geometry pot = new Geometry("WaxPot_" + i, new Sphere(10, 10, r));
                pot.setMaterial(waxMat);
                pot.setLocalScale(1.0f, 1.35f, 1.0f);
                pot.setLocalTranslation(x + ox, y + r * 1.35f, z + oz);
                parent.attachChild(pot);
            }
        }
    }

    private void buildLivingBivouac(Node parent, float x, float y, float z, RenderMode mode) {
        Material antMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        antMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = (mode == RenderMode.SCIENTIFIC) ? new ColorRGBA(0.40f, 0.20f, 0.80f, 0.85f) : new ColorRGBA(0.18f, 0.12f, 0.08f, 1.0f);
        antMat.setColor("Diffuse", col);
        antMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry clump = new Geometry("GamifiedBivouac", new Box(0.55f, 0.80f, 0.55f));
            clump.setMaterial(antMat);
            clump.setLocalTranslation(x, y - 0.80f, z);
            parent.attachChild(clump);
        } else {
            Geometry curtain = new Geometry("LivingBivouac", new Sphere(14, 14, 0.70f));
            curtain.setMaterial(antMat);
            curtain.setLocalScale(0.85f, 1.6f, 0.85f);
            curtain.setLocalTranslation(x, y - 0.85f, z);
            parent.attachChild(curtain);
        }
    }

    private void buildCartonNest(Node parent, float x, float y, float z, RenderMode mode) {
        Material cartonMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        cartonMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.32f, 0.24f, 0.18f, 1.0f);
        cartonMat.setColor("Diffuse", col);
        cartonMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry box = new Geometry("GamifiedCarton", new Box(0.50f, 0.45f, 0.50f));
            box.setMaterial(cartonMat);
            box.setLocalTranslation(x, y, z);
            parent.attachChild(box);
        } else {
            Geometry bulb = new Geometry("CartonNest", new Sphere(12, 12, 0.60f));
            bulb.setMaterial(cartonMat);
            bulb.setLocalScale(1.1f, 0.95f, 1.0f);
            bulb.setLocalTranslation(x, y, z);
            parent.attachChild(bulb);
        }
    }

    private void buildBambooStemNest(Node parent, float x, float y, float z, RenderMode mode) {
        Material stemMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        stemMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.55f, 0.68f, 0.25f, 1.0f);
        stemMat.setColor("Diffuse", col);
        stemMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry stem = new Geometry("GamifiedBamboo", new Box(0.20f, 0.80f, 0.20f));
            stem.setMaterial(stemMat);
            stem.setLocalTranslation(x, y, z);
            parent.attachChild(stem);
        } else {
            Geometry bamboo = new Geometry("BambooStem", new Cylinder(8, 12, 0.15f, 1.6f, true));
            bamboo.setMaterial(stemMat);
            bamboo.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            bamboo.setLocalTranslation(x, y, z);
            parent.attachChild(bamboo);
        }
    }

    private void buildHollowTrunkNest(Node parent, float x, float y, float z, RenderMode mode) {
        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.42f, 0.32f, 0.22f, 1.0f);
        trunkMat.setColor("Diffuse", col);
        trunkMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry trunk = new Geometry("GamifiedHollowTrunk", new Box(0.65f, 0.90f, 0.65f));
            trunk.setMaterial(trunkMat);
            trunk.setLocalTranslation(x, y + 0.90f, z);
            parent.attachChild(trunk);
        } else {
            Geometry trunk = new Geometry("HollowTrunk", new Cylinder(10, 14, 0.55f, 1.8f, true));
            trunk.setMaterial(trunkMat);
            trunk.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            trunk.setLocalTranslation(x, y + 0.90f, z);
            parent.attachChild(trunk);
        }
    }

    private void buildHexagonalWaxComb(Node parent, float x, float y, float z, RenderMode mode) {
        Material combMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        combMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.92f, 0.75f, 0.20f, 1.0f);
        combMat.setColor("Diffuse", col);
        combMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry comb = new Geometry("GamifiedWaxComb", new Box(0.65f, 0.75f, 0.10f));
            comb.setMaterial(combMat);
            comb.setLocalTranslation(x, y, z);
            parent.attachChild(comb);
        } else {
            Geometry combPlate = new Geometry("WaxCombPlate", new Box(0.60f, 0.85f, 0.08f));
            combPlate.setMaterial(combMat);
            combPlate.setLocalTranslation(x, y, z);
            parent.attachChild(combPlate);
        }
    }

    private void buildSubterraneanEntranceCollar(Node parent, float x, float y, float z, RenderMode mode) {
        // Subtle excavated soil collar around subterranean gallery entrance
        Material earthMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        earthMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.38f, 0.28f, 0.18f, 1.0f);
        earthMat.setColor("Diffuse", col);
        earthMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            Geometry collar = new Geometry("GamifiedCollar", new Box(0.40f, 0.08f, 0.40f));
            collar.setMaterial(earthMat);
            collar.setLocalTranslation(x, y + 0.04f, z);
            parent.attachChild(collar);
        } else {
            Geometry collar = new Geometry("ExcavatedSoilCollar", new Cylinder(12, 12, 0.45f, 0.08f, true));
            collar.setMaterial(earthMat);
            collar.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            collar.setLocalTranslation(x, y + 0.04f, z);
            parent.attachChild(collar);
        }
    }

    private void buildFormicaThatchedMound(Node parent, float x, float y, float z, RenderMode mode, float southAngle) {
        if (mode == RenderMode.SCIENTIFIC) {
            Material sciMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            sciMat.setBoolean("UseMaterialColors", true);
            sciMat.setColor("Diffuse", new ColorRGBA(0.95f, 0.45f, 0.15f, 0.80f));
            sciMat.setColor("Ambient", new ColorRGBA(0.60f, 0.25f, 0.10f, 0.80f));

            Geometry dome = new Geometry("SciThatchedMound", new Sphere(16, 16, 1.10f));
            dome.setMaterial(sciMat);
            dome.setLocalScale(1.3f, 0.75f, 1.1f);
            dome.setLocalTranslation(x, y + 0.45f, z);
            dome.setLocalRotation(new Quaternion().fromAngles(0, FastMath.DEG_TO_RAD * southAngle, 0));
            parent.attachChild(dome);

            Material arrowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            arrowMat.setColor("Color", new ColorRGBA(1.0f, 0.85f, 0.10f, 0.95f));
            Geometry arrow = new Geometry("SolarVectorArrow", new Cylinder(4, 8, 0.04f, 0.90f, true));
            arrow.setMaterial(arrowMat);
            arrow.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            arrow.setLocalTranslation(x, y + 1.25f, z + 0.55f);
            parent.attachChild(arrow);
        } else if (mode == RenderMode.GAMIFIED) {
            Material needleMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            needleMat.setBoolean("UseMaterialColors", true);
            needleMat.setColor("Diffuse", new ColorRGBA(0.42f, 0.28f, 0.14f, 1.0f));
            needleMat.setColor("Ambient", new ColorRGBA(0.28f, 0.18f, 0.08f, 1.0f));

            for (int lvl = 0; lvl < 3; lvl++) {
                float sz = 0.90f - lvl * 0.25f;
                Geometry step = new Geometry("GamifiedMoundStep_" + lvl, new Box(sz, 0.18f, sz));
                step.setMaterial(needleMat);
                step.setLocalTranslation(x, y + 0.18f + lvl * 0.32f, z);
                parent.attachChild(step);
            }
        } else {
            Material thatchMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            thatchMat.setBoolean("UseMaterialColors", true);
            thatchMat.setColor("Diffuse", new ColorRGBA(0.38f, 0.24f, 0.14f, 1.0f));
            thatchMat.setColor("Ambient", new ColorRGBA(0.24f, 0.15f, 0.08f, 1.0f));

            Geometry dome = new Geometry("ThatchedNeedleMound", new Sphere(18, 18, 1.15f));
            dome.setMaterial(thatchMat);
            dome.setLocalScale(1.35f, 0.85f, 1.15f);
            dome.setLocalTranslation(x, y + 0.40f, z);
            parent.attachChild(dome);

            Material baseMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            baseMat.setBoolean("UseMaterialColors", true);
            baseMat.setColor("Diffuse", new ColorRGBA(0.30f, 0.20f, 0.10f, 1.0f));
            Geometry apron = new Geometry("NeedleApron", new Cylinder(16, 16, 1.65f, 0.08f, true));
            apron.setMaterial(baseMat);
            apron.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            apron.setLocalTranslation(x, y + 0.04f, z);
            parent.attachChild(apron);
        }
    }

    private void buildRefuseMidden(Node parent, float x, float y, float z, RenderMode mode) {
        float mx = x + 6.0f;
        float mz = z + 6.0f;
        float my = (activeTerrarium != null) ? activeTerrarium.getSurfaceElevation(mx, mz) : y;

        if (mode == RenderMode.SCIENTIFIC) {
            Material ringMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            ringMat.setColor("Color", new ColorRGBA(0.95f, 0.25f, 0.25f, 0.65f));
            ringMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

            Geometry perimeter = new Geometry("MiddenSanitaryPerimeter", new Cylinder(16, 16, 1.2f, 0.04f, false));
            perimeter.setMaterial(ringMat);
            perimeter.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            perimeter.setLocalTranslation(mx, my + 0.05f, mz);
            parent.attachChild(perimeter);

            Geometry centerHeap = new Geometry("SciMiddenCore", new Box(0.35f, 0.20f, 0.35f));
            Material coreMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            coreMat.setBoolean("UseMaterialColors", true);
            coreMat.setColor("Diffuse", new ColorRGBA(0.65f, 0.20f, 0.20f, 0.85f));
            centerHeap.setMaterial(coreMat);
            centerHeap.setLocalTranslation(mx, my + 0.10f, mz);
            parent.attachChild(centerHeap);
        } else if (mode == RenderMode.GAMIFIED) {
            Material compostMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            compostMat.setBoolean("UseMaterialColors", true);
            compostMat.setColor("Diffuse", new ColorRGBA(0.25f, 0.18f, 0.12f, 1.0f));

            Geometry compost = new Geometry("GamifiedCompostHeap", new Box(0.60f, 0.35f, 0.60f));
            compost.setMaterial(compostMat);
            compost.setLocalTranslation(mx, my + 0.18f, mz);
            parent.attachChild(compost);
        } else {
            Material wasteMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            wasteMat.setBoolean("UseMaterialColors", true);
            wasteMat.setColor("Diffuse", new ColorRGBA(0.18f, 0.14f, 0.10f, 1.0f));
            wasteMat.setColor("Ambient", new ColorRGBA(0.10f, 0.08f, 0.05f, 1.0f));

            Geometry heap = new Geometry("OrganicRefuseMidden", new Sphere(12, 12, 0.75f));
            heap.setMaterial(wasteMat);
            heap.setLocalScale(1.3f, 0.45f, 1.2f);
            heap.setLocalTranslation(mx, my + 0.15f, mz);
            parent.attachChild(heap);
        }
    }

    private void buildTrunkTrails(Node parent, float x, float y, float z, RenderMode mode) {
        float[] angles = { 45.0f, 160.0f, 290.0f };
        for (float ang : angles) {
            float rad = FastMath.DEG_TO_RAD * ang;
            float len = 12.0f;
            float endX = x + len * FastMath.cos(rad);
            float endZ = z + len * FastMath.sin(rad);
            float endY = (activeTerrarium != null) ? activeTerrarium.getSurfaceElevation(endX, endZ) : y;
            float midX = (x + endX) * 0.5f;
            float midZ = (z + endZ) * 0.5f;
            float midY = (y + endY) * 0.5f;

            if (mode == RenderMode.SCIENTIFIC) {
                Material ribbonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                ribbonMat.setColor("Color", new ColorRGBA(0.15f, 0.75f, 0.95f, 0.65f));
                ribbonMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

                Geometry ribbon = new Geometry("TrunkTrailSci_" + (int)ang, new Box(0.15f, 0.02f, len * 0.5f));
                ribbon.setMaterial(ribbonMat);
                ribbon.setLocalTranslation(midX, midY + 0.04f, midZ);
                ribbon.setLocalRotation(new Quaternion().fromAngles(0, -rad + FastMath.HALF_PI, 0));
                parent.attachChild(ribbon);
            } else if (mode == RenderMode.GAMIFIED) {
                Material pathMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                pathMat.setBoolean("UseMaterialColors", true);
                pathMat.setColor("Diffuse", new ColorRGBA(0.55f, 0.42f, 0.28f, 1.0f));

                Geometry path = new Geometry("GamifiedTrail_" + (int)ang, new Box(0.25f, 0.03f, len * 0.5f));
                path.setMaterial(pathMat);
                path.setLocalTranslation(midX, midY + 0.02f, midZ);
                path.setLocalRotation(new Quaternion().fromAngles(0, -rad + FastMath.HALF_PI, 0));
                parent.attachChild(path);
            } else {
                Material trailMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                trailMat.setBoolean("UseMaterialColors", true);
                trailMat.setColor("Diffuse", new ColorRGBA(0.26f, 0.18f, 0.11f, 1.0f));
                trailMat.setColor("Ambient", new ColorRGBA(0.16f, 0.10f, 0.06f, 1.0f));

                Geometry trail = new Geometry("RealisticTrunkTrail_" + (int)ang, new Box(0.20f, 0.02f, len * 0.5f));
                trail.setMaterial(trailMat);
                trail.setLocalTranslation(midX, midY + 0.03f, midZ);
                trail.setLocalRotation(new Quaternion().fromAngles(0, -rad + FastMath.HALF_PI, 0));
                parent.attachChild(trail);
            }
        }
    }

    public Spatial renderBeehive(float x, float y, float z, float orientationAngle) {
        if (beehiveModel == null) return null;

        Spatial hive = beehiveModel.clone();
        hive.setName("3D_Beehive_" + (int) x + "_" + (int) y);
        hive.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        
        hive.setLocalScale(0.015f);
        hive.setLocalTranslation(x, z, y);
        hive.setLocalRotation(new Quaternion().fromAngles(0, FastMath.DEG_TO_RAD * orientationAngle, 0));

        rootNode.attachChild(hive);
        return hive;
    }

    public void ensureHostTreeForWaspNest(float x, float y, float nestAltitude) {
        for (Spatial child : rootNode.getChildren()) {
            if (child.getName() != null && child.getName().startsWith("Tree_")) {
                Vector3f pos = child.getLocalTranslation();
                float dist = FastMath.sqrt(FastMath.sqr(pos.x - x) + FastMath.sqr(pos.z - y));
                if (dist < 3.5f) {
                    return;
                }
            }
        }

        Spatial hostTree = !deciduousTrees.isEmpty() ? pickRandomFromList(deciduousTrees, new Random()) : null;
        if (hostTree != null) {
            Spatial treeInstance = hostTree.clone();
            treeInstance.setName("Tree_WaspHost_" + (int) x + "_" + (int) y);
            float targetHeight = Math.max(nestAltitude + 4.0f, 14.0f);
            float surfY = (activeTerrarium != null) ? activeTerrarium.getSurfaceElevation(x, y) : 0.0f;
            normalizeAndPositionModel(treeInstance, x, surfY, y, targetHeight, new Random(42));
        }
    }

    public void setUVVisionMode(boolean enabled) {
        this.uvVisionMode = enabled;
        applyUVColoration(rootNode, enabled);
    }

    public boolean isUVVisionMode() {
        return uvVisionMode;
    }

    public void setOmmatidialVisionMode(boolean enabled) {
        this.ommatidialVisionMode = enabled;
        if (enabled) {
            rebuildOmmatidialOverlay();
            if (ommatidialOverlayNode.getParent() == null) {
                rootNode.attachChild(ommatidialOverlayNode);
            }
        } else {
            ommatidialOverlayNode.detachAllChildren();
            ommatidialOverlayNode.removeFromParent();
        }
    }

    public boolean isOmmatidialVisionMode() {
        return ommatidialVisionMode;
    }

    private void rebuildOmmatidialOverlay() {
        ommatidialOverlayNode.detachAllChildren();
        if (activeTerrarium == null) return;

        float cx = currentGridWidth * 0.5f;
        float cz = currentGridHeight * 0.5f;
        float cy = activeTerrarium.getSurfaceElevation(cx, cz) + 12.0f;

        // 1. Celestial Rayleigh Polarization Compass Rings (e-vector sky rings)
        Material polMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        polMat.setColor("Color", new ColorRGBA(0.20f, 0.65f, 0.95f, 0.40f));
        polMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int r = 1; r <= 3; r++) {
            Geometry polRing = new Geometry("PolarizationRing_" + r, new Cylinder(24, 24, r * 6.0f, 0.05f, false));
            polRing.setMaterial(polMat);
            polRing.setQueueBucket(RenderQueue.Bucket.Transparent);
            polRing.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            polRing.setLocalTranslation(cx, cy + 25.0f, cz);
            ommatidialOverlayNode.attachChild(polRing);
        }

        // 2. Hexagonal Ommatidial Facet Lattice
        Material facetMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        facetMat.setColor("Color", new ColorRGBA(0.70f, 0.85f, 1.0f, 0.22f));
        facetMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                Geometry facet = new Geometry("Ommatidium_" + i + "_" + j, new Cylinder(6, 6, 1.4f, 0.02f, false));
                facet.setMaterial(facetMat);
                facet.setQueueBucket(RenderQueue.Bucket.Transparent);
                facet.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
                facet.setLocalTranslation(cx + i * 2.8f + (j % 2) * 1.4f, cy + 18.0f, cz + j * 2.4f);
                ommatidialOverlayNode.attachChild(facet);
            }
        }
    }

    private void applyUVColoration(Spatial spatial, boolean uv) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                try {
                    boolean hasDiffuse = mat.getMaterialDef().getMaterialParam("Diffuse") != null;
                    boolean hasAmbient = mat.getMaterialDef().getMaterialParam("Ambient") != null;
                    boolean hasColor = mat.getMaterialDef().getMaterialParam("Color") != null;

                    if (uv) {
                        // Store original colors if not already stored
                        if (hasDiffuse && mat.getParam("Diffuse") != null && geom.getUserData("origDiffuse") == null) {
                            geom.setUserData("origDiffuse", mat.getParam("Diffuse").getValue());
                        }
                        if (hasAmbient && mat.getParam("Ambient") != null && geom.getUserData("origAmbient") == null) {
                            geom.setUserData("origAmbient", mat.getParam("Ambient").getValue());
                        }
                        if (hasColor && mat.getParam("Color") != null && geom.getUserData("origColor") == null) {
                            geom.setUserData("origColor", mat.getParam("Color").getValue());
                        }

                        // Determine biological insect UV coloration based on geometry type
                        String gName = geom.getName() != null ? geom.getName().toLowerCase() : "";
                        ColorRGBA uvDiffuse;
                        ColorRGBA uvAmbient;

                        if (gName.contains("flower") || gName.contains("petal") || gName.contains("blossom") || gName.contains("nectar")) {
                            // Fluorescent UV nectar guide (electric violet/cyan bullseye)
                            uvDiffuse = new ColorRGBA(0.85f, 0.20f, 0.98f, 1.0f);
                            uvAmbient = new ColorRGBA(0.45f, 0.10f, 0.65f, 1.0f);
                        } else if (gName.contains("leaf") || gName.contains("leaves") || gName.contains("canopy") || gName.contains("bush")) {
                            // UV-absorptive dark green-violet foliage
                            uvDiffuse = new ColorRGBA(0.22f, 0.38f, 0.28f, 1.0f);
                            uvAmbient = new ColorRGBA(0.12f, 0.18f, 0.15f, 1.0f);
                        } else if (gName.contains("water") || gName.contains("river") || gName.contains("puddle")) {
                            // UV-reflective cyan polarization sheen
                            uvDiffuse = new ColorRGBA(0.20f, 0.75f, 1.0f, 0.90f);
                            uvAmbient = new ColorRGBA(0.10f, 0.40f, 0.70f, 0.90f);
                        } else {
                            // Neutral mineral / bark UV tint
                            uvDiffuse = new ColorRGBA(0.40f, 0.30f, 0.55f, 1.0f);
                            uvAmbient = new ColorRGBA(0.20f, 0.15f, 0.35f, 1.0f);
                        }

                        if (hasDiffuse) mat.setColor("Diffuse", uvDiffuse);
                        if (hasAmbient) mat.setColor("Ambient", uvAmbient);
                        if (hasColor) mat.setColor("Color", uvDiffuse);
                    } else {
                        // Restore original colors
                        if (hasDiffuse && geom.getUserData("origDiffuse") instanceof ColorRGBA origD) {
                            mat.setColor("Diffuse", origD);
                        }
                        if (hasAmbient && geom.getUserData("origAmbient") instanceof ColorRGBA origA) {
                            mat.setColor("Ambient", origA);
                        }
                        if (hasColor && geom.getUserData("origColor") instanceof ColorRGBA origC) {
                            mat.setColor("Color", origC);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applyUVColoration(child, uv);
            }
        }
    }
}

