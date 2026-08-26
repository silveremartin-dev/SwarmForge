# SwarmForge Behavioral Ethology Engine Specification
**Version 2.2.0-SNAPSHOT | Author: Silvère Martin-Michiellot & Gemini AI Assistant (Google DeepMind)**

---

## Executive Summary
This document provides a comprehensive technical and biological reference for the **80 advanced eusocial insect behavioral systems** implemented within the SwarmForge simulation framework. Each behavioral system is governed by a modular capability flag registered in `Species.java` and `CustomSpecies.java`, executed through dedicated simulation subsystems in `org.swarmforge.core.simulation`, and rendered in real-time within the HUD inspector mouseover view (`WorldEditorPane`).

---

## Comprehensive Catalog of 80 Eusocial Behaviors

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

### 31. Tandem Running Recruitment (`TandemRunningSystem`)
* **Capability Flag**: `canPerformTandemRunning()`
* **Key Species**: *Temnothorax albipennis*
* **Mechanism**: Leader workers guide single followers to new nest sites, moving forward only when receiving tactile antennal feedback on hind legs.

### 32. Thermal Balling Defense Oven (`ThermalBallingSystem`)
* **Capability Flag**: `canPerformThermalBalling()`
* **Key Species**: *Apis cerana* (Asian Honeybee)
* **Mechanism**: Workers swarm hornets (*Vespa mandarinia*) in a dense ball, vibrating flight muscles to reach $47^\circ\text{C}$ core temperature to heat-kill invaders.

### 33. Self-Assembled Waterproof Raft (`SelfAssembledRaftSystem`)
* **Capability Flag**: `canFormLivingRaft()`
* **Key Species**: *Solenopsis invicta* (Fire Ants)
* **Mechanism**: Workers interlock claws to create a hydrophobic floating raft carrying queen and brood safely across floodwaters.

### 34. Domatia Mutualism & Foliage Pruning (`DomatiaMutualismSystem`)
* **Capability Flag**: `canInhabitDomatia()`
* **Key Species**: *Pseudomyrmex ferruginea*
* **Mechanism**: Ants nest inside swollen thorn domatia of *Acacia* trees, consuming Beltian bodies and aggressively pruning encroaching plant vines.

### 35. Voluntary Necrotropic Self-Isolation (`SelfIsolationSystem`)
* **Capability Flag**: `canSelfIsolateWhenInfected()`
* **Key Species**: *Formica* group
* **Mechanism**: Workers infected with fungal pathogens (*Beauveria*) voluntarily leave the nest to die outdoors, protecting the colony.

### 36. Formic Acid & Resin Disinfectant Spray (`ResinSpraySystem`)
* **Capability Flag**: `canSprayFormicResinDisinfectant()`
* **Key Species**: *Formica rufa* (Wood Ants)
* **Mechanism**: Workers spray a mixture of formic acid and collected tree resin over brood cells as an antimicrobial social disinfectant.

### 37. Emergency Swarming & Colony Fission (`EmergencySwarmingSystem`)
* **Capability Flag**: `canTriggerEmergencySwarming()`
* **Key Species**: *Apis mellifera*, Eusocial Wasps
* **Mechanism**: Catastrophic nest damage or queen loss triggers emergency swarming, splitting workers into daughter reproductive swarms.

### 38. Subterranean Spiral Clay Pillars (`ClayPillarSystem`)
* **Capability Flag**: `canConstructClayPillars()`
* **Key Species**: *Macrotermes bellicosus*
* **Mechanism**: Termite architects build vertical spiral clay pillars to reinforce high-load gallery ceilings against soil collapses.

### 39. Granary Seed De-Germination (`SeedStorageSystem`)
* **Capability Flag**: `canDeGermStoredSeeds()`
* **Key Species**: *Messor barbarus* (Harvester Ants)
* **Mechanism**: Workers bite off seed radicles and embryos to prevent subterranean sprouting in damp storage granaries.

### 40. High-Frequency Virgin Queen Piping (`QueenPipingSystem`)
* **Capability Flag**: `canPerformQueenPiping()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Newly emerged virgin queens emit $450\,\text{Hz}$ acoustic piping calls to challenge and locate unhatched rival queen cells.

### 41. Water Trophallaxis & Hive Humidity Regulation (`WaterTrophallaxisSystem`)
* **Capability Flag**: `canPerformWaterTrophallaxis()`
* **Key Species**: *Apis mellifera*, *Formica*
* **Mechanism**: High-volume liquid water transfer between nestmates maintaining $90\%$ relative humidity microclimate in central brood chambers.

### 42. Herd Sanitary Cordon & Aphid Culling (`AphidSanitaryCordonSystem`)
* **Capability Flag**: `canEnforceAphidSanitaryCordon()`
* **Key Species**: *Lasius niger*
* **Mechanism**: Ant herders inspect managed aphid herds, cull pathogen-infected aphids, and isolate clean honeydew producers.

### 43. Self-Assembled Living Architectural Bridges (`LivingBridgeSystem`)
* **Capability Flag**: `canFormLivingBridges()`
* **Key Species**: *Eciton burchellii* (Army Ants)
* **Mechanism**: Workers link tarsi to span physical terrain gaps, forming living structural bridges to accelerate raid traffic.

### 44. Acoustic Stridulation Prey Surge (`AcousticSurgeSystem`)
* **Capability Flag**: `canEmitAcousticPreySurge()`
* **Key Species**: *Megaponera analis* (Matabele Ants)
* **Mechanism**: Predatory scouts emit $75\,\text{dB}$ acoustic stridulation pulses to synchronize massive group attacks on termite mounds.

### 45. Refuse Pit Sorting & Cemetery Quarantine (`RefuseSortingSystem`)
* **Capability Flag**: `canSortExternalRefusePits()`
* **Key Species**: *Atta cephalotes*
* **Mechanism**: Specialized undertakers transport moldy garden refuse and exuviae to segregated external refuse pits located far from nest intakes.

### 46. Subterranean Wood-Fungus Garden Cultivation (`SubterraneanFungusWoodSystem`)
* **Capability Flag**: `canCultivateWoodFungus()`
* **Key Species**: *Odontotermes obesus* (Termitomyces Termites)
* **Mechanism**: Termites digest harvested wood and construct cellulose combs to cultivate *Termitomyces* mushrooms inside deep nest vaults.

### 47. Fast Emergency Evacuation Alarm Pheromones (`EscapePheromoneSystem`)
* **Capability Flag**: `hasEmergencyEscapePheromone()`
* **Key Species**: Subterranean ant species
* **Mechanism**: Volatile alarm pheromones trigger rapid directional flee responses away from breached nest entryways.

### 48. Hydrophobic Wax Lipid Queen Cell Sealing (`QueenWaxSealingSystem`)
* **Capability Flag**: `canSealQueenChamberWax()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Builder bees cap developing royal queen cells with protective wax lipids to maintain ideal pupal development moisture.

### 49. Pheromonal Soldier Caste Ratio Regulation (`CasteRatioInhibitionSystem`)
* **Capability Flag**: `hasCasteRatioPheromoneInhibition()`
* **Key Species**: *Pheidole pallidula*, Isoptera
* **Mechanism**: Inhibitory soldier pheromones suppress further soldier caste differentiation when the soldier ratio reaches $15\%$.

### 50. Substrate Clamping Suction Escape Posture (`SuctionEscapeSystem`)
* **Capability Flag**: `canPerformSuctionEscapePosture()`
* **Key Species**: *Cephalotes atratus* (Gliding Ants)
* **Mechanism**: Arboreal ants flatten their bodies and clamp legs against tree bark to resist bird strikes and wind gusts.

### 51. Low-Frequency Queen Recognition Stridulation (`QueenRecognitionStridulationSystem`)
* **Capability Flag**: `canStridulateQueenRecognition()`
* **Key Species**: *Camponotus*, *Apis mellifera*
* **Mechanism**: Low-frequency acoustic stridulations ($120\,\text{Hz}$) emitted by the mother queen pacify workers and suppress aggressive encounters.

### 52. Abdominal Pulsatile Convective Ventilation (`PulsatileVentilationSystem`)
* **Capability Flag**: `canPerformPulsatileVentilation()`
* **Key Species**: *Macrotermes*, *Apis mellifera*
* **Mechanism**: Workers in entrance tunnels pump abdominal segments synchronously to drive fresh convective air currents into subterranean galleries.

### 53. Rapid Clay Mortar Storm Breach Repair (`ClayBreachRepairSystem`)
* **Capability Flag**: `canRepairBreachesClay()`
* **Key Species**: *Termitidae*, *Formica*
* **Mechanism**: Emergency builder teams deploy clay saliva mortar to patch breached exterior mound walls following storm damage.

### 54. Dynamic Exhausting Trail Pheromone Decay (`DepletingTrailSystem`)
* **Capability Flag**: `hasDepletingTrailPheromone()`
* **Key Species**: *Linepithema humile*, *Pheidole*
* **Mechanism**: Foragers reduce chemical trail deposition as food sources approach depletion, rapidly diverting worker streams to rich patches.

### 55. Trophic Inviable Egg Cannibalism (`EggCannibalismSystem`)
* **Capability Flag**: `canRecycleInviableEggs()`
* **Key Species**: *Formica*, *Lasius*, *Plagiolepis*
* **Mechanism**: Nurse workers consume unfertilized or damaged eggs to recycle essential amino acids during famine periods.

### 56. Worker Parasite Encirclement Quarantine (`ParasiteQuarantineSystem`)
* **Capability Flag**: `canQuarantineInvasiveParasites()`
* **Key Species**: *Apis cerana*, *Formica*
* **Mechanism**: Groups of 6+ workers form dense rings around intruding mites or beetles to immobilize and isolate them against gallery walls.

### 57. Controlled Arboreal Free-Fall Gliding Escape (`ArborealGlidingEscapeSystem`)
* **Capability Flag**: `canPerformArborealGlidingEscape()`
* **Key Species**: *Cephalotes varians*
* **Mechanism**: Canopy workers leap into free-fall upon predator strikes, steering J-shaped gliding trajectories back to tree trunks.

### 58. Morning Solar Brood Basking (`SolarBroodBaskingSystem`)
* **Capability Flag**: `canPerformSolarBroodBasking()`
* **Key Species**: *Formica polyctena*
* **Mechanism**: Workers carry larvae to sunlit mound surfaces on cool mornings to absorb radiant solar energy, boosting developmental rates.

### 59. Epicuticular CHC Gestalt Harmonization (`ChcGestaltHarmonizationSystem`)
* **Capability Flag**: `canHarmonizeChcGestalt()`
* **Key Species**: All Eusocial Hymenoptera
* **Mechanism**: Allogrooming and epicuticular hydrocarbon exchange distribute cuticular lipids evenly across nestmates to enforce colony odor identity.

### 60. Subterranean Collapsible Pitfall Traps (`CollapsiblePitTrapSystem`)
* **Capability Flag**: `canConstructCollapsiblePitTraps()`
* **Key Species**: *Termitidae*
* **Mechanism**: Architects excavate thin-roofed fragile soil pits around nest perimeters that collapse under heavy raiding anteaters or rival colonies.

### 61. Dew Condensation Tarsal Harvesting (`DewCondensationHarvestSystem`)
* **Capability Flag**: `canHarvestDewCondensation()`
* **Key Species**: Desert & Savannah Hymenoptera
* **Mechanism**: Early morning foragers collect micro-dew drops condensing on plant tarsal spines to satisfy colony water needs before heat rise.

### 62. Callow Exoskeleton Anti-Fungal Patrol (`ExoskeletonAntiFungalPatrolSystem`)
* **Capability Flag**: `canPerformExoskeletonAntiFungalPatrol()`
* **Key Species**: *Formica*, *Lasius*
* **Mechanism**: Specialized nurses coat newly emerged soft-shelled callow workers with acid secretions to prevent fungal spore infection.

### 63. Guard Shift Vibrational Whisper (`GuardShiftVibrationalWhisperSystem`)
* **Capability Flag**: `canPerformGuardShiftVibrationalWhisper()`
* **Key Species**: *Camponotus*
* **Mechanism**: Sternal acoustic pulses signal guard shift transitions at nest entrances, ensuring continuous 24/7 security without alert alarms.

### 64. Thermoregulated Air-Water Conduits (`ThermoregulatedAirWaterConduitSystem`)
* **Capability Flag**: `canConstructThermoregulatedConduits()`
* **Key Species**: *Macrotermes bellicosus*
* **Mechanism**: Dual parallel underground channels guide cool moisture currents under central brood vaults during extreme heat waves.

### 65. Toxic Plant Resin Rodent Repellent Raids (`ToxicPlantResinRaidSystem`)
* **Capability Flag**: `canRaidToxicPlantResin()`
* **Key Species**: Subterranean Termites & Ants
* **Mechanism**: Targeted foraging of toxic plant resins applied around tunnel perimeters to repel burrowing rodents.

### 66. Fine Dust Substrate Camouflage (`DustSubstrateCamouflageSystem`)
* **Capability Flag**: `canApplyDustSubstrateCamouflage()`
* **Key Species**: Ground-foraging ants
* **Mechanism**: Workers roll in fine silt and clay dust to disrupt visual predator contrast against soil backgrounds.

### 67. Interlocked Mandible Chain Brood Transport (`ChainBroodTransportSystem`)
* **Capability Flag**: `canTransportChainBrood()`
* **Key Species**: Nomadic Ecitoninae
* **Mechanism**: Workers link mandibles to transport multiple larvae in linked chains during rapid nest evacuations.

### 68. Oral Trophallactic Ovary Suppression (`TrophallacticOvaryInhibitionSystem`)
* **Capability Flag**: `hasTrophallacticOvaryInhibition()`
* **Key Species**: *Apis mellifera*, *Pheidole*
* **Mechanism**: Oral trophallactic fluid transfer delivers inhibitory peptides suppressing worker ovarian maturation.

### 69. Soil-Moisture Drought Vibrato Dance (`DroughtSoilMoistureVibratoSystem`)
* **Capability Flag**: `canPerformDroughtVibratoDance()`
* **Key Species**: Subterranean Harvester Ants
* **Mechanism**: Sub-surface vibrato dances recruit nestmates to deepen galleries when soil moisture drops below critical thresholds.

### 70. Clay-Saliva Propolis Mummification (`LargeIntruderClayEncapsulationSystem`)
* **Capability Flag**: `canEncapsulateLargeIntrudersClay()`
* **Key Species**: *Apis mellifera*, *Formica*
* **Mechanism**: Carcasses of large intruding reptiles or beetles too heavy to carry are completely sealed in clay-propolis mummies to stop decomposition odor.

### 71. Phonic Isolation Royal Chambers (`PhonicIsolationChamberSystem`)
* **Capability Flag**: `canConstructPhonicIsolationChambers()`
* **Key Species**: *Apis mellifera*, *Macrotermes*
* **Mechanism**: Construction of clay and wax sound-dampening acoustic baffles around royal queen chambers to insulate egg laying from external ground vibrations.

### 72. Hydrophobic Lipid Trail Coating (`HydrophobicTrailCoatingSystem`)
* **Capability Flag**: `canApplyHydrophobicTrailCoating()`
* **Key Species**: *Solenopsis invicta*, *Camponotus*
* **Mechanism**: Deposition of epicuticular lipid films along subterranean gallery walls in flood-prone zones, reducing soil water infiltration by $85\%$.

### 73. Fermented Sap Combat Anesthetic (`FermentedSapAnestheticSystem`)
* **Capability Flag**: `canConsumeFermentedSapAnesthetic()`
* **Key Species**: *Formica*, *Camponotus*
* **Mechanism**: Targeted ingestion of ethanol-rich fermented tree sap before major inter-colony battles to elevate pain tolerance and combat endurance.

### 74. Size-Graded Relay Seed Transport (`RelaySeedTransportSystem`)
* **Capability Flag**: `canPerformRelaySeedTransport()`
* **Key Species**: *Messor barbarus*, *Pogonomyrmex*
* **Mechanism**: Heavy seeds harvested by major workers are handed off to faster minor workers at intermediate relay staging points along trunk trails.

### 75. Prey-Size Selective Chemical Trails (`PreySizeSelectivePheromoneSystem`)
* **Capability Flag**: `hasPreySizeSelectivePheromones()`
* **Key Species**: *Pheidole*, *Eciton*
* **Mechanism**: Scout foragers lay distinct chemical trail blends indicating prey mass, recruiting appropriate worker/soldier caste ratios.

### 76. Larval Wood Dust Mildew Drying (`LarvalWoodDustDryingSystem`)
* **Capability Flag**: `canDryLarvaeWoodDust()`
* **Key Species**: *Camponotus ligniperda*
* **Mechanism**: Workers dust damp larvae with fine wood shavings to absorb excess humidity and prevent fungal mildew growth.

### 77. Formic Acid Fan-Out Escape (`FanoutEscapeFormicAcidSystem`)
* **Capability Flag**: `canPerformFanoutEscapeFormicAcid()`
* **Key Species**: *Serviformica*, *Temnothorax*
* **Mechanism**: Detection of enemy formic acid vapor triggers an instant $360^\circ$ radial scatter escape response to minimize raid casualties.

### 78. Synchronous Overheat Mound Vibrato (`MoundOverheatVibratoSystem`)
* **Capability Flag**: `canEmitMoundOverheatVibrato()`
* **Key Species**: *Formica polyctena*
* **Mechanism**: Substrate vibrato pulses emitted when mound core temperature exceeds $38^\circ\text{C}$ recruit builders to uncap top ventilation chimneys.

### 79. Pre-Flight Virgin Queen Hyper-Nourishment (`VirginQueenPreFlightNourishmentSystem`)
* **Capability Flag**: `canNourishVirginQueensPreFlight()`
* **Key Species**: *Apis mellifera*, *Atta*
* **Mechanism**: Intensive trophallactic feeding of virgin queens in the 48h preceding nuptial flights to maximize wing muscle lipid reserves.

### 80. Emergency Honey Store Brick Plugging (`HoneyStoreBrickPluggingSystem`)
* **Capability Flag**: `canPlugHoneyStoresBricks()`
* **Key Species**: *Apis cerana*, Meliponini
* **Mechanism**: Rapid sealing of food storage vault entrances with compressed soil bricks during invasive wasp or robber bee raids.

### 81. Nocturnal Infrared Hunting (`NocturnalInfraredHuntingSystem`)
* **Capability Flag**: `canHuntNocturnalInfrared()`
* **Key Species**: *Pachycondyla*, *Myrmecia*
* **Mechanism**: Thermal infrared sensilla detection of sleeping small vertebrate prey during nighttime foraging missions.

### 82. Inter-Canopy Larval Silk Bridges (`LarvalSilkCanopyBridgeSystem`)
* **Capability Flag**: `canWeaveLarvalSilkCanopyBridges()`
* **Key Species**: *Oecophylla smaragdina*
* **Mechanism**: Weaving of tensioned silk threads between distant tree branches using silk-producing larvae held in mandibles.

### 83. Synchronized Egg-Laying Stridulation (`EggLayingSynchronizationStridulationSystem`)
* **Capability Flag**: `canStridulateEggLayingSynchronization()`
* **Key Species**: *Plagiolepis pygmaea*
* **Mechanism**: Abdominal stridulation by dominant queens inducing synchronized egg-laying bursts across all co-habiting gynes.

### 84. Tarsal Brush Antennal Dust Grooming (`AntennalDustGroomingSystem`)
* **Capability Flag**: `canPerformAntennalDustGrooming()`
* **Key Species**: *Formica*, *Lasius*
* **Mechanism**: Frequent grooming of antennal sensilla using specialized tarsal notch brushes to clear environmental dust and maintain chemical acuity.

### 85. Mineral Salt Crystal Osmoregulation (`SaltCrystalOsmoregulationSystem`)
* **Capability Flag**: `canForageSaltCrystalsOsmoregulation()`
* **Key Species**: *Cataglyphis*, *Cephalotes*
* **Mechanism**: Targeted foraging and transport of solid mineral salt crystals to regulate osmotic pressure in developing larvae during dry spells.

### 86. Hydraulic Rain Evacuation Siphons (`RainEvacuationSiphonSystem`)
* **Capability Flag**: `canConstructRainEvacuationSiphons()`
* **Key Species**: *Macrotermes bellicosus*
* **Mechanism**: Construction of subterranean curved siphon conduits that automatically prime and drain rainwater away from deep brood vaults during torrential storms.

### 87. Host Tree Bark Chemical Camouflage (`HostPlantChemicalCamouflageSystem`)
* **Capability Flag**: `canAbsorbHostPlantChemicalCamouflage()`
* **Key Species**: *Pseudomyrmex ferruginea*
* **Mechanism**: Prolonged rubbing against host tree bark to absorb cuticular hydrocarbons, masking colony presence from parasitic wasps and rival ants.

### 88. Natural Mineral Sulfur Anti-Mite Dusting (`SulfurDustAntiMitePatrolSystem`)
* **Capability Flag**: `canDepositSulfurDustAntiMitePatrol()`
* **Key Species**: *Apis dorsata*, *Formica rufa*
* **Mechanism**: Collection of natural sulfur deposits and dusting of brood chambers to eradicate parasitic mites and fungal hyphae.

### 89. Prey Hatching Announcement Vibrato (`HatchingEnthusiasmVibratoDanceSystem`)
* **Capability Flag**: `canDanceVibratoHatchingEnthusiasm()`
* **Key Species**: *Eciton burchellii*
* **Mechanism**: Rhythmic substrate drumming dance performed by scouts announcing mass hatching events of caterpillars or cicada nymphs.

### 90. Antiseptic Resin Pupal Mummification (`ResinNymphalMummificationSystem`)
* **Capability Flag**: `canResinMummifyNymphalChambers()`
* **Key Species**: *Melipona*, *Trigona*
* **Mechanism**: Enclosing queen nymphal cocoons in antimicrobial plant resin envelopes to guarantee sterile conditions during delicate metamorphosis.

### 91. Sand Pitfall Trap Excavation (`PitfallTrapExcavationSystem`)
* **Capability Flag**: `canExcavatePitfallTraps()`
* **Key Species**: *Myrmeleon*, *Cataglyphis*
* **Mechanism**: Excavation of conical pitfall traps in loose sandy soils to capture prey arthropods stumbling across the colony boundary.

### 92. Winter Metabolic Glycerol Synthesis (`GlycerolCryoprotectionSystem`)
* **Capability Flag**: `canSynthesizeGlycerolCryoprotection()`
* **Key Species**: *Camponotus herculeanus*, *Lasius niger*
* **Mechanism**: Bio-accumulation of intracellular glycerol and polyols to prevent ice crystal formation during sub-zero overwintering.

### 93. Pheromonal Stretcher Rescue Transport (`InjuredPheromonalStretcherSystem`)
* **Capability Flag**: `canTransportInjuredPheromonalStretcher()`
* **Key Species**: *Megaponera analis*
* **Mechanism**: Identification of distress chemical signals from wounded nestmates and mandible stretcher transport back to hospital chambers.

### 94. Abandoned Hive Wax Vault Raiding (`AbandonedWaxVaultRaidSystem`)
* **Capability Flag**: `canRaidAbandonedWaxVaults()`
* **Key Species**: *Lasius fuliginosus*, *Apis mellifera*
* **Mechanism**: Targeted foraging and structural dismantling of abandoned bee/wasp hives to recover lipid-rich wax and honey reserves.

### 95. Reproductive Dominance Ritual Wrestling (`RitualMandibularWrestlingSystem`)
* **Capability Flag**: `canPerformRitualMandibularWrestling()`
* **Key Species**: *Harpegnathos saltator*, *Dinoponera quadriceps*
* **Mechanism**: Non-lethal ritualized antennal boxing and mandibular wrestling tournaments determining Gamergate dominance hierarchies.

### 96. Synchronized Pulsed Convective Air Pumping (`PulsedAirConvectiveVentilationSystem`)
* **Capability Flag**: `canPerformPulsedAirConvectiveVentilation()`
* **Key Species**: *Atta sexdens*, *Macrotermes*
* **Mechanism**: Coordinated abdominal compression cycles pumping stagnant $\text{CO}_2$ out of subterranean fungus gardens.

### 97. Cuticular Streptomyces Antibiotic Cultivation (`StreptomycesAntibioticsSystem`)
* **Capability Flag**: `canCultivateStreptomycesAntibiotics()`
* **Key Species**: *Acromyrmex*, *Atta*
* **Mechanism**: Symbiotic culture of *Streptomyces* actinobacteria in propleural crypts producing targeted antibiotics against pathogenic *Escovopsis* fungi.

### 98. Twilight UV Sky Polarization Navigation (`PolarizedTwilightUVNavigationSystem`)
* **Capability Flag**: `canNavigatePolarizedTwilightUV()`
* **Key Species**: *Cataglyphis bicolor*, *Melophorus bagoti*
* **Mechanism**: Vector navigation using the e-vector direction of polarized ultraviolet sunlight during dawn and dusk foraging trips.

### 99. Elastic Snap-Trap Mandible Catapulting (`TrapMandibleCatapultSystem`)
* **Capability Flag**: `canSnapTrapMandiblesCatapult()`
* **Key Species**: *Odontomachus bauri*, *Anochetus*
* **Mechanism**: Latch-mediated spring actuation of mandibular strikes generating $126-230\,\text{km/h}$ catapult jumps to escape vertebrate predators.

### 100. Pedestrian Swarm Budding & Colony Fission (`PedestrianSwarmBuddingSystem`)
* **Capability Flag**: `canPerformPedestrianSwarmBudding()`
* **Key Species**: *Dorylus*, *Linepithema humile*, *Eciton*
* **Mechanism**: Pedestrian march migration of a queen and worker sub-colony carrying complete brood reserves to found a daughter nest.

### 101. Termite Lignocellulose Protozoan Trophallaxis (`TermiteProtozoaTrophallaxisSystem`)
* **Capability Flag**: `canTrophallaxisProtozoa()`
* **Key Species**: *Reticulitermes*, *Cryptotermes*
* **Mechanism**: Anal trophallactic transfer of flagellate symbiotic protozoa essential for digesting wood cellulose in newly molted termite nymphs.

### 102. Nasute Solenoid Hydroordinate Squirt Nozzle (`NasuteChemicalSquirtSystem`)
* **Capability Flag**: `canSquirtNasuteChemical()`
* **Key Species**: *Nasutitermes*, *Trinervitermes*
* **Mechanism**: Specialized nasus fontanelle nozzle spraying sticky terpenoid glue at intruders from a distance.

### 103. Paper Pulp Carton Fiber Mastication (`PaperPulpCartonMasticationSystem`)
* **Capability Flag**: `canMasticatePaperPulpCarton()`
* **Key Species**: *Vespula vulgaris*, *Polistes*
* **Mechanism**: Scraping and chewing weathered wood fibers mixed with salivary proteins to build hexagonal carton paper nest cells.

### 104. Larval Amino Acid Saliva Harvesting (`LarvalSalivaHarvestingSystem`)
* **Capability Flag**: `canHarvestLarvalSalivaDroplets()`
* **Key Species**: *Vespula*, *Polistes*
* **Mechanism**: Adult wasps solicit nutrient-rich amino acid droplets from larvae in exchange for solid prey meat.

### 105. Pedicel Ant-Repellent Glandular Coating (`PedicelAntRepellentSystem`)
* **Capability Flag**: `canApplyPedicelAntRepellent()`
* **Key Species**: *Polistes dominula*
* **Mechanism**: Applying van der Vecht organ secretion around the nest pedicel stalk to create a chemical barrier against ant raids.

### 106. Wasp Facial Visual Pattern Recognition (`WaspFacialRecognitionSystem`)
* **Capability Flag**: `canRecognizeFacialVisualPatterns()`
* **Key Species**: *Polistes fuscatus*
* **Mechanism**: Visual recognition of unique facial yellow/black markings to maintain stable dominance hierarchies without constant fighting.

### 107. Buzz Pollination Thoracic Sonication (`BuzzPollinationSonicationSystem`)
* **Capability Flag**: `canPerformBuzzPollination()`
* **Key Species**: *Bombus terrestris*
* **Mechanism**: High-frequency thorax flight muscle vibration (300 Hz) to dislodge pollen from pore-bearing solanaceous flowers.

### 108. Abdominal Incubating Brood Heat Transfer (`BumblebeeAbdominalIncubationSystem`)
* **Capability Flag**: `canIncubateBroodAbdominalHeat()`
* **Key Species**: *Bombus*
* **Mechanism**: Pressing hairless ventral abdomen onto brood cells to transfer metabolic heat, maintaining 35°C incubation during cold spells.

### 109. Aphid Frontal Horn Stabbing Defense (`AphidSoldierHornStabbingSystem`)
* **Capability Flag**: `canStabFrontalHornsAphid()`
* **Key Species**: *Pseudoregma alexanderi*
* **Mechanism**: Sterile soldier aphid nymphs using sharp frontal horns to pierce and glue ladybug larvae hemolymph.

### 110. Eusocial Thrips Gall Raptorial Squeezing (`ThripsGallForelegSqueezingSystem`)
* **Capability Flag**: `canSqueezeGallIntrudersThrips()`
* **Key Species**: *Kladothrips acaciae*
* **Mechanism**: Soldier thrips using enlarged raptorial forelegs to crush kleptoparasitic thrips invading acacia galls.

### 111. Eusocial Shrimp Acoustic Cavitation Shockwave (`EusocialShrimpClawShockwaveSystem`)
* **Capability Flag**: `canSnapClawAcousticShockwave()`
* **Key Species**: *Synalpheus regalis*
* **Mechanism**: Queen/soldier shrimp snapping giant chela to produce 210 dB cavitation bubbles repelling intruders inside sponge canals.

### 112. Passalid Beetle Wood Frass Stridulation (`PassalidParentalStridulationSystem`)
* **Capability Flag**: `canStridulatePassalidParentalCare()`
* **Key Species**: *Odontotaenius disjunctus*
* **Mechanism**: Adult and larval stridulatory communication while preparing and feeding masticated wood frass to young grubs.

### 113. Physogastric Termite Queen Peristalsis (`PhysogastricPeristalsisSystem`)
* **Capability Flag**: `canPerformPhysogastricPeristalsis()`
* **Key Species**: *Macrotermes natalensis*
* **Mechanism**: Massive enlarged queen abdomen performing continuous muscular peristalsis for 30,000 egg/day throughput.

### 114. Geomagnetic Field Mound Orientation (`MagneticMoundOrientationSystem`)
* **Capability Flag**: `canOrientMagneticMound()`
* **Key Species**: *Amitermes meridionalis*
* **Mechanism**: Building wedge-shaped mounds strictly aligned N-S along geomagnetic lines for optimal solar thermoregulation.

### 115. Hornet Venom Spray Group Alarm Raid (`HornetGroupAlarmPheromoneSystem`)
* **Capability Flag**: `canEmitHornetGroupAlarmPheromone()`
* **Key Species**: *Vespa mandarinia*
* **Mechanism**: Venom-spraying alarm recruitment triggering mass coordinated slaughter raids of honeybee hives.

### 116. Stenogastrine Paper-Flake Saliva Jelly Weaving (`StenogastrinePaperJellyWeavingSystem`)
* **Capability Flag**: `canWeaveStenogastrinePaperJelly()`
* **Key Species**: *Parischnogaster*
* **Mechanism**: Mixing salivary gelatinous secretions with fine plant fibers to build delicate hair-thin suspended nests.

### 117. Termite Subterranean Fungal Comb Inoculation (`TermiteFungalCombSystem`)
* **Capability Flag**: `canInoculateFungalCombTermite()`
* **Key Species**: *Odontotermes*
* **Mechanism**: Cultivating *Termitomyces* fungal gardens on chewed fecal pellets to break down tough plant lignin.

### 118. Wasp Abdominal Warning Rim Drumming (`WaspCellRimDrummingSystem`)
* **Capability Flag**: `canDrumAbdomenWaspCellRim()`
* **Key Species**: *Polistes*, *Vespula*
* **Mechanism**: Rhythmic abdominal drumming against paper cell walls to alert nestmates of nearby predators.

### 119. Bumblebee Nectar Wax Urn Pot Molding (`BumblebeeNectarWaxPotSystem`)
* **Capability Flag**: `canConstructNectarWaxPots()`
* **Key Species**: *Bombus*
* **Mechanism**: Molding urn-shaped wax pots near nest entrance to store emergency honey and nectar reserves.

### 120. Subsocial Shield Bug Maternal Shield Guarding (`MaternalShieldGuardingSystem`)
* **Capability Flag**: `canPerformMaternalShieldGuarding()`
* **Key Species**: *Elasmucha grisea*
* **Mechanism**: Female standing guard over egg clutch and first-instar nymphs, spreading body to block parasitic wasps.

### 121. Subsocial Spider Communal Web Dragline Silk Weaving (`CommunalSpiderSilkSystem`)
* **Capability Flag**: `canWeaveCommunalSpiderSilk()`
* **Key Species**: *Stegodyphus dumicola*
* **Mechanism**: Cooperative spinning and maintenance of massive sticky capture webs spanning entire trees.

### 122. Processionary Caterpillar Head-to-Tail Silk Pheromone Trail (`ProcessionarySilkTrailSystem`)
* **Capability Flag**: `canFormProcessionarySilkTrail()`
* **Key Species**: *Thaumetopoea pityocampa*
* **Mechanism**: Single file procession guided by silk thread and abdominal trail pheromones.

### 123. Termite Clay-Saliva Vault Arch Engineering (`ClayVaultArchSystem`)
* **Capability Flag**: `canConstructClayVaultArches()`
* **Key Species**: *Coptotermes formosanus*
* **Mechanism**: Curved clay-saliva pellet vaulting forming self-supporting gothic arches in subterranean chambers.

### 124. Stenogastrine Larval Pap-Food Droplet Delivery (`StenogastrinePapFoodSystem`)
* **Capability Flag**: `canDeliverStenogastrinePapFood()`
* **Key Species**: *Liostenogaster flavolineata*
* **Mechanism**: Secretion of gelatinous carbohydrate-protein pap droplets placed directly into egg cells before hatching.

### 125. Social Beetle Frass Gallery Plastering (`BeetleFrassGalleryPlasterSystem`)
* **Capability Flag**: `canPlasterFrassGalleryWalls()`
* **Key Species**: *Austroplatypus incompertus*
* **Mechanism**: Plastering gallery walls with chewed wood and frass to prevent resin inundation in eucalyptus trunks.

### 126. Bumblebee Foraging Trap-lining Route Learning (`TrapliningFlightRouteSystem`)
* **Capability Flag**: `canLearnTrapliningFlightRoutes()`
* **Key Species**: *Bombus lapidarius*
* **Mechanism**: Spatial memory learning of multi-patch flower trajectories optimized for distance and reward yield.

### 127. Wasp Hydro-Pneumatic Cell Water Cooling (`WaspNestWaterCoolingSystem`)
* **Capability Flag**: `canCoolNestWaterRegurgitation()`
* **Key Species**: *Vespa crabro*
* **Mechanism**: Regurgitation of water droplets onto carton cell caps followed by rapid wing fanning to induce evaporative cooling.

### 128. Eusocial Aphid Honeydew Droplet Ejection Signaling (`AphidHoneydewSignalingSystem`)
* **Capability Flag**: `canEjectHoneydewSignalingDroplets()`
* **Key Species**: *Tuberocephalus*
* **Mechanism**: Rhythmic abdominal flicks ejecting honeydew droplets away from galls to recruit mutualist ants.

### 129. Termite Soldier Mandibular Snap-Strike Acoustic Alarm (`TermiteMandibleSnapAlarmSystem`)
* **Capability Flag**: `canSnapMandibleAcousticAlarm()`
* **Key Species**: *Termes hospes*
* **Mechanism**: Asymmetric mandible snapping against wooden gallery walls creating loud acoustic percussion warnings.

### 130. Subsocial Earwig Egg Licking Anti-Fungal Grooming (`EarwigEggLickingGroomingSystem`)
* **Capability Flag**: `canPerformEggLickingGrooming()`
* **Key Species**: *Forficula auricularia*
* **Mechanism**: Continuous maternal mouthpart licking and salivary coating of eggs to prevent fungal spore germination.

### 131. Harvester Ant Chaff Garbage Dune Construction (`ChaffGarbageDuneSystem`)
* **Capability Flag**: `canConstructChaffGarbageDunes()`
* **Key Species**: *Messor barbarus*
* **Mechanism**: Orderly dumping of seed chaff and husk refuse in specialized exterior crescent dunes.

### 132. Paper Wasp Antennal Drumming Brood Stimulation (`WaspAntennalDrummingSystem`)
* **Capability Flag**: `canDrumAntennaeLarvalStimulation()`
* **Key Species**: *Polistes fuscatus*
* **Mechanism**: Rhythmic antennal drumming on larval cell heads soliciting gut regurgitation and saliva release.

### 133. Weaver Ant Leaf-Edge Riveting Chain Pulling (`WeaverLeafPullingChainSystem`)
* **Capability Flag**: `canFormLeafPullingChains()`
* **Key Species**: *Oecophylla smaragdina*
* **Mechanism**: Multi-individual interlocked body chains exerting tensile force to pull large tree leaves together for nest assembly.

### 134. Termite Salivary Cement Moisture Sealing (`TermiteSalivaryCementMoistureSealSystem`)
* **Capability Flag**: `canApplySalivaryCementMoistureSeal()`
* **Key Species**: *Reticulitermes flavipes*
* **Mechanism**: Applying water-resistant salivary-clay mortar to seal gallery breaches against desiccation.

### 135. Bumblebee Nectar Foraging Temperature Thresholding (`SubZeroBumblebeeForagingSystem`)
* **Capability Flag**: `canForageSubZeroBumblebee()`
* **Key Species**: *Bombus polaris*
* **Mechanism**: Thermogenic shivering of flight muscles allowing nectar foraging at temperatures as low as 0°C.

### 136. Eusocial Thrips Substratal Gall Repair Secretion (`ThripsGallRepairSecretionSystem`)
* **Capability Flag**: `canRepairGallSubstratalSecretion()`
* **Key Species**: *Kladothrips waterhousei*
* **Mechanism**: Secretion of liquid plant growth stimulants from abdomen to repair outer gall wall cracks.

### 137. Horned Beetle Wood Chamber Gallery Trophallaxis (`PassalidWoodFrassTrophallaxisSystem`)
* **Capability Flag**: `canTrophallaxisPassalidWoodFrass()`
* **Key Species**: *Odontotaenius*
* **Mechanism**: Oral regurgitation of pre-digested fungal-wood pulp to newly hatched grubs lacking gut microbiota.

### 138. Social Spider Regurgitation Feeding Crèche (`SpiderCrècheRegurgitationSystem`)
* **Capability Flag**: `canPerformCrècheRegurgitationSpider()`
* **Key Species**: *Stegodyphus lineatus*
* **Mechanism**: Maternal liquefied gut regurgitation feeding of entire communal crèches of spiderlings.

### 139. Termite Royal Chamber Guard Sentry Blockade (`TermiteRoyalChamberBlockadeSystem`)
* **Capability Flag**: `canBlockRoyalChamberSentry()`
* **Key Species**: *Macrotermes bellicosus*
* **Mechanism**: Interlocked soldier head capsules forming a solid chitinous wall blocking access to the royal physogastric chamber.

### 140. Parent Bug Pheromonal Alarm Dispersion Gathering (`ParentBugAlarmGatheringSystem`)
* **Capability Flag**: `canEmitParentBugAlarmGathering()`
* **Key Species**: *Elasmucha*
* **Mechanism**: Abdominal alarm pheromone burst signaling nymphs to cluster tightly under the mother's body shield.

### 141. Marquage Hydrophobe des Réserves de Pain d'Abeille par Sécrétion de Lipides (`BeeBreadHydrophobicCoatingSystem`)
* **Capability Flag**: `canApplyBeeBreadHydrophobicCoating()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Lipid coating applied over stored pollen and bee-bread reserves to prevent atmospheric water absorption and mold growth.

### 142. Immobilisation par Ligature de Soie des Parasites Infiltrés (`ParasiteSilkBindingSystem`)
* **Capability Flag**: `canBindParasitesWithSilk()`
* **Key Species**: *Oecophylla smaragdina*, *Polyrhachis*
* **Mechanism**: Collective silk tethering and immobilization of parasitic beetles inside nest chambers to neutralize intruders.

### 143. Vibrato Sonore de Signalisation d'Obstacle aux Colonnes de Charge (`SubstrateObstacleVibratoSystem`)
* **Capability Flag**: `canEmitSubstrateObstacleVibrato()`
* **Key Species**: *Atta sexdens*
* **Mechanism**: Emission of low-frequency substrate shockwaves alerting loaded foraging columns of obstacles or trail collapses ahead.

### 144. Rituels de Toilettage Post-Combat par Bain d'Acide Formique (`FormicAcidBathGroomingSystem`)
* **Capability Flag**: `canPerformFormicAcidBathGrooming()`
* **Key Species**: *Formica rufa*
* **Mechanism**: Mutual cuticular spraying and bath grooming with formic acid for disinfection after territorial battles with rival colonies.

### 145. Forage d'Urgence de Chutes d'Eau en Conduits Verticaux (`VerticalDrainageShaftSystem`)
* **Capability Flag**: `canExcavateVerticalDrainageShafts()`
* **Key Species**: *Camponotus*
* **Mechanism**: Rapid digging of vertical drainage shafts to evacuate water during subterranean water table breaches or flash floods.

### 146. Consommation de Résines Phénoliques pour Stimulation Immunitaire (`PhenolicResinMedicationSystem`)
* **Capability Flag**: `canIngestPhenolicResinMedication()`
* **Key Species**: *Formica paralugubris*
* **Mechanism**: Selective harvesting and consumption of phenolic-rich plant resins to boost colony immune defenses during fungal epidemics.

### 147. Construction de Dômes Sphaignes de Rétention d'Humidité (`SphagnumMoistureDomeSystem`)
* **Capability Flag**: `canConstructSphagnumMoistureDomes()`
* **Key Species**: *Formica polyctena*
* **Mechanism**: Weaving living sphagnum moss into nest mound crowns to capture and retain atmospheric humidity.

### 148. Dépose de Phéromones Repellentes sur les Cadavres Parasités (`ParasitizedCadaverRepellentSystem`)
* **Capability Flag**: `canMarkParasitizedCadaverRepellent()`
* **Key Species**: *Linepithema humile*
* **Mechanism**: Depositing repellent marking pheromones on sporulating infected pupal corpses to prevent healthy worker contact.

### 149. Danse de Tambourinage Substratique pour Synchroniser le Vol Nuptial (`NuptialFlightDrummingSystem`)
* **Capability Flag**: `canDrumNuptialFlightSynchronization()`
* **Key Species**: *Camponotus ligniperda*
* **Mechanism**: Rhythmic substrate drumming by workers triggering simultaneous synchronized takeoff of winged alates for nuptial flights.

### 150. Refuge Hydrique par Condensation sur Poils Cuticulaires (`CuticularWaterCondensationSystem`)
* **Capability Flag**: `canHarvestCuticularWaterCondensation()`
* **Key Species**: *Cataglyphis*, *Onymacris*
* **Mechanism**: Collecting micro-droplets of water condensed on dense abdominal cuticular setae during desert morning fogs.

### 151. Passalid Grub Hunger Solicitation Stridulation Chirp (`PassalidGrubHungerStridulationSystem`)
* **Capability Flag**: `canStridulateLarvalHungerChirp()`
* **Key Species**: *Odontotaenius disjunctus*
* **Mechanism**: Larval stridulation chirps soliciting pre-digested wood frass regurgitation from adult passalid guardians.

### 152. Termite Thermal Clay Chimney Flue Construction (`TermiteThermalChimneyFlueSystem`)
* **Capability Flag**: `canConstructThermalChimneyFlues()`
* **Key Species**: *Macrotermes subhyalinus*
* **Mechanism**: Building vertical clay chimney flues drawing cool ambient air into subterranean fungus chambers via convective draft.

### 153. Paper Wasp Emergency Larval Saliva Food Drop (`WaspEmergencySalivaFoodDropSystem`)
* **Capability Flag**: `canDepositLarvalFoodSalivaDrop()`
* **Key Species**: *Polistes dominula*
* **Mechanism**: Depositing concentrated nectar-saliva drops in empty comb cells as emergency larval rations during bad weather.

### 154. Subsocial Beetle Egg Clutch Mucilage Envelope (`EggMassMucilageEnvelopeSystem`)
* **Capability Flag**: `canApplyEggMassMucilageEnvelope()`
* **Key Species**: *Chrysomelidae*
* **Mechanism**: Coating egg clutches in protective antimicrobial mucilage to block parasitoid wasp oviposition.

### 155. Weaver Ant Silk Pavilion Aphid Shelter Construction (`WeaverSilkPavilionAphidShelterSystem`)
* **Capability Flag**: `canWeaveSilkPavilionAphidShelter()`
* **Key Species**: *Oecophylla smaragdina*
* **Mechanism**: Weaving protective silk pavilions over honeydew aphid herds to shield mutualists from rain and predators.

### 156. Honeybee Hot-Ball Thermal Defense Swarm (`HotBallThermalDefenseSystem`)
* **Capability Flag**: `canFormHotBallThermalDefense()`
* **Key Species**: *Apis cerana*
* **Mechanism**: Dense thermo-balling around hornet intruders raising temperature to 47°C to cook predatory hornets alive.

### 157. Termite Fontanelle Autothysis Explosive Sacrifice (`FontanelleAutothysisSystem`)
* **Capability Flag**: `canPerformFontanelleAutothysis()`
* **Key Species**: *Neocapritermes taracua*
* **Mechanism**: Age-dependent abdominal autothysis rupture releasing toxic blue copper protein crystals that immobilize enemy ants.

### 158. Social Spider Dragline Signal Wire Tripping (`SpiderDraglineSignalWireSystem`)
* **Capability Flag**: `canSensePreySignalWireTripping()`
* **Key Species**: *Stegodyphus dumicola*
* **Mechanism**: Sensing tension impulse changes along communal draglines to coordinate simultaneous multi-spider strikes on large prey.

### 159. Harvester Ant Seed Radicle Mutilation Storage (`HarvesterSeedRadicleMutilationSystem`)
* **Capability Flag**: `canMutilateSeedRadicles()`
* **Key Species**: *Messor barbarus*
* **Mechanism**: Biting off seed radicles to prevent stored seeds from germinating inside moist underground granaries.

### 160. Bumblebee Nectar Theft Hole Biting (`BumblebeeNectarTheftHoleBiteSystem`)
* **Capability Flag**: `canBiteNectarTheftHoles()`
* **Key Species**: *Bombus terrestris*
* **Mechanism**: Piercing flower corolla bases to rob nectar directly without entering through the flower mouth for pollination.

### 161. Termite Micro-Fungal Comb Spore Sowing (`FungalSporeCombSystem`)
* **Capability Flag**: `canSowFungalSporeCombs()`
* **Key Species**: *Macrotermes bellicosus*
* **Mechanism**: Sowing fresh fungal spores on newly prepared chewed-wood combs inside subterranean gardens.

### 162. Weaver Ant Silk Cocoon Larval Weaving Harness (`LarvalSilkHarnessSystem`)
* **Capability Flag**: `canHarnessLarvalSilkCocoon()`
* **Key Species**: *Oecophylla smaragdina*
* **Mechanism**: Holding living larvae like silk glue-guns to stitch nest leaves together into arboreal tents.

### 163. Army Ant Biomechanical Bivouac Wall Formation (`BiomechanicalBivouacSystem`)
* **Capability Flag**: `canFormBiomechanicalBivouac()`
* **Key Species**: *Eciton burchellii*
* **Mechanism**: Interlocking legs and tarsal claws to build living hanging walls and thermal nest bivouacs during nomadic phases.

### 164. Bumblebee Buzz Pollination Sonication (`BuzzPollinationSystem`)
* **Capability Flag**: `canPerformBuzzPollinationSonication()`
* **Key Species**: *Bombus terrestris*
* **Mechanism**: Vibrating flight muscles at high frequency to shake pollen free from stubborn anthers.

### 165. Subsocial Earwig Maternal Food Regurgitation (`EarwigMaternalRegurgitationSystem`)
* **Capability Flag**: `canRegurgitateEarwigMaternalFood()`
* **Key Species**: *Forficula auricularia*
* **Mechanism**: Regurgitating crop contents to feed first-instar nymphs inside dark maternal incubation chambers.

### 166. Paper Wasp Facial Pattern Recognition (`WaspFacialRecognitionSystem`)
* **Capability Flag**: `canRecognizeWaspFacialPatterns()`
* **Key Species**: *Polistes fuscatus*
* **Mechanism**: Visual recognition of individual facial markings to enforce dominance hierarchies without fight energy waste.

### 167. Eusocial Shrimp Acoustic Cannon Defense (`ShrimpAcousticCannonSystem`)
* **Capability Flag**: `canFireShrimpAcousticCannon()`
* **Key Species**: *Synalpheus regalis*
* **Mechanism**: Firing high-speed water jets creating cavitation bubbles (up to 200 dB) against sponge intruders.

### 168. Passalid Beetle Substratal Sound Duet Communication (`PassalidSubstrateDuetSystem`)
* **Capability Flag**: `canDuetPassalidSubstrateVibration()`
* **Key Species**: *Odontotaenius disjunctus*
* **Mechanism**: Adult-pupal vibrational acoustic duetting coordinating gallery movements inside decaying logs.

### 169. Harvester Ant Granary Aeration Turning (`GranarySeedAerationSystem`)
* **Capability Flag**: `canTurnGranarySeedsAeration()`
* **Key Species**: *Messor barbarus*
* **Mechanism**: Periodically moving and turning stored seed stockpiles to prevent humidity accumulation and fungal rot.

### 170. Honeybee Waggle Dance Sun-Compass Direction Encoding (`WaggleDanceSunCompassSystem`)
* **Capability Flag**: `canEncodeWaggleDanceSunCompass()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Transducing waggle dance angles relative to gravity into sun-compass flight vectors for foraging recruits.

### 171. Termite Subterranean Clay Aqueduct Digging (`SubterraneanClayAqueductSystem`)
* **Capability Flag**: `canDigSubterraneanClayAqueducts()`
* **Key Species**: *Coptotermes formosanus*
* **Mechanism**: Digging deep vertical wells down to water tables to bring moisture up to dry mounds during droughts.

### 172. Formica Ant Acid-Jet Artillery Repellent (`FormicAcidArtilleryJetSystem`)
* **Capability Flag**: `canFireFormicAcidArtilleryJet()`
* **Key Species**: *Formica lugubris*
* **Mechanism**: Arching gaster forward to spray formic acid jets up to 30 cm against vertebrate predators.

### 173. Social Spider Nest Web Garbage Chute Ejection (`SpiderGarbageChuteSystem`)
* **Capability Flag**: `canEjectGarbageChuteRefuse()`
* **Key Species**: *Stegodyphus lineatus*
* **Mechanism**: Dropping prey carcasses and molted cuticles through dedicated vertical silk chute funnels to prevent disease.

### 174. Bumblebee Thermoregulatory Wing Fanning (`BroodThermoregulatoryWingFanningSystem`)
* **Capability Flag**: `canFanWingsForBroodThermoregulation()`
* **Key Species**: *Bombus hypnorum*
* **Mechanism**: Fanning wings over brood comb cells during high ambient heat to cool larvae and prevent thermal stress.

### 175. Thrips Soldier Tube Gall Defense Plugging (`ThripsChitinousTubePlugSystem`)
* **Capability Flag**: `canPlugGallWithChitinousTube()`
* **Key Species**: *Kladothrips hamiltoni*
* **Mechanism**: Using flattened sclerotized abdominal tips to plug narrow gall entrance tubes against invading kleptoparasites.

### 176. Paper Wasp Pedicel Chemical Ant Repellent Coating (`WaspPedicelAntRepellentSystem`)
* **Capability Flag**: `canCoatWaspPedicelAntRepellent()`
* **Key Species**: *Polistes dominula*
* **Mechanism**: Applying abdominal gland secretions onto nest attachment pedicels to deter climbing ants.

### 177. Termite Nasute Secretion Resin Immobilization (`NasuteViscousResinSquirtSystem`)
* **Capability Flag**: `canSquirtNasuteViscousResin()`
* **Key Species**: *Nasutitermes exitiosus*
* **Mechanism**: Squirting sticky terpenoid threads from frontal nozzle to entangle enemy ants.

### 178. Aphid Soldier Foreleg Squeezing (`AphidForelegIntruderSqueezeSystem`)
* **Capability Flag**: `canSqueezeIntrudersWithForelegs()`
* **Key Species**: *Pseudoregma bambucicola*
* **Mechanism**: Squeezing predatory fly larvae against plant stems using enlarged spinous forelegs.

### 179. Subsocial Shield Bug Egg Guarding Parasitoid Shielding (`ShieldBugParasitoidShieldSystem`)
* **Capability Flag**: `canShieldEggsFromParasitoidWasps()`
* **Key Species**: *Elasmucha fieberi*
* **Mechanism**: Tilting body armor to block parasitoid wasp ovipositors from reaching egg clutches.

### 180. Horned Beetle Wood Wall Plastering (`PassalidWoodWallPlasterSystem`)
* **Capability Flag**: `canPlasterWoodWallGallery()`
* **Key Species**: *Odontotaenius disjunctus*
* **Mechanism**: Mixing wood dust with salivary secretions to seal damaged gallery tunnels.

### 181. Atta Ant Leaf-Cutting Crescent Mandible Shearing (`AttaLeafCrescentShearSystem`)
* **Capability Flag**: `canShearLeafCrescentMandible()`
* **Key Species**: *Atta cephalotes*
* **Mechanism**: Rhythmic shearing of foliage into transportable crescent leaf discs using specialized mandible teeth.

### 182. Honeybee Swarm Thermoregulation Heat Shielding (`HoneybeeSwarmCoreHeatShieldSystem`)
* **Capability Flag**: `canShieldSwarmCoreHeat()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Forming outer mantle layers of cool bees around the warm core during swarm cluster flight.

### 183. Termite Queen Physogastric Egg Laying Peristalsis (`TermiteQueenPhysogastricEggPeristalsisSystem`)
* **Capability Flag**: `canPerformQueenPhysogastricPeristalsis()`
* **Key Species**: *Macrotermes natalensis*
* **Mechanism**: Rhythmic abdominal peristalsis producing 30,000 eggs per day inside the royal chamber.

### 184. Social Caterpillar Silk Net Hammock Weaving (`CaterpillarSilkHammockTentSystem`)
* **Capability Flag**: `canWeaveSocialSilkHammock()`
* **Key Species**: *Malacosoma americanum*
* **Mechanism**: Building multi-layered silk tent hammocks for group thermal buffering and predator avoidance.

### 185. Bumblebee Pollen Basket Packing Mechanics (`CorbiculaPollenPackingSystem`)
* **Capability Flag**: `canPackCorbiculaPollenBaskets()`
* **Key Species**: *Bombus terrestris*
* **Mechanism**: Moistening pollen with nectar and combing it into hind tibia corbiculae during flight.

### 186. Paper Wasp Nest Carton Pulp Scraping (`WoodPulpCartonScrapeSystem`)
* **Capability Flag**: `canScrapeWoodPulpCarton()`
* **Key Species**: *Vespa crabro*
* **Mechanism**: Scraping weathered wood fibres with mandibles to produce paper pulp carton for nest comb construction.

### 187. Weaver Ant Egg Laying Trophic Nourishment (`TrophicEggNourishmentSystem`)
* **Capability Flag**: `canLayTrophicNourishmentEggs()`
* **Key Species**: *Oecophylla smaragdina*
* **Mechanism**: Laying unfertilized trophic eggs to feed queen and young larvae during colony founding.

### 188. Desert Ant Celestial Polarization Navigation (`PolarizedLightCompassNavigationSystem`)
* **Capability Flag**: `canNavigatePolarizedLightCompass()`
* **Key Species**: *Cataglyphis fortis*
* **Mechanism**: Using dorsal rim area ommatidia to decode UV atmospheric polarization patterns for path integration in featureless deserts.

### 189. Termite Gallery Fungal Waste Isolation Burial (`TermiteFungalWasteBurialSystem`)
* **Capability Flag**: `canBuryFungalWasteInGallery()`
* **Key Species**: *Odontotermes obesus*
* **Mechanism**: Sealing senescent fungal comb waste in dead-end gallery vaults to protect healthy fungus gardens.

### 190. Harvester Ant Seed Coat Husk Chewing (`HarvesterAntBreadPulpChewSystem`)
* **Capability Flag**: `canChewSeedHuskBreadPulp()`
* **Key Species**: *Messor barbarus*
* **Mechanism**: Chewing seed starch mixed with amylase saliva to produce "ant bread" for worker and larval consumption.

### 191. Social Spider Prey Carcass Wrapping (`SpiderCommunalSilkPreyWrapSystem`)
* **Capability Flag**: `canWrapPreyInCommunalSilk()`
* **Key Species**: *Stegodyphus dumicola*
* **Mechanism**: Multi-spider wrapping of large captured grasshoppers in dense silk sheets to prevent prey escape.

### 192. Honeybee Propolis Tree Resin Sealing (`HoneybeePropolisNestSealSystem`)
* **Capability Flag**: `canSealNestGapsWithPropolis()`
* **Key Species**: *Apis mellifera*
* **Mechanism**: Collecting tree propolis resins to caulk micro-cracks and sterilize hive walls against bacterial spores.

### 193. Termite Soldier Alarm Drumming Synchrony (`TermiteSoldierAlarmDrumSynchronySystem`)
* **Capability Flag**: `canSynchronizeSoldierAlarmDrumming()`
* **Key Species**: *Reticulitermes flavipes*
* **Mechanism**: Phase-locking head drumming across hundreds of soldiers to amplify substrate vibration throughout nest galleries.

### 194. Paper Wasp Dominance Mounting Behavior (`WaspDominanceMountingSystem`)
* **Capability Flag**: `canPerformDominanceMounting()`
* **Key Species**: *Polistes dominula*
* **Mechanism**: Dominance mounting and antennal drumming to suppress worker ovarian development in social hierarchies.

### 195. Bumblebee Nectar Reservoir Lapping (`BumblebeeNectarTongueLappingSystem`)
* **Capability Flag**: `canLapNectarTongueExtension()`
* **Key Species**: *Bombus hortorum*
* **Mechanism**: Extending glossal tongue into deep floral nectaries to lap high-viscosity nectar efficiently.

### 196. Aphid Gall Wall Gall-Closing Secretion (`AphidGallClosingFluidSystem`)
* **Capability Flag**: `canSecreteGallClosingFluid()`
* **Key Species**: *Ceratovacuna lanigera*
* **Mechanism**: Secreting liquid waxy droplets to plug natural openings in plant galls against predators.

### 197. Subsocial Earwig Nymph Grooming Cleaning (`EarwigNymphCuticularGroomingSystem`)
* **Capability Flag**: `canGroomNymphCuticularSurface()`
* **Key Species**: *Forficula auricularia*
* **Mechanism**: Licking cuticular surfaces of newly molted nymphs to remove exuviae and pathogens.

### 198. Atta Ant Garden Waste Dump Excavation (`AttaGardenWasteChamberDigSystem`)
* **Capability Flag**: `canExcavateGardenWasteChambers()`
* **Key Species**: *Atta sexdens*
* **Mechanism**: Digging deep subterranean waste chambers for toxic spent fungal substrate and dead ants.

### 199. Termite Royal Pair Grooming Exchange (`TermiteRoyalPairGroomingSystem`)
* **Capability Flag**: `canExchangeRoyalPairGrooming()`
* **Key Species**: *Cryptotermes secundus*
* **Mechanism**: Mutual grooming and pheromonal exchange between king and queen inside the royal cell.

### 200. Eusocial Arthropod Universal Emergency Evacuation (`UniversalEmergencyEvacuationSystem`)
* **Capability Flag**: `canTriggerUniversalEmergencyEvacuation()`
* **Key Species**: *SwarmForge Ethological Engine Core*
* **Mechanism**: Colony-wide emergency evacuation trigger evacuating brood, reserves, and queen during catastrophic nest collapse.

### 201. Myrmecocystus Honeypot Replete Storage (`HoneypotRepleteStorageSystem`)
* **Capability Flag**: `canStoreNectarAsHoneypotReplete()`
* **Key Species**: *Myrmecocystus mexicanus*
* **Mechanism**: Distending gaster with liquid nectar as hanging living honeypot repletes.

### 202. Formica Ant Raft Flood Navigation (`FloatingAntRaftSystem`)
* **Capability Flag**: `canFormFloatingAntRaft()`
* **Key Species**: *Solenopsis invicta* / *Formica*
* **Mechanism**: Interlocking bodies with hydrophobic cuticles to form floating rafts carrying queen and brood during floods.

### 203. Meliponini Stingless Bee Mud-Resin Entrance Funnel (`MudResinEntranceFunnelSystem`)
* **Capability Flag**: `canConstructMudResinEntranceFunnel()`
* **Key Species**: *Melipona quadrifasciata*
* **Mechanism**: Building trumpet-shaped mud and resin entrance tubes guarded by specialized stingless bee soldiers.

### 204. Bombus Queen Hibernation Burrow Excavation (`QueenHibernationBurrowSystem`)
* **Capability Flag**: `canExcavateHibernationBurrow()`
* **Key Species**: *Bombus terrestris*
* **Mechanism**: Solitary fertilized autumn queen digging subterranean overwintering hibernacula in soil banks.

### 205. Acromyrmex Leaf Micro-Mastication & Enzyme Inoculation (`LeafPulpEnzymeInoculationSystem`)
* **Capability Flag**: `canInoculateLeafPulpEnzymes()`
* **Key Species**: *Acromyrmex octospinosus*
* **Mechanism**: Micro-masticating leaf margins into fine pulp and applying digestive enzymes before comb insertion.

### 206. Dinoponera Gamergate Dominance Tournament (`GamergateDominanceTournamentSystem`)
* **Capability Flag**: `canPerformGamergateDominanceTournament()`
* **Key Species**: *Dinoponera quadriceps*
* **Mechanism**: Gamergate physical dominance tournaments with abdominal rubbing and sting-smearing to establish reproductive status in queenless ant colonies.

### 207. Dracula Ant Subsocial Larval Hemolymph Feeding (`DraculaAntLarvalHemolymphSystem`)
* **Capability Flag**: `canFeedOnLarvalHemolymphDracula()`
* **Key Species**: *Stigmatomma pallipes*
* **Mechanism**: Non-fatally puncturing larval integument to drink hemolymph droplets during food scarcity.

### 208. Oecophylla Leaf Tarsal Friction Gripping Bridge (`TarsalFrictionBridgeSystem`)
* **Capability Flag**: `canFormTarsalFrictionBridge()`
* **Key Species**: *Oecophylla smaragdina*
* **Mechanism**: Forming multi-individual tensile bridges pulling heavy tree branches together using tarsal adhesive pads.

### 209. Pachycondyla Mandibular Droplet Water Transport (`MandibleDropletWaterTransportSystem`)
* **Capability Flag**: `canTransportWaterInMandibleDroplet()`
* **Key Species**: *Pachycondyla villosa*
* **Mechanism**: Transporting surface tension water droplets trapped between mandibles for brood hydration.

### 210. Cataglyphis High-Temperature Stilt Walking (`DesertAntStiltWalkingSystem`)
* **Capability Flag**: `canStiltWalkThermalRegim()`
* **Key Species**: *Cataglyphis bombycina*
* **Mechanism**: Raising body high on long legs ("stilt walking") and pausing on dry grass stems to cool off above 50°C desert sand.

### 211. Giant Honeybee Anti-Predator Shimmering Wave (`GiantHoneybeeShimmeringWaveSystem`)
* **Capability Flag**: `canPerformAntiPredatorShimmeringWave()`
* **Key Species**: *Apis dorsata*
* **Mechanism**: Synchronized abdomen flipping creating visible shimmering waves across open-nest comb curtain to confuse hornet scouts.

### 212. Paper Wasp Nest Water Dousing Evaporative Cooling (`PaperWaspWaterDousingSystem`)
* **Capability Flag**: `canDouseNestWaterCooling()`
* **Key Species**: *Polistes dominula*
* **Mechanism**: Carrying water droplets in crop and spitting them on paper comb cells combined with fanning to evaporatively cool nest.

### 213. Termite Clay Wall Fungal Chamber Aeration (`ClayWallFungalAerationSystem`)
* **Capability Flag**: `canAerateFungalCombChambers()`
* **Key Species**: *Odontotermes obesus*
* **Mechanism**: Chewing micro-perforations through clay chamber walls to maintain optimal CO2 exchange for fungus gardens.

### 214. Passalid Beetle Larval Exuvia Chitin Recycling (`LarvalExuviaChitinRecyclingSystem`)
* **Capability Flag**: `canFeedLarvaeExuviaRecycling()`
* **Key Species**: *Odontotaenius disjunctus*
* **Mechanism**: Feeding molted chitinous exuviae back to larvae to recycle essential nitrogenous nutrients.

### 215. Atta Minim Leaf Cleansing Allogrooming (`MinimLeafParasiteGroomingSystem`)
* **Capability Flag**: `canGroomLeafPulpParasitesMinim()`
* **Key Species**: *Atta cephalotes*
* **Mechanism**: Tiny minim workers riding on harvested leaf discs to clean parasitic phorid fly eggs off larger leaf-cutter foragers.

### 216. Social Spider Nest Web Plant Debris Camouflage (`SpiderWebDebrisCamouflageSystem`)
* **Capability Flag**: `canCamouflageWebWithPlantDebris()`
* **Key Species**: *Stegodyphus dumicola*
* **Mechanism**: Weaving dry twigs, leaves, and prey husks into outer silk web walls to disguise the nest from bird predators.

### 217. Acrobat Ant Cocked-Gaster Chemical Defense (`AcrobatAntGasterVenomSystem`)
* **Capability Flag**: `canCockGasterFormicAcidRepellent()`
* **Key Species**: *Crematogaster scutellaris*
* **Mechanism**: Arching heart-shaped gaster overhead to deposit defensive venom droplets on intruders.

### 218. Lasius Ant Aphid Honeydew Antennal Milking (`AphidHoneydewMilkingSystem`)
* **Capability Flag**: `canMilkAphidHoneydewStroking()`
* **Key Species**: *Lasius niger*
* **Mechanism**: Rhythmic antennal stroking of aphid abdomens to solicit honeydew excretion.

### 219. Formica Ant Mound Solar Heat Collector Clustering (`MoundSolarHeatCollectorSystem`)
* **Capability Flag**: `canClusterSolarHeatCollector()`
* **Key Species**: *Formica polyctena*
* **Mechanism**: Workers basking in morning sunlight on mound surfaces and carrying absorbed body heat back down into deep subterranean brood chambers.

### 220. SwarmForge Global Ethological BitSet Serialization (`GlobalEthologicalBitSetSerializationSystem`)
* **Capability Flag**: `canSerializeGlobalEthologicalBitSet()`
* **Key Species**: *SwarmForge Ethological Engine Core*
* **Mechanism**: Compact binary BitSet state serialization supporting ultra-high throughput state sync across distributed compute nodes.

---

## Real-Time Inspector & Mouseover Integration
When tracking or hovering over any individual in the simulation view (`WorldEditorPane`), the active behavioral flags for that individual's species are displayed in the HUD overlay:
```text
🎯 FOURMI SUIVIE : WORKER #a1b2c3d4
💓 Santé : 100 / 100
⚡ Énergie: 95% | 🍗 Faim: 5% | 💧 Soif: 0%
🎂 Âge: 2.4 jours | Stade: ADULT | Tâche: FORAGER
🧠 État IA & Comportements: FORAGE [HYDROPHOBIC_TRAIL_COATING | PREFLIGHT_QUEEN_NOURISHMENT | RELAY_SEED_TRANSPORT | CHC_GESTALT_HARMONIZATION]
📍 Pos 3D: (X: 24.5, Y: 12.0, Z: 0.0)
🚀 Cap (Heading): 180° | 📦 Cargo: Aucun
🧪 Gestalt Hydrocarbonée Cuticulaire (CHC): Authentifiée
```

---

## Architectural Performance Strategy
To maintain high performance ($60\,\text{FPS}$) during large-scale simulations (over $100,000$ active agents):
1. **Capability Flag Bitmasking**: Species capability flags are stored as primitive boolean fields and cached in `Species` interfaces for $O(1)$ lookups.
2. **Lazy State Evaluation**: Behavior simulation handlers (`HydrophobicTrailCoatingSystem`, `HoneyStoreBrickPluggingSystem`, etc.) are triggered conditionally only when state thresholds or environmental triggers are met.
3. **Statistical Sampling over Full Logging**: Event journaling relies on interval sampling (e.g. every 60 ticks) rather than per-tick allocation, avoiding CPU overhead and garbage collection pauses.
