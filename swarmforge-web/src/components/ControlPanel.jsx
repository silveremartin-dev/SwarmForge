import React from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { usePresetStore } from '../store/presetStore'
import PopulationGraph from './PopulationGraph'
import PresetSelectorSection from './control/PresetSelectorSection'
import CheckpointSection from './control/CheckpointSection'
import SimulationPlaybackSection from './control/SimulationPlaybackSection'
import SimulationParametersSection from './control/SimulationParametersSection'

export default function ControlPanel({ inline = false }) {
    const { tick, running, stats } = useSimulationStore()
    const { hasPendingChanges } = usePresetStore()

    const styles = {
        panel: {
            position: inline ? 'relative' : 'absolute',
            top: inline ? 0 : 60,
            right: inline ? 'auto' : 20,
            left: inline ? 0 : 'auto',
            width: inline ? '100%' : 350,
            maxHeight: inline ? 'none' : 'calc(100vh - 80px)',
            overflowY: 'auto',
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(16px)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
            borderRadius: 12,
            padding: 16,
            display: 'flex',
            flexDirection: 'column',
            gap: 12,
            zIndex: 90,
            color: '#e2e8f0',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
        },
        title: {
            fontSize: 16,
            fontWeight: 800,
            color: '#38bdf8',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
        },
        sectionHeader: {
            fontSize: 11,
            fontWeight: 700,
            color: '#94a3b8',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            marginBottom: 6,
            display: 'flex',
            alignItems: 'center',
            gap: 6,
        },
        selectGroup: {
            background: 'rgba(255,255,255,0.03)',
            border: '1px solid rgba(255,255,255,0.06)',
            borderRadius: 8,
            padding: 10,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
        },
        label: {
            fontSize: 11,
            fontWeight: 600,
            color: '#cbd5e1',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
        },
        select: {
            width: '100%',
            background: '#0f172a',
            border: '1px solid #334155',
            color: '#38bdf8',
            padding: '6px 8px',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 600,
            outline: 'none',
        },
        seedBox: {
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            background: '#0f172a',
            border: '1px solid #334155',
            borderRadius: 6,
            padding: '4px 8px',
        },
        seedInput: {
            flex: 1,
            background: 'transparent',
            border: 'none',
            color: '#f59e0b',
            fontWeight: 700,
            fontSize: 12,
            outline: 'none',
        },
        btnRow: {
            display: 'flex',
            gap: 6,
            marginTop: 4,
        },
        iconBtn: {
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 4,
            padding: '5px 8px',
            fontSize: 11,
            fontWeight: 600,
            background: '#1e293b',
            border: '1px solid #334155',
            color: '#cbd5e1',
            borderRadius: 6,
            cursor: 'pointer',
            transition: 'all 0.2s',
        },
        applyBtn: {
            width: '100%',
            padding: '9px 12px',
            fontSize: 12,
            fontWeight: 800,
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            background: hasPendingChanges
                ? 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
                : 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
            color: '#fff',
            boxShadow: hasPendingChanges
                ? '0 4px 14px rgba(245, 158, 11, 0.45)'
                : '0 2px 8px rgba(2, 132, 199, 0.3)',
            animation: hasPendingChanges ? 'pulse 1.5s infinite' : 'none',
        },
        button: {
            width: '100%',
            padding: '10px',
            fontSize: 13,
            fontWeight: 700,
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            transition: 'all 0.2s',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
        },
        playBtn: {
            background: running ? 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' : 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)',
            color: '#fff',
            boxShadow: running ? '0 4px 12px rgba(239, 68, 68, 0.4)' : '0 4px 12px rgba(34, 197, 94, 0.4)',
        },
        slider: {
            width: '100%',
            accentColor: '#38bdf8',
            marginTop: 4,
        },
        statGrid: {
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 6,
        },
        statItem: {
            background: 'rgba(255,255,255,0.03)',
            border: '1px solid rgba(255,255,255,0.05)',
            borderRadius: 6,
            padding: 6,
            textAlign: 'center',
        },
        statValue: {
            fontSize: 16,
            fontWeight: 700,
            color: '#fff',
        },
        statLabel: {
            fontSize: 9,
            color: '#64748b',
            textTransform: 'uppercase',
        },
    }

    return (
        <div style={styles.panel}>
            <div style={styles.title}>
                <span>⚙️ Gestionnaire de Simulation</span>
                {running && <span style={{ fontSize: 10, background: '#16a34a', color: '#fff', padding: '2px 6px', borderRadius: 4 }}>EN COURS</span>}
            </div>

            {/* Presets and Master Seed */}
            <PresetSelectorSection styles={styles} />

            {/* Checkpoints & Restoration */}
            <CheckpointSection styles={styles} />

            {/* Playback Controls */}
            <SimulationPlaybackSection styles={styles} />

            {/* Parameters Tabs & Sliders */}
            <SimulationParametersSection styles={styles} />

            {/* Ticks & Real-Time Metrics */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(0,0,0,0.3)', padding: '6px 10px', borderRadius: 6 }}>
                <span style={{ fontSize: 11, color: '#94a3b8' }}>Ticks Exécutés</span>
                <span style={{ fontSize: 16, fontWeight: 700, color: '#38bdf8' }}>{tick.toLocaleString()}</span>
            </div>

            {/* Statistics */}
            <div style={styles.statGrid}>
                <div style={styles.statItem}>
                    <div style={styles.statValue}>{stats.totalPopulation}</div>
                    <div style={styles.statLabel}>Pop Total</div>
                </div>
                <div style={styles.statItem}>
                    <div style={{ ...styles.statValue, color: '#fbbf24' }}>{stats.totalWorkers}</div>
                    <div style={styles.statLabel}>Ouvrières</div>
                </div>
                <div style={styles.statItem}>
                    <div style={{ ...styles.statValue, color: '#f87171' }}>{stats.totalSoldiers}</div>
                    <div style={styles.statLabel}>Soldats</div>
                </div>
                <div style={styles.statItem}>
                    <div style={{ ...styles.statValue, color: '#4ade80' }}>{stats.totalFood.toFixed(0)}</div>
                    <div style={styles.statLabel}>Stock Nourriture</div>
                </div>
            </div>

            <PopulationGraph />
        </div>
    )
}
