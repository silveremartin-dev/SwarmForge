# Reddit Announcement (English)

**Suggested Subreddits:**
- `r/cellular_automata`
- `r/programming`
- `r/java`
- `r/gamedev`
- `r/indiegames`
- `r/biology`
- `r/computationalbiology`
- `r/artificial`

---

## 🐜 Post Title Ideas (Choose one according to subreddit)

1. **[Tech/Eng]** *SwarmForge v2.0 Released: An Open-Source High-Performance Eusocial Insect & Colony Simulation Engine (1M+ Agents, Artemis-odb ECS, OpenCL Pheromone Diffusion, Thermodynamics)*
2. **[GameDev/Sim]** *I built a 3D Eusocial Insect Simulation Studio from scratch with dynamic nest thermodynamics, 220+ ethological behaviors, and real-time world generation — SwarmForge is now available as a standalone 1-click release!*
3. **[Biology/Research]** *Introducing SwarmForge: A 100% Data-Driven Simulation Platform for Modeling Social Insect Societies (Ants, Bees, Termites, Wasps)*

---

## 📝 Post Body

Hey everyone! 👋

Over the past few years, I’ve been developing **SwarmForge**, an open-source, academic-grade simulation and research platform designed to model social and eusocial insect societies (*Atta*, *Apis*, *Vespula*, *Reticulitermes*, *Pogonomyrmex*, and more) with biological fidelity and real-time 3D visualization.

Today, I’m excited to announce the release of **SwarmForge v2.0** along with **standalone, zero-prerequisite standalone packages** for instant 1-click download and deployment! 🚀

---

### 🌟 What is SwarmForge?

SwarmForge bridges computational biology, artificial life, multi-agent systems, and real-time 3D simulation into a unified Studio environment.

Key capabilities include:
- 🧪 **100% Data-Driven Biological Engine**: Zero hardcoded constants! Lifespans, walking/flight speeds, oviposition rates, maturation stages (days $\rightarrow$ 1440 ticks/day), Arrhenius $Q_{10} = 2.2$ thermal kinetics, and caste protein thresholds are entirely customizable via biological presets.
- ⚡ **High-Throughput Artemis-odb ECS Engine**: Custom zero-allocation Structure-of-Arrays (SoA) memory pooling, 256-bit bitmask ethology covering 220+ individual behaviors across 4 primitive words, and $O(1)$ spatial hashing grid.
- 🏔️ **3D Terrarium & Subterranean World Editor**: Procedural voxel terrain synthesis, soil moisture layers, underground cut planes, and dynamic water table simulation.
- 🔥 **Physics-Based Nest Thermodynamics**: Stack-effect buoyancy ventilation, 1D Fourier heat diffusion, metabolic $CO_2$ dispersion grids, and passive colony thermal regulation.
- 💨 **OpenCL / GPU Pheromone Diffusion**: Massive 3D matrix evaporation, decay, and chemotaxis vector calculation.
- 🧠 **Multi-Tier Cognitive Architectures**: Belief-Desire-Intention (BDI), Multi-rate Finite State Machines, Fuzzy Logic, Behavior Trees, and PyTorch ONNX integration.
- 🌐 **Distributed Megaterrarium & Multiplayer**: World sharding across compute nodes, boundary halo pheromone exchange, inter-colony diplomacy, and tribute trade convoys.

---

### 📊 Performance & Scaling Benchmarks

| Colony Size | Engine Throughput | Latency | Execution Profile |
| :--- | :--- | :--- | :--- |
| **1,000 entities** | **61.7 TPS** | 16.2 ms | 🟢 Real-Time 3D Interactive (60 FPS) |
| **10,000 entities** | **1.0 TPS** | ~1,048 ms | 🟡 Full local ECS evaluation |
| **1,000,000 entities** | Megacolony Compute | *Off-Grid* | ⚙️ Headless SoA Mode (~32 MB RAM footprint) |

---

### ⚡ Instant Standalone Download & 1-Click Deployment

You **do NOT need Java, Maven, or any build tools installed**. SwarmForge v2.0 comes bundled with its own optimized standalone runtime:

1. **Download the Windows Standalone Bundle**:
   👉 [SwarmForge-v2.0.0-Windows-x64-Standalone.zip](https://github.com/swarmforge/swarmforge/releases)
2. **Extract anywhere** and double-click `SwarmForge.exe` (or run `Install-Shortcuts.bat` to create Desktop & Start Menu shortcuts).
3. **Headless & Multi-Node Cluster**: Also available as Docker container (`docker compose up -d`) and cross-platform headless server zip for Linux/macOS.

---

### 🔗 Links & Resources

- 💻 **GitHub Repository**: [github.com/swarmforge/swarmforge](https://github.com/swarmforge/swarmforge)
- 📦 **Releases & Standalone Packages**: [github.com/swarmforge/swarmforge/releases](https://github.com/swarmforge/swarmforge/releases)
- 📖 **Documentation & Behavioral Ethology Spec**: [docs/BEHAVIORAL_ETHOLOGY_SPECIFICATION.md](https://github.com/swarmforge/swarmforge/tree/main/docs)
- 📜 **License**: MIT (Free & Open Source)

I would love to get your thoughts, biological feedback, benchmark results on your rigs, or feature ideas for upcoming biomes!

Feel free to AMA below! 🐜🐝🪵
