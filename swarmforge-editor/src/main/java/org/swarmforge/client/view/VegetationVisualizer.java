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
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D Vegetation and Flora visualizer for JMonkeyEngine.
 * Handles rendering of trees, plants, and foliage across 3 distinct modes:
 * - REALISTIC (Naturalist): Quaternius GLB Nature Pack & OBJ models with metric heights and multi-biome flora
 * - SCIENTIFIC: Metric parametric trees with DBH markers and LAI foliage envelopes
 * - GAMIFIED: Authentic Minecraft cubic voxel trees (Oak, Birch, Spruce/Pine, Cacti)
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

    // Loaded 3D Assets (GLB Nature Pack & OBJ)
    private Spatial treesGlb;
    private Spatial birchTreesGlb;
    private Spatial pineTreesGlb;
    private Spatial mapleTreesGlb;
    private Spatial deadTreesGlb;
    private Spatial palmTreesGlb;
    private Spatial bushesGlb;
    private Spatial flowerBushesGlb;
    private Spatial flowersGlb;
    private Spatial rocksGlb;

    private Spatial bambooModel;
    private Spatial cactusModel;
    private Spatial tropicalPlantsModel;

    public VegetationVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("VegetationNode");
        loadAssets();
    }

    private void loadAssets() {
        // Load GLB Nature Pack Assets
        treesGlb = safeLoadModel("models/nature_pack/Trees.glb");
        birchTreesGlb = safeLoadModel("models/nature_pack/Birch Trees.glb");
        pineTreesGlb = safeLoadModel("models/nature_pack/Pine Trees.glb");
        mapleTreesGlb = safeLoadModel("models/nature_pack/Maple Trees.glb");
        deadTreesGlb = safeLoadModel("models/nature_pack/Dead Trees.glb");
        palmTreesGlb = safeLoadModel("models/nature_pack/Palm Trees.glb");
        bushesGlb = safeLoadModel("models/nature_pack/Bushes.glb");
        flowerBushesGlb = safeLoadModel("models/nature_pack/Flower Bushes.glb");
        flowersGlb = safeLoadModel("models/nature_pack/Flowers.glb");
        rocksGlb = safeLoadModel("models/nature_pack/Rocks.glb");

        // Load OBJ Models
        bambooModel = safeLoadModel("models/bamboo_set.obj");
        cactusModel = safeLoadModel("models/cactus.obj");
        tropicalPlantsModel = safeLoadModel("models/tropical_plants.obj");
    }

    private Spatial safeLoadModel(String path) {
        try {
            Spatial model = assetManager.loadModel(path);
            if (model != null) {
                applyAlphaAndLightingFixes(model);
            }
            return model;
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Notice: Could not load " + path + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private void applyAlphaAndLightingFixes(Spatial spatial) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                // Ensure proper shadow casting and clean face culling
                mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applyAlphaAndLightingFixes(child);
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
            rebuildVegetation(64, 64);
        }
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rebuildVegetation(64, 64);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rootNode.setCullHint(visible ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
    }

    /**
     * Computes the hemisphere-adjusted season.
     * Southern hemisphere inverts seasons (Winter <-> Summer, Autumn <-> Spring).
     */
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

    public void rebuildVegetation(int gridWidth, int gridDepth) {
        rootNode.detachAllChildren();
        if (!visible) return;

        Random rand = new Random(42);
        int treeCount = Math.min(65, (gridWidth * gridDepth) / 45);
        Biome biome = Biome.forLatitude(currentLatitude);
        Season effectiveSeason = getEffectiveSeason();

        for (int i = 0; i < treeCount; i++) {
            float x = 4 + rand.nextFloat() * (gridWidth - 8);
            float z = 4 + rand.nextFloat() * (gridDepth - 8);
            float y = 0.5f;

            if (currentRenderMode == RenderMode.REALISTIC) {
                createRealisticFlora(x, y, z, biome, effectiveSeason, rand, i);
            } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                createProceduralTreeScientific(x, y, z, biome, rand);
            } else if (currentRenderMode == RenderMode.GAMIFIED) {
                createProceduralTreeGamified(x, y, z, biome, effectiveSeason, rand);
            }
        }
    }

    /**
     * Builds realistic 3D flora scaled accurately in meters based on biome and seasonal tint.
     */
    private void createRealisticFlora(float x, float y, float z, Biome biome, Season season, Random rand, int index) {
        Spatial chosenModel = null;
        float targetHeight = 9.5f + rand.nextFloat() * 3.0f; // Standard ~9-12m mature canopy tree

        switch (biome) {
            case DESERT:
                if (rand.nextFloat() < 0.40f && cactusModel != null) {
                    chosenModel = cactusModel;
                    targetHeight = 2.5f + rand.nextFloat() * 2.0f; // 2.5 - 4.5m Saguaro cactus
                } else if (rand.nextFloat() < 0.70f && deadTreesGlb != null) {
                    chosenModel = pickRandomSubModel(deadTreesGlb, rand);
                    targetHeight = 5.0f + rand.nextFloat() * 3.0f;
                } else if (rocksGlb != null) {
                    chosenModel = pickRandomSubModel(rocksGlb, rand);
                    targetHeight = 0.8f + rand.nextFloat() * 1.5f;
                }
                break;

            case TROPICAL:
                float rTrop = rand.nextFloat();
                if (rTrop < 0.45f && palmTreesGlb != null) {
                    chosenModel = pickRandomSubModel(palmTreesGlb, rand);
                    targetHeight = 8.0f + rand.nextFloat() * 4.0f;
                } else if (rTrop < 0.70f && bambooModel != null) {
                    chosenModel = bambooModel;
                    targetHeight = 4.0f + rand.nextFloat() * 2.5f;
                } else if (rTrop < 0.85f && tropicalPlantsModel != null) {
                    chosenModel = tropicalPlantsModel;
                    targetHeight = 1.8f + rand.nextFloat() * 1.2f;
                } else if (flowerBushesGlb != null) {
                    chosenModel = pickRandomSubModel(flowerBushesGlb, rand);
                    targetHeight = 1.5f + rand.nextFloat() * 1.0f;
                }
                break;

            case MEDITERRANEAN:
                float rMed = rand.nextFloat();
                if (rMed < 0.45f && treesGlb != null) {
                    chosenModel = pickRandomSubModel(treesGlb, rand);
                    targetHeight = 7.0f + rand.nextFloat() * 3.5f;
                } else if (rMed < 0.70f && bushesGlb != null) {
                    chosenModel = pickRandomSubModel(bushesGlb, rand);
                    targetHeight = 1.6f + rand.nextFloat() * 1.2f;
                } else if (rocksGlb != null) {
                    chosenModel = pickRandomSubModel(rocksGlb, rand);
                    targetHeight = 0.9f + rand.nextFloat() * 1.4f;
                }
                break;

            case ALPINE_SNOW:
            case TUNDRA:
                float rAlp = rand.nextFloat();
                if (rAlp < 0.60f && pineTreesGlb != null) {
                    chosenModel = pickRandomSubModel(pineTreesGlb, rand);
                    targetHeight = 9.0f + rand.nextFloat() * 4.5f;
                } else if (rAlp < 0.80f && birchTreesGlb != null) {
                    chosenModel = pickRandomSubModel(birchTreesGlb, rand);
                    targetHeight = 7.5f + rand.nextFloat() * 3.0f;
                } else if (rocksGlb != null) {
                    chosenModel = pickRandomSubModel(rocksGlb, rand);
                    targetHeight = 1.0f + rand.nextFloat() * 1.8f;
                }
                break;

            case FOREST:
            case GRASSLAND:
            case WETLAND:
            default:
                float rFor = rand.nextFloat();
                if (rFor < 0.35f && mapleTreesGlb != null) {
                    chosenModel = pickRandomSubModel(mapleTreesGlb, rand);
                    targetHeight = 9.0f + rand.nextFloat() * 3.5f;
                } else if (rFor < 0.60f && treesGlb != null) {
                    chosenModel = pickRandomSubModel(treesGlb, rand);
                    targetHeight = 8.5f + rand.nextFloat() * 3.0f;
                } else if (rFor < 0.80f && birchTreesGlb != null) {
                    chosenModel = pickRandomSubModel(birchTreesGlb, rand);
                    targetHeight = 8.0f + rand.nextFloat() * 3.0f;
                } else if (bushesGlb != null) {
                    chosenModel = pickRandomSubModel(bushesGlb, rand);
                    targetHeight = 1.4f + rand.nextFloat() * 1.0f;
                }
                break;
        }

        if (chosenModel != null) {
            Spatial instance = chosenModel.clone();
            normalizeAndPositionModel(instance, x, y, z, targetHeight, rand);
            applySeasonalTint(instance, season, biome);
            rootNode.attachChild(instance);
        } else {
            // High-fidelity fallback procedural mesh
            createProceduralTree3D(x, y, z, rand.nextInt(5), rand, season);
        }
    }

    /**
     * Picks a random variant sub-spatial from a multi-mesh GLB node.
     */
    private Spatial pickRandomSubModel(Spatial model, Random rand) {
        if (model instanceof Node node && node.getQuantity() > 0) {
            int idx = rand.nextInt(node.getQuantity());
            return node.getChild(idx);
        }
        return model;
    }

    /**
     * Normalizes bounding box so model sits precisely at base Y=0 and reaches target metric height.
     */
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

        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        spatial.setUserData("BaseRotY", rotY);
        spatial.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
    }

    /**
     * Applies seasonal color tinting to foliage geometries.
     */
    private void applySeasonalTint(Spatial spatial, Season season, Biome biome) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Diffuse") != null) {
                ColorRGBA seasonalColor = getSeasonFoliageColor(season, biome);
                if (seasonalColor != null) {
                    mat.setColor("Diffuse", seasonalColor);
                    mat.setColor("Ambient", seasonalColor.mult(0.6f));
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

    /**
     * Realistic procedural fallback tree mesh with seasonal foliage.
     */
    private void createProceduralTree3D(float x, float y, float z, int speciesType, Random rand, Season season) {
        Node treeNode = new Node("Tree3D");
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
            Cylinder trunkMesh = new Cylinder(8, 12, 0.20f, 0.30f, 4.5f, true, false);
            Geometry trunkGeom = new Geometry("PineTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 2.25f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            float[] tierRadii = {2.0f, 1.5f, 1.0f};
            float[] tierHeights = {2.8f, 4.0f, 5.2f};
            for (int i = 0; i < 3; i++) {
                Cylinder coneMesh = new Cylinder(10, 12, 0.05f, tierRadii[i], 1.5f, true, false);
                Geometry coneGeom = new Geometry("PineTier_" + i, coneMesh);
                coneGeom.setMaterial(leafMat);
                coneGeom.setLocalTranslation(0, tierHeights[i], 0);
                coneGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
                treeNode.attachChild(coneGeom);
            }
        } else { // Deciduous / Oak
            Cylinder trunkMesh = new Cylinder(8, 12, 0.28f, 0.38f, 4.0f, true, false);
            Geometry trunkGeom = new Geometry("OakTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 2.0f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            float[][] clusters = {
                {0.0f, 4.8f, 0.0f, 1.8f},
                {-0.9f, 4.2f, 0.6f, 1.4f},
                {0.9f, 4.4f, -0.6f, 1.4f}
            };
            for (int i = 0; i < clusters.length; i++) {
                com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(12, 12, clusters[i][3]);
                Geometry crownGeom = new Geometry("OakCluster_" + i, crownMesh);
                crownGeom.setMaterial(leafMat);
                crownGeom.setLocalTranslation(clusters[i][0], clusters[i][1], clusters[i][2]);
                treeNode.attachChild(crownGeom);
            }
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }

    /**
     * Scientific mode: Standardized metric scale (6.0m trunk, DBH 1.3m ring indicator, LAI crown envelope).
     */
    private void createProceduralTreeScientific(float x, float y, float z, Biome biome, Random rand) {
        Node treeNode = new Node("TreeScientific");
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trunkMat.setColor("Color", new ColorRGBA(0.40f, 0.25f, 0.12f, 1.0f));

        // 6.0m Metric Trunk Axis
        Cylinder trunkMesh = new Cylinder(4, 8, 0.10f, 0.10f, 6.0f, true, false);
        Geometry trunkGeom = new Geometry("SciTrunk", trunkMesh);
        trunkGeom.setMaterial(trunkMat);
        trunkGeom.setLocalTranslation(0, 3.0f, 0);
        trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        treeNode.attachChild(trunkGeom);

        // Standard DBH (Diameter at Breast Height = 1.3m) Metric Ring Marker
        Material dbhMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dbhMat.setColor("Color", new ColorRGBA(0.1f, 0.8f, 1.0f, 1.0f));
        Cylinder dbhRing = new Cylinder(8, 12, 0.18f, 0.18f, 0.08f, true, false);
        Geometry dbhGeom = new Geometry("DBHMarker", dbhRing);
        dbhGeom.setMaterial(dbhMat);
        dbhGeom.setLocalTranslation(0, 1.3f, 0);
        dbhGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
        treeNode.attachChild(dbhGeom);

        // LAI (Leaf Area Index) Crown Volume Envelope
        Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        leafMat.setColor("Color", new ColorRGBA(0.12f, 0.65f, 0.28f, 0.75f));

        com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(10, 10, 1.6f);
        Geometry crownGeom = new Geometry("SciCrownLAI", crownMesh);
        crownGeom.setMaterial(leafMat);
        crownGeom.setLocalTranslation(0, 5.2f, 0);
        treeNode.attachChild(crownGeom);

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }

    /**
     * Gamified Mode: Authentic Minecraft cubic voxel trees (Oak, Birch, Spruce/Pine, Cactus).
     * Rendered with Lit materials and backface culling to ensure correct voxel orientation and shadows.
     */
    private void createProceduralTreeGamified(float x, float y, float z, Biome biome, Season season, Random rand) {
        Node treeNode = new Node("TreeGamified");
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        float voxelSize = 0.6f; // 0.6m per Minecraft voxel block

        Material woodMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        woodMat.setBoolean("UseMaterialColors", true);
        woodMat.setColor("Diffuse", new ColorRGBA(0.46f, 0.28f, 0.14f, 1f));
        woodMat.setColor("Ambient", new ColorRGBA(0.28f, 0.18f, 0.08f, 1f));
        woodMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        if (biome == Biome.DESERT) {
            // Authentic Minecraft Voxel Saguaro Cactus (3 block trunk + 2 side arms)
            Material cactusMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            cactusMat.setBoolean("UseMaterialColors", true);
            cactusMat.setColor("Diffuse", new ColorRGBA(0.20f, 0.58f, 0.22f, 1f));
            cactusMat.setColor("Ambient", new ColorRGBA(0.12f, 0.35f, 0.14f, 1f));
            cactusMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            for (int h = 0; h < 4; h++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("CactusVoxel_" + h, box);
                g.setMaterial(cactusMat);
                g.setLocalTranslation(0, h * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }
            // Arm Left
            Box armLeft1 = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry gL1 = new Geometry("CactusArmL1", armLeft1);
            gL1.setMaterial(cactusMat);
            gL1.setLocalTranslation(-voxelSize, 1.5f * voxelSize, 0);
            treeNode.attachChild(gL1);

            Box armLeft2 = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry gL2 = new Geometry("CactusArmL2", armLeft2);
            gL2.setMaterial(cactusMat);
            gL2.setLocalTranslation(-voxelSize, 2.5f * voxelSize, 0);
            treeNode.attachChild(gL2);

        } else if (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            // Authentic Minecraft Spruce/Pine Tree (6 block trunk + staggered conical canopy)
            int trunkHeight = 6;
            for (int h = 0; h < trunkHeight; h++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("PineLog_" + h, box);
                g.setMaterial(woodMat);
                g.setLocalTranslation(0, h * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }

            Material pineLeafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            pineLeafMat.setBoolean("UseMaterialColors", true);
            ColorRGBA pineCol = (season == Season.WINTER) ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1f) : new ColorRGBA(0.12f, 0.42f, 0.18f, 1f);
            pineLeafMat.setColor("Diffuse", pineCol);
            pineLeafMat.setColor("Ambient", pineCol.mult(0.6f));
            pineLeafMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            float canopyBaseY = 3 * voxelSize;

            // Tier 1: 5x5 Cross at Y=3
            for (int bx = -2; bx <= 2; bx++) {
                for (int bz = -2; bz <= 2; bz++) {
                    if (Math.abs(bx) == 2 && Math.abs(bz) == 2) continue; // Skip 4 extreme corners
                    Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                    Geometry g = new Geometry("PineLeaf_T1_" + bx + "_" + bz, box);
                    g.setMaterial(pineLeafMat);
                    g.setLocalTranslation(bx * voxelSize, canopyBaseY + voxelSize / 2, bz * voxelSize);
                    treeNode.attachChild(g);
                }
            }

            // Tier 2: 3x3 Cross at Y=4
            for (int bx = -1; bx <= 1; bx++) {
                for (int bz = -1; bz <= 1; bz++) {
                    Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                    Geometry g = new Geometry("PineLeaf_T2_" + bx + "_" + bz, box);
                    g.setMaterial(pineLeafMat);
                    g.setLocalTranslation(bx * voxelSize, canopyBaseY + 1.5f * voxelSize, bz * voxelSize);
                    treeNode.attachChild(g);
                }
            }

            // Tier 3: 1x1 Peak at Y=5 & Y=6
            for (int by = 2; by <= 3; by++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("PineLeaf_Peak_" + by, box);
                g.setMaterial(pineLeafMat);
                g.setLocalTranslation(0, canopyBaseY + by * voxelSize + voxelSize / 2, 0);
                treeNode.attachChild(g);
            }

        } else {
            // Authentic Minecraft Oak / Birch Tree (5 block trunk + 5x5 / 3x3 canopy with corner notches)
            int trunkHeight = 5;
            for (int h = 0; h < trunkHeight; h++) {
                Box box = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
                Geometry g = new Geometry("OakLog_" + h, box);
                g.setMaterial(woodMat);
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
                    g.setLocalTranslation(bx * voxelSize, canopyBaseY + 2 * voxelSize + voxelSize / 2, bz * voxelSize);
                    treeNode.attachChild(g);
                }
            }

            // Top Cap (1x1)
            Box topBox = new Box(voxelSize / 2, voxelSize / 2, voxelSize / 2);
            Geometry topGeom = new Geometry("OakLeaf_Cap", topBox);
            topGeom.setMaterial(leafMat);
            topGeom.setLocalTranslation(0, canopyBaseY + 3 * voxelSize + voxelSize / 2, 0);
            treeNode.attachChild(topGeom);
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }

    /**
     * Updates wind swaying animation for foliage and trees based on environmental wind speed and angle.
     */
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
}
