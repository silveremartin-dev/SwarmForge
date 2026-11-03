import React, { useState, useEffect, useRef } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { soundEngine } from '../utils/soundEngine'
import { showToast } from '../store/toastStore'
import SimulationSidebarTabs from './SimulationSidebarTabs'
import SimulationMediaSection from './SimulationMediaSection'
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
    Bird,
    Bug,
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
        ambientEnabled,
        toggleAmbient,
        riverEnabled,
        toggleRiver,
        weatherEnabled,
        toggleWeatherAudio,
        insectEnabled,
        toggleInsect
    } = useSimulationStore()

    const [collapsed, setCollapsed] = useState(false)
    const [activeSidebarTab, setActiveSidebarTab] = useState('CONTROLS') // 'CONTROLS' | 'LAYERS' | 'AUDIO' | 'LEGENDS'
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

                        <SimulationMediaSection t={t} isDark={isDark} onTriggerFlash={onTriggerFlash} />                    </div>
                )}

                <SimulationSidebarTabs
                    activeSidebarTab={activeSidebarTab}
                    isDark={isDark}
                    t={t}
                    styles={styles}
                    slicePlaneRatio={slicePlaneRatio}
                    setSlicePlaneRatio={setSlicePlaneRatio}
                    showTerrain={showTerrain}
                    toggleTerrain={toggleTerrain}
                    show3DSkirt={show3DSkirt}
                    toggle3DSkirt={toggle3DSkirt}
                    showVegetation={showVegetation}
                    toggleVegetation={toggleVegetation}
                    showChambers={showChambers}
                    toggleChambers={toggleChambers}
                    showPheromones={showPheromones}
                    togglePheromones={togglePheromones}
                    showAnts={showAnts}
                    toggleAnts={toggleAnts}
                    showWeather={showWeather}
                    toggleWeather={toggleWeather}
                    showMinimap={showMinimap}
                    toggleMinimap={toggleMinimap}
                    showGrid={showGrid}
                    toggleGrid={toggleGrid}
                    showScientificIsolinesTopo={showScientificIsolinesTopo}
                    toggleScientificIsolinesTopo={toggleScientificIsolinesTopo}
                    showScientificIsolinesMicroclimate={showScientificIsolinesMicroclimate}
                    toggleScientificIsolinesMicroclimate={toggleScientificIsolinesMicroclimate}
                    showScientificIsolinesPheromones={showScientificIsolinesPheromones}
                    toggleScientificIsolinesPheromones={toggleScientificIsolinesPheromones}
                    isUVVisionMode={isUVVisionMode}
                    toggleUVVisionMode={toggleUVVisionMode}
                    isMuted={isMuted}
                    setIsMuted={setIsMuted}
                    masterVolume={masterVolume}
                    setMasterVolume={setMasterVolume}
                    ambientEnabled={ambientEnabled}
                    toggleAmbient={toggleAmbient}
                    riverEnabled={riverEnabled}
                    toggleRiver={toggleRiver}
                    weatherEnabled={weatherEnabled}
                    toggleWeatherAudio={toggleWeatherAudio}
                    insectEnabled={insectEnabled}
                    toggleInsect={toggleInsect}
                />
            </div>
        </div>
    )
}
