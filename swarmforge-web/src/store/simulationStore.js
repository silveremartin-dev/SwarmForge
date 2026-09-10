import { create } from 'zustand'

const WMO_WEATHER_CODES = {
    0: { label: 'Ciel dégagé', state: 'CLEAR', icon: '☀️' },
    1: { label: 'Principalement dégagé', state: 'CLEAR', icon: '🌤️' },
    2: { label: 'Partiellement nuageux', state: 'CLOUDY', icon: '⛅' },
    3: { label: 'Couvert', state: 'CLOUDY', icon: '☁️' },
    45: { label: 'Brouillard', state: 'FOG', icon: '🌫️' },
    48: { label: 'Brouillard givrant', state: 'FOG', icon: '🌫️' },
    51: { label: 'Bruine légère', state: 'RAIN', icon: '🌦️' },
    53: { label: 'Bruine modérée', state: 'RAIN', icon: '🌦️' },
    55: { label: 'Bruine dense', state: 'RAIN', icon: '🌧️' },
    61: { label: 'Pluie faible', state: 'RAIN', icon: '🌧️' },
    63: { label: 'Pluie modérée', state: 'RAIN', icon: '🌧️' },
    65: { label: 'Pluie forte', state: 'TEMPEST', icon: '🌧️' },
    71: { label: 'Neige légère', state: 'SNOW', icon: '🌨️' },
    73: { label: 'Neige modérée', state: 'SNOW', icon: '🌨️' },
    75: { label: 'Blizzard / Neige forte', state: 'BLIZZARD', icon: '❄️' },
    80: { label: 'Averses faibles', state: 'RAIN', icon: '🌦️' },
    81: { label: 'Averses modérées', state: 'RAIN', icon: '🌧️' },
    82: { label: 'Averses violentes', state: 'TEMPEST', icon: '⛈️' },
    95: { label: 'Orage', state: 'THUNDERSTORM', icon: '⚡' },
    96: { label: 'Orage avec grêle légère', state: 'HAIL', icon: '⛈️' },
    99: { label: 'Orage avec forte grêle', state: 'HAIL', icon: '⛈️' },
}

const loadLocalCheckpoints = () => {
    try {
        const saved = localStorage.getItem('swarmforge_checkpoints')
        return saved ? JSON.parse(saved) : []
    } catch {
        return []
    }
}

const saveLocalCheckpoints = (checkpoints) => {
    try {
        localStorage.setItem('swarmforge_checkpoints', JSON.stringify(checkpoints.slice(0, 20)))
    } catch (e) {
        console.error('Failed to save checkpoints', e)
    }
}

export function formatSimTime(totalSeconds) {
    const totalSec = Math.floor(Math.max(0, totalSeconds || 0))
    const days = Math.floor(totalSec / 86400)
    const rem = totalSec % 86400
    const hours = Math.floor(rem / 3600)
    const mins = Math.floor((rem % 3600) / 60)
    const secs = Math.floor(rem % 60)
    return `J+${days} ${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

export function generateDefaultChambersForNest(nest) {
    const scale = nest.scale || 1.0
    const nx = nest.x <= 5 ? nest.x * 50 : nest.x
    const nz = nest.y <= 5 ? (nest.y !== undefined ? nest.y * 50 : 50) : (nest.z !== undefined ? nest.z : 50)

    return [
        {
            id: `${nest.id}_ch_queen`,
            nestId: nest.id,
            nestName: nest.name,
            name: 'Chambre Royale (Reine)',
            type: 'QUEEN_QUARTERS',
            position: { x: nx, y: -2.4 * scale, z: nz },
            radius: 2.2 * scale,
            capacity: 15,
            occupants: 1,
            humidity: 84,
            temperature: 24.8,
            foodStored: 50,
            safetyLevel: 'MAXIMALE (Cœur)',
            caste: 'Reine & Gardes Royales',
            icon: '👑',
            description: 'Chambre centrale la plus profonde, maintenue à hygrométrie et température stables pour la ponte.'
        },
        {
            id: `${nest.id}_ch_nursery`,
            nestId: nest.id,
            nestName: nest.name,
            name: 'Nurserie & Couvain',
            type: 'NURSERY',
            position: { x: nx + 3.2 * scale, y: -1.6 * scale, z: nz + 1.2 * scale },
            radius: 1.9 * scale,
            capacity: 80,
            occupants: 45,
            humidity: 90,
            temperature: 25.2,
            foodStored: 80,
            safetyLevel: 'ÉLEVÉE',
            caste: 'Nourrices & Larves',
            icon: '🍼',
            description: 'Incubation des œufs et développement des larves régulés par les ouvrières nourrices.'
        },
        {
            id: `${nest.id}_ch_food`,
            nestId: nest.id,
            nestName: nest.name,
            name: 'Grenier à Graines & Miellat',
            type: 'FOOD_STORAGE',
            position: { x: nx - 3.0 * scale, y: -1.2 * scale, z: nz - 1.5 * scale },
            radius: 2.0 * scale,
            capacity: 160,
            occupants: 20,
            humidity: 62,
            temperature: 21.5,
            foodStored: 240,
            safetyLevel: 'SÉCURISÉE',
            caste: 'Magasinières & Butineuses',
            icon: '🍯',
            description: 'Stockage des réserves énergétiques (glucides, graines et lipides) à l\'abri des moisissures.'
        },
        {
            id: `${nest.id}_ch_waste`,
            nestId: nest.id,
            nestName: nest.name,
            name: 'Dépotoir & Chambre Sanitaire',
            type: 'WASTE_DUMP',
            position: { x: nx + 2.5 * scale, y: -3.2 * scale, z: nz - 2.8 * scale },
            radius: 1.6 * scale,
            capacity: 50,
            occupants: 8,
            humidity: 48,
            temperature: 19.8,
            foodStored: 0,
            safetyLevel: 'BASSE (Isolée)',
            caste: 'Éboueuses Sanitaires',
            icon: '☣️',
            description: 'Zone de confinement des débris organiques et individus défunts pour éviter les épidémies.'
        },
        {
            id: `${nest.id}_ch_entrance`,
            nestId: nest.id,
            nestName: nest.name,
            name: 'Poste de Garde & Entrée',
            type: 'ENTRANCE',
            position: { x: nx, y: -0.3 * scale, z: nz },
            radius: 1.7 * scale,
            capacity: 50,
            occupants: 28,
            humidity: 58,
            temperature: 22.0,
            foodStored: 20,
            safetyLevel: 'FRONTALIÈRE',
            caste: 'Soldats & Sentinelles',
            icon: '🛡️',
            description: 'Accès vers l\'extérieur et zone de filtrage des odeurs cuticulaires coloniales (CHC).'
        }
    ]
}

// Helper to generate realistic starting ants with species and colony metadata
function generateInitialAnts(colonies = [], count = 80) {
    const list = []
    const cols = colonies && colonies.length > 0 ? colonies : [
        { id: 'COLONY_1', name: 'Colonie #1 (Native)', species: 'Formica fusca', color: '#38bdf8' },
        { id: 'COLONY_2', name: 'Colonie #2 (Rivale)', species: 'Linepithema humile', color: '#f43f5e' }
    ]

    cols.forEach((col, cIdx) => {
        const nestBaseX = cIdx === 0 ? 35 : 65
        const nestBaseY = cIdx === 0 ? 35 : 65
        const perColonyCount = Math.floor(count / cols.length)

        // 1 Queen
        list.push({
            id: `ant_${col.id}_queen`,
            colonyId: col.id,
            colonyName: col.name,
            species: col.species || 'Formica fusca',
            caste: 'QUEEN',
            job: 'LAYING_EGGS',
            x: nestBaseX,
            y: nestBaseY,
            z: 0,
            health: 100,
            energy: 100,
            ageInDays: 365 + Math.floor(Math.random() * 200), // ~1-2 years
            maxLifespanDays: 365 * 10,
            bodyLengthMm: 11.0,
            heading: Math.random() * Math.PI * 2,
            carriedItem: 'NONE',
            diseaseState: 'HEALTHY'
        })

        // Soldiers & Workers
        for (let i = 0; i < perColonyCount; i++) {
            const isSoldier = i < Math.floor(perColonyCount * 0.25)
            const caste = isSoldier ? 'SOLDIER' : 'WORKER'
            const jobs = isSoldier ? ['GUARDING', 'PATROLLING'] : ['FORAGING', 'EXCAVATING', 'NURSING', 'EXPLORING']
            const job = jobs[Math.floor(Math.random() * jobs.length)]
            const angle = Math.random() * Math.PI * 2
            const dist = Math.random() * 15

            list.push({
                id: `ant_${col.id}_${i}`,
                colonyId: col.id,
                colonyName: col.name,
                species: col.species || 'Formica fusca',
                caste,
                job,
                x: Math.max(5, Math.min(95, nestBaseX + Math.cos(angle) * dist)),
                y: Math.max(5, Math.min(95, nestBaseY + Math.sin(angle) * dist)),
                z: 0,
                health: 90 + Math.floor(Math.random() * 10),
                energy: 70 + Math.floor(Math.random() * 30),
                ageInDays: Math.floor(Math.random() * 15) + 3, // 3 to 18 days (healthy prime youth)
                maxLifespanDays: isSoldier ? 180 : 120,
                bodyLengthMm: isSoldier ? 7.5 : 5.0,
                heading: Math.random() * Math.PI * 2,
                carriedItem: Math.random() < 0.2 ? 'SEEDS' : (Math.random() < 0.1 ? 'SUGAR_NECTAR' : 'NONE'),
                diseaseState: 'HEALTHY'
            })
        }
    })

    return list
}

export const useSimulationStore = create((set, get) => ({
    // Connection state
    connected: false,
    ws: null,

    // Simulation state
    tick: 0,
    running: false,
    speed: 1.0,
    simSeconds: 0,
    simTimeFormatted: 'J+0 00:00:00',
    simulationSeed: 12345,

    // Chamber selection & overlay toggle
    selectedChamber: null,
    setSelectedChamber: (chamber) => set({ selectedChamber: chamber, selectedEntity: null }),
    showChamberOverlay: true,
    setShowChamberOverlay: (show) => set({ showChamberOverlay: show }),
    toggleChamberOverlay: () => set(state => ({ showChamberOverlay: !state.showChamberOverlay })),

    // Scientific Mode: Kinematic vectors and sensory FOV cones toggle
    showScientificSensoryVectors: true,
    setShowScientificSensoryVectors: (show) => set({ showScientificSensoryVectors: show }),
    toggleScientificSensoryVectors: () => set(state => ({ showScientificSensoryVectors: !state.showScientificSensoryVectors })),

    // Ant navigation & cycling
    selectNextAnt: () => {
        const { ants, selectedEntity } = get()
        if (!ants || ants.length === 0) return
        let currentIdx = -1
        if (selectedEntity && selectedEntity.id) {
            currentIdx = ants.findIndex(a => a.id === selectedEntity.id)
        }
        const nextIdx = (currentIdx + 1) % ants.length
        set({ selectedEntity: ants[nextIdx], selectedChamber: null })
    },
    selectPreviousAnt: () => {
        const { ants, selectedEntity } = get()
        if (!ants || ants.length === 0) return
        let currentIdx = 0
        if (selectedEntity && selectedEntity.id) {
            currentIdx = ants.findIndex(a => a.id === selectedEntity.id)
        }
        const prevIdx = (currentIdx - 1 + ants.length) % ants.length
        set({ selectedEntity: ants[prevIdx], selectedChamber: null })
    },

    stepSingleTick: () => {
        get().stepSimulationTick()
    },

    // Camera 3D Viewport Reset Trigger
    cameraResetTrigger: 0,
    triggerCameraReset: () => {
        set(state => ({
            cameraResetTrigger: state.cameraResetTrigger + 1,
            selectedEntity: null
        }))
        get().addEventLog({
            level: 'INFO',
            category: 'VIEWPORT',
            message: '🎥 Caméra 3D repositionnée aux coordonnées par défaut [50, 65, 125].'
        })
    },

    // Checkpoint Management System
    checkpoints: loadLocalCheckpoints(),
    autoCheckpoint: true,
    autoCheckpointInterval: 25000, // Save every 25,000 ticks (approx. 500-1000s)

    createCheckpoint: (customLabel) => {
        const state = get()
        const newCheckpoint = {
            id: `ckpt_${Date.now()}_${state.tick}`,
            tick: state.tick,
            timestamp: new Date().toISOString(),
            label: customLabel || `Checkpoint Tick #${state.tick}`,
            masterSeed: state.simulationSeed,
            stats: { ...state.stats },
            colonies: JSON.parse(JSON.stringify(state.colonies)),
            antsCount: state.ants.length,
            ants: JSON.parse(JSON.stringify(state.ants)),
            foodSources: JSON.parse(JSON.stringify(state.foodSources)),
            predators: JSON.parse(JSON.stringify(state.predators)),
            nests: JSON.parse(JSON.stringify(state.nests)),
            environment: { ...state.environment },
            climateEngine: { ...state.climateEngine },
            weatherMode: state.weatherMode,
            realWeatherData: state.realWeatherData,
            weatherQueryHistory: JSON.parse(JSON.stringify(state.weatherQueryHistory || [])),
        }

        const updated = [newCheckpoint, ...state.checkpoints.slice(0, 19)]
        set({ checkpoints: updated })
        saveLocalCheckpoints(updated)

        get().addEventLog({
            level: 'INFO',
            category: 'CHECKPOINT',
            message: `💾 Checkpoint "${newCheckpoint.label}" sauvegardé avec succès (Tick #${state.tick}, ${state.ants.length} individus).`
        })
        return newCheckpoint
    },

    restoreCheckpoint: (checkpointId) => {
        const state = get()
        const target = state.checkpoints.find(c => c.id === checkpointId)
        if (!target) return false

        const { localTickInterval } = state
        if (localTickInterval) clearInterval(localTickInterval)

        set({
            tick: target.tick,
            running: false,
            localTickInterval: null,
            simulationSeed: target.masterSeed || state.simulationSeed,
            stats: target.stats || state.stats,
            colonies: target.colonies || state.colonies,
            ants: target.ants || [],
            foodSources: target.foodSources || state.foodSources,
            predators: target.predators || state.predators,
            nests: target.nests || state.nests,
            environment: target.environment || state.environment,
            climateEngine: target.climateEngine || state.climateEngine,
            weatherMode: target.weatherMode || 'SIMULATED',
            realWeatherData: target.realWeatherData || null,
            weatherQueryHistory: target.weatherQueryHistory || [],
            selectedEntity: null,
            cameraResetTrigger: state.cameraResetTrigger + 1,
        })

        get().addEventLog({
            level: 'WARN',
            category: 'CHECKPOINT',
            message: `⏮️ Restauration réussie au Checkpoint "${target.label}" (Tick #${target.tick}).`
        })
        return true
    },

    deleteCheckpoint: (checkpointId) => {
        const state = get()
        const updated = state.checkpoints.filter(c => c.id !== checkpointId)
        set({ checkpoints: updated })
        saveLocalCheckpoints(updated)
        get().addEventLog({
            level: 'INFO',
            category: 'CHECKPOINT',
            message: `🗑️ Checkpoint supprimé.`
        })
    },

    clearCheckpoints: () => {
        set({ checkpoints: [] })
        saveLocalCheckpoints([])
    },

    // Weather Engine Mode: 'SIMULATED' (Presets) vs 'REAL_WORLD' (Open-Meteo Live API)
    weatherMode: 'SIMULATED',
    realWeatherData: null,
    realWeatherLoading: false,
    realWeatherError: null,
    weatherQueryHistory: [],

    setWeatherMode: (mode) => {
        set({ weatherMode: mode })
        if (mode === 'REAL_WORLD') {
            get().fetchRealWorldWeather()
        }
        get().addEventLog({
            level: 'INFO',
            category: 'WEATHER',
            message: mode === 'REAL_WORLD'
                ? '🟢 Météo basculée en mode Météo Réelle (Open-Meteo Live API).'
                : '🔵 Météo basculée en mode Météo Simulée (Presets du Terrarium).'
        })
    },

    fetchRealWorldWeather: async (customLat = 48.8566, customLon = 2.3522, customCity = 'Paris', forceLive = false) => {
        const state = get()
        const currentTick = state.tick || 0

        // If recorded history has weather data for replay / determinism and not forcing live, use stored data
        if (!forceLive && state.weatherQueryHistory && state.weatherQueryHistory.length > 0) {
            const storedRecord = state.weatherQueryHistory.slice().reverse().find(r => r.tick === currentTick) ||
                state.weatherQueryHistory.slice().reverse().find(r => r.tick <= currentTick)
            
            if (storedRecord && storedRecord.data) {
                const realData = storedRecord.data
                set(st => ({
                    realWeatherData: realData,
                    realWeatherLoading: false,
                    realWeatherError: null,
                    environment: {
                        ...st.environment,
                        temperature: realData.temp,
                        humidity: realData.humidity,
                        rainIntensity: realData.precipitation,
                        windSpeed: realData.windSpeed,
                        weatherState: realData.state,
                    },
                    climateEngine: {
                        ...st.climateEngine,
                        latitudeDeg: realData.latitude,
                        cloudCover: realData.cloudCover,
                        precipitationMm: realData.precipitation,
                        windSpeedMs: realData.windSpeed,
                        barometricPressureHpa: realData.pressure,
                    }
                }))
                get().addEventLog({
                    level: 'INFO',
                    category: 'WEATHER',
                    message: `⏮️ [Replay Déterministe] Météo réelle restaurée depuis l'historique enregistré (Tick #${storedRecord.tick}): ${realData.temp}°C, ${realData.condition} ${realData.icon}.`
                })
                return realData
            }
        }

        set({ realWeatherLoading: true, realWeatherError: null })
        try {
            let lat = customLat
            let lon = customLon
            let cityName = customCity

            if (navigator.geolocation && customCity === 'Paris') {
                try {
                    const pos = await new Promise((resolve, reject) => {
                        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 3000 })
                    })
                    lat = pos.coords.latitude
                    lon = pos.coords.longitude
                    cityName = 'Position Locale'
                } catch {
                    // Fallback to default
                }
            }

            const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat.toFixed(4)}&longitude=${lon.toFixed(4)}&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,surface_pressure,cloud_cover`
            const res = await fetch(url)
            if (!res.ok) throw new Error(`Erreur API Open-Meteo (${res.status})`)
            const data = await res.json()

            const cur = data.current || {}
            const wCode = cur.weather_code ?? 0
            const wmo = WMO_WEATHER_CODES[wCode] || { label: 'Variable', state: 'CLEAR', icon: '🌤️' }

            const realData = {
                cityName,
                latitude: lat,
                longitude: lon,
                temp: cur.temperature_2m ?? 20.0,
                humidity: cur.relative_humidity_2m ?? 50,
                precipitation: cur.precipitation ?? 0.0,
                windSpeed: cur.wind_speed_10m ?? 3.5,
                pressure: cur.surface_pressure ?? 1013.25,
                cloudCover: (cur.cloud_cover ?? 20) / 100,
                weatherCode: wCode,
                condition: wmo.label,
                state: wmo.state,
                icon: wmo.icon,
                lastUpdated: new Date().toLocaleTimeString('fr-FR')
            }

            const newHistoryEntry = {
                tick: currentTick,
                simTime: state.stats?.simTime || currentTick,
                timestamp: new Date().toISOString(),
                data: realData
            }

            set(st => ({
                realWeatherData: realData,
                weatherQueryHistory: [...(st.weatherQueryHistory || []), newHistoryEntry],
                realWeatherLoading: false,
                environment: {
                    ...st.environment,
                    temperature: realData.temp,
                    humidity: realData.humidity,
                    rainIntensity: realData.precipitation,
                    windSpeed: realData.windSpeed,
                    weatherState: realData.state,
                },
                climateEngine: {
                    ...st.climateEngine,
                    latitudeDeg: lat,
                    cloudCover: realData.cloudCover,
                    precipitationMm: realData.precipitation,
                    windSpeedMs: realData.windSpeed,
                    barometricPressureHpa: realData.pressure,
                }
            }))

            get().addEventLog({
                level: 'INFO',
                category: 'WEATHER',
                message: `🛰️ Météo Réelle Open-Meteo synchronisée (${cityName}): ${realData.temp}°C, ${realData.condition} ${realData.icon}, Vent: ${realData.windSpeed} km/h, Humidité: ${realData.humidity}%.`
            })
        } catch (err) {
            console.error('Failed to fetch real weather:', err)
            set({ realWeatherLoading: false, realWeatherError: err.message })
            get().addEventLog({
                level: 'WARN',
                category: 'WEATHER',
                message: `⚠️ Échec de récupération de la Météo Réelle: ${err.message}`
            })
        }
    },

    // Dynamic Pheromones Field
    pheromones: [],

    // Entity data
    colonies: [
        { id: 'COLONY_1', name: 'Colonie #1 (Native)', species: 'Formica fusca', color: '#38bdf8', foodStored: 250, queenCount: 1, workerCount: 120 },
        { id: 'COLONY_2', name: 'Colonie #2 (Rivale)', species: 'Linepithema humile', color: '#f43f5e', foodStored: 180, queenCount: 2, workerCount: 90 },
    ],
    ants: generateInitialAnts([
        { id: 'COLONY_1', name: 'Colonie #1 (Native)', species: 'Formica fusca', color: '#38bdf8' },
        { id: 'COLONY_2', name: 'Colonie #2 (Rivale)', species: 'Linepithema humile', color: '#f43f5e' }
    ], 60),
    predators: [],
    foodSources: [
        { id: 'food_init_1', x: 45, y: 55, quantity: 200, type: 'SUGAR_NECTAR' },
        { id: 'food_init_2', x: 60, y: 40, quantity: 150, type: 'SEEDS' },
    ],

    lookAndFeel: 'SCIENTIFIC',
    setLookAndFeel: (mode) => {
        const themeAttr = mode === 'SCIENTIFIC' ? 'scientific' : mode === 'REALISTIC' ? 'realistic' : 'gaming'
        document.documentElement.setAttribute('data-theme', themeAttr)
        set({ lookAndFeel: mode })
        get().addEventLog({
            level: 'INFO',
            category: 'THEME',
            message: `🎨 Theme graphique basculé sur: ${mode} (${mode === 'GAMING' ? 'Jeu Vidéo Neon' : mode === 'SCIENTIFIC' ? 'Scientifique Épuré' : 'Réaliste / Naturel'})`
        })
    },

    timeSyncMode: 'REAL_WORLD',
    realWorldTimeStr: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    realWorldDateStr: new Date().toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }),
    
    setTimeSyncMode: (mode) => {
        set({ timeSyncMode: mode })
        if (mode === 'REAL_WORLD') {
            get().updateRealWorldTime()
        }
        get().addEventLog({
            level: 'INFO',
            category: 'TIME_SYNC',
            message: mode === 'REAL_WORLD' 
                ? '🕒 Horloge synchronisée sur la date & heure du Monde Réel.' 
                : '⏱️ Horloge basculée en mode Temps Simulé autonome.'
        })
    },

    updateRealWorldTime: () => {
        const now = new Date()
        const timeStr = now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
        const dateStr = now.toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
        
        const hours = now.getHours() + now.getMinutes() / 60
        const sunAngle = (hours / 24)
        
        let lightLevel = Math.max(0.08, Math.sin((hours - 6) / 12 * Math.PI))
        let timeOfDay = 'NIGHT'
        if (hours >= 6 && hours < 8) timeOfDay = 'DAWN'
        else if (hours >= 8 && hours < 18) timeOfDay = 'DAY'
        else if (hours >= 18 && hours < 21) timeOfDay = 'DUSK'

        set(state => ({
            realWorldTimeStr: timeStr,
            realWorldDateStr: dateStr,
            environment: {
                ...state.environment,
                sunAngle,
                lightLevel,
                timeOfDay
            }
        }))
    },

    phantomNestsVisible: true,
    togglePhantomNests: () => set(state => ({ phantomNestsVisible: !state.phantomNestsVisible })),

    ghostNest: {
        active: false,
        type: 'PINE_NEEDLES',
        x: 50,
        y: 50,
        z: 0,
        scale: 1.0,
        species: 'Formica fusca'
    },
    setGhostNest: (data) => set(state => ({ ghostNest: { ...state.ghostNest, ...data } })),

    nests: [
        {
            id: 'nest_pine_1',
            name: 'Dôme d\'Épines de Pin',
            type: 'PINE_NEEDLES',
            x: 35, y: 35, z: 0,
            scale: 1.2,
            species: 'Formica rufa',
            population: 140,
            isPhantom: false,
            chambers: generateDefaultChambersForNest({ id: 'nest_pine_1', name: 'Dôme d\'Épines de Pin', x: 35, y: 35, scale: 1.2 }),
            tunnels: [
                { startChamberId: 'nest_pine_1_ch_entrance', endChamberId: 'nest_pine_1_ch_nursery' },
                { startChamberId: 'nest_pine_1_ch_entrance', endChamberId: 'nest_pine_1_ch_food' },
                { startChamberId: 'nest_pine_1_ch_nursery', endChamberId: 'nest_pine_1_ch_queen' },
                { startChamberId: 'nest_pine_1_ch_food', endChamberId: 'nest_pine_1_ch_queen' },
                { startChamberId: 'nest_pine_1_ch_queen', endChamberId: 'nest_pine_1_ch_waste' },
            ]
        },
        {
            id: 'nest_termite_1',
            name: 'Termitière Cathédrale',
            type: 'TERMITE_MOUND',
            x: 75, y: 30, z: 0,
            scale: 1.5,
            species: 'Macrotermes',
            population: 310,
            isPhantom: false,
            chambers: generateDefaultChambersForNest({ id: 'nest_termite_1', name: 'Termitière Cathédrale', x: 75, y: 30, scale: 1.5 }),
            tunnels: [
                { startChamberId: 'nest_termite_1_ch_entrance', endChamberId: 'nest_termite_1_ch_nursery' },
                { startChamberId: 'nest_termite_1_ch_entrance', endChamberId: 'nest_termite_1_ch_food' },
                { startChamberId: 'nest_termite_1_ch_nursery', endChamberId: 'nest_termite_1_ch_queen' },
                { startChamberId: 'nest_termite_1_ch_food', endChamberId: 'nest_termite_1_ch_queen' },
                { startChamberId: 'nest_termite_1_ch_queen', endChamberId: 'nest_termite_1_ch_waste' },
            ]
        },
        { id: 'nest_wasp_1', name: 'Guêpier Suspendu sur Branche', type: 'WASP_BRANCH', x: 40, y: 70, z: 1.8, scale: 1.1, species: 'Vespula vulgaris', population: 85, isPhantom: false },
        { id: 'nest_beehive_1', name: 'Ruche Ruche Traditionnelle', type: 'WOODEN_BEEHIVE', x: 65, y: 65, z: 0, scale: 1.3, species: 'Apis mellifera', population: 220, isPhantom: false },
        { id: 'nest_trunk_1', name: 'Cavité dans Tronc d\'Arbre', type: 'TREE_TRUNK', x: 20, y: 55, z: 0, scale: 1.4, species: 'Camponotus herculeanus', population: 95, isPhantom: false },
    ],

    addNest: (newNest) => set(state => {
        const id = newNest.id || `nest_${Date.now()}`
        const updated = [...state.nests, { ...newNest, id, isPhantom: false }]
        get().addEventLog({
            level: 'INFO',
            category: 'NEST',
            message: `🏗️ Nouveau Nid construit: "${newNest.name || newNest.type}" à (X:${newNest.x}m, Y:${newNest.y}m)`,
        })
        return { nests: updated }
    }),

    removeNest: (nestId) => set(state => {
        return { nests: state.nests.filter(n => n.id !== nestId) }
    }),

    climateEngine: {
        latitudeDeg: 45.0,
        hemisphere: 'NORTHERN',
        climateType: 'OCEANIC',
        season: 'SUMMER',
        dayOfYear: 200,
        cloudCover: 0.25,
        precipitationMm: 0.0,
        barometricPressureHpa: 1013.25,
        windSpeedMs: 2.4,
        windDirectionDeg: 225,
        soilMoisture: 0.45,
    },

    terrainConfig: {
        baseElevation: 0,
        slopeX: 0.0,
        slopeZ: 0.0,
        roughness: 0.45,
        hasRiver: true,
        riverX: 25,
        riverWidth: 10,
        riverDepth: 0.6
    },
    updateTerrainConfig: (config) => set(state => ({ terrainConfig: { ...state.terrainConfig, ...config } })),
    setHemisphere: (hemisphere) => set(state => ({ climateEngine: { ...state.climateEngine, hemisphere } })),

    environmentLighting: {
        sunAzimuthDeg: 145,
        sunElevationDeg: 42,
        slopeExposure: 'SOUTH',
        isNight: false,
        moonPhase: 'FULL_MOON',
        moonlightIntensity: 0.4,
        currentCalculatedTempC: 22.5,
    },

    disasterState: {
        activeDisaster: null,
        intensity: 0,
        floodWaterLevelMm: 0,
    },

    trophallaxisEvents: [],
    trophallaxisActive: true,

    predatorCatalog: [
        { id: 'TAMANDUA', name: 'Fourmilier (Tamandua)', target: 'ALL_ANTS', threat: 'CRITICAL', icon: '🦥' },
        { id: 'PICUS_VIRIDIS', name: 'Pic-vert (Picus viridis)', target: 'PINE_NEEDLES', threat: 'HIGH', icon: '🐦' },
        { id: 'PSEUDACTEON', name: 'Mouche Phoride Parasite', target: 'WORKER_ANTS', threat: 'MEDIUM', icon: '🪰' },
        { id: 'SPIDER_MYRMECOPHAGE', name: 'Araignée Myrmécophage', target: 'FORAGERS', threat: 'MEDIUM', icon: '🕷️' },
    ],

    diseaseParams: {
        activeOutbreak: false,
        diseaseType: 'CORDYCEPS',
        contagionRate: 0.35,
    },
    epidemicStats: {
        healthy: 210,
        incubating: 0,
        infected: 0,
        contagious: 0,
        immune: 0,
        totalDeaths: 0,
    },

    triggerEpidemic: (type = 'CORDYCEPS', patientZeroCount = 3) => {
        const ants = [...get().ants]
        let infectedCount = 0
        const updatedAnts = ants.map((ant, idx) => {
            if (idx < patientZeroCount || Math.random() < 0.05) {
                infectedCount++
                return {
                    ...ant,
                    diseaseState: 'INFECTED',
                    diseaseType: type,
                    health: ant.health || 100,
                    contagionTimer: 0
                }
            }
            return ant
        })

        set(state => ({
            ants: updatedAnts,
            diseaseParams: {
                ...state.diseaseParams,
                activeOutbreak: true,
                diseaseType: type
            },
            epidemicStats: {
                ...state.epidemicStats,
                infected: infectedCount,
                contagious: infectedCount,
                healthy: Math.max(0, state.ants.length - infectedCount)
            }
        }))

        get().addEventLog({
            level: 'WARN',
            category: 'DISEASE',
            message: `☣️ ÉPIDÉMIE DÉCLENCHÉE: Pathogène ${type} propagé dans la colonie ! (${infectedCount} cas initiaux)`,
        })
    },

    cureEpidemic: () => {
        const updatedAnts = get().ants.map(ant => ({
            ...ant,
            diseaseState: 'HEALTHY',
            diseaseType: null,
            health: 100
        }))

        set(state => ({
            ants: updatedAnts,
            diseaseParams: {
                ...state.diseaseParams,
                activeOutbreak: false
            },
            epidemicStats: {
                healthy: updatedAnts.length,
                incubating: 0,
                infected: 0,
                contagious: 0,
                immune: state.epidemicStats.immune + state.epidemicStats.infected,
                totalDeaths: state.epidemicStats.totalDeaths
            }
        }))

        get().addEventLog({
            level: 'INFO',
            category: 'DISEASE',
            message: `💉 TRAITEMENT FONGICIDE DIVIN APPLIQUÉ: Épidémie éradiquée, individus soignés.`,
        })
    },

    simulationParams: {
        pheromones: {
            dissipationRate: { value: 0.50, unit: '%/s', min: 0.01, max: 5.00, label: 'Taux de Dissipation des Phéromones' },
            diffusionRadius: { value: 1.20, unit: 'm', min: 0.20, max: 5.00, label: 'Rayon de Diffusion Évaporative' },
            alarmThreshold: { value: 40, unit: '%', min: 5, max: 100, label: 'Seuil d\'Intensité d\'Alarme' },
        },
        resources: {
            foodSpawnRate: { value: 12, unit: 'g/min', min: 1, max: 100, label: 'Taux d\'Apparition de Nourriture' },
            foodDecayRate: { value: 0.20, unit: '%/h', min: 0.0, max: 2.0, label: 'Taux de Décomposition Organique' },
            honeydewRate: { value: 2.50, unit: 'mg/h', min: 0.1, max: 10.0, label: 'Production de Miellat (Pucerons)' },
        },
        species: {
            queenFecundity: { value: 60, unit: 'œufs/jour', min: 5, max: 300, label: 'Fécondité Reine (Taux de Ponte)' },
            workerLifespan: { value: 90, unit: 'jours', min: 10, max: 365, label: 'Espérance de Vie des Ouvrières' },
            soldierRatio: { value: 25, unit: '%', min: 0, max: 60, label: 'Proportion de Soldats dans la Colonie' },
        },
        economics: {
            metabolicCost: { value: 0.50, unit: 'mW', min: 0.05, max: 5.00, label: 'Puissance Métabolique de Base' },
            harvestEfficiency: { value: 85, unit: '%', min: 20, max: 100, label: 'Rendement de Récolte Énergétique' },
        }
    },

    setSimulationParam: (category, key, newValue) => set(state => ({
        simulationParams: {
            ...state.simulationParams,
            [category]: {
                ...state.simulationParams[category],
                [key]: {
                    ...state.simulationParams[category][key],
                    value: Number(newValue)
                }
            }
        }
    })),

    addColony: (newColony) => set(state => {
        const id = newColony.id || `COLONY_${Date.now()}`
        const updated = [...state.colonies, { ...newColony, id }]
        get().addEventLog({
            level: 'INFO',
            category: 'COLONY',
            message: `🏛️ Colonie "${newColony.name}" (${newColony.species}) créée et enregistrée dans le monde.`,
        })
        return { colonies: updated }
    }),

    removeColony: (colonyId) => set(state => {
        const col = state.colonies.find(c => c.id === colonyId)
        get().addEventLog({
            level: 'WARN',
            category: 'COLONY',
            message: `🗑️ Colonie "${col?.name || colonyId}" supprimée.`,
        })
        return { colonies: state.colonies.filter(c => c.id !== colonyId) }
    }),

    eventLogs: [
        {
            id: `evt_init_${Date.now()}`,
            tick: 0,
            simTimeFormatted: 'J+0 00:00:00',
            timestamp: new Date().toISOString(),
            level: 'INFO',
            category: 'SYSTEM',
            message: '🎬 Session de simulation initialisée (Horloge 1:1). Prêt pour le démarrage.',
        }
    ],

    addEventLog: (logObj) => set(state => {
        const simTimeStr = logObj.simTimeFormatted || state.simTimeFormatted || formatSimTime(state.simSeconds || state.tick || 0)
        const newLog = {
            id: `evt_${Date.now()}_${Math.floor(Math.random() * 10000)}`,
            tick: state.tick,
            simTimeFormatted: simTimeStr,
            timestamp: new Date().toISOString(),
            level: logObj.level || 'INFO',
            category: logObj.category || 'SIMULATION',
            message: logObj.message || '',
            details: logObj.details || null,
        }
        const updated = [newLog, ...state.eventLogs.slice(0, 499)]
        return { eventLogs: updated }
    }),

    clearEventLogs: () => set({ eventLogs: [] }),

    interventionsLog: [],

    recordDivineIntervention: (actionData) => set(state => {
        const intervention = {
            id: `god_act_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            tick: state.tick,
            timestamp: new Date().toISOString(),
            ...actionData
        }

        let newFood = [...state.foodSources]
        let newPredators = [...state.predators]

        if (actionData.type === 'SPAWN_FOOD') {
            newFood.push({
                id: `food_god_${Date.now()}`,
                x: actionData.x || 50,
                y: actionData.y || 50,
                quantity: actionData.quantity || 100,
                type: actionData.foodType || 'SUGAR_NECTAR'
            })
        } else if (actionData.type === 'SPAWN_PREDATOR') {
            newPredators.push({
                id: `pred_god_${Date.now()}`,
                x: actionData.x || 50,
                y: actionData.y || 50,
                type: actionData.predatorType || 'SPIDER',
                state: 'HUNTING'
            })
        }

        if (state.ws && state.connected) {
            state.ws.send(JSON.stringify({
                type: 'GOD_MODE_INTERVENTION',
                intervention
            }))
        }

        get().addEventLog({
            level: 'WARN',
            category: 'GOD_MODE',
            message: `⚡ Intervention Divinement: ${actionData.actionName}`,
            details: actionData
        })

        return {
            interventionsLog: [intervention, ...state.interventionsLog],
            foodSources: newFood,
            predators: newPredators
        }
    }),

    clearInterventionsLog: () => set({ interventionsLog: [] }),

    localTickInterval: null,

    stats: {
        totalPopulation: 210,
        totalWorkers: 150,
        totalSoldiers: 60,
        totalFood: 430,
    },

    environment: {
        terrariumWidth: 2.0,
        terrariumDepth: 2.0,
        terrariumHeight: 1.0,
        lightLevel: 1.0,
        timeOfDay: 'DAY',
        sunAngle: 0.5,
        temperature: 20,
        humidity: 50,
        rainIntensity: 0,
        windSpeed: 5,
        weatherState: 'CLEAR',
    },

    updateEnvironment: (newEnv) => {
        set(state => ({
            environment: { ...state.environment, ...newEnv }
        }))
        get().addEventLog({
            level: 'INFO',
            category: 'ENVIRONMENT',
            message: `🌍 Mise à jour environnementale: Météo=${newEnv.weatherState || get().environment.weatherState}, Temp=${(newEnv.temperature ?? get().environment.temperature).toFixed(1)}°C`
        })
    },

    weatherToggles: {
        showSun: true,
        showLightning: true,
        showClouds: true,
        showPrecipitation: true,
        showFog: true,
        showWindDust: true,
        nightVision: false,
        lightningTrigger: 0,
    },
    setWeatherToggle: (key, value) => set(state => ({
        weatherToggles: { ...state.weatherToggles, [key]: value }
    })),
    triggerLightning: () => {
        set(state => ({
            weatherToggles: { ...state.weatherToggles, lightningTrigger: state.weatherToggles.lightningTrigger + 1 }
        }))
        get().addEventLog({
            level: 'WARN',
            category: 'WEATHER',
            message: '⚡ Éclair atmosphérique déclenché par l\'utilisateur.'
        })
    },

    selectedEntity: null,
    setSelectedEntity: (entity) => set({ selectedEntity: entity }),

    connect: () => {
        try {
            const ws = new WebSocket('ws://localhost:8081')

            ws.onopen = () => {
                set({ connected: true, ws })
                get().addEventLog({
                    level: 'INFO',
                    category: 'SYSTEM',
                    message: '🟢 Connecté au serveur de simulation haute performance (WebSocket: 8081)'
                })
                ws.send(JSON.stringify({ type: 'SUBSCRIBE', viewport: { x: 0, y: 0, width: 100, height: 100 } }))
            }

            ws.onmessage = (event) => {
                const data = JSON.parse(event.data)
                get().handleMessage(data)
            }

            ws.onclose = () => {
                set({ connected: false, ws: null })
            }

            ws.onerror = () => {
                set({ connected: false, ws: null })
            }
        } catch {
            set({ connected: false, ws: null })
        }
    },

    disconnect: () => {
        const { ws } = get()
        if (ws) {
            ws.close()
            set({ connected: false, ws: null })
        }
    },

    handleMessage: (data) => {
        switch (data.type) {
            case 'STATE':
                set({
                    tick: data.tick,
                    running: data.running,
                    ants: data.individuals || [],
                    colonies: data.colonies && data.colonies.length > 0 ? data.colonies : get().colonies,
                    foodSources: data.foodSources || get().foodSources,
                    predators: data.predators || get().predators,
                    stats: {
                        totalPopulation: data.individuals?.length || 0,
                        totalWorkers: data.individuals?.filter(a => a.caste === 'WORKER').length || 0,
                        totalSoldiers: data.individuals?.filter(a => a.caste === 'SOLDIER').length || 0,
                        totalFood: data.colonies?.reduce((sum, c) => sum + (c.foodStored || 0), 0) || 0,
                    },
                })
                break
            case 'UPDATE':
                if (data.individuals) {
                    set(state => ({
                        ants: state.ants.map(ant => {
                            const update = data.individuals.find(u => u.id === ant.id)
                            return update ? { ...ant, ...update } : ant
                        }),
                    }))
                }
                if (data.tick) set({ tick: data.tick })
                if (data.environment) set({ environment: data.environment })
                if (data.nests) set({ nests: data.nests })
                break
            default:
                console.log('Unknown message:', data)
        }
    },

    resetSimulation: (sessionData) => {
        const { localTickInterval, colonies, cameraResetTrigger } = get()
        if (localTickInterval) clearInterval(localTickInterval)
        
        const newAnts = generateInitialAnts(colonies, 75)

        set(state => ({
            tick: 0,
            simSeconds: 0,
            simTimeFormatted: 'J+0 00:00:00',
            running: false,
            localTickInterval: null,
            ants: newAnts,
            pheromones: [],
            selectedEntity: null,
            selectedChamber: null,
            interventionsLog: [],
            eventLogs: [
                {
                    id: `evt_init_${Date.now()}`,
                    tick: 0,
                    simTimeFormatted: 'J+0 00:00:00',
                    timestamp: new Date().toISOString(),
                    level: 'INFO',
                    category: 'SYSTEM',
                    message: '🎬 Nouvelle session initialisée à Tick #0 (Temps: J+0 00:00:00).',
                }
            ],
            cameraResetTrigger: cameraResetTrigger + 1,
            simulationSeed: sessionData?.masterSeed || state.simulationSeed || 12345,
            stats: {
                totalPopulation: newAnts.length,
                totalWorkers: newAnts.filter(a => a.caste === 'WORKER').length,
                totalSoldiers: newAnts.filter(a => a.caste === 'SOLDIER').length,
                totalFood: 430,
            }
        }))
    },

    stepSimulationTick: () => {
        const state = get()
        const nextTick = state.tick + 1
        const nextSimSeconds = (state.simSeconds || 0) + 1.0
        const nextSimTimeFormatted = formatSimTime(nextSimSeconds)

        let currentSeed = (state.simulationSeed || 42) + nextTick * 10007
        const getSeededRandom = () => {
            const x = Math.sin(currentSeed++) * 10000
            return x - Math.floor(x)
        }

        if (state.timeSyncMode === 'REAL_WORLD') {
            state.updateRealWorldTime()
        }

        if (state.weatherMode === 'REAL_WORLD') {
            if (state.weatherQueryHistory && state.weatherQueryHistory.length > 0) {
                const recordedEntry = state.weatherQueryHistory.slice().reverse().find(r => r.tick <= nextTick)
                if (recordedEntry && recordedEntry.data && (!state.realWeatherData || state.realWeatherData.lastUpdated !== recordedEntry.data.lastUpdated)) {
                    const realData = recordedEntry.data
                    set(s => ({
                        realWeatherData: realData,
                        environment: {
                            ...s.environment,
                            temperature: realData.temp,
                            humidity: realData.humidity,
                            rainIntensity: realData.precipitation,
                            windSpeed: realData.windSpeed,
                            weatherState: realData.state,
                        },
                        climateEngine: {
                            ...s.climateEngine,
                            latitudeDeg: realData.latitude,
                            cloudCover: realData.cloudCover,
                            precipitationMm: realData.precipitation,
                            windSpeedMs: realData.windSpeed,
                            barometricPressureHpa: realData.pressure,
                        }
                    }))
                }
            }
        }

        if (state.weatherMode === 'SIMULATED') {
            // Realistic day-night solar progression: 24h = 86400 sim seconds
            const simHourOfDay = ((nextSimSeconds % 86400) / 3600)
            const dayOfYear = state.climateEngine?.dayOfYear || 200
            const latRad = ((state.climateEngine?.latitudeDeg || 45.0) * Math.PI) / 180.0

            const declinationRad = ((23.45 * Math.sin(((360 / 365) * (dayOfYear - 81) * Math.PI) / 180.0)) * Math.PI) / 180.0
            const hourAngleRad = ((15 * (simHourOfDay - 12)) * Math.PI) / 180.0

            const sinElevation = Math.sin(latRad) * Math.sin(declinationRad) + Math.cos(latRad) * Math.cos(declinationRad) * Math.cos(hourAngleRad)
            const solarElevationDeg = (Math.asin(Math.max(-1, Math.min(1, sinElevation))) * 180) / Math.PI
            const isNightTime = solarElevationDeg < -0.833

            const climatePresets = {
                OCEANIC: { baseTemp: 18.0, amplitude: 4.5, humidityBase: 75 },
                CONTINENTAL: { baseTemp: 21.0, amplitude: 14.0, humidityBase: 50 },
                MEDITERRANEAN: { baseTemp: 25.0, amplitude: 9.0, humidityBase: 45 },
                ALPINE: { baseTemp: 12.0, amplitude: 11.0, humidityBase: 65 },
                TROPICAL: { baseTemp: 28.0, amplitude: 3.5, humidityBase: 85 },
            }
            const activePreset = climatePresets[state.climateEngine?.climateType || 'OCEANIC']
            const cloudFactor = 1.0 - 0.55 * (state.climateEngine?.cloudCover || 0.25)
            const solarHeatingGain = Math.max(0, Math.sin((solarElevationDeg * Math.PI) / 180.0)) * activePreset.amplitude * cloudFactor
            const nightCoolingLoss = isNightTime ? activePreset.amplitude * 0.75 : 0.0
            const calculatedDiurnalTemp = (activePreset.baseTemp + solarHeatingGain - nightCoolingLoss).toFixed(1)

            set(s => ({
                environmentLighting: {
                    ...s.environmentLighting,
                    sunElevationDeg: parseFloat(solarElevationDeg.toFixed(1)),
                    sunAzimuthDeg: parseFloat(((180 + 15 * (simHourOfDay - 12)) % 360).toFixed(1)),
                    isNight: isNightTime,
                    currentCalculatedTempC: parseFloat(calculatedDiurnalTemp)
                }
            }))
        }

        const newPheromones = [...state.pheromones]
        const dissipationFactor = 0.985

        const decayedPheros = newPheromones
            .map(p => ({ ...p, intensity: p.intensity * dissipationFactor }))
            .filter(p => p.intensity > 0.05)

        let currentAnts = state.ants
        if (currentAnts.length === 0) {
            currentAnts = generateInitialAnts(state.colonies, 60)
        }

        const updatedAnts = currentAnts.map(ant => {
            if (ant.caste === 'QUEEN') {
                return { ...ant, ageInDays: ant.ageInDays + (1.0 / 86400.0) }
            }

            const stepDist = 0.4 + getSeededRandom() * 0.6
            const turnAngle = (getSeededRandom() - 0.5) * 0.6
            const heading = (ant.heading || 0) + turnAngle
            
            let nx = ant.x + Math.cos(heading) * stepDist
            let ny = ant.y + Math.sin(heading) * stepDist

            if (nx < 2 || nx > 98) nx = Math.max(2, Math.min(98, nx))
            if (ny < 2 || ny > 98) ny = Math.max(2, Math.min(98, ny))

            if (getSeededRandom() < 0.35 && decayedPheros.length < 800) {
                const pType = ant.carriedItem !== 'NONE' ? 'FOOD' : (ant.job === 'GUARDING' ? 'ALARM' : 'HOME')
                decayedPheros.push({
                    id: `ph_${nextTick}_${ant.id}`,
                    x: nx,
                    y: ny,
                    intensity: pType === 'FOOD' ? 1.0 : (pType === 'ALARM' ? 0.9 : 0.6),
                    type: pType,
                    colonyId: ant.colonyId
                })
            }

            return {
                ...ant,
                x: nx,
                y: ny,
                heading,
                energy: Math.max(10, ant.energy - 0.005),
                ageInDays: (ant.ageInDays || 10) + (1.0 / 86400.0), // precise day progression (1 day = 86400s)
            }
        })

        if (state.autoCheckpoint && nextTick > 0 && nextTick % state.autoCheckpointInterval === 0) {
            state.createCheckpoint(`Auto-Checkpoint #${nextTick}`)
        }

        // Generate rich, varied biological & ecological simulation events
        const rand = getSeededRandom()
        if (updatedAnts.length > 0) {
            const sampleAnt = updatedAnts[Math.floor(rand * updatedAnts.length)]
            const antShortId = sampleAnt.id.replace('ant_', '').slice(0, 8)

            if (nextTick % 2 === 0) {
                if (rand < 0.18) {
                    const foodAmt = (0.3 + getSeededRandom() * 1.2).toFixed(2)
                    state.addEventLog({
                        level: 'INFO',
                        category: 'FORAGING',
                        simTimeFormatted: nextSimTimeFormatted,
                        message: `🍓 [${nextSimTimeFormatted}] ${sampleAnt.species} (#${antShortId}): Récolte de ${foodAmt}mg de miellat ramenée au grenier (X:${sampleAnt.x.toFixed(1)}m, Z:${sampleAnt.y.toFixed(1)}m).`
                    })
                } else if (rand < 0.35) {
                    state.addEventLog({
                        level: 'DEBUG',
                        category: 'PHEROMONE',
                        simTimeFormatted: nextSimTimeFormatted,
                        message: `🧪 [${nextSimTimeFormatted}] Piste de recrutement d'attraction (${sampleAnt.colonyName}) tracée à (X:${sampleAnt.x.toFixed(1)}, Z:${sampleAnt.y.toFixed(1)}).`
                    })
                } else if (rand < 0.52) {
                    const larvaCount = Math.floor(1 + getSeededRandom() * 4)
                    state.addEventLog({
                        level: 'INFO',
                        category: 'NURSERY',
                        simTimeFormatted: nextSimTimeFormatted,
                        message: `🍼 [${nextSimTimeFormatted}] Soins du Couvain : Une ouvrière nourrice a alimenté ${larvaCount} larves dans la Nurserie principale.`
                    })
                } else if (rand < 0.68) {
                    state.addEventLog({
                        level: 'INFO',
                        category: 'QUEEN',
                        simTimeFormatted: nextSimTimeFormatted,
                        message: `👑 [${nextSimTimeFormatted}] Ponte Royale : La Reine fondatrice (${sampleAnt.species}) a pondu 2 nouveaux œufs dans la Chambre Royale.`
                    })
                } else if (rand < 0.82) {
                    state.addEventLog({
                        level: 'VERBOSE',
                        category: 'EXCAVATION',
                        simTimeFormatted: nextSimTimeFormatted,
                        message: `⛏️ [${nextSimTimeFormatted}] Excavation : Paroi de galerie consolidée (+0.3m) par les ouvrières mineuses.`
                    })
                } else if (rand < 0.94) {
                    state.addEventLog({
                        level: 'DEBUG',
                        category: 'PATROL',
                        simTimeFormatted: nextSimTimeFormatted,
                        message: `🛡️ [${nextSimTimeFormatted}] Sentinelle : Soldat en ronde de surveillance aux abords du dôme extérieur.`
                    })
                }
            }
        }

        const updatedStats = {
            totalPopulation: updatedAnts.length,
            totalWorkers: updatedAnts.filter(a => a.caste === 'WORKER').length,
            totalSoldiers: updatedAnts.filter(a => a.caste === 'SOLDIER').length,
            totalFood: Math.max(10, 430 + Math.floor(Math.sin(nextTick * 0.05) * 30)),
        }

        // If an ant is currently selected in the Inspector, update its live position/health/energy
        let updatedSelectedEntity = state.selectedEntity
        if (state.selectedEntity && state.selectedEntity.id) {
            const found = updatedAnts.find(a => a.id === state.selectedEntity.id)
            if (found) updatedSelectedEntity = found
        }

        set({
            tick: nextTick,
            simSeconds: nextSimSeconds,
            simTimeFormatted: nextSimTimeFormatted,
            ants: updatedAnts,
            pheromones: decayedPheros,
            stats: updatedStats,
            selectedEntity: updatedSelectedEntity,
        })
    },

    play: () => {
        const { ws, localTickInterval, speed } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PLAY' }))

        if (localTickInterval) clearInterval(localTickInterval)

        // 1:1 real-time default: at 1.0x, 1 tick per 1000ms (1s sim = 1s real)
        // Rate increases smoothly with speed (e.g. 2.0x -> 500ms, 10.0x -> 100ms, 20.0x -> 50ms)
        const intervalMs = Math.max(20, Math.floor(1000 / (speed || 1.0)))
        const newInterval = setInterval(() => {
            if (get().running) {
                get().stepSimulationTick()
            }
        }, intervalMs)

        set({ running: true, localTickInterval: newInterval })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            simTimeFormatted: get().simTimeFormatted,
            message: `▶ LANCEMENT DE SIMULATION (Tick #${get().tick}, Vitesse: ${(speed || 1.0).toFixed(1)}x) - Moteur 1:1 temps réel actif.`
        })
    },

    pause: () => {
        const { ws, localTickInterval } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PAUSE' }))
        if (localTickInterval) clearInterval(localTickInterval)

        set({ running: false, localTickInterval: null })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            simTimeFormatted: get().simTimeFormatted,
            message: `⏸️ SIMULATION INTERROMPUE / PAUSE (Tick #${get().tick}).`
        })
    },

    setSpeed: (newSpeed) => {
        const { ws, running } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'SPEED', speed: newSpeed }))
        set({ speed: newSpeed })

        if (running) {
            get().play()
        }

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            simTimeFormatted: get().simTimeFormatted,
            message: `⚡ Vitesse de simulation ajustée à ${newSpeed.toFixed(1)}x (1s réelle = ${newSpeed.toFixed(1)}s simulées)`
        })
    },
}))
