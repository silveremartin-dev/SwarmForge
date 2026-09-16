import React, { useState } from 'react'
import { Users, Minus, ChevronDown, X, Crosshair, Crown, Shield, HardHat, Utensils } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'

export default function MultiplayerScoreboardOverlay({ onFocusColony, isVisible, onClose }) {
    const { colonies, theme, language } = useSimulationStore()
    const [isCollapsed, setIsCollapsed] = useState(false)
    const isDark = theme === 'dark'

    if (!isVisible) return null

    const teamColors = ['#38bdf8', '#f87171', '#4ade80', '#fbbf24', '#c084fc', '#f472b6']

    return (
        <div style={{
            position: 'absolute',
            top: 15,
            left: 15,
            width: isCollapsed ? 220 : 320,
            background: isDark ? 'rgba(15, 23, 42, 0.90)' : 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(12px)',
            borderRadius: 10,
            border: isDark ? '1.2px solid rgba(56, 189, 248, 0.4)' : '1.2px solid rgba(56, 189, 248, 0.6)',
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.35)',
            color: isDark ? '#f1f5f9' : '#0f172a',
            zIndex: 90,
            padding: 10,
            transition: 'all 0.2s ease-in-out',
            fontSize: 11
        }}>
            {/* Header */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: isCollapsed ? 0 : 8,
                borderBottom: isCollapsed ? 'none' : (isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)'),
                marginBottom: isCollapsed ? 0 : 8
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <Users size={15} color="#38bdf8" />
                    <div>
                        <div style={{ fontWeight: 800, color: '#38bdf8', fontSize: 12 }}>
                            Scoreboard des Colonies
                        </div>
                        {!isCollapsed && (
                            <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>
                                Matchmaking / Monde Persistant
                            </div>
                        )}
                    </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <button
                        onClick={() => setIsCollapsed(!isCollapsed)}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: isDark ? '#94a3b8' : '#64748b',
                            cursor: 'pointer',
                            padding: 3,
                            display: 'flex',
                            alignItems: 'center'
                        }}
                        title={isCollapsed ? 'Déplier' : 'Réduire'}
                    >
                        {isCollapsed ? <ChevronDown size={14} /> : <Minus size={14} />}
                    </button>
                    {onClose && (
                        <button
                            onClick={onClose}
                            style={{
                                background: 'transparent',
                                border: 'none',
                                color: '#f87171',
                                cursor: 'pointer',
                                padding: 3,
                                display: 'flex',
                                alignItems: 'center'
                            }}
                            title="Fermer le Scoreboard"
                        >
                            <X size={14} />
                        </button>
                    )}
                </div>
            </div>

            {/* Colonies List */}
            {!isCollapsed && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 360, overflowY: 'auto' }}>
                    {colonies.length === 0 ? (
                        <div style={{ color: isDark ? '#64748b' : '#94a3b8', fontStyle: 'italic', textAlign: 'center', padding: '10px 0' }}>
                            Aucune colonie active détectée.
                        </div>
                    ) : (
                        colonies.map((col, idx) => {
                            const color = teamColors[idx % teamColors.length]
                            const pop = col.population || 0
                            const workers = col.workerCount ?? (col.castes?.WORKER || Math.round(pop * 0.85))
                            const soldiers = col.soldierCount ?? (col.castes?.SOLDIER || Math.round(pop * 0.12))
                            const queens = col.queenCount ?? (col.castes?.QUEEN || 1)
                            const food = col.foodStored ?? col.food ?? 500
                            const isQueenAlive = queens > 0

                            return (
                                <div
                                    key={col.id || idx}
                                    style={{
                                        background: isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(241, 245, 249, 0.85)',
                                        border: isDark ? '1px solid rgba(255, 255, 255, 0.08)' : '1px solid rgba(0, 0, 0, 0.08)',
                                        borderRadius: 8,
                                        padding: '8px 10px',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        gap: 5
                                    }}
                                >
                                    {/* Top Line: Colony Team & Title */}
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <div style={{
                                                width: 10,
                                                height: 10,
                                                borderRadius: '50%',
                                                background: color,
                                                boxShadow: `0 0 8px ${color}`
                                            }} />
                                            <span style={{ fontWeight: 800, fontSize: 11, color: isDark ? '#fff' : '#0f172a' }}>
                                                {col.speciesName || col.name || `Colonie #${idx + 1}`}
                                            </span>
                                            <span style={{
                                                fontSize: 9,
                                                padding: '1px 5px',
                                                borderRadius: 4,
                                                background: isDark ? '#334155' : '#e2e8f0',
                                                color: isDark ? '#cbd5e1' : '#475569'
                                            }}>
                                                {idx === 0 ? 'Joueur' : `IA #${idx}`}
                                            </span>
                                        </div>

                                        <button
                                            onClick={() => onFocusColony && onFocusColony(col.nestX ?? 50, col.nestY ?? 0, col.nestZ ?? 50)}
                                            title="Centrer la caméra 3D sur le nid"
                                            style={{
                                                background: 'transparent',
                                                border: `1px solid ${color}`,
                                                color: color,
                                                borderRadius: 4,
                                                padding: '2px 6px',
                                                fontSize: 9,
                                                fontWeight: 700,
                                                cursor: 'pointer',
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: 3
                                            }}
                                        >
                                            <Crosshair size={10} /> Nid
                                        </button>
                                    </div>

                                    {/* Metrics Grid */}
                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 4, fontSize: 10 }}>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b' }}>Pop</div>
                                            <div style={{ fontWeight: 800, color: '#38bdf8' }}>{pop.toLocaleString()}</div>
                                        </div>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                                <HardHat size={8} /> Ouvr.
                                            </div>
                                            <div style={{ fontWeight: 700 }}>{workers.toLocaleString()}</div>
                                        </div>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                                <Shield size={8} /> Sold.
                                            </div>
                                            <div style={{ fontWeight: 700 }}>{soldiers.toLocaleString()}</div>
                                        </div>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                                <Utensils size={8} /> Stock
                                            </div>
                                            <div style={{ fontWeight: 700, color: '#f59e0b' }}>{Math.round(food)}</div>
                                        </div>
                                    </div>

                                    {/* Queen Status Footer */}
                                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 9 }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                            <Crown size={10} color={isQueenAlive ? '#fbbf24' : '#ef4444'} />
                                            <span style={{ color: isQueenAlive ? '#4ade80' : '#f87171', fontWeight: 700 }}>
                                                {isQueenAlive ? `Reine active (${queens})` : '⚠ Orpheline'}
                                            </span>
                                        </div>
                                        <div style={{ color: isDark ? '#94a3b8' : '#64748b' }}>
                                            Nid: ({Math.round(col.nestX ?? 50)}, {Math.round(col.nestZ ?? 50)})
                                        </div>
                                    </div>
                                </div>
                            )
                        })
                    )}
                </div>
            )}
        </div>
    )
}
