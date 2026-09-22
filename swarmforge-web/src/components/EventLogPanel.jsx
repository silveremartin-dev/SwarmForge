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
            showToast(t('noEvents', 'Aucun événement à exporter'), 'warning')
            return
        }

        let content = `${t('colTimestamp', 'Timestamp')};${t('colTick', 'Tick')};${t('colSeverity', 'Severite')};${t('colType', 'Type')};${t('colSource', 'Source')};${t('colMessage', 'Message')}\n`
        filteredEvents.forEach(e => {
            content += `${e.simCalendarTime};${e.tick};${e.severity};${e.type};${e.source};"${(e.message || '').replace(/"/g, '""')}"\n`
        })

        const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `swarmforge_eventlog_${Date.now()}.csv`
        a.click()
        showToast('📋 CSV Export OK', 'success')
    }

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    const getSeverityBadge = (sev) => {
        switch (sev) {
            case 'CRITICAL':
                return <span style={{ background: 'rgba(239, 68, 68, 0.25)', color: '#ef4444', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>{t('sevCritical', 'CRITIQUE')}</span>
            case 'ERROR':
                return <span style={{ background: 'rgba(244, 63, 94, 0.25)', color: '#f43f5e', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>{t('sevError', 'ERREUR')}</span>
            case 'WARNING':
                return <span style={{ background: 'rgba(245, 158, 11, 0.25)', color: '#f59e0b', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>{t('sevWarning', 'AVERT.')}</span>
            default:
                return <span style={{ background: 'rgba(56, 189, 248, 0.2)', color: '#38bdf8', padding: '2px 6px', borderRadius: 4, fontWeight: 800, fontSize: 10 }}>{t('sevInfo', 'INFO')}</span>
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
                        <List size={18} /> {t('eventLogTitle', 'Journal d\'Événements de la Simulation')}
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
                            {autoScroll ? <CheckSquare size={13} color="#10b981" /> : <Square size={13} />} {t('autoScroll', 'Défilement Auto')}
                        </button>

                        <button
                            onClick={exportLogs}
                            style={{
                                background: isDark ? '#047857' : '#10b981',
                                color: '#fff',
                                border: 'none',
                                padding: '5px 10px',
                                borderRadius: 6,
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 5
                            }}
                        >
                            <Download size={13} /> {t('exportCsv', 'CSV')}
                        </button>

                        <button
                            onClick={() => {
                                const logs = eventsLog
                                const blob = new Blob([JSON.stringify(logs, null, 2)], { type: 'application/json' })
                                const url = URL.createObjectURL(blob)
                                const a = document.createElement('a')
                                a.href = url
                                a.download = `swarmforge_eventlog_${Date.now()}.json`
                                a.click()
                                showToast('📋 JSON Export OK', 'success')
                            }}
                            style={{
                                background: isDark ? '#334155' : '#e2e8f0',
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                padding: '5px 10px',
                                borderRadius: 6,
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 5
                            }}
                        >
                            <Download size={13} /> {t('exportJson', 'JSON')}
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
                            <Trash2 size={13} /> {t('clearLogs', 'Effacer')}
                        </button>
                    </div>
                </div>

                {/* Filters Row */}
                <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>{t('colType', 'Type')} :</span>
                        <select
                            value={typeFilter}
                            onChange={(e) => setTypeFilter(e.target.value)}
                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 8px', fontSize: 11 }}
                        >
                            <option value="ALL">{t('filterTypeAll', 'TOUS LES TYPES')}</option>
                            <option value="COLONY_FOUNDED">{t('evt_COLONY_FOUNDED', 'Fondation de Colonie')}</option>
                            <option value="FOOD_DISCOVERED">{t('evt_FOOD_DISCOVERED', 'Nourriture Découverte')}</option>
                            <option value="TROPHALLAXIS">{t('evt_TROPHALLAXIS', 'Trophallaxie Stomodéale')}</option>
                            <option value="WORKER_BORN">{t('evt_WORKER_BORN', 'Éclosion Ouvrière')}</option>
                            <option value="SOLDIER_BORN">{t('evt_SOLDIER_BORN', 'Éclosion Soldat')}</option>
                            <option value="QUEEN_BORN">{t('evt_QUEEN_BORN', 'Naissance Royale (Reine)')}</option>
                            <option value="WORKER_DIED">{t('evt_WORKER_DIED', 'Mort d\'Ouvrière')}</option>
                            <option value="SOLDIER_DIED">{t('evt_SOLDIER_DIED', 'Mort de Soldat')}</option>
                            <option value="QUEEN_DIED">{t('evt_QUEEN_DIED', 'Décès de la Reine')}</option>
                            <option value="TERRITORY_CLAIMED">{t('evt_TERRITORY_CLAIMED', 'Territoire Marqué')}</option>
                            <option value="COMBAT_OCCURRED">{t('evt_COMBAT_OCCURRED', 'Combat Interspécifique')}</option>
                            <option value="RAID_STARTED">{t('evt_RAID_STARTED', 'Raid Déclenché')}</option>
                            <option value="WEATHER_CHANGED">{t('evt_WEATHER_CHANGED', 'Changement Météorologique')}</option>
                            <option value="SEASON_CHANGED">{t('evt_SEASON_CHANGED', 'Changement de Saison')}</option>
                            <option value="DISASTER_OCCURRED">{t('evt_DISASTER_OCCURRED', 'Désastre Écologique')}</option>
                            <option value="MILESTONE_REACHED">{t('evt_MILESTONE_REACHED', 'Cap Démographique Atteint')}</option>
                            <option value="GOD_MODE_INTERVENTION">{t('evt_GOD_MODE_INTERVENTION', 'Intervention Divine')}</option>
                            <option value="SIMULATION_STARTED">{t('evt_SIMULATION_STARTED', 'Simulation Démarrée')}</option>
                            <option value="SIMULATION_PAUSED">{t('evt_SIMULATION_PAUSED', 'Simulation en Pause')}</option>
                            <option value="SYSTEM">{t('evt_SYSTEM', 'Système')}</option>
                        </select>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>{t('colSeverity', 'Sévérité')} :</span>
                        <select
                            value={severityFilter}
                            onChange={(e) => setSeverityFilter(e.target.value)}
                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 8px', fontSize: 11 }}
                        >
                            <option value="ALL">{t('filterSeverityAll', 'TOUTES LES SÉVÉRITÉS')}</option>
                            <option value="INFO">{t('sevInfo', 'INFO')}</option>
                            <option value="WARNING">{t('sevWarning', 'AVERTISSEMENT')}</option>
                            <option value="ERROR">{t('sevError', 'ERREUR')}</option>
                            <option value="CRITICAL">{t('sevCritical', 'CRITIQUE')}</option>
                        </select>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1, minWidth: 200 }}>
                        <Search size={14} color={textMuted} />
                        <input
                            type="text"
                            placeholder={t('searchPlaceholder', 'Rechercher dans les messages, sources, identifiants...')}
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
                                <th style={{ padding: '8px 10px', width: 85, cursor: 'pointer' }} onClick={() => handleSort('severity')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>{t('colSeverity', 'Sévérité')}</span>
                                        {sortField === 'severity' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 10px', width: 100, cursor: 'pointer' }} onClick={() => handleSort('sequenceId')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>ID</span>
                                        {sortField === 'sequenceId' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 12px', width: 145, cursor: 'pointer' }} onClick={() => handleSort('simCalendarTime')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>{t('colTimestamp', 'Date Calendrier')}</span>
                                        {sortField === 'simCalendarTime' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 10px', width: 75, cursor: 'pointer' }} onClick={() => handleSort('tick')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>{t('colTick', 'Tick')}</span>
                                        {sortField === 'tick' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 10px', width: 155, cursor: 'pointer' }} onClick={() => handleSort('type')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>{t('colType', 'Type')}</span>
                                        {sortField === 'type' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 12px', width: 140, cursor: 'pointer' }} onClick={() => handleSort('source')}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                        <span>{t('colSource', 'Source')}</span>
                                        {sortField === 'source' && (sortDirection === 'asc' ? <ChevronUp size={12} /> : <ChevronDown size={12} />)}
                                    </div>
                                </th>
                                <th style={{ padding: '8px 12px' }}>{t('colMessage', 'Message & Données')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredEvents.length === 0 ? (
                                <tr>
                                    <td colSpan={7} style={{ padding: 32, textAlign: 'center', color: textMuted }}>
                                        {t('noEvents', 'Aucun événement correspondant aux critères de recherche.')}
                                    </td>
                                </tr>
                            ) : (
                                filteredEvents.map(evt => {
                                    const seqStr = `EVT-${String(evt.sequenceId || 1).padStart(6, '0')}`
                                    const localizedType = t(`evt_${evt.type}`, evt.type)
                                    return (
                                        <tr
                                            key={evt.id}
                                            onClick={() => setSelectedEvent(evt)}
                                            style={{
                                                borderBottom: `1px solid ${borderCol}`,
                                                cursor: 'pointer',
                                                background: selectedEvent?.id === evt.id ? (isDark ? 'rgba(56, 189, 248, 0.1)' : 'rgba(2, 132, 199, 0.08)') : 'transparent'
                                            }}
                                        >
                                            <td style={{ padding: '7px 10px' }}>
                                                {getSeverityBadge(evt.severity)}
                                            </td>
                                            <td style={{ padding: '7px 10px', fontFamily: 'monospace', fontWeight: 700, color: '#38bdf8', fontSize: 11 }}>
                                                {seqStr}
                                            </td>
                                            <td style={{ padding: '7px 12px', fontFamily: 'monospace', fontSize: 11 }}>
                                                {evt.simCalendarTime}
                                            </td>
                                            <td style={{ padding: '7px 10px', fontWeight: 700, color: textMuted, fontSize: 11 }}>
                                                {evt.tick}
                                            </td>
                                            <td style={{ padding: '7px 10px', fontSize: 11, fontWeight: 700, color: isDark ? '#e2e8f0' : '#1e293b' }}>
                                                {localizedType}
                                            </td>
                                            <td style={{ padding: '7px 12px', fontSize: 11, fontWeight: 600 }}>
                                                {evt.source}
                                            </td>
                                            <td style={{ padding: '7px 12px', color: textMain }}>
                                                <span>{evt.message}</span>
                                                {evt.metadata && Object.keys(evt.metadata).length > 0 && (
                                                    <span style={{ marginLeft: 8, fontSize: 10, color: '#38bdf8', fontFamily: 'monospace' }}>
                                                        [{Object.entries(evt.metadata).map(([k, v]) => `${k}: ${v}`).join(', ')}]
                                                    </span>
                                                )}
                                            </td>
                                        </tr>
                                    )
                                })
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Event Detail Drawer if row selected (1:1 with EventLogPane.java line 680) */}
                {selectedEvent && (
                    <div style={{
                        padding: '14px 18px',
                        borderTop: `2px solid #38bdf8`,
                        background: inputBg,
                        fontSize: 11,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 8
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontWeight: 800, color: '#38bdf8', fontSize: 12, display: 'flex', alignItems: 'center', gap: 6 }}>
                                🔍 {t('eventDetailsTitle', 'Détail de l\'Événement')} - EVT-{String(selectedEvent.sequenceId || 1).padStart(6, '0')} ({t('colTick', 'Tick')} #{selectedEvent.tick} - {selectedEvent.simCalendarTime})
                            </span>
                            <button
                                onClick={() => setSelectedEvent(null)}
                                style={{ background: 'transparent', border: 'none', color: textMuted, cursor: 'pointer', padding: 2 }}
                            >
                                <X size={15} />
                            </button>
                        </div>

                        <div style={{ color: textMain, fontSize: 12 }}>
                            <strong>{t('colMessage', 'Message')} :</strong> {selectedEvent.message}
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 6, background: cardBg, padding: 10, borderRadius: 6, border: `1px solid ${borderCol}` }}>
                            <div><strong>{t('colType', 'Type')} :</strong> <span style={{ color: '#38bdf8', fontWeight: 700 }}>{t(`evt_${selectedEvent.type}`, selectedEvent.type)} ({selectedEvent.type})</span></div>
                            <div><strong>{t('colSeverity', 'Sévérité')} :</strong> {getSeverityBadge(selectedEvent.severity)}</div>
                            <div><strong>{t('colSource', 'Source')} :</strong> <span style={{ color: isDark ? '#e2e8f0' : '#1e293b', fontWeight: 600 }}>{selectedEvent.source}</span></div>
                            <div><strong>{t('colTick', 'Tick')} :</strong> <span style={{ fontFamily: 'monospace' }}>#{selectedEvent.tick}</span></div>
                        </div>

                        {selectedEvent.metadata && Object.keys(selectedEvent.metadata).length > 0 && (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                <strong style={{ color: textMuted }}>📊 {t('colMetadata', 'Données & Paramètres Associés')} :</strong>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                                    {Object.entries(selectedEvent.metadata).map(([k, v]) => (
                                        <div key={k} style={{ background: cardBg, border: `1px solid ${borderCol}`, padding: '3px 8px', borderRadius: 4, fontSize: 10.5, fontFamily: 'monospace' }}>
                                            <span style={{ color: textMuted }}>{k} : </span>
                                            <span style={{ color: '#38bdf8', fontWeight: 700 }}>{String(v)}</span>
                                        </div>
                                    ))}
                                </div>
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
                        {t('eventsCount', 'Événements affichés :')} <strong>{filteredEvents.length}</strong> / {eventsLog.length} total
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <FileText size={12} /> {t('eventStorageNotice', 'Stockage journal : mémoire locale (1000 événements max)')}
                    </span>
                </div>
            </div>
        </div>
    )
}
