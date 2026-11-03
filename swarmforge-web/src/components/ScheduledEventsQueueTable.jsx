import React from 'react'
import { Copy, Trash2, Play, Pause, Clock } from 'lucide-react'

/**
 * Scheduled events timeline table for God Mode interventions.
 * Supports pausing, resuming, duplicating and deleting scheduled abiotic/biotic interventions.
 */
export function ScheduledEventsQueueTable({
    t,
    cardBg,
    borderCol,
    textMuted,
    ticks,
    scheduledEvents,
    togglePauseScheduledEvent,
    duplicateScheduledEvent,
    removeScheduledEvent
}) {
    return (
        <div style={{
            background: cardBg,
            border: `1px solid ${borderCol}`,
            borderRadius: 10,
            padding: '16px',
            display: 'flex',
            flexDirection: 'column',
            gap: 12
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                    <Clock size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>
                        {t('scheduledEventsTitle', "File d'Événements Programmés")} ({scheduledEvents.length})
                    </span>
                </div>

                <span style={{ fontSize: 11, color: textMuted }}>
                    {t('currentTickLabel', 'Tick actuel :')} <strong>{ticks}</strong>
                </span>
            </div>

            {scheduledEvents.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '24px', color: textMuted, fontSize: 12 }}>
                    {t('noScheduledEventsMsg', "Aucun événement planifié dans la file d'attente.")}
                </div>
            ) : (
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12, textAlign: 'left' }}>
                        <thead>
                            <tr style={{ borderBottom: `2px solid ${borderCol}`, color: textMuted }}>
                                <th style={{ padding: '8px 10px' }}>{t('tickLabel', 'Tick')}</th>
                                <th style={{ padding: '8px 10px' }}>{t('colCalendarTime', 'Heure Calendrier')}</th>
                                <th style={{ padding: '8px 10px' }}>{t('categoryCol', 'Catégorie')}</th>
                                <th style={{ padding: '8px 10px' }}>{t('colTypeAction', 'Type / Action')}</th>
                                <th style={{ padding: '8px 10px' }}>{t('colTarget', 'Cible')}</th>
                                <th style={{ padding: '8px 10px' }}>{t('colDescription', 'Description')}</th>
                                <th style={{ padding: '8px 10px' }}>{t('colStatus', 'Statut')}</th>
                                <th style={{ padding: '8px 10px', textAlign: 'right' }}>{t('colActions', 'Actions')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {(scheduledEvents || []).map(evt => {
                                const isExecuted = evt.executed
                                const isPaused = evt.paused
                                return (
                                    <tr key={evt.id} style={{
                                        borderBottom: `1px solid ${borderCol}`,
                                        opacity: isExecuted ? 0.5 : 1.0,
                                        background: isExecuted ? 'transparent' : (isPaused ? 'rgba(239, 68, 68, 0.05)' : 'transparent')
                                    }}>
                                        <td style={{ padding: '8px 10px', fontWeight: 700, color: '#38bdf8' }}>
                                            {evt.targetTick}
                                        </td>
                                        <td style={{ padding: '8px 10px', fontFamily: 'monospace' }}>
                                            {evt.scheduledCalendarTime || '2026-03-20 08:00:00'}
                                        </td>
                                        <td style={{ padding: '8px 10px' }}>
                                            <span style={{
                                                background: evt.category === 'DISASTER' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(56, 189, 248, 0.2)',
                                                color: evt.category === 'DISASTER' ? '#ef4444' : '#38bdf8',
                                                padding: '2px 6px',
                                                borderRadius: 4,
                                                fontSize: 10,
                                                fontWeight: 800
                                            }}>
                                                {evt.category}
                                            </span>
                                        </td>
                                        <td style={{ padding: '8px 10px', fontWeight: 600 }}>{evt.eventType}</td>
                                        <td style={{ padding: '8px 10px', color: textMuted }}>{evt.colonyTarget}</td>
                                        <td style={{ padding: '8px 10px', maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {evt.description}
                                        </td>
                                        <td style={{ padding: '8px 10px' }}>
                                            {isExecuted ? (
                                                <span style={{ color: '#10b981', fontWeight: 700, fontSize: 11 }}>{t('statusExecuted', '✓ Exécuté')}</span>
                                            ) : isPaused ? (
                                                <span style={{ color: '#ef4444', fontWeight: 700, fontSize: 11 }}>{t('statusPaused', '⏸ Suspendu')}</span>
                                            ) : (
                                                <span style={{ color: '#f59e0b', fontWeight: 700, fontSize: 11 }}>{t('statusPending', '⏳ En attente')}</span>
                                            )}
                                        </td>
                                        <td style={{ padding: '8px 10px', textAlign: 'right' }}>
                                            <div style={{ display: 'inline-flex', gap: 6 }}>
                                                {!isExecuted && (
                                                    <button
                                                        onClick={() => togglePauseScheduledEvent(evt.id)}
                                                        title={isPaused ? t('btnResumeEvent', 'Reprendre') : t('btnPauseEvent', 'Suspendre')}
                                                        style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: textMuted }}
                                                    >
                                                        {isPaused ? <Play size={14} color="#10b981" /> : <Pause size={14} color="#ef4444" />}
                                                    </button>
                                                )}
                                                <button
                                                    onClick={() => duplicateScheduledEvent(evt.id, 100)}
                                                    title={t('btnDuplicate100', 'Dupliquer (+100 ticks)')}
                                                    style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: textMuted }}
                                                >
                                                    <Copy size={14} color="#38bdf8" />
                                                </button>
                                                <button
                                                    onClick={() => {
                                                        if (window.confirm(t('confirmDeleteEvent', 'Confirmer la suppression de cet événement ?'))) {
                                                             removeScheduledEvent(evt.id)
                                                        }
                                                    }}
                                                    title={t('btnDeleteEvent', "Supprimer l'événement")}
                                                    style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#ef4444' }}
                                                >
                                                    <Trash2 size={14} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                )
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}
