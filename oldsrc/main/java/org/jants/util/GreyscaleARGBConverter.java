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

package org.jants.util;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GreyscaleARGBConverter {

    /**
     * Convertit une valeur de luminance (0-255) en un entier ARGB 32 bits opaque (255 Alpha).
     * * @param luminance La valeur d'intensité L (0 à 255).
     * @return L'entier ARGB (0xFFLLLLLL).
     */
    public static int luminanceToArgb(int luminance) {
        // 1. Assurer que l'Alpha est maximal (opaque)
        int a = 0xFF;

        // 2. R, G, B sont tous égaux à la luminance
        int r = luminance;
        int g = luminance;
        int b = luminance;

        // 3. Recombiner en utilisant les décalages de bits
        int argb = (a << 24) | (r << 16) | (g << 8) | b;

        return argb;
    }

    public static int argbToGrayscale(int argb) {
        // 1. Extraire les composantes A, R, G, B par décalage de bits (shift) et masque (AND)
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // 2. Calculer la luminance (L) en utilisant la formule pondérée (BT.709)
        // Le résultat doit être arrondi et casté en entier (0-255)
        int luminance = (int) (
                0.2126 * r +
                        0.7152 * g +
                        0.0722 * b
        );

        // 3. Recombiner le pixel ARGB avec R=G=B=Luminance.
        // L'alpha (a) reste le même.
        int grayscaleArgb = (a << 24) | (luminance << 16) | (luminance << 8) | luminance;

        return grayscaleArgb;
    }

    //fast but may not work for some king of images
    public static byte[] toGrayscale(@NotNull byte[] inImg) {
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
    public static BufferedImage toGrayscale(@NotNull BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                gray.getColorModel().getColorSpace(), null);
        op.filter(image, gray);
        return gray;
    }

    public static byte[] toByteArray(@NotNull BufferedImage bi, @NotNull String format)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public static BufferedImage toBufferedImage(@NotNull byte[] bytes)
            throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage bi = ImageIO.read(is);
        return bi;
    }

}
