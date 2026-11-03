/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Torus;

/**
 * Handles 3D camera navigation (orbit rotation, pan, zoom raycasting, resets)
 * and selection reticle mesh creation for {@link JmeGameApp}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class JmeCameraNavigationController {

    private JmeCameraNavigationController() {
    }

    static void rotateCamera(JmeGameApp app, float x, float y) {
        app.enqueueTask(() -> {
            app.followedAntId = null;
            Camera cam = app.getCamera();
            Vector3f offset = cam.getLocation().subtract(app.cameraTarget);
            Quaternion qYaw = new Quaternion().fromAngleAxis(-x * 0.006f, Vector3f.UNIT_Y);
            Vector3f camLeft = cam.getLeft();
            Quaternion qPitch = new Quaternion().fromAngleAxis(-y * 0.006f, camLeft);

            Vector3f newOffset = qYaw.mult(qPitch.mult(offset));
            if (newOffset.y < 0.2f) newOffset.y = 0.2f;
            cam.setLocation(app.cameraTarget.add(newOffset));
            cam.lookAt(app.cameraTarget, Vector3f.UNIT_Y);
        });
    }

    static void panCamera(JmeGameApp app, float dx, float dy) {
        app.enqueueTask(() -> {
            app.followedAntId = null;
            Camera cam = app.getCamera();
            Vector3f left = cam.getLeft().mult(dx * 0.08f);
            Vector3f up = cam.getUp().mult(dy * 0.08f);
            app.cameraTarget.addLocal(left).addLocal(up);
            cam.setLocation(cam.getLocation().add(left).add(up));
        });
    }

    static void zoomCamera(JmeGameApp app, float delta, double fxX, double fxY, double paneW, double paneH) {
        app.enqueueTask(() -> {
            app.followedAntId = null;
            Camera cam = app.getCamera();
            float jmeX = (float) ((fxX / Math.max(1.0, paneW)) * app.width);
            float jmeY = (float) ((1.0 - (fxY / Math.max(1.0, paneH))) * app.height);

            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(jmeX, jmeY), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(jmeX, jmeY), 1f).subtractLocal(click3d).normalizeLocal();
            Ray ray = new Ray(click3d, dir);

            CollisionResults results = new CollisionResults();
            app.getRootNode().collideWith(ray, results);

            Vector3f targetPoint = null;
            if (results.size() > 0) {
                targetPoint = results.getClosestCollision().getContactPoint();
            } else if (Math.abs(dir.y) > 0.001f) {
                float groundY = app.cameraTarget.y;
                float t = (groundY - click3d.y) / dir.y;
                if (t > 0) {
                    targetPoint = click3d.add(dir.mult(t));
                }
            }

            Vector3f toTarget = (targetPoint != null) ? targetPoint.subtract(cam.getLocation()) : app.cameraTarget.subtract(cam.getLocation());
            float dist = toTarget.length();
            float move = delta * (dist * 0.08f + 0.4f);

            if (delta > 0) {
                if (dist - move < 0.35f) {
                    move = Math.max(0f, dist - 0.35f);
                }
                Vector3f zoomDir = toTarget.normalize();
                cam.setLocation(cam.getLocation().add(zoomDir.mult(move)));
                if (targetPoint != null) {
                    app.cameraTarget.interpolateLocal(targetPoint, 0.12f);
                }
            } else {
                if (dist + Math.abs(move) > 350.0f) {
                    move = -(350.0f - dist);
                }
                Vector3f zoomDir = toTarget.normalize();
                cam.setLocation(cam.getLocation().add(zoomDir.mult(move)));
            }
        });
    }

    static void panCameraTo(JmeGameApp app, float x, float y, float z) {
        app.enqueueTask(() -> {
            app.followedAntId = null;
            Camera cam = app.getCamera();
            app.cameraTarget.set(x, y, z);
            float distance = Math.max(10.0f, cam.getLocation().distance(app.cameraTarget));
            cam.setLocation(app.cameraTarget.add(new Vector3f(0, distance * 0.6f, distance * 0.6f)));
            cam.lookAt(app.cameraTarget, Vector3f.UNIT_Y);
        });
    }

    static void resetCamera(JmeGameApp app) {
        app.enqueueTask(() -> {
            app.followedAntId = null;
            app.initialCameraConfigured = false;
            Camera cam = app.getCamera();
            if (app.simulation != null && app.simulation.getTerrarium() != null) {
                int w = app.simulation.getTerrarium().getWidth();
                int d = app.simulation.getTerrarium().getDepth();
                int h = app.simulation.getTerrarium().getHeight();
                app.cameraTarget.set(w / 2f, d / 2f, h / 2f);
                cam.setLocation(new Vector3f(w / 2f, d + 25, h + 25));
            } else {
                app.cameraTarget.set(32, 10, 32);
                cam.setLocation(new Vector3f(32, 45, 65));
            }
            cam.lookAt(app.cameraTarget, Vector3f.UNIT_Y);
        });
    }

    static void setTopDownView(JmeGameApp app) {
        app.enqueueTask(() -> {
            int w = 64, d = 32, h = 64;
            if (app.simulation != null && app.simulation.getTerrarium() != null) {
                w = app.simulation.getTerrarium().getWidth();
                d = app.simulation.getTerrarium().getDepth();
                h = app.simulation.getTerrarium().getHeight();
            }
            Camera cam = app.getCamera();
            cam.setLocation(new Vector3f(w / 2f, d + 45, h / 2f));
            cam.lookAt(new Vector3f(w / 2f, 0, h / 2f), Vector3f.UNIT_Z);
        });
    }

    static Node createSelectionReticle(JmeGameApp app) {
        Node selectionReticleNode = new Node("SelectionReticle");
        Geometry outerGeom = new Geometry("OuterRing", new Torus(16, 24, 0.025f, 0.45f));
        Material matOuter = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matOuter.setColor("Color", new ColorRGBA(0.22f, 0.74f, 0.97f, 0.85f));
        matOuter.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        matOuter.getAdditionalRenderState().setDepthWrite(false);
        outerGeom.setMaterial(matOuter);
        outerGeom.rotate((float) Math.PI / 2f, 0, 0);
        selectionReticleNode.attachChild(outerGeom);

        Geometry innerGeom = new Geometry("InnerRing", new Torus(16, 24, 0.018f, 0.28f));
        Material matInner = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matInner.setColor("Color", new ColorRGBA(0.96f, 0.62f, 0.04f, 0.90f));
        matInner.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        matInner.getAdditionalRenderState().setDepthWrite(false);
        innerGeom.setMaterial(matInner);
        innerGeom.rotate((float) Math.PI / 2f, 0, 0);
        selectionReticleNode.attachChild(innerGeom);

        for (int i = 0; i < 4; i++) {
            Geometry tickGeom = new Geometry("Tick_" + i, new Box(0.015f, 0.015f, 0.07f));
            tickGeom.setMaterial(matOuter);
            float angle = i * ((float) Math.PI / 2.0f);
            tickGeom.setLocalTranslation((float) Math.cos(angle) * 0.45f, 0, (float) Math.sin(angle) * 0.45f);
            tickGeom.rotate(0, angle, 0);
            selectionReticleNode.attachChild(tickGeom);
        }
        selectionReticleNode.setQueueBucket(RenderQueue.Bucket.Transparent);
        selectionReticleNode.setCullHint(Spatial.CullHint.Always);
        app.getRootNode().attachChild(selectionReticleNode);
        return selectionReticleNode;
    }
}
