/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.gpu;

import java.util.logging.Logger;

/**
 * GPU compute executor that manages kernel execution.
 * Automatically falls back to CPU if GPU is unavailable.
 * 
 * In production, this would integrate with TornadoVM like:
 * 
 * <pre>
 * TaskGraph taskGraph = new TaskGraph("pheromones")
 *         .transferToDevice(DataTransferMode.FIRST_EXECUTION, input)
 *         .task("diffuse", PheromoneKernel::diffuseFlat, input, output, w, h, d, t)
 *         .transferToHost(DataTransferMode.EVERY_EXECUTION, output);
 * </pre>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GpuExecutor {

    private static final Logger LOG = Logger.getLogger(GpuExecutor.class.getName());

    private final boolean gpuAvailable;
    private final String deviceName;

    public GpuExecutor() {
        // Check for GPU availability (simplified - real impl would probe TornadoVM)
        this.gpuAvailable = checkGpuAvailability();
        this.deviceName = gpuAvailable ? detectDevice() : "CPU Fallback";
        LOG.info("GpuExecutor initialized: " + deviceName);
    }

    private boolean checkGpuAvailability() {
        // In production: TornadoRuntime.getTornadoRuntime().getDefaultDevice() != null
        // For now, always use CPU fallback
        return false;
    }

    private String detectDevice() {
        // In production: query TornadoVM for device info
        return "GPU Device";
    }

    /**
     * Execute pheromone diffusion.
     */
    public void executePheromone(
            float[] input, float[] output,
            int width, int height, int depth, int types) {

        if (gpuAvailable) {
            // GPU path (TornadoVM)
            executePheromonesGpu(input, output, width, height, depth, types);
        } else {
            // CPU fallback
            PheromoneKernel.diffuseFlat(input, output, width, height, depth, types);
        }
    }

    /**
     * Execute spatial neighbor search.
     */
    public void executeFindNeighbors(
            float[] positions, float targetX, float targetY, float targetZ,
            float radius, float[] results, int count) {

        if (gpuAvailable) {
            executeFindNeighborsGpu(positions, targetX, targetY, targetZ, radius, results, count);
        } else {
            SpatialKernel.findNeighborsCPU(positions, targetX, targetY, targetZ, radius, results, count);
        }
    }

    /**
     * Execute position updates for all individuals.
     */
    public void executePositionUpdate(
            float[] positions, float[] headings, float[] speeds, int count) {

        if (gpuAvailable) {
            executePositionUpdateGpu(positions, headings, speeds, count);
        } else {
            SpatialKernel.updatePositionsCPU(positions, headings, speeds, count);
        }
    }

    /**
     * Execute gradient calculation for pheromone following.
     */
    public void executeGradientCalc(
            float[] positions, float[] pheromones, float[] gradients,
            int width, int height, int depth, int pheromoneType, int count) {

        if (gpuAvailable) {
            executeGradientCalcGpu(positions, pheromones, gradients, width, height, depth, pheromoneType, count);
        } else {
            SpatialKernel.calculateGradientsCPU(positions, pheromones, gradients, width, height, depth, pheromoneType,
                    count);
        }
    }

    // GPU implementations (stubs for TornadoVM integration)
    private void executePheromonesGpu(float[] input, float[] output, int w, int h, int d, int t) {
        // TornadoVM TaskGraph execution would go here
        LOG.fine("GPU pheromone diffusion: " + (w * h * d * t) + " cells");
        PheromoneKernel.diffuseFlat(input, output, w, h, d, t);
    }

    private void executeFindNeighborsGpu(float[] pos, float tx, float ty, float tz, float r, float[] res, int c) {
        LOG.fine("GPU neighbor search: " + c + " individuals");
        SpatialKernel.findNeighborsCPU(pos, tx, ty, tz, r, res, c);
    }

    private void executePositionUpdateGpu(float[] pos, float[] head, float[] spd, int c) {
        LOG.fine("GPU position update: " + c + " individuals");
        SpatialKernel.updatePositionsCPU(pos, head, spd, c);
    }

    private void executeGradientCalcGpu(float[] pos, float[] pher, float[] grad, int w, int h, int d, int pt, int c) {
        LOG.fine("GPU gradient calc: " + c + " individuals");
        SpatialKernel.calculateGradientsCPU(pos, pher, grad, w, h, d, pt, c);
    }

    public boolean isGpuAvailable() {
        return gpuAvailable;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
