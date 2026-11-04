/**
 * SwarmForge Network Message Handler
 * Handles message deserialization and dispatching for SwarmForgeNetworkClient.
 */

export function handleIncomingMessage(client, data) {
    if (!data) return
    try {
        const msg = JSON.parse(data)

        // 1. Welcome / Handshake
        if (msg.type === 'WELCOME') {
            client.notifyEventLog({
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
            if (client.lastPingTimestamp > 0) {
                client.pingMs = Math.max(1, Math.round(performance.now() - client.lastPingTimestamp))
                client.notifyStateChange({ pingMs: client.pingMs })
            }
            return
        }

        // 3. Scenario state deployed from server (Join Mode)
        if (msg.type === 'SCENARIO_STATE' || (msg.type !== 'SCENARIO_DATA' && msg.scenario && !msg.type)) {
            client.notifyScenarioReceived(msg.scenario || msg)
            return
        }

        // 3b. Scenario Data received for export / download
        if (msg.type === 'SCENARIO_DATA') {
            client.notifyScenarioData(msg.scenario || msg)
            return
        }

        // 4. Server Scenarios Catalog (LOBBY_WAITING, ACTIVE, INACTIVE)
        if (msg.type === 'SERVER_SCENARIOS_LIST' || msg.scenarios) {
            client.notifyServerScenarios(msg.scenarios || [])
            return
        }

        // 5. Lobby & Matchmaking state updates
        if (msg.type === 'LOBBY_STATE') {
            client.notifyLobbyState(msg)
            return
        }

        // 6. Server Event Log item
        if (msg.type === 'EVENT_LOG' || msg.eventLog) {
            client.notifyEventLog(msg.eventLog || msg)
            return
        }

        // 7. Protobuf JSON SimulationUpdate frame (broadcasted at 20-60 Hz)
        if (msg.tick !== undefined || msg.individuals || msg.cells || msg.environment || msg.nests) {
            client.notifySimulationUpdate(msg)
        }
    } catch (e) {
        // Not JSON or partial binary packet
        console.warn('Network message parsing exception:', e)
    }
}

export function createScenarioDeployPayload(simulationId, participantTag, scenarioData) {
    return {
        type: 'DEPLOY_SCENARIO',
        simulationId: simulationId,
        participantTag: participantTag,
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
}
