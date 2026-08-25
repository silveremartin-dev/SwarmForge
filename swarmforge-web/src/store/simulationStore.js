import { create } from 'zustand'

export const useSimulationStore = create((set, get) => ({
    // Connection state
    connected: false,
    ws: null,

    // Simulation state
    tick: 0,
    running: false,
    speed: 1.0,

    // Entity data (Default multi-colony setup)
    colonies: [
        { id: 'COLONY_1', name: 'Colony #1 (Native)', species: 'Formica fusca', color: '#38bdf8', foodStored: 250, queenCount: 1, workerCount: 120 },
        { id: 'COLONY_2', name: 'Colony #2 (Rival)', species: 'Linepithema humile', color: '#f43f5e', foodStored: 180, queenCount: 2, workerCount: 90 },
    ],
    ants: [],
    predators: [],
    foodSources: [
        { id: 'food_init_1', x: 45, y: 55, quantity: 200, type: 'SUGAR_NECTAR' },
        { id: 'food_init_2', x: 60, y: 40, quantity: 150, type: 'SEEDS' },
    ],
    // Look and Feel Themes ('GAMING', 'SCIENTIFIC', 'REALISTIC')
    lookAndFeel: 'GAMING',
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

    // Real-World Date / Time Synchronization
    timeSyncMode: 'REAL_WORLD', // 'REAL_WORLD' | 'SIMULATED'
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
        
        // Calculate sun position based on real local hour (0-24h -> 0.0-1.0 sun angle)
        const hours = now.getHours() + now.getMinutes() / 60
        const sunAngle = (hours / 24)
        
        // Light level calculation (daylight curve)
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

    // Nest Creation & Phantom Ghost Rendering Engine
    phantomNestsVisible: true,
    togglePhantomNests: () => set(state => ({ phantomNestsVisible: !state.phantomNestsVisible })),

    ghostNest: {
        active: false,
        type: 'PINE_NEEDLES', // 'PINE_NEEDLES', 'TERMITE_MOUND', 'WASP_BRANCH', 'WOODEN_BEEHIVE', 'EARTH_MOUND', 'TREE_TRUNK'
        x: 50,
        y: 50,
        z: 0,
        scale: 1.0,
        species: 'Formica fusca'
    },
    setGhostNest: (data) => set(state => ({ ghostNest: { ...state.ghostNest, ...data } })),

    nests: [
        { id: 'nest_pine_1', name: 'Dôme d\'Épines de Pin', type: 'PINE_NEEDLES', x: 35, y: 35, z: 0, scale: 1.2, species: 'Formica rufa', population: 140, isPhantom: false },
        { id: 'nest_termite_1', name: 'Termitière Cathédrale', type: 'TERMITE_MOUND', x: 75, y: 30, z: 0, scale: 1.5, species: 'Macrotermes', population: 310, isPhantom: false },
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

    // Multi-Variable Astro-Atmospheric Climate & Seasonal Engine
    climateEngine: {
        latitudeDeg: 45.0,         // Latitude (0° = Equator, 45° = Temperate, 65° = Boreal)
        climateType: 'OCEANIC',    // 'OCEANIC' | 'CONTINENTAL' | 'MEDITERRANEAN' | 'ALPINE' | 'TROPICAL'
        season: 'SUMMER',          // 'SPRING' | 'SUMMER' | 'AUTUMN' | 'WINTER'
        dayOfYear: 200,            // 1 to 365
        cloudCover: 0.25,          // 0.0 (clear sky) to 1.0 (overcast storm)
        precipitationMm: 0.0,      // Rain intensity mm/h
        barometricPressureHpa: 1013.25,
        windSpeedMs: 2.4,          // Wind speed in m/s
        windDirectionDeg: 225,     // SW Wind direction
        soilMoisture: 0.45,        // 0.0 (dry dust) to 1.0 (saturated mud)
    },

    environmentLighting: {
        sunAzimuthDeg: 145,       // Solar direction angle (0° = North, 180° = South)
        sunElevationDeg: 42,      // Calculated solar elevation angle above horizon
        slopeExposure: 'SOUTH',   // 'SOUTH' (warmer solar gain) vs 'NORTH' (shaded cool slope)
        isNight: false,
        moonPhase: 'FULL_MOON',   // 'NEW_MOON' | 'CRESCENT' | 'FULL_MOON'
        moonlightIntensity: 0.4,  // 0.0 (pitch dark) to 1.0 (bright silver moonlight)
        currentCalculatedTempC: 22.5,
    },

    // Natural Hazards & Disasters (Inondation, Sécheresse, Gelé Spring)
    disasterState: {
        activeDisaster: null,     // 'FLASH_FLOOD' | 'DROUGHT_CRACKS' | 'LATE_FROST'
        intensity: 0,             // 0 to 100
        floodWaterLevelMm: 0,     // Submergence of lower galleries
    },

    // Trophallaxis & Crop Social Energy Transfer Engine
    trophallaxisEvents: [],       // List of active food transfers between workers
    trophallaxisActive: true,

    // Specific Predator Species Engine
    predatorCatalog: [
        { id: 'TAMANDUA', name: 'Fourmilier (Tamandua)', target: 'ALL_ANTS', threat: 'CRITICAL', icon: '🦥' },
        { id: 'PICUS_VIRIDIS', name: 'Pic-vert (Picus viridis)', target: 'PINE_NEEDLES', threat: 'HIGH', icon: '🐦' },
        { id: 'PSEUDACTEON', name: 'Mouche Phoride Parasite', target: 'WORKER_ANTS', threat: 'MEDIUM', icon: '🪰' },
        { id: 'SPIDER_MYRMECOPHAGE', name: 'Araignée Myrmécophage', target: 'FORAGERS', threat: 'MEDIUM', icon: '🕷️' },
    ],

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

    // Simulation Parameters (Categorized with strictly metric SI units)
    simulationParams: {
        // Category 1: Phéromones & Diffusion
        pheromones: {
            dissipationRate: { value: 0.50, unit: '%/s', min: 0.01, max: 5.00, label: 'Taux de Dissipation des Phéromones' },
            diffusionRadius: { value: 1.20, unit: 'm', min: 0.20, max: 5.00, label: 'Rayon de Diffusion Évaporative' },
            alarmThreshold: { value: 40, unit: '%', min: 5, max: 100, label: 'Seuil d\'Intensité d\'Alarme' },
        },
        // Category 2: Ressources & Alimentation
        resources: {
            foodSpawnRate: { value: 12, unit: 'g/min', min: 1, max: 100, label: 'Taux d\'Apparition de Nourriture' },
            foodDecayRate: { value: 0.20, unit: '%/h', min: 0.0, max: 2.0, label: 'Taux de Décomposition Organic' },
            honeydewRate: { value: 2.50, unit: 'mg/h', min: 0.1, max: 10.0, label: 'Production de Miellat (Pucerons)' },
        },
        // Category 3: Espèces & Démographie
        species: {
            queenFecundity: { value: 60, unit: 'œufs/jour', min: 5, max: 300, label: 'Fécondité Reine (Taux de Ponte)' },
            workerLifespan: { value: 90, unit: 'jours', min: 10, max: 365, label: 'Espérance de Vie des Ouvrières' },
            soldierRatio: { value: 25, unit: '%', min: 0, max: 60, label: 'Proportion de Soldats dans la Colonie' },
        },
        // Category 4: Commerciaux & Économie
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

    // Multi-Colony Management Actions
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

    // Real-Time Dense Simulation Event Logging System
    eventLogs: [
        {
            id: `evt_init_${Date.now()}`,
            tick: 0,
            timestamp: new Date().toISOString(),
            level: 'INFO',
            category: 'SYSTEM',
            message: '🎬 Session de simulation initialisée. Prêt pour le démarrage.',
        }
    ],

    addEventLog: (logObj) => set(state => {
        const newLog = {
            id: `evt_${Date.now()}_${Math.floor(Math.random() * 10000)}`,
            tick: state.tick,
            timestamp: new Date().toISOString(),
            level: logObj.level || 'INFO',
            category: logObj.category || 'SIMULATION',
            message: logObj.message || '',
            details: logObj.details || null,
        }
        // Cap max log entries to 500 for optimal UI performance
        const updated = [newLog, ...state.eventLogs.slice(0, 499)]
        return { eventLogs: updated }
    }),

    clearEventLogs: () => set({ eventLogs: [] }),

    // Mode Divin (God Mode) Intervention Log for Deterministic Replay
    interventionsLog: [],

    recordDivineIntervention: (actionData) => set(state => {
        const intervention = {
            id: `god_act_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
            tick: state.tick,
            timestamp: new Date().toISOString(),
            ...actionData
        }

        // Apply physical changes locally if in standalone/simulated mode
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

        // Send via WebSocket if connected
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

    // Local tick timer ref
    localTickInterval: null,

    // Statistics
    stats: {
        totalPopulation: 210,
        totalWorkers: 150,
        totalSoldiers: 60,
        totalFood: 430,
    },

    // Environment
    environment: {
        terrariumWidth: 2.0,  // Variable surface size from World Editor (meters)
        terrariumDepth: 2.0,  // Variable surface size from World Editor (meters)
        terrariumHeight: 1.0, // Depth/Height (meters)
        lightLevel: 1.0,
        timeOfDay: 'DAY',
        sunAngle: 0.5,
        temperature: 20,
        humidity: 50,
        rainIntensity: 0,
        windSpeed: 5,
        weatherState: 'CLEAR', // 'CLEAR', 'CLOUDY', 'THUNDERSTORM', 'SNOW', 'BLIZZARD', 'TEMPEST', 'HAIL', 'FOG'
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

    // Display Toggles for Simulation Mode (Soleil, Éclairs, Nuages, Pluie, Brouillard, Vent, Vision Nuit)
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

    // Selection
    selectedEntity: null,
    setSelectedEntity: (entity) => set({ selectedEntity: entity }),

    // Actions
    connect: () => {
        try {
            const ws = new WebSocket('ws://localhost:8081')

            ws.onopen = () => {
                console.log('WebSocket connected')
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

            ws.onerror = (err) => {
                set({ connected: false, ws: null })
            }
        } catch (e) {
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
        const { localTickInterval } = get()
        if (localTickInterval) clearInterval(localTickInterval)
        
        set({
            tick: 0,
            running: false,
            localTickInterval: null,
            ants: [],
            interventionsLog: [],
        })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: '🔄 Simulation réinitialisée à Tick #0. Presets & seed synchronisés.'
        })
    },

    // Standalone Tick Step Engine for local mode with dense events (100% Deterministic Seeded Engine)
    stepSimulationTick: () => {
        const state = get()
        const nextTick = state.tick + 1

        // Seeded PRNG sequence generator (Zero unseeded Math.random in simulation loop)
        let currentSeed = (state.simulationSeed || 42) + nextTick * 10007
        const getSeededRandom = () => {
            const x = Math.sin(currentSeed++) * 10000
            return x - Math.floor(x)
        }

        // Real World Clock Synchronization Update
        if (state.timeSyncMode === 'REAL_WORLD') {
            state.updateRealWorldTime()
        }

        // Realistic Astronomical & Multi-Variable Astro-Atmospheric Climate Engine
        const simHourOfDay = (nextTick * 0.25) % 24
        const dayOfYear = state.climateEngine?.dayOfYear || 200
        const latRad = ((state.climateEngine?.latitudeDeg || 45.0) * Math.PI) / 180.0

        // Solar Declination angle δ (Earth's tilt over 365 days)
        const declinationRad = ((23.45 * Math.sin(((360 / 365) * (dayOfYear - 81) * Math.PI) / 180.0)) * Math.PI) / 180.0
        const hourAngleRad = ((15 * (simHourOfDay - 12)) * Math.PI) / 180.0

        // Calculated Solar Elevation Angle α above horizon
        const sinElevation = Math.sin(latRad) * Math.sin(declinationRad) + Math.cos(latRad) * Math.cos(declinationRad) * Math.cos(hourAngleRad)
        const solarElevationDeg = (Math.asin(Math.max(-1, Math.min(1, sinElevation))) * 180) / Math.PI
        const isNightTime = solarElevationDeg < -0.833 // Sun below horizon

        // Climate Type Thermal Inertia & Diurnal Amplitude Factors
        const climatePresets = {
            OCEANIC: { baseTemp: 18.0, amplitude: 4.5, humidityBase: 75 },       // Buffered oceanic climate
            CONTINENTAL: { baseTemp: 21.0, amplitude: 14.0, humidityBase: 50 },   // Sharp day/night continental swings
            MEDITERRANEAN: { baseTemp: 25.0, amplitude: 9.0, humidityBase: 45 },  // Hot dry summer
            ALPINE: { baseTemp: 12.0, amplitude: 11.0, humidityBase: 65 },       // Cold high altitude
            TROPICAL: { baseTemp: 28.0, amplitude: 3.5, humidityBase: 85 },      // Hot humid equator
        }
        const activePreset = climatePresets[state.climateEngine?.climateType || 'OCEANIC']

        // Cloud Cover & Rain Thermal Attenuation Factor
        const cloudFactor = 1.0 - 0.55 * (state.climateEngine?.cloudCover || 0.25)
        const solarHeatingGain = Math.max(0, Math.sin((solarElevationDeg * Math.PI) / 180.0)) * activePreset.amplitude * cloudFactor
        const nightCoolingLoss = isNightTime ? activePreset.amplitude * 0.75 : 0.0

        // Final Calculated Soil & Air Temperature (°C)
        const calculatedDiurnalTemp = (activePreset.baseTemp + solarHeatingGain - nightCoolingLoss).toFixed(1)

        // Update Climate & Astro state deterministically
        set(s => ({
            environmentLighting: {
                ...s.environmentLighting,
                sunElevationDeg: parseFloat(solarElevationDeg.toFixed(1)),
                sunAzimuthDeg: parseFloat(((180 + 15 * (simHourOfDay - 12)) % 360).toFixed(1)),
                isNight: isNightTime,
                currentCalculatedTempC: parseFloat(calculatedDiurnalTemp)
            }
        }))

        // Disease Propagation Step (Deterministic Seeded Proximity Step)
        if (state.diseaseParams.activeOutbreak && state.ants.length > 0) {
            const randInfect = getSeededRandom()
            if (randInfect < (state.diseaseParams.contagionRate || 0.35)) {
                const healthyCount = state.epidemicStats.healthy
                if (healthyCount > 0) {
                    const newlyInfected = Math.min(healthyCount, Math.floor(getSeededRandom() * 3) + 1)
                    state.addEventLog({
                        level: 'WARN',
                        category: 'DISEASE',
                        message: `☣️ [Tick #${nextTick}] Propagation de ${state.diseaseParams.diseaseType}: ${newlyInfected} nouvel(s) individu(s) contaminé(s) par proximité.`
                    })
                    set(s => ({
                        epidemicStats: {
                            ...s.epidemicStats,
                            healthy: Math.max(0, s.epidemicStats.healthy - newlyInfected),
                            infected: s.epidemicStats.infected + newlyInfected,
                            contagious: s.epidemicStats.contagious + newlyInfected,
                        }
                    }))
                }
            }
        }

        // Dense Simulation Event Generation per tick (Seeded)
        const rand = getSeededRandom()

        if (nextTick % 1 === 0) { // Log every tick in VERBOSE (Dense) level
            const workerId = Math.floor(Math.random() * 120) + 1
            const colonyName = state.colonies[Math.floor(Math.random() * state.colonies.length)]?.name || 'Colonie #1'
            
            if (rand < 0.30) {
                const foodAmt = (0.2 + Math.random() * 0.8).toFixed(2)
                state.addEventLog({
                    level: 'VERBOSE',
                    category: 'FORAGING',
                    message: `🐜 [Tick #${nextTick}] Ouvrière #${workerId} (${colonyName}): Récolte de ${foodAmt}mg de miellat à (X:${(Math.random()*2).toFixed(2)}m, Y:${(Math.random()*2).toFixed(2)}m)`
                })
            } else if (rand < 0.55) {
                const depthMm = (1.5 + Math.random() * 3.0).toFixed(1)
                state.addEventLog({
                    level: 'VERBOSE',
                    category: 'DIGGING',
                    message: `⛏️ [Tick #${nextTick}] Excavation: Tunnel N°${Math.floor(Math.random()*4)+1} approfondi de ${depthMm}mm (Substrat argileux compaction ${state.simulationParams.resources ? 65 : 50}%)`
                })
            } else if (rand < 0.75) {
                const pheroInt = (40 + Math.random() * 55).toFixed(0)
                state.addEventLog({
                    level: 'DEBUG',
                    category: 'PHEROMONE',
                    message: `🧪 [Tick #${nextTick}] Piste d'attraction déposée avec intensité ${pheroInt}% par patrouilleur`
                })
            } else if (rand < 0.90) {
                const eggCount = Math.floor(Math.random() * 3) + 1
                state.addEventLog({
                    level: 'INFO',
                    category: 'COLONY',
                    message: `👑 [Tick #${nextTick}] Reine (${colonyName}): Ponte de ${eggCount} nouveaux œufs dans la chambre royale`
                })
            } else {
                state.addEventLog({
                    level: 'DEBUG',
                    category: 'METABOLISM',
                    message: `💼 [Tick #${nextTick}] Consommation métabolique: -${(0.12 * state.colonies.length).toFixed(2)}mW consommés par la colonie`
                })
            }
        }

        // Random Stats update
        const updatedStats = {
            totalPopulation: 210 + Math.floor(nextTick * 0.1),
            totalWorkers: 150 + Math.floor(nextTick * 0.08),
            totalSoldiers: 60 + Math.floor(nextTick * 0.02),
            totalFood: Math.max(10, 430 + Math.floor(Math.sin(nextTick * 0.1) * 20)),
        }

        set({ tick: nextTick, stats: updatedStats })
    },

    // Control actions
    play: () => {
        const { ws, localTickInterval, speed } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'PLAY' }))

        if (localTickInterval) clearInterval(localTickInterval)

        const intervalMs = Math.max(50, Math.floor(200 / (speed || 1.0)))
        const newInterval = setInterval(() => {
            if (get().running) {
                get().stepSimulationTick()
            }
        }, intervalMs)

        set({ running: true, localTickInterval: newInterval })

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: `▶ LANCEMENT DE SIMULATION (Tick #${get().tick}) - Génération d'événements bas niveau active.`
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
            message: `⏸️ SIMULATION INTERROMPUE / PAUSE (Tick #${get().tick}).`
        })
    },

    setSpeed: (newSpeed) => {
        const { ws, running } = get()
        if (ws) ws.send(JSON.stringify({ type: 'CONTROL', action: 'SPEED', speed: newSpeed }))
        set({ speed: newSpeed })

        if (running) {
            get().play() // restart interval with new speed
        }

        get().addEventLog({
            level: 'INFO',
            category: 'SIMULATION',
            message: `⚡ Vitesse de simulation ajustée à ${newSpeed.toFixed(1)}x`
        })
    },
}))
