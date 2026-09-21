import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { Home, Users, Thermometer, Droplets, Package, Shield, MapPin, Info, Minus, ChevronDown, Crosshair, X } from 'lucide-react'

/**
 * Collapsible HUD Overlay Panel for subterranean chamber inspection.
 */
export default function ChamberInspectorHUD({ onFocusChamber }) {
    const {
        selectedChamber,
        setSelectedChamber,
        colonies,
        theme
    } = useSimulationStore()

    const [isCollapsed, setIsCollapsed] = useState(false)
    const isDark = theme === 'dark'

    if (!selectedChamber) return null

    const chamberTypeIcons = {
        QUEEN: '👑',
        BROOD: '🍼',
        FOOD: '🌾',
        FUNGUS: '🍄',
        WASTE: '🗑️',
        DORMITORY: '💤',
        TUNNEL: '🚇'
    }

    const icon = chamberTypeIcons[selectedChamber.type] || selectedChamber.icon || '🏛️'
    const occPct = Math.round(((selectedChamber.occupants || 0) / (selectedChamber.capacity || 50)) * 100)
    const posX = selectedChamber.x ?? (selectedChamber.position?.x ?? 50)
    const posY = selectedChamber.y ?? (selectedChamber.position?.y ?? -1.2)
    const posZ = selectedChamber.z ?? (selectedChamber.position?.z ?? (selectedChamber.position?.y ?? 50))

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            left: 20,
            width: isCollapsed ? 230 : 310,
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
            border: isDark ? '1.2px solid rgba(245, 158, 11, 0.4)' : '1.2px solid rgba(245, 158, 11, 0.6)',
            borderRadius: 10,
            padding: 10,
            color: isDark ? '#fff' : '#0f172a',
            zIndex: 95,
            backdropFilter: 'blur(14px)',
            boxShadow: '0 8px 30px rgba(0,0,0,0.4)',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            fontSize: 11,
            transition: 'all 0.2s ease-in-out'
        }}>
            {/* Header */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: isCollapsed ? 0 : 6,
                borderBottom: isCollapsed ? 'none' : (isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)'),
                marginBottom: isCollapsed ? 0 : 6
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 14 }}>{icon}</span>
                    <span style={{ fontWeight: 800, color: '#f59e0b', fontSize: 12 }}>
                        {selectedChamber.name || 'Chambre Souterraine'}
                    </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                    <button
                        onClick={() => setIsCollapsed(!isCollapsed)}
                        title={isCollapsed ? 'Déplier' : 'Réduire'}
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        {isCollapsed ? <ChevronDown size={14} /> : <Minus size={14} />}
                    </button>
                    <button
                        onClick={() => setSelectedChamber(null)}
                        title="Fermer l'inspecteur de chambre"
                        style={{ background: 'transparent', border: 'none', color: '#f87171', cursor: 'pointer', padding: 2 }}
                    >
                        <X size={14} />
                    </button>
                </div>
            </div>

            {/* Details Content */}
            {!isCollapsed && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    {/* Nest & Role Badges */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '4px 6px', borderRadius: 6 }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontWeight: 700, color: '#38bdf8' }}>
                            <Home size={11} /> {selectedChamber.nestName || 'Nid Principal'}
                        </span>
                        <span style={{ fontSize: 9.5, fontWeight: 700, padding: '1px 6px', borderRadius: 4, background: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b' }}>
                            {selectedChamber.type || 'CHAMBER'}
                        </span>
                    </div>

                    {/* Population / Occupants Bar */}
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, marginBottom: 2 }}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 3, color: '#38bdf8' }}>
                                <Users size={10} /> Occupants Présents
                            </span>
                            <span style={{ fontWeight: 700 }}>
                                {selectedChamber.occupants || 0} / {selectedChamber.capacity || 50} ({occPct}%)
                            </span>
                        </div>
                        <div style={{ width: '100%', height: 4, background: isDark ? '#1e293b' : '#e2e8f0', borderRadius: 2, overflow: 'hidden' }}>
                            <div style={{ width: `${Math.min(100, occPct)}%`, height: '100%', background: occPct > 90 ? '#ef4444' : '#f59e0b', transition: 'width 0.3s' }} />
                        </div>
                    </div>

                    {/* Microclimate: Temp, Humidity, Food */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 4, fontSize: 10 }}>
                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b' }}>Temp</div>
                            <div style={{ fontWeight: 700, color: '#f97316' }}>{selectedChamber.temperature ?? 22.5}°C</div>
                        </div>
                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b' }}>Humidité</div>
                            <div style={{ fontWeight: 700, color: '#38bdf8' }}>{selectedChamber.humidity ?? 68}%</div>
                        </div>
                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b' }}>Stock</div>
                            <div style={{ fontWeight: 700, color: '#22c55e' }}>{selectedChamber.foodStored ?? 120} mg</div>
                        </div>
                    </div>

                    {/* Depth & 3D Coordinates */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, color: isDark ? '#94a3b8' : '#64748b' }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                            <MapPin size={10} /> Pos: ({Math.round(posX)}, {Math.round(posZ)})
                        </span>
                        <span style={{ fontWeight: 700, color: '#a855f7' }}>
                            Prof: {Math.abs(posY).toFixed(1)}m
                        </span>
                    </div>

                    {/* Focus Camera Button */}
                    <button
                        onClick={() => onFocusChamber && onFocusChamber(posX, posY, posZ)}
                        style={{
                            marginTop: 2,
                            background: isDark ? 'rgba(245, 158, 11, 0.15)' : 'rgba(245, 158, 11, 0.1)',
                            color: '#f59e0b',
                            border: '1px solid #f59e0b',
                            borderRadius: 6,
                            padding: '5px 8px',
                            fontSize: 10,
                            fontWeight: 700,
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 5
                        }}
                    >
                        <Crosshair size={12} />
                        Centrer la Caméra sur cette Chambre
                    </button>
                </div>
            )}
        </div>
    )
}
