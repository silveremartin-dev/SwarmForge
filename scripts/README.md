# 🐝 SwarmForge - Guide des Scripts & Lanceurs

Ce dossier regroupe tous les scripts d'automatisation, de compilation, d'exécution, de packaging de release et de test du projet SwarmForge, organisés par catégories sous forme de sous-dossiers dédiés.

---

## 📁 Organisation du dossier `scripts/`

```
scripts/
├── build/       # Compilation Maven, génération Javadoc, packaging et releases multi-plateformes
├── run/         # Lanceurs opérationnels (Client lourd, Client léger, Serveur, Studio, Docker, i18n)
├── test/        # Banc d'essai tout-en-un multi-clients, tests unitaires et benchmarks
├── i18n/        # Suite universelle d'audit, traduction 5 langues et couverture des infobulles
├── gcp/         # Déploiement Cloud GCP, calcul distribué batch et synchronisation
└── package-release.ps1 # Script PowerShell unifié de packaging de release multi-OS
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
| **`i18n-audit.bat`** | **`i18n-audit.sh`** | Lance l'audit complet, la synchronisation 5 langues et la détection de chaînes / tooltips. |
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

## 📦 2. Déploiement & Packaging des Releases (`scripts/build/`)

Scripts permettant de générer immédiatement des bundles autonomes prêts à l'emploi pour les 3 plateformes (Linux, macOS, Windows) ainsi qu'un package universel multi-plateformes dans `dist/release/` avec sommes de contrôle `SHA256SUMS.txt`.

| Script | Plateforme Cible | Artefacts Produits dans `dist/release/` |
| :--- | :--- | :--- |
| **`deploy-release-linux.sh`** | Linux (x64) | `SwarmForge-v<version>-Linux-x64.tar.gz` & `.zip` |
| **`deploy-release-macos.sh`** | macOS (Universal) | `SwarmForge-v<version>-macOS-Universal.tar.gz` & `.zip` |
| **`deploy-release-windows.bat`** | Windows (x64) | `SwarmForge-v<version>-Windows-x64.zip` |
| **`deploy-release-windows.sh`** | Windows (Cross-build Unix) | `SwarmForge-v<version>-Windows-x64.zip` |
| **`deploy-release-all.bat`** | **Linux + Mac + Windows** | Tous les packages OS + `SwarmForge-v<version>-AllPlatforms.zip` |
| **`deploy-release-all.sh`** | **Linux + Mac + Windows** | Tous les packages OS + `SwarmForge-v<version>-AllPlatforms.zip` |
| **`package-release.ps1`** | **Toutes plateformes** (PowerShell) | Packages selon paramètre `-Platform [windows\|linux\|macos\|all]` |
| **`build-all.bat` / `.sh`** | Tout OS | Compilation Maven complète (`mvn clean install -DskipTests`). |
| **`build-rust-native.bat` / `.sh`** | Tout OS | Compilation des bibliothèques natives Rust JNI (`swarmforge-core`). |
| **`generate-javadoc.bat` / `.sh`** | Tout OS | Génération de la Javadoc complète dans `/javadoc`. |

---

## 🧪 3. Tests & Bancs d'Essai (`scripts/test/`)

| Script Windows (`.bat`) | Script Unix (`.sh`) | Description |
| :--- | :--- | :--- |
| **`test-all-in-one.bat`** | **`test-all-in-one.sh`** | **Banc d'essai tout-en-un unifié** : configure et lance le serveur (scénario 1-16), $N$ clients Web, le Studio JavaFX et un nœud de calcul. |
| **`run-all-tests.bat`** | **`run-all-tests.sh`** | Exécute la suite complète de tests unitaires et d'intégration Maven (`mvn test`). |
| **`run-benchmarks.bat`** | **`run-benchmarks.sh`** | Compile et lance la suite de benchmarks JMH haute performance (`swarmforge-benchmarks`). |
| **`serve_web_static.py`** | — | Serveur HTTP statique léger pour les tests multi-clients. |

---

## 🌐 4. Internationalisation & Traduction (`scripts/i18n/`)

La suite `scripts/i18n/i18n_validator_and_translator.py` permet d'auditer et de synchroniser automatiquement l'ensemble des traductions du projet sur les **5 langues officielles** (**FR, EN, DE, ES, ZH**) pour tous les modules (`swarmforge-editor`, `swarmforge-client`, `swarmforge-core`, `swarmforge-web`).

| Option / Commande | Description |
| :--- | :--- |
| **`scripts\run\i18n-audit.bat`** / **`.sh`** | Lance l'audit et la synchronisation complète sur tous les modules. |
| `python scripts/i18n/i18n_validator_and_translator.py --audit` | Détecte les chaînes Java codées en dur, vérifie la complétude des bundles et la couverture des infobulles (tooltips). |
| `python scripts/i18n/i18n_validator_and_translator.py --sync` | Synchronise et propage les clés manquantes avec le lexique entomologique & biologique vers toutes les langues. |
| `python scripts/i18n/i18n_validator_and_translator.py --fix-encoding` | Corrige les corruptions d'encodage multi-octets UTF-8 / mojibake dans les sources Java. |
| `python scripts/i18n/i18n_validator_and_translator.py --all` | Exécute la réparation d'encodage, la synchronisation universelle et l'audit. |

---

## ☁️ 5. Déploiement Google Cloud Platform (`scripts/gcp/`)

Scripts d'orchestration pour le calcul massivement distribué sur GCP (Batch API, VM Compute Engine, synchronisation de données).

| Script | Description |
| :--- | :--- |
| **`deploy-gcp.sh`** | Déploiement automatisé du cluster SwarmForge complet (Master Server, 2 Nœuds Compute, PostgreSQL, Redis, Web) sur une VM GCP. |
| **`create-infra.sh`** | Provisionne l'infrastructure VM Compute Engine sur GCP. |
| **`start-vm.sh` / `stop-vm.sh` / `status-vm.sh`** | Contrôle du cycle de vie de la VM de calcul GCP. |
| **`sync-code.sh` / `sync-and-rebuild.sh`** | Synchronisation du code source local vers la VM GCP et recompilation. |
| **`logs-cloud.sh`** | Streaming des logs du cluster SwarmForge distant en temps réel. |
| **`fetch-snapshots.sh`** | Rapatriement des captures et snapshots de simulation depuis le Cloud. |
| **`batch/`** | Scripts de soumission et monitoring de jobs massifs sur GCP Batch API. |
