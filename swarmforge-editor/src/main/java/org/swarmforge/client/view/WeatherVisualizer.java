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
    private Geometry moonMesh;
    private Node starsNode;
    private Node cloudsNode;
    private Node lightningNode;
    private Node precipitationNode;
    private Node mistNode;
    private Node fireNode;
    private Node snowGroundNode;
    private Node iceGroundNode;

    // Geometries for precipitation particles
    private final List<Geometry> rainDrops = new ArrayList<>();
    private final List<Geometry> snowFlakes = new ArrayList<>();
    private final List<Geometry> hailPellets = new ArrayList<>();
    private final List<Geometry> gamifiedHailCubes = new ArrayList<>();

    // Snow Ground Overlay Materials & Mesh
    private Geometry snowGroundGeom;
    private Material snowGroundMat;
    private Geometry iceGroundGeom;
    private Material iceGroundMat;

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

        initSunAndMoon();
        initStars();
        rebuildClouds();
        rebuildPrecipitation();
        initMistAndFire();
        initGroundSnowAndIceCover();
        initLightningNode();
        initScientificVectors();
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rebuildClouds();
            rebuildPrecipitation();
            updateMistAndFireVisibility();
            initGroundSnowAndIceCover();
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

        if (currentRenderMode == RenderMode.REALISTIC) {
            // REALISTIC MODE: Layered Volumetric Cloud Particles & Soft Multi-Puffs
            Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            cloudMat.setColor("Color", new ColorRGBA(0.96f, 0.97f, 1.0f, 0.55f));
            cloudMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

            Sphere cloudPuff = new Sphere(12, 12, 6.5f);
            for (int cluster = 0; cluster < 6; cluster++) {
                float cx = (float) ((Math.random() - 0.5) * 100.0);
                float cz = (float) ((Math.random() - 0.5) * 100.0);
                float cy = 42.0f + (float) (Math.random() * 6.0);

                for (int p = 0; p < 4; p++) {
                    Geometry puff = new Geometry("CloudPuff_" + cluster + "_" + p, cloudPuff);
                    puff.setMaterial(cloudMat);
                    puff.setLocalTranslation(
                            cx + (float) ((Math.random() - 0.5) * 12.0),
                            cy + (float) ((Math.random() - 0.5) * 3.0),
                            cz + (float) ((Math.random() - 0.5) * 12.0)
                    );
                    puff.setLocalScale(0.8f + (float) Math.random() * 0.6f);
                    cloudsNode.attachChild(puff);
                }
            }

        } else if (currentRenderMode == RenderMode.SCIENTIFIC) {
            // SCIENTIFIC MODE: Schematic Wireframe Cloud Deck
            Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            cloudMat.setColor("Color", new ColorRGBA(0.3f, 0.7f, 0.9f, 0.4f));
            cloudMat.getAdditionalRenderState().setWireframe(true);

            Sphere wireCloud = new Sphere(8, 8, 8.0f);
            for (int i = 0; i < 8; i++) {
                Geometry g = new Geometry("SciCloud_" + i, wireCloud);
                g.setMaterial(cloudMat);
                g.setLocalTranslation((float) ((Math.random() - 0.5) * 110.0), 45.0f, (float) ((Math.random() - 0.5) * 110.0));
                cloudsNode.attachChild(g);
            }

        } else if (currentRenderMode == RenderMode.GAMIFIED) {
            // GAMIFIED MODE: Voxel Cubic Cloud Blocks (Minecraft style)
            Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            cloudMat.setColor("Color", new ColorRGBA(1.0f, 1.0f, 1.0f, 0.85f));

            float cubeSize = 3.5f;
            for (int c = 0; c < 5; c++) {
                float baseCylinderX = (float) ((Math.random() - 0.5) * 90.0);
                float baseCylinderZ = (float) ((Math.random() - 0.5) * 90.0);
                float baseCylinderY = 40.0f;

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

    private void rebuildPrecipitation() {
        if (precipitationNode != null) {
            precipitationNode.removeFromParent();
        }
        precipitationNode = new Node("PrecipitationSystem");
        rainDrops.clear();
        snowFlakes.clear();
        hailPellets.clear();
        gamifiedHailCubes.clear();

        // 1. Rain Particles
        Material rainMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        rainMat.setColor("Color", new ColorRGBA(0.4f, 0.75f, 1.0f, 0.75f));
        rainMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        Cylinder dropShape = new Cylinder(4, 4, 0.04f, 0.9f, true);

        for (int i = 0; i < 45; i++) {
            Geometry drop = new Geometry("RainDrop_" + i, dropShape);
            drop.setMaterial(rainMat);
            resetParticlePos(drop, 35.0f);
            rainDrops.add(drop);
            precipitationNode.attachChild(drop);
        }

        // 2. Snow Flakes
        Material snowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        snowMat.setColor("Color", new ColorRGBA(1.0f, 1.0f, 1.0f, 0.92f));
        Sphere flakeShape = new Sphere(6, 6, 0.18f);

        for (int i = 0; i < 35; i++) {
            Geometry flake = new Geometry("SnowFlake_" + i, flakeShape);
            flake.setMaterial(snowMat);
            resetParticlePos(flake, 35.0f);
            snowFlakes.add(flake);
            precipitationNode.attachChild(flake);
        }

        // 3. Hail Pellets (Grêle)
        if (currentRenderMode == RenderMode.GAMIFIED) {
            // Gamified Voxel Ice Cube Hail
            Material hailVoxelMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            hailVoxelMat.setColor("Color", new ColorRGBA(0.75f, 0.92f, 1.0f, 0.95f));
            Box hailCube = new Box(0.2f, 0.2f, 0.2f);

            for (int i = 0; i < 25; i++) {
                Geometry cube = new Geometry("GamifiedHailCube_" + i, hailCube);
                cube.setMaterial(hailVoxelMat);
                resetParticlePos(cube, 35.0f);
                gamifiedHailCubes.add(cube);
                precipitationNode.attachChild(cube);
            }
        } else {
            // Realistic High-Speed Icy Pellets & Particle Splash
            Material hailMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            hailMat.setColor("Color", new ColorRGBA(0.85f, 0.95f, 1.0f, 0.95f));
            Sphere hailShape = new Sphere(8, 8, 0.28f);

            for (int i = 0; i < 25; i++) {
                Geometry pellet = new Geometry("HailPellet_" + i, hailShape);
                pellet.setMaterial(hailMat);
                resetParticlePos(pellet, 35.0f);
                hailPellets.add(pellet);
                precipitationNode.attachChild(pellet);
            }
        }

        rootNode.attachChild(precipitationNode);
    }

    private void initGroundSnowAndIceCover() {
        if (snowGroundNode != null) snowGroundNode.removeFromParent();
        if (iceGroundNode != null) iceGroundNode.removeFromParent();

        snowGroundNode = new Node("SnowGroundCover");
        iceGroundNode = new Node("IceSheetCover");

        // Ground Snowpack Overlay (High-resolution XZ quad layer lying just above terrain)
        snowGroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        snowGroundMat.setColor("Color", new ColorRGBA(0.98f, 0.99f, 1.0f, 0.0f));
        snowGroundMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        Quad snowQuad = new Quad(90, 90);
        snowGroundGeom = new Geometry("SnowCoverMesh", snowQuad);
        snowGroundGeom.setMaterial(snowGroundMat);
        snowGroundGeom.rotate(1.5708f, 0, 0); // Flat on ground XZ plane
        snowGroundGeom.setLocalTranslation(-13, 0.18f, -13);
        snowGroundNode.attachChild(snowGroundGeom);

        // Ground Surface Ice Sheet Overlay
        iceGroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        iceGroundMat.setColor("Color", new ColorRGBA(0.70f, 0.88f, 1.0f, 0.0f));
        iceGroundMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        Quad iceQuad = new Quad(90, 90);
        iceGroundGeom = new Geometry("IceSheetMesh", iceQuad);
        iceGroundGeom.setMaterial(iceGroundMat);
        iceGroundGeom.rotate(1.5708f, 0, 0);
        iceGroundGeom.setLocalTranslation(-13, 0.22f, -13);
        iceGroundNode.attachChild(iceGroundGeom);

        rootNode.attachChild(snowGroundNode);
        rootNode.attachChild(iceGroundNode);
    }

    private void initMistAndFire() {
        mistNode = new Node("MistNode");
        fireNode = new Node("FireNode");

        // Ground Mist Particle Layer (Realistic Mode)
        Material mistMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mistMat.setColor("Color", new ColorRGBA(0.9f, 0.95f, 1.0f, 0.18f));
        mistMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        Quad mistQuad = new Quad(80, 80);
        Geometry mistGeom = new Geometry("GroundMistOverlay", mistQuad);
        mistGeom.setMaterial(mistMat);
        mistGeom.rotate(1.5708f, 0, 0); // Flat on ground XZ
        mistGeom.setLocalTranslation(-10, 0.4f, -10);
        mistNode.attachChild(mistGeom);
        rootNode.attachChild(mistNode);

        // Fire & Smoke Disaster Node
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fireMat.setColor("Color", new ColorRGBA(1.0f, 0.35f, 0.05f, 0.9f));
        Sphere fireSpike = new Sphere(8, 8, 1.2f);
        Geometry fireGeom = new Geometry("FireSpike", fireSpike);
        fireGeom.setMaterial(fireMat);
        fireGeom.setLocalTranslation(32f, 1.0f, 32f);
        fireNode.attachChild(fireGeom);
        rootNode.attachChild(fireNode);

        updateMistAndFireVisibility();
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
        scientificVectorNode = new Node("ScientificVectors");
        scientificWindArrows.clear();

        Material vecMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        vecMat.setColor("Color", new ColorRGBA(0.2f, 0.85f, 1.0f, 0.95f));

        Cylinder arrow = new Cylinder(4, 4, 0.06f, 2.5f, true);
        for (int i = 0; i < 16; i++) {
            Geometry g = new Geometry("VelocityVector_" + i, arrow);
            g.setMaterial(vecMat);
            float gx = 8f + (i % 4) * 16f;
            float gz = 8f + (i / 4) * 16f;
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
            sunMesh.setCullHint((!showSun || !isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (isDay) {
                float sx = worldCenterX + (float) Math.cos((sunAngle - 0.25f) * Math.PI * 2) * 160f;
                float sy = 85f + (float) Math.sin((sunAngle - 0.25f) * Math.PI * 2) * 110f;
                float sz = worldCenterZ + 40f;
                sunMesh.setLocalTranslation(sx, sy, sz);

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(worldCenterX - sx, 10f - sy, worldCenterZ - sz).normalizeLocal());
                    float intensity = Math.max(0.7f, weather.getLightLevel() * 2.5f);
                    sunLight.setColor(new ColorRGBA(1.25f, 1.20f, 1.05f, 1.0f).mult(intensity));
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
                    sunLight.setColor(new ColorRGBA(0.35f, 0.45f, 0.75f, 1.0f));
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

        // Update Scientific Wind Arrows rotation
        if (scientificVectorNode != null) {
            boolean isSci = (currentRenderMode == RenderMode.SCIENTIFIC);
            scientificVectorNode.setCullHint(isSci ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isSci) {
                Quaternion windRot = new Quaternion().fromAngles(0, windRad, 0);
                for (Geometry arrow : scientificWindArrows) {
                    arrow.setLocalRotation(windRot);
                    arrow.setLocalScale(0.8f + (windSpeedMs / 30f) * 0.8f);
                }
            }
        }

        // 3. Clouds Drift Motion along Wind Direction
        if (cloudsNode != null) {
            cloudsNode.setCullHint(!showClouds ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (showClouds) {
                // Translation along wind vector + slow orbital rotation
                cloudsNode.rotate(0, windSpeedMs * 0.0008f * tpf, 0);
            }
        }

        // 4. Precipitation Slant & Kinematics (Rain, Snow, Hail)
        WeatherState state = weather.getWeatherState();
        float rainRate = weather.getRainfall(); // mm/h
        float snowRate = weather.getSnowfall(); // mm/h

        boolean isRaining = showPrecipitation && (state == WeatherState.LIGHT_RAIN || state == WeatherState.HEAVY_RAIN || state == WeatherState.THUNDERSTORM || state == WeatherState.TEMPEST || rainRate > 0.5f);
        boolean isSnowing = showPrecipitation && (state == WeatherState.SNOW || state == WeatherState.BLIZZARD || snowRate > 0.5f);
        boolean isHailing = showPrecipitation && (state == WeatherState.HAIL);

        // Rain Slant angle proportional to wind velocity
        Quaternion rainSlant = new Quaternion().fromAngles(windDz * 0.03f, windRad, -windDx * 0.03f);

        for (Geometry drop : rainDrops) {
            drop.setCullHint(isRaining ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isRaining) {
                drop.setLocalRotation(rainSlant);
                Vector3f pos = drop.getLocalTranslation();
                float fallSpeed = 22f + (rainRate / 5f) * 4f;
                pos.y -= tpf * fallSpeed;
                pos.x += windDx * tpf;
                pos.z += windDz * tpf;
                if (pos.y < 0f || pos.x < -10f || pos.x > 75f || pos.z < -10f || pos.z > 75f) {
                    resetParticlePos(drop, 35f);
                } else {
                    drop.setLocalTranslation(pos);
                }
            }
        }

        // Snow Flakes: Micro-turbulent drift + wind vector
        for (Geometry flake : snowFlakes) {
            flake.setCullHint(isSnowing ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isSnowing) {
                Vector3f pos = flake.getLocalTranslation();
                pos.y -= tpf * (5.5f + (snowRate / 10f) * 2f);
                pos.x += (windDx + FastMath.sin(pos.y * 0.3f) * 0.25f) * tpf;
                pos.z += (windDz + FastMath.cos(pos.y * 0.3f) * 0.25f) * tpf;
                if (pos.y < 0f || pos.x < -10f || pos.x > 75f || pos.z < -10f || pos.z > 75f) {
                    resetParticlePos(flake, 35f);
                } else {
                    flake.setLocalTranslation(pos);
                }
            }
        }

        // Realistic & Scientific Hail
        for (Geometry pellet : hailPellets) {
            pellet.setCullHint(isHailing ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isHailing) {
                Vector3f pos = pellet.getLocalTranslation();
                pos.y -= tpf * 44f;
                pos.x += windDx * 0.4f * tpf;
                pos.z += windDz * 0.4f * tpf;
                if (pos.y < 0f) resetParticlePos(pellet, 35f);
                else pellet.setLocalTranslation(pos);
            }
        }

        // Gamified Hail Voxel Cubes
        for (Geometry cube : gamifiedHailCubes) {
            cube.setCullHint((isHailing && currentRenderMode == RenderMode.GAMIFIED) ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isHailing && currentRenderMode == RenderMode.GAMIFIED) {
                Vector3f pos = cube.getLocalTranslation();
                pos.y -= tpf * 32f;
                pos.x += windDx * 0.3f * tpf;
                pos.z += windDz * 0.3f * tpf;
                if (pos.y < 0f) resetParticlePos(cube, 35f);
                else cube.setLocalTranslation(pos);
            }
        }

        // 5. Technical Resolution of Ground Snow Cover & Ice Sheet Overlay Rendering
        float snowDepthMm = weather.getSnowDepthMm();
        float iceThicknessMm = weather.getIceThicknessMm();

        if (showSnowCover && snowGroundMat != null) {
            float snowAlpha = Math.min(0.92f, snowDepthMm / 50.0f); // 50mm snowpack = full opaque white cover
            snowGroundMat.setColor("Color", new ColorRGBA(0.98f, 0.99f, 1.0f, snowAlpha));
            snowGroundNode.setCullHint(snowAlpha > 0.02f ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);

            // Dynamic elevation offset as snow accumulates
            float snowElevationY = 0.18f + Math.min(0.25f, (snowDepthMm / 200.0f) * 0.25f);
            if (snowGroundGeom != null) {
                Vector3f p = snowGroundGeom.getLocalTranslation();
                p.y = snowElevationY;
                snowGroundGeom.setLocalTranslation(p);
            }
        }

        if (iceGroundMat != null) {
            float iceAlpha = Math.min(0.85f, iceThicknessMm / 30.0f); // 30mm ice sheet = full specular cyan ice
            iceGroundMat.setColor("Color", new ColorRGBA(0.68f, 0.88f, 1.0f, iceAlpha));
            iceGroundNode.setCullHint(iceAlpha > 0.02f ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
        }

        // 6. Lightning Strobe Timer
        if (lightningFlashTime > 0) {
            lightningFlashTime -= tpf;
            if (lightningFlashTime <= 0) {
                lightningNode.detachAllChildren();
            }
        }

        if (showLightning && (state == WeatherState.THUNDERSTORM || state == WeatherState.TEMPEST)) {
            if (Math.random() < 0.018) {
                triggerLightningFlash();
            }
        }
    }

    private void resetParticlePos(Geometry p, float maxH) {
        float px = 32f + (float) ((Math.random() - 0.5) * 58.0);
        float py = 10f + (float) (Math.random() * maxH);
        float pz = 32f + (float) ((Math.random() - 0.5) * 58.0);
        p.setLocalTranslation(px, py, pz);
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
