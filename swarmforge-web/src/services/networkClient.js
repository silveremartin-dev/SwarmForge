/**
 * SwarmForge Network Client Service
 * Manages WebSocket / gRPC communication between SwarmForge Web Thin Client and SwarmForge Server.
 * 
 * Supports:
 * - Real-time streaming subscription (SimulationUpdate frames)
 * - Scenario deployment (Client -> Server: Host Mode)
 * - Session joining & colony assignment (Server -> Client: Join Mode)
 * - Remote VCR control commands (Play, Pause, Step, Speed)
 * - God Mode remote interventions & event scheduling
 */

class SwarmForgeNetworkClient {
    constructor() {
        this.socket = null
        this.isConnected = false
        this.host = 'localhost'
        this.port = 8081
        this.grpcPort = 50051
        this.simulationId = 'main'
        this.participantTag = 'Participant_1'
        this.participantSpecies = 'Formica fusca'
        this.serverRole = 'HOST' // 'HOST' | 'JOIN'
        
        this.pingMs = 0
        this.lastPingTimestamp = 0
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectTimer = null
        this.pingTimer = null
        
        this.onStateChangeCallbacks = new Set()
        this.onSimulationUpdateCallbacks = new Set()
        this.onEventLogCallbacks = new Set()
        this.onScenarioReceivedCallbacks = new Set()
        this.onServerScenariosCallbacks = new Set()
        this.onLobbyStateCallbacks = new Set()
    }

    /**
     * Subscribe to connection state changes
     */
    onStateChange(callback) {
        this.onStateChangeCallbacks.add(callback)
        return () => this.onStateChangeCallbacks.delete(callback)
    }

    /**
     * Subscribe to SimulationUpdate frames
     */
    onSimulationUpdate(callback) {
        this.onSimulationUpdateCallbacks.add(callback)
        return () => this.onSimulationUpdateCallbacks.delete(callback)
    }

    /**
     * Subscribe to server event logs
     */
    onEventLog(callback) {
        this.onEventLogCallbacks.add(callback)
        return () => this.onEventLogCallbacks.delete(callback)
    }

    /**
     * Subscribe to scenario received from server (Join mode)
     */
    onScenarioReceived(callback) {
        this.onScenarioReceivedCallbacks.add(callback)
        return () => this.onScenarioReceivedCallbacks.delete(callback)
    }

    /**
     * Subscribe to server scenarios list
     */
    onServerScenariosReceived(callback) {
        this.onServerScenariosCallbacks.add(callback)
        return () => this.onServerScenariosCallbacks.delete(callback)
    }

    /**
     * Subscribe to server lobby state updates
     */
    onLobbyStateReceived(callback) {
        this.onLobbyStateCallbacks.add(callback)
        return () => this.onLobbyStateCallbacks.delete(callback)
    }

    notifyStateChange(state) {
        this.onStateChangeCallbacks.forEach(cb => {
            try { cb(state) } catch (e) { console.error('Network state callback error:', e) }
        })
    }

    notifyServerScenarios(scenarios) {
        this.onServerScenariosCallbacks.forEach(cb => {
            try { cb(scenarios) } catch (e) { console.error('ServerScenarios callback error:', e) }
        })
    }

    notifyLobbyState(lobbyState) {
        this.onLobbyStateCallbacks.forEach(cb => {
            try { cb(lobbyState) } catch (e) { console.error('LobbyState callback error:', e) }
        })
    }

    notifySimulationUpdate(update) {
        this.onSimulationUpdateCallbacks.forEach(cb => {
            try { cb(update) } catch (e) { console.error('SimulationUpdate callback error:', e) }
        })
    }

    notifyEventLog(eventLog) {
        this.onEventLogCallbacks.forEach(cb => {
            try { cb(eventLog) } catch (e) { console.error('EventLog callback error:', e) }
        })
    }

    notifyScenarioReceived(scenario) {
        this.onScenarioReceivedCallbacks.forEach(cb => {
            try { cb(scenario) } catch (e) { console.error('ScenarioReceived callback error:', e) }
        })
    }

    /**
     * Connect to SwarmForge Server
     */
    connect(host = 'localhost', port = 50051, participantTag = 'Participant_1', species = 'Formica fusca', role = 'HOST') {
        this.host = host || 'localhost'
        this.grpcPort = Number(port) || 50051
        // SwarmForge WebSocket endpoint runs on 8081 when gRPC is on 50051
        this.port = this.grpcPort === 50051 ? 8081 : this.grpcPort
        this.participantTag = participantTag
        this.participantSpecies = species
        this.serverRole = role

        if (this.socket) {
            this.disconnect()
        }

        const wsUrl = `ws://${this.host}:${this.port}`
        this.notifyStateChange({ status: 'CONNECTING', text: `⟳ Connexion à ${wsUrl}...`, isConnected: false })

        try {
            this.socket = new WebSocket(wsUrl)

            this.socket.onopen = () => {
                this.isConnected = true
                this.reconnectAttempts = 0
                this.notifyStateChange({
                    status: 'CONNECTED',
                    text: `● Connecté (${this.host}:${this.grpcPort})`,
                    isConnected: true,
                    host: this.host,
                    port: this.grpcPort
                })

                // Send Subscription / Handshake
                this.sendRaw({
                    type: 'SUBSCRIBE',
                    simulationId: this.simulationId,
                    participantTag: this.participantTag,
                    species: this.participantSpecies,
                    role: this.serverRole,
                    clientType: 'LIGHT_WEB_VIEWER'
                })

                this.startPingMonitor()

                this.notifyEventLog({
                    severity: 'INFO',
                    type: 'SYSTEM',
                    source: 'SwarmForge Server',
                    sourceKey: 'sourceEngine',
                    messageKey: 'evt_SERVER_CONNECTED',
                    message: `Liaison établie avec le serveur SwarmForge (${this.host}:${this.grpcPort}). Mode : ${this.serverRole}.`,
                    metadata: { host: this.host, port: this.grpcPort, role: this.serverRole }
                })
            }

            this.socket.onmessage = (event) => {
                this.handleIncomingMessage(event.data)
            }

            this.socket.onclose = (event) => {
                this.isConnected = false
                this.stopPingMonitor()
                this.notifyStateChange({
                    status: 'OFFLINE',
                    text: 'Hors ligne',
                    isConnected: false
                })
            }

            this.socket.onerror = (err) => {
                console.warn('SwarmForge WebSocket error:', err)
                this.notifyStateChange({
                    status: 'ERROR',
                    text: `Erreur de connexion (${this.host}:${this.grpcPort})`,
                    isConnected: false
                })
            }
        } catch (error) {
            console.error('Failed to establish WebSocket connection:', error)
            this.notifyStateChange({
                status: 'ERROR',
                text: 'Échec de connexion réseau',
                isConnected: false
            })
        }
    }

    /**
     * Disconnect from server
     */
    disconnect() {
        this.stopPingMonitor()
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
            this.reconnectTimer = null
        }
        if (this.socket) {
            try {
                this.socket.close()
            } catch (e) {
                // ignore
            }
            this.socket = null
        }
        this.isConnected = false
        this.notifyStateChange({
            status: 'OFFLINE',
            text: 'Hors ligne',
            isConnected: false
        })
    }

    /**
     * Parse incoming messages from SwarmForge Server
     */
    handleIncomingMessage(data) {
        if (!data) return
        try {
            const msg = JSON.parse(data)

            // 1. Welcome / Handshake
            if (msg.type === 'WELCOME') {
                this.notifyEventLog({
                    severity: 'INFO',
                    type: 'SYSTEM',
                    source: 'SwarmForge Server',
                    sourceKey: 'sourceEngine',
                    message: msg.message || 'Bienvenue sur SwarmForge Server'
                })
                return
            }

            // 2. Heartbeat Ping / Pong
            if (msg.type === 'PONG') {
                if (this.lastPingTimestamp > 0) {
                    this.pingMs = Math.max(1, Math.round(performance.now() - this.lastPingTimestamp))
                    this.notifyStateChange({ pingMs: this.pingMs })
                }
                return
            }

            // 3. Scenario state deployed from server (Join Mode)
            if (msg.type === 'SCENARIO_STATE' || msg.scenario) {
                this.notifyScenarioReceived(msg.scenario || msg)
                return
            }

            // 4. Server Scenarios Catalog (LOBBY_WAITING, ACTIVE, INACTIVE)
            if (msg.type === 'SERVER_SCENARIOS_LIST' || msg.scenarios) {
                this.notifyServerScenarios(msg.scenarios || [])
                return
            }

            // 5. Lobby & Matchmaking state updates
            if (msg.type === 'LOBBY_STATE') {
                this.notifyLobbyState(msg)
                return
            }

            // 6. Server Event Log item
            if (msg.type === 'EVENT_LOG' || msg.eventLog) {
                this.notifyEventLog(msg.eventLog || msg)
                return
            }

            // 7. Protobuf JSON SimulationUpdate frame (broadcasted at 20-60 Hz)
            if (msg.tick !== undefined || msg.individuals || msg.cells || msg.environment || msg.nests) {
                this.notifySimulationUpdate(msg)
            }
        } catch (e) {
            // Not JSON or partial binary packet
            console.warn('Network message parsing exception:', e)
        }
    }

    /**
     * Send raw JSON packet to server
     */
    sendRaw(payload) {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
            return false
        }
        try {
            this.socket.send(JSON.stringify(payload))
            return true
        } catch (e) {
            console.error('Failed to send packet to SwarmForge Server:', e)
            return false
        }
    }

    /**
     * Deploy Scenario (Client -> Server: Host Mode)
     * Packages the complete ecosystem parameters and transmits them to SwarmForge Server.
     */
    deployScenario(scenarioData) {
        const payload = {
            type: 'DEPLOY_SCENARIO',
            simulationId: this.simulationId,
            participantTag: this.participantTag,
            timestamp: Date.now(),
            scenario: {
                worldPresetId: scenarioData.selectedWorldPresetId,
                weatherPresetId: scenarioData.selectedWeatherPresetId,
                startDateTime: scenarioData.startDateTime,
                masterSeed: scenarioData.masterSeed,
                stepSeconds: scenarioData.stepSeconds,
                maxDurationSeconds: scenarioData.maxDurationSeconds,
                minPopulationStop: scenarioData.minPopulationStop,
                speciesCards: scenarioData.speciesCards,
                colonies: scenarioData.colonies,
                nests: scenarioData.nests
            }
        }

        const sent = this.sendRaw(payload)
        if (sent) {
            this.notifyEventLog({
                severity: 'INFO',
                type: 'SYSTEM',
                source: 'Client Léger',
                sourceKey: 'sourceScenario',
                messageKey: 'evt_SCENARIO_DEPLOYED_TO_SERVER',
                message: `Scénario maître déployé avec succès sur le serveur (${scenarioData.speciesCards?.length || 0} espèces, graine ${scenarioData.masterSeed}).`,
                metadata: { seed: scenarioData.masterSeed, speciesCount: scenarioData.speciesCards?.length }
            })
        }
        return sent
    }

    /**
     * Join Simulation Session (Server -> Client: Join Mode)
     */
    joinSession(participantTag, species) {
        this.participantTag = participantTag
        this.participantSpecies = species
        this.serverRole = 'JOIN'

        return this.sendRaw({
            type: 'JOIN_SESSION',
            simulationId: this.simulationId,
            participantTag: this.participantTag,
            species: this.participantSpecies
        })
    }

    /**
     * Send Simulation Control Commands (Play, Pause, Step, Speed)
     */
    sendControlCommand(action, value = null) {
        return this.sendRaw({
            type: 'CONTROL_COMMAND',
            simulationId: this.simulationId,
            action: action, // 'PLAY' | 'PAUSE' | 'STEP' | 'SET_SPEED' | 'RESET'
            value: value,
            timestamp: Date.now()
        })
    }

    /**
     * Send God Mode Intervention to Server
     */
    sendGodModeIntervention(intervention) {
        return this.sendRaw({
            type: 'GOD_MODE_INTERVENTION',
            simulationId: this.simulationId,
            intervention: intervention,
            timestamp: Date.now()
        })
    }

    /**
     * Schedule Future Intervention on Server
     */
    sendScheduledEvent(event) {
        return this.sendRaw({
            type: 'SCHEDULE_EVENT',
            simulationId: this.simulationId,
            scheduledEvent: event,
            timestamp: Date.now()
        })
    }

    /**
     * Request list of scenarios known by the server
     */
    requestServerScenarios() {
        return this.sendRaw({
            type: 'LIST_SERVER_SCENARIOS',
            simulationId: this.simulationId,
            timestamp: Date.now()
        })
    }

    /**
     * Toggle player ready state in matchmaking lobby
     */
    setPlayerReady(ready = true) {
        return this.sendRaw({
            type: 'PLAYER_READY',
            simulationId: this.simulationId,
            participantTag: this.participantTag,
            ready: ready,
            timestamp: Date.now()
        })
    }

    /**
     * Start the match on the server (Host / Admin trigger)
     */
    startMatch() {
        return this.sendRaw({
            type: 'START_MATCH',
            simulationId: this.simulationId,
            timestamp: Date.now()
        })
    }

    /**
     * Select a scenario from the server's known catalog (Lobby mode)
     */
    selectServerScenario(scenarioId) {
        return this.sendRaw({
            type: 'SELECT_SCENARIO',
            simulationId: this.simulationId,
            scenarioId: scenarioId,
            timestamp: Date.now()
        })
    }

    startPingMonitor() {
        this.stopPingMonitor()
        this.pingTimer = setInterval(() => {
            if (this.isConnected) {
                this.lastPingTimestamp = performance.now()
                this.sendRaw({ type: 'PING', timestamp: Date.now() })
            }
        }, 5000)
    }

    stopPingMonitor() {
        if (this.pingTimer) {
            clearInterval(this.pingTimer)
            this.pingTimer = null
        }
    }
}

export const networkClient = new SwarmForgeNetworkClient()
