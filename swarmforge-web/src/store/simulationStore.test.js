import { describe, it, expect, beforeEach } from 'vitest'
import { useSimulationStore } from './simulationStore'

describe('useSimulationStore Zustand Store', () => {
    beforeEach(() => {
        useSimulationStore.getState().goToBeginning()
    })

    it('should initialize with default state', () => {
        const state = useSimulationStore.getState()
        expect(state.connected).toBe(false)
        expect(state.ticks).toBe(0)
        expect(state.running).toBe(false)
        expect(state.speed).toBe(1.0)
        expect(state.stepSeconds).toBe(0.05)
        expect(state.speciesCards.length).toBeGreaterThan(0)
    })

    it('should handle play, pause and speed control actions', () => {
        useSimulationStore.getState().play()
        expect(useSimulationStore.getState().running).toBe(true)

        useSimulationStore.getState().pause()
        expect(useSimulationStore.getState().running).toBe(false)

        useSimulationStore.getState().setSpeed(2.5)
        expect(useSimulationStore.getState().speed).toBe(2.5)
    })

    it('should advance ticks with stepTick', () => {
        expect(useSimulationStore.getState().ticks).toBe(0)
        useSimulationStore.getState().stepTick()
        expect(useSimulationStore.getState().ticks).toBe(1)
        expect(useSimulationStore.getState().simTimeSeconds).toBeCloseTo(0.05)
    })

    it('should add and manage scheduled events for God Mode', () => {
        const initialCount = useSimulationStore.getState().scheduledEvents.length
        useSimulationStore.getState().addScheduledEvent({
            targetTick: 50,
            category: 'RESOURCE',
            eventType: 'Dépôt Sucre Test',
            action: 'SPAWN_FOOD',
            amount: 100
        })

        const events = useSimulationStore.getState().scheduledEvents
        expect(events.length).toBe(initialCount + 1)
        const added = events.find(e => e.eventType === 'Dépôt Sucre Test')
        expect(added).toBeDefined()
        expect(added.targetTick).toBe(50)
    })

    it('should add and filter event logs', () => {
        useSimulationStore.getState().addEventLog({
            severity: 'WARNING',
            type: 'DISASTER',
            source: 'Test Disaster',
            message: 'Incendie simulé'
        })

        const logs = useSimulationStore.getState().eventsLog
        expect(logs.length).toBeGreaterThan(0)
        const latest = logs[0]
        expect(latest.severity).toBe('WARNING')
        expect(latest.type).toBe('DISASTER')
        expect(latest.message).toBe('Incendie simulé')
    })
})
