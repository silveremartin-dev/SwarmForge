import React from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { Bug, Home, Heart, Zap, MapPin, Briefcase, Package, Clock, ChevronLeft, ChevronRight, Droplets, Thermometer, Shield, Users, Info } from 'lucide-react'

export default function InspectorPanel() {
    const {
        selectedEntity,
        setSelectedEntity,
        selectedChamber,
        setSelectedChamber,
        selectNextAnt,
        selectPreviousAnt,
        colonies
    } = useSimulationStore()

    if (!selectedEntity && !selectedChamber) return null

    const styles = {
        panel: {
            position: 'absolute',
            top: 60,
            right: 20,
            width: 330,
            background: 'rgba(15, 23, 42, 0.94)',
            border: '1px solid rgba(56, 189, 248, 0.3)',
            borderRadius: 12,
            padding: 16,
            color: '#fff',
            backdropFilter: 'blur(12px)',
            boxShadow: '0 10px 30px rgba(0,0,0,0.6)',
            zIndex: 95,
            fontFamily: 'system-ui, -apple-system, sans-serif',
            transition: 'all 0.2s ease',
        },
        header: {
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 12,
            borderBottom: '1px solid rgba(255,255,255,0.1)',
            paddingBottom: 8,
        },
        title: {
            fontSize: 14,
            fontWeight: 800,
            color: '#38bdf8',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
        },
        navBtn: {
            background: '#1e293b',
            border: '1px solid #334155',
            color: '#38bdf8',
            cursor: 'pointer',
            padding: '3px 6px',
            borderRadius: 6,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 11,
            fontWeight: 700,
            transition: 'all 0.15s ease'
        },
        closeBtn: {
            background: '#1e293b',
            border: '1px solid #334155',
            color: '#94a3b8',
            cursor: 'pointer',
            fontSize: 12,
            width: 24,
            height: 24,
            borderRadius: 6,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
        },
        badge: (bg, color) => ({
            background: bg,
            color: color,
            padding: '2px 8px',
            borderRadius: 6,
            fontSize: 11,
            fontWeight: 700,
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
        }),
        row: {
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 8,
            fontSize: 12,
        },
        label: {
            color: '#94a3b8',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
        },
        value: {
            fontWeight: 600,
            color: '#f8fafc',
        },
        barContainer: {
            width: '100%',
            height: 6,
            background: '#1e293b',
            borderRadius: 3,
            overflow: 'hidden',
            marginTop: 4,
            border: '1px solid rgba(255,255,255,0.05)',
        },
        bar: (pct, color) => ({
            width: `${Math.max(0, Math.min(100, pct))}%`,
            height: '100%',
            background: color,
            transition: 'width 0.3s',
        }),
    }

    // ── RENDER 1: CHAMBER OVERLAY DETAILS ──
    if (selectedChamber) {
        const occPct = Math.round(((selectedChamber.occupants || 0) / (selectedChamber.capacity || 50)) * 100)
        return (
            <div style={styles.panel}>
                <div style={styles.header}>
                    <div style={styles.title}>
                        <span>{selectedChamber.icon || '🏛️'}</span>
                        <span style={{ color: '#f59e0b' }}>{selectedChamber.name}</span>
                    </div>
                    <button style={styles.closeBtn} onClick={() => setSelectedChamber(null)}>✕</button>
                </div>

                {/* Nid & Type Badges */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 12, background: 'rgba(255,255,255,0.03)', padding: 8, borderRadius: 8, border: '1px solid rgba(255,255,255,0.06)' }}>
                    <div style={styles.row}>
                        <span style={styles.label}><Home size={13} style={{ color: '#38bdf8' }} /> Nid Source</span>
                        <span style={styles.badge('rgba(56, 189, 248, 0.15)', '#38bdf8')}>
                            {selectedChamber.nestName || 'Fourmilière'}
                        </span>
                    </div>
                    <div style={{ ...styles.row, marginBottom: 0 }}>
                        <span style={styles.label}><Users size={13} style={{ color: '#a855f7' }} /> Rôle / Caste</span>
                        <span style={styles.badge('rgba(168, 85, 247, 0.15)', '#d8b4fe')}>
                            {selectedChamber.caste || 'Ouvrières'}
                        </span>
                    </div>
                </div>

                {/* Occupants & Capacity */}
                <div style={{ marginBottom: 10 }}>
                    <div style={styles.row}>
                        <span style={styles.label}><Users size={12} style={{ color: '#38bdf8' }} /> Population Présente</span>
                        <span style={styles.value}>
                            <strong style={{ color: '#38bdf8' }}>{selectedChamber.occupants || 0}</strong> / {selectedChamber.capacity || 50} ({occPct}%)
                        </span>
                    </div>
                    <div style={styles.barContainer}>
                        <div style={styles.bar(occPct, occPct > 90 ? '#ef4444' : (occPct > 60 ? '#f59e0b' : '#22c55e'))} />
                    </div>
                </div>

                {/* Microclimate & Safety */}
                <div style={styles.row}>
                    <span style={styles.label}><Thermometer size={12} style={{ color: '#f87171' }} /> Température</span>
                    <span style={styles.value}>{selectedChamber.temperature ?? 22.5}°C</span>
                </div>

                <div style={styles.row}>
                    <span style={styles.label}><Droplets size={12} style={{ color: '#38bdf8' }} /> Hygrométrie</span>
                    <span style={styles.value}>{selectedChamber.humidity ?? 65}%</span>
                </div>

                <div style={styles.row}>
                    <span style={styles.label}><Package size={12} style={{ color: '#fbbf24' }} /> Nourriture Stockée</span>
                    <span style={{ ...styles.value, color: '#4ade80' }}>{selectedChamber.foodStored ?? 0} mg</span>
                </div>

                <div style={styles.row}>
                    <span style={styles.label}><Shield size={12} style={{ color: '#a78bfa' }} /> Sécurité Défensive</span>
                    <span style={{ ...styles.value, color: '#c084fc', fontWeight: 700 }}>{selectedChamber.safetyLevel || 'Normale'}</span>
                </div>

                <div style={styles.row}>
                    <span style={styles.label}><MapPin size={12} /> Coordonnées 3D</span>
                    <span style={{ ...styles.value, color: '#94a3b8', fontSize: 11 }}>
                        X:{selectedChamber.position?.x?.toFixed(1)}m, Y:{selectedChamber.position?.y?.toFixed(1)}m, Z:{selectedChamber.position?.z?.toFixed(1)}m
                    </span>
                </div>

                {/* Description */}
                {selectedChamber.description && (
                    <div style={{ marginTop: 8, background: 'rgba(0,0,0,0.3)', padding: 8, borderRadius: 6, fontSize: 10, color: '#94a3b8', lineHeight: 1.4 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#38bdf8', fontWeight: 700, marginBottom: 2 }}>
                            <Info size={11} /> Bio-Fonctionnement :
                        </div>
                        {selectedChamber.description}
                    </div>
                )}
            </div>
        )
    }

    // ── RENDER 2: ANT INDIVIDUAL DETAILS ──
    const antColony = colonies?.find(c => c.id === selectedEntity.colonyId) || {
        name: selectedEntity.colonyName || 'Colonie Native',
        color: '#38bdf8'
    }

    const formatAge = () => {
        let days = selectedEntity.ageInDays
        if (days === undefined) {
            if (typeof selectedEntity.age === 'number') {
                days = selectedEntity.age > 1000 ? selectedEntity.age / 86400.0 : selectedEntity.age * 0.1
            } else {
                days = 12
            }
        }

        if (days < 60) {
            return `${Math.max(1, Math.round(days))} jours`
        } else if (days < 365) {
            return `${(days / 30.4).toFixed(1)} mois (${Math.round(days)} j)`
        } else {
            return `${(days / 365.25).toFixed(1)} an(s) (${Math.round(days)} j)`
        }
    }

    const shortId = selectedEntity.id ? selectedEntity.id.replace('ant_', '').slice(0, 10) : '001'

    return (
        <div style={styles.panel}>
            <div style={styles.header}>
                <div style={styles.title}>
                    <Bug size={16} className="text-sky-400" />
                    <span>Fourmi #{shortId}</span>
                </div>

                {/* Previous / Next Ant Cycle Navigation Buttons */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <button
                        style={styles.navBtn}
                        onClick={selectPreviousAnt}
                        title="Passer à la fourmi précédente"
                    >
                        <ChevronLeft size={13} />
                        <span>Préc.</span>
                    </button>
                    <button
                        style={styles.navBtn}
                        onClick={selectNextAnt}
                        title="Passer à la fourmi suivante"
                    >
                        <span>Suiv.</span>
                        <ChevronRight size={13} />
                    </button>
                    <button style={styles.closeBtn} onClick={() => setSelectedEntity(null)}>✕</button>
                </div>
            </div>

            {/* Espèce & Colonie Header Badges */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 12, background: 'rgba(255,255,255,0.03)', padding: 8, borderRadius: 8, border: '1px solid rgba(255,255,255,0.06)' }}>
                <div style={styles.row}>
                    <span style={styles.label}><Bug size={13} style={{ color: '#f43f5e' }} /> Espèce</span>
                    <span style={styles.badge('rgba(244, 63, 94, 0.15)', '#fda4af')}>
                        {selectedEntity.species || 'Formica fusca'}
                    </span>
                </div>
                <div style={{ ...styles.row, marginBottom: 0 }}>
                    <span style={styles.label}><Home size={13} style={{ color: antColony.color || '#38bdf8' }} /> Colonie</span>
                    <span style={styles.badge('rgba(56, 189, 248, 0.15)', antColony.color || '#38bdf8')}>
                        ● {antColony.name}
                    </span>
                </div>
            </div>

            {/* Caste & Tâche */}
            <div style={styles.row}>
                <span style={styles.label}>Caste</span>
                <span style={{ fontWeight: 700, color: selectedEntity.caste === 'QUEEN' ? '#ffd700' : (selectedEntity.caste === 'SOLDIER' ? '#f87171' : '#38bdf8') }}>
                    {selectedEntity.caste === 'QUEEN' ? '👑 Reine' : (selectedEntity.caste === 'SOLDIER' ? '🛡️ Soldat' : '🐜 Ouvrière')}
                </span>
            </div>

            <div style={styles.row}>
                <span style={styles.label}><Briefcase size={12} /> Tâche (Job)</span>
                <span style={styles.value}>{selectedEntity.job || 'Patrouille'}</span>
            </div>

            <div style={styles.row}>
                <span style={styles.label}><Package size={12} /> Transport</span>
                <span style={styles.value}>
                    {selectedEntity.carriedItem && selectedEntity.carriedItem !== 'NONE' ? `🍯 ${selectedEntity.carriedItem}` : '—'}
                </span>
            </div>

            <div style={styles.row}>
                <span style={styles.label}><MapPin size={12} /> Position 3D</span>
                <span style={{ ...styles.value, color: '#94a3b8', fontSize: 11 }}>
                    X:{selectedEntity.x.toFixed(1)}m, Z:{selectedEntity.y.toFixed(1)}m
                </span>
            </div>

            {/* Health Bar */}
            <div style={{ marginBottom: 10 }}>
                <div style={styles.row}>
                    <span style={styles.label}><Heart size={12} style={{ color: '#ef4444' }} /> Santé</span>
                    <span style={styles.value}>{Math.round(selectedEntity.health ?? 100)}%</span>
                </div>
                <div style={styles.barContainer}>
                    <div style={styles.bar(selectedEntity.health ?? 100, '#22c55e')} />
                </div>
            </div>

            {/* Energy Bar */}
            <div style={{ marginBottom: 10 }}>
                <div style={styles.row}>
                    <span style={styles.label}><Zap size={12} style={{ color: '#f59e0b' }} /> Énergie</span>
                    <span style={styles.value}>{Math.round(selectedEntity.energy ?? 90)}%</span>
                </div>
                <div style={styles.barContainer}>
                    <div style={styles.bar(selectedEntity.energy ?? 90, '#f59e0b')} />
                </div>
            </div>

            {/* Age */}
            <div style={{ ...styles.row, marginBottom: 0 }}>
                <span style={styles.label}><Clock size={12} /> Âge Réel</span>
                <span style={{ ...styles.value, color: '#a78bfa', fontWeight: 700 }}>
                    {formatAge()}
                </span>
            </div>
        </div>
    )
}
