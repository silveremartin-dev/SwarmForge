package org.swarmforge.core.simulation.gpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified Multi-Tier Hardware Compute Dispatcher for SwarmForge.
 *
 * Implements seamless, graceful hardware acceleration escalation:
 * 1. Tier 1: Vulkan 1.2+ SPIR-V Compute Shaders (Windows / Linux / macOS Metal via MoltenVK)
 * 2. Tier 2: OpenCL Compute Kernels via Aparapi (Dedicated GPU / iGPU)
 * 3. Tier 3: Hardware SIMD 256-bit AVX2 / AVX-512 via Java 21 Panama Vector API
 * 4. Tier 4: Multithreaded Pure Java Scalar CPU Fallback
 *
 * Ensures 100% mathematical parity across all compute platforms.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class GpuComputeDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(GpuComputeDispatcher.class);

    public enum ComputeTier {
        VULKAN_SPIRV,
        OPENCL_APARAPI,
        VECTOR_API_SIMD,
        CPU_SCALAR
    }

    private static volatile ComputeTier preferredTier = ComputeTier.VULKAN_SPIRV;
    private static volatile boolean gpuEnabled = true;

    private GpuComputeDispatcher() {
        // Utility / Dispatcher class
    }

    public static void setGpuEnabled(boolean enabled) {
        gpuEnabled = enabled;
        LOG.info("GpuComputeDispatcher: GPU offloading {}", enabled ? "ENABLED" : "DISABLED");
    }

    public static boolean isGpuEnabled() {
        return gpuEnabled;
    }

    public static void setPreferredTier(ComputeTier tier) {
        preferredTier = tier != null ? tier : ComputeTier.VULKAN_SPIRV;
        LOG.info("GpuComputeDispatcher: Preferred compute tier set to {}", preferredTier);
    }

    public static ComputeTier getPreferredTier() {
        return preferredTier;
    }

    /**
     * Dispatches 3D pheromone matrix diffusion and evaporation across the optimal hardware compute tier.
     *
     * @return The actual compute tier utilized for this execution step.
     */
    public static ComputeTier dispatchPheromoneCompute(int width, int height, int depth, int channels,
                                                      float[] inputMatrix, float[] outputMatrix,
                                                      float diffusionRate, float evaporationRate) {
        // 1. Attempt Tier 1: Vulkan SPIR-V Compute Shader
        if (gpuEnabled && (preferredTier == ComputeTier.VULKAN_SPIRV)) {
            if (VulkanComputePipeline.isVulkanAvailable()) {
                boolean success = VulkanComputePipeline.executeComputePass(
                        width, height, depth, channels, inputMatrix, outputMatrix, diffusionRate, evaporationRate
                );
                if (success) {
                    return ComputeTier.VULKAN_SPIRV;
                }
            }
        }

        // 2. Attempt Tier 2: OpenCL via Aparapi
        if (gpuEnabled && (preferredTier == ComputeTier.VULKAN_SPIRV || preferredTier == ComputeTier.OPENCL_APARAPI)) {
            try {
                PheromoneKernel kernel = new PheromoneKernel(
                        width, height, depth, channels, inputMatrix, outputMatrix, diffusionRate, evaporationRate
                );
                kernel.execute(width * height * depth);
                kernel.dispose();
                return ComputeTier.OPENCL_APARAPI;
            } catch (Throwable t) {
                LOG.debug("OpenCL execution unavailable, cascading to SIMD Vector API: {}", t.getMessage());
            }
        }

        // 3. Attempt Tier 3: Hardware SIMD 256-bit Vector API (Panama)
        try {
            PheromoneVectorFallback.processVectorized(
                    width, height, depth, channels, inputMatrix, outputMatrix, diffusionRate, evaporationRate
            );
            return ComputeTier.VECTOR_API_SIMD;
        } catch (Throwable t) {
            LOG.warn("Vector API execution failed, falling back to scalar CPU: {}", t.getMessage());
        }

        // 4. Tier 4: Multithreaded Scalar CPU Fallback
        PheromoneCpuFallback.process(
                width, height, depth, channels, inputMatrix, outputMatrix, diffusionRate, evaporationRate
        );
        return ComputeTier.CPU_SCALAR;
    }
}
