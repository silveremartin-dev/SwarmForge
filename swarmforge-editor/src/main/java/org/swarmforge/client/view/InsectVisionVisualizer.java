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
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import org.swarmforge.core.domain.Terrarium;

/**
 * Handles optical vision modes for insect physiology visualization:
 * - Ultraviolet (UV) reflectance and floral nectar guides
 * - Ommatidial compound eye facets and skylight polarization rings (E-vector navigation).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InsectVisionVisualizer {

    public static void rebuildOmmatidialOverlay(
            Node ommatidialOverlayNode,
            Terrarium activeTerrarium,
            float gridWidth,
            float gridHeight,
            AssetManager assetManager) {

        ommatidialOverlayNode.detachAllChildren();
        if (activeTerrarium == null) return;

        float cx = gridWidth * 0.5f;
        float cz = gridHeight * 0.5f;
        float cy = activeTerrarium.getSurfaceElevation(cx, cz) + 12.0f;

        Material polMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        polMat.setColor("Color", new ColorRGBA(0.20f, 0.65f, 0.95f, 0.40f));
        polMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int r = 1; r <= 3; r++) {
            Geometry polRing = new Geometry("PolarizationRing_" + r, new Cylinder(24, 24, r * 6.0f, 0.05f, false));
            polRing.setMaterial(polMat);
            polRing.setQueueBucket(RenderQueue.Bucket.Transparent);
            polRing.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
            polRing.setLocalTranslation(cx, cy + 25.0f, cz);
            ommatidialOverlayNode.attachChild(polRing);
        }

        Material facetMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        facetMat.setColor("Color", new ColorRGBA(0.70f, 0.85f, 1.0f, 0.22f));
        facetMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                Geometry facet = new Geometry("Ommatidium_" + i + "_" + j, new Cylinder(6, 6, 1.4f, 0.02f, false));
                facet.setMaterial(facetMat);
                facet.setQueueBucket(RenderQueue.Bucket.Transparent);
                facet.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
                facet.setLocalTranslation(cx + i * 2.8f + (j % 2) * 1.4f, cy + 18.0f, cz + j * 2.4f);
                ommatidialOverlayNode.attachChild(facet);
            }
        }
    }

    public static void applyUVColoration(Spatial spatial, boolean uv) {
        if (spatial instanceof Geometry geom) {
            Material mat = geom.getMaterial();
            if (mat != null) {
                try {
                    boolean hasDiffuse = mat.getMaterialDef().getMaterialParam("Diffuse") != null;
                    boolean hasAmbient = mat.getMaterialDef().getMaterialParam("Ambient") != null;
                    boolean hasColor = mat.getMaterialDef().getMaterialParam("Color") != null;

                    if (uv) {
                        if (hasDiffuse && mat.getParam("Diffuse") != null && geom.getUserData("origDiffuse") == null) {
                            geom.setUserData("origDiffuse", mat.getParam("Diffuse").getValue());
                        }
                        if (hasAmbient && mat.getParam("Ambient") != null && geom.getUserData("origAmbient") == null) {
                            geom.setUserData("origAmbient", mat.getParam("Ambient").getValue());
                        }
                        if (hasColor && mat.getParam("Color") != null && geom.getUserData("origColor") == null) {
                            geom.setUserData("origColor", mat.getParam("Color").getValue());
                        }

                        String gName = geom.getName() != null ? geom.getName().toLowerCase() : "";
                        ColorRGBA uvDiffuse;
                        ColorRGBA uvAmbient;

                        if (gName.contains("flower") || gName.contains("petal") || gName.contains("blossom") || gName.contains("nectar")) {
                            uvDiffuse = new ColorRGBA(0.85f, 0.20f, 0.98f, 1.0f);
                            uvAmbient = new ColorRGBA(0.45f, 0.10f, 0.65f, 1.0f);
                        } else if (gName.contains("leaf") || gName.contains("leaves") || gName.contains("canopy") || gName.contains("bush")) {
                            uvDiffuse = new ColorRGBA(0.22f, 0.38f, 0.28f, 1.0f);
                            uvAmbient = new ColorRGBA(0.12f, 0.18f, 0.15f, 1.0f);
                        } else if (gName.contains("water") || gName.contains("river") || gName.contains("puddle")) {
                            uvDiffuse = new ColorRGBA(0.20f, 0.75f, 1.0f, 0.90f);
                            uvAmbient = new ColorRGBA(0.10f, 0.40f, 0.70f, 0.90f);
                        } else {
                            uvDiffuse = new ColorRGBA(0.40f, 0.30f, 0.55f, 1.0f);
                            uvAmbient = new ColorRGBA(0.20f, 0.15f, 0.35f, 1.0f);
                        }

                        if (hasDiffuse) mat.setColor("Diffuse", uvDiffuse);
                        if (hasAmbient) mat.setColor("Ambient", uvAmbient);
                        if (hasColor) mat.setColor("Color", uvDiffuse);
                    } else {
                        if (hasDiffuse && geom.getUserData("origDiffuse") instanceof ColorRGBA origD) {
                            mat.setColor("Diffuse", origD);
                        }
                        if (hasAmbient && geom.getUserData("origAmbient") instanceof ColorRGBA origA) {
                            mat.setColor("Ambient", origA);
                        }
                        if (hasColor && geom.getUserData("origColor") instanceof ColorRGBA origC) {
                            mat.setColor("Color", origC);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } else if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                applyUVColoration(child, uv);
            }
        }
    }
}
