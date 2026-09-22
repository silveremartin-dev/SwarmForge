import React, { useState } from 'react'
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
    Eye
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'

// SVG Line Chart Component for real-time telemetry
function DynamicLineChart({ data, series, height = 150, title, unit = '' }) {
    if (!data || data.length === 0) {
        return (
            <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b', fontSize: 12 }}>
                En attente de données télémétriques...
            </div>
        )
    }

    const padding = { top: 14, right: 16, bottom: 20, left: 40 }
    const width = 460
    const innerWidth = width - padding.left - padding.right
    const innerHeight = height - padding.top - padding.bottom

    let maxY = 1
    series.forEach(s => {
        data.forEach(d => {
            const val = s.getValue(d)
            if (val > maxY) maxY = val
        })
    })
    maxY = Math.ceil(maxY * 1.15) || 10

    const pointsBySeries = series.map(s => {
        return data.map((d, idx) => {
            const x = padding.left + (idx / Math.max(1, data.length - 1)) * innerWidth
            const val = s.getValue(d)
            const y = padding.top + innerHeight - (val / maxY) * innerHeight
            return `${x},${y}`
        }).join(' ')
    })

    return (
        <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 6 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 12, fontWeight: 700 }}>
                <span>{title}</span>
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    {series.map(s => (
                        <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 10, color: s.color }}>
                            <span style={{ width: 8, height: 8, borderRadius: '50%', background: s.color }} />
                            <span>{s.name}</span>
                        </div>
                    ))}
                </div>
            </div>

            <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', height: height, background: 'rgba(0,0,0,0.15)', borderRadius: 6 }}>
                <line x1={padding.left} y1={padding.top} x2={width - padding.right} y2={padding.top} stroke="rgba(255,255,255,0.06)" />
                <line x1={padding.left} y1={padding.top + innerHeight / 2} x2={width - padding.right} y2={padding.top + innerHeight / 2} stroke="rgba(255,255,255,0.06)" />
                <line x1={padding.left} y1={padding.top + innerHeight} x2={width - padding.right} y2={padding.top + innerHeight} stroke="rgba(255,255,255,0.1)" />

                <text x={padding.left - 4} y={padding.top + 8} fill="#64748b" fontSize="9" textAnchor="end">{maxY}{unit}</text>
                <text x={padding.left - 4} y={padding.top + innerHeight} fill="#64748b" fontSize="9" textAnchor="end">0{unit}</text>

                {series.map((s, sIdx) => (
                    <polyline
                        key={s.name}
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
        trackedAntId,
        trackedAntData,
        setTrackedAntId,
        setFollowAntCamera,
        setActiveSubTab,
        theme,
        language
    } = useSimulationStore()

    const [searchAntId, setSearchAntId] = useState('')
    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const handleSearchAnt = () => {
        if (!searchAntId.trim()) return
        const ant = ants.find(a => a.id.toLowerCase().includes(searchAntId.trim().toLowerCase()))
        if (ant) {
            setTrackedAntId(ant.id)
            showToast(`✓ Individu sélectionné : ${ant.id}`, 'info')
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
            showToast('Aucune donnée télémétrique à exporter', 'warning')
            return
        }

        let csv = 'Tick;TempsSecondes;PopulationTotale;Reines;Ouvrieres;Soldats;Males;Nourriture;Eau;Proteines;Temperature;Pluie;TPS\n'
        statsHistory.forEach(s => {
            csv += `${s.tick};${s.simTimeSeconds.toFixed(1)};${s.totalPopulation};${s.casteBreakdown.queens};${s.casteBreakdown.workers};${s.casteBreakdown.soldiers};${s.casteBreakdown.males};${s.resources.food.toFixed(1)};${s.resources.water.toFixed(1)};${s.resources.protein.toFixed(1)};${s.weather.temp.toFixed(1)};${s.weather.rain.toFixed(1)};${s.performance.tps}\n`
        })

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `swarmforge_statistics_${Date.now()}.csv`
        a.click()
        showToast('📊 Export CSV téléchargé avec succès !', 'success')
    }

    const windowPointsCount = timeWindow === '1m' ? 12 : (timeWindow === '3m' ? 36 : (timeWindow === '10m' ? 120 : (timeWindow === '30m' ? 360 : 1000)))
    const activeData = statsHistory.slice(-windowPointsCount)

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    return (
        <div style={{
            maxWidth: 1150,
            margin: '0 auto',
            padding: '20px 24px',
            display: 'flex',
            flexDirection: 'column',
            gap: 20
        }}>
            {/* Header & Window Controls Bar */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
                <div>
                    <h2 style={{ margin: 0, fontSize: 18, fontWeight: 800, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 8 }}>
                        <BarChart2 size={20} /> Tableau de Bord & Télémétrie Dynamique
                    </h2>
                    <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                        Graphiques de population, castes, ressources biochimiques, climat, comportements éthologiques et TPS moteur.
                    </p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <div style={{ display: 'flex', background: cardBg, padding: 3, borderRadius: 8, border: `1px solid ${borderCol}` }}>
                        {['1m', '3m', '10m', '30m', '1h', 'all'].map(w => (
                            <button
                                key={w}
                                onClick={() => setTimeWindow(w)}
                                style={{
                                    padding: '4px 10px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    borderRadius: 5,
                                    border: 'none',
                                    cursor: 'pointer',
                                    background: timeWindow === w ? '#0284c7' : 'transparent',
                                    color: timeWindow === w ? '#fff' : textMuted
                                }}
                            >
                                {w}
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
                            padding: '7px 14px',
                            borderRadius: 6,
                            fontSize: 12,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Download size={14} /> Exporter CSV
                    </button>
                </div>
            </div>

            {/* 6 Grid Dynamic Line Charts */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
                gap: 16
            }}>
                {/* 1. Multi-Colony Population */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: 16 }}>
                    <DynamicLineChart
                        title="1. Population Multi-Colonies"
                        data={activeData}
                        series={colonies.map(c => ({
                            name: c.name,
                            color: c.color || '#38bdf8',
                            getValue: (d) => d.coloniesPop?.[c.id] || 0
                        }))}
                    />
                </div>

                {/* 2. Castes Breakdown */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: 16 }}>
                    <DynamicLineChart
                        title="2. Répartition des Castes"
                        data={activeData}
                        series={[
                            { name: 'Ouvrières', color: '#38bdf8', getValue: (d) => d.casteBreakdown.workers },
                            { name: 'Soldats', color: '#ef4444', getValue: (d) => d.casteBreakdown.soldiers },
                            { name: 'Reines', color: '#eab308', getValue: (d) => d.casteBreakdown.queens },
                            { name: 'Mâles', color: '#a855f7', getValue: (d) => d.casteBreakdown.males }
                        ]}
                    />
                </div>

                {/* 3. Bio-Resources & Vital Dynamics */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: 16 }}>
                    <DynamicLineChart
                        title="3. Bio-Ressources (Nourriture & Eau)"
                        data={activeData}
                        unit="u"
                        series={[
                            { name: 'Nourriture', color: '#f59e0b', getValue: (d) => d.resources.food },
                            { name: 'Eau', color: '#06b6d4', getValue: (d) => d.resources.water },
                            { name: 'Protéines', color: '#ec4899', getValue: (d) => d.resources.protein }
                        ]}
                    />
                </div>

                {/* 4. Climate & Ecosystem */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: 16 }}>
                    <DynamicLineChart
                        title="4. Météo & Écosystème"
                        data={activeData}
                        series={[
                            { name: 'Température (°C)', color: '#f97316', getValue: (d) => d.weather.temp },
                            { name: 'Précipitations (mm)', color: '#3b82f6', getValue: (d) => d.weather.rain }
                        ]}
                    />
                </div>

                {/* 5. Ethological Behaviors Breakdown */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: 16 }}>
                    <DynamicLineChart
                        title="5. Comportements Éthologiques"
                        data={activeData}
                        series={[
                            { name: 'Récolte', color: '#10b981', getValue: (d) => d.behaviors.foraging },
                            { name: 'Excavation', color: '#d97706', getValue: (d) => d.behaviors.digging },
                            { name: 'Soins Couvain', color: '#ec4899', getValue: (d) => d.behaviors.nursing },
                            { name: 'Garde / Sentinelle', color: '#ef4444', getValue: (d) => d.behaviors.guarding }
                        ]}
                    />
                </div>

                {/* 6. Simulation Engine TPS Performance */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: 16 }}>
                    <DynamicLineChart
                        title="6. Performance Moteur (TPS)"
                        data={activeData}
                        unit=" TPS"
                        series={[
                            { name: 'TPS Réel', color: '#10b981', getValue: (d) => d.performance.tps },
                            { name: 'TPS Cible', color: '#64748b', getValue: (d) => d.performance.targetTps }
                        ]}
                    />
                </div>
            </div>

            {/* 7. Individual Ant Telemetry Inspector (1:1 with JavaFX createIndividualAntCard) */}
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
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#a855f7' }}>
                        <User size={18} />
                        <span style={{ fontSize: 14, fontWeight: 800 }}>
                            7. Inspection & Télémétrie d'un Individu Spécifique
                        </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <input
                            type="text"
                            placeholder="Identifiant de fourmi..."
                            value={searchAntId}
                            onChange={(e) => setSearchAntId(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSearchAnt()}
                            style={{ width: 170, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 8px', fontSize: 12 }}
                        />

                        <button
                            onClick={() => cycleAnt(-1)}
                            title="Fourmi précédente"
                            style={{ background: isDark ? '#334155' : '#e2e8f0', border: 'none', color: textMain, padding: '5px 8px', borderRadius: 4, cursor: 'pointer' }}
                        >
                            <ChevronLeft size={14} />
                        </button>

                        <button
                            onClick={() => cycleAnt(1)}
                            title="Fourmi suivante"
                            style={{ background: isDark ? '#334155' : '#e2e8f0', border: 'none', color: textMain, padding: '5px 8px', borderRadius: 4, cursor: 'pointer' }}
                        >
                            <ChevronRight size={14} />
                        </button>

                        <button
                            onClick={handleSearchAnt}
                            style={{ background: '#0284c7', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: 4, fontSize: 12, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                        >
                            <Search size={13} /> Rechercher
                        </button>

                        {trackedAntData && (
                            <button
                                onClick={handleTrackIn3D}
                                title="Suivre dans la vue 3D"
                                style={{ background: '#10b981', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: 4, fontSize: 12, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                            >
                                <Crosshair size={13} /> Suivre en 3D
                            </button>
                        )}
                    </div>
                </div>

                {trackedAntData ? (
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
                            <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>Identifiant :</span>
                            <strong style={{ fontSize: 13, color: '#38bdf8' }}>{trackedAntData.id}</strong>
                        </div>

                        <div>
                            <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>Colonie & Espèce :</span>
                            <strong style={{ fontSize: 12 }}>{trackedAntData.colonyName} ({trackedAntData.species})</strong>
                        </div>

                        <div>
                            <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>Caste & Tâche :</span>
                            <strong style={{ fontSize: 12, color: '#10b981' }}>{trackedAntData.caste} ({trackedAntData.task})</strong>
                        </div>

                        <div>
                            <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>Santé & Énergie :</span>
                            <strong style={{ fontSize: 12 }}>{trackedAntData.health.toFixed(1)}% | {trackedAntData.energy.toFixed(1)}%</strong>
                        </div>

                        <div>
                            <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>Distance & Vitesse :</span>
                            <strong style={{ fontSize: 12 }}>
                                {(trackedAntData.distanceTraveled || 0).toFixed(2)} m ({(trackedAntData.speedMms || 22.5).toFixed(1)} mm/s)
                            </strong>
                        </div>

                        <div>
                            <span style={{ fontSize: 11, color: textMuted, display: 'block' }}>Position (X, Z) & Charge :</span>
                            <strong style={{ fontSize: 12 }}>
                                ({trackedAntData.x.toFixed(1)}, {trackedAntData.z.toFixed(1)}) | {trackedAntData.carriedItem && trackedAntData.carriedItem !== 'NONE' ? '5.2 mg' : '0.0 mg'}
                            </strong>
                        </div>
                    </div>
                ) : (
                    <div style={{ textAlign: 'center', padding: '16px', color: textMuted, fontSize: 12 }}>
                        Aucun individu sélectionné. Utilisez la recherche ou les flèches pour inspecter un individu en temps réel.
                    </div>
                )}
            </div>
        </div>
    )
}
