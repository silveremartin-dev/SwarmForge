# AGENT.md - SwarmForge Agent Guidelines & Core Philosophy

## 🎯 1. Project Objective & Core Philosophy

**SwarmForge** is a state-of-the-art, high-performance, academic-grade simulation platform for modeling **social and eusocial insect societies** (ants, honeybees, wasps, termites, bumblebees, etc.).

### Key Directives & Standards:
1. **Academic Rigor & Scientific Fidelity**:
   - The primary goal of SwarmForge is to produce simulation results that align closely with **empirical research, sociobiology, and biological reality** (myrmecology, melittology, entomology, behavioral ecology, and biochemistry).
   - Although the platform includes visual gamification features, interactive 3D editors, and visual studio components for accessibility, **scientific validity and academic rigor take absolute priority**.
   - All mathematical, physical, and biological models—such as chemical pheromone diffusion matrices, endocrine feedback loops, spatial navigation, caste differentiation, trophallaxis, nest architecture, and epidemiological dynamics—must be grounded in real-world biological mechanisms.

2. **Professional & Enterprise Quality**:
   - Every artifact produced (source code, architecture documentation, API schemas, microservices, GPU kernels, unit tests, and Javadoc) must meet the **highest professional software engineering and scientific standards**.
   - Avoid crude placeholders, trivialized toy algorithms, or overly simplified heuristics when implementing core domain logic.

---

## 🏗️ 2. Architectural Overview & Technical Stack

SwarmForge is designed as a modular, high-throughput, distributed simulation system using modern technologies:

| Subsystem | Module | Key Technologies & Responsibilities |
| :--- | :--- | :--- |
| **Core Engine** | `swarmforge-core` | Java 21 LTS, Hybrid ECS Architecture, Morton3D spatial hashing ($O(1)$ lookups), 3D Octree ($O(\log N)$ range queries), BDI / FSM / Fuzzy / RL cognitive architectures, Endocrine system, Pheromone diffusion kernels. |
| **Distributed Server** | `swarmforge-server` | gRPC over TLS, Protobuf / FlatBuffers zero-copy streaming, JWT authentication, PostgreSQL persistence, Redis cache, Prometheus telemetry export. |
| **Visual Studio Editor** | `swarmforge-editor` | JavaFX 21 + jMonkeyEngine 3.6 (3D viewport), LWJGL3, procedural voxel terrain/nest generators, species editor studio, real-time telemetry panels. |
| **Compute Worker** | `swarmforge-compute` | Headless worker nodes offloading heavy simulation tick execution & GPU matrix math (TornadoVM / OpenCL). |
| **Extension API** | `swarmforge-plugins` | Dynamic plugin loading mechanism for custom species, behaviors, and environmental disaster modules. |
| **Benchmarking** | `swarmforge-benchmarks`| JMH suite measuring tick latency, spatial query performance, and serialization throughput. |
| **Web Dashboard** | `swarmforge-web` | React 18, Three.js 3D web viewer, WebSockets / gRPC-Web control panel. |

---

## 🔬 3. Biological Domain & Simulation Modeling

When extending or modifying the simulation engine, strictly adhere to the following biological domains:

1. **Taxonomic & Social Scope**:
   - **Formicidae (Ants)**: Leafcutter fungus farming, army ant bivouacs, Weaver ant nest building, honeydew aphid farming, inter-colony warfare.
   - **Apidae (Honeybees & Bumblebees)**: Waggle dance spatial communications, hexagonal comb building, thermoregulation, brood nursing, queen mandibular pheromones.
   - **Vespidae (Wasps)**: Paper nest construction, predatory foraging, larval salivary reward feedback.
   - **Isoptera (Termites)**: Fungus comb vaults, mound airflow thermoregulation, protozoan gut symbiosis, caste plasticity.

2. **Caste Polymorphic & Social Organization**:
   - **Social Structures**: Monogyne, Polygyne, Oligogyne, and Gamergate hierarchy transitions.
   - **Castes**: Queens, Workers (Minor, Media, Major/Soldier), Drones, Eggs, Larvae, Pupae.
   - **Endocrine System Feedback**: Hormonal control loops (Juvenile Hormone, Ecdysone, Octopamine) regulating age polyethism, aggression levels, and task allocation.

3. **Environment & Spatial Dynamics**:
   - 3D spatial grids with elevation, subterranean strata, soil moisture, water tables, and dynamic weather (temperature, humidity, precipitation, solar vector).
   - Chemical communication via multi-channel 3D pheromone fields with diffusion, decay, and evaporation equations.

---

## 🎨 4. UI/UX, Internationalization & Parameter Rigor Directives

Whenever creating or modifying any UI components across editors, clients, panels, or web dashboards, you MUST automatically enforce the following standards:

1. **Systematized Mouse-Over Tooltips & Glossary Alignment**:
   - **EVERY SINGLE** UI parameter, label, slider, combo box, and input field must have a comprehensive mouse-over tooltip.
   - Tooltips must contain a complete label title, a clear biological or physical description (aligned with the Glossary tab), and its precise operational effect in the simulation.
2. **Explicit Real Metric Temporal Units (No Raw Ticks in UI)**:
   - **DO NOT display raw `ticks` in any UI parameter or label.**
   - All durations, lifespans, incubation periods, and rates in the UI must be expressed in standard human-readable metric time units (`s`, `min`, `h`, `jours`, `ans`).
   - **Single Location Rule**: There is **ONLY ONE** designated location in the entire suite—the Simulation Settings tab—where the user configures the simulation tick step duration (e.g. `1 tick = 0.1 s`). Everywhere else, the UI presents metric temporal units and performs conversion internally to simulation ticks.
3. **Explicit & Systematized Metric Units Everywhere**:
   - Every numerical or physical parameter in the UI must explicitly display its SI / metric unit of measurement (`°C`, `mm`, `m`, `cm`, `g`, `g/m²`, `ind/m²`, `km/h`, `ppm`, `µT`, `ms`, `s`, `min`, `h`, `jours`, `ans`, `%`, `MPa`, `victimes/j`, `g/ind/j`, `mL/ind/j`, etc.).
4. **No Truncated Labels (`...`)**:
   - Ensure label layout columns have sufficient minimum/preferred width (`ColumnConstraints` set to 240–260px or `USE_PREF_SIZE`) and text wrapping enabled so labels are NEVER cut off or truncated with `...`.
5. **Parameter-to-Simulation Integrity**:
   - Every parameter exposed in the UI must correspond to an active, functional variable used in the `swarmforge-core` simulation engine or domain models. No ghost or orphaned UI parameters.
6. **Complete Internationalization (i18n)**:
   - All user-facing text strings across all modules must be externalized in `i18n/messages_*.properties` files (supporting EN, FR, ES, DE, ZH) and dynamically bound to locale changes via I18n bindings.
7. **Explicit Confirmation on Delete Actions**:
   - Every single UI button executing a deletion or destructive action (caste, species, preset, nest, simulation, resource, etc.) MUST display an explicit JavaFX confirmation dialog (`Alert.AlertType.CONFIRMATION`) requiring user validation before processing the removal.
8. **Integrated Searchable Help & Documentation**:
   - Complex editor panes must consolidate reference material, glossaries, and pedagogical documentation directly into a dedicated, searchable UI tab rather than relying on external popup dialogs.

---

## 🤖 5. AI Agent Operating Rules & Directives

Whenever assisting with design, refactoring, feature implementation, or bug fixes:

1. **Automatic Adherence to Principles**:
   - You must automatically preserve and uphold these academic, high-fidelity, and UI parameter directives in **all** future interactions without requiring explicit reminders from the user.
2. **Code Standards**:
   - Write idiomatic Java 21+ code utilizing modern features (Virtual Threads, Records, Sealed Interfaces, Pattern Matching).
   - Ensure zero GC pause spikes during high-frequency simulation ticks by utilizing spatial indices (`Morton3D`, `Octree`) and object pools.
3. **Documentation & Tests**:
   - Maintain comprehensive Javadoc comments explaining biological context and mathematical formulations for public methods and domain classes.
   - Keep architectural documentation (`docs/ARCHITECTURE.md`, `README.md`, `AGENT.md`) in sync with codebase changes.
