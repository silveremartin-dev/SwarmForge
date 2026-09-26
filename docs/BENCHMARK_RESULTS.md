# 📊 SwarmForge Performance Benchmark Report

## 🖥️ System Architecture & Hardware Environment

| Parameter | Specification |
| :--- | :--- |
| **Operating System** | Windows 11 10.0 (amd64) |
| **Java Runtime** | 25 (Oracle Corporation) |
| **CPU Cores** | 4 Threads / Logical Cores |
| **System RAM / JVM** | 8192 MB Max Heap |
| **GPU Acceleration** | *Integrated Graphics / CPU Software Renderer (No Dedicated GPU)* |
| **Architecture Mode** | **1 Nœud de calcul standard (Single Compute Node Baseline)** |

---

## 🐜 1. Species Comparative Performance & Scaling

| Species Name | Scientific Name | Population | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) | Memory (MB) | Nœuds |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Black Garden Ant | *Lasius niger* | 100 | 204.42 | 4.8909 | 16.0908 | 180 MB | 1 nœud |
| Black Garden Ant | *Lasius niger* | 500 | 153.33 | 6.5208 | 29.3972 | 218 MB | 1 nœud |
| Black Garden Ant | *Lasius niger* | 1 000 | 113.25 | 8.8267 | 42.0624 | 255 MB | 1 nœud |
| Black Garden Ant | *Lasius niger* | 2 500 | 71.02 | 14.0787 | 137.8218 | 355 MB | 1 nœud |
| Black Garden Ant | *Lasius niger* | 5 000 | 31.19 | 32.0570 | 450.9455 | 333 MB | 1 nœud |
| Wood Ant | *Formica rufa* | 100 | 401.40 | 2.4906 | 7.3322 | 235 MB | 1 nœud |
| Wood Ant | *Formica rufa* | 500 | 361.52 | 2.7649 | 10.2815 | 335 MB | 1 nœud |
| Wood Ant | *Formica rufa* | 1 000 | 402.79 | 2.4815 | 13.5238 | 376 MB | 1 nœud |
| Wood Ant | *Formica rufa* | 2 500 | 95.20 | 10.5020 | 120.8363 | 573 MB | 1 nœud |
| Wood Ant | *Formica rufa* | 5 000 | 25.89 | 38.6172 | 601.7467 | 1150 MB | 1 nœud |
| Leafcutter Ant | *Atta cephalotes* | 100 | 972.71 | 1.0276 | 4.2886 | 351 MB | 1 nœud |
| Leafcutter Ant | *Atta cephalotes* | 500 | 331.80 | 3.0118 | 10.7291 | 453 MB | 1 nœud |
| Leafcutter Ant | *Atta cephalotes* | 1 000 | 217.22 | 4.6017 | 34.4729 | 494 MB | 1 nœud |
| Leafcutter Ant | *Atta cephalotes* | 2 500 | 101.47 | 9.8532 | 101.7699 | 691 MB | 1 nœud |
| Leafcutter Ant | *Atta cephalotes* | 5 000 | 21.87 | 45.7322 | 681.0377 | 1264 MB | 1 nœud |
| Fire Ant | *Solenopsis invicta* | 100 | 1037.55 | 0.9627 | 4.1969 | 470 MB | 1 nœud |
| Fire Ant | *Solenopsis invicta* | 500 | 600.06 | 1.6649 | 5.1636 | 571 MB | 1 nœud |
| Fire Ant | *Solenopsis invicta* | 1 000 | 236.60 | 4.2240 | 22.2978 | 610 MB | 1 nœud |
| Fire Ant | *Solenopsis invicta* | 2 500 | 22.22 | 45.0092 | 299.3245 | 804 MB | 1 nœud |
| Fire Ant | *Solenopsis invicta* | 5 000 | 10.91 | 91.6932 | 1544.0723 | 1366 MB | 1 nœud |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 100 | 1799.18 | 0.5554 | 2.9676 | 588 MB | 1 nœud |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 500 | 849.86 | 1.1758 | 3.8721 | 689 MB | 1 nœud |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 1 000 | 334.69 | 2.9863 | 16.8221 | 728 MB | 1 nœud |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 2 500 | 82.93 | 12.0569 | 152.1118 | 928 MB | 1 nœud |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 5 000 | 21.36 | 46.8039 | 748.0578 | 1500 MB | 1 nœud |
| Western Honey Bee | *Apis mellifera* | 100 | 1307.69 | 0.7639 | 3.5387 | 706 MB | 1 nœud |
| Western Honey Bee | *Apis mellifera* | 500 | 180.99 | 5.5205 | 19.3616 | 807 MB | 1 nœud |
| Western Honey Bee | *Apis mellifera* | 1 000 | 277.40 | 3.6031 | 14.2010 | 846 MB | 1 nœud |
| Western Honey Bee | *Apis mellifera* | 2 500 | 97.83 | 10.2204 | 111.6712 | 1040 MB | 1 nœud |
| Western Honey Bee | *Apis mellifera* | 5 000 | 23.69 | 42.2086 | 661.8707 | 1602 MB | 1 nœud |

---

## 🌐 2. Full 3D Virtual World Scenario Benchmarks (1 Node Baseline)

| Scenario Name | Species | Nest Architecture | Entities | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) | Nœuds |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Jardin Tempéré (Lasius niger) | Lasius niger | Terrier Souterrain | 1 000 | 81.30 | 12.2981 | 42.4487 | 1 nœud |
| Forêt Épicéa (Formica rufa) | Formica rufa | Dôme de Pin | 2 500 | 90.67 | 11.0257 | 55.1971 | 1 nœud |
| Jungle Tropicale (Atta cephalotes) | Atta cephalotes | Chambres Fongiques | 3 500 | 69.57 | 14.3725 | 77.5488 | 1 nœud |
| Supercolonie Aride (Solenopsis invicta) | Solenopsis invicta | Supercolonie Mature | 5 000 | 44.30 | 22.5730 | 236.3822 | 1 nœud |

---

## 🖥️ 3. Headless vs Non-Headless (GUI 3D Interface) Mode Comparison

| Execution Mode | Entities | TPS (ticks/s) | FPS (Render) | Avg Latency (ms) | p95 Latency (ms) | GUI Overhead |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Headless (Backend Compute)** | 2 000 | 108.53 | N/A | 9.2116 | 11.3737 | Baseline (0%) |
| **Non-Headless (GUI Interface Graphique 3D)** | 2 000 | 131.74 | 152.4 FPS | 7.5907 | 18.4748 | **+-21.38% Overhead** |

> **Technical Note**: On systems without discrete GPU acceleration, Non-Headless GUI mode utilizes CPU software rasterization for 3D/2D views. Headless mode isolates pure simulation compute capacity for maximum throughput.

---

## 🚀 4. Massive Population Scaling Benchmarks (5,000 to 2,000,000 Individuals)

> ℹ️ **Architecture Baseline**: Tous les résultats ci-dessous sont mesurés sur **1 Nœud de calcul autonome unique (1 Compute Node: 4 Cores / 8 GB JVM Heap)** en mode Headless pur (Artemis-odb ECS unifié + indexation spatiale Morton3D).

| Population | Entities / Agents | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) | Agent-Updates / sec | Heap Memory (MB) | Équivalent Nœud |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **5 000** | 5 000 ants | 53.24 | 18.7820 | 75.8948 | 266 213 | 220 MB | **1 Nœud** |
| **10 000** | 10 000 ants | 23.88 | 41.8811 | 192.7253 | 238 771 | 387 MB | **1 Nœud** |
| **20 000** | 20 000 ants | 19.72 | 50.7022 | 248.6791 | 394 460 | 721 MB | **1 Nœud** |
| **50 000** | 50 000 ants | 8.94 | 111.8237 | 559.4301 | 447 132 | 130 MB | **1 Nœud** |
| **100 000** | 100 000 ants | 4.12 | 242.7875 | 1178.6491 | 411 883 | 378 MB | **1 Nœud** |
| **200 000** | 200 000 ants | 2.15 | 464.6135 | 2260.5383 | 430 465 | 192 MB | **1 Nœud** |
| **500 000** | 500 000 ants | 0.76 | 1321.5526 | 6644.0627 | 378 343 | 956 MB | **1 Nœud** |
| **1 000 000** | 1 000 000 ants | 0.40 | 2519.0284 | 12791.5013 | 396 978 | 1828 MB | **1 Nœud** |
| **1 500 000** | 1 500 000 ants | 0.22 | 4638.8122 | 26105.0317 | 323 359 | 1176 MB | **1 Nœud** |
| **2 000 000** | 2 000 000 ants | 0.19 | 5374.5357 | 26724.4001 | 372 125 | 2578 MB | **1 Nœud** |

---

## 🌐 5. Extension Cluster Multi-Nœuds & Sharding Spatial (Multi-Node Scaling)

### 📊 Mesures Empiriques : 1 Nœud (Local) vs Cluster 2 Nœuds (Mégaterrarium Shardé)

Les mesures réelles ci-dessous comparent l'exécution sur **1 Nœud autonome** versus un **Cluster 2 Nœuds** avec synchronisation synchrone des halos phéromonaux et transfert de frontières (`BorderMigrationSystem`) :

| Population Totale | Configuration | Débit (TPS) | Latence Moyenne (ms) | Latence p95 (ms) | Agent-Updates / sec | Gain d'Accélération (Speedup) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **5 000** | 1 Nœud Local | 3,41 TPS | 292,83 ms | 773,05 ms | 17 075 /s | 1.00x *(Baseline)* |
| **5 000** | 2 Nœuds Cluster | 3,13 TPS | 319,61 ms | 1 166,53 ms | 15 644 /s | 0.92x *(Coût sync)* |
| **10 000** | 1 Nœud Local | 2,82 TPS | 354,15 ms | 1 009,81 ms | 28 237 /s | 1.00x *(Baseline)* |
| **10 000** | 2 Nœuds Cluster | 2,45 TPS | 407,93 ms | 1 434,96 ms | 24 514 /s | 0.87x *(Coût sync)* |
| **25 000** | 1 Nœud Local | 2,11 TPS | 473,64 ms | 1 203,41 ms | 52 783 /s | 1.00x *(Baseline)* |
| **25 000** | 2 Nœuds Cluster | 1,84 TPS | 543,19 ms | 1 748,09 ms | 46 025 /s | 0.87x *(Coût sync)* |
| **50 000** | 1 Nœud Local | 1,39 TPS | 721,11 ms | 2 866,43 ms | 69 337 /s | 1.00x *(Baseline)* |
| **50 000** | 2 Nœuds Cluster | 1,26 TPS | 792,16 ms | 2 430,58 ms | 63 118 /s | 0.91x *(Transition)* |
| **100 000** | 1 Nœud Local | 0,72 TPS | 1 395,61 ms | 8 449,59 ms | 71 653 /s | 1.00x *(Baseline)* |
| **100 000** | 2 Nœuds Cluster | **0,81 TPS** | **1 237,50 ms** | **4 215,71 ms** | **80 808 /s** | **+1.13x** |
| **250 000** | 1 Nœud Local | 0,19 TPS | 5 186,61 ms | 59 745,04 ms | 48 201 /s | 1.00x *(Baseline)* |
| **250 000** | 2 Nœuds Cluster | **0,37 TPS** | **2 715,05 ms** | **19 254,98 ms** | **92 079 /s** | **+1.91x** |
| **500 000** | 1 Nœud Local | 0,06 TPS | 17 845,26 ms | 269 317,94 ms | 28 019 /s | 1.00x *(Baseline)* |
| **500 000** | 2 Nœuds Cluster | **0,12 TPS** | **8 690,52 ms** | **99 334,92 ms** | **57 534 /s** | **+2.05x (Superlinéaire)** |

> 💡 **Analyse des Résultats** :
> - **Petites populations (< 50 000)** : Le surcoût d'échange gRPC/IPC et de synchronisation des bordures dépasse le temps de calcul brut, rendant le mono-nœud légèrement plus rapide.
> - **Fortes charges (≥ 100 000)** : Le partitionnement spatial allège drastiquement la contention mémoire et l'évaluation BDI/FSM par nœud. À **500 000 agents**, le cluster 2 nœuds divise le temps de tick par deux (**2.05x de speedup**) et stabilise la latence p95.
