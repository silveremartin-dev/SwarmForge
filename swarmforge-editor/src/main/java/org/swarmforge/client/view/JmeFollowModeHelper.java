/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Handles camera tracking and selection reticle placement for followed individuals in {@link JmeGameApp}.
 */
class JmeFollowModeHelper {

    private final JmeGameApp app;
    private float reticleAnimationTimer = 0.0f;

    JmeFollowModeHelper(JmeGameApp app) {
        this.app = app;
    }

    void updateReticleAndFollowCamera(float tpf, boolean pausedOrStopped) {
        if (!pausedOrStopped) {
            reticleAnimationTimer += tpf;
        }

        Node reticleNode = app.selectionReticleNode;
        String followedId = app.followedAntId;

        if (reticleNode != null) {
            if (followedId != null && app.antVisuals.containsKey(followedId)) {
                Spatial antSpatial = app.antVisuals.get(followedId);
                if (antSpatial != null) {
                    reticleNode.setCullHint(Spatial.CullHint.Never);
                    Vector3f antPos = antSpatial.getWorldTranslation();
                    reticleNode.setLocalTranslation(antPos.x, antPos.y + 0.08f, antPos.z);
                    if (!pausedOrStopped) {
                        reticleNode.rotate(0, tpf * 2.2f, 0);
                    }
                    float pulse = 1.0f + 0.12f * (float) Math.sin(reticleAnimationTimer * 5.0f);
                    reticleNode.setLocalScale(pulse);
                } else {
                    reticleNode.setCullHint(Spatial.CullHint.Always);
                }
            } else {
                reticleNode.setCullHint(Spatial.CullHint.Always);
            }
        }

        if (followedId != null) {
            Spatial ant = app.antVisuals.get(followedId);
            if (ant != null) {
                Vector3f target = ant.getLocalTranslation();
                Quaternion rot = ant.getLocalRotation();
                Vector3f fwd = rot.mult(Vector3f.UNIT_Z);
                Camera cam = app.getCamera();

                if (app.getCameraFollowMode() == JmeGameApp.CameraFollowMode.FPS) {
                    Vector3f eyePos = target.add(fwd.mult(0.12f)).add(new Vector3f(0, 0.06f, 0));
                    cam.setLocation(eyePos);
                    cam.lookAt(eyePos.add(fwd.mult(15.0f)), Vector3f.UNIT_Y);
                } else if (app.getCameraFollowMode() == JmeGameApp.CameraFollowMode.TPS) {
                    Vector3f chasePos = target.subtract(fwd.mult(5.0f)).add(new Vector3f(0, 2.8f, 0));
                    Vector3f camPos = cam.getLocation();
                    cam.setLocation(camPos.interpolateLocal(chasePos, tpf * 7.0f));
                    cam.lookAt(target.add(new Vector3f(0, 0.4f, 0)), Vector3f.UNIT_Y);
                } else {
                    Vector3f offset = new Vector3f(0, 18, 18);
                    Vector3f camPos = cam.getLocation();
                    Vector3f desiredPos = target.add(offset);
                    cam.setLocation(camPos.interpolateLocal(desiredPos, tpf * 5.0f));
                    cam.lookAt(target, Vector3f.UNIT_Y);
                }
            } else {
                app.followedAntId = null;
            }
        }
    }
}
