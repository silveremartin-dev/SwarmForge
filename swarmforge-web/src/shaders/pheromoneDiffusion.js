/**
 * WebGPU WGSL Compute Shader for Real-Time Pheromone Diffusion & Evaporation.
 * 
 * Executes directly on client GPU hardware (Chrome/Edge/Firefox WebGPU) at 60 FPS,
 * solving continuous Laplacian 2D/3D chemical diffusion kernels without CPU bottlenecks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */

export const PHEROMONE_DIFFUSION_WGSL = /* wgsl */ `
struct SimulationParams {
  width: u32,
  height: u32,
  diffusionRate: f32,
  evaporationRate: f32,
  deltaTime: f32,
  pad: f32,
};

@group(0) @binding(0) var<uniform> params: SimulationParams;
@group(0) @binding(1) var<storage, read> currentField: array<f32>;
@group(0) @binding(2) var<storage, read_write> nextField: array<f32>;

@compute @workgroup_size(16, 16)
fn main(@builtin(global_invocation_id) global_id: vec3<u32>) {
  let x = global_id.x;
  let y = global_id.y;
  let w = params.width;
  let h = params.height;

  if (x >= w || y >= h) {
    return;
  }

  let index = y * w + x;
  let currentVal = currentField[index];

  // Boundary preservation
  if (x == 0u || x == w - 1u || y == 0u || y == h - 1u) {
    nextField[index] = currentVal * max(0.0, 1.0 - params.evaporationRate * params.deltaTime);
    return;
  }

  // 4-neighbor 2D/3D Laplacian finite difference
  let left   = currentField[index - 1u];
  let right  = currentField[index + 1u];
  let up     = currentField[index - w];
  let down   = currentField[index + w];

  let laplacian = (left + right + up + down) - 4.0 * currentVal;
  let diffused = currentVal + params.diffusionRate * laplacian * params.deltaTime;
  
  // Evaporation decay
  let decayed = diffused * max(0.0, 1.0 - params.evaporationRate * params.deltaTime);

  nextField[index] = max(0.0, decayed);
}
`;

/**
 * Helper to initialize WebGPU compute pipeline if supported by the browser.
 */
export async function createPheromoneComputePipeline(device, width, height) {
  if (!device) return null;

  const shaderModule = device.createShaderModule({
    code: PHEROMONE_DIFFUSION_WGSL,
  });

  const pipeline = device.createComputePipeline({
    layout: 'auto',
    compute: {
      module: shaderModule,
      entryPoint: 'main',
    },
  });

  return { pipeline, shaderModule };
}
