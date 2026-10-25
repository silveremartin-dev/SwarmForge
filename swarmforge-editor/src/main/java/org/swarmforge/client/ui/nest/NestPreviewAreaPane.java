/*
 * SwarmForge - Ant Colony & Insect Simulation Platform
 * Copyright (c) 2024-2026 Silvère Martin-Michiellot & SwarmForge Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swarmforge.client.ui.nest;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import org.swarmforge.client.ui.NestGeneratorPane;
import org.swarmforge.client.ui.NestRenderer;
import org.swarmforge.client.ui.ResizableCanvas;
import org.swarmforge.client.util.CanvasInteractionHandler;
import org.swarmforge.client.util.I18nManager;

/**
 * Interactive 3D and Orthographic 2D multi-view preview canvas pane for the Nest Generator.
 * Displays isometric 3D view with camera orbit/zoom/pan, side elevation view, top plan view,
 * and biological chamber legend bar.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestPreviewAreaPane extends VBox {

    private final Supplier<NestGeneratorPane.GeneratedNest> nestSupplier;
    private final DoubleSupplier tunnelWidthSupplier;

    // View Canvas & Graphic Contexts
    private ResizableCanvas canvas3D;
    private GraphicsContext gc3D;
    private ResizableCanvas canvasSide;
    private GraphicsContext gcSide;
    private ResizableCanvas canvasTop;
    private GraphicsContext gcTop;

    // Camera 3D View Parameters
    private double azimuth = 45;
    private double elevation = 35;
    private double zoom = 7.5;
    private double pan3DX = 0.0;
    private double pan3DY = 0.0;
    private double lastMX;
    private double lastMY;

    // 2D Orthogonal Camera Parameters
    private double sideZoom = 1.0;
    private double sidePanX = 0.0;
    private double sidePanY = 0.0;
    private double lastSideMX;
    private double lastSideMY;

    private double topZoom = 1.0;
    private double topPanX = 0.0;
    private double topPanY = 0.0;
    private double lastTopMX;
    private double lastTopMY;

    // UI Legend & View Controls
    private CheckBox showGhostMeshCheckBox;
    private CheckBox syncViewsCheckBox;

    public NestPreviewAreaPane(
            Supplier<NestGeneratorPane.GeneratedNest> nestSupplier,
            DoubleSupplier tunnelWidthSupplier) {
        this.nestSupplier = nestSupplier;
        this.tunnelWidthSupplier = tunnelWidthSupplier;

        setSpacing(4);
        buildUI();
    }

    private void buildUI() {
        I18nManager i18n = I18nManager.getInstance();
        canvas3D = new ResizableCanvas(540, 510);
        gc3D = canvas3D.getGraphicsContext2D();

        canvasSide = new ResizableCanvas(215, 245);
        gcSide = canvasSide.getGraphicsContext2D();

        canvasTop = new ResizableCanvas(215, 245);
        gcTop = canvasTop.getGraphicsContext2D();

        setupMouse();

        Pane h3d = new Pane(canvas3D);
        h3d.setStyle("-fx-border-color:#555;-fx-border-width:1;");
        HBox.setHgrow(h3d, Priority.ALWAYS);

        canvas3D.widthProperty().bind(h3d.widthProperty());
        canvas3D.heightProperty().bind(h3d.heightProperty());
        canvas3D.widthProperty().addListener((obs, oldV, newV) -> repaint());
        canvas3D.heightProperty().addListener((obs, oldV, newV) -> repaint());

        Pane hSide = new Pane(canvasSide);
        hSide.setStyle("-fx-border-color:#444;-fx-border-width:1;");
        hSide.setPrefSize(215, 245);
        canvasSide.widthProperty().bind(hSide.widthProperty());
        canvasSide.heightProperty().bind(hSide.heightProperty());
        canvasSide.widthProperty().addListener((obs, oldV, newV) -> repaint());
        canvasSide.heightProperty().addListener((obs, oldV, newV) -> repaint());

        Pane hTop = new Pane(canvasTop);
        hTop.setStyle("-fx-border-color:#444;-fx-border-width:1;");
        hTop.setPrefSize(215, 245);
        canvasTop.widthProperty().bind(hTop.widthProperty());
        canvasTop.heightProperty().bind(hTop.heightProperty());
        canvasTop.widthProperty().addListener((obs, oldV, newV) -> repaint());
        canvasTop.heightProperty().addListener((obs, oldV, newV) -> repaint());

        Label ls = new Label();
        ls.textProperty().bind(i18n.createStringBinding("nest.view.side"));
        ls.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        Label lt = new Label();
        lt.textProperty().bind(i18n.createStringBinding("nest.view.top"));
        lt.setStyle("-fx-font-size:11;-fx-font-weight:bold;");

        VBox side = new VBox(5, ls, hSide, lt, hTop);
        side.setPadding(new Insets(0, 4, 0, 8));
        side.setAlignment(Pos.TOP_CENTER);

        HBox area = new HBox(6, h3d, side);
        area.setPadding(new Insets(8, 8, 4, 8));
        VBox.setVgrow(area, Priority.ALWAYS);

        HBox legendBar = buildLegendBar();
        getChildren().addAll(area, legendBar);
    }

    private HBox buildLegendBar() {
        I18nManager i18n = I18nManager.getInstance();
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(4, 10, 6, 10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("legend-bar");

        showGhostMeshCheckBox = new CheckBox();
        showGhostMeshCheckBox.textProperty().bind(i18n.createStringBinding("nest.chk.ghost_mesh"));
        showGhostMeshCheckBox.setSelected(true);
        showGhostMeshCheckBox.getStyleClass().add("legend-checkbox");
        showGhostMeshCheckBox.setTooltip(new Tooltip());
        showGhostMeshCheckBox.getTooltip().textProperty().bind(i18n.createStringBinding("nest.chk.ghost_mesh.tt"));
        showGhostMeshCheckBox.setOnAction(e -> repaint());

        syncViewsCheckBox = new CheckBox();
        syncViewsCheckBox.textProperty().bind(i18n.createStringBinding("nest.chk.sync_views"));
        syncViewsCheckBox.setSelected(true);
        syncViewsCheckBox.getStyleClass().add("legend-checkbox");

        HBox legendItemsContainer = new HBox(10);
        legendItemsContainer.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("nest.legend.title"));
        title.getStyleClass().add("legend-title");

        legendItemsContainer.getChildren().addAll(new Separator(Orientation.VERTICAL), title);

        Object[][] items = {
                {"nest.legend.entrance", NestRenderer.CHAMBER_COLOR_ENTRANCE},
                {"nest.legend.queen",    NestRenderer.CHAMBER_COLOR_QUEEN},
                {"nest.legend.brood",    NestRenderer.CHAMBER_COLOR_BROOD},
                {"nest.legend.storage",  NestRenderer.CHAMBER_COLOR_STORAGE},
                {"nest.legend.fungus",   NestRenderer.CHAMBER_COLOR_FUNGUS},
                {"nest.legend.waste",    NestRenderer.CHAMBER_COLOR_WASTE},
                {"nest.legend.tunnel",   NestRenderer.CHAMBER_COLOR_TUNNEL}
        };

        for (Object[] it : items) {
            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("legend-item");
            Canvas dot = new Canvas(9, 9);
            GraphicsContext g = dot.getGraphicsContext2D();
            g.setFill((Color) it[1]);
            g.fillOval(0, 0, 9, 9);
            g.setStroke(Color.WHITE);
            g.setLineWidth(0.5);
            g.strokeOval(0, 0, 9, 9);

            Label lbl = new Label();
            lbl.textProperty().bind(i18n.createStringBinding((String) it[0]));
            item.getChildren().addAll(dot, lbl);
            legendItemsContainer.getChildren().add(item);
        }

        bar.getChildren().addAll(showGhostMeshCheckBox, syncViewsCheckBox, legendItemsContainer);
        return bar;
    }

    private void setupMouse() {
        // 3D Canvas Orbit & Zoom & Pan
        canvas3D.setOnMousePressed(e -> { lastMX = e.getX(); lastMY = e.getY(); });
        canvas3D.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMX;
            double dy = e.getY() - lastMY;
            if (e.isSecondaryButtonDown() || e.isShiftDown()) {
                pan3DX += dx;
                pan3DY += dy;
                if (isSync()) {
                    sidePanX = pan3DX; sidePanY = pan3DY;
                    topPanX = pan3DX; topPanY = pan3DY;
                }
            } else {
                azimuth = (azimuth + dx * 0.55) % 360;
                if (azimuth < 0) azimuth += 360;
                elevation = Math.max(5, Math.min(85, elevation - dy * 0.35));
            }
            lastMX = e.getX(); lastMY = e.getY();
            repaint();
        });
        canvas3D.setOnScroll(e -> {
            double oldZoom = zoom;
            zoom = Math.max(1.0, Math.min(100.0, zoom + e.getDeltaY() * 0.04 * (zoom / 10.0 + 0.5)));
            if (zoom != oldZoom) {
                double scaleRatio = zoom / oldZoom;
                double cx = canvas3D.getWidth() / 2.0;
                double cy = canvas3D.getHeight() / 2.0;
                pan3DX = CanvasInteractionHandler.calculatePointerAnchoredPan(pan3DX, e.getX(), cx, scaleRatio);
                pan3DY = CanvasInteractionHandler.calculatePointerAnchoredPan(pan3DY, e.getY(), cy, scaleRatio);
                if (isSync()) {
                    sideZoom = Math.max(0.1, Math.min(25.0, zoom / 7.5));
                    topZoom = sideZoom;
                    sidePanX = pan3DX; sidePanY = pan3DY;
                    topPanX = pan3DX; topPanY = pan3DY;
                }
            }
            repaint();
        });
        canvas3D.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });

        // 2D Side View Zoom & Pan
        canvasSide.setOnMousePressed(e -> { lastSideMX = e.getX(); lastSideMY = e.getY(); });
        canvasSide.setOnMouseDragged(e -> {
            double dx = e.getX() - lastSideMX;
            double dy = e.getY() - lastSideMY;
            sidePanX += dx;
            sidePanY += dy;
            if (isSync()) {
                pan3DX = sidePanX; pan3DY = sidePanY;
                topPanX = sidePanX; topPanY = sidePanY;
            }
            lastSideMX = e.getX(); lastSideMY = e.getY();
            repaint();
        });
        canvasSide.setOnScroll(e -> {
            double oldZoom = sideZoom;
            sideZoom = Math.max(0.3, Math.min(6.0, sideZoom + e.getDeltaY() * 0.003));
            if (sideZoom != oldZoom) {
                double scaleRatio = sideZoom / oldZoom;
                double cx = canvasSide.getWidth() / 2.0;
                double cy = canvasSide.getHeight() / 2.0;
                sidePanX = CanvasInteractionHandler.calculatePointerAnchoredPan(sidePanX, e.getX(), cx, scaleRatio);
                sidePanY = CanvasInteractionHandler.calculatePointerAnchoredPan(sidePanY, e.getY(), cy, scaleRatio);
                if (isSync()) {
                    topZoom = sideZoom;
                    zoom = Math.max(2.5, Math.min(22.0, sideZoom * 7.5));
                    pan3DX = sidePanX; pan3DY = sidePanY;
                    topPanX = sidePanX; topPanY = sidePanY;
                }
            }
            repaint();
        });
        canvasSide.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });

        // 2D Top View Zoom & Pan
        canvasTop.setOnMousePressed(e -> { lastTopMX = e.getX(); lastTopMY = e.getY(); });
        canvasTop.setOnMouseDragged(e -> {
            double dx = e.getX() - lastTopMX;
            double dy = e.getY() - lastTopMY;
            topPanX += dx;
            topPanY += dy;
            if (isSync()) {
                sidePanX = topPanX; sidePanY = topPanY;
                pan3DX = topPanX; pan3DY = topPanY;
            }
            lastTopMX = e.getX(); lastTopMY = e.getY();
            repaint();
        });
        canvasTop.setOnScroll(e -> {
            double oldZoom = topZoom;
            topZoom = Math.max(0.3, Math.min(6.0, topZoom + e.getDeltaY() * 0.003));
            if (topZoom != oldZoom) {
                double scaleRatio = topZoom / oldZoom;
                double cx = canvasTop.getWidth() / 2.0;
                double cy = canvasTop.getHeight() / 2.0;
                topPanX = CanvasInteractionHandler.calculatePointerAnchoredPan(topPanX, e.getX(), cx, scaleRatio);
                topPanY = CanvasInteractionHandler.calculatePointerAnchoredPan(topPanY, e.getY(), cy, scaleRatio);
                if (isSync()) {
                    sideZoom = topZoom;
                    zoom = Math.max(2.5, Math.min(22.0, topZoom * 7.5));
                    sidePanX = topPanX; sidePanY = topPanY;
                    pan3DX = topPanX; pan3DY = topPanY;
                }
            }
            repaint();
        });
        canvasTop.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetAllCameras();
            }
        });
    }

    public boolean isSync() {
        return syncViewsCheckBox != null && syncViewsCheckBox.isSelected();
    }

    public void resetAllCameras() {
        azimuth = 45; elevation = 35; zoom = 7.5;
        pan3DX = 0; pan3DY = 0;
        sideZoom = 1.0; sidePanX = 0; sidePanY = 0;
        topZoom = 1.0; topPanX = 0; topPanY = 0;
        repaint();
    }

    public void repaint() {
        draw3D();
        drawSide();
        drawTop();
    }

    private void draw3D() {
        NestGeneratorPane.GeneratedNest nest = nestSupplier != null ? nestSupplier.get() : null;
        double tunnelW = tunnelWidthSupplier != null ? tunnelWidthSupplier.getAsDouble() : 1.0;
        if (canvas3D != null && gc3D != null && canvas3D.getWidth() >= 10 && canvas3D.getHeight() >= 10 && nest != null) {
            NestRenderer.draw3D(nest, gc3D, canvas3D.getWidth(), canvas3D.getHeight(), azimuth, elevation, zoom, tunnelW, pan3DX, pan3DY, showGhostMeshCheckBox == null || showGhostMeshCheckBox.isSelected());
        }
    }

    private void drawSide() {
        NestGeneratorPane.GeneratedNest nest = nestSupplier != null ? nestSupplier.get() : null;
        double tunnelW = tunnelWidthSupplier != null ? tunnelWidthSupplier.getAsDouble() : 1.0;
        if (canvasSide != null && gcSide != null && canvasSide.getWidth() >= 10 && canvasSide.getHeight() >= 10 && nest != null) {
            NestRenderer.drawSide(nest, gcSide, canvasSide.getWidth(), canvasSide.getHeight(), tunnelW, sideZoom, sidePanX, sidePanY);
        }
    }

    private void drawTop() {
        NestGeneratorPane.GeneratedNest nest = nestSupplier != null ? nestSupplier.get() : null;
        double tunnelW = tunnelWidthSupplier != null ? tunnelWidthSupplier.getAsDouble() : 1.0;
        if (canvasTop != null && gcTop != null && canvasTop.getWidth() >= 10 && canvasTop.getHeight() >= 10 && nest != null) {
            NestRenderer.drawTop(nest, gcTop, canvasTop.getWidth(), canvasTop.getHeight(), tunnelW, topZoom, topPanX, topPanY);
        }
    }
}
