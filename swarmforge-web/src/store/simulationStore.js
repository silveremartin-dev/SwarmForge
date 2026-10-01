import { create } from 'zustand'
import { soundEngine } from '../utils/soundEngine'
import { showToast } from './toastStore'
import { networkClient } from '../services/networkClient'
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

export function formatSimCalendarTime(startDateTimeIso, simTimeSeconds) {
    try {
        const startDate = new Date(startDateTimeIso || '2026-03-20T08:00:00')
        const currentMs = startDate.getTime() + (simTimeSeconds || 0) * 1000
        const curDate = new Date(currentMs)
        const y = curDate.getFullYear()
        const m = String(curDate.getMonth() + 1).padStart(2, '0')
        const d = String(curDate.getDate()).padStart(2, '0')
        const hh = String(curDate.getHours()).padStart(2, '0')
        const mm = String(curDate.getMinutes()).padStart(2, '0')
        const ss = String(curDate.getSeconds()).padStart(2, '0')
        return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
    } catch {
        return '2026-03-20 08:00:00'
    }
}

export function formatSimRelativeTime(totalSeconds) {
    const totalSec = Math.floor(Math.max(0, totalSeconds || 0))
    const days = Math.floor(totalSec / 86400)
    const rem = totalSec % 86400
    const hours = Math.floor(rem / 3600)
    const mins = Math.floor((rem % 3600) / 60)
    const secs = Math.floor(rem % 60)
    return `J+${days} ${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}



function generateInitialColonies(speciesCards) {
    if (!speciesCards || speciesCards.length === 0) return []
    return speciesCards.map(c => ({
        id: c.id,
        name: c.name,
        speciesId: c.speciesId,
        color: c.color,
        food: 0,
        water: 0,
        protein: 0,
        population: (c.initialQueens || 0) + (c.initialWorkers || 0) + (c.initialSoldiers || 0) + (c.initialMales || 0),
        queens: c.initialQueens || 0,
        workers: c.initialWorkers || 0,
        soldiers: c.initialSoldiers || 0,
        males: c.initialMales || 0
    }))
}

function generateInitialFoodSources() {
    return []
}

function generateInitialNests(speciesCards) {
    if (!speciesCards || speciesCards.length === 0) return []
    return speciesCards.map((c, idx) => ({
        id: `nest_${c.id}`,
        colonyId: c.id,
        name: `Nid de ${c.name}`,
        x: idx === 0 ? 35 : (35 + idx * 30),
        y: idx === 0 ? 35 : (35 + idx * 30),
        z: 0,
        nestType: c.nestType || 'DOME_AND_SUBTERRANEAN',
        scale: 1.0,
        color: c.color,
        chambers: [],
        tunnels: []
    }))
}

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
        createCheckpoint: (name) => {
            const state = get()
            const cp = {
                id: `cp_${Date.now()}`,
                name: name || `Checkpoint @ Tick ${state.ticks}`,
                tick: state.ticks,
                simTimeSeconds: state.simTimeSeconds,
                simCalendarTime: state.simTimeFormatted,
                snapshot: {
                    ants: JSON.parse(JSON.stringify(state.ants)),
                    colonies: JSON.parse(JSON.stringify(state.colonies)),
                    foodSources: JSON.parse(JSON.stringify(state.foodSources)),
                    environment: { ...state.environment }
                }
            }
            set({ checkpoints: [cp, ...state.checkpoints] })
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Points de Contrôle',
                message: `Point de contrôle "${cp.name}" créé au tick ${cp.tick}.`
            })
            return cp
        },

        restoreCheckpoint: (checkpointId) => {
            const cp = get().checkpoints.find(c => c.id === checkpointId)
            if (!cp) return false
            get().pause()
            set({
                ticks: cp.tick,
                simTimeSeconds: cp.simTimeSeconds,
                simTimeFormatted: cp.simCalendarTime,
                ants: JSON.parse(JSON.stringify(cp.snapshot.ants)),
                colonies: JSON.parse(JSON.stringify(cp.snapshot.colonies)),
                foodSources: JSON.parse(JSON.stringify(cp.snapshot.foodSources)),
                environment: { ...cp.snapshot.environment }
            })
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Points de Contrôle',
                message: `Restauration du point de contrôle "${cp.name}" au tick ${cp.tick}.`
            })
            return true
        },

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
        selectNextAnt: () => {
            const state = get()
            const ants = (state.ants || []).filter(a => a && (a.health === undefined || a.health > 0))
            if (ants.length === 0) return
            const currentId = state.trackedAntId || state.selectedEntity?.id
            const currentAnt = ants.find(a => a.id === currentId)
            if (!currentAnt) {
                const first = ants[0]
                set({ trackedAntId: first.id, trackedAntData: first, selectedEntity: first })
                return
            }

            const curX = currentAnt.x ?? 50
            const curY = currentAnt.y ?? 0.15
            const curZ = currentAnt.z !== undefined ? currentAnt.z : (currentAnt.y ?? 50)

            // Push current ant into navigation history stack (capped at 50)
            const history = [...(state.antNavigationHistory || [])]
            if (currentId && (history.length === 0 || history[history.length - 1] !== currentId)) {
                history.push(currentId)
                if (history.length > 50) history.shift()
            }

            // Calculate 3D Euclidean distance to all other living ants
            const candidates = ants
                .filter(a => a.id !== currentId)
                .map(a => {
                    const ax = a.x ?? 50
                    const ay = a.y ?? 0.15
                    const az = a.z !== undefined ? a.z : (a.y ?? 50)
                    const distSq = (ax - curX) * (ax - curX) + (ay - curY) * (ay - curY) + (az - curZ) * (az - curZ)
                    return { ant: a, distSq }
                })
                .sort((a, b) => a.distSq - b.distSq)

            if (candidates.length > 0) {
                const nextAnt = candidates[0].ant
                set({
                    trackedAntId: nextAnt.id,
                    trackedAntData: nextAnt,
                    selectedEntity: nextAnt,
                    antNavigationHistory: history
                })
            }
        },
        selectPreviousAnt: () => {
            const state = get()
            const ants = (state.ants || []).filter(a => a && (a.health === undefined || a.health > 0))
            if (ants.length === 0) return
            const currentId = state.trackedAntId || state.selectedEntity?.id
            const history = [...(state.antNavigationHistory || [])]

            // Try to pop previous ant from history stack
            while (history.length > 0) {
                const prevId = history.pop()
                if (prevId !== currentId) {
                    const found = ants.find(a => a.id === prevId)
                    if (found) {
                        set({
                            trackedAntId: found.id,
                            trackedAntData: found,
                            selectedEntity: found,
                            antNavigationHistory: history
                        })
                        return
                    }
                }
            }

            // If no history, select closest ant by Euclidean distance
            const currentAnt = ants.find(a => a.id === currentId)
            if (!currentAnt) {
                const last = ants[ants.length - 1]
                set({ trackedAntId: last.id, trackedAntData: last, selectedEntity: last, antNavigationHistory: [] })
                return
            }

            const curX = currentAnt.x ?? 50
            const curY = currentAnt.y ?? 0.15
            const curZ = currentAnt.z !== undefined ? currentAnt.z : (currentAnt.y ?? 50)

            const candidates = ants
                .filter(a => a.id !== currentId)
                .map(a => {
                    const ax = a.x ?? 50
                    const ay = a.y ?? 0.15
                    const az = a.z !== undefined ? a.z : (a.y ?? 50)
                    const distSq = (ax - curX) * (ax - curX) + (ay - curY) * (ay - curY) + (az - curZ) * (az - curZ)
                    return { ant: a, distSq }
                })
                .sort((a, b) => a.distSq - b.distSq)

            if (candidates.length > 0) {
                const prevAnt = candidates[0].ant
                set({
                    trackedAntId: prevAnt.id,
                    trackedAntData: prevAnt,
                    selectedEntity: prevAnt,
                    antNavigationHistory: []
                })
            }
        },
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

        executeInstantIntervention: (intervention) => {
            const state = get()
            const { category, type, posX, posY, posZ, count, amount, intensity, colonyId, caste } = intervention

            if (state.connected) {
                networkClient.sendGodModeIntervention(intervention)
            }

            get().addEventLog({
                severity: 'WARNING',
                type: category === 'DISASTER' ? 'DISASTER' : 'SYSTEM',
                source: 'God Mode (Instant)',
                message: `Intervention immédiate : [${category}] ${type || 'Action'} (Pos: ${posX ?? 32}, ${posY ?? 32})`
            })

            if (category === 'ENTITIES') {
                if (intervention.action === 'SPAWN' || type?.includes('Spawn')) {
                    const newAnts = []
                    const targetColony = state.colonies.find(c => c.id === colonyId) || state.colonies[0]
                    for (let i = 0; i < (count || 5); i++) {
                        newAnts.push({
                            id: `ant_spawned_${Date.now()}_${i}`,
                            colonyId: targetColony?.id || 'col_1',
                            colonyName: targetColony?.name || 'Colonie',
                            species: targetColony?.speciesId || 'Formica fusca',
                            caste: caste || 'WORKER',
                            job: caste === 'SOLDIER' ? 'GUARD' : (caste === 'QUEEN' ? 'LAYING_EGGS' : 'FORAGER'),
                            x: (posX || 32) + (Math.random() - 0.5) * 3,
                            y: 0.1,
                            z: (posY || 32) + (Math.random() - 0.5) * 3,
                            vx: 0, vy: 0, vz: 0,
                            health: 100,
                            energy: 100,
                            age: 1,
                            carriedItem: 'NONE',
                            task: 'Instant God Mode Injection',
                            color: targetColony?.color || '#38bdf8'
                        })
                    }
                    set({ ants: [...state.ants, ...newAnts] })
                } else if (intervention.action === 'KILL') {
                    const toKill = count || 5
                    const updatedAnts = state.ants.slice(toKill)
                    set({ ants: updatedAnts })
                }
            } else if (category === 'RESOURCE') {
                const newFood = {
                    id: `food_${Date.now()}`,
                    x: posX || 32,
                    y: 0.2,
                    z: posY || 32,
                    type: type?.includes('Sugar') ? 'SUGAR' : 'SEEDS',
                    name: type || 'Dépôt Nourriture',
                    amount: amount || 100,
                    maxAmount: amount || 100,
                    color: '#10b981'
                }
                set({ foodSources: [...state.foodSources, newFood] })
            } else if (category === 'DISASTER') {
                const upper = (type || '').toUpperCase()
                if (upper.includes('RAIN') || upper.includes('FLOOD')) {
                    set({
                        environment: {
                            ...state.environment,
                            weatherState: 'TEMPEST',
                            humidity: Math.min(100, state.environment.humidity + (intensity || 0.5) * 30),
                            temperature: Math.max(5, state.environment.temperature - 4)
                        }
                    })
                } else if (upper.includes('HEAT') || upper.includes('FIRE')) {
                    set({
                        environment: {
                            ...state.environment,
                            weatherState: 'CLEAR',
                            temperature: state.environment.temperature + (intensity || 0.5) * 15,
                            humidity: Math.max(10, state.environment.humidity - 25)
                        }
                    })
                }
            } else if (category === 'ABIOTIC') {
                set({
                    environment: {
                        ...state.environment,
                        temperature: intervention.tempCelsius ?? state.environment.temperature,
                        humidity: intervention.humidityPercent ?? state.environment.humidity,
                        windSpeed: intervention.windMetersPerSec ?? state.environment.windSpeed
                    }
                })
            } else if (category === 'PHEROMONE') {
                const pheroPoints = []
                const centerXPhero = posX || 50
                const centerZPhero = posY || 50
                const radius = intervention.pheromoneRadius || 10
                const countPhero = Math.min(30, Math.max(5, Math.floor(radius * 2)))
                for (let i = 0; i < countPhero; i++) {
                    const angle = Math.random() * Math.PI * 2
                    const dist = Math.random() * radius
                    pheroPoints.push({
                        id: `phero_god_${Date.now()}_${i}`,
                        x: Math.max(5, Math.min(95, centerXPhero + Math.cos(angle) * dist)),
                        z: Math.max(5, Math.min(95, centerZPhero + Math.sin(angle) * dist)),
                        type: intervention.pheromoneType || 'ALARM',
                        intensity: Math.min(1.0, (intervention.pheromoneIntensity || 100) / 100),
                        colonyId: colonyId || 'ALL',
                        createdAtTick: state.ticks
                    })
                }
                set({ pheromones: [...(state.pheromones || []), ...pheroPoints].slice(-1000) })
            }
        },

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

        // --- 8. Server Connection & Execution Topologies (1:1 with JavaFX) ---
        executionMode: 'REMOTE_CLIENT_SERVER', // 'REMOTE_CLIENT_SERVER' default architecture
        serverHost: 'localhost',
        serverPort: 50051,
        connected: false,
        playerAlias: 'Participant_1',
        playerSpecies: 'Black Garden Ant (Lasius niger)',
        serverRole: 'JOIN', // 'JOIN' | 'HOST'
        serverStatusText: 'Offline',

        // Server Scenario Browser & Matchmaking Lobby State
        serverScenarios: [],
        lobbyStatus: 'LOBBY_WAITING', // 'LOBBY_WAITING' | 'ACTIVE' | 'INACTIVE'
        lobbyPlayers: [],
        selectedServerScenarioId: 'ACAD_01_LEVY_BROWNIAN',
        isPlayerReady: false,

        setExecutionMode: (mode) => set({ executionMode: mode }),
        setServerHost: (host) => set({ serverHost: host }),
        setServerPort: (port) => set({ serverPort: Number(port) || 50051 }),
        setPlayerAlias: (alias) => set({ playerAlias: alias }),
        setPlayerSpecies: (sp) => set({ playerSpecies: sp }),
        setServerRole: (role) => set({ serverRole: role }),

        requestServerScenarios: () => networkClient.requestServerScenarios(),
        togglePlayerReady: () => {
            const current = get().isPlayerReady
            const next = !current
            set({ isPlayerReady: next })
            networkClient.setPlayerReady(next)
            showToast(next ? '✓ Vous êtes prêt pour la partie !' : 'Statut prêt désactivé.', 'info')
        },
        startServerMatch: () => {
            networkClient.startMatch()
            showToast('🚀 Lancement de la partie sur le serveur...', 'success')
        },
        selectServerScenario: (scenarioId) => {
            set({ selectedServerScenarioId: scenarioId })
            networkClient.selectServerScenario(scenarioId)
            showToast(`Scénario sélectionné : ${scenarioId}`, 'info')
        },

        // Audio Controls (1:1 with Desktop Client SwarmForgeClient.java / SimulationAudioManager.java)
        masterVolume: 0.7,
        ambientEnabled: true,
        riverEnabled: true,
        weatherEnabled: true,
        insectEnabled: true,

        setMasterVolume: (v) => {
            set({ masterVolume: v })
            soundEngine.setMasterVolume(v)
        },
        setAmbientEnabled: (enabled) => {
            set({ ambientEnabled: enabled })
            soundEngine.setAmbientEnabled(enabled)
        },
        toggleAmbient: () => {
            const next = !get().ambientEnabled
            set({ ambientEnabled: next })
            soundEngine.setAmbientEnabled(next)
        },
        setRiverEnabled: (enabled) => {
            set({ riverEnabled: enabled })
            soundEngine.setRiverEnabled(enabled)
        },
        toggleRiver: () => {
            const next = !get().riverEnabled
            set({ riverEnabled: next })
            soundEngine.setRiverEnabled(next)
        },
        setWeatherEnabled: (enabled) => {
            set({ weatherEnabled: enabled })
            soundEngine.setWeatherEnabled(enabled)
        },
        toggleWeatherAudio: () => {
            const next = !get().weatherEnabled
            set({ weatherEnabled: next })
            soundEngine.setWeatherEnabled(next)
        },
        setInsectEnabled: (enabled) => {
            set({ insectEnabled: enabled })
            soundEngine.setInsectEnabled(enabled)
        },
        toggleInsect: () => {
            const next = !get().insectEnabled
            set({ insectEnabled: next })
            soundEngine.setInsectEnabled(next)
        },

        connect: () => {
            const { serverHost, serverPort, playerAlias, playerSpecies, serverRole } = get()
            if (!playerAlias || !playerAlias.trim()) {
                showToast('Veuillez saisir un tag / alias de participant valide avant de vous connecter.', 'error')
                return
            }

            networkClient.connect(serverHost, serverPort, playerAlias, playerSpecies, serverRole)
        },

        disconnect: () => {
            networkClient.disconnect()
            set({
                connected: false,
                serverStatusText: 'Offline'
            })
            showToast('Déconnecté du serveur SwarmForge.', 'info')
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Réseau',
                message: 'Déconnexion du serveur distant effectuée.'
            })
        },

        discover: () => {
            showToast('🔍 Détection des serveurs SwarmForge locaux sur les ports 8081, 50051...', 'info')
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Réseau',
                message: 'Détection automatique de serveurs SwarmForge locaux sur les ports 8081, 50051...'
            })
            setTimeout(() => {
                set({ serverHost: 'localhost', serverPort: 50051 })
                showToast('✓ Serveur local SwarmForge détecté sur localhost:50051 / 8081 !', 'success')
                get().addEventLog({
                    severity: 'INFO',
                    type: 'SYSTEM',
                    source: 'Réseau',
                    message: 'Serveur local SwarmForge détecté sur localhost:50051 (gRPC) / 8081 (WebSocket).'
                })
            }, 600)
        },

        // --- 9. Core Simulation Actions & Transport Controls ---
        applyScenarioSetup: () => {
            const state = get()
            const { speciesCards, startDateTime, masterSeed, selectedWorldPresetId, selectedWeatherPresetId, connected, serverRole, playerAlias, playerSpecies, serverHost, serverPort } = state
            const initialColonies = generateInitialColonies(speciesCards)
            const initialFoods = generateInitialFoodSources()
            const initialNests = generateInitialNests(speciesCards)

            const worldPreset = DEFAULT_WORLD_PRESETS.find(w => w.id === selectedWorldPresetId)
            const weatherPreset = DEFAULT_WEATHER_PRESETS.find(w => w.id === selectedWeatherPresetId)

            // Ensure server connection is active or initiated
            if (!connected) {
                networkClient.connect(serverHost, serverPort, playerAlias, playerSpecies, serverRole)
                showToast(`Connexion au serveur SwarmForge (${serverHost}:${serverPort})...`, 'info')
            }

            // If connected to SwarmForge Server: deploy scenario or join session
            if (serverRole === 'HOST') {
                networkClient.deployScenario({
                    selectedWorldPresetId,
                    selectedWeatherPresetId,
                    startDateTime,
                    masterSeed,
                    stepSeconds: state.stepSeconds,
                    maxDurationSeconds: state.maxDuration,
                    minPopulationStop: state.minPopStop,
                    isMultiplayerOnly: state.isMultiplayerOnly,
                    requiredPlayerCount: state.requiredPlayerCount,
                    gridTilesX: state.gridTilesX,
                    gridTilesY: state.gridTilesY,
                    speciesCards,
                    colonies: initialColonies,
                    nests: initialNests
                })
            } else {
                networkClient.joinSession(playerAlias, playerSpecies)
            }

            set({
                ticks: 0,
                highestRecordedTick: 0,
                simTimeSeconds: 0,
                running: false,
                isPaused: false,
                isScenarioApplied: true,
                colonies: initialColonies,
                ants: [], // Strictly reconciled via server SimulationUpdate frames
                nests: initialNests,
                pheromones: [],
                foodSources: initialFoods,
                statsHistory: [],
                simTimeFormatted: formatSimCalendarTime(startDateTime, 0),
                simRelativeTimeFormatted: formatSimRelativeTime(0),
                environment: {
                    timeOfDay: 'DAY',
                    lightLevel: 1.0,
                    temperature: weatherPreset?.tempDay ?? 22.0,
                    humidity: weatherPreset?.humidity ?? 65.0,
                    windSpeed: 1.5,
                    weatherState: 'CLEAR',
                    season: weatherPreset?.season ?? 'SPRING',
                    solarRadiation: 450.0
                }
            })

            get().addEventLog({
                severity: 'INFO',
                type: 'SIMULATION_STARTED',
                source: 'Gestionnaire de Scénario',
                sourceKey: 'sourceScenario',
                message: `Nouveau scénario initialisé : ${worldPreset?.name || 'Monde'} (${initialColonies.length} colonies). Synchronisation avec le serveur SwarmForge en cours...`,
                metadata: { world: worldPreset?.name, coloniesCount: initialColonies.length }
            })

            // Switch to 3D view on apply (1:1 with SwarmForgeClient.java line 916)
            set({
                activeTab: 'VISUAL_3D',
                activeSubTab: 'VISUAL_3D'
            })
            soundEngine.updateSimulationState(false, get().speed, true)
        },

        play: () => {
            const state = get()
            if (state.running) return

            if (!state.connected) {
                networkClient.connect(state.serverHost, state.serverPort, state.playerAlias, state.playerSpecies, state.serverRole)
                showToast('Connexion au serveur SwarmForge en cours...', 'info')
            }
            networkClient.sendControlCommand('PLAY')

            set({
                running: true,
                isPaused: false
            })
            soundEngine.updateSimulationState(true, state.speed, get().activeTab === 'VISUAL_3D')

            get().addEventLog({
                severity: 'INFO',
                type: 'SIMULATION_STARTED',
                source: 'Moteur de Simulation',
                message: `Ordre de démarrage transmis au serveur (vitesse ${state.speed}x, dt=${state.stepSeconds}s)`,
                metadata: { speed: state.speed, dt: state.stepSeconds }
            })
        },

        pause: () => {
            if (get().connected) {
                networkClient.sendControlCommand('PAUSE')
            }
            set({ running: false, isPaused: true })
            soundEngine.updateSimulationState(false, get().speed, get().activeTab === 'VISUAL_3D')
            get().addEventLog({
                severity: 'INFO',
                type: 'SIMULATION_PAUSED',
                source: 'Moteur de Simulation',
                message: `Simulation mise en pause au tick ${get().ticks}`,
                metadata: { tick: get().ticks, simTime: get().simTimeFormatted }
            })
        },

        stepTick: () => {
            const state = get()
            if (state.connected) {
                networkClient.sendControlCommand('STEP')
            }
        },

        seekToTick: (targetTick) => {
            const state = get()
            const clampedTick = Math.max(0, Math.min(state.highestRecordedTick || 1000, Number(targetTick) || 0))
            const newSimTime = clampedTick * state.stepSeconds
            const calendarTime = formatSimCalendarTime(state.startDateTime, newSimTime)
            const relativeTime = formatSimRelativeTime(newSimTime)

            // Reset executed status of scheduled events that are ahead of clampedTick
            const updatedEvents = state.scheduledEvents.map(e => {
                if (e.targetTick > clampedTick) {
                    return { ...e, executed: false }
                }
                return e
            })

            // Truncate stats history after clampedTick
            const updatedHistory = state.statsHistory.filter(s => s.tick <= clampedTick)

            set({
                ticks: clampedTick,
                simTimeSeconds: newSimTime,
                simTimeFormatted: calendarTime,
                simRelativeTimeFormatted: relativeTime,
                scheduledEvents: updatedEvents,
                statsHistory: updatedHistory
            })
        },

        stepForward: () => {
            get().stepTick()
        },

        stepBackward: () => {
            const currentTicks = get().ticks
            if (currentTicks <= 0) return
            get().seekToTick(currentTicks - 1)
        },

        rewind: (ticksCount = 10) => {
            const currentTicks = get().ticks
            const newTicks = Math.max(0, currentTicks - ticksCount)
            get().seekToTick(newTicks)
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Transport',
                message: `Recul temporel de ${ticksCount} pas (Pas actuel : ${newTicks})`
            })
        },

        advanceTicks: (ticksCount = 100) => {
            for (let i = 0; i < ticksCount; i++) {
                get().stepTick()
            }
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Transport',
                message: `Avance rapide de ${ticksCount} pas (Tick actuel : ${get().ticks})`
            })
        },

        fastForward: () => {
            set({ speed: Math.min(50, get().speed * 2) })
        },

        goToBeginning: () => {
            get().pause()
            get().seekToTick(0)
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Transport',
                message: 'Retour au début de la simulation (T=0).'
            })
        },

        goToEnd: () => {
            get().pause()
            const target = Math.max(get().highestRecordedTick || 1000, get().ticks + 100)
            get().seekToTick(target)
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Transport',
                message: `Avance temporelle jusqu'à la fin enregistrée (Tick : ${target}).`
            })
        },

        setSpeed: (spd) => {
            const newSpeed = Math.max(0.1, Math.min(100, Number(spd) || 1.0))
            if (get().connected) {
                networkClient.sendControlCommand('SET_SPEED', newSpeed)
            }
            set({ speed: newSpeed })
            soundEngine.updateSimulationState(get().running, newSpeed, get().activeTab === 'VISUAL_3D')
        },
        setStepSeconds: (dt) => set({ stepSeconds: Math.max(0.001, Math.min(10.0, Number(dt) || 0.05)) }),

        // --- 10. Real Weather Live API (Open-Meteo) ---
        isRealWeatherMode: false,
        realWeatherCity: 'Paris',
        realWeatherStatus: 'Cliquez pour charger Open-Meteo',
        setRealWeatherMode: (val) => set({ isRealWeatherMode: val }),
        setRealWeatherCity: (city) => set({ realWeatherCity: city }),
        fetchRealWeather: async (queryCity) => {
            const city = queryCity || get().realWeatherCity || 'Paris'
            set({ realWeatherStatus: 'Chargement météo en cours...' })
            try {
                let lat = 48.8566
                let lon = 2.3522
                const coords = city.split(',')
                if (coords.length === 2 && !isNaN(parseFloat(coords[0])) && !isNaN(parseFloat(coords[1]))) {
                    lat = parseFloat(coords[0])
                    lon = parseFloat(coords[1])
                } else {
                    const geoRes = await fetch(`https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(city)}&count=1&language=fr&format=json`)
                    const geoData = await geoRes.json()
                    if (geoData.results && geoData.results.length > 0) {
                        lat = geoData.results[0].latitude
                        lon = geoData.results[0].longitude
                    }
                }
                const weatherRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,wind_speed_10m,direct_radiation`)
                const data = await weatherRes.json()
                if (data.current) {
                    const temp = data.current.temperature_2m ?? 20.0
                    const hum = data.current.relative_humidity_2m ?? 60.0
                    const wind = data.current.wind_speed_10m ?? 8.0
                    const radiation = data.current.direct_radiation ?? 400.0

                    set(state => ({
                        environment: {
                            ...state.environment,
                            temperature: temp,
                            humidity: hum,
                            windSpeed: wind,
                            solarRadiation: radiation
                        },
                        realWeatherStatus: `✓ ${city} : ${temp.toFixed(1)}°C, ${hum}% Hum, ${wind.toFixed(1)} km/h`
                    }))
                    get().addEventLog({
                        severity: 'INFO',
                        type: 'ENVIRONMENT',
                        source: 'Open-Meteo API',
                        message: `🛰️ Conditions réelles appliquées pour ${city} : ${temp.toFixed(1)}°C, ${hum}% Humidité, Vent ${wind.toFixed(1)} km/h.`
                    })
                }
            } catch (e) {
                set({ realWeatherStatus: 'Erreur lors de la récupération Open-Meteo' })
            }
        },

        // --- 11. Scenario Import / Export ---
        exportScenarioJson: () => {
            const s = get()
            const scenario = {
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
            }
            const blob = new Blob([JSON.stringify(scenario, null, 2)], { type: 'application/json' })
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `swarmforge_scenario_${Date.now()}.json`
            a.click()
            URL.revokeObjectURL(url)
        },

        importScenarioJson: (jsonString) => {
            try {
                const data = JSON.parse(jsonString)
                set({
                    scenarioDescription: data.description || '',
                    selectedWorldPresetId: data.worldPresetId || 'world_terrarium_01',
                    selectedWeatherPresetId: data.weatherPresetId || 'weather_printemps_doux',
                    masterSeed: data.masterSeed ?? 12345,
                    startDateTime: data.startDateTime || '2026-03-20T08:00:00',
                    maxDuration: data.maxDuration ?? 0,
                    durationUnit: data.durationUnit || 'Days',
                    minPopStop: data.minPopStop ?? 0,
                    isMultiplayerOnly: Boolean(data.isMultiplayerOnly),
                    requiredPlayerCount: data.requiredPlayerCount ?? 1,
                    gridTilesX: data.gridTilesX ?? 1,
                    gridTilesY: data.gridTilesY ?? 1,
                    speciesCards: data.speciesCards || []
                })
                get().applyScenarioSetup()
                return true
            } catch (e) {
                return false
            }
        },

        // --- 12. Metrics & Event Log CSV/JSON Exports ---
        exportStatsCsv: () => {
            const history = get().statsHistory
            if (history.length === 0) return
            const headers = ['Tick', 'SimTimeSeconds', 'TotalPopulation', 'Queens', 'Workers', 'Soldiers', 'Males', 'Food', 'Water', 'Protein', 'Foraging', 'Digging', 'Nursing', 'Guarding', 'Resting', 'TempC', 'Rain', 'TPS']
            const rows = history.map(h => [
                h.tick,
                h.simTimeSeconds.toFixed(2),
                h.totalPopulation,
                h.casteBreakdown?.queens ?? 0,
                h.casteBreakdown?.workers ?? 0,
                h.casteBreakdown?.soldiers ?? 0,
                h.casteBreakdown?.males ?? 0,
                h.resources?.food?.toFixed(1) ?? 0,
                h.resources?.water?.toFixed(1) ?? 0,
                h.resources?.protein?.toFixed(1) ?? 0,
                h.ethology?.foraging ?? 0,
                h.ethology?.digging ?? 0,
                h.ethology?.nursing ?? 0,
                h.ethology?.guarding ?? 0,
                h.ethology?.resting ?? 0,
                h.weather?.temp?.toFixed(1) ?? 0,
                h.weather?.rain?.toFixed(1) ?? 0,
                h.performance?.tps?.toFixed(1) ?? 0
            ].join(','))

            const csvContent = [headers.join(','), ...rows].join('\n')
            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `swarmforge_statistics_${Date.now()}.csv`
            a.click()
            URL.revokeObjectURL(url)
        },

        exportLogsCsv: () => {
            const logs = get().eventLogs
            if (logs.length === 0) return
            const headers = ['Id', 'Timestamp', 'Tick', 'Severity', 'Type', 'Source', 'Message']
            const rows = logs.map(l => [
                l.id,
                `"${l.timestamp}"`,
                l.tick,
                l.severity,
                l.type,
                `"${l.source || ''}"`,
                `"${(l.message || '').replace(/"/g, '""')}"`
            ].join(','))

            const csvContent = [headers.join(','), ...rows].join('\n')
            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `swarmforge_event_logs_${Date.now()}.csv`
            a.click()
            URL.revokeObjectURL(url)
        },

        exportLogsJson: () => {
            const logs = get().eventLogs
            const blob = new Blob([JSON.stringify(logs, null, 2)], { type: 'application/json' })
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `swarmforge_event_logs_${Date.now()}.json`
            a.click()
            URL.revokeObjectURL(url)
        },

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

// --- Reactive Network Client Event Binding ---
networkClient.onStateChange((netState) => {
    useSimulationStore.setState({
        connected: Boolean(netState.isConnected),
        serverStatusText: netState.text || (netState.isConnected ? `● Connecté (${netState.host}:${netState.port})` : 'Hors ligne')
    })
})

networkClient.onEventLog((evt) => {
    useSimulationStore.getState().addEventLog(evt)
})

networkClient.onServerScenariosReceived((scenarios) => {
    if (scenarios && Array.isArray(scenarios)) {
        useSimulationStore.setState({ serverScenarios: scenarios })
    }
})

networkClient.onLobbyStateReceived((lobbyState) => {
    if (lobbyState) {
        useSimulationStore.setState({
            lobbyStatus: lobbyState.status || 'LOBBY_WAITING',
            selectedServerScenarioId: lobbyState.selectedScenarioId || 'ACAD_01_LEVY_BROWNIAN',
            lobbyPlayers: lobbyState.players || []
        })
    }
})

networkClient.onScenarioReceived((scenario) => {
    if (scenario) {
        useSimulationStore.setState(state => ({
            selectedWorldPresetId: scenario.worldPresetId || state.selectedWorldPresetId,
            selectedWeatherPresetId: scenario.weatherPresetId || state.selectedWeatherPresetId,
            startDateTime: scenario.startDateTime || state.startDateTime,
            masterSeed: scenario.masterSeed ?? state.masterSeed,
            stepSeconds: scenario.stepSeconds ?? state.stepSeconds,
            maxDuration: scenario.maxDurationSeconds ?? state.maxDuration,
            speciesCards: scenario.speciesCards || state.speciesCards,
            colonies: scenario.colonies || state.colonies,
            nests: scenario.nests || state.nests,
            isScenarioApplied: true
        }))
        useSimulationStore.getState().addEventLog({
            severity: 'INFO',
            type: 'SYSTEM',
            source: 'SwarmForge Server',
            sourceKey: 'sourceEngine',
            message: `Scénario reçu du serveur hôte (${scenario.speciesCards?.length || 0} espèces configurées).`
        })
    }
})

networkClient.onSimulationUpdate((update) => {
    const state = useSimulationStore.getState()
    const newTick = update.tick !== undefined ? update.tick : (state.ticks + 1)
    const newSimTime = update.simTimeSeconds !== undefined ? update.simTimeSeconds : (newTick * (state.stepSeconds || 0.05))
    const calendarTime = formatSimCalendarTime(state.startDateTime, newSimTime)
    const relativeTime = formatSimRelativeTime(newSimTime)

    // Parse individuals / ants from server
    let updatedAnts = state.ants
    if (update.individuals && Array.isArray(update.individuals)) {
        updatedAnts = update.individuals
            .filter(ind => ind.alive !== false)
            .map(ind => {
                const existing = state.ants.find(a => a.id === ind.id)
                const colony = state.colonies.find(c => c.id === ind.colonyId)
                const posX = ind.position?.x ?? ind.x ?? ind.posX ?? existing?.x ?? 50
                const posY = ind.position?.y ?? ind.y ?? ind.posY ?? existing?.y ?? 0.1
                const posZ = ind.position?.z ?? ind.z ?? ind.posZ ?? existing?.z ?? 50
                const caste = ind.caste || ind.currentAction || existing?.caste || 'WORKER'
                const job = ind.job || existing?.job || 'FORAGER'

                return {
                    ...existing,
                    id: ind.id,
                    colonyId: ind.colonyId || existing?.colonyId || 'col_1',
                    colonyName: ind.colonyName || colony?.name || existing?.colonyName || 'Colonie',
                    species: ind.species || existing?.species || 'Formica fusca',
                    caste: caste,
                    job: job,
                    x: posX,
                    y: posY,
                    z: posZ,
                    heading: ind.heading ?? existing?.heading ?? 0,
                    speedMms: ind.speedMms ?? existing?.speedMms ?? 20.0,
                    health: ind.health ?? existing?.health ?? 100,
                    energy: ind.energy ?? existing?.energy ?? 100,
                    carriedItem: ind.carriedItem ?? existing?.carriedItem ?? 'NONE',
                    task: (ind.task ?? existing?.task) || (job === 'FORAGER' ? 'Fourragement' : (job === 'GUARD' ? 'Garde' : 'Activité en cours')),
                    color: ind.color || colony?.color || existing?.color || '#38bdf8'
                }
            })
    }

    // Parse food sources from server
    let updatedFoodSources = state.foodSources
    if (update.food && Array.isArray(update.food)) {
        updatedFoodSources = update.food.map((f, i) => ({
            id: f.id || `food_srv_${i}`,
            x: f.position?.x ?? f.x ?? f.posX ?? 50,
            y: f.position?.y ?? f.y ?? f.posY ?? 0.2,
            z: f.position?.z ?? f.z ?? f.posZ ?? 50,
            quantity: f.quantity ?? f.amount ?? 100,
            amount: f.quantity ?? f.amount ?? 100,
            type: f.type || 'SUGAR',
            name: f.type === 'SUGAR' ? 'Miellat / Sucre' : (f.type === 'SEEDS' ? 'Graines' : (f.type === 'PREY' ? 'Proie' : 'Eau'))
        }))
    }

    // Parse environment from server
    let updatedEnv = state.environment
    if (update.environment) {
        updatedEnv = {
            ...state.environment,
            temperature: update.environment.temperature ?? state.environment.temperature,
            humidity: update.environment.humidity ?? state.environment.humidity,
            windSpeed: update.environment.windSpeed ?? update.environment.wind ?? state.environment.windSpeed,
            weatherState: update.environment.weatherState ?? update.environment.weather ?? (update.environment.rainIntensity > 0 ? 'RAIN' : 'CLEAR'),
            season: update.environment.season ?? state.environment.season,
            lightLevel: update.environment.lightLevel ?? update.environment.light ?? state.environment.lightLevel,
            timeOfDay: update.environment.timeOfDay ?? state.environment.timeOfDay
        }
    }

    // Parse nests if provided
    let updatedNests = state.nests
    if (update.nests && Array.isArray(update.nests)) {
        updatedNests = update.nests.map(n => ({
            id: n.id || `nest_${n.id}`,
            colonyId: n.id,
            name: `Nid ${n.id}`,
            x: n.x ?? (n.chambers && n.chambers[0]?.position?.x) ?? 50,
            y: n.y ?? (n.chambers && n.chambers[0]?.position?.y) ?? 0,
            z: n.z ?? (n.chambers && n.chambers[0]?.position?.z) ?? 50,
            chambers: (n.chambers || []).map(ch => ({
                id: ch.id,
                name: ch.id,
                type: ch.type || 'QUEEN',
                x: ch.position?.x ?? ch.x ?? 50,
                y: ch.position?.y ?? ch.y ?? -1.2,
                z: ch.position?.z ?? ch.z ?? 50,
                radius: 1.2
            })),
            tunnels: (n.tunnels || []).map(tu => ({
                from: [50, 0, 50],
                to: [50, -1.2, 50]
            }))
        }))
    }

    // Parse colonies if provided
    let updatedColonies = state.colonies
    if (update.colonies && Array.isArray(update.colonies)) {
        updatedColonies = update.colonies
    }

    // Parse predators from server
    let updatedPredators = state.predators
    if (update.predators && Array.isArray(update.predators)) {
        updatedPredators = update.predators.map(p => ({
            id: p.id || `pred_${Math.random()}`,
            type: p.type || 'SPIDER',
            x: p.x ?? p.posX ?? 50,
            y: p.y ?? p.posY ?? 0.3,
            z: p.z ?? p.posZ ?? 50,
            state: p.state || 'HUNTING',
            health: p.health ?? 100
        }))
    }

    // Tracked Ant telemetry update
    let updatedTrackedAntData = state.trackedAntData
    if (state.trackedAntId) {
        const currentTracked = updatedAnts.find(a => a.id === state.trackedAntId)
        if (currentTracked) {
            const dt = state.stepSeconds || 0.05
            const deltaMeters = ((currentTracked.speedMms || 20.0) * dt) / 1000.0
            updatedTrackedAntData = {
                ...currentTracked,
                distanceTraveled: (state.trackedAntData?.distanceTraveled || 0) + deltaMeters,
                healthHistory: [...(state.trackedAntData?.healthHistory || []), currentTracked.health].slice(-100),
                energyHistory: [...(state.trackedAntData?.energyHistory || []), currentTracked.energy].slice(-100)
            }
        }
    }

    // Append telemetry snapshot to statsHistory
    const statsSnapshot = {
        tick: newTick,
        simTimeSeconds: newSimTime,
        totalPopulation: updatedAnts.length,
        coloniesPop: updatedColonies.reduce((acc, c) => ({ ...acc, [c.id]: updatedAnts.filter(a => a.colonyId === c.id).length }), {}),
        casteBreakdown: {
            queens: updatedAnts.filter(a => a.caste === 'QUEEN').length,
            workers: updatedAnts.filter(a => a.caste === 'WORKER').length,
            soldiers: updatedAnts.filter(a => a.caste === 'SOLDIER').length,
            males: updatedAnts.filter(a => a.caste === 'MALE').length
        },
        resources: {
            food: updatedColonies.reduce((sum, c) => sum + (c.food || 0), 0),
            water: updatedColonies.reduce((sum, c) => sum + (c.water || 0), 0),
            protein: updatedColonies.reduce((sum, c) => sum + (c.protein || 0), 0),
            births: Math.floor(newTick / 300),
            deaths: Math.floor(newTick / 600)
        },
        weather: {
            temp: updatedEnv.temperature,
            rain: updatedEnv.weatherState === 'TEMPEST' ? 25.0 : 0.0,
            phero: (update.pheromones || state.pheromones || []).length
        },
        behaviors: {
            foraging: updatedAnts.filter(a => a.job === 'FORAGER').length,
            digging: updatedAnts.filter(a => a.job === 'BUILDER').length,
            nursing: updatedAnts.filter(a => a.job === 'NURSE').length,
            guarding: updatedAnts.filter(a => a.job === 'GUARD').length,
            royalCare: updatedAnts.filter(a => a.caste === 'QUEEN').length,
            resting: updatedAnts.filter(a => a.job === 'RESTING').length
        },
        performance: {
            tps: update.tps ?? state.measuredTps ?? 20,
            targetTps: 20
        }
    }

    let updatedHistory = state.statsHistory
    if (newTick % 3 === 0 || updatedHistory.length === 0) {
        updatedHistory = [...state.statsHistory, statsSnapshot].slice(-7200)
    }

    useSimulationStore.setState({
        ticks: newTick,
        highestRecordedTick: Math.max(state.highestRecordedTick, newTick),
        simTimeSeconds: newSimTime,
        simTimeFormatted: calendarTime,
        simRelativeTimeFormatted: relativeTime,
        ants: updatedAnts,
        foodSources: updatedFoodSources,
        predators: updatedPredators,
        environment: updatedEnv,
        nests: updatedNests,
        colonies: updatedColonies,
        statsHistory: updatedHistory,
        trackedAntData: updatedTrackedAntData,
        measuredTps: update.tps ?? state.measuredTps
    })
})

networkClient.onScenarioData((scenario) => {
    if (scenario) {
        useSimulationStore.getState().addEventLog({
            severity: 'INFO',
            type: 'SYSTEM',
            source: 'SwarmForge Server',
            message: `Scénario "${scenario.title || scenario.id}" prêt pour export.`
        })
        try {
            const blob = new Blob([JSON.stringify(scenario, null, 2)], { type: 'application/json' })
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `swarmforge_scenario_${scenario.id || Date.now()}.json`
            a.click()
            URL.revokeObjectURL(url)
        } catch (e) {
            console.warn('Could not auto-download scenario blob:', e)
        }
    }
})

