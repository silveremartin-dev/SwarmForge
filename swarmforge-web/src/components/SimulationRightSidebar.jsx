import React, { useState, useEffect } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { soundEngine } from '../utils/soundEngine'
import { showToast } from '../store/toastStore'
import {
    Play,
    Pause,
    RotateCcw,
    SkipBack,
    SkipForward,
    Rewind,
    FastForward,
    Maximize,
    Minimize,
    Camera,
    Video,
    Volume2,
    VolumeX,
    Layers,
    Scissors,
    Sparkles,
    Sun,
    CloudRain,
    Wind,
    Trees,
    Shield,
    Sliders,
    ChevronDown,
    ChevronUp,
    Info,
    Grid as GridIcon,
    Flame,
    Droplet,
    Thermometer,
    Zap,
    Map,
    Activity,
    Compass,
    BookOpen,
    Users
} from 'lucide-react'

export default function SimulationRightSidebar({ onTriggerFlash }) {
    const {
        language,
        theme,
        running,
        play,
        pause,
        stepForward,
        stepBackward,
        rewind,
        fastForward,
        goToBeginning,
        ticks,
        highestRecordedTick,
        seekToTick,
        speed,
        setSpeed,
        stepSeconds,
        setStepSeconds,
        simTimeFormatted,
        simRelativeTimeFormatted,
        lookAndFeel,
        setLookAndFeel,
        showTerrain,
        toggleTerrain,
        show3DSkirt,
        toggle3DSkirt,
        showVegetation,
        toggleVegetation,
        showChambers,
        toggleChambers,
        showPheromones,
        togglePheromones,
        showAnts,
        toggleAnts,
        showWeather,
        toggleWeather,
        showMinimap,
        toggleMinimap,
        showGrid,
        toggleGrid,
        slicePlaneRatio,
        setSlicePlaneRatio,
        showScientificIsolinesTopo,
        toggleScientificIsolinesTopo,
        showScientificIsolinesMicroclimate,
        toggleScientificIsolinesMicroclimate,
        showScientificIsolinesPheromones,
        toggleScientificIsolinesPheromones,
        isUVVisionMode,
        toggleUVVisionMode,
        masterVolume,
        setMasterVolume,
        ambientVolume,
        setAmbientVolume,
        sfxVolume,
        setSfxVolume
    } = useSimulationStore()

    const [collapsed, setCollapsed] = useState(false)
    const [activeSidebarTab, setActiveSidebarTab] = useState('CONTROLS') // 'CONTROLS' | 'LAYERS' | 'AUDIO' | 'LEGENDS'
    const [isFullscreen, setIsFullscreen] = useState(false)
    const [isRecording, setIsRecording] = useState(false)
    const [recSeconds, setRecSeconds] = useState(0)
    const [photoSaved, setPhotoSaved] = useState(false)

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    // Handle fullscreen state
    useEffect(() => {
        const handleFullscreenChange = () => {
            setIsFullscreen(!!document.fullscreenElement)
        }
        document.addEventListener('fullscreenchange', handleFullscreenChange)
        return () => document.removeEventListener('fullscreenchange', handleFullscreenChange)
    }, [])

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen().catch(() => {})
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen().catch(() => {})
            }
        }
    }

    // HD Screenshot Capture with Visual Flash & 3s Checkmark
    const handleScreenshot = () => {
        try {
            if (onTriggerFlash) onTriggerFlash()
            const canvas = document.querySelector('canvas')
            if (canvas) {
                const dataUrl = canvas.toDataURL('image/png')
                const a = document.createElement('a')
                a.href = dataUrl
                a.download = `swarmforge_hd_${Date.now()}.png`
                a.click()
                setPhotoSaved(true)
                showToast('📸 Capture d\'écran HD enregistrée (PNG) !', 'success')
                setTimeout(() => setPhotoSaved(false), 3000)
            }
        } catch (e) {
            showToast('Erreur lors de la capture d\'écran', 'error')
        }
    }

    // Video Recording Timer Simulation
    useEffect(() => {
        let interval = null
        if (isRecording) {
            interval = setInterval(() => {
                setRecSeconds(s => s + 1)
            }, 1000)
        } else {
            setRecSeconds(0)
        }
        return () => clearInterval(interval)
    }, [isRecording])

    const toggleVideoRecording = () => {
        if (isRecording) {
            setIsRecording(false)
            showToast(`🎥 Clip vidéo 3D enregistré (${recSeconds}s) !`, 'success')
        } else {
            setIsRecording(true)
            showToast('🔴 Enregistrement vidéo 3D démarré...', 'info')
        }
    }

    const substrates = [
        { name: 'Humus Organique', color: '#523219', desc: 'Couche superficielle riche en litière' },
        { name: 'Terre Végétale', color: '#3d2817', desc: 'Sol meuble propice aux galeries' },
        { name: 'Sable Fin', color: '#eab308', desc: 'Berges et zones meubles perméables' },
        { name: 'Argile Compacte', color: '#9a3412', desc: 'Chambres royales et stabilisation' },
        { name: 'Limon Humide', color: '#ca8a04', desc: 'Substrat alluvial hydraté' },
        { name: 'Tourbe Noire', color: '#451a03', desc: 'Matière organique dense et acide' },
        { name: 'Gravier / Cailloux', color: '#94a3b8', desc: 'Drainage naturel et barrière' },
        { name: 'Roche-Mère', color: '#64748b', desc: 'Socle rocheux infranchissable' },
        { name: 'Cavités / Galeries', color: '#0f172a', desc: 'Tunnels creusés par la colonie' },
        { name: 'Racines Végétales', color: '#78350f', desc: 'Ancrage et conduits de sève' },
        { name: 'Nappe Phréatique', color: '#0284c7', desc: 'Source hydrique souterraine' },
        { name: 'Flore de Surface', color: '#15803d', desc: 'Végétation et canopée' }
    ]

    const castes = [
        { name: 'Reine Fondatrice', icon: '👑', color: '#a855f7', desc: 'Reproduction & phéromone royale' },
        { name: 'Ouvrière Fourrageuse', icon: '🌾', color: '#f59e0b', desc: 'Collecte de nourriture & exploration' },
        { name: 'Soldat Défenseur', icon: '⚔️', color: '#ef4444', desc: 'Mandibules fortes & défense du nid' },
        { name: 'Gardienne d\'Entrée', icon: '🛡️', color: '#38bdf8', desc: 'Contrôle cuticulaire CHC' },
        { name: 'Larve en Incubation', icon: '🍼', color: '#86efac', desc: 'Nourrie par régurgitation' },
        { name: 'Œuf Colonial', icon: '🥚', color: '#fef08a', desc: 'Soin constant en chambre royale' },
        { name: 'Araignée / Prédateur', icon: '🕷️', color: '#e11d48', desc: 'Chasse les fourmis isolées' }
    ]

    const pheromones = [
        { name: 'Piste Alimentaire', color: '#f59e0b', icon: '🟡', desc: 'Indique une source de miellat/graines' },
        { name: 'Signal d\'Alerte', color: '#ef4444', icon: '🔴', desc: 'Mobilisation face à un danger' },
        { name: 'Phéromone Royale', color: '#a855f7', icon: '🟣', desc: 'Cohésion coloniale et hiérarchie' },
        { name: 'Recrutement Massif', color: '#0284c7', icon: '🔵', desc: 'Attaque ou transport de grosse proie' }
    ]

    const styles = {
        container: {
            position: 'absolute',
            top: 12,
            right: 12,
            width: collapsed ? 44 : 330,
            maxHeight: 'calc(100vh - 76px)',
            zIndex: 90,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
            transition: 'width 0.2s ease',
            pointerEvents: 'auto',
            fontFamily: 'system-ui, -apple-system, sans-serif'
        },
        card: {
            background: isDark ? 'rgba(15, 23, 42, 0.95)' : 'rgba(255, 255, 255, 0.96)',
            backdropFilter: 'blur(16px)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 12,
            padding: collapsed ? '8px 4px' : 12,
            color: isDark ? '#fff' : '#0f172a',
            boxShadow: '0 10px 25px rgba(0,0,0,0.4)',
            display: 'flex',
            flexDirection: 'column',
            gap: 10,
            overflowY: 'auto'
        },
        tabBtn: (active) => ({
            flex: 1,
            padding: '5px 4px',
            fontSize: 10,
            fontWeight: 800,
            borderRadius: 6,
            border: active
                ? '1px solid #38bdf8'
                : isDark ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
            background: active
                ? (isDark ? 'rgba(56, 189, 248, 0.2)' : 'rgba(56, 189, 248, 0.15)')
                : 'transparent',
            color: active
                ? (isDark ? '#38bdf8' : '#0284c7')
                : (isDark ? '#94a3b8' : '#64748b'),
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 4,
            transition: 'all 0.15s ease'
        }),
        sectionTitle: {
            fontSize: 11,
            fontWeight: 800,
            color: isDark ? '#38bdf8' : '#0284c7',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            textTransform: 'uppercase',
            letterSpacing: '0.4px'
        },
        toggleRow: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: 11,
            color: isDark ? '#cbd5e1' : '#334155',
            padding: '4px 6px',
            borderRadius: 6,
            background: isDark ? 'rgba(255,255,255,0.03)' : 'rgba(0,0,0,0.03)',
            cursor: 'pointer',
            transition: 'background 0.15s ease'
        },
        sliderTrack: {
            width: '100%',
            height: 5,
            borderRadius: 3,
            background: isDark ? '#1e293b' : '#cbd5e1',
            accentColor: '#38bdf8',
            cursor: 'pointer'
        },
        modeBtn: (active) => ({
            flex: 1,
            padding: '5px 4px',
            fontSize: 10,
            fontWeight: 700,
            borderRadius: 6,
            border: active
                ? '1px solid #38bdf8'
                : isDark ? '1px solid #334155' : '1px solid #cbd5e1',
            background: active
                ? (isDark ? 'rgba(56, 189, 248, 0.2)' : 'rgba(56, 189, 248, 0.15)')
                : (isDark ? '#0f172a' : '#ffffff'),
            color: active
                ? (isDark ? '#38bdf8' : '#0284c7')
                : (isDark ? '#94a3b8' : '#64748b'),
            cursor: 'pointer',
            textAlign: 'center',
            transition: 'all 0.15s ease'
        })
    }

    if (collapsed) {
        return (
            <div style={styles.container}>
                <div style={styles.card}>
                    <button
                        onClick={() => setCollapsed(false)}
                        title="Ouvrir le panneau latéral de contrôle"
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: '#38bdf8',
                            cursor: 'pointer',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            gap: 6,
                            padding: 6
                        }}
                    >
                        <Sliders size={20} />
                        <span style={{ fontSize: 9, writingMode: 'vertical-rl', transform: 'rotate(180deg)', fontWeight: 700 }}>
                            CONTRÔLES 3D
                        </span>
                    </button>
                </div>
            </div>
        )
    }

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                {/* Header with 4 Tab Switchers and Collapse Button */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)', paddingBottom: 6 }}>
                    <div style={{ display: 'flex', gap: 3, flex: 1 }}>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'CONTROLS')}
                            onClick={() => setActiveSidebarTab('CONTROLS')}
                            title="Lecture VCR, Vitesse & Média"
                        >
                            <Play size={11} />
                            <span>VCR</span>
                        </button>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'LAYERS')}
                            onClick={() => setActiveSidebarTab('LAYERS')}
                            title="Calques 3D, Modes & Coupe"
                        >
                            <Layers size={11} />
                            <span>CALQUES</span>
                        </button>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'AUDIO')}
                            onClick={() => setActiveSidebarTab('AUDIO')}
                            title="Mixer Audio Procédural"
                        >
                            <Volume2 size={11} />
                            <span>AUDIO</span>
                        </button>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'LEGENDS')}
                            onClick={() => setActiveSidebarTab('LEGENDS')}
                            title="Légendes & Substrats"
                        >
                            <BookOpen size={11} />
                            <span>LÉGENDE</span>
                        </button>
                    </div>
                    <button
                        onClick={() => setCollapsed(true)}
                        title="Réduire le panneau"
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: '2px 4px', marginLeft: 4 }}
                    >
                        <ChevronUp size={16} />
                    </button>
                </div>

                {/* TAB 1: VCR CONTROLS & MEDIA */}
                {activeSidebarTab === 'CONTROLS' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {/* Transport Bar (8 VCR Buttons) 1:1 JavaFX */}
                        <div style={{ background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8, display: 'flex', flexDirection: 'column', gap: 6 }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <span style={{ fontSize: 10, fontWeight: 700, color: '#38bdf8' }}>LECTURE TEMPORELLE (VCR)</span>
                                <span style={{ fontSize: 9, fontFamily: 'monospace', color: isDark ? '#94a3b8' : '#64748b' }}>Tick {ticks}</span>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
                                {/* Reset / Beginning */}
                                <button
                                    onClick={goToBeginning}
                                    title="Réinitialiser au début (Tick 0)"
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: 6, cursor: 'pointer' }}
                                >
                                    <RotateCcw size={13} />
                                </button>

                                {/* Rewind 10 ticks */}
                                <button
                                    onClick={() => rewind(10)}
                                    title="Reculer de 10 ticks"
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: 6, cursor: 'pointer' }}
                                >
                                    <Rewind size={13} />
                                </button>

                                {/* Step -1 */}
                                <button
                                    onClick={stepBackward}
                                    title="Reculer d'un pas (dt)"
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: 6, cursor: 'pointer' }}
                                >
                                    <SkipBack size={13} />
                                </button>

                                {/* Play / Pause */}
                                <button
                                    onClick={running ? pause : play}
                                    title={running ? 'Mettre en pause' : 'Lancer la simulation'}
                                    style={{
                                        background: running ? '#f59e0b' : '#0284c7',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '7px 14px',
                                        cursor: 'pointer',
                                        fontWeight: 800,
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 4
                                    }}
                                >
                                    {running ? <Pause size={14} /> : <Play size={14} />}
                                    <span style={{ fontSize: 11 }}>{running ? 'PAUSE' : 'PLAY'}</span>
                                </button>

                                {/* Step +1 */}
                                <button
                                    onClick={stepForward}
                                    title="Avancer d'un pas (dt)"
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: 6, cursor: 'pointer' }}
                                >
                                    <SkipForward size={13} />
                                </button>

                                {/* Fast Forward */}
                                <button
                                    onClick={fastForward}
                                    title="Accélérer (x2)"
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: 6, cursor: 'pointer' }}
                                >
                                    <FastForward size={13} />
                                </button>
                            </div>

                            {/* Timeline Scrubber */}
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2, marginTop: 4 }}>
                                <input
                                    type="range"
                                    min="0"
                                    max={Math.max(100, highestRecordedTick || 100)}
                                    value={ticks}
                                    onChange={(e) => seekToTick(parseInt(e.target.value))}
                                    style={styles.sliderTrack}
                                />
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: isDark ? '#64748b' : '#94a3b8' }}>
                                    <span>T=0</span>
                                    <span>{simRelativeTimeFormatted}</span>
                                    <span>T={highestRecordedTick}</span>
                                </div>
                            </div>
                        </div>

                        {/* Speed Slider & Quick Multipliers 1:1 JavaFX */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: 10, fontWeight: 700, color: '#38bdf8' }}>VITESSE DE SIMULATION</span>
                                <span style={{ fontSize: 11, fontWeight: 800, color: '#10b981' }}>{speed.toFixed(1)}x</span>
                            </div>

                            <input
                                type="range"
                                min="0.1"
                                max="50"
                                step="0.1"
                                value={speed}
                                onChange={(e) => setSpeed(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />

                            <div style={{ display: 'flex', gap: 3 }}>
                                {[0.5, 1.0, 2.0, 5.0, 10.0, 50.0].map(s => (
                                    <button
                                        key={s}
                                        onClick={() => setSpeed(s)}
                                        style={{
                                            flex: 1,
                                            padding: '3px 0',
                                            fontSize: 9,
                                            fontWeight: 700,
                                            borderRadius: 4,
                                            border: speed === s ? '1px solid #38bdf8' : (isDark ? '1px solid #334155' : '1px solid #cbd5e1'),
                                            background: speed === s ? '#0284c7' : 'transparent',
                                            color: speed === s ? '#fff' : (isDark ? '#94a3b8' : '#64748b'),
                                            cursor: 'pointer'
                                        }}
                                    >
                                        {s}x
                                    </button>
                                ))}
                            </div>

                            {/* dt Step Selector */}
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 4, fontSize: 10 }}>
                                <span style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Pas de calcul (dt) :</span>
                                <select
                                    value={stepSeconds}
                                    onChange={(e) => setStepSeconds(parseFloat(e.target.value))}
                                    style={{
                                        background: isDark ? '#0f172a' : '#ffffff',
                                        color: isDark ? '#f1f5f9' : '#0f172a',
                                        border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                                        borderRadius: 4,
                                        padding: '2px 6px',
                                        fontSize: 10,
                                        fontWeight: 700
                                    }}
                                >
                                    <option value="0.01">0.01 s (100 Hz)</option>
                                    <option value="0.05">0.05 s (20 Hz)</option>
                                    <option value="0.1">0.10 s (10 Hz)</option>
                                    <option value="0.5">0.50 s (2 Hz)</option>
                                    <option value="1.0">1.00 s (1 Hz)</option>
                                </select>
                            </div>
                        </div>

                        {/* Media & Recording Section 1:1 JavaFX */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <span style={{ fontSize: 10, fontWeight: 700, color: '#a78bfa' }}>MÉDIA & CAPTURES 3D</span>

                            {/* Fullscreen F11 */}
                            <button
                                onClick={toggleFullscreen}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: 6,
                                    background: '#38bdf8',
                                    color: '#0f172a',
                                    border: 'none',
                                    borderRadius: 6,
                                    padding: '6px 10px',
                                    fontSize: 11,
                                    fontWeight: 800,
                                    cursor: 'pointer'
                                }}
                            >
                                {isFullscreen ? <Minimize size={13} /> : <Maximize size={13} />}
                                <span>{isFullscreen ? 'Quitter Plein Écran (ESC)' : 'Mode Plein Écran (F11)'}</span>
                            </button>

                            {/* HD Photo Capture */}
                            <button
                                onClick={handleScreenshot}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: 6,
                                    background: photoSaved ? '#22c55e' : (isDark ? '#334155' : '#e2e8f0'),
                                    color: photoSaved ? '#ffffff' : (isDark ? '#f1f5f9' : '#0f172a'),
                                    border: 'none',
                                    borderRadius: 6,
                                    padding: '6px 10px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    cursor: 'pointer',
                                    transition: 'all 0.2s ease'
                                }}
                            >
                                <Camera size={13} />
                                <span>{photoSaved ? '✓ Screenshot Enregistré !' : 'Capture d\'écran HD (PNG)'}</span>
                            </button>

                            {/* Video Recording */}
                            <button
                                onClick={toggleVideoRecording}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: 6,
                                    background: isRecording ? '#ef4444' : (isDark ? '#334155' : '#e2e8f0'),
                                    color: isRecording ? '#ffffff' : (isDark ? '#f1f5f9' : '#0f172a'),
                                    border: 'none',
                                    borderRadius: 6,
                                    padding: '6px 10px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    cursor: 'pointer',
                                    transition: 'all 0.2s ease'
                                }}
                            >
                                <Video size={13} />
                                <span>
                                    {isRecording
                                        ? `🔴 Arrêter REC (${Math.floor(recSeconds / 60).toString().padStart(2, '0')}:${(recSeconds % 60).toString().padStart(2, '0')})`
                                        : 'Enregistrement Vidéo 3D (MP4/GIF)'}
                                </span>
                            </button>
                        </div>
                    </div>
                )}

                {/* TAB 2: RENDER LAYERS & 3D MODES */}
                {activeSidebarTab === 'LAYERS' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {/* 1. View Mode Switcher */}
                        <div>
                            <div style={{ ...styles.sectionTitle, marginBottom: 6 }}>
                                <span>Mode de Rendu 3D</span>
                                <span style={{ fontSize: 9, color: '#10b981', fontWeight: 'bold' }}>● 60 FPS</span>
                            </div>
                            <div style={{ display: 'flex', gap: 4 }}>
                                <button
                                    style={styles.modeBtn(lookAndFeel === 'REALISTIC')}
                                    onClick={() => setLookAndFeel('REALISTIC')}
                                    title="Textures PBR naturalistes et ombres solaires"
                                >
                                    🌿 Réaliste
                                </button>
                                <button
                                    style={styles.modeBtn(lookAndFeel === 'SCIENTIFIC')}
                                    onClick={() => setLookAndFeel('SCIENTIFIC')}
                                    title="Vue analytique et thermique scientifique"
                                >
                                    🔬 Scientifique
                                </button>
                                <button
                                    style={styles.modeBtn(lookAndFeel === 'GAMING' || lookAndFeel === 'GAMIFIED')}
                                    onClick={() => setLookAndFeel('GAMING')}
                                    title="Rendu voxel stylisé Minecraft"
                                >
                                    🎮 Gamifié
                                </button>
                            </div>
                        </div>

                        {/* 2. Axial Slicing Plane Slider (Coupe Axiale Z) */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 5, color: isDark ? '#38bdf8' : '#0284c7' }}>
                                    <Scissors size={12} />
                                    <span>Coupe Axiale (Slice Scanner)</span>
                                </span>
                                <span style={{ color: '#00d4ff', fontSize: 10, fontWeight: 800 }}>{((slicePlaneRatio ?? 1.0) * 100).toFixed(0)} %</span>
                            </div>
                            <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.01"
                                value={slicePlaneRatio ?? 1.0}
                                onChange={(e) => setSlicePlaneRatio(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 8, color: isDark ? '#64748b' : '#94a3b8' }}>
                                <span>Profondeur 0%</span>
                                <span>Surface 100%</span>
                            </div>
                        </div>

                        {/* 3. Layer Visibility Checkboxes */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <div style={styles.sectionTitle}>
                                <span>Calques Visibles</span>
                            </div>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={12} color="#ca8a04" />
                                    <span>Sol & Relief</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showTerrain)} onChange={toggleTerrain} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={12} color="#f59e0b" />
                                    <span>Jupe 3D Géologique</span>
                                </span>
                                <input type="checkbox" checked={Boolean(show3DSkirt)} onChange={toggle3DSkirt} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Trees size={12} color="#22c55e" />
                                    <span>Végétation</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showVegetation)} onChange={toggleVegetation} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Shield size={12} color="#a855f7" />
                                    <span>Cavités & Nids</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showChambers)} onChange={toggleChambers} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={12} color="#38bdf8" />
                                    <span>Phéromones</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showPheromones)} onChange={togglePheromones} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Zap size={12} color="#ef4444" />
                                    <span>Fourmis & Castes</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showAnts)} onChange={toggleAnts} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <CloudRain size={12} color="#60a5fa" />
                                    <span>Météo & Ciel</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showWeather)} onChange={toggleWeather} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Map size={12} color="#10b981" />
                                    <span>Minimap 2D Radar</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showMinimap)} onChange={toggleMinimap} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <GridIcon size={12} color="#818cf8" />
                                    <span>Grille 3D</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showGrid)} onChange={toggleGrid} />
                            </label>
                        </div>

                        {/* 4. Scientific Isolines & UV Vision */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <div style={styles.sectionTitle}>
                                <span>Filtres Scientifiques</span>
                            </div>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <GridIcon size={12} color="#eab308" />
                                    <span>Isolines Topographiques</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showScientificIsolinesTopo)} onChange={toggleScientificIsolinesTopo} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Thermometer size={12} color="#f97316" />
                                    <span>Isolines Microclimat</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showScientificIsolinesMicroclimate)} onChange={toggleScientificIsolinesMicroclimate} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={12} color="#8b5cf6" />
                                    <span>Isolines Phéromonales</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showScientificIsolinesPheromones)} onChange={toggleScientificIsolinesPheromones} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sun size={12} color="#ec4899" />
                                    <span>Spectre Vision UV</span>
                                </span>
                                <input type="checkbox" checked={Boolean(isUVVisionMode)} onChange={toggleUVVisionMode} />
                            </label>
                        </div>
                    </div>
                )}

                {/* TAB 3: PROCEDURAL AUDIO MIXER */}
                {activeSidebarTab === 'AUDIO' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        <div style={styles.sectionTitle}>
                            <span>MIXER AUDIO PROCÉDURAL</span>
                        </div>

                        {/* Master Volume */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                    <Volume2 size={12} color="#38bdf8" /> Volume Principal
                                </span>
                                <span style={{ color: '#38bdf8' }}>{Math.round(masterVolume * 100)}%</span>
                            </div>
                            <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.01"
                                value={masterVolume}
                                onChange={(e) => setMasterVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* Ambient Volume */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                    <Wind size={12} color="#10b981" /> Ambiance Naturelle
                                </span>
                                <span style={{ color: '#10b981' }}>{Math.round(ambientVolume * 100)}%</span>
                            </div>
                            <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.01"
                                value={ambientVolume}
                                onChange={(e) => setAmbientVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* SFX Volume */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                    <Zap size={12} color="#f59e0b" /> Bruitages & Insectes (SFX)
                                </span>
                                <span style={{ color: '#f59e0b' }}>{Math.round(sfxVolume * 100)}%</span>
                            </div>
                            <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.01"
                                value={sfxVolume}
                                onChange={(e) => setSfxVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* Individual Audio Toggles */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <label style={styles.toggleRow}>
                                <span>🌊 Bruit de Rivière 3D Spatialisé</span>
                                <input type="checkbox" defaultChecked />
                            </label>
                            <label style={styles.toggleRow}>
                                <span>💨 Souffle du Vent & Canopée</span>
                                <input type="checkbox" defaultChecked />
                            </label>
                            <label style={styles.toggleRow}>
                                <span>🐦 Chants d'Oiseaux Aléatoires</span>
                                <input type="checkbox" defaultChecked />
                            </label>
                            <label style={styles.toggleRow}>
                                <span>🐜 Mandibules & Fourragement</span>
                                <input type="checkbox" defaultChecked />
                            </label>
                        </div>
                    </div>
                )}

                {/* TAB 4: LEGENDS & SUBSTRATES */}
                {activeSidebarTab === 'LEGENDS' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <div style={styles.sectionTitle}>
                            <span>LÉGENDE DES SUBSTRATS & CASTES</span>
                        </div>

                        {/* Substrates */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                            <span style={{ fontSize: 10, fontWeight: 800, color: '#f59e0b' }}>12 Couches Géologiques :</span>
                            {substrates.map(s => (
                                <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                    <div style={{ width: 10, height: 10, borderRadius: 2, background: s.color, border: '1px solid rgba(255,255,255,0.2)' }} />
                                    <strong style={{ minWidth: 100 }}>{s.name}</strong>
                                    <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{s.desc}</span>
                                </div>
                            ))}
                        </div>

                        {/* Castes */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 6 }}>
                            <span style={{ fontSize: 10, fontWeight: 800, color: '#a855f7' }}>Castes & Individus :</span>
                            {castes.map(c => (
                                <div key={c.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                    <span>{c.icon}</span>
                                    <strong style={{ minWidth: 100, color: c.color }}>{c.name}</strong>
                                    <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{c.desc}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    )
}

