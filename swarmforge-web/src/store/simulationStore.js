import { create } from 'zustand'

export const useSimulationStore = create((set, get) => ({
    // Connection state
    connected: false,
    ws: null,

    // Simulation state
    tick: 0,
    running: false,
    speed: 1.0,

    // Entity data (Default multi-colony setup)
    colonies: [
        { id: 'COLONY_1', name: 'Colony #1 (Native)', species: 'Formica fusca', color: '#38bdf8', foodStored: 250, queenCount: 1, workerCount: 120 },
        { id: 'COLONY_2', name: 'Colony #2 (Rival)', species: 'Linepithema humile', color: '#f43f5e', foodStored: 180, queenCount: 2, workerCount: 90 },
    ],
    ants: [],
    predators: [],
    foodSources: [
        { id: 'food_init_1', x: 45, y: 55, quantity: 200, type: 'SUGAR_NECTAR' },
        { id: 'food_init_2', x: 60, y: 40, quantity: 150, type: 'SEEDS' },
    ],
    nests: [],

    // Simulation Parameters (Categorized with strictly metric SI units)
    simulationParams: {
        // Category 1: Phéromones & Diffusion
        pheromones: {
            dissipationRate: { value: 0.50, unit: '%/s', min: 0.01, max: 5.00, label: 'Taux de Dissipation des Phéromones' },
            diffusionRadius: { value: 1.20, unit: 'm', min: 0.20, max: 5.00, label: 'Rayon de Diffusion Évaporative' },
            alarmThreshold: { value: 40, unit: '%', min: 5, max: 100, label: 'Seuil d\'Intensité d\'Alarme' },
        },
        // Category 2: Ressources & Alimentation
        resources: {
            foodSpawnRate: { value: 12, unit: 'g/min', min: 1, max: 100, label: 'Taux d\'Apparition de Nourriture' },
            foodDecayRate: { value: 0.20, unit: '%/h', min: 0.0, max: 2.0, label: 'Taux de Décomposition Organic' },
            honeydewRate: { value: 2.50, unit: 'mg/h', min: 0.1, max: 10.0, label: 'Production de Miellat (Pucerons)' },
        },
        // Category 3: Espèces & Démographie
        species: {
            queenFecundity: { value: 60, unit: 'œufs/jour', min: 5, max: 300, label: 'Fécondité Reine (Taux de Ponte)' },
            workerLifespan: { value: 90, unit: 'jours', min: 10, max: 365, label: 'Espérance de Vie des Ouvrières' },
            soldierRatio: { value: 25, unit: '%', min: 0, max: 60, label: 'Proportion de Soldats dans la Colonie' },
        },
        // Category 4: Commerciaux & Économie
        economics: {
            metabolicCost: { value: 0.50, unit: 'mW', min: 0.05, max: 5.00, label: 'Puissance Métabolique de Base' },
            harvestEfficiency: { value: 85, unit: '%', min: 20, max: 100, label: 'Rendement de Récolte Énergétique' },
        }
    },

    setSimulationParam: (category, key, newValue) => set(state => ({
        simulationParams: {
            ...state.simulationParams,
            [category]: {
                ...state.simulationParams[category],
                [key]: {
                    ...state.simulationParams[category][key],
                    value: Number(newValue)
                }
            }
        }
    })),

    // Multi-Colony Management Actions
    addColony: (newColony) => set(state => {
        const id = newColony.id || `COLONY_${Date.now()}`
        const updated = [...state.colonies, { ...newColony, id }]
        get().addEventLog({
            level: 'INFO',
            category: 'COLONY',
            message: `🏛️ Colonie "${newColony.name}" (${newColony.species}) créée et enregistrée dans le monde.`,
        })
        return { colonies: updated }
    }),

    removeColony: (colonyId) => set(state => {
        const col = state.colonies.find(c => c.id === colonyId)
        get().addEventLog({
            level: 'WARN',
            category: 'COLONY',
            message: `🗑️ Colonie "${col?.name || colonyId}" supprimée.`,
        })
        return { colonies: state.colonies.filter(c => c.id !== colonyId) }
    }),

    // Real-Time Dense Simulation Event Logging System
    eventLogs: [
        {
            id: `evt_init_${Date.now()}`,
            tick: 0,
            timestamp: new Date().toISOString(),
            level: 'INFO',
            category: 'SYSTEM',
            message: '🎬 Session de simulation initialisée. Prêt pour le démarrage.',
        }
    ],

    addEventLog: (logObj) => set(state => {
        const newLog = {
            id: `evt_${Date.now()}_${Math.floor(Math.random() * 10000)}`,
            tick: state.tick,
            timestamp: new Date().toISOString(),
            level: logObj.level || 'INFO',
            category: logObj.category || 'SIMULATION',
            message: logObj.message || '',
            details: logObj.details || null,
        }
        // Cap max log entries to 500 for optimal UI performance
        const updated = [newLog, ...state.eventLogs.slice(0, 499)]
        return { eventLogs: updated }
    }),

    clearEventLogs: () => set({ eventLogs: [] }),

    // Mode Divin (God Mode) Intervention Log for Deterministic Replay
    interventionsLog: [],

    recordDivineIntervention: (actionData) => set(state => {
        const intervention = {
            id: `god_act_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            tick: state.tick,
            timestamp: new Date().toISOString(),
            ...actionData
        }

        // Apply physical changes locally if in standalone/simulated mode
        let newFood = [...state.foodSources]
        let newPredators = [...state.predators]

        if (actionData.type === 'SPAWN_FOOD') {
            newFood.push({
                id: `food_god_${Date.now()}`,
                x: actionData.x || 50,
                y: actionData.y || 50,
                quantity: actionData.quantity || 100,
                type: actionData.foodType || 'SUGAR_NECTAR'
            })
        } else if (actionData.type === 'SPAWN_PREDATOR') {
            newPredators.push({
                id: `pred_god_${Date.now()}`,
                x: actionData.x || 50,
                y: actionData.y || 50,
                type: actionData.predatorType || 'SPIDER',
                state: 'HUNTING'
            })
        }

        // Send via WebSocket if connected
        if (state.ws && state.connected) {
            state.ws.send(JSON.stringify({
                type: 'GOD_MODE_INTERVENTION',
                intervention
            }))
        }

        get().addEventLog({
            level: 'WARN',
            category: 'GOD_MODE',
            message: `⚡ Intervention Divinement: ${actionData.actionName}`,
            details: actionData
        })

        return {
            interventionsLog: [intervention, ...state.interventionsLog],
            foodSources: newFood,
            predators: newPredators
        }
    }),

    clearInterventionsLog: () => set({ interventionsLog: [] }),

    // Local tick timer ref
    localTickInterval: null,

    // Statistics
    stats: {
        totalPopulation: 210,
        totalWorkers: 150,
        totalSoldiers: 60,
        totalFood: 430,
    },

    // Environment
    environment: {
        terrariumWidth: 2.0,  // Variable surface size from World Editor (meters)
        terrariumDepth: 2.0,  // Variable surface size from World Editor (meters)
        terrariumHeight: 1.0, // Depth/Height (meters)
        lightLevel: 1.0,
        timeOfDay: 'DAY',
        sunAngle: 0.5,
        temperature: 20,
        humidity: 50,
        rainIntensity: 0,
        windSpeed: 5,
        weatherState: 'CLEAR', // 'CLEAR', 'CLOUDY', 'THUNDERSTORM', 'SNOW', 'BLIZZARD', 'TEMPEST', 'HAIL', 'FOG'
    },

    updateEnvironment: (newEnv) => {
        set(state => ({
            environment: { ...state.environment, ...newEnv }
        }))
        get().addEventLog({
            level: 'INFO',
            category: 'ENVIRONMENT',
            message: `🌍 Mise à jour environnementale: Météo=${newEnv.weatherState || get().environment.weatherState}, Temp=${(newEnv.temperature ?? get().environment.temperature).toFixed(1)}°C`
        })
    },

    // Display Toggles for Simulation Mode (Soleil, Éclairs, Nuages, Pluie, Brouillard, Vent, Vision Nuit)
    weatherToggles: {
        showSun: true,
        showLightning: true,
        showClouds: true,
        showPrecipitation: true,
        showFog: true,
        showWindDust: true,
        nightVision: false,
        lightningTrigger: 0,
    },
    setWeatherToggle: (key, value) => set(state => ({
        weatherToggles: { ...state.weatherToggles, [key]: value }
    })),
    triggerLightning: () => {
        set(state => ({
            weatherToggles: { ...state.weatherToggles, lightningTrigger: state.weatherToggles.lightningTrigger + 1 }
        }))
        get().addEventLog({
            level: 'WARN',
            category: 'WEATHER',
            message: '⚡ Éclair atmosphérique déclenché par l\'utilisateur.'
        })
    },

    // Selection
    selectedEntity: null,
    setSelectedEntity: (entity) => set({ selectedEntity: entity }),

    // Actions
    connect: () => {
        try {
            const ws = new WebSocket('ws://localhost:8081')

            ws.onopen = () => {
                console.log('WebSocket connected')
                set({ connected: true, ws })
                get().addEventLog({
                    level: 'INFO',
                    category: 'SYSTEM',
                    message: '🟢 Connecté au serveur de simulation haute performance (WebSocket: 8081)'
                })
                ws.send(JSON.stringify({ type: 'SUBSCRIBE', viewport: { x: 0, y: 0, width: 100, height: 100 } }))
            }

            ws.onmessage = (event) => {
                const data = JSON.parse(event.data)
                get().handleMessage(data)
            }

            ws.onclose = () => {
                set({ connected: false, ws: null })
            }

            ws.onerror = (err) => {
                set({ connected: false, ws: null })
            }
        } catch (e) {
            set({ connected: false, ws: null })
        }
    },

    disconnect: () => {
        const { ws } = get()
        if (ws) {
            ws.close()
            set({ connected: false, ws: null })
        }
    },

    handleMessage: (data) => {
        switch (data.type) {
            case 'STATE':
                set({
                    tick: data.tick,
                    running: data.running,
                    ants: data.individuals || [],
                    colonies: data.colonies && data.colonies.length > 0 ? data.colonies : get().colonies,
                    foodSources: data.foodSources || get().foodSources,
                    predators: data.predators || get().predators,
                    stats: {
                        totalPopulation: data.individuals?.length || 0,
                        totalWorkers: data.individuals?.filter(a => a.caste === 'WORKER').length || 0,
                        totalSoldiers: data.individuals?.filter(a => a.caste === 'SOLDIER').length || 0,
                        totalFood: data.colonies?.reduce((sum, c) => sum + (c.foodStored || 0), 0) || 0,
                    },
                })
                break
            case 'UPDATE':
                if (data.individuals) {
                    set(state => ({
                        ants: state.ants.map(ant => {
                            const update = data.individuals.find(u => u.id === ant.id)
                            return update ? { ...ant, ...update } : ant
                        }),
                    }))
                }
                if (data.tick) set({ tick: data.tick })
                if (data.environment) set({ environment: data.environment })
                if (data.nests) set({ nests: data.nests })
                break
            default:
                console.log('Unknown message:', data)
        }
    },

    resetSimulation: (sessionData) => {
        const { localTickInterval } = get()
        if (localTickInterval) clearInterval(localTickInterval)
        
        set({
            tick: 0,
            running: false,
            localTickInterval: null,
            ants: [],
            interventionsLog: [],
        })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: '🔄 Simulation réinitialisée à Tick #0. Presets & seed synchronisés.'
        })
    },

    // Standalone Tick Step Engine for local mode with dense events
    stepSimulationTick: () => {
        const state = get()
        const nextTick = state.tick + 1

        // Dense Simulation Event Generation per tick
        const rand = Math.random()

        if (nextTick % 1 === 0) { // Log every tick in VERBOSE (Dense) level
            const workerId = Math.floor(Math.random() * 120) + 1
            const colonyName = state.colonies[Math.floor(Math.random() * state.colonies.length)]?.name || 'Colonie #1'
            
            if (rand < 0.30) {
                const foodAmt = (0.2 + Math.random() * 0.8).toFixed(2)
                state.addEventLog({
                    level: 'VERBOSE',
                    category: 'FORAGING',
                    message: `🐜 [Tick #${nextTick}] Ouvrière #${workerId} (${colonyName}): Récolte de ${foodAmt}mg de miellat à (X:${(Math.random()*2).toFixed(2)}m, Y:${(Math.random()*2).toFixed(2)}m)`
                })
            } else if (rand < 0.55) {
                const depthMm = (1.5 + Math.random() * 3.0).toFixed(1)
                state.addEventLog({
                    level: 'VERBOSE',
                    category: 'DIGGING',
                    message: `⛏️ [Tick #${nextTick}] Excavation: Tunnel N°${Math.floor(Math.random()*4)+1} approfondi de ${depthMm}mm (Substrat argileux compaction ${state.simulationParams.resources ? 65 : 50}%)`
                })
            } else if (rand < 0.75) {
                const pheroInt = (40 + Math.random() * 55).toFixed(0)
                state.addEventLog({
                    level: 'DEBUG',
                    category: 'PHEROMONE',
                    message: `🧪 [Tick #${nextTick}] Piste d'attraction déposée avec intensité ${pheroInt}% par patrouilleur`
                })
            } else if (rand < 0.90) {
                const eggCount = Math.floor(Math.random() * 3) + 1
                state.addEventLog({
                    level: 'INFO',
                    category: 'COLONY',
                    message: `👑 [Tick #${nextTick}] Reine (${colonyName}): Ponte de ${eggCount} nouveaux œufs dans la chambre royale`
                })
            } else {
                state.addEventLog({
                    level: 'DEBUG',
                    category: 'METABOLISM',
                    message: `💼 [Tick #${nextTick}] Consommation métabolique: -${(0.12 * state.colonies.length).toFixed(2)}mW consommés par la colonie`
                })
            }
        }

        // Random Stats update
        const updatedStats = {
            totalPopulation: 210 + Math.floor(nextTick * 0.1),
            totalWorkers: 150 + Math.floor(nextTick * 0.08),
            totalSoldiers: 60 + Math.floor(nextTick * 0.02),
            totalFood: Math.max(10, 430 + Math.floor(Math.sin(nextTick * 0.1) * 20)),
        }

        set({ tick: nextTick, stats: updatedStats })
    },

    // Control actions
    play: () => {
        const { ws, localTickInterval, speed } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PLAY' }))

        if (localTickInterval) clearInterval(localTickInterval)

        const intervalMs = Math.max(50, Math.floor(200 / (speed || 1.0)))
        const newInterval = setInterval(() => {
            if (get().running) {
                get().stepSimulationTick()
            }
        }, intervalMs)

        set({ running: true, localTickInterval: newInterval })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: `▶ LANCEMENT DE SIMULATION (Tick #${get().tick}) - Génération d'événements bas niveau active.`
        })
    },

    pause: () => {
        const { ws, localTickInterval } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PAUSE' }))
        if (localTickInterval) clearInterval(localTickInterval)

        set({ running: false, localTickInterval: null })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: `⏸️ SIMULATION INTERROMPUE / PAUSE (Tick #${get().tick}).`
        })
    },

    setSpeed: (newSpeed) => {
        const { ws, running } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'SPEED', speed: newSpeed }))
        set({ speed: newSpeed })

        if (running) {
            get().play() // restart interval with new speed
        }

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: `⚡ Vitesse de simulation ajustée à ${newSpeed.toFixed(1)}x`
        })
    },
}))
