import { useSimulationStore } from '../store/simulationStore'
import PopulationGraph from './PopulationGraph'

export default function ControlPanel() {
    const {
        tick,
        running,
        speed,
        stats,
        play,
        pause,
        setSpeed,
    } = useSimulationStore()

    const styles = {
        panel: {
            width: 280,
            background: 'linear-gradient(180deg, #1a1a2e 0%, #16162a 100%)',
            borderLeft: '1px solid #333',
            padding: 16,
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
        },
        title: {
            fontSize: 20,
            fontWeight: 600,
            color: '#fff',
            marginBottom: 8,
        },
        section: {
            background: 'rgba(255,255,255,0.05)',
            borderRadius: 8,
            padding: 12,
        },
        label: {
            fontSize: 12,
            color: '#888',
            marginBottom: 4,
        },
        value: {
            fontSize: 24,
            fontWeight: 700,
            color: '#4af',
        },
        button: {
            padding: '10px 20px',
            fontSize: 14,
            fontWeight: 600,
            border: 'none',
            borderRadius: 6,
            cursor: 'pointer',
            transition: 'all 0.2s',
        },
        playBtn: {
            background: running ? '#f44' : '#4f4',
            color: '#fff',
        },
        slider: {
            width: '100%',
            marginTop: 8,
        },
        statGrid: {
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 8,
        },
        statItem: {
            background: 'rgba(255,255,255,0.03)',
            borderRadius: 6,
            padding: 8,
            textAlign: 'center',
        },
        statValue: {
            fontSize: 18,
            fontWeight: 600,
            color: '#fff',
        },
        statLabel: {
            fontSize: 10,
            color: '#666',
            textTransform: 'uppercase',
        },
    }

    return (
        <div style={styles.panel}>
            <div style={styles.title}>🐜 SwarmForge</div>

            {/* Simulation Controls */}
            <div style={styles.section}>
                <div style={styles.label}>Simulation</div>
                <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                    <button
                        style={{ ...styles.button, ...styles.playBtn }}
                        onClick={() => running ? pause() : play()}
                    >
                        {running ? '⏸ Pause' : '▶ Play'}
                    </button>
                </div>
                <div>
                    <div style={styles.label}>Speed: {speed.toFixed(1)}x</div>
                    <input
                        type="range"
                        min="0.1"
                        max="10"
                        step="0.1"
                        value={speed}
                        onChange={(e) => setSpeed(parseFloat(e.target.value))}
                        style={styles.slider}
                    />
                </div>
            </div>

            {/* Tick Counter */}
            <div style={styles.section}>
                <div style={styles.label}>Current Tick</div>
                <div style={styles.value}>{tick.toLocaleString()}</div>
            </div>

            {/* Statistics */}
            <div style={styles.section}>
                <div style={styles.label}>Population</div>
                <div style={styles.statGrid}>
                    <div style={styles.statItem}>
                        <div style={styles.statValue}>{stats.totalPopulation}</div>
                        <div style={styles.statLabel}>Total</div>
                    </div>
                    <div style={styles.statItem}>
                        <div style={{ ...styles.statValue, color: '#8b4513' }}>{stats.totalWorkers}</div>
                        <div style={styles.statLabel}>Workers</div>
                    </div>
                    <div style={styles.statItem}>
                        <div style={{ ...styles.statValue, color: '#f44' }}>{stats.totalSoldiers}</div>
                        <div style={styles.statLabel}>Soldiers</div>
                    </div>
                    <div style={styles.statItem}>
                        <div style={{ ...styles.statValue, color: '#4f4' }}>{stats.totalFood.toFixed(0)}</div>
                        <div style={styles.statLabel}>Food</div>
                    </div>
                </div>
            </div>
            <PopulationGraph />

            {/* Info */}
            <div style={{ marginTop: 'auto', fontSize: 11, color: '#555', textAlign: 'center' }}>
                SwarmForge v2.0<br />
                Eusocial Insect Simulation
            </div>
        </div >
    )
}
