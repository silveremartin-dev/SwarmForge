/**
 * Utility functions for exporting and importing SwarmForge simulation artifacts
 * (Scenarios, Statistical CSV series, and Event Log dumps).
 */

/**
 * Exports current scenario configuration as a formatted JSON file download.
 * @param {Object} scenarioData
 */
export function downloadScenarioJson(scenarioData) {
    const blob = new Blob([JSON.stringify(scenarioData, null, 2)], { type: 'application/json' })
    triggerFileDownload(blob, `swarmforge_scenario_${Date.now()}.json`)
}

/**
 * Parses and validates an imported scenario JSON string.
 * @param {string} jsonString
 * @returns {Object|null}
 */
export function parseScenarioJson(jsonString) {
    try {
        const data = JSON.parse(jsonString)
        return {
            scenarioDescription: data.description || '',
            selectedWorldPresetId: data.worldPresetId || 'world_terrarium_01',
            selectedWeatherPresetId: data.weatherPresetId || 'weather_printemps_doux',
            masterSeed: data.masterSeed ?? 12345,
            startDateTime: data.startDateTime || '2026-03-20T08:00:00',
            maxDuration: data.maxDuration ?? 0,
            durationUnit: data.durationUnit || 'Days',
            minPopStop: data.minPopStop ?? 0,
            isMultiplayerOnly: Boolean(data.isMultiplayerOnly),
            requiredPlayerCount: data.requiredPlayerCount ?? 1,
            gridTilesX: data.gridTilesX ?? 1,
            gridTilesY: data.gridTilesY ?? 1,
            speciesCards: data.speciesCards || []
        }
    } catch {
        return null
    }
}

/**
 * Exports simulation metrics history as a standard RFC 4180 CSV file download.
 * @param {Array<Object>} history
 */
export function downloadStatsCsv(history) {
    if (!history || history.length === 0) return
    const headers = ['Tick', 'SimTimeSeconds', 'TotalPopulation', 'Queens', 'Workers', 'Soldiers', 'Males', 'Food', 'Water', 'Protein', 'Foraging', 'Digging', 'Nursing', 'Guarding', 'Resting', 'TempC', 'Rain', 'TPS']
    const rows = history.map(h => [
        h.tick,
        h.simTimeSeconds.toFixed(2),
        h.totalPopulation,
        h.casteBreakdown?.queens ?? 0,
        h.casteBreakdown?.workers ?? 0,
        h.casteBreakdown?.soldiers ?? 0,
        h.casteBreakdown?.males ?? 0,
        h.resources?.food?.toFixed(1) ?? 0,
        h.resources?.water?.toFixed(1) ?? 0,
        h.resources?.protein?.toFixed(1) ?? 0,
        h.ethology?.foraging ?? 0,
        h.ethology?.digging ?? 0,
        h.ethology?.nursing ?? 0,
        h.ethology?.guarding ?? 0,
        h.ethology?.resting ?? 0,
        h.weather?.temp?.toFixed(1) ?? 0,
        h.weather?.rain?.toFixed(1) ?? 0,
        h.performance?.tps?.toFixed(1) ?? 0
    ].join(','))

    const csvContent = [headers.join(','), ...rows].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    triggerFileDownload(blob, `swarmforge_statistics_${Date.now()}.csv`)
}

/**
 * Exports simulation event logs as CSV file download.
 * @param {Array<Object>} logs
 */
export function downloadLogsCsv(logs) {
    if (!logs || logs.length === 0) return
    const headers = ['Id', 'Timestamp', 'Tick', 'Severity', 'Type', 'Source', 'Message']
    const rows = logs.map(l => [
        l.id,
        `"${l.timestamp}"`,
        l.tick,
        l.severity,
        l.type,
        `"${l.source || ''}"`,
        `"${(l.message || '').replace(/"/g, '""')}"`
    ].join(','))

    const csvContent = [headers.join(','), ...rows].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    triggerFileDownload(blob, `swarmforge_event_logs_${Date.now()}.csv`)
}

/**
 * Exports simulation event logs as JSON file download.
 * @param {Array<Object>} logs
 */
export function downloadLogsJson(logs) {
    const blob = new Blob([JSON.stringify(logs, null, 2)], { type: 'application/json' })
    triggerFileDownload(blob, `swarmforge_event_logs_${Date.now()}.json`)
}

function triggerFileDownload(blob, filename) {
    if (typeof document === 'undefined') return
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
}
