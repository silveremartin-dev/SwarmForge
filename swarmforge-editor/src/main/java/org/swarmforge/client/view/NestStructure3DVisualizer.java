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
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedural and PBR 3D Visualizer for Eusocial Insect Nest Architectures.
 * Renders species-specific physical structures: cathedral termite spires,
 * suspended paper wasp pedicels, arboreal silk leaf pavilions, thatched Formica mounds,
 * bumblebee wax pot clusters, and apiary Langstroth hives.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestStructure3DVisualizer {

    private final AssetManager assetManager;
    private final Node rootNode;
    private final Node nestArchitectureNode = new Node("NestArchitectureNode");
    private final Node colonyInfrastructureNode = new Node("ColonyInfrastructureNode");
    private final List<Colony> trackedColonies = new ArrayList<>();
    private Terrarium activeTerrarium;
    private RenderMode currentRenderMode = RenderMode.REALISTIC;
    private Spatial beehiveModel;

    public NestStructure3DVisualizer(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        rootNode.attachChild(nestArchitectureNode);
        rootNode.attachChild(colonyInfrastructureNode);
    }

    public void setBeehiveModel(Spatial beehiveModel) {
        this.beehiveModel = beehiveModel;
    }

    public void setActiveTerrarium(Terrarium terrarium) {
        this.activeTerrarium = terrarium;
    }

    public Node getNestArchitectureNode() {
        return nestArchitectureNode;
    }

    public Node getColonyInfrastructureNode() {
        return colonyInfrastructureNode;
    }

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
        } else if ((arch.equals("MOUND") || arch.contains("SOLAR_PINE_NEEDLE_MOUND")) && colony.getTunnelNetwork() == null) {
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
            buildSubterraneanEntranceCollar(nestNode, x, surfY, y, mode);
        }

        nestArchitectureNode.attachChild(nestNode);
    }

    private void createColonyInfrastructure(Colony colony, RenderMode mode) {
        if (colony == null) return;
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

        Material woodMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        woodMat.setBoolean("UseMaterialColors", true);
        woodMat.setColor("Diffuse", new ColorRGBA(0.85f, 0.58f, 0.22f, 1.0f));
        woodMat.setColor("Ambient", new ColorRGBA(0.55f, 0.35f, 0.12f, 1.0f));

        Geometry hiveBlock = new Geometry("GamifiedBeehive", new Box(0.50f, 0.50f, 0.50f));
        hiveBlock.setMaterial(woodMat);
        hiveBlock.setLocalTranslation(x, y + 0.50f, z);
        parent.attachChild(hiveBlock);

        Material honeyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        honeyMat.setColor("Color", new ColorRGBA(0.95f, 0.72f, 0.10f, 1.0f));
        Geometry slit = new Geometry("HoneySlit", new Box(0.30f, 0.06f, 0.02f));
        slit.setMaterial(honeyMat);
        slit.setLocalTranslation(x, y + 0.35f, z + 0.51f);
        parent.attachChild(slit);
    }

    private void buildPaperPedunculateNest(Node parent, float x, float y, float z, RenderMode mode) {
        Material paperMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        paperMat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.62f, 0.58f, 0.52f, 1.0f);
        paperMat.setColor("Diffuse", col);
        paperMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
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
        ColorRGBA col = new ColorRGBA(0.72f, 0.38f, 0.22f, 1.0f);
        clayMat.setColor("Diffuse", col);
        clayMat.setColor("Ambient", col.mult(0.6f));

        if (mode == RenderMode.GAMIFIED) {
            for (int i = 0; i < 6; i++) {
                float size = 0.90f - i * 0.12f;
                Geometry block = new Geometry("CathedralSpire_" + i, new Box(size, 0.30f, size));
                block.setMaterial(clayMat);
                block.setLocalTranslation(x, y + i * 0.60f + 0.30f, z);
                parent.attachChild(block);
            }
        } else {
            Geometry mainSpire = new Geometry("CathedralSpire", new Cylinder(12, 16, 0.75f, 3.4f, true));
            mainSpire.setMaterial(clayMat);
            mainSpire.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            mainSpire.setLocalTranslation(x, y + 1.7f, z);
            parent.attachChild(mainSpire);

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
        ColorRGBA col = new ColorRGBA(0.28f, 0.65f, 0.22f, 1.0f);
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
            Geometry pavilion = new Geometry("SilkPavilion", new Sphere(14, 14, 0.75f));
            pavilion.setMaterial(leafMat);
            pavilion.setLocalScale(1.2f, 0.9f, 1.0f);
            pavilion.setLocalTranslation(x, y, z);
            parent.attachChild(pavilion);

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
        if (mode == RenderMode.GAMIFIED) {
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
}
