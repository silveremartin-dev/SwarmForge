# SwarmForge Architecture

## 1. System Overview

SwarmForge is a high-performance, GPU-accelerated, distributed multi-agent simulation platform designed for modeling complex eusocial insect colonies, dynamic ecosystems, and emergent swarm intelligence. Built on **Java 21 LTS**, it features a modular, hybrid Entity-Component-System (ECS) engine, GPU compute offloading via TornadoVM, real-time 3D visualization using jMonkeyEngine, and distributed cluster streaming via gRPC and FlatBuffers/Protobuf.

```
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                         SwarmForge Ecosystem                                            │
├─────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                         │
│   ┌───────────────────────────────┐     ┌───────────────────────────────┐     ┌─────────────────────┐   │
│   │   swarmforge-editor           │     │   swarmforge-web              │     │  Headless Node /    │   │
│   │   (JavaFX + jMonkeyEngine 3D) │     │   (Vite / React Dashboard)    │     │  CLI Client         │   │
│   └───────────────┬───────────────┘     └───────────────┬───────────────┘     └──────────┬──────────┘   │
│                   │                                     │                                │              │
│                   └─────────────────────────────────────┼────────────────────────────────┘              │
│                                                         │                                               │
│                                    gRPC (HTTP/2) / WebSockets / REST API                                │
│                                                         │                                               │
│   ┌─────────────────────────────────────────────────────▼───────────────────────────────────────────┐   │
│   │                                      swarmforge-server Cluster                                  │   │
│   │  ┌─────────────────────────┐    ┌──────────────────────────┐    ┌──────────────────────────┐    │   │
│   │  │ Simulation Service Host │    │ JWT Security / Auth      │    │ Metrics & Telemetry      │    │   │
│   │  │ & State Orchestrator    │    │ Interceptors             │    │ Exporter (Prometheus)    │    │   │
│   │  └────────────┬────────────┘    └──────────────────────────┘    └──────────────────────────┘    │   │
│   └───────────────┼─────────────────────────────────────────────────────────────────────────────────┘   │
│                   │                                                                                     │
│        ┌──────────┴───────────────────────────┬──────────────────────────────────┐                      │
│        │                                      │                                  │                      │
│   ┌────▼──────────────────────────────┐  ┌────▼───────────────────────────┐  ┌────▼─────────────────┐   │
│   │  swarmforge-compute               │  │  Persistence Storage          │  │  External APIs      │   │
│   │  (Distributed Worker Nodes        │  │  - PostgreSQL (World / Colony)│  │  - OpenWeatherMap   │   │
│   │   TornadoVM GPU Acceleration)     │  │  - Redis Cache                │  │  - OpenTopography   │   │
│   └───────────────────────────────────┘  └───────────────────────────────┘  └─────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Module Architecture

SwarmForge is organized as a multi-module Maven project adhering to clear separation of concerns:

| Module | Core Responsibility | Key Technologies & Components |
| :--- | :--- | :--- |
| `swarmforge-core` | Simulation engine, domain models, ECS architecture, spatial indexes, ecology & AI behaviors. | Java 21, ECS Engine, Morton3D, 3D Octree, A* Pathfinder, FSM, Fuzzy Logic, RL |
| `swarmforge-server` | Distributed server, orchestration, security, multi-protocol communication, persistence. | gRPC, Protobuf/FlatBuffers, WebSockets, REST, PostgreSQL, Redis, Log4j2 |
| `swarmforge-editor` | Interactive visual studio, real-time 3D viewports, terrain/weather editors, telemetry controls. | JavaFX 21, jMonkeyEngine 3D (jME3), LWJGL3, Custom Mesh Generators, I18n |
| `swarmforge-client` | Desktop client viewer & lightweight simulation runtime launcher. | JavaFX, gRPC Client Stubs |
| `swarmforge-compute` | Headless worker node dedicated to offloading heavy tick workloads & GPU matrix execution. | TornadoVM, OpenCL/CUDA, gRPC Worker Services |
| `swarmforge-plugins` | Dynamic extension system for custom species, behaviors, and environmental disaster modules. | Java ServiceLoader / Plugin API |
| `swarmforge-benchmarks`| Microbenchmarking suite for tick latency, spatial lookups, and serialization throughput. | JMH (Java Microbenchmark Harness) |
| `swarmforge-web` | Web-based monitoring and remote control dashboard. | HTML5/JS/TS, WebSockets, gRPC-Web, Vite |

---

## 3. Core Engine Architecture (`swarmforge-core`)

### 3.1 Entity Component System (ECS) & Hybrid Domain Model (Zero Legacy Policy)
The core engine balances object-oriented domain richness with data-oriented performance:
- **Zero Legacy Policy**: All legacy and obsolete individual models have been eliminated. The canonical domain entity `Individual` implements `AgentView` for zero-cost bridging to the Artemis ECS and rendering engines.
- **ECS Engine**: `World`, `Entity`, `Component` (e.g., `PositionComponent`, `HealthComponent`, `AiComponent`, `EthologyComponent`), `System` (e.g., `MovementSystem`, `AiSystem`, `PheromoneSystem`, `SpatialPartitioningSystem`).
- **Object Pooling**: Managed via `ObjectPool<T>` and `NodePool` to eliminate GC pauses during high-frequency entity spawning and recycling.

### 3.2 Spatial Indexing & Navigation
- **Morton 3D (Z-Order Curve Coding)**: Maps 3D coordinates `(x, y, z)` into 64-bit integer Morton codes for $O(1)$ spatial hashing and cache-coherent cell grouping.
- **3D Octree & Open-Addressing Spatial Partition**: Hierarchical spatial tree and flat hash tables for fast $O(1)$ to $O(\log N)$ range queries, raycasting, perception checks, and collision detection across 100,000+ entities.
- **A* Pathfinder**: Multi-layered 3D grid pathfinding with zero-allocation `NodePool` (ThreadLocal recycling) to eliminate GC overhead during massive concurrent path requests.
- **Simulation Event Bus**: High-throughput asynchronous event pipeline (`EventBus`) with non-blocking disk streaming and decoupled UI/telemetry broadcasting.

### 3.3 Behavioral & AI Systems
- **Finite State Machines (FSM)**: Declarative state transitions (`FSMArchitecture`) governing individual agent behavior cycles (Foraging, Nesting, Defending, Nursing) with multi-rate decision cycles (staggered 3–5 ticks for cognitive/perception tasks, 1 tick for locomotion).
- **Cuticular Hydrocarbon (CHC) Discrimination**: Bray-Curtis dissimilarity metric applied to 32-compound cuticular hydrocarbon chemical signatures for precise nestmate/conspecific/interloper recognition.
- **Arrhenius Thermal Kinetics & Ethology**: Full ECS integration of 220+ ethological capabilities modulated by voxel microclimate temperatures via the Arrhenius $Q_{10} = 2.2$ law and cold torpor triggers.
- **Ecology, Stoichiometry & Symbiosis**: Symbiotic fungus garden (*Atta*, *Macrotermes*) cultivation governed by stoichiometric $C:N$ (22.5:1) dynamics yielding gongylidia/staphylae protein bodies, aphid farming, disease propagation (`DiseaseManager`), predator-prey hunting styles, and inter-colony diplomacy.
- **Nuptial Flight Aerology**: Synchronized alate departure conditioned on real-time meteorological windows (temperature $22\text{--}30^\circ\text{C}$, relative humidity $>70\%$, wind speed $<4.5\,\text{m/s}$, rising barometric pressure trend).

### 3.4 World & Subterranean Thermodynamics
- **Dynamic Terrain & Biomes**: Procedural heightmaps (`BiomeTerrainGenerator`), water tables, soil moisture, and vegetation growth (`VegetationSystem`).
- **Subterranean Microclimate Physics**: 1D Fourier thermal diffusion equation ($\frac{\partial T}{\partial t} = \alpha \frac{\partial^2 T}{\partial z^2}$) modeling soil thermal inertia, Darcy-Weisbach friction head loss ($\Delta h = f \frac{L}{D_h} \frac{v^2}{2g}$) in ventilation tunnels, and buoyancy-driven stack-effect air exchange.
- **Excavation & Mohr-Coulomb Stability**: Dynamic nest architecture generation (`Nest`, `Chamber`, `Tunnel`, `ConstructionManager`) coupled to real-time Mohr-Coulomb shear collapse physics.

---

## 4. Server & Distributed Compute (`swarmforge-server` & `swarmforge-compute`)

### 4.1 Networking & API Layer
- **gRPC & Protobuf / FlatBuffers**: High-efficiency, bidirectional streaming API (`SimulationServiceImpl`, `AuthServiceImpl`, `LeaderboardServiceImpl`, `MatchmakingServiceImpl`) running on Java 21 Virtual Threads. Hardened with 16 MB max frame limits and Gzip stream compression.
- **WebSockets & REST**: Web-compatible real-time event streaming (`SwarmForgeWebSocketServer`) hardened with sliding-window rate limiting (100 msg/sec/client) and connection quotas, paired with REST endpoints (`RestApiServer`).
- **Prometheus Telemetry**: Real-time server performance metrics export (`MetricsServer`, `MetricsExporter`).

### 4.2 Security & Authentication
- **Unified JWT Middleware & BCrypt**: `JwtServerInterceptor`, `JwtUtil`, and `RestApiServer` enforce Bearer JWT authentication, BCrypt password verification, and strict credential checking across both gRPC and REST endpoints.
- **Path-Traversal Protection**: `SimulationSerializer` sanitizes state file paths via `Path.normalize()` and null-byte rejection.

### 4.3 GPU Acceleration (TornadoVM)
- Pheromone grid diffusion and evaporation equations are offloaded to GPU hardware using **TornadoVM** task graphs:
  ```java
  TaskGraph taskGraph = new TaskGraph("pheromones")
      .task("diffuse", PheromoneKernel::diffuse, inputGrid, outputGrid);
  ```
- Transparent fallback to parallel CPU streams when hardware acceleration is unavailable.

### 4.4 Persistence Tier
- **PostgreSQL**: Relational storage for persistent worlds, colony profiles, user credentials, and historical telemetry.
- **H2 In-Memory Database**: Automatic fallback for offline standalone mode without external server dependencies.
- **Redis Cache**: High-speed in-memory store for session states, active leaderboard rankings, and volatile simulation updates.
- **State Checkpointing**: Binary GZIP compressed snapshots (`SimulationCheckpoint`) recording physical grid states and God Mode intervention journals.

---

## 5. Visual Studio & Rendering (`swarmforge-editor`)

- **Dual UI Architecture**: Combines JavaFX desktop controls (`SimulationControlPanel`, `StatisticsDashboard`, `PopulationGraphPane`, `WeatherEditorPane`, `NestGeneratorPane`) with an embedded 3D viewport.
- **jMonkeyEngine 3D Rendering**: Hardware-accelerated 3D viewport featuring level-of-detail management (`LODManager`), procedural terrain rendering (`TerrainMeshGenerator`), pheromone heatmap overlays (`PheromoneVisualizer`), ant mesh instancing (`AntVisualizer`), and subterranean tunnel rendering (`TunnelVisualizer`).
- **Internationalization (I18n)**: Fully localized string management via `I18nManager`.

---

## 6. Simulation Tick Lifecycle

Each simulation tick operates through a deterministic pipeline:

```mermaid
graph TD
    A[Tick Trigger] --> B[Climate & Weather Update]
    B --> C[Environment & Water Table Cycle]
    C --> D[Pheromone Diffusion & Decay Kernel]
    D --> E[Biological & Ecological Systems Update]
    E --> F[ECS Systems Execution - Arrhenius Q10]
    F --> G[FSM & Behavior Strategy Execution]
    G --> H[Spatial Partition & Octree Rebuild]
    H --> I[Event & Telemetry Streaming gRPC/WebSocket]
    I --> J[Async Checkpoint Persistence If Scheduled]
```

---

## 7. Performance Objectives & Scaling Benchmarks

| Parameter | Target | Achieved / Design Capacity |
| :--- | :--- | :--- |
| **Simulated Entities** | 1,000,000+ Active Agents | Verified up to 1,000,000 entities with SpatialHashMap + Virtual Threads |
| **World Dimensions** | 1,000m × 1,000m × 100m | Supported with sparse Morton3D spatial maps |
| **Tick Execution Rate**| 60 TPS (Ticks Per Second) | Sustained up to 500 agents on CPU 4-cores (1,131–2,333 TPS @ 100 ants, 70–96 TPS @ 500 ants) |
| **Supercolony Scale**  | 1,000,000 Agents | 0.95 TPS (~1s/tick) on CPU, scalable to >60 TPS with GPU compute nodes |
| **Streaming Latency**  | < 50 ms | Achieved via gRPC HTTP/2 bidirectional streams on Virtual Threads |

> 📖 For comprehensive benchmark metrics from 100 to 1,000,000 agents, consult [BENCHMARKS.md](BENCHMARKS.md) and [BENCHMARK_RESULTS.md](BENCHMARK_RESULTS.md).

---

## 8. Infrastructure & Containerization

- **Docker & Compose**: Production containerized multi-service configuration (`Dockerfile`, `docker-compose.yml`, `envoy.yaml`) packaging SwarmForge Server, Envoy Proxy, PostgreSQL, Redis, Compute Node, and Web UI.
- **Kubernetes**: Helm deployment charts available in `charts/` for scalable cluster orchestration.
