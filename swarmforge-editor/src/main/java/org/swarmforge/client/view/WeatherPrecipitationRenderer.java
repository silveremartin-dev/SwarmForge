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
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.world.WeatherMarkovChain.WeatherState;
import org.swarmforge.core.world.WeatherSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles 3D particle rendering and kinematics for precipitation:
 * Rain drops, Snow flakes, and Hail pellets (realistic & gamified voxel cubes).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherPrecipitationRenderer {

    private final AssetManager assetManager;
    private final Node precipitationNode;

    private final List<Geometry> rainDrops = new ArrayList<>();
    private final List<Geometry> snowFlakes = new ArrayList<>();
    private final List<Geometry> hailPellets = new ArrayList<>();
    private final List<Geometry> gamifiedHailCubes = new ArrayList<>();

    private int worldWidth = 64;
    private int worldHeight = 64;
    private RenderMode currentRenderMode = RenderMode.REALISTIC;

    public WeatherPrecipitationRenderer(AssetManager assetManager, int worldWidth, int worldHeight, RenderMode renderMode) {
        this.assetManager = assetManager;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.currentRenderMode = renderMode;
        this.precipitationNode = new Node("PrecipitationSystem");
        rebuildPrecipitation();
    }

    public Node getPrecipitationNode() {
        return precipitationNode;
    }

    public void setWorldDimensions(int width, int height) {
        if (this.worldWidth != width || this.worldHeight != height) {
            this.worldWidth = Math.max(8, width);
            this.worldHeight = Math.max(8, height);
            rebuildPrecipitation();
        }
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rebuildPrecipitation();
        }
    }

    public void rebuildPrecipitation() {
        precipitationNode.detachAllChildren();
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
    }

    public void update(WeatherSystem weather, float tpf, boolean showPrecipitation, float windDx, float windDz, float windRad) {
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
            if (isRaining && tpf > 0.0001f) {
                drop.setLocalRotation(rainSlant);
                Vector3f pos = drop.getLocalTranslation();
                float fallSpeed = 22f + (rainRate / 5f) * 4f;
                pos.y -= tpf * fallSpeed;
                pos.x += windDx * tpf;
                pos.z += windDz * tpf;
                if (pos.y < 0f || pos.x < -10f || pos.x > (worldWidth + 11f) || pos.z < -10f || pos.z > (worldHeight + 11f)) {
                    resetParticlePos(drop, 35f);
                } else {
                    drop.setLocalTranslation(pos);
                }
            }
        }

        // Snow Flakes: Micro-turbulent drift + wind vector
        for (Geometry flake : snowFlakes) {
            flake.setCullHint(isSnowing ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isSnowing && tpf > 0.0001f) {
                Vector3f pos = flake.getLocalTranslation();
                pos.y -= tpf * (5.5f + (snowRate / 10f) * 2f);
                pos.x += (windDx + FastMath.sin(pos.y * 0.3f) * 0.25f) * tpf;
                pos.z += (windDz + FastMath.cos(pos.y * 0.3f) * 0.25f) * tpf;
                if (pos.y < 0f || pos.x < -10f || pos.x > (worldWidth + 11f) || pos.z < -10f || pos.z > (worldHeight + 11f)) {
                    resetParticlePos(flake, 35f);
                } else {
                    flake.setLocalTranslation(pos);
                }
            }
        }

        // Realistic Hail
        for (Geometry pellet : hailPellets) {
            pellet.setCullHint(isHailing ? com.jme3.scene.Spatial.CullHint.Never : com.jme3.scene.Spatial.CullHint.Always);
            if (isHailing && tpf > 0.0001f) {
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
            if (isHailing && currentRenderMode == RenderMode.GAMIFIED && tpf > 0.0001f) {
                Vector3f pos = cube.getLocalTranslation();
                pos.y -= tpf * 32f;
                pos.x += windDx * 0.3f * tpf;
                pos.z += windDz * 0.3f * tpf;
                if (pos.y < 0f) resetParticlePos(cube, 35f);
                else cube.setLocalTranslation(pos);
            }
        }
    }

    private void resetParticlePos(Geometry p, float maxH) {
        float px = 1.0f + (float) (Math.random() * Math.max(1.0, worldWidth - 2.0));
        float py = 10f + (float) (Math.random() * maxH);
        float pz = 1.0f + (float) (Math.random() * Math.max(1.0, worldHeight - 2.0));
        p.setLocalTranslation(px, py, pz);
    }
}
