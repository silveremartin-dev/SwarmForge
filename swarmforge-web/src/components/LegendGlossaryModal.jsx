import React, { useState } from 'react'
import { BookOpen, X, Search, Layers, Crown, HardHat, Shield, Sparkles } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

import { GLOSSARY_DATA } from '../data/glossaryData'

export default function LegendGlossaryModal({ isOpen, onClose }) {
    const { theme, language } = useSimulationStore()
    const isDark = theme === 'dark'
    const [activeTab, setActiveTab] = useState('legend')
    const [searchQuery, setSearchQuery] = useState('')

    if (!isOpen) return null

    const langData = GLOSSARY_DATA[language] || GLOSSARY_DATA.fr

    return (
        <div style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0, 0, 0, 0.65)',
            backdropFilter: 'blur(6px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: 20
        }}>
            <div style={{
                width: '100%',
                maxWidth: 750,
                maxHeight: '85vh',
                background: isDark ? '#0f172a' : '#ffffff',
                border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                borderRadius: 12,
                boxShadow: '0 20px 50px rgba(0,0,0,0.5)',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                color: isDark ? '#f1f5f9' : '#0f172a'
            }}>
                {/* Modal Header */}
                <div style={{
                    padding: '16px 20px',
                    borderBottom: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <BookOpen size={20} color="#38bdf8" />
                        <div>
                            <h3 style={{ margin: 0, fontSize: 16, fontWeight: 800, color: '#38bdf8' }}>
                                {langData.modalTitle}
                            </h3>
                            <div style={{ fontSize: 11, color: isDark ? '#94a3b8' : '#64748b' }}>
                                {langData.modalSubtitle}
                            </div>
                        </div>
                    </div>

                    <button
                        onClick={onClose}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: isDark ? '#94a3b8' : '#64748b',
                            cursor: 'pointer',
                            padding: 4
                        }}
                    >
                        <X size={18} />
                    </button>
                </div>

                {/* Sub-Tabs */}
                <div style={{
                    display: 'flex',
                    borderBottom: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
                    background: isDark ? '#1e293b' : '#f8fafc',
                    padding: '0 16px'
                }}>
                    <button
                        onClick={() => setActiveTab('legend')}
                        style={{
                            padding: '10px 18px',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: activeTab === 'legend' ? '2px solid #38bdf8' : '2px solid transparent',
                            color: activeTab === 'legend' ? '#38bdf8' : (isDark ? '#94a3b8' : '#64748b'),
                            fontWeight: 700,
                            fontSize: 12,
                            cursor: 'pointer'
                        }}
                    >
                        {langData.tabLegend}
                    </button>
                    <button
                        onClick={() => setActiveTab('glossary')}
                        style={{
                            padding: '10px 18px',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: activeTab === 'glossary' ? '2px solid #38bdf8' : '2px solid transparent',
                            color: activeTab === 'glossary' ? '#38bdf8' : (isDark ? '#94a3b8' : '#64748b'),
                            fontWeight: 700,
                            fontSize: 12,
                            cursor: 'pointer'
                        }}
                    >
                        {langData.tabGlossary}
                    </button>
                </div>

                {/* Body Content */}
                <div style={{ padding: 20, overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: 20 }}>
                    {activeTab === 'legend' ? (
                        <>
                            {/* 1. Geological Substrates */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={15} /> {langData.substratesHeader}
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {langData.substrates.map((sub, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            <div style={{ width: 18, height: 18, borderRadius: 4, background: sub.color, flexShrink: 0, border: '1px solid rgba(255,255,255,0.2)' }} />
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{sub.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{sub.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* 2. Pheromone Gradients (8 Canonical Channels - No English parentheticals, no redundant color text in descriptions) */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#a855f7', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={15} /> {langData.pheromonesHeader}
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {langData.pheromones.map((phero, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            <div style={{ width: 16, height: 16, borderRadius: '50%', background: phero.color, flexShrink: 0, boxShadow: `0 0 8px ${phero.color}` }} />
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{phero.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{phero.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* 3. Castes */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#fbbf24', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Crown size={15} /> {langData.castesHeader}
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {langData.castes.map((caste, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            {i === 0 ? <Crown size={15} color="#fbbf24" /> : (i === 1 ? <HardHat size={15} color="#38bdf8" /> : <Shield size={15} color="#ef4444" />)}
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{caste.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{caste.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </>
                    ) : (
                        <>
                            {/* Search Input */}
                            <div style={{ position: 'relative' }}>
                                <Search size={14} color="#94a3b8" style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)' }} />
                                <input
                                    type="text"
                                    placeholder={langData.searchPlaceholder}
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    style={{
                                        width: '100%',
                                        padding: '8px 12px 8px 32px',
                                        background: isDark ? '#1e293b' : '#f8fafc',
                                        border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                                        borderRadius: 6,
                                        color: isDark ? '#fff' : '#000',
                                        fontSize: 12
                                    }}
                                />
                            </div>

                            {/* Glossary List */}
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                                {langData.categories.map((cat, idx) => {
                                    const filteredItems = cat.items.filter(item =>
                                        item.term.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                        item.definition.toLowerCase().includes(searchQuery.toLowerCase())
                                    )

                                    if (filteredItems.length === 0) return null

                                    return (
                                        <div key={idx}>
                                            <h4 style={{ margin: '0 0 8px', fontSize: 12, fontWeight: 800, color: '#38bdf8' }}>
                                                {cat.title}
                                            </h4>
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                                                {filteredItems.map((item, i) => (
                                                    <div key={i} style={{
                                                        background: isDark ? 'rgba(30,41,59,0.5)' : '#f8fafc',
                                                        padding: 10,
                                                        borderRadius: 6,
                                                        borderLeft: '3px solid #38bdf8'
                                                    }}>
                                                        <div style={{ fontWeight: 800, fontSize: 12, color: isDark ? '#fff' : '#0f172a', marginBottom: 3 }}>
                                                            {item.term}
                                                        </div>
                                                        <div style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#475569', lineHeight: 1.4 }}>
                                                            {item.definition}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )
                                })}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    )
}
