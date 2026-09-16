import { create } from 'zustand'
import { soundEngine } from '../utils/soundEngine'
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

const createDefaultSpeciesCards = () => [
    {
        id: 'card_col_1',
        name: 'Colonie Principale (Formica fusca)',
        speciesId: 'species_formica_fusca',
        nestType: 'DOME_AND_SUBTERRANEAN',
        color: '#38bdf8',
        initialQueens: 1,
        initialWorkers: 80,
        initialSoldiers: 15,
        initialMales: 0,
        aiArchitecture: 'BEHAVIOR_TREE',
        preyPredatorPresetId: 'prey_pred_pucerons_fourmilion'
    },
    {
        id: 'card_col_2',
        name: 'Colonie Rivale (Linepithema humile)',
        speciesId: 'species_linepithema_humile',
        nestType: 'ARBOREAL_WOOD',
        color: '#f43f5e',
        initialQueens: 2,
        initialWorkers: 50,
        initialSoldiers: 10,
        initialMales: 0,
        aiArchitecture: 'HYBRID_RULE_UTILITY',
        preyPredatorPresetId: 'prey_pred_peaceful_abundance'
    }
]

function spawnInitialAntsFromCards(speciesCards) {
    const ants = []
    const cards = speciesCards && speciesCards.length > 0 ? speciesCards : createDefaultSpeciesCards()

    cards.forEach((card, cIdx) => {
        const baseX = cIdx === 0 ? 35 : 65
        const baseY = cIdx === 0 ? 35 : 65
        const speciesPreset = DEFAULT_SPECIES_PRESETS.find(s => s.id === card.speciesId) || DEFAULT_SPECIES_PRESETS[0]

        // Queens
        for (let i = 0; i < (card.initialQueens || 1); i++) {
            ants.push({
                id: `ant_${card.id}_queen_${i + 1}`,
                colonyId: card.id,
                colonyName: card.name,
                species: speciesPreset.name,
                caste: 'QUEEN',
                job: 'LAYING_EGGS',
                x: baseX + (Math.random() - 0.5) * 4,
                y: -1.2,
                z: baseY + (Math.random() - 0.5) * 4,
                vx: 0, vy: 0, vz: 0,
                health: 100,
                energy: 100,
                age: 180 + Math.floor(Math.random() * 100),
                carriedItem: 'NONE',
                task: 'Royal Care & Egg Laying',
                color: card.color
            })
        }

        // Soldiers
        for (let i = 0; i < (card.initialSoldiers || 10); i++) {
            ants.push({
                id: `ant_${card.id}_soldier_${i + 1}`,
                colonyId: card.id,
                colonyName: card.name,
                species: speciesPreset.name,
                caste: 'SOLDIER',
                job: 'GUARD',
                x: baseX + (Math.random() - 0.5) * 12,
                y: 0.1,
                z: baseY + (Math.random() - 0.5) * 12,
                vx: (Math.random() - 0.5) * 0.2,
                vy: 0,
                vz: (Math.random() - 0.5) * 0.2,
                health: 100,
                energy: 90 + Math.random() * 10,
                age: 30 + Math.floor(Math.random() * 60),
                carriedItem: 'NONE',
                task: 'Nest Perimeter Defense',
                color: card.color
            })
        }

        // Workers
        for (let i = 0; i < (card.initialWorkers || 40); i++) {
            const isForager = i % 2 === 0
            const isBuilder = i % 4 === 1
            const job = isForager ? 'FORAGER' : (isBuilder ? 'BUILDER' : 'NURSE')
            const task = isForager ? 'Searching for Nectar/Seeds' : (isBuilder ? 'Tunnel Excavation' : 'Tending Brood')

            ants.push({
                id: `ant_${card.id}_worker_${i + 1}`,
                colonyId: card.id,
                colonyName: card.name,
                species: speciesPreset.name,
                caste: 'WORKER',
                job: job,
                x: baseX + (Math.random() - 0.5) * 20,
                y: isBuilder ? -0.8 : 0.1,
                z: baseY + (Math.random() - 0.5) * 20,
                vx: (Math.random() - 0.5) * 0.3,
                vy: 0,
                vz: (Math.random() - 0.5) * 0.3,
                health: 100,
                energy: 85 + Math.random() * 15,
                age: 15 + Math.floor(Math.random() * 45),
                carriedItem: isForager && Math.random() > 0.6 ? 'SUGAR' : 'NONE',
                task: task,
                color: card.color
            })
        }

        // Males
        for (let i = 0; i < (card.initialMales || 0); i++) {
            ants.push({
                id: `ant_${card.id}_male_${i + 1}`,
                colonyId: card.id,
                colonyName: card.name,
                species: speciesPreset.name,
                caste: 'MALE',
                job: 'RESTING',
                x: baseX + (Math.random() - 0.5) * 6,
                y: -0.5,
                z: baseY + (Math.random() - 0.5) * 6,
                vx: 0, vy: 0, vz: 0,
                health: 100,
                energy: 95,
                age: 10 + Math.floor(Math.random() * 20),
                carriedItem: 'NONE',
                task: 'Resting in Queen Chamber',
                color: card.color
            })
        }
    })

    return ants
}

function generateInitialColonies(speciesCards) {
    const cards = speciesCards && speciesCards.length > 0 ? speciesCards : createDefaultSpeciesCards()
    return cards.map(c => ({
        id: c.id,
        name: c.name,
        speciesId: c.speciesId,
        color: c.color,
        food: 250,
        water: 150,
        protein: 80,
        population: (c.initialQueens || 1) + (c.initialWorkers || 40) + (c.initialSoldiers || 10) + (c.initialMales || 0),
        queens: c.initialQueens || 1,
        workers: c.initialWorkers || 40,
        soldiers: c.initialSoldiers || 10,
        males: c.initialMales || 0
    }))
}

function generateInitialFoodSources() {
    return [
        { id: 'food_1', x: 25, y: 0.2, z: 25, type: 'SUGAR', name: 'Miellat de Pucerons', amount: 350, maxAmount: 500, color: '#f59e0b' },
        { id: 'food_2', x: 75, y: 0.2, z: 75, type: 'SEEDS', name: 'Graines de Graminées', amount: 600, maxAmount: 800, color: '#eab308' },
        { id: 'food_3', x: 70, y: 0.2, z: 30, type: 'PREY', name: 'Cadavre de Scarabée', amount: 400, maxAmount: 400, color: '#ef4444' },
        { id: 'food_4', x: 30, y: 0.2, z: 70, type: 'WATER', name: 'Gouttelettes de Rosée', amount: 200, maxAmount: 300, color: '#38bdf8' }
    ]
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
        isScenarioApplied: true, // Whether scenario has been initialized

        slicePlaneRatio: 1.0,
        setSlicePlaneRatio: (ratio) => set({ slicePlaneRatio: typeof ratio === 'number' && !isNaN(ratio) ? ratio : 1.0 }),

        setActiveTab: (tab) => {
            const currentRunning = get().running
            // Auto-pause when entering God Mode (1:1 with SwarmForgeClient.java line 1052)
            if (tab === 'GOD_MODE' && currentRunning) {
                get().pause()
                get().addEventLog({
                    severity: 'WARNING',
                    type: 'SYSTEM',
                    source: 'Mode Divin',
                    message: '⏸️ Simulation mise en pause automatique pour agencement des interventions.'
                })
            }
            // Update audio engine to ensure sound only plays when 3D tab is active
            soundEngine.updateSimulationState(get().running, get().speed, tab === 'VISUAL_3D')
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
        selectedScenarioPresetId: 'scenario_mon_terrarium_1',
        selectedWorldPresetId: 'world_terrarium_01',
        selectedWeatherPresetId: 'weather_printemps_doux',
        masterSeed: 12345,
        scenarioDescription: 'Scénario standard de simulation multi-espèces et équilibre trophique.',
        maxDuration: 100.0,
        durationUnit: 'Days',
        minPopStop: 0,
        speciesCards: createDefaultSpeciesCards(),

        setScenarioPresetId: (presetId) => {
            const meta = DEFAULT_SCENARIO_META_PRESETS.find(p => p.id === presetId)
            if (meta) {
                set({
                    selectedScenarioPresetId: presetId,
                    selectedWorldPresetId: meta.worldPresetId || 'world_terrarium_01',
                    selectedWeatherPresetId: meta.weatherPresetId || 'weather_printemps_doux',
                    masterSeed: meta.masterSeed || 12345,
                    scenarioDescription: meta.description || ''
                })
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
        colonies: generateInitialColonies(createDefaultSpeciesCards()),
        ants: spawnInitialAntsFromCards(createDefaultSpeciesCards()),
        foodSources: generateInitialFoodSources(),
        predators: [],
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
        scheduledEvents: [
            {
                id: 'evt_init_1',
                targetTick: 120,
                scheduledCalendarTime: '2026-03-20 08:01:00',
                category: 'RESOURCE',
                eventType: 'Surface Food Deposit',
                colonyTarget: 'TOUTES',
                description: 'Dépôt automatique de graines de pissenlit (+150)',
                action: 'SPAWN_FOOD',
                amount: 150,
                posX: 45, posY: 45, posZ: 0.2,
                executed: false,
                paused: false
            }
        ],

        addScheduledEvent: (event) => {
            const current = get().scheduledEvents
            const newEvt = {
                id: `evt_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
                executed: false,
                paused: false,
                ...event
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

        // --- 7. Event Log Bus ---
        eventsLog: [
            {
                id: 'evt_log_init',
                tick: 0,
                simCalendarTime: '2026-03-20 08:00:00',
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Moteur SwarmForge',
                message: 'Simulation initialisée avec succès en mode autonome haute performance.',
                metadata: { seed: 12345, world: 'world_terrarium_01' }
            }
        ],

        addEventLog: (log) => {
            const current = get().eventsLog
            const newLog = {
                id: `log_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
                tick: get().ticks,
                simCalendarTime: get().simTimeFormatted,
                severity: log.severity || 'INFO',
                type: log.type || 'SYSTEM',
                source: log.source || 'Simulation Engine',
                message: log.message || '',
                metadata: log.metadata || {}
            }
            set({ eventsLog: [newLog, ...current].slice(0, 1000) })
        },

        clearEventLogs: () => set({ eventsLog: [] }),

        // --- 8. Server Connection (gRPC / WebSocket) ---
        serverHost: 'localhost',
        serverPort: 50051,
        connected: false,
        serverStatusText: '○ Hors-ligne (Mode autonome local)',

        setServerHost: (host) => set({ serverHost: host }),
        setServerPort: (port) => set({ serverPort: Number(port) || 50051 }),

        connect: () => {
            const { serverHost, serverPort } = get()
            set({ serverStatusText: '⟳ Connexion en cours...' })
            try {
                set({
                    connected: false,
                    serverStatusText: `○ Hors-ligne (${serverHost}:${serverPort} injoignable, mode autonome actif)`
                })
            } catch (e) {
                set({
                    connected: false,
                    serverStatusText: '○ Hors-ligne (Mode autonome local)'
                })
            }
        },

        disconnect: () => {
            set({
                connected: false,
                serverStatusText: '○ Hors-ligne'
            })
        },

        discover: () => {
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Réseau',
                message: 'Détection automatique de serveurs SwarmForge locaux sur les ports 50051, 8080...'
            })
            setTimeout(() => {
                get().addEventLog({
                    severity: 'INFO',
                    type: 'SYSTEM',
                    source: 'Réseau',
                    message: 'Détection terminée : Aucun serveur distant actif détecté. Mode autonome actif.'
                })
            }, 500)
        },

        // --- 9. Core Simulation Actions & Transport Controls ---
        applyScenarioSetup: () => {
            const { speciesCards, startDateTime, masterSeed, selectedWorldPresetId, selectedWeatherPresetId } = get()
            const initialColonies = generateInitialColonies(speciesCards)
            const initialAnts = spawnInitialAntsFromCards(speciesCards)
            const initialFoods = generateInitialFoodSources()

            const worldPreset = DEFAULT_WORLD_PRESETS.find(w => w.id === selectedWorldPresetId)
            const weatherPreset = DEFAULT_WEATHER_PRESETS.find(w => w.id === selectedWeatherPresetId)

            set({
                ticks: 0,
                highestRecordedTick: 0,
                simTimeSeconds: 0,
                running: false,
                isPaused: false,
                isScenarioApplied: true,
                colonies: initialColonies,
                ants: initialAnts,
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
                type: 'COLONY',
                source: 'Gestionnaire de Scénario',
                message: `Nouveau scénario appliqué : ${worldPreset?.name || 'Monde'} (${initialAnts.length} individus, ${initialColonies.length} colonies)`
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

            set({ running: true, isPaused: false })
            soundEngine.updateSimulationState(true, state.speed, get().activeTab === 'VISUAL_3D')

            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Moteur de Simulation',
                message: `Simulation lancée à vitesse ${state.speed}x (dt=${state.stepSeconds}s)`
            })

            lastTickTime = performance.now()
            lastTpsMeasureTime = performance.now()
            tickCountInWindow = 0

            clearInterval(simLoopInterval)
            simLoopInterval = setInterval(() => {
                const current = get()
                if (!current.running || current.isPaused) return

                const now = performance.now()
                const elapsedMs = now - lastTickTime
                lastTickTime = now

                const ticksToAdvance = Math.max(1, Math.round((current.speed * (elapsedMs / 1000)) / current.stepSeconds))

                for (let i = 0; i < Math.min(ticksToAdvance, 20); i++) {
                    get().stepTick()
                }

                tickCountInWindow += Math.min(ticksToAdvance, 20)
                if (now - lastTpsMeasureTime >= 500) {
                    const actualTps = Math.round((tickCountInWindow / (now - lastTpsMeasureTime)) * 1000)
                    set({ measuredTps: actualTps })
                    tickCountInWindow = 0
                    lastTpsMeasureTime = now
                }
            }, 33)
        },

        pause: () => {
            set({ running: false, isPaused: true })
            soundEngine.updateSimulationState(false, get().speed, get().activeTab === 'VISUAL_3D')
            clearInterval(simLoopInterval)
            get().addEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Moteur de Simulation',
                message: `Simulation mise en pause au tick ${get().ticks}`
            })
        },

        stepTick: () => {
            const state = get()
            const newTick = state.ticks + 1
            const newHighestTick = Math.max(state.highestRecordedTick, newTick)
            const newSimTime = newTick * state.stepSeconds
            const calendarTime = formatSimCalendarTime(state.startDateTime, newSimTime)
            const relativeTime = formatSimRelativeTime(newSimTime)

            // 1. Check & Execute Scheduled Events
            const pendingEvents = state.scheduledEvents.filter(e => !e.executed && !e.paused && e.targetTick <= newTick)
            if (pendingEvents.length > 0) {
                pendingEvents.forEach(evt => {
                    get().executeInstantIntervention(evt)
                    evt.executed = true
                })
                set({
                    scheduledEvents: state.scheduledEvents.map(e => {
                        const matched = pendingEvents.find(p => p.id === e.id)
                        return matched ? { ...e, executed: true } : e
                    })
                })
            }

            // 2. Simulate Ant movement and behaviors
            let foragingCount = 0
            let diggingCount = 0
            let nursingCount = 0
            let guardingCount = 0
            let royalCareCount = 0
            let restingCount = 0
            let workersCount = 0
            let soldiersCount = 0
            let queensCount = 0
            let malesCount = 0

            const updatedAnts = state.ants.map(ant => {
                if (ant.caste === 'QUEEN') {
                    queensCount++
                    royalCareCount++
                } else if (ant.caste === 'SOLDIER') {
                    soldiersCount++
                    guardingCount++
                } else if (ant.caste === 'MALE') {
                    malesCount++
                    restingCount++
                } else {
                    workersCount++
                    if (ant.job === 'FORAGER') foragingCount++
                    else if (ant.job === 'BUILDER') diggingCount++
                    else nursingCount++
                }

                const speedMult = ant.caste === 'SOLDIER' ? 0.25 : 0.4
                const newX = Math.max(5, Math.min(95, ant.x + (Math.random() - 0.5) * speedMult))
                const newZ = Math.max(5, Math.min(95, ant.z + (Math.random() - 0.5) * speedMult))
                const newEnergy = Math.max(10, ant.energy - 0.005)

                return {
                    ...ant,
                    x: newX,
                    z: newZ,
                    energy: newEnergy
                }
            })

            // 3. Update Colonies stats & Demographics
            const updatedColonies = state.colonies.map(col => {
                const colAnts = updatedAnts.filter(a => a.colonyId === col.id)
                return {
                    ...col,
                    population: colAnts.length,
                    food: Math.max(0, col.food + (foragingCount > 0 ? 0.05 : -0.02)),
                    queens: colAnts.filter(a => a.caste === 'QUEEN').length,
                    workers: colAnts.filter(a => a.caste === 'WORKER').length,
                    soldiers: colAnts.filter(a => a.caste === 'SOLDIER').length,
                    males: colAnts.filter(a => a.caste === 'MALE').length
                }
            })

            // 4. Record Snapshot into Statistics History
            const statsSnapshot = {
                tick: newTick,
                simTimeSeconds: newSimTime,
                totalPopulation: updatedAnts.length,
                coloniesPop: updatedColonies.reduce((acc, c) => ({ ...acc, [c.id]: c.population }), {}),
                casteBreakdown: {
                    queens: queensCount,
                    workers: workersCount,
                    soldiers: soldiersCount,
                    males: malesCount
                },
                resources: {
                    food: updatedColonies.reduce((sum, c) => sum + c.food, 0),
                    water: updatedColonies.reduce((sum, c) => sum + c.water, 0),
                    protein: updatedColonies.reduce((sum, c) => sum + c.protein, 0),
                    births: Math.floor(newTick / 300),
                    deaths: Math.floor(newTick / 600)
                },
                weather: {
                    temp: state.environment.temperature,
                    rain: state.environment.weatherState === 'TEMPEST' ? 25.0 : 0.0,
                    phero: 100 + (updatedAnts.length * 2.5)
                },
                behaviors: {
                    foraging: foragingCount,
                    digging: diggingCount,
                    nursing: nursingCount,
                    guarding: guardingCount,
                    royalCare: royalCareCount,
                    resting: restingCount
                },
                performance: {
                    tps: state.measuredTps || 20,
                    targetTps: 20
                }
            }

            let updatedHistory = state.statsHistory
            if (newTick % 5 === 0 || updatedHistory.length === 0) {
                updatedHistory = [...state.statsHistory, statsSnapshot].slice(-1200)
            }

            // 5. Update Tracked Individual Ant Telemetry if active
            let updatedTrackedAntData = state.trackedAntData
            if (state.trackedAntId) {
                const currentTracked = updatedAnts.find(a => a.id === state.trackedAntId)
                if (currentTracked) {
                    updatedTrackedAntData = {
                        ...currentTracked,
                        distanceTraveled: (state.trackedAntData?.distanceTraveled || 0) + 0.15,
                        healthHistory: [...(state.trackedAntData?.healthHistory || []), currentTracked.health].slice(-100),
                        energyHistory: [...(state.trackedAntData?.energyHistory || []), currentTracked.energy].slice(-100)
                    }
                }
            }

            set({
                ticks: newTick,
                highestRecordedTick: newHighestTick,
                simTimeSeconds: newSimTime,
                simTimeFormatted: calendarTime,
                simRelativeTimeFormatted: relativeTime,
                ants: updatedAnts,
                colonies: updatedColonies,
                statsHistory: updatedHistory,
                trackedAntData: updatedTrackedAntData
            })
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
                message: `Recul temporel de ${ticksCount} ticks (Tick actuel : ${newTicks})`
            })
        },

        fastForward: () => {
            set({ speed: Math.min(100, get().speed * 2) })
        },

        goToBeginning: () => {
            get().pause()
            get().applyScenarioSetup()
        },

        goToEnd: () => {
            get().pause()
            get().addEventLog({
                severity: 'WARNING',
                type: 'SYSTEM',
                source: 'Transport',
                message: 'Atteinte de la fin du scénario.'
            })
        },

        setSpeed: (spd) => {
            const newSpeed = Math.max(0.1, Math.min(100, Number(spd) || 1.0))
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
                    durationUnit: data.durationUnit || 'minutes',
                    minPopStop: data.minPopStop ?? 0,
                    speciesCards: data.speciesCards || createDefaultSpeciesCards()
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
