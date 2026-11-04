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
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Handles 3D rendering for celestial bodies: Sun, Corona Halo, Moon, and Starfield.
 */
class WeatherCelestialRenderer {

    private final Node rootNode;
    private final AssetManager assetManager;

    private Geometry sunMesh;
    private Geometry coronaHaloGeom;
    private Geometry moonMesh;
    private Node starsNode;

    WeatherCelestialRenderer(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        initSunAndMoon();
        initStars();
    }

    private void initSunAndMoon() {
        Sphere sunSphere = new Sphere(20, 20, 7.0f);
        sunMesh = new Geometry("SunVisual", sunSphere);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", new ColorRGBA(1.0f, 0.95f, 0.4f, 1.0f));
        sunMesh.setMaterial(sunMat);
        rootNode.attachChild(sunMesh);

        Sphere haloSphere = new Sphere(16, 16, 12.0f);
        coronaHaloGeom = new Geometry("SunCoronaHalo", haloSphere);
        Material coronaHaloMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        coronaHaloMat.setColor("Color", new ColorRGBA(1.0f, 0.92f, 0.50f, 0.14f));
        coronaHaloMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        coronaHaloGeom.setMaterial(coronaHaloMat);
        coronaHaloGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(coronaHaloGeom);

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

    void update(WeatherSystem weather, boolean showSun, DirectionalLight sunLight) {
        float sunAngle = weather.getSunAngle();
        boolean isDay = weather.isDaytime();
        float worldCenterX = 32f;
        float worldCenterZ = 32f;

        if (sunMesh != null) {
            boolean sunVis = showSun && isDay;
            sunMesh.setCullHint(!sunVis ? Spatial.CullHint.Always : Spatial.CullHint.Never);
            if (coronaHaloGeom != null) {
                coronaHaloGeom.setCullHint(!sunVis ? Spatial.CullHint.Always : Spatial.CullHint.Never);
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
            moonMesh.setCullHint((!showSun || isDay) ? Spatial.CullHint.Always : Spatial.CullHint.Never);
            if (!isDay) {
                float mx = worldCenterX + (float) Math.cos((sunAngle + 0.25f) * Math.PI * 2) * 160f;
                float my = 85f + (float) Math.sin((sunAngle + 0.25f) * Math.PI * 2) * 110f;
                float mz = worldCenterZ + 40f;
                moonMesh.setLocalTranslation(mx, my, mz);

                if (sunLight != null) {
                    sunLight.setDirection(new Vector3f(worldCenterX - mx, 10f - my, worldCenterZ - mz).normalizeLocal());
                    sunLight.setColor(new ColorRGBA(0.60f, 0.65f, 0.75f, 1.0f).mult(Math.max(0.40f, weather.getLightLevel() * 1.5f)));
                }
            }
        }

        if (starsNode != null) {
            starsNode.setCullHint((!showSun || isDay) ? Spatial.CullHint.Always : Spatial.CullHint.Never);
        }
    }
}
