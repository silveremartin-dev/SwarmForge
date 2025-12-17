import { useSimulationStore } from '../store/simulationStore'

export default function InspectorPanel() {
    const { selectedEntity, setSelectedEntity } = useSimulationStore()

    if (!selectedEntity) return null

    const styles = {
        panel: {
            position: 'absolute',
            top: 20,
            right: 20,
            width: 300,
            background: 'rgba(20, 20, 35, 0.9)',
            border: '1px solid #446',
            borderRadius: 8,
            padding: 16,
            color: '#fff',
            backdropFilter: 'blur(5px)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
            transition: 'all 0.2s',
        },
        header: {
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 12,
            borderBottom: '1px solid #335',
            paddingBottom: 8,
        },
        title: {
            fontSize: 18,
            fontWeight: 700,
            color: '#4af',
        },
        closeBtn: {
            background: 'none',
            border: 'none',
            color: '#888',
            cursor: 'pointer',
            fontSize: 16,
        },
        row: {
            display: 'flex',
            justifyContent: 'space-between',
            marginBottom: 8,
            fontSize: 14,
        },
        label: {
            color: '#88a',
        },
        value: {
            fontWeight: 600,
        },
        barContainer: {
            width: '100%',
            height: 6,
            background: '#334',
            borderRadius: 3,
            overflow: 'hidden',
            marginTop: 4,
        },
        bar: (pct, color) => ({
            width: `${Math.max(0, Math.min(100, pct))}%`,
            height: '100%',
            background: color,
            transition: 'width 0.3s',
        }),
    }

    return (
        <div style={styles.panel}>
            <div style={styles.header}>
                <div style={styles.title}>Ant {selectedEntity.id.substring(0, 8)}</div>
                <button style={styles.closeBtn} onClick={() => setSelectedEntity(null)}>✕</button>
            </div>

            <div style={styles.row}>
                <span style={styles.label}>Caste</span>
                <span style={styles.value}>{selectedEntity.caste}</span>
            </div>

            <div style={styles.row}>
                <span style={styles.label}>Position</span>
                <span style={styles.value}>
                    {selectedEntity.x.toFixed(1)}, {selectedEntity.y.toFixed(1)}
                </span>
            </div>

            <div style={styles.row}>
                <span style={styles.label}>Job</span>
                <span style={styles.value}>{selectedEntity.job || 'Idle'}</span>
            </div>

            <div style={styles.row}>
                <span style={styles.label}>Carrying</span>
                <span style={styles.value}>{selectedEntity.carriedItem !== 'NONE' ? selectedEntity.carriedItem : '-'}</span>
            </div>

            {/* Health Bar */}
            <div style={{ marginBottom: 12 }}>
                <div style={styles.row}>
                    <span style={styles.label}>Health</span>
                    <span style={styles.value}>{Math.round(selectedEntity.health)}%</span>
                </div>
                <div style={styles.barContainer}>
                    <div style={styles.bar(selectedEntity.health, '#f44')} />
                </div>
            </div>

            {/* Energy Bar */}
            <div style={{ marginBottom: 12 }}>
                <div style={styles.row}>
                    <span style={styles.label}>Energy</span>
                    <span style={styles.value}>{Math.round(selectedEntity.energy)}%</span>
                </div>
                <div style={styles.barContainer}>
                    <div style={styles.bar(selectedEntity.energy, '#fb0')} />
                </div>
            </div>

            {/* Age */}
            <div style={styles.row}>
                <span style={styles.label}>Age (Ticks)</span>
                <span style={styles.value}>{selectedEntity.age}</span>
            </div>
        </div>
    )
}
