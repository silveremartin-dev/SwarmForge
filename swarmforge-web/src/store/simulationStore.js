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
        { id: 'COLONY_1', name: 'Colonie #1 (Indigène)', species: 'Formica fusca', color: '#38bdf8', foodStored: 250, queenCount: 1, workerCount: 120 },
        { id: 'COLONY_2', name: 'Colonie #2 (Rivale)', species: 'Linepithema humile', color: '#f43f5e', foodStored: 180, queenCount: 2, workerCount: 90 },
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
        return { colonies: updated }
    }),

    removeColony: (colonyId) => set(state => ({
        colonies: state.colonies.filter(c => c.id !== colonyId)
    })),

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

        return {
            interventionsLog: [intervention, ...state.interventionsLog],
            foodSources: newFood,
            predators: newPredators
        }
    }),

    clearInterventionsLog: () => set({ interventionsLog: [] }),

    // Statistics
    stats: {
        totalPopulation: 0,
        totalWorkers: 0,
        totalSoldiers: 0,
        totalFood: 0,
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
        windSpeed: 0,
        weatherState: 'CLEAR', // 'CLEAR', 'CLOUDY', 'THUNDERSTORM', 'SNOW', 'BLIZZARD', 'TEMPEST', 'HAIL', 'FOG'
    },

    updateEnvironment: (newEnv) => set(state => ({
        environment: { ...state.environment, ...newEnv }
    })),

    // Display Toggles for Simulation Mode (Soleil, Éclairs, Nuages, Pluie, Brouillard, Vent)
    weatherToggles: {
        showSun: true,
        showLightning: true,
        showClouds: true,
        showPrecipitation: true,
        showFog: true,
        showWindDust: true,
        lightningTrigger: 0,
    },
    setWeatherToggle: (key, value) => set(state => ({
        weatherToggles: { ...state.weatherToggles, [key]: value }
    })),
    triggerLightning: () => set(state => ({
        weatherToggles: { ...state.weatherToggles, lightningTrigger: state.weatherToggles.lightningTrigger + 1 }
    })),

    // Selection
    selectedEntity: null,
    setSelectedEntity: (entity) => set({ selectedEntity: entity }),

    // Actions
    connect: () => {
        const ws = new WebSocket('ws://localhost:8081')

        ws.onopen = () => {
            console.log('WebSocket connected')
            set({ connected: true, ws })
            // Subscribe to updates
            ws.send(JSON.stringify({ type: 'SUBSCRIBE', viewport: { x: 0, y: 0, width: 100, height: 100 } }))
        }

        ws.onmessage = (event) => {
            const data = JSON.parse(event.data)
            get().handleMessage(data)
        }

        ws.onclose = () => {
            console.log('WebSocket disconnected')
            set({ connected: false, ws: null })
            // Reconnect after 3 seconds
            setTimeout(() => get().connect(), 3000)
        }

        ws.onerror = (err) => {
            console.error('WebSocket error:', err)
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
                // Delta updates
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
        set({
            tick: 0,
            ants: [],
            interventionsLog: [],
        })
    },

    // Control actions
    play: () => {
        const { ws } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PLAY' }))
        set({ running: true })
    },

    pause: () => {
        const { ws } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PAUSE' }))
        set({ running: false })
    },

    setSpeed: (speed) => {
        const { ws } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'SPEED', speed }))
        set({ speed })
    },
}))
