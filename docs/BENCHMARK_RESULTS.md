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
| Black Garden Ant | *Lasius niger* | 100 | 79,54 | 12,5712 | 116,4667 | 107 MB |
| Black Garden Ant | *Lasius niger* | 500 | 61,94 | 16,1440 | 138,7653 | 142 MB |
| Black Garden Ant | *Lasius niger* | 1 000 | 9,00 | 111,0939 | 235,3680 | 191 MB |
| Black Garden Ant | *Lasius niger* | 2 500 | 6,23 | 160,4553 | 263,9773 | 332 MB |
| Black Garden Ant | *Lasius niger* | 5 000 | 1,14 | 880,6252 | 1143,7015 | 353 MB |
| Wood Ant | *Formica rufa* | 100 | 1131,61 | 0,8830 | 2,9259 | 319 MB |
| Wood Ant | *Formica rufa* | 500 | 89,22 | 11,2076 | 48,9663 | 430 MB |
| Wood Ant | *Formica rufa* | 1 000 | 24,88 | 40,1975 | 180,7476 | 658 MB |
| Wood Ant | *Formica rufa* | 2 500 | 5,69 | 175,7746 | 536,8829 | 600 MB |
| Wood Ant | *Formica rufa* | 5 000 | 1,36 | 737,6975 | 1135,8428 | 735 MB |
| Leafcutter Ant | *Atta cephalotes* | 100 | 39,28 | 25,4602 | 398,9240 | 561 MB |
| Leafcutter Ant | *Atta cephalotes* | 500 | 96,83 | 10,3255 | 82,4235 | 668 MB |
| Leafcutter Ant | *Atta cephalotes* | 1 000 | 22,48 | 44,4842 | 164,3857 | 755 MB |
| Leafcutter Ant | *Atta cephalotes* | 2 500 | 4,88 | 204,7785 | 397,6784 | 732 MB |
| Leafcutter Ant | *Atta cephalotes* | 5 000 | 0,31 | 3189,3609 | 10783,7870 | 1180 MB |
| Fire Ant | *Solenopsis invicta* | 100 | 94,76 | 10,5516 | 66,8263 | 809 MB |
| Fire Ant | *Solenopsis invicta* | 500 | 72,87 | 13,7201 | 141,4464 | 914 MB |
| Fire Ant | *Solenopsis invicta* | 1 000 | 32,86 | 30,4312 | 95,6642 | 1142 MB |
| Fire Ant | *Solenopsis invicta* | 2 500 | 5,50 | 181,9437 | 330,7686 | 1145 MB |
| Fire Ant | *Solenopsis invicta* | 5 000 | 1,72 | 580,6765 | 832,0640 | 1506 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 100 | 2333,64 | 0,4276 | 0,8575 | 1052 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 500 | 70,37 | 14,2085 | 146,0958 | 1161 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 1 000 | 25,49 | 39,2209 | 145,1163 | 1393 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 2 500 | 2,31 | 432,9311 | 650,8297 | 1610 MB |
| Black Carpenter Ant | *Camponotus pennsylvanicus* | 5 000 | 0,40 | 2498,3953 | 7213,3231 | 2263 MB |
| Western Honey Bee | *Apis mellifera* | 100 | 316,73 | 3,1553 | 20,5742 | 1291 MB |
| Western Honey Bee | *Apis mellifera* | 500 | 29,47 | 33,9242 | 73,8623 | 1406 MB |
| Western Honey Bee | *Apis mellifera* | 1 000 | 25,73 | 38,8685 | 142,3298 | 1630 MB |
| Western Honey Bee | *Apis mellifera* | 2 500 | 1,24 | 808,1648 | 1559,3604 | 1723 MB |
| Western Honey Bee | *Apis mellifera* | 5 000 | 0,29 | 3472,7533 | 5933,1321 | 2359 MB |


## 🌐 2. Full 3D Virtual World Scenario Benchmarks

| Scenario Name | Species | Nest Architecture | Entities | TPS (ticks/s) | Avg Latency (ms) | p95 Latency (ms) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Jardin Tempéré (Lasius niger) | Lasius niger | Terrier Souterrain | 1 000 | 13,90 | 71,1042 | 235,8875 |
| Forêt Épicéa (Formica rufa) | Formica rufa | Dôme de Pin | 2 500 | 7,03 | 142,2150 | 349,3849 |
| Jungle Tropicale (Atta cephalotes) | Atta cephalotes | Chambres Fongiques | 3 500 | 4,78 | 209,1593 | 378,3925 |
| Supercolonie Aride (Solenopsis invicta) | Solenopsis invicta | Supercolonie Mature | 5 000 | 1,11 | 902,2970 | 2717,3724 |


## 🖥️ 3. Headless vs Non-Headless (GUI 3D Interface) Mode Comparison

| Execution Mode | Entities | TPS (ticks/s) | FPS (Render) | Avg Latency (ms) | p95 Latency (ms) | GUI Overhead |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Headless (Backend Compute)** | 2 000 | 8,14 | N/A | 122,7952 | 330,7622 | Baseline (0%) |
| **Non-Headless (GUI Interface Graphique 3D)** | 2 000 | Infinity | 0,0 FPS | 0,0000 | 0,0000 | **+-Infinity% Overhead** |

> **Technical Note**: On systems without discrete GPU acceleration, Non-Headless GUI mode utilizes CPU software rasterization for 3D/2D views. Headless mode isolates pure simulation compute capacity for maximum throughput.

