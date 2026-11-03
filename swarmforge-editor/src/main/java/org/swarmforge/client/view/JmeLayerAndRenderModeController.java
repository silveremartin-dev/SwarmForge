/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.domain.Individual.Caste;

/**
 * Manages 3D layer visibility (terrain, tunnels, ants, pheromones, weather, vegetation)
 * and rendering modes (realistic, scientific, gamified voxel, UV, ommatidial compound-eye)
 * for {@link JmeGameApp}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class JmeLayerAndRenderModeController {

    private final JmeGameApp app;

    boolean terrainVisible = true;
    boolean tunnelsVisible = true;
    boolean antsVisible = true;
    boolean pheromonesVisible = true;
    boolean weatherVisible = true;
    boolean vegetationVisible = true;

    private Node ommatidialGuiNode = null;
    private boolean ommatidialVisionActive = false;
    private boolean uvVisionActive = false;

    JmeLayerAndRenderModeController(JmeGameApp app) {
        this.app = app;
    }

    void setUVVisionMode(boolean enabled) {
        this.uvVisionActive = enabled;
        app.enqueueTask(() -> {
            if (app.vegetationVisualizer != null) {
                app.vegetationVisualizer.setUVVisionMode(enabled);
            }
            if (app.getViewPort() != null) {
                if (enabled) {
                    app.getViewPort().setBackgroundColor(new ColorRGBA(0.08f, 0.04f, 0.22f, 1.0f));
                    if (app.sunLight != null) {
                        app.sunLight.setColor(new ColorRGBA(0.85f, 0.55f, 1.45f, 1.0f));
                    }
                } else {
                    applyRenderModeInternal(app.currentRenderMode);
                }
            }
        });
    }

    boolean isUVVisionMode() {
        return uvVisionActive;
    }

    void setOmmatidialVisionMode(boolean enabled) {
        this.ommatidialVisionActive = enabled;
        app.enqueueTask(() -> {
            if (app.vegetationVisualizer != null) {
                app.vegetationVisualizer.setOmmatidialVisionMode(enabled);
            }
            if (ommatidialGuiNode == null) {
                ommatidialGuiNode = app.getSceneBuilder().createOmmatidialGuiOverlay();
            }
            if (enabled) {
                if (ommatidialGuiNode != null && ommatidialGuiNode.getParent() == null) {
                    app.getGuiNode().attachChild(ommatidialGuiNode);
                }
            } else if (ommatidialGuiNode != null) {
                ommatidialGuiNode.removeFromParent();
            }
        });
    }

    boolean isOmmatidialVisionMode() {
        return ommatidialVisionActive;
    }

    void applyRenderModeInternal(RenderMode mode) {
        if (mode == null) mode = RenderMode.REALISTIC;
        app.currentRenderMode = mode;
        app.isGamifiedVoxelMode = (mode == RenderMode.GAMIFIED);

        if (app.getViewPort() != null) {
            if (mode == RenderMode.GAMIFIED) {
                app.getViewPort().setBackgroundColor(new ColorRGBA(0.22f, 0.47f, 0.88f, 1.0f));
            } else {
                app.getViewPort().setBackgroundColor(new ColorRGBA(0.48f, 0.68f, 0.85f, 1.0f));
            }
            if (app.sunLight != null) {
                if (mode == RenderMode.GAMIFIED) {
                    app.sunLight.setColor(new ColorRGBA(1.3f, 1.25f, 1.2f, 1.0f));
                } else {
                    app.sunLight.setColor(new ColorRGBA(1.35f, 1.30f, 1.18f, 1.0f));
                }
            }
        }
        if (app.vegetationVisualizer != null) {
            app.vegetationVisualizer.setRenderMode(mode);
            app.getSceneBuilder().checkAndUpdateNestArchitectures();
        }
        if (app.tunnelVisualizer != null) {
            app.tunnelVisualizer.setRenderMode(mode);
        }
        if (app.weatherVisualizer != null) {
            app.weatherVisualizer.setRenderMode(mode);
        }
        if (app.pheromoneVisualizer != null) {
            app.pheromoneVisualizer.setRenderMode(mode);
        }
        app.rebuildTerrainMesh();
    }

    void setTerrainVisible(boolean visible) {
        this.terrainVisible = visible;
        app.enqueueTask(() -> {
            if (app.terrainNode != null) {
                if (visible) {
                    if (app.terrainNode.getParent() == null) app.getRootNode().attachChild(app.terrainNode);
                } else {
                    app.terrainNode.removeFromParent();
                }
            }
        });
    }

    void setTunnelsVisible(boolean visible) {
        this.tunnelsVisible = visible;
        app.enqueueTask(() -> {
            if (app.tunnelVisualizer != null && app.tunnelVisualizer.getRootNode() != null) {
                if (visible) {
                    if (app.tunnelVisualizer.getRootNode().getParent() == null) app.getRootNode().attachChild(app.tunnelVisualizer.getRootNode());
                } else {
                    app.tunnelVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    void setAntsVisible(boolean visible) {
        this.antsVisible = visible;
        app.enqueueTask(() -> {
            if (app.insectVisualizer != null) {
                for (Caste caste : Caste.values()) {
                    Node node = app.insectVisualizer.getInstancedNode(caste);
                    if (node != null) {
                        if (visible) {
                            if (node.getParent() == null) app.getRootNode().attachChild(node);
                        } else {
                            node.removeFromParent();
                        }
                    }
                }
            }
            if (!visible) {
                for (Spatial ant : app.antVisuals.values()) {
                    ant.removeFromParent();
                }
                app.antVisuals.clear();
            }
        });
    }

    void setPheromonesVisible(boolean visible) {
        this.pheromonesVisible = visible;
        app.enqueueTask(() -> {
            if (app.pheromoneVisualizer != null && app.pheromoneVisualizer.getRootNode() != null) {
                if (visible) {
                    if (app.pheromoneVisualizer.getRootNode().getParent() == null) app.getRootNode().attachChild(app.pheromoneVisualizer.getRootNode());
                } else {
                    app.pheromoneVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    void setWeatherVisible(boolean visible) {
        this.weatherVisible = visible;
        app.enqueueTask(() -> {
            if (app.weatherVisualizer != null && app.weatherVisualizer.getRootNode() != null) {
                if (visible) {
                    if (app.weatherVisualizer.getRootNode().getParent() == null) app.getRootNode().attachChild(app.weatherVisualizer.getRootNode());
                } else {
                    app.weatherVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }

    void setVegetationVisible(boolean visible) {
        this.vegetationVisible = visible;
        app.enqueueTask(() -> {
            if (app.vegetationVisualizer != null && app.vegetationVisualizer.getRootNode() != null) {
                if (visible) {
                    if (app.vegetationVisualizer.getRootNode().getParent() == null) app.getRootNode().attachChild(app.vegetationVisualizer.getRootNode());
                } else {
                    app.vegetationVisualizer.getRootNode().removeFromParent();
                }
            }
        });
    }
}
