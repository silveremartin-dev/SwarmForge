import React, { useState, useRef } from 'react'
import { useSimulationStore } from '../../store/simulationStore'
import { showToast } from '../../store/toastStore'
import { Play, Pause, RefreshCw, RotateCcw, Bug, Home, Bookmark, Globe } from 'lucide-react'

export default function SimulationPlaybackSection({ styles }) {
    const {
        running,
        speed,
        play,
        pause,
        setSpeed,
        resetSimulation,
        stepSingleTick,
        showChamberOverlay,
        toggleChamberOverlay,
        antTrackingEnabled,
        toggleAntTracking,
        showChambers,
        toggleShowChambers,
        showChamberInfo,
        toggleShowChamberInfo,
    } = useSimulationStore()

    const [isLongPressing, setIsLongPressing] = useState(false)
    const pressTimerRef = useRef(null)
    const stepIntervalRef = useRef(null)
    const longPressTriggeredRef = useRef(false)

    return (
        <div style={styles.selectGroup}>
            <div style={styles.sectionHeader}>
                <span>Contrôles de Simulation & Vitesse 1:1</span>
            </div>

            {/* Main Play / Step Button with Long-Press Detection */}
            <div style={{ position: 'relative' }}>
                <button
                    style={{
                        ...styles.button,
                        ...styles.playBtn,
                        outline: isLongPressing ? '2px solid #38bdf8' : 'none',
                        transform: isLongPressing ? 'scale(0.98)' : 'scale(1.0)',
                        boxShadow: isLongPressing ? '0 0 16px rgba(56, 189, 248, 0.6)' : undefined
                    }}
                    onMouseDown={() => {
                        longPressTriggeredRef.current = false
                        pressTimerRef.current = setTimeout(() => {
                            longPressTriggeredRef.current = true
                            setIsLongPressing(true)
                            if (running) pause()
                            stepSingleTick()
                            showToast("⏭️ Pas-à-Pas (Step-by-Step) : Tick exécuté", "info")
                            stepIntervalRef.current = setInterval(() => {
                                stepSingleTick()
                            }, 150)
                        }, 350)
                    }}
                    onMouseUp={() => {
                        if (pressTimerRef.current) clearTimeout(pressTimerRef.current)
                        if (stepIntervalRef.current) clearInterval(stepIntervalRef.current)
                        setIsLongPressing(false)
                        if (!longPressTriggeredRef.current) {
                            if (running) {
                                pause()
                            } else {
                                play()
                            }
                        }
                    }}
                    onMouseLeave={() => {
                        if (pressTimerRef.current) clearTimeout(pressTimerRef.current)
                        if (stepIntervalRef.current) clearInterval(stepIntervalRef.current)
                        setIsLongPressing(false)
                    }}
                    title="Clic simple : Lancer / Pause • Clic long maintenu : Mode Pas-à-Pas (Step-by-Step)"
                >
                    {isLongPressing ? (
                        <>
                            <RefreshCw size={16} className="animate-spin text-sky-300" />
                            <span>PAS-À-PAS (STEPPING...)</span>
                        </>
                    ) : running ? (
                        <>
                            <Pause size={16} />
                            <span>PAUSE / INTERROMPRE</span>
                        </>
                    ) : (
                        <>
                            <Play size={16} />
                            <span>LANCER SIMULATION</span>
                        </>
                    )}
                </button>
            </div>

            {/* Explanatory Overlay Banner on Button Behavior */}
            <div style={{ background: 'rgba(56, 189, 248, 0.08)', border: '1px solid rgba(56, 189, 248, 0.2)', borderRadius: 6, padding: '5px 8px', fontSize: 10, color: '#94a3b8', lineHeight: 1.35 }}>
                <span style={{ color: '#38bdf8', fontWeight: 700 }}>💡 Utilisation Bouton :</span><br/>
                • <strong>Clic simple :</strong> Lancer / Mettre en Pause la simulation<br/>
                • <strong>Clic long appuyé (maintenu) :</strong> Mode Pas-à-Pas (1 tick à la fois)
            </div>

            {/* Reset Button */}
            <div style={{ marginTop: 2 }}>
                <button
                    onClick={() => {
                        if (resetSimulation) resetSimulation()
                        showToast("Nouvelle simulation initialisée (Tick #0)", "info")
                    }}
                    style={{ ...styles.iconBtn, width: '100%', padding: '7px' }}
                    title="Réinitialiser la simulation à Tick #0 et remettre la vue 3D à zéro"
                >
                    <RotateCcw size={12} />
                    <span>Réinitialiser la Simulation (Tick #0)</span>
                </button>
            </div>

            {/* Simulation Display & Tracking Toggles */}
            <div style={{ background: 'rgba(0,0,0,0.2)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 6, padding: '8px', display: 'flex', flexDirection: 'column', gap: 6 }}>
                {/* 1. Ant Tracking Toggle */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <label style={{ ...styles.label, cursor: 'pointer', margin: 0 }}>
                        <Bug size={13} style={{ color: '#38bdf8' }} />
                        <span>Suivi des Fourmis (Ant Tracking)</span>
                    </label>
                    <input
                        type="checkbox"
                        checked={antTrackingEnabled}
                        onChange={toggleAntTracking}
                        style={{ accentColor: '#38bdf8', cursor: 'pointer', width: 15, height: 15 }}
                        title="Activer ou désactiver la sélection et le panneau d'inspection en direct d'une fourmi"
                    />
                </div>

                {/* 2. 3D Chambers Display */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <label style={{ ...styles.label, cursor: 'pointer', margin: 0 }}>
                        <Home size={13} style={{ color: '#a855f7' }} />
                        <span>Chambres 3D Souterraines</span>
                    </label>
                    <input
                        type="checkbox"
                        checked={showChambers}
                        onChange={toggleShowChambers}
                        style={{ accentColor: '#a855f7', cursor: 'pointer', width: 15, height: 15 }}
                        title="Afficher ou masquer la géométrie 3D des chambres du nid souterrain"
                    />
                </div>

                {/* 3. Chamber Info Labels */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <label style={{ ...styles.label, cursor: 'pointer', margin: 0 }}>
                        <Bookmark size={13} style={{ color: '#f59e0b' }} />
                        <span>Étiquettes d'Infos des Chambres</span>
                    </label>
                    <input
                        type="checkbox"
                        checked={showChamberInfo}
                        onChange={toggleShowChamberInfo}
                        style={{ accentColor: '#f59e0b', cursor: 'pointer', width: 15, height: 15 }}
                        title="Afficher ou masquer les étiquettes flottantes d'informations au-dessus des chambres"
                    />
                </div>

                {/* 4. Global Subterranean Overlay */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <label style={{ ...styles.label, cursor: 'pointer', margin: 0 }}>
                        <Globe size={13} style={{ color: '#10b981' }} />
                        <span>Overlay Vue Globale du Nid</span>
                    </label>
                    <input
                        type="checkbox"
                        checked={showChamberOverlay}
                        onChange={toggleChamberOverlay}
                        style={{ accentColor: '#10b981', cursor: 'pointer', width: 15, height: 15 }}
                        title="Activer ou désactiver l'overlay souterrain global avec les tunnels et connexions"
                    />
                </div>
            </div>

            {/* Speed Controls: 1:1 Real-Time Seconds Calibrated */}
            <div style={{ marginTop: 4 }}>
                <div style={{ fontSize: 11, color: '#94a3b8', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>Vitesse (1:1 Réel)</span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                        {speed.toFixed(1)}x {speed === 1.0 ? '(1s réelle = 1s sim)' : `(1s réelle = ${speed.toFixed(1)}s sim)`}
                    </span>
                </div>
                <input
                    type="range"
                    min="0.1"
                    max="20"
                    step="0.1"
                    value={speed}
                    onChange={(e) => setSpeed(parseFloat(e.target.value))}
                    style={styles.slider}
                />
                <div style={{ display: 'flex', gap: 4, marginTop: 4 }}>
                    {[0.5, 1.0, 2.0, 5.0, 10.0].map(sVal => (
                        <button
                            key={sVal}
                            onClick={() => setSpeed(sVal)}
                            style={{
                                flex: 1,
                                padding: '3px 0',
                                fontSize: 9,
                                fontWeight: 700,
                                borderRadius: 4,
                                border: speed === sVal ? '1px solid #38bdf8' : '1px solid #334155',
                                background: speed === sVal ? '#0284c7' : '#1e293b',
                                color: '#fff',
                                cursor: 'pointer'
                            }}
                        >
                            {sVal}x
                        </button>
                    ))}
                </div>
            </div>
        </div>
    )
}
