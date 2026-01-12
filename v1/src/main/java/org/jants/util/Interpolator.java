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

// modified after
// https://icy.bioimageanalysis.org/doc/src-html/icy/math/Interpolator.html
// to implement splines
public class Interpolator {

    private static double[] prepareYInterpolation(double[] x, double[] y, double xinc) {
        if ((x.length == 0) || (y.length == 0))
            throw new IllegalArgumentException("x[] and y[] should not be empty.");
        if (x.length != y.length)
            throw new IllegalArgumentException("x[] and y[] should have the same length.");
        if (xinc == 0)
            throw new IllegalArgumentException("step must be > 0");

        return new double[(int) ((x[x.length - 1] - x[0]) / xinc) + 1];
    }

    /**
     * Return Y linear interpolated coordinates from specified points and given X increment
     */
    public static double[] doYLinearInterpolation(double[] x, double[] y, double xinc) {
        final double[] result = prepareYInterpolation(x, y, xinc);
        final int len = result.length;
        if (len == 1)
            result[0] = x[0];
        else {
            final int xlen = x.length - 1;
            int index = 0;
            int offset = 0;
            double xvalue = x[0];
            double yvalue = y[0];
            double yinc = 0;

            while (offset < len) {
                while ((index < xlen) && (xvalue >= x[index])) {
                    index++;
                    final double dx = x[index] - xvalue;

                    if (dx != 0)
                        yinc = (y[index] - yvalue) / dx;
                    else
                        yinc = 0;
                }

                result[offset++] = yvalue;
                yvalue += yinc;
                xvalue += xinc;
            }
        }

        return result;
    }

    /**
     * Return Y spline interpolated coordinates from specified points and given X increment.<br>
     * Not yet implemented !
     */
    public static double[] doYSplineInterpolation(double[] x, double[] y, double xstep) {
        final double[] result = prepareYInterpolation(x, y, xstep);
        final int len = result.length;
        if (len > 1) {
            //https://en.wikipedia.org/wiki/Spline_interpolation
            //TODO
        }

        return result;
    }

    /**
     * Do linear interpolation from start to end with specified increment step
     */
    public static double[] doLinearInterpolation(double start, double end, double step) {
        int size;

        if (step == 0)
            size = 1;
        else
            size = (int) ((end - start) / step) + 1;

        // size should be at least 1
        if (size < 1)
            size = 1;

        final double[] result = new double[size];

        double value = start;
        for (int i = 0; i < size; i++) {
            result[i] = value;
            value += step;
        }

        return result;
    }

    /**
     * Do linear interpolation from start to end with specified size (step number)
     */
    public static double[] doLinearInterpolation(double start, double end, int size) {
        if (size < 1)
            return null;

        // special case
        if (size == 1) {
            final double[] result = new double[size];
            result[0] = end;
            return result;
        }

        return doLinearInterpolation(start, end, (end - start) / (size - 1));
    }

    //https://icy.bioimageanalysis.org/doc/src-html/icy/math/Scaler.html
    //TODO : fix

    /**
     * Do logarithmic interpolation from start to end with specified size (step number)
     */
    public double[] doLogInterpolation(double start, double end, int size) {
        // get linear interpolation
        final double[] result = doLinearInterpolation(start, end, size);

        // define input and output scaler
        final Scaler scalerIn = new Scaler(start, end, 2, 20, true, true);
        final Scaler scalerOut = new Scaler(Math.log(2), Math.log(20), start, end, true, true);

        final int len = result.length;

        // log scaling
        for (int i = 0; i < len; i++)
            result[i] = scalerOut.scale(Math.log(scalerIn.scale(result[i])));

        return result;
    }

    /**
     * Do exponential interpolation from start to end with specified size (step number)
     */
    public double[] doExpInterpolation(double start, double end, int size) {
        // get linear interpolation
        final double[] result = doLinearInterpolation(start, end, size);

        // define input and output scaler
        final Scaler scalerIn = new Scaler(start, end, 0, 2, false, true);
        final Scaler scalerOut = new Scaler(Math.exp(0), Math.exp(2), start, end, false, true);

        final int len = result.length;

        // exp scaling
        for (int i = 0; i < len; i++)
            result[i] = scalerOut.scale(Math.exp(scalerIn.scale(result[i])));

        return result;
    }

    /**
     * https://icy.bioimageanalysis.org/doc/src-html/icy/math/Scaler.html
     */
    private class Scaler {
        private double absLeftIn;
        private double absRightIn;

        private double leftIn;
        private double rightIn;

        private double leftOut;
        private double rightOut;

        private double scaler;
        private double unscaler;

        private boolean integerData;
        private boolean canCross;
        private boolean crossed;

        public Scaler(double leftIn, double rightIn, double leftOut, double rightOut, boolean integerData, boolean canCross) {
            this(leftIn, rightIn, leftIn, rightIn, leftOut, rightOut, integerData, canCross);
        }

        public Scaler(double absLeftIn, double absRightIn, double leftIn, double rightIn, double leftOut, double rightOut,
                      boolean integerData, boolean canCross) {
            this.absLeftIn = absLeftIn;
            this.absRightIn = absRightIn;
            this.leftIn = leftIn;
            this.rightIn = rightIn;
            this.leftOut = leftOut;
            this.rightOut = rightOut;
            this.integerData = integerData;
            this.canCross = canCross;

            crossed = absLeftIn > absRightIn;

            if (crossed && !canCross)
                throw new IllegalArgumentException("Can't create scaler : left > right and canCross = false");

            final double deltaIn = rightIn - leftIn;
            final double deltaOut = rightOut - leftOut;

            // delta null
            if ((deltaIn == 0) || (deltaOut == 0))
                scaler = 1;
            else {
                scaler = deltaOut / deltaIn;
                unscaler = deltaIn / deltaOut;
            }

        }

        /**
         * Scale the value
         *
         * @param value value to scale
         * @return scaled output value
         */
        public double scale(double value) {
            if (crossed) {
                if (value >= leftIn)
                    return leftOut;
                else if (value <= rightIn)
                    return rightOut;
                else
                    return ((value - leftIn) * scaler) + leftOut;
            }

            if (value <= leftIn)
                return leftOut;
            else if (value >= rightIn)
                return rightOut;
            else
                return ((value - leftIn) * scaler) + leftOut;
        }
    }
}