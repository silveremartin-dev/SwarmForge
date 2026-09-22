import React from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { MapPin, Thermometer, Droplets, Sparkles, X, Wind, Activity, Layers } from 'lucide-react'

/**
 * Floating dynamic HUD overlay that follows the mouse cursor when hovering over terrain voxels.
 * Aligned 1:1 with WorldEditorPane.java line 4582.
 */
export default function VoxelMouseHoverHUD() {
    const { hoveredVoxel, setHoveredVoxel, theme, language } = useSimulationStore()
    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    if (!hoveredVoxel || !hoveredVoxel.isHovering) return null

    const { screenX, screenY, x, y, z, substrate, temp, humidity, phero, isPinned } = hoveredVoxel

    // Position tooltip offset from mouse pointer to prevent obscuring the cursor
    const posX = Math.max(20, Math.min(window.innerWidth - 310, (screenX ?? 100) + 16))
    const posY = Math.max(70, Math.min(window.innerHeight - 220, (screenY ?? 100) + 16))

    const vx = Math.round(x ?? 50)
    const vz = Math.round(z ?? 50)
    const vy = y ?? 0.0
    const altM = Math.max(0, vy).toFixed(2)
    const strataY = Math.max(0, Math.min(31, Math.round(31 - (Math.abs(vy) / 3.0) * 31)))

    const substrateColor = substrate?.color || '#854d0e'
    const substrateName = substrate?.name || t('substrateHumus', 'Humus Organique')
    const isRiver = substrate?.type === 'WATER' || (vx >= 19 && vx <= 31 && vy <= 0.05)

    const tempC = (temp ?? 22.0).toFixed(1)
    const humPct = Math.round(humidity ?? 65)
    const humBadge = humPct >= 55 && humPct <= 90 ? '🟢' : (humPct >= 35 ? '🟠' : '🔴')

    const co2Ppm = Math.round(415 + (31 - strataY) * 20 + (substrate?.type === 'CLAY' ? 120 : 0))
    const co2Pct = (co2Ppm / 10000.0).toFixed(3)
    const o2Pct = Math.max(12.0, (20.95 - (co2Ppm - 400) * 0.0006)).toFixed(1)
    const gasBadge = co2Ppm < 1500 && o2Pct >= 19.5 ? '🟢' : (co2Ppm < 5000 ? '🟠' : '🔴')
    const phVal = isRiver ? '7.2' : (substrate?.type === 'HUMUS' ? '6.4' : (substrate?.type === 'CLAY' ? '6.8' : '7.0'))
    const rootPct = substrate?.type === 'HUMUS' ? 45 : (substrate?.type === 'CLAY' ? 15 : 0)

    return (
        <div style={{
            position: 'fixed',
            left: posX,
            top: posY,
            zIndex: 120,
            pointerEvents: isPinned ? 'auto' : 'none',
            background: isDark ? 'rgba(15, 23, 42, 0.95)' : 'rgba(255, 255, 255, 0.97)',
            border: `1.5px solid ${isDark ? 'rgba(56, 189, 248, 0.5)' : 'rgba(2, 132, 199, 0.6)'}`,
            borderRadius: 8,
            padding: '8px 11px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.45)',
            backdropFilter: 'blur(10px)',
            color: isDark ? '#f1f5f9' : '#0f172a',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            fontSize: 10.5,
            minWidth: 265,
            maxWidth: 300,
            display: 'flex',
            flexDirection: 'column',
            gap: 4.5
        }}>
            {/* Header: Voxel Coordinates + Altitude + Close Button */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)', paddingBottom: 4 }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontWeight: 800, color: '#38bdf8' }}>
                    <MapPin size={12} />
                    Voxel ({vx}, {vz}) [{t('voxelStrata', 'Strate Y')}: {strataY}/31]
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                    <span style={{ fontSize: 9.5, color: isDark ? '#94a3b8' : '#64748b', fontFamily: 'monospace' }}>
                        {t('voxelAltitude', 'Alt')}: {altM}m
                    </span>
                    {isPinned && (
                        <button
                            onClick={() => setHoveredVoxel(null)}
                            title={t('close', 'Fermer')}
                            style={{
                                background: 'transparent',
                                border: 'none',
                                color: isDark ? '#94a3b8' : '#64748b',
                                cursor: 'pointer',
                                padding: 0,
                                display: 'flex'
                            }}
                        >
                            <X size={12} />
                        </button>
                    )}
                </div>
            </div>

            {/* Substrate Stratum & Habitat */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 6 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                    <span style={{
                        width: 9,
                        height: 9,
                        borderRadius: 2,
                        background: substrateColor,
                        boxShadow: `0 0 6px ${substrateColor}`,
                        flexShrink: 0
                    }} />
                    <span style={{ fontWeight: 700, color: isDark ? '#f8fafc' : '#0f172a', fontSize: 10.5 }}>
                        {substrateName}
                    </span>
                </div>
                <span style={{ fontSize: 9.5, color: isRiver ? '#38bdf8' : '#22c55e', fontWeight: 600 }}>
                    {isRiver ? t('voxelRiver', '🏞️ Fluvial') : t('voxelTerrestrial', '🌲 Terrestre')}
                </span>
            </div>

            {/* Microclimate: Temp, Humidity, pH, Roots (1:1 with WorldEditorPane.java) */}
            <div style={{ display: 'grid', gridTemplateColumns: '1.1fr 1fr', gap: 3, fontSize: 9.5, color: isDark ? '#cbd5e1' : '#334155' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Thermometer size={11} color="#f97316" />
                    <span>{tempC}°C | 💧 {humPct}% {humBadge}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Layers size={11} color="#10b981" />
                    <span>pH: {phVal} | 🌱 {rootPct}% rac.</span>
                </div>
            </div>

            {/* Atmospheric Gases (CO2 & O2) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 9.5, color: isDark ? '#94a3b8' : '#475569' }}>
                <Wind size={11} color="#a78bfa" />
                <span>CO₂: {co2Ppm} ppm ({co2Pct}%) {gasBadge} | O₂: {o2Pct}%</span>
            </div>

            {/* Local Pheromones */}
            {phero > 0.05 && (
                <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#a855f7', fontSize: 9.5, fontWeight: 700, marginTop: 1 }}>
                    <Sparkles size={11} />
                    <span>{t('voxelPheromones', 'Phéromone locale')} : {phero.toFixed(2)} ppm</span>
                </div>
            )}
        </div>
    )
}
