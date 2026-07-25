# 🌀 Boucle de Simulation & Spécification des Paramètres Environnementaux

Ce document détaille l'architecture de la boucle de simulation temporelle de **SwarmForge**, son séquençage multi-échelles par tick, ainsi que l'intégralité des paramètres physiques, biologiques et sensoriels gérés en unités SI.

---

## 1. Architecture de la Boucle de Simulation

La boucle de simulation (`Simulation.java`) s'exécute à fréquence fixe (par défaut 60 ticks par seconde ou mode accéléré à plusieurs milliers de ticks/sec) sur des **Virtual Threads Java 21**.

```
                           ┌──────────────────────────────────────────┐
                           │      Cadenceur Temporel (Tick Loop)      │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │        1. WeatherSystem (Climat)         │
                           │ Température, Pluie, Vent, Soleil, Saison │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │  2. Systèmes Physiques & Sous-Sols       │
                           │ Microclimat Nids, Stabilité Sols (Mohr-   │
                           │ Coulomb), Jardins à Champignons (Atta)   │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │     3. Organismes & Colonies (Agents)    │
                           │   FSM Comportements, IA, Déplacements,   │
                           │  Sensors (Vision, Magnétique, Chimique)  │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │    4. Voxel Grid / Terrarium Updates     │
                           │ Diffusion Phéromones, Échanges de Gaz,   │
                           │ Accumulation CO2/O2, Évaporations Eau    │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │ 5. Sync Réseau & Rendu 3D (RFI / JME)    │
                           │ Protobuf, WebSockets, Three.js, JME3     │
                           └──────────────────────────────────────────┘
```

### Séquençage détaillé d'un Tick (`tickCount`) :
1. **Mise à jour Climatique (`WeatherSystem`)** :
   - Trajectoire solaire & inclinaison azimuthale calculées selon l'heure de la journée (0.0h à 24.0h) et le jour de l'année.
   - Chaine de Markov d'états météo (`CLEAR`, `CLOUDY`, `RAIN`, `THUNDERSTORM`, `HAIL`, `SNOW`, `TEMPEST`).
   - Ajustement dynamique du vent (m/s), de l'humidité relative (%), du champ magnétique ($\mu\text{T}$) et des précipitations.
2. **Systèmes Physiques Sous-Souterrains** :
   - `SoilStructureSystem` : Évaluation de la stabilité des galeries (Critère de rupture de Mohr-Coulomb, cohésion du sol, contrainte verticale).
   - `NestMicroclimateSystem` : Simulation de la respiration de la colonie (production de CO₂ par ouvrière, consommation d'O₂), déclenchant l'excavation de conduits de ventilation si $\text{CO}_2 > 2.5\%$.
   - `FungusGardenSystem` : Croissance des jardins du champignon symbiotique (chez les fourmi/termites coupeuses de feuilles *Atta/Acromyrmex/Macrotermes*).
3. **Mise à jour des Agents (`Colony` & `Individual`)** :
   - Traitement parallèle des cerveaux d'insects (FSM, arbres de comportement, vol, thigmotaxie).
   - Prise en compte des informations sensoriels : perception du champ magnétique terrestre, gradients de température, niveau de CO₂, phéromones.
4. **Diffusion & Physique des Cellules Voxels (`TerrariumCell`)** :
   - Équation de diffusion des phéromones (8 canaux distincts) et dissipation par le vent.
   - Équilibre gazeux et gradients de température entre voxels adjacents.

---

## 2. Paramètres Physiques & Unités SI de la Grille Voxel (`TerrariumCell`)

La cellule (`TerrariumCell`) représente la résolution spatiale fondamentale de la simulation ($\le 1.0\text{ mm}^3$).

| Paramètre | Unité SI / Format | Description & Effet Biologique |
| :--- | :--- | :--- |
| **Résolution Spatiale** | $\le 1.0\text{ mm}^3$ | Taille et volume de référence d'un voxel dans la grille 3D. |
| **Matériaux** | Enum (`Material`) | `AIR`, `EARTH`, `SAND`, `ROCK`, `WOOD`, `WATER`, `SNOW`, `ICE`, `DEAD_ORGANISM`. |
| **Température** | Kelvin ($\text{K}$) / $^{\circ}\text{C}$ | Influe sur le métabolisme, la vitesse de déplacement et la survie. |
| **Humidité Relative** | Pourcentage ($\%$) | Détermine l'évapotranspiration et les besoins en eau des couvain. |
| **CO₂ (Dioxyde de Carbone)** | $\text{ppm}$ / Pourcentage ($\%$) | Respiration de la colonie. Déclenche l'hyperpnée et la ventilation des nids. |
| **O₂ (Dioxygène)** | Pourcentage ($\%$) | Taux de ventilation des galeries souterraines. |
| **N₂O (Protoxyde d'Azote)** | $\text{ppm}$ | Gaz d'origine microbienne du sol. |
| **Lumière / Éclairement** | Lux / $[0.0, 1.0]$ | Niveau d'éclairement solaire ou de pénombre (influence les rythmes meutes/nycthéméraux). |
| **Champ Magnétique** | Microtesla ($\mu\text{T}$) | Vecteur d'orientation géomagnétique ($B_x, B_y, B_z$) utilisé pour l'orientation des monticules. |
| **Vent** | Mètres par seconde ($\text{m/s}$) | Vecteur de vitesse du vent ($v_x, v_y$) influençant la dérive des phéromones. |
| **Pression Atmosphérique** | Pascal ($\text{Pa}$) / $\text{hPa}$ | Pression hydrostatique et de l'air liée à la profondeur ou l'altitude. |
| **Phéromones** | Tableau `float[8]` | 8 canaux de pistes chimiques (nourriture, alarme, territoire, reine, cadavre, etc.). |

---

## 3. Paramètres Sensoriels des Espèces (`Species` & `CustomSpecies`)

Chaque espèce ou caste d'insectes eusociaux (Fourmis, Abeilles, Guêpes, Termites) dispose d'un profil sensoriel complet :

### 🧲 1. Magnétoréception (`hasMagnetoreception`, `magnetoreceptionSensitivity`)
- **Biological Basis** : Les termites (ex: *Reticulitermes flavipes*, *Macrotermes*, *Amitermes meridionalis* - "compass termites") et certaines espèces de fourmis possèdent des récepteurs magnétiques (particules de magnétite $\text{Fe}_3\text{O}_4$ dans les antennes ou l'abdomen).
- **Function in SwarmForge** :
  - Alignement nord-sud des monticules et cathédrales de termites pour l'optimisation thermorégulatrice.
  - Orientation dans l'obscurité totale des galeries souterraines en l'absence de repères visuels ou phéromonaux.

### 🌡️ 2. Thermoréception (`thermoreceptionSensitivity`)
- **Sensibilité au Gradient Thermique** ($^{\circ}\text{C}$ / $\text{K}$).
- Permet aux ouvrières de déplacer le couvain (œufs, larves, nymphes) vers les chambres d'incubation dont la température est optimale ($24^{\circ}\text{C} - 28^{\circ}\text{C}$).

### 💨 3. Chémioréception & Capteur de Gaz (`gasSensitivityCo2Ppm`)
- **Seuil de détection du CO₂ et des COV** ($\text{ppm}$).
- Déclenche un comportement d'urgence d'excavation de puits de ventilation lorsque la concentration de CO₂ accumulée dans les chambres profondes dépasse $2.5\%$.

### 👁️ 4. Photoréception & Vision (`visualAcuity`, `minLightLevelThreshold`)
- **Acuité Visuelle & Seuil d'Éclairement Minimal** (lux).
- Distingue les yeux composés des ouvrières de surface (fourmis moissonneuses, abeilles) et la cécité quasi-totale des ouvrières termites souterraines.

### 🔊 5. Perception des Vibrations du Substrat (`hasSubstrateVibrationSensing`, `vibrationSensitivityDb`)
- **Organes Subgénuaux & Organe de Johnston** (Sensibilité en dB).
- Percetion du tambourinage de la tête/abdomen des termites et fourmis charpentières (*Camponotus*) pour les signaux d'alarme, ainsi que la transmission acoustique de la danse frétillante (*waggle dance*) dans les rayons de cire des abeilles (*Apis mellifera*).

### 💧 6. Hygroréception (`hasHygroreception`, `hygroreceptionSensitivityPercent`)
- **Sensibilité au Gradient d'Humidité Relative** (%).
- Essentiel pour la sélection des chambres d'incubation du couvain et la prévention de la dessiccation des œufs.

### ⚡ 7. Électroréception Atmosphérique (`hasElectrosensing`, `electroceptionSensitivityVolts`)
- **Perception des Champs Électrostatiques** ($\text{V/m}$).
- Utilisé par les abeilles et les guêpes pour percevoir la charge électrique des fleurs visitées, la fixation du pollen et l'approche d'orages électrostatiques.

### ☀️ 8. Boussole Céleste & Lumière Polarisée UV (`hasPolarizedLightNavigation`)
- **Aire du Bord Dorsal (DRA) & Ocelles**.
- Navigation par intégration de chemin (*dead reckoning*) selon la polarisation UV du ciel (ex: fourmis du désert *Cataglyphis*, abeilles domestiques, guêpes vespines).

---

## 4. Systèmes Moteurs & Capacité Biomécanique (`Species` & `CustomSpecies`)

| Paramètre Moteur | Unité SI / Type | Rôle Biologique par Groupe (Abeilles, Guêpes, Fourmis, Termites) |
| :--- | :--- | :--- |
| **Battement d'Ailes** | Hertz ($\text{Hz}$) | Fréquence de battement asynchrone ($180 - 250\text{ Hz}$ pour les apidés/vespidés et alés). |
| **Vol Stationnaire** | Boolean | Capacité de sustentation aérienne fixe (*hovering*) chez les guêpes et abeilles. |
| **Ratio de Charge Transportable** | Adimensionnel ($\times\text{masse}$) | Ratio masse transportée/masse corporelle ($10\times - 50\times$ chez les fourmis; $0.8\times - 1.5\times$ chez les abeilles). |
| **Force Mandibulaire de Cisaillement** | Mégapascal ($\text{MPa}$) | Pression de coupe du bois (termites/scolytes: $20\text{ MPa}$), des feuilles (*Atta*: $30\text{ MPa}$), ou malaxage de la pâte à papier (*Vespula*: $15\text{ MPa}$). |
| **Autothysie Explosive** | Boolean | Défense suicidaire par rupture glandulaire (*Colobopsis explodens*, *Neocapritermes taracua*). |
| **Adhésion Ventouses Arolia** | Boolean | Adhésion par fluide tarsal permettant la locomotion verticale et au plafond sur parois lisses. |

---

## 5. Validation des Compilations & Tests
Les moteurs de simulation de `swarmforge-core` et l'éditeur graphique `swarmforge-editor` s'appuient sur ces définitions unifiées pour assurer une synchronisation exacte entre la physique des particules 3D et le comportement biologique des colonies.
