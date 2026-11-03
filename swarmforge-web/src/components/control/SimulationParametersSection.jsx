import React, { useState } from 'react'
import { useSimulationStore } from '../../store/simulationStore'

export default function SimulationParametersSection({ styles }) {
    const { simulationParams, setSimulationParam } = useSimulationStore()
    const [paramCategoryTab, setParamCategoryTab] = useState('pheromones')

    return (
        <div style={styles.selectGroup}>
            <div style={styles.sectionHeader}>
                <span>🧪 Paramètres & Échelles</span>
            </div>

            <div style={{ display: 'flex', gap: 2, background: 'rgba(0,0,0,0.3)', padding: 3, borderRadius: 6, marginBottom: 8 }}>
                {[
                    { id: 'pheromones', label: '🧪 Phéromones' },
                    { id: 'resources', label: '🍎 Ressources' },
                    { id: 'species', label: '👥 Espèces' },
                    { id: 'economics', label: '💼 Économie' },
                    { id: 'glossary', label: '📖 Glossaire' },
                ].map(tab => (
                    <button
                        key={tab.id}
                        onClick={() => setParamCategoryTab(tab.id)}
                        style={{
                            flex: 1,
                            padding: '4px 2px',
                            fontSize: 9,
                            fontWeight: 700,
                            border: paramCategoryTab === tab.id ? '1px solid #38bdf8' : '1px solid transparent',
                            borderRadius: 4,
                            cursor: 'pointer',
                            background: paramCategoryTab === tab.id ? '#1e293b' : 'transparent',
                            color: paramCategoryTab === tab.id ? '#38bdf8' : '#94a3b8',
                            transition: 'all 0.15s ease',
                        }}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {paramCategoryTab !== 'glossary' && simulationParams && simulationParams[paramCategoryTab] && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {Object.entries(simulationParams[paramCategoryTab]).map(([key, param]) => (
                        <div key={key} style={{ background: 'rgba(255,255,255,0.02)', padding: 6, borderRadius: 6, border: '1px solid rgba(255,255,255,0.04)' }}>
                            <div style={{ fontSize: 11, fontWeight: 600, color: '#cbd5e1', display: 'flex', justifyContent: 'space-between', marginBottom: 2 }}>
                                <span>{param.label}</span>
                                <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                                    {param.value} {param.unit}
                                </span>
                            </div>
                            <input
                                type="range"
                                min={param.min}
                                max={param.max}
                                step={param.min < 1 ? 0.01 : 1}
                                value={param.value}
                                onChange={(e) => setSimulationParam(paramCategoryTab, key, e.target.value)}
                                style={styles.slider}
                            />
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: '#64748b', marginTop: 2 }}>
                                <span>Min: {param.min} {param.unit}</span>
                                <span>Max: {param.max} {param.unit}</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {paramCategoryTab === 'glossary' && (
                <div style={{ maxHeight: 200, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 6, paddingRight: 4 }}>
                    {[
                        { term: "🏛️ Bio-Architecture", color: "#38bdf8", definition: "Géométrie des galeries souterraines et strates du terrarium." },
                        { term: "👥 Démographie", color: "#fbbf24", definition: "Espérance de vie naturelle (en jours) et taux de ponte de la reine." },
                        { term: "🧪 Phéromones", color: "#c084fc", definition: "Pistes d'attraction et gradients de diffusion/évaporation des signaux chimiques." },
                        { term: "🌀 Stigmergie", color: "#38bdf8", definition: "Coordination collective via les modifications persistantes de l'environnement." }
                    ].map((item, idx) => (
                        <div key={idx} style={{ background: 'rgba(255,255,255,0.03)', padding: 6, borderRadius: 6, fontSize: 10 }}>
                            <span style={{ color: item.color, fontWeight: 700 }}>{item.term} :</span>
                            <div style={{ color: '#94a3b8', marginTop: 2 }}>{item.definition}</div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}
