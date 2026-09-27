/**
 * SwarmForge - WebGPU Client Compute Pipeline
 * Executes 3D pheromone diffusion and agent kinematics acceleration directly in the user browser GPU.
 */
export class WebGpuPheromonePipeline {
  private device: GPUDevice | null = null;
  private pipeline: GPUComputePipeline | null = null;
  private uniformBuffer: GPUBuffer | null = null;
  private inputBuffer: GPUBuffer | null = null;
  private outputBuffer: GPUBuffer | null = null;
  private bindGroup: GPUBindGroup | null = null;

  private width: number;
  private height: number;
  private depth: number;
  private types: number;

  constructor(width: number, height: number, depth: number, types: number = 4) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.types = types;
  }

  public async initialize(shaderCode: string): Promise<boolean> {
    if (!navigator.gpu) {
      console.warn("WebGPU not supported on this browser/platform.");
      return false;
    }

    try {
      const adapter = await navigator.gpu.requestAdapter({ powerPreference: "high-performance" });
      if (!adapter) return false;
      this.device = await adapter.requestDevice();

      const module = this.device.createShaderModule({ code: shaderCode });
      this.pipeline = this.device.createComputePipeline({
        layout: "auto",
        compute: { module, entryPoint: "main" },
      });

      const totalElements = this.width * this.height * this.depth * this.types;
      const bufferSizeBytes = totalElements * Float32Array.BYTES_PER_ELEMENT;

      this.uniformBuffer = this.device.createBuffer({
        size: 32, // 4 u32 + 3 f32
        usage: GPUBufferUsage.UNIFORM | GPUBufferUsage.COPY_DST,
      });

      this.inputBuffer = this.device.createBuffer({
        size: bufferSizeBytes,
        usage: GPUBufferUsage.STORAGE | GPUBufferUsage.COPY_DST | GPUBufferUsage.COPY_SRC,
      });

      this.outputBuffer = this.device.createBuffer({
        size: bufferSizeBytes,
        usage: GPUBufferUsage.STORAGE | GPUBufferUsage.COPY_DST | GPUBufferUsage.COPY_SRC,
      });

      this.bindGroup = this.device.createBindGroup({
        layout: this.pipeline.getBindGroupLayout(0),
        entries: [
          { binding: 0, resource: { buffer: this.uniformBuffer } },
          { binding: 1, resource: { buffer: this.inputBuffer } },
          { binding: 2, resource: { buffer: this.outputBuffer } },
        ],
      });

      return true;
    } catch (err) {
      console.error("Failed to initialize WebGPU Compute Pipeline:", err);
      return false;
    }
  }

  public executeStep(diffusionRate: number, evaporationRate: number, dt: number): void {
    if (!this.device || !this.pipeline || !this.bindGroup || !this.uniformBuffer) return;

    // Update uniforms
    const uniformData = new ArrayBuffer(32);
    const u32View = new Uint32Array(uniformData);
    const f32View = new Float32Array(uniformData);

    u32View[0] = this.width;
    u32View[1] = this.height;
    u32View[2] = this.depth;
    u32View[3] = this.types;
    f32View[4] = diffusionRate;
    f32View[5] = evaporationRate;
    f32View[6] = dt;

    this.device.queue.writeBuffer(this.uniformBuffer, 0, uniformData);

    const commandEncoder = this.device.createCommandEncoder();
    const passEncoder = commandEncoder.beginComputePass();
    passEncoder.setPipeline(this.pipeline);
    passEncoder.setBindGroup(0, this.bindGroup);

    // Dispatch workgroups (8x8x4 workgroup size)
    const workgroupsX = Math.ceil(this.width / 8);
    const workgroupsY = Math.ceil(this.height / 8);
    const workgroupsZ = Math.ceil(this.depth / 4);

    passEncoder.dispatchWorkgroups(workgroupsX, workgroupsY, workgroupsZ);
    passEncoder.end();

    // Ping-pong copy output to input for next step
    if (this.inputBuffer && this.outputBuffer) {
      commandEncoder.copyBufferToBuffer(
        this.outputBuffer, 0,
        this.inputBuffer, 0,
        this.width * this.height * this.depth * this.types * 4
      );
    }

    this.device.queue.submit([commandEncoder.finish()]);
  }
}
