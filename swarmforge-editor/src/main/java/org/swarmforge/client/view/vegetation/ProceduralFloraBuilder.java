/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view.vegetation;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.VegetationSystem;

import java.util.List;
import java.util.Random;

/**
 * Procedural mesh builder for 3D trees, gamified cubic voxel flora,
 * and parametric scientific vegetation in JMonkeyEngine.
 */
public class ProceduralFloraBuilder {

    private final AssetManager assetManager;

    public ProceduralFloraBuilder(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public ColorRGBA getSeasonFoliageColor(Season season, Biome biome) {
        if (biome == Biome.DESERT) {
            return new ColorRGBA(0.38f, 0.52f, 0.20f, 1.0f);
        }
        return switch (season) {
            case SPRING -> new ColorRGBA(0.16f, 0.52f, 0.14f, 1.0f);
            case SUMMER -> new ColorRGBA(0.10f, 0.44f, 0.12f, 1.0f);
            case FALL -> new ColorRGBA(0.80f, 0.40f, 0.08f, 1.0f);
            case WINTER -> (biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA)
                    ? new ColorRGBA(0.85f, 0.90f, 0.92f, 1.0f)
                    : new ColorRGBA(0.22f, 0.36f, 0.20f, 1.0f);
        };
    }

    public void applySeasonalTint(Spatial spatial, Season season, Biome biome) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Diffuse") != null) {
                String name = geom.getName() != null ? geom.getName().toLowerCase() : "";
                boolean isFoliage = name.contains("leaf") || name.contains("leaves") || name.contains("foliage")
                        || name.contains("bush") || name.contains("grass") || name.contains("crown");

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

    public void normalizeAndPositionModel(Spatial spatial, float x, float y, float z, float targetHeight, Random rand,
                                          Node rootNode, double currentLatitude, List<Spatial> mossyRocks) {
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

        if (targetHeight >= 3.5f) {
            float asymX = 0.86f + rand.nextFloat() * 0.28f;
            float asymZ = 0.86f + rand.nextFloat() * 0.28f;
            float asymY = 0.92f + rand.nextFloat() * 0.16f;
            spatial.setLocalScale(spatial.getLocalScale().x * asymX, spatial.getLocalScale().y * asymY, spatial.getLocalScale().z * asymZ);
        }

        Node pivotNode = new Node("VegPivot");
        pivotNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        float scale = spatial.getLocalScale().x;
        spatial.setLocalTranslation(-centerX * scale, baseYOffset - 0.08f, -centerZ * scale);
        pivotNode.attachChild(spatial);

        float rotY;
        String sName = (spatial.getName() != null) ? spatial.getName().toLowerCase() : "";
        if (sName.contains("moss") || mossyRocks.contains(spatial)) {
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

    public Geometry createMicroVoxel(String name, float sizeX, float sizeY, float sizeZ, Material mat, float posX, float posY, float posZ) {
        Box box = new Box(sizeX * 0.5f, sizeY * 0.5f, sizeZ * 0.5f);
        Geometry g = new Geometry(name, box);
        g.setMaterial(mat);
        g.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        g.setLocalTranslation(posX, posY, posZ);
        return g;
    }

    public void createProceduralTree3D(float x, float y, float z, int speciesType, Random rand, Season season, Node rootNode) {
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
                Sphere crownMesh = new Sphere(12, 12, clusters[i][3]);
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

    public void createProceduralTreeScientific(float x, float y, float z, Biome biome, Random rand, int speciesIdx, Node rootNode) {
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
                Cylinder coneMesh = new Cylinder(10, 12, 0.05f, 1.8f, 4.0f, true, false);
                Geometry coneGeom = new Geometry("SciCrownLAI", coneMesh);
                coneGeom.setMaterial(leafMat);
                coneGeom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                coneGeom.setLocalTranslation(0, 5.0f, 0);
                coneGeom.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
                treeNode.attachChild(coneGeom);
            } else {
                Sphere crownMesh = new Sphere(12, 12, 1.8f);
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

    public void createProceduralTreeGamified(float x, float y, float z, Biome biome, Season season, Random rand,
                                             int speciesIdx, Node rootNode, int cactusPct) {
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
            Material cactusMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            cactusMat.setBoolean("UseMaterialColors", true);
            cactusMat.setColor("Diffuse", new ColorRGBA(0.24f, 0.58f, 0.22f, 1f));
            cactusMat.setColor("Ambient", new ColorRGBA(0.10f, 0.28f, 0.10f, 1f));
            cactusMat.setColor("Specular", ColorRGBA.Black);
            cactusMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

            treeNode.attachChild(createMicroVoxel("CactusCore", 1.0f, 7.5f, 1.0f, cactusMat, 0, 3.75f, 0));
            treeNode.attachChild(createMicroVoxel("CactusRibN", 0.50f, 7.0f, 0.35f, cactusMat, 0, 3.50f, 0.60f));
            treeNode.attachChild(createMicroVoxel("CactusRibS", 0.50f, 7.0f, 0.35f, cactusMat, 0, 3.50f, -0.60f));
            treeNode.attachChild(createMicroVoxel("CactusRibE", 0.35f, 7.0f, 0.50f, cactusMat, 0.60f, 3.50f, 0));
            treeNode.attachChild(createMicroVoxel("CactusRibW", 0.35f, 7.0f, 0.50f, cactusMat, -0.60f, 3.50f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmJointL", 1.0f, 0.70f, 0.70f, cactusMat, -0.95f, 3.8f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmUpL", 0.70f, 3.0f, 0.70f, cactusMat, -1.45f, 5.2f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmJointR", 1.0f, 0.70f, 0.70f, cactusMat, 0.95f, 4.6f, 0));
            treeNode.attachChild(createMicroVoxel("CactusArmUpR", 0.70f, 2.6f, 0.70f, cactusMat, 1.45f, 5.8f, 0));

        } else if (speciesIdx == 1 || biome == Biome.ALPINE_SNOW || biome == Biome.TUNDRA) {
            // Spruce / Conifer
            Material spruceWood = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            spruceWood.setBoolean("UseMaterialColors", true);
            spruceWood.setColor("Diffuse", new ColorRGBA(0.28f, 0.18f, 0.10f, 1f));
            spruceWood.setColor("Ambient", new ColorRGBA(0.15f, 0.10f, 0.05f, 1f));
            treeNode.attachChild(createMicroVoxel("SpruceTrunk", 0.8f, 8.0f, 0.8f, spruceWood, 0, 4.0f, 0));

            Material spruceLeaf = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            spruceLeaf.setBoolean("UseMaterialColors", true);
            ColorRGBA leafCol = (biome == Biome.ALPINE_SNOW) ? new ColorRGBA(0.85f, 0.92f, 0.95f, 1f) : new ColorRGBA(0.12f, 0.38f, 0.18f, 1f);
            spruceLeaf.setColor("Diffuse", leafCol);
            spruceLeaf.setColor("Ambient", leafCol.mult(0.6f));

            treeNode.attachChild(createMicroVoxel("SpruceCrownB", 4.0f, 1.0f, 4.0f, spruceLeaf, 0, 4.5f, 0));
            treeNode.attachChild(createMicroVoxel("SpruceCrownM", 3.0f, 1.5f, 3.0f, spruceLeaf, 0, 6.0f, 0));
            treeNode.attachChild(createMicroVoxel("SpruceCrownT", 1.8f, 2.0f, 1.8f, spruceLeaf, 0, 7.8f, 0));

        } else if (speciesIdx == 4) {
            // Birch Tree
            Material birchWood = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            birchWood.setBoolean("UseMaterialColors", true);
            birchWood.setColor("Diffuse", new ColorRGBA(0.88f, 0.88f, 0.85f, 1f));
            birchWood.setColor("Ambient", new ColorRGBA(0.55f, 0.55f, 0.52f, 1f));
            treeNode.attachChild(createMicroVoxel("BirchTrunk", 0.7f, 6.0f, 0.7f, birchWood, 0, 3.0f, 0));

            Material birchLeaf = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            birchLeaf.setBoolean("UseMaterialColors", true);
            ColorRGBA leafCol = getSeasonFoliageColor(season, biome);
            birchLeaf.setColor("Diffuse", leafCol);
            birchLeaf.setColor("Ambient", leafCol.mult(0.6f));
            treeNode.attachChild(createMicroVoxel("BirchCrown", 3.0f, 3.0f, 3.0f, birchLeaf, 0, 5.5f, 0));

        } else if (speciesIdx == 6) {
            // Deadwood
            Material deadMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            deadMat.setBoolean("UseMaterialColors", true);
            deadMat.setColor("Diffuse", new ColorRGBA(0.35f, 0.30f, 0.25f, 1f));
            deadMat.setColor("Ambient", new ColorRGBA(0.20f, 0.18f, 0.15f, 1f));
            treeNode.attachChild(createMicroVoxel("DeadTrunk", 0.7f, 3.2f, 0.7f, deadMat, 0, 1.6f, 0));

        } else {
            // Oak / Standard Deciduous
            treeNode.attachChild(createMicroVoxel("OakTrunk", 0.8f, 5.5f, 0.8f, woodMat, 0, 2.75f, 0));

            Material oakLeaf = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            oakLeaf.setBoolean("UseMaterialColors", true);
            ColorRGBA leafCol = getSeasonFoliageColor(season, biome);
            oakLeaf.setColor("Diffuse", leafCol);
            oakLeaf.setColor("Ambient", leafCol.mult(0.6f));

            treeNode.attachChild(createMicroVoxel("OakCrownBase", 3.6f, 1.2f, 3.6f, oakLeaf, 0, 4.8f, 0));
            treeNode.attachChild(createMicroVoxel("OakCrownMid", 2.8f, 1.4f, 2.8f, oakLeaf, 0, 6.0f, 0));
            treeNode.attachChild(createMicroVoxel("OakCrownTop", 1.6f, 0.9f, 1.6f, oakLeaf, 0, 7.0f, 0));
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        treeNode.setLocalScale(1.8f, 1.8f, 1.8f);
        rootNode.attachChild(treeNode);
    }

    public void createGamifiedFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome,
                                            Season season, Random rand, Node rootNode, int cactusPct) {
        if (plant.type == VegetationSystem.PlantType.TREE) {
            createProceduralTreeGamified(x, y, z, biome, season, rand, 0, rootNode, cactusPct);
            return;
        }

        Node floraNode = new Node("GamifiedFlora");
        floraNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        floraNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        floraNode.setUserData("BaseRotY", rotY);

        switch (plant.type) {
            case SHRUB -> {
                Material shrubMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                shrubMat.setBoolean("UseMaterialColors", true);
                ColorRGBA shrubCol = getSeasonFoliageColor(season, biome);
                shrubMat.setColor("Diffuse", shrubCol);
                shrubMat.setColor("Ambient", shrubCol.mult(0.6f));
                shrubMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

                float scale = Math.max(0.5f, plant.growth);
                floraNode.attachChild(createMicroVoxel("ShrubCore", 0.60f * scale, 0.50f * scale, 0.60f * scale, shrubMat, 0, 0.25f * scale, 0));
                floraNode.attachChild(createMicroVoxel("ShrubL", 0.30f * scale, 0.35f * scale, 0.30f * scale, shrubMat, -0.30f * scale, 0.18f * scale, 0.10f * scale));
                floraNode.attachChild(createMicroVoxel("ShrubR", 0.30f * scale, 0.35f * scale, 0.30f * scale, shrubMat, 0.28f * scale, 0.18f * scale, -0.10f * scale));
            }
            case FLOWER -> {
                Material stemMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                stemMat.setBoolean("UseMaterialColors", true);
                stemMat.setColor("Diffuse", new ColorRGBA(0.18f, 0.68f, 0.22f, 1f));
                floraNode.attachChild(createMicroVoxel("FlowerStem", 0.08f, 0.40f, 0.08f, stemMat, 0, 0.20f, 0));

                Material petalMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                petalMat.setBoolean("UseMaterialColors", true);
                ColorRGBA petalCol = switch (rand.nextInt(4)) {
                    case 0 -> new ColorRGBA(0.95f, 0.20f, 0.20f, 1f);
                    case 1 -> new ColorRGBA(0.20f, 0.50f, 0.95f, 1f);
                    case 2 -> new ColorRGBA(0.95f, 0.85f, 0.15f, 1f);
                    default -> new ColorRGBA(0.95f, 0.95f, 0.95f, 1f);
                };
                petalMat.setColor("Diffuse", petalCol);
                floraNode.attachChild(createMicroVoxel("FlowerBloom", 0.25f, 0.18f, 0.25f, petalMat, 0, 0.48f, 0));
            }
            default -> {
                Material grassMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                grassMat.setBoolean("UseMaterialColors", true);
                ColorRGBA grassCol = getSeasonFoliageColor(season, biome);
                grassMat.setColor("Diffuse", grassCol);
                floraNode.attachChild(createMicroVoxel("GrassTuft", 0.25f, 0.30f, 0.25f, grassMat, 0, 0.15f, 0));
            }
        }

        rootNode.attachChild(floraNode);
    }

    public void createScientificFloraForPlant(float x, float y, float z, VegetationSystem.Plant plant, Biome biome, Random rand, Node rootNode) {
        if (plant.type == VegetationSystem.PlantType.TREE) {
            createProceduralTreeScientific(x, y, z, biome, rand, 0, rootNode);
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
                Sphere bushSphere = new Sphere(8, 8, 0.45f * Math.max(0.4f, plant.growth));
                Geometry g = new Geometry("SciShrub", bushSphere);
                g.setMaterial(mat);
                g.setLocalTranslation(0, 0.45f * Math.max(0.4f, plant.growth), 0);
                floraNode.attachChild(g);
            }
            case FLOWER -> {
                mat.setColor("Diffuse", new ColorRGBA(0.95f, 0.80f, 0.20f, 1.0f));
                mat.setColor("Ambient", new ColorRGBA(0.60f, 0.50f, 0.10f, 1.0f));
                Sphere flowerSphere = new Sphere(6, 6, 0.20f);
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
}
