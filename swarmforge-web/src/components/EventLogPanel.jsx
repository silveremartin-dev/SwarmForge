import React, { useState, useMemo } from 'react'
import {
    List,
    Filter,
    Search,
    Trash2,
    Download,
    CheckSquare,
    Square,
    AlertCircle,
    Info,
    AlertTriangle,
    XCircle,
    FileText,
    ChevronUp,
    ChevronDown,
    RotateCcw,
    X
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'

export default function EventLogPanel() {
    const {
        eventsLog,
        clearEventLogs,
        theme,
        language
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const [typeFilter, setTypeFilter] = useState('ALL')
    const [severityFilter, setSeverityFilter] = useState('ALL')
    const [searchTerm, setSearchTerm] = useState('')
    const [autoScroll, setAutoScroll] = useState(true)
    const [sortField, setSortField] = useState('tick')
    const [sortDirection, setSortDirection] = useState('desc') // 'asc' | 'desc'
    const [selectedEvent, setSelectedEvent] = useState(null)

    const handleSort = (field) => {
        if (sortField === field) {
            setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc')
        } else {
            setSortField(field)
            setSortDirection('asc')
        }
    }

    const filteredEvents = useMemo(() => {
        let list = eventsLog.filter(evt => {
            if (typeFilter !== 'ALL' && evt.type !== typeFilter) return false
            if (severityFilter !== 'ALL' && evt.severity !== severityFilter) return false
            if (searchTerm.trim()) {
                const term = searchTerm.toLowerCase()
                const matchMsg = (evt.message || '').toLowerCase().includes(term)
                const matchSrc = (evt.source || '').toLowerCase().includes(term)
                const matchType = (evt.type || '').toLowerCase().includes(term)
                if (!matchMsg && !matchSrc && !matchType) return false
            }
            return true
        })

        list.sort((a, b) => {
            let valA = a[sortField]
            let valB = b[sortField]
            if (typeof valA === 'string') {
                return sortDirection === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA)
            }
            return sortDirection === 'asc' ? valA - valB : valB - valA
        })

        return list
    }, [eventsLog, typeFilter, severityFilter, searchTerm, sortField, sortDirection])

    const exportLogs = () => {
        if (filteredEvents.length === 0) {
            showToast('Aucun événement à exporter', 'warning')
            return
        }

        let content = 'Timestamp;Tick;Severite;Type;Source;Message\n'
        filteredEvents.forEach(e => {
            content += `${e.simCalendarTime};${e.tick};${e.severity};${e.type};${e.source};"${e.message.replace(/"/g, '""')}"\n`
        })

        const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `swarmforge_eventlog_${Date.now()}.csv`
        a.click()
        showToast('📋 Journal d\'événements exporté avec succès !', 'success')
    }

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    const getSeverityBadge = (sev) => {
        switch (sev) {
            case 'CRITICAL':
                return <span style={{ background: 'rgba(239, 68, 68, 0.25)', color: '#ef4444', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>CRITIQUE</span>
            case 'ERROR':
                return <span style={{ background: 'rgba(244, 63, 94, 0.25)', color: '#f43f5e', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>ERREUR</span>
            case 'WARNING':
                return <span style={{ background: 'rgba(245, 158, 11, 0.25)', color: '#f59e0b', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>AVERT.</span>
            default:
                return <span style={{ background: 'rgba(56, 189, 248, 0.2)', color: '#38bdf8', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>INFO</span>
        }
    }

    return (
        <div style={{
            maxWidth: 1150,
            margin: '0 auto',
            padding: '20px 24px',
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
            height: 'calc(100% - 40px)',
            boxSizing: 'border-box'
        }}>
            {/* Header & Filter Toolbar */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '14px 18px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
                    <h2 style={{ margin: 0, fontSize: 16, fontWeight: 800, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 8 }}>
                        <List size={18} /> Journal d'Événements de la Simulation
                    </h2>

                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <button
                            onClick={() => setAutoScroll(!autoScroll)}
                            style={{
                                background: 'transparent',
                                border: `1px solid ${borderCol}`,
                                color: textMain,
                                padding: '5px 10px',
                                borderRadius: 6,
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 6
                            }}
                        >
                            {autoScroll ? <CheckSquare size={13} color="#10b981" /> : <Square size={13} />} Défilement Auto
                        </button>

                        <button
                            onClick={exportLogs}
                            style={{
                                background: isDark ? '#047857' : '#10b981',
                                color: '#fff',
                                border: 'none',
                                padding: '5px 12px',
                                borderRadius: 6,
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 6
                            }}
                        >
                            <Download size={13} /> Exporter Logs
                        </button>

                        <button
                            onClick={clearEventLogs}
                            style={{
                                background: 'transparent',
                                border: `1px solid ${borderCol}`,
                                color: '#ef4444',
                                padding: '5px 10px',
                                borderRadius: 6,
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 6
                            }}
                        >
                            <Trash2 size={13} /> Effacer
                        </button>
                    </div>
                </div>

                {/* Filters Row */}
                <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>Type :</span>
                        <select
                            value={typeFilter}
                            onChange={(e) => setTypeFilter(e.target.value)}
                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 8px', fontSize: 11 }}
                        >
                            <option value="ALL">TOUS LES TYPES</option>
                            <option value="SYSTEM">SYSTÈME</option>
                            <option value="ENTITY">ENTITÉ</option>
                            <option value="COLONY">COLONIE</option>
                            <option value="ENVIRONMENT">ENVIRONNEMENT</option>
                            <option value="DISASTER">DÉSASTRE</option>
                            <option value="COMBAT">COMBAT</option>
                            <option value="PHEROMONE">PHÉROMONE</option>
                            <option value="GENETICS">GÉNÉTIQUE</option>
                        </select>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>Sévérité :</span>
                        <select
                            value={severityFilter}
                            onChange={(e) => setSeverityFilter(e.target.value)}
                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 8px', fontSize: 11 }}
                        >
                            <option value="ALL">TOUTES LES SÉVÉRITÉS</option>
                            <option value="INFO">INFO</option>
                            <option value="WARNING">AVERTISSEMENT</option>
                            <option value="ERROR">ERREUR</option>
                            <option value="CRITICAL">CRITIQUE</option>
                        </select>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1, minWidth: 200 }}>
                        <Search size={14} color={textMuted} />
                        <input
                            type="text"
                            placeholder="Rechercher dans les messages, sources..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 8px', fontSize: 11 }}
                        />
                    </div>
                </div>
            </div>

            {/* Events Table Container */}
            <div style={{
                flex: 1,
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column'
            }}>
                <div style={{ flex: 1, overflowY: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12, textAlign: 'left' }}>
                        <thead style={{ position: 'sticky', top: 0, background: isDark ? '#0f172a' : '#f1f5f9', zIndex: 10 }}>
                            <tr style={{ borderBottom: `2px solid ${borderCol}`, color: textMuted }}>
                                <th style={{ padding: '8px 12px', width: 145, cursor: 'pointer' }} onClick={() => handleSort('simCalendarTime')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>Date Calendrier</span>
                                        {sortField === 'simCalendarTime' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 10px', width: 75, cursor: 'pointer' }} onClick={() => handleSort('tick')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>Tick</span>
                                        {sortField === 'tick' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 10px', width: 90, cursor: 'pointer' }} onClick={() => handleSort('severity')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>Sévérité</span>
                                        {sortField === 'severity' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 10px', width: 110, cursor: 'pointer' }} onClick={() => handleSort('type')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>Type</span>
                                        {sortField === 'type' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 12px', width: 140, cursor: 'pointer' }} onClick={() => handleSort('source')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>Source</span>
                                        {sortField === 'source' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 12px' }}>Message & Données</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredEvents.length === 0 ? (
                                <tr>
                                    <td colSpan={6} style={{ padding: 32, textAlign: 'center', color: textMuted }}>
                                        Aucun événement correspondant aux critères de recherche.
                                    </td>
                                </tr>
                            ) : (
                                filteredEvents.map(evt => (
                                    <tr
                                        key={evt.id}
                                        onClick={() => setSelectedEvent(evt)}
                                        style={{
                                            borderBottom: `1px solid ${borderCol}`,
                                            cursor: 'pointer',
                                            background: selectedEvent?.id === evt.id ? (isDark ? 'rgba(56, 189, 248, 0.1)' : 'rgba(2, 132, 199, 0.08)') : 'transparent'
                                        }}
                                    >
                                        <td style={{ padding: '7px 12px', fontFamily: 'monospace', fontSize: 11 }}>
                                            {evt.simCalendarTime}
                                        </td>
                                        <td style={{ padding: '7px 10px', fontWeight: 700, color: '#38bdf8', fontSize: 11 }}>
                                            {evt.tick}
                                        </td>
                                        <td style={{ padding: '7px 10px' }}>
                                            {getSeverityBadge(evt.severity)}
                                        </td>
                                        <td style={{ padding: '7px 10px', fontSize: 11, fontWeight: 600, color: textMuted }}>
                                            {evt.type}
                                        </td>
                                        <td style={{ padding: '7px 12px', fontSize: 11, fontWeight: 700 }}>
                                            {evt.source}
                                        </td>
                                        <td style={{ padding: '7px 12px', color: textMain }}>
                                            {evt.message}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Event Detail Drawer if row selected */}
                {selectedEvent && (
                    <div style={{
                        padding: '12px 16px',
                        borderTop: `2px solid #38bdf8`,
                        background: inputBg,
                        fontSize: 11,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 6
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontWeight: 800, color: '#38bdf8' }}>
                                🔍 Détail de l'Événement (Tick {selectedEvent.tick} - {selectedEvent.simCalendarTime})
                            </span>
                            <button
                                onClick={() => setSelectedEvent(null)}
                                style={{ background: 'transparent', border: 'none', color: textMuted, cursor: 'pointer', padding: 2 }}
                            >
                                <X size={14} />
                            </button>
                        </div>
                        <div style={{ color: textMain }}>
                            <strong>Message :</strong> {selectedEvent.message}
                        </div>
                        {selectedEvent.metadata && Object.keys(selectedEvent.metadata).length > 0 && (
                            <div style={{ color: textMuted, fontFamily: 'monospace', fontSize: 10 }}>
                                <strong>Métadonnées :</strong> {JSON.stringify(selectedEvent.metadata)}
                            </div>
                        )}
                    </div>
                )}

                {/* Bottom Status Bar */}
                <div style={{
                    padding: '8px 16px',
                    borderTop: `1px solid ${borderCol}`,
                    background: inputBg,
                    fontSize: 11,
                    color: textMuted,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <span>
                        Événements affichés : <strong>{filteredEvents.length}</strong> / {eventsLog.length} total
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <FileText size={12} /> Stockage journal : mémoire locale (1000 événements max)
                    </span>
                </div>
            </div>
        </div>
    )
}
