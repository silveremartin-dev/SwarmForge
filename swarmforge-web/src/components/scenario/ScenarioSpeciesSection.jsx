import React from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { useSimulationStore } from '../../store/simulationStore'
import { DEFAULT_SPECIES_PRESETS, DEFAULT_NEST_PRESETS } from '../../store/presetStore'
import { getTranslation } from '../../i18n/translations'

/**
 * Multi-species ecosystem configuration section with caste demography controls.
 */
export default function ScenarioSpeciesSection({ cardBg, borderCol, inputBg, textMain, textMuted }) {
    const {
        speciesCards,
        addSpeciesCard,
        removeSpeciesCard,
        updateSpeciesCard,
        language
    } = useSimulationStore()

    const t = (key, fallback) => getTranslation(language, key, fallback)

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: 12,
            borderTop: `1px solid ${borderCol}`,
            paddingTop: 16,
            marginTop: 8
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: 13, fontWeight: 800, color: '#38bdf8' }}>
                    {t('ecosystemColoniesTitle', 'Colonies & Castes IA de l\'Écosystème')} ({speciesCards.length} colonies)
                </span>

                <button
                    onClick={() => addSpeciesCard()}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 6,
                        background: '#0284c7',
                        color: '#ffffff',
                        border: 'none',
                        padding: '6px 14px',
                        borderRadius: 6,
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: 'pointer'
                    }}
                >
                    <Plus size={13} /> {t('btnAddColony', '＋ Ajouter une Colonie')}
                </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 14 }}>
                {speciesCards.map((card) => (
                    <div
                        key={card.id}
                        style={{
                            background: cardBg,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 8,
                            padding: '12px 14px',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: 8,
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                        }}
                    >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <div style={{ width: 12, height: 12, borderRadius: '50%', background: card.color || '#10b981' }} />
                                <input
                                    type="text"
                                    value={card.name}
                                    onChange={(e) => updateSpeciesCard(card.id, { name: e.target.value })}
                                    style={{
                                        background: 'transparent',
                                        color: textMain,
                                        border: 'none',
                                        borderBottom: `1px solid ${borderCol}`,
                                        fontSize: 12,
                                        fontWeight: 800,
                                        padding: '2px 4px'
                                    }}
                                />
                            </div>
                            {speciesCards.length > 1 && (
                                <button
                                    onClick={() => {
                                        if (window.confirm(t('confirmDeleteColony', `Confirmer la suppression de la colonie "${card.name}" ?`))) {
                                            removeSpeciesCard(card.id)
                                        }
                                    }}
                                    title={t('btnDeleteColonyTt', 'Supprimer cette colonie du scénario (confirmation requise)')}
                                    style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', padding: 2 }}
                                >
                                    <Trash2 size={13} />
                                </button>
                            )}
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <span style={{ fontSize: 10, color: textMuted }}>{t('speciesCardLabel', 'Espèce :')}</span>
                            <select
                                value={card.speciesId}
                                onChange={(e) => updateSpeciesCard(card.id, { speciesId: e.target.value })}
                                style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 6px', fontSize: 11 }}
                            >
                                {DEFAULT_SPECIES_PRESETS.map(s => (
                                    <option key={s.id} value={s.id}>{s.name} ({s.latinName})</option>
                                ))}
                            </select>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <span style={{ fontSize: 10, color: textMuted }}>{t('nestTypeCardLabel', 'Type de Nid :')}</span>
                            <select
                                value={card.nestType}
                                onChange={(e) => updateSpeciesCard(card.id, { nestType: e.target.value })}
                                style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 6px', fontSize: 11 }}
                            >
                                {DEFAULT_NEST_PRESETS.map(n => (
                                    <option key={n.id} value={n.nestType}>{n.name}</option>
                                ))}
                            </select>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 6, paddingTop: 4 }}>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <span style={{ fontSize: 9, color: textMuted }}>{t('queensCardLabel', 'Reines')}</span>
                                <input
                                    type="number"
                                    min="0"
                                    value={card.initialQueens ?? 1}
                                    onChange={(e) => updateSpeciesCard(card.id, { initialQueens: parseInt(e.target.value) || 0 })}
                                    style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                />
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <span style={{ fontSize: 9, color: textMuted }}>{t('workersCardLabel', 'Ouvrières')}</span>
                                <input
                                    type="number"
                                    min="0"
                                    value={card.initialWorkers ?? 40}
                                    onChange={(e) => updateSpeciesCard(card.id, { initialWorkers: parseInt(e.target.value) || 0 })}
                                    style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                />
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <span style={{ fontSize: 9, color: textMuted }}>{t('soldiersCardLabel', 'Soldats')}</span>
                                <input
                                    type="number"
                                    min="0"
                                    value={card.initialSoldiers ?? 10}
                                    onChange={(e) => updateSpeciesCard(card.id, { initialSoldiers: parseInt(e.target.value) || 0 })}
                                    style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                />
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                <span style={{ fontSize: 9, color: textMuted }}>{t('malesCardLabel', 'Mâles')}</span>
                                <input
                                    type="number"
                                    min="0"
                                    value={card.initialMales ?? 0}
                                    onChange={(e) => updateSpeciesCard(card.id, { initialMales: parseInt(e.target.value) || 0 })}
                                    style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                />
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}
