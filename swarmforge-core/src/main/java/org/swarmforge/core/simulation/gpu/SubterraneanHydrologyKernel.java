package org.swarmforge.core.simulation.gpu;

import com.aparapi.Kernel;

/**
 * OpenCL / Aparapi Hardware-Accelerated 3D Finite-Difference PDE Kernel for Subterranean Hydrology and Thermal Dynamics.
 *
 * Solves coupled 3D heat and moisture Laplace diffusion equations across subterranean soil voxel lattices:
 * \[\frac{\partial T}{\partial t} = \alpha \nabla^2 T\]
 * \[\frac{\partial \theta}{\partial t} = K \nabla^2 \theta\]
 *
 * Runs massively parallelized across GPU compute units or SIMD vector CPU workers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class SubterraneanHydrologyKernel extends Kernel {

    private final int width;
    private final int depth;
    private final int height;

    private final float[] currentTemp;
    private final float[] nextTemp;
    private final float[] currentMoisture;
    private final float[] nextMoisture;

    private final float thermalDiffusivity;
    private final float moisturePercolationRate;
    private final float deltaSeconds;

    public SubterraneanHydrologyKernel(int width, int depth, int height,
                                       float[] currentTemp, float[] nextTemp,
                                       float[] currentMoisture, float[] nextMoisture,
                                       float thermalDiffusivity, float moisturePercolationRate,
                                       float deltaSeconds) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.currentTemp = currentTemp;
        this.nextTemp = nextTemp;
        this.currentMoisture = currentMoisture;
        this.nextMoisture = nextMoisture;
        this.thermalDiffusivity = thermalDiffusivity;
        this.moisturePercolationRate = moisturePercolationRate;
        this.deltaSeconds = deltaSeconds;
    }

    @Override
    public void run() {
        int i = getGlobalId();
        int totalCells = width * depth * height;

        if (i < totalCells) {
            int tmp = i;
            int x = tmp % width;
            tmp /= width;
            int y = tmp % depth;
            int z = tmp / depth;

            // Surface boundary cells (z == 0) preserve imposed atmospheric values
            if (z == 0 || x == 0 || x == width - 1 || y == 0 || y == depth - 1 || z == height - 1) {
                nextTemp[i] = currentTemp[i];
                nextMoisture[i] = currentMoisture[i];
                return;
            }

            int sliceSize = width * depth;

            // 6-neighbor 3D stencil indices
            int idxLeft   = i - 1;
            int idxRight  = i + 1;
            int idxFront  = i - width;
            int idxBack   = i + width;
            int idxTop    = i - sliceSize;
            int idxBottom = i + sliceSize;

            float tCenter = currentTemp[i];
            float tempLaplacian = currentTemp[idxLeft] + currentTemp[idxRight]
                                + currentTemp[idxFront] + currentTemp[idxBack]
                                + currentTemp[idxTop] + currentTemp[idxBottom]
                                - 6.0f * tCenter;

            nextTemp[i] = tCenter + thermalDiffusivity * tempLaplacian * deltaSeconds;

            float mCenter = currentMoisture[i];
            float moistLaplacian = currentMoisture[idxLeft] + currentMoisture[idxRight]
                                 + currentMoisture[idxFront] + currentMoisture[idxBack]
                                 + currentMoisture[idxTop] + currentMoisture[idxBottom]
                                 - 6.0f * mCenter;

            float newMoist = mCenter + moisturePercolationRate * moistLaplacian * deltaSeconds;
            // Clamping moisture within physical limits [0.01 (1%), 1.0 (100% saturation)]
            if (newMoist < 0.01f) newMoist = 0.01f;
            if (newMoist > 1.0f)  newMoist = 1.0f;
            nextMoisture[i] = newMoist;
        }
    }
}
