import React, { useRef, useEffect, useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { Map, Crosshair, ChevronDown, ChevronUp } from 'lucide-react'

/**
 * Minimap overlay matching the World Editor & SwarmForgeClient dual 2D maps system:
 * - Top-Down View: Ant density heatmap, nests, camera viewport rect, directional borders.
 * - Side Profile View: Stratigraphy bands (Humus, Argile, Bedrock), water table, subterranean ant depth & nests.
 */
export default function MinimapOverlay() {
    const {
        ants,
        colonies,
        nests,
        showMinimap,
        language,
        theme,
        triggerCameraReset
    } = useSimulationStore()

    const canvasTopRef = useRef(null)
    const canvasSideRef = useRef(null)
    const [collapsed, setCollapsed] = useState(false)
    const [showLegend, setShowLegend] = useState(true)
    const [syncViews, setSyncViews] = useState(true)

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const mapWidth = 200
    const topHeight = 140
    const sideHeight = 90
    const GRID_RES = 32

    useEffect(() => {
        if (!showMinimap || collapsed) return

        const canvasTop = canvasTopRef.current
        const canvasSide = canvasSideRef.current
        if (!canvasTop || !canvasSide) return

        const gcTop = canvasTop.getContext('2d')
        const gcSide = canvasSide.getContext('2d')
        if (!gcTop || !gcSide) return

        // 1. TOP-DOWN VIEW RENDERING
        const wTop = canvasTop.width
        const hTop = canvasTop.height

        // Clear Background (Dark Slate)
        gcTop.fillStyle = '#0f172a'
        gcTop.fillRect(0, 0, wTop, hTop)

        // Subdued Grid Lines
        gcTop.strokeStyle = 'rgba(51, 65, 85, 0.4)'
        gcTop.lineWidth = 0.5
        for (let i = 1; i < 4; i++) {
            gcTop.beginPath()
            gcTop.moveTo(i * wTop / 4, 1)
            gcTop.lineTo(i * wTop / 4, hTop - 1)
            gcTop.stroke()

            gcTop.beginPath()
            gcTop.moveTo(1, i * hTop / 4)
            gcTop.lineTo(wTop - 1, i * hTop / 4)
            gcTop.stroke()
        }

        // Density Top Grid Calculation
        const densityTop = Array.from({ length: GRID_RES }, () => Array(GRID_RES).fill(0))
        const densitySide = Array.from({ length: GRID_RES }, () => Array(GRID_RES).fill(0))

        const worldW = 100
        const worldH = 100
        const worldD = 4 // Depth in meters

        if (ants && Array.isArray(ants)) {
            ants.forEach(ant => {
                const ax = ant.x ?? 50
                const ay = ant.z !== undefined ? ant.z : (ant.y ?? 50)
                const az = Math.abs(ant.y ?? 0)

                const gx = Math.min(GRID_RES - 1, Math.max(0, Math.floor((ax / worldW) * GRID_RES)))
                const gy = Math.min(GRID_RES - 1, Math.max(0, Math.floor((ay / worldH) * GRID_RES)))
                const gz = Math.min(GRID_RES - 1, Math.max(0, Math.floor((az / worldD) * GRID_RES)))

                densityTop[gx][gy]++
                densitySide[gx][gz]++
            })
        }

        // Top Heatmap
        let maxDensityTop = 1
        for (let x = 0; x < GRID_RES; x++) {
            for (let y = 0; y < GRID_RES; y++) {
                if (densityTop[x][y] > maxDensityTop) maxDensityTop = densityTop[x][y]
            }
        }

        const cellW = wTop / GRID_RES
        const cellH = hTop / GRID_RES

        for (let x = 0; x < GRID_RES; x++) {
            for (let y = 0; y < GRID_RES; y++) {
                const count = densityTop[x][y]
                if (count > 0) {
                    const intensity = Math.min(1.0, count / maxDensityTop)
                    const r = Math.round(50 + 205 * intensity)
                    const g = Math.round(180 + 75 * intensity)
                    const b = 50
                    gcTop.fillStyle = `rgba(${r}, ${g}, ${b}, ${0.4 + 0.5 * intensity})`
                    gcTop.fillRect(x * cellW, y * cellH, cellW + 0.5, cellH + 0.5)
                }
            }
        }

        // Colony Nests Top View (Radial Glow + Circle)
        const nestList = nests && nests.length > 0 ? nests : (colonies || [])
        nestList.forEach((n, idx) => {
            const nx = ((n.x ?? (idx === 0 ? 35 : 65)) / worldW) * wTop
            const ny = (((n.z !== undefined ? n.z : n.y) ?? (idx === 0 ? 35 : 65)) / worldH) * hTop

            // Radial Glow
            const radGrad = gcTop.createRadialGradient(nx, ny, 1, nx, ny, 12)
            radGrad.addColorStop(0, 'rgba(251, 191, 36, 0.85)')
            radGrad.addColorStop(1, 'rgba(251, 191, 36, 0)')
            gcTop.fillStyle = radGrad
            gcTop.beginPath()
            gcTop.arc(nx, ny, 12, 0, Math.PI * 2)
            gcTop.fill()

            // Solid Ring
            gcTop.fillStyle = n.color || '#f59e0b'
            gcTop.beginPath()
            gcTop.arc(nx, ny, 3.5, 0, Math.PI * 2)
            gcTop.fill()
            gcTop.strokeStyle = '#ffffff'
            gcTop.lineWidth = 1
            gcTop.stroke()
        })

        // Camera Viewport Indicator Box (Centered on default 50, 50)
        const vpX = (50 / worldW) * wTop
        const vpY = (50 / worldH) * hTop
        const vpW = (35 / worldW) * wTop
        const vpH = (35 / worldH) * hTop

        gcTop.strokeStyle = 'rgba(56, 189, 248, 0.9)'
        gcTop.lineWidth = 1.5
        gcTop.strokeRect(vpX - vpW / 2, vpY - vpH / 2, vpW, vpH)

        // Megaterrarium Boundary Indicators
        gcTop.font = '8px sans-serif'
        gcTop.fillStyle = 'rgba(56, 189, 248, 0.75)'
        gcTop.fillText('▲ [0,1]', wTop / 2 - 12, 8)
        gcTop.fillText('▼ [0,0]', wTop / 2 - 12, hTop - (showLegend ? 18 : 3))
        gcTop.fillText('◀[0,0]', 2, hTop / 2 + 3)
        gcTop.fillText('[0,1]▶', wTop - 26, hTop / 2 + 3)

        // Top Legend Overlay
        if (showLegend) {
            const lgH = 15
            const lgY = hTop - lgH - 2
            gcTop.fillStyle = 'rgba(15, 23, 42, 0.88)'
            gcTop.fillRect(3, lgY, wTop - 6, lgH)
            gcTop.strokeStyle = 'rgba(51, 65, 85, 0.6)'
            gcTop.lineWidth = 1
            gcTop.strokeRect(3, lgY, wTop - 6, lgH)

            gcTop.font = '8.5px sans-serif'
            // Density swatch
            gcTop.fillStyle = '#f59e0b'
            gcTop.fillRect(6, lgY + 4, 6, 6)
            gcTop.fillStyle = '#cbd5e1'
            gcTop.fillText('Densité', 15, lgY + 9)

            // Nest swatch
            gcTop.fillStyle = '#f59e0b'
            gcTop.beginPath()
            gcTop.arc(wTop * 0.44, lgY + 7, 3, 0, Math.PI * 2)
            gcTop.fill()
            gcTop.fillStyle = '#cbd5e1'
            gcTop.fillText('Nids', wTop * 0.44 + 6, lgY + 9)

            // Camera swatch
            gcTop.strokeStyle = '#38bdf8'
            gcTop.lineWidth = 1
            gcTop.strokeRect(wTop * 0.72, lgY + 4, 6, 6)
            gcTop.fillStyle = '#cbd5e1'
            gcTop.fillText('Caméra', wTop * 0.72 + 9, lgY + 9)
        }

        // Clean Outer Border
        gcTop.strokeStyle = '#334155'
        gcTop.lineWidth = 1
        gcTop.strokeRect(0.5, 0.5, wTop - 1, hTop - 1)

        // 2. SIDE PROFILE VIEW RENDERING (Coupe Géologique)
        const wSide = canvasSide.width
        const hSide = canvasSide.height

        // Background Dark Slate
        gcSide.fillStyle = '#0f172a'
        gcSide.fillRect(0, 0, wSide, hSide)

        // Stratigraphy bands (Humus, Argile, Bedrock)
        gcSide.fillStyle = '#3d2817' // Humus surface
        gcSide.fillRect(0, 0, wSide, hSide * 0.25)
        gcSide.fillStyle = '#9a3412' // Argile subsoil
        gcSide.fillRect(0, hSide * 0.25, wSide, hSide * 0.45)
        gcSide.fillStyle = '#64748b' // Pierre / Bedrock
        gcSide.fillRect(0, hSide * 0.70, wSide, hSide * 0.30)

        // Water Table Line
        gcSide.strokeStyle = '#0284c7'
        gcSide.lineWidth = 1.2
        gcSide.beginPath()
        gcSide.moveTo(0, hSide * 0.75)
        gcSide.lineTo(wSide, hSide * 0.75)
        gcSide.stroke()

        // Side Ant Density Heatmap
        for (let x = 0; x < GRID_RES; x++) {
            for (let z = 0; z < GRID_RES; z++) {
                const count = densitySide[x][z]
                if (count > 0) {
                    gcSide.fillStyle = 'rgba(250, 204, 21, 0.75)'
                    gcSide.fillRect(x * (wSide / GRID_RES), z * (hSide / GRID_RES), (wSide / GRID_RES) + 0.5, (hSide / GRID_RES) + 0.5)
                }
            }
        }

        // Nests Depth Markers
        nestList.forEach((n, idx) => {
            const nx = ((n.x ?? (idx === 0 ? 35 : 65)) / worldW) * wSide
            const nz = (1.2 / worldD) * hSide // Queen chamber depth ~ 1.2m

            gcSide.fillStyle = '#d97706'
            gcSide.beginPath()
            gcSide.arc(nx, nz, 4, 0, Math.PI * 2)
            gcSide.fill()
            gcSide.strokeStyle = '#ffffff'
            gcSide.lineWidth = 1
            gcSide.stroke()
        })

        // Camera Depth Line
        gcSide.strokeStyle = 'rgba(56, 189, 248, 0.9)'
        gcSide.lineWidth = 1.5
        gcSide.beginPath()
        gcSide.moveTo(vpX, 0)
        gcSide.lineTo(vpX, hSide)
        gcSide.stroke()

        // Side Legend Overlay
        if (showLegend) {
            const lgH = 15
            const lgY = hSide - lgH - 2
            gcSide.fillStyle = 'rgba(15, 23, 42, 0.88)'
            gcSide.fillRect(3, lgY, wSide - 6, lgH)
            gcSide.strokeStyle = 'rgba(51, 65, 85, 0.6)'
            gcSide.lineWidth = 1
            gcSide.strokeRect(3, lgY, wSide - 6, lgH)

            gcSide.font = '8.5px sans-serif'
            // Humus / Argile swatches
            gcSide.fillStyle = '#3d2817'
            gcSide.fillRect(6, lgY + 4, 4, 6)
            gcSide.fillStyle = '#9a3412'
            gcSide.fillRect(11, lgY + 4, 4, 6)
            gcSide.fillStyle = '#64748b'
            gcSide.fillRect(16, lgY + 4, 4, 6)
            gcSide.fillStyle = '#cbd5e1'
            gcSide.fillText('Humus / Argile', 24, lgY + 9)

            // Nappe phréatique line
            gcSide.strokeStyle = '#0284c7'
            gcSide.lineWidth = 1.5
            gcSide.beginPath()
            gcSide.moveTo(wSide * 0.65, lgY + 7)
            gcSide.lineTo(wSide * 0.65 + 10, lgY + 7)
            gcSide.stroke()
            gcSide.fillStyle = '#cbd5e1'
            gcSide.fillText('Nappe', wSide * 0.65 + 13, lgY + 9)
        }

        // Clean Outer Border
        gcSide.strokeStyle = '#334155'
        gcSide.lineWidth = 1
        gcSide.strokeRect(0.5, 0.5, wSide - 1, hSide - 1)

    }, [ants, colonies, nests, showMinimap, collapsed, showLegend, syncViews])

    if (!showMinimap) return null

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            right: 20,
            zIndex: 92,
            background: isDark ? 'rgba(15, 23, 42, 0.95)' : 'rgba(255, 255, 255, 0.95)',
            border: isDark ? '1.5px solid #0284c7' : '1.5px solid #0284c7',
            borderRadius: 8,
            padding: 6,
            boxShadow: '0 8px 28px rgba(0,0,0,0.5)',
            backdropFilter: 'blur(12px)',
            color: isDark ? '#fff' : '#0f172a',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            display: 'flex',
            flexDirection: 'column',
            gap: 4
        }}>
            {/* Header (1:1 with MinimapOverlay.java) */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: 3,
                fontSize: 10.5,
                fontWeight: 700,
                color: '#38bdf8'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Map size={13} />
                    <span>{t('minimapTitle', 'Minimap 2D')}</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 9 }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 3, color: '#00d4ff', cursor: 'pointer' }}>
                        <input
                            type="checkbox"
                            checked={syncViews}
                            onChange={(e) => setSyncViews(e.target.checked)}
                            style={{ margin: 0, accentColor: '#00d4ff' }}
                        />
                        <span>Sync</span>
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', gap: 3, color: '#38bdf8', cursor: 'pointer' }}>
                        <input
                            type="checkbox"
                            checked={showLegend}
                            onChange={(e) => setShowLegend(e.target.checked)}
                            style={{ margin: 0, accentColor: '#38bdf8' }}
                        />
                        <span>Légende</span>
                    </label>

                    <button
                        onClick={triggerCameraReset}
                        title="Recentrer la caméra 3D"
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            color: isDark ? '#94a3b8' : '#64748b',
                            padding: 1,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        <Crosshair size={12} />
                    </button>

                    <button
                        onClick={() => setCollapsed(!collapsed)}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            color: isDark ? '#94a3b8' : '#64748b',
                            padding: 1,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        {collapsed ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                    </button>
                </div>
            </div>

            {/* Dual Canvas Layout (Top-Down + Side Cross-Section) */}
            {!collapsed && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    {/* Top Down Label & Canvas */}
                    <div style={{ fontSize: 9.5, fontWeight: 700, color: '#cbd5e1' }}>
                        Vue du dessus (Top-Down)
                    </div>
                    <canvas
                        ref={canvasTopRef}
                        width={mapWidth}
                        height={topHeight}
                        style={{
                            borderRadius: 4,
                            cursor: 'crosshair',
                            display: 'block'
                        }}
                    />

                    {/* Side View Label & Canvas */}
                    <div style={{ fontSize: 9.5, fontWeight: 700, color: '#cbd5e1', marginTop: 2 }}>
                        Vue de profil / Coupe (Side Profile)
                    </div>
                    <canvas
                        ref={canvasSideRef}
                        width={mapWidth}
                        height={sideHeight}
                        style={{
                            borderRadius: 4,
                            cursor: 'crosshair',
                            display: 'block'
                        }}
                    />
                </div>
            )}
        </div>
    )
}
