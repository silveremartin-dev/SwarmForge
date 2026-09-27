// SwarmForge - WebGPU WGSL Pheromone Compute Shader
// Solves 3D Laplacien diffusion, decay and evaporation equations in parallel workgroups on the client GPU

struct SimParams {
    width: u32,
    height: u32,
    depth: u32,
    pheromoneTypes: u32,
    diffusionRate: f32,
    evaporationRate: f32,
    dt: f32,
};

@group(0) @binding(0) var<uniform> params: SimParams;
@group(0) @binding(1) var<storage, read> inputGrid: array<f32>;
@group(0) @binding(2) var<storage, read_write> outputGrid: array<f32>;

fn getIndex(x: u32, y: u32, z: u32, t: u32) -> u32 {
    return ((x + (y * params.width) + (z * params.width * params.height)) * params.pheromoneTypes) + t;
}

@compute @workgroup_size(8, 8, 4)
fn main(@builtin(global_invocation_id) global_id: vec3<u32>) {
    let x = global_id.x;
    let y = global_id.y;
    let z = global_id.z;

    if (x >= params.width || y >= params.height || z >= params.depth) {
        return;
    }

    for (var t: u32 = 0u; t < params.pheromoneTypes; t = t + 1u) {
        let centerIdx = getIndex(x, y, z, t);
        let centerVal = inputGrid[centerIdx];

        var neighborSum: f32 = 0.0;
        var count: f32 = 0.0;

        // 6-Neighbor 3D stencil
        if (x > 0u) { neighborSum = neighborSum + inputGrid[getIndex(x - 1u, y, z, t)]; count = count + 1.0; }
        if (x + 1u < params.width) { neighborSum = neighborSum + inputGrid[getIndex(x + 1u, y, z, t)]; count = count + 1.0; }

        if (y > 0u) { neighborSum = neighborSum + inputGrid[getIndex(x, y - 1u, z, t)]; count = count + 1.0; }
        if (y + 1u < params.height) { neighborSum = neighborSum + inputGrid[getIndex(x, y + 1u, z, t)]; count = count + 1.0; }

        if (z > 0u) { neighborSum = neighborSum + inputGrid[getIndex(x, y, z - 1u, t)]; count = count + 1.0; }
        if (z + 1u < params.depth) { neighborSum = neighborSum + inputGrid[getIndex(x, y, z + 1u, t)]; count = count + 1.0; }

        let laplacian = (neighborSum - (count * centerVal));
        let diffused = centerVal + (params.diffusionRate * laplacian * params.dt);
        let evaporated = diffused * (1.0 - (params.evaporationRate * params.dt));

        outputGrid[centerIdx] = max(0.0, evaporated);
    }
}
