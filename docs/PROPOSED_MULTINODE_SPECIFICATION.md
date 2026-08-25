# SwarmForge - Multi-Node Distributed Architecture, Server Browser & Automated Checkpoint Specification

## 1. Overview & Architectural Scope

This specification defines the extension plan for **SwarmForge** to support multi-client distributed multiplayer, seamless multi-terrarium spatial topology, automated periodic checkpoint retention, and player-to-player diplomatic interaction protocols.

The underlying gRPC infrastructure (`SimulationService`, `WorldService`, `MatchmakingService`, `ComputeService`, `AuthService`) provides backend RPC contracts for state synchronization, node registration, and matchmaking. This document specifies the complete visual, topological, and persistence contracts for production execution.

---

## 2. Automated Periodic Checkpointing & Disk Cache Retention Policy

### 2.1 Current Implementation State
- **Manual Checkpoints:** Implemented via `Simulation.createCheckpoint(String name)` and `Simulation.restoreCheckpoint(SimulationCheckpoint cp)`.
- **Snapshot & Intervention Journaling:** `SimulationSnapshot` captures physical grid state, colonies, and entities. `GodModeIntervention` journals all divine interventions (food spawns, temperature shifts, weather triggers) with their exact tick numbers.
- **Binary Compression:** `SimulationCheckpoint.toCompressedBytes()` compresses snapshots and intervention logs into GZIP binary payloads.

### 2.2 Proposed Automated Checkpointing Specification
1. **Periodic Tick Trigger:**
   - Configurable tick interval `autoCheckpointIntervalTicks` (default: `1,000` ticks in fast mode, `10,000` ticks in long-running macro mode).
   - Executed asynchronously off the main simulation thread to prevent tick stutter.
2. **Rotating Disk Cache & Pruning Retention:**
   - Disk location: `~/.swarmforge/checkpoints/{simulation_id}/`
   - **Retention Policy:** Keep at most $N$ auto-checkpoints (default: $N = 10$).
   - **Pruning Algorithm:** When count exceeds $N$, the oldest auto-checkpoint is automatically deleted (`FIFO` eviction), while user-named explicit checkpoints are pinned and preserved permanently.
   - **Disk Quota Safety:** Hard storage limit per simulation (e.g., 500 MB). If exceeded, emergency pruning removes intermediate auto-checkpoints.

---

## 3. Multi-Client GUI: Server Browser & Room Lobby Specification

### 3.1 Server Browser UI (`ServerBrowserPane`)
A dedicated tab in `swarmforge-editor` / `swarmforge-client` enabling players to discover, inspect, and connect to remote `swarmforge-server` instances.

- **Server Discovery List:**
  - Displays Server Name, Host IP/Port, Ping Latency, Active Simulations, Connected Clients, and Database Persistence status (`PostgreSQL` vs `H2 In-Memory`).
  - Calls `SimulationService.GetServerStatus()` and `SimulationService.ListSimulations()`.
- **Room / Simulation Creation Dialog:**
  - Allows host to set World Dimensions ($W \times H \times D$), Master Seed, Max Players, Species restrictions, and Tick Execution Rate (TPS).

### 3.2 Interactive Terrarium Placement & Lobby
- **Terrarium Slot Selection:** Visual 2D/3D map grid displaying available starting zones.
- **Colony Profile Setup:** Players select species preset (`Lasius niger`, `Atta sexdens`, `Solenopsis invicta`, etc.), initial caste ratios (Queen, Workers, Soldiers), and player alias.
- **Ready / Start Handshake:** Host initiates tick execution once all players confirm readiness.

---

## 4. Multi-Terrarium Topology & Seamless Spatial Continuity

### 4.1 Topology Standardization
To maintain physical and biological consistency without mismatched borders or visual artifacts:
- **Regular Grid Tiling:** All interconnected terrariums in a cluster MUST use standardized matching dimensions ($N \times N \times Z$, e.g., $100\text{ m} \times 100\text{ m} \times 50\text{ m}$).
- **Border Alignment:** Terrariums are organized on a 2D/3D grid index $(I_x, I_y)$. Terrarium $(0,0)$ shares its East border ($X = N$) with Terrarium $(1,0)$'s West border ($X = 0$).

### 4.2 Cross-Border Streaming Protocol
1. **Entity Migration (`IndividualDelta` streaming):**
   - When an individual entity reaches a boundary cell (e.g., $X \ge N - 0.5$), the host server serializes its state, removes it from Terrarium A's spatial index, and injects it into Terrarium B at $X = 0.5$ via `StreamUpdates`.
2. **Pheromone Matrix Edge Coupling:**
   - Boundary cells exchange pheromone intensity values during the `ProcessPheromones` diffusion pass.
   - Pheromone gradient vectors cross borders seamlessly, allowing foragers in Terrarium B to follow chemical trails initiated in Terrarium A.

---

## 5. Player-to-Player Diplomacy & Interaction Protocol

### 5.1 Diplomatic State Machine (`DiplomacyManager`)
Inter-colony relationships between players are governed by 4 status states:
- `NEUTRAL`: Default state. Passive foraging; collision occurs without immediate mass war.
- `ALLY`: Mutual non-aggression. Shared pheromone trail awareness (`FOOD_TRAIL`, `HOME_TRAIL`). Workers do not initiate combat.
- `ENEMY`: Active territorial war. Soldiers prioritize attacking opposing individuals on perception checks ($O(\log N)$ Octree lookups).
- `TRADING`: Active resource exchange. Designated worker convois transfer food/water stockpiles across terrarium boundaries.

### 5.2 Diplomatic Actions Protocol (`DiplomacyAction`)
- `PROPOSE_ALLIANCE` / `ACCEPT_ALLIANCE` / `REJECT_ALLIANCE` / `BREAK_ALLIANCE`
- `DECLARE_WAR`
- `OFFER_TRIBUTE` (Resource type: `SUGAR`, `PROTEIN`, `HONEYDEW`, Amount)

---

## 6. Execution & Implementation Roadmap

| Phase | Subsystem | Description & Artifact Deliverables |
| :--- | :--- | :--- |
| **Phase 1** | **Auto-Checkpointing** | Implement background timer & FIFO disk pruning (`~/.swarmforge/checkpoints/`). |
| **Phase 2** | **Server Browser GUI** | Build `ServerBrowserPane` in JavaFX for listing rooms and server status. |
| **Phase 3** | **Border Topology** | Implement cross-border entity handoff and boundary pheromone coupling via gRPC. |
| **Phase 4** | **Diplomacy UI** | Add interactive diplomacy panel for alliance proposals and resource transfers. |
