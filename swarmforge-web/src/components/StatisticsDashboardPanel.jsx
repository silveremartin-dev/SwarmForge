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
import { ScientificLineChart, IndividualAntInspectionCard } from './StatisticsChartComponents'
import { StatisticsSeriesFilterBar, StatisticsSummaryKpis } from './StatisticsSeriesFilterBar'


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

                        {/* 2. KPI Summary Cards Panel */}
            <StatisticsSummaryKpis
                t={t}
                cardBg={cardBg}
                borderCol={borderCol}
                textMain={textMain}
                textMuted={textMuted}
                totalPopCount={totalPopCount}
                coloniesCount={colonies.length}
                totalQueensCount={totalQueensCount}
                totalWorkersCount={totalWorkersCount}
                totalSoldiersCount={totalSoldiersCount}
                totalFoodReserves={totalFoodReserves}
                totalWaterReserves={totalWaterReserves}
                measuredTps={measuredTps}
                simTimeFormatted={simTimeFormatted}
                simTimeSeconds={simTimeSeconds}
            />

            {/* 3. Global Series Selection Checkboxes Bar */}
            <StatisticsSeriesFilterBar
                t={t}
                cardBg={cardBg}
                borderCol={borderCol}
                textMain={textMain}
                textMuted={textMuted}
                isDark={isDark}
                visibleSeries={visibleSeries}
                toggleSeries={toggleSeries}
                toggleAllSeries={toggleAllSeries}
            />

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
                    <IndividualAntInspectionCard
                        t={t}
                        isDark={isDark}
                        cardBg={cardBg}
                        borderCol={borderCol}
                        inputBg={inputBg}
                        textMain={textMain}
                        textMuted={textMuted}
                        searchAntId={searchAntId}
                        setSearchAntId={setSearchAntId}
                        handleSearchAnt={handleSearchAnt}
                        cycleAnt={cycleAnt}
                        trackedAntData={trackedAntData}
                        handleTrackIn3D={handleTrackIn3D}
                        activeData={activeData}
                        visibleSeries={visibleSeries}
                        toggleSeries={toggleSeries}
                    />
                )}
            </div>
        </div>
    )
}
