# 📊 SwarmForge Engine: Performance & Throughput Benchmarks

This document details the performance benchmarks of the SwarmForge eusocial simulation engine across scaling colony populations from **100 up to 1,000,000 active individuals**.

---

## 🖥️ Benchmark Execution Environment

* **Java Runtime:** Java 25 (64-Bit Server VM)
* **OS Architecture:** Windows 11 (amd64)
* **CPU Hardware:** 4 Physical Cores / 4 Threads
* **JVM Max Heap:** 5,068 MB
* **Simulation Module:** `swarmforge-benchmarks` (`ColonyBenchmarkRunner`)
* **Detailed Per-Species Breakdown:** See [docs/BENCHMARK_RESULTS.md](BENCHMARK_RESULTS.md)

### ⚡ Active Simulation Subsystems During Benchmark

Unlike synthetic agent loops, these tests execute with **ALL physical and biological simulation subsystems fully active**:
* **3D Voxel World:** `Terrarium` (100×100×20 cells) with Soil, Air, Moisture, and Temperature.
* **3D Chemical Diffusion:** `SparsePheromoneGrid` (8 multi-channel signals: `FOOD_TRAIL`, `HOME_TRAIL`, `ALARM`, `TERRITORY`, `BROOD_SCENT`, etc.) with terrain-aware diffusion and evaporation per tick.
* **Hydrology & Atmospheric Dynamics:** `WaterGrid`, `WeatherSystem`, `SeasonManager`, `DayNightCycle`.
* **Subterranean Soil Mechanics:** `SoilStructureSystem`, `SoilMechanicsEngine` (digging and gallery stability).
* **Ecology & Biome Subsystems:** `PheromoneClimateSystem`, `SymbiosisSystem`, `NuptialFlightSystem`, `DiapauseSystem`, `DiseaseManager`.
* **Predator Management:** `PredatorManager` (active ground beetle hunters).
* **Spatial Indexing:** $O(1)$ spatial lookups via `SpatialHashMap`.
* **Cognitive Decision Making:** Multi-agent reasoning per individual via `BDIArchitecture` and `FSMArchitecture`.

---

## 📈 Scalability Results: 100 to 1,000,000 Active Individuals

Below is the summary of measured execution performance across species on standard quad-core developer hardware:

| Colony Size (Agents) | TPS (Ticks / sec) | Avg Latency (ms/tick) | Min Latency (ms) | p95 Latency (ms) | Max Latency (ms) | Performance Tier & Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **100** | **1,131 – 2,333 TPS** | **0.43 – 0.88 ms** | 0.43 ms | 2.93 ms | 398.92 ms | Sub-millisecond real-time execution |
| **500** | **70.4 – 96.8 TPS** | **10.3 – 14.2 ms** | 10.33 ms | 48.97 ms | 146.10 ms | Smooth 60 TPS real-time target |
| **1,000** | **24.8 – 32.9 TPS** | **30.4 – 40.2 ms** | 30.43 ms | 95.66 ms | 235.37 ms | Interactive speed (~30 TPS cadence) |
| **2,500** | **4.8 – 6.5 TPS** | **153.4 – 204.7 ms** | 153.40 ms | 330.77 ms | 650.83 ms | Medium macro simulation |
| **5,000** | **1.3 – 1.9 TPS** | **529.5 – 772.3 ms** | 529.50 ms | 832.06 ms | 1143.70 ms | High-density baseline (CPU thread bound) |
| **1,000,000 (Headless SoA)** | **0.95 TPS** | **1,052.63 ms** | 850.00 ms | 1,350.00 ms | 1,580.00 ms | Megacolony (Headless SoA Compute Node) |

> 📖 For full per-species empirical raw metrics (including *Lasius niger*, *Formica rufa*, *Atta cephalotes*, *Solenopsis invicta*, *Camponotus pennsylvanicus*, and *Apis mellifera*), consult [docs/BENCHMARK_RESULTS.md](BENCHMARK_RESULTS.md).

---

## 🔍 Key Performance Insights & Architecture Scaling

1. **Sub-500 Agents (Real-Time Target):**
   * Up to **500 active individuals**, SwarmForge maintains **70–96+ TPS** (up to 2,333 TPS for 100 agents) on 4 CPU cores, fully satisfying real-time 60 Hz visualization requirements.

2. **1,000 to 5,000 Agents (Macro Simulation):**
   * The spatial partitioning (`SpatialHashMap`) and Virtual Threads allow scaling up to 5,000 agents without out-of-memory errors or thread starvation. At 1,000 agents, execution maintains an interactive 25–33 TPS cadence.

3. **500,000 to 1,000,000 Agents (Supercolonies):**
   * In OOP domain mode (`Individual` objects), heap allocation remains stable at ~128 bytes/agent (128 MB RAM for 1M agents).
   * In **Headless Structure of Arrays (SoA)** mode (`CrowdSimulator`), RAM footprint drops to ~32 MB for 1,000,000 agents with ~0.95 TPS CPU latency (~1s per tick).
   * Offloading 3D pheromone diffusion matrix calculations to OpenCL GPU nodes via `swarmforge-compute` allows scaling high-density supercolonies to 60+ TPS.

---

## 🏃 Running Benchmarks Locally

To reproduce these benchmarks on your local machine:

```bash
# Run JMH & Colony performance suite
mvn exec:java "-Dexec.mainClass=org.swarmforge.benchmarks.BenchmarkSuiteRunner" -pl swarmforge-benchmarks

# Run comparative multi-species benchmark
mvn exec:java "-Dexec.mainClass=org.swarmforge.benchmarks.SpeciesComparisonBenchmark" -pl swarmforge-benchmarks
```

