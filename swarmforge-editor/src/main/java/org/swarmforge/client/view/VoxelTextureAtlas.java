/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Procedural pixel-art Minecraft-style block texture atlas generator for Gamified Mode.
 * Generates an authentic 4x4 tile grid of 16x16 pixel blocks:
 * - Row 0: Grass Top, Grass Side (with overhang fringe), Dirt, Stone
 * - Row 1: Cobblestone/Gravel, Sand, Clay, Peat
 * - Row 2: Oak Bark Side, Oak Rings Top, Leaves, Water
 * - Row 3: Bedrock, Snow Top, Snow Side, Wood Planks
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public final class VoxelTextureAtlas {

    public static final int TILE_SIZE = 16;
    public static final int TILES_PER_ROW = 4;
    public static final int ATLAS_SIZE = TILE_SIZE * TILES_PER_ROW; // 64x64 pixels

    // Tile indices (column, row)
    public static final int TILE_GRASS_TOP = 0;
    public static final int TILE_GRASS_SIDE = 1;
    public static final int TILE_DIRT = 2;
    public static final int TILE_STONE = 3;

    public static final int TILE_COBBLE = 4;
    public static final int TILE_SAND = 5;
    public static final int TILE_CLAY = 6;
    public static final int TILE_PEAT = 7;

    public static final int TILE_OAK_SIDE = 8;
    public static final int TILE_OAK_TOP = 9;
    public static final int TILE_LEAVES = 10;
    public static final int TILE_WATER = 11;

    public static final int TILE_BEDROCK = 12;
    public static final int TILE_SNOW_TOP = 13;
    public static final int TILE_SNOW_SIDE = 14;
    public static final int TILE_PLANKS = 15;

    private static Texture2D cachedAtlasTexture = null;

    private VoxelTextureAtlas() {}

    /**
     * Gets or creates the cached pixel-art texture atlas.
     */
    public static synchronized Texture2D getAtlasTexture() {
        if (cachedAtlasTexture == null) {
            cachedAtlasTexture = createAtlasTexture();
        }
        return cachedAtlasTexture;
    }

    /**
     * Returns the UV coordinate bounding box [uMin, vMin, uMax, vMax] for a given tile index.
     */
    public static float[] getTileUV(int tileIndex) {
        int tx = tileIndex % TILES_PER_ROW;
        int ty = tileIndex / TILES_PER_ROW;

        float uMin = (float) tx / TILES_PER_ROW;
        float uMax = (float) (tx + 1) / TILES_PER_ROW;
        // JME UV coordinates: V=0 is bottom, V=1 is top
        float vMin = 1.0f - (float) (ty + 1) / TILES_PER_ROW;
        float vMax = 1.0f - (float) ty / TILES_PER_ROW;

        return new float[]{uMin, vMin, uMax, vMax};
    }

    private static Texture2D createAtlasTexture() {
        ByteBuffer buffer = BufferUtils.createByteBuffer(ATLAS_SIZE * ATLAS_SIZE * 4);
        int[][][] atlasPixels = new int[ATLAS_SIZE][ATLAS_SIZE][4];

        Random rng = new Random(1337);

        // 1. Generate individual 16x16 tiles
        paintGrassTop(atlasPixels, 0, 0, rng);
        paintGrassSide(atlasPixels, 1, 0, rng);
        paintDirt(atlasPixels, 2, 0, rng);
        paintStone(atlasPixels, 3, 0, rng);

        paintCobble(atlasPixels, 0, 1, rng);
        paintSand(atlasPixels, 1, 1, rng);
        paintClay(atlasPixels, 2, 1, rng);
        paintPeat(atlasPixels, 3, 1, rng);

        paintOakSide(atlasPixels, 0, 2, rng);
        paintOakTop(atlasPixels, 1, 2, rng);
        paintLeaves(atlasPixels, 2, 2, rng);
        paintWater(atlasPixels, 3, 2, rng);

        paintBedrock(atlasPixels, 0, 3, rng);
        paintSnowTop(atlasPixels, 1, 3, rng);
        paintSnowSide(atlasPixels, 2, 3, rng);
        paintPlanks(atlasPixels, 3, 3, rng);

        // Fill byte buffer (RGBA)
        for (int y = 0; y < ATLAS_SIZE; y++) {
            for (int x = 0; x < ATLAS_SIZE; x++) {
                buffer.put((byte) atlasPixels[y][x][0]);
                buffer.put((byte) atlasPixels[y][x][1]);
                buffer.put((byte) atlasPixels[y][x][2]);
                buffer.put((byte) atlasPixels[y][x][3]);
            }
        }
        buffer.flip();

        Image img = new Image(Image.Format.RGBA8, ATLAS_SIZE, ATLAS_SIZE, buffer, ColorSpace.sRGB);
        Texture2D tex = new Texture2D(img);
        tex.setMagFilter(Texture.MagFilter.Nearest); // Nearest-neighbor for crisp Minecraft pixel-art
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setWrap(Texture.WrapMode.Clamp);
        return tex;
    }

    private static void setPixel(int[][][] atlas, int tx, int ty, int px, int py, int r, int g, int b, int a) {
        int x = tx * TILE_SIZE + px;
        int y = ty * TILE_SIZE + py;
        if (x >= 0 && x < ATLAS_SIZE && y >= 0 && y < ATLAS_SIZE) {
            atlas[y][x][0] = Math.max(0, Math.min(255, r));
            atlas[y][x][1] = Math.max(0, Math.min(255, g));
            atlas[y][x][2] = Math.max(0, Math.min(255, b));
            atlas[y][x][3] = Math.max(0, Math.min(255, a));
        }
    }

    private static void paintGrassTop(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 86, baseG = 175, baseB = 46; // Lush Minecraft foliage green
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(25) - 12;
                if ((x + y) % 3 == 0) noise += 8;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintDirt(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 134, baseG = 96, baseB = 67; // Warm brown dirt
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(30) - 15;
                if (rng.nextFloat() < 0.12f) noise += 25; // Light pebble speckle
                if (rng.nextFloat() < 0.12f) noise -= 25; // Dark soil clump
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintGrassSide(int[][][] atlas, int tx, int ty, Random rng) {
        // Base dirt
        paintDirt(atlas, tx, ty, rng);

        // Jagged grass fringe hanging from top (0-4 pixels depth)
        int[] fringePattern = {3, 4, 3, 2, 4, 3, 2, 3, 4, 3, 2, 3, 4, 3, 2, 3};
        int gR = 86, gG = 175, gB = 46;
        for (int x = 0; x < TILE_SIZE; x++) {
            int depth = fringePattern[x % fringePattern.length];
            for (int y = 0; y < depth; y++) {
                int noise = rng.nextInt(20) - 10;
                setPixel(atlas, tx, ty, x, y, gR + noise, gG + noise, gB + noise, 255);
            }
            // Hanging root/pixel
            if (depth >= 4 && x % 2 == 0) {
                setPixel(atlas, tx, ty, x, depth, gR - 15, gG - 15, gB - 15, 255);
            }
        }
    }

    private static void paintStone(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 125, baseG = 125, baseB = 125; // Classic stone grey
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(24) - 12;
                if ((x * 3 + y * 7) % 5 == 0) noise += 18;
                if ((x * 5 + y * 2) % 6 == 0) noise -= 18;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintCobble(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 110, baseG = 110, baseB = 110;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                boolean isMortar = (x % 4 == 0 || y % 4 == 0) && rng.nextFloat() < 0.75f;
                int val = isMortar ? (baseR - 35) : (baseR + rng.nextInt(35) - 15);
                setPixel(atlas, tx, ty, x, y, val, val, val, 255);
            }
        }
    }

    private static void paintSand(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 219, baseG = 207, baseB = 153; // Golden desert sand
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(20) - 10;
                if ((x + y * 2) % 4 == 0) noise += 8;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintClay(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 160, baseG = 95, baseB = 68; // Terracotta clay
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int wave = (int) (Math.sin(x * 0.5) * 8);
                int noise = rng.nextInt(16) - 8 + wave;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintPeat(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 75, baseG = 50, baseB = 32; // Rich dark organic humus
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(22) - 11;
                if (rng.nextFloat() < 0.10f) noise += 20;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintOakSide(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 107, baseG = 84, baseB = 51; // Oak bark brown
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int stripe = (x % 3 == 0) ? -20 : ((x % 3 == 1) ? 15 : 0);
                int noise = rng.nextInt(14) - 7 + stripe;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintOakTop(int[][][] atlas, int tx, int ty, Random rng) {
        int center = TILE_SIZE / 2;
        int barkR = 107, barkG = 84, barkB = 51;
        int woodR = 188, woodG = 152, woodB = 98;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                double dist = Math.sqrt((x - center + 0.5) * (x - center + 0.5) + (y - center + 0.5) * (y - center + 0.5));
                if (x == 0 || x == TILE_SIZE - 1 || y == 0 || y == TILE_SIZE - 1) {
                    setPixel(atlas, tx, ty, x, y, barkR, barkG, barkB, 255);
                } else {
                    int ring = ((int) (dist * 2)) % 2 == 0 ? -12 : 12;
                    int noise = rng.nextInt(10) - 5 + ring;
                    setPixel(atlas, tx, ty, x, y, woodR + noise, woodG + noise, woodB + noise, 255);
                }
            }
        }
    }

    private static void paintLeaves(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 60, baseG = 145, baseB = 30; // Dappled canopy green
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(35) - 17;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }

    private static void paintWater(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 40, baseG = 120, baseB = 220;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int wave = (int) (Math.sin((x + y) * 0.8) * 15);
                int noise = rng.nextInt(12) - 6 + wave;
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 210);
            }
        }
    }

    private static void paintBedrock(int[][][] atlas, int tx, int ty, Random rng) {
        int base = 45;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(40) - 20;
                int v = Math.max(10, Math.min(90, base + noise));
                setPixel(atlas, tx, ty, x, y, v, v, v, 255);
            }
        }
    }

    private static void paintSnowTop(int[][][] atlas, int tx, int ty, Random rng) {
        int base = 240;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                int noise = rng.nextInt(14) - 7;
                int v = Math.max(220, Math.min(255, base + noise));
                setPixel(atlas, tx, ty, x, y, v, v, v + 2, 255);
            }
        }
    }

    private static void paintSnowSide(int[][][] atlas, int tx, int ty, Random rng) {
        paintDirt(atlas, tx, ty, rng);
        int[] fringePattern = {4, 5, 4, 3, 5, 4, 3, 4, 5, 4, 3, 4, 5, 4, 3, 4};
        for (int x = 0; x < TILE_SIZE; x++) {
            int depth = fringePattern[x % fringePattern.length];
            for (int y = 0; y < depth; y++) {
                int noise = rng.nextInt(12) - 6;
                int v = Math.max(220, Math.min(255, 240 + noise));
                setPixel(atlas, tx, ty, x, y, v, v, v + 2, 255);
            }
        }
    }

    private static void paintPlanks(int[][][] atlas, int tx, int ty, Random rng) {
        int baseR = 170, baseG = 130, baseB = 80;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                boolean isGroove = (y % 4 == 0);
                int noise = isGroove ? -30 : (rng.nextInt(16) - 8);
                setPixel(atlas, tx, ty, x, y, baseR + noise, baseG + noise, baseB + noise, 255);
            }
        }
    }
}
