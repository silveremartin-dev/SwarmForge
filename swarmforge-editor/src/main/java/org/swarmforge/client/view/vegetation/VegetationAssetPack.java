/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view.vegetation;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.swarmforge.core.world.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D Nature Pack Asset Repository and Loader for trees, plants, rocks, and ground detritus.
 */
public class VegetationAssetPack {

    private final AssetManager assetManager;

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

    public VegetationAssetPack(AssetManager assetManager) {
        this.assetManager = assetManager;
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

        // 3. Dead Trees, Logs & Stumps
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

        // 6. Mushrooms
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

        // Mossy Rocks & Boulders
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

        // 11. Low-Poly Ground Foliage
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

    public Spatial safeLoadModel(String path) {
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
                if (mat.getMaterialDef().getMaterialParam("UseMaterialColors") != null) {
                    mat.setBoolean("UseMaterialColors", true);
                }
                if (mat.getMaterialDef().getMaterialParam("Ambient") != null && mat.getParam("Ambient") == null) {
                    mat.setColor("Ambient", new ColorRGBA(0.85f, 0.85f, 0.85f, 1.0f));
                }
                if (mat.getMaterialDef().getMaterialParam("Diffuse") != null && mat.getParam("Diffuse") == null) {
                    mat.setColor("Diffuse", ColorRGBA.White);
                }
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

    public Spatial pickRandomFromList(List<Spatial> list, Random rand) {
        if (list == null || list.isEmpty()) return null;
        return list.get(rand.nextInt(list.size()));
    }

    public Spatial pickModelForBiome(Biome biome, Random rand, boolean treesOnly, int bambooPct, int cactusPct) {
        if (treesOnly) {
            return switch (biome) {
                case DESERT -> (cactusPct > 0 && cactusModel != null && rand.nextFloat() < 0.65f) ? cactusModel : (!deadTrees.isEmpty() ? pickRandomFromList(deadTrees, rand) : cactusModel);
                case TROPICAL -> {
                    float r = rand.nextFloat();
                    if (r < 0.50f && !palmTrees.isEmpty()) yield pickRandomFromList(palmTrees, rand);
                    if (r < 0.70f && bambooPct > 0 && bambooModel != null) yield bambooModel;
                    if (!deciduousTrees.isEmpty()) yield pickRandomFromList(deciduousTrees, rand);
                    yield pickRandomFromList(palmTrees, rand);
                }
                case ALPINE_SNOW, TUNDRA -> !coniferTrees.isEmpty() ? pickRandomFromList(coniferTrees, rand) : pickRandomFromList(deadTrees, rand);
                case MEDITERRANEAN -> (rand.nextFloat() < 0.65f && !coniferTrees.isEmpty()) ? pickRandomFromList(coniferTrees, rand) : pickRandomFromList(deciduousTrees, rand);
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

    public List<Spatial> getDeciduousTrees() { return deciduousTrees; }
    public List<Spatial> getConiferTrees() { return coniferTrees; }
    public List<Spatial> getPalmTrees() { return palmTrees; }
    public List<Spatial> getDeadTrees() { return deadTrees; }
    public List<Spatial> getDeadwoodLogs() { return deadwoodLogs; }
    public List<Spatial> getBushes() { return bushes; }
    public List<Spatial> getFlowers() { return flowers; }
    public List<Spatial> getMushrooms() { return mushrooms; }
    public List<Spatial> getRocks() { return rocks; }
    public List<Spatial> getMossyRocks() { return mossyRocks; }
    public List<Spatial> getGrassTufts() { return grassTufts; }
    public List<Spatial> getTwigsAndBranches() { return twigsAndBranches; }
    public Spatial getCactusModel() { return cactusModel; }
    public Spatial getBambooModel() { return bambooModel; }
    public Spatial getBeehiveModel() { return beehiveModel; }
}
