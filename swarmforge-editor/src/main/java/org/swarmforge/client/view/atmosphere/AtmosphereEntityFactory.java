/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view.atmosphere;

import com.jme3.asset.AssetManager;
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
import org.swarmforge.client.view.atmosphere.AtmosphereEntities.*;
import org.swarmforge.core.domain.Terrarium;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedural geometry and material factory for 3D atmospheric micro-ecology agents,
 * aerobiological particles, and micro-faunal visualizers in JMonkeyEngine.
 */
public class AtmosphereEntityFactory {

    private final AssetManager assetManager;

    public AtmosphereEntityFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public FallingLeafParticle createFallingLeaf(Vector3f spawnPos, float groundY, Random rand) {
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

    public ButterflyAgent createButterfly(Vector3f flowerPos, Random rand, int index) {
        Node bNode = new Node("Butterfly_" + index);
        bNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

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

        Geometry body = new Geometry("Body", new Box(0.008f, 0.008f, 0.035f));
        body.setMaterial(bodyMat);
        bNode.attachChild(body);

        Node leftWingPivot = new Node("LeftWingPivot");
        Geometry leftWingGeom = new Geometry("LeftWing", new Box(0.040f, 0.002f, 0.028f));
        leftWingGeom.setMaterial(wingMat);
        leftWingGeom.setLocalTranslation(-0.040f, 0, 0);
        leftWingPivot.attachChild(leftWingGeom);
        bNode.attachChild(leftWingPivot);

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

    public FireflyParticle createFirefly(Vector3f basePos, Random rand) {
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

    public BirdAgent createBird(Vector3f center, int gridWidth, int gridHeight, Random rand, int index) {
        Material birdMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        birdMat.setColor("Color", new ColorRGBA(0.12f, 0.12f, 0.15f, 0.85f));
        birdMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        birdMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Box wingBox = new Box(0.45f, 0.02f, 0.12f);
        Geometry geom = new Geometry("SoaringBird_" + index, wingBox);
        geom.setMaterial(birdMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);

        float maxDim = Math.min(gridWidth, gridHeight);
        float orbitRadius = maxDim * 0.22f + rand.nextFloat() * (maxDim * 0.15f);
        float speed = 0.25f + rand.nextFloat() * 0.15f;
        float altitude = 22.0f + rand.nextFloat() * 8.0f;
        float startAngle = rand.nextFloat() * FastMath.TWO_PI;

        return new BirdAgent(geom, center, orbitRadius, speed, altitude, startAngle);
    }

    public PollenParticle createPollen(Vector3f spawnPos, float groundY, Random rand) {
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

    public DesertDustParticle createDesertDust(Vector3f spawnPos, float groundY, Random rand) {
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

    public WaterStriderAgent createWaterStrider(Vector3f spawnPos, int gridWidth, int gridHeight, Random rand, int index) {
        Node striderNode = new Node("WaterStrider_" + index);
        striderNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        Material bodyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bodyMat.setColor("Color", new ColorRGBA(0.12f, 0.10f, 0.08f, 1.0f));

        Material legMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        legMat.setColor("Color", new ColorRGBA(0.18f, 0.15f, 0.12f, 0.95f));

        Geometry body = new Geometry("StriderBody", new Box(0.012f, 0.005f, 0.035f));
        body.setMaterial(bodyMat);
        striderNode.attachChild(body);

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
        return new WaterStriderAgent(striderNode, spawnPos, gridWidth, gridHeight, rand);
    }

    public OrbWebAgent createOrbWeb(Vector3f pos, int gridWidth, int gridHeight, Terrarium terrarium, Random rand, int index) {
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
        return new OrbWebAgent(geom, pos, size, rand.nextFloat() * FastMath.TWO_PI, lifespan, gridWidth, gridHeight, terrarium);
    }

    public SoilSteamParticle createSoilSteam(Vector3f spawnPos, Random rand) {
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

    public StridulationAura createStridulationAura(Vector3f centerPos, Random rand) {
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

    public DandelionPappusParticle createDandelionPappus(Vector3f spawnPos, float groundY, Random rand) {
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

    public DynamicPuddleAgent createDynamicPuddle(Vector3f center, float maxRadius) {
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

    public CanopyDripParticle createCanopyDrip(Vector3f canopyPos, float groundY, float initialDelay) {
        Material dropMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dropMat.setColor("Color", new ColorRGBA(0.85f, 0.92f, 1.0f, 0.80f));
        dropMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("CanopyDrip", new Box(0.008f, 0.022f, 0.008f));
        geom.setMaterial(dropMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(canopyPos);

        return new CanopyDripParticle(geom, canopyPos, groundY, initialDelay);
    }

    public GnatSwarmAgent createGnatSwarm(Vector3f center, float radius, Random rand, int index) {
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

    public AphidClusterAgent createAphidCluster(Vector3f stemPos, Random rand, int index) {
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

            Geometry aphid = new Geometry("Aphid_" + i, new Sphere(8, 8, 0.025f));
            aphid.setMaterial(aphidMat);
            aphid.setLocalScale(1.0f, 0.8f, 1.4f);
            aphid.setLocalTranslation(stemPos.x + ox, stemPos.y + oy, stemPos.z + oz);
            clusterNode.attachChild(aphid);

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

    public ExtrafloralNectaryAgent createExtrafloralNectary(Vector3f axilPos, Random rand) {
        Material nectaryMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        nectaryMat.setColor("Color", new ColorRGBA(0.98f, 0.88f, 0.25f, 0.90f));
        nectaryMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry geom = new Geometry("ExtrafloralNectary", new Sphere(6, 6, 0.022f));
        geom.setMaterial(nectaryMat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setLocalTranslation(axilPos);

        return new ExtrafloralNectaryAgent(geom, axilPos);
    }

    public CordycepsCadaverAgent createCordycepsCadaver(Vector3f anchorPos, Random rand, int index) {
        Node cadaverNode = new Node("CordycepsCadaver_" + index);
        cadaverNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        Material deadAntMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        deadAntMat.setBoolean("UseMaterialColors", true);
        deadAntMat.setColor("Diffuse", new ColorRGBA(0.12f, 0.08f, 0.06f, 1.0f));
        deadAntMat.setColor("Ambient", new ColorRGBA(0.08f, 0.05f, 0.04f, 1.0f));

        Geometry antBody = new Geometry("DeadAntGrip", new Box(0.025f, 0.02f, 0.05f));
        antBody.setMaterial(deadAntMat);
        antBody.setLocalTranslation(anchorPos);
        cadaverNode.attachChild(antBody);

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

        Geometry bulb = new Geometry("PerithecialBulb", new Sphere(8, 8, 0.022f));
        bulb.setMaterial(stromaMat);
        bulb.setLocalTranslation(anchorPos.x, anchorPos.y + 0.20f, anchorPos.z);
        cadaverNode.attachChild(bulb);

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

    public Geometry createMudcrackPatch(Vector3f pos, float size, Random rand, int index) {
        Material crackMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        crackMat.setColor("Color", new ColorRGBA(0.28f, 0.18f, 0.12f, 0.70f));
        crackMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry crack = new Geometry("Mudcrack_" + index, new Quad(size, size));
        crack.setMaterial(crackMat);
        crack.setQueueBucket(RenderQueue.Bucket.Transparent);
        crack.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, rand.nextFloat() * FastMath.TWO_PI));
        crack.setLocalTranslation(pos.x - size * 0.5f, pos.y, pos.z - size * 0.5f);
        crack.setCullHint(Spatial.CullHint.Always);
        return crack;
    }
}
