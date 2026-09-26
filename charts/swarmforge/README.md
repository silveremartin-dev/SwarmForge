# ⎈ SwarmForge Helm Chart

Chart Helm officiel pour le déploiement de l'écosystème de simulation **SwarmForge 1.0.0-beta.1** sur un cluster Kubernetes (**Google Kubernetes Engine (GKE)**, AWS EKS, Azure AKS, Minikube, K3s, MicroK8s).

---

## 🎯 Vue d'ensemble de l'Architecture

Le chart déploie et orchestre automatiquement l'ensemble des microservices SwarmForge :

```
                        [ Ingress (Nginx / Cloud Load Balancer) ]
                                      |
                     +----------------+----------------+
                     | (port 80: /)                    | (port 8080: /ws, /api)
                     v                                 v
        [ swarmforge-web (UI) ]          [ swarmforge-server (gRPC: 50051 / HTTP: 8080) ]
        (React 18 + Three.js 3D)                       |
                                                       +-----> [ PostgreSQL ] (Persistence & runs)
                                                       |
                                                       +-----> [ Redis ] (Spatial caching & pub/sub)
                                                       ^
                                                       | (gRPC Tick streaming)
                                        [ swarmforge-compute (x N Workers) ]
                                           (Auto-scaled with HPA)
```

### Microservices déployés :
1. **`swarmforge-server`** : Cœur de coordination gRPC (port `50051`) et passerelle WebSocket/REST (port `8080`).
2. **`swarmforge-compute`** : Nœuds de calcul headless distribués déchargeant les calculs matriciels et l'évaluation des individus. Évolutifs horizontalement (HPA).
3. **`swarmforge-web`** : Interface web interactive Three.js 3D servie via Nginx.
4. **`PostgreSQL`** : Persistance des terrariums, colonies, historiques de simulation et métriques.
5. **`Redis`** : Cache mémoire distribué et bus d'événements temps réel.

---

## 🚀 Prérequis

- **Kubernetes** 1.25+
- **Helm** 3.8+
- Un accès administrateur au cluster (`kubectl`)

---

## 📦 Installation rapide

### 1. Cloner ou naviguer dans le dossier du chart
```bash
cd charts/swarmforge
```

### 2. Mettre à jour les dépendances Helm (PostgreSQL & Redis)
```bash
helm dependency update
```

### 3. Installer le chart dans le namespace `swarmforge`
```bash
helm install swarmforge . --namespace swarmforge --create-namespace
```

---

## ⚙️ Configuration principale (`values.yaml`)

| Paramètre | Description | Valeur par défaut |
| :--- | :--- | :--- |
| `server.replicaCount` | Nombre d'instances du serveur gRPC | `1` |
| `server.service.grpcPort` | Port gRPC de communication interne | `50051` |
| `server.service.httpPort` | Port HTTP / WebSocket télémétrie | `8080` |
| `server.resources` | Requêtes & limites CPU/Mémoire du serveur | `cpu: 500m-2000m, mem: 1Gi-4Gi` |
| `compute.enabled` | Activer les workers de calcul distribué | `true` |
| `compute.replicaCount` | Nombre de workers de calcul | `2` |
| `compute.autoscaling.enabled` | Activer l'autoscaling horizontal (HPA) | `false` |
| `web.enabled` | Activer le dashboard Web Three.js | `true` |
| `web.service.port` | Port du service web | `80` |
| `ingress.enabled` | Activer l'Ingress controller | `false` |
| `postgresql.enabled` | Déployer PostgreSQL via Bitnami | `true` |
| `redis.enabled` | Déployer Redis via Bitnami | `true` |

---

## 📈 Autoscaling des Compute Workers

Pour activer l'autoscaling horizontal automatique des workers de calcul en fonction de la charge CPU/Mémoire :

```yaml
compute:
  autoscaling:
    enabled: true
    minReplicas: 2
    maxReplicas: 10
    targetCPUUtilizationPercentage: 80
```

---

## 🔄 Mise à jour & Désinstallation

### Mettre à jour la configuration :
```bash
helm upgrade swarmforge . --namespace swarmforge -f my-values.yaml
```

### Désinstaller :
```bash
helm uninstall swarmforge --namespace swarmforge
```
