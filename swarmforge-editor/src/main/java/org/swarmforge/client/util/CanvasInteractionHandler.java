/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;

/**
 * Shared utility handler to normalize and standardize Google Maps-style pointer-anchored
 * zoom (zoom-in and de-zoom) and panning interaction logic across SwarmForge editor canvases.
 */
public class CanvasInteractionHandler {

    @FunctionalInterface
    public interface InteractionCallback {
        void onInteraction();
    }

    @FunctionalInterface
    public interface ZoomChangeHandler {
        void onZoomChanged(double oldZoom, double newZoom, double scaleRatio, double mouseX, double mouseY);
    }

    @FunctionalInterface
    public interface PanChangeHandler {
        void onPanChanged(double dx, double dy);
    }

    private double zoom = 1.0;
    private double panX = 0.0;
    private double panY = 0.0;

    private double minZoom = 0.1;
    private double maxZoom = 100.0;
    private double zoomSensitivity = 0.005;

    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private boolean isDragging = false;
    private MouseButton dragButton = MouseButton.PRIMARY;

    private InteractionCallback repaintCallback;
    private ZoomChangeHandler zoomChangeHandler;
    private PanChangeHandler panChangeHandler;

    public CanvasInteractionHandler() {}

    public CanvasInteractionHandler(double initialZoom, double minZoom, double maxZoom) {
        this.zoom = initialZoom;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }

    /**
     * Calculates updated pan coordinates anchored to mouse pointer position during zoom scaling.
     * Standard Google Maps-style formula preserving coordinate alignment under cursor.
     *
     * @param currentPan Current pan coordinate (X or Y)
     * @param mousePos Mouse cursor position on canvas axis (X or Y)
     * @param viewCenter Canvas center dimension (width / 2.0 or height / 2.0)
     * @param scaleRatio Ratio of newZoom / oldZoom
     * @return Updated pan coordinate anchored to mouse location
     */
    public static double calculatePointerAnchoredPan(double currentPan, double mousePos, double viewCenter, double scaleRatio) {
        if (scaleRatio <= 0 || Double.isNaN(scaleRatio)) return currentPan;
        return (mousePos - viewCenter) * (1.0 - scaleRatio) + currentPan * scaleRatio;
    }

    /**
     * Attach mouse drag-pan and scroll-zoom handlers to a JavaFX target node.
     *
     * @param targetNode Target Node (e.g., Canvas or Pane)
     */
    public void attach(Node targetNode) {
        if (targetNode == null) return;

        targetNode.setOnMousePressed(e -> {
            if (dragButton == null || e.getButton() == dragButton) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                isDragging = true;
            }
        });

        targetNode.setOnMouseDragged(e -> {
            if (isDragging) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;
                panX += dx;
                panY += dy;
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                if (panChangeHandler != null) {
                    panChangeHandler.onPanChanged(dx, dy);
                }
                if (repaintCallback != null) {
                    repaintCallback.onInteraction();
                }
            }
        });

        targetNode.setOnMouseReleased(e -> {
            if (dragButton == null || e.getButton() == dragButton) {
                isDragging = false;
            }
        });

        targetNode.setOnScroll(e -> handleScroll(e, targetNode.getBoundsInLocal().getWidth(), targetNode.getBoundsInLocal().getHeight()));
    }

    /**
     * Processes scroll event with pointer anchoring (zoom in and de-zoom towards pointer).
     *
     * @param e JavaFX ScrollEvent
     * @param canvasWidth Target viewport width
     * @param canvasHeight Target viewport height
     */
    public void handleScroll(ScrollEvent e, double canvasWidth, double canvasHeight) {
        if (e == null) return;

        double deltaY = e.getDeltaY();
        if (deltaY == 0) return;

        double oldZoom = this.zoom;
        double newZoom = Math.max(minZoom, Math.min(maxZoom, oldZoom + deltaY * zoomSensitivity));

        if (newZoom == oldZoom) return;

        double scaleRatio = newZoom / oldZoom;
        double mx = e.getX();
        double my = e.getY();
        double cx = canvasWidth / 2.0;
        double cy = canvasHeight / 2.0;

        this.panX = calculatePointerAnchoredPan(this.panX, mx, cx, scaleRatio);
        this.panY = calculatePointerAnchoredPan(this.panY, my, cy, scaleRatio);
        this.zoom = newZoom;

        if (zoomChangeHandler != null) {
            zoomChangeHandler.onZoomChanged(oldZoom, newZoom, scaleRatio, mx, my);
        }
        if (repaintCallback != null) {
            repaintCallback.onInteraction();
        }
    }

    public double getZoom() { return zoom; }
    public void setZoom(double zoom) { this.zoom = Math.max(minZoom, Math.min(maxZoom, zoom)); }

    public double getPanX() { return panX; }
    public void setPanX(double panX) { this.panX = panX; }

    public double getPanY() { return panY; }
    public void setPanY(double panY) { this.panY = panY; }

    public void setPan(double panX, double panY) {
        this.panX = panX;
        this.panY = panY;
    }

    public double getMinZoom() { return minZoom; }
    public void setMinZoom(double minZoom) { this.minZoom = minZoom; }

    public double getMaxZoom() { return maxZoom; }
    public void setMaxZoom(double maxZoom) { this.maxZoom = maxZoom; }

    public double getZoomSensitivity() { return zoomSensitivity; }
    public void setZoomSensitivity(double zoomSensitivity) { this.zoomSensitivity = zoomSensitivity; }

    public MouseButton getDragButton() { return dragButton; }
    public void setDragButton(MouseButton dragButton) { this.dragButton = dragButton; }

    public InteractionCallback getRepaintCallback() { return repaintCallback; }
    public void setRepaintCallback(InteractionCallback repaintCallback) { this.repaintCallback = repaintCallback; }

    public ZoomChangeHandler getZoomChangeHandler() { return zoomChangeHandler; }
    public void setZoomChangeHandler(ZoomChangeHandler zoomChangeHandler) { this.zoomChangeHandler = zoomChangeHandler; }

    public PanChangeHandler getPanChangeHandler() { return panChangeHandler; }
    public void setPanChangeHandler(PanChangeHandler panChangeHandler) { this.panChangeHandler = panChangeHandler; }

    public void reset() {
        this.panX = 0.0;
        this.panY = 0.0;
        this.zoom = 1.0;
    }
}
