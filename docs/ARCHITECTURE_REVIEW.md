# SwarmForge - Architecture & Technical Design Audit Report

## 1. System Overview & Module Boundaries

SwarmForge is organized as a multi-module Maven project implementing a decoupled, event-driven multi-agent simulation architecture:

```
SwarmForge (Parent POM)
├── swarmforge-core       : Core domain, simulation tick loop, ECS, spatial partitioning, genetics, weather
├── swarmforge-server     : gRPC microservices, Netty server, Redis/PostgreSQL/H2 persistence, JWT auth
├── swarmforge-editor     : JavaFX 21 & jMonkeyEngine 3.6 3D editor studio, audio synthesis, visual panels
├── swarmforge-client     : Lightweight client SDK & gRPC stubs
├── swarmforge-compute    : Distributed worker compute nodes for multi-machine scaling
├── swarmforge-plugins    : ServiceProvider (SPI) plugin extensibility system
└── swarmforge-benchmarks : JMH benchmarks for tick latency and spatial query throughput
```

## 2. Key Architectural Patterns

1. **Spatial Indexing & Concurrency:**
   - Spatial queries utilize `SpatialHashMap` and Octree structures achieving $O(1)$ spatial lookup performance.
   - Core tick loop leverages Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`) for massive agent parallelism.
2. **GPU Matrix Computations:**
   - 3D pheromone decay, evaporation, and gradient diffusion are offloaded to OpenCL GPU kernels via Aparapi.
3. **Decoupled Client-Server Communication:**
   - Low-latency gRPC protocol buffer streams bind the simulation server with the 3D JavaFX/jME3 editor studio.
4. **Resilient Serialization:**
   - Jackson `ObjectMapper` configured with `@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)` and `FAIL_ON_UNKNOWN_PROPERTIES=false` to prevent class-loading vulnerabilities and legacy preset deserialization breaks.

## 3. Evaluation & Recommendations

- **Module Isolation:** `swarmforge-core` has zero UI or heavy rendering dependencies.
- **Service Locators:** UI components in `swarmforge-editor` cleanly bind to domain events via reactive property bindings and listener patterns.
