import { formatSimCalendarTime, formatSimRelativeTime } from '../utils/timeUtils'

/**
 * Reactive Network Client Event Binding
 * Binds incoming gRPC/WebSocket updates from networkClient to the Zustand simulation store.
 */
export function bindNetworkToStore(useSimulationStore, networkClient) {
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
}
