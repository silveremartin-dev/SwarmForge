# SwarmForge Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SwarmForge Architecture                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐       │
│  │  JavaFX Client  │     │   Web Client    │     │  Headless Node  │       │
│  └────────┬────────┘     └────────┬────────┘     └────────┬────────┘       │
│           │                       │                       │                 │
│           └───────────────────────┴───────────────────────┘                 │
│                                   │                                         │
│                          gRPC + FlatBuffers                                 │
│                                   │                                         │
│  ┌────────────────────────────────┴────────────────────────────────┐       │
│  │                    SwarmForge Server Cluster                     │       │
│  │    ┌──────────┐    ┌──────────┐    ┌──────────┐                │       │
│  │    │  GPU     │◄──►│  Redis   │◄──►│   GPU    │                │       │
│  │    │  Node    │    │  Cluster │    │   Node   │                │       │
│  │    └──────────┘    └──────────┘    └──────────┘                │       │
│  └─────────────────────────────┬───────────────────────────────────┘       │
│                                │                                            │
│                       ┌────────▼────────┐                                  │
│                       │   PostgreSQL    │                                  │
│                       └─────────────────┘                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Modules

| Module | Purpose |
|--------|---------|
| `swarmforge-core` | Domain models, simulation engine, spatial data structures |
| `swarmforge-server` | GPU compute, gRPC services, database persistence |
| `swarmforge-client-javafx` | 3D rendering, real-time streaming, UI controls |
| `swarmforge-plugins` | Species definitions, behavior strategies |

## Key Data Structures

### Morton Encoding (Z-order Curve)

```java
long mortonCode = Morton3D.encode(x, y, z);
cells.put(mortonCode, cell);  // ConcurrentHashMap
```

- O(1) lookup, sparse storage, GPU-friendly

### Simulation Cycle

1. Climate Update → 2. Environment Update → 3. Pheromone Diffusion (GPU) → 4. Individual Update → 5. Behavior Execution

## GPU Acceleration (TornadoVM)

```java
TaskGraph taskGraph = new TaskGraph("pheromones")
    .task("diffuse", PheromoneKernel::diffuse, inputCells, outputCells);
executor.execute();
```

- CPU fallback for systems without GPU

## Communication

- **gRPC bidirectional streaming** for real-time updates
- **FlatBuffers** for zero-copy serialization
- **Delta updates** to minimize bandwidth

## Performance Targets

| Metric | Target |
|--------|--------|
| Individuals | 100,000+ |
| World Size | 1000m × 1000m × 100m |
| Tick Rate | 60 TPS |
| Latency | < 50ms |
