import React, { useState, useMemo } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import {
    BarChart3,
    TrendingUp,
    Users,
    Heart,
    Zap,
    Utensils,
    Shield,
    Activity,
    Compass,
    Crosshair,
    Maximize2,
    ZoomIn,
    ZoomOut,
    Eye
} from 'lucide-react'

export default function StatisticsDashboardPanel() {
    const {
        stats,
        tick,
        ants,
        selectedEntity,
        setSelectedEntity,
        antTrackingEnabled,
        setAntTrackingEnabled,
        language,
        theme
    } = useSimulationStore()

    const [zoomWindow, setZoomWindow] = useState(60)
    const [selectedCasteFilter, setSelectedCasteFilter] = useState('ALL')
    const [history, setHistory] = useState([])

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    // Collect historical ticks for charts
    React.useEffect(() => {
        if (tick === 0) {
            setHistory([{
                tick: 0,
                total: stats.totalPopulation || 0,
                workers: stats.totalWorkers || 0,
                soldiers: stats.totalSoldiers || 0,
                queens: stats.totalQueens || 1,
                larvae: stats.totalLarvae || 0,
                eggs: stats.totalEggs || 0,
                biomass: stats.colonyBiomass || (stats.totalPopulation || 0) * 0.005,
                food: stats.foodStored || 150
            }])
            return
        }

        setHistory(prev => {
            const entry = {
                tick,
                total: stats.totalPopulation || ants.length || 0,
                workers: stats.totalWorkers || 0,
                soldiers: stats.totalSoldiers || 0,
                queens: stats.totalQueens || 1,
                larvae: stats.totalLarvae || 0,
                eggs: stats.totalEggs || 0,
                biomass: stats.colonyBiomass || (ants.length || 0) * 0.005,
                food: stats.foodStored || 150
            }
            const updated = [...prev, entry]
            return updated.length > 500 ? updated.slice(updated.length - 500) : updated
        })
    }, [tick, stats, ants])

    const displayedHistory = useMemo(() => {
        if (zoomWindow === -1 || history.length <= zoomWindow) return history
        return history.slice(history.length - zoomWindow)
    }, [history, zoomWindow])

    // Scale calculation for SVG chart
    const maxVal = useMemo(() => {
        if (displayedHistory.length === 0) return 10
        let m = 0
        displayedHistory.forEach(h => {
            if (h.total > m) m = h.total
        })
        return Math.max(m * 1.15, 10)
    }, [displayedHistory])

    const w = 340
    const h = 130
    const p = 10
    const n = Math.max(displayedHistory.length, 2)
    const getX = (i) => p + (i / (n - 1)) * (w - 2 * p)
    const getY = (v) => h - p - ((v || 0) / maxVal) * (h - 2 * p)

    const makePath = (key) => {
        if (displayedHistory.length < 2) return ''
        return displayedHistory.map((pt, i) => `${i === 0 ? 'M' : 'L'} ${getX(i)} ${getY(pt[key])}`).join(' ')
    }

    const currentAnt = selectedEntity || (ants && ants.length > 0 ? ants[0] : null)

    return (
        <div style={{
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.94)',
            backdropFilter: 'blur(16px)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 12,
            padding: 14,
            color: isDark ? '#fff' : '#0f172a',
            display: 'flex',
            flexDirection: 'column',
            gap: 12,
            maxHeight: 'calc(100vh - 140px)',
            overflowY: 'auto'
        }}>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)', paddingBottom: 8 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 800, color: isDark ? '#38bdf8' : '#0284c7', fontSize: 13 }}>
                    <BarChart3 size={16} />
                    <span>{t('statsTitle', 'Tableau de Bord & Télémétrie')}</span>
                </div>
                <div style={{ fontSize: 10, color: isDark ? '#94a3b8' : '#64748b', background: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)', padding: '2px 8px', borderRadius: 4 }}>
                    Tick #{tick}
                </div>
            </div>

            {/* Demographics Overview Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 6 }}>
                <div style={{ background: 'rgba(56, 189, 248, 0.1)', border: '1px solid rgba(56, 189, 248, 0.25)', borderRadius: 8, padding: '8px 6px', textAlign: 'center' }}>
                    <div style={{ fontSize: 10, color: isDark ? '#38bdf8' : '#0284c7', fontWeight: 600 }}>🐜 {t('totalPop', 'Population')}</div>
                    <div style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#fff' : '#0f172a' }}>{ants.length || stats.totalPopulation || 0}</div>
                </div>
                <div style={{ background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.25)', borderRadius: 8, padding: '8px 6px', textAlign: 'center' }}>
                    <div style={{ fontSize: 10, color: '#f59e0b', fontWeight: 600 }}>🌾 {t('workers', 'Ouvrières')}</div>
                    <div style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#fff' : '#0f172a' }}>{stats.totalWorkers || Math.floor((ants.length || 0) * 0.75)}</div>
                </div>
                <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.25)', borderRadius: 8, padding: '8px 6px', textAlign: 'center' }}>
                    <div style={{ fontSize: 10, color: '#ef4444', fontWeight: 600 }}>⚔️ {t('soldiers', 'Soldats')}</div>
                    <div style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#fff' : '#0f172a' }}>{stats.totalSoldiers || Math.floor((ants.length || 0) * 0.20)}</div>
                </div>
                <div style={{ background: 'rgba(168, 85, 247, 0.1)', border: '1px solid rgba(168, 85, 247, 0.25)', borderRadius: 8, padding: '8px 6px', textAlign: 'center' }}>
                    <div style={{ fontSize: 10, color: '#a855f7', fontWeight: 600 }}>👑 {t('queens', 'Reines')}</div>
                    <div style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#fff' : '#0f172a' }}>{stats.totalQueens || 1}</div>
                </div>
                <div style={{ background: 'rgba(34, 197, 94, 0.1)', border: '1px solid rgba(34, 197, 94, 0.25)', borderRadius: 8, padding: '8px 6px', textAlign: 'center' }}>
                    <div style={{ fontSize: 10, color: '#22c55e', fontWeight: 600 }}>🍼 {t('brood', 'Couvain')}</div>
                    <div style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#fff' : '#0f172a' }}>{(stats.totalEggs || 0) + (stats.totalLarvae || 0)}</div>
                </div>
                <div style={{ background: 'rgba(148, 163, 184, 0.1)', border: '1px solid rgba(148, 163, 184, 0.25)', borderRadius: 8, padding: '8px 6px', textAlign: 'center' }}>
                    <div style={{ fontSize: 10, color: isDark ? '#94a3b8' : '#64748b', fontWeight: 600 }}>🍯 {t('biomass', 'Biomasse')}</div>
                    <div style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#fff' : '#0f172a' }}>{((ants.length || 0) * 0.005).toFixed(2)}g</div>
                </div>
            </div>

            {/* Dynamic Multi-Caste Population Chart */}
            <div style={{ background: isDark ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.04)', border: isDark ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)', borderRadius: 8, padding: 10 }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 6 }}>
                    <div style={{ fontSize: 11, fontWeight: 700, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', gap: 4 }}>
                        <TrendingUp size={13} color="#38bdf8" />
                        <span>{t('popHistory', 'Courbe Démographique')}</span>
                    </div>
                    <div style={{ display: 'flex', gap: 4 }}>
                        <button
                            onClick={() => setZoomWindow(w => w === -1 ? 60 : Math.max(15, Math.floor(w / 1.5)))}
                            style={{ background: isDark ? '#1e293b' : '#e2e8f0', border: isDark ? '1px solid #334155' : '1px solid #cbd5e1', color: isDark ? '#38bdf8' : '#0369a1', padding: '2px 6px', borderRadius: 4, cursor: 'pointer', fontSize: 10 }}
                            title="Zoom avant"
                        >
                            +
                        </button>
                        <button
                            onClick={() => setZoomWindow(w => w === -1 ? -1 : (w * 1.5 > 600 ? -1 : Math.floor(w * 1.5)))}
                            style={{ background: isDark ? '#1e293b' : '#e2e8f0', border: isDark ? '1px solid #334155' : '1px solid #cbd5e1', color: isDark ? '#38bdf8' : '#0369a1', padding: '2px 6px', borderRadius: 4, cursor: 'pointer', fontSize: 10 }}
                            title="Zoom arrière"
                        >
                            -
                        </button>
                    </div>
                </div>

                <svg width="100%" height={h} viewBox={`0 0 ${w} ${h}`} style={{ overflow: 'visible' }}>
                    {/* Background grid lines */}
                    <line x1={p} y1={p} x2={w - p} y2={p} stroke={isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)'} strokeDasharray="3 3" />
                    <line x1={p} y1={h / 2} x2={w - p} y2={h / 2} stroke={isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)'} strokeDasharray="3 3" />
                    <line x1={p} y1={h - p} x2={w - p} y2={h - p} stroke={isDark ? 'rgba(255,255,255,0.15)' : 'rgba(0,0,0,0.15)'} />

                    {/* Series Paths */}
                    <path d={makePath('total')} fill="none" stroke="#38bdf8" strokeWidth={2} />
                    <path d={makePath('workers')} fill="none" stroke="#f59e0b" strokeWidth={1.5} strokeDasharray="2 2" />
                    <path d={makePath('soldiers')} fill="none" stroke="#ef4444" strokeWidth={1.5} />

                    {/* Max Pop Label */}
                    <text x={w - p - 2} y={p + 8} fill={isDark ? '#64748b' : '#94a3b8'} fontSize={9} textAnchor="end">{Math.round(maxVal)}</text>
                </svg>

                {/* Legend */}
                <div style={{ display: 'flex', justifyContent: 'center', gap: 12, marginTop: 4, fontSize: 10 }}>
                    <span style={{ color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 3 }}>● {t('filterAll', 'Total')}</span>
                    <span style={{ color: '#f59e0b', display: 'flex', alignItems: 'center', gap: 3 }}>-- {t('workers', 'Ouvrières')}</span>
                    <span style={{ color: '#ef4444', display: 'flex', alignItems: 'center', gap: 3 }}>━ {t('soldiers', 'Soldats')}</span>
                </div>
            </div>

            {/* Individual Ant Live Telemetry Card */}
            {currentAnt && (
                <div style={{ background: isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(241, 245, 249, 0.9)', border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.4)', borderRadius: 8, padding: 10 }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 700, fontSize: 12, color: '#f59e0b' }}>
                            <Crosshair size={14} color="#f59e0b" />
                            <span>{t('antInspector', 'Télémétrie')} #{currentAnt.id || 'A-01'}</span>
                        </div>
                        <button
                            onClick={() => {
                                setAntTrackingEnabled(true)
                                setSelectedEntity(currentAnt)
                            }}
                            style={{
                                background: antTrackingEnabled && selectedEntity?.id === currentAnt.id ? '#0284c7' : (isDark ? '#334155' : '#e2e8f0'),
                                border: '1px solid #38bdf8',
                                color: antTrackingEnabled && selectedEntity?.id === currentAnt.id ? '#fff' : (isDark ? '#fff' : '#0f172a'),
                                padding: '3px 8px',
                                borderRadius: 4,
                                fontSize: 10,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 4
                            }}
                        >
                            <Eye size={12} />
                            <span>{antTrackingEnabled && selectedEntity?.id === currentAnt.id ? t('followCamera', 'Suivie (Caméra)') : t('followCamera', 'Suivre en 3D')}</span>
                        </button>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px 12px', fontSize: 11 }}>
                        <div style={{ color: isDark ? '#94a3b8' : '#64748b' }}>{t('caste', 'Caste')}: <span style={{ color: isDark ? '#fff' : '#0f172a', fontWeight: 600 }}>{currentAnt.caste || 'Ouvrière'}</span></div>
                        <div style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Stade: <span style={{ color: isDark ? '#fff' : '#0f172a', fontWeight: 600 }}>{currentAnt.lifeStage || 'Adulte'}</span></div>
                        <div style={{ color: isDark ? '#94a3b8' : '#64748b' }}>{t('job', 'Tâche')}: <span style={{ color: isDark ? '#38bdf8' : '#0284c7', fontWeight: 600 }}>{currentAnt.task || 'Fourrageuse (Collecte)'}</span></div>
                        <div style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Pos: <span style={{ color: isDark ? '#fff' : '#0f172a', fontWeight: 600 }}>({Math.round(currentAnt.x || 50)}, {Math.round(currentAnt.z || currentAnt.y || 50)})</span></div>
                    </div>

                    {/* Vitals Progress Bars */}
                    <div style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 5 }}>
                        <div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: isDark ? '#94a3b8' : '#64748b', marginBottom: 2 }}>
                                <span>{t('health', 'Santé')}</span>
                                <span style={{ color: '#4ade80' }}>{Math.round(currentAnt.health ?? 100)}%</span>
                            </div>
                            <div style={{ height: 5, background: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)', borderRadius: 3, overflow: 'hidden' }}>
                                <div style={{ width: `${Math.max(0, Math.min(100, currentAnt.health ?? 100))}%`, height: '100%', background: '#22c55e' }} />
                            </div>
                        </div>
                        <div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: isDark ? '#94a3b8' : '#64748b', marginBottom: 2 }}>
                                <span>{t('energy', 'Énergie')}</span>
                                <span style={{ color: '#38bdf8' }}>{Math.round(currentAnt.energy ?? 85)}%</span>
                            </div>
                            <div style={{ height: 5, background: isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)', borderRadius: 3, overflow: 'hidden' }}>
                                <div style={{ width: `${Math.max(0, Math.min(100, currentAnt.energy ?? 85))}%`, height: '100%', background: '#0284c7' }} />
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
