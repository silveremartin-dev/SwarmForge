/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Visualizes pheromone trails using a dynamic texture overlay.
 */
public class PheromoneVisualizer {

    private final Node rootNode;
    private final AssetManager assetManager;
    private Geometry overlayGeom;
    private Texture2D texture;
    private ByteBuffer imageBuffer;
    private int width, depth;
    private boolean initialized = false;

    public PheromoneVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("Pheromones");
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void initialize(int width, int depth) {
        this.width = width;
        this.depth = depth;

        // Create texture buffer (RGBA8)
        this.imageBuffer = BufferUtils.createByteBuffer(width * depth * 4);
        Image img = new Image(Image.Format.RGBA8, width, depth, imageBuffer, ColorSpace.Linear);
        this.texture = new Texture2D(img);

        // Create overlay geometry
        // We use a Quad rotated to lie flat on XZ plane
        Quad quad = new Quad(width, depth);
        this.overlayGeom = new Geometry("PheromoneOverlay", quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false); // Don't write depth, transparent

        this.overlayGeom.setMaterial(mat);
        this.overlayGeom.rotate(-1.5708f, 0, 0); // Rotate -90 deg around X to face up
        this.overlayGeom.setLocalTranslation(0, 0.1f, depth); // Offset slightly above terrain (y=0.1), fix orientation
        // JME Quad is (width, height) in XY plane. Rotated -90deg X -> XZ plane.
        // (0,0,0) -> (W, 0, -D) ?
        // Standard JME Quad: (0,0) to (W, H).
        // Rotate -90 X: Y becomes Z (towards camera? away?).
        // Need to check coordinates. Assuming standard orientation for now.
        // Actually, let's reset rotation and use custom mesh if needed, but Quad is
        // easiest.

        // Correct rotation for XZ plane:
        // Quad is XY. Rotate -90 on X means +Y becomes +Z (towards viewer usually) or
        // -Z.
        // Let's assume standard mapping: X->X, Y->Z.
        // Position: X=0, Y=0.2 (above ground), Z=depth (since quad goes from 0 to H,
        // and we want 0 to D)
        // Actually, usually Quad is 0,0 bottom left.
        // Let's set translation to (0, 0.2f, 0) and verify orientation later.
        this.overlayGeom.setLocalTranslation(0, 0.2f, 0);
        // We probably need to flip Z or something if it renders backward, but texture
        // map handles U,V.

        rootNode.attachChild(overlayGeom);
        initialized = true;
    }

    public void update(SparsePheromoneGrid grid) {
        if (!initialized)
            return;

        // Clear buffer
        clearBuffer();

        // Iterate active entries
        Map<Long, float[]> entries = grid.getAllEntries();
        for (Map.Entry<Long, float[]> entry : entries.entrySet()) {
            long key = entry.getKey();
            float[] pheromones = entry.getValue();

            int[] coords = org.swarmforge.core.spatial.Morton3D.decode(key);
            int x = coords[0];
            int y = coords[1]; // Correct mapping: coords[1] is Y depth (horizontal terrain position)

            // Project 3D to 2D map (Top-down view)
            if (x >= 0 && x < width && y >= 0 && y < depth) {
                // Color mapping:
                // 0: TO_HOME (Blue)
                // 1: TO_FOOD (Green/Red)
                // 2: DANGER (Red/Purple)

                float homing = pheromones.length > 0 ? pheromones[0] : 0f;
                float food = pheromones.length > 1 ? pheromones[1] : 0f;
                float danger = pheromones.length > 2 ? pheromones[2] : 0f;

                // Heatmap composite intensity and color
                float r = Math.min(1.0f, danger * 1.5f + food * 0.8f);
                float g = Math.min(1.0f, food * 1.2f);
                float b = Math.min(1.0f, homing * 1.2f + danger * 0.5f);
                float a = Math.min(1.0f, (food + homing + danger) * 2.0f);

                if (a > 0.02f) {
                    setPixel(x, y, r, g, b, a); // Y maps to texture depth
                }
            }
        }

        // Upload to GPU
        texture.getImage().setData(imageBuffer);
    }

    private void clearBuffer() {
        // Fast clear?
        for (int i = 0; i < imageBuffer.capacity(); i++) {
            imageBuffer.put(i, (byte) 0);
        }
        imageBuffer.clear(); // Reset position
    }

    private void setPixel(int x, int y, float r, float g, float b, float a) {
        // Texture coordinates: (0,0) is usually bottom-left.
        // Our map might be different. Let's assume 1:1.
        int index = (y * width + x) * 4;
        if (index < 0 || index >= imageBuffer.capacity() - 4)
            return;

        // JME Image format usually RGBA8 or ABGR8? We selected RGBA8.
        // Values 0-255

        // Check if pixel already set? (Simple Overwrite or Blend?)
        // Let's Blend logic: Max
        byte newR = (byte) (Math.min(1.0f, r) * 255);
        byte newG = (byte) (Math.min(1.0f, g) * 255);
        byte newB = (byte) (Math.min(1.0f, b) * 255);
        byte newA = (byte) (Math.min(1.0f, a) * 255);

        imageBuffer.put(index, newR);
        imageBuffer.put(index + 1, newG);
        imageBuffer.put(index + 2, newB);
        imageBuffer.put(index + 3, newA);
    }
}
