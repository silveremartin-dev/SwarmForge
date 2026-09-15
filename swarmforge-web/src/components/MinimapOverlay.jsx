import React, { useRef, useEffect, useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { Map, Eye, EyeOff, ZoomIn, ZoomOut, Crosshair, ChevronDown, ChevronUp } from 'lucide-react'

export default function MinimapOverlay() {
    const {
        ants,
        colonies,
        foodSources,
        predators,
        showMinimap,
        toggleMinimap,
        language,
        theme,
        triggerCameraReset
    } = useSimulationStore()

    const canvasRef = useRef(null)
    const [collapsed, setCollapsed] = useState(false)
    const isDark = theme === 'dark'

    const t = (key, fallback) => getTranslation(language, key, fallback)

    const mapSize = 160

    useEffect(() => {
        if (!showMinimap || collapsed) return
        const canvas = canvasRef.current
        if (!canvas) return
        const ctx = canvas.getContext('2d')
        if (!ctx) return

        // Clear canvas
        ctx.fillStyle = isDark ? '#0b0f19' : '#e2e8f0'
        ctx.fillRect(0, 0, mapSize, mapSize)

        // Draw terrain grid & border
        ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.15)' : 'rgba(15, 23, 42, 0.15)'
        ctx.lineWidth = 1
        const step = mapSize / 4
        for (let i = 1; i < 4; i++) {
            ctx.beginPath()
            ctx.moveTo(i * step, 0)
            ctx.lineTo(i * step, mapSize)
            ctx.stroke()
            ctx.beginPath()
            ctx.moveTo(0, i * step)
            ctx.lineTo(mapSize, i * step)
            ctx.stroke()
        }

        // Draw circular radar range rings
        ctx.strokeStyle = isDark ? 'rgba(56, 189, 248, 0.25)' : 'rgba(56, 189, 248, 0.4)'
        ctx.beginPath()
        ctx.arc(mapSize / 2, mapSize / 2, mapSize * 0.25, 0, Math.PI * 2)
        ctx.stroke()
        ctx.beginPath()
        ctx.arc(mapSize / 2, mapSize / 2, mapSize * 0.45, 0, Math.PI * 2)
        ctx.stroke()

        // Draw Food Sources (Green squares)
        if (foodSources && Array.isArray(foodSources)) {
            foodSources.forEach(f => {
                const fx = (f.x / 100) * mapSize
                const fy = (f.y / 100) * mapSize
                ctx.fillStyle = '#22c55e'
                ctx.beginPath()
                ctx.arc(fx, fy, 3.5, 0, Math.PI * 2)
                ctx.fill()
            })
        }

        // Draw Nests / Colonies (Large rings)
        if (colonies && Array.isArray(colonies)) {
            colonies.forEach(c => {
                const nx = (c.x / 100) * mapSize
                const ny = (c.y / 100) * mapSize
                ctx.strokeStyle = c.color || '#a855f7'
                ctx.lineWidth = 2
                ctx.beginPath()
                ctx.arc(nx, ny, 6, 0, Math.PI * 2)
                ctx.stroke()
                ctx.fillStyle = c.color || '#a855f7'
                ctx.beginPath()
                ctx.arc(nx, ny, 2.5, 0, Math.PI * 2)
                ctx.fill()
            })
        }

        // Draw Ants (Dots colored by caste)
        if (ants && Array.isArray(ants)) {
            ants.forEach(ant => {
                const ax = (ant.x / 100) * mapSize
                const ay = (ant.y / 100) * mapSize
                if (ant.caste === 'QUEEN') {
                    ctx.fillStyle = '#a855f7'
                    ctx.beginPath()
                    ctx.arc(ax, ay, 3, 0, Math.PI * 2)
                    ctx.fill()
                } else if (ant.caste === 'SOLDIER') {
                    ctx.fillStyle = '#ef4444'
                    ctx.beginPath()
                    ctx.arc(ax, ay, 2, 0, Math.PI * 2)
                    ctx.fill()
                } else {
                    ctx.fillStyle = '#f59e0b'
                    ctx.fillRect(ax - 1, ay - 1, 2, 2)
                }
            })
        }

        // Draw Predators (Crimson triangles)
        if (predators && Array.isArray(predators)) {
            predators.forEach(p => {
                const px = (p.x / 100) * mapSize
                const py = (p.y / 100) * mapSize
                ctx.fillStyle = '#e11d48'
                ctx.beginPath()
                ctx.moveTo(px, py - 4)
                ctx.lineTo(px + 4, py + 3)
                ctx.lineTo(px - 4, py + 3)
                ctx.closePath()
                ctx.fill()
            })
        }

        // Compass orientation indicator
        ctx.fillStyle = '#38bdf8'
        ctx.font = 'bold 9px system-ui'
        ctx.fillText('N', mapSize / 2 - 3, 10)
    }, [ants, colonies, foodSources, predators, showMinimap, collapsed, isDark])

    if (!showMinimap) return null

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            right: 20,
            zIndex: 92,
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.94)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.35)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 12,
            padding: 8,
            boxShadow: '0 8px 24px rgba(0,0,0,0.5)',
            backdropFilter: 'blur(12px)',
            color: isDark ? '#fff' : '#0f172a',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            transition: 'all 0.2s ease',
            display: 'flex',
            flexDirection: 'column',
            gap: 6
        }}>
            {/* Header */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: 4,
                borderBottom: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)',
                fontSize: 11,
                fontWeight: 700,
                color: isDark ? '#38bdf8' : '#0284c7'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                    <Map size={13} />
                    <span>{t('minimapTitle', 'Radar 2D')}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <button
                        onClick={triggerCameraReset}
                        title="Recentrer la caméra 3D"
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            color: isDark ? '#94a3b8' : '#64748b',
                            padding: 2,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        <Crosshair size={13} />
                    </button>
                    <button
                        onClick={() => setCollapsed(!collapsed)}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            color: isDark ? '#94a3b8' : '#64748b',
                            padding: 2,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        {collapsed ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                    </button>
                </div>
            </div>

            {/* Radar Canvas */}
            {!collapsed && (
                <>
                    <canvas
                        ref={canvasRef}
                        width={mapSize}
                        height={mapSize}
                        style={{
                            borderRadius: 6,
                            border: isDark ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
                            cursor: 'crosshair',
                            display: 'block'
                        }}
                    />
                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        fontSize: 9,
                        color: isDark ? '#94a3b8' : '#64748b',
                        paddingTop: 2
                    }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                            <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#f59e0b', display: 'inline-block' }}></span>
                            {t('workers', 'Ouvrières')}
                        </span>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                            <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#ef4444', display: 'inline-block' }}></span>
                            {t('soldiers', 'Soldats')}
                        </span>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                            <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#22c55e', display: 'inline-block' }}></span>
                            {t('filterFood', 'Nourriture')}
                        </span>
                    </div>
                </>
            )}
        </div>
    )
}
