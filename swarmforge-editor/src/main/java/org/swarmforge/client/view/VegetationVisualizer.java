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
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
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
 * 3D Vegetation and Flora visualizer for JMonkeyEngine.
 * Handles rendering of trees, plants, and foliage across 3 distinct modes:
 * - REALISTIC: 150+ native standalone OBJ Nature Pack models (Oaks, Birches, Spruces, Palms, Bushes, Rocks, Cacti)
 * - SCIENTIFIC: Metric parametric trees with DBH markers and LAI foliage envelopes
 * - GAMIFIED: Authentic full-scale Minecraft cubic voxel trees (Oak, Birch, Spruce/Pine, Cacti) with active shadows
 *
 * Features physical wind sway coupling and hemisphere-aware seasonal foliage tinting.
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
    private final List<Spatial> bushes = new ArrayList<>();
    private final List<Spatial> flowers = new ArrayList<>();
    private final List<Spatial> mushrooms = new ArrayList<>();
    private final List<Spatial> rocks = new ArrayList<>();
    private Spatial cactusModel;
    private Spatial bambooModel;
    private Spatial beehiveModel;
    private boolean uvVisionMode = false;

    public VegetationVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("VegetationNode");
        this.rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        loadAssets();
    }

    private void loadAssets() {
        // 1. Deciduous / Oak / Lush Forest Trees
        loadIntoList(deciduousTrees, "models/nature_pack_obj/forest_Tree_average_lush_Cube_004.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/forest_Tree_average_regular_Cube_002.obj");
        loadIntoList(deciduousTrees, "models/nature_pack_obj/forest_Tree_small_regular_Cube_005.obj");

        // 2. Conifers / Spruces / Pines
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_small_01_Cylinder_016.obj");
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_small_02_Cylinder_003.obj");
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_tiny_01_Cylinder_012.obj");
        loadIntoList(coniferTrees, "models/nature_pack_obj/forest_Tree_Spruce_tiny_02_Cylinder_014.obj");

        // 3. Dead Trees, Logs & Stumps
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Tree_average_bare_Cube.obj");
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Tree_small_bare_Cube_007.obj");
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Log_big_regular_Cylinder_015.obj");
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Log_big_knotty_Cylinder_017.obj");
        loadIntoList(deadTrees, "models/nature_pack_obj/forest_Stump_average_flat_Cube_013.obj");

        // 4. Bushes & Shrubs
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_average_Plane_001.obj");
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_group_average_Plane_137.obj");
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_group_big_Plane_138.obj");
        loadIntoList(bushes, "models/nature_pack_obj/forest_Bush_group_small_Plane_140.obj");

        // 5. Flowers & Grass
        loadIntoList(flowers, "models/nature_pack_obj/forest_Flower_bush_blue_Plane_030.obj");
        loadIntoList(flowers, "models/nature_pack_obj/forest_Flower_bush_red_Plane_031.obj");
        loadIntoList(flowers, "models/nature_pack_obj/forest_Flower_bush_white_Plane_023.obj");
        loadIntoList(flowers, "models/nature_pack_obj/forest_Grass_bush_high_01_Plane_002.obj");
        loadIntoList(flowers, "models/nature_pack_obj/forest_Grass_bush_low_01_Plane_005.obj");

        // 6. Mushrooms
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_big_brown_Icosphere_019.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_big_group_brown_Icosphere_027.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_flat_group_white_Cylinder_046.obj");
        loadIntoList(mushrooms, "models/nature_pack_obj/forest_Mushroom_high_group_yellow_Cylinder_068.obj");

        // 7. Rocks & Boulders
        loadIntoList(rocks, "models/nature_pack_obj/forest_Stone_average_01_Icosphere.obj");
        loadIntoList(rocks, "models/nature_pack_obj/forest_Stone_average_01_mossy_Icosphere_009.obj");
        loadIntoList(rocks, "models/nature_pack_obj/forest_Stone_group_average_Icosphere_022.obj");
        loadIntoList(rocks, "models/nature_pack_obj/forest_Stone_group_average_mossy_Icosphere_030.obj");

        // 8. Cacti & Bamboo Sets
        cactusModel = safeLoadModel("models/cactus.obj");
        bambooModel = safeLoadModel("models/bamboo_set.obj");

        // 9. Beehive
        beehiveModel = safeLoadModel("models/beehive/beehive_low.glb");
        if (beehiveModel == null) beehiveModel = safeLoadModel("models/beehive/beehive_box.glb");

        // 10. Load Tropical Banana / Palm Trees
        loadIntoList(palmTrees, "models/nature_pack_obj/lp_banan_1_Plane_003.obj");
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
        if (!visible) return;

        Biome biome = Biome.forLatitude(currentLatitude);
        Season effectiveSeason = getEffectiveSeason();
        Random rand = new Random(42);
        int cutX = Math.max(2, Math.min(gridWidth, (int) Math.ceil(gridWidth * Math.max(0.05f, Math.min(1.0f, slicePlaneRatio)))));

        if (vegSystem != null && !vegSystem.getPlants().isEmpty()) {
            // Position flora according to real simulation plants
            for (VegetationSystem.Plant plant : vegSystem.getPlants()) {
                if (plant.x >= cutX - 0.5f) continue; // Respect 3D geological cutaway slice
                float x = Math.max(1.5f, Math.min(cutX - 1.5f, plant.x));
                float z = Math.max(1.5f, Math.min(gridHeight - 1.5f, plant.y)); // Horizontal Y in domain -> JME Z
                float y = (terrarium != null) ? terrarium.getSurfaceElevation(x, z) : 0.0f;

                if (currentRenderMode == RenderMode.REALISTIC) {
                    createRealisticFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand);
                } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    createScientificFloraForPlant(x, y, z, plant, biome, rand);
                } else if (currentRenderMode == RenderMode.GAMIFIED) {
                    createGamifiedFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand);
                }
            }
        }

        // Realistic Mode Natural Ground Scatter Pass:
        // Distributes pebbles, gravel, mossy rocks, mushrooms, and grass tufts
        // based on simulation substrate and soil coverage.
        if (currentRenderMode == RenderMode.REALISTIC && terrarium != null) {
            spawnRealisticGroundScatter(terrarium, cutX, biome, effectiveSeason, rand);
        }
    }

    private void spawnRealisticGroundScatter(Terrarium terrarium, int cutX, Biome biome, Season season, Random rand) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        // Sample surface with biological jitter clamped inside terrain margins
        int step = Math.max(2, Math.min(width, height) / 24);
        for (int x = 2; x < cutX - 2; x += step) {
            for (int y = 2; y < height - 2; y += step) {
                float jx = Math.max(1.5f, Math.min(cutX - 1.5f, x + (rand.nextFloat() - 0.5f) * (step * 0.8f)));
                float jz = Math.max(1.5f, Math.min(height - 1.5f, y + (rand.nextFloat() - 0.5f) * (step * 0.8f)));
                if (jx >= cutX - 1.0f || jx < 1.0f || jz < 1.0f || jz >= height - 1.0f) continue;

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
                float targetScale = 0.35f;

                if (mat == TerrariumCell.Material.ROCK || mat == TerrariumCell.Material.GRAVEL) {
                    // Pebble & Rock Scatter on stony ground
                    if (roll < 0.45f && !rocks.isEmpty()) {
                        scatterModel = pickRandomFromList(rocks, rand);
                        targetScale = (mat == TerrariumCell.Material.GRAVEL) ? (0.18f + rand.nextFloat() * 0.25f) : (0.40f + rand.nextFloat() * 0.60f);
                    }
                } else if (mat == TerrariumCell.Material.PEAT || mat == TerrariumCell.Material.LEAF_LITTER) {
                    // Mushrooms & Fallen Forest Detritus on rich organic soil
                    if (roll < 0.30f && !mushrooms.isEmpty()) {
                        scatterModel = pickRandomFromList(mushrooms, rand);
                        targetScale = 0.25f + rand.nextFloat() * 0.25f;
                    } else if (roll < 0.50f && !rocks.isEmpty()) {
                        scatterModel = pickRandomFromList(rocks, rand); // Mossy stones
                        targetScale = 0.30f + rand.nextFloat() * 0.30f;
                    }
                } else if (mat == TerrariumCell.Material.SAND) {
                    // Desert Pebbles & Small Scrub
                    if (roll < 0.25f && !rocks.isEmpty()) {
                        scatterModel = pickRandomFromList(rocks, rand);
                        targetScale = 0.20f + rand.nextFloat() * 0.30f;
                    }
                } else if (mat == TerrariumCell.Material.EARTH || mat == TerrariumCell.Material.SILT) {
                    // Natural Wild Grass Tufts & Small Bushes
                    if (roll < 0.35f && !flowers.isEmpty()) {
                        scatterModel = pickRandomFromList(flowers, rand);
                        targetScale = 0.30f + rand.nextFloat() * 0.35f;
                    } else if (roll < 0.50f && !bushes.isEmpty()) {
                        scatterModel = pickRandomFromList(bushes, rand);
                        targetScale = 0.45f + rand.nextFloat() * 0.40f;
                    }
                }

                if (scatterModel != null) {
                    Spatial instance = scatterModel.clone();
                    normalizeAndPositionModel(instance, jx, elev, jz, targetScale, rand);
                    applySeasonalTint(instance, season, biome);
                    rootNode.attachChild(instance);
                }
            }
        }
    }

    private void createRealisticFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Season season, Random rand) {
        Spatial chosenModel = null;
        float targetHeight = 8.5f * plant.growth;

        switch (plant.type) {
            case TREE -> {
                chosenModel = pickModelForBiome(biome, rand, true);
                String name = (chosenModel != null && chosenModel.getName() != null) ? chosenModel.getName().toLowerCase() : "";
                if (name.contains("stump") || name.contains("log")) {
                    targetHeight = (0.8f + rand.nextFloat() * 0.5f) * Math.max(0.4f, plant.growth);
                } else if (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
                    targetHeight = (6.0f + rand.nextFloat() * 3.0f) * Math.max(0.4f, plant.growth);
                } else {
                    targetHeight = (7.5f + rand.nextFloat() * 3.0f) * Math.max(0.4f, plant.growth);
                }
            }
            case SHRUB -> {
                targetHeight = (1.5f + rand.nextFloat() * 0.9f) * Math.max(0.4f, plant.growth);
                chosenModel = pickRandomFromList(bushes, rand);
            }
            case FLOWER -> {
                targetHeight = (0.50f + rand.nextFloat() * 0.40f) * Math.max(0.4f, plant.growth);
                chosenModel = pickRandomFromList(flowers, rand);
            }
            case MOSS, GRASS -> {
                targetHeight = (0.35f + rand.nextFloat() * 0.30f) * Math.max(0.4f, plant.growth);
                chosenModel = pickRandomFromList(flowers, rand);
            }
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            normalizeAndPositionModel(instance, x, y, z, Math.max(0.35f, targetHeight), rand);
            applySeasonalTint(instance, season, biome);
            rootNode.attachChild(instance);
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

    private void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int index) {
        Spatial chosenModel = pickModelForBiome(biome, rand, false);
        float targetHeight;

        if (biome == Biome.DESERT && chosenModel == cactusModel) {
            targetHeight = 3.0f + rand.nextFloat() * 2.0f;
        } else if (chosenModel == bambooModel) {
            targetHeight = 3.2f + rand.nextFloat() * 1.8f;
        } else if (chosenModel != null && (rocks.contains(chosenModel) || flowers.contains(chosenModel) || mushrooms.contains(chosenModel))) {
            targetHeight = 0.7f + rand.nextFloat() * 0.7f;
        } else if (chosenModel != null && bushes.contains(chosenModel)) {
            targetHeight = 1.4f + rand.nextFloat() * 1.0f;
        } else if (chosenModel != null && palmTrees.contains(chosenModel)) {
            targetHeight = 4.0f + rand.nextFloat() * 2.5f;
        } else {
            targetHeight = 8.5f + rand.nextFloat() * 4.0f;
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            normalizeAndPositionModel(instance, x, y, z, targetHeight, rand);
            applySeasonalTint(instance, season, biome);
            rootNode.attachChild(instance);
        } else {
            createProceduralTree3D(x, y, z, rand.nextInt(5), rand, season);
        }
    }

    private Spatial pickModelForBiome(Biome biome, Random rand, boolean preferTrees) {
        switch (biome) {
            case DESERT -> {
                float r = rand.nextFloat();
                if (r < 0.45f && cactusModel != null) return cactusModel;
                if (r < 0.75f && !deadTrees.isEmpty()) return pickRandomFromList(deadTrees, rand);
                if (!rocks.isEmpty()) return pickRandomFromList(rocks, rand);
                return cactusModel;
            }
            case TROPICAL -> {
                float r = rand.nextFloat();
                if (r < 0.40f && !palmTrees.isEmpty()) return pickRandomFromList(palmTrees, rand);
                if (r < 0.65f && bambooModel != null) return bambooModel;
                if (r < 0.85f && !deciduousTrees.isEmpty()) return pickRandomFromList(deciduousTrees, rand);
                if (!flowers.isEmpty()) return pickRandomFromList(flowers, rand);
                return pickRandomFromList(deciduousTrees, rand);
            }
            case ALPINE_SNOW, TUNDRA -> {
                float r = rand.nextFloat();
                if (r < 0.65f && !coniferTrees.isEmpty()) return pickRandomFromList(coniferTrees, rand);
                if (r < 0.85f && !rocks.isEmpty()) return pickRandomFromList(rocks, rand);
                if (!deadTrees.isEmpty()) return pickRandomFromList(deadTrees, rand);
                return pickRandomFromList(coniferTrees, rand);
            }
            case MEDITERRANEAN -> {
                float r = rand.nextFloat();
                if (r < 0.50f && !coniferTrees.isEmpty()) return pickRandomFromList(coniferTrees, rand);
                if (r < 0.80f && !bushes.isEmpty()) return pickRandomFromList(bushes, rand);
                if (!rocks.isEmpty()) return pickRandomFromList(rocks, rand);
                return pickRandomFromList(coniferTrees, rand);
            }
            case FOREST, GRASSLAND, WETLAND -> {
            }
        }
        float r = rand.nextFloat();
        if (r < 0.55f && !deciduousTrees.isEmpty()) return pickRandomFromList(deciduousTrees, rand);
        if (r < 0.75f && !coniferTrees.isEmpty()) return pickRandomFromList(coniferTrees, rand);
        if (r < 0.90f && !bushes.isEmpty()) return pickRandomFromList(bushes, rand);
        if (!flowers.isEmpty()) return pickRandomFromList(flowers, rand);
        return pickRandomFromList(deciduousTrees, rand);
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

        if (bv instanceof BoundingBox bbox) {
            modelHeight = Math.max(0.1f, bbox.getYExtent() * 2.0f);
            float scale = targetHeight / modelHeight;
            float minY = bbox.getCenter().y - bbox.getYExtent();
            baseYOffset = -minY * scale;
            spatial.setLocalScale(scale);
        } else {
            spatial.setLocalScale(targetHeight / 2.0f);
        }

        spatial.setLocalTranslation(x, y + baseYOffset, z);
        spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        spatial.setUserData("BaseRotY", rotY);
        spatial.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
    }

    private void applySeasonalTint(Spatial spatial, Season season, Biome biome) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Diffuse") != null) {
                String name = geom.getName() != null ? geom.getName().toLowerCase() : "";
                boolean isRockOrWood = name.contains("stone") || name.contains("rock") || name.contains("log")
                        || name.contains("stump") || name.contains("trunk") || name.contains("branch")
                        || name.contains("mushroom") || name.contains("cactus") || name.contains("bamboo");

                if (!isRockOrWood) {
                    ColorRGBA seasonalColor = getSeasonFoliageColor(season, biome);
                    if (seasonalColor != null) {
                        // If geometry has a diffuse texture map, apply light tint blend rather than overriding solid color
                        if (mat.getMaterialDef().getMaterialParam("DiffuseMap") != null && mat.getParam("DiffuseMap") != null) {
                            mat.setColor("Diffuse", ColorRGBA.White.mult(0.7f).add(seasonalColor.mult(0.3f)));
                        } else {
                            mat.setColor("Diffuse", seasonalColor);
                        }
                        mat.setColor("Ambient", ColorRGBA.White.mult(0.45f));
                    }
                } else {
                    mat.setColor("Diffuse", ColorRGBA.White);
                    mat.setColor("Ambient", new ColorRGBA(0.45f, 0.45f, 0.45f, 1.0f));
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
            return new ColorRGBA(0.48f, 0.62f, 0.28f, 1.0f); // Olive desert tone
        }
        return switch (season) {
            case SPRING -> new ColorRGBA(0.32f, 0.78f, 0.25f, 1.0f); // Fresh budding green
            case SUMMER -> new ColorRGBA(0.14f, 0.58f, 0.18f, 1.0f); // Lush chlorophyll
            case FALL -> new ColorRGBA(0.85f, 0.46f, 0.10f, 1.0f);   // Golden amber / autumn foliage
            case WINTER -> (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA)
                    ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1.0f)       // Snowy frosty white/green
                    : new ColorRGBA(0.42f, 0.50f, 0.38f, 1.0f);       // Muted winter evergreen
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
        rootNode.attachChild(treeNode);
    }

    private void createProceduralTreeScientific(float x, float y, float z, Biome biome, Random rand) {
        Node treeNode = new Node("TreeScientific");
        treeNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        trunkMat.setColor("Diffuse", new ColorRGBA(0.40f, 0.25f, 0.12f, 1.0f));
        trunkMat.setColor("Ambient", new ColorRGBA(0.25f, 0.15f, 0.08f, 1.0f));
        trunkMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        Cylinder trunkMesh = new Cylinder(8, 12, 0.15f, 0.15f, 6.0f, true, false);
        Geometry trunkGeom = new Geometry("SciTrunk", trunkMesh);
        trunkGeom.setMaterial(trunkMat);
        trunkGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        trunkGeom.setLocalTranslation(0, 3.0f, 0);
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

        Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        leafMat.setBoolean("UseMaterialColors", true);
        leafMat.setColor("Diffuse", new ColorRGBA(0.12f, 0.65f, 0.28f, 1.0f));
        leafMat.setColor("Ambient", new ColorRGBA(0.08f, 0.40f, 0.18f, 1.0f));
        leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(12, 12, 1.8f);
        Geometry crownGeom = new Geometry("SciCrownLAI", crownMesh);
        crownGeom.setMaterial(leafMat);
        crownGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        crownGeom.setLocalTranslation(0, 5.5f, 0);
        treeNode.attachChild(crownGeom);

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
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

        if (biome == Biome.DESERT) {
            // Authentic Ribbed Micro-Voxel Saguaro Cactus
            Material cactusMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            cactusMat.setBoolean("UseMaterialColors", true);
            cactusMat.setColor("Diffuse", new ColorRGBA(0.22f, 0.60f, 0.24f, 1f));
            cactusMat.setColor("Ambient", new ColorRGBA(0.12f, 0.36f, 0.14f, 1f));
            cactusMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            // Central Trunk (4.5m) with ribbed cross geometry
            treeNode.attachChild(createMicroVoxel("CactusCore", 0.60f, 4.5f, 0.60f, cactusMat, 0, 2.25f, 0));
            treeNode.attachChild(createMicroVoxel("CactusRibN", 0.30f, 4.2f, 0.20f, cactusMat, 0, 2.10f, 0.35f));
            treeNode.attachChild(createMicroVoxel("CactusRibS", 0.30f, 4.2f, 0.20f, cactusMat, 0, 2.10f, -0.35f));
            treeNode.attachChild(createMicroVoxel("CactusRibE", 0.20f, 4.2f, 0.30f, cactusMat, 0.35f, 2.10f, 0));
            treeNode.attachChild(createMicroVoxel("CactusRibW", 0.20f, 4.2f, 0.30f, cactusMat, -0.35f, 2.10f, 0));

            // Left Arm (Joint + Upward column)
            treeNode.attachChild(createMicroVoxel("CactusArmJointL", 0.60f, 0.40f, 0.40f, cactusMat, -0.55f, 2.2f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmUpL", 0.40f, 1.8f, 0.40f, cactusMat, -0.85f, 3.1f, 0));

            // Right Arm (Joint + Upward column at offset height)
            treeNode.attachChild(createMicroVoxel("CactusArmJointR", 0.60f, 0.40f, 0.40f, cactusMat, 0.55f, 2.8f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmUpR", 0.40f, 1.6f, 0.40f, cactusMat, 0.85f, 3.6f, 0));

        } else if (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            // Hierarchical Micro-Voxel Spruce/Pine Tree (4 stepped needle tiers)
            int trunkHeight = 7;
            treeNode.attachChild(createMicroVoxel("SpruceTrunk", 0.50f, 6.8f, 0.50f, woodMat, 0, 3.4f, 0));
            // Base Root Flares
            treeNode.attachChild(createMicroVoxel("RootN", 0.30f, 0.40f, 0.30f, woodMat, 0, 0.20f, 0.35f));
            treeNode.attachChild(createMicroVoxel("RootS", 0.30f, 0.40f, 0.30f, woodMat, 0, 0.20f, -0.35f));
            treeNode.attachChild(createMicroVoxel("RootE", 0.30f, 0.40f, 0.30f, woodMat, 0.35f, 0.20f, 0));
            treeNode.attachChild(createMicroVoxel("RootW", 0.30f, 0.40f, 0.30f, woodMat, -0.35f, 0.20f, 0));

            Material pineLeafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            pineLeafMat.setBoolean("UseMaterialColors", true);
            ColorRGBA pineCol = (season == Season.WINTER) ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1f) : new ColorRGBA(0.12f, 0.42f, 0.18f, 1f);
            pineLeafMat.setColor("Diffuse", pineCol);
            pineLeafMat.setColor("Ambient", pineCol.mult(0.6f));
            pineLeafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            // Tier 1: Wide Lower Skirt (Stepped cross + overhangs)
            treeNode.attachChild(createMicroVoxel("PineT1_Core", 2.4f, 0.60f, 2.4f, pineLeafMat, 0, 3.5f, 0));
            treeNode.attachChild(createMicroVoxel("PineT1_OverN", 1.4f, 0.45f, 0.60f, pineLeafMat, 0, 3.3f, 1.4f));
            treeNode.attachChild(createMicroVoxel("PineT1_OverS", 1.4f, 0.45f, 0.60f, pineLeafMat, 0, 3.3f, -1.4f));
            treeNode.attachChild(createMicroVoxel("PineT1_OverE", 0.60f, 0.45f, 1.4f, pineLeafMat, 1.4f, 3.3f, 0));
            treeNode.attachChild(createMicroVoxel("PineT1_OverW", 0.60f, 0.45f, 1.4f, pineLeafMat, -1.4f, 3.3f, 0));

            // Tier 2: Mid Skirt
            treeNode.attachChild(createMicroVoxel("PineT2_Core", 1.8f, 0.60f, 1.8f, pineLeafMat, 0, 4.7f, 0));
            treeNode.attachChild(createMicroVoxel("PineT2_OverN", 1.0f, 0.40f, 0.45f, pineLeafMat, 0, 4.5f, 1.05f));
            treeNode.attachChild(createMicroVoxel("PineT2_OverS", 1.0f, 0.40f, 0.45f, pineLeafMat, 0, 4.5f, -1.05f));
            treeNode.attachChild(createMicroVoxel("PineT2_OverE", 0.45f, 0.40f, 1.0f, pineLeafMat, 1.05f, 4.5f, 0));
            treeNode.attachChild(createMicroVoxel("PineT2_OverW", 0.45f, 0.40f, 1.0f, pineLeafMat, -1.05f, 4.5f, 0));

            // Tier 3: Upper Tier
            treeNode.attachChild(createMicroVoxel("PineT3_Core", 1.2f, 0.60f, 1.2f, pineLeafMat, 0, 5.8f, 0));

            // Tier 4: Spire Peak
            treeNode.attachChild(createMicroVoxel("PinePeak", 0.50f, 0.80f, 0.50f, pineLeafMat, 0, 6.8f, 0));

        } else {
            // Authentic Stepped Micro-Voxel Oak / Birch Tree
            treeNode.attachChild(createMicroVoxel("OakTrunk", 0.55f, 4.8f, 0.55f, woodMat, 0, 2.4f, 0));
            // Base Root Spurs
            treeNode.attachChild(createMicroVoxel("OakRootN", 0.35f, 0.50f, 0.35f, woodMat, 0, 0.25f, 0.35f));
            treeNode.attachChild(createMicroVoxel("OakRootS", 0.35f, 0.50f, 0.35f, woodMat, 0, 0.25f, -0.35f));
            treeNode.attachChild(createMicroVoxel("OakRootE", 0.35f, 0.50f, 0.35f, woodMat, 0.35f, 0.25f, 0));
            treeNode.attachChild(createMicroVoxel("OakRootW", 0.35f, 0.50f, 0.35f, woodMat, -0.35f, 0.25f, 0));

            // Branches
            treeNode.attachChild(createMicroVoxel("OakBranchL", 0.50f, 0.30f, 0.30f, woodMat, -0.45f, 3.5f, 0.15f));
            treeNode.attachChild(createMicroVoxel("OakBranchR", 0.50f, 0.30f, 0.30f, woodMat, 0.45f, 3.8f, -0.15f));

            ColorRGBA baseCol = getSeasonFoliageColor(season, biome);
            Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafMat.setBoolean("UseMaterialColors", true);
            leafMat.setColor("Diffuse", baseCol);
            leafMat.setColor("Ambient", baseCol.mult(0.6f));
            leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            Material leafHighlightMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafHighlightMat.setBoolean("UseMaterialColors", true);
            ColorRGBA highlightCol = baseCol.mult(1.12f);
            leafHighlightMat.setColor("Diffuse", highlightCol);
            leafHighlightMat.setColor("Ambient", highlightCol.mult(0.6f));
            leafHighlightMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            // Layer 1: Bottom Foliage Overhangs (y = 3.2 - 3.8)
            treeNode.attachChild(createMicroVoxel("OakL1_Center", 2.2f, 0.60f, 2.2f, leafMat, 0, 3.5f, 0));
            treeNode.attachChild(createMicroVoxel("OakL1_North", 1.4f, 0.50f, 0.60f, leafHighlightMat, 0, 3.4f, 1.25f));
            treeNode.attachChild(createMicroVoxel("OakL1_South", 1.4f, 0.50f, 0.60f, leafMat, 0, 3.4f, -1.25f));
            treeNode.attachChild(createMicroVoxel("OakL1_East", 0.60f, 0.50f, 1.4f, leafHighlightMat, 1.25f, 3.4f, 0));
            treeNode.attachChild(createMicroVoxel("OakL1_West", 0.60f, 0.50f, 1.4f, leafMat, -1.25f, 3.4f, 0));

            // Layer 2: Main Dense Stepped Canopy (y = 4.1 - 4.9)
            treeNode.attachChild(createMicroVoxel("OakL2_Main", 2.8f, 0.80f, 2.8f, leafMat, 0, 4.3f, 0));
            treeNode.attachChild(createMicroVoxel("OakL2_CornerNE", 0.70f, 0.65f, 0.70f, leafHighlightMat, 1.15f, 4.3f, 1.15f));
            treeNode.attachChild(createMicroVoxel("OakL2_CornerSW", 0.70f, 0.65f, 0.70f, leafMat, -1.15f, 4.3f, -1.15f));

            // Layer 3: Upper Stepped Canopy (y = 5.0 - 5.6)
            treeNode.attachChild(createMicroVoxel("OakL3_Core", 2.0f, 0.70f, 2.0f, leafHighlightMat, 0, 5.2f, 0));
            treeNode.attachChild(createMicroVoxel("OakL3_SideN", 1.1f, 0.50f, 0.45f, leafMat, 0, 5.1f, 1.0f));
            treeNode.attachChild(createMicroVoxel("OakL3_SideS", 1.1f, 0.50f, 0.45f, leafHighlightMat, 0, 5.1f, -1.0f));

            // Layer 4: Crown Cap (y = 5.8 - 6.3)
            treeNode.attachChild(createMicroVoxel("OakL4_Cap", 1.1f, 0.50f, 1.1f, leafMat, 0, 5.9f, 0));
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }

    public void update(WeatherSystem weather, float tpf) {
        if (!visible || rootNode.getChildren().isEmpty()) return;

        swayTime += tpf;

        float windSpeedMs = 3.3f;
        float windAngleDeg = 45.0f;
        if (weather != null) {
            windSpeedMs = weather.getWindSpeedMs();
            windAngleDeg = weather.getWindDirectionAngle();
        }

        float windIntensity = Math.min(1.2f, windSpeedMs / 15.0f);
        float swayFrequency = 1.1f + windIntensity * 0.6f;
        float swayAmplitude = 0.006f + windIntensity * 0.014f;

        float windRad = FastMath.DEG_TO_RAD * windAngleDeg;
        float windCos = FastMath.cos(windRad);
        float windSin = FastMath.sin(windRad);

        for (Spatial child : rootNode.getChildren()) {
            if (child instanceof Node treeNode) {
                Vector3f pos = treeNode.getLocalTranslation();
                float spatialPhase = pos.x * 0.18f + pos.z * 0.18f;

                float primarySin = FastMath.sin(swayTime * swayFrequency + spatialPhase);
                float secondaryCos = FastMath.cos(swayTime * swayFrequency * 1.35f + spatialPhase) * 0.3f;

                float tiltAngle = (primarySin + secondaryCos) * swayAmplitude;

                Float baseRotY = (Float) treeNode.getUserData("BaseRotY");
                if (baseRotY == null) {
                    baseRotY = 0.0f;
                }

                float pitch = windCos * tiltAngle;
                float roll = windSin * tiltAngle;

                Quaternion baseQuat = new Quaternion().fromAngles(0, baseRotY, 0);
                Quaternion swayQuat = new Quaternion().fromAngles(pitch, 0, roll);
                treeNode.setLocalRotation(baseQuat.mult(swayQuat));
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
            float targetHeight = Math.max(nestAltitude + 2.5f, 10.0f);
            BoundingBox bbox = (BoundingBox) treeInstance.getWorldBound();
            float naturalHeight = bbox != null ? (bbox.getYExtent() * 2.0f) : 1.0f;
            float scale = (naturalHeight > 0.01f) ? (targetHeight / naturalHeight) : 1.0f;

            treeInstance.setLocalScale(scale);
            treeInstance.setLocalTranslation(x, 0.5f, y);
            treeInstance.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            treeInstance.setUserData("BaseRotY", 0.0f);
            rootNode.attachChild(treeInstance);
        }
    }

    public void setUVVisionMode(boolean enabled) {
        this.uvVisionMode = enabled;
        applyUVColoration(rootNode, enabled);
    }

    public boolean isUVVisionMode() {
        return uvVisionMode;
    }

    private void applyUVColoration(Spatial spatial, boolean uv) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                if (uv) {
                    mat.setColor("Diffuse", new ColorRGBA(0.45f, 0.20f, 0.85f, 1.0f));
                    mat.setColor("Ambient", new ColorRGBA(0.25f, 0.10f, 0.65f, 1.0f));
                }
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applyUVColoration(child, uv);
            }
        }
    }
}

