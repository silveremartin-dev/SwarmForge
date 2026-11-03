export const DEFAULT_WORLD_PRESETS = [
    {
        id: 'world_terrarium_01',
        name: 'My Terrarium No. 1',
        description: 'Temperate sub-millimeter terrarium with clay-sandy substrate and micro-hydrography',
        scale: { sizeX: 2.0, sizeY: 2.0, resolutionMm: 0.5 },
        terrain: { roughness: 0.45, compaction: 65, soilComposition: { earth: 50, sand: 20, clay: 20, stone: 10 } },
        hydrology: { hasRiver: true, riverWidthMm: 120, riverFlowVelocity: 0.3, staticPools: 2 },
        structures: { treesCount: 2, hollowLogs: 1, nestHostType: 'SUBTERRANEAN_AND_ARBOREAL' }
    },
    {
        id: 'world_tropical_rainforest',
        name: 'Tropical Rainforest',
        description: 'Rich litter substrate, high humidity, dense trees and torrential micro-stream',
        scale: { sizeX: 3.5, sizeY: 3.5, resolutionMm: 0.4 },
        terrain: { roughness: 0.70, compaction: 40, soilComposition: { earth: 70, sand: 10, clay: 10, stone: 10 } },
        hydrology: { hasRiver: true, riverWidthMm: 250, riverFlowVelocity: 0.8, staticPools: 4 },
        structures: { treesCount: 5, hollowLogs: 3, nestHostType: 'ARBOREAL_LEAF' }
    },
    {
        id: 'world_desert_canyon',
        name: 'Arid Desert & Caves',
        description: 'Unstable sandy substrate, absence of surface water, extreme temperatures',
        scale: { sizeX: 4.0, sizeY: 4.0, resolutionMm: 0.8 },
        terrain: { roughness: 0.30, compaction: 85, soilComposition: { earth: 10, sand: 70, clay: 10, stone: 10 } },
        hydrology: { hasRiver: false, riverWidthMm: 0, riverFlowVelocity: 0, staticPools: 0 },
        structures: { treesCount: 0, hollowLogs: 0, nestHostType: 'SUBTERRANEAN_DEEP' }
    }
]

export const DEFAULT_SPECIES_PRESETS = [
    {
        id: 'species_formica_fusca',
        name: 'Formica fusca (Black Wood Ant)',
        description: 'Agile honeydew harvester species, high reactivity and Lévy flight strategy',
        taxonomicalClass: 'Formicinae',
        morphology: { workerLengthMm: 5.5, soldierLengthMm: 7.0, colorHex: '#2a221b' },
        behavior: { foragingStrategy: 'LEVY_FLIGHT', aggressionLevel: 0.4, trailDecayRate: 0.05 }
    },
    {
        id: 'species_messor_barbarus',
        name: 'Messor barbarus (Harvester Ant)',
        description: 'Polymorphic species specialized in seed harvesting and ant bread production',
        taxonomicalClass: 'Myrmicinae',
        morphology: { workerLengthMm: 4.0, soldierLengthMm: 11.0, colorHex: '#7f1d1d' },
        behavior: { foragingStrategy: 'PHYSICAL_TRAIL', aggressionLevel: 0.6, trailDecayRate: 0.02 }
    },
    {
        id: 'species_linepithema_humile',
        name: 'Linepithema humile (Argentine Ant)',
        description: 'Polygyne invasive species forming aggressive supercolonies without territorial borders',
        taxonomicalClass: 'Dolichoderinae',
        morphology: { workerLengthMm: 2.8, soldierLengthMm: 3.2, colorHex: '#92400e' },
        behavior: { foragingStrategy: 'MASS_RECRUITMENT', aggressionLevel: 0.95, trailDecayRate: 0.01 }
    }
]

export const DEFAULT_NEST_PRESETS = [
    {
        id: 'nest_dome_brindilles',
        name: 'Twig Dome & Damp Galleries',
        description: 'Thermoregulated surface dome nest with subterranean gallery network',
        nestType: 'DOME_AND_SUBTERRANEAN',
        depthCm: 25,
        chambers: { queen: 1, nursery: 3, food: 4, waste: 2 },
        tunnelDiameterMm: 6.0
    },
    {
        id: 'nest_messor_granary',
        name: 'Messor Sub-Surface Granaries',
        description: 'Dry chambers dedicated to seed storage and crushing rooms',
        nestType: 'SUBTERRANEAN_GRANARY',
        depthCm: 45,
        chambers: { queen: 1, nursery: 4, food: 8, waste: 3 },
        tunnelDiameterMm: 8.5
    },
    {
        id: 'nest_arboreal_log',
        name: 'Arboreal Hollow Log Lodge',
        description: 'Nest hollowed into decaying deadwood with tiered micro-chambers',
        nestType: 'ARBOREAL_WOOD',
        depthCm: 10,
        chambers: { queen: 1, nursery: 2, food: 2, waste: 1 },
        tunnelDiameterMm: 4.5
    }
]

export const DEFAULT_PREY_PREDATOR_PRESETS = [
    {
        id: 'prey_pred_pucerons_fourmilion',
        name: 'Aphid & Antlion Ecosystem',
        description: 'Honeydew-producing aphid herd + Antlion funnel traps',
        prey: [
            { type: 'APHID', name: 'Thistle Aphids (Aphis fabae)', count: 60, honeydewRate: 0.8 },
            { type: 'SEED', name: 'Grass Seeds', count: 150 }
        ],
        predators: [
            { type: 'ANTLION', name: 'Antlion Larvae (Myrmeleon)', count: 2, aggression: 0.9 }
        ]
    },
    {
        id: 'prey_pred_vespula_attack',
        name: 'Solitary Wasp & Spider Incursion',
        description: 'Marauding predatory wasp (Vespula) and jumping spider',
        prey: [
            { type: 'CATERPILLAR', name: 'Lymantria Caterpillar', count: 5, honeydewRate: 0 }
        ],
        predators: [
            { type: 'WASP', name: 'Solitary Wasp (Vespula germanica)', count: 1, aggression: 1.0 },
            { type: 'SPIDER', name: 'Jumping Spider (Salticidae)', count: 3, aggression: 0.75 }
        ]
    },
    {
        id: 'prey_pred_peaceful_abundance',
        name: 'Abundant Resources without Predators',
        description: 'Abundance of harmless prey and seeds for rapid colony expansion',
        prey: [
            { type: 'APHID', name: 'Green Aphids', count: 120, honeydewRate: 1.0 },
            { type: 'SEED', name: 'Pine & Grass Seeds', count: 300 }
        ],
        predators: []
    }
]

export const DEFAULT_WEATHER_PRESETS = [
    {
        id: 'weather_printemps_doux',
        name: 'Mild Spring (22°C, 14h Daylight, Light Rain)',
        description: 'Balanced conditions ideal for spring activity',
        season: 'SPRING',
        photoperiodHours: 14,
        tempDay: 22,
        tempNight: 14,
        precipitationMm: 8,
        humidity: 65
    },
    {
        id: 'weather_tropical_rainforest',
        name: 'Tropical Monsoon & Rainforest (30°C, 95% Humidity, Heavy Rain)',
        description: 'Warm, dense equatorial climate with constant high humidity and frequent tropical showers',
        season: 'SUMMER',
        photoperiodHours: 12,
        tempDay: 30,
        tempNight: 24,
        precipitationMm: 50,
        humidity: 95
    },
    {
        id: 'weather_desert_arid',
        name: 'Arid Desert & Scorching Sun (42°C, 10% Humidity, Zero Rain)',
        description: 'Extreme thermal amplitude, high UV index and hyper-arid substrate',
        season: 'SUMMER',
        photoperiodHours: 15,
        tempDay: 42,
        tempNight: 12,
        precipitationMm: 0,
        humidity: 12
    },
    {
        id: 'weather_ete_caniculaire',
        name: 'Heatwave Summer & Storms (34°C, Torrential Rain)',
        description: 'High daytime heat triggering violent storms and lightning',
        season: 'SUMMER',
        photoperiodHours: 16,
        tempDay: 34,
        tempNight: 24,
        precipitationMm: 35,
        humidity: 80
    },
    {
        id: 'weather_automne_frais',
        name: 'Cool & Windy Autumn (14°C, Fog)',
        description: 'Gradual cooling, moderate winds and morning fog',
        season: 'AUTUMN',
        photoperiodHours: 10,
        tempDay: 14,
        tempNight: 8,
        precipitationMm: 18,
        humidity: 88
    }
]

export const DEFAULT_SCENARIO_META_PRESETS = [
    {
        id: 'CUSTOM_NEW',
        name: '[➕ Scénario Personnalisé (Création libre)]',
        description: 'Scénario personnalisé : définissez librement le biotope, le climat, les espèces et la topologie mégaterrarium.',
        academicCategory: 'Custom Creation',
        masterSeed: 12345,
        worldPresetId: 'world_terrarium_01',
        speciesPresetId: 'species_formica_fusca',
        nestPresetId: 'nest_dome_brindilles',
        preyPredatorPresetId: 'prey_pred_pucerons_fourmilion',
        weatherPresetId: 'weather_printemps_doux',
        isMultiplayerOnly: false,
        requiredPlayerCount: 1,
        gridTilesX: 1,
        gridTilesY: 1
    },
    {
        id: 'ACAD_01_LEVY_BROWNIAN',
        name: '🔬 Dispersion & Mouvement Brownien - Lévy Flight (Formica fusca)',
        description: 'Étude académique des trajectoires optimales de fourragement en vol de Lévy vs marche brownienne.',
        academicCategory: 'Optimal Foraging & Bio-Kinetics',
        masterSeed: 12345,
        worldPresetId: 'world_terrarium_01',
        speciesPresetId: 'species_formica_fusca',
        nestPresetId: 'nest_dome_brindilles',
        preyPredatorPresetId: 'prey_pred_pucerons_fourmilion',
        weatherPresetId: 'weather_printemps_doux',
        isMultiplayerOnly: false,
        requiredPlayerCount: 1,
        gridTilesX: 1,
        gridTilesY: 1
    },
    {
        id: 'ACAD_02_TAYLOR_EXCAVATION',
        name: '🔬 Dynamique d\'Excavation & Granulométrie (Messor barbarus)',
        description: 'Comportement d\'excavation souterraine, stockage granivore et résistance des galeries aux contraintes mécaniques.',
        academicCategory: 'Division of Labor & Granaries',
        masterSeed: 424242,
        worldPresetId: 'world_desert_canyon',
        speciesPresetId: 'species_messor_barbarus',
        nestPresetId: 'nest_messor_granary',
        preyPredatorPresetId: 'prey_pred_peaceful_abundance',
        weatherPresetId: 'weather_desert_arid',
        isMultiplayerOnly: false,
        requiredPlayerCount: 1,
        gridTilesX: 1,
        gridTilesY: 1
    },
    {
        id: 'ACAD_03_COLONY_CONFLICT',
        name: '🔬 Écologie des Populations & Compétition Interspécifique (Linepithema)',
        description: 'Expansion territoriale agressive d\'une supercolonie invasive face à des colonies autochtones.',
        academicCategory: 'Population Ecology & Competition',
        masterSeed: 777777,
        worldPresetId: 'world_tropical_rainforest',
        speciesPresetId: 'species_linepithema_humile',
        nestPresetId: 'nest_arboreal_log',
        preyPredatorPresetId: 'prey_pred_vespula_attack',
        weatherPresetId: 'weather_tropical_rainforest',
        isMultiplayerOnly: false,
        requiredPlayerCount: 1,
        gridTilesX: 1,
        gridTilesY: 1
    },
    {
        id: 'MP_01_TERRITORIAL_WAR',
        name: '⚔️ Guerre Territoriale & Conquête (2 Joueurs)',
        description: 'Affrontement symétrique 1v1 pour le contrôle des sources de nourriture centrales et domination territoriale.',
        academicCategory: 'Multiplayer Competitive Warfare',
        masterSeed: 10101,
        worldPresetId: 'world_terrarium_01',
        speciesPresetId: 'species_formica_fusca',
        nestPresetId: 'nest_dome_brindilles',
        preyPredatorPresetId: 'prey_pred_pucerons_fourmilion',
        weatherPresetId: 'weather_printemps_doux',
        isMultiplayerOnly: true,
        requiredPlayerCount: 2,
        gridTilesX: 2,
        gridTilesY: 1
    },
    {
        id: 'MP_02_MEGATERRARIUM_COOP',
        name: '⚔️ Mégaterrarium Coopératif Shardé (4 Joueurs - Grille 2x2)',
        description: 'Grille 2x2 de 4 sous-volumes interconnectés avec halo de phéromones continu et migration d\'individus.',
        academicCategory: 'Distributed Sharded Megaterrarium',
        masterSeed: 20202,
        worldPresetId: 'world_tropical_rainforest',
        speciesPresetId: 'species_linepithema_humile',
        nestPresetId: 'nest_arboreal_log',
        preyPredatorPresetId: 'prey_pred_peaceful_abundance',
        weatherPresetId: 'weather_tropical_rainforest',
        isMultiplayerOnly: true,
        requiredPlayerCount: 4,
        gridTilesX: 2,
        gridTilesY: 2
    },
    {
        id: 'MP_03_TRI_COLONY_TOURNAMENT',
        name: '⚔️ Tournoi Tri-Colonial (3 Joueurs - Grille 3x1)',
        description: 'Compétition triangulaire asymétrique entre trois espèces aux stratégies divergentes.',
        academicCategory: 'Multiplayer Asymmetric Tournament',
        masterSeed: 30303,
        worldPresetId: 'world_desert_canyon',
        speciesPresetId: 'species_messor_barbarus',
        nestPresetId: 'nest_messor_granary',
        preyPredatorPresetId: 'prey_pred_vespula_attack',
        weatherPresetId: 'weather_desert_arid',
        isMultiplayerOnly: true,
        requiredPlayerCount: 3,
        gridTilesX: 3,
        gridTilesY: 1
    },
    {
        id: 'MP_04_MEGATERRARIUM_8X8',
        name: '⚔️ Mégaterrarium Géant Distribué (16 Joueurs - Grille 4x4)',
        description: 'Simulation distribuée massive sur cluster haute performance avec 16 colonies réparties sur 16 sous-volumes.',
        academicCategory: 'HPC Extreme Scale Simulation',
        masterSeed: 88888,
        worldPresetId: 'world_terrarium_01',
        speciesPresetId: 'species_formica_fusca',
        nestPresetId: 'nest_dome_brindilles',
        preyPredatorPresetId: 'prey_pred_peaceful_abundance',
        weatherPresetId: 'weather_printemps_doux',
        isMultiplayerOnly: true,
        requiredPlayerCount: 16,
        gridTilesX: 4,
        gridTilesY: 4
    }
]
