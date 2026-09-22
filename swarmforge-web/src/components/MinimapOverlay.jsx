import React, { useRef, useEffect, useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { Map, ZoomIn, ZoomOut, ChevronDown, ChevronUp } from 'lucide-react'

/**
 * Minimap overlay matching the World Editor & SwarmForgeClient dual 2D maps system:
 * - Top-Down View: Ant density heatmap, nests, camera viewport rect, directional borders with interactive zoom.
 * - Side Profile View: Stratigraphy bands (Humus, Argile, Bedrock), water table, subterranean ant depth & nests.
 * - Positioned to the left of the right sidebar without overlapping.
 */
export default function MinimapOverlay() {
    const {
        ants,
        colonies,
        nests,
        showMinimap,
        language,
        theme
    } = useSimulationStore()

    const canvasTopRef = useRef(null)
    const canvasSideRef = useRef(null)
    const [collapsed, setCollapsed] = useState(false)
    const [zoom, setZoom] = useState(1.0)

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const mapWidth = 210
    const topHeight = 145
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
        gcTop.save()
        gcTop.fillStyle = '#0f172a'
        gcTop.fillRect(0, 0, wTop, hTop)

        // Apply Zoom Transform centered on (wTop/2, hTop/2)
        if (zoom > 1.0) {
            gcTop.translate(wTop / 2, hTop / 2)
            gcTop.scale(zoom, zoom)
            gcTop.translate(-wTop / 2, -hTop / 2)
        }

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
        gcTop.fillText('▼ [0,0]', wTop / 2 - 12, hTop - 3)
        gcTop.fillText('◀[0,0]', 2, hTop / 2 + 3)
        gcTop.fillText('[0,1]▶', wTop - 26, hTop / 2 + 3)

        gcTop.restore()

        // 2. SIDE PROFILE VIEW RENDERING (Stratigraphy & Depth)
        const wSide = canvasSide.width
        const hSide = canvasSide.height

        gcSide.fillStyle = '#0f172a'
        gcSide.fillRect(0, 0, wSide, hSide)

        // Stratigraphy Color Bands
        // Topsoil (0 - 0.8m)
        gcSide.fillStyle = 'rgba(82, 50, 25, 0.85)'
        gcSide.fillRect(0, 0, wSide, hSide * 0.25)

        // Subsoil Clay (0.8m - 2.5m)
        gcSide.fillStyle = 'rgba(154, 52, 18, 0.75)'
        gcSide.fillRect(0, hSide * 0.25, wSide, hSide * 0.45)

        // Deep Bedrock (2.5m - 4m)
        gcSide.fillStyle = 'rgba(51, 65, 85, 0.9)'
        gcSide.fillRect(0, hSide * 0.70, wSide, hSide * 0.30)

        // Water Table Blue Shimmer Line at Y = -3.2m
        gcSide.strokeStyle = 'rgba(2, 132, 199, 0.8)'
        gcSide.lineWidth = 1.5
        gcSide.setLineDash([3, 2])
        gcSide.beginPath()
        gcSide.moveTo(0, hSide * 0.8)
        gcSide.lineTo(wSide, hSide * 0.8)
        gcSide.stroke()
        gcSide.setLineDash([])

        // Subterranean Chambers on Profile
        nestList.forEach((n, idx) => {
            const nx = ((n.x ?? (idx === 0 ? 35 : 65)) / worldW) * wSide
            const nyCh1 = hSide * 0.35 // Brood
            const nyCh2 = hSide * 0.55 // Queen

            gcSide.fillStyle = 'rgba(168, 85, 247, 0.9)'
            gcSide.beginPath()
            gcSide.arc(nx, nyCh1, 3, 0, Math.PI * 2)
            gcSide.fill()

            gcSide.fillStyle = 'rgba(245, 158, 11, 0.9)'
            gcSide.beginPath()
            gcSide.arc(nx + 3, nyCh2, 3.5, 0, Math.PI * 2)
            gcSide.fill()
        })

        // Subterranean Ants Depth Points
        if (ants && Array.isArray(ants)) {
            ants.forEach(ant => {
                const ax = ((ant.x ?? 50) / worldW) * wSide
                const depthNorm = Math.min(1.0, Math.max(0, Math.abs(ant.y ?? 0) / worldD))
                const ay = depthNorm * hSide

                gcSide.fillStyle = ant.color || '#38bdf8'
                gcSide.fillRect(ax - 0.5, ay - 0.5, 1.2, 1.2)
            })
        }

        // Depth Axis Ticks
        gcSide.font = '7px sans-serif'
        gcSide.fillStyle = 'rgba(203, 213, 225, 0.6)'
        gcSide.fillText('0m', 2, 8)
        gcSide.fillText('-2m', 2, hSide * 0.5)
        gcSide.fillText('-4m', 2, hSide - 2)

    }, [showMinimap, collapsed, ants, colonies, nests, zoom])

    if (!showMinimap) return null

    const handleWheel = (e) => {
        e.stopPropagation()
        setZoom(z => Math.max(1.0, Math.min(4.0, z + (e.deltaY < 0 ? 0.25 : -0.25))))
    }

    const handleDoubleClick = () => {
        setZoom(1.0)
    }

    return (
        <div
            style={{
                position: 'absolute',
                top: 12,
                right: 355, // Positioned neatly to the left of the right sidebar (330px width + 12px margin)
                zIndex: 85,
                background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.95)',
                backdropFilter: 'blur(16px)',
                border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
                borderRadius: 10,
                color: isDark ? '#ffffff' : '#0f172a',
                boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
                fontFamily: 'system-ui, -apple-system, sans-serif',
                overflow: 'hidden',
                userSelect: 'none',
                pointerEvents: 'auto'
            }}
        >
            {/* Header Bar */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '5px 8px',
                borderBottom: isDark ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
                background: isDark ? 'rgba(30, 41, 59, 0.5)' : 'rgba(241, 245, 249, 0.8)'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 10, fontWeight: 800, color: '#38bdf8' }}>
                    <Map size={12} />
                    <span>RADAR TOPOGRAPHIQUE 2D</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                    {/* Zoom In Button */}
                    <button
                        onClick={() => setZoom(z => Math.min(4.0, z + 0.25))}
                        title="Zoom avant (+)"
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: isDark ? '#cbd5e1' : '#334155',
                            cursor: 'pointer',
                            padding: 2,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        <ZoomIn size={12} />
                    </button>

                    {/* Zoom Out Button */}
                    <button
                        onClick={() => setZoom(z => Math.max(1.0, z - 0.25))}
                        title="Zoom arrière (-)"
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: isDark ? '#cbd5e1' : '#334155',
                            cursor: 'pointer',
                            padding: 2,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        <ZoomOut size={12} />
                    </button>

                    <span style={{ fontSize: 8, fontWeight: 700, color: '#38bdf8', minWidth: 20, textAlign: 'center' }}>
                        {zoom.toFixed(1)}x
                    </span>

                    {/* Collapse Button */}
                    <button
                        onClick={() => setCollapsed(!collapsed)}
                        title={collapsed ? 'Agrandir' : 'Réduire'}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: isDark ? '#94a3b8' : '#64748b',
                            cursor: 'pointer',
                            padding: 2,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        {collapsed ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
                    </button>
                </div>
            </div>

            {/* Map Canvases (Top-Down + Side Profile) */}
            {!collapsed && (
                <div
                    onWheel={handleWheel}
                    onDoubleClick={handleDoubleClick}
                    title="Molette souris pour zoomer / Double-clic pour réinitialiser"
                    style={{ padding: 6, display: 'flex', flexDirection: 'column', gap: 6 }}
                >
                    {/* 1. Top-Down Map Canvas */}
                    <div style={{ position: 'relative', borderRadius: 6, overflow: 'hidden', border: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)' }}>
                        <canvas
                            ref={canvasTopRef}
                            width={mapWidth}
                            height={topHeight}
                            style={{ display: 'block', width: mapWidth, height: topHeight }}
                        />
                        <div style={{
                            position: 'absolute',
                            top: 3,
                            left: 5,
                            fontSize: 8,
                            fontWeight: 800,
                            color: 'rgba(255,255,255,0.7)',
                            textShadow: '0 1px 2px #000'
                        }}>
                            Vue Zénithale (Densité)
                        </div>
                    </div>

                    {/* 2. Side Profile Stratigraphy Map Canvas */}
                    <div style={{ position: 'relative', borderRadius: 6, overflow: 'hidden', border: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)' }}>
                        <canvas
                            ref={canvasSideRef}
                            width={mapWidth}
                            height={sideHeight}
                            style={{ display: 'block', width: mapWidth, height: sideHeight }}
                        />
                        <div style={{
                            position: 'absolute',
                            top: 3,
                            left: 5,
                            fontSize: 8,
                            fontWeight: 800,
                            color: 'rgba(255,255,255,0.7)',
                            textShadow: '0 1px 2px #000'
                        }}>
                            Profil Géologique & Profondeur
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
