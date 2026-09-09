/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
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
import org.swarmforge.core.world.WeatherSystem;

import java.util.Random;

/**
 * 3D Vegetation and Flora visualizer for JMonkeyEngine.
 * Handles rendering of trees, plants, and foliage across 3 distinct modes:
 * - REALISTIC (Naturalist): Maximize usage of 3D OBJ models (bamboo_set.obj, cactus.obj, tropical_plants.obj)
 * - SCIENTIFIC: Minimalist procedural parametric trees
 * - GAMIFIED: Stylized voxel block trees (Minecraft style)
 *
 * Provides physical wind sway coupling for natural, lightweight foliage swaying.
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

    // Pre-loaded OBJ models
    private Spatial bambooModel;
    private Spatial cactusModel;
    private Spatial tropicalPlantsModel;

    public VegetationVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("VegetationNode");
        loadObjModels();
    }

    private void loadObjModels() {
        try {
            bambooModel = assetManager.loadModel("models/bamboo_set.obj");
            if (bambooModel != null) {
                applyAlphaAndLightingFixes(bambooModel);
            }
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Could not load bamboo_set.obj: " + e.getMessage());
        }

        try {
            cactusModel = assetManager.loadModel("models/cactus.obj");
            if (cactusModel != null) {
                applyAlphaAndLightingFixes(cactusModel);
            }
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Could not load cactus.obj: " + e.getMessage());
        }

        try {
            tropicalPlantsModel = assetManager.loadModel("models/tropical_plants.obj");
            if (tropicalPlantsModel != null) {
                applyAlphaAndLightingFixes(tropicalPlantsModel);
            }
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Could not load tropical_plants.obj: " + e.getMessage());
        }
    }

    private void applyAlphaAndLightingFixes(Spatial spatial) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
                geom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
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

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rebuildVegetation(64, 64);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            rootNode.setCullHint(Spatial.CullHint.Always);
        } else {
            rootNode.setCullHint(Spatial.CullHint.Dynamic);
        }
    }

    public void rebuildVegetation(int gridWidth, int gridDepth) {
        rootNode.detachAllChildren();
        if (!visible) return;

        Random rand = new Random(42);
        int treeCount = Math.min(60, (gridWidth * gridDepth) / 50);

        for (int i = 0; i < treeCount; i++) {
            float x = 5 + rand.nextFloat() * (gridWidth - 10);
            float z = 5 + rand.nextFloat() * (gridDepth - 10);
            float y = 0.5f; // Ground baseline

            int speciesType = rand.nextInt(5); // 0=bamboo, 1=cactus, 2=tropical, 3=oak, 4=pine

            if (currentRenderMode == RenderMode.REALISTIC) {
                // REALISTIC NATURALIST MODE: Utilize OBJ assets for flora (bamboo, cactus, tropical)
                // and procedural high-fidelity 3D meshes for trees (oak, pine).
                Spatial plantObj = null;
                float scale = 1.0f;

                if (speciesType == 0 && bambooModel != null) {
                    plantObj = bambooModel.clone();
                    scale = 0.35f + rand.nextFloat() * 0.2f;
                } else if (speciesType == 1 && cactusModel != null) {
                    plantObj = cactusModel.clone();
                    scale = 0.05f + rand.nextFloat() * 0.03f;
                } else if (speciesType == 2 && tropicalPlantsModel != null) {
                    plantObj = tropicalPlantsModel.clone();
                    scale = 0.25f + rand.nextFloat() * 0.15f;
                }

                if (plantObj != null) {
                    Node plantNode = new Node("PlantObjNode_" + i);
                    plantNode.attachChild(plantObj);
                    plantNode.setLocalTranslation(x, y, z);
                    plantNode.setLocalScale(scale);
                    float rotY = rand.nextFloat() * FastMath.TWO_PI;
                    plantNode.setUserData("BaseRotY", rotY);
                    plantNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
                    rootNode.attachChild(plantNode);
                } else {
                    // Procedural high-fidelity 3D tree mesh (Oak, Pine, Birch, Cactus, Tropical)
                    createProceduralTree3D(x, y, z, speciesType, rand);
                }

            } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                // SCIENTIFIC MODE: Minimalist schematic procedural tree
                createProceduralTreeScientific(x, y, z, speciesType, rand);

            } else if (currentRenderMode == RenderMode.GAMIFIED) {
                // GAMIFIED MODE: Voxel cubic block tree (Minecraft style blocky cubes with clean volume culling)
                createProceduralTreeGamified(x, y, z, speciesType, rand);
            }
        }
    }

    /**
     * Updates wind swaying animation for foliage and trees based on environmental wind speed and angle.
     * Provides a subtle, natural sway for typical breezes (10-12 km/h), avoiding exaggerated motions.
     */
    public void update(WeatherSystem weather, float tpf) {
        if (!visible || rootNode.getChildren().isEmpty()) return;

        swayTime += tpf;

        float windSpeedMs = 3.3f; // Default ~12 km/h light breeze
        float windAngleDeg = 45.0f;
        if (weather != null) {
            windSpeedMs = weather.getWindSpeedMs();
            windAngleDeg = weather.getWindDirectionAngle();
        }

        // Wind velocity scaling: 10-12 km/h (3.3 m/s) produces a gentle, natural sway (~0.7 to 1.0 degrees)
        float windIntensity = Math.min(1.2f, windSpeedMs / 15.0f);
        float swayFrequency = 1.1f + windIntensity * 0.6f;
        float swayAmplitude = 0.006f + windIntensity * 0.014f; // Extremely gentle, realistic sway

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

    private void createProceduralTree3D(float x, float y, float z, int speciesType, Random rand) {
        Node treeNode = new Node("Tree3D");
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        trunkMat.getAdditionalRenderState().setFaceCullMode(com.jme3.material.RenderState.FaceCullMode.Back);

        Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        leafMat.setBoolean("UseMaterialColors", true);
        leafMat.getAdditionalRenderState().setFaceCullMode(com.jme3.material.RenderState.FaceCullMode.Back);

        if (speciesType == 4) {
            // Species 4 = Pine / Conifer Tree
            trunkMat.setColor("Diffuse", new ColorRGBA(0.32f, 0.20f, 0.10f, 1f));
            trunkMat.setColor("Ambient", new ColorRGBA(0.32f, 0.20f, 0.10f, 1f));

            Cylinder trunkMesh = new Cylinder(8, 12, 0.15f, 0.25f, 4.2f, true, false);
            Geometry trunkGeom = new Geometry("PineTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 2.1f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            ColorRGBA pineCol = new ColorRGBA(0.08f, 0.38f, 0.16f, 1f);
            leafMat.setColor("Diffuse", pineCol);
            leafMat.setColor("Ambient", pineCol);

            // 3 Tiered Conical Foliage Layers
            float[] tierRadii = {1.5f, 1.1f, 0.7f};
            float[] tierHeights = {2.4f, 3.4f, 4.3f};
            for (int i = 0; i < 3; i++) {
                Cylinder coneMesh = new Cylinder(10, 12, 0.05f, tierRadii[i], 1.2f, true, false);
                Geometry coneGeom = new Geometry("PineLayer_" + i, coneMesh);
                coneGeom.setMaterial(leafMat);
                coneGeom.setLocalTranslation(0, tierHeights[i], 0);
                coneGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
                treeNode.attachChild(coneGeom);
            }

        } else if (speciesType == 1) {
            // Species 1 = Cactus / Saguaro
            trunkMat.setColor("Diffuse", new ColorRGBA(0.18f, 0.52f, 0.22f, 1f));
            trunkMat.setColor("Ambient", new ColorRGBA(0.18f, 0.52f, 0.22f, 1f));

            Cylinder mainCactus = new Cylinder(8, 12, 0.32f, 0.35f, 2.8f, true, false);
            Geometry cactusGeom = new Geometry("CactusTrunk", mainCactus);
            cactusGeom.setMaterial(trunkMat);
            cactusGeom.setLocalTranslation(0, 1.4f, 0);
            cactusGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(cactusGeom);

            // Cactus Branch Arm
            Cylinder armMesh = new Cylinder(6, 8, 0.22f, 0.22f, 1.2f, true, false);
            Geometry armGeom = new Geometry("CactusArm", armMesh);
            armGeom.setMaterial(trunkMat);
            armGeom.setLocalTranslation(0.5f, 1.6f, 0);
            treeNode.attachChild(armGeom);

        } else if (speciesType == 0) {
            // Species 0 = Bamboo / Birch Tree
            trunkMat.setColor("Diffuse", new ColorRGBA(0.75f, 0.72f, 0.65f, 1f));
            trunkMat.setColor("Ambient", new ColorRGBA(0.75f, 0.72f, 0.65f, 1f));

            Cylinder trunkMesh = new Cylinder(8, 12, 0.12f, 0.15f, 3.8f, true, false);
            Geometry trunkGeom = new Geometry("BirchTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 1.9f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            ColorRGBA leafCol = new ColorRGBA(0.35f, 0.68f, 0.25f, 1f);
            leafMat.setColor("Diffuse", leafCol);
            leafMat.setColor("Ambient", leafCol);

            com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(12, 12, 1.1f);
            Geometry crownGeom = new Geometry("BirchCrown", crownMesh);
            crownGeom.setMaterial(leafMat);
            crownGeom.setLocalTranslation(0, 4.0f, 0);
            treeNode.attachChild(crownGeom);

        } else if (speciesType == 2) {
            // Species 2 = Tropical Palm Tree
            trunkMat.setColor("Diffuse", new ColorRGBA(0.42f, 0.28f, 0.15f, 1f));
            trunkMat.setColor("Ambient", new ColorRGBA(0.42f, 0.28f, 0.15f, 1f));

            Cylinder trunkMesh = new Cylinder(8, 12, 0.18f, 0.28f, 3.6f, true, false);
            Geometry trunkGeom = new Geometry("PalmTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 1.8f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            ColorRGBA palmCol = new ColorRGBA(0.12f, 0.62f, 0.22f, 1f);
            leafMat.setColor("Diffuse", palmCol);
            leafMat.setColor("Ambient", palmCol);

            com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(10, 10, 1.4f);
            Geometry crownGeom = new Geometry("PalmCrown", crownMesh);
            crownGeom.setMaterial(leafMat);
            crownGeom.setLocalTranslation(0, 3.8f, 0);
            treeNode.attachChild(crownGeom);

        } else {
            // Species 3 = Oak / Deciduous Tree (Spreading organic crown)
            trunkMat.setColor("Diffuse", new ColorRGBA(0.45f, 0.25f, 0.12f, 1f));
            trunkMat.setColor("Ambient", new ColorRGBA(0.45f, 0.25f, 0.12f, 1f));

            Cylinder trunkMesh = new Cylinder(8, 12, 0.22f, 0.32f, 3.4f, true, false);
            Geometry trunkGeom = new Geometry("OakTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 1.7f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            ColorRGBA oakCol = new ColorRGBA(0.18f, 0.58f, 0.22f, 1f);
            leafMat.setColor("Diffuse", oakCol);
            leafMat.setColor("Ambient", oakCol);

            // Multi-Cluster Crown (3 overlapping foliage spheres for rich volume)
            float[][] clusters = {
                {0.0f, 3.8f, 0.0f, 1.3f},
                {-0.6f, 3.4f, 0.4f, 1.0f},
                {0.6f, 3.5f, -0.4f, 1.0f}
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

    private void createProceduralTreeScientific(float x, float y, float z, int speciesType, Random rand) {
        Node treeNode = new Node("TreeScientific");
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trunkMat.setColor("Color", new ColorRGBA(0.40f, 0.25f, 0.12f, 1.0f));

        if (speciesType == 1) {
            Cylinder stumpMesh = new Cylinder(6, 8, 0.30f, 0.35f, 0.7f, true, false);
            Geometry stumpGeom = new Geometry("SciStump", stumpMesh);
            stumpGeom.setMaterial(trunkMat);
            stumpGeom.setLocalTranslation(0, 0.35f, 0);
            stumpGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(stumpGeom);
        } else {
            Cylinder trunkMesh = new Cylinder(4, 8, 0.08f, 0.08f, 3.0f, true, false);
            Geometry trunkGeom = new Geometry("SciTrunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 1.5f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            leafMat.setColor("Color", new ColorRGBA(0.12f, 0.55f, 0.22f, 0.85f));

            com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(8, 8, 0.9f);
            Geometry crownGeom = new Geometry("SciCrown", crownMesh);
            crownGeom.setMaterial(leafMat);
            crownGeom.setLocalTranslation(0, 3.2f, 0);
            treeNode.attachChild(crownGeom);
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }

    private void createProceduralTreeGamified(float x, float y, float z, int speciesType, Random rand) {
        Node treeNode = new Node("TreeGamified");
        treeNode.setLocalTranslation(x, y, z);
        float rotY = rand.nextFloat() * FastMath.TWO_PI;
        treeNode.setUserData("BaseRotY", rotY);

        // Use lighting material with backface culling to ensure crisp 3D voxel volume and prevent face inversion
        Material woodMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        woodMat.setBoolean("UseMaterialColors", true);
        woodMat.setColor("Diffuse", new ColorRGBA(0.48f, 0.28f, 0.12f, 1f));
        woodMat.setColor("Ambient", new ColorRGBA(0.48f, 0.28f, 0.12f, 1f));
        woodMat.getAdditionalRenderState().setFaceCullMode(com.jme3.material.RenderState.FaceCullMode.Back);

        if (speciesType == 1) {
            // Species 1 = Cactus / Stump Voxel Block
            Box box = new Box(0.35f, 0.35f, 0.35f);
            Geometry g = new Geometry("StumpCube", box);
            g.setMaterial(woodMat);
            g.setLocalTranslation(0, 0.35f, 0);
            treeNode.attachChild(g);
        } else {
            float cubeSize = 0.6f;
            int trunkHeightCubes = speciesType == 4 ? 5 : 4;

            // Trunk Voxel Stack
            for (int h = 0; h < trunkHeightCubes; h++) {
                Box box = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                Geometry g = new Geometry("LogCube_" + h, box);
                g.setMaterial(woodMat);
                g.setLocalTranslation(0, h * cubeSize + cubeSize / 2, 0);
                treeNode.attachChild(g);
            }

            float canopyBaseY = trunkHeightCubes * cubeSize;

            if (speciesType == 4) {
                // Pine Voxel Pyramid Canopy (3x3 base -> 2x2 mid -> 1x1 peak)
                ColorRGBA pineCol = new ColorRGBA(0.08f, 0.45f, 0.18f, 1f);
                Material leafMatPine = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                leafMatPine.setBoolean("UseMaterialColors", true);
                leafMatPine.setColor("Diffuse", pineCol);
                leafMatPine.setColor("Ambient", pineCol);
                leafMatPine.getAdditionalRenderState().setFaceCullMode(com.jme3.material.RenderState.FaceCullMode.Back);

                // Tier 0 (3x3)
                for (int bx = -1; bx <= 1; bx++) {
                    for (int bz = -1; bz <= 1; bz++) {
                        Box box = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                        Geometry g = new Geometry("PineLeaf_0_" + bx + "_" + bz, box);
                        g.setMaterial(leafMatPine);
                        g.setLocalTranslation(bx * cubeSize, canopyBaseY + cubeSize / 2, bz * cubeSize);
                        treeNode.attachChild(g);
                    }
                }
                // Tier 1 (2x2)
                for (int bx = 0; bx <= 1; bx++) {
                    for (int bz = 0; bz <= 1; bz++) {
                        Box box = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                        Geometry g = new Geometry("PineLeaf_1_" + bx + "_" + bz, box);
                        g.setMaterial(leafMatPine);
                        g.setLocalTranslation((bx - 0.5f) * cubeSize, canopyBaseY + 1.5f * cubeSize, (bz - 0.5f) * cubeSize);
                        treeNode.attachChild(g);
                    }
                }
                // Tier 2 (1x1 Peak)
                Box peakBox = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                Geometry peak = new Geometry("PinePeak", peakBox);
                peak.setMaterial(leafMatPine);
                peak.setLocalTranslation(0, canopyBaseY + 2.5f * cubeSize, 0);
                treeNode.attachChild(peak);

            } else {
                // Oak / Standard Voxel Canopy with Vertical Color Gradients
                ColorRGBA baseLeafCol = speciesType == 0 ? new ColorRGBA(0.35f, 0.78f, 0.28f, 1f) : new ColorRGBA(0.18f, 0.72f, 0.25f, 1f);

                for (int by = 0; by <= 1; by++) {
                    Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                    leafMat.setBoolean("UseMaterialColors", true);
                    float tint = by * 0.12f;
                    ColorRGBA shadedCol = new ColorRGBA(
                        Math.min(1f, baseLeafCol.r + tint),
                        Math.min(1f, baseLeafCol.g + tint),
                        Math.min(1f, baseLeafCol.b + tint),
                        1f
                    );
                    leafMat.setColor("Diffuse", shadedCol);
                    leafMat.setColor("Ambient", shadedCol);
                    leafMat.getAdditionalRenderState().setFaceCullMode(com.jme3.material.RenderState.FaceCullMode.Back);

                    for (int bx = -1; bx <= 1; bx++) {
                        for (int bz = -1; bz <= 1; bz++) {
                            if (bx != 0 && bz != 0 && by == 1) continue; // Cut upper corners for natural block crown
                            Box box = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                            Geometry g = new Geometry("LeafCube_" + bx + "_" + by + "_" + bz, box);
                            g.setMaterial(leafMat);
                            g.setLocalTranslation(bx * cubeSize, canopyBaseY + by * cubeSize + cubeSize / 2, bz * cubeSize);
                            treeNode.attachChild(g);
                        }
                    }
                }
            }
        }

        treeNode.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
        rootNode.attachChild(treeNode);
    }
}

