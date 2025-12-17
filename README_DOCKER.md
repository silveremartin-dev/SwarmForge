# SwarmForge Deployment Guide 🐳

This guide explains how to deploy the full SwarmForge ecosystem using Docker.

## Architecture

The deployment consists of three main components:

1. **Server (`swarmforge-server`)**: The central brain, hosting the game world and simulation logic.
2. **Compute Node (`swarmforge-compute`)**: Worker nodes that offload heavy calculations. Can be scaled horizontally.
3. **Web Client (`swarmforge-web`)**: The 3D frontend interface served via Nginx.

## Prerequisites

- Docker Desktop
- Docker Compose

## Quick Start

1. **Build the images**:

    ```bash
    docker-compose build
    ```

    *Note: The first build may take a few minutes as it downloads Maven dependencies.*

2. **Start the Swarm**:

    ```bash
    docker-compose up
    ```

3. **Access the application**:
    - Web Client: [http://localhost:3000](http://localhost:3000)
    - Server API: [http://localhost:8080](http://localhost:8080)

## Scaling Compute Nodes

To simulate widespread distributed computing, you can scale the compute nodes:

```bash
docker-compose up --scale compute=5
```

This will launch 5 worker nodes that automatically register with the central server.
