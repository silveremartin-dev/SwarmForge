package org.swarmforge.client.view;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.ArrayList;
import java.util.List;

/**
 * optimized mesh generator for voxel terrain.
 * Uses exposed face culling to reduce geometry.
 */
public class TerrainMeshGenerator {

    public Mesh generateMesh(Terrarium terrarium) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight(); // Z-axis in domain, Y-axis in JME usually but mapped to Z here
        int depth = terrarium.getDepth(); // Y-axis in domain? Check JmeGameApp mapping

        // JmeGameApp mapping: x=x, y=z (height in domain), z=y (depth in domain) based
        // on:
        // g.setLocalTranslation(x, z, y); // Y is up
        // Wait, Terrarium has x,y,z.
        // JME: Y is UP.
        // Terrarium likely: X, Y (North/South), Z (Height/Altitude).
        // JmeGameApp loop: for x, for y (depth), for z (height).
        // g.setLocalTranslation(x, z, y) -> X=x, Y=z, Z=y.
        // So Terrarium Z maps to JME Y. Terrarium Y maps to JME Z.

        int indexOffset = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < depth; y++) {
                for (int z = 0; z < height; z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    if (cell.material() == TerrariumCell.Material.AIR)
                        continue;

                    // Check 6 faces
                    // JME Coords: X, Y(Up, from z), Z(Depth, from y)
                    float jmeX = x;
                    float jmeY = z;
                    float jmeZ = y;

                    // Top Face (JME +Y, Domain +Z)
                    if (isTransparent(terrarium, x, y, z + 1)) {
                        addUpFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // Bottom Face (JME -Y, Domain -Z)
                    if (isTransparent(terrarium, x, y, z - 1)) {
                        addDownFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // Front Face (JME +Z, Domain +Y)
                    if (isTransparent(terrarium, x, y + 1, z)) {
                        addFrontFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // Back Face (JME -Z, Domain -Y)
                    if (isTransparent(terrarium, x, y - 1, z)) {
                        addBackFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // Right Face (JME +X, Domain +X)
                    if (isTransparent(terrarium, x + 1, y, z)) {
                        addRightFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // Left Face (JME -X, Domain -X)
                    if (isTransparent(terrarium, x - 1, y, z)) {
                        addLeftFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }
                }
            }
        }

        Mesh mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(toFloatArray(vertices)));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(toFloatArray(normals)));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(toFloatArray(texCoords)));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(toIntArray(indices)));
        mesh.updateBound();

        return mesh;
    }

    private boolean isTransparent(Terrarium terrarium, int x, int y, int z) {
        if (x < 0 || x >= terrarium.getWidth() ||
                y < 0 || y >= terrarium.getDepth() ||
                z < 0 || z >= terrarium.getHeight()) {
            return true; // Boundary faces visible
        }
        return terrarium.getCell(x, y, z).material() == TerrariumCell.Material.AIR;
    }

    // --- Face Generation Helpers ---
    // Assuming voxel size 1.0, positioned at min corner or center?
    // JmeGameApp used Box(0.5, 0.5, 0.5) which has extends 0.5 (size 1.0) generally
    // centered.
    // Let's use offsets +0.5 to keep logic consistent with "center at integer+.5"
    // or similar.
    // Box constructor (0.5, 0.5, 0.5) creates a box of size 1x1x1.
    // setLocalTranslation(x, z, y) places center of box.
    // So vertices should be relative to center (x, z, y) with +/- 0.5.

    private void addUpFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        v.add(x - 0.5f);
        v.add(y + 0.5f);
        v.add(z + 0.5f);
        v.add(x + 0.5f);
        v.add(y + 0.5f);
        v.add(z + 0.5f);
        v.add(x + 0.5f);
        v.add(y + 0.5f);
        v.add(z - 0.5f);
        v.add(x - 0.5f);
        v.add(y + 0.5f);
        v.add(z - 0.5f);

        for (int i = 0; i < 4; i++) {
            n.add(0f);
            n.add(1f);
            n.add(0f);
        }
        t.add(0f);
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
    }

    private void addDownFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        v.add(x - 0.5f);
        v.add(y - 0.5f);
        v.add(z - 0.5f);
        v.add(x + 0.5f);
        v.add(y - 0.5f);
        v.add(z - 0.5f);
        v.add(x + 0.5f);
        v.add(y - 0.5f);
        v.add(z + 0.5f);
        v.add(x - 0.5f);
        v.add(y - 0.5f);
        v.add(z + 0.5f);

        for (int i = 0; i < 4; i++) {
            n.add(0f);
            n.add(-1f);
            n.add(0f);
        }
        t.add(0f);
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
    }

    private void addFrontFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME +Z
        v.add(x - 0.5f);
        v.add(y + 0.5f);
        v.add(z + 0.5f);
        v.add(x - 0.5f);
        v.add(y - 0.5f);
        v.add(z + 0.5f);
        v.add(x + 0.5f);
        v.add(y - 0.5f);
        v.add(z + 0.5f);
        v.add(x + 0.5f);
        v.add(y + 0.5f);
        v.add(z + 0.5f);

        for (int i = 0; i < 4; i++) {
            n.add(0f);
            n.add(0f);
            n.add(1f);
        }
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
        t.add(1f);
    }

    private void addBackFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME -Z
        v.add(x + 0.5f);
        v.add(y + 0.5f);
        v.add(z - 0.5f);
        v.add(x + 0.5f);
        v.add(y - 0.5f);
        v.add(z - 0.5f);
        v.add(x - 0.5f);
        v.add(y - 0.5f);
        v.add(z - 0.5f);
        v.add(x - 0.5f);
        v.add(y + 0.5f);
        v.add(z - 0.5f);

        for (int i = 0; i < 4; i++) {
            n.add(0f);
            n.add(0f);
            n.add(-1f);
        }
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
        t.add(1f);
    }

    private void addRightFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME +X
        v.add(x + 0.5f);
        v.add(y + 0.5f);
        v.add(z + 0.5f);
        v.add(x + 0.5f);
        v.add(y - 0.5f);
        v.add(z + 0.5f);
        v.add(x + 0.5f);
        v.add(y - 0.5f);
        v.add(z - 0.5f);
        v.add(x + 0.5f);
        v.add(y + 0.5f);
        v.add(z - 0.5f);

        for (int i = 0; i < 4; i++) {
            n.add(1f);
            n.add(0f);
            n.add(0f);
        }
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
        t.add(1f);
    }

    private void addLeftFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME -X
        v.add(x - 0.5f);
        v.add(y + 0.5f);
        v.add(z - 0.5f);
        v.add(x - 0.5f);
        v.add(y - 0.5f);
        v.add(z - 0.5f);
        v.add(x - 0.5f);
        v.add(y - 0.5f);
        v.add(z + 0.5f);
        v.add(x - 0.5f);
        v.add(y + 0.5f);
        v.add(z + 0.5f);

        for (int i = 0; i < 4; i++) {
            n.add(-1f);
            n.add(0f);
            n.add(0f);
        }
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(0f);
        t.add(1f);
        t.add(0f);
        t.add(1f);
        t.add(1f);
    }

    private void addIndices(List<Integer> indices, int offset) {
        // Quad 0,1,2, 2,3,0
        indices.add(offset + 0);
        indices.add(offset + 1);
        indices.add(offset + 2);
        indices.add(offset + 2);
        indices.add(offset + 3);
        indices.add(offset + 0);
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }

    private int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }
}
