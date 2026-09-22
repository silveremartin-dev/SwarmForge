import React, { useState } from 'react'
import { BookOpen, X, Search, Layers, Compass, HelpCircle, Shield, Crown, HardHat, Sparkles } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'

const GLOSSARY_CATEGORIES = [
    {
        id: 'stigmergy',
        title: 'Stigmergie & Auto-organisation',
        items: [
            {
                term: 'Stigmergie',
                definition: 'Mécanisme de coordination indirecte entre insectes sociaux où la trace laissée dans l\'environnement par une action stimule l\'action suivante.'
            },
            {
                term: 'Trophallaxie',
                definition: 'Transfert direct de nourriture liquide régurgitée de jabot à jabot entre ouvrières ou de l\'ouvrière vers la reine et les larves.'
            },
            {
                term: 'Polyéthisme temporel',
                definition: 'Changement prévisible des fonctions et des tâches d\'un individu au cours de son cycle de vie (de nourrice à nettoyeuse, puis bâtisseuse, puis fourrageuse).'
            },
            {
                term: 'Quorum Sensing',
                definition: 'Seuil critique de densité ou de phéromones requis pour qu\'un comportement collectif bascule (ex: décision d\'attaque massive ou déménagement de nid).'
            }
        ]
    },
    {
        id: 'morphology',
        title: 'Castes & Écologie Comportementale',
        items: [
            {
                term: 'Reine (Gynomorphe)',
                definition: 'Individu femelle fertile assurant la ponte continue des œufs. Produit la phéromone royale inhibant la reproduction des ouvrières.'
            },
            {
                term: 'Ouvrière Minor / Médian',
                definition: 'Castes spécialisées dans le soin au couvain (nurse), l\'excavation souterraine (builder) et la prospection de surface (forager).'
            },
            {
                term: 'Soldat (Major)',
                definition: 'Individu doté d\'une capsule céphalique hyper-développée et de mandibules puissantes, dédié à la défense territoriale et au broyage de graines coriaces.'
            },
            {
                term: 'Nécrophorèse',
                definition: 'Comportement d\'évacuation des cadavres d\'ouvrières hors du nid guidé par la libération d\'acide oléique, évitant la prolifération de pathogènes.'
            }
        ]
    },
    {
        id: 'pheromones',
        title: 'Chimio-réception & Pistes Chimiques',
        items: [
            {
                term: 'Phéromone de Piste (Trail)',
                definition: 'Substance volatile déposée sur le substrat reliant une source de biomasse glucidique ou protéique au nid.'
            },
            {
                term: 'Phéromone d\'Alarme',
                definition: 'Signal à diffusion rapide déclenchant l\'agressivité ou la dispersion selon la concentration.'
            },
            {
                term: 'Phéromone Royale',
                definition: 'Cocktail d\'hydrocarbures cuticulaires signalant la fertilité et assurant la cohésion de la société.'
            }
        ]
    }
]

export default function LegendGlossaryModal({ isOpen, onClose }) {
    const { theme } = useSimulationStore()
    const isDark = theme === 'dark'
    const [activeTab, setActiveTab] = useState('legend') // 'legend' or 'glossary'
    const [searchQuery, setSearchQuery] = useState('')

    if (!isOpen) return null

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
                                📖 Guide & Légende Scientifique
                            </h3>
                            <div style={{ fontSize: 11, color: isDark ? '#94a3b8' : '#64748b' }}>
                                Répertoire des substrats, gradients de phéromones et lexique de myrmécologie
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

                {/* Sub-Tabs: Légende Visuelle vs Glossaire */}
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
                        🎨 Légende des Éléments 3D
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
                        📚 Glossaire Myrmécologique
                    </button>
                </div>

                {/* Body Content */}
                <div style={{ padding: 20, overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: 20 }}>
                    {activeTab === 'legend' ? (
                        <>
                            {/* Substrats Géologiques */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={15} /> Substrats & Strates Géologiques
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {[
                                        { name: 'Terre Végétale (Humus)', color: '#4d7c0f', desc: 'Couche superficielle arable, excavation rapide' },
                                        { name: 'Argile & Sédiment', color: '#854d0e', desc: 'Sol compact idéal pour la solidité des galeries' },
                                        { name: 'Roche Mère (Bedrock)', color: '#475569', desc: 'Substrat impénétrable formant le plancher géologique' },
                                        { name: 'Sable & Alluvions', color: '#ca8a04', desc: 'Matériau meuble sensible aux éboulements' },
                                        { name: 'Rivière & Flaques', color: '#0284c7', desc: 'Point d\'hydratation et obstacle de surface' },
                                        { name: 'Chambres & Tunnels', color: '#1e293b', desc: 'Espace excavé pour stockage et incubation' },
                                    ].map((sub, i) => (
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

                            {/* Phéromones (8 Canaux Canoniques 1:1 avec PheromoneType.java & PheromoneOverlay.java) */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#a855f7', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={15} /> Gradients de Phéromones (8 Canaux)
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {[
                                        { name: '1. Piste de Nourriture (Food)', color: '#4caf50', desc: 'Vert (#4caf50) : Guide vers la biomasse et le miellat' },
                                        { name: '2. Retour au Nid (Home)', color: '#2196f3', desc: 'Bleu (#2196f3) : Orientation vectorielle vers les entrées' },
                                        { name: '3. Alarme Chimique (Alarm)', color: '#f44336', desc: 'Rouge (#f44336) : Mobilisation défensive et alerte de danger' },
                                        { name: '4. Recrutement de Masse (Trail)', color: '#ffc107', desc: 'Jaune (#ffc107) : Amplification du flux d\'ouvrières' },
                                        { name: '5. Phéromone Royale (Queen)', color: '#9c27b0', desc: 'Violet (#9c27b0) : Fertilité et cohésion coloniale' },
                                        { name: '6. Soin du Couvain (Brood)', color: '#ff9800', desc: 'Orange (#ff9800) : Reconnaissance des larves et œufs' },
                                        { name: '7. Nécrophorèse & Déchets (Death)', color: '#607d8b', desc: 'Gris/Ardoise (#607d8b) : Transport vers le dépotoir' },
                                        { name: '8. Marquage Territorial (Territory)', color: '#009688', desc: 'Sarcelle (#009688) : Délimitation des frontières du nid' },
                                    ].map((phero, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            <div style={{ width: 18, height: 18, borderRadius: '50%', background: phero.color, flexShrink: 0, boxShadow: `0 0 8px ${phero.color}` }} />
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{phero.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{phero.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Castes */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#fbbf24', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Crown size={15} /> Castes d\'Insectes Sociaux
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {[
                                        { name: 'Reine', icon: <Crown size={14} color="#fbbf24" />, desc: 'Longévité maximale, ponte des œufs' },
                                        { name: 'Ouvrière', icon: <HardHat size={14} color="#38bdf8" />, desc: 'Soins, construction, récolte de nourriture' },
                                        { name: 'Soldat', icon: <Shield size={14} color="#ef4444" />, desc: 'Garde de l\'entrée, dissuasion prédateurs' },
                                    ].map((caste, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            {caste.icon}
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
                            {/* Search Field */}
                            <div style={{ position: 'relative' }}>
                                <Search size={14} color="#94a3b8" style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)' }} />
                                <input
                                    type="text"
                                    placeholder="Rechercher un terme (ex: Stigmergie, Quorum, Trophallaxie...)"
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
                                {GLOSSARY_CATEGORIES.map((cat) => {
                                    const filteredItems = cat.items.filter(item =>
                                        item.term.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                        item.definition.toLowerCase().includes(searchQuery.toLowerCase())
                                    )

                                    if (filteredItems.length === 0) return null

                                    return (
                                        <div key={cat.id}>
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
