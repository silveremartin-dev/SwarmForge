/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Handles procedural ground snowpack, surface ice sheet, and crystalline hoarfrost overlay rendering
 * for {@link WeatherVisualizer}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherGroundCoverRenderer {

    private final Node rootNode;
    private final AssetManager assetManager;
    private final float worldWidth;
    private final float worldHeight;

    private Node snowGroundNode;
    private Node iceGroundNode;
    private Node hoarfrostNode;

    private Geometry snowGroundGeom;
    private Geometry iceGroundGeom;
    private Geometry hoarfrostGeom;

    private Material snowGroundMat;
    private Material iceGroundMat;
    private Material hoarfrostMat;

    public WeatherGroundCoverRenderer(Node rootNode, AssetManager assetManager, float worldWidth, float worldHeight) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void init() {
        if (snowGroundNode != null) snowGroundNode.removeFromParent();
        if (iceGroundNode != null) iceGroundNode.removeFromParent();
        if (hoarfrostNode != null) hoarfrostNode.removeFromParent();

        snowGroundNode = new Node("SnowGroundCover");
        iceGroundNode = new Node("IceSheetCover");
        hoarfrostNode = new Node("HoarfrostCover");

        // Ground Snowpack Overlay (High-resolution XZ quad layer lying just above terrain)
        snowGroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        snowGroundMat.setColor("Color", new ColorRGBA(0.98f, 0.99f, 1.0f, 0.0f));
        snowGroundMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        snowGroundMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Quad snowQuad = new Quad(worldWidth, worldHeight);
        snowGroundGeom = new Geometry("SnowCoverMesh", snowQuad);
        snowGroundGeom.setMaterial(snowGroundMat);
        snowGroundGeom.rotate(-FastMath.HALF_PI, 0, 0);
        snowGroundGeom.setLocalTranslation(0, 0.18f, 0);
        snowGroundNode.attachChild(snowGroundGeom);

        // Ground Surface Ice Sheet Overlay
        iceGroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        iceGroundMat.setColor("Color", new ColorRGBA(0.70f, 0.88f, 1.0f, 0.0f));
        iceGroundMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        iceGroundMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Quad iceQuad = new Quad(worldWidth, worldHeight);
        iceGroundGeom = new Geometry("IceSheetMesh", iceQuad);
        iceGroundGeom.setMaterial(iceGroundMat);
        iceGroundGeom.rotate(-FastMath.HALF_PI, 0, 0);
        iceGroundGeom.setLocalTranslation(0, 0.22f, 0);
        iceGroundNode.attachChild(iceGroundGeom);

        // Crystalline Hoarfrost / Gelée blanche Overlay
        hoarfrostMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        hoarfrostMat.setColor("Color", new ColorRGBA(0.96f, 0.98f, 1.0f, 0.0f));
        hoarfrostMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        hoarfrostMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        Quad hoarQuad = new Quad(worldWidth, worldHeight);
        hoarfrostGeom = new Geometry("HoarfrostCoverMesh", hoarQuad);
        hoarfrostGeom.setMaterial(hoarfrostMat);
        hoarfrostGeom.rotate(-FastMath.HALF_PI, 0, 0);
        hoarfrostGeom.setLocalTranslation(0, 0.15f, 0);
        hoarfrostNode.attachChild(hoarfrostGeom);

        snowGroundNode.setCullHint(Spatial.CullHint.Always);
        iceGroundNode.setCullHint(Spatial.CullHint.Always);
        hoarfrostNode.setCullHint(Spatial.CullHint.Always);
        rootNode.attachChild(snowGroundNode);
        rootNode.attachChild(iceGroundNode);
        rootNode.attachChild(hoarfrostNode);
    }

    public void update(WeatherSystem weather, boolean showSnowCover) {
        if (weather == null) return;
        float snowDepthMm = weather.getSnowDepthMm();
        float iceThicknessMm = weather.getIceThicknessMm();

        if (showSnowCover && snowGroundMat != null) {
            float snowAlpha = Math.min(0.92f, snowDepthMm / 50.0f);
            snowGroundMat.setColor("Color", new ColorRGBA(0.98f, 0.99f, 1.0f, snowAlpha));
            snowGroundNode.setCullHint(snowAlpha > 0.02f ? Spatial.CullHint.Never : Spatial.CullHint.Always);

            float snowElevationY = 0.18f + Math.min(0.25f, (snowDepthMm / 200.0f) * 0.25f);
            if (snowGroundGeom != null) {
                Vector3f p = snowGroundGeom.getLocalTranslation();
                p.y = snowElevationY;
                snowGroundGeom.setLocalTranslation(p);
            }
        }

        if (iceGroundMat != null) {
            float iceAlpha = Math.min(0.85f, iceThicknessMm / 30.0f);
            iceGroundMat.setColor("Color", new ColorRGBA(0.68f, 0.88f, 1.0f, iceAlpha));
            iceGroundNode.setCullHint(iceAlpha > 0.02f ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        }

        float temp = weather.getTemperature();
        float humidity = weather.getHumidity();
        boolean hoarfrostCondition = (temp >= -2.5f && temp <= 2.5f && humidity > 0.78f && snowDepthMm < 1.0f);
        if (hoarfrostMat != null && hoarfrostNode != null) {
            float hoarAlpha = hoarfrostCondition ? Math.min(0.42f, (humidity - 0.78f) * 2.1f) : 0.0f;
            hoarfrostMat.setColor("Color", new ColorRGBA(0.96f, 0.98f, 1.0f, hoarAlpha));
            hoarfrostNode.setCullHint(hoarAlpha > 0.02f ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        }
    }
}
