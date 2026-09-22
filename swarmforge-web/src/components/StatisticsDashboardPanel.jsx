import React, { useState, useMemo } from 'react'
import {
    BarChart2,
    Download,
    Clock,
    Search,
    User,
    Heart,
    Battery,
    Compass,
    Activity,
    Users,
    Package,
    Sun,
    ChevronLeft,
    ChevronRight,
    Crosshair,
    Trash2,
    Eye,
    Shield,
    Flame,
    Droplets
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'

// High-Fidelity Scientific SVG Line Chart Component with explicit X/Y axes and Metric SI Units
function ScientificLineChart({
    data,
    series,
    visibleSeriesKeys,
    onToggleSeries,
    height = 200,
    title,
    yAxisLabel,
    xAxisLabel,
    yUnit = '',
    emptyMsg = 'En attente de données télémétriques...',
    timeWindowSec = 180,
    isDark = true
}) {
    // Filter active series based on visibility set
    const activeSeries = series.filter(s => visibleSeriesKeys[s.key] !== false)

    if (!data || data.length === 0) {
        return (
            <div style={{
                height,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#64748b',
                fontSize: 12,
                background: isDark ? 'rgba(15, 23, 42, 0.4)' : 'rgba(241, 245, 249, 0.6)',
                borderRadius: 8,
                border: isDark ? '1px dashed #334155' : '1px dashed #cbd5e1'
            }}>
                {emptyMsg}
            </div>
        )
    }

    const padding = { top: 20, right: 24, bottom: 36, left: 55 }
    const width = 520
    const innerWidth = width - padding.left - padding.right
    const innerHeight = height - padding.top - padding.bottom

    // Calculate maximum Y with a safety headroom
    let maxY = 1
    activeSeries.forEach(s => {
        data.forEach(d => {
            const val = s.getValue(d)
            if (typeof val === 'number' && !isNaN(val) && val > maxY) {
                maxY = val
            }
        })
    })
    maxY = Math.ceil(maxY * 1.15) || 10
    const midY = (maxY / 2).toFixed(maxY > 10 ? 0 : 1)

    // Calculate time range on X axis (metric seconds)
    const startTimeSec = data[0]?.simTimeSeconds ?? 0
    const endTimeSec = data[data.length - 1]?.simTimeSeconds ?? (startTimeSec + data.length)
    const durationSec = Math.max(1, endTimeSec - startTimeSec)

    // Format time axis ticks (s / min)
    const formatTimeTick = (sec) => {
        if (sec >= 3600) return `${(sec / 3600).toFixed(1)}h`
        if (sec >= 60) return `${(sec / 60).toFixed(1)}m`
        return `${Math.round(sec)}s`
    }

    const pointsBySeries = activeSeries.map(s => {
        return data.map((d, idx) => {
            const currentSec = d.simTimeSeconds ?? (startTimeSec + idx)
            const tRatio = durationSec > 0 ? (currentSec - startTimeSec) / durationSec : idx / Math.max(1, data.length - 1)
            const x = padding.left + Math.max(0, Math.min(1, tRatio)) * innerWidth
            const val = s.getValue(d) || 0
            const y = padding.top + innerHeight - (Math.max(0, val) / maxY) * innerHeight
            return `${x.toFixed(1)},${y.toFixed(1)}`
        }).join(' ')
    })

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const gridLineColor = isDark ? 'rgba(255,255,255,0.07)' : 'rgba(0,0,0,0.06)'
    const axisColor = isDark ? '#475569' : '#cbd5e1'
    const textColor = isDark ? '#94a3b8' : '#64748b'

    return (
        <div style={{
            background: cardBg,
            border: `1px solid ${isDark ? '#334155' : '#e2e8f0'}`,
            borderRadius: 10,
            padding: '14px 16px',
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
            boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
        }}>
            {/* Header: Title and Interactive Series Legend */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                <div style={{ fontSize: 13, fontWeight: 800, color: isDark ? '#38bdf8' : '#0284c7', display: 'flex', alignItems: 'center', gap: 6 }}>
                    {title}
                </div>
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
                    {series.map(s => {
                        const isVisible = visibleSeriesKeys[s.key] !== false
                        return (
                            <button
                                key={s.key}
                                onClick={() => onToggleSeries && onToggleSeries(s.key)}
                                title={`Cliquer pour ${isVisible ? 'masquer' : 'afficher'} la série ${s.name}`}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 5,
                                    fontSize: 10.5,
                                    fontWeight: 700,
                                    padding: '2px 7px',
                                    borderRadius: 4,
                                    border: `1px solid ${isVisible ? s.color : (isDark ? '#334155' : '#cbd5e1')}`,
                                    background: isVisible ? `${s.color}15` : 'transparent',
                                    color: isVisible ? s.color : textColor,
                                    cursor: 'pointer',
                                    opacity: isVisible ? 1.0 : 0.45,
                                    transition: 'all 0.15s'
                                }}
                            >
                                <span style={{
                                    width: 8,
                                    height: 8,
                                    borderRadius: '50%',
                                    background: isVisible ? s.color : 'transparent',
                                    border: `1.5px solid ${s.color}`
                                }} />
                                <span>{s.name}</span>
                            </button>
                        )
                    })}
                </div>
            </div>

            {/* SVG Visual Graph Container */}
            <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', height: height, background: isDark ? 'rgba(0,0,0,0.22)' : 'rgba(248,250,252,0.8)', borderRadius: 6, overflow: 'visible' }}>
                {/* Horizontal Grid lines */}
                <line x1={padding.left} y1={padding.top} x2={width - padding.right} y2={padding.top} stroke={gridLineColor} strokeDasharray="3 3" />
                <line x1={padding.left} y1={padding.top + innerHeight / 2} x2={width - padding.right} y2={padding.top + innerHeight / 2} stroke={gridLineColor} strokeDasharray="3 3" />
                <line x1={padding.left} y1={padding.top + innerHeight} x2={width - padding.right} y2={padding.top + innerHeight} stroke={axisColor} strokeWidth="1.2" />

                {/* Vertical Grid lines */}
                <line x1={padding.left} y1={padding.top} x2={padding.left} y2={padding.top + innerHeight} stroke={axisColor} strokeWidth="1.2" />
                <line x1={padding.left + innerWidth / 2} y1={padding.top} x2={padding.left + innerWidth / 2} y2={padding.top + innerHeight} stroke={gridLineColor} strokeDasharray="3 3" />
                <line x1={width - padding.right} y1={padding.top} x2={width - padding.right} y2={padding.top + innerHeight} stroke={gridLineColor} strokeDasharray="3 3" />

                {/* Y-Axis Labels (SI metric units) */}
                <text x={padding.left - 6} y={padding.top + 4} fill={textColor} fontSize="9" fontWeight="700" textAnchor="end">{maxY} {yUnit}</text>
                <text x={padding.left - 6} y={padding.top + innerHeight / 2 + 3} fill={textColor} fontSize="8.5" textAnchor="end">{midY} {yUnit}</text>
                <text x={padding.left - 6} y={padding.top + innerHeight + 1} fill={textColor} fontSize="9" fontWeight="700" textAnchor="end">0 {yUnit}</text>

                {/* Y-Axis Unit Header */}
                <text x={padding.left - 4} y={padding.top - 8} fill={isDark ? '#38bdf8' : '#0284c7'} fontSize="9" fontWeight="800" textAnchor="start">{yAxisLabel}</text>

                {/* X-Axis Labels (Time in metric s / min) */}
                <text x={padding.left} y={padding.top + innerHeight + 16} fill={textColor} fontSize="9" fontWeight="600" textAnchor="start">{formatTimeTick(startTimeSec)}</text>
                <text x={padding.left + innerWidth / 2} y={padding.top + innerHeight + 16} fill={textColor} fontSize="9" fontWeight="600" textAnchor="middle">{formatTimeTick(startTimeSec + durationSec / 2)}</text>
                <text x={width - padding.right} y={padding.top + innerHeight + 16} fill={textColor} fontSize="9" fontWeight="600" textAnchor="end">{formatTimeTick(endTimeSec)}</text>

                {/* X-Axis Label */}
                <text x={padding.left + innerWidth / 2} y={padding.top + innerHeight + 28} fill={textColor} fontSize="9" fontWeight="700" textAnchor="middle">
                    {xAxisLabel || 'Temps Réel Écoulé (secondes)'}
                </text>

                {/* Polylines for each active series */}
                {activeSeries.map((s, sIdx) => (
                    <polyline
                        key={s.key}
                        fill="none"
                        stroke={s.color}
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        points={pointsBySeries[sIdx]}
                    />
                ))}
            </svg>
        </div>
    )
}

export default function StatisticsDashboardPanel() {
    const {
        statsHistory,
        timeWindow,
        setTimeWindow,
        colonies,
        ants,
        ticks,
        simTimeSeconds,
        simTimeFormatted,
        measuredTps,
        trackedAntId,
        trackedAntData,
        setTrackedAntId,
        setFollowAntCamera,
        setActiveSubTab,
        theme,
        language
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallbackOrParams, params) => getTranslation(language, key, fallbackOrParams, params)

    const [graphViewMode, setGraphViewMode] = useState(0) // 0: ALL, 1: Multi-Colony, 2: Castes, 3: Behaviors, 4: Resources, 5: Climate, 6: Performance, 7: Telemetry
    const [searchAntId, setSearchAntId] = useState('')
    const [zoomMultiplier, setZoomMultiplier] = useState(1.0)

    // Series visibility map (1:1 with CheckBoxes in StatisticsDashboard.java)
    const [visibleSeries, setVisibleSeries] = useState({
        totalPop: true,
        workers: true,
        soldiers: true,
        queens: true,
        males: true,
        foraging: true,
        digging: true,
        nursing: true,
        guarding: true,
        royalCare: true,
        resting: true,
        food: true,
        water: true,
        protein: true,
        births: true,
        deaths: true,
        temp: true,
        rain: true,
        phero: true,
        tps: true,
        antHealth: true,
        antEnergy: true,
        antDistance: true
    })

    const toggleSeries = (key) => {
        setVisibleSeries(prev => ({ ...prev, [key]: !prev[key] }))
    }

    const toggleAllSeries = (val) => {
        const next = {}
        Object.keys(visibleSeries).forEach(k => { next[k] = val })
        setVisibleSeries(next)
    }

    const handleSearchAnt = () => {
        if (!searchAntId.trim()) return
        const ant = ants.find(a => a.id.toLowerCase().includes(searchAntId.trim().toLowerCase()))
        if (ant) {
            setTrackedAntId(ant.id)
            showToast(`✓ Individu ciblé : ${ant.id}`, 'info')
        } else {
            showToast(`Individu "${searchAntId}" introuvable`, 'error')
        }
    }

    const cycleAnt = (direction) => {
        if (!ants || ants.length === 0) return
        const currentIndex = ants.findIndex(a => a.id === trackedAntId)
        let nextIndex = 0
        if (currentIndex !== -1) {
            nextIndex = (currentIndex + direction + ants.length) % ants.length
        }
        const nextAnt = ants[nextIndex]
        if (nextAnt) {
            setTrackedAntId(nextAnt.id)
            setSearchAntId(nextAnt.id)
        }
    }

    const handleTrackIn3D = () => {
        if (trackedAntId) {
            setFollowAntCamera(true)
            setActiveSubTab('VISUAL_3D')
            showToast('🎥 Caméra 3D asservie sur l\'individu !', 'info')
        }
    }

    const exportCSV = () => {
        if (!statsHistory || statsHistory.length === 0) {
            showToast(t('noDataToExport', 'Aucune donnée télémétrique à exporter'), 'warning')
            return
        }

        let csv = 'Tick;TempsSecondes;PopulationTotale;Reines;Ouvrieres;Soldats;Males;Fourragement;Excavation;SoinsLarves;Garde;SoinsRoyaux;Repos;Nourriture;Eau;Proteines;Naissances;Morts;Temperature;Pluie;Pheromones;TPS\n'
        statsHistory.forEach(s => {
            csv += `${s.tick};${(s.simTimeSeconds || 0).toFixed(2)};${s.totalPopulation || 0};${s.casteBreakdown?.queens || 0};${s.casteBreakdown?.workers || 0};${s.casteBreakdown?.soldiers || 0};${s.casteBreakdown?.males || 0};${s.behaviors?.foraging || 0};${s.behaviors?.digging || 0};${s.behaviors?.nursing || 0};${s.behaviors?.guarding || 0};${s.behaviors?.royalCare || 0};${s.behaviors?.resting || 0};${(s.resources?.food || 0).toFixed(1)};${(s.resources?.water || 0).toFixed(1)};${(s.resources?.protein || 0).toFixed(1)};${s.resources?.births || 0};${s.resources?.deaths || 0};${(s.weather?.temp || 0).toFixed(1)};${(s.weather?.rain || 0).toFixed(1)};${s.weather?.phero || 0};${s.performance?.tps || 20}\n`
        })

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `swarmforge_statistics_${Date.now()}.csv`
        a.click()
        showToast('📊 Export statistique CSV téléchargé avec succès !', 'success')
    }

    // Time window calculation with zoom factor
    const basePointsCount = timeWindow === '1m' ? 20 : (timeWindow === '3m' ? 60 : (timeWindow === '5m' ? 100 : (timeWindow === '10m' ? 200 : 7200)))
    const effectivePointsCount = Math.max(10, Math.round(basePointsCount / zoomMultiplier))
    const activeData = statsHistory.slice(-effectivePointsCount)

    // Current latest KPIs
    const latestSnapshot = statsHistory.length > 0 ? statsHistory[statsHistory.length - 1] : null
    const totalPopCount = latestSnapshot?.totalPopulation ?? ants.length
    const totalQueensCount = latestSnapshot?.casteBreakdown?.queens ?? ants.filter(a => a.caste === 'QUEEN').length
    const totalWorkersCount = latestSnapshot?.casteBreakdown?.workers ?? ants.filter(a => a.caste === 'WORKER').length
    const totalSoldiersCount = latestSnapshot?.casteBreakdown?.soldiers ?? ants.filter(a => a.caste === 'SOLDIER').length
    const totalFoodReserves = (latestSnapshot?.resources?.food ?? colonies.reduce((sum, c) => sum + (c.food || 0), 0)).toFixed(1)
    const totalWaterReserves = (latestSnapshot?.resources?.water ?? colonies.reduce((sum, c) => sum + (c.water || 0), 0)).toFixed(1)

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    return (
        <div style={{
            maxWidth: 1200,
            margin: '0 auto',
            padding: '20px 24px',
            display: 'flex',
            flexDirection: 'column',
            gap: 16
        }}>
            {/* 1. Header Toolbar & Controls */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '14px 18px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <BarChart2 size={20} color="#38bdf8" />
                    <div>
                        <h2 style={{ margin: 0, fontSize: 16, fontWeight: 800, color: isDark ? '#38bdf8' : '#0284c7' }}>
                            {t('stats.dashboard_title', 'Tableau de Bord Statistiques & Télémétrie')}
                        </h2>
                        <span style={{ fontSize: 11, color: textMuted }}>
                            {t('stats.dashboard_title.tt', 'Téléométrie de simulation en temps réel, démographie et biomasse.')}
                        </span>
                    </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                    {/* View Mode Dropdown (1:1 with StatisticsDashboard.java) */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>{t('stats.graph_view.label', 'Mode Affichage :')}</span>
                        <select
                            value={graphViewMode}
                            onChange={(e) => setGraphViewMode(Number(e.target.value))}
                            style={{
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 6,
                                padding: '5px 10px',
                                fontSize: 11.5,
                                fontWeight: 600
                            }}
                        >
                            <option value={0}>{t('stats.view.all', 'Tous les Graphiques (Vue Globale)')}</option>
                            <option value={1}>{t('stats.view.demographics', '1. Démographie Multi-Colonies & Espèces')}</option>
                            <option value={2}>{t('stats.view.castes', '2. Répartition Globale des Castes')}</option>
                            <option value={3}>{t('stats.view.behaviors', '3. Répartition Éthologique des Comportements')}</option>
                            <option value={4}>{t('stats.view.resources', '4. Bio-Ressources & Événements')}</option>
                            <option value={5}>{t('stats.view.ecosystem', '5. Écosystème & Climat')}</option>
                            <option value={6}>{t('stats.view.tps', '6. Performance Moteur (TPS)')}</option>
                            <option value={7}>{t('stats.view.telemetry', '7. Télémétrie Individuelle (Fourmi Spécifique)')}</option>
                        </select>
                    </div>

                    {/* Zoom Buttons */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: 3, background: inputBg, padding: 2, borderRadius: 6, border: `1px solid ${borderCol}` }}>
                        <button
                            onClick={() => setZoomMultiplier(prev => Math.min(4.0, prev * 1.5))}
                            title={t('stats.zoom.in.tt', 'Zoom avant temporel')}
                            style={{ background: 'transparent', border: 'none', color: '#38bdf8', padding: '3px 8px', fontSize: 11, fontWeight: 800, cursor: 'pointer' }}
                        >
                            🔍+
                        </button>
                        <button
                            onClick={() => setZoomMultiplier(prev => Math.max(0.25, prev / 1.5))}
                            title={t('stats.zoom.out.tt', 'Zoom arrière temporel')}
                            style={{ background: 'transparent', border: 'none', color: '#38bdf8', padding: '3px 8px', fontSize: 11, fontWeight: 800, cursor: 'pointer' }}
                        >
                            🔍-
                        </button>
                        <button
                            onClick={() => setZoomMultiplier(1.0)}
                            title={t('stats.zoom.reset.tt', 'Réinitialiser le zoom temporel (100%)')}
                            style={{ background: 'transparent', border: 'none', color: textMuted, padding: '3px 6px', fontSize: 10, cursor: 'pointer' }}
                        >
                            100%
                        </button>
                    </div>

                    {/* Time Window Buttons */}
                    <div style={{ display: 'flex', background: inputBg, padding: 3, borderRadius: 6, border: `1px solid ${borderCol}` }}>
                        {[
                            { id: '1m', label: '1 min' },
                            { id: '3m', label: '3 min' },
                            { id: '5m', label: '5 min' },
                            { id: '10m', label: '10 min' },
                            { id: 'all', label: 'Tout' }
                        ].map(w => (
                            <button
                                key={w.id}
                                onClick={() => setTimeWindow(w.id)}
                                style={{
                                    padding: '4px 8px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    borderRadius: 4,
                                    border: 'none',
                                    cursor: 'pointer',
                                    background: timeWindow === w.id ? '#0284c7' : 'transparent',
                                    color: timeWindow === w.id ? '#fff' : textMuted
                                }}
                            >
                                {w.label}
                            </button>
                        ))}
                    </div>

                    <button
                        onClick={exportCSV}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 6,
                            background: isDark ? '#047857' : '#10b981',
                            color: '#fff',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: 6,
                            fontSize: 11.5,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Download size={13} /> {t('stats.export_btn', 'Exporter (CSV)')}
                    </button>
                </div>
            </div>

            {/* 2. KPI Summary Cards Panel (1:1 with createSummaryPanel in JavaFX) */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(135px, 1fr))',
                gap: 10
            }}>
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.population', 'Population :')}</span>
                    <strong style={{ fontSize: 15, color: '#38bdf8' }}>{totalPopCount} ind</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.active_colonies', 'Colonies :')}</span>
                    <strong style={{ fontSize: 15, color: '#a855f7' }}>{colonies.length}</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.queens', 'Reines :')}</span>
                    <strong style={{ fontSize: 15, color: '#eab308' }}>{totalQueensCount} ind</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.workers', 'Ouvrières :')}</span>
                    <strong style={{ fontSize: 15, color: '#10b981' }}>{totalWorkersCount} ind</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.soldiers', 'Soldats :')}</span>
                    <strong style={{ fontSize: 15, color: '#ef4444' }}>{totalSoldiersCount} ind</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.food', 'Nourriture :')}</span>
                    <strong style={{ fontSize: 15, color: '#f59e0b' }}>{totalFoodReserves} g</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.water', 'Eau :')}</span>
                    <strong style={{ fontSize: 15, color: '#06b6d4' }}>{totalWaterReserves} mL</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.tick_rate', 'Vitesse :')}</span>
                    <strong style={{ fontSize: 15, color: '#22c55e' }}>{measuredTps || 20} TPS</strong>
                </div>

                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 8, padding: '8px 12px', display: 'flex', flexDirection: 'column', gridColumn: 'span 2' }}>
                    <span style={{ fontSize: 11, color: textMuted }}>{t('stats.sim_time', 'Temps Simulé :')}</span>
                    <strong style={{ fontSize: 13, color: textMain }}>{simTimeFormatted} ({(simTimeSeconds || 0).toFixed(1)} s)</strong>
                </div>
            </div>

            {/* 3. Global Series Selection Checkboxes Bar (1:1 with JavaFX FlowPane selectorBox) */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '12px 16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 8
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: 12, fontWeight: 700, color: textMuted }}>
                        {t('stats.series_select', 'Sélection des statistiques à afficher sur les graphiques :')}
                    </span>
                    <div style={{ display: 'flex', gap: 6 }}>
                        <button
                            onClick={() => toggleAllSeries(true)}
                            style={{ background: 'transparent', border: `1px solid ${borderCol}`, color: textMain, padding: '2px 8px', borderRadius: 4, fontSize: 10, cursor: 'pointer' }}
                        >
                            ✓ Tout cocher
                        </button>
                        <button
                            onClick={() => toggleAllSeries(false)}
                            style={{ background: 'transparent', border: `1px solid ${borderCol}`, color: textMain, padding: '2px 8px', borderRadius: 4, fontSize: 10, cursor: 'pointer' }}
                        >
                            ✕ Tout décocher
                        </button>
                    </div>
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {[
                        { key: 'totalPop', label: t('stats.population_total', 'Population Totale'), color: '#38bdf8' },
                        { key: 'workers', label: t('stats.workers', 'Ouvrières'), color: '#22c55e' },
                        { key: 'soldiers', label: t('stats.soldiers', 'Soldats'), color: '#ef4444' },
                        { key: 'queens', label: t('stats.queens', 'Reines'), color: '#eab308' },
                        { key: 'males', label: t('stats.chk.males', 'Mâles'), color: '#a855f7' },
                        { key: 'foraging', label: t('stats.behavior.foraging', '🌾 Fourragement'), color: '#10b981' },
                        { key: 'digging', label: t('stats.behavior.digging', '⛏️ Excavation'), color: '#d97706' },
                        { key: 'nursing', label: t('stats.behavior.nursing', '🍼 Soins Larves'), color: '#ec4899' },
                        { key: 'guarding', label: t('stats.behavior.guarding', '🛡️ Garde/Défense'), color: '#f43f5e' },
                        { key: 'royalCare', label: t('stats.behavior.royal_care', '👑 Soins Royaux'), color: '#8b5cf6' },
                        { key: 'resting', label: t('stats.behavior.resting', '💤 Repos/Inactivité'), color: '#64748b' },
                        { key: 'food', label: t('stats.food', 'Nourriture'), color: '#f59e0b' },
                        { key: 'water', label: t('stats.water', 'Eau'), color: '#06b6d4' },
                        { key: 'protein', label: t('stats.chk.protein', 'Protéines'), color: '#ec4899' },
                        { key: 'births', label: t('stats.births', 'Naissances'), color: '#10b981' },
                        { key: 'deaths', label: t('stats.deaths', 'Décès'), color: '#dc2626' },
                        { key: 'temp', label: t('stats.chk.temp', 'Temp (°C)'), color: '#f97316' },
                        { key: 'rain', label: t('stats.chk.rain', 'Pluie (mm/h)'), color: '#3b82f6' },
                        { key: 'phero', label: t('stats.chk.phero', 'Phéromones'), color: '#a855f7' },
                        { key: 'tps', label: t('stats.tps', 'TPS Moteur'), color: '#22c55e' },
                        { key: 'antHealth', label: t('stats.chk.ant_health', 'Santé Fourmi'), color: '#ef4444' },
                        { key: 'antEnergy', label: t('stats.chk.ant_energy', 'Énergie Fourmi'), color: '#f59e0b' },
                        { key: 'antDistance', label: t('stats.chk.ant_distance', 'Distance Fourmi'), color: '#38bdf8' }
                    ].map(item => {
                        const isChecked = visibleSeries[item.key] !== false
                        return (
                            <label
                                key={item.key}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 5,
                                    fontSize: 11,
                                    fontWeight: 600,
                                    color: isChecked ? textMain : textMuted,
                                    cursor: 'pointer',
                                    background: isChecked ? (isDark ? 'rgba(56, 189, 248, 0.08)' : 'rgba(2, 132, 199, 0.06)') : 'transparent',
                                    padding: '3px 8px',
                                    borderRadius: 4,
                                    border: `1px solid ${isChecked ? item.color : borderCol}`
                                }}
                            >
                                <input
                                    type="checkbox"
                                    checked={isChecked}
                                    onChange={() => toggleSeries(item.key)}
                                    style={{ accentColor: item.color, cursor: 'pointer' }}
                                />
                                <span>{item.label}</span>
                            </label>
                        )
                    })}
                </div>
            </div>

            {/* 4. The 7 Dynamic Real-Time Scientific Charts */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {/* 1. Multi-Colony Demographics & Species Chart */}
                {(graphViewMode === 0 || graphViewMode === 1) && (
                    <ScientificLineChart
                        title={t('stats.chart.multi_colony.title', '📈 1. Démographie Multi-Colonies & Espèces (Population par Colonie)')}
                        yAxisLabel={t('stats.chart.multi_colony.y', 'Effectifs par Colonie (ind)')}
                        xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                        yUnit="ind"
                        data={activeData}
                        isDark={isDark}
                        visibleSeriesKeys={visibleSeries}
                        onToggleSeries={toggleSeries}
                        series={colonies.map(c => ({
                            key: `colony_${c.id}`,
                            name: `${c.name} (${c.speciesId || 'Fourmi'})`,
                            color: c.color || '#38bdf8',
                            getValue: (d) => d.coloniesPop?.[c.id] || (colonies.length === 1 ? d.totalPopulation : 0)
                        }))}
                    />
                )}

                {/* 2. Castes Global Breakdown Chart */}
                {(graphViewMode === 0 || graphViewMode === 2) && (
                    <ScientificLineChart
                        title={t('stats.chart.castes.title', '👥 2. Répartition Globale des Castes (Reines, Ouvrières, Soldats, Mâles)')}
                        yAxisLabel={t('stats.chart.castes.y', 'Effectif par Caste (ind)')}
                        xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                        yUnit="ind"
                        data={activeData}
                        isDark={isDark}
                        visibleSeriesKeys={visibleSeries}
                        onToggleSeries={toggleSeries}
                        series={[
                            { key: 'totalPop', name: t('stats.series.total_pop', 'Population Totale Globale'), color: '#38bdf8', getValue: (d) => d.totalPopulation },
                            { key: 'workers', name: t('stats.workers', 'Ouvrières'), color: '#22c55e', getValue: (d) => d.casteBreakdown?.workers || 0 },
                            { key: 'soldiers', name: t('stats.soldiers', 'Soldats'), color: '#ef4444', getValue: (d) => d.casteBreakdown?.soldiers || 0 },
                            { key: 'queens', name: t('stats.queens', 'Reines'), color: '#eab308', getValue: (d) => d.casteBreakdown?.queens || 0 },
                            { key: 'males', name: t('stats.series.males', 'Mâles'), color: '#a855f7', getValue: (d) => d.casteBreakdown?.males || 0 }
                        ]}
                    />
                )}

                {/* 3. Ethological Behaviors Breakdown Chart */}
                {(graphViewMode === 0 || graphViewMode === 3) && (
                    <ScientificLineChart
                        title={t('stats.chart.behaviors.title', '🐜 3. Répartition Éthologique des Comportements')}
                        yAxisLabel={t('stats.chart.behaviors.y', 'Nombre d\'Individus (ind)')}
                        xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                        yUnit="ind"
                        data={activeData}
                        isDark={isDark}
                        visibleSeriesKeys={visibleSeries}
                        onToggleSeries={toggleSeries}
                        series={[
                            { key: 'foraging', name: t('stats.behavior.series.foraging', '🌾 Fourragement (Récolte)'), color: '#10b981', getValue: (d) => d.behaviors?.foraging || 0 },
                            { key: 'digging', name: t('stats.behavior.series.digging', '⛏️ Excavation (Galeries)'), color: '#d97706', getValue: (d) => d.behaviors?.digging || 0 },
                            { key: 'nursing', name: t('stats.behavior.series.nursing', '🍼 Soins aux Larves'), color: '#ec4899', getValue: (d) => d.behaviors?.nursing || 0 },
                            { key: 'guarding', name: t('stats.behavior.series.guarding', '🛡️ Garde & Défense'), color: '#f43f5e', getValue: (d) => d.behaviors?.guarding || 0 },
                            { key: 'royalCare', name: t('stats.behavior.series.royal_care', '👑 Soins Royaux'), color: '#8b5cf6', getValue: (d) => d.behaviors?.royalCare || 0 },
                            { key: 'resting', name: t('stats.behavior.series.resting', '💤 Repos / Inactivité'), color: '#64748b', getValue: (d) => d.behaviors?.resting || 0 }
                        ]}
                    />
                )}

                {/* 4. Bio-Resources & Events Chart */}
                {(graphViewMode === 0 || graphViewMode === 4) && (
                    <ScientificLineChart
                        title={t('stats.chart.resources.title', '🌾 4. Bio-Ressources & Événements (Nourriture, Eau, Naissances, Morts)')}
                        yAxisLabel={t('stats.chart.resources.y', 'Biomasse & Événements (g / ind)')}
                        xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                        yUnit="g"
                        data={activeData}
                        isDark={isDark}
                        visibleSeriesKeys={visibleSeries}
                        onToggleSeries={toggleSeries}
                        series={[
                            { key: 'food', name: t('stats.food', 'Nourriture Stockée (g)'), color: '#f59e0b', getValue: (d) => d.resources?.food || 0 },
                            { key: 'water', name: t('stats.water', 'Eau / Humidité (mL)'), color: '#06b6d4', getValue: (d) => d.resources?.water || 0 },
                            { key: 'protein', name: t('stats.series.protein', 'Protéines (g)'), color: '#ec4899', getValue: (d) => d.resources?.protein || 0 },
                            { key: 'births', name: t('stats.births', 'Naissances Cumulées (ind)'), color: '#10b981', getValue: (d) => d.resources?.births || 0 },
                            { key: 'deaths', name: t('stats.deaths', 'Décès Cumulés (ind)'), color: '#dc2626', getValue: (d) => d.resources?.deaths || 0 }
                        ]}
                    />
                )}

                {/* 5. Ecosystem & Climate Chart */}
                {(graphViewMode === 0 || graphViewMode === 5) && (
                    <ScientificLineChart
                        title={t('stats.chart.weather.title', '🌤️ 5. Écosystème & Climat (Température °C, Pluie mm/h, Phéromones %)')}
                        yAxisLabel={t('stats.chart.weather.y', 'Unités Environnementales (°C / mm/h / %)')}
                        xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                        yUnit=""
                        data={activeData}
                        isDark={isDark}
                        visibleSeriesKeys={visibleSeries}
                        onToggleSeries={toggleSeries}
                        series={[
                            { key: 'temp', name: t('stats.series.temp', 'Temp. Air (°C)'), color: '#f97316', getValue: (d) => d.weather?.temp || 22.0 },
                            { key: 'rain', name: t('stats.series.rain', 'Précipitations (mm/h)'), color: '#3b82f6', getValue: (d) => d.weather?.rain || 0.0 },
                            { key: 'phero', name: t('stats.series.phero', 'Intensité Phéromones (%)'), color: '#a855f7', getValue: (d) => Math.min(100, (d.weather?.phero || 0) / 2) }
                        ]}
                    />
                )}

                {/* 6. Engine Performance TPS Chart */}
                {(graphViewMode === 0 || graphViewMode === 6) && (
                    <ScientificLineChart
                        title={t('stats.chart.performance.title', '⚡ 6. Performance Moteur (Vitesse de Calcul TPS)')}
                        yAxisLabel={t('stats.chart.performance.y', 'Ticks Par Seconde (TPS)')}
                        xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                        yUnit="TPS"
                        data={activeData}
                        isDark={isDark}
                        visibleSeriesKeys={visibleSeries}
                        onToggleSeries={toggleSeries}
                        series={[
                            { key: 'tps', name: t('stats.tps', 'TPS Réel Moteur'), color: '#22c55e', getValue: (d) => d.performance?.tps || 20 }
                        ]}
                    />
                )}

                {/* 7. Individual Ant Inspection Card & Live Telemetry Chart (1:1 with StatisticsDashboard.java createIndividualAntCard) */}
                {(graphViewMode === 0 || graphViewMode === 7) && (
                    <div style={{
                        background: cardBg,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 10,
                        padding: '16px 20px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 14
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                                <User size={18} />
                                <span style={{ fontSize: 14, fontWeight: 800 }}>
                                    {t('stats.indiv.title', '🐜 7. Inspection Individuelle & Télémétrie en Direct')}
                                </span>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <input
                                    type="text"
                                    placeholder={t('inspector.prompt.id', 'ID fourmi (ex. ant_1)...')}
                                    value={searchAntId}
                                    onChange={(e) => setSearchAntId(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && handleSearchAnt()}
                                    style={{ width: 170, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 8px', fontSize: 12 }}
                                />

                                <button
                                    onClick={() => cycleAnt(-1)}
                                    title={t('inspector.btn.prev', '◀ Fourmi Préc.')}
                                    style={{ background: isDark ? '#334155' : '#e2e8f0', border: 'none', color: textMain, padding: '5px 8px', borderRadius: 4, cursor: 'pointer' }}
                                >
                                    <ChevronLeft size={14} />
                                </button>

                                <button
                                    onClick={() => cycleAnt(1)}
                                    title={t('inspector.btn.next', 'Fourmi Suiv. ▶')}
                                    style={{ background: isDark ? '#334155' : '#e2e8f0', border: 'none', color: textMain, padding: '5px 8px', borderRadius: 4, cursor: 'pointer' }}
                                >
                                    <ChevronRight size={14} />
                                </button>

                                <button
                                    onClick={handleSearchAnt}
                                    style={{ background: '#0284c7', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: 4, fontSize: 12, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                                >
                                    <Search size={13} /> {t('stats.track_btn', 'Traquer & Suivre')}
                                </button>

                                {trackedAntData && (
                                    <button
                                        onClick={handleTrackIn3D}
                                        title={t('btnTrack3D', 'Suivre en 3D')}
                                        style={{ background: '#10b981', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: 4, fontSize: 12, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                                    >
                                        <Crosshair size={13} /> {t('btnTrack3D', 'Suivre en 3D')}
                                    </button>
                                )}
                            </div>
                        </div>

                        {trackedAntData ? (
                            <>
                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                                    gap: 12,
                                    background: inputBg,
                                    padding: '14px',
                                    borderRadius: 8,
                                    border: `1px solid ${borderCol}`
                                }}>
                                    <div>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.id', 'Identifiant :')}</span>
                                        <strong style={{ fontSize: 13, color: '#38bdf8' }}>{trackedAntData.id}</strong>
                                    </div>

                                    <div>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.colony', 'Colonie & Espèce :')}</span>
                                        <strong style={{ fontSize: 12 }}>{trackedAntData.colonyName || 'Colonie #1'} ({trackedAntData.species || 'Formica fusca'})</strong>
                                    </div>

                                    <div>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.caste_age', 'Caste & Âge :')}</span>
                                        <strong style={{ fontSize: 12, color: '#10b981' }}>{trackedAntData.caste} ({(trackedAntData.age || 14).toFixed(0)} j)</strong>
                                    </div>

                                    <div>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.health', 'Santé & Énergie :')}</span>
                                        <strong style={{ fontSize: 12 }}>{(trackedAntData.health || 100).toFixed(1)}% | {(trackedAntData.energy || 95).toFixed(1)}%</strong>
                                    </div>

                                    <div>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.distance', 'Distance au Nid :')}</span>
                                        <strong style={{ fontSize: 12 }}>
                                            {(trackedAntData.distanceTraveled || 0).toFixed(2)} m
                                        </strong>
                                    </div>

                                    <div>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.payload', 'Charge transportée :')}</span>
                                        <strong style={{ fontSize: 12 }}>
                                            {trackedAntData.carriedItem && trackedAntData.carriedItem !== 'NONE' ? `${trackedAntData.carriedItem} (5.2 mg)` : '0.0 mg'}
                                        </strong>
                                    </div>

                                    <div style={{ gridColumn: 'span 2' }}>
                                        <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>{t('stats.indiv.task', 'Tâche / Éthologie :')}</span>
                                        <strong style={{ fontSize: 12, color: '#38bdf8' }}>
                                            {trackedAntData.caste === 'QUEEN' ? '👑 Soins Royaux / Ponte' : (trackedAntData.job === 'FORAGER' ? '🌾 Fourragement (Récolte / Transport)' : (trackedAntData.job === 'BUILDER' ? '⛏️ Excavation / Creusage' : (trackedAntData.caste === 'SOLDIER' ? '🛡️ Garde & Défense' : '💤 Repos / Inactivité')))} [{trackedAntData.job || 'ACTIF'}]
                                        </strong>
                                    </div>
                                </div>

                                {/* Live Individual Ant Telemetry Chart */}
                                <ScientificLineChart
                                    title={`📊 ${t('stats.chart.individual.title', 'Télémétrie Individuelle & Évolution')} - ${trackedAntData.id}`}
                                    yAxisLabel={t('stats.chart.individual.y', 'Valeurs Métriques (% / m)')}
                                    xAxisLabel={t('stats.chart.x_axis', 'Temps Réel Écoulé (secondes)')}
                                    yUnit="%"
                                    data={activeData}
                                    isDark={isDark}
                                    visibleSeriesKeys={visibleSeries}
                                    onToggleSeries={toggleSeries}
                                    series={[
                                        { key: 'antHealth', name: t('stats.series.ant_health', 'Santé Individuelle (%)'), color: '#ef4444', getValue: () => trackedAntData.health || 100 },
                                        { key: 'antEnergy', name: t('stats.series.ant_energy', 'Énergie / Réserves (%)'), color: '#f59e0b', getValue: () => trackedAntData.energy || 95 },
                                        { key: 'antDistance', name: t('stats.series.ant_distance', 'Distance Parcourue (m)'), color: '#38bdf8', getValue: () => trackedAntData.distanceTraveled || 0 }
                                    ]}
                                />
                            </>
                        ) : (
                            <div style={{ textAlign: 'center', padding: '24px', color: textMuted, fontSize: 12 }}>
                                {t('stats.ant_search.tt', 'Saisissez un identifiant (ex. ant_1) ou utilisez les flèches pour inspecter un individu en temps réel.')}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    )
}
