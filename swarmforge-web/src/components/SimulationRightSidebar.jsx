import React, { useState, useEffect, useRef } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { soundEngine } from '../utils/soundEngine'
import { showToast } from '../store/toastStore'
import {
    Play,
    Pause,
    SkipBack,
    SkipForward,
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
    Trees,
    Shield,
    Sliders,
    ChevronUp,
    Grid as GridIcon,
    Thermometer,
    Zap,
    Map,
    BookOpen,
    Waves
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
        advanceTicks,
        goToBeginning,
        goToEnd,
        ticks,
        speed,
        setSpeed,
        simRelativeTimeFormatted,
        lookAndFeel,
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
        weatherVolume,
        setWeatherVolume,
        insectsVolume,
        setInsectsVolume,
        sfxVolume,
        setSfxVolume
    } = useSimulationStore()

    const [collapsed, setCollapsed] = useState(false)
    const [activeSidebarTab, setActiveSidebarTab] = useState('CONTROLS') // 'CONTROLS' | 'LAYERS' | 'AUDIO' | 'LEGENDS'
    const [isFullscreen, setIsFullscreen] = useState(false)
    const [isRecording, setIsRecording] = useState(false)
    const [recSeconds, setRecSeconds] = useState(0)
    const [photoSaved, setPhotoSaved] = useState(false)
    const [riverVolume, setLocalRiverVolume] = useState(0.60)
    const [isMuted, setIsMuted] = useState(false)

    const mediaRecorderRef = useRef(null)
    const recordedChunksRef = useRef([])

    const isDark = theme === 'dark'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'
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

    // HD Screenshot Capture with Visual Flash
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

    // Real Canvas Web MediaRecorder Video Recording
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
            // Stop recording and save clip
            try {
                if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
                    mediaRecorderRef.current.stop()
                }
            } catch (e) {}
            setIsRecording(false)
        } else {
            // Start real canvas stream recording
            try {
                const canvas = document.querySelector('canvas')
                if (!canvas) {
                    showToast('Impossible de trouver le canvas 3D pour la capture vidéo', 'error')
                    return
                }

                const stream = canvas.captureStream(30)
                recordedChunksRef.current = []

                const mimeType = MediaRecorder.isTypeSupported('video/webm;codecs=vp9')
                    ? 'video/webm;codecs=vp9'
                    : (MediaRecorder.isTypeSupported('video/webm') ? 'video/webm' : '')

                const recorder = new MediaRecorder(stream, mimeType ? { mimeType } : {})
                recorder.ondataavailable = (event) => {
                    if (event.data && event.data.size > 0) {
                        recordedChunksRef.current.push(event.data)
                    }
                }
                recorder.onstop = () => {
                    const blob = new Blob(recordedChunksRef.current, { type: 'video/webm' })
                    const url = URL.createObjectURL(blob)
                    const a = document.createElement('a')
                    a.href = url
                    a.download = `swarmforge_recording_${Date.now()}.webm`
                    a.click()
                    URL.revokeObjectURL(url)
                    showToast(`🎥 Clip vidéo 3D enregistré (${recSeconds}s) !`, 'success')
                }

                recorder.start()
                mediaRecorderRef.current = recorder
                setIsRecording(true)
                showToast('🔴 Enregistrement vidéo 3D démarré...', 'info')
            } catch (e) {
                showToast('Erreur lors de l\'enregistrement vidéo WebGL: ' + e.message, 'error')
            }
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
        }
    }

    if (collapsed) {
        return (
            <div style={styles.container}>
                <div style={styles.card}>
                    <button
                        onClick={() => setCollapsed(false)}
                        title={t('sidebarControlsTitle', 'CONTRÔLES 3D')}
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
                            {t('sidebarControlsTitle', 'CONTRÔLES 3D')}
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
                            title={t('tabVcr', 'VCR')}
                        >
                            <Play size={11} />
                            <span>{t('tabVcr', 'VCR')}</span>
                        </button>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'LAYERS')}
                            onClick={() => setActiveSidebarTab('LAYERS')}
                            title={t('tabLayers', 'CALQUES')}
                        >
                            <Layers size={11} />
                            <span>{t('tabLayers', 'CALQUES')}</span>
                        </button>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'AUDIO')}
                            onClick={() => setActiveSidebarTab('AUDIO')}
                            title={t('tabAudio', 'AUDIO')}
                        >
                            <Volume2 size={11} />
                            <span>{t('tabAudio', 'AUDIO')}</span>
                        </button>
                        <button
                            style={styles.tabBtn(activeSidebarTab === 'LEGENDS')}
                            onClick={() => setActiveSidebarTab('LEGENDS')}
                            title={t('tabLegends', 'LÉGENDE')}
                        >
                            <BookOpen size={11} />
                            <span>{t('tabLegends', 'LÉGENDE')}</span>
                        </button>
                    </div>
                    <button
                        onClick={() => setCollapsed(true)}
                        title={t('collapse', 'Réduire le panneau')}
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: '2px 4px', marginLeft: 4 }}
                    >
                        <ChevronUp size={16} />
                    </button>
                </div>

                {/* TAB 1: VCR CONTROLS & MEDIA */}
                {activeSidebarTab === 'CONTROLS' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {/* Transport Bar (1:1 JavaFX SimulationControlPanel.java) */}
                        <div style={{ background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8, display: 'flex', flexDirection: 'column', gap: 6 }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <span style={{ fontSize: 10, fontWeight: 700, color: '#38bdf8' }}>{t('simPlaybackTitle', 'HORLOGE & LECTURE TEMPORELLE')}</span>
                                <span style={{ fontSize: 9, fontFamily: 'monospace', color: isDark ? '#94a3b8' : '#64748b' }}>{t('stepLabel', 'Pas')} #{ticks} ({simRelativeTimeFormatted})</span>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
                                {/* 1. btnGoToBeginning (⏮) */}
                                <button
                                    onClick={goToBeginning}
                                    title={t('simBtnBeginningTt', "Début de la simulation (Retour à l'étape #0)")}
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: '6px 8px', cursor: 'pointer', fontSize: 11, fontWeight: 800 }}
                                >
                                    ⏮
                                </button>

                                {/* 2. btnRewind (⏪ -10 000 pas) */}
                                <button
                                    onClick={() => rewind(10000)}
                                    title={t('simBtnRewindTt', "Rembobiner (-10 000 étapes)")}
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: '6px 8px', cursor: 'pointer', fontSize: 11, fontWeight: 800 }}
                                >
                                    ⏪
                                </button>

                                {/* 3. btnStepBack (◀ -100 pas) */}
                                <button
                                    onClick={() => rewind(100)}
                                    title={t('simBtnStepBackTt', "Reculer (-100 étapes)")}
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: '6px 8px', cursor: 'pointer', fontSize: 11, fontWeight: 800 }}
                                >
                                    ◀
                                </button>

                                {/* 4. btnPlay / btnPause (▶ / ⏸) */}
                                <button
                                    onClick={running ? pause : play}
                                    title={running ? t('simBtnPauseTt', "Mettre la simulation en pause") : t('simBtnPlayTt', "Démarrer / Reprendre la simulation")}
                                    style={{
                                        background: running ? '#f59e0b' : '#0284c7',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '6px 12px',
                                        cursor: 'pointer',
                                        fontWeight: 800,
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 4
                                    }}
                                >
                                    {running ? <Pause size={13} /> : <Play size={13} />}
                                    <span style={{ fontSize: 10 }}>{running ? 'PAUSE' : 'PLAY'}</span>
                                </button>

                                {/* 5. btnStepForward (▶ +100 pas) */}
                                <button
                                    onClick={() => advanceTicks(100)}
                                    title={t('simBtnStepForwardTt', "Avancer (+100 étapes)")}
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: '6px 8px', cursor: 'pointer', fontSize: 11, fontWeight: 800 }}
                                >
                                    ▶
                                </button>

                                {/* 6. btnFastForward (⏩ +10 000 pas) */}
                                <button
                                    onClick={() => advanceTicks(10000)}
                                    title={t('simBtnFastForwardTt', "Avance rapide (+10 000 étapes)")}
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: '6px 8px', cursor: 'pointer', fontSize: 11, fontWeight: 800 }}
                                >
                                    ⏩
                                </button>

                                {/* 7. btnGoToEnd (⏭) */}
                                <button
                                    onClick={goToEnd}
                                    title={t('simBtnGoToEndTt', "Aller à la fin de la simulation (Dernière étape enregistrée)")}
                                    style={{ background: isDark ? '#1e293b' : '#e2e8f0', color: isDark ? '#cbd5e1' : '#334155', border: 'none', borderRadius: 4, padding: '6px 8px', cursor: 'pointer', fontSize: 11, fontWeight: 800 }}
                                >
                                    ⏭
                                </button>
                            </div>
                        </div>

                        {/* Speed Slider & Quick Multipliers (with MAX 🚀) */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: 10, fontWeight: 700, color: '#38bdf8' }}>{t('simSpeedTitle', 'VITESSE DE SIMULATION')}</span>
                                <span style={{ fontSize: 11, fontWeight: 800, color: speed >= 90 ? '#a855f7' : '#10b981' }}>
                                    {speed >= 90 ? 'MAX 🚀' : `${speed.toFixed(1)}x`}
                                </span>
                            </div>

                            <input
                                type="range"
                                min="0.1"
                                max="100"
                                step="0.1"
                                value={speed}
                                onChange={(e) => setSpeed(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />

                            <div style={{ display: 'flex', gap: 3 }}>
                                {[
                                    { label: '0.5x', val: 0.5 },
                                    { label: '1.0x', val: 1.0 },
                                    { label: '2.0x', val: 2.0 },
                                    { label: '5.0x', val: 5.0 },
                                    { label: '10x', val: 10.0 },
                                    { label: 'MAX 🚀', val: 100.0, isMax: true }
                                ].map(btn => (
                                    <button
                                        key={btn.label}
                                        onClick={() => setSpeed(btn.val)}
                                        style={{
                                            flex: 1,
                                            padding: '4px 0',
                                            fontSize: 8.5,
                                            fontWeight: 800,
                                            borderRadius: 4,
                                            border: (btn.isMax && speed >= 90) || (!btn.isMax && Math.abs(speed - btn.val) < 0.1)
                                                ? (btn.isMax ? '1px solid #d946ef' : '1px solid #38bdf8')
                                                : (isDark ? '1px solid #334155' : '1px solid #cbd5e1'),
                                            background: (btn.isMax && speed >= 90)
                                                ? '#7c3aed'
                                                : ((!btn.isMax && Math.abs(speed - btn.val) < 0.1) ? '#0284c7' : 'transparent'),
                                            color: ((btn.isMax && speed >= 90) || Math.abs(speed - btn.val) < 0.1)
                                                ? '#ffffff'
                                                : (isDark ? '#94a3b8' : '#64748b'),
                                            cursor: 'pointer'
                                        }}
                                    >
                                        {btn.label}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Media & Recording Section */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <span style={{ fontSize: 10, fontWeight: 700, color: '#a78bfa' }}>{t('mediaTitle', 'MÉDIA & CAPTURES 3D')}</span>

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
                                <span>{isFullscreen ? t('exitFullscreenBtn', 'Quitter Plein Écran (ESC)') : t('fullscreenBtn', 'Mode Plein Écran (F11)')}</span>
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
                                <span>{photoSaved ? t('screenshotSaved', '✓ Screenshot Enregistré !') : t('screenshotBtn', 'Capture d\'écran HD (PNG)')}</span>
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
                                        ? `${t('videoStopBtn', '🔴 Arrêter REC')} (${Math.floor(recSeconds / 60).toString().padStart(2, '0')}:${(recSeconds % 60).toString().padStart(2, '0')})`
                                        : t('videoRecBtn', 'Enregistrement Vidéo 3D (WebM)')}
                                </span>
                            </button>
                        </div>
                    </div>
                )}

                {/* TAB 2: RENDER LAYERS & 3D MODES */}
                {activeSidebarTab === 'LAYERS' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {/* 1. Axial Slicing Plane Slider (Coupe Axiale Z) */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 5, color: isDark ? '#38bdf8' : '#0284c7' }}>
                                    <Scissors size={12} />
                                    <span>{t('sliceScannerTitle', 'Coupe Axiale (Slice Scanner)')}</span>
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
                                <span>{t('depth0', 'Profondeur 0%')}</span>
                                <span>{t('surface100', 'Surface 100%')}</span>
                            </div>
                        </div>

                        {/* 2. Layer Visibility Checkboxes */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <div style={styles.sectionTitle}>
                                <span>{t('visibleLayersTitle', 'Calques Visibles')}</span>
                            </div>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={12} color="#ca8a04" />
                                    <span>{t('layerTerrain', 'Sol & Relief')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showTerrain)} onChange={toggleTerrain} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={12} color="#f59e0b" />
                                    <span>{t('layerSkirt', 'Jupe 3D Géologique')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(show3DSkirt)} onChange={toggle3DSkirt} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Trees size={12} color="#22c55e" />
                                    <span>{t('layerVegetation', 'Végétation')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showVegetation)} onChange={toggleVegetation} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Shield size={12} color="#a855f7" />
                                    <span>{t('layerChambers', 'Cavités & Nids')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showChambers)} onChange={toggleChambers} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={12} color="#38bdf8" />
                                    <span>{t('layerPheromones', 'Phéromones & Traces')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showPheromones)} onChange={togglePheromones} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Zap size={12} color="#ef4444" />
                                    <span>{t('layerAnts', 'Fourmis & Castes')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showAnts)} onChange={toggleAnts} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <CloudRain size={12} color="#60a5fa" />
                                    <span>{t('layerWeather', 'Météo & Ciel')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showWeather)} onChange={toggleWeather} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Map size={12} color="#10b981" />
                                    <span>{t('layerMinimap', 'Minimap 2D Radar')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showMinimap)} onChange={toggleMinimap} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <GridIcon size={12} color="#818cf8" />
                                    <span>{t('layerGrid', 'Grille 3D')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showGrid)} onChange={toggleGrid} />
                            </label>
                        </div>

                        {/* 3. Scientific Isolines & UV Vision */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <div style={styles.sectionTitle}>
                                <span>{t('sciFiltersTitle', 'Filtres Scientifiques')}</span>
                            </div>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <GridIcon size={12} color="#eab308" />
                                    <span>{t('isolinesTopo', 'Isolines Topographiques')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showScientificIsolinesTopo)} onChange={toggleScientificIsolinesTopo} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Thermometer size={12} color="#f97316" />
                                    <span>{t('isolinesMicroclimate', 'Isolines Microclimat')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showScientificIsolinesMicroclimate)} onChange={toggleScientificIsolinesMicroclimate} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={12} color="#8b5cf6" />
                                    <span>{t('isolinesPhero', 'Isolines Phéromonales')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(showScientificIsolinesPheromones)} onChange={toggleScientificIsolinesPheromones} />
                            </label>

                            <label style={styles.toggleRow}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sun size={12} color="#ec4899" />
                                    <span>{t('uvVision', 'Spectre Vision UV')}</span>
                                </span>
                                <input type="checkbox" checked={Boolean(isUVVisionMode)} onChange={toggleUVVisionMode} />
                            </label>
                        </div>
                    </div>
                )}

                {/* TAB 3: PROCEDURAL AUDIO MIXER (5 Channels 1:1 JavaFX) */}
                {activeSidebarTab === 'AUDIO' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        <div style={styles.sectionTitle}>
                            <span>{t('audioProceduralTitle', 'MIXER AUDIO PROCÉDURAL (1:1 JAVAFX)')}</span>
                            <button
                                onClick={() => {
                                    const m = soundEngine.toggleMute()
                                    setIsMuted(m)
                                }}
                                style={{ background: 'transparent', border: 'none', color: isMuted ? '#ef4444' : '#38bdf8', cursor: 'pointer', padding: 2 }}
                            >
                                {isMuted ? <VolumeX size={14} /> : <Volume2 size={14} />}
                            </button>
                        </div>

                        {/* Channel 1: Master Volume */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#38bdf8' }}>
                                    <Volume2 size={12} /> {t('channelMaster', '1. Volume Général (Master)')}
                                </span>
                                <span style={{ color: '#38bdf8', fontWeight: 800 }}>{Math.round(masterVolume * 100)}%</span>
                            </div>
                            <input
                                type="range" min="0" max="1" step="0.01"
                                value={masterVolume}
                                onChange={(e) => setMasterVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* Channel 2: Ambiance & Biome */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#10b981' }}>
                                    <Trees size={12} /> {t('channelAmbient', '2. Ambiance & Biome')}
                                </span>
                                <span style={{ color: '#10b981', fontWeight: 800 }}>{Math.round(ambientVolume * 100)}%</span>
                            </div>
                            <input
                                type="range" min="0" max="1" step="0.01"
                                value={ambientVolume}
                                onChange={(e) => setAmbientVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* Channel 3: River & Water Stream */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#06b6d4' }}>
                                    <Waves size={12} /> {t('channelRiver', '3. Rivière & Écoulement d\'Eau')}
                                </span>
                                <span style={{ color: '#06b6d4', fontWeight: 800 }}>{Math.round(riverVolume * 100)}%</span>
                            </div>
                            <input
                                type="range" min="0" max="1" step="0.01"
                                value={riverVolume}
                                onChange={(e) => {
                                    const val = parseFloat(e.target.value)
                                    setLocalRiverVolume(val)
                                    soundEngine.setChannelVolume('river', val)
                                }}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* Channel 4: Weather, Wind & Rain */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#60a5fa' }}>
                                    <CloudRain size={12} /> {t('channelWeather', '4. Météo, Vent & Pluie')}
                                </span>
                                <span style={{ color: '#60a5fa', fontWeight: 800 }}>{Math.round(weatherVolume * 100)}%</span>
                            </div>
                            <input
                                type="range" min="0" max="1" step="0.01"
                                value={weatherVolume}
                                onChange={(e) => setWeatherVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>

                        {/* Channel 5: Bio-Acoustics & Ant Chatter */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#f59e0b' }}>
                                    <Zap size={12} /> {t('channelInsects', '5. Bio-Acoustique & Nids')}
                                </span>
                                <span style={{ color: '#f59e0b', fontWeight: 800 }}>{Math.round(insectsVolume * 100)}%</span>
                            </div>
                            <input
                                type="range" min="0" max="1" step="0.01"
                                value={insectsVolume}
                                onChange={(e) => setInsectsVolume(parseFloat(e.target.value))}
                                style={styles.sliderTrack}
                            />
                        </div>
                    </div>
                )}

                {/* TAB 4: LEGENDS & SUBSTRATES */}
                {activeSidebarTab === 'LEGENDS' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        <div style={styles.sectionTitle}>
                            <span>{t('legendsFullTitle', 'LÉGENDE COMPLÈTE 1:1 JAVAFX')}</span>
                        </div>

                        {/* Nests & Chambers Legend */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                            <span style={{ fontSize: 10, fontWeight: 800, color: '#38bdf8' }}>{t('nestChambersLegendTitle', 'Structure des Nids & Chambres :')}</span>
                            {[
                                { name: t('chamberRoyal', 'Chambre Royale (Reine)'), icon: '👑', color: '#a855f7', desc: t('descChamberRoyal', 'Ponte & phéromone de fécondité') },
                                { name: t('chamberBrood', 'Couvain & Pouponnière'), icon: '🍼', color: '#38bdf8', desc: t('descChamberBrood', 'Soins aux larves et œufs') },
                                { name: t('chamberFood', 'Grenier à Graines / Aliments'), icon: '🌾', color: '#f59e0b', desc: t('descChamberFood', 'Stockage des ressources nutritives') },
                                { name: t('chamberFungus', 'Champignonnière (Symbiote)'), icon: '🍄', color: '#22c55e', desc: t('descChamberFungus', 'Culture fongique (Atta/Acromyrmex)') },
                                { name: t('chamberWaste', 'Dépotoir / Décharge'), icon: '🗑️', color: '#78350f', desc: t('descChamberWaste', 'Évacuation des déchets & cadavres') },
                                { name: t('chamberRest', 'Dortoir & Repos'), icon: '💤', color: '#6366f1', desc: t('descChamberRest', 'Repos des ouvrières et alates') },
                                { name: t('chamberTunnels', 'Tunnels & Galeries'), icon: '🕳️', color: '#94a3b8', desc: t('descChamberTunnels', 'Réseau de circulation souterrain') }
                            ].map(nc => (
                                <div key={nc.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                    <span>{nc.icon}</span>
                                    <strong style={{ minWidth: 120, color: nc.color }}>{nc.name}</strong>
                                    <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{nc.desc}</span>
                                </div>
                            ))}
                        </div>

                        {/* Substrates */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 4 }}>
                            <span style={{ fontSize: 10, fontWeight: 800, color: '#f59e0b' }}>{t('geologicalSubstratesTitle', '12 Couches Géologiques :')}</span>
                            {substrates.map(s => (
                                <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                    <div style={{ width: 10, height: 10, borderRadius: 2, background: s.color, border: '1px solid rgba(255,255,255,0.2)' }} />
                                    <strong style={{ minWidth: 120 }}>{s.name}</strong>
                                    <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{s.desc}</span>
                                </div>
                            ))}
                        </div>

                        {/* Castes */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 4 }}>
                            <span style={{ fontSize: 10, fontWeight: 800, color: '#a855f7' }}>{t('castesTitle', 'Castes & Individus :')}</span>
                            {castes.map(c => (
                                <div key={c.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                    <span>{c.icon}</span>
                                    <strong style={{ minWidth: 120, color: c.color }}>{c.name}</strong>
                                    <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{c.desc}</span>
                                </div>
                            ))}
                        </div>

                        {/* 8 Pheromones (1:1 with PheromoneType.java & PheromoneOverlay.java) */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 4 }}>
                            <span style={{ fontSize: 10, fontWeight: 800, color: '#ec4899' }}>{t('pheromonesLegendTitle', '8 Phéromones Chimiques (1:1 JavaFX) :')}</span>
                            {[
                                { name: t('pheroFood', '1. Food (Nourriture)'), color: '#4caf50', desc: 'Vert (#4caf50) : Piste alimentaire & miellat' },
                                { name: t('pheroHome', '2. Home (Retour Nid)'), color: '#2196f3', desc: 'Bleu (#2196f3) : Orientation vers entrées' },
                                { name: t('pheroAlarm', '3. Alarm (Alerte Danger)'), color: '#f44336', desc: 'Rouge (#f44336) : Attaque & défense' },
                                { name: t('pheroRecruitment', '4. Trail (Recrutement)'), color: '#ffc107', desc: 'Jaune (#ffc107) : Amplification flux de masse' },
                                { name: t('pheroQueen', '5. Queen (Phéromone Royale)'), color: '#9c27b0', desc: 'Violet (#9c27b0) : Cohésion & fertilité' },
                                { name: t('pheroBrood', '6. Brood (Couvain)'), color: '#ff9800', desc: 'Orange (#ff9800) : Soins larves & œufs' },
                                { name: t('pheroDeath', '7. Death (Nécrophorèse)'), color: '#607d8b', desc: 'Gris (#607d8b) : Transport vers dépotoir' },
                                { name: t('pheroTerritory', '8. Territory (Marquage)'), color: '#009688', desc: 'Sarcelle (#009688) : Limite territoriale' }
                            ].map(p => (
                                <div key={p.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                    <div style={{ width: 10, height: 10, borderRadius: '50%', background: p.color, border: '1px solid rgba(255,255,255,0.3)', flexShrink: 0 }} />
                                    <strong style={{ minWidth: 120, color: p.color }}>{p.name}</strong>
                                    <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{p.desc}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    )
}
