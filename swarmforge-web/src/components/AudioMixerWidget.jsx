import React, { useState, useEffect } from 'react'
import { Volume2, VolumeX, Bird, CloudRain, Bug, Waves, CheckSquare, Square } from 'lucide-react'
import { soundEngine } from '../utils/soundEngine'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

/**
 * AudioMixerWidget.jsx - Procedural Sound & Audio Controls
 * Strictly matches SwarmForgeClient.java / SimulationAudioManager.java:
 * - 1 Master Volume Slider (0 - 100%)
 * - 4 Channel Toggle Checkboxes:
 *   1. Biome Ambience (Birds / Fauna)
 *   2. River Water Sound
 *   3. Weather Audio (Rain / Hail / Thunder)
 *   4. Insect Activity & Nest (Digging)
 */
export default function AudioMixerWidget() {
    const [muted, setMuted] = useState(false)
    const [masterVol, setMasterVol] = useState(0.70)
    const [ambientEnabled, setAmbientEnabled] = useState(true)
    const [riverEnabled, setRiverEnabled] = useState(true)
    const [weatherEnabled, setWeatherEnabled] = useState(true)
    const [insectEnabled, setInsectEnabled] = useState(true)
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
        soundEngine.init()
    }, [])

    // Sync simulation runtime state with sound synthesis engine
    useEffect(() => {
        soundEngine.updateSimulationState({
            isDay: (environment?.lightLevel ?? 1.0) >= 0.3,
            lightLevel: environment?.lightLevel ?? 1.0,
            simRunning: running,
            speed: speed ?? 1.0,
        })
    }, [running, speed, environment?.lightLevel])

    // Weather audio dynamics (rain & thunderstorm)
    useEffect(() => {
        if (environment && running && speed > 0 && weatherEnabled) {
            const isRaining = environment.weatherState === 'RAIN' || environment.weatherState === 'TEMPEST' || environment.weatherState === 'THUNDERSTORM'
            const rainIntensity = environment.rainIntensity || (isRaining ? 15 : 0)
            soundEngine.updateRainSound(rainIntensity)
        } else {
            soundEngine.updateRainSound(0)
        }
    }, [environment?.rainIntensity, environment?.weatherState, running, speed, weatherEnabled])

    useEffect(() => {
        if (weatherEnabled && (weatherToggles?.lightningTrigger > 0 || environment?.weatherState === 'THUNDERSTORM')) {
            soundEngine.ensureContext()
            soundEngine.triggerThunder()
        }
    }, [weatherToggles?.lightningTrigger, environment?.weatherState, weatherEnabled])

    const handleToggleMute = () => {
        const isMuted = soundEngine.toggleMute()
        setMuted(isMuted)
    }

    const handleMasterVolChange = (e) => {
        const val = parseFloat(e.target.value)
        setMasterVol(val)
        soundEngine.setMasterVolume(val)
        if (muted && val > 0) {
            setMuted(false)
        }
    }

    const handleToggleAmbient = () => {
        const next = !ambientEnabled
        setAmbientEnabled(next)
        soundEngine.setAmbientEnabled(next)
    }

    const handleToggleRiver = () => {
        const next = !riverEnabled
        setRiverEnabled(next)
        soundEngine.setRiverEnabled(next)
    }

    const handleToggleWeather = () => {
        const next = !weatherEnabled
        setWeatherEnabled(next)
        soundEngine.setWeatherEnabled(next)
    }

    const handleToggleInsect = () => {
        const next = !insectEnabled
        setInsectEnabled(next)
        soundEngine.setInsectEnabled(next)
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
            color: isDark ? '#a78bfa' : '#7c3aed',
            cursor: 'pointer',
        },
        channelRow: {
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            fontSize: 11,
            color: isDark ? '#cbd5e1' : '#334155',
            marginTop: 6,
        },
        slider: {
            flex: 1,
            accentColor: '#a78bfa',
            height: 4,
            cursor: 'pointer',
        },
        checkRow: {
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            fontSize: 11,
            padding: '4px 6px',
            borderRadius: 6,
            cursor: 'pointer',
            userSelect: 'none',
            background: isDark ? 'rgba(255,255,255,0.03)' : 'rgba(0,0,0,0.02)',
            border: isDark ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(0,0,0,0.06)',
            transition: 'all 0.15s ease'
        }
    }

    return (
        <div style={styles.container}>
            {/* Section Header */}
            <div style={styles.header} onClick={() => setCollapsed(!collapsed)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    {muted ? <VolumeX size={15} color="#ef4444" /> : <Volume2 size={15} color="#a78bfa" />}
                    <span>{t('audioSectionTitle', 'Sons :')}</span>
                </div>
                <span style={{ fontSize: 10, color: isDark ? '#94a3b8' : '#64748b' }}>
                    {collapsed ? '▶' : '▼'}
                </span>
            </div>

            {!collapsed && (
                <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {/* Single Master Volume Slider (1:1 with JavaFX SwarmForgeClient.java) */}
                    <div style={styles.channelRow}>
                        <button
                            onClick={handleToggleMute}
                            title={muted ? t('unmute', 'Réactiver le son') : t('mute', 'Couper le son')}
                            style={{
                                background: muted ? '#ef4444' : '#7c3aed',
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
                            <span>{t('audioVolume', 'Volume :')}</span>
                        </button>
                        <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.01"
                            value={muted ? 0 : masterVol}
                            onChange={handleMasterVolChange}
                            style={styles.slider}
                        />
                        <span style={{ fontSize: 10, width: 32, textAlign: 'right', fontWeight: 700, color: isDark ? '#a78bfa' : '#7c3aed' }}>
                            {muted ? '0%' : `${Math.round(masterVol * 100)}%`}
                        </span>
                    </div>

                    <div style={{ height: 1, background: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)' }} />

                    {/* 4 Checkboxes conforming 1:1 to heavy client */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                        {/* 1. Biome Ambience Checkbox */}
                        <div
                            style={styles.checkRow}
                            onClick={handleToggleAmbient}
                            title={t('audioAmbientTt', 'Activer le paysage sonore d\'ambiance naturelle (faune, oiseaux, forêt).')}
                        >
                            {ambientEnabled ? <CheckSquare size={14} color="#a78bfa" /> : <Square size={14} color={isDark ? '#64748b' : '#94a3b8'} />}
                            <Bird size={13} color="#38bdf8" />
                            <span style={{ fontSize: 10.5, fontWeight: ambientEnabled ? 600 : 400, color: ambientEnabled ? (isDark ? '#f8fafc' : '#0f172a') : (isDark ? '#64748b' : '#94a3b8') }}>
                                {t('audioAmbient', 'Ambiance Biome (Oiseaux/Faune)')}
                            </span>
                        </div>

                        {/* 2. River Water Sound Checkbox */}
                        <div
                            style={styles.checkRow}
                            onClick={handleToggleRiver}
                            title={t('audioRiverTt', 'Activer le son du courant d’eau et clapotis de rivière.')}
                        >
                            {riverEnabled ? <CheckSquare size={14} color="#a78bfa" /> : <Square size={14} color={isDark ? '#64748b' : '#94a3b8'} />}
                            <Waves size={13} color="#06b6d4" />
                            <span style={{ fontSize: 10.5, fontWeight: riverEnabled ? 600 : 400, color: riverEnabled ? (isDark ? '#f8fafc' : '#0f172a') : (isDark ? '#64748b' : '#94a3b8') }}>
                                {t('audioRiver', 'Bruit Eau Rivière')}
                            </span>
                        </div>

                        {/* 3. Weather Sound Checkbox */}
                        <div
                            style={styles.checkRow}
                            onClick={handleToggleWeather}
                            title={t('audioWeatherTt', 'Activer les effets sonores météorologiques synchrone.')}
                        >
                            {weatherEnabled ? <CheckSquare size={14} color="#a78bfa" /> : <Square size={14} color={isDark ? '#64748b' : '#94a3b8'} />}
                            <CloudRain size={13} color="#60a5fa" />
                            <span style={{ fontSize: 10.5, fontWeight: weatherEnabled ? 600 : 400, color: weatherEnabled ? (isDark ? '#f8fafc' : '#0f172a') : (isDark ? '#64748b' : '#94a3b8') }}>
                                {t('audioWeather', 'Sons Météo (Pluie/Grêle/Orage)')}
                            </span>
                        </div>

                        {/* 4. Insect & Nest Digging Sound Checkbox */}
                        <div
                            style={styles.checkRow}
                            onClick={handleToggleInsect}
                            title={t('audioInsectTt', 'Activer les sons procéduraux d\'activité des insectes et d\'excavation.')}
                        >
                            {insectEnabled ? <CheckSquare size={14} color="#a78bfa" /> : <Square size={14} color={isDark ? '#64748b' : '#94a3b8'} />}
                            <Bug size={13} color="#f59e0b" />
                            <span style={{ fontSize: 10.5, fontWeight: insectEnabled ? 600 : 400, color: insectEnabled ? (isDark ? '#f8fafc' : '#0f172a') : (isDark ? '#64748b' : '#94a3b8') }}>
                                {t('audioInsect', 'Activité Insectes & Nid (Creusement)')}
                            </span>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
