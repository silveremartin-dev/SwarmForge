import React, { useState } from 'react'
import { useSimulationStore } from '../../store/simulationStore'
import { showToast } from '../../store/toastStore'
import { Database, Plus, Trash2 } from 'lucide-react'

export default function CheckpointSection({ styles }) {
    const {
        tick,
        checkpoints,
        createCheckpoint,
        restoreCheckpoint,
        deleteCheckpoint,
    } = useSimulationStore()

    const [checkpointInputLabel, setCheckpointInputLabel] = useState('')
    const [showCheckpointModal, setShowCheckpointModal] = useState(false)

    const handleCreateCheckpoint = () => {
        const label = checkpointInputLabel.trim() || `Checkpoint Tick #${tick}`
        createCheckpoint(label)
        setCheckpointInputLabel('')
        setShowCheckpointModal(false)
        showToast(`💾 Checkpoint "${label}" créé avec succès !`, "success")
    }

    const handleRestoreCheckpoint = (id, label) => {
        const ok = restoreCheckpoint(id)
        if (ok) {
            showToast(`⏮️ Simulation restaurée au checkpoint "${label}"`, "info")
        }
    }

    return (
        <>
            <div style={styles.selectGroup}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={styles.sectionHeader}>
                        <Database size={13} className="text-amber-400" />
                        <span>Checkpoints ({checkpoints.length})</span>
                    </div>
                    <button
                        onClick={() => setShowCheckpointModal(true)}
                        style={{ ...styles.iconBtn, flex: 'none', padding: '3px 8px', fontSize: 10, background: '#0284c7', color: '#fff', borderColor: '#0284c7' }}
                        title="Créer un instantané manuel de la simulation"
                    >
                        <Plus size={11} />
                        <span>Nouveau</span>
                    </button>
                </div>

                {checkpoints.length === 0 ? (
                    <div style={{ fontSize: 11, color: '#64748b', fontStyle: 'italic', textAlign: 'center', padding: '6px 0' }}>
                        Aucun checkpoint enregistré. Sauvegardes auto toutes les 25 000 ticks.
                    </div>
                ) : (
                    <div style={{ maxHeight: 150, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 4 }}>
                        {checkpoints.map(c => (
                            <div
                                key={c.id}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    background: 'rgba(255,255,255,0.02)',
                                    border: '1px solid rgba(255,255,255,0.05)',
                                    borderRadius: 6,
                                    padding: '5px 8px',
                                    fontSize: 10
                                }}
                            >
                                <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: 170 }}>
                                    <div style={{ fontWeight: 700, color: '#f59e0b' }}>{c.label}</div>
                                    <div style={{ color: '#64748b', fontSize: 9 }}>Tick #{c.tick} • Pop: {c.antsCount || c.stats?.totalPopulation || 0}</div>
                                </div>
                                <div style={{ display: 'flex', gap: 4 }}>
                                    <button
                                        onClick={() => handleRestoreCheckpoint(c.id, c.label)}
                                        style={{ background: '#1e293b', border: '1px solid #334155', color: '#38bdf8', padding: '3px 6px', borderRadius: 4, cursor: 'pointer', fontSize: 10, fontWeight: 700 }}
                                        title="Restaurer la simulation à cet instant"
                                    >
                                        ⏮️
                                    </button>
                                    <button
                                        onClick={() => deleteCheckpoint(c.id)}
                                        style={{ background: 'transparent', border: 'none', color: '#ef4444', padding: '2px 4px', cursor: 'pointer' }}
                                        title="Supprimer ce checkpoint"
                                    >
                                        <Trash2 size={12} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Modal for saving new Checkpoint */}
            {showCheckpointModal && (
                <div style={{
                    position: 'fixed',
                    top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.7)',
                    zIndex: 2000,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    <div style={{
                        background: '#0f172a',
                        border: '1px solid #334155',
                        borderRadius: 12,
                        padding: 20,
                        width: 320,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 12
                    }}>
                        <div style={{ fontSize: 14, fontWeight: 700, color: '#f59e0b' }}>💾 Créer un Checkpoint (Tick #{tick})</div>
                        <input
                            type="text"
                            placeholder="Nom du Checkpoint (ex: Avant Attaque)"
                            value={checkpointInputLabel}
                            onChange={(e) => setCheckpointInputLabel(e.target.value)}
                            style={{ width: '100%', background: '#1e293b', border: '1px solid #475569', color: '#fff', padding: 8, borderRadius: 6, fontSize: 12 }}
                        />
                        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                            <button onClick={() => setShowCheckpointModal(false)} style={{ padding: '6px 12px', background: '#334155', border: 'none', color: '#fff', borderRadius: 6, fontSize: 12, cursor: 'pointer' }}>Annuler</button>
                            <button onClick={handleCreateCheckpoint} style={{ padding: '6px 12px', background: '#f59e0b', border: 'none', color: '#0f172a', borderRadius: 6, fontSize: 12, fontWeight: 800, cursor: 'pointer' }}>Sauvegarder</button>
                        </div>
                    </div>
                </div>
            )}
        </>
    )
}
