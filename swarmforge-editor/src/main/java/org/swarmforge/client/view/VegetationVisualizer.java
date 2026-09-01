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

import java.util.Random;

/**
 * 3D Vegetation and Flora visualizer for JMonkeyEngine.
 * Handles rendering of trees, plants, and foliage across 3 distinct modes:
 * - REALISTIC (Naturalist): Maximize usage of 3D OBJ models (bamboo_set.obj, cactus.obj, tropical_plants.obj)
 * - SCIENTIFIC: Minimalist procedural parametric trees
 * - GAMIFIED: Stylized voxel block trees (Minecraft style)
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class VegetationVisualizer {

    private final AssetManager assetManager;
    private final Node rootNode;
    private RenderMode currentRenderMode = RenderMode.REALISTIC;
    private boolean visible = true;

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
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Could not load bamboo_set.obj: " + e.getMessage());
        }

        try {
            cactusModel = assetManager.loadModel("models/cactus.obj");
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Could not load cactus.obj: " + e.getMessage());
        }

        try {
            tropicalPlantsModel = assetManager.loadModel("models/tropical_plants.obj");
        } catch (Exception e) {
            System.err.println("[VegetationVisualizer] Could not load tropical_plants.obj: " + e.getMessage());
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

            int speciesType = rand.nextInt(5); // 0=bamboo, 1=cactus, 2=tropical, 3=oak/birch, 4=pine

            if (currentRenderMode == RenderMode.REALISTIC) {
                // REALISTIC NATURALIST MODE: Maximizing 3D OBJ model asset utilization!
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
                } else if (bambooModel != null) {
                    plantObj = bambooModel.clone();
                    scale = 0.3f + rand.nextFloat() * 0.2f;
                } else if (tropicalPlantsModel != null) {
                    plantObj = tropicalPlantsModel.clone();
                    scale = 0.25f + rand.nextFloat() * 0.15f;
                }

                if (plantObj != null) {
                    plantObj.setLocalTranslation(x, y, z);
                    plantObj.setLocalScale(scale);
                    float rotY = rand.nextFloat() * FastMath.TWO_PI;
                    plantObj.setLocalRotation(new Quaternion().fromAngles(0, rotY, 0));
                    rootNode.attachChild(plantObj);
                } else {
                    // Fallback detailed 3D mesh
                    createProceduralTree3D(x, y, z, speciesType, rand);
                }

            } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
                // SCIENTIFIC MODE: Minimalist schematic procedural tree (slender cylinder trunk + wireframe/clean sphere)
                createProceduralTreeScientific(x, y, z, speciesType, rand);

            } else if (currentRenderMode == RenderMode.GAMIFIED) {
                // GAMIFIED MODE: Voxel cubic block tree (Minecraft style blocky cubes)
                createProceduralTreeGamified(x, y, z, speciesType, rand);
            }
        }
    }

    private void createProceduralTree3D(float x, float y, float z, int speciesType, Random rand) {
        Node treeNode = new Node("Tree3D");
        treeNode.setLocalTranslation(x, y, z);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        trunkMat.setBoolean("UseMaterialColors", true);
        trunkMat.setColor("Diffuse", new ColorRGBA(0.45f, 0.25f, 0.12f, 1f));
        trunkMat.setColor("Ambient", new ColorRGBA(0.45f, 0.25f, 0.12f, 1f));

        if (speciesType == 1) {
            // Species 1 = Stump / Dead Wood (Short cylinder, no leaf crown)
            Cylinder stumpMesh = new Cylinder(8, 12, 0.35f, 0.4f, 0.8f, true, false);
            Geometry stumpGeom = new Geometry("Stump", stumpMesh);
            stumpGeom.setMaterial(trunkMat);
            stumpGeom.setLocalTranslation(0, 0.4f, 0);
            stumpGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(stumpGeom);
        } else {
            Cylinder trunkMesh = new Cylinder(8, 12, 0.2f, 0.25f, 3.5f, true, false);
            Geometry trunkGeom = new Geometry("Trunk", trunkMesh);
            trunkGeom.setMaterial(trunkMat);
            trunkGeom.setLocalTranslation(0, 1.75f, 0);
            trunkGeom.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0));
            treeNode.attachChild(trunkGeom);

            Material leafMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            leafMat.setBoolean("UseMaterialColors", true);
            ColorRGBA leafCol = speciesType == 4 ? new ColorRGBA(0.08f, 0.35f, 0.15f, 1f) : new ColorRGBA(0.15f, 0.55f, 0.2f, 1f);
            leafMat.setColor("Diffuse", leafCol);
            leafMat.setColor("Ambient", leafCol);

            com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(12, 12, 1.2f);
            Geometry crownGeom = new Geometry("Crown", crownMesh);
            crownGeom.setMaterial(leafMat);
            crownGeom.setLocalTranslation(0, 3.8f, 0);
            treeNode.attachChild(crownGeom);
        }

        rootNode.attachChild(treeNode);
    }

    private void createProceduralTreeScientific(float x, float y, float z, int speciesType, Random rand) {
        Node treeNode = new Node("TreeScientific");
        treeNode.setLocalTranslation(x, y, z);

        Material trunkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trunkMat.setColor("Color", new ColorRGBA(0.40f, 0.25f, 0.12f, 1.0f)); // Realistic wood brown

        if (speciesType == 1) {
            // Species 1 = Stump / Dead Wood (Minimalist wood block)
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
            leafMat.setColor("Color", new ColorRGBA(0.12f, 0.55f, 0.22f, 0.85f)); // Natural emerald green

            com.jme3.scene.shape.Sphere crownMesh = new com.jme3.scene.shape.Sphere(8, 8, 0.9f);
            Geometry crownGeom = new Geometry("SciCrown", crownMesh);
            crownGeom.setMaterial(leafMat);
            crownGeom.setLocalTranslation(0, 3.2f, 0);
            treeNode.attachChild(crownGeom);
        }

        rootNode.attachChild(treeNode);
    }

    private void createProceduralTreeGamified(float x, float y, float z, int speciesType, Random rand) {
        Node treeNode = new Node("TreeGamified");
        treeNode.setLocalTranslation(x, y, z);

        Material woodMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        woodMat.setColor("Color", new ColorRGBA(0.45f, 0.25f, 0.1f, 1f));

        if (speciesType == 1) {
            // Species 1 = Stump / Dead Wood (1-2 low voxel blocks)
            Box box = new Box(0.35f, 0.3f, 0.35f);
            Geometry g = new Geometry("StumpCube", box);
            g.setMaterial(woodMat);
            g.setLocalTranslation(0, 0.3f, 0);
            treeNode.attachChild(g);
        } else {
            Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            leafMat.setColor("Color", new ColorRGBA(0.15f, 0.75f, 0.25f, 1f));

            float cubeSize = 0.6f;
            int trunkHeightCubes = 4;

            for (int h = 0; h < trunkHeightCubes; h++) {
                Box box = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                Geometry g = new Geometry("LogCube", box);
                g.setMaterial(woodMat);
                g.setLocalTranslation(0, h * cubeSize + cubeSize / 2, 0);
                treeNode.attachChild(g);
            }

            float canopyBaseY = trunkHeightCubes * cubeSize;
            for (int bx = -1; bx <= 1; bx++) {
                for (int bz = -1; bz <= 1; bz++) {
                    for (int by = 0; by <= 1; by++) {
                        if (bx != 0 && bz != 0 && by == 1) continue;
                        Box box = new Box(cubeSize / 2, cubeSize / 2, cubeSize / 2);
                        Geometry g = new Geometry("LeafCube", box);
                        g.setMaterial(leafMat);
                        g.setLocalTranslation(bx * cubeSize, canopyBaseY + by * cubeSize + cubeSize / 2, bz * cubeSize);
                        treeNode.attachChild(g);
                    }
                }
            }
        }

        rootNode.attachChild(treeNode);
    }
}
