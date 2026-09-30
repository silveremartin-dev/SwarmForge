import { describe, it, expect, beforeEach } from 'vitest'
import { useSimulationStore } from './simulationStore'
import { networkClient } from '../services/networkClient'

describe('useSimulationStore Zustand Store & Real Data Sync', () => {
    beforeEach(() => {
        useSimulationStore.setState({
            ticks: 0,
            simTimeSeconds: 0,
            running: false,
            colonies: [],
            ants: [],
            foodSources: [],
            predators: [],
            nests: [],
            speciesCards: [],
            scheduledEvents: [],
            lobbyPlayers: [],
            serverScenarios: []
        })
    })

    it('should initialize with strictly empty entities and NO mock fallbacks', () => {
        const state = useSimulationStore.getState()
        expect(state.connected).toBe(false)
        expect(state.ticks).toBe(0)
        expect(state.running).toBe(false)
        expect(state.speed).toBe(1.0)
        expect(state.stepSeconds).toBe(0.05)
        expect(state.speciesCards).toEqual([])
        expect(state.colonies).toEqual([])
        expect(state.ants).toEqual([])
        expect(state.foodSources).toEqual([])
        expect(state.predators).toEqual([])
        expect(state.scheduledEvents).toEqual([])
    })

    it('should accurately parse real simulation updates from server (individuals, food, predators, env)', () => {
        // Simulate a real protobuf JSON broadcast frame from SwarmForgeWebSocketServer
        const mockServerFrame = {
            tick: 42,
            simTimeSeconds: 2.1,
            individuals: [
                {
                    id: 'ind_001',
                    colonyId: 'col_alpha',
                    species: 'Formica fusca',
                    caste: 'SOLDIER',
                    job: 'GUARD',
                    position: { x: 38.5, y: 0.15, z: 42.0 },
                    heading: 1.57,
                    speedMms: 22.5,
                    health: 95.0,
                    energy: 88.0,
                    alive: true
                },
                {
                    id: 'ind_002',
                    colonyId: 'col_alpha',
                    species: 'Formica fusca',
                    caste: 'WORKER',
                    job: 'FORAGER',
                    position: { x: 55.0, y: 0.1, z: 60.2 },
                    heading: 0.0,
                    speedMms: 26.0,
                    health: 100.0,
                    energy: 92.0,
                    alive: true
                }
            ],
            food: [
                {
                    id: 'food_pollen_1',
                    position: { x: 56.0, y: 0.2, z: 61.0 },
                    quantity: 450.0,
                    type: 'SUGAR'
                }
            ],
            predators: [
                {
                    id: 'pred_spider_1',
                    type: 'SPIDER',
                    x: 70.0,
                    y: 0.3,
                    z: 75.0,
                    state: 'AMBUSH',
                    health: 100
                }
            ],
            environment: {
                temperature: 24.5,
                humidity: 70.0,
                windSpeed: 2.0,
                weatherState: 'CLEAR',
                season: 'SPRING',
                lightLevel: 0.95
            }
        }

        // Notify subscribers via networkClient callback
        networkClient.notifySimulationUpdate(mockServerFrame)

        const state = useSimulationStore.getState()
        expect(state.ticks).toBe(42)
        expect(state.simTimeSeconds).toBeCloseTo(2.1)
        expect(state.ants.length).toBe(2)

        // Validate Individual 1
        const ant1 = state.ants.find(a => a.id === 'ind_001')
        expect(ant1).toBeDefined()
        expect(ant1.caste).toBe('SOLDIER')
        expect(ant1.job).toBe('GUARD')
        expect(ant1.x).toBeCloseTo(38.5)
        expect(ant1.z).toBeCloseTo(42.0)
        expect(ant1.health).toBe(95.0)

        // Validate Food Source
        expect(state.foodSources.length).toBe(1)
        expect(state.foodSources[0].x).toBeCloseTo(56.0)
        expect(state.foodSources[0].z).toBeCloseTo(61.0)
        expect(state.foodSources[0].quantity).toBe(450.0)
        expect(state.foodSources[0].type).toBe('SUGAR')

        // Validate Predator
        expect(state.predators.length).toBe(1)
        expect(state.predators[0].type).toBe('SPIDER')
        expect(state.predators[0].x).toBeCloseTo(70.0)
        expect(state.predators[0].state).toBe('AMBUSH')

        // Validate Environment
        expect(state.environment.temperature).toBeCloseTo(24.5)
        expect(state.environment.humidity).toBeCloseTo(70.0)
    })

    it('should accurately parse real lobby state updates from server', () => {
        const mockLobby = {
            type: 'LOBBY_STATE',
            status: 'LOBBY_WAITING',
            selectedScenarioId: 'MP_01_TERRITORIAL_WAR',
            players: [
                { tag: 'Alice_Host', species: 'Formica fusca', role: 'HOST', isReady: true },
                { tag: 'Bob_Challenger', species: 'Linepithema humile', role: 'JOIN', isReady: true }
            ]
        }

        networkClient.notifyLobbyState(mockLobby)

        const state = useSimulationStore.getState()
        expect(state.lobbyStatus).toBe('LOBBY_WAITING')
        expect(state.selectedServerScenarioId).toBe('MP_01_TERRITORIAL_WAR')
        expect(state.lobbyPlayers.length).toBe(2)
        expect(state.lobbyPlayers[0].tag).toBe('Alice_Host')
        expect(state.lobbyPlayers[1].tag).toBe('Bob_Challenger')
    })

    it('should accurately parse server scenario catalogs', () => {
        const mockCatalog = [
            { id: 'ACAD_01_LEVY_BROWNIAN', title: 'Dispersion de Lévy', academicCategory: 'Academic', requiredPlayerCount: 1 },
            { id: 'MP_01_TERRITORIAL_WAR', title: 'Guerre Territoriale', academicCategory: 'Multiplayer', requiredPlayerCount: 2 }
        ]

        networkClient.notifyServerScenarios(mockCatalog)

        const state = useSimulationStore.getState()
        expect(state.serverScenarios.length).toBe(2)
        expect(state.serverScenarios[0].id).toBe('ACAD_01_LEVY_BROWNIAN')
        expect(state.serverScenarios[1].id).toBe('MP_01_TERRITORIAL_WAR')
    })
})
