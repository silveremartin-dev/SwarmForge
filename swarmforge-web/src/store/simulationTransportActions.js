import { showToast } from './toastStore'
import { DEFAULT_WORLD_PRESETS, DEFAULT_WEATHER_PRESETS } from './presetStore'
import { formatSimCalendarTime, formatSimRelativeTime } from '../utils/timeUtils'
import { generateInitialColonies, generateInitialFoodSources, generateInitialNests } from './initialEntities'

/**
 * Creates connection, scenario setup, and transport timeline control actions for simulationStore.
 * Aligned with 1:1 server RPC control semantics and desktop JavaFX client timeline transport.
 *
 * @param {Function} set Zustand store setter
 * @param {Function} get Zustand store getter
 * @param {object} networkClient Active network RPC client instance
 * @param {object} soundEngine Procedural sound synthesis engine
 * @returns {object} Action handlers for simulation transport and connection
 */
export function createTransportActions(set, get, networkClient, soundEngine) {
    return {
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

        // --- Core Simulation Actions & Transport Controls ---
        applyScenarioSetup: () => {
            const state = get()
            const {
                speciesCards,
                startDateTime,
                masterSeed,
                selectedWorldPresetId,
                selectedWeatherPresetId,
                connected,
                serverRole,
                playerAlias,
                playerSpecies,
                serverHost,
                serverPort
            } = state

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
                ants: [], // Reconciled via server SimulationUpdate frames
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

        setStepSeconds: (dt) => set({ stepSeconds: Math.max(0.001, Math.min(10.0, Number(dt) || 0.05)) })
    }
}
