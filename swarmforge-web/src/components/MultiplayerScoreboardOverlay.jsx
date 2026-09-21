import React, { useState } from 'react'
import { Users, Minus, ChevronDown, X, Crosshair, Crown, Shield, HardHat, Utensils } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'

export default function MultiplayerScoreboardOverlay({ onFocusColony, isVisible, onClose }) {
    const { colonies, ants, nests, playerAlias, playerSpecies, theme, language } = useSimulationStore()
    const [isCollapsed, setIsCollapsed] = useState(false)
    const isDark = theme === 'dark'

    if (!isVisible) return null

    const teamColors = ['#38bdf8', '#f87171', '#4ade80', '#fbbf24', '#c084fc', '#f472b6']

    // Compute live per-colony dynamic statistics from real ants array
    const coloniesData = (colonies || []).map((col, idx) => {
        const isLocal = idx === 0 || col.isLocalParticipant || col.id === 'card_col_1'
        const colAnts = (ants || []).filter(a => a.colonyId === col.id || (idx === 0 && !a.colonyId))
        
        const realQueens = colAnts.length > 0 
            ? colAnts.filter(a => a.caste === 'QUEEN').length 
            : (col.queens ?? (col.initialQueens || 1))
        
        const realWorkers = colAnts.length > 0 
            ? colAnts.filter(a => a.caste === 'WORKER').length 
            : (col.workers ?? (col.initialWorkers || 40))
        
        const realSoldiers = colAnts.length > 0 
            ? colAnts.filter(a => a.caste === 'SOLDIER').length 
            : (col.soldiers ?? (col.initialSoldiers || 10))
        
        const realMales = colAnts.length > 0 
            ? colAnts.filter(a => a.caste === 'MALE').length 
            : (col.males ?? (col.initialMales || 0))

        const totalPop = colAnts.length > 0 ? colAnts.length : (realQueens + realWorkers + realSoldiers + realMales)
        const foodAmount = col.foodStored ?? col.food ?? 250

        const nest = (nests || []).find(n => n.colonyId === col.id) || (nests && nests[idx])
        const nestX = nest?.x ?? (idx === 0 ? 35 : 65)
        const nestY = nest?.y ?? 0
        const nestZ = nest?.z ?? (nest?.y ?? (idx === 0 ? 35 : 65))

        const participantName = isLocal ? (playerAlias || 'Joueur Local') : (col.participantName || `IA Rival #${idx}`)
        const speciesName = isLocal ? (playerSpecies || col.speciesId || 'Formica fusca') : (col.speciesId || col.speciesName || 'Espèce Rivale')

        return {
            id: col.id || `col_${idx}`,
            participantName,
            speciesName,
            teamColorHex: col.color || teamColors[idx % teamColors.length],
            population: totalPop,
            workers: realWorkers,
            soldiers: realSoldiers,
            queens: realQueens,
            males: realMales,
            foodStored: foodAmount,
            isQueenAlive: realQueens > 0,
            nestX,
            nestY,
            nestZ,
            isLocalParticipant: isLocal
        }
    })

    return (
        <div style={{
            position: 'absolute',
            top: 15,
            left: 15,
            width: isCollapsed ? 220 : 310,
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
            backdropFilter: 'blur(14px)',
            borderRadius: 10,
            border: isDark ? '1.2px solid rgba(56, 189, 248, 0.4)' : '1.2px solid rgba(56, 189, 248, 0.6)',
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.4)',
            color: isDark ? '#f1f5f9' : '#0f172a',
            zIndex: 90,
            padding: 10,
            transition: 'all 0.2s ease-in-out',
            fontSize: 11,
            fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
            {/* Header (1:1 JavaFX MultiplayerScoreboardOverlay) */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: isCollapsed ? 0 : 8,
                borderBottom: isCollapsed ? 'none' : (isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)'),
                marginBottom: isCollapsed ? 0 : 8
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <Users size={16} color="#38bdf8" />
                    <div>
                        <div style={{ fontWeight: 800, color: '#38bdf8', fontSize: 12 }}>
                            Scoreboard des Colonies
                        </div>
                        {!isCollapsed && (
                            <div style={{ fontSize: 9.5, color: isDark ? '#94a3b8' : '#64748b' }}>
                                Participants & Biomasse Active
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

            {/* Colonies List (1:1 with JavaFX ColonyCard) */}
            {!isCollapsed && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 360, overflowY: 'auto' }}>
                    {coloniesData.length === 0 ? (
                        <div style={{ color: isDark ? '#64748b' : '#94a3b8', fontStyle: 'italic', textAlign: 'center', padding: '10px 0' }}>
                            Aucune colonie active détectée.
                        </div>
                    ) : (
                        coloniesData.map((entry) => {
                            const isLocal = entry.isLocalParticipant
                            const color = entry.teamColorHex || '#38bdf8'

                            return (
                                <div
                                    key={entry.id}
                                    style={{
                                        background: isDark
                                            ? (isLocal ? 'rgba(56, 189, 248, 0.12)' : 'rgba(30, 41, 59, 0.75)')
                                            : (isLocal ? 'rgba(56, 189, 248, 0.15)' : 'rgba(241, 245, 249, 0.85)'),
                                        border: isLocal
                                            ? '1.5px solid #38bdf8'
                                            : (isDark ? '1px solid rgba(255, 255, 255, 0.08)' : '1px solid rgba(0, 0, 0, 0.08)'),
                                        borderRadius: 8,
                                        padding: '7px 9px',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        gap: 5
                                    }}
                                >
                                    {/* Top Line: Player / AI Identity + Focus Button */}
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <div style={{
                                                width: 9,
                                                height: 9,
                                                borderRadius: '50%',
                                                background: color,
                                                boxShadow: `0 0 6px ${color}`
                                            }} />
                                            <span style={{
                                                fontWeight: 800,
                                                fontSize: 11,
                                                color: isLocal ? '#38bdf8' : (isDark ? '#f1f5f9' : '#0f172a')
                                            }}>
                                                {entry.participantName}
                                            </span>
                                            {isLocal && (
                                                <span style={{
                                                    fontSize: 8.5,
                                                    fontWeight: 800,
                                                    padding: '1px 5px',
                                                    borderRadius: 4,
                                                    background: '#0284c7',
                                                    color: '#ffffff'
                                                }}>
                                                    (Vous)
                                                </span>
                                            )}
                                        </div>

                                        <button
                                            onClick={() => onFocusColony && onFocusColony(entry.nestX, entry.nestY, entry.nestZ)}
                                            title="Centrer la caméra 3D sur le nid"
                                            style={{
                                                background: 'rgba(56, 189, 248, 0.15)',
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

                                    {/* Species Line */}
                                    <div style={{ fontSize: 9.5, color: isDark ? '#94a3b8' : '#64748b', fontStyle: 'italic', marginTop: -2 }}>
                                        {entry.speciesName}
                                    </div>

                                    {/* Metrics Grid (Pop, Ouvrières, Soldats, Stock) */}
                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 4, fontSize: 10 }}>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b' }}>Pop</div>
                                            <div style={{ fontWeight: 800, color: '#38bdf8' }}>{entry.population}</div>
                                        </div>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                                <HardHat size={8} /> Ouvr.
                                            </div>
                                            <div style={{ fontWeight: 700 }}>{entry.workers}</div>
                                        </div>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                                <Shield size={8} /> Sold.
                                            </div>
                                            <div style={{ fontWeight: 700 }}>{entry.soldiers}</div>
                                        </div>
                                        <div style={{ background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: '3px 4px', borderRadius: 4, textAlign: 'center' }}>
                                            <div style={{ fontSize: 8, color: isDark ? '#94a3b8' : '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                                <Utensils size={8} /> Stock
                                            </div>
                                            <div style={{ fontWeight: 700, color: '#f59e0b' }}>{Math.round(entry.foodStored)} mg</div>
                                        </div>
                                    </div>

                                    {/* Queen Status Footer */}
                                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 9 }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                            <Crown size={10} color={entry.isQueenAlive ? '#fbbf24' : '#ef4444'} />
                                            <span style={{ color: entry.isQueenAlive ? '#4ade80' : '#ef4444', fontWeight: 700 }}>
                                                {entry.isQueenAlive ? `Reine active (${entry.queens})` : '💀 Orpheline'}
                                            </span>
                                        </div>
                                        <div style={{ color: isDark ? '#94a3b8' : '#64748b' }}>
                                            Nid: ({Math.round(entry.nestX)}, {Math.round(entry.nestZ)})
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
