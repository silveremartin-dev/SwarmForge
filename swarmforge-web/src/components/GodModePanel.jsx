import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { showToast } from '../store/toastStore'
import { Zap, Plus, Skull, Apple, ShieldAlert, Sparkles, Download, Trash2, History, Layers } from 'lucide-react'

export default function GodModePanel() {
    const {
        colonies,
        tick,
        running,
        interventionsLog,
        recordDivineIntervention,
        clearInterventionsLog,
        addColony,
        removeColony,
        environment,
    } = useSimulationStore()

    const terrariumWidth = environment?.terrariumWidth || 2.0
    const terrariumDepth = environment?.terrariumDepth || 2.0
    const terrariumHeight = environment?.terrariumHeight || 1.0

    const [selectedColonyId, setSelectedColonyId] = useState(colonies[0]?.id || 'COLONY_1')
    const [foodQuantity, setFoodQuantity] = useState(150)
    const [foodType, setFoodType] = useState('SUGAR_NECTAR')
    const [antCountToAdd, setAntCountToAdd] = useState(20)
    const [antCasteToAdd, setAntCasteToAdd] = useState('WORKER')
    const [predatorType, setPredatorType] = useState('SPIDER')
    const [newColonyName, setNewColonyName] = useState('')
    const [newColonySpecies, setNewColonySpecies] = useState('Linepithema humile')
    const [showNewColonyModal, setShowNewColonyModal] = useState(false)
    const [activeTab, setActiveTab] = useState('actions') // 'actions' | 'timeline' | 'colonies'

    // Explicit Metric Coordinates [X, Y, Z in meters - Dynamically synchronized with World Editor]
    const [posX, setPosX] = useState(terrariumWidth / 2)
    const [posY, setPosY] = useState(terrariumDepth / 2)
    const [posZ, setPosZ] = useState(0.1)

    const isSimActive = running || tick > 0

    const handleSpawnFood = () => {
        if (!isSimActive) {
            showToast('⚠️ Veuillez dabord lancer la simulation depuis le Gestionnaire de Simulation !', 'error')
            return
        }
        recordDivineIntervention({
            type: 'SPAWN_FOOD',
            actionName: `🍎 Dépôt Divin de Nourriture (${foodQuantity}u)`,
            foodType,
            quantity: foodQuantity,
            x: posX, y: posY, z: posZ,
            targetColonyId: selectedColonyId,
        })
        showToast(`✨ Dépôt de ${foodQuantity}u de ${foodType} aux coordonnées (${posX}m, ${posY}m, ${posZ}m)`, 'success')
    }

    const handleAddAnts = () => {
        if (!isSimActive) {
            showToast('⚠️ Veuillez dabord lancer la simulation depuis le Gestionnaire de Simulation !', 'error')
            return
        }
        const targetCol = colonies.find(c => c.id === selectedColonyId) || colonies[0]
        recordDivineIntervention({
            type: 'ADD_ANTS',
            actionName: `🐜 Injection Divinement de ${antCountToAdd} ${antCasteToAdd}s`,
            caste: antCasteToAdd,
            count: antCountToAdd,
            x: posX, y: posY, z: posZ,
            targetColonyId: selectedColonyId,
            colonyName: targetCol?.name,
        })
        showToast(`✨ ${antCountToAdd} ${antCasteToAdd}s injectées dans ${targetCol?.name || 'la colonie'} à (${posX}m, ${posY}m)`, 'success')
    }

    const handleSpawnPredator = () => {
        if (!isSimActive) {
            showToast('⚠️ Veuillez dabord lancer la simulation depuis le Gestionnaire de Simulation !', 'error')
            return
        }
        recordDivineIntervention({
            type: 'SPAWN_PREDATOR',
            actionName: `🕸️ Invocateur de Prédateur (${predatorType})`,
            predatorType,
            x: posX, y: posY, z: posZ,
        })
        showToast(`⚠️ Prédateur ${predatorType} invoqué à (${posX}m, ${posY}m, ${posZ}m) !`, 'warning')
    }

    const handleDivineStrike = () => {
        if (!isSimActive) {
            showToast('⚠️ Veuillez dabord lancer la simulation depuis le Gestionnaire de Simulation !', 'error')
            return
        }
        const targetCol = colonies.find(c => c.id === selectedColonyId)
        recordDivineIntervention({
            type: 'DIVINE_STRIKE',
            actionName: `⚡ Fléau Divin sur ${targetCol?.name || 'le terrain'}`,
            targetColonyId: selectedColonyId,
        })
        showToast(`⚡ Fléau divin déclenché à Tick #${tick} ! Interventions loguées pour replay.`, 'error')
    }

    const handleCreateColony = () => {
        if (!newColonyName.trim()) return
        const colors = ['#38bdf8', '#f43f5e', '#a855f7', '#fbbf24', '#4ade80', '#ec4899']
        const randomColor = colors[colonies.length % colors.length]
        const newCol = {
            id: `COLONY_${Date.now()}`,
            name: newColonyName.trim(),
            species: newColonySpecies,
            color: randomColor,
            foodStored: 100,
            queenCount: 1,
            workerCount: 50,
        }
        addColony(newCol)
        setSelectedColonyId(newCol.id)
        recordDivineIntervention({
            type: 'CREATE_COLONY',
            actionName: `🏛️ Fondation Divinement de ${newCol.name} (${newCol.species})`,
            colonyId: newCol.id,
            colonyName: newCol.name,
            species: newCol.species,
        })
        setNewColonyName('')
        setShowNewColonyModal(false)
        showToast(`🏛️ Nouvelle colonie "${newCol.name}" créée et synchronisée !`, 'success')
    }

    const handleExportLog = () => {
        const jsonStr = JSON.stringify(interventionsLog, null, 2)
        const blob = new Blob([jsonStr], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `god_mode_interventions_replay_${new Date().toISOString().slice(0, 10)}.json`
        a.click()
        showToast('📥 Journal des Interventions Divines exporté pour Replay !', 'success')
    }

    const styles = {
        container: {
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(16px)',
            border: '1px solid rgba(168, 85, 247, 0.3)',
            borderRadius: 12,
            padding: 14,
            color: '#f8fafc',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            boxShadow: '0 10px 30px rgba(0,0,0,0.6)',
            maxHeight: 'calc(100vh - 140px)',
            overflowY: 'auto',
        },
        header: {
            fontSize: 14,
            fontWeight: 800,
            color: '#c084fc',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: 10,
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
            paddingBottom: 6,
        },
        tabs: {
            display: 'flex',
            gap: 4,
            marginBottom: 10,
            background: 'rgba(0, 0, 0, 0.3)',
            padding: 3,
            borderRadius: 6,
        },
        tabBtn: (active) => ({
            flex: 1,
            padding: '5px 8px',
            fontSize: 10,
            fontWeight: 700,
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer',
            background: active ? '#a855f7' : 'transparent',
            color: active ? '#ffffff' : '#94a3b8',
            transition: 'all 0.15s ease',
        }),
        section: {
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 255, 255, 0.06)',
            borderRadius: 8,
            padding: 10,
            marginBottom: 10,
        },
        label: {
            fontSize: 11,
            fontWeight: 600,
            color: '#cbd5e1',
            marginBottom: 4,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
        },
        select: {
            width: '100%',
            background: '#0f172a',
            border: '1px solid #475569',
            color: '#c084fc',
            padding: '5px 8px',
            borderRadius: 6,
            fontSize: 11,
            fontWeight: 700,
            outline: 'none',
        },
        actionBtn: (bgColor) => ({
            width: '100%',
            padding: '7px 10px',
            fontSize: 11,
            fontWeight: 700,
            border: 'none',
            borderRadius: 6,
            background: bgColor,
            color: '#ffffff',
            cursor: isSimActive ? 'pointer' : 'not-allowed',
            opacity: isSimActive ? 1 : 0.6,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 6,
            boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
            marginTop: 6,
            transition: 'all 0.15s ease',
        }),
    }

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Sparkles size={16} className="text-purple-400" />
                    <span>👑 Mode Divin & Interventions</span>
                </div>
                <span style={{ fontSize: 10, background: '#a855f7', color: '#fff', padding: '2px 6px', borderRadius: 4 }}>
                    {interventionsLog.length} Actes Rejouables
                </span>
            </div>

            {!isSimActive && (
                <div style={{ background: 'rgba(245, 158, 11, 0.15)', border: '1px solid #f59e0b', borderRadius: 8, padding: '8px 10px', fontSize: 11, color: '#fbbf24', marginBottom: 10, lineHeight: 1.4 }}>
                    ⚠️ <strong>Mode Divin en attente :</strong> Veuillez peupler et lancer la simulation (<em>"LANCER SIMULATION"</em>) dans le Gestionnaire de Simulation pour agir sur le monde.
                </div>
            )}

            {/* Navigation Sub-Tabs */}
            <div style={styles.tabs}>
                <button style={styles.tabBtn(activeTab === 'actions')} onClick={() => setActiveTab('actions')}>
                    ⚡ Actions Divines
                </button>
                <button style={styles.tabBtn(activeTab === 'colonies')} onClick={() => setActiveTab('colonies')}>
                    🏛️ Colonies ({colonies.length})
                </button>
                <button style={styles.tabBtn(activeTab === 'timeline')} onClick={() => setActiveTab('timeline')}>
                    📜 Journal ({interventionsLog.length})
                </button>
            </div>

            {/* TAB 1: DIVINE ACTIONS */}
            {activeTab === 'actions' && (
                <>
                    {/* EXPLICIT METRIC COORDINATES (TERRARIUM SYNCHRONIZED) */}
                    <div style={styles.section}>
                        <div style={styles.label}>
                            <span>📍 Coordonnées d'Intervention (m) :</span>
                            <span style={{ color: '#38bdf8', fontSize: 10 }}>Terrarium: {terrariumWidth}m × {terrariumDepth}m × {terrariumHeight}m</span>
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 6 }}>
                            <div>
                                <label style={{ fontSize: 9, color: '#94a3b8' }}>X (m) :</label>
                                <input
                                    type="number" min="0" max={terrariumWidth} step="0.5"
                                    value={posX} onChange={(e) => setPosX(parseFloat(e.target.value) || 0)}
                                    style={{ width: '100%', background: '#0f172a', border: '1px solid #475569', color: '#38bdf8', padding: '4px 6px', borderRadius: 6, fontSize: 11, fontWeight: 700 }}
                                />
                            </div>
                            <div>
                                <label style={{ fontSize: 9, color: '#94a3b8' }}>Y (m) :</label>
                                <input
                                    type="number" min="0" max={terrariumDepth} step="0.5"
                                    value={posY} onChange={(e) => setPosY(parseFloat(e.target.value) || 0)}
                                    style={{ width: '100%', background: '#0f172a', border: '1px solid #475569', color: '#38bdf8', padding: '4px 6px', borderRadius: 6, fontSize: 11, fontWeight: 700 }}
                                />
                            </div>
                            <div>
                                <label style={{ fontSize: 9, color: '#94a3b8' }}>Z (m) :</label>
                                <input
                                    type="number" min="0" max={terrariumHeight} step="0.1"
                                    value={posZ} onChange={(e) => setPosZ(parseFloat(e.target.value) || 0)}
                                    style={{ width: '100%', background: '#0f172a', border: '1px solid #475569', color: '#38bdf8', padding: '4px 6px', borderRadius: 6, fontSize: 11, fontWeight: 700 }}
                                />
                            </div>
                        </div>
                    </div>
                    {/* DYNAMIC COLONY SELECTOR (SYNCHRONIZED WITH ALL ENGINE COLONIES) */}
                    <div style={styles.section}>
                        <div style={styles.label}>
                            <span>🏛️ Colonie Cible / Sélectionnée :</span>
                            <span style={{ color: '#c084fc', fontSize: 10 }}>Sync En Temps Réel</span>
                        </div>
                        <select
                            value={selectedColonyId}
                            onChange={(e) => setSelectedColonyId(e.target.value)}
                            style={styles.select}
                        >
                            {colonies.map((col) => (
                                <option key={col.id} value={col.id}>
                                    ● {col.name} ({col.species})
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* SPAWN FOOD DIVINE ACTION */}
                    <div style={styles.section}>
                        <div style={styles.label}>
                            <span>🍎 Créer / Déposer de la Nourriture</span>
                            <span style={{ color: '#4ade80' }}>{foodQuantity}u</span>
                        </div>
                        <div style={{ display: 'flex', gap: 6, marginBottom: 6 }}>
                            <select
                                value={foodType}
                                onChange={(e) => setFoodType(e.target.value)}
                                style={{ ...styles.select, flex: 1 }}
                            >
                                <option value="SUGAR_NECTAR">🍯 Nectar Sucré</option>
                                <option value="SEEDS">🌾 Graines & Graminées</option>
                                <option value="INSECT_MEAT">🥩 Proie / Viande</option>
                            </select>
                            <input
                                type="number"
                                min="10"
                                max="1000"
                                step="10"
                                value={foodQuantity}
                                onChange={(e) => setFoodQuantity(parseInt(e.target.value) || 50)}
                                style={{ width: 60, background: '#0f172a', border: '1px solid #475569', color: '#fff', padding: '4px 6px', borderRadius: 6, fontSize: 11 }}
                            />
                        </div>
                        <button
                            style={styles.actionBtn('linear-gradient(135deg, #16a34a 0%, #15803d 100%)')}
                            onClick={handleSpawnFood}
                        >
                            <Apple size={13} />
                            <span>Poser la Nourriture (Tick #{tick})</span>
                        </button>
                    </div>

                    {/* ADD ANTS DIVINE ACTION */}
                    <div style={styles.section}>
                        <div style={styles.label}>
                            <span>🐜 Créer / Injecter des Fourmis</span>
                            <span style={{ color: '#38bdf8' }}>+{antCountToAdd}</span>
                        </div>
                        <div style={{ display: 'flex', gap: 6, marginBottom: 6 }}>
                            <select
                                value={antCasteToAdd}
                                onChange={(e) => setAntCasteToAdd(e.target.value)}
                                style={{ ...styles.select, flex: 1 }}
                            >
                                <option value="WORKER">👷 Ouvrière</option>
                                <option value="SOLDIER">🛡️ Soldat</option>
                                <option value="QUEEN">👑 Reine Fondatrice</option>
                            </select>
                            <input
                                type="number"
                                min="1"
                                max="100"
                                value={antCountToAdd}
                                onChange={(e) => setAntCountToAdd(parseInt(e.target.value) || 10)}
                                style={{ width: 60, background: '#0f172a', border: '1px solid #475569', color: '#fff', padding: '4px 6px', borderRadius: 6, fontSize: 11 }}
                            />
                        </div>
                        <button
                            style={styles.actionBtn('linear-gradient(135deg, #0284c7 0%, #0369a1 100%)')}
                            onClick={handleAddAnts}
                        >
                            <Plus size={13} />
                            <span>Injecter les Fourmis à la Colonie</span>
                        </button>
                    </div>

                    {/* SPAWN PREDATOR & DIVINE STRIKE */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
                        <button
                            style={styles.actionBtn('linear-gradient(135deg, #d97706 0%, #b45309 100%)')}
                            onClick={handleSpawnPredator}
                        >
                            <ShieldAlert size={13} />
                            <span>Invoquer Prédateur</span>
                        </button>

                        <button
                            style={styles.actionBtn('linear-gradient(135deg, #dc2626 0%, #991b1b 100%)')}
                            onClick={handleDivineStrike}
                        >
                            <Skull size={13} />
                            <span>Fléau Divin</span>
                        </button>
                    </div>
                </>
            )}

            {/* TAB 2: COLONIES DYNAMIC LIST & CREATION */}
            {activeTab === 'colonies' && (
                <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: '#c084fc' }}>Liste des Colonies Actives ({colonies.length})</span>
                        <button
                            onClick={() => setShowNewColonyModal(true)}
                            style={{ padding: '4px 8px', background: '#a855f7', border: 'none', color: '#fff', borderRadius: 4, fontSize: 10, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                        >
                            <Plus size={12} />
                            <span>Nouvelle Colonie</span>
                        </button>
                    </div>

                    <div style={{ maxHeight: 180, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 6 }}>
                        {colonies.map((col) => (
                            <div
                                key={col.id}
                                style={{
                                    background: 'rgba(255, 255, 255, 0.04)',
                                    border: '1px solid ' + (selectedColonyId === col.id ? col.color : 'rgba(255, 255, 255, 0.08)'),
                                    borderRadius: 6,
                                    padding: 8,
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                }}
                            >
                                <div>
                                    <div style={{ fontSize: 11, fontWeight: 700, color: col.color || '#fff' }}>
                                        ● {col.name}
                                    </div>
                                    <div style={{ fontSize: 9, color: '#94a3b8' }}>
                                        {col.species} | Stock: {col.foodStored || 0}u | Ouvrières: {col.workerCount || 0}
                                    </div>
                                </div>

                                <div style={{ display: 'flex', gap: 4 }}>
                                    <button
                                        onClick={() => setSelectedColonyId(col.id)}
                                        style={{ padding: '2px 6px', background: '#334155', border: 'none', color: '#fff', borderRadius: 4, fontSize: 9, cursor: 'pointer' }}
                                    >
                                        Sélectionner
                                    </button>
                                    {colonies.length > 1 && (
                                        <button
                                            onClick={() => removeColony(col.id)}
                                            style={{ padding: '2px 4px', background: 'rgba(239, 68, 68, 0.2)', border: '1px solid #ef4444', color: '#f87171', borderRadius: 4, fontSize: 9, cursor: 'pointer' }}
                                            title="Supprimer la colonie"
                                        >
                                            <Trash2 size={10} />
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* TAB 3: TIMELINE INTERVENTIONS LOG (REPLAY SYSTEM) */}
            {activeTab === 'timeline' && (
                <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                        <span style={{ fontSize: 11, fontWeight: 700, color: '#c084fc' }}>Journal des Interventions Divines</span>
                        <div style={{ display: 'flex', gap: 4 }}>
                            <button
                                onClick={handleExportLog}
                                style={{ padding: '3px 6px', background: '#0284c7', border: 'none', color: '#fff', borderRadius: 4, fontSize: 9, fontWeight: 700, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 3 }}
                                title="Exporter les interventions en JSON pour rejouer la simulation"
                            >
                                <Download size={10} />
                                <span>JSON Replay</span>
                            </button>
                            <button
                                onClick={clearInterventionsLog}
                                style={{ padding: '3px 6px', background: '#334155', border: 'none', color: '#94a3b8', borderRadius: 4, fontSize: 9, cursor: 'pointer' }}
                                title="Effacer le journal"
                            >
                                Effacer
                            </button>
                        </div>
                    </div>

                    {interventionsLog.length === 0 ? (
                        <div style={{ fontSize: 10, color: '#64748b', fontStyle: 'italic', textAlign: 'center', padding: 12 }}>
                            Aucune intervention divinement enregistrée à ce jour. Effectuez des actions divines pour qu'elles soient consignées et rejouables déterministement !
                        </div>
                    ) : (
                        <div style={{ maxHeight: 180, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 4 }}>
                            {interventionsLog.map((log) => (
                                <div
                                    key={log.id}
                                    style={{
                                        background: 'rgba(0, 0, 0, 0.4)',
                                        borderLeft: '3px solid #a855f7',
                                        padding: '4px 8px',
                                        borderRadius: '0 4px 4px 0',
                                        fontSize: 10,
                                    }}
                                >
                                    <div style={{ display: 'flex', justifyContent: 'space-between', color: '#c084fc', fontWeight: 700 }}>
                                        <span>{log.actionName}</span>
                                        <span style={{ color: '#f59e0b', fontFamily: 'monospace' }}>Tick #{log.tick}</span>
                                    </div>
                                    <div style={{ fontSize: 9, color: '#94a3b8', marginTop: 2 }}>
                                        Target: {log.targetColonyId || 'Global'} | Horodateur: {new Date(log.timestamp).toLocaleTimeString()}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Modal for creating a New Colony */}
            {showNewColonyModal && (
                <div style={{
                    position: 'fixed',
                    top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.7)',
                    zIndex: 3000,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    <div style={{
                        background: '#0f172a',
                        border: '1px solid #a855f7',
                        borderRadius: 12,
                        padding: 16,
                        width: 300,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 10
                    }}>
                        <div style={{ fontSize: 13, fontWeight: 700, color: '#c084fc' }}>➕ Ajouter une Nouvelle Colonie</div>
                        <input
                            type="text"
                            placeholder="Nom de la Colonie (ex: Colonie #3 Supérieure)"
                            value={newColonyName}
                            onChange={(e) => setNewColonyName(e.target.value)}
                            style={{ width: '100%', background: '#1e293b', border: '1px solid #475569', color: '#fff', padding: 6, borderRadius: 6, fontSize: 11 }}
                        />
                        <select
                            value={newColonySpecies}
                            onChange={(e) => setNewColonySpecies(e.target.value)}
                            style={{ width: '100%', background: '#1e293b', border: '1px solid #475569', color: '#38bdf8', padding: 6, borderRadius: 6, fontSize: 11 }}
                        >
                            <option value="Formica fusca">Formica fusca (Fourmi Noire)</option>
                            <option value="Messor barbarus">Messor barbarus (Fourmi Moissonneuse)</option>
                            <option value="Linepithema humile">Linepithema humile (Fourmi d'Argentine)</option>
                            <option value="Crematogaster scutellaris">Crematogaster scutellaris (Arboricole)</option>
                        </select>
                        <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end', marginTop: 4 }}>
                            <button onClick={() => setShowNewColonyModal(false)} style={{ padding: '5px 10px', background: '#334155', border: 'none', color: '#fff', borderRadius: 6, fontSize: 11, cursor: 'pointer' }}>Annuler</button>
                            <button onClick={handleCreateColony} style={{ padding: '5px 10px', background: '#a855f7', border: 'none', color: '#fff', borderRadius: 6, fontSize: 11, fontWeight: 700, cursor: 'pointer' }}>Créer Colonie</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
