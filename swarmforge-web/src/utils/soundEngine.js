/**
 * SoundEngine.js - High-Fidelity Audio Manager & Sound Bank Player for SwarmForge Web.
 * Directly utilizes real bio-acoustic recordings and environmental audio files (1:1 with SimulationAudioManager.java):
 * - Biome Ambiance: Natural bird songs, forest wind, desert breeze, nocturnal crickets.
 * - River & Water: Rushing stream flows, gentle water ripples, splashes.
 * - Weather & Storms: Real howling gale winds, light/heavy rain loops, delayed thunder strikes.
 * - Insect & Nest Activity: Colony bio-acoustics, ant mandibles, soil digging, queen activity.
 */

const SOUND_BANK = {
    // 1. Biome & Ambient
    ambientDay: '/sounds/mixkit-morning-birds-2472.wav',
    ambientForest: '/sounds/Bourne_woods_windy_2020-05-05_0753.mp3',
    ambientDesert: '/sounds/desert_wind_ambient.mp3',
    ambientNight: '/sounds/ElevenLabs_Ambiance_nocturne_animée,_grillons_qui_chantent_et_lucioles_qui_brillent_dans_le_noir.mp3',
    ambientJungle: '/sounds/mixkit-night-forest-with-insects-2414.wav',

    // 2. River & Water
    riverStream: '/sounds/WATRFlow_Small stream 4 (ID 1354)_BigSoundBank.com.mp3',
    riverFlow: '/sounds/mixkit-river-water-flow-and-surroundings-2452.wav',
    waterSplash: '/sounds/water_splash2.ogg',

    // 3. Weather & Wind
    rainLight: '/sounds/mixkit-light-rain-loop-2393.wav',
    rainHeavy: '/sounds/mixkit-heavy-rain-2403.wav',
    windBreeze: '/sounds/soft_wind_leaves.mp3',
    windHowl: '/sounds/strong_howling_wind.mp3',
    windGust: '/sounds/wind_gust_leaves.mp3',
    leavesRustle: '/sounds/dry_leaves_rustling.mp3',
    thunder1: '/sounds/mixkit-thunder-strike-in-storm-2405.wav',
    thunder2: '/sounds/THUN_Thunder 2 (ID 3113)_BigSoundBank.com.mp3',
    thunder3: '/sounds/THUN_Thunder 3 (ID 3114)_BigSoundBank.com.mp3',

    // 4. Insect & Colony
    colonyActivity: '/sounds/ant_colony_activity.mp3',
    anthillNest: '/sounds/anthill_nest_sounds.mp3',
    soilDigging: '/sounds/sand_soil_digging.mp3',
    queenCare: '/sounds/queen-ants-sound.mp3',
    termites: '/sounds/termites-and-ants-sound.mp3',
}

class SimulationAudioPlayer {
    constructor() {
        this.isInitialized = false
        this.ctx = null
        this.masterGain = null
        this.muted = false
        this.simRunning = false
        this.speed = 1.0

        this.volumes = {
            master: 0.70,
            ambient: 0.70,
            river: 0.60,
            weather: 0.65,
            insect: 0.60,
        }

        this.enabled = {
            ambient: true,
            river: true,
            weather: true,
            insect: true,
        }

        this.isDay = true
        this.lightLevel = 1.0
        this.rainIntensity = 0
        this.windSpeed = 2.5

        // HTML5 Audio Channels for smooth persistent playback
        this.channels = {
            ambient: null,
            river: null,
            weatherRain: null,
            weatherWind: null,
            insect: null,
            digging: null,
        }

        this.oneShotSounds = []
        this.rustleTimer = null
    }

    init() {
        if (this.isInitialized) return
        try {
            const AudioCtx = window.AudioContext || window.webkitAudioContext
            if (AudioCtx) {
                this.ctx = new AudioCtx()
                this.masterGain = this.ctx.createGain()
                this.masterGain.gain.setValueAtTime(this.volumes.master, this.ctx.currentTime)
                this.masterGain.connect(this.ctx.destination)
            }
        } catch (e) {
            console.warn('[AudioEngine] WebAudio context init fallback:', e)
        }

        // Initialize persistent channel Audio elements
        this.channels.ambient = this._createAudioChannel(SOUND_BANK.ambientDay, true)
        this.channels.river = this._createAudioChannel(SOUND_BANK.riverStream, true)
        this.channels.weatherRain = this._createAudioChannel(SOUND_BANK.rainLight, true)
        this.channels.weatherWind = this._createAudioChannel(SOUND_BANK.windBreeze, true)
        this.channels.insect = this._createAudioChannel(SOUND_BANK.colonyActivity, true)
        this.channels.digging = this._createAudioChannel(SOUND_BANK.soilDigging, true)

        this.isInitialized = true
        this._updateChannelVolumes()
    }

    _createAudioChannel(src, loop = true) {
        if (typeof window === 'undefined' || typeof Audio === 'undefined') return null
        try {
            const audio = new Audio(src)
            audio.loop = loop
            audio.preload = 'auto'
            audio.volume = 0
            return audio
        } catch {
            return null
        }
    }

    async ensureContext() {
        if (!this.isInitialized) {
            this.init()
        }
        if (this.ctx && this.ctx.state === 'suspended') {
            await this.ctx.resume().catch(() => {})
        }
    }

    _updateChannelVolumes() {
        if (!this.isInitialized) return

        const master = (this.muted || !this.simRunning) ? 0 : this.volumes.master

        // Ambient Channel
        if (this.channels.ambient) {
            const targetVol = (this.enabled.ambient && this.simRunning) ? (master * this.volumes.ambient) : 0
            this._fadeVolume(this.channels.ambient, targetVol)
        }

        // River Channel
        if (this.channels.river) {
            const targetVol = (this.enabled.river && this.simRunning) ? (master * this.volumes.river) : 0
            this._fadeVolume(this.channels.river, targetVol)
        }

        // Weather Rain Channel
        if (this.channels.weatherRain) {
            const hasRain = this.rainIntensity > 0.1
            const rainScale = Math.min(1.0, this.rainIntensity / 25.0)
            const targetVol = (this.enabled.weather && this.simRunning && hasRain) ? (master * this.volumes.weather * (0.3 + 0.7 * rainScale)) : 0
            this._fadeVolume(this.channels.weatherRain, targetVol)
        }

        // Weather Wind Channel
        if (this.channels.weatherWind) {
            const windScale = Math.min(1.5, Math.max(0.2, this.windSpeed / 5.0))
            const targetVol = (this.enabled.weather && this.simRunning) ? (master * this.volumes.weather * 0.7 * windScale) : 0
            this._fadeVolume(this.channels.weatherWind, targetVol)
        }

        // Insect Activity Channel
        if (this.channels.insect) {
            const targetVol = (this.enabled.insect && this.simRunning) ? (master * this.volumes.insect) : 0
            this._fadeVolume(this.channels.insect, targetVol)
        }

        // Digging Channel
        if (this.channels.digging) {
            const targetVol = (this.enabled.insect && this.simRunning) ? (master * this.volumes.insect * 0.45) : 0
            this._fadeVolume(this.channels.digging, targetVol)
        }
    }

    _fadeVolume(audio, targetVolume, durationMs = 200) {
        if (!audio) return
        const clampedTarget = Math.max(0, Math.min(1, targetVolume))
        
        if (clampedTarget > 0 && audio.paused) {
            audio.play().catch(() => {})
        }

        const startVol = audio.volume
        const diff = clampedTarget - startVol
        if (Math.abs(diff) < 0.02) {
            audio.volume = clampedTarget
            if (clampedTarget === 0 && !audio.paused) {
                audio.pause()
            }
            return
        }

        const steps = 10
        const stepTime = durationMs / steps
        let currentStep = 0

        const interval = setInterval(() => {
            currentStep++
            const currentVol = startVol + diff * (currentStep / steps)
            audio.volume = Math.max(0, Math.min(1, currentVol))

            if (currentStep >= steps) {
                clearInterval(interval)
                audio.volume = clampedTarget
                if (clampedTarget === 0 && !audio.paused) {
                    audio.pause()
                }
            }
        }, stepTime)
    }

    updateSimulationState(arg1, arg2, arg3) {
        let simRunning = false
        let speed = 1.0

        if (typeof arg1 === 'object' && arg1 !== null) {
            simRunning = Boolean(arg1.simRunning)
            speed = typeof arg1.speed === 'number' ? arg1.speed : 1.0
            if (arg1.isDay !== undefined) this.isDay = arg1.isDay
            if (arg1.lightLevel !== undefined) this.lightLevel = arg1.lightLevel
            if (arg1.windSpeed !== undefined) this.windSpeed = arg1.windSpeed
        } else {
            simRunning = Boolean(arg1)
            speed = typeof arg2 === 'number' ? arg2 : 1.0
        }

        this.simRunning = Boolean(simRunning && speed > 0)
        this.speed = speed

        // Ambient track selection based on Day / Night
        if (this.channels.ambient) {
            const expectedSrc = (this.isDay || this.lightLevel >= 0.25) ? SOUND_BANK.ambientDay : SOUND_BANK.ambientNight
            if (!this.channels.ambient.src.includes(encodeURI(expectedSrc).split('/').pop())) {
                this.channels.ambient.src = expectedSrc
                if (this.simRunning && this.enabled.ambient && !this.muted) {
                    this.channels.ambient.play().catch(() => {})
                }
            }
        }

        this._updateChannelVolumes()
        this._scheduleRandomLeavesRustle()
    }

    _scheduleRandomLeavesRustle() {
        if (this.rustleTimer) clearTimeout(this.rustleTimer)
        if (!this.simRunning || !this.enabled.weather || this.muted) return

        const delay = 6000 + Math.random() * 12000
        this.rustleTimer = setTimeout(() => {
            if (this.simRunning && this.enabled.weather && !this.muted) {
                this.playOneShot(SOUND_BANK.leavesRustle, 0.4 * this.volumes.master * this.volumes.weather)
            }
            this._scheduleRandomLeavesRustle()
        }, delay)
    }

    updateRainSound(intensity) {
        this.rainIntensity = Math.max(0, Number(intensity) || 0)
        if (this.channels.weatherRain) {
            const isHeavy = this.rainIntensity > 15
            const rainSrc = isHeavy ? SOUND_BANK.rainHeavy : SOUND_BANK.rainLight
            if (!this.channels.weatherRain.src.includes(encodeURI(rainSrc).split('/').pop())) {
                this.channels.weatherRain.src = rainSrc
            }
        }
        this._updateChannelVolumes()
    }

    triggerThunder(distanceMeters = 50) {
        if (!this.enabled.weather || this.muted || !this.simRunning) return
        const sounds = [SOUND_BANK.thunder1, SOUND_BANK.thunder2, SOUND_BANK.thunder3]
        const choice = sounds[Math.floor(Math.random() * sounds.length)]
        
        // Speed of sound delay
        const delayMs = Math.max(0, Math.min(2500, Math.round((distanceMeters / 343.0) * 1000.0)))
        const attenuation = 1.0 / (1.0 + Math.pow(distanceMeters / 150.0, 1.5))

        setTimeout(() => {
            this.playOneShot(choice, this.volumes.master * this.volumes.weather * attenuation)
        }, delayMs)
    }

    playOneShot(src, volume = 0.5) {
        if (this.muted || volume <= 0.01) return
        try {
            const audio = new Audio(src)
            audio.volume = Math.max(0, Math.min(1, volume))
            audio.play().catch(() => {})
        } catch {}
    }

    // --- 1:1 Controls with Desktop SimulationAudioManager.java ---
    setMasterVolume(val) {
        const v = Math.max(0, Math.min(1, Number(val) || 0))
        this.volumes.master = v
        if (this.muted && v > 0) {
            this.muted = false
        }
        this._updateChannelVolumes()
    }

    setAmbientEnabled(enabled) {
        this.enabled.ambient = Boolean(enabled)
        this._updateChannelVolumes()
    }

    setRiverEnabled(enabled) {
        this.enabled.river = Boolean(enabled)
        this._updateChannelVolumes()
    }

    setWeatherEnabled(enabled) {
        this.enabled.weather = Boolean(enabled)
        this._updateChannelVolumes()
    }

    setInsectEnabled(enabled) {
        this.enabled.insect = Boolean(enabled)
        this._updateChannelVolumes()
    }

    setChannelVolume(channel, val) {
        if (this.volumes[channel] !== undefined) {
            this.volumes[channel] = Math.max(0, Math.min(1, Number(val) || 0))
            this._updateChannelVolumes()
        }
    }

    toggleMute() {
        this.muted = !this.muted
        this._updateChannelVolumes()
        return this.muted
    }

    resumeAmbient() {
        this.updateSimulationState({ simRunning: true, speed: 1.0 })
    }

    pauseAmbient() {
        this.updateSimulationState({ simRunning: false, speed: 0.0 })
    }

    setAmbientVolume(val) {
        this.setChannelVolume('ambient', val)
    }
}

export const soundEngine = new SimulationAudioPlayer()
