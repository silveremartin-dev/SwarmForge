import React from 'react'
import {
    Search,
    User,
    ChevronLeft,
    ChevronRight,
    Crosshair
} from 'lucide-react'

// High-Fidelity Scientific SVG Line Chart Component with explicit X/Y axes and Metric SI Units
export function ScientificLineChart({
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
    isDark = true
}) {
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

    const startTimeSec = data[0]?.simTimeSeconds ?? 0
    const endTimeSec = data[data.length - 1]?.simTimeSeconds ?? (startTimeSec + data.length)
    const durationSec = Math.max(1, endTimeSec - startTimeSec)

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

            <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', height: height, background: isDark ? 'rgba(0,0,0,0.22)' : 'rgba(248,250,252,0.8)', borderRadius: 6, overflow: 'visible' }}>
                <line x1={padding.left} y1={padding.top} x2={width - padding.right} y2={padding.top} stroke={gridLineColor} strokeDasharray="3 3" />
                <line x1={padding.left} y1={padding.top + innerHeight / 2} x2={width - padding.right} y2={padding.top + innerHeight / 2} stroke={gridLineColor} strokeDasharray="3 3" />
                <line x1={padding.left} y1={padding.top + innerHeight} x2={width - padding.right} y2={padding.top + innerHeight} stroke={axisColor} strokeWidth="1.2" />

                <line x1={padding.left} y1={padding.top} x2={padding.left} y2={padding.top + innerHeight} stroke={axisColor} strokeWidth="1.2" />
                <line x1={padding.left + innerWidth / 2} y1={padding.top} x2={padding.left + innerWidth / 2} y2={padding.top + innerHeight} stroke={gridLineColor} strokeDasharray="3 3" />
                <line x1={width - padding.right} y1={padding.top} x2={width - padding.right} y2={padding.top + innerHeight} stroke={gridLineColor} strokeDasharray="3 3" />

                <text x={padding.left - 6} y={padding.top + 4} fill={textColor} fontSize="9" fontWeight="700" textAnchor="end">{maxY} {yUnit}</text>
                <text x={padding.left - 6} y={padding.top + innerHeight / 2 + 3} fill={textColor} fontSize="8.5" textAnchor="end">{midY} {yUnit}</text>
                <text x={padding.left - 6} y={padding.top + innerHeight + 1} fill={textColor} fontSize="9" fontWeight="700" textAnchor="end">0 {yUnit}</text>

                <text x={padding.left - 4} y={padding.top - 8} fill={isDark ? '#38bdf8' : '#0284c7'} fontSize="9" fontWeight="800" textAnchor="start">{yAxisLabel}</text>

                <text x={padding.left} y={padding.top + innerHeight + 16} fill={textColor} fontSize="9" fontWeight="600" textAnchor="start">{formatTimeTick(startTimeSec)}</text>
                <text x={padding.left + innerWidth / 2} y={padding.top + innerHeight + 16} fill={textColor} fontSize="9" fontWeight="600" textAnchor="middle">{formatTimeTick(startTimeSec + durationSec / 2)}</text>
                <text x={width - padding.right} y={padding.top + innerHeight + 16} fill={textColor} fontSize="9" fontWeight="600" textAnchor="end">{formatTimeTick(endTimeSec)}</text>

                <text x={padding.left + innerWidth / 2} y={padding.top + innerHeight + 28} fill={textColor} fontSize="9" fontWeight="700" textAnchor="middle">
                    {xAxisLabel || 'Temps Réel Écoulé (secondes)'}
                </text>

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

export function IndividualAntInspectionCard({
    t,
    isDark,
    cardBg,
    borderCol,
    inputBg,
    textMain,
    textMuted,
    searchAntId,
    setSearchAntId,
    handleSearchAnt,
    cycleAnt,
    trackedAntData,
    handleTrackIn3D,
    activeData,
    visibleSeries,
    toggleSeries
}) {
    return (
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
    )
}
