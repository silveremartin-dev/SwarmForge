import { create } from 'zustand'

export * from '../data/defaultPresetsData'
import {
    DEFAULT_WORLD_PRESETS,
    DEFAULT_SPECIES_PRESETS,
    DEFAULT_NEST_PRESETS,
    DEFAULT_PREY_PREDATOR_PRESETS,
    DEFAULT_WEATHER_PRESETS,
    DEFAULT_SCENARIO_META_PRESETS
} from '../data/defaultPresetsData'

// Helper to safely load presets from LocalStorage or fall back to defaults

const loadLocal = (key, fallback) => {
    try {
        const saved = localStorage.getItem(`swarmforge_preset_${key}`)
        if (!saved) return fallback
        const parsed = JSON.parse(saved)
        // If cached array contains obsolete French names, discard cache and use English defaults
        const isObsoleteFrench = Array.isArray(parsed) && parsed.some(item => 
            item.name && (
                item.name.includes('Mon Terrarium') || 
                item.name.includes('Fourmi') || 
                item.name.includes('Pucerons') ||
                item.name.includes('Dôme') ||
                item.name.includes('Printemps') ||
                item.name.includes('Forêt') ||
                item.name.includes('Désert') ||
                item.name.includes('Écosystème') ||
                item.name.includes('Greniers') ||
                item.name.includes('Incursion')
            )
        )
        if (isObsoleteFrench) return fallback
        return parsed
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
