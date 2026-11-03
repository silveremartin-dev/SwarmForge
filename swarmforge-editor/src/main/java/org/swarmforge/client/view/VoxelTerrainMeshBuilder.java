/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

/**
 * Builds stylized cubic voxel terrain meshes (Minecraft style) for Gamified Mode.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class VoxelTerrainMeshBuilder {

    private VoxelTerrainMeshBuilder() {}

    public static Mesh generateVoxelMesh(Terrarium terrarium, float sliceRatio, boolean showSkirt) {
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
                    if (cell.material() == TerrariumCell.Material.AIR 
                            || cell.material() == TerrariumCell.Material.CAVITY 
                            || cell.material() == TerrariumCell.Material.CHAMBER) {
                        continue;
                    }

                    boolean isSurface = (z == depth - 1 || isAir(terrarium, x, y, z + 1));
                    TerrariumCell.Material mat = cell.material();

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
                    } else {
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

                    if (z == depth - 1 || isAir(terrarium, x, y, z + 1)) {
                        addUpFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvTop);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    if (showSkirt && (z == 0 || isAir(terrarium, x, y, z - 1))) {
                        addDownFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvBot);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    if ((y == height - 1 && showSkirt) || isAir(terrarium, x, y + 1, z)) {
                        addFrontFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    if ((y == 0 && showSkirt) || isAir(terrarium, x, y - 1, z)) {
                        addBackFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

                    if ((x == 0 && showSkirt) || isAir(terrarium, x - 1, y, z)) {
                        addLeftFaceUV(jmeX, jmeY, jmeZ, vertices, normals, texCoords, colors, uvSide);
                        addIndices(indices, indexOffset);
                        indexOffset += 4;
                    }

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

    private static boolean isAir(Terrarium terrarium, int x, int y, int z) {
        if (!terrarium.inBounds(x, y, z)) {
            return true;
        }
        TerrariumCell cell = terrarium.getCell(x, y, z);
        return cell.material() == TerrariumCell.Material.AIR 
                || cell.material() == TerrariumCell.Material.CAVITY 
                || cell.material() == TerrariumCell.Material.CHAMBER;
    }

    private static void addUpFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

    private static void addDownFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

    private static void addFrontFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

    private static void addBackFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

    private static void addRightFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

    private static void addLeftFaceUV(float x, float y, float z, List<Float> v, List<Float> n, List<Float> t, List<Float> c, float[] uv) {
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

    private static void addColors(List<Float> c, float r, float g, float b, float a) {
        for (int i = 0; i < 4; i++) {
            c.add(r); c.add(g); c.add(b); c.add(a);
        }
    }

    private static void addIndices(List<Integer> indices, int offset) {
        indices.add(offset + 0);
        indices.add(offset + 1);
        indices.add(offset + 2);
        indices.add(offset + 2);
        indices.add(offset + 3);
        indices.add(offset + 0);
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
