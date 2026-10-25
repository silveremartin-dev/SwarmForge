/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import javafx.scene.canvas.GraphicsContext;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.core.domain.Colony;

/**
 * 3D Subterranean Galleries & Epigeic Nest Architecture Composite Renderer for the World Editor Studio.
 * Acts as a unified facade delegating to {@link WorldSubterraneanGallery3DRenderer} and
 * {@link WorldEpigeicNestStructure3DRenderer}.
 *
 * @author SilvÃ¨re Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WorldNestArchitecture3DRenderer {

    private final WorldEpigeicNestStructure3DRenderer epigeicRenderer;
    private final WorldSubterraneanGallery3DRenderer subterraneanRenderer;

    public WorldNestArchitecture3DRenderer() {
        this.epigeicRenderer = new WorldEpigeicNestStructure3DRenderer();
        this.subterraneanRenderer = new WorldSubterraneanGallery3DRenderer(this.epigeicRenderer);
    }

    public void drawGalleriesOverlay3D(WorldEditorPane pane, GraphicsContext gc3D,
                                       double cx, double cy, double scale, double radAz, double radEl) {
        subterraneanRenderer.drawGalleriesOverlay3D(pane, gc3D, cx, cy, scale, radAz, radEl);
    }

    public void drawGalleriesLegendHUD(WorldEditorPane pane, GraphicsContext gc3D, double w, double h) {
        subterraneanRenderer.drawGalleriesLegendHUD(pane, gc3D, w, h);
    }

    public void drawRealistic3DNestStructure(WorldEditorPane pane, GraphicsContext gc3D,
                                            double cx, double cy, double scale, double radAz, double radEl) {
        epigeicRenderer.drawRealistic3DNestStructure(pane, gc3D, cx, cy, scale, radAz, radEl);
    }

    public void drawSingleRealistic3DNestStructure(WorldEditorPane pane, GraphicsContext gc3D,
                                                  double cx, double cy, double scale, double radAz, double radEl,
                                                  int nestX, int nestY, double maturityScale, Colony colony) {
        epigeicRenderer.drawSingleRealistic3DNestStructure(pane, gc3D, cx, cy, scale, radAz, radEl, nestX, nestY, maturityScale, colony);
    }
}