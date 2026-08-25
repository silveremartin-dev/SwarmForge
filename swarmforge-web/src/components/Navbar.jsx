import React, { useState, useEffect } from 'react'
import { Maximize, Minimize, Clock, Sparkles } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { showToast } from '../store/toastStore'

export default function Navbar({ activeMode, setActiveMode }) {
    const { lookAndFeel, setLookAndFeel, timeSyncMode, setTimeSyncMode, realWorldTimeStr, realWorldDateStr } = useSimulationStore()
    const [isFullscreen, setIsFullscreen] = useState(false)

    useEffect(() => {
        const handleFullscreenChange = () => {
            setIsFullscreen(!!document.fullscreenElement)
        }
        document.addEventListener('fullscreenchange', handleFullscreenChange)
        return () => document.removeEventListener('fullscreenchange', handleFullscreenChange)
    }, [])

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen().then(() => {
                setIsFullscreen(true)
                showToast('⛶ Mode Plein Écran activé', 'info')
            }).catch(err => {
                showToast('Plein écran non supporté par le navigateur: ' + err.message, 'error')
            })
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen().then(() => {
                    setIsFullscreen(false)
                    showToast('Sortie du mode Plein Écran', 'info')
                })
            }
        }
    }

    const styles = {
        nav: {
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: 50,
            background: 'var(--bg-panel, rgba(15, 23, 42, 0.85))',
            backdropFilter: 'blur(16px)',
            borderBottom: '1px solid var(--bg-panel-border, rgba(255, 255, 255, 0.1))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 20px',
            zIndex: 1000,
            fontFamily: 'var(--font-family, system-ui, sans-serif)',
        },
        brand: {
            fontSize: 16,
            fontWeight: 800,
            letterSpacing: '0.5px',
            background: 'linear-gradient(135deg, #38bdf8 0%, #818cf8 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            display: 'flex',
            alignItems: 'center',
            gap: 8,
        },
        modeGroup: {
            display: 'flex',
            gap: 6,
            background: 'rgba(0, 0, 0, 0.4)',
            padding: 4,
            borderRadius: 8,
            border: '1px solid rgba(255, 255, 255, 0.08)',
        },
        modeBtn: (active, activeColor) => ({
            padding: '6px 14px',
            fontSize: 12,
            fontWeight: 700,
            border: 'none',
            borderRadius: 6,
            cursor: 'pointer',
            background: active ? activeColor : 'transparent',
            color: active ? '#ffffff' : '#94a3b8',
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            boxShadow: active ? '0 2px 8px rgba(0,0,0,0.3)' : 'none',
        }),
        actionBtn: {
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            padding: '5px 12px',
            background: 'rgba(255, 255, 255, 0.08)',
            border: '1px solid rgba(255, 255, 255, 0.15)',
            borderRadius: 6,
            color: '#fff',
            fontSize: 11,
            fontWeight: 600,
            cursor: 'pointer',
        }
    }

    return (
        <div style={styles.nav}>
            <div style={styles.brand}>
                <span>🐜 Studio SwarmForge</span>
            </div>

            <div style={styles.modeGroup}>
                <button
                    style={styles.modeBtn(activeMode === 'WORLD_EDITOR', '#0284c7')}
                    onClick={() => setActiveMode('WORLD_EDITOR')}
                >
                    🌍 Éditeur de Monde
                </button>
                <button
                    style={styles.modeBtn(activeMode === 'CLIMATE_STUDIO', '#d97706')}
                    onClick={() => setActiveMode('CLIMATE_STUDIO')}
                >
                    ⛅ Studio Climat
                </button>
                <button
                    style={styles.modeBtn(activeMode === 'SIMULATION', '#16a34a')}
                    onClick={() => setActiveMode('SIMULATION')}
                >
                    🚀 Vue Simulation Live
                </button>
            </div>

            {/* Look & Feel Switcher, Real World Clock Sync & Fullscreen (Only in 3D Simulation Mode) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                {/* Look and Feel 3D Rendering Mode Selector */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 4, background: 'rgba(0,0,0,0.4)', padding: 3, borderRadius: 6, border: '1px solid rgba(255,255,255,0.1)' }}>
                    <span style={{ fontSize: 11, color: '#94a3b8', padding: '0 6px', fontWeight: 600 }}>Rendu 3D:</span>
                    <button
                        onClick={() => setLookAndFeel('REALISTIC')}
                        title="Style Réaliste: Brins d'herbe 3D, arbres feuillus, textures sols biologiques"
                        style={styles.modeBtn(lookAndFeel === 'REALISTIC', '#84cc16')}
                    >
                        🌿 Réaliste
                    </button>
                    <button
                        onClick={() => setLookAndFeel('SCIENTIFIC')}
                        title="Style Scientifique: Rendu voxels & géométries abstraites épurées pour l'analyse de données"
                        style={styles.modeBtn(lookAndFeel === 'SCIENTIFIC', '#2563eb')}
                    >
                        🔬 Scientifique
                    </button>
                    <button
                        onClick={() => setLookAndFeel('GAMING')}
                        title="Style Gaming: Esthétique blocs Voxels style Minecraft"
                        style={styles.modeBtn(lookAndFeel === 'GAMING', '#0284c7')}
                    >
                        🎮 Gaming (Minecraft)
                    </button>
                </div>

                {/* Real-World Clock Sync Display */}
                <button
                    onClick={() => setTimeSyncMode(timeSyncMode === 'REAL_WORLD' ? 'SIMULATED' : 'REAL_WORLD')}
                    title={timeSyncMode === 'REAL_WORLD' ? 'Synchronisé sur le Monde Réel. Cliquer pour passer en temps simulé' : 'Temps Simulé. Cliquer pour synchroniser sur le Monde Réel'}
                    style={{
                        ...styles.actionBtn,
                        borderColor: timeSyncMode === 'REAL_WORLD' ? '#10b981' : 'rgba(255,255,255,0.15)',
                        background: timeSyncMode === 'REAL_WORLD' ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255,255,255,0.05)',
                        color: timeSyncMode === 'REAL_WORLD' ? '#34d399' : '#94a3b8'
                    }}
                >
                    <Clock size={13} />
                    <span>{timeSyncMode === 'REAL_WORLD' ? `🕒 ${realWorldTimeStr}` : '⏱️ Temps Simulé'}</span>
                </button>

                {/* Fullscreen Toggle Button - Only visible in SIMULATION Mode */}
                {activeMode === 'SIMULATION' && (
                    <button
                        onClick={toggleFullscreen}
                        title="Basculer la vue Simulation 3D en Mode Plein Écran"
                        style={styles.actionBtn}
                    >
                        {isFullscreen ? <Minimize size={14} /> : <Maximize size={14} />}
                        <span>{isFullscreen ? 'Quitter' : 'Plein Écran'}</span>
                    </button>
                )}
            </div>
        </div>
    )
}
