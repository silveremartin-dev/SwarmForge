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
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Visualizes pheromone chemical trails on the terrain surface.
 * Supports 3 distinct rendering modes:
 * - SCIENTIFIC: Discrete voxel-accurate chemical concentration heatmap (crisp cell borders, quantitative precision)
 * - REALISTIC: Smooth organic chemical vapor diffusion (continuous vapor trail corridors, natural evaporation falloff)
 * - GAMIFIED: Glowing continuous particle-flow ribbons (luminous neon trails with radiant energy halo along ant paths)
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
    private RenderMode currentRenderMode = RenderMode.REALISTIC;

    public PheromoneVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("Pheromones");
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRenderMode(RenderMode mode) {
        if (mode == null) mode = RenderMode.REALISTIC;
        this.currentRenderMode = mode;
        applyTextureFiltering();
    }

    private void applyTextureFiltering() {
        if (texture == null) return;
        if (currentRenderMode == RenderMode.SCIENTIFIC) {
            // Discrete voxel precision
            texture.setMinFilter(MinFilter.NearestNoMipMaps);
            texture.setMagFilter(MagFilter.Nearest);
        } else {
            // Smooth continuous organic or glowing particle trail
            texture.setMinFilter(MinFilter.BilinearNearestMipMap);
            texture.setMagFilter(MagFilter.Bilinear);
        }
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
        applyTextureFiltering();

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

            if (x >= 0 && x < width && y >= 0 && y < height) {
                float homing = pheromones.length > 0 ? pheromones[0] : 0f;
                float food = pheromones.length > 1 ? pheromones[1] : 0f;
                float danger = pheromones.length > 2 ? pheromones[2] : 0f;

                if (currentRenderMode == RenderMode.SCIENTIFIC) {
                    // Scientific mode: discrete quantitative heatmap
                    float r = Math.min(1.0f, danger * 1.6f + food * 0.3f);
                    float g = Math.min(1.0f, food * 1.5f + homing * 0.2f);
                    float b = Math.min(1.0f, homing * 1.6f + danger * 0.1f);
                    float a = Math.min(0.95f, food * 1.3f + homing * 1.2f + danger * 1.8f);

                    if (a > 0.02f) {
                        setPixel(x, y, r, g, b, a);
                    }
                } else if (currentRenderMode == RenderMode.REALISTIC) {
                    // Realistic mode: continuous organic chemical vapor trail diffusion
                    // Smooth 3x3 kernel creates uninterrupted chemical corridors between voxels
                    float r = Math.min(1.0f, danger * 1.5f + food * 0.2f);
                    float g = Math.min(1.0f, food * 1.4f + homing * 0.15f);
                    float b = Math.min(1.0f, homing * 1.5f + danger * 0.05f);
                    float intensity = Math.min(0.85f, food * 1.2f + homing * 1.1f + danger * 1.6f);

                    if (intensity > 0.02f) {
                        // Core pixel
                        setPixel(x, y, r, g, b, intensity * 0.90f);
                        // Continuous organic vapor dispersion onto 4-neighbors (merging adjacent dots into trails)
                        spreadPixel(x + 1, y, r, g, b, intensity * 0.42f);
                        spreadPixel(x - 1, y, r, g, b, intensity * 0.42f);
                        spreadPixel(x, y + 1, r, g, b, intensity * 0.42f);
                        spreadPixel(x, y - 1, r, g, b, intensity * 0.42f);
                        // Diagonal soft falloff
                        spreadPixel(x + 1, y + 1, r, g, b, intensity * 0.20f);
                        spreadPixel(x - 1, y + 1, r, g, b, intensity * 0.20f);
                        spreadPixel(x + 1, y - 1, r, g, b, intensity * 0.20f);
                        spreadPixel(x - 1, y - 1, r, g, b, intensity * 0.20f);
                    }
                } else {
                    // Gamified mode: luminous neon glowing particle trail ribbons
                    float r = Math.min(1.0f, danger * 2.0f + food * 0.4f);
                    float g = Math.min(1.0f, food * 1.8f + homing * 0.3f);
                    float b = Math.min(1.0f, homing * 1.9f + danger * 0.2f);
                    float intensity = Math.min(0.95f, food * 1.5f + homing * 1.4f + danger * 2.0f);

                    if (intensity > 0.02f) {
                        // Radiant intense core
                        setPixel(x, y, r * 1.2f, g * 1.2f, b * 1.2f, intensity * 0.95f);
                        // Continuous luminous energy envelope
                        spreadPixel(x + 1, y, r, g, b, intensity * 0.55f);
                        spreadPixel(x - 1, y, r, g, b, intensity * 0.55f);
                        spreadPixel(x, y + 1, r, g, b, intensity * 0.55f);
                        spreadPixel(x, y - 1, r, g, b, intensity * 0.55f);
                        spreadPixel(x + 1, y + 1, r * 0.9f, g * 0.9f, b * 0.9f, intensity * 0.30f);
                        spreadPixel(x - 1, y + 1, r * 0.9f, g * 0.9f, b * 0.9f, intensity * 0.30f);
                        spreadPixel(x + 1, y - 1, r * 0.9f, g * 0.9f, b * 0.9f, intensity * 0.30f);
                        spreadPixel(x - 1, y - 1, r * 0.9f, g * 0.9f, b * 0.9f, intensity * 0.30f);
                        // Outer radiant halo
                        spreadPixel(x + 2, y, r * 0.7f, g * 0.7f, b * 0.7f, intensity * 0.18f);
                        spreadPixel(x - 2, y, r * 0.7f, g * 0.7f, b * 0.7f, intensity * 0.18f);
                        spreadPixel(x, y + 2, r * 0.7f, g * 0.7f, b * 0.7f, intensity * 0.18f);
                        spreadPixel(x, y - 2, r * 0.7f, g * 0.7f, b * 0.7f, intensity * 0.18f);
                    }
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

    private void spreadPixel(int x, int y, float r, float g, float b, float a) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        setPixel(x, y, r, g, b, a);
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

        // Additive & maximum blending for continuous flowing trail superposition
        byte newR = (byte) Math.min(255, Math.max(existingR, targetR));
        byte newG = (byte) Math.min(255, Math.max(existingG, targetG));
        byte newB = (byte) Math.min(255, Math.max(existingB, targetB));
        byte newA = (byte) Math.min(255, Math.max(existingA, targetA));

        imageBuffer.put(index, newR);
        imageBuffer.put(index + 1, newG);
        imageBuffer.put(index + 2, newB);
        imageBuffer.put(index + 3, newA);
    }
}
