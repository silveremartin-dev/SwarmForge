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

## 🛡️ Configuration & Security Tips

- **JWT Authentication**: Export `SWARMFORGE_JWT_SECRET` (minimum 32 characters) to enable persistent authenticated multi-client sessions.
- **Rate Limiting**: Built-in sliding-window rate limiters prevent client flooding (max 100 messages/second per WebSocket connection).
- **Deterministic Checkpointing**: Save states at any time via the **File $\rightarrow$ Save Checkpoint** menu for exact snapshot replay and analysis.
