import React, { useState, useEffect } from 'react';
import { Volume2, VolumeX, Bird, CloudRain, Bug, Zap, Wind } from 'lucide-react';
import { soundEngine } from '../utils/soundEngine';
import { useSimulationStore } from '../store/simulationStore';

export default function AudioMixerWidget() {
    const [muted, setMuted] = useState(false);
    const [masterVol, setMasterVol] = useState(0.85);
    const [ambianceVol, setAmbianceVol] = useState(0.65);
    const [weatherVol, setWeatherVol] = useState(0.60);
    const [insectsVol, setInsectsVol] = useState(0.45);
    const [diggingVol, setDiggingVol] = useState(0.50);
    const [collapsed, setCollapsed] = useState(false);

    const { environment, weatherToggles, isSimulating, playbackSpeed } = useSimulationStore();

    useEffect(() => {
        // Initialize WebAudio context on user interaction or mount
        soundEngine.init();
    }, []);

    // Sync sound engine state (simulation pause, day/night light level)
    useEffect(() => {
        soundEngine.updateSimulationState({
            isDay: environment ? environment.isDaytime : true,
            lightLevel: environment ? environment.lightLevel : 1.0,
            simRunning: isSimulating,
            speed: playbackSpeed,
        });
    }, [isSimulating, playbackSpeed, environment?.isDaytime, environment?.lightLevel]);

    // Update procedural rain sound dynamically as weather intensity changes
    useEffect(() => {
        if (environment && isSimulating && playbackSpeed > 0) {
            soundEngine.updateRainSound(environment.rainIntensity || (environment.weatherState === 'THUNDERSTORM' ? 15 : 0));
        } else {
            soundEngine.updateRainSound(0);
        }
    }, [environment?.rainIntensity, environment?.weatherState, isSimulating, playbackSpeed]);

    // Trigger thunder audio synthesis when storm occurs or lightning is triggered
    useEffect(() => {
        if (weatherToggles?.lightningTrigger > 0) {
            soundEngine.ensureContext();
            soundEngine.triggerThunder();
        }
    }, [weatherToggles?.lightningTrigger]);

    const handleToggleMute = () => {
        const isMuted = soundEngine.toggleMute();
        setMuted(isMuted);
    };

    const handleMasterVolChange = (e) => {
        const val = parseFloat(e.target.value);
        setMasterVol(val);
        soundEngine.setMasterVolume(val);
    };

    const handleChannelVolChange = (channel, setLocalFn, val) => {
        setLocalFn(val);
        soundEngine.setChannelVolume(channel, val);
    };

    const handleTestSound = (type) => {
        soundEngine.ensureContext();
        if (type === 'BIRD') soundEngine.triggerBirdChirp();
        if (type === 'LEAVES') soundEngine.triggerLeavesRustle();
        if (type === 'RAIN') soundEngine.updateRainSound(12);
        if (type === 'THUNDER') soundEngine.triggerThunder();
        if (type === 'STORM') soundEngine.triggerStormGust();
        if (type === 'INSECT') soundEngine.triggerInsectStep();
        if (type === 'DIG') soundEngine.triggerNestDiggingSound();
    };

    const styles = {
        container: {
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(16px)',
            border: '1px solid rgba(56, 189, 248, 0.25)',
            borderRadius: 12,
            padding: collapsed ? '8px 12px' : '14px',
            color: '#f8fafc',
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
            color: '#38bdf8',
            cursor: 'pointer',
        },
        channelRow: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 8,
            fontSize: 11,
            color: '#cbd5e1',
            marginTop: 8,
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
        },
        testBtn: {
            background: 'rgba(255, 255, 255, 0.08)',
            border: '1px solid rgba(255,255,255,0.15)',
            color: '#e2e8f0',
            borderRadius: 4,
            padding: '3px 6px',
            fontSize: 9,
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 3,
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.header} onClick={() => setCollapsed(!collapsed)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    {muted ? <VolumeX size={15} className="text-red-400" /> : <Volume2 size={15} className="text-sky-400" />}
                    <span>🔊 Mixer & Synthesizer Audio Atmosphérique</span>
                </div>
                <span style={{ fontSize: 10, color: '#94a3b8' }}>{collapsed ? '▶ Déplier' : '▼ Réduire'}</span>
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
                        <span style={{ fontSize: 10, width: 28, textAlign: 'right', fontWeight: 700, color: '#38bdf8' }}>
                            {Math.round(masterVol * 100)}%
                        </span>
                    </div>

                    <div style={{ height: 1, background: 'rgba(255,255,255,0.1)' }} />

                    {/* Channel 1: Biome & Tree Canopy */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 130, fontSize: 11, color: '#38bdf8' }}>
                                <Bird size={12} /> Biome & Faune
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
                        </div>
                    </div>

                    {/* Channel 2: Weather & Wind */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 130, fontSize: 11, color: '#60a5fa' }}>
                                <CloudRain size={12} /> Météo & Vent
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
                            <button style={styles.testBtn} onClick={() => handleTestSound('STORM')}>💨 Vent & Tempête</button>
                        </div>
                    </div>

                    {/* Channel 3: Insect Activity */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.channelRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, width: 120, fontSize: 11, color: '#f59e0b' }}>
                                <Bug size={12} /> Pas & Insectes
                            </span>
                            <input
                                type="range" min="0" max="1" step="0.05"
                                value={insectsVol}
                                onChange={(e) => handleChannelVolChange('insects', setInsectsVol, parseFloat(e.target.value))}
                                style={styles.slider}
                            />
                        </div>
                        <div style={styles.btnGroup}>
                            <button style={styles.testBtn} onClick={() => handleTestSound('INSECT')}>🐜 Pas Insectes</button>
                            <button style={styles.testBtn} onClick={() => handleTestSound('DIG')}>⛏️ Excavation Sol</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
