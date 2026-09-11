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
        int height = terrarium.getHeight(); // Y-axis in Terrarium (horizontal North/South -> JME Z)
        int depth = terrarium.getDepth();   // Z-axis in Terrarium (vertical altitude -> JME Y)

        int indexOffset = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    if (cell.material() == TerrariumCell.Material.AIR)
                        continue;

                    // JME Coords: X=x, Y=z (Up, altitude), Z=y (North/South depth)
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
                y < 0 || y >= terrarium.getHeight() ||
                z < 0 || z >= terrarium.getDepth()) {
            return true; // Boundary faces visible
        }
        return terrarium.getCell(x, y, z).material() == TerrariumCell.Material.AIR;
    }

    // --- Face Generation Helpers with Continuous Seamless World UVs (No Tile Seams) ---
    private static final float UV_SCALE = 0.25f; // 4 meters per full texture repeat for realistic natural look

    private void addUpFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        float x0 = x - 0.5f, x1 = x + 0.5f;
        float y0 = y + 0.5f;
        float z0 = z - 0.5f, z1 = z + 0.5f;

        v.add(x0); v.add(y0); v.add(z0);
        v.add(x0); v.add(y0); v.add(z1);
        v.add(x1); v.add(y0); v.add(z1);
        v.add(x1); v.add(y0); v.add(z0);

        for (int i = 0; i < 4; i++) {
            n.add(0f); n.add(1f); n.add(0f);
        }

        // Continuous World-Space UVs on Horizontal Plane (eliminates individual voxel tile borders)
        t.add(x0 * UV_SCALE); t.add(z0 * UV_SCALE);
        t.add(x0 * UV_SCALE); t.add(z1 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(z1 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(z0 * UV_SCALE);
    }

    private void addDownFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        float x0 = x - 0.5f, x1 = x + 0.5f;
        float y0 = y - 0.5f;
        float z0 = z - 0.5f, z1 = z + 0.5f;

        v.add(x0); v.add(y0); v.add(z0);
        v.add(x1); v.add(y0); v.add(z0);
        v.add(x1); v.add(y0); v.add(z1);
        v.add(x0); v.add(y0); v.add(z1);

        for (int i = 0; i < 4; i++) {
            n.add(0f); n.add(-1f); n.add(0f);
        }

        t.add(x0 * UV_SCALE); t.add(z0 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(z0 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(z1 * UV_SCALE);
        t.add(x0 * UV_SCALE); t.add(z1 * UV_SCALE);
    }

    private void addFrontFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME +Z
        float x0 = x - 0.5f, x1 = x + 0.5f;
        float y0 = y - 0.5f, y1 = y + 0.5f;
        float z0 = z + 0.5f;

        v.add(x0); v.add(y1); v.add(z0);
        v.add(x0); v.add(y0); v.add(z0);
        v.add(x1); v.add(y0); v.add(z0);
        v.add(x1); v.add(y1); v.add(z0);

        for (int i = 0; i < 4; i++) {
            n.add(0f); n.add(0f); n.add(1f);
        }

        t.add(x0 * UV_SCALE); t.add(y1 * UV_SCALE);
        t.add(x0 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(y1 * UV_SCALE);
    }

    private void addBackFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME -Z
        float x0 = x - 0.5f, x1 = x + 0.5f;
        float y0 = y - 0.5f, y1 = y + 0.5f;
        float z0 = z - 0.5f;

        v.add(x1); v.add(y1); v.add(z0);
        v.add(x1); v.add(y0); v.add(z0);
        v.add(x0); v.add(y0); v.add(z0);
        v.add(x0); v.add(y1); v.add(z0);

        for (int i = 0; i < 4; i++) {
            n.add(0f); n.add(0f); n.add(-1f);
        }

        t.add(x1 * UV_SCALE); t.add(y1 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(x0 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(x0 * UV_SCALE); t.add(y1 * UV_SCALE);
    }

    private void addRightFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME +X
        float x0 = x + 0.5f;
        float y0 = y - 0.5f, y1 = y + 0.5f;
        float z0 = z - 0.5f, z1 = z + 0.5f;

        v.add(x0); v.add(y1); v.add(z1);
        v.add(x0); v.add(y0); v.add(z1);
        v.add(x0); v.add(y0); v.add(z0);
        v.add(x0); v.add(y1); v.add(z0);

        for (int i = 0; i < 4; i++) {
            n.add(1f); n.add(0f); n.add(0f);
        }

        t.add(z1 * UV_SCALE); t.add(y1 * UV_SCALE);
        t.add(z1 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(z0 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(z0 * UV_SCALE); t.add(y1 * UV_SCALE);
    }

    private void addLeftFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t) {
        // JME -X
        float x0 = x - 0.5f;
        float y0 = y - 0.5f, y1 = y + 0.5f;
        float z0 = z - 0.5f, z1 = z + 0.5f;

        v.add(x0); v.add(y1); v.add(z0);
        v.add(x0); v.add(y0); v.add(z0);
        v.add(x0); v.add(y0); v.add(z1);
        v.add(x0); v.add(y1); v.add(z1);

        for (int i = 0; i < 4; i++) {
            n.add(-1f); n.add(0f); n.add(0f);
        }

        t.add(z0 * UV_SCALE); t.add(y1 * UV_SCALE);
        t.add(z0 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(z1 * UV_SCALE); t.add(y0 * UV_SCALE);
        t.add(z1 * UV_SCALE); t.add(y1 * UV_SCALE);
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
