import React, { useState, useRef, useEffect } from 'react';
import { useSimulationStore } from '../store/simulationStore';
import { Terminal, Download, Trash2, Search } from 'lucide-react';

export default function EventLogPanel() {
    const { eventLogs, clearEventLogs, running, tick, play, pause } = useSimulationStore();
    const [levelFilter, setLevelFilter] = useState('ALL'); // 'ALL' | 'INFO' | 'DEBUG' | 'VERBOSE'
    const [searchTerm, setSearchTerm] = useState('');
    const [collapsed, setCollapsed] = useState(false);
    const logEndRef = useRef(null);

    // Auto-scroll to bottom of log when new events arrive
    useEffect(() => {
        if (!collapsed && logEndRef.current) {
            logEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [eventLogs.length, collapsed]);

    const filteredLogs = eventLogs.filter(log => {
        const matchesLevel = levelFilter === 'ALL' || log.level === levelFilter;
        const matchesSearch = !searchTerm || 
            log.message?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.category?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            String(log.tick).includes(searchTerm);
        return matchesLevel && matchesSearch;
    });

    const handleExportLog = (format = 'json') => {
        let content, filename, mime;
        if (format === 'json') {
            content = JSON.stringify(eventLogs, null, 2);
            filename = `simulation_events_tick_${tick}_${new Date().toISOString().slice(0, 10)}.json`;
            mime = 'application/json';
        } else {
            content = eventLogs.map(l => `[${new Date(l.timestamp).toLocaleTimeString()}] [Tick #${l.tick}] [${l.level}] [${l.category}] ${l.message}`).join('\n');
            filename = `simulation_events_tick_${tick}_${new Date().toISOString().slice(0, 10)}.txt`;
            mime = 'text/plain';
        }
        const blob = new Blob([content], { type: mime });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
    };

    const getLevelBadge = (level) => {
        switch (level) {
            case 'INFO':
                return <span style={{ color: '#38bdf8', background: 'rgba(56, 189, 248, 0.15)', padding: '1px 5px', borderRadius: 4, fontSize: 9, fontWeight: 700 }}>INFO</span>;
            case 'DEBUG':
                return <span style={{ color: '#fbbf24', background: 'rgba(251, 191, 36, 0.15)', padding: '1px 5px', borderRadius: 4, fontSize: 9, fontWeight: 700 }}>DEBUG</span>;
            case 'VERBOSE':
                return <span style={{ color: '#a78bfa', background: 'rgba(167, 139, 250, 0.15)', padding: '1px 5px', borderRadius: 4, fontSize: 9, fontWeight: 700 }}>DENSE</span>;
            case 'WARN':
                return <span style={{ color: '#f97316', background: 'rgba(249, 115, 22, 0.15)', padding: '1px 5px', borderRadius: 4, fontSize: 9, fontWeight: 700 }}>WARN</span>;
            case 'ERROR':
                return <span style={{ color: '#ef4444', background: 'rgba(239, 68, 68, 0.15)', padding: '1px 5px', borderRadius: 4, fontSize: 9, fontWeight: 700 }}>ERROR</span>;
            default:
                return <span style={{ color: '#94a3b8', background: 'rgba(148, 163, 184, 0.15)', padding: '1px 5px', borderRadius: 4, fontSize: 9 }}>LOG</span>;
        }
    };

    const styles = {
        container: {
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(16px)',
            border: '1px solid rgba(56, 189, 248, 0.25)',
            borderRadius: 12,
            padding: 12,
            color: '#f8fafc',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            boxShadow: '0 10px 30px rgba(0,0,0,0.6)',
            marginBottom: 10,
        },
        header: {
            fontSize: 13,
            fontWeight: 800,
            color: '#38bdf8',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: collapsed ? 0 : 8,
            cursor: 'pointer',
        },
        toolbar: {
            display: 'flex',
            gap: 6,
            alignItems: 'center',
            marginBottom: 8,
            flexWrap: 'wrap',
        },
        searchInput: {
            flex: 1,
            background: '#0f172a',
            border: '1px solid #334155',
            color: '#38bdf8',
            padding: '4px 8px',
            borderRadius: 6,
            fontSize: 11,
            outline: 'none',
        },
        filterBtn: (active) => ({
            padding: '3px 7px',
            fontSize: 10,
            fontWeight: 700,
            border: active ? '1px solid #38bdf8' : '1px solid #334155',
            borderRadius: 4,
            background: active ? '#1e293b' : 'transparent',
            color: active ? '#38bdf8' : '#94a3b8',
            cursor: 'pointer',
        }),
        logList: {
            maxHeight: 220,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 4,
            background: '#090d16',
            border: '1px solid #1e293b',
            borderRadius: 6,
            padding: 8,
            fontFamily: 'Consolas, Monaco, monospace',
            fontSize: 11,
        },
        logItem: {
            display: 'flex',
            alignItems: 'flex-start',
            gap: 6,
            lineHeight: 1.35,
            borderBottom: '1px dotted rgba(255,255,255,0.05)',
            paddingBottom: 3,
        },
        actionBtn: {
            padding: '3px 6px',
            fontSize: 10,
            background: '#1e293b',
            border: '1px solid #334155',
            color: '#cbd5e1',
            borderRadius: 4,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 4,
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.header} onClick={() => setCollapsed(!collapsed)}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Terminal size={15} className="text-sky-400" />
                    <span>📜 Journal d'Événements (Live Log)</span>
                    <span style={{ fontSize: 10, background: '#0284c7', color: '#fff', padding: '1px 6px', borderRadius: 4 }}>
                        {filteredLogs.length} / {eventLogs.length} évts
                    </span>
                </div>
                <span style={{ fontSize: 10, color: '#94a3b8' }}>{collapsed ? '▶ Déplier' : '▼ Réduire'}</span>
            </div>

            {!collapsed && (
                <>
                    {/* Toolbar with Filter & Export */}
                    <div style={styles.toolbar}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4, flex: 1 }}>
                            <Search size={12} className="text-slate-400" />
                            <input
                                type="text"
                                placeholder="Rechercher événements (tick, ouvrière, météo)..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                style={styles.searchInput}
                            />
                        </div>

                        <div style={{ display: 'flex', gap: 3 }}>
                            {['ALL', 'INFO', 'DEBUG', 'VERBOSE'].map(lvl => (
                                <button
                                    key={lvl}
                                    style={styles.filterBtn(levelFilter === lvl)}
                                    onClick={() => setLevelFilter(lvl)}
                                >
                                    {lvl === 'VERBOSE' ? 'DENSE (Bas Nivo)' : lvl}
                                </button>
                            ))}
                        </div>

                        <div style={{ display: 'flex', gap: 4 }}>
                            <button style={styles.actionBtn} onClick={() => handleExportLog('json')} title="Exporter le log en JSON">
                                <Download size={11} /> JSON
                            </button>
                            <button style={styles.actionBtn} onClick={clearEventLogs} title="Effacer le journal">
                                <Trash2 size={11} />
                            </button>
                        </div>
                    </div>

                    {/* Live Log Area */}
                    <div style={styles.logList}>
                        {filteredLogs.length === 0 ? (
                            <div style={{ color: '#64748b', fontStyle: 'italic', textAlign: 'center', padding: 12, fontSize: 11 }}>
                                Aucun événement journalisé. Cliquez sur "▶ LANCER SIMULATION" pour démarrer l'émission des événements bas niveau !
                            </div>
                        ) : (
                            filteredLogs.map(log => (
                                <div key={log.id} style={styles.logItem}>
                                    <span style={{ color: '#f59e0b', fontSize: 10, fontWeight: 700, minWidth: 60 }}>
                                        #{log.tick}
                                    </span>
                                    {getLevelBadge(log.level)}
                                    <span style={{ color: '#64748b', fontSize: 9 }}>
                                        {new Date(log.timestamp).toLocaleTimeString()}
                                    </span>
                                    <span style={{ color: log.level === 'WARN' ? '#fbbf24' : log.level === 'ERROR' ? '#f87171' : '#e2e8f0', flex: 1 }}>
                                        {log.message}
                                    </span>
                                </div>
                            ))
                        )}
                        <div ref={logEndRef} />
                    </div>
                </>
            )}
        </div>
    );
}
