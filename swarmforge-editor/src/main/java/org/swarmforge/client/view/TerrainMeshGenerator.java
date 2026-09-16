package org.swarmforge.client.view;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimized mesh generator for voxel terrain and natural landscapes in JME.
 * Supports:
 * - Exposed face culling
 * - Continuous seamless world UV coordinates
 * - 3D Subterranean Skirt ("Jupe 3D") along outer boundaries
 * - Live Cross-Section Slice Plane ("Vue en coupe") at sliceRatio
 * - 3D Elevation Isoline contour line mesh generation
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TerrainMeshGenerator {

    private static final float UV_SCALE = 0.25f; // 4 meters per texture tile repeat

    public Mesh generateMesh(Terrarium terrarium) {
        return generateMesh(terrarium, 1.0f, true, false);
    }

    public Mesh generateMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt, boolean isGamified) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight(); // Horizontal Y in domain -> JME Z (Depth)
        int depth = terrarium.getDepth();   // Vertical Z in domain -> JME Y (Altitude)

        int cutX = Math.max(1, Math.min(width, (int) Math.ceil(width * Math.max(0.05f, Math.min(1.0f, sliceRatio)))));
        int indexOffset = 0;
        double lat = Math.abs(terrarium.getLatitude());

        for (int x = 0; x < cutX; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    if (cell.material() == TerrariumCell.Material.AIR || cell.material() == TerrariumCell.Material.CAVITY) {
                        continue;
                    }

                    // JME Coords: X=x, Y=z (Up, altitude), Z=y (North/South depth)
                    float jmeX = x;
                    float jmeY = z;
                    float jmeZ = y;

                    boolean isSurface = (z == depth - 1 || isAir(terrarium, x, y, z + 1));

                    // Base Substrate / Biome color
                    float r = 0.40f, g = 0.28f, b = 0.16f, a = 1.0f;
                    if (isGamified) {
                        if (cell.material() == TerrariumCell.Material.SAND) {
                            r = 0.90f; g = 0.82f; b = 0.48f;
                        } else if (cell.material() == TerrariumCell.Material.CLAY) {
                            r = 0.78f; g = 0.38f; b = 0.20f;
                        } else if (cell.material() == TerrariumCell.Material.ROCK) {
                            r = 0.52f; g = 0.54f; b = 0.56f;
                        } else if (cell.material() == TerrariumCell.Material.GRAVEL) {
                            r = 0.65f; g = 0.67f; b = 0.70f;
                        } else if (cell.material() == TerrariumCell.Material.PEAT) {
                            r = 0.28f; g = 0.18f; b = 0.10f;
                        } else {
                            r = 0.45f; g = 0.30f; b = 0.18f;
                        }
                    } else {
                        if (lat > 60.0) {
                            r = 0.88f; g = 0.92f; b = 0.95f; // Tundra snow / frost
                        } else if (lat < 23.5) {
                            r = 0.86f; g = 0.74f; b = 0.46f; // Tropical / desert sand
                        } else if (cell.material() == TerrariumCell.Material.SAND) {
                            r = 0.84f; g = 0.74f; b = 0.46f;
                        } else if (cell.material() == TerrariumCell.Material.CLAY) {
                            r = 0.70f; g = 0.36f; b = 0.20f;
                        } else if (cell.material() == TerrariumCell.Material.ROCK) {
                            r = 0.54f; g = 0.56f; b = 0.58f;
                        } else if (cell.material() == TerrariumCell.Material.GRAVEL) {
                            r = 0.62f; g = 0.64f; b = 0.66f;
                        } else if (cell.material() == TerrariumCell.Material.PEAT) {
                            r = 0.35f; g = 0.22f; b = 0.12f;
                        } else {
                            r = 0.48f; g = 0.34f; b = 0.22f;
                        }
                    }

                    // Surface face color (Green meadow grass unless desert/snow)
                    float topR = r, topG = g, topB = b;
                    if (isSurface) {
                        if (lat > 60.0) {
                            topR = 0.92f; topG = 0.95f; topB = 0.98f;
                        } else if (lat < 23.5 || cell.material() == TerrariumCell.Material.SAND) {
                            topR = 0.88f; topG = 0.78f; topB = 0.48f;
                        } else {
                            // Temperate lush green grass
                            if (isGamified) {
                                topR = 0.22f; topG = 0.72f; topB = 0.20f;
                            } else {
                                topR = 0.32f; topG = 0.65f; topB = 0.24f;
                            }
                        }
                    }

                    // 1. Top Face (+Y in JME, +Z in domain)
                    if (z == depth - 1 || isAir(terrarium, x, y, z + 1)) {
                        addUpFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, topR, topG, topB, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 2. Bottom Face (-Y in JME, -Z in domain)
                    if (showSkirt && (z == 0 || isAir(terrarium, x, y, z - 1))) {
                        addDownFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.7f, g * 0.7f, b * 0.7f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 3. Front Face (+Z in JME, +Y in domain)
                    if ((y == height - 1 && showSkirt) || isAir(terrarium, x, y + 1, z)) {
                        addFrontFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.85f, g * 0.85f, b * 0.85f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 4. Back Face (-Z in JME, -Y in domain)
                    if ((y == 0 && showSkirt) || isAir(terrarium, x, y - 1, z)) {
                        addBackFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.80f, g * 0.80f, b * 0.80f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 5. Left Face (-X in JME, -X in domain)
                    if ((x == 0 && showSkirt) || isAir(terrarium, x - 1, y, z)) {
                        addLeftFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.90f, g * 0.90f, b * 0.90f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 6. Right Face (+X in JME, +X in domain) - Also represents the Cut Wall on slice plane!
                    if ((x == cutX - 1) || isAir(terrarium, x + 1, y, z)) {
                        addRightFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.95f, g * 0.95f, b * 0.95f, a);
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
        mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(toFloatArray(colors)));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(toIntArray(indices)));
        mesh.updateBound();

        return mesh;
    }

    /**
     * Generates a 3D Elevation Isoline contour line mesh.
     */
    public Mesh generateIsolinesMesh(Terrarium terrarium, float sliceRatio, float stepHeight) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int cutX = Math.max(1, Math.min(width, (int) Math.ceil(width * Math.max(0.05f, Math.min(1.0f, sliceRatio)))));
        float step = Math.max(1.0f, stepHeight);

        int idx = 0;
        for (int x = 0; x < cutX - 1; x++) {
            for (int y = 0; y < height - 1; y++) {
                float e00 = terrarium.getSurfaceElevation(x, y);
                float e10 = terrarium.getSurfaceElevation(x + 1, y);
                float e01 = terrarium.getSurfaceElevation(x, y + 1);
                float e11 = terrarium.getSurfaceElevation(x + 1, y + 1);

                float minE = Math.min(Math.min(e00, e10), Math.min(e01, e11));
                float maxE = Math.max(Math.max(e00, e10), Math.max(e01, e11));

                int kStart = (int) Math.ceil(minE / step);
                int kEnd = (int) Math.floor(maxE / step);

                for (int k = kStart; k <= kEnd; k++) {
                    float isoLevel = k * step;
                    // Horizontal segment in quad
                    if ((e00 - isoLevel) * (e10 - isoLevel) <= 0) {
                        float t = (isoLevel - e00) / Math.max(0.001f, (e10 - e00));
                        float lx1 = x + t;
                        float ly1 = y;
                        float lz1 = isoLevel + 0.08f; // slightly above surface to prevent z-fighting

                        float lx2 = x + 0.5f;
                        float ly2 = y + 0.5f;
                        float lz2 = (e00 + e10 + e01 + e11) / 4.0f + 0.08f;

                        vertices.add(lx1); vertices.add(lz1); vertices.add(ly1);
                        vertices.add(lx2); vertices.add(lz2); vertices.add(ly2);
                        indices.add(idx++);
                        indices.add(idx++);
                    }
                    if ((e00 - isoLevel) * (e01 - isoLevel) <= 0) {
                        float t = (isoLevel - e00) / Math.max(0.001f, (e01 - e00));
                        float lx1 = x;
                        float ly1 = y + t;
                        float lz1 = isoLevel + 0.08f;

                        float lx2 = x + 0.5f;
                        float ly2 = y + 0.5f;
                        float lz2 = (e00 + e10 + e01 + e11) / 4.0f + 0.08f;

                        vertices.add(lx1); vertices.add(lz1); vertices.add(ly1);
                        vertices.add(lx2); vertices.add(lz2); vertices.add(ly2);
                        indices.add(idx++);
                        indices.add(idx++);
                    }
                }
            }
        }

        if (vertices.isEmpty()) {
            return null;
        }

        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);
        lineMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(toFloatArray(vertices)));
        lineMesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(toIntArray(indices)));
        lineMesh.updateBound();
        return lineMesh;
    }

    private boolean isAir(Terrarium terrarium, int x, int y, int z) {
        if (!terrarium.inBounds(x, y, z)) {
            return true;
        }
        TerrariumCell cell = terrarium.getCell(x, y, z);
        return cell.material() == TerrariumCell.Material.AIR || cell.material() == TerrariumCell.Material.CAVITY;
    }

    // --- Face Generation Helpers with Continuous Seamless World UVs and Vertex Colors ---

    private void addColors(List<Float> c, float r, float g, float b, float a) {
        for (int i = 0; i < 4; i++) {
            c.add(r); c.add(g); c.add(b); c.add(a);
        }
    }

    private void addUpFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float r, float g, float b, float a) {
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

        t.add(x0 * UV_SCALE); t.add(z0 * UV_SCALE);
        t.add(x0 * UV_SCALE); t.add(z1 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(z1 * UV_SCALE);
        t.add(x1 * UV_SCALE); t.add(z0 * UV_SCALE);

        addColors(c, r, g, b, a);
    }

    private void addDownFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float r, float g, float b, float a) {
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

        addColors(c, r, g, b, a);
    }

    private void addFrontFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float r, float g, float b, float a) {
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

        addColors(c, r, g, b, a);
    }

    private void addBackFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float r, float g, float b, float a) {
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

        addColors(c, r, g, b, a);
    }

    private void addRightFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float r, float g, float b, float a) {
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

        addColors(c, r, g, b, a);
    }

    private void addLeftFace(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float r, float g, float b, float a) {
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

        addColors(c, r, g, b, a);
    }

    private void addIndices(List<Integer> indices, int offset) {
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
