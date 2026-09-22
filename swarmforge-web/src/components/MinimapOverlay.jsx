import React, { useRef, useEffect, useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { Map, ChevronDown, ChevronUp } from 'lucide-react'
import { getTerrainHeight, getSubstrateAt } from '../utils/terrainUtils'

/**
 * MinimapOverlay.jsx - 2D Dual Minimap
 * Strictly conforms with heavy client (MinimapOverlay.java & WorldEditorPane.java):
 * - Top-Down View (Vue Zénithale): Renders ONLY the physical terrain (elevation, substrate shading, river channel),
 *   colony nest locations, and camera viewport rectangle. No ant density overlay.
 * - Side Profile View (Profil Géologique & Profondeur): Stratigraphy layers (Humus, Argile, Bedrock), water table line,
 *   subterranean nest chambers, and camera depth indicator.
 * - Zoom & Pan controlled directly via mouse wheel and direct mouse manipulation (zoom buttons removed).
 */
export default function MinimapOverlay() {
    const {
        colonies,
        nests,
        terrainConfig,
        showMinimap,
        language,
        theme
    } = useSimulationStore()

    const canvasTopRef = useRef(null)
    const canvasSideRef = useRef(null)
    const [collapsed, setCollapsed] = useState(false)
    const [zoom, setZoom] = useState(1.0)
    const [panOffset, setPanOffset] = useState({ x: 0, y: 0 })
    const isDraggingRef = useRef(false)
    const lastMouseRef = useRef({ x: 0, y: 0 })

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

        const worldW = 100
        const worldH = 100
        const worldD = 4 // Subterranean depth in meters

        // 1. TOP-DOWN TERRAIN VIEW RENDERING (Vue Zénithale - Terrain Exclusif)
        const wTop = canvasTop.width
        const hTop = canvasTop.height

        gcTop.save()
        gcTop.fillStyle = '#0f172a'
        gcTop.fillRect(0, 0, wTop, hTop)

        // Apply Zoom & Pan centered
        if (zoom !== 1.0 || panOffset.x !== 0 || panOffset.y !== 0) {
            gcTop.translate(wTop / 2 + panOffset.x, hTop / 2 + panOffset.y)
            gcTop.scale(zoom, zoom)
            gcTop.translate(-wTop / 2, -hTop / 2)
        }

        // Render pure physical terrain grid (Relief, Substrates, River Bed)
        const cellW = wTop / GRID_RES
        const cellH = hTop / GRID_RES

        for (let gx = 0; gx < GRID_RES; gx++) {
            for (let gy = 0; gy < GRID_RES; gy++) {
                const wx = (gx / GRID_RES) * worldW
                const wz = (gy / GRID_RES) * worldH
                const heightY = getTerrainHeight(wx, wz, terrainConfig)
                const substrate = getSubstrateAt(wx, heightY, wz, terrainConfig)

                let baseColor = '#2e4a1f' // Default surface humus/vegetation green
                if (substrate.type === 'WATER') {
                    baseColor = '#0284c7'
                } else if (substrate.type === 'SAND') {
                    baseColor = '#ca8a04'
                } else if (substrate.type === 'BEDROCK') {
                    baseColor = '#475569'
                } else {
                    // Organic elevation-shaded terrain
                    const altShade = Math.min(1.0, Math.max(0.0, (heightY + 2.0) / 5.0))
                    const r = Math.round(35 + altShade * 25)
                    const g = Math.round(70 + altShade * 45)
                    const b = Math.round(25 + altShade * 20)
                    baseColor = `rgb(${r}, ${g}, ${b})`
                }

                gcTop.fillStyle = baseColor
                gcTop.fillRect(gx * cellW, gy * cellH, cellW + 0.5, cellH + 0.5)
            }
        }

        // Subdued Topographic Contour Lines & Grid
        gcTop.strokeStyle = 'rgba(255, 255, 255, 0.08)'
        gcTop.lineWidth = 0.5
        for (let i = 1; i < 4; i++) {
            gcTop.beginPath()
            gcTop.moveTo(i * wTop / 4, 0)
            gcTop.lineTo(i * wTop / 4, hTop)
            gcTop.stroke()

            gcTop.beginPath()
            gcTop.moveTo(0, i * hTop / 4)
            gcTop.lineTo(wTop, i * hTop / 4)
            gcTop.stroke()
        }

        // River Channel Overlay (if river enabled in terrainConfig)
        const hasRiver = terrainConfig?.hasRiver ?? true
        const riverX = terrainConfig?.riverX ?? 25
        const riverWidth = terrainConfig?.riverWidth ?? 12
        if (hasRiver) {
            const rxPix = (riverX / worldW) * wTop
            const rwPix = (riverWidth / worldW) * wTop
            gcTop.fillStyle = 'rgba(2, 132, 199, 0.75)'
            gcTop.fillRect(rxPix - rwPix / 2, 0, rwPix, hTop)
            gcTop.strokeStyle = 'rgba(56, 189, 248, 0.6)'
            gcTop.lineWidth = 1
            gcTop.beginPath()
            gcTop.moveTo(rxPix, 0)
            gcTop.lineTo(rxPix, hTop)
            gcTop.stroke()
        }

        // Colony Nests Top View (Orange Ring + Radial Glow)
        const nestList = nests && nests.length > 0 ? nests : (colonies || [])
        nestList.forEach((n, idx) => {
            const nx = ((n.x ?? (idx === 0 ? 35 : 65)) / worldW) * wTop
            const ny = (((n.z !== undefined ? n.z : n.y) ?? (idx === 0 ? 35 : 65)) / worldH) * hTop

            const radGrad = gcTop.createRadialGradient(nx, ny, 1, nx, ny, 10)
            radGrad.addColorStop(0, 'rgba(251, 191, 36, 0.9)')
            radGrad.addColorStop(1, 'rgba(251, 191, 36, 0)')
            gcTop.fillStyle = radGrad
            gcTop.beginPath()
            gcTop.arc(nx, ny, 10, 0, Math.PI * 2)
            gcTop.fill()

            gcTop.fillStyle = n.color || '#f59e0b'
            gcTop.beginPath()
            gcTop.arc(nx, ny, 3.5, 0, Math.PI * 2)
            gcTop.fill()
            gcTop.strokeStyle = '#ffffff'
            gcTop.lineWidth = 1
            gcTop.stroke()
        })

        // Camera Viewport Indicator Box
        const vpX = (50 / worldW) * wTop
        const vpY = (50 / worldH) * hTop
        const vpW = (35 / worldW) * wTop
        const vpH = (35 / worldH) * hTop

        gcTop.strokeStyle = 'rgba(56, 189, 248, 0.9)'
        gcTop.lineWidth = 1.5
        gcTop.strokeRect(vpX - vpW / 2, vpY - vpH / 2, vpW, vpH)

        // Cluster / Megaterrarium Boundary Indicators
        gcTop.font = '8px sans-serif'
        gcTop.fillStyle = 'rgba(56, 189, 248, 0.85)'
        gcTop.fillText('▲ [0,1]', wTop / 2 - 12, 8)
        gcTop.fillText('▼ [0,0]', wTop / 2 - 12, hTop - 3)
        gcTop.fillText('◀[0,0]', 2, hTop / 2 + 3)
        gcTop.fillText('[0,1]▶', wTop - 26, hTop / 2 + 3)

        gcTop.restore()

        // 2. SIDE PROFILE VIEW RENDERING (Profil Géologique & Profondeur)
        const wSide = canvasSide.width
        const hSide = canvasSide.height

        gcSide.fillStyle = '#0f172a'
        gcSide.fillRect(0, 0, wSide, hSide)

        // Stratigraphy Geological Bands
        // Topsoil Humus (0 - 0.8m)
        gcSide.fillStyle = 'rgba(82, 50, 25, 0.9)'
        gcSide.fillRect(0, 0, wSide, hSide * 0.25)

        // Clay Subsoil (0.8m - 2.5m)
        gcSide.fillStyle = 'rgba(154, 52, 18, 0.85)'
        gcSide.fillRect(0, hSide * 0.25, wSide, hSide * 0.45)

        // Bedrock Base (2.5m - 4m)
        gcSide.fillStyle = 'rgba(51, 65, 85, 0.95)'
        gcSide.fillRect(0, hSide * 0.70, wSide, hSide * 0.30)

        // Water Table Line at Depth = -3.2m
        gcSide.strokeStyle = 'rgba(2, 132, 199, 0.9)'
        gcSide.lineWidth = 1.5
        gcSide.setLineDash([3, 2])
        gcSide.beginPath()
        gcSide.moveTo(0, hSide * 0.8)
        gcSide.lineTo(wSide, hSide * 0.8)
        gcSide.stroke()
        gcSide.setLineDash([])

        // Subterranean Nest Chambers on Geological Profile
        nestList.forEach((n, idx) => {
            const nx = ((n.x ?? (idx === 0 ? 35 : 65)) / worldW) * wSide
            const nyCh1 = hSide * 0.35 // Brood chamber
            const nyCh2 = hSide * 0.55 // Queen chamber

            gcSide.fillStyle = 'rgba(168, 85, 247, 0.9)'
            gcSide.beginPath()
            gcSide.arc(nx, nyCh1, 3, 0, Math.PI * 2)
            gcSide.fill()

            gcSide.fillStyle = 'rgba(245, 158, 11, 0.9)'
            gcSide.beginPath()
            gcSide.arc(nx + 3, nyCh2, 3.5, 0, Math.PI * 2)
            gcSide.fill()
        })

        // Camera Depth Position Line
        gcSide.strokeStyle = 'rgba(56, 189, 248, 0.9)'
        gcSide.lineWidth = 1.5
        gcSide.beginPath()
        gcSide.moveTo((50 / worldW) * wSide, 0)
        gcSide.lineTo((50 / worldW) * wSide, hSide)
        gcSide.stroke()

        // Depth Axis Ticks
        gcSide.font = '7px sans-serif'
        gcSide.fillStyle = 'rgba(203, 213, 225, 0.7)'
        gcSide.fillText('0m', 2, 8)
        gcSide.fillText('-2m', 2, hSide * 0.5)
        gcSide.fillText('-4m', 2, hSide - 2)

    }, [showMinimap, collapsed, colonies, nests, terrainConfig, zoom, panOffset])

    if (!showMinimap) return null

    const handleWheel = (e) => {
        e.stopPropagation()
        setZoom(z => Math.max(1.0, Math.min(4.0, z + (e.deltaY < 0 ? 0.25 : -0.25))))
    }

    const handleDoubleClick = () => {
        setZoom(1.0)
        setPanOffset({ x: 0, y: 0 })
    }

    const handleMouseDown = (e) => {
        isDraggingRef.current = true
        lastMouseRef.current = { x: e.clientX, y: e.clientY }
    }

    const handleMouseMove = (e) => {
        if (!isDraggingRef.current) return
        const dx = e.clientX - lastMouseRef.current.x
        const dy = e.clientY - lastMouseRef.current.y
        lastMouseRef.current = { x: e.clientX, y: e.clientY }
        setPanOffset(prev => ({ x: prev.x + dx, y: prev.y + dy }))
    }

    const handleMouseUp = () => {
        isDraggingRef.current = false
    }

    return (
        <div
            style={{
                position: 'absolute',
                top: 12,
                right: 355,
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
            onMouseUp={handleMouseUp}
        >
            {/* Header Bar (Zoom in/out buttons removed per specification, direct mouse control) */}
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
                    <span>{t('minimapTitle', 'RADAR TOPOGRAPHIQUE 2D')}</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                    <span style={{ fontSize: 8.5, fontWeight: 700, color: '#38bdf8' }}>
                        {zoom.toFixed(1)}x
                    </span>

                    {/* Collapse / Expand Button */}
                    <button
                        onClick={() => setCollapsed(!collapsed)}
                        title={collapsed ? t('expand', 'Agrandir') : t('collapse', 'Réduire')}
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

            {/* Map Canvases (Top-Down Terrain + Side Profile Stratigraphy) */}
            {!collapsed && (
                <div
                    onWheel={handleWheel}
                    onDoubleClick={handleDoubleClick}
                    onMouseDown={handleMouseDown}
                    onMouseMove={handleMouseMove}
                    title={t('minimapZoomWheelTt', 'Molette souris pour zoomer / Glisser pour déplacer / Double-clic pour réinitialiser')}
                    style={{ padding: 6, display: 'flex', flexDirection: 'column', gap: 6, cursor: isDraggingRef.current ? 'grabbing' : 'grab' }}
                >
                    {/* 1. Top-Down Terrain Map Canvas */}
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
                            color: 'rgba(255,255,255,0.85)',
                            textShadow: '0 1px 2px #000'
                        }}>
                            {t('minimapTopdown', 'Vue Zénithale (Terrain)')}
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
                            color: 'rgba(255,255,255,0.85)',
                            textShadow: '0 1px 2px #000'
                        }}>
                            {t('minimapSideview', 'Profil Géologique & Profondeur')}
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
