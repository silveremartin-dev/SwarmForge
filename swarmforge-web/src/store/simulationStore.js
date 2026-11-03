import { create } from 'zustand'
import { soundEngine } from '../utils/soundEngine'
import { showToast } from './toastStore'
import { networkClient } from '../services/networkClient'
import { bindNetworkToStore } from './simulationNetworkBindings'
import { createTransportActions } from './simulationTransportActions'
import { createAudioAndLobbyActions } from './simulationAudioAndLobbyActions'
import {
    executeInstantInterventionHelper,
    selectNextAntHelper,
    selectPreviousAntHelper,
    createCheckpointHelper,
    restoreCheckpointHelper,
    fetchRealWeatherHelper
} from './simulationStoreHelpers'

import {
    DEFAULT_WORLD_PRESETS,
    DEFAULT_SPECIES_PRESETS,
    DEFAULT_NEST_PRESETS,
    DEFAULT_PREY_PREDATOR_PRESETS,
    DEFAULT_WEATHER_PRESETS,
    DEFAULT_SCENARIO_META_PRESETS
} from './presetStore'

const loadLocalStorage = (key, fallback) => {
    try {
        const saved = localStorage.getItem(key)
        return saved ? JSON.parse(saved) : fallback
    } catch {
        return fallback
    }
}

const saveLocalStorage = (key, value) => {
    try {
        localStorage.setItem(key, JSON.stringify(value))
    } catch (e) {
        console.error('Failed to save to localStorage:', key, e)
    }
}

import { formatSimCalendarTime, formatSimRelativeTime } from '../utils/timeUtils'
import {
    downloadScenarioJson,
    parseScenarioJson,
    downloadStatsCsv,
    downloadLogsCsv,
    downloadLogsJson
} from '../utils/exportUtils'

export { formatSimCalendarTime, formatSimRelativeTime }

export const useSimulationStore = create((set, get) => {
    let simLoopInterval = null
    let lastTickTime = performance.now()
    let tickCountInWindow = 0
    let lastTpsMeasureTime = performance.now()

    const initialLanguage = loadLocalStorage('swarmforge_lang', 'fr')
    const initialTheme = loadLocalStorage('swarmforge_theme', 'dark')

    return {
        // --- 1. App Mode & Top-Level Tab State (1:1 JavaFX Tabs) ---
        activeTab: 'SIMULATION', // 'SIMULATION' | 'VISUAL_3D' | 'GOD_MODE' | 'STATISTICS' | 'EVENT_LOG' | 'SETTINGS'
        activeMainTab: 'SIMULATION', // backwards compatibility
        activeSubTab: 'CONTROLS',    // backwards compatibility
        language: initialLanguage,
        theme: initialTheme,
        engineBackend: loadLocalStorage('swarmforge_engine_backend', 'auto'),
        computeAcceleration: loadLocalStorage('swarmforge_compute_accel', 'auto'),
        setEngineBackend: (backend) => {
            saveLocalStorage('swarmforge_engine_backend', backend)
            set({ engineBackend: backend })
        },
        setComputeAcceleration: (accel) => {
            saveLocalStorage('swarmforge_compute_accel', accel)
            set({ computeAcceleration: accel })
        },
        isScenarioApplied: false, // Whether scenario has been initialized (locks downstream tabs when false)

        slicePlaneRatio: 1.0,
        setSlicePlaneRatio: (ratio) => set({ slicePlaneRatio: typeof ratio === 'number' && !isNaN(ratio) ? ratio : 1.0 }),

        setActiveTab: (tab) => {
            const currentRunning = get().running
            // Auto-pause ONLY when entering God Mode (1:1 with SwarmForgeClient.java line 1052)
            if (tab === 'GOD_MODE' && currentRunning) {
                get().pause()
                get().addEventLog({
                    severity: 'WARNING',
                    type: 'SYSTEM',
                    source: 'Mode Divin',
                    message: '⏸️ Simulation mise en pause automatique pour agencement des interventions.'
                })
            }
            // Continuous audio and background simulation across all other tabs
            soundEngine.updateSimulationState(get().running, get().speed)
            set({
                activeTab: tab,
                activeMainTab: tab === 'SETTINGS' ? 'SETTINGS' : 'SIMULATION',
                activeSubTab: tab === 'SIMULATION' ? 'CONTROLS' : tab
            })
        },
        setActiveMainTab: (tab) => get().setActiveTab(tab),
        setActiveSubTab: (tab) => get().setActiveTab(tab === 'CONTROLS' ? 'SIMULATION' : tab),
        setLanguage: (lang) => {
            saveLocalStorage('swarmforge_lang', lang)
            set({ language: lang })
        },
        setTheme: (theme) => {
            saveLocalStorage('swarmforge_theme', theme)
            set({ theme: theme })
        },

        // --- 2. Simulation Transport & Clocks ---
        ticks: 0,
        highestRecordedTick: 0,
        simTimeSeconds: 0,
        startDateTime: '2026-03-20T08:00:00',
        stepSeconds: 0.05, // dt = 50ms (20 ticks/sec standard)
        speed: 1.0,
        running: false,
        isPaused: false,
        measuredTps: 0,
        targetTps: 20,
        simTimeFormatted: '2026-03-20 08:00:00',
        simRelativeTimeFormatted: 'J+0 08:00:00',

        // --- Checkpoints ---
        checkpoints: [],
        createCheckpoint: (name) => createCheckpointHelper(get, set, name),
        restoreCheckpoint: (checkpointId) => restoreCheckpointHelper(get, set, checkpointId),

        // --- 3. Scenario Configuration (1:1 with SimulationControlPanel.java) ---
        selectedScenarioPresetId: 'ACAD_01_LEVY_BROWNIAN',
        selectedWorldPresetId: 'world_terrarium_01',
        selectedWeatherPresetId: 'weather_printemps_doux',
        masterSeed: 12345,
        scenarioDescription: 'Scénario standard de simulation multi-espèces et équilibre trophique.',
        maxDuration: 100.0,
        durationUnit: 'Days',
        minPopStop: 0,
        isMultiplayerOnly: false,
        requiredPlayerCount: 1,
        gridTilesX: 1,
        gridTilesY: 1,
        speciesCards: [],

        setScenarioPresetId: (presetId) => {
            const meta = DEFAULT_SCENARIO_META_PRESETS.find(p => p.id === presetId)
            if (meta) {
                set({
                    selectedScenarioPresetId: presetId,
                    selectedWorldPresetId: meta.worldPresetId || 'world_terrarium_01',
                    selectedWeatherPresetId: meta.weatherPresetId || 'weather_printemps_doux',
                    masterSeed: meta.masterSeed || 12345,
                    scenarioDescription: meta.description || '',
                    isMultiplayerOnly: Boolean(meta.isMultiplayerOnly),
                    requiredPlayerCount: meta.requiredPlayerCount || 1,
                    gridTilesX: meta.gridTilesX || 1,
                    gridTilesY: meta.gridTilesY || 1
                })
            } else {
                set({ selectedScenarioPresetId: presetId })
            }
        },

        setWorldPresetId: (id) => set({ selectedWorldPresetId: id }),
        setWeatherPresetId: (id) => set({ selectedWeatherPresetId: id }),
        setMasterSeed: (seed) => set({ masterSeed: Number(seed) || 12345 }),
        setScenarioDescription: (desc) => set({ scenarioDescription: desc }),
        setStartDateTime: (dt) => {
            set({
                startDateTime: dt,
                simTimeFormatted: formatSimCalendarTime(dt, get().simTimeSeconds)
            })
        },
        setMaxDuration: (dur) => set({ maxDuration: Number(dur) || 100 }),
        setDurationUnit: (unit) => set({ durationUnit: unit }),
        setMinPopStop: (pop) => set({ minPopStop: Number(pop) || 0 }),
        setIsMultiplayerOnly: (val) => set({ isMultiplayerOnly: Boolean(val) }),
        setRequiredPlayerCount: (cnt) => set({ requiredPlayerCount: Math.max(1, Math.min(16, Number(cnt) || 1)) }),
        setGridTilesX: (x) => set({ gridTilesX: Math.max(1, Math.min(8, Number(x) || 1)) }),
        setGridTilesY: (y) => set({ gridTilesY: Math.max(1, Math.min(8, Number(y) || 1)) }),

        addSpeciesCard: (card) => {
            const current = get().speciesCards
            const newCard = card || {
                id: `card_col_${Date.now()}`,
                name: `Colonie #${current.length + 1}`,
                speciesId: DEFAULT_SPECIES_PRESETS[0].id,
                nestType: DEFAULT_NEST_PRESETS[0].nestType,
                color: ['#10b981', '#a855f7', '#f59e0b', '#ec4899', '#06b6d4'][current.length % 5],
                initialQueens: 1,
                initialWorkers: 40,
                initialSoldiers: 10,
                initialMales: 0,
                aiArchitecture: 'BEHAVIOR_TREE',
                preyPredatorPresetId: DEFAULT_PREY_PREDATOR_PRESETS[0].id
            }
            set({ speciesCards: [...current, newCard] })
        },

        removeSpeciesCard: (cardId) => {
            set({ speciesCards: get().speciesCards.filter(c => c.id !== cardId) })
        },

        updateSpeciesCard: (cardId, updates) => {
            set({
                speciesCards: get().speciesCards.map(c => c.id === cardId ? { ...c, ...updates } : c)
            })
        },

        // --- 4. Simulation Entities & Environment ---
        colonies: [],
        ants: [],
        pheromones: [],
        nests: [],
        phantomNestsVisible: false,
        ghostNest: null,
        foodSources: [],
        predators: [],
        hoveredVoxel: null,
        setHoveredVoxel: (v) => set({ hoveredVoxel: v }),
        selectedEntity: null,
        setSelectedEntity: (ant) => set({ selectedEntity: ant, trackedAntId: ant ? ant.id : null, trackedAntData: ant }),
        selectedChamber: null,
        setSelectedChamber: (ch) => set({ selectedChamber: ch }),
        trackedAntId: null,
        trackedAntData: null,
        followAntCamera: false,
        cameraFollowMode: null, // null | 'TPS' | 'FPS'
        customCameraTarget: null, // [x, y, z]
        setFollowAntCamera: (f) => set({ followAntCamera: f, cameraFollowMode: f ? 'TPS' : null }),
        setCameraFollowMode: (mode) => set({ cameraFollowMode: mode, followAntCamera: Boolean(mode) }),
        setCustomCameraTarget: (pos) => set({ customCameraTarget: pos }),
        setTrackedAntId: (id) => {
            const ant = (get().ants || []).find(a => a.id === id || a.id?.includes(id)) || null
            set({ trackedAntId: ant ? ant.id : id, trackedAntData: ant, selectedEntity: ant })
            return ant
        },
        antNavigationHistory: [],
        selectNextAnt: () => selectNextAntHelper(get, set),
        selectPreviousAnt: () => selectPreviousAntHelper(get, set),
        showTerrain: true,
        toggleTerrain: () => set(s => ({ showTerrain: !s.showTerrain })),
        show3DSkirt: true,
        toggle3DSkirt: () => set(s => ({ show3DSkirt: !s.show3DSkirt })),
        showVegetation: true,
        toggleVegetation: () => set(s => ({ showVegetation: !s.showVegetation })),
        showChambers: true,
        toggleChambers: () => set(s => ({ showChambers: !s.showChambers })),
        showPheromones: false,
        togglePheromones: () => set(s => ({ showPheromones: !s.showPheromones })),
        showAnts: true,
        toggleAnts: () => set(s => ({ showAnts: !s.showAnts })),
        showWeather: true,
        toggleWeather: () => set(s => ({ showWeather: !s.showWeather })),
        showMinimap: true,
        toggleMinimap: () => set(s => ({ showMinimap: !s.showMinimap })),
        showGrid: false,
        toggleGrid: () => set(s => ({ showGrid: !s.showGrid })),
        showScientificIsolinesTopo: false,
        toggleScientificIsolinesTopo: () => set(s => ({ showScientificIsolinesTopo: !s.showScientificIsolinesTopo })),
        showScientificIsolinesMicroclimate: false,
        toggleScientificIsolinesMicroclimate: () => set(s => ({ showScientificIsolinesMicroclimate: !s.showScientificIsolinesMicroclimate })),
        showScientificIsolinesPheromones: false,
        toggleScientificIsolinesPheromones: () => set(s => ({ showScientificIsolinesPheromones: !s.showScientificIsolinesPheromones })),
        isUVVisionMode: false,
        toggleUVVisionMode: () => set(s => ({ isUVVisionMode: !s.isUVVisionMode })),
        lookAndFeel: 'REALISTIC',
        terrainConfig: { type: 'TERRARIUM', roughness: 0.3, waterLevel: 0.1 },
        environment: {
            timeOfDay: 'DAY',
            lightLevel: 1.0,
            temperature: 22.0,
            humidity: 65.0,
            windSpeed: 1.5,
            weatherState: 'CLEAR',
            season: 'SPRING',
            solarRadiation: 450.0
        },


        // --- 5. God Mode Interventions & Scheduled Events Queue ---
        scheduledEvents: [],

        addScheduledEvent: (event) => {
            const current = get().scheduledEvents
            const newEvt = {
                id: `evt_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
                executed: false,
                paused: false,
                ...event
            }
            if (get().connected) {
                networkClient.sendScheduledEvent(newEvt)
            }
            const updated = [...current, newEvt].sort((a, b) => a.targetTick - b.targetTick)
            set({ scheduledEvents: updated })
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'God Mode Scheduler',
                message: `Événement planifié au tick ${newEvt.targetTick} : [${newEvt.category}] ${newEvt.eventType}`
            })
        },

        removeScheduledEvent: (eventId) => {
            set({ scheduledEvents: get().scheduledEvents.filter(e => e.id !== eventId) })
        },

        togglePauseScheduledEvent: (eventId) => {
            set({
                scheduledEvents: get().scheduledEvents.map(e => e.id === eventId ? { ...e, paused: !e.paused } : e)
            })
        },

        duplicateScheduledEvent: (eventId, offsetTicks = 100) => {
            const target = get().scheduledEvents.find(e => e.id === eventId)
            if (!target) return
            const newTargetTick = (target.targetTick || get().ticks) + offsetTicks
            const stepSec = get().stepSeconds
            const calTime = formatSimCalendarTime(get().startDateTime, newTargetTick * stepSec)
            get().addScheduledEvent({
                ...target,
                id: `evt_${Date.now()}`,
                targetTick: newTargetTick,
                scheduledCalendarTime: calTime,
                description: `${target.description} (Copie +${offsetTicks} ticks)`,
                executed: false,
                paused: false
            })
        },

        executeInstantIntervention: (intervention) => executeInstantInterventionHelper(get, set, networkClient, intervention),

        // --- 6. Statistics History Buffers (7 Dynamic Real Charts) ---
        timeWindow: '3m', // '1m' | '3m' | '10m' | '30m' | '1h' | 'all'
        statsHistory: [],
        trackedAntId: null,
        trackedAntData: null,
        followAntCamera: false,

        setTimeWindow: (w) => set({ timeWindow: w }),
        setFollowAntCamera: (enabled) => set({ followAntCamera: enabled }),
        setTrackedAntId: (id) => {
            const ant = get().ants.find(a => a.id === id)
            set({
                trackedAntId: id,
                trackedAntData: ant ? {
                    ...ant,
                    healthHistory: [ant.health],
                    energyHistory: [ant.energy],
                    distanceTraveled: 0
                } : null
            })
        },

        // --- 7. Event Log Bus (1:1 with SimulationEvent.java & EventLogPane.java) ---
        eventSequenceCounter: 1,
        eventsLog: [
            {
                id: 'evt_log_init',
                sequenceId: 1,
                tick: 0,
                simCalendarTime: '2026-03-20 08:00:00',
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'SwarmForge Engine',
                sourceKey: 'sourceEngine',
                message: 'Simulation initialisée avec succès en mode autonome haute performance.',
                messageKey: 'evt_SIMULATION_INITIALIZED',
                metadata: { seed: 12345, world: 'world_terrarium_01' }
            }
        ],

        addEventLog: (log) => {
            const current = get().eventsLog
            const nextSeq = (get().eventSequenceCounter || current.length) + 1
            const newLog = {
                id: `log_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
                sequenceId: log.sequenceId || nextSeq,
                tick: log.tick !== undefined ? log.tick : get().ticks,
                simCalendarTime: log.simCalendarTime || get().simTimeFormatted,
                severity: log.severity || 'INFO',
                type: log.type || 'SYSTEM',
                source: log.source || 'Simulation Engine',
                sourceKey: log.sourceKey,
                message: log.message || '',
                messageKey: log.messageKey,
                messageParams: log.messageParams,
                metadata: log.metadata || {}
            }
            set({
                eventSequenceCounter: nextSeq,
                eventsLog: [newLog, ...current].slice(0, 1000)
            })
        },

        clearEventLogs: () => set({ eventsLog: [] }),

        ...createAudioAndLobbyActions(set, get, networkClient, soundEngine),

        ...createTransportActions(set, get, networkClient, soundEngine),



        // --- 10. Real Weather Live API (Open-Meteo) ---
        isRealWeatherMode: false,
        realWeatherCity: 'Paris',
        realWeatherStatus: 'Cliquez pour charger Open-Meteo',
        setRealWeatherMode: (val) => set({ isRealWeatherMode: val }),
        setRealWeatherCity: (city) => set({ realWeatherCity: city }),
        fetchRealWeather: (queryCity) => fetchRealWeatherHelper(get, set, queryCity),

        // --- 11. Scenario Import / Export ---
        exportScenarioJson: () => {
            const s = get()
            downloadScenarioJson({
                title: s.selectedScenarioPresetId,
                description: s.scenarioDescription,
                worldPresetId: s.selectedWorldPresetId,
                weatherPresetId: s.selectedWeatherPresetId,
                masterSeed: s.masterSeed,
                startDateTime: s.startDateTime,
                maxDuration: s.maxDuration,
                durationUnit: s.durationUnit,
                minPopStop: s.minPopStop,
                isMultiplayerOnly: s.isMultiplayerOnly,
                requiredPlayerCount: s.requiredPlayerCount,
                gridTilesX: s.gridTilesX,
                gridTilesY: s.gridTilesY,
                speciesCards: s.speciesCards
            })
        },

        importScenarioJson: (jsonString) => {
            const parsed = parseScenarioJson(jsonString)
            if (!parsed) return false
            set(parsed)
            get().applyScenarioSetup()
            return true
        },

        // --- 12. Metrics & Event Log CSV/JSON Exports ---
        exportStatsCsv: () => downloadStatsCsv(get().statsHistory),
        exportLogsCsv: () => downloadLogsCsv(get().eventLogs),
        exportLogsJson: () => downloadLogsJson(get().eventLogs),

        // --- 13. Viewport & Rendering Options ---
        sliceY: 0.0,
        showPheromones: true,
        showMinimap: true,
        showGrid: true,
        showChambers: true,
        masterVolume: 0.8,
        ambientVolume: 0.6,
        sfxVolume: 0.7,

        setSliceY: (val) => set({ sliceY: Number(val) }),
        setShowPheromones: (v) => set({ showPheromones: v }),
        setShowMinimap: (v) => set({ showMinimap: v }),
        setShowGrid: (v) => set({ showGrid: v }),
        setShowChambers: (v) => set({ showChambers: v }),
        setMasterVolume: (v) => {
            soundEngine.setMasterVolume(v)
            set({ masterVolume: v })
        },
        setAmbientVolume: (v) => {
            soundEngine.setAmbientVolume(v)
            set({ ambientVolume: v })
        },
        setSfxVolume: (v) => set({ sfxVolume: v })
    }
})

bindNetworkToStore(useSimulationStore, networkClient)
