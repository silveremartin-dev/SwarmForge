/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.world.WeatherMarkovChain.WeatherState;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * WeatherVisualizer renders atmospheric, climate, and hydric dynamics in 3D (jMonkeyEngine):
 * - Dynamic Solar Photoperiod & Sunlight Brightness Boost
 * - Physical Wind Velocity Coupling (Speed, Angle, Slant & Turbulence) for Rain, Snow, Hail, Clouds & Fog
 * - Ground Snowpack Cover & Ice Sheet Overlay Rendering with Dynamic Resolution
 * - Multi-mode Weather System: REALISTIC (Particles/Volumetric), SCIENTIFIC (Vector Indicators/Isolines), GAMIFIED (Voxel Blocks)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherVisualizer {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final DirectionalLight sunLight;

    private RenderMode currentRenderMode = RenderMode.REALISTIC;

    // Visual nodes
    private Geometry sunMesh;
    private Geometry coronaHaloGeom;
    private Material coronaHaloMat;
    private Geometry moonMesh;
    private Node starsNode;
    private Node cloudsNode;
    private Node lightningNode;
    private final WeatherPrecipitationRenderer precipitationRenderer;
    private Node mistNode;
    private Node godRaysNode;
    private final List<Geometry> godRays = new ArrayList<>();
    private final List<Geometry> groundMistPuffs = new ArrayList<>();
    private Node fireNode;
    private WeatherGroundCoverRenderer groundCoverRenderer;

    // Scientific Mode Vector & Isoline Indicators
    private Node scientificVectorNode;
    private final List<Geometry> scientificWindArrows = new ArrayList<>();

    private boolean showSun = true;
    private boolean showLightning = true;
    private boolean showClouds = true;
    private boolean showPrecipitation = true;
    private boolean showFog = true;
    private boolean showFire = false;
    private boolean showSnowCover = true;

    private float lightningFlashTime = 0f;

    public WeatherVisualizer(AssetManager assetManager, DirectionalLight sunLight) {
        this.assetManager = assetManager;
        this.sunLight = sunLight;
        this.rootNode = new Node("WeatherVisualizer");

        this.precipitationRenderer = new WeatherPrecipitationRenderer(assetManager, worldWidth, worldHeight, currentRenderMode);
        rootNode.attachChild(precipitationRenderer.getPrecipitationNode());
        this.groundCoverRenderer = new WeatherGroundCoverRenderer(rootNode, assetManager, worldWidth, worldHeight);

        initSunAndMoon();
        initStars();
        rebuildClouds();
        initMistAndFire();
        initGodRays();
        initGroundSnowAndIceCover();
        initLightningNode();
        initScientificVectors();
    }

    public Node getRootNode() {
        return rootNode;
    }

    private int worldWidth = 64;
    private int worldHeight = 64;

    public void setWorldDimensions(int width, int height) {
        if (this.worldWidth != width || this.worldHeight != height) {
            this.worldWidth = Math.max(8, width);
            this.worldHeight = Math.max(8, height);
            precipitationRenderer.setWorldDimensions(this.worldWidth, this.worldHeight);
            groundCoverRenderer = new WeatherGroundCoverRenderer(rootNode, assetManager, this.worldWidth, this.worldHeight);
            rebuildClouds();
            initMistAndFire();
            initGroundSnowAndIceCover();
            initScientificVectors();
        }
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            precipitationRenderer.setRenderMode(mode);
            rebuildClouds();
            updateMistAndFireVisibility();
            initGroundSnowAndIceCover();
            if (godRaysNode != null) {
                godRaysNode.setCullHint(mode == RenderMode.REALISTIC ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            }
        }
    }

    private void initSunAndMoon() {
        // Sun Mesh (Glowing Solar Sphere with Corona Flare)
        Sphere sunSphere = new Sphere(20, 20, 7.0f);
        sunMesh = new Geometry("SunVisual", sunSphere);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", new ColorRGBA(1.0f, 0.95f, 0.4f, 1.0f));
        sunMesh.setMaterial(sunMat);
        rootNode.attachChild(sunMesh);

        // Soft Solar Corona Halo Disc
        Sphere haloSphere = new Sphere(16, 16, 12.0f);
        coronaHaloGeom = new Geometry("SunCoronaHalo", haloSphere);
        coronaHaloMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        coronaHaloMat.setColor("Color", new ColorRGBA(1.0f, 0.92f, 0.50f, 0.14f));
        coronaHaloMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        coronaHaloGeom.setMaterial(coronaHaloMat);
        coronaHaloGeom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent);
        rootNode.attachChild(coronaHaloGeom);

        // Moon Mesh (Silver Blue Lunar Sphere)
        Sphere moonSphere = new Sphere(16, 16, 4.5f);
        moonMesh = new Geometry("MoonVisual", moonSphere);
        Material moonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        moonMat.setColor("Color", new ColorRGBA(0.88f, 0.94f, 1.0f, 0.95f));
        moonMesh.setMaterial(moonMat);
        rootNode.attachChild(moonMesh);
    }

    private void initStars() {
        starsNode = new Node("StarsField");
        Material starMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        starMat.setColor("Color", new ColorRGBA(1.0f, 1.0f, 0.9f, 0.85f));

        Sphere starDot = new Sphere(6, 6, 0.45f);
        for (int i = 0; i < 50; i++) {
            Geometry star = new Geometry("Star_" + i, starDot);
            star.setMaterial(starMat);
            float sx = (float) ((Math.random() - 0.5) * 220.0);
            float sy = 95.0f + (float) (Math.random() * 30.0);
            float sz = (float) ((Math.random() - 0.5) * 220.0);
            star.setLocalTranslation(sx, sy, sz);
            starsNode.attachChild(star);
        }
        rootNode.attachChild(starsNode);
    }

    private void rebuildClouds() {
        if (cloudsNode != null) {
            cloudsNode.removeFromParent();
        }
        cloudsNode = new Node("CloudsDeck");

        float midX = worldWidth * 0.5f;
        float midZ = worldHeight * 0.5f;
        float spanX = worldWidth * 0.6f;
        float spanZ = worldHeight * 0.6f;

        if (currentRenderMode == RenderMode.REALISTIC) {
            // REALISTIC MODE: Layered Volumetric Cloud Particles & Soft Multi-Puffs
            Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            cloudMat.setColor("Color", new ColorRGBA(0.96f, 0.97f, 1.0f, 0.55f));
            cloudMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

            Sphere cloudPuff = new Sphere(12, 12, 6.5f);
            for (int cluster = 0; cluster < 6; cluster++) {
                float cx = midX + (float) ((Math.random() - 0.5) * spanX);
                float cz = midZ + (float) ((Math.random() - 0.5) * spanZ);
                float cy = 38.0f + (float) (Math.random() * 5.0);

                for (int p = 0; p < 4; p++) {
                    Geometry puff = new Geometry("CloudPuff_" + cluster + "_" + p, cloudPuff);
                    puff.setMaterial(cloudMat);
                    puff.setLocalTranslation(
                            cx + (float) ((Math.random() - 0.5) * 8.0),
                            cy + (float) ((Math.random() - 0.5) * 3.0),
                            cz + (float) ((Math.random() - 0.5) * 8.0)
                    );
                    puff.setLocalScale(0.8f + (float) Math.random() * 0.6f);
                    cloudsNode.attachChild(puff);
                }
            }

        } else if (currentRenderMode == RenderMode.GAMIFIED) {
            // GAMIFIED MODE: Voxel Cubic Cloud Blocks (Minecraft style)
            Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            cloudMat.setColor("Color", new ColorRGBA(1.0f, 1.0f, 1.0f, 0.85f));

            float cubeSize = 3.5f;
            for (int c = 0; c < 5; c++) {
                float baseCylinderX = midX + (float) ((Math.random() - 0.5) * spanX);
                float baseCylinderZ = midZ + (float) ((Math.random() - 0.5) * spanZ);
                float baseCylinderY = 36.0f;

                for (int bx = -2; bx <= 2; bx++) {
                    for (int bz = -1; bz <= 1; bz++) {
                        if (Math.abs(bx) == 2 && Math.abs(bz) == 1 && Math.random() > 0.5) continue;
                        Box block = new Box(cubeSize / 2, cubeSize / 4, cubeSize / 2);
                        Geometry g = new Geometry("VoxelCloudBlock", block);
                        g.setMaterial(cloudMat);
                        g.setLocalTranslation(baseCylinderX + bx * cubeSize, baseCylinderY, baseCylinderZ + bz * cubeSize);
                        cloudsNode.attachChild(g);
                    }
                }
            }
        }

        rootNode.attachChild(cloudsNode);
    }

    private void initGroundSnowAndIceCover() {
        if (groundCoverRenderer != null) {
            groundCoverRenderer.init();
        }
    }

    private void initMistAndFire() {
        mistNode = new Node("MistNode");
        mistNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        groundMistPuffs.clear();
        fireNode = new Node("FireNode");
        fireNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);

        // Volumetric Low-Hanging Understory Ground Mist (Brume matinale de sous-bois)
        Material mistMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mistMat.setColor("Color", new ColorRGBA(0.92f, 0.96f, 1.0f, 0.18f));
        mistMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        Sphere mistPuff = new Sphere(8, 8, 4.0f);
        for (int i = 0; i < 12; i++) {
            Geometry g = new Geometry("GroundMistPuff_" + i, mistPuff);
            g.setMaterial(mistMat);
            float mx = 4f + (float) (Math.random() * Math.max(1.0, worldWidth - 8.0));
            float mz = 4f + (float) (Math.random() * Math.max(1.0, worldHeight - 8.0));
            float my = 0.8f + (float) (Math.random() * 2.2f);
            g.setLocalTranslation(mx, my, mz);
            g.setLocalScale(2.2f, 0.35f, 2.2f);
            groundMistPuffs.add(g);
            mistNode.attachChild(g);
        }
        rootNode.attachChild(mistNode);

        // Fire & Smoke Disaster Node
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fireMat.setColor("Color", new ColorRGBA(1.0f, 0.35f, 0.05f, 0.9f));
        Sphere fireSpike = new Sphere(8, 8, 1.2f);
        Geometry fireGeom = new Geometry("FireSpike", fireSpike);
        fireGeom.setMaterial(fireMat);
        fireGeom.setLocalTranslation(worldWidth * 0.5f, 16.0f, worldHeight * 0.5f);
        fireNode.attachChild(fireGeom);
        rootNode.attachChild(fireNode);

        updateMistAndFireVisibility();
    }

    private void initGodRays() {
        if (godRaysNode != null) godRaysNode.removeFromParent();
        godRaysNode = new Node("CrepuscularGodRays");
        godRays.clear();

        Material rayMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        rayMat.setColor("Color", new ColorRGBA(1.0f, 0.96f, 0.82f, 0.08f));
        rayMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        rayMat.getAdditionalRenderState().setFaceCullMode(com.jme3.material.RenderState.FaceCullMode.Off);

        Cylinder shaft = new Cylinder(6, 6, 1.2f, 3.8f, 50.0f, true, false);
        for (int i = 0; i < 5; i++) {
            Geometry g = new Geometry("Sunbeam_" + i, shaft);
            g.setMaterial(rayMat);
            float rx = (worldWidth * 0.25f) + i * (worldWidth * 0.12f) + (float) ((Math.random() - 0.5) * 4.0);
            float rz = (worldHeight * 0.25f) + (i % 3) * (worldHeight * 0.16f) + (float) ((Math.random() - 0.5) * 4.0);
            g.setLocalTranslation(rx, 25.0f, rz);
            godRays.add(g);
            godRaysNode.attachChild(g);
        }
        godRaysNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
        rootNode.attachChild(godRaysNode);
    }

    private void updateMistAndFireVisibility() {
        if (mistNode != null) {
            mistNode.setCullHint(showFog ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
        }
        if (fireNode != null) {
            fireNode.setCullHint(showFire ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
        }
    }

    private void initScientificVectors() {
        if (scientificVectorNode != null) scientificVectorNode.removeFromParent();
        scientificVectorNode = new Node("ScientificVectors");
        scientificWindArrows.clear();

        Material vecMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        vecMat.setColor("Color", new ColorRGBA(0.2f, 0.85f, 1.0f, 0.95f));

        Cylinder arrow = new Cylinder(4, 4, 0.06f, 2.5f, true);
        for (int i = 0; i < 16; i++) {
            Geometry g = new Geometry("VelocityVector_" + i, arrow);
            g.setMaterial(vecMat);
            float gx = (worldWidth * 0.15f) + (i % 4) * (worldWidth * 0.23f);
            float gz = (worldHeight * 0.15f) + (i / 4) * (worldHeight * 0.23f);
            g.setLocalTranslation(gx, 15f, gz);
            scientificWindArrows.add(g);
            scientificVectorNode.attachChild(g);
        }
        rootNode.attachChild(scientificVectorNode);
    }

    private void initLightningNode() {
        lightningNode = new Node("LightningBolts");
        rootNode.attachChild(lightningNode);
    }

    public void update(WeatherSystem weather, float tpf) {
        if (weather == null) return;

        // 1. Solar & Lunar Orbital Trajectory + Day/Night Sunlight Brightness Boost
        float sunAngle = weather.getSunAngle();
        boolean isDay = weather.isDaytime();

        float worldCenterX = 32f;
        float worldCenterZ = 32f;

        if (sunMesh != null) {
            boolean sunVis = showSun && isDay;
            sunMesh.setCullHint(!sunVis ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (coronaHaloGeom != null) {
                coronaHaloGeom.setCullHint(!sunVis ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            }
            if (isDay) {
                float sx = worldCenterX + (float) Math.cos((sunAngle - 0.25f) * Math.PI * 2) * 160f;
                float sy = 85f + (float) Math.sin((sunAngle - 0.25f) * Math.PI * 2) * 110f;
                float sz = worldCenterZ + 40f;
                sunMesh.setLocalTranslation(sx, sy, sz);
                if (coronaHaloGeom != null) {
                    coronaHaloGeom.setLocalTranslation(sx, sy, sz);
                }

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(worldCenterX - sx, 10f - sy, worldCenterZ - sz).normalizeLocal());
                    float intensity = Math.max(0.75f, weather.getLightLevel() * 2.5f);
                    sunLight.setColor(new ColorRGBA(1.35f, 1.30f, 1.20f, 1.0f).mult(intensity));
                }
            }
        }

        if (moonMesh != null) {
            moonMesh.setCullHint((!showSun || isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (!isDay) {
                float mx = worldCenterX + (float) Math.cos((sunAngle + 0.25f) * Math.PI * 2) * 160f;
                float my = 85f + (float) Math.sin((sunAngle + 0.25f) * Math.PI * 2) * 110f;
                float mz = worldCenterZ + 40f;
                moonMesh.setLocalTranslation(mx, my, mz);

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(worldCenterX - mx, 10f - my, worldCenterZ - mz).normalizeLocal());
                    // Soft moonlight illumination
                    sunLight.setColor(new ColorRGBA(0.60f, 0.65f, 0.75f, 1.0f).mult(Math.max(0.40f, weather.getLightLevel() * 1.5f)));
                }
            }
        }

        // Night Stars visibility
        if (starsNode != null) {
            starsNode.setCullHint((!showSun || isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
        }

        // 2. Physical Wind Vector Coupling (Speed m/s & Angle Deg)
        float windSpeedMs = weather.getWindSpeedMs(); // m/s
        float windAngleDeg = weather.getWindDirectionAngle(); // 0° = North, 90° = East, etc.
        float windRad = FastMath.DEG_TO_RAD * windAngleDeg;

        // Velocity vector components (Drift along wind direction)
        float windDx = FastMath.sin(windRad) * (windSpeedMs * 0.45f);
        float windDz = -FastMath.cos(windRad) * (windSpeedMs * 0.45f);


        // 3. Clouds (Stationary atmospheric layer)
        if (cloudsNode != null) {
            cloudsNode.setCullHint(!showClouds ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
        }

        // 4. Precipitation Slant & Kinematics (Rain, Snow, Hail)
        precipitationRenderer.update(weather, tpf, showPrecipitation, windDx, windDz, windRad);

        // 5. Technical Resolution of Ground Snow Cover & Ice Sheet Overlay Rendering
        if (groundCoverRenderer != null) {
            groundCoverRenderer.update(weather, showSnowCover);
        }

        // 6. Understory Ground Mist Drift (Brume matinale de sous-bois)
        if (showFog && tpf > 0.0001f) {
            for (Geometry puff : groundMistPuffs) {
                Vector3f pos = puff.getLocalTranslation();
                pos.x += windDx * 0.12f * tpf;
                pos.z += windDz * 0.12f * tpf;
                if (pos.x < 2.0f) pos.x = 62.0f;
                if (pos.x > 62.0f) pos.x = 2.0f;
                if (pos.z < 2.0f) pos.z = 62.0f;
                if (pos.z > 62.0f) pos.z = 2.0f;
                puff.setLocalTranslation(pos);
            }
        }

        // 7. Crepuscular God Rays (Rayons solaires de canopée à travers les clairières)
        if (godRaysNode != null) {
            boolean raysActive = currentRenderMode == RenderMode.REALISTIC && isDay && showSun;
            if (!raysActive) {
                godRaysNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            } else {
                // Fade in during morning / late afternoon when sun elevation is low
                float solarElev = (float) Math.sin((sunAngle - 0.25f) * Math.PI * 2);
                float rayStrength = (solarElev > 0.08f && solarElev < 0.78f) ? (1.0f - Math.abs(solarElev - 0.42f) / 0.35f) : 0.0f;
                rayStrength = Math.max(0.0f, Math.min(1.0f, rayStrength));

                if (rayStrength <= 0.02f) {
                    godRaysNode.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
                } else {
                    godRaysNode.setCullHint(com.jme3.scene.Spatial.CullHint.Never);
                    Quaternion sunDirQuat = new Quaternion().fromAngles(0.35f, (sunAngle - 0.25f) * FastMath.TWO_PI, 0);
                    godRaysNode.setLocalRotation(sunDirQuat);
                    for (Geometry ray : godRays) {
                        Material m = ray.getMaterial();
                        if (m != null && m.getMaterialDef().getMaterialParam("Color") != null) {
                            m.setColor("Color", new ColorRGBA(1.0f, 0.95f, 0.80f, 0.08f * rayStrength));
                        }
                    }
                }
            }
        }

        // 8. Lightning Strobe Timer
        if (lightningFlashTime > 0) {
            lightningFlashTime -= tpf;
            if (lightningFlashTime <= 0) {
                lightningNode.detachAllChildren();
            }
        }

        WeatherState state = weather.getWeatherState();
        if (showLightning && (state == WeatherState.THUNDERSTORM || state == WeatherState.TEMPEST) && tpf > 0.0001f) {
            if (Math.random() < 0.018) {
                triggerLightningFlash();
            }
        }
    }

    public void triggerLightningFlash() {
        if (!showLightning) return;
        lightningFlashTime = 0.22f;

        com.jme3.light.PointLight flash = new com.jme3.light.PointLight();
        flash.setColor(ColorRGBA.White.mult(6.0f));
        flash.setPosition(new Vector3f((float) (Math.random() - 0.5) * 50 + 32f, 38, (float) (Math.random() - 0.5) * 50 + 32f));
        flash.setRadius(220f);

        rootNode.addLight(flash);

        new Thread(() -> {
            try {
                Thread.sleep(160);
                rootNode.removeLight(flash);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public void setShowSun(boolean showSun) { this.showSun = showSun; }
    public void setShowLightning(boolean showLightning) { this.showLightning = showLightning; }
    public void setShowClouds(boolean showClouds) { this.showClouds = showClouds; }
    public void setShowPrecipitation(boolean showPrecipitation) { this.showPrecipitation = showPrecipitation; }
    public void setShowFog(boolean showFog) { this.showFog = showFog; updateMistAndFireVisibility(); }
    public void setShowFire(boolean showFire) { this.showFire = showFire; updateMistAndFireVisibility(); }
    public void setShowSnowCover(boolean showSnowCover) { this.showSnowCover = showSnowCover; }
}
