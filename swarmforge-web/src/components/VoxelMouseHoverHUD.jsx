import React from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { MapPin, Layers, Thermometer, Droplets, Sparkles } from 'lucide-react'

/**
 * Floating dynamic HUD overlay that follows the mouse cursor when hovering over terrain voxels.
 * Displays real-time voxel coordinates (X, Y, Z), geological substrate type, local microclimate, and pheromones.
 */
export default function VoxelMouseHoverHUD() {
    const { hoveredVoxel, theme } = useSimulationStore()
    const isDark = theme === 'dark'

    if (!hoveredVoxel || !hoveredVoxel.isHovering) return null

    const { screenX, screenY, x, y, z, substrate, temp, humidity, phero } = hoveredVoxel

    // Position tooltip offset from mouse pointer to prevent obscuring the cursor
    const posX = Math.min(window.innerWidth - 230, (screenX ?? 100) + 16)
    const posY = Math.min(window.innerHeight - 150, (screenY ?? 100) + 16)

    const substrateColor = substrate?.color || '#854d0e'
    const substrateName = substrate?.name || 'Humus Organique'

    return (
        <div style={{
            position: 'fixed',
            left: posX,
            top: posY,
            zIndex: 120,
            pointerEvents: 'none', // Allow mouse events to pass through to 3D canvas
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
            border: `1.5px solid ${isDark ? 'rgba(56, 189, 248, 0.5)' : 'rgba(2, 132, 199, 0.6)'}`,
            borderRadius: 8,
            padding: '7px 10px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.45)',
            backdropFilter: 'blur(10px)',
            color: isDark ? '#f1f5f9' : '#0f172a',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            fontSize: 10.5,
            minWidth: 190,
            display: 'flex',
            flexDirection: 'column',
            gap: 4
        }}>
            {/* Voxel Coordinates Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)', paddingBottom: 4 }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontWeight: 800, color: '#38bdf8' }}>
                    <MapPin size={12} />
                    Voxel ({Math.round(x ?? 50)}, {Math.round(z ?? 50)})
                </span>
                <span style={{ fontSize: 9.5, color: isDark ? '#94a3b8' : '#64748b', fontFamily: 'monospace' }}>
                    Alt: {(y ?? 0).toFixed(1)}m
                </span>
            </div>

            {/* Substrate Stratum */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
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

            {/* Environmental Conditions: Temp, Moisture, Pheromones */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4, marginTop: 2, fontSize: 9.5 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: isDark ? '#cbd5e1' : '#334155' }}>
                    <Thermometer size={11} color="#f97316" />
                    <span>{(temp ?? 22.0).toFixed(1)}°C</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: isDark ? '#cbd5e1' : '#334155' }}>
                    <Droplets size={11} color="#38bdf8" />
                    <span>{Math.round(humidity ?? 65)}% Hum.</span>
                </div>
            </div>

            {phero > 0.05 && (
                <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#a855f7', fontSize: 9, fontWeight: 700, marginTop: 1 }}>
                    <Sparkles size={11} />
                    <span>Phéromone : {(phero ?? 0.1).toFixed(2)} ppm</span>
                </div>
            )}
        </div>
    )
}
