package org.swarmforge.core.simulation.gpu;

import com.aparapi.Kernel;

/**
 * OpenCL Kernel for parallel pheromone updates (Diffusion + Decay).
 * 
 * Uses flattened 1D arrays to represent the 3D grid for GPU compatibility.
 * Z-slices are laid out sequentially.
 */
public class PheromoneKernel extends Kernel {

    // Dimensions
    private final int width;
    private final int height;
    private final int depth;
    private final int pheromoneTypes;

    // Data - flattened arrays [x + y*width + z*width*height] * types
    // Actually, easier to handle Types as separate arrays or interleaved?
    // Let's interleave: index = (x + y*width + z*width*height) * types + typeId
    private final float[] pheromones;
    private final float[] newPheromones;

    // Parameters
    private final float diffusionRate;
    private final float evaporationRate;

    public PheromoneKernel(int w, int h, int d, int types, float[] matrix, float[] resultMatrix, float diff,
            float evap) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.pheromoneTypes = types;
        this.pheromones = matrix;
        this.newPheromones = resultMatrix;
        this.diffusionRate = diff;
        this.evaporationRate = evap;
    }

    @Override
    public void run() {
        int i = getGlobalId();

        // i represents a single cell * types block?
        // Best to parallelize over cells (width * height * depth)
        // Global Id range should be (W * H * D)

        int totalCells = width * height * depth;

        if (i < totalCells) {
            // Decode 3D coords
            int tmp = i;
            int x = tmp % width;
            tmp /= width;
            int y = tmp % height;
            int z = tmp / height;

            // For each pheromone type
            for (int t = 0; t < pheromoneTypes; t++) {
                int index = (i * pheromoneTypes) + t;

                float currentVal = pheromones[index];

                // Diffusion: average of neighbors
                // Simplified 6-neighbor check
                float sum = 0;
                int count = 0;

                // x-1
                if (x > 0) {
                    sum += pheromones[((i - 1) * pheromoneTypes) + t];
                    count++;
                }
                // x+1
                if (x < width - 1) {
                    sum += pheromones[((i + 1) * pheromoneTypes) + t];
                    count++;
                }
                // y-1
                if (y > 0) {
                    sum += pheromones[((i - width) * pheromoneTypes) + t];
                    count++;
                }
                // y+1
                if (y < height - 1) {
                    sum += pheromones[((i + width) * pheromoneTypes) + t];
                    count++;
                }
                // z-1 (i - w*h)
                if (z > 0) {
                    sum += pheromones[((i - width * height) * pheromoneTypes) + t];
                    count++;
                }
                // z+1
                if (z < depth - 1) {
                    sum += pheromones[((i + width * height) * pheromoneTypes) + t];
                    count++;
                }

                // Apply diffusion formula: new = current + rate * (avg_neighbors - current)
                // Or simplified: (1-rate)*current + rate*avg
                float avg = (count > 0) ? sum / count : 0;
                float diffused = (1.0f - diffusionRate) * currentVal + diffusionRate * avg;

                // Apply Decay
                newPheromones[index] = Math.max(0, diffused - evaporationRate);
            }
        }
    }
}
