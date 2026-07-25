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
    private Node cloudsNode;
    private Node lightningNode;

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

        initSun();
        initClouds();
        initLightningNode();
    }

    public Node getRootNode() {
        return rootNode;
    }

    private void initSun() {
        Sphere sphere = new Sphere(16, 16, 4.0f);
        sunMesh = new Geometry("SunVisual", sphere);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        sunMesh.setMaterial(mat);

        rootNode.attachChild(sunMesh);
    }

    private void initClouds() {
        cloudsNode = new Node("CloudsDeck");
        Material cloudMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cloudMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.7f));
        cloudMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);

        // Simple procedural cloud puffs
        Sphere cloudSphere = new Sphere(8, 8, 5.0f);
        for (int i = 0; i < 8; i++) {
            Geometry puff = new Geometry("CloudPuff_" + i, cloudSphere);
            puff.setMaterial(cloudMat);
            float px = (float) ((Math.random() - 0.5) * 80.0);
            float py = 35.0f + (float) (Math.random() * 5.0);
            float pz = (float) ((Math.random() - 0.5) * 80.0);
            puff.setLocalTranslation(px, py, pz);
            cloudsNode.attachChild(puff);
        }
        rootNode.attachChild(cloudsNode);
    }

    private void initLightningNode() {
        lightningNode = new Node("LightningBolts");
        rootNode.attachChild(lightningNode);
    }

    public void update(WeatherSystem weather, float tpf) {
        if (weather == null) return;

        // 1. Sun Solar Trajectory Position & Light
        float sunAngle = weather.getSunAngle();
        boolean isDay = weather.isDaytime();

        if (sunMesh != null) {
            sunMesh.setCullHint((!showSun || !isDay) ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (isDay) {
                float sx = (float) Math.cos((sunAngle - 0.25f) * Math.PI * 2) * 60f;
                float sy = (float) Math.sin((sunAngle - 0.25f) * Math.PI * 2) * 60f;
                float sz = 20f;
                sunMesh.setLocalTranslation(sx, sy, sz);

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(-sx, -sy, -sz).normalizeLocal());
                    float intensity = Math.max(0.2f, weather.getLightLevel() * 1.5f);
                    sunLight.setColor(ColorRGBA.White.mult(intensity));
                }
            }
        }

        // 2. Clouds Motion
        if (cloudsNode != null) {
            cloudsNode.setCullHint(!showClouds ? com.jme3.scene.Spatial.CullHint.Always : com.jme3.scene.Spatial.CullHint.Never);
            if (showClouds) {
                float windSpeed = (float) weather.getWindSpeedMs();
                cloudsNode.rotate(0, windSpeed * 0.001f * tpf, 0);
            }
        }

        // 3. Lightning Strobe Timer
        if (lightningFlashTime > 0) {
            lightningFlashTime -= tpf;
            if (lightningFlashTime <= 0) {
                lightningNode.detachAllChildren();
            }
        }

        // Automatic Lightning in Storms
        WeatherState state = weather.getWeatherState();
        if (showLightning && (state == WeatherState.THUNDERSTORM || state == WeatherState.TEMPEST)) {
            if (Math.random() < 0.01) {
                triggerLightningFlash();
            }
        }
    }

    public void triggerLightningFlash() {
        if (!showLightning) return;
        lightningFlashTime = 0.2f;

        // Flash Light Visual
        com.jme3.light.PointLight flash = new com.jme3.light.PointLight();
        flash.setColor(ColorRGBA.White.mult(5.0f));
        flash.setPosition(new Vector3f((float) (Math.random() - 0.5) * 50, 40, (float) (Math.random() - 0.5) * 50));
        flash.setRadius(200f);

        rootNode.addLight(flash);

        // Remove flash light after 150ms
        new Thread(() -> {
            try {
                Thread.sleep(150);
                rootNode.removeLight(flash);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    // Toggle Getters / Setters
    public void setShowSun(boolean showSun) { this.showSun = showSun; }
    public void setShowLightning(boolean showLightning) { this.showLightning = showLightning; }
    public void setShowClouds(boolean showClouds) { this.showClouds = showClouds; }
    public void setShowPrecipitation(boolean showPrecipitation) { this.showPrecipitation = showPrecipitation; }
    public void setShowFog(boolean showFog) { this.showFog = showFog; }
}
