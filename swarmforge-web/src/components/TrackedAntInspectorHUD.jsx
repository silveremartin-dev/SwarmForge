import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { Bug, Heart, Zap, MapPin, Briefcase, Package, Clock, ChevronLeft, ChevronRight, Minus, ChevronDown, Crosshair, X } from 'lucide-react'

/**
 * Collapsible HUD Overlay Panel for tracked individual ant inspection.
 */
export default function TrackedAntInspectorHUD() {
    const {
        trackedAntData,
        selectedEntity,
        setSelectedEntity,
        setTrackedAntId,
        selectNextAnt,
        selectPreviousAnt,
        followAntCamera,
        setFollowAntCamera,
        colonies,
        theme
    } = useSimulationStore()

    const [isCollapsed, setIsCollapsed] = useState(false)
    const isDark = theme === 'dark'

    const ant = trackedAntData || selectedEntity
    if (!ant) return null

    const antColony = colonies?.find(c => c.id === ant.colonyId) || {
        name: ant.colonyName || 'Colonie Native',
        color: ant.color || '#38bdf8'
    }

    const shortId = ant.id ? ant.id.replace('ant_', '').slice(0, 10) : '001'
    const antX = ant.x ?? 50
    const antZ = ant.z !== undefined ? ant.z : (ant.y ?? 50)
    const antY = ant.y !== undefined && ant.z !== undefined ? ant.y : 0.12

    const formatAge = () => {
        let days = ant.ageInDays
        if (days === undefined) {
            if (typeof ant.age === 'number') {
                days = ant.age > 1000 ? ant.age / 86400.0 : ant.age * 0.1
            } else {
                days = 12
            }
        }
        return `${Math.max(1, Math.round(days))} j`
    }

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            left: 20,
            width: isCollapsed ? 230 : 310,
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
            border: isDark ? '1.2px solid rgba(56, 189, 248, 0.4)' : '1.2px solid rgba(56, 189, 248, 0.6)',
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
                    <Bug size={15} color="#38bdf8" />
                    <span style={{ fontWeight: 800, color: '#38bdf8', fontSize: 12 }}>
                        Fourmi #{shortId}
                    </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                    <button
                        onClick={selectPreviousAnt}
                        title="Fourmi précédente"
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        <ChevronLeft size={14} />
                    </button>
                    <button
                        onClick={selectNextAnt}
                        title="Fourmi suivante"
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        <ChevronRight size={14} />
                    </button>
                    <button
                        onClick={() => setIsCollapsed(!isCollapsed)}
                        title={isCollapsed ? 'Déplier' : 'Réduire'}
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        {isCollapsed ? <ChevronDown size={14} /> : <Minus size={14} />}
                    </button>
                    <button
                        onClick={() => {
                            setSelectedEntity(null)
                            setTrackedAntId(null)
                        }}
                        title="Fermer l'inspecteur"
                        style={{ background: 'transparent', border: 'none', color: '#f87171', cursor: 'pointer', padding: 2 }}
                    >
                        <X size={14} />
                    </button>
                </div>
            </div>

            {/* Details Content */}
            {!isCollapsed && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    {/* Colony & Species Banner */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '4px 6px', borderRadius: 6 }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontWeight: 700, color: antColony.color }}>
                            <span style={{ width: 7, height: 7, borderRadius: '50%', background: antColony.color, display: 'inline-block' }} />
                            {antColony.name}
                        </span>
                        <span style={{ fontSize: 9.5, color: isDark ? '#94a3b8' : '#64748b', fontStyle: 'italic' }}>
                            {ant.species || 'Formica fusca'}
                        </span>
                    </div>

                    {/* Caste & Job */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Caste :</span>
                        <span style={{
                            fontWeight: 800,
                            color: ant.caste === 'QUEEN' ? '#a855f7' : (ant.caste === 'SOLDIER' ? '#ef4444' : '#f59e0b')
                        }}>
                            {ant.caste === 'QUEEN' ? '👑 Reine' : (ant.caste === 'SOLDIER' ? '🛡️ Soldat' : '🐜 Ouvrière')}
                        </span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', gap: 4 }}>
                            <Briefcase size={11} /> Tâche :
                        </span>
                        <span style={{ color: '#10b981', fontWeight: 700 }}>
                            {ant.task || ant.job || 'Fourragement'}
                        </span>
                    </div>

                    {/* Transport / Inventory */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', gap: 4 }}>
                            <Package size={11} /> Transport :
                        </span>
                        <span style={{ fontWeight: 600, color: ant.carriedItem && ant.carriedItem !== 'NONE' ? '#f59e0b' : (isDark ? '#64748b' : '#94a3b8') }}>
                            {ant.carriedItem && ant.carriedItem !== 'NONE' ? `🍯 ${ant.carriedItem}` : 'Aucun'}
                        </span>
                    </div>

                    {/* Health & Energy Bars */}
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, marginBottom: 2 }}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 3, color: '#ef4444' }}>
                                <Heart size={10} /> Santé
                            </span>
                            <span style={{ fontWeight: 700 }}>{Math.round(ant.health ?? 100)}%</span>
                        </div>
                        <div style={{ width: '100%', height: 4, background: isDark ? '#1e293b' : '#e2e8f0', borderRadius: 2, overflow: 'hidden' }}>
                            <div style={{ width: `${Math.round(ant.health ?? 100)}%`, height: '100%', background: '#22c55e', transition: 'width 0.3s' }} />
                        </div>
                    </div>

                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, marginBottom: 2 }}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 3, color: '#f59e0b' }}>
                                <Zap size={10} /> Énergie
                            </span>
                            <span style={{ fontWeight: 700 }}>{Math.round(ant.energy ?? 90)}%</span>
                        </div>
                        <div style={{ width: '100%', height: 4, background: isDark ? '#1e293b' : '#e2e8f0', borderRadius: 2, overflow: 'hidden' }}>
                            <div style={{ width: `${Math.round(ant.energy ?? 90)}%`, height: '100%', background: '#f59e0b', transition: 'width 0.3s' }} />
                        </div>
                    </div>

                    {/* Position & Age */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, color: isDark ? '#94a3b8' : '#64748b' }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                            <MapPin size={10} /> Pos: ({antX.toFixed(1)}, {antZ.toFixed(1)})
                        </span>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                            <Clock size={10} /> Âge: {formatAge()}
                        </span>
                    </div>

                    {/* Action Button: Follow Ant Camera */}
                    <button
                        onClick={() => setFollowAntCamera(!followAntCamera)}
                        style={{
                            marginTop: 2,
                            background: followAntCamera ? '#0284c7' : (isDark ? 'rgba(56, 189, 248, 0.15)' : 'rgba(2, 132, 199, 0.1)'),
                            color: followAntCamera ? '#ffffff' : (isDark ? '#38bdf8' : '#0284c7'),
                            border: '1px solid #0284c7',
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
                        {followAntCamera ? 'Caméra Fixée sur l\'Insecte' : 'Suivre cette Fourmi en 3D'}
                    </button>
                </div>
            )}
        </div>
    )
}
