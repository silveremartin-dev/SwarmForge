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
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.core.world.WeatherMarkovChain.WeatherState;
import org.swarmforge.core.world.WeatherSystem;

/**
 * WeatherVisualizer renders atmospheric and climate elements in 3D (jMonkeyEngine):
 * - Sun (Directional lighting & solar sphere positioning)
 * - Lightning Bolts & Strobe Flash
 * - Clouds Deck (Drifting cloud clusters)
 * - Rain / Snow / Hail Precipitation
 * - Fog overlay
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherVisualizer {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final DirectionalLight sunLight;

    private Geometry sunMesh;
    private Geometry moonMesh;
    private Node starsNode;
    private Node cloudsNode;
    private Node lightningNode;
    private Node precipitationNode;
    private java.util.List<Geometry> rainDrops = new java.util.ArrayList<>();
    private java.util.List<Geometry> snowFlakes = new java.util.ArrayList<>();
    private java.util.List<Geometry> hailPellets = new java.util.ArrayList<>();

    private boolean showSun = true;
    private boolean showLightning = true;
    private boolean showClouds = true;
    private boolean showPrecipitation = true;
    private boolean showFog = true;

    private float lightningFlashTime = 0f;

    public WeatherVisualizer(AssetManager assetManager, DirectionalLight sunLight) {
        this.assetManager = assetManager;
        this.sunLight = sunLight;
        this.rootNode = new Node("WeatherVisualizer");

        initSunAndMoon();
        initStars();
        initClouds();
        initPrecipitation();
        initLightningNode();
    }

    public Node getRootNode() {
        return rootNode;
    }

    private void initSunAndMoon() {
        // Sun Mesh (Glowing Golden Solar Sphere)
        Sphere sunSphere = new Sphere(16, 16, 5.0f);
        sunMesh = new Geometry("SunVisual", sunSphere);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", new ColorRGBA(1.0f, 0.9f, 0.3f, 1.0f));
        sunMesh.setMaterial(sunMat);
        rootNode.attachChild(sunMesh);

        // Moon Mesh (Silver Blue Lunar Sphere)
        Sphere moonSphere = new Sphere(16, 16, 4.0f);
        moonMesh = new Geometry("MoonVisual", moonSphere);
        Material moonMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        moonMat.setColor("Color", new ColorRGBA(0.85f, 0.92f, 1.0f, 0.9f));
        moonMesh.setMaterial(moonMat);
        rootNode.attachChild(moonMesh);
    }

    private void initStars() {
        starsNode = new Node("StarsField");
        Material starMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        starMat.setColor("Color", new ColorRGBA(1.0f, 1.0f, 0.9f, 0.8f));

        Sphere starDot = new Sphere(6, 6, 0.4f);
        for (int i = 0; i < 40; i++) {
            Geometry star = new Geometry("Star_" + i, starDot);
            star.setMaterial(starMat);
            float sx = (float) ((Math.random() - 0.5) * 200.0);
            float sy = 90.0f + (float) (Math.random() * 30.0);
            float sz = (float) ((Math.random() - 0.5) * 200.0);
            star.setLocalTranslation(sx, sy, sz);
            starsNode.attachChild(star);
        }
        rootNode.attachChild(starsNode);
    }

    private void initClouds() {
        cloudsNode = new Node("CloudsDeck");
        Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cloudMat.setColor("Color", new ColorRGBA(0.95f, 0.95f, 1.0f, 0.65f));
        cloudMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        Sphere cloudSphere = new Sphere(8, 8, 5.0f);
        for (int i = 0; i < 12; i++) {
            Geometry puff = new Geometry("CloudPuff_" + i, cloudSphere);
            puff.setMaterial(cloudMat);
            float px = (float) ((Math.random() - 0.5) * 120.0);
            float py = 45.0f + (float) (Math.random() * 8.0);
            float pz = (float) ((Math.random() - 0.5) * 120.0);
            puff.setLocalTranslation(px, py, pz);
            cloudsNode.attachChild(puff);
        }
        rootNode.attachChild(cloudsNode);
    }

    private void initPrecipitation() {
        precipitationNode = new Node("PrecipitationSystem");

        // Rain Material & Geometry Streaks
        Material rainMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        rainMat.setColor("Color", new ColorRGBA(0.4f, 0.7f, 1.0f, 0.6f));
        rainMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        com.jme3.scene.shape.Cylinder dropShape = new com.jme3.scene.shape.Cylinder(4, 4, 0.04f, 0.8f, true);

        for (int i = 0; i < 30; i++) {
            Geometry drop = new Geometry("RainDrop_" + i, dropShape);
            drop.setMaterial(rainMat);
            resetParticlePos(drop, 35.0f);
            rainDrops.add(drop);
            precipitationNode.attachChild(drop);
        }

        // Snow Flakes
        Material snowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        snowMat.setColor("Color", new ColorRGBA(1.0f, 1.0f, 1.0f, 0.85f));
        Sphere flakeShape = new Sphere(6, 6, 0.15f);

        for (int i = 0; i < 25; i++) {
            Geometry flake = new Geometry("SnowFlake_" + i, flakeShape);
            flake.setMaterial(snowMat);
            resetParticlePos(flake, 35.0f);
            snowFlakes.add(flake);
            precipitationNode.attachChild(flake);
        }

        // Hail Pellets
        Material hailMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        hailMat.setColor("Color", new ColorRGBA(0.9f, 0.95f, 1.0f, 0.95f));
        Sphere hailShape = new Sphere(6, 6, 0.25f);

        for (int i = 0; i < 15; i++) {
            Geometry pellet = new Geometry("HailPellet_" + i, hailShape);
            pellet.setMaterial(hailMat);
            resetParticlePos(pellet, 35.0f);
            hailPellets.add(pellet);
            precipitationNode.attachChild(pellet);
        }

        rootNode.attachChild(precipitationNode);
    }

    private void resetParticlePos(Geometry p, float maxH) {
        float px = 32f + (float) ((Math.random() - 0.5) * 50.0);
        float py = 10f + (float) (Math.random() * maxH);
        float pz = 32f + (float) ((Math.random() - 0.5) * 50.0);
        p.setLocalTranslation(px, py, pz);
    }

    private void initLightningNode() {
        lightningNode = new Node("LightningBolts");
        rootNode.attachChild(lightningNode);
    }

    public void update(WeatherSystem weather, float tpf) {
        if (weather == null) return;

        // 1. Solar & Lunar Orbital Trajectory + Day/Night Light Transition
        float sunAngle = weather.getSunAngle();
        boolean isDay = weather.isDaytime();

        float worldCenterX = 32f;
        float worldCenterZ = 32f;

        if (sunMesh != null) {
            sunMesh.setCullHint((!showSun || !isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (isDay) {
                float sx = worldCenterX + (float) Math.cos((sunAngle - 0.25f) * Math.PI * 2) * 150f;
                float sy = 80f + (float) Math.sin((sunAngle - 0.25f) * Math.PI * 2) * 100f;
                float sz = worldCenterZ + 40f;
                sunMesh.setLocalTranslation(sx, sy, sz);

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(worldCenterX - sx, 10f - sy, worldCenterZ - sz).normalizeLocal());
                    float intensity = Math.max(0.25f, weather.getLightLevel() * 1.5f);
                    sunLight.setColor(ColorRGBA.White.mult(intensity));
                }
            }
        }

        if (moonMesh != null) {
            moonMesh.setCullHint((!showSun || isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (!isDay) {
                float mx = worldCenterX + (float) Math.cos((sunAngle + 0.25f) * Math.PI * 2) * 150f;
                float my = 80f + (float) Math.sin((sunAngle + 0.25f) * Math.PI * 2) * 100f;
                float mz = worldCenterZ + 40f;
                moonMesh.setLocalTranslation(mx, my, mz);

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(worldCenterX - mx, 10f - my, worldCenterZ - mz).normalizeLocal());
                    sunLight.setColor(new ColorRGBA(0.25f, 0.35f, 0.6f, 1.0f)); // Soft nocturnal moonlight
                }
            }
        }

        // Night Stars visibility
        if (starsNode != null) {
            starsNode.setCullHint((!showSun || isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
        }

        // 2. Clouds Motion
        if (cloudsNode != null) {
            cloudsNode.setCullHint(!showClouds ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (showClouds) {
                float windSpeed = (float) weather.getWindSpeedMs();
                cloudsNode.rotate(0, windSpeed * 0.001f * tpf, 0);
            }
        }

        // 3. Dynamic Precipitation System (Rain / Snow / Hail)
        WeatherState state = weather.getWeatherState();
        boolean isRaining = showPrecipitation && (state == WeatherState.LIGHT_RAIN || state == WeatherState.HEAVY_RAIN || state == WeatherState.THUNDERSTORM || state == WeatherState.TEMPEST);
        boolean isSnowing = showPrecipitation && (state == WeatherState.SNOW || state == WeatherState.BLIZZARD);
        boolean isHailing = showPrecipitation && (state == WeatherState.HAIL);

        for (Geometry drop : rainDrops) {
            drop.setCullHint(isRaining ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isRaining) {
                Vector3f pos = drop.getLocalTranslation();
                pos.y -= tpf * (state == WeatherState.HEAVY_RAIN || state == WeatherState.THUNDERSTORM ? 30f : 18f);
                if (pos.y < 0f) resetParticlePos(drop, 35f);
                drop.setLocalTranslation(pos);
            }
        }

        for (Geometry flake : snowFlakes) {
            flake.setCullHint(isSnowing ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isSnowing) {
                Vector3f pos = flake.getLocalTranslation();
                pos.y -= tpf * 6f;
                pos.x += Math.sin(pos.y * 0.2f) * 0.05f;
                if (pos.y < 0f) resetParticlePos(flake, 35f);
                flake.setLocalTranslation(pos);
            }
        }

        for (Geometry pellet : hailPellets) {
            pellet.setCullHint(isHailing ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isHailing) {
                Vector3f pos = pellet.getLocalTranslation();
                pos.y -= tpf * 35f;
                if (pos.y < 0f) resetParticlePos(pellet, 35f);
                pellet.setLocalTranslation(pos);
            }
        }

        // 4. Lightning Strobe Timer
        if (lightningFlashTime > 0) {
            lightningFlashTime -= tpf;
            if (lightningFlashTime <= 0) {
                lightningNode.detachAllChildren();
            }
        }

        if (showLightning && (state == WeatherState.THUNDERSTORM || state == WeatherState.TEMPEST)) {
            if (Math.random() < 0.015) {
                triggerLightningFlash();
            }
        }
    }

    public void triggerLightningFlash() {
        if (!showLightning) return;
        lightningFlashTime = 0.2f;

        com.jme3.light.PointLight flash = new com.jme3.light.PointLight();
        flash.setColor(ColorRGBA.White.mult(5.0f));
        flash.setPosition(new Vector3f((float) (Math.random() - 0.5) * 50 + 32f, 35, (float) (Math.random() - 0.5) * 50 + 32f));
        flash.setRadius(200f);

        rootNode.addLight(flash);

        new Thread(() -> {
            try {
                Thread.sleep(150);
                rootNode.removeLight(flash);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public void setShowSun(boolean showSun) { this.showSun = showSun; }
    public void setShowLightning(boolean showLightning) { this.showLightning = showLightning; }
    public void setShowClouds(boolean showClouds) { this.showClouds = showClouds; }
    public void setShowPrecipitation(boolean showPrecipitation) { this.showPrecipitation = showPrecipitation; }
    public void setShowFog(boolean showFog) { this.showFog = showFog; }
}
