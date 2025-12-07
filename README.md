# SwarmForge - Eusocial Insect Simulation Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)

**SwarmForge** is a high-performance, GPU-accelerated simulation platform for eusocial insects (ants, bees, termites). It supports distributed computing, real-time 3D visualization, and realistic modeling of colony dynamics and behaviors.

## 🎯 Goals

- **Realistic Simulation**: Model accurate ant behaviors, pheromone trails, colony lifecycle
- **Massive Scale**: Simulate 100,000+ individuals with GPU acceleration
- **3D Visualization**: Minecraft-style voxel world with LOD rendering
- **Research Ready**: Tools for studying colony dynamics and collective behavior

## 🏗️ Architecture

```
swarmforge/
├── swarmforge-core/          # Core API, simulation engine
├── swarmforge-server/        # GPU-accelerated server
├── swarmforge-client-javafx/ # JavaFX 3D client
├── swarmforge-plugins/       # Species extensions
├── demo/                     # Demo applications
└── scripts/                  # Launch scripts
```

### Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 LTS (Virtual Threads) |
| Communication | gRPC + FlatBuffers |
| GPU | TornadoVM |
| 3D Engine | jMonkeyEngine 3.6 |
| UI | JavaFX 21 |
| Database | PostgreSQL + Redis |

## 🚀 Quick Start

```bash
# Build
mvn clean install

# Run Server
scripts\run-server.bat   # Windows
./scripts/run-server.sh  # Linux/Mac

# Run Client
scripts\run-client.bat   # Windows
./scripts/run-client.sh  # Linux/Mac
```

## 📖 Documentation

- [Architecture Guide](architecture.md)
- [Credits](credits.md)

## 🌍 Languages

🇺🇸 English | 🇫🇷 Français | 🇩🇪 Deutsch | 🇪🇸 Español

## 📜 License

MIT License - Copyright (c) 2022-2025 Silvère Martin-Michiellot

## 👥 Authors

- **Silvère Martin-Michiellot** - Lead Developer
- **Gemini AI Assistant** - AI Development Partner (Google DeepMind)
