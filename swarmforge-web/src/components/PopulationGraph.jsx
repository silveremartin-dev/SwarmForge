import { useEffect, useState, useMemo } from 'react'
import { useSimulationStore } from '../store/simulationStore'

export default function PopulationGraph() {
    const { stats, tick } = useSimulationStore()
    const [history, setHistory] = useState([])
    const [visibleSeries, setVisibleSeries] = useState({
        total: true,
        workers: true,
        soldiers: true,
    })
    const [zoomWindow, setZoomWindow] = useState(60) // window in data points / seconds

    const maxHistoryPoints = 1200 // Store up to 20 minutes of history at 1s tick

    useEffect(() => {
        if (tick === 0) {
            setHistory([{
                tick: 0,
                total: stats.totalPopulation || 0,
                workers: stats.totalWorkers || 0,
                soldiers: stats.totalSoldiers || 0
            }])
            return
        }
        setHistory(prev => {
            const newHistory = [...prev, {
                tick,
                total: stats.totalPopulation || 0,
                workers: stats.totalWorkers || 0,
                soldiers: stats.totalSoldiers || 0
            }]
            if (newHistory.length > maxHistoryPoints) {
                return newHistory.slice(newHistory.length - maxHistoryPoints)
            }
            return newHistory
        })
    }, [tick, stats])

    // Filter displayed history according to zoomWindow
    const displayedHistory = useMemo(() => {
        if (zoomWindow === -1 || history.length <= zoomWindow) {
            return history
        }
        return history.slice(history.length - zoomWindow)
    }, [history, zoomWindow])

    const toggleSeries = (key) => {
        setVisibleSeries(prev => ({ ...prev, [key]: !prev[key] }))
    }

    const handleZoomIn = () => {
        setZoomWindow(prev => {
            if (prev === -1) return 120
            return Math.max(15, Math.floor(prev / 1.5))
        })
    }

    const handleZoomOut = () => {
        setZoomWindow(prev => {
            if (prev === -1) return -1
            const next = Math.floor(prev * 1.5)
            return next >= maxHistoryPoints ? -1 : next
        })
    }

    const width = 280
    const height = 110
    const padding = 8

    // Calculate scales
    const maxPop = useMemo(() => {
        if (displayedHistory.length === 0) return 10
        let highest = 0
        displayedHistory.forEach(h => {
            if (visibleSeries.total && h.total > highest) highest = h.total
            if (visibleSeries.workers && h.workers > highest) highest = h.workers
            if (visibleSeries.soldiers && h.soldiers > highest) highest = h.soldiers
        })
        return Math.max(highest * 1.1, 10)
    }, [displayedHistory, visibleSeries])

    const numPoints = Math.max(displayedHistory.length, 2)
    const getX = (i) => padding + (i / (numPoints - 1)) * (width - 2 * padding)
    const getY = (val) => height - padding - (val / maxPop) * (height - 2 * padding)

    // Generate paths
    const makePath = (key) => {
        if (displayedHistory.length < 2) return ''
        return displayedHistory.map((pt, i) =>
            `${i === 0 ? 'M' : 'L'} ${getX(i)} ${getY(pt[key] || 0)}`
        ).join(' ')
    }

    return (
        <div style={{ marginTop: 14, background: 'rgba(15, 23, 42, 0.6)', padding: '10px 12px', borderRadius: 8, border: '1px solid rgba(255,255,255,0.08)' }}>
            {/* Header with Title and Zoom Controls */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                <div style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8' }}>📈 Historique Population</div>
                <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                    <button
                        onClick={handleZoomIn}
                        title="Zoom avant (+)"
                        style={{ background: '#1e293b', border: '1px solid #475569', color: '#38bdf8', padding: '1px 6px', borderRadius: 4, fontSize: 11, cursor: 'pointer' }}
                    >
                        🔍+
                    </button>
                    <button
                        onClick={handleZoomOut}
                        title="Zoom arrière (-)"
                        style={{ background: '#1e293b', border: '1px solid #475569', color: '#38bdf8', padding: '1px 6px', borderRadius: 4, fontSize: 11, cursor: 'pointer' }}
                    >
                        🔍-
                    </button>
                    <select
                        value={zoomWindow}
                        onChange={(e) => setZoomWindow(Number(e.target.value))}
                        style={{ background: '#1e293b', border: '1px solid #475569', color: '#cbd5e1', padding: '1px 4px', borderRadius: 4, fontSize: 10 }}
                    >
                        <option value={30}>30s</option>
                        <option value={60}>1 min</option>
                        <option value={180}>3 min</option>
                        <option value={300}>5 min</option>
                        <option value={-1}>Tout</option>
                    </select>
                </div>
            </div>

            {displayedHistory.length < 2 ? (
                <div style={{ height: height, color: '#64748b', fontSize: 11, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    Collecte des données...
                </div>
            ) : (
                <svg width="100%" height={height} viewBox={`0 0 ${width} ${height}`} style={{ background: 'rgba(0,0,0,0.3)', borderRadius: 4, overflow: 'visible' }}>
                    {/* Grid lines */}
                    <line x1={0} y1={height / 2} x2={width} y2={height / 2} stroke="#334155" strokeDasharray="3 3" />
                    <line x1={0} y1={padding} x2={width} y2={padding} stroke="#334155" strokeDasharray="3 3" />
                    
                    {/* Axis labels */}
                    <text x={padding} y={padding + 9} fill="#64748b" fontSize="9">{Math.round(maxPop)}</text>
                    <text x={padding} y={height - padding - 2} fill="#64748b" fontSize="9">0</text>
                    <text x={width - padding - 25} y={height - padding - 2} fill="#64748b" fontSize="9">{displayedHistory.length}s</text>

                    {/* Series lines */}
                    {visibleSeries.total && (
                        <path d={makePath('total')} fill="none" stroke="#38bdf8" strokeWidth="2" />
                    )}
                    {visibleSeries.workers && (
                        <path d={makePath('workers')} fill="none" stroke="#22c55e" strokeWidth="1.5" />
                    )}
                    {visibleSeries.soldiers && (
                        <path d={makePath('soldiers')} fill="none" stroke="#f43f5e" strokeWidth="1.5" strokeDasharray="3 2" />
                    )}
                </svg>
            )}

            {/* Series Checkboxes */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, fontSize: 10, marginTop: 8, color: '#cbd5e1' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
                    <input
                        type="checkbox"
                        checked={visibleSeries.total}
                        onChange={() => toggleSeries('total')}
                        style={{ cursor: 'pointer', accentColor: '#38bdf8' }}
                    />
                    <span style={{ color: '#38bdf8' }}>● Total</span>
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
                    <input
                        type="checkbox"
                        checked={visibleSeries.workers}
                        onChange={() => toggleSeries('workers')}
                        style={{ cursor: 'pointer', accentColor: '#22c55e' }}
                    />
                    <span style={{ color: '#22c55e' }}>● Ouvrières</span>
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
                    <input
                        type="checkbox"
                        checked={visibleSeries.soldiers}
                        onChange={() => toggleSeries('soldiers')}
                        style={{ cursor: 'pointer', accentColor: '#f43f5e' }}
                    />
                    <span style={{ color: '#f43f5e' }}>-- Soldats</span>
                </label>
            </div>
        </div>
    )
}

