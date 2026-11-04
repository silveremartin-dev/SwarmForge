/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.client.view.JmeGameApp;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Handles mouse interactions and fullscreen keyboard/stage events for the client 3D viewport.
 */
class ClientViewportInputHelper {

    private final SwarmForgeClient client;
    private double lastX;
    private double lastY;

    ClientViewportInputHelper(SwarmForgeClient client) {
        this.client = client;
    }

    void wireFullscreenSupport(BorderPane rootPane, ScrollPane sideScroll, Button btnFullscreenMode, Button btnExitFullscreen, CheckBox chkMinimap) {
        Runnable toggleFullscreen = () -> {
            Stage stage = rootPane.getScene() != null ? (Stage) rootPane.getScene().getWindow() : null;
            if (stage != null) {
                boolean willBeFS = !stage.isFullScreen();
                if (willBeFS) {
                    if (client.mainTabs != null && client.simTab != null) {
                        client.mainTabs.getSelectionModel().select(client.simTab);
                    }
                    if (client.simSubTabs != null && client.visualTab != null) {
                        client.simSubTabs.getSelectionModel().select(client.visualTab);
                    }
                }
                stage.setFullScreen(willBeFS);
            }
        };

        btnFullscreenMode.setOnAction(e -> toggleFullscreen.run());
        btnExitFullscreen.setOnAction(e -> toggleFullscreen.run());

        Consumer<Stage> registerFsListener = new Consumer<Stage>() {
            private final Set<Stage> registered = new HashSet<>();

            @Override
            public void accept(Stage stage) {
                if (stage == null || !registered.add(stage)) return;
                stage.fullScreenProperty().addListener((fsObs, oldFS, isFS) -> {
                    if (isFS) {
                        if (client.mainTabs != null && client.simTab != null) {
                            client.mainTabs.getSelectionModel().select(client.simTab);
                        }
                        if (client.simSubTabs != null && client.visualTab != null) {
                            client.simSubTabs.getSelectionModel().select(client.visualTab);
                        }
                        if (client.mainTabs != null) {
                            if (!client.mainTabs.getStyleClass().contains("tab-pane-fullscreen")) {
                                client.mainTabs.getStyleClass().add("tab-pane-fullscreen");
                            }
                            Node hdr = client.mainTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(false); hdr.setManaged(false); }
                        }
                        if (client.simSubTabs != null) {
                            if (!client.simSubTabs.getStyleClass().contains("tab-pane-fullscreen")) {
                                client.simSubTabs.getStyleClass().add("tab-pane-fullscreen");
                            }
                            Node hdr = client.simSubTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(false); hdr.setManaged(false); }
                        }
                        if (client.connectBox != null) {
                            client.connectBox.setVisible(false);
                            client.connectBox.setManaged(false);
                        }
                        rootPane.setRight(null);
                        if (client.simWorldViewer != null) {
                            client.simWorldViewer.setFullscreenMode(true);
                        }
                        if (client.worldEditorPane != null) {
                            client.worldEditorPane.setFullscreenMode(true);
                        }
                        btnExitFullscreen.setVisible(false);
                    } else {
                        if (client.mainTabs != null) {
                            client.mainTabs.getStyleClass().remove("tab-pane-fullscreen");
                            Node hdr = client.mainTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(true); hdr.setManaged(true); }
                        }
                        if (client.simSubTabs != null) {
                            client.simSubTabs.getStyleClass().remove("tab-pane-fullscreen");
                            Node hdr = client.simSubTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(true); hdr.setManaged(true); }
                        }
                        if (client.connectBox != null) {
                            client.connectBox.setVisible(true);
                            client.connectBox.setManaged(true);
                        }
                        rootPane.setRight(sideScroll);
                        if (client.simWorldViewer != null) {
                            client.simWorldViewer.setFullscreenMode(false);
                            client.simWorldViewer.setDualMinimapVisible(chkMinimap.isSelected());
                        }
                        if (client.worldEditorPane != null) {
                            client.worldEditorPane.setFullscreenMode(false);
                        }
                        btnExitFullscreen.setVisible(false);
                    }
                    client.update3DRenderingState();
                    if (client.simWorldViewer != null) {
                        client.simWorldViewer.repaintAllViews();
                    }
                    if (client.worldEditorPane != null) {
                        client.worldEditorPane.repaintAllViews();
                    }
                });
            }
        };

        Consumer<Scene> onSceneAttached = scene -> {
            if (scene == null) return;
            scene.setOnKeyPressed(ke -> {
                if (ke.getCode() == KeyCode.F11) {
                    toggleFullscreen.run();
                    ke.consume();
                }
            });
            scene.windowProperty().addListener((wObs, oldW, newW) -> {
                if (newW instanceof Stage s) registerFsListener.accept(s);
            });
            if (scene.getWindow() instanceof Stage s) {
                registerFsListener.accept(s);
            }
        };

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> onSceneAttached.accept(newScene));
        onSceneAttached.accept(rootPane.getScene());
    }

    void setupMouseControls(GameViewPane view) {
        view.setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        view.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            if (e.isPrimaryButtonDown()) {
                view.getGameApp().rotateCamera((float) dx, (float) dy);
            } else if (e.isSecondaryButtonDown() || e.isShiftDown() || e.isMiddleButtonDown()) {
                view.getGameApp().panCamera((float) -dx, (float) dy);
            }

            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        view.setOnScroll(e -> {
            double delta = e.getDeltaY();
            view.getGameApp().zoomCamera((float) delta * 0.05f, e.getX(), e.getY(), view.getWidth(), view.getHeight());
        });

        view.getGameApp().setSelectionListener(new JmeGameApp.ObjectSelectionListener() {
            @Override
            public void onIndividualSelected(String id, String caste, String stage, float health, float energy, float hunger, float age, String job) {
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.setFollowedIndividualById(id);
                }
            }

            @Override
            public void onChamberSelected(String chamberId) {
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.selectChamberById(chamberId);
                }
            }

            @Override
            public void onVoxelSelected(int x, int y, int z, String material, float moisture, float temp, float compaction) {
                if (client.simWorldViewer != null && client.simWorldViewer.getLblHoverInfo() != null) {
                    client.simWorldViewer.getLblHoverInfo().setText("📍 Voxel: (" + x + ", " + y + ", " + z + ") | " + material + " | Hum: " + (int) moisture + "% | Temp: " + String.format(Locale.US, "%.1f", temp) + "°C");
                }
            }

            @Override
            public void onHoverInfo(String text) {
                if (client.simWorldViewer != null && client.simWorldViewer.getLblHoverInfo() != null && text != null) {
                    client.simWorldViewer.getLblHoverInfo().setText(text);
                }
            }
        });

        view.setOnMouseMoved(e -> {
            if (view.getGameApp() != null) {
                view.getGameApp().hover(e.getX(), e.getY(), view.getWidth(), view.getHeight());
            }
        });

        view.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                view.getGameApp().resetCamera();
            } else if (e.getClickCount() == 1) {
                view.getGameApp().pick(e.getX(), e.getY(), view.getWidth(), view.getHeight());
            }
        });
    }
}
