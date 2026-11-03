import React from 'react'

/**
 * Filter bar allowing users to toggle visible series across scientific line charts.
 */
export function StatisticsSeriesFilterBar({
    t,
    cardBg,
    borderCol,
    textMain,
    textMuted,
    isDark,
    visibleSeries,
    toggleSeries,
    toggleAllSeries
}) {
    const seriesItems = [
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
    ]

    return (
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
                {seriesItems.map(item => {
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
    )
}

/**
 * Top KPI Summary panel displaying demographic, resources and timing metrics.
 */
export function StatisticsSummaryKpis({
    t,
    cardBg,
    borderCol,
    textMain,
    textMuted,
    totalPopCount,
    coloniesCount,
    totalQueensCount,
    totalWorkersCount,
    totalSoldiersCount,
    totalFoodReserves,
    totalWaterReserves,
    measuredTps,
    simTimeFormatted,
    simTimeSeconds
}) {
    return (
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
                <strong style={{ fontSize: 15, color: '#a855f7' }}>{coloniesCount}</strong>
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
    )
}
