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
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.nio.Buffer;

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
    public void saveMap(@NotNull String file, @NotNull Terrarium map) throws IOException {
        BufferedImage newBi = new BufferedImage(map.heightmap.length, map.heightmap[0].length, BufferedImage.TYPE_BYTE_GRAY);
        for (int i=0; i<map.heightmap.length; i++) {
            for (int j=0; j<map.heightmap[0].length; j++) {
                newBi.setRGB(i, j, map.heightmap[i][j]);
            }
        }
        ImageIO.write(newBi, "png", new File(file));
    }

    //fast but may not work for some king of images
    private static byte[] toGrayscale(@NotNull byte[] inImg) {
        int r, g, b;
        int grayscale;
        byte[] outImgByte;
        outImgByte = new byte[inImg.length];
        for (int i = 0; i < inImg.length; i++) {
            r = (inImg[i] >> 16) & 0xff;
            g = (inImg[i] >> 8) & 0xff;
            b = (inImg[i]) & 0xff;
            grayscale = (r + g + b) / 3;
            outImgByte[i] = (byte) (grayscale << 16 | grayscale << 8 | grayscale);
        }
        return outImgByte;
    }

    //the (slow but) correct way
    private static BufferedImage toGrayscale(@NotNull BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                gray.getColorModel().getColorSpace(), null);
        op.filter(image, gray);
        return gray;
    }

    private static byte[] toByteArray(@NotNull BufferedImage bi, @NotNull String format)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    private static BufferedImage toBufferedImage(@NotNull byte[] bytes)
            throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage bi = ImageIO.read(is);
        return bi;
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
                        heightMap[i][j] = Math.floor(zDimension/3) * (1 + Math.min(0, zDimension/3 - Math.sqrt(((xDimension/2) - i) * ((xDimension/2) - i) + ((yDimension/2) - j) * ((yDimension/2) - j))));
                    }
                }
            case soilSpike:
                for (int i=0; i<xDimension; i++) {
                    for (int j=1; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor(zDimension/3) * (1 + Math.min(Math.sin(((xDimension/2) - i)*0.4), Math.sin(((yDimension/2) - j)*0.4));
                    }
                }
            case soilDome:
                for (int i=0; i<xDimension; i++) {
                    for (int j=1; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor(zDimension/3) * (1 + Math.max(Math.sin(((xDimension/2) - i)*0.4), Math.sin(((yDimension/2) - j)*0.4));
                    }
                }
            case soilFlat:
                for (int i=0; i<xDimension; i++) {
                    for (int j=0; j<yDimension; j++) {
                        heightMap[i][j] = Math.floor(zDimension/3);
                    }
                }
            case soilPitCone:
                for (int i=0; i<xDimension; i++) {
                    for (int j=0; j<yDimension; j++) {
                        for (int k=0; k<zDimension; k++) {
                            heightMap[i][j] = Math.floor(zDimension/3) * (2 - Math.min(0, zDimension/3 - Math.sqrt(((xDimension/2) - i) * ((xDimension/2) - i) + ((yDimension/2) - j) * ((yDimension/2) - j))));
                        }
                    }
                }
            case soilHill:
                for (int i=0; i<xDimension; i++) {
                    currentHeight = Math.floor(Math.floor(zDimension/3) * (1 + i/xDimension));
                    for (int j=0; j<yDimension; j++) {
                        heightMap[i][j] = currentHeight;
                    }
                }
            default: //noisy
                heightMap[0][0] = 2 * Math.floor(zDimension/3);
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
