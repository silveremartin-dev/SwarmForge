# 🐝 SwarmForge - Suite de Tests & Lanceurs Multi-Clients

Ce sous-répertoire contient des scripts prêts à l'emploi pour tester et valider l'architecture distribuée de SwarmForge en local avec différentes configurations de serveurs, de clients et de nœuds de calcul.

---

## 📋 Répertoire des Scripts

| Script (PowerShell / BAT) | Description | Composants Démarrés |
| :--- | :--- | :--- |
| **`test-all-in-one.ps1`** / `.bat` | **Lanceur Tout-en-Un Interactif** | Scénario au choix (1-16) + N clients Web + Client Studio + Compute Node (optionnel) |
| **`test-server-web-multi.ps1`** / `.bat` | **Test Multi-Clients Web** | Serveur Java + 2 fenêtres de navigateurs connectées en WebSocket (`:8081`) |
| **`test-server-editor-web.ps1`** / `.bat` | **Test Hybride Studio + Web** | Serveur Java + Client Web 3D + Client Studio lourd JavaFX/jMonkeyEngine |
| **`test-cluster-full.ps1`** / `.bat` | **Test Cluster Distribué Complet** | Serveur Master + Compute Node headless (gRPC) + Client Web + Client Studio |
| **`serve_web_static.py`** | **Serveur HTTP Statique (Port 5173)** | Distribution locale des bundles Web dist sans conflit de port WebSocket |

---

## 🚀 Utilisation Rapide

### 1. Lanceur Interactif Tout-en-Un
```powershell
# PowerShell
.\scripts\test\test-all-in-one.ps1

# Ou avec arguments directs :
.\scripts\test\test-all-in-one.ps1 -Scenario 1 -WebClients 2 -LaunchEditor $true
```

```cmd
:: CMD
scripts\test\test-all-in-one.bat
```

### 2. Tester 2 Clients Web sur le Scénario 2 (Foraging)
```powershell
.\scripts\test\test-server-web-multi.ps1 -Scenario 2
```

### 3. Tester le Client Lourd Studio et le Client Web en simultané
```powershell
.\scripts\test\test-server-editor-web.ps1 -Scenario 4
```

### 4. Tester le Cluster avec Nœud de Calcul Headless
```powershell
.\scripts\test\test-cluster-full.ps1 -Scenario 4
```

---

## 🌐 Ports & Réseau

- **`50051` (gRPC)** : Communication hautes performances entre le serveur, les compute nodes et le client lourd Studio.
- **`8081` (WebSocket)** : Télémétrie et diffusion temps réel des entités pour les clients Web Three.js.
- **`5173` (HTTP)** : Serveur Web distribuant l'interface utilisateur.
- **`5006` (JDWP)** : Port optionnel de débogage JVM.

---

## 🛑 Arrêt des Tests
Chaque composant s'exécute dans une fenêtre de terminal dédiée. Pour arrêter un test, fermez simplement les fenêtres de commande ou appuyez sur `Ctrl+C` dans chaque fenêtre.
