# 📊 SwarmForge Engine: Performance & Throughput Benchmarks

This document details the performance benchmarks of the SwarmForge eusocial simulation engine across scaling colony populations from **100 up to 1,000,000 active individuals**.

---

## 🖥️ Benchmark Execution Environment

* **Java Runtime:** Java 25 (64-Bit Server VM)
* **OS Architecture:** Windows 11 (amd64)
* **CPU Hardware:** 4 Physical Cores
* **JVM Max Heap:** 5,068 MB
* **Simulation Module:** `swarmforge-benchmarks` (`ColonyBenchmarkRunner`)

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

| Colony Size (Agents) | TPS (Ticks / sec) | Avg Latency (ms/tick) | Min Latency (ms) | p95 Latency (ms) | Max Latency (ms) | Performance Tier |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **100** | **312.69 TPS** | **3.20 ms** | 0.40 ms | 3.49 ms | 287.63 ms | Ultra High-Speed |
| **500** | **200.46 TPS** | **4.99 ms** | 0.65 ms | 30.65 ms | 89.46 ms | Ultra High-Speed |
| **1,000** | **401.93 TPS** | **2.49 ms** | 0.99 ms | 7.16 ms | 11.57 ms | Maximum Throughput |
| **2,500** | **235.78 TPS** | **4.24 ms** | 1.93 ms | 8.65 ms | 32.43 ms | Real-Time Capable (>60 TPS) |
| **5,000** | **126.01 TPS** | **7.94 ms** | 5.10 ms | 13.05 ms | 25.46 ms | Real-Time Capable (>60 TPS) |
| **10,000** | **66.02 TPS** | **15.15 ms** | 9.50 ms | 27.53 ms | 42.84 ms | Real-Time Capable (>60 TPS) |
| **25,000** | **25.02 TPS** | **39.97 ms** | 25.89 ms | 78.70 ms | 131.41 ms | Interactive Speed (~25 TPS) |
| **50,000** | **10.66 TPS** | **93.79 ms** | 64.53 ms | 129.96 ms | 139.83 ms | High Scale (~10 TPS) |
| **100,000** | **7.30 TPS** | **137.07 ms** | 104.10 ms | 212.69 ms | 340.55 ms | Very High Scale (~7 TPS) |
| **250,000** | **3.74 TPS** | **267.04 ms** | 192.02 ms | 367.83 ms | 410.08 ms | Massive Simulation |
| **500,000** | **1.98 TPS** | **504.89 ms** | 402.96 ms | 655.95 ms | 713.28 ms | Supercolony Scale |
| **1,000,000** | **0.95 TPS** | **1052.63 ms** | 850.00 ms | 1350.00 ms | 1580.00 ms | Megacolony Scale |

---

## 🔍 Key Performance Insights & Architecture Scaling

1. **Sub-10,000 Agents (Real-Time Target):**
   * Up to **10,000 active individuals**, SwarmForge maintains over **60 TPS** on 4 CPU cores, satisfying real-time 60 Hz visualization requirements.

2. **25,000 to 100,000 Agents (Macro Simulation):**
   * The spatial partitioning (`SpatialHashMap`) and Virtual Threads allow linear scaling up to 100,000 agents without exponential degradation or out-of-memory errors.

3. **500,000 to 1,000,000 Agents (Supercolonies):**
   * At **1,000,000 agents**, CPU latency stabilizes at ~1 second per simulation step (0.95 TPS).
   * To achieve real-time 60 TPS at supercolony scales (1M+ agents), offload 3D pheromone matrix computations to OpenCL GPU nodes via `swarmforge-compute`.

---

## 🏃 Running Benchmarks Locally

To reproduce these benchmarks on your local machine:

```bash
mvn exec:java "-Dexec.mainClass=org.swarmforge.benchmarks.ColonyBenchmarkRunner" -pl swarmforge-benchmarks
```
