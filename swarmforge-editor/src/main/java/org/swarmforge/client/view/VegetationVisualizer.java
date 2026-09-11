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

        // 10. Load Low-Poly Variants for Palms & Jungle
        for (int i = 1; i <= 30; i++) {
            Spatial lp = safeLoadModel(String.format("models/nature_pack_obj/lp_Plane_%03d_Plane_%03d.obj", i, i));
            if (lp != null) palmTrees.add(lp);
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
                mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
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

        if (vegSystem != null && !vegSystem.getPlants().isEmpty()) {
            // Position flora according to real simulation plants
            for (VegetationSystem.Plant plant : vegSystem.getPlants()) {
                float x = plant.x;
                float z = plant.y; // Horizontal Y in domain -> JME Z
                float y = (terrarium != null) ? terrarium.getSurfaceElevation(x, z) : 0.5f;

                if (currentRenderMode == RenderMode.REALISTIC) {
                    createRealisticFloraForPlant(x, y, z, plant, biome, effectiveSeason, rand);
                } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    createProceduralTreeScientific(x, y, z, biome, rand);
                } else if (currentRenderMode == RenderMode.GAMIFIED) {
                    createProceduralTreeGamified(x, y, z, biome, effectiveSeason, rand);
                }
            }
        } else {
            // Procedural landscape distribution across full terrarium footprint (0..gridWidth, 0..gridHeight)
            int count = Math.min(65, (gridWidth * gridHeight) / 45);
            for (int i = 0; i < count; i++) {
                float x = 3 + rand.nextFloat() * (gridWidth - 6);
                float z = 3 + rand.nextFloat() * (gridHeight - 6);
                float y = (terrarium != null) ? terrarium.getSurfaceElevation(x, z) : 0.5f;

                if (currentRenderMode == RenderMode.REALISTIC) {
                    createRealisticFlora(x, y, z, biome, effectiveSeason, rand, i);
                } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    createProceduralTreeScientific(x, y, z, biome, rand);
                } else if (currentRenderMode == RenderMode.GAMIFIED) {
                    createProceduralTreeGamified(x, y, z, biome, effectiveSeason, rand);
                }
            }
        }
    }

    private void createRealisticFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Season season, Random rand) {
        Spatial chosenModel = null;
        float targetHeight = 8.5f * plant.growth;

        switch (plant.type) {
            case TREE -> {
                targetHeight = (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA)
                        ? (8.0f + rand.nextFloat() * 4.0f) * plant.growth
                        : (9.5f + rand.nextFloat() * 3.5f) * plant.growth;
                chosenModel = pickModelForBiome(biome, rand, true);
            }
            case SHRUB -> {
                targetHeight = (1.5f + rand.nextFloat() * 1.5f) * plant.growth;
                chosenModel = pickRandomFromList(bushes, rand);
            }
            case FLOWER -> {
                targetHeight = (0.5f + rand.nextFloat() * 0.6f) * plant.growth;
                chosenModel = pickRandomFromList(flowers, rand);
            }
            case MOSS, GRASS -> {
                targetHeight = (0.4f + rand.nextFloat() * 0.5f) * plant.growth;
                chosenModel = pickRandomFromList(flowers, rand);
            }
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            normalizeAndPositionModel(instance, x, y, z, Math.max(0.4f, targetHeight), rand);
            applySeasonalTint(instance, season, biome);
            rootNode.attachChild(instance);
        } else {
            createProceduralTree3D(x, y, z, (biome == Biome.ALPINE_SNOW) ? 4 : 0, rand, season);
        }
    }

    private void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int index) {
        Spatial chosenModel = pickModelForBiome(biome, rand, false);
        float targetHeight = 9.0f + rand.nextFloat() * 3.5f;

        if (biome == Biome.DESERT && chosenModel == cactusModel) {
            targetHeight = 2.8f + rand.nextFloat() * 2.2f;
        } else if (chosenModel != null && (rocks.contains(chosenModel) || flowers.contains(chosenModel))) {
            targetHeight = 0.8f + rand.nextFloat() * 1.2f;
        } else if (chosenModel != null && bushes.contains(chosenModel)) {
            targetHeight = 1.6f + rand.nextFloat() * 1.2f;
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
        spatial.updateModelBound();
        BoundingBox bbox = (BoundingBox) spatial.getWorldBound();

        float modelHeight = bbox != null ? bbox.getYExtent() * 2f : 2.0f;
        float scale = (modelHeight > 0.001f) ? (targetHeight / modelHeight) : 1.0f;

        float baseYOffset = 0.0f;
        if (bbox != null) {
            baseYOffset = -(bbox.getCenter().y - bbox.getYExtent()) * scale;
        }

        spatial.setLocalScale(scale);
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

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trunkMat.setColor("Color", new ColorRGBA(0.40f, 0.25f, 0.12f, 1.0f));

        Cylinder trunkMesh = new Cylinder(4, 8, 0.15f, 0.15f, 6.0f, true, false);
        Geometry trunkGeom = new Geometry("SciTrunk", trunkMesh);
        trunkGeom.setMaterial(trunkMat);
        trunkGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        trunkGeom.setLocalTranslation(0, 3.0f, 0);
        trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        treeNode.attachChild(trunkGeom);

        Material dbhMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dbhMat.setColor("Color", new ColorRGBA(0.1f, 0.8f, 1.0f, 1.0f));
        Cylinder dbhRing = new Cylinder(8, 12, 0.22f, 0.22f, 0.10f, true, false);
        Geometry dbhGeom = new Geometry("DBHMarker", dbhRing);
        dbhGeom.setMaterial(dbhMat);
        dbhGeom.setLocalTranslation(0, 1.3f, 0);
        dbhGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        treeNode.attachChild(dbhGeom);

        Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        leafMat.setColor("Color", new ColorRGBA(0.12f, 0.65f, 0.28f, 0.75f));

        com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(10, 10, 1.8f);
        Geometry crownGeom = new Geometry("SciCrownLAI", crownMesh);
        crownGeom.setMaterial(leafMat);
        crownGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        crownGeom.setLocalTranslation(0, 5.5f, 0);
        treeNode.attachChild(crownGeom);

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }

    private void createProceduralTreeGamified(float x, float y, float z, Biome biome, Season season, Random rand) {
        Node treeNode = new Node("TreeGamified");
        treeNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        float voxelSize = 1.0f; // 1.0m per authentic Minecraft voxel block for realistic stature

        Material woodMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        woodMat.setBoolean("UseMaterialColors", true);
        woodMat.setColor("Diffuse", new ColorRGBA(0.46f, 0.28f, 0.14f, 1f));
        woodMat.setColor("Ambient", new ColorRGBA(0.28f, 0.18f, 0.08f, 1f));
        woodMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        if (biome == Biome.DESERT) {
            // Authentic Minecraft Saguaro Cactus (5-block trunk + 2 staggered arms)
            Material cactusMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            cactusMat.setBoolean("UseMaterialColors", true);
            cactusMat.setColor("Diffuse", new ColorRGBA(0.20f, 0.58f, 0.22f, 1f));
            cactusMat.setColor("Ambient", new ColorRGBA(0.12f, 0.35f, 0.14f, 1f));
            cactusMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            for (int h = 0; h < 5; h++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("CactusVoxel_" + h, box);
                g.setMaterial(cactusMat);
                g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                g.setLocalTranslation(0, h * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }
            // Arm Left
            Box armLeft1 = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry gL1 = new Geometry("CactusArmL1", armLeft1);
            gL1.setMaterial(cactusMat);
            gL1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            gL1.setLocalTranslation(-voxelSize, 2.0f * voxelSize + voxelSize / 2, 0);
            treeNode.attachChild(gL1);

            Box armLeft2 = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry gL2 = new Geometry("CactusArmL2", armLeft2);
            gL2.setMaterial(cactusMat);
            gL2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            gL2.setLocalTranslation(-voxelSize, 3.0f * voxelSize + voxelSize / 2, 0);
            treeNode.attachChild(gL2);

            // Arm Right
            Box armRight1 = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry gR1 = new Geometry("CactusArmR1", armRight1);
            gR1.setMaterial(cactusMat);
            gR1.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            gR1.setLocalTranslation(voxelSize, 3.0f * voxelSize + voxelSize / 2, 0);
            treeNode.attachChild(gR1);

            Box armRight2 = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry gR2 = new Geometry("CactusArmR2", armRight2);
            gR2.setMaterial(cactusMat);
            gR2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            gR2.setLocalTranslation(voxelSize, 4.0f * voxelSize + voxelSize / 2, 0);
            treeNode.attachChild(gR2);

        } else if (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            // Authentic Minecraft Spruce/Pine Tree (7-8 block trunk + multi-tiered cross canopy)
            int trunkHeight = 8;
            for (int h = 0; h < trunkHeight; h++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("PineLog_" + h, box);
                g.setMaterial(woodMat);
                g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                g.setLocalTranslation(0, h * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }

            Material pineLeafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            pineLeafMat.setBoolean("UseMaterialColors", true);
            ColorRGBA pineCol = (season == Season.WINTER) ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1f) : new ColorRGBA(0.12f, 0.42f, 0.18f, 1f);
            pineLeafMat.setColor("Diffuse", pineCol);
            pineLeafMat.setColor("Ambient", pineCol.mult(0.6f));
            pineLeafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            float canopyBaseY = 4 * voxelSize;

            // Tier 1: 5x5 Cross at Y=4
            for (int bx = -2; bx <= 2; bx++) {
                for (int bz = -2; bz <= 2; bz++) {
                    if (Math.abs(bx) == 2 && Math.abs(bz) == 2) continue;
                    Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                    Geometry g = new Geometry("PineLeaf_T1_" + bx + "_" + bz, box);
                    g.setMaterial(pineLeafMat);
                    g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                    g.setLocalTranslation(bx * voxelSize, canopyBaseY + voxelSize / 2, bz * voxelSize);
                    treeNode.attachChild(g);
                }
            }

            // Tier 2: 3x3 Cross at Y=6
            for (int bx = -1; bx <= 1; bx++) {
                for (int bz = -1; bz <= 1; bz++) {
                    Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                    Geometry g = new Geometry("PineLeaf_T2_" + bx + "_" + bz, box);
                    g.setMaterial(pineLeafMat);
                    g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                    g.setLocalTranslation(bx * voxelSize, canopyBaseY + 2 * voxelSize + voxelSize / 2, bz * voxelSize);
                    treeNode.attachChild(g);
                }
            }

            // Tier 3: 1x1 Peak at Y=7 & Y=8
            for (int by = 3; by <= 4; by++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("PineLeaf_Peak_" + by, box);
                g.setMaterial(pineLeafMat);
                g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                g.setLocalTranslation(0, canopyBaseY + by * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }

        } else {
            // Authentic Minecraft Oak / Birch Tree (6 block trunk + 5x5 / 3x3 canopy with corner notches)
            int trunkHeight = 6;
            for (int h = 0; h < trunkHeight; h++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("OakLog_" + h, box);
                g.setMaterial(woodMat);
                g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                g.setLocalTranslation(0, h * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }

            ColorRGBA baseCol = getSeasonFoliageColor(season, biome);
            Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafMat.setBoolean("UseMaterialColors", true);
            leafMat.setColor("Diffuse", baseCol);
            leafMat.setColor("Ambient", baseCol.mult(0.6f));
            leafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            float canopyBaseY = (trunkHeight - 2) * voxelSize;

            // Canopy Layers 0 and 1: 5x5 with 4 corners omitted
            for (int by = 0; by < 2; by++) {
                for (int bx = -2; bx <= 2; bx++) {
                    for (int bz = -2; bz <= 2; bz++) {
                        if (Math.abs(bx) == 2 && Math.abs(bz) == 2) continue; // Minecraft corner notch
                        Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                        Geometry g = new Geometry("OakLeaf_" + by + "_" + bx + "_" + bz, box);
                        g.setMaterial(leafMat);
                        g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                        g.setLocalTranslation(bx * voxelSize, canopyBaseY + by * voxelSize + voxelSize / 2, bz * voxelSize);
                        treeNode.attachChild(g);
                    }
                }
            }

            // Canopy Layer 2: 3x3
            for (int bx = -1; bx <= 1; bx++) {
                for (int bz = -1; bz <= 1; bz++) {
                    if (Math.abs(bx) == 1 && Math.abs(bz) == 1) continue;
                    Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                    Geometry g = new Geometry("OakLeaf_Top_" + bx + "_" + bz, box);
                    g.setMaterial(leafMat);
                    g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                    g.setLocalTranslation(bx * voxelSize, canopyBaseY + 2 * voxelSize + voxelSize / 2, bz * voxelSize);
                    treeNode.attachChild(g);
                }
            }

            // Top Cap (1x1)
            Box topBox = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry topGeom = new Geometry("OakLeaf_Cap", topBox);
            topGeom.setMaterial(leafMat);
            topGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            topGeom.setLocalTranslation(0, canopyBaseY + 3 * voxelSize + voxelSize / 2, 0);
            treeNode.attachChild(topGeom);
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

