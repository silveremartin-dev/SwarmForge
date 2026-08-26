# SwarmForge Behavioral Ethology Engine Specification
**Version 2.0.0-SNAPSHOT | Author: Silvère Martin-Michiellot & Gemini AI Assistant (Google DeepMind)**

---

## Executive Summary
This document provides a comprehensive technical and biological reference for the **30 advanced eusocial insect behavioral systems** implemented within the SwarmForge simulation framework. Each behavioral system is governed by a modular capability flag registered in `Species.java` and `CustomSpecies.java`, executed through dedicated simulation subsystems in `org.swarmforge.core.simulation`, and rendered in real-time within the HUD inspector mouseover view (`WorldEditorPane`).

---

## Comprehensive Catalog of 30 Eusocial Behaviors

### 1. Geomagnetic Navigation (`MagnetoreceptionSystem`)
* **Capability Flag**: `hasMagnetoreception()`
* **Key Species**: *Amitermes meridionalis* (Magnetic Termites), *Cataglyphis*
* **Mechanism**: Detects ambient geomagnetic field intensity ($\approx 50\,\mu\text{T}$) to orient wedge-shaped mound construction along the North-South magnetic axis, reducing peak noon solar exposure.

### 2. Mutualistic Aphid Farming (`AphidFarmingSystem`)
* **Capability Flag**: `canFarmAphids()`
* **Key Species**: *Lasius niger*, *Formica rufa*
* **Mechanism**: Ant foragers stroke aphids with antennae to stimulate honeydew secretion. In return, ants protect aphid herds from syrphid and ladybug predators.

### 3. Royal Pheromone Ovary Suppression (`RoyalPheromoneSystem`)
* **Capability Flag**: `hasRoyalPheromoneInhibition()`
* **Key Species**: *Apis mellifera* (9-ODA), *Formica*, Isoptera
* **Mechanism**: Queen mandibular gland pheromones inhibit worker ovarian development. Depletion of royal pheromones triggers emergency queen-cell construction and egg laying by laying workers.

### 4. Substrate Acoustic Alarm Drumming (`SubstrateAcousticSystem`)
* **Capability Flag**: `canDrumSubstrate()`
* **Key Species**: *Camponotus pennsylvanicus*, *Reticulitermes flavipes*
* **Mechanism**: Workers bang their heads or gasters against tunnel walls at $\approx 1000\,\text{Hz}$, transmitting impulse waves through subterranean substrate to alert nestmates of breaches.

### 5. Polycalic Supercolony Network Routing (`PolycalicNetworkSystem`)
* **Capability Flag**: `isPolycalic()`
* **Key Species**: *Formica lugubris*, *Linepithema humile*
* **Mechanism**: Links multiple satellite nests via trunk trails, dynamically balancing food, brood, and worker distribution across the inter-mound network based on resource gradient flows.

### 6. Antimicrobial Propolis Shield (`PropolisShieldSystem`)
* **Capability Flag**: `canCollectPropolis()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Foragers gather tree resins (propolis) to coat internal hive surfaces, suppressing bacterial and fungal pathogen germination (*Ascosphaera apis*).

### 7. Leaf-Sewing with Larval Silk (`WeaverSilkSystem`)
* **Capability Flag**: `canSewLeavesWithLarvalSilk()`
* **Key Species**: *Oecophylla smaragdina* (Weaver Ants)
* **Mechanism**: Workers form physical chains to pull tree leaves together while others hold silk-producing larvae, using them as living adhesive shuttles to construct arboreal nests.

### 8. Fungus Garden Weeding & Antibiotics (`FungusWeedingSystem`)
* **Capability Flag**: `canWeedFungusGarden()`
* **Key Species**: *Atta cephalotes*, *Acromyrmex*
* **Mechanism**: Minor workers prune parasitic mold (*Escovopsis*) from *Leucoagaricus* gardens and apply *Pseudonocardia* actinobacteria secretions to sterilize garden substrate.

### 9. Autothysis Suicidal Defensive Explosion (`AutothysisSystem`)
* **Capability Flag**: `canPerformAutothysis()`
* **Key Species**: *Colobopsis explodens*, *Globitermes sulfureus*
* **Mechanism**: Cornered workers contract abdominal muscles until hypertrophied mandibular glands rupture, releasing a sticky, toxic necrotizing glue that immobilizes enemies.

### 10. Stercoral Soil-Saliva Mortar (`StercoralCementSystem`)
* **Capability Flag**: `canMakeStercoralCement()`
* **Key Species**: *Macrotermes natalensis*
* **Mechanism**: Termites mix harvested clay particles with saliva and fecal secretions to produce concrete-like stercoral mortar for building cathedral mounds with high structural stability.

### 11. Proctodeal Trophallaxis & Microbiome Exchange (`ProctodealTrophallaxisSystem`)
* **Capability Flag**: `hasProctodealTrophallaxis()`
* **Key Species**: *Reticulitermes*, *Cryptotermes*
* **Mechanism**: Direct anal fluid transfer from mature workers to newly molted nymphs, re-inoculating their hindguts with cellulolytic flagellate protozoa required for wood digestion.

### 12. Phragmosis Head-Door Gallery Sealing (`PhragmosisSystem`)
* **Capability Flag**: `canPerformPhragmosis()`
* **Key Species**: *Cephalotes varians*, *Colobopsis truncata*
* **Mechanism**: Major soldiers with flat, disc-shaped truncated heads act as living plugs to seal circular entrance holes against raiding predators.

### 13. Evaporative Hive Cooling (`EvaporativeCoolingSystem`)
* **Capability Flag**: `canPerformEvaporativeCooling()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Water-foraging bees deposit micro-droplets on brood cells combined with high-frequency wing fanning to lower hive temperature below $35^\circ\text{C}$ during heatwaves.

### 14. Trap-Jaw Catapult Mandibles (`TrapJawSystem`)
* **Capability Flag**: `hasTrapJawMechanism()`
* **Key Species**: *Odontomachus*, *Anochetus*
* **Mechanism**: Latch-mediated spring actuation accelerates mandibles to $60\,\text{m/s}$, delivering instant lethal strikes or catapulting the ant backward to escape danger.

### 15. Dulosis & Slave-Making Raids (`DulosisRaidSystem`)
* **Capability Flag**: `isSlaveMakingSpecies()`
* **Key Species**: *Polyergus rufescens* (Amazon Ants)
* **Mechanism**: Specialized warrior ants conduct targeted raids on *Serviformica* nests, stealing pupae to raise enslaved workers that perform domestic nest duties.

### 16. Nomadic Living Bivouac (`LivingBivouacSystem`)
* **Capability Flag**: `canFormLivingBivouac()`
* **Key Species**: *Eciton burchellii* (Army Ants)
* **Mechanism**: Hundreds of thousands of workers interlock claws to form a suspended living nest structure protecting the queen and brood during nomadic migration phases.

### 17. South-Sloping Solar Mound Collectors (`SolarMoundSystem`)
* **Capability Flag**: `hasSolarOrientedMound()`
* **Key Species**: *Formica rufa* group
* **Mechanism**: Pine needle mounds built with asymmetric south-facing slopes capture spring solar radiation, boosting internal nest temperatures by up to $+6.5^\circ\text{C}$.

### 18. Allogrooming & Spore Sanitization (`AllogroomingSystem`)
* **Capability Flag**: `canPerformAllogrooming()`
* **Key Species**: *Lasius*, *Apis mellifera*
* **Mechanism**: Mutual licking and grooming between nestmates mechanically removes fungal spores (*Metarhizium*, *Beauveria*) before cuticular penetration.

### 19. Tremble Dance Recruitment (`TrembleDanceSystem`)
* **Capability Flag**: `canPerformTrembleDance()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Returning foragers encountering long search delays for receiver bees perform tremble dances to recruit idle in-hive workers into nectar processing roles.

### 20. Temperature-Dependent $Q_{10}$ Trail Evaporation (`ThermalTrailDecaySystem`)
* **Capability Flag**: `hasThermalTrailDecay()`
* **Key Species**: *Linepithema humile*, *Solenopsis invicta*
* **Mechanism**: Chemical trail degradation rates double for every $10^\circ\text{C}$ increase in soil temperature according to Arrhenius kinetics.

### 21. Thoracic Shivering Incubation (`ThoracicIncubationSystem`)
* **Capability Flag**: `canPerformThoracicIncubation()`
* **Key Species**: *Formica*, *Apis mellifera*
* **Mechanism**: Flight muscle contractions elevate thoracic temperature to $40^\circ\text{C}$. Workers press their warm thorax against brood cells to accelerate development.

### 22. Non-Lethal Ritual Jousting Tournaments (`RitualJoustingSystem`)
* **Capability Flag**: `canPerformRitualJousting()`
* **Key Species**: *Myrmecocystus mimicus*
* **Mechanism**: Neighboring colonies engage in stilt-walking posture displays to resolve territorial boundaries without physical casualties.

### 23. Inter-Species Territorial Repellent Pheromones (`TerritorialRepellentSystem`)
* **Capability Flag**: `hasTerritorialRepellentPheromone()`
* **Key Species**: *Solenopsis invicta*, *Linepithema humile*
* **Mechanism**: Dominant species deposit long-lasting chemical boundary markers that induce immediate retreat in subordinate rival species.

### 24. Barometric Hydrostatic Flood Evacuation (`FloodEvacuationSystem`)
* **Capability Flag**: `canDetectHydrostaticPressure()`
* **Key Species**: Subterranean ant species
* **Mechanism**: Early detection of barometric pressure drops and soil moisture saturation triggers rapid transport of brood to elevated nest chambers prior to flooding.

### 25. Robber Bee Kleptoparasitic Raids (`RobberBeeSystem`)
* **Capability Flag**: `isRobberBeeSpecies()`
* **Key Species**: *Lestrimelitta limao*
* **Mechanism**: Stingless robber bees raid neighboring weak hives to plunder honey reserves and propolis supplies.

### 26. Acoustic Cave-In Rescue Stridulation (`StridulationSystem`)
* **Capability Flag**: `canStridulateRescueCall()`
* **Key Species**: *Atta*, *Pogonomyrmex*
* **Mechanism**: Trapped ants use abdominal stridulation files to emit $85\,\text{dB}$ vibrational distress signals through soil, guiding excavating nestmates.

### 27. Honeypot Replete Sugar Storage (`HoneypotStorageSystem`)
* **Capability Flag**: `isHoneypotStorageCaste()`
* **Key Species**: *Myrmecocystus* (Honeypot Ants)
* **Mechanism**: Specialized workers hang from chamber ceilings with gasters distended by liquid sugars, serving as living reservoirs during dry seasons.

### 28. Antiseptic Chamber Gravel Sealing (`GravelPluggingSystem`)
* **Capability Flag**: `canPlugContaminatedGalleries()`
* **Key Species**: *Lasius niger*
* **Mechanism**: Workers transport gravel coated with antimicrobial metapleural gland secretions to permanently seal off pathogen-infected chambers.

### 29. Oleic Acid Oxidation Threshold Necrophoresis (`OleicAcidNecrophoresisSystem`)
* **Capability Flag**: `hasOleicAcidThresholdNecrophoresis()`
* **Key Species**: *Formica*, *Lasius*, *Apis*
* **Mechanism**: Accumulation of oxidized oleic acid on deceased individuals triggers undertaker workers to transport corpses to cemetery refuse pits.

### 30. Polarized UV Celestial Compass Navigation (`UVPolarizedNavigationSystem`)
* **Capability Flag**: `hasUVPolarizedLightNavigation()`
* **Key Species**: *Cataglyphis fortis* (Desert Ants)
* **Mechanism**: Detects E-vector skylight polarization patterns to maintain accurate path integration home vectors even under cloudy conditions.

---

## Real-Time Inspector & Mouseover Integration
When tracking or hovering over any individual in the simulation view (`WorldEditorPane`), the active behavioral flags for that individual's species are displayed in the HUD overlay:
```text
🎯 TRACKED INDIVIDUAL: WORKER #a1b2c3d4
💓 Health: 100 / 100
⚡ Energy: 95% | 🍗 Hunger: 5% | 💧 Stridulation: NORMAL
🎂 Age: 1420 ticks | Stage: ADULT | Job: FORAGER
🧠 AI State: FORAGE | 📦 Cargo: NONE
📍 3D Pos: (X: 24.5, Y: 12.0, Z: 0.0)
🧬 Active Behaviors: [APHID_FARMING | ROYAL_INHIBITION | ALLOGROOMING | NECROPHORESIS]
```

---

## Architectural Performance Strategy
To maintain high performance ($60\,\text{FPS}$) during large-scale simulations (over $100,000$ active agents):
1. **Capability Flag Bitmasking**: Species capability flags are stored as primitive boolean fields and cached in `Species` interfaces for $O(1)$ lookups.
2. **Lazy State Evaluation**: Behavior simulation handlers (`ProctodealTrophallaxisSystem`, `EvaporativeCoolingSystem`, etc.) are triggered conditionally only when state thresholds or environmental triggers are met.
3. **Statistical Sampling over Full Logging**: Event journaling relies on interval sampling (e.g. every 60 ticks) rather than per-tick allocation, avoiding CPU overhead and garbage collection pauses.
