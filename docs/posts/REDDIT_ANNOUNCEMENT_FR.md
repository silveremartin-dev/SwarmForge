# Annonce Reddit (Français)

**Subreddits recommandés :**
- `r/france`
- `r/developpeurs`
- `r/france_science`
- `r/jeuxvideo`
- `r/programmation`

---

## 🐜 Titres suggérés

1. **[Projet Open-Source]** *SwarmForge v2.0 : J'ai créé un moteur de simulation 3D haute performance d'insectes sociaux (fourmis, abeilles, termites) avec thermodynamique de nid et IA multi-agents — Sortie en version autonome 1-clic !*
2. **[Dev / Simulation]** *SwarmForge : Plateforme de recherche et de simulation écologique en temps réel (Artemis ECS, diffusion de phéromones GPU OpenCL, 1M+ agents) — Package autonome disponible sans prérequis.*

---

## 📝 Corps du post

Bonjour à toutes et à tous ! 👋

Après plusieurs mois/années de R&D, j'ai le plaisir de vous présenter la version 2.0 de **SwarmForge**, une plateforme open-source de simulation et de recherche dédiée à la modélisation des **sociétés d'insectes eusociaux** (*Atta*, *Apis*, *Vespula*, *Reticulitermes*, etc.) couplant rendu 3D temps réel, biologie computationnelle et architecture distribuée.

Grande nouveauté avec cette version : un **package autonome 1-clic (Windows Standalone)** avec JRE intégrée, prêt à l'emploi sans nécessiter l'installation de Java ou Maven ! 🚀

---

### 🌟 Ce que propose SwarmForge

SwarmForge combine un Studio graphique complet (JavaFX + jMonkeyEngine 3.6) et un moteur de simulation scientifique haute performance :

- 🔬 **Biologie 100% Data-Driven (Zéro constante en dur)** : Durée de vie, cinétique thermique Arrhenius ($Q_{10} = 2.2$), taux de ponte, maturation par stade larvaire (jours $\rightarrow$ 1440 ticks/jour), forces mandibulaires en MPa et seuils nutritionnels par caste entièrement paramétrables.
- ⚡ **Moteur Artemis-odb ECS & Architecture SoA** : Structure of Arrays sans allocation mémoire superflue, éthologie encodée sur des masques de bits de 256 bits couvrant plus de 220 comportements comportementaux, et requêtage spatial en $O(1)$.
- 🌋 **Éditeur de Monde & Terrarium 3D Voxel** : Synthèse procédurale de terrain, humidité du sol, strates géologiques, nappe phréatique et plans de coupe souterrains pour observer l'intérieur du nid.
- 🌡️ **Thermodynamique de Nid & Mécanique des Fluides** : Ventilation passive par tirage thermique (effet de cheminée), diffusion de chaleur de Fourier 1D, dispersion du $CO_2$ métabolique et régulation thermique collective.
- 🧪 **Diffusion de Phéromones par GPU (OpenCL / TornadoVM)** : Évaporation, diffusion matricielle 3D et chimiotaxie pour le traçage des pistes en temps réel.
- 🧠 **Architectures Cognitives Multi-Niveaux** : BDI (Belief-Desire-Intention), machines à états finis cadencées, logique floue, arbres de comportement et inférence ONNX / PyTorch.
- 🌐 **Megaterrarium Distribué & Multijoueur** : Découpage de monde par nœuds de calcul, échange de phéromones aux frontières, diplomatie et convois de tributs entre colonies.

---

### 📊 Performances & Passage à l'échelle

| Taille de Colonie | Débit Moteur | Latence Tick | Profil d'exécution |
| :--- | :--- | :--- | :--- |
| **1 000 individus** | **61,7 TPS** | 16,2 ms | 🟢 Temps réel interactif fluide (60 FPS) |
| **10 000 individus** | **1,0 TPS** | ~1 048 ms | 🟡 Évaluation complète locale |
| **1 000 000 individus** | Mode Megacolonie | *Off-Grid* | ⚙️ Mode Headless SoA (~32 Mo de RAM) |

---

### ⚡ Téléchargement Immédiat & Déploiement Autonome

Aucun outil de dev requis :

1. **Téléchargez l'archive autonome Windows** :
   👉 [SwarmForge-v2.0.0-Windows-x64-Standalone.zip](https://github.com/swarmforge/swarmforge/releases)
2. **Dézippez où vous voulez** et lancez `SwarmForge.exe` (ou exécutez `Install-Shortcuts.bat` pour créer automatiquement les raccourcis Bureau et Menu Démarrer).
3. **Serveur & Docker** : Également disponible pour serveurs Linux/macOS via Docker (`docker compose up -d`) et archive serveur multi-plateforme.

---

### 🔗 Liens du projet

- 💻 **Dépôt GitHub** : [github.com/swarmforge/swarmforge](https://github.com/swarmforge/swarmforge)
- 📦 **Releases & Téléchargement direct** : [github.com/swarmforge/swarmforge/releases](https://github.com/swarmforge/swarmforge/releases)
- 📖 **Spécifications Éthologiques & Documentation** : [docs/BEHAVIORAL_ETHOLOGY_SPECIFICATION.md](https://github.com/swarmforge/swarmforge/tree/main/docs)
- 📜 **Licence** : MIT (Libre & Open Source)

Hâte d'avoir vos retours, avis biologiques, et suggestions pour les prochains biomes et espèces ! 🐜🐝🪵
