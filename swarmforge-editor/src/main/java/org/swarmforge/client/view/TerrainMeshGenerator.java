package org.swarmforge.client.view;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimized dual-mode terrain mesh generator for JMonkeyEngine:
 * - REALISTIC MODE: Continuous, smooth natural heightfield surface with interpolated normals,
 *   seamless world UVs, multi-biome vertex colors (grass, sand, rock, snow) and clean 3D strata skirt.
 * - GAMIFIED MODE: Discrete Minecraft-style cubic voxel blocks with stylized vibrant block colors.
 * - ELEVATION ISOLINES: 3D vector contour lines for topographical analysis.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TerrainMeshGenerator {

    private static final float UV_SCALE = 0.20f; // 5 meters per texture tile repeat

    public Mesh generateMesh(Terrarium terrarium) {
        return generateMesh(terrarium, 1.0f, true, false);
    }

    public Mesh generateMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt, boolean isGamified) {
        if (isGamified) {
            return generateVoxelMesh(terrarium, sliceRatio, showSkirt);
        } else {
            return generateRealisticMesh(terrarium, sliceRatio, showSkirt);
        }
    }

    /**
     * Generates a continuous, smooth natural heightfield landscape for Realistic Mode.
     */
    private Mesh generateRealisticMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight(); // Horizontal Y in domain -> JME Z (Depth)
        int depth = terrarium.getDepth();

        int cutX = Math.max(2, Math.min(width, (int) Math.ceil(width * Math.max(0.05f, Math.min(1.0f, sliceRatio)))));
        double lat = Math.abs(terrarium.getLatitude());

        // 1. Surface Heightfield Grid Vertices
        int[][] vertexIndexGrid = new int[cutX][height];
        int vertexCounter = 0;

        for (int x = 0; x < cutX; x++) {
            for (int y = 0; y < height; y++) {
                float elev = terrarium.getSurfaceElevation(x, y);

                // Calculate smooth surface normal from neighbors
                float eL = terrarium.getSurfaceElevation(Math.max(0, x - 1), y);
                float eR = terrarium.getSurfaceElevation(Math.min(width - 1, x + 1), y);
                float eD = terrarium.getSurfaceElevation(x, Math.max(0, y - 1));
                float eU = terrarium.getSurfaceElevation(x, Math.min(height - 1, y + 1));
                Vector3f norm = new Vector3f((eL - eR) * 0.5f, 1.0f, (eD - eU) * 0.5f).normalizeLocal();

                // Multi-biome natural coloration
                TerrariumCell topCell = terrarium.getCell(x, y, Math.min(depth - 1, Math.max(0, (int) elev)));
                TerrariumCell.Material mat = topCell != null ? topCell.material() : TerrariumCell.Material.PEAT;

                float r, g, b;
                if (lat > 60.0) {
                    // Snow / Alpine frost
                    r = 0.90f; g = 0.93f; b = 0.97f;
                } else if (lat < 23.5 || mat == TerrariumCell.Material.SAND) {
                    // Golden Sand / Beach / Desert
                    r = 0.88f; g = 0.78f; b = 0.48f;
                } else if (mat == TerrariumCell.Material.ROCK) {
                    // Mountain Granite
                    r = 0.55f; g = 0.58f; b = 0.60f;
                } else if (mat == TerrariumCell.Material.CLAY) {
                    // Clay / Terracotta
                    r = 0.72f; g = 0.40f; b = 0.22f;
                } else {
                    // Temperate lush meadow grass
                    r = 0.28f; g = 0.62f; b = 0.22f;
                }

                vertices.add((float) x);
                vertices.add(elev);
                vertices.add((float) y);

                normals.add(norm.x);
                normals.add(norm.y);
                normals.add(norm.z);

                texCoords.add(x * UV_SCALE);
                texCoords.add(y * UV_SCALE);

                // Pure neutral base color for realistic PBR lighting so textures are vibrant and un-dimmed
                colors.add(1.0f); colors.add(1.0f); colors.add(1.0f); colors.add(1.0f);

                vertexIndexGrid[x][y] = vertexCounter++;
            }
        }

        // 2. Surface Triangles (Quads)
        for (int x = 0; x < cutX - 1; x++) {
            for (int y = 0; y < height - 1; y++) {
                int i00 = vertexIndexGrid[x][y];
                int i10 = vertexIndexGrid[x + 1][y];
                int i01 = vertexIndexGrid[x][y + 1];
                int i11 = vertexIndexGrid[x + 1][y + 1];

                // Triangle 1
                indices.add(i00);
                indices.add(i01);
                indices.add(i10);

                // Triangle 2
                indices.add(i10);
                indices.add(i01);
                indices.add(i11);
            }
        }

        // 3. Geological Strata Skirt & Slice Plane Wall
        if (showSkirt) {
            // Front Boundary Wall (y = height - 1)
            addWallStrip(vertices, normals, texCoords, colors, indices, cutX, (i) -> (float) i, (i) -> (float) (height - 1), (i) -> terrarium.getSurfaceElevation(i, height - 1), 0f, 0f, 1f);
            // Back Boundary Wall (y = 0)
            addWallStrip(vertices, normals, texCoords, colors, indices, cutX, (i) -> (float) i, (i) -> 0f, (i) -> terrarium.getSurfaceElevation(i, 0), 0f, 0f, -1f);
            // Left Boundary Wall (x = 0)
            addWallStrip(vertices, normals, texCoords, colors, indices, height, (i) -> 0f, (i) -> (float) i, (i) -> terrarium.getSurfaceElevation(0, i), -1f, 0f, 0f);
            // Right Slice Plane Cut Wall (x = cutX - 1)
            addWallStrip(vertices, normals, texCoords, colors, indices, height, (i) -> (float) (cutX - 1), (i) -> (float) i, (i) -> terrarium.getSurfaceElevation(cutX - 1, i), 1f, 0f, 0f);
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

    private interface CoordFunc { float get(int i); }

    private void addWallStrip(List<Float> vertices, List<Float> normals, List<Float> texCoords, List<Float> colors,
                              List<Integer> indices, int count, CoordFunc getX, CoordFunc getZ, CoordFunc getTopY,
                              float nx, float ny, float nz) {
        int baseIdx = vertices.size() / 3;
        float bottomY = 0f;

        for (int i = 0; i < count; i++) {
            float px = getX.get(i);
            float pz = getZ.get(i);
            float topY = getTopY.get(i);

            // Top vertex
            vertices.add(px); vertices.add(topY); vertices.add(pz);
            normals.add(nx); normals.add(ny); normals.add(nz);
            texCoords.add((px + pz) * UV_SCALE); texCoords.add(topY * UV_SCALE);
            colors.add(0.45f); colors.add(0.30f); colors.add(0.18f); colors.add(1.0f);

            // Bottom vertex (Bedrock)
            vertices.add(px); vertices.add(bottomY); vertices.add(pz);
            normals.add(nx); normals.add(ny); normals.add(nz);
            texCoords.add((px + pz) * UV_SCALE); texCoords.add(bottomY * UV_SCALE);
            colors.add(0.20f); colors.add(0.25f); colors.add(0.32f); colors.add(1.0f);
        }

        for (int i = 0; i < count - 1; i++) {
            int iTop1 = baseIdx + i * 2;
            int iBot1 = baseIdx + i * 2 + 1;
            int iTop2 = baseIdx + (i + 1) * 2;
            int iBot2 = baseIdx + (i + 1) * 2 + 1;

            if (nz > 0 || nx < 0) {
                // Front (+Z) and Left (-X) outward facing winding
                indices.add(iTop1); indices.add(iBot1); indices.add(iTop2);
                indices.add(iTop2); indices.add(iBot1); indices.add(iBot2);
            } else {
                // Back (-Z) and Right (+X / Slice Plane) outward facing winding
                indices.add(iTop1); indices.add(iTop2); indices.add(iBot1);
                indices.add(iTop2); indices.add(iBot2); indices.add(iBot1);
            }
        }
    }

    /**
     * Generates discrete voxel block cubic meshes for Gamified Mode (Minecraft style).
     */
    private Mesh generateVoxelMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        int cutX = Math.max(1, Math.min(width, (int) Math.ceil(width * Math.max(0.05f, Math.min(1.0f, sliceRatio)))));
        int indexOffset = 0;

        for (int x = 0; x < cutX; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    if (cell.material() == TerrariumCell.Material.AIR || cell.material() == TerrariumCell.Material.CAVITY) {
                        continue;
                    }

                    boolean isSurface = (z == depth - 1 || isAir(terrarium, x, y, z + 1));

                    // Vibrant substrate voxel colors
                    float r = 0.45f, g = 0.30f, b = 0.18f, a = 1.0f;
                    if (cell.material() == TerrariumCell.Material.SAND) {
                        r = 0.92f; g = 0.85f; b = 0.48f;
                    } else if (cell.material() == TerrariumCell.Material.CLAY) {
                        r = 0.80f; g = 0.40f; b = 0.20f;
                    } else if (cell.material() == TerrariumCell.Material.ROCK) {
                        r = 0.55f; g = 0.58f; b = 0.62f;
                    } else if (cell.material() == TerrariumCell.Material.GRAVEL) {
                        r = 0.68f; g = 0.70f; b = 0.72f;
                    } else if (cell.material() == TerrariumCell.Material.PEAT) {
                        r = 0.28f; g = 0.18f; b = 0.10f;
                    }

                    // Deterministic subtle noise variation per voxel block to eliminate flat color boredom
                    float var = (float) (((x * 17 + y * 31 + z * 7) % 9) - 4) * 0.015f;
                    float topR = r + var;
                    float topG = g + var;
                    float topB = b + var;

                    if (isSurface) {
                        double lat = Math.abs(terrarium.getLatitude());
                        if (lat > 60.0) {
                            topR = 0.92f + var; topG = 0.94f + var; topB = 0.98f; // Arctic snow cover
                        } else if (cell.material() == TerrariumCell.Material.EARTH || cell.material() == TerrariumCell.Material.SILT) {
                            topR = 0.20f + var; topG = 0.72f + var; topB = 0.18f; // Lush meadow grass
                        } else if (cell.material() == TerrariumCell.Material.PEAT) {
                            topR = 0.32f + var; topG = 0.22f + var; topB = 0.12f; // Rich organic humus / dark peat
                        } else if (cell.material() == TerrariumCell.Material.SAND) {
                            topR = 0.95f + var; topG = 0.88f + var; topB = 0.52f; // Golden sand dune top
                        } else if (cell.material() == TerrariumCell.Material.CLAY) {
                            topR = 0.82f + var; topG = 0.44f + var; topB = 0.22f; // Terracotta clay top
                        } else if (cell.material() == TerrariumCell.Material.ROCK) {
                            topR = 0.60f + var; topG = 0.62f + var; topB = 0.65f; // Granite outcropping top
                        } else if (cell.material() == TerrariumCell.Material.GRAVEL) {
                            topR = 0.72f + var; topG = 0.74f + var; topB = 0.76f; // River pebble gravel top
                        }
                    }

                    float jmeX = x;
                    float jmeY = z;
                    float jmeZ = y;

                    // 1. Top Face
                    if (z == depth - 1 || isAir(terrarium, x, y, z + 1)) {
                        addUpFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, topR, topG, topB, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 2. Bottom Face
                    if (showSkirt && (z == 0 || isAir(terrarium, x, y, z - 1))) {
                        addDownFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.7f, g * 0.7f, b * 0.7f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 3. Front Face
                    if ((y == height - 1 && showSkirt) || isAir(terrarium, x, y + 1, z)) {
                        addFrontFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.85f, g * 0.85f, b * 0.85f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 4. Back Face
                    if ((y == 0 && showSkirt) || isAir(terrarium, x, y - 1, z)) {
                        addBackFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.80f, g * 0.80f, b * 0.80f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 5. Left Face
                    if ((x == 0 && showSkirt) || isAir(terrarium, x - 1, y, z)) {
                        addLeftFace(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, r * 0.90f, g * 0.90f, b * 0.90f, a);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 6. Right Face (Cut Wall)
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
                    if ((e00 - isoLevel) * (e10 - isoLevel) <= 0) {
                        float t = (isoLevel - e00) / Math.max(0.001f, (e10 - e00));
                        float lx1 = x + t;
                        float ly1 = y;
                        float lz1 = isoLevel + 0.08f;

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

    // --- Face Helpers ---
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
