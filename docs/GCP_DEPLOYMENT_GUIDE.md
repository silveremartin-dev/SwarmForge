# ☁️ SwarmForge - Guide de Déploiement & Clustering Google Cloud (GCP)

Ce guide décrit l'architecture, le déploiement automatisé et le cycle de développement itératif (*hot sync & rebuild*) pour faire tourner SwarmForge sur **Google Cloud Compute Engine**.

---

## 🏗️ Architecture Déployée

```
                                  GOOGLE CLOUD (GCP)
┌───────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                       │
│   ┌─────────────────────┐   ┌───────────────────────┐   ┌─────────────────────────┐  │
│   │   PostgreSQL 16     │   │     Redis 7 Cache     │   │   SwarmForge Server     │  │
│   │  (Snapshots/Stats)  │◄──┼── (Positions/Streams) ┼──►│  (Master Engine / gRPC) │  │
│   └─────────────────────┘   └───────────────────────┘   └───────────┬─────────────┘  │
│                                                                     │ gRPC (50051)    │
│                                                    ┌────────────────┴─────────────┐  │
│                                                    ▼                              ▼  │
│                                      ┌────────────────────────┐     ┌───────────────────────┐│
│                                      │ Compute Worker Node #1 │     │ Compute Worker Node #2││
│                                      │ (Diffusions 3D / ECS)  │     │ (Diffusions 3D / ECS) ││
│                                      └────────────────────────┘     └───────────────────────┘│
└──────────────────────────────────────────────────▲────────────────────────────────────┘
                                                   │ gRPC Stream / REST Export / Checkpoints
                                                   │
                                      ┌────────────┴─────────────┐
                                      │   POSTE LOCAL (Windows)  │
                                      │  SwarmForge Studio / 3D  │
                                      │ (Replay & Visualisation) │
                                      └──────────────────────────┘
```

---

## 🛠️ Boîte à Outils des Scripts (`scripts/gcp/`)

Tous les scripts sont disponibles en version **PowerShell (Windows)** et **Bash (Linux / Cloud Shell)**.

### 1. Gestion de l'Infrastructure & Cycle de Vie VM
* **Créer l'infrastructure (Firewall + VM)** :
  - Windows : `.\scripts\gcp\create-infra.ps1`
  - Cloud Shell : `./scripts/gcp/create-infra.sh`
* **Vérifier l'état & IP publique** :
  - Windows : `.\scripts\gcp\status-vm.ps1`
  - Cloud Shell : `./scripts/gcp/status-vm.sh`
* **Mettre en pause la VM (arrête la facturation)** :
  - Windows : `.\scripts\gcp\stop-vm.ps1`
  - Cloud Shell : `./scripts/gcp/stop-vm.sh`
* **Rallumer la VM** :
  - Windows : `.\scripts\gcp\start-vm.ps1`
  - Cloud Shell : `./scripts/gcp/start-vm.sh`
* **Supprimer définitivement l'infrastructure** :
  - Windows : `.\scripts\gcp\delete-vm.ps1`
  - Cloud Shell : `./scripts/gcp/delete-vm.sh`

---

### 2. Développement Itératif & Rebuild Rapide (Hot Sync)
Lorsque vous modifiez du code en local et souhaitez le re-déployer immédiatement sur la VM :

* **Synchroniser le code local & Rebuilder le cluster en 1 clic** :
  ```powershell
  .\scripts\gcp\sync-and-rebuild.ps1
  ```
  *(Exclut automatiquement `target/`, `node_modules/`, `.git`, compresse le code source, l'envoie sur la VM et relance `docker compose build && up`)*.

* **Rebuilder un service spécifique (ex: server ou compute)** :
  ```powershell
  .\scripts\gcp\rebuild-cloud.ps1 -Service server
  ```

* **Suivre les logs en direct (streaming)** :
  ```powershell
  .\scripts\gcp\logs-cloud.ps1 -Service server
  ```

* **Réinitialiser la base de données & Redis** :
  ```powershell
  .\scripts\gcp\clean-db.ps1
  ```

---

### 3. Exécution de Scénarios, Benchmarks & Replay Local

* **Lancer un scénario headless sur le Cloud** :
  ```powershell
  .\scripts\gcp\run-scenario.ps1 -Scenario 1
  ```

* **Lancer la suite de benchmarks officielle** :
  ```powershell
  .\scripts\gcp\run-benchmark.ps1
  ```

* **Télécharger les snapshots pour les rejouer dans l'éditeur local** :
  ```powershell
  .\scripts\gcp\fetch-snapshots.ps1
  ```

---

## 🔒 Sécurité & `.gitignore`
Les fichiers de secrets suivants sont systématiquement exclus des commits :
* `*credentials*.json`, `*service-account*.json`, `.env*`
* `*.sql`, `*.dump`, `snapshots_export.sql`
* `*.pem`, `*.key`, `id_rsa*`
