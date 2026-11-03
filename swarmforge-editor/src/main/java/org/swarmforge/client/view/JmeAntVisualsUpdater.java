/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Individual.Caste;
import org.swarmforge.core.domain.Individual.LifeStage;
import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.species.Species;
import org.swarmforge.protocol.grpc.IndividualDelta;

import java.util.*;

/**
 * Handles 3D individual insect visual synchronization, instanced scene nodes,
 * LOD distance magnification, and coordinate mapping for {@link JmeGameApp}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class JmeAntVisualsUpdater {

    private final JmeGameApp app;

    JmeAntVisualsUpdater(JmeGameApp app) {
        this.app = app;
    }

    void initializeInstancing() {
        for (Caste caste : Caste.values()) {
            Node node = new Node("CasteNode_" + caste);
            app.insectVisualizer.registerInstancedNode(caste, node);
            app.getRootNode().attachChild(node);
        }
    }

    void updateAntVisuals(float tpf) {
        if (app.insectVisualizer == null) {
            app.insectVisualizer = new InsectVisualizer(app.getAssetManager());
            initializeInstancing();
        }

        if (app.simulation != null && app.simulation.getTerrarium() != null) {
            app.insectVisualizer.setTerrainDimensions(10.0f, app.simulation.getTerrarium().getWidth());
        }

        Set<String> activeIds = new HashSet<>();

        List<IndividualDelta> individuals = (app.getNetworkClient() != null
                && app.getNetworkClient().isConnected())
                        ? app.getNetworkClient().getLatestIndividuals()
                        : Collections.emptyList();

        if (!individuals.isEmpty()) {
            for (IndividualDelta ind : individuals) {
                String id = ind.getId();
                IndividualDelta.LifeStage protoStage = ind.getLifeStage();
                LifeStage lifeStage = LifeStage.valueOf(protoStage.name());
                updateSingleAntVisual(id, Caste.WORKER, lifeStage,
                        ind.getPosition().getX(), ind.getPosition().getZ(), ind.getPosition().getY(), ind.getHeading(), activeIds, null, null);
            }
        } else if (app.simulation != null) {
            for (Colony colony : app.simulation.getColonies()) {
                for (Individual ind : colony.getLivingIndividuals()) {
                    String id = ind.getId().toString();
                    Caste caste = ind.getCaste() != null ? ind.getCaste() : Caste.WORKER;
                    LifeStage stage = ind.getLifeStage() != null ? ind.getLifeStage() : LifeStage.ADULT;
                    updateSingleAntVisual(id, caste, stage, ind.getX(), ind.getZ(), ind.getY(), ind.getHeading(), activeIds, ind.getSpecies(), ind.getCasteTemplate());
                }
            }
            if (app.simulation.getPredatorManager() != null) {
                for (Predator pred : app.simulation.getPredatorManager().getPredators()) {
                    if (pred != null && pred.isAlive()) {
                        String id = "Predator_" + pred.getId();
                        updateSingleAntVisual(id, Caste.SOLDIER, LifeStage.ADULT,
                                pred.getX(), pred.getZ(), pred.getY(), pred.getHeading(), activeIds, null, null);
                    }
                }
            }
        }

        Iterator<Map.Entry<String, Spatial>> it = app.antVisuals.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Spatial> entry = it.next();
            if (!activeIds.contains(entry.getKey())) {
                entry.getValue().removeFromParent();
                it.remove();
            }
        }
    }

    private void updateSingleAntVisual(String id, Caste caste,
                                       LifeStage lifeStage,
                                       float x, float y, float z, float heading,
                                       Set<String> activeIds,
                                       Species species,
                                       CasteTemplate template) {
        if (!app.isAntsVisible()) return;
        activeIds.add(id);

        Geometry antGeom = (Geometry) app.antVisuals.get(id);

        if (antGeom != null) {
            String currentStageName = (String) antGeom.getUserData("LifeStage");
            if (currentStageName != null && !currentStageName.equals(lifeStage.name())) {
                antGeom.removeFromParent();
                app.antVisuals.remove(id);
                antGeom = null;
            }
        }

        if (antGeom == null) {
            antGeom = app.insectVisualizer.createOrganismGeometry(caste, lifeStage, species);
            antGeom.setUserData("LifeStage", lifeStage.name());
            antGeom.setUserData("ID", id);

            Node node = app.insectVisualizer.getInstancedNode(caste);
            if (node != null) {
                node.attachChild(antGeom);
            } else {
                app.getRootNode().attachChild(antGeom);
            }
            app.antVisuals.put(id, antGeom);
        }

        if (app.simulation != null && app.simulation.getTerrarium() != null) {
            float surfaceElevation = app.simulation.getTerrarium().getSurfaceElevation(x, z);
            if (y >= surfaceElevation - 0.5f) {
                y = surfaceElevation + 0.05f;
            }
        }

        antGeom.setLocalTranslation(x, y, z);
        antGeom.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));

        float baseScale = app.insectVisualizer.getScaleForIndividual(caste, lifeStage, species, template);
        int hash = id != null ? Math.abs(id.hashCode()) : 0;
        float variance = 1.0f + (((hash % 1000) / 1000.0f) - 0.5f) * 0.10f;

        Vector3f camLoc = app.getCamera() != null ? app.getCamera().getLocation() : app.cameraTarget;
        float distToCam = camLoc.distance(antGeom.getLocalTranslation());
        float distMin = 3.0f;
        float distMax = 35.0f;
        float t = Math.max(0.0f, Math.min(1.0f, (distToCam - distMin) / (distMax - distMin)));
        float smoothT = t * t * (3.0f - 2.0f * t);
        float maxFarMagnification = Math.max(1.0f, app.getAntVisualScaleMultiplier() * 3.5f);
        float magnification = 1.0f + smoothT * (maxFarMagnification - 1.0f);

        antGeom.setLocalScale(baseScale * variance * magnification);
    }
}
