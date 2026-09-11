# SwarmForge User & Researcher Guide

Welcome to **SwarmForge**! This guide assists computational biologists, students, and enthusiasts in running the simulation, navigating the 3D visual editor, parameterizing colonies, and conducting high-fidelity ecological experiments.

---

## 🚀 Quick Start

### 1. Build the Complete Suite
```bash
mvn clean install -DskipTests
```

### 2. Start the SwarmForge Server (with Headless or GUI Monitor)
```bash
mvn exec:java -pl swarmforge-server -Dexec.mainClass=org.swarmforge.server.ServerGuiApp
```

### 3. Launch the 3D Studio & World Editor
```bash
mvn exec:java -pl swarmforge-editor -Dexec.mainClass=org.swarmforge.client.SwarmForgeClient
```

### 4. Optional: Launch Distributed GPU Compute Nodes
```bash
mvn exec:java -pl swarmforge-compute -Dexec.mainClass=org.swarmforge.compute.ComputeNodeApp \
  -Dexec.args="--host localhost --port 50051 --my-port 50052 --gpu"
```

---

## 🎮 3D Viewport Controls & Shortcuts

| Action | Control Key / Mouse Gesture |
| :--- | :--- |
| **Orbit / Rotate Camera** | Right-click + Drag |
| **Pan Camera** | Middle-click + Drag (or Shift + Right-click) |
| **Zoom In / Out** | Mouse Wheel scroll |
| **Inspect Entity (Ant / Block / Plant)** | Left-click on entity to open HUD Inspector |
| **Toggle Full HUD Overlay** | `H` key |
| **Pause / Resume Execution** | `Space` key |
| **Toggle Subterranean Cut Plane** | `C` key |
| **Toggle Pheromone Bioluminescence** | `P` key |

---

## 🐜 Core Biological & Ecological Subsystems

### 1. Polymorphic Castes & Life Stages
- **Queens**: The reproductive center of the colony. Nuptial flights require specific aerological windows (temperature $22\text{--}30^\circ\text{C}$, humidity $>70\%$, calm winds $<4.5\,\text{m/s}$, rising barometric pressure).
- **Workers (Minors / Medias)**: Forage for vegetation and nectar, maintain fungal gardens, care for brood (*allogrooming*), and excavate tunnels.
- **Soldiers (Majors)**: High mandibular bite forces (up to $45\,\text{MPa}$), phragmotic head plugs, and lethal defensive responses against marauding predators.
- **Drones (Males)**: Ephemeral alates produced during reproductive seasons for nuptial flight genetic dissemination.

### 2. Symbiotic Fungal Agriculture (*Atta*, *Acromyrmex*)
- **Substrate Processing**: Foraged leaf fragments are masticated into mulch by media workers.
- **Stoichiometric $C:N$ Digestion**: Gardens convert mulch ($C:N = 22.5:1$) into nutrient-dense **gongylidia** / **staphylae** protein clusters, directly fueling larval development and queen oviposition.
- **Weeding & Sanitation**: Minor workers apply symbiotic *Pseudonocardia* actinobacteria secretions to eliminate parasitic *Escovopsis* micro-molds.

### 3. Subterranean Nest Physics & Fluid Dynamics
- **Stack-Effect Ventilation**: Buoyancy differentials drive airflow through vertical shafts, expelling metabolic $\text{CO}_2$ and drawing in fresh $\text{O}_2$.
- **Mohr-Coulomb Structural Stability**: Excavated chambers calculate overburden soil stresses, cohesion, and friction angles, warning of cave-in risks under excessive moisture or seismic disturbances.
- **1D Fourier Thermal Diffusion**: Deep chambers maintain stable microclimates, buffering against harsh winter frost and summer heatwaves.

### 4. Chemical Ecology & CHC Discrimination
- **8-Channel Pheromone Diffusion**: Real-time trail diffusion (Food, Home, Alarm, Territory, Brood, Queen, Necrophoric, Aggression).
- **Cuticular Hydrocarbon (CHC) Discrimination**: Ants evaluate 32-compound Bray-Curtis dissimilarity indices to distinguish nestmates from interlopers, preventing infiltration by foreign competitors.

---

## ⚡ God Mode Interventions & Environmental Disasters

Through the **God Mode & Climate Panel**, researchers can trigger controlled ecological stresses:
- **Severe Drought**: Shrinks surface water sources and accelerates worker desiccation.
- **Flash Flood**: Inundates low-lying terrain and floods underground chambers.
- **Wildfire**: Rapidly sweeps across surface vegetation, demanding emergency defensive firebreaks.
- **Pathogen Outbreak (*Cordyceps* / Mites)**: Tests colony social immunity, allogrooming, and quarantine behaviors.

---

## 🌐 Multi-Node Megaterrarium, Server Browser & Multiplayer Guide

### 1. Launching a Multi-Node Cluster
1. **Start the Master Orchestrator Server**:
   ```bash
   mvn exec:java -pl swarmforge-server -Dexec.mainClass=org.swarmforge.server.SwarmForgeServer
   ```
2. **Connect Distributed Compute Nodes**:
   ```bash
   mvn exec:java -pl swarmforge-compute -Dexec.mainClass=org.swarmforge.compute.ComputeNodeApp \
     -Dexec.args="--host localhost --port 50051 --my-port 50052"
   ```
3. **Launch the Studio with Server Browser**:
   ```bash
   mvn exec:java -pl swarmforge-editor -Dexec.mainClass=org.swarmforge.client.SwarmForgeClient
   ```

### 2. Using the Server Browser (`ServerBrowserPane`)
- Open the **Server Browser** tab in the main studio window.
- **Server Discovery**: Click **Refresh** to query local LAN and registered servers, showing real-time ping latencies, player counts, and active simulations.
- **Create Megaterrarium Room**:
  - Set grid dimensions (e.g. $2 \times 1$ for a 2-player 1v1 duel, $2 \times 2$ for a 4-node federation).
  - Select a dedicated Multiplayer Scenario from the dropdown.
  - Choose your species deck profile (starting queen, worker counts, and genetic adaptations).
- **Ready & Start**: When all players indicate readiness, the host launches the synchronized simulation.

### 3. Dedicated Multiplayer Scenarios
- **`MP_01_BATTLE_ARENA_1V1`**: 2-Player territorial duel (*Atta sexdens* vs *Solenopsis invicta*) with contested central food resources.
- **`MP_02_COOP_TRIBUTE_TRADE`**: 2-Player cooperative trade (*Messor barbarus* seed granary trading with *Lasius niger* honeydew harvesters via diplomatic tribute transfers).
- **`MP_03_MEGATERRARIUM_4NODE_ALLIANCE`**: 4-Node sharded megaterrarium simulating a polycalic supercolony (*Formica polyctena*) across a $2 \times 2$ grid with real-time border migration and pheromone halo synchronization.

---

## 🛡️ Configuration & Security Tips

- **JWT Authentication**: Export `SWARMFORGE_JWT_SECRET` (minimum 32 characters) to enable persistent authenticated multi-client sessions.
- **Rate Limiting**: Built-in sliding-window rate limiters prevent client flooding (max 100 messages/second per WebSocket connection).
- **FIFO Checkpoint Retention**: Automatic periodic checkpoints keep the last 10 snapshots (up to 500 MB) while preserving user-pinned manual checkpoints.

