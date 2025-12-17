import { useEffect, useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'

export default function PopulationGraph() {
    const { stats, tick } = useSimulationStore()
    const [history, setHistory] = useState([])
    const maxPoints = 50

    useEffect(() => {
        setHistory(prev => {
            const newHistory = [...prev, {
                tick,
                total: stats.totalPopulation || 0,
                workers: stats.totalWorkers || 0,
                soldiers: stats.totalSoldiers || 0
            }]
            if (newHistory.length > maxPoints) {
                return newHistory.slice(newHistory.length - maxPoints)
            }
            return newHistory
        })
    }, [tick, stats])

    if (history.length < 2) return <div style={{ height: 100, color: '#666', fontSize: 12, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Gathering data...</div>

    const width = 240
    const height = 100
    const padding = 5

    // Calculate scales
    const maxPop = Math.max(...history.map(h => h.total), 10)

    const getX = (i) => padding + (i / (maxPoints - 1)) * (width - 2 * padding)
    const getY = (val) => height - padding - (val / maxPop) * (height - 2 * padding)

    // Generate paths
    const makePath = (key) => {
        return history.map((pt, i) =>
            `${i === 0 ? 'M' : 'L'} ${getX(i)} ${getY(pt[key])}`
        ).join(' ')
    }

    return (
        <div style={{ marginTop: 16 }}>
            <div style={{ fontSize: 12, color: '#888', marginBottom: 4 }}>Population History</div>
            <svg width="100%" height={height} viewBox={`0 0 ${width} ${height}`} style={{ background: 'rgba(0,0,0,0.2)', borderRadius: 4 }}>
                {/* Grid lines */}
                <line x1={0} y1={height / 2} x2={width} y2={height / 2} stroke="#333" strokeDasharray="4 4" />

                {/* Series */}
                <path d={makePath('total')} fill="none" stroke="#4af" strokeWidth="2" />
                <path d={makePath('soldiers')} fill="none" stroke="#f44" strokeWidth="1.5" strokeDasharray="2 2" />

                {/* Legend */}
            </svg>
            <div style={{ display: 'flex', gap: 12, fontSize: 10, marginTop: 4 }}>
                <span style={{ color: '#4af' }}>● Total</span>
                <span style={{ color: '#f44' }}>- - Soldiers</span>
            </div>
        </div>
    )
}
