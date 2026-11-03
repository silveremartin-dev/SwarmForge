import { formatSimCalendarTime, formatSimRelativeTime } from '../utils/timeUtils'

export function executeInstantInterventionHelper(get, set, networkClient, intervention) {
    const state = get()
    const { category, type, posX, posY, posZ, count, amount, intensity, colonyId, caste } = intervention

    if (state.connected) {
        networkClient.sendGodModeIntervention(intervention)
    }

    get().addEventLog({
        severity: 'WARNING',
        type: category === 'DISASTER' ? 'DISASTER' : 'SYSTEM',
        source: 'God Mode (Instant)',
        message: `Intervention immédiate : [${category}] ${type || 'Action'} (Pos: ${posX ?? 32}, ${posY ?? 32})`
    })

    if (category === 'ENTITIES') {
        if (intervention.action === 'SPAWN' || type?.includes('Spawn')) {
            const newAnts = []
            const targetColony = state.colonies.find(c => c.id === colonyId) || state.colonies[0]
            for (let i = 0; i < (count || 5); i++) {
                newAnts.push({
                    id: `ant_spawned_${Date.now()}_${i}`,
                    colonyId: targetColony?.id || 'col_1',
                    colonyName: targetColony?.name || 'Colonie',
                    species: targetColony?.speciesId || 'Formica fusca',
                    caste: caste || 'WORKER',
                    job: caste === 'SOLDIER' ? 'GUARD' : (caste === 'QUEEN' ? 'LAYING_EGGS' : 'FORAGER'),
                    x: (posX || 32) + (Math.random() - 0.5) * 3,
                    y: 0.1,
                    z: (posY || 32) + (Math.random() - 0.5) * 3,
                    vx: 0, vy: 0, vz: 0,
                    health: 100,
                    energy: 100,
                    age: 1,
                    carriedItem: 'NONE',
                    task: 'Instant God Mode Injection',
                    color: targetColony?.color || '#38bdf8'
                })
            }
            set({ ants: [...state.ants, ...newAnts] })
        } else if (intervention.action === 'KILL') {
            const toKill = count || 5
            const updatedAnts = state.ants.slice(toKill)
            set({ ants: updatedAnts })
        }
    } else if (category === 'RESOURCE') {
        const newFood = {
            id: `food_${Date.now()}`,
            x: posX || 32,
            y: 0.2,
            z: posY || 32,
            type: type?.includes('Sugar') ? 'SUGAR' : 'SEEDS',
            name: type || 'Dépôt Nourriture',
            amount: amount || 100,
            maxAmount: amount || 100,
            color: '#10b981'
        }
        set({ foodSources: [...state.foodSources, newFood] })
    } else if (category === 'DISASTER') {
        const upper = (type || '').toUpperCase()
        if (upper.includes('RAIN') || upper.includes('FLOOD')) {
            set({
                environment: {
                    ...state.environment,
                    weatherState: 'TEMPEST',
                    humidity: Math.min(100, state.environment.humidity + (intensity || 0.5) * 30),
                    temperature: Math.max(5, state.environment.temperature - 4)
                }
            })
        } else if (upper.includes('HEAT') || upper.includes('FIRE')) {
            set({
                environment: {
                    ...state.environment,
                    weatherState: 'CLEAR',
                    temperature: state.environment.temperature + (intensity || 0.5) * 15,
                    humidity: Math.max(10, state.environment.humidity - 25)
                }
            })
        }
    } else if (category === 'ABIOTIC') {
        set({
            environment: {
                ...state.environment,
                temperature: intervention.tempCelsius ?? state.environment.temperature,
                humidity: intervention.humidityPercent ?? state.environment.humidity,
                windSpeed: intervention.windMetersPerSec ?? state.environment.windSpeed
            }
        })
    } else if (category === 'PHEROMONE') {
        const pheroPoints = []
        const centerXPhero = posX || 50
        const centerZPhero = posY || 50
        const radius = intervention.pheromoneRadius || 10
        const countPhero = Math.min(30, Math.max(5, Math.floor(radius * 2)))
        for (let i = 0; i < countPhero; i++) {
            const angle = Math.random() * Math.PI * 2
            const dist = Math.random() * radius
            pheroPoints.push({
                id: `phero_god_${Date.now()}_${i}`,
                x: Math.max(5, Math.min(95, centerXPhero + Math.cos(angle) * dist)),
                z: Math.max(5, Math.min(95, centerZPhero + Math.sin(angle) * dist)),
                type: intervention.pheromoneType || 'ALARM',
                intensity: Math.min(1.0, (intervention.pheromoneIntensity || 100) / 100),
                colonyId: colonyId || 'ALL',
                createdAtTick: state.ticks
            })
        }
        set({ pheromones: [...(state.pheromones || []), ...pheroPoints].slice(-1000) })
    }
}

export function selectNextAntHelper(get, set) {
    const state = get()
    const ants = (state.ants || []).filter(a => a && (a.health === undefined || a.health > 0))
    if (ants.length === 0) return
    const currentId = state.trackedAntId || state.selectedEntity?.id
    const currentAnt = ants.find(a => a.id === currentId)
    if (!currentAnt) {
        const first = ants[0]
        set({ trackedAntId: first.id, trackedAntData: first, selectedEntity: first })
        return
    }

    const curX = currentAnt.x ?? 50
    const curY = currentAnt.y ?? 0.15
    const curZ = currentAnt.z !== undefined ? currentAnt.z : (currentAnt.y ?? 50)

    const history = [...(state.antNavigationHistory || [])]
    if (currentId && (history.length === 0 || history[history.length - 1] !== currentId)) {
        history.push(currentId)
        if (history.length > 50) history.shift()
    }

    const candidates = ants
        .filter(a => a.id !== currentId)
        .map(a => {
            const ax = a.x ?? 50
            const ay = a.y ?? 0.15
            const az = a.z !== undefined ? a.z : (a.y ?? 50)
            const distSq = (ax - curX) * (ax - curX) + (ay - curY) * (ay - curY) + (az - curZ) * (az - curZ)
            return { ant: a, distSq }
        })
        .sort((a, b) => a.distSq - b.distSq)

    if (candidates.length > 0) {
        const nextAnt = candidates[0].ant
        set({
            trackedAntId: nextAnt.id,
            trackedAntData: nextAnt,
            selectedEntity: nextAnt,
            antNavigationHistory: history
        })
    }
}

export function selectPreviousAntHelper(get, set) {
    const state = get()
    const ants = (state.ants || []).filter(a => a && (a.health === undefined || a.health > 0))
    if (ants.length === 0) return
    const currentId = state.trackedAntId || state.selectedEntity?.id
    const history = [...(state.antNavigationHistory || [])]

    while (history.length > 0) {
        const prevId = history.pop()
        if (prevId !== currentId) {
            const found = ants.find(a => a.id === prevId)
            if (found) {
                set({
                    trackedAntId: found.id,
                    trackedAntData: found,
                    selectedEntity: found,
                    antNavigationHistory: history
                })
                return
            }
        }
    }

    const currentAnt = ants.find(a => a.id === currentId)
    if (!currentAnt) {
        const last = ants[ants.length - 1]
        set({ trackedAntId: last.id, trackedAntData: last, selectedEntity: last, antNavigationHistory: [] })
        return
    }

    const curX = currentAnt.x ?? 50
    const curY = currentAnt.y ?? 0.15
    const curZ = currentAnt.z !== undefined ? currentAnt.z : (currentAnt.y ?? 50)

    const candidates = ants
        .filter(a => a.id !== currentId)
        .map(a => {
            const ax = a.x ?? 50
            const ay = a.y ?? 0.15
            const az = a.z !== undefined ? a.z : (a.y ?? 50)
            const distSq = (ax - curX) * (ax - curX) + (ay - curY) * (ay - curY) + (az - curZ) * (az - curZ)
            return { ant: a, distSq }
        })
        .sort((a, b) => a.distSq - b.distSq)

    if (candidates.length > 0) {
        const prevAnt = candidates[0].ant
        set({
            trackedAntId: prevAnt.id,
            trackedAntData: prevAnt,
            selectedEntity: prevAnt,
            antNavigationHistory: []
        })
    }
}

export function createCheckpointHelper(get, set, name) {
    const state = get()
    const cp = {
        id: `cp_${Date.now()}`,
        name: name || `Checkpoint @ Tick ${state.ticks}`,
        tick: state.ticks,
        simTimeSeconds: state.simTimeSeconds,
        simCalendarTime: state.simTimeFormatted,
        snapshot: {
            ants: JSON.parse(JSON.stringify(state.ants)),
            colonies: JSON.parse(JSON.stringify(state.colonies)),
            foodSources: JSON.parse(JSON.stringify(state.foodSources)),
            environment: { ...state.environment }
        }
    }
    set({ checkpoints: [cp, ...state.checkpoints] })
    get().addEventLog({
        severity: 'INFO',
        type: 'SYSTEM',
        source: 'Points de Contrôle',
        message: `Point de contrôle "${cp.name}" créé au tick ${cp.tick}.`
    })
    return cp
}

export function restoreCheckpointHelper(get, set, checkpointId) {
    const cp = get().checkpoints.find(c => c.id === checkpointId)
    if (!cp) return false
    get().pause()
    set({
        ticks: cp.tick,
        simTimeSeconds: cp.simTimeSeconds,
        simTimeFormatted: cp.simCalendarTime,
        ants: JSON.parse(JSON.stringify(cp.snapshot.ants)),
        colonies: JSON.parse(JSON.stringify(cp.snapshot.colonies)),
        foodSources: JSON.parse(JSON.stringify(cp.snapshot.foodSources)),
        environment: { ...cp.snapshot.environment }
    })
    get().addEventLog({
        severity: 'INFO',
        type: 'SYSTEM',
        source: 'Points de Contrôle',
        message: `Restauration du point de contrôle "${cp.name}" au tick ${cp.tick}.`
    })
    return true
}

export async function fetchRealWeatherHelper(get, set, queryCity) {
    const city = queryCity || get().realWeatherCity || 'Paris'
    set({ realWeatherStatus: 'Chargement météo en cours...' })
    try {
        let lat = 48.8566
        let lon = 2.3522
        const coords = city.split(',')
        if (coords.length === 2 && !isNaN(parseFloat(coords[0])) && !isNaN(parseFloat(coords[1]))) {
            lat = parseFloat(coords[0])
            lon = parseFloat(coords[1])
        } else {
            const geoRes = await fetch(`https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(city)}&count=1&language=fr&format=json`)
            const geoData = await geoRes.json()
            if (geoData.results && geoData.results.length > 0) {
                lat = geoData.results[0].latitude
                lon = geoData.results[0].longitude
            }
        }
        const weatherRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,wind_speed_10m,direct_radiation`)
        const data = await weatherRes.json()
        if (data.current) {
            const temp = data.current.temperature_2m ?? 20.0
            const hum = data.current.relative_humidity_2m ?? 60.0
            const wind = data.current.wind_speed_10m ?? 8.0
            const radiation = data.current.direct_radiation ?? 400.0

            set(state => ({
                environment: {
                    ...state.environment,
                    temperature: temp,
                    humidity: hum,
                    windSpeed: wind,
                    solarRadiation: radiation
                },
                realWeatherStatus: `✓ ${city} : ${temp.toFixed(1)}°C, ${hum}% Hum, ${wind.toFixed(1)} km/h`
            }))
            get().addEventLog({
                severity: 'INFO',
                type: 'ENVIRONMENT',
                source: 'Open-Meteo API',
                message: `🛰️ Conditions réelles appliquées pour ${city} : ${temp.toFixed(1)}°C, ${hum}% Humidité, Vent ${wind.toFixed(1)} km/h.`
            })
        }
    } catch (e) {
        set({ realWeatherStatus: 'Erreur lors de la récupération Open-Meteo' })
    }
}
