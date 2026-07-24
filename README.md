# SwarmForge - Eusocial Insect Simulation Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

**SwarmForge** is a high-performance simulation platform for eusocial insects (ants, bees, termites). Features GPU-accelerated computing, distributed processing, real-time 3D visualization, and realistic modeling of colony dynamics with genetic evolution.

## ✨ Features

| Category | Features |
|----------|----------|
| **Simulation** | Virtual threads, SpatialHashMap (O(1) lookups), distributed compute nodes, A* pathfinding |
| **AI** | FSM, Fuzzy Logic, Neural Network, Behavior Trees - pluggable brains |
| **Ecology** | Predators (spiders, beetles), diseases, parasites, inter-colony wars |
| **Evolution** | Genetic traits, crossover/mutation, personality system, lineage tracking |
| **World** | 6 terrain presets, 5 weather profiles, day/night cycle, underground water table |
| **Species** | 8 species (Lasius, Atta, Formica, Solenopsis, Camponotus, Linepithema, etc.) |
| **Visualization** | jMonkeyEngine 3D, pheromone heatmaps, population graphs, individual tracking |
| **Recording** | Timelapse recorder, simulation checkpoints, replay |
| **Web** | React + Three.js client, REST API, WebSocket streaming |
| **Monitoring** | Prometheus metrics, Grafana dashboard |

## 🏗️ Architecture

```
swarmforge/
├── swarmforge-core/       # Core API, simulation engine, GPU kernels
├── swarmforge-server/     # gRPC server, cluster manager, REST API
├── swarmforge-editor/     # JavaFX 3D client (SwarmForge Studio)
├── swarmforge-compute/    # Distributed compute node
├── swarmforge-web/        # React + Three.js web client
├── swarmforge-plugins/    # Species extensions
├── demo/                  # Demo applications
└── scripts/               # Launch scripts
```

### Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21+ (Virtual Threads) |
| GPU | Aparapi (OpenCL) |
| Communication | gRPC + Protobuf |
| 3D Engine | jMonkeyEngine 3.6 |
| UI | JavaFX 21 |
| Web | React 18 + Three.js |
| Database | PostgreSQL + Redis |

## 🚀 Quick Start

### Prerequisites

- Java 21 or later
- Maven 3.9+
- Node.js 18+ (for web client)
- GPU with OpenCL support (optional, for acceleration)

### Build & Run

```bash
# Build all modules
mvn clean install -DskipTests

# Run Server (with GUI)
mvn exec:java -pl swarmforge-server -Dexec.mainClass=org.swarmforge.server.ServerGuiApp

# Run Editor/Client
mvn exec:java -pl swarmforge-editor -Dexec.mainClass=org.swarmforge.client.SwarmForgeClient

# Run Compute Node (for distributed processing)
mvn exec:java -pl swarmforge-compute -Dexec.mainClass=org.swarmforge.compute.ComputeNodeApp \
  -Dexec.args="--host localhost --port 50051 --my-port 50052 --gpu"
```

## 🐜 Species Library

| Species | Common Name | Colony Size | Notes |
|---------|-------------|-------------|-------|
| Lasius niger | Black Garden Ant | 15,000 | Starter species |
| Atta cephalotes | Leafcutter | 8M | Fungus farmers |
| Formica rufa | Wood Ant | 400,000 | Mega colonies |
| Solenopsis invicta | Fire Ant | 250,000 | Aggressive |
| Camponotus | Carpenter Ant | 10,000 | Long-lived |

## 🌍 Terrain & Weather Presets

**Terrains:** Temperate Forest, Tropical Rainforest, Desert, Savanna, Mediterranean, Tundra

**Weather:** Temperate (4 seasons), Tropical (monsoon), Arid, Mediterranean, Arctic

## 📊 Distributed Computing

SwarmForge supports distributed simulation across multiple compute nodes:

```bash
# Server auto-discovers nodes via gRPC registration
# Nodes send heartbeats every 30s, auto-removed after 60s timeout
# GPU nodes prioritized for pheromone/pathfinding tasks
```

## 📖 Documentation

- [User Guide](USER_GUIDE.md) - Getting started
- [Architecture Guide](architecture.md) - System design
- [API Javadoc](javadoc/index.html) - Generated docs
- [Credits](credits.md) - Contributors

## 📜 License

MIT License - Copyright (c) 2022-2026 Silvère Martin-Michiellot

## 👥 Authors

- **Gemini AI Assistant** - AI Development Partner (Google DeepMind)
- **Silvère Martin-Michiellot** - Lead Developer
