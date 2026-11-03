/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
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
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.Map;

/**
 * Handles 3D raycast picking, hover inspection, camera orbit/pan/zoom navigation,
 * and selection reticle creation for {@link JmeGameApp}.
 */
class JmeViewportInteractionController {

    private static final Logger LOG = LoggerFactory.getLogger(JmeViewportInteractionController.class);

    private JmeGameApp app;
    private long lastHoverTimeMs = 0;

    final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && !isPressed) {
                Camera cam = app.getCamera();
                Vector2f click2d = app.getInputManager().getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f)
                        .subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);

                CollisionResults results = new CollisionResults();
                app.getRootNode().collideWith(ray, results);

                if (results.size() > 0) {
                    CollisionResult closes = results.getClosestCollision();
                    Geometry geom = closes.getGeometry();

                    if (app.isIndividualTrackingEnabled) {
                        for (int i = 0; i < results.size(); i++) {
                            Geometry hitGeom = results.getCollision(i).getGeometry();
                            Spatial antSpatial = hitGeom;
                            while (antSpatial != null && antSpatial.getUserData("ID") == null) {
                                antSpatial = antSpatial.getParent();
                            }
                            if (antSpatial != null && antSpatial.getUserData("ID") != null) {
                                selectAndFollowAnt((String) antSpatial.getUserData("ID"), (String) antSpatial.getUserData("LifeStage"));
                                return;
                            }
                        }

                        String bestAntId = null;
                        String bestAntStage = null;
                        float bestDistSq = Float.MAX_VALUE;
                        boolean bestIsAdult = false;
                        float maxClickRadiusSq = 28.0f * 28.0f;

                        for (Map.Entry<String, Spatial> entry : app.antVisuals.entrySet()) {
                            Spatial antSpatial = entry.getValue();
                            if (antSpatial != null && antSpatial.getCullHint() != Spatial.CullHint.Always) {
                                Vector3f antPos = antSpatial.getWorldTranslation();
                                Vector3f screenPos = cam.getScreenCoordinates(antPos);
                                if (screenPos.z >= 0f && screenPos.z <= 1.0f) {
                                    float dx = screenPos.x - click2d.x;
                                    float dy = screenPos.y - click2d.y;
                                    float distSq = dx * dx + dy * dy;
                                    if (distSq <= maxClickRadiusSq) {
                                        String stage = antSpatial.getUserData("LifeStage") != null ? (String) antSpatial.getUserData("LifeStage") : "ADULT";
                                        boolean isAdult = "ADULT".equalsIgnoreCase(stage);
                                        if (bestAntId == null || (isAdult && !bestIsAdult) || (isAdult == bestIsAdult && distSq < bestDistSq)) {
                                            bestAntId = entry.getKey();
                                            bestAntStage = stage;
                                            bestDistSq = distSq;
                                            bestIsAdult = isAdult;
                                        }
                                    }
                                }
                            }
                        }

                        if (bestAntId != null) {
                            selectAndFollowAnt(bestAntId, bestAntStage);
                            return;
                        }
                    }

                    Spatial chamberSpatial = geom;
                    while (chamberSpatial != null && chamberSpatial.getUserData("ChamberID") == null && (chamberSpatial.getName() == null || !chamberSpatial.getName().startsWith("Node_"))) {
                        chamberSpatial = chamberSpatial.getParent();
                    }
                    if (chamberSpatial != null) {
                        String chamberIdStr = (String) chamberSpatial.getUserData("ChamberID");
                        if (chamberIdStr == null && chamberSpatial.getName() != null && chamberSpatial.getName().startsWith("Node_")) {
                            chamberIdStr = chamberSpatial.getName().substring(5);
                        }
                        if (chamberIdStr != null && app.selectionListener != null) {
                            final String fChamberId = chamberIdStr;
                            Platform.runLater(() -> app.selectionListener.onChamberSelected(fChamberId));
                            return;
                        }
                    }

                    String[] parts = geom.getName().split("_");
                    if (parts.length == 4 && parts[0].equals("Voxel")) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);

                        if (app.currentTool.equals("Remove Block")) {
                            if (app.terrainListener != null) {
                                app.terrainListener.onBlockChanged(x, y, z, false);
                            }
                            app.enqueueTask(geom::removeFromParent);
                        } else if (app.currentTool.equals("Add Block")) {
                            Vector3f normal = closes.getContactNormal();
                            int ax = x + (int) Math.signum(normal.x);
                            int ay = y + (int) Math.signum(normal.y);
                            int az = z + (int) Math.signum(normal.z);
                            if (app.terrainListener != null) {
                                app.terrainListener.onBlockChanged(ax, ay, az, true);
                            }
                        } else if (app.selectionListener != null) {
                            String matName = "Humus / Sol Organique";
                            float moisture = 45.0f;
                            float temp = 18.5f;
                            float compaction = 65.0f;
                            if (app.simulation != null && app.simulation.getTerrarium() != null) {
                                TerrariumCell cell = app.simulation.getTerrarium().getCell(x, y, z);
                                if (cell != null) {
                                    matName = cell.material().name();
                                    moisture = cell.humidity() * 100.0f;
                                    temp = cell.temperature();
                                }
                            }
                            final String fMat = matName;
                            final float fM = moisture, fT = temp, fC = compaction;
                            Platform.runLater(() -> app.selectionListener.onVoxelSelected(x, y, z, fMat, fM, fT, fC));
                        }
                    }
                }
            }
        }
    };

    final AnalogListener cameraAnalogListener = (name, value, tpf) -> {
        if (app.followedAntId != null && (name.startsWith("Pan") || name.startsWith("Rotate"))) {
            app.followedAntId = null;
        }

        Camera cam = app.getCamera();
        float panSpeed = 15f;
        float rotateSpeed = 1.5f;
        float zoomSpeed = 30f;

        switch (name) {
            case "Pan_Left" -> cam.setLocation(cam.getLocation().add(cam.getLeft().mult(panSpeed * tpf)));
            case "Pan_Right" -> cam.setLocation(cam.getLocation().add(cam.getLeft().mult(-panSpeed * tpf)));
            case "Pan_Forward" -> cam.setLocation(cam.getLocation().add(cam.getDirection().mult(panSpeed * tpf)));
            case "Pan_Back" -> cam.setLocation(cam.getLocation().add(cam.getDirection().mult(-panSpeed * tpf)));
            case "Rotate_Left" -> {
                Quaternion rot = new Quaternion().fromAngleAxis(rotateSpeed * tpf, Vector3f.UNIT_Y);
                cam.setRotation(cam.getRotation().mult(rot));
            }
            case "Rotate_Right" -> {
                Quaternion rot = new Quaternion().fromAngleAxis(-rotateSpeed * tpf, Vector3f.UNIT_Y);
                cam.setRotation(cam.getRotation().mult(rot));
            }
            case "Zoom_In", "Zoom_Wheel" ->
                cam.setLocation(cam.getLocation().add(cam.getDirection().mult(zoomSpeed * value)));
            case "Zoom_Out", "Zoom_Wheel_Neg" ->
                cam.setLocation(cam.getLocation().add(cam.getDirection().mult(-zoomSpeed * value)));
        }
    };

    final ActionListener cameraActionListener = (name, isPressed, tpf) -> {
        if (name.equals("Center_Colony") && isPressed) {
            if (app.simulation != null && !app.simulation.getColonies().isEmpty()) {
                Colony colony = app.simulation.getColonies().get(0);
                float targetX = colony.getNestX();
                float targetY = colony.getNestY();
                float targetZ = colony.getNestZ();
                Camera cam = app.getCamera();
                cam.setLocation(new Vector3f(targetX, targetZ + 30, targetY + 30));
                cam.lookAt(new Vector3f(targetX, targetZ, targetY), Vector3f.UNIT_Y);
            }
        }
    };

    JmeViewportInteractionController(JmeGameApp app) {
        this.app = app;
    }

    void selectAndFollowAnt(String antId, String stage) {
        app.followedAntId = antId;
        String fStage = stage != null ? stage : "ADULT";
        LOG.debug("Following Individual: {}", app.followedAntId);
        if (app.selectionListener != null) {
            String roleName = "OuvriÃ¨re (Worker)";
            String taskName = "Forager";
            float health = 95.0f, energy = 88.0f, hydration = 80.0f, distanceTravelled = 450.0f;
            if (app.simulation != null && !app.simulation.getColonies().isEmpty()) {
                for (Colony colony : app.simulation.getColonies()) {
                    for (Individual ind : colony.getLivingIndividuals()) {
                        if (ind.getId().toString().equals(antId)) {
                            health = (float) ind.getEnergy();
                            energy = (float) ind.getEnergy();
                            if (ind.getCaste() != null) roleName = ind.getCaste().name();
                            break;
                        }
                    }
                }
            }
            final String finalRole = roleName;
            final String finalTask = taskName;
            final float finalHealth = health;
            final float finalEnergy = energy;
            final float finalHydration = hydration;
            final float finalDistance = distanceTravelled;
            Platform.runLater(() -> app.selectionListener.onIndividualSelected(antId, finalRole, fStage, finalHealth, finalEnergy, finalHydration, finalDistance, finalTask));
        }
    }

    void pick(double fxX, double fxY, double paneW, double paneH) {
        app.enqueueTask(() -> {
            Camera cam = app.getCamera();
            float camW = cam != null ? cam.getWidth() : app.width;
            float camH = cam != null ? cam.getHeight() : app.height;
            float jmeX = (float) ((fxX / Math.max(1.0, paneW)) * camW);
            float jmeY = (float) ((1.0 - (fxY / Math.max(1.0, paneH))) * camH);

            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(jmeX, jmeY), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(jmeX, jmeY), 1f).subtractLocal(click3d).normalizeLocal();
            Ray ray = new Ray(click3d, dir);

            CollisionResults results = new CollisionResults();
            app.getRootNode().collideWith(ray, results);

            for (int i = 0; i < results.size(); i++) {
                Geometry hitGeom = results.getCollision(i).getGeometry();
                Spatial antSpatial = hitGeom;
                while (antSpatial != null && antSpatial.getUserData("ID") == null) {
                    antSpatial = antSpatial.getParent();
                }
                if (antSpatial != null && antSpatial.getUserData("ID") != null) {
                    String antId = (String) antSpatial.getUserData("ID");
                    String stage = antSpatial.getUserData("LifeStage") != null ? (String) antSpatial.getUserData("LifeStage") : "ADULT";
                    selectAndFollowAnt(antId, stage);
                    return;
                }
            }

            String bestAntId = null;
            String bestAntStage = null;
            float bestDistSq = Float.MAX_VALUE;
            boolean bestIsAdult = false;
            float maxClickRadiusSq = 28.0f * 28.0f;

            for (Map.Entry<String, Spatial> entry : app.antVisuals.entrySet()) {
                Spatial antSpatial = entry.getValue();
                if (antSpatial != null && antSpatial.getCullHint() != Spatial.CullHint.Always) {
                    Vector3f antPos = antSpatial.getWorldTranslation();
                    Vector3f screenPos = cam.getScreenCoordinates(antPos);
                    if (screenPos.z >= 0f && screenPos.z <= 1.0f) {
                        float dx = screenPos.x - jmeX;
                        float dy = screenPos.y - jmeY;
                        float distSq = dx * dx + dy * dy;
                        if (distSq <= maxClickRadiusSq) {
                            String stage = antSpatial.getUserData("LifeStage") != null ? (String) antSpatial.getUserData("LifeStage") : "ADULT";
                            boolean isAdult = "ADULT".equalsIgnoreCase(stage);
                            if (bestAntId == null || (isAdult && !bestIsAdult) || (isAdult == bestIsAdult && distSq < bestDistSq)) {
                                bestAntId = entry.getKey();
                                bestAntStage = stage;
                                bestDistSq = distSq;
                                bestIsAdult = isAdult;
                            }
                        }
                    }
                }
            }

            if (bestAntId != null) {
                selectAndFollowAnt(bestAntId, bestAntStage);
                return;
            }

            if (results.size() > 0) {
                CollisionResult closes = results.getClosestCollision();

                for (int i = 0; i < results.size(); i++) {
                    Spatial chamberSpatial = results.getCollision(i).getGeometry();
                    while (chamberSpatial != null && chamberSpatial.getUserData("ChamberID") == null && (chamberSpatial.getName() == null || !chamberSpatial.getName().startsWith("Node_"))) {
                        chamberSpatial = chamberSpatial.getParent();
                    }
                    if (chamberSpatial != null) {
                        String chamberIdStr = (String) chamberSpatial.getUserData("ChamberID");
                        if (chamberIdStr == null && chamberSpatial.getName() != null && chamberSpatial.getName().startsWith("Node_")) {
                            chamberIdStr = chamberSpatial.getName().substring(5);
                        }
                        if (chamberIdStr != null && app.selectionListener != null) {
                            final String fChamberId = chamberIdStr;
                            Platform.runLater(() -> app.selectionListener.onChamberSelected(fChamberId));
                            return;
                        }
                    }
                }

                Vector3f contact = closes.getContactPoint();
                if (contact != null && app.simulation != null && app.simulation.getTerrarium() != null && app.selectionListener != null) {
                    Terrarium terr = app.simulation.getTerrarium();
                    int vx = Math.max(0, Math.min(terr.getWidth() - 1, Math.round(contact.x)));
                    int vz = Math.max(0, Math.min(terr.getDepth() - 1, Math.round(contact.y)));
                    int vy = Math.max(0, Math.min(terr.getHeight() - 1, Math.round(contact.z)));
                    TerrariumCell cell = terr.getCell(vx, vy, vz);
                    String mat = cell != null ? cell.material().name() : "HUMUS";
                    float hum = cell != null ? cell.humidity() * 100.0f : 45.0f;
                    float temp = cell != null ? cell.temperature() : 19.0f;
                    float compaction = 60.0f;
                    final int fx = vx, fy = vy, fz = vz;
                    final String fMat = mat;
                    final float fHum = hum, fTemp = temp, fComp = compaction;
                    Platform.runLater(() -> app.selectionListener.onVoxelSelected(fx, fy, fz, fMat, fHum, fTemp, fComp));
                }
            }
        });
    }

    void hover(double fxX, double fxY, double paneW, double paneH) {
        long now = System.currentTimeMillis();
        if (now - lastHoverTimeMs < 40) return;
        lastHoverTimeMs = now;

        app.enqueueTask(() -> {
            Camera cam = app.getCamera();
            float camW = cam != null ? cam.getWidth() : app.width;
            float camH = cam != null ? cam.getHeight() : app.height;
            float jmeX = (float) ((fxX / Math.max(1.0, paneW)) * camW);
            float jmeY = (float) ((1.0 - (fxY / Math.max(1.0, paneH))) * camH);

            String bestHoverId = null;
            String bestHoverStage = null;
            float bestHoverDistSq = Float.MAX_VALUE;
            boolean bestHoverIsAdult = false;
            float maxHoverRadiusSq = 22.0f * 22.0f;

            for (Map.Entry<String, Spatial> entry : app.antVisuals.entrySet()) {
                Spatial antSpatial = entry.getValue();
                if (antSpatial != null && antSpatial.getCullHint() != Spatial.CullHint.Always) {
                    Vector3f antPos = antSpatial.getWorldTranslation();
                    Vector3f screenPos = cam.getScreenCoordinates(antPos);
                    if (screenPos.z >= 0f && screenPos.z <= 1.0f) {
                        float dx = screenPos.x - jmeX;
                        float dy = screenPos.y - jmeY;
                        float distSq = dx * dx + dy * dy;
                        if (distSq <= maxHoverRadiusSq) {
                            String stage = antSpatial.getUserData("LifeStage") != null ? (String) antSpatial.getUserData("LifeStage") : "ADULT";
                            boolean isAdult = "ADULT".equalsIgnoreCase(stage);
                            if (bestHoverId == null || (isAdult && !bestHoverIsAdult) || (isAdult == bestHoverIsAdult && distSq < bestHoverDistSq)) {
                                bestHoverId = entry.getKey();
                                bestHoverStage = stage;
                                bestHoverDistSq = distSq;
                                bestHoverIsAdult = isAdult;
                            }
                        }
                    }
                }
            }

            if (bestHoverId != null) {
                final String fId = bestHoverId;
                final String fStage = bestHoverStage;
                if (app.selectionListener != null) {
                    Platform.runLater(() -> app.selectionListener.onHoverInfo("ðŸœ Fourmi #" + fId + " [" + fStage + "]"));
                }
                return;
            }

            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(jmeX, jmeY), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(jmeX, jmeY), 1f).subtractLocal(click3d).normalizeLocal();
            Ray ray = new Ray(click3d, dir);

            CollisionResults results = new CollisionResults();
            app.getRootNode().collideWith(ray, results);

            for (int i = 0; i < results.size(); i++) {
                Spatial chamberSpatial = results.getCollision(i).getGeometry();
                while (chamberSpatial != null && chamberSpatial.getUserData("ChamberID") == null && (chamberSpatial.getName() == null || !chamberSpatial.getName().startsWith("Node_"))) {
                    chamberSpatial = chamberSpatial.getParent();
                }
                if (chamberSpatial != null) {
                    String typeStr = (String) chamberSpatial.getUserData("ChamberType");
                    if (typeStr == null) typeStr = "CHAMBRE";
                    final String fType = typeStr;
                    if (app.selectionListener != null) {
                        Platform.runLater(() -> app.selectionListener.onHoverInfo("ðŸ›ï¸ CavitÃ©: " + fType));
                    }
                    return;
                }
            }

            if (results.size() > 0) {
                CollisionResult closes = results.getClosestCollision();
                Vector3f contact = closes.getContactPoint();
                Terrarium terr = (app.simulation != null && app.simulation.getTerrarium() != null)
                        ? app.simulation.getTerrarium() : app.lastTerrarium;
                if (contact != null && terr != null && app.selectionListener != null) {
                    int vx = Math.max(0, Math.min(terr.getWidth() - 1, Math.round(contact.x)));
                    int vz = Math.max(0, Math.min(terr.getDepth() - 1, Math.round(contact.y)));
                    int vy = Math.max(0, Math.min(terr.getHeight() - 1, Math.round(contact.z)));
                    TerrariumCell cell = terr.getCell(vx, vy, vz);
                    String mat = cell != null ? cell.material().name() : "HUMUS";
                    float hum = cell != null ? cell.humidity() * 100.0f : 45.0f;
                    float temp = cell != null ? cell.temperature() : 19.0f;
                    float compaction = 60.0f;
                    final int fx = vx, fy = vy, fz = vz;
                    final String fMat = mat;
                    final float fHum = hum, fTemp = temp, fComp = compaction;
                    Platform.runLater(() -> app.selectionListener.onVoxelSelected(fx, fy, fz, fMat, fHum, fTemp, fComp));
                }
            }
        });
    }

    void rotateCamera(float x, float y) {
        JmeCameraNavigationController.rotateCamera(app, x, y);
    }

    void panCamera(float dx, float dy) {
        JmeCameraNavigationController.panCamera(app, dx, dy);
    }

    void zoomCamera(float delta, double fxX, double fxY, double paneW, double paneH) {
        JmeCameraNavigationController.zoomCamera(app, delta, fxX, fxY, paneW, paneH);
    }

    void panCameraTo(float x, float y, float z) {
        JmeCameraNavigationController.panCameraTo(app, x, y, z);
    }

    void resetCamera() {
        JmeCameraNavigationController.resetCamera(app);
    }

    void setTopDownView() {
        JmeCameraNavigationController.setTopDownView(app);
    }

    Node createSelectionReticle() {
        return JmeCameraNavigationController.createSelectionReticle(app);
    }
}