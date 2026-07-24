import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useSimulationStore } from './simulationStore'

describe('useSimulationStore Zustand Store', () => {
    beforeEach(() => {
        useSimulationStore.setState({
            connected: false,
            ws: null,
            tick: 0,
            running: false,
            speed: 1.0,
            colonies: [],
            ants: [],
            predators: [],
            foodSources: [],
            nests: [],
            stats: {
                totalPopulation: 0,
                totalWorkers: 0,
                totalSoldiers: 0,
                totalFood: 0,
            },
            selectedEntity: null,
        })
    })

    it('should initialize with default state', () => {
        const state = useSimulationStore.getState()
        expect(state.connected).toBe(false)
        expect(state.tick).toBe(0)
        expect(state.running).toBe(false)
        expect(state.speed).toBe(1.0)
    })

    it('should update selectedEntity on setSelectedEntity call', () => {
        const mockEntity = { id: 'ant_1', caste: 'WORKER', health: 100 }
        useSimulationStore.getState().setSelectedEntity(mockEntity)
        expect(useSimulationStore.getState().selectedEntity).toEqual(mockEntity)
    })

    it('should correctly process STATE message', () => {
        const mockMessage = {
            type: 'STATE',
            tick: 1500,
            running: true,
            individuals: [
                { id: 'ant_1', caste: 'WORKER' },
                { id: 'ant_2', caste: 'SOLDIER' },
            ],
            colonies: [{ id: 'colony_1', foodStored: 450 }],
            foodSources: [{ id: 'food_1', amount: 200 }],
        }

        useSimulationStore.getState().handleMessage(mockMessage)

        const state = useSimulationStore.getState()
        expect(state.tick).toBe(1500)
        expect(state.running).toBe(true)
        expect(state.ants).toHaveLength(2)
        expect(state.stats.totalPopulation).toBe(2)
        expect(state.stats.totalWorkers).toBe(1)
        expect(state.stats.totalSoldiers).toBe(1)
        expect(state.stats.totalFood).toBe(450)
    })

    it('should process UPDATE delta message', () => {
        useSimulationStore.setState({
            ants: [{ id: 'ant_1', caste: 'WORKER', x: 10, y: 10 }],
            tick: 100,
        })

        const mockDeltaMessage = {
            type: 'UPDATE',
            tick: 101,
            individuals: [{ id: 'ant_1', x: 12, y: 14 }],
        }

        useSimulationStore.getState().handleMessage(mockDeltaMessage)

        const state = useSimulationStore.getState()
        expect(state.tick).toBe(101)
        expect(state.ants[0]).toEqual({ id: 'ant_1', caste: 'WORKER', x: 12, y: 14 })
    })

    it('should handle play, pause and speed control actions', () => {
        const mockWs = { send: vi.fn() }
        useSimulationStore.setState({ ws: mockWs })

        useSimulationStore.getState().play()
        expect(useSimulationStore.getState().running).toBe(true)
        expect(mockWs.send).toHaveBeenCalledWith(JSON.stringify({ type: 'CONTROL', action: 'PLAY' }))

        useSimulationStore.getState().pause()
        expect(useSimulationStore.getState().running).toBe(false)
        expect(mockWs.send).toHaveBeenCalledWith(JSON.stringify({ type: 'CONTROL', action: 'PAUSE' }))

        useSimulationStore.getState().setSpeed(2.5)
        expect(useSimulationStore.getState().speed).toBe(2.5)
        expect(mockWs.send).toHaveBeenCalledWith(JSON.stringify({ type: 'CONTROL', action: 'SPEED', speed: 2.5 }))
    })
})
