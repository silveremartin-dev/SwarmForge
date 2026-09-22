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

                // Multi-biome natural coloration (bright, sunny illumination)
                TerrariumCell topCell = terrarium.getCell(x, y, Math.min(depth - 1, Math.max(0, (int) elev)));
                TerrariumCell.Material mat = topCell != null ? topCell.material() : TerrariumCell.Material.PEAT;

                float r = 0.95f, g = 1.0f, b = 0.90f; // Lush meadow default
                if (mat == TerrariumCell.Material.WATER) {
                    r = 0.40f; g = 0.70f; b = 1.0f;
                } else if (lat > 60.0) {
                    r = 1.0f; g = 1.0f; b = 1.0f;
                } else if (mat == TerrariumCell.Material.SAND || lat < 23.5) {
                    r = 1.0f; g = 0.95f; b = 0.75f;
                } else if (mat == TerrariumCell.Material.ROCK || mat == TerrariumCell.Material.GRAVEL) {
                    r = 0.92f; g = 0.92f; b = 0.94f;
                } else if (mat == TerrariumCell.Material.CLAY) {
                    r = 0.95f; g = 0.78f; b = 0.65f;
                } else if (mat == TerrariumCell.Material.PEAT) {
                    r = 0.75f; g = 0.65f; b = 0.55f;
                }

                vertices.add((float) x);
                vertices.add(elev);
                vertices.add((float) y);

                normals.add(norm.x);
                normals.add(norm.y);
                normals.add(norm.z);

                texCoords.add(x * UV_SCALE);
                texCoords.add(y * UV_SCALE);

                // Modulate PBR texture by natural substrate vertex color
                colors.add(r); colors.add(g); colors.add(b); colors.add(1.0f);

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
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, cutX, (i) -> (float) i, (i) -> (float) (height - 1), 0f, 0f, 1f);
            // Back Boundary Wall (y = 0)
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, cutX, (i) -> (float) i, (i) -> 0f, 0f, 0f, -1f);
            // Left Boundary Wall (x = 0)
            addStratifiedWall(vertices, normals, texCoords, colors, indices, terrarium, height, (i) -> 0f, (i) -> (float) i, -1f, 0f, 0f);
            // Right Slice Plane Cut Wall (x = cutX - 1)
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

                // Sample substrate strata
                TerrariumCell cellA = terrarium.getCell(Math.round(xA), Math.round(zA), Math.min(depth - 1, z));
                if (cellA.material() == TerrariumCell.Material.AIR || cellA.material() == TerrariumCell.Material.CHAMBER || cellA.material() == TerrariumCell.Material.CAVITY) {
                    continue; // Leave excavated chambers/tunnels open in the cutaway skirt
                }

                float r = 0.45f, g = 0.30f, b = 0.18f; // Humus/dirt default
                if (z == 0) {
                    r = 0.25f; g = 0.28f; b = 0.32f; // Granite Bedrock
                } else if (cellA.material() == TerrariumCell.Material.ROCK || cellA.material() == TerrariumCell.Material.GRAVEL) {
                    r = 0.55f; g = 0.58f; b = 0.62f;
                } else if (cellA.material() == TerrariumCell.Material.CLAY) {
                    r = 0.70f; g = 0.42f; b = 0.24f;
                } else if (cellA.material() == TerrariumCell.Material.SAND) {
                    r = 0.85f; g = 0.78f; b = 0.45f;
                } else if (cellA.material() == TerrariumCell.Material.PEAT) {
                    r = 0.28f; g = 0.18f; b = 0.10f;
                }

                int baseIdx = vertices.size() / 3;

                // 4 Vertices for this quad segment
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
        double lat = Math.abs(terrarium.getLatitude());

        for (int x = 0; x < cutX; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    // Open hollow voids (air, natural cavities, and excavated nest chambers)
                    if (cell.material() == TerrariumCell.Material.AIR 
                            || cell.material() == TerrariumCell.Material.CAVITY 
                            || cell.material() == TerrariumCell.Material.CHAMBER) {
                        continue;
                    }

                    boolean isSurface = (z == depth - 1 || isAir(terrarium, x, y, z + 1));
                    TerrariumCell.Material mat = cell.material();

                    // Determine Minecraft Texture Tiles
                    int topTile = VoxelTextureAtlas.TILE_DIRT;
                    int sideTile = VoxelTextureAtlas.TILE_DIRT;
                    int botTile = (z == 0) ? VoxelTextureAtlas.TILE_BEDROCK : VoxelTextureAtlas.TILE_DIRT;

                    if (mat == TerrariumCell.Material.WATER) {
                        topTile = VoxelTextureAtlas.TILE_WATER;
                        sideTile = VoxelTextureAtlas.TILE_WATER;
                        botTile = VoxelTextureAtlas.TILE_WATER;
                    } else if (mat == TerrariumCell.Material.SAND) {
                        topTile = VoxelTextureAtlas.TILE_SAND;
                        sideTile = VoxelTextureAtlas.TILE_SAND;
                        botTile = VoxelTextureAtlas.TILE_SAND;
                    } else if (mat == TerrariumCell.Material.CLAY) {
                        topTile = VoxelTextureAtlas.TILE_CLAY;
                        sideTile = VoxelTextureAtlas.TILE_CLAY;
                        botTile = VoxelTextureAtlas.TILE_CLAY;
                    } else if (mat == TerrariumCell.Material.ROCK) {
                        topTile = VoxelTextureAtlas.TILE_STONE;
                        sideTile = VoxelTextureAtlas.TILE_STONE;
                        botTile = (z == 0) ? VoxelTextureAtlas.TILE_BEDROCK : VoxelTextureAtlas.TILE_STONE;
                    } else if (mat == TerrariumCell.Material.GRAVEL) {
                        topTile = VoxelTextureAtlas.TILE_COBBLE;
                        sideTile = VoxelTextureAtlas.TILE_COBBLE;
                        botTile = VoxelTextureAtlas.TILE_COBBLE;
                    } else if (mat == TerrariumCell.Material.PEAT) {
                        topTile = VoxelTextureAtlas.TILE_PEAT;
                        sideTile = VoxelTextureAtlas.TILE_PEAT;
                        botTile = VoxelTextureAtlas.TILE_PEAT;
                    } else if (mat == TerrariumCell.Material.TREE_TRUNK || mat == TerrariumCell.Material.DEAD_WOOD) {
                        topTile = VoxelTextureAtlas.TILE_OAK_TOP;
                        sideTile = VoxelTextureAtlas.TILE_OAK_SIDE;
                        botTile = VoxelTextureAtlas.TILE_OAK_TOP;
                    } else if (mat == TerrariumCell.Material.WOODEN_HIVE_BOX || mat == TerrariumCell.Material.WOOD_PULP_PAPER) {
                        topTile = VoxelTextureAtlas.TILE_PLANKS;
                        sideTile = VoxelTextureAtlas.TILE_PLANKS;
                        botTile = VoxelTextureAtlas.TILE_PLANKS;
                    } else { // EARTH / SILT
                        if (isSurface) {
                            if (lat > 60.0) {
                                topTile = VoxelTextureAtlas.TILE_SNOW_TOP;
                                sideTile = VoxelTextureAtlas.TILE_SNOW_SIDE;
                            } else {
                                topTile = VoxelTextureAtlas.TILE_GRASS_TOP;
                                sideTile = VoxelTextureAtlas.TILE_GRASS_SIDE;
                            }
                        } else {
                            topTile = VoxelTextureAtlas.TILE_DIRT;
                            sideTile = VoxelTextureAtlas.TILE_DIRT;
                        }
                    }

                    if (z == 0) {
                        botTile = VoxelTextureAtlas.TILE_BEDROCK;
                    }

                    float[] uvTop = VoxelTextureAtlas.getTileUV(topTile);
                    float[] uvSide = VoxelTextureAtlas.getTileUV(sideTile);
                    float[] uvBot = VoxelTextureAtlas.getTileUV(botTile);

                    float jmeX = x;
                    float jmeY = z;
                    float jmeZ = y;

                    // 1. Top Face
                    if (z == depth - 1 || isAir(terrarium, x, y, z + 1)) {
                        addUpFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvTop);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 2. Bottom Face
                    if (showSkirt && (z == 0 || isAir(terrarium, x, y, z - 1))) {
                        addDownFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvBot);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 3. Front Face (+Y in domain -> +Z in JME)
                    if ((y == height - 1 && showSkirt) || isAir(terrarium, x, y + 1, z)) {
                        addFrontFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 4. Back Face (-Y in domain -> -Z in JME)
                    if ((y == 0 && showSkirt) || isAir(terrarium, x, y - 1, z)) {
                        addBackFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 5. Left Face (-X)
                    if ((x == 0 && showSkirt) || isAir(terrarium, x - 1, y, z)) {
                        addLeftFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    // 6. Right Face (+X / Cut Plane)
                    if ((x == cutX - 1) || isAir(terrarium, x + 1, y, z)) {
                        addRightFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
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
        return cell.material() == TerrariumCell.Material.AIR 
                || cell.material() == TerrariumCell.Material.CAVITY 
                || cell.material() == TerrariumCell.Material.CHAMBER;
    }

    // --- UV Atlas Face Helpers for Gamified Mode ---
    private void addUpFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

        t.add(uv[0]); t.add(uv[1]);
        t.add(uv[0]); t.add(uv[3]);
        t.add(uv[2]); t.add(uv[3]);
        t.add(uv[2]); t.add(uv[1]);

        addColors(c, 1f, 1f, 1f, 1f);
    }

    private void addDownFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

        t.add(uv[0]); t.add(uv[1]);
        t.add(uv[2]); t.add(uv[1]);
        t.add(uv[2]); t.add(uv[3]);
        t.add(uv[0]); t.add(uv[3]);

        addColors(c, 0.75f, 0.75f, 0.75f, 1f);
    }

    private void addFrontFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

        t.add(uv[0]); t.add(uv[3]);
        t.add(uv[0]); t.add(uv[1]);
        t.add(uv[2]); t.add(uv[1]);
        t.add(uv[2]); t.add(uv[3]);

        addColors(c, 0.90f, 0.90f, 0.90f, 1f);
    }

    private void addBackFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

        t.add(uv[2]); t.add(uv[3]);
        t.add(uv[2]); t.add(uv[1]);
        t.add(uv[0]); t.add(uv[1]);
        t.add(uv[0]); t.add(uv[3]);

        addColors(c, 0.85f, 0.85f, 0.85f, 1f);
    }

    private void addRightFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

        t.add(uv[2]); t.add(uv[3]);
        t.add(uv[2]); t.add(uv[1]);
        t.add(uv[0]); t.add(uv[1]);
        t.add(uv[0]); t.add(uv[3]);

        addColors(c, 0.95f, 0.95f, 0.95f, 1f);
    }

    private void addLeftFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

        t.add(uv[0]); t.add(uv[3]);
        t.add(uv[0]); t.add(uv[1]);
        t.add(uv[2]); t.add(uv[1]);
        t.add(uv[2]); t.add(uv[3]);

        addColors(c, 0.90f, 0.90f, 0.90f, 1f);
    }

    private void addColors(List<Float> c, float r, float g, float b, float a) {
        for (int i = 0; i < 4; i++) {
            c.add(r); c.add(g); c.add(b); c.add(a);
        }
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
