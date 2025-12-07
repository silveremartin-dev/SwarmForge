/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.mapcomponents;

import org.jants.Terrarium;
import org.jants.TerrariumCell;
import org.jants.util.GreyscaleARGBConverter;
import org.jants.util.Morton3D;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

// resolution level is dependent of xSize, ySize and heightMap horizontal and vertical size
//it is recommended that the same scale applies in all dimensions
// https://en.wikipedia.org/wiki/Heightmap
public class HeightMapGenerator {

    //soil kind
    public static final int soilCone = 0;
    public static final int soilSpike = 1;
    public static final int soilDome = 2;
    public static final int soilFlat = 10;
    public static final int soilNoisy = 11;
    public static final int soilHill = 12;
    public static final int soilPitCone = 20;

    //x > 0, y > 0, z > 0
    //TODO: there is probably a faster way using toByteArray(BufferedImage bi, String format)
    public static byte[][] loadMap(@NotNull String file, int xDimension, int yDimension, int zDimension) throws IOException {
        byte[][] result;
        int r, g, b;
        int grayscale;
        int currentPixel;
        BufferedImage bi = ImageIO.read(new File(file));
        result = new byte[bi.getWidth()][bi.getHeight()];
        for (int i=0; i<bi.getWidth(); i++) {
            for (int j=0; j<bi.getHeight(); j++) {
                currentPixel = bi.getRGB(i, j);
                r = (currentPixel >> 16) & 0xff;
                g = (currentPixel>> 8) & 0xff;
                b = (currentPixel) & 0xff;
                grayscale = (r + g + b) / 3;
                result[i][j] = (byte) (grayscale << 16 | grayscale << 8 | grayscale);
            }
        }
        return result;
    }

    //TODO: there is probably a faster way using toBufferedImage(byte[] bytes)
    public void saveMap(@NotNull String file, @NotNull Terrarium terrarium) throws IOException {
        Morton3D mortonEncoder = new Morton3D();
        int[][] heightmap = new int[(int)Math.ceil(terrarium.xSize/terrarium.cellSize)][(int)Math.ceil(terrarium.ySize/terrarium.cellSize)];
        Iterator<Map.Entry<Long, TerrariumCell>> entryIterator = terrarium.cells.entrySet().iterator();
        for (int[] ints : heightmap) {
            Arrays.fill(ints, 0);
        }
        while (entryIterator.hasNext()) {
            Map.Entry<Long, TerrariumCell> entry = entryIterator.next();
            TerrariumCell cell = entry.getValue();
            if ((heightmap[cell.x][cell.y]<cell.z) && (cell.contents==TerrariumCell.contentsRock || cell.contents==TerrariumCell.contentsClay || cell.contents==TerrariumCell.contentsSand)) {
                heightmap[cell.x][cell.y]=cell.z;
            }
        }
        // TODO normalize z to values between 0 and 255
        BufferedImage newBi = new BufferedImage(heightmap.length, heightmap[0].length, BufferedImage.TYPE_BYTE_GRAY);
        for (int i=0; i<heightmap.length; i++) {
            for (int j=0; j<heightmap[0].length; j++) {
                newBi.setRGB(i, j, GreyscaleARGBConverter.luminanceToArgb(heightmap[i][j]));
            }
        }
        ImageIO.write(newBi, "png", new File(file));
    }

    //kind in expected values, x > 0, y > 0, z > 0
    public static double[][] heightGenerator(int kind, int xDimension, int yDimension, int zDimension) {
        double[][] heightMap;
        double currentHeight;
        heightMap = new double[xDimension][yDimension];
        switch (kind) {
            case soilCone:
                for (int i=0; i<xDimension; i++) {
                    for (int j=1; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor((double) zDimension /3) * (1 + Math.min(0, (double) zDimension /3 - Math.sqrt((((double) xDimension /2) - i) * (((double) xDimension /2) - i) + (((double) yDimension /2) - j) * (((double) yDimension /2) - j))));
                    }
                }
            case soilSpike:
                for (int i=0; i<xDimension; i++) {
                    for (int j=1; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor((double) zDimension/3) * (1 + Math.min(Math.sin((((double) xDimension/2) - i)*0.4), Math.sin((((double) yDimension/2) - j)*0.4)));
                    }
                }
            case soilDome:
                for (int i=0; i<xDimension; i++) {
                    for (int j=1; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor((double) zDimension/3) * (1 + Math.max(Math.sin((((double) xDimension/2) - i)*0.4), Math.sin((((double) yDimension/2) - j)*0.4)));
                    }
                }
            case soilFlat:
                for (int i=0; i<xDimension; i++) {
                    for (int j=0; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor((double) zDimension/3);
                    }
                }
            case soilPitCone:
                for (int i=0; i<xDimension; i++) {
                    for (int j=0; j<yDimension; j++) {
                        for (int k=0; k<zDimension; k++) {
                            heightMap[i][j] = Math.floor((double) zDimension/3) * (2 - Math.min(0, (double) zDimension/3 - Math.sqrt((((double) xDimension/2) - i) * (((double) xDimension/2) - i) + (((double) yDimension/2) - j) * (((double) yDimension/2) - j))));
                        }
                    }
                }
            case soilHill:
                for (int i=0; i<xDimension; i++) {
                    currentHeight = Math.floor(Math.floor((double) zDimension/3) * (1 + (double) i/xDimension));
                    for (int j=0; j<yDimension; j++) {
                        heightMap[i][j] = currentHeight;
                    }
                }
            default: //noisy
                heightMap[0][0] = 2 * Math.floor((double) zDimension/3);
                for (int j=1; j<yDimension; j++) {
                    heightMap[0][j] = Math.min(Math.max(heightMap[0][j - 1] + Math.rint(Math.random()*2 - 1), 0), zDimension);
                }
                for (int i=1; i<xDimension; i++) {
                    heightMap[i][0] = Math.min(Math.max(heightMap[i - 1][0] + Math.rint(Math.random()*2 - 1), 0), zDimension);
                    for (int j=1; j<yDimension; j++) {
                        if (heightMap[i - 1][j]==heightMap[i][j - 1]) {
                            heightMap[i][j] = Math.min(Math.max(heightMap[i - 1][j] + Math.rint(Math.random()*2 - 1), 0), zDimension);
                        } else {
                            heightMap[i][j] = Math.min(Math.max(Math.rint((heightMap[i - 1][j] + heightMap[i][j - 1]) /2), 0), zDimension);
                        }
                    }
                }
        }
        return heightMap;
    }

}
