# 🌀 Simulation Loop & Environmental Parameter Specification

This document provides a detailed specification of the **SwarmForge** temporal simulation loop architecture, multi-scale tick sequencing, and all physical, biological, biomechanical, and sensory parameters standardized in SI metric units.

---

## 1. Simulation Loop Architecture

The SwarmForge simulation loop (`Simulation.java` / `World.java`) executes at a configurable fixed frequency (default: 60 ticks per second, or headless accelerated mode reaching thousands of ticks/sec) powered by **Java 21 Virtual Threads** (`Executors.newVirtualThreadPerTaskExecutor()`).

```
                           ┌──────────────────────────────────────────┐
                           │      Temporal Cadence (Tick Loop)        │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │        1. WeatherSystem (Climate)        │
                           │ Temperature, Rain, Wind, Solar, Seasons  │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │    2. Physical & Subterranean Systems    │
                           │ Nest Microclimate, Soil Mohr-Coulomb     │
                           │ Stability, Symbiotic Fungus Gardens      │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │     3. Organisms & Colonies (Agents)     │
                           │ FSM/BDI Behaviors, AI, Pathfinding,      │
                           │ Sensory Grids (Visual, Magnetic, Chem)   │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │    4. Voxel Grid / Terrarium Updates     │
                           │ Pheromone Diffusion, Gas Equilibrium     │
                           │ CO2/O2 Respiration, Water Evaporation    │
                           └────────────────────┬─────────────────────┘
                                                │
                                                ▼
                           ┌──────────────────────────────────────────┐
                           │ 5. Network Sync & 3D Rendering (gRPC/JME)│
                           │ Protobuf/FlatBuffers, WebSockets, JME3   │
                           └──────────────────────────────────────────┘
```

### Detailed Tick Sequencing (`tickCount`):
1. **Atmospheric & Climate Update (`WeatherSystem`)**:
   - Computes solar trajectory and azimuthal inclination according to the time of day ($0.0\,\text{h} - 24.0\,\text{h}$) and day of year.
   - Markov chain weather state transitions (`CLEAR`, `CLOUDY`, `RAIN`, `THUNDERSTORM`, `HAIL`, `SNOW`, `TEMPEST`).
   - Dynamically updates wind speed vectors ($\text{m/s}$), relative humidity ($\%$), geomagnetic field ($\mu\text{T}$), and precipitation volume.
2. **Subterranean Physical Systems**:
   - `SoilStructureSystem`: Evaluates tunnel stability (Mohr-Coulomb failure criterion, soil cohesion, vertical overburden stress).
   - `NestMicroclimateSystem`: Models collective colony respiration ($\text{CO}_2$ production per worker, $\text{O}_2$ consumption), triggering ventilation shaft excavation if $\text{CO}_2 > 2.5\%$.
   - `FungusGardenSystem`: Simulates symbiotic fungal cultivar growth and substrate decay (*Atta*, *Acromyrmex*, *Macrotermes*).
3. **Agent & Colony Processing (`Colony`, `Individual`, `EcsEngine`)**:
   - Parallel evaluation of multi-agent cognitive architectures (FSM, Behavior Trees, BDI, flight kinematics, thigmotaxis).
   - Multi-channel sensory processing: geomagnetic field orientation, thermal gradients, $\text{CO}_2$ levels, trail pheromones.
4. **Voxel Grid Physics & Diffusion (`TerrariumCell`)**:
   - 3D numerical pheromone diffusion across 8 distinct chemical channels, subject to wind advection and substrate porosity.
   - Inter-cell gaseous equilibrium and heat conduction between adjacent voxels.
5. **Network Telemetry & Rendering Synchronization**:
   - Incremental entity deltas serialized via Protobuf / FlatBuffers.
   - Real-time gRPC stream broadcast and jMonkeyEngine 3.6 GPU viewport synchronization.

---

## 2. Physical Parameters & SI Units of the Voxel Grid (`TerrariumCell`)

The voxel cell (`TerrariumCell`) represents the fundamental spatial discretization unit of the simulation ($\le 1.0\,\text{mm}^3$).

| Parameter | SI Metric Unit / Format | Description & Biological Effect |
| :--- | :--- | :--- |
| **Spatial Resolution** | $\le 1.0\,\text{mm}^3$ | Reference voxel volume and grid resolution in the 3D terrarium. |
| **Substrate Material** | Enum (`Material`) | `AIR`, `EARTH`, `SAND`, `ROCK`, `WOOD`, `WATER`, `SNOW`, `ICE`, `DEAD_ORGANISM`. |
| **Temperature** | Kelvin ($\text{K}$) / $^{\circ}\text{C}$ | Governs ectothermic metabolic rates ($Q_{10}$ kinetics), locomotion speed, and critical thermal limits. |
| **Relative Humidity** | Percentage ($\%$) | Modulates cuticle water loss, egg desiccation risks, and fungal growth. |
| **$\text{CO}_2$ (Carbon Dioxide)** | $\text{ppm}$ / Percentage ($\%$) | Colony respiration byproduct. Triggers hyperpnea, fanning behavior, and chimney excavation. |
| **$\text{O}_2$ (Dioxygen)** | Percentage ($\%$) | Tunnel ventilation adequacy index; hypoxemia triggers emergency digging. |
| **$\text{N}_2\text{O}$ (Nitrous Oxide)** | $\text{ppm}$ | Subterranean soil microbial emissions and anaerobic decomposition. |
| **Illuminance / Light** | Lux / $[0.0, 1.0]$ | Solar irradiation and subterranean darkness; drives circadian and nycthemeral rhythms. |
| **Geomagnetic Vector** | Microtesla ($\mu\text{T}$) | Triaxial magnetic field vector ($B_x, B_y, B_z$) for mound alignment and blind orientation. |
| **Wind Velocity** | Meters per second ($\text{m/s}$) | 3D wind velocity vector modulating aerial dispersion of pheromone plumes and flight drag. |
| **Atmospheric Pressure** | Pascal ($\text{Pa}$) / $\text{hPa}$ | Hydrostatic and barometric pressure correlated with subterranean depth and altitude. |
| **Pheromone Matrix** | `float[8]` array | 8 chemical channels (Food, Home, Alarm, Territory, Brood, Queen, Necrophoric, Aggression). |

---

## 3. Sensory Capabilities & Modalities (`Species` & `CustomSpecies`)

Every eusocial insect species and polymorphic caste (Ants, Bees, Wasps, Termites) possesses a comprehensive physiological sensory profile:

### 🧲 1. Magnetoreception (`hasMagnetoreception`, `magnetoreceptionSensitivity`)
- **Biological Basis**: Termites (*Reticulitermes flavipes*, *Macrotermes*, *Amitermes meridionalis* - "magnetic termites") and select ant species utilize biogenic magnetite ($\text{Fe}_3\text{O}_4$) crystals in antenna/abdomen.
- **Function in SwarmForge**:
  - Directs North-South planar orientation of wedge mounds for passive thermoregulation.
  - Enables dead-reckoning navigation in dark subterranean tunnels lacking visual or pheromone cues.

### 🌡️ 2. Thermoreception (`thermoreceptionSensitivity`)
- **Thermal Gradient Sensitivity** ($^{\circ}\text{C}$ / $\text{K}$).
- Drives brood relocation behavior: workers transport eggs, larvae, and pupae to optimal incubation chambers ($24^{\circ}\text{C} - 28^{\circ}\text{C}$).

### 💨 3. Chemoreception & Gas Sensing (`gasSensitivityCo2Ppm`)
- **$\text{CO}_2$ & VOC Detection Threshold** ($\text{ppm}$).
- Triggers emergency ventilation shaft excavation and fanning when subterranean nest chamber $\text{CO}_2$ exceeds $2.5\%$.

### 👁️ 4. Photoreception & Compound Vision (`visualAcuity`, `minLightLevelThreshold`)
- **Visual Acuity & Minimum Illuminance Threshold** (Lux).
- Accurately differentiates surface foraging compound eyes (*Cataglyphis*, *Formica*, *Apis*) from microphthalmic or eyeless subterranean workers (*Dorylus*, *Reticulitermes*).

### 🔊 5. Substrate Vibration Sensing (`hasSubstrateVibrationSensing`, `vibrationSensitivityDb`)
- **Subgenual Organs & Johnston's Organ** (Sensitivity in $\text{dB}$).
- Detects substrate head/gaster drumming alarms (*Camponotus*, *Reticulitermes*) and comb vibration acoustics (*Apis mellifera* waggle dance).

### 💧 6. Hygroreception (`hasHygroreception`, `hygroreceptionSensitivityPercent`)
- **Relative Humidity Gradient Sensitivity** ($\%$).
- Governs nursery chamber selection and protects vulnerable brood from lethal desiccation.

### ⚡ 7. Atmospheric Electroreception (`hasElectrosensing`, `electroceptionSensitivityVolts`)
- **Electrostatic Field Perception** ($\text{V/m}$).
- Utilized by bees and wasps to sense floral electrical charges, pollen adhesion potentials, and impending thunderstorm fronts.

### ☀️ 8. Celestial Polarized Light Navigation (`hasPolarizedLightNavigation`)
- **Dorsal Rim Area (DRA) & Ocelli**.
- UV polarized celestial e-vector path integration (*dead reckoning*) for long-distance foraging (*Cataglyphis*, *Apis*).

---

## 4. Biomechanical & Motor Systems (`Species` & `CustomSpecies`)

| Motor Parameter | SI Unit / Type | Biological Function by Clade (Ants, Bees, Wasps, Termites) |
| :--- | :--- | :--- |
| **Wing Beat Frequency** | Hertz ($\text{Hz}$) | Asynchronous flight muscle oscillation ($180 - 250\,\text{Hz}$ in Apidae, Vespidae, and alate reproductives). |
| **Hovering Capability** | Boolean | Stationary aerial sustentation in bees and hunting wasps. |
| **Payload Capacity Ratio** | Dimensionless ($\times\,\text{body mass}$) | Transportable cargo multiplier ($10\times - 50\times$ in Formicidae; $0.8\times - 1.5\times$ in Apidae). |
| **Mandibular Biting Pressure**| Megapascals ($\text{MPa}$) | Substrate shearing: wood boring ($20\,\text{MPa}$), leaf shearing (*Atta*: $30\,\text{MPa}$), paper mastication (*Vespula*: $15\,\text{MPa}$). |
| **Explosive Autothysis** | Boolean | Suicidal glandular rupture defense (*Colobopsis explodens*, *Globitermes sulfureus*). |
| **Tarsal Arolia Adhesion** | Boolean | Wet adhesive pads enabling vertical climbing and inverted locomotion on smooth surfaces. |

---

## 5. System Consistency & Validation
The `swarmforge-core` simulation pipeline, `swarmforge-compute` distributed offloading, and `swarmforge-editor` visual studio maintain 1:1 parameter synchronization, ensuring scientific accuracy between 3D particle physics and biological swarm ethology.
