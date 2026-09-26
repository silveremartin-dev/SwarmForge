# 📊 SwarmForge Performance Benchmark Report

## 🖥️ System Architecture & Hardware Environment

| Parameter | Specification |
| :--- | :--- |
| **Operating System** | Windows 11 10.0 (amd64) |
| **Java Runtime** | 25 (Oracle Corporation) |
| **CPU Cores** | 4 Threads / Logical Cores |
| **System RAM / JVM** | 5068 MB Max Heap |
| **GPU Acceleration** | *Integrated Graphics / CPU Software Renderer (No Dedicated GPU)* |

## 🐜 1. Species Comparative Performance & Scaling

| Species Name | Scientific Name | Population | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) | Memory (MB) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Black Garden Ant | *Lasius niger* | 100 | 123,22 | 8,1150 | 41,4247 | 112 MB |
| Black Garden Ant | *Lasius niger* | 500 | 79,69 | 12,5481 | 64,4241 | 209 MB |
| Black Garden Ant | *Lasius niger* | 1 000 | 72,95 | 13,7073 | 70,2887 | 205 MB |
| Black Garden Ant | *Lasius niger* | 2 500 | 38,99 | 25,6429 | 229,7813 | 224 MB |
| Black Garden Ant | *Lasius niger* | 5 000 | 25,69 | 38,9210 | 602,3663 | 419 MB |
| Wood Ant | *Formica rufa* | 100 | 275,77 | 3,6254 | 12,6994 | 224 MB |
| Wood Ant | *Formica rufa* | 500 | 322,05 | 3,1041 | 8,3966 | 323 MB |
| Wood Ant | *Formica rufa* | 1 000 | 216,24 | 4,6229 | 23,3210 | 362 MB |
| Wood Ant | *Formica rufa* | 2 500 | 68,46 | 14,6053 | 168,2684 | 564 MB |
| Wood Ant | *Formica rufa* | 5 000 | 27,69 | 36,1126 | 508,8555 | 388 MB |
| Leafcutter Ant | *Atta cephalotes* | 100 | 369,45 | 2,7055 | 6,2542 | 340 MB |
| Leafcutter Ant | *Atta cephalotes* | 500 | 176,59 | 5,6604 | 22,5223 | 438 MB |
| Leafcutter Ant | *Atta cephalotes* | 1 000 | 145,92 | 6,8511 | 60,4411 | 476 MB |
| Leafcutter Ant | *Atta cephalotes* | 2 500 | 58,88 | 16,9808 | 163,4328 | 677 MB |
| Leafcutter Ant | *Atta cephalotes* | 5 000 | 32,85 | 30,4388 | 470,8100 | 601 MB |
| Fire Ant | *Solenopsis invicta* | 100 | 1013,30 | 0,9858 | 4,5224 | 458 MB |
| Fire Ant | *Solenopsis invicta* | 500 | 667,33 | 1,4975 | 4,9163 | 562 MB |
| Fire Ant | *Solenopsis invicta* | 1 000 | 315,63 | 3,1665 | 16,2427 | 599 MB |
| Fire Ant | *Solenopsis invicta* | 2 500 | 88,61 | 11,2842 | 119,4025 | 794 MB |
| Fire Ant | *Solenopsis invicta* | 5 000 | 32,08 | 31,1720 | 464,3814 | 679 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 100 | 1154,69 | 0,8610 | 3,3844 | 579 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 500 | 578,46 | 1,7275 | 5,1619 | 680 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 1 000 | 227,23 | 4,3987 | 17,3772 | 719 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 2 500 | 78,35 | 12,7612 | 152,6890 | 917 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 5 000 | 29,99 | 33,3371 | 498,6719 | 897 MB |
| Western Honey Bee | *Apis mellifera* | 100 | 899,75 | 1,1102 | 5,5505 | 695 MB |
| Western Honey Bee | *Apis mellifera* | 500 | 241,12 | 4,1446 | 16,8787 | 797 MB |
| Western Honey Bee | *Apis mellifera* | 1 000 | 209,38 | 4,7732 | 21,5971 | 836 MB |
| Western Honey Bee | *Apis mellifera* | 2 500 | 92,06 | 10,8607 | 125,8539 | 1031 MB |
| Western Honey Bee | *Apis mellifera* | 5 000 | 29,65 | 33,7198 | 501,4450 | 1239 MB |


## 🌐 2. Full 3D Virtual World Scenario Benchmarks

| Scenario Name | Species | Nest Architecture | Entities | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Jardin Tempéré (Lasius niger) | Lasius niger | Terrier Souterrain | 1 000 | 55,46 | 18,0300 | 125,9111 |
| Forêt Épicéa (Formica rufa) | Formica rufa | Dôme de Pin | 2 500 | 44,89 | 22,2718 | 132,8745 |
| Jungle Tropicale (Atta cephalotes) | Atta cephalotes | Chambres Fongiques | 3 500 | 48,30 | 20,7028 | 112,2270 |
| Supercolonie Aride (Solenopsis invicta) | Solenopsis invicta | Supercolonie Mature | 5 000 | 31,98 | 31,2637 | 243,8492 |


## 🖥️ 3. Headless vs Non-Headless (GUI 3D Interface) Mode Comparison

| Execution Mode | Entities | TPS (ticks/s) | FPS (Render) | Avg Latency (ms) | p95 Latency (ms) | GUI Overhead |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Headless (Backend Compute)** | 2 000 | 81,68 | N/A | 12,2390 | 27,4057 | Baseline (0%) |
| **Non-Headless (GUI Interface Graphique 3D)** | 2 000 | 9,65 | 11,2 FPS | 103,6780 | 183,5510 | **+88,19% Overhead** |

> **Technical Note**: On systems without discrete GPU acceleration, Non-Headless GUI mode utilizes CPU software rasterization for 3D/2D views. Headless mode isolates pure simulation compute capacity for maximum throughput.

