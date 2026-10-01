# 🐝 SwarmForge - Guide des Scripts & Lanceurs

Ce dossier regroupe tous les scripts d'automatisation, de compilation, d'exécution et de test du projet SwarmForge, organisés par catégories sous forme de sous-dossiers dédiés.

---

## 📁 Organisation du dossier `scripts/`

```
scripts/
├── build/       # Compilation Maven, génération Javadoc, packaging et releases
├── run/         # Lanceurs opérationnels (Client lourd, Client léger, Serveur, Studio, Docker)
├── test/        # Banc d'essai tout-en-un multi-clients, tests unitaires et benchmarks
└── gcp/         # Déploiement Cloud GCP, calcul distribué batch et synchronisation
```

---

## 🚀 1. Exécution & Lanceurs (`scripts/run/`)

Tous les scripts `.bat` sont 100% natifs en pur Batch Windows sans dépendance PowerShell.

| Script Windows (`.bat`) | Script Unix (`.sh`) | Description |
| :--- | :--- | :--- |
| **`run-client.bat`** | **`run-client.sh`** | Démarre le **Client Lourd seul** (Viewer 3D, module `swarmforge-client`). |
| **`run-client-and-server.bat`** | **`run-client-and-server.sh`** | Démarre le **Client Lourd AVEC le Serveur** (`swarmforge-server` + `swarmforge-client`). |
| **`run-web.bat`** | **`run-web.sh`** | Démarre le **Client Léger seul** (serveur HTTP SPA sur `http://localhost:5173`). |
| **`run-web-and-server.bat`** | **`run-web-and-server.sh`** | Démarre le **Client Léger AVEC le Serveur** (`swarmforge-server` + `swarmforge-web`). |
| **`run-server.bat`** | **`run-server.sh`** | Démarre le **Serveur de Simulation** (GUI par défaut, options `--nogui`, `--scenario <ID>`, `--postgres`). |
| **`run-editor.bat`** | **`run-editor.sh`** | Démarre l'**Éditeur Studio autonome** (JavaFX / jMonkeyEngine, module `swarmforge-editor`). |
| **`run-computenode.bat`** | **`run-computenode.sh`** | Démarre un **Nœud de calcul headless** (`swarmforge-compute`). |
| **`start-docker.bat`** | **`start-docker.sh`** | Démarre l'infrastructure PostgreSQL et Redis via Docker Compose. |
| **`web_server.py`** | — | Serveur HTTP local Python servant `swarmforge-web/dist` sur le port `5173`. |

### ⚙️ Options du Serveur (`run-server.bat` / `run-server.sh`)

Par défaut, le serveur se lance avec sa **console d'administration graphique (GUI)**, permettant de surveiller en direct les simulations actives, les connexions gRPC / WebSocket et les métriques de tick.

```cmd
:: 1. Démarrage standard avec GUI d'administration
scripts\run\run-server.bat

:: 2. Démarrage en mode console Headless (sans GUI, idéal pour serveurs distants/CI)
scripts\run\run-server.bat --nogui

:: 3. Démarrage direct avec un scénario de recherche spécifique pré-chargé (1 à 16)
scripts\run\run-server.bat --scenario 4

:: 4. Démarrage combiné (Headless + Scénario 6 + BD PostgreSQL Docker)
scripts\run\run-server.bat --nogui --scenario 6 --postgres
```

### 🔬 Scénarios Académiques Disponibles (IDs 1 à 16)

| ID | Code Scénario | Description Scientifique |
| :---: | :--- | :--- |
| **1** | `ACAD_01_LEVY_BROWNIAN` | Marches aléatoires de Lévy vs Mouvement brownien (optimisation du foraging) |
| **2** | `ACAD_02_POLYETHISM_BDI` | Polyéthisme d'âge et spécialisation des tâches par architecture BDI |
| **3** | `ACAD_03_NEST_MORPHOGENESIS` | Morphogenèse 3D du nid et microclimat thermodynamique |
| **4** | `ACAD_04_INTERSPECIFIC_COMPETITION` | Compétition territoriale inter-espèces (*Lasius niger* vs *Linepithema humile*) *(Défaut)* |
| **5** | `ACAD_05_TROPHALLAXIS` | Dynamique des flux nutritifs stomodéaux et trophallaxie collective |
| **6** | `ACAD_06_EPIDEMIOLOGY_QUARANTINE` | Dynamique SIR épidémiologique, auto-quarantaine et allotoilettage |
| **7** | `ACAD_07_ATTINE_FUNGI` | Symbiose champignonnière des fourmis coupeuses de feuilles *Atta* |
| **8** | `ACAD_08_STIGMERGIC_PHEROMONES` | Stigmergie chimique et résolution géométrique de labyrinthes |
| **9** | `ACAD_09_DULOSIS_RAID` | Raids esclavagistes de *Polyergus rufescens* sur *Formica fusca* |
| **10** | `ACAD_10_SAVANNA_COEVOLUTION` | Adaptation thermo-tolérante tropicale en savane (Serengeti) |
| **11** | `ACAD_11_ALPINE_THERMOREGULATION` | Cryo-tolérance et thermorégulation de haute altitude |
| **12** | `ACAD_12_BOREAL_SOLAR_DOMES` | Dômes d'aiguilles solaires et piégeage thermique en forêt boréale |
| **13** | `ACAD_13_STEPPE_HARVESTING` | Granivorie, stockage de graines et xérotolérance steppique |
| **14** | `ACAD_14_WETLAND_FLOOD_RAFTING` | Auto-assemblage en radeaux vivants insubmersibles (*Solenopsis invicta*) |
| **15** | `ACAD_15_WASP_WILD_BEEHIVE` | Prédation et défense coloniale : Guêpier suspendu vs Ruche sauvage |
| **16** | `ACAD_16_APICULTURAL_APIARY` | Rucher moderne Dadant, régulation du couvain et butinage |

---

## 🔨 2. Compilation & Packaging (`scripts/build/`)

| Script Windows (`.bat`) | Script Unix (`.sh`) | Description |
| :--- | :--- | :--- |
| **`build-all.bat`** | **`build-all.sh`** | Compilation Maven complète de tous les modules du projet. |
| **`package_client.bat`** | **`package_client.sh`** | Packaging des artefacts du client lourd (`swarmforge-client`). |
| **`build-rust-native.bat`** | **`build-rust-native.sh`** | Compilation des bibliothèques d'accélération Rust natives (`swarmforge-core/src/main/rust`). |
| **`generate-javadoc.bat`** | **`generate-javadoc.sh`** | Génère la documentation Javadoc agrégée et par module dans `/javadoc`. |
| **`package-release.bat`** | **`package-release.sh`** | Génère le bundle de release serveur autonome compressé (`.zip`) dans `dist/release/`. |

---

## 🧪 3. Tests & Bancs d'Essai (`scripts/test/`)

| Script Windows (`.bat`) | Script Unix (`.sh`) | Description |
| :--- | :--- | :--- |
| **`test-all-in-one.bat`** | **`test-all-in-one.sh`** | **Banc d'essai tout-en-un unifié** : configure et lance le serveur (scénario 1-16), $N$ clients Web, le Studio JavaFX et un nœud de calcul. |
| **`run-all-tests.bat`** | **`run-all-tests.sh`** | Exécute la suite complète de tests unitaires et d'intégration Maven (`mvn test`). |
| **`run-benchmarks.bat`** | **`run-benchmarks.sh`** | Compile et lance la suite de benchmarks JMH haute performance (`swarmforge-benchmarks`). |
| **`serve_web_static.py`** | — | Serveur HTTP statique léger pour les tests multi-clients. |

---

## ☁️ 4. Déploiement Google Cloud Platform (`scripts/gcp/`)

Scripts d'orchestration pour le calcul massivement distribué sur GCP (Batch API, VM Compute Engine, synchronisation de données).
