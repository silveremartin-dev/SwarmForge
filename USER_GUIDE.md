# SwarmForge User Guide

Welcome to SwarmForge! This guide will help you run the simulation, navigate the interface, and explore the complex behaviors of the ant colonies.

## 🚀 Quick Start

1. **Build the project**:

    ```bash
    mvn clean install -DskipTests
    ```

2. **Start the Server**:
    Run `scripts\run-server.bat` (Windows) or `./scripts/run-server.sh` (Mac/Linux).
3. **Start the Client**:
    Run `scripts\run-client.bat` (Windows) or `./scripts/run-client.sh` (Mac/Linux).

---

## 🎮 Controls

### 3D View (JavaFX Client)

| Action | Control |
|--------|---------|
| **Rotate Camera** | Right-click + Drag |
| **Pan Camera** | Middle-click + Drag |
| **Zoom** | Mouse Wheel |
| **Select Object** | Left-click on ant/block |
| **Toggle HUD** | `H` key |
| **Pause/Resume** | `Space` key |

### Simulation Control Panel

- **Play/Pause**: Top-left play button.
- **Speed Slider**: Accelerate time (1x to 10x).
- **Time Slider**: Rewind history to see past events (if History is enabled).
- **Overlays**: Toggle Pheromones, Territory, or Temperature views.

---

## 🐜 Key Features & Systems

### 1. The Colony Lifecycle

- **Queens**: The heart of the colony. They mate (during mating season), lay eggs, and manage the swarm.
- **Workers**: Forage for food, dig tunnels, and tend to the brood.
- **Soldiers**: Defend the territory and attack predators.

### 2. The Nest System (Underground)

SwarmForge simulates a 3D underground tunnel network.

- **Digging**: Workers automatically dig new shafts and chambers when the nest gets crowded.
- **Chambers**:
  - **Brood Chambers**: Where eggs and larvae are kept warm.
  - **Food Storage**: Stockpiles of gathered resources.
  - **Queen's Chamber**: The central hub.
- **Visualization**: The client renders the tunnel network visible below the surface.

### 3. Seasons & Weather

The world simulates a yearly cycle:

- **Spring**: High food growth, mating flights occur.
- **Summer**: Active foraging, potential heatwaves.
- **Autumn**: Food scarcity begins, colony prepares for winter.
- **Winter**: Hibernation (torpor), slow metabolism, no foraging.

**Weather Events**:

- **Rain**: Washes away pheromones, creates puddles.
- **Heatwave**: Increases thirst, dangers of overheating.

### 4. Ant Wars (Territory)

Colonies compete for space.

- **Pheromone Borders**: Ants mark territory. Overlap causes conflict.
- **Combat**: Soldiers fight enemy ants interloping on their land.

### 5. Predation & Disease

- **Predators**: Spiders (ambush), Antlions (traps), and Birds (swoop attacks) hunt your ants.
- **Disease**: Fungal infections (Cordyceps), Mites, and Bacteria can spread through contact or food. Use the `DiseaseManager` to simulate outbreaks.

---

## 🛠️ Troubleshooting

- **Performance Issues**: Reduce the simulation speed or the number of ants in `config.properties`.
- **Connection Failed**: Ensure the Server is running *before* starting the Client.
