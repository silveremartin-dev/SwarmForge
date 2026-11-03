import { showToast } from './toastStore'

/**
 * Creates Audio and Multiplayer Lobby state handlers for useSimulationStore.
 *
 * @param {Function} set Zustand setter
 * @param {Function} get Zustand getter
 * @param {object} networkClient Network client RPC singleton
 * @param {object} soundEngine Procedural WebAudio sound engine singleton
 * @returns {object} Action handlers for lobby and audio configuration
 */
export function createAudioAndLobbyActions(set, get, networkClient, soundEngine) {
    return {
        // --- Server Connection & Execution Topologies (1:1 with JavaFX) ---
        executionMode: 'REMOTE_CLIENT_SERVER',
        serverHost: 'localhost',
        serverPort: 50051,
        connected: false,
        playerAlias: 'Participant_1',
        playerSpecies: 'Black Garden Ant (Lasius niger)',
        serverRole: 'JOIN',
        serverStatusText: 'Offline',

        // Server Scenario Browser & Matchmaking Lobby State
        serverScenarios: [],
        lobbyStatus: 'LOBBY_WAITING',
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
        }
    }
}
