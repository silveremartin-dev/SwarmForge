package org.swarmforge.client.view;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

/**
 * Optimized dual-mode terrain mesh generator for JMonkeyEngine:
 * - REALISTIC MODE: Continuous, smooth natural heightfield surface with interpolated normals,
 *   seamless world UVs, multi-biome vertex colors (grass, sand, rock, snow) and clean 3D strata skirt.
 * - GAMIFIED MODE: Discrete Minecraft-style cubic voxel blocks via {@link VoxelTerrainMeshBuilder}.
 * - ELEVATION ISOLINES: 3D vector contour lines for topographical analysis.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TerrainMeshGenerator {

    private static final float UV_SCALE = 0.35f;

    public Mesh generateMesh(Terrarium terrarium) {
        return generateMesh(terrarium, 1.0f, true, false);
    }

    public Mesh generateMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt, boolean isGamified) {
        if (isGamified) {
            return VoxelTerrainMeshBuilder.generateVoxelMesh(terrarium, sliceRatio, showSkirt);
        } else {
            return generateRealisticMesh(terrarium, sliceRatio, showSkirt);
        }
    }

    public Mesh generateWaterMesh(Terrarium terrarium, float sliceRatio) {
        if (terrarium == null) return null;
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();
        int cutX = Math.max(2, Math.min(width, (int) Math.ceil(width * Math.max(0.05f, Math.min(1.0f, sliceRatio)))));

        boolean hasWater = false;
        for (int x = 0; x < cutX - 1; x++) {
            for (int y = 0; y < height - 1; y++) {
                float elev00 = terrarium.getSurfaceElevation(x, y);
                float elev10 = terrarium.getSurfaceElevation(x + 1, y);
                float elev01 = terrarium.getSurfaceElevation(x, y + 1);
                float elev11 = terrarium.getSurfaceElevation(x + 1, y + 1);

                TerrariumCell c00 = terrarium.getCell(x, y, Math.min(depth - 1, Math.max(0, (int) elev00)));
                TerrariumCell c10 = terrarium.getCell(x + 1, y, Math.min(depth - 1, Math.max(0, (int) elev10)));
                TerrariumCell c01 = terrarium.getCell(x, y + 1, Math.min(depth - 1, Math.max(0, (int) elev01)));
                TerrariumCell c11 = terrarium.getCell(x + 1, y + 1, Math.min(depth - 1, Math.max(0, (int) elev11)));

                boolean isWaterQuad = (c00 != null && c00.material() == TerrariumCell.Material.WATER)
                                   || (c10 != null && c10.material() == TerrariumCell.Material.WATER)
                                   || (c01 != null && c01.material() == TerrariumCell.Material.WATER)
                                   || (c11 != null && c11.material() == TerrariumCell.Material.WATER);

                if (!isWaterQuad) continue;
                hasWater = true;

                int baseIdx = vertices.size() / 3;
                float wY00 = elev00 + 0.05f;
                float wY10 = elev10 + 0.05f;
                float wY01 = elev01 + 0.05f;
                float wY11 = elev11 + 0.05f;

                vertices.add((float) x); vertices.add(wY00); vertices.add((float) y);
                normals.add(0f); normals.add(1f); normals.add(0f);
                texCoords.add(x * 0.5f); texCoords.add(y * 0.5f);
                colors.add(0.18f); colors.add(0.58f); colors.add(0.92f); colors.add(0.85f);

                vertices.add((float) (x + 1)); vertices.add(wY10); vertices.add((float) y);
                normals.add(0f); normals.add(1f); normals.add(0f);
                texCoords.add((x + 1) * 0.5f); texCoords.add(y * 0.5f);
                colors.add(0.18f); colors.add(0.58f); colors.add(0.92f); colors.add(0.85f);

                vertices.add((float) x); vertices.add(wY01); vertices.add((float) (y + 1));
                normals.add(0f); normals.add(1f); normals.add(0f);
                texCoords.add(x * 0.5f); texCoords.add((y + 1) * 0.5f);
                colors.add(0.18f); colors.add(0.58f); colors.add(0.92f); colors.add(0.85f);

                vertices.add((float) (x + 1)); vertices.add(wY11); vertices.add((float) (y + 1));
                normals.add(0f); normals.add(1f); normals.add(0f);
                texCoords.add((x + 1) * 0.5f); texCoords.add((y + 1) * 0.5f);
                colors.add(0.18f); colors.add(0.58f); colors.add(0.92f); colors.add(0.85f);

                indices.add(baseIdx + 0); indices.add(baseIdx + 2); indices.add(baseIdx + 1);
                indices.add(baseIdx + 1); indices.add(baseIdx + 2); indices.add(baseIdx + 3);
            }
        }

        if (!hasWater || vertices.isEmpty()) return null;

        Mesh mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(toFloatArray(vertices)));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(toFloatArray(normals)));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(toFloatArray(texCoords)));
        mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(toFloatArray(colors)));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(toIntArray(indices)));
        mesh.updateBound();
        return mesh;
    }

    private Mesh generateRealisticMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt) {
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        int cutX = Math.max(2, Math.min(width, (int) Math.ceil(width * Math.max(0.05f, Math.min(1.0f, sliceRatio)))));
        double lat = Math.abs(terrarium.getLatitude());

        int[][] vertexIndexGrid = new int[cutX][height];
        int vertexCounter = 0;

        for (int x = 0; x < cutX; x++) {
            for (int y = 0; y < height; y++) {
                float elev = terrarium.getSurfaceElevation(x, y);

                float eL = terrarium.getSurfaceElevation(Math.max(0, x - 1), y);
                float eR = terrarium.getSurfaceElevation(Math.min(width - 1, x + 1), y);
                float eD = terrarium.getSurfaceElevation(x, Math.max(0, y - 1));
                float eU = terrarium.getSurfaceElevation(x, Math.min(height - 1, y + 1));
                Vector3f norm = new Vector3f((eL - eR) * 0.5f, 1.0f, (eD - eU) * 0.5f).normalizeLocal();

                TerrariumCell topCell = terrarium.getCell(x, y, Math.min(depth - 1, Math.max(0, (int) elev)));
                TerrariumCell.Material mat = topCell != null ? topCell.material() : TerrariumCell.Material.EARTH;

                float r = 0.42f, g = 0.82f, b = 0.32f;
                if (mat == TerrariumCell.Material.WATER) {
                    r = 0.20f; g = 0.58f; b = 0.95f;
                } else if (lat > 60.0) {
                    r = 0.98f; g = 0.98f; b = 1.0f;
                } else if (mat == TerrariumCell.Material.SAND || lat < 23.5) {
                    r = 0.95f; g = 0.84f; b = 0.46f;
                } else if (mat == TerrariumCell.Material.ROCK) {
                    r = 0.68f; g = 0.70f; b = 0.74f;
                } else if (mat == TerrariumCell.Material.GRAVEL) {
                    r = 0.78f; g = 0.74f; b = 0.68f;
                } else if (mat == TerrariumCell.Material.CLAY) {
                    r = 0.82f; g = 0.48f; b = 0.26f;
                } else if (mat == TerrariumCell.Material.PEAT || mat == TerrariumCell.Material.LEAF_LITTER) {
                    r = 0.48f; g = 0.32f; b = 0.18f;
                }

                if (mat != TerrariumCell.Material.WATER && mat != TerrariumCell.Material.ROCK && topCell != null) {
                    float hum = topCell.humidity();
                    float moistureDarken = 1.0f - (hum * 0.18f);
                    r *= moistureDarken;
                    g *= moistureDarken;
                    b *= moistureDarken;

                    boolean nearWater = false;
                    for (int dx = -1; dx <= 1 && !nearWater; dx++) {
                        for (int dy = -1; dy <= 1 && !nearWater; dy++) {
                            int nx = Math.max(0, Math.min(width - 1, x + dx));
                            int ny = Math.max(0, Math.min(height - 1, y + dy));
                            float nElev = terrarium.getSurfaceElevation(nx, ny);
                            TerrariumCell nCell = terrarium.getCell(nx, ny, Math.min(depth - 1, Math.max(0, (int) nElev)));
                            if (nCell != null && nCell.material() == TerrariumCell.Material.WATER) {
                                nearWater = true;
                            }
                        }
                    }
                    if (nearWater) {
                        r *= 0.85f;
                        g *= 0.88f;
                        b *= 0.82f;
                    }
                }

                if (mat != TerrariumCell.Material.WATER && norm.y < 0.78f) {
                    float slopeFactor = Math.min(1.0f, Math.max(0.0f, (0.78f - norm.y) / 0.28f));
                    r = r * (1.0f - slopeFactor) + 0.62f * slopeFactor;
                    g = g * (1.0f - slopeFactor) + 0.64f * slopeFactor;
                    b = b * (1.0f - slopeFactor) + 0.66f * slopeFactor;
                }

                vertices.add((float) x);
                vertices.add(elev);
                vertices.add((float) y);

                normals.add(norm.x);
                normals.add(norm.y);
                normals.add(norm.z);

                texCoords.add(x * UV_SCALE);
                texCoords.add(y * UV_SCALE);

                colors.add(r); colors.add(g); colors.add(b); colors.add(1.0f);

                vertexIndexGrid[x][y] = vertexCounter++;
            }
        }

        for (int x = 0; x < cutX - 1; x++) {
            for (int y = 0; y < height - 1; y++) {
                int i00 = vertexIndexGrid[x][y];
                int i10 = vertexIndexGrid[x + 1][y];
                int i01 = vertexIndexGrid[x][y + 1];
                int i11 = vertexIndexGrid[x + 1][y + 1];

                indices.add(i00); indices.add(i01); indices.add(i10);
                indices.add(i10); indices.add(i01); indices.add(i11);
            }
        }

        if (showSkirt) {
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, cutX, (i) -> (float) i, (i) -> (float) (height - 1), 0f, 0f, 1f);
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, cutX, (i) -> (float) i, (i) -> 0f, 0f, 0f, -1f);
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, height, (i) -> 0f, (i) -> (float) i, -1f, 0f, 0f);
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, height, (i) -> (float) (cutX - 1), (i) -> (float) i, 1f, 0f, 0f);
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

    private void addStratifiedWall(List<Float> vertices, List<Float> normals, List<Float> texCoords, List<Float> colors,
                                   List<Integer> indices, Terrarium terrarium, int count, CoordFunc getX, CoordFunc getZ,
                                   float nx, float ny, float nz) {
        int depth = terrarium.getDepth();
        for (int i = 0; i < count - 1; i++) {
            float xA = getX.get(i), zA = getZ.get(i);
            float xB = getX.get(i + 1), zB = getZ.get(i + 1);
            float topYA = terrarium.getSurfaceElevation(xA, zA);
            float topYB = terrarium.getSurfaceElevation(xB, zB);

            int maxZ = Math.max((int) Math.ceil(topYA), (int) Math.ceil(topYB));
            for (int z = 0; z < maxZ; z++) {
                float yBotA = Math.min(topYA, (float) z);
                float yTopA = Math.min(topYA, (float) (z + 1));
                float yBotB = Math.min(topYB, (float) z);
                float yTopB = Math.min(topYB, (float) (z + 1));

                if (yTopA <= yBotA && yTopB <= yBotB) continue;

                TerrariumCell cellA = terrarium.getCell(Math.round(xA), Math.round(zA), Math.min(depth - 1, z));
                if (cellA.material() == TerrariumCell.Material.AIR || cellA.material() == TerrariumCell.Material.CHAMBER || cellA.material() == TerrariumCell.Material.CAVITY) {
                    continue;
                }

                float r = 0.45f, g = 0.30f, b = 0.18f;
                float depthFromSurface = topYA - yTopA;

                if (z == 0) {
                    r = 0.25f; g = 0.28f; b = 0.32f;
                } else if (cellA.material() == TerrariumCell.Material.ROCK || cellA.material() == TerrariumCell.Material.GRAVEL) {
                    r = 0.55f; g = 0.58f; b = 0.62f;
                } else if (cellA.material() == TerrariumCell.Material.CLAY) {
                    r = 0.68f; g = 0.38f; b = 0.20f;
                } else if (cellA.material() == TerrariumCell.Material.SAND) {
                    r = 0.85f; g = 0.78f; b = 0.45f;
                } else if (cellA.material() == TerrariumCell.Material.PEAT) {
                    r = 0.22f; g = 0.15f; b = 0.09f;
                } else if (depthFromSurface <= 0.28f) {
                    r = 0.20f; g = 0.14f; b = 0.08f;
                } else if (depthFromSurface <= 1.0f) {
                    r = 0.38f; g = 0.26f; b = 0.15f;
                } else {
                    r = 0.48f; g = 0.32f; b = 0.19f;
                }

                float sedimentVar = 0.94f + 0.06f * (float) Math.sin(yTopA * 8.0f + xA * 0.5f);
                r *= sedimentVar;
                g *= sedimentVar;
                b *= sedimentVar;

                int baseIdx = vertices.size() / 3;

                vertices.add(xA); vertices.add(yTopA); vertices.add(zA);
                normals.add(nx); normals.add(ny); normals.add(nz);
                texCoords.add((xA + zA) * UV_SCALE); texCoords.add(yTopA * UV_SCALE);
                colors.add(r); colors.add(g); colors.add(b); colors.add(1.0f);

                vertices.add(xA); vertices.add(yBotA); vertices.add(zA);
                normals.add(nx); normals.add(ny); normals.add(nz);
                texCoords.add((xA + zA) * UV_SCALE); texCoords.add(yBotA * UV_SCALE);
                colors.add(r * 0.9f); colors.add(g * 0.9f); colors.add(b * 0.9f); colors.add(1.0f);

                vertices.add(xB); vertices.add(yTopB); vertices.add(zB);
                normals.add(nx); normals.add(ny); normals.add(nz);
                texCoords.add((xB + zB) * UV_SCALE); texCoords.add(yTopB * UV_SCALE);
                colors.add(r); colors.add(g); colors.add(b); colors.add(1.0f);

                vertices.add(xB); vertices.add(yBotB); vertices.add(zB);
                normals.add(nx); normals.add(ny); normals.add(nz);
                texCoords.add((xB + zB) * UV_SCALE); texCoords.add(yBotB * UV_SCALE);
                colors.add(r * 0.9f); colors.add(g * 0.9f); colors.add(b * 0.9f); colors.add(1.0f);

                if (nz > 0 || nx < 0) {
                    indices.add(baseIdx + 0); indices.add(baseIdx + 1); indices.add(baseIdx + 2);
                    indices.add(baseIdx + 2); indices.add(baseIdx + 1); indices.add(baseIdx + 3);
                } else {
                    indices.add(baseIdx + 0); indices.add(baseIdx + 2); indices.add(baseIdx + 1);
                    indices.add(baseIdx + 2); indices.add(baseIdx + 3); indices.add(baseIdx + 1);
                }
            }
        }
    }

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

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
