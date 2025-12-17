import { create } from 'zustand'

export const useSimulationStore = create((set, get) => ({
    // Connection state
    connected: false,
    ws: null,

    // Simulation state
    tick: 0,
    running: false,
    speed: 1.0,

    // Entity data
    colonies: [],
    ants: [],
    predators: [],
    foodSources: [],
    nests: [],

    // Statistics
    stats: {
        totalPopulation: 0,
        totalWorkers: 0,
        totalSoldiers: 0,
        totalFood: 0,
    },

    // Environment
    environment: {
        lightLevel: 1.0,
        timeOfDay: 'DAY',
        sunAngle: 0.5,
        temperature: 20,
        humidity: 50,
        rainIntensity: 0,
        windSpeed: 0,
        season: 'SPRING',
    },

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
                    colonies: data.colonies || [],
                    foodSources: data.foodSources || [],
                    predators: data.predators || [],
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
