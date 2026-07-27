import { create } from 'zustand'

export const DEFAULT_WORLD_PRESETS = [
    {
        id: 'world_terrarium_01',
        name: 'Mon Terrarium N°1',
        description: 'Terrarium tempéré sub-millimétrique avec substrat argilo-sableux et micro-hydrographie',
        scale: { sizeX: 2.0, sizeY: 2.0, resolutionMm: 0.5 },
        terrain: { roughness: 0.45, compaction: 65, soilComposition: { earth: 50, sand: 20, clay: 20, stone: 10 } },
        hydrology: { hasRiver: true, riverWidthMm: 120, riverFlowVelocity: 0.3, staticPools: 2 },
        structures: { treesCount: 2, hollowLogs: 1, nestHostType: 'SUBTERRANEAN_AND_ARBOREAL' }
    },
    {
        id: 'world_tropical_rainforest',
        name: 'Forêt Tropicale Humide',
        description: 'Substrat litière riche, humidité élevée, arbres denses et micro-cours d\'eau torrentiel',
        scale: { sizeX: 3.5, sizeY: 3.5, resolutionMm: 0.4 },
        terrain: { roughness: 0.70, compaction: 40, soilComposition: { earth: 70, sand: 10, clay: 10, stone: 10 } },
        hydrology: { hasRiver: true, riverWidthMm: 250, riverFlowVelocity: 0.8, staticPools: 4 },
        structures: { treesCount: 5, hollowLogs: 3, nestHostType: 'ARBOREAL_LEAF' }
    },
    {
        id: 'world_desert_canyon',
        name: 'Désert Aride & Grottes',
        description: 'Substrat sableux instable, absence d\'eau de surface, températures extrêmes',
        scale: { sizeX: 4.0, sizeY: 4.0, resolutionMm: 0.8 },
        terrain: { roughness: 0.30, compaction: 85, soilComposition: { earth: 10, sand: 70, clay: 10, stone: 10 } },
        hydrology: { hasRiver: false, riverWidthMm: 0, riverFlowVelocity: 0, staticPools: 0 },
        structures: { treesCount: 0, hollowLogs: 0, nestHostType: 'SUBTERRANEAN_DEEP' }
    }
]

export const DEFAULT_SPECIES_PRESETS = [
    {
        id: 'species_formica_fusca',
        name: 'Formica fusca (Fourmi Noire des Bois)',
        description: 'Espèce agile récolteuse de miellat, forte réactivité et stratégie de vol de Lévy',
        taxonomicalClass: 'Formicinae',
        morphology: { workerLengthMm: 5.5, soldierLengthMm: 7.0, colorHex: '#2a221b' },
        behavior: { foragingStrategy: 'LEVY_FLIGHT', aggressionLevel: 0.4, trailDecayRate: 0.05 }
    },
    {
        id: 'species_messor_barbarus',
        name: 'Messor barbarus (Fourmi Moissonneuse)',
        description: 'Espèce polymorphe spécialisée dans la récolte de graines et fabrication du pain de fourmi',
        taxonomicalClass: 'Myrmicinae',
        morphology: { workerLengthMm: 4.0, soldierLengthMm: 11.0, colorHex: '#7f1d1d' },
        behavior: { foragingStrategy: 'PHYSICAL_TRAIL', aggressionLevel: 0.6, trailDecayRate: 0.02 }
    },
    {
        id: 'species_linepithema_humile',
        name: 'Linepithema humile (Fourmi d\'Argentine)',
        description: 'Espèce invasive polygyne formant de supercolonies agressives sans frontières territoriales',
        taxonomicalClass: 'Dolichoderinae',
        morphology: { workerLengthMm: 2.8, soldierLengthMm: 3.2, colorHex: '#92400e' },
        behavior: { foragingStrategy: 'MASS_RECRUITMENT', aggressionLevel: 0.95, trailDecayRate: 0.01 }
    }
]

export const DEFAULT_NEST_PRESETS = [
    {
        id: 'nest_dome_brindilles',
        name: 'Dôme de Brindilles & Galeries Humides',
        description: 'Nid dôme thermo-régulé en surface avec réseau de galeries sous-terraines',
        nestType: 'DOME_AND_SUBTERRANEAN',
        depthCm: 25,
        chambers: { queen: 1, nursery: 3, food: 4, waste: 2 },
        tunnelDiameterMm: 6.0
    },
    {
        id: 'nest_messor_granary',
        name: 'Greniers Sub-Superficielles Messor',
        description: 'Chambres sèches dédiées au stockage des graines et salles de broyage',
        nestType: 'SUBTERRANEAN_GRANARY',
        depthCm: 45,
        chambers: { queen: 1, nursery: 4, food: 8, waste: 3 },
        tunnelDiameterMm: 8.5
    },
    {
        id: 'nest_arboreal_log',
        name: 'Loge de Souche Creuse Arboricole',
        description: 'Nid creusé dans le bois mort vermoulu avec micro-chambres étagées',
        nestType: 'ARBOREAL_WOOD',
        depthCm: 10,
        chambers: { queen: 1, nursery: 2, food: 2, waste: 1 },
        tunnelDiameterMm: 4.5
    }
]

export const DEFAULT_PREY_PREDATOR_PRESETS = [
    {
        id: 'prey_pred_pucerons_fourmilion',
        name: 'Écosystème Pucerons & Fourmilion',
        description: 'Troupeau de pucerons producteurs de miellat + pièges d\'entonnoirs de Fourmilion',
        prey: [
            { type: 'APHID', name: 'Pucerons du Chardon (Aphis fabae)', count: 60, honeydewRate: 0.8 },
            { type: 'SEED', name: 'Graines de Graminées', count: 150 }
        ],
        predators: [
            { type: 'ANTLION', name: 'Larves de Fourmilion (Myrmeleon)', count: 2, aggression: 0.9 }
        ]
    },
    {
        id: 'prey_pred_vespula_attack',
        name: 'Incursion Guêpe Solitaire & Araignée',
        description: 'Guêpe prédatrice en maraude (Vespula) et Araignée sauteuse',
        prey: [
            { type: 'CATERPILLAR', name: 'Chenille Lymantria', count: 5, honeydewRate: 0 }
        ],
        predators: [
            { type: 'WASP', name: 'Guêpe Solitaire (Vespula germanica)', count: 1, aggression: 1.0 },
            { type: 'SPIDER', name: 'Araignée Sauteuse (Salticidae)', count: 3, aggression: 0.75 }
        ]
    },
    {
        id: 'prey_pred_peaceful_abundance',
        name: 'Ressources Abondantes sans Prédateurs',
        description: 'Abondance de proies inoffensives et graines pour expansion rapide de la colonie',
        prey: [
            { type: 'APHID', name: 'Pucerons Verts', count: 120, honeydewRate: 1.0 },
            { type: 'SEED', name: 'Graines de Pin & Graminées', count: 300 }
        ],
        predators: []
    }
]

export const DEFAULT_WEATHER_PRESETS = [
    {
        id: 'weather_printemps_doux',
        name: 'Printemps Doux (22°C, 14h Jour, Pluie Fine)',
        description: 'Conditions équilibrées idéales pour l\'activité printanière',
        season: 'SPRING',
        photoperiodHours: 14,
        tempDay: 22,
        tempNight: 14,
        precipitationMm: 8,
        humidity: 65
    },
    {
        id: 'weather_ete_caniculaire',
        name: 'Été Caniculaire & Orages (34°C, Pluie Torrentielle)',
        description: 'Fortes chaleurs diurnes déclenchant de violents orages et éclairs',
        season: 'SUMMER',
        photoperiodHours: 16,
        tempDay: 34,
        tempNight: 24,
        precipitationMm: 35,
        humidity: 80
    },
    {
        id: 'weather_automne_frais',
        name: 'Automne Humide & Venté (14°C, Brouillard)',
        description: 'Refroidissement progressif, vents modérés et brouillard matinal',
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
        id: 'scenario_mon_terrarium_1',
        name: 'Mon Terrarium N°1 (Complet)',
        description: 'Scénario complet combinant le Terrarium Tempéré, Formica fusca, le Dôme de Brindilles, les Pucerons/Fourmilion et le Printemps Doux.',
        academicCategory: 'Optimal Foraging & Ecosystem Balance',
        masterSeed: 12345,
        worldPresetId: 'world_terrarium_01',
        speciesPresetId: 'species_formica_fusca',
        nestPresetId: 'nest_dome_brindilles',
        preyPredatorPresetId: 'prey_pred_pucerons_fourmilion',
        weatherPresetId: 'weather_printemps_doux'
    },
    {
        id: 'scenario_savane_messor',
        name: 'Savane Granivore - Messor barbarus',
        description: 'Comportement d\'excavation et récolte de graines en environnement méditerranéen aride.',
        academicCategory: 'Division du Travail & Greniers',
        masterSeed: 424242,
        worldPresetId: 'world_desert_canyon',
        speciesPresetId: 'species_messor_barbarus',
        nestPresetId: 'nest_messor_granary',
        preyPredatorPresetId: 'prey_pred_peaceful_abundance',
        weatherPresetId: 'weather_ete_caniculaire'
    },
    {
        id: 'scenario_guerre_invasives',
        name: 'Conflit Territorial & Prédateurs - Linepithema',
        description: 'Raid prédateur et expansion d\'une supercolonie d\'Argentine sous climat tropical.',
        academicCategory: 'Écologie des Populations & Compétition',
        masterSeed: 777777,
        worldPresetId: 'world_tropical_rainforest',
        speciesPresetId: 'species_linepithema_humile',
        nestPresetId: 'nest_arboreal_log',
        preyPredatorPresetId: 'prey_pred_vespula_attack',
        weatherPresetId: 'weather_automne_frais'
    }
]

// Helper to safely load presets from LocalStorage or fall back to defaults
const loadLocal = (key, fallback) => {
    try {
        const saved = localStorage.getItem(`swarmforge_preset_${key}`)
        return saved ? JSON.parse(saved) : fallback
    } catch (e) {
        return fallback
    }
}

const saveLocal = (key, data) => {
    try {
        localStorage.setItem(`swarmforge_preset_${key}`, JSON.stringify(data))
    } catch (e) {
        console.error('Failed to save preset to LocalStorage', e)
    }
}

// Auto-Load Last Session if present
const lastSession = loadLocal('active_session', {
    masterSeed: 12345,
    selectedWorldId: 'world_terrarium_01',
    selectedSpeciesId: 'species_formica_fusca',
    selectedNestId: 'nest_dome_brindilles',
    selectedPreyPredatorId: 'prey_pred_pucerons_fourmilion',
    selectedWeatherId: 'weather_printemps_doux',
    selectedScenarioMetaId: 'scenario_mon_terrarium_1'
})

export const usePresetStore = create((set, get) => ({
    // Presets catalog
    worldPresets: loadLocal('world', DEFAULT_WORLD_PRESETS),
    speciesPresets: loadLocal('species', DEFAULT_SPECIES_PRESETS),
    nestPresets: loadLocal('nest', DEFAULT_NEST_PRESETS),
    preyPredatorPresets: loadLocal('prey_predator', DEFAULT_PREY_PREDATOR_PRESETS),
    weatherPresets: loadLocal('weather', DEFAULT_WEATHER_PRESETS),
    scenarioMetaPresets: loadLocal('scenario_meta', DEFAULT_SCENARIO_META_PRESETS),

    // Master Seed for deterministic replay integrity
    masterSeed: lastSession.masterSeed || 12345,

    // Currently selected preset IDs (Auto-restored from last session)
    selectedWorldId: lastSession.selectedWorldId || 'world_terrarium_01',
    selectedSpeciesId: lastSession.selectedSpeciesId || 'species_formica_fusca',
    selectedNestId: lastSession.selectedNestId || 'nest_dome_brindilles',
    selectedPreyPredatorId: lastSession.selectedPreyPredatorId || 'prey_pred_pucerons_fourmilion',
    selectedWeatherId: lastSession.selectedWeatherId || 'weather_printemps_doux',
    selectedScenarioMetaId: lastSession.selectedScenarioMetaId || 'scenario_mon_terrarium_1',

    // Pending changes flag (true when user changed dropdowns/seed but hasn't clicked Apply yet)
    hasPendingChanges: false,

    // Seed actions
    setMasterSeed: (seed) => set({ masterSeed: Number(seed), hasPendingChanges: true }),
    randomizeMasterSeed: () => set({ masterSeed: Math.floor(Math.random() * 900000) + 100000, hasPendingChanges: true }),

    // Selectors
    setSelectedWorld: (id) => set({ selectedWorldId: id, hasPendingChanges: true }),
    setSelectedSpecies: (id) => set({ selectedSpeciesId: id, hasPendingChanges: true }),
    setSelectedNest: (id) => set({ selectedNestId: id, hasPendingChanges: true }),
    setSelectedPreyPredator: (id) => set({ selectedPreyPredatorId: id, hasPendingChanges: true }),
    setSelectedWeather: (id) => set({ selectedWeatherId: id, hasPendingChanges: true }),

    // Select Scenario Meta-Preset (Updates all 5 individual preset dropdowns & master seed at once)
    selectScenarioMeta: (scenarioId) => {
        const scenario = get().scenarioMetaPresets.find(s => s.id === scenarioId)
        if (scenario) {
            set({
                selectedScenarioMetaId: scenarioId,
                masterSeed: scenario.masterSeed || get().masterSeed,
                selectedWorldId: scenario.worldPresetId || get().selectedWorldId,
                selectedSpeciesId: scenario.speciesPresetId || get().selectedSpeciesId,
                selectedNestId: scenario.nestPresetId || get().selectedNestId,
                selectedPreyPredatorId: scenario.preyPredatorPresetId || get().selectedPreyPredatorId,
                selectedWeatherId: scenario.weatherPresetId || get().selectedWeatherId,
                hasPendingChanges: true
            })
        }
    },

    // APPLY ACTION: Applies current preset selections + master seed to active simulation
    applyPresetsToSimulation: () => {
        const state = get()
        const sessionData = {
            timestamp: new Date().toISOString(),
            masterSeed: state.masterSeed,
            selectedScenarioMetaId: state.selectedScenarioMetaId,
            selectedWorldId: state.selectedWorldId,
            selectedSpeciesId: state.selectedSpeciesId,
            selectedNestId: state.selectedNestId,
            selectedPreyPredatorId: state.selectedPreyPredatorId,
            selectedWeatherId: state.selectedWeatherId,
        }
        saveLocal('active_session', sessionData)
        set({ hasPendingChanges: false })
        return sessionData
    },

    // Save a new Scenario Meta-Preset
    saveScenarioMeta: (name, description) => {
        const state = get()
        const newScenario = {
            id: `scenario_custom_${Date.now()}`,
            name,
            description: description || 'Scénario personnalisé créé par l\'utilisateur',
            academicCategory: 'Custom User Scenario',
            masterSeed: state.masterSeed,
            worldPresetId: state.selectedWorldId,
            speciesPresetId: state.selectedSpeciesId,
            nestPresetId: state.selectedNestId,
            preyPredatorPresetId: state.selectedPreyPredatorId,
            weatherPresetId: state.selectedWeatherId,
        }
        const updated = [...state.scenarioMetaPresets, newScenario]
        set({ scenarioMetaPresets: updated, selectedScenarioMetaId: newScenario.id, hasPendingChanges: false })
        saveLocal('scenario_meta', updated)
        return newScenario
    },

    // Save or Add domain preset
    addDomainPreset: (category, preset) => {
        const state = get()
        const keyMap = {
            world: 'worldPresets',
            species: 'speciesPresets',
            nest: 'nestPresets',
            prey_predator: 'preyPredatorPresets',
            weather: 'weatherPresets',
        }
        const storeKey = keyMap[category]
        if (!storeKey) return
        const updated = [...state[storeKey], preset]
        set({ [storeKey]: updated })
        saveLocal(category, updated)
    },

    // Import external JSON presets file
    importPresetsJSON: (jsonString) => {
        try {
            const data = JSON.parse(jsonString)
            if (data.scenarioMetaPresets) {
                set({ scenarioMetaPresets: data.scenarioMetaPresets })
                saveLocal('scenario_meta', data.scenarioMetaPresets)
            }
            if (data.worldPresets) {
                set({ worldPresets: data.worldPresets })
                saveLocal('world', data.worldPresets)
            }
            if (data.speciesPresets) {
                set({ speciesPresets: data.speciesPresets })
                saveLocal('species', data.speciesPresets)
            }
            if (data.nestPresets) {
                set({ nestPresets: data.nestPresets })
                saveLocal('nest', data.nestPresets)
            }
            if (data.preyPredatorPresets) {
                set({ preyPredatorPresets: data.preyPredatorPresets })
                saveLocal('prey_predator', data.preyPredatorPresets)
            }
            if (data.weatherPresets) {
                set({ weatherPresets: data.weatherPresets })
                saveLocal('weather', data.weatherPresets)
            }
            set({ hasPendingChanges: true })
            return true
        } catch (e) {
            console.error('Import failed:', e)
            return false
        }
    },

    // Export all current presets as JSON
    exportPresetsJSON: () => {
        const state = get()
        const exportData = {
            exportedAt: new Date().toISOString(),
            masterSeed: state.masterSeed,
            selectedScenarioMetaId: state.selectedScenarioMetaId,
            scenarioMetaPresets: state.scenarioMetaPresets,
            worldPresets: state.worldPresets,
            speciesPresets: state.speciesPresets,
            nestPresets: state.nestPresets,
            preyPredatorPresets: state.preyPredatorPresets,
            weatherPresets: state.weatherPresets,
        }
        return JSON.stringify(exportData, null, 2)
    }
}))
