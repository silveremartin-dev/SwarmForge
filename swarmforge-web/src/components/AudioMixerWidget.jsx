import React, { useState, useEffect } from 'react'
import { Volume2, VolumeX, Bird, CloudRain, Bug, Zap, Wind, Waves, BellRing, Sparkles } from 'lucide-react'
import { soundEngine } from '../utils/soundEngine'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

export default function AudioMixerWidget() {
    const [muted, setMuted] = useState(false)
    const [masterVol, setMasterVol] = useState(0.85)
    const [ambianceVol, setAmbianceVol] = useState(0.70)
    const [weatherVol, setWeatherVol] = useState(0.60)
    const [insectsVol, setInsectsVol] = useState(0.55)
    const [diggingVol, setDiggingVol] = useState(0.50)
    const [riverVol, setRiverVol] = useState(0.65)
    const [collapsed, setCollapsed] = useState(false)

    const {
        environment,
        weatherToggles,
        running,
        speed,
        language,
        theme
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    useEffect(() => {
        // Initialize WebAudio synthesizer context
        soundEngine.init()
    }, [])

    // Sync sound engine state (simulation running, day/night light level, speed)
    useEffect(() => {
        soundEngine.updateSimulationState({
            isDay: (environment?.lightLevel ?? 1.0) >= 0.3,
            lightLevel: environment?.lightLevel ?? 1.0,
            simRunning: running,
            speed: speed ?? 1.0,
        })
    }, [running, speed, environment?.lightLevel])

    // Update procedural rain sound dynamically as weather intensity changes
    useEffect(() => {
        if (environment && running && speed > 0) {
            const isRaining = environment.weatherState === 'RAIN' || environment.weatherState === 'TEMPEST' || environment.weatherState === 'THUNDERSTORM'
            const rainIntensity = environment.rainIntensity || (isRaining ? 15 : 0)
            soundEngine.updateRainSound(rainIntensity)
        } else {
            soundEngine.updateRainSound(0)
        }
    }, [environment?.rainIntensity, environment?.weatherState, running, speed])

    // Trigger thunder audio synthesis when storm occurs or lightning is triggered
    useEffect(() => {
        if (weatherToggles?.lightningTrigger > 0 || environment?.weatherState === 'THUNDERSTORM') {
            soundEngine.ensureContext()
            soundEngine.triggerThunder()
        }
    }, [weatherToggles?.lightningTrigger, environment?.weatherState])

    const handleToggleMute = () => {
        const isMuted = soundEngine.toggleMute()
        setMuted(isMuted)
    }

    const handleMasterVolChange = (e) => {
        const val = parseFloat(e.target.value)
        setMasterVol(val)
        soundEngine.setMasterVolume(val)
    }

    const handleChannelVolChange = (channel, setLocalFn, val) => {
        setLocalFn(val)
        soundEngine.setChannelVolume(channel, val)
    }

    const handleTestSound = (type) => {
        soundEngine.ensureContext()
        if (type === 'BIRD') soundEngine.triggerBirdChirp()
        if (type === 'LEAVES') soundEngine.triggerLeavesRustle()
        if (type === 'CRICKET') soundEngine.triggerNightCricket()
        if (type === 'RAIN') soundEngine.updateRainSound(12)
        if (type === 'THUNDER') soundEngine.triggerThunder()
        if (type === 'STORM') soundEngine.triggerStormGust()
        if (type === 'INSECT') soundEngine.triggerInsectStep()
        if (type === 'DIG') soundEngine.triggerNestDiggingSound()
        if (type === 'RIVER') soundEngine.startRiverAmbiance()
        if (type === 'ALERT') soundEngine.triggerDiseaseOutbreakSound()
    }

    const styles = {
        container: {
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.94)',
            backdropFilter: 'blur(16px)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 12,
            padding: collapsed ? '8px 12px' : '14px',
            color: isDark ? '#f8fafc' : '#0f172a',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
            marginBottom: 10,
            transition: 'all 0.2s ease',
        },
        header: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: 12,
            fontWeight: 700,
            color: isDark ? '#38bdf8' : '#0284c7',
            cursor: 'pointer',
        },
        channelRow: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 8,
            fontSize: 11,
            color: isDark ? '#cbd5e1' : '#334155',
            marginTop: 6,
        },
        slider: {
            flex: 1,
            accentColor: '#38bdf8',
            height: 4,
            cursor: 'pointer',
        },
        btnGroup: {
            display: 'flex',
            gap: 4,
            flexWrap: 'wrap',
            marginTop: 2,
        },
        testBtn: {
            background: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)',
            border: isDark ? '1px solid rgba(255,255,255,0.15)' : '1px solid rgba(0,0,0,0.12)',
            color: isDark ? '#e2e8f0' : '#334155',
            borderRadius: 4,
            padding: '3px 6px',
            fontSize: 9,
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 3,
            transition: 'all 0.15s ease'
        }
    }

    return (
        <div style={styles.container}>
            <div style={styles.header} onClick={() => setCollapsed(!collapsed)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    {muted ? <VolumeX size={15} color="#ef4444" /> : <Volume2 size={15} color="#38bdf8" />}
                    <span>🔊 {t('audioTitle', 'Mixer Audio & Ambiances')}</span>
                </div>
                <span style={{ fontSize: 10, color: isDark ? '#94a3b8' : '#64748b' }}>{collapsed ? '▶ Déplier' : '▼ Réduire'}</span>
            </div>

            {!collapsed && (
                <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {/* Master Volume */}
                    <div style={styles.channelRow}>
                        <button
                            onClick={handleToggleMute}
                            style={{
                                background: muted ? '#ef4444' : '#0284c7',
                                border: 'none',
                                color: '#fff',
                                padding: '4px 8px',
                                borderRadius: 6,
                                fontSize: 10,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 4,
                            }}
                        >
                            {muted ? <VolumeX size={12} /> : <Volume2 size={12} />}
                            <span>{muted ? 'MUET' : 'MASTER'}</span>
                        </button>
                        <input
                            type="range" min="0" max="1" step="0.05"
                            value={masterVol}
                            onChange={handleMasterVolChange}
                            style={styles.slider}
                        />
                        <span style={{ fontSize: 10, width: 28, textAlign: 'right', fontWeight: 700, color: isDark ? '#38bdf8' : '#0284c7' }}>
                            {Math.round(masterVol * 100)}%
                        </span>
                    </div>

                    <div style={{ height: 1, background: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)' }} />

                    {/* Channel 1: Biome & Tree Canopy */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 130, fontSize: 11, color: isDark ? '#38bdf8' : '#0284c7' }}>
                                <Bird size={12} /> {t('biomeAmbience', 'Biome & Faune')}
                            </span>
                            <input
                                type="range" min="0" max="1" step="0.05"
                                value={ambianceVol}
                                onChange={(e) => handleChannelVolChange('ambiance', setAmbianceVol, parseFloat(e.target.value))}
                                style={styles.slider}
                            />
                        </div>
                        <div style={styles.btnGroup}>
                            <button style={styles.testBtn} onClick={() => handleTestSound('BIRD')}>🎵 Oiseaux</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('LEAVES')}>🍃 Feuillage</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('CRICKET')}>🦗 Grillons</button>
                        </div>
                    </div>

                    {/* Channel 2: Weather & Wind */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 130, fontSize: 11, color: '#60a5fa' }}>
                                <CloudRain size={12} /> {t('layerWeather', 'Météo & Vent')}
                            </span>
                            <input
                                type="range" min="0" max="1" step="0.05"
                                value={weatherVol}
                                onChange={(e) => handleChannelVolChange('weather', setWeatherVol, parseFloat(e.target.value))}
                                style={styles.slider}
                            />
                        </div>
                        <div style={styles.btnGroup}>
                            <button style={styles.testBtn} onClick={() => handleTestSound('RAIN')}>🌧️ Pluie</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('THUNDER')}>⚡ Tonnerre</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('STORM')}>💨 Vent</button>
                        </div>
                    </div>

                    {/* Channel 3: Insect Activity & Digging */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 130, fontSize: 11, color: '#f59e0b' }}>
                                <Bug size={12} /> {t('swarmChatter', 'Insectes & Galeries')}
                            </span>
                            <input
                                type="range" min="0" max="1" step="0.05"
                                value={insectsVol}
                                onChange={(e) => handleChannelVolChange('insects', setInsectsVol, parseFloat(e.target.value))}
                                style={styles.slider}
                            />
                        </div>
                        <div style={styles.btnGroup}>
                            <button style={styles.testBtn} onClick={() => handleTestSound('INSECT')}>🐜 Pas Fourmis</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('DIG')}>⛏️ Creusement</button>
                        </div>
                    </div>

                    {/* Channel 4: River & Water Flow */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 130, fontSize: 11, color: '#06b6d4' }}>
                                <Waves size={12} /> Cours d'Eau & Rivière
                            </span>
                            <input
                                type="range" min="0" max="1" step="0.05"
                                value={riverVol}
                                onChange={(e) => handleChannelVolChange('river', setRiverVol, parseFloat(e.target.value))}
                                style={styles.slider}
                            />
                        </div>
                        <div style={styles.btnGroup}>
                            <button style={styles.testBtn} onClick={() => handleTestSound('RIVER')}>🌊 Écoulement</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('ALERT')}>🚨 Alerte Épidémie</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
