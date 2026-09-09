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
        this.texture.setMinFilter(com.jme3.texture.Texture.MinFilter.BilinearNearestMipMap);
        this.texture.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);

        // Create overlay geometry flat on XZ ground plane
        Quad quad = new Quad(width, depth);
        this.overlayGeom = new Geometry("PheromoneOverlay", quad);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false); // Don't write depth, transparent

        this.overlayGeom.setMaterial(mat);
        // Rotate -90 deg on X so (X, Y) quad becomes (X, Z) ground plane with normal pointing UP (0, 1, 0)
        this.overlayGeom.rotate(-1.5707963f, 0, 0);
        this.overlayGeom.setLocalTranslation(0, 0.15f, 0); // Offset slightly above terrain (y=0.15)

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
            int y = coords[1];
            int z = coords[2];

            // Project 3D (X, Y, Z) to 2D texture map (Top-down ground view)
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
                    setPixel(x, y, r, g, b, a); // Y maps to texture ground depth
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
        int index = (y * width + x) * 4;
        if (index < 0 || index >= imageBuffer.capacity() - 4)
            return;

        int existingR = imageBuffer.get(index) & 0xFF;
        int existingG = imageBuffer.get(index + 1) & 0xFF;
        int existingB = imageBuffer.get(index + 2) & 0xFF;
        int existingA = imageBuffer.get(index + 3) & 0xFF;

        int targetR = (int) (Math.min(1.0f, r) * 255);
        int targetG = (int) (Math.min(1.0f, g) * 255);
        int targetB = (int) (Math.min(1.0f, b) * 255);
        int targetA = (int) (Math.min(1.0f, a) * 255);

        // Composite / Max blend to combine contributions across Z-layers without overwriting
        byte newR = (byte) Math.max(existingR, targetR);
        byte newG = (byte) Math.max(existingG, targetG);
        byte newB = (byte) Math.max(existingB, targetB);
        byte newA = (byte) Math.max(existingA, targetA);

        imageBuffer.put(index, newR);
        imageBuffer.put(index + 1, newG);
        imageBuffer.put(index + 2, newB);
        imageBuffer.put(index + 3, newA);
    }
}
