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
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Visualizes pheromone chemical trails on the terrain surface using a dynamic texture overlay.
 * Uses exact 1:1 spatial mapping aligned with ant positions and world coordinates.
 */
public class PheromoneVisualizer {

    private final Node rootNode;
    private final AssetManager assetManager;
    private Geometry overlayGeom;
    private Texture2D texture;
    private ByteBuffer imageBuffer;
    private int width, height;
    private float groundElevation = 0.0f;
    private boolean initialized = false;

    public PheromoneVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("Pheromones");
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void initialize(int width, int height) {
        initialize(width, height, this.groundElevation);
    }

    public void initialize(int width, int height, float groundY) {
        this.width = width;
        this.height = height;
        this.groundElevation = groundY;

        rootNode.detachAllChildren();

        // Create texture buffer (RGBA8)
        this.imageBuffer = BufferUtils.createByteBuffer(width * height * 4);
        Image img = new Image(Image.Format.RGBA8, width, height, imageBuffer, ColorSpace.Linear);
        this.texture = new Texture2D(img);
        this.texture.setMinFilter(com.jme3.texture.Texture.MinFilter.BilinearNearestMipMap);
        this.texture.setMagFilter(com.jme3.texture.Texture.MagFilter.Bilinear);

        // Build horizontal flat mesh spanning X in [0, width] and Z in [0, height]
        // Facing strictly UP (+Y) so normal is (0, 1, 0)
        Mesh planeMesh = new Mesh();
        float[] positions = new float[] {
            0f,     0f, 0f,         // v0 (bottom-left)
            width,  0f, 0f,         // v1 (bottom-right)
            width,  0f, height,     // v2 (top-right)
            0f,     0f, height      // v3 (top-left)
        };
        float[] normals = new float[] {
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f
        };
        float[] texCoords = new float[] {
            0f, 0f, // (0, 0)
            1f, 0f, // (1, 0)
            1f, 1f, // (1, 1)
            0f, 1f  // (0, 1)
        };
        int[] indices = new int[] {
            0, 2, 1,
            0, 3, 2
        };

        planeMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(positions));
        planeMesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        planeMesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        planeMesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indices));
        planeMesh.updateBound();

        this.overlayGeom = new Geometry("PheromoneOverlayMesh", planeMesh);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthWrite(false); // Transparent overlay
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off); // Visible from all overhead camera angles

        this.overlayGeom.setMaterial(mat);
        // Position slightly above terrain surface to eliminate z-fighting
        this.overlayGeom.setLocalTranslation(0, groundY + 0.08f, 0);

        rootNode.attachChild(overlayGeom);
        initialized = true;
    }

    public void setGroundElevation(float groundY) {
        this.groundElevation = groundY;
        if (overlayGeom != null) {
            overlayGeom.setLocalTranslation(0, groundY + 0.08f, 0);
        }
    }

    public void update(SparsePheromoneGrid grid) {
        if (!initialized || grid == null)
            return;

        // Clear buffer
        clearBuffer();

        // Iterate active chemical grid entries
        Map<Long, float[]> entries = grid.getAllEntries();
        for (Map.Entry<Long, float[]> entry : entries.entrySet()) {
            long key = entry.getKey();
            float[] pheromones = entry.getValue();

            int[] coords = org.swarmforge.core.spatial.Morton3D.decode(key);
            int x = coords[0]; // Domain X -> JME X
            int y = coords[1]; // Domain Y -> JME Z
            int z = coords[2]; // Domain Z -> Vertical altitude

            // Map across full terrarium footprint (0..width-1, 0..height-1)
            if (x >= 0 && x < width && y >= 0 && y < height) {
                float homing = pheromones.length > 0 ? pheromones[0] : 0f;
                float food = pheromones.length > 1 ? pheromones[1] : 0f;
                float danger = pheromones.length > 2 ? pheromones[2] : 0f;

                // Vivid, high-contrast chemical coloration:
                // Food = Glowing Emerald Green / Yellow
                // Homing = Deep Electric Blue / Cyan
                // Danger = Vivid Crimson Red
                float r = Math.min(1.0f, danger * 1.8f + food * 0.4f);
                float g = Math.min(1.0f, food * 1.5f + homing * 0.2f);
                float b = Math.min(1.0f, homing * 1.6f + danger * 0.1f);
                float a = Math.min(0.95f, (food * 1.4f + homing * 1.2f + danger * 1.8f));

                if (a > 0.02f) {
                    setPixel(x, y, r, g, b, a);
                }
            }
        }

        // Upload texture update to GPU
        texture.getImage().setData(imageBuffer);
    }

    private void clearBuffer() {
        for (int i = 0; i < imageBuffer.capacity(); i++) {
            imageBuffer.put(i, (byte) 0);
        }
        imageBuffer.clear();
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

        // Maximum blending to combine contributions across layers
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
